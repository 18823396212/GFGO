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
<fmt:message var="OLD_MAIN_MATERIAL" key="OLD_MAIN_MATERIAL" />
<fmt:message var="OLD_MAIN_MATERIAL_NAME" key="OLD_MAIN_MATERIAL_NAME" />
<fmt:message var="OLD_MAIN_MATERIAL_VIEW" key="OLD_MAIN_MATERIAL_VIEW" />
<fmt:message var="NEW_MAIN_MATERIAL" key="NEW_MAIN_MATERIAL" />
<fmt:message var="NEW_MAIN_MATERIAL_NAME" key="NEW_MAIN_MATERIAL_NAME" />
<fmt:message var="NEW_MAIN_MATERIAL_VIEW" key="NEW_MAIN_MATERIAL_VIEW" />	
<fmt:message var="QUANTITY" key="QUANTITY" />		
<fmt:message var="SEARCH_USEAGE_LINK" key="SEARCH_USEAGE_LINK" />
<fmt:message var="CLAER_VALUES" key="CLAER_VALUES" />
<fmt:message var="MASSCHANGE_WARN" key="MASSCHANGE_WARN" />	
<fmt:message var="QUANTITY_NOT_NUM" key="QUANTITY_NOT_NUM" />	

<jsp:useBean id="localeBean" class="com.ptc.netmarkets.util.beans.NmLocaleBean" scope="request"/>

<%
	ResourceBundle resource = ResourceBundle.getBundle("ext.generic.masschange.resource.MassChangeResource", localeBean.getLocale()); 	
	String SUBSTITUTE_MASSCHANGE_TYPE_LOCAL = resource.getString(MassChangeResource.MASSCHANGE_REPLACE_MAIN);
	String OLD_MAIN_MATERIAL_NUMBER_VALUE = "";
	String OLD_MAIN_MATERIAL_NAME_VALUE = "";
	String OLD_MAIN_MATERIAL_VIEW_VALUE = "";
	String NEW_MAIN_MATERIAL_NUMBER_VALUE = "";
	String NEW_MAIN_MATERIAL_NAME_VALUE = "";
	String NEW_MAIN_MATERIAL_VIEW_VALUE = "";
	String QUANTITY="";
	Boolean QUANTITY_ISNUM = true;
	Cookie[] cookie = request.getCookies();
  for (int i = 0; i < cookie.length; i++) {
		 Cookie cook = cookie[i];	 
 		 if(cook.getName().equalsIgnoreCase("OLD_MAIN_MATERIAL_NUMBER_VALUE")){ 
 		 		OLD_MAIN_MATERIAL_NUMBER_VALUE = cook.getValue().toString();
 		 } else if(cook.getName().equalsIgnoreCase("OLD_MAIN_MATERIAL_NAME_VALUE")) {
 		 		OLD_MAIN_MATERIAL_NAME_VALUE = cook.getValue().toString(); 
 		 }else if(cook.getName().equalsIgnoreCase("OLD_MAIN_MATERIAL_VIEW_VALUE")) {
 		 		OLD_MAIN_MATERIAL_VIEW_VALUE = cook.getValue().toString(); 
 		 }else if(cook.getName().equalsIgnoreCase("NEW_MAIN_MATERIAL_NUMBER_VALUE")) {
 		 		NEW_MAIN_MATERIAL_NUMBER_VALUE = cook.getValue().toString(); 
 		 }else if(cook.getName().equalsIgnoreCase("NEW_MAIN_MATERIAL_NAME_VALUE")) {
 		 		NEW_MAIN_MATERIAL_NAME_VALUE = cook.getValue().toString(); 
 		 }else if(cook.getName().equalsIgnoreCase("NEW_MAIN_MATERIAL_VIEW_VALUE")) {
 		 		NEW_MAIN_MATERIAL_VIEW_VALUE = cook.getValue().toString(); 
 		 }else if(cook.getName().equalsIgnoreCase("QUANTITY")) {
 		 		QUANTITY = cook.getValue().toString().trim();
 		 		QUANTITY_ISNUM = MassChangeUtil.isNum(QUANTITY); 
 		 }
 	}
	
	
	
%>
<c:set var="type" value="<%=SUBSTITUTE_MASSCHANGE_TYPE_LOCAL%>"/>

<jca:renderPropertyPanel> 
  <w:textBox propertyLabel="${SUBSTITUTE_MASSCHANGE_TYPE}" id="SUBSTITUTE_MASSCHANGE_TYPE" name="SUBSTITUTE_MASSCHANGE_TYPE" required="false" size="31" readonly="true" value="${type}"/>
  <jca:addSeparator/>
</jca:renderPropertyPanel>
<wctags:itemPicker id="OLD_MAIN_MATERIAL"  objectType="wt.part.WTPart" excludeSubTypes="WCTYPE|wt.doc.WTDocument,WCTYPE|wt.epm.EPMDocument,WCTYPE|com.ptc.windchill.enterprise.requirement.Specification,WCTYPE|com.ptc.windchill.enterprise.requirement.Requirement" componentId="variantPickerSearch" defaultVersionValue="LATEST" label="${OLD_MAIN_MATERIAL}:" pickerTextBoxLength="47" pickerCallback="PartPickerCallback"/>
<jca:renderPropertyPanel> 
  <w:textBox propertyLabel="${OLD_MAIN_MATERIAL_NAME}" id="OLD_MAIN_MATERIAL_NAME" name="OLD_MAIN_MATERIAL_NAME" required="false" size="40" readonly="true"/>
  <w:textBox propertyLabel="${OLD_MAIN_MATERIAL_VIEW}" id="OLD_MAIN_MATERIAL_VIEW" name="OLD_MAIN_MATERIAL_VIEW" required="false" size="40" readonly="true"/>
  <jca:addSeparator/>
</jca:renderPropertyPanel>
<wctags:itemPicker id="NEW_MAIN_MATERIAL"  objectType="wt.part.WTPart" excludeSubTypes="WCTYPE|wt.doc.WTDocument,WCTYPE|wt.epm.EPMDocument,WCTYPE|com.ptc.windchill.enterprise.requirement.Specification,WCTYPE|com.ptc.windchill.enterprise.requirement.Requirement" componentId="variantPickerSearch" defaultVersionValue="LATEST" label="${NEW_MAIN_MATERIAL}:" pickerTextBoxLength="47" pickerCallback="PartPickerCallback"/>
<jca:renderPropertyPanel> 
  <w:textBox propertyLabel="${NEW_MAIN_MATERIAL_NAME}" id="NEW_MAIN_MATERIAL_NAME" name="OLD_MAIN_MATERIAL_NAME" required="false" size="40" readonly="true"/>
  <w:textBox propertyLabel="${NEW_MAIN_MATERIAL_VIEW}" id="NEW_MAIN_MATERIAL_VIEW" name="NEW_MAIN_MATERIAL_VIEW" required="false" size="40" readonly="true"/>
  <jca:addSeparator/>
  <w:textBox propertyLabel="${QUANTITY}" id="QUANTITY" name="QUANTITY" required="false" size="40" />
	<jca:addSeparator/>  
  <w:button id="SEARCH_USEAGE_LINK" name="SEARCH_USEAGE_LINK" value="${SEARCH_USEAGE_LINK}" typeSubmit="false" toolTipText="${SEARCH_USEAGE_LINK}" onclick="searchUseageLink()"/>
  <w:button id="CLAER_VALUES" name="CLAER_VALUES" value="${CLAER_VALUES}" typeSubmit="false" toolTipText="${CLAER_VALUES}" onclick="clearValues()"/>
  <jca:addSeparator/>
</jca:renderPropertyPanel>

<c:set var="OLD_MAIN_MATERIAL_NUMBER_VALUE" value="<%=OLD_MAIN_MATERIAL_NUMBER_VALUE%>"/>
<c:set var="OLD_MAIN_MATERIAL_VIEW_VALUE" value="<%=OLD_MAIN_MATERIAL_VIEW_VALUE%>"/>
<c:set var="NEW_MAIN_MATERIAL_NUMBER_VALUE" value="<%=NEW_MAIN_MATERIAL_NUMBER_VALUE%>"/>
<c:set var="NEW_MAIN_MATERIAL_VIEW_VALUE" value="<%=NEW_MAIN_MATERIAL_VIEW_VALUE%>"/>
<c:set var="QUANTITY_VALUE" value="<%=QUANTITY%>"/>
	
<c:choose>
	  <c:when test='${OLD_MAIN_MATERIAL_NUMBER_VALUE!="" && OLD_MAIN_MATERIAL_VIEW_VALUE!="" && NEW_MAIN_MATERIAL_NUMBER_VALUE!="" && NEW_MAIN_MATERIAL_VIEW_VALUE!=""}'>
    	<c:set var="QUANTITY_ISNUM_VALUE" value="<%=QUANTITY_ISNUM%>"/>
	    <c:choose>
	    	<c:when test='${(QUANTITY_VALUE!="" && QUANTITY_ISNUM_VALUE == "true")||QUANTITY_VALUE==""}'>
							<jsp:include page="${mvc:getComponentURL('ext.generic.masschange.builder.SubstituteMassChangeSearchResultReviseTableBuilder')}" flush="true" />
							<jsp:include page="${mvc:getComponentURL('ext.generic.masschange.builder.SubstituteMassChangeSearchResultCheckOutTableBuilder')}" flush="true" />
							<jsp:include page="${mvc:getComponentURL('ext.generic.masschange.builder.SubstituteMassChangeSearchResultCheckListTableBuilder')}" flush="true" />
				</c:when>
				<c:otherwise>    	
    			${QUANTITY_NOT_NUM} 	   	
    		</c:otherwise>
			</c:choose>  	 
    </c:when>
    <c:otherwise>    	
    	${MASSCHANGE_WARN}   	   	
    </c:otherwise>
</c:choose>

<script>
		 	 
	document.getElementById("OLD_MAIN_MATERIAL$label$").value= '<%=OLD_MAIN_MATERIAL_NUMBER_VALUE %>';
	document.getElementById("OLD_MAIN_MATERIAL_NAME").value= unescape('<%=OLD_MAIN_MATERIAL_NAME_VALUE %>');
	document.getElementById("OLD_MAIN_MATERIAL_VIEW").value= '<%=OLD_MAIN_MATERIAL_VIEW_VALUE %>';
	
	document.getElementById("NEW_MAIN_MATERIAL$label$").value= '<%=NEW_MAIN_MATERIAL_NUMBER_VALUE %>';
	document.getElementById("NEW_MAIN_MATERIAL_NAME").value= unescape('<%=NEW_MAIN_MATERIAL_NAME_VALUE %>');
	document.getElementById("NEW_MAIN_MATERIAL_VIEW").value= '<%=NEW_MAIN_MATERIAL_VIEW_VALUE %>';
	
	document.getElementById("QUANTITY").value= unescape('<%=QUANTITY %>');
	
</script>


<script type="text/javascript">

  function searchUseageLink () {
			
		document.cookie = "SUBSTITUTE_MASSCHANGE_TYPE=" + "MASSCHANGE_REPLACE_MAIN";
		
		var OLD_MAIN_MATERIAL_NUMBER_VALUE = document.getElementById("OLD_MAIN_MATERIAL$label$").value;
		document.cookie = "OLD_MAIN_MATERIAL_NUMBER_VALUE=" + OLD_MAIN_MATERIAL_NUMBER_VALUE;
		var OLD_MAIN_MATERIAL_NAME_VALUE = document.getElementById("OLD_MAIN_MATERIAL_NAME").value;
		document.cookie = "OLD_MAIN_MATERIAL_NAME_VALUE=" + escape(OLD_MAIN_MATERIAL_NAME_VALUE);
		var OLD_MAIN_MATERIAL_VIEW_VALUE = document.getElementById("OLD_MAIN_MATERIAL_VIEW").value;
		document.cookie = "OLD_MAIN_MATERIAL_VIEW_VALUE=" + OLD_MAIN_MATERIAL_VIEW_VALUE;
		
		var NEW_MAIN_MATERIAL_NUMBER_VALUE = document.getElementById("NEW_MAIN_MATERIAL$label$").value;
		document.cookie = "NEW_MAIN_MATERIAL_NUMBER_VALUE=" + NEW_MAIN_MATERIAL_NUMBER_VALUE;
		var NEW_MAIN_MATERIAL_NAME_VALUE = document.getElementById("NEW_MAIN_MATERIAL_NAME").value;
		document.cookie = "NEW_MAIN_MATERIAL_NAME_VALUE=" + escape(NEW_MAIN_MATERIAL_NAME_VALUE);
		var NEW_MAIN_MATERIAL_VIEW_VALUE = document.getElementById("NEW_MAIN_MATERIAL_VIEW").value;
		document.cookie = "NEW_MAIN_MATERIAL_VIEW_VALUE=" + NEW_MAIN_MATERIAL_VIEW_VALUE;
		
		var QUANTITY = document.getElementById("QUANTITY").value;
		document.cookie = "QUANTITY=" + escape(QUANTITY);
		
		refreshCurrentStep();			
	}
		function clearValues() {
		
		document.cookie = "SUBSTITUTE_MASSCHANGE_TYPE=" + "";
					
		document.cookie = "OLD_MAIN_MATERIAL_NUMBER_VALUE=" + "";
		document.cookie = "OLD_MAIN_MATERIAL_NAME_VALUE=" + "";
		document.cookie = "OLD_MAIN_MATERIAL_VIEW_VALUE=" + "";
		
		document.cookie = "NEW_MAIN_MATERIAL_NUMBER_VALUE=" + "";
		document.cookie = "NEW_MAIN_MATERIAL_NAME_VALUE=" + "";
		document.cookie = "NEW_MAIN_MATERIAL_VIEW_VALUE=" + "";
		
		document.cookie = "QUANTITY=" + "";
			
		
		document.getElementById("OLD_MAIN_MATERIAL$label$").value= "";
		document.getElementById("OLD_MAIN_MATERIAL_NAME").value= "";
		document.getElementById("OLD_MAIN_MATERIAL_VIEW").value= "";
	
		document.getElementById("NEW_MAIN_MATERIAL$label$").value= "";
		document.getElementById("NEW_MAIN_MATERIAL_NAME").value= "";
		document.getElementById("NEW_MAIN_MATERIAL_VIEW").value= "";
	
		document.getElementById("QUANTITY").value= "";
		
		refreshCurrentStep();	
  }
  
</script>



