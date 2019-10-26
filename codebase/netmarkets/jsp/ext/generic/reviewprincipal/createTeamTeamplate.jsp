<%@page pageEncoding="UTF-8"%>
<%@ taglib prefix="jca" uri="http://www.ptc.com/windchill/taglib/components" %>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>

<fmt:setBundle basename="ext.generic.reviewprincipal.resource.WFTeamTemplateRB"/>
<fmt:message var="WFTEAM_CREATETEMPLATE_TITLE" key="WFTEAM_CREATETEMPLATE_TITLE" />

<jca:wizard title="${WFTEAM_CREATETEMPLATE_TITLE}" buttonList="DefaultWizardButtonsNoApply" wizardSelectedOnly="true">
    <jca:wizardStep action="createTeamTeamplate_step" type="wfteam" />
</jca:wizard>

<%@ include file="/netmarkets/jsp/util/end.jspf"%>