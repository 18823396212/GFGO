<%@page pageEncoding="UTF-8"%>
<%@ taglib prefix="jca" uri="http://www.ptc.com/windchill/taglib/components"%>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf"%>
<%@ taglib prefix="fmt" uri="http://www.ptc.com/windchill/taglib/fmt"%>

<fmt:setBundle basename="ext.appo.part.resource.PartResourceRB"/>
<fmt:message var="Title" key="partStruct.addrole.tooltip" />

<jca:wizard buttonList="DefaultWizardButtonsNoApply" title="${Title}">
    <jca:wizardStep action="addroleWizardStep" type="partStruct" label="${Title}" />
</jca:wizard>
<%@ include file="/netmarkets/jsp/util/end.jspf"%>