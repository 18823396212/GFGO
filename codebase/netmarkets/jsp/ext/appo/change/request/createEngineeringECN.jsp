<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="jca" uri="http://www.ptc.com/windchill/taglib/components" %>
<%@ taglib prefix="cwiz" uri="http://www.ptc.com/windchill/taglib/changeWizards" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/attachments" prefix="attachments" %>
<%@ taglib prefix="fmt" uri="http://www.ptc.com/windchill/taglib/fmt" %>
<%@include file="/netmarkets/jsp/components/beginWizard.jspf" %>
<%@include file="/netmarkets/jsp/components/includeWizBean.jspf" %>
<%@include file="/netmarkets/jsp/change/propagationConfiguration.jspf" %>

<script type="text/javascript" src="netmarkets/javascript/change2/changeUtil.js"></script>

<jca:initializeItem operation="${createBean.create}" baseTypeName="WCTYPE|wt.change2.WTChangeOrder2" attributePopulatorClass="com.ptc.windchill.enterprise.change2.forms.populators.FlexibleChangeNoticeAttributePopulator"/>
<%@include file="/netmarkets/jsp/change/changeWizardConfig.jspf" %>

<cwiz:initializeChangeWizard changeMode="CREATE" annotationUIContext="change" changeItemClass="wt.change2.ChangeOrderIfc"/>
<cwiz:initializeSelectedItems/>

<SCRIPT LANGUAGE="JavaScript">
    var storeIframes = true;
    var iframeTableId = "changeNotice.wizardImplementationPlan.table";
    var changeNotice = true;
    PTC.wizardIframes.initStoreIframes();
</SCRIPT>

<fmt:setBundle basename="ext.appo.ecn.resource.changeNoticeActionsRB"/>
<fmt:message var="createTitle" key="changeNotice.createEngineeringChangeOrder.title"/>

<jca:wizard helpSelectorKey="change_createChangeNotice" buttonList="CustomWizardButtonsWithSubmitPrompt" formProcessorController="com.ptc.windchill.enterprise.change2.forms.controllers.ChangeTaskTemplatedFormProcessorController" wizardSelectedOnly="true" title="${createTitle}">
    <jca:wizardStep action="createEngineeringChangeOrderStep" type="changeNotice"/>
</jca:wizard>

<%@ include file="/netmarkets/jsp/util/end.jspf" %>