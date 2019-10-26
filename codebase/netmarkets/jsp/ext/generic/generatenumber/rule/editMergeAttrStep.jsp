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

<%
	//编辑合成属性
	//属性类型
	Map<String,Object> infoMap = NORuleUtil.getSelectedMergeAttrInfo(commandBean);
	String mergeType = (String)infoMap.get(MergeAttribute.MERGE_TYPE);
	String mergeValue = (String)infoMap.get(MergeAttribute.MERGE_VALUE);
	String actualMergeValue = (String)infoMap.get(MergeAttribute.ACTUAL_MERGE_VALUE);

	String attrObjType = "";
	//日期格式
	List<String> timeFormats = new ArrayList<String>();
	List<String> selectedFormats = new ArrayList<String>();
	timeFormats.add("");
	if(NumberRuleConstant.TYPE_MERGE_DATE.equals(mergeType)){
		selectedFormats.add(mergeValue);
	}
	timeFormats.addAll(AttrBusinessRuleXml.getInstance().getDateFormats());

	//日期属性的格式
	List<String> actualSelectedFormats = new ArrayList<String>();
	if(actualMergeValue != null && actualMergeValue != ""){
		if(timeFormats.contains(actualMergeValue)){
			attrObjType="DateAttr";
			actualSelectedFormats.add(actualMergeValue);
		}else{
			actualSelectedFormats.add("");
		}
	}else{
		actualSelectedFormats.add("");
	}

	//多属性值得显示
	List<String> valueTypes = new ArrayList<String>();
	List<String> displayValueTypes = new ArrayList<String>();
	List<String> selectedTypes = new ArrayList<String>();
	valueTypes.add("");displayValueTypes.add("");
	Map<String,String> valueMap = NORuleUtil.initMultiRBValueTypeMap(commandBean, valueTypes);

	if(actualMergeValue != null && actualMergeValue != ""){
		if(valueMap.containsKey(actualMergeValue)){
			attrObjType="enumAttr";
			selectedTypes.add(actualMergeValue);
		}else{
			selectedTypes.add(NumberRuleConstant.MULTI_RB_INTERNAL_VALUE);
		}
	}else{
		selectedTypes.add(NumberRuleConstant.MULTI_RB_INTERNAL_VALUE);
	}
	
	for(String key : valueTypes){
		if(valueMap.containsKey(key)){
			displayValueTypes.add(valueMap.get(key));
		}	
	}

	//获得域名
	URLFactory factory = new URLFactory();
	String host = factory.getBaseHREF();
	String mergeTypeDisplay = (String)infoMap.get("mergeTypeDisplay") ;
%>
<c:set var="baseHref" scope="page" value="<%=host%>"/>

<c:set var="timeFormats" scope="page" value="<%=timeFormats%>"/>
<c:set var="selectedFormats" scope="page" value="<%=selectedFormats%>"/>
<c:set var="actualSelectedFormats" scope="page" value="<%=actualSelectedFormats%>"/>

<c:set var="valueTypes" scope="page" value="<%=valueTypes%>"/>
<c:set var="displayValueTypes" scope="page" value="<%=displayValueTypes%>"/>
<c:set var="selectedTypes" scope="page" value="<%=selectedTypes%>"/>

<c:set var="CONSTANT" scope="page" value="<%=NumberRuleConstant.TYPE_MERGE_CONSTANT %>"/>
<c:set var="DATE" scope="page" value="<%=NumberRuleConstant.TYPE_MERGE_DATE %>"/>
<c:set var="OBJECTATTR" scope="page" value="<%=NumberRuleConstant.TYPE_MERGE_OBJECTATTR %>"/>
<c:set var="CLASSIFYATTR" scope="page" value="<%=NumberRuleConstant.TYPE_MERGE_CLASSIFYATTR %>"/>
<c:set var="FLOWCODE" scope="page" value="<%=NumberRuleConstant.TYPE_MERGE_FLOWCODE %>"/>


<c:set var="attrObjType" scope="page" value="<%=attrObjType%>"/>
<c:set var="mergeType" scope="page" value="<%=mergeType%>"/>
<c:set var="mergeValue" scope="page" value="<%=mergeValue%>"/>
<c:set var="mergeTypeDisplay" scope="page" value="<%=mergeTypeDisplay %>"/>
<c:set var="mergeName" scope="page" value="<%=infoMap.get(MergeAttribute.MERGE_NAME) %>"/>
<c:set var="mergeLength" scope="page" value="<%=infoMap.get(MergeAttribute.MERGE_LENGTH) %>"/>
<c:set var="mergeOrder" scope="page" value="<%=infoMap.get(MergeAttribute.MERGE_ORDER) %>"/>
<c:set var="mergePrefix" scope="page" value="<%=infoMap.get(MergeAttribute.MERGE_PREFIX) %>"/>
<c:set var="mergeSuffix" scope="page" value="<%=infoMap.get(MergeAttribute.MERGE_SUFFIX) %>"/>
<c:set var="controlMergeAttrs" scope="page" value="<%=infoMap.get(MergeAttribute.CONTROLLED_MERGE_ATTRS) %>"/>

<div style="width: auto; height: auto;">
		<table border="0" class="attributePanel-group-panel" id="tab">
		  
		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;*${MERGE_NAME}:</td>
			 <td>
			<w:textBox propertyLabel="" id="mergeName" name="mergeName" value="${mergeName}" size="30" styleClass="required" required="true" maxlength="200"/>
			 </td>
		  </tr>

		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;*${MERGE_TYPE}:</td>
			 <td>
				${mergeTypeDisplay}
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
			<w:textBox propertyLabel="" id="mergeValue" name="mergeValue" value="${mergeValue}" size="30" styleClass="required" required="true" maxlength="200"/>
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
			<w:comboBox propertyLabel="" id="mergeDateValue" name="mergeDateValue" onselect="lauch()" internalValues="${timeFormats}"  displayValues="${timeFormats}" selectedValues="${selectedFormats}" required="true"/>
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
			<w:textBox propertyLabel="" id="selectAttrName" name="selectAttrName" value="${mergeValue}" size="30" styleClass="required" required="true" maxlength="200" readonly="true" enabled="true"/>
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
			<w:comboBox propertyLabel="" id="actualMultiMergeValue" name="actualMultiMergeValue" onselect="lauch()" internalValues="${valueTypes}"  displayValues="${displayValueTypes}" selectedValues="${selectedTypes}" required="true"/>
			 </td>
		  </tr>
		  <!-- 对象属性，分类属性--日期 -->
		  <tr id="attrDateTr" style="display:none">
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;*${ACTUAL_MERGE_VALUE}:</td>
			 <td>
			<w:comboBox propertyLabel="" id="actualDateMergeValue" name="actualDateMergeValue" onselect="lauch()" internalValues="${timeFormats}"  displayValues="${timeFormats}" selectedValues="${actualSelectedFormats}" required="true"/>
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
		  <tr id="controlMergeAttrsTr" style="display:none">
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;${MERGE_CONTROLLEDMERGEATTRS}:</td>
			 <td>
			<w:textBox propertyLabel="" id="controlMergeAttrsOid" name="controlMergeAttrsOid" value="${controlMergeAttrs}" size="30"  maxlength="2000"/>
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
				<w:textBox propertyLabel="" id="mergeLength" name="mergeLength" value="${mergeLength}" size="30" styleClass="" maxlength="200"/>
			 </td>
		  </tr>

		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label" >&nbsp;&nbsp;*${MERGE_ORDER}:</td>
			 <td>
				<w:textBox propertyLabel="" id="mergeOrder" name="mergeOrder" value="${mergeOrder}" size="30" styleClass="required" required="true" maxlength="200"/>
			 </td>
		  </tr>

		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label" >&nbsp;&nbsp;${MERGE_PREFIX}:</td>
			 <td>
				<w:textBox propertyLabel="" id="mergePrefix" name="mergePrefix" value="${mergePrefix}" size="30" styleClass=""  maxlength="200"/>
			 </td>
		  </tr>

		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label" >&nbsp;&nbsp;${MERGE_SUFFIX}:</td>
			 <td>
				<w:textBox propertyLabel="" id="mergeSuffix" name="mergeSuffix" value="${mergeSuffix}" size="30" styleClass=""  maxlength="200"/>
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

	PTC.onReady(function() {
		
		if (navigator.userAgent.indexOf("MSIE") != -1) {
	         document.getElementById("navType").value="IE";
	    } else if (navigator.userAgent.indexOf("Firefox") != -1 || navigator.userAgent.indexOf("Mozilla") != -1) {
	         document.getElementById("navType").value="Firefox";
	    }
		
	});
	showTypeDiv();
	//根据选择的属性类别，来显示相应需要维护的属性
	function showTypeDiv(){
		var type = "${mergeType}";
		var ruleType = document.getElementById("ruleType").value;
		//alert(ruleType);
		if(type != null && type != ""){
			//只有编码类型可以选择常量、日期和流水码
			if(ruleType != "RULE_NUMBER"){
				if(type=="${CONSTANT}" || type=="${DATE}" || type=="${FLOWCODE}"){
					//alert("${ATTR_TYPE_SELECT_WRONG}");
					document.getElementById("constantDiv").style.display="none";
					document.getElementById("dateDiv").style.display="none";
					document.getElementById("attrDiv").style.display="none";
					document.getElementById("flowDiv").style.display="none";
					return;
				}
			}

			if(type=="${FLOWCODE}"){
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
				}
			}

			if(type=="${CONSTANT}"){
				document.getElementById("constantDiv").style.display="block";
				document.getElementById("dateDiv").style.display="none";
				document.getElementById("attrDiv").style.display="none";
				document.getElementById("flowDiv").style.display="none";
			}else if(type=="${DATE}"){
				document.getElementById("constantDiv").style.display="none";
				document.getElementById("dateDiv").style.display="block";
				document.getElementById("attrDiv").style.display="none";
				document.getElementById("flowDiv").style.display="none";
			}else if(type=="${OBJECTATTR}" || type=="${CLASSIFYATTR}"){
				document.getElementById("constantDiv").style.display="none";
				document.getElementById("dateDiv").style.display="none";
				document.getElementById("attrDiv").style.display="block";
				document.getElementById("flowDiv").style.display="none";
				var dataType = "${attrObjType}";
				if(dataType == "enumAttr"){
					document.getElementById("attrMultiRBTr").style.display="block";
					document.getElementById("attrDateTr").style.display="none";
				}else if(dataType == "DateAttr"){
					document.getElementById("attrMultiRBTr").style.display="none";
					document.getElementById("attrDateTr").style.display="block";
				}else{
					document.getElementById("attrMultiRBTr").style.display="none";
					document.getElementById("attrDateTr").style.display="none";
				}
			}else if(type=="${FLOWCODE}"){
				document.getElementById("constantDiv").style.display="none";
				document.getElementById("dateDiv").style.display="none";
				document.getElementById("attrDiv").style.display="none";
				document.getElementById("flowDiv").style.display="block";
			}
		}else{
			document.getElementById("constantDiv").style.display="none";
			document.getElementById("dateDiv").style.display="none";
			document.getElementById("attrDiv").style.display="none";
			document.getElementById("flowDiv").style.display="none";
		}
	}

	//提交前校验合成属性类型是否正确
	function checkMergeAttrType(){
		var type = "${mergeType}";;
		var ruleType = document.getElementById("ruleType").value;
		//alert("check");
		//只有编码类型可以选择常量、日期和流水码
			if(ruleType != "RULE_NUMBER"){
				if(type=="${CONSTANT}" || type=="${DATE}" || type=="${FLOWCODE}"){
					alert("${ATTR_TYPE_SELECT_WRONG}");
					return false;
				}
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