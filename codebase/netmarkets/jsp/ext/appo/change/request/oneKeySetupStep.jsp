<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@page pageEncoding="UTF-8" %>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf" %>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="jca" %>
<%@ taglib prefix="wctags" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/wrappers" prefix="w" %>

<fmt:setBundle basename="ext.appo.ecn.resource.changeNoticeActionsRB"/>
<fmt:message var="completiontime" key="COMPLETIONTIME"/>
<%
    Calendar curr = Calendar.getInstance();
    curr.set(Calendar.DAY_OF_MONTH,curr.get(Calendar.DAY_OF_MONTH)+7);
    Date date=curr.getTime();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    String completiontime=dateFormat.format(date);
%>

<table>
    <tr>
        <td>${completiontime}:</td>
        <td>
            <w:dateInputComponent id="completiontime" name="completiontime" required="true"/>
        </td>
    </tr>
    <tr>
        <td>责任人:</td>
        <td>
<%--            <w:pickerInputComponent propertyLabel="" id="actualDateMergeValue" name="actualDateMergeValue"  required="true"/>--%>
        </td>
    </tr>
</table>


<%@ include file="/netmarkets/jsp/util/end.jspf" %>
