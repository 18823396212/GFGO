package ext.generic.integration.cis.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import wt.log4j.LogR;
import wt.util.WTProperties;
import ext.generic.integration.cis.beans.CisConfigExcelBean;
import ext.lang.PIStringUtils;

public class CISConfigExcelUtil {
	public static WTProperties wtProperties;
	private static String location = "";
	public static String configFilePath;
	public static HashMap<String, String> tablemap = null;
	public static HashMap<String, ArrayList<HashMap<String, String>>> configInfo = null;
	private static final Logger logger = LogR.getLogger(CISConfigExcelUtil.class.getName());
	static {
		try {
			wtProperties = WTProperties.getLocalProperties();
			location = wtProperties.getProperty("wt.codebase.location", "");// Windchill
																			// codebase路径
			configFilePath = location + File.separator + "ext" + File.separator + "generic" + File.separator + "integration" + File.separator + "cis"
					+ File.separator + "config" + File.separator + "CISConfigTemplate.xlsx";
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * 重新加载配置数据
	 * 
	 * @Description:
	 */
	public static void reload() {
		readCISConfigInfo(configFilePath);
	}

	/**
	 * 获取配置信息
	 * 
	 * @param excelPath
	 * @return
	 * @Description:
	 */
	public static HashMap<String, ArrayList<HashMap<String, String>>> getCISConfigInfo() {
		if (configInfo == null) {
			readCISConfigInfo(configFilePath);
		}
		return configInfo;
	}
	
	/**
	 * 获取配置信息
	 * 
	 * @param excelPath
	 * @return
	 * @Description:
	 */
	public static HashMap<String, ArrayList<HashMap<String, String>>> getCisConfigTemplate() {
		readCisConfigTemplateInfo(configFilePath);
		
		return configInfo;
	}
	
	/***
	 * 读取CIS后台配置
	 * 
	 * @param excelPath
	 */
	public static void readCisConfigTemplateInfo(String excelPath){
		configInfo = new HashMap<String, ArrayList<HashMap<String, String>>>();
		tablemap = new HashMap<String, String>();
		if(PIStringUtils.isNull(excelPath)){
			return ;
		}
		
		try {
			CisConfigTemplateExcelReader reader = new CisConfigTemplateExcelReader(excelPath) ;
			reader.read(); 
			List<CisConfigExcelBean> beanList = reader.getBeanList() ;
			if(logger.isDebugEnabled()){
				logger.debug("beanList : " + beanList);
			}
			
			ArrayList<HashMap<String, String>> temp = new ArrayList<HashMap<String,String>>() ;
			for (int i = 1; i < beanList.size(); i++) {
				CisConfigExcelBean excelBean = beanList.get(i) ;
				// 获取分类名称
				String nodeNumber = excelBean.getNodeNumber() ;
				if(PIStringUtils.isNotNull(nodeNumber)){
					temp = new ArrayList<HashMap<String,String>>() ;
					configInfo.put(nodeNumber, temp);
					// 分类对应CIS数据库关系
					tablemap.put(nodeNumber, excelBean.getTableName()) ;
				}else{
					HashMap<String, String> arrmap = new HashMap<String, String>();
					arrmap.put("pdmName", excelBean.getPlmAttributeName());
					arrmap.put("cisName", excelBean.getCisAttributeName());
					arrmap.put("pdmType", excelBean.getAttributeType());
					temp.add(arrmap);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * 读取配置
	 * 
	 * @param excelPath
	 * @Description:
	 */
	public static void readCISConfigInfo(String excelPath) {
		configInfo = new HashMap<String, ArrayList<HashMap<String, String>>>();
		tablemap = new HashMap<String, String>();
		FileInputStream in = null;
		XSSFWorkbook wb = null;
		logger.debug(" >>>excelPath :" + excelPath);
		try {
			in = new FileInputStream(excelPath);
			wb = new XSSFWorkbook(in);
			XSSFSheet sheet = wb.getSheetAt(0);
			Iterator<Row> rowit = sheet.rowIterator();
			XSSFRow rowTemp = null;
			XSSFCell cellTemp = null;
			logger.debug(" >>>>rowit.hasNext :" + rowit.hasNext());
			// 去表头
			if (rowit.hasNext()) {
				rowit.next();
			}

			ArrayList<HashMap<String, String>> temp = null;
			while (rowit.hasNext()) {
				rowTemp = (XSSFRow) rowit.next();
				System.out.print(">>> cisconf rowTemp:" + rowTemp.getRowNum() + "\n");
				cellTemp = rowTemp.getCell(0);
				System.out.print(">>> cisconf cellTemp:" + cellTemp + "\n");
				if (cellTemp != null) {
					String classInternalName = cellTemp.getStringCellValue();
					if(classInternalName.equals("E0400000")){
						System.out.println(classInternalName);
					}
					if (classInternalName != null && classInternalName.trim().length() > 0) {
						temp = new ArrayList<HashMap<String, String>>();
						configInfo.put(classInternalName, temp);
					}
					String tableName = rowTemp.getCell(4).getStringCellValue();
					tablemap.put(classInternalName, tableName);

				} else {
					cellTemp = rowTemp.getCell(1);
					String pdmName = cellTemp.getStringCellValue();
					if (pdmName != null && pdmName.trim().length() > 0) {
						HashMap<String, String> arrmap = new HashMap<String, String>();
						temp.add(arrmap);
						arrmap.put("pdmName", pdmName);

						cellTemp = rowTemp.getCell(2);
						String cisName = cellTemp.getStringCellValue();
						arrmap.put("cisName", cisName);

						cellTemp = rowTemp.getCell(3);
						String pdmType = cellTemp.getStringCellValue();
						arrmap.put("pdmType", pdmType);
					}
				}
			}
		} catch (Exception e) {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public static ArrayList<HashMap<String, String>> getAttConfig(String classInternalName) {
		HashMap<String, ArrayList<HashMap<String, String>>> cisconf = getCisConfigTemplate();
		ArrayList<HashMap<String, String>> result = cisconf.get(classInternalName);
		return result;
	}

	public static void main(String[] args) {
		logger.debug(" >>>>>>>>>>>>>>>>>>>Start ");
		configFilePath = "E:\\CISConfigTemplate.xlsx";
		reload();
		logger.debug(" >>>>>>>>>>>>>>end ");
	}

	public static String getTableName(String classInternalName) {
		return tablemap.get(classInternalName);
	}

}
