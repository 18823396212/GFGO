<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Enumeration" %>
<%@page pageEncoding="UTF-8" %>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf" %>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="jca" %>
<%@ taglib prefix="wctags" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/wrappers" prefix="w" %>
<jsp:useBean id="pickerHelper" class="com.ptc.netmarkets.search.PickerHelper"  scope="request"/>

<fmt:setBundle basename="ext.appo.ecn.resource.changeNoticeActionsRB"/>
<fmt:message var="completiontime" key="COMPLETIONTIME"/>
<style type="text/css">
    img:hover{
        color: red;
        cursor: pointer;
    }

</style>


<%
    //取路径
    wt.httpgw.URLFactory urlFactory = new wt.httpgw.URLFactory();
    String baseUrl = urlFactory.getBaseHREF();
    String search = baseUrl + "netmarkets/images/search.gif";
    String callSearchPicker=baseUrl+"netmarkets/jsp/search/callSearchPicker.jsp?pickerType=&portlet=poppedup&objectType=wt.org.WTUser";

    Calendar curr = Calendar.getInstance();
    curr.set(Calendar.DAY_OF_MONTH,curr.get(Calendar.DAY_OF_MONTH)+7);
    Date date=curr.getTime();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    String completiontime=dateFormat.format(date);

%>
<input type="hidden" id="search" value="<%=search%>">
<input type="hidden" id="callSearchPicker" value="<%=callSearchPicker%>">

<span style="margin-right: 20px;" >${completiontime}:</span>
<w:dateInputComponent propertyLabel="DateInputComponent" id="completiontime" name="completiontime" required="true" dateValueType="DATE_ONLY"/>
<br/>
<br/>
<span style="margin-right: 53px;" >责任人:</span>
<wctags:userPicker id="userPicker" pickerTitle="搜索负责人" label="" readOnlyPickerTextBox="true" />

</table>

<script>
    function oneKeySetup() {
        var completiontimeTextbox = getMainForm().null___completiontime_col_completiontime___textbox;
        var completiontime=completiontimeTextbox.value;
        var userPicker = document.getElementById('userPicker$label$').value;
        window.opener.addOneKeySetup(completiontime,userPicker);
        window.close();
    }
</script>
<%@ include file="/netmarkets/jsp/util/end.jspf" %>
