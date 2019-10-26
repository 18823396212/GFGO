<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="jca" %>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf" %>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt" %>
<%@ page pageEncoding="UTF-8" %>
<fmt:setLocale value="${localeBean.locale}"/>
<fmt:setBundle basename="ext.appo.ecn.resource.changeNoticeActionsRB"/>
<fmt:message var="PRIVATE_CONSTANT_71_TITLE" key="PRIVATE_CONSTANT_71"/>


<jca:wizard title="${PRIVATE_CONSTANT_71_TITLE}" buttonList="collectItemsWizardButtons">
    <jca:wizardStep action="collectItemsStep" type="changeNotice"/>
</jca:wizard>


<%@ include file="/netmarkets/jsp/util/end.jspf" %>
