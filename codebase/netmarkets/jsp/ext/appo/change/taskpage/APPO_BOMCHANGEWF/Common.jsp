<%@ include file="/netmarkets/jsp/util/begin.jspf"%>
<%@ include file="/netmarkets/jsp/ext/appo/change/taskpage/ExtWorkflowStepGuide.jsp"%>
<jsp:include page="${mvc:getComponentURL('ext.appo.change.mvc.builder.AffectedItemsTableBuilder')}" flush="true"/>
<jsp:include page="${mvc:getComponentURL('ext.appo.change.mvc.builder.ResultingItemsTableBuilder')}" flush="true"/>
<%@ include file="/netmarkets/jsp/util/end.jspf"%>

