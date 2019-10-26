<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="ext.appo.part.beans.ProductNamingNoticBean"%>
<%@ page import="org.json.JSONObject"%>

<%
 ProductNamingNoticBean  bean = new ProductNamingNoticBean();
 JSONObject object = new JSONObject();
 object.put("oid", bean.getOid().toString());
 out.write(object.toString());
%>