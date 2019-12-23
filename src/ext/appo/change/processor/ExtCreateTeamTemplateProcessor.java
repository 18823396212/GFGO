package ext.appo.change.processor;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.util.FeedbackMessage;
import com.ptc.core.ui.resources.FeedbackType;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import ext.appo.change.util.ECAReviewActivityUtil;
import ext.generic.reviewprincipal.service.WFTeamTemplateHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import wt.fc.ObjectReference;
import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTMessage;
import wt.util.WTPropertyVetoException;
import wt.workflow.engine.ProcessData;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ExtCreateTeamTemplateProcessor extends DefaultObjectFormProcessor {
    private static final Logger logger = LogR.getLogger(ExtCreateTeamTemplateProcessor.class.getName());
    private static final String RESOURCE = "ext.generic.reviewprincipal.resource.WFTeamTemplateRB";

    public FormResult doOperation(NmCommandBean commandBean, List<ObjectBean> objectBeans) throws WTException {
        FormResult formResult = new FormResult();
        logger.debug("Enter .createTeamTemplate.doOperation()");
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        Object actionObj = commandBean.getActionOid().getRefObject();
        Locale locale = commandBean.getLocale();
        logger.debug("actionObj=" + actionObj);
        if (actionObj instanceof WorkItem) {
            WorkItem workItem = (WorkItem) actionObj;
            WfAssignedActivity wfActivity = (WfAssignedActivity) workItem.getSource().getObject();
            String activityName = wfActivity.getName();
            logger.debug("activityName=" + activityName);
            WfProcess process = wfActivity.getParentProcess();
            ProcessData processData = wfActivity.getContext();
            String augmentActivities = (String) processData.getValue("augmentActivities");
            logger.debug("augmentActivities=" + augmentActivities);
            if (StringUtils.isBlank(augmentActivities)) {
                throw new WTException(WTMessage.getLocalizedMessage("ext.generic.reviewprincipal.resource.WFTeamTemplateRB", "CREATE_TEMPLATE_ERR1", new Object[0], locale));
            }

            WTObject pbo = (WTObject) processData.getValue("primaryBusinessObject");
            logger.debug("pbo=" + pbo);
            ECAReviewActivityUtil.checkUsersOnlyToCommit(pbo, ObjectReference.newObjectReference(wfActivity), augmentActivities, activityName);
            HashMap textMap = commandBean.getText();
            logger.debug("textMap=" + textMap);
            String templateName = "";
            if (textMap != null && textMap.containsKey("teamTemplateName")) {
                templateName = (String) textMap.get("teamTemplateName");
            }

            logger.debug("templateName=" + templateName);
            if (StringUtils.isBlank(templateName)) {
                throw new WTException(WTMessage.getLocalizedMessage("ext.generic.reviewprincipal.resource.WFTeamTemplateRB", "CREATE_TEMPLATE_ERR2", new Object[0], locale));
            }

            HashMap checkMap = commandBean.getChecked();
            logger.debug("checkMap=" + checkMap);
            boolean isDefault = checkMap != null && checkMap.size() > 0 && checkMap.containsKey("isDefault");
            logger.debug("isDefault=" + isDefault);

            try {
                WFTeamTemplateHelper.service.savePersonalTeamTemplate(process, pbo, templateName, isDefault, augmentActivities);
            } catch (WTPropertyVetoException var19) {
                var19.printStackTrace();
                throw new WTException(var19);
            }

            formResult.addFeedbackMessage(new FeedbackMessage(FeedbackType.SUCCESS, (Locale) null, WTMessage.getLocalizedMessage("ext.generic.reviewprincipal.resource.WFTeamTemplateRB", "CREATE_TEMPLATE_MSG", new Object[]{templateName}, locale), (ArrayList) null, new String[0]));
        }

        SessionServerHelper.manager.setAccessEnforced(enforce);
        return formResult;
    }
}