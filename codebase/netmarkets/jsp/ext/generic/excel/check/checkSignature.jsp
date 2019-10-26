<%@page import="com.ptc.projectmanagement.plan.planResource" %>
<%@ page language="java" contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/wrappers" prefix="w"%>
<script type="text/javascript" language="javascript">

function clearAll(){
	var result = document.getElementById("result");
	result.innerHTML = "";
}

function checkSignature(){
	var pdfFilePath = document.getElementById("pdfFilePath").value;
	if(pdfFilePath == null || pdfFilePath == ""){
		alert("请选择PDF模版路径！");
		return;
	}
	var pat = /^.*(pdf|PDF)$/;
	if(pat.test(pdfFilePath) == false){
		alert("选择的文件不是以pdf结尾，请选择pdf模版。");
	}	
  document.forms[0].submit();
	
}

function callback(msg)   
{   
    var result = document.getElementById("result");
		result.innerHTML = msg; 
} 


</script>

<div id="search_div">

<form  id="readReportForm" action="netmarkets/jsp/ext/generic/excel/check/checked.jsp" method="post"
	 enctype="multipart/form-data" target="hidden_frame">
  <table border="0" align="center" cellspacing="10" cellpadding="0" width="50%">
		<tr>
			<td>
				PDF模版路径：
			</td>	
			<td>
				<input type="file" name="file" size="50" id="pdfFilePath"/>
			</td>
		</tr>
		
		<tr><td></td><td></td></tr>
		
		<tr>
			<td>
			
			</td>
			<td><w:button name="check" value="验证" onclick="checkSignature();"></w:button>
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<w:button name="clear" value="清空" onclick="clearAll();"></w:button>
			</td>
		</tr>
		<tr><td></td><td></td></tr>
	</table>
	<iframe name="hidden_frame" id="hidden_frame" style="display:none"></iframe>
</form> 
	
	

</div>

<br/>
<br/>

<div style="padding-top:20px;padding-left:25%;font-size:14px" id="result">

</div>