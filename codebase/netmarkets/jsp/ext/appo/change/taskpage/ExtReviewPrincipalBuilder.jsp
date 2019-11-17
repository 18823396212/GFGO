<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc" %>
<script>
    function reviewCheckOnchange(activityName, workItemOid, checkId) {
        var url;
        if (Ext.getDom(checkId).checked) {
            url = 'netmarkets/jsp/ext/generic/reviewprincipal/addActivity.jsp';
        } else {
            url = 'netmarkets/jsp/ext/generic/reviewprincipal/deleteActivity.jsp';
        }
        Ext.Ajax.request({
            url: url,
            params: {
                activityName: activityName,
                workItemOid: workItemOid
            },
            success: function (response, options) {
                varresultStr = trim(response.responseText);
            }
        });
    }
</script>
<jsp:include page="${mvc:getComponentURL('ext.appo.change.mvc.builder.ExtReviewPrincipalTableBuilder')}" flush="true"/>