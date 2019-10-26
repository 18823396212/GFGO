<%@page import="ext.appo.part.util.EffecitveBaselineUtil"%>
<%@page import="ext.appo.part.processor.ExportEffecitveBaselineProcessor"%>
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
	ArrayList soids = commandBean.getSelectedOidForPopup();  


	List<String> list =  EffecitveBaselineUtil.getSelectedOid(soids);
    //List<String> list = new ArrayList<>();
	System.out.println("soids========================================"+soids);
	//for(int i=0;i<soids.size();i++){
	//    list.add(soids.get(i).toString());
	//}
	String oid = "";
  	String datatime = "";
  	ExportEffecitveBaselineProcessor helper = new ExportEffecitveBaselineProcessor();
	List<String> dataCount = null;
	System.out.println("list========================================"+list);
	if(!list.isEmpty()){
		for(String oidStr : list){
				if(oidStr.contains("___")){
					System.out.println("oidStr========================================"+oidStr);
  					//String oid = request.getParameter("oid");
         			String arry[] = oidStr.split("___");
        			oid = arry[5];
         			datatime = arry[13];
				}else if(!oidStr.contains("___")){
					 oid = list.get(5);
					 datatime = list.get(13);
				}
	System.out.println("oidStr=====================oid==================="+oid);
	System.out.println("oidStr=====================datatime==================="+datatime);
         	dataCount = ExportEffecitveBaselineProcessor.checkDataCount(oid,datatime);
         	
         	if(dataCount.size() > 1){
        try {
            Workbook wb = helper.exportReport(oid,datatime);
            if (wb != null) {
            	String fileName = "有效基线数据";
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
		}
		}
	 }
	
	int count = 0;
	if(dataCount != null){
		count =  dataCount.size();
	}
%>

<input type="hidden" id="setp" name="setp" value=<%=list.toString() %> />
<input type="hidden" id="countData" name="countData" value=<%=count%> />
</body>

<script type="text/javascript">

window.onload = function () {
	 var oid = document.getElementById("setp").value;
	 var count = document.getElementById("countData").value;
	if(oid == null || oid == "" || oid =="[]"){
		alert("至少选中一行记录！");
		window.close();
	}else if(parseInt(count) < 2){
		alert("选中数据没有下层！");
		window.close();
	}
	
};

 </script>

</html>