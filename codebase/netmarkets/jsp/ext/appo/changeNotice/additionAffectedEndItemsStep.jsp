<%@ include file="/netmarkets/jsp/components/beginWizard.jspf" %>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="jca" %>
<%@ page import="com.ptc.core.components.util.RequestHelper" %>
<%@ taglib prefix="wctags" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/wrappers" prefix="w" %>


<%@ page import="com.ptc.netmarkets.util.beans.NmCommandBean,
                 com.ptc.netmarkets.util.misc.NmAction,
                 com.ptc.netmarkets.util.misc.NmActionServiceHelper,
                 ext.generic.borrow.utils.BorrowUtil,
                 java.util.ArrayList,
                 java.util.Vector"
%>

<fmt:setLocale value="${localeBean.locale}"/>
<fmt:setBundle basename="ext.appo.ecn.resource.changeNoticeActionsRB"/>
<fmt:message var="defaultTableName" key="SEARCH_RESULT"/>
<fmt:message var="stateLabel" key="STATE"/>
<fmt:message var="modifitorLabel" key="BORROW_OBJECT_MODIFITOR"/>
<fmt:message var="numberLabel" key="NAME_LABEL"/>
<fmt:message var="nameLabel" key="NUMBER_LABEL"/>
<fmt:message var="searchLabel" key="SEARCH_LABEL"/>
<fmt:message var="versionLabel" key="VERSION_LABEL"/>
<fmt:message var="contextLabel" key="CONTEXT"/>
<fmt:message var="typeLabel" key="TYPE"/>
<fmt:message var="objecttypeLabel" key="ObjectTYPE"/>
<fmt:message var="ggmsLabel" key="GGMS"/>
<fmt:message var="productLabel" key="CONTEXT"/>

<%
    NmCommandBean cb = new NmCommandBean();
    cb.setCompContext(nmcontext.getContext().toString());
    cb.setRequest(request);
    Vector typevc = BorrowUtil.getBorrowObjectType();
    ArrayList objecttpyedisplaytemp = (ArrayList) typevc.elementAt(0);
    ArrayList objecttpyevaluetemp = (ArrayList) typevc.elementAt(1);
    String tyepvaluedefault = "";
    ArrayList objecttypelist = (ArrayList) cb.getComboBox().get("objecttype");
    if (objecttypelist != null && objecttypelist.size() > 0) tyepvaluedefault = (String) objecttypelist.get(0);
    ArrayList typedefaulttemp = new ArrayList();
    typedefaulttemp.add(tyepvaluedefault);

    String name_in = RequestHelper.getEncodedRequestParam("name", request);
    String number_in = RequestHelper.getEncodedRequestParam("number", request);
    String ggms_in = RequestHelper.getEncodedRequestParam("ggms", request);
    //String context_in = RequestHelper.getEncodedRequestParam("contextselect", request);
    String contexttype_in = RequestHelper.getEncodedRequestParam("contexttype", request);

    /*NmAction nmaction = NmActionServiceHelper.service.getAction("borrowObjects", "searchContext");
    nmaction.setContextObject(nmcontext.getContext().getPrimaryOid());
    String url = nmaction.getActionUrlExternal();*/

%>
<c:set var="objecttpyevalue" value="<%=objecttpyevaluetemp%>"/>
<c:set var="objecttypedisplay" value="<%=objecttpyedisplaytemp%>"/>
<c:set var="typedefault" value="<%=typedefaulttemp%>"/>
<c:set var="typestr" value="<%=tyepvaluedefault%>"/>
<table>
    <tr>
        <th scope="row">${nameLabel}:</th>
        <td class="tabledatafont">&nbsp;&nbsp;&nbsp;
            <input type="text" name="name" id="name" size="40" value="<%=name_in%>"/></td>
        <td></td>
    </tr>
    <tr>
        <th scope="row">${numberLabel}:</th>
        <td class="tabledatafont">&nbsp;&nbsp;&nbsp;
            <input type="text" name="number" id="number" size="40" value="<%=number_in%>"/></td>
        <td></td>
    </tr>
    <tr>
        <th scope="row">${ggmsLabel}:</th>
        <td class="tabledatafont">&nbsp;&nbsp;&nbsp;
            <input type="text" name="ggms" id="ggms" size="40" value="<%=ggms_in%>"/></td>
        <td></td>
    </tr>
    <tr>
        <th scope="row">${objecttypeLabel}:</th>
        <td class="tabledatafont">
            <jca:renderPropertyPanel>
                <w:comboBox id="objecttype" name="objecttype" internalValues="${objecttpyevalue}"
                            displayValues="${objecttypedisplay}" multiSelect="false" selectedValues="${typedefault}"/>
            </jca:renderPropertyPanel>
        </td>
        <td></td>
    </tr>


    <!--tr>
   	<th scope="row" >${productLabel}:</th>
    <td class="tabledatafont">&nbsp;&nbsp;&nbsp;
    <input type="text" name="contextselect"  id="contextselect" readonly size="40"  value=""/></td>
    <td>
    	<td align="right">
    	<button type="button" onclick=""> ${productLabel}</button>
    </td>
    </td>
  </tr-->

    <tr>
        <td align="right">
            <button onclick="submittForm();">${searchLabel}</button>
        </td>
    </tr>

</table>
<input type="hidden" name="contexttype" id="contexttype" value="<%=contexttype_in%>"/>


<jca:describeTable var="searchBorrowObjectTableDescriptor"
                   id="searchRelateBorrowObjectlist"
                   label="${defaultTableName}">
    <jca:setComponentProperty key="selectable" value="true"/>
    <jca:describeColumn id="type_icon" sortable="false"/>
    <jca:describeColumn id="name" dataUtilityId="customizationDataUtility"/>
    <jca:describeColumn id="number" dataUtilityId="customizationDataUtility"/>
    <jca:describeColumn id="displayType" label="${typeLabel}"/>
    <jca:describeColumn id="borroworder_version" label="${versionLabel}" dataUtilityId="borroworder_version"/>
    <jca:describeColumn id="creator"/>
    <jca:describeColumn id="lifeCycleState" label="${stateLabel}"/>
    <jca:describeColumn id="borroworder_containername" label="${contextLabel}"
                        dataUtilityId="borroworder_containername"/>
    <jca:describeColumn id="ggms" label="${ggmsLabel}" dataUtilityId="customizationDataUtility"/>
</jca:describeTable>

<jca:getModel var="relateBorrowObjectModel"
              descriptor="${searchBorrowObjectTableDescriptor}"
              serviceName="ext.generic.borrow.utils.BorrowUtil"
              methodName="searchBorrowObject">
    <jca:addServiceArgument value="${param.name}" type="java.lang.String"/>
    <jca:addServiceArgument value="${param.number}" type="java.lang.String"/>
    <jca:addServiceArgument value="${param.ggms}" type="java.lang.String"/>
    <jca:addServiceArgument value="${param.contexttype}" type="java.lang.String"/>
    <jca:addServiceArgument value="${typestr}" type="java.lang.String"/>

</jca:getModel>

<jca:renderTable model="${relateBorrowObjectModel}"/>

<script>
    function submittForm() {
        var in_name = document.getElementById('name').value;
        var in_number = document.getElementById('number').value;
        //var in_context=document.getElementById('contextselect').value;
        var in_describe = document.getElementById('ggms').value;
        if (in_name == "" && in_number == "" && in_describe == "") {
            alert("\u8bf7\u6307\u5b9a\u641c\u7d22\u6761\u4ef6");
        }
        refreshCurrentStep();
    }

    function affectedEndItemsBySearch() {
        var itemsOid = [];
        var selectedOidsArray = getSelectedItems();
        var len = selectedOidsArray.length;
        for (var i = 0; i < len; i++) {
            itemsOid[i] = getOidFromRowValue(selectedOidsArray[i].value);
        }
        window.opener.addCollectItemsForAffectedEndItems(itemsOid);
        window.close();
    }
</script>
<%@ include file="/netmarkets/jsp/util/end.jspf" %>