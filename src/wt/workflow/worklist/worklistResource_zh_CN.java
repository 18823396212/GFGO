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
package wt.workflow.worklist;

import wt.util.resource.*;

@RBUUID("wt.workflow.worklist.worklistResource")
public final class worklistResource_zh_CN extends WTListResourceBundle {
    @RBEntry("资料错误。 {0}")
    @RBComment("Translators--this is a message for developer use only :-)")
    public static final String BETH_MSG = "142";

    @RBEntry("未找到工作项参数。")
    public static final String MISSING_PARAMETER = "1";

    @RBEntry("指示")
    public static final String INSTRUCTIONS = "2";

    @RBEntry("说明")
    public static final String DESCRIPTION = "2a";

    @RBEntry(":")
    @RBPseudo(false)
    public static final String COLON = "3";

    @RBEntry("变量名称")
    public static final String VARIABLE_NAME = "4";

    @RBEntry("值")
    public static final String VALUE = "5";

    @RBEntry("工作流任务")
    public static final String WORKFLOW_TASK_TITLE = "6";

    @RBEntry("工作项已成功完成。")
    public static final String WORKITEM_COMPLETE = "7";

    @RBEntry("由于以下错误该工作项无法完成")
    public static final String WORKITEM_COMPLETION_FAILED = "8";

    @RBEntry("目标对象不可用。")
    public static final String TARGET_OBJECT_GONE = "24";

    @RBEntry("任务完成")
    public static final String TASK_COMPLETE_BUTTON = "9";

    @RBEntry("进程")
    public static final String PROCESS_NAME = "10";

    @RBEntry("截止日期")
    public static final String DUE_DATE = "11";

    @RBEntry("进程启动者")
    public static final String PROCESS_INITIATOR = "12";

    @RBEntry("进程启动者:")
    public static final String PROCESS_INITIATOR_LABEL = "PROCESS_INITIATOR_LABEL";

    @RBEntry("进程已启动")
    public static final String PROCESS_INITIATED = "12.1";

    @RBEntry("工作负责人")
    public static final String ASSIGNEE = "13";

    @RBEntry("目标对象")
    public static final String TARGET_OBJECT = "14";

    @RBEntry("更新")
    public static final String UPDATE_LABEL = "19";

    @RBEntry("内容")
    public static final String CONTENT_LABEL = "20";

    @RBEntry("更新 {0} 内容")
    public static final String CONTENT_LINK_LABEL = "118";

    @RBEntry("进程指示")
    public static final String PROCESS_INSTRUCTIONS = "21";

    @RBEntry("进程说明")
    public static final String PROCESS_DESCRIPTION = "22";

    @RBEntry("该任务不再有效，因为它已完成")
    public static final String OBSOLETE_WORKITEM = "17";

    @RBEntry("该任务已暂停")
    public static final String SUSPENDED_WORKITEM_MSG = "81";

    @RBEntry("不可用")
    public static final String NOT_AVAILABLE = "79";

    @RBEntry("(已暂停)")
    public static final String SUSPENDED_WORKITEM_FLAG = "80";

    @RBEntry("是")
    public static final String YES = "82";

    @RBEntry("否")
    public static final String NO = "83";

    @RBEntry("真")
    public static final String TRUE = "134";

    @RBEntry("假")
    public static final String FALSE = "135";

    @RBEntry("删除")
    public static final String DELETE = "145";

    @RBEntry("更新")
    public static final String UPDATE = "146";

    @RBEntry("创建专用活动")
    public static final String CREATE_ADHOC_ACTIVITIES_LINK = "86";

    @RBEntry("定义项目 ")
    public static final String DEFINE_PROJECTS_LINK = "113";

    @RBEntry("为项目角色设置参与者")
    public static final String SET_UP_PARTICIPANTS_LINK = "112";

    @RBEntry("列{0}")
    @RBArgComment0("{0} - Number e.g. Column 1 Column 2")
    public static final String COLUMN = "138";

    @RBEntry("源")
    @RBComment("The data source of the worklist:  Current user's workitems or all workitems")
    public static final String WORKLIST_SOURCE = "139";

    @RBEntry("另存为")
    public static final String SAVE_AS = "140";

    @RBEntry("用作默认值")
    public static final String USE_AS_DEFAULT = "141";

    @RBEntry("用于现在")
    public static final String USE_NOW = "149";

    @RBEntry("工作项选择器")
    public static final String WORKITEM_SELECTOR = "154";

    @RBEntry("({0} 的委派)")
    public static final String DELEGATE_FOR = "167";

    /**
     * Buttons
     **/
    @RBEntry("接受")
    public static final String ACCEPT_BUTTON = "25";

    @RBEntry("重新分配")
    public static final String REASSIGN_BUTTON = "26";

    @RBEntry("更新截止日期")
    public static final String UPDATE_DUE_DATE_BUTTON = "27";

    @RBEntry("帮助")
    public static final String HELP_BUTTON = "28";

    @RBEntry("重新构建工作表")
    @RBComment("Translator--This item was changed from \"Create Work List\" to \"Rebuild Work List\"")
    public static final String CREATE_WORKLIST_BUTTON = "30";

    @RBEntry("提交")
    public static final String SUBMIT_BUTTON = "29";

    @RBEntry(" 确定 ")
    @RBComment("Translator--Please leave the leading spaces if this is a short (<5 char) word")
    public static final String OK_BUTTON = "148";

    @RBEntry("启动活动")
    public static final String START_ADHOC_ACTIVITIES_BUTTON = "87";

    @RBEntry("更多活动")
    public static final String MORE_ACTIVITIES_BUTTON = "108";

    @RBEntry("自动创建更改通告")
    public static final String AUTOMATE_FAST_TRACK_CHECKBOX = "225";

    @RBEntry("操作")
    public static final String ACTIONS_LABEL = "226";

    @RBEntry("查看简化进程图")
    public static final String VIEW_LIGHTWEIGHT_PROCESS = "227";

    @RBEntry("设置参与者")
    @RBComment("Label displayed for the Set Up Participants icon on the workflow task page")
    public static final String PARTICIPANTS_LABEL = "228";

    @RBEntry("参与者:")
    @RBComment("Participants inthe workflow task page")
    public static final String PARTICIPANTS = "229";

    /**
     * Labels & table headers --------------------------------------------------
     **/
    @RBEntry("排序方式")
    public static final String SORT_BY = "31";

    @RBEntry("分组方式")
    public static final String GROUP_BY = "32";

    @RBEntry("工作项")
    public static final String WORKITEM_COLUMN_HEADER = "65";

    @RBEntry("状况")
    public static final String STATUS_COLUMN_HEADER = "66";

    @RBEntry("已完成")
    public static final String COMPLETE_STATUS = "68";

    @RBEntry("失败")
    public static final String FAILED_STATUS = "69";

    @RBEntry("分配给")
    public static final String ASSIGNED_TO = "73";

    @RBEntry("所有工作项")
    public static final String ALL_WORKITEMS = "58";

    @RBEntry("已更新的工作项")
    public static final String UPDATED_WORKITEMS = "115";

    @RBEntry("开始的日期")
    public static final String START_TIME = "231";

    @RBEntry("完成日期")
    public static final String END_TIME = "232";

    @RBEntry("处理状况")
    public static final String PROCESS_STATUS = "233";

    /**
     * Titles ------------------------------------------------------------------
     **/
    @RBEntry("工作表")
    public static final String WORKLIST = "35";

    @RBEntry("工作表 < {0} >")
    @RBArgComment0("{0}-Worklist Owner's name or \"All Workitems\"")
    public static final String WORKLIST_TITLE = "132";

    @RBEntry("更新工作项截止日期")
    public static final String UPD_DUE_DATE_TASK_TITLE = "67";

    @RBEntry("重新分配工作项")
    public static final String REASSIGN_TASK_TITLE = "72";

    @RBEntry("接受工作项")
    public static final String ACCEPT_TASK_TITLE = "75";

    @RBEntry("更新参与角色")
    public static final String AUGMENT_LINK_TITLE = "78";

    @RBEntry("管理布局")
    public static final String MANAGE_LAYOUTS = "147";

    /**
     * ALT tags -----------------------------------------------------------------
     **/
    @RBEntry("启动进程")
    public static final String INITIATE_PROCESS = "101";

    @RBEntry("正在运行进程")
    public static final String RUNNING_PROCESSES = "102";

    @RBEntry("所有进程")
    public static final String ALL_PROCESSES = "103";

    @RBEntry("搜索进程")
    public static final String SEARCH_FOR_PROCESSES = "111";

    @RBEntry("已完成的进程")
    public static final String COMPLETED_PROCESSES = "104";

    @RBEntry("管理进程")
    public static final String MANAGE_PROCESSES = "105";

    /**
     * Text & Messages ----------------------------------------------------------
     **/
    @RBEntry("重新分配")
    public static final String REASSIGN = "33";

    @RBEntry("已重新分配")
    public static final String REASSIGNED = "34";

    @RBEntry("任务 - {0} {1}")
    @RBComment("Title display for the name of the task.")
    @RBArgComment0("The name of the task.  For example \"Submit Problem Report\"")
    @RBArgComment1("The primary business object display if available (may not be present).  Example: Problem Report: 001221 or something")
    public static final String TASK_NAME_TITLE = "163";

    @RBEntry("{0}-{1}")
    @RBComment("Display name of the WorkItem")
    @RBArgComment0("Name of the WFProcess, WorkItem belongs to.")
    @RBArgComment1("Name of the WFActivity of WorkItem.")
    public static final String WORKITEM_TITLE = "163.1";

    @RBEntry("任务内容")
    public static final String TASK_CONTENT = "164";

    @RBEntry("讨论论坛")
    public static final String DISCUSSION_FORUM = "165";

    @RBEntry("重新分配的历史记录")
    @RBComment("Label for the reassignment history table in a task details page.")
    public static final String ACTIVITY_REASSIGNMENT_HISTORY = "166";

    @RBEntry("在此活动中没有定义项目变量。")
    public static final String NO_PROJECTS_TO_DEFINE = "114";

    @RBEntry("备注")
    public static final String COMMENTS = "136";

    @RBEntry("您已经更改了您的用户的{0}到{1}验证。任务{2} 没有完成。")
    public static final String SIGNATURE_VALIDATION_FAILED = "137";

    @RBEntry("接受工作项 {0} 吗?")
    @RBArgComment0("{0} - identifying attribute of the workitem to be accepted")
    public static final String ACCEPT_WORKITEM = "36";

    @RBEntry("{0}不是有效日期...往回翻页并按显示的格式重新输入日期。")
    @RBArgComment0("{o} - The badly formatted date")
    public static final String INVALID_DATE = "70";

    @RBEntry("更新截止日期失败，错误的详细信息如下:")
    public static final String UPD_DUE_DATE_FAILED = "71";

    @RBEntry("重新分配失败，错误的详细信息如下:")
    public static final String REASSIGN_FAILED = "74";

    @RBEntry("要完成此任务必须至少选择一个路由。")
    public static final String NO_ROUTE_SELECTED = "76";

    @RBEntry("此工作项不能完成，因为它的父活动已暂停。")
    public static final String WORKITEM_IS_SUSPENDED = "77";

    @RBEntry("必须为变量{0}提供一个值。")
    @RBArgComment0("{0} - Name of the variable that was not supplied.")
    public static final String MISSING_REQUIRED_VARIABLE = "85";

    @RBEntry("{0}数据必须为{1}类型")
    @RBArgComment0("{0} - field name")
    @RBArgComment1("{1} - expected data type")
    public static final String INVALID_VARIABLE_TYPE = "18";

    @RBEntry("{0}不是有效的全名、用户 ID、组、角色或项目")
    @RBArgComment0("{0} - assignee name")
    public static final String INVALID_ASSIGNEE = "88";

    @RBEntry("没有选择工作项。使用复选框来标注所需的工作项。")
    public static final String NO_WORKITEMS_SELECTED = "119";

    @RBEntry("成功接受{0} 项工作任务。")
    @RBArgComment0("{0} - number of workitems accepted successfully")
    public static final String ACCEPT_SUCCESSFUL = "120";

    @RBEntry("成功接受工作项{0}。")
    @RBArgComment0("{0} - number of workitems accepted successfully (it should be 1--this is the singular case")
    public static final String ACCEPT_ONE_SUCCESSFUL = "121";

    @RBEntry("接受工作项失败，错误的详细信息如下: {0}")
    @RBArgComment0("{0} - the exception text of why the accepting  of the workitems failed for all selected workitems")
    public static final String ACCEPT_FAILED = "122";

    @RBEntry("未接受工作项{0}，因为有下列错误发生: {1}")
    @RBArgComment0("{0} - identity of the workitem which could not be accepted")
    @RBArgComment1("{1} - exception text of why the workitem wasn't accepted")
    public static final String ACCEPT_WORKITEM_FAILED = "123";

    @RBEntry("成功重新分配工作项{0} 。")
    @RBArgComment0("{0} - number of workitems reassigned successfully")
    public static final String REASSIGN_SUCCESSFUL = "124";

    @RBEntry("成功重新分配工作项{0} 。")
    @RBArgComment0("{0} - number of workitems reassigned successfully (it should be 1--this is the singular case)")
    public static final String REASSIGN_ONE_SUCCESSFUL = "125";

    @RBEntry("重新分配工作项失败，错误的详细信息如下: {0}")
    @RBArgComment0("{0} - the exception text of why the reassigning the workitems failed for all the selected workitems")
    public static final String REASSIGNMENT_FAILED = "126";

    @RBEntry("因为下列错误没有重新分配工作项{0}: {1}")
    @RBArgComment0("{0} - identity of the workitem which could not be reassigned")
    @RBArgComment1("{1} - exception text of why the workitem wasn't reasigned")
    public static final String REASSIGN_WORKITEM_FAILED = "127";

    @RBEntry("成功更新工作项{0}。")
    @RBArgComment0("{0} - number of workitems whose due date was successfully updated")
    public static final String UPDATE_DUE_DATE_SUCCESSFUL = "128";

    @RBEntry("成功更新工作项{0}。")
    @RBArgComment0("{0} - number of workitems whose due date was successfully updated (it should be 1--this is the singular case)")
    public static final String UPDATE_ONE_DUE_DATE_SUCCESSFUL = "129";

    @RBEntry("更新工作项失败，错误的详细信息如下: {0}")
    @RBArgComment0("{0} - the exception text of why the updating the workitems' due dates failed for all the selected workitems")
    public static final String UPDATE_DUE_DATE_FAILED = "130";

    @RBEntry("因为下列错误没有更新工作项{0}: {1}")
    @RBArgComment0("{0} - identity of the workitem whose due date could not be updated")
    @RBArgComment1("{1} - exception text of why the workitem wasn't updated")
    public static final String UPDATE_WORKITEM_DUE_DATE_FAILED = "131";

    @RBEntry("一个自定义的活动或变量脚本参考变量 \"{0}\"。没有带有该名称的变量定义在这个活动内。")
    @RBArgComment0("{0} - The user defined name of the variable that could not be found")
    public static final String MISSING_ACTIVITY_VARIABLE = "133";

    @RBEntry("成功删除布局\"{0}\" 。")
    @RBArgComment0("{0} - The name of the deleted layout")
    public static final String DELETED_LAYOUT = "143";

    @RBEntry("没有删除布局\"{0}\" 。")
    @RBArgComment0("{0} - The name of the layout that should have been deleted")
    public static final String LAYOUT_NOT_DELETED = "144";

    @RBEntry("在保存之前，请为该布局输入一个名称。")
    public static final String NO_LAYOUT_NAME = "150";

    @RBEntry("用于组的字段必须包含在该布局定义的列内。")
    public static final String GROUP_KEY_NOT_IN_LAYOUT = "151";

    @RBEntry("用于排序的字段必须包含在该布局定义的列内。")
    public static final String SORT_KEY_NOT_IN_LAYOUT = "152";

    @RBEntry("在该布局至少要定义一列。")
    public static final String NO_COLUMNS_DEFINED = "153";

    /**
     * Attribute table headers ---------------------------------------------------
     **/
    @RBEntry("ID")
    public static final String ID = "37";

    @RBEntry("角色")
    public static final String ROLE = "38";

    @RBEntry("所有者")
    public static final String OWNER = "39";

    @RBEntry("任务")
    public static final String TASK = "40";

    @RBEntry("源")
    public static final String SOURCE = "41";

    @RBEntry("优先级")
    public static final String PRIORITY = "42";

    @RBEntry("必需的")
    public static final String REQUIRED = "43";

    @RBEntry("操作者")
    public static final String ACTOR_NAME = "44";

    @RBEntry("已接受")
    public static final String ACCEPTED = "57";

    @RBEntry("已完成")
    public static final String COMPLETED = "45";

    @RBEntry("项目 ")
    public static final String PROJECT = "46";

    @RBEntry("类型")
    public static final String TYPE = "47";

    @RBEntry("状况 (任务)")
    public static final String STATUS = "84";

    @RBEntry("最后期限")
    public static final String DEADLINE = "48";

    @RBEntry("活动开始")
    public static final String ACTIVITY_START = "49";

    @RBEntry("活动名称")
    public static final String ACTIVITY_NAME = "50";

    @RBEntry("活动说明")
    public static final String ACTIVITY_DESCRIPTION = "51";

    @RBEntry("工作流名称")
    public static final String WORKFLOW_NAME = "54";

    @RBEntry("工作流最后期限")
    public static final String WORKFLOW_DEADLINE = "52";

    @RBEntry("工作流启动")
    public static final String WORKFLOW_START = "53";

    @RBEntry("工作流说明")
    public static final String WORKFLOW_DESCRIPTION = "55";

    @RBEntry("主题")
    @RBComment(" Translater - this changed from \"Primary Business Object\" to subject")
    public static final String PRIMARY_BUSINESS_OBJECT = "56";

    @RBEntry("类型")
    public static final String PRIMARY_BUSINESS_OBJECT_TYPE = "116";

    @RBEntry("状态")
    public static final String PRIMARY_BUSINESS_OBJECT_STATE = "117";


    @RBEntry("工作分配主题")
    @RBComment("Primary Business Object of an assignment(task). Used to display column in search table.")
    public static final String SEARCH_SUBJECT_OF_ASSIGNMENT = "SEARCH_SUBJECT_OF_ASSIGNMENT";

    /**
     * Workitem priorities -------------------------------------------------------
     **/
    @RBEntry("最高")
    public static final String PRIORITY_ONE = "59";

    @RBEntry("高")
    public static final String PRIORITY_TWO = "60";

    @RBEntry("普通")
    public static final String PRIORITY_THREE = "61";

    @RBEntry("低")
    public static final String PRIORITY_FOUR = "62";

    @RBEntry("最低")
    public static final String PRIORITY_FIVE = "63";

    /**
     * ---------------------------------------------------------------------------
     **/
    @RBEntry("ID")
    public static final String AD_HOC_ACTIVITY_ID = "89";

    @RBEntry("活动名称")
    public static final String AD_HOC_ACTIVITY_NAME = "90";

    @RBEntry("工作负责人")
    public static final String AD_HOC_ASSIGNEE = "91";

    @RBEntry("提供")
    public static final String AD_HOC_OFFER = "92";

    @RBEntry("持续时间")
    public static final String AD_HOC_DURATION = "93";

    @RBEntry("前置任务")
    public static final String AD_HOC_PREDECESSORS = "94";

    @RBEntry("任务")
    public static final String AD_HOC_TASK = "95";

    @RBEntry("指示:")
    public static final String AD_HOC_INSTRUCTIONS = "96";

    @RBEntry("发送电子邮件通知")
    public static final String AD_HOC_EMAIL_NOTIFICATION = "97";

    @RBEntry("活动已成功启动")
    public static final String AD_HOC_COMPLETE = "100";

    @RBEntry("活动当前正在运行")
    public static final String AD_HOC_IN_PROGRESS = "106";

    @RBEntry("不允许循环进程，请检查前置任务的循环相关性")
    public static final String AD_HOC_CYCLIC_ERROR = "107";

    @RBEntry("未找到活动。")
    public static final String AD_HOC_LIST_EMPTY = "109";

    @RBEntry("前置任务{0}超出界限")
    @RBArgComment0(" {0} is the id of the predecessor that was entered out of bounds.")
    public static final String AD_HOC_OUT_OF_BOUNDS = "110";

    @RBEntry("已成功创建专用活动。")
    public static final String Ad_HOC_CREATED_SUCCESSFULLY = "adhocCreatedSuccessfully";

    @RBEntry("*密码")
    public static final String PASSWORD = "PASSWORD";

    /**
     * Ad hoc labels --------------------------------------------------------------
     **/
    @RBEntry("项目:")
    public static final String AD_HOC_PROJECT_LABEL = "98";

    @RBEntry("父任务:")
    public static final String AD_HOC_PARENT_TASK_LABEL = "99";

    /**
     * Trouble: Exceptions & Messages ---------------------------------------------
     **/
    @RBEntry("无法检索选定工作项的父活动。")
    public static final String MISSING_PARENT = "64";

    /**
     * Notifications
     **/
    @RBEntry("项目名称:")
    public static final String NOTIFICATION_PROJECT_NAME = "200";

    @RBEntry("项目创建者:")
    public static final String NOTIFICATION_PROJECT_CREATOR = "201";

    @RBEntry("项目所有者:")
    public static final String NOTIFICATION_PROJECT_OWNER = "201a";

    @RBEntry("主办组织:")
    public static final String NOTIFICATION_PROJECT_HOST = "202";

    @RBEntry("项目说明:")
    public static final String NOTIFICATION_PROJECT_DESC = "203";

    @RBEntry("无 ")
    public static final String NOTIFICATION_NONE = "204";

    @RBEntry("您被分配了一个 <A HREF=\"{0}\">{1}</A>。")
    @RBComment(" This is a line of text that is seen at the top of a workflow notification email that contains a hyperlink to the workflow details page in a Windchill system.  The translation can be found in the <BODY> element of the 7.0 WCL10N template srcwtworkflowhtmltmplworkNotificationGeneral*.html")
    @RBArgComment0("This is the url to the details page for the workflow task.  This is not seen by the user.")
    @RBArgComment1("This is the type of the workflow task that is generated by the system. Examples of this are Submit and Approve.  The final produced string would look something like the following where Submit is the variable for arg1 and Submit task is a hyperlink; You have been assigned a Submit task.")
    public static final String NOTIFICATION_TASK_URL = "205";

    @RBEntry("您已作为 {0} 的代表接收此工作分配")
    public static final String NOTIFICATION_DELEGATE_FOR = "206";

    @RBEntry("该任务已经完成")
    public static final String TASK_COMPLETE = "230";

    /**
     * Online help ----------------------------------------------------------------
     **/
    @RBEntry("wt/workflow/help_zh_CN/NotificationHelp.html")
    public static final String PRIVATE_CONSTANT_0 = "Help/WorkNotification/MainHelp";

    @RBEntry("...已本地化...")
    public static final String PRIVATE_CONSTANT_1 = "TestString";

    /**
     * ---------------------------------------------------------------------------
     **/
    @RBEntry("布局名称不能包含字符 \"/\"。")
    public static final String INVALID_LAYOUT_NAME = "155";

    /**
     * Teams ----------------------------------------------------------------------
     **/
    @RBEntry("团队")
    public static final String TEAM = "156";

    @RBEntry("定义团队")
    public static final String DEFINE_TEAMS_LINK = "157";

    @RBEntry("在此活动中没有定义团队变量。")
    public static final String NO_TEAMS_TO_DEFINE = "158";

    @RBEntry("团队:")
    public static final String AD_HOC_TEAM_LABEL = "159";

    @RBEntry("团队名称:")
    public static final String NOTIFICATION_TEAM_NAME = "160";

    @RBEntry("团队创建者:")
    public static final String NOTIFICATION_TEAM_CREATOR = "161";

    @RBEntry("团队说明:")
    public static final String NOTIFICATION_TEAM_DESC = "162";

    /**
     * ---------------------------------------------------------------------------- Preferences
     **/
    @RBEntry("按启动排序")
    @RBComment("Localized name for Sort By Start worklist")
    public static final String PRIVATE_CONSTANT_2 = "SORT_BY_START_NAME";

    @RBEntry("“按启动顺序排序”首选项用于依据启动顺序来排序工作表。")
    @RBComment("Localized description for Sort By Start worklist")
    public static final String PRIVATE_CONSTANT_3 = "SORT_BY_START_DESC";

    @RBEntry("系统默认值")
    @RBComment("Localized name for System Default worklist")
    public static final String PRIVATE_CONSTANT_4 = "SYSTEM_DEFAULT_NAME";

    @RBEntry("系统默认工作表")
    @RBComment("Localized description for System Default worklist")
    public static final String PRIVATE_CONSTANT_5 = "SYSTEM_DEFAULT_DESC";

    /**
     * ---------------------------------------------------------------------------- Labels for Manage Layout page
     **/
    @RBEntry("组:")
    public static final String GROUP_LABEL = "220";

    @RBEntry("排序:")
    public static final String SORT_LABEL = "221";

    @RBEntry("定义的布局")
    public static final String DEFINED_LAYOUTS_LABEL = "222";

    @RBEntry("请求的升级状态")
    public static final String REQUESTED_PROMOTION_STATE = "223";

    @RBEntry("布局:")
    public static final String LAYOUTS_LABEL = "224";

    /**
     * ------------------------------------------------------ Preferences for assignments table
     **/
    @RBEntry("工作分配表格")
    @RBComment("Assignments table related preferences.")
    public static final String ASSIGNMENTS_TABLE_CAT_NAME = "ASSIGNMENTS_TABLE_CAT_NAME";

    @RBEntry("与工作分配表格相关的首选项。")
    public static final String ASSIGNMENTS_TABLE_CAT_DESCR = "ASSIGNMENTS_TABLE_CAT_DESCR";

    @RBEntry("“主页” --> “概述”中的“工作分配”表格")
    @RBComment("Preferences for Assignments table at Home-->Overview.")
    public static final String HOME_OVRV_ASSIGNMENTS_TABLE_CAT_NAME = "HOME_OVRV_ASSIGNMENTS_TABLE_CAT_NAME";

    @RBEntry("“主页” --> “概述”中的“工作分配”表格首选项。")
    public static final String HOME_OVRV_ASSIGNMENTS_TABLE_CAT_DESCR = "HOME_OVRV_ASSIGNMENTS_TABLE_CAT_DESCR";

    /**
     * removed QUERY_LIMIT_PREF ------------------------------------------------------
     **/
    @RBEntry("您将丢失未保存的更改。请保存此任务页面以保留所做的更改。")
    @RBComment("Confirmation message while setup participant table expansion on task detail page")
    public static final String UNSAVED_CHANGES_WARNING = "234";

    @RBEntry("至少需要一个活动名称和工作负责人才能启动活动。")
    @RBComment("Validation error message when Adhoc activities are started without specifying Name and Assignee")
    public static final String ADHOC_VALIDATION_ERROR1 = "235";

    @RBEntry("需要活动名称和工作负责人才能启动活动。")
    @RBComment("Validation error message when Adhoc activities are started without specifying Name and Assignee")
    public static final String ADHOC_VALIDATION_ERROR2 = "236";

    /*
     * Labels for Task Assistant
     */

    @RBEntry("路由选项")
    @RBComment("Task Routing Options")
    public static final String TASK_ROUTING = "237";

    @RBEntry("投票选项")
    @RBComment("Task voting options")
    public static final String TASK_VOTING = "238";

    @RBEntry("\"{0}\" 的未保存更改会丢失，是否要打开任务 \"{1}\"?")
    @RBComment("Task Assistant confirmation for existing tracked task assistant")
    public static final String TASKBAR_CONFIRMATION = "239";

    @RBEntry("名称")
    public static final String WORKITEM_NAME = "250";

    @RBEntry("上下文")
    public static final String WORKITEM_CONTEXT = "251";

    @RBEntry("任务助理")
    @RBComment("Title of task assistant")
    public static final String TASK_WINDOW_TITLE = "TASK_WINDOW_TITLE";

    @RBEntry("主题:")
    @RBComment("Label for task assistant PBO")
    public static final String TASK_WINDOW_SUBJECT = "TASK_WINDOW_SUBJECT";

    @RBEntry("任务:")
    @RBComment("Label for workitem name on task assistant")
    public static final String TASK_WINDOW_TASK = "TASK_WINDOW_TASK";

    @RBEntry("完成任务")
    @RBComment("Label for complete task button on task assistant")
    public static final String TASK_WINDOW_COMPLETE_TASK_BUTTON = "TASK_WINDOW_COMPLETE_TASK_BUTTON";

    @RBEntry("保存")
    @RBComment("Label for save button on task assistant")
    public static final String TASK_WINDOW_SAVE_BUTTON = "TASK_WINDOW_SAVE_BUTTON";

    @RBEntry("继续任务完成")
    @RBComment("Label for continue button on task assistant")
    public static final String TASK_WINDOW_CONTINUE_BUTTON = "TASK_WINDOW_CONTINUE_BUTTON";

    @RBEntry("已成功保存备注。")
    @RBComment("Message after comment is saved from task assistant")
    public static final String TASK_WINDOW_SAVE_MESSAGE = "TASK_WINDOW_SAVE_MESSAGE";

    @RBEntry("任务已成功完成。")
    @RBComment("Message after task is completed from task assistant")
    public static final String TASK_WINDOW_COMPLETE_MESSAGE = "TASK_WINDOW_COMPLETE_MESSAGE";

    @RBEntry("有未保存的更改，是否关闭任务助理?")
    @RBComment("Confirmation message to close task assistant")
    public static final String TASK_WINDOW_CLOSE_CONFIRMATION_MESSAGE = "TASK_WINDOW_CLOSE_CONFIRMATION_MESSAGE";

    /*
     * Labels for task details page
     */

    @RBEntry("处理状况")
    public static final String workitem_routingStatus_description = "workitem.routingStatus.description";

    @RBEntry("处理状况")
    public static final String workitem_routingStatus_tooltip = "workitem.routingStatus.tooltip";

    @RBEntry("newproject.gif")
    @RBPseudo(false)
    @RBComment("DO NOT TRANSLATE")
    public static final String workitem_routingStatus_icon = "workitem.routingStatus.icon";

    @RBEntry("详细信息")
    @RBComment("The Details tab on the info page for WorkItem")
    public static final String workitem_taskDetailsDetails_description = "object.taskDetailsDetails.description";

    @RBEntry("模板视图")
    @RBComment("The main tab on the info page for WorkItem which will render task form template")
    public static final String workitem_taskFormTemplateDetails_description = "object.taskFormTemplateDetails.description";

    @RBEntry("相关")
    @RBComment("The Others tab on the info page for WorkItem")
    public static final String workitem_taskDetailsOthers_description = "object.taskDetailsOthers.description";

    @RBEntry("属性")
    @RBComment("The Attributes menu on the info page for WorkItem")
    public static final String workitem_attributes_description = "workitem.attributes.description";

    @RBEntry("笔记本")
    @RBComment("The Notebook menu on the info page for WorkItem")
    public static final String workitem_notebook_description = "workitem.notebook.description";

    @RBEntry("讨论")
    @RBComment("The Discussions menu on the info page for WorkItem")
    public static final String workitem_discussions_description = "workitem.discussions.description";

    @RBEntry("设置参与者")
    @RBComment("The Set Up Participant tab on the info page for WorkItem")
    public static final String workitem_taskDetailsSetupParticipant_description = "object.taskDetailsSetupParticipant.description";

    @RBEntry("专用活动")
    @RBComment("The Adhoc Activities tab on the info page for WorkItem")
    public static final String workitem_taskDetailsAdhoc_description = "object.taskDetailsAdhoc.description";

    @RBEntry("专用活动")
    @RBComment("The Adhoc Activities customization action")
    public static final String workitem_adhocActivities_description = "workitem.adhocActivities.description";

    @RBEntry("设置参与者")
    @RBComment("Set Up PArticipant customization action")
    public static final String workitem_setupParticipant_description = "workitem.setupParticipant.description";

    @RBEntry("insert_multi_rows_below.gif")
    @RBPseudo(false)
    public static final String workitem_addMultipleObjects_icon = "workitem.addMultipleObjects.icon";

    @RBEntry("添加 5 行")
    @RBComment("Action to add 5 empty rows")
    public static final String workitem_addMultipleObjects_description = "workitem.addMultipleObjects.description";

    @RBEntry("添加 5 行")
    public static final String workitem_addMultipleObjects_tooltip = "workitem.addMultipleObjects.tooltip";

    @RBEntry("特殊指示")
    @RBComment("Special instructions is a special Workflow veriable which is used in Change related Workflow.")
    public static final String SPECIAL_INSTRUCTIONS_LABEL = "special_instructions_label";

    @RBEntry("模板视图")
    @RBComment("Label for Task Form Template action")
    public static final String workitem_taskFormTemplate_description = "workitem.taskFormTemplate.description";

    @RBEntry("这是一个“专用活动”任务。请使用“专用活动”选项卡来创建和分配新的专用活动。")
    @RBComment("Inline help contents for ad hoc type of activity.")
    public static final String ADHOC_INLINE_HELP = "ADHOC_INLINE_HELP";

    @RBEntry("这是一个“设置参与者”任务。请使用“设置参与者”选项卡来将参与者分配给此进程中使用的不同角色。")
    @RBComment("Inline help contents for set up participant type of activity.")
    public static final String SETUP_INLINE_HELP = "SETUP_INLINE_HELP";

    @RBEntry("使用针对此任务类型和主要业务对象类型定义的任务表单模板来呈现此任务。")
    @RBComment("Inline help contents if task form template preference is set to true.")
    public static final String TEMPLATE_VIEW_INLINE_HELP = "TEMPLATE_VIEW_INLINE_HELP";

    @RBEntry("此任务已被设置为“专用活动”任务和“设置参与者”任务。请使用“专用活动”选项卡来创建和分配新的专用活动。请使用“设置参与者”选项卡来将参与者分配给此进程中使用的不同角色。")
    @RBComment("Inline help contents for set up as well as ad hoc type of activity.")
    public static final String SETUP_ADHOC_INLINE_HELP = "SETUP_ADHOC_INLINE_HELP";


	@RBEntry("生命周期历史记录")
    public static final String PBOLEGACYHISTORY = "history.pboLegacyLCHistory.description";

	@RBEntry("小版本历史记录")
	public static final String PBOITERATIONHISTORY = "history.pboIterationHistory.description";

	@RBEntry("成熟度历史记录")
    public static final String PBOMATURITYHISTORY = "history.pboMaturityHistory.description";

	@RBEntry("受影响对象")
    public static final String TestAffectedData = "change.TestAffectedData.description";

	@RBEntry("更改请求")
	public static final String TestRelatedChangeRequest = "change.TestRelatedChangeRequest.description";

	@RBEntry("受影响的成品")
	public static final String  TestAffectedEndItemsTable  = "change.TestAffectedEndItemsTable.description";

	@RBEntry("附件")
	public static final String  TestAttachment  = "change.TestAttachment.description";


	@RBEntry("问题和超差")
	public static final String TestRelatedChangeIssues = "change.TestRelatedChangeIssues.description";

	@RBEntry("受更改通告的影响")
	public static final String  TestRelatedChangeNotice  = "change.TestRelatedChangeNotice.description";

	@RBEntry("由更改通告产生")
	public static final String  TestResultingfromChangeNotice  = "change.TestResultingfromChangeNotice.description";


	@RBEntry("版本历史记录")
	public static final String PBOVERSIONHISTORY = "history.pboVersionHistory.description";

	@RBEntry("主题属性")
	public static final String WORKFLOW_TASK_PBO_ATTRIBUTES = "object.workflowTaskPboAttributes.description";

	@RBEntry("对主题的操作")
	public static final String WORKFLOW_TASK_PBO_ACTIONS = "workitem.workflowTaskPboAction.description";

	@RBEntry("任务完成按钮")
	public static final String saveComplete = "workitem.saveComplete.description";

	@RBEntry("任务变量")
	public static final String workItemActivityVariables = "workitem.workItemActivityVariables.description";

	@RBEntry("任务完成")
	public static final String signOffComponent = "workitem.workItemSignOffComponent.description";

	@RBEntry("任务变量")
	public static final String CUSTOM_VARIABLES_PANEL = "CUSTOM_VARIABLES_PANEL";

	@RBEntry("任务完成")
	public static final String TASK_SIGN_OFF_PANEL = "TASK_SIGN_OFF_PANEL";

    //add by lzy 20191120 start
    @RBEntry("BOM变更报表")
    public static final String workitem_bomChangeReport_description = "workitem.bomChangeReport.description";
    //add by lzy 20191120 end

}
