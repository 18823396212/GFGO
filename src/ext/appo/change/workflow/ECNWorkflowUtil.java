package ext.appo.change.workflow;

import com.ptc.windchill.enterprise.team.server.TeamCCHelper;
import com.ptc.windchill.pdmlink.change.server.impl.WorkflowProcessHelper;
import ext.appo.change.ModifyHelper;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.models.CorrelationObjectLink;
import ext.appo.change.models.TransactionTask;
import ext.appo.change.util.ModifyUtils;
import ext.appo.ecn.common.util.ChangePartQueryUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.part.workflow.PartWorkflowUtil;
import ext.com.workflow.WorkflowUtil;
import ext.lang.PIStringUtils;
import ext.pi.PIException;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIClassificationHelper;
import ext.pi.core.PICoreHelper;
import ext.pi.core.PIWorkflowHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import wt.change2.*;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.*;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTHashSet;
import wt.inf.container.WTContainer;
import wt.inf.team.ContainerTeam;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.team.ContainerTeamManaged;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.PartDocHelper;
import wt.part.WTPart;
import wt.pom.Transaction;
import wt.project.Role;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.sandbox.SandboxHelper;
import wt.session.SessionServerHelper;
import wt.team.Team;
import wt.team.WTRoleHolder2;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;
import wt.vc.views.View;
import wt.vc.views.ViewHelper;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfEngineServerHelper;
import wt.workflow.engine.WfProcess;

import java.sql.Timestamp;
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
     * 同步ECA状态-已解决表达式逻辑
     * @param pbo
     * @param self
     * @return
     * @throws WTException
     */
    public String syncExpression(WTObject pbo, ObjectReference self) throws Exception {
        String routing = "OK";

        if (pbo instanceof WTChangeOrder2) {
            //所有ECA都是已解决,已取消状态
            if (WorkflowProcessHelper.isRelatedChildrenInStates(pbo, new String[]{RESOLVED, CANCELLED}, new String[]{TYPE_1, TYPE_2})) {
                Set<CorrelationObjectLink> links = ModifyHelper.service.queryCorrelationObjectLinks((WTChangeOrder2) pbo, LINKTYPE_1);
                LOGGER.info("=====syncExpression.links: " + links);
                for (CorrelationObjectLink link : links) {
                    Persistable persistable = ModifyUtils.getPersistable(link.getEcaIdentifier());
                    if (!isReplace((WTChangeActivity2) persistable)) {
                        //存在路由不是「已完成」继续等待
                        if (!ROUTING_3.equals(link.getRouting())) {
                            routing = "WAIT";
                            break;
                        }
                    }
                }
            }
            //存在ECA不是「已解决」,已取消状态，继续等待
            else {
                routing = "WAIT";
            }
        }

        return routing;
    }

    /**
     * 2.2.1 若已经产生“修订对象”，且“修订对象”的状态为“正在工作”或“重新工作”才可以删除“受影响对象”，且受影响对象需要恢复到变更前的版本，
     * 2.2.2 删除的“受影响对象”关联的ECA及ECN中的“受影响的对象”“产生的对象”列表需要同步处理。
     * 2.2.3 删除的“受影响对象”的收集图纸对象需要同步处理。
     * 2.2.4 若ECA中无受影响的对象，需要删除当前的ECA对象及流程
     * 驳回处理（新增）：保留ECA，状态更改为已取消，ECA流程结束，产生的对象还原，ECA跟受影响对象关系解除。
     * @param pbo
     * @param self
     * @throws WTException
     */
    public void rejectChangeRequest(WTObject pbo, ObjectReference self) throws Exception {
        if (pbo instanceof WTChangeOrder2) {
            boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
            Transaction tx = null;
            try {
                tx = new Transaction();
                tx.start();

                boolean flog = false;
                WTChangeOrder2 changeOrder2 = (WTChangeOrder2) pbo;
                //获取ECN关联的ECA
                Collection<WTChangeActivity2> activity2s = ModifyUtils.getChangeActivities(changeOrder2);
                for (WTChangeActivity2 activity2 : activity2s) {
                    //判断ECA的状态是否为「已取消」
                    if (CANCELLED.equals(activity2.getState().toString())) {
                        //更新Link的路由为「已驳回」并清空ECA的Id
                        if (PICoreHelper.service.isTypeOrSubType(activity2, TYPE_3)) {
                            TransactionTask task = ModifyHelper.service.queryTransactionTask(changeOrder2, activity2, "");
                            ModifyHelper.service.updateTransactionTask(task, "");
                            CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(changeOrder2, task);
                            if (link != null) {
                                ModifyHelper.service.updateCorrelationObjectLink(link, link.getEcaIdentifier(), link.getAadDescription(), ROUTING_2);
                            }
                        } else {
                            Set<CorrelationObjectLink> links = ModifyHelper.service.queryCorrelationObjectLinks(activity2, LINKTYPE_1);
                            for (CorrelationObjectLink link : links) {
                                ModifyHelper.service.updateCorrelationObjectLink(link, "", link.getAadDescription(), ROUTING_2);
                            }
                        }
                        //获取所有需要回退版本的对象
                        Collection<Persistable> collection = ModifyUtils.getRollbackObject(activity2);//获取ECA关联的产生对象
                        LOGGER.info("=====syncExpression.activity2: " + activity2.getNumber() + " >>>>>collection: " + collection);
                        //移除受影响对象
                        ModifyUtils.removeAffectedActivityData(activity2);
                        //移除产生对象
                        ModifyUtils.removeChangeRecord(activity2);
                        //删除进程
                        QueryResult result = WfEngineHelper.service.getAssociatedProcesses(activity2, null, null);
                        LOGGER.info(">>>>>>>>>>syncExpression.activity2:" + activity2.getNumber() + " >>>>>result: " + result.size());
                        while (result.hasMoreElements()) {
                            WfProcess process = (WfProcess) result.nextElement();
                            LOGGER.info(">>>>>>>>>>syncExpression.process:" + process);
                            //add by lzy at 20191207 start
                            //终止进程,不删除进程，不删除ECA
                            PIWorkflowHelper.service.stop(process);
                            //PersistenceServerHelper.manager.remove(process);
                        }
                        //删除ECA
                        //PersistenceServerHelper.manager.remove(activity2);
                        //设置ECA状态为-已取消
                        State state = State.toState(CANCELLED);
                        LifeCycleHelper.service.setLifeCycleState(activity2, state);
                        //add by lzy at 20191207 end
                        //删除修订版本
                        SandboxHelper.service.removeObjects(new WTHashSet(collection));
                        flog = true;
                    }
                }
                if (!flog) {
                    //其他驳回情况处理
                    String ecnVid = String.valueOf(PICoreHelper.service.getBranchId(changeOrder2));
                    LOGGER.info(">>>>>>>>>>syncExpression.ecnVid:" + ecnVid);
                    CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, CONSTANTS_7, CONSTANTS_7);
                    if (null != link && ROUTING_2.equals(link.getRouting())) {
                        flog = true;
                    }
                }
                if (flog) {
                    WfEngineServerHelper.service.emitCustomObjectEvent(CONSTANTS_3, changeOrder2, new Hashtable());
                }

                tx.commit();
                tx = null;
            } finally {
                if (tx != null) {
                    tx.rollback();
                }
                SessionServerHelper.manager.setAccessEnforced(flag);
            }
        }
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
            LOGGER.info("=====isRejected.links1: " + links);
            System.out.println("=====isRejected.links1: " + links);
            if (links.size() > 0) return true;
            links = ModifyHelper.service.queryCorrelationObjectLinks((WTChangeOrder2) pbo, LINKTYPE_3, ROUTING_2);
            LOGGER.info("=====isRejected.links2: " + links);
            System.out.println("=====isRejected.links2: " + links);
            if (links.size() > 0) return true;
            links = ModifyHelper.service.queryCorrelationObjectLinks((WTChangeOrder2) pbo, CONSTANTS_7, ROUTING_2);//其他情况驳回判定
            LOGGER.info("=====isRejected.links3: " + links);
            System.out.println("=====isRejected.links3: " + links);
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
        if (CONSTANTS_1.equals(actionName)||CONSTANTS_12.equals(actionName)) {
            return true;
        }
        return false;
    }

    /**
     * 2.3 当选中“取消变更”，点击完成任务时
     * 2.3.1、需要判断是否所有子流程都处于实施状态（或者已取消状态）。（修改：子流程存在已完成状态也不能取消变更）
     * 2.3.2、若路由选择“取消变更”，需要恢复所有变更对象到变更前版本。
     * @param pbo
     * @param self
     * @throws WTException
     */
    public void cancelRoute(WTObject pbo, ObjectReference self) throws Exception {
        if (pbo instanceof WTChangeOrder2) {
            WTChangeOrder2 changeOrder2 = (WTChangeOrder2) pbo;

            Set<WTChangeActivity2> activity2s = new HashSet<>();
            Set<WTChangeActivity2> unfinished = new HashSet<>();
            Set<CorrelationObjectLink> links = ModifyHelper.service.queryCorrelationObjectLinks(changeOrder2, LINKTYPE_1);
            LOGGER.info("=====cancelRoute.links: " + links);
            link:for (CorrelationObjectLink link : links) {
                Persistable persistable = ModifyUtils.getPersistable(link.getEcaIdentifier());
                LOGGER.info("=====cancelRoute.persistable: " + persistable);
                if (persistable instanceof WTChangeActivity2) {
                    WTChangeActivity2 activity2 = (WTChangeActivity2) persistable;
                    activity2s.add(activity2);

                    String routing = link.getRouting();
                    LOGGER.info("=====cancelRoute.routing: " + routing);
                    //add by lzy at 20200103 start
                    //替换类型不需判断路由
                    Collection<Changeable2> befores = ModifyUtils.getChangeablesBefore(activity2);
                    for (Changeable2 before : befores) {
                        if (before instanceof WTPart) {
                            String value = ModifyUtils.getValue(before, CHANGETYPE_COMPID);//获取物料变更类型
                            String[] str = value.split(";");
                            if (str.length > 1) {
                                value = str[1];
                            }
                            if (value.equals("替换")) {
                                continue link;
                            }
                        }
                    }
                     //add by lzy at 20200103 start
                    if (!ROUTING_2.equals(routing)) {
                        unfinished.add(activity2);
                    }
                    //add by lzy at 2200103 end
//                    if (!ROUTING_2.equals(routing) && !ROUTING_3.equals(routing)) {
//                        unfinished.add(activity2);
//                    }
                }
            }
            if (unfinished.isEmpty()) {
                for (WTChangeActivity2 activity2 : activity2s) {
                    //获取所有需要回退版本的对象
                    Collection<Persistable> collection = ModifyUtils.getRollbackObject(activity2);//获取ECA关联的产生对象
                    LOGGER.info("=====syncExpression.activity2: " + activity2.getNumber() + " >>>>>collection: " + collection);
                    //移除受影响对象
                    ModifyUtils.removeAffectedActivityData(activity2);
                    //移除产生对象
                    ModifyUtils.removeChangeRecord(activity2);
                    //删除进程
                    QueryResult result = WfEngineHelper.service.getAssociatedProcesses(activity2, null, null);
                    LOGGER.info(">>>>>>>>>>syncExpression.activity2:" + activity2.getNumber() + " >>>>>result: " + result.size());
                    while (result.hasMoreElements()) {
                        WfProcess process = (WfProcess) result.nextElement();
                        LOGGER.info(">>>>>>>>>>syncExpression.process:" + process);
                        PersistenceServerHelper.manager.remove(process);
                    }
                    //删除ECA
                    PersistenceServerHelper.manager.remove(activity2);
                    //删除修订版本
                    SandboxHelper.service.removeObjects(new WTHashSet(collection));
                }
                //add by lzy at 20200114 start
                Set<CorrelationObjectLink> transactionLinks = ModifyHelper.service.queryCorrelationObjectLinks(changeOrder2, LINKTYPE_3);
                for (CorrelationObjectLink link : transactionLinks) {
                    Persistable persistable = ModifyUtils.getPersistable(link.getEcaIdentifier());
                    LOGGER.info("=====cancelRoute.persistable: " + persistable);
                    if (persistable instanceof WTChangeActivity2) {
                        WTChangeActivity2 activity2 = (WTChangeActivity2) persistable;
                        //终止进程
                        QueryResult result = WfEngineHelper.service.getAssociatedProcesses(activity2, null, null);
                        while (result.hasMoreElements()) {
                            WfProcess process = (WfProcess) result.nextElement();
                            LOGGER.info(">>>>>>>>>>process:" + process);
                            //终止进程
                            PIWorkflowHelper.service.stop(process);
                        }
                        PersistenceServerHelper.manager.remove(activity2);
                    }
                }
                //add by lzy at 20200114 end
            } else {
                for (WTChangeActivity2 activity2 : unfinished) {
                    MESSAGES.add("变更申请「" + changeOrder2.getNumber() + "」关联的更改任务「" + activity2.getNumber() + "」已完成或未完成或未取消，不允许取消变更！");
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
    public void submitRoute(WTObject pbo, ObjectReference self) throws Exception {
        if (pbo instanceof WTChangeOrder2) {
            WTChangeOrder2 changeOrder2 = (WTChangeOrder2) pbo;

            //收集需要创建ECA的部件、文档、CAD文档
            Map<Persistable, Collection<Persistable>> collectionMap = collectionObjectsAffected(changeOrder2);

            //检查是否存在独立进行变更的说明文档
            if (MESSAGES.size() > 0) compoundMessage();

            //创建ECA并关联受影响对象、产生对象
            createChangeActivity2(changeOrder2, collectionMap);

            //创建事务性ECA
            createTransactionECA(changeOrder2);

            //add by lzy at 20191230 start
            //删除other路由link
            setOtherRouting(changeOrder2);
            //add by lzy at 20191230 end
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
            if (persistable instanceof WTDocument) {
                WTDocument document = (WTDocument) persistable;
                if (!PartDocHelper.isReferenceDocument(document)) {
                    MESSAGES.add(document.getDisplayIdentity() + " 说明文档不能独立进行变更！");
                }
            }
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
                    String flowName = "";
                    String description = "";
                    //add by lzy at 20191231 start
//                    String changeObjectType = ModifyUtils.getValue(changeable2, ATTRIBUTE_7);//变更对象类型
                    String changeObjectType = ModifyUtils.getValue(changeable2, CHANGETYPE_COMPID);//物料变更类型
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
                    //add by lzy at 20191229 start
                    else if (changeObjectType.contains(VALUE_1)) {
                        type = TYPE_1;
                        flowName = FLOWNAME_1;
                        description = FLOWNAME_3;
                    }
                    //add by lzy at 20191229 end
                    //游离WTDocument、EPMDocument(图纸单独走变更的场景)，创建图纸变更ECA
                    if (StringUtils.isEmpty(type) && !(changeable2 instanceof WTPart)) {
                        type = TYPE_2;
                        flowName = FLOWNAME_2;
                        description = FLOWNAME_4;
                    }
                    LOGGER.info(">>>>>>>>>>createChangeActivity2.type:" + type + " >>>>>flowName: " + flowName + " >>>>>description: " + description);
                    if (StringUtils.isEmpty(type) || StringUtils.isEmpty(flowName)) continue;

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

                    // 期望完成日期
                    eca = ModifyUtils.updateNeedDate(eca, ModifyUtils.getValue(changeable2, ChangeConstants.COMPLETIONTIME_COMPID));

                    String ecnVid = String.valueOf(PICoreHelper.service.getBranchId(changeOrder2));
                    LOGGER.info(">>>>>>>>>>createChangeActivity2.ecnVid:" + ecnVid);

                    //更新受影响对象描述，受影响对象Link路由、ECA ID
                    updateDescription(ecnVid, eca, changeable2);
                    //更新部件关联的说明文档及图纸受影响对象描述，受影响对象Link路由、ECA ID
                    for (Persistable persistable : entryMap.getValue()) {
                        if (persistable instanceof Changeable2) {
                            updateDescription(ecnVid, eca, (Changeable2) persistable);
                        }
                    }

//                    // 部件‘类型’选择‘替换’时ECA状态设置为‘已发布’,选择‘升级’时ECA状态设置为‘开启’
//                    String attributeValue = ModifyUtils.getValue(changeable2, CHANGETYPE_COMPID);
//                    if (PIStringUtils.isNotNull(attributeValue) && attributeValue.contains(VALUE_1)) {
//                        eca = (WTChangeActivity2) PICoreHelper.service.setLifeCycleState(eca, RESOLVED);
//                    } else if (PIStringUtils.isNotNull(attributeValue) && attributeValue.contains(VALUE_4)) {
//                        eca = (WTChangeActivity2) PICoreHelper.service.setLifeCycleState(eca, OPEN);
//                    }

                    // 部件‘类型’选择‘替换’时ECA状态设置为‘已解决’,不启动ECA,选择‘升级’时ECA状态设置为‘开启’
                    //add by lzy at 20191231 start
                    String attributeValue = ModifyUtils.getValue(changeable2, CHANGETYPE_COMPID);
//                    String attributeValue = ModifyUtils.getValue(changeable2, CHANGOBJECTETYPE_COMPID);
                    //add by lzy at 20191231  end
                    if (PIStringUtils.isNotNull(attributeValue) && attributeValue.contains(VALUE_1)) {
                        PICoreHelper.service.setLifeCycleState(eca, RESOLVED);
//                        add by lzy at 20191209 start
                    } else if (PIStringUtils.isNotNull(attributeValue) && attributeValue.contains(VALUE_4)) {
//                    } else if (PIStringUtils.isNotNull(attributeValue) && (attributeValue.contains(VALUE_7)||attributeValue.contains(VALUE_8))) {
//                        add by lzy at 20191209 end

                        //修订受影响对象，并添加到产生对象列表
                        //add by lzy at 20200114 start
//                        WTCollection collection = ModifyUtils.revise(vector, reviseMap);
                        WTCollection collection = ModifyUtils.revise(vector, reviseMap,eca);
                        //add by lzy at 20200114 end
                        LOGGER.info(">>>>>>>>>>createChangeActivity2.collection:" + collection);
                        ModifyUtils.AddChangeRecord2(eca, collection);

                        eca = (WTChangeActivity2) PICoreHelper.service.setLifeCycleState(eca, OPEN);
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
     * 创建事务性任务
     * @param changeOrder2
     * @throws WTException
     */
    public void createTransactionECA(WTChangeOrder2 changeOrder2) throws WTException {
        try {
            Set<Persistable> links = ModifyHelper.service.queryPersistable(changeOrder2, LINKTYPE_3);
            LOGGER.info("=====createTransactionECA.links: " + links);
            for (Persistable persistable : links) {
                LOGGER.info("=====createTransactionECA.persistable: " + persistable);
                if (persistable instanceof TransactionTask) {
                    TransactionTask task = (TransactionTask) persistable;

                    String changeTheme = task.getChangeTheme();//变更主题
                    String changeDescribe = task.getChangeDescribe();//变更任务描述
                    String responsible = task.getResponsible();//责任人
                    String needDate = task.getNeedDate();//期望完成日期
                    String changeActivity2 = task.getChangeActivity2();//ECA
                    LOGGER.info("createTransactionECA>>>>>>>>>>changeTheme: " + changeTheme + " changeDescribe: " + changeDescribe + " responsible: " + responsible + " needDate: " + needDate + " changeActivity2: " + changeActivity2);

                    WTChangeActivity2 activity2 = null;
                    if (PIStringUtils.isNotNull(changeActivity2)) {
                        activity2 = (WTChangeActivity2) (new ReferenceFactory()).getReference(changeActivity2).getObject();
                    }

                    if (activity2 == null) {
                        activity2 = ModifyUtils.createChangeTask(changeOrder2, changeTheme, null, changeDescribe, TYPE_3, responsible);
                        ModifyHelper.service.updateTransactionTask(task, PersistenceHelper.getObjectIdentifier(activity2).toString());
                    } else {
                        // 名称修改
                        if (!activity2.getName().equals(changeTheme)) {
                            ModifyUtils.setChangeActivity2Name(activity2, changeTheme);
                        }
                        // 工作负责人修改
                        ModifyUtils.setChangeActivity2Assignee(activity2, responsible);
                        // 说明修改
                        if (!changeDescribe.equals(activity2.getDescription())) {
                            activity2.setDescription(changeDescribe);
                            activity2 = (WTChangeActivity2) PersistenceHelper.manager.save(activity2);
                        }
                    }
                    // 期望完成日期
                    if (PIStringUtils.isNotNull(needDate)) {
                        ModifyUtils.updateNeedDate(activity2, needDate);
                    }
                    //更新路由为已创建
                    String ecnVid = String.valueOf(changeOrder2.getBranchIdentifier());
                    String taskOid = String.valueOf(task.getPersistInfo().getObjectIdentifier().getId());
                    CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, taskOid, LINKTYPE_3);
                    link.setRouting(ROUTING_1);
                    PersistenceServerHelper.manager.update(link);
                }
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
    }

    /**
     * 更新受影响对象描述，受影响对象Link路由、ECA ID
     * @param ecnVid
     * @param eca
     * @param changeable2
     * @throws Exception
     */
    public void updateDescription(String ecnVid, WTChangeActivity2 eca, Changeable2 changeable2) throws Exception {
        String branchId = String.valueOf(PICoreHelper.service.getBranchId(changeable2));
        LOGGER.info(">>>>>>>>>>createChangeActivity2.branchId:" + branchId);
        CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, branchId, LINKTYPE_1);
        if (link != null) {
            // 更新备注
            String aadDescription = link.getAadDescription();
            AffectedActivityData activityData = ModifyUtils.getAffectedActivity(eca, changeable2);
            if (activityData != null) {
                activityData.setDescription(aadDescription);
                PersistenceHelper.manager.save(activityData);
            }

            String ecaVid = PersistenceHelper.getObjectIdentifier(eca).toString();
            LOGGER.info(">>>>>>>>>>createChangeActivity2.ecaVid:" + ecaVid);
            link.setEcaIdentifier(ecaVid);
            link.setRouting(ROUTING_1);
            PersistenceServerHelper.manager.update(link);
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
     * 获取ECN关联ECA的产生对象
     * @param pbo
     * @param self
     * @return
     * @throws WTException
     */
    public WTArrayList getChangeAfter(WTObject pbo, ObjectReference self) throws WTException {
        WTArrayList list = new WTArrayList();
        if (pbo instanceof WTChangeOrder2) {
            WTChangeOrder2 order2 = (WTChangeOrder2) pbo;
            Collection<WTChangeActivity2> collection = ModifyUtils.getChangeActivities(order2);
            for (WTChangeActivity2 activity2 : collection) {
                list.addAll(ModifyUtils.getChangeablesAfter(activity2));
            }
        }
        return list;
    }

    /**
     * ECN流程会签、批准驳回处理
     * @param pbo
     * @param self
     * @throws Exception
     */
    public void rejectDispose(WTObject pbo, ObjectReference self) throws Exception {
        if (pbo instanceof WTChangeOrder2) {
            String ecnVid = String.valueOf(PICoreHelper.service.getBranchId(pbo));
            LOGGER.info(">>>>>>>>>>saveAttribute.ecnVid: " + ecnVid);
            Set<CorrelationObjectLink> links = ModifyHelper.service.queryCorrelationObjectLinks((WTChangeOrder2) pbo, LINKTYPE_1);
            //add by lzy at 20191231 start
//            links.add(ModifyHelper.service.queryCorrelationObjectLink(ecnVid, CONSTANTS_7, CONSTANTS_7));
            CorrelationObjectLink otherLink=ModifyHelper.service.queryCorrelationObjectLink(ecnVid, CONSTANTS_7, CONSTANTS_7);
            if (otherLink!=null) links.add(otherLink);
            //add by lzy at 20191231 end
            LOGGER.info("=====rejectDispose.links: " + links);
            System.out.println("=====rejectDispose.links: " + links);
            for (CorrelationObjectLink link : links) {
                String approvalOpinion = link.getApprovalOpinion();
                LOGGER.info("=====rejectDispose.approvalOpinion: " + approvalOpinion);
                System.out.println("=====rejectDispose.approvalOpinion: " + approvalOpinion);
                System.out.println("CONSTANTS_8.equals(approvalOpinion)=="+CONSTANTS_8.equals(approvalOpinion));
                if (CONSTANTS_8.equals(approvalOpinion)) {
                    Persistable persistable = ModifyUtils.getPersistable(link.getEcaIdentifier());
                    System.out.println("persistable=="+persistable);
                    if (persistable instanceof LifeCycleManaged)
                        ModifyUtils.setLifeCycleState((LifeCycleManaged) persistable, CANCELLED);
                    link.setApprovalOpinion("");
                    link.setRemark("");
                    link.setRouting(ROUTING_2);
                    //add by lzy at 20200102 start
//                    if (CONSTANTS_7.equals(link.getPerBranchIdentifier()) && CONSTANTS_7.equals(link.getLinkType())) {
//                        link.setRouting(ROUTING_2);
//                    }
                    //add by lzy at 20200102 end
                    PersistenceServerHelper.manager.update(link);
                }
            }
        }
    }

    /**
     * 获取ECA中所有受影响对象,如果物料变更类型为替换，则不需判断路由节点是否已完成，没启动流程，查询的路由节点为已创建
     * @param changeActivity2
     * @return
     * @throws WTException
     */
    public boolean isReplace(WTChangeActivity2 changeActivity2) throws WTException {
        Boolean isReplace = false;
        if (changeActivity2!=null){
            Collection<Changeable2> befores = ModifyUtils.getChangeablesBefore(changeActivity2);
            for (Changeable2 before : befores) {
                if (before instanceof WTPart) {
                String value = ModifyUtils.getValue(before, CHANGETYPE_COMPID);//获取物料变更类型
                    String[] str = value.split(";");
                    if (str.length > 1) {
                        value = str[1];
                    }
                    if (value.equals("替换")) {
                        isReplace = true;
                    }
                }
            }
        }
        return isReplace;
    }

    /**
     * 设置ECN解决时间
     * @param pbo
     * @param self
     * @throws WTException
     */
    public void setECNResolutionDate(WTObject pbo, ObjectReference self) throws WTException {
        if (pbo instanceof WTChangeOrder2) {
            WTChangeOrder2 changeOrder2 = (WTChangeOrder2) pbo;
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            changeOrder2.setResolutionDate(timestamp);
            PersistenceHelper.manager.save(changeOrder2);
        }
    }

    /**
     * 设置ECN解决时间
     * @param pbo
     * @param self
     * @throws WTException
     */
    public void setECAResolutionDate(WTObject pbo, ObjectReference self) throws WTException {
        if (pbo instanceof WTChangeActivity2) {
            WTChangeActivity2 activity2 = (WTChangeActivity2) pbo;
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            activity2.setResolutionDate(timestamp);
            PersistenceHelper.manager.save(activity2);
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

    /**
     * 检查受影响对象是否为最新大版本
     * @param pbo
     * @param self
     * @throws WTException
     */
    public void checkAffectObjectVesionNew(WTObject pbo, ObjectReference self) throws WTException {
        Set<String> parts=new HashSet();//物料
        Set<String> docs=new HashSet();//文档
        String str="";
        if (pbo instanceof WTChangeOrder2) {
            WTChangeOrder2 changeOrder2 = (WTChangeOrder2) pbo;
            //收集需要创建ECA的部件、文档、CAD文档
            Map<Persistable, Collection<Persistable>> collectionMap = collectionObjectsAffected(changeOrder2);
            System.out.println("collectionMap=="+collectionMap);
            for (Map.Entry<Persistable, Collection<Persistable>> entryMap : collectionMap.entrySet()) {
                if (entryMap.getKey() instanceof Changeable2) {
                    Changeable2 changeable2 = (Changeable2) entryMap.getKey();
                    if (changeable2 instanceof WTPart) {
                        WTPart part = (WTPart) changeable2;
                        String view=part.getViewName();
                        String number=part.getNumber();
                        String version=part.getVersionInfo().getIdentifier().getValue();
                        Vector latestVector=getAllLatestWTParts(view,number);
                        if (latestVector!=null&&latestVector.size()>0) {
                            WTPart newPart = (WTPart) latestVector.get(0);
                            if (newPart != null) {
                                String newVersion = newPart.getVersionInfo().getIdentifier().getValue();
                                System.out.println("version=="+version+"==newVersion=="+newVersion);
                                if (!version.equals(newVersion)){
                                    parts.add(number);
                                }
                            }
                        }
                        for (Persistable persistable : entryMap.getValue()) {
                            if (persistable instanceof WTDocument) {
                                WTDocument doc = (WTDocument) persistable;
                                String docVersion=doc.getVersionInfo().getIdentifier().getValue();
//                                WTDocument newDoc =(WTDocument) VersionControlHelper.service.getLatestIteration(doc, false);
                                QueryResult qrVersions = VersionControlHelper.service.allVersionsOf(doc.getMaster());
                                WTDocument newDoc=new WTDocument();
                                while (qrVersions.hasMoreElements()) {
                                    newDoc=(WTDocument) qrVersions.nextElement();
                                    break;
                                }
                                if (newDoc!=null){
                                    String newVersion = newDoc.getVersionInfo().getIdentifier().getValue();
                                    System.out.println("docVersion=="+docVersion+"==newVersion=="+newVersion);
                                    if (!docVersion.equals(newVersion)){
                                        docs.add(doc.getNumber());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if ((parts!= null && parts.size() > 0)||(docs!= null && docs.size() > 0)) {
                for (String partNumber:parts){
                    str=str+"物料"+partNumber+",";
                }
                for (String docNumber:docs){
                    str=str+"文档"+docNumber+",";
                }
                str=str+"不是最新版本！";
                throw new WTException(str);
            }

        }

    }

    /**
     * 检查受影响对象是否检出
     * @param pbo
     * @throws WTException
     */
    public void isCheckout(WTObject pbo, ObjectReference self) throws WTException {
        Set<String> checkoutPart=new HashSet();//检出物料
        Set<String> checkoutDoc=new HashSet();//检出文档
        String str="";
        if (pbo instanceof WTChangeOrder2) {
            WTChangeOrder2 changeOrder2 = (WTChangeOrder2) pbo;
            //收集需要创建ECA的部件、文档、CAD文档
            Map<Persistable, Collection<Persistable>> collectionMap = collectionObjectsAffected(changeOrder2);
            System.out.println("collectionMap==" + collectionMap);
            for (Map.Entry<Persistable, Collection<Persistable>> entryMap : collectionMap.entrySet()) {
                if (entryMap.getKey() instanceof Changeable2) {
                    Changeable2 changeable2 = (Changeable2) entryMap.getKey();
                    if (changeable2 instanceof WTPart){
                        WTPart part = (WTPart) changeable2;
                        String view=part.getViewName();
                        String number=part.getNumber();
                        Vector latestVector=getAllLatestWTParts(view,number);//同一视图最新版本
                        if (latestVector!=null&&latestVector.size()>0) {
                            WTPart newPart = (WTPart) latestVector.get(0);
                            Boolean flag=PICoreHelper.service.isCheckout(newPart);
                            if (flag){
                                checkoutPart.add(part.getNumber());
                            }
                        }
                        for (Persistable persistable : entryMap.getValue()) {
                            if (persistable instanceof WTDocument) {
                                WTDocument doc= (WTDocument) persistable;
                                WTDocument wtDocument=(WTDocument)VersionControlHelper.service.getLatestIteration(doc,false);//最新版本
                                Boolean flag2=PICoreHelper.service.isCheckout(wtDocument);
                                if (flag2){
                                    checkoutDoc.add(doc.getNumber());
                                }
                            }
                        }

                    }

                }
            }
            if ((checkoutPart!= null && checkoutPart.size() > 0)||(checkoutDoc!= null && checkoutDoc.size() > 0)) {
                for (String partNumber:checkoutPart){
                    str=str+"物料"+partNumber+",";
                }
                for (String docNumber:checkoutDoc){
                    str=str+"文档"+docNumber+",";
                }
                str=str+"被检出！";
                throw new WTException(str);
            }

        }
    }

    /**
     * 通过number获取某视图的最新的物料
     * @param viewName
     * @param number
     * @return
     * @throws WTException
     */
    public static Vector getAllLatestWTParts(String viewName, String number) throws WTException {
        QuerySpec qs = new QuerySpec(WTPart.class);

        View view = ViewHelper.service.getView(viewName);
        SearchCondition sc = new SearchCondition(WTPart.class, "view.key.id", SearchCondition.EQUAL, view.getPersistInfo().getObjectIdentifier().getId());
        qs.appendWhere(sc);
        if (number.trim().length() > 0) {
            qs.appendAnd();
            SearchCondition scNumber = new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL, number.toUpperCase());
            qs.appendWhere(scNumber);
        }

        SearchCondition scLatestIteration = new SearchCondition(WTPart.class, WTAttributeNameIfc.LATEST_ITERATION, SearchCondition.IS_TRUE);
        qs.appendAnd();
        qs.appendWhere(scLatestIteration);

        QueryResult qr = PersistenceHelper.manager.find(qs);
        if (qr != null && qr.hasMoreElements()) qr = (new LatestConfigSpec()).process(qr);

        if (qr != null && qr.hasMoreElements()) return qr.getObjectVectorIfc().getVector();

        return new Vector();
    }


    /**
     * 如果存在驳回为其他情况other(之前驳回过为其他情况的，删除other路由link)
     * @param changeOrder2
     * @throws WTException
     */
    public void setOtherRouting(WTChangeOrder2 changeOrder2) throws Exception{
        String ecnVid = String.valueOf(PICoreHelper.service.getBranchId(changeOrder2));
        CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, CONSTANTS_7, CONSTANTS_7);
        if (null != link) {
            PersistenceServerHelper.manager.remove(link);
        }
    }


    /**
     * 判断ECN流程中是否强制选择会签人员
     * @param pbo
     * @param self
     * @return
     * @throws WTException
     */
    public String checkPartType(WTObject pbo, ObjectReference self) throws WTException {
        List<String> isRDPart = new ArrayList<>();
        if (pbo instanceof WTChangeActivity2) {
            Collection<Changeable2> collection = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
            LOGGER.info("=====checkPartType.collection: " + collection);
            for (Changeable2 changeable2 : collection) {
                if (changeable2 instanceof WTPart) {
                    WTPart part = (WTPart) changeable2;
                    LOGGER.info("=====checkPartType.part: " + part.getNumber());
                    if (part.getNumber().startsWith("A") || part.getNumber().startsWith("C") || part.getNumber().startsWith("D") || part.getNumber().startsWith("E") || part.getNumber().startsWith("P") || part.getNumber().startsWith("T") || part.getNumber().startsWith("S") || part.getNumber().startsWith("K")) {
                        return "RD";
                    }
                    if (part.getNumber().startsWith("B")) {
                        String value = (String) PIAttributeHelper.service.getValue(part, "Classification");
                        String nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(value);
                        if (nodeHierarchy.contains("appo_bcp22")) {// 软件总成
                            isRDPart.add("NOTRD");
                        } else {
                            return "RD";
                        }
                    }
                    if (part.getNumber().startsWith("X")) {
                        isRDPart.add("NOTRD");
                    }
                    if (part.getNumber().startsWith("N") || part.getNumber().startsWith("M")) {
                        return "NM";
                    }
                }
            }
        } else if (pbo instanceof WTChangeOrder2) {
            WTChangeOrder2 changeOrder2 = (WTChangeOrder2) pbo;
            //收集需要创建ECA的部件、文档、CAD文档
            Map<Persistable, Collection<Persistable>> collectionMap = collectionObjectsAffected(changeOrder2);
            System.out.println("collectionMap=="+collectionMap);
            for (Map.Entry<Persistable, Collection<Persistable>> entryMap : collectionMap.entrySet()) {
                if (entryMap.getKey() instanceof Changeable2) {
                    Changeable2 changeable2 = (Changeable2) entryMap.getKey();
                    if (changeable2 instanceof WTPart) {
                        WTPart part = (WTPart) changeable2;
                        if (part.getNumber().startsWith("A") || part.getNumber().startsWith("C") || part.getNumber().startsWith("D") || part.getNumber().startsWith("E") || part.getNumber().startsWith("P") || part.getNumber().startsWith("T") || part.getNumber().startsWith("S") || part.getNumber().startsWith("K")) {
                            return "RD";
                        }
                        if (part.getNumber().startsWith("B")) {
                            String value = (String) PIAttributeHelper.service.getValue(part, "Classification");
                            String nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(value);
                            if (nodeHierarchy.contains("appo_bcp22")) {// 软件总成
                                isRDPart.add("NOTRD");
                            } else {
                                return "RD";
                            }
                        }
                        if (part.getNumber().startsWith("X")) {
                            isRDPart.add("NOTRD");
                        }
                        if (part.getNumber().startsWith("N") || part.getNumber().startsWith("M")) {
                            return "NM";
                        }
                    }
                }
            }
        }
        LOGGER.info("=====checkPartType.isRDPart: " + isRDPart);
        return "NOTRD";
    }
    /**
     * 检查团队角色
     * @param pbo
     * @param rolename
     * @param reviewname
     * @throws WTException
     */
    public void checkTeam(WTObject pbo, String rolename, String reviewname) throws WTException {
        boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
        Role role = Role.toRole(reviewname);
        Role selectrole = Role.toRole(rolename);
        Boolean isrole = false;

        if (pbo instanceof WTChangeOrder2) {
            WTChangeOrder2 ecn = (WTChangeOrder2) pbo;
            Team processTeam = WorkflowUtil.getTeam(ecn);
            if (processTeam != null) {
                Enumeration enumPrin = processTeam.getPrincipalTarget(role);// 会签者

                while (enumPrin.hasMoreElements()) {
                    WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
                    WTPrincipal principal = tempPrinRef.getPrincipal();
                    LOGGER.info("=====checkTeam.principal: " + principal.getName());
                    isrole = checkRole(pbo, rolename, principal);
                    LOGGER.info("=====checkTeam.isrole: " + isrole);
                    if (isrole) {
                        break;
                    }
                }
                if (!isrole && reviewname.equalsIgnoreCase("Signer")) {
                    throw new WTException("会签者/会签节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Receiver")) {
                    throw new WTException("接收者/通知节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Approver")) {
                    throw new WTException("批准者/批准节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Receiver")) {
                    throw new WTException("审核者/审核节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
            }
        } else if (pbo instanceof WTChangeActivity2) {
            WTChangeActivity2 activity2 = (WTChangeActivity2) pbo;
            Team processTeam = WorkflowUtil.getTeam(activity2);
            if (processTeam != null) {
                Enumeration enumPrin = processTeam.getPrincipalTarget(role);// 会签者

                while (enumPrin.hasMoreElements()) {
                    WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
                    WTPrincipal principal = tempPrinRef.getPrincipal();
                    LOGGER.info("=====checkTeam.principal: " + principal.getName());
                    isrole = checkRole(pbo, rolename, principal);
                    LOGGER.info("=====checkTeam.isrole: " + isrole);
                    if (isrole) {
                        break;
                    }
                }
                if (!isrole && reviewname.equalsIgnoreCase("Signer")) {
                    throw new WTException("会签者/会签节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Receiver")) {
                    throw new WTException("接收者/通知节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Approver")) {
                    throw new WTException("批准者/批准节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Receiver")) {
                    throw new WTException("审核者/审核节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
            }
        } else if (pbo instanceof WTPart) {
            WTPart part = (WTPart) pbo;
            Team processTeam = WorkflowUtil.getTeam(part);
            if (processTeam != null) {
                Enumeration enumPrin = processTeam.getPrincipalTarget(role);// 会签者
                while (enumPrin.hasMoreElements()) {
                    WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
                    WTPrincipal principal = tempPrinRef.getPrincipal();
                    LOGGER.info("=====checkTeam.principal: " + principal.getName());
                    isrole = checkRole(pbo, rolename, principal);
                    LOGGER.info("=====checkTeam.isrole: " + isrole);
                    if (isrole) {
                        break;
                    }
                }
                if (!isrole && reviewname.equalsIgnoreCase("Signer")) {
                    throw new WTException("会签者/会签节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Receiver")) {
                    throw new WTException("接收者/通知节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Approver")) {
                    throw new WTException("批准者/批准节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Receiver")) {
                    throw new WTException("审核者/审核节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
            }
        } else if (pbo instanceof WTDocument) {
            WTDocument doc = (WTDocument) pbo;
            Team processTeam = WorkflowUtil.getTeam(doc);
            if (processTeam != null) {
                Enumeration enumPrin = processTeam.getPrincipalTarget(role);// 会签者
                while (enumPrin.hasMoreElements()) {
                    WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
                    WTPrincipal principal = tempPrinRef.getPrincipal();
                    LOGGER.info("=====checkTeam.principal: " + principal.getName());
                    isrole = checkRole(pbo, rolename, principal);
                    LOGGER.info("=====checkTeam.isrole: " + isrole);
                    if (isrole) {
                        break;
                    }
                }
                if (!isrole && reviewname.equalsIgnoreCase("Signer")) {
                    throw new WTException("会签者/会签节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Receiver")) {
                    throw new WTException("接收者/通知节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Approver")) {
                    throw new WTException("批准者/批准节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Receiver")) {
                    throw new WTException("审核者/审核节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
            }
        }
    }
    /**
     * 检查指定角色是否在团队中
     * @param pbo
     * @param rolename
     * @param principal
     * @return
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    public static Boolean checkRole(WTObject pbo, String rolename, WTPrincipal principal) {
        Boolean isrole = false;

        Role role = Role.toRole(rolename);
        System.out.println("role===" + role.getShortDescription());
        System.out.println("pbo===" + pbo);
        WTContainer container = null;
        if (pbo instanceof WTChangeOrder2) {
            WTChangeOrder2 object = (WTChangeOrder2) pbo;
            container = object.getContainer();
        }
        if (pbo instanceof WTPart) {
            WTPart object = (WTPart) pbo;
            container = object.getContainer();
        }
        if (pbo instanceof WTDocument) {
            WTDocument object = (WTDocument) pbo;
            container = object.getContainer();
        }
        //add by lzy at 20191212 start
        if (pbo instanceof WTChangeActivity2) {
            WTChangeActivity2 object = (WTChangeActivity2) pbo;
            container = object.getContainer();
        }
        //add by lzy at 20191212 end
        if (container != null) {
            try {
                WTRoleHolder2 wtroleholder2 = TeamCCHelper.getTeamFromObject(container);
                ContainerTeam containerTeam = ContainerTeamHelper.service.getContainerTeam((ContainerTeamManaged) container);
                Enumeration enumPrin = containerTeam.getPrincipalTarget(role);
                Set<WTUser> userSet = new HashSet<>();
                while (enumPrin.hasMoreElements()) {
                    WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
                    WTPrincipal wtPrincipal = tempPrinRef.getPrincipal();
                    System.out.println("wtprincipal====" + wtPrincipal.getName());

                    if (wtPrincipal instanceof WTUser) {
                        userSet.add((WTUser) wtPrincipal);
                    } else if (wtPrincipal instanceof wt.org.WTGroup) {
                        userSet.addAll(PartWorkflowUtil.getGroupMembers((wt.org.WTGroup) wtPrincipal));
                    }
                }

                if (userSet != null && userSet.size() > 0 && userSet.contains(principal)) {
                    isrole = true;
                }
            } catch (WTException e) {
                e.printStackTrace();
            } // 获取容器的人

        }

        return isrole;
    }

    /**
     * 检查流程中的角色是否在上下文团队中
     * @param pbo
     * @throws WTException
     */
    public void checkUserPermission(WTObject pbo) throws WTException {
        StringBuffer result = new StringBuffer();
        if (pbo instanceof WTChangeOrder2) {
            WTChangeOrder2 ecn = (WTChangeOrder2) pbo;

            WTContainer container = ecn.getContainer();
            ContainerTeam containerTeam = ContainerTeamHelper.service.getContainerTeam((ContainerTeamManaged) container);
            Set<WTUser> userSet = PartWorkflowUtil.getAllMembers(containerTeam);
            Team processTeam = WorkflowUtil.getTeam(ecn);
            // 流程实例所有角色
            Vector processRoleVector = processTeam.getRoles();
            a:for (Object o : processRoleVector) {
                Role processRole = (Role) o;
                Enumeration enumPrin = processTeam.getPrincipalTarget(processRole);// 会签者
                while (enumPrin.hasMoreElements()) {
                    WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
                    WTPrincipal principal = tempPrinRef.getPrincipal();
                    if (principal instanceof WTUser) {
                        WTUser user = (WTUser) principal;
                        if (userSet != null && userSet.size() > 0 && !userSet.contains(user)) {
                            result.append("用户").append(user.getFullName()).append("不在").append(container.getContainerName()).append("团队中，请另外选择人员或通知业务管理员设置用户权限！");
                            break a;
                        }
                    }
                }
            }
        }
        if (result.length() > 0) {
            throw new WTException(result.toString());
        }
    }
    /**
     * 给流程节点角色添加固定人员
     * 删除用户为空抛出异常
     * @param pbo
     * @param rolename
     * @param principalname
     * @throws WTException
     */
    public void addTeamRole(WTObject pbo, String rolename, String principalname) throws WTException {
        Role role = Role.toRole(rolename);
        WTPrincipal wtprincipal = PartWorkflowUtil.getUserFromName(principalname);
        if (wtprincipal != null) {
            LOGGER.info("=====addTeamRole.wtprincipal: " + wtprincipal.getName());
            if (pbo instanceof WTChangeOrder2) {
                WTChangeOrder2 ecn = (WTChangeOrder2) pbo;
                Team processTeam = WorkflowUtil.getTeam(ecn);
                if (processTeam != null) {
                    processTeam.addPrincipal(role, wtprincipal);
                    LOGGER.info("=====addTeamRole.end add people success: ");
                }
            }
        }
    }
    /**
     * 根据产品类别，判断是否需要加人
     * @param pbo
     * @return
     */
    public Boolean checkContainer(WTObject pbo) {
        Boolean isok = false;
        if (pbo instanceof WTChangeOrder2) {
            WTChangeOrder2 ecn = (WTChangeOrder2) pbo;
            String productline = "";
            try {
                productline = (String) PIAttributeHelper.service.getValue(ecn, ATTRIBUTE_2);
            } catch (PIException e) {
                e.printStackTrace();
            }
            LOGGER.info("=====checkContainer.productline: " + productline);
            // 微投--40 激光电视--60
            if (productline.equalsIgnoreCase("40") || productline.equalsIgnoreCase("60")) {
                isok = true;
            }
        }
        LOGGER.info("=====checkContainer.isok: " + isok);
        return isok;
    }

    /***
     * 产生对象批量设置状态为‘已发布’
     *并返回所有产生对象及发布BOM下所有已发布子件(不是只返回发布的产生对象及发布BOM下所有已发布子件)
     * @param wtObject
     *            WTChangeOrder2
     * @return
     */
    public static Collection<WTPart> setReleaseState(WTObject wtObject) {
        Collection<WTPart> releasePartsArray = new HashSet<WTPart>();
        if (wtObject == null || !(wtObject instanceof WTChangeOrder2)) {
            return releasePartsArray;
        }

        try {
            WTChangeOrder2 ecn = (WTChangeOrder2) wtObject;
            // 获取ECN中所有ECA与受影响对象集合
            Map<ChangeActivityIfc, Collection<Changeable2>> beforeInfoMap = getChangeablesBeforeInfo(ecn);
            if (beforeInfoMap == null || beforeInfoMap.size() == 0) {
                return releasePartsArray;
            }
            // 获取ECN中所有ECA与产生对象集合
            Map<ChangeActivityIfc, Collection<Changeable2>> afterInfoMap = getChangeablesAfterInfo(ecn);
            if (afterInfoMap == null || afterInfoMap.size() == 0) {
                return releasePartsArray;
            }

            for (Map.Entry<ChangeActivityIfc, Collection<Changeable2>> beforeEntryMap : beforeInfoMap.entrySet()) {
                ChangeActivityIfc eca = beforeEntryMap.getKey();
                // 产生对象集合
                Collection<Changeable2> afterInfoArray = afterInfoMap.get(eca);
                for (Changeable2 afterObject : afterInfoArray) {
                    if (afterObject instanceof WTPart) {
                        //添加所有产生的对象
                        releasePartsArray.add((WTPart) afterObject);
                    }
                }
                // 受影响对象集合
                Collection<Changeable2> beforeArray = beforeEntryMap.getValue();
                for (Changeable2 changeable2 : beforeArray) {
                    if (checkState((LifeCycleManaged) changeable2, ChangeConstants.RELEASED)) {
                        for (Changeable2 afterObject : afterInfoArray) {
                            if (getNumber(afterObject).equals(getNumber(changeable2))) {
                                // 设置状态
                                PICoreHelper.service.setLifeCycleState((LifeCycleManaged) afterObject,
                                        ChangeConstants.RELEASED);
                                if (afterObject instanceof WTPart) {
                                    // 增加自动随签发布的逻辑，edit by cjt
                                    WTPart part = (WTPart) afterObject;
                                    // 获取部件下所有子件信息
                                    Collection<WTPart> partMultiwallStructure = ChangePartQueryUtils
                                            .getPartMultiwallStructure(part);
                                    for (WTPart childpart : partMultiwallStructure) {
                                        if (childpart.getState().toString()
                                                .equalsIgnoreCase(ChangeConstants.ARCHIVED)) {
                                            // 设置状态
                                            PICoreHelper.service.setLifeCycleState((LifeCycleManaged) childpart,
                                                    ChangeConstants.RELEASED);
                                            if (!releasePartsArray.contains(childpart)) {
                                                releasePartsArray.add(childpart);
                                            }

                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        return releasePartsArray;
    }

    /***
     * 获取ECN中所有ECA与受影响对象
     *
     * @param ecn
     * @return
     * @throws WTException
     */
    public static Map<ChangeActivityIfc, Collection<Changeable2>> getChangeablesBeforeInfo(WTChangeOrder2 ecn)
            throws WTException {
        Map<ChangeActivityIfc, Collection<Changeable2>> datasMap = new HashMap<ChangeActivityIfc, Collection<Changeable2>>();
        if (ecn == null) {
            return datasMap;
        }

        try {
            // 获取ECN中所有ECA对象
            Collection<ChangeActivityIfc> ecaArray = getChangeActivities(ecn);
            for (ChangeActivityIfc changeActivityIfc : ecaArray) {
                // 受影响对象集合
                Collection<Changeable2> datasArray = new HashSet<Changeable2>();
                // 获取ECA中所有受影响对象
                QueryResult qr = ChangeHelper2.service.getChangeablesBefore(changeActivityIfc);
                while (qr.hasMoreElements()) {
                    Object object = qr.nextElement();
                    if (object instanceof ObjectReference) {
                        object = ((ObjectReference) object).getObject();
                    }
                    if (object instanceof Changeable2) {
                        datasArray.add((Changeable2) object);
                    }
                }
                datasMap.put(changeActivityIfc, datasArray);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return datasMap;
    }

    /***
     * 获取ECN中所有ECA与产生对象
     *
     * @param ecn
     * @return
     * @throws WTException
     */
    public static Map<ChangeActivityIfc, Collection<Changeable2>> getChangeablesAfterInfo(WTChangeOrder2 ecn)
            throws WTException {
        Map<ChangeActivityIfc, Collection<Changeable2>> datasMap = new HashMap<ChangeActivityIfc, Collection<Changeable2>>();
        if (ecn == null) {
            return datasMap;
        }

        try {
            // 获取ECN中所有ECA对象
            Collection<ChangeActivityIfc> ecaArray = getChangeActivities(ecn);
            for (ChangeActivityIfc changeActivityIfc : ecaArray) {
                // 产生对象集合
                Collection<Changeable2> datasArray = new HashSet<Changeable2>();
                // 获取ECA中所有产生对象
                QueryResult qr = ChangeHelper2.service.getChangeablesAfter(changeActivityIfc);
                while (qr.hasMoreElements()) {
                    Object object = qr.nextElement();
                    if (object instanceof ObjectReference) {
                        object = ((ObjectReference) object).getObject();
                    }
                    if (object instanceof Changeable2) {
                        datasArray.add((Changeable2) object);
                    }
                }
                datasMap.put(changeActivityIfc, datasArray);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return datasMap;
    }


    /***
     * 判断对象状态是否符合要求
     *
     * @param lifecycleManaged
     * @param state
     * @return
     */
    public static Boolean checkState(LifeCycleManaged lifecycleManaged, String state) {
        if (lifecycleManaged == null || PIStringUtils.isNull(state)) {
            return false;
        }

        return lifecycleManaged.getLifeCycleState().toString().equalsIgnoreCase(state);
    }

    /***
     * 获取对象编码
     *
     * @param persistable
     * @return
     */
    public static String getNumber(Persistable persistable) {
        if (persistable instanceof ObjectReference) {
            persistable = ((ObjectReference) persistable).getObject();
        }

        String number = "";

        if (persistable instanceof WTPart) {
            number = ((WTPart) persistable).getNumber();
        } else if (persistable instanceof EPMDocument) {
            number = ((EPMDocument) persistable).getNumber();
        } else if (persistable instanceof WTDocument) {
            number = ((WTDocument) persistable).getNumber();
        } else if (persistable instanceof WTChangeRequest2) {
            number = ((WTChangeRequest2) persistable).getNumber();
        } else if (persistable instanceof WTChangeOrder2) {
            number = ((WTChangeOrder2) persistable).getNumber();
        } else if (persistable instanceof WTChangeActivity2) {
            number = ((WTChangeActivity2) persistable).getNumber();
        }

        return number;
    }

    /***
     * 获取更改通告中所有更改任务
     *
     * @param changeOrder2
     *            变更通告
     * @return
     * @throws WTException
     */
    public static Collection<ChangeActivityIfc> getChangeActivities(WTChangeOrder2 changeOrder2) throws WTException {
        Collection<ChangeActivityIfc> datasArray = new HashSet<ChangeActivityIfc>();
        if (changeOrder2 == null) {
            return datasArray;
        }

        try {
            // 获取更改通告中所有的受影响对象
            QueryResult qr = ChangeHelper2.service.getChangeActivities(changeOrder2);
            while (qr.hasMoreElements()) {
                Object object = qr.nextElement();
                if (object instanceof ObjectReference) {
                    object = ((ObjectReference) object).getObject();
                }
                if (object instanceof WTChangeActivity2) {
                    datasArray.add((WTChangeActivity2) object);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return datasArray;
    }

    /**
     * 提交时第一校验
     * 校验ECN是暂存按钮操作还是完成按钮操作
     * @param pbo
     * @param self
     * @throws WTException
     */
    public void checkECNActionName(WTObject pbo, ObjectReference self) throws WTException {
        if (pbo instanceof WTChangeOrder2) {
            WTChangeOrder2 changeOrder2 = (WTChangeOrder2) pbo;
            String actionName = ModifyUtils.getValue(changeOrder2, ATTRIBUTE_6);
            if (CONSTANTS_12.equals(actionName)){
                throw new WTException("变更申请表单未提交，请先进入【变更申请表单页面】，检查表单数据是否填写完整，再点击【完成】进行提交!");
            }
        }
    }

}