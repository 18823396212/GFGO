package ext.appo.change.mvc.builder;

import com.ptc.jca.mvc.components.JcaComponentParams;
import com.ptc.mvc.components.*;
import com.ptc.mvc.util.ClientMessageSource;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.beans.NmHelperBean;
import ext.appo.change.util.ECAReviewActivityUtil;
import ext.generic.excel.bean.CellBean;
import ext.generic.excel.bean.RowBean;
import ext.generic.workflow.resource.WorkflowStepGuideResource;
import org.apache.log4j.Logger;
import wt.fc.*;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.ownership.Ownership;
import wt.project.Role;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionServerHelper;
import wt.team.Team;
import wt.team.TeamReference;
import wt.util.WTException;
import wt.workflow.definer.WfAssignedActivityTemplate;
import wt.workflow.engine.*;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

@ComponentBuilder({"ext.appo.change.mvc.builder.ExtWorkflowStepGuidePanelBuilder"})
public class ExtWorkflowStepGuidePanelBuilder extends AbstractComponentBuilder {
    private final ClientMessageSource messageSource = this.getMessageSource(WorkflowStepGuideResource.class.getName());
    private static final Logger LOGGER = LogR.getLogger(ExtWorkflowStepGuidePanelBuilder.class.getName());

    public Object buildComponentData(ComponentConfig componentConfig, ComponentParams componentParams) throws Exception {
        NmHelperBean nmhelperbean = ((JcaComponentParams) componentParams).getHelperBean();
        NmCommandBean nmcommandbean = nmhelperbean.getNmCommandBean();
        Object object = nmcommandbean.getActionOid().getRefObject();
        HashMap<String, String> workflowStepMap = null;
        if (object instanceof WorkItem) {
            boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);

            try {
                HashMap<String, WfAssignedActivity> activityMap = new HashMap();
                WorkItem workItem = (WorkItem) object;
                WfActivity wfactivity = (WfActivity) workItem.getSource().getObject();
                WfProcess wfprocess = wfactivity.getParentProcess();
                ArrayList<String> activeInprocess = this.getAllActivitysHashtableInProcess(wfprocess, WfState.OPEN_RUNNING, activityMap);
                LOGGER.debug("...inwork process activity=" + activeInprocess);
                ArrayList<String> activeComplete = this.getAllActivitysHashtableInProcess(wfprocess, null, activityMap);
                activeComplete.removeAll(activeInprocess);
                LOGGER.debug("...completed process activity=" + activeComplete);
                HashMap map = nmcommandbean.getMap();
                String activeStr = (String) (map.get("activeStr") == null ? "" : map.get("activeStr"));
                if (activeStr.trim().length() > 0) {
                    String[] activeArr = activeStr.split(";;;qqq");
                    workflowStepMap = this.collectWorkflowInfo(wfprocess, activeInprocess, activeComplete, activeArr, activityMap);
                }
            } finally {
                SessionServerHelper.manager.setAccessEnforced(enforce);
            }
        }

        return workflowStepMap;
    }

    private void setNmcommandBeanMap(NmCommandBean nmcommandbean, String activeStr) {
        HashMap<String, Object> nmcommandbeanMap = nmcommandbean.getMap();
        if (nmcommandbeanMap == null) {
            nmcommandbeanMap = new HashMap();
            nmcommandbean.setMap(nmcommandbeanMap);
        }

        nmcommandbeanMap.put("activeStr", activeStr);
    }

    public ComponentConfig buildComponentConfig(ComponentParams componentParams) throws WTException {
        ComponentConfigFactory componentconfigfactory = this.getComponentConfigFactory();
        AttributePanelConfig attributepanelconfig = componentconfigfactory.newAttributePanelConfig();
        GroupConfig groupconfig = componentconfigfactory.newGroupConfig("workflowGuide", this.messageSource.getMessage("STEPBUILDERLABLE_STRING"), 1);
        AttributeConfig defalutattributeconfig = componentconfigfactory.newAttributeConfig("defalut", "", 0, 0);
        groupconfig.addComponent(defalutattributeconfig);
        NmHelperBean nmhelperbean = ((JcaComponentParams) componentParams).getHelperBean();
        NmCommandBean nmcommandbean = nmhelperbean.getNmCommandBean();
        Object object = nmcommandbean.getActionOid().getRefObject();
        int cul = 0;
        int activeSum = this.getActiveSum(nmcommandbean, object);

        for (int i = 0; i < activeSum; ++i) {
            String dtid = "step_" + cul;
            AttributeConfig attributeconfig1 = componentconfigfactory.newAttributeConfig(dtid + "_Name", "", 1, cul);
            attributeconfig1.setColSpan(3);
            attributeconfig1.setDataUtilityId("WorkflowStepGuideDatautility");
            groupconfig.addComponent(attributeconfig1);
            AttributeConfig attributeconfig2 = componentconfigfactory.newAttributeConfig(dtid + "_Role", "", 2, cul);
            attributeconfig2.setColSpan(3);
            attributeconfig2.setDataUtilityId("WorkflowStepGuideDatautility");
            groupconfig.addComponent(attributeconfig2);
            ++cul;
        }

        attributepanelconfig.addComponent(groupconfig);
        return attributepanelconfig;
    }

    private int getActiveSum(NmCommandBean nmcommandbean, Object object) throws WTException {
        int activeSum = 0;
        if (object instanceof WorkItem) {
            String activeStr = this.setActiveStr(object);
            this.setNmcommandBeanMap(nmcommandbean, activeStr);
            if (activeStr.trim().length() > 0) {
                String[] activeArr = activeStr.split(";;;qqq");
                activeSum = activeArr.length;
            }
        }
        return activeSum;
    }

    private String setActiveStr(Object object) throws WTException {
        WorkItem workItem = (WorkItem) object;
        Persistable pbo = workItem.getPrimaryBusinessObject().getObject();
        WfActivity wfactivity = (WfActivity) workItem.getSource().getObject();
        WfProcess wfprocess = wfactivity.getParentProcess();
        ObjectReference self = new ObjectReference();
        String sheetName = ECAReviewActivityUtil.getSheetName((WTObject) pbo);
        RowBean rowBean = ECAReviewActivityUtil.getRowBean(self, wfprocess, (WTObject) pbo, sheetName);
        StringBuilder activeStr = new StringBuilder("编制;;;qqq修改");
        if (rowBean != null) {
            List<CellBean> cellBeans = rowBean.getCellList();
            for (CellBean bean : cellBeans) {
                String activityName = bean.getActivityName();
                if (activeStr.length() > 0 && !activeStr.toString().contains(activityName)) {
                    activeStr.append(";;;qqq").append(activityName);
                }
            }
        }

        return activeStr.toString();
    }

    private ArrayList<String> getAllActivitysHashtableInProcess(WfProcess wfprocess, WfState stateWfState, HashMap<String, WfAssignedActivity> activityMap) throws WTException {
        ArrayList<String> activieNameList = new ArrayList();
        Enumeration<?> enSteps = WfEngineHelper.service.getProcessSteps(wfprocess, stateWfState);
        while (enSteps.hasMoreElements()) {
            WfActivity activity = (WfActivity) enSteps.nextElement();
            if (activity instanceof WfAssignedActivity) {
                String activename = activity.getName();
                activieNameList.add(activename);
                activityMap.put(activename, (WfAssignedActivity) activity);
            }
        }
        return activieNameList;
    }

    public Role getRoleByActivity(String activityName, WfProcess process) throws WTException {
        Role role = null;
        if (process != null && activityName != null) {
            Persistable template = process.getTemplate().getObject();
            Enumeration em = this.getRoleByActivity(activityName, template);
            if (em != null && em.hasMoreElements()) {
                role = (Role) em.nextElement();
            }
        }
        return role;
    }

    @SuppressWarnings("deprecation")
    private Enumeration<?> getRoleByActivity(String activityName, Persistable template) throws WTException {
        Enumeration<?> enums = null;
        QuerySpec qs = new QuerySpec(WfAssignedActivityTemplate.class);
        SearchCondition sc = new SearchCondition(WfAssignedActivityTemplate.class, "name", "=", activityName);
        qs.appendSearchCondition(sc);
        qs.appendAnd();
        sc = new SearchCondition(WfAssignedActivityTemplate.class, "parentTemplate.key", "=", template.getPersistInfo().getObjectIdentifier());
        qs.appendSearchCondition(sc);
        QueryResult qr = PersistenceServerHelper.manager.query(qs);
        if (qr != null && qr.hasMoreElements()) {
            WfAssignedActivityTemplate activityTemplate = (WfAssignedActivityTemplate) qr.nextElement();
            enums = activityTemplate.getRoles();
        }
        return enums;
    }

    private HashMap<String, String> collectWorkflowInfo(WfProcess wfprocess, ArrayList<String> activeInprocess, ArrayList<String> activeComplete, String[] activeArr, HashMap<String, WfAssignedActivity> activityMap) throws WTException {
        HashMap<String, String> workflowStepMap = new HashMap();
        for (int i = 0; i < activeArr.length; ++i) {
            String activeName = activeArr[i];
            LOGGER.debug("...activeName=" + activeName);
            if (activeName != null && activeName.trim().length() != 0) {
                this.collectStepInfo(activeInprocess, activeComplete, workflowStepMap, i, activeName);
                Role activeRole = this.getRoleByActivity(activeName, wfprocess);
                LOGGER.debug("...activeRole=" + activeRole);
                TeamReference processTeamReference = wfprocess.getTeamId();
                if (wfprocess.getTeamId() != null) {
                    WfAssignedActivity currentActivity = (WfAssignedActivity) activityMap.get(activeName);
                    Team processTeam = (Team) processTeamReference.getObject();
                    Enumeration<?> participants = processTeam.getPrincipalTarget(activeRole);
                    workflowStepMap = this.collectRoleInfo(workflowStepMap, i, activeRole, participants, currentActivity);
                }
            }
        }
        return workflowStepMap;
    }

    private HashMap<String, String> collectRoleInfo(HashMap<String, String> workflowStepMap, int i, Role activeRole, Enumeration<?> participants, WfAssignedActivity currentActivity) throws WTException {
        StringBuffer roleInfoBuffer = new StringBuffer();
        HashMap<WTPrincipal, String> taskCommentMap = this.getActivityComments(currentActivity);
        if (currentActivity != null && (currentActivity.getName().equals("编制") || currentActivity.getName().equals("修改"))) {
            this.getActivityInfo(roleInfoBuffer, taskCommentMap, currentActivity.getParentProcess().getCreator().getPrincipal());
        } else {
            while (participants != null && participants.hasMoreElements()) {
                roleInfoBuffer.append("\n");
                WTPrincipal principal = ((WTPrincipalReference) participants.nextElement()).getPrincipal();
                LOGGER.debug("...principal=" + principal.getName());
                this.getActivityInfo(roleInfoBuffer, taskCommentMap, principal);
            }
        }
        workflowStepMap.put("step_" + i + "_Role", roleInfoBuffer.toString());
        return workflowStepMap;
    }

    private void getActivityInfo(StringBuffer roleInfoBuffer, HashMap<WTPrincipal, String> taskCommentMap, WTPrincipal principal) {
        if (principal instanceof WTUser) {
            String comments = taskCommentMap.get(principal);
            WTUser user = (WTUser) principal;
            roleInfoBuffer.append("用户：");
            roleInfoBuffer.append(user.getName());
            roleInfoBuffer.append("(");
            roleInfoBuffer.append(user.getFullName());
            roleInfoBuffer.append(")");
            if (comments != null && !comments.trim().equals("")) {
                roleInfoBuffer.append("\n");
                roleInfoBuffer.append("备注：");
                roleInfoBuffer.append(comments);
            }
        }
    }

    private HashMap<WTPrincipal, String> getActivityComments(WfAssignedActivity currentActivity) throws WTException {
        HashMap<WTPrincipal, String> taskCommentMap = new HashMap();
        if (currentActivity != null) {
            QueryResult activityQr = this.getWorkItemsByActivity(currentActivity);
            while (activityQr.hasMoreElements()) {
                WorkItem item = (WorkItem) activityQr.nextElement();
                Ownership ownerShip = item.getOwnership();
                if (ownerShip != null) {
                    WTPrincipal principal = ownerShip.getOwner().getPrincipal();
                    ProcessData itemData = item.getContext();
                    if (itemData != null) {
                        String taskComments = itemData.getTaskComments();
                        if (taskComments != null) {
                            taskCommentMap.put(principal, taskComments);
                        }
                    }
                }
            }
        }
        return taskCommentMap;
    }

    @SuppressWarnings("deprecation")
    private QueryResult getWorkItemsByActivity(WfAssignedActivity currentActivity) throws WTException {
        QuerySpec qs = new QuerySpec(WorkItem.class);
        SearchCondition sc = new SearchCondition(WorkItem.class, "source.key", "=", PersistenceHelper.getObjectIdentifier(currentActivity));
        qs.appendSearchCondition(sc);
        return PersistenceHelper.manager.find(qs);
    }

    private void collectStepInfo(ArrayList<String> activeInprocess, ArrayList<String> activeComplete, HashMap<String, String> workflowStepMap, int i, String activeName) {
        if (activeInprocess.contains(activeName)) {
            workflowStepMap.put("step_" + i + "_Name", activeName + "_Open");
        } else if (activeComplete.contains(activeName)) {
            workflowStepMap.put("step_" + i + "_Name", activeName + "_Close");
        } else {
            workflowStepMap.put("step_" + i + "_Name", activeName + "_Waitting");
        }
    }
}
