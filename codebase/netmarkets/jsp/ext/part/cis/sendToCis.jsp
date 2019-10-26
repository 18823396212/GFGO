<%@ taglib prefix="jca" uri="http://www.ptc.com/windchill/taglib/components"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>

<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf"%>
<fmt:setLocale value="${localeBean.locale}"/>
<fmt:setBundle basename="ext.generic.integration.cis.resource.CisActionsRB"/>
<fmt:message var="TITLE" key="CIS_SENDTOCIS_DESC"/>


<jca:wizard title="${TITLE}" buttonList="DefaultWizardButtonsNoApply">

	 <jca:wizardStep action="sendToCis_step" type="cis" />
	
</jca:wizard>


<%@include file="/netmarkets/jsp/util/end.jspf"%>