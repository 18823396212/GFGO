
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
  String reslut = partWorkflowUtil.getProductLineAndProjectDiesplay(cb);
  System.out.println("reslut : " + reslut) ;
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
	<br></br>
	  <tr>
         <td>      
         	<%=BELONTOPRODUCTLINE%>:<%=productline%>
         </td>
        <td>&nbsp;&nbsp;&nbsp;</td>
         
         <td>
         	<%=BELONTOGPROJECT%>:<%=project%>
         </td>
    </tr>	
  </table>
  
<jsp:include page="${mvc:getComponentURL('ext.appo.part.builder.ShowProductNamingNoticTableBuilder')}" flush="true"></jsp:include>
<%@include file="/netmarkets/jsp/util/end.jspf"%>
