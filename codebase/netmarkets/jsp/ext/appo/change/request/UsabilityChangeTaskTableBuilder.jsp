<%@ page import="ext.appo.change.beans.ChangeTaskInfoBean" %>
<%@ page import="ext.appo.change.report.EcnChangeTaskService" %>
<%@ page import="ext.pi.core.PIWorkflowHelper" %>
<%@ page import="wt.change2.WTChangeOrder2" %>
<%@ page import="wt.fc.Persistable" %>
<%@ page import="wt.workflow.engine.WfProcess" %>
<%@ page import="wt.workflow.work.WfAssignedActivity" %>
<%@ page import="wt.workflow.work.WfAssignmentState" %>
<%@ page import="wt.workflow.work.WorkItem" %>
<%@ page import="static ext.appo.change.constants.ModifyConstants.TASK_3" %>
<%@ page import="java.text.ParsePosition" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Calendar" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/wrappers" prefix="w" %>
<%@ taglib prefix="wctags" tagdir="/WEB-INF/tags" %>
<%@ page contentType="text/html; charset=UTF-8" %>

<%
    //取路径
    wt.httpgw.URLFactory urlFactory = new wt.httpgw.URLFactory();
    String baseUrl = urlFactory.getBaseHREF();
    String expandImageUrl = baseUrl + "netmarkets/images/column_expand.gif";
    String collapseImageUrl = baseUrl + "netmarkets/images/column_collapse.gif";

    String isCompleted = "false";//默认流程未完成
    //通过oid获取流程项->流程
    String oid = request.getParameter("oid");
    WTChangeOrder2 ecn = null;
    String wfactivityname = "";
    List<ChangeTaskInfoBean> changeTaskInfoBeans = new ArrayList<>();
    List<ChangeTaskInfoBean> mergeBean = new ArrayList<>();
    Persistable persistable = EcnChangeTaskService.getObjectByOid(oid);
    if (persistable instanceof WorkItem) {
        WorkItem workItem = (WorkItem) persistable;
        WfAssignmentState status = workItem.getStatus();
        if (status.equals(WfAssignmentState.COMPLETED)) {
            isCompleted = "true";
        }
        WfAssignedActivity wfassignedactivity = (WfAssignedActivity) workItem.getSource().getObject();
        wfactivityname = wfassignedactivity.getName();

        WfProcess wfprocess = PIWorkflowHelper.service.getParentProcess(workItem);
        Object[] objects = wfprocess.getContext().getObjects();
        if (objects.length > 0) {
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] instanceof WTChangeOrder2) { //ecn
                    ecn = (WTChangeOrder2) objects[i];
                }
            }
        }
    }
    if (ecn != null) {
        //获取事务性任务(所有eca和未启动eca暂存的事务性任务)
        changeTaskInfoBeans = EcnChangeTaskService.getEcnChangeTask(ecn);
    }
    //读取事务性任务模板表
    List<ChangeTaskInfoBean> changeTaskInfoBeanList = EcnChangeTaskService.getChangeTaskTemplate();
    //合并模板表和事务性任务ChangeTaskInfoBean，是否存在相同任务主题,存在则不显示模板表对应ChangeTaskInfoBean
    mergeBean = EcnChangeTaskService.mergeChangeTaskInfoBean(changeTaskInfoBeanList, changeTaskInfoBeans);
%>
<style>
    img:hover {
        color: red;
        cursor: pointer;
    }

    .div_button:hover {
        cursor: pointer;
    }

    .div_button {
        border: none; /*去阴影*/
        border-radius: 4px;
        background-color: #169BD5;
        margin-left: 6px;
        color: white;
        height: 22px;
        margin-top: 4px;
    }

    .div_text {
        margin-left: 2px;
    }

    .div_input {
        width: 90%;
        margin-left: 2px;
    }

    .all_div {
        box-sizing: border-box;
        border: #B9D0FE 1px solid;
        align-items: center;
        display: flex;
        /*justify-content:center;*/
        /*padding: 0;*/
    }

    .all_div_center {
        box-sizing: border-box;
        border: #B9D0FE 1px solid;
        align-items: center;
        display: flex;
        justify-content: center;
        /*padding: 0;*/
    }

    .div_title1 {
        float: left;
        width: 3%;
        height: 30px;
        background: #EAF0FB;
    }

    .div_title2 {
        float: left;
        width: 2%;
        height: 30px;
        background: #EAF0FB;
    }

    .div_title3 {
        float: left;
        width: 10%;
        height: 30px;
        background: #EAF0FB;
    }

    .div_title4 {
        float: left;
        width: 11%;
        height: 30px;
        background: #EAF0FB;
    }

    .div_title5 {
        float: left;
        width: 11%;
        height: 30px;
        background: #EAF0FB;
    }

    .div_title6 {
        float: left;
        width: 15%;
        height: 30px;
        background: #EAF0FB;
    }

    .div_title7 {
        float: left;
        width: 12%;
        height: 30px;
        background: #EAF0FB;
    }

    .div_title8 {
        float: left;
        width: 15%;
        height: 30px;
        background: #EAF0FB;
    }

    .div_title9 {
        float: left;
        width: 3%;
        height: 30px;
        background: #EAF0FB;
    }

    .div_title10 {
        float: left;
        width: 8%;
        height: 30px;
        background: #EAF0FB;
    }

    .div_title11 {
        float: left;
        width: 10%;
        height: 30px;
        background: #EAF0FB;
    }

    .div_content1 {
        float: left;
        width: 3%;
        height: 30px;
    }

    .div_content2 {
        float: left;
        width: 2%;
        height: 30px;
    }

    .div_content3 {
        float: left;
        width: 10%;
        height: 30px;
    }

    .div_content4 {
        float: left;
        width: 11%;
        height: 30px;
    }

    .div_content5 {
        float: left;
        width: 11%;
        height: 30px;
    }

    .div_content6 {
        float: left;
        width: 15%;
        height: 30px;
    }

    .div_content7 {
        float: left;
        width: 12%;
        height: 30px;
    }

    .div_content8 {
        float: left;
        width: 15%;
        height: 30px;
    }

    .div_content9 {
        float: left;
        width: 3%;
        height: 30px;
    }

    .div_content10 {
        float: left;
        width: 8%;
        height: 30px;
    }

    .div_content11 {
        float: left;
        width: 10%;
        height: 30px;
    }

</style>
<form name="form1" action="" method="post">
    <input type="hidden" id="expandImageUrl" value="<%=expandImageUrl%>">
    <input type="hidden" id="collapseImageUrl" value="<%=collapseImageUrl%>">
    <input type="hidden" id="initTaskArray" name="initTaskArray" value=""/>
    <%
        if (ecn != null) {
    %>
    <input type="hidden" id="ecn" name="ecn" value="<%=ecn.getNumber()%>">
    <%
        }
    %>
    <input type="hidden" id="oid" name="ecn" value="<%=oid%>">
    <div style="display:inline-block;width: 99%">
        <div style="float: left;line-height: normal;"><img id="baseInfo" src="<%=collapseImageUrl%>"
                                                           onclick="showInfo(this)"/></div>
        <h3>*事务性任务</h3>
        <div id="taskDiv">
            <%
                if (!isCompleted.contains("true")) {
                    if (wfactivityname.contains("会签")) {
            %>
            <div style="margin-bottom: 4px;background: #EAF0FB;height: 30px; line-height: 30px;">
                <input type="button" class="div_button" value="启动任务跟踪" onclick="startTask()" style="width: 100px;"/>
                <input type="button" class="div_button" value="新增" onclick="addRow()" style="width: 40px;"/>
            </div>
            <%
            } else {
            %>
            <div style="margin-bottom: 4px;background: #EAF0FB;height: 30px; line-height: 30px;">
                <input type="button" class="div_button" value="保存" onclick="saveTask()" style="width: 40px;"/>
                <input type="button" class="div_button" value="新增" onclick="addRow()" style="width: 40px;"/>
            </div>
            <%
                    }
                }
            %>
            <div>
                <div class="all_div_center div_title1">
                    <span>序号</span>
                </div>
                <div class="all_div_center div_title2">
                    <input type="checkbox" id="isChoose" name="isChoose" onclick="selectCheckbox(this)"/>
                </div>
                <div class="all_div_center div_title3">
                    <span>*任务类型</span>
                </div>
                <div class="all_div_center div_title4">
                    <span>*任务主题</span>
                </div>
                <div class="all_div_center div_title5">
                    <span>管理方式</span>
                </div>
                <div class="all_div_center div_title6">
                    <span>*任务描述</span>
                </div>
                <div class="all_div_center div_title7">
                    <span>*期望完成时间</span>
                </div>
                <div class="all_div_center div_title8">
                    <span>*责任人</span>
                </div>
                <div class="all_div_center div_title9">
                    <span>状态</span>
                </div>
                <div class="all_div_center div_title10">
                    <span>任务单号</span>
                </div>
                <div class="all_div_center div_title11">
                    <span>实际完成时间</span>
                </div>
            </div>
            <%
                for (int i = 0; i < mergeBean.size(); i++) {
                    ChangeTaskInfoBean changeTaskInfoBean = mergeBean.get(i);
            %>
            <div>
                <div class="all_div_center div_content1">
                    <%=i + 1%>
                </div>
                <%
                    if (changeTaskInfoBean.getEca() == null) {
                        //任务主题为空，新增项，可编辑
                        String taskTheme = changeTaskInfoBean.getTaskTheme();
                %>
                <div class="all_div_center div_content2">
                    <input type="checkbox" class="taskCheckbox" name="taskCheckbox" value="<%=i + 1%>"/>
                </div>
                <div class="all_div div_content3">
                    <%
                        if (taskTheme == null || (taskTheme != null && taskTheme.isEmpty())) {
                    %>
                    <input type="text" id="taskType_<%=i + 1%>" name="taskType_<%=i + 1%>" class="div_input"
                           value="<%=changeTaskInfoBean.getTaskType()%>">
                    <%
                    } else {
                    %>
                    <%=changeTaskInfoBean.getTaskType()%>
                    <input type="hidden" id="taskType_<%=i + 1%>" name="taskType_<%=i + 1%>" class="div_input"
                           value="<%=changeTaskInfoBean.getTaskType()%>">
                    <%
                        }
                    %>
                </div>
                <div class="all_div div_content4">
                    <%
                        if (taskTheme == null || (taskTheme != null && taskTheme.isEmpty())) {
                    %>
                    <input type="text" id="taskTheme_<%=i + 1%>" name="taskTheme_<%=i + 1%>" class="div_input"
                           value="<%=changeTaskInfoBean.getTaskTheme()%>">
                    <%
                    } else {
                    %>
                    <%=changeTaskInfoBean.getTaskTheme()%>
                    <input type="hidden" id="taskTheme_<%=i + 1%>" name="taskTheme_<%=i + 1%>" class="div_input"
                           value="<%=changeTaskInfoBean.getTaskTheme()%>">
                    <%
                        }
                    %>
                </div>
                <div class="all_div div_content5">
                    <%
                        if (taskTheme == null || (taskTheme != null && taskTheme.isEmpty())) {
                    %>
                    <input type="text" id="glfs_<%=i + 1%>" name="glfs_<%=i + 1%>" class="div_input"
                           value="<%=changeTaskInfoBean.getGlfs()%>">
                    <%
                    } else {
                        if (changeTaskInfoBean.getGlfs() != null && !changeTaskInfoBean.getGlfs().isEmpty()) {
                    %>
                    <%=changeTaskInfoBean.getGlfs()%>
                    <input type="hidden" id="glfs_<%=i + 1%>" name="glfs_<%=i + 1%>" class="div_input"
                           value="<%=changeTaskInfoBean.getGlfs()%>">
                    <%
                    } else {
                    %>
                    <input type="text" id="glfs_<%=i + 1%>" name="glfs_<%=i + 1%>" class="div_input"
                           value="<%=changeTaskInfoBean.getGlfs()%>">
                    <%
                            }
                        }
                    %>
                </div>
                <div class="all_div div_content6">
                    <%
                        if (taskTheme != null && !taskTheme.isEmpty() && changeTaskInfoBean.getChangeDescribe() != null && !changeTaskInfoBean.getChangeDescribe().isEmpty()) {
                    %>
                    <%=changeTaskInfoBean.getChangeDescribe()%>
                    <input type="hidden" id="changeDescribe_<%=i + 1%>" name="changeDescribe_<%=i + 1%>"
                           class="div_input"
                           value="<%=changeTaskInfoBean.getChangeDescribe()%>">
                    <%
                    } else {
                    %>
                    <input type="text" id="changeDescribe_<%=i + 1%>" name="changeDescribe_<%=i + 1%>" class="div_input"
                           value="<%=changeTaskInfoBean.getChangeDescribe()%>">
                    <%
                        }
                    %>

                </div>
                <div class="all_div_center div_content7">
                    <%
                        String needDate = "needDate_" + (i + 1);
                        if (changeTaskInfoBean.getNeedDate() != null && !changeTaskInfoBean.getNeedDate().isEmpty()) {
                            String dateStr = changeTaskInfoBean.getNeedDate();
                            dateStr = dateStr.replace("-", "/");
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
                            ParsePosition pos = new ParsePosition(0);
                            Date date = formatter.parse(dateStr, pos);
                    %>
                    <w:dateInputComponent propertyLabel="DateInputComponent" styleClass="ppdata" id="<%=needDate%>"
                                          name="<%=needDate%>" dateValue="<%=date%>"
                                          required="true" dateValueType="DATE_ONLY"/>
                    <%
                    } else {
                        //期望完成时间默认推迟一周
                        Calendar curr = Calendar.getInstance();
                        curr.set(Calendar.DAY_OF_MONTH, curr.get(Calendar.DAY_OF_MONTH) + 7);
                        Date date = curr.getTime();
                    %>
                    <w:dateInputComponent propertyLabel="DateInputComponent" styleClass="ppdata" id="<%=needDate%>"
                                          name="<%=needDate%>" dateValue="<%=date%>"
                                          required="true" dateValueType="DATE_ONLY"/>
                    <%
                        }
                    %>
                </div>
                <div class="all_div_center div_content8">
                    <%
                        String responsible = "responsible_" + (i + 1);
                        if (changeTaskInfoBean.getResponsible() != null && !changeTaskInfoBean.getResponsible().isEmpty()) {
                    %>
                    <wctags:userPicker pickerTitle="搜索责任人" id="<%=responsible%>" label="" showSuggestion="true"
                                       pickerTextBoxLength="20"
                                       defaultValue="<%=changeTaskInfoBean.getResponsible()%>"
                                       readOnlyPickerTextBox="false"/>
                    <%
                    } else {
                    %>
                    <wctags:userPicker pickerTitle="搜索责任人" id="<%=responsible%>" label="" showSuggestion="true"
                                       pickerTextBoxLength="20"
                                       readOnlyPickerTextBox="false"/>
                    <%
                        }
                    %>
                </div>
                <div class="all_div_center div_content9">
                    <%=changeTaskInfoBean.getTaskState()%>
                </div>
                <div class="all_div_center div_content10">
                    <%=changeTaskInfoBean.getTaskNumber()%>
                </div>
                <div class="all_div_center div_content11">
                    <%=changeTaskInfoBean.getActualDate()%>
                </div>
                <%
                    if (changeTaskInfoBean.getTaskOid() != null && !changeTaskInfoBean.getTaskOid().isEmpty()) {
                %>
                <input type="hidden" name="taskOid_<%=i + 1%>" id="taskOid_<%=i + 1%>"
                       value="<%=changeTaskInfoBean.getTaskOid()%>"/>
                <%
                } else {
                %>
                <input type="hidden" name="taskOid_<%=i + 1%>" id="taskOid_<%=i + 1%>"
                       value=""/>
                <%
                    }
                } else {
                %>
                <div class="all_div_center div_content2">
                    <input type="hidden" value="<%=i + 1%>" name="hasEca"/>
                </div>
                <div class="all_div div_content3">
                    <span class="div_text"><%=changeTaskInfoBean.getTaskType()%></span>
                </div>
                <div class="all_div div_content4">
                    <span class="div_text"><%=changeTaskInfoBean.getTaskTheme()%></span>
                    <input type="hidden" id="taskTheme_<%=i + 1%>" name="taskTheme_<%=i + 1%>" class="div_input"
                           value="<%=changeTaskInfoBean.getTaskTheme()%>">
                </div>
                <div class="all_div div_content5">
                    <span class="div_text"><%=changeTaskInfoBean.getGlfs()%></span>
                </div>
                <div class="all_div div_content6">
                    <span class="div_text"><%=changeTaskInfoBean.getChangeDescribe()%></span>
                </div>
                <div class="all_div_center div_content7">
                    <%=changeTaskInfoBean.getNeedDate()%>
                </div>
                <div class="all_div_center div_content8" %>
                    <%=changeTaskInfoBean.getResponsible()%>
                </div>
                <%
                    if (changeTaskInfoBean.getTaskState() != null && changeTaskInfoBean.getTaskState().equals(TASK_3)) {
                %>
                <div class="all_div_center div_content9" style="color: red">
                    <%
                    } else {
                    %>
                    <div class="all_div_center div_content9">
                        <%
                            }
                        %>
                        <%=changeTaskInfoBean.getTaskState()%>
                    </div>
                    <div class="all_div_center div_content10">
                        <%
                            if (changeTaskInfoBean.getEca() != null) {
                        %>
                        <a href="<%=baseUrl%>app/#ptc1/tcomp/infoPage?oid=OR:<%=changeTaskInfoBean.getEca()%>&u8=1"
                           style="color: blue"><%=changeTaskInfoBean.getTaskNumber()%>
                        </a>
                        <%
                        } else {
                        %>
                        <%=changeTaskInfoBean.getTaskNumber()%>
                        <%
                            }
                        %>
                    </div>
                    <div class="all_div_center div_content11">
                        <%=changeTaskInfoBean.getActualDate()%>
                    </div>
                    <%
                        }
                    %>

                </div>
                <%
                    }
                %>
            </div>
        </div>
    </div>
</form>
<br/>
<input type="hidden" name="rowSize" id="rowSize" value="<%=mergeBean.size()%>"/>
<script type="text/javascript">

    //启动任务跟踪
    function startTask() {
        // 用于存储所有数据
        var tableRowArry = [];
        var boxs = document.querySelectorAll("#taskDiv input[type=checkbox]");
        var boxArrry = [];
        var uncheckboxArrry = [];
        for (let i = 0; i < boxs.length; i++) {
            if (boxs[i].id.indexOf('isChoose') > -1) continue;
            if (boxs[i].checked == true) {
                boxArrry.push(boxs[i]);
            } else {
                uncheckboxArrry.push(boxs[i]);
            }
        }
        if (boxArrry.length == 0) {
            alert("请至少选择一项任务！");
            return;
        }
        var taskThemeStrArray = [];
        //勾选的行校验【任务类型】,【任务主题】,【任务描述】,【期望完成时间】,【责任人】不能为空
        for (let i = 0; i < boxArrry.length; i++) {
            var boxValue = boxArrry[i].value;
            var taskType = document.getElementById("taskType_" + boxValue).value;
            var taskTheme = document.getElementById("taskTheme_" + boxValue).value;
            var changeDescribe = document.getElementById("changeDescribe_" + boxValue).value;
            var needDate = document.getElementById("needDate_" + boxValue).value;
            var responsible = document.getElementById("responsible_" + boxValue + "$label$").value;
            if (taskType == "" || taskTheme == "" || changeDescribe == "" || needDate == "" || responsible == "") {
                alert("【任务类型】,【任务主题】,【任务描述】,【期望完成时间】,【责任人】不能为空");
                return;
            }
            //比较日期，【期望完成时间】必须>=启动日期(当前日期)
            //获取当前时间yy/mm/dd
            var strDate = getStrDate();
            var strNeedDate = replace(needDate);
            if (strNeedDate != "") {
                var startDate = new Date(strNeedDate);
                var nowDate = new Date(strDate);
                if (startDate < nowDate) {
                    alert("【期望完成日期】不能小于当前日期");
                    return;
                }
            }
            for (let i = 0; i < uncheckboxArrry.length; i++) {
                var uncheckboxValue = uncheckboxArrry[i].value;
                var unchecktaskTheme = document.getElementById("taskTheme_" + uncheckboxValue).value;
                if (unchecktaskTheme == taskTheme) {
                    alert("提交的事务性任务已存在相同任务主题事务性任务！");
                    return;
                }
            }
            var hasEca = document.getElementsByName("hasEca");
            for (var j = 0; j < hasEca.length; j++) {
                var hasEcaValue = hasEca[j].value;
                var hasEcaTheme = document.getElementById("taskTheme_" + hasEcaValue).value;
                if (hasEcaTheme == taskTheme) {
                    alert("提交的事务性任务已存在相同任务主题事务性任务！");
                    return;
                }
            }
            for (let i = 0; i <taskThemeStrArray.length; i++) {
                var taskThemeStr = taskThemeStrArray[i];
                if (taskThemeStr == taskTheme) {
                    alert("提交的事务性任务中存在相同任务主题！");
                    return;
                }
            }
            taskThemeStrArray.push(taskTheme);
        }

        var result = confirm("启动任务跟踪");
        if (result == true) {
            document.getElementById("taskDiv").style.display = "none";
            var url = "<%=baseUrl%>netmarkets/jsp/ext/appo/change/request/startupTask.jsp";
            var ecnNumber = "<%=ecn.getNumber()%>";
            var param = "";
            var checkboxArray = [];
            //勾选列
            for (let i = 0; i < boxArrry.length; i++) {
                var boxValue = boxArrry[i].value;
                var taskTypeId = "taskType_" + boxValue;
                var taskType = document.getElementById(taskTypeId).value;
                var taskThemeId = "taskTheme_" + boxValue;
                var taskTheme = document.getElementById(taskThemeId).value;
                var changeDescribeId = "changeDescribe_" + boxValue;
                var changeDescribe = document.getElementById(changeDescribeId).value;
                var needDateId = "needDate_" + boxValue;
                var needDate = document.getElementById(needDateId).value;
                var responsibleId = "responsible_" + boxValue + "$label$";
                var responsible = document.getElementById(responsibleId).value;
                var glfsId = "glfs_" + boxValue;
                var glfs = document.getElementById(glfsId).value;
                var taskOid = "";
                var taskOidId = "taskOid_" + boxValue;
                if (document.getElementById(taskOidId)) {
                    taskOid = document.getElementById(taskOidId).value;
                }
                checkboxArray.push(boxValue);
                param += taskTypeId + "=" + taskType + "&" + taskThemeId + "=" + taskTheme + "&" + changeDescribeId + "=" + changeDescribe +
                    "&" + needDateId + "=" + needDate + "&" + responsibleId + "=" + responsible + "&" + glfsId + "=" + glfs + "&" + taskOidId + "=" + taskOid + "&"
            }
            param += "ecn=" + ecnNumber + "&taskCheckbox=" + JSON.stringify(checkboxArray);
            var result = ajaxRequest(url, param).trim();
            //保存事务任务信息(暂存的事务性任务)
            saveDate();
            //刷新事务性任务JSP
            if (result != null && result == "true") {
                startrefresh();
            } else {
                setTimeout("startrefresh()", 500);
            }
        } else {
            return;
        }
    }

    //保存
    function saveTask() {
        // 用于存储所有数据
        var tableRowArry = [];
        var boxs = document.querySelectorAll("#taskDiv input[type=checkbox]");
        var boxArrry = [];
        var uncheckboxArrry = [];
        for (let i = 0; i < boxs.length; i++) {
            if (boxs[i].id.indexOf('isChoose') > -1) continue;
            if (boxs[i].checked == true) {
                boxArrry.push(boxs[i]);
            } else {
                uncheckboxArrry.push(boxs[i]);
            }
        }
        if (boxArrry.length == 0) {
            alert("请至少选择一项任务！");
            return;
        }
        var taskThemeStrArray = [];
        //勾选的行校验【任务类型】,【任务主题】,【任务描述】,【期望完成时间】,【责任人】不能为空
        for (let i = 0; i < boxArrry.length; i++) {
            var boxValue = boxArrry[i].value;
            var taskType = trim(document.getElementById("taskType_" + boxValue).value);
            var taskTheme = trim(document.getElementById("taskTheme_" + boxValue).value);
            var changeDescribe = trim(document.getElementById("changeDescribe_" + boxValue).value);
            var needDate = trim(document.getElementById("needDate_" + boxValue).value);
            var responsible = trim(document.getElementById("responsible_" + boxValue + "$label$").value);
            if (taskType == "" || taskTheme == "" || changeDescribe == "" || needDate == "" || responsible == "") {
                alert("【任务类型】,【任务主题】,【任务描述】,【期望完成时间】,【责任人】不能为空");
                return;
            }
            //比较日期，【期望完成时间】必须>=启动日期(当前日期)
            //获取当前时间yy/mm/dd
            var strDate = getStrDate();
            var strNeedDate = replace(needDate);
            if (strNeedDate != "") {
                var startDate = new Date(strNeedDate);
                var nowDate = new Date(strDate);
                if (startDate < nowDate) {
                    alert("【期望完成日期】不能小于当前日期");
                    return;
                }
            }
            for (let i = 0; i < uncheckboxArrry.length; i++) {
                var uncheckboxValue = uncheckboxArrry[i].value;
                var unchecktaskTheme = document.getElementById("taskTheme_" + uncheckboxValue).value;
                if (unchecktaskTheme == taskTheme) {
                    alert("提交的事务性任务已存在相同任务主题事务性任务！");
                    return;
                }
            }
            var hasEca = document.getElementsByName("hasEca");
            for (var j = 0; j < hasEca.length; j++) {
                var hasEcaValue = hasEca[j].value;
                var hasEcaTheme = document.getElementById("taskTheme_" + hasEcaValue).value;
                if (hasEcaTheme == taskTheme) {
                    alert("提交的事务性任务已存在相同任务主题事务性任务！");
                    return;
                }
            }
            for (let i = 0; i <taskThemeStrArray.length; i++) {
                var taskThemeStr = taskThemeStrArray[i];
                if (taskThemeStr == taskTheme) {
                    alert("提交的事务性任务中存在相同任务主题！");
                    return;
                }
            }
            taskThemeStrArray.push(taskTheme);
        }

        var result = confirm("保存任务");
        if (result == true) {
            document.getElementById("taskDiv").style.display = "none";
            var url = "<%=baseUrl%>netmarkets/jsp/ext/appo/change/request/saveTask.jsp";
            var ecnNumber = "<%=ecn.getNumber()%>";
            var param = "";
            var checkboxArray = [];
            //勾选列
            for (let i = 0; i < boxArrry.length; i++) {
                var boxValue = boxArrry[i].value;
                var taskTypeId = "taskType_" + boxValue;
                var taskType = document.getElementById(taskTypeId).value;
                var taskThemeId = "taskTheme_" + boxValue;
                var taskTheme = document.getElementById(taskThemeId).value;
                var changeDescribeId = "changeDescribe_" + boxValue;
                var changeDescribe = document.getElementById(changeDescribeId).value;
                var needDateId = "needDate_" + boxValue;
                var needDate = document.getElementById(needDateId).value;
                var responsibleId = "responsible_" + boxValue + "$label$";
                var responsible = document.getElementById(responsibleId).value;
                var glfsId = "glfs_" + boxValue;
                var glfs = document.getElementById(glfsId).value;
                var taskOid = "";
                var taskOidId = "taskOid_" + boxValue;
                if (document.getElementById(taskOidId)) {
                    taskOid = document.getElementById(taskOidId).value;
                }
                checkboxArray.push(boxValue);
                param += taskTypeId + "=" + taskType + "&" + taskThemeId + "=" + taskTheme + "&" + changeDescribeId + "=" + changeDescribe +
                    "&" + needDateId + "=" + needDate + "&" + responsibleId + "=" + responsible + "&" + glfsId + "=" + glfs + "&" + taskOidId + "=" + taskOid + "&"
            }
            param += "ecn=" + ecnNumber + "&taskCheckbox=" + JSON.stringify(checkboxArray);
            var result = ajaxRequest(url, param).trim();
            //保存事务任务信息(暂存的事务性任务)
            saveDate();
            //刷新事务性任务JSP
            if (result != null && result == "true") {
                startrefresh();
            } else {
                setTimeout("startrefresh()", 500);
            }
        } else {
            return;
        }
    }

    //获取当前日期yy/mm/dd
    function getStrDate() {
        var date = new Date();
        var today = date.toISOString().substring(0, 10);
        today = replace(today);
        return today;
    }

    //替换日期yy-mm-dd为yy/mm/dd
    function replace(date) {
        date = date.replace(/-/g, "/");
        return date;
    }

    //新增一行
    function addRow() {
        //保存事务任务信息(暂存的事务性任务)
        saveDate();
        var url = "netmarkets/jsp/ext/appo/change/request/newTransactionTask.jsp";
        var result = ajaxRequest(url, {oid: "<%=oid%>"}).trim();
        //刷新事务性任务JSP
        setTimeout("startrefresh()", 100);
    }

    //保存事务任务信息
    function saveDate() {
        // 用于存储所有数据
        var tableRowArry = [];
        // 获取页面所有input控件
        var inputFormArray = document.getElementsByTagName("input");
        for (var j = 0; j < inputFormArray.length; j++) {
            var columnArray = {};
            var inputForm = inputFormArray[j];
            var taskThemeId = inputForm.id;
            if (taskThemeId.indexOf("taskTheme_") > -1) {
                //获取对应列数据
                var id = taskThemeId.substring(taskThemeId.lastIndexOf('_') + 1, taskThemeId.length);
                var taskTypeId = "taskType_" + id;
                var taskType = "";
                if (document.getElementById(taskTypeId))
                    taskType = document.getElementById(taskTypeId).value;
                var taskThemeId = "taskTheme_" + id;
                var taskTheme = "";
                if (document.getElementById(taskThemeId))
                    taskTheme = document.getElementById(taskThemeId).value;
                var changeDescribeId = "changeDescribe_" + id;
                var changeDescribe = "";
                if (document.getElementById(changeDescribeId))
                    changeDescribe = document.getElementById(changeDescribeId).value;
                var needDateId = "needDate_" + id;
                var needDate = "";
                if (document.getElementById(needDateId))
                    needDate = document.getElementById(needDateId).value;
                var responsibleId = "responsible_" + id + "$label$";
                var responsible = "";
                if (document.getElementById(responsibleId))
                    responsible = document.getElementById(responsibleId).value;
                var glfsId = "glfs_" + id;
                var glfs = "";
                if (document.getElementById(glfsId))
                    glfs = document.getElementById(glfsId).value;
                var taskOidId = "taskOid_" + id;
                var taskOid = "";
                if (document.getElementById(taskOidId))
                    taskOid = document.getElementById(taskOidId).value;
                columnArray['taskOid'] = taskOid;
                columnArray['taskType'] = taskType;
                columnArray['taskTheme'] = taskTheme;
                columnArray['changeDescribe'] = changeDescribe;
                columnArray['needDate'] = needDate;
                columnArray['responsible'] = responsible;
                columnArray['glfs'] = glfs;
                tableRowArry.push(columnArray);
            }
            // var taskId = inputForm.id;
            // var taskOid = inputForm.value;
            // if (inputForm.type == 'hidden' && taskId.indexOf("taskOid_") > -1) {
            //     //获取对应列数据
            //     var id = taskId.substring(taskId.lastIndexOf('_') + 1, taskId.length);
            //     var taskTypeId = "taskType_" + id;
            //     var taskType = "";
            //     if (document.getElementById(taskTypeId))
            //         taskType = document.getElementById(taskTypeId).value;
            //     var taskThemeId = "taskTheme_" + id;
            //     var taskTheme = "";
            //     if (document.getElementById(taskThemeId))
            //         taskTheme = document.getElementById(taskThemeId).value;
            //     var changeDescribeId = "changeDescribe_" + id;
            //     var changeDescribe = "";
            //     if (document.getElementById(changeDescribeId))
            //         changeDescribe = document.getElementById(changeDescribeId).value;
            //     var needDateId = "needDate_" + id;
            //     var needDate = "";
            //     if (document.getElementById(needDateId))
            //         needDate = document.getElementById(needDateId).value;
            //     var responsibleId = "responsible_" + id + "$label$";
            //     var responsible = "";
            //     if (document.getElementById(responsibleId))
            //         responsible = document.getElementById(responsibleId).value;
            //     var glfsId = "glfs_" + id;
            //     var glfs = "";
            //     if (document.getElementById(glfsId))
            //         glfs = document.getElementById(glfsId).value;
            //     columnArray['taskOid'] = taskOid;
            //     columnArray['taskType'] = taskType;
            //     columnArray['taskTheme'] = taskTheme;
            //     columnArray['changeDescribe'] = changeDescribe;
            //     columnArray['needDate'] = needDate;
            //     columnArray['responsible'] = responsible;
            //     columnArray['glfs'] = glfs;
            //     tableRowArry.push(columnArray);
            // }

        }
        document.getElementById("cacheTaskArray").value = JSON.stringify(tableRowArry);
    }

    function ajaxRequest(url, params) {
        var options = {
            asynchronous: false,
            parameters: params,
            method: 'POST'
        };
        var transport = requestHandler.doRequest(url, options);
        return transport.responseText;
    }

    //全选/全不选
    function selectCheckbox(e) {
        var checkbox = document.getElementsByClassName("taskCheckbox");
        if (checkbox.length > 0) {
            for (let i = 0; i < checkbox.length; i++) {
                if (e.checked == true) {
                    checkbox[i].checked = true;
                } else {
                    checkbox[i].checked = false;
                }
            }
        }
    }

    //展示当前信息
    function showInfo(e) {
        var expandImageUrl = document.getElementById("expandImageUrl").value;
        var collapseImageUrl = document.getElementById("collapseImageUrl").value;
        var display = document.getElementById("taskDiv").style.display;
        if (display.trim() == "" || display == "block") {
            e.src = expandImageUrl;
            document.getElementById("taskDiv").style.display = "none";
        } else {
            e.src = collapseImageUrl;
            document.getElementById("taskDiv").style.display = "block";
        }
    }

    //回填事务任务信息(暂存的事务性任务)
    function cacheTaskWriteBack() {
        if (document.getElementById("cacheTaskArray")) {
            // 获取页面所有input控件
            var inputFormArray = document.getElementsByTagName("input");
            if (trim(document.getElementById("cacheTaskArray").value) != "") {
                // 获取原有数据
                var cacheTaskArray = eval("(" + document.getElementById("cacheTaskArray").value + ")");
                if (cacheTaskArray.length > 0) {
                    for (var i = 0; i < cacheTaskArray.length; i++) {
                        var datasArray = cacheTaskArray[i];
                        for (var j = 0; j < inputFormArray.length; j++) {
                            var inputForm = inputFormArray[j];
                            if (inputForm.id.indexOf('taskOid_') > -1) {
                                if (trim(inputForm.value) != "") {
                                    if (datasArray['taskOid'].indexOf(inputForm.value) > -1) {
                                        //获取当前序号
                                        var id = inputForm.id.substring(inputForm.id.lastIndexOf('_') + 1, inputForm.id.length);
                                        var taskTypeId = "taskType_" + id;
                                        if (document.getElementById(taskTypeId) && document.getElementById(taskTypeId).type == 'text')
                                            document.getElementById(taskTypeId).value = datasArray['taskType'];
                                        var taskThemeId = "taskTheme_" + id;
                                        if (document.getElementById(taskThemeId) && document.getElementById(taskThemeId).type == 'text')
                                            document.getElementById(taskThemeId).value = datasArray['taskTheme'];
                                        var changeDescribeId = "changeDescribe_" + id;
                                        if (document.getElementById(changeDescribeId) && document.getElementById(changeDescribeId).type == 'text')
                                            document.getElementById(changeDescribeId).value = datasArray['changeDescribe'];
                                        var needDateId = "needDate_" + id;
                                        if (document.getElementById(needDateId) && document.getElementById(needDateId).type == 'text')
                                            document.getElementById(needDateId).value = datasArray['needDate'];
                                        var responsibleId = "responsible_" + id + "$label$";
                                        if (document.getElementById(responsibleId) && document.getElementById(responsibleId).type == 'text')
                                            document.getElementById(responsibleId).value = datasArray['responsible'];
                                        var glfsId = "glfs_" + id;
                                        if (document.getElementById(glfsId) && document.getElementById(glfsId).type == 'text')
                                            document.getElementById(glfsId).value = datasArray['glfs'];
                                    }
                                } else {
                                    //获取当前序号
                                    var id = inputForm.id.substring(inputForm.id.lastIndexOf('_') + 1, inputForm.id.length);
                                    var taskThemeId = "taskTheme_" + id;
                                    if (document.getElementById(taskThemeId) && datasArray['taskTheme'].indexOf(trim(document.getElementById(taskThemeId).value)) > -1) {
                                        var taskTypeId = "taskType_" + id;
                                        if (document.getElementById(taskTypeId) && document.getElementById(taskTypeId).type == 'text')
                                            document.getElementById(taskTypeId).value = datasArray['taskType'];
                                        if (document.getElementById(taskThemeId) && document.getElementById(taskThemeId).type == 'text')
                                            document.getElementById(taskThemeId).value = datasArray['taskTheme'];
                                        var changeDescribeId = "changeDescribe_" + id;
                                        if (document.getElementById(changeDescribeId) && document.getElementById(changeDescribeId).type == 'text')
                                            document.getElementById(changeDescribeId).value = datasArray['changeDescribe'];
                                        var needDateId = "needDate_" + id;
                                        if (document.getElementById(needDateId) && document.getElementById(needDateId).type == 'text')
                                            document.getElementById(needDateId).value = datasArray['needDate'];
                                        var responsibleId = "responsible_" + id + "$label$";
                                        if (document.getElementById(responsibleId) && document.getElementById(responsibleId).type == 'text')
                                            document.getElementById(responsibleId).value = datasArray['responsible'];
                                        var glfsId = "glfs_" + id;
                                        if (document.getElementById(glfsId) && document.getElementById(glfsId).type == 'text')
                                            document.getElementById(glfsId).value = datasArray['glfs'];
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    //去左右空格;
    function trim(s) {
        return s.replace(/(^\s*)|(\s*$)/g, "");
    }

    cacheTaskWriteBack();
</script>
