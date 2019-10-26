package ext.appo.ecn.common.util;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import wt.access.AccessPermission;
import wt.change2.ChangeActivityIfc;
import wt.change2.Changeable2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.configurablelink.ConfigurableDescribeLink;
import wt.doc.WTDocument;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.identity.IdentityFactory;
import wt.lifecycle.LifeCycleManaged;
import wt.log4j.LogR;
import wt.org.WTGroup;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.pom.PersistentObjectManager;
import wt.pom.Transaction;
import wt.session.SessionHelper;
import wt.team.Team;
import wt.team.TeamHelper;
import wt.team.TeamManaged;
import wt.util.WTException;
import wt.workflow.engine.WfProcess;

import com.ptc.windchill.enterprise.change2.ChangeTaskRoleParticipantHelper;
import com.ptc.windchill.enterprise.part.commands.PartDocServiceCommand;

import ext.appo.ecn.constants.ChangeConstants;
import ext.com.workflow.WorkflowUtil;
import ext.lang.PIStringUtils;
import ext.pi.core.PIAccessHelper;
import ext.pi.core.PICoreHelper;

public class ChangeWorkFlowUtils {

	private static final String CLASSNAME = ChangeWorkFlowUtils.class.getName() ;
	private static final Logger LOG = LogR.getLogger(CLASSNAME) ;
	
	/***
	 * 修订ECA中所有受影响对象，并添加至产生对象列表中
	 * 
	 * @param pbo
	 * @throws WTException
	 */
	public static void reviseAddChangeRecord2(WTObject pbo) throws WTException{
		if(!(pbo instanceof WTChangeOrder2)){
			return ;
		}
		WTChangeOrder2 changeOrder2 = (WTChangeOrder2) pbo ;
		if(LOG.isDebugEnabled()){
			LOG.debug("reviseAddChangeRecord2() ... WTChangeOrder2 : " + changeOrder2.getDisplayIdentity());
		}
		
		Transaction tx = null;
		// 事务能否提交，通常情况下，哪个方法启动事务，哪个方法提交事务
		boolean canCommit = false;
		// 当前用户
//		WTPrincipal principal = SessionHelper.manager.getPrincipal();     
		try {
			//开启事物
			if (!PersistentObjectManager.getPom().isTransactionActive()) {
				tx = new Transaction();
				tx.start();
				canCommit = true;
			}
			// 获取ECN中所有变更信息
			Map<ChangeActivityIfc, Collection<Changeable2>> changeMap = ChangeUtils.getChangeablesBeforeInfo(changeOrder2) ;
			for(Map.Entry<ChangeActivityIfc, Collection<Changeable2>> entryMap : changeMap.entrySet()){
				WTChangeActivity2 eca = (WTChangeActivity2)entryMap.getKey() ;
				if(PICoreHelper.service.isType(eca, ChangeConstants.TRANSACTIONAL_CHANGEACTIVITY2)){
					continue ;
				}
				if(eca.getState().toString().equalsIgnoreCase("OPEN")){
//					WTUser user = getChangeTaskAssignee(eca) ;
//					// 设置当前操作用户
//					if(user != null){
//						SessionHelper.manager.setPrincipal(user.getName());
//					}
					// 修订并添加数据
					ChangeUtils.reviseAndAddChangeRecord2(eca, entryMap.getValue()) ;
				}
			}
			
			if (canCommit) {
				tx.commit();
				tx = null;
			}
		} catch (Exception e) {
			LOG.error( "当前用户：" + SessionHelper.manager.getPrincipal().getName() );
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage()) ;
		} finally{
			if (canCommit) {
				if (tx != null) {
					tx.rollback();
					tx = null;
				}
			}
//			SessionHelper.manager.setPrincipal(principal.getName());
		}
	}
	
	/***
	 * 获取ECA对象上的工作负责人
	 * 
	 * @param changeActivity2 
	 * @return
	 * @throws WTException
	 */
	public static WTUser getChangeTaskAssignee(WTChangeActivity2 changeActivity2) throws WTException{
		WTUser user = null ;
		if(changeActivity2 == null){
			return user ;
		}
		
		try {
			// 获取工作负责人
			Enumeration<?> roleem = ChangeTaskRoleParticipantHelper.getRoleParticipants(changeActivity2, ChangeConstants.ROLE_ASSIGNEE);
			while(roleem.hasMoreElements()){
				Object object = roleem.nextElement() ;
				if(object instanceof ObjectReference){
					object = ((ObjectReference)object).getObject() ;
				}
				if(object instanceof WTUser){
					user = (WTUser) object;
					break ;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage()) ;
		}
		
		return user ;
	}
	
	/***
	 * 批量设置ECN中ECA状态
	 * 
	 * @param pbo
	 * @param targetState
	 *            目标状态
	 * @param orgState
	 *            原状态(存在时对象状态必须满足时才进行状态设置)
	 * @throws WTException 
	 */
	public static void setChangeActivitiesState(WTObject pbo, String targetState, String orgState) throws WTException{
		if(pbo == null || !(pbo instanceof WTChangeOrder2) || PIStringUtils.isNull(targetState)){
			return ;
		}
		try {
			WTChangeOrder2 changeOrder2 = (WTChangeOrder2)pbo ;
			// 获取ECN中所有ECA对象
			Collection<ChangeActivityIfc> ecaArray = ChangeUtils.getChangeActivities(changeOrder2) ;
			for(ChangeActivityIfc changeActivityIfc : ecaArray){
				if(changeActivityIfc instanceof LifeCycleManaged){
					LifeCycleManaged lifeCycleManaged = (LifeCycleManaged) changeActivityIfc ;
					if(PIStringUtils.isNotNull(orgState)){
						if(lifeCycleManaged.getState().toString().equalsIgnoreCase(orgState)){
							WorkflowUtil.setLifeCycleState(lifeCycleManaged, targetState);
						}
					}else{
						WorkflowUtil.setLifeCycleState(lifeCycleManaged, targetState);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage()) ;
		}
	}
	
	/***
	 * 流程批量删权
	 * 
	 * @param self
	 *            流程对象
	 * @param pbo
	 *            流程PBO
	 * @param authority
	 *            权限 完全控制(-1)、读取(0)、下载(10)、修改(1)、修改标识符(16)、修订(7)、删除(5)
	 * @throws WTException
	 */
	public static void removePersistablePermissions(ObjectReference self, WTObject pbo, int authority) throws WTException{
		if(self == null || pbo == null){
			return ;
		}
		
		// 流程对象
		Persistable persistable = self.getObject();
		if(LOG.isDebugEnabled()){
			LOG.debug("self : " + IdentityFactory.getDisplayIdentity(persistable));
			LOG.debug("WTObject : " + IdentityFactory.getDisplayIdentity(pbo));
		}
		if(persistable instanceof WfProcess){
			WfProcess process = (WfProcess)persistable ;
			// 获取ECN关联的所有对象
			Collection<Persistable> ptArray = new HashSet<Persistable>() ;
			if(pbo instanceof WTChangeOrder2){
				ptArray = getAllAssociationObject((WTChangeOrder2)pbo) ;
			}else if(pbo instanceof WTDocument){
				WTDocument doc = (WTDocument) pbo ;
				// 说明部件
				QueryResult qr = PartDocServiceCommand.getAssociatedDescParts(doc) ;
				while(qr.hasMoreElements()){
					Object object = qr.nextElement() ;
					if(object instanceof ObjectReference){
						object = ((ObjectReference)object).getObject() ;
					}
					if(object instanceof WTPart){
						ptArray.add((WTPart)object) ;
					}
				}
			}
			// 获取流程团队中所有用户
			Collection<WTUser> userArray = getAllUserByTeam(process) ;
			for(WTUser user : userArray){
				// 批量赋权
				removePersistablePermissions(ptArray, user, authority);
			}
		}
	}
	
	/***
	 * 批量删权
	 * 
	 * @param dataArray
	 *            授权对象
	 * @param user
	 *            授权用户
	 * @param authority
	 *            权限 完全控制(-1)、读取(0)、下载(10)、修改(1)、修改标识符(16)、修订(7)、删除(5)
	 * @throws WTException
	 */
	public static void removePersistablePermissions(Collection<Persistable> dataArray,
			WTUser user, int authority) throws WTException{
		if(dataArray == null || dataArray.size() == 0 || user == null){
			return ;
		}
		
		try {
			AccessPermission accessPermission = AccessPermission.toAccessPermission(authority+"") ;
			if(accessPermission == null){
				throw new WTException("删除权限：" + authority + " 不存在!") ;
			}
			for(Persistable persistable : dataArray){
				PIAccessHelper.service.removePersistablePermissions(persistable, 
						WTPrincipalReference.newWTPrincipalReference(user), accessPermission, true);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace(); 
			throw new WTException(e.getLocalizedMessage()) ;
		}
	}
	
	/***
	 * 流程批量授权
	 * 
	 * @param self
	 *            流程对象
	 * @param pbo
	 *            流程PBO
	 * @param authority
	 *            权限 完全控制(-1)、读取(0)、下载(10)、修改(1)、修改标识符(16)、修订(7)、删除(5)
	 * @throws WTException
	 */
	public static void grantPersistablePermissions(ObjectReference self, WTObject pbo, int authority) throws WTException{
		if(self == null || pbo == null){
			return ;
		}
		
		// 流程对象
		Persistable persistable = self.getObject();
		if(LOG.isDebugEnabled()){
			LOG.debug("self : " + IdentityFactory.getDisplayIdentity(persistable));
			LOG.debug("WTObject : " + IdentityFactory.getDisplayIdentity(pbo));
		}
		if(persistable instanceof WfProcess){
			WfProcess process = (WfProcess)persistable ;
			// 获取ECN关联的所有对象
			Collection<Persistable> ptArray = new HashSet<Persistable>() ;
			if(pbo instanceof WTChangeOrder2){
				ptArray = getAllAssociationObject((WTChangeOrder2)pbo) ;
			}else if(pbo instanceof WTDocument){
				WTDocument doc = (WTDocument) pbo ;
				// 说明部件
				QueryResult qr = PartDocServiceCommand.getAssociatedDescParts(doc) ;
				while(qr.hasMoreElements()){
					Object object = qr.nextElement() ;
					if(object instanceof ObjectReference){
						object = ((ObjectReference)object).getObject() ;
					}
					if(object instanceof WTPart){
						ptArray.add((WTPart)object) ;
					}
				}
			}
			// 获取流程团队中所有用户
			Collection<WTUser> userArray = getAllUserByTeam(process) ;
			for(WTUser user : userArray){
				// 批量赋权
				grantPersistablePermissions(ptArray, user, authority);
			}
		}
	}
	
	/***
	 * 批量授权
	 * 
	 * @param dataArray
	 *            授权对象
	 * @param user
	 *            授权用户
	 * @param authority
	 *            权限 完全控制(-1)、读取(0)、下载(10)、修改(1)、修改标识符(16)、修订(7)、删除(5)
	 * @throws WTException
	 */
	public static void grantPersistablePermissions(Collection<Persistable> dataArray,
			WTUser user, int authority) throws WTException{
		if(dataArray == null || dataArray.size() == 0 || user == null){
			return ;
		}
		
		try {
			AccessPermission accessPermission = AccessPermission.toAccessPermission(authority+"") ;
			if(accessPermission == null){
				throw new WTException("赋予权限：" + authority + " 不存在!") ;
			}
			for(Persistable persistable : dataArray){
				PIAccessHelper.service.grantPersistablePermissions(persistable, 
						WTPrincipalReference.newWTPrincipalReference(user), accessPermission, true);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace(); 
			throw new WTException(e.getLocalizedMessage()) ;
		}
	}
	
	/***
	 * 获取ECN关联的所有对象
	 * 
	 * @param changeOrder2
	 * @return
	 * @throws WTException
	 */
	public static Collection<Persistable> getAllAssociationObject(WTChangeOrder2 changeOrder2) throws WTException{
		Collection<Persistable> ptArray = new HashSet<Persistable>() ;
		if(changeOrder2 == null){
			return ptArray ;
		}
		if(LOG.isDebugEnabled()){
			LOG.debug("getAllAssociationObject()  WTChangeOrder2 : " + changeOrder2.getDisplayIdentity()); 
		}
		
		try {
			// 获取ECN中所有受影响对象
			ptArray.addAll(ChangeUtils.getChangeablesBefore(changeOrder2)) ;
			// 获取ECN中所有产品对象
			QueryResult qr = PersistenceHelper.manager.navigate(changeOrder2 , ConfigurableDescribeLink.DESCRIBED_BY_ROLE, ConfigurableDescribeLink.class, true);
			while(qr.hasMoreElements()){
				Object value = qr.nextElement() ;
				if(value instanceof WTPart){
					ptArray.add((WTPart) value) ;
				}
			}
			// 获取ECN中所有ECA对象
			ptArray.addAll(ChangeUtils.getChangeActivities(changeOrder2)) ;
			
			if(LOG.isDebugEnabled()){
				for(Persistable persistable : ptArray){
					LOG.debug("getAllAssociationObject() ... " + IdentityFactory.getDisplayIdentifier(persistable)) ;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage()) ;
		}
		
		return ptArray ;
	}
	
	
	/***
	 * 获取指定对象团队中所有用户
	 * 
	 * @param teamManaged
	 * @return
	 * @throws WTException 
	 */
	public static Collection<WTUser> getAllUserByTeam(TeamManaged teamManaged) throws WTException{
		Collection<WTUser> userArray = new HashSet<WTUser>() ;
		if(teamManaged == null){
			return userArray ;
		}
		if(LOG.isDebugEnabled()){
			LOG.debug("getAllUserByTeam()  TeamManaged : " + teamManaged.getTeamIdentity()); 
		}
		
		try {
			// 获取团队
			Team team = (Team) teamManaged.getTeamId().getObject();
			if(team != null){
				HashMap<?, ?> teamMap = TeamHelper.service.findAllParticipantsByRole(team);
				Iterator<?> keyIterator = teamMap.keySet().iterator() ;
				while(keyIterator.hasNext()){
					List<?> principalList = (List<?>)teamMap.get(keyIterator.next()) ;
					for( int j = 0 ; j < principalList.size() ; j++ ){
						WTPrincipal principal = toPrincipal( principalList.get(j) );
						if( principal != null ){
							userArray.addAll(toWTUser( principal ));
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage()) ;
		}
		
		return userArray ;
	}
	
	/***
	 * Object对象 转换为 WTPrincipal
	 * 
	 * @param object
	 * @return
	 */
	public static WTPrincipal toPrincipal(Object object){
		WTPrincipal principal = null;
		try {
			if( object != null ){
				if( object != null && object instanceof WTPrincipal ){
					principal = ( WTPrincipal ) object ;
				}else if( object != null && object instanceof WTPrincipalReference ){
					principal = ((WTPrincipalReference)object).getPrincipal() ;					
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return principal;
	}
	
	/***
	 * WTPrincipal对象 转换为 WTUser对象
	 * 
	 * @param principal
	 * @return
	 */
	public static Collection<WTUser> toWTUser(WTPrincipal principal){
		Collection< WTUser > userList = new HashSet< WTUser >();
		if( principal == null ){
			return userList;
		}
		try {
			if(principal instanceof WTUser){
				userList.add( (WTUser)principal );
			}else if(principal instanceof WTGroup){
				userList.addAll(getGroupUser((WTGroup)principal));
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return userList;
	}
	
	/***
	 * 获取用户组中所有的用户
	 * 
	 * @param group
	 * @return
	 * @throws WTException
	 */
	public static Collection<WTUser> getGroupUser( WTGroup group )throws WTException {
		Collection<WTUser> users = new HashSet<WTUser>();
		if( group == null ){
			return users;
		}
		//获取用户组成员
		Enumeration<?> member = group.members();
		while( member.hasMoreElements() ){
			WTPrincipal principal = (WTPrincipal)member.nextElement();
			if( principal instanceof WTUser ){
				users.add((WTUser)principal);
			}else if( principal instanceof WTGroup ){
				users.addAll(getGroupUser((WTGroup)principal) );
			}
		}
		return users;
	}
}
