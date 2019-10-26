<%@ page pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=UTF-8" language="java" errorPage=""%>
<%request.setCharacterEncoding("UTF-8");%>

<%@ taglib prefix="wctags" tagdir="/WEB-INF/tags" %>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>

<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>
<%@taglib prefix="w" uri="http://www.ptc.com/windchill/taglib/wrappers"%>
<%@ taglib prefix="fmt" uri="http://www.ptc.com/windchill/taglib/fmt" %>

<fmt:setLocale value="${localeBean.locale}"/>
<fmt:setBundle basename="ext.generic.generatenumber.rule.resource.NORuleMessageRB" />
<fmt:message var="EDIT_ERR_NOT_SELECTED"			key="EDIT_ERR_NOT_SELECTED" />
<fmt:message var="EDIT_ERR_SELECT_OVER_ONE"			key="EDIT_ERR_SELECT_OVER_ONE" />

<div>
		<jsp:include page="${mvc:getComponentURL('ext.generic.generatenumber.rule.builder.ClfNumberRuleBuilder')}" />
		<br/>
</div>

<script language="Javascript">
	Ext.onReady(function() {

		if (navigator.userAgent.indexOf("MSIE") != -1) {
	         document.getElementById("navType").value="IE";
	    } else if (navigator.userAgent.indexOf("Firefox") != -1 || navigator.userAgent.indexOf("Mozilla") != -1) {
	         document.getElementById("navType").value="Firefox";
	    }
	});

	function checkSelectedToModify(){
		var tableId = "ext.generic.generatenumber.rule.builder.ClfNumberRuleBuilder";
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

</script>
<%@ include file="/netmarkets/jsp/util/end.jspf"%>