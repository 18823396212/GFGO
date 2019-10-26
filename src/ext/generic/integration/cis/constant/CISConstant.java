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
package ext.generic.integration.cis.constant;

public class CISConstant {
	/* 发送文件类型 */
	public static final String PRIMARY = "primary";//主内容
	public static final String SECONDARY = "secondary";//附件
	public static final String ALL = "all";//全部
	
	public static final String FOLDER_SEPARATER = "_";
	
	/*数据库类型*/
	public static final String ORACLE="Oracle";
	public static final String SQLSERVER="SQLServer";
	
	/*主内容的后缀名*/
	public static final String SCHLIB=".SCHLIB";
	public static final String PCBLIB=".PCBLIB";
	
	/* CISBusinessRule.xml 配置文件节点的node */
	public static final String NODE_NAME_MANUALLY_PUBLISH = "manually-publish" ;
	public static final String ATTR_NAME_MANUALLY_PUBLISH_NAME = "name" ;
	public static final String NODE_NAME_MANUALLY_PUBLISH_USER_ROLE = "user-role" ;
	public static final String NODE_NAME_MANUALLY_PUBLISH_ROLE = "role" ;
	public static final String NODE_NAME_MANUALLY_PUBLISH_USER_GROUP = "user-group" ;
	public static final String NODE_NAME_MANUALLY_PUBLISH_GROUP = "group" ;
	public static final String NODE_NAME_MANUALLY_PUBLISH_EPMDOC_STATE = "part-state" ;
	public static final String NODE_NAME_MANUALLY_PUBLISH_STATE = "state" ;
	
	public static final String NODE_NAME_MES_RELEASE_STATUS = "mes-release-status" ;
	public static final String ATTR_NAME_LOGIC_ID = "logicid" ;
	
	
	public static final String NODE_NAME_FILEADDRESS_TYPE = "fileaddress-type" ;
	public static final String ATTR_NAME_FILEADDRESS_TYPE = "type" ;
	
	public static final String NODE_NAME_CIS_LIBRARY = "cis-library" ;
	public static final String ATTR_NAME_NAME = "libraryname" ;
	
	public static final String NODE_NAME_CIS_RELEASE_RULE = "cis-release-rule" ;
	public static final String ATTR_NAME_CLSDISPLAY = "clsDisplay" ;
	
	public static final String NODE_NAME_FILEADDRESS_PATH = "fileaddress-path" ;
	public static final String ATTR_NAME_FILEADDRESS_PATH = "path" ;
	
	public static final String NODE_NAME_FILETYPE = "fileType" ;
	public static final String ATTR_NAME_FILETYPE = "type" ;
	
	public static final String NODE_NAME_DATABASE = "database" ;
	public static final String ATTR_NAME_DATABASENAME = "dataBaseName" ;
	
	public static final String NODE_NAME_MULTI_TABLE = "multi-Table" ;
	public static final String ATTR_NAME_ISMULTITABLE = "isMultiTable" ;
	
	public static final String NODE_NAME_CLS_INTERNAME = "cls-interName" ;
	public static final String ATTR_NAME_LOGICID = "logicId" ;
	
	public static final String NODE_NAME_SYMBOL_SOFTTYPE = "symbol-softtype-type" ;
	public static final String ATTR_NAME_SYMBOLTYPE = "symbolType" ;
	
	public static final String NODE_NAME_FOOTPRINT_SOFTTYPE = "footprint-softtype-type" ;
	public static final String ATTR_NAME_FOOTPRINTTYPE = "footprintType" ;
	
	public static final String NODE_NAME_DATASHEET_SOFTTYPE = "datasheet-softtype-type" ;
	public static final String ATTR_NAME_DATASHEETTYPE = "datasheetType" ;
	
	public static final String NODE_NAME_SYMBOL_PATH = "symbol-path" ;
	public static final String ATTR_NAME_SPATH = "spath" ;
	
	public static final String NODE_NAME_FOOTPRINT_PATH = "footprint-path" ;
	public static final String ATTR_NAME_FPATH = "fpath" ;
	
	public static final String NODE_NAME_DATASHEET_PATH = "datasheet-path" ;
	public static final String ATTR_NAME_DPATH = "dpath" ;
	
	/*IBA属性*/
	public static final String WLPART_DESCRIPTION_ZH="47_ChineseDescription";//中文描述
	public static final String WLPART_DESCRIPTION_EN="48_EnglishDescription";//英文描述
	public static final String CLASSIFICATION="Classification"; //分类
	public static final String LIBRARYREF="LibraryRef";	
	public static final String LIBRARYPATH="LibraryPath";	
	public static final String FOOTPRINTREF="FootprintRef";	
	public static final String FOOTPRINTPATH="FootprintPath";	
	public static final String FOOTPRINTREF2="LibraryRef";	
	public static final String FOOTPRINTPATH2="FootprintPath2";	
	public static final String FOOTPRINTREF3="LibraryRef";	
	public static final String FOOTPRINTPATH3="FootprintPath3";	
	public static final String PINCOUNT="WLPART_PINCOUNT";	
	public static final String MANUFACTURER="WLPART_MANUFACTURER";	
	
	
}
