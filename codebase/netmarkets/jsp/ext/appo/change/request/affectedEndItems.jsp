<%@page pageEncoding="UTF-8" %>
<%@page import="ext.appo.ecn.common.util.ChangeUtils" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="wt.fc.Persistable" %>
<%@ page import="wt.fc.PersistenceHelper" %>
<%@ page import="wt.fc.ReferenceFactory" %>
<%@ page import="wt.part.WTPart" %>
<%@ page import="ext.appo.part.util.MversionControlHelper" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="io.swagger.util.Json" %>
<%@ page language="java" isThreadSafe="true" contentType="text/html;charset=UTF-8" %>
<%
    // 对象OID
    String itemOid = request.getParameter("itemOid");
    // 调用发放
    String method = request.getParameter("method");
    //如果itemOid传值为oid转换为vid(通过收集对象收集需做特殊处理)
    JSONArray resultArray = new JSONArray();
    JSONArray jsonArray = new JSONArray(itemOid);
    for (int i = 0; i < jsonArray.length(); i++) {
        String value = jsonArray.getString(i);
        System.out.println("value==" + value);
        if (value.contains("oid")) {
            JSONObject jsonObject = new JSONObject(value);
            String oid = jsonObject.getString("oid");
            if (oid != null && !oid.isEmpty()) {
                Persistable persistable = (new ReferenceFactory()).getReference(oid).getObject();
                if (persistable instanceof WTPart) {
                    WTPart part = (WTPart) persistable;
                    String partVid = MversionControlHelper.getVidByObject(part);
                    JSONObject object = new JSONObject();
                    object.optString(partVid);
                    resultArray.put(object);
                }
            }
        }
    }
    if (resultArray != null && resultArray.length() > 0) {
        itemOid = resultArray.toJSONString();
    }
    out.write(ChangeUtils.disposeAffectedEndItems(itemOid, method));
%>