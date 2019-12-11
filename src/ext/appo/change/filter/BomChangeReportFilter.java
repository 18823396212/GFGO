package ext.appo.change.filter;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.fc.WTReference;
import wt.workflow.definer.WfProcessTemplate;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;

public class BomChangeReportFilter extends DefaultSimpleValidationFilter {


    @Override
    public UIValidationStatus preValidateAction(UIValidationKey key, UIValidationCriteria uivalidationcriteria) {
        UIValidationStatus uivalidationstatus = UIValidationStatus.HIDDEN;
        WTReference wtreference = uivalidationcriteria.getContextObject();
        if (wtreference != null) {
            Persistable persistable = wtreference.getObject();
            try {
                if (persistable != null && persistable instanceof WorkItem) {
                    WorkItem workItem = (WorkItem) persistable;
                    WfAssignedActivity wfassignedactivity = (WfAssignedActivity) workItem.getSource().getObject();
                    WfProcess wfprocess  = wfassignedactivity.getParentProcess();
                    WfProcessTemplate wfprocesstemplate = (WfProcessTemplate) wfprocess.getTemplate().getObject();
                    String templateName = wfprocesstemplate.getName();
                    // 判断是否是ECN流程，BOM变更流程，图纸变更流程
                    if(templateName!=null&&templateName.equals("APPO_ECNWF")){
                        uivalidationstatus=UIValidationStatus.ENABLED;
                    }
                    if(templateName!=null&&templateName.equals("APPO_BOMCHANGEWF")){
                        uivalidationstatus=UIValidationStatus.ENABLED;
                    }
                    if(templateName!=null&&templateName.equals("APPO_DRAWINGCHANGEWF")){
                        uivalidationstatus=UIValidationStatus.ENABLED;
                    }
//                    if(templateName!=null&&templateName.equals("GenericECNWF")){
//                        uivalidationstatus=UIValidationStatus.ENABLED;
//                    }
                }else if (persistable != null && persistable instanceof WTChangeOrder2){
                    // 判断是否是ECN流程
                    uivalidationstatus=UIValidationStatus.ENABLED;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return uivalidationstatus;
    }


}
