<%@page pageEncoding="UTF-8"%>
<%@page import="ext.appo.change.util.*"%>
<%@ page language="java" isThreadSafe="true" contentType="text/html;charset=UTF-8"%>
<%
	// 对象OID
	String itemOid = request.getParameter("itemOid");

    out.write(CollectDrawingUtil.collectDrawing(itemOid));
%>
