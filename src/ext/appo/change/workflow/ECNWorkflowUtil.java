package ext.appo.change.workflow;

import com.ptc.windchill.pdmlink.change.server.impl.WorkflowProcessHelper;
import ext.appo.change.ModifyHelper;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.models.CorrelationObjectLink;
import ext.appo.change.models.TransactionTask;
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
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTHashSet;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.part.PartDocHelper;
import wt.part.WTPart;
import wt.pom.Transaction;
import wt.project.Role;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.sandbox.SandboxHelper;
import wt.session.SessionServerHelper;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
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
            if (links.size() > 0) return true;
            links = ModifyHelper.service.queryCorrelationObjectLinks((WTChangeOrder2) pbo, LINKTYPE_3, ROUTING_2);
            LOGGER.info("=====isRejected.links2: " + links);
            if (links.size() > 0) return true;
            links = ModifyHelper.service.queryCorrelationObjectLinks((WTChangeOrder2) pbo, CONSTANTS_7, ROUTING_2);//其他情况驳回判定
            LOGGER.info("=====isRejected.links3: " + links);
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
    public void cancelRoute(WTObject pbo, ObjectReference self) throws Exception {
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
                    String changeObjectType = ModifyUtils.getValue(changeable2, ATTRIBUTE_7);//变更对象类型
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
                    //modify by xiebowen at 2019/12/24  start
                    //String attributeValue = ModifyUtils.getValue(changeable2, CHANGETYPE_COMPID);
                    String attributeValue = ModifyUtils.getValue(changeable2, CHANGOBJECTETYPE_COMPID);
                    //modify by xiebowen at 2019/12/24  end
                    if (PIStringUtils.isNotNull(attributeValue) && attributeValue.contains(VALUE_1)) {
                        PICoreHelper.service.setLifeCycleState(eca, RESOLVED);
//                        add by lzy at 20191209 start
                    } else if (PIStringUtils.isNotNull(attributeValue) && attributeValue.contains(VALUE_4)) {
//                    } else if (PIStringUtils.isNotNull(attributeValue) && (attributeValue.contains(VALUE_7)||attributeValue.contains(VALUE_8))) {
//                        add by lzy at 20191209 end

                        //修订受影响对象，并添加到产生对象列表
                        WTCollection collection = ModifyUtils.revise(vector, reviseMap);
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
            links.add(ModifyHelper.service.queryCorrelationObjectLink(ecnVid, CONSTANTS_7, CONSTANTS_7));
            LOGGER.info("=====rejectDispose.links: " + links);
            for (CorrelationObjectLink link : links) {
                String approvalOpinion = link.getApprovalOpinion();
                LOGGER.info("=====rejectDispose.approvalOpinion: " + approvalOpinion);
                if (CONSTANTS_8.equals(approvalOpinion)) {
                    Persistable persistable = ModifyUtils.getPersistable(link.getEcaIdentifier());
                    if (persistable instanceof LifeCycleManaged)
                        ModifyUtils.setLifeCycleState((LifeCycleManaged) persistable, CANCELLED);
                    link.setApprovalOpinion("");
                    link.setRemark("");
                    if (CONSTANTS_7.equals(link.getPerBranchIdentifier()) && CONSTANTS_7.equals(link.getLinkType())) {
                        link.setRouting(ROUTING_2);
                    }
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
//                String value = ModifyUtils.getValue(before, "ChangeType");//获取物料变更类型
                    //add by lzy at 20191229 start
                    String value = ModifyUtils.getValue(before, CHANGOBJECTETYPE_COMPID);//获取物料变更对象类型
                    //add by lzy at 20191229 end
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
                                if (!version.equals(newVersion)){
                                    parts.add(number);
                                }
                            }
                        }
                        for (Persistable persistable : entryMap.getValue()) {
                            if (persistable instanceof WTDocument) {
                                WTDocument doc = (WTDocument) persistable;
                                String docVersion=doc.getVersionInfo().getIdentifier().getValue();
                                QueryResult qrVersions = VersionControlHelper.service.allVersionsOf(doc.getMaster());
                                WTDocument newDoc=new WTDocument();
                                while (qrVersions.hasMoreElements()) {
                                    newDoc=(WTDocument) qrVersions.nextElement();
                                    break;
                                }
                                if (newDoc!=null){
                                    String newVersion = newDoc.getVersionInfo().getIdentifier().getValue();
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

}