<%@page pageEncoding="UTF-8" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="wca" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt" %>
<%@ include file="/netmarkets/jsp/components/beginWizard.jspf" %>
<%@ include file="/netmarkets/jsp/components/includeWizBean.jspf" %>
<%@ page import="com.ptc.netmarkets.model.NmOid" %>
<%@ page import="ext.appo.change.util.ECAReviewActivityUtil,wt.fc.Persistable,wt.project.Role" %>
<%@ page import="wt.workflow.engine.WfActivity" %>
<%@ page import="wt.workflow.engine.WfProcess" %>
<%@ page import="wt.workflow.work.WorkItem" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Map" %>
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

<%--<wca:wizard type="clerk" title="" buttonList="DefaultClerkButtonsApply">--%>
<wca:wizard type="clerk" title="" buttonList="ModifyClerkButtonsApply">
    <wca:wizardStep action="user" type="modify"/>
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

    var singleSelectRole = "";
    <%
    Map<String,Role> singleSelect = ECAReviewActivityUtil.isSingleSelect(commandBean);
    String roleStr = "";
    for (Role role : singleSelect.values()) {
        if(roleStr!=null){
            roleStr += ";"+role.toString();
            role.getDisplay(Locale.CHINA);
        }
    }
    if (roleStr.indexOf(";")==0){
        roleStr=roleStr.substring(1);
    }

%>
    singleSelectRole = "<%=roleStr%>";

    Ext.onReady(function () {
        PTC.jca.table.Utils.reload("ChooseOrganizationTable", null, true);
        PTC.jca.table.Utils.reload("ChooseWTUserTable", null, true);

        if (exist === true) {
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

    //确定按钮 判断当前角色是否只能单选
    function isSingleSelectSubmit() {
        var result = isSingleSelect();
        if (result == true) {
            javascript:onSubmit();
        }
    }

    //应用按钮 判断当前角色是否只能单选
    function isSingleSelectApply() {
        var result = isSingleSelect();
        if (result == true) {
            javascript:onApply();
        }
    }

    function isSingleSelect() {
        var rolecomb = document.getElementById("rolecomb");
        // 获取页面所有input控件
        var inputArray = document.getElementsByTagName("input");
        var radio = 0;
        //获取other页面单选列表选择人数
        for (var i = 0; i < inputArray.length; i++) {
            var input = inputArray[i];
            if (input.type == 'radio' && input.checked == true) {
                radio = parseInt(radio) + 1;
            }
        }
        //获取显示值
        var index = rolecomb.selectedIndex;//获取被选中的option的索引
        var showValue = rolecomb.options[index].text;//获取相应的option的内容
        var selectRows = PTC.jca.table.Utils.getTableSelectedRowsById('ChooseWTUserTable', false, false);
        //合并页面所有选择的人数
        var count = parseInt(radio) + parseInt(selectRows.length);
        if (singleSelectRole.indexOf(rolecomb.value) > -1 && count > 1) {
            alert(showValue + "选择的用户多于1位,请重新选择！");
            return false;
        }
        return true;
    }

</script>

<%@ include file="/netmarkets/jsp/util/end.jspf" %>

