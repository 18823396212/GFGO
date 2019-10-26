<%@ taglib prefix="jca" uri="http://www.ptc.com/windchill/taglib/components" %>

<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>

<fmt:setBundle basename="ext.generic.esignature.esignatureResource"/>
<fmt:message var="WizStepLabel" key="esignature.createESignFile.title" />

<jca:wizard title="${defineDocWizStepLabel}"  buttonList="DefaultClerkButtonsNoApply">    
<jca:wizardStep action="createESignFileWiz_step" type="esignature" />
</jca:wizard>


<%@ include file="/netmarkets/jsp/util/end.jspf"%>