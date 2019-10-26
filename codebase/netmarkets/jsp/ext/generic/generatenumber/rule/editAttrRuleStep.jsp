<%@page import="wt.httpgw.URLFactory"%>
<%@page pageEncoding="UTF-8"%>
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
<%@page import="wt.session.SessionHelper,java.util.*,ext.generic.generatenumber.rule.util.*"%>
<%@page import="ext.generic.generatenumber.rule.model.*"%>

<fmt:setLocale value="${localeBean.locale}"/>
<fmt:setBundle basename="ext.generic.generatenumber.rule.resource.NORuleMessageRB" />
<fmt:message var="ATTR_NAME"			key="ATTR_NAME" />
<fmt:message var="ATTR_INTERNAL_NAME"			key="ATTR_INTERNAL_NAME" />
<fmt:message var="ATTR_TYPE" 		key="ATTR_TYPE" />
<fmt:message var="ATTR_RULE_ENABLED" 	key="ATTR_RULE_ENABLED" />
<fmt:message var="ATTR_RULE_MAXLENGTH" 		key="ATTR_RULE_MAXLENGTH" />
<fmt:message var="EDIT_ATTRRULE_MSG" 		key="EDIT_ATTRRULE_MSG" />

<%
	//编辑属性规则
	//String type = request.getParameter("type");
	Map<String,Object> infoMap = NORuleUtil.getSelectedAttrRuleInfo(commandBean);
	NumberAttrRule rule = NORuleUtil.getSelectedAttrRule(commandBean);

%>

<c:set var="attrRuleName" scope="page" value="<%=infoMap.get(NumberAttrRule.ATTR_DISPLAY_NAME)%>"/>
<c:set var="attrInternalName" scope="page" value="<%=infoMap.get(NumberAttrRule.ATTR_INTERNAL_NAME)%>"/>
<c:set var="attrRuleType" scope="page" value="<%=infoMap.get(NumberAttrRule.ATTR_RULE_TYPE)%>"/>
<c:set var="isEnabled" scope="page" value="<%=infoMap.get(NumberAttrRule.ENABLED)%>"/>
<c:set var="attrRuleMaxLength" scope="page" value="<%=infoMap.get(NumberAttrRule.MAX_RULE_LENGTH)%>"/>
<c:set var="attrRuleObj" scope="page" value="<%=rule %>"/>

<div style="width: auto; height: auto;">
		<table border="0" class="attributePanel-group-panel" id="tab">

		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;*${ATTR_INTERNAL_NAME}:</td>
			 <td>
				${attrInternalName}
			 </td>
		  </tr>

		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;*${ATTR_NAME}:</td>
			 <td>
			<w:textBox propertyLabel="" id="attrRuleName" name="attrRuleName" value="${attrRuleName}" size="30" styleClass="required" required="true" maxlength="200"/>
			 </td>
		  </tr>

		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;*${ATTR_TYPE}:</td>
			 <td>
				${attrRuleType}
			 </td>
		  </tr>

		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;${ATTR_RULE_ENABLED}:</td>
			 <td>
			 <w:checkBox id="isEnabled" name="isEnabled" checked="${isEnabled}"/>
			 </td>
		  </tr>

		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label" >&nbsp;&nbsp;*${ATTR_RULE_MAXLENGTH}:</td>
			 <td>
				<w:textBox propertyLabel="" id="attrRuleMaxLength" name="attrRuleMaxLength" value="${attrRuleMaxLength}" size="30" styleClass="required" required="true" maxlength="200"/>
			 </td>
		  </tr>

		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;&nbsp;&nbsp;</td>
			  <td style="color:red">
			  ${EDIT_ATTRRULE_MSG}
			 </td>
		  </tr>
		  

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