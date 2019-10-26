<%@ taglib prefix="jca" uri="http://www.ptc.com/windchill/taglib/components"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>
<%@ taglib prefix="wctags" tagdir="/WEB-INF/tags" %>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf"%>
<fmt:setBundle basename="ext.generic.reviewObject.resource.ReviewObjectResourceRB"/>
<fmt:message var="tableLabel" key="REMOVE_SELECTED_REVIEWOBJS"/>

<jca:wizard  buttonList="DefaultWizardButtonsNoApply"  title="${tableLabel}">
	    <jca:wizardStep action="removeReviewObjectStep" type="reviewObject" />
</jca:wizard>

<%@include file="/netmarkets/jsp/util/end.jspf"%>