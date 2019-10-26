<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="wca"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>

<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf"%>
<script type="text/javascript" src="netmarkets/jsp/ext/generic/masschange/javascript/addPart.js"></script>

<script type="text/javascript">
	//Ext.EventManager.on(window, 'unload', clearBomValue);
	if (window.addEventListener)
      window.addEventListener('beforeunload', clearBomValue, false);
    else if (window.attachEvent)
      window.attachEvent('onbeforeunload', clearBomValue);
</script>



<fmt:setLocale value="${localeBean.locale}"/>
<fmt:setBundle basename="ext.generic.masschange.resource.MassChangeResource"/>

<fmt:message var="BOM_MASSCHANGE_TITLE" key="BOM_MASSCHANGE_TITLE" />

<wca:wizard title="${BOM_MASSCHANGE_TITLE}"  buttonList="DefaultWizardButtons" >
	<wca:wizardStep action="BomMassChangeStep" type="masschange" label="Step1"/>
</wca:wizard>

<style type="text/css">
<!--
.redColor {color: #FF0000}
-->
</style>

<%@include file="/netmarkets/jsp/util/end.jspf"%>