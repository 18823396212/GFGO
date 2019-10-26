<% request.setAttribute(NmAction.SHOW_CONTEXT_INFO, "false"); %>
<%@ include file="/netmarkets/jsp/util/beginPopup.jspf"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>

<jsp:include page="${mvc:getComponentURL('netmarkets.object.clipboard')}" flush="true" />

<input type="hidden" name="isPasteSelect" value="true">
<%@ include file="/netmarkets/jsp/util/end.jspf"%>  