<%@ page errorPage="/netmarkets/jsp/util/error.jsp"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="jca"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/wrappers" prefix="w"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page import="com.ptc.netmarkets.util.beans.NmCommandBean,
                 com.ptc.netmarkets.model.NmOid,
				 com.ptc.netmarkets.lifecycle.NmLifeCycleHelper,
				 com.ptc.core.components.rendering.guicomponents.ComboBox,				 				 
				 com.ptc.netmarkets.util.beans.NmLocaleBean,
				 com.ptc.netmarkets.util.misc.NmComboBox,
				 com.ptc.netmarkets.model.NmException,
				 java.util.ResourceBundle,
				 ext.generic.changephase.resource.changePhaseResource,		
				 ext.generic.changephase.util.ChangePhaseXMLConfigUtil,	 
				 wt.lifecycle.LifeCycleManaged,
				 wt.lifecycle.State,
				 wt.inf.container.WTContainerRef,
				 wt.inf.container.WTContained,	
				 java.util.List,
				 java.util.Map,	 
				 java.util.ArrayList,
				 ext.generic.changephase.bean.LifecycleChangeRuleBean,
				 java.util.Hashtable,
				 java.util.Iterator,
				 ext.generic.changephase.util.ChangePhaseWorkFlowUtil	"
				 			 
%>          


<jsp:useBean id="nmcontext" class="com.ptc.netmarkets.util.beans.NmContextBean" scope="request">
<jsp:setProperty name="nmcontext" property="portlet" param="portlet"/>
</jsp:useBean>
<jsp:useBean id="localeBean" class="com.ptc.netmarkets.util.beans.NmLocaleBean" scope="request"/>

<%--> @COMMENTS Build the localized display messages<--%>
<fmt:setLocale value="${localeBean.locale}"/>
<fmt:setBundle basename="ext.generic.changephase.resource.changePhaseResource" />
<fmt:message var="DESCRIPTION" key="DESCRIPTION"/>
<%!
  private static final String PART_CHANGEPHASE_STEP_RESOURCE = "ext.generic.changephase.resource.changePhaseResource";
%>

<%
		ResourceBundle lifecycleUpdateStepRb = ResourceBundle.getBundle(PART_CHANGEPHASE_STEP_RESOURCE, localeBean.getLocale());
		NmCommandBean cb = new NmCommandBean();
		cb.setCompContext(nmcontext.getContext().toString());
		cb.setRequest(request);
		NmOid lcOid = cb.getPrimaryOid();
		
		LifeCycleManaged plcOid = null;
		
		if( lcOid != null && lcOid.isA(LifeCycleManaged.class)){
		   plcOid = (LifeCycleManaged) lcOid.getRef();
		}
		LifeCycleManaged lcm = plcOid;
		ChangePhaseWorkFlowUtil changePhaseWorkFlowUtil = new ChangePhaseWorkFlowUtil();
		NmComboBox combo = changePhaseWorkFlowUtil.generaterNmComboBox(lcOid);
		

%>
<c:choose>
	<c:when test="<%=(combo==null)%>">
		<c:out value="${OBJECTS_SETSTATE_NOTVALID}"/>   
	</c:when>	
	<c:otherwise>
	<%
	WTContainerRef contRef = null;
	if (lcm instanceof WTContained)
	{
	   contRef = ((WTContained) lcm).getContainerReference();
	}
	else
	{
		// If object is not WTContained, try invoking getContainerReference() method. This
		// is for objects that do not implement WTContained but who act like they are by
		// using the same Container as their parent. An example of this is
		// LifeCycleManagedWtMarkUps.
		try
		{
			contRef = (WTContainerRef) lcm.getClass().getMethod("getContainerReference").invoke(lcm, null);
		}
		catch (Exception e) {}
	}
	
%>
	<c:set var="iVals" value="<%=combo.getInternalValues()%>"/>
	<c:set var="dVals" value="<%=combo.getValues()%>"/>	

	<c:set var="p_lcOid" value="<%=plcOid%>"/>
	<c:set var="p_confref" value="<%=contRef%>"/>
	
		   <jca:renderPropertyPanel>
		     <w:comboBox propertyLabel="${DESCRIPTION}" id="lifecyclestate" name="lifecyclestate" internalValues="${iVals}"  displayValues="${dVals}"/>
		   </jca:renderPropertyPanel>
		   <br>	
	
</c:otherwise>
</c:choose>
