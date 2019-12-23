<%@ page import="ext.appo.change.ModifyHelper" %>
<%@ page import="ext.appo.change.constants.ModifyConstants" %>
<%@ page import="ext.appo.change.models.TransactionTask" %>
<%@ page import="ext.appo.ecn.constants.ChangeConstants" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="wt.change2.WTChangeOrder2" %>
<%@ page import="wt.fc.Persistable" %>
<%@ page import="wt.fc.ReferenceFactory" %>
<%@ page import="wt.org.WTPrincipal" %>
<%@ page import="wt.pom.Transaction" %>
<%@ page import="wt.session.SessionHelper" %>
<%@ page import="wt.workflow.work.WorkItem" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.Date" %>
<%@ page import="ext.pi.core.PICoreHelper" %>
<%@page pageEncoding="UTF-8" %>
<%
    WTPrincipal principal = SessionHelper.manager.getPrincipal();
    SessionHelper.manager.setAdministrator();
    Transaction tx = null;
    try {
        tx = new Transaction();
        tx.start();

        String oid = request.getParameter("oid");
        System.out.println("=====oid: " + oid);

        if (StringUtils.isNotEmpty(oid)) {
            ReferenceFactory factory = new ReferenceFactory();
            Persistable persistable = factory.getReference(oid) == null ? null : factory.getReference(oid).getObject();
            if (persistable instanceof WorkItem) {
                WorkItem workItem = (WorkItem) persistable;
                persistable = workItem.getPrimaryBusinessObject().getObject();
                if (persistable instanceof WTChangeOrder2) {
                    WTChangeOrder2 changeOrder2 = (WTChangeOrder2) persistable;
                    String ecnVid = String.valueOf(changeOrder2.getBranchIdentifier());

                    DateFormat format = new SimpleDateFormat(ChangeConstants.SIMPLE_DATE_FORMAT);
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 7);
                    Date date = calendar.getTime();
                    TransactionTask task = ModifyHelper.service.newTransactionTask("", "", "", format.format(date), null);
                    String taskOid = String.valueOf(task.getPersistInfo().getObjectIdentifier().getId());

                    ModifyHelper.service.newCorrelationObjectLink(changeOrder2, task, ModifyConstants.LINKTYPE_3, ecnVid, taskOid);

                    out.write(PICoreHelper.service.getOid(task));
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