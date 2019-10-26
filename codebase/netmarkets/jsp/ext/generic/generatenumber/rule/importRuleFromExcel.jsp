<%@page pageEncoding="UTF-8"%>
<%@ taglib prefix="jca" uri="http://www.ptc.com/windchill/taglib/components" %>

<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>

<fmt:setBundle basename="ext.generic.generatenumber.rule.resource.NumberRuleRB"/>

<fmt:message var="NORULE_IMPORTRULE_TITLE" key="NORULE_IMPORTRULE_TITLE" />

<jca:wizard title="${NORULE_IMPORTRULE_TITLE}"  buttonList="DefaultWizardButtonsNoApply">    
	<jca:wizardStep action="importRuleStep" type="cuscsm"/>
</jca:wizard>


<%@ include file="/netmarkets/jsp/util/end.jspf"%>