<%@page import="com.ptc.netmarkets.model.NmOid"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.sql.Timestamp"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.io.FileInputStream"%>
<%@page import="java.io.File"%>
<%@page import="wt.content.ContentHelper"%>
<%@page import="wt.content.ContentRoleType"%>
<%@page import="wt.change2.WTChangeOrder2"%>
<%@page import="wt.content.ContentServerHelper"%>
<%@page import="java.io.InputStream"%>
<%@page import="ext.test.CreatePdf"%>
<%@page import="ext.appo.ecn.pdf.PdfUtil"%>
<%@page import="wt.maturity.PromotionNotice"%>
<%@page import="wt.workflow.work.WorkItem"%>
<%@page import="org.apache.poi.ss.usermodel.Workbook"%>
<%@ page contentType="text/html; charset=gb2312" language="java" errorPage=""%>
<jsp:useBean id="wtcontext" class="wt.httpgw.WTContextBean" scope="request" />
<jsp:setProperty name="wtcontext" property="request" value="<%=request%>" />
<jsp:useBean id="url_factory" class="wt.httpgw.URLFactory" scope="request">
	<%
	    url_factory.setRequestURL(request.getScheme(), request.getHeader("HOST"), request.getRequestURI());
	%>
</jsp:useBean>
<jsp:useBean id="localeBean" class="com.ptc.netmarkets.util.beans.NmLocaleBean" scope="request" />
<jsp:useBean id="commandBean" class="com.ptc.netmarkets.util.beans.NmCommandBean" scope="request"/>
<%@page import="java.util.*"%>
<%@page import="java.io.OutputStream"%>
<%@ page import="java.util.*"%>
<%@page import="wt.util.Encoder"%>
<%@page import="wt.part.*"%>
<%@page import="wt.fc.*"%>
<html>
<head>
<title>ExportToExcel</title>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
</head>
<body>
<% 
              String soid = request.getParameter("oid");
              Persistable per = PdfUtil.getObjectByOid(soid);
	          WTChangeOrder2 eco = null;
	          if(per instanceof WTChangeOrder2)
			  {
				    eco = (WTChangeOrder2)per;
			  }

 	         String filePath = new CreatePdf().generatePDFs(eco);

  	  
        try {
        	InputStream in = new FileInputStream(filePath);
            if (in != null) {
                java.util.Date curTime = new java.util.Date();
                SimpleDateFormat formater = new SimpleDateFormat("yyyyMMddHHmmss");
                formater.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
                String fileName = eco.getName()+"_"+formater.format(new Timestamp(System.currentTimeMillis()))+".pdf";
                response.reset();
            	response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"),"ISO8859-1"));
                out.clear();
               	out = pageContext.pushBody();
                OutputStream os = response.getOutputStream();
                
                byte[] b = new byte[1024];
				
				int len = 0;
				while((len = in.read(b)) != -1){
					os.write(b,0,len);
				} 
				
                //out.clear();
                in.close();
               	os.flush();
               	os.close();
         	  	out.clear();
               	out = pageContext.pushBody();
            } else {
                response.setContentType("text/html");
                out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
                out.println("<HTML>");
                out.println("  <HEAD><TITLE>Result</TITLE></HEAD>");
                out.println("  <BODY>");
                out.println("导出异常，请联系管理员.");
                out.println("  <BR>");
                out.println("  </BODY>");
                out.println("</HTML>");
                out.flush();
                out.close();
            }
        } catch (Exception e) {
            response.setContentType("text/html");
            out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
            out.println("<HTML>");
            out.println("  <HEAD><TITLE>Result</TITLE></HEAD>");
            out.println("  <BODY>");
            out.println("导出异常:");
            out.println("<BR>");
            out.println(e);
            out.println("  <BR>");
            out.println("  </BODY>");
            out.println("</HTML>");
            out.flush();
            out.close();
        }
%>
</body>

</html>