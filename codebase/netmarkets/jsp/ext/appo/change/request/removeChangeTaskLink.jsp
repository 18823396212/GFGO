<%@ page import="ext.appo.change.ModifyHelper" %>
<%@ page import="ext.appo.change.constants.ModifyConstants" %>
<%@ page import="ext.appo.change.models.CorrelationObjectLink" %>
<%@ page import="ext.appo.change.models.TransactionTask" %>
<%@ page import="ext.appo.change.util.ModifyUtils" %>
<%@ page import="ext.pi.core.PICoreHelper" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="wt.change2.WTChangeActivity2" %>
<%@ page import="wt.fc.Persistable" %>
<%@ page import="wt.fc.PersistenceServerHelper" %>
<%@ page import="wt.org.WTPrincipal" %>
<%@ page import="wt.pom.Transaction" %>
<%@ page import="wt.session.SessionHelper" %>
<%@ page import="wt.fc.QueryResult" %>
<%@ page import="wt.workflow.engine.WfEngineHelper" %>
<%@ page import="wt.workflow.engine.WfProcess" %>
<%@ page import="ext.pi.core.PIWorkflowHelper" %>
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
                if (persistable instanceof TransactionTask) {
                    TransactionTask task = (TransactionTask) persistable;

                    //删除所选事务性任务模型对应的ECA、事务性任务的产生对象不回退！
                    persistable = ModifyUtils.getPersistable(task.getChangeActivity2());
                    System.out.println("=====ChangeActivity2: " + persistable);
                    if (persistable instanceof WTChangeActivity2) {
                        WTChangeActivity2 activity2 = (WTChangeActivity2) persistable;
                        //add by lzy at 20200107 start
                        //先终止事务性任务进程
                        QueryResult result = WfEngineHelper.service.getAssociatedProcesses(activity2, null, null);
                        System.out.println("事务性任务流程=="+result);
                        while (result.hasMoreElements()) {
                            WfProcess process = (WfProcess) result.nextElement();
                            System.out.println("process=="+process);
                            PIWorkflowHelper.service.stop(process);
                        }
                        WTChangeActivity2 eca = (WTChangeActivity2) persistable;
                        ModifyUtils.removeAffectedActivityData(eca);
                        ModifyUtils.removeChangeRecord(eca);
                        //add by lzy at 20200107 end
                        PersistenceServerHelper.manager.remove(eca);
                    }

                    //移除ECN与所选事务性任务模型对象的Link
                    String ida2a2 = String.valueOf(PICoreHelper.service.getIda2a2(task));
                    System.out.println("=====ida2a2: " + ida2a2);
                    CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, ida2a2, ModifyConstants.LINKTYPE_3);
                    System.out.println("=====link: " + link);
                    ModifyHelper.service.removeCorrelationObjectLink(link);

                    //删除所选事务性任务模型对象
                    ModifyHelper.service.deleteTransactionTask(task);
                }
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