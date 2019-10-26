<%@page pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="jca"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>
<%@taglib uri="http://www.ptc.com/windchill/taglib/jcaMvc" prefix="jcaMvc"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ include file="/netmarkets/jsp/util/begin.jspf"%>
<table border="0">
	<tr>
		<td nowrap="nowrap">&nbsp;&nbsp;&nbsp;&nbsp;</td>
		<td align="right" nowrap="nowrap"><strong>*产品结构搭建文件(Excel)：</strong><input type="file" name="txtFile" id="txtFile" size="40" /></td>
		<td nowrap="nowrap">&nbsp;</td>
		<td nowrap="nowrap"><br><a style="color:red" href='/Windchill/netmarkets/jsp/ext/appo/part/BuildStruct.xlsx' target='_blank'>点击此处下载模板文件</a></td>
	</tr>
</table>
<SCRIPT LANGUAGE="JavaScript">
function uploadFileValidate() {
    var eleFile=window.document.forms.mainform.txtFile;
    if(!eleFile) {
        wfalert("请选择文件!");
        return false;
    } 
    
    var file_value= eleFile.value;
    file_value = trim(file_value);
    if(file_value == null || file_value == ""){
        alert("请选择文件!");
        eleFile.focus();
        return false;
    }

    if(file_value != null && file_value!=""){
        var index=file_value.lastIndexOf(".");
        var k = file_value.substring(index+1);
       if(k.toLowerCase() == "xls" || k.toLowerCase() == "xlsx"){   
            return true;
       }else{
          alert("导入文件必须是Excel文件!");
          return false;
      }
    }
    return true;
}

function validateInput() {
    return uploadFileValidate();
}
setUserSubmitFunction(validateInput);
</SCRIPT>
<%@ include file="/netmarkets/jsp/util/end.jspf"%>