package ext.appo.change.util;

import com.amazonaws.services.dynamodbv2.xspec.S;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import ext.appo.change.ModifyHelper;
import ext.appo.change.beans.ChangeTaskInfoBean;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.models.CorrelationObjectLink;
import ext.appo.change.models.TransactionTask;
import ext.appo.ecn.constants.ChangeConstants;
import ext.com.workflow.WorkflowUtil;
import ext.lang.PIStringUtils;
import ext.pi.core.PIAttributeHelper;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.ReferenceFactory;
import wt.log4j.LogR;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import java.util.*;

/**
 * 处理事务性任务(ECA)相关的逻辑
 */
public class TransactionECAUtil implements ChangeConstants, ModifyConstants {

    private static final Logger LOGGER = LogR.getLogger(TransactionECAUtil.class.getName());
    private WTChangeOrder2 ORDER2;
    private NmCommandBean NMCOMMANDBEAN;
    public Set<TransactionTask> TASKS = new HashSet<>();//事务性任务模型对象
    private Set<Map<String, String>> TRANSACTIONS = new HashSet<>();//事务性任务表单
    private Set<String> REPETITION = new HashSet<>();//重复的事务性任务名称
    public Set<String> MESSAGES = new HashSet<>();

    public TransactionECAUtil(WTChangeOrder2 changeOrder2, NmCommandBean nmcommandBean) throws WTException {
        NMCOMMANDBEAN = nmcommandBean;
        ORDER2 = changeOrder2;
        //收集事务性任务表单属性
        collectionOne();
    }

    /**
     * 收集事务性任务表单属性
     *
     * @throws WTException
     */
    private void collectionOne() throws WTException {
        if (NMCOMMANDBEAN != null) {
            try {
                Set<String> theme = new HashSet<>();
                Map<String, Object> parameterMap = NMCOMMANDBEAN.getParameterMap();
                LOGGER.info(">>>>>>>>>>collectionOne.parameterMap: " + parameterMap);
                if (parameterMap.containsKey(DATA_ARRAY)) {
                    String[] datasArrayStr = (String[]) parameterMap.get(DATA_ARRAY);
                    if (datasArrayStr != null && datasArrayStr.length > 0) {
                        String datasJSON = datasArrayStr[0];
                        LOGGER.info(">>>>>>>>>>collectionOne.datasJSON: " + datasJSON);
                        if (PIStringUtils.isNotNull(datasJSON)) {
                            JSONObject jsonObject = new JSONObject(datasJSON);
                            Iterator<?> keyIterator = jsonObject.keys();
                            while (keyIterator.hasNext()) {
                                Object key = keyIterator.next();
                                // 数据信息
                                JSONObject dataJSONObject = new JSONObject(jsonObject.getString((String) key));
                                LOGGER.info(">>>>>>>>>>collectionOne.dataJSONObject: " + dataJSONObject);

                                String changeTheme = "";//变更主题
                                if (dataJSONObject.has(CHANGETHEME_COMPID))
                                    changeTheme = dataJSONObject.getString(CHANGETHEME_COMPID);//变更主题
                                String changeDescribe = "";//变更任务描述
                                if (dataJSONObject.has(CHANGEDESCRIBE_COMPID))
                                    changeDescribe = dataJSONObject.getString(CHANGEDESCRIBE_COMPID);//变更任务描述
                                String responsible = "";//责任人
                                if (dataJSONObject.has(RESPONSIBLE_COMPID))
                                    responsible = dataJSONObject.getString(RESPONSIBLE_COMPID);//责任人
                                String needDate = "";//期望完成日期
                                if (dataJSONObject.has(NEEDDATE_COMPID))
                                    needDate = dataJSONObject.getString(NEEDDATE_COMPID);//期望完成日期
                                String changeActivity2 = "";//ECA
                                if (dataJSONObject.has(CHANGEACTIVITY2_COMPID))
                                    changeActivity2 = dataJSONObject.getString(CHANGEACTIVITY2_COMPID);//ECA
                                //add by lzy at 20200417 start
                                String taskType = "";//任务类型
                                if (dataJSONObject.has(TASKTYPE_COMPID))
                                    taskType = dataJSONObject.getString(TASKTYPE_COMPID);//任务类型
                                String clfs = "";//管理方式
                                if (dataJSONObject.has(GLFS_COMPID))
                                    clfs = dataJSONObject.getString(GLFS_COMPID);//管理方式
//                                String taskState = "";//状态
//                                if (dataJSONObject.has(TASKSTATE_COMPID))
//                                    taskState = dataJSONObject.getString(TASKSTATE_COMPID);//状态
                                String taskNumber = "";//任务单号
                                if (dataJSONObject.has(TASKNUMBER_COMPID))
                                    taskNumber = dataJSONObject.getString(TASKNUMBER_COMPID);//任务单号
//                                String actualDate = "";//实际完成时间
//                                if (dataJSONObject.has(ACTUALDATE_COMPID))
//                                    actualDate = dataJSONObject.getString(ACTUALDATE_COMPID);//实际完成时间
                                //add by lzy at 20200417 end
                                LOGGER.info(">>>>>>>>>>changeTheme: " + changeTheme + " changeDescribe: " + changeDescribe + " responsible: " + responsible + " needDate: " + needDate + " changeActivity2: " + changeActivity2);

                                Map<String, String> attributeMap = new HashMap<>();
                                attributeMap.put(CHANGETHEME_COMPID, changeTheme);//变更主题
                                attributeMap.put(CHANGEDESCRIBE_COMPID, changeDescribe);//变更任务描述
                                attributeMap.put(RESPONSIBLE_COMPID, responsible);//责任人
                                attributeMap.put(NEEDDATE_COMPID, needDate);//期望完成日期
                                attributeMap.put(CHANGEACTIVITY2_COMPID, changeActivity2);//ECA
                                //add by lzy at 20200417 start
                                attributeMap.put(TASKTYPE_COMPID, taskType);//任务类型
                                attributeMap.put(GLFS_COMPID, clfs);//管理方式
//                                attributeMap.put(TASKSTATE_COMPID, taskState);//状态
                                attributeMap.put(TASKNUMBER_COMPID, taskNumber);//任务单号
//                                attributeMap.put(ACTUALDATE_COMPID, actualDate);//实际完成时间
                                //add by lzy at 20200417 end
                                TRANSACTIONS.add(attributeMap);

                                if (theme.contains(changeTheme)) REPETITION.add(changeTheme);
                                else theme.add(changeTheme);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new WTException(e.getStackTrace());
            }
        }
        LOGGER.info(">>>>>>>>>>collectionOne.TRANSACTIONS: " + TRANSACTIONS);
    }

    /**
     * 校验 任务主题 是否重复
     *
     * @throws WTException
     */
    public void check() {
        if (REPETITION.size() > 0) {
            MESSAGES.add("事务性任务列表中存在「任务主题」重复的的数据！");
        }
    }

    /***
     * 创建或更新ECA对象信息
     */
    public void createEditChangeActivity2() throws WTException {
        if (ORDER2 != null) {
            try {
                for (Map<String, String> attributeMap : TRANSACTIONS) {
                    String changeTheme = attributeMap.get(CHANGETHEME_COMPID);//变更主题
                    String changeDescribe = attributeMap.get(CHANGEDESCRIBE_COMPID);//变更任务描述
                    String responsible = attributeMap.get(RESPONSIBLE_COMPID);//责任人
                    String needDate = attributeMap.get(NEEDDATE_COMPID);//期望完成日期
                    String changeActivity2 = attributeMap.get(CHANGEACTIVITY2_COMPID);//ECA

                    String taskType = attributeMap.get(TASKTYPE_COMPID);//任务类型
                    String clfs = attributeMap.get(GLFS_COMPID);//管理方式

                    WTChangeActivity2 activity2 = null;
                    if (PIStringUtils.isNotNull(changeActivity2)) {
                        activity2 = (WTChangeActivity2) (new ReferenceFactory()).getReference(changeActivity2).getObject();
                    }

                    if (activity2 == null) {
                        activity2 = ModifyUtils.createChangeTask(ORDER2, changeTheme, null, changeDescribe, TYPE_3, responsible);
                        attributeMap.put(CHANGEACTIVITY2_COMPID, PersistenceHelper.getObjectIdentifier(activity2).toString());
                    } else {
                        // 名称修改
                        if (!activity2.getName().equals(changeTheme)) {
                            ModifyUtils.setChangeActivity2Name(activity2, changeTheme);
                        }
                        // 工作负责人修改
                        ModifyUtils.setChangeActivity2Assignee(activity2, responsible);
                        // 说明修改
                        if (!changeDescribe.equals(activity2.getDescription())) {
                            activity2.setDescription(changeDescribe);
                            activity2 = (WTChangeActivity2) PersistenceHelper.manager.save(activity2);
                        }
                    }
                    // 期望完成日期
                    if (PIStringUtils.isNotNull(needDate)) {
                        ModifyUtils.updateNeedDate(activity2, needDate);
                    }
//                    //任务类型
//                    if (PIStringUtils.isNotNull(taskType)) {
//                        PIAttributeHelper.service.forceUpdateSoftAttribute(activity2, TASKTYPE_COMPID, taskType);
//                    }
//                    //管理方式
//                    if (PIStringUtils.isNotNull(clfs)) {
//                        PIAttributeHelper.service.forceUpdateSoftAttribute(activity2, GLFS_COMPID, clfs);
//                    }
                }
            } catch (Exception e) {
                throw new WTException(e.getStackTrace());
            }
        }
    }

    /**
     * 创建事务性任务-模型对象
     *
     * @return
     * @throws WTException
     */
    public void createTransactionECA() throws WTException {
        if (ORDER2 != null) {
            Set<String> taskIds = new HashSet<>();
            for (Map<String, String> attributeMap : TRANSACTIONS) {
                String changeTheme = attributeMap.get(CHANGETHEME_COMPID);//变更主题
                String changeDescribe = attributeMap.get(CHANGEDESCRIBE_COMPID);//变更任务描述
                String responsible = attributeMap.get(RESPONSIBLE_COMPID);//责任人
                String needDate = attributeMap.get(NEEDDATE_COMPID);//期望完成日期
                String changeActivity2 = attributeMap.get(CHANGEACTIVITY2_COMPID);//ECA

                String taskType = attributeMap.get(TASKTYPE_COMPID);//任务类型
                String clfs = attributeMap.get(GLFS_COMPID);//管理方式
//                String taskState = attributeMap.get(TASKSTATE_COMPID);//状态
//                String taskNumber = attributeMap.get(TASKNUMBER_COMPID);//任务单号
//                String actualDate = attributeMap.get(ACTUALDATE_COMPID);//实际完成时间

                WTChangeActivity2 activity2 = null;
                if (PIStringUtils.isNotNull(changeActivity2)) {
                    activity2 = (WTChangeActivity2) (new ReferenceFactory()).getReference(changeActivity2).getObject();
                }

                //创建模型对象，保存事务性任务属性
                TransactionTask task = ModifyHelper.service.queryTransactionTask(ORDER2, activity2, changeTheme);
                if (task == null) {
//                    task = ModifyHelper.service.newTransactionTask(changeTheme, changeDescribe, responsible, needDate, activity2);
                    task = ModifyHelper.service.newTransactionTask(changeTheme, changeDescribe, responsible, needDate, activity2, taskType, clfs);
                } else {
//                    task = ModifyHelper.service.updateTransactionTask(task, changeTheme, changeDescribe, responsible, needDate);
                    task = ModifyHelper.service.updateTransactionTask(task, changeTheme, changeDescribe, responsible, needDate, taskType, clfs);
                }
                TASKS.add(task);
                taskIds.add(String.valueOf(task.getPersistInfo().getObjectIdentifier().getId()));
            }
            LOGGER.info(">>>>>>>>>>createTransactionECA.TASKS: " + TASKS);
            LOGGER.info(">>>>>>>>>>createTransactionECA.taskIds: " + taskIds);

            Set<CorrelationObjectLink> links = ModifyHelper.service.queryCorrelationObjectLinks(ORDER2, LINKTYPE_3);
            for (CorrelationObjectLink link : links) {
                Persistable persistable = link.getPersistable();
                if (persistable instanceof TransactionTask) {
                    TransactionTask task = (TransactionTask) persistable;
                    String taskId = String.valueOf(task.getPersistInfo().getObjectIdentifier().getId());
                    if (!taskIds.contains(taskId)) {
                        LOGGER.info(">>>>>>>>>>createTransactionECA.taskId: " + taskId);
                        PersistenceServerHelper.manager.remove(link);
                        PersistenceServerHelper.manager.remove(task);
                    }
                }
            }
        }
    }

    /**
     * 启动事务性任务
     *
     * @return
     * @throws WTException
     */
    public static void startupTask(WTChangeOrder2 ecn, List<ChangeTaskInfoBean> ChangeTaskInfoBeans) throws WTException, WTPropertyVetoException {
        if (ecn == null || ChangeTaskInfoBeans == null) return;
//        Set<String> changeThemes = new HashSet<>();
//        Set<Persistable> links = ModifyHelper.service.queryPersistable(ecn, LINKTYPE_3);
//        for (Persistable persistable : links) {
//            if (persistable instanceof TransactionTask) {
//                TransactionTask task = (TransactionTask) persistable;
//                String changeTheme = task.getChangeTheme();// 变更主题
//                changeThemes.add(changeTheme);
//            }
//        }

        Set<TransactionTask> tasks = new HashSet<>();
        for (int i = 0; i < ChangeTaskInfoBeans.size(); i++) {
            ChangeTaskInfoBean changeTaskInfoBean = ChangeTaskInfoBeans.get(i);
            WTChangeActivity2 activity2 = ModifyUtils.createChangeTask(ecn, changeTaskInfoBean.getTaskTheme(), null, changeTaskInfoBean.getChangeDescribe(),
                    TYPE_3, changeTaskInfoBean.getResponsible());
            WorkflowUtil.setLifeCycleState(activity2, "IMPLEMENTATION");//设置为实施状态

            // 期望完成日期
            if (PIStringUtils.isNotNull(changeTaskInfoBean.getNeedDate())) {
                ModifyUtils.updateNeedDate(activity2, changeTaskInfoBean.getNeedDate());
            }
            if (changeTaskInfoBean.getTaskOid() != null && !changeTaskInfoBean.getTaskOid().isEmpty()) {
                String taskOid = changeTaskInfoBean.getTaskOid();
                Set<Persistable> persistables = ModifyHelper.service.queryPersistable(ecn, LINKTYPE_3);
                for (Persistable persistable : persistables) {
                    if (persistable instanceof TransactionTask) {
                        TransactionTask task = (TransactionTask) persistable;
                        if (taskOid.equals(String.valueOf(task.getPersistInfo().getObjectIdentifier().getId()))) {
                            //更新模型对象，保存事务性任务属性
                            task = ModifyHelper.service.updateTransactionTask(task, changeTaskInfoBean.getTaskTheme(), changeTaskInfoBean.getChangeDescribe(),
                                    changeTaskInfoBean.getResponsible(), changeTaskInfoBean.getNeedDate(), changeTaskInfoBean.getTaskType(), changeTaskInfoBean.getGlfs());
                            ModifyHelper.service.updateTransactionTask(task,
                                    PersistenceHelper.getObjectIdentifier(activity2).toString());
                            tasks.add(task);
                            break;
                        }
                    }
                }
            } else {
                //创建模型对象，保存事务性任务属性
                TransactionTask task = ModifyHelper.service.newTransactionTask(changeTaskInfoBean.getTaskTheme(), changeTaskInfoBean.getChangeDescribe(),
                        changeTaskInfoBean.getResponsible(), changeTaskInfoBean.getNeedDate(), activity2, changeTaskInfoBean.getTaskType(), changeTaskInfoBean.getGlfs());
                tasks.add(task);
            }

        }
        //根据上一步骤收集的模型对象，与ECN建立关联关系
        linkTransactionECA(ecn, tasks);
    }

    /**
     * 新增ChangeOrder2与事务性任务的关系(新增创建事务性任务路由为已创建)
     *
     * @param changeOrder2
     * @param tasks
     * @throws Exception
     */
    public static void linkTransactionECA(WTChangeOrder2 changeOrder2, Set<TransactionTask> tasks) throws WTException, WTPropertyVetoException {
        if (changeOrder2 == null || tasks.size() < 1) return;

        String ecnVid = String.valueOf(changeOrder2.getBranchIdentifier());
        LOGGER.info(">>>>>>>>>>linkTransactionECA.ecnVid: " + ecnVid);
        for (TransactionTask task : tasks) {
            String taskOid = String.valueOf(task.getPersistInfo().getObjectIdentifier().getId());
            LOGGER.info(">>>>>>>>>>linkTransactionECA.taskOid: " + taskOid);
            CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, taskOid, LINKTYPE_3);
            LOGGER.info(">>>>>>>>>>linkTransactionECA.link: " + link);
            if (link == null) {
                CorrelationObjectLink link1 = ModifyHelper.service.newCorrelationObjectLink(changeOrder2, task, LINKTYPE_3, ecnVid, taskOid);
                link1.setRouting(ROUTING_1);
            } else {
                ModifyHelper.service.updateCorrelationObjectLink(ecnVid, taskOid, LINKTYPE_3);
            }
        }
    }

    /**
     * 暂存事务性任务
     *
     * @return
     * @throws WTException
     */
    public static void saveTask(WTChangeOrder2 ecn, List<ChangeTaskInfoBean> ChangeTaskInfoBeans) throws WTException, WTPropertyVetoException {
        if (ecn == null || ChangeTaskInfoBeans == null) return;

        Set<TransactionTask> tasks = new HashSet<>();
        for (int i = 0; i < ChangeTaskInfoBeans.size(); i++) {
            ChangeTaskInfoBean changeTaskInfoBean = ChangeTaskInfoBeans.get(i);
            if (changeTaskInfoBean.getTaskOid() != null && !changeTaskInfoBean.getTaskOid().isEmpty()) {
                String taskOid = changeTaskInfoBean.getTaskOid();
                Set<Persistable> persistables = ModifyHelper.service.queryPersistable(ecn, LINKTYPE_3);
                for (Persistable persistable : persistables) {
                    if (persistable instanceof TransactionTask) {
                        TransactionTask task = (TransactionTask) persistable;
                        if (taskOid.equals(String.valueOf(task.getPersistInfo().getObjectIdentifier().getId()))) {
                            //更新模型对象，保存事务性任务属性
                            task = ModifyHelper.service.updateTransactionTask(task, changeTaskInfoBean.getTaskTheme(), changeTaskInfoBean.getChangeDescribe(),
                                    changeTaskInfoBean.getResponsible(), changeTaskInfoBean.getNeedDate(), changeTaskInfoBean.getTaskType(), changeTaskInfoBean.getGlfs());
                            tasks.add(task);
                            break;
                        }
                    }
                }
            }else{
                TransactionTask task = ModifyHelper.service.newTransactionTask(changeTaskInfoBean.getTaskTheme(), changeTaskInfoBean.getChangeDescribe(),
                        changeTaskInfoBean.getResponsible(), changeTaskInfoBean.getNeedDate(), null,changeTaskInfoBean.getTaskType(), changeTaskInfoBean.getGlfs());
                tasks.add(task);
            }
        }
        //根据上一步骤收集的模型对象，与ECN建立关联关系
        linkTransactionECA(ecn, tasks);
    }
}
