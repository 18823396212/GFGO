package ext.appo.change.util;

import ext.appo.change.constants.ModifyConstants;
import ext.appo.ecn.constants.ChangeConstants;
import ext.lang.PIStringUtils;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PICoreHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import wt.change2.AffectedActivityData;
import wt.change2.Changeable2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.collections.WTCollection;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.util.WTException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/*
 * 8.6、根据受影响对象，及变更类型，及类型(升版)创建ECA对象（BOM变更创建多个ECA对象，图纸变更分别创建不同的ECA对象，关联不同的图纸变更对象），
 * 并ECA关联“受影响对象”，同步生成“产生的对象”。
 */
public class ChangeActivity2Util implements ChangeConstants, ModifyConstants {

    private static final Logger LOGGER = LogR.getLogger(ChangeActivity2Util.class.getName());
    private Map<Persistable, Map<String, String>> PAGEDATAMAP = new HashMap<>();//ECN受影响对象集合
    private Map<Persistable, Collection<Persistable>> CONSTRUCTRELATION = new HashMap<>();//ECA受影响对象集合
    private WTChangeOrder2 ORDER2;

    public ChangeActivity2Util(WTChangeOrder2 changeOrder2, Map<Persistable, Map<String, String>> pageDataMap, Map<Persistable, Collection<Persistable>> constructRelation) throws WTException {
        ORDER2 = changeOrder2;
        PAGEDATAMAP.putAll(pageDataMap);
        CONSTRUCTRELATION.putAll(constructRelation);
        LOGGER.info(">>>>>>>>>>ORDER2:" + ORDER2);
        LOGGER.info(">>>>>>>>>>PAGEDATAMAP:" + PAGEDATAMAP);
        LOGGER.info(">>>>>>>>>>CONSTRUCTRELATION:" + CONSTRUCTRELATION);

        if (ORDER2 != null && !PAGEDATAMAP.isEmpty() && !CONSTRUCTRELATION.isEmpty()) {
            //更新受影响对象的IBA属性
            updateAttributes();
            /*
             * 8.6、根据受影响对象，及变更类型，及类型(升版)创建ECA对象（BOM变更创建多个ECA对象，图纸变更分别创建不同的ECA对象，关联不同的图纸变更对象），
             * 并ECA关联“受影响对象”，同步生成“产生的对象”。
             */
            createChangeActivity2();
        }
    }

    /**
     * 8.6、根据受影响对象，及变更类型，及类型(升版)创建ECA对象（BOM变更创建多个ECA对象，图纸变更分别创建不同的ECA对象，关联不同的图纸变更对象），
     * 并ECA关联“受影响对象”，同步生成“产生的对象”。
     * @throws WTException
     */
    private void createChangeActivity2() throws WTException {
        try {
            Map<String, Changeable2> reviseMap = new HashMap<>();//已修订对象
            for (Map.Entry<Persistable, Collection<Persistable>> entryMap : CONSTRUCTRELATION.entrySet()) {
                if (entryMap.getKey() instanceof Changeable2) {
                    Changeable2 changeable2 = (Changeable2) entryMap.getKey();

                    // 获取用户针对每一列输入的数据
                    Map<String, String> attributesMap = PAGEDATAMAP.get(entryMap.getKey());
                    LOGGER.info(">>>>>>>>>>createChangeActivity2.changeable2:" + ModifyUtils.getNumber(changeable2) + " >>>>>>>>>>attributesMap: " + attributesMap);

                    // ECA类型
                    String type = "";
                    String changeObjectType = attributesMap.get(CHANGOBJECTETYPE_COMPID);//变更对象类型
                    if (VALUE_5.equals(changeObjectType)) type = TYPE_1;
                    else if (VALUE_6.equals(changeObjectType)) type = TYPE_2;
                    //游离WTDocument、EPMDocument(图纸单独走变更的场景)，创建图纸变更ECA
                    if (StringUtils.isEmpty(type) && !(changeable2 instanceof WTPart)) type = TYPE_2;
                    LOGGER.info(">>>>>>>>>>createChangeActivity2.type:" + type);
                    if (StringUtils.isEmpty(type)) continue;

                    // 责任人
                    String assigneeName = attributesMap.get(RESPONSIBLEPERSON_COMPID);

                    // 创建ECA
                    WTChangeActivity2 eca = ModifyUtils.createChangeTask(ORDER2, ModifyUtils.getNumber(changeable2), null, null, type, assigneeName);
                    LOGGER.info(">>>>>>>>>>createChangeActivity2.eca:" + eca);
                    if (eca == null) continue;

                    // 收集需要添加的受影响对象
                    Vector<Changeable2> vector = new Vector<>();
                    vector.add(changeable2);
                    for (Persistable persistable : entryMap.getValue()) {
                        if (persistable instanceof Changeable2) vector.add((Changeable2) persistable);
                    }

                    //修订受影响对象，并添加到产生对象列表
                    WTCollection collection = ModifyUtils.revise(vector, reviseMap);
                    LOGGER.info(">>>>>>>>>>createChangeActivity2.collection:" + collection);
                    ModifyUtils.AddChangeRecord2(eca, collection);


                    // 添加受影响对象
                    LOGGER.info(">>>>>>>>>>createChangeActivity2.vector:" + vector);
                    ModifyUtils.addAffectedActivityData(eca, vector);

                    // 部件‘类型’选择‘替换’时ECA状态设置为‘已发布’,选择‘升级’时ECA状态设置为‘开启’
                    String attributeValue = attributesMap.get(CHANGETYPE_COMPID);
                    if (PIStringUtils.isNotNull(attributeValue) && attributeValue.contains(VALUE_1)) {
                        eca = (WTChangeActivity2) PICoreHelper.service.setLifeCycleState(eca, RESOLVED);
                    } else if (PIStringUtils.isNotNull(attributeValue) && attributeValue.contains(VALUE_4)) {
                        eca = (WTChangeActivity2) PICoreHelper.service.setLifeCycleState(eca, OPEN);
                    }

                    // 期望完成日期
                    if (attributesMap.containsKey(ChangeConstants.COMPLETIONTIME_COMPID)) {
                        eca = ModifyUtils.updateNeedDate(eca, attributesMap.get(ChangeConstants.COMPLETIONTIME_COMPID));
                    }
                    // 更新备注
                    if (attributesMap.containsKey(AADDESCRIPTION_COMPID)) {
                        AffectedActivityData affectedActivityData = ModifyUtils.getAffectedActivity(eca, changeable2);
                        if (affectedActivityData != null) {
                            affectedActivityData.setDescription(attributesMap.get(AADDESCRIPTION_COMPID));
                            PersistenceHelper.manager.save(affectedActivityData);
                        }
                    }
                    // 更新部件关联的说明文档及图纸备注
                    for (Persistable persistable : entryMap.getValue()) {
                        attributesMap = PAGEDATAMAP.get(persistable);
                        if (attributesMap.containsKey(AADDESCRIPTION_COMPID)) {
                            AffectedActivityData affectedActivityData = ModifyUtils.getAffectedActivity(eca, (Changeable2) persistable);
                            if (affectedActivityData != null) {
                                affectedActivityData.setDescription(attributesMap.get(AADDESCRIPTION_COMPID));
                                PersistenceHelper.manager.save(affectedActivityData);
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
     * 更新受影响对象的IBA属性
     * @throws WTException
     */
    private void updateAttributes() throws WTException {
        for (Map.Entry<Persistable, Map<String, String>> entryMap : PAGEDATAMAP.entrySet()) {
            Map<String, Object> attributesMap = new HashMap<>();
            for (Map.Entry<String, String> ibaEntryMap : entryMap.getValue().entrySet()) {
                // 过滤「受影响对象列表备注」
                if (ibaEntryMap.getKey().equals(AADDESCRIPTION_COMPID)) continue;
                attributesMap.put(ibaEntryMap.getKey(), ibaEntryMap.getValue());
            }
            PIAttributeHelper.service.forceUpdateSoftAttributes(entryMap.getKey(), attributesMap);
        }
    }

}
