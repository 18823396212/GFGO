<%@ page language="java" contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>

<jsp:include page="${mvc:getComponentURL('ext.generic.excel.check.builder.WorkFlowConfigurationInfoBuilder')}" flush="true"/>

<div style="padding-top:20px;padding-left:5%;font-size:14px" id="result">

</div>

<script type="text/javascript" language="javascript">

function clearAll(){
	var result = document.getElementById("result");
	result.innerHTML = "";
}

function checkSignature(infomation){
	var result = document.getElementById("result");
	result.innerHTML = infomation;
}


</script>
