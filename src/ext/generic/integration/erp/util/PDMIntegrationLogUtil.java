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
package ext.generic.integration.erp.util;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.generic.integration.erp.config.ERPPropertiesUtil;

import wt.fc.ObjectReference;
import wt.fc.collections.WTKeyedHashMap;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.part.WTPart;
import wt.part.WTPartUsageLink;
import wt.util.WTException;
import wt.vc.config.ConfigException;
/**
 * 集成日志输出工具类
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-10-08
 */
public class PDMIntegrationLogUtil implements RemoteAccess, Serializable {

	private static final long serialVersionUID = 1849548443252074612L;
	
	private static final String CLASSNAME = PDMIntegrationLogUtil.class.getName() ;

	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	public static boolean verbose = true ;
	private static int log_level = 0 ;
	
	static {
		try {
//			verbose = (WTProperties.getLocalProperties()).getProperty( PDMIntegrationLogConstant.VERBOSE , false);			
		} catch (Throwable throwable) {
			printLog( CLASSNAME , throwable.getMessage() ,IntegrationConstant.LOG_LEVEL_ERROR ) ;
			throw new ExceptionInInitializerError(throwable);
		}
		
		try{
			String log_level_str = (String)ERPPropertiesUtil.getInstance().readProperties().get("log_level");
			log_level = Integer.parseInt(log_level_str);
		}catch(Exception e){
			log_level = 0 ;
			printLog( CLASSNAME , e.getMessage(),IntegrationConstant.LOG_LEVEL_ERROR ) ;
		}finally{
			logger.info("Current Log Level : " + log_level );
		}
	}
	
	/**
	 * 
	 * 一般情况下，日志等级为INFO，即为1级
	 * 
	 * @param clazz
	 * @param logInfo
	 */
	public static void printLog( Class<?> clazz , String logInfo ){
		printLog( clazz , logInfo , IntegrationConstant.LOG_LEVEL_INFO ) ;
	}
	
	/**
	 * 一般情况下，日志等级为INFO，即为1级
	 * 
	 * @param clazz
	 * @param logInfo
	 */
	public static void printLog( String clazz , String logInfo ){
		printLog( clazz , logInfo , IntegrationConstant.LOG_LEVEL_INFO ) ;
	}
	
	/**
	 * 
	 * @param clazz
	 * @param logInfo
	 * @param level
	 */
	public static void printLog( Class<?> clazz, String logInfo , int level ){
		printLog( clazz , logInfo , level , verbose) ;
	}
	
	/**
	 * 
	 * @param clazz
	 * @param logInfo
	 * @param level
	 */
	public static void printLog( String clazz , String logInfo , int level){
		printLog( clazz , logInfo , level , verbose) ;
	}
	
	/**
	 * 
	 * @param clazz
	 * @param logInfo
	 * @param level
	 * @param verbose
	 */
	public static void printLog(Class<?> clazz , String logInfo , int level , boolean verbose){
		if( clazz != null ){
			printLog( clazz.getName() , logInfo , level , verbose) ;
		}
	}
	
	/**
	 * 当日志开关verbose=true时，
	 * 只输出日志等级大于和等于当前设定等级log_level的日志
	 * 
	 * @param clazz
	 * @param logInfo
	 * @param level
	 * @param verbose
	 */
	public static void printLog( String clazz , String logInfo , int level , boolean verbose){
		
		if(level >= log_level){
			
			if( verbose ){
				if(logInfo == null || logInfo.trim().equals("")){
					logInfo = "" ;
				}

				logger = LogR.getLogger( clazz ) ;
				
				if( level == IntegrationConstant.LOG_LEVEL_INFO ){
					logger.info( logInfo );
				}else if( level == IntegrationConstant.LOG_LEVEL_WARN ){
					logger.warn( logInfo );
				}else if( level == IntegrationConstant.LOG_LEVEL_ERROR ){
					logger.error( logInfo );
				}else{
					logger.debug( logInfo );
				}
			}
		}
	}
	
	/**
	 * Test Case必须嵌入到工作流中，才可以输出到指定的日志文件中。
	 * Windchill Shell中，不会输出到指定的日志文件中。
	 * 
	 */
	public static void testCase() {
		System.out.println("Test Log Start......") ;
		
		logger.debug("Debug log......") ;
		
		printLog( CLASSNAME , "Debug Log" , 0 ) ;
		
		printLog( CLASSNAME , "Add Log" ) ;
		
		printLog( CLASSNAME , "WARNing Log" , 2 ) ;
		
		printLog( CLASSNAME , "Error Log" , 3) ;
		
		System.out.println("Test Log End......") ;
	}
	
	public static void main(String[] args) {
		
		testCase();
	}
	
	public static String startReleasePartLogInfo(WTPart part){
		String logInfo = "开始发布零部件信息到ERP：\n" ;
		logInfo = logInfo + CommonPDMUtil.getPartInfo( part );
		return logInfo ;
	}
	
	public static String finishReleasePartLogInfo(WTPart part){
		String logInfo = "成功发布零部件信息到ERP：\n" ;
		logInfo = logInfo + CommonPDMUtil.getPartInfo( part );
		return logInfo ;
	}
	
	public static  String failedReleasePartLogInfo(WTPart part){
		String logInfo = "发布零部件信息到ERP失败：\n" ;
		logInfo = logInfo + CommonPDMUtil.getPartInfo( part );
		return logInfo ;
	}
	
	public static  String notReleasePartLogInfo(WTPart part){
		String logInfo = "零部件信息不发布到ERP：\n" ;
		logInfo = logInfo + CommonPDMUtil.getPartInfo( part );
		return logInfo ;
	}
	
	public static String startReleaseBOMLogInfo(WTPart part){
		String logInfo = "开始发布零部件BOM结构信息到ERP：\n" ;
		logInfo = logInfo + CommonPDMUtil.getPartInfo( part );
		return logInfo ;
	}
	
	public static String finishReleaseBOMLogInfo(WTPart part){
		String logInfo = "成功发布零部件BOM结构信息到ERP：\n" ;
		logInfo = logInfo + CommonPDMUtil.getPartInfo( part );
		return logInfo ;
	}
	
	public static String failedReleaseBOMLogInfo(WTPart part){
		String logInfo = "发布零部件BOM结构信息到ERP失败：\n" ;
		logInfo = logInfo + CommonPDMUtil.getPartInfo( part );
		return logInfo ;
	}
	
	public static String notReleaseBOMLogInfo(WTPart part){
		String logInfo = "零部件BOM结构信息不发布到ERP：\n" ;
		logInfo = logInfo + CommonPDMUtil.getPartInfo( part );
		return logInfo ;
	}
	
	/**
	 * 解析变更发布错误
	 * @param partMap
	 * @param part
	 * @return
	 */
	public static WTKeyedHashMap getChangeErrorInfo( WTKeyedHashMap partMap , WTPart part ) {
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		try {
			List< WTPartUsageLink > list = BOMStructureUtil.getFirstLevelUsageLink(part) ;
							
			if( list != null ){
				Iterator< WTPartUsageLink > listI = list.iterator();
								
				while( listI.hasNext() ){
					WTPartUsageLink usageLink = listI.next();
									
					if( usageLink != null ){
						hashMap.put( usageLink , "父件发布异常，请根据后台日志检查" );
										
						return hashMap;
					}
				}
			}
		} catch (ConfigException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
		
		return hashMap;
	}
	
	/**
	 * 解析物料发布错误信息(存在问题)
	 * @param partMap
	 * @return
	 */
	public static WTKeyedHashMap getErrorInfo( WTKeyedHashMap partMap ) {
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		Iterator iterator = partMap.keySet().iterator();
		
		while( iterator.hasNext() ){
			Object obj = iterator.next();
			System.out.println( "Debug   obj : " + obj );
			if( obj != null ){
				if( obj instanceof ObjectReference ){
					ObjectReference orf = ( ObjectReference )obj;
					obj = orf.getObject();
				}
				
				if( obj instanceof WTPart ){
					WTPart part = ( WTPart )obj;
					
					if( part != null ){
						try {
							List< WTPartUsageLink > list = BOMStructureUtil.getFirstLevelUsageLink(part) ;
							System.out.println( "Debug   list : " + list );
							if( list != null ){
								Iterator< WTPartUsageLink > listI = list.iterator();
								
								while( listI.hasNext() ){
									WTPartUsageLink usageLink = listI.next();
									
									if( usageLink != null ){
										hashMap.put( usageLink , "父件发布异常，请根据后台日志检查" );
										
										return hashMap;
									}
								}
							}
						} catch (ConfigException e) {
							e.printStackTrace();
						} catch (WTException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		return hashMap;
	}
}
