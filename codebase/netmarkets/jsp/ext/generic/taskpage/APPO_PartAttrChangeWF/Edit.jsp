<%@page pageEncoding="UTF-8"%>
<%@ include file="/netmarkets/jsp/util/begin.jspf"%>
<%@ include file="/netmarkets/jsp/ext/generic/workflow/workflowStepGuide.jsp"%>
<%@ include file="/netmarkets/jsp/ext/generic/reviewprincipal/ReviewPrincipalBuilder.jsp"%>
<%@ include file="/netmarkets/jsp/ext/generic/reviewObject/PartAttrChangeReviewObject.jsp"%>

<SCRIPT LANGUAGE="JavaScript">
    // 随签对象列表‘添加’按钮调用
    function addItems(itemsOid){
        // 保存数据
        // saveChangeTaskArray();
        // alert("itemsOid=="+itemsOid);
        // 重新加载数据表
        PTC.jca.table.Utils.reload('ext.appo.part.builder.PartAttrChangeReviewObjectTableBuilder',{selectOids:JSON.stringify(itemsOid)},true);
    }

</SCRIPT>
<%@ include file="/netmarkets/jsp/util/end.jspf"%>
