<%request.setCharacterEncoding("UTF-8");%>
<%@ page pageEncoding="UTF-8"%>
<%@ taglib prefix="wctags" tagdir="/WEB-INF/tags" %>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>

<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>
<%@taglib prefix="w" uri="http://www.ptc.com/windchill/taglib/wrappers"%>
<%@ taglib prefix="fmt" uri="http://www.ptc.com/windchill/taglib/fmt" %>

<fmt:setLocale value="${localeBean.locale}"/>
<fmt:setBundle basename="ext.generic.generatenumber.rule.resource.NORuleMessageRB" />
<fmt:message var="FLOWCODE"			key="TYPE_MERGE_FLOWCODE" />
<fmt:message var="MERGETYPE_ERR_FLOWCODE"			key="MERGETYPE_ERR_FLOWCODE" />

<%
		String componentId = request.getParameter("componentId");
		//System.out.println("控件id="+componentId);//& (PUBLICITY_NUMBER != '')
%>

<div>
 <jsp:include page="${mvc:getComponentURL('ext.generic.generatenumber.rule.builder.SelectMergeRuleAttrBuilder')}" />
</div>

<input type="hidden" value="" id="ruleOid" name="ruleOid" size="100"/><br/>
<input type="hidden" value="" id="clfOid" name="clfOid" size="100"/><br/> 
	
<script language="javascript">

PTC.onReady(function() {
	//
	document.getElementById("clfOid").value = window.opener.document.getElementById("clfOid").value;
	document.getElementById("ruleOid").value = window.opener.document.getElementById("ruleOid").value;
});

var tableId = "ext.generic.generatenumber.rule.builder.SelectMergeRuleAttrBuilder";

function addSearchResult(){
	var table = PTC.jca.table.Utils.getTable("ext.generic.generatenumber.rule.builder.SelectMergeRuleAttrBuilder");
	var arrayObj = new Array();
	var arrayOid = new Array();
	var selections = table.getSelectionModel().getSelections();
	 if (selections ==null || selections.length == 0 ){
	       JCAAlert('com.ptc.core.agreements.agreementsResource.NOTHING_SELECTED');
	       return false;
	    }
	var dataType = "";//enum or timestamp	
	for(var i=0;i<selections.length;i++){
		var dataType = selections[i].get('mergeType')['gui']['comparable'];
		if(dataType == "${FLOWCODE}"){
			alert("${MERGETYPE_ERR_FLOWCODE}");
			return;
		}

		arrayObj.push(selections[i].get('mergeValue'));
		arrayOid.push(selections[i].get('oid'));
	}
	var displayName = "";
	var internalName = "";
	var displayName = "";
	var length = arrayObj.length;
    for (var i=0; i<length; i++) {
		var tempSelectedStr = arrayObj[i]; 
		if(tempSelectedStr != null){
	        displayName += tempSelectedStr + ";;;";
		}

		var tempOid = arrayOid[i];
		if(tempOid != null){
			internalName += tempOid + ";;;";
		}
    }  
    displayName = displayName.substring(0, displayName.length - 3);
	internalName = internalName.substring(0, internalName.length - 3);
	//alert("displayName="+displayName+",internalName="+internalName );
	window.opener.document.getElementById('<%= componentId %>').value = displayName;
	window.opener.document.getElementById('<%= componentId %>'+'Oid').value = internalName;
    window.close();
 
 }
setUserSubmitFunction(addSearchResult);

</script>

<%@include file="/netmarkets/jsp/util/end.jspf" %>