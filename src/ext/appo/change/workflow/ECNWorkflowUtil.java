package ext.appo.change.workflow;

import ext.appo.change.ModifyHelper;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.models.CorrelationObjectLink;
import ext.appo.change.util.ModifyUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.lang.PIStringUtils;
import ext.pi.core.PICoreHelper;
import ext.pi.core.PIWorkflowHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import wt.change2.AffectedActivityData;
import wt.change2.Changeable2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.*;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTSet;
import wt.log4j.LogR;
import wt.part.PartDocHelper;
import wt.part.WTPart;
import wt.project.Role;
import wt.util.WTException;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;

import java.util.*;

/**
 * ECN主流程相关处理逻辑
 */
public class ECNWorkflowUtil implements ChangeConstants, ModifyConstants {

    private static Logger LOGGER = LogR.getLogger(ECNWorkflowUtil.class.getName());
    private Set<String> MESSAGES = new HashSet<>();
    private static Set<String> WORKITEMNAME;

    static {
        WORKITEMNAME = new HashSet<>();
        WORKITEMNAME.add("审核");
        WORKITEMNAME.add("会签");
        WORKITEMNAME.add("批准");
    }

    /**
     * 查询ECN与受影响对象的Link，是否存在路由为 已驳回的数据
     * 存在则触发「修改变更申请」任务
     * @param pbo
     * @param self
     * @throws WTException
     * @return
     */
    public boolean isRejected(WTObject pbo, ObjectReference self) throws WTException {
        if (pbo instanceof WTChangeOrder2) {
            Set<CorrelationObjectLink> links = ModifyHelper.service.queryCorrelationObjectLinks((WTChangeOrder2) pbo, LINKTYPE_1, ROUTING_2);
            LOGGER.info("=====isRejected.links: " + links);
            if (links.size() > 0) return true;
        }
        return false;
    }

    /**
     * 判定是否暂存
     * @param pbo
     * @param self
     * @return
     * @throws WTException
     */
    public boolean isCache(WTObject pbo, ObjectReference self) throws WTException {
        String actionName = ModifyUtils.getValue(pbo, ATTRIBUTE_6);
        LOGGER.info("=====isCache.actionName: " + actionName);
        if (CONSTANTS_1.equals(actionName)) {
            return true;
        }
        return false;
    }

    /**
     * 2.3 当选中“取消变更”，点击完成任务时
     * 2.3.1、需要判断是否所有子流程都处于实施状态（或者已取消状态）。
     * 2.3.2、若路由选择“取消变更”，需要恢复所有变更对象到变更前版本。
     * @param pbo
     * @param self
     * @throws WTException
     */
    public void cancelRoute(WTObject pbo, ObjectReference self) throws WTException {
        if (pbo instanceof WTChangeOrder2) {
            WTChangeOrder2 changeOrder2 = (WTChangeOrder2) pbo;

            Set<WTChangeActivity2> activity2s = new HashSet<>();
            Set<WTChangeActivity2> unfinished = new HashSet<>();
            Set<CorrelationObjectLink> links = ModifyHelper.service.queryCorrelationObjectLinks(changeOrder2, LINKTYPE_1);
            LOGGER.info("=====cancelRoute.links: " + links);
            for (CorrelationObjectLink link : links) {
                Persistable persistable = ModifyUtils.getPersistable(link.getEcaIdentifier());
                LOGGER.info("=====cancelRoute.persistable: " + persistable);
                if (persistable instanceof WTChangeActivity2) {
                    WTChangeActivity2 activity2 = (WTChangeActivity2) persistable;
                    activity2s.add(activity2);

                    String routing = link.getRouting();
                    LOGGER.info("=====cancelRoute.routing: " + routing);
                    if (!ROUTING_2.equals(routing) && !ROUTING_3.equals(routing)) {
                        unfinished.add(activity2);
                    }
                }
            }
            if (unfinished.isEmpty()) {
                //获取所有需要回退版本的对象
                WTSet produces = new WTHashSet();
                for (WTChangeActivity2 activity2 : activity2s) {
                    Collection<Changeable2> collection = ModifyUtils.getChangeablesAfter(activity2);
                    LOGGER.info("=====cancelRoute.activity2: " + activity2.getNumber() + " >>>>>collection: " + collection);
                    produces.addAll(collection);//获取ECA关联的产生对象

                    ModifyUtils.removeAffectedActivityData(activity2);//移除受影响对象
                    ModifyUtils.removeChangeRecord(activity2);//移除产生对象
                    PersistenceServerHelper.manager.remove(activity2);//删除ECA
                }
                //删除修订版本
                PersistenceServerHelper.manager.remove(produces);
            } else {
                for (WTChangeActivity2 activity2 : unfinished) {
                    MESSAGES.add("变更申请「" + changeOrder2.getNumber() + "」关联的更改任务「" + activity2.getNumber() + "」未完成或未取消，不允许取消变更！");
                }
                compoundMessage();
            }
        }
    }

    /**
     * 2.4 当选中“提交”，点击完成任务时
     * 2.4.1、ECN流程正常流向。
     * 2.4.2、启动新创建的对象的ECA流程。
     * @param pbo
     * @param self
     * @throws WTException
     */
    public void submitRoute(WTObject pbo, ObjectReference self) throws WTException {
        if (pbo instanceof WTChangeOrder2) {
            WTChangeOrder2 changeOrder2 = (WTChangeOrder2) pbo;

            //收集需要创建ECA的部件、文档、CAD文档
            Map<Persistable, Collection<Persistable>> collectionMap = collectionObjectsAffected(changeOrder2);

            //创建ECA并关联受影响对象、产生对象
            createChangeActivity2(changeOrder2, collectionMap);
        }
    }

    /**
     * 收集需要创建ECA的数据
     * 「修改变更申请」节点提交删除ECA可以在这里操作
     * @param changeOrder2
     * @return
     * @throws WTException
     */
    public Map<Persistable, Collection<Persistable>> collectionObjectsAffected(WTChangeOrder2 changeOrder2) throws WTException {
        Set<WTPart> parts = new HashSet<>();//部件集合
        Map<String, Persistable> docMap = new HashMap<>();//文档集合
        Set<CorrelationObjectLink> links = ModifyHelper.service.queryCorrelationObjectLinks(changeOrder2, LINKTYPE_1);
        LOGGER.info("=====collectionObjectsAffected.links: " + links);
        for (CorrelationObjectLink link : links) {
            String routing = link.getRouting();
            LOGGER.info("=====collectionObjectsAffected.routing: " + routing);
            if (StringUtils.isEmpty(routing) || ROUTING_2.equals(routing)) {
                Persistable persistable = link.getPersistable();
                String number = ModifyUtils.getNumber(persistable);
                LOGGER.info("=====collectionObjectsAffected.persistable: " + number);

                if (persistable instanceof WTPart) {
                    parts.add((WTPart) persistable);
                } else if (persistable instanceof WTDocument || persistable instanceof EPMDocument) {
                    docMap.put(number, persistable);
                }
            }
        }
        LOGGER.info("=====collectionObjectsAffected.docMap: " + docMap);

        Map<Persistable, Collection<Persistable>> collectionMap = new HashMap<>();
        //部件与关联的图纸集合
        for (WTPart part : parts) {
            Collection<Persistable> associatedItems = new HashSet<>();//收集图文档

            //按照收集规则获取关联的所有WTDocument文档和EPMDocument文档
            QueryResult result = PartDocHelper.service.getAssociatedDocuments(part);//获取部件关联的图文档
            LOGGER.info("=====collectionObjectsAffected.part: " + part.getNumber() + " >>>>>result: " + result.size());
            while (result.hasMoreElements()) {
                Object object = result.nextElement();
                if (object instanceof ObjectReference) {
                    object = ((ObjectReference) object).getObject();
                }
                LOGGER.info("=====collectionObjectsAffected.object: " + object);
                if (object instanceof Persistable) {
                    Persistable persistable = (Persistable) object;
                    String number = ModifyUtils.getNumber(persistable);

                    LOGGER.info("=====collectionObjectsAffected.number: " + number);
                    if (docMap.containsKey(number)) {
                        associatedItems.add(persistable);
                        docMap.remove(number);
                    }
                }
            }

            collectionMap.put(part, associatedItems);
        }
        //游离的图纸（WTDocument、EPMDocument）单独走「图纸变更」ECA
        for (Persistable persistable : docMap.values()) {
            collectionMap.put(persistable, new HashSet<>());
        }
        LOGGER.info("=====collectionObjectsAffected.collectionMap: " + collectionMap);
        return collectionMap;
    }

    /**
     * 「修改变更申请」节点创建ECA
     * @param changeOrder2
     * @param collectionMap
     * @throws WTException
     */
    public void createChangeActivity2(WTChangeOrder2 changeOrder2, Map<Persistable, Collection<Persistable>> collectionMap) throws WTException {
        try {
            Map<String, Changeable2> reviseMap = new HashMap<>();//已修订对象
            for (Map.Entry<Persistable, Collection<Persistable>> entryMap : collectionMap.entrySet()) {
                if (entryMap.getKey() instanceof Changeable2) {
                    Changeable2 changeable2 = (Changeable2) entryMap.getKey();

                    // ECA类型
                    String type = "";
                    String changeObjectType = ModifyUtils.getValue(changeable2, CHANGOBJECTETYPE_COMPID);//变更对象类型
                    if (changeObjectType.contains(VALUE_5)) type = TYPE_1;
                    else if (changeObjectType.contains(VALUE_6)) type = TYPE_2;
                    //游离WTDocument、EPMDocument(图纸单独走变更的场景)，创建图纸变更ECA
                    if (StringUtils.isEmpty(type) && !(changeable2 instanceof WTPart)) type = TYPE_2;
                    LOGGER.info(">>>>>>>>>>createChangeActivity2.type:" + type);
                    if (StringUtils.isEmpty(type)) continue;

                    // 责任人
                    String assigneeName = ModifyUtils.getValue(changeable2, RESPONSIBLEPERSON_COMPID);

                    // 创建ECA
                    WTChangeActivity2 eca = ModifyUtils.createChangeTask(changeOrder2, ModifyUtils.getNumber(changeable2), null, null, type, assigneeName);
                    LOGGER.info(">>>>>>>>>>createChangeActivity2.eca:" + eca);
                    if (eca == null) continue;

                    // 收集需要添加的受影响对象
                    Vector<Changeable2> vector = new Vector<>();
                    vector.add(changeable2);
                    for (Persistable persistable : entryMap.getValue()) {
                        if (persistable instanceof Changeable2) {
                            vector.add((Changeable2) persistable);
                        }
                    }

                    // 添加受影响对象
                    LOGGER.info(">>>>>>>>>>createChangeActivity2.vector:" + vector);
                    ModifyUtils.addAffectedActivityData(eca, vector);

                    //修订受影响对象，并添加到产生对象列表
                    WTCollection collection = ModifyUtils.revise(vector, reviseMap);
                    LOGGER.info(">>>>>>>>>>createChangeActivity2.collection:" + collection);
                    ModifyUtils.AddChangeRecord2(eca, collection);

                    // 部件‘类型’选择‘替换’时ECA状态设置为‘已发布’,选择‘升级’时ECA状态设置为‘开启’
                    String attributeValue = ModifyUtils.getValue(changeable2, CHANGETYPE_COMPID);
                    if (PIStringUtils.isNotNull(attributeValue) && attributeValue.contains(VALUE_1)) {
                        eca = (WTChangeActivity2) PICoreHelper.service.setLifeCycleState(eca, RESOLVED);
                    } else if (PIStringUtils.isNotNull(attributeValue) && attributeValue.contains(VALUE_4)) {
                        eca = (WTChangeActivity2) PICoreHelper.service.setLifeCycleState(eca, OPEN);
                    }

                    // 期望完成日期
                    eca = ModifyUtils.updateNeedDate(eca, ModifyUtils.getValue(changeable2, ChangeConstants.COMPLETIONTIME_COMPID));

                    String ecnVid = String.valueOf(PICoreHelper.service.getBranchId(changeOrder2));
                    LOGGER.info(">>>>>>>>>>createChangeActivity2.ecnVid:" + ecnVid);
                    String branchId = String.valueOf(PICoreHelper.service.getBranchId(changeable2));
                    LOGGER.info(">>>>>>>>>>createChangeActivity2.branchId:" + branchId);
                    CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, branchId, LINKTYPE_1);
                    if (link != null) {
                        // 更新备注
                        String aadDescription = link.getAadDescription();
                        AffectedActivityData affectedActivityData = ModifyUtils.getAffectedActivity(eca, changeable2);
                        if (affectedActivityData != null) {
                            affectedActivityData.setDescription(aadDescription);
                            PersistenceHelper.manager.save(affectedActivityData);
                        }

                        // 更新部件关联的说明文档及图纸备注
                        for (Persistable persistable : entryMap.getValue()) {
                            AffectedActivityData activityData = ModifyUtils.getAffectedActivity(eca, (Changeable2) persistable);
                            if (activityData != null) {
                                activityData.setDescription(aadDescription);
                                PersistenceHelper.manager.save(activityData);
                            }
                        }

                        String ecaVid = PersistenceHelper.getObjectIdentifier(eca).toString();
                        LOGGER.info(">>>>>>>>>>createChangeActivity2.ecaVid:" + ecaVid);
                        link.setEcaIdentifier(ecaVid);
                        link.setRouting(ROUTING_1);
                        PersistenceServerHelper.manager.update(link);
                    }
                }
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
    }

    /**
     * 收集子流程会签者、审核者、批准者到ECN流程接收者
     * @param pbo
     * @param self
     * @throws WTException
     */
    public void collectionNoticeMember(WTObject pbo, ObjectReference self) throws WTException {
        if (pbo instanceof WTChangeOrder2) {
            WTChangeOrder2 changeOrder2 = (WTChangeOrder2) pbo;
            WfProcess destProcess = PIWorkflowHelper.service.getProcess(self);
            LOGGER.info(">>>>>>>>>>collectionNoticeMember.destProcess:" + destProcess);

            Collection<WTChangeActivity2> collection = ModifyUtils.getChangeActivities(changeOrder2);
            LOGGER.info(">>>>>>>>>>collectionNoticeMember.collection:" + collection);
            for (WTChangeActivity2 activity2 : collection) {
                QueryResult result = WfEngineHelper.service.getAssociatedProcesses(activity2, null, null);
                LOGGER.info(">>>>>>>>>>collectionNoticeMember.activity2:" + activity2.getNumber() + " >>>>>result: " + result.size());
                while (result.hasMoreElements()) {
                    WfProcess sourceProcess = (WfProcess) result.nextElement();
                    LOGGER.info(">>>>>>>>>>collectionNoticeMember.sourceProcess:" + sourceProcess);

                    //检查任务节点角色是否根据Excel配置改变
                    /*Set<Role> roles = new HashSet<>();
                    QueryResult workItems = PIWorkflowHelper.service.findWorkItems(sourceProcess, true);
                    while (workItems.hasMoreElements()) {
                        WorkItem workItem = (WorkItem) workItems.nextElement();
                        WfAssignedActivity activity = (WfAssignedActivity) workItem.getSource().getObject();
                        String name = activity.getName();
                        if (WORKITEMNAME.contains(name)) {
                            roles.add(workItem.getRole());
                        }
                    }

                    for (Role role : roles) {
                        PIWorkflowHelper.service.copyPrincipals(sourceProcess, role, destProcess, Role.toRole(ROLE_1), false);
                    }*/

                    PIWorkflowHelper.service.copyPrincipals(sourceProcess, Role.toRole(ROLE_2), destProcess, Role.toRole(ROLE_1), false);//会签者
                    PIWorkflowHelper.service.copyPrincipals(sourceProcess, Role.toRole(ROLE_3), destProcess, Role.toRole(ROLE_1), false);//审核者
                    PIWorkflowHelper.service.copyPrincipals(sourceProcess, Role.toRole(ROLE_4), destProcess, Role.toRole(ROLE_1), false);//批准者
                }
            }
        }
    }

    /**
     * 合成错误信息
     * @return
     */
    public void compoundMessage() throws WTException {
        StringBuilder builder = new StringBuilder();
        if (MESSAGES.size() > 0) {
            builder.append("无法提交流程，存在以下问题：").append("\n");
            int i = 1;
            for (String message : MESSAGES) {
                builder.append(i++).append(". ").append(message).append("\n");
            }
        }

        if (builder.length() > 0) {
            throw new WTException(builder.toString());
        }
    }

}