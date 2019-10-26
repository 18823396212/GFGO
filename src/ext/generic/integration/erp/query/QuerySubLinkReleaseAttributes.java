package ext.generic.integration.erp.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import wt.log4j.LogR;
import ext.generic.integration.erp.bean.GenericIEBean;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.generic.integration.erp.util.QueryXMLConfigUtil;

/**
 * 查询中间表替代料信息
 * 
 * @author Kwang
 */
public class QuerySubLinkReleaseAttributes extends QueryIETask {
	
	private static final Logger logger = LogR.getLogger(QuerySubLinkReleaseAttributes.class.getName() );
	
	private static final String CONFIG_XML_FILE = "QuerySubLinkReleaseAttributes.xml" ;
	
	private static String table = "" ;
	
	public QuerySubLinkReleaseAttributes(){
		super();
		table = super.getTable() ;
	}
	
	@Override
	protected String setConfigXMLFile() {
		return CONFIG_XML_FILE ;
	}

	/***
	 * 根据编号查询中间表BOM数据
	 * @param number BOM主件编号
	 * @return
	 */
	public static List<GenericIEBean> querySubLink( String number ){
		logger.debug("Entering In QueryBOMReleaseAttributes.querySubLink( String number )......") ;
		
		List<GenericIEBean> list = new ArrayList<GenericIEBean>();
		
		/**
		 * 根据零部件编号进行查询
		 * 
		 */
		String where = "";
		
		if( number != null && !number.trim().isEmpty()  ){
			where = "parent_item ='*" + number + "*' " ;
		}else{
			//不存在所有搜索情况
			where = "parent_item ='" + "\"\"" + "' " ;
		}

		logger.debug( "query() ... where : " + where );
		
		QueryIETask queryIETask = new QuerySubLinkReleaseAttributes() ;
		
		list = queryIETask.query( where );
		
		logger.debug("Existed Out QueryBOMReleaseAttributes.querySubLink( String number )......") ;
		
		return list ;
	}
	
	public static String getTableName(){
		return table ;
	}
	
	/***
	 * 获取查询对象属性参数列表
	 * @return
	 */
	public static List< String > getQueryAttributes(){
		
		String url = CommonPDMUtil.getCodebasePath() + "ext/generic/integration/erp/config/QuerySubLinkReleaseAttributes.xml";
		
		QueryXMLConfigUtil config = new QueryXMLConfigUtil(url);
		
		return config.getQueryAttributes();
	}
}
