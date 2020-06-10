package ext.appo.change.processor;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.util.FeedbackMessage;
import com.ptc.core.ui.resources.FeedbackType;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import ext.appo.change.ModifyHelper;
import ext.appo.change.models.ManageTeamTemplate;
import ext.appo.change.models.ManageTeamTemplateShow;
import ext.com.core.CoreUtil;
import ext.generic.reviewprincipal.model.PersonalTeamTemplate;
import ext.generic.reviewprincipal.service.WFTeamTemplateHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.apache.log4j.Logger;
import wt.log4j.LogR;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTMessage;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;

public class DeleteTeamTemplateProcessor extends DefaultObjectFormProcessor {
    private static final String CLASSNAME = DeleteTeamTemplateProcessor.class.getName();
    private static final Logger logger;
    private static final String RESOURCE = "ext.generic.reviewprincipal.resource.WFTeamTemplateRB";

    static {
        logger = LogR.getLogger(CLASSNAME);
    }

    public DeleteTeamTemplateProcessor() {
    }

    public FormResult doOperation(NmCommandBean commandBean, List<ObjectBean> objectBeans) throws WTException {
        FormResult formResult = new FormResult();
        logger.debug("Enter .deleteTeamTemplate.doOperation()");
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        Object actionObj = commandBean.getActionOid().getRefObject();
        Locale locale = commandBean.getLocale();
        logger.debug("actionObj=" + actionObj);
        if (actionObj instanceof WorkItem) {
            WorkItem workItem = (WorkItem)actionObj;
            WfAssignedActivity wfActivity = (WfAssignedActivity)workItem.getSource().getObject();
            String activityName = wfActivity.getName();
            logger.debug("activityName=" + activityName);
            HashMap comboMap = commandBean.getComboBox();
            logger.debug("comboMap=" + comboMap);
            if (comboMap != null && comboMap.containsKey("selectTemplateName")) {
                ArrayList aryList = (ArrayList)comboMap.get("selectTemplateName");
                if (aryList != null && aryList.size() > 0) {
                    String tempOid = (String)aryList.get(0);
                    logger.debug("tempOid=" + tempOid);
                    PersonalTeamTemplate ptemplate = (PersonalTeamTemplate)CoreUtil.getWTObjectByOid(tempOid);
                    String templateName = ptemplate.getPTemplateName();
                    WFTeamTemplateHelper.service.deleteTeamTemplate(ptemplate);
                    ManageTeamTemplate manageTeamTemplate= ModifyHelper.service.queryManageTeamTemplate(tempOid);
                    ModifyHelper.service.deleteManageTeamTemplate(manageTeamTemplate);
                    List<ManageTeamTemplateShow> manageTeamTemplateShows= ModifyHelper.service.queryManageTeamTemplateShow(tempOid);
                    if (manageTeamTemplateShows!=null&&manageTeamTemplateShows.size()>0){
                        for (int i = 0; i <manageTeamTemplateShows.size() ; i++) {
                            ManageTeamTemplateShow manageTeamTemplateShow=manageTeamTemplateShows.get(i);
                            ModifyHelper.service.deleteManageTeamTemplateShow(manageTeamTemplateShow);
                        }
                    }
                    formResult.addFeedbackMessage(new FeedbackMessage(FeedbackType.SUCCESS, (Locale)null, WTMessage.getLocalizedMessage("ext.generic.reviewprincipal.resource.WFTeamTemplateRB", "DELETE_TEMPLATE_MSG", new Object[]{templateName}, locale), (ArrayList)null, new String[0]));
                }
            }
        }

        SessionServerHelper.manager.setAccessEnforced(enforce);
        return formResult;
    }
}
