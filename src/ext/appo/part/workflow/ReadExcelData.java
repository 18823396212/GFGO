package ext.appo.part.workflow;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import wt.util.WTException;
import ext.appo.part.beans.ProductLineConfigBean;
import ext.appo.part.util.ProductLineConfigExcelReader;
import ext.lang.PIExcelUtils;
import ext.pi.PIException;
import ext.pi.core.PIClassificationHelper;
import ext.pi.core.PICoreHelper;

public class ReadExcelData {

	private static String codebalocation;
	private static String filePath;
	static {
		try {
			codebalocation = PICoreHelper.service.getCodebase();
			filePath = codebalocation + File.separator + "ext" + File.separator + "appo" + File.separator + "part"
					+ File.separator + "ProductNamingNoticeConfig.xlsx";
		} catch (PIException e) {
			e.printStackTrace();
		}
	}
	
	public static List<ProductLineConfigBean> getProductLineConfig() throws WTException{
		ProductLineConfigExcelReader reader = new ProductLineConfigExcelReader(filePath) ;
		Workbook workbook =reader.getWorkbook() ;
		reader.read(workbook.getSheetAt(1));
		return reader.getBeanList() ;
	}

	public static List<String> readExcel(String productLine) throws WTException {

		// Excel属性集合
		List<String> attributeList = new ArrayList<String>();
		if (productLine != null && !productLine.equals("")) {
			String nodeLocalizedHierarchy = PIClassificationHelper.service.getNodeLocalizedName(productLine,
					Locale.CHINA);
			Workbook workbook = PIExcelUtils.getWorkbook(new File(filePath));
			Sheet sheetAt = workbook.getSheetAt(0);
			int lastRowNum = sheetAt.getLastRowNum();
			for (int i = 1; i < lastRowNum; i++) {
				Row row = sheetAt.getRow(i);
				Cell cell = row.getCell(0);
				String line = cell.getStringCellValue();
				if (line.equals(nodeLocalizedHierarchy)) {
					short lastCellNum = row.getLastCellNum();
					for (int j = 3; j < lastCellNum; j++) {
						Cell cell2 = row.getCell(j);
						String attribute = cell2.getStringCellValue();
						if (!attribute.equals("")) {
							attributeList.add(attribute);
						}
					}
					break;
				}
			}
		}
		return attributeList;

	}

	public static String readExcelReturn(String productLine) throws WTException {

		if (productLine != null && !productLine.equals("")) {

			String nodeLocalizedHierarchy = PIClassificationHelper.service.getNodeLocalizedName(productLine,
					Locale.CHINA);

			// Excel所属产品线
			List<String> rowList = new ArrayList<String>();

			Collection<String> childrenNodes = PIClassificationHelper.service.getChildrenNodeNames("appo_cp");
			if (!childrenNodes.contains(productLine)) {
				return "选择的所属产品线不是“成品”分类子件！";
			}

			Workbook workbook = PIExcelUtils.getWorkbook(new File(filePath));
			Sheet sheetAt = workbook.getSheetAt(0);
			int lastRowNum = sheetAt.getLastRowNum();
			for (int i = 1; i < lastRowNum; i++) {
				Row row = sheetAt.getRow(i);
				Cell cell = row.getCell(0);
				String line = cell.getStringCellValue();
				rowList.add(line);
			}

			if (!rowList.contains(nodeLocalizedHierarchy)) {
				return "选择的所属产品线不存在配置表中！";
			}
			
			List<String> list = new ArrayList<>();
			Collection<String> ibaAttributeNames = PIClassificationHelper.service.getAttributeNames(productLine);
			Map<String, String> attributeDisplayNames = PIClassificationHelper.service
					.getAttributeDisplayNames(productLine, ibaAttributeNames, Locale.CHINA);

			List<String> readExcel = readExcel(productLine);
			for (int i = 0; i < readExcel.size(); i++) {
				String value = readExcel.get(i);

				Iterator it = attributeDisplayNames.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry entry = (Map.Entry) it.next();
					String key = (String) entry.getKey();
					String display = (String) entry.getValue();
					if (display.equals(value)) {
						list.add(key);
					}
				}
			}
			list.add("name");
			list.add("nbxh");
			String attribute = list.toString();
		    attribute = attribute.substring(1, attribute.length()-1);
			return attribute;
		}
		return "";

	}

}
