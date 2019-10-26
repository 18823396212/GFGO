<%@ taglib prefix="jca" uri="http://www.ptc.com/windchill/taglib/components"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>

<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf"%>
<fmt:setLocale value="${localeBean.locale}"/>
<fmt:setBundle basename="ext.generic.organization.resources.OrganizationRB"/>
<fmt:message var="CREATEORGNAME" key="createOrgName"/>

<jca:initializeItem operation="${createBean.create}" baseTypeName="WCTYPE|ext.generic.organization.models.MgtArchitecture" />
<%--  renders javascript to be used by the duplicate name validation on the attributes step --%>
   <docmgnt:validateNameJSTag/>  


<jca:wizard title="${CREATEORGNAME}" buttonList="DefaultWizardButtonsNoApply">

	<jca:wizardStep action="createWizardStep"  type="organization"/>
	
</jca:wizard>


<%@include file="/netmarkets/jsp/util/end.jspf"%>