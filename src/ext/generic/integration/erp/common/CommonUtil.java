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

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import wt.log4j.LogR;
import wt.session.SessionHelper;
import wt.util.WTException;
/**
 * 一般工具类
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-07-09
 */
public class CommonUtil {
	private static final String clazz = CommonUtil.class.getName() ;
	private static final Logger logger = LogR.getLogger(clazz);
	
	/**
	 * 获取当前时间
	 * 
	 * @return
	 */
	public static String getCurrentTime(){
		
		String currentTime = "" ;
		
		Date currentDate = new Date();
		
		DateFormat sdf = getDateFormat();
		
		try{
			currentTime = sdf.format(currentDate) ;
		}catch(Exception e){
			e.printStackTrace() ;
		}
		
		return currentTime ;
	}
	/**
	 * 获取当前时间--日期类型
	 * @return 日期
	 */
	public static Date getCurrentDate(){
		//设置中国时区
		TimeZone timeZone = TimeZone.getTimeZone(CommonConstant.CHINA_TIME_ZONE_ID) ;
		Calendar cal = Calendar.getInstance();
		try {
			cal = Calendar.getInstance(timeZone, SessionHelper.getLocale());
		} catch (WTException e) {
			e.printStackTrace();
		}
		return cal.getTime();
	}
	
	/**
	 * 按格式 yyyy-MM-dd HH:mm:ss，
	 * 将Timestamp类型的对象转换为String值
	 * 
	 * @param timestamp
	 * @return
	 */
	public static String getDateValue( Timestamp timestamp ){
		String dateValue = "";
		
		if( timestamp != null ){
			DateFormat sdf = getDateFormat();
			
			try {   
				dateValue = sdf.format( timestamp );
			} catch (Exception e) {   
				e.printStackTrace();   
			} finally{
				logger.debug("getDateValue( Timestamp timestamp ) In try block , return : " + dateValue);
			}
		}
		
		logger.debug("getDateValue( Timestamp timestamp ), return : " + dateValue);
		
		return dateValue ;
	}
	
	/**
	 * 格式化double类型浮点数
	 * 
	 * @param num
	 * @return
	 */
	public static String numFormat(double num){
		String number = numFormat(num , "0.00" ) ;
		
		return number ;
	}
	
	/**
	 * 格式化double类型浮点数
	 * 
	 * @param num
	 * @param format
	 * @return
	 */
	public static String numFormat(double num , String format){
		NumberFormat numberFormat = new DecimalFormat(format);
		
		return numberFormat.format(num) ;
	}
	
	/**
	 * 空值处理
	 * 
	 * @param str
	 * @return
	 */
	public static String nullStringConvert(String str) {
		if( str == null || str.toLowerCase().equals("null")){
			str = "" ;
		}else{
			str = str.trim();
		}
		return str ;
	}
	
	/**
	 * 打印Map
	 * 
	 * @param map
	 */
	public static void printMap( Map<String,String> map ){
		String result = getMapStringValue(map);
		
		logger.trace( "printMap( Map<String,String> map ) ="+ result);
	}
	
	/**
	 * 打印Map
	 * 
	 * @param map
	 */
	public static void printMap2( Map<String,String[]> map ){
		String result = getMapStringValue2(map);
		
		logger.trace( "printMap2( Map<String,String[]> map ) ="+result);
	}
	
	/**
	 * 获取Map中的所有键值对
	 * 
	 * @param map
	 * @return
	 */
	public static String getMapStringValue( Map<String,String> map ){
		StringBuffer buffer = new StringBuffer("\n");
		
		if( map != null ){
			buffer.append(clazz);
			buffer.append(".getMapStringValue( Map<String,String> map )\n");
			buffer.append("********* Start Print Map **********\n");
			
			Iterator<String> ite = map.keySet().iterator();
			
			while( ite.hasNext()){
				String key = ite.next();
				String value = map.get(key) ;
				
				buffer.append("Key : ") ;
				buffer.append(key) ;
				buffer.append(" ====> ") ;
				buffer.append(" Value : ");
				buffer.append(value) ;
				buffer.append("\n");
			}
			
			buffer.append("********* End Print Map **********\n");
		}
		
		return buffer.toString() ;
	}
	
	/**
	 * 获取Map中的所有键值对
	 * 
	 * @param map
	 * @return
	 */
	public static String getMapStringValue2( Map<String,String[]> map ){
		StringBuffer buffer = new StringBuffer("\n");
		
		if( map != null ){
			buffer.append(clazz);
			buffer.append(".getMapStringValue2( Map<String,String[]> map )\n");
			buffer.append("********* Start Print Map **********\n");
			
			Iterator<String> ite = map.keySet().iterator();
						
			while( ite.hasNext()){
				String key = ite.next();
				String[] values = map.get(key) ;
				
				String value = getArrayStringValue(values);
				
				buffer.append("Key : ") ;
				buffer.append(key) ;
				buffer.append(" ====> ") ;
				buffer.append(" Value : ");
				buffer.append(value) ;
				buffer.append("\n");
			}
			
			buffer.append("********* End Print Map **********\n");
		}
		
		return buffer.toString() ;
	}

	/**
	 * 获取数组中所有的值
	 * 
	 * @param values
	 * @return
	 */
	public static String getArrayStringValue(String[] values) {
		String value = "" ;
		
		if(values == null || values.length == 0){
			value = "" ;
		}else{
			for(int i=0 ; i < values.length ; i++ ){
				value = value + values[i] + CommonConstant.COMMA_SEPARATOR ;
			}
			
			if( value.endsWith( CommonConstant.COMMA_SEPARATOR ) ){
				value = value.substring(0, value.lastIndexOf( CommonConstant.COMMA_SEPARATOR )) ;
			}
		}
		return value;
	}
	
	/**
	 * 获取List中的所有值
	 * 
	 * @param list
	 * @return
	 */
	public static String getListValue(List<String> list){
		String listValues = "" ;
		
		if( list != null ){
			int size = list.size() ;
			
			for(int i=0 ; i < size ; i++ ){
				listValues = listValues + list.get(i) + CommonConstant.COMMA_SEPARATOR ;
			}
			
			if( listValues.endsWith( CommonConstant.COMMA_SEPARATOR ) ){
				listValues = listValues.substring(0, listValues.lastIndexOf( CommonConstant.COMMA_SEPARATOR )) ;
			}
		}
		
		return listValues ;
	}
	
	/**
	 * 格式化日期，并设置为中国时区
	 * 
	 * @return
	 */
	private static DateFormat getDateFormat() {
		//设置日期格式
		DateFormat sdf = new SimpleDateFormat( CommonConstant.DATE_FORMAT_1 );
		
		//设置中国时区
		TimeZone timeZone = TimeZone.getTimeZone(CommonConstant.CHINA_TIME_ZONE_ID) ;
		
		if( timeZone != null ){
			sdf.setTimeZone(timeZone) ;
		}
		
		return sdf;
	}
	
	public static void main(String[] args) {
//		String[] values = new String[]{"1" , "2" , "3" , "4" , "5"} ;
//		
//		System.out.println( getArrayStringValue( values ));
		
		Map< String , String > map = new HashMap<String, String>() ;
		map.put("aa", "AA") ;
		map.put("bb", "BB") ;
		map.put("cc", "CC") ;
		map.put("dd", "DD") ;
		printMap(null) ;
		printMap(map) ;
		
		Map< String , String[] > map2 = new HashMap<String, String[]>() ;
		map2.put("aa", new String[]{"AA","11111"}) ;
		map2.put("bb", new String[]{"BB"}) ;
		map2.put("cc", new String[]{"CC","3333"}) ;
		map2.put("dd", new String[]{"DD","444444"}) ;
		printMap2(null) ;
		printMap2(map2) ;
		
		System.out.println("Current Time : " + getCurrentTime() ) ;
		
		Object testStr = new String("abc");
		
		System.out.println( TimeZone.getDefault());
		
		System.out.println(testStr.toString()) ;
	}
}
