<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="wca"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>

<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf"%>

<script type="text/javascript" src="netmarkets/jsp/ext/generic/masschange/javascript/addPart.js"></script>

<script type="text/javascript">
	//Ext.EventManager.on(window, 'unload',  clearValues);
	if (window.addEventListener)
      window.addEventListener('beforeunload', clearSubstituteValue, false);
    else if (window.attachEvent)
      window.attachEvent('onbeforeunload', clearSubstituteValue);
</script>

<fmt:setLocale value="${localeBean.locale}"/>
<fmt:setBundle basename="ext.generic.masschange.resource.MassChangeResource"/>

<fmt:message var="SUBSTITUTE_MASSCHANGE_TITLE" key="SUBSTITUTE_MASSCHANGE_TITLE" />
<fmt:message var="SUBSTITUTE_TYPE" key="SUBSTITUTE_TYPE" />
<fmt:message var="SUBSTITUTE_OPERATION" key="SUBSTITUTE_OPERATION" />

<wca:wizard title="${SUBSTITUTE_MASSCHANGE_TITLE}"  buttonList="DefaultWizardButtons" >
	<wca:wizardStep action="SubstituteMassChangeStep1" type="masschange" label="${SUBSTITUTE_TYPE}"/>
	<wca:wizardStep action="SubstituteMassChangeStep2" type="masschange" label="${SUBSTITUTE_OPERATION}"/>
	<wca:wizardStep action="SubstituteMassChangeStep3" type="masschange" label="${SUBSTITUTE_OPERATION}"/>
	<wca:wizardStep action="SubstituteMassChangeStep4" type="masschange" label="${SUBSTITUTE_OPERATION}"/>
	<wca:wizardStep action="SubstituteMassChangeStep5" type="masschange" label="${SUBSTITUTE_OPERATION}"/>
</wca:wizard>

<style type="text/css">
<!--
.redColor {color: #FF0000}
-->
</style>

<%@include file="/netmarkets/jsp/util/end.jspf"%>