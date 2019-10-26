<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page import="ext.yulong.plm.pdm.report.StatChangeInfoService" %>

<%
response.setContentType("text/html; charset=UTF-8");
String states = "";
String objectType = request.getParameter("objectType");

if(null != objectType && !"".equals(objectType))
{
    states = StatChangeInfoService.getStates(objectType);
    
    if(objectType == null)
    {
        states = "";
    }
}
out.clearBuffer();         
out.write(states);
out.flush();
%>