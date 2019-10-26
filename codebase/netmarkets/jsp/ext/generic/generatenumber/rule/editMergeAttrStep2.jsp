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
<%@page import="ext.generic.generatenumber.rule.model.*"%>

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
<fmt:message var="MERGE_FLOW_INITVALUE" 		key="MERGE_FLOW_INITVALUE" />
<%
	//编辑合成属性
	//属性类型
	Map<Integer, String> valuesMap = new HashMap<Integer, String>();
	Map<Integer, ArrayList<String>> listMap = new HashMap<Integer, ArrayList<String>>();
	NORuleUtil.initEditMergeAttrValue(commandBean, valuesMap, listMap);
	//System.out.println("valuesMap="+valuesMap);
	//获得域名
	URLFactory factory = new URLFactory();
	String host = factory.getBaseHREF();
%>

<c:set var="mergeTypeDisplay" scope="page" value="<%=valuesMap.get(1) %>"/>
<c:set var="mergeName" scope="page" value="<%=valuesMap.get(2) %>"/>
<c:set var="attrObjType" scope="page" value="<%=valuesMap.get(3)%>"/>
<c:set var="mergeType" scope="page" value="<%=valuesMap.get(4)%>"/>
<c:set var="mergeValue" scope="page" value="<%=valuesMap.get(5)%>"/>
<c:set var="controlMergeAttrs" scope="page" value="<%=valuesMap.get(6) %>"/>

<div style="width: auto; height: auto; " id="">
		<table border="0" class="attributePanel-group-panel" id="">
		<tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;*${MERGE_NAME}:</td>
			 <td>
			<w:textBox propertyLabel="" id="mergeName" name="mergeName" value="${mergeName}" size="30" styleClass="required" required="true" maxlength="200"/>
			 </td>
		  </tr>
		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;${MERGE_TYPE}:</td>
			 <td>
				<span id="mergeTypeSpan">${mergeTypeDisplay}</span>
			 </td>
		  </tr>
		</table>
</div>
<div style="width: auto; height: auto; display:none;" id="constantDiv">
		<table border="0" class="attributePanel-group-panel" id="constantTab">
		  <!-- 常量 -->
		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;*${MERGE_VALUE}:</td>
			 <td>
			<w:textBox propertyLabel="" id="mergeValue" name="mergeValue" value="${mergeValue}" size="30" styleClass="" maxlength="200"/>
			 </td>
		  </tr>
		</table>
</div>

<div style="width: auto; height: auto; display:none;" id="dateDiv">
		<table border="0" class="attributePanel-group-panel" id="dateTab">
		  <!-- 日期 -->
		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;*${MERGE_DATE_FORMAT}:</td>
			 <td>
			<w:comboBox propertyLabel="" id="mergeDateValue" name="mergeDateValue" onselect="lauch()" internalValues="<%=listMap.get(1)%>"  displayValues="<%=listMap.get(1)%>" selectedValues="<%=listMap.get(2)%>" />
			 </td>
		  </tr>
		</table>
</div>

<div style="width: auto; height: auto; display:none;" id="attrDiv">
		<table border="0" class="attributePanel-group-panel" id="attrTab">
		  <!-- 对象属性，分类属性 -->
		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;*${SELECT_MERGE_ATTR}:</td>
			 <td>
			<w:textBox propertyLabel="" id="selectAttrName" name="selectAttrName" value="${mergeValue}" size="30" styleClass=""  maxlength="200" readonly="true" enabled="true"/>
			<img alt="Find.." onClick="openandSelectAttr();" src="netmarkets/images/search.gif"> 
			 </td>
		  </tr>
		  <tr style="display:none">
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;${SELECT_MERGE_ATTR}:</td>
			 <td>
			<w:textBox propertyLabel="" id="selectAttrNameDisplay" name="selectAttrNameDisplay"/>
			<w:textBox propertyLabel="" id="selectAttrNameType" name="selectAttrNameType" value="${attrObjType}" />
			 </td>
		  </tr>
		  <!-- 对象属性，分类属性--多语言显示属性 -->
		  <tr id="attrMultiRBTr" style="display:none">
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;*${ACTUAL_MERGE_VALUE}:</td>
			 <td>
			<w:comboBox propertyLabel="" id="actualMultiMergeValue" name="actualMultiMergeValue" onselect="lauch()" internalValues="<%=listMap.get(4)%>"  displayValues="<%=listMap.get(5)%>" selectedValues="<%=listMap.get(6)%>" />
			 </td>
		  </tr>
		  <!-- 对象属性，分类属性--日期 -->
		  <tr id="attrDateTr" style="display:none">
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;*${ACTUAL_MERGE_VALUE}:</td>
			 <td>
			<w:comboBox propertyLabel="" id="actualDateMergeValue" name="actualDateMergeValue" onselect="lauch()" internalValues="<%=listMap.get(1)%>"  displayValues="<%=listMap.get(1)%>" selectedValues="<%=listMap.get(3)%>" />
			 </td>
		  </tr>
		  <!-- 对象属性，分类属性--带单位的实数 -->
		  <tr id="attrUnitTr" style="display:none">
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;*${ACTUAL_MERGE_VALUE}:</td>
			 <td>
			<w:comboBox propertyLabel="" id="actualUnitMergeValue" name="actualUnitMergeValue" onselect="lauch()" internalValues="<%=listMap.get(7)%>"  displayValues="<%=listMap.get(8)%>" selectedValues="<%=listMap.get(9)%>" />
			 </td>
		  </tr>
		</table>
</div>

<div style="width: auto; height: auto; display:none;" id="flowDiv">
		<table border="0" class="attributePanel-group-panel" id="flowTab">
		  <!-- 流水码 -->
		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;${MERGE_CONTROLLEDMERGEATTRS}:</td>
			 <td>
			<w:textBox propertyLabel="" id="controlMergeAttrs" name="controlMergeAttrs"  size="30" value="${mergeValue}" maxlength="2000" readonly="true" enabled="true"/>
			<img alt="Find.." onClick="openandSelectMergeAttr();" src="netmarkets/images/search.gif">
			 </td>
		  </tr>
		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;${MERGE_FLOW_INITVALUE}:</td>
			 <td>
			<w:textBox propertyLabel="" id="flowInitValue" name="flowInitValue"  value="<%=valuesMap.get(7) %>" size="30" styleClass="" maxlength="200"/>
			 </td>
		  </tr>
		  <tr id="controlMergeAttrsTr" style="display:none">
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;${MERGE_CONTROLLEDMERGEATTRS}:</td>
			 <td>
			<w:textBox propertyLabel="" id="controlMergeAttrsOid" name="controlMergeAttrsOid" value="${controlMergeAttrs}" size="30"  maxlength="2000"/>
			 </td>
		  </tr>
		</table>
</div>



<script language="Javascript">
	//根据选择的属性类别，来显示相应需要维护的属性
	function showTypeDiv(){
		var type = "${mergeType}";
		var ruleType = document.getElementById("ruleType").value;
		//alert(type);
		if(type != null && type != ""){
			//只有编码类型可以选择常量、日期和流水码
			if(ruleType != "RULE_NUMBER"){
				if(type=="<%=NumberRuleConstant.TYPE_MERGE_CONSTANT %>" || type=="<%=NumberRuleConstant.TYPE_MERGE_DATE %>" || type=="<%=NumberRuleConstant.TYPE_MERGE_FLOWCODE %>"){
					alert("${ATTR_TYPE_SELECT_WRONG}");
					document.getElementById("constantDiv").style.display="none";
					document.getElementById("dateDiv").style.display="none";
					document.getElementById("attrDiv").style.display="none";
					document.getElementById("flowDiv").style.display="none";
					return false;
				}
			}

			if(type=="<%=NumberRuleConstant.TYPE_MERGE_FLOWCODE %>"){
				var len = document.getElementById("mergeLength").value;
				//alert("len="+len);
				if(!len || len==''){
					alert("[流水码]属性必须维护长度");
					return false;
				}else{
					if(isNaN(len)){
						alert("长度必须为数字");
						return false;
					}
					if(Number(len)<=0){
						alert("长度必须为正整数");
						return false;
					}
				}
			}
			
			if(type=="<%=NumberRuleConstant.TYPE_MERGE_CONSTANT %>"){
				document.getElementById("constantDiv").style.display="block";
				document.getElementById("dateDiv").style.display="none";
				document.getElementById("attrDiv").style.display="none";
				document.getElementById("flowDiv").style.display="none";
				//
				document.getElementById("mergeValue").className = "required";
				document.getElementById("mergeDateValue").className = "";
				document.getElementById("selectAttrName").className = "";
			}else if(type=="<%=NumberRuleConstant.TYPE_MERGE_DATE %>"){
				document.getElementById("constantDiv").style.display="none";
				document.getElementById("dateDiv").style.display="block";
				document.getElementById("attrDiv").style.display="none";
				document.getElementById("flowDiv").style.display="none";
				//
				document.getElementById("mergeValue").className = "";
				document.getElementById("mergeDateValue").className = "required";
				document.getElementById("selectAttrName").className = "";
			}else if(type=="<%=NumberRuleConstant.TYPE_MERGE_OBJECTATTR %>" || type=="<%=NumberRuleConstant.TYPE_MERGE_CLASSIFYATTR %>"){
				document.getElementById("constantDiv").style.display="none";
				document.getElementById("dateDiv").style.display="none";
				document.getElementById("attrDiv").style.display="block";
				document.getElementById("flowDiv").style.display="none";
				var dataType = "${attrObjType}";
				//alert("dataType="+dataType);
				if(dataType == "enumAttr"){
					document.getElementById("attrMultiRBTr").style.display="block";
					document.getElementById("attrDateTr").style.display="none";
					document.getElementById("attrUnitTr").style.display="none";
					document.getElementById("actualMultiMergeValue").className = "required";
					document.getElementById("actualDateMergeValue").className = "";
					document.getElementById("actualUnitMergeValue").className = "";
				}else if(dataType == "DateAttr"){
					document.getElementById("attrMultiRBTr").style.display="none";
					document.getElementById("attrDateTr").style.display="block";
					document.getElementById("attrUnitTr").style.display="none";
					document.getElementById("actualMultiMergeValue").className = "";
					document.getElementById("actualDateMergeValue").className = "required";
					document.getElementById("actualUnitMergeValue").className = "";
				}else if(dataType == "unitAttr"){
					document.getElementById("attrMultiRBTr").style.display="none";
					document.getElementById("attrDateTr").style.display="none";
					document.getElementById("attrUnitTr").style.display="block";
					document.getElementById("actualMultiMergeValue").className = "";
					document.getElementById("actualDateMergeValue").className = "";
					document.getElementById("actualUnitMergeValue").className = "required";
				}else{
					document.getElementById("attrMultiRBTr").style.display="none";
					document.getElementById("attrDateTr").style.display="none";
					document.getElementById("attrUnitTr").style.display="none";
					document.getElementById("actualMultiMergeValue").className = "";
					document.getElementById("actualDateMergeValue").className = "";
					document.getElementById("actualUnitMergeValue").className = "";
				}
				//
				document.getElementById("mergeValue").className = "";
				document.getElementById("mergeDateValue").className = "";
				document.getElementById("selectAttrName").className = "required";
			}else if(type=="<%=NumberRuleConstant.TYPE_MERGE_FLOWCODE %>"){
				document.getElementById("constantDiv").style.display="none";
				document.getElementById("dateDiv").style.display="none";
				document.getElementById("attrDiv").style.display="none";
				document.getElementById("flowDiv").style.display="block";
				//
				document.getElementById("mergeValue").className = "";
				document.getElementById("mergeDateValue").className = "";
				document.getElementById("selectAttrName").className = "";
			}
		}else{
			document.getElementById("constantDiv").style.display="none";
			document.getElementById("dateDiv").style.display="none";
			document.getElementById("attrDiv").style.display="none";
			document.getElementById("flowDiv").style.display="none";
			//
			document.getElementById("mergeValue").className = "";
			document.getElementById("mergeDateValue").className = "";
			document.getElementById("selectAttrName").className = "";
		}
		return true;
	}

	//打开并选择属性
	function openandSelectAttr(){
		var componentId = "selectAttrName";
		var mergeType = "${mergeType}";
		var clfOid = document.getElementById("clfOid").value;
		var host = '${baseHref}';
		//alert(""+host);
		var url = host+"netmarkets/jsp/ext/generic/generatenumber/rule/selectAttr.jsp?componentId="+componentId+"&mergeType="+mergeType+"&clfOid="+clfOid;
		window.open(url,'_blank','width=750,height=565,toolbar=no,menubar=no,location=no,status=no,resizable=yes');
	}

	//打开并选择受控属性
	function openandSelectMergeAttr(){
		var componentId = "controlMergeAttrs";
		var mergeType = "${mergeType}";
		var ruleOid = document.getElementById("ruleOid").value;
		var host = '${baseHref}';
		//alert(""+host);
		var url = host+"netmarkets/jsp/ext/generic/generatenumber/rule/selectMergeAttr.jsp?componentId="+componentId+"&mergeType="+mergeType+"&ruleOid="+ruleOid;
		window.open(url,'_blank','width=750,height=565,toolbar=no,menubar=no,location=no,status=no,resizable=yes');
	}
</script>
<%@ include file="/netmarkets/jsp/util/end.jspf"%>