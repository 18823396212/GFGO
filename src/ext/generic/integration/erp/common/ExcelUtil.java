/**
 *
 * Copyright (c) 2017-2030 上海湃睿信息科技有限公司 (PISX). All Rights Reserved.
 *
 * This software is the confidential and proprietary information of PISX
 * and is subject to the terms of a software license agreement. You shall
 * not disclose such confidential information and shall use it only in accordance
 * with the terms of the license agreement.
 *
 */
package ext.generic.integration.erp.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import wt.log4j.LogR;
/**
 * 一般工具类
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-07-09
 */ 
public class ExcelUtil {	
	private static final DecimalFormat df = new DecimalFormat("0");
	private static final String CLASSNAME = ExcelUtil.class.getName();
	private static final Logger logger = LogR.getLogger(CLASSNAME);
	
	/**
	 * 获取Excel表格第一个Sheet页
	 * 
	 * @param excel
	 * @return
	 */
	public static Sheet getFirstSheet( String excel ){
		Workbook book = getWorkbook( excel );
		Sheet sheet = getFirstSheet( book ) ;
		
		return sheet ;
	}
	
	/**
	 * 获取Workbook对象第一个Sheet页
	 * 
	 * @param book
	 * @return
	 */
	public static Sheet getFirstSheet( Workbook book ){
		Sheet sheet = null ;
		
		if(book == null){
			return sheet;
		}else{
			sheet = book.getSheetAt( CommonConstant.FIRST_SHEET_INDEX );
		}

		return sheet ;
	}
	
	/**
	 * 获取Excel表格的Workbook对象
	 * 
	 * @param excel
	 * @return
	 */
	public static Workbook getWorkbook(String excel){
		InputStream inStream = null;
		
		Workbook book = null ;
		
		if( excel != null ){
			//路径转换为小写会在Linux和AIX系统下产生BUG
			//String fileName = excel.toLowerCase().trim();
			String fileName = excel.trim();
			
			try {
				inStream = new FileInputStream(fileName);
				
				//根据文件后缀名，生成对应的Workbook对象
				if( fileName.endsWith( CommonConstant.XLS ) ){
					book = new HSSFWorkbook( inStream );
				}else if( fileName.endsWith( CommonConstant.XLSX ) ){
					book = new XSSFWorkbook( inStream );
				}else{
					logger.error("不支持的Excel文件后缀名：" + excel ) ;
				}
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				FileUtil.closeStream(inStream);
			}			
		}else{
			logger.error("Excel文件路径不正确：" + excel ) ;
		}
					
		return book ;
		
	}
	
	/**
	 * 根据Row对象和Cell的Index获取单元格的值
	 * 
	 * @param row
	 * @param index
	 * @param getNumbericAsString
	 * @return
	 */
	public static String getCellValueByIndex( Row row , int index , boolean getNumbericAsString ){
		String value = "" ;
		
		if( row == null ){
			return value ;
		}else{
			Cell valueCell = row.getCell( index ) ;
			value = getCellValueAsString( valueCell , getNumbericAsString);
			
			value =  convertNullString(value) ;
		}
		
		return value ;
	}
	
	/**
	 * 获取单元格的值
	 * 
	 * @param cell
	 * @return
	 */
	public static String getCellValueAsString(Cell cell){
		String cellStringValue = "" ;
		
		if(cell == null){
			return cellStringValue ;
		}
		
		int cellType = cell.getCellType();
		
		switch (cellType) {
			case Cell.CELL_TYPE_BOOLEAN :
			{	
				cellStringValue = cell.getBooleanCellValue() + "" ;
				break;
			}
			case Cell.CELL_TYPE_ERROR:
			{
				cellStringValue = cell.getErrorCellValue() + "";
				break;
			}
			case Cell.CELL_TYPE_FORMULA:
			{
				cellStringValue = parseFormula(cell.getCellFormula());
				break;
			}
			case Cell.CELL_TYPE_NUMERIC:
			{	
				cellStringValue = cell.getNumericCellValue() + "";
				break;
			}
			case Cell.CELL_TYPE_STRING:
			{
				cellStringValue = cell.getStringCellValue();
				break;
			}
		}
		
		return cellStringValue ;
	}
	
	/**
	 *  获取单元格的值
	 * 
	 * @param cell
	 * @param getNumbericAsString
	 * @return
	 */
	public static String getCellValueAsString(Cell cell , boolean getNumbericAsString){
		String cellStringValue = "" ;
		
		
		
		if(cell == null){
			return cellStringValue ;
		}
		
		int cellType = cell.getCellType();
		
		switch (cellType) {
			case Cell.CELL_TYPE_BOOLEAN :
			{	
				cellStringValue = cell.getBooleanCellValue() + "" ;
				break;
			}
			case Cell.CELL_TYPE_ERROR:
			{
				cellStringValue = cell.getErrorCellValue() + "";
				break;
			}
			case Cell.CELL_TYPE_FORMULA:
			{
				cellStringValue = parseFormula(cell.getCellFormula());
				break;
			}
			case Cell.CELL_TYPE_NUMERIC:
			{	if( getNumbericAsString ){
					cellStringValue = df.format(cell.getNumericCellValue());
				}else{
					cellStringValue = cell.getNumericCellValue() + "";
				}
				break;
			}
			case Cell.CELL_TYPE_STRING:
			{
				cellStringValue = cell.getStringCellValue();
				break;
			}
		}
		
		return cellStringValue ;
	}
	
	/**
	 * NULL值处理
	 * 
	 * @param value
	 * @return
	 */
	public static String convertNullString(String value) {
		if( value == null ){
			value = "" ;
		}else{
			value = value.trim() ;
		}
		return value;
	}
	
	private static String parseFormula(String pPOIFormula) {
		final String cstReplaceString = "ATTR(semiVolatile)";
		StringBuffer result = null;
		int index;

		result = new StringBuffer();
		index = pPOIFormula.indexOf(cstReplaceString);
		if (index >= 0) {
			result.append(pPOIFormula.substring(0, index));
			result.append(pPOIFormula.substring(index + cstReplaceString.length()));
		} else {
			result.append(pPOIFormula);
		}

		return result.toString();
	}
}
