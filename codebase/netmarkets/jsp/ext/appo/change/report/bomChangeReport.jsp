<%@ include file="/netmarkets/jsp/util/beginPopup.jspf"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/wrappers" prefix="w"%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="wt.workflow.engine.WfProcess" %>
<%@ page import="wt.workflow.work.WorkItem" %>
<%@ page import="wt.fc.Persistable" %>
<%@ page import="ext.appo.change.report.BomChangeReport" %>
<%@ page import="ext.appo.change.beans.ECNInfoBean" %>
<%@ page import="wt.change2.WTChangeOrder2" %>
<%@ page import="ext.pi.core.PIWorkflowHelper" %>
<%@ page import="ext.appo.change.beans.AffectedParentPartsBean" %>
<%@ page import="wt.change2.WTChangeActivity2" %>
<%@ page import="ext.appo.change.beans.BOMChangeInfoBean" %>

<style type="text/css">
    .tb{
        width:100%;
        table-layout:fixed;
        border-collapse:collapse;
        line-height: 30px;
        white-space: normal;
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
    p{
        word-wrap:break-word;
    }
    img:hover{
        color: red;
        cursor: pointer;
    }

</style>

<head>
    <title>BOM变更报表</title>

</head>

<%
    //取路径
    wt.httpgw.URLFactory urlFactory = new wt.httpgw.URLFactory();
    String baseUrl = urlFactory.getBaseHREF();

    String expandImageUrl = baseUrl + "netmarkets/images/column_expand.gif";
    String collapseImageUrl = baseUrl + "netmarkets/images/column_collapse.gif";
    //通过oid获取流程项->流程
    String oid=request.getParameter("oid");
    Persistable persistable= BomChangeReport.getObjectByOid(oid);
    WfProcess wfprocess=new WfProcess();
    Object[] objects = new Object[0];
    WTChangeOrder2 ecn=new WTChangeOrder2();
    ECNInfoBean ecnInfo=new ECNInfoBean();
    List<AffectedParentPartsBean> affectedParts=new ArrayList<>();
    Map<String,List<BOMChangeInfoBean>> bomChangeInfos=new HashMap<>();

    if (persistable instanceof WorkItem){
        WorkItem workItem=(WorkItem)persistable;
        wfprocess = PIWorkflowHelper.service.getParentProcess(workItem);
        objects= wfprocess.getContext().getObjects();
        if (objects.length>0) {
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] instanceof WTChangeActivity2) { //eca
                    ecn=BomChangeReport.getECNbyECA((WTChangeActivity2)objects[i]);
                }else if (objects[i] instanceof WTChangeOrder2) { //ecn
                    ecn= (WTChangeOrder2) objects[i];
                }
                if (ecn!=null){
                    ecnInfo= BomChangeReport.getECNInfo(ecn);
                    affectedParts=BomChangeReport.getAffectedInfo(ecn);
                    bomChangeInfos=BomChangeReport.getBomChangeInfos(ecn);

                }

            }
        }
    }
%>

<body width="100%">
<input type="hidden" id="expandImageUrl" value="<%=expandImageUrl%>">
<input type="hidden" id="collapseImageUrl" value="<%=collapseImageUrl%>">
    <br />
    <div style="background: #D2E1FD;height: 20px;line-height: 20px;"><h4>BOM变更报表</h4></div>
    <br />
    <div style="float: left;line-height: normal; " ><img id="baseInfo"src="<%=expandImageUrl%>" onclick="showInfo(this)" /></div><span>基本属性</span>
    <div id="tb_baseInfo">
    <table  class="tb" border="1">
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
                <textarea class="td_textarea"  readonly><%=ecnInfo.getChangeDescription()==null?"":ecnInfo.getChangeDescription()%></textarea>
            </td>
        </tr>
    </table>
    </div>
    <br />
    <br />
    <div style="float: left;line-height: normal; " ><img id="affectedObject"src="<%=expandImageUrl%>" onclick="showInfo(this)" /></div><span class="affectedObject">受影响的母件</span>
    <div id="tb_affectedObject">
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
    </div>
        <%
            if(bomChangeInfos != null && bomChangeInfos.size() > 0)
            {
                %>
    <%
                for (String key : bomChangeInfos.keySet()) {
                    List<BOMChangeInfoBean> bomChangeInfoBeans=bomChangeInfos.get(key);
                    if (bomChangeInfoBeans!=null&&bomChangeInfoBeans.size()>0){
                        %>
    <br />
    <br />
    <div style="float: left;line-height: normal; " ><img id="<%=key%>"src="<%=expandImageUrl%>" onclick="showInfo(this)" /></div><span class="<%=key%>">BOM <%=key%>变更明细</span>
    <div id="tb_<%=key%>">
    <table  class="tb" border="1">
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
        <%
        for (int i = 0; i < bomChangeInfoBeans.size(); i++)
        {
        BOMChangeInfoBean bean = bomChangeInfoBeans.get(i);
        %>

        <tr>
            <td style="text-align: center" colspan="1"><%=i+1%></td>
            <%
                Set changeTypeList=bean.getChangeType();
                %>
            <%
                Map<String,String> placeNumber=new HashMap<>();
                Map<String,String> quantit=new HashMap<>();
                Map<String,List<String>> replacePartNumbers=new HashMap<>();
                String typeName="";
                for(Object s:changeTypeList){
                    typeName=typeName+","+s;
                }
                if (typeName.length()>0){
                    typeName=typeName.substring(1);
                }

                if (bean.getPlaceNumber()!=null&&bean.getPlaceNumber().size()>0){
                    placeNumber=bean.getPlaceNumber();
                }
                if (bean.getQuantit()!=null&&bean.getQuantit().size()>0){
                    quantit=bean.getQuantit();
                }
                if (bean.getReplacePartNumbers()!=null&&bean.getReplacePartNumbers().size()>0){
                    replacePartNumbers=bean.getReplacePartNumbers();
                }

            %>
            <td colspan="2"><%=typeName%></td>
            <td colspan="2"><%=bean.getNumber()%></td>
            <td colspan="2"><%=bean.getName()%></td>
            <td colspan="2"><p><%=bean.getSpecification()%></p></td>
            <%
                if (placeNumber!=null&&placeNumber.size()>0){
                    if(typeName.contains("新增物料")){
                        String after=placeNumber.get("after");
                        %>
                        <td style="text-align: center" colspan="2"><%=after%></td>
                        <%
                    }else if(typeName.contains("删除物料")){
                        %>
                        <td colspan="2"></td>
                        <%
                    }else{
                        String before=placeNumber.get("before");
                        String after=placeNumber.get("after");
                        %>
                        <td style="text-align: center" colspan="2">
                            <div>
                                <p style="border-bottom: 1px solid #DDDDDD;">变更前：<%=before%></p>
                                <p>变更后：<%=after%></p></div>
                        </td>
                        <%
                    }

                }else{
                    %>
                    <td colspan="2"></td>
                    <%
                }
                %>
            <%
                if (quantit!=null&&quantit.size()>0){
                    if(typeName.contains("新增物料")){
                        String after=quantit.get("after");
                %>
                <td style="text-align: center" colspan="2"><%=after%></td>
                <%
                }else if(typeName.contains("删除物料")){
                %>
                <td colspan="2"></td>
                <%
                }else{
                    String before=quantit.get("before");
                    String after=quantit.get("after");
                %>
                <td style="text-align: center" colspan="2">
                    <div>
                        <p style="border-bottom: 1px solid #DDDDDD;">变更前：<%=before%></p>
                        <p>变更后：<%=after%></p></div>
                </td>
                <%
                    }

                }else{
                %>
                <td colspan="2"></td>
                <%
                    }
                %>
            <%
                if (replacePartNumbers!=null&&replacePartNumbers.size()>0){

                    if (typeName.contains("新增替代料")&&typeName.contains("删除替代料")){
                        List<String> addReplacePartNumberList=replacePartNumbers.get("add");
                        List<String> delReplacePartNumberList=replacePartNumbers.get("delete");
                        String add="";
                        String delete="";
                        if (addReplacePartNumberList!=null&&addReplacePartNumberList.size()>0) {
                            for (int j = 0; j < addReplacePartNumberList.size(); j++) {
                                if (j!=0){
                                    add=add+","+addReplacePartNumberList.get(j);
                                }else{
                                    add=addReplacePartNumberList.get(j);
                                }
                            }
                        }
                        if (delReplacePartNumberList!=null&&delReplacePartNumberList.size()>0) {
                            for (int j = 0; j < delReplacePartNumberList.size(); j++) {
                                if (j!=0){
                                    delete=delete+","+delReplacePartNumberList.get(j);
                                }else{
                                    delete=delReplacePartNumberList.get(j);
                                }
                            }
                        }

                            %>
            <td colspan="2"><div>
                    <p style="border-bottom: 1px solid #DDDDDD;">新增替代料：<%=add%></p>
                    <p>删除替代料：<%=delete%></p>
            </div></td>

            <%
                        }else{
                            List<String> addReplacePartNumberList=replacePartNumbers.get("add");
                            List<String> delReplacePartNumberList=replacePartNumbers.get("delete");
                            String replacePartNumber="";
                            if (addReplacePartNumberList!=null&&addReplacePartNumberList.size()>0) {
                                for (int j = 0; j < addReplacePartNumberList.size(); j++) {
                                    if (j != 0) {
                                        replacePartNumber = replacePartNumber + "," + addReplacePartNumberList.get(j);
                                    } else {
                                        replacePartNumber = addReplacePartNumberList.get(j);
                                    }
                                }
                            }
                            if (delReplacePartNumberList!=null&&delReplacePartNumberList.size()>0) {
                                for (int j = 0; j < delReplacePartNumberList.size(); j++) {
                                    if (j != 0) {
                                        replacePartNumber = replacePartNumber + "," + delReplacePartNumberList.get(j);
                                    } else {
                                        replacePartNumber = delReplacePartNumberList.get(j);
                                    }
                                }
                            }

                        %>
                            <td colspan="2"><%=replacePartNumber%></td>
                        <%

                        }

                    }else{

                        %>
                        <td colspan="2"></td>
                        <%
                        }
            %>

        </tr>

        <%
           }
        %>
    </table>
    </div>

    <%
                }
            }
        }
    %>
<div style="margin-bottom: 20px;"></div>
</body>

<script type="text/javascript">
    function showInfo(e) {
        var expandImageUrl=document.getElementById("expandImageUrl").value;
        var collapseImageUrl=document.getElementById("collapseImageUrl").value;
        var display=document.getElementById("tb_"+e.id).style.display;
        if (display.trim()==""||display=="block") {
            e.src=collapseImageUrl;
            document.getElementById("tb_"+e.id).style.display="none";
        }else {
            e.src= expandImageUrl;
            document.getElementById("tb_"+e.id).style.display="block";
        }
    }
</script>


<%@ include file="/netmarkets/jsp/util/end.jspf"%>