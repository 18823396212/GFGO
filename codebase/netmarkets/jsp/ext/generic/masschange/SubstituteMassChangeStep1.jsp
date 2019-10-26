<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="jca"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/wrappers" prefix="w"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/core" prefix="wc"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="wctags" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/picker" prefix="p"%>
<%@page import="org.apache.commons.lang.exception.ExceptionUtils"%>
<%@page import="com.ptc.core.components.util.RequiredAttributeException"%>
<%@ page import="com.ptc.netmarkets.util.beans.NmCommandBean,
                 com.ptc.netmarkets.model.NmOid,
				 com.ptc.netmarkets.lifecycle.NmLifeCycleHelper,
				 com.ptc.core.components.rendering.guicomponents.ComboBox,				 				 
				 com.ptc.netmarkets.util.beans.NmLocaleBean,
				 com.ptc.netmarkets.util.misc.NmComboBox,
				 com.ptc.netmarkets.model.NmException,
				 com.ptc.netmarkets.util.beans.NmContextBean,
				 com.ptc.netmarkets.util.beans.NmURLFactoryBean,
				 java.util.ResourceBundle,				 
				 wt.lifecycle.LifeCycleManaged,
				 wt.lifecycle.State,
				 wt.inf.container.WTContainerRef,
				 wt.inf.container.WTContained,
				 wt.util.HTMLEncoder,				 
				 java.util.ArrayList,
				 ext.generic.masschange.resource.MassChangeResource,
				 wt.util.WTMessage,
				 com.ptc.core.components.util.RequestHelper,
				 javax.servlet.http.*,
				 ext.com.core.CoreUtil,
				 wt.part.WTPart,
				 ext.generic.masschange.util.MassChangeUtil"			 
%>

<fmt:setLocale value="${localeBean.locale}"/>
<fmt:setBundle basename="ext.generic.masschange.resource.MassChangeResource"/>
	
<fmt:message var="SUBSTITUTE_MASSCHANGE_TYPE" key="SUBSTITUTE_MASSCHANGE_TYPE" />
	
<jsp:useBean id="localeBean" class="com.ptc.netmarkets.util.beans.NmLocaleBean" scope="request"/>
<jsp:useBean id="nmcontext" class="com.ptc.netmarkets.util.beans.NmContextBean" scope="request"/>
<jsp:useBean id="urlFactoryBean" class="com.ptc.netmarkets.util.beans.NmURLFactoryBean" scope="request"/>

<%
  String SUBSTITUTE_MASSCHANGE_TYPE="MASSCHANGE_REPLACE_MAIN";
%>

<%
	ResourceBundle resource = ResourceBundle.getBundle("ext.generic.masschange.resource.MassChangeResource", localeBean.getLocale()); 
	NmComboBox combo = new NmComboBox();
	combo.setEnabled(true);
	ArrayList internal = new ArrayList();
	ArrayList display = new ArrayList();
	ArrayList selected = new ArrayList();
	internal.add("MASSCHANGE_REPLACE_MAIN");
	internal.add("MASSCHANGE_REPLACE_SUBSTITUTE");
	internal.add("MASSCHANGE_ADD_SUBSTITUTE");	
	internal.add("MASSCHANGE_REMOVE_SUBSTITUTE");
	
	display.add(resource.getString(MassChangeResource.MASSCHANGE_REPLACE_MAIN));
	display.add(resource.getString(MassChangeResource.MASSCHANGE_REPLACE_SUBSTITUTE));
	display.add(resource.getString(MassChangeResource.MASSCHANGE_ADD_SUBSTITUTE));	
	display.add(resource.getString(MassChangeResource.MASSCHANGE_REMOVE_SUBSTITUTE));
	
	selected.add(resource.getString(SUBSTITUTE_MASSCHANGE_TYPE));
  combo.setInternalValues(internal);
  combo.setValues(display);
%>

	<c:set var="iVals" value="<%=combo.getInternalValues()%>"/>
	<c:set var="dVals" value="<%=combo.getValues()%>"/>	
	<c:set var="sVals" value="<%=selected%>"/>
		
<jca:renderPropertyPanel>
	<w:comboBox propertyLabel="${SUBSTITUTE_MASSCHANGE_TYPE}" id="SUBSTITUTE_MASSCHANGE_TYPE" name="SUBSTITUTE_MASSCHANGE_TYPE" internalValues="${iVals}"  displayValues="${dVals}" selectedValues="${sVals}" onchange="changeWizardStep(this)"/>
	<jca:addSeparator/>
</jca:renderPropertyPanel>


<script language="javascript">
	function changeWizardStep(theElement) {
		if (window.document.getElementById("WIZARDTYPE") &&
        window.document.getElementById("WIZARDTYPE").value == "wizard") { // Dynamic steps are not applicable for clerk
        if (theElement.value === "MASSCHANGE_REPLACE_MAIN") {
            insertStep("SubstituteMassChangeStep2");
            if (typeof wizardSteps['SubstituteMassChangeStep3'] !== 'undefined') {
                removeStep("SubstituteMassChangeStep3");
            }
            if (typeof wizardSteps['SubstituteMassChangeStep4'] !== 'undefined') {
                removeStep("SubstituteMassChangeStep4");
            }
            if (typeof wizardSteps['SubstituteMassChangeStep5'] !== 'undefined') {
                removeStep("SubstituteMassChangeStep5");
            }
        }
        if (theElement.value === "MASSCHANGE_REPLACE_SUBSTITUTE") {
            insertStep("SubstituteMassChangeStep3");
            if (typeof wizardSteps['SubstituteMassChangeStep2'] !== 'undefined') {
                removeStep("SubstituteMassChangeStep2");
            }
            if (typeof wizardSteps['SubstituteMassChangeStep4'] !== 'undefined') {
                removeStep("SubstituteMassChangeStep4");
            }
            if (typeof wizardSteps['SubstituteMassChangeStep5'] !== 'undefined') {
                removeStep("SubstituteMassChangeStep5");
            }
        }
        if (theElement.value === "MASSCHANGE_ADD_SUBSTITUTE") {
            insertStep("SubstituteMassChangeStep4");
            if (typeof wizardSteps['SubstituteMassChangeStep2'] !== 'undefined') {
                removeStep("SubstituteMassChangeStep2");
            }
            if (typeof wizardSteps['SubstituteMassChangeStep3'] !== 'undefined') {
                removeStep("SubstituteMassChangeStep3");
            }
            if (typeof wizardSteps['SubstituteMassChangeStep5'] !== 'undefined') {
                removeStep("SubstituteMassChangeStep5");
            }
        }
        if (theElement.value === "MASSCHANGE_REMOVE_SUBSTITUTE") {
            insertStep("SubstituteMassChangeStep5");
            if (typeof wizardSteps['SubstituteMassChangeStep2'] !== 'undefined') {
                removeStep("SubstituteMassChangeStep2");
            }
            if (typeof wizardSteps['SubstituteMassChangeStep3'] !== 'undefined') {
                removeStep("SubstituteMassChangeStep3");
            }
            if (typeof wizardSteps['SubstituteMassChangeStep4'] !== 'undefined') {
                removeStep("SubstituteMassChangeStep4");
            }
        }
    }
		
	}
	
</script>
	