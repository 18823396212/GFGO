<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://www.ptc.com/windchill/taglib/picker" prefix="p"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/jcaMvc" prefix="jcaMvc"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ include file="/netmarkets/jsp/components/createEditUIText.jspf"%>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="wctags"%>

<div id='<%=wt.util.HTMLEncoder.encodeForHTMLAttribute(createBean.getCurrentObjectHandle())%>driverAttributesPane'>
<%@ include file="/netmarkets/jsp/components/defineItemReadOnlyPropertyPanel.jspf"%>
<c:choose>
  <c:when test='${title != null}' >
     <c:set var="wizardTitle" value="${title}"/>
  </c:when>
</c:choose>

<c:choose>
  <c:when test='${validTypes != null}' >
      <jca:configureTypePicker>			
         <c:forEach var="item" items="${validTypes}">
            <p:pickerParam name="seedType" value="${item}"/>
         </c:forEach>
         <p:pickerParam name="filterType" value="${filterType}"/>
         <p:pickerParam name="showRoot" value="${showRoot}"/>
         <p:pickerParam name="type" value="ROOT_TYPES"/>
      </jca:configureTypePicker>      
   </c:when>
   <c:otherwise>
      <jca:configureTypePicker>
         <p:pickerParam name="showRoot" value="${showRoot}"/>
      </jca:configureTypePicker>      
   </c:otherwise>
</c:choose>
<%@ include file="/netmarkets/jsp/components/defineItem.jspf"%>
</div>			  
<jcaMvc:attributesTableWizComponent/>

<script src='netmarkets/javascript/hangingChanges/deferChange.js'></script>

<jsp:include page="${mvc:getComponentURL('ext.appo.ecn.mvc.builder.AffectedItemsTableBuilder')}" flush="true"/>

<jsp:include page="${mvc:getComponentURL('ext.appo.ecn.mvc.builder.AffectedEndItemsTableBuilder')}" flush="true"/>

<jsp:include page="${mvc:getComponentURL('ext.appo.ecn.mvc.builder.ChangeTaskTableBuilder')}" flush="true" />

<input type="hidden" name="datasArray" id="datasArray"></input>

<input type="hidden" name="changeTaskArray" id="changeTaskArray"></input>

<input type="hidden" name="affectedProductID" id="affectedProductID"></input>

<SCRIPT LANGUAGE="JavaScript">
	// 受影响对象列表‘收集对象’及‘添加受影响对象’按钮调用
	function addCollectItemsForAffectedEndItems(itemsOid){
		// 保存数据
		saveChangeTaskArray() ;
		// 重新加载数据表
		PTC.jca.table.Utils.reload('ext.appo.ecn.mvc.builder.AffectedItemsTableBuilder',{selectOids:JSON.stringify(itemsOid)},true);
	}
	
	// 受影响对象表单中'责任人'回填
	function responsiblePersonWriteBack(){
		if(document.getElementById("changeTaskArray")){
			// 获取页面所有input控件
			var inputFormArray = document.getElementsByTagName("input");
			// 获取原有数据
			var changeTaskArray = eval("("+document.getElementById("changeTaskArray").value+")");
			if(changeTaskArray.length > 0){
				for (var j = 0; j < inputFormArray.length; j++) {
					var inputForm = inputFormArray[j];
					if (inputForm.type == 'text'){
						if((inputForm.id.indexOf('ResponsiblePerson') > -1) && (inputForm.id.indexOf('userPicker') > -1)){
							for(var i = 0 ; i < changeTaskArray.length ; i++){
								var datasArray = changeTaskArray[i] ;
								if(inputForm.id.indexOf(datasArray['oid']) > -1){
									inputForm.value = datasArray['ResponsiblePerson'] ;
									break ;
								}
							}
						}
					}
				}
			}
		}
	}
	
	// 受影响对象列表‘移除’按钮调用
	function removeAffectedEndItems(event){
		// 移除数据
		var selectRows = PTC.jca.table.Utils.getTableSelectedRowsById('ext.appo.ecn.mvc.builder.AffectedItemsTableBuilder', false, false);
		if(selectRows.length == 0){
			alert("至少选择一项进行删除!") ;
			return ;
		}	
		var table = PTC.jca.table.Utils.getTable('ext.appo.ecn.mvc.builder.AffectedItemsTableBuilder');
        var rowDatas = PTC.jca.table.Utils.getRowData(table);
        var rowCount = rowDatas.length;
        var oidArray = new Array();
        for(var i=0; i<selectRows.length; i++){
            oidArray.push(getOidFromRowValue(selectRows[i].value));
        }
        PTC.jca.table.Utils.removeRows(table, oidArray);
		// 数据保存
		saveChangeTaskArray() ;
    }

    var table_id = "ext.appo.ecn.mvc.builder.ChangeTaskTableBuilder";
	// 移除事务性任务
	function deleteChangeTask(){
		var selectRows = PTC.jca.table.Utils.getTableSelectedRowsById(table_id, false, false);
		if(selectRows.length == 0){
			alert("至少选择一项进行删除!") ;
			return ;
		}

        var table = PTC.jca.table.Utils.getTable(table_id);
        var rowDatas = PTC.jca.table.Utils.getRowData(table);
        var rowCount = rowDatas.length;

        var oidArray = new Array();
        for(var i=0; i<selectRows.length; i++){
            oidArray.push(getOidFromRowValue(selectRows[i].value));
        }
        PTC.jca.table.Utils.removeRows(table, oidArray);
		
		// 保存数据
		delaySaveDatasArray
    }
	
	// 添加数据至事务性任务
	function addChangeTask(){
		saveDatasArray () ;
		// 获取事务性表单数据
		var datasArray = eval("("+document.getElementById("datasArray").value+")");
		// 构建列基本数据
		var url = "netmarkets/jsp/ext/appo/changeNotice/createTransactionalChangeActivity2.jsp";
		var lineID = trim1(ajaxRequest(url, "")) ;
		var json = {'changeTheme':'','changeDescribe':'','responsible':'','changeActivity2':'','needDate':''};
		datasArray[lineID] = json ;
		// 添加列
		reloadTable(JSON.stringify(datasArray)) ;
	}
	
	// 去除空格
	function trim1(str) {
		return str.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
	}
	
	// 重新加载 ChangeTaskTableBuilder
	function reloadTable(param){
		// 保存数据
		document.getElementById("datasArray").value = param ;
		PTC.jca.table.Utils.reload(table_id,{changeTaskBeanID:param},true);
	}
	
	// 移除  AffectedEndItemsTableBuilder 中数据
	function deleteAffectedEndItems(){
		var selectRows = PTC.jca.table.Utils.getTableSelectedRowsById('ext.appo.ecn.mvc.builder.AffectedEndItemsTableBuilder', false, false);
		if(selectRows.length == 0){
			alert("至少选择一项进行删除!") ;
			return ;
		}	
		var table = PTC.jca.table.Utils.getTable('ext.appo.ecn.mvc.builder.AffectedEndItemsTableBuilder');
        var rowDatas = PTC.jca.table.Utils.getRowData(table);
        var rowCount = rowDatas.length;
        var oidArray = new Array();
        for(var i=0; i<selectRows.length; i++){
            oidArray.push(getOidFromRowValue(selectRows[i].value));
        }
        PTC.jca.table.Utils.removeRows(table, oidArray);
		
		// 重新收集表格数据
		var affectedProductID = [] ;
		var tableObj = PTC.jca.table.Utils.getTableRows('ext.appo.ecn.mvc.builder.AffectedEndItemsTableBuilder') ;
		// 获取受影响对象表单中的所有数据
		var tableRows = tableObj.items;
		for(var i = 0; i < tableRows.length; i++){
			affectedProductID[i] = tableRows[i].data.oid ;
		}
		document.getElementById("affectedProductID").value = JSON.stringify(affectedProductID) ;
	}
	
	// 收集受影响对象表中选中数据的上层产品对象
	function collectUpperProduct(){
		var selectRows = PTC.jca.table.Utils.getTableSelectedRowsById('ext.appo.ecn.mvc.builder.AffectedItemsTableBuilder', false, false);
		if(selectRows.length == 0){
			alert("至少选择一条数据进行收集操作!") ;
			return ;
		}
		// 保存数据
		saveChangeTaskArray() ;
		
		var oidArray = new Array();
        for(var i = 0; i < selectRows.length ; i++){
            oidArray.push(getOidFromRowValue(selectRows[i].value));
        }
		if(oidArray.length > 0){
			// 检查选择产品是否符合要求
			var params = "itemOid=" + JSON.stringify(oidArray) + "&method=collectAffectedEndItems";
			var url = "netmarkets/jsp/ext/appo/changeNotice/affectedEndItems.jsp";
			var json = eval("("+ajaxRequest(url, params)+")");
			// 数据反填
			reloadAffectedEndItemsTable(json['resultDatas']) ;
		}
	}
	
	// 重新加载 AffectedEndItemsTableBuilder
	function reloadAffectedEndItemsTable(param){
		// 获取原有数据
		var affectedProductID = eval("("+document.getElementById("affectedProductID").value+")");
		// 输入参数转换JSON
		var addJson = eval("("+param+")");
		for(var i=0; i < addJson.length; i++){
			if(affectedProductID.indexOf(addJson[i]) == -1){
				affectedProductID[affectedProductID.length] = addJson[i] ;
			}
        }
		// 重新赋值
		document.getElementById("affectedProductID").value = JSON.stringify(affectedProductID) ;
		// 重新加载数据表
		PTC.jca.table.Utils.reload('ext.appo.ecn.mvc.builder.AffectedEndItemsTableBuilder',{affectedProductID:JSON.stringify(affectedProductID)},true);
	}
   
    function ajaxRequest(url, params) {
		var options = {
			asynchronous : false,
			parameters : params,
			method : 'POST'
		};
		var transport = requestHandler.doRequest(url, options);
		return transport.responseText ;
	}
	
	// 延时保存数据
	function delaySaveChangeTaskArray(){
		setTimeout("saveChangeTaskArray()",100);
	}
	
	// 保存受影响列表客制化字段信息
	function saveChangeTaskArray(){
		// 获取页面所有input控件
		var inputFormArray = document.getElementsByTagName("input");
		// 用于存储所有数据
		var tableRowArry=[];
		// 获取‘受影响对象’表单中存储的数据
		var tableRows = PTC.jca.table.Utils.getTableRows("ext.appo.ecn.mvc.builder.AffectedItemsTableBuilder").items;
		for(var i = 0; i < tableRows.length; i++){
			var tableRow = tableRows[i].data ;
			var columnArray = {} ;
			columnArray['oid'] = tableRow.oid ;
			columnArray['aadDescription'] = tableRow.aadDescription.gui.comparable ;
			if(tableRow.hasOwnProperty('ArticleInventory')){
				columnArray['ArticleInventory'] = tableRow['ArticleInventory'].gui.comparable ;
			}else{
				columnArray['ArticleInventory'] = '' ;
			}
			if(tableRow.hasOwnProperty('CentralWarehouseInventory')){
				columnArray['CentralWarehouseInventory'] = tableRow['CentralWarehouseInventory'].gui.comparable ;
			}else{
				columnArray['CentralWarehouseInventory'] = '' ;
			}
			if(tableRow.hasOwnProperty('PassageInventory')){
				columnArray['PassageInventory'] = tableRow['PassageInventory'].gui.comparable ;
			}else{
				columnArray['PassageInventory'] = '' ;
			}
			if(tableRow.hasOwnProperty('ResponsiblePerson')){
				var responsiblePersonValue = tableRow['ResponsiblePerson'].gui.comparable ;
				if(responsiblePersonValue.length == 0){
					for (var j = 0; j < inputFormArray.length; j++) {
						var inputForm = inputFormArray[j];
						if (inputForm.type == 'text'){
							if((inputForm.id.indexOf('ResponsiblePerson') > -1) && (inputForm.id.indexOf(tableRow.oid) > -1)){
								responsiblePersonValue = inputForm.value ;
								break ;
							}
						}
					}
				}
				columnArray['ResponsiblePerson'] = responsiblePersonValue ;
			}else{
				columnArray['ResponsiblePerson'] = '' ;
			}
			if(tableRow.hasOwnProperty('ArticleDispose')){
				var articleDispose = tableRow['ArticleDispose'].gui.comparable ;
				if(articleDispose.indexOf('[') > -1){
					columnArray['ArticleDispose'] = articleDispose.substring(articleDispose.lastIndexOf('[') + 1, articleDispose.length - 1) ;
				}else{
					columnArray['ArticleDispose'] = articleDispose;
				}
			}else{
				columnArray['ArticleDispose'] = '' ;
			}
			if(tableRow.hasOwnProperty('PassageDispose')){
				var passageDispose = tableRow['PassageDispose'].gui.comparable
				if(passageDispose.indexOf('[') > -1){
					columnArray['PassageDispose'] = passageDispose.substring(passageDispose.lastIndexOf('[') + 1, passageDispose.length - 1) ;
				}else{
					columnArray['PassageDispose'] = passageDispose ;
				}
			}else{
				columnArray['PassageDispose'] = '' ;
			}
			if(tableRow.hasOwnProperty('InventoryDispose')){
				var inventoryDispose = tableRow['InventoryDispose'].gui.comparable ;
				if(inventoryDispose.indexOf('[') > -1){
					columnArray['InventoryDispose'] =  inventoryDispose.substring(inventoryDispose.lastIndexOf('[') + 1, inventoryDispose.length - 1) ;
				}else{
					columnArray['InventoryDispose'] =  inventoryDispose ;
				}
			}else{
				columnArray['InventoryDispose'] = '' ;
			}
			if(tableRow.hasOwnProperty('CompletionTime')){
				columnArray['CompletionTime'] = tableRow['CompletionTime'].gui.comparable ;
			}else{
				columnArray['CompletionTime'] = '' ;
			}
			if(tableRow.hasOwnProperty('ChangeType')){
				var changeType = tableRow['ChangeType'].gui.comparable ;
				if(changeType.indexOf('[') > -1){
					columnArray['ChangeType'] = changeType.substring(changeType.lastIndexOf('[') + 1, changeType.length - 1) ;
				}else{
					columnArray['ChangeType'] = changeType ;
				}
			}else{
				columnArray['ChangeType'] = '' ;
			}
			if(tableRow.hasOwnProperty('ProductDispose')){
				var productDispose = tableRow['ProductDispose'].gui.comparable ;
				if(productDispose.indexOf('[') > -1){
					columnArray['ProductDispose'] = productDispose.substring(productDispose.lastIndexOf('[') + 1, productDispose.length - 1) ;
				}else{
					columnArray['ProductDispose'] = productDispose ;
				}
			}else{
				columnArray['ProductDispose'] = '' ;
			}
			tableRowArry[i] = columnArray ;
		}
		document.getElementById("changeTaskArray").value = JSON.stringify(tableRowArry) ;
		// 受影响对象表单中'责任人'回填
		setTimeout(function(){
			responsiblePersonWriteBack() ;
		}, 50);
	}
	
	// 保存受影响产品表单中所有数据
	function saveAffectedEndItemsTable(){
		var affectedProductID = [] ;
		// 获取受影响对象表单中的所有数据
		var tableRows = PTC.jca.table.Utils.getTableRows("ext.appo.ecn.mvc.builder.AffectedEndItemsTableBuilder").items;
		for(var i = 0; i < tableRows.length; i++){
			affectedProductID[i] = tableRows[i].data.oid ;
		}
		document.getElementById("affectedProductID").value = JSON.stringify(affectedProductID) ;
	}
	
	// 保存事务性任务表单数据
	function saveDatasArray(){
		// 获取页面所有input控件
		var inputFormArray = document.getElementsByTagName("input");
		// 获取表单中所有存储的数据
		var allDatas = {} ;
		var grid = window.PTC.jca.table.Utils.getTable(table_id);
		var store = grid.getStore();
		store.each(function(record){
			var json = {};
			if(record.data.changeTheme){
				json['changeTheme'] = record.data.changeTheme.gui.comparable;
			}
			if(record.data.changeDescribe){
				json['changeDescribe'] = record.data.changeDescribe.gui.comparable;
			}
			if(record.data.responsible){
				var responsibleValue = record.data.responsible.gui.comparable;
				if(responsibleValue.length == 0){
					for (var j = 0; j < inputFormArray.length; j++) {
						var inputForm = inputFormArray[j];
						if (inputForm.type == 'text'){
							if((inputForm.id.indexOf('responsible') > -1) && (inputForm.id.indexOf(record.data.oid) > -1)){
								responsibleValue = inputForm.value ;
								break ;
							}
						}
					}
				}
				json['responsible'] = responsibleValue ;
			}
			if(record.data.changeActivity2){
				json['changeActivity2'] = record.data.changeActivity2;
			}
			if(record.data.needDate){
				json['needDate'] = record.data.needDate.gui.comparable;
			}
			allDatas[record.data.oid] = json ;
		});
		document.getElementById("datasArray").value = JSON.stringify(allDatas) ;
	}
	
	function delaySaveDatasArray(){
		setTimeout("saveDatasArray()",100);
	}
	
	PTC.onReady(function() {
		setTimeout("saveAffectedEndItemsTable()",1000);

		setTimeout("saveDatasArray()",1000);
		
		setTimeout("saveChangeTaskArray()",1000);
	});
	
	function submitSaveData(){
		saveDatasArray () ;
		
		saveChangeTaskArray() ;
	}
	
	setUserSubmitFunction(submitSaveData) ;
</SCRIPT>

<%@ include file="/netmarkets/jsp/util/end.jspf"%>
