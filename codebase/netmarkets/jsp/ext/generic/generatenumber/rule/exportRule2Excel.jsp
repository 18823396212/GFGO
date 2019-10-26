<%@page import="org.apache.poi.ss.usermodel.Workbook,wt.session.SessionHelper,java.net.URLEncoder" %>
<%@page import="java.io.OutputStream,ext.generic.generatenumber.rule.util.*" %>
<%@ page pageEncoding="UTF-8"%>
<%
	try{
		String name = SessionHelper.getPrincipal().getName();
		String prefixName = "NumberRule-"+name+"-"+System.currentTimeMillis();

		String exportFileName = URLEncoder.encode(prefixName,"UTF-8")+ ".xlsx";
		response.reset();
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-disposition", "attachment; filename=" + exportFileName);
		OutputStream outputStream = response.getOutputStream();

		ClfNoRuleExcel cre = new ClfNoRuleExcel();
		Workbook wb = cre.exportNoRules();
		wb.write(outputStream);
		outputStream.flush();
		out.clear();
		out = pageContext.pushBody();
	 } catch (Exception e) {
		e.printStackTrace();
	}finally{
	}
		  
%>