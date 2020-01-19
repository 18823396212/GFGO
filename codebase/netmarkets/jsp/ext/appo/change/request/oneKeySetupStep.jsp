<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.*" %>
<%@ page import="ext.appo.change.util.EnumeratedMap" %>
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
    input[type=checkbox]{
        vertical-align:middle;
        margin-left: 4px;
    }
    .span_value{
        position: absolute;
        left:140px;
    }
</style>

<%
    Map<ArrayList<String>, ArrayList<String>> articleDisposeMap= EnumeratedMap.getEnumeratedMap("ArticleDispose");//在制处理措施枚举
//    Map<ArrayList<String>, ArrayList<String>> passageDisposeMap= EnumeratedMap.getEnumeratedMap("PassageDispose");//在途处理措施枚举
//    Map<ArrayList<String>, ArrayList<String>> inventoryDisposeMap= EnumeratedMap.getEnumeratedMap("InventoryDispose");//库存处理措施枚举
    // 内部名称
    ArrayList<String> keyArray = articleDisposeMap.keySet().iterator().next();
//    // 显示名称
//    ArrayList<String> displayArray = articleDisposeMap.get(keyArray);
//    //合并
//    ArrayList<String> disposeArray=new ArrayList<>();
//    for (int i = 0; i <keyArray.size() ; i++) {
//        if (displayArray.size()>=i){
//            String key=keyArray.get(i);
//            String displayName=displayArray.get(i);
//            disposeArray.add(key+";"+displayName);
//        }
//    }

    Map<ArrayList<String>, ArrayList<String>> productDisposeMap= EnumeratedMap.getEnumeratedMap("ProductDispose");//已出货成品处理措施枚举
    // 内部名称
    ArrayList<String> articleDisposeKeyArray=productDisposeMap.keySet().iterator().next();
//    // 显示名称
//    ArrayList<String> articleDisposeDisplayArray = productDisposeMap.get(articleDisposeKeyArray);
//    //合并
//    ArrayList<String> articleDisposeArray=new ArrayList<>();
//    for (int i = 0; i <articleDisposeKeyArray.size() ; i++) {
//        if (articleDisposeDisplayArray.size()>=i){
//            String key=articleDisposeKeyArray.get(i);
//            String displayName=articleDisposeDisplayArray.get(i);
//            articleDisposeArray.add(key+";"+displayName);
//        }
//    }

    Map<ArrayList<String>, ArrayList<String>> changeTypeMap= EnumeratedMap.getEnumeratedMap("ChangeType"); //类型枚举
    // 内部名称
    ArrayList<String> changeTypeKeyArray = changeTypeMap.keySet().iterator().next();
//    // 显示名称
//    ArrayList<String> changeTypeDisplayArray = changeTypeMap.get(changeTypeKeyArray);
//    //合并
//    ArrayList<String> changeTypeArray=new ArrayList<>();
//    for (int i = 0; i <changeTypeKeyArray.size() ; i++) {
//        if (changeTypeDisplayArray.size()>=i){
//            String key=changeTypeKeyArray.get(i);
//            String displayName=changeTypeDisplayArray.get(i);
//            changeTypeArray.add(key+";"+displayName);
//        }
//    }

%>

<div>
    <input type="checkbox" id="checkBox" name="checkBox" onclick="isChooseAll(this);"/>
    <span>全选/全不选</span>
</div>
<br/>
<div>
    <input type="checkbox" id="articleDispose" name="articleDispose"/>
    <span>在制处理措施:</span>
    <span class="span_value">
        <select name="selectArticleDispose" id="selectArticleDispose">
            <%
                for (String array:keyArray){
            %>
            <option value="<%=array%>">
        <%
            String[] str=array.split(";");
            if (str.length>1){
        %>
            <%=str[1]%>
            <%
            }else{
            %>
            <%=str[0]%>
            <%
                }
            %>
            </option>
        <%
            }
        %>
        </select>
    </span>

</div>
<br/>
<div>
    <input type="checkbox" id="passageDispose" name="passageDispose"/>
    <span>在途处理措施:</span>
    <span class="span_value">
        <select name="selectPassageDispose" id="selectPassageDispose">
        <%
            for (String array:keyArray){
        %>
        <option value="<%=array%>">
        <%
            String[] str=array.split(";");
            if (str.length>1){
        %>
            <%=str[1]%>
            <%
            }else{
            %>
            <%=str[0]%>
            <%
                }
            %>
            </option>
        <%
            }
        %>
    </select>
    </span>
</div>
<br/>
<div>
    <input type="checkbox" id="inventoryDispose" name="inventoryDispose"/>
    <span>库存处理措施:</span>
    <span class="span_value">
        <select name="selectInventoryDispose" id="selectInventoryDispose">
        <%
            for (String array:keyArray){
        %>
        <option value="<%=array%>">
        <%
            String[] str=array.split(";");
            if (str.length>1){
        %>
            <%=str[1]%>
            <%
            }else{
            %>
            <%=str[0]%>
            <%
                }
            %>
            </option>
        <%
            }
        %>
        </select>
    </span>
</div>
<br/>
<div>
    <input type="checkbox" id="productDispose" name="productDispose"/>
    <span>已出货成品处理措施:</span>
    <span class="span_value">
        <select name="selectProductDispose" id="selectProductDispose">
            <%
                for (String array:articleDisposeKeyArray){
            %>
            <option value="<%=array%>">
        <%
            String[] str=array.split(";");
            if (str.length>1){
        %>
            <%=str[1]%>
            <%
            }else{
            %>
            <%=str[0]%>
            <%
                }
            %>
            </option>
        <%
            }
        %>
        </select>
    </span>
</div>
<br/>
<div>
    <input type="checkbox" id="changeType" name="changeType"/>
    <span>类型:</span>
    <span class="span_value">
        <select name="selectChangeType" id="selectChangeType">
        <%
            for (String array:changeTypeKeyArray){
        %>
       <option value="<%=array%>">
        <%
            String[] str=array.split(";");
            if (str.length>1){
        %>
            <%=str[1]%>
            <%
            }else{
            %>
            <%=str[0]%>
            <%
                }
            %>
            </option>
        <%
            }
        %>
    </select>
    </span>
</div>
<br/>
<div>
    <input type="checkbox" id="completiontime" name="completiontime"/>
    <span style="margin-right: 20px;" >${completiontime}:</span>
    <span class="span_value">
        <w:dateInputComponent propertyLabel="DateInputComponent" id="completiontime" name="completiontime" required="true" dateValueType="DATE_ONLY"/>
    </span>
</div>
<br/>
<div>
    <input type="checkbox"id="userPicker" name="userPicker"/>
    <span style="margin-right: 53px;">责任人:</span>
    <span class="span_value">
        <wctags:userPicker id="userPicker" pickerTitle="搜索负责人" label="" readOnlyPickerTextBox="false"/>
    </span>
</div>
<br/>
<div>
    <input type="checkbox" id="aadDescription" name="aadDescription"/>
    <span>更改详细描述:</span>
    <span class="span_value">
        <w:textArea id="aadDescription" name="aadDescription" maxLength="10000" rows="4" cols="50"/>
    </span>
</div>


<%--<span style="margin-right: 20px;" >${completiontime}:</span>--%>
<%--<w:dateInputComponent propertyLabel="DateInputComponent" id="completiontime" name="completiontime" required="true" dateValueType="DATE_ONLY"/>--%>
<%--<br/>--%>
<%--<br/>--%>
<%--<span style="margin-right: 53px;" >责任人:</span>--%>
<%--<wctags:userPicker id="userPicker" pickerTitle="搜索负责人" label="" readOnlyPickerTextBox="true" />--%>

<script>
    function oneKeySetup() {
        var completiontimeTextbox = getMainForm().null___completiontime_col_completiontime___textbox;
        var completiontime=completiontimeTextbox.value;//期望完成时间
        var userPicker = document.getElementById('userPicker$label$').value;//责任人
        var articleDispose=selectValue("selectArticleDispose")//在制处理措施
        var passageDispose=selectValue("selectPassageDispose");//在途处理措施
        var inventoryDispose=selectValue("selectInventoryDispose")//库存处理措施
        var productDispose=selectValue("selectProductDispose")//已出货成品处理措施
        var changeType=selectValue("selectChangeType")//类型
        var aadDescriptionTextarea=getMainForm().null___aadDescription___textarea;
        var aadDescription=aadDescriptionTextarea.value;//更改详细描述
        // alert("completiontime=="+completiontime+"==userPicker=="+userPicker+"==articleDispose=="+articleDispose
        // +"==passageDispose=="+passageDispose+"==inventoryDispose=="+inventoryDispose+"==productDispose=="+productDispose
        // +"==changeType=="+changeType+"==aadDescription=="+aadDescription);
        var completiontime_isChoose=isChoose("completiontime");//是否勾选期望完成时间
        var userPicker_isChoose=isChoose("userPicker");//是否勾选责任人
        var articleDispose_isChoose=isChoose("articleDispose");//是否勾选在制处理措施
        var passageDispose_isChoose=isChoose("passageDispose");//是否勾选在途处理措施
        var inventoryDispose_isChoose=isChoose("inventoryDispose");//是否勾选库存处理措施
        var productDispose_isChoose=isChoose("productDispose");//是否勾选已出货成品处理措施
        var changeType_isChoose=isChoose("changeType");//是否勾选类型
        var aadDescription_isChoose=isChoose("aadDescription");//是否勾选更改详细描述
        // alert("completiontime_isChoose=="+completiontime_isChoose+"==userPicker_isChoose=="+userPicker_isChoose+"==articleDispose_isChoose=="+articleDispose_isChoose
        // +"==passageDispose_isChoose=="+passageDispose_isChoose+"==inventoryDispose_isChoose=="+inventoryDispose_isChoose+"==productDispose_isChoose=="+productDispose_isChoose
        // +"==changeType_isChoose=="+changeType_isChoose+"==aadDescription_isChoose=="+aadDescription_isChoose);
        //勾选赋值，没勾选赋""
        if (completiontime_isChoose==false) completiontime="";
        if (userPicker_isChoose==false) userPicker="";
        if (articleDispose_isChoose==false) articleDispose="";
        if (passageDispose_isChoose==false) passageDispose="";
        if (inventoryDispose_isChoose==false) inventoryDispose="";
        if (productDispose_isChoose==false) productDispose="";
        if (changeType_isChoose==false) changeType="";
        if (aadDescription_isChoose==false) aadDescription="";
        // alert("completiontime=="+completiontime+"==userPicker=="+userPicker+"==articleDispose=="+articleDispose
        // +"==passageDispose=="+passageDispose+"==inventoryDispose=="+inventoryDispose+"==productDispose=="+productDispose
        // +"==changeType=="+changeType+"==aadDescription=="+aadDescription);

        window.opener.addOneKeySetup(completiontime,userPicker,articleDispose,passageDispose,inventoryDispose,productDispose,changeType,aadDescription);
        window.close();
    }

    function isChooseAll(e) {
        var value=false;
        if (e.checked==true) {
            value=true;
        }
        //获取页面所有input控件
        var inputFormArray = document.getElementsByTagName("input");
        for (var i = 0; i < inputFormArray.length; i++) {
            var obj = inputFormArray[i];
            if (obj.type == 'checkbox') {
                obj.checked=value;
            }
        }
    }

    //通过select下拉框ID获取选中的值
    function selectValue(id){
        var obj = document.getElementById(id); //定位id
        var index = obj.selectedIndex; // 选中索引
        var text = obj.options[index].value; // 选中value值
        return text;
    }
    //通过checkbox得ID判断是否选择
    function isChoose(id){
        var result=false;
        var obj = document.getElementById(id);
        var isChoose = obj.checked;
        if (isChoose==true){
            result=true;
        }
        return result;
    }

</script>
<%@ include file="/netmarkets/jsp/util/end.jspf" %>
