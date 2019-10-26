
<%@page import="com.ptc.mvc.util.ClientMessageSource"%>
<%@page import="com.ptc.mvc.util.ResourceBundleClientMessageSource"%>
<%@page import="ext.appo.part.resource.PartResourceRB"%>
<%@page import="ext.appo.part.workflow.PartWorkflowUtil"%>
<%@page pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="jca"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>
<%@ taglib prefix="wrap" uri="http://www.ptc.com/windchill/taglib/wrappers"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>
<%@ taglib prefix="wctags" tagdir="/WEB-INF/tags"%>
<%@page import="java.util.*"%>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf"%>
<%@ page import="com.ptc.netmarkets.util.beans.NmCommandBean"%>
<%@ page import="com.ptc.netmarkets.model.NmOid"%>
<jsp:useBean id="nmcontex"
	class="com.ptc.netmarkets.util.beans.NmContextBean" />

<fmt:message var="TITLE" key="PRIVATE_CONSTANT_11"/>
<fmt:message var="BELONTOPRODUCTLINE" key="BELONTOPRODUCTLINE"/>
<fmt:message var="BELONTOGPROJECT" key="BELONTOGPROJECT"/>
<fmt:message var="ADDTABLECELL" key="ADDTABLECELL"/>
<fmt:message var="DELECTTABLECELL" key="DELECTTABLECELL"/>
	
	

<%
	NmCommandBean cb = new NmCommandBean();
  cb.setCompContext(nmcontex.getContext().toString());
  cb.setRequest(request);
 
  PartWorkflowUtil partWorkflowUtil = new PartWorkflowUtil();
  String reslut = partWorkflowUtil.getProductLineAndProject(cb);
  String productline = "";
  String project = "";
  if(reslut.contains(";")){
  	String[] value = reslut.split(";");	
  	productline = value[0];
  	project = value[1];
  }
ClientMessageSource messageSource = new ResourceBundleClientMessageSource(PartResourceRB.class.getName());
String TITLE = messageSource.getMessage(PartResourceRB.PRIVATE_CONSTANT_11);
String BELONTOPRODUCTLINE = messageSource.getMessage(PartResourceRB.BELONTOPRODUCTLINE);
String BELONTOGPROJECT = messageSource.getMessage(PartResourceRB.BELONTOGPROJECT);
String ADDTABLECELL = messageSource.getMessage(PartResourceRB.ADDTABLECELL);
String DELECTTABLECELL = messageSource.getMessage(PartResourceRB.DELECTTABLECELL);
%>  

	<table align="center"  valign="center"> 
	  <tr>
         <td><%=BELONTOPRODUCTLINE%>:
         		 <select name="productLine" id="productLine" style="width:100px;" onChange="change()" disabled="disabled">
							<option></option>
					   <%
								PartWorkflowUtil util = new PartWorkflowUtil();
								Map<ArrayList<String>, ArrayList<String>> values = util.getEnumeratedMap("sscpx");
								for (Map.Entry<ArrayList<String>, ArrayList<String>> entry : values.entrySet()) {  
                   ArrayList<String> insideName = entry.getKey();
                   ArrayList<String> displayName = entry.getValue(); 
									 for(int i=0; i < insideName.size(); i++){
			                 String insideValue = insideName.get(i);
			                 String displayValue = displayName.get(i);     
					    %>
					         <option value="<%=insideValue%>"><%=displayValue%></option>
					    <%
						       }
						  }
					    %>
			       </select>	
         </td>
        <td>&nbsp;&nbsp;&nbsp;</td>
        <td><%=BELONTOGPROJECT%>:
         	 		<select name="project" id="project" style="width:130px;" disabled="disabled">
						<option value="<%=project%>"><%=project%></option>
					</select>	
         	
         </td>
    </tr>	
  </table>

<script type="text/javascript">
	
	var table_id = 'ext.appo.part.builder.ProductNamingNoticTableBuilder';
		
(function(){
			document.getElementById("productLine").value="<%=productline%>";
	   // document.getElementById("project").value= "<%=project%>";
	    change();
	})()
		
		
		
		function getXhr() {
		var xhr = null;
		if (window.XMLHttpRequest) {
			
			xhr = new XMLHttpRequest();
		} else {
			xhr = new ActiveXObject('MicroSoft.XMLHttp');
		}
		return xhr;
	}
	
function change() {
  var xhr = getXhr();
  var productLine = document.getElementById("productLine").value;
  
  	xhr.open('post',
				'netmarkets/jsp/ext/appo/part/partProductNamingNotice/readExcelData.jsp',
				true);
		xhr.setRequestHeader('content-type',
				'application/x-www-form-urlencoded');

		xhr.onreadystatechange = function() {

			if (xhr.readyState == 4) {//必须等待ajax对象获取了服务器返回的所有数据
				if (xhr.status == 200) {

					var txt = xhr.responseText;		
						if(txt.trim().startsWith('选')){
						     alert(txt.trim());
                 return;
						}else{
							 document.getElementById("attributers").value = txt.trim();
								var mform = getMainForm(); 
                var url="netmarkets/jsp/ext/appo/part/partProductNamingNotice/notice.jsp?productLine="+encodeURIComponent(productLine)+
    	  				"&run=1";  
                getElementHtml(Form.serialize(mform),'ext.appo.part.builder.ProductNamingNoticTableBuilder', true, url,false);
						}
					

				}
			}
		};
		xhr.send('productLine=' + productLine);
 
}


function ajaxRequest(url, params) {
		var options = {
			asynchronous : false,
			parameters : params,
			method : 'POST'
		};
		var transport = requestHandler.doRequest(url, options);
		return transport.responseText ;
	}


	function addPartData() {
		//var url = 'netmarkets/jsp/ext/appo/part/partProductNamingNotice/addRowsData.jsp';
		//var json = eval("("+ajaxRequest(url, '')+")");
		//var oid = json['oid'];
		//insertTableNewInfo(oid);
	 
		//var params = {doAjaxUpdate:true, preventDuplicates: true };
		//rowHandler.addRows(json['oid'],'ext.appo.part.builder.ProductNamingNoticTableBuilder', null, params);
		
		// 保存数据
		saveInputValue() ;
		
		// 获取页面存储的数据
		var datasArray = document.getElementById("datasArray").value ;
		// 重新加载表单，并添加需要复制的数据
		var params = {
			selectDatas : 'ADD' ,
			datasArray : datasArray
		};
		PTC.jca.table.Utils.reload(table_id, params, true);	
	}

	function copyAddPartData(){
		// 保存数据
		saveInputValue() ;
		
		var selectDatas = [] ;
		// 获取用户选择的数据
		var selections = tableUtils.getTable(table_id).getSelectionModel().getSelections();
		if (!selections || selections.length === 0) {
			JCAAlert('com.ptc.netmarkets.util.utilResource.NONE_CHECKED');
			return ;
		}else{
			for ( var i = 0, l = selections.length; i < l; i++) {
				selectDatas.push(selections[i].get('oid'));
			}
		}
		// 获取页面存储的数据
		var datasArray = document.getElementById("datasArray").value ;
		// 重新加载表单，并添加需要复制的数据
		var params = {
			selectDatas : JSON.stringify(selectDatas) ,
			datasArray : datasArray
		};
		PTC.jca.table.Utils.reload(table_id, params, true);	
	}

function insertTableNewInfo(objs){
        var rowid = [];
        rowid.push(objs);
        addRows(rowid, 'ext.appo.part.builder.ProductNamingNoticTableBuilder', false, true, true);
}

	function saveInputValue() {
		var attributers = document.getElementById("attributers").value;
 		var attr = attributers.split(",");
		var grid = window.PTC.jca.table.Utils.getTable(table_id);
		var store = grid.getStore();
		var resultJSON = {} ;
		store.each(function(record){
			var jsonData = {};
			var datas = record.data;
			for(var i=0; i<attr.length; i++){
				var val = attr[i].trim();
				if(datas.hasOwnProperty(val)){
					jsonData[val] = datas[val].gui.comparable;
				}
			}
			
			// 备注
			var bz = '' ;
			if(datas.hasOwnProperty('bz')){
				bz = datas['bz'].gui.comparable;
			}
			jsonData['bz'] = bz ;
			
			jsonData['partOid'] = datas.partOid ;
			resultJSON[datas.oid] = jsonData ;
		}); 
		document.getElementById("datasArray").value = JSON.stringify(resultJSON) ;
	}
	
	setUserSubmitFunction(saveInputValue) ;
 
	function delaySaveInputValue(){
		setTimeout("saveInputValue()",100);
	}

function removeSelectedRow(event){
	var removeArray = document.getElementById("removeArray").value;
	var removeJson;
	if(removeArray.length > 0){
		removeJson = eval("("+removeArray+")");
	}else{
		removeJson = {} ;
	}
  
  var tableId = tableUtils.findTableID(event);
  var table = tableUtils.getTable(tableId);
  
  var selectRows = table.getSelectionModel().getSelections();
  if (!selectRows || selectRows.length === 0) {
   JCAAlert('com.ptc.netmarkets.util.utilResource.NONE_CHECKED');
  } else {
   var soids = [];
   for ( var i = 0; i < selectRows.length; i++) {
    var row = selectRows[i];
    var rowId = getOidFromRowValue(row.id);
    var partOid = row.get('partOid'); 
    soids.push(rowId) ;
    removeJson[rowId] = partOid ;
   }
   PTC.jca.table.Utils.removeRows(table, soids);
  }
  document.getElementById("removeArray").value = JSON.stringify(removeJson) ;
  
 }
 
 function removeRowsFromTable(oids, tableId) {
  
  rowHandler.removeRows(oids, tableId, true);
  try {
   PTC.util.tableDataManager.remove(tableId, oids);
  } catch (e) {
   PTC.log.error("Error in netmarkets/javascript/purchaseRequisition/purchaseRequisition.js file : ",e);
  }
 }
 
	PTC.onReady(function() {
		setTimeout("saveInputValue()",1000);
	});
</script>
<jsp:include page="${mvc:getComponentURL('ext.appo.part.builder.ProductNamingNoticTableBuilder')}" flush="true"></jsp:include>
<%@include file="/netmarkets/jsp/util/end.jspf"%>

<input type="hidden" name="attributers" id="attributers"></input>
<input type="hidden" name="datasArray" id="datasArray"></input>
<input type="hidden" name="removeArray" id="removeArray"></input>
