<%@ page import="ext.appo.change.beans.ChangeTaskInfoBean" %>
<%@ page import="ext.appo.change.report.EcnChangeTaskService" %>
<%@ page import="wt.change2.WTChangeOrder2" %>
<%@ page import="wt.fc.Persistable" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="static ext.appo.change.constants.ModifyConstants.TASK_3" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/wrappers" prefix="w" %>
<%@ taglib prefix="wctags" tagdir="/WEB-INF/tags" %>
<%@ page contentType="text/html; charset=UTF-8" %>

<%
    //取路径
    wt.httpgw.URLFactory urlFactory = new wt.httpgw.URLFactory();
    String baseUrl = urlFactory.getBaseHREF();
    String expandImageUrl = baseUrl + "netmarkets/images/column_expand.gif";
    String collapseImageUrl = baseUrl + "netmarkets/images/column_collapse.gif";

    //通过oid获取流程项->流程
    String oid = request.getParameter("oid");
    WTChangeOrder2 ecn = null;
    List<ChangeTaskInfoBean> mergeBean = new ArrayList<>();
    Persistable persistable = EcnChangeTaskService.getObjectByOid(oid);
    if (persistable instanceof WTChangeOrder2) {
        ecn = (WTChangeOrder2) persistable;
    }
    if (ecn != null) {
        //获取事务性任务(所有eca和未启动eca暂存的事务性任务)
        mergeBean = EcnChangeTaskService.getEcnChangeTask(ecn);
    }
%>
<style>
    img:hover {
        color: red;
        cursor: pointer;
    }

    .div_text {
        margin-left: 2px;
    }

    .all_div {
        box-sizing: border-box;
        border: #B9D0FE 1px solid;
        align-items: center;
        display: flex;
        /*justify-content:center;*/
        /*padding: 0;*/
    }

    .all_div_center {
        box-sizing: border-box;
        border: #B9D0FE 1px solid;
        align-items: center;
        display: flex;
        justify-content: center;
        /*padding: 0;*/
    }

    .div_title1 {
        float: left;
        width: 3%;
        height: 30px;
        background: #EAF0FB;
    }

    /*.div_title2 {*/
    /*    float: left;*/
    /*    width: 2%;*/
    /*    height: 30px;*/
    /*    background: #EAF0FB;*/
    /*}*/

    .div_title3 {
        float: left;
        width: 10%;
        height: 30px;
        background: #EAF0FB;
    }

    .div_title4 {
        float: left;
        width: 13%;
        height: 30px;
        background: #EAF0FB;
    }

    .div_title5 {
        float: left;
        width: 11%;
        height: 30px;
        background: #EAF0FB;
    }

    .div_title6 {
        float: left;
        width: 15%;
        height: 30px;
        background: #EAF0FB;
    }

    .div_title7 {
        float: left;
        width: 12%;
        height: 30px;
        background: #EAF0FB;
    }

    .div_title8 {
        float: left;
        width: 15%;
        height: 30px;
        background: #EAF0FB;
    }

    .div_title9 {
        float: left;
        width: 3%;
        height: 30px;
        background: #EAF0FB;
    }

    .div_title10 {
        float: left;
        width: 8%;
        height: 30px;
        background: #EAF0FB;
    }

    .div_title11 {
        float: left;
        width: 10%;
        height: 30px;
        background: #EAF0FB;
    }

    .div_content1 {
        float: left;
        width: 3%;
        height: 30px;
    }

    /*.div_content2 {*/
    /*    float: left;*/
    /*    width: 2%;*/
    /*    height: 30px;*/
    /*}*/

    .div_content3 {
        float: left;
        width: 10%;
        height: 30px;
    }

    .div_content4 {
        float: left;
        width: 13%;
        height: 30px;
    }

    .div_content5 {
        float: left;
        width: 11%;
        height: 30px;
    }

    .div_content6 {
        float: left;
        width: 15%;
        height: 30px;
    }

    .div_content7 {
        float: left;
        width: 12%;
        height: 30px;
    }

    .div_content8 {
        float: left;
        width: 15%;
        height: 30px;
    }

    .div_content9 {
        float: left;
        width: 3%;
        height: 30px;
    }

    .div_content10 {
        float: left;
        width: 8%;
        height: 30px;
    }

    .div_content11 {
        float: left;
        width: 10%;
        height: 30px;
    }

</style>
<input type="hidden" id="expandImageUrl" value="<%=expandImageUrl%>">
<input type="hidden" id="collapseImageUrl" value="<%=collapseImageUrl%>">
<input type="hidden" id="ecn" name="ecn" value="<%=ecn.getNumber()%>">
<input type="hidden" id="oid" name="ecn" value="<%=oid%>">
<div style="display:inline-block;width: 99%">
    <div style="float: left;line-height: normal;"><img id="baseInfo" src="<%=collapseImageUrl%>"
                                                       onclick="showInfo(this)"/></div>
    <h3>*事务性任务</h3>
    <div id="taskDiv">
        <div>
            <div class="all_div_center div_title1">
                <span>序号</span>
            </div>
            <div class="all_div_center div_title3">
                <span>*任务类型</span>
            </div>
            <div class="all_div_center div_title4">
                <span>*任务主题</span>
            </div>
            <div class="all_div_center div_title5">
                <span>管理方式</span>
            </div>
            <div class="all_div_center div_title6">
                <span>*任务描述</span>
            </div>
            <div class="all_div_center div_title7">
                <span>*期望完成时间</span>
            </div>
            <div class="all_div_center div_title8">
                <span>*责任人</span>
            </div>
            <div class="all_div_center div_title9">
                <span>状态</span>
            </div>
            <div class="all_div_center div_title10">
                <span>任务单号</span>
            </div>
            <div class="all_div_center div_title11">
                <span>实际完成时间</span>
            </div>
        </div>
        <%
            for (int i = 0; i < mergeBean.size(); i++) {
                ChangeTaskInfoBean changeTaskInfoBean = mergeBean.get(i);
                //展示事务性任务无需展示新增的事务性任务（无任务主题）
                if (changeTaskInfoBean.getTaskTheme() != null && changeTaskInfoBean.getTaskTheme().trim() != "") {
        %>
        <div>
            <div class="all_div_center div_content1">
                <%=i + 1%>
            </div>
            <div class="all_div div_content3">
                <span class="div_text"><%=changeTaskInfoBean.getTaskType()%></span>
            </div>
            <div class="all_div div_content4">
                <span class="div_text"><%=changeTaskInfoBean.getTaskTheme()%></span>
            </div>
            <div class="all_div div_content5">
                <span class="div_text"><%=changeTaskInfoBean.getGlfs()%></span>
            </div>
            <div class="all_div div_content6">
                <span class="div_text"><%=changeTaskInfoBean.getChangeDescribe()%></span>
            </div>
            <div class="all_div_center div_content7">
                <%=changeTaskInfoBean.getNeedDate()%>
            </div>
            <div class="all_div_center div_content8" %>
                <%=changeTaskInfoBean.getResponsible()%>
            </div>
            <%
                if (changeTaskInfoBean.getTaskState() != null && changeTaskInfoBean.getTaskState().equals(TASK_3)) {
            %>
            <div class="all_div_center div_content9" style="color: red">
                <%
                } else {
                %>
                <div class="all_div_center div_content9">
                    <%
                        }
                    %>
                    <%=changeTaskInfoBean.getTaskState()%>
                </div>
                <div class="all_div_center div_content10">
                    <%
                        if (changeTaskInfoBean.getEca() != null) {
                    %>
                    <a href="<%=baseUrl%>app/#ptc1/tcomp/infoPage?oid=OR:<%=changeTaskInfoBean.getEca()%>&u8=1"
                       style="color: blue"><%=changeTaskInfoBean.getTaskNumber()%>
                    </a>
                    <%
                    } else {
                    %>
                    <%=changeTaskInfoBean.getTaskNumber()%>
                    <%
                        }
                    %>
                </div>
                <div class="all_div_center div_content11">
                    <%=changeTaskInfoBean.getActualDate()%>
                </div>
            </div>
            <%
                    }
                }
            %>
        </div>
    </div>
</div>
<br/>
<br/>
<input type="hidden" name="rowSize" id="rowSize" value="<%=mergeBean.size()%>"/>
<script type="text/javascript">

    //展示当前信息
    function showInfo(e) {
        var expandImageUrl = document.getElementById("expandImageUrl").value;
        var collapseImageUrl = document.getElementById("collapseImageUrl").value;
        var display = document.getElementById("taskDiv").style.display;
        if (display.trim() == "" || display == "block") {
            e.src = expandImageUrl;
            document.getElementById("taskDiv").style.display = "none";
        } else {
            e.src = collapseImageUrl;
            document.getElementById("taskDiv").style.display = "block";
        }
    }
</script>
