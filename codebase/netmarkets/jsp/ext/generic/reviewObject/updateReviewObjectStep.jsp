<%@ page import="wt.fc.ObjectIdentifier, 
				java.util.ResourceBundle" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="wca"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/wrappers" prefix="w"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>
<%@ include file="/netmarkets/jsp/util/begin.jspf"%>
<%@ include file="/netmarkets/jsp/components/standardAttributeConfigs.jspf"%>
<fmt:setLocale value="${localeBean.locale}"/>
<fmt:setBundle basename="ext.generic.reviewObject.resource.ReviewObjectResourceRB"/>
<fmt:message var="msg" key="UPDATE_SELECTED_REVIEWOBJS_MSG"/>
	
${msg}

<script type="text/javascript" language="javascript">
	
	//“≥√Ê≥ı º ±
	Ext.onReady(function() {
		onSubmit();
	});

</script>

<%@include file="/netmarkets/jsp/util/end.jspf"%>