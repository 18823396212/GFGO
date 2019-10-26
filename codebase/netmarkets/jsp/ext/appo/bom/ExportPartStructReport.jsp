<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="ext.appo.bom.export.ExportPartStructHelper"%>
<%
	ExportPartStructHelper helper = new ExportPartStructHelper() ;
    helper.exportData(request,response);
	out.clear();
	out = pageContext.pushBody();
%>
