package ext.appo.ecn.processor;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.windchill.enterprise.change2.forms.processors.CreateChangeNoticeFormProcessor;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.util.*;
import ext.appo.ecn.constants.ChangeConstants;
import ext.lang.PIStringUtils;
import ext.pi.core.PIAttributeHelper;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import wt.change2.WTChangeOrder2;
import wt.configurablelink.ConfigurableDescribeLink;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.ReferenceFactory;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTSet;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;
import wt.vc.Iterated;

import java.util.*;

public class ExtCreateChangeNoticeFormProcessor extends CreateChangeNoticeFormProcessor {

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

    private Set<String> MESSAGES = new HashSet<>();
    //add by tongwang 20191023 end

    @Override
    public FormResult postProcess(NmCommandBean nmcommandBean, List<ObjectBean> objectBeans) throws WTException {
        FormResult result = new FormResult();
        result.setStatus(FormProcessingStatus.SUCCESS);

        SessionContext previous = SessionContext.newContext();
        try {
            // 当前用户设置为管理员，用于忽略权限
            SessionHelper.manager.setAdministrator();
            WTChangeOrder2 changeOrder2 = (WTChangeOrder2) objectBeans.get(0).getObject();//ECN

            String actionName = nmcommandBean.getRequest().getParameter(ACTIONNAME);
            LOGGER.info(">>>>>>>>>>actionName: " + actionName);
            //暂存操作
            if (ACTIONNAME_1.equals(actionName)) {
                //只创建流程（检查是否生命周期触发）
                //保存页面受影响对象列表、受影响产品列表、事务性任务列表数据与ECN建立关联关系
                //当点击ECN编辑按钮时，受影响对象列表、受影响产品列表、事务性任务列表初始化逻辑：
                //1.未创建ECA的情况，获取ECN关联的数据进行初始化；再次点击暂存按钮增量建立关联关系
                //2.已创建ECA的情况，参照旧逻辑从关联的ECA初始化数据
                //3.已创建ECA并点击过暂存按钮的情况，参照旧逻辑从关联的ECA初始化数据；并且从流程变量中获取新增数据
                //4.编辑页面点击确定时，创建完ECA后清空流程变量

                //以上方式不好，考虑使用Link记录

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
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        } finally {
            SessionContext.setContext(previous);
        }

        return result;
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
                        PIAttributeHelper.service.forceUpdateSoftAttribute(changeOrder2, ModifyConstants.ATTRIBUTE_2, list.get(0));//所属产品类别
                    }
                } else if (value != null) {
                    PIAttributeHelper.service.forceUpdateSoftAttribute(changeOrder2, ModifyConstants.ATTRIBUTE_2, value);//所属产品类别
                }
            } else if (key.contains("ssxm")) {
                Object value = comboBox.get(key);
                if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    if (list.size() > 0) {
                        PIAttributeHelper.service.forceUpdateSoftAttribute(changeOrder2, ModifyConstants.ATTRIBUTE_3, list.get(0));//所属项目
                    }
                } else if (value != null) {
                    PIAttributeHelper.service.forceUpdateSoftAttribute(changeOrder2, ModifyConstants.ATTRIBUTE_3, value);//所属项目
                }
            }
        }
    }

    /**
     * 新增ChangeOrder2与受影响对象的关系
     * @param changeOrder2
     * @param collections
     * @throws Exception
     */
    public void linkAffectedItems(WTChangeOrder2 changeOrder2, Collection<Persistable> collections) throws Exception {
        if (changeOrder2 == null || collections.size() < 1) return;

        // 获取需要移除的Link
        Collection<ConfigurableDescribeLink> removeArray = new HashSet<>();
        Map<Persistable, ConfigurableDescribeLink> linkMap = ModifyUtils.getDescribedBy(changeOrder2, ModifyConstants.TYPE_4);
        LOGGER.info(">>>>>>>>>>linkAffectedItems.linkMap: " + linkMap);
        for (Persistable persistable : linkMap.keySet()) {
            if (collections.contains(persistable)) {
                collections.remove(persistable);
            } else {
                removeArray.add(linkMap.get(persistable));
            }
        }

        LOGGER.info(">>>>>>>>>>linkAffectedItems.removeArray: " + removeArray);
        if (removeArray.size() > 0) {
            PersistenceHelper.manager.delete(new WTHashSet(removeArray));
        }

        LOGGER.info(">>>>>>>>>>linkAffectedItems.collections: " + collections);
        if (collections.size() > 0) {
            TypeDefinitionReference td = TypedUtilityServiceHelper.service.getTypeDefinitionReference(ModifyConstants.TYPE_4);
            if (td == null) {
                throw new WTException(ModifyConstants.TYPE_4 + " 可配置Link软类型未创建!");
            }
            WTSet wtSet = new WTHashSet();
            for (Persistable persistable : collections) {
                if (persistable instanceof Iterated)
                    wtSet.add(ConfigurableDescribeLink.newConfigurableDescribeLink(changeOrder2, (Iterated) persistable, td));
            }
            PersistenceHelper.manager.save(wtSet);
        }
    }

    /***
     * 新增ChangeOrder2与受影响产品的关系
     * @param nmcommandBean
     * @param changeOrder2
     * @throws WTException
     */
    public void linkAffectedEndItems(NmCommandBean nmcommandBean, WTChangeOrder2 changeOrder2) throws Exception {
        if (nmcommandBean == null || changeOrder2 == null) return;

        Map<String, Object> parameterMap = nmcommandBean.getParameterMap();
        if (parameterMap.containsKey(ChangeConstants.AFFECTED_PRODUCT_ID)) {
            String[] endItemsArrayStr = (String[]) parameterMap.get(ChangeConstants.AFFECTED_PRODUCT_ID);
            if (endItemsArrayStr != null && endItemsArrayStr.length > 0) {
                String endItemsJSON = endItemsArrayStr[0];
                LOGGER.info(">>>>>>>>>>endItemsJSON: " + endItemsJSON);
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
                LOGGER.info(">>>>>>>>>>collection1: " + collection);

                // 获取需要移除的Link
                Collection<ConfigurableDescribeLink> removeArray = new HashSet<>();
                Map<Persistable, ConfigurableDescribeLink> linkMap = ModifyUtils.getDescribedBy(changeOrder2, ModifyConstants.TYPE_5);
                LOGGER.info(">>>>>>>>>>linkMap: " + linkMap);
                for (Persistable persistable : linkMap.keySet()) {
                    if (collection.contains(persistable)) {
                        collection.remove(persistable);
                    } else {
                        removeArray.add(linkMap.get(persistable));
                    }
                }

                LOGGER.info(">>>>>>>>>>removeArray: " + removeArray);
                if (removeArray.size() > 0) {
                    PersistenceHelper.manager.delete(new WTHashSet(removeArray));
                }

                LOGGER.info(">>>>>>>>>>collection2: " + collection);
                if (collection.size() > 0) {
                    TypeDefinitionReference td = TypedUtilityServiceHelper.service.getTypeDefinitionReference(ModifyConstants.TYPE_5);
                    if (td == null) {
                        throw new WTException(ModifyConstants.TYPE_5 + " 可配置Link软类型未创建!");
                    }
                    WTSet wtSet = new WTHashSet();
                    for (Persistable persistable : collection) {
                        if (persistable instanceof Iterated)
                            wtSet.add(ConfigurableDescribeLink.newConfigurableDescribeLink(changeOrder2, (Iterated) persistable, td));
                    }
                    PersistenceHelper.manager.save(wtSet);
                }
            }
        }
    }

}
