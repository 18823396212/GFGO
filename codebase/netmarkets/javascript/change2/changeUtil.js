function CusPickerInputComponentCallback(objects, pickerID, attr, displayFieldId) {
	var updateHiddenIDField = document.getElementById(pickerID);
	var updateHiddenField = document.getElementsByName(pickerID)[0];
	var updateDisplayField = document.getElementsByName(displayFieldId)[0];
	
	var myJSONObjects=objects.pickedObject;
	for(var i=0;i<myJSONObjects.length;i++){
		var oid=myJSONObjects[i].typeInstanceId;
		var displayAttr=myJSONObjects[i][attr];		
		if(pickerID.indexOf('IBA|ResponsiblePerson') > -1){
			// 获取页面所有input控件
			var inputFormArray = document.getElementsByTagName("input");
			// 表单
			var storeTable = PTC.jca.table.Utils.getTable('changeTask_affectedItems_table').getStore();
			for(var i = 0; i < storeTable.getCount(); i++){
				var row = storeTable.getAt(i) ;
				// 列ID
				var rowID = getOidFromRowValue(row.id) ;
				if(displayFieldId.indexOf(rowID) != -1){
					// 获取'责任人'
					var responsiblePerson = row.get('IBA|ResponsiblePerson') ;
					// '责任人'属性值
					var responsiblePersonValue = responsiblePerson.gui.comparable ;
					if(responsiblePersonValue.length == 0){
						for (var j = 0; j < inputFormArray.length; j++) {
							var inputForm = inputFormArray[j];
							if (inputForm.type == 'text'){
								if((inputForm.id.indexOf('IBA|ResponsiblePerson') > -1) && (inputForm.id.indexOf(rowID) > -1)){
									responsiblePersonValue = inputForm.value ;
									break ;
								}
							}
						}
					}
					var agoHtml = new RegExp("value=\"" + responsiblePersonValue + "\"", "g");
					responsiblePerson.gui.comparable = displayAttr ;
					var rowHtml = responsiblePerson.gui.html ;
					responsiblePerson.gui.html = rowHtml.replace(agoHtml,"value=\"" + displayAttr + "\"");
					row.store.afterCommit(responsiblePerson) ;
				}
			}
		}
		updateHiddenField.value = oid ;
		updateDisplayField.value = displayAttr;
		updateHiddenIDField.value = displayAttr ;
	}
	if(pickerID.indexOf('IBA|ResponsiblePerson') > -1){
		saveChangeTaskArray() ;
	}else{
		saveDatasArray() ;
	}
}