<%@page pageEncoding="UTF-8"%>
<%@page import="ext.appo.ecn.common.util.*"%>
<%@ page language="java" isThreadSafe="true" contentType="text/html;charset=UTF-8"%>
<%  
	// 用户信息
	String userName = request.getParameter("userName");

    out.write(ChangeUtils.searchUser(userName));   
%>