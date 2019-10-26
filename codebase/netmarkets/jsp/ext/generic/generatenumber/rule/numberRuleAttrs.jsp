<%@ page pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=UTF-8" language="java" errorPage=""%>
<%request.setCharacterEncoding("UTF-8");%>
<%@ taglib prefix="wctags" tagdir="/WEB-INF/tags" %>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>
<%@taglib prefix="w" uri="http://www.ptc.com/windchill/taglib/wrappers"%>
<%@ taglib prefix="fmt" uri="http://www.ptc.com/windchill/taglib/fmt" %>
<%@page import="java.util.*,ext.com.core.CoreUtil,wt.fc.WTObject,ext.pi.core.*,ext.generic.generatenumber.rule.util.*"%>
<%@page import="ext.generic.generatenumber.rule.model.*,com.ptc.core.lwc.server.LWCStructEnumAttTemplate"%>

<fmt:setLocale value="${localeBean.locale}"/>
<fmt:setBundle basename="ext.generic.generatenumber.rule.resource.NORuleMessageRB" />
<fmt:message var="EDIT_ERR_NOT_SELECTED"			key="EDIT_ERR_NOT_SELECTED" />
<fmt:message var="EDIT_ERR_SELECT_OVER_ONE"			key="EDIT_ERR_SELECT_OVER_ONE" />
<fmt:message var="CHANGEORDER_ERR_SELECT_NOT_TWO"			key="CHANGEORDER_ERR_SELECT_NOT_TWO" />
<fmt:message var="CLF_NAME"			key="CLF_NAME" />
<fmt:message var="ATTR_RULE"			key="ATTR_RULE" />

<%
  String clfOid = request.getParameter("clfOid");
  String ruleOid = request.getParameter("ruleOid");
  //System.out.println("clfOid="+clfOid+",,,ruleOid="+ruleOid);
  LWCStructEnumAttTemplate clfObj = (LWCStructEnumAttTemplate)CoreUtil.getWTObjectByOid(clfOid);
  String clfName = clfObj.getName();
  clfName = PIClassificationHelper.service.getNodeLocalizedHierarchy(clfName, commandBean.getLocale());
  NumberAttrRule ruleObj = (NumberAttrRule)CoreUtil.getWTObjectByOid(ruleOid);
  String ruleName = ruleObj.getAttrDisplayName()+"("+ruleObj.getAttrInternalName()+")";
  String ruleType = ruleObj.getAttrRuleType();
  int nextOrder = NORuleUtil.getNextOrderByRule(ruleObj);
%>
<c:set var="clfOid" scope="page" value="<%=clfOid %>"/>
<c:set var="ruleOid" scope="page" value="<%=ruleOid %>"/>
<c:set var="clfName" scope="page" value="<%=clfName %>"/>
<c:set var="ruleName" scope="page" value="<%=ruleName %>"/>
<c:set var="ruleType" scope="page" value="<%=ruleType %>"/>
<c:set var="nextOrder" scope="page" value="<%=nextOrder %>"/>
<div>
<br/>
		<table border="0" class="attributePanel-group-panel" id="tab">
		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;${CLF_NAME}:</td>
			<td>
			${clfName}
			</td>
		  </tr>
		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;${ATTR_RULE}:</td>
			<td>
			${ruleName}
			</td>
		  </tr>
		</table>
		<br/>
		<jsp:include page="${mvc:getComponentURL('ext.generic.generatenumber.rule.builder.MergeRuleAttrBuilder')}" />
		<br/>
		<input type="hidden" value="${ruleOid}" id="ruleOid" name="ruleOid"/>
		<input type="hidden" value="${clfOid}" id="clfOid" name="clfOid"/>
		<input type="hidden" value="${ruleType}" id="ruleType" name="ruleType"/>
		<input type="hidden" value="${nextOrder}" id="nextOrder" name="nextOrder"/>
</div>

<script language="Javascript">

	function checkSelectedToModify(){
		var tableId = "ext.generic.generatenumber.rule.builder.MergeRuleAttrBuilder";
		var num = PTC.jca.table.Utils.getTableSelectedRowsCountById(tableId);
		//alert("选择num="+num);
		if(num == 0){
			alert("${EDIT_ERR_NOT_SELECTED}");
			return false;
		}else if(num>1){
			alert("${EDIT_ERR_SELECT_OVER_ONE}");
			return false;
		}
		
		return true;
	}

	function checkSelectedToChangeOrder(){
		var tableId = "ext.generic.generatenumber.rule.builder.MergeRuleAttrBuilder";
		var num = PTC.jca.table.Utils.getTableSelectedRowsCountById(tableId);
		//alert("选择num="+num);
		if(num == 0){
			alert("${EDIT_ERR_NOT_SELECTED}");
			return false;
		}else if(num != 2){
			alert("${CHANGEORDER_ERR_SELECT_NOT_TWO}");
			return false;
		}
		
		return true;
	}

</script>
<%@ include file="/netmarkets/jsp/util/end.jspf"%>