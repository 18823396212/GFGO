<%@page pageEncoding="UTF-8"%>
<%@page import="ext.appo.ecn.beans.ChangeTaskBean"%>
<%@page import="com.ptc.netmarkets.model.NmOid"%>
<%  
	ChangeTaskBean changeTaskBean = new ChangeTaskBean() ;
	String lineID = changeTaskBean.getOid().toString() ;
	
    out.write(lineID);   
%>