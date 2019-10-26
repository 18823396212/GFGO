package ext.appo.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
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

import ext.appo.part.workflow.AppoContainerTeamExecutor;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.inf.container.WTContainer;
import wt.inf.team.ContainerTeamManaged;
import wt.org.OrganizationServicesHelper;
import wt.org.WTGroup;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.util.WTProperties;

public class UpdateContainerRole {
	private static String sepa = File.separator;
	private static String propertiesPath = "ext" + sepa + "appo" + sepa + "cfg";
	private static String propertiesName = "addrole.xlsx";

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		setrole();
	}

	public static void setrole() throws Exception {
		List<Map> list = new ArrayList<Map>();
		list = getExcelData();
		System.out.println("partDesignvector size====" + list.size());
		setcontainerRole(list);

	}

	public static void setcontainerRole(List<Map> list) throws WTException {
		for (Map map2 : list) {
			String containername = "";
			String role = "";
			String name = "";
			String operation = "";
			String type = "";

			containername = (String) map2.get("库名称");
			role = (String) map2.get("角色");
			name = (String) map2.get("账号");
			operation = (String) map2.get("操作");
			type = (String) map2.get("账号类型");
			WTContainer container = getContainer(containername);
			Vector<WTPrincipal> principalVector = new Vector();
			if (operation == null || operation.length() == 0) {

			} else {
				if (type.startsWith("user")) {
					WTPrincipal wtprincipal = getUserFromName(name);
					principalVector.addElement(wtprincipal);
					AppoContainerTeamExecutor teamExecutor = new AppoContainerTeamExecutor(
							(ContainerTeamManaged) container, role, principalVector, operation);
					teamExecutor.doExecute();
				}
				if (type.startsWith("group")) {

					WTGroup group = OrganizationServicesHelper.manager.getGroup(name);
					System.out.println("group name===" + group.getName());
					principalVector.addElement(group);

					AppoContainerTeamExecutor teamExecutor = new AppoContainerTeamExecutor(
							(ContainerTeamManaged) container, role, principalVector, operation);
					teamExecutor.doExecute();
				}
			}
		}
	}

	public static WTUser getUserFromName(String name) throws WTException {
		WTUser user = null;
		if ("".equals(name) || null == name) {
			throw new WTException("用户名不能为null");
		}
		Enumeration enumUser = OrganizationServicesHelper.manager.findUser(WTUser.NAME, name);
		if (enumUser == null) {
			enumUser = (Enumeration) OrganizationServicesHelper.manager.getGroup(name);
			System.out.println("enumuser==" + enumUser);
		}

		if (enumUser.hasMoreElements())
			user = (WTUser) enumUser.nextElement();
		if (user == null) {
			enumUser = OrganizationServicesHelper.manager.findUser(WTUser.FULL_NAME, name);
			if (enumUser.hasMoreElements())
				user = (WTUser) enumUser.nextElement();
		}
		if (user == null) {
			System.out.println("系统中不存在用户名为'" + name + "'的用户！");
		}
		return user;
	}

	public static WTContainer getContainer(String containerName) {

		try {
			QuerySpec qs = new QuerySpec(WTContainer.class);
			SearchCondition sc = new SearchCondition(WTContainer.class, WTContainer.NAME, "=", containerName);
			qs.appendWhere(sc);
			QueryResult qr = PersistenceHelper.manager.find(qs);
			while (qr.hasMoreElements()) {
				WTContainer container = (WTContainer) qr.nextElement();
				return container;
			}
		} catch (QueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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

	private static Sheet getSheet() throws IOException {
		WTProperties wtproperties = WTProperties.getLocalProperties();
		String codebasePath = wtproperties.getProperty("wt.codebase.location");
		propertiesPath = codebasePath + sepa + propertiesPath + sepa + propertiesName;
		Workbook workBook = getWorkBook(propertiesPath);
		Sheet sheetAt = workBook.getSheetAt(0);
		return sheetAt;
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
