<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="ext.appo.statReport.statChangeInfo.StatChangeInfoExport"%>
<%@ page import="ext.appo.statReport.statChangeInfo.StatChangeInfoService" %>
<%@ page import="ext.appo.statReport.statChangeInfo.bean.ChangeInfoBean" %>

<body>
	
	<%
	String ecnNumber = request.getParameter("ecnNumber");
	String effectObjectNo = request.getParameter("effectObjectNo");
	String projectName = request.getParameter("projectName");
	
	
	String fromTime = request.getParameter("fromTime");
    if (fromTime == null)
    {
        fromTime = "";
    }
    String toTime = request.getParameter("toTime");
    if (toTime == null)
    {
        toTime = "";
    }
	    
    List<ChangeInfoBean> resultList  = StatChangeInfoService.getSearchResult(ecnNumber, effectObjectNo,projectName, fromTime, toTime);
	    
	    StatChangeInfoExport excleExport = new StatChangeInfoExport();
		excleExport.exportSearchResult(request, response, resultList);
		  	 
		//去掉getOutpurStream()异常
	    out.clear();
	    out = pageContext.pushBody();
    %>

</body>