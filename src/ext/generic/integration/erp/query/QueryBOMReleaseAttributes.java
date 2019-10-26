package ext.generic.integration.erp.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import wt.log4j.LogR;
import ext.generic.integration.erp.bean.GenericIEBean;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.generic.integration.erp.util.QueryXMLConfigUtil;

/**
 * 查询中间表BOM结构
 * 
 * @author Kwang
 */
public class QueryBOMReleaseAttributes extends QueryIETask {
	
	private static final Logger logger = LogR.getLogger(QueryBOMReleaseAttributes.class.getName() );
	
	public static final String CONFIG_XML_FILE = "QueryBOMReleaseAttributes.xml" ;
	
	private static String table = "" ;
	
	public QueryBOMReleaseAttributes(){
		super();
		table = super.getTable() ;
	}
	
	@Override
	protected String setConfigXMLFile() {
		return CONFIG_XML_FILE ;
	}

	/***
	 * 根据编号查询中间表BOM数据
	 * @param number
	 * @return
	 */
	public static List<GenericIEBean> queryBOM( String number ){
		logger.debug("Entering In QueryBOMReleaseAttributes.queryPart number is:"+number) ;
		
		List<GenericIEBean> list = new ArrayList<GenericIEBean>();
		
		/**
		 * 根据零部件编号进行查询
		 * 
		 */
		String where = "";
		
		if( number != null && !number.trim().isEmpty()  ){
			where = "parent_item_id ='*" + number + "*' " ;
		}else{
			//不存在所有搜索情况
			where = "parent_item_id ='" + "\"\"" + "' " ;
		}

		logger.debug( "query() ... where : " + where );
		
		QueryIETask queryIETask = new QueryBOMReleaseAttributes() ;
		
		list = queryIETask.query( where );
		
		logger.debug("Existed Out QueryBOMReleaseAttributes.queryPart( String number )......") ;
		
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
		
		String url = CommonPDMUtil.getCodebasePath() + "ext/generic/integration/erp/config/QueryBOMReleaseAttributes.xml";
		
		QueryXMLConfigUtil config = new QueryXMLConfigUtil(url);
		
		return config.getQueryAttributes();
	}
}
