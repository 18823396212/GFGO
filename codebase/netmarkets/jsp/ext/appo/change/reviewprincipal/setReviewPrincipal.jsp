<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="wca" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt" %>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf" %>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf" %>
<%@ page import="com.ptc.netmarkets.model.NmOid" %>
<%@ page import="wt.fc.Persistable,wt.workflow.engine.WfActivity,wt.workflow.engine.WfProcess" %>
<%@ page import="wt.workflow.work.WorkItem" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/wrappers" prefix="w" %>

<fmt:setLocale value="${localeBean.locale}"/>
<fmt:setBundle basename="ext.generic.reviewprincipal.resource.ReviewPrincipalRB"/>
<fmt:setBundle basename="ext.appo.part.resource.BaselineResourceRB"/>
<fmt:message var="wizardLabel" key="SETPRINCIPAL"/>
<fmt:message var="roleStrvar" key="SELECTROLE"/>
<fmt:message var="message" key="message_35"/>

<div style="font-weight:bold;font-size:13pt;background-color:#FFFFFF;">
    <jsp:include page="/netmarkets/jsp/ext/appo/change/reviewprincipal/comboxRole.jsp" flush="true"/>
</div>

<wca:wizard type="clerk" title="" buttonList="DefaultClerkButtonsApply"><%-- DefaultClerkButtonsApply --%>
    <wca:wizardStep action="user" type="generic"/>
    <wca:wizardStep action="organization" type="generic"/>
    <wca:wizardStep action="other" type="generic"/>
</wca:wizard>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<script type="text/javascript">
    var exist = "ECN";
    <%

        NmOid contextNmOid = commandBean.getElementContext().getPrimaryOid();
            System.out.println("contextNmOid===" + contextNmOid);
                if (contextNmOid != null) {

                WorkItem workitem = (WorkItem) contextNmOid.getRefObject();

                WfActivity wfactivity = (WfActivity) workitem.getSource().getObject();

                Persistable pbo = workitem.getPrimaryBusinessObject().getObject();

                WfProcess wfprocess = wfactivity.getParentProcess();
                System.out.println("wfprocess===" + wfprocess);
                System.out.println("wfprocess.getName()===" + wfprocess.getName());
                  if(wfprocess.getName().contains("ECN")) {
    %>
    exist = true;
    <%
    }
}
%>


    Ext.onReady(function () {
        PTC.jca.table.Utils.reload("ChooseOrganizationTable", null, true);
        PTC.jca.table.Utils.reload("ChooseWTUserTable", null, true);

        if (exist == true) {

            alert("${message}");
        }
    });

    function changeDepart(obj) {
        obj.style.display = "none";
        PTC.jca.table.Utils.reload("ChooseOrganizationTable", null, true);
        PTC.jca.table.Utils.reload("ChooseWTUserTable", null, true);
        setTimeout(function () {
            obj.style.display = "block";
        }, 700);
    }
</script>

<%@ include file="/netmarkets/jsp/util/end.jspf" %>

