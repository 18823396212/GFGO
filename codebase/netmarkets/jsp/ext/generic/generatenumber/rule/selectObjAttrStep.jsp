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
	
<script language="javascript">

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
		alert("icon="+icon);*/
		if("netmarkets/images/dtype_RichText.png" != icon && "netmarkets/images/dtype_String.png" != icon){
				alert("所选属性["+displayName+"]不是字符串类型");
				return;
		}
		if(isEnum=='true'){
			alert("所选属性["+displayName+"]有枚举约束，不适合属性合并");
			return;
		}
	}
	//alert("dataType="+dataType);
	window.opener.document.getElementById('<%= componentId %>').value = internalName;
	window.opener.document.getElementById('attrRuleName').value = displayName;
    window.close();
 
 }
setUserSubmitFunction(addSearchResult);

</script>

<%@include file="/netmarkets/jsp/util/end.jspf" %>