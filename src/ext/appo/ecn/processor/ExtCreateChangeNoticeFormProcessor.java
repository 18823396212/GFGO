package ext.appo.ecn.processor;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.windchill.enterprise.change2.forms.processors.CreateChangeNoticeFormProcessor;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.util.AffectedObjectUtil;
import ext.appo.change.util.ChangeActivity2Util;
import ext.appo.change.util.ModifyUtils;
import ext.appo.change.util.TransactionECAUtil;
import ext.appo.ecn.common.util.ChangePartQueryUtils;
import ext.appo.ecn.common.util.ChangeUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.part.filter.StandardPartsRevise;
import ext.appo.util.PartUtil;
import ext.lang.PICollectionUtils;
import ext.lang.PIStringUtils;
import ext.pi.PIException;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIClassificationHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import wt.change2.*;
import wt.configurablelink.ConfigurableDescribeLink;
import wt.doc.WTDocument;
import wt.fc.*;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTSet;
import wt.identity.IdentityFactory;
import wt.inf.container.WTContainer;
import wt.inf.library.WTLibrary;
import wt.lifecycle.LifeCycleManaged;
import wt.log4j.LogR;
import wt.part.PartDocHelper;
import wt.part.WTPart;
import wt.part.WTPartUsageLink;
import wt.pdmlink.PDMLinkProduct;
import wt.pds.StatementSpec;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.WhereExpression;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;
import wt.vc.Iterated;
import wt.vc.VersionControlHelper;

import java.io.IOException;
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
                    new TransactionECAUtil(changeOrder2, nmcommandBean);
                    /*
                     * 更新受影响对象的IBA属性
                     * 8.6、根据受影响对象，及变更类型，及类型(升版)创建ECA对象（BOM变更创建多个ECA对象，图纸变更分别创建不同的ECA对象，关联不同的图纸变更对象），
                     * 并ECA关联“受影响对象”，同步生成“产生的对象”。
                     */
                    new ChangeActivity2Util(changeOrder2, affectedObjectUtil.PAGEDATAMAP, affectedObjectUtil.CONSTRUCTRELATION);


                }
            }

            // 新增ChangeOrder2与产品关系
            saveAffectedEndItems(nmcommandBean, changeOrder2);


            // 回填所属产品线及所属项目
            HashMap<?, ?> comboBox = nmcommandBean.getComboBox();
            for (Object o : comboBox.keySet()) {
                String key = (String) o;
                if (key.contains("sscpx")) {
                    Object value = comboBox.get(key);
                    if (value instanceof List) {
                        List<?> list = (List<?>) value;
                        if (list.size() > 0) {
                            PIAttributeHelper.service.forceUpdateSoftAttribute(changeOrder2, "sscpx", list.get(0));
                        }
                    } else if (value != null) {
                        PIAttributeHelper.service.forceUpdateSoftAttribute(changeOrder2, "sscpx", value);
                    }
                } else if (key.contains("ssxm")) {
                    Object value = comboBox.get(key);
                    if (value instanceof List) {
                        List<?> list = (List<?>) value;
                        if (list.size() > 0) {
                            PIAttributeHelper.service.forceUpdateSoftAttribute(changeOrder2, "ssxm", list.get(0));
                        }
                    } else if (value != null) {
                        PIAttributeHelper.service.forceUpdateSoftAttribute(changeOrder2, "ssxm", value);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        } finally {
            SessionContext.setContext(previous);
        }

        return result;
    }




    /***
     * 新增ChangeOrder2与产品关系
     *
     * @param nmcommandBean
     * @param changeOrder2
     * @throws WTException
     */
    public void saveAffectedEndItems(NmCommandBean nmcommandBean, WTChangeOrder2 changeOrder2) throws WTException {
        if (nmcommandBean == null) {
            return;
        }

        try {
            Map<String, Object> parameterMap = nmcommandBean.getParameterMap();
            if (parameterMap.containsKey(ChangeConstants.AFFECTED_PRODUCT_ID)) {
                String[] endItemsArrayStr = (String[]) parameterMap.get(ChangeConstants.AFFECTED_PRODUCT_ID);
                if (endItemsArrayStr != null && endItemsArrayStr.length > 0) {
                    String endItemsJSON = endItemsArrayStr[0];
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("endItemsJSON : " + endItemsJSON);
                    }
                    if (PIStringUtils.isNull(endItemsJSON)) {
                        return;
                    }
                    // 页面表单中所有产品对象
                    Collection<WTPart> parentArray = new HashSet<>();
                    JSONArray jsonArray = new JSONArray(endItemsJSON);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String oid = jsonArray.getString(i);
                        if (PIStringUtils.isNotNull(oid)) {
                            if (!oid.contains(WTPart.class.getName())) {
                                continue;
                            }
                            parentArray.add((WTPart) ((new ReferenceFactory()).getReference(oid).getObject()));
                        }
                    }
                    // 存储需要移除的数据
                    Collection<ConfigurableDescribeLink> removeArray = new HashSet<ConfigurableDescribeLink>();
                    // 获取changeOrder2关联的Link
                    QueryResult qr = PersistenceHelper.manager.navigate(changeOrder2, ConfigurableDescribeLink.DESCRIBED_BY_ROLE, ConfigurableDescribeLink.class, false);
                    while (qr.hasMoreElements()) {
                        ConfigurableDescribeLink link = (ConfigurableDescribeLink) qr.nextElement();
                        if (link != null) {
                            Iterated iterated = link.getDescribedBy();
                            if (iterated instanceof WTPart) {
                                WTPart parentPart = (WTPart) iterated;
                                if (parentArray.contains(parentPart)) {
                                    parentArray.remove(parentPart);
                                } else {
                                    removeArray.add(link);
                                }
                            }
                        }
                    }
                    if (removeArray.size() > 0) {
                        PersistenceHelper.manager.delete(new WTHashSet(removeArray));
                    }
                    if (parentArray.size() > 0) {
                        TypeDefinitionReference td = TypedUtilityServiceHelper.service.getTypeDefinitionReference(ChangeConstants.CHANGEORDER2_ENDITEMS_LINK_TYPE);
                        if (td == null) {
                            throw new WTException(ChangeConstants.CHANGEORDER2_ENDITEMS_LINK_TYPE + " 可配置Link软类型未创建!");
                        }
                        WTSet wtSet = new WTHashSet();
                        for (WTPart parentPart : parentArray) {
                            wtSet.add(ConfigurableDescribeLink.newConfigurableDescribeLink(changeOrder2, parentPart, td));
                        }
                        PersistenceHelper.manager.save(wtSet);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }
    }

    /***
     * 设置ECN上下文容器
     *
     * @param nmCommandBean
     * @param changeOrder2
     *            ChangeOrder2对象
     * @return
     * @throws WTException
     */
    public WTChangeOrder2 setContainer(NmCommandBean nmCommandBean, WTChangeOrder2 changeOrder2) throws WTException {
        if (nmCommandBean == null || changeOrder2 == null) {
            return changeOrder2;
        }

        try {
            // TODO 添加获取对象上下文逻辑
            changeOrder2.setContainer(getWTContainer("影院产品库"));
            changeOrder2 = (WTChangeOrder2) PersistenceHelper.manager.save(changeOrder2);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            throw new WTException("无法在 影院产品库 库中创建更改通告对象!");
        }

        return changeOrder2;
    }

    /***
     * 根据上下文名称查询上下文容器
     *
     * @param containerName
     *            容器名称
     * @return
     * @throws WTException
     */
    public WTContainer getWTContainer(String containerName) throws WTException {
        WTContainer wtContainer = null;

        try {
            QuerySpec querySpec = new QuerySpec(PDMLinkProduct.class);
            querySpec.appendWhere(new SearchCondition(WTContainer.class, WTContainer.NAME, SearchCondition.EQUAL, containerName), new int[]{0});
            QueryResult qr = PersistenceServerHelper.manager.query(querySpec);
            if (qr.hasMoreElements()) {
                wtContainer = (WTContainer) qr.nextElement();
            }
            if (wtContainer == null) {
                querySpec = new QuerySpec(WTLibrary.class);
                querySpec.appendWhere(new SearchCondition(WTContainer.class, WTContainer.NAME, SearchCondition.EQUAL, containerName), new int[]{0});
                qr = PersistenceServerHelper.manager.query(querySpec);
                if (qr.hasMoreElements()) {
                    wtContainer = (WTContainer) qr.nextElement();
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return wtContainer;
    }

    /**
     * 根据部件编码查找部件的所有版本
     * @param number
     * @return
     * @throws WTException
     */
    private QueryResult getParts(String number) throws WTException {
        StatementSpec stmtSpec = new QuerySpec(WTPart.class);
        WhereExpression where = new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL, number.toUpperCase());
        QuerySpec querySpec = (QuerySpec) stmtSpec;
        querySpec.appendWhere(where, new int[]{0});
        return PersistenceServerHelper.manager.query(stmtSpec);
    }

}
