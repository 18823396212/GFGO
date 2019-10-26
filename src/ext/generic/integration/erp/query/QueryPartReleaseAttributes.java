package ext.generic.integration.erp.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import wt.log4j.LogR;
import ext.generic.integration.erp.bean.GenericIEBean;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.generic.integration.erp.util.QueryXMLConfigUtil;

/**
 * 中间表物料详细查询
 * 
 * @author Kwang
 */
public class QueryPartReleaseAttributes extends QueryIETask {
	
	private static final Logger logger = LogR.getLogger(QueryPartReleaseAttributes.class.getName() );
	
	public static final String CONFIG_XML_FILE = "QueryPartReleaseAttributes.xml" ;
	
	private static String table = "" ;
	
	public QueryPartReleaseAttributes(){
		super();
		table = super.getTable() ;
	}
	
	@Override
	protected String setConfigXMLFile() {
		return CONFIG_XML_FILE ;
	}

	/***
	 * 根据编号查询中间表物料数据
	 * @param number
	 * @return
	 */
	public static List<GenericIEBean> queryPart( String number ){
		logger.debug("Entering In QueryPartReleaseAttributes.queryPart number is: " + number) ;
		
		List<GenericIEBean> list = new ArrayList<GenericIEBean>();
		
		/**
		 * 根据零部件编号进行查询
		 * 
		 */
		String where = "";
		
		if( number != null && !number.trim().isEmpty()  ){
			where = "item_id ='*" + number + "*' " ;
		}else{
			//不存在所有搜索情况
			where = "item_id ='" + "\"\"" + "' " ;
		}

		logger.debug( "query() ... where : " + where );
		
		QueryIETask queryIETask = new QueryPartReleaseAttributes() ;
		
		list = queryIETask.query( where );
		
		logger.debug("Existed Out QueryPartReleaseAttributes.queryPart( String number )......") ;
		
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
		
		String url = CommonPDMUtil.getCodebasePath() + "ext/generic/integration/erp/config/QueryPartReleaseAttributes.xml";
		
		QueryXMLConfigUtil config = new QueryXMLConfigUtil(url);
		
		return config.getQueryAttributes();
	}
}
