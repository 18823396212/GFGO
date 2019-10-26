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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/***
 * 位号合并工具类
 */
public class PartOccurrence {
	private static final String COMMA = "," ;
	
	private String longOccurrenceStr = null  ;
	
	private String shortOccurrence = null ;
	
	private List<String> shortOccurrenceStrList = null ;
	
	public PartOccurrence(){
		this.shortOccurrenceStrList = new ArrayList<String>() ;
	}
	
	public PartOccurrence(String longOccurrenceStr){
		this() ;
		
		this.longOccurrenceStr = longOccurrenceStr ;
	}
	
	/**
	 * 生成短位号字符串例如：R1,R2,R3,R4,R5
	 * 
	 * 则返回R1-R5
	 * 
	 */
	public void generate(){
		if( ! isNullString( this.longOccurrenceStr ) ){
			
			//将长位号(形如：R1,R2,R3,R4,R5)用逗号分隔
			String[] occurrenceArray = longOccurrenceStr.split( COMMA ) ;
			
			Map<String , Map<Integer,String> > occurrenceMap = getMap( occurrenceArray ) ;
			
			generateShortOccurrenceStrList( occurrenceMap ) ;
			
			generateShortOccurrence() ;
		}
	}

	/**
	 * 将位号按照开头的字符串进行分类，并且放入HashMap中
	 * 
	 * 如果是以非数字字符串开头，以数字字符串结尾的位号，则进行分类
	 * 否则，直接放入shortOccurrenceStrList中
	 * 
	 * @param occurrenceArray
	 * @return
	 */
	private Map<String, Map<Integer, String>> getMap(String[] occurrenceArray) {
		Map<String, Map<Integer, String>> occurrenceMap = new HashMap<String, Map<Integer, String>>() ;
		
		if( occurrenceArray != null ){
			for( int i = 0 ; i < occurrenceArray.length ; i++  ){
				if( needProcess( occurrenceArray[i] ) ){
					
					//获取位号后面的数字部分
					String lastIntValueStr = getLastIntValue( occurrenceArray[i] ) ;
					
					//获取位号前面的字符串部分
					String previousKeyStr = getPreviousKeyValue ( occurrenceArray[i] , lastIntValueStr ) ;
					
					addToMap( occurrenceMap , previousKeyStr , lastIntValueStr , occurrenceArray[i] ) ;
				}else{
					addToList( occurrenceArray[i] );
				}
			}
		}
		
		return occurrenceMap ;
	}

	/**
	 * 字符串是否需要进行处理
	 * 
	 * 如果字符串不为null值，并且字符串以非数字字符开头，以数字字符结尾，则返回true，否则返回false
	 * 
	 * @param occurrenceStr
	 * @return
	 */
	private boolean needProcess(String occurrenceStr) {
		if( isNullString( occurrenceStr ) ){
			
			return false ;
		}else if( startWithNoDigit( occurrenceStr ) && endWithDigit( occurrenceStr ) ){
			
			return true ;
		}
		
		return false ;
	}

	/**
	 * 判断字符串是否以以非数字字符开头
	 * 
	 * 以数字开头，则返回false，否则返回true
	 * 
	 * @param occurrenceStr
	 * @return
	 */
	private boolean startWithNoDigit(String occurrenceStr) {
		Pattern pattern = Pattern.compile("[^0-9](.)*"); 
		
		Matcher matcher = pattern.matcher(occurrenceStr);
		
		return matcher.find() ;
	}

	/**
	 * 判断字符串是否以数字结尾
	 * 
	 * 以数字结尾，则返回true，否则返回false
	 * 
	 * @param occurrenceStr
	 * @return
	 */
	private boolean endWithDigit(String occurrenceStr) {
		Pattern pattern = Pattern.compile("\\d+$"); 
		
		Matcher matcher = pattern.matcher(occurrenceStr);
		
		return matcher.find() ;
	}

	/**
	 * 如果字符串以数字结尾，则获取结尾部分的数字字符串
	 * 
	 * @param occurrenceStr
	 * @return
	 */
	private String getLastIntValue(String occurrenceStr ) {
		
		return occurrenceStr.replaceAll(".*[^\\d](?=(\\d+))","");
	}

	/**
	 * 如果字符串以非数字开头，则获取开头部分的非数字字符串
	 * 
	 * @param occurrenceStr
	 * @param lastIntValueStr
	 * @return
	 */
	private String getPreviousKeyValue(String occurrenceStr , String lastIntValueStr) {
		int lastIndex = occurrenceStr.lastIndexOf( lastIntValueStr ) ;

		return occurrenceStr.substring(0 , lastIndex );
	}

	private void addToMap(Map<String, Map<Integer, String>> occurrenceMap,
			String previousKeyStr, String lastIntValueStr, String occurrenceStr) {
		
		Map<Integer, String> previousKeyMap = occurrenceMap.get( previousKeyStr ) ;
		
		//如果以previousKeyStr开头的子Map还没有，则构造一个TreeMap对象
		//每一个previousKeyStr会对应一个子Map
		if( previousKeyMap == null ){			
			previousKeyMap = new TreeMap<Integer, String>() ;
			
			occurrenceMap.put( previousKeyStr , previousKeyMap ) ;
		}
		
		Integer lastIntValue = Integer.parseInt( lastIntValueStr ) ;
		
		//将位号后面的数字和整个位号字符串放入子Map中
		previousKeyMap.put( lastIntValue , occurrenceStr ) ;
	}
	
	/**
	 * 根据按照开头的字符串进行分类后的Map，计算出短位号的List
	 * 
	 * @param occurrenceMap
	 */
	private void generateShortOccurrenceStrList(Map<String, Map<Integer, String>> occurrenceMap) {
		Iterator<String> occurrenceMapKeySetIte = occurrenceMap.keySet().iterator() ;
		
		while( occurrenceMapKeySetIte.hasNext() ){
			String previousStr = occurrenceMapKeySetIte.next();
			
			Map<Integer, String> previousKeyMap = occurrenceMap.get( previousStr ) ;
			
			processPreviousKeyMap(previousStr , previousKeyMap)  ;
		}
	}
	
	/**
	 * 处理一个子Map
	 * 
	 * @param previousStr
	 * @param previousKeyMap
	 */
	private void processPreviousKeyMap(String previousStr , Map<Integer, String> previousKeyMap) {
		Iterator<Integer> previousKeyMapKeySetIte = previousKeyMap.keySet().iterator() ;
		
		List<Integer> tempList = new ArrayList<Integer>() ;

		while( previousKeyMapKeySetIte.hasNext() ){			
			Integer currentKey = previousKeyMapKeySetIte.next() ;
			
			//判断当前的key是否应该放入一个新的List中
			//如果不是，则加入当前List
			//如果是，则处理之前已有的List，然后将List的内容清空，然后再把当前的key放入List中
			if( ! isNextListKey( tempList , currentKey) ){
				tempList.add( currentKey ) ;
			}else{
				addToList( tempList , previousKeyMap ) ;
				
				tempList.clear() ;
				
				tempList.add( currentKey ) ;
			}
		}
		
		//如果循环结束后，List不为空，则表示，最后的一批数据尚未处理
		if( ! tempList.isEmpty() ){
			addToList( tempList , previousKeyMap ) ;
			
			tempList.clear();
		}
	}

	/**
	 * 判断currentKey是否应该放入一个新的List中
	 * 
	 * @param tempList
	 * @param currentKey
	 * @return
	 */
	private boolean isNextListKey(List<Integer> tempList, Integer currentKey) {
		//如果tempList为空，则表示当前的List才刚开始放入数据
		if( tempList.isEmpty() ){
			return false ;
		}
		
		int size = tempList.size() ;
		if( size > 0 ){
			//如果tempList中的最后一个Integer的数值为当前的currentKey的数值加1，
			//则表示currentKey应该放入当前的List中，而不是放入新的List中处理
			Integer lastKey = tempList.get( size - 1 ) ;
			if( currentKey.intValue() == (lastKey.intValue() + 1 )){
				return false ;
			}
		}
		
		return true ;
	}
	
	private void addToList(String occurrenceStr) {
		if( ( ! isNullString(occurrenceStr) ) && ( ! this.shortOccurrenceStrList.contains(occurrenceStr) ) ){
			
			this.shortOccurrenceStrList.add(occurrenceStr) ;
		}
	}

	/**
	 * 根据tempList中的数据，生成短位号，即tempList中的第一个元素为起始位号，最后一个位置的为结束位号
	 * 
	 * @param tempList
	 * @param previousKeyMap
	 */
	private void addToList(List<Integer> tempList,Map<Integer, String> previousKeyMap) {
		if( tempList.isEmpty() ){
			return ;
		}
		
		int size = tempList.size() ;
		if( size > 0 ){
			int startKey = tempList.get(0) ;
			int endKey = tempList.get(size - 1) ;
			
			String startOccurrence = previousKeyMap.get( startKey ) ;
			String endOccurrence = previousKeyMap.get( endKey ) ;
			
			addToList( generateSubOccurrenceStr(startOccurrence , endOccurrence) ) ;
		}
		
	}
	
	/**
	 * 短位号 = 起始值+"-" + 结束值
	 * 
	 * @param startOccurrence
	 * @param endOccurrence
	 * @return
	 */
	private String generateSubOccurrenceStr(String startOccurrence,String endOccurrence) {
		if( startOccurrence.equals( endOccurrence ) ){
			return startOccurrence ;
		}else{
			return startOccurrence + "-" + endOccurrence ;
		}
	}

	/**
	 * 将shortOccurrenceStrList中的数据进行拼接，产生完整的短位号
	 * 
	 */
	private void generateShortOccurrence() {
//		Collections.sort( this.shortOccurrenceStrList) ;
		
		int size = this.shortOccurrenceStrList.size() ;
		
		StringBuffer strBuffer = new StringBuffer("") ;
		for( int i = 0 ; i < size ; i++ ){
			strBuffer.append( this.shortOccurrenceStrList.get(i) ) ;
			strBuffer.append(COMMA) ;
		}
		
		strBuffer.deleteCharAt( strBuffer.length() - 1) ;
		
		this.shortOccurrence = strBuffer.toString() ;
	}
	
	private boolean isNullString(String longOccurrenceStr) {
		if( longOccurrenceStr == null || longOccurrenceStr.trim().isEmpty()){
			return true ;
		}else{
			return false ;
		}
	}
	
	public String getLongOccurrenceStr() {
		return this.longOccurrenceStr;
	}

	public void setLongOccurrenceStr(String longOccurrenceStr) {
		this.longOccurrenceStr = longOccurrenceStr;
	}

	public String getShortOccurrence() {
		return this.shortOccurrence;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String occurrenceStr = "S1,S2,S3,S100,S101" ;
		
		PartOccurrence partOccurrence = new PartOccurrence( occurrenceStr ) ;
		partOccurrence.generate() ;
		System.out.println(partOccurrence.getShortOccurrence()) ;
	}

}
