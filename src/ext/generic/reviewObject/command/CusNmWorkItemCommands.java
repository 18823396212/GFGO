package ext.generic.reviewObject.command;

import java.io.Externalizable;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import ext.appo.util.PartUtil;
import ext.com.iba.IBAUtil;
import ext.generic.reviewObject.util.ReviewObjectUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.log4j.LogR;
import wt.org.TimeZoneHelper;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.preference.PreferenceClient;
import wt.preference.PreferenceHelper;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTMessage;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.vc.Mastered;
import wt.workflow.engine.ProcessData;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfEngineServerHelper;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;

import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.work.NmWorkItemCommands;

import ext.generic.reviewObject.cache.ExcelCacheManager;
import ext.generic.reviewObject.constant.ReviewObjectConstant;
import ext.generic.reviewObject.datautility.DataUtilityHelper;
import ext.generic.reviewObject.model.ProcessReviewObjectLink;
import ext.generic.reviewObject.model.ProcessReviewObjectLinkHelper;
import ext.generic.reviewObject.model.SignedOpinion;
import ext.generic.reviewObject.model.SignedOpinionHelper;
import ext.pi.core.PIAttributeHelper;

import static wt.doc.LoadDoc.getDocument;

/**
 * 页面
 * @author administrator
 * TODO
 */
public class CusNmWorkItemCommands extends NmWorkItemCommands implements Externalizable{
	private static final String RESOURCE ="ext.generic.reviewObject.resource.ReviewObjectResourceRB";
	private static String CLASSNAME = CusNmWorkItemCommands.class.getName();
	private static final Logger LOGGER = LogR.getLogger(CLASSNAME);
	private static String WFTEMPLATE_PATH = "";
	private static WTProperties wtproperties;

	static {
		try {
			wtproperties = WTProperties.getLocalProperties();
			WFTEMPLATE_PATH = wtproperties.getProperty("wt.home", "")
					+ ReviewObjectConstant.WFTEMPLATE_PATH;// Windchill路径
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * 保存按钮
	 * @param nmCommandBean nmCommandBean
	 * @return formResult
	 * @throws WTException 异常
	 * @throws WTPropertyVetoException 
	 */
	public static FormResult save(NmCommandBean nmCommandBean)throws WTException{
		FormResult formResult = NmWorkItemCommands.save(nmCommandBean);
		formResult = completeOrSave(nmCommandBean, formResult);
		return formResult;
	}

	/**
	 * 完成按钮
	 * @param nmCommandBean nmCommandBean
	 * @return formResult
	 * @throws WTException 异常
	 * @throws WTPropertyVetoException 
	 */
	public static FormResult complete(NmCommandBean nmCommandBean)throws WTException{
//		WTPrincipal current = SessionHelper.manager.getPrincipal();
//		SessionHelper.manager.setAdministrator();
		FormResult formResult = new FormResult();
		checkOpinion(nmCommandBean);
		//添加驳回时必须添加备注 begin
		checkUnPassComments(nmCommandBean);
		//添加驳回时必须添加备注 end
		formResult = NmWorkItemCommands.complete(nmCommandBean);
		
		//一人驳回即驳回
		oneReject(nmCommandBean);
		//		Transaction ts = new Transaction();
		//		
		//		try{
		//			ts.start();
		//			
		//			new CheckListComplete(nmCommandBean).saveCheckList();
		//			ts.commit();
		//			ts = null;
		//		}finally{
		//			if(ts!= null){
		//				ts.rollback();
		//			}
		//		}
		formResult = completeOrSave(nmCommandBean, formResult);
//		SessionHelper.manager.setPrincipal(current.getName());
		return formResult;
	}
	
	/**
	 * 检查驳回时，必须添加备注意见
	 * @param cb
	 * @throws WTException 
	 */
	private static void checkUnPassComments(NmCommandBean cb) throws WTException {
		Vector<String> eventList = null;
        String comments = null;
        HttpServletRequest request = cb.getRequest();
        Enumeration parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) { // loop through all the user's form fields
            String plainKey = (String) parameterNames.nextElement();
            String key = NmCommandBean.convert(plainKey);
            LOGGER.trace("=>" + key + ":" + cb.getTextParameter(plainKey));

            if (key.indexOf(ROUTER_EVENT) >= 0 && key.lastIndexOf("old") == -1) {
                String eventValue = null;
                if (key.indexOf(ROUTER_CHECK) >= 0) {
                    eventValue = key.substring(key.indexOf(ROUTER_CHECK) + NmWorkItemCommands.ROUTER_CHECK.length(), key.lastIndexOf("___"));
                } else {
                    eventValue = cb.getTextParameter(plainKey);
                }

                if (eventList == null) {
                    eventList = new Vector<String>();
                }
                eventList.addElement(eventValue);
            } else if (key.indexOf("___" + COMMENTS + "___") >= 0 && !key.endsWith("___old")) {
                comments = cb.getTextParameter(plainKey);
            }
        }

        if (comments == null)
            comments = "";
        comments = comments.trim();

        LOGGER.debug("comments:" + comments);
        LOGGER.debug("eventList:" + eventList);
        //驳回时必须要添加备注
		if (eventList != null && eventList.size() > 0) {
			for (int i = 0; i < eventList.size(); i++) {
				String eventName = (String) eventList.get(i);
				List<String> noEmptyRouts = readWorkflowNoEmptyRoutsFromPreference();
				for(String noEmptyRout:noEmptyRouts){
					if(eventName.startsWith(noEmptyRout)){
						if (StringUtils.isBlank(comments)) {
//							throw new WTException("请输入备注!");
							throw new WTException(WTMessage.getLocalizedMessage(RESOURCE, "CHECK_REMARK_NULL", null, SessionHelper.getLocale()));
						}
					}			
				}
			}
		}
        
	}
	
	/**
	 * 读取首选项中备注信息不能为空的路由
	 * @return
	 */
	private static List<String> readWorkflowNoEmptyRoutsFromPreference(){
		
		List<String> noEmptyRouts = new ArrayList<String>();
        try {
        	//读取首选项
            Object preferenceValue = PreferenceHelper.service.getValue("/pisxcustomization/workflowNoEmptyRouts", PreferenceClient.WINDCHILL_CLIENT_NAME);
           
            if (preferenceValue != null) {
                String value = preferenceValue.toString().trim();
                LOGGER.debug("readWorkflowNoEmptyRoutsFromPreference value=" + value);
                //将中文逗号替换为英文逗号
                value = value.replace("，", ",");
                String [] strNoEmptyRouts = value.split(",");
                for(int i = 0; i < strNoEmptyRouts.length; i ++){
                	String emptyRout = strNoEmptyRouts[i];
                	if(!emptyRout.isEmpty()){
                		noEmptyRouts.add(emptyRout);
                	}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("获取首选项'备注信息不能为空的路由'的值失败!e=" + e.getMessage());
        }
		
		return noEmptyRouts;
	}

	/**
	 * 保存完成按钮执行的方法
	 * @param nmCommandBean nmCommandBean
	 * @param formResult formResult
	 * @return 结果集
	 * @throws WTException 异常
	 * @throws WTPropertyVetoException 
	 */
	private static FormResult completeOrSave(NmCommandBean nmCommandBean, FormResult formResult) throws WTException{
		HashMap hashmap =  nmCommandBean.getText();
		LOGGER.debug("hashmap : = " + hashmap);
		//拼接用户全名
		WTPrincipal Principal = SessionHelper.getPrincipal();
		String PrincipalName = Principal.getName();
		String userFullName = ((WTUser)Principal).getFullName();
		String tempUserName = PrincipalName + "_" + userFullName;
		LOGGER.debug("tempUserName : = " + tempUserName);
		//获取当前时间
		Locale locale = SessionHelper.getLocale();
		TimeZone timezone = TimeZoneHelper.getTimeZone();
		Calendar cal = Calendar.getInstance(timezone, locale);
		Timestamp timestamp = new Timestamp(cal.getTime().getTime());
		LOGGER.debug("timestamp : = " + timestamp);
		Persistable persistable = (Persistable) nmCommandBean.getActionOid().getRefObject();
		if(persistable instanceof WorkItem){
			WorkItem workItem = (WorkItem) persistable;
			WfAssignedActivity wfassignedactivity = (WfAssignedActivity) workItem.getSource().getObject();
			String wfactivityname = wfassignedactivity.getName();
			ReferenceFactory rf = new ReferenceFactory();
			WfProcess wfprocess  = wfassignedactivity.getParentProcess();
			if(wfprocess == null){
				throw new WTException(WTMessage.getLocalizedMessage(RESOURCE, "PROCESS_NULL", null, locale));
			}
			
			//modify by 0524
			if(wfactivityname.equals("CIS库维护")){
				
				HashMap text = nmCommandBean.getText();
				QueryResult queryresult = ProcessReviewObjectLinkHelper.service.getProcessReviewObjectLinkByRoleA(wfprocess);
				if(queryresult != null ){
					while ( queryresult.hasMoreElements()) {
						ProcessReviewObjectLink link = (ProcessReviewObjectLink) queryresult.nextElement();
						WTObject obj = (WTObject)link.getRoleBObject();
						if (obj instanceof WTPart) {
							WTPart part = (WTPart) obj;
							String number = part.getNumber();
							String libraryref = "libraryref" + number;
							String footprintref = "footprintref" + number;
							String footprintref2 = "footprintref2" + number;
							String datasheet = "datasheet" + number;
							Object ibaLibraryref = text.get(libraryref);
							Object ibaFootprintref = text.get(footprintref);
							Object ibaFootprintref2 = text.get(footprintref2);
							Object ibaDatasheet = text.get(datasheet);
							
							// 优选等级
							HashMap<?, ?> comboBox = nmCommandBean.getComboBox() ;
							System.out.println("comboBox : " + comboBox);
							// 控件ID
							String comboBoxID = "yxdj" + number ;
							System.out.println("comboBoxID : " + comboBoxID);
							if(comboBox.containsKey(comboBoxID)){
								Object value = comboBox.get(comboBoxID);
								if(value instanceof List){
									List<?> list = (List<?>)value ;
									if(list.size() > 0){
										PIAttributeHelper.service.forceUpdateSoftAttribute(part, "yxdj", list.get(0));
									}
								}else if (value != null) {
									PIAttributeHelper.service.forceUpdateSoftAttribute(part, "yxdj", value);
								}
							}
							
							PIAttributeHelper.service.forceUpdateSoftAttribute(part, "libraryref", ibaLibraryref);
							PIAttributeHelper.service.forceUpdateSoftAttribute(part, "footprintref", ibaFootprintref);
							PIAttributeHelper.service.forceUpdateSoftAttribute(part, "footprintref2", ibaFootprintref2);
							PIAttributeHelper.service.forceUpdateSoftAttribute(part, "datasheet", ibaDatasheet);
							
						}
					}
				}
			}

			//获取流程的id
			String wfProcessId = rf.getReferenceString(wfprocess);
			LOGGER.debug("wfProcessId : = " + wfProcessId);
			String workFlowNameXML = wfprocess.getTemplate().getName();
			LOGGER.debug("workFlowNameXML : = " + workFlowNameXML);

			//属性变更流程
			if (workFlowNameXML.equals("APPO_PartAttrChangeWF")&& (wfactivityname.equals("编制") || wfactivityname.equals("修改"))) {
				JSONObject changeReason = new JSONObject();
				JSONObject preChangeContent = new JSONObject();
				JSONObject postChangeContent = new JSONObject();
				QueryResult queryresult = ProcessReviewObjectLinkHelper.service.getProcessReviewObjectLinkByRoleA(wfprocess);
				if(queryresult != null ){
					while ( queryresult.hasMoreElements()) {
//						Object getValue=  wfprocess.getContext().getValueObject("primaryBusinessObject");
						HashMap textArea =nmCommandBean.getTextArea();
//						System.out.println("获取属性变更流程text属性=="+text);
						System.out.println("获取属性变更流程textArea属性=="+textArea);
						ProcessReviewObjectLink link = (ProcessReviewObjectLink) queryresult.nextElement();
						WTObject obj = (WTObject)link.getRoleBObject();

						if (obj instanceof WTPart) {
							WTPart part = (WTPart) obj;
							String branchIdentifier= String.valueOf(part.getBranchIdentifier())==null?"":String.valueOf(part.getBranchIdentifier());
							String crStr="VR:wt.part.WTPart:"+branchIdentifier+"_col_ChangeReason";
							String preStr="VR:wt.part.WTPart:"+branchIdentifier+"_col_PreChangeContent";
							String postStr="VR:wt.part.WTPart:"+branchIdentifier+"_col_PostChangeContent";

//							String version = part.getVersionInfo().getIdentifier().getValue()+"."+part.getIterationInfo().getIdentifier().getValue();
                            String changeReasonValue=String.valueOf(textArea.get(crStr))==null?"":String.valueOf(textArea.get(crStr));
							String preChangeContentValue=String.valueOf(textArea.get(preStr))==null?"":String.valueOf(textArea.get(preStr));
							String postChangeContentValue=String.valueOf(textArea.get(postStr))==null?"":String.valueOf(textArea.get(postStr));

							try {
								changeReason.put(branchIdentifier, changeReasonValue);
								preChangeContent.put(branchIdentifier, preChangeContentValue);
								postChangeContent.put(branchIdentifier, postChangeContentValue);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				}


				System.out.println("changeReason=="+changeReason.toString());
				System.out.println("preChangeContent=="+preChangeContent.toString());
				System.out.println("postChangeContent=="+postChangeContent.toString());
				//修改流程变量值
				wfprocess = (WfProcess) PersistenceHelper.manager.refresh(wfprocess);
				ProcessData context = wfprocess.getContext();
				context.setValue("changeReason", changeReason.toString());
				context.setValue("preChangeContent", preChangeContent.toString());
				context.setValue("postChangeContent", postChangeContent.toString());
				wfprocess.setContext(context);
				PersistenceHelper.manager.save(wfprocess);
			}


			if (workFlowNameXML.equals("APPO_ProductPhaseStateWF") && (wfactivityname.equals("编制") || wfactivityname.equals("修改"))) {
				JSONObject cpztValues = new JSONObject();
				HashMap text = nmCommandBean.getComboBox();
				QueryResult queryresult = ProcessReviewObjectLinkHelper.service.getProcessReviewObjectLinkByRoleA(wfprocess);
				if(queryresult != null ){
					while ( queryresult.hasMoreElements()) {
						ProcessReviewObjectLink link = (ProcessReviewObjectLink) queryresult.nextElement();
						WTObject obj = (WTObject)link.getRoleBObject();
						if (obj instanceof WTPart) {
							WTPart part = (WTPart) obj;
							String number = part.getNumber();
							String cpzt = "cpzt" + number;
							ArrayList list = (ArrayList) text.get(cpzt);
							String cpztValue = (String) list.get(0);
							try {
								cpztValues.put(number, cpztValue);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				}
				wfprocess = (WfProcess) PersistenceHelper.manager.refresh(wfprocess);
				ProcessData context = wfprocess.getContext();
				context.setValue("cpztValues", cpztValues.toString());
				wfprocess.setContext(context);
				PersistenceHelper.manager.save(wfprocess);
			}
			
			ProcessReviewObjectLink link = null;
			//查找excel配置的需要活动意见列
			Hashtable<String,String> signedOpinionList = ExcelCacheManager.getWorkFlowExcelFromCache(workFlowNameXML);
			if(signedOpinionList == null || signedOpinionList.size() == 0){
				ExcelCacheManager.setWorkFlowExcelCache(WFTEMPLATE_PATH, workFlowNameXML);
				signedOpinionList = ExcelCacheManager.getWorkFlowExcelFromCache(workFlowNameXML);
				if(signedOpinionList == null || signedOpinionList.size()==0){
					return formResult;
				}
			}
			String tempKey = "";
			//查寻与流程关联的随签对象
			QueryResult queryresult = ProcessReviewObjectLinkHelper.service.getProcessReviewObjectLinkByRoleA(wfprocess);
			String wtObjectoid = "";
			String wtObjectRfoid = "";
			if(queryresult != null ){
				while ( queryresult.hasMoreElements()) {
					link = (ProcessReviewObjectLink) queryresult.nextElement();
					WTObject obj = (WTObject)link.getRoleBObject();
					wtObjectoid = "OR:" + PersistenceHelper.getObjectIdentifier(obj).toString() ;
					wtObjectRfoid = rf.getReferenceString(obj);
					if(signedOpinionList.keySet().contains(wfactivityname)){
						tempKey = wtObjectRfoid + "_col_" + wfactivityname + "__SignedOpinion" + wtObjectoid;
						//获取活动节点签字的意见
						String opinionValue = (String) hashmap.get(tempKey);
						LOGGER.debug("value : = " + opinionValue );
						if(opinionValue != null){
//							QueryResult qr = SignedOpinionHelper.service.queryOpinionValue(wfprocess,obj,wfactivityname,tempUserName);
							QueryResult qr = SignedOpinionHelper.service.queryOpinionValue(wfprocess,obj,wfactivityname);
							if(!compareSignedOpinion(opinionValue,qr)){
								//将数据存放到表中
								if(!opinionValue.trim().equals("") && ! opinionValue.startsWith(tempUserName)){
									String [] opinionValueSplit = opinionValue.split(":");
									if(opinionValueSplit.length > 1){
										opinionValue = opinionValueSplit[opinionValueSplit.length-1].trim();
									}else{
										opinionValue = opinionValueSplit[0].trim();
									}
									opinionValue = tempUserName + ":" +opinionValue;
								}
								try {
									SignedOpinionHelper.service.newSignedOpinionLink(wfactivityname, opinionValue, tempUserName, timestamp, wfprocess, obj);
								} catch (WTPropertyVetoException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
		return formResult;
	}
	
	/**
	 * 比较意见是否在数据库中
	 * @param opinionValue 意见
	 * @param qr 
	 * @return
	 */
	public static boolean compareSignedOpinion(String opinionValue, QueryResult qr)  {
		boolean flag = false;
		if(qr != null && qr.hasMoreElements()){
			SignedOpinion signedOpinion = (SignedOpinion)qr.nextElement();
			String oldOpinion = signedOpinion.getWfactivityopinion();
			if(oldOpinion == null){
				oldOpinion = "";
			}
			String [] oldOpinionSplit = oldOpinion.split(":");
			String [] opinionValueSplit = opinionValue.split(":");

			if(oldOpinionSplit.length > 1){
				oldOpinion = oldOpinionSplit[oldOpinionSplit.length-1].trim();
			}else{
				oldOpinion = oldOpinionSplit[0].trim();
			}

			if(opinionValueSplit.length > 1){
				opinionValue = opinionValueSplit[opinionValueSplit.length -1].trim();
			}else{
				opinionValue = opinionValueSplit[0].trim();
			}

			LOGGER.debug("oldOpinion : = " + oldOpinion + " opinionValue : = " + opinionValue );
			//			if(opinionValue.trim().equals(oldOpinion.trim()) || (tempUserName.trim() + ":" + opinionValue.trim()).equals(oldOpinion.trim() ) || opinionValue.trim().equals(tempUserName.trim() + ":" + oldOpinion.trim())){
			if(oldOpinion.equals(opinionValue)){
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * 检查意见是否填写
	 * @param nmCommandBean
	 * @throws WTException
	 */
	public static void checkOpinion(NmCommandBean nmCommandBean) throws WTException{
		
		HashMap hashmap = nmCommandBean.getText();
		LOGGER.debug("checkOpinion : = " ); 
		Persistable persistable = (Persistable) nmCommandBean.getActionOid().getRefObject();
		if(persistable instanceof WorkItem){
			WorkItem workItem = (WorkItem) persistable;
			WfAssignedActivity wfassignedactivity = (WfAssignedActivity) workItem.getSource().getObject();
			String wfactivityname = wfassignedactivity.getName();
			WfProcess wfprocess = wfassignedactivity.getParentProcess();
			LOGGER.debug("wfactivityname : = "  + wfactivityname); 
			WTPrincipal Principal = SessionHelper.getPrincipal();
			String PrincipalName = Principal.getName();
			String userFullName = ((WTUser)Principal).getFullName();
			String tempUserName = PrincipalName + "_" + userFullName;

			String workFlowNameXML = wfprocess.getTemplate().getName();
			Hashtable<String,String> signedOpinionList = ExcelCacheManager.getWorkFlowExcelFromCache(workFlowNameXML);
			LOGGER.debug("signedOpinionList : = "  + signedOpinionList); 
			if(signedOpinionList != null && signedOpinionList.size() > 0 ){
				if(signedOpinionList.keySet().contains(wfactivityname)){
					String check = signedOpinionList.get(wfactivityname);
					
					if(check == null || check.isEmpty() || !check.equals(ReviewObjectConstant.CHECK)){
						return ;
					}
					LOGGER.debug("check : = "  + check); 
					
					ReferenceFactory rf = new ReferenceFactory();
					
					ProcessReviewObjectLink link = null;
					QueryResult queryresult = ProcessReviewObjectLinkHelper.service.getProcessReviewObjectLinkByRoleA(wfprocess);
					while (queryresult != null && queryresult.hasMoreElements()) {
						link = (ProcessReviewObjectLink) queryresult.nextElement();
						WTObject obj = (WTObject)link.getRoleBObject();
						String userName = link.getStandby1();
						String wtObjectoid = "OR:" + PersistenceHelper.getObjectIdentifier(obj).toString() ;
						String wtObjectRfoid = rf.getReferenceString(obj);
						String tempKey = wtObjectRfoid + "_col_" + wfactivityname + "__SignedOpinion" + wtObjectoid;
						LOGGER.debug("tempKey : = "  + tempKey + " wtObjectoid : = " + wtObjectoid + " wtObjectRfoid : = " + wtObjectRfoid +  " userName : = " + userName);
						DataUtilityHelper dataUtilityHelper = new DataUtilityHelper(nmCommandBean, wfprocess);
						if(!(dataUtilityHelper.isSign(wfassignedactivity.getName(),dataUtilityHelper.isSignHashtable(wfprocess))) ){
							String opinionValue = (String) hashmap.get(tempKey);
							LOGGER.debug("opinionValue : = "  + opinionValue);
							if(opinionValue == null || opinionValue.isEmpty()){
//								throw new wt.util.WTException("有对象没有填写意见，请检查。");
								throw new WTException(WTMessage.getLocalizedMessage(RESOURCE, "SIGN_OPINION_NULL", null, SessionHelper.getLocale()));
							}

						}else{
							
							List<String> oldAssigneeList = dataUtilityHelper.assignmentHistory();
							if( userName!= null && userName.equals(tempUserName) || (oldAssigneeList.contains(userName))){
								String opinionValue = (String) hashmap.get(tempKey);
								LOGGER.debug("opinionValue : = "  + opinionValue);
								if(opinionValue == null || opinionValue.isEmpty()){
//									throw new wt.util.WTException("有对象没有填写意见，请检查。");
									throw new WTException(WTMessage.getLocalizedMessage(RESOURCE, "SIGN_OPINION_NULL", null, SessionHelper.getLocale()));
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * 一人驳回即驳回
	 * @param cb
	 * @throws WTException 
	 */
	private static void oneReject(NmCommandBean cb) throws WTException {
		Vector<String> eventList = null;
        String comments = null;
        HttpServletRequest request = cb.getRequest();
        Enumeration parameterNames = request.getParameterNames();

        Persistable persistable = (Persistable) cb.getActionOid().getRefObject();
		if(persistable instanceof WorkItem){
			WorkItem workItem = (WorkItem) persistable;
			WfAssignedActivity wfActivity = (WfAssignedActivity) workItem.getSource().getObject();
			String wfactivityname = wfActivity.getName();
			
		       while (parameterNames.hasMoreElements()) { // loop through all the user's form fields
		            String plainKey = (String) parameterNames.nextElement();
		            String key = NmCommandBean.convert(plainKey);
		            LOGGER.trace("=>" + key + ":" + cb.getTextParameter(plainKey));

		            if (key.indexOf(ROUTER_EVENT) >= 0 && key.lastIndexOf("old") == -1) {
		                String eventValue = null;
		                if (key.indexOf(ROUTER_CHECK) >= 0) {
		                    eventValue = key.substring(key.indexOf(ROUTER_CHECK) + NmWorkItemCommands.ROUTER_CHECK.length(), key.lastIndexOf("___"));
		                } else {
		                    eventValue = cb.getTextParameter(plainKey);
		                }

		                if (eventList == null) {
		                    eventList = new Vector<String>();
		                }
		                eventList.addElement(eventValue);
		            } else if (key.indexOf("___" + COMMENTS + "___") >= 0 && !key.endsWith("___old")) {
		                comments = cb.getTextParameter(plainKey);
		            }
		        }
		       
		       LOGGER.debug("eventList:" + eventList);
		       
		       if(!wfActivity.isComplete()){
		    	   List<String> oneRejects = readWorkflowOneRejectFromPreference();
		    	   wfActivity = (WfAssignedActivity) PersistenceHelper.manager.refresh(wfActivity);
					if (eventList != null && eventList.size() > 0) {
						for (int i = 0; i < eventList.size(); i++) {
							String eventName = (String) eventList.get(i);						
							for(String oneReject:oneRejects){
								String activityRoutName = wfactivityname + ":" + eventName;
								LOGGER.debug("activityRoutName is:" + activityRoutName);
								if(oneReject.equals(activityRoutName)){
									WfEngineHelper.service.complete(wfActivity, eventList);
								}			
							}
						}
					}
				}
		}
        
	}
	
	/**
	 * 读取首选项中一人驳回即驳回的活动及路由
	 * @return
	 */
	private static List<String> readWorkflowOneRejectFromPreference(){
		
		List<String> oneReject = new ArrayList<String>();
        try {
        	//读取首选项
            Object preferenceValue = PreferenceHelper.service.getValue("/pisxcustomization/workflowOneRejects", PreferenceClient.WINDCHILL_CLIENT_NAME);
           
            if (preferenceValue != null) {
                String value = preferenceValue.toString().trim();
                LOGGER.debug("workflowOneRejects value=" + value);
                //将中文逗号替换为英文逗号
                value = value.replace("，", ",");
                String [] strNoEmptyRouts = value.split(",");
                for(int i = 0; i < strNoEmptyRouts.length; i ++){
                	String emptyRout = strNoEmptyRouts[i];
                	if(!emptyRout.isEmpty()){
                		oneReject.add(emptyRout);
                	}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("获取首选项'备注信息不能为空的路由'的值失败!e=" + e.getMessage());
        }
		
		return oneReject;
	}

}
