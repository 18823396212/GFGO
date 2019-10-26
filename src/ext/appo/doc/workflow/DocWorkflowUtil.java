package ext.appo.doc.workflow;

import org.apache.log4j.Logger;

import wt.content.ContentItem;
import wt.doc.WTDocument;
import wt.fc.ObjectReference;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.util.WTException;

import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.windchill.enterprise.attachments.validators.AttachmentsValidationHelper;
import com.ptc.windchill.enterprise.part.commands.PartDocServiceCommand;

import ext.com.workflow.WorkflowUtil;
import ext.generic.reviewObject.util.ReviewObjectAndWorkFlowLink;
import ext.lang.PIStringUtils;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PICoreHelper;

public class DocWorkflowUtil {
	
	  private static final String CLASSNAME = DocWorkflowUtil.class.getName();
	  private static final Logger logger = LogR.getLogger(CLASSNAME);
	  
	public void checkReviewObject(ObjectReference self) throws WTException {
		StringBuffer sBuffer = new StringBuffer();
		WTArrayList reviewObjectByProcess = ReviewObjectAndWorkFlowLink.getReviewObjectByProcess(self);
		logger.debug("reviewObjectByProcess======"+reviewObjectByProcess.size());
		for (int i = 0; i < reviewObjectByProcess.size(); i++) {
			WTDocument doc = (WTDocument) reviewObjectByProcess.getPersistable(i);
			logger.debug("doc======"+doc.getName());
			TypeIdentifier typeIdentifier = TypeIdentifierUtility.getTypeIdentifier(doc);
			String typename = typeIdentifier.getTypename();
			if (!typename.contains("com.ptc.ReferenceDocument")) {
				sBuffer.append("随迁对象中的 " + doc.getDisplayIdentifier() + "文档不是参考文档子类型。\n");
			}

		}

		if (sBuffer.length() > 0) {
			throw new WTException(sBuffer.toString());
		}

	}

	public void checkPublishReviewObject(ObjectReference self) throws WTException {
		StringBuffer sBuffer = new StringBuffer();
		WTArrayList reviewObjectByProcess = ReviewObjectAndWorkFlowLink.getReviewObjectByProcess(self);
		logger.debug("reviewObjectByProcess======"+reviewObjectByProcess.size());
		for (int i = 0; i < reviewObjectByProcess.size(); i++) {
			WTDocument doc = (WTDocument) reviewObjectByProcess.getPersistable(i);
			logger.debug("doc======"+doc.getName());
			TypeIdentifier typeIdentifier = TypeIdentifierUtility.getTypeIdentifier(doc);
			String typename = typeIdentifier.getTypename();
			if (!typename.contains("com.ptc.ReferenceDocument")) {
				sBuffer.append("随迁对象中的 " + doc.getDisplayIdentifier() + "文档不是参考文档子类型。\n");
			}
		}

		if (sBuffer.length() > 0) {
			throw new WTException(sBuffer.toString());
		}
	}
	
	/***
	 * 产品命名通知单关联说明部件状态设置为指定状态
	 * 
	 * @param pbo
	 *            流程PBO
	 */
	public void setStateForProductNamingNotic(WTObject pbo, String state){
		if(!(pbo instanceof WTDocument)){
			return ;
		}
		try {
			// 判断文档是否为‘产品命名通知单’
			WTDocument doc = (WTDocument) pbo ;
			if(PICoreHelper.service.isType(doc, "com.plm.productNamingNotic")){
				QueryResult qr = PartDocServiceCommand.getAssociatedDescParts(doc) ;
				while(qr.hasMoreElements()){
					Object object = qr.nextElement() ;
					if(object instanceof ObjectReference){
						object = ((ObjectReference)object).getObject() ;
					}
					if(object instanceof WTPart){
						WorkflowUtil.setLifeCycleState((WTPart)object, state);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/***
	 * 批量检查对象是否符合条件
	 * 
	 * 文档：主内容不能为空  部件：‘规格描述’不能为空
	 * 
	 * @param datasArray
	 * @throws WTException
	 */
	public void checkCompoundConditions(WTCollection datasArray) throws WTException{
		if(datasArray == null || datasArray.size() == 0){
			return ;
		}
		
		StringBuilder sb = new StringBuilder() ;
		for(Object object : datasArray){
			if(object instanceof ObjectReference){
				object = ((ObjectReference)object).getObject() ;
			}
			if(object instanceof WTDocument){
				// 批量获取EPM对象表示法
				ContentItem contentItem = AttachmentsValidationHelper.getPrimaryContentItem((WTDocument)object) ;
				if(contentItem == null){
					if(sb.length() > 0){
						sb.append("\n") ;
					}
					sb.append(((WTDocument)object).getDisplayIdentity()) ;
					sb.append(" 文档主内容不能为空!") ;
				}
			}else if(object instanceof WTPart){
				String ibaValue = PIAttributeHelper.service.getValue((WTPart)object, "ggms") == null ? "" :
					(String)PIAttributeHelper.service.getValue((WTPart)object, "ggms");
				if(PIStringUtils.isNull(ibaValue)){
					if(sb.length() > 0){
						sb.append("\n") ;
					}
					sb.append(((WTPart)object).getDisplayIdentity()) ;
					sb.append(" 规格描述 不能为空!") ;
				}
			}
		}
		
		if(sb.length() > 0){
			throw new WTException(sb.toString()) ;
		}
	}
}
