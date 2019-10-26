<%@page language="java" session="true" pageEncoding="UTF-8"%>
<%@ include file="/netmarkets/jsp/util/beginPopup.jspf"%>
<%@page import="ext.appo.ecn.beans.ChangeTaskBean"%>
<%@page import="org.json.JSONObject"%>
<%@page import="ext.appo.ecn.constants.ChangeConstants"%>
<%@ taglib prefix="mvc" uri="http://www.ptc.com/windchill/taglib/mvc"%>

<%
	// 更改主图
	String changeTheme = "";
	// 更改说明
	String changeDescribe = "";
	// 责任人
	String responsible = "";
	// eca对象Oid
	String changeActivity2 = "" ;
	// 表单中所有对象OID
	String allDatas = request.getParameter("allDatas");
	// 选择的对象
	String seleteOID = request.getParameter("seleteOID");
	if(seleteOID != null && !seleteOID.trim().isEmpty()){
		JSONObject allDatasJSONObject = new JSONObject(allDatas) ;
		if(allDatasJSONObject.has(seleteOID)){
			JSONObject jsonObject = new JSONObject(allDatasJSONObject.getString(seleteOID)) ;
			if(jsonObject.has(ChangeConstants.CHANGETHEME_COMPID)){
				changeTheme = jsonObject.getString(ChangeConstants.CHANGETHEME_COMPID) ;
			}
			if(jsonObject.has(ChangeConstants.CHANGEDESCRIBE_COMPID)){
				changeDescribe = jsonObject.getString(ChangeConstants.CHANGEDESCRIBE_COMPID);
			}
			if(jsonObject.has(ChangeConstants.RESPONSIBLE_COMPID)){
				responsible = jsonObject.getString(ChangeConstants.RESPONSIBLE_COMPID);
			}
			if(jsonObject.has(ChangeConstants.CHANGEACTIVITY2_COMPID)){
				changeActivity2 = jsonObject.getString(ChangeConstants.CHANGEACTIVITY2_COMPID);
			}
		}
	}
%>

<table>
  <tr>
    <td align="right" class="a_lable">*任务主题:</td>
	<td>
		<input type="text" class="a_input" name="changeTheme" id="changeTheme" value="<%=changeTheme%>"/>
	</td>
  </tr>
  <tr>
    <td align="right" class="a_lable">*责任人:</td>
	<td>
		<input type="text" class="a_input" name="responsible" id="responsible" onchange='checkUser(this);' value="<%=responsible%>"/>
	</td>
  </tr>
  <tr>
    <td align="right" class="a_lable">*任务描述:</FONT></td>
	<td>
		<textarea cols="50" rows="5" name="changeDescribe" id="changeDescribe"><%=changeDescribe%></textarea>
	</td>
  </tr>
  <tr>
	<td>
    <td align="right" class="a_div">
	    <input type="button" name="OK" id="OK" class="a_lable" value="确　定" onClick="determineEvent();"/>
	    &nbsp;
	    &nbsp;
	    &nbsp;
		<input type="button" name="Cancel" class="a_lable" id="Cancel" value="取　消" onClick="window.open('','_self');window.close();"/>
	</td>
  </tr>
</table>

<script type="text/javascript">
	var changeActivity2 = "<%=changeActivity2%>" ;
	var seleteOID = "<%=seleteOID%>" ;
	var allDatas =  eval('(' + '<%=allDatas%>' + ')') ;
	function determineEvent(){
		// 变更主题
		var changeTheme = document.getElementById("changeTheme").value; 
		// 责任人
		var responsible = document.getElementById("responsible").value; 
		// 变更任务描述
		var changeDescribe = document.getElementById("changeDescribe").value; 
		if(changeTheme.length == 0 || responsible.length == 0 || changeDescribe.length == 0){
			alert("请完成信息录入后在保存!") ;
			return ;
		}
		// 创建ChangeTaskBean对象
		var params = "changeTheme=" + changeTheme + "&responsible=" +  responsible + "&changeDescribe=" + changeDescribe + "&changeActivity2=" + changeActivity2 + "&seleteOID=" + seleteOID + "&allDatas=" + JSON.stringify(allDatas) ;
		var url = "netmarkets/jsp/ext/appo/changeNotice/transactionalChangeActivity2.jsp";
		var json = eval("("+window.opener.ajaxRequest(url, params)+")");
		//错误信息
		var errorInfo = json['message'];
        if(errorInfo.length != 0){
        	alert(errorInfo);
        	return;
        }
		window.opener.reloadTable(json['resultDatas']) ;
		window.close();
	}
	
	// 检查用户是否存在
	function checkUser(obj){
		var userName = obj.value ;
		if(userName.length == 0){
			alert('责任人不能为空!') ;
			return ;
		}
		var params = "userName=" + userName;
		var url = "netmarkets/jsp/ext/appo/changeNotice/searchUserPicker.jsp";
		var json = eval("("+window.opener.ajaxRequest(url, params)+")");
		//错误信息
		var errorInfo = json['message'];
        if(errorInfo.length != 0){
			obj.value = "" ;
        	alert(errorInfo);
        	return;
        }
		obj.value = json['resultDatas'] ;
	}
</script>
<style type="text/css">
	.a_div{
		position:absolute;
		right:10px;
		bottom:10px;
	}
	
	.a_lable{
		height:35px;
		width:120px;
		font-size:15px;
		font-weight:bold;
	}
	
	.a_input{
		height:35px;
		width:338px;
		font-size:11px;
	}
</style>
<%@ include file="/netmarkets/jsp/util/end.jspf"%>