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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import wt.log4j.LogR;
import ext.generic.integration.erp.common.CommonUtil;
/**
 * Info*E 通用查询类使用的XML配置文件解析类
 * 
 * 主要解析JDBC Adapter,中间表,需要查询列
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-10-08
 */
public class QueryXMLConfigUtil {
	private static String clazz = QueryXMLConfigUtil.class.getName() ;
	
	private static final Logger log = LogR.getLogger(clazz);
	
	//Info*E适配器名称
	private String ieAdapter = "" ;
	
	//需要查询的表名
	private String table = "" ;
	
	//中间表与PDM系统的属性映射关系
	private Map<String,String> attMapping = new HashMap<String,String>();
	
	//查询列表存储
	private List<String> queryAttributes = new ArrayList<String>();

	//查询条件，目前，暂未确定逻辑
	private String where = "" ;
	
	public QueryXMLConfigUtil(String file){
		parse( file );
	}
	
	/**
	 * 获取Info*E的JDBC的名称
	 * 
	 * @return
	 */
	public String getIeAdapter() {
		return ieAdapter;
	}

	/**
	 * 获取需要查询的表名
	 * 
	 * @return
	 */
	public String getTable() {
		return table;
	}

	/**
	 * 获取中间表列名和PDM属性的映射关系
	 * 
	 * @return
	 */
	public Map<String, String> getAttMapping() {
		return attMapping;
	}
	
	/***
	 * 获取查询字段列表
	 * @return
	 */
	public List<String> getQueryAttributes() {
		return queryAttributes;
	}
	
	/**
	 * 打印中间表列名和PDM属性的映射关系
	 * 
	 */
	public void printMap(){
		CommonUtil.printMap( this.attMapping ) ;
	}

	public String getWhere() {
		return where;
	}
	
	/**
	 * 从XML文件中解析需要的信息
	 * 
	 * @param file
	 */
	private void parse( String file ){
		if( file == null ){
			return ;
		}
		
		try{
			//获取XML的DOM对象
			DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = fac.newDocumentBuilder();
			Document document = documentBuilder.parse(file);
			
			if(document == null){
				log.debug( "<<<Can't Parse Config File : " + file );
				
				return ;
			}else{
				//解析相关参数
//				Element root = document.getDocumentElement() ;
				
				initIeAdapter( document ) ;
				
				initTableName( document ) ;
				
				initColumn( document ) ;
				
				initWhere( document ) ;
				
				checkParameter(file);
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 初始化Info*E JDBC适配器名
	 * 
	 * @param document
	 */
	private void initIeAdapter( Document document ){
		NodeList nodeList = document.getElementsByTagName( IntegrationConstant.NODE_NAME_IEADAPTER );
		
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			
			if( size > 0 ){
				Node node = nodeList.item( 0 );
				
				if( node != null && node instanceof Element){
					Element element = (Element) node ;
					
					this.ieAdapter = element.getAttribute( IntegrationConstant.NODE_NAME_IEADAPTER_ATT ) ;
				}
			}
		}
	}
	
	/**
	 * 初始化表名
	 * 
	 * @param document
	 */
	private void initTableName( Document document ){
		NodeList nodeList = document.getElementsByTagName( IntegrationConstant.NODE_NAME_TABLE );
		
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			
			if( size > 0 ){
				Node node = nodeList.item( 0 );
				
				if( node != null && node instanceof Element){
					Element element = (Element) node ;
					
					this.table = element.getAttribute( IntegrationConstant.NODE_NAME_TABLE_ATT ) ;
				}
			}
		}
	}
	
	/**
	 * 初始化配置文件中的数据库列与PDM系统中的属性映射关系
	 * 
	 * @param document
	 */
	private void initColumn( Document document ){
		NodeList nodeList = document.getElementsByTagName( IntegrationConstant.NODE_NAME_ATTRIBUTE );
		
		if( nodeList != null ){
			int length = nodeList.getLength() ;
			
			for( int i=0 ; i < length ; i++ ){
				Node node = nodeList.item(i) ;
				
				if( node != null && node instanceof Element ){
					Element element = (Element) node ;
					
					this.attMapping = getAllColumns( element );
				}
			}
		}
	}
	
	/**
	 * 初始化配置文件中的数据库列与PDM系统中的属性映射关系
	 * 
	 * @param element
	 * @return
	 */
	private Map<String , String > getAllColumns(Element element){
		Map<String , String > map  = new HashMap<String,String>() ;
		
		//获取子节点列表
		NodeList childList = element.getChildNodes() ;
		
		if(childList != null){
			int size = childList.getLength();
			
			for(int i=0 ; i < size ; i++ ){
				Node childNode = childList.item(i);
				
				//判断子阶类型，与NodeList数据存储结构有关，必须判断此类型
				if( childNode != null && childNode instanceof Element){
					Element childElement = (Element)childNode ;
					
					String dbcolumn = childElement.getAttribute( IntegrationConstant.NODE_NAME_ATT_DB ) ;
					
					if( dbcolumn == null){
						dbcolumn = "" ;
					}
					
					dbcolumn = dbcolumn.trim() ;
					
					String pdmLogicId = childElement.getAttribute( IntegrationConstant.NODE_NAME_ATT_PDM ) ;
					
					if( pdmLogicId == null ){
						pdmLogicId = "" ;
					}
					
					pdmLogicId.trim() ;
					
					if( ! dbcolumn.equals("")){
						map.put(dbcolumn, pdmLogicId) ;
						
						this.queryAttributes.add( dbcolumn );
					}
				}
			}
		}
		
		return map ;
	}
	
	private void initWhere( Document document ){
		NodeList nodeList = document.getElementsByTagName( IntegrationConstant.NODE_NAME_WHERE );
		
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			
			if( size > 0 ){
				Node node = nodeList.item( 0 );
				
				if( node != null && node instanceof Element){
					//Element element = (Element) node ;
					
					//where逻辑尚未确定如何处理
				}
			}
		}
	}
	
	/**
	 * 初始化参数检查
	 * 
	 */
	private void checkParameter(String file){
		log.debug( "<<<Entering In checkParameter()......") ;
		log.debug( "<<<检查的文件名为：" + file ) ;
		
		try{
			if(this.ieAdapter == null || this.ieAdapter.equals("")){
				log.error( "<<<初始化参数错误：ieAdapter为空" ) ;
			}else{
				log.debug( "<<<解析XML文件，ieAdapter = " + this.ieAdapter ) ;
			}
			
			if(this.table == null || this.table.equals("")){
				log.error( "<<<初始化参数错误：table为空" ) ;
			}else{
				log.debug( "<<<解析XML文件，table = " + this.table ) ;
			}
			
			if( this.attMapping == null || this.attMapping.isEmpty() ){
				log.error( "<<<初始化参数错误：attMapping为空" ) ;
			}else{
				
				log.debug( "<<<解析XML文件，查询的列为：\n" + CommonUtil.getMapStringValue( this.attMapping ) ) ;
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			log.debug( "<<<Existed Out checkParameter()......") ;
		}
	}
	
	/**
	 * 测试用例
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		QueryXMLConfigUtil configUtil = null ;
		
		if(args != null && args.length == 1 && args[0] != null ){
			configUtil = new QueryXMLConfigUtil( args[0] ) ;
		}else{
			configUtil = new QueryXMLConfigUtil("E:/CVS/windchill/Sprocomm/codebase/ext/generic/integration/erp/config/QueryPartReleaseAttributes.xml") ;
		}

		System.out.println("IeAdapter : " + configUtil.getIeAdapter()); 
		System.out.println("Table Name : " + configUtil.getTable());
		
		Map<String,String> map = configUtil.getAttMapping();
		System.out.println(map);
		
		List< String > attributes = configUtil.getQueryAttributes();
		
		
	}
}
