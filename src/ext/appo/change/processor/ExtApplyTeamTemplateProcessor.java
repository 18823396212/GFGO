package ext.appo.change.processor;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.util.FeedbackMessage;
import com.ptc.core.ui.resources.FeedbackType;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import ext.appo.change.util.ECAReviewActivityUtil;
import ext.com.core.CoreUtil;
import ext.generic.reviewprincipal.model.PersonalTeamTemplate;
import ext.generic.reviewprincipal.service.WFTeamTemplateHelper;
import ext.lang.PIStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import wt.fc.PersistenceHelper;
import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTMessage;
import wt.workflow.engine.ProcessData;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;

import java.util.*;

public class ExtApplyTeamTemplateProcessor extends DefaultObjectFormProcessor {

    private static final Logger logger = LogR.getLogger(ExtApplyTeamTemplateProcessor.class.getName());
    private static final String RESOURCE = "ext.generic.reviewprincipal.resource.WFTeamTemplateRB";

    public FormResult doOperation(NmCommandBean commandBean, List<ObjectBean> objectBeans) throws WTException {
        FormResult formResult = new FormResult();
        logger.debug("Enter .applyTeamTemplate.doOperation()");
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        Object actionObj = commandBean.getActionOid().getRefObject();
        Locale locale = commandBean.getLocale();
        logger.debug("actionObj=" + actionObj);
        if (actionObj instanceof WorkItem) {
            WorkItem workItem = (WorkItem) actionObj;
            ProcessData workItemContext = workItem.getContext();
            WfAssignedActivity wfActivity = (WfAssignedActivity) workItem.getSource().getObject();
            String activityName = wfActivity.getName();
            logger.debug("activityName=" + activityName);
            ProcessData processData = wfActivity.getContext();
            String augmentActivities = (String) processData.getValue("augmentActivities");
            logger.debug("process.augmentActivities=" + augmentActivities);
            WTObject pbo = (WTObject) workItem.getPrimaryBusinessObject().getObject();
            logger.debug("pbo=" + pbo);
            WfProcess process = wfActivity.getParentProcess();
            HashMap comboMap = commandBean.getComboBox();
            logger.debug("comboMap=" + comboMap);
            if (comboMap != null && comboMap.containsKey("selectTemplateName")) {
                ArrayList aryList = (ArrayList) comboMap.get("selectTemplateName");
                if (aryList != null && aryList.size() > 0) {
                    String tempOid = (String) aryList.get(0);
                    logger.debug("tempOid=" + tempOid);
                    PersonalTeamTemplate ptemplate = (PersonalTeamTemplate) CoreUtil.getWTObjectByOid(tempOid);
                    String templateAugmentActivities = ptemplate.getCurAugmentActivities();
                    logger.debug("template.AugmentActivities=" + templateAugmentActivities);
                    Hashtable<String, String> table = ECAReviewActivityUtil.getAugumentTable(pbo, null, process);
                    augmentActivities = this.synAttributeValue(augmentActivities, templateAugmentActivities, table);
                    WFTeamTemplateHelper.service.applyTeamTemplate(process, pbo, ptemplate, augmentActivities);
                    if (PIStringUtils.isNotNull(augmentActivities)) {
                        if (workItemContext != null) {
                            workItemContext.setValue("augmentActivities", augmentActivities);
                            workItem.setContext(workItemContext);
                            PersistenceHelper.manager.save(workItem);
                            PersistenceHelper.manager.refresh(workItem);
                        }

                        processData.setValue("augmentActivities", augmentActivities);
                        PersistenceHelper.manager.save(wfActivity);
                        PersistenceHelper.manager.refresh(wfActivity);
                    }

                    logger.debug("保存流程变量");
                    formResult.addFeedbackMessage(new FeedbackMessage(FeedbackType.SUCCESS, (Locale) null, WTMessage.getLocalizedMessage("ext.generic.reviewprincipal.resource.WFTeamTemplateRB", "APPLY_TEMPLATE_MSG", new Object[]{ptemplate.getPTemplateName()}, locale), (ArrayList) null, new String[0]));
                }
            }
        }

        SessionServerHelper.manager.setAccessEnforced(enforce);
        return formResult;
    }

    public String synAttributeValue(String targetStr, String synStr) {
        StringBuilder returnString = new StringBuilder(targetStr);
        if (PIStringUtils.isNull(synStr)) {
            return targetStr;
        } else {
            if (targetStr == null) {
                returnString = new StringBuilder();
            }

            if (synStr.contains(">>AND<<")) {
                String[] synArray = synStr.substring(synStr.indexOf(">>AND<<"), synStr.length()).split(">>AND<<");
                int var7 = synArray.length;

                for (String syn : synArray) {
                    if (!PIStringUtils.isNull(syn) && !returnString.toString().contains(">>AND<<" + syn) && !returnString.toString().contains(syn + ";;;qqq")) {
                        returnString.append(">>AND<<").append(syn);
                    }
                }
            }

            return returnString.toString();
        }
    }

    public String synAttributeValue(String targetStr, String synStr, Hashtable<String, String> table) {
        logger.debug("Enter .synAttributeValue() targetStr=" + targetStr);
        logger.debug("synStr=" + synStr);
        String returnString = targetStr;
        if (PIStringUtils.isNull(synStr)) {
            return targetStr;
        } else {
            if (targetStr == null) {
                returnString = "";
            }

            String augmentRoles = (String) table.get("augmentRoles");
            logger.debug("augmentRoles=" + augmentRoles);
            List<String> allActs = new ArrayList();
            if (augmentRoles != null && augmentRoles.trim().length() > 0) {
                allActs.addAll(Arrays.asList(augmentRoles.split(";;;qqq")));
            }

            List<String> mustActs = new ArrayList();
            List<String> tOptionActs = new ArrayList();
            String[] augmentActs = targetStr.split(">>AND<<");
            int var12 = augmentActs.length;

            for (String actStr : augmentActs) {
                if (actStr.contains(";;;qqq")) {
                    mustActs.addAll(Arrays.asList(actStr.split(";;;qqq")));
                } else {
                    tOptionActs.add(actStr);
                }
            }

            List<String> sOptionActs = new ArrayList();
            String[] synStrAry = synStr.split(">>AND<<");
            int var14 = synStrAry.length;

            for (String sOptionActStr : synStrAry) {
                if (sOptionActStr != null && !sOptionActStr.contains(";;;qqq") && allActs.contains(sOptionActStr) && !tOptionActs.contains(sOptionActStr) && !mustActs.contains(sOptionActStr)) {
                    sOptionActs.add(sOptionActStr);
                }
            }

            logger.debug("sOptionActs=" + sOptionActs);
            String sOptionActStr = StringUtils.join(sOptionActs, ">>AND<<");
            if (returnString != null && returnString.length() > 0) {
                returnString = returnString + ">>AND<<" + sOptionActStr;
            } else {
                returnString = sOptionActStr;
            }

            logger.debug("Exit .synAttributeValue() result=" + returnString);
            return returnString;
        }
    }
}
