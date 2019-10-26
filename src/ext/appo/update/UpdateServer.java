package ext.appo.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

import ext.appo.util.DocUtil;
import ext.appo.util.PartUtil;
import ext.pi.core.PIAttributeHelper;
import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.fc.Persistable;
import wt.httpgw.GatewayAuthenticator;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.State;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.util.WTException;
import wt.util.WTProperties;

//ext.holitech.update.UpdateServer
public class UpdateServer implements RemoteAccess {
	private static String sepa = File.separator;
	private static String propertiesPath = "ext" + sepa + "appo" + sepa + "cfg";
	private static String propertiesName = "edit.xlsx";

	public static void main(String[] args) {

		RemoteMethodServer rms = RemoteMethodServer.getDefault();
		GatewayAuthenticator auth = new GatewayAuthenticator();
		auth.setRemoteUser("wcadmin");
		rms.setAuthenticator(auth);
		try {
			edit();
		} catch (IOException | WTException e) {
			e.printStackTrace();
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

	@SuppressWarnings("unchecked")
	private static void startEdit(List<Map> editList) throws WTException {
		for (Map map : editList) {
			Set keySet = map.keySet();
			Iterator iterator = keySet.iterator();
			Object next = iterator.next();
			Map<String, Object> attrsMap = (Map<String, Object>) map.get(next);
			if (attrsMap.containsKey("name")) {
				String name = (String) attrsMap.get("name");
				PIAttributeHelper.service.changeIdentity((RevisionControlled) next, null, name);
				attrsMap.remove("name");
			} // 增加修改状态的内容
			if (attrsMap.containsKey("state")) {
				String state = (String) attrsMap.get("state");
				// PIAttributeHelper.service.changeIdentity((RevisionControlled)next,null,name);
				LifeCycleHelper.service.setLifeCycleState((RevisionControlled) next, State.toState(state), false);
				attrsMap.remove("state");
			}
			System.out.println(next);
			System.out.println(attrsMap);
			Persistable persistable = PIAttributeHelper.service.forceUpdateSoftAttributes((Persistable) next, attrsMap);
			System.out.println(persistable);
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
	 * @throws WTException
	 */
	private static List<Map> getEditList(List<Map> allEditObj) throws WTException {
		List<Map> list = new ArrayList<>();
		if (allEditObj != null && allEditObj.size() > 0) {
			for (Map map2 : allEditObj) {
				Map<Persistable, Map> map = new HashMap<>();
				String type = (String) map2.get("type");
				if ("WTPart".equals(type)) {
					String number = (String) map2.get("number");
					// WTPart wtPart = PIPartHelper.service.findWTPart(number,
					// "Design");
					WTPart wtPart = PartUtil.getLastestWTPartByNumber(number);
					map2.remove("type");
					map2.remove("number");
					map.put(wtPart, map2);
					list.add(map);
				} else if ("WTDocument".equals(type)) {
					String number = (String) map2.get("number");
					System.out.println("number==" + number);
					WTDocument wtDocument = DocUtil.getLatestWTDocument(number);
					System.out.println("wtDocument==" + wtDocument);
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
