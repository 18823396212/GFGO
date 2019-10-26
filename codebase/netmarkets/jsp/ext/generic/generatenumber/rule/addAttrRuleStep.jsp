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
<%@page import="wt.session.SessionHelper,java.util.*,ext.generic.generatenumber.rule.util.*,wt.httpgw.URLFactory"%>
<%@page import="ext.generic.generatenumber.rule.constant.*" %>
<fmt:setLocale value="${localeBean.locale}"/>
<fmt:setBundle basename="ext.generic.generatenumber.rule.resource.NORuleMessageRB" />
<fmt:message var="ATTR_NAME"			key="ATTR_NAME" />
<fmt:message var="ATTR_INTERNAL_NAME"			key="ATTR_INTERNAL_NAME" />
<fmt:message var="ATTR_TYPE" 		key="ATTR_TYPE" />
<fmt:message var="ATTR_RULE_ENABLED" 	key="ATTR_RULE_ENABLED" />
<fmt:message var="ATTR_RULE_MAXLENGTH" 		key="ATTR_RULE_MAXLENGTH" />
<fmt:message var="CREATE_ATTRRULE_MSG" 		key="CREATE_ATTRRULE_MSG" />

<%
	//添加属性规则
	//String type = request.getParameter("type");
	
	List<String> internalKeys = new ArrayList<String>();
	List<String> displayValues = new ArrayList<String>();
	List<String> selectedValues = new ArrayList<String>();
	internalKeys.add("");displayValues.add("");selectedValues.add("");
	Map<String,String> buMap = NORuleUtil.initAttrRuleTypeMap(commandBean, internalKeys);
	for(String key : internalKeys){
		if(buMap.containsKey(key)){
			displayValues.add(buMap.get(key));
		}	
	}

	//获得域名
	URLFactory factory = new URLFactory();
	String host = factory.getBaseHREF();
%>
<c:set var="baseHref" scope="page" value="<%=host%>"/>
<c:set var="internalKeys" scope="page" value="<%=internalKeys%>"/>
<c:set var="displayValues" scope="page" value="<%=displayValues%>"/>
<c:set var="selectedValues" scope="page" value="<%=selectedValues%>"/>
<c:set var="RULE_NUMBER" scope="page" value="<%=NumberRuleConstant.TYPE_RULE_NUMBER %>"/>

<div style="width: auto; height: auto;">
		<table border="0" class="attributePanel-group-panel" id="tab">

		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;*${ATTR_INTERNAL_NAME}:</td>
			 <td>
				<w:textBox propertyLabel="" id="attrInternalName" name="attrInternalName" size="30" styleClass="required" required="true" maxlength="200" readonly="true" enabled="true"/>
				<img alt="Find.." onClick="openandSelectAttr();" src="netmarkets/images/search.gif">
			 </td>
		  </tr>

		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;*${ATTR_NAME}:</td>
			 <td>
			<w:textBox propertyLabel="" id="attrRuleName" name="attrRuleName"  size="30" styleClass="required" required="true" maxlength="200"/>
			 </td>
		  </tr>

		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;*${ATTR_TYPE}:</td>
			 <td>
				<w:comboBox propertyLabel="" id="attrRuleType" name="attrRuleType" onselect="lauch()" internalValues="${internalKeys}"  displayValues="${displayValues}" selectedValues="${selectedValues}" required="true"/>
			 </td>
		  </tr>

		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;${ATTR_RULE_ENABLED}:</td>
			 <td>
			 <w:checkBox id="isEnabled" name="isEnabled" checked="true"/>
			 </td>
		  </tr>

		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label" >&nbsp;&nbsp;*${ATTR_RULE_MAXLENGTH}:</td>
			 <td>
				<w:textBox propertyLabel="" id="attrRuleMaxLength" name="attrRuleMaxLength" size="30" styleClass="required" required="true" maxlength="200"/>
			 </td>
		  </tr>

		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;&nbsp;&nbsp;</td>
			  <td style="color:red">
			  ${CREATE_ATTRRULE_MSG}
			 </td>
		  </tr>
		  

		 </table>
</div>


<script language="Javascript">
	//打开并选择属性
	function openandSelectAttr(){
		var componentId = "attrInternalName";
		var mergeType = "OBJECTATTR";
		var clfOid = "";
		var host = '${baseHref}';
		//alert(""+host);
		var url = host+"netmarkets/jsp/ext/generic/generatenumber/rule/selectObjAttr.jsp?componentId="+componentId+"&mergeType="+mergeType+"&clfOid="+clfOid;
		window.open(url,'_blank','width=750,height=565,toolbar=no,menubar=no,location=no,status=no,resizable=yes');
	}

	//只有编号才能选择编号类型
	function checkAttrRuleType(){
		var internalName = document.getElementById("attrInternalName").value;
		var ruleType = document.getElementById("attrRuleType").value;
		if(ruleType=="${RULE_NUMBER}"){
			if(internalName != "number"){
				alert("[编码]类型的属性只能为编号[number]");
				return false;
			}
		}

		if(internalName == "number"){
			if(ruleType !="${RULE_NUMBER}"){
				alert("编号[number]的类型只能为[编码]");
				return false;
			}
		}
		return true;
	}
</script>
<%@ include file="/netmarkets/jsp/util/end.jspf"%>