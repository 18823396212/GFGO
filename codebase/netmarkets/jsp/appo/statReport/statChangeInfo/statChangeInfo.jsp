<%@page import="wt.fc.ReferenceFactory"%>
<%@ include file="/netmarkets/jsp/util/beginPopup.jspf"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/wrappers" prefix="w"%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="ext.appo.statReport.statChangeInfo.StatChangeInfoService" %>
<%@ page import="ext.appo.statReport.statChangeInfo.bean.ChangeInfoBean" %>

<%
    //取路径
    wt.httpgw.URLFactory urlFactory = new wt.httpgw.URLFactory();
    String baseUrl = urlFactory.getBaseHREF();
    
 
    String findImageUrl = baseUrl + "netmarkets/images/appo/icon_search1.gif";
    String requestURL = request.getRequestURL().toString();
    
    
%>
<link href="<%=baseUrl%>netmarkets/css/ylCustom2.css" type="text/css" rel="stylesheet">
<head>
	<title>变更信息执行情况统计</title>

</head>

<%
	String ecnNumber = request.getParameter("ecnNumber");
if (ecnNumber == null)
{
	ecnNumber = "";
}
	String effectObjectNo = request.getParameter("effectObjectNo");
	 if (effectObjectNo == null)
	    {
		 effectObjectNo = "";
	    }
	String projectName = request.getParameter("projectName");
	 if (projectName == null)
	    {
		 projectName = "";
	    }
	String appoaction = request.getParameter("appoaction");
	
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
    List<ChangeInfoBean> resultList = new ArrayList<ChangeInfoBean>();
   
    resultList  = StatChangeInfoService.getSearchResult(ecnNumber, effectObjectNo,projectName, fromTime, toTime);
	
%>

<body>
		<form name="form1" action="">
			<input type="hidden" id="appoaction" name="appoaction">
			<legend>
				<font size="2">查询条件</font>
			</legend>
			<br />
			<table border="0" cellspacing="1" cellpadding="1"
				class=tb_searchbar>
				
				
				<tr>
					<td class="td_title">
						*ECN编号:
					</td>
					<td class="ppData">
						<input type="text" name="ecnNumber"  id="ecnNumber" value="<%=ecnNumber %>" /> 
					</td>
					<td class="td_title">
						*受影响对象编号:
					</td>
					<td class="ppData">
						<input type="text" name="effectObjectNo" id="effectObjectNo" value="<%=effectObjectNo %>" /> 
					</td>
				</tr>
				
				<tr>
					<td class="td_title">
						*所属项目:
					</td>
					<td class="ppData">
						<input type="text" name="projectName" id="projectName" value="<%=projectName %>" /> 
					</td>
					<td></td>
					<td></td>
				</tr>
				
				
				<tr>
					<td colspan="4" align="right">
						<input class="button" type="button" value="查询" onclick="javascript:doSearchAppo();" />
						<input class="button" type="reset" value="清空" />
					</td>
				</tr>
			</table>
		</form>

		<table parseWidgets="false" class="tb_datalist" border="1">
			<tr class="tr_title">
				<td colspan=18>
					<h1>
						<div class="title displayInlineClass"
							id="headerDiv_table__wt.workflow.engine.WfProcess.defaultSearchView_TABLE">
							变更信息统计
						</div>
					</h1>
					<input type="button" value="导出" onclick="exportExcelAppo()" />
				</td>
			</tr>
			<tr>
				<th rowspan="2" scope="col" NOWRAP class="tablecolumnheaderbg">
					<span class="tablecolumnheaderfont">No.</span>
				</th>
				<th scope="col" NOWRAP class="tablecolumnheaderbg" colspan="7" align="center">
					<span class="tablecolumnheaderfont">变更信息</span>
				</th>
				<th scope="col" NOWRAP class="tablecolumnheaderbg" colspan="4" align="center">
					<span class="tablecolumnheaderfont">受影响对象信息</span>
				</th>
				<th scope="col" NOWRAP class="tablecolumnheaderbg" colspan="6" align="center">
					<span class="tablecolumnheaderfont">库存等信息</span>
				</th>
				
			</tr>
			<tr>	
				<th scope="col" NOWRAP class="tablecolumnheaderbg" align="center">
					<span class="tablecolumnheaderfont">ECN创建者</span>
				</th>
				<th scope="col" NOWRAP class="tablecolumnheaderbg" align="center">
					<span class="tablecolumnheaderfont">ECN编号</span>
				</th>							
				<th scope="col" NOWRAP class="tablecolumnheaderbg" align="center">
					<span class="tablecolumnheaderfont">ECN名称</span>
				</th>	
				<th scope="col" NOWRAP class="tablecolumnheaderbg" align="center">
					<span class="tablecolumnheaderfont">变更原因&nbsp;&nbsp;&nbsp;</span>
				</th>						
				<th scope="col" NOWRAP class="tablecolumnheaderbg" align="center">
					<span class="tablecolumnheaderfont">变更原因说明</span>
				</th>							
				<th scope="col" NOWRAP class="tablecolumnheaderbg" align="center">
					<span class="tablecolumnheaderfont">所属产品类别</span>
				</th>							
				<th scope="col" NOWRAP class="tablecolumnheaderbg" align="center">
					<span class="tablecolumnheaderfont">所属项目</span>
				</th>							
				<th scope="col" NOWRAP class="tablecolumnheaderbg" align="center">
					<span class="tablecolumnheaderfont">受影响对象状态</span>
				</th>							
				<th scope="col" NOWRAP class="tablecolumnheaderbg" align="center">
					<span class="tablecolumnheaderfont">受影响对象编号&nbsp;&nbsp;&nbsp;&nbsp;</span>
				</th>							
				<th scope="col" NOWRAP class="tablecolumnheaderbg" align="center">
					<span class="tablecolumnheaderfont">受影响对象名称</span>
				</th>							
				<th scope="col" NOWRAP class="tablecolumnheaderbg" align="center">
					<span class="tablecolumnheaderfont">受影响对象版本</span>
				</th>							
				<th scope="col" NOWRAP class="tablecolumnheaderbg" align="center">
					<span class="tablecolumnheaderfont">在制数量</span>
				</th>
				
				<th scope="col" NOWRAP class="tablecolumnheaderbg" align="center">
					<span class="tablecolumnheaderfont">在制处理措施</span>
				</th>
				<th scope="col" NOWRAP class="tablecolumnheaderbg" align="center">
					<span class="tablecolumnheaderfont">在途数量</span>
				</th>
				<th scope="col" NOWRAP class="tablecolumnheaderbg" align="center">
					<span class="tablecolumnheaderfont">在途处理措施</span>
				</th>
				<th scope="col" NOWRAP class="tablecolumnheaderbg" align="center">
					<span class="tablecolumnheaderfont">库存数量</span>
				</th>
				<th scope="col" NOWRAP class="tablecolumnheaderbg" align="center">
					<span class="tablecolumnheaderfont">库存处理措施</span>
				</th>
			
			</tr>
			
			<%
	    		if(resultList != null && resultList.size() > 0)
	    		{    
	    			String priEcnNumber = "";
					for (int i = 0; i < resultList.size(); i++)
	    			{
						ChangeInfoBean bean = resultList.get(i);
			%>
			<tr>
				<td><%=i+1%></td>
				<%
				if(!priEcnNumber.equals(bean.getEcnNumber())) {
				%>
				<td rowspan="<%=bean.getLine() %>"><%=bean.getEcnCreator()%></td>
				<td rowspan="<%=bean.getLine() %>"><%=bean.getEcnNumber()%></td>
				<td rowspan="<%=bean.getLine() %>"><%=bean.getEcnName()%></td>
				<td rowspan="<%=bean.getLine() %>"><%=bean.getChangeReason()%></td>
				<td rowspan="<%=bean.getLine() %>"><%=bean.getChangeReasonDes()%></td>
				<td rowspan="<%=bean.getLine() %>"><%=bean.getProductType()%></td>
				<td rowspan="<%=bean.getLine() %>"><%=bean.getProjectName()%></td>
				<%} %>
				<td><%=bean.getEffectObjectState()%></td>
				<td><%=bean.getEffectObjectNo()%></td>
				<td><%=bean.getEffectObjectName()%></td>
				<td><%=bean.getEffectObjectVesion()%></td>
				<td><%=bean.getInProcessQuantities()%></td>
				<td><%=bean.getProcessingMeasures()%></td>
				<td><%=bean.getOnthewayQuantity()%></td>
				<td><%=bean.getOnthewayTreatmentMeasure()%></td>
				<td><%=bean.getStockQuantity()%></td>
				<td><%=bean.getStockTreatmentMeasure()%></td>
			<%
			priEcnNumber = bean.getEcnNumber();
			}
			%>	
			</tr>

			<%
	    	}
			%>
		</table>

	<iframe id="exportFrame" name="exportFrame" height="0" width="0"></iframe>
</body>

<script type="text/javascript">
function doSearchAppo()
{
	//var url = "/Windchill/netmarkets/jsp/appo/statReport/statChangeInfo/statChangeInfo.jsp?tab=StatReport;
	//document.location.href = url;
	var url ="<%=requestURL %>?tab=StatReport";
	document.forms[0].appoaction.value = "query";
 	document.forms[0].action = url;
 	document.forms[0].submit();
}

function exportExcelAppo()
{
	
	var totalSize = <%=resultList.size()%>;
	if(totalSize > 0)
	{
		var url = "/Windchill/netmarkets/jsp/appo/statReport/statChangeInfo/searchResultExport.jsp";
		//屏蔽掉导出时显示白色的窗口
		window.frames['exportFrame'].location = url;
	}
}
</script>


<%@ include file="/netmarkets/jsp/util/end.jspf"%>