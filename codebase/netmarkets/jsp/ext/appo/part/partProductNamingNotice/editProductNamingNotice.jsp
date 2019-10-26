<%@ taglib prefix="jca" uri="http://www.ptc.com/windchill/taglib/components"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>

<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf"%>
<fmt:setLocale value="${localeBean.locale}"/>
<fmt:setBundle basename="ext.appo.part.resource.PartResourceRB"/>
<fmt:message var="TITLE" key="PRIVATE_CONSTANT_26"/>


<jca:wizard title="${TITLE}" buttonList="DefaultWizardButtonsNoApply">

	 <jca:wizardStep action="editProductNamingNotice_step" type="partProductNamingNotice" />
	
</jca:wizard>

<%@include file="/netmarkets/jsp/util/end.jspf"%>