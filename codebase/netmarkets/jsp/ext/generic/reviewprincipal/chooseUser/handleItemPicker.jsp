<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ page import="com.ptc.netmarkets.util.misc.NmTextBox,
                 com.ptc.netmarkets.util.misc.NmComboBox,
                 com.ptc.netmarkets.util.misc.NmDate,
                 com.ptc.netmarkets.util.misc.NmAction,
                 com.ptc.netmarkets.util.misc.NmActionServiceHelper,
                 com.ptc.netmarkets.model.NmOid,
                 com.ptc.netmarkets.util.beans.NmCommandBean,
                 com.ptc.netmarkets.search.beans.AttributeInfoBean,
                 com.ptc.netmarkets.search.beans.CriteriaBean,
                 com.ptc.netmarkets.search.beans.SearchBean,
                 com.ptc.netmarkets.search.AttributeRenderer,
                 com.ptc.netmarkets.search.utils.SearchUtils,
                 com.ptc.netmarkets.search.PickerXMLParser,
                 wt.session.SessionHelper,
                 java.util.Iterator,
                 java.util.Collection,
                 java.util.ArrayList,
                 java.util.Locale,
                 com.ptc.netmarkets.search.SearchWebConstants,
                 wt.util.HTMLEncoder"
        %>

<jsp:useBean id="searchbean" class="com.ptc.netmarkets.search.beans.SearchBean" scope="request"/>

<jsp:useBean id="errorBean"       class="com.ptc.netmarkets.util.beans.NmErrorBean"  scope="request"/><%//
%><jsp:useBean id="commandBean"       class="com.ptc.netmarkets.util.beans.NmCommandBean"  scope="request"/><%//
%><jsp:useBean id="wizardBean"       class="com.ptc.netmarkets.util.beans.NmWizardBean"  scope="request"/><%//
%><jsp:useBean id="presentationBean" class="com.ptc.netmarkets.util.beans.NmPresentationBean" scope="request"/><%//
%><jsp:useBean id="loginBean"        class="com.ptc.netmarkets.util.beans.NmLoginBean"   scope="session"/><%//
%><jsp:useBean id="tabBean"          class="com.ptc.netmarkets.util.beans.NmTabBean"     scope="request"/><%//
%><jsp:useBean id="modelBean" class="com.ptc.netmarkets.util.beans.NmModelBean" scope="request"/><%//
%><jsp:useBean id="actionBean" class="com.ptc.netmarkets.util.beans.NmActionBean" scope="request"/><%//
%><jsp:useBean id="objectBean" class="com.ptc.netmarkets.util.beans.NmObjectBean" scope="request"/><%//
%><jsp:useBean id="checkBoxBean" class="com.ptc.netmarkets.util.beans.NmCheckBoxBean"  scope="request"/><%//
%><jsp:useBean id="textBoxBean"  class="com.ptc.netmarkets.util.beans.NmTextBoxBean"  scope="request"/><%//
%><jsp:useBean id="radioButtonBean" class="com.ptc.netmarkets.util.beans.NmRadioButtonBean"  scope="request"/><%//
%><jsp:useBean id="textAreaBean" class="com.ptc.netmarkets.util.beans.NmTextAreaBean"  scope="request"/><%//
%><jsp:useBean id="comboBoxBean" class="com.ptc.netmarkets.util.beans.NmComboBoxBean"  scope="request"/><%//
%><jsp:useBean id="dateBean"    class="com.ptc.netmarkets.util.beans.NmDateBean"    scope="request"/><%//
%><jsp:useBean id="stringBean" class="com.ptc.netmarkets.util.beans.NmStringBean"  scope="request"/><%//
%><jsp:useBean id="linkBean"    class="com.ptc.netmarkets.util.beans.NmLinkBean"    scope="request"/><%//
%><jsp:useBean id="urlFactoryBean" class="com.ptc.netmarkets.util.beans.NmURLFactoryBean" scope="request"/><%//
%><jsp:useBean id="nmcontext" class="com.ptc.netmarkets.util.beans.NmContextBean" scope="request"/><%//
%><jsp:useBean id="sessionBean" class="com.ptc.netmarkets.util.beans.NmSessionBean" scope="session"/><%//
%><jsp:useBean id="localeBean" class="com.ptc.netmarkets.util.beans.NmLocaleBean" scope="request"/>

<%
    String objectType = request.getParameter("objectType");
    String searchObjectType = objectType;
    searchbean.setObjectType(searchObjectType);
    if(searchObjectType==null) searchObjectType = "";
    boolean isItemMasterPicker=false;
    boolean showTypePicker = Boolean.parseBoolean(request.getParameter("showTypePicker"));
    String pickerName = request.getParameter("pickerName");
    if("ItemMasterPicker".equals(pickerName)){
        isItemMasterPicker = true;
    }
    if(pickerName==null) pickerName = "";
    request.setAttribute("pickerName", pickerName); // for Type Picker

    searchbean.setSearchPage(SearchWebConstants.PICKER_PAGE);
    String reqSearchType = request.getParameter("searchType");
    String singleObjectSelect = request.getParameter("singleSelectTypePicker");
    String defaultObjectSelect = request.getParameter("typePickerDefaultType");

    // if request searchType is not null then use it.
    if(reqSearchType!=null && reqSearchType.length()>0){
        //int index=0;
        //if((index = reqSearchType.indexOf(",")) > 0) reqSearchType = reqSearchType.substring(0, index);
        searchObjectType = reqSearchType;
    }

    if(searchObjectType==null || searchObjectType.trim().length()==0) {
        searchObjectType="wt.fc.Persistable";
    }
    if(reqSearchType == null && defaultObjectSelect != null && defaultObjectSelect.trim().length() > 0 ){
        searchbean.setSearchType(defaultObjectSelect);
    }else{
        searchbean.setSearchType(searchObjectType);
    }

    //String objectType = searchObjectType;
    if(isItemMasterPicker) {
        objectType = "wt.fc.Persistable";
        String context = request.getParameter("contextPicker"+SearchWebConstants.HIDDEN_OID_STRING);
        if(context!=null && context.length()>0)searchbean.getHmSearchAttr().put(SearchWebConstants.KEY_CONTAINER_TYPE_LIST, context);
    }

    String typeComponentId = request.getParameter("typeComponentId");
    String typePickerObjectList = request.getParameter("typePickerObjectList");
      if(typeComponentId == null) {
        if("ItemPicker".equals(pickerName) && typePickerObjectList == null )
            typeComponentId = "Foundation.itemPicker";
        else if(isItemMasterPicker)
            typeComponentId = "Foundation.itemMasterPicker";
        else
            typeComponentId = "";
    }
    request.setAttribute("typeComponentId", typeComponentId);
    request.setAttribute("typePickerObjectList", typePickerObjectList);
//out.println("<BR> --- objectype="+objectType);
    request.setAttribute("objectType", "false".equals(singleObjectSelect) ? objectType : searchObjectType);
    request.setAttribute("searchObjectType", searchObjectType);
    //request.setAttribute("searchType", searchbean.getSearchType());
    request.setAttribute("searchType", "false".equals(singleObjectSelect)  ? searchbean.getSearchType() : searchObjectType);
%>

<tr>
<td>
<Input type="hidden" name="showTypePicker" value="<%=showTypePicker%>" />
<Input type="hidden" name="pickerName" value="<%=HTMLEncoder.encodeForHTMLAttribute(pickerName)%>" />
<Input type="hidden" name="typeComponentId" value="<%=HTMLEncoder.encodeForHTMLAttribute(typeComponentId)%>" />
</td>
</tr>

<%
    if(showTypePicker){
%>
<%@ include file="/netmarkets/jsp/search/typeChooser.jsp"%>
<%
    }
%>


