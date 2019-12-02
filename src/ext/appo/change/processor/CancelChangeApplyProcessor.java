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
import ext.pi.core.PICoreHelper;
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
import wt.sandbox.SandboxHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfProcess;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 取消变更申请按钮
 */
public class CancelChangeApplyProcessor extends DefaultObjectFormProcessor implements ChangeConstants, ModifyConstants {

    private static final Logger LOGGER = LogR.getLogger(CancelChangeApplyProcessor.class.getName());

    public FormResult doOperation(NmCommandBean nmCommandBean, List<ObjectBean> list) throws WTException {
        FormResult formResult = new FormResult();
        formResult.setStatus(FormProcessingStatus.SUCCESS);
        boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
        try {
            Object object = nmCommandBean.getActionOid().getRefObject();
            LOGGER.info("=====object: " + object);
            if (object instanceof WTChangeOrder2) {
                WTChangeOrder2 changeOrder2 = (WTChangeOrder2) object;
                Collection<WTChangeActivity2> activity2s = ModifyUtils.getChangeActivities(changeOrder2);
                LOGGER.info("=====activity2s: " + activity2s);
                for (WTChangeActivity2 activity2 : activity2s) {
                    //if (RESOLVED.equals(activity2.getState().toString())) continue;//已解决的ECA跳过？？？？

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
                        PersistenceServerHelper.manager.remove(process);
                    }
                    //删除ECA
                    PersistenceServerHelper.manager.remove(activity2);
                    //删除修订版本
                    SandboxHelper.service.removeObjects(new WTHashSet(collection));
                }
                //add by lzy at 20191130 start
                //设置ECN状态为-已取消
                State state = State.toState("CANCELLED");
                LifeCycleHelper.service.setLifeCycleState(changeOrder2,state);
                //add by lzy at 20191130 end
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        } finally {
            SessionServerHelper.manager.setAccessEnforced(flag);
        }
        setRefreshFormResult(nmCommandBean, formResult);
        return formResult;
    }

    @SuppressWarnings("deprecation")
    public static FormResult setRefreshFormResult(NmCommandBean nmCommandBean, FormResult result) throws WTException{
        DynamicRefreshInfo refreshInfo = new DynamicRefreshInfo();
        refreshInfo.setLocation(nmCommandBean.getActionOid());
        refreshInfo.setOid(nmCommandBean.getActionOid());
        refreshInfo.setAction(DynamicRefreshInfo.Action.UPDATE);

        result.setNextAction(FormResultAction.REFRESH_OPENER);
        result.addDynamicRefreshInfo(refreshInfo);
        return result;
    }

}