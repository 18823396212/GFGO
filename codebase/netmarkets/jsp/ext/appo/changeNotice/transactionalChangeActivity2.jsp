<%@page pageEncoding="UTF-8"%>
<%@page import="wt.fc.PersistenceHelper"%>
<%@page import="ext.appo.ecn.common.util.*"%>
<%@ page language="java" isThreadSafe="true" contentType="text/html;charset=UTF-8"%>
<%  
	// 更改主图
	String changeTheme = request.getParameter("changeTheme");
	// 更改说明
	String changeDescribe = request.getParameter("changeDescribe");
	// 责任人
	String responsible = request.getParameter("responsible");
	// 关联的ECA
	String changeActivity2 = request.getParameter("changeActivity2");
	// 选择对象的OID
	String seleteOID = request.getParameter("seleteOID");
	// 表单中原有数据
	String allDatas = request.getParameter("allDatas");
	
    out.write(ChangeUtils.createEditChangeTaskBean(changeTheme, changeDescribe, responsible, changeActivity2, seleteOID, allDatas));   
%>