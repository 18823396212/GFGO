<%@page import="ext.appo.doc.uploadDoc.UploadHistoricalData"%>
<%@page import="ext.appo.doc.uploadDoc.UploadHistoricalDataDoc"%>
<%@page import="wt.maturity.PromotionNotice"%>
<%@page import="org.apache.poi.ss.usermodel.Workbook"%>
<%@ page language="java" session="true" pageEncoding="UTF-8"%>
<jsp:useBean id="wtcontext" class="wt.httpgw.WTContextBean" scope="request" />
<jsp:setProperty name="wtcontext" property="request" value="<%=request%>" />
<jsp:useBean id="url_factory" class="wt.httpgw.URLFactory" scope="request">
	<%
	    url_factory.setRequestURL(request.getScheme(), request.getHeader("HOST"), request.getRequestURI());
	%>
</jsp:useBean>
<jsp:useBean id="localeBean" class="com.ptc.netmarkets.util.beans.NmLocaleBean" scope="request" />
<%@page import="java.util.*"%>
<%@page import="java.io.OutputStream"%>
<%@ page import="java.util.*"%>
<%@page import="wt.util.Encoder"%>
<%@page import="wt.part.*"%>
<%@page import="wt.fc.*"%>
<jsp:useBean id="commandBean" class="com.ptc.netmarkets.util.beans.NmCommandBean" scope="request"/>
<html>
<head>
<title>ExportToExcel</title>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
</head>


<body>
<%


        UploadHistoricalData helper = new UploadHistoricalData();

        try {
            Workbook wb = helper.exportData();
            if (wb != null) {
            	String fileName = "发送全部部件清单";
                java.util.Date curTime = new java.util.Date();
                response.reset();
                response.setContentType("application/vnd.ms-excel");
                response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gbk"),"iso8859-1")+".xlsx");
                out.clear();
                out = pageContext.pushBody();
                OutputStream os = response.getOutputStream();
                //out.clear();
                wb.write(os);
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

<script type="text/javascript">

 </script>

</html>