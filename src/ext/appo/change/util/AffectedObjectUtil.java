package ext.appo.change.util;

import com.ptc.netmarkets.util.beans.NmCommandBean;
import ext.appo.change.ModifyHelper;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.models.CorrelationObjectLink;
import ext.appo.ecn.common.util.ChangeUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.ecn.pdf.PdfUtil;
import ext.appo.part.filter.StandardPartsRevise;
import ext.appo.part.util.EffecitveBaselineUtil;
import ext.lang.PICollectionUtils;
import ext.lang.PIStringUtils;
import ext.pi.PIException;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIClassificationHelper;
import ext.pi.core.PICoreHelper;
import ext.pi.core.PIPartHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import wt.change2.*;
import wt.configurablelink.ConfigurableDescribeLink;
import wt.doc.WTDocument;
import wt.enterprise.Master;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMDocument;
import wt.fc.*;
import wt.fc.collections.WTCollection;
import wt.identity.IdentityFactory;
import wt.lifecycle.LifeCycleManaged;
import wt.log4j.LogR;
import wt.part.*;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.vc.OneOffVersioned;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;
import wt.vc.views.View;
import wt.vc.views.ViewHelper;
import wt.workflow.engine.WfProcess;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 处理受影响对象列表相关的逻辑
 */
public class AffectedObjectUtil implements ChangeConstants, ModifyConstants {

    private static final Logger LOGGER = LogR.getLogger(AffectedObjectUtil.class.getName());
    private static final String SEPARATOR_1 = "-";
    private NmCommandBean NMCOMMANDBEAN;
    private WTChangeOrder2 ORDER2;
    public Map<Persistable, Map<String, String>> PAGEDATAMAP = new HashMap<>();//ECN受影响对象集合
    public Map<Persistable, Collection<Persistable>> CONSTRUCTRELATION = new HashMap<>();//ECA受影响对象集合
    public Map<WTPart, Collection<Persistable>> PARTASSOCIATIONDOC = new HashMap<>();//部件关联的文档集合
    private Collection<WTPart> CHILDPART = new HashSet<>();//需要收集上层父件的子件集合（用户所选"类型"为「替换」的部件、物料分类为「PCB」「E1500000」的部件）
    private Collection<WTPart> LVERSIONPART = new HashSet<>();//用户所选"类型"为「升版」的部件
    private Collection<WTPart> BOMLVERSIONPART = new HashSet<>();//用户所选"类型"为「BOM变更升版」的部件
    private Map<String, Set<String>> PARENTMAP = new HashMap<>();//子件对应的上层父件编码集合
    private Set<String> AFFECTEDPARTNUMBER = new HashSet<>();//ECN受影响对象-部件编码集合
    private Set<String> AFFECTEDDOCNUMBER = new HashSet<>();//ECN受影响对象-文档编码集合
    private Set<String> NUMBERS = new HashSet<>();//ECN受影响对象-编码集合
    private Set<WTPart> AFFECTEDPART = new HashSet<>();//ECN受影响对象-部件集合
    private Set<Persistable> AFFECTEDDOC = new HashSet<>();//ECN受影响对象-文档集合
    private Set<Persistable> DISSOCIATEDOC = new HashSet<>();//ECN受影响对象-需要单独走ECA的文档集合（游离）
    public Set<String> MESSAGES = new HashSet<>();

    public AffectedObjectUtil(NmCommandBean nmcommandBean, WTChangeOrder2 changeOrder2) throws WTException {
        NMCOMMANDBEAN = nmcommandBean;
        ORDER2 = changeOrder2;
        if (NMCOMMANDBEAN != null) {
            //获取页面中受影响对象列表数据，以及属性集合
            getPageChangeTaskArray();
        }
    }

    /**
     * 新暂存按钮
     * @throws WTException
     */
    public void newCacheButton() throws WTException {
        if (NMCOMMANDBEAN != null && ORDER2 != null) {
            /*
             * 9.0、至少一条受影响对象，必填项验证。
             * 9.1、检查受影响对象是否存在未结束的ECN，有则不允许创建。
             */
            checkFives();
        }
    }

    /**
     * 暂存按钮
     * @throws WTException
     */
    public void cacheButton() throws WTException {
        if (NMCOMMANDBEAN != null && ORDER2 != null) {
            /*
             * 9.0、至少一条受影响对象，必填项验证。
             * 9.1、检查受影响对象是否存在未结束的ECN，有则不允许创建。
             * 9.2、检查受影响对象的状态必须是已归档及已发布。
             * 9.3、检查受影响对象不能为标准件。
             * 检查受影响对象列表中是否存在已修订的对象
             */
            checkFour();
        }
    }

    /**
     * 完成按钮
     * @throws WTException
     */
    public void okButton() throws WTException {
        if (NMCOMMANDBEAN != null && ORDER2 != null) {
            /*
             * 收集部件关联的文档集合
             * 收集需要收集上层父件集合
             */
            collectionOne();
            //收集子件对应的上层父件编码集合
            collectionTwo();
            //收集ECA受影响对象集合
            collectionThree();

            checkEnvProtection(ORDER2);

            //add by lzy at 20191213 start
//            checkObjectVesionNew();
            checkStandardPartsRevise();
            //add by lzy at 20191213 end
            //add by lzy at 20191227 start
            checkHasBom();
            //add by lzy at 20191227 end
            //add by lzy at 20200103 start
            checkProductStatus();
            //add by lzy at 20200103 end

            //校验需要收集上层对象的部件是否满足收集条件
            checkOne();
            /*
             * 8.0、至少一条受影响对象，必填项验证。
             * 8.1、检查受影响对象是否存在未结束的ECN（包含的ECA非取消状态），有则不允许创建。(已取消)
             * 8.1、检查受影响对象是否存在未结束的ECN（无需判断ECA状态），有则不允许创建。
             * 8.2、检查受影响对象的状态必须是已归档及已发布。
             * 8.3、检查受影响对象不能为标准件。
             * 8.4、A“ECN和完成功能” ，提交的时候校验图纸是否收集，如果没有收集，要给提交人提示“xx部件未收集图纸，请收集图纸！”
             * 检查受影响对象列表中是否存在已修订的对象
             */
            checkTwo();
            //检查是否存在单独进行变更的说明文档
            checkThree();
        }
    }

    /***
     * 获取页面中受影响对象列表数据，以及属性集合
     * @throws WTException
     */
    private void getPageChangeTaskArray() throws WTException {
        try {
            // 获取新增数据列
            Map<String, Object> parameterMap = NMCOMMANDBEAN.getParameterMap();
            if (parameterMap.containsKey(CHANGETASK_ARRAY)) {
                String[] changeTaskArrayStr = (String[]) parameterMap.get(CHANGETASK_ARRAY);
                if (changeTaskArrayStr != null && changeTaskArrayStr.length > 0) {
                    String datasJSON = changeTaskArrayStr[0];
                    LOGGER.info(">>>>>>>>>>datasJSON: " + datasJSON);
                    if (PIStringUtils.isNotNull(datasJSON)) {
                        JSONArray jsonArray = new JSONArray(datasJSON);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Persistable persistable = null;
                            // 存储页面属性信息
                            Map<String, String> attributesMap = new HashMap<>();
                            JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                            Iterator<String> keyIterator = jsonObject.keys();
                            while (keyIterator.hasNext()) {
                                // 属性ID
                                String key = keyIterator.next();
                                // 属性值
                                String value = jsonObject.getString(key);
                                if (PIStringUtils.isNotNull(value)) {
                                    if (key.equalsIgnoreCase(OID_COMPID)) {
                                        persistable = (new ReferenceFactory()).getReference(value).getObject();
                                        continue;
                                    }
                                    if (key.equals(COMPLETIONTIME_COMPID)) {
                                        if (value.contains(" ")) {
                                            value = value.substring(0, value.indexOf(" ")).trim();
                                        }
                                        if (value.contains("-")) {
                                            value = (new SimpleDateFormat(ChangeConstants.SIMPLE_DATE_FORMAT_02)).format((new SimpleDateFormat(ChangeConstants.SIMPLE_DATE_FORMAT_03)).parse(value));
                                        }
                                    }
                                    attributesMap.put(key, value);
                                }
                            }
                            if (persistable != null) {
                                PAGEDATAMAP.put(persistable, attributesMap);
                                NUMBERS.add(ModifyUtils.getNumber(persistable) + SEPARATOR_1 + ModifyUtils.getVersion(persistable));
                            }
                            if (persistable instanceof WTPart) {
                                WTPart part = (WTPart) persistable;
                                AFFECTEDPARTNUMBER.add(part.getNumber() + part.getViewName());
                                AFFECTEDPART.add(part);

                                //add by lzy at 20191231  start
                                if (attributesMap.containsKey(CHANGETYPE_COMPID)) {
                                    String changeType = attributesMap.get(CHANGETYPE_COMPID);
//                                if (attributesMap.containsKey(CHANGOBJECTETYPE_COMPID)) {
//                                    String changeType = attributesMap.get(CHANGOBJECTETYPE_COMPID);
                                 //add by lzy at 20191231  end
                                    if (PIStringUtils.isNotNull(changeType)) {
                                        //根据用户所选"类型"为「替换」必须收集上层部件
                                        if (changeType.contains(VALUE_1)) CHILDPART.add(part);
                                        //用户所选"类型"为「升版」的部件
                                        //add by lzy at 20191227 start
//                                        else if (changeType.contains(VALUE_4)) LVERSIONPART.add(part);
                                        if (changeType.contains(VALUE_4)) LVERSIONPART.add(part);
                                        if (changeType.contains(VALUE_5)) BOMLVERSIONPART.add(part);
                                        //add by lzy at 20191227 end
                                    }
                                }
                            }
                            if (persistable instanceof WTDocument) {
                                AFFECTEDDOCNUMBER.add(ModifyUtils.getNumber(persistable));
                                AFFECTEDDOC.add(persistable);
                            }
                            if (persistable instanceof EPMDocument) {
                                AFFECTEDDOCNUMBER.add(ModifyUtils.getNumber(persistable));
                                AFFECTEDDOC.add(persistable);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
    }

    /**
     * 收集部件关联的文档集合
     * 收集需要收集上层父件集合(物料分类为PCB、E1500000必须收集上层部件)
     * @throws WTException
     */
    private void collectionOne() throws WTException {
        for (Map.Entry<Persistable, Map<String, String>> entryMap : PAGEDATAMAP.entrySet()) {
            Persistable persistable = entryMap.getKey();//受影响对象
            if (persistable instanceof WTPart) {
                WTPart part = (WTPart) persistable;

                //按照收集规则获取关联的所有WTDocument文档和EPMDocument文档
                Collection<Persistable> associatedItems = new HashSet<>();//收集图文档
                QueryResult result = PartDocHelper.service.getAssociatedDocuments(part);//获取部件关联的图文档
                while (result.hasMoreElements()) {
                    Object object = result.nextElement();
                    if (object instanceof ObjectReference) {
                        object = ((ObjectReference) object).getObject();
                    }
                    associatedItems.add((Persistable) object);
                }
                PARTASSOCIATIONDOC.put(part, associatedItems);

                //物料分类为PCB、E1500000必须收集上层部件
                if (ModifyUtils.specificNode(part, VALUE_2) || ModifyUtils.specificNode(part, VALUE_3)) {
                    CHILDPART.add(part);
                }
            }
        }
    }

    /**
     * 收集子件对应的上层父件编码集合
     * @throws WTException
     */
    private void collectionTwo() throws WTException {
        // 批量查询上层父件
        Map<WTPartUsageLink, WTPart> parentMap = ModifyUtils.batchQueryFirstParents(CHILDPART);
        // 过滤非最新部件
        Collection<WTPart> parentArray = ModifyUtils.excludeNonLatestVersionsPart(parentMap.values());
        // 收集子件对应的上层父件
        for (WTPart parentPart : parentArray) {
            for (Map.Entry<WTPartUsageLink, WTPart> entryMap : parentMap.entrySet()) {
                if (PersistenceHelper.isEquivalent(parentPart, entryMap.getValue())) {
                    WTPartMaster childMaster = entryMap.getKey().getUses();
                    String number = childMaster.getNumber();
                    Set<String> parentInfoArray = PARENTMAP.computeIfAbsent(number, k -> new HashSet<>());
                    parentInfoArray.add(parentPart.getNumber() + parentPart.getViewName());
                }
            }
        }
    }

    /**
     * 收集ECA受影响对象集合
     * @throws WTException
     */
    private void collectionThree() {
        if (!PAGEDATAMAP.isEmpty()) {
            //生成ECA受影响对象，一个部件对应一组文档；启动一个ECA
            for (Map.Entry<WTPart, Collection<Persistable>> entryMap : PARTASSOCIATIONDOC.entrySet()) {
                WTPart part = entryMap.getKey();
                // 构建关系
                Collection<Persistable> associatedItems = new HashSet<>();
                for (Persistable persistable : entryMap.getValue()) {
                    String number = ModifyUtils.getNumber(persistable);
                    LOGGER.info(">>>>>>>>>>number: " + number);
                    if (AFFECTEDDOCNUMBER.contains(number)) {
                        associatedItems.add(persistable);
                    }
                }
                CONSTRUCTRELATION.put(part, associatedItems);
            }

            //游离的图纸（WTDocument、EPMDocument）单独走「图纸变更」ECA
            for (Persistable persistable : AFFECTEDDOC) {
                Boolean isDissociate = true;
                for (Collection<Persistable> collection : CONSTRUCTRELATION.values()) {
                    if (collection.contains(persistable)) {
                        isDissociate = false;
                        break;
                    }
                }
                if (isDissociate) {
                    CONSTRUCTRELATION.put(persistable, new HashSet<>());
                    DISSOCIATEDOC.add(persistable);
                }
            }
        }
    }

    /**
     * 校验需要收集上层对象的部件是否满足收集条件
     * @throws WTException
     */
    private void checkOne() throws WTException {
        /*
         * 用户所选"类型"为「替换」的部件
         * 物料分类为「PCB」「E1500000」的部件
         */
        for (WTPart part : CHILDPART) {
            String number = part.getNumber();
            LOGGER.info(">>>>>>>>>>number: " + number);

            if (PARENTMAP.containsKey(number)) {
                // 上层父件信息
                Set<String> parentInfoArray = PARENTMAP.get(number);
                LOGGER.info(">>>>>>>>>>parentInfoArray: " + parentInfoArray);

                //物料分类为「PCB」「E1500000」的部件
                if (ModifyUtils.specificNode(part, VALUE_2) || ModifyUtils.specificNode(part, VALUE_3)) {
                    Set<String> result = PICollectionUtils.intersect(parentInfoArray, AFFECTEDPARTNUMBER);
                    if (parentInfoArray.size() > 0 && result.size() < 1) {
                        MESSAGES.add(part.getDisplayIdentity() + " PCB部件上层父件必须至少收集一个！");
                    }
                }
                //用户所选"类型"为「替换」的部件
                else {
                    //add by lzy at 20200103 start
//                    Set<String> result = PICollectionUtils.difference(parentInfoArray, AFFECTEDPARTNUMBER);
//                    if (result.size() > 0) {
//                        MESSAGES.add(part.getDisplayIdentity() + " 用户所选\"类型\"为「替换」必须收集所有上层对象！");
//                    }
                    Set<String> result = PICollectionUtils.intersect(parentInfoArray, AFFECTEDPARTNUMBER);
                    if (parentInfoArray.size() > 0 && result.size() < 1) {
                        MESSAGES.add(part.getDisplayIdentity() + " 用户所选\"类型\"为「替换」上层父件必须至少收集一个！");
                    }
                    //add by lzy at 20200103 end
                }

            }
        }
    }

    /**
     * 8.0、至少一条受影响对象，必填项验证。
     * 8.1、检查受影响对象是否存在未结束的ECN（包含的ECA非取消状态），有则不允许创建。（已取消）
     * 8.1、检查受影响对象是否存在未结束的ECN（无需判断ECA状态），有则不允许创建。
     * 8.2、检查受影响对象的状态必须是已归档及已发布。
     * 8.3、检查受影响对象不能为标准件。
     * 8.4、A“ECN和完成功能” ，提交的时候校验图纸是否收集，如果没有收集，要给提交人提示“xx部件未收集图纸，请收集图纸！”
     * 检查受影响对象列表中是否存在已修订的对象
     */
    private void checkTwo() throws WTException {
        //8.0、至少一条受影响对象，必填项验证。
        if (PAGEDATAMAP.isEmpty()) {
            MESSAGES.add("受影响对象列表不能为空！");
        }

        //8.1、检查受影响对象是否存在未结束的ECN（包含的ECA非取消状态、以及暂存状态的ECN），有则不允许创建。（已取消）
        //8.1、检查受影响对象是否存在未结束的ECN（无需判断ECA状态、以及暂存状态的ECN），有则不允许创建。
        for (Persistable persistable : PAGEDATAMAP.keySet()) {
            if (persistable instanceof WTPart) {
                WTPart part = (WTPart) persistable;
                String number = part.getNumber();
                LOGGER.info(">>>>>>>>>>part:" + number);

                boolean flog = true;
                //先检查每个大版本的最新小版本是否有关联的ECA、ECN非「已取消」「已解决」状态
                QueryResult queryResult = VersionControlHelper.service.allVersionsOf(part.getMaster());//获取所有大版本的最新小版本
                while (queryResult.hasMoreElements()) {
                    WTPart oldPart = (WTPart) queryResult.nextElement();

                    boolean flag = false;
                    //获取对象所有关联的ECA对象
                    QueryResult result = ChangeHelper2.service.getAffectingChangeActivities(oldPart);
                    LOGGER.info(">>>>>>>>>>result size:" + result.size());
                    while (result.hasMoreElements()) {
                        WTChangeActivity2 changeActivity2 = (WTChangeActivity2) result.nextElement();
                        LOGGER.info(">>>>>>>>>>changeActivity2:" + changeActivity2.getNumber());

                        WTChangeOrder2 changeOrder2 = ChangeUtils.getEcnByEca(changeActivity2);
                        LOGGER.info(">>>>>>>>>>changeOrder2:" + changeOrder2.getNumber());
                        if (!ORDER2.getNumber().startsWith(changeOrder2.getNumber())) {
//                            //判断关联的ECA是否非「已取消」「已解决」状态
//                            if ((!ChangeUtils.checkState(changeActivity2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeActivity2, ChangeConstants.RESOLVED))) {
//                                MESSAGES.add("物料: " + number + " 存在未解决的ECA: " + changeActivity2.getNumber() + " 不能同时提交两个ECA！");
//                                flag = true;
//                                flog = false;
//                                break;
//                            }
                            //add by lzy at 20191130 start
                            //判断关联的ECN是否非「已取消」「已解决」状态，用户所选"类型"为「替换」的部件则无需判断
                            if ((!ChangeUtils.checkState(changeOrder2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeOrder2, ChangeConstants.RESOLVED))) {
                                if (!LVERSIONPART.contains(part)){
                                    break;
                                }else{
                                    MESSAGES.add("物料: " + number + " 存在未解决的ECN: " + changeOrder2.getNumber() + " 不能同时提交两个ECN！");
                                    flag = true;
                                    flog = false;
                                    break;
                                }
                            }
                            //add by lzy at 20191130 end

                        }
                    }
                    if (flag) break;
                }

                //再检查「暂存」的情况，遍历所有大版本的最新小版本检查是否关联非「已取消」「已解决」状态的ECN
                if (flog) {
                    boolean flag = false;
                    QueryResult result = VersionControlHelper.service.allVersionsOf(part.getMaster());//获取所有大版本的最新小版本
                    while (result.hasMoreElements()) {
                        WTPart oldPart = (WTPart) result.nextElement();
                        String branchId = String.valueOf(PICoreHelper.service.getBranchId(oldPart));
                        LOGGER.info(">>>>>>>>>>checkTwo.branchId: " + branchId);

                        Set<WTChangeOrder2> order2s = ModifyHelper.service.queryWTChangeOrder2(branchId, ModifyConstants.LINKTYPE_1);
                        LOGGER.info(">>>>>>>>>>checkTwo.order2s: " + order2s);
                        for (WTChangeOrder2 changeOrder2 : order2s) {
                            LOGGER.info(">>>>>>>>>>checkTwo.changeOrder2:" + changeOrder2.getNumber());
                            if (!ORDER2.getNumber().startsWith(changeOrder2.getNumber())) {
//                                //判断关联的ECN是否非「已取消」「已解决」状态
//                                if ((!ChangeUtils.checkState(changeOrder2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeOrder2, ChangeConstants.RESOLVED))) {
//                                    MESSAGES.add("物料: " + number + " 存在未解决的ECN: " + changeOrder2.getNumber() + " 不能同时提交两个ECN！");
//                                    flag = true;
//                                    break;
//                                }
                                //add by lzy at 20191130 start
                                //判断关联的ECN是否非「已取消」「已解决」状态,用户所选"类型"为「替换」的部件则无需判断
                                if ((!ChangeUtils.checkState(changeOrder2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeOrder2, ChangeConstants.RESOLVED))) {
                                    if (!LVERSIONPART.contains(part)){
                                        break;
                                    }else{
                                        MESSAGES.add("物料: " + number + " 存在未解决的ECN: " + changeOrder2.getNumber() + " 不能同时提交两个ECN！");
                                        flag = true;
                                        break;
                                    }

                                }
                                //add by lzy at 20191130 end
                            }
                        }
                        if (flag) break;
                    }
                }
            }
        }

        //8.2、检查受影响对象的状态必须是已归档及已发布。
        StringBuilder messages = new StringBuilder();
        for (Persistable persistable : PAGEDATAMAP.keySet()) {
            if (!ModifyUtils.checkState((LifeCycleManaged) persistable, ChangeConstants.ARCHIVED) && !ModifyUtils.checkState((LifeCycleManaged) persistable, ChangeConstants.RELEASED)) {
                messages.append(IdentityFactory.getDisplayIdentifier(persistable)).append("\n");
            }
        }
        if (messages.length() > 0) MESSAGES.add(messages.toString() + " 状态不满足：已归档或已发布！");

        //8.3、检查受影响对象不能为标准件。
        messages = new StringBuilder();
        try {
            List<Map> list = StandardPartsRevise.getExcelData();
            for (WTPart part : AFFECTEDPART) {
                if (LVERSIONPART.contains(part) && ModifyUtils.isStandardPart(list, part)) {
                    messages.append(part.getNumber()).append("、");
                }
            }

        } catch (WTException | IOException e) {
            throw new WTException(e.getStackTrace());
        }
        if (messages.length() > 0) MESSAGES.add(messages.toString() + " 业务定义为标准件，不能变更升版！");

        //8.4、A“ECN和完成功能” ，提交的时候校验图纸是否收集，如果没有收集，要给提交人提示“xx部件未收集图纸，请收集图纸！”
        for (Map.Entry<WTPart, Collection<Persistable>> entryMap : PARTASSOCIATIONDOC.entrySet()) {
            WTPart part = entryMap.getKey();

            Set<String> numbers = new HashSet<>();
            for (Persistable persistable : entryMap.getValue()) {
                //            add by lzy at 20191205 start
                if (persistable instanceof  WTDocument){
                    WTDocument document=(WTDocument) persistable;
                    //是否为说明文档,必须是说明文档
                    if (!PartDocHelper.isReferenceDocument(document)){
                        //部件说明文档中产品命名通知单不需要加入判断
                        if(!PICoreHelper.service.isType(document, "com.plm.productNamingNotic")){
                            numbers.add(ModifyUtils.getNumber(persistable));
                        }
                    }
                }

//                numbers.add(ModifyUtils.getNumber(persistable));
                //            add by lzy at 20191205 end
            }
            Set<String> result = PICollectionUtils.intersect(numbers, AFFECTEDDOCNUMBER);
            //            add by lzy at 20191205 start
            //替换类型部件无需判断是否收集图纸,升版才需判断
//            if (numbers.size() > 0 && result.size() < 1) MESSAGES.add("部件: " + part.getNumber() + "未收集图纸，请收集图纸！");
            if (LVERSIONPART.contains(part)){
                //PCBA不需要加入判断
                String classification = (String) PIAttributeHelper.service.getValue(part, "Classification");
                if (!classification.contains("appo_bcp01")){
                    if (numbers.size() > 0 && result.size() < 1) MESSAGES.add("部件: " + part.getNumber() + "未收集说明方文档，请至少收集一份说明方文档！");
                }
            }
            //            add by lzy at 20191205 end
        }

        //检查受影响对象列表中是否存在已修订的对象
        checkRevised();
    }

    /**
     * 检查是否存在单独进行变更的说明文档
     * @throws WTException
     */
    private void checkThree() {
        for (Persistable persistable : DISSOCIATEDOC) {
            if (persistable instanceof WTDocument) {
                WTDocument document = (WTDocument) persistable;
                if (!PartDocHelper.isReferenceDocument(document)) {
                    MESSAGES.add(document.getDisplayIdentity() + " 说明文档不能独立进行变更！");
                }
            }
        }
    }

    /**
     * 9.0、至少一条受影响对象，必填项验证。
     * 9.1、检查受影响对象是否存在未结束的ECN，有则不允许创建。
     * 9.2、检查受影响对象的状态必须是已归档及已发布。
     * 9.3、检查受影响对象不能为标准件。
     * 检查受影响对象列表中是否存在已修订的对象
     */
    private void checkFour() throws WTException {
        //9.0、至少一条受影响对象，必填项验证。
        if (PAGEDATAMAP.isEmpty()) {
            MESSAGES.add("受影响对象列表不能为空！");
        }

        //9.1、检查受影响对象是否存在未结束的ECN（包含的ECA非取消状态、以及暂存状态的ECN），有则不允许创建。(已取消)
        //9.1、检查受影响对象是否存在未结束的ECN（无需判断ECA状态、以及暂存状态的ECN），有则不允许创建。
        for (Persistable persistable : PAGEDATAMAP.keySet()) {
            if (persistable instanceof WTPart) {
                WTPart part = (WTPart) persistable;
                String number = part.getNumber();
                LOGGER.info(">>>>>>>>>>part:" + number);

                boolean flog = true;
                //先检查每个大版本的最新小版本是否有关联的ECA、ECN非「已取消」「已解决」状态
                QueryResult queryResult = VersionControlHelper.service.allVersionsOf(part.getMaster());//获取所有大版本的最新小版本
                while (queryResult.hasMoreElements()) {
                    WTPart oldPart = (WTPart) queryResult.nextElement();

                    boolean flag = false;
                    //获取对象所有关联的ECA对象
                    QueryResult result = ChangeHelper2.service.getAffectingChangeActivities(oldPart);
                    LOGGER.info(">>>>>>>>>>result size:" + result.size());
                    while (result.hasMoreElements()) {
                        WTChangeActivity2 changeActivity2 = (WTChangeActivity2) result.nextElement();
                        LOGGER.info(">>>>>>>>>>changeActivity2:" + changeActivity2.getNumber());

                        WTChangeOrder2 changeOrder2 = ChangeUtils.getEcnByEca(changeActivity2);
                        LOGGER.info(">>>>>>>>>>changeOrder2:" + changeOrder2.getNumber());
                        if (!ORDER2.getNumber().startsWith(changeOrder2.getNumber())) {
//                            //判断关联的ECA是否非「已取消」「已解决」状态
//                            if ((!ChangeUtils.checkState(changeActivity2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeActivity2, ChangeConstants.RESOLVED))) {
//                                MESSAGES.add("物料: " + number + " 存在未解决的ECA: " + changeActivity2.getNumber() + " 不能同时提交两个ECA！");
//                                flag = true;
//                                flog = false;
//                                break;
//                            }
//
//                            //判断关联的ECN是否非「已取消」「已解决」状态
//                            if ((!ChangeUtils.checkState(changeOrder2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeOrder2, ChangeConstants.RESOLVED))) {
//                                MESSAGES.add("物料: " + number + " 存在未解决的ECN: " + changeOrder2.getNumber() + " 不能同时提交两个ECN！");
//                                flag = true;
//                                flog = false;
//                                break;
//                            }
                            //add by lzy at 20191130 start
                            //判断关联的ECN是否非「已取消」「已解决」状态,用户所选"类型"为「替换」的部件则无需判断
                            if ((!ChangeUtils.checkState(changeOrder2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeOrder2, ChangeConstants.RESOLVED))) {
                                if (!LVERSIONPART.contains(part)){
                                    break;
                                }else{
                                    MESSAGES.add("物料: " + number + " 存在未解决的ECN: " + changeOrder2.getNumber() + " 不能同时提交两个ECN！");
                                    flag = true;
                                    flog = false;
                                    break;
                                }
                            }
                            //add by lzy at 20191130 end
                        }
                    }
                    if (flag) break;
                }

                //再检查「暂存」的情况，遍历所有大版本的最新小版本检查是否关联非「已取消」「已解决」状态的ECN
                if (flog) {
                    boolean flag = false;
                    QueryResult result = VersionControlHelper.service.allVersionsOf(part.getMaster());//获取所有大版本的最新小版本
                    while (result.hasMoreElements()) {
                        WTPart oldPart = (WTPart) result.nextElement();
                        String branchId = String.valueOf(PICoreHelper.service.getBranchId(oldPart));
                        LOGGER.info(">>>>>>>>>>checkTwo.branchId: " + branchId);

                        Set<WTChangeOrder2> order2s = ModifyHelper.service.queryWTChangeOrder2(branchId, ModifyConstants.LINKTYPE_1);
                        LOGGER.info(">>>>>>>>>>checkTwo.order2s: " + order2s);
                        for (WTChangeOrder2 changeOrder2 : order2s) {
                            LOGGER.info(">>>>>>>>>>checkTwo.changeOrder2:" + changeOrder2.getNumber());
                            if (!ORDER2.getNumber().startsWith(changeOrder2.getNumber())) {
                                //判断关联的ECN是否非「已取消」「已解决」状态
//                                if ((!ChangeUtils.checkState(changeOrder2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeOrder2, ChangeConstants.RESOLVED))) {
//                                    MESSAGES.add("物料: " + number + " 存在未解决的ECN: " + changeOrder2.getNumber() + " 不能同时提交两个ECN！");
//                                    flag = true;
//                                    break;
//                                }
                                //add by lzy at 20191130 start
                                //判断关联的ECN是否非「已取消」「已解决」状态,用户所选"类型"为「替换」的部件则无需判断
                                if ((!ChangeUtils.checkState(changeOrder2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeOrder2, ChangeConstants.RESOLVED))) {
                                    if (!LVERSIONPART.contains(part)){
                                        break;
                                    }else{
                                        MESSAGES.add("物料: " + number + " 存在未解决的ECN: " + changeOrder2.getNumber() + " 不能同时提交两个ECN！");
                                        flag = true;
                                        break;
                                    }
                                }
                                //add by lzy at 20191130 end


                            }
                        }
                        if (flag) break;
                    }
                }
            }
        }

        //9.2、检查受影响对象的状态必须是已归档及已发布。
        StringBuilder messages = new StringBuilder();
        for (Persistable persistable : PAGEDATAMAP.keySet()) {
            if (!ModifyUtils.checkState((LifeCycleManaged) persistable, ChangeConstants.ARCHIVED) && !ModifyUtils.checkState((LifeCycleManaged) persistable, ChangeConstants.RELEASED)) {
                messages.append(IdentityFactory.getDisplayIdentifier(persistable)).append("\n");
            }
        }
        if (messages.length() > 0) MESSAGES.add(messages.toString() + " 状态不满足：已归档或已发布！");

        //9.3、检查受影响对象不能为标准件。
        messages = new StringBuilder();
        try {
            List<Map> list = StandardPartsRevise.getExcelData();
            for (WTPart part : AFFECTEDPART) {
                if (LVERSIONPART.contains(part) && ModifyUtils.isStandardPart(list, part)) {
                    messages.append(part.getNumber()).append("、");
                }
            }

        } catch (WTException | IOException e) {
            throw new WTException(e.getStackTrace());
        }
        if (messages.length() > 0) MESSAGES.add(messages.toString() + " 业务定义为标准件，不能变更升版！");

        //检查受影响对象列表中是否存在已修订的对象
        checkRevised();
    }

    /**
     * 9.0、至少一条受影响对象，必填项验证。
     * 9.1、检查受影响对象是否存在未结束的ECN，有则不允许创建。
     */
    private void checkFives() throws WTException {
        //9.0、至少一条受影响对象，必填项验证。
        if (PAGEDATAMAP.isEmpty()) {
            MESSAGES.add("受影响对象列表不能为空！");
        }

        //9.1、检查受影响对象是否存在未结束的ECN（包含的ECA非取消状态、以及暂存状态的ECN），有则不允许创建。(已取消)
        //9.1、检查受影响对象是否存在未结束的ECN（无需判断ECA状态、以及暂存状态的ECN），有则不允许创建。
        for (Persistable persistable : PAGEDATAMAP.keySet()) {
            if (persistable instanceof WTPart) {
                WTPart part = (WTPart) persistable;
                String number = part.getNumber();
                LOGGER.info(">>>>>>>>>>part:" + number);

                boolean flog = true;
                //先检查每个大版本的最新小版本是否有关联的ECA、ECN非「已取消」「已解决」状态
                QueryResult queryResult = VersionControlHelper.service.allVersionsOf(part.getMaster());//获取所有大版本的最新小版本
                while (queryResult.hasMoreElements()) {
                    WTPart oldPart = (WTPart) queryResult.nextElement();

                    boolean flag = false;
                    //获取对象所有关联的ECA对象
                    QueryResult result = ChangeHelper2.service.getAffectingChangeActivities(oldPart);
                    LOGGER.info(">>>>>>>>>>result size:" + result.size());
                    while (result.hasMoreElements()) {
                        WTChangeActivity2 changeActivity2 = (WTChangeActivity2) result.nextElement();
                        LOGGER.info(">>>>>>>>>>changeActivity2:" + changeActivity2.getNumber());

                        WTChangeOrder2 changeOrder2 = ChangeUtils.getEcnByEca(changeActivity2);
                        LOGGER.info(">>>>>>>>>>changeOrder2:" + changeOrder2.getNumber());
                        if (!ORDER2.getNumber().startsWith(changeOrder2.getNumber())) {
//                            //判断关联的ECA是否非「已取消」「已解决」状态
//                            if ((!ChangeUtils.checkState(changeActivity2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeActivity2, ChangeConstants.RESOLVED))) {
//                                MESSAGES.add("物料: " + number + " 存在未解决的ECA: " + changeActivity2.getNumber() + " 不能同时提交两个ECA！");
//                                flag = true;
//                                flog = false;
//                                break;
//                            }
//
//                            //判断关联的ECN是否非「已取消」「已解决」状态
//                            if ((!ChangeUtils.checkState(changeOrder2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeOrder2, ChangeConstants.RESOLVED))) {
//                                MESSAGES.add("物料: " + number + " 存在未解决的ECN: " + changeOrder2.getNumber() + " 不能同时提交两个ECN！");
//                                flag = true;
//                                flog = false;
//                                break;
//                            }
                            //add by lzy at 20191130 start
                            //判断关联的ECN是否非「已取消」「已解决」状态,用户所选"类型"为「替换」的部件则无需判断
                            if ((!ChangeUtils.checkState(changeOrder2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeOrder2, ChangeConstants.RESOLVED))) {
                                if (!LVERSIONPART.contains(part)) {
                                    break;
                                } else {
                                    MESSAGES.add("物料: " + number + " 存在未解决的ECN: " + changeOrder2.getNumber() + " 不能同时提交两个ECN！");
                                    flag = true;
                                    flog = false;
                                    break;
                                }
                            }
                            //add by lzy at 20191130 end
                        }
                    }
                    if (flag) break;
                }

                //再检查「暂存」的情况，遍历所有大版本的最新小版本检查是否关联非「已取消」「已解决」状态的ECN
                if (flog) {
                    boolean flag = false;
                    QueryResult result = VersionControlHelper.service.allVersionsOf(part.getMaster());//获取所有大版本的最新小版本
                    while (result.hasMoreElements()) {
                        WTPart oldPart = (WTPart) result.nextElement();
                        String branchId = String.valueOf(PICoreHelper.service.getBranchId(oldPart));
                        LOGGER.info(">>>>>>>>>>checkTwo.branchId: " + branchId);

                        Set<WTChangeOrder2> order2s = ModifyHelper.service.queryWTChangeOrder2(branchId, ModifyConstants.LINKTYPE_1);
                        LOGGER.info(">>>>>>>>>>checkTwo.order2s: " + order2s);
                        for (WTChangeOrder2 changeOrder2 : order2s) {
                            LOGGER.info(">>>>>>>>>>checkTwo.changeOrder2:" + changeOrder2.getNumber());
                            if (!ORDER2.getNumber().startsWith(changeOrder2.getNumber())) {
                                //判断关联的ECN是否非「已取消」「已解决」状态
//                                if ((!ChangeUtils.checkState(changeOrder2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeOrder2, ChangeConstants.RESOLVED))) {
//                                    MESSAGES.add("物料: " + number + " 存在未解决的ECN: " + changeOrder2.getNumber() + " 不能同时提交两个ECN！");
//                                    flag = true;
//                                    break;
//                                }
                                //add by lzy at 20191130 start
                                //判断关联的ECN是否非「已取消」「已解决」状态,用户所选"类型"为「替换」的部件则无需判断
                                if ((!ChangeUtils.checkState(changeOrder2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeOrder2, ChangeConstants.RESOLVED))) {
                                    if (!LVERSIONPART.contains(part)) {
                                        break;
                                    } else {
                                        MESSAGES.add("物料: " + number + " 存在未解决的ECN: " + changeOrder2.getNumber() + " 不能同时提交两个ECN！");
                                        flag = true;
                                        break;
                                    }
                                }
                                //add by lzy at 20191130 end


                            }
                        }
                        if (flag) break;
                    }
                }
            }
        }
    }





    /**
     * 检查受影响对象列表中是否存在已修订的对象
     * @throws WTException
     */
    private void checkRevised() throws WTException {
        LOGGER.info(">>>>>>>>>>checkRevised.NUMBERS: " + NUMBERS);
        Map<WTChangeActivity2, Collection<Changeable2>> map = ModifyUtils.getChangeablesAfter(ORDER2);
        LOGGER.info(">>>>>>>>>>checkRevised.map: " + map.size());
        for (Map.Entry<WTChangeActivity2, Collection<Changeable2>> entry : map.entrySet()) {
            WTChangeActivity2 activity2 = entry.getKey();
            Collection<Changeable2> collection = entry.getValue();
            LOGGER.info(">>>>>>>>>>checkRevised.collection: " + collection.size());
            for (Changeable2 changeable2 : collection) {
                String number = ModifyUtils.getNumber(changeable2) + SEPARATOR_1 + ModifyUtils.getVersion(changeable2);
                LOGGER.info(">>>>>>>>>>checkRevised.number: " + number);
                if (SEPARATOR_1.equals(number)) continue;
                if (NUMBERS.contains(number)) {
                    MESSAGES.add("受影响对象「" + number + "」已存在当前变更申请关联的变更任务「" + activity2.getNumber() + "」的产生对象中！");
                }
            }
        }
    }


    /**
     * 若是否经过环保评审为否，环保说明必填
     *
     * @param
     * @throws WTException
     */
    public void checkEnvProtection(ChangeOrder2 ecn) throws WTException {
        if (PdfUtil.getIBAObjectValue(ecn, "ISEnvProtectionReview") != null
                && PdfUtil.getIBAObjectValue(ecn, "ISEnvProtectionReview") instanceof String) {
            String ISEnvProtectionReview = (String) PdfUtil.getIBAObjectValue(ecn, "ISEnvProtectionReview");
            if ("否".equals(ISEnvProtectionReview)) {
                if (PdfUtil.getIBAObjectValue(ecn, "EnvProtectionDesc") == null
                        || (PdfUtil.getIBAObjectValue(ecn, "EnvProtectionDesc") instanceof String
                        && "".equals((String) PdfUtil.getIBAObjectValue(ecn, "EnvProtectionDesc")))) {
                    throw new WTException(" 未经过环保评审，请您必须填写'环保说明'的属性字段.");
                }
            }
        }
    }

    /**
     * 归档状态下未进BOM的定制件、PCBA及归档状态的软件可通过修订来升版(只需考虑升版的部件)
     */
    private void checkStandardPartsRevise() throws WTException {
        //选择升版的部件
        for (WTPart part : LVERSIONPART) {
            String cls = (String) PIAttributeHelper.service.getValue(part, "Classification");
            String state=part.getState().toString();
            String number=part.getNumber();
            // PCBA归档状态，没有被bom使用过的
            if (cls.contains("appo_bcp01")&& !isbyuse(part)&& state.endsWith("ARCHIVED")){
                MESSAGES.add("物料："+number+"是归档状态下未进BOM的PCBA，请通过修订升版！");
            }
            //归档状态的软件
            if (number.startsWith("X")&&state.endsWith("ARCHIVED")) {
                MESSAGES.add("物料："+number+"是归档状态的软件，请通过修订升版！");
            }
            //非A、B、X开头成品、半成品、软件归档状态物料,没有被bom使用到
            if (!number.startsWith("A")&&!number.startsWith("B")&&!number.startsWith("X")&&!isbyuse(part)&&state.endsWith("ARCHIVED")){
                MESSAGES.add("物料："+number+"是归档状态下未进BOM的定制件，请通过修订升版！");
            }

        }

    }

    private void checkProductStatus() throws WTException {
        for (Persistable persistable : PAGEDATAMAP.keySet()) {
            if (persistable instanceof WTPart) {
                WTPart part=(WTPart)persistable;
                // 是否成品
                if (specificNode(part, "成品") || specificNode(part, "appo_cp")) {
                    // 判断‘产品状态’
                    String jdztValue = PIAttributeHelper.service.getValue(part, JDZT) == null ? ""
                            : (String) PIAttributeHelper.service.getValue(part, JDZT);
                    if (("停止销售").equals(jdztValue) || ("停止生产").equals(jdztValue) || ("停止维护").equals(jdztValue)) {
                        MESSAGES.add("受影响对象中，"+part.getNumber()+"产品状态不能为‘停止销售’、‘停止维护’、‘停止生产’");
                    }
                }
            }
        }

    }


    /**
     * 判断部件同一视图是不是最新大版本(有部件可通过修订升版)
     * @throws WTException
     */
    private void checkObjectVesionNew() throws WTException {
        for (Persistable persistable : PAGEDATAMAP.keySet()) {
            if (persistable instanceof WTPart) {
                Vector latestVector= new Vector();
                WTPart part = (WTPart) persistable;
                String number=part.getNumber();
                String version = part.getVersionInfo().getIdentifier().getValue();
                String view=part.getViewName();
                //获取同一视图下最新物料
                latestVector=getAllLatestWTParts(view,number);
                if (latestVector!=null&&latestVector.size()>0){
                    WTPart newPart= (WTPart) latestVector.get(0);
                    if (newPart!=null){
                        String newNumber=newPart.getNumber();
                        String newVersion = newPart.getVersionInfo().getIdentifier().getValue();
                        String newView=newPart.getViewName();
                        if (!version.equals(newVersion)){
                            MESSAGES.add("物料："+number+"不是最新版本！");
                        }
                    }

                }
            }

        }

    }


    /*
     * 通过number获取某视图的最新的物料
     */
    public static Vector getAllLatestWTParts(String viewName, String number) throws WTException {
        QuerySpec qs = new QuerySpec(WTPart.class);

        View view = ViewHelper.service.getView(viewName);
        SearchCondition sc = new SearchCondition(WTPart.class, "view.key.id", SearchCondition.EQUAL,
                view.getPersistInfo().getObjectIdentifier().getId());
        qs.appendWhere(sc);
        if (number.trim().length() > 0) {
            qs.appendAnd();
            SearchCondition scNumber = new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL,
                    number.toUpperCase());
            qs.appendWhere(scNumber);
        }

        SearchCondition scLatestIteration = new SearchCondition(WTPart.class, WTAttributeNameIfc.LATEST_ITERATION,
                SearchCondition.IS_TRUE);
        qs.appendAnd();
        qs.appendWhere(scLatestIteration);

        QueryResult qr = PersistenceHelper.manager.find(qs);
        if (qr != null && qr.hasMoreElements())
            qr = (new LatestConfigSpec()).process(qr);

        if (qr != null && qr.hasMoreElements())
            return qr.getObjectVectorIfc().getVector();

        return new Vector();
    }

    /**
     * 判断物料是否被BOM使用
     * @param part
     * @return
     * @throws WTException
     */
    public Boolean isbyuse(WTPart part) throws WTException {
        //默认被使用
        Boolean isbyuse=true;
        QueryResult parentResult = WTPartHelper.service.getUsedByWTParts((WTPartMaster) part.getMaster());
        // 剔除非最新版本的BOM,剔除掉“A版本”“正在工作”的BOM
        List<WTPart> partList = deleteAAndInworkData(parentResult);
        if (partList == null || partList.size() == 0) {
            isbyuse = false;
        }
        return  isbyuse;
    }

    /**
     * 过滤掉非最新版本的母件，过滤掉A版本正在工作的母件
     *
     * @param qr
     * @return
     */
    public List<WTPart> deleteAAndInworkData(QueryResult qr) {
        List<WTPart> parentParts = new ArrayList<WTPart>();

        while (qr != null && qr.hasMoreElements()) {
            Object obj = qr.nextElement();
            if (obj instanceof WTPart) {
                WTPart parentPart = (WTPart) obj;
                WTPart newParentPart = (WTPart) getLatestVersionByMaster(parentPart.getMaster());
                // 过滤不是最新版本的物料
                if (!getOidByObject(parentPart).equals(getOidByObject(newParentPart))) {
                    continue;
                }
                // 过滤掉为A版本正在工作的物料
                String version = parentPart.getVersionInfo().getIdentifier().getValue();
                String state = parentPart.getState().toString();

                if ("INWORK".equals(state) && "A".equals(version)) {
                    continue;
                }
                parentParts.add(parentPart);
            }
        }

        return parentParts;
    }

    /**
     * 获取最新大版本的最新小版本
     *
     * @param master
     * @return
     */
    public static Persistable getLatestVersionByMaster(Master master) {
        try {
            if (master != null) {
                QueryResult qrVersions = VersionControlHelper.service.allVersionsOf(master);
                while (qrVersions.hasMoreElements()) {
                    Persistable p = (Persistable) qrVersions.nextElement();
                    if (!VersionControlHelper.isAOneOff((OneOffVersioned) p)) {
                        return p;
                    }
                }
            }
        } catch (QueryException e) {
            e.printStackTrace();
        } catch (WTException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getOidByObject(Persistable p) {
        String oid = "";
        if (p instanceof WfProcess) {
            oid = "OR:wt.workflow.engine.WfProcess:" + p.getPersistInfo().getObjectIdentifier().getId();
            return oid;
        }
        if (p != null) {
            oid = "OR:" + p.toString();
        }
        return oid;
    }


    /**
     * BOM变更升版需要判断当条受影响的对象是否存在BOM结构
     */
    private void checkHasBom() throws WTException {
        //选择BOM变更升版的部件
        for (WTPart part : BOMLVERSIONPART) {
            WTCollection childrens = PIPartHelper.service.findChildren(part);
            if (childrens.size() <= 0) {
                MESSAGES.add("物料"+part.getNumber()+"没有BOM结构,不能BOM变更升版!");
            }

        }

    }

    /***
     * 判断物料是否为指定分类
     *
     * @param part
     *            部件
     * @param nodeName
     *            分类
     * @return
     * @throws WTException
     */
    public static Boolean specificNode(WTPart part, String nodeName) throws WTException {
        Boolean isSpecificNode = false;
        if (part == null || PIStringUtils.isNull(nodeName)) {
            return isSpecificNode;
        }

        Collection<String> classifyNodeArray = PIClassificationHelper.service.getClassifyNodes(part);
        for (String classifyNode : classifyNodeArray) {
            // 分类完整路径
            String classifyNodePath = PIClassificationHelper.service.getNodeLocalizedHierarchy(classifyNode,
                    SessionHelper.getLocale());
            // 判断是否为指定分类部件
            List<String> clfArray = new ArrayList<String>();
            if (classifyNodePath.contains(USER_KEYWORD4)) {
                clfArray = Arrays.asList(classifyNodePath.split(USER_KEYWORD4));
            } else {
                clfArray.add(classifyNodePath);
            }
            if (clfArray.contains(nodeName)) {
                isSpecificNode = true;
                break;
            }
        }

        return isSpecificNode;
    }


}

