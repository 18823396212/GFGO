<%@page pageEncoding="UTF-8"%>
<%@page import="ext.appo.ecn.beans.ChangeTaskBean"%>
<%
	ChangeTaskBean changeTaskBean = new ChangeTaskBean() ;
	String lineID = changeTaskBean.getOid().toString() ;
	
    out.write(lineID);   
%>