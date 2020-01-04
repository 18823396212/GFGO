package ext.appo.change.validator;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.wvs.livecycle.assembler.Principal;
import ext.appo.change.util.AffectedObjectUtil;
import ext.appo.change.util.ChangeActivity2Util;
import ext.appo.change.util.ModifyUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.pi.core.PICoreHelper;
import ext.pi.core.PIPrincipalHelper;
import org.apache.log4j.Logger;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.change2.WTChangeRequest2;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.WTReference;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.session.SessionHelper;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WfAssignment;
import wt.workflow.work.WorkItem;

import java.util.*;

/**
 * 驳回变更申请按钮过滤（只需判断所有流程节点在是否数据变更节点，无需判断责任人）
 */
public class RejectChangeApplyFilter extends DefaultSimpleValidationFilter {

    private static final Logger LOGGER = LogR.getLogger(RejectChangeApplyFilter.class.getName());

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
                    WTChangeOrder2 changeOrder2 = (WTChangeOrder2) persistable;
                    boolean flag = PIPrincipalHelper.service.isOrganizationAdministrator((WTUser) principal);
                    String creatorFullName = changeOrder2.getCreatorFullName();
                    LOGGER.info("=====flag: " + flag);
                    Collection<WTChangeActivity2> activity2s = ModifyUtils.getChangeActivities(changeOrder2);
                    LOGGER.info("=====activity2s: " + activity2s);
                    System.out.println("=====activity2s: " + activity2s);
                    WTUser user = null;
                    if (activity2s.size() > 0) {
                        for (WTChangeActivity2 eca:activity2s){
                            //过滤事务性任务eca
                            if (PICoreHelper.service.isType(eca, ChangeConstants.TRANSACTIONAL_CHANGEACTIVITY2)) continue;

                            QueryResult qr = wt.workflow.work.WorkflowHelper.service.getWorkItems(eca);
                            while (qr.hasMoreElements()) {
                                WorkItem item = (WorkItem) qr.nextElement();
                                WfAssignedActivity activity = (WfAssignedActivity) item.getSource().getObject();
                                String activityName = activity.getName();
                                System.out.println("eca=="+eca+"==activityName=="+activityName);
                                if(!activityName.equals("数据更改")){
                                    return status;
                                }
//                                else {
//                                    WTPrincipalReference owner = item.getOwnership().getOwner();
//                                    //数据更改 节点负责人
//                                    user=(WTUser)owner.getPrincipal();
//                                    //数据更改 节点工作负责人 需要等于ECO提交者
//                                    if (!user.getFullName().equals(creatorFullName)){
//                                        return status;
//                                    }
//
//                                }
                            }
                        }
                        String current = ((WTUser) principal).getFullName();
//                        if (flag||(user!=null&&user.getFullName().equals(current))){
//                            status = UIValidationStatus.ENABLED;
//                        }
                        //管理员或者是ECN提交者
                        if (flag||creatorFullName.equals(current)){
                            status = UIValidationStatus.ENABLED;
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