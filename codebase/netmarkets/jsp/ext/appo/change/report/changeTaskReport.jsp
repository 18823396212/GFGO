<%@page import="ext.appo.change.beans.ChangeTaskInfoBean" %>
<%@ include file="/netmarkets/jsp/util/beginPopup.jspf" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/wrappers" prefix="w" %>
<%@ page language="java" import="ext.appo.change.beans.EcnChangeTaskBean" pageEncoding="UTF-8" %>
<%@ taglib prefix="wctags" tagdir="/WEB-INF/tags" %>
<%@ page import="ext.appo.change.report.EcnChangeTaskService" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="static ext.appo.change.constants.ModifyConstants.TASK_3" %>
<%@ page import="wt.util.WTProperties" %>

<%
    //取路径
    wt.httpgw.URLFactory urlFactory = new wt.httpgw.URLFactory();
    String baseUrl = urlFactory.getBaseHREF();
    String searchUrl = baseUrl + "netmarkets/images/search.gif";
    String resetUrl = baseUrl + "netmarkets/images/reset.gif";
    String excelExportUrl = baseUrl + "netmarkets/images/excel_export.gif";

%>
<style type="text/css">
    .head_checkbox {
        vertical-align: middle;
    }

    .div_title {
        float: left;
        width: 7%;
        height: 30px;
        background: #EAF0FB;
    }

    .div_content {
        float: left;
        width: 26%;
        height: 30px;
    }

    .head_div_div {
        box-sizing: border-box;
        border: #B9D0FE 1px solid;
        align-items: center;
        display: flex;
        /*justify-content:center;*/
        /*padding: 0;*/
    }

    .div_content_span {
        margin-left: 3px;
    }

    .head_div_date {
        box-sizing: border-box;
        border: #B9D0FE 1px solid;
        line-height: 30px;
    }

    .td_center {
        text-align: center;
    }

    .head_div_input {
        width: 80%;
    }

    .th_head {
        background: #D2E1FD;
        text-align: center;
    }
    .taskTable{
        table-layout: fixed;
    }
    .taskTable TD {
        background: white;
    }
</style>
<head>
    <title>事务性任务跟踪报表</title>
</head>

<%
    //获取oid
    String oid = request.getParameter("oid");//申请人
    if (oid == null) oid = "";

    //任务状态-默认勾选-进行中，已超期
    String ecnCreator = request.getParameter("ecnCreator");//申请人
    if (ecnCreator == null) ecnCreator = "";
    String ecnNumber = request.getParameter("ecnNumber");//ECN编号
    if (ecnNumber == null) ecnNumber = "";
    String changeReasonDes = request.getParameter("changeReasonDes");//变更原因说明
    if (changeReasonDes == null) changeReasonDes = "";
    String taskState_inProgress = request.getParameter("taskState_inProgress");//任务状态-进行中
    if (taskState_inProgress == null) taskState_inProgress = "checked";
    String taskState_overdue = request.getParameter("taskState_overdue");//任务状态-已超期
    if (taskState_overdue == null) taskState_overdue = "checked";
    String taskState_closed = request.getParameter("taskState_closed");//任务状态-已关闭
    if (taskState_closed == null) taskState_closed = "unchecked";
    String responsible = request.getParameter("responsible");//责任人
    if (responsible == null) responsible = "";
    String taskTheme = request.getParameter("taskTheme");//任务主题
    if (taskTheme == null) taskTheme = "";
    String startNeedDate = request.getParameter("startNeedDate");//期望完成日期-开始时间
    if (startNeedDate == null) startNeedDate = "";
    String endNeedDate = request.getParameter("endNeedDate");//期望完成日期-结束时间
    if (endNeedDate == null) endNeedDate = "";
    String startActualDate = request.getParameter("startActualDate");//实际完成日期-开始时间
    if (startActualDate == null) startActualDate = "";
    String endActualDate = request.getParameter("endActualDate");//实际完成日期-结束时间
    if (endActualDate == null) endActualDate = "";
    String affectedProductNumber = request.getParameter("affectedProductNumber");//受影响的产品编号
    if (affectedProductNumber == null) affectedProductNumber = "";
    String productType = request.getParameter("productType");//所属产品类别
    if (productType == null) productType = "";
    String projectName = request.getParameter("projectName");//所属项目
    if (projectName == null) projectName = "";
    List<EcnChangeTaskBean> resultList = new ArrayList<>();
    resultList = EcnChangeTaskService.getSearchResult(ecnCreator.trim(), ecnNumber.trim(), changeReasonDes.trim(), taskState_inProgress.trim(), taskState_overdue.trim(), taskState_closed.trim()
            , responsible.trim(), taskTheme.trim(), startNeedDate.trim(), endNeedDate.trim(), startActualDate.trim(), endActualDate.trim(), affectedProductNumber.trim(), productType.trim(), projectName.trim());
%>

<body>
<div id="head_title" style="background: #D2E1FD;">
    <span style="margin-left: 4px;">事务性任务跟踪报表</span>
</div>
<form name="form" action="">
    <div class="head_div">
        <div>
            <img src="netmarkets/images/invite_edit.gif"><font size="2"> 请填写查询条件（至少输入一个）</font>
        </div>
        <div>
            <div class="div_title head_div_div">
                申请人
            </div>
            <div class="div_content head_div_div">
                <span class="div_content_span"></span>
                <wctags:userPicker id="ecnCreator" pickerTitle="搜索申请人" label="" readOnlyPickerTextBox="false"/>
            </div>
            <div class="div_title head_div_div">
                ECN编号
            </div>
            <div class="div_content head_div_div">
                <span class="div_content_span"></span>
                <input class="head_div_input" type="text" name="ecnNumber" id="ecnNumber" placeholder="请输入完整信息"
                       value="<%=ecnNumber%>"/>
            </div>
            <div class="div_title head_div_div">
                变更原因说明
            </div>
            <div class="div_content head_div_div" style="width: 27%">
                <span class="div_content_span"></span>
                <input class="head_div_input" type="text" name="changeReasonDes" id="changeReasonDes"
                       placeholder="支持模糊查询" value="<%=changeReasonDes%>"/>
            </div>
        </div>

        <div>
            <div class="div_title head_div_div">
                任务状态
            </div>
            <div class="div_content head_div_div">
                <span class="div_content_span"></span>
                <input type="checkbox" id="taskState_inProgress" name="taskState_inProgress" class="head_checkbox"/>进行中
                <span class="div_content_span"></span>
                <input type="checkbox" id="taskState_overdue" name="taskState_overdue" class="head_checkbox"/>已超期
                <span class="div_content_span"></span>
                <input type="checkbox" id="taskState_closed" name="taskState_closed" class="head_checkbox"/>已关闭
            </div>
            <div class="div_title head_div_div">
                责任人
            </div>
            <div class="div_content head_div_div">
                <span class="div_content_span"></span>
                <wctags:userPicker id="responsible" pickerTitle="搜索责任人" label="" readOnlyPickerTextBox="false"/>
            </div>
            <div class=" div_title head_div_div">
                任务主题
            </div>
            <div class="div_content head_div_div" style="width: 27%">
                <span class="div_content_span"></span>
                <input class="head_div_input" type="text" name="taskTheme" id="taskTheme"
                       placeholder="支持模糊查询" value="<%=taskTheme%>"/>
            </div>
        </div>

        <div>
            <div class="div_title head_div_div">
                期望完成日期
            </div>
            <div class="div_content head_div_date">
                <span class="div_content_span"></span>
                <w:dateInputComponent propertyLabel="DateInputComponent" id="startNeedDate" name="startNeedDate"
                                      required="true" dateValueType="DATE_ONLY"/>
                <span class="div_content_span"></span>
                to:
                <span class="div_content_span"></span>
                <w:dateInputComponent propertyLabel="DateInputComponent" id="endNeedDate" name="endNeedDate"
                                      required="true" dateValueType="DATE_ONLY"/>
            </div>
            <div class="div_title head_div_div">
                实际完成日期
            </div>
            <div class="div_content head_div_date">
                <span class="div_content_span"></span>
                <w:dateInputComponent propertyLabel="DateInputComponent" id="startActualDate" name="startActualDate"
                                      required="true" dateValueType="DATE_ONLY"/>
                <span class="div_content_span"></span>
                to:
                <span class="div_content_span"></span>
                <w:dateInputComponent propertyLabel="DateInputComponent" id="endActualDate" name="endActualDate"
                                      required="true" dateValueType="DATE_ONLY"/>
            </div>
            <div class="div_title head_div_div">
                受影响的产品编号
            </div>
            <div class="div_content head_div_div" style="width: 27%">
                <span class="div_content_span"></span>
                <input class="head_div_input" type="text" name="affectedProductNumber" id="affectedProductNumber"
                       placeholder="请输入完整信息" value="<%=affectedProductNumber%>"/>
            </div>
        </div>

        <div>
            <div class="div_title head_div_div">
                所属产品类别
            </div>
            <div class="div_content head_div_div">
                <span class="div_content_span"></span>
                <input class="head_div_input" type="text" name="productType" id="productType"
                       placeholder="支持模糊查询" value="<%=productType%>"/>
            </div>
            <div class="div_title head_div_div">
                所属项目
            </div>
            <div class="div_content head_div_div">
                <span class="div_content_span"></span>
                <input class="head_div_input" type="text" name="projectName" id="projectName"
                       placeholder="支持模糊查询" value="<%=projectName%>"/>
            </div>
            <div class="div_title head_div_div">
            </div>
            <div class="div_content head_div_div" style="width: 27%">
                <span class="div_content_span"></span>
            </div>
        </div>
        <div align="right" style="border: #B9D0FE 1px solid;">
<%--            <img src="<%=searchUrl%>" onclick="doSearchAppo()" />--%>
<%--            <img src="<%=resetUrl%>" onclick="resetForm()" />--%>
<%--            <img src="<%=excelExportUrl%>" onclick="exportExcelAppo()" />--%>
            <input type="button" value="查询" onclick="javascript:doSearchAppo();"/>
            <input type="button" value="重置" onclick="resetForm()"/>
            <input type="button" value="导出" onclick="exportExcelAppo()" style="margin: 4px"/>
        </div>
    </div>
</form>
<br/>
<table class="taskTable" border="1" style="background: #C6D5F5;width: 100%;">
    <tr>
        <th scope="col" NOWRAP class="th_head" colspan="1">
            <span>序号</span>
        </th>
        <th scope="col" NOWRAP class="th_head" colspan="3">
            <span>ECN编号</span>
        </th>
        <th scope="col" NOWRAP class="th_head" colspan="6">
            <span>ECN变更原因说明</span>
        </th>
        <th scope="col" NOWRAP class="th_head" colspan="3">
            <span>任务类型</span>
        </th>
        <th scope="col" NOWRAP class="th_head" colspan="4">
            <span>任务主题</span>
        </th>
        <th scope="col" NOWRAP class="th_head" colspan="3">
            <span>管理方式</span>
        </th>
        <th scope="col" NOWRAP class="th_head" colspan="6">
            <span>任务描述</span>
        </th>
        <th scope="col" NOWRAP class="th_head" colspan="3">
            <span>期望完成时间</span>
        </th>
        <th scope="col" NOWRAP class="th_head" colspan="3">
            <span>责任人</span>
        </th>
        <th scope="col" NOWRAP class="th_head" colspan="2">
            <span>状态</span>
        </th>
        <th scope="col" NOWRAP class="th_head" colspan="3">
            <span>任务单号</span>
        </th>
        <th scope="col" NOWRAP class="th_head" colspan="3">
            <span>实际完成时间</span>
        </th>
    </tr>

    <%
        if (resultList != null && resultList.size() > 0) {
            int id = 1;
            for (int i = 0; i < resultList.size(); i++) {
                EcnChangeTaskBean bean = resultList.get(i);
                int rows = 1;
                if (bean.getChangeTaskBeans().size() > 0) {
                    rows = bean.getChangeTaskBeans().size();
                }
                for (int j = 0; j < rows; j++) {
    %>
    <tr>
        <td class="td_center" colspan="1"><%=id + j%>
        </td>
        <%
            if (j == 0) {
        %>
        <td class="td_center" rowspan="<%=rows%>" colspan="3"><%=bean.getEcnNumber()%>
        </td>
        <td rowspan="<%=rows%>" colspan="6"><%=bean.getChangeDescription()%>
        </td>
        <%
            }
            if (bean.getChangeTaskBeans() != null && bean.getChangeTaskBeans().size() > 0) {
                ChangeTaskInfoBean changeTaskBean = bean.getChangeTaskBeans().get(j);
        %>
        <td colspan="3"><%=changeTaskBean.getTaskType()%>
        </td>
        <td colspan="4"><%=changeTaskBean.getTaskTheme()%>
        </td>
        <td colspan="3"><%=changeTaskBean.getGlfs()%>
        </td>
        <td colspan="6"><%=changeTaskBean.getChangeDescribe()%>
        </td>
        <td class="td_center" colspan="3"><%=changeTaskBean.getNeedDate()%>
        </td>
        <td class="td_center" colspan="3"><%=changeTaskBean.getResponsible()%>
        </td>
        <%
            if (changeTaskBean.getTaskState() != null && changeTaskBean.getTaskState().equals(TASK_3)) {
        %>
        <td class="td_center" style="color: red" colspan="2">
                <%
            }else{
                %>
        <td class="td_center" colspan="2">
            <%
                }
            %>
            <%=changeTaskBean.getTaskState()%>
        </td>
        <td class="td_center" colspan="3">
            <%
                if (changeTaskBean.getEca() != null) {
            %>
            <a href="<%=baseUrl%>app/#ptc1/tcomp/infoPage?oid=OR:<%=changeTaskBean.getEca()%>&u8=1" style="color: blue"><%=changeTaskBean.getTaskNumber()%></a>
            <%
            } else {
            %>
            <%=changeTaskBean.getTaskNumber()%>
            <%
                }
            %>
        </td>
        <td class="td_center" colspan="3"><%=changeTaskBean.getActualDate()%>
        </td>
        <%
        } else {
        %>
        <td colspan="3"></td>
        <td colspan="4"></td>
        <td colspan="3"></td>
        <td colspan="6"></td>
        <td colspan="3"></td>
        <td colspan="3"></td>
        <td colspan="2"></td>
        <td colspan="3"></td>
        <td colspan="3"></td>
        <%
            }

        %>
    </tr>
    <%
                }
                id += rows;
            }
        }
    %>
</table>
<iframe id="exportFrame" name="exportFrame" height="0" width="0"></iframe>
</body>

<script type="text/javascript">
    //查询
    function doSearchAppo() {
        var ecnCreator = document.getElementById("ecnCreator$label$").value;
        var ecnNumber = document.getElementById("ecnNumber").value;
        var changeReasonDes = document.getElementById("changeReasonDes").value;
        var taskState_inProgressDoc = document.getElementById("taskState_inProgress");
        var taskState_inProgress = "unchecked";
        if (taskState_inProgressDoc.checked) {
            taskState_inProgress = "checked";
        }
        var taskState_overdueDoc = document.getElementById("taskState_overdue");
        var taskState_overdue = "unchecked";
        if (taskState_overdueDoc.checked) {
            taskState_overdue = "checked";
        }
        var taskState_closedDoc = document.getElementById("taskState_closed");
        var taskState_closed = "unchecked";
        if (taskState_closedDoc.checked) {
            taskState_closed = "checked";
        }
        var responsible = document.getElementById("responsible$label$").value;
        var taskTheme = document.getElementById("taskTheme").value;
        var startNeedDate = document.getElementById("startNeedDate").value;
        var endNeedDate = document.getElementById("endNeedDate").value;
        var startActualDate = document.getElementById("startActualDate").value;
        var endActualDate = document.getElementById("endActualDate").value;
        var affectedProductNumber = document.getElementById("affectedProductNumber").value;
        var productType = document.getElementById("productType").value;
        var projectName = document.getElementById("projectName").value;
        //获取当前时间yy/mm/dd
        var strDate = getStrDate();
        var startNeedDate = replace(startNeedDate);
        var endNeedDate = replace(endNeedDate);
        var startActualDate = replace(startActualDate);
        var endActualDate = replace(endActualDate);
        //比较日期，开始时间不能大于结束时间
        if (startNeedDate != "" && endNeedDate != "") {
            var startDate = new Date(startNeedDate);
            var endDate = new Date(endNeedDate);
            if (endDate < startDate) {
                alert("【期望完成日期】结束时间不能小于开始时间");
                return;
            }
        }
        if (startActualDate != "" && endActualDate != "") {
            var startDate = new Date(startActualDate);
            var endDate = new Date(endActualDate);
            if (endDate < startDate) {
                alert("【实际完成日期 】结束时间不能小于开始时间");
                return;
            }
        }
        var url = "<%=baseUrl%>app/#ptc1/ext/appo/change/report/changeTaskReport?oid=<%=oid%>&ecnCreator=" + ecnCreator + "&ecnNumber=" + ecnNumber + "&changeReasonDes=" + changeReasonDes +
            "&taskState_inProgress=" + taskState_inProgress + "&taskState_overdue=" + taskState_overdue + "&taskState_closed=" + taskState_closed + "&responsible=" + responsible +
            "&taskTheme=" + taskTheme + "&startNeedDate=" + startNeedDate + "&endNeedDate=" + endNeedDate + "&startActualDate=" + startActualDate + "&endActualDate=" + endActualDate +
            "&affectedProductNumber=" + affectedProductNumber + "&productType=" + productType + "&projectName=" + projectName;
        // alert("url==" + url)
        window.open(url, '_self');
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

    //导出报表
    function exportExcelAppo() {
        var size = "<%=resultList.size()%>";
        if (size > 0) {
            var ecnCreator = "<%=ecnCreator%>";
            var ecnNumber = "<%=ecnNumber%>";
            var changeReasonDes = "<%=changeReasonDes%>";
            var taskState_inProgress = "<%=taskState_inProgress%>";
            var taskState_overdue = "<%=taskState_overdue%>";
            var taskState_closed = "<%=taskState_closed%>";
            var responsible = "<%=responsible%>";
            var taskTheme = "<%=taskTheme%>";
            var startNeedDate = "<%=startNeedDate%>";
            var endNeedDate = "<%=endNeedDate%>";
            var startActualDate = "<%=startActualDate%>";
            var endActualDate = "<%=endActualDate%>";
            var affectedProductNumber = "<%=affectedProductNumber%>";
            var productType = "<%=productType%>";
            var projectName = "<%=projectName%>";
            var url = "/Windchill/netmarkets/jsp/ext/appo/change/report/searchResultExport.jsp?oid=<%=oid%>&ecnCreator=" + ecnCreator + "&ecnNumber=" + ecnNumber + "&changeReasonDes=" + changeReasonDes +
                "&taskState_inProgress=" + taskState_inProgress + "&taskState_overdue=" + taskState_overdue + "&taskState_closed=" + taskState_closed + "&responsible=" + responsible +
                "&taskTheme=" + taskTheme + "&startNeedDate=" + startNeedDate + "&endNeedDate=" + endNeedDate + "&startActualDate=" + startActualDate + "&endActualDate=" + endActualDate +
                "&affectedProductNumber=" + affectedProductNumber + "&productType=" + productType + "&projectName=" + projectName;
            //屏蔽掉导出时显示白色的窗口
            window.frames['exportFrame'].location = url;
        }
    }

    //重置条件
    function resetForm() {
        document.getElementById("ecnCreator$label$").value = "";
        document.getElementById("ecnNumber").value = "";
        document.getElementById("changeReasonDes").value = "";
        document.getElementById("taskState_inProgress").checked = true;
        document.getElementById("taskState_overdue").checked = true;
        document.getElementById("taskState_closed").checked = false;
        document.getElementById("responsible$label$").value = "";
        document.getElementById("taskTheme").value = "";
        document.getElementById("startNeedDate").value = "";
        document.getElementById("endNeedDate").value = "";
        document.getElementById("startActualDate").value = "";
        document.getElementById("endActualDate").value = "";
        document.getElementById("affectedProductNumber").value = "";
        document.getElementById("productType").value = "";
        document.getElementById("projectName").value = "";

    }

    //初始化查询条件
    function init() {
        var ecnCreator = "<%=ecnCreator%>";
        var ecnNumber = "<%=ecnNumber%>";
        var changeReasonDes = "<%=changeReasonDes%>";
        var taskState_inProgress = "<%=taskState_inProgress%>";
        var taskState_overdue = "<%=taskState_overdue%>";
        var taskState_closed = "<%=taskState_closed%>";
        var responsible = "<%=responsible%>";
        var taskTheme = "<%=taskTheme%>";
        var startNeedDate = "<%=startNeedDate%>";
        var endNeedDate = "<%=endNeedDate%>";
        var startActualDate = "<%=startActualDate%>";
        var endActualDate = "<%=endActualDate%>";
        var affectedProductNumber = "<%=affectedProductNumber%>";
        var productType = "<%=productType%>";
        var projectName = "<%=projectName%>";
        document.getElementById("ecnCreator$label$").value = ecnCreator;
        document.getElementById("ecnNumber").value = ecnNumber;
        document.getElementById("changeReasonDes").value = changeReasonDes;
        if (taskState_inProgress == "checked") {
            document.getElementById("taskState_inProgress").checked = true;
        } else {
            document.getElementById("taskState_inProgress").checked = false;
        }
        if (taskState_overdue == "checked") {
            document.getElementById("taskState_overdue").checked = true;
        } else {
            document.getElementById("taskState_overdue").checked = false;
        }
        if (taskState_closed == "checked") {
            document.getElementById("taskState_closed").checked = true;
        } else {
            document.getElementById("taskState_closed").checked = false;
        }
        document.getElementById("responsible$label$").value = responsible;
        document.getElementById("taskTheme").value = taskTheme;
        document.getElementById("startNeedDate").value = startNeedDate;
        document.getElementById("endNeedDate").value = endNeedDate;
        document.getElementById("startActualDate").value = startActualDate;
        document.getElementById("endActualDate").value = endActualDate;
        document.getElementById("affectedProductNumber").value = affectedProductNumber;
        document.getElementById("productType").value = productType;
        document.getElementById("projectName").value = projectName;
    }

    init();
</script>


<%@ include file="/netmarkets/jsp/util/end.jspf" %>