package ext.appo.change.processor;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.windchill.enterprise.change2.forms.processors.CreateChangeNoticeFormProcessor;
import ext.appo.change.ModifyHelper;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.models.CorrelationObjectLink;
import ext.appo.change.models.TransactionTask;
import ext.appo.change.util.AffectedObjectUtil;
import ext.appo.change.util.ChangeActivity2Util;
import ext.appo.change.util.ModifyUtils;
import ext.appo.change.util.TransactionECAUtil;
import ext.appo.ecn.constants.ChangeConstants;
import ext.lang.PIStringUtils;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PICoreHelper;
import org.apache.log4j.Logger;
import org.json.JSONArray;
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

    private static final Logger LOGGER = LogR.getLogger(CreateChangeNoticeFormProcessor.class.getName());
    private static final String ROUTINGNAME = "routingName";
    private static final String ROUTINGNAME_1 = "cacheButton";
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
            if (ROUTINGNAME_1.equals(routingName)) {
                /*
                 * 9.0、至少一条受影响对象，必填项验证。
                 * 9.1、检查受影响对象是否存在未结束的ECN，有则不允许创建。
                 * 9.2、检查受影响对象的状态必须是已归档及已发布。
                 * 9.3、检查受影响对象不能为标准件。
                 * 检查受影响对象列表中是否存在已修订的对象
                 */
                affectedObjectUtil.cacheButton();
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
                    linkAffectedItems(changeOrder2, affectedObjectUtil.PAGEDATAMAP.keySet());

                    //创建事务性任务-模型对象
                    transactionUtil.createTransactionECA();

                    //根据上一步骤收集的模型对象，与ECN建立关联关系
                    linkTransactionECA(changeOrder2, transactionUtil.TASKS);
                }
            } else {
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
                    ChangeActivity2Util activity2Util = new ChangeActivity2Util(changeOrder2, affectedObjectUtil.PAGEDATAMAP, affectedObjectUtil.CONSTRUCTRELATION);

                    //8.5、创建事务性任务的ECA；
                    transactionUtil.createEditChangeActivity2();

                    //创建事务性任务-模型对象，已存在则更新
                    transactionUtil.createTransactionECA();

                    //根据上一步骤收集的模型对象，与ECN建立关联关系
                    linkTransactionECA(changeOrder2, transactionUtil.TASKS);

                    /*
                     * 更新受影响对象的IBA属性
                     * 8.6、根据受影响对象，及变更类型，及类型(升版)创建ECA对象（BOM变更创建多个ECA对象，图纸变更分别创建不同的ECA对象，关联不同的图纸变更对象），
                     * 并ECA关联“受影响对象”，同步生成“产生的对象”。
                     */
                    activity2Util.okButton();

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
     * 新增ChangeOrder2与受影响对象的关系
     * @param changeOrder2
     * @param collections
     * @throws Exception
     */
    public void linkAffectedItems(WTChangeOrder2 changeOrder2, Collection<Persistable> collections) throws WTException {
        if (changeOrder2 == null || collections.size() < 1) return;

        String ecnVid = String.valueOf(PICoreHelper.service.getBranchId(changeOrder2));
        LOGGER.info(">>>>>>>>>>linkAffectedItems.ecnVid: " + ecnVid);
        for (Persistable persistable : collections) {
            String branchId = String.valueOf(PICoreHelper.service.getBranchId(persistable));
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

                    String ecnVid = String.valueOf(PICoreHelper.service.getBranchId(changeOrder2));
                    LOGGER.info(">>>>>>>>>>linkAffectedEndItems.ecnVid: " + ecnVid);
                    for (Persistable persistable : collection) {
                        String branchId = String.valueOf(PICoreHelper.service.getBranchId(persistable));
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

    /**
     * 合成错误信息
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

}
