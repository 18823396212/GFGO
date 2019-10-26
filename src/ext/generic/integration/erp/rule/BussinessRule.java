/**
 *
 * Copyright (c) 2017-2030 上海湃睿信息科技有限公司 (PISX). All Rights Reserved.
 *
 * This software is the confidential and proprietary information of PISX
 * and is subject to the terms of a software license agreement. You shall
 * not disclose such confidential information and shall use it only in accordance
 * with the terms of the license agreement.
 *
 */
package ext.generic.integration.erp.rule;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.ptc.netmarkets.workflow.NmWorkflowHelper;

import wt.access.AccessControlHelper;
import wt.access.AccessPermission;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.inf.container.WTContainer;
import wt.inf.team.ContainerTeam;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.team.ContainerTeamManaged;
import wt.log4j.LogR;
import wt.org.OrganizationServicesHelper;
import wt.org.WTGroup;
import wt.org.WTOrganization;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.part.WTPart;
import wt.project.Role;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.workflow.engine.WfProcess;
import ext.appo.part.workflow.PartWorkflowUtil;
import ext.com.org.OrgUtil;
import ext.customer.common.MBAUtil;
import ext.generic.integration.erp.common.CommonConstant;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.generic.integration.erp.util.BOMStructureUtil;
import ext.generic.integration.erp.util.BusinessRuleXMLConfigUtil;
/**
 * ERP集成规则控制类
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-10-08
 */
public class BussinessRule {
	private static final String clazz = BussinessRule.class.getName() ;
	private static final Logger logger = LogR.getLogger(clazz);
	/**
	 * 判断零部件软类型是否可发布
	 * 
	 * @param part
	 * @return
	 */
	public static boolean canReleasePart(WTPart part) {
		boolean canReleasePart = true;

		try {
			if (part != null) {
				//判断类型
				String softType = CommonPDMUtil.getSoftType(part);

				if (BusinessRuleXMLConfigUtil.getInstance().getNoReleasePartSoftTypeList().contains(softType)) {
					canReleasePart = false;
				}
				
				//判断分类
				if(BusinessRuleXMLConfigUtil.getInstance().getNoReleasePartClassficationList().size() > 0){
					
					String classfication = (String) MBAUtil.getValue(part, "Classification");
					
					if(BusinessRuleXMLConfigUtil.getInstance().getNoReleasePartClassficationList().contains(classfication)){
						canReleasePart = false;
					}
				}
				
				//判断属性
				Map ibaMap = BusinessRuleXMLConfigUtil.getInstance().getNoRelasePartIBAList();
				if(ibaMap.size() > 0){
					Iterator<String> ite = ibaMap.keySet().iterator();
					
					while( ite.hasNext()){
						String noReleaseIBA = ite.next();
						String noRelaseValue = (String) ibaMap.get(noReleaseIBA) ;
												
						String ibaValue =  (String) MBAUtil.getValue(part, noReleaseIBA);
						
						String[] noRelaseValueArray = noRelaseValue.split(CommonConstant.COMMA_SEPARATOR);
						List noRelaseValueList = Arrays.asList(noRelaseValueArray);
						if(noRelaseValueList.contains(ibaValue)){
							canReleasePart = false;
						}
					}
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return canReleasePart;
	}
	
	/**
	 * 判断零部件是否可发布
	 * @param part
	 * @return
	 */
	public static boolean isPartCanRelease(WTPart part){
		boolean canReleasePart = true;
		try {
			if (part != null) {
				String softType = CommonPDMUtil.getSoftType(part);

				if (BusinessRuleXMLConfigUtil.getInstance().getNoReleasePartSoftTypeList().contains(softType)) {
					canReleasePart = false;
				}else{
					//如果零部件处于允许发布至中间表的状态，并且为允许发布至中间表的视图
					if( !(isManuallyPublishAllowedState( part, "part" ) && isManuallyPublishAllowedView( part ))){
						canReleasePart = false ;
					}
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return canReleasePart;
	}
	
	/**
	 * 检查部件的直接一层子件的状态是否符合发布ERP的状态
	 * @param parent 父件
	 * @param childPartList 子件集合
	 * @return
	 */
	public static String checkChildListState(WTPart parent, List<WTPart> childPartList){
		StringBuilder builder = new StringBuilder("");
		try {
			if(childPartList==null || childPartList.size()<=0){
				childPartList = BOMStructureUtil.getFirstLevelChildren(parent);
			}
			for(WTPart part : childPartList){
				
				boolean ismbomprocess=ismbom(parent);
				if(BussinessRule.canReleasePart(part)){//符合发布的类型
//					String state = part.getLifeCycleState().toString();
					if (ismbomprocess) {
						String mess=PartWorkflowUtil.checkSonHistoryversion(part);
						builder.append(mess);
					}else{
					
					if(!(isManuallyPublishAllowedState( part, "part" ))){
						builder.append("子件：").append(part.getNumber()).append("状态不符合条件").append("\n");
					}
					}
				}
			}
			if(builder.length()>0){
				builder.insert(0, "父件:"+parent.getNumber()+"的");
			}
		} catch (WTException e) {
			e.printStackTrace();
			builder.append("检查子件状态时发生异常").append(e).append("\n");
		}
		return builder.toString().trim();
	}
	
	public static boolean  ismbom(WTPart part){
        QueryResult processResult=new QueryResult();
        boolean flag=false;
        try {
			processResult = NmWorkflowHelper.service.getAssociatedProcesses(part, null, null);
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("processResult size====="+processResult.size());
	      while(processResult.hasMoreElements()) {
		         WfProcess process = (WfProcess) processResult.nextElement();
		         System.out.println("process.getState()..toString()"+process.getState().toString());
		         System.out.println("process name====="+process.getName());
		         if (process.getName().startsWith("GenericManufacturingPartWF")&&process.getState().toString().endsWith("OPEN_RUNNING")){
		        	 flag=true;
				}
		      }	
	      return flag;
	}
	/**
	 * 是否启用数据备份功能
	 * 
	 * @return
	 */
	public static boolean startBackupData() {
		return BusinessRuleXMLConfigUtil.getInstance().isStartBackupData(); 
	}
	
	/**
	 * 是否发布全局替代关系
	 * 
	 * @return
	 */
	public static boolean isAlternateLinkRelease() {
		return BusinessRuleXMLConfigUtil.getInstance().isReleaseAlternateLink(); 
	}

	/**
	 * 判断当前用户是否可以查询价格信息
	 * 
	 * @return
	 */
	public static boolean canReadPrice() {
		boolean canReadPrice = false;
		
		try{			
			WTPrincipal currentPrincipal = SessionHelper.getPrincipal() ;
			
			WTOrganization wtorganization = OrganizationServicesHelper.manager.getOrganization(currentPrincipal);
			
			if( currentPrincipal != null ){
				boolean isGroupUser = isReadPriceGroupUser( currentPrincipal, wtorganization ) ;
				
				if( isGroupUser ){
					canReadPrice = true ;
				}
			}else{
			}
		} catch (WTException e) {
			e.printStackTrace();
		}finally{
			
		}
		
		return canReadPrice;
	}
	
	/**
	 * 判断当前Session的用户，是否为特殊用户组中的用户
	 * 
	 * @param currentPrincipal	当前Session中的Principal
	 * @param org	当前对象所属的组织
	 * @return
	 * @throws WTException
	 */
	private static boolean isReadPriceGroupUser( WTPrincipal currentPrincipal, WTOrganization org) throws WTException {	
		boolean isGroupUser = false ;
		
		if( org != null ){

			List<String> userGroup = BusinessRuleXMLConfigUtil.getInstance().getReadPriceUserGroup();
			
			Iterator<String> groupIte = userGroup.iterator() ;
			while( groupIte.hasNext()){
				String groupName = groupIte.next() ;
				
				WTGroup group = OrgUtil.getGroupByName( org , groupName );
				if(group == null){
					logger.debug("以下组获取失败，请检查是否在系统中定义"+  groupName);
				}
				isGroupUser = isGroupUser(currentPrincipal , group);
				
				if( isGroupUser ){
					return isGroupUser ;
				}
			}
		}else{
		}
		
		return isGroupUser;
	}
	
	/**
	 * 判断当前用户，是否可以执行手工发布零部件信息到中间表的操作
	 * 
	 * @param part
	 * @return
	 */
	public static boolean showManuallyPublishPartMenu( WTPart part ){
		boolean result = false ;

		try {
			WTPrincipal principal = SessionHelper.getPrincipal() ;
			
			if( principal != null ){
				//如果当前用户为授权的角色，或者当前用户属于授权的组，或者当前用户对零部件具有管理权限
				if( isManuallyPublishAllowedRole( principal , part , "part" ) || isManuallyPublishAllowedUserGroup( principal , part , "part" ) || isManuallyPublishAllowedPermission( part ) ){
					
					//如果零部件处于允许发布至中间表的状态，并且为允许发布至中间表的视图
					if( isManuallyPublishAllowedState( part, "part" ) && isManuallyPublishAllowedView( part )){
						result = true ;
					}
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}

		return result ;
	}
	
	/**
	 * 判断当前用户，是否可以执行手工发布BOM信息到中间表的操作
	 * 
	 * @param part
	 * @return
	 */
	public static boolean showManuallyPublishBOMMenu( WTPart part ){
		boolean result = false ;

		try {
			WTPrincipal principal = SessionHelper.getPrincipal() ;
			
			if( principal != null ){
				//如果当前用户为授权的角色，或者当前用户属于授权的组，或者当前用户对零部件具有管理权限
				if( isManuallyPublishAllowedRole( principal , part , "bom" ) || isManuallyPublishAllowedUserGroup( principal , part , "bom" ) || isManuallyPublishAllowedPermission( part ) ){
					
					//如果零部件处于允许发布至中间表的状态，并且为允许发布至中间表的视图
					if( isManuallyPublishAllowedState( part , "bom") && isManuallyPublishAllowedView( part )){
						result = true ;
					}
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}

		return result ;
	}
	
	/**
	 * 判断当前用户，是否为授权的角色
	 * 
	 * @param currentPrincipal
	 * @param part
	 * @return
	 * @throws WTException
	 */
	private static boolean isManuallyPublishAllowedRole( WTPrincipal currentPrincipal , WTPart part , String type ) throws WTException{
		boolean isAllowedRole = false ;
		
		List<String> userRoleList = null;
		
		if( type.equals("part") ){
			userRoleList = BusinessRuleXMLConfigUtil.getInstance().getManuallyPublishUserRole() ;
		}else if( type.equals("bom") ){
			userRoleList =  BusinessRuleXMLConfigUtil.getInstance().getManuallyPublishBOMUserRole();
		}
		
		if( userRoleList == null ){
			return isAllowedRole;
		}
		
		if( userRoleList.contains( "ALL" ) ){
			return true;
		}
		
		WTContainer container = part.getContainer();
		
		if( container != null && container instanceof ContainerTeamManaged){
			ContainerTeamManaged containerTeamManaged = ( ContainerTeamManaged ) container ;
			
			ContainerTeam containerTeam = ContainerTeamHelper.service.getContainerTeam( containerTeamManaged );
			
			if( containerTeam != null ){
				Vector roles = containerTeam.getRoles();

				Iterator ite = roles.iterator() ;

				while(ite.hasNext()){
					Object roleObj = ite.next() ;
					
					if( roleObj != null && roleObj instanceof Role ){
						Role role = (Role) roleObj ;
						
						String roleName = role.getDisplay( Locale.CHINA ) ;
						

						if( userRoleList.contains( roleName )){
							Enumeration participants= containerTeam.getPrincipalTarget( role );
							
							isAllowedRole = hasCurrentPrincipal( participants , currentPrincipal );
							
							if( isAllowedRole ){

								return isAllowedRole ;
							}
						}
					}
				}
			}	
		}
		
		
		return isAllowedRole ;
	}

	/**
	 * 判断当前用户是否在集合中
	 * 
	 * @param participants
	 * @param currentPrincipal
	 * @return
	 * @throws WTException
	 */
	private static boolean hasCurrentPrincipal( Enumeration participants , WTPrincipal currentPrincipal ) throws WTException {
		boolean hasCurrentPrincipal = false ;
		
		WTPrincipal principal = null ;
		
		if( participants != null ){
			while( participants.hasMoreElements() ){
				Object obj = participants.nextElement() ;
				
				if( obj != null && obj instanceof WTPrincipal ){
					principal = ( WTPrincipal ) obj ;
				}else if( obj != null && obj instanceof WTPrincipalReference ){
					WTPrincipalReference wtprincipalReference = ( WTPrincipalReference ) obj ;
					principal = wtprincipalReference.getPrincipal() ;					
				}else{
//					if( obj == null ){
//						PDMIntegrationLogUtil.printLog(clazz, ">>>>> hasCurrentPrincipal , obj == null ", IntegrationConstant.LOG_LEVEL_WARN ) ;
//					}else{
//						PDMIntegrationLogUtil.printLog(clazz, ">>>>> hasCurrentPrincipal , obj class type is " + obj.getClass() , IntegrationConstant.LOG_LEVEL_WARN ) ;
//					}
				}
				
				if( principal != null ){
					hasCurrentPrincipal = PersistenceHelper.isEquivalent( principal , currentPrincipal );
				}
				
				if( hasCurrentPrincipal ){
					return hasCurrentPrincipal ;
				}
			}
		}
		
		return hasCurrentPrincipal ;
	}
	
	/**
	 * 判断当前用户是否属于授权的用户组
	 * 
	 * @param currentPrincipal
	 * @param part
	 * @return
	 * @throws WTException
	 */
	private static boolean isManuallyPublishAllowedUserGroup( WTPrincipal currentPrincipal , WTPart part , String type ) throws WTException{
		boolean isAllowedUserGroup = false ;
		
		WTOrganization org = CommonPDMUtil.getOrganization( part ) ;
		
		List<String> userGroupList = null;
		
		if( type.equals("part") ){
			userGroupList = BusinessRuleXMLConfigUtil.getInstance().getManuallyPublishUserGroup() ;
		}else if( type.equals("bom") ){
			userGroupList = BusinessRuleXMLConfigUtil.getInstance().getManuallyPublishBOMUserGroup() ;
		}
		
		if( userGroupList == null ){
			return isAllowedUserGroup;
		}
		
		if( userGroupList.contains("ALL") ){
			return true;
		}
		
		Iterator<String> groupIte = userGroupList.iterator() ;
		while( groupIte.hasNext()){
			String groupName = groupIte.next() ;
			
			WTGroup group = OrgUtil.getGroupByName( org , groupName );
			
			isAllowedUserGroup = isGroupUser(currentPrincipal , group);
			
			if( isAllowedUserGroup ){
				
				return isAllowedUserGroup ;
			}
		}
		
		
		return isAllowedUserGroup ;
	}
	
	/**
	 * 判断当前用户是否为零部件的管理者
	 * 
	 * @param part
	 * @return
	 * @throws WTException
	 */
	private static boolean isManuallyPublishAllowedPermission( WTPart part ) throws WTException{
		boolean isAllowedPermission = AccessControlHelper.manager.hasAccess( part ,AccessPermission.ADMINISTRATIVE);
		
		
		return isAllowedPermission ;
	}
	
	/**
	 * 判断当前零部件是否为可发布至中间表的生命周期状态
	 * 
	 * @param part
	 * @return
	 * @throws WTException
	 */
	private static boolean isManuallyPublishAllowedState( WTPart part ) throws WTException{
		boolean isAllowedState = false ;
		
		List<String> partStateList = BusinessRuleXMLConfigUtil.getInstance().getManuallyPublishPartState() ;
		
		//如果包含ALL，则所有状态都可以发布
		if( partStateList.contains("ALL") ){
			isAllowedState = true ;
		}else{
			String lifecycleState = CommonPDMUtil.getLifecycleCN( part ) ;
			
			if( lifecycleState != null && partStateList.contains( lifecycleState ) ){
				isAllowedState = true ;
			}
		}
		
//		PDMIntegrationLogUtil.printLog(clazz, ">>>>> is Allowed State : " + isAllowedState ) ;

		return isAllowedState ;
	}
	
	/**
	 * 判断当前零部件是否为可发布至中间表的生命周期状态
	 * 
	 * @param part
	 * @return
	 * @throws WTException
	 */
	private static boolean isManuallyPublishAllowedState( WTPart part , String type) throws WTException{
		boolean isAllowedState = false ;
		
		List<String> partStateList = null ;
		
		if( type.equals("part") ){
			partStateList = BusinessRuleXMLConfigUtil.getInstance().getManuallyPublishPartState() ;
		}else if( type.equals("bom") ){
			partStateList = BusinessRuleXMLConfigUtil.getInstance().getManuallyPublishBOMState() ;
		}
		
		//如果包含ALL，则所有状态都可以发布
		if( partStateList.contains("ALL") ){
			isAllowedState = true ;
		}else{
			String lifecycleState = CommonPDMUtil.getLifecycleCN( part ) ;
			
			if( lifecycleState != null && partStateList.contains( lifecycleState ) ){
				isAllowedState = true ;
			}
		}
		

		return isAllowedState ;
	}
	
	/**
	 * 判断零部件是否为可发布中间表的视图
	 * 
	 * @param part
	 * @return
	 */
	private static boolean isManuallyPublishAllowedView( WTPart part ){
		boolean isAllowedView = false ;
		
		List<String> partViewList = BusinessRuleXMLConfigUtil.getInstance().getManuallyPublishPartView() ;
		
		//如果包含ALL，则所有视图都可以发布
		if( partViewList.contains("ALL") ){
			isAllowedView = true ;
		}else{
			String partView = part.getViewName() ;
			
			if( partView != null && partViewList.contains(partView) ){
				isAllowedView = true ;
			}
		}
		

		return isAllowedView ;
	}
	
	public static boolean isGroupUser(WTPrincipal currentPrincipal , WTGroup group) throws WTException {
		boolean isGroupUser = false ;
		
		logger.debug("currentPrincipal" + currentPrincipal.getName());
		
		if( group != null ){
			
			//获取组成员
			Enumeration members= group.members();
			while( members.hasMoreElements()) {
				Object userObj =  members.nextElement();
				
				if( userObj != null && userObj instanceof WTPrincipal ){
					WTPrincipal groupPrincipal = ( WTPrincipal ) userObj ;
					logger.debug("groupPrincipal" + groupPrincipal.getName());
					
					isGroupUser = PersistenceHelper.isEquivalent( groupPrincipal , currentPrincipal ) ;

					if( isGroupUser ){
						return isGroupUser ;
					}
				}
			}
		}
		
		
		return isGroupUser ;
	}
}
