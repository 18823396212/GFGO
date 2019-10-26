package ext.generic.integration.erp.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import wt.log4j.LogR;

import com.infoengine.SAK.Task;
import com.infoengine.object.IeAtt;
import com.infoengine.object.IeDatum;
import com.infoengine.object.IeGroup;
import com.infoengine.object.IeObject;
import com.infoengine.object.factory.Group;
import com.infoengine.util.IEException;

import ext.generic.integration.erp.bean.GenericIEBean;
import ext.generic.integration.erp.common.CommonConstant;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.generic.integration.erp.config.ERPPropertiesUtil;
import ext.generic.integration.erp.util.IntegrationConstant;
import ext.generic.integration.erp.util.QueryXMLConfigUtil;

public abstract class QueryIETask {
	
//	private static String clazz = QueryIETask.class.getName() ;
//	
//	private static boolean verbose = PDMIntegrationLogUtil.verbose ;
	private static final String CLASSNAME = QueryIETask.class.getName() ;

	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	//通过配置文件来获取Task的配置文件，配置文件是：ERPCustomized.properties
	private String ieXMLFileQuery = (String) ERPPropertiesUtil.getInstance().readProperties().get(IntegrationConstant.GENERIC_OBJECT_QUERY_IE_XML_FILE); ;
	
	protected QueryXMLConfigUtil configUtil = null ;
	
	protected Map<String,String> attMapping = null ;
	
	private String configXMLFile = null ;
	
	private String ieAdapter = "" ;

	private String table = "" ;

	private String column = "" ;
	
	private String where = "" ;

	public QueryIETask(){
		configXMLFile = getConfigFilePath() + setConfigXMLFile() ;
		
		configUtil = new QueryXMLConfigUtil( configXMLFile ) ;
		
		this.ieAdapter = configUtil.getIeAdapter() ;
		this.table = configUtil.getTable() ;
		this.attMapping = configUtil.getAttMapping() ;
		
		this.column = getColumn( this.attMapping ) ;
		
	}
	
	//抽象方法，需要指定XML配置文件
	protected abstract String setConfigXMLFile( ) ;

	/**
	 * 按照where条件查询中间表
	 * 
	 * @param where
	 * @return
	 */
	protected List<GenericIEBean> query(String where){
		logger.debug("QueryIETask.query , where is " + where);
		
		List<GenericIEBean> list = new ArrayList<GenericIEBean>() ;
		
		if( where != null ){
			Group group = runTask( where ) ;

			//获取查询对象信息列表
			list = getQueryInfo( group ) ;
			
			logger.debug("list is:" + getObjectListInfo( list ));
		}

		return list ;
	}
	
	/**
	 * 执行查询的Info*E Task
	 * 
	 * @param where 查询条件
	 * @return Group , 通过Info*E查询，返回Group
	 */
	private Group runTask( String where ){

		Group group = null ;
		
		//创建任务
		Task task = new Task( ieXMLFileQuery );

		//添加参数
		task.addParam("instance", this.ieAdapter ) ;
		task.addParam("table",  this.table ) ;
		task.addParam("column", this.column ) ;
		
		task.addParam( "where", where );

//		logger.debug("runTask IETaskCommonUtil.getTaskParams(task):" + IETaskCommonUtil.getTaskParams(task) );
		
		//执行任务
		try {
			task.invoke();
		} catch (IEException e) {
			logger.debug("Throws IEException..." +  e.getMessage());
			
			e.printStackTrace();
		} catch (IOException e) {
			logger.debug("Throws IOException..." + e.getMessage());
			e.printStackTrace();
		}
		
		//获取返回值
		group =  task.getGroup("QueryObjects");
		
		
		return group ;
	}
	
	/**
	 * 通过Info*E查询，返回Group, 通过此方法解析查询信息
	 * 
	 * @param group
	 * @return List<GenericIEBean> , 查询对象列表
	 */
	private List<GenericIEBean> getQueryInfo(Group group){
		
		List<GenericIEBean> list = new ArrayList<GenericIEBean>() ;
		
		if( group == null ){
			logger.debug("QueryIETask.getQueryInfo , group == null");
			return list ;
		}else{
			int count = group.getElementCount() ;
			logger.debug("QueryIETask.getQueryInfo , count = " + count);
			
			if(  count > 0 ){
				IeGroup ieGroup = group.getGroup();
				
				Enumeration children = ieGroup.getChildren();
				
				if( children != null ){
					
					int i = 1 ;
					//从查询的Group中解析数据
					while(children.hasMoreElements()){
						IeObject ieRecord = (IeObject)children.nextElement();
						
						GenericIEBean genericIEBean = new GenericIEBean() ;
						
						//将解析的数据与XML配置的列进行匹配，并将数据存入GenericIEBean类对象中。
						Iterator<String> attMappingIte = this.attMapping.keySet().iterator() ;
						while( attMappingIte.hasNext() ){
							String dbColumnStr = attMappingIte.next() ;

							String ieDatumValue = "" ;
							
							//修复空指针BUG
//							IeDatum ieDatum = ieRecord.getAtt( dbColumnStr ).getDatum();
//							
//							String ieDatumValue = ieDatum.rawValueOf().toString();
							
							IeAtt ieAtt = ieRecord.getAtt( dbColumnStr ) ;
							
							if( ieAtt != null ){
								IeDatum ieDatum = ieAtt.getDatum();
								
								if( ieDatum != null ){
									Object rawValue = ieDatum.rawValueOf() ;
									
									if( rawValue != null ){
										ieDatumValue = rawValue.toString();
									}
								}
							}
							
							if( ieDatumValue == null ){
								ieDatumValue = "" ;
							}
							
							if( dbColumnStr != null && ( ! dbColumnStr.trim().isEmpty() ) ){
								genericIEBean.setAttribute( dbColumnStr , ieDatumValue ) ;
							}
						}
												
						//加入List列表中，作为查询的返回值
						list.add(genericIEBean) ;
					}
					
				}else{
					logger.debug("Enumeration children = null!");
				}
			}
		}
		
		
		return list ;
	}
	
	/**
	 * 获取需要查询的列名的列表
	 * 
	 * @param map
	 * @return
	 */
	private String getColumn( Map<String,String> map){
//		PDMIntegrationLogUtil.printLog(clazz, "Entering In getColumn( Map<String,String> map)......") ;
		
		String column = "" ;
		
		if( map != null ){
			Iterator<String> ite = map.keySet().iterator() ;
			
			while( ite.hasNext()){
				String key = ite.next() ;
				
				column = column + key + CommonConstant.COMMA ;
			}
			
			if(column.endsWith( CommonConstant.COMMA )){
				column = column.substring(0, column.lastIndexOf( CommonConstant.COMMA )) ;
			}
		}
		
//		PDMIntegrationLogUtil.printLog(clazz, "Query Column List : " + column ) ;
		
//		PDMIntegrationLogUtil.printLog(clazz, "Existed Out getColumn( Map<String,String> map)......") ;
		
		return column ;
	}
	
	/**
	 * 获取列表中的对象信息
	 * 
	 * @param list
	 * @return
	 */
	protected String getObjectListInfo( List<GenericIEBean> list ){
		String objInfo = "" ;
		
		int size = list.size() ;

		for( int i = 0 ; i < size ; i++ ){
			GenericIEBean genericIEBean = list.get(i) ;
			objInfo = objInfo + genericIEBean.toString() ;
		}
		
		return objInfo ;
	} 
	
	/**
	 * 获取XML配置文件的路径
	 * 
	 * @return
	 */
	private String getConfigFilePath() {
		
		String configFilePath = CommonPDMUtil.getCodebasePath() ;
		
		logger.debug("Codebase Path : " + configFilePath);
		
		if( configFilePath == null ){
			configFilePath = "" ;
		}
		
		configFilePath = configFilePath + ERPPropertiesUtil.getInstance().readProperties().get(IntegrationConstant.ERP_CONFIG_FILE_PATH) + "/" ;
		
		configFilePath.replaceAll("\\\\", "/") ;
		
		logger.debug("Config File Path : " + configFilePath);
		
		return configFilePath ;
	}
	
	public String getIeAdapter() {
		return ieAdapter;
	}

	public String getTable() {
		return table;
	}
}
