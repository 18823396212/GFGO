<%@page pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="jca"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/core" prefix="wc"%>

<div>
<br/><br/>
选择文件：<input type="file" name="file" id="file"/>
<br/><br/>
</div>
<script type="text/javascript" language="javascript">

	function validateFileName(){

		var filename = document.getElementById("file").value;
		//alert("filename:"+filename);
		if(filename.trim().length==0){
			alert("请选择excel文件！");
			return false;
		}
		if(filename.toLowerCase().indexOf(".xls") < 0 || filename.toLowerCase().indexOf(".xlsx") < 0){
			alert("选中文件格式不符，请选择excel文件");

			return false;

		}
		return true;

	}

</script>