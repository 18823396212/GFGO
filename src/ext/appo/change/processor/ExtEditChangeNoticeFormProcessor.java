package ext.appo.change.processor;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.windchill.enterprise.change2.forms.processors.EditChangeNoticeFormProcessor;
import ext.appo.change.ModifyHelper;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.models.CorrelationObjectLink;
import ext.appo.change.models.TransactionTask;
import ext.appo.change.util.AffectedObjectUtil;
import ext.appo.change.util.ChangeActivity2Util;
import ext.appo.change.util.TransactionECAUtil;
import ext.appo.ecn.constants.ChangeConstants;
import ext.lang.PIStringUtils;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PICoreHelper;
import ext.pi.core.PIWorkflowHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.log4j.LogR;
import wt.org.WTPrincipalReference;
import wt.part.WTPart;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfState;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;
import wt.workflow.work.WorkflowHelper;

import java.util.*;

public class ExtEditChangeNoticeFormProcessor extends EditChangeNoticeFormProcessor implements ChangeConstants, ModifyConstants {

    private static final Logger LOGGER = LogR.getLogger(EditChangeNoticeFormProcessor.class.getName());
    private static final String ROUTINGNAME = "routingName";
    private Map<Persistable, Map<String, String>> PAGEDATAMAP = new HashMap<>();//页面中changeTaskArray控件值并根据规则解析为对应集合
    private Map<Persistable, Collection<Persistable>> CONSTRUCTRELATION = new HashMap<>();//根据受影响对象表单构建创建ECA时需要填充的数据关系
    private Set<Persistable> AFFECTEDOBJECT = new HashSet<>();//所有受影响对象，包括收集对象
    private Set<String> AFFECTEDDOC = new HashSet<>();//创建页面受影响对象列表的WTDocument编码

    @Override
    public FormResult postProcess(NmCommandBean nmcommandBean, List<ObjectBean> objectBeans) throws WTException {
        FormResult result = new FormResult();
        result.setStatus(FormProcessingStatus.SUCCESS);

        SessionContext previous = SessionContext.newContext();
        try {
            SessionHelper.manager.setAdministrator();// 当前用户设置为管理员，用于忽略权限
            WTChangeOrder2 changeOrder2 = (WTChangeOrder2) objectBeans.get(0).getObject();//ECN
            AffectedObjectUtil affectedObjectUtil = new AffectedObjectUtil(nmcommandBean, changeOrder2);//受影响对象列表
            TransactionECAUtil transactionUtil = new TransactionECAUtil(changeOrder2, nmcommandBean);//事务性任务列表

            String routingName = nmcommandBean.getRequest().getParameter(ROUTINGNAME);
            LOGGER.info(">>>>>>>>>>routingName: " + routingName);

            Set<String> messages = new HashSet<>();
            //暂存操作
            if (CONSTANTS_1.equals(routingName)) {
                /*
                 * 9.0、至少一条受影响对象，必填项验证。
                 * 9.1、检查受影响对象是否存在未结束的ECN，有则不允许创建。
                 * 9.2、检查受影响对象的状态必须是已归档及已发布。
                 * 9.3、检查受影响对象不能为标准件。
                 * 检查受影响对象列表中是否存在已修订的对象
                 */
//                affectedObjectUtil.cacheButton();
                //编辑ECN完成按钮，无需校验受影响对象是否最新版本
                affectedObjectUtil.editOkButton();
                //校验 任务主题 是否重复
                transactionUtil.check();

                messages.addAll(affectedObjectUtil.MESSAGES);
                messages.addAll(transactionUtil.MESSAGES);
                if (messages.size() > 0) {
                    throw new WTException(compoundMessage(messages));
                } else {
                    ChangeActivity2Util activity2Util = new ChangeActivity2Util(changeOrder2, affectedObjectUtil.PAGEDATAMAP, affectedObjectUtil.CONSTRUCTRELATION);

                    //更新受影响对象的IBA属性
                    activity2Util.cacheButton();

                    //新增ChangeOrder2与受影响对象的关系
                    linkAffectedItems(changeOrder2, affectedObjectUtil.PAGEDATAMAP, activity2Util.BRANCHIDMAP);
                }
                PIAttributeHelper.service.forceUpdateSoftAttribute(changeOrder2, ATTRIBUTE_6, routingName);
            }
            //add by lzy at 20200110 start
            else if (CONSTANTS_12.equals(routingName)) {
                /**
                 * 新暂存按钮
                 * 9.0、至少一条受影响对象，必填项验证。
                 * 9.1、检查受影响对象是否存在未结束的ECN，有则不允许创建。
                 */
//                affectedObjectUtil.newCacheButton();
                //校验 任务主题 是否重复
//                transactionUtil.check();

//                messages.addAll(affectedObjectUtil.MESSAGES);
//                messages.addAll(transactionUtil.MESSAGES);
                if (messages.size() > 0) {
                    throw new WTException(compoundMessage(messages));
                } else {
                    ChangeActivity2Util activity2Util = new ChangeActivity2Util(changeOrder2, affectedObjectUtil.PAGEDATAMAP, affectedObjectUtil.CONSTRUCTRELATION);

                    //更新受影响对象的IBA属性
                    activity2Util.cacheButton();

                    //新增ChangeOrder2与受影响对象的关系
                    linkAffectedItems(changeOrder2, affectedObjectUtil.PAGEDATAMAP, activity2Util.BRANCHIDMAP);
                }
                PIAttributeHelper.service.forceUpdateSoftAttribute(changeOrder2, ATTRIBUTE_6, routingName);
            }
            //add by lzy at 20200110 end
            else {
                /*
                 * 8.0、至少一条受影响对象，必填项验证。
                 * 8.1、检查受影响对象是否存在未结束的ECN（包含的ECA非取消状态），有则不允许创建。
                 * 8.2、检查受影响对象的状态必须是已归档及已发布。
                 * 8.3、检查受影响对象不能为标准件。
                 * 8.4、A“ECN和完成功能” ，提交的时候校验图纸是否收集，如果没有收集，要给提交人提示“xx部件未收集图纸，请收集图纸！”
                 * 校验需要收集上层对象的部件是否满足收集条件
                 * 检查是否存在单独进行变更的说明文档
                 * 检查受影响对象列表中是否存在已修订的对象
                 */
                affectedObjectUtil.okButton();
                //校验 任务主题 是否重复
                transactionUtil.check();

                messages.addAll(affectedObjectUtil.MESSAGES);
                messages.addAll(transactionUtil.MESSAGES);

                if (messages.size() > 0) {
                    throw new WTException(compoundMessage(messages));
                } else {
                    //8.5、创建事务性任务的ECA；
                    transactionUtil.createEditChangeActivity2();

                    ChangeActivity2Util activity2Util = new ChangeActivity2Util(changeOrder2, affectedObjectUtil.PAGEDATAMAP, affectedObjectUtil.CONSTRUCTRELATION);
                    /*
                     * 更新受影响对象的IBA属性
                     * 8.6、根据受影响对象，及变更类型，及类型(升版)创建ECA对象（BOM变更创建多个ECA对象，图纸变更分别创建不同的ECA对象，关联不同的图纸变更对象），
                     * 并ECA关联“受影响对象”，同步生成“产生的对象”。
                     */
                    activity2Util.okButton();

                    //新增ChangeOrder2与受影响对象的关系
                    linkAffectedItems(changeOrder2, affectedObjectUtil.PAGEDATAMAP, activity2Util.BRANCHIDMAP);

                    //自动完成「修改变更申请」
                    completeWorkItem(changeOrder2);
                }
            }

            // 新增受影响产品列表与ECN的关系(ECN与受影响产品链接)
            linkAffectedEndItems(nmcommandBean, changeOrder2);

            //创建事务性任务-模型对象，已存在则更新
            transactionUtil.createTransactionECA();

            //根据上一步骤收集的模型对象，与ECN建立关联关系
            linkTransactionECA(changeOrder2, transactionUtil.TASKS);

            // 更新ECN「所属产品类别」「所属项目」
            UpdateSoftAttribute(nmcommandBean, changeOrder2);
        } finally {
            SessionContext.setContext(previous);
        }

        return result;
    }

    /**
     * 新增ChangeOrder2与受影响对象的关系
     *
     * @param changeOrder2
     * @param pageDataMap
     * @param branchMap
     * @throws WTException
     */
    public void linkAffectedItems(WTChangeOrder2 changeOrder2, Map<Persistable, Map<String, String>> pageDataMap, Map<String, String> branchMap) throws WTException {
        if (changeOrder2 == null || pageDataMap.size() < 1) return;

        String ecnVid = String.valueOf(PICoreHelper.service.getBranchId(changeOrder2));
        LOGGER.info(">>>>>>>>>>linkAffectedItems.ecnVid: " + ecnVid);
        for (Map.Entry<Persistable, Map<String, String>> entry : pageDataMap.entrySet()) {
            LOGGER.info(">>>>>>>>>>linkAffectedItems.entry: " + entry);
            Persistable persistable = entry.getKey();
            Map<String, String> attributeMap = entry.getValue();
            String aadDescription = attributeMap.get(AADDESCRIPTION_COMPID);
            LOGGER.info(">>>>>>>>>>linkAffectedItems.aadDescription: " + aadDescription);
            String collectionNumber = attributeMap.get(ATTRIBUTE_12);
            LOGGER.info(">>>>>>>>>>linkAffectedItems.collectionNumber: " + collectionNumber);

            String branchId = String.valueOf(PICoreHelper.service.getBranchId(persistable));
            LOGGER.info(">>>>>>>>>>linkAffectedItems.branchId: " + branchId);
            CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, branchId, LINKTYPE_1);
            LOGGER.info(">>>>>>>>>>linkAffectedItems.link: " + link);
            if (link == null) {
                String ecaIdentifier = branchMap.get(branchId);//获取所在ECA
                LOGGER.info(">>>>>>>>>>linkAffectedItems.ecaIdentifier: " + ecaIdentifier);
                String routing = StringUtils.isEmpty(ecaIdentifier) ? "" : ROUTING_1;
                LOGGER.info(">>>>>>>>>>linkAffectedItems.routing: " + routing);
                link = ModifyHelper.service.newCorrelationObjectLink(changeOrder2, persistable, LINKTYPE_1, ecnVid, branchId, ecaIdentifier, aadDescription, routing);
                try {
                    link.setCollection(collectionNumber);
                } catch (WTPropertyVetoException e) {
                    e.printStackTrace();
                }
                PersistenceServerHelper.manager.update(link);
            } else {
                String routing = link.getRouting();
                LOGGER.info(">>>>>>>>>>linkAffectedItems.routing: " + routing);
                if (!ROUTING_1.equals(routing) && !ROUTING_3.equals(routing)) {
                    String ecaIdentifier = branchMap.get(branchId);//获取所在ECA
                    LOGGER.info(">>>>>>>>>>linkAffectedItems.ecaIdentifier: " + ecaIdentifier);
                    routing = StringUtils.isEmpty(ecaIdentifier) ? routing : ROUTING_1;
                    LOGGER.info(">>>>>>>>>>linkAffectedItems.routing: " + routing);
                    link = ModifyHelper.service.updateCorrelationObjectLink(link, ecaIdentifier, aadDescription, routing);
                    try {
                        link.setCollection(collectionNumber);
                    } catch (WTPropertyVetoException e) {
                        e.printStackTrace();
                    }
                    PersistenceServerHelper.manager.update(link);
                }
            }
        }
    }

    /***
     * 新增ChangeOrder2与受影响产品的关系
     * @param nmcommandBean
     * @param changeOrder2
     * @throws WTException
     */
    public void linkAffectedEndItems(NmCommandBean nmcommandBean, WTChangeOrder2 changeOrder2) throws WTException {
        if (nmcommandBean == null || changeOrder2 == null) return;

        try {
            Map<String, Object> parameterMap = nmcommandBean.getParameterMap();
            if (parameterMap.containsKey(AFFECTED_PRODUCT_ID)) {
                String[] endItemsArrayStr = (String[]) parameterMap.get(AFFECTED_PRODUCT_ID);
                if (endItemsArrayStr != null && endItemsArrayStr.length > 0) {
                    String endItemsJSON = endItemsArrayStr[0];
                    LOGGER.info(">>>>>>>>>>linkAffectedEndItems.endItemsJSON: " + endItemsJSON);
                    if (PIStringUtils.isNull(endItemsJSON)) return;

                    // 页面表单中所有产品对象
                    Collection<Persistable> collection = new HashSet<>();
                    JSONArray jsonArray = new JSONArray(endItemsJSON);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String oid = jsonArray.getString(i);
                        if (PIStringUtils.isNotNull(oid)) {
                            if (!oid.contains(WTPart.class.getName())) continue;
                            collection.add(((new ReferenceFactory()).getReference(oid).getObject()));
                        }
                    }
                    LOGGER.info(">>>>>>>>>>linkAffectedEndItems.collection: " + collection);

                    String ecnVid = String.valueOf(PICoreHelper.service.getBranchId(changeOrder2));
                    LOGGER.info(">>>>>>>>>>linkAffectedEndItems.ecnVid: " + ecnVid);
                    for (Persistable persistable : collection) {
                        String branchId = String.valueOf(PICoreHelper.service.getBranchId(persistable));
                        LOGGER.info(">>>>>>>>>>linkAffectedEndItems.branchId: " + branchId);
                        CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, branchId, LINKTYPE_2);
                        LOGGER.info(">>>>>>>>>>linkAffectedEndItems.link: " + link);
                        //add by lzy at 20200605 start
                        String treatmentValue = getTreatmentValue(nmcommandBean, persistable);
                        //add by lzy at 20200605 end
                        if (link == null) {
                            ModifyHelper.service.newCorrelationObjectLink(changeOrder2, persistable, LINKTYPE_2, ecnVid, branchId, treatmentValue);
                        } else {
                            ModifyHelper.service.updateCorrelationObjectLink(ecnVid, branchId, LINKTYPE_2, treatmentValue);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
    }

    /**
     * 获取页面受影响产品处理方式值
     *
     * @param nmcommandBean
     * @param persistable
     * @return
     * @throws JSONException
     * @throws WTException
     */
    public String getTreatmentValue(NmCommandBean nmcommandBean, Persistable persistable) throws JSONException, WTException {
        String treatmentValue = "";
        String branchId = String.valueOf(PICoreHelper.service.getBranchId(persistable));
        Map<String, Object> parameterMap = nmcommandBean.getParameterMap();
        if (parameterMap.containsKey(ATTRIBUTE_14)) {
            String[] endItemsArrayStr = (String[]) parameterMap.get(ATTRIBUTE_14);
            if (endItemsArrayStr != null && endItemsArrayStr.length > 0) {
                String endItemsJSON = endItemsArrayStr[0];
                LOGGER.info(">>>>>>>>>>linkAffectedEndItems.endItemsJSON: " + endItemsJSON);
                if (PIStringUtils.isNull(endItemsJSON)) return treatmentValue;

                // 页面表单中所有产品对象
                JSONArray jsonArray = new JSONArray(endItemsJSON);
                for (int i = 0; i < jsonArray.length(); i++) {
                    Map<String, Object> attributesMap = new HashMap<>();
                    JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                    Persistable persistable1 = null;
                    if (jsonObject.has("oid")) {
                        // 属性值
                        String value = jsonObject.getString("oid");
                        // 获取主对象
                        persistable1 = (new ReferenceFactory()).getReference(value).getObject();
                        String branchId1 = String.valueOf(PICoreHelper.service.getBranchId(persistable1));
                        if (persistable1 != null && branchId.equals(branchId1) && jsonObject.has("clfs")) {
                            //返回值值
                            treatmentValue = jsonObject.getString("clfs");
                        }
                    }
                }
            }
        }
        return treatmentValue;
    }

    /**
     * 新增ChangeOrder2与事务性任务的关系
     *
     * @param changeOrder2
     * @param tasks
     * @throws Exception
     */
    public void linkTransactionECA(WTChangeOrder2 changeOrder2, Set<TransactionTask> tasks) throws WTException {
        if (changeOrder2 == null || tasks.size() < 1) return;

        String ecnVid = String.valueOf(changeOrder2.getBranchIdentifier());
        LOGGER.info(">>>>>>>>>>linkTransactionECA.ecnVid: " + ecnVid);
        for (TransactionTask task : tasks) {
            String taskOid = String.valueOf(task.getPersistInfo().getObjectIdentifier().getId());
            LOGGER.info(">>>>>>>>>>linkTransactionECA.taskOid: " + taskOid);
            CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, taskOid, LINKTYPE_3);
            LOGGER.info(">>>>>>>>>>linkTransactionECA.link: " + link);
            if (link == null) {
                ModifyHelper.service.newCorrelationObjectLink(changeOrder2, task, LINKTYPE_3, ecnVid, taskOid);
            } else {
                ModifyHelper.service.updateCorrelationObjectLink(ecnVid, taskOid, LINKTYPE_3);
            }
        }
    }

    /**
     * 更新ECN「所属产品类别」「所属项目」
     *
     * @param nmcommandBean
     * @param changeOrder2
     * @throws WTException
     */
    private void UpdateSoftAttribute(NmCommandBean nmcommandBean, WTChangeOrder2 changeOrder2) throws WTException {
        HashMap<?, ?> comboBox = nmcommandBean.getComboBox();
        for (Object object : comboBox.keySet()) {
            String key = (String) object;
            if (key.contains("sscpx")) {
                Object value = comboBox.get(key);
                if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    if (list.size() > 0) {
                        PIAttributeHelper.service.forceUpdateSoftAttribute(changeOrder2, ATTRIBUTE_2, list.get(0));//所属产品类别
                    }
                } else if (value != null) {
                    PIAttributeHelper.service.forceUpdateSoftAttribute(changeOrder2, ATTRIBUTE_2, value);//所属产品类别
                }
            } else if (key.contains("ssxm")) {
                Object value = comboBox.get(key);
                if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    if (list.size() > 0) {
                        PIAttributeHelper.service.forceUpdateSoftAttribute(changeOrder2, ATTRIBUTE_3, list.get(0));//所属项目
                    }
                } else if (value != null) {
                    PIAttributeHelper.service.forceUpdateSoftAttribute(changeOrder2, ATTRIBUTE_3, value);//所属项目
                }
            }
        }
    }

    /**
     * 自动完成「修改变更申请」
     *
     * @param changeOrder2
     * @throws WTException
     */
    private void completeWorkItem(WTChangeOrder2 changeOrder2) throws WTException {
        WTPrincipalReference reference = SessionHelper.manager.getPrincipalReference();
        Vector vector = new Vector();
        vector.add("提交");

        QueryResult result = WfEngineHelper.service.getAssociatedProcesses(changeOrder2, WfState.OPEN_RUNNING, null);
        LOGGER.info(">>>>>>>>>>completeWorkItem.result: " + result.size());
        while (result.hasMoreElements()) {
            WfProcess process = (WfProcess) result.nextElement();
            LOGGER.info(">>>>>>>>>>completeWorkItem.process:" + process);

            QueryResult workItems = PIWorkflowHelper.service.findWorkItems(process, false);
            LOGGER.info(">>>>>>>>>>completeWorkItem.workItems:" + workItems.size());
            while (workItems.hasMoreElements()) {
                WorkItem workItem = (WorkItem) workItems.nextElement();
                WfAssignedActivity activity = (WfAssignedActivity) workItem.getSource().getObject();
                String name = activity.getName();
                LOGGER.info(">>>>>>>>>>completeWorkItem.name:" + name);
                if (name.contains(CONSTANTS_2)) {
                    WorkflowHelper.service.workComplete(workItem, reference, vector);
                }
            }
        }
    }

    /**
     * 合成错误信息
     *
     * @param messages
     * @return
     */
    public String compoundMessage(Set<String> messages) {
        StringBuilder builder = new StringBuilder();
        if (messages.size() > 0) {
            builder.append("无法保存变更申请，存在以下问题：").append("\n");
            int i = 1;
            for (String message : messages) {
                builder.append(i++).append(". ").append(message).append("\n");
            }
        }
        return builder.toString();
    }

    //add by lzy at 20200414

    /**
     * 更新受影响产品处理方式属性
     *
     * @param nmcommandBean
     */
    private void updateAffectedEndItemsAttribute(NmCommandBean nmcommandBean) throws JSONException, WTException {
        Map<String, Object> parameterMap = nmcommandBean.getParameterMap();
        if (parameterMap.containsKey(ATTRIBUTE_14)) {
            String[] endItemsArrayStr = (String[]) parameterMap.get(ATTRIBUTE_14);
            if (endItemsArrayStr != null && endItemsArrayStr.length > 0) {
                String endItemsJSON = endItemsArrayStr[0];
                LOGGER.info(">>>>>>>>>>linkAffectedEndItems.endItemsJSON: " + endItemsJSON);
                if (PIStringUtils.isNull(endItemsJSON)) return;

                // 页面表单中所有产品对象
                JSONArray jsonArray = new JSONArray(endItemsJSON);
                for (int i = 0; i < jsonArray.length(); i++) {
                    Map<String, Object> attributesMap = new HashMap<>();
                    JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                    Persistable persistable = null;
                    if (jsonObject.has("oid")) {
                        // 属性值
                        String value = jsonObject.getString("oid");
                        // 获取主对象
                        persistable = (new ReferenceFactory()).getReference(value).getObject();
                        if (persistable != null && jsonObject.has("clfs")) {
                            // 属性值
                            String clfsValue = jsonObject.getString("clfs");
                            attributesMap.put(ATTRIBUTE_13, clfsValue == null ? "" : clfsValue);
                            PIAttributeHelper.service.forceUpdateSoftAttributes(persistable, attributesMap);
                        }

                    }
                }
            }
        }
    }
}
