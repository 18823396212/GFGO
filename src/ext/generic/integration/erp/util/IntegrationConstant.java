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
/**
 * 用于定义集成代码中的一些常量
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-10-08
 */
public class IntegrationConstant {
	
	/** 以下常量用于日志信息控制  **/
	
	public static final String VERBOSE = "ext.generic.integration.log.verbose" ;
	
	public static final int LOG_LEVEL_NORMAL = 0 ;
	public static final int LOG_LEVEL_INFO = 1 ;
	public static final int LOG_LEVEL_WARN = 2 ;
	public static final int LOG_LEVEL_ERROR = 3 ;
	
	/**			End			**/
	
	
	/********** 定义物料和BOM的发布状态 *********/
	
	//PDM发布数据成功
	public static final String PDM_RELEASE_STATUS = "N" ;
	
	public static final String PDM_RELEASE_STATUS_CN = "PDM发布成功" ;
	
	//ERP接收数据成功
	public static final String ERP_PROCESS_SUCCESS = "Y" ;

	public static final String ERP_PROCESS_SUCCESS_CN = "ERP接收成功" ;
	
	//ERP接收数据失败
	public static final String ERP_PROCESS_FAILED = "E" ;
	
	public static final String ERP_PROCESS_FAILED_CN = "ERP接收失败" ;
	
	//PDM反写信息成功
	public static final String PDM_WRITE_BACK = "S" ;

	/******************** End *******************/
	
	
	/** 以下常量为ERPCustomized.properties文件的Key值  **/

	public static final String LOG_LEVEL = "log_level" ;

//	public static final String TASK_DIRECTORY = "task_file_directory" ;
	
	public static final String ERP_CONFIG_FILE_PATH = "erp_config_file_path" ;

	public static final String PART_QUERY_IE_XML_FILE = "part_query_ie_xml_file" ;
	public static final String PART_CREATE_IE_XML_FILE = "part_create_ie_xml_file" ;
	public static final String PART_UPDATE_IE_XML_FILE = "part_update_ie_xml_file" ;
	public static final String PART_DELETE_IE_XML_FILE = "part_delete_ie_xml_file" ;
	
	public static final String PART_BACKUP_IE_XML_FILE = "part_bakcup_ie_xml_file" ;
	public static final String PART_DELETE_WITH_STATUS_IE_XML_FILE = "part_delete_with_status_ie_xml_file" ;
	
	public static final String BOM_INFO_QUERY_IE_XML_FILE = "bom_info_query_ie_xml_file" ;
	public static final String BOM_INFO_CREATE_IE_XML_FILE = "bom_info_create_ie_xml_file" ;
	public static final String BOM_INFO_UPDATE_IE_XML_FILE = "bom_info_update_ie_xml_file" ;
	public static final String BOM_INFO_DELETE_IE_XML_FILE = "bom_info_delete_ie_xml_file" ;
	
	public static final String BOM_STRUCTRUE_QUERY_IE_XML_FILE = "bom_structure_query_ie_xml_file" ;
	public static final String BOM_STRUCTRUE_CREATE_IE_XML_FILE = "bom_structure_create_ie_xml_file" ;
	public static final String BOM_STRUCTURE_UPDATE_IE_XML_FILE = "bom_structure_update_ie_xml_file" ;
	public static final String BOM_STRUCTURE_DELETE_IE_XML_FILE = "bom_structure_delete_ie_xml_file" ;
	
	public static final String BOM_STRUCTURE_BACKUP_IE_XML_FILE = "bom_structure_bakcup_ie_xml_file" ;
	public static final String BOM_STRUCTURE_DELETE_WITH_STATUS_IE_XML_FILE = "bom_structure_delete_with_status_ie_xml_file" ;
	
	
	
	
	public static final String BOM_STRUCTURE_REMOVE_BY_PARENT_IE_XML_FILE = "bom_structure_remove_by_parent_ie_xml_file" ;

	public static final String ECN_QUERY_IE_XML_FILE = "ecn_query_ie_xml_file" ;
	public static final String ECN_CREATE_IE_XML_FILE = "ecn_create_ie_xml_file" ;
	public static final String ECN_UPDATE_IE_XML_FILE = "ecn_update_ie_xml_file" ;
	public static final String ECN_DELETE_IE_XML_FILE = "ecn_delete_ie_xml_file" ;

	public static final String REPLACE_PART_QUERY_IE_XML_FILE = "replace_part_query_ie_xml_file" ;
	public static final String REPLACE_PART_CREATE_IE_XML_FILE = "replace_part_create_ie_xml_file" ;
	public static final String REPLACE_PART_UPDATE_IE_XML_FILE = "replace_part_update_ie_xml_file" ;
	public static final String REPLACE_PART_DELETE_IE_XML_FILE = "replace_part_delete_ie_xml_file" ;
	
	public static final String SUPPLIER_INFO_QUERY_IE_XML_FILE = "supplier_info_query_ie_xml_file" ;
	
	public static final String GENERIC_OBJECT_QUERY_IE_XML_FILE = "generic_object_query_ie_xml_file" ;
	
	public static final String UPDATE_WRITE_BACK_STATUS_IE_XML_FILE = "update_write_back_status_ie_xml_file" ;	
	
	/**			End			**/
	
	
	/** 以下常量用于IEXMLUtil解析Task XML文件  **/
	
	protected static final String FIELD_NAME_STR = "<ie:param name=\"FIELD\"" ;
	
	protected static final String ANNOTATION_STR = "<!--" ;
	
	protected static final String KEY_START_STR = "data=\"" ;
	protected static final String KEY_END_STR_1 = "='$" ;
	protected static final String KEY_END_STR_2 = "=N'$" ;
	
	protected static final String VALUE_START_STR = "@FORM[]" ;
	protected static final String VALUE_END_STR = "[]}'" ;
	
	protected static int keyStartStrLen = KEY_START_STR.length() ;
	protected static int valueStartStrLen = VALUE_START_STR.length() ;
	
	/**			End			**/
	
	
	
	/** 以下常量用于QueryXMLConfigUtil解析XML文件  **/
	
	protected static final String NODE_NAME_IEADAPTER = "ieAdapter" ;
	protected static final String NODE_NAME_IEADAPTER_ATT = "name" ;
	
	protected static final String NODE_NAME_TABLE = "table" ;
	protected static final String NODE_NAME_TABLE_ATT = "name" ;
	
	protected static final String NODE_NAME_ATTRIBUTE = "attribute" ;
	
	protected static final String NODE_NAME_COLUMN = "column" ;
	
	protected static final String NODE_NAME_ATT_DB = "dbcolumn" ;
	protected static final String NODE_NAME_ATT_PDM = "pdmLogicId" ;
	
	protected static final String NODE_NAME_WHERE = "dbcolumn" ;
	
	/**			End			**/
	
	
	/** 以下常量用于BusinessRuleXMLConfigUtil解析XML文件  **/
	
	protected static final String NODE_NAME_WTPART = "wtpart" ;
	protected static final String NODE_NAME_WTPART_ATT = "name" ;
	
	protected static final String NODE_NAME_WTPART_CLAFFICATION = "ClassNodeInternalName" ;
	
	protected static final String NODE_NAME_WTPART_IBA_LOGICID = "smallTypeIba" ;
	
	protected static final String NODE_NAME_WTPART_IBA_VALUE = "smallType" ;
	
	protected static final String NODE_NAME_PART_RELEASE_STATUS = "part-release-status" ;
	
	protected static final String NODE_NAME_BOM_RELEASE_STATUS = "bom-release-status" ;
	
	protected static final String NODE_NAME_LOGIC_ID = "logicid" ;
	
	protected static final String NODE_NAME_ALTERNATE_LINKE = "alternateLink" ;
	protected static final String NODE_NAME_ALTERNATE_LINKE_ATT = "release";
	
	protected static final String NODE_NAME_WTPART_PRICE = "price" ;
	protected static final String NODE_NAME_WTPART_PRICE_ATT = "name" ;
	
	protected static final String NODE_NAME_MANUALLY_PUBLISH_ATT = "manually-publish" ;
	protected static final String NODE_NAME_MANUALLY_PUBLISH_NAME = "name" ;
	
	protected static final String NODE_NAME_MANUALLY_PUBLISH_USER_ROLE = "user-role" ;
	protected static final String NODE_NAME_MANUALLY_PUBLISH_BOM_USER_ROLE = "bom-user-role" ;
	protected static final String NODE_NAME_MANUALLY_PUBLISH_ROLE = "role" ;
	
	protected static final String NODE_NAME_MANUALLY_PUBLISH_USER_GROUP = "user-group" ;
	protected static final String NODE_NAME_MANUALLY_PUBLISH_BOM_USER_GROUP = "bom-user-group" ;
	protected static final String NODE_NAME_MANUALLY_PUBLISH_GROUP = "group" ;
	
	protected static final String NODE_NAME_MANUALLY_PUBLISH_PART_STATE = "part-state" ;
	protected static final String NODE_NAME_MANUALLY_PUBLISH_BOM_STATE = "bom-state" ;
	protected static final String NODE_NAME_MANUALLY_PUBLISH_STATE = "state" ;
	
	protected static final String NODE_NAME_MANUALLY_PUBLISH_PART_VIEW = "part-view" ;
	protected static final String NODE_NAME_MANUALLY_PUBLISH_VIEW = "view" ;
	
	protected static final String NODE_NAME_DATA_BACKUP = "backup" ;
	protected static final String NODE_NAME_DATA_BACKUP_ATT = "start";
	
	protected static final String NODE_NAME_PART_PHANTOM = "part-phantom" ;
	protected static final String NODE_NAME_PART_HIDEPARTINSTRUCTURE = "part-hidePartInStructure" ;
	/**			End			**/
	
	
	/*** Info*E查询数据库时，不能识别作为条件值得空字符串(""),
	 * 例如： where number="" ;
	 * 
	 * 因此将这些值发布到中间表时，设置为"noValue"
	****/
	public static final String NO_VALUE = "noValue" ;
	
	
	/***************	布尔值定义	****************/
	public static final String TRUE_VALUE = "true" ;
	public static final String FALSE_VALUE = "false" ;
}
