package ext.appo.part.filter;

import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import wt.enterprise.RevisionControlled;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.WTReference;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfState;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.workflow.NmWorkflowHelper;

import ext.com.workflow.WorkflowUtil;
import ext.generic.partpromotion.util.PartPromotionConstant;
import ext.generic.partpromotion.util.PartPromotionXMLConfigUtil;


/**
 * 手动启动流程入口
 */
public class EngineerPartPublishFilter extends DefaultSimpleValidationFilter {
	private static String CLASSNAME = EngineerPartPublishFilter.class.getName();
	private static final Logger LOGGER = LogR.getLogger(CLASSNAME);
	@Override
	public UIValidationStatus preValidateAction(UIValidationKey uivalidationkey, UIValidationCriteria uivalidationcriteria) {
		LOGGER.debug(" ===ManualStartProcessValidator===" );
//		UIValidationStatus uivalidationstatus = UIValidationStatus.DISABLED;
		UIValidationStatus uivalidationstatus = UIValidationStatus.HIDDEN;
		WTReference wtreference = uivalidationcriteria.getContextObject();
		if (wtreference != null) {
			Persistable persistable = wtreference.getObject();
			try {
				if ( persistable != null && persistable instanceof WTPart) {
					WTPart wtpart = (WTPart) persistable;					
					//是否显示手动启动流程入口
					if(!WorkflowUtil.isObjectCheckedOut(wtpart)){
						boolean result = showManualStartProcess(wtpart) ;
						if ( result ) {
							uivalidationstatus =  UIValidationStatus.ENABLED;
						} else {
//							uivalidationstatus = UIValidationStatus.DISABLED;
							uivalidationstatus =  UIValidationStatus.HIDDEN;
						}
					}

				}					
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return uivalidationstatus;
	}

	/**
	 * 判断是否显示入口
	 * @param part pbo
	 * @return boolean
	 * @throws WTException 异常
	 */
	private static boolean showManualStartProcess(WTPart part) throws WTException{

		boolean isManualStart = true;
		String partState = part.getLifeCycleState().toString();

		PartPromotionXMLConfigUtil partPromotionXMLConfigUtil = new PartPromotionXMLConfigUtil();
		partPromotionXMLConfigUtil.partPromotionParser();
		Map partPromotionRuleMap = partPromotionXMLConfigUtil.getPartPromotionRuleMap();
		if(partPromotionRuleMap == null || partPromotionRuleMap.size() == 0){
			LOGGER.debug( " partPromotionRuleMap : = " + partPromotionRuleMap);
			return false;
		}
		
		String partView = part.getView().getName().toString();
		if(!partView.equals("Manufacturing")){
			return false;
		}
		//判断是否为最新版本
		boolean getlatest = (Boolean) partPromotionRuleMap.get(PartPromotionConstant.NODE_NAME_LATESTED);
		LOGGER.debug( " getlatest : = " + getlatest);
		if(getlatest){
			if(!isLatestObject(part)){
				return false;
			}
		}

		//判断是否修改者相同
		boolean isModifier = (Boolean) partPromotionRuleMap.get(PartPromotionConstant.NODE_NAME_ONLYCREATOR);
		LOGGER.debug( " isModifier : = " + isModifier);
		if(isModifier){
			if(!isEqualCreator(part)){
				return false;
			}
		}

		//判断是否满足xml配置的生命周期状态
//		Vector valitatorStates = (Vector) partPromotionRuleMap.get(PartPromotionConstant.NODE_NAME_VALIDATE_PART_STATE);
//		LOGGER.debug( " valitatorStates : = " + valitatorStates);
		if(!partState.equals("ARCHIVED")){
			return false;
		}
		
		if( isRunningWorkflow(part) ){
			return false;
		}
		
		return isManualStart;
	}

	/**
	 * 判断部件是不是最新版本
	 * @param oldObj 当前对象
	 * @return boolean
	 */
	private static boolean isLatestObject(WTObject oldObj)  {
		boolean isLstest = true;   //是最新
		RevisionControlled iterated = null;
		if(oldObj instanceof RevisionControlled) {
			iterated = (RevisionControlled) oldObj;
			if (!iterated.isLatestIteration()) {
				isLstest = false;
			}
		}
		return isLstest;
	}

	/**
	 * 判断是否创建者一致
	 * @param pbo pbo
	 * @return boolean
	 * @throws WTException 异常
	 */
	private static boolean isEqualCreator(WTPart pbo) throws WTException{
		boolean isequalCreator = false;
		String principalName = SessionHelper.getPrincipal().getName();//当前用户
		String thePartprincipalName = pbo.getModifier().getPrincipal().getName();//部件修改者
		if(principalName.equals(thePartprincipalName)){
			isequalCreator = true;
		}
		return isequalCreator;
	}

	/**
	 * 判断pbo是否在其他流程中运行
	 * @param pbo 
	 * @return
	 * @throws WTException
	 */
	private static boolean isRunningWorkflow(WTPart pbo) throws WTException{
		boolean processok = false;
		NmOid nmOid = NmOid.newNmOid(PersistenceHelper.getObjectIdentifier(pbo));
		QueryResult qr = NmWorkflowHelper.service.getRoutingHistoryData(nmOid);
		while(qr.hasMoreElements()){
			WfProcess process = (WfProcess)qr.nextElement();
			String templateName = process.getTemplate().getName();
			//只判断是否存在关联的流程，不再判断流程名称
			if (process.getState().equals(WfState.OPEN_RUNNING) /*&& templateName.contains("GenericPartWF")*/ ) {
				processok = true;
				break;
			}
		}
		return processok;
	}
	
}
