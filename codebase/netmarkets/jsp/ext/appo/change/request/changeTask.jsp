<%@ page import="com.ptc.netmarkets.util.beans.NmCommandBean" %>
<%@ page import="wt.fc.Persistable" %>
<%@ page import="wt.fc.PersistenceHelper" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc" %>
<jsp:include page="${mvc:getComponentURL('ext.appo.change.mvc.builder.TransactionalTaskTableBuilder')}" flush="true"/>
<input type="hidden" name="datasArray" id="datasArray"/>

<%
    NmCommandBean nmCommandBean = new NmCommandBean();
    nmCommandBean.setCompContext(nmcontext.getContext().toString());
    nmCommandBean.setRequest(request);
    Object object = nmCommandBean.getPageOid().getRefObject();
    String oid = object.toString();
    if (object instanceof Persistable) {
        oid = PersistenceHelper.getObjectIdentifier((Persistable) object).toString();
    }
%>

<script>
    function CusPickerInputComponentCallback(objects, pickerID, attr, displayFieldId) {
        var updateHiddenIDField = document.getElementById(pickerID);
        var updateHiddenField = document.getElementsByName(pickerID)[0];
        var updateDisplayField = document.getElementsByName(displayFieldId)[0];

        var myJSONObjects = objects.pickedObject;
        for (var i = 0; i < myJSONObjects.length; i++) {
            var oid = myJSONObjects[i].typeInstanceId;
            var displayAttr = myJSONObjects[i][attr];
            if (pickerID.indexOf('IBA|ResponsiblePerson') > -1) {
                // 获取页面所有input控件
                var inputFormArray = document.getElementsByTagName("input");
                // 表单
                var storeTable = PTC.jca.table.Utils.getTable('changeTask_affectedItems_table').getStore();
                for (var i = 0; i < storeTable.getCount(); i++) {
                    var row = storeTable.getAt(i);
                    // 列ID
                    var rowID = getOidFromRowValue(row.id);
                    if (displayFieldId.indexOf(rowID) != -1) {
                        // 获取'责任人'
                        var responsiblePerson = row.get('IBA|ResponsiblePerson');
                        // '责任人'属性值
                        var responsiblePersonValue = responsiblePerson.gui.comparable;
                        if (responsiblePersonValue.length == 0) {
                            for (var j = 0; j < inputFormArray.length; j++) {
                                var inputForm = inputFormArray[j];
                                if (inputForm.type == 'text') {
                                    if ((inputForm.id.indexOf('IBA|ResponsiblePerson') > -1) && (inputForm.id.indexOf(rowID) > -1)) {
                                        responsiblePersonValue = inputForm.value;
                                        break;
                                    }
                                }
                            }
                        }
                        var agoHtml = new RegExp("value=\"" + responsiblePersonValue + "\"", "g");
                        responsiblePerson.gui.comparable = displayAttr;
                        var rowHtml = responsiblePerson.gui.html;
                        responsiblePerson.gui.html = rowHtml.replace(agoHtml, "value=\"" + displayAttr + "\"");
                        row.store.afterCommit(responsiblePerson);
                    }
                }
            }
            updateHiddenField.value = oid;
            updateDisplayField.value = displayAttr;
            updateHiddenIDField.value = displayAttr;
        }
        if (pickerID.indexOf('IBA|ResponsiblePerson') > -1) {
            saveChangeTaskArray();
        } else {
            saveDatasArray();
        }
    }

    var table_id = "ext.appo.change.mvc.builder.TransactionalTaskTableBuilder";

    // 添加数据至事务性任务
    function addChangeTask() {
        saveDatasArray();

        var url1 = "netmarkets/jsp/ext/appo/change/request/newTransactionTask.jsp";
        var lineID = ajaxRequest(url1, {oid: "<%=oid%>"});

        // 获取事务性表单数据
        var datasArray = eval("(" + document.getElementById("datasArray").value + ")");
        // 构建列基本数据
        //var url2 = "netmarkets/jsp/ext/appo/changeNotice/createTransactionalChangeActivity2.jsp";
        //var lineID = trim1(ajaxRequest(url2, ""));
        datasArray[lineID] = {'changeTheme': '', 'changeDescribe': '', 'responsible': '', 'changeActivity2': '', 'needDate': ''};
        // 添加列
        reloadTable(JSON.stringify(datasArray));
    }

    // 移除事务性任务
    function deleteChangeTask() {
        var selectRows = PTC.jca.table.Utils.getTableSelectedRowsById(table_id, false, false);
        if (selectRows.length === 0) {
            alert("至少选择一项进行删除!");
            return;
        }
        var oidArray = [];
        for (var i = 0; i < selectRows.length; i++) {
            oidArray.push(getOidFromRowValue(selectRows[i].value));
        }
        //移除Link
        var oid = "<%=request.getParameter("oid")%>";
        var params = "oid=" + oid + "&selectOid=" + JSON.stringify(oidArray);
        var url = "netmarkets/jsp/ext/appo/change/request/removeChangeTaskLink.jsp";
        var removeVid = eval("(" + ajaxRequest(url, params) + ")");
        // 移除数据
        var table = PTC.jca.table.Utils.getTable(table_id);
        PTC.jca.table.Utils.removeRows(table, removeVid);
        // 保存数据
        delaySaveDatasArray();
    }

    // 去除空格
    function trim1(str) {
        return str.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
    }

    // 重新加载 ChangeTaskTableBuilder
    function reloadTable(param) {
        // 保存数据
        document.getElementById("datasArray").value = param;
        PTC.jca.table.Utils.reload(table_id, {changeTaskBeanID: param}, true);
    }

    function delaySaveDatasArray() {
        setTimeout("saveDatasArray()", 100);
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
            allDatas[record.data.oid] = json;
        });
        document.getElementById("datasArray").value = JSON.stringify(allDatas);
    }

    PTC.onReady(function () {
        setTimeout("saveDatasArray()", 1000);
    });

    setUserSubmitFunction(saveDatasArray);
</script>