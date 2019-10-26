/**
 * 供应商信息查询
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-10-08
 */

package ext.generic.integration.erp.query;

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import wt.log4j.LogR;

import ext.generic.integration.erp.common.CommonUtil;
import ext.generic.integration.erp.config.ERPPropertiesUtil;
import ext.generic.integration.erp.util.IntegrationConstant;
import ext.generic.integration.erp.util.PDMIntegrationLogUtil;

import com.infoengine.SAK.Task;
import com.infoengine.util.IEException;
import com.infoengine.object.IeDatum;
import com.infoengine.object.IeGroup;
import com.infoengine.object.IeObject;
import com.infoengine.object.factory.Group;

public class SupplierInfoIETask {
	
//	private static String clazz = SupplierInfoIETask.class.getName() ;
	
//	private static boolean verbose = PDMIntegrationLogUtil.verbose ;
	
	private static final Logger logger = LogR.getLogger(SupplierInfoIETask.class.getName() );
	
	private static String ieXMLFileQuery = null ;
	
	static {
		ieXMLFileQuery = (String) ERPPropertiesUtil.getInstance().readProperties().get(IntegrationConstant.SUPPLIER_INFO_QUERY_IE_XML_FILE);
		
		initialCheck();
	}

	protected static void initialCheck() {
		if(ieXMLFileQuery != null){
			ieXMLFileQuery = ieXMLFileQuery.trim() ;
			
			logger.debug("Query Config File : " + ieXMLFileQuery );
//			PDMIntegrationLogUtil.printLog(clazz, "Query Config File : " + ieXMLFileQuery ) ;
		}else{
			logger.debug("Query Config File : NULL");
//			PDMIntegrationLogUtil.printLog(clazz, "Query Config File : NULL" , IntegrationConstant.LOG_LEVEL_ERROR ) ;
		}
	}
	
	/**
	 * 通过供应商id模糊查询
	 * 
	 * @param id
	 * 
	 * @return Map<String,String> , key为供应商id , value为供应商name
	 */
	public static Map<String,String> queryLikeId(String id){
//		PDMIntegrationLogUtil.printLog(clazz, "Entering In queryLikeId(String id)......") ;
		
		Group group = null ;
		
		if( ( id != null ) && ( ! id.trim().equals("") ) ){
//			PDMIntegrationLogUtil.printLog(clazz, "id=" + id ) ;
			
			String where = "vendor_id='*" + id + "*' " ;

//			PDMIntegrationLogUtil.printLog(clazz, "where is:" + where ) ;
			
			group = runTask(where) ;
		}
		
		//获取供应商信息
		Map<String,String> map = getSupplierInfo( group ) ;
		
//		if( verbose ){
//			PDMIntegrationLogUtil.printLog(clazz, CommonUtil.getMapStringValue( map ) ) ;
//		}
		
//		PDMIntegrationLogUtil.printLog(clazz, "Existed Out queryLikeId(String id)......") ;
		
		return map ;
	}
	
	/**
	 * 通过id值，查询供应商
	 * 
	 * @param id
	 * @return Map<String,String> , key为供应商id , value为供应商name
	 */
	public static Map<String,String> queryById(String id){
//		PDMIntegrationLogUtil.printLog(clazz, "Entering In queryById(String id)......") ;
		
		Group group = null ;
		
		if( ( id != null ) && ( ! id.trim().equals("") ) ){
//			PDMIntegrationLogUtil.printLog(clazz, "id=" + id ) ;
			
			String where = "vendor_id='" + id + "' " ;

//			PDMIntegrationLogUtil.printLog(clazz, "where is:" + where ) ;
			
			group = runTask(where) ;
		}
		
		//获取供应商信息
		Map<String,String> map = getSupplierInfo( group ) ;
		
//		if( verbose ){
//			PDMIntegrationLogUtil.printLog(clazz, CommonUtil.getMapStringValue( map ) ) ;
//		}
		
//		PDMIntegrationLogUtil.printLog(clazz, "Existed Out queryById(String id)......") ;
		
		return map ;
	}
	
	/**
	 * 通过name值，模糊查询供应商信息
	 * 
	 * @param name
	 * @return Map<String,String> , key为供应商id , value为供应商name
	 */
	public static Map<String,String> queryLikeName(String name){
//		PDMIntegrationLogUtil.printLog(clazz, "Entering In queryLikeName(String name)......") ;
		
		Group group = null ;
		
		String where = "" ;
		
		if( ( name != null ) && ( ! name.trim().equals("") ) ){
//			PDMIntegrationLogUtil.printLog(clazz, "name=" + name ) ;
			
			where = "vendor_name='*" + name + "*' " ;
		}
//		else{
//			where = "*" ;
//		}
		
//		PDMIntegrationLogUtil.printLog(clazz, "where is:" + where ) ;
		
		group = runTask(where) ;
		
		//获取供应商信息
		Map<String,String> map = getSupplierInfo( group ) ;
		
//		if( verbose ){
//			PDMIntegrationLogUtil.printLog(clazz, CommonUtil.getMapStringValue( map ) ) ;
//		}
		
//		PDMIntegrationLogUtil.printLog(clazz, "Existed Out queryLikeName(String name)......") ;
		
		return map ;
	}
	
	/**
	 * 通过name值，查询供应商信息
	 * 
	 * @param name
	 * @return Map<String,String> , key为供应商id , value为供应商name
	 */
	public static Map<String,String> queryByName(String name){
		
		Group group = null ;
		
		String where = ""	; 
		
		if( ( name != null ) && ( ! name.trim().equals("") ) ){
			logger.debug("name=" + name);
			
			where = "vendor_name='" + name + "' " ;
		}else{
			where = "vendor_name='*'";
		}
		
		logger.debug("where is:" + where );
		
		group = runTask(where) ;
		
		//获取供应商信息
		Map<String,String> map = getSupplierInfo( group ) ;
		
		logger.debug("CommonUtil.getMapStringValue( map ) is:"+CommonUtil.getMapStringValue( map ));
		
		return map ;
	}
	
	/**
	 * 通过Info*E查询，返回Group, 通过此方法解析供应商信息
	 * 
	 * @param group
	 * @return Map<String,String> , key为供应商id , value为供应商name
	 */
	protected static Map<String,String> getSupplierInfo(Group group){
//		PDMIntegrationLogUtil.printLog(clazz, "Entering In getSupplierInfo(Group group)......") ;
		
		Map<String,String> map = new LinkedHashMap<String,String>() ;
		
		if( group == null ){
			logger.debug("group == null");
//			PDMIntegrationLogUtil.printLog(clazz, "group == null" , IntegrationConstant.LOG_LEVEL_ERROR) ;
			return map ;
		}else{
			int count = group.getElementCount() ;
//			PDMIntegrationLogUtil.printLog(clazz, "count = " + count ) ;
			logger.debug("count = " + count );
			
			if(  count > 0 ){
				IeGroup ieGroup = group.getGroup();
				
				Enumeration children = ieGroup.getChildren();
				if( children != null ){
//					PDMIntegrationLogUtil.printLog(clazz, ">>>>> 开始解析供应商信息......") ;
					
					int i = 1 ;
					while(children.hasMoreElements()){
						IeObject ieRecord = (IeObject)children.nextElement();
						
						//解析供应商id和供应商名称
						IeDatum vendorIdIeDatum = ieRecord.getAtt("vendor_id").getDatum();
						IeDatum vendorNameIeDatum = ieRecord.getAtt("vendor_name").getDatum();
						
						String vendorId = vendorIdIeDatum.rawValueOf().toString();
						String vendorName = vendorNameIeDatum.rawValueOf().toString();
						
//						if(verbose){
//							PDMIntegrationLogUtil.printLog(clazz, "获取第" + ( i++ ) + "个供应商信息：" ) ;
//							PDMIntegrationLogUtil.printLog(clazz, "key = " + vendorId ) ;
//							PDMIntegrationLogUtil.printLog(clazz, "value = " + vendorName ) ;
//						}
						
						//将供应商id和供应商名称放入map中
						map.put(vendorId, vendorName) ;
					}
					
				}else{
					logger.debug("Enumeration children = null!");
				}
			}
		}
		
		
		return map ;
	}
	
	/**
	 * 执行查询的Info*E Task
	 * 
	 * @param where 插叙条件
	 * @return Group , 通过Info*E查询，返回Group
	 */
	protected static Group runTask(String where){
		
		logger.debug("Enter runTask where is: "+where);
//		PDMIntegrationLogUtil.printLog(clazz, "Entering In runTask(String where)......") ;
		
		Group group = null ;
		
		if( where == null ){
			logger.debug("runTask where is null");
			return group ;
		}
		
		//创建任务
		Task task = new Task( ieXMLFileQuery );

		task.addParam( "WHERE",where );

		//执行任务
		try {
			task.invoke();
		} catch (IEException e) {
			logger.debug("Throws IEException..." + e.getmessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.debug("Throws IOException..." + e.getMessage());
			e.printStackTrace();
		}
		
		//获取返回值
		group =  task.getGroup("QueryObjects");
		
//		PDMIntegrationLogUtil.printLog(clazz, "Existed Out runTask(String where)......") ;
		
		return group ;
	}
}
