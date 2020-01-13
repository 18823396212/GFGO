<%@ page import="ext.appo.change.ModifyHelper" %>
<%@ page import="ext.appo.change.constants.ModifyConstants" %>
<%@ page import="ext.appo.change.models.CorrelationObjectLink" %>
<%@ page import="ext.appo.change.util.ModifyUtils" %>
<%@ page import="ext.pi.core.PICoreHelper" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="wt.fc.ObjectReference" %>
<%@ page import="wt.fc.Persistable" %>
<%@ page import="wt.fc.QueryResult" %>
<%@ page import="wt.org.WTPrincipal" %>
<%@ page import="wt.part.PartDocHelper" %>
<%@ page import="wt.part.WTPart" %>
<%@ page import="wt.pom.Transaction" %>
<%@ page import="wt.session.SessionHelper" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@page pageEncoding="UTF-8" %>
<%
    WTPrincipal principal = SessionHelper.manager.getPrincipal();
    SessionHelper.manager.setAdministrator();

    String oid = request.getParameter("oid");
    String ecnVid = oid.replaceFirst("VR:wt.change2.WTChangeOrder2:", "");
    String selectOid = request.getParameter("selectOid");
    System.out.println("=====oid: " + oid);
    System.out.println("=====ecnVid: " + ecnVid);
    System.out.println("=====selectOid: " + selectOid);

    JSONArray removeVid = new JSONArray();
    Map<Persistable, CorrelationObjectLink> resultMap = new HashMap<>();
    //add by lzy at 20200113 start
//    StringBuffer messages = new StringBuffer("存在以下受影响对象不允许移除：\n");
    StringBuffer messages = new StringBuffer("当前数据(");
    //add by lzy at 20200113 end
    System.out.println("=====messages.length()2: " + messages.length());
    //检查所选数据是否可以移除
    if (StringUtils.isNotEmpty(ecnVid) && StringUtils.isNotEmpty(selectOid)) {
        JSONArray selects = new JSONArray(selectOid);
        System.out.println("=====selects: " + selects);
        for (int i = 0; i < selects.length(); i++) {
            String select = selects.getString(i);
            System.out.println("=====select: " + select);
            removeVid.put(select);

            Persistable persistable = ModifyUtils.getPersistable(select);
            System.out.println("=====persistable: " + persistable);
            //未找到所选ID对应的对象，直接从受影响对象列表移除
            if (persistable == null) continue;

            String branchId = String.valueOf(PICoreHelper.service.getBranchId(persistable));
            System.out.println("=====branchId: " + branchId);
            CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, branchId, ModifyConstants.LINKTYPE_1);
            System.out.println("=====link: " + link);
            //未找到Link，直接从受影响对象列表移除
            if (link == null) continue;

            String routing = link.getRouting();
            System.out.println("=====routing: " + routing);
            if (StringUtils.isNotEmpty(routing) && !ModifyConstants.ROUTING_2.equals(routing)) {
                messages.append(ModifyUtils.getNumber(persistable));
                if (i < selects.length() - 1) messages.append("、");
            }
            resultMap.put(persistable, link);
        }
    }
    System.out.println("=====messages.length(): " + messages.length());
//    if (messages.length() > 16) {
//        out.write(messages.toString());
    //add by lzy at 20200113 start
    if (messages.length() > 5) {
        out.write(messages.toString()+")已在审签流程中,不能移除!");
    //add by lzy at 20200113 end
    } else {
        Transaction tx = null;
        try {
            tx = new Transaction();
            tx.start();

            for (Map.Entry<Persistable, CorrelationObjectLink> entry : resultMap.entrySet()) {
                Persistable persistable = entry.getKey();
                CorrelationObjectLink link = entry.getValue();

                //移除ECN与所选受影响对象的Link
                ModifyHelper.service.removeCorrelationObjectLink(link);

                //所选对象为WTPart，移除关联的文档
                if (persistable instanceof WTPart) {
                    QueryResult result = PartDocHelper.service.getAssociatedDocuments((WTPart) persistable);//获取部件关联的图文档
                    System.out.println("=====result: " + result.size());
                    while (result.hasMoreElements()) {
                        Object object = result.nextElement();
                        if (object instanceof ObjectReference) {
                            object = ((ObjectReference) object).getObject();
                        }
                        String branchId = String.valueOf(PICoreHelper.service.getBranchId(object));
                        if (StringUtils.isEmpty(branchId)) continue;

                        link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, branchId, ModifyConstants.LINKTYPE_1);
                        if (link == null) continue;

                        removeVid.put(PICoreHelper.service.getVid(object));
                        ModifyHelper.service.removeCorrelationObjectLink(link);
                    }
                }
            }

            tx.commit();
            tx = null;
        } finally {
            if (tx != null) {
                tx.rollback();
            }
        }
        System.out.println("=====removeVid: " + removeVid.toJSONString());
        out.write(removeVid.toJSONString());
    }

    SessionHelper.manager.setPrincipal(principal.getName());
%>