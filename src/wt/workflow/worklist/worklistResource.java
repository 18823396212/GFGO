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
public final class worklistResource extends WTListResourceBundle {
    @RBEntry("Stuff is wrong. {0}")
    @RBComment("Translators--this is a message for developer use only :-)")
    public static final String BETH_MSG = "142";

    @RBEntry("The workitem parameter was not found.")
    public static final String MISSING_PARAMETER = "1";

    @RBEntry("Instructions")
    public static final String INSTRUCTIONS = "2";

    @RBEntry("Description")
    public static final String DESCRIPTION = "2a";

    @RBEntry(":")
    @RBPseudo(false)
    public static final String COLON = "3";

    @RBEntry("Variable Name")
    public static final String VARIABLE_NAME = "4";

    @RBEntry("Value")
    public static final String VALUE = "5";

    @RBEntry("Workflow Task")
    public static final String WORKFLOW_TASK_TITLE = "6";

    @RBEntry("Work item has been successfully completed.")
    public static final String WORKITEM_COMPLETE = "7";

    @RBEntry("The workitem could not be completed because of the following error")
    public static final String WORKITEM_COMPLETION_FAILED = "8";

    @RBEntry("The target object is not available.")
    public static final String TARGET_OBJECT_GONE = "24";

    @RBEntry("Task Complete")
    public static final String TASK_COMPLETE_BUTTON = "9";

    @RBEntry("Process")
    public static final String PROCESS_NAME = "10";

    @RBEntry("Due Date")
    public static final String DUE_DATE = "11";

    @RBEntry("Process Initiator")
    public static final String PROCESS_INITIATOR = "12";

    @RBEntry("Process Initiator:")
    public static final String PROCESS_INITIATOR_LABEL = "PROCESS_INITIATOR_LABEL";

    @RBEntry("Process Initiated")
    public static final String PROCESS_INITIATED = "12.1";

    @RBEntry("Assignee")
    public static final String ASSIGNEE = "13";

    @RBEntry("Target Object")
    public static final String TARGET_OBJECT = "14";

    @RBEntry("Update")
    public static final String UPDATE_LABEL = "19";

    @RBEntry("Content")
    public static final String CONTENT_LABEL = "20";

    @RBEntry("Update {0} Content")
    public static final String CONTENT_LINK_LABEL = "118";

    @RBEntry("Process Instructions")
    public static final String PROCESS_INSTRUCTIONS = "21";

    @RBEntry("Process Description")
    public static final String PROCESS_DESCRIPTION = "22";

    @RBEntry("This task is no longer valid because it has been completed")
    public static final String OBSOLETE_WORKITEM = "17";

    @RBEntry("This task has been suspended")
    public static final String SUSPENDED_WORKITEM_MSG = "81";

    @RBEntry("Not Available")
    public static final String NOT_AVAILABLE = "79";

    @RBEntry("(Suspended)")
    public static final String SUSPENDED_WORKITEM_FLAG = "80";

    @RBEntry("Yes")
    public static final String YES = "82";

    @RBEntry("No")
    public static final String NO = "83";

    @RBEntry("True")
    public static final String TRUE = "134";

    @RBEntry("False")
    public static final String FALSE = "135";

    @RBEntry("Delete")
    public static final String DELETE = "145";

    @RBEntry("Update")
    public static final String UPDATE = "146";

    @RBEntry("Create ad hoc activities")
    public static final String CREATE_ADHOC_ACTIVITIES_LINK = "86";

    @RBEntry("Define projects")
    public static final String DEFINE_PROJECTS_LINK = "113";

    @RBEntry("Set up participants for project roles")
    public static final String SET_UP_PARTICIPANTS_LINK = "112";

    @RBEntry("Column {0}")
    @RBArgComment0("{0} - Number e.g. Column 1 Column 2")
    public static final String COLUMN = "138";

    @RBEntry("Source")
    @RBComment("The data source of the worklist:  Current user's workitems or all workitems")
    public static final String WORKLIST_SOURCE = "139";

    @RBEntry("Save as")
    public static final String SAVE_AS = "140";

    @RBEntry("Use as default")
    public static final String USE_AS_DEFAULT = "141";

    @RBEntry("Use now")
    public static final String USE_NOW = "149";

    @RBEntry("Work Item Selector")
    public static final String WORKITEM_SELECTOR = "154";

    @RBEntry("(Delegate for {0})")
    public static final String DELEGATE_FOR = "167";

    /**
     * Buttons
     **/
    @RBEntry("Accept")
    public static final String ACCEPT_BUTTON = "25";

    @RBEntry("Reassign")
    public static final String REASSIGN_BUTTON = "26";

    @RBEntry("Update Due Date")
    public static final String UPDATE_DUE_DATE_BUTTON = "27";

    @RBEntry("Help")
    public static final String HELP_BUTTON = "28";

    @RBEntry("Rebuild Worklist")
    @RBComment("Translator--This item was changed from \"Create Work List\" to \"Rebuild Work List\"")
    public static final String CREATE_WORKLIST_BUTTON = "30";

    @RBEntry("Submit")
    public static final String SUBMIT_BUTTON = "29";

    @RBEntry(" OK ")
    @RBComment("Translator--Please leave the leading spaces if this is a short (<5 char) word")
    public static final String OK_BUTTON = "148";

    @RBEntry("Start Activities")
    public static final String START_ADHOC_ACTIVITIES_BUTTON = "87";

    @RBEntry("More Activities")
    public static final String MORE_ACTIVITIES_BUTTON = "108";

    @RBEntry("Automatically Create Change Notice")
    public static final String AUTOMATE_FAST_TRACK_CHECKBOX = "225";

    @RBEntry("Actions")
    public static final String ACTIONS_LABEL = "226";

    @RBEntry("View Lightweight Process Image")
    public static final String VIEW_LIGHTWEIGHT_PROCESS = "227";

    @RBEntry("Set Up Participants")
    @RBComment("Label displayed for the Set Up Participants icon on the workflow task page")
    public static final String PARTICIPANTS_LABEL = "228";

    @RBEntry("Participants:")
    @RBComment("Participants inthe workflow task page")
    public static final String PARTICIPANTS = "229";

    /**
     * Labels & table headers --------------------------------------------------
     **/
    @RBEntry("Sort By")
    public static final String SORT_BY = "31";

    @RBEntry("Group By")
    public static final String GROUP_BY = "32";

    @RBEntry("Work Item")
    public static final String WORKITEM_COLUMN_HEADER = "65";

    @RBEntry("Status")
    public static final String STATUS_COLUMN_HEADER = "66";

    @RBEntry("Completed")
    public static final String COMPLETE_STATUS = "68";

    @RBEntry("Failed")
    public static final String FAILED_STATUS = "69";

    @RBEntry("Assigned to")
    public static final String ASSIGNED_TO = "73";

    @RBEntry("All Work Items")
    public static final String ALL_WORKITEMS = "58";

    @RBEntry("Updated Work Items")
    public static final String UPDATED_WORKITEMS = "115";

    @RBEntry("Date Started")
    public static final String START_TIME = "231";

    @RBEntry("Date Completed")
    public static final String END_TIME = "232";

    @RBEntry("Process Status")
    public static final String PROCESS_STATUS = "233";

    /**
     * Titles ------------------------------------------------------------------
     **/
    @RBEntry("Worklist")
    public static final String WORKLIST = "35";

    @RBEntry("Worklist < {0} >")
    @RBArgComment0("{0}-Worklist Owner's name or \"All Workitems\"")
    public static final String WORKLIST_TITLE = "132";

    @RBEntry("Update Work Item Due Dates")
    public static final String UPD_DUE_DATE_TASK_TITLE = "67";

    @RBEntry("Reassign Work Items")
    public static final String REASSIGN_TASK_TITLE = "72";

    @RBEntry("Accept Work Items")
    public static final String ACCEPT_TASK_TITLE = "75";

    @RBEntry("Update Role Participants")
    public static final String AUGMENT_LINK_TITLE = "78";

    @RBEntry("Manage Layouts")
    public static final String MANAGE_LAYOUTS = "147";

    /**
     * ALT tags -----------------------------------------------------------------
     **/
    @RBEntry("Initiate Process")
    public static final String INITIATE_PROCESS = "101";

    @RBEntry("Running Processes")
    public static final String RUNNING_PROCESSES = "102";

    @RBEntry("All Processes")
    public static final String ALL_PROCESSES = "103";

    @RBEntry("Search for Processes")
    public static final String SEARCH_FOR_PROCESSES = "111";

    @RBEntry("Completed Processes")
    public static final String COMPLETED_PROCESSES = "104";

    @RBEntry("Manage Processes")
    public static final String MANAGE_PROCESSES = "105";

    /**
     * Text & Messages ----------------------------------------------------------
     **/
    @RBEntry("Reassign")
    public static final String REASSIGN = "33";

    @RBEntry("Reassigned")
    public static final String REASSIGNED = "34";

    @RBEntry("Task - {0} {1}")
    @RBComment("Title display for the name of the task.")
    @RBArgComment0("The name of the task.  For example \"Submit Problem Report\"")
    @RBArgComment1("The primary business object display if available (may not be present).  Example: Problem Report: 001221 or something")
    public static final String TASK_NAME_TITLE = "163";

    @RBEntry("{0}-{1}")
    @RBComment("Display name of the WorkItem")
    @RBArgComment0("Name of the WFProcess, WorkItem belongs to.")
    @RBArgComment1("Name of the WFActivity of WorkItem.")
    public static final String WORKITEM_TITLE = "163.1";

    @RBEntry("Task Content")
    public static final String TASK_CONTENT = "164";

    @RBEntry("Discussion Forum")
    public static final String DISCUSSION_FORUM = "165";

    @RBEntry("Reassignment History")
    @RBComment("Label for the reassignment history table in a task details page.")
    public static final String ACTIVITY_REASSIGNMENT_HISTORY = "166";

    @RBEntry("There are no Project variables defined in this activity.")
    public static final String NO_PROJECTS_TO_DEFINE = "114";

    @RBEntry("Comments")
    public static final String COMMENTS = "136";

    @RBEntry("You have changed your user authentication from {0} to {1}. The task {2} was not completed.")
    public static final String SIGNATURE_VALIDATION_FAILED = "137";

    @RBEntry("Accept work item {0} ?")
    @RBArgComment0("{0} - identifying attribute of the workitem to be accepted")
    public static final String ACCEPT_WORKITEM = "36";

    @RBEntry("{0} is not a valid date...Page back and re-enter the date, following the displayed format.")
    @RBArgComment0("{o} - The badly formatted date")
    public static final String INVALID_DATE = "70";

    @RBEntry("Updating the due date failed.  Error details follow:")
    public static final String UPD_DUE_DATE_FAILED = "71";

    @RBEntry("Reassignment failed.  Error details follow:")
    public static final String REASSIGN_FAILED = "74";

    @RBEntry("You must select at least one route to complete this task.")
    public static final String NO_ROUTE_SELECTED = "76";

    @RBEntry("This work item cannot be completed because its parent activity has been suspended.")
    public static final String WORKITEM_IS_SUSPENDED = "77";

    @RBEntry("Variable {0} must have a value supplied.")
    @RBArgComment0("{0} - Name of the variable that was not supplied.")
    public static final String MISSING_REQUIRED_VARIABLE = "85";

    @RBEntry("{0} data must be of type {1}")
    @RBArgComment0("{0} - field name")
    @RBArgComment1("{1} - expected data type")
    public static final String INVALID_VARIABLE_TYPE = "18";

    @RBEntry("{0} is not a valid Full Name, UserID, Group, Role or Project")
    @RBArgComment0("{0} - assignee name")
    public static final String INVALID_ASSIGNEE = "88";

    @RBEntry("No work items were selected.  Use the check boxes to mark the desired work items.")
    public static final String NO_WORKITEMS_SELECTED = "119";

    @RBEntry("{0} work items were successfully accepted.")
    @RBArgComment0("{0} - number of workitems accepted successfully")
    public static final String ACCEPT_SUCCESSFUL = "120";

    @RBEntry("{0} work item was successfully accepted.")
    @RBArgComment0("{0} - number of workitems accepted successfully (it should be 1--this is the singular case")
    public static final String ACCEPT_ONE_SUCCESSFUL = "121";

    @RBEntry("Accepting work items failed.  Error details follow: {0}")
    @RBArgComment0("{0} - the exception text of why the accepting  of the workitems failed for all selected workitems")
    public static final String ACCEPT_FAILED = "122";

    @RBEntry("Work item {0} was not accepted because of the following error: {1}")
    @RBArgComment0("{0} - identity of the workitem which could not be accepted")
    @RBArgComment1("{1} - exception text of why the workitem wasn't accepted")
    public static final String ACCEPT_WORKITEM_FAILED = "123";

    @RBEntry("{0} work items were successfully reassigned.")
    @RBArgComment0("{0} - number of workitems reassigned successfully")
    public static final String REASSIGN_SUCCESSFUL = "124";

    @RBEntry("{0} work item was successfully reassigned.")
    @RBArgComment0("{0} - number of workitems reassigned successfully (it should be 1--this is the singular case)")
    public static final String REASSIGN_ONE_SUCCESSFUL = "125";

    @RBEntry("Reassigning work items failed.  Error details follow: {0}")
    @RBArgComment0("{0} - the exception text of why the reassigning the workitems failed for all the selected workitems")
    public static final String REASSIGNMENT_FAILED = "126";

    @RBEntry("Work item {0} was not reassigned because of the following error: {1}")
    @RBArgComment0("{0} - identity of the workitem which could not be reassigned")
    @RBArgComment1("{1} - exception text of why the workitem wasn't reasigned")
    public static final String REASSIGN_WORKITEM_FAILED = "127";

    @RBEntry("{0} work items were successfully updated.")
    @RBArgComment0("{0} - number of workitems whose due date was successfully updated")
    public static final String UPDATE_DUE_DATE_SUCCESSFUL = "128";

    @RBEntry("{0} work item was successfully updated.")
    @RBArgComment0("{0} - number of workitems whose due date was successfully updated (it should be 1--this is the singular case)")
    public static final String UPDATE_ONE_DUE_DATE_SUCCESSFUL = "129";

    @RBEntry("Updating work item due dates failed.  Error details follow: {0}")
    @RBArgComment0("{0} - the exception text of why the updating the workitems' due dates failed for all the selected workitems")
    public static final String UPDATE_DUE_DATE_FAILED = "130";

    @RBEntry("Work item {0} was not updated because of the following error: {1}")
    @RBArgComment0("{0} - identity of the workitem whose due date could not be updated")
    @RBArgComment1("{1} - exception text of why the workitem wasn't updated")
    public static final String UPDATE_WORKITEM_DUE_DATE_FAILED = "131";

    @RBEntry("A customized activityVariable script references variable \"{0}\".  No variable with that name is defined in this activity.")
    @RBArgComment0("{0} - The user defined name of the variable that could not be found")
    public static final String MISSING_ACTIVITY_VARIABLE = "133";

    @RBEntry("Layout \"{0}\" was successfully deleted.")
    @RBArgComment0("{0} - The name of the deleted layout")
    public static final String DELETED_LAYOUT = "143";

    @RBEntry("Layout \"{0}\" was not deleted.")
    @RBArgComment0("{0} - The name of the layout that should have been deleted")
    public static final String LAYOUT_NOT_DELETED = "144";

    @RBEntry("Supply a name for this layout before saving.")
    public static final String NO_LAYOUT_NAME = "150";

    @RBEntry("The field selected for grouping must be included in this layout's defined columns.")
    public static final String GROUP_KEY_NOT_IN_LAYOUT = "151";

    @RBEntry("The field selected for sorting must be included in this layout's defined columns.")
    public static final String SORT_KEY_NOT_IN_LAYOUT = "152";

    @RBEntry("There must be at least one column defined in this layout.")
    public static final String NO_COLUMNS_DEFINED = "153";

    /**
     * Attribute table headers ---------------------------------------------------
     **/
    @RBEntry("Id")
    public static final String ID = "37";

    @RBEntry("Role")
    public static final String ROLE = "38";

    @RBEntry("Owner")
    public static final String OWNER = "39";

    @RBEntry("Task")
    public static final String TASK = "40";

    @RBEntry("Source")
    public static final String SOURCE = "41";

    @RBEntry("Priority")
    public static final String PRIORITY = "42";

    @RBEntry("Required")
    public static final String REQUIRED = "43";

    @RBEntry("Actor")
    public static final String ACTOR_NAME = "44";

    @RBEntry("Accepted")
    public static final String ACCEPTED = "57";

    @RBEntry("Completed")
    public static final String COMPLETED = "45";

    @RBEntry("Project")
    public static final String PROJECT = "46";

    @RBEntry("Type")
    public static final String TYPE = "47";

    @RBEntry("Status (Task)")
    public static final String STATUS = "84";

    @RBEntry("Deadline")
    public static final String DEADLINE = "48";

    @RBEntry("Activity Start")
    public static final String ACTIVITY_START = "49";

    @RBEntry("Activity Name")
    public static final String ACTIVITY_NAME = "50";

    @RBEntry("Activity Description")
    public static final String ACTIVITY_DESCRIPTION = "51";

    @RBEntry("Workflow Name")
    public static final String WORKFLOW_NAME = "54";

    @RBEntry("Workflow Deadline")
    public static final String WORKFLOW_DEADLINE = "52";

    @RBEntry("Workflow Start")
    public static final String WORKFLOW_START = "53";

    @RBEntry("Workflow Description")
    public static final String WORKFLOW_DESCRIPTION = "55";

    @RBEntry("Subject")
    @RBComment(" Translater - this changed from \"Primary Business Object\" to subject")
    public static final String PRIMARY_BUSINESS_OBJECT = "56";

    @RBEntry("Type")
    public static final String PRIMARY_BUSINESS_OBJECT_TYPE = "116";

    @RBEntry("State")
    public static final String PRIMARY_BUSINESS_OBJECT_STATE = "117";


    @RBEntry("Subject of Assignment")
    @RBComment("Primary Business Object of an assignment(task). Used to display column in search table.")
    public static final String SEARCH_SUBJECT_OF_ASSIGNMENT = "SEARCH_SUBJECT_OF_ASSIGNMENT";

    /**
     * Workitem priorities -------------------------------------------------------
     **/
    @RBEntry("Highest")
    public static final String PRIORITY_ONE = "59";

    @RBEntry("High")
    public static final String PRIORITY_TWO = "60";

    @RBEntry("Normal")
    public static final String PRIORITY_THREE = "61";

    @RBEntry("Low")
    public static final String PRIORITY_FOUR = "62";

    @RBEntry("Lowest")
    public static final String PRIORITY_FIVE = "63";

    /**
     * ---------------------------------------------------------------------------
     **/
    @RBEntry("ID")
    public static final String AD_HOC_ACTIVITY_ID = "89";

    @RBEntry("Activity Name")
    public static final String AD_HOC_ACTIVITY_NAME = "90";

    @RBEntry("Assignee")
    public static final String AD_HOC_ASSIGNEE = "91";

    @RBEntry("Offer")
    public static final String AD_HOC_OFFER = "92";

    @RBEntry("Duration")
    public static final String AD_HOC_DURATION = "93";

    @RBEntry("Predecessors")
    public static final String AD_HOC_PREDECESSORS = "94";

    @RBEntry("Task")
    public static final String AD_HOC_TASK = "95";

    @RBEntry("Instructions:")
    public static final String AD_HOC_INSTRUCTIONS = "96";

    @RBEntry("Send email notification")
    public static final String AD_HOC_EMAIL_NOTIFICATION = "97";

    @RBEntry("Activities have been successfully started")
    public static final String AD_HOC_COMPLETE = "100";

    @RBEntry("Activities are currently running")
    public static final String AD_HOC_IN_PROGRESS = "106";

    @RBEntry("Circular processes are not allowed, check the Predecessors for a circular dependency")
    public static final String AD_HOC_CYCLIC_ERROR = "107";

    @RBEntry("No activities found.")
    public static final String AD_HOC_LIST_EMPTY = "109";

    @RBEntry("The predecessor {0} is out of bounds")
    @RBArgComment0(" {0} is the id of the predecessor that was entered out of bounds.")
    public static final String AD_HOC_OUT_OF_BOUNDS = "110";

    @RBEntry("Ad-hoc activities created successfully.")
    public static final String Ad_HOC_CREATED_SUCCESSFULLY = "adhocCreatedSuccessfully";

    @RBEntry("*Password")
    public static final String PASSWORD = "PASSWORD";

    /**
     * Ad hoc labels --------------------------------------------------------------
     **/
    @RBEntry("Project:")
    public static final String AD_HOC_PROJECT_LABEL = "98";

    @RBEntry("Parent Task:")
    public static final String AD_HOC_PARENT_TASK_LABEL = "99";

    /**
     * Trouble: Exceptions & Messages ---------------------------------------------
     **/
    @RBEntry("Unable to retrieve the parent activity of the selected work item.")
    public static final String MISSING_PARENT = "64";

    /**
     * Notifications
     **/
    @RBEntry("Project Name:")
    public static final String NOTIFICATION_PROJECT_NAME = "200";

    @RBEntry("Project Creator:")
    public static final String NOTIFICATION_PROJECT_CREATOR = "201";

    @RBEntry("Project Owner:")
    public static final String NOTIFICATION_PROJECT_OWNER = "201a";

    @RBEntry("Host Organization:")
    public static final String NOTIFICATION_PROJECT_HOST = "202";

    @RBEntry("Project Description:")
    public static final String NOTIFICATION_PROJECT_DESC = "203";

    @RBEntry("None")
    public static final String NOTIFICATION_NONE = "204";

    @RBEntry("You have been assigned a <A HREF=\"{0}\">{1} </A>.")
    @RBComment(" This is a line of text that is seen at the top of a workflow notification email that contains a hyperlink to the workflow details page in a Windchill system.  The translation can be found in the <BODY> element of the 7.0 WCL10N template srcwtworkflowhtmltmplworkNotificationGeneral*.html")
    @RBArgComment0("This is the url to the details page for the workflow task.  This is not seen by the user.")
    @RBArgComment1("This is the type of the workflow task that is generated by the system. Examples of this are Submit and Approve.  The final produced string would look something like the following where Submit is the variable for arg1 and Submit task is a hyperlink; You have been assigned a Submit task.")
    public static final String NOTIFICATION_TASK_URL = "205";

    @RBEntry("You have received this assignment as a delegate for {0}")
    public static final String NOTIFICATION_DELEGATE_FOR = "206";

    @RBEntry("This task has been already completed")
    public static final String TASK_COMPLETE = "230";

    /**
     * Online help ----------------------------------------------------------------
     **/
    @RBEntry("wt/workflow/help_en/NotificationHelp.html")
    public static final String PRIVATE_CONSTANT_0 = "Help/WorkNotification/MainHelp";

    @RBEntry("...Localized...")
    public static final String PRIVATE_CONSTANT_1 = "TestString";

    /**
     * ---------------------------------------------------------------------------
     **/
    @RBEntry("The layout name cannot contain a \"/\".")
    public static final String INVALID_LAYOUT_NAME = "155";

    /**
     * Teams ----------------------------------------------------------------------
     **/
    @RBEntry("Team")
    public static final String TEAM = "156";

    @RBEntry("Define teams")
    public static final String DEFINE_TEAMS_LINK = "157";

    @RBEntry("There are no Team variables defined in this activity.")
    public static final String NO_TEAMS_TO_DEFINE = "158";

    @RBEntry("Team:")
    public static final String AD_HOC_TEAM_LABEL = "159";

    @RBEntry("Team name:")
    public static final String NOTIFICATION_TEAM_NAME = "160";

    @RBEntry("Team creator:")
    public static final String NOTIFICATION_TEAM_CREATOR = "161";

    @RBEntry("Team description:")
    public static final String NOTIFICATION_TEAM_DESC = "162";

    /**
     * ---------------------------------------------------------------------------- Preferences
     **/
    @RBEntry("SortByStart")
    @RBComment("Localized name for Sort By Start worklist")
    public static final String PRIVATE_CONSTANT_2 = "SORT_BY_START_NAME";

    @RBEntry("The Sort by Start preference is used to sort the worklist by start.")
    @RBComment("Localized description for Sort By Start worklist")
    public static final String PRIVATE_CONSTANT_3 = "SORT_BY_START_DESC";

    @RBEntry("System Default")
    @RBComment("Localized name for System Default worklist")
    public static final String PRIVATE_CONSTANT_4 = "SYSTEM_DEFAULT_NAME";

    @RBEntry("System Default worklist")
    @RBComment("Localized description for System Default worklist")
    public static final String PRIVATE_CONSTANT_5 = "SYSTEM_DEFAULT_DESC";

    /**
     * ---------------------------------------------------------------------------- Labels for Manage Layout page
     **/
    @RBEntry("Group:")
    public static final String GROUP_LABEL = "220";

    @RBEntry("Sort:")
    public static final String SORT_LABEL = "221";

    @RBEntry("Defined Layouts")
    public static final String DEFINED_LAYOUTS_LABEL = "222";

    @RBEntry("Requested Promotion State")
    public static final String REQUESTED_PROMOTION_STATE = "223";

    @RBEntry("Layouts:")
    public static final String LAYOUTS_LABEL = "224";

    /**
     * ------------------------------------------------------ Preferences for assignments table
     **/
    @RBEntry("Assignments Table")
    @RBComment("Assignments table related preferences.")
    public static final String ASSIGNMENTS_TABLE_CAT_NAME = "ASSIGNMENTS_TABLE_CAT_NAME";

    @RBEntry("Assignments table related preferences.")
    public static final String ASSIGNMENTS_TABLE_CAT_DESCR = "ASSIGNMENTS_TABLE_CAT_DESCR";

    @RBEntry("Assignments table at Home-->Overview")
    @RBComment("Preferences for Assignments table at Home-->Overview.")
    public static final String HOME_OVRV_ASSIGNMENTS_TABLE_CAT_NAME = "HOME_OVRV_ASSIGNMENTS_TABLE_CAT_NAME";

    @RBEntry("Preferences for Assignments table at Home-->Overview.")
    public static final String HOME_OVRV_ASSIGNMENTS_TABLE_CAT_DESCR = "HOME_OVRV_ASSIGNMENTS_TABLE_CAT_DESCR";

    /**
     * removed QUERY_LIMIT_PREF ------------------------------------------------------
     **/
    @RBEntry("You will lose unsaved changes. Save the task page to preserve changes.")
    @RBComment("Confirmation message while setup participant table expansion on task detail page")
    public static final String UNSAVED_CHANGES_WARNING = "234";

    @RBEntry("At least one activity name and assignee is needed to start activity.")
    @RBComment("Validation error message when Adhoc activities are started without specifying Name and Assignee")
    public static final String ADHOC_VALIDATION_ERROR1 = "235";

    @RBEntry("Activity name and assignee are both required to start activities.")
    @RBComment("Validation error message when Adhoc activities are started without specifying Name and Assignee")
    public static final String ADHOC_VALIDATION_ERROR2 = "236";

    /*
     * Labels for Task Assistant
     */

    @RBEntry("Routing Options")
    @RBComment("Task Routing Options")
    public static final String TASK_ROUTING = "237";

    @RBEntry("Voting Options")
    @RBComment("Task voting options")
    public static final String TASK_VOTING = "238";

    @RBEntry("Unsaved changes for \"{0}\" will be lost, do you want to open task \"{1}\"?")
    @RBComment("Task Assistant confirmation for existing tracked task assistant")
    public static final String TASKBAR_CONFIRMATION = "239";

    @RBEntry("Name")
    public static final String WORKITEM_NAME = "250";

    @RBEntry("Context")
    public static final String WORKITEM_CONTEXT = "251";

    @RBEntry("Task Assistant")
    @RBComment("Title of task assistant")
    public static final String TASK_WINDOW_TITLE = "TASK_WINDOW_TITLE";

    @RBEntry("Subject:")
    @RBComment("Label for task assistant PBO")
    public static final String TASK_WINDOW_SUBJECT = "TASK_WINDOW_SUBJECT";

    @RBEntry("Task:")
    @RBComment("Label for workitem name on task assistant")
    public static final String TASK_WINDOW_TASK = "TASK_WINDOW_TASK";

    @RBEntry("Complete Task")
    @RBComment("Label for complete task button on task assistant")
    public static final String TASK_WINDOW_COMPLETE_TASK_BUTTON = "TASK_WINDOW_COMPLETE_TASK_BUTTON";

    @RBEntry("Save")
    @RBComment("Label for save button on task assistant")
    public static final String TASK_WINDOW_SAVE_BUTTON = "TASK_WINDOW_SAVE_BUTTON";

    @RBEntry("Continue Task Completion")
    @RBComment("Label for continue button on task assistant")
    public static final String TASK_WINDOW_CONTINUE_BUTTON = "TASK_WINDOW_CONTINUE_BUTTON";

    @RBEntry("Comment successfully saved.")
    @RBComment("Message after comment is saved from task assistant")
    public static final String TASK_WINDOW_SAVE_MESSAGE = "TASK_WINDOW_SAVE_MESSAGE";

    @RBEntry("Task successfully completed.")
    @RBComment("Message after task is completed from task assistant")
    public static final String TASK_WINDOW_COMPLETE_MESSAGE = "TASK_WINDOW_COMPLETE_MESSAGE";

    @RBEntry("There are unsaved changes, close Task Assistant?")
    @RBComment("Confirmation message to close task assistant")
    public static final String TASK_WINDOW_CLOSE_CONFIRMATION_MESSAGE = "TASK_WINDOW_CLOSE_CONFIRMATION_MESSAGE";

    /*
     * Labels for task details page
     */

    @RBEntry("Process Status")
    public static final String workitem_routingStatus_description = "workitem.routingStatus.description";

    @RBEntry("Process Status")
    public static final String workitem_routingStatus_tooltip = "workitem.routingStatus.tooltip";

    @RBEntry("newproject.gif")
    @RBPseudo(false)
    @RBComment("DO NOT TRANSLATE")
    public static final String workitem_routingStatus_icon = "workitem.routingStatus.icon";

    @RBEntry("Details")
    @RBComment("The Details tab on the info page for WorkItem")
    public static final String workitem_taskDetailsDetails_description = "object.taskDetailsDetails.description";

    @RBEntry("Template View")
    @RBComment("The main tab on the info page for WorkItem which will render task form template")
    public static final String workitem_taskFormTemplateDetails_description = "object.taskFormTemplateDetails.description";

    @RBEntry("Related")
    @RBComment("The Others tab on the info page for WorkItem")
    public static final String workitem_taskDetailsOthers_description = "object.taskDetailsOthers.description";

    @RBEntry("Attributes")
    @RBComment("The Attributes menu on the info page for WorkItem")
    public static final String workitem_attributes_description = "workitem.attributes.description";

    @RBEntry("Notebook")
    @RBComment("The Notebook menu on the info page for WorkItem")
    public static final String workitem_notebook_description = "workitem.notebook.description";

    @RBEntry("Discussions")
    @RBComment("The Discussions menu on the info page for WorkItem")
    public static final String workitem_discussions_description = "workitem.discussions.description";

    @RBEntry("Set Up Participants")
    @RBComment("The Set Up Participant tab on the info page for WorkItem")
    public static final String workitem_taskDetailsSetupParticipant_description = "object.taskDetailsSetupParticipant.description";

    @RBEntry("Ad Hoc Activities")
    @RBComment("The Adhoc Activities tab on the info page for WorkItem")
    public static final String workitem_taskDetailsAdhoc_description = "object.taskDetailsAdhoc.description";

    @RBEntry("Ad Hoc Activities")
    @RBComment("The Adhoc Activities customization action")
    public static final String workitem_adhocActivities_description = "workitem.adhocActivities.description";

    @RBEntry("Set Up Participants")
    @RBComment("Set Up PArticipant customization action")
    public static final String workitem_setupParticipant_description = "workitem.setupParticipant.description";

    @RBEntry("insert_multi_rows_below.gif")
    @RBPseudo(false)
    public static final String workitem_addMultipleObjects_icon = "workitem.addMultipleObjects.icon";

    @RBEntry("Add 5 Rows")
    @RBComment("Action to add 5 empty rows")
    public static final String workitem_addMultipleObjects_description = "workitem.addMultipleObjects.description";

    @RBEntry("Add 5 Rows")
    public static final String workitem_addMultipleObjects_tooltip = "workitem.addMultipleObjects.tooltip";

    @RBEntry("Special Instructions")
    @RBComment("Special instructions is a special Workflow veriable which is used in Change related Workflow.")
    public static final String SPECIAL_INSTRUCTIONS_LABEL = "special_instructions_label";

    @RBEntry("Template View")
    @RBComment("Label for Task Form Template action")
    public static final String workitem_taskFormTemplate_description = "workitem.taskFormTemplate.description";

    @RBEntry("This is an 'Ad Hoc Activities' task. Use the 'Ad Hoc Activities' tab to create and assign new ad hoc activities.")
    @RBComment("Inline help contents for ad hoc type of activity.")
    public static final String ADHOC_INLINE_HELP = "ADHOC_INLINE_HELP";

    @RBEntry("This is a 'Set Up Participants' task. Use the Set Up Participants tab to assign participants to different roles used in this process.")
    @RBComment("Inline help contents for set up participant type of activity.")
    public static final String SETUP_INLINE_HELP = "SETUP_INLINE_HELP";

    @RBEntry("This task is rendered using a task form template defined for this task type and primary business object type.")
    @RBComment("Inline help contents if task form template preference is set to true.")
    public static final String TEMPLATE_VIEW_INLINE_HELP = "TEMPLATE_VIEW_INLINE_HELP";

    @RBEntry("This task has been set as an 'Ad Hoc Activities' task and a 'Set Up Participants' task. Use the 'Ad Hoc Activities' tab to create and assign new ad hoc activities. Use the Set Up Participants tab to assign participants to different roles used in this process.")
    @RBComment("Inline help contents for set up as well as ad hoc type of activity.")
    public static final String SETUP_ADHOC_INLINE_HELP = "SETUP_ADHOC_INLINE_HELP";


	@RBEntry("Lifecycle History")
    public static final String PBOLEGACYHISTORY = "history.pboLegacyLCHistory.description";

	@RBEntry("Iteration History")
	public static final String PBOITERATIONHISTORY = "history.pboIterationHistory.description";

	@RBEntry("Maturity History")
    public static final String PBOMATURITYHISTORY = "history.pboMaturityHistory.description";

	@RBEntry("Affected Objects")
    public static final String TestAffectedData = "change.TestAffectedData.description";

	@RBEntry("Change Requests")
	public static final String TestRelatedChangeRequest = "change.TestRelatedChangeRequest.description";

	@RBEntry("Affected End Items")
	public static final String  TestAffectedEndItemsTable  = "change.TestAffectedEndItemsTable.description";

	@RBEntry("Attachments")
	public static final String  TestAttachment  = "change.TestAttachment.description";


	@RBEntry("Issues and Variances")
	public static final String TestRelatedChangeIssues = "change.TestRelatedChangeIssues.description";

	@RBEntry("Affected by Change Notices")
	public static final String  TestRelatedChangeNotice  = "change.TestRelatedChangeNotice.description";

	@RBEntry("Resulting from Change Notices")
	public static final String  TestResultingfromChangeNotice  = "change.TestResultingfromChangeNotice.description";


	@RBEntry("Version History")
	public static final String PBOVERSIONHISTORY = "history.pboVersionHistory.description";

	@RBEntry("Subject Attributes")
	public static final String WORKFLOW_TASK_PBO_ATTRIBUTES = "object.workflowTaskPboAttributes.description";

	@RBEntry("Actions on Subject")
	public static final String WORKFLOW_TASK_PBO_ACTIONS = "workitem.workflowTaskPboAction.description";

	@RBEntry("Task Completion Buttons")
	public static final String saveComplete = "workitem.saveComplete.description";

	@RBEntry("Task Variables")
	public static final String workItemActivityVariables = "workitem.workItemActivityVariables.description";

	@RBEntry("Task Completion")
	public static final String signOffComponent = "workitem.workItemSignOffComponent.description";

	@RBEntry("Task Variables")
	public static final String CUSTOM_VARIABLES_PANEL = "CUSTOM_VARIABLES_PANEL";

	@RBEntry("Task Completion")
	public static final String TASK_SIGN_OFF_PANEL = "TASK_SIGN_OFF_PANEL";

    //add by lzy 20191024 start
    @RBEntry("BOM change report")
    public static final String workitem_bomChangeReport_description = "workitem.bomChangeReport.description";
    //add by lzy 20191024 end
}
