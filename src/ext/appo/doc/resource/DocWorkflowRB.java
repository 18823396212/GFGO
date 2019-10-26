package ext.appo.doc.resource;

import wt.util.resource.RBEntry;
import wt.util.resource.RBUUID;
import wt.util.resource.WTListResourceBundle;

@RBUUID("ext.appo.doc.resource.DocWorkflowRB")
public class DocWorkflowRB extends WTListResourceBundle {
	
	 @RBEntry("Confirm To Submit Doc Sign ？")
	    public static final String confirmToDocSign = "confirmToDocSign";
	    @RBEntry("Start DocSign Workflow Failure")
	    public static final String START_DOCSIGN_ERR = "START_DOCSIGN_ERR";
	    @RBEntry("Start DocSign Workflow Successfully")
	    public static final String START_DOCSIGN_MSG = "START_DOCSIGN_MSG";
	    @RBEntry("Submit Doc Archived Workflow")
	    public static final String PRIVATE_CONSTANT_1 = "cusAppoDoc.appoDocArchived.description";
	    @RBEntry("Submit Doc Archived Workflow")
	    public static final String PRIVATE_CONSTANT_2 = "cusAppoDoc.appoDocArchived.tooltip";
	    @RBEntry("Submit Doc Archived Workflow")
	    public static final String PRIVATE_CONSTANT_3 = "cusAppoDoc.appoDocArchived.title";
	    @RBEntry("Submit Doc Sign Workflow")
	    public static final String PRIVATE_CONSTANT_4 = "generic.submitDocSignWorkflow.description";
	    @RBEntry("Submit Doc Sign Workflow")
	    public static final String PRIVATE_CONSTANT_5 = "generic.submitDocSignWorkflow.tooltip";
	    @RBEntry("Submit Doc Sign Workflow")
	    public static final String PRIVATE_CONSTANT_6 = "generic.submitDocSignWorkflow.title";

	  //================================================================发送DCC————ACTION================================================================
	    @RBEntry("Manually send DCC files")
	    public static final String sendDcc_manualSendPartDcc_description = "sendDcc.manualSendPartDcc.description";
	    @RBEntry("Manually send DCC files")
	    public static final String sendDcc_manualSendPartDcc_tooltip = "sendDcc.manualSendPartDcc.tooltip";
	    @RBEntry("Manually send DCC files")
	    public static final String sendDcc_manualSendPartDcc_title = "sendDcc.manualSendPartDcc.title";
	    
	    @RBEntry("Manually send DCC files")
	    public static final String sendDcc_manualSendDocDcc_description = "sendDcc.manualSendDocDcc.description";
	    @RBEntry("Manually send DCC files")
	    public static final String sendDcc_manualSendDocDcc_tooltip = "sendDcc.manualSendDocDcc.tooltip";
	    @RBEntry("Manually send DCC files")
	    public static final String sendDcc_manualSendDocDcc_title = "sendDcc.manualSendDocDcc.title";

	    
	    @RBEntry("Send all Part")
	    public static final String sendDcc_CountPartSendDcc_description = "sendDcc.CountPartSendDcc.description";
	    @RBEntry("Send all Part")
	    public static final String sendDcc_CountPartSendDcc_tooltip = "sendDcc.CountPartSendDcc.tooltip";
	    @RBEntry("Send all Part")
	    public static final String sendDcc_CountPartSendDcc_title = "sendDcc.CountPartSendDcc.title";
	    
	    
	    @RBEntry("Send all Document")
	    public static final String sendDcc_CountDocSendDcc_description = "sendDcc.CountDocSendDcc.description";
	    @RBEntry("Send all Document")
	    public static final String sendDcc_CountDocSendDcc_tooltip = "sendDcc.CountDocSendDcc.tooltip";
	    @RBEntry("Send all Document")
	    public static final String sendDcc_CountDocSendDcc_title = "sendDcc.CountDocSendDcc.title";
	    
	  //=========================================================发送DCC------END===================================================================

	    
	    public DocWorkflowRB() {
	    }

}
