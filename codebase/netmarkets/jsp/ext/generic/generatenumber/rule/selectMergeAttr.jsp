<%@ page pageEncoding="UTF-8"%>
<%@taglib prefix="jca" uri="http://www.ptc.com/windchill/taglib/components"%>
<%@include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@include file="/netmarkets/jsp/components/includeWizBean.jspf"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>
<fmt:setBundle basename="ext.generic.generatenumber.rule.resource.NORuleMessageRB"/>
<fmt:message var="SELECT_CONTROLLEDMERGEATTRS" key="SELECT_CONTROLLEDMERGEATTRS" />

<jca:wizard title="${SELECT_CONTROLLEDMERGEATTRS}">
    <jca:wizardStep action="selectMergeAttrStep" type="cuscsm"/>
</jca:wizard>

<%@include file="/netmarkets/jsp/util/end.jspf"%>