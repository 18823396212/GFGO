package ext.appo.part.processor;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lowagie.text.DocumentException;
import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.forms.FormResultAction;
import com.ptc.core.components.util.FeedbackMessage;
import com.ptc.core.ui.resources.FeedbackType;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.appo.part.beans.EffectiveBaselineBean;
import ext.appo.part.util.EffecitveBaselineUtil;
import ext.appo.part.util.MversionControlHelper;
import ext.com.workflow.WorkflowUtil;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.generic.integration.erp.common.CommonUtil;
import ext.generic.integration.erp.service.BOMReleaseNewService;
import ext.generic.integration.erp.service.BOMReleaseService;
import ext.generic.reviewObject.model.ProcessReviewObjectLink;
import ext.generic.reviewObject.model.ProcessReviewObjectLinkHelper;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTKeyedHashMap;
import wt.part.WTPart;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfBlock;
import wt.workflow.engine.WfConnector;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WorkItem;
import wt.workflow.work.WorkflowHelper;

public class EffecitveBaselineProcessor extends DefaultObjectFormProcessor{
	
	@Override
	public FormResult doOperation(NmCommandBean nmCommandBean, List<ObjectBean> objectBeans) throws WTException {
	
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false); // 忽略权限
		FormResult doOperation = super.doOperation(nmCommandBean, objectBeans);
		doOperation.setStatus(FormProcessingStatus.SUCCESS);
		  System.out.println("=======================action=======================");
		  try{
				Object refObject = nmCommandBean.getActionOid().getRefObject();
				if (refObject != null && refObject instanceof WTPart) {
					WTPart currentPart = (WTPart) refObject;
					QueryResult qr = WorkflowHelper.service.getWorkItems(currentPart);
					System.out.println("QueryResult======================"+qr);
					while(qr.hasMoreElements()){
				        WorkItem wi = (WorkItem)qr.nextElement();
				        System.out.println("wi=============================="+wi);
				        ObjectReference self = wi.getSource();
				        System.out.println("self=============================="+self);
				        WfProcess process = getProcess(self);
				        
				        System.out.println("process=============================="+process);
				        
				          	Set<WTPart> set = new HashSet<WTPart>();
				          	WTArrayList collect = collect(self);
							for (int i = 0; i < collect.size(); i++) {
								Persistable persistable = collect.getPersistable(i);
								if (persistable instanceof WTPart) {
									WTPart part = (WTPart) persistable;
										String partNumber = part.getNumber();
						    			Object obj = partNumber.substring(0,1);
						    			if(obj.toString().equals("A") || obj.toString().equals("B")){
						    				set.add(part);
					    				}
									}
							}
							
							List<EffectiveBaselineBean> listBean = 	MversionControlHelper.buildAllEffectiveParts(process, set);
							if(!listBean.isEmpty()){
								EffecitveBaselineUtil.insertEffectiveBaselineBean(listBean);
							}
							
							

							
							String releaseTime = CommonUtil.getCurrentTime() ;
							
							for(WTPart wtpart : set) {
								WTKeyedHashMap hashMap =	BOMReleaseNewService.releaseSingleLevelNew(wtpart, releaseTime, null, CommonPDMUtil.getBatchNumber(null),null);
								if( hashMap.size() > 0 ){
									doOperation.addFeedbackMessage( new  FeedbackMessage(
											FeedbackType.FAILURE, null, "BOM发布异常" , null, new String[] {} ) );
								}else{
									doOperation.addFeedbackMessage( new  FeedbackMessage(
											FeedbackType.SUCCESS, null, "BOM发布成功" , null, new String[] {} ) );
								}
							}
						
							
				        break;
					}
				}
		  } catch (IOException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}finally {
			  SessionServerHelper.manager.setAccessEnforced(flag); // 添加权限
		  }
		  
		doOperation.setNextAction(FormResultAction.REFRESH_CURRENT_PAGE);
		return doOperation;
		}
	
	/**
	 * 搜集随签列表中的对象
	 * 
	 * @param self
	 * @return
	 * @throws WTException
	 */
	public static WTArrayList collect(ObjectReference self) throws WTException {
		WfProcess wfprocess = null;
		WTArrayList list = new WTArrayList();
		wfprocess = WorkflowUtil.getProcess(self);
		ProcessReviewObjectLink link = null;
		if (wfprocess != null) {
			QueryResult queryresult = ProcessReviewObjectLinkHelper.service
					.getProcessReviewObjectLinkByRoleA(wfprocess);
			while (queryresult != null && queryresult.hasMoreElements()) {
				link = (ProcessReviewObjectLink) queryresult.nextElement();
				WTObject obj = (WTObject) link.getRoleBObject();
				list.add(obj);
			}
		}
		return list;

	}
	
	/**
	 * @param self
	 * @return 获取流程对象
	 */
	public static WfProcess getProcess(Object obj) {
		if (obj == null) {
			return null;
		}
		try {
			Persistable persistable = null;
			if (obj instanceof Persistable) {
				persistable = (Persistable) obj;
			} else if (obj instanceof ObjectIdentifier) {
				persistable = PersistenceHelper.manager.refresh((ObjectIdentifier) obj);
			} else if (obj instanceof ObjectReference) {
				persistable = ((ObjectReference) obj).getObject();
			}
			if (persistable == null) {
				return null;
			}
			if (persistable instanceof WorkItem) {
				persistable = ((WorkItem) persistable).getSource().getObject();
			}
			if (persistable instanceof WfActivity) {
				persistable = ((WfActivity) persistable).getParentProcess();
			}
			if (persistable instanceof WfConnector) {
				persistable = ((WfConnector) persistable).getParentProcessRef().getObject();
			}
			if (persistable instanceof WfBlock) {
				persistable = ((WfBlock) persistable).getParentProcess();
			}
			if (persistable instanceof WfProcess) {
				return (WfProcess) persistable;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	

}
