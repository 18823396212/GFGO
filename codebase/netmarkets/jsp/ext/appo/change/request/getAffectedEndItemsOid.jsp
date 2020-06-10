<%@page pageEncoding="UTF-8" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="ext.lang.PIStringUtils" %>
<%@ page import="wt.part.WTPart" %>
<%@ page import="wt.fc.ReferenceFactory" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="static ext.appo.ecn.constants.ChangeConstants.RESULT_DATAS" %>
<%@ page language="java" isThreadSafe="true" contentType="text/html;charset=UTF-8" %>
<%
    //
    String removeOid = request.getParameter("removeOid");
    JSONArray accordArray = new JSONArray();
    JSONObject returnJSON = new JSONObject();
    if (removeOid != null) {
        JSONArray jsonArray = new JSONArray(removeOid);
        for (int i = 0; i < jsonArray.length(); i++) {
            String oid = jsonArray.getString(i);
            if (PIStringUtils.isNotNull(oid)) {
                WTPart part = (WTPart) ((new ReferenceFactory()).getReference(oid).getObject());
                if (part != null) {
                    accordArray.put("VR:wt.part.WTPart:" + part.getBranchIdentifier());
                }
            }
        }
        returnJSON.put(RESULT_DATAS, accordArray.toJSONString() == null ? "" : accordArray.toJSONString());
    }
    out.write(returnJSON.toJSONString());
%>

