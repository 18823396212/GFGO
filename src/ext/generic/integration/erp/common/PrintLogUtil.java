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

import java.io.Serializable;
import java.util.Date;

import wt.method.RemoteAccess;
/**
 * 格式化日志输出工具类
 * 
 * 输出日志到Method Server或者Background Method Server
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-07-09
 */ 
public class PrintLogUtil implements RemoteAccess, Serializable {

	private static final long serialVersionUID = -4972308066815397702L;

	/**
	 * 格式化日志，默认的verbose日志开关为true
	 * 
	 * @param clazz
	 * @param methodName
	 * @param logInfo
	 */
	public static void printLog(Class<?> clazz, String methodName , String logInfo ){
		if(clazz != null){
			printLog( clazz.getName() , methodName , logInfo);
		}
	}
	
	/**
	 * 格式化日志，默认的verbose日志开关为true
	 * 
	 * @param className
	 * @param methodName
	 * @param logInfo
	 */
	public static void printLog(String className, String methodName , String logInfo ){
		if(className != null){
			printLog( className , methodName , logInfo , true );
		}
	}
	
	/**
	 * 格式化日志
	 * 
	 * @param clazz
	 * @param methodName
	 * @param logInfo
	 * @param verbose
	 */
	public static void printLog(Class<?> clazz, String methodName , String logInfo , boolean verbose){
		if(clazz != null){
			printLog( clazz.getName() , methodName , logInfo , verbose );
		}
	}
	
	/**
	 * 格式化日志
	 * 
	 * @param className
	 * @param methodName
	 * @param logInfo
	 * @param verbose
	 */
	public static void printLog(String className, String methodName , String logInfo , boolean verbose){
		if(verbose){
			StringBuffer logBuff = new StringBuffer(">>> ") ;
			
			//格式化时间输出
			logBuff.append(new Date(System.currentTimeMillis()));
			logBuff.append(" Log Info : ");
			
			//加入方法名
			if(className != null ){
				logBuff.append(className) ;
				logBuff.append(".");
			}
			
			if(methodName == null){
				methodName = " ()===> " ;
			}else{
				methodName = methodName + "===> " ;
			}
			
			logBuff.append(methodName);
			
			//加入具体的Log描述
			if(logInfo == null || logInfo.equals("")){
				logInfo = "print logs" ;
			}
			
			logBuff.append(logInfo);
			
			System.out.println(logBuff.toString());
		}
	}
}
