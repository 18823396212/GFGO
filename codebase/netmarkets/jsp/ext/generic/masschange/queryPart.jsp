<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@page import="wt.session.SessionHelper" %>
<%@page import="wt.part.WTPart"%>
<%@page import="wt.fc.ReferenceFactory"%>
<%@page import="ext.generic.masschange.util.MassChangeUtil"%>

<%
	response.setContentType("text/xml; charset=UTF-8");
	response.setHeader("Cache-Control", "no-cache");

	String oid = request.getParameter("oid");
	
	String number = "";
	String name = "";
	String view = "";
	
	if(oid != null && ( !oid.equals("") ) )	{ 
		ReferenceFactory rf = new ReferenceFactory();
		Object obj = rf.getReference(oid).getObject();
		WTPart part =(WTPart) obj ;
		number = MassChangeUtil.escape(part.getNumber());
		name = MassChangeUtil.escape(part.getName());
		view = MassChangeUtil.escape(part.getViewName());
	}
	
	out.println("<response>");
	out.println("<res>" + number + "</res>");
	out.println("<res>" + name + "</res>");
	out.println("<res>" + view + "</res>");
	out.println("</response>");
%>
