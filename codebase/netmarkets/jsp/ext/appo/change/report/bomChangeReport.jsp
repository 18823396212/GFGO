<%@page import="wt.fc.ReferenceFactory"%>
<%@ include file="/netmarkets/jsp/util/beginPopup.jspf"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/wrappers" prefix="w"%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="wt.workflow.engine.WfProcess" %>
<%@ page import="wt.workflow.work.WorkItem" %>
<%@ page import="wt.workflow.work.WfAssignedActivity" %>
<%@ page import="wt.workflow.definer.WfProcessTemplate" %>
<%@ page import="wt.fc.Persistable" %>
<%@ page import="ext.appo.change.report.BomChangeReport" %>
<%@ page import="ext.appo.change.beans.ECNInfoBean" %>
<%@ page import="wt.change2.WTChangeOrder2" %>
<%@ page import="ext.pi.core.PIWorkflowHelper" %>
<%@ page import="wt.part.WTPart" %>
<%@ page import="ext.appo.change.beans.AffectedParentPartsBean" %>

<style type="text/css">
    .tb{
        width:100%;
        table-layout:fixed;
        border-collapse:collapse;
        line-height: 30px;
    }
    .td_title{
        background: #EAF0FB;
    }

    .th_datalist{
        background: #D2E1FD;
        text-align: center;
        white-space:nowrap;
    }

    .td_textarea{
        height: 40px;
        width: 80%;
        margin: 2px;
    }
</style>

<head>
    <title>BOM变更报表</title>

</head>

<%
    String oid=request.getParameter("oid");

    Persistable persistable= BomChangeReport.getObjectByOid(oid);
//    WorkItem workItem=(WorkItem)persistable;
//    List<ECNInfoBean> resultList = new ArrayList<ECNInfoBean>();
    WfProcess wfprocess = PIWorkflowHelper.service.getParentProcess((WorkItem)persistable);
    String[] list=wfprocess.getName().split("_");
    String ecnNumber="";
    if (list.length>0){
        ecnNumber=list[1];
    }

    WTChangeOrder2 ecn=BomChangeReport.getECNByNumber(ecnNumber);

    ECNInfoBean ecnInfo= BomChangeReport.getECNInfo(ecn);

    List<AffectedParentPartsBean> affectedParts=BomChangeReport.getAffectedInfo(ecn);
%>

<body width="100%">
    <br />
    <div style="background: #D2E1FD;height: 20px;"><h4>BOM变更报表</h4></div>

    <span>基本属性</span>
    <table class="tb" border="1">
        <tr>
            <td class="td_title" colspan="1">变更申请人</td>
            <td colspan="3">  <%=ecnInfo.getEcnCreator()==null?"":ecnInfo.getEcnCreator()%></td>
            <td class="td_title" colspan="1">申请时间</td>
            <td colspan="3">  <%=ecnInfo.getEcnStartTime()==null?"":ecnInfo.getEcnStartTime()%></td>
        </tr>
        <tr>
            <td class="td_title" colspan="1">所属产品类别</td>
            <td colspan="3">  <%=ecnInfo.getProductType()==null?"":ecnInfo.getProductType()%></td>
            <td class="td_title" colspan="1">所属项目</td>
            <td colspan="3">  <%=ecnInfo.getProjectName()==null?"":ecnInfo.getProjectName()%></td>
        </tr>
        <tr>
            <td class="td_title" colspan="1">变更类型</td>
            <td colspan="3">  <%=ecnInfo.getChangeType()==null?"":ecnInfo.getChangeType()%></td>
            <td class="td_title" colspan="1">变更原因</td>
            <td colspan="3">  <%=ecnInfo.getChangeReason()==null?"":ecnInfo.getChangeReason()%></td>
        </tr>
        <tr>
            <td class="td_title" colspan="1">变更阶段</td>
            <td colspan="3">  <%=ecnInfo.getChangePhase()==null?"":ecnInfo.getChangePhase()%></td>
            <td class="td_title" colspan="1">是否变更图纸</td>
            <td colspan="3">  <%=ecnInfo.getIsChangeDrawing()==null?"":ecnInfo.getIsChangeDrawing()%></td>
        </tr>
        <tr>
            <td class="td_title" colspan="1">变更说明</td>
            <td colspan="7">
                <textarea class="td_textarea"><%=ecnInfo.getChangeDescription()==null?"":ecnInfo.getChangeDescription()%></textarea>
            </td>
        </tr>
    </table>
    <br />createEngineeringECN
    <span>受影响的母件</span>
    <table class="tb" border="1">
        <tr>
            <th class="th_datalist" scope="col" colspan="1">
                <span class="tablecolumnheaderfont">序号</span>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <span class="tablecolumnheaderfont">编号</span>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <span class="tablecolumnheaderfont">名称</span>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <span class="tablecolumnheaderfont">版本</span>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <span class="tablecolumnheaderfont">状态</span>
            </th>
            <th class="th_datalist" scope="col" colspan="4">
                <span class="tablecolumnheaderfont">更改详细描述</span>
            </th>
        </tr>
        <%
            if(affectedParts != null && affectedParts.size() > 0)
            {
                for (int i = 0; i < affectedParts.size(); i++)
                {
                    AffectedParentPartsBean bean = affectedParts.get(i);
        %>
        <tr>
            <td  style="text-align: center" colspan="1"><%=i+1%></td>
            <td colspan="2"><%=bean.getEffectObjectNumber()%></td>
            <td colspan="2"><%=bean.getEffectObjectName()%></td>
            <td colspan="2"><%=bean.getEffectObjectVersion()%></td>
            <td colspan="2"><%=bean.getEffectObjectState()%></td>
            <td colspan="4"><%=bean.getChangeDetailedDescription()%></td>
        </tr>
                <%} %>

        <%
            }
        %>
    </table>
    <br />
    <span>BOM 变更明细</span>
    <table class="tb" border="1">
        <tr>
            <th class="th_datalist" scope="col" colspan="1">
                <span class="tablecolumnheaderfont">序号</span>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <span class="tablecolumnheaderfont">变更类型</span>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <span class="tablecolumnheaderfont">物料编码</span>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <span class="tablecolumnheaderfont">名称</span>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <span class="tablecolumnheaderfont">规格</span>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <span class="tablecolumnheaderfont">位号</span>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <span class="tablecolumnheaderfont">数量</span>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <span class="tablecolumnheaderfont">替代料</span>
            </th>
        </tr>
        <tr>
        </tr>
<%--        <%--%>
<%--            if(resultList != null && resultList.size() > 0)--%>
<%--            {--%>
<%--                String priEcnNumber = "";--%>
<%--                for (int i = 0; i < resultList.size(); i++)--%>
<%--                {--%>
<%--                    ChangeInfoBean bean = resultList.get(i);--%>
<%--        %>--%>
<%--        <tr>--%>
<%--            <td><%=i+1%></td>--%>
<%--            <%--%>
<%--                if(!priEcnNumber.equals(bean.getEcnNumber())) {--%>
<%--            %>--%>
<%--            <td rowspan="<%=bean.getLine() %>"><%=bean.getEcnCreator()%></td>--%>
<%--            <td rowspan="<%=bean.getLine() %>"><%=bean.getEcnNumber()%></td>--%>
<%--            <td rowspan="<%=bean.getLine() %>"><%=bean.getEcnName()%></td>--%>
<%--            <td rowspan="<%=bean.getLine() %>"><%=bean.getChangeReason()%></td>--%>
<%--            <td rowspan="<%=bean.getLine() %>"><%=bean.getChangeReasonDes()%></td>--%>
<%--            <td rowspan="<%=bean.getLine() %>"><%=bean.getProductType()%></td>--%>
<%--            <td rowspan="<%=bean.getLine() %>"><%=bean.getProjectName()%></td>--%>
<%--            <%} %>--%>
<%--            <td><%=bean.getEffectObjectState()%></td>--%>
<%--            <td><%=bean.getEffectObjectNo()%></td>--%>
<%--            <td><%=bean.getEffectObjectName()%></td>--%>
<%--            <td><%=bean.getEffectObjectVesion()%></td>--%>
<%--            <td><%=bean.getInProcessQuantities()%></td>--%>
<%--            <td><%=bean.getProcessingMeasures()%></td>--%>
<%--            <td><%=bean.getOnthewayQuantity()%></td>--%>
<%--            <td><%=bean.getOnthewayTreatmentMeasure()%></td>--%>
<%--            <td><%=bean.getStockQuantity()%></td>--%>
<%--            <td><%=bean.getStockTreatmentMeasure()%></td>--%>
<%--            <%--%>
<%--                    priEcnNumber = bean.getEcnNumber();--%>
<%--                }--%>
<%--            %>--%>
<%--        </tr>--%>

<%--        <%--%>
<%--            }--%>
<%--        %>--%>
    </table>

</body>

<script type="text/javascript">

</script>


<%@ include file="/netmarkets/jsp/util/end.jspf"%>