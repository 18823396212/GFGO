<jsp:useBean id="errorBean"
	class="com.ptc.netmarkets.util.beans.NmErrorBean" scope="request" />
<jsp:useBean id="objectBean"
	class="com.ptc.netmarkets.util.beans.NmObjectBean" scope="request" />
<jsp:useBean id="localeBean"
	class="com.ptc.netmarkets.util.beans.NmLocaleBean" scope="request" />
	
<%@ taglib prefix="wctags" tagdir="/WEB-INF/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>
<%@ page
	import="wt.util.WTMessage,
	wt.util.WTProperties,
	com.ptc.netmarkets.search.beans.SearchBean,
	com.ptc.netmarkets.util.NmUtils,
	com.ptc.windchill.enterprise.search.client.searchClientResource,
	com.ptc.netmarkets.search.SearchWebConstants,
	com.ptc.netmarkets.search.utils.SearchUtils,
	 wt.util.HTMLEncoder"%>

<%
	boolean isKeywordSearchEnabled = false;
	boolean isTreePicker=false;
%>
<c:set var="pickerType" value="${param.pickerType}" />
<c:set var="objectTypeAtLaunch" value="${param.objectType}" scope="request"/>

<%
	String objectTypeAtLaunch = (String) request.getAttribute("objectTypeAtLaunch");
	String objectTypeSelected = (String) request.getParameter("searchType");


	if(objectTypeSelected==null || objectTypeSelected.equals(""))
	{
		objectTypeSelected = objectTypeAtLaunch;
	}
	isKeywordSearchEnabled = SearchUtils.isKeywordSearchEnabled(objectTypeSelected);
%>
<%

	String wncurl = WTProperties.getServerCodebase().toString();
	//get value for keyword
	String defaultKeyword = "";
	
	// on press of clear button reset the keyword field to blank which is the default value. SPR#2139212 
	if (!"true".equalsIgnoreCase(request.getParameter("pickerClear"))) 
	{
		defaultKeyword = HTMLEncoder.encodeForHTMLAttribute(request.getParameter("null___keywordkeywordField_SearchTextBox___textbox"));

		if (defaultKeyword == null) {
			defaultKeyword = HTMLEncoder.encodeForHTMLAttribute(request.getParameter("defaultKeyword"));

			if (!isKeywordSearchEnabled) 
			{
				defaultKeyword = null;
			}

			if (defaultKeyword == null) 
			{
				defaultKeyword = "";
			} else {
				// fire search when defaultKeyword is passed
				request.setAttribute(SearchWebConstants.FIRE_SEARCH,"true");
			}
		}
	}

%>


<tr>
	<td>

		<input type="hidden" id="<%=SearchWebConstants.KEY_SEARCH_PAGE%>"
			name="<%=SearchWebConstants.KEY_SEARCH_PAGE%>"
			value="<%=SearchWebConstants.PICKER_PAGE%>" />
	</td>
</tr>

<fmt:setLocale value="${localeBean.locale}" />
<fmt:setBundle
	basename="com.ptc.windchill.enterprise.search.client.searchClientResource" />
<fmt:message var="lbl_keyword" key="KEYWORD_LABEL" />

<fmt:message var="lbl_advanced_search" key="ADVANCED_SEARCH_LBL" />
<fmt:message var="lbl_basic_search" key="BASIC_SEARCH_LBL" />

<%@ include file="/netmarkets/jsp/components/picker.jspf"%>
<link type="text/css" rel="stylesheet" href="<%=wncurl%>netmarkets/jsp/search/css/searchNavigation.css">

<c:choose>
	<c:when test="${'picker' == pickerType || 'tree' == pickerType}">
	<%

	isTreePicker=true;
	isKeywordSearchEnabled=false;
	%>
	</c:when>
	<c:otherwise>
		<c:if
			test="${'false' != param.renderKeyword && 'true' != param.onlyAttributeRendering}">

			<div id="advancedSearchPanel" name="advancedSearchPanel" style="border:0">
			<%if (isKeywordSearchEnabled) {	%>
			<div id="keyWordFieldDiv" style="visibility:visible">
			<%} else { 
				//For SPR 2061836
				defaultKeyword=""; %>
			<div id="keyWordFieldDiv" style="height:0px;visibility:hidden">
			<%}%>
			<table>
				<tr>
					<wctags:searchTextBoxWithLabel label="${lbl_keyword}"
						attrID="keyword" uiID='keywordField' width="50"
						searchTermProducer="com.ptc.netmarkets.search.stp.KeywordSearchTermProducer"
						defaultValue="<%=defaultKeyword%>"
						onkeyPress="checkPickerSearchPressed(event)" />


				</tr>
			</table>
			</div>
			<div id="advPanel" >
			<table>
				<tr>
					<td>
					<table>


						<tr>
							<td colspan="2">
							<div id="suggestionDiv" />
							</td>

						</tr>

					</table>
					</td>


				</tr>
			</table>
			<div id="panelContent">
		</c:if>
	</c:otherwise>

</c:choose>


<%-- Table tag is outside because it is pickerAttributes.jsp is used by Pricipal picker CD --%>
<table>
	<%@ include file="/netmarkets/jsp/search/pickerAttributes.jsp"%>
</table>
 <%if (!isTreePicker) {	%>
		<script>

			$('suggestionDiv').hide();
		</script>

		</div>
		</div>
		</div>

<%}%>

<c:choose>
    <c:when  test="${'picker' == pickerType || 'tree' == pickerType}">
        <%
        	request.setAttribute(SearchWebConstants.FIRE_SEARCH,"true");
        	request.setAttribute(SearchWebConstants.SHOW_REFINE_SEARCH,"false");
        %>
    </c:when>
    <c:otherwise>
        	<%@include file="/netmarkets/jsp/search/pickerButton.jsp"%>
    </c:otherwise>
</c:choose>


<c:choose>
	<c:when test="${'tree' == pickerType}">
		<%@include file="/netmarkets/jsp/search/treePickerDelegate.jsp"%>
	</c:when>
	<c:otherwise>
		<%@include file="/netmarkets/jsp/search/searchResults.jsp"%>
	</c:otherwise>
</c:choose>
