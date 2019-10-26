package ext.appo.work.dataUtilities;

import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.util.WTException;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;

import com.infoengine.object.factory.Element;
import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.workflow.NmWorkflowHelper;
import com.ptc.windchill.enterprise.change2.dataUtilities.ChangeLinkAttributeDataUtility;

import ext.lang.PIStringUtils;
import ext.pi.core.PIWorkflowHelper;

/**
 * @author HYJ&NJH 首页增加2列
 */
public class AppoWorkItemDataUtitly extends ChangeLinkAttributeDataUtility {

	@Override
	public Object getDataValue(String paramString, Object paramObject, ModelContext paramModelContext) throws WTException {
		
		if ("IBA|appo_opensWorkItem_Owner".equals(paramString)) {
			return getWorkItemOwner(paramObject) ;
		} else if ("IBA|appo_opensWorkItem_Name".equals(paramString)) {
			return getWorkItemName(paramObject) ;
		}
		
		return "" ;
	}
	
	/***
	 * 获取传入对象当前流程活动节点名称
	 * 
	 * @param paramObject
	 * @return
	 * @throws WTException
	 */
	public String getWorkItemName(Object paramObject) throws WTException{
		if(paramObject == null){
			return "" ;
		}
		
		if(paramObject instanceof Element){
			String ptIdentifier = ((Element) paramObject).getPersistenceIdentifier() ;
			if(PIStringUtils.isNotNull(ptIdentifier)){
				paramObject = (new ReferenceFactory()).getReference(ptIdentifier).getObject() ;
			}
		}
		
		if(paramObject instanceof WorkItem){
			// 获取活动节点对应的流程
			WfProcess process = PIWorkflowHelper.service.getParentProcess((WorkItem)paramObject);
			
			return getWorkItemName(process) ;
		}else if(paramObject instanceof Persistable){
			NmOid nmOid = NmOid.newNmOid(PersistenceHelper.getObjectIdentifier((Persistable)paramObject));
			// 获取对象所有路由处理记录中的流程
			QueryResult qr = NmWorkflowHelper.service.getRoutingHistoryData(nmOid);
			while (qr.hasMoreElements()) {
				Object object = qr.nextElement() ;
				if(object instanceof ObjectReference){
					object = ((ObjectReference)object).getObject() ;
				}
				if(object instanceof WfProcess){
					// 获取流程当前活动节点名称
					String workItemName = getWorkItemName((WfProcess)object) ;
					if(PIStringUtils.isNotNull(workItemName)){
						return workItemName ;
					}
				}
			}
		}
		
		return "" ;
	}
	
	/***
	 * 获取流程当前活动节点名称
	 * 
	 * @param wfProcess
	 * @return
	 * @throws WTException
	 */
	public String getWorkItemName(WfProcess wfProcess) throws WTException{
		StringBuilder sb = new StringBuilder() ;
		if(wfProcess == null){
			return sb.toString() ;
		}
		
		// 获取流程未关闭活动节点
		QueryResult qr = PIWorkflowHelper.service.findWorkItems(wfProcess, false);
		while(qr.hasMoreElements()){
			Object object = qr.nextElement();
			if(object instanceof ObjectReference){
				object = ((ObjectReference)object).getObject() ;
			}
			if(object instanceof WorkItem){
				WfAssignedActivity activity = (WfAssignedActivity) ((WorkItem) object).getSource().getObject();
				// 活动名称
				String name = activity.getName();
				if(sb.length() > 0){
					sb.append(",");
				}
				sb.append(name);
			}
		}
		
		return sb.toString() ;
	}
	
	/***
	 * 获取传入对象当前流程活动节点所有者名称
	 * 
	 * @param paramObject
	 * @return
	 * @throws WTException
	 */
	public String getWorkItemOwner(Object paramObject) throws WTException{
		if(paramObject == null){
			return "" ;
		}
		
		if(paramObject instanceof Element){
			String ptIdentifier = ((Element) paramObject).getPersistenceIdentifier() ;
			if(PIStringUtils.isNotNull(ptIdentifier)){
				paramObject = (new ReferenceFactory()).getReference(ptIdentifier).getObject() ;
			}
		}
		
		if(paramObject instanceof WorkItem){
			// 获取活动节点对应的流程
			WfProcess process = PIWorkflowHelper.service.getParentProcess((WorkItem)paramObject);
			
			return getWorkItemOwner(process) ;
		}else if(paramObject instanceof Persistable){
			NmOid nmOid = NmOid.newNmOid(PersistenceHelper.getObjectIdentifier((Persistable)paramObject));
			// 获取对象所有路由处理记录中的流程
			QueryResult qr = NmWorkflowHelper.service.getRoutingHistoryData(nmOid);
			while (qr.hasMoreElements()) {
				Object object = qr.nextElement() ;
				if(object instanceof ObjectReference){
					object = ((ObjectReference)object).getObject() ;
				}
				if(object instanceof WfProcess){
					// 获取流程当前活动节点所有者名称
					String workItemName = getWorkItemOwner((WfProcess)object) ;
					if(PIStringUtils.isNotNull(workItemName)){
						return workItemName ;
					}
				}
			}
		}
		
		return "" ;
	}
	
	/***
	 * 获取流程当前活动节点的所有者
	 * 
	 * @param wfProcess
	 * @return
	 * @throws WTException
	 */
	public String getWorkItemOwner(WfProcess wfProcess) throws WTException{
		StringBuilder sb = new StringBuilder() ;
		if(wfProcess == null){
			return sb.toString() ;
		}
		
		// 获取流程未关闭活动节点
		QueryResult qr = PIWorkflowHelper.service.findWorkItems(wfProcess, false);
		while(qr.hasMoreElements()){
			Object object = qr.nextElement();
			if(object instanceof ObjectReference){
				object = ((ObjectReference)object).getObject() ;
			}
			if(object instanceof WorkItem){
				WorkItem workItem = (WorkItem) object;
				// 活动所有者全名
				String name = workItem.getOwnership().getOwner().getFullName();
				if(sb.length() > 0){
					sb.append(",");
				}
				sb.append(name);
			}
		}
		
		return sb.toString() ;
	}

}
