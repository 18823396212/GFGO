<%@page import="com.ptc.netmarkets.model.NmOid" %>
<%@page import="com.ptc.netmarkets.util.beans.NmCommandBean" %>
<%@page pageEncoding="UTF-8" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="jca" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/jcaMvc" prefix="jcaMvc" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/wrappers" prefix="w" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ include file="/netmarkets/jsp/util/begin.jspf" %>
<%@page import="java.util.List" %>

<%
    NmCommandBean nmCommandBean = (NmCommandBean) request.getAttribute("commandBean");
    List selectedObject = nmCommandBean.getSelectedOidForPopup();
    if ((selectedObject == null) || (selectedObject.size() <= 0)) {
        selectedObject = nmCommandBean.getSelectedInOpener();
    }
    String selectOid = "";
    if (selectedObject != null) {
        for (Object obj : selectedObject) {
            if (obj != null) {
                NmOid oid = (NmOid) obj;
                if (selectOid.trim().isEmpty()) {
                    selectOid = oid.toString();
                } else {
                    selectOid = selectOid + ";" + oid.toString();
                }
            }
        }
    }
%>

<jsp:include page="${mvc:getComponentURL('ext.appo.ecn.mvc.builder.CollectItemsTableBuilder')}">
    <jsp:param name="selectOid" value="<%=selectOid%>"/>
</jsp:include>

<script language="JavaScript">
    function initializeTable() {
        var selectOid = '<%=selectOid%>';
        if (selectOid.length == 0) {
            alert("未作任何选择");
            window.close();
        }
    }

    var table_id = 'ext.appo.ecn.mvc.builder.CollectItemsTableBuilder';

    function okButton() {
        var selectRows = PTC.jca.table.Utils.getTableSelectedRowsById(table_id, false, false);
        if (selectRows.length == 0) {
            alert("至少选择一项进行删除!");
            return;
        }
        var itemsOid = [];
        for (var i = 0; i < selectRows.length; i++) {
            var row = selectRows[i];
            itemsOid[i] = getOidFromRowValue(row.value);
        }
        window.opener.addCollectItemsForAffectedEndItems(itemsOid);
        window.close();
    }

    PTC.onReady(function () {
        initializeTable();
    });
</script>

<%@ include file="/netmarkets/jsp/util/end.jspf" %>