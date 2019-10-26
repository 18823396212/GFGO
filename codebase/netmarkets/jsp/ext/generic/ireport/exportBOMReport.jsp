<%@page pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@page import="java.util.*,java.io.*,java.net.URLEncoder,wt.util.WTException,ext.generic.ireport.report.*" %>

<%
		try{
			ExportBomLevelReport ebr = new ExportBomLevelReport();
			ebr.exportBOM(request, response);
			out.clear();
			out = pageContext.pushBody();
		 } catch (Exception e) {
			e.printStackTrace();
		}finally{
			/*if(excelFile != null){
				excelFile.delete();	
			}*/
	}
		  
%>