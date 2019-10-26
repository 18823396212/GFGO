<%@page pageEncoding="UTF-8"%>
<%@ taglib prefix="jca" uri="http://www.ptc.com/windchill/taglib/components" %>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>

<fmt:setBundle basename="ext.generic.generatenumber.rule.resource.NumberRuleRB"/>
<fmt:message var="NORULE_MERGEATTR_EDIT_TITLE" key="NORULE_MERGEATTR_EDIT_TITLE" />
<fmt:message var="MERGEATTR_STEP_BASEINFO" key="MERGEATTR_STEP_BASEINFO" />
<fmt:message var="MERGEATTR_STEP_MAINTAININFO" key="MERGEATTR_STEP_MAINTAININFO" />

<jca:wizard title="${NORULE_MERGEATTR_EDIT_TITLE}" buttonList="DefaultWizardButtonsNoApply" wizardSelectedOnly="true">
    <jca:wizardStep action="editMergeAttrStep1" type="cuscsm" label="${MERGEATTR_STEP_BASEINFO}"/>
	<jca:wizardStep action="editMergeAttrStep2" type="cuscsm" label="${MERGEATTR_STEP_MAINTAININFO}"/>
</jca:wizard>

<%@ include file="/netmarkets/jsp/util/end.jspf"%>