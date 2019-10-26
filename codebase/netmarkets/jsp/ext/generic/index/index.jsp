<%@ include file="/netmarkets/jsp/util/begin.jspf"%>
<%
	String subPageUrl = ext.generic.reviewprincipal.util.ReviewPrincipalUtil.getSubPageUrl(commandBean);
%>
<c:set var="subPageUrl" value="<%=subPageUrl%>" />
<jsp:include page="${subPageUrl}" flush="true" />
	
<%@ include file="/netmarkets/jsp/util/end.jspf"%>
