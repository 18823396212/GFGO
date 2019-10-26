<%@ page import="ext.generic.changephase.resource.changePhaseResource"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="jca"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>

<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>

<%--> @COMMENTS Build the localized display messages<--%>
<fmt:setLocale value="${localeBean.locale}"/>
<fmt:setBundle basename="ext.generic.changephase.resource.changePhaseResource" />
<fmt:message var="PART_CHANGEPHASE_TITLE" key="PART_CHANGEPHASE_TITLE"/>

<jca:wizard title="${PART_CHANGEPHASE_TITLE}" buttonList="DefaultWizardButtonsNoApply">
	<jca:wizardStep action="changePhase_step" type="part"
    	label="${PART_CHANGEPHASE_TITLE}"/>   
</jca:wizard>

<%@ include file="/netmarkets/jsp/util/end.jspf"%>