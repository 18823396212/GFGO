<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="ext.appo.change.report.ExportChangeHistoryHelper" %>
<%
    ExportChangeHistoryHelper helper = new ExportChangeHistoryHelper();
    helper.exportData(request, response);
    out.clear();
    out = pageContext.pushBody();
%>
