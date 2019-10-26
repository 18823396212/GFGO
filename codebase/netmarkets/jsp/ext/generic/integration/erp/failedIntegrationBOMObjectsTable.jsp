<%@ include file="/netmarkets/jsp/util/begin.jspf"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/carambola" prefix="cmb"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="jca"%>


<fmt:setLocale value="${localeBean.locale}"/>
<fmt:setBundle basename="ext.generic.integration.erp.manually.IntegrationResource" />
<fmt:message var="tableName" key="FAILED_INTEGRATION_PART_INFO" />
<fmt:message var="PARENT_NUMBER" key="PARENT_NUMBER" />
<fmt:message var="PARENT_VERSION" key="PARENT_VERSION" />
	<fmt:message var="SON_NUMBER" key="SON_NUMBER" />
		<fmt:message var="PARENT_VIEW" key="PARENT_VIEW" />
			<fmt:message var="FAILED_INFO" key="FAILED_INFO" />
		

<%
	NmCommandBean cb = new NmCommandBean();
	cb.setCompContext(nmcontext.getContext().toString());
	cb.setRequest(request);
%>

<%-->Build a descriptor and assign it to page variable tableDescriptor<--%>
<jca:describeTable var="tableDescriptor" type="wt.vc.Iterated"
                   id="FailedIntegrationObjects"
                   label="${tableName}"
                   configurable="false">
      <jca:describeColumn id="type"  label="" dataUtilityId="type_icon" />
      <jca:describeColumn id="parentnumber" label="${PARENT_NUMBER}" dataUtilityId="integrationInfo" sortable="true"  />
      <jca:describeColumn id="parentview" label="${PARENT_VIEW}" dataUtilityId="integrationInfo" sortable="false" />
      <jca:describeColumn id="parentversion" label="${PARENT_VERSION}" dataUtilityId="integrationInfo" sortable="false" />
   		<jca:describeColumn  id="sonnumber"   label="${SON_NUMBER}" dataUtilityId="integrationInfo" sortable="true"/>
  		
   		<jca:describeColumn id="lineNumber.value" />  			
      <jca:describeColumn  id="integrationErrInfo" label="${FAILED_INFO}"  dataUtilityId="integrationInfo" sortable="true" />	
</jca:describeTable>

<jca:getModel var="tableModel" descriptor="${tableDescriptor}" 
		serviceName="ext.generic.integration.erp.common.IntegrationCommand"
		methodName="getFailedPrintObjects">
<jca:addServiceArgument type="wt.workflow.work.WorkItem" value="${param.oid}"/>
</jca:getModel>

<%-->Render the table model<--%>
<jca:renderTable model="${tableModel}" showPagingLinks ="true"  pageLimit="-1" />

<%@ include file="/netmarkets/jsp/util/end.jspf"%>
