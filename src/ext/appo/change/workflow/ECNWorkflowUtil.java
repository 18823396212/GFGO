package ext.appo.change.workflow;

import ext.appo.change.ModifyHelper;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.models.CorrelationObjectLink;
import ext.appo.change.util.ModifyUtils;
import ext.appo.ecn.constants.ChangeConstants;
import org.apache.log4j.Logger;
import wt.change2.Changeable2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceServerHelper;
import wt.fc.WTObject;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTSet;
import wt.log4j.LogR;
import wt.util.WTException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * ECN主流程相关处理逻辑
 */
public class ECNWorkflowUtil implements ChangeConstants, ModifyConstants {

    private static Logger LOGGER = LogR.getLogger(ECNWorkflowUtil.class.getName());

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

    /*public boolean isRejected(WTObject pbo, ObjectReference self) throws WTException {
        if (pbo instanceof WTChangeOrder2) {
            Collection<WTChangeActivity2> activity2s = ModifyUtils.getChangeActivities((WTChangeOrder2) pbo);
            for (WTChangeActivity2 activity2 : activity2s) {
                String state = activity2.getLifeCycleState().toString();
                LOGGER.info("=====isRejected.state1: " + state);
                if (CANCELLED.equals(state)) {
                    Collection<Changeable2> collection = ModifyUtils.getChangeablesAfter(activity2);
                    LOGGER.info("=====isRejected.collection: " + collection);
                    for (Changeable2 changeable2 : collection) {
                        state = ModifyUtils.getState(changeable2);
                        LOGGER.info("=====isRejected.state2: " + state);
                        //产生对象列表只要存在一个正在工作、重新工作的对象即认为子流程被驳回
                        if (STATE_1.equals(state) || STATE_2.equals(state)) return true;
                    }
                }
            }
        }
        return false;
    }*/

    /**
     * 修改变更申请任务，取消变更路由逻辑
     * 2.3.1、需要判断是否所有子流程都处于实施状态（或者已取消状态）。
     * 2.3.2、若路由选择“取消变更”，需要恢复所有变更对象到变更前版本
     * @throws WTException
     */
    public void cancelRoute(WTObject pbo, ObjectReference self) throws WTException {
        Set<WTChangeActivity2> unfinished = new HashSet<>();

        Set<WTChangeActivity2> activity2s = new HashSet<>();
        Set<CorrelationObjectLink> links = ModifyHelper.service.queryCorrelationObjectLinks((WTChangeOrder2) pbo, LINKTYPE_1);
        LOGGER.info("=====cancelRoute.links: " + links);
        for (CorrelationObjectLink link : links) {
            LOGGER.info("=====cancelRoute.ecaIdentifier: " + link.getEcaIdentifier());
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
        //存在
        if (unfinished.size() > 0) {

        } else {

        }


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
    }

    /**
     * 检查对象关联的ECA是否已取消状态，并且产生对象包含正在工作、重新工作的对象
     * @param link
     * @return
     * @throws WTException
     */
    public static boolean isDelete(CorrelationObjectLink link) throws WTException {
        //查不到对应的Link，默认为子流程被驳回、可以移除
        if (link == null) return true;

        Persistable persistable = ModifyUtils.getPersistable(link.getEcaIdentifier());
        //ECA为null，默认为子流程被驳回、可以移除
        if (persistable == null) return true;

        if (persistable instanceof WTChangeActivity2) {
            //检查对象关联的ECA是否已取消状态
            if (CANCELLED.equals(ModifyUtils.getState(persistable))) {
                Collection<Changeable2> collection = ModifyUtils.getChangeablesAfter((WTChangeActivity2) persistable);
                LOGGER.info("=====cancelRoute.collection: " + collection);
                for (Changeable2 changeable2 : collection) {
                    String state = ModifyUtils.getState(changeable2);
                    LOGGER.info("=====cancelRoute.state: " + state);
                    //产生对象列表只要存在一个正在工作、重新工作的对象即认为子流程被驳回、可以移除
                    if (STATE_1.equals(state) || STATE_2.equals(state)) return true;
                }
            }
        }
        return false;
    }

}