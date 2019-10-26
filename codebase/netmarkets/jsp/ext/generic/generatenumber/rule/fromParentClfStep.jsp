<%request.setCharacterEncoding("UTF-8");%>
<%@ page pageEncoding="UTF-8"%>
<%@ taglib prefix="wctags" tagdir="/WEB-INF/tags" %>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>

<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>
<%@taglib prefix="w" uri="http://www.ptc.com/windchill/taglib/wrappers"%>
<%@ taglib prefix="fmt" uri="http://www.ptc.com/windchill/taglib/fmt" %>
<fmt:setLocale value="${localeBean.locale}"/>
<fmt:setBundle basename="ext.generic.generatenumber.rule.resource.NORuleMessageRB" />
<fmt:message var="EDIT_ERR_NOT_SELECTED"			key="EDIT_ERR_NOT_SELECTED" />

<div>
 <jsp:include page="${mvc:getComponentURL('ext.generic.generatenumber.rule.builder.ParentClfNumberRuleBuilder')}" />
</div>
	
<script language="javascript">

function checkAttrRuleSelected(){
	var tableId = "ext.generic.generatenumber.rule.builder.ParentClfNumberRuleBuilder";
	var num = PTC.jca.table.Utils.getTableSelectedRowsCountById(tableId);
		//alert("选择num="+num);
		if(num == 0){
			alert("${EDIT_ERR_NOT_SELECTED}");
			return false;
		}
		return true;
 
 }

</script>

<%@include file="/netmarkets/jsp/util/end.jspf" %>