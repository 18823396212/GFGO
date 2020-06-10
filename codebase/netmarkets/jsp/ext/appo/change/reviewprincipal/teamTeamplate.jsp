<%@page pageEncoding="UTF-8"%>
<%@ taglib prefix="jca" uri="http://www.ptc.com/windchill/taglib/components" %>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>

<fmt:setBundle basename="ext.generic.reviewprincipal.resource.WFTeamTemplateRB"/>
<fmt:message var="SELECT_WFTEAM_TEMPLATE" key="SELECT_WFTEAM_TEMPLATE" />

<jca:wizard title="${SELECT_WFTEAM_TEMPLATE}" buttonList="DefaultWizardButtonsNoApply" wizardSelectedOnly="true">
    <jca:wizardStep action="teamTeamplate_step" type="modify" />
</jca:wizard>

<%@ include file="/netmarkets/jsp/util/end.jspf"%>