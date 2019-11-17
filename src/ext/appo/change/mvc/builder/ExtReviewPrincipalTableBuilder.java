package ext.appo.change.mvc.builder;

import com.ptc.jca.mvc.components.JcaComponentParams;
import com.ptc.mvc.components.*;
import com.ptc.mvc.util.ClientMessageSource;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.beans.NmHelperBean;
import ext.appo.change.handler.ExtReviewPrincipalTreeHandler;
import ext.appo.change.util.ECAReviewActivityUtil;
import ext.generic.reviewprincipal.resource.ReviewPrincipalRB;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.util.WTException;
import wt.workflow.engine.ProcessData;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WfAssignmentState;
import wt.workflow.work.WorkItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

@ComponentBuilder({"ext.appo.change.mvc.builder.ExtReviewPrincipalTableBuilder"})
public class ExtReviewPrincipalTableBuilder extends AbstractComponentBuilder {
    private static final Logger logger = LogR.getLogger(ExtReviewPrincipalTableBuilder.class.getName());
    private final ClientMessageSource messageSource = this.getMessageSource(ReviewPrincipalRB.class.getName());

    public ComponentConfig buildComponentConfig(ComponentParams componentparams) throws WTException {
        ComponentConfigFactory componentconfigfactory = this.getComponentConfigFactory();
        TreeConfig treeconfig = componentconfigfactory.newTreeConfig();
        treeconfig.setLabel(this.messageSource.getMessage("SETPRINCIPAL"));
        NmHelperBean nmhelperbean = ((JcaComponentParams) componentparams).getHelperBean();
        NmCommandBean nmcommandbean = nmhelperbean.getNmCommandBean();
        NmOid nmoid = nmcommandbean.getPrimaryOid();
        if (nmoid != null) {
            Persistable persistable = nmoid.getWtRef().getObject();
            if (persistable instanceof WorkItem) {
                WorkItem workitem = (WorkItem) persistable;
                WfAssignmentState status = workitem.getStatus();
                if (!status.equals(WfAssignmentState.COMPLETED)) {
                    treeconfig.setActionModel("generic_partcipants_table_actions");
                    this.initProcessNode(workitem);
                }
            }
        }

        treeconfig.setSelectable(true);
        treeconfig.setShowCount(true);
        treeconfig.setExpansionLevel("full");
        treeconfig.setShowTreeLines(true);
        ColumnConfig columnconfig = componentconfigfactory.newColumnConfig("genericRoleParticipantsName", this.messageSource.getMessage("ROLENAMELABLE"), true);
        columnconfig.setWidth(300);
        columnconfig.setSortable(false);
        treeconfig.addComponent(columnconfig);
        columnconfig = componentconfigfactory.newColumnConfig("isNeed", this.messageSource.getMessage("ISNEED"), false);
        columnconfig.setWidth(100);
        columnconfig.setSortable(false);
        columnconfig.setDataUtilityId("genericRoleParticipantsName");
        treeconfig.addComponent(columnconfig);
        columnconfig = componentconfigfactory.newColumnConfig("workItemInfo", this.messageSource.getMessage("workItemInfoDetail"), true);
        columnconfig.setWidth(100);
        columnconfig.setSortable(false);
        columnconfig.setDataUtilityId("genericRoleParticipantsName");
        treeconfig.addComponent(columnconfig);
        columnconfig = componentconfigfactory.newColumnConfig("processSignInfo", this.messageSource.getMessage("SignInfoDetail"), true);
        columnconfig.setWidth(100);
        columnconfig.setSortable(false);
        columnconfig.setDataUtilityId("genericRoleParticipantsName");
        treeconfig.addComponent(columnconfig);
        columnconfig = componentconfigfactory.newColumnConfig("blankInfo", "", true);
        columnconfig.setWidth(100);
        treeconfig.addComponent(columnconfig);
        return treeconfig;
    }

    public ExtReviewPrincipalTreeHandler buildComponentData(ComponentConfig componentconfig, ComponentParams componentparams) throws WTException {
        return new ExtReviewPrincipalTreeHandler();
    }

    public void initProcessNode(WorkItem workIterm) {
        if (workIterm != null) {
            try {
                Persistable pbo = workIterm.getPrimaryBusinessObject().getObject();
                if (pbo == null) {
                    return;
                }

                WfActivity wfactivity = (WfActivity) workIterm.getSource().getObject();
                WfProcess process = wfactivity.getParentProcess();
                Hashtable<String, String> table = ECAReviewActivityUtil.getAugumentTable((WTObject) pbo, null, process);
                if (table != null && table.containsKey("augmentActivities")) {
                    String augmentRoles = table.get("augmentRoles");
                    List<String> allActs = new ArrayList();
                    if (augmentRoles != null && augmentRoles.trim().length() > 0) {
                        allActs.addAll(Arrays.asList(augmentRoles.split(";;;qqq")));
                    }

                    logger.debug("Builder.allActs=" + allActs);
                    String augmentActivities = table.get("augmentActivities") == null ? "" : table.get("augmentActivities");
                    List<String> mustActs = new ArrayList();
                    if (augmentActivities != null && augmentActivities.trim().length() > 0) {
                        mustActs.addAll(Arrays.asList(augmentActivities.split(";;;qqq")));
                    }

                    logger.debug("Builder.mustActs=" + mustActs);
                    ProcessData processdata = process.getContext();
                    String processAugmentActivities = processdata.getValue("augmentActivities") == null ? "" : (String) processdata.getValue("augmentActivities");
                    logger.debug("Excel.augmentActivities=" + augmentActivities);
                    logger.debug("Process.augmentActivities=" + processAugmentActivities);
                    if (!augmentActivities.equals(processAugmentActivities)) {
                        logger.debug(process.getName() + " 签陪表调整");
                        processdata.setValue("augmentActivities", augmentActivities);
                        PersistenceHelper.manager.save(process);
                    }

                    ProcessData workData = wfactivity.getContext();
                    String beforeAugmentActivities = workData.getValue("augmentActivities") == null ? "" : (String) workData.getValue("augmentActivities");
                    logger.debug("beforeAugmentActivities=" + beforeAugmentActivities);
                    List<String> optionActs = new ArrayList();
                    String[] beforeActAry = beforeAugmentActivities.split(">>AND<<");
                    for (String optionStr : beforeActAry) {
                        if (!optionStr.contains(";;;qqq") && !mustActs.contains(optionStr) && allActs.contains(optionStr)) {
                            optionActs.add(optionStr);
                        }
                    }

                    logger.debug("可选活动中打钩的活动optionActs=" + optionActs);
                    String optionStr = StringUtils.join(optionActs, ">>AND<<");
                    if (augmentActivities.length() > 0) {
                        augmentActivities = augmentActivities + ">>AND<<" + optionStr;
                    } else {
                        augmentActivities = optionStr;
                    }

                    logger.debug("augmentActivities=" + augmentActivities);
                    if (!augmentActivities.equals(beforeAugmentActivities)) {
                        logger.debug("augmentActivities changed");
                        workData.setValue("augmentActivities", augmentActivities);
                        PersistenceHelper.manager.save(wfactivity);
                    }
                }
            } catch (Exception var20) {
                var20.printStackTrace();
            }

        }
    }
}
