<%@ include file="/netmarkets/jsp/util/begin.jspf" %>
<%@ include file="/netmarkets/jsp/ext/generic/workflow/workflowStepGuide.jsp" %>
<%--<%@ include file="/netmarkets/jsp/ext/generic/reviewprincipal/ReviewPrincipalBuilder.jsp"%>--%>
<%@ include file="/netmarkets/jsp/ext/appo/change/taskpage/ExtReviewPrincipalBuilder.jsp" %>
<div id="mydiv">
    <%@ include file="/netmarkets/jsp/ext/appo/change/request/UsabilityChangeTaskTableBuilder.jsp" %>
</div>
<input type="hidden" id="cacheTaskArray" name="cacheTaskArray"/>
<%@ include file="/netmarkets/jsp/util/end.jspf" %>
<%
    //取路径
    wt.httpgw.URLFactory urlFactory = new wt.httpgw.URLFactory();
    String baseUrl = urlFactory.getBaseHREF();
    //通过oid获取流程项->流程
    String oid = request.getParameter("oid");
%>
<script type="text/javascript" src="https://code.jquery.com/jquery-3.1.1.min.js">
    //重新加载事务性任务jsp
    function startrefresh() {
        var url = "<%=baseUrl%>netmarkets/jsp/ext/appo/change/request/UsabilityChangeTaskTableBuilder.jsp?oid=<%=oid%>";
        jQuery.noConflict();//将变量$的控制权让渡给给其他插件或库
        jQuery(function () {//不在直接使用$，而是使用jQuery
            //兼容ie,不读取缓存
            jQuery.ajaxSetup ({ cache: false });
            jQuery("#mydiv").empty();
            jQuery("#mydiv").load(url);
        });
    }
</script>