<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="wca"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>
<%@ page import="ext.generic.part.partResource" %>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf"%>
<fmt:setLocale value="${localeBean.locale}" />
<fmt:setBundle basename="ext.generic.part.partResource" />
<fmt:message var="wizardLabel"	key="<%= partResource.PASTE_OBJECT_TITLE %>" />
<jca:wizard title="${wizardLabel}" buttonList="NoStepsWizardButtons"> 
    <jca:wizardStep action="pastedocstep" type="partWorkFlow"/>
</jca:wizard>

<%@ include file="/netmarkets/jsp/util/end.jspf"%>