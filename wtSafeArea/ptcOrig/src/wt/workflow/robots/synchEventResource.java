/* bcwti
 *
 * Copyright (c) 2010 Parametric Technology Corporation (PTC). All Rights Reserved.
 *
 * This software is the confidential and proprietary information of PTC
 * and is subject to the terms of a software license agreement. You shall
 * not disclose such confidential information and shall use it only in accordance
 * with the terms of the license agreement.
 *
 * ecwti
 */
package wt.workflow.robots;

import wt.util.resource.*;

@RBUUID("wt.workflow.robots.synchEventResource")
public final class synchEventResource extends WTListResourceBundle {
   @RBEntry("CHECKIN")
   public static final String PRIVATE_CONSTANT_0 = "*/wt.vc.wip.WorkInProgressServiceEvent/POST_CHECKIN";

   @RBEntry("CHECKOUT")
   public static final String PRIVATE_CONSTANT_1 = "*/wt.vc.wip.WorkInProgressServiceEvent/POST_CHECKOUT";

   @RBEntry("CREATE")
   public static final String PRIVATE_CONSTANT_2 = "*/wt.fc.PersistenceManagerEvent/POST_STORE";

   @RBEntry("DELETE")
   public static final String PRIVATE_CONSTANT_3 = "*/wt.fc.PersistenceManagerEvent/POST_DELETE";

   @RBEntry("MODIFY")
   public static final String PRIVATE_CONSTANT_4 = "*/wt.fc.PersistenceManagerEvent/POST_MODIFY";

   @RBEntry("LOCK")
   public static final String PRIVATE_CONSTANT_5 = "*/wt.locks.LockServiceEvent/POST_LOCK";

   @RBEntry("UNLOCK")
   public static final String PRIVATE_CONSTANT_6 = "*/wt.locks.LockServiceEvent/POST_UNLOCK";

   @RBEntry("STATE CHANGE")
   public static final String PRIVATE_CONSTANT_7 = "*/wt.lifecycle.LifeCycleServiceEvent/STATE_CHANGE";

   @RBEntry("UNDO CHECKOUT")
   public static final String PRIVATE_CONSTANT_8 = "*/wt.vc.wip.WorkInProgressServiceEvent/POST_UNDO_CHECKOUT";

   @RBEntry("NEW VERSION")
   public static final String PRIVATE_CONSTANT_9 = "*/wt.vc.VersionControlServiceEvent/NEW_VERSION";

   @RBEntry("CHANGE DOMAIN")
   public static final String PRIVATE_CONSTANT_10 = "*/wt.admin.AdministrativeDomainManagerEvent/POST_CHANGE_DOMAIN";

   @RBEntry("CHANGE FOLDER")
   public static final String PRIVATE_CONSTANT_11 = "*/wt.folder.FolderServiceEvent/POST_CHANGE_FOLDER";

   @RBEntry("PROCESS CREATED")
   public static final String PRIVATE_CONSTANT_12 = "*/wt.workflow.engine.WfEngineServiceEvent/PROCESS_CREATED";

   @RBEntry("PROCESS STATE CHANGED")
   public static final String PRIVATE_CONSTANT_13 = "*/wt.workflow.engine.WfEngineServiceEvent/PROCESS_STATE_CHANGED";

   @RBEntry("PROCESS CONTEXT CHANGED")
   public static final String PRIVATE_CONSTANT_14 = "*/wt.workflow.engine.WfEngineServiceEvent/PROCESS_CONTEXT_CHANGED";

   @RBEntry("ACTIVITY ASSIGNMENT CHANGED")
   public static final String PRIVATE_CONSTANT_15 = "*/wt.workflow.engine.WfEngineServiceEvent/ACTIVITY_ASSIGNMENT_CHANGED";

   @RBEntry("ACTIVITY CONTEXT CHANGED")
   public static final String PRIVATE_CONSTANT_16 = "*/wt.workflow.engine.WfEngineServiceEvent/ACTIVITY_CONTEXT_CHANGED";

   @RBEntry("ACTIVITY RESULT CHANGED")
   public static final String PRIVATE_CONSTANT_17 = "*/wt.workflow.engine.WfEngineServiceEvent/ACTIVITY_RESULT_CHANGED";

   @RBEntry("ACTIVITY STATE CHANGED")
   public static final String PRIVATE_CONSTANT_18 = "*/wt.workflow.engine.WfEngineServiceEvent/ACTIVITY_STATE_CHANGED";

   @RBEntry("PROCESS RESULT CHANGED")
   public static final String PRIVATE_CONSTANT_19 = "*/wt.workflow.engine.WfEngineServiceEvent/PROCESS_RESULT_CHANGED";

   @RBEntry("EXECUTION ERROR")
   public static final String PRIVATE_CONSTANT_20 = "*/wt.workflow.engine.WfEngineServiceEvent/EXECUTION_ERROR";

   @RBEntry("OUT_OF_STOCK")
   public static final String PRIVATE_CONSTANT_21 = "*/wt.workflow.engine.WfCustomEvent/OUT_OF_STOCK";

   @RBEntry("OVERFLOW")
   public static final String PRIVATE_CONSTANT_22 = "*/wt.fv.FvServiceEvent/OVERFLOW";

   @RBEntry("CHANGE ISSUE FORMALIZED")
   public static final String PRIVATE_CONSTANT_23 = "*/wt.change2.ChangeService2Event/ISSUE_FORMALIZED";

   @RBEntry("CHANGE ISSUE UNFORMALIZED")
   public static final String PRIVATE_CONSTANT_24 = "*/wt.change2.ChangeService2Event/ISSUE_UNFORMALIZED";

   @RBEntry("CHANGE ACTIVITY STATE CHANGE")
   public static final String PRIVATE_CONSTANT_25 = "*/wt.change2.ChangeService2Event/CA_STATE_CHANGED";

   @RBEntry("CHANGE NOTICE STATE CHANGE")
   public static final String PRIVATE_CONSTANT_26 = "*/wt.change2.ChangeService2Event/CN_STATE_CHANGED";

   @RBEntry("CHANGE IMPLEMENTATION")
   public static final String PRIVATE_CONSTANT_27 = "*/wt.change2.ChangeService2Event/CHANGE_IMPLEMENTATION";
   
   @RBEntry("PARENT CHANGE OBJECT STATE CHANGE")   
   public static final String PARENT_CHANGE_OBJECT_STATE_CHANGE = "*/wt.change2.ChangeService2Event/PARENT_CHANGE_OBJECT_STATE_CHANGE"; 

   @RBEntry("CHILD CHANGE OBJECT STATE CHANGE")   
   public static final String CHILD_CHANGE_OBJECT_STATE_CHANGE = "*/wt.change2.ChangeService2Event/CHILD_CHANGE_OBJECT_STATE_CHANGE"; 

   @RBEntry("ESI_RELEASE_COMPLETE")
   public static final String PRIVATE_CONSTANT_28 = "*/com.ptc.windchill.esi.wf.ESIResultEvent/ESI_RELEASE_COMPLETE";

   @RBEntry("POST_RELEASE")
   public static final String PRIVATE_CONSTANT_29 = "*/com.ptc.windchill.esi.svc.ESIServiceEvent/POST_RELEASE";

   @RBEntry("CAPA REQUEST CREATED")
   public static final String PRIVATE_CONSTANT_30 = "*/com.ptc.qualitymanagement.capa.CAPAServiceEvent/CAPA_REQUEST_CREATED";

   @RBEntry("CAPA ACTIVITY COMPLETED")
   public static final String PRIVATE_CONSTANT_31 = "*/com.ptc.qualitymanagement.capa.CAPAServiceEvent/CAPA_ACTIVITY_COMPLETED";

   @RBEntry("CAPACHANGEACTIVITY STATE CHANGE")
   public static final String PRIVATE_CONSTANT_32 = "*/com.ptc.qualitymanagement.capa.CAPAServiceEvent/CAPACHANGEACTIVITY_STATE_CHANGE";

   @RBEntry("NC CREATED")
   public static final String PRIVATE_CONSTANT_33 = "*/com.ptc.qualitymanagement.nc.NCServiceEvent/NC_CREATED";

   @RBEntry("NC DISPOSITIONACTION STATE CHANGE")
   public static final String PRIVATE_CONSTANT_34 = "*/com.ptc.qualitymanagement.nc.NCServiceEvent/NC_DISPOSITIONACTION_STATE_CHANGE";

   @RBEntry("NC DISPOSITIONACTION DISPOSITION STATE COMPLETED")
   public static final String PRIVATE_CONSTANT_35 = "*/com.ptc.qualitymanagement.nc.NCServiceEvent/NC_DISPOSITIONACTION_DISPOSITION_STATE_COMPLETED";

   @RBEntry("COMPLAINT CREATED")
   public static final String PRIVATE_CONSTANT_36 = "*/com.ptc.qualitymanagement.cem.CustomerExperienceServiceEvent/COMPLAINT_CREATED";

   @RBEntry("COMPLAINT EVALUATION COMPLETED")
   public static final String PRIVATE_CONSTANT_37 = "*/com.ptc.qualitymanagement.cem.CustomerExperienceServiceEvent/COMPLAINT_EVALUATION_COMPLETED";

   @RBEntry("CEM ACTIVITY COMPLETED")
   public static final String PRIVATE_CONSTANT_38 = "*/com.ptc.qualitymanagement.cem.CustomerExperienceServiceEvent/CEM_ACTIVITY_COMPLETED";

   @RBEntry("CAPA CHANGE NOTICE COMPLETED")
   public static final String PRIVATE_CONSTANT_39 = "*/com.ptc.qualitymanagement.capa.CAPAServiceEvent/CAPA_CHANGE_NOTICE_COMPLETED";
}
