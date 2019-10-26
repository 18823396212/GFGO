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
<%@page import="ext.generic.generatenumber.rule.constant.*,wt.httpgw.URLFactory"%>
<fmt:setLocale value="${localeBean.locale}"/>
<fmt:setBundle basename="ext.generic.generatenumber.rule.resource.NORuleMessageRB" />
<fmt:message var="ATTR_NAME"			key="ATTR_NAME" />
<fmt:message var="ATTR_INTERNAL_NAME"			key="ATTR_INTERNAL_NAME" />
<fmt:message var="ATTR_TYPE" 		key="ATTR_TYPE" />
<fmt:message var="ATTR_RULE_ENABLED" 	key="ATTR_RULE_ENABLED" />
<fmt:message var="ATTR_RULE_MAXLENGTH" 		key="ATTR_RULE_MAXLENGTH" />

<fmt:message var="MERGE_NAME"			key="MERGE_NAME" />
<fmt:message var="MERGE_TYPE"			key="MERGE_TYPE" />
<fmt:message var="MERGE_VALUE" 		key="MERGE_VALUE" />
<fmt:message var="ACTUAL_MERGE_VALUE" 	key="ACTUAL_MERGE_VALUE" />
<fmt:message var="MERGE_LENGTH" 		key="MERGE_LENGTH" />
<fmt:message var="MERGE_ORDER"			key="MERGE_ORDER" />
<fmt:message var="MERGE_PREFIX"			key="MERGE_PREFIX" />
<fmt:message var="MERGE_SUFFIX" 		key="MERGE_SUFFIX" />
<fmt:message var="MERGE_CONTROLLEDMERGEATTRS" 	key="MERGE_CONTROLLEDMERGEATTRS" />

<fmt:message var="MERGE_DATE_FORMAT" 	key="MERGE_DATE_FORMAT" />
<fmt:message var="SELECT_MERGE_ATTR" 	key="SELECT_MERGE_ATTR" />

<fmt:message var="MERGE_VALUE_MSG" 		key="MERGE_VALUE_MSG" />
<fmt:message var="ATTR_TYPE_SELECT_WRONG" 		key="ATTR_TYPE_SELECT_WRONG" />

<%
	//添加属性规则
	//属性类型
	List<String> internalKeys = new ArrayList<String>();
	List<String> displayValues = new ArrayList<String>();
	List<String> selectedValues = new ArrayList<String>();
	internalKeys.add("");displayValues.add("");selectedValues.add("");
	Map<String,String> buMap = NORuleUtil.initMergeAttrTypeMap(commandBean, internalKeys);
	for(String key : internalKeys){
		if(buMap.containsKey(key)){
			displayValues.add(buMap.get(key));
		}	
	}
%>

<c:set var="internalKeys" scope="page" value="<%=internalKeys%>"/>
<c:set var="displayValues" scope="page" value="<%=displayValues%>"/>
<c:set var="selectedValues" scope="page" value="<%=selectedValues%>"/>

<div style="width: auto; height: auto;">
		<table border="0" class="attributePanel-group-panel" id="tab">
		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;*${MERGE_TYPE}:</td>
			 <td>
				<w:comboBox propertyLabel="" id="mergeType" name="mergeType" onselect="lauch()" internalValues="${internalKeys}"  displayValues="${displayValues}" selectedValues="${selectedValues}" required="true" />
			 </td>
		  </tr>
		</table>
</div>


<div style="width: auto; height: auto;">
		<table border="0" class="attributePanel-group-panel" id="commonTab">
		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label" >&nbsp;&nbsp;${MERGE_LENGTH}:</td>
			 <td>
				<w:textBox propertyLabel="" id="mergeLength" name="mergeLength" size="30" styleClass="" maxlength="200"/>
			 </td>
		  </tr>

		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label" >&nbsp;&nbsp;*${MERGE_ORDER}:</td>
			 <td>
				<w:textBox propertyLabel="" id="mergeOrder" name="mergeOrder" size="30" styleClass="required" required="true" maxlength="200"/>
			 </td>
		  </tr>

		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label" >&nbsp;&nbsp;${MERGE_PREFIX}:</td>
			 <td>
				<w:textBox propertyLabel="" id="mergePrefix" name="mergePrefix" size="30" styleClass=""  maxlength="200"/>
			 </td>
		  </tr>

		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label" >&nbsp;&nbsp;${MERGE_SUFFIX}:</td>
			 <td>
				<w:textBox propertyLabel="" id="mergeSuffix" name="mergeSuffix" size="30" styleClass=""  maxlength="200"/>
			 </td>
		  </tr>

		 </table>
</div>

<input type="hidden" value="" id="ruleOid" name="ruleOid" size="100"/><br/>
<input type="hidden" value="" id="clfOid"  name="clfOid" size="100"/><br/>
<input type="hidden" value="" id="ruleType"  name="ruleType" size="100"/>

<script language="Javascript">
	document.getElementById("clfOid").value = window.opener.document.getElementById("clfOid").value;
	document.getElementById("ruleOid").value = window.opener.document.getElementById("ruleOid").value;
	document.getElementById("ruleType").value = window.opener.document.getElementById("ruleType").value;
	document.getElementById("mergeOrder").value = window.opener.document.getElementById("nextOrder").value;
</script>
<%@ include file="/netmarkets/jsp/util/end.jspf"%>