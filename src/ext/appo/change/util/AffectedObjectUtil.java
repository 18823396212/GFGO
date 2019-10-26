package ext.appo.change.util;

import com.ptc.netmarkets.util.beans.NmCommandBean;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.ecn.common.util.ChangeUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.part.filter.StandardPartsRevise;
import ext.lang.PICollectionUtils;
import ext.lang.PIStringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import wt.change2.ChangeHelper2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
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
    private Set<WTPart> AFFECTEDPART = new HashSet<>();//ECN受影响对象-部件集合
    private Set<Persistable> AFFECTEDDOC = new HashSet<>();//ECN受影响对象-文档集合
    private Set<Persistable> DISSOCIATEDOC = new HashSet<>();//ECN受影响对象-需要单独走ECA的文档集合（游离）
    private Set<String> MESSAGES = new HashSet<>();

    public AffectedObjectUtil(NmCommandBean nmcommandBean, WTChangeOrder2 changeOrder2) throws WTException {
        NMCOMMANDBEAN = nmcommandBean;
        ORDER2 = changeOrder2;
        if (NMCOMMANDBEAN != null && ORDER2 != null) {
            //获取页面中受影响对象列表数据，以及属性集合
            getPageChangeTaskArray();
            /*
             * 收集部件关联的文档集合
             * 收集需要收集上层父件集合
             */
            collectionOne();
            //收集子件对应的上层父件编码集合
            collectionTwo();

            //校验需要收集上层对象的部件是否满足收集条件
            checkOne();
            /*
             * 8.0、至少一条受影响对象，必填项验证。
             * 8.1、检查受影响对象是否存在未结束的ECN（包含的ECA非取消状态），有则不允许创建。
             * 8.2、检查受影响对象的状态必须是已归档及已发布。
             * 8.3、检查受影响对象不能为标准件。
             * 8.4、A“ECN和完成功能” ，提交的时候校验图纸是否收集，如果没有收集，要给提交人提示“xx部件未收集图纸，请收集图纸！”
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
        if (NMCOMMANDBEAN != null) {
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
                                }
                                if (persistable instanceof WTPart) {
                                    WTPart part = (WTPart) persistable;
                                    AFFECTEDPARTNUMBER.add(part.getNumber() + part.getViewName());
                                    AFFECTEDPART.add(part);
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
    }

    /**
     * 收集部件关联的文档集合
     * 收集需要收集上层父件集合
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

                Map<String, String> attributeInfoMap = entryMap.getValue();
                if (attributeInfoMap.containsKey(CHANGETYPE_COMPID)) {
                    String attributeValue = attributeInfoMap.get(CHANGETYPE_COMPID);
                    if (PIStringUtils.isNotNull(attributeValue)) {
                        //根据用户所选"类型"为「替换」必须收集上层部件
                        if (attributeValue.contains(VALUE_1)) CHILDPART.add(part);
                        //用户所选"类型"为「升版」的部件
                        else if (attributeValue.contains(VALUE_4)) LVERSIONPART.add(part);
                    }
                }

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
                    if (result.size() < 1) {
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
     * 8.1、检查受影响对象是否存在未结束的ECN（包含的ECA非取消状态），有则不允许创建。
     * 8.2、检查受影响对象的状态必须是已归档及已发布。
     * 8.3、检查受影响对象不能为标准件。
     * 8.4、A“ECN和完成功能” ，提交的时候校验图纸是否收集，如果没有收集，要给提交人提示“xx部件未收集图纸，请收集图纸！”
     */
    private void checkTwo() throws WTException {
        //8.0、至少一条受影响对象，必填项验证。
        if (PAGEDATAMAP.isEmpty()) {
            MESSAGES.add("受影响对象列表不能为空！");
        }

        //8.1、检查受影响对象是否存在未结束的ECN（包含的ECA非取消状态），有则不允许创建。
        for (Persistable persistable : PAGEDATAMAP.keySet()) {
            if (persistable instanceof WTPart) {
                WTPart part = (WTPart) persistable;
                LOGGER.info(">>>>>>>>>>part:" + part.getNumber());

                //获取所有大版本的最新小版本
                QueryResult queryResult = VersionControlHelper.service.allVersionsOf(part.getMaster());
                while (queryResult.hasMoreElements()) {
                    WTPart oldPart = (WTPart) queryResult.nextElement();

                    //获取对象所有关联的ECA对象
                    QueryResult result = ChangeHelper2.service.getAffectingChangeActivities(oldPart);
                    LOGGER.info(">>>>>>>>>>result size:" + result.size());
                    while (result.hasMoreElements()) {
                        WTChangeActivity2 changeActivity2 = (WTChangeActivity2) result.nextElement();
                        LOGGER.info(">>>>>>>>>>changeActivity2:" + changeActivity2.getNumber());
                        if ((!ChangeUtils.checkState(changeActivity2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeActivity2, ChangeConstants.RESOLVED))) {
                            MESSAGES.add("物料: " + part.getNumber() + " 存在未解决的ECA: " + changeActivity2.getNumber() + " 不能同时提交两个ECA！");
                        }

                        WTChangeOrder2 changeOrder2 = ChangeUtils.getEcnByEca(changeActivity2);
                        LOGGER.info(">>>>>>>>>>changeOrder2:" + changeOrder2.getNumber());
                        if (!ORDER2.getNumber().startsWith(changeOrder2.getNumber())) {
                            if ((!ChangeUtils.checkState(changeOrder2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeOrder2, ChangeConstants.RESOLVED))) {
                                MESSAGES.add("物料: " + part.getNumber() + " 存在未解决的ECN: " + changeOrder2.getNumber() + " 不能同时提交两个ECN！");
                            }
                        }
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
        if (messages.length() > 0) {
            MESSAGES.add(messages.toString() + " 状态不满足：已归档或已发布！");
        }

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
        if (messages.length() > 0) {
            MESSAGES.add(messages.toString() + " 业务定义为标准件，不能变更升版！");
        }

        //8.4、A“ECN和完成功能” ，提交的时候校验图纸是否收集，如果没有收集，要给提交人提示“xx部件未收集图纸，请收集图纸！”
        for (Map.Entry<WTPart, Collection<Persistable>> entryMap : PARTASSOCIATIONDOC.entrySet()) {
            WTPart part = entryMap.getKey();

            Set<String> numbers = new HashSet<>();
            for (Persistable persistable : entryMap.getValue()) {
                numbers.add(ModifyUtils.getNumber(persistable));
            }

            Set<String> result = PICollectionUtils.intersect(numbers, AFFECTEDDOCNUMBER);
            if (result.size() < 1) {
                MESSAGES.add("部件: " + part.getNumber() + "未收集图纸，请收集图纸！");
            }
        }
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
     * 合成错误信息
     * @return
     */
    public String compoundMessage() {
        StringBuilder builder = new StringBuilder();
        if (MESSAGES.size() > 0) {
            builder.append("无法创建变更申请，存在以下问题：").append("\n");
            int i = 1;
            for (String message : MESSAGES) {
                builder.append(i++).append(". ").append(message).append("\n");
            }
        }
        return builder.toString();
    }

}
