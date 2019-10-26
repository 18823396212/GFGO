package ext.appo.bom.change;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import ext.appo.util.PartUtil;
import ext.pi.PIException;
import ext.pi.core.PIDocumentHelper;
import wt.doc.WTDocument;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.collections.WTCollection;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartSubstituteLink;
import wt.util.WTException;
import wt.util.WTProperties;

public class DeleteSublinkUtil {
	private static String sepa = File.separator;
	private static String propertiesPath = "ext" + sepa + "appo" + sepa + "cfg";
	private static String propertiesName = "deletesublink.xlsx";

	public static void main(String[] args) throws IOException, WTException {
		// TODO Auto-generated method stub
		RemoteMethodServer server = RemoteMethodServer.getDefault();
		server.setUserName("wcadmin");
		server.setPassword("wcadmin");
		List<Map> list = new ArrayList<Map>();
		list = getExcelData();
		doexcel(list);

	}

	public static void doexcel(List<Map> datalist) {
		StringBuffer message = new StringBuffer();
		Vector datavector = new Vector();
		if (datalist != null && datalist.size() > 0) {
			for (Map map2 : datalist) {
				Map<Persistable, Map> map = new HashMap<>();
				String oldpartnumber = "";
				String newpartnumber = "";

				oldpartnumber = (String) map2.get("oldpartnumber");
				newpartnumber = (String) map2.get("newpartnumber");

				WTPart newpart = null;
				WTPart oldpart = null;
				oldpart = PartUtil.getLastestWTPartByNumber(oldpartnumber);
				newpart = PartUtil.getLastestWTPartByNumber(newpartnumber);
				try {
					WTCollection collection = WTPartHelper.service.getSubstituteLinksAnyAssembly(newpart.getMaster());
					for (Object object : collection) {
						if (object instanceof ObjectReference) {
							object = ((ObjectReference) object).getObject();
						}
						if (object instanceof WTPartSubstituteLink) {
							WTPartSubstituteLink usage = (WTPartSubstituteLink) object;
							WTPartMaster partmasterObj = (WTPartMaster) usage.getRoleBObject();
							if (partmasterObj.getNumber().startsWith(oldpartnumber)) {
								PersistenceHelper.manager.delete(usage);
								System.out.println("link " + oldpartnumber + "has been delete");
							}

						}
					}
				} catch (WTException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

	public static List<Map> getExcelData() throws IOException, WTException {
		Sheet sheetAt = getSheet();
		int lastRowNum = sheetAt.getLastRowNum();
		List<String> attslist = getAttsList(sheetAt);// 获取表头
		System.out.println(attslist.size());
		List<List> allValuesList = getAllValuesList(sheetAt, lastRowNum, attslist);
		System.out.println(allValuesList.size());
		List<Map> datalist = getAllEditObj(attslist, allValuesList);
		System.out.println(datalist);
		return datalist;
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
					// WTPart wtPart = PIPartHelper.service.findWTPart(number,
					// "Design");
					System.out.println("number===" + number);
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
