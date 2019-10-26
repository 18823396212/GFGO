<%@page pageEncoding="UTF-8"%>
<%@ taglib prefix="jca" uri="http://www.ptc.com/windchill/taglib/components" %>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>

<fmt:setBundle basename="ext.generic.generatenumber.rule.resource.NumberRuleRB"/>
<fmt:message var="ATTRRULE_ADD_TITLE" key="ATTRRULE_ADD_TITLE" />

<jca:wizard title="${ATTRRULE_ADD_TITLE}" buttonList="DefaultWizardButtonsNoApply" wizardSelectedOnly="true">
    <jca:wizardStep action="addAttrRuleStep" type="cuscsm" />
</jca:wizard>

<%@ include file="/netmarkets/jsp/util/end.jspf"%>