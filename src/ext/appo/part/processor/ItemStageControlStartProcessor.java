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
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.util.WTException;

/**
 * 手动工程发布流程
 */
public class ItemStageControlStartProcessor extends DefaultObjectFormProcessor {
	private static String CLASSNAME = ItemStageControlStartProcessor.class.getName();
	private static final Logger LOGGER = LogR.getLogger(CLASSNAME);
	private static final String RESOURCE = "ext.generic.partpromotion.resource.PartPromotionResourceRB";

	@Override
	public FormResult doOperation(NmCommandBean nmCommandBean, List<ObjectBean> arg1) throws WTException {
		FormResult formresult = new FormResult();
		formresult.setStatus(FormProcessingStatus.SUCCESS);

		NmOid nmoid = nmCommandBean.getActionOid();
		WTPart part = (WTPart) nmoid.getRefObject();

		String workFlowName = "APPO_ProductPhaseStateWF";

		Object valueObject = ext.pi.core.PIAttributeHelper.service.getValue(part.getContainer(), "ssgs");
		String company = valueObject == null ? "" : (String) valueObject;
		if ("APPO".equals(company)) {
			workFlowName = "APPO_ProductPhaseStateWF";
		} else if ("APPO_FM".equals(company) || "FM".equals(company)) {
			workFlowName = "APPO_ProductPhaseStateWF_FM";
		}

		Locale locale = nmCommandBean.getLocale();
		FeedbackMessage feedbackmessage = null;
		try {
			WorkFlowBase workFlowBase = new WorkFlowBase();
			if (workFlowBase.startWorkFlow(part, workFlowName)) {
				String msg = "启动产品阶段状态控制流程成功!";
				feedbackmessage = new FeedbackMessage(FeedbackType.SUCCESS, null, msg, null, new String[] {});
			}
		} catch (Exception e) {
			e.printStackTrace();
			String error = "启动产品阶段状态控制流程失败!";
			feedbackmessage = new FeedbackMessage(FeedbackType.FAILURE, null, error + e.toString(), null,
					new String[] {});
		} finally {
		}

		if (feedbackmessage == null) {
			String error = "启动产品阶段状态控制流程失败!";
			feedbackmessage = new FeedbackMessage(FeedbackType.FAILURE, null, error, null, new String[] {});
		}

		formresult.addFeedbackMessage(feedbackmessage);
		formresult.setNextAction(FormResultAction.REFRESH_CURRENT_PAGE);

		return formresult;
	}
}
