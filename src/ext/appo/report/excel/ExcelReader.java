package ext.appo.report.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import wt.util.WTException;
import wt.util.WTProperties;

public class ExcelReader {

	public String filename = "";

	public ExcelReader(String filename) {
		this.filename = filename;
	}

	/**
	 * @throws IOException
	 * @throws WTException
	 */
	public List<Map> getExcelData() throws IOException, WTException {
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

	public static List<Map> getAllEditObj(List<String> attslist, List<List> allValuesList) {
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

	public static List<List> getAllValuesList(Sheet sheetAt, int lastRowNum, List<String> attslist) {
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

	public static List<String> getAttsList(Sheet sheetAt) {
		List<String> attlist = new ArrayList<>();
		Row row = sheetAt.getRow(0);
		short s = row.getLastCellNum();
		for (int i = 0; i < s; i++) {
			String velue = getStringVelue(row.getCell(i));
			attlist.add(velue);
		}
		return attlist;
	}

	public Sheet getSheet() throws IOException {
		WTProperties wtproperties = WTProperties.getLocalProperties();
		String codebasePath = wtproperties.getProperty("wt.codebase.location");
		String path = codebasePath + File.separator + "ext/appo/cfg/" + this.filename;
		Workbook workBook = getWorkBook(path);
		Sheet sheetAt = workBook.getSheetAt(0);
		return sheetAt;
	}

	/**
	 * 获取string类型的值
	 * 
	 * @param cell
	 * @return
	 */
	public static String getStringVelue(Cell cell) {
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
