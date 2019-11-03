<%@ page import="ext.appo.change.ModifyHelper" %>
<%@ page import="ext.appo.change.constants.ModifyConstants" %>
<%@ page import="ext.appo.change.models.CorrelationObjectLink" %>
<%@ page import="ext.appo.change.util.ModifyUtils" %>
<%@ page import="ext.pi.core.PICoreHelper" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="wt.fc.Persistable" %>
<%@ page import="wt.org.WTPrincipal" %>
<%@ page import="wt.pom.Transaction" %>
<%@ page import="wt.session.SessionHelper" %>
<%@page pageEncoding="UTF-8" %>
<%
    WTPrincipal principal = SessionHelper.manager.getPrincipal();
    SessionHelper.manager.setAdministrator();
    Transaction tx = null;
    try {
        tx = new Transaction();
        tx.start();

        String oid = request.getParameter("oid");
        String ecnVid = oid.replaceFirst("VR:wt.change2.WTChangeOrder2:", "");
        String selectOid = request.getParameter("selectOid");

        System.out.println("=====oid: " + oid);
        System.out.println("=====ecnVid: " + ecnVid);
        System.out.println("=====selectOid: " + selectOid);

        JSONArray removeVid = new JSONArray();
        if (StringUtils.isNotEmpty(ecnVid) && StringUtils.isNotEmpty(selectOid)) {
            JSONArray selects = new JSONArray(selectOid);
            System.out.println("=====selects: " + selects);
            for (int i = 0; i < selects.length(); i++) {
                Persistable persistable = ModifyUtils.getPersistable(selects.getString(i));
                System.out.println("=====persistable: " + persistable);
                if (persistable == null) continue;

                //移除ECN与所选受影响产品的Link
                String branchId = String.valueOf(PICoreHelper.service.getBranchId(persistable));
                System.out.println("=====branchId: " + branchId);
                CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, branchId, ModifyConstants.LINKTYPE_2);
                System.out.println("=====link: " + link);
                ModifyHelper.service.removeCorrelationObjectLink(link);

                removeVid.put(selects.getString(i));
            }
        }
        System.out.println("=====removeVid: " + removeVid.toJSONString());
        out.write(removeVid.toJSONString());

        tx.commit();
        tx = null;
    } finally {
        if (tx != null) {
            tx.rollback();
        }
        SessionHelper.manager.setPrincipal(principal.getName());
    }
%>