package ext.generic.borrow.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import wt.access.agreement.AuthorizationAgreement;
import wt.annotation.AnnotationSet;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeInvestigation;
import wt.change2.WTChangeIssue;
import wt.change2.WTChangeOrder2;
import wt.change2.WTChangeProposal;
import wt.change2.WTChangeRequest2;
import wt.doc.WTDocument;
import wt.enterprise.Managed;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMDocument;
import wt.fc.Named;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectReference;
import wt.fc.PersistInfo;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.iba.definition.StringDefinition;
import wt.iba.value.StringValue;
import wt.identity.IdentityFactory;
import wt.inf.container.WTContained;
import wt.inf.container.WTContainer;
import wt.inf.library.WTLibrary;
import wt.method.RemoteAccess;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.pdmlink.PDMLinkProduct;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.type.TypedUtility;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.vc.config.LatestConfigSpec;

import com.ptc.core.components.rendering.guicomponents.UrlDisplayComponent;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.type.mgmt.server.impl.TypeDomainHelper;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmURLFactoryBean;
import com.ptc.netmarkets.util.misc.NetmarketURL;

import ext.appo.ecn.common.util.ChangeUtils;
import ext.generic.esignature.util.ReadConfigUtil;
import ext.lang.PIStringUtils;
import ext.pi.core.PICoreHelper;


public class BorrowUtil implements RemoteAccess{
	
	public static WTProperties wtProperties;
	private static String RESOURCE = "ext.generic.borrow.borrowResource";
	public static String WTDocumentStates="";
	public static String WTPartStates="";
	public static String EPMDocumentStates="";
	static
	{
		try
		{
			wtProperties = WTProperties.getLocalProperties();
			//VERBOSE = wtProperties.getProperty("ext.generic.borroworder.verbose", false);
			ReadConfigUtil config = ReadConfigUtil.getInstance();
            Map configs = config.readProperties();
            WTDocumentStates =  (String)configs.get("WTDocumentBorrowStates");
            WTPartStates =  (String)configs.get("WTPartBorrowStates");
            EPMDocumentStates =  (String)configs.get("EPMDocumentBorrowStates");
		}
		catch (Throwable throwable)
		{
			throw new ExceptionInInitializerError(throwable);
		}
	}
	/**
	 *读取对象的编号链接
	 * 
	 * @param wtobject
	 *            对象；
	 * @return AttributeGuiComponent 编号链接组件
	 * @throws WTException
	 */
	public static UrlDisplayComponent getURLObjectByNumber(WTObject wtobject) throws WTException
	{
		String s = "";
		UrlDisplayComponent urldisplaycomponent = null;
		NmOid nmoid = new NmOid(wtobject.getPersistInfo().getObjectIdentifier());
		NmURLFactoryBean nmurlfactorybean = new NmURLFactoryBean();
		nmurlfactorybean.setRequestURI(NetmarketURL.BASEURL);
		String s1 = NetmarketURL.buildURL(nmurlfactorybean, "borrowOrder", "viewInNewPage", nmoid, null);
		if (wtobject instanceof WTPart)
		{
			s = ((WTPart) wtobject).getNumber();
		}
		else if (wtobject instanceof WTDocument)
		{
			s = ((WTDocument) wtobject).getNumber();
		}
		else if (wtobject instanceof EPMDocument)
		{
			s = ((EPMDocument) wtobject).getNumber();
		}
		else if (wtobject instanceof PDMLinkProduct)
		{
			s = ((PDMLinkProduct) wtobject).getName();
		}
		else if (wtobject instanceof WTLibrary)
		{
			s = ((WTLibrary) wtobject).getName();
		}
		else if (wtobject instanceof WTChangeIssue)
		{
			WTChangeIssue wtchangeissue = (WTChangeIssue) wtobject;
			s=wtchangeissue.getNumber();		
		}
		else if (wtobject instanceof WTChangeOrder2)
		{
			WTChangeOrder2 wtchangeorder2 = (WTChangeOrder2) wtobject;
			s=wtchangeorder2.getNumber();			
		}
		else if (wtobject instanceof WTChangeRequest2)
		{
			WTChangeRequest2 wtchangerequest2 = (WTChangeRequest2) wtobject;
			s = wtchangerequest2.getNumber();
		}
		else if (wtobject instanceof WTChangeActivity2)
		{
			WTChangeActivity2 wtchangeactivity2 = (WTChangeActivity2) wtobject;
			s=wtchangeactivity2.getNumber();			
		}
		else if (wtobject instanceof WTChangeProposal)
		{
			WTChangeProposal wtchangeproposal = (WTChangeProposal) wtobject;
			s = wtchangeproposal.getNumber();			
		}
		else if (wtobject instanceof WTChangeInvestigation)
		{
			WTChangeInvestigation wtchangeinvestigation = (WTChangeInvestigation) wtobject;
			s =wtchangeinvestigation.getNumber();		
		}
		else if (wtobject instanceof AnnotationSet)
		{
			s = ((AnnotationSet) wtobject).getName();
			s1 = NetmarketURL.buildURL(nmurlfactorybean, "pdmObject", "CLASSICPIE", nmoid, null);
		}
		else if (wtobject instanceof AuthorizationAgreement)
		{
			s = ((AuthorizationAgreement) wtobject).getNumber();
		}
		else if (wtobject instanceof Managed)
		{
			s = ((Managed) wtobject).getName();
			if (s == null)
			{
				s = IdentityFactory.getDisplayIdentity(wtobject).getLocalizedMessage(SessionHelper.manager.getLocale());
			}
		}
		else if (wtobject instanceof Named)
		{
			s = ((Named) wtobject).getName();
		}
		
		else if (wtobject instanceof WTContained)
		{
			s = IdentityFactory.getDisplayIdentity(wtobject).getLocalizedMessage(SessionHelper.manager.getLocale());
		}
		else
		{
			s = wtobject.getDisplayIdentifier().getLocalizedMessage(SessionHelper.manager.getLocale());
		}
		urldisplaycomponent = new UrlDisplayComponent(s, s, s1);
		return urldisplaycomponent;
	}
	/**
	 * 查找借阅对象页面上选择“对象类型”
	 * @throws Hashtable
	 */
	public static Vector getBorrowObjectType() throws WTException
	{
		Vector returnvc = new Vector();
		ArrayList disparray=new ArrayList();
		ArrayList vaulearray=new ArrayList();		
		Locale locale=SessionHelper.getLocale();
		String domain = TypeDomainHelper.getExchangeDomain();
		disparray.add("全部");
		vaulearray.add("ALL");
		disparray.add("文档");
		vaulearray.add("wt.doc.WTDocument");
		// 读取DHF及DHF底下的子类型
		/*TypeIdentifier typeidentifierDHF = TypeHelper.getTypeIdentifier("wt.doc.WTDocument|com.ptc.ReferenceDocument|" + domain + ".DHFDoc");
		disparray.add("DHF文档");
		vaulearray.add(typeidentifierDHF.getTypename());
		if (typeidentifierDHF != null)
		{
			Set set = com.ptc.core.htmlcomp.util.TypeHelper.getSubTypesForType(typeidentifierDHF, locale);
			Iterator iterator = set.iterator();
			String entryname = null;
			String entrytype = null;
			String preflex = "    ";
			while (iterator.hasNext())
			{
				TypeIdentifier entry = (TypeIdentifier) iterator.next();
				if(typeidentifierDHF.equals(entry))
					continue;
				entryname = TypeHelper.getLocalizedTypeString(entry, locale);
				entrytype = entry.getTypename();
				if (entryname != null && entrytype != null)
				{
					disparray.add(preflex + entryname);
					vaulearray.add(entrytype);
				}
			}
		}*/
		// 读取DMR及DMR底下的子类型
		/*TypeIdentifier typeidentifierDMR = TypeHelper.getTypeIdentifier("wt.doc.WTDocument|com.ptc.ReferenceDocument|" + domain + ".DMRDoc");
		disparray.add("DMR文档");
		vaulearray.add(typeidentifierDMR.getTypename());
		if (typeidentifierDMR != null)
		{
			
			Set set = com.ptc.core.htmlcomp.util.TypeHelper.getSubTypesForType(typeidentifierDMR, locale);
			Iterator iterator = set.iterator();
			String entryname = null;
			String entrytype = null;
			String preflex = "    ";
			while (iterator.hasNext())
			{
				TypeIdentifier entry = (TypeIdentifier) iterator.next();
				if(typeidentifierDMR.equals(entry))
					continue;
				entryname = TypeHelper.getLocalizedTypeString(entry, locale);
				entrytype = entry.getTypename();
				if (entryname != null && entrytype != null)
				{
					disparray.add(preflex + entryname);
					vaulearray.add(entrytype);
				}
			}
		}*/
		/*disparray.add("全部DMR");
		vaulearray.add("ALLDMR");*/
		
		disparray.add("部件");
		vaulearray.add("wt.part.WTPart");
		
		disparray.add("EPM文档");
		vaulearray.add("wt.epm.EPMDocument");
		
		/*disparray.add("全部体系文件");
		vaulearray.add("ALLQualitySystem");
		
		disparray.add("    质量体系文件");
		vaulearray.add("wt.doc.WTDocument|com.ptc.ReferenceDocument|" + domain + ".QualitySystemDocumentsDoc");
		
		disparray.add("    TC质量体系文件");
		vaulearray.add("wt.doc.WTDocument|com.ptc.ReferenceDocument|" + domain + ".TCQualitySystemDocumentsDoc");*/
		
		returnvc.add(disparray);
		returnvc.add(vaulearray);
		return returnvc;
		
	}
	/**
	 * 通过各个参数查询借阅对象
	 * 
	 * @param number
	 *            编号
	 * @param name
	 *            名称
	 * @param contoid
	 *            库的oid
	 * @param objectype
	 *            查找的类型
	 */
	public static ArrayList searchBorrowObject(String number, String name,String contoid,String objectype) throws WTException
	{
		System.out.println("Debug 搜索参数："+number+"   "+name+"   "+contoid+"   "+objectype);
		ArrayList alist=new ArrayList();
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		try
		{
			if((number==null||number.trim().length()==0)&&(name==null||name.trim().length()==0)&&(contoid==null||contoid.trim().length()==0))
				return alist;
			WTContainer container=null;
			if(contoid!=null&&contoid.trim().length()>0&&!contoid.equalsIgnoreCase("null"))
			{
				ReferenceFactory referencefactory = new ReferenceFactory();
				container= (WTContainer)referencefactory.getReference(contoid).getObject();
			}
			QuerySpec wtpartQs = new QuerySpec(WTPart.class);
			QuerySpec docQs = new QuerySpec(WTDocument.class);
			QuerySpec epmQs = new QuerySpec(EPMDocument.class);
			boolean hasCondition=false;
			if(number!=null&&number.trim().length()>0)
			{
				SearchCondition wtpartsc = new SearchCondition(WTPart.class, "master>number", "LIKE", number.replace('*', '%'),false);
				SearchCondition docsc = new SearchCondition(WTDocument.class, "master>number", "LIKE", number.replace('*', '%'),false);
				SearchCondition epmsc = new SearchCondition(EPMDocument.class, "master>number", "LIKE", number.replace('*', '%'),false);
				wtpartQs.appendWhere(wtpartsc);
				docQs.appendWhere(docsc);
				epmQs.appendWhere(epmsc);
				hasCondition = true;
			}
			if(name!=null&&name.trim().length()>0)
			{
				SearchCondition wtpartsc = new SearchCondition(WTPart.class, "master>name", "LIKE", name.replace('*', '%'),false);
				SearchCondition docsc = new SearchCondition(WTDocument.class, "master>name", "LIKE", name.replace('*', '%'),false);
				SearchCondition epmsc = new SearchCondition(EPMDocument.class, "master>name", "LIKE", name.replace('*', '%'),false);
				if (hasCondition)
				{
					wtpartQs.appendAnd();
					docQs.appendAnd();
					epmQs.appendAnd();
				}
				wtpartQs.appendWhere(wtpartsc);
				docQs.appendWhere(docsc);
				epmQs.appendWhere(epmsc);
				hasCondition = true;
			}
			//如果选择了产品库或者存储库，则只搜索该库下的内容。
			if(container!=null)
			{
				System.out.println("Debug  当前容器的名称："+container.getName());
				ObjectIdentifier contidfier=PersistenceHelper.getObjectIdentifier(container);
				SearchCondition wtpartsc = new SearchCondition(WTPart.class, "containerReference.key",SearchCondition.EQUAL,contidfier );
				SearchCondition docsc = new SearchCondition(WTDocument.class, "containerReference.key",SearchCondition.EQUAL, contidfier);
				SearchCondition epmsc = new SearchCondition(EPMDocument.class, "containerReference.key",SearchCondition.EQUAL, contidfier);
				if (hasCondition)
				{
					wtpartQs.appendAnd();
					docQs.appendAnd();
					epmQs.appendAnd();
				}
				wtpartQs.appendWhere(wtpartsc);
				docQs.appendWhere(docsc);
				epmQs.appendWhere(epmsc);
				hasCondition = true;
			}
			//如果没选择产品库或者存储库，则搜索所有产品库和者存储库的内容。
			else
			{
				System.out.println("Debug  当前容器为空");
				if (hasCondition)
				{
					wtpartQs.appendAnd();
					docQs.appendAnd();
					epmQs.appendAnd();
				}
				wtpartQs.appendOpenParen();
				SearchCondition sc0=new SearchCondition(WTPart.class,"containerReference.key.classname",SearchCondition.EQUAL,"wt.pdmlink.PDMLinkProduct");
				wtpartQs.appendWhere(sc0);
				wtpartQs.appendOr();
				SearchCondition sc1=new SearchCondition(WTPart.class,"containerReference.key.classname",SearchCondition.EQUAL,"wt.inf.library.WTLibrary");
				wtpartQs.appendWhere(sc1);
				wtpartQs.appendCloseParen();
				
				docQs.appendOpenParen();
				SearchCondition sc00=new SearchCondition(WTDocument.class,"containerReference.key.classname",SearchCondition.EQUAL,"wt.pdmlink.PDMLinkProduct");
				docQs.appendWhere(sc00);
				docQs.appendOr();
				SearchCondition sc11=new SearchCondition(WTDocument.class,"containerReference.key.classname",SearchCondition.EQUAL,"wt.inf.library.WTLibrary");
				docQs.appendWhere(sc11);
				docQs.appendCloseParen();
				
				epmQs.appendOpenParen();
				SearchCondition sc000=new SearchCondition(EPMDocument.class,"containerReference.key.classname",SearchCondition.EQUAL,"wt.pdmlink.PDMLinkProduct");
				epmQs.appendWhere(sc000);
				epmQs.appendOr();
				SearchCondition sc111=new SearchCondition(EPMDocument.class,"containerReference.key.classname",SearchCondition.EQUAL,"wt.inf.library.WTLibrary");
				epmQs.appendWhere(sc111);
				epmQs.appendCloseParen();
				hasCondition = true;
			}
			
			//必须是发布过后的对象
			if (hasCondition)
			{
				wtpartQs.appendAnd();
				docQs.appendAnd();
				epmQs.appendAnd();
			}
			
			
			wtpartQs.appendOpenParen();
//			SearchCondition sc0=new SearchCondition(WTPart.class,"state.state",SearchCondition.EQUAL,"PROTOTYPE");
//			wtpartQs.appendWhere(sc0);
//			/*wtpartQs.appendOr();
//			SearchCondition sc1=new SearchCondition(WTPart.class,"state.state",SearchCondition.EQUAL,"RELEASED");
//			wtpartQs.appendWhere(sc1);*/
//			wtpartQs.appendOr();
//			SearchCondition sc2=new SearchCondition(WTPart.class,"state.state",SearchCondition.EQUAL,"PRODUCTION_STAGE");
//			wtpartQs.appendWhere(sc2);
//			wtpartQs.appendOr();
//			SearchCondition sc3=new SearchCondition(WTPart.class,"state.state",SearchCondition.EQUAL,"MODEL_STAGE");
//			wtpartQs.appendWhere(sc3);
			//读取资源文件得到的值，判断是否有多个。如果有则分割循环添加，没有则直接添加。
			if(WTPartStates.indexOf(",")!=-1){
				String[] partStates=WTPartStates.split(",");
				for (int i = 0; i < partStates.length; i++) {
					SearchCondition sc=new SearchCondition(WTPart.class,"state.state",SearchCondition.EQUAL,partStates[i].trim());
					wtpartQs.appendWhere(sc);
					if(i!=partStates.length-1){
						wtpartQs.appendOr();
					}
				}
			}else{
				SearchCondition sc=new SearchCondition(WTPart.class,"state.state",SearchCondition.EQUAL,WTPartStates.trim());
				wtpartQs.appendWhere(sc);
			}		
			wtpartQs.appendCloseParen();
			
			
			docQs.appendOpenParen();
			/*SearchCondition sc00=new SearchCondition(WTDocument.class,"state.state",SearchCondition.EQUAL,"EFFECTED");
			docQs.appendWhere(sc00);
			docQs.appendOr();*/
			/*SearchCondition sc11=new SearchCondition(WTDocument.class,"state.state",SearchCondition.EQUAL,"RELEASED");
			docQs.appendWhere(sc11);*/
			/*docQs.appendOr();
			SearchCondition sc22=new SearchCondition(WTDocument.class,"state.state",SearchCondition.EQUAL,"HISTORY");
			docQs.appendWhere(sc22);
			docQs.appendOr();
			SearchCondition sc33=new SearchCondition(WTDocument.class,"state.state",SearchCondition.EQUAL,"INVALID");
			docQs.appendWhere(sc33);*/
			//读取资源文件得到的值，判断是否有多个。如果有则分割循环添加，没有则直接添加。
			if(WTDocumentStates.indexOf(",")!=-1){
				String[] wtDocStates=WTDocumentStates.split(",");
				for (int i = 0; i < wtDocStates.length; i++) {
					SearchCondition sc=new SearchCondition(WTDocument.class,"state.state",SearchCondition.EQUAL,wtDocStates[i].trim());
					docQs.appendWhere(sc);
					if(i!=wtDocStates.length-1){
						docQs.appendOr();						
					}
				}
			}else{
				SearchCondition sc=new SearchCondition(WTDocument.class,"state.state",SearchCondition.EQUAL,WTDocumentStates.trim());
				docQs.appendWhere(sc);
			}
			docQs.appendCloseParen();
			epmQs.appendOpenParen();
//			SearchCondition sc000=new SearchCondition(EPMDocument.class,"state.state",SearchCondition.EQUAL,"PROTOTYPE");
//			epmQs.appendWhere(sc000);
//			/*epmQs.appendOr();
//			SearchCondition sc111=new SearchCondition(EPMDocument.class,"state.state",SearchCondition.EQUAL,"RELEASED");
//			epmQs.appendWhere(sc111);*/
//			epmQs.appendOr();
//			SearchCondition sc222=new SearchCondition(EPMDocument.class,"state.state",SearchCondition.EQUAL,"PRODUCTION_STAGE");
//			epmQs.appendWhere(sc222);
//			epmQs.appendOr();
//			SearchCondition sc333=new SearchCondition(EPMDocument.class,"state.state",SearchCondition.EQUAL,"MODEL_STAGE");
//			epmQs.appendWhere(sc333);
			//读取资源文件得到的值，判断是否有多个。如果有则分割循环添加，没有则直接添加。
			if(EPMDocumentStates.indexOf(",")!=-1){
				String[] epmDocStates=EPMDocumentStates.split(",");
				for (int i = 0; i < epmDocStates.length; i++) {
					SearchCondition sc=new SearchCondition(EPMDocument.class,"state.state",SearchCondition.EQUAL,epmDocStates[i].trim());
					epmQs.appendWhere(sc);
					if(i!=epmDocStates.length-1){
						epmQs.appendOr();
					}
				}
			}else{
				SearchCondition sc=new SearchCondition(EPMDocument.class,"state.state",SearchCondition.EQUAL,EPMDocumentStates.trim());
				epmQs.appendWhere(sc);
			}
			epmQs.appendCloseParen();
			
			//每个大版本的最新小版本
			wtpartQs = new LatestConfigSpec().appendSearchCriteria(wtpartQs);
			docQs = new LatestConfigSpec().appendSearchCriteria(docQs);
			epmQs = new LatestConfigSpec().appendSearchCriteria(epmQs);
			
			//文档的同一个master的不同版本类型是一致的，所以为了增强搜索性能，做缓存以减少重复判断
			Vector mastertempvc=new Vector();
			//全部对象
			if(objectype==null||objectype.trim().length()==0||objectype.trim().equalsIgnoreCase("ALL"))
			{
				QueryResult	wtpartQr = PersistenceHelper.manager.find(wtpartQs);
				QueryResult	docQr = PersistenceHelper.manager.find(docQs);
				QueryResult	epmQr = PersistenceHelper.manager.find(epmQs);
				while(docQr!=null&&docQr.hasMoreElements())
				{
					/*//DHF,DMR,体系文件
					WTDocument doc=(WTDocument)docQr.nextElement();
					if(mastertempvc.contains(doc.getMaster()))
					{
						alist.add(doc);
					}
					else
					{
						TypeIdentifier typeidentifier = TypedUtility.getTypeIdentifier(doc);
						String typeName= typeidentifier.getTypename();
						if(typeName.indexOf("DHFDoc")>=0
								||typeName.indexOf("DMRDoc")>=0
								||typeName.indexOf("QualitySystemDocumentsDoc")>=0
								||typeName.indexOf("TCQualitySystemDocumentsDoc")>=0)
						{
							alist.add(doc);
							mastertempvc.add(doc.getMaster());
						}
					}*/
					alist.add(docQr.nextElement());
				}
				//所有的EPM
				while(epmQr!=null&&epmQr.hasMoreElements())
				{
					alist.add(epmQr.nextElement());
				}
				//所以的部件过滤其他条件
				while(wtpartQr!=null&&wtpartQr.hasMoreElements())
				{
					alist.add(wtpartQr.nextElement());
				}
			}
			//全部DMR：包括部件，epm，和DMR类型文档
		/*	else if(objectype.equalsIgnoreCase("ALLDMR"))
			{
				QueryResult	wtpartQr = PersistenceHelper.manager.find(wtpartQs);
				QueryResult	docQr = PersistenceHelper.manager.find(docQs);
				QueryResult	epmQr = PersistenceHelper.manager.find(epmQs);
				//DMRDoc
				while(docQr!=null&&docQr.hasMoreElements())
				{
					WTDocument doc=(WTDocument)docQr.nextElement();
					if(mastertempvc.contains(doc.getMaster()))
					{
						alist.add(doc);
					}
					else
					{
						TypeIdentifier typeidentifier = TypedUtility.getTypeIdentifier(doc);
						String typeName= typeidentifier.getTypename();
						if(typeName.indexOf("DMRDoc")>=0)
						{
							alist.add(doc);
							mastertempvc.add(doc.getMaster());
						}
					}
				}
				//所有的EPM
				while(epmQr!=null&&epmQr.hasMoreElements())
				{
					alist.add(epmQr.nextElement());
				}
				//所以的部件过滤其他条件
				while(wtpartQr!=null&&wtpartQr.hasMoreElements())
				{
					alist.add(wtpartQr.nextElement());
				}
			}*/
			//部件
			else if(objectype.equalsIgnoreCase("wt.part.WTPart"))
			{
				QueryResult	wtpartQr = PersistenceHelper.manager.find(wtpartQs);
				//所以的部件过滤其他条件
				while(wtpartQr!=null&&wtpartQr.hasMoreElements())
				{
					alist.add(wtpartQr.nextElement());
				}
			}
			//epm文档
			else if(objectype.equalsIgnoreCase("wt.epm.EPMDocument"))
			{
				QueryResult	epmQr = PersistenceHelper.manager.find(epmQs);
				//所以的部件过滤其他条件
				while(epmQr!=null&&epmQr.hasMoreElements())
				{
					alist.add(epmQr.nextElement());
				}
			}
			else if(objectype.equalsIgnoreCase("wt.doc.WTDocument"))
			{
				QueryResult	docQr = PersistenceHelper.manager.find(docQs);
				//所以的部件过滤其他条件
				while(docQr!=null&&docQr.hasMoreElements())
				{
					alist.add(docQr.nextElement());
				}
			}
			//全部体系文档
			/*else if(objectype.equalsIgnoreCase("ALLQualitySystem"))
			{
				QueryResult	docQr = PersistenceHelper.manager.find(docQs);
				//DMRDoc
				while(docQr!=null&&docQr.hasMoreElements())
				{
					WTDocument doc=(WTDocument)docQr.nextElement();
					if(mastertempvc.contains(doc.getMaster()))
					{
						alist.add(doc);
					}
					else
					{
						TypeIdentifier typeidentifier = TypedUtility.getTypeIdentifier(doc);
						String typeName= typeidentifier.getTypename();
						if(typeName.indexOf(".QualitySystemDocumentsDoc")>=0||typeName.indexOf(".TCQualitySystemDocumentsDoc")>=0)
						{
							alist.add(doc);
							mastertempvc.add(doc.getMaster());
						}
					}
				}
			}*/
			else 
			{
				QueryResult	docQr = PersistenceHelper.manager.find(docQs);
				while(docQr!=null&&docQr.hasMoreElements())
				{
					WTDocument doc=(WTDocument)docQr.nextElement();
					if(mastertempvc.contains(doc.getMaster()))
					{
						alist.add(doc);
					}
					else
					{
						TypeIdentifier typeidentifier = TypedUtility.getTypeIdentifier(doc);
						String typeName= typeidentifier.getTypename();
						if(typeName.indexOf(objectype)>=0)
						{
							alist.add(doc);
							mastertempvc.add(doc.getMaster());
						}
					}
				}
			}
			return alist;
		}
		finally
		{
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
	}
	
	/**
	 * 通过各个参数查询对象
	 * 
	 * @param number
	 *            编号
	 * @param name
	 *            名称
	 * @param describe
	 *            规格描述
	 * @param contoid
	 *            库的oid
	 * @param objectype
	 *            查找的类型
	 */
	@SuppressWarnings({ "rawtypes", "deprecation", "unchecked" })
	public static ArrayList searchBorrowObject(String number, String name, String describe, String contoid, String objectype) throws WTException {
		System.out.println("Debug 搜索参数：" + number + "   " + name + "   " + describe + "   "
				+ contoid + "   " + objectype);
		ArrayList alist = new ArrayList();
		if (PIStringUtils.isNull(number) && PIStringUtils.isNull(name) && PIStringUtils.isNull(describe)){
			return alist;
		}
		
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		try {
			WTContainer container = null;
			if (PIStringUtils.isNotNull(contoid) && !contoid.equalsIgnoreCase("null")) {
				ReferenceFactory referencefactory = new ReferenceFactory();
				container = (WTContainer) referencefactory.getReference(contoid).getObject();
			}
			
			QuerySpec wtpartQs = new QuerySpec();
			int partIndex = wtpartQs.appendClassList(WTPart.class, true);
			int valueIndex = 1 ;
			int definitionIndex = 2 ;
			if(PIStringUtils.isNotNull(describe)){
				valueIndex = wtpartQs.appendClassList(StringValue.class, false);
				definitionIndex = wtpartQs.appendClassList(StringDefinition.class, false);
			}
			
			QuerySpec docQs = new QuerySpec(WTDocument.class);
			QuerySpec epmQs = new QuerySpec(EPMDocument.class);
			boolean hasCondition = false;
			if (PIStringUtils.isNotNull(number)) {
				SearchCondition wtpartsc = new SearchCondition(WTPart.class, "master>number", "LIKE", number.replace('*', '%'), false);
				SearchCondition docsc = new SearchCondition(WTDocument.class, "master>number", "LIKE", number.replace('*', '%'), false);
				SearchCondition epmsc = new SearchCondition(EPMDocument.class, "master>number", "LIKE", number.replace('*', '%'), false);
				wtpartQs.appendWhere(wtpartsc, new int[]{partIndex});
				docQs.appendWhere(docsc);
				epmQs.appendWhere(epmsc);
				hasCondition = true;
			}
			if (PIStringUtils.isNotNull(name)) {
				SearchCondition wtpartsc = new SearchCondition(WTPart.class, "master>name", "LIKE", name.replace('*', '%'), false);
				SearchCondition docsc = new SearchCondition(WTDocument.class, "master>name", "LIKE", name.replace('*', '%'), false);
				SearchCondition epmsc = new SearchCondition(EPMDocument.class, "master>name", "LIKE", name.replace('*', '%'), false);
				if (hasCondition) {
					wtpartQs.appendAnd();
					docQs.appendAnd();
					epmQs.appendAnd();
				}
				wtpartQs.appendWhere(wtpartsc, new int[]{partIndex});
				docQs.appendWhere(docsc);
				epmQs.appendWhere(epmsc);
				hasCondition = true;
			}
			// 如果选择了产品库或者存储库，则只搜索该库下的内容。
			if (container != null) {
				System.out.println("Debug  当前容器的名称：" + container.getName());
				ObjectIdentifier contidfier = PersistenceHelper.getObjectIdentifier(container);
				SearchCondition wtpartsc = new SearchCondition(WTPart.class,"containerReference.key", SearchCondition.EQUAL, contidfier);
				SearchCondition docsc = new SearchCondition(WTDocument.class, "containerReference.key", SearchCondition.EQUAL, contidfier);
				SearchCondition epmsc = new SearchCondition(EPMDocument.class, "containerReference.key", SearchCondition.EQUAL, contidfier);
				if (hasCondition) {
					wtpartQs.appendAnd();
					docQs.appendAnd();
					epmQs.appendAnd();
				}
				wtpartQs.appendWhere(wtpartsc, new int[]{partIndex});
				docQs.appendWhere(docsc);
				epmQs.appendWhere(epmsc);
				hasCondition = true;
			}
			// 如果没选择产品库或者存储库，则搜索所有产品库和者存储库的内容。
			else {
				System.out.println("Debug  当前容器为空");
				if (hasCondition) {
					wtpartQs.appendAnd();
					docQs.appendAnd();
					epmQs.appendAnd();
				}
				wtpartQs.appendOpenParen();
				SearchCondition sc0 = new SearchCondition(WTPart.class,"containerReference.key.classname", SearchCondition.EQUAL, "wt.pdmlink.PDMLinkProduct");
				wtpartQs.appendWhere(sc0, new int[]{partIndex});
				wtpartQs.appendOr();
				SearchCondition sc1 = new SearchCondition(WTPart.class, "containerReference.key.classname", SearchCondition.EQUAL, "wt.inf.library.WTLibrary");
				wtpartQs.appendWhere(sc1, new int[]{partIndex});
				wtpartQs.appendCloseParen();

				docQs.appendOpenParen();
				SearchCondition sc00 = new SearchCondition(WTDocument.class, "containerReference.key.classname", SearchCondition.EQUAL, "wt.pdmlink.PDMLinkProduct");
				docQs.appendWhere(sc00);
				docQs.appendOr();
				SearchCondition sc11 = new SearchCondition(WTDocument.class, "containerReference.key.classname", SearchCondition.EQUAL, "wt.inf.library.WTLibrary");
				docQs.appendWhere(sc11);
				docQs.appendCloseParen();

				epmQs.appendOpenParen();
				SearchCondition sc000 = new SearchCondition(EPMDocument.class, "containerReference.key.classname", SearchCondition.EQUAL, "wt.pdmlink.PDMLinkProduct");
				epmQs.appendWhere(sc000);
				epmQs.appendOr();
				SearchCondition sc111 = new SearchCondition(EPMDocument.class, "containerReference.key.classname", SearchCondition.EQUAL, "wt.inf.library.WTLibrary");
				epmQs.appendWhere(sc111);
				epmQs.appendCloseParen();
				hasCondition = true;
			}

			// 必须是发布过后的对象
			if (hasCondition) {
				wtpartQs.appendAnd();
				docQs.appendAnd();
				epmQs.appendAnd();
			}

			wtpartQs.appendOpenParen();
			// 读取资源文件得到的值，判断是否有多个。如果有则分割循环添加，没有则直接添加。
			if (WTPartStates.indexOf(",") != -1) {
				String[] partStates = WTPartStates.split(",");
				for (int i = 0; i < partStates.length; i++) {
					SearchCondition sc = new SearchCondition(WTPart.class, "state.state", SearchCondition.EQUAL, partStates[i].trim());
					wtpartQs.appendWhere(sc, new int[]{partIndex});
					if (i != partStates.length - 1) {
						wtpartQs.appendOr();
					}
				}
			} else {
				SearchCondition sc = new SearchCondition(WTPart.class, "state.state", SearchCondition.EQUAL, WTPartStates.trim());
				wtpartQs.appendWhere(sc, new int[]{partIndex});
			}
			wtpartQs.appendCloseParen();
			
			// 添加规格描述搜索
			if(PIStringUtils.isNotNull(describe)){
				if(describe.contains("*")){
					describe = describe.replace("*", "%") ;
				}
				wtpartQs.appendAnd() ;
				SearchCondition sc = new SearchCondition(StringDefinition.class, StringDefinition.NAME, SearchCondition.EQUAL, "ggms");
				wtpartQs.appendWhere(sc, new int[] { definitionIndex });
				
				wtpartQs.appendAnd() ;
				String defKeyId = StringValue.DEFINITION_REFERENCE + "." + ObjectReference.KEY + "." + ObjectIdentifier.ID;
				String defIda2a2 = StringDefinition.PERSIST_INFO + "." + PersistInfo.OBJECT_IDENTIFIER + "." + ObjectIdentifier.ID;
				sc = new SearchCondition(StringValue.class, defKeyId, StringDefinition.class, defIda2a2);
				wtpartQs.appendWhere(sc, new int[] {valueIndex, definitionIndex});
				
				wtpartQs.appendAnd() ;
				sc = new SearchCondition(StringValue.class, StringValue.VALUE, SearchCondition.LIKE, describe);
				wtpartQs.appendWhere(sc, new int[] {valueIndex});
				
				wtpartQs.appendAnd() ;
				String ptKeyId = StringValue.IBAHOLDER_REFERENCE + "." + ObjectReference.KEY + "." + ObjectIdentifier.ID;
				String ptIda2a2 = WTPart.PERSIST_INFO + "." + PersistInfo.OBJECT_IDENTIFIER + "." + ObjectIdentifier.ID;
				sc = new SearchCondition(StringValue.class, ptKeyId, WTPart.class, ptIda2a2);
				wtpartQs.appendWhere(sc, new int[] {valueIndex, partIndex});
			}

			docQs.appendOpenParen();
			// 读取资源文件得到的值，判断是否有多个。如果有则分割循环添加，没有则直接添加。
			if (WTDocumentStates.indexOf(",") != -1) {
				String[] wtDocStates = WTDocumentStates.split(",");
				for (int i = 0; i < wtDocStates.length; i++) {
					SearchCondition sc = new SearchCondition(WTDocument.class, "state.state", SearchCondition.EQUAL, wtDocStates[i].trim());
					docQs.appendWhere(sc);
					if (i != wtDocStates.length - 1) {
						docQs.appendOr();
					}
				}
			} else {
				SearchCondition sc = new SearchCondition(WTDocument.class, "state.state", SearchCondition.EQUAL, WTDocumentStates.trim());
				docQs.appendWhere(sc);
			}
			docQs.appendCloseParen();
			
			epmQs.appendOpenParen();
			// 读取资源文件得到的值，判断是否有多个。如果有则分割循环添加，没有则直接添加。
			if (EPMDocumentStates.indexOf(",") != -1) {
				String[] epmDocStates = EPMDocumentStates.split(",");
				for (int i = 0; i < epmDocStates.length; i++) {
					SearchCondition sc = new SearchCondition(EPMDocument.class,"state.state", SearchCondition.EQUAL, epmDocStates[i].trim());
					epmQs.appendWhere(sc);
					if (i != epmDocStates.length - 1) {
						epmQs.appendOr();
					}
				}
			} else {
				SearchCondition sc = new SearchCondition(EPMDocument.class, "state.state", SearchCondition.EQUAL, EPMDocumentStates.trim());
				epmQs.appendWhere(sc);
			}
			epmQs.appendCloseParen();

			// 每个大版本的最新小版本
			wtpartQs = new LatestConfigSpec().appendSearchCriteria(wtpartQs);
			docQs = new LatestConfigSpec().appendSearchCriteria(docQs);
			epmQs = new LatestConfigSpec().appendSearchCriteria(epmQs);

			// 文档的同一个master的不同版本类型是一致的，所以为了增强搜索性能，做缓存以减少重复判断
			Vector mastertempvc = new Vector();
			// 全部对象
			if (PIStringUtils.isNull(objectype) || objectype.trim().equalsIgnoreCase("ALL")) {
				QueryResult wtpartQr = PersistenceHelper.manager.find(wtpartQs);
				if(PIStringUtils.isNull(describe)){
					QueryResult docQr = PersistenceHelper.manager.find(docQs);
					QueryResult epmQr = PersistenceHelper.manager.find(epmQs);
					while (docQr != null && docQr.hasMoreElements()) {
						alist.add(docQr.nextElement());
					}
					// 所有的EPM
					while (epmQr != null && epmQr.hasMoreElements()) {
						alist.add(epmQr.nextElement());
					}
				}
				// 所以的部件过滤其他条件
				while (wtpartQr != null && wtpartQr.hasMoreElements()) {
					Object[] object = (Object[])wtpartQr.nextElement() ;
					alist.add(object[0]);
				}
			}

			// 部件
			else if (objectype.equalsIgnoreCase("wt.part.WTPart")) {
				QueryResult wtpartQr = PersistenceHelper.manager.find(wtpartQs);
				// 所以的部件过滤其他条件
				while (wtpartQr != null && wtpartQr.hasMoreElements()) {
					Object[] object = (Object[])wtpartQr.nextElement() ;
					alist.add(object[0]);
				}
			}
			// epm文档
			else if (objectype.equalsIgnoreCase("wt.epm.EPMDocument")) {
				QueryResult epmQr = PersistenceHelper.manager.find(epmQs);
				// 所以的部件过滤其他条件
				while (epmQr != null && epmQr.hasMoreElements()) {
					alist.add(epmQr.nextElement());
				}
			} else if (objectype.equalsIgnoreCase("wt.doc.WTDocument")) {
				QueryResult docQr = PersistenceHelper.manager.find(docQs);
				// 所以的部件过滤其他条件
				while (docQr != null && docQr.hasMoreElements()) {
					alist.add(docQr.nextElement());
				}
			}
			// 全部体系文档
			else {
				QueryResult docQr = PersistenceHelper.manager.find(docQs);
				while (docQr != null && docQr.hasMoreElements()) {
					WTDocument doc = (WTDocument) docQr.nextElement();
					if (mastertempvc.contains(doc.getMaster())) {
						alist.add(doc);
					} else {
						TypeIdentifier typeidentifier = TypedUtility.getTypeIdentifier(doc);
						String typeName = typeidentifier.getTypename();
						if (typeName.indexOf(objectype) >= 0) {
							alist.add(doc);
							mastertempvc.add(doc.getMaster());
						}
					}
				}
			}
			
			return getLasterObjects(alist);
		} finally {
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
	}
	
	/***
	 * 获取最新版本对象
	 * 
	 * @param inputArray
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ArrayList getLasterObjects(ArrayList inputArray) throws WTException{
		ArrayList outArray = new ArrayList() ;
		if(inputArray == null || inputArray.size() == 0){
			return outArray ;
		}
		
		try {
			Map<String, Persistable> collectMap = new HashMap<String, Persistable>() ;
			for(Object object : inputArray){
				if(object instanceof ObjectReference){
					object = ((ObjectReference)object).getObject() ;
				}
				if(object instanceof WTPart){
					WTPart part = (WTPart)object ;
					// 关键字
					String key = part.getNumber()+part.getViewName() ;
					if(collectMap.containsKey(key)){
						WTPart oldPart = (WTPart)collectMap.get(key) ;
						if(part.getVersionIdentifier().getSeries().greaterThan(oldPart.getVersionIdentifier().getSeries())){
							collectMap.put(key, part) ;
						}
					}else{
						collectMap.put(key, part) ;
					}
				} else if(object instanceof RevisionControlled){
					RevisionControlled revisionControlled = (RevisionControlled)object ;
					// 获取对象编码
					String rcNumber = ChangeUtils.getNumber(revisionControlled) ;
					if(collectMap.containsKey(rcNumber)){
						RevisionControlled oldRevisionControlled = (RevisionControlled)collectMap.get(rcNumber) ;
						if(PICoreHelper.service.getType(revisionControlled).equals(PICoreHelper.service.getType(oldRevisionControlled))){
							if(revisionControlled.getVersionIdentifier().getSeries().greaterThan(oldRevisionControlled.getVersionIdentifier().getSeries())){
								collectMap.put(rcNumber, revisionControlled) ;
							}
						}
					}else{
						collectMap.put(rcNumber, revisionControlled) ;
					}
				}  else{
					outArray.add(object) ;
				}
			}
			outArray.addAll(collectMap.values()) ;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage()) ;
		}
		
		return outArray ;
	}
	
	/**
	 * 通过名称查询产品或者存储库
	 * 
	 * @param name
	 *            搜索的名称
	 * @throws WTException
	 */
	public static QueryResult searchContextByname(String name) throws WTException
	{
		// 读取当前用户名
		WTUser wtuser = (WTUser) SessionHelper.manager.getPrincipal();
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		try
		{
			QueryResult result = new QueryResult();
			if (name == null || name.trim().length() == 0)
				return result;
			//outDebugInfo("searchContextByname by name=" + name);
			QuerySpec queryspec = new QuerySpec(PDMLinkProduct.class);
			SearchCondition sc = new SearchCondition(PDMLinkProduct.class, "containerInfo.name", SearchCondition.LIKE, name.trim().replace('*', '%'));
			queryspec.appendWhere(sc);
			QueryResult resultPDMlink = PersistenceHelper.manager.find(queryspec);
			if (resultPDMlink != null && resultPDMlink.size() > 0)
				result.appendObjectVector(resultPDMlink.getObjectVector());

			QuerySpec queryspec2 = new QuerySpec(WTLibrary.class);
			SearchCondition sc2 = new SearchCondition(WTLibrary.class, "containerInfo.name", SearchCondition.LIKE, name.trim().replace('*', '%'));
			queryspec2.appendWhere(sc2);
			QueryResult resultlib = PersistenceHelper.manager.find(queryspec2);
			if (resultlib != null && resultlib.size() > 0)
				result.appendObjectVector(resultlib.getObjectVector());
			return result;
		}
		finally
		{
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
	}
}
