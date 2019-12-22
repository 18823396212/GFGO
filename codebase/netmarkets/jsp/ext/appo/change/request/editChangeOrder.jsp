<%@ taglib prefix="jca" uri="http://www.ptc.com/windchill/taglib/components" %>
<%@ taglib prefix="cwiz" uri="http://www.ptc.com/windchill/taglib/changeWizards" %>
<%@ taglib prefix="rwiz" uri="http://www.ptc.com/windchill/taglib/reservation" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/attachments" prefix="attachments" %>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf" %>
<%@ taglib prefix="fmt" uri="http://www.ptc.com/windchill/taglib/fmt" %>
<%@include file="/netmarkets/jsp/components/includeWizBean.jspf" %>

<jca:initializeItem operation="${createBean.edit}"/>

<script type="text/javascript" src="netmarkets/javascript/change2/changeUtil.js"></script>

<%@include file="/netmarkets/jsp/change/changeWizardConfig.jspf" %>
<%@include file="/netmarkets/jsp/attachments/initAttachments.jspf" %>

<cwiz:initializeChangeWizard changeMode="EDIT" annotationUIContext="change" changeItemClass="wt.change2.ChangeOrderIfc"/>

<SCRIPT LANGUAGE="JavaScript">
    var storeIframes = true;
    var iframeTableId = "changeNotice.wizardImplementationPlan.table";
    var changeNotice = true;
    PTC.wizardIframes.initStoreIframes();
</SCRIPT>

<fmt:setBundle basename="ext.appo.ecn.resource.changeNoticeActionsRB"/>
<fmt:message var="editTitle" key="changeNotice.editEngineeringChangeOrder.title"/>

<!-- buttonList="CustomWizardButtonsWithSubmitPrompt" -->
<jca:wizard helpSelectorKey="change_editChangeNotice" buttonList="DefaultWizardButtonsWithSubmitPrompt" formProcessorController="com.ptc.windchill.enterprise.change2.forms.controllers.EffectivityAwareIframeFormProcessorController" title="${editTitle}">
    <jca:wizardStep action="editChangeOrderStep" type="modify"/>
</jca:wizard>

<rwiz:handleUpdateCount/>
<rwiz:configureReservation reservationType="modify" enforcedByService="true" workflowOverride="true"/>

<script language='Javascript'>
    change_postLoad();
</script>

<%@ include file="/netmarkets/jsp/util/end.jspf" %>




