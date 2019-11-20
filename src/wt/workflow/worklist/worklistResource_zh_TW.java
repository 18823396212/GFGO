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
public final class worklistResource_zh_TW extends WTListResourceBundle {
    @RBEntry("發生錯誤。 {0}")
    @RBComment("Translators--this is a message for developer use only :-)")
    public static final String BETH_MSG = "142";

    @RBEntry("找不到工作項目參數。")
    public static final String MISSING_PARAMETER = "1";

    @RBEntry("指示")
    public static final String INSTRUCTIONS = "2";

    @RBEntry("描述")
    public static final String DESCRIPTION = "2a";

    @RBEntry(":")
    @RBPseudo(false)
    public static final String COLON = "3";

    @RBEntry("變數名稱")
    public static final String VARIABLE_NAME = "4";

    @RBEntry("值")
    public static final String VALUE = "5";

    @RBEntry("工作流程任務")
    public static final String WORKFLOW_TASK_TITLE = "6";

    @RBEntry("工作項目已經順利完成。")
    public static final String WORKITEM_COMPLETE = "7";

    @RBEntry("無法完成工作項目，因為發生下列錯誤")
    public static final String WORKITEM_COMPLETION_FAILED = "8";

    @RBEntry("目標物件無法使用。")
    public static final String TARGET_OBJECT_GONE = "24";

    @RBEntry("任務完成")
    public static final String TASK_COMPLETE_BUTTON = "9";

    @RBEntry("流程")
    public static final String PROCESS_NAME = "10";

    @RBEntry("到期日")
    public static final String DUE_DATE = "11";

    @RBEntry("流程啟動者")
    public static final String PROCESS_INITIATOR = "12";

    @RBEntry("流程啟動者:")
    public static final String PROCESS_INITIATOR_LABEL = "PROCESS_INITIATOR_LABEL";

    @RBEntry("流程已啟動")
    public static final String PROCESS_INITIATED = "12.1";

    @RBEntry("工作負責人")
    public static final String ASSIGNEE = "13";

    @RBEntry("目標物件")
    public static final String TARGET_OBJECT = "14";

    @RBEntry("更新")
    public static final String UPDATE_LABEL = "19";

    @RBEntry("內容")
    public static final String CONTENT_LABEL = "20";

    @RBEntry("更新 {0} 內容")
    public static final String CONTENT_LINK_LABEL = "118";

    @RBEntry("流程指示")
    public static final String PROCESS_INSTRUCTIONS = "21";

    @RBEntry("流程描述")
    public static final String PROCESS_DESCRIPTION = "22";

    @RBEntry("此任務已經無效，因為它已經完成了")
    public static final String OBSOLETE_WORKITEM = "17";

    @RBEntry("此任務已經被暫停")
    public static final String SUSPENDED_WORKITEM_MSG = "81";

    @RBEntry("無法使用")
    public static final String NOT_AVAILABLE = "79";

    @RBEntry("(已暫停)")
    public static final String SUSPENDED_WORKITEM_FLAG = "80";

    @RBEntry("是")
    public static final String YES = "82";

    @RBEntry("否")
    public static final String NO = "83";

    @RBEntry("真")
    public static final String TRUE = "134";

    @RBEntry("假")
    public static final String FALSE = "135";

    @RBEntry("刪除")
    public static final String DELETE = "145";

    @RBEntry("更新")
    public static final String UPDATE = "146";

    @RBEntry("建立隨機活動")
    public static final String CREATE_ADHOC_ACTIVITIES_LINK = "86";

    @RBEntry("定義專案")
    public static final String DEFINE_PROJECTS_LINK = "113";

    @RBEntry("設定專案角色的參與者")
    public static final String SET_UP_PARTICIPANTS_LINK = "112";

    @RBEntry("第 {0} 欄")
    @RBArgComment0("{0} - Number e.g. Column 1 Column 2")
    public static final String COLUMN = "138";

    @RBEntry("來源")
    @RBComment("The data source of the worklist:  Current user's workitems or all workitems")
    public static final String WORKLIST_SOURCE = "139";

    @RBEntry("另存新檔")
    public static final String SAVE_AS = "140";

    @RBEntry("作為預設")
    public static final String USE_AS_DEFAULT = "141";

    @RBEntry("立即使用")
    public static final String USE_NOW = "149";

    @RBEntry("工作項目選取器")
    public static final String WORKITEM_SELECTOR = "154";

    @RBEntry("({0} 的代理人)")
    public static final String DELEGATE_FOR = "167";

    /**
     * Buttons
     **/
    @RBEntry("接受")
    public static final String ACCEPT_BUTTON = "25";

    @RBEntry("重新指派")
    public static final String REASSIGN_BUTTON = "26";

    @RBEntry("更新到期日")
    public static final String UPDATE_DUE_DATE_BUTTON = "27";

    @RBEntry("說明")
    public static final String HELP_BUTTON = "28";

    @RBEntry("重建工作清單")
    @RBComment("Translator--This item was changed from \"Create Work List\" to \"Rebuild Work List\"")
    public static final String CREATE_WORKLIST_BUTTON = "30";

    @RBEntry("提交")
    public static final String SUBMIT_BUTTON = "29";

    @RBEntry(" 確定 ")
    @RBComment("Translator--Please leave the leading spaces if this is a short (<5 char) word")
    public static final String OK_BUTTON = "148";

    @RBEntry("開始活動")
    public static final String START_ADHOC_ACTIVITIES_BUTTON = "87";

    @RBEntry("更多活動")
    public static final String MORE_ACTIVITIES_BUTTON = "108";

    @RBEntry("自動建立變更通知")
    public static final String AUTOMATE_FAST_TRACK_CHECKBOX = "225";

    @RBEntry("動作")
    public static final String ACTIONS_LABEL = "226";

    @RBEntry("檢視輕量流程圖像")
    public static final String VIEW_LIGHTWEIGHT_PROCESS = "227";

    @RBEntry("設定參與者")
    @RBComment("Label displayed for the Set Up Participants icon on the workflow task page")
    public static final String PARTICIPANTS_LABEL = "228";

    @RBEntry("參與者:")
    @RBComment("Participants inthe workflow task page")
    public static final String PARTICIPANTS = "229";

    /**
     * Labels & table headers --------------------------------------------------
     **/
    @RBEntry("排序依據")
    public static final String SORT_BY = "31";

    @RBEntry("群組依據")
    public static final String GROUP_BY = "32";

    @RBEntry("工作項目")
    public static final String WORKITEM_COLUMN_HEADER = "65";

    @RBEntry("狀況")
    public static final String STATUS_COLUMN_HEADER = "66";

    @RBEntry("已完成")
    public static final String COMPLETE_STATUS = "68";

    @RBEntry("失敗")
    public static final String FAILED_STATUS = "69";

    @RBEntry("工作負責人")
    public static final String ASSIGNED_TO = "73";

    @RBEntry("所有工作項目")
    public static final String ALL_WORKITEMS = "58";

    @RBEntry("更新的工作項目")
    public static final String UPDATED_WORKITEMS = "115";

    @RBEntry("開始日期")
    public static final String START_TIME = "231";

    @RBEntry("完成日期")
    public static final String END_TIME = "232";

    @RBEntry("流程狀況")
    public static final String PROCESS_STATUS = "233";

    /**
     * Titles ------------------------------------------------------------------
     **/
    @RBEntry("工作清單")
    public static final String WORKLIST = "35";

    @RBEntry("工作清單 < {0} >")
    @RBArgComment0("{0}-Worklist Owner's name or \"All Workitems\"")
    public static final String WORKLIST_TITLE = "132";

    @RBEntry("更新工作項目到期日")
    public static final String UPD_DUE_DATE_TASK_TITLE = "67";

    @RBEntry("重新指派工作項目")
    public static final String REASSIGN_TASK_TITLE = "72";

    @RBEntry("接受工作項目")
    public static final String ACCEPT_TASK_TITLE = "75";

    @RBEntry("更新角色參與者")
    public static final String AUGMENT_LINK_TITLE = "78";

    @RBEntry("管理配置圖")
    public static final String MANAGE_LAYOUTS = "147";

    /**
     * ALT tags -----------------------------------------------------------------
     **/
    @RBEntry("啟動流程")
    public static final String INITIATE_PROCESS = "101";

    @RBEntry("正在執行流程")
    public static final String RUNNING_PROCESSES = "102";

    @RBEntry("所有流程")
    public static final String ALL_PROCESSES = "103";

    @RBEntry("搜尋流程")
    public static final String SEARCH_FOR_PROCESSES = "111";

    @RBEntry("已完成流程數")
    public static final String COMPLETED_PROCESSES = "104";

    @RBEntry("管理流程")
    public static final String MANAGE_PROCESSES = "105";

    /**
     * Text & Messages ----------------------------------------------------------
     **/
    @RBEntry("重新指派")
    public static final String REASSIGN = "33";

    @RBEntry("已重新指派")
    public static final String REASSIGNED = "34";

    @RBEntry("任務 - {0} {1}")
    @RBComment("Title display for the name of the task.")
    @RBArgComment0("The name of the task.  For example \"Submit Problem Report\"")
    @RBArgComment1("The primary business object display if available (may not be present).  Example: Problem Report: 001221 or something")
    public static final String TASK_NAME_TITLE = "163";

    @RBEntry("{0}-{1}")
    @RBComment("Display name of the WorkItem")
    @RBArgComment0("Name of the WFProcess, WorkItem belongs to.")
    @RBArgComment1("Name of the WFActivity of WorkItem.")
    public static final String WORKITEM_TITLE = "163.1";

    @RBEntry("任務內容")
    public static final String TASK_CONTENT = "164";

    @RBEntry("討論區")
    public static final String DISCUSSION_FORUM = "165";

    @RBEntry("重新指派記錄")
    @RBComment("Label for the reassignment history table in a task details page.")
    public static final String ACTIVITY_REASSIGNMENT_HISTORY = "166";

    @RBEntry("此活動中沒有定義「專案」變數。")
    public static final String NO_PROJECTS_TO_DEFINE = "114";

    @RBEntry("註解")
    public static final String COMMENTS = "136";

    @RBEntry("您已將您的使用者驗證從 {0} 改為 {1}。任務 {2} 沒有完成。")
    public static final String SIGNATURE_VALIDATION_FAILED = "137";

    @RBEntry("是否接受工作項目 {0}？")
    @RBArgComment0("{0} - identifying attribute of the workitem to be accepted")
    public static final String ACCEPT_WORKITEM = "36";

    @RBEntry("{0} 不是一個有效的日期...請回到上一頁並依照所顯示的格式重新輸入日期。")
    @RBArgComment0("{o} - The badly formatted date")
    public static final String INVALID_DATE = "70";

    @RBEntry("更新到期日失敗。錯誤的詳細資訊如下:")
    public static final String UPD_DUE_DATE_FAILED = "71";

    @RBEntry("重新指派失敗。錯誤詳細資訊如下:")
    public static final String REASSIGN_FAILED = "74";

    @RBEntry("您必須至少選取一個完成此任務的路由。")
    public static final String NO_ROUTE_SELECTED = "76";

    @RBEntry("無法完成此工作項目，因為它的父活動已經被暫停了。")
    public static final String WORKITEM_IS_SUSPENDED = "77";

    @RBEntry("必須提供一個值給變數 {0}。")
    @RBArgComment0("{0} - Name of the variable that was not supplied.")
    public static final String MISSING_REQUIRED_VARIABLE = "85";

    @RBEntry("{0} 資料必須是 {1} 類型")
    @RBArgComment0("{0} - field name")
    @RBArgComment1("{1} - expected data type")
    public static final String INVALID_VARIABLE_TYPE = "18";

    @RBEntry("{0} 不是一個有效的「全名」、「使用者 ID」、「群組」、「角色」或「專案」")
    @RBArgComment0("{0} - assignee name")
    public static final String INVALID_ASSIGNEE = "88";

    @RBEntry("未選取工作項目。請用核取方塊標定想要的工作項目。")
    public static final String NO_WORKITEMS_SELECTED = "119";

    @RBEntry("已順利接收 {0} 個工作項目。")
    @RBArgComment0("{0} - number of workitems accepted successfully")
    public static final String ACCEPT_SUCCESSFUL = "120";

    @RBEntry("已順利接受 {0} 個工作項目。")
    @RBArgComment0("{0} - number of workitems accepted successfully (it should be 1--this is the singular case")
    public static final String ACCEPT_ONE_SUCCESSFUL = "121";

    @RBEntry("接收工作項目失敗。錯誤如下: {0}")
    @RBArgComment0("{0} - the exception text of why the accepting  of the workitems failed for all selected workitems")
    public static final String ACCEPT_FAILED = "122";

    @RBEntry("工作項目 {0} 未被接受，因發生下列錯誤: {1}")
    @RBArgComment0("{0} - identity of the workitem which could not be accepted")
    @RBArgComment1("{1} - exception text of why the workitem wasn't accepted")
    public static final String ACCEPT_WORKITEM_FAILED = "123";

    @RBEntry("{0} 個工作項目重新指派成功。")
    @RBArgComment0("{0} - number of workitems reassigned successfully")
    public static final String REASSIGN_SUCCESSFUL = "124";

    @RBEntry("{0} 個工作項目重新指派成功。")
    @RBArgComment0("{0} - number of workitems reassigned successfully (it should be 1--this is the singular case)")
    public static final String REASSIGN_ONE_SUCCESSFUL = "125";

    @RBEntry("重新指派工作項目失敗。錯誤如下: {0}")
    @RBArgComment0("{0} - the exception text of why the reassigning the workitems failed for all the selected workitems")
    public static final String REASSIGNMENT_FAILED = "126";

    @RBEntry("工作項目 {0} 未重新指派，因發生下列錯誤: {1}")
    @RBArgComment0("{0} - identity of the workitem which could not be reassigned")
    @RBArgComment1("{1} - exception text of why the workitem wasn't reasigned")
    public static final String REASSIGN_WORKITEM_FAILED = "127";

    @RBEntry("{0} 個工作項目更新成功。")
    @RBArgComment0("{0} - number of workitems whose due date was successfully updated")
    public static final String UPDATE_DUE_DATE_SUCCESSFUL = "128";

    @RBEntry("已順利更新 {0} 個工作項目。")
    @RBArgComment0("{0} - number of workitems whose due date was successfully updated (it should be 1--this is the singular case)")
    public static final String UPDATE_ONE_DUE_DATE_SUCCESSFUL = "129";

    @RBEntry("工作項目到期日更新失敗。錯誤如下: {0}")
    @RBArgComment0("{0} - the exception text of why the updating the workitems' due dates failed for all the selected workitems")
    public static final String UPDATE_DUE_DATE_FAILED = "130";

    @RBEntry("工作項目 {0} 未更新，因發生下列錯誤: {1}")
    @RBArgComment0("{0} - identity of the workitem whose due date could not be updated")
    @RBArgComment1("{1} - exception text of why the workitem wasn't updated")
    public static final String UPDATE_WORKITEM_DUE_DATE_FAILED = "131";

    @RBEntry("一個自訂的 activityVariable 指令集參考了變數 \"{0}\"。此活動尚未定義具該名稱的變數。")
    @RBArgComment0("{0} - The user defined name of the variable that could not be found")
    public static final String MISSING_ACTIVITY_VARIABLE = "133";

    @RBEntry("\"{0}\" 配置圖刪除成功。")
    @RBArgComment0("{0} - The name of the deleted layout")
    public static final String DELETED_LAYOUT = "143";

    @RBEntry("未刪除 \"{0}\" 配置圖。")
    @RBArgComment0("{0} - The name of the layout that should have been deleted")
    public static final String LAYOUT_NOT_DELETED = "144";

    @RBEntry("儲存前請提供此配置圖的名稱。")
    public static final String NO_LAYOUT_NAME = "150";

    @RBEntry("選來編組的欄必須包括在此配置圖的已定義欄中。")
    public static final String GROUP_KEY_NOT_IN_LAYOUT = "151";

    @RBEntry("選來排序的欄必須包括在此配置圖的已定義欄中。")
    public static final String SORT_KEY_NOT_IN_LAYOUT = "152";

    @RBEntry("此配置圖至少必須定義一個欄。")
    public static final String NO_COLUMNS_DEFINED = "153";

    /**
     * Attribute table headers ---------------------------------------------------
     **/
    @RBEntry("ID")
    public static final String ID = "37";

    @RBEntry("角色")
    public static final String ROLE = "38";

    @RBEntry("擁有者")
    public static final String OWNER = "39";

    @RBEntry("任務")
    public static final String TASK = "40";

    @RBEntry("來源")
    public static final String SOURCE = "41";

    @RBEntry("優先順序")
    public static final String PRIORITY = "42";

    @RBEntry("必要")
    public static final String REQUIRED = "43";

    @RBEntry("實行者")
    public static final String ACTOR_NAME = "44";

    @RBEntry("已接受")
    public static final String ACCEPTED = "57";

    @RBEntry("已完成")
    public static final String COMPLETED = "45";

    @RBEntry("專案")
    public static final String PROJECT = "46";

    @RBEntry("類型")
    public static final String TYPE = "47";

    @RBEntry("狀況 (任務)")
    public static final String STATUS = "84";

    @RBEntry("期限")
    public static final String DEADLINE = "48";

    @RBEntry("活動開始")
    public static final String ACTIVITY_START = "49";

    @RBEntry("活動名稱")
    public static final String ACTIVITY_NAME = "50";

    @RBEntry("活動描述")
    public static final String ACTIVITY_DESCRIPTION = "51";

    @RBEntry("工作流程名稱")
    public static final String WORKFLOW_NAME = "54";

    @RBEntry("工作流程期限")
    public static final String WORKFLOW_DEADLINE = "52";

    @RBEntry("工作流程開始")
    public static final String WORKFLOW_START = "53";

    @RBEntry("工作流程描述")
    public static final String WORKFLOW_DESCRIPTION = "55";

    @RBEntry("主旨")
    @RBComment(" Translater - this changed from \"Primary Business Object\" to subject")
    public static final String PRIMARY_BUSINESS_OBJECT = "56";

    @RBEntry("類型")
    public static final String PRIMARY_BUSINESS_OBJECT_TYPE = "116";

    @RBEntry("狀態")
    public static final String PRIMARY_BUSINESS_OBJECT_STATE = "117";


    @RBEntry("工作主旨")
    @RBComment("Primary Business Object of an assignment(task). Used to display column in search table.")
    public static final String SEARCH_SUBJECT_OF_ASSIGNMENT = "SEARCH_SUBJECT_OF_ASSIGNMENT";

    /**
     * Workitem priorities -------------------------------------------------------
     **/
    @RBEntry("最高")
    public static final String PRIORITY_ONE = "59";

    @RBEntry("高")
    public static final String PRIORITY_TWO = "60";

    @RBEntry("一般")
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

    @RBEntry("活動名稱")
    public static final String AD_HOC_ACTIVITY_NAME = "90";

    @RBEntry("工作負責人")
    public static final String AD_HOC_ASSIGNEE = "91";

    @RBEntry("提供")
    public static final String AD_HOC_OFFER = "92";

    @RBEntry("持續時間")
    public static final String AD_HOC_DURATION = "93";

    @RBEntry("前置任務")
    public static final String AD_HOC_PREDECESSORS = "94";

    @RBEntry("任務")
    public static final String AD_HOC_TASK = "95";

    @RBEntry("指示:")
    public static final String AD_HOC_INSTRUCTIONS = "96";

    @RBEntry("傳送電子郵件通知")
    public static final String AD_HOC_EMAIL_NOTIFICATION = "97";

    @RBEntry("活動已經順利開始")
    public static final String AD_HOC_COMPLETE = "100";

    @RBEntry("活動目前在執行中")
    public static final String AD_HOC_IN_PROGRESS = "106";

    @RBEntry("不允許循環流程，請檢查前置任務的循環相依性")
    public static final String AD_HOC_CYCLIC_ERROR = "107";

    @RBEntry("找不到活動。")
    public static final String AD_HOC_LIST_EMPTY = "109";

    @RBEntry("前置任務 {0} 超出界限")
    @RBArgComment0(" {0} is the id of the predecessor that was entered out of bounds.")
    public static final String AD_HOC_OUT_OF_BOUNDS = "110";

    @RBEntry("成功建立隨機活動。")
    public static final String Ad_HOC_CREATED_SUCCESSFULLY = "adhocCreatedSuccessfully";

    @RBEntry("*密碼")
    public static final String PASSWORD = "PASSWORD";

    /**
     * Ad hoc labels --------------------------------------------------------------
     **/
    @RBEntry("專案:")
    public static final String AD_HOC_PROJECT_LABEL = "98";

    @RBEntry("父任務:")
    public static final String AD_HOC_PARENT_TASK_LABEL = "99";

    /**
     * Trouble: Exceptions & Messages ---------------------------------------------
     **/
    @RBEntry("無法擷取所選取之工作項目的父活動。")
    public static final String MISSING_PARENT = "64";

    /**
     * Notifications
     **/
    @RBEntry("專案名稱:")
    public static final String NOTIFICATION_PROJECT_NAME = "200";

    @RBEntry("專案建立者:")
    public static final String NOTIFICATION_PROJECT_CREATOR = "201";

    @RBEntry("專案擁有者:")
    public static final String NOTIFICATION_PROJECT_OWNER = "201a";

    @RBEntry("主持組織:")
    public static final String NOTIFICATION_PROJECT_HOST = "202";

    @RBEntry("專案描述:")
    public static final String NOTIFICATION_PROJECT_DESC = "203";

    @RBEntry("無")
    public static final String NOTIFICATION_NONE = "204";

    @RBEntry("系統已經指派您 <A HREF=\"{0}\">{1} </A>。")
    @RBComment(" This is a line of text that is seen at the top of a workflow notification email that contains a hyperlink to the workflow details page in a Windchill system.  The translation can be found in the <BODY> element of the 7.0 WCL10N template srcwtworkflowhtmltmplworkNotificationGeneral*.html")
    @RBArgComment0("This is the url to the details page for the workflow task.  This is not seen by the user.")
    @RBArgComment1("This is the type of the workflow task that is generated by the system. Examples of this are Submit and Approve.  The final produced string would look something like the following where Submit is the variable for arg1 and Submit task is a hyperlink; You have been assigned a Submit task.")
    public static final String NOTIFICATION_TASK_URL = "205";

    @RBEntry("您收到這份工作分派是因為您是 {0} 的代理人")
    public static final String NOTIFICATION_DELEGATE_FOR = "206";

    @RBEntry("此任務已經完成")
    public static final String TASK_COMPLETE = "230";

    /**
     * Online help ----------------------------------------------------------------
     **/
    @RBEntry("wt/workflow/help_zh_TW/NotificationHelp.html")
    public static final String PRIVATE_CONSTANT_0 = "Help/WorkNotification/MainHelp";

    @RBEntry("...已本地化...")
    public static final String PRIVATE_CONSTANT_1 = "TestString";

    /**
     * ---------------------------------------------------------------------------
     **/
    @RBEntry("配置圖名稱不能包含「/」。")
    public static final String INVALID_LAYOUT_NAME = "155";

    /**
     * Teams ----------------------------------------------------------------------
     **/
    @RBEntry("小組")
    public static final String TEAM = "156";

    @RBEntry("定義小組")
    public static final String DEFINE_TEAMS_LINK = "157";

    @RBEntry("此活動中沒有定義「小組」變數。")
    public static final String NO_TEAMS_TO_DEFINE = "158";

    @RBEntry("小組:")
    public static final String AD_HOC_TEAM_LABEL = "159";

    @RBEntry("小組名稱:")
    public static final String NOTIFICATION_TEAM_NAME = "160";

    @RBEntry("小組建立者:")
    public static final String NOTIFICATION_TEAM_CREATOR = "161";

    @RBEntry("小組描述:")
    public static final String NOTIFICATION_TEAM_DESC = "162";

    /**
     * ---------------------------------------------------------------------------- Preferences
     **/
    @RBEntry("依開始時間排序")
    @RBComment("Localized name for Sort By Start worklist")
    public static final String PRIVATE_CONSTANT_2 = "SORT_BY_START_NAME";

    @RBEntry("「依開始時間排序」偏好設定是利用開始時間來排序工作清單。")
    @RBComment("Localized description for Sort By Start worklist")
    public static final String PRIVATE_CONSTANT_3 = "SORT_BY_START_DESC";

    @RBEntry("系統預設")
    @RBComment("Localized name for System Default worklist")
    public static final String PRIVATE_CONSTANT_4 = "SYSTEM_DEFAULT_NAME";

    @RBEntry("系統預設工作清單")
    @RBComment("Localized description for System Default worklist")
    public static final String PRIVATE_CONSTANT_5 = "SYSTEM_DEFAULT_DESC";

    /**
     * ---------------------------------------------------------------------------- Labels for Manage Layout page
     **/
    @RBEntry("群組:")
    public static final String GROUP_LABEL = "220";

    @RBEntry("排序:")
    public static final String SORT_LABEL = "221";

    @RBEntry("預先定義的配置圖")
    public static final String DEFINED_LAYOUTS_LABEL = "222";

    @RBEntry("要求推進狀態")
    public static final String REQUESTED_PROMOTION_STATE = "223";

    @RBEntry("配置圖:")
    public static final String LAYOUTS_LABEL = "224";

    /**
     * ------------------------------------------------------ Preferences for assignments table
     **/
    @RBEntry("工作分派表")
    @RBComment("Assignments table related preferences.")
    public static final String ASSIGNMENTS_TABLE_CAT_NAME = "ASSIGNMENTS_TABLE_CAT_NAME";

    @RBEntry("工作分派表相關偏好設定。")
    public static final String ASSIGNMENTS_TABLE_CAT_DESCR = "ASSIGNMENTS_TABLE_CAT_DESCR";

    @RBEntry("「首頁」-->「概觀」中的工作分派表")
    @RBComment("Preferences for Assignments table at Home-->Overview.")
    public static final String HOME_OVRV_ASSIGNMENTS_TABLE_CAT_NAME = "HOME_OVRV_ASSIGNMENTS_TABLE_CAT_NAME";

    @RBEntry("「首頁」-->「概觀」中的工作分派表偏好設定。")
    public static final String HOME_OVRV_ASSIGNMENTS_TABLE_CAT_DESCR = "HOME_OVRV_ASSIGNMENTS_TABLE_CAT_DESCR";

    /**
     * removed QUERY_LIMIT_PREF ------------------------------------------------------
     **/
    @RBEntry("您會遺失未儲存變更。請儲存任務頁以保存變更。")
    @RBComment("Confirmation message while setup participant table expansion on task detail page")
    public static final String UNSAVED_CHANGES_WARNING = "234";

    @RBEntry("至少需有一個活動名稱與工作負責人才能開始活動。")
    @RBComment("Validation error message when Adhoc activities are started without specifying Name and Assignee")
    public static final String ADHOC_VALIDATION_ERROR1 = "235";

    @RBEntry("需要活動名稱及工作負責人才能開始活動。")
    @RBComment("Validation error message when Adhoc activities are started without specifying Name and Assignee")
    public static final String ADHOC_VALIDATION_ERROR2 = "236";

    /*
     * Labels for Task Assistant
     */

    @RBEntry("路由選項")
    @RBComment("Task Routing Options")
    public static final String TASK_ROUTING = "237";

    @RBEntry("投票選項")
    @RBComment("Task voting options")
    public static final String TASK_VOTING = "238";

    @RBEntry("尚未儲存的 \"{0}\" 變更將會遺失，是否要開啟任務 \"{1}\"?")
    @RBComment("Task Assistant confirmation for existing tracked task assistant")
    public static final String TASKBAR_CONFIRMATION = "239";

    @RBEntry("名稱")
    public static final String WORKITEM_NAME = "250";

    @RBEntry("前後關聯")
    public static final String WORKITEM_CONTEXT = "251";

    @RBEntry("任務助理")
    @RBComment("Title of task assistant")
    public static final String TASK_WINDOW_TITLE = "TASK_WINDOW_TITLE";

    @RBEntry("主旨:")
    @RBComment("Label for task assistant PBO")
    public static final String TASK_WINDOW_SUBJECT = "TASK_WINDOW_SUBJECT";

    @RBEntry("任務:")
    @RBComment("Label for workitem name on task assistant")
    public static final String TASK_WINDOW_TASK = "TASK_WINDOW_TASK";

    @RBEntry("完成任務")
    @RBComment("Label for complete task button on task assistant")
    public static final String TASK_WINDOW_COMPLETE_TASK_BUTTON = "TASK_WINDOW_COMPLETE_TASK_BUTTON";

    @RBEntry("儲存")
    @RBComment("Label for save button on task assistant")
    public static final String TASK_WINDOW_SAVE_BUTTON = "TASK_WINDOW_SAVE_BUTTON";

    @RBEntry("繼續任務完成")
    @RBComment("Label for continue button on task assistant")
    public static final String TASK_WINDOW_CONTINUE_BUTTON = "TASK_WINDOW_CONTINUE_BUTTON";

    @RBEntry("已成功儲存註解。")
    @RBComment("Message after comment is saved from task assistant")
    public static final String TASK_WINDOW_SAVE_MESSAGE = "TASK_WINDOW_SAVE_MESSAGE";

    @RBEntry("已成功完成任務。")
    @RBComment("Message after task is completed from task assistant")
    public static final String TASK_WINDOW_COMPLETE_MESSAGE = "TASK_WINDOW_COMPLETE_MESSAGE";

    @RBEntry("有未儲存的變更，是否關閉任務助理?")
    @RBComment("Confirmation message to close task assistant")
    public static final String TASK_WINDOW_CLOSE_CONFIRMATION_MESSAGE = "TASK_WINDOW_CLOSE_CONFIRMATION_MESSAGE";

    /*
     * Labels for task details page
     */

    @RBEntry("流程狀況")
    public static final String workitem_routingStatus_description = "workitem.routingStatus.description";

    @RBEntry("流程狀況")
    public static final String workitem_routingStatus_tooltip = "workitem.routingStatus.tooltip";

    @RBEntry("newproject.gif")
    @RBPseudo(false)
    @RBComment("DO NOT TRANSLATE")
    public static final String workitem_routingStatus_icon = "workitem.routingStatus.icon";

    @RBEntry("詳細資訊")
    @RBComment("The Details tab on the info page for WorkItem")
    public static final String workitem_taskDetailsDetails_description = "object.taskDetailsDetails.description";

    @RBEntry("範本檢視")
    @RBComment("The main tab on the info page for WorkItem which will render task form template")
    public static final String workitem_taskFormTemplateDetails_description = "object.taskFormTemplateDetails.description";

    @RBEntry("相關")
    @RBComment("The Others tab on the info page for WorkItem")
    public static final String workitem_taskDetailsOthers_description = "object.taskDetailsOthers.description";

    @RBEntry("屬性")
    @RBComment("The Attributes menu on the info page for WorkItem")
    public static final String workitem_attributes_description = "workitem.attributes.description";

    @RBEntry("記事本")
    @RBComment("The Notebook menu on the info page for WorkItem")
    public static final String workitem_notebook_description = "workitem.notebook.description";

    @RBEntry("討論區")
    @RBComment("The Discussions menu on the info page for WorkItem")
    public static final String workitem_discussions_description = "workitem.discussions.description";

    @RBEntry("設定參與者")
    @RBComment("The Set Up Participant tab on the info page for WorkItem")
    public static final String workitem_taskDetailsSetupParticipant_description = "object.taskDetailsSetupParticipant.description";

    @RBEntry("隨機活動")
    @RBComment("The Adhoc Activities tab on the info page for WorkItem")
    public static final String workitem_taskDetailsAdhoc_description = "object.taskDetailsAdhoc.description";

    @RBEntry("隨機活動")
    @RBComment("The Adhoc Activities customization action")
    public static final String workitem_adhocActivities_description = "workitem.adhocActivities.description";

    @RBEntry("設定參與者")
    @RBComment("Set Up PArticipant customization action")
    public static final String workitem_setupParticipant_description = "workitem.setupParticipant.description";

    @RBEntry("insert_multi_rows_below.gif")
    @RBPseudo(false)
    public static final String workitem_addMultipleObjects_icon = "workitem.addMultipleObjects.icon";

    @RBEntry("新增 5 列")
    @RBComment("Action to add 5 empty rows")
    public static final String workitem_addMultipleObjects_description = "workitem.addMultipleObjects.description";

    @RBEntry("新增 5 列")
    public static final String workitem_addMultipleObjects_tooltip = "workitem.addMultipleObjects.tooltip";

    @RBEntry("特殊指示")
    @RBComment("Special instructions is a special Workflow veriable which is used in Change related Workflow.")
    public static final String SPECIAL_INSTRUCTIONS_LABEL = "special_instructions_label";

    @RBEntry("範本檢視")
    @RBComment("Label for Task Form Template action")
    public static final String workitem_taskFormTemplate_description = "workitem.taskFormTemplate.description";

    @RBEntry("這是「隨機活動」任務。請使用「隨機活動」標籤來建立及指派新的隨機活動。")
    @RBComment("Inline help contents for ad hoc type of activity.")
    public static final String ADHOC_INLINE_HELP = "ADHOC_INLINE_HELP";

    @RBEntry("這是「設定參與者」任務。請使用「設定參與者」標籤將參與者指派給此流程中使用的不同角色。")
    @RBComment("Inline help contents for set up participant type of activity.")
    public static final String SETUP_INLINE_HELP = "SETUP_INLINE_HELP";

    @RBEntry("此任務使用針對此任務類型與主要企業物件類型定義的任務表範本進行顯示。")
    @RBComment("Inline help contents if task form template preference is set to true.")
    public static final String TEMPLATE_VIEW_INLINE_HELP = "TEMPLATE_VIEW_INLINE_HELP";

    @RBEntry("已將此任務設定為「隨機活動」任務和「設定參與者」任務。請使用「隨機活動」標籤來建立及指派新的隨機活動。請使用「設定參與者」標籤將參與者指派給此流程中使用的不同角色。")
    @RBComment("Inline help contents for set up as well as ad hoc type of activity.")
    public static final String SETUP_ADHOC_INLINE_HELP = "SETUP_ADHOC_INLINE_HELP";


	@RBEntry("生命週期記錄")
    public static final String PBOLEGACYHISTORY = "history.pboLegacyLCHistory.description";

	@RBEntry("版序記錄")
	public static final String PBOITERATIONHISTORY = "history.pboIterationHistory.description";

	@RBEntry("成熟度記錄")
    public static final String PBOMATURITYHISTORY = "history.pboMaturityHistory.description";

	@RBEntry("受影響物件")
    public static final String TestAffectedData = "change.TestAffectedData.description";

	@RBEntry("變更請求")
	public static final String TestRelatedChangeRequest = "change.TestRelatedChangeRequest.description";

	@RBEntry("受影響最終項目")
	public static final String  TestAffectedEndItemsTable  = "change.TestAffectedEndItemsTable.description";

	@RBEntry("附件")
	public static final String  TestAttachment  = "change.TestAttachment.description";


	@RBEntry("問題與變動")
	public static final String TestRelatedChangeIssues = "change.TestRelatedChangeIssues.description";

	@RBEntry("受變更通知影響")
	public static final String  TestRelatedChangeNotice  = "change.TestRelatedChangeNotice.description";

	@RBEntry("源於變更通知")
	public static final String  TestResultingfromChangeNotice  = "change.TestResultingfromChangeNotice.description";


	@RBEntry("版本記錄")
	public static final String PBOVERSIONHISTORY = "history.pboVersionHistory.description";

	@RBEntry("主旨屬性")
	public static final String WORKFLOW_TASK_PBO_ATTRIBUTES = "object.workflowTaskPboAttributes.description";

	@RBEntry("針對主旨的動作")
	public static final String WORKFLOW_TASK_PBO_ACTIONS = "workitem.workflowTaskPboAction.description";

	@RBEntry("任務完成按鈕")
	public static final String saveComplete = "workitem.saveComplete.description";

	@RBEntry("任務變數")
	public static final String workItemActivityVariables = "workitem.workItemActivityVariables.description";

	@RBEntry("任務完成")
	public static final String signOffComponent = "workitem.workItemSignOffComponent.description";

	@RBEntry("任務變數")
	public static final String CUSTOM_VARIABLES_PANEL = "CUSTOM_VARIABLES_PANEL";

	@RBEntry("任務完成")
	public static final String TASK_SIGN_OFF_PANEL = "TASK_SIGN_OFF_PANEL";

    //add by lzy 20191024 start
    @RBEntry("BOM變更報表")
    public static final String workitem_bomChangeReport_description = "workitem.bomChangeReport.description";
    //add by lzy 20191024 end
}
