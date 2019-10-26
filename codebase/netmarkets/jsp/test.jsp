<%@ page import="wt.fc.PersistenceHelper" %>
<%@ page import="wt.fc.QueryResult" %>
<%@ page import="wt.org.WTPrincipal" %>
<%@ page import="wt.part.WTPart" %>
<%@ page import="wt.pds.StatementSpec" %>
<%@ page import="wt.pom.Transaction" %>
<%@ page import="wt.query.QuerySpec" %>
<%@ page import="wt.query.SearchCondition" %>
<%@ page import="wt.session.SessionHelper" %>
<%@ page import="wt.session.SessionServerHelper" %>
<%@ page import="wt.vc.VersionControlHelper" %>
<%@ page import="wt.part.PartDocHelper" %>
<%@ page import="wt.access.AccessControlHelper" %>
<%@ page import="wt.access.AccessPermission" %>
<%@ page import="wt.epm.EPMDocument" %>
<%@ page import="wt.fc.Persistable" %>
<%@ page import="wt.doc.WTDocument" %>
<%@ page import="com.ptc.netmarkets.model.NmOid" %>
<%@ page import="wt.fc.ObjectReference" %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    System.out.println("========test page start========");
    boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
    WTPrincipal sessionUser = SessionHelper.manager.getPrincipal();
    SessionHelper.manager.setAdministrator();
    Transaction tx = null;
    try {
        tx = new Transaction();
        tx.start();


        /* NmOid mpnPart = NmOid.newNmOid("VR:com.ptc.windchill.suma.part.ManufacturerPart:1233032");
		ManufacturerPart mpn = (ManufacturerPart)mpnPart.getRefObject(); */

		NmOid par1 = NmOid.newNmOid("VR:wt.part.WTPart:131411");
		WTPart part = (WTPart)par1.getRefObject();

        /*NmOid org = NmOid.newNmOid("OR:wt.inf.container.OrgContainer:49542");
        OrgContainer orgContainer = (OrgContainer)org.getRefObject();*/

		/*NmOid pbo = NmOid.newNmOid("VR:wt.change2.WTChangeRequest2:357179");
        WTChangeRequest2 ecr = (WTChangeRequest2)pbo.getRefObject();*/

        /*NmOid epmRule = NmOid.newNmOid("OR:wt.epm.build.EPMBuildRule:1460421");
        EPMBuildRule epmLink = (EPMBuildRule)epmRule.getRefObject();*/

        /*NmOid wk = NmOid.newNmOid("OR:wt.workflow.work.WorkItem:206087");
		WorkItem workItem = (WorkItem)wk.getRefObject();*/

       /* NmOid wfc = NmOid.newNmOid("OR:wt.workflow.engine.WfConnector:4295400");
        WfConnector connector = (WfConnector)wfc.getRefObject();*/

		/*NmOid doc = NmOid.newNmOid("VR:wt.doc.WTDocument:240420");
		WTDocument document = (WTDocument)doc.getRefObject();*/

		/* NmOid doc2 = NmOid.newNmOid("VR:wt.doc.WTDocument:307002");
		WTDocument document2 = (WTDocument)doc2.getRefObject(); */

        /*NmOid epm = NmOid.newNmOid("VR:wt.epm.EPMDocument:129057");
        EPMDocument epmdoc = (EPMDocument)epm.getRefObject();*/

		/*NmOid promotion = NmOid.newNmOid("OR:wt.maturity.PromotionNotice:1479480");
		PromotionNotice pn = (PromotionNotice)promotion.getRefObject();*/

		/* NmOid promotion2 = NmOid.newNmOid("OR:wt.maturity.PromotionNotice:1197833");
		PromotionNotice pn2 = (PromotionNotice)promotion2.getRefObject(); */

		/*NmOid oldpar = NmOid.newNmOid("OR:wt.workflow.work.WfAssignedActivity:4316427");
		WfAssignedActivity activity = (WfAssignedActivity)oldpar.getRefObject();*/

		/*NmOid ecNoid = NmOid.newNmOid("OR:wt.change2.WTChangeOrder2:2383314");
		WTChangeOrder2 ecn = (WTChangeOrder2)ecNoid.getRefObject();*/

		/*NmOid wfProcess = NmOid.newNmOid("OR:wt.workflow.engine.WfProcess:4398574");
		WfProcess self = (WfProcess)wfProcess.getRefObject();*/

		/* NmOid epmdoc3d = NmOid.newNmOid("VR:wt.epm.EPMDocument:1242724");
		EPMDocument epm3d = (EPMDocument)epmdoc3d.getRefObject();

		NmOid epmdoc2d = NmOid.newNmOid("VR:wt.epm.EPMDocument:1242804");
		EPMDocument epm2d = (EPMDocument)epmdoc2d.getRefObject();  */

		/* NmOid proj = NmOid.newNmOid("OR:wt.projmgmt.admin.Project2:1246422");
		Project2 project = (Project2)proj.getRefObject(); */

        /*NmOid nmOid = NmOid.newNmOid("OR:wt.workflow.engine.WfProcess:206046");
        WfProcess process = (WfProcess) nmOid.getRefObject();*/

        /*NmOid nmOid = NmOid.newNmOid("OR:wt.part.WTPartUsageLink:287434");
        WTPartUsageLink usageLink = (WTPartUsageLink) nmOid.getRefObject();*/

        /*NmOid nmOid = NmOid.newNmOid("VR:wt.change2.WTChangeActivity2:309253");
        WTChangeActivity2 eca = (WTChangeActivity2)nmOid.getRefObject();*/

        /*NmOid gOid = NmOid.newNmOid("OR:wt.org.WTGroup:684309");//684317
        WTGroup group = (WTGroup)gOid.getRefObject();*/

        /*NmOid nmOid = NmOid.newNmOid("OR:wt.vc.baseline.ManagedBaseline:186079");//684317
        ManagedBaseline baseline = (ManagedBaseline)nmOid.getRefObject();*/

		/* out.print("Role Name:");
		out.print(role);
		out.print("<br>"); */

        QueryResult result = PartDocHelper.service.getAssociatedDocuments(part);// 获取部件关联的图文档
        while (result.hasMoreElements()) {
            Object object = result.nextElement();
            if (object instanceof ObjectReference) {
                object = ((ObjectReference) object).getObject();
            }

            if (object instanceof EPMDocument) {
                EPMDocument document = (EPMDocument) object;
                out.print(document.getNumber());
                out.print("<br>");
            } else if (object instanceof WTDocument) {
                WTDocument document = (WTDocument) object;
                out.print(document.getNumber());
                out.print("<br>");
            }
        }

        tx.commit();
        tx = null;
    } catch (Exception e) {
        e.printStackTrace();
        out.print("Error: " + e.getLocalizedMessage());
    } finally {
        if (tx != null) {
            tx.rollback();
        }
    }

    System.out.println("========test page end========");
%>