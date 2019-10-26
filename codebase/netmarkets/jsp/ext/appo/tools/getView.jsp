<%@ page import="wt.org.WTPrincipal" %>
<%@ page import="wt.session.SessionHelper" %>
<%@ page import="java.util.List" %>
<%@ page import="ext.appo.test.tools.UpdateAttrsTools" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Vector" %>
<%@ page import="wt.part.WTPart" %>
<%@ page import="ext.appo.erp.constants.ERPConstants" %>
<%@ page import="static ext.appo.doc.uploadDoc.UploadHistoricalData.getAllLatestWTParts" %>

<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!--发送BOM，取最新版本，状态为已归档，已发布则发送，否则不发送，发D视图时判断是否有同一版本M视图物料，有则不发-->
<%
	out.println("========send BOM start========");
	out.println("<br>");

	WTPrincipal sessionUser = SessionHelper.manager.getPrincipal();
	SessionHelper.manager.setAdministrator();
	Vector partDesignvector = new Vector();
	Vector partManufacturvector = new Vector();
//	System.out.println("开始查询D物料：" + new Date());
	partDesignvector = getAllLatestWTParts("Design", "");
	Vector AllPartvector = new Vector();
	Vector DPartvector = new Vector();
	if (partDesignvector != null && partDesignvector.size() > 0) {
		for (int i = 0; i < partDesignvector.size(); i++) {
			WTPart part=(WTPart) partDesignvector.get(i);
			String lifeCycleState = part.getLifeCycleState().toString();
			if (lifeCycleState.equals(ERPConstants.RELEASED)||lifeCycleState.equals(ERPConstants.ARCHIVED)){
				//已发布或者已归档
				DPartvector.add(part);
			}
		}
	}
	out.println("====DPartvector===="+DPartvector.size());
	out.println("<br>");
	int sumD=0;//D视图
	int sumM=0;//M视图
	int sumMM=0;//
	if (DPartvector!=null&&DPartvector.size()>0){
		for (int i = 0; i <DPartvector.size() ; i++) {
			WTPart part=(WTPart) DPartvector.get(i);
			String partNumber=part.getNumber();
			//查D视图的物料是否有同一版本M视图物料，有则不发
			partManufacturvector = getAllLatestWTParts("Manufacturing", partNumber);
			if (partManufacturvector!=null&&partManufacturvector.size()>0){
				sumM=sumM+1;
				WTPart dpart=(WTPart) DPartvector.get(i);
				String dversion=dpart.getVersionIdentifier().getValue();
				WTPart mpart=(WTPart) partManufacturvector.get(0);
				String mversion=mpart.getVersionIdentifier().getValue();
				out.println("=====最新D视图有M视图的编码====="+dpart.getNumber()+"====版本===="+dversion+"====M视图最新版本===="+mversion);
				int result=dversion.compareTo(mversion);
				if (result>0){
					AllPartvector.add(dpart);
					out.println("====D视图版本更高的物料======"+dpart.getNumber());
					sumMM=sumMM+1;
				}
				out.println("<br>");
			}else{
				AllPartvector.add(DPartvector.get(i));
			}
		}
		out.println("<br>");
		out.println("AllD==="+DPartvector.size());
	}

//	if (partManufacturvector != null && partManufacturvector.size() > 0) {
//		for (int i = 0; i < partManufacturvector.size(); i++) {
//			WTPart part=(WTPart) partManufacturvector.get(i);
//			String lifeCycleState = part.getLifeCycleState().toString();
//			if (lifeCycleState.equals(ERPConstants.RELEASED)||lifeCycleState.equals(ERPConstants.ARCHIVED)){
//				//已发布或者已归档
//				AllPartvector.add(part);
//			}
//		}
//	}
	out.println("<br>");
	sumD=AllPartvector.size();
	out.println("=====sumD====="+sumD);
	out.println("<br>");
	out.println("=====sumM====="+sumM);
	out.println("<br>");
	out.println("=====sumMM====="+sumMM);
	out.println("<br>");
//	System.out.println("完成发送最新已归档或已发布BOM");
	SessionHelper.manager.setPrincipal(sessionUser.getName());

	out.println("========send BOM end========");
%>