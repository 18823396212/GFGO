<%@page import="ext.generic.reviewObject.util.ReviewObjectUtil"%>

<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%> 

<%
String oid = request.getParameter("oid");

ReviewObjectUtil reviewObjectUtil = new ReviewObjectUtil();

reviewObjectUtil.reviewObjManualRefresh(oid);
      
%>
