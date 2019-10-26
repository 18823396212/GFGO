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
package ext.generic.integration.erp.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import ext.generic.integration.erp.common.CommonUtil;
import ext.generic.integration.erp.common.ExcelUtil;
import wt.log4j.LogR;
/**
 * PDM和ERP单位映射关系工具类
 * 
 */
public class UnitGroupMappingUtil {
	private static final String clazz = UnitGroupMappingUtil.class.getName() ;
	private static final Logger logger = LogR.getLogger(clazz);
	private static Map<String,String> unitGroupMap = null ;

	private static final String UNIT_EXCEL_FILE = "UnitMapping.xls" ;
	
	private static final int PDM_UNIT_COL_INDEX = 0 ;
	private static final int ERP_UNIT_COL_INDEX = 2 ;
	
	static{
		//配置文件与ext.generic.integration.erp.config.UnitMappingUtil.class文件在同一路径下
		String filePath = UnitGroupMappingUtil.class.getResource("").getPath() ;
		
		//BUG Fix，解决Linux下文件路径中windchill小写不能找到文件的BUG
		filePath = filePath.replaceAll("windchill", "Windchill") ;
		
		logger.debug("Unit Mapping File Path : " + filePath);
		unitGroupMap = parseExcel( filePath + UNIT_EXCEL_FILE ) ;
	}
	
	/**
	 * 获取PDM单位对应的ERP单位
	 * 
	 * @param pdmUnit
	 * @return
	 */
	public static String getERPUnitGroup( String pdmUnit){
		String erpUnit = "" ;
		
		if(pdmUnit == null){
			pdmUnit = "" ;
		}
		
		pdmUnit = pdmUnit.trim();
		
		erpUnit = unitGroupMap.get(pdmUnit) ;
		
		return erpUnit ;
	}
	
	/**
	 * 获取整个单位映射表
	 * 
	 * @return
	 */
	public static Map<String, String> getUnitGroupMap() {
		return unitGroupMap;
	}

	/**
	 * 通过解析Excel文件，构造单位映射表
	 * 
	 * @param file
	 * @return
	 */
	private static Map<String,String> parseExcel(String file){
		Map<String,String> map = new HashMap<String,String>() ;
		
		Sheet sheet = ExcelUtil.getFirstSheet(file);
		
		if(sheet != null){
			Iterator<Row> ite = sheet.iterator() ;
			
			//跳过表头
			if( ite.hasNext() ){
				ite.next() ;
			}
			
			//遍历，获取单位映射关系
			while( ite.hasNext() ){
				Row row = ite.next() ;
				
				if( row != null ){
					String pdmUnit = ExcelUtil.getCellValueByIndex(row, PDM_UNIT_COL_INDEX, false);
					String erpUnit = ExcelUtil.getCellValueByIndex(row, ERP_UNIT_COL_INDEX, true);  //Modify By ZhouLihua 20160822  
					
					pdmUnit = CommonUtil.nullStringConvert( pdmUnit );
					erpUnit = CommonUtil.nullStringConvert( erpUnit );
					
					if( ( ! pdmUnit.equals("") ) && ( !erpUnit.equals("") ) ){	
						map.put( pdmUnit , erpUnit ) ;
					}
				}
			}
		}
		
		return map ;
	}
	
	public static void main(String[] args) {
		CommonUtil.printMap(unitGroupMap) ;
	}
}
