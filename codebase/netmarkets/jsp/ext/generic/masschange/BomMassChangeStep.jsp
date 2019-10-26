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
	
<fmt:message var="BOM_MASSCHANGE_TYPE" key="BOM_MASSCHANGE_TYPE" />
	
<fmt:message var="MODIFIED_PART" key="MODIFIED_PART" />
<fmt:message var="MODIFIED_PART_NAME" key="MODIFIED_PART_NAME" />		
<fmt:message var="MODIFIED_PART_VIEW" key="MODIFIED_PART_VIEW" />
	
<fmt:message var="REFERENCE_PART" key="REFERENCE_PART" />
<fmt:message var="REFERENCE_PART_NAME" key="REFERENCE_PART_NAME" />
<fmt:message var="REFERENCE_PART_VIEW" key="REFERENCE_PART_VIEW" />
	
<fmt:message var="QUANTITY" key="QUANTITY" />
	
<fmt:message var="SUBSTITUTED_PART" key="SUBSTITUTED_PART" />
<fmt:message var="SUBSTITUTED_PART_NAME" key="SUBSTITUTED_PART_NAME" />
<fmt:message var="SUBSTITUTED_PART_VIEW" key="SUBSTITUTED_PART_VIEW" />	

<fmt:message var="SEARCH_USEAGE_LINK" key="SEARCH_USEAGE_LINK" />
<fmt:message var="CLAER_VALUES" key="CLAER_VALUES" />

<fmt:message var="PARENT_LIST" key="PARENT_LIST" />
<fmt:message var="CUST_VIEW" key="CUST_VIEW" />
<fmt:message var="CUST_QUANTITY" key="CUST_QUANTITY" />
<fmt:message var="CUST_UNIT" key="CUST_UNIT" />
<fmt:message var="CUST_YIELD" key="CUST_YIELD" />
<fmt:message var="CHECK_LIST" key="CHECK_LIST" />	
<fmt:message var="MASSCHANGE_WARN" key="MASSCHANGE_WARN" />	
<fmt:message var="QUANTITY_NOT_NUM" key="QUANTITY_NOT_NUM" />				

<% 
  String BOM_MASSCHANGE_TYPE="MASSCHANGE_ADD_PART";
  String MODIFIED_PART="";
  String REFERENCE_PART="";
  String QUANTITY="";
  String SUBSTITUTED_PART="";
  String MODIFIED_PART_NUMBER_VALUE ="";
  String MODIFIED_PART_NAME_VALUE ="";
  String MODIFIED_PART_VIEW_VALUE ="";
  String REFERENCE_PART_NUMBER_VALUE ="";
  String REFERENCE_PART_NAME_VALUE ="";
  String REFERENCE_PART_VIEW_VALUE ="";
  String SUBSTITUTED_PART_NUMBER_VALUE ="";
  String SUBSTITUTED_PART_NAME_VALUE ="";
  String SUBSTITUTED_PART_VIEW_VALUE ="";
  Boolean QUANTITY_ISNUM = true;
  Cookie[] cookie = request.getCookies();
  for (int i = 0; i < cookie.length; i++) {
		 Cookie cook = cookie[i];	 
 		 if(cook.getName().equalsIgnoreCase("BOM_MASSCHANGE_TYPE")){ 
 		 		BOM_MASSCHANGE_TYPE = cook.getValue().toString();
 		 } else if(cook.getName().equalsIgnoreCase("MODIFIED_PART_NUMBER_VALUE")) {
 		 		MODIFIED_PART_NUMBER_VALUE = cook.getValue().toString(); 
 		 } else if(cook.getName().equalsIgnoreCase("MODIFIED_PART_NAME_VALUE")) {
 		 		MODIFIED_PART_NAME_VALUE = cook.getValue().toString(); 
 		 } else if(cook.getName().equalsIgnoreCase("MODIFIED_PART_VIEW_VALUE")) {
 		 		MODIFIED_PART_VIEW_VALUE = cook.getValue().toString();
 		 } else if(cook.getName().equalsIgnoreCase("REFERENCE_PART_NUMBER_VALUE")) {
 		 		REFERENCE_PART_NUMBER_VALUE = cook.getValue().toString();  
 		 } else if(cook.getName().equalsIgnoreCase("REFERENCE_PART_NAME_VALUE")) {
 		 		REFERENCE_PART_NAME_VALUE = cook.getValue().toString();
 		 } else if(cook.getName().equalsIgnoreCase("REFERENCE_PART_VIEW_VALUE")) {
 		 		REFERENCE_PART_VIEW_VALUE = cook.getValue().toString(); 
 		 } else if(cook.getName().equalsIgnoreCase("QUANTITY")) {
 		 		QUANTITY = cook.getValue().toString().trim();
 		 		QUANTITY_ISNUM = MassChangeUtil.isNum(QUANTITY);
 		 } else if(cook.getName().equalsIgnoreCase("SUBSTITUTED_PART_NUMBER_VALUE")) {
 		 		SUBSTITUTED_PART_NUMBER_VALUE = cook.getValue().toString(); 
 		 } else if(cook.getName().equalsIgnoreCase("SUBSTITUTED_PART_NAME_VALUE")) {
 		 		SUBSTITUTED_PART_NAME_VALUE = cook.getValue().toString(); 
 		 } else if(cook.getName().equalsIgnoreCase("SUBSTITUTED_PART_VIEW_VALUE")) {
 		 		SUBSTITUTED_PART_VIEW_VALUE = cook.getValue().toString();
 		 } 
 } 
%>

<jsp:useBean id="localeBean" class="com.ptc.netmarkets.util.beans.NmLocaleBean" scope="request"/>
<jsp:useBean id="nmcontext" class="com.ptc.netmarkets.util.beans.NmContextBean" scope="request"/>
<jsp:useBean id="urlFactoryBean" class="com.ptc.netmarkets.util.beans.NmURLFactoryBean" scope="request"/>
<%
	ResourceBundle resource = ResourceBundle.getBundle("ext.generic.masschange.resource.MassChangeResource", localeBean.getLocale()); 
	NmComboBox combo = new NmComboBox();
	combo.setEnabled(true);
	ArrayList internal = new ArrayList();
	ArrayList display = new ArrayList();
	ArrayList selected = new ArrayList();
	internal.add("MASSCHANGE_ADD_PART");
	internal.add("MASSCHANGE_DELETE_PART");
	internal.add("MASSCHANGE_SUBSTITUTE_PART");	
	internal.add("MASSCHANGE_QUANTITY_PART");
	
	display.add(resource.getString(MassChangeResource.MASSCHANGE_ADD_PART));
	display.add(resource.getString(MassChangeResource.MASSCHANGE_DELETE_PART));
	display.add(resource.getString(MassChangeResource.MASSCHANGE_SUBSTITUTE_PART));	
	display.add(resource.getString(MassChangeResource.MASSCHANGE_QUANTITY_PART));
	
	selected.add(resource.getString(BOM_MASSCHANGE_TYPE));
  combo.setInternalValues(internal);
  combo.setValues(display);
%>

	<c:set var="iVals" value="<%=combo.getInternalValues()%>"/>
	<c:set var="dVals" value="<%=combo.getValues()%>"/>	
	<c:set var="sVals" value="<%=selected%>"/>
		
<jca:renderPropertyPanel>
	<w:comboBox propertyLabel="${BOM_MASSCHANGE_TYPE}" id="BOM_MASSCHANGE_TYPE" name="BOM_MASSCHANGE_TYPE" internalValues="${iVals}"  displayValues="${dVals}" selectedValues="${sVals}"/>
	<jca:addSeparator/>
</jca:renderPropertyPanel>

<wctags:itemPicker id="MODIFIED_PART"  objectType="wt.part.WTPart" excludeSubTypes="WCTYPE|wt.doc.WTDocument,WCTYPE|wt.epm.EPMDocument,WCTYPE|com.ptc.windchill.enterprise.requirement.Specification,WCTYPE|com.ptc.windchill.enterprise.requirement.Requirement" componentId="variantPickerSearch" defaultVersionValue="LATEST" label="${MODIFIED_PART}:" pickerTextBoxLength="47" pickerCallback="PartPickerCallback"/>
<jca:renderPropertyPanel> 
  <w:textBox propertyLabel="${MODIFIED_PART_NAME}" id="MODIFIED_PART_NAME" name="MODIFIED_PART_NAME" required="false" size="40" readonly="true"/>
  <w:textBox propertyLabel="${MODIFIED_PART_VIEW}" id="MODIFIED_PART_VIEW" name="MODIFIED_PART_VIEW" required="false" size="40" readonly="true"/>
  <jca:addSeparator/>
</jca:renderPropertyPanel>
<wctags:itemPicker id="REFERENCE_PART" objectType="wt.part.WTPart" excludeSubTypes="WCTYPE|wt.doc.WTDocument,WCTYPE|wt.epm.EPMDocument,WCTYPE|com.ptc.windchill.enterprise.requirement.Specification,WCTYPE|com.ptc.windchill.enterprise.requirement.Requirement" componentId="variantPickerSearch" defaultVersionValue="LATEST" label="${REFERENCE_PART}:" pickerTextBoxLength="47" pickerCallback="PartPickerCallback"/>
<jca:renderPropertyPanel>  
  <w:textBox propertyLabel="${REFERENCE_PART_NAME}" id="REFERENCE_PART_NAME" name="REFERENCE_PART_NAME" required="false" size="40" readonly="true"/>
  <w:textBox propertyLabel="${REFERENCE_PART_VIEW}" id="REFERENCE_PART_VIEW" name="REFERENCE_PART_VIEW" required="false" size="40" readonly="true"/>
  <jca:addSeparator/>
  <w:textBox propertyLabel="${QUANTITY}" id="QUANTITY" name="QUANTITY" required="false" size="40" />
  <jca:addSeparator/>
</jca:renderPropertyPanel>
<wctags:itemPicker id="SUBSTITUTED_PART" objectType="wt.part.WTPart" excludeSubTypes="WCTYPE|wt.doc.WTDocument,WCTYPE|wt.epm.EPMDocument,WCTYPE|com.ptc.windchill.enterprise.requirement.Specification,WCTYPE|com.ptc.windchill.enterprise.requirement.Requirement" componentId="variantPickerSearch" defaultVersionValue="LATEST" label="${SUBSTITUTED_PART}:" pickerTextBoxLength="45" pickerCallback="PartPickerCallback"/>
<jca:renderPropertyPanel>
  <w:textBox propertyLabel="${SUBSTITUTED_PART_NAME}" id="SUBSTITUTED_PART_NAME" name="SUBSTITUTED_PART_NAME" required="false" size="38" readonly="true"/>
  <w:textBox propertyLabel="${SUBSTITUTED_PART_VIEW}" id="SUBSTITUTED_PART_VIEW" name="SUBSTITUTED_PART_VIEW" required="false" size="38" readonly="true"/>
  <jca:addSeparator/>  
  <w:button id="SEARCH_USEAGE_LINK" name="SEARCH_USEAGE_LINK" value="${SEARCH_USEAGE_LINK}" typeSubmit="false" toolTipText="${SEARCH_USEAGE_LINK}" onclick="searchUseageLink()"/>
  <w:button id="CLAER_VALUES" name="CLAER_VALUES" value="${CLAER_VALUES}" typeSubmit="false" toolTipText="${CLAER_VALUES}" onclick="clearValues()"/>
  <jca:addSeparator/>
</jca:renderPropertyPanel>

<c:set var="BOM_MASSCHANGE_TYPE_VALUE" value="<%=BOM_MASSCHANGE_TYPE%>"/>
<c:set var="MODIFIED_PART_NUMBER_VALUE" value="<%=MODIFIED_PART_NUMBER_VALUE%>"/>
<c:set var="MODIFIED_PART_VIEW_VALUE" value="<%=MODIFIED_PART_VIEW_VALUE%>"/>
<c:set var="REFERENCE_PART_NUMBER_VALUE" value="<%=REFERENCE_PART_NUMBER_VALUE%>"/>
<c:set var="REFERENCE_PART_VIEW_VALUE" value="<%=REFERENCE_PART_VIEW_VALUE%>"/>
<c:set var="QUANTITY_VALUE" value="<%=QUANTITY%>"/>
<c:set var="SUBSTITUTED_PART_NUMBER_VALUE" value="<%=SUBSTITUTED_PART_NUMBER_VALUE%>"/>
<c:set var="SUBSTITUTED_PART_VIEW_VALUE" value="<%=SUBSTITUTED_PART_VIEW_VALUE%>"/>

<c:choose>
	  <c:when test='${(BOM_MASSCHANGE_TYPE_VALUE == "MASSCHANGE_ADD_PART" && MODIFIED_PART_NUMBER_VALUE!="" && MODIFIED_PART_VIEW_VALUE!="" ) || 
	  (BOM_MASSCHANGE_TYPE_VALUE == "MASSCHANGE_DELETE_PART" && MODIFIED_PART_NUMBER_VALUE!="" && MODIFIED_PART_VIEW_VALUE!="" ) ||
    (BOM_MASSCHANGE_TYPE_VALUE == "MASSCHANGE_SUBSTITUTE_PART" && MODIFIED_PART_NUMBER_VALUE!="" && MODIFIED_PART_VIEW_VALUE!="" && SUBSTITUTED_PART_NUMBER_VALUE!="" && SUBSTITUTED_PART_VIEW_VALUE!="") || 
    (BOM_MASSCHANGE_TYPE_VALUE == "MASSCHANGE_QUANTITY_PART" && MODIFIED_PART_NUMBER_VALUE!="" && MODIFIED_PART_VIEW_VALUE!="" )}'>
    	<c:set var="QUANTITY_ISNUM_VALUE" value="<%=QUANTITY_ISNUM%>"/>
	    <c:choose>
	    	<c:when test='${(QUANTITY_VALUE!="" && QUANTITY_ISNUM_VALUE == "true")||QUANTITY_VALUE==""}'>
	    				<jsp:include page="${mvc:getComponentURL('ext.generic.masschange.builder.BomMassChangeSearchResultEndItemTableBuilder')}" flush="true" />
							<jsp:include page="${mvc:getComponentURL('ext.generic.masschange.builder.BomMassChangeSearchResultReviseTableBuilder')}" flush="true" />
							<jsp:include page="${mvc:getComponentURL('ext.generic.masschange.builder.BomMassChangeSearchResultCheckOutTableBuilder')}" flush="true" />
							<jsp:include page="${mvc:getComponentURL('ext.generic.masschange.builder.BomMassChangeSearchResultCheckListTableBuilder')}" flush="true" />
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
		 	 
	document.getElementById("MODIFIED_PART$label$").value= '<%=MODIFIED_PART_NUMBER_VALUE %>';
	document.getElementById("MODIFIED_PART_NAME").value= unescape('<%=MODIFIED_PART_NAME_VALUE %>');
	document.getElementById("MODIFIED_PART_VIEW").value= '<%=MODIFIED_PART_VIEW_VALUE %>';
	
	document.getElementById("REFERENCE_PART$label$").value= '<%=REFERENCE_PART_NUMBER_VALUE %>';
	document.getElementById("REFERENCE_PART_NAME").value= unescape('<%=REFERENCE_PART_NAME_VALUE %>');
	document.getElementById("REFERENCE_PART_VIEW").value= '<%=REFERENCE_PART_VIEW_VALUE %>';
	
	document.getElementById("QUANTITY").value= unescape('<%=QUANTITY %>');
	
	document.getElementById("SUBSTITUTED_PART$label$").value= '<%=SUBSTITUTED_PART_NUMBER_VALUE %>';
	document.getElementById("SUBSTITUTED_PART_NAME").value= unescape('<%=SUBSTITUTED_PART_NAME_VALUE %>');
	document.getElementById("SUBSTITUTED_PART_VIEW").value= '<%=SUBSTITUTED_PART_VIEW_VALUE %>';
	
</script>


<script type="text/javascript">

  function searchUseageLink () {
  	
  	var BOM_MASSCHANGE_TYPE = document.getElementById("BOM_MASSCHANGE_TYPE").value; 	
  	document.cookie = "BOM_MASSCHANGE_TYPE=" + BOM_MASSCHANGE_TYPE;		
  		
		var MODIFIED_PART_NUMBER_VALUE = document.getElementById("MODIFIED_PART$label$").value;
		document.cookie = "MODIFIED_PART_NUMBER_VALUE=" + MODIFIED_PART_NUMBER_VALUE;
		var MODIFIED_PART_NAME_VALUE = document.getElementById("MODIFIED_PART_NAME").value;
		document.cookie = "MODIFIED_PART_NAME_VALUE=" + escape(MODIFIED_PART_NAME_VALUE);
		var MODIFIED_PART_VIEW_VALUE = document.getElementById("MODIFIED_PART_VIEW").value;
		document.cookie = "MODIFIED_PART_VIEW_VALUE=" + MODIFIED_PART_VIEW_VALUE;
		
		var REFERENCE_PART_NUMBER_VALUE = document.getElementById("REFERENCE_PART$label$").value;
		document.cookie = "REFERENCE_PART_NUMBER_VALUE=" + REFERENCE_PART_NUMBER_VALUE;
		var REFERENCE_PART_NAME_VALUE = document.getElementById("REFERENCE_PART_NAME").value;
		document.cookie = "REFERENCE_PART_NAME_VALUE=" + escape(REFERENCE_PART_NAME_VALUE);
		var REFERENCE_PART_VIEW_VALUE = document.getElementById("REFERENCE_PART_VIEW").value;
		document.cookie = "REFERENCE_PART_VIEW_VALUE=" + REFERENCE_PART_VIEW_VALUE;
		
		var QUANTITY = document.getElementById("QUANTITY").value;
		document.cookie = "QUANTITY=" + escape(QUANTITY);
		
		var SUBSTITUTED_PART_NUMBER_VALUE = document.getElementById("SUBSTITUTED_PART$label$").value;
		document.cookie = "SUBSTITUTED_PART_NUMBER_VALUE=" + SUBSTITUTED_PART_NUMBER_VALUE;
		var SUBSTITUTED_PART_NAME_VALUE = document.getElementById("SUBSTITUTED_PART_NAME").value;
		document.cookie = "SUBSTITUTED_PART_NAME_VALUE=" + escape(SUBSTITUTED_PART_NAME_VALUE);
		var SUBSTITUTED_PART_VIEW_VALUE = document.getElementById("SUBSTITUTED_PART_VIEW").value;
		document.cookie = "SUBSTITUTED_PART_VIEW_VALUE=" + SUBSTITUTED_PART_VIEW_VALUE;
		refreshCurrentStep();				
	}
		function clearValues() {		
  	document.cookie = "BOM_MASSCHANGE_TYPE=" + "";
				
		document.cookie = "MODIFIED_PART_NUMBER_VALUE=" + "";
		document.cookie = "MODIFIED_PART_NAME_VALUE=" + "";
		document.cookie = "MODIFIED_PART_VIEW_VALUE=" + "";
		
		document.cookie = "REFERENCE_PART_NUMBER_VALUE=" + "";
		document.cookie = "REFERENCE_PART_NAME_VALUE=" + "";
		document.cookie = "REFERENCE_PART_VIEW_VALUE=" + "";
		
		document.cookie = "QUANTITY=" + "";
		
		document.cookie = "SUBSTITUTED_PART_NUMBER_VALUE=" + "";
		document.cookie = "SUBSTITUTED_PART_NAME_VALUE=" + "";
		document.cookie = "SUBSTITUTED_PART_VIEW_VALUE=" + "";
		
		document.cookie = "ADD_SELECT_PARTS=" + "";		
		
		document.getElementById("MODIFIED_PART$label$").value= "";
		document.getElementById("MODIFIED_PART_NAME").value= "";
		document.getElementById("MODIFIED_PART_VIEW").value= "";
	
		document.getElementById("REFERENCE_PART$label$").value= "";
		document.getElementById("REFERENCE_PART_NAME").value= "";
		document.getElementById("REFERENCE_PART_VIEW").value= "";
	
		document.getElementById("QUANTITY").value= "";
	
		document.getElementById("SUBSTITUTED_PART$label$").value= "";
		document.getElementById("SUBSTITUTED_PART_NAME").value= "";
		document.getElementById("SUBSTITUTED_PART_VIEW").value= "";
		
		//PTC.navigation.reload();
		refreshCurrentStep();	
  }
  
</script>
