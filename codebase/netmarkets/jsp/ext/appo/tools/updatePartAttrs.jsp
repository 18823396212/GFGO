<%@ page import="wt.org.WTPrincipal" %>
<%@ page import="wt.session.SessionHelper" %>
<%@ page import="java.util.List" %>
<%@ page import="ext.appo.test.tools.UpdateAttrsTools" %>
<%@ page import="java.util.ArrayList" %>

<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
	out.println("========udpate part attr start========");
	out.println("<br>");

	WTPrincipal sessionUser = SessionHelper.manager.getPrincipal();
	SessionHelper.manager.setAdministrator();

	List<String> msgs = new ArrayList<>();
	String operType = request.getParameter("operType");
	if("typeA".equals(operType)) {
		msgs = UpdateAttrsTools.updateAttrsTransA();
	}else if("typeB".equals(operType)){
		msgs = UpdateAttrsTools.updateAttrsTransB();
	}
	for(String str : msgs){
	    out.println(str);
	}

	SessionHelper.manager.setPrincipal(sessionUser.getName());

	out.println("========udpate part attr end========");
%>