package ext.appo.change.validator;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;
import ext.appo.change.util.ModifyUtils;
import ext.pi.core.PIPrincipalHelper;
import org.apache.log4j.Logger;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.WTReference;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.session.SessionHelper;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfState;

import java.util.Collection;

/**
 * 取消变更申请按钮过滤
 */
public class CancelChangeApplyFilter extends DefaultSimpleValidationFilter {

    private static final Logger LOGGER = LogR.getLogger(CancelChangeApplyFilter.class.getName());

    @Override
    public UIValidationStatus preValidateAction(UIValidationKey key, UIValidationCriteria criteria) {
        UIValidationStatus status = UIValidationStatus.HIDDEN;
        try {
            WTReference reference = criteria.getContextObject();
            if (reference != null) {
                Persistable persistable = reference.getObject();
                LOGGER.info("=====persistable: " + persistable);
                if (persistable instanceof WTChangeOrder2) {
                    WTPrincipal principal = SessionHelper.manager.getPrincipal();
                    LOGGER.info("=====principal: " + principal.getName());
                    boolean flag = PIPrincipalHelper.service.isOrganizationAdministrator((WTUser) principal);
                    LOGGER.info("=====flag: " + flag);
                    if (flag) {
                        WTChangeOrder2 changeOrder2 = (WTChangeOrder2) persistable;
                        Collection<WTChangeActivity2> activity2s = ModifyUtils.getChangeActivities(changeOrder2);
                        LOGGER.info("=====activity2s: " + activity2s);
                        System.out.println("=====activity2s: " + activity2s);
                        if (activity2s.size() > 0) {
                            for (WTChangeActivity2 eca:activity2s){
                                //ECA流程是否都在运行中
                                QueryResult qr = WfEngineHelper.service.getAssociatedProcesses(eca, null, null);
                                while (qr.hasMoreElements()) {
                                    WfProcess process = (WfProcess) qr.nextElement();
                                    if (process!=null){
                                        String templateName = process.getTemplate().getName();
                                        String state = String.valueOf(changeOrder2.getLifeCycleState());
                                        //存在运行流程
                                        if (process.getState().equals(WfState.OPEN_RUNNING)){
                                            status = UIValidationStatus.ENABLED;
                                            break;
                                        }

                                    }

                                }
                            }

                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

}