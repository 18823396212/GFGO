package ext.appo.ecn.processor;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.windchill.enterprise.change2.forms.processors.CreateChangeNoticeFormProcessor;
import ext.appo.change.ModifyHelper;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.models.CorrelationObjectLink;
import ext.appo.change.models.TransactionTask;
import ext.appo.change.util.*;
import ext.appo.ecn.constants.ChangeConstants;
import ext.lang.PIStringUtils;
import ext.pi.core.PIAttributeHelper;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.fc.ReferenceFactory;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.util.WTException;

import java.util.*;

public class ExtCreateChangeNoticeFormProcessor extends CreateChangeNoticeFormProcessor implements ChangeConstants, ModifyConstants {

    private static final String CLASSNAME = ExtCreateChangeNoticeFormProcessor.class.getName();
    private static final Logger LOGGER = LogR.getLogger(CLASSNAME);
    //add by tongwang 20191023 start
    private static final String ACTIONNAME = "actionName";
    private static final String ACTIONNAME_1 = "cacheButton";
    private static final String ACTIONNAME_2 = "okButton";
    private static final String SEPARATOR_1 = "_";
    private Map<Persistable, Map<String, String>> PAGEDATAMAP = new HashMap<>();//页面中changeTaskArray控件值并根据规则解析为对应集合
    private Map<Persistable, Collection<Persistable>> CONSTRUCTRELATION = new HashMap<>();//根据受影响对象表单构建创建ECA时需要填充的数据关系
    private Set<Persistable> AFFECTEDOBJECT = new HashSet<>();//所有受影响对象，包括收集对象
    private Set<String> AFFECTEDDOC = new HashSet<>();//创建页面受影响对象列表的WTDocument编码
    //add by tongwang 20191023 end

    @Override
    public FormResult postProcess(NmCommandBean nmcommandBean, List<ObjectBean> objectBeans) throws WTException {
        FormResult result = new FormResult();
        result.setStatus(FormProcessingStatus.SUCCESS);

        SessionContext previous = SessionContext.newContext();
        try {
            SessionHelper.manager.setAdministrator();// 当前用户设置为管理员，用于忽略权限
            WTChangeOrder2 changeOrder2 = (WTChangeOrder2) objectBeans.get(0).getObject();//ECN

            String actionName = nmcommandBean.getRequest().getParameter(ACTIONNAME);
            LOGGER.info(">>>>>>>>>>actionName: " + actionName);
            //暂存操作
            if (ACTIONNAME_1.equals(actionName)) {
                /*
                 * 9.0、至少一条受影响对象，必填项验证。
                 * 9.1、检查受影响对象是否存在未结束的ECN，有则不允许创建。
                 * 9.2、检查受影响对象的状态必须是已归档及已发布。
                 * 9.3、检查受影响对象不能为标准件。
                 */
                CacheAffectedObjectUtil affectedObjectUtil = new CacheAffectedObjectUtil(nmcommandBean, changeOrder2);
                String message = affectedObjectUtil.compoundMessage();
                if (message.length() > 0) {
                    throw new WTException(message);
                } else {
                    //新增ChangeOrder2与受影响对象的关系
                    linkAffectedItems(changeOrder2, affectedObjectUtil.PAGEDATAMAP.keySet());

                    //存储事务性任务列表数据
                    Set<TransactionTask> tasks = saveTransactionECA(changeOrder2, nmcommandBean);
                    LOGGER.info(">>>>>>>>>>postProcess.tasks: " + tasks);

                    //根据上一步骤收集的模型对象，与ECN建立关联关系
                    linkTransactionECA(changeOrder2, tasks);
                }
            }
            //确定操作
            else if (ACTIONNAME_2.equals(actionName)) {
                /*
                 * 8.0、至少一条受影响对象，必填项验证。
                 * 8.1、检查受影响对象是否存在未结束的ECN（包含的ECA非取消状态），有则不允许创建。
                 * 8.2、检查受影响对象的状态必须是已归档及已发布。
                 * 8.3、检查受影响对象不能为标准件。
                 * 8.4、A“ECN和完成功能” ，提交的时候校验图纸是否收集，如果没有收集，要给提交人提示“xx部件未收集图纸，请收集图纸！”
                 * 校验需要收集上层对象的部件是否满足收集条件
                 * 检查是否存在单独进行变更的说明文档
                 */
                AffectedObjectUtil affectedObjectUtil = new AffectedObjectUtil(nmcommandBean, changeOrder2);
                String message = affectedObjectUtil.compoundMessage();
                if (message.length() > 0) {
                    throw new WTException(message);
                } else {
                    //8.5、创建事务性任务的ECA；
                    TransactionECAUtil ecaUtil = new TransactionECAUtil(changeOrder2, nmcommandBean);

                    //根据上一步骤收集的模型对象，与ECN建立关联关系
                    linkTransactionECA(changeOrder2, ecaUtil.TASKS);

                    /*
                     * 更新受影响对象的IBA属性
                     * 8.6、根据受影响对象，及变更类型，及类型(升版)创建ECA对象（BOM变更创建多个ECA对象，图纸变更分别创建不同的ECA对象，关联不同的图纸变更对象），
                     * 并ECA关联“受影响对象”，同步生成“产生的对象”。
                     */
                    new ChangeActivity2Util(changeOrder2, affectedObjectUtil.PAGEDATAMAP, affectedObjectUtil.CONSTRUCTRELATION);

                    //新增ChangeOrder2与受影响对象的关系
                    Collection<Persistable> collections = new HashSet<>();
                    for (Map.Entry<Persistable, Collection<Persistable>> entry : affectedObjectUtil.CONSTRUCTRELATION.entrySet()) {
                        collections.add(entry.getKey());
                        collections.addAll(entry.getValue());
                    }
                    linkAffectedItems(changeOrder2, collections);
                }
            }

            // 新增受影响产品列表与ECN的关系(ECN与受影响产品链接)
            linkAffectedEndItems(nmcommandBean, changeOrder2);

            // 更新ECN「所属产品类别」「所属项目」
            UpdateSoftAttribute(nmcommandBean, changeOrder2);
        } finally {
            SessionContext.setContext(previous);
        }

        return result;
    }

    /**
     * 创建 暂存 按钮的事务性任务-模型对象
     * @param changeOrder
     * @param nmcommandBean
     * @throws WTException
     * @return
     */
    public Set<TransactionTask> saveTransactionECA(WTChangeOrder2 changeOrder, NmCommandBean nmcommandBean) throws WTException {
        Set<TransactionTask> tasks = new HashSet<>();//事务性任务模型对象
        try {
            Map<String, Object> parameterMap = nmcommandBean.getParameterMap();
            LOGGER.info(">>>>>>>>>>saveTransactionECA.parameterMap: " + parameterMap);
            if (parameterMap.containsKey(DATA_ARRAY)) {
                String[] datasArrayStr = (String[]) parameterMap.get(DATA_ARRAY);
                if (datasArrayStr != null && datasArrayStr.length > 0) {
                    String datasJSON = datasArrayStr[0];
                    LOGGER.info(">>>>>>>>>>saveTransactionECA.datasJSON: " + datasJSON);
                    if (PIStringUtils.isNotNull(datasJSON)) {
                        JSONObject jsonObject = new JSONObject(datasJSON);
                        Iterator<?> keyIterator = jsonObject.keys();
                        while (keyIterator.hasNext()) {
                            Object key = keyIterator.next();
                            // 数据信息
                            JSONObject dataJSONObject = new JSONObject(jsonObject.getString((String) key));
                            LOGGER.info(">>>>>>>>>>saveTransactionECA.dataJSONObject: " + dataJSONObject);

                            WTChangeActivity2 eca = null;
                            // ECA对象OID
                            if (dataJSONObject.has(CHANGEACTIVITY2_COMPID)) {
                                String ecaOID = dataJSONObject.getString(CHANGEACTIVITY2_COMPID);
                                if (PIStringUtils.isNotNull(ecaOID)) {
                                    eca = (WTChangeActivity2) (new ReferenceFactory()).getReference(ecaOID).getObject();
                                }
                            }

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
                            LOGGER.info(">>>>>>>>>>changeTheme: " + changeTheme + " changeDescribe: " + changeDescribe + " responsible: " + responsible + " needDate: " + needDate);

                            //创建模型对象，保存事务性任务属性
                            TransactionTask task = ModifyHelper.service.queryTransactionTask(eca);
                            if (task == null) {
                                tasks.add(ModifyHelper.service.newTransactionTask(changeTheme, changeDescribe, responsible, needDate, eca));
                            } else {
                                tasks.add(ModifyHelper.service.updateTransactionTask(task, changeTheme, changeDescribe, responsible, needDate));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
        return tasks;
    }

    /**
     * 新增ChangeOrder2与受影响对象的关系
     * @param changeOrder2
     * @param collections
     * @throws Exception
     */
    public void linkAffectedItems(WTChangeOrder2 changeOrder2, Collection<Persistable> collections) throws WTException {
        if (changeOrder2 == null || collections.size() < 1) return;

        String ecnVid = ModifyUtils.geBranchId(changeOrder2);
        LOGGER.info(">>>>>>>>>>linkAffectedItems.ecnVid: " + ecnVid);
        for (Persistable persistable : collections) {
            String branchId = ModifyUtils.geBranchId(persistable);
            LOGGER.info(">>>>>>>>>>linkAffectedItems.branchId: " + branchId);
            CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, branchId, LINKTYPE_1);
            LOGGER.info(">>>>>>>>>>linkAffectedItems.link: " + link);
            if (link == null) {
                ModifyHelper.service.newCorrelationObjectLink(changeOrder2, persistable, LINKTYPE_1, ecnVid, branchId);
            } else {
                ModifyHelper.service.updateCorrelationObjectLink(ecnVid, branchId, LINKTYPE_1);
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

                    String ecnVid = ModifyUtils.geBranchId(changeOrder2);
                    LOGGER.info(">>>>>>>>>>linkAffectedEndItems.ecnVid: " + ecnVid);
                    for (Persistable persistable : collection) {
                        String branchId = ModifyUtils.geBranchId(persistable);
                        LOGGER.info(">>>>>>>>>>linkAffectedEndItems.branchId: " + branchId);
                        CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, branchId, LINKTYPE_2);
                        LOGGER.info(">>>>>>>>>>linkAffectedEndItems.link: " + link);
                        if (link == null) {
                            ModifyHelper.service.newCorrelationObjectLink(changeOrder2, persistable, LINKTYPE_2, ecnVid, branchId);
                        } else {
                            ModifyHelper.service.updateCorrelationObjectLink(ecnVid, branchId, LINKTYPE_2);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
    }

    /**
     * 新增ChangeOrder2与事务性任务的关系
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

}
