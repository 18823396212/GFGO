<%@page pageEncoding="UTF-8" %>
<%@taglib uri="http://www.ptc.com/windchill/taglib/picker" prefix="p" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/jcaMvc" prefix="jcaMvc" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc" %>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf" %>
<%@ include file="/netmarkets/jsp/components/createEditUIText.jspf" %>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="wctags" %>


<div id='<%=wt.util.HTMLEncoder.encodeForHTMLAttribute(createBean.getCurrentObjectHandle())%>driverAttributesPane'>
    <%@ include file="/netmarkets/jsp/components/defineItemReadOnlyPropertyPanel.jspf" %>
    <c:choose>
        <c:when test='${title != null}'>
            <c:set var="wizardTitle" value="${title}"/>
        </c:when>
    </c:choose>

    <c:choose>
        <c:when test='${validTypes != null}'>
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
    <%@ include file="/netmarkets/jsp/components/defineItem.jspf" %>
</div>
<jcaMvc:attributesTableWizComponent/>
<script src='netmarkets/javascript/hangingChanges/deferChange.js'></script>

<jsp:include page="${mvc:getComponentURL('ext.appo.change.mvc.builder.AffectedItemsTableBuilder')}" flush="true"/>

<jsp:include page="${mvc:getComponentURL('ext.appo.change.mvc.builder.AffectedEndItemsTableBuilder')}" flush="true"/>

<%--add by lzy at 20200417 start--%>
<jsp:include page="${mvc:getComponentURL('ext.appo.change.mvc.builder.UsabilityChangeTaskTableBuilder')}" flush="true"/>
<%--<jsp:include page="${mvc:getComponentURL('ext.appo.change.mvc.builder.ChangeTaskTableBuilder')}" flush="true"/>--%>
<%--add by lzy at 20200417 end--%>

<input type="hidden" name="datasArray" id="datasArray"/>

<input type="hidden" name="changeTaskArray" id="changeTaskArray"/>

<input type="hidden" name="affectedProductID" id="affectedProductID"/>

<input type="hidden" name="affectedProductArray" id="affectedProductArray"/>

<!--modify by wangtong at 20191024 start-->
<input type="hidden" name="routingName" id="routingName"/>
<!--modify by wangtong at 20191024 end-->

<SCRIPT LANGUAGE="JavaScript">
    //一键设置
    function addOneKeySetup(completiontime, userPicker, articleDispose_result, passageDispose_result, inventoryDispose_result, productDispose_result, changeType_result, aadDescription_result) {
        // //修改、保存数据
        // saveChangeTaskArrayByOneKeySetup(completiontime,userPicker,articleDispose_result,passageDispose_result,inventoryDispose_result,productDispose_result,changeType_result,aadDescription_result);
        // // 重新加载数据表
        // PTC.jca.table.Utils.reload('ext.appo.change.mvc.builder.AffectedEndItemsTableBuilder', "", true);

        // 保存数据
        saveChangeTaskArray();
        // 重新加载数据表
        PTC.jca.table.Utils.reload('ext.appo.change.mvc.builder.AffectedItemsTableBuilder', {
            completiontime: completiontime,
            userPicker: userPicker,
            articleDispose_result: articleDispose_result,
            passageDispose_result: passageDispose_result,
            inventoryDispose_result: inventoryDispose_result,
            productDispose_result: productDispose_result,
            changeType_result: changeType_result,
            aadDescription_result: aadDescription_result
        }, true);
        // 保存数据
        saveChangeTaskArray();
    }


    // 受影响对象列表‘收集对象’及‘添加受影响对象’按钮调用
    function addCollectItemsForAffectedEndItems(itemsOid,type) {
        // 保存数据
        saveChangeTaskArray();
        var tableRows = PTC.jca.table.Utils.getTableRows("ext.appo.change.mvc.builder.AffectedItemsTableBuilder").items;
        // 重新加载数据表
        PTC.jca.table.Utils.reload('ext.appo.change.mvc.builder.AffectedItemsTableBuilder', {selectOids: JSON.stringify(itemsOid)}, true);
        //add by lzy at 20200519 start
        if (type.indexOf("collectItems")>-1){
            // 保存数据
            setTimeout(saveChangeTaskArray(), 1000);
        }else{
            var count = parseInt(tableRows.length) + parseInt(itemsOid.length);
            var splitNumber = 10;//拆分数
            var size = parseInt(count) / parseInt(splitNumber);
            //向上取整
            size = Math.ceil(size);
            if (size > 1) {
                // 通过原有受影响列表数 和 选择添加的受影响对象数 延迟保存数据，收集受影响产品
                var time = size * 1200;
                setTimeout("delaySaveAndEndItems(" + JSON.stringify(itemsOid) + ")", time);
            } else {
                setTimeout("delaySaveAndEndItems(" + JSON.stringify(itemsOid) + ")", 1000);
            }
        }
        //add by lzy at 20200519 end
    }

    function delaySaveAndEndItems(itemsOid) {
        // 保存数据
        saveChangeTaskArray();
        //保存受影响产品数据
        saveAffectedEndItemsTable();
        // 检查选择产品是否符合要求
        var params = "itemOid=" + JSON.stringify(itemsOid) + "&method=collectAffectedEndItems";
        var url = "netmarkets/jsp/ext/appo/change/request/affectedEndItems.jsp";
        var json = eval("(" + ajaxRequest(url, params) + ")");
        // 数据反填
        reloadAffectedEndItemsTable(json['resultDatas']);
        // add by lzy at 20200519 end
    }

    // 受影响对象表单中'责任人'回填
    function responsiblePersonWriteBack() {
        if (document.getElementById("changeTaskArray")) {
            // 获取页面所有input控件
            var inputFormArray = document.getElementsByTagName("input");
            // 获取原有数据
            var changeTaskArray = eval("(" + document.getElementById("changeTaskArray").value + ")");
            if (changeTaskArray.length > 0) {
                for (var j = 0; j < inputFormArray.length; j++) {
                    var inputForm = inputFormArray[j];
                    if (inputForm.type === 'text') {
                        // if ((inputForm.id.indexOf('ResponsiblePerson') > -1) && (inputForm.id.indexOf('userPicker') > -1)) {
                        if ((inputForm.id.indexOf('ResponsiblePerson') > -1)) {
                            for (var i = 0; i < changeTaskArray.length; i++) {
                                var datasArray = changeTaskArray[i];
                                if (inputForm.id.indexOf(datasArray['oid']) > -1) {
                                    inputForm.value = datasArray['ResponsiblePerson'];
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 受影响对象列表‘移除’按钮调用
    function removeAffectedEndItems(event) {
        // 移除数据
        var selectRows = PTC.jca.table.Utils.getTableSelectedRowsById('ext.appo.change.mvc.builder.AffectedItemsTableBuilder', false, false);
        if (selectRows.length === 0) {
            alert("至少选择一项进行删除!");
            return;
        }
        var table = PTC.jca.table.Utils.getTable('ext.appo.change.mvc.builder.AffectedItemsTableBuilder');
        var oidArray = [];
        for (var i = 0; i < selectRows.length; i++) {
            oidArray.push(getOidFromRowValue(selectRows[i].value));
        }
        PTC.jca.table.Utils.removeRows(table, oidArray);
        //add by lzy at 20200520 start
        //删除‘移除’受影响对象的受影响产品（不删除不是‘移除’受影响对象的受影响产品）
        //保存受影响产品数据
        saveAffectedEndItemsTable();
        // 获取‘移除’受影响对象的受影响产品oid
        var params = "itemOid=" + JSON.stringify(oidArray) + "&method=collectAffectedEndItems";
        var url = "netmarkets/jsp/ext/appo/changeNotice/affectedEndItems.jsp";
        var json = eval("(" + ajaxRequest(url, params) + ")");
        //获取不是'移除'受影响对象的受影响产品oid
        var tableRows = PTC.jca.table.Utils.getTableRows("ext.appo.change.mvc.builder.AffectedItemsTableBuilder").items;
        var oids = [];
        for (var i = 0; i < tableRows.length; i++) {
            var tableRow = tableRows[i].data;
            //不是‘移除’受影响对象
            if (oidArray.indexOf(tableRow.oid) == -1)
                oids.push(tableRow.oid);
        }
        // 获取不是‘移除’受影响对象的受影响产品oid
        var params2 = "itemOid=" + JSON.stringify(oids) + "&method=collectAffectedEndItems";
        var url2 = "netmarkets/jsp/ext/appo/changeNotice/affectedEndItems.jsp";
        var json2 = eval("(" + ajaxRequest(url2, params2) + ")");
        // 输入参数转换JSON
        var removeOid = eval("(" + json['resultDatas'] + ")");
        var noRemoveOid = eval("(" + json2['resultDatas'] + ")");
        //移除不需要的Oid
        //需要移除的oid
        var needRemoveOid = [];
        for (var i = 0; i < removeOid.length; i++) {
            for (var j = 0; j < noRemoveOid.length; j++) {
                if (removeOid[i].indexOf(noRemoveOid[j]) > -1) {
                    needRemoveOid.push(removeOid[i]);
                }
            }
        }
        if (needRemoveOid.length > 0) {
            for (let i = 0; i < needRemoveOid.length; i++) {
                removeOid.remove(needRemoveOid[i]);
            }
        }
        //通过removeOid获取获取受影响产品表单对应Oid
        var params3 = "removeOid=" + JSON.stringify(removeOid)
        var url3 = "netmarkets/jsp/ext/appo/change/request/getAffectedEndItemsOid.jsp";
        var json3 = eval("(" + ajaxRequest(url3, params3) + ")");
        var rowOids = eval("(" + json3['resultDatas'] + ")");
        //移除需要移除的受影响产品
        var table2 = PTC.jca.table.Utils.getTable('ext.appo.change.mvc.builder.AffectedEndItemsTableBuilder');
        PTC.jca.table.Utils.removeRows(table2, rowOids);
        //保存受影响产品数据
        saveAffectedEndItemsTable();
        //add by lzy at 20200520 end
        // 数据保存
        saveChangeTaskArray();
    }

    var table_id = "ext.appo.change.mvc.builder.UsabilityChangeTaskTableBuilder";

    // 移除事务性任务
    function deleteChangeTask() {
        var selectRows = PTC.jca.table.Utils.getTableSelectedRowsById(table_id, false, false);
        if (selectRows.length === 0) {
            alert("至少选择一项进行删除!");
            return;
        }

        var table = PTC.jca.table.Utils.getTable(table_id);
        var rowDatas = PTC.jca.table.Utils.getRowData(table);
        var rowCount = rowDatas.length;

        var oidArray = [];
        for (var i = 0; i < selectRows.length; i++) {
            oidArray.push(getOidFromRowValue(selectRows[i].value));
        }
        PTC.jca.table.Utils.removeRows(table, oidArray);

        // 保存数据
        delaySaveDatasArray();
    }

    // 添加数据至事务性任务
    function addChangeTask() {
        saveDatasArray();
        // 获取事务性表单数据
        var datasArray = eval("(" + document.getElementById("datasArray").value + ")");
        // 构建列基本数据
        var url = "netmarkets/jsp/ext/appo/changeNotice/createTransactionalChangeActivity2.jsp";
        var lineID = trim1(ajaxRequest(url, ""));
        datasArray[lineID] = {
            'changeTheme': '',
            'changeDescribe': '',
            'responsible': '',
            'changeActivity2': '',
            'needDate': ''
        };
        // 添加列
        reloadTable(JSON.stringify(datasArray));
    }

    // 去除空格
    function trim1(str) {
        return str.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
    }

    // 重新加载 UsabilityChangeTaskTableBuilder
    function reloadTable(param) {
        // 保存数据
        document.getElementById("datasArray").value = param;
        PTC.jca.table.Utils.reload(table_id, {changeTaskBeanID: param}, true);
    }

    // 移除  AffectedEndItemsTableBuilder(受影响产品) 中数据
    function deleteAffectedEndItems() {
        var selectRows = PTC.jca.table.Utils.getTableSelectedRowsById('ext.appo.change.mvc.builder.AffectedEndItemsTableBuilder', false, false);
        if (selectRows.length === 0) {
            alert("至少选择一项进行删除!");
            return;
        }
        var table = PTC.jca.table.Utils.getTable('ext.appo.change.mvc.builder.AffectedEndItemsTableBuilder');
        var rowDatas = PTC.jca.table.Utils.getRowData(table);
        var rowCount = rowDatas.length;
        var oidArray = [];
        for (var i = 0; i < selectRows.length; i++) {
            oidArray.push(getOidFromRowValue(selectRows[i].value));
        }
        PTC.jca.table.Utils.removeRows(table, oidArray);

        // 重新收集表格数据
        var affectedProductID = [];
        var tableObj = PTC.jca.table.Utils.getTableRows('ext.appo.change.mvc.builder.AffectedEndItemsTableBuilder');
        // 获取受影响对象表单中的所有数据
        var tableRows = tableObj.items;
        for (var i = 0; i < tableRows.length; i++) {
            affectedProductID[i] = tableRows[i].data.oid;
        }
        document.getElementById("affectedProductID").value = JSON.stringify(affectedProductID);
        //add by lzy at 20200414 start
        //保存受影响产品数据
        saveAffectedEndItemsTable();
        //add by lzy at 20200414 end
    }

    // 收集受影响对象表中选中数据的上层产品对象
    function collectUpperProduct() {
        var selectRows = PTC.jca.table.Utils.getTableSelectedRowsById('ext.appo.change.mvc.builder.AffectedItemsTableBuilder', false, false);
        if (selectRows.length === 0) {
            alert("至少选择一条数据进行收集操作!");
            return;
        }
        // 保存数据
        saveChangeTaskArray();
        //add by lzy at 20200414 start
        //保存受影响产品数据
        saveAffectedEndItemsTable();
        //add by lzy at 20200414 end
        var oidArray = [];
        for (var i = 0; i < selectRows.length; i++) {
            oidArray.push(getOidFromRowValue(selectRows[i].value));
        }
        if (oidArray.length > 0) {
            // 检查选择产品是否符合要求
            var params = "itemOid=" + JSON.stringify(oidArray) + "&method=collectAffectedEndItems";
            var url = "netmarkets/jsp/ext/appo/changeNotice/affectedEndItems.jsp";
            var json = eval("(" + ajaxRequest(url, params) + ")");
            // 数据反填
            reloadAffectedEndItemsTable(json['resultDatas']);
        }
    }

    // 重新加载 AffectedEndItemsTableBuilder
    function reloadAffectedEndItemsTable(param) {
        // 获取原有数据
        var affectedProductID = eval("(" + document.getElementById("affectedProductID").value + ")");
        // 输入参数转换JSON
        var addJson = eval("(" + param + ")");
        for (var i = 0; i < addJson.length; i++) {
            if (affectedProductID.indexOf(addJson[i]) === -1) {
                affectedProductID[affectedProductID.length] = addJson[i];
            }
        }
        // 重新赋值
        document.getElementById("affectedProductID").value = JSON.stringify(affectedProductID);
        // 重新加载数据表
        PTC.jca.table.Utils.reload('ext.appo.change.mvc.builder.AffectedEndItemsTableBuilder', {affectedProductID: JSON.stringify(affectedProductID)}, true);
    }

    function ajaxRequest(url, params) {
        var options = {
            asynchronous: false,
            parameters: params,
            method: 'POST'
        };
        var transport = requestHandler.doRequest(url, options);
        return transport.responseText;
    }

    // 延时保存数据
    function delaySaveChangeTaskArray() {
        setTimeout("saveChangeTaskArray()", 100);
    }

    // 保存受影响列表客制化字段信息
    function saveChangeTaskArray() {
        //add by lzy at 20200119 start
        // 获取页面所有select控件
        var selectArray = document.getElementsByTagName("select");
        //add by lzy at 20200119 end
        // 获取页面所有input控件
        var inputFormArray = document.getElementsByTagName("input");
        // 用于存储所有数据
        var tableRowArry = [];
        // 获取‘受影响对象’表单中存储的数据
        var tableRows = PTC.jca.table.Utils.getTableRows("ext.appo.change.mvc.builder.AffectedItemsTableBuilder").items;
        for (var i = 0; i < tableRows.length; i++) {
            var tableRow = tableRows[i].data;
            var columnArray = {};
            columnArray['oid'] = tableRow.oid;
            //add by lzy at 20200119 start
            if (tableRow.hasOwnProperty('aadDescription')) {
                var aadDescription = tableRow['aadDescription'].gui.comparable;
                //add by lzy at 20200118 start
                for (var j = 0; j < inputFormArray.length; j++) {
                    var inputForm = inputFormArray[j];
                    if ((inputForm.name.indexOf('aadDescription') > -1) && (inputForm.name.indexOf(tableRow.oid) > -1)) {
                        aadDescription = inputForm.value;
                        break;
                    }
                }
                columnArray['aadDescription'] = aadDescription;
            }
            //add by lzy at 20200118 end
            // columnArray['aadDescription'] = tableRow.aadDescription.gui.comparable;
            if (tableRow.hasOwnProperty('ArticleInventory')) {
                columnArray['ArticleInventory'] = tableRow['ArticleInventory'].gui.comparable;
            } else {
                columnArray['ArticleInventory'] = '';
            }
            if (tableRow.hasOwnProperty('CentralWarehouseInventory')) {
                columnArray['CentralWarehouseInventory'] = tableRow['CentralWarehouseInventory'].gui.comparable;
            } else {
                columnArray['CentralWarehouseInventory'] = '';
            }
            if (tableRow.hasOwnProperty('PassageInventory')) {
                columnArray['PassageInventory'] = tableRow['PassageInventory'].gui.comparable;
            } else {
                columnArray['PassageInventory'] = '';
            }
            if (tableRow.hasOwnProperty('ResponsiblePerson')) {
                var responsiblePersonValue = tableRow['ResponsiblePerson'].gui.comparable;

                // if (str!=null&&str.trim()!=""){
                //     responsiblePersonValue=str;
                // }else{
                //     if (responsiblePersonValue.length === 0) {
                //         for (var j = 0; j < inputFormArray.length; j++) {
                //             var inputForm = inputFormArray[j];
                //             if (inputForm.type === 'text') {
                //                 if (inputForm.id.indexOf('ResponsiblePerson') > -1 && inputForm.id.indexOf(tableRow.oid) > -1) {
                //                     responsiblePersonValue = inputForm.value;
                //                     break;
                //                 }
                //             }
                //         }
                //     }
                // }
                if (responsiblePersonValue.length === 0) {
                    for (var j = 0; j < inputFormArray.length; j++) {
                        var inputForm = inputFormArray[j];
                        if (inputForm.type === 'text') {
                            if (inputForm.id.indexOf('ResponsiblePerson') > -1 && inputForm.id.indexOf(tableRow.oid) > -1) {
                                responsiblePersonValue = inputForm.value;
                                break;
                            }
                        }
                    }
                }
                if (responsiblePersonValue.length === 0) {
                    var str = JSON.stringify(tableRow['ResponsiblePerson'].gui);
                    if (str.indexOf("value") > -1) {
                        str = str.substring(str.indexOf("value") + 8, str.length - 1);
                    }
                    if (str.indexOf("\"") > -1) {
                        str = str.substring(0, str.indexOf("\"") - 1);
                    }
                    responsiblePersonValue = str;
                }
                columnArray['ResponsiblePerson'] = responsiblePersonValue;
            } else {
                columnArray['ResponsiblePerson'] = '';
            }
            if (tableRow.hasOwnProperty('ArticleDispose')) {
                var articleDispose = tableRow['ArticleDispose'].gui.comparable;
                //add by lzy at 20200118 start
                for (var j = 0; j < selectArray.length; j++) {
                    var select = selectArray[j];
                    if ((select.name.indexOf('ArticleDispose') > -1) && (select.name.indexOf(tableRow.oid) > -1)) {
                        articleDispose = select.value;
                        break;
                    }
                }

                if (articleDispose.indexOf('[') > -1) {
                    articleDispose = articleDispose.substring(articleDispose.lastIndexOf('[') + 1, articleDispose.length - 1);
                }
                if (articleDispose.indexOf(']') > -1) {
                    articleDispose = articleDispose.substring(0, articleDispose.indexOf(']') - 1);
                }

                columnArray['ArticleDispose'] = articleDispose;
                //add by lzy at 20200118 end
                // if (articleDispose.indexOf('[') > -1) {
                //     columnArray['ArticleDispose'] = articleDispose.substring(articleDispose.lastIndexOf('[') + 1, articleDispose.length - 1);
                // } else {
                //     columnArray['ArticleDispose'] = articleDispose;
                // }
            } else {
                columnArray['ArticleDispose'] = '';
            }
            if (tableRow.hasOwnProperty('PassageDispose')) {
                var passageDispose = tableRow['PassageDispose'].gui.comparable
                //add by lzy at 20200118 start
                for (var j = 0; j < selectArray.length; j++) {
                    var select = selectArray[j];
                    if ((select.name.indexOf('PassageDispose') > -1) && (select.name.indexOf(tableRow.oid) > -1)) {
                        passageDispose = select.value;
                        break;
                    }
                }

                if (passageDispose.indexOf('[') > -1) {
                    passageDispose = passageDispose.substring(passageDispose.lastIndexOf('[') + 1, passageDispose.length - 1);
                }
                if (passageDispose.indexOf(']') > -1) {
                    passageDispose = passageDispose.substring(0, passageDispose.indexOf(']') - 1);
                }

                columnArray['PassageDispose'] = passageDispose;
                //add by lzy at 20200118 end
                // if (passageDispose.indexOf('[') > -1) {
                //     columnArray['PassageDispose'] = passageDispose.substring(passageDispose.lastIndexOf('[') + 1, passageDispose.length - 1);
                // } else {
                //     columnArray['PassageDispose'] = passageDispose;
                // }
            } else {
                columnArray['PassageDispose'] = '';
            }
            if (tableRow.hasOwnProperty('InventoryDispose')) {
                var inventoryDispose = tableRow['InventoryDispose'].gui.comparable;
                //add by lzy at 20200118 start
                for (var j = 0; j < selectArray.length; j++) {
                    var select = selectArray[j];
                    if ((select.name.indexOf('InventoryDispose') > -1) && (select.name.indexOf(tableRow.oid) > -1)) {
                        inventoryDispose = select.value;
                        break;
                    }
                }
                if (inventoryDispose.indexOf('[') > -1) {
                    inventoryDispose = inventoryDispose.substring(inventoryDispose.lastIndexOf('[') + 1, inventoryDispose.length - 1);
                }
                if (inventoryDispose.indexOf(']') > -1) {
                    inventoryDispose = inventoryDispose.substring(0, inventoryDispose.indexOf(']') - 1);
                }
                columnArray['InventoryDispose'] = inventoryDispose;
                //add by lzy at 20200118 end
                // if (inventoryDispose.indexOf('[') > -1) {
                //     columnArray['InventoryDispose'] = inventoryDispose.substring(inventoryDispose.lastIndexOf('[') + 1, inventoryDispose.length - 1);
                // } else {
                //     columnArray['InventoryDispose'] = inventoryDispose;
                // }
            } else {
                columnArray['InventoryDispose'] = '';
            }
            if (tableRow.hasOwnProperty('CompletionTime')) {
                //add by lzy at 20200113 start
                var completionTimeValue = tableRow['CompletionTime'].gui.comparable;
                for (var j = 0; j < selectArray.length; j++) {
                    var select = selectArray[j];
                    if ((select.name.indexOf('CompletionTime') > -1) && (select.name.indexOf(tableRow.oid) > -1)) {
                        completionTimeValue = select.value;
                        break;
                    }
                }
                columnArray['CompletionTime'] = completionTimeValue;
                //add by lzy at 20200113 end
                // columnArray['CompletionTime'] = tableRow['CompletionTime'].gui.comparable;
            } else {
                columnArray['CompletionTime'] = '';
            }
            if (tableRow.hasOwnProperty('ChangeType')) {
                var changeType = tableRow['ChangeType'].gui.comparable;
                //add by lzy at 20200118 start
                // for (var j = 0; j < selectArray.length; j++) {
                //     var select = selectArray[j];
                //     if ((select.name.indexOf('ChangeType') > -1) && (select.name.indexOf(tableRow.oid) > -1)) {
                //         changeType = select.value;
                //         break;
                //     }
                // }
                if (changeType.indexOf('[') > -1) {
                    changeType = changeType.substring(changeType.lastIndexOf('[') + 1, changeType.length - 1);
                }
                if (changeType.indexOf(']') > -1) {
                    changeType = changeType.substring(0, changeType.indexOf(']') - 1);
                }
                columnArray['ChangeType'] = changeType;
                //add by lzy at 20200118 end
                // if (changeType.indexOf('[') > -1) {
                //     columnArray['ChangeType'] = changeType.substring(changeType.lastIndexOf('[') + 1, changeType.length - 1);
                // } else {
                //     columnArray['ChangeType'] = changeType;
                // }
            } else {
                columnArray['ChangeType'] = '';
            }
            if (tableRow.hasOwnProperty('ProductDispose')) {
                var productDispose = tableRow['ProductDispose'].gui.comparable;
                //add by lzy at 20200118 start
                // for (var j = 0; j < selectArray.length; j++) {
                //     var select = selectArray[j];
                //     if ((select.name.indexOf('ProductDispose') > -1) && (select.name.indexOf(tableRow.oid) > -1)) {
                //         productDispose = select.value;
                //         break;
                //     }
                // }

                if (productDispose.indexOf('[') > -1) {
                    productDispose = productDispose.substring(productDispose.lastIndexOf('[') + 1, productDispose.length - 1);
                }
                if (productDispose.indexOf(']') > -1) {
                    productDispose = productDispose.substring(0, productDispose.indexOf(']') - 1);
                }

                columnArray['ProductDispose'] = productDispose;
                //add by lzy at 20200118 end
                // if (productDispose.indexOf('[') > -1) {
                //     columnArray['ProductDispose'] = productDispose.substring(productDispose.lastIndexOf('[') + 1, productDispose.length - 1);
                // } else {
                //     columnArray['ProductDispose'] = productDispose;
                // }
            } else {
                columnArray['ProductDispose'] = '';
            }

            //add by tongwang 20191023 start
            if (tableRow.hasOwnProperty('ChangeObjectType')) {
                var changeObjectType = tableRow['ChangeObjectType'].gui.comparable;
                if (changeObjectType.indexOf('[') > -1) {
                    changeObjectType = changeObjectType.substring(changeObjectType.lastIndexOf('[') + 1, changeObjectType.length - 1);
                }
                if (changeObjectType.indexOf(']') > -1) {
                    changeObjectType = changeObjectType.substring(0, changeObjectType.indexOf(']') - 1);
                }
                columnArray['ChangeObjectType'] = changeObjectType;
            } else {
                columnArray['ChangeObjectType'] = '';
            }
            columnArray['CollectionNumber'] = tableRow.CollectionNumber.gui.comparable;
            //add by tongwang 20191023 end

            tableRowArry[i] = columnArray;
        }
        document.getElementById("changeTaskArray").value = JSON.stringify(tableRowArry);
        // 受影响对象表单中'责任人'回填
        setTimeout(function () {
            responsiblePersonWriteBack();
        }, 50);
    }

    // 保存受影响产品表单中所有数据
    function saveAffectedEndItemsTable() {
        var affectedProductID = [];
        // 获取受影响对象表单中的所有数据
        var tableRows = PTC.jca.table.Utils.getTableRows("ext.appo.change.mvc.builder.AffectedEndItemsTableBuilder").items;
        // for (var i = 0; i < tableRows.length; i++) {
        //     affectedProductID[i] = tableRows[i].data.oid;
        // }
        // document.getElementById("affectedProductID").value = JSON.stringify(affectedProductID);
        //add by lzy at 20200414 start
        // 获取页面所有input控件
        var inputFormArray = document.getElementsByTagName("input");
        var affectedProductArray = [];
        for (var i = 0; i < tableRows.length; i++) {
            var columnArray = {};
            var tableRow = tableRows[i].data;
            affectedProductID[i] = tableRow.oid;
            columnArray['oid'] = tableRow.oid;
            if (tableRow.hasOwnProperty('clfs')) {
                var clfs = tableRow['clfs'].gui.comparable;
                for (var j = 0; j < inputFormArray.length; j++) {
                    var inputForm = inputFormArray[j];
                    if ((inputForm.name.indexOf('clfs') > -1) && (inputForm.name.indexOf(tableRow.oid) > -1)) {
                        clfs = inputForm.value;
                        break;
                    }
                }
                columnArray['clfs'] = clfs;
            }
            affectedProductArray[i] = columnArray;
        }
        document.getElementById("affectedProductID").value = JSON.stringify(affectedProductID);
        document.getElementById("affectedProductArray").value = JSON.stringify(affectedProductArray);
        //add by lzy at 20200414 end
    }

    // 保存事务性任务表单数据
    function saveDatasArray() {
        // 获取页面所有input控件
        var inputFormArray = document.getElementsByTagName("input");
        // 获取表单中所有存储的数据
        var allDatas = {};
        var grid = window.PTC.jca.table.Utils.getTable(table_id);
        var store = grid.getStore();
        store.each(function (record) {
            var json = {};
            if (record.data.changeTheme) {
                json['changeTheme'] = record.data.changeTheme.gui.comparable;
            }
            if (record.data.changeDescribe) {
                json['changeDescribe'] = record.data.changeDescribe.gui.comparable;
            }
            if (record.data.responsible) {
                var responsibleValue = record.data.responsible.gui.comparable;
                if (responsibleValue.length === 0) {
                    for (var j = 0; j < inputFormArray.length; j++) {
                        var inputForm = inputFormArray[j];
                        if (inputForm.type === 'text') {
                            if ((inputForm.id.indexOf('responsible') > -1) && (inputForm.id.indexOf(record.data.oid) > -1)) {
                                responsibleValue = inputForm.value;
                                break;
                            }
                        }
                    }
                }
                json['responsible'] = responsibleValue;
            }
            if (record.data.changeActivity2) {
                json['changeActivity2'] = record.data.changeActivity2;
            }
            if (record.data.needDate) {
                json['needDate'] = record.data.needDate.gui.comparable;
            }
            //add by lzy at 20200417 start
            if (record.data.taskType) {
                json['taskType'] = record.data.taskType.gui.comparable;
            }
            if (record.data.glfs) {
                json['glfs'] = record.data.glfs.gui.comparable;
            }
            if (record.data.taskState) {
                json['taskState'] = record.data.taskState;
            }
            if (record.data.taskNumber) {
                json['taskNumber'] = record.data.taskNumber;
            }
            if (record.data.actualDate) {
                json['actualDate'] = record.data.actualDate;
            }
            //add by lzy at 20200417 end
            allDatas[record.data.oid] = json;
        });
        document.getElementById("datasArray").value = JSON.stringify(allDatas);
    }

    function delaySaveDatasArray() {
        setTimeout("saveDatasArray()", 100);
    }

    PTC.onReady(function () {
        setTimeout("saveAffectedEndItemsTable()", 1000);

        setTimeout("saveDatasArray()", 1000);

        setTimeout("saveChangeTaskArray()", 1000);

        //add by tongwang 20191023 start
        /*document.getElementById("PJL_wizard_cache").onclick = function () {
            document.getElementById("routingName").value = "cacheButton";
        };*/
        document.getElementById("PJL_wizard_ok").onclick = function () {
            document.getElementById("routingName").value = "cacheButton";
        };
        //add by tongwang 20191023 end
        //add by lzy at 20200110 start
        document.getElementById("PJL_wizard_cache").onclick = function () {
            document.getElementById("routingName").value = "newCacheButton";
        };
        //add by lzy at 20200110 end
    });

    function submitSaveData() {
        saveDatasArray();

        saveChangeTaskArray();
        //add by lzy at 20200414 start
        saveAffectedEndItemsTable();
        //add by lzy at 20200414 end
    }

    setUserSubmitFunction(submitSaveData);


    // 保存受影响列表客制化字段信息（一键设置功能保存）
    function saveChangeTaskArrayByOneKeySetup(completiontime, userPicker, articleDispose_result, passageDispose_result, inventoryDispose_result, productDispose_result, changeType_result, aadDescription_result) {
        // 获取页面所有input控件
        var inputFormArray = document.getElementsByTagName("input");
        // 用于存储所有数据
        var tableRowArry = [];
        // 获取‘受影响对象’表单中存储的数据
        var tableRows = PTC.jca.table.Utils.getTableRows("ext.appo.change.mvc.builder.AffectedItemsTableBuilder").items;
        for (var i = 0; i < tableRows.length; i++) {
            var tableRow = tableRows[i].data;
            var columnArray = {};
            columnArray['oid'] = tableRow.oid;
            //add by lzy at 20200119 start
            if (tableRow.hasOwnProperty('aadDescription')) {
                var gui = JSON.stringify(tableRow['aadDescription'].gui);
                var isEdit = gui.indexOf('disabled');//isEdit为-1表示不存在该字符，即是可编辑状态
                if (aadDescription_result != null && aadDescription_result != "" && isEdit == -1) {
                    columnArray['aadDescription'] = aadDescription_result;
                } else {
                    columnArray['aadDescription'] = tableRow['aadDescription'].gui.comparable;
                }
            }
            //add by lzy at 20200119 end
            // columnArray['aadDescription'] = tableRow.aadDescription.gui.comparable;
            if (tableRow.hasOwnProperty('ArticleInventory')) {
                columnArray['ArticleInventory'] = tableRow['ArticleInventory'].gui.comparable;
            } else {
                columnArray['ArticleInventory'] = '';
            }
            if (tableRow.hasOwnProperty('CentralWarehouseInventory')) {
                columnArray['CentralWarehouseInventory'] = tableRow['CentralWarehouseInventory'].gui.comparable;
            } else {
                columnArray['CentralWarehouseInventory'] = '';
            }
            if (tableRow.hasOwnProperty('PassageInventory')) {
                columnArray['PassageInventory'] = tableRow['PassageInventory'].gui.comparable;
            } else {
                columnArray['PassageInventory'] = '';
            }
            if (tableRow.hasOwnProperty('ResponsiblePerson')) {
                var responsiblePersonValue = tableRow['ResponsiblePerson'].gui.comparable;
                if (responsiblePersonValue.length === 0) {
                    for (var j = 0; j < inputFormArray.length; j++) {
                        var inputForm = inputFormArray[j];
                        if (inputForm.type === 'text') {
                            if ((inputForm.id.indexOf('ResponsiblePerson') > -1) && (inputForm.id.indexOf(tableRow.oid) > -1)) {
                                responsiblePersonValue = inputForm.value;
                                break;
                            }
                        }
                    }
                }
                //add by lzy at 20200109 start
                var gui = JSON.stringify(tableRow['ResponsiblePerson'].gui);
                var isEdit = gui.indexOf('disabled');//isEdit为-1表示不存在该字符，即是可编辑状态
                if (userPicker != null && userPicker != "" && isEdit == -1) {
                    responsiblePersonValue = userPicker;
                }
                //add by lzy at 20200109 end
                columnArray['ResponsiblePerson'] = responsiblePersonValue;
            } else {
                columnArray['ResponsiblePerson'] = '';
            }
            if (tableRow.hasOwnProperty('ArticleDispose')) {
                var articleDispose = tableRow['ArticleDispose'].gui.comparable;
                //add by lzy at 20200118 start
                var gui = JSON.stringify(tableRow['ArticleDispose'].gui);
                var isEdit = gui.indexOf('disabled');//isEdit为-1表示不存在该字符，即是可编辑状态
                if (articleDispose_result != null && articleDispose_result != "" && isEdit == -1) {
                    columnArray['ArticleDispose'] = articleDispose_result;
                } else {
                    if (articleDispose.indexOf('[') > -1) {
                        articleDispose = articleDispose.substring(articleDispose.lastIndexOf('[') + 1, articleDispose.length - 1);
                    }
                    if (articleDispose.indexOf(']') > -1) {
                        articleDispose = articleDispose.substring(0, articleDispose.indexOf(']') - 1);
                    }

                    columnArray['ArticleDispose'] = articleDispose;
                }
                //add by lzy at 20200118 end
                // if (articleDispose.indexOf('[') > -1) {
                //     columnArray['ArticleDispose'] = articleDispose.substring(articleDispose.lastIndexOf('[') + 1, articleDispose.length - 1);
                // } else {
                //     columnArray['ArticleDispose'] = articleDispose;
                // }
            } else {
                columnArray['ArticleDispose'] = '';
            }
            if (tableRow.hasOwnProperty('PassageDispose')) {
                var passageDispose = tableRow['PassageDispose'].gui.comparable
                //add by lzy at 20200118 start
                var gui = JSON.stringify(tableRow['PassageDispose'].gui);
                var isEdit = gui.indexOf('disabled');//isEdit为-1表示不存在该字符，即是可编辑状态
                if (passageDispose_result != null && passageDispose_result != "" && isEdit == -1) {
                    columnArray['PassageDispose'] = passageDispose_result;
                } else {
                    if (passageDispose.indexOf('[') > -1) {
                        passageDispose = passageDispose.substring(passageDispose.lastIndexOf('[') + 1, passageDispose.length - 1);
                    }
                    if (passageDispose.indexOf(']') > -1) {
                        passageDispose = passageDispose.substring(0, passageDispose.indexOf(']') - 1);
                    }
                    columnArray['PassageDispose'] = passageDispose;
                }
                //add by lzy at 20200118 end

                // if (passageDispose.indexOf('[') > -1) {
                //     // columnArray['PassageDispose'] = passageDispose.substring(passageDispose.lastIndexOf('[') + 1, passageDispose.length - 1);
                // } else {
                //     columnArray['PassageDispose'] = passageDispose;
                // }
            } else {
                columnArray['PassageDispose'] = '';
            }
            if (tableRow.hasOwnProperty('InventoryDispose')) {
                var inventoryDispose = tableRow['InventoryDispose'].gui.comparable;
                //add by lzy at 20200118 start
                var gui = JSON.stringify(tableRow['InventoryDispose'].gui);
                var isEdit = gui.indexOf('disabled');//isEdit为-1表示不存在该字符，即是可编辑状态
                if (inventoryDispose_result != null && inventoryDispose_result != "" && isEdit == -1) {
                    columnArray['InventoryDispose'] = inventoryDispose_result;
                } else {
                    if (inventoryDispose.indexOf('[') > -1) {
                        inventoryDispose = inventoryDispose.substring(inventoryDispose.lastIndexOf('[') + 1, inventoryDispose.length - 1);
                    }
                    if (inventoryDispose.indexOf(']') > -1) {
                        inventoryDispose = inventoryDispose.substring(0, inventoryDispose.indexOf(']') - 1);
                    }
                    columnArray['InventoryDispose'] = inventoryDispose;

                }
                //add by lzy at 20200118 end
                // if (inventoryDispose.indexOf('[') > -1) {
                //     columnArray['InventoryDispose'] = inventoryDispose.substring(inventoryDispose.lastIndexOf('[') + 1, inventoryDispose.length - 1);
                // } else {
                //     columnArray['InventoryDispose'] = inventoryDispose;
                // }
            } else {
                columnArray['InventoryDispose'] = '';
            }
            if (tableRow.hasOwnProperty('CompletionTime')) {
                //add by lzy at 20200109 start
                var completionTimeValue = tableRow['CompletionTime'].gui.comparable;
                if (completionTimeValue.length === 0) {
                    for (var j = 0; j < inputFormArray.length; j++) {
                        var inputForm = inputFormArray[j];
                        if (inputForm.type === 'text') {
                            if ((inputForm.name.indexOf('CompletionTime') > -1) && (inputForm.name.indexOf(tableRow.oid) > -1)) {
                                completionTimeValue = inputForm.value;
                                break;
                            }
                        }
                    }
                }
                var gui = JSON.stringify(tableRow['CompletionTime'].gui);
                var isEdit = gui.indexOf('disabled');//isEdit为-1表示不存在该字符，即是可编辑状态
                if (completiontime != null && completiontime != "" && isEdit == -1) {
                    completionTimeValue = completiontime;
                }
                columnArray['CompletionTime'] = date(completionTimeValue);
                //add by lzy at 20200109 end
            } else {
                columnArray['CompletionTime'] = '';
            }
            if (tableRow.hasOwnProperty('ChangeType')) {
                var changeType = tableRow['ChangeType'].gui.comparable;
                //add by lzy at 20200118 start
                var gui = JSON.stringify(tableRow['ChangeType'].gui);
                var isEdit = gui.indexOf('disabled');//isEdit为-1表示不存在该字符，即是可编辑状态
                if (changeType_result != null && changeType_result != "" && isEdit == -1) {
                    columnArray['ChangeType'] = changeType_result;
                } else {
                    if (changeType.indexOf('[') > -1 || changeType.indexOf(']') > -1) {
                        var changeTypeValue = "";
                        if (changeType.indexOf('[') > -1) {
                            changeTypeValue = changeTypeValue = changeType.substring(changeType.lastIndexOf('[') + 1, changeType.length - 1);
                        }
                        if (changeType.indexOf(']') > -1) {
                            changeTypeValue = changeTypeValue = changeType.substring(0, changeType.indexOf(']') - 1);
                        }
                        if (changeTypeValue.indexOf(";") > -1) {
                            columnArray['ChangeType'] = changeTypeValue;
                        } else {
                            columnArray['ChangeType'] = changeTypeValue + ";" + changeTypeValue;
                        }
                    } else {
                        columnArray['ChangeType'] = changeType;
                    }
                }
                //add by lzy at 20200118 end
                // if (changeType.indexOf('[') > -1) {
                //     columnArray['ChangeType'] = changeType.substring(changeType.lastIndexOf('[') + 1, changeType.length - 1);
                // } else {
                //     columnArray['ChangeType'] = changeType;
                // }
            } else {
                columnArray['ChangeType'] = '';
            }
            if (tableRow.hasOwnProperty('ProductDispose')) {
                var productDispose = tableRow['ProductDispose'].gui.comparable;
                //add by lzy at 20200118 start
                var gui = JSON.stringify(tableRow['ProductDispose'].gui);
                var isEdit = gui.indexOf('disabled');//isEdit为-1表示不存在该字符，即是可编辑状态
                if (productDispose_result != null && productDispose_result != "" && isEdit == -1) {
                    columnArray['ProductDispose'] = productDispose_result;
                } else {
                    if (productDispose.indexOf('[') > -1) {
                        productDispose = productDispose.substring(productDispose.lastIndexOf('[') + 1, productDispose.length - 1);
                    }
                    if (productDispose.indexOf(']') > -1) {
                        productDispose = productDispose.substring(0, productDispose.indexOf(']') - 1);
                    }
                    columnArray['ProductDispose'] = productDispose;

                }
                //add by lzy at 20200118 end
                // if (productDispose.indexOf('[') > -1) {
                //     columnArray['ProductDispose'] = productDispose.substring(productDispose.lastIndexOf('[') + 1, productDispose.length - 1);
                // } else {
                //     columnArray['ProductDispose'] = productDispose;
                // }
            } else {
                columnArray['ProductDispose'] = '';
            }

            //add by tongwang 20191023 start
            if (tableRow.hasOwnProperty('ChangeObjectType')) {
                var changeObjectType = tableRow['ChangeObjectType'].gui.comparable;
                if (changeObjectType.indexOf('[') > -1) {
                    changeObjectType = changeObjectType.substring(changeObjectType.lastIndexOf('[') + 1, changeObjectType.length - 1);
                }
                if (changeObjectType.indexOf(']') > -1) {
                    changeObjectType = changeObjectType.substring(0, changeObjectType.indexOf(']') - 1);
                }
                columnArray['ChangeObjectType'] = changeObjectType;
            } else {
                columnArray['ChangeObjectType'] = '';
            }
            columnArray['CollectionNumber'] = tableRow.CollectionNumber.gui.comparable;
            //add by tongwang 20191023 end

            tableRowArry[i] = columnArray;
        }

        document.getElementById("changeTaskArray").value = JSON.stringify(tableRowArry);
        // 受影响对象表单中'责任人'，'期望完成时间',受影响对象表单中'在制处理措施','在途处理措施'，'库存处理措施'，'已出货成品处理措施'，'类型'回填
        setTimeout(function () {
            responsiblePersonWriteBack();
            completionTimeWriteBack();
            disposeWriteBack();
            aadDescriptionWriteBack();
        }, 50);

    }

    // 受影响对象表单中'期望完成时间'回填
    function completionTimeWriteBack() {
        if (document.getElementById("changeTaskArray")) {
            // 获取页面所有input控件
            var inputFormArray = document.getElementsByTagName("input");
            // 获取原有数据
            var changeTaskArray = eval("(" + document.getElementById("changeTaskArray").value + ")");
            if (changeTaskArray.length > 0) {
                for (var j = 0; j < inputFormArray.length; j++) {
                    var inputForm = inputFormArray[j];
                    if (inputForm.type === 'text') {
                        if (inputForm.name.indexOf('CompletionTime') > -1) {
                            for (var i = 0; i < changeTaskArray.length; i++) {
                                var datasArray = changeTaskArray[i];
                                if (inputForm.name.indexOf(datasArray['oid']) > -1) {
                                    inputForm.value = datasArray['CompletionTime'];
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 受影响对象表单中'更改详细描述'回填
    function aadDescriptionWriteBack() {
        if (document.getElementById("changeTaskArray")) {
            // 获取页面所有input控件
            var inputFormArray = document.getElementsByTagName("input");
            // 获取原有数据
            var changeTaskArray = eval("(" + document.getElementById("changeTaskArray").value + ")");
            if (changeTaskArray.length > 0) {
                for (var j = 0; j < inputFormArray.length; j++) {
                    var inputForm = inputFormArray[j];
                    if (inputForm.type === 'text') {
                        if (inputForm.name.indexOf('aadDescription') > -1) {
                            for (var i = 0; i < changeTaskArray.length; i++) {
                                var datasArray = changeTaskArray[i];

                                if (inputForm.name.indexOf(datasArray['oid']) > -1) {
                                    inputForm.value = datasArray['aadDescription'];
                                    break;
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    // 受影响对象表单中'在制处理措施','在途处理措施'，'库存处理措施'，'已出货成品处理措施'，'类型'回填
    function disposeWriteBack() {
        if (document.getElementById("changeTaskArray")) {
            // 获取页面所有select控件
            var inputFormArray = document.getElementsByTagName("select");
            // 获取原有数据
            var changeTaskArray = eval("(" + document.getElementById("changeTaskArray").value + ")");
            if (changeTaskArray.length > 0) {
                for (var j = 0; j < inputFormArray.length; j++) {
                    var inputForm = inputFormArray[j];
                    if (inputForm.name.indexOf('ArticleDispose') > -1) {
                        for (var i = 0; i < changeTaskArray.length; i++) {
                            var datasArray = changeTaskArray[i];
                            if (inputForm.name.indexOf(datasArray['oid']) > -1) {
                                inputForm.value = datasArray['ArticleDispose'];
                                break;
                            }
                        }
                    } else if (inputForm.name.indexOf('PassageDispose') > -1) {
                        for (var i = 0; i < changeTaskArray.length; i++) {
                            var datasArray = changeTaskArray[i];
                            if (inputForm.name.indexOf(datasArray['oid']) > -1) {
                                inputForm.value = datasArray['PassageDispose'];
                                break;
                            }
                        }
                    } else if (inputForm.name.indexOf('InventoryDispose') > -1) {
                        for (var i = 0; i < changeTaskArray.length; i++) {
                            var datasArray = changeTaskArray[i];
                            if (inputForm.name.indexOf(datasArray['oid']) > -1) {
                                inputForm.value = datasArray['InventoryDispose'];
                                break;
                            }
                        }
                    } else if (inputForm.name.indexOf('ProductDispose') > -1) {
                        for (var i = 0; i < changeTaskArray.length; i++) {
                            var datasArray = changeTaskArray[i];
                            if (inputForm.name.indexOf(datasArray['oid']) > -1) {
                                inputForm.value = datasArray['ProductDispose'];
                                break;
                            }
                        }
                    } else if (inputForm.name.indexOf('ChangeType') > -1) {
                        for (var i = 0; i < changeTaskArray.length; i++) {
                            var datasArray = changeTaskArray[i];
                            if (inputForm.name.indexOf(datasArray['oid']) > -1) {
                                inputForm.value = datasArray['ChangeType'];
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    //日期格式转换 转换成yyyy-MM-dd
    function date(date) {
        var nowdate = date.substring(0, 10);
        return nowdate;
    }

</SCRIPT>

<%@ include file="/netmarkets/jsp/util/end.jspf" %>
