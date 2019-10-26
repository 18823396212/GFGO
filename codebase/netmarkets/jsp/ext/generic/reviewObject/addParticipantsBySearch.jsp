<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ taglib prefix="w" uri="http://www.ptc.com/windchill/taglib/wrappers"%>
<%@ taglib prefix="jca" uri="http://www.ptc.com/windchill/taglib/components"%>
<%@taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>
<fmt:setBundle basename="ext.generic.wfaugment.processors.processorsResource" />
<fmt:message var="key1"    key="SEARCH_WFAUGMENT" />
<fmt:message var="key2"    key="SEARCH" />
<fmt:message var="key3"    key="CONFIRM" />
<fmt:message var="key4"    key="CANCLE" />
<SCRIPT LANGUAGE="JavaScript">
function clickCompleteButton() {
	 var name = document.getElementById('text1').value;
	 var loader = new ajax.ContentLoader("netmarkets/jsp/ext/generic/part/search.jsp", null, null, 'POST', 'name='+name);
	 var resText = null;
	if (loader.req.readyState == 4 && loader.req.status == 200) {
        resText = loader.req.responseText;
		var names = eval(resText);
		var ok=document.getElementsByName("ck");
		var tb=document.getElementById("tb");
		for(var k=0;k<ok.length;k++)
		{
			
			tb.deleteRow(k);
			k=k-1;
		}
		for(var i=0;i<names.length;i++)
		{
			var name = names[i]["name"];
			var val = names[i]["val"];
			add(tb,name,i,val);
		}
    }
}

 function add(tb,name,i,val)  
 {
   var mytr=tb.insertRow();
   mytr.setAttribute("id","r"+i);  
   var mytd_1=mytr.insertCell();  
   var mytd_2=mytr.insertCell();  
   mytd_1.innerHTML="<input type='checkbox' name='ck' value='"+val+";"+name+"'>";  
   mytd_2.innerText=name;  
 }
 
 function bcancel()
{
	window.close();
}

function bconfirm()
 {
	 var names = document.getElementsByName("ck");
	 var count = 0;
	 var temp = 0;
	 for(var i=0;i<names.length;i++)
	 {
		//通过遍历对象来每个对象看被选中的个数
		if(names[i].checked == true)
		{
			count++;
		}
	 }
	 var message = new Array(count);
	 for(var i=0;i<names.length;i++)
	 {
		if(names[i].checked == true)
		{
			message[temp] = names[i].value;
			temp++;
		}
	 }

	 window.returnValue = message;
	 self.close();
	 return;
 }
</SCRIPT>
<center>
<table>
	<tr>
		<td>
			<w:label value="${key1}:"></w:label>
			<w:textBox name="text" id="text1" value=""></w:textBox>
			<w:button name="submit" value="${key2}" onclick="javascript:clickCompleteButton()"></w:button>
		</td>
	</tr>
</table>

<table id="tb">
</table>
	<w:button name="confirm" value="${key3}" onclick="bconfirm()"></w:button>
	<w:button name="cancel" value="${key4}" onclick="bcancel()"></w:button>
</center>  	
<%@ include file="/netmarkets/jsp/util/end.jspf"%>