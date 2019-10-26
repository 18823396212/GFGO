<%@ include file="/netmarkets/jsp/util/begin.jspf"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/carambola" prefix="cmb"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="jca"%>


<fmt:setLocale value="${localeBean.locale}"/>
<fmt:setBundle basename="ext.generic.generatenumber.resource.GenerateNumberRB" />
<fmt:message var="tableName" key="FAILEDNUMBEROBJS_TABLE" />
<fmt:message var="NUMBER_FAILED_INFO" key="NUMBER_FAILED_INFO" />

<%-->Build a descriptor and assign it to page variable tableDescriptor<--%>
<jca:describeTable var="tableDescriptor" type="wt.vc.Iterated"
                   id="FailedNumberObjects"
                   label="${tableName}"
                   configurable="false">
      <jca:describeColumn id="type"  label="" dataUtilityId="type_icon" />
      <jca:describeColumn id="name"  />
      <jca:describeColumn id="number" sortable="false"  />
      <jca:describeColumn  id="infoPageAction"             sortable="false"/>
   	<jca:describeColumn  id="nmActions"                  sortable="false"/>
      <jca:describeColumn id="version" sortable="false" />
      <jca:describeColumn id="lifecycle" need="state.state" sortable="false" />
      <jca:describeColumn id="modifyStamp"    need="thePersistInfo.modifyStamp" />
      <jca:describeColumn id="modifier" />
      <jca:describeColumn  id="numberFailedInfo"     label="${NUMBER_FAILED_INFO}"  dataUtilityId="GenerateNoDatautility" sortable="true" />	
</jca:describeTable>

<jca:getModel var="tableModel" descriptor="${tableDescriptor}" 
		serviceName="ext.generic.generatenumber.util.GenerateNumberUtil"
		methodName="getFailedNumberObjects">
<jca:addServiceArgument type="wt.workflow.work.WorkItem" value="${param.oid}"/>
</jca:getModel>

<%-->Render the table model<--%>
<jca:renderTable model="${tableModel}" showPagingLinks ="true"  pageLimit="-1" />

<%@ include file="/netmarkets/jsp/util/end.jspf"%>
