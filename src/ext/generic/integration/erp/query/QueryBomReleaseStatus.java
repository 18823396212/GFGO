/**
 * BOM发布状态查询Info*E Task
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-10-08
 */

package ext.generic.integration.erp.query;

import java.util.ArrayList;
import java.util.List;

import ext.generic.integration.erp.bean.GenericIEBean;
import ext.generic.integration.erp.util.IntegrationConstant;

public class QueryBomReleaseStatus extends QueryIETask {
//	private static String clazz = QueryBomReleaseStatus.class.getName() ;
//	
//	private static boolean verbose = PDMIntegrationLogUtil.verbose ;
	
	private static final String CONFIG_XML_FILE = "QueryBomReleaseStatus.xml" ;
	
	private static String table = "" ;
	
	public QueryBomReleaseStatus(){
		super();
		table = super.getTable() ;
	}
	
	@Override
	protected String setConfigXMLFile() {
		return CONFIG_XML_FILE ;
	}

	public static List<GenericIEBean> query(){
//		PDMIntegrationLogUtil.printLog(clazz, "Entering In QueryPartReleaseStatus.query()......") ;
		
		List<GenericIEBean> list = new ArrayList<GenericIEBean>();
		
		/**
		 * 如果标识符不为N，且PDM状态回写标识符不为S，则返回当前中间表记录
		 * 
		 */
		String where = "flag <> '" + IntegrationConstant.PDM_RELEASE_STATUS + "' " ;
		
		where = where + " And " + "pdm_write_back_status <> '" + IntegrationConstant.PDM_WRITE_BACK + "' " ;
		
		QueryIETask queryIETask = new QueryBomReleaseStatus() ;
		
		list = queryIETask.query( where );
		
//		if( verbose ){
//			PDMIntegrationLogUtil.printLog(clazz, queryIETask.getObjectListInfo( list ) ) ;
//		}
		
//		PDMIntegrationLogUtil.printLog(clazz, "Existed Out QueryPartReleaseStatus.query()......") ;
		
		return list ;
	}
	
	public static String getTableName(){
		return table ;
	}
}
