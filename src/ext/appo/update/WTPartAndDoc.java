 package ext.appo.update;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Vector;

import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;

import ext.appo.util.DocUtil;
import ext.appo.util.PartUtil;
import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.part.WTPartDescribeLink;
import wt.part.WTPartReferenceLink;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionServerHelper;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.vc.config.LatestConfigSpec;
import wt.vc.views.ViewHelper;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;


public class WTPartAndDoc implements RemoteAccess {
	public WTPartAndDoc() {

	}

	public static String className = WTPartAndDoc.class.getName();

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		String partnumber = null;
		String documentnumber = null;
		String filename = null;

		if (args == null)
			return;
		for (int i = 0; i < args.length; i += 2) {
			if (i + 1 < args.length) {

				if (("-p".equals(args[i]))) {
					partnumber = args[i + 1];
				}
				if (("-d".equals(args[i]))) {
					documentnumber = args[i + 1];
				}
				 if (("-f".equals(args[i]))) {
				    filename = args[i + 1];
				 }

			}
		}
		RemoteMethodServer server = RemoteMethodServer.getDefault();
		server.setUserName("01507");
		server.setPassword("123");

		WTDocument doc =DocUtil.getLatestWTDocument(documentnumber);
		WTPart part = PartUtil.getLastestWTPartByNumber(partnumber);
		String doctypeString = getObjectType(doc);
		String keyString = "wt.doc.WTDocument";
		if (doctypeString.length() >= 43) {
			keyString = doctypeString.substring(0, 43);
		}
		String typeString = "wt.doc.WTDocument|com.ptc.ReferenceDocument";
		System.out.println("keyStirng is " + keyString);
		System.out.println("lenght is " + keyString.length());
		if (typeString.trim().equals(keyString.trim())) {
			createReferenceLink(doc, part);// 创建参考关系
			System.out.println("the referencelink has created!!!!!!!!");
		} else {
			createDescribeLink(part, doc);// 创建描述关系
			System.out.println("the describelink has created!!");

			// createMoreDescribeLink(dataVector);
		}

		/*
		 * System.out.println(">>>>>>>>>>>>>>>>" + doc.getNumber()); WTPart part =
		 * ext.custom.util.test.Searchclass.getPart(partnumber);
		 * System.out.println(">>>>>>>>>>>>>>>" + part.getNumber()); if (type ==
		 * null||!type.equals("re")||!type.equals("de")) {
		 * System.out.println("please input the document and part reference
		 * type: -t ..de..or...re..."); } if (type.equals("re")) {
		 * createReferenceLink(doc, part); } if (type.equals("de")) {
		 * createDescribeLink(part, doc); }
		 */
	}

	public static String getObjectType(Object object) throws WTException
	{
		String type = "";
		boolean flag = true;
		try
		{
			flag = SessionServerHelper.manager.isAccessEnforced();
			SessionServerHelper.manager.setAccessEnforced(false);

			if (object != null)
			{
				TypeIdentifier ti = TypeIdentifierUtility.getTypeIdentifier(object);
				type = ti.getTypename();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
		return type;
	}
	public static void createMoreDescribeLink(Vector objectVector) {
		if (objectVector == null) {
			System.out.println("this vector is null>>");
		} else {
			int count = 0;
			int sum = 0;
			Vector vectordocwrong=new Vector();
			Vector vectorpartwrong=new Vector();
			Vector vectorpartcheckout=new Vector();
			Vector vectordes=new Vector();
			Vector vectorref=new Vector();
			for (int i = 0; i < objectVector.size(); i++) {
				RefObject refObject = (RefObject) objectVector.get(i);
				String partnumberString = refObject.getPartNumber();
				String docnumberString = refObject.getRefdocnumber();

				try {
					WTPart part = getLatestPartByNumber(partnumberString,
							"Design");
					WTDocument doc = getLatestDocByNumber(docnumberString);
					boolean needToCheckin = WorkInProgressHelper.isCheckedOut((Workable) part);
					if(needToCheckin){
						vectorpartcheckout.add(part.getNumber());
					}
                     if(doc==null)
                     {
                    	vectordocwrong.add(docnumberString);
                     }
                     if(doc==null)
                     {
                    	 vectorpartwrong.add(partnumberString);
                     }
					if (doc != null && part != null) {
						String doctypeString = getObjectType(doc);
						String firstString = "wt.doc.WTDocument";
						if (doctypeString.length() >= 43) {
							firstString = doctypeString.substring(0, 43);
						}
						String typeString = "wt.doc.WTDocument|com.ptc.ReferenceDocument";
						if (typeString.equals(firstString)) {
							try {
								createReferenceLink(doc, part);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								refObject.setPartNumber(part.getNumber());
								refObject.setRefdocnumber(doc.getNumber());
								refObject.setRefdocstate("fail");
								vectorref.add(refObject);
								e.printStackTrace();
							}
							count++;
						} else {
							createDescribeLink(part, doc);
							sum++;
						}
					}
				} catch (WTException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println("create>>" + count + ">>reference ship");
			System.out.println("create>>" + sum + ">>DescribeLink ship");
			System.out.println(vectordocwrong);
			System.out.println(vectorpartwrong);
			System.out.println(vectorpartcheckout);
		}

	}

	/*
	 * 创建部件与文档的关系
	 */
	public static void createDescribeLink(WTPart part, WTDocument doc) {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				Class[] aclass = new Class[] { WTPart.class, WTDocument.class };
				Object[] aobj = new Object[] { part, doc };
				System.out.println(1);
				RemoteMethodServer.getDefault().invoke("createDescribeLink",
						className, null, aclass, aobj);
				System.out.println(2);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e){
				e.printStackTrace();
			}
		} else {
			try {
				WTPartDescribeLink descriptionLink = WTPartDescribeLink
						.newWTPartDescribeLink(part, doc);

				PersistenceServerHelper.manager.insert(descriptionLink);

				descriptionLink = (WTPartDescribeLink) PersistenceHelper.manager
						.refresh(descriptionLink);
				System.out.println("the part" + part.getNumber()
						+ "has added document" + doc.getNumber());
			} catch (WTException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 创建part与document之间的参考关系
	 * 
	 * @param document
	 * @param part
	 * @throws Exception
	 */
	public static void createReferenceLink(WTDocument document, WTPart part)
			throws Exception {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				Class[] aclass = new Class[] { WTDocument.class, WTPart.class };
				Object[] aobj = new Object[] { document, part };
				System.out.println(1);
				RemoteMethodServer.getDefault().invoke("createReferenceLink",
						className, null, aclass, aobj);
				System.out.println(2);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			try {
				WTDocumentMaster wtdocumentmaster = (WTDocumentMaster) document
						.getMaster();
				WTPartReferenceLink wtpartreferencelink = getPartReferenceLink(
						part, wtdocumentmaster);
				System.out.println(">>>>>>>>" + wtpartreferencelink);
				if (wtpartreferencelink == null) {
					WTPartReferenceLink wtpartreferencelink1 = WTPartReferenceLink
							.newWTPartReferenceLink(part, wtdocumentmaster);
					PersistenceServerHelper.manager
							.insert(wtpartreferencelink1);
					wtpartreferencelink1 = (WTPartReferenceLink) PersistenceHelper.manager
							.refresh(wtpartreferencelink1);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

	}

	/*
	 * docnumber 文档编号 partnumber 部件编号 创建part与wtdocument之间的关系
	 * 
	 */
	public static WTPartDescribeLink partDescribeLinkanddoc(String docnumber,
			String partnumber) throws Exception {
		WTDocument document =DocUtil.getLatestWTDocument(docnumber);
		WTPart part = PartUtil.getLastestWTPartByNumber(partnumber);
		WTPartDescribeLink partDescribeLinkold = getwtpDescribeLinkdoc(part,
				document);
		if (partDescribeLinkold != null) {
			return partDescribeLinkold;
		} else {
			WTPartDescribeLink pDescribeLink = WTPartDescribeLink
					.newWTPartDescribeLink(part, document);
			if (pDescribeLink == null) {
				System.out.println("null>>>>>>>>>>>>>null");
			} else {
				System.out.println("error>>>>>>>>>>" + pDescribeLink);
			}
			PersistenceServerHelper.manager.insert(pDescribeLink);
			System.out.println("error>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

			pDescribeLink = (WTPartDescribeLink) PersistenceHelper.manager
					.refresh(pDescribeLink);
			WTPartDescribeLink partDescribeLink = getwtpDescribeLinkdoc(part,
					document);
			if (partDescribeLink == null) {
				return null;
			} else {
				return partDescribeLink;
			}
		}

	}

	/*
	 * * 查询part和document之间的关联关系 @param wtpart @param epmdocument @return @throws
	 * WTException
	 */
	public static WTPartDescribeLink getwtpDescribeLinkdoc(WTPart part,
			WTDocument doc) throws Exception {
		QueryResult queryResult = PersistenceHelper.manager.find(
				WTPartDescribeLink.class, part,
				WTPartDescribeLink.DESCRIBES_ROLE, doc);
		if (queryResult == null || queryResult.size() == 0)
			return null;
		else {
			WTPartDescribeLink describeLink = (WTPartDescribeLink) queryResult
					.nextElement();
			return describeLink;
		}
	}

	/**
	 * 查询part与doc之间的参考关系
	 * 
	 * @param wtpart
	 * @param wtdocumentmaster
	 * @return
	 * @throws WTException
	 */
	public static WTPartReferenceLink getPartReferenceLink(WTPart wtpart,
			WTDocumentMaster wtdocumentmaster) throws WTException {
		QueryResult queryresult = PersistenceHelper.manager.find(
				wt.part.WTPartReferenceLink.class, wtpart,
				WTPartReferenceLink.REFERENCED_BY_ROLE, wtdocumentmaster);
		if (queryresult == null || queryresult.size() == 0)
			return null;
		else {
			WTPartReferenceLink wtpartreferencelink = (WTPartReferenceLink) queryresult
					.nextElement();
			return wtpartreferencelink;
		}
	}

	public static WTPart getLatestPartByNumber(String partNumber, String view)
			throws WTException {
		if (partNumber == null || partNumber.length() == 0)
			return null;

		QuerySpec qs = new QuerySpec(WTPart.class);
		SearchCondition scNumber = new SearchCondition(WTPart.class,
				WTPart.NUMBER, SearchCondition.EQUAL, partNumber.toUpperCase());
		SearchCondition scLatestIteration = new SearchCondition(WTPart.class,
				WTAttributeNameIfc.LATEST_ITERATION, SearchCondition.IS_TRUE);
		qs.appendWhere(scNumber);
		qs.appendAnd();
		qs.appendWhere(scLatestIteration);
		if (view != null && view.length() > 0) {
			qs.appendAnd();
			SearchCondition scView = new SearchCondition(WTPart.class,
					"view.key", SearchCondition.EQUAL, PersistenceHelper
							.getObjectIdentifier(ViewHelper.service
									.getView(view)));
			qs.appendWhere(scView);
		}
		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr != null && qr.hasMoreElements())
			qr = (new LatestConfigSpec()).process(qr);

		if (qr != null && qr.hasMoreElements())
			return (WTPart) qr.nextElement();
		return null;
	}

	public static WTDocument getLatestDocByNumber(String docNumber)
			throws WTException {
		if (docNumber == null || docNumber.length() == 0)
			return null;

		QuerySpec qs = new QuerySpec(WTDocument.class);
		SearchCondition scNumber = new SearchCondition(WTDocument.class,
				WTDocument.NUMBER, SearchCondition.EQUAL, docNumber
						.toUpperCase());
		SearchCondition scLatestIteration = new SearchCondition(
				WTDocument.class, WTAttributeNameIfc.LATEST_ITERATION,
				SearchCondition.IS_TRUE);
		qs.appendWhere(scNumber);
		qs.appendAnd();
		qs.appendWhere(scLatestIteration);

		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr != null && qr.hasMoreElements())
			qr = (new LatestConfigSpec()).process(qr);

		if (qr != null && qr.hasMoreElements())
			return (WTDocument) qr.nextElement();

		return null;
	}

}
