<%@ page pageEncoding="UTF-8"%>
<%@page import="java.util.*"%>
<%@page import="wt.fc.Persistable"%>
<%@page import="com.ptc.netmarkets.model.NmOid"%>
<%@page import="wt.fc.ReferenceFactory"%>
<%@ taglib prefix="jca" uri="http://www.ptc.com/windchill/taglib/components" %>
<%@ taglib prefix="cwiz" uri="http://www.ptc.com/windchill/taglib/changeWizards" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/attachments" prefix="attachments"%>
<%@ taglib prefix="fmt" uri="http://www.ptc.com/windchill/taglib/fmt"%>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@include file="/netmarkets/jsp/components/includeWizBean.jspf"%> 
<%@include file="/netmarkets/jsp/change/propagationConfiguration.jspf"%>

<%

	if(commandBean.getSelectedOidForPopup().size()>0){
		%>
		<SCRIPT LANGUAGE="JavaScript">
			alert('不允许从文件夹选择变更对象!');
			window.open('','_self');
			window.close();
		</SCRIPT>
		<%
	}	
  
%>

<jca:initializeItem operation="${createBean.create}"  baseTypeName="WCTYPE|wt.change2.WTChangeOrder2|com.plm.DesignECN"
attributePopulatorClass="com.ptc.windchill.enterprise.change2.forms.populators.FlexibleChangeNoticeAttributePopulator" />

<%@include file="/netmarkets/jsp/change/changeWizardConfig.jspf"%>

<cwiz:initializeChangeWizard changeMode="CREATE" annotationUIContext="change" changeItemClass="wt.change2.ChangeOrderIfc" />
<cwiz:initializeSelectedItems />

<SCRIPT LANGUAGE="JavaScript">
    var storeIframes = true;
    var iframeTableId = "changeNotice.wizardImplementationPlan.table"; 
    var changeNotice = true;
    PTC.wizardIframes.initStoreIframes();
</SCRIPT>

<fmt:setBundle basename="ext.appo.ecn.resource.changeNoticeActionsRB"/>
<fmt:message var="createTitle" key="changeNotice.createDesignChangeOrder.title" />

<jca:wizard helpSelectorKey="change_createChangeNotice" buttonList="DefaultWizardButtonsWithSubmitPrompt" formProcessorController="com.ptc.windchill.enterprise.change2.forms.controllers.ChangeTaskTemplatedFormProcessorController" wizardSelectedOnly="true" title="${createTitle}">
	<%-->Create Change Notice<--%>
 	<jca:wizardStep action="setChangeContextWizStep" type="change"/>	
	<jca:wizardStep action="defineItemAttributesWizStep" type="object"/>
	<jca:wizardStep action="securityLabelStep" type="securityLabels"/>

	<jca:wizardStep action="affectedAndProductChangeTaskStep" type="changeNotice"/>
</jca:wizard>

<%@ include file="/netmarkets/jsp/util/end.jspf"%>