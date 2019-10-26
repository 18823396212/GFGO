<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="wca"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf"%>

<fmt:setLocale value="${localeBean.locale}" />
<fmt:setBundle basename="ext.generic.reviewObject.resource.ReviewObjectResourceRB" />
<fmt:message var="wizardLabel"	key="ADDDATASHEET" />

<jca:wizard buttonList="addDatasheetButtons" title="${wizardLabel}">
    <jca:wizardStep action="addDatasheetStep" type="reviewObject" label="${tableLabel}"/>
</jca:wizard>

<%@include file="/netmarkets/jsp/util/end.jspf"%>