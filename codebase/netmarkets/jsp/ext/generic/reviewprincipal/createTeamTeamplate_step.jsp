<%@page import="wt.httpgw.URLFactory"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="wt.session.SessionHelper"%>
<%@page import="java.util.*"%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf"%>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf"%>
<%@taglib uri="http://www.ptc.com/windchill/taglib/jcaMvc" prefix="jcaMvc"%>
<%@ taglib prefix="wctags" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>
<%@ taglib prefix="jca" uri="http://www.ptc.com/windchill/taglib/components"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/wrappers" prefix="w"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<div style="width: auto; height: auto;">
		<table border="0" class="attributePanel-group-panel" id="tab">
		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp;*模板名称：</td>
			 <td>
			<w:textBox propertyLabel="" id="teamTemplateName" name="teamTemplateName"  size="30" styleClass="required" required="true" maxlength="200"/>
			 </td>
		  </tr>

		  <tr>
			<td>&nbsp;</td>
			<td class="attributePanel-label">&nbsp;&nbsp; 是否默认：</td>
			 <td>
			 <w:checkBox id="isDefault" name="isDefault" checked="false"/>
			 </td>
		  </tr>

		 </table>
</div>


<script language="Javascript">
	Ext.onReady(function() {
		
		if (navigator.userAgent.indexOf("MSIE") != -1) {
	         document.getElementById("navType").value="IE";
	    } else if (navigator.userAgent.indexOf("Firefox") != -1 || navigator.userAgent.indexOf("Mozilla") != -1) {
	         document.getElementById("navType").value="Firefox";
	    }
	});
</script>
<%@ include file="/netmarkets/jsp/util/end.jspf"%>