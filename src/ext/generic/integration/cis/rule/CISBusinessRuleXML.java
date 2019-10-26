package ext.generic.integration.cis.rule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import ext.generic.integration.cis.constant.CISConstant;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.util.WTException;
import wt.util.WTProperties;

public class CISBusinessRuleXML implements RemoteAccess {

	private static final String CLASSNAME = CISBusinessRuleXML.class.getName();
	private static final Logger logger = LogR.getLogger(CLASSNAME);
	private static final String BUSSINESSRULE_XML_FILENAME = "CISBusinessRule.xml";
	private static String BUSSINESSRULE_XML_FILEPATH = "";
	static{
		try {
			WTProperties wtProperties = WTProperties.getLocalProperties();
			String codebasePath = wtProperties.getProperty("wt.codebase.location");
			BUSSINESSRULE_XML_FILEPATH = codebasePath + File.separator + "ext" +File.separator+"" +
					"generic" + File.separator + "integration" + File.separator + "cis" +
							""+File.separator+"config"+File.separator;
			logger.debug("cis-xml-path="+BUSSINESSRULE_XML_FILEPATH);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	//需要发布至中间表的文档类型
	private static List<String> releaseWTDocumentSoftTypeList=null;
	private String mesRelaseStatusKey;
	private List<String> manuallyPublishUserRole = null ;
	private List<String> manuallyPublishUserGroup = null ;
	private List<String> manuallyPublishState = null ;
	private String library=null;
	private String cls=null;
	private String symbol=null;
	private String footprint=null;
	private String datasheet=null;
	private String filePath=null;
	private String fileType=null;
	private String dataBaseName=null;
	private String isMultiTable=null;
	private String logicId=null;
	private String symbolpath=null;
	private String footprintpath=null;
	private String datasheetpath=null;
	private static Map<Long, CISBusinessRuleXML> configMap = new HashMap<Long, CISBusinessRuleXML>();
	
	/**
	 * 获取实例
	 * @return
	 * @throws WTException 
	 */
	public static CISBusinessRuleXML getInstance() throws WTException{
		CISBusinessRuleXML instance = null;
		String configFile = BUSSINESSRULE_XML_FILEPATH + BUSSINESSRULE_XML_FILENAME;
		File file = new File(configFile);
		long t = file.lastModified();
		if(configMap.containsKey(t)){
			instance = configMap.get(t);
		}else{
			logger.info("cis配置文件被修改，重新加载");
			instance = new CISBusinessRuleXML();
			configMap.put(t, instance);
		}
		return instance;
	}
	
	private CISBusinessRuleXML() throws WTException{
		String configFile = BUSSINESSRULE_XML_FILEPATH + BUSSINESSRULE_XML_FILENAME;
		logger.trace("mes配置文件路径="+configFile);
		parse(configFile);
	}
	/**
	 * 解析配置文件
	 * @param configFile
	 * @throws WTException 
	 */
	private void parse(String configFile) throws WTException {
		try {
			//获取XML的DOM对象
			DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = fac.newDocumentBuilder();
			Document document = documentBuilder.parse( configFile );
			
			if(document == null){
				logger.error("Can't Parse Config File : " + configFile);
				return ;
			}else{
				initManuallyPublish(document);
				initMESReleaseStatus(document);
				initLibrary(document);
				initCls(document);
				initDatasheet(document);
				initFootprint(document);
				initSymbol(document);
				initFileType(document);
				initFilePath(document);
				initDataBase(document);
				initIsMultiTable(document);
				initLogicId(document);
				initDatasheetPath(document);
				initFootprintPath(document);
				initSymbolPath(document);
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new WTException(e);
		} catch (SAXException e) {
			e.printStackTrace();
			throw new WTException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WTException(e);
		}
	}

	private void initDatasheetPath(Document document) {
		NodeList nodeList = document.getElementsByTagName( CISConstant.NODE_NAME_DATASHEET_PATH );
		datasheetpath = "";
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			if( size > 0 ){
				Node node = nodeList.item( 0 );
				if( node != null && node instanceof Element ){
					Element element = ( Element ) node ;
					
					datasheetpath = element.getAttribute( CISConstant.ATTR_NAME_DPATH ) ;
					
					if(StringUtils.isBlank(datasheetpath)){
						
						datasheetpath = "" ;
					}
				}
			}
		}
		
	}

	private void initFootprintPath(Document document) {
		NodeList nodeList = document.getElementsByTagName( CISConstant.NODE_NAME_FOOTPRINT_PATH );
		footprintpath = "";
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			if( size > 0 ){
				Node node = nodeList.item( 0 );
				if( node != null && node instanceof Element ){
					Element element = ( Element ) node ;
					
					footprintpath = element.getAttribute( CISConstant.ATTR_NAME_FPATH ) ;
					
					if(StringUtils.isBlank(footprintpath)){
						
						footprintpath = "" ;
					}
				}
			}
		}
		
	}

	private void initSymbolPath(Document document) {
		NodeList nodeList = document.getElementsByTagName( CISConstant.NODE_NAME_SYMBOL_PATH );
		symbolpath = "";
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			if( size > 0 ){
				Node node = nodeList.item( 0 );
				if( node != null && node instanceof Element ){
					Element element = ( Element ) node ;
					
					symbolpath = element.getAttribute( CISConstant.ATTR_NAME_SPATH ) ;
					
					if(StringUtils.isBlank(symbolpath)){
						
						symbolpath = "" ;
					}
				}
			}
		}
		
	}

	private void initLogicId(Document document) {
		NodeList nodeList = document.getElementsByTagName( CISConstant.NODE_NAME_CLS_INTERNAME );
		logicId = "";
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			if( size > 0 ){
				Node node = nodeList.item( 0 );
				if( node != null && node instanceof Element ){
					Element element = ( Element ) node ;
					
					logicId = element.getAttribute( CISConstant.ATTR_NAME_LOGICID ) ;
					
					if(StringUtils.isBlank(logicId)){
						
						logicId = "" ;
					}
				}
			}
		}
	}

	private void initIsMultiTable(Document document) {
		NodeList nodeList = document.getElementsByTagName( CISConstant.NODE_NAME_MULTI_TABLE );
		isMultiTable = "";
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			if( size > 0 ){
				Node node = nodeList.item( 0 );
				if( node != null && node instanceof Element ){
					Element element = ( Element ) node ;
					
					isMultiTable = element.getAttribute( CISConstant.ATTR_NAME_ISMULTITABLE ) ;
					
					if(StringUtils.isBlank(isMultiTable)){
						
						isMultiTable = "" ;
					}
				}
			}
		}
	}

	private void initDataBase(Document document) {
		NodeList nodeList = document.getElementsByTagName( CISConstant.NODE_NAME_DATABASE );
		dataBaseName = "";
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			if( size > 0 ){
				Node node = nodeList.item( 0 );
				if( node != null && node instanceof Element ){
					Element element = ( Element ) node ;
					
					dataBaseName = element.getAttribute( CISConstant.ATTR_NAME_DATABASENAME ) ;
					
					if(StringUtils.isBlank(dataBaseName)){
						
						dataBaseName = "" ;
					}
				}
			}
		}
	}

	private void initFilePath(Document document) {
		NodeList nodeList = document.getElementsByTagName( CISConstant.NODE_NAME_FILEADDRESS_PATH );
		filePath = "";
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			if( size > 0 ){
				Node node = nodeList.item( 0 );
				if( node != null && node instanceof Element ){
					Element element = ( Element ) node ;
					
					filePath = element.getAttribute( CISConstant.ATTR_NAME_FILEADDRESS_PATH ) ;
					
					if(StringUtils.isBlank(filePath)){
						
						filePath = "" ;
					}
				}
			}
		}
	}

	private void initFileType(Document document) {
		NodeList nodeList = document.getElementsByTagName( CISConstant.NODE_NAME_FILETYPE );
		fileType = "";
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			if( size > 0 ){
				Node node = nodeList.item( 0 );
				if( node != null && node instanceof Element ){
					Element element = ( Element ) node ;
					
					fileType = element.getAttribute( CISConstant.ATTR_NAME_FILETYPE ) ;
					
					if(StringUtils.isBlank(fileType)){
						
						fileType = "" ;
					}
				}
			}
		}
	}

	private void initDatasheet(Document document) {
		NodeList nodeList = document.getElementsByTagName( CISConstant.NODE_NAME_DATASHEET_SOFTTYPE );
		datasheet = "";
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			if( size > 0 ){
				Node node = nodeList.item( 0 );
				if( node != null && node instanceof Element ){
					Element element = ( Element ) node ;
					
					datasheet = element.getAttribute( CISConstant.ATTR_NAME_DATASHEETTYPE ) ;
					
					if(StringUtils.isBlank(datasheet)){
						
						datasheet = "" ;
					}
				}
			}
		}
	}

	private void initFootprint(Document document) {
		NodeList nodeList = document.getElementsByTagName( CISConstant.NODE_NAME_FOOTPRINT_SOFTTYPE );
		footprint = "";
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			if( size > 0 ){
				Node node = nodeList.item( 0 );
				if( node != null && node instanceof Element ){
					Element element = ( Element ) node ;
					
					footprint = element.getAttribute( CISConstant.ATTR_NAME_FOOTPRINTTYPE ) ;
					
					if(StringUtils.isBlank(footprint)){
						
						footprint = "" ;
					}
				}
			}
		}
	}

	private void initSymbol(Document document) {
		NodeList nodeList = document.getElementsByTagName( CISConstant.NODE_NAME_SYMBOL_SOFTTYPE );
		symbol = "";
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			if( size > 0 ){
				Node node = nodeList.item( 0 );
				if( node != null && node instanceof Element ){
					Element element = ( Element ) node ;
					
					symbol = element.getAttribute( CISConstant.ATTR_NAME_SYMBOLTYPE ) ;
					
					if(StringUtils.isBlank(symbol)){
						
						symbol = "" ;
					}
				}
			}
		}
	}

	private void initCls(Document document) {
		NodeList nodeList = document.getElementsByTagName( CISConstant.NODE_NAME_CIS_RELEASE_RULE );
		cls = "";
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			if( size > 0 ){
				Node node = nodeList.item( 0 );
				if( node != null && node instanceof Element ){
					Element element = ( Element ) node ;
					
					cls = element.getAttribute( CISConstant.ATTR_NAME_CLSDISPLAY ) ;
					
					if(StringUtils.isBlank(cls)){
						
						cls = "" ;
					}
				}
			}
		}
	}

	private void initLibrary(Document document) {
		NodeList nodeList = document.getElementsByTagName( CISConstant.NODE_NAME_CIS_LIBRARY );
		library = "";
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			if( size > 0 ){
				Node node = nodeList.item( 0 );
				if( node != null && node instanceof Element ){
					Element element = ( Element ) node ;
					
					library = element.getAttribute( CISConstant.ATTR_NAME_NAME ) ;
					
					if(StringUtils.isBlank(library)){
						
						library = "" ;
					}
				}
			}
		}
	}

	/**
	 * 手工按钮的初始化
	 * @param document
	 */
	private void initManuallyPublish(Document document) {
		NodeList nodeList = document.getElementsByTagName(CISConstant.NODE_NAME_MANUALLY_PUBLISH);
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			logger.debug("manually-publish.node.size="+size);
			for( int i = 0 ; i < size ; i++ ){
				Node node = nodeList.item( i );
				if( node != null && node instanceof Element){
					Element element = (Element) node ;
					intiManuallyPublishUserRole(element);
					intiManuallyPublishUserGroup(element);
					intiManuallyPublishState(element);
				}
			}
		}
	}
	/**
	 * 初始化角色
	 * @param element
	 */
	private void intiManuallyPublishUserRole(Element element){
		manuallyPublishUserRole = new ArrayList<String>();
		NodeList childList = element.getElementsByTagName(CISConstant.NODE_NAME_MANUALLY_PUBLISH_USER_ROLE) ;
		if( childList != null ){
			int size = childList.getLength() ;
			logger.debug("manually-publish-->user-role.node.size="+size);
			for( int i = 0 ; i < size ; i++ ){
				Node node = childList.item(i) ;
				if( node != null && node instanceof Element){
					Element childElement = (Element) node ;
					intiManuallyPublishList( childElement , manuallyPublishUserRole ,CISConstant.ATTR_NAME_MANUALLY_PUBLISH_NAME) ;
				}
			}
		}
		
	}
	/**
	 * 取得子节点集合
	 * @param element
	 * @param list
	 * @param attrName
	 */
	private void intiManuallyPublishList(Element element , List<String> list, String attrName){
		NodeList childList = element.getChildNodes() ;
		
		if( childList != null ){
			int size = childList.getLength() ;
			
			for( int i = 0 ; i < size ; i++ ){
				Node node = childList.item(i) ;
				if( node != null && node instanceof Element){
					Element childElement = (Element) node ;
					String UserRoleName = childElement.getAttribute(attrName);
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
	/**
	 * 初始化组
	 * @param element
	 */
	private void intiManuallyPublishUserGroup(Element element){
		manuallyPublishUserGroup = new ArrayList<String>();
		
		NodeList childList = element.getElementsByTagName(CISConstant.NODE_NAME_MANUALLY_PUBLISH_USER_GROUP) ;
	
		if( childList != null ){
			int size = childList.getLength() ;
			logger.debug("manually-publish-->user-group.node.size="+size);
			for( int i = 0 ; i < size ; i++ ){
				Node node = childList.item(i) ;
				if( node != null && node instanceof Element){
					Element childElement = (Element) node ;
					
					intiManuallyPublishList( childElement , manuallyPublishUserGroup,CISConstant.ATTR_NAME_MANUALLY_PUBLISH_NAME ) ;
				}
			}
		}
		
	}
	/**
	 * 初始化状态
	 * @param element
	 */
	private void intiManuallyPublishState(Element element){
		manuallyPublishState = new ArrayList<String>();
		
		NodeList childList = element.getElementsByTagName(CISConstant.NODE_NAME_MANUALLY_PUBLISH_EPMDOC_STATE) ;
		
		if( childList != null ){
			int size = childList.getLength() ;
			for( int i = 0 ; i < size ; i++ ){
				Node node = childList.item(i) ;
				if( node != null && node instanceof Element){
					Element childElement = (Element) node ;
					intiManuallyPublishList( childElement , manuallyPublishState,CISConstant.ATTR_NAME_MANUALLY_PUBLISH_NAME ) ;
				}
			}
		}
		
	}
	/**
	 * 读取MES发布状态
	 * @param document
	 */
	private void initMESReleaseStatus( Document document ){
		NodeList nodeList = document.getElementsByTagName( CISConstant.NODE_NAME_MES_RELEASE_STATUS );
		mesRelaseStatusKey = "";
		if( nodeList != null ){
			int size = nodeList.getLength() ;
			if( size > 0 ){
				Node node = nodeList.item( 0 );
				if( node != null && node instanceof Element ){
					Element element = ( Element ) node ;
					
					mesRelaseStatusKey = element.getAttribute( CISConstant.ATTR_NAME_LOGIC_ID ) ;
					
					if(StringUtils.isBlank(mesRelaseStatusKey)){
						
						mesRelaseStatusKey = "" ;
					}
				}
			}
		}
		
	}
	

	public String getMesRelaseStatusKey() {
		return mesRelaseStatusKey;
	}

	public List<String> getManuallyPublishUserRole() {
		return manuallyPublishUserRole;
	}

	public List<String> getManuallyPublishUserGroup() {
		return manuallyPublishUserGroup;
	}

	public List<String> getManuallyPublishState() {
		return manuallyPublishState;
	}

	
	public String getLibrary() {
		return library;
	}

	public void setLibrary(String library) {
		this.library = library;
	}

	public String getCls() {
		return cls;
	}

	public void setCls(String cls) {
		this.cls = cls;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getFootprint() {
		return footprint;
	}

	public void setFootprint(String footprint) {
		this.footprint = footprint;
	}

	public String getDatasheet() {
		return datasheet;
	}

	public void setDatasheet(String datasheet) {
		this.datasheet = datasheet;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getDataBaseName() {
		return dataBaseName;
	}

	public void setDataBaseName(String dataBaseName) {
		this.dataBaseName = dataBaseName;
	}

	public String getIsMultiTable() {
		return isMultiTable;
	}

	public void setIsMultiTable(String isMultiTable) {
		this.isMultiTable = isMultiTable;
	}

	public String getLogicId() {
		return logicId;
	}

	public void setLogicId(String logicId) {
		this.logicId = logicId;
	}

	public String getSymbolpath() {
		return symbolpath;
	}

	public void setSymbolpath(String symbolpath) {
		this.symbolpath = symbolpath;
	}

	public String getFootprintpath() {
		return footprintpath;
	}

	public void setFootprintpath(String footprintpath) {
		this.footprintpath = footprintpath;
	}

	public String getDatasheetpath() {
		return datasheetpath;
	}

	public void setDatasheetpath(String datasheetpath) {
		this.datasheetpath = datasheetpath;
	}

	public static void main(String[] args) {
		try {
			CISBusinessRuleXML mes = new CISBusinessRuleXML();
			System.out.println(" "+mes.getMesRelaseStatusKey());
			System.out.println(" "+mes.getManuallyPublishUserGroup());
			System.out.println(" "+mes.getManuallyPublishUserRole());
			System.out.println(" "+mes.getManuallyPublishState());
			System.out.println(" "+mes.getCls());
			System.out.println(" "+mes.getLibrary());
			System.out.println(" "+mes.getSymbol());
			System.out.println(" "+mes.getFootprint());
			System.out.println(" "+mes.getDatasheet());
			System.out.println(" "+mes.getFilePath());
			System.out.println(" "+mes.getFileType());
			System.out.println(" "+mes.getDataBaseName());
		} catch (WTException e) {
			e.printStackTrace();
		}
		
	}

}
