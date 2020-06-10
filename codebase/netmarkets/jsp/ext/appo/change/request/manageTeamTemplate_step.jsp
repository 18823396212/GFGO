<%@ page import="ext.appo.change.beans.TeamTemplateBean" %>
<%@ page import="ext.appo.change.util.TeamTemplateUtil" %>
<%@ page import="ext.pi.core.PIPrincipalHelper" %>
<%@ page import="wt.org.WTPrincipal" %>
<%@ page import="wt.org.WTUser" %>
<%@ page import="wt.session.SessionHelper" %>
<%@ page import="java.util.List" %>
<%@page pageEncoding="UTF-8" %>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<%
    //取路径
    wt.httpgw.URLFactory urlFactory = new wt.httpgw.URLFactory();
    String baseUrl = urlFactory.getBaseHREF();
    String expandImageUrl = baseUrl + "netmarkets/images/column_expand.gif";
    String collapseImageUrl = baseUrl + "netmarkets/images/column_collapse.gif";

    String workItemOid = request.getParameter("oid");
    List<TeamTemplateBean> teamTemplateBeans = TeamTemplateUtil.getTeamTemplateInfosLimitRole(workItemOid);
    // 获取当前用户
    WTPrincipal principal = SessionHelper.manager.getPrincipal();
    WTUser user = (WTUser) principal;
//    //是否管理员，管理员可以修改所有模板是否显示，共享；其他角色只能修改自己创建的模板
//    boolean flag = PIPrincipalHelper.service.isOrganizationAdministrator(user);

%>
<style>
    img:hover {
        color: red;
        cursor: pointer;
    }

    .tb {
        width: 99%;
        table-layout: fixed;
        border-collapse: collapse;
        line-height: 30px;
        white-space: normal;
    }

    .title {
        background: #D2E1FD;
        text-align: center;
    }

    .data {
        text-align: center;
    }
</style>
<head>
    <title>管理模板</title>
</head>
<body>
<br/>
<input type="hidden" id="expandImageUrl" value="<%=expandImageUrl%>">
<input type="hidden" id="collapseImageUrl" value="<%=collapseImageUrl%>">
<div style="float: left;line-height: normal;"><img id="baseInfo" src="<%=collapseImageUrl%>" onclick="showInfo(this)"/>
</div>
<h3>管理模板</h3>
<div id="templateDiv">
    <table class="tb" border="1">
        <tr>
            <th class="title" scope="col" NOWRAP colspan="1">
                <span>序号</span>
            </th>
            <th class="title" scope="col" NOWRAP colspan="3">
                <span>名称</span>
            </th>
            <th class="title" scope="col" NOWRAP colspan="2">
                <span>显示</span>
            </th>
            <th class="title" scope="col" NOWRAP colspan="2">
                <span>共享</span>
            </th>
            <th class="title" scope="col" NOWRAP colspan="3">
                <span>创建者</span>
            </th>
            <th class="title" scope="col" NOWRAP colspan="4">
                <span>创建时间</span>
            </th>
        </tr>
        <%
            if (teamTemplateBeans != null && teamTemplateBeans.size() > 0) {
                for (int i = 0; i < teamTemplateBeans.size(); i++) {
                    TeamTemplateBean teamTemplateBean = teamTemplateBeans.get(i);
                    String showTemplate = teamTemplateBean.getShowTemplate();
                    String shareTemplate = teamTemplateBean.getShareTemplate();
                    String templateOid = teamTemplateBean.getTemplateOid();
                    String creator = teamTemplateBean.getCreator();
                    String createData = teamTemplateBean.getCreateData();
                    WTUser wtUser = teamTemplateBean.getUser();
        %>
        <tr>
            <td class="data" colspan="1"><%=i + 1%>
            </td>
            <td class="data" colspan="3"><%=teamTemplateBean.getTemplateName()%>
            </td>
            <td class="data" colspan="2">
                <%
                    if (showTemplate != null && showTemplate.contains("true")) {
                %>
                <input type="checkbox" id="show_<%=templateOid%>" name="show"
                       value="<%=templateOid%>;<%=showTemplate%>;show;editable" checked/>
                <%
                } else {
                %>
                <input type="checkbox" id="show_<%=templateOid%>" name="show"
                       value="<%=templateOid%>;<%=showTemplate%>;show;editable"/>
                <%
                    }
                %>
            </td>
            <td class="data" colspan="2">
                <%
                    if (shareTemplate != null && shareTemplate.contains("true")) {
                        if (wtUser != null && wtUser.getIdentity().equals(user.getIdentity())) {
                %>
                <input type="checkbox" id="share_<%=templateOid%>" name="share"
                       value="<%=templateOid%>;<%=shareTemplate%>;share;editable" checked/>
                <%
                } else {
                %>
                <input type="checkbox" id="share_<%=templateOid%>" name="share"
                       value="<%=templateOid%>;<%=shareTemplate%>;share;disabled" checked disabled/>
                <%
                    }
                } else {
                    if (wtUser != null && wtUser.getIdentity().equals(user.getIdentity())) {
                %>
                <input type="checkbox" id="share_<%=templateOid%>" name="share"
                       value="<%=templateOid%>;<%=shareTemplate%>;share;editable"/>
                <%
                } else {
                %>
                <input type="checkbox" id="share_<%=templateOid%>" name="share"
                       value="<%=templateOid%>;<%=shareTemplate%>;share;disabled" disabled/>
                <%
                        }
                    }
                %>
            </td>
            <td class="data" colspan="3"><%=creator%>
            </td>
            <td class="data" colspan="4"><%=createData%>
            </td>
        </tr>
        <%
                }
            }
        %>
    </table>
</div>
<input type="hidden" name="saveData" id="saveData"/>
</body>
<script type="text/javascript">
    //展示当前信息
    function showInfo(e) {
        var expandImageUrl = document.getElementById("expandImageUrl").value;
        var collapseImageUrl = document.getElementById("collapseImageUrl").value;
        var display = document.getElementById("templateDiv").style.display;
        if (display.trim() == "" || display == "block") {
            e.src = expandImageUrl;
            document.getElementById("templateDiv").style.display = "none";
        } else {
            e.src = collapseImageUrl;
            document.getElementById("templateDiv").style.display = "block";
        }
    }

    function submitSaveData() {
        var shows = document.getElementsByName("show");
        var shares = document.getElementsByName("share");
        // 用于存储所有数据
        var tableRowArry = [];
        for (let i = 0; i < shows.length; i++) {
            var show = shows[i];
            var showValue = show.value;
            if (showValue.indexOf('disabled') > -1) {
                //不可编辑，不做修改
                continue;
            }
            if (show.checked == true) {
                showValue += ";true";
            } else {
                showValue += ";false";
            }
            tableRowArry.push(showValue);
        }
        for (let i = 0; i < shares.length; i++) {
            var share = shares[i];
            var shareValue = share.value;
            if (shareValue.indexOf('disabled') > -1) {
                //不可编辑，不做修改
                continue;
            }
            if (share.checked == true) {
                shareValue += ";true";
            } else {
                shareValue += ";false";
            }
            tableRowArry.push(shareValue);
        }
        document.getElementById("saveData").value = JSON.stringify(tableRowArry);
    }

    setUserSubmitFunction(submitSaveData);

</script>
