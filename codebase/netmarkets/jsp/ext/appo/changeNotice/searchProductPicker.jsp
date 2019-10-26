<%@page pageEncoding="UTF-8"%> 
<%@taglib tagdir="/WEB-INF/tags" prefix="wctags"%>
<%@include file="/netmarkets/jsp/components/beginWizard.jspf"%>

<wctags:itemPicker id="searchProduct" inline="true" showVersion="true" pickerTitle="查找产品" componentId="searchProductPicker" objectType="wt.part.WTPart" defaultVersionValue="LATEST"
	typeComponentId="PDMLink.relatedPartSearch" multiSelect="true" pickerCallback="addProduct"/>
	
<script type="text/javascript">
	function addProduct(objects, pickerID) {
		var itemOid = [] ;
		// 搜索内容
		var pickerObjects = objects.pickedObject;
		for (var i = 0; i < pickerObjects.length; i++) {
			itemOid[i] = pickerObjects[i].oid ;
		}
		if(itemOid.length > 0){
			// 检查选择产品是否符合要求
			var params = "itemOid=" + JSON.stringify(itemOid) + "&method=checkAffectedEndItems";
			var url = "netmarkets/jsp/ext/appo/changeNotice/affectedEndItems.jsp";
			var json = eval("("+window.opener.ajaxRequest(url, params)+")");
			//错误信息
			var errorInfo = json['message'];
			if(errorInfo.length != 0){
				alert(errorInfo);
				return;
			}
			window.opener.reloadAffectedEndItemsTable(JSON.stringify(itemOid)) ;
		}
	}
</script>

<%@include file="/netmarkets/jsp/util/end.jspf" %>
