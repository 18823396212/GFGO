<%request.setCharacterEncoding("UTF-8");%>
<%@ page pageEncoding="UTF-8"%>
<%@ taglib prefix="wctags" tagdir="/WEB-INF/tags" %>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>

<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>
<%@taglib prefix="w" uri="http://www.ptc.com/windchill/taglib/wrappers"%>
<%@ taglib prefix="fmt" uri="http://www.ptc.com/windchill/taglib/fmt" %>

<%
		String componentId = request.getParameter("componentId");
		//System.out.println("控件id="+componentId);//& (PUBLICITY_NUMBER != '')
%>

<div>
 <jsp:include page="${mvc:getComponentURL('ext.generic.generatenumber.rule.builder.SelectAttrsBuilder')}" />
</div>

<input type="hidden" value="" id="ruleOid" name="ruleOid" size="100"/><br/>
<input type="hidden" value="" id="clfOid" name="clfOid" size="100"/><br/> 
	
<script language="javascript">

PTC.onReady(function() {
	//
	document.getElementById("clfOid").value = window.opener.document.getElementById("clfOid").value;
	document.getElementById("ruleOid").value = window.opener.document.getElementById("ruleOid").value;
});

var tableId = "ext.generic.generatenumber.rule.builder.SelectAttrsBuilder";

function addSearchResult(){
	var table = PTC.jca.table.Utils.getTable("ext.generic.generatenumber.rule.builder.SelectAttrsBuilder");
	
	var selections = table.getSelectionModel().getSelections();
	 if (selections ==null || selections.length == 0 ){
	       JCAAlert('com.ptc.core.agreements.agreementsResource.NOTHING_SELECTED');
	       return false;
	    }
		var displayName = "";
		var internalName = "";
		var dataType = "";//enum or timestamp
	for(var i=0;i<selections.length;i++){
		var displayObj = selections[i].get('lwcAttributeDisplayName')['gui'];
		if(displayObj){
			displayName = displayObj['comparable'];
		}else{
			displayName = selections[i].get('lwcAttributeDisplayName')['comparable'];
		}
		//displayName = selections[i].get('lwcAttributeDisplayName')['gui']['comparable'];

		var internalObj = selections[i].get('lwcAttributeInternalName')['gui'];
		if(internalObj){
			internalName = internalObj['comparable'];
		}else{
			internalName = selections[i].get('lwcAttributeInternalName')['comparable'];
		}
		//internalName = selections[i].get('lwcAttributeInternalName')['gui']['comparable'];
		var icon = selections[i].get('lwcAttributeDataTypeIcon')['gui']['img'];
		var isEnum = selections[i].get('isEnumAttr')['gui']['comparable'];
		/*alert("displayName="+displayName);
		alert("internalName="+internalName);
		alert("icon="+icon);
		alert("isEnum="+isEnum);*/
		/*var obj = selections[i].get('lwcAttributeDataTypeIcon')['gui'];
		for(var key in obj){
			alert("key="+key+",,,value="+obj[key]);
		}*/

		if(isEnum=='true'){
			dataType = "enumAttr";
		}else{
			if("netmarkets/images/dtype_DataAndTime.png"==icon){
				dataType = "DateAttr";
			}else if("netmarkets/images/dtype_RealNumberWithUnits.png"==icon){
				dataType = "unitAttr";
			}
		}
	}
	//alert("dataType="+dataType);
	window.opener.document.getElementById('<%= componentId %>').value = internalName;
	window.opener.document.getElementById('<%= componentId %>'+'Display').value = displayName;
	window.opener.document.getElementById('mergeName').value = displayName;
	window.opener.document.getElementById('<%= componentId %>'+'Type').value = dataType;
	if(dataType != null && dataType != ""){
		if(dataType == "enumAttr"){
			window.opener.document.getElementById("attrMultiRBTr").style.display="block";
			window.opener.document.getElementById("attrDateTr").style.display="none";
			window.opener.document.getElementById("attrUnitTr").style.display="none";
			window.opener.document.getElementById("actualMultiMergeValue").className = "required";
			window.opener.document.getElementById("actualDateMergeValue").className = "";
			window.opener.document.getElementById("actualUnitMergeValue").className = "";
		}else if(dataType == "DateAttr"){
			window.opener.document.getElementById("attrMultiRBTr").style.display="none";
			window.opener.document.getElementById("attrDateTr").style.display="block";
			window.opener.document.getElementById("attrUnitTr").style.display="none";
			window.opener.document.getElementById("actualMultiMergeValue").className = "";
			window.opener.document.getElementById("actualDateMergeValue").className = "required";
			window.opener.document.getElementById("actualUnitMergeValue").className = "";
		}else if(dataType == "unitAttr"){
			window.opener.document.getElementById("attrMultiRBTr").style.display="none";
			window.opener.document.getElementById("attrDateTr").style.display="none";
			window.opener.document.getElementById("attrUnitTr").style.display="block";
			window.opener.document.getElementById("actualMultiMergeValue").className = "";
			window.opener.document.getElementById("actualDateMergeValue").className = "";
			window.opener.document.getElementById("actualUnitMergeValue").className = "required";
		}else{
			window.opener.document.getElementById("attrMultiRBTr").style.display="none";
			window.opener.document.getElementById("attrDateTr").style.display="none";
			window.opener.document.getElementById("attrUnitTr").style.display="none";
			window.opener.document.getElementById("actualMultiMergeValue").className = "";
			window.opener.document.getElementById("actualDateMergeValue").className = "";
			window.opener.document.getElementById("actualUnitMergeValue").className = "";
		}
		
	}else{
		window.opener.document.getElementById("attrMultiRBTr").style.display="none";
		window.opener.document.getElementById("attrDateTr").style.display="none";
		window.opener.document.getElementById("actualMultiMergeValue").className = "";
			window.opener.document.getElementById("actualDateMergeValue").className = "";
	}
    window.close();
 
 }
setUserSubmitFunction(addSearchResult);

</script>

<%@include file="/netmarkets/jsp/util/end.jspf" %>