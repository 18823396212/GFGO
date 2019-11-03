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
<%@ page import="wt.part.PartDocHelper" %>
<%@ page import="wt.part.WTPart" %>
<%@page pageEncoding="UTF-8" %>
<%
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

            //移除ECN与所选受影响对象的Link
            String branchId = String.valueOf(PICoreHelper.service.getBranchId(persistable));
            System.out.println("=====branchId: " + branchId);
            CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, branchId, ModifyConstants.LINKTYPE_1);
            System.out.println("=====link: " + link);
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
                    branchId = String.valueOf(PICoreHelper.service.getBranchId(object));
                    link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, branchId, ModifyConstants.LINKTYPE_1);
                    if (link != null) removeVid.put(PICoreHelper.service.getVid(object));
                    ModifyHelper.service.removeCorrelationObjectLink(link);
                }
            }
            removeVid.put(selects.getString(i));
        }
    }
    System.out.println("=====removeVid: " + removeVid.toJSONString());
    out.write(removeVid.toJSONString());
%>