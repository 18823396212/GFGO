/**
 * 物料信息发布状态查询Info*E Task
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-10-08
 */

package ext.generic.integration.erp.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import wt.log4j.LogR;

import ext.generic.integration.erp.bean.GenericIEBean;
import ext.generic.integration.erp.util.IntegrationConstant;
import ext.generic.integration.erp.util.PDMIntegrationLogUtil;

public class QueryPartReleaseStatus extends QueryIETask {
//	private static String clazz = QueryPartReleaseStatus.class.getName() ;
	
//	private static boolean verbose = PDMIntegrationLogUtil.verbose ;
	
	private static final Logger logger = LogR.getLogger(QueryPartReleaseStatus.class.getName() );
	
	private static final String CONFIG_XML_FILE = "QueryPartReleaseStatus.xml" ;
	
	private static String table = "" ;
	
	public QueryPartReleaseStatus(){
		super();
		table = super.getTable() ;
	}
	
	@Override
	protected String setConfigXMLFile() {
		return CONFIG_XML_FILE ;
	}

	public static List<GenericIEBean> query(){
		
		List<GenericIEBean> list = new ArrayList<GenericIEBean>();
		
		/**
		 * 如果标识符不为N，且PDM状态回写标识符不为S，则返回当前中间表记录
		 * 
		 */
		String where = "flag <> '" + IntegrationConstant.PDM_RELEASE_STATUS + "' " ;
		
		where = where + " And " + "pdm_write_back_status <> '" + IntegrationConstant.PDM_WRITE_BACK + "' " ;
		
		QueryIETask queryIETask = new QueryPartReleaseStatus() ;
		list = queryIETask.query( where );
		
//		if( verbose ){
//			PDMIntegrationLogUtil.printLog(clazz, queryIETask.getObjectListInfo( list ) ) ;
//		}
		
		
		return list ;
	}
	
	public static String getTableName(){
		return table ;
	}
}
