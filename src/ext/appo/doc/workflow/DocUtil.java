package ext.appo.doc.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import ext.com.workflow.WorkflowUtil;
import ext.generic.doc.util.ReadEmailConfigUtil;
import ext.generic.reviewObject.model.ProcessReviewObjectLink;
import ext.generic.reviewObject.model.ProcessReviewObjectLinkHelper;
import ext.generic.reviewObject.util.ReviewObjectAndWorkFlowLink;
import ext.generic.reviewObject.util.ReviewObjectOfDocUtil;
import ext.generic.workflow.WorkFlowBase;
import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.fc.ObjectReference;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.lifecycle.LifeCycleManaged;
import wt.log4j.LogR;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTMessage;
import wt.vc.VersionControlHelper;
import wt.workflow.engine.WfProcess;

/**
 * 流程帮助类
 */
public class DocUtil extends WorkFlowBase{

	private static final String RESOURCE = "ext.generic.reviewObject.resource.ReviewObjectResourceRB";
//	private static Locale locale = null;
	private static String CLASSNAME = DocUtil.class.getName();
	private static final Logger LOGGER = LogR.getLogger(CLASSNAME);
	
//	static {
//		try {
//			locale = SessionHelper.manager.getLocale();
//		} catch (Throwable throwable) {
//			throw new ExceptionInInitializerError(throwable);
//		}
//	}
	
	public DocUtil(){
	}
	
	public DocUtil(WTObject pbo, ObjectReference self){
		if(pbo != null && self != null){
			this.pbo = pbo;
			this.self = self;
		}
	}
	
	/**
	 * 检查随签对象
	 * @param list
	 * @throws WTException
	 */
	public void check() throws WTException{
		if(this.self != null && this.pbo != null){
			WTArrayList list = collect();
			
			isObjsCheckedOut(list);
			
			checkISlatestVersion(list);
	
			checkReviewObjectStates(list);
	
			checkIsAssociatedRunningProcesses(list);
			
			getErrorMessge();
			if(!checkPboIsDelect(list)){
//				errorMessage.append("\n 随签列表中没有主对象，请点击重新加载随签对象按钮");
				Locale locale = SessionHelper.manager.getLocale();
				errorMessage.append(WTMessage.getLocalizedMessage(RESOURCE, "WF_PBO_NULL", null, locale));
				getErrorMessge();
			}
		}
		
	}
	
	/**
	 * 文控中心检查随签对象
	 * @param list
	 * @throws WTException
	 */
	public void checkDocs() throws WTException{
		if(this.self != null && this.pbo != null){
			WTArrayList list = collect();
			
			isObjsCheckedOut(list);
			
			checkISlatestVersion(list);
			
			getErrorMessge();
			if(!checkPboIsDelect(list)){
//				errorMessage.append("\n 随签列表中没有主对象，请点击重新加载随签对象按钮");
				Locale locale = SessionHelper.manager.getLocale();
				errorMessage.append(WTMessage.getLocalizedMessage(RESOURCE, "WF_PBO_NULL", null, locale));
				getErrorMessge();
			}
		}
		
	}

	/**
	 * 搜集对象
	 * @param self
	 * @return
	 * @throws WTException
	 */
	private WTArrayList collect() throws WTException {
		WfProcess wfprocess = null;
		WTArrayList list = new WTArrayList();
		wfprocess = WorkflowUtil.getProcess(this.self);
		ProcessReviewObjectLink link = null;
		if(wfprocess != null){
			QueryResult queryresult = ProcessReviewObjectLinkHelper.service.getProcessReviewObjectLinkByRoleA(wfprocess);
			while (queryresult != null && queryresult.hasMoreElements()) {
				link = (ProcessReviewObjectLink) queryresult.nextElement();
				WTObject obj = (WTObject)link.getRoleBObject();
				list.add(obj);
			}
		}
		return list;
		
	}

	/**
	 * 检查pbo是否被删除
	 * @param list
	 * @return
	 * @throws WTException
	 */
	private boolean checkPboIsDelect( WTArrayList list) throws WTException {
		boolean hasPbo = false;
		if(this.pbo != null){
			for(int i = 0 ; i < list.size(); i++ ){
				WTObject obj = (WTObject) list.getPersistable(i);
				if(PersistenceHelper.isEquivalent(this.pbo , obj)){
					hasPbo = true;
					break;
				}
			}
		}
		return hasPbo;
	}

	/**
	 * 验证生命周期状态
	 * @param list
	 * @return
	 * @throws WTException
	 */
	public void checkReviewObjectStates(WTArrayList list) throws WTException {

		List<WTObject> tempReviewObjsList = null;
		if( list != null && list.size() > 0 ){
			tempReviewObjsList = new ArrayList<WTObject>(list.size());
			for(int i = 0; i < list.size(); i++ ){
				WTObject reviewObj = (WTObject) list.getPersistable(i);
				if(reviewObj != null&& (reviewObj instanceof LifeCycleManaged)){
					LifeCycleManaged lcm = (LifeCycleManaged) reviewObj;
					String state = lcm.getLifeCycleState().toString();
					if(!state.equals("ARCHIVED")){
						tempReviewObjsList.add(reviewObj);
					}
				}
			}
			if(tempReviewObjsList.size() > 0){
				Locale locale = SessionHelper.manager.getLocale();
				errorMessage.append("\n PI-260002: 对象的生命周期状态只能为“已归档”");
				errorMessage.append(getCollectionDisplayInfo(tempReviewObjsList));		
			}
		}

	}

	/**
	 * 获取最新版本
	 * @param reviewObjsList 随签对象集合
	 * @throws WTException 异常
	 */
	public void checkISlatestVersion( WTArrayList reviewObjsList ) throws WTException{
		List<WTObject> tempReviewObjsList = null;
		if( reviewObjsList != null && reviewObjsList.size() > 0 ){
			tempReviewObjsList = new ArrayList<WTObject>(reviewObjsList.size());
			for(int i = 0; i < reviewObjsList.size(); i++ ){

				WTObject reviewObj = (WTObject) reviewObjsList.getPersistable(i);

				if(reviewObj instanceof RevisionControlled){
					RevisionControlled revisionControlled = (RevisionControlled)reviewObj;
					if (!VersionControlHelper.isLatestIteration((revisionControlled))) {
						tempReviewObjsList.add(revisionControlled);
					}
				}
			}
			
			if(tempReviewObjsList.size() > 0){
				Locale locale = SessionHelper.manager.getLocale();
				errorMessage.append(WTMessage.getLocalizedMessage(RESOURCE, "WF_TASK_REVIEWOBJECT_IS_LASTESD_VERSION", null, locale));
				errorMessage.append(getCollectionDisplayInfo(tempReviewObjsList));		
			}
		}
	}

	/**
	 * 在编制节点，路由选择为取消时，对重新工作的对象设置状态
	 * @throws WTException
	 */
	public void setCancelState() throws WTException{
		if(this.pbo != null && this.self != null){
			WTArrayList reviewObjs = ReviewObjectAndWorkFlowLink.getReviewObjectByProcess(this.self);
			for(int i = 0; i < reviewObjs.size(); i ++){
				WTObject obj = (WTObject) reviewObjs.getPersistable(i);
				if(obj instanceof LifeCycleManaged){
					LifeCycleManaged lcm = (LifeCycleManaged)obj;
					String state = ((LifeCycleManaged)obj).getLifeCycleState().toString();
					if(state.equals("ARCHIVED")){
						setState(lcm, "ARCHIVED");
					}
				}
			}
		}
	}

	/**
	 * 检查随签对象是否已经在其他流程中正在运行
	 * @param self 流程
	 * @throws WTException 异常
	 */
	public void checkIsAssociatedRunningProcesses( WTArrayList list) throws WTException{
		List<WTObject> tempReviewObjsList = null;
		for(int i = 0 ; i < list.size(); i++ ){
			tempReviewObjsList = new ArrayList<WTObject>(list.size());
			WTObject obj = (WTObject) list.getPersistable(i);
			if(obj!=null&& (obj instanceof LifeCycleManaged)){
				LifeCycleManaged lcm = (LifeCycleManaged) obj;
				String state = lcm.getLifeCycleState().toString();
				if( state.equals("UNDERREVIEW")){
					tempReviewObjsList.add(obj);
				}
			}
			if(tempReviewObjsList.size()>0){
				Locale locale = SessionHelper.manager.getLocale();
				errorMessage.append(WTMessage.getLocalizedMessage(RESOURCE, "WF_TASK_REVIEWOBJECT_IS_ASSOCIATEDPROCESSES", null,locale));
				errorMessage.append(getCollectionDisplayInfo(tempReviewObjsList));		
			}
		}
	}
	
	public boolean validateSendEmail(){
		boolean flag = false;
		if(pbo != null && self != null){
			if(pbo instanceof WTDocument){
				WTDocument doc = (WTDocument)pbo;
				String docSoft = ReviewObjectOfDocUtil.getSoftType(doc);
				String value = readIBAValue(docSoft);
				LOGGER.debug("doc>>>"+doc.getNumber()+"  docSoft>>>"+docSoft+" readvalue>>>"+value); 
				if(value != null && !value.equals("")){
					 flag = true;
				}
			}
		}
		return flag;  
	}
	
	/**
	 * 获取需要发送邮件的文档类型
	 * @param actionName
	 * @return
	 */
	public static String readIBAValue(String actionName) {
		ReadEmailConfigUtil config = ReadEmailConfigUtil.getInstance();
		Map configs = config.readProperties();
		Object IBAName = configs.get(actionName);
		if (IBAName != null) {
			return IBAName.toString().trim();
		}
		return null;
	}
	
}
