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

import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.generic.integration.erp.common.CommonUtil;
import ext.generic.integration.erp.config.ERPPropertiesUtil;
/**
 * BusinessRule.xml文件解析工具类
 * 
 * BusinessRule.xml文件主要用于定义一些数据发布规则，权限校验规则等。
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-10-08
 */
public class BusinessRuleXMLConfigUtil {
	private static final String BUSINESS_RULE_XML_CONFIG_FILE = "BusinessRule.xml" ;
	
	private static final String CLASSNAME = BusinessRuleXMLConfigUtil.class.getName() ;

	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	
	//不发布中间表的零部件类型
	private static List<String> noReleasePartSoftTypeList = new ArrayList<String>()  ;
	
	//不发布到中间表的分类属性内部值
	private static List<String> noReleasePartClassficationList = new ArrayList<String>() ;
	
	//不发布到中间表的IBA属性
	private static Map<String, String> noRelasePartIBAMap = new HashMap();
	
	//物料发布状态的IBA属性
	private static String partReleaseStatus = "" ;

	//BOM发布状态的IBA属性
	private static String bomReleaseStatus = "" ;
	
	//是否发布全局替代关系
	private static boolean releaseAlternateLink = false ;
	
	//可以查询价格信息的用户组列表
	private static List<String> readPriceUserGroup = null ;
	
	/************ 手工发布功能控制条件 ************/
	
	//可以执行发布操作的团队角色(Part)
	private static List<String> manuallyPublishUserRole = null ;
	
	//可以执行发布操作的团队角色(BOM)
	private static List<String> manuallyPublishBOMUserRole = null ;
	
	//可以执行发布操作的用户组(Part)
	private static List<String> manuallyPublishUserGroup = null ;
	
	//可以执行发布操作的用户组(BOM)
	private static List<String> manuallyPublishBOMUserGroup = null ;
	
	//可以执行发布操作的零部件状态
	private static List<String> manuallyPublishPartState = null ;
	
	//可以执行发布BOM操作的零部件状态
	private static List<String> manuallyPublishBOMState = null ;

	//可以执行发布操作的零部件视图
	private static List<String> manuallyPublishPartView = null ;
	
	/************ End ************/
	
	private static String isReleasePhantom = null;
	
	private static String isReleaseHidePartInStructure = null;
	
	//是否启用数据备份功能
	private static boolean startBackupData = false ;
	
	private static final BusinessRuleXMLConfigUtil configUtil = new BusinessRuleXMLConfigUtil();
	
	private BusinessRuleXMLConfigUtil(){
		String configFile = CommonPDMUtil.getCodebasePath() + ERPPropertiesUtil.getInstance().readProperties().get(IntegrationConstant.ERP_CONFIG_FILE_PATH) + "/" ;
		configFile = configFile + BUSINESS_RULE_XML_CONFIG_FILE ;
		
		logger.debug("Business Rule Config File : " + configFile);
		
		parse( configFile ) ;
	}
	
	public static BusinessRuleXMLConfigUtil getInstance() {
		return configUtil;
	}

	public List<String> classfication() {
		return noReleasePartSoftTypeList;
	}
	
	public static List<String> getNoReleasePartSoftTypeList() {
		return noReleasePartSoftTypeList;
	}
	
	public static List<String> getNoReleasePartClassficationList() {
		return noReleasePartClassficationList;
	}

	public static Map<String, String> getNoRelasePartIBAList() {
		return noRelasePartIBAMap;
	}

	public String getPartReleaseStatus() {
		return partReleaseStatus;
	}

	public String getBomReleaseStatus() {
		return bomReleaseStatus;
	}

	public boolean isReleaseAlternateLink() {
		return releaseAlternateLink;
	}


	public List<String> getReadPriceUserGroup() {
		return readPriceUserGroup;
	}

	public List<String> getManuallyPublishUserRole() {
		return manuallyPublishUserRole;
	}

	public List<String> getManuallyPublishUserGroup() {
		return manuallyPublishUserGroup;
	}

	public List<String> getManuallyPublishPartState() {
		return manuallyPublishPartState;
	}
	
	public List<String> getManuallyPublishBOMState() {
		return manuallyPublishBOMState;
	}

	public List<String> getManuallyPublishPartView() {
		return manuallyPublishPartView;
	}
	
	public List<String> getManuallyPublishBOMUserGroup() {
		return manuallyPublishBOMUserGroup;
	}
	
	public List<String> getManuallyPublishBOMUserRole() {
		return manuallyPublishBOMUserRole;
	}
	
	public String getIsReleasePhantom() {
		return isReleasePhantom;
	}

	public String getIsReleaseHidePartInStructure() {
		return isReleaseHidePartInStructure;
	}
	
	public boolean isStartBackupData() {
		return startBackupData;
	}
	
	private void parse( String configFile ){
		try{
			//获取XML的DOM对象
			DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = fac.newDocumentBuilder();
			Document document = documentBuilder.parse( configFile );
			
			if(document == null){
				logger.debug("Can't Parse Config File : " + configFile);
				
				return ;
			}else{
				//解析相关参数
//				Element root = document.getDocumentElement() ;
				
				initNoReleasePartSoftTypeList( document );
				
				initNoReleasePartClassficationList( document );
				
				initNoReleasePartIBAList( document );
				
				initPartReleaseStatus( document );
				
				initBOMReleaseStatus( document );
				
				initReleaseAlternateLink( document );
				
				initReadPriceUserGroup( document ) ;

				initManuallyPublish( document ) ;
				
				initStartBackupData( document ) ;
				
				initisReleasePhantom( document );
				
				initisReleaseHidePartInStructure( document );
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			logger.debug("Not Release Part Soft Type List : " + CommonUtil.getListValue( noReleasePartSoftTypeList ));
			logger.debug("Not Release Part Classfication List : " + CommonUtil.getListValue( noReleasePartClassficationList ));
			logger.debug("Not Release Part IBA List : " + CommonUtil.getMapStringValue( noRelasePartIBAMap ));
			logger.debug("releaseAlternateLink : " + releaseAlternateLink);
			logger.debug("Read Price User Group : " + CommonUtil.getListValue( readPriceUserGroup ));
			logger.debug("startBackupData : " + startBackupData);


			logger.debug("Manually Publish User Role : " + CommonUtil.getListValue( manuallyPublishUserRole ) ) ;
			logger.debug("Manually Publish User Group : " + CommonUtil.getListValue( manuallyPublishUserGroup ) ) ;
			logger.debug("Manually Publish Part State : " + CommonUtil.getListValue( manuallyPublishPartState ) ) ;
			logger.debug("Manually Publish Part View : " + CommonUtil.getListValue( manuallyPublishPartView ) ) ;
		}
	}
	
	private static void initisReleaseHidePartInStructure( Document document ){
		NodeList nodeList = document.getElementsByTagName( IntegrationConstant.NODE_NAME_PART_HIDEPARTINSTRUCTURE );
		
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			if( size > 0 ){
				Node node = nodeList.item( 0 );
				if( node != null && node instanceof Element ){
					Element element = ( Element ) node ;
					
					isReleaseHidePartInStructure = element.getAttribute( IntegrationConstant.NODE_NAME_ALTERNATE_LINKE_ATT ) ;
					
					if( isReleaseHidePartInStructure != null ){
						isReleaseHidePartInStructure = isReleaseHidePartInStructure.trim() ;
					}else{
						isReleaseHidePartInStructure = "" ;
					}
				}
			}
		}
		
	}
	
	private static void initisReleasePhantom( Document document ){
		NodeList nodeList = document.getElementsByTagName( IntegrationConstant.NODE_NAME_PART_PHANTOM );
		
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			if( size > 0 ){
				Node node = nodeList.item( 0 );
				if( node != null && node instanceof Element ){
					Element element = ( Element ) node ;
					
					isReleasePhantom = element.getAttribute( IntegrationConstant.NODE_NAME_ALTERNATE_LINKE_ATT ) ;
					
					if( isReleasePhantom != null ){
						isReleasePhantom = isReleasePhantom.trim() ;
					}else{
						isReleasePhantom = "" ;
					}
				}
			}
		}
		
	}
	
	private static void initNoReleasePartSoftTypeList( Document document ){
		
		
		
		NodeList nodeList = document.getElementsByTagName( IntegrationConstant.NODE_NAME_WTPART );
		
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			for( int i = 0 ; i < size ; i++ ){
				Node node = nodeList.item( i );
				if( node != null && node instanceof Element){
					Element element = (Element) node ;
					
					initNoReleasePartSoftTypeList(element , noReleasePartSoftTypeList );
					
				}
			}
		}
		
	}
	
	private static void initNoReleasePartClassficationList( Document document ){
		
//		noReleasePartClassficationList = new ArrayList<String>() ;
		
		NodeList nodeList = document.getElementsByTagName( IntegrationConstant.NODE_NAME_WTPART );
		
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			for( int i = 0 ; i < size ; i++ ){
				Node node = nodeList.item( i );
				if( node != null && node instanceof Element){
					Element element = (Element) node ;
					
					initNoReleasePartClassficationList(element , noReleasePartClassficationList );
					
				}
			}
		}
		
	}
	
    private static void initNoReleasePartIBAList( Document document ){
		
//    	noRelasePartIBAMap = new HashMap<String, String>() ;
		
		NodeList nodeList = document.getElementsByTagName( IntegrationConstant.NODE_NAME_WTPART );
		
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			for( int i = 0 ; i < size ; i++ ){
				Node node = nodeList.item( i );
				if( node != null && node instanceof Element){
					Element element = (Element) node ;
					
					initNoReleasePartIBAList(element , noRelasePartIBAMap );
					
				}
			}
		}
		
	}

	private static void initNoReleasePartSoftTypeList( Element element , List<String> list ) {
		NodeList childList = element.getChildNodes() ;
		
		if( childList != null ){
			int childListSize = childList.getLength() ;
			for( int n = 0 ; n < childListSize ; n ++ ){
				Node childNode = childList.item(n) ;
				
				if( childNode != null && childNode instanceof Element ){
					Element childElement = ( Element ) childNode ;
					
					String partSoftType = childElement.getAttribute( IntegrationConstant.NODE_NAME_WTPART_ATT ) ;
					
					if(partSoftType != null){
						partSoftType = partSoftType.trim() ;
						
						if( ! partSoftType.equals("") ){
							list.add( partSoftType ) ;
						}
					}
				}
			}
		}
	}
	
	private static void initNoReleasePartClassficationList( Element element , List<String> list ) {
		NodeList childList = element.getChildNodes() ;
		
		if( childList != null ){
			int childListSize = childList.getLength() ;
			for( int n = 0 ; n < childListSize ; n ++ ){
				Node childNode = childList.item(n) ;
				
				if( childNode != null && childNode instanceof Element ){
					Element childElement = ( Element ) childNode ;
					
					String classfication = childElement.getAttribute( IntegrationConstant.NODE_NAME_WTPART_CLAFFICATION ) ;
					
					if(classfication != null){
						classfication = classfication.trim() ;
						
						if( ! classfication.equals("") ){
							list.add( classfication ) ;
						}
					}
				}
			}
		}
	}
	
	private static void initNoReleasePartIBAList( Element element , Map<String, String> map ) {
		NodeList childList = element.getChildNodes() ;
		
		if( childList != null ){
			int childListSize = childList.getLength() ;
			for( int n = 0 ; n < childListSize ; n ++ ){
				Node childNode = childList.item(n) ;
				
				if( childNode != null && childNode instanceof Element ){
					Element childElement = ( Element ) childNode ;
					
					String ibaLogicId = childElement.getAttribute( IntegrationConstant.NODE_NAME_WTPART_IBA_LOGICID ) ;
					
					String ibaValue = childElement.getAttribute( IntegrationConstant.NODE_NAME_WTPART_IBA_VALUE ) ;
					
					if(ibaLogicId != null && ibaValue != null){
						ibaLogicId = ibaLogicId.trim() ;
						ibaValue = ibaValue.trim() ;
						
						if( !ibaLogicId.isEmpty() && !ibaValue.isEmpty()){
							map.put(ibaLogicId, ibaValue);
						}
					}
				}
			}
		}
	}
	
	private static void initPartReleaseStatus( Document document ){
		NodeList nodeList = document.getElementsByTagName( IntegrationConstant.NODE_NAME_PART_RELEASE_STATUS );
		
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			if( size > 0 ){
				Node node = nodeList.item( 0 );
				if( node != null && node instanceof Element ){
					Element element = ( Element ) node ;
					
					partReleaseStatus = element.getAttribute( IntegrationConstant.NODE_NAME_LOGIC_ID ) ;
					
					if( partReleaseStatus != null ){
						partReleaseStatus = partReleaseStatus.trim() ;
					}else{
						partReleaseStatus = "" ;
					}
				}
			}
		}
		
	}
	
	private static void initBOMReleaseStatus( Document document ){
		NodeList nodeList = document.getElementsByTagName( IntegrationConstant.NODE_NAME_BOM_RELEASE_STATUS );
		
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			if( size > 0 ){
				Node node = nodeList.item( 0 );
				if( node != null && node instanceof Element ){
					Element element = ( Element ) node ;
					
					bomReleaseStatus = element.getAttribute( IntegrationConstant.NODE_NAME_LOGIC_ID ) ;
					
					if( bomReleaseStatus != null ){
						bomReleaseStatus = bomReleaseStatus.trim() ;
					}else{
						bomReleaseStatus = "" ;
					}
				}
			}
		}
	}
	
	private static void initReleaseAlternateLink( Document document ){
		NodeList nodeList = document.getElementsByTagName( IntegrationConstant.NODE_NAME_ALTERNATE_LINKE );
		
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			if( size > 0 ){
				Node node = nodeList.item( 0 );
				if( node != null && node instanceof Element ){
					Element element = ( Element ) node ;
					
					String alternateLinkRelease = element.getAttribute( IntegrationConstant.NODE_NAME_ALTERNATE_LINKE_ATT ) ;
					
					if( alternateLinkRelease != null ){
						alternateLinkRelease = alternateLinkRelease.trim() ;
						
						if( alternateLinkRelease.equalsIgnoreCase( IntegrationConstant.TRUE_VALUE ) ){
							releaseAlternateLink = true ;
						}else{
							releaseAlternateLink = false ;
						}
					}
				}
			}
		}
	}
	
	private static void initReadPriceUserGroup( Document document ){
		readPriceUserGroup = new ArrayList<String>();
		
		NodeList nodeList = document.getElementsByTagName( IntegrationConstant.NODE_NAME_WTPART_PRICE );
		
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			for( int i = 0 ; i < size ; i++ ){
				Node node = nodeList.item( i );
				if( node != null && node instanceof Element){
					Element element = (Element) node ;
					
					initReadPriceUserGroup(element , readPriceUserGroup );
					
				}
			}
		}
	}
	
	private static void initReadPriceUserGroup( Element element , List<String> list ){
		NodeList childList = element.getChildNodes() ;
		
		if( childList != null ){
			int size = childList.getLength() ;
			for( int i = 0 ; i< size ; i++ ){
				Node node = childList.item(i) ;
				
				if( node != null && node instanceof Element){
					Element childElement = ( Element ) node ;
					
					String userGroupName = childElement.getAttribute(IntegrationConstant.NODE_NAME_WTPART_PRICE_ATT);
					
					if( userGroupName != null ){
						userGroupName = userGroupName.trim() ;
						
						if( ! userGroupName.equals("")){
							list.add(userGroupName) ;
						}
					}
				}
			}
		}
	}
	
	private static void initManuallyPublish( Document document ){		
		NodeList nodeList = document.getElementsByTagName( IntegrationConstant.NODE_NAME_MANUALLY_PUBLISH_ATT );
		
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			for( int i = 0 ; i < size ; i++ ){
				Node node = nodeList.item( i );
				if( node != null && node instanceof Element){
					Element element = (Element) node ;
					
					intiManuallyPublishUserRole( element ) ;
					
					intiManuallyPublishBOMUserRole( element );
					
					intiManuallyPublishUserGroup( element ) ;
					
					intiManuallyPublishBOMUserGroup( element );
					
					intiManuallyPublishPartState( element ) ;
					
					intiManuallyPublishBOMState( element ) ;
					
					intiManuallyPublishPartView( element ) ;
				}
			}
		}
	}
	
	private static void intiManuallyPublishUserRole(Element element){
		manuallyPublishUserRole = new ArrayList<String>();
		
		NodeList childList = element.getElementsByTagName(IntegrationConstant.NODE_NAME_MANUALLY_PUBLISH_USER_ROLE) ;
		
		if( childList != null ){
			int size = childList.getLength() ;
			for( int i = 0 ; i < size ; i++ ){
				Node node = childList.item(i) ;
				if( node != null && node instanceof Element){
					Element childElement = (Element) node ;
					
					intiManuallyPublishList( childElement , manuallyPublishUserRole ) ;
				}
			}
		}
		
	}
	
	private static void intiManuallyPublishBOMUserRole(Element element){
		manuallyPublishBOMUserRole = new ArrayList<String>();
		
		NodeList childList = element.getElementsByTagName(IntegrationConstant.NODE_NAME_MANUALLY_PUBLISH_BOM_USER_ROLE) ;
		
		if( childList != null ){
			int size = childList.getLength() ;
			for( int i = 0 ; i < size ; i++ ){
				Node node = childList.item(i) ;
				if( node != null && node instanceof Element){
					Element childElement = (Element) node ;
					
					intiManuallyPublishList( childElement , manuallyPublishBOMUserRole ) ;
				}
			}
		}
		
	}
	
	private static void intiManuallyPublishUserGroup(Element element){
		manuallyPublishUserGroup = new ArrayList<String>();
		
		NodeList childList = element.getElementsByTagName(IntegrationConstant.NODE_NAME_MANUALLY_PUBLISH_USER_GROUP) ;
	
		if( childList != null ){
			int size = childList.getLength() ;
			for( int i = 0 ; i < size ; i++ ){
				Node node = childList.item(i) ;
				if( node != null && node instanceof Element){
					Element childElement = (Element) node ;
					
					intiManuallyPublishList( childElement , manuallyPublishUserGroup ) ;
				}
			}
		}
		
	}
	
	private static void intiManuallyPublishBOMUserGroup(Element element){
		manuallyPublishBOMUserGroup = new ArrayList<String>();
		
		NodeList childList = element.getElementsByTagName(IntegrationConstant.NODE_NAME_MANUALLY_PUBLISH_BOM_USER_GROUP) ;
	
		if( childList != null ){
			int size = childList.getLength() ;
			for( int i = 0 ; i < size ; i++ ){
				Node node = childList.item(i) ;
				if( node != null && node instanceof Element){
					Element childElement = (Element) node ;
					
					intiManuallyPublishList( childElement , manuallyPublishBOMUserGroup ) ;
				}
			}
		}
		
	}

	private static void intiManuallyPublishPartState(Element element){
		manuallyPublishPartState = new ArrayList<String>();
		
		NodeList childList = element.getElementsByTagName(IntegrationConstant.NODE_NAME_MANUALLY_PUBLISH_PART_STATE) ;
		
		if( childList != null ){
			int size = childList.getLength() ;
			for( int i = 0 ; i < size ; i++ ){
				Node node = childList.item(i) ;
				if( node != null && node instanceof Element){
					Element childElement = (Element) node ;
					
					intiManuallyPublishList( childElement , manuallyPublishPartState ) ;
				}
			}
		}
		
	}
	
	private static void intiManuallyPublishBOMState(Element element){
		manuallyPublishBOMState = new ArrayList<String>();
		
		NodeList childList = element.getElementsByTagName(IntegrationConstant.NODE_NAME_MANUALLY_PUBLISH_BOM_STATE) ;
		
		if( childList != null ){
			int size = childList.getLength() ;
			for( int i = 0 ; i < size ; i++ ){
				Node node = childList.item(i) ;
				if( node != null && node instanceof Element){
					Element childElement = (Element) node ;
					
					intiManuallyPublishList( childElement , manuallyPublishBOMState ) ;
				}
			}
		}
		
	}

	private static void intiManuallyPublishPartView(Element element){
		manuallyPublishPartView = new ArrayList<String>();
		
		NodeList childList = element.getElementsByTagName(IntegrationConstant.NODE_NAME_MANUALLY_PUBLISH_PART_VIEW) ;
		
		if( childList != null ){
			int size = childList.getLength() ;
			for( int i = 0 ; i < size ; i++ ){
				Node node = childList.item(i) ;
				if( node != null && node instanceof Element){
					Element childElement = (Element) node ;
					
					intiManuallyPublishList( childElement , manuallyPublishPartView ) ;
				}
			}
		}
	}
	
	private static void intiManuallyPublishList(Element element , List<String> list){
		NodeList childList = element.getChildNodes() ;
		
		if( childList != null ){
			int size = childList.getLength() ;
			
			for( int i = 0 ; i < size ; i++ ){
				Node node = childList.item(i) ;
				if( node != null && node instanceof Element){
					Element childElement = (Element) node ;
					
					String UserRoleName = childElement.getAttribute(IntegrationConstant.NODE_NAME_MANUALLY_PUBLISH_NAME);
					
					if( UserRoleName != null ){
						UserRoleName = UserRoleName.trim() ;
						
						if( ! UserRoleName.equals("") ){
							list.add(UserRoleName) ;
						}
					}
				}
			}
		}
	}
	
	private static void initStartBackupData( Document document ){
		NodeList nodeList = document.getElementsByTagName( IntegrationConstant.NODE_NAME_DATA_BACKUP );
		
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			
			if( size > 0 ){
				Node node = nodeList.item( 0 ) ;
				
				if( node != null && node instanceof Element ){
					Element element = (Element) node ;
					
					String backupDataStart = element.getAttribute( IntegrationConstant.NODE_NAME_DATA_BACKUP_ATT );
					
					if( backupDataStart != null ){
						backupDataStart = backupDataStart.trim() ;
						
						if( backupDataStart.equalsIgnoreCase( IntegrationConstant.TRUE_VALUE ) ){
							startBackupData = true ;
						}else{
							startBackupData = false ;
						}
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		BusinessRuleXMLConfigUtil.getInstance().getNoReleasePartSoftTypeList() ;
		BusinessRuleXMLConfigUtil.getInstance().isReleaseAlternateLink();
		BusinessRuleXMLConfigUtil.getInstance().getReadPriceUserGroup();
		BusinessRuleXMLConfigUtil.getInstance().isStartBackupData();
	}
}
