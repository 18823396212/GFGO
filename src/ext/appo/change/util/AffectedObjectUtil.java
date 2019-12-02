package ext.appo.change.util;

import com.ptc.netmarkets.util.beans.NmCommandBean;
import ext.appo.change.ModifyHelper;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.models.CorrelationObjectLink;
import ext.appo.ecn.common.util.ChangeUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.ecn.pdf.PdfUtil;
import ext.appo.part.filter.StandardPartsRevise;
import ext.lang.PICollectionUtils;
import ext.lang.PIStringUtils;
import ext.pi.core.PICoreHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import wt.change2.*;
import wt.configurablelink.ConfigurableDescribeLink;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.*;
import wt.identity.IdentityFactory;
import wt.lifecycle.LifeCycleManaged;
import wt.log4j.LogR;
import wt.part.PartDocHelper;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.util.WTException;
import wt.vc.VersionControlHelper;

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

                                if (attributesMap.containsKey(CHANGETYPE_COMPID)) {
                                    String changeType = attributesMap.get(CHANGETYPE_COMPID);
                                    if (PIStringUtils.isNotNull(changeType)) {
                                        //根据用户所选"类型"为「替换」必须收集上层部件
                                        if (changeType.contains(VALUE_1)) CHILDPART.add(part);
                                        //用户所选"类型"为「升版」的部件
                                        else if (changeType.contains(VALUE_4)) LVERSIONPART.add(part);
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
                    Set<String> result = PICollectionUtils.difference(parentInfoArray, AFFECTEDPARTNUMBER);
                    if (result.size() > 0) {
                        MESSAGES.add(part.getDisplayIdentity() + " 用户所选\"类型\"为「替换」必须收集所有上层对象！");
                    }
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
//
//
                            //add by lzy at 20191130 start
                            //判断关联的ECN是否非「已取消」「已解决」状态，用户所选"类型"为「替换」的部件则无需判断
                            if ((!ChangeUtils.checkState(changeOrder2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeOrder2, ChangeConstants.RESOLVED))&&LVERSIONPART.contains(part)) {
                                MESSAGES.add("物料: " + number + " 存在未解决的ECN: " + changeOrder2.getNumber() + " 不能同时提交两个ECN！");
                                flag = true;
                                flog = false;
                                break;
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
                                if ((!ChangeUtils.checkState(changeOrder2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeOrder2, ChangeConstants.RESOLVED))&&LVERSIONPART.contains(part)) {
                                    MESSAGES.add("物料: " + number + " 存在未解决的ECN: " + changeOrder2.getNumber() + " 不能同时提交两个ECN！");
                                    flag = true;
                                    break;
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
                numbers.add(ModifyUtils.getNumber(persistable));
            }

            Set<String> result = PICollectionUtils.intersect(numbers, AFFECTEDDOCNUMBER);
            if (numbers.size() > 0 && result.size() < 1) MESSAGES.add("部件: " + part.getNumber() + "未收集图纸，请收集图纸！");
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
                            if ((!ChangeUtils.checkState(changeOrder2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeOrder2, ChangeConstants.RESOLVED))&&LVERSIONPART.contains(part)) {
                                MESSAGES.add("物料: " + number + " 存在未解决的ECN: " + changeOrder2.getNumber() + " 不能同时提交两个ECN！");
                                flag = true;
                                flog = false;
                                break;
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
                                if ((!ChangeUtils.checkState(changeOrder2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeOrder2, ChangeConstants.RESOLVED))&&LVERSIONPART.contains(part)) {
                                    MESSAGES.add("物料: " + number + " 存在未解决的ECN: " + changeOrder2.getNumber() + " 不能同时提交两个ECN！");
                                    flag = true;
                                    break;
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
     * @param order
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

}
