package ext.appo.part.processor;

import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.forms.FormResultAction;
import com.ptc.core.components.util.FeedbackMessage;
import com.ptc.core.ui.resources.FeedbackType;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.generic.workflow.WorkFlowBase;
import wt.httpgw.URLFactory;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.util.WTException;
import wt.util.WTMessage;

/**
 * 手动设计发布流程
 */
public class EngineerPartStartProcessor extends DefaultObjectFormProcessor {
	private static String CLASSNAME = EngineerPartStartProcessor.class.getName();
	private static final Logger LOGGER = LogR.getLogger(CLASSNAME);
	private static final String RESOURCE ="ext.generic.partpromotion.resource.PartPromotionResourceRB";

	@Override
	public FormResult doOperation(NmCommandBean nmCommandBean, List<ObjectBean> arg1)
			throws WTException {
		FormResult formresult = new FormResult();		
		formresult.setStatus(FormProcessingStatus.SUCCESS);	
		NmOid nmoid = nmCommandBean.getActionOid();
		WTPart part = (WTPart) nmoid.getRefObject();	
		String workFlowName = "APPO_ReleasedManufacturingPartWF";
		Locale locale = nmCommandBean.getLocale();
		FeedbackMessage feedbackmessage = null;
		try{
			WorkFlowBase workFlowBase = new WorkFlowBase();
			if(workFlowBase.startWorkFlow(part, workFlowName)){
				String msg = WTMessage.getLocalizedMessage(RESOURCE, "START_PARTSIGN_MSG", null, locale);
				feedbackmessage = new FeedbackMessage(FeedbackType.SUCCESS, null,msg, null, new String[] {});
			}
		}
		catch(Exception e){
			e.printStackTrace();
			String error = WTMessage.getLocalizedMessage(RESOURCE, "START_PARTSIGN_ERR", null, locale);
			feedbackmessage = new FeedbackMessage(FeedbackType.FAILURE, null, error + e.toString(), null, new String[] {});
		}finally{		
		}

		if(feedbackmessage == null){
			String error = WTMessage.getLocalizedMessage(RESOURCE, "START_PARTSIGN_ERR", null, locale);
			feedbackmessage = new FeedbackMessage(FeedbackType.FAILURE, null,error, null, new String[] {});
		}
		
		formresult.addFeedbackMessage(feedbackmessage);
		formresult.setNextAction(FormResultAction.REFRESH_CURRENT_PAGE);		
		URLFactory urlFactory = new URLFactory();
		formresult.setForcedUrl(urlFactory .getBaseHREF());
		return formresult;
	}  	  
}
