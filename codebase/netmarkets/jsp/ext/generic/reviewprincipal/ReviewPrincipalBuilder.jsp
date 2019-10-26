  <%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>
<script>
	function reviewCheckOnchange(activityName,workItemOid,checkId){
		var url;
		
		if(Ext.getDom(checkId).checked){
			url = 'netmarkets/jsp/ext/generic/reviewprincipal/addActivity.jsp';
		}else{
			url = 'netmarkets/jsp/ext/generic/reviewprincipal/deleteActivity.jsp';
		}
		Ext.Ajax.request({
				url: url,
				params: {
				activityName:activityName,
				workItemOid:workItemOid
				    },
				success: function(response, options) {
				varresultStr =trim(response.responseText); 	     
	   }
	  });
	 
	 // PTC.jca.table.Utils.reload("ReviewPrincipalTable", null, true);
	}
	</script>
  <jsp:include page="${mvc:getComponentURL('ext.generic.reviewprincipal.builder')}" flush="true"/>