<%@page pageEncoding="UTF-8"%>
<%@ taglib prefix="jca" uri="http://www.ptc.com/windchill/taglib/components" %>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>


<jca:wizard title="管理模板" buttonList="DefaultWizardButtonsNoApply" wizardSelectedOnly="true">
    <jca:wizardStep action="manageTeamTemplate_step" type="modify" />
</jca:wizard>

<%@ include file="/netmarkets/jsp/util/end.jspf"%>