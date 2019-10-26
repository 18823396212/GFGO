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
package ext.generic.integration.erp.util ;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException ;
import java.io.File ;
import java.io.Reader;
import java.io.FileNotFoundException ;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import wt.log4j.LogR;

import ext.generic.integration.erp.common.FileUtil;
/**
 * Info*E Task XML文件解析工具类
 * 
 * 主要解析PDM和中间表之间，属性和列的对应关系。
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-10-08
 */
public class IEXMLUtil implements IEXMLInterface {
	
	private Map<String,String> map = new HashMap<String,String>() ;
	
	private static final String CLASSNAME = IEXMLUtil.class.getName() ;

	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	public IEXMLUtil() {
		initIEXMLConstant();
	}

	public Map<String,String> parserXml(String filename){
		
//		logger.debug("Parse File : filename = " + filename);
		
		File file = new File(filename) ;
		
		if(file.exists() && file.isFile()){
			Reader reader = null ;
			BufferedReader bufferedReader = null ;
			
			StringBuffer logBuffer = new StringBuffer("\n");
			
			try {
				reader = new FileReader( file );
				
				bufferedReader = new BufferedReader( reader ) ;
				String tempStr = "" ;
				
				while( ( tempStr = bufferedReader.readLine()) != null ){						
//					if(verbose){
//						logBuffer.append("-----------------------------------------\n");
//						logBuffer.append(tempStr);
//						logBuffer.append("\n");
//					}
					
					//支持单行注释
					if(tempStr.startsWith( IntegrationConstant.ANNOTATION_STR )){
//						if(verbose){
//							logBuffer.append("Contains ");
//							logBuffer.append(IntegrationConstant.ANNOTATION_STR);
//							logBuffer.append("\n");
//						}

						continue ;
					}
					//获取属性映射的行，进行解析
					else if( tempStr.contains( IntegrationConstant.FIELD_NAME_STR ) ){	
						String key = getStrKey( tempStr );
						String value = getStrValue( tempStr ) ;
						
//						if(verbose){
//							logBuffer.append("Contains ");
//							logBuffer.append(IntegrationConstant.FIELD_NAME_STR);
//							logBuffer.append("\n");
//							logBuffer.append(key) ;
//							logBuffer.append(" --> ") ;
//							logBuffer.append(value) ;
//							logBuffer.append("\n") ;
//						}

						this.map.put( key , value );
					}else{
//						if(verbose){
//							logBuffer.append("Doesn't contain ");
//							logBuffer.append(IntegrationConstant.FIELD_NAME_STR);
//							logBuffer.append("\n");
//						}
					}
				}
				
			} catch (FileNotFoundException e) {
				logger.equals("Throw FileNotFoundException " + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				logger.equals("Throw IOException " + e.getMessage());
				e.printStackTrace();
			}finally{
//				if(verbose){
//					PDMIntegrationLogUtil.printLog(clazz, logBuffer.toString() , IntegrationConstant.LOG_LEVEL_NORMAL );
//					PDMIntegrationLogUtil.printLog(clazz, getMapKeyValue( filename, map) , IntegrationConstant.LOG_LEVEL_NORMAL);
//				}
//				logger.debug("logBuffer is:" + logBuffer.toString());
				
				FileUtil.closeStream(reader);
				FileUtil.closeStream(bufferedReader);
				
			}
		}else{
			if( ! file.exists() ){
				logger.debug("<<<<< parserXml(String filename) file:"+ filename + " is not existed ......");
//				PDMIntegrationLogUtil.printLog(clazz,"<<<<< parserXml(String filename) file:"+ filename + " is not existed ......" , IntegrationConstant.LOG_LEVEL_ERROR) ;
			}else if( ! file.isFile()){
				logger.debug("<<<<< parserXml(String filename) file:"+ filename + " is not a file ......");
//				PDMIntegrationLogUtil.printLog(clazz,"<<<<< parserXml(String filename) file:"+ filename + " is not a file ......" , IntegrationConstant.LOG_LEVEL_ERROR) ;
			}else{
				logger.debug("<<<<< parserXml(String filename) file:"+ filename + " unknow file error ......");
//				PDMIntegrationLogUtil.printLog(clazz,"<<<<< parserXml(String filename) file:"+ filename + " unknow file error ......" , IntegrationConstant.LOG_LEVEL_ERROR) ;
			}
		}
		
		return map ;
	}
	
	private String getStrKey(String str){
		String key = "" ;
		
		int beginIndex = str.indexOf( IntegrationConstant.KEY_START_STR ) + IntegrationConstant.keyStartStrLen ;
		
		int endIndex = str.indexOf( IntegrationConstant.KEY_END_STR_1 );
		
		if( endIndex < 0 ){
			endIndex = str.indexOf( IntegrationConstant.KEY_END_STR_2 );
		}
		
		key = str.substring(beginIndex, endIndex) ;
		
		return key ;
	}
	
	private String getStrValue(String str){
		String value = "" ;
		
		int beginIndex = str.indexOf( IntegrationConstant.VALUE_START_STR ) + IntegrationConstant.valueStartStrLen ;
		
		int endIndex = str.indexOf( IntegrationConstant.VALUE_END_STR );
		
		value = str.substring(beginIndex, endIndex) ;
		
		return value ;
	}
	
	private void initIEXMLConstant() {
//		if( verbose ){
//			StringBuffer strBuffer = new StringBuffer("\n");
//			
//			strBuffer.append("FIELD_NAME_STR => " + IntegrationConstant.FIELD_NAME_STR + "\n");
//			strBuffer.append("KEY_START_STR => " + IntegrationConstant.KEY_START_STR + "\n");
//			strBuffer.append("KEY_END_STR_1 => " + IntegrationConstant.KEY_END_STR_1 + "\n");
//			strBuffer.append("KEY_END_STR_2 => " + IntegrationConstant.KEY_END_STR_2 + "\n");
//			strBuffer.append("VALUE_START_STR => " + IntegrationConstant.VALUE_START_STR + "\n");
//			strBuffer.append("VALUE_END_STR =>" + IntegrationConstant.VALUE_END_STR + "\n");
//			strBuffer.append("keyStartStrLen=" + IntegrationConstant.keyStartStrLen + "\n");
//			strBuffer.append("valueStartStrLen=" + IntegrationConstant.valueStartStrLen + "\n");
//			
//			PDMIntegrationLogUtil.printLog( clazz , strBuffer.toString(), IntegrationConstant.LOG_LEVEL_NORMAL) ;
//		}
	}
	
	private String getMapKeyValue( String file , Map<String,String> map ){
		StringBuffer strBuffer = new StringBuffer("\n------- Print Map --------\n") ;
		
		if(map == null){
			strBuffer.append("IEXMLUtil's map of file ") ;
			strBuffer.append(file);
			strBuffer.append(" is null\n");
		}else{
			Iterator<String> ite = map.keySet().iterator() ;
			while(ite.hasNext()){
				String key = ite.next() ;
				String value = map.get(key) ;
				
				strBuffer.append("Key : ") ;
				strBuffer.append(key);
				strBuffer.append(" ===> Value : ");
				strBuffer.append(value);
				strBuffer.append("\n");
			}
		}
		
		strBuffer.append("----- End Print Map -----\n");
		
		return strBuffer.toString() ;
	}
	
	public static void testCase(String fileName){
		System.out.println("Start IEXMLUtil testCase()......") ;
		
		IEXMLUtil ieXMLUtil = new IEXMLUtil() ;
		
		if(fileName == null || fileName.trim().equals("")){
			ieXMLUtil.parserXml("D:\\tmp\\BomStructureCreate.xml");
		}else{
			ieXMLUtil.parserXml( fileName );
		}
		
		
		System.out.println("End IEXMLUtil testCase()......") ;
	}
	
	public static void main(String[] args) {
		System.out.println("Enter IEXMLUtil main()......  ") ;
		if(args != null && args.length > 0){
			testCase(args[0]) ;
		}else{
			testCase(null) ;
		}
	}
}
