<%@page pageEncoding="UTF-8"%>
<%@page import="ext.appo.ecn.common.util.*"%>
<%@ page language="java" isThreadSafe="true" contentType="text/html;charset=UTF-8"%>
<%  
	// 对象OID
	String itemOid = request.getParameter("itemOid");
	// 调用发放
	String method = request.getParameter("method");

    out.write(ChangeUtils.disposeAffectedEndItems(itemOid, method));   
%>