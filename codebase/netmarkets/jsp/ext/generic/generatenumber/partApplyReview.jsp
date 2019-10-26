<!-- customization NO.1 begin -->            
<tr><td></td><td>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/carambola" prefix="cmb"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="jca"%>

<%@ page import="java.util.Vector"%>
<%@ page import="wt.workflow.work.WorkItem"%>
<%@ page import="wt.fc.ObjectReference"%>
<%@ page import="wt.part.WTPart,wt.session.SessionServerHelper"%>

<fmt:setLocale value="${localeBean.locale}"/>

<%
         //获取PBO
         NmOid wfItemOid = commandBean.getActionOid();
         WorkItem aWorkItem = (WorkItem)ObjectReference.newObjectReference(wfItemOid.getOid()).getObject();
         System.out.println(" part apply workflow-------aWorkItem = "+aWorkItem);

         WTPart part = (WTPart)aWorkItem.getPrimaryBusinessObject().getObject();
         System.out.println("part apply workflow-------pbo = "+ part.getIdentity());
         //获取table显示所需的IBA属性名称
         Vector IBANames= ext.generic.generatenumber.util.GenerateNumberUtil.getIBANamesForTableDisplay(part);
         
%>

<%
	boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
%>
<c:set var="pbo" value="<%=part%>"/>
<c:set var="ClassNodeIBANames" value="<%=IBANames%>"/>
	
         	
         
    <jca:describeTable var="partTableDescriptor" 
                              type="wt.part.WTPart" 
                              id="classificationNodeParts" 
                              label="Same Part">
	   <jca:describeColumn id="smallThumbnail" sortable="false"/>
       <jca:describeColumn id="number" /> 
       <jca:describeColumn id="name" />
        <jca:describeColumn id="partType" />
        <jca:describeColumn id="source" />
         <jca:describeColumn id="state" />
       <jca:describeColumn id="nmActions"/>
       <c:forEach var="ibaName" items="${ClassNodeIBANames}">
                            <jca:describeColumn id="${ibaName}" sortable="true"/>
                </c:forEach>

    </jca:describeTable>
    
    <jca:getModel var="partTableModel" 
                         descriptor="${partTableDescriptor}" 
                         serviceName="ext.generic.generatenumber.util.GenerateNumberUtil"
                                        methodName="getSameParts">
                   <jca:addServiceArgument value="${pbo}" type="wt.part.WTPart"/>
           </jca:getModel>
         <jca:renderTable model="${partTableModel}" useJSCA="true" showCount="true" scroll="false" fullListLimit = "-1"/>
         
    
<%
	SessionServerHelper.manager.setAccessEnforced(flag);
%>

</td></tr>                           
<!-- review table end -->
