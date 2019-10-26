
 <%@ taglib prefix="jca" uri="http://www.ptc.com/windchill/taglib/components"%> 
 <%@ taglib prefix="w" uri="http://www.ptc.com/windchill/taglib/wrappers"%> 
 <%@ include file="/netmarkets/jsp/components/beginWizard.jspf" %> 
 <%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>


 <table> 


	<tr> 
	  <td scope="row" width="100" class="tableColumnHeaderfont" align="right">新文件夹名称:</td> 
	  <td class="tabledatafont" align="left">&nbsp; 
	     <w:textBox name="foldername" id="foldername" maxlength="100" size="20"/> 
	  </td> 
	</tr> 
 </table> 
 <%@include file="/netmarkets/jsp/util/end.jspf"%> 
