package ext.appo.doc.uploadDoc.validator;

import java.util.HashSet;
import java.util.Set;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;
import wt.doc.WTDocument;
import wt.fc.QueryResult;
import wt.fc.WTReference;
import wt.part.WTPart;
import wt.util.WTException;
import wt.workflow.WfException;
import wt.workflow.work.WorkItem;
import wt.workflow.work.WorkflowHelper;


public class ManualSendDCCValidator extends DefaultSimpleValidationFilter {


    public UIValidationStatus preValidateAction(UIValidationKey uivalidationkey, UIValidationCriteria uivalidationcriteria){
        UIValidationStatus uivalidationstatus = UIValidationStatus.HIDDEN;
        WTReference contextObj = uivalidationcriteria.getContextObject();
		Object obj = contextObj.getObject();
        boolean isAdmin = uivalidationcriteria.isSiteAdmin() || uivalidationcriteria.isOrgAdmin();
            // 组织、站点管理员以及指定组成员有权限
        if (isAdmin) {
        	if(obj instanceof WTDocument){
        		 WTDocument doc = (WTDocument) obj;	
        		 try {
        			Set<WorkItem> set = new HashSet<>(); 
					QueryResult qr = WorkflowHelper.service.getWorkItems(doc);
					System.out.println("QueryResult======================"+qr);
					while(qr.hasMoreElements()){
				        WorkItem wi = (WorkItem)qr.nextElement();
				        set.add(wi);
				     }
					System.out.println("set============================"+set);
					if(set.isEmpty()) {
						uivalidationstatus = UIValidationStatus.DISABLED;
					}else {
						uivalidationstatus = UIValidationStatus.ENABLED;
					}
				} catch (WfException e) {
					e.printStackTrace();
				} catch (WTException e) {
					e.printStackTrace();
				}
            	 
   		    }else if(obj instanceof WTPart){
   		    	 WTPart part = (WTPart)obj;
   		    	 try {
   		    		Set<WorkItem> set = new HashSet<>(); 
 					QueryResult qr = WorkflowHelper.service.getWorkItems(part);
 					while(qr.hasMoreElements()){
				        WorkItem wi = (WorkItem)qr.nextElement();
				        set.add(wi);
				     }
					System.out.println("set============================"+set);
					if(set.isEmpty()) {
						uivalidationstatus = UIValidationStatus.DISABLED;
					}else {
						uivalidationstatus = UIValidationStatus.ENABLED;
					}
 				} catch (WfException e) {
 					e.printStackTrace();
 				} catch (WTException e) {
 					e.printStackTrace();
 				}
   		    	 
   		    }
         }
        
        return uivalidationstatus;
    }
}
