package ext.appo.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import ext.appo.part.workflow.PartWorkflowUtil;
import ext.appo.util.DocUtil;
import ext.appo.util.PartUtil;
import ext.pi.PIException;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIDocumentHelper;
import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.enterprise.RevisionControlled;
import wt.fc.IdentityHelper;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectNoLongerExistsException;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.State;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.part.WTPartDescribeLink;
import wt.part.WTPartMasterIdentity;
import wt.part.WTPartReferenceLink;
import wt.pds.StatementSpec;
import wt.pom.PersistenceException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.WhereExpression;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.util.WTRuntimeException;
import wt.vc.config.ConfigHelper;
import wt.vc.config.LatestConfigSpec;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.WorkInProgressException;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;
//ext.holitech.update.UpdateServer
public class checkServerData implements RemoteAccess {
	private static String sepa = File.separator;
	private static String propertiesPath = "ext" + sepa + "appo" + sepa + "cfg";
	private static String propertiesName = "data.xlsx";
	
	public static void main(String[] args) {
		String filename = null;

		if (args == null)
			return;
		for (int i = 0; i < args.length; i += 2) {
			if (i + 1 < args.length) {
				 if (("-f".equals(args[i]))) {
					 propertiesName = args[i + 1];
				 }else{
						try {
							edit();
						} catch (IOException | WTException e) {
							e.printStackTrace();
						}
				 }
			}
		}
		RemoteMethodServer server = RemoteMethodServer.getDefault();
		server.setUserName("wcadmin");
		server.setPassword("wcadmin");
		List<Map> list=new ArrayList<Map>();
		try {
			list = getExcelData();
			check(list);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (WTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static List<Map> getExcelData() throws IOException, WTException {
		Sheet sheetAt = getSheet();
		int lastRowNum = sheetAt.getLastRowNum();
		List<String> attslist = getAttsList(sheetAt);//获取表头
		System.out.println(attslist.size());
		List<List> allValuesList = getAllValuesList(sheetAt, lastRowNum, attslist);
		System.out.println(allValuesList.size());
		List<Map> datalist= getAllEditObj(attslist, allValuesList);
		System.out.println(datalist);
		return datalist;
	}
	
	public static void check(List<Map> datalist) throws WTException {
		StringBuffer message =new StringBuffer();
		if (datalist != null && datalist.size() > 0) {
			for (Map map2 : datalist) {
				Map<Persistable, Map> map = new HashMap<>();
				String partnumber ="";
				partnumber=(String) map2.get("partnumber");
				System.out.println("partnumber=="+partnumber);
				QueryResult partqr=getParts(partnumber);
			
			    while (partqr.hasMoreElements())
				{
					WTPart  oldpart = (WTPart) partqr.nextElement();						
					String statea =oldpart.getState().toString();
					
					System.out.println("state====="+statea);
					if (statea.equalsIgnoreCase("INWORK")) {
						System.out.println("number==:"+partnumber+"-no ok");
					}	

	}
			}
	}
	}
	
	
	public static QueryResult getParts(String number) throws WTException
	{
		StatementSpec stmtSpec = new QuerySpec(WTPart.class);
		WhereExpression where = new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL, number.toUpperCase());
		QuerySpec querySpec = (QuerySpec) stmtSpec;
		querySpec.appendWhere(where, new int[] { 0 });
		QueryResult qr = PersistenceServerHelper.manager.query(stmtSpec);
		return qr;
	}
	/**
     * 移除部件与某个参考文档的关系
     * 
     * @param wtpart
     * @param document
     * @throws WTException
     */
    public static void removeWTPartReferenceLink(WTPart wtpart, WTDocument document) throws WTException {
        QueryResult qr = PersistenceHelper.manager.navigate(wtpart, WTPartReferenceLink.REFERENCES_ROLE, WTPartReferenceLink.class, false);
        while (qr.hasMoreElements()) {
            Object wtobject = (Object) qr.nextElement();
            if (wtobject != null && wtobject instanceof WTPartReferenceLink) {
                WTPartReferenceLink reflink = (WTPartReferenceLink) wtobject;

                WTDocumentMaster theMaster = (WTDocumentMaster) reflink.getReferences();

                QueryResult queryresult = ConfigHelper.service.filteredIterationsOf(theMaster, new LatestConfigSpec());
                if (queryresult != null) {
                    WTDocument theDocument = (WTDocument) queryresult.nextElement();
                    if (document.getNumber().equals(theDocument.getNumber())) {
                        PersistenceHelper.manager.delete(reflink);
                    }
                }
            }
        }
    }
    
	/**
	 * 
	 * Get Describe Link by part and document.
	 * 
	 * @param wtpart
	 *            WTPart : part object
	 * @param wtdocument
	 *            WTDocument : document object
	 * @return WTPartDescribeLink : part and document relationship
	 * @throws WTException
	 *             : exception handling
	 * 
	 * 
	 */
	public  static WTPartDescribeLink getPartDescribeLink(WTPart wtpart, WTDocument wtdocument) throws WTException
	{
		WTPartDescribeLink partLink = null;
		QueryResult queryresult = PersistenceHelper.manager.find(WTPartDescribeLink.class, wtpart, WTPartDescribeLink.DESCRIBES_ROLE, wtdocument);
		if (queryresult != null && queryresult.size() > 0)
		{
			partLink = (WTPartDescribeLink) queryresult.nextElement();
		}
		return partLink;
	}

	/**
	 * 
	 * Get Reference Link by part and document.
	 * 
	 * @param wtpart
	 * @param wtdocument
	 * @return
	 * @throws WTException
	 * @throws RemoteException
	 * @throws InvocationTargetException
	 */
	public static WTPartReferenceLink getPartReferenceLink(WTPart wtpart, WTDocument wtdocument) throws WTException, RemoteException, InvocationTargetException
	{
		int[] index = { 0 };
		QuerySpec querySpec = null;
		querySpec = new QuerySpec(WTPartReferenceLink.class);
		querySpec.appendWhere(new SearchCondition(WTPartReferenceLink.class, WTPartReferenceLink.ROLE_AOBJECT_REF + "." + ObjectReference.KEY, SearchCondition.EQUAL,
				getOid(wtpart)), index);
		querySpec.appendAnd();
		querySpec.appendWhere(new SearchCondition(WTPartReferenceLink.class, WTPartReferenceLink.ROLE_BOBJECT_REF + "." + ObjectReference.KEY, SearchCondition.EQUAL,
				getOid(wtdocument.getMaster())), index);

		QueryResult queryResult = PersistenceHelper.manager.find((StatementSpec) querySpec);
		if (!queryResult.hasMoreElements())
		{
			return null;
		}
		return (WTPartReferenceLink) queryResult.nextElement();
	}
	private  static ObjectIdentifier getOid(Object object)
	{
		if (object != null && object instanceof Persistable)
		{
			return PersistenceHelper.getObjectIdentifier((Persistable) object);
		} else
		{
			throw new WTRuntimeException("Class not handled: " + object.getClass().getName());
		}
	}

	/*
	 * 检出部件
	 */
	public static WTPart checkoutWTPart(WTPart part, String checkOutNote)
			throws WTException, WTPropertyVetoException {
		WTPart workingPart;
		// 判断是否有检出权限)
		if (WorkInProgressHelper.service.isCheckoutAllowed(part)) {
			wt.folder.Folder checkOutFolder = null;
			try {
				checkOutFolder = WorkInProgressHelper.service
						.getCheckoutFolder();
			} catch (WTException e) {
				e.printStackTrace();
			}
			CheckoutLink checkOutLink = null;
			try {
				checkOutLink = WorkInProgressHelper.service.checkout(part,
						checkOutFolder, checkOutNote);
				System.out.println(">>>>>>>>>>>>>>The part has  check out1");
			} catch (WTException e) {
				throw e;
			}
			workingPart = (WTPart) checkOutLink.getWorkingCopy();
		} else {
			workingPart = part;
		}
		return workingPart;
	}
	// 检入部件
	public static WTPart checkinWTPart(WTPart part, String checkInNote)
			throws WTException {
		// 判断是否需要检入部件
		boolean needToCheckin = WorkInProgressHelper
				.isCheckedOut((Workable) part);
		try {
			if (needToCheckin) {
				part = (WTPart) WorkInProgressHelper.service.checkin(part,
						checkInNote);
				System.out.print("check in>>>>>>");
			}
		} catch (WorkInProgressException e) {
			e.printStackTrace();
		} catch (WTPropertyVetoException e) {
			e.printStackTrace();
		} catch (PersistenceException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
		return part;
	}
	/*
	 * 删除部件与文档的说明关系，升级小版本
	 */
	public static void deleteDescribeLinkRemote(WTPartDescribeLink describeLink) {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				Class[] aclass = new Class[] { WTPartDescribeLink.class };
				Object[] aobj = new Object[] { describeLink };
				System.out.println(1);
				RemoteMethodServer.getDefault().invoke("deleteDescribeLinkRemote",
						checkServerData.class.getName(), null, aclass, aobj);
				System.out.println(2);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e){
				e.printStackTrace();
			}
		} else {
			try {
				PersistenceServerHelper.manager.remove(describeLink);

			} catch (WTException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @throws IOException
	 * @throws WTException
	 */
	private static void edit() throws IOException, WTException {
		Sheet sheetAt = getSheet();
		int lastRowNum = sheetAt.getLastRowNum();
		List<String> attslist = getAttsList(sheetAt);
		List<List> allValuesList = getAllValuesList(sheetAt, lastRowNum, attslist);
		List<Map> allEditObj = getAllEditObj(attslist, allValuesList);
		System.out.println(allEditObj);
		List<Map> editList = getEditList(allEditObj);
		startEdit(editList);
		System.out.println("改完收工");
	}
	/**
	 * 修改料件名称
	 * @param wtpart
	 * @param name
	 */
	public static void setPartNumber(WTDocument doc,String number){
		WTDocumentMaster master = (WTDocumentMaster)doc.getMaster();
		try {
			master = (WTDocumentMaster)PersistenceHelper.manager.refresh(master);
			WTPartMasterIdentity identity = (WTPartMasterIdentity)master.getIdentificationObject();
			identity.setNumber(number);
			master = (WTDocumentMaster)IdentityHelper.service.changeIdentity(master,identity);
			PersistenceHelper.manager.save(master);
		} catch (ObjectNoLongerExistsException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		} catch (WTPropertyVetoException e) {
			e.printStackTrace();
		}
	}	
	@SuppressWarnings("unchecked")
	private static void startEdit(List<Map> editList) throws WTException {
		for (Map map : editList) {
			Set keySet = map.keySet();
			Iterator iterator = keySet.iterator();
			Object next = iterator.next();
			Map<String, Object> attrsMap = (Map<String, Object>) map.get(next);
			if(attrsMap.containsKey("name")) {
				String name = (String) attrsMap.get("name");
				System.out.println("name======="+name);
				System.out.println("next======="+next);
				PIAttributeHelper.service.changeIdentity((RevisionControlled)next,null,name);
				attrsMap.remove("name");				
			}
			if(attrsMap.containsKey("num")) {
				String number = (String) attrsMap.get("num");
				setPartNumber((WTDocument)next,number);
				attrsMap.remove("num");
			}
			//增加修改状态的内容
			RevisionControlled rev=(RevisionControlled)next;
			String state = (String) attrsMap.get("state");
			WTPart part=null;
			if(rev instanceof WTPart)
			{
				part =(WTPart)rev;
			
			if(attrsMap.containsKey("state")) {
				
				//PIAttributeHelper.service.changeIdentity((RevisionControlled)next,null,name);
				//RevisionControlled rev=(RevisionControlled)next;
				String orState=part.getState().toString();
				//System.out.println("state==="+part.getNumber()+"--"+orState);
				if(!orState.endsWith("ARCHIVED")){
				if(!WorkInProgressHelper.isCheckedOut(rev))	{
				  LifeCycleHelper.service.setLifeCycleState((RevisionControlled)next, State.toState(state), false);
				 System.out.println(part.getNumber()+"change ok!");
				attrsMap.remove("state");
				}else{
					System.out.println(part.getNumber()+" has been checkout!");
				}
				}else{
					 System.out.println(part.getNumber()+"--"+orState+"--状态不符合要求 !");
				}
					// System.out.println(part.getNumber()+"--"+orState+"--状态不符合要求 !");
			}
			Persistable persistable = PIAttributeHelper.service.forceUpdateSoftAttributes((Persistable) next,
					attrsMap);
			}else{
				  LifeCycleHelper.service.setLifeCycleState((RevisionControlled)next, State.toState(state), false);			}
			
		}
		
	}

	private static List<Map> getAllEditObj(List<String> attslist, List<List> allValuesList) {
		List<Map> allEditObj = new ArrayList<>();
		if (allValuesList.size() > 0) {
			for (List list : allValuesList) {
				Map<String, Object> map = new HashMap<>();
				for (int i = 0; i < list.size(); i++) {
					map.put(attslist.get(i), list.get(i));
				}
				allEditObj.add(map);
			}
		}
		return allEditObj;
	}

	private static List<List> getAllValuesList(Sheet sheetAt, int lastRowNum, List<String> attslist) {
		List<List> tableValueList = new ArrayList<>();
		int size = attslist.size();
		for (int i = 1; i < lastRowNum + 1; i++) {
			Row row2 = sheetAt.getRow(i);
			List<String> rowValueList = new ArrayList<>();
			for (int j = 0; j < size; j++) {
				String velue = getStringVelue(row2.getCell(j));
				rowValueList.add(velue);
			}
			tableValueList.add(rowValueList);
		}
		return tableValueList;
	}

	private static List<String> getAttsList(Sheet sheetAt) {
		List<String> attlist = new ArrayList<>();
		Row row = sheetAt.getRow(0);
		short s = row.getLastCellNum();
		for (int i = 0; i < s; i++) {
			String velue = getStringVelue(row.getCell(i));
			attlist.add(velue);
		}
		return attlist;
	}

	private static Sheet getSheet() throws IOException {
		WTProperties wtproperties = WTProperties.getLocalProperties();
		String codebasePath = wtproperties.getProperty("wt.codebase.location");
		propertiesPath = codebasePath + sepa + propertiesPath + sepa + propertiesName;
		Workbook workBook = getWorkBook(propertiesPath);
		Sheet sheetAt = workBook.getSheetAt(0);
		return sheetAt;
	}

	/**
	 * 获取最终修改的list
	 * 
	 * @param allEditObj
	 * @return
	 * @throws PIException
	 */
	private static List<Map> getEditList(List<Map> allEditObj) throws PIException {
		List<Map> list = new ArrayList<>();
		if (allEditObj != null && allEditObj.size() > 0) {
			for (Map map2 : allEditObj) {
				Map<Persistable, Map> map = new HashMap<>();
				String type = (String) map2.get("type");
				if ("WTPart".equals(type)) {
					String number = (String) map2.get("number");
				//	WTPart wtPart = PIPartHelper.service.findWTPart(number, "Design");
					System.out.println("number==="+number);
					WTPart wtPart = PartUtil.getLastestWTPartByNumber(number);
					map2.remove("type");
					map2.remove("number");
					map.put(wtPart, map2);
					list.add(map);
				} else if ("WTDocument".equals(type)) {
					String number = (String) map2.get("number");
					WTDocument wtDocument = PIDocumentHelper.service.findWTDocument(number);
					map2.remove("type");
					map2.remove("number");
					map.put(wtDocument, map2);
					list.add(map);
				}
			}
		}
		return list;
	}

	/**
	 * 获取string类型的值
	 * 
	 * @param cell
	 * @return
	 */
	private static String getStringVelue(Cell cell) {
		if (cell != null) {
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			String value = cell.getStringCellValue();
			return value;
		} else {
			return null;
		}
	}

	// 创建workbook对象
	public static Workbook getWorkBook(final String fileName) {
		Workbook workbook = null;
		InputStream is = null;
		try {
			is = new FileInputStream(fileName);
			workbook = WorkbookFactory.create(is);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (EncryptedDocumentException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return workbook;
	}
}
