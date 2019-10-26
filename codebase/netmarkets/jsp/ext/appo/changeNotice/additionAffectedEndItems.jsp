
<%@ taglib prefix="jca" uri="http://www.ptc.com/windchill/taglib/components"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf"%>

<fmt:setLocale value="${localeBean.locale}"/>
<fmt:setBundle basename="ext.appo.ecn.resource.changeNoticeActionsRB"/>
<fmt:message var="tableLabel" key="changeNotice.additionAffectedEndItems.title"/>

<jca:wizard  buttonList="affectedItemsWizardButtons" title="${tableLabel}">
    <jca:wizardStep action="additionAffectedEndItemsStep" type="changeNotice" label="${tableLabel}"/> 
</jca:wizard>

<%@include file="/netmarkets/jsp/util/end.jspf"%>
