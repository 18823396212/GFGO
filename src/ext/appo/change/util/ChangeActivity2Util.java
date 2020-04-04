package ext.appo.change.util;

import ext.appo.change.ModifyHelper;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.models.CorrelationObjectLink;
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
import wt.org.WTUser;
import wt.part.WTPart;
import wt.preference.PreferenceUtilityForExpImp;
import wt.util.WTException;

import java.util.*;

/*
 * 8.6、根据受影响对象，及变更类型，及类型(升版)创建ECA对象（BOM变更创建多个ECA对象，图纸变更分别创建不同的ECA对象，关联不同的图纸变更对象），
 * 并ECA关联“受影响对象”，同步生成“产生的对象”。
 */
public class ChangeActivity2Util implements ChangeConstants, ModifyConstants {

    private static final Logger LOGGER = LogR.getLogger(ChangeActivity2Util.class.getName());
    private Map<Persistable, Map<String, String>> PAGEDATAMAP = new HashMap<>();//ECN受影响对象集合
    private Map<Persistable, Collection<Persistable>> CONSTRUCTRELATION = new HashMap<>();//ECA受影响对象集合
    private WTChangeOrder2 ORDER2;
    public Set<WTChangeActivity2> ACTIVITY2S = new HashSet<>();//创建的ECA集合
    public Map<String, String> BRANCHIDMAP = new HashMap<>();//受影响对象branchId、ECA ID集合

    public ChangeActivity2Util(WTChangeOrder2 changeOrder2, Map<Persistable, Map<String, String>> pageDataMap, Map<Persistable, Collection<Persistable>> constructRelation) {
        ORDER2 = changeOrder2;
        PAGEDATAMAP.putAll(pageDataMap);
        CONSTRUCTRELATION.putAll(constructRelation);
        LOGGER.info(">>>>>>>>>>ORDER2:" + ORDER2);
        LOGGER.info(">>>>>>>>>>PAGEDATAMAP:" + PAGEDATAMAP);
        LOGGER.info(">>>>>>>>>>CONSTRUCTRELATION:" + CONSTRUCTRELATION);
    }

    /**
     * 暂存按钮
     * @throws WTException
     */
    public void cacheButton() throws WTException {
        if (!PAGEDATAMAP.isEmpty()) {
            //更新受影响对象的IBA属性
            updateAttributes();
        }
    }

    /**
     * 完成按钮
     * @throws WTException
     */
    public void okButton() throws WTException {
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
                    String ecnVid = String.valueOf(PICoreHelper.service.getBranchId(ORDER2));
                    LOGGER.info(">>>>>>>>>>createChangeActivity2.ecnVid:" + ecnVid);
                    String branchId = String.valueOf(PICoreHelper.service.getBranchId(changeable2));
                    LOGGER.info(">>>>>>>>>>createChangeActivity2.branchId:" + branchId);
                    CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, branchId, LINKTYPE_1);
                    if (link != null) {
                        String routing = link.getRouting();
                        LOGGER.info(">>>>>>>>>>createChangeActivity2.routing:" + routing);
                        if (ROUTING_1.equals(routing) || ROUTING_3.equals(routing)) continue;//子流程状态为已创建、已完成跳过以下逻辑
                    }

                    // 获取用户针对每一列输入的数据
                    Map<String, String> attributesMap = PAGEDATAMAP.get(entryMap.getKey());
                    LOGGER.info(">>>>>>>>>>createChangeActivity2.changeable2:" + ModifyUtils.getNumber(changeable2) + " >>>>>>>>>>attributesMap: " + attributesMap);

                    // ECA类型
                    String type = "";
                    String flowName = "";
                    String description = "";
                    //add by lzy at 20191231 start
                    String changeObjectType = attributesMap.get(CHANGETYPE_COMPID) == null ? "" : attributesMap.get(CHANGETYPE_COMPID);//物料变更类型
//                    String changeObjectType = attributesMap.get(ATTRIBUTE_7) == null ? "" : attributesMap.get(ATTRIBUTE_7);
                    //add by lzy at 20191231 end
                    if (changeObjectType.contains(VALUE_5)) {
                        type = TYPE_1;
                        flowName = FLOWNAME_1;
                        description = FLOWNAME_3;
                    } else if (changeObjectType.contains(VALUE_6)) {
                        type = TYPE_2;
                        flowName = FLOWNAME_2;
                        description = FLOWNAME_4;
                    }
                    //游离WTDocument、EPMDocument(图纸单独走变更的场景)，创建图纸变更ECA
                    if (StringUtils.isEmpty(type) && !(changeable2 instanceof WTPart)) {
                        type = TYPE_2;
                        flowName = FLOWNAME_2;
                        description = FLOWNAME_4;
                    }
                    LOGGER.info(">>>>>>>>>>createChangeActivity2.type:" + type + " >>>>>flowName: " + flowName + " >>>>>description: " + description);
                    if (StringUtils.isEmpty(type) || StringUtils.isEmpty(flowName)) continue;

                    // 责任人
                    String assigneeName = attributesMap.get(RESPONSIBLEPERSON_COMPID);

                    // 创建ECA
                    WTChangeActivity2 eca = ModifyUtils.createChangeTask(ORDER2, ModifyUtils.getNumber(changeable2), null, null, type, assigneeName);
                    LOGGER.info(">>>>>>>>>>createChangeActivity2.eca:" + eca);
                    if (eca == null) continue;

                    // 收集需要添加的受影响对象
                    String ecaVid = PersistenceHelper.getObjectIdentifier(eca).toString();
                    LOGGER.info(">>>>>>>>>>createChangeActivity2.ecaVid:" + ecaVid);
                    Vector<Changeable2> vector = new Vector<>();
                    vector.add(changeable2);
                    for (Persistable persistable : entryMap.getValue()) {
                        if (persistable instanceof Changeable2) {
                            vector.add((Changeable2) persistable);
                            BRANCHIDMAP.put(String.valueOf(PICoreHelper.service.getBranchId(persistable)), ecaVid);
                        }
                    }
                    BRANCHIDMAP.put(branchId, ecaVid);

                    // 添加受影响对象
                    LOGGER.info(">>>>>>>>>>createChangeActivity2.vector:" + vector);
                    ModifyUtils.addAffectedActivityData(eca, vector);
                    //add by lzy at 20191231  start
                    String attributeValue = attributesMap.get(CHANGETYPE_COMPID);
//                    String attributeValue = attributesMap.get(CHANGOBJECTETYPE_COMPID);
                    //add by lzy at 20191231  end
                    // 部件「类型」选择「替换」时ECA状态设置为「已解决」不起流程、不升版、不添加到产生对象
                    if (PIStringUtils.isNotNull(attributeValue) && attributeValue.contains(VALUE_1)) {
                        eca = (WTChangeActivity2) PICoreHelper.service.setLifeCycleState(eca, RESOLVED);

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
                        eca = (WTChangeActivity2) PersistenceHelper.manager.refresh(eca);
                        ACTIVITY2S.add(eca);

                        updateAttributes(entryMap, changeObjectType);
                    }
                    //部件「类型」选择「升级」时ECA状态设置为「开启」启动子流程、修订受影响对象，并添加到产生对象列表
                    else if (PIStringUtils.isNotNull(attributeValue) && attributeValue.contains(VALUE_4)) {
                        eca = (WTChangeActivity2) PICoreHelper.service.setLifeCycleState(eca, OPEN);
                        //修订受影响对象，并添加到产生对象列表
                        //add by lzy at 20200114 start
//                        WTCollection collection = ModifyUtils.revise(vector, reviseMap);
                        WTCollection collection = ModifyUtils.revise(vector, reviseMap,eca);
                        //add by lzy at 20200114 end
                        LOGGER.info(">>>>>>>>>>createChangeActivity2.collection:" + collection);
                        ModifyUtils.AddChangeRecord2(eca, collection);


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
                        eca = (WTChangeActivity2) PersistenceHelper.manager.refresh(eca);
                        ACTIVITY2S.add(eca);

                        updateAttributes(entryMap, changeObjectType);

                        //启动ECA流程
                        ModifyUtils.startWorkflow(eca, flowName, description);
                    }
                }
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
    }

    /**
     * 设置文档、EPM文档「变更对象类型」
     * 「变更对象类型」修改为「变更类型」
     * @param entryMap
     * @param changeObjectType
     * @throws WTException
     */
    private void updateAttributes(Map.Entry<Persistable, Collection<Persistable>> entryMap, String changeObjectType) throws WTException {
        Persistable key = entryMap.getKey();
        //设置部件关联文档、EPM文档的「变更对象类型」与部件相同
        if (key instanceof WTPart) {
            //modify by xiebowen at 2019/12/25  start
            //changeObjectType = changeObjectType.replaceFirst("BOM变更;", "").replaceFirst("图纸变更;", "");
            changeObjectType = changeObjectType.replaceFirst("BOM变更升版;", "").replaceFirst("图纸变更升版;", "").replaceFirst("替换;", "");
            //modify by xiebowen at 2019/12/25  end
            Collection<Persistable> collection = entryMap.getValue();
            for (Persistable persistable : collection) {
                //add by lzy at 20191231 start
//                PIAttributeHelper.service.forceUpdateSoftAttribute(persistable, ATTRIBUTE_7, changeObjectType);
                PIAttributeHelper.service.forceUpdateSoftAttribute(persistable, CHANGETYPE_COMPID, changeObjectType);
                //add by lzy at 20191231 end
            }
        }
        //游离WTDocument、EPMDocument(图纸单独走变更的场景)设置「变更对象类型」为「图纸变更」
        else {
            PIAttributeHelper.service.forceUpdateSoftAttribute(key, ATTRIBUTE_7, VALUE_6);
        }
    }

    /**
     * 更新受影响对象的IBA属性
     * @throws WTException
     */
    private void updateAttributes() throws WTException {
        String changeItemType = ModifyUtils.getValue(ORDER2, ATTRIBUTE_8);//变更类型
        for (Map.Entry<Persistable, Map<String, String>> entryMap : PAGEDATAMAP.entrySet()) {
            Map<String, Object> attributesMap = new HashMap<>();
            for (Map.Entry<String, String> ibaEntryMap : entryMap.getValue().entrySet()) {
                String key = ibaEntryMap.getKey();
                String value = ibaEntryMap.getValue();
                LOGGER.info(">>>>>>>>>>updateAttributes.key:" + key);
                LOGGER.info(">>>>>>>>>>updateAttributes.value:" + value);

                // 过滤「受影响对象列表备注」
                if (AADDESCRIPTION_COMPID.equals(key)) continue;
                //add by lzy at 20191231 start
//                //变更对象类型
//                if (ATTRIBUTE_7.equals(key))
                //变更类型
                if (CHANGETYPE_COMPID.equals(key))
                //add by lzy at 20191231 end
                    //modify by xiebowen at 2019/12/25  start
                    //value = value.replaceFirst("BOM变更;", "").replaceFirst("图纸变更;", "");
                    value = value.replaceFirst("BOM变更升版;", "").replaceFirst("图纸变更升版;", "").replaceFirst("替换;", "");
                    //modify by xiebowen at 2019/12/25  start
//                //add by lzy at 20200401 start
//                if (RESPONSIBLEPERSON_COMPID.equals(key)){
//                    WTUser user= PreferenceUtilityForExpImp.getUser(value);
//                    if (user!=null){
//                        value=user.getFullName();
//                    }
//                }
//                //add by lzy at 20200401 end

                attributesMap.put(ibaEntryMap.getKey(), value);
            }
            attributesMap.put(ATTRIBUTE_8, changeItemType);//同步ECN属性「变更类型」到受影响对象
            LOGGER.info(">>>>>>>>>>updateAttributes.attributesMap:" + attributesMap);

            PIAttributeHelper.service.forceUpdateSoftAttributes(entryMap.getKey(), attributesMap);
        }
    }

}
