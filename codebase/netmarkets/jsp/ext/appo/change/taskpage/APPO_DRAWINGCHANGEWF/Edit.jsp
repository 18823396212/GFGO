<%@page pageEncoding="UTF-8"%>
<%@ include file="/netmarkets/jsp/util/begin.jspf"%>
<%@ include file="/netmarkets/jsp/ext/appo/change/taskpage/ExtWorkflowStepGuide.jsp"%>
<%@ include file="/netmarkets/jsp/ext/appo/change/taskpage/ExtReviewPrincipalBuilder.jsp"%>
<jsp:include page="${mvc:getComponentURL('ext.appo.change.mvc.builder.ResultingItemsTableBuilder')}" flush="true"/>
<jsp:include page="${mvc:getComponentURL('ext.appo.change.mvc.builder.AffectedItemsTableBuilder')}" flush="true"/>

<%
    //取路径
    wt.httpgw.URLFactory urlFactory = new wt.httpgw.URLFactory();
    String baseUrl = urlFactory.getBaseHREF();
%>
<input type="hidden" id="baseUrl" value="<%=baseUrl%>">
<SCRIPT LANGUAGE="JavaScript">
    // 产生对象列表‘收集图纸’按钮调用
    function collectDrawing(){
        var selectRows = PTC.jca.table.Utils.getTableSelectedRowsById('ext.appo.change.mvc.builder.ResultingItemsTableBuilder', false, false);
        if(selectRows.length == 0){
            alert("至少选择一条数据进行收集图纸操作!") ;
            return ;
        }
        for(var i = 0; i < selectRows.length ; i++){
            //文档不能收集图纸
            if (selectRows[i].value.indexOf("wt.doc.WTDocument")>-1){
                alert("文档不能进行收集图纸操作!") ;
                return ;
            }
        }

        var result=confirm("确定要收集图纸吗?");
        if (result==false) return;

        var oidArray = [];
        for(var i = 0; i < selectRows.length ; i++){
            oidArray.push(getOidFromRowValue(selectRows[i].value));
        }

        if(oidArray.length > 0){
            // 检查选择对象是否符合要求
            var baseUrl=document.getElementById("baseUrl").value;
            var params = "itemOid=" + JSON.stringify(oidArray);
            var url = baseUrl+"/netmarkets/jsp/ext/appo/change/request/collectDrawing.jsp";
            var json = eval("("+ajaxRequest(url, params)+")");
            // 数据反填
            reloadResultingItemsTable(json['resultDatas']) ;
        }

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

    // 重新加载 ResultingItemsTableBuilder
    function reloadResultingItemsTable(param) {
        // 重新加载数据表
        PTC.jca.table.Utils.reload('ext.appo.change.mvc.builder.ResultingItemsTableBuilder', {selectOids: param.toString()}, true);
    }


    // 产生对象列表‘移除图纸’按钮调用
    function removeDrawing(){
        // 移除数据
        var selectRows = PTC.jca.table.Utils.getTableSelectedRowsById('ext.appo.change.mvc.builder.ResultingItemsTableBuilder', false, false);
        if(selectRows.length == 0){
            alert("至少选择一项进行删除!") ;
            return ;
        }
        for(var i = 0; i < selectRows.length ; i++){
            //不能移除部件
            if (selectRows[i].value.indexOf("wt.part.WTPart")>-1){
                alert("不能移除部件!") ;
                return ;
            }
        }

        var result=confirm("确定要移除图纸吗?");
        if (result==false) return;

        var oidArray = [];
        for(var i = 0; i < selectRows.length ; i++){
            oidArray.push(getOidFromRowValue(selectRows[i].value));
        }

        if(oidArray.length > 0){
            // 重新加载数据表
            PTC.jca.table.Utils.reload('ext.appo.change.mvc.builder.ResultingItemsTableBuilder', {deleteOids:JSON.stringify(oidArray)}, true);
        }

    }

</SCRIPT>

<%@ include file="/netmarkets/jsp/util/end.jspf"%>
