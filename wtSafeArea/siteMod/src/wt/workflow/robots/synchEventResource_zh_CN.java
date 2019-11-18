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
public final class synchEventResource_zh_CN extends WTListResourceBundle {
   @RBEntry("检入")
   public static final String PRIVATE_CONSTANT_0 = "*/wt.vc.wip.WorkInProgressServiceEvent/POST_CHECKIN";

   @RBEntry("检出")
   public static final String PRIVATE_CONSTANT_1 = "*/wt.vc.wip.WorkInProgressServiceEvent/POST_CHECKOUT";

   @RBEntry("创建")
   public static final String PRIVATE_CONSTANT_2 = "*/wt.fc.PersistenceManagerEvent/POST_STORE";

   @RBEntry("删除")
   public static final String PRIVATE_CONSTANT_3 = "*/wt.fc.PersistenceManagerEvent/POST_DELETE";

   @RBEntry("修改")
   public static final String PRIVATE_CONSTANT_4 = "*/wt.fc.PersistenceManagerEvent/POST_MODIFY";

   @RBEntry("锁定")
   public static final String PRIVATE_CONSTANT_5 = "*/wt.locks.LockServiceEvent/POST_LOCK";

   @RBEntry("解锁")
   public static final String PRIVATE_CONSTANT_6 = "*/wt.locks.LockServiceEvent/POST_UNLOCK";

   @RBEntry("状态更改")
   public static final String PRIVATE_CONSTANT_7 = "*/wt.lifecycle.LifeCycleServiceEvent/STATE_CHANGE";

   @RBEntry("撤消检出")
   public static final String PRIVATE_CONSTANT_8 = "*/wt.vc.wip.WorkInProgressServiceEvent/POST_UNDO_CHECKOUT";

   @RBEntry("新建版本")
   public static final String PRIVATE_CONSTANT_9 = "*/wt.vc.VersionControlServiceEvent/NEW_VERSION";

   @RBEntry("更改域")
   public static final String PRIVATE_CONSTANT_10 = "*/wt.admin.AdministrativeDomainManagerEvent/POST_CHANGE_DOMAIN";

   @RBEntry("更改文件夹")
   public static final String PRIVATE_CONSTANT_11 = "*/wt.folder.FolderServiceEvent/POST_CHANGE_FOLDER";

   @RBEntry("进程已创建")
   public static final String PRIVATE_CONSTANT_12 = "*/wt.workflow.engine.WfEngineServiceEvent/PROCESS_CREATED";

   @RBEntry("进程状态已经更改")
   public static final String PRIVATE_CONSTANT_13 = "*/wt.workflow.engine.WfEngineServiceEvent/PROCESS_STATE_CHANGED";

   @RBEntry("进程上下文已经更改")
   public static final String PRIVATE_CONSTANT_14 = "*/wt.workflow.engine.WfEngineServiceEvent/PROCESS_CONTEXT_CHANGED";

   @RBEntry("活动分配已经更改")
   public static final String PRIVATE_CONSTANT_15 = "*/wt.workflow.engine.WfEngineServiceEvent/ACTIVITY_ASSIGNMENT_CHANGED";

   @RBEntry("活动上下文已经更改")
   public static final String PRIVATE_CONSTANT_16 = "*/wt.workflow.engine.WfEngineServiceEvent/ACTIVITY_CONTEXT_CHANGED";

   @RBEntry("活动结果已经更改")
   public static final String PRIVATE_CONSTANT_17 = "*/wt.workflow.engine.WfEngineServiceEvent/ACTIVITY_RESULT_CHANGED";

   @RBEntry("活动状态已经更改")
   public static final String PRIVATE_CONSTANT_18 = "*/wt.workflow.engine.WfEngineServiceEvent/ACTIVITY_STATE_CHANGED";

   @RBEntry("进程结果已经更改")
   public static final String PRIVATE_CONSTANT_19 = "*/wt.workflow.engine.WfEngineServiceEvent/PROCESS_RESULT_CHANGED";

   @RBEntry("执行错误")
   public static final String PRIVATE_CONSTANT_20 = "*/wt.workflow.engine.WfEngineServiceEvent/EXECUTION_ERROR";

   @RBEntry("无存货")
   public static final String PRIVATE_CONSTANT_21 = "*/wt.workflow.engine.WfCustomEvent/OUT_OF_STOCK";

   @RBEntry("溢出")
   public static final String PRIVATE_CONSTANT_22 = "*/wt.fv.FvServiceEvent/OVERFLOW";

   @RBEntry("更改事项形式化")
   public static final String PRIVATE_CONSTANT_23 = "*/wt.change2.ChangeService2Event/ISSUE_FORMALIZED";

   @RBEntry("更改事项非形式化")
   public static final String PRIVATE_CONSTANT_24 = "*/wt.change2.ChangeService2Event/ISSUE_UNFORMALIZED";

   @RBEntry("更改活动状态更改")
   public static final String PRIVATE_CONSTANT_25 = "*/wt.change2.ChangeService2Event/CA_STATE_CHANGED";

   @RBEntry("更改通告状态更改")
   public static final String PRIVATE_CONSTANT_26 = "*/wt.change2.ChangeService2Event/CN_STATE_CHANGED";

   @RBEntry("更改实施")
   public static final String PRIVATE_CONSTANT_27 = "*/wt.change2.ChangeService2Event/CHANGE_IMPLEMENTATION";
   
   @RBEntry("父项更改对象状态更改")
   public static final String PARENT_CHANGE_OBJECT_STATE_CHANGE = "*/wt.change2.ChangeService2Event/PARENT_CHANGE_OBJECT_STATE_CHANGE"; 

   @RBEntry("子项更改对象状态更改")
   public static final String CHILD_CHANGE_OBJECT_STATE_CHANGE = "*/wt.change2.ChangeService2Event/CHILD_CHANGE_OBJECT_STATE_CHANGE"; 

   @RBEntry("企业系统集成 版本完整")
   public static final String PRIVATE_CONSTANT_28 = "*/com.ptc.windchill.esi.wf.ESIResultEvent/ESI_RELEASE_COMPLETE";

   @RBEntry("公布版本")
   public static final String PRIVATE_CONSTANT_29 = "*/com.ptc.windchill.esi.svc.ESIServiceEvent/POST_RELEASE";

   @RBEntry("创建 CAPA 请求")
   public static final String PRIVATE_CONSTANT_30 = "*/com.ptc.qualitymanagement.capa.CAPAServiceEvent/CAPA_REQUEST_CREATED";

   @RBEntry("完成 CAPA 活动")
   public static final String PRIVATE_CONSTANT_31 = "*/com.ptc.qualitymanagement.capa.CAPAServiceEvent/CAPA_ACTIVITY_COMPLETED";

   @RBEntry("CAPACHANGEACTIVITY 状态更改")
   public static final String PRIVATE_CONSTANT_32 = "*/com.ptc.qualitymanagement.capa.CAPAServiceEvent/CAPACHANGEACTIVITY_STATE_CHANGE";

   @RBEntry("创建 NC")
   public static final String PRIVATE_CONSTANT_33 = "*/com.ptc.qualitymanagement.nc.NCServiceEvent/NC_CREATED";

   @RBEntry("NC 处置操作状态更改")
   public static final String PRIVATE_CONSTANT_34 = "*/com.ptc.qualitymanagement.nc.NCServiceEvent/NC_DISPOSITIONACTION_STATE_CHANGE";

   @RBEntry("NC DISPOSITIONACTION 处置状态已完成")
   public static final String PRIVATE_CONSTANT_35 = "*/com.ptc.qualitymanagement.nc.NCServiceEvent/NC_DISPOSITIONACTION_DISPOSITION_STATE_COMPLETED";

   @RBEntry("投诉已创建")
   public static final String PRIVATE_CONSTANT_36 = "*/com.ptc.qualitymanagement.cem.CustomerExperienceServiceEvent/COMPLAINT_CREATED";

   @RBEntry("投诉评估已完成")
   public static final String PRIVATE_CONSTANT_37 = "*/com.ptc.qualitymanagement.cem.CustomerExperienceServiceEvent/COMPLAINT_EVALUATION_COMPLETED";

   @RBEntry("CEM 活动已完成")
   public static final String PRIVATE_CONSTANT_38 = "*/com.ptc.qualitymanagement.cem.CustomerExperienceServiceEvent/CEM_ACTIVITY_COMPLETED";

   @RBEntry("CAPA 更改通告已完成")
   public static final String PRIVATE_CONSTANT_39 = "*/com.ptc.qualitymanagement.capa.CAPAServiceEvent/CAPA_CHANGE_NOTICE_COMPLETED";

   @RBEntry("CORRELATIONLINK路由更改")
   public static final String PRIVATE_CONSTANT_40 = "*/wt.workflow.engine.WfCustomEvent/CORRELATION_LINK_CHANGE";

}
