<%@ page import="ext.appo.change.ModifyHelper" %>
<%@ page import="ext.appo.change.constants.ModifyConstants" %>
<%@ page import="ext.appo.change.models.TransactionTask" %>
<%@ page import="ext.appo.ecn.constants.ChangeConstants" %>
<%@ page import="ext.pi.core.PICoreHelper" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="wt.change2.WTChangeOrder2" %>
<%@ page import="wt.fc.Persistable" %>
<%@ page import="wt.fc.ReferenceFactory" %>
<%@ page import="wt.org.WTPrincipal" %>
<%@ page import="wt.pom.Transaction" %>
<%@ page import="wt.session.SessionHelper" %>
<%@ page import="wt.workflow.work.WorkItem" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Set" %>
<%@ page import="static ext.appo.change.constants.ModifyConstants.LINKTYPE_3" %>
<%@page pageEncoding="UTF-8" %>
<%
    WTPrincipal principal = SessionHelper.manager.getPrincipal();
    SessionHelper.manager.setAdministrator();
    Transaction tx = null;
    try {
        tx = new Transaction();
        tx.start();

        String oid = request.getParameter("oid");
        String datasArray = request.getParameter("datasArray");

        if (StringUtils.isNotEmpty(oid)) {
            ReferenceFactory factory = new ReferenceFactory();
            Persistable persistable = factory.getReference(oid) == null ? null : factory.getReference(oid).getObject();
            if (persistable instanceof WorkItem) {
                WorkItem workItem = (WorkItem) persistable;
                persistable = workItem.getPrimaryBusinessObject().getObject();
                if (persistable instanceof WTChangeOrder2) {
                    WTChangeOrder2 changeOrder2 = (WTChangeOrder2) persistable;
                    String ecnVid = String.valueOf(changeOrder2.getBranchIdentifier());

                    Set<Persistable> result = ModifyHelper.service.queryPersistable(changeOrder2, LINKTYPE_3);
                    for (Persistable persistable1 : result) {
                        if (persistable1 instanceof TransactionTask) {
                            TransactionTask task = (TransactionTask) persistable1;
                            if (task.getChangeActivity2() == null) {
                                //没创建eca更新属性
                                if (StringUtils.isNotEmpty(datasArray)) {
                                    JSONObject object = new JSONObject(datasArray);
                                    Iterator<?> keyIterator = object.keys();
                                    while (keyIterator.hasNext()) {
                                        // 对象ID
                                        String key = (String) keyIterator.next();
                                        if (PICoreHelper.service.getOid(task).equals(key)) {
                                            // 对象数据
                                            JSONObject jsonObject = new JSONObject(object.getString(key));
                                            String changeTheme="";
                                            String changeDescribe="";
                                            String responsible="";
                                            String needDate="";
                                            String taskType="";
                                            String clfs="";
                                            if (jsonObject.has(ChangeConstants.CHANGETHEME_COMPID)) {
                                                changeTheme=jsonObject.getString(ChangeConstants.CHANGETHEME_COMPID);
                                            }
                                            if (jsonObject.has(ChangeConstants.CHANGEDESCRIBE_COMPID)) {
                                                changeDescribe=jsonObject.getString(ChangeConstants.CHANGEDESCRIBE_COMPID);
                                            }
                                            if (jsonObject.has(ChangeConstants.RESPONSIBLE_COMPID)) {
                                                responsible=jsonObject.getString(ChangeConstants.RESPONSIBLE_COMPID);
                                            }
                                            if (jsonObject.has(ChangeConstants.NEEDDATE_COMPID)) {
                                                needDate=jsonObject.getString(ChangeConstants.NEEDDATE_COMPID);
                                            }
                                            if (jsonObject.has(ModifyConstants.TASKTYPE_COMPID)) {
                                                taskType=jsonObject.getString(ModifyConstants.TASKTYPE_COMPID);
                                            }
                                            if (jsonObject.has(ModifyConstants.GLFS_COMPID)) {
                                                clfs=jsonObject.getString(ModifyConstants.GLFS_COMPID);
                                            }

                                            ModifyHelper.service.updateTransactionTask(task, changeTheme, changeDescribe, responsible, needDate, taskType, clfs);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        tx.commit();
        tx = null;
    } finally {
        if (tx != null) {
            tx.rollback();
        }
        SessionHelper.manager.setPrincipal(principal.getName());
    }
%>