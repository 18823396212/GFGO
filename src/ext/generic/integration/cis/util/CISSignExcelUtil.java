package ext.generic.integration.cis.util;  

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import wt.log4j.LogR;
import wt.util.WTProperties;
  
public class CISSignExcelUtil {
	public static WTProperties wtProperties;
	private static String location = "";
	public static String configFilePath;
	public static HashMap<String,String> tablemap = null;
	public static HashMap<String,ArrayList<HashMap<String,String>>> configInfo = null;
	
	private static Logger logger = LogR.getLogger(CISSignExcelUtil.class.getName());
    static{
        try{
            wtProperties = WTProperties.getLocalProperties();
            location = wtProperties.getProperty("wt.codebase.location", "");//Windchill codebase路径
            configFilePath = location + File.separator + "ext" + File.separator + "generic"
            		+ File.separator+ "integration" + File.separator+ "cis" + File.separator + "config" + File.separator+"CISSingleTemplate.xlsx";
        }catch (java.io.IOException ioe){
            ioe.printStackTrace();
        }
    }

    /**
     * 获取数据库的名称即sheetName
     * @return
     */
    public static String getSheetName(){
    	String name="";
    	FileInputStream in = null;
		XSSFWorkbook wb = null;
		try {
			in = new FileInputStream(configFilePath);
			wb = new XSSFWorkbook(in);
			XSSFSheet sheet = wb.getSheetAt(0);
			name=sheet.getSheetName().trim().toString();
			logger.debug(" >>>>>>>.. sheet :"+sheet.getSheetName().trim().toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return name;
			
    }
	
	/**
	 * 读取配置
	 * key 存放 PLM内部名称，value存放数据库字段
	 * @param excelPath  
	 * @Description:
	 */
	public static HashMap<String,String> readCISConfigInfo(){
		HashMap<String,String> map=new HashMap<String,String>();
		FileInputStream in = null;
		XSSFWorkbook wb = null;
		try {
			in = new FileInputStream(configFilePath);
			wb = new XSSFWorkbook(in);
			XSSFSheet sheet = wb.getSheetAt(0);
			int count=sheet.getLastRowNum();
			logger.debug(" >>>>>.count :"+count);
			for(int i=1;i<=count;i++){
				XSSFRow row = sheet.getRow(i);
				XSSFCell cell0 = row.getCell(0);
				XSSFCell cell1 = row.getCell(1);
				String wcName=cell0.getStringCellValue();
				String dataName=cell1.getStringCellValue();
				logger.debug(" >>>>>>>>plm :"+wcName+"   data :"+dataName);
				if(wcName!=null && !wcName.equals(""))
					map.put(wcName, dataName);
			}
			
		}catch (Exception e) {
			if(in!=null){
				try {
					in.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return map;
	}
	
	
	public static void main(String[] args) {
		logger.debug(" >>>>>>>>>>>>>>>>>>>Start ");
		configFilePath = "E:\\CISSingleTemplate.xlsx";
		String name=getSheetName();
		
		readCISConfigInfo();
		
		logger.debug(" >>>>>>>>>>>>>>end ");
	}
	
	
}
