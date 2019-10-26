<%@ page import="wt.org.WTPrincipal" %>
<%@ page import="wt.session.SessionHelper" %>
<%@ page import="java.util.List" %>
<%@ page import="ext.appo.ecn.common.util.ChangeUtils" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="ext.appo.ecn.pdf.PdfUtil" %>
<%@ page import="wt.change2.WTChangeOrder2" %>
<%@ page import="wt.part.WTPart" %>
<%@ page import="java.util.Collection" %>
<%@ page import="ext.appo.erp.service.BomReleaseService" %>
<%@ page import="wt.fc.collections.WTArrayList" %>
<%@ page import="wt.pom.Transaction" %>


<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
	out.println("========udpate part attr start========");
	out.println("<br>");

	WTPrincipal sessionUser = SessionHelper.manager.getPrincipal();
	SessionHelper.manager.setAdministrator();

	Transaction transaction = null;
	try{

		transaction = new Transaction();
		transaction.start();

		List<String> msgs = new ArrayList<>();
		String key = request.getParameter("ecnoid");
		String ecnoid = "OR:wt.change2.WTChangeOrder2:"+key;
		WTChangeOrder2 ecn = (WTChangeOrder2)PdfUtil.getObjectByOid(ecnoid);
		Collection<WTPart> cols = ChangeUtils.setReleaseState(ecn);
		WTArrayList list = new WTArrayList();
		list.addAll(cols);
		BomReleaseService.releasePersistableSingleLevel(list,ecn,null);

		transaction.commit();
		transaction = null;

	} finally {
		if (transaction != null) {
			transaction.rollback();
			transaction = null;
		}
	}


	SessionHelper.manager.setPrincipal(sessionUser.getName());

	out.println("========udpate part attr end========");
%>