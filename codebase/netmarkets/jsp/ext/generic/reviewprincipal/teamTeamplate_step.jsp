<%@page import="wt.httpgw.URLFactory"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="wt.session.SessionHelper"%>
<%@page import="java.util.*,ext.generic.reviewprincipal.util.*"%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf"%>
<%@taglib uri="http://www.ptc.com/windchill/taglib/jcaMvc" prefix="jcaMvc"%>
<%@ taglib prefix="wctags" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>
<%@ taglib prefix="jca" uri="http://www.ptc.com/windchill/taglib/components"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/wrappers" prefix="w"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%
	String type = request.getParameter("type");
	String oid = request.getParameter("oid");
	Map<String,String> buMap = WFTeamTemplateUtil.getPboTemplates(oid);
	List<String> buKeys = new ArrayList<String>();
	List<String> buNames = new ArrayList<String>();
	List<String> selectedValues = WFTeamTemplateUtil.getSelectPboTemplates(oid);
	buKeys.add("");buNames.add("");
	for(String key : buMap.keySet()){
		buKeys.add(key);
		buNames.add(buMap.get(key));
	}
%>

<c:set var="type" scope="page" value="<%=type%>"/>
<c:set var="buKeys" scope="page" value="<%=buKeys%>"/>
<c:set var="buNames" scope="page" value="<%=buNames%>"/>
<c:set var="selectedValues" scope="page" value="<%=selectedValues%>"/>
	
<fmt:setBundle basename="ext.generic.reviewprincipal.resource.WFTeamTemplateRB"/>
<fmt:message var="deleteTemplate" key="WFTEAM_DELETETEMPLATE_TITLE" />
<fmt:message var="applyTemplate" key="WFTEAM_APPLYTEMPLATE_TITLE" />

<div style="width: auto; height: auto;">
		<table border="0" class="attributePanel-group-panel" id="tab">
		<%
			if(type != null && type.equalsIgnoreCase("delete") ){
			%>
				  <tr>
					<td>&nbsp;</td>
					<td class="attributePanel-label">&nbsp;&nbsp;*${deleteTemplate}：</td>
					 <td>
						<w:comboBox propertyLabel="" id="selectTemplateName" name="selectTemplateName" onselect="lauch()" required="true" internalValues="${buKeys}"  displayValues="${buNames}" selectedValues="${selectedValues}" />
					 </td>
				  </tr>
			<%
			}else if(type != null && type.equalsIgnoreCase("apply") ){
				%>
				  <tr>
					<td>&nbsp;</td>
					<td class="attributePanel-label">&nbsp;&nbsp;*${applyTemplate}：</td>
					 <td>
						<w:comboBox propertyLabel="" id="selectTemplateName" name="selectTemplateName" onselect="lauch()" required="true" internalValues="${buKeys}"  displayValues="${buNames}" selectedValues="${selectedValues}" />
					 </td>
				  </tr>
				<%
			}
		%>
		 
		  

		 </table>
</div>


<script language="Javascript">
	Ext.onReady(function() {
		
		if (navigator.userAgent.indexOf("MSIE") != -1) {
	         document.getElementById("navType").value="IE";
	    } else if (navigator.userAgent.indexOf("Firefox") != -1 || navigator.userAgent.indexOf("Mozilla") != -1) {
	         document.getElementById("navType").value="Firefox";
	    }
	});
</script>
<%@ include file="/netmarkets/jsp/util/end.jspf"%>