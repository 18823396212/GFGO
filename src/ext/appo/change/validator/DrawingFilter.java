package ext.appo.change.validator;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;
import ext.appo.change.util.ModifyUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.pi.core.PICoreHelper;
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
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WfAssignmentState;
import wt.workflow.work.WorkItem;

import java.util.Collection;

/**
 * ECA子流程已完成则不显示产生对象按钮（节点必须为【数据更改】）
 */
public class DrawingFilter extends DefaultSimpleValidationFilter {

    private static final Logger LOGGER = LogR.getLogger(DrawingFilter.class.getName());

    @Override
    public UIValidationStatus preValidateAction(UIValidationKey key, UIValidationCriteria criteria) {
        UIValidationStatus status = UIValidationStatus.HIDDEN;
        try {
            WTReference reference = criteria.getContextObject();
            if (reference != null) {
                Persistable persistable = reference.getObject();
                LOGGER.info("=====persistable: " + persistable);
                if (persistable instanceof WorkItem) {
                    WorkItem workItem=(WorkItem)persistable;
                    WfAssignmentState state = workItem.getStatus();
                    WfAssignedActivity activity = (WfAssignedActivity) workItem.getSource().getObject();
                    String activityName = activity.getName();
                    if (!state.equals(WfAssignmentState.COMPLETED)&&activityName.equals("数据更改")) {
                        status = UIValidationStatus.ENABLED;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

}