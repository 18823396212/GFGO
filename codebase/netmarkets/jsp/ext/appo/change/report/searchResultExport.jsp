<%@ page import="ext.appo.change.beans.EcnChangeTaskBean" %>
<%@ page import="ext.appo.change.report.EcnChangeTaskService" %>
<%@ page import="java.util.List" %>
<%@ page import="ext.appo.change.report.ChangeInfoExport" %>
<%@ page language="java" pageEncoding="UTF-8" %>

<body>

<%
    //任务状态-默认勾选-进行中，已超期
    String ecnCreator = request.getParameter("ecnCreator");//申请人
    if (ecnCreator == null) ecnCreator = "";
    String ecnNumber = request.getParameter("ecnNumber");//ECN编号
    if (ecnNumber == null) ecnNumber = "";
    String changeReasonDes = request.getParameter("changeReasonDes");//变更原因说明
    if (changeReasonDes == null) changeReasonDes = "";
    String taskState_inProgress = request.getParameter("taskState_inProgress");//任务状态-进行中
    if (taskState_inProgress == null) taskState_inProgress = "";
    String taskState_overdue = request.getParameter("taskState_overdue");//任务状态-已超期
    if (taskState_overdue == null) taskState_overdue = "";
    String taskState_closed = request.getParameter("taskState_closed");//任务状态-已关闭
    if (taskState_closed == null) taskState_closed = "";
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

    List<EcnChangeTaskBean> resultList = EcnChangeTaskService.getSearchResult(ecnCreator, ecnNumber, changeReasonDes, taskState_inProgress, taskState_overdue, taskState_closed
            , responsible, taskTheme, startNeedDate, endNeedDate, startActualDate, endActualDate, affectedProductNumber, productType, projectName);

    ChangeInfoExport excleExport = new ChangeInfoExport();
    excleExport.exportSearchResult(request, response, resultList);
    //去掉getOutpurStream()异常
    out.clear();
    out = pageContext.pushBody();
%>
</body>