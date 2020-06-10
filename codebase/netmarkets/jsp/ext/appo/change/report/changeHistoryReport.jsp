<%@ include file="/netmarkets/jsp/util/beginPopup.jspf" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/wrappers" prefix="w" %>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ page import="wt.fc.Persistable" %>
<%@ page import="ext.appo.change.report.BomChangeReport" %>
<%@ page import="wt.part.WTPart" %>
<%@ page import="wt.change2.WTChangeOrder2" %>
<%@ page import="ext.appo.change.report.ChangeHistoryReport" %>
<%@ page import="ext.appo.change.beans.PartUpdateTypeBean" %>
<%@ page import="ext.appo.change.beans.PartUpdateInfoBean" %>
<%@ page import="ext.appo.change.beans.BOMChangeInfoBean" %>
<%@ page import="ext.appo.change.constants.BomChangeConstants" %>
<%@ page import="wt.fc.QueryResult" %>
<%@ page import="com.ptc.windchill.enterprise.history.HistoryTablesCommands" %>
<%@ page import="com.ptc.windchill.enterprise.history.MaturityHistoryInfo" %>
<%@ page import="wt.change2.ChangeHelper2" %>

<style type="text/css">
    .tb {
        /*width:100%;*/
        width: 2000px;
        table-layout: fixed;
        border-collapse: collapse;
        line-height: 30px;
        white-space: normal;
    }

    .td_title {
        background: #EAF0FB;
    }

    .th_datalist {
        background: #D2E1FD;
        text-align: center;
    }

    .td_textarea {
        height: 40px;
        width: 80%;
        margin: 2px;
    }

    p {
        word-wrap: break-word;
    }

    img:hover {
        color: red;
        cursor: pointer;
    }

    #tb_affectedObject tr:hover {
        background-color: #FBF1D4;
    }

    .mui-ellipsis {
        display: -webkit-box;
        overflow: hidden;
        white-space: normal !important;
        text-overflow: ellipsis;
        word-wrap: break-word;
        -webkit-line-clamp: 10;
        -webkit-box-orient: vertical;
    }

    .td_center {
        text-align: center;
    }

    .td_right {
        text-align: right;
    }

    .change {
        border: 1px black solid;
    }

</style>

<head>
    <title>变更履历</title>
</head>
<%
    //取路径
    wt.httpgw.URLFactory urlFactory = new wt.httpgw.URLFactory();
    String baseUrl = urlFactory.getBaseHREF();

    String expandImageUrl = baseUrl + "netmarkets/images/column_expand.gif";
    String collapseImageUrl = baseUrl + "netmarkets/images/column_collapse.gif";
    String url = baseUrl + "netmarkets/jsp/ext/appo/change/report/exportChangeHistoryReport.jsp";

    List<PartUpdateTypeBean> partUpdateTypeBeans = new ArrayList<>();
    Set<WTChangeOrder2> wtChangeOrder2s = new HashSet<>();
    Map<String, PartUpdateInfoBean> partUpdateInfoBeanMap = new HashMap<>();//<ECN编码，PartUpdateInfoBean>
    List<BOMChangeInfoBean> bomChangeInfoBeans = new ArrayList<>();
    //通过oid获取物料
    String oid = request.getParameter("oid");
    Persistable persistable = BomChangeReport.getObjectByOid(oid);
    if (persistable instanceof WTPart) {
        WTPart part = (WTPart) persistable;
        String number = part.getNumber();

        //物料升版各阶段信息
        partUpdateTypeBeans = ChangeHistoryReport.getPartUpdateType(part);
        wtChangeOrder2s = ChangeHistoryReport.getAllECN(part);
        //获取ECN,受影响物料的合并信息
        partUpdateInfoBeanMap = ChangeHistoryReport.getUpdateInfo(wtChangeOrder2s, number);
    }
%>
<body width="100%">
<input type="hidden" id="expandImageUrl" value="<%=expandImageUrl%>">
<input type="hidden" id="collapseImageUrl" value="<%=collapseImageUrl%>">
<input type="hidden" id="url" value="<%=url%>">
<input type="hidden" id="oid" value="<%=oid%>">
<%
    if (partUpdateTypeBeans != null && partUpdateTypeBeans.size() > 0) {
%>
<div align="right" style="margin-right: 4px;margin-bottom: 2px;">
    <input type="button" onclick="exportChangeHistoryReport()" value="导出变更履历"/>
</div>
<div style="background: #D2E1FD;height: 20px;line-height: 20px;width: 2000px"><h4>变更履历</h4></div>
<br/>
<%
    //物料升版各阶段信息
    for (int i = 0; i < partUpdateTypeBeans.size(); i++) {
        PartUpdateTypeBean partUpdateTypeBean = partUpdateTypeBeans.get(i);
        Map<String, String> changeVersionMap = partUpdateTypeBean.getChangeVersion();
        String beforeVersion = changeVersionMap.get("before") == null ? "" : changeVersionMap.get("before");
        String afterVersion = changeVersionMap.get("after") == null ? "" : changeVersionMap.get("after");
        String updateType = partUpdateTypeBean.getUpdateType();
        String idFlag = beforeVersion + ";" + afterVersion;
        String viewName = partUpdateTypeBean.getView();
        String partNumber = partUpdateTypeBean.getPartNumber();
%>
<div style="float: left;line-height: normal; "><img id="<%=idFlag%>" src="<%=collapseImageUrl%>"
                                                    onclick="showInfo(this)"/></div>
<span>物料<%=partUpdateTypeBean.getPartNumber()%>-<%=beforeVersion%>版本变更为<%=afterVersion%>版本</span>

<div id="div_<%=idFlag%>">
    <%
        if (updateType.equals(BomChangeConstants.TYPE_10)) {
            //ECN变更
            PartUpdateInfoBean partUpdateInfoBean = new PartUpdateInfoBean();
            WTChangeOrder2 ecn = partUpdateTypeBean.getEcn();
            String ecnNumber = ecn.getNumber();
            for (String ecnStr : partUpdateInfoBeanMap.keySet()) {
                if (ecnNumber.equals(ecnStr)) {
                    partUpdateInfoBean = partUpdateInfoBeanMap.get(ecnStr);
                    break;
                }
            }
    %>

    <table class="tb" border="1">
        <tr>
            <th class="th_datalist" scope="col" colspan="1">
                <p>序号</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>ECN编号</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>变更申请人</p>
            </th>
            <th class="th_datalist" scope="col" colspan="3">
                <p>申请时间</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>所属产品类别</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>所属项目</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>ECN变更类型</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>变更原因</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>变更阶段</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>是否变更图纸</p>
            </th>
            <th class="th_datalist" scope="col" colspan="6">
                <p>变更说明</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>部件状态</p>
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
                <p>部件变更类型</p>
            </th>
            <th class="th_datalist" scope="col" colspan="2">
                <p>完成时间</p>
            </th>
            <th class="th_datalist" scope="col" colspan="4">
                <p>更改详细描述</p>
            </th>
        </tr>
        <tr id="">
            <td style="text-align: center" colspan="1"><%=1%>
            </td>
            <td colspan="2"><p><a
                    href="app/#ptc1/tcomp/infoPage?oid=VR%3Awt.change2.WTChangeOrder2%3A<%=partUpdateInfoBean.getEcn().getBranchIdentifier()%>&u8=1"><%=partUpdateInfoBean.getEcnNumber()%>
            </a></p></td>
            <td class="td_center" colspan="2"><p><%=partUpdateInfoBean.getEcnCreator()%>
            </p></td>
            <td class="td_center" colspan="3"><p><%=partUpdateInfoBean.getEcnStartTime()%>
            </p></td>
            <td class="td_center" colspan="2"><p><%=partUpdateInfoBean.getProductType()%>
            </p></td>
            <td class="td_center" colspan="2"><p><%=partUpdateInfoBean.getProjectName()%>
            </p></td>
            <td class="td_center" colspan="2"><p><%=partUpdateInfoBean.getChangeType()%>
            </p></td>
            <td class="td_center" colspan="2"><p><%=partUpdateInfoBean.getChangeReason()%>
            </p></td>
            <td class="td_center" colspan="2"><p><%=partUpdateInfoBean.getChangePhase()%>
            </p></td>
            <td class="td_center" colspan="2"><p><%=partUpdateInfoBean.getIsChangeDrawing()%>
            </p></td>
            <td colspan="6"><p class="mui-ellipsis" id="mui-ellipsis"
                               onclick="showAll()"><%=partUpdateInfoBean.getChangeDescription()%>
            </p></td>
            <td class="td_center" colspan="2"><p><%=partUpdateInfoBean.getEffectObjectState()%>
            </p></td>
            <td class="td_right" colspan="2"><p><%=partUpdateInfoBean.getInProcessQuantities()%>
            </p></td>
            <td class="td_center" colspan="2"><p><%=partUpdateInfoBean.getProcessingMeasures()%>
            </p></td>
            <td class="td_right" colspan="2"><p><%=partUpdateInfoBean.getOnthewayQuantity()%>
            </p></td>
            <td class="td_center" colspan="2"><p><%=partUpdateInfoBean.getOnthewayTreatmentMeasure()%>
            </p></td>
            <td class="td_right" colspan="2"><p><%=partUpdateInfoBean.getStockQuantity()%>
            </p></td>
            <td class="td_center" colspan="2"><p><%=partUpdateInfoBean.getStockTreatmentMeasure()%>
            </p></td>
            <td class="td_center" colspan="3"><p><%=partUpdateInfoBean.getFinishedHandleMeasures()%>
            </p></td>
            <td class="td_center" colspan="2"><p><%=partUpdateInfoBean.getEffectObjectChangeType()%>
            </p></td>
            <td class="td_center" colspan="2"><p><%=partUpdateInfoBean.getExpectDate()%>
            </p></td>
            <td colspan="4"><p><%=partUpdateInfoBean.getChangeDetailedDescription()%>
            </p></td>
        </tr>
    </table>
    <%

    } else if (updateType.equals(BomChangeConstants.TYPE_11)) {
        //修订
    %>
    <table class="tb">
        <tr>
            <td class="td_title" colspan="1" style="border: 1px black solid">变更方式</td>
            <td colspan="2" style="border: 1px black solid;text-align: center"><p><%=updateType%>
            </p></td>
            <td colspan="14"></td>
        </tr>
    </table>
    <%
        }
        WTPart beforePart = ChangeHistoryReport.getLatestPart(partNumber, viewName, beforeVersion);
        WTPart afterPart = ChangeHistoryReport.getLatestPart(partNumber, viewName, afterVersion);
        bomChangeInfoBeans = ChangeHistoryReport.getBomChangeInfoByPart(beforePart, afterPart);
        if (bomChangeInfoBeans != null && bomChangeInfoBeans.size() > 0) {
    %>
    <br/>
    <table class="tb" border="0">
        <tr>
            <th class="th_datalist change" scope="col" colspan="1">
                <p>序号</p>
            </th>
            <th class="th_datalist change" scope="col" colspan="2">
                <p>变更类型</p>
            </th>
            <th class="th_datalist change" scope="col" colspan="2">
                <p>物料编码</p>
            </th>
            <th class="th_datalist change" scope="col" colspan="2">
                <p>名称</p>
            </th>
            <th class="th_datalist change" scope="col" colspan="3">
                <p>规格</p>
            </th>
            <th class="th_datalist change" scope="col" colspan="4">
                <p>位号</p>
            </th>
            <th class="th_datalist change" scope="col" colspan="2">
                <p>数量</p>
            </th>
            <th class="th_datalist change" scope="col" colspan="2">
                <p>替代料</p>
            </th>
            <th scope="col" colspan="8" style="border: none"></th>
        </tr>

        <%

            for (int j = 0; j < bomChangeInfoBeans.size(); j++) {
                BOMChangeInfoBean bean = bomChangeInfoBeans.get(j);
        %>
        <tr>
            <td class="change" style="text-align: center" colspan="1"><%=j + 1%>
            </td>
            <%
                Set changeTypeList = bean.getChangeType();
                if (changeTypeList.contains(BomChangeConstants.TYPE_1) && changeTypeList.contains(BomChangeConstants.TYPE_2)) {
                    changeTypeList.remove(BomChangeConstants.TYPE_1);
                    changeTypeList.remove(BomChangeConstants.TYPE_2);
                    changeTypeList.add(BomChangeConstants.TYPE_7);
                }
                if (changeTypeList.contains(BomChangeConstants.TYPE_3) && changeTypeList.contains(BomChangeConstants.TYPE_4)) {
                    changeTypeList.remove(BomChangeConstants.TYPE_3);
                    changeTypeList.remove(BomChangeConstants.TYPE_4);
                    changeTypeList.add(BomChangeConstants.TYPE_8);
                }

                Map<String, String> placeNumber = new HashMap<>();
                Map<String, String> quantit = new HashMap<>();
                Map<String, List<String>> replacePartNumbers = new HashMap<>();
                Map<String, String> parentSpecification = new HashMap<>();
                String typeName = "";
                for (Object s : changeTypeList) {
                    typeName = typeName + "," + s;
                }
                if (typeName.length() > 0) {
                    typeName = typeName.substring(1);
                }

                if (bean.getPlaceNumber() != null && bean.getPlaceNumber().size() > 0) {
                    placeNumber = bean.getPlaceNumber();
                }
                if (bean.getQuantit() != null && bean.getQuantit().size() > 0) {
                    quantit = bean.getQuantit();
                }
                if (bean.getReplacePartNumbers() != null && bean.getReplacePartNumbers().size() > 0) {
                    replacePartNumbers = bean.getReplacePartNumbers();
                }
                if (bean.getParentSpecification() != null && bean.getParentSpecification().size() > 0) {
                    parentSpecification = bean.getParentSpecification();
                }
            %>
            <td class="change" colspan="2"><p><%=typeName%>
            </p></td>
            <td class="change" colspan="2"><p><%=bean.getNumber()%>
            </p></td>
            <td class="change" colspan="2"><p><%=bean.getName()%>
            </p></td>
            <%
                //规格描述
                if (parentSpecification != null && parentSpecification.size() > 0) {
                    String before = parentSpecification.get("before");
                    String after = parentSpecification.get("after");
            %>
            <td class="change" style="text-align: center" colspan="3">
                <div>
                    <p style="border-bottom: 1px solid #DDDDDD;">变更前：<%=before%>
                    </p>
                    <p>变更后：<%=after%>
                    </p></div>
            </td>
            <%
            } else {
            %>
            <td class="change" colspan="3"><p><%=bean.getSpecification()%>
            </p></td>
            <%
                }

                //位号
                if (placeNumber != null && placeNumber.size() > 0) {
                    if (typeName.contains(BomChangeConstants.TYPE_1) || typeName.contains(BomChangeConstants.TYPE_7)) {
                        String after = placeNumber.get("after");
            %>
            <td class="change" style="text-align: center" colspan="4"><p><%=after%>
            </p></td>
            <%
            } else if (typeName.contains(BomChangeConstants.TYPE_3) || typeName.contains(BomChangeConstants.TYPE_8)) {
            %>
            <td class="change" colspan="4"></td>
            <%
            } else {
                String before = placeNumber.get("before");
                String after = placeNumber.get("after");
            %>
            <td class="change" style="text-align: center" colspan="4">
                <div>
                    <p style="border-bottom: 1px solid #DDDDDD;">变更前：<%=before%>
                    </p>
                    <p>变更后：<%=after%>
                    </p></div>
            </td>
            <%
                }

            } else {
            %>
            <td class="change" colspan="4"></td>
            <%
                }
            %>
            <%
                //数量
                if (quantit != null && quantit.size() > 0) {
                    if (typeName.contains(BomChangeConstants.TYPE_1) || typeName.contains(BomChangeConstants.TYPE_7)) {
                        String after = quantit.get("after");
            %>
            <td class="change" style="text-align: center" colspan="2"><p><%=after%>
            </p></td>
            <%
            } else if (typeName.contains(BomChangeConstants.TYPE_3) || typeName.contains(BomChangeConstants.TYPE_8)) {
            %>
            <td class="change" colspan="2"></td>
            <%
            } else {
                String before = quantit.get("before");
                String after = quantit.get("after");
            %>
            <td class="change" style="text-align: center" colspan="2">
                <div>
                    <p style="border-bottom: 1px solid #DDDDDD;">变更前：<%=before%>
                    </p>
                    <p>变更后：<%=after%>
                    </p></div>
            </td>
            <%
                }

            } else {
            %>
            <td class="change" colspan="2"></td>
            <%
                }
            %>
            <%
                //替代料
                if (replacePartNumbers != null && replacePartNumbers.size() > 0) {

                    if (typeName.contains(BomChangeConstants.TYPE_2) && typeName.contains(BomChangeConstants.TYPE_4)) {
                        List<String> addReplacePartNumberList = replacePartNumbers.get("add");
                        List<String> delReplacePartNumberList = replacePartNumbers.get("delete");
                        String add = "";
                        String delete = "";
                        if (addReplacePartNumberList != null && addReplacePartNumberList.size() > 0) {
                            for (int k = 0; k < addReplacePartNumberList.size(); k++) {
                                if (k != 0) {
                                    add = add + "," + addReplacePartNumberList.get(k);
                                } else {
                                    add = addReplacePartNumberList.get(k);
                                }
                            }
                        }
                        if (delReplacePartNumberList != null && delReplacePartNumberList.size() > 0) {
                            for (int k = 0; k < delReplacePartNumberList.size(); k++) {
                                if (k != 0) {
                                    delete = delete + "," + delReplacePartNumberList.get(k);
                                } else {
                                    delete = delReplacePartNumberList.get(k);
                                }
                            }
                        }

            %>
            <td class="change" colspan="2">
                <div>
                    <p style="border-bottom: 1px solid #DDDDDD;">新增替代料：<%=add%>
                    </p>
                    <p>删除替代料：<%=delete%>
                    </p>
                </div>
            </td>

            <%
            } else {
                List<String> addReplacePartNumberList = replacePartNumbers.get("add");
                List<String> delReplacePartNumberList = replacePartNumbers.get("delete");
                String replacePartNumber = "";
                if (addReplacePartNumberList != null && addReplacePartNumberList.size() > 0) {
                    for (int k = 0; k < addReplacePartNumberList.size(); k++) {
                        if (k != 0) {
                            replacePartNumber = replacePartNumber + "," + addReplacePartNumberList.get(k);
                        } else {
                            replacePartNumber = addReplacePartNumberList.get(k);
                        }
                    }
                }
                if (delReplacePartNumberList != null && delReplacePartNumberList.size() > 0) {
                    for (int k = 0; k < delReplacePartNumberList.size(); k++) {
                        if (k != 0) {
                            replacePartNumber = replacePartNumber + "," + delReplacePartNumberList.get(k);
                        } else {
                            replacePartNumber = delReplacePartNumberList.get(k);
                        }
                    }
                }

            %>
            <td class="change" colspan="2"><p><%=replacePartNumber%>
            </p></td>
            <%

                }

            } else {

            %>
            <td class="change" colspan="2"></td>
            <%
                }
            %>

            <td colspan="8"></td>
        </tr>

        <%
            }
        %>
    </table>

    <%


        }

    %>

</div>
<br/>
<br/>

<%

        }
    }
%>

<div style="margin-bottom: 20px;"></div>
</body>

<script type="text/javascript">
    //展示当前信息
    function showInfo(e) {
        var expandImageUrl = document.getElementById("expandImageUrl").value;
        var collapseImageUrl = document.getElementById("collapseImageUrl").value;
        var display = document.getElementById("div_" + e.id).style.display;
        if (display.trim() == "" || display == "block") {
            e.src = expandImageUrl;
            document.getElementById("div_" + e.id).style.display = "none";
        } else {
            e.src = collapseImageUrl;
            document.getElementById("div_" + e.id).style.display = "block";
        }
    }

    //去除变更说明css显示全部内容
    function showAll() {
        document.getElementById("mui-ellipsis").className = "";
    }

    //导出变更履历
    function exportChangeHistoryReport() {
        var oid = document.getElementById("oid").value;
        var url = document.getElementById("url").value + "?oid=" + oid;
        var width=700;
        var height=500;
        //获得窗口的垂直位置
        var iTop = (window.screen.availHeight - 30 - height) / 2;
        //获得窗口的水平位置
        var iLeft = (window.screen.availWidth - 10 - width) / 2;
        window.open(url,'_blank','width='+width+',height='+height+',toolbar=no,menubar=no,location=no,status=no,resizable=yes,top='+iTop+',left='+iLeft);
    }

</script>


<%@ include file="/netmarkets/jsp/util/end.jspf" %>