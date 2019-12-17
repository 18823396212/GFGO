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
<%@ page import="ext.appo.change.constants.BomChangeConstants" %>
<%@ page import="wt.fc.QueryResult" %>
<%@ page import="wt.change2.ChangeHelper2" %>
<%@ page import="wt.part.WTPart" %>
<%@ page import="wt.org.WTPrincipal" %>
<%@ page import="wt.session.SessionHelper" %>
<%@ page import="wt.org.WTUser" %>

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
    #tb_affectedObject tr:hover{
        background-color: #FBF1D4;
    }

</style>

<head>
    <title>BOM变更报表</title>

</head>

<%
    //取路径
    wt.httpgw.URLFactory urlFactory = new wt.httpgw.URLFactory();
    String baseUrl = urlFactory.getBaseHREF();

    String affectedObjects="";
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
    String currentaffectedObject="";
    if (persistable instanceof WorkItem){
        WorkItem workItem=(WorkItem)persistable;
        wfprocess = PIWorkflowHelper.service.getParentProcess(workItem);
        objects= wfprocess.getContext().getObjects();
        if (objects.length>0) {
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] instanceof WTChangeActivity2) { //eca
                    WTChangeActivity2 eca=(WTChangeActivity2)objects[i];
                    ecn=BomChangeReport.getECNbyECA(eca);
                    // 查询ECA中所有受影响对象
                    QueryResult qr = ChangeHelper2.service.getChangeablesBefore(eca);
                    while (qr.hasMoreElements()) {
                        Object object = qr.nextElement();

                        if (object instanceof WTPart) {
                            WTPart part = (WTPart) object;
                            currentaffectedObject=part.getNumber();
                        }
                    }
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
    }else if (persistable instanceof WTChangeOrder2){
        ecn=(WTChangeOrder2)persistable;
        if (ecn!=null){
            ecnInfo= BomChangeReport.getECNInfo(ecn);
            affectedParts=BomChangeReport.getAffectedInfo(ecn);
            bomChangeInfos=BomChangeReport.getBomChangeInfos(ecn);
        }
    }

%>
<body width="100%">
<input type="hidden" id="expandImageUrl" value="<%=expandImageUrl%>">
<input type="hidden" id="collapseImageUrl" value="<%=collapseImageUrl%>">
<input type="hidden" id="currentaffectedObject" value="<%=currentaffectedObject%>">
    <br />
    <div style="background: #D2E1FD;height: 20px;line-height: 20px;"><h4>BOM变更报表</h4></div>
    <br />
    <div style="float: left;line-height: normal; " ><img id="baseInfo"src="<%=collapseImageUrl%>" onclick="showInfo(this)" /></div><span>基本属性</span>
    <div id="tb_baseInfo">
    <table  class="tb" border="1">
        <tr>
            <td class="td_title" colspan="1">变更申请人</td>
            <td colspan="3"><p><%=ecnInfo.getEcnCreator()==null?"":ecnInfo.getEcnCreator()%></p></td>
            <td class="td_title" colspan="1">申请时间</td>
            <td colspan="3"><p><%=ecnInfo.getEcnStartTime()==null?"":ecnInfo.getEcnStartTime()%></p></td>
        </tr>
        <tr>
            <td class="td_title" colspan="1">所属产品类别</td>
            <td colspan="3"><p><%=ecnInfo.getProductType()==null?"":ecnInfo.getProductType()%></p></td>
            <td class="td_title" colspan="1">所属项目</td>
            <td colspan="3"><p><%=ecnInfo.getProjectName()==null?"":ecnInfo.getProjectName()%></p></td>
        </tr>
        <tr>
            <td class="td_title" colspan="1">变更类型</td>
            <td colspan="3"><p><%=ecnInfo.getChangeType()==null?"":ecnInfo.getChangeType()%></p></td>
            <td class="td_title" colspan="1">变更原因</td>
            <td colspan="3"><p><%=ecnInfo.getChangeReason()==null?"":ecnInfo.getChangeReason()%></p></td>
        </tr>
        <tr>
            <td class="td_title" colspan="1">变更阶段</td>
            <td colspan="3"><p><%=ecnInfo.getChangePhase()==null?"":ecnInfo.getChangePhase()%></p></td>
            <td class="td_title" colspan="1">是否变更图纸</td>
            <td colspan="3"><p><%=ecnInfo.getIsChangeDrawing()==null?"":ecnInfo.getIsChangeDrawing()%></p></td>
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
    <div style="float: left;line-height: normal; " ><img id="affectedObject"src="<%=collapseImageUrl%>" onclick="showInfo(this)" /></div><span class="affectedObject">受影响的部件</span>
    <div id="tb_affectedObject">
    <table class="tb" border="1">
        <tr>
            <th class="th_datalist" scope="col" colspan="1">
                <p>序号</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>编号</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>名称</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>版本</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>状态</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>在制数量</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>在制处理措施</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>在途数量</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>在途处理措施</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>库存数量</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>库存处理措施</p>
            </th>
            <th class="th_datalist" scope="col" colspan="3">
                <p>已出货成品处理措施</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>变更类型</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>完成时间</p>
            </th>
            <th class="th_datalist" scope="col" colspan="4">
                <p>更改详细描述</p>
            </th>
        </tr>
        <%
            if(affectedParts != null && affectedParts.size() > 0)
            {
                for (int i = 0; i < affectedParts.size(); i++)
                {
                    AffectedParentPartsBean bean = affectedParts.get(i);
                    if (i==0){
                        affectedObjects=bean.getEffectObjectNumber();
                    }else{
                        affectedObjects=affectedObjects+";"+bean.getEffectObjectNumber();
                    }

        %>
        <tr id="<%=bean.getEffectObjectNumber()%>" onclick="showBomChange(this)">
            <td  style="text-align: center" colspan="1"><%=i+1%></td>
            <td colspan="2"><p><%=bean.getEffectObjectNumber()%></p></td>
            <td colspan="2"><p><%=bean.getEffectObjectName()%></p></td>
            <td colspan="2"><p><%=bean.getEffectObjectVersion()%></p></td>
            <td colspan="2"><p><%=bean.getEffectObjectState()%></p></td>
            <td colspan="2"><p><%=bean.getInProcessQuantities()%></p></td>
            <td colspan="2"><p><%=bean.getProcessingMeasures()%></p></td>
            <td colspan="2"><p><%=bean.getOnthewayQuantity()%></p></td>
            <td colspan="2"><p><%=bean.getOnthewayTreatmentMeasure()%></p></td>
            <td colspan="2"><p><%=bean.getStockQuantity()%></p></td>
            <td colspan="2"><p><%=bean.getStockTreatmentMeasure()%></p></td>
            <td colspan="3"><p><%=bean.getFinishedHandleMeasures()%></p></td>
            <td colspan="2"><p><%=bean.getChangeType()%></p></td>
            <td colspan="2"><p><%=bean.getExpectDate()%></p></td>
            <td colspan="4"><p><%=bean.getChangeDetailedDescription()%></p></td>
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

    <div id="div_<%=key%>" style="display: none">
    <br />
    <br />
    <div id="<%=key%>" style="float: left;line-height: normal; " ><img id="<%=key%>"  src="<%=collapseImageUrl%>" onclick="showInfo(this)" /></div><span>BOM <%=key%>变更明细</span>
    <div id="tb_<%=key%>">
    <table  class="tb" border="1">
        <tr>
            <th class="th_datalist" scope="col" colspan="1">
                <p>序号</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>变更类型</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>物料编码</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>名称</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>规格</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>位号</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>数量</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>替代料</p>
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
                if (changeTypeList.contains(BomChangeConstants.TYPE_1)&&changeTypeList.contains(BomChangeConstants.TYPE_2)){
                    changeTypeList.remove(BomChangeConstants.TYPE_1);
                    changeTypeList.remove(BomChangeConstants.TYPE_2);
                    changeTypeList.add(BomChangeConstants.TYPE_7);
                }
                if (changeTypeList.contains(BomChangeConstants.TYPE_3)&&changeTypeList.contains(BomChangeConstants.TYPE_4)){
                    changeTypeList.remove(BomChangeConstants.TYPE_3);
                    changeTypeList.remove(BomChangeConstants.TYPE_4);
                    changeTypeList.add(BomChangeConstants.TYPE_8);
                }

                Map<String,String> placeNumber=new HashMap<>();
                Map<String,String> quantit=new HashMap<>();
                Map<String,List<String>> replacePartNumbers=new HashMap<>();
                Map<String,String> parentSpecification=new HashMap<>();
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
                if (bean.getParentSpecification()!=null&&bean.getParentSpecification().size()>0){
                    parentSpecification=bean.getParentSpecification();
                }
            %>
            <td colspan="2"><p><%=typeName%></p></td>
            <td colspan="2"><p><%=bean.getNumber()%></p></td>
            <td colspan="2"><p><%=bean.getName()%></p></td>
            <%
                //规格描述
                if (parentSpecification!=null&&parentSpecification.size()>0){
                    String before=parentSpecification.get("before");
                    String after=parentSpecification.get("after");
                %>
                <td style="text-align: center" colspan="2">
                    <div>
                        <p style="border-bottom: 1px solid #DDDDDD;">变更前：<%=before%></p>
                        <p>变更后：<%=after%></p></div>
                </td>
                <%
                }else{
                %>
                <td colspan="2"><p><%=bean.getSpecification()%></p></td>
                <%
                }

                //位号
                if (placeNumber!=null&&placeNumber.size()>0){
                    if(typeName.contains(BomChangeConstants.TYPE_1)||typeName.contains(BomChangeConstants.TYPE_7)){
                        String after=placeNumber.get("after");
                        %>
                        <td style="text-align: center" colspan="2"><p><%=after%></p></td>
                        <%
                    }else if(typeName.contains(BomChangeConstants.TYPE_3)||typeName.contains(BomChangeConstants.TYPE_8)){
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
                //数量
                if (quantit!=null&&quantit.size()>0){
                    if(typeName.contains(BomChangeConstants.TYPE_1)||typeName.contains(BomChangeConstants.TYPE_7)){
                        String after=quantit.get("after");
                %>
            <td style="text-align: center" colspan="2"><p><%=after%></p></td>
                <%
                }else if(typeName.contains(BomChangeConstants.TYPE_3)||typeName.contains(BomChangeConstants.TYPE_8)){
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
                //替代料
                if (replacePartNumbers!=null&&replacePartNumbers.size()>0){

                    if (typeName.contains(BomChangeConstants.TYPE_2)&&typeName.contains(BomChangeConstants.TYPE_4)){
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
                            <td colspan="2"><p><%=replacePartNumber%></p></td>
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
    </div>
    <%
                }
            }
        }
    %>
<input type="hidden" id="affectedObjects" value="<%=affectedObjects%>">
<div style="margin-bottom: 20px;"></div>
</body>

<script type="text/javascript">

    function showInfo(e) {
        var expandImageUrl=document.getElementById("expandImageUrl").value;
        var collapseImageUrl=document.getElementById("collapseImageUrl").value;
        var display=document.getElementById("tb_"+e.id).style.display;
        if (display.trim()==""||display=="block") {
            e.src=expandImageUrl;
            document.getElementById("tb_"+e.id).style.display="none";
        }else {
            e.src= collapseImageUrl;
            document.getElementById("tb_"+e.id).style.display="block";
        }
    }
    function showBomChange(e){
        var affectedObject=document.getElementById("affectedObjects").value;
        var affectedObjects=affectedObject.split(";");
        for (i=0;i<affectedObjects.length ;i++ ){
            document.getElementById(affectedObjects[i]).style.backgroundColor="";
            if (document.getElementById("div_"+affectedObjects[i])){
                document.getElementById("div_"+affectedObjects[i]).style.display="none";
            }
        }
        document.getElementById(e.id).style.backgroundColor="#FBD9A7";
        document.getElementById("div_"+e.id).style.display="block";
    }
    function showCurrentaffectedObject() {
        var currentaffectedObject=document.getElementById("currentaffectedObject").value;
        if (currentaffectedObject!=""&&document.getElementById(currentaffectedObject)){
            document.getElementById(currentaffectedObject).click();
        }

    }
    showCurrentaffectedObject();

</script>


<%@ include file="/netmarkets/jsp/util/end.jspf"%>