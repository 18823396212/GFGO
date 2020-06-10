package ext.appo.change.processor;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.*;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import ext.appo.change.ModifyHelper;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.models.CorrelationObjectLink;
import ext.appo.change.models.TransactionTask;
import ext.appo.change.util.ModifyUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PICoreHelper;
import ext.pi.core.PIWorkflowHelper;
import org.apache.log4j.Logger;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.collections.WTHashSet;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.sandbox.SandboxHelper;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfProcess;

import java.util.*;

/**
 * 驳回变更申请按钮
 */
public class RejectChangeApplyProcessor extends DefaultObjectFormProcessor implements ChangeConstants, ModifyConstants {

    private static final Logger LOGGER = LogR.getLogger(RejectChangeApplyProcessor.class.getName());

    public FormResult doOperation(NmCommandBean nmCommandBean, List<ObjectBean> list) throws WTException {
        FormResult formResult = new FormResult();
        formResult.setStatus(FormProcessingStatus.SUCCESS);
//        boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
        SessionContext previous = SessionContext.newContext();
        // 当前用户设置为管理员，用于忽略权限
        SessionHelper.manager.setAdministrator();
        try {
            Object object = nmCommandBean.getActionOid().getRefObject();
            LOGGER.info("=====object: " + object);
            if (object instanceof WTChangeOrder2) {
                WTChangeOrder2 changeOrder2 = (WTChangeOrder2) object;
                Collection<WTChangeActivity2> activity2s = ModifyUtils.getChangeActivities(changeOrder2);
                LOGGER.info("=====activity2s: " + activity2s);
                for (WTChangeActivity2 activity2 : activity2s) {

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
                    LOGGER.info("=====activity2: " + activity2.getNumber() + " >>>>>collection: " + collection);
                    //移除受影响对象
                    ModifyUtils.removeAffectedActivityData(activity2);
                    //移除产生对象
                    ModifyUtils.removeChangeRecord(activity2);
                    //删除进程
                    QueryResult result = WfEngineHelper.service.getAssociatedProcesses(activity2, null, null);
                    LOGGER.info(">>>>>>>>>>activity2:" + activity2.getNumber() + " >>>>>result: " + result.size());
                    while (result.hasMoreElements()) {
                        WfProcess process = (WfProcess) result.nextElement();
                        LOGGER.info(">>>>>>>>>>process:" + process);
                        //终止进程
                        PIWorkflowHelper.service.stop(process);
//                        //删除进程
//                        PersistenceServerHelper.manager.remove(process);
                    }
                    //删除ECA
                    PersistenceServerHelper.manager.remove(activity2);
                    //删除修订版本
                    SandboxHelper.service.removeObjects(new WTHashSet(collection));
                }
                //先终止进程
                QueryResult result = WfEngineHelper.service.getAssociatedProcesses(changeOrder2, null, null);
                LOGGER.info(">>>>>>>>>>activity2:" + changeOrder2.getNumber() + " >>>>>result: " + result.size());
                while (result.hasMoreElements()) {
                    WfProcess process = (WfProcess) result.nextElement();
                    PIWorkflowHelper.service.stop(process);
                }
                SessionContext.setContext(previous);//设置回原用户，ECN编辑节点会自动获取当前用户为责任人
                boolean flag = SessionServerHelper.manager.setAccessEnforced(false); // 忽略权限
                try{
                    //设置ECN状态为-已开启
                    State state = State.toState("OPEN");
                    LifeCycleHelper.service.setLifeCycleState(changeOrder2, state);
                } catch (Exception e) {
                    throw new WTException(e.getStackTrace());
                } finally {
                    SessionServerHelper.manager.setAccessEnforced(flag);
                }
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        } finally {
            SessionContext.setContext(previous);
        }
        setRefreshFormResult(nmCommandBean, formResult);
        return formResult;
    }

    @SuppressWarnings("deprecation")
    public static FormResult setRefreshFormResult(NmCommandBean nmCommandBean, FormResult result) throws WTException {
        DynamicRefreshInfo refreshInfo = new DynamicRefreshInfo();
        refreshInfo.setLocation(nmCommandBean.getActionOid());
        refreshInfo.setOid(nmCommandBean.getActionOid());
        refreshInfo.setAction(DynamicRefreshInfo.Action.UPDATE);

        result.setNextAction(FormResultAction.REFRESH_OPENER);
        result.addDynamicRefreshInfo(refreshInfo);
        return result;
    }

}