var XMLHttpReq;

// 创建XMLHttpRequest对象
function createXMLHttpRequest() {
	// Mozilla 浏览器
	if (window.XMLHttpRequest) {
		XMLHttpReq = new XMLHttpRequest();
	}
	// IE浏览器
	else if (window.ActiveXObject) {
		try {
			XMLHttpReq = new ActiveXObject("Msxml2.XMLHTTP");
		} catch (e) {
			try {
				XMLHttpReq = new ActiveXObject("Microsoft.XMLHTTP");
			} catch (e) {
				
			}
		}
	}
}

// 发送请求函数
function sendRequest(url) {
	createXMLHttpRequest();
	/**
	 * 如果Open的第三个参数为true,则不等待服务端数据同步，就返回；
	 * 如果Open的第四个参数为false，则等待服务端数据同步完成，在返回。
	 */
	XMLHttpReq.open("POST", url, false);

	// 指定响应函数
	XMLHttpReq.onreadystatechange = processResponse;

	// 发送请求
	XMLHttpReq.send(null);
	
	return XMLHttpReq ;
}

// 处理返回信息函数
function processResponse(pnumber , pname , pview) {
	// 判断对象状态
	var info;
	
	if (XMLHttpReq.readyState == 4) {
		// 信息已经成功返回，开始处理信息XMLHttpReq.status == 200
		if (XMLHttpReq.status == 200) {		
			// 调用自定义方法解析返回值
			process(pnumber , pname , pview);
		} else {
			// 页面不正常，比较常见的错误是404，Request发送请求的页面不存在
			alert("您所请求的页面有异常:" + XMLHttpReq.status);
		}
	}
	
}

function process(pnumber , pname , pview) {
	// JSP页面返回一组结构为 <res>数据<res>的数据，这里只返回一条数据
	var res = XMLHttpReq.responseXML.getElementsByTagName("res");
	var number="";
	var name="";
	var view="";
	
	if( res != null && res.length > 0 ){
		number =  res[0].firstChild.nodeValue ;
		name =  res[1].firstChild.nodeValue ;
		view =  res[2].firstChild.nodeValue ;
		
		if( number != null ){
			var numberElement = document.getElementById(pnumber);
			if(numberElement!=null && typeof numberElement != 'undefined') {
				numberElement.value = unescape(number);
			}
		}
		
		if( name != null ){
			var nameElement = document.getElementById(pname);
			if(nameElement!=null && typeof nameElement != 'undefined') {
				nameElement.value = unescape(name);
			}
		}
		
		if( view != null ){
			var viewElement = document.getElementById(pview);
			if(viewElement!=null && typeof viewElement != 'undefined') {
				viewElement.value = unescape(view);
			}
		}
	}
}


function getInfo(oid , pnumber , pname , pview) {
		sendRequest("/Windchill/ptc1/netmarkets/jsp/ext/generic/masschange/queryPart.jsp?oid=" + oid);
		var info = processResponse(pnumber , pname , pview);
		return info;	
}
 
  function PartPickerCallback(objects, pickerID) {
   var partNumber = pickerID +"$label$"
   var partName = pickerID +"_NAME";
   var partView = pickerID +"_VIEW";
   var myJSONObjects = objects.pickedObject;
   for(var i=0; i< myJSONObjects.length;i++) {
     var oid = myJSONObjects[i].oid; 
     getInfo(oid , partNumber , partName , partView);
   }
 }
 


function clearBomValue() {
	document.cookie = "BOM_MASSCHANGE_TYPE=" + "";
				
	document.cookie = "MODIFIED_PART_NUMBER_VALUE=" + "";
	document.cookie = "MODIFIED_PART_NAME_VALUE=" + "";
	document.cookie = "MODIFIED_PART_VIEW_VALUE=" + "";
	
	document.cookie = "REFERENCE_PART_NUMBER_VALUE=" + "";
	document.cookie = "REFERENCE_PART_NAME_VALUE=" + "";
	document.cookie = "REFERENCE_PART_VIEW_VALUE=" + "";
	
	document.cookie = "QUANTITY=" + "";
	
	document.cookie = "SUBSTITUTED_PART_NUMBER_VALUE=" + "";
	document.cookie = "SUBSTITUTED_PART_NAME_VALUE=" + "";
	document.cookie = "SUBSTITUTED_PART_VIEW_VALUE=" + "";
	
	document.cookie = "ADD_SELECT_PARTS=" + "";
}


function clearSubstituteValue() {
	document.cookie = "SUBSTITUTE_MASSCHANGE_TYPE=" + "";
				
	document.cookie = "OLD_MAIN_MATERIAL_NUMBER_VALUE=" + "";
	document.cookie = "OLD_MAIN_MATERIAL_NAME_VALUE=" + "";
	document.cookie = "OLD_MAIN_MATERIAL_VIEW_VALUE=" + "";
	
	document.cookie = "NEW_MAIN_MATERIAL_NUMBER_VALUE=" + "";
	document.cookie = "NEW_MAIN_MATERIAL_NAME_VALUE=" + "";
	document.cookie = "NEW_MAIN_MATERIAL_VIEW_VALUE=" + "";
	
	document.cookie = "QUANTITY=" + "";
		
	document.cookie = "OLD_SUBSTITUTE_MATERIAL_NUMBER_VALUE=" + "";
	document.cookie = "OLD_SUBSTITUTE_MATERIAL_NAME_VALUE=" + "";
	document.cookie = "OLD_SUBSTITUTE_MATERIAL_VIEW_VALUE=" + "";
	
	document.cookie = "NEW_SUBSTITUTE_MATERIAL_NUMBER_VALUE=" + "";
	document.cookie = "NEW_SUBSTITUTE_MATERIAL_NAME_VALUE=" + "";
	document.cookie = "NEW_SUBSTITUTE_MATERIAL_VIEW_VALUE=" + "";
	
	document.cookie = "MAIN_MATERIAL_NUMBER_VALUE=" + "";
	document.cookie = "MAIN_MATERIAL_NAME_VALUE=" + "";
	document.cookie = "MAIN_MATERIAL_VIEW_VALUE=" + "";
			
	document.cookie = "SUBSTITUTE_MATERIAL_NUMBER_VALUE=" + "";
	document.cookie = "SUBSTITUTE_MATERIAL_NAME_VALUE=" + "";
	document.cookie = "SUBSTITUTE_MATERIAL_VIEW_VALUE=" + "";
		
}


