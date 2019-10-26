<%@ page pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=UTF-8" language="java" errorPage=""%>
<%request.setCharacterEncoding("UTF-8");%>

<%@ taglib prefix="wctags" tagdir="/WEB-INF/tags" %>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf"%>

<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>
<%@taglib prefix="w" uri="http://www.ptc.com/windchill/taglib/wrappers"%>
<%@ taglib prefix="fmt" uri="http://www.ptc.com/windchill/taglib/fmt" %>

<jsp:include page="${mvc:getComponentURL('query.inventory.price')}" />

<%@ include file="/netmarkets/jsp/util/end.jspf"%>