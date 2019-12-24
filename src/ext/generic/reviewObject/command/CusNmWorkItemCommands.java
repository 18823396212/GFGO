package ext.generic.reviewObject.command;

import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.work.NmWorkItemCommands;
import com.ptc.windchill.enterprise.workflow.WfDataUtilitiesHelper;
import ext.appo.change.ModifyHelper;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.models.CorrelationObjectLink;
import ext.appo.change.models.TransactionTask;
import ext.appo.change.util.ModifyUtils;
import ext.appo.change.workflow.ECNWorkflowUtil;
import ext.appo.ecn.constants.ChangeConstants;
import ext.generic.reviewObject.cache.ExcelCacheManager;
import ext.generic.reviewObject.constant.ReviewObjectConstant;
import ext.generic.reviewObject.datautility.DataUtilityHelper;
import ext.generic.reviewObject.model.ProcessReviewObjectLink;
import ext.generic.reviewObject.model.ProcessReviewObjectLinkHelper;
import ext.generic.reviewObject.model.SignedOpinion;
import ext.generic.reviewObject.model.SignedOpinionHelper;
import ext.lang.PIStringUtils;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PICoreHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import wt.change2.WTChangeOrder2;
import wt.fc.*;
import wt.log4j.LogR;
import wt.org.TimeZoneHelper;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.preference.PreferenceClient;
import wt.preference.PreferenceHelper;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTMessage;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.workflow.engine.ProcessData;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfVotingEventAudit;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;

import javax.servlet.http.HttpServletRequest;
import java.io.Externalizable;
import java.sql.Timestamp;
import java.util.*;

/**
 * 页面
 * @author administrator
 * TODO
 */
public class CusNmWorkItemCommands extends NmWorkItemCommands implements Externalizable, ModifyConstants, ChangeConstants {
    private static final String RESOURCE = "ext.generic.reviewObject.resource.ReviewObjectResourceRB";
    private static String CLASSNAME = CusNmWorkItemCommands.class.getName();
    private static final Logger LOGGER = LogR.getLogger(CLASSNAME);
    private static String WFTEMPLATE_PATH = "";
    private static WTProperties wtproperties;

    static {
        try {
            wtproperties = WTProperties.getLocalProperties();
            WFTEMPLATE_PATH = wtproperties.getProperty("wt.home", "") + ReviewObjectConstant.WFTEMPLATE_PATH;// Windchill路径
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * 保存按钮
     * @param nmCommandBean nmCommandBean
     * @return formResult
     * @throws WTException             异常
     * @throws WTPropertyVetoException
     */
    public static FormResult save(NmCommandBean nmCommandBean) throws WTException {
        FormResult formResult = NmWorkItemCommands.save(nmCommandBean);
        formResult = completeOrSave(nmCommandBean, formResult);
        /* 保存受影响对象、事务性任务表单属性 */
        saveAttribute(nmCommandBean);
        /* 保存受影响对象、事务性任务表单属性 */
        return formResult;
    }

    /**
     * 完成按钮
     *
     * @param nmCommandBean nmCommandBean
     * @return formResult
     * @throws WTException             异常
     * @throws WTPropertyVetoException
     */
    public static FormResult complete(NmCommandBean nmCommandBean) throws WTException {
        checkOpinion(nmCommandBean);
        //添加驳回时必须添加备注 begin
        checkUnPassComments(nmCommandBean);
        //添加驳回时必须添加备注 end
        /* 检查受影响对象列表「审批意见」「备注（驳回必填）」*/
        checkAttribute(nmCommandBean);
        /* 检查受影响对象列表「审批意见」「备注（驳回必填）」*/
        FormResult formResult = NmWorkItemCommands.complete(nmCommandBean);
        //一人驳回即驳回
        oneReject(nmCommandBean);
        formResult = completeOrSave(nmCommandBean, formResult);
        /* 保存受影响对象、事务性任务表单属性 */
        String taskComments = saveAttribute(nmCommandBean);
        /* 保存受影响对象、事务性任务表单属性 */
        /* 设置备注 */
        setTaskComments(nmCommandBean, taskComments);
        /* 设置备注 */
        return formResult;
    }

    /**
     * 检查受影响对象列表「审批意见」「备注（驳回必填）」
     * @param nmCommandBean
     * @throws WTException
     */
    private static void checkAttribute(NmCommandBean nmCommandBean) throws WTException {
        Object refObject = nmCommandBean.getActionOid().getRefObject();
        if (refObject instanceof WorkItem) {
            WorkItem workItem = (WorkItem) refObject;
            WfAssignedActivity activity = (WfAssignedActivity) workItem.getSource().getObject();
            String activityName = activity.getName();
            String template = activity.getParentProcess().getTemplate().getName();
            LOGGER.info("=====checkAttribute.activityName: " + activityName);
            LOGGER.info("=====checkAttribute.template: " + template);
            if (FLOWNAME_5.equals(template) && (CONSTANTS_5.equals(activityName) || CONSTANTS_6.equals(activityName))) {
                List<String> voteList = ModifyUtils.getVoteList(nmCommandBean);
                LOGGER.info("=====checkAttribute.voteList: " + voteList);

                Map<String, Map<String, String>> map = conversionParameter(nmCommandBean);
                LOGGER.info("=====checkAttribute.map: " + map);
                for (String oid : map.keySet()) {
                    if (oid.contains(TransactionTask.class.getName())) continue;
                    Map<String, String> attributeMap = map.get(oid);
                    LOGGER.info("=====checkAttribute.oid: " + oid + " >>>>>attributeMap: " + attributeMap);
                    String approvalOpinion = attributeMap.get(ATTRIBUTE_9) == null ? "" : attributeMap.get(ATTRIBUTE_9);
                    if (voteList.contains(CONSTANTS_11)) {
                        if (approvalOpinion.contains(CONSTANTS_8)) {
                            throw new WTException("受影响对象列表存在审批意见为「驳回」的数据，不允许通过！");
                        }
                    } else if (voteList.contains(CONSTANTS_8)) {
                        if (approvalOpinion.contains(CONSTANTS_8) && PIStringUtils.isNull(attributeMap.get(ATTRIBUTE_10))) {
                            String number = CONSTANTS_9;
                            if (!CONSTANTS_7.equals(oid)) {
                                Persistable persistable = PICoreHelper.service.getWTObjectByOid(oid);
                                number = ModifyUtils.getNumber(persistable);
                            }
                            throw new WTException("受影响对象: " + number + " 的审批意见为「驳回」时，备注必填！");
                        }
                    }
                }
            }
        }
    }

    /**
     * 保存受影响对象、事务性任务表单属性
     * @param nmCommandBean
     * @throws Exception
     * @return
     */
    private static String saveAttribute(NmCommandBean nmCommandBean) throws WTException {
        StringBuilder taskComments = new StringBuilder();
        try {
            Object refObject = nmCommandBean.getActionOid().getRefObject();
            if (refObject instanceof WorkItem) {
                WorkItem workItem = (WorkItem) refObject;
                WfAssignedActivity activity = (WfAssignedActivity) workItem.getSource().getObject();
                String activityName = activity.getName();
                String template = activity.getParentProcess().getTemplate().getName();
                LOGGER.info("=====saveAttribute.activityName: " + activityName);
                LOGGER.info("=====saveAttribute.template: " + template);
                Object object = workItem.getPrimaryBusinessObject().getObject();
                LOGGER.info("=====saveAttribute.object: " + object);

                if (FLOWNAME_5.equals(template) && (CONSTANTS_5.equals(activityName) || CONSTANTS_6.equals(activityName)) && (object instanceof WTChangeOrder2)) {
                    WTChangeOrder2 changeOrder2 = (WTChangeOrder2) object;
                    String ecnVid = String.valueOf(PICoreHelper.service.getBranchId(changeOrder2));
                    LOGGER.info(">>>>>>>>>>saveAttribute.ecnVid: " + ecnVid);

                    Map<String, Map<String, String>> map = conversionParameter(nmCommandBean);
                    LOGGER.info("=====saveAttribute.map: " + map);
                    for (String oid : map.keySet()) {
                        Map<String, String> attributeMap = map.get(oid);
                        LOGGER.info("=====saveAttribute.oid: " + oid + " >>>>>attributeMap: " + attributeMap);
                        if (oid.equals(CONSTANTS_7)) {
                            String approvalOpinion = attributeMap.get(ATTRIBUTE_9) == null ? "" : attributeMap.get(ATTRIBUTE_9);
                            String remark = attributeMap.get(ATTRIBUTE_10) == null ? "" : attributeMap.get(ATTRIBUTE_10);

                            CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, CONSTANTS_7, CONSTANTS_7);
                            if (null == link) {
                                link = new CorrelationObjectLink();
                                link.setChangeOrder2(changeOrder2);
                                link.setLinkType(CONSTANTS_7);
                                link.setEcnBranchIdentifier(ecnVid);
                                link.setPerBranchIdentifier(CONSTANTS_7);
                                PersistenceServerHelper.manager.insert(link);
                                link = (CorrelationObjectLink) PersistenceHelper.manager.refresh(link);
                            }
                            link.setApprovalOpinion(approvalOpinion.contains(CONSTANTS_8) ? CONSTANTS_8 : approvalOpinion.contains(CONSTANTS_10) ? CONSTANTS_10 : approvalOpinion);
                            link.setRemark(remark);
                            PersistenceServerHelper.manager.update(link);

                            if (approvalOpinion.contains(CONSTANTS_8)) {
                                taskComments.append(CONSTANTS_9).append(SEPARATOR_1).append(CONSTANTS_8).append(SEPARATOR_1).append(remark).append("\n");
                            }
                        } else {
                            Persistable persistable = PICoreHelper.service.getWTObjectByOid(oid);
                            if (persistable == null) continue;
                            if (persistable instanceof TransactionTask) {
                                TransactionTask task = (TransactionTask) persistable;
                                String changeTheme = attributeMap.get(CHANGETHEME_COMPID) == null ? task.getChangeTheme() : attributeMap.get(CHANGETHEME_COMPID);
                                String changeDescribe = attributeMap.get(CHANGEDESCRIBE_COMPID) == null ? task.getChangeDescribe() : attributeMap.get(CHANGEDESCRIBE_COMPID);
                                String responsible = attributeMap.get(RESPONSIBLE_COMPID) == null ? task.getResponsible() : attributeMap.get(RESPONSIBLE_COMPID);
                                String needDate = attributeMap.get(NEEDDATE_COMPID) == null ? task.getNeedDate() : attributeMap.get(NEEDDATE_COMPID);
                                ModifyHelper.service.updateTransactionTask(task, changeTheme, changeDescribe, responsible, needDate);
                            } else {
                                String approvalOpinion = attributeMap.get(ATTRIBUTE_9) == null ? "" : attributeMap.get(ATTRIBUTE_9);
                                String remark = attributeMap.get(ATTRIBUTE_10) == null ? "" : attributeMap.get(ATTRIBUTE_10);

                                String branchId = String.valueOf(PICoreHelper.service.getBranchId(persistable));
                                LOGGER.info(">>>>>>>>>>saveAttribute.branchId: " + branchId);
                                CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, branchId, LINKTYPE_1);
                                link.setApprovalOpinion(approvalOpinion.contains(CONSTANTS_8) ? CONSTANTS_8 : approvalOpinion.contains(CONSTANTS_10) ? CONSTANTS_10 : approvalOpinion);
                                link.setRemark(remark);
                                PersistenceServerHelper.manager.update(link);

                                if (approvalOpinion.contains(CONSTANTS_8)) {
                                    String number = ModifyUtils.getNumber(persistable);
                                    taskComments.append(number).append(SEPARATOR_1).append(CONSTANTS_8).append(SEPARATOR_1).append(remark).append("\n");
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
        LOGGER.info("=====saveAttribute.taskComments: " + taskComments);
        return taskComments.toString();
    }

    /**
     * 设置备注
     * @param nmCommandBean
     * @param taskComments
     * @throws WTException
     */
    private static void setTaskComments(NmCommandBean nmCommandBean, String taskComments) throws WTException {
        try {
            Object refObject = nmCommandBean.getActionOid().getRefObject();
            if (refObject instanceof WorkItem) {
                WorkItem workItem = (WorkItem) refObject;
                workItem = (WorkItem) PersistenceHelper.manager.refresh(workItem);
                WfAssignedActivity activity = (WfAssignedActivity) workItem.getSource().getObject();
                String activityName = activity.getName();
                String template = activity.getParentProcess().getTemplate().getName();
                LOGGER.info("=====setTaskComments.activityName: " + activityName);
                LOGGER.info("=====setTaskComments.template: " + template);
                if (FLOWNAME_5.equals(template) && (CONSTANTS_5.equals(activityName) || CONSTANTS_6.equals(activityName))) {
                    if (PIStringUtils.isNotNull(taskComments)) {
                        ProcessData processData = workItem.getContext();
                        processData.setTaskComments(taskComments);
                        PersistenceHelper.manager.save(workItem);

                        WfVotingEventAudit audit = WfDataUtilitiesHelper.getMatchingEventAudit(workItem);
                        audit.setUserComment(taskComments);
                        PersistenceServerHelper.manager.update(audit);
                    }

                    Object object = workItem.getPrimaryBusinessObject().getObject();
                    LOGGER.info("=====saveAttribute.object: " + object);
                    if (object instanceof WTChangeOrder2) {
                        new ECNWorkflowUtil().createTransactionECA((WTChangeOrder2) object);
                    }
                }
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
    }

    /**
     * 获取流程中需要保存的属性值
     * @param nmCommandBean
     * @return
     */
    private static Map<String, Map<String, String>> conversionParameter(NmCommandBean nmCommandBean) {
        Map<String, Map<String, String>> map = new HashMap<>();
        HashMap text = nmCommandBean.getText();
        LOGGER.info("=====text: " + text);
        for (Object object : text.keySet()) {
            String key = object == null ? "" : object.toString();
            /* 备注（驳回必填）、责任人、任务主题、任务描述、期望完成时间 */
            if (key.contains(ATTRIBUTE_10) || key.contains(RESPONSIBLE_COMPID)
                    || key.contains(CHANGETHEME_COMPID) || key.contains(CHANGEDESCRIBE_COMPID)
                    || key.contains(NEEDDATE_COMPID)) {
                String value = text.get(object) == null ? "" : text.get(object).toString();
                LOGGER.info("=====key: " + key);
                LOGGER.info("=====value: " + value);
                String[] keys = key.split(SEPARATOR_2, -1);
                if (keys.length == 2) {
                    String oid = keys[0];
                    String attribute = keys[1];
                    LOGGER.info("=====oid: " + oid + " >>>>>attribute: " + attribute);
                    if (ATTRIBUTE_10.equals(oid)) oid = CONSTANTS_7;
                    Map<String, String> attributeMap = map.computeIfAbsent(oid, k -> new HashMap<>());
                    attributeMap.put(attribute, value);
                }
            }
        }
        LOGGER.info("=====text: " + text);

        HashMap comboBox = nmCommandBean.getComboBox();
        LOGGER.info("=====comboBox: " + comboBox);
        for (Object object : comboBox.keySet()) {
            String key = object == null ? "" : object.toString();
            if (key.contains(ATTRIBUTE_9)) {
                String value = comboBox.get(object) == null ? "" : comboBox.get(object).toString();
                value = value.replaceAll("^\\[", "").replaceAll("\\]$", "");
                LOGGER.info("=====key: " + key);
                LOGGER.info("=====value: " + value);
                String[] keys = key.split(SEPARATOR_2, -1);
                if (keys.length == 2) {
                    String oid = keys[0];
                    String attribute = keys[1];
                    LOGGER.info("=====oid: " + oid + " >>>>>attribute: " + attribute);
                    if (ATTRIBUTE_9.equals(oid)) oid = CONSTANTS_7;
                    Map<String, String> attributeMap = map.computeIfAbsent(oid, k -> new HashMap<>());
                    attributeMap.put(attribute, value);
                }
            }
        }
        LOGGER.info("=====comboBox: " + comboBox);
        return map;
    }

    /**
     * 检查驳回时，必须添加备注意见
     * @param cb
     * @throws WTException
     */
    private static void checkUnPassComments(NmCommandBean cb) throws WTException {
        Vector<String> eventList = null;
        String comments = null;
        HttpServletRequest request = cb.getRequest();
        Enumeration parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) { // loop through all the user's form fields
            String plainKey = (String) parameterNames.nextElement();
            String key = NmCommandBean.convert(plainKey);
            LOGGER.trace("=>" + key + ":" + cb.getTextParameter(plainKey));

            if (key.contains(ROUTER_EVENT) && key.lastIndexOf("old") == -1) {
                String eventValue;
                if (key.contains(ROUTER_CHECK)) {
                    eventValue = key.substring(key.indexOf(ROUTER_CHECK) + NmWorkItemCommands.ROUTER_CHECK.length(), key.lastIndexOf("___"));
                } else {
                    eventValue = cb.getTextParameter(plainKey);
                }

                if (eventList == null) {
                    eventList = new Vector<String>();
                }
                eventList.addElement(eventValue);
            } else if (key.contains("___" + COMMENTS + "___") && !key.endsWith("___old")) {
                comments = cb.getTextParameter(plainKey);
            }
        }

        if (comments == null) comments = "";
        comments = comments.trim();

        LOGGER.debug("comments:" + comments);
        LOGGER.debug("eventList:" + eventList);
        //驳回时必须要添加备注
        if (eventList != null && eventList.size() > 0) {
            for (int i = 0; i < eventList.size(); i++) {
                String eventName = (String) eventList.get(i);
                List<String> noEmptyRouts = readWorkflowNoEmptyRoutsFromPreference();
                for (String noEmptyRout : noEmptyRouts) {
                    if (eventName.startsWith(noEmptyRout)) {
                        if (StringUtils.isBlank(comments)) {
//							throw new WTException("请输入备注!");
                            throw new WTException(WTMessage.getLocalizedMessage(RESOURCE, "CHECK_REMARK_NULL", null, SessionHelper.getLocale()));
                        }
                    }
                }
            }
        }

    }

    /**
     * 读取首选项中备注信息不能为空的路由
     * @return
     */
    private static List<String> readWorkflowNoEmptyRoutsFromPreference() {
        List<String> noEmptyRouts = new ArrayList<>();
        try {
            //读取首选项
            Object preferenceValue = PreferenceHelper.service.getValue("/pisxcustomization/workflowNoEmptyRouts", PreferenceClient.WINDCHILL_CLIENT_NAME);

            if (preferenceValue != null) {
                String value = preferenceValue.toString().trim();
                LOGGER.debug("readWorkflowNoEmptyRoutsFromPreference value=" + value);
                //将中文逗号替换为英文逗号
                value = value.replace("，", ",");
                String[] strNoEmptyRouts = value.split(",");
                for (int i = 0; i < strNoEmptyRouts.length; i++) {
                    String emptyRout = strNoEmptyRouts[i];
                    if (!emptyRout.isEmpty()) {
                        noEmptyRouts.add(emptyRout);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("获取首选项'备注信息不能为空的路由'的值失败!e=" + e.getMessage());
        }
        return noEmptyRouts;
    }

    /**
     * 保存完成按钮执行的方法
     * @param nmCommandBean nmCommandBean
     * @param formResult    formResult
     * @return 结果集
     * @throws WTException             异常
     * @throws WTPropertyVetoException
     */
    private static FormResult completeOrSave(NmCommandBean nmCommandBean, FormResult formResult) throws WTException {
        HashMap hashmap = nmCommandBean.getText();
        LOGGER.debug("hashmap : = " + hashmap);
        //拼接用户全名
        WTPrincipal Principal = SessionHelper.getPrincipal();
        String PrincipalName = Principal.getName();
        String userFullName = ((WTUser) Principal).getFullName();
        String tempUserName = PrincipalName + "_" + userFullName;
        LOGGER.debug("tempUserName : = " + tempUserName);
        //获取当前时间
        Locale locale = SessionHelper.getLocale();
        TimeZone timezone = TimeZoneHelper.getTimeZone();
        Calendar cal = Calendar.getInstance(timezone, locale);
        Timestamp timestamp = new Timestamp(cal.getTime().getTime());
        LOGGER.debug("timestamp : = " + timestamp);
        Persistable persistable = (Persistable) nmCommandBean.getActionOid().getRefObject();
        if (persistable instanceof WorkItem) {
            WorkItem workItem = (WorkItem) persistable;
            WfAssignedActivity wfassignedactivity = (WfAssignedActivity) workItem.getSource().getObject();
            String wfactivityname = wfassignedactivity.getName();
            ReferenceFactory rf = new ReferenceFactory();
            WfProcess wfprocess = wfassignedactivity.getParentProcess();
            if (wfprocess == null) {
                throw new WTException(WTMessage.getLocalizedMessage(RESOURCE, "PROCESS_NULL", null, locale));
            }

            //modify by 0524
            if (wfactivityname.equals("CIS库维护")) {

                HashMap text = nmCommandBean.getText();
                QueryResult queryresult = ProcessReviewObjectLinkHelper.service.getProcessReviewObjectLinkByRoleA(wfprocess);
                if (queryresult != null) {
                    while (queryresult.hasMoreElements()) {
                        ProcessReviewObjectLink link = (ProcessReviewObjectLink) queryresult.nextElement();
                        WTObject obj = (WTObject) link.getRoleBObject();
                        if (obj instanceof WTPart) {
                            WTPart part = (WTPart) obj;
                            String number = part.getNumber();
                            String libraryref = "libraryref" + number;
                            String footprintref = "footprintref" + number;
                            String footprintref2 = "footprintref2" + number;
                            String datasheet = "datasheet" + number;
                            Object ibaLibraryref = text.get(libraryref);
                            Object ibaFootprintref = text.get(footprintref);
                            Object ibaFootprintref2 = text.get(footprintref2);
                            Object ibaDatasheet = text.get(datasheet);

                            // 优选等级
                            HashMap<?, ?> comboBox = nmCommandBean.getComboBox();
                            System.out.println("comboBox : " + comboBox);
                            // 控件ID
                            String comboBoxID = "yxdj" + number;
                            System.out.println("comboBoxID : " + comboBoxID);
                            if (comboBox.containsKey(comboBoxID)) {
                                Object value = comboBox.get(comboBoxID);
                                if (value instanceof List) {
                                    List<?> list = (List<?>) value;
                                    if (list.size() > 0) {
                                        PIAttributeHelper.service.forceUpdateSoftAttribute(part, "yxdj", list.get(0));
                                    }
                                } else if (value != null) {
                                    PIAttributeHelper.service.forceUpdateSoftAttribute(part, "yxdj", value);
                                }
                            }

                            PIAttributeHelper.service.forceUpdateSoftAttribute(part, "libraryref", ibaLibraryref);
                            PIAttributeHelper.service.forceUpdateSoftAttribute(part, "footprintref", ibaFootprintref);
                            PIAttributeHelper.service.forceUpdateSoftAttribute(part, "footprintref2", ibaFootprintref2);
                            PIAttributeHelper.service.forceUpdateSoftAttribute(part, "datasheet", ibaDatasheet);

                        }
                    }
                }
            }

            //获取流程的id
            String wfProcessId = rf.getReferenceString(wfprocess);
            LOGGER.debug("wfProcessId : = " + wfProcessId);
            String workFlowNameXML = wfprocess.getTemplate().getName();
            LOGGER.debug("workFlowNameXML : = " + workFlowNameXML);

            //属性变更流程
            if (workFlowNameXML.equals("APPO_PartAttrChangeWF") && (wfactivityname.equals("编制") || wfactivityname.equals("修改"))) {
                JSONObject changeReason = new JSONObject();
                JSONObject preChangeContent = new JSONObject();
                JSONObject postChangeContent = new JSONObject();
                QueryResult queryresult = ProcessReviewObjectLinkHelper.service.getProcessReviewObjectLinkByRoleA(wfprocess);
                if (queryresult != null) {
                    while (queryresult.hasMoreElements()) {
                        HashMap textArea = nmCommandBean.getTextArea();
                        System.out.println("获取属性变更流程textArea属性==" + textArea);
                        ProcessReviewObjectLink link = (ProcessReviewObjectLink) queryresult.nextElement();
                        WTObject obj = (WTObject) link.getRoleBObject();

                        if (obj instanceof WTPart) {
                            WTPart part = (WTPart) obj;
                            String branchIdentifier = String.valueOf(part.getBranchIdentifier()) == null ? "" : String.valueOf(part.getBranchIdentifier());
                            String crStr = "VR:wt.part.WTPart:" + branchIdentifier + "_col_ChangeReason";
                            String preStr = "VR:wt.part.WTPart:" + branchIdentifier + "_col_PreChangeContent";
                            String postStr = "VR:wt.part.WTPart:" + branchIdentifier + "_col_PostChangeContent";

                            String changeReasonValue = String.valueOf(textArea.get(crStr)) == null ? "" : String.valueOf(textArea.get(crStr));
                            String preChangeContentValue = String.valueOf(textArea.get(preStr)) == null ? "" : String.valueOf(textArea.get(preStr));
                            String postChangeContentValue = String.valueOf(textArea.get(postStr)) == null ? "" : String.valueOf(textArea.get(postStr));

                            try {
                                changeReason.put(branchIdentifier, changeReasonValue);
                                preChangeContent.put(branchIdentifier, preChangeContentValue);
                                postChangeContent.put(branchIdentifier, postChangeContentValue);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }


                System.out.println("changeReason==" + changeReason.toString());
                System.out.println("preChangeContent==" + preChangeContent.toString());
                System.out.println("postChangeContent==" + postChangeContent.toString());
                //修改流程变量值
                wfprocess = (WfProcess) PersistenceHelper.manager.refresh(wfprocess);
                ProcessData context = wfprocess.getContext();
                context.setValue("changeReason", changeReason.toString());
                context.setValue("preChangeContent", preChangeContent.toString());
                context.setValue("postChangeContent", postChangeContent.toString());
                wfprocess.setContext(context);
                PersistenceHelper.manager.save(wfprocess);
            }


            if (workFlowNameXML.equals("APPO_ProductPhaseStateWF") && (wfactivityname.equals("编制") || wfactivityname.equals("修改"))) {
                JSONObject cpztValues = new JSONObject();
                HashMap text = nmCommandBean.getComboBox();
                QueryResult queryresult = ProcessReviewObjectLinkHelper.service.getProcessReviewObjectLinkByRoleA(wfprocess);
                if (queryresult != null) {
                    while (queryresult.hasMoreElements()) {
                        ProcessReviewObjectLink link = (ProcessReviewObjectLink) queryresult.nextElement();
                        WTObject obj = (WTObject) link.getRoleBObject();
                        if (obj instanceof WTPart) {
                            WTPart part = (WTPart) obj;
                            String number = part.getNumber();
                            String cpzt = "cpzt" + number;
                            ArrayList list = (ArrayList) text.get(cpzt);
                            String cpztValue = (String) list.get(0);
                            try {
                                cpztValues.put(number, cpztValue);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                wfprocess = (WfProcess) PersistenceHelper.manager.refresh(wfprocess);
                ProcessData context = wfprocess.getContext();
                context.setValue("cpztValues", cpztValues.toString());
                wfprocess.setContext(context);
                PersistenceHelper.manager.save(wfprocess);
            }

            ProcessReviewObjectLink link = null;
            //查找excel配置的需要活动意见列
            Hashtable<String, String> signedOpinionList = ExcelCacheManager.getWorkFlowExcelFromCache(workFlowNameXML);
            if (signedOpinionList == null || signedOpinionList.size() == 0) {
                ExcelCacheManager.setWorkFlowExcelCache(WFTEMPLATE_PATH, workFlowNameXML);
                signedOpinionList = ExcelCacheManager.getWorkFlowExcelFromCache(workFlowNameXML);
                if (signedOpinionList == null || signedOpinionList.size() == 0) {
                    return formResult;
                }
            }
            String tempKey = "";
            //查寻与流程关联的随签对象
            QueryResult queryresult = ProcessReviewObjectLinkHelper.service.getProcessReviewObjectLinkByRoleA(wfprocess);
            String wtObjectoid = "";
            String wtObjectRfoid = "";
            if (queryresult != null) {
                while (queryresult.hasMoreElements()) {
                    link = (ProcessReviewObjectLink) queryresult.nextElement();
                    WTObject obj = (WTObject) link.getRoleBObject();
                    wtObjectoid = "OR:" + PersistenceHelper.getObjectIdentifier(obj).toString();
                    wtObjectRfoid = rf.getReferenceString(obj);
                    if (signedOpinionList.keySet().contains(wfactivityname)) {
                        tempKey = wtObjectRfoid + "_col_" + wfactivityname + "__SignedOpinion" + wtObjectoid;
                        //获取活动节点签字的意见
                        String opinionValue = (String) hashmap.get(tempKey);
                        LOGGER.debug("value : = " + opinionValue);
                        if (opinionValue != null) {
                            QueryResult qr = SignedOpinionHelper.service.queryOpinionValue(wfprocess, obj, wfactivityname);
                            if (!compareSignedOpinion(opinionValue, qr)) {
                                //将数据存放到表中
                                if (!opinionValue.trim().equals("") && !opinionValue.startsWith(tempUserName)) {
                                    String[] opinionValueSplit = opinionValue.split(":");
                                    if (opinionValueSplit.length > 1) {
                                        opinionValue = opinionValueSplit[opinionValueSplit.length - 1].trim();
                                    } else {
                                        opinionValue = opinionValueSplit[0].trim();
                                    }
                                    opinionValue = tempUserName + ":" + opinionValue;
                                }
                                try {
                                    SignedOpinionHelper.service.newSignedOpinionLink(wfactivityname, opinionValue, tempUserName, timestamp, wfprocess, obj);
                                } catch (WTPropertyVetoException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
        return formResult;
    }

    /**
     * 比较意见是否在数据库中
     * @param opinionValue 意见
     * @param qr
     * @return
     */
    public static boolean compareSignedOpinion(String opinionValue, QueryResult qr) {
        boolean flag = false;
        if (qr != null && qr.hasMoreElements()) {
            SignedOpinion signedOpinion = (SignedOpinion) qr.nextElement();
            String oldOpinion = signedOpinion.getWfactivityopinion();
            if (oldOpinion == null) {
                oldOpinion = "";
            }
            String[] oldOpinionSplit = oldOpinion.split(":");
            String[] opinionValueSplit = opinionValue.split(":");

            if (oldOpinionSplit.length > 1) {
                oldOpinion = oldOpinionSplit[oldOpinionSplit.length - 1].trim();
            } else {
                oldOpinion = oldOpinionSplit[0].trim();
            }

            if (opinionValueSplit.length > 1) {
                opinionValue = opinionValueSplit[opinionValueSplit.length - 1].trim();
            } else {
                opinionValue = opinionValueSplit[0].trim();
            }

            LOGGER.debug("oldOpinion : = " + oldOpinion + " opinionValue : = " + opinionValue);
            if (oldOpinion.equals(opinionValue)) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 检查意见是否填写
     * @param nmCommandBean
     * @throws WTException
     */
    public static void checkOpinion(NmCommandBean nmCommandBean) throws WTException {
        HashMap hashmap = nmCommandBean.getText();
        LOGGER.debug("checkOpinion : = ");
        Persistable persistable = (Persistable) nmCommandBean.getActionOid().getRefObject();
        if (persistable instanceof WorkItem) {
            WorkItem workItem = (WorkItem) persistable;
            WfAssignedActivity wfassignedactivity = (WfAssignedActivity) workItem.getSource().getObject();
            String wfactivityname = wfassignedactivity.getName();
            WfProcess wfprocess = wfassignedactivity.getParentProcess();
            LOGGER.debug("wfactivityname : = " + wfactivityname);
            WTPrincipal Principal = SessionHelper.getPrincipal();
            String PrincipalName = Principal.getName();
            String userFullName = ((WTUser) Principal).getFullName();
            String tempUserName = PrincipalName + "_" + userFullName;

            String workFlowNameXML = wfprocess.getTemplate().getName();
            Hashtable<String, String> signedOpinionList = ExcelCacheManager.getWorkFlowExcelFromCache(workFlowNameXML);
            LOGGER.debug("signedOpinionList : = " + signedOpinionList);
            if (signedOpinionList != null && signedOpinionList.size() > 0) {
                if (signedOpinionList.keySet().contains(wfactivityname)) {
                    String check = signedOpinionList.get(wfactivityname);

                    if (check == null || check.isEmpty() || !check.equals(ReviewObjectConstant.CHECK)) {
                        return;
                    }
                    LOGGER.debug("check : = " + check);

                    ReferenceFactory rf = new ReferenceFactory();

                    QueryResult queryresult = ProcessReviewObjectLinkHelper.service.getProcessReviewObjectLinkByRoleA(wfprocess);
                    while (queryresult != null && queryresult.hasMoreElements()) {
                        ProcessReviewObjectLink link = (ProcessReviewObjectLink) queryresult.nextElement();
                        WTObject obj = (WTObject) link.getRoleBObject();
                        String userName = link.getStandby1();
                        String wtObjectoid = "OR:" + PersistenceHelper.getObjectIdentifier(obj).toString();
                        String wtObjectRfoid = rf.getReferenceString(obj);
                        String tempKey = wtObjectRfoid + "_col_" + wfactivityname + "__SignedOpinion" + wtObjectoid;
                        LOGGER.debug("tempKey : = " + tempKey + " wtObjectoid : = " + wtObjectoid + " wtObjectRfoid : = " + wtObjectRfoid + " userName : = " + userName);
                        DataUtilityHelper dataUtilityHelper = new DataUtilityHelper(nmCommandBean, wfprocess);
                        if (!(dataUtilityHelper.isSign(wfassignedactivity.getName(), dataUtilityHelper.isSignHashtable(wfprocess)))) {
                            String opinionValue = (String) hashmap.get(tempKey);
                            LOGGER.debug("opinionValue : = " + opinionValue);
                            if (opinionValue == null || opinionValue.isEmpty()) {
                                throw new WTException(WTMessage.getLocalizedMessage(RESOURCE, "SIGN_OPINION_NULL", null, SessionHelper.getLocale()));
                            }

                        } else {

                            List<String> oldAssigneeList = dataUtilityHelper.assignmentHistory();
                            if (userName != null && userName.equals(tempUserName) || (oldAssigneeList.contains(userName))) {
                                String opinionValue = (String) hashmap.get(tempKey);
                                LOGGER.debug("opinionValue : = " + opinionValue);
                                if (opinionValue == null || opinionValue.isEmpty()) {
                                    throw new WTException(WTMessage.getLocalizedMessage(RESOURCE, "SIGN_OPINION_NULL", null, SessionHelper.getLocale()));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 一人驳回即驳回
     * @param cb
     * @throws WTException
     */
    private static void oneReject(NmCommandBean cb) throws WTException {
        Vector<String> eventList = null;
        String comments = null;
        HttpServletRequest request = cb.getRequest();
        Enumeration parameterNames = request.getParameterNames();

        Persistable persistable = (Persistable) cb.getActionOid().getRefObject();
        if (persistable instanceof WorkItem) {
            WorkItem workItem = (WorkItem) persistable;
            WfAssignedActivity wfActivity = (WfAssignedActivity) workItem.getSource().getObject();
            String wfactivityname = wfActivity.getName();

            while (parameterNames.hasMoreElements()) { // loop through all the user's form fields
                String plainKey = (String) parameterNames.nextElement();
                String key = NmCommandBean.convert(plainKey);
                LOGGER.trace("=>" + key + ":" + cb.getTextParameter(plainKey));

                if (key.contains(ROUTER_EVENT) && key.lastIndexOf("old") == -1) {
                    String eventValue;
                    if (key.contains(ROUTER_CHECK)) {
                        eventValue = key.substring(key.indexOf(ROUTER_CHECK) + NmWorkItemCommands.ROUTER_CHECK.length(), key.lastIndexOf("___"));
                    } else {
                        eventValue = cb.getTextParameter(plainKey);
                    }

                    if (eventList == null) {
                        eventList = new Vector<>();
                    }
                    eventList.addElement(eventValue);
                } else if (key.contains("___" + COMMENTS + "___") && !key.endsWith("___old")) {
                    comments = cb.getTextParameter(plainKey);
                }
            }

            LOGGER.debug("eventList:" + eventList);

            if (!wfActivity.isComplete()) {
                List<String> oneRejects = readWorkflowOneRejectFromPreference();
                wfActivity = (WfAssignedActivity) PersistenceHelper.manager.refresh(wfActivity);
                if (eventList != null && eventList.size() > 0) {
                    for (int i = 0; i < eventList.size(); i++) {
                        String eventName = eventList.get(i);
                        for (String oneReject : oneRejects) {
                            String activityRoutName = wfactivityname + ":" + eventName;
                            LOGGER.debug("activityRoutName is:" + activityRoutName);
                            if (oneReject.equals(activityRoutName)) {
                                WfEngineHelper.service.complete(wfActivity, eventList);
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * 读取首选项中一人驳回即驳回的活动及路由
     * @return
     */
    private static List<String> readWorkflowOneRejectFromPreference() {
        List<String> oneReject = new ArrayList<>();
        try {
            //读取首选项
            Object preferenceValue = PreferenceHelper.service.getValue("/pisxcustomization/workflowOneRejects", PreferenceClient.WINDCHILL_CLIENT_NAME);

            if (preferenceValue != null) {
                String value = preferenceValue.toString().trim();
                LOGGER.debug("workflowOneRejects value=" + value);
                //将中文逗号替换为英文逗号
                value = value.replace("，", ",");
                String[] strNoEmptyRouts = value.split(",");
                for (int i = 0; i < strNoEmptyRouts.length; i++) {
                    String emptyRout = strNoEmptyRouts[i];
                    if (!emptyRout.isEmpty()) {
                        oneReject.add(emptyRout);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("获取首选项'备注信息不能为空的路由'的值失败!e=" + e.getMessage());
        }
        return oneReject;
    }

}
