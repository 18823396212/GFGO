/**
 * 库存价格查询Info*E Task
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
import wt.part.WTPart;
import ext.generic.integration.erp.bean.GenericIEBean;


public class InventoryPriceIETask extends QueryIETask {
	
	private static final String CLASSNAME = InventoryPriceIETask.class.getName() ;

	@SuppressWarnings("unused")
	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	//指定配置文件
	private static final String CONFIG_XML_FILE = "InventoryPrice.xml" ;
	
	public InventoryPriceIETask(){
		super();
	}
	
	@Override
	protected String setConfigXMLFile() {
		return CONFIG_XML_FILE ;
	}
	
	/**
	 * 查询库存和价格
	 * 
	 * @param pbo
	 * @return
	 */
	public static List<GenericIEBean> query( Object pbo ){
		List<GenericIEBean> list = new ArrayList<GenericIEBean>();
		
		if( pbo != null && pbo instanceof WTPart){
			WTPart part = ( WTPart ) pbo ;
			
			list = query( part ) ;
		}
		
		return list ;
	}
	
	/**
	 * 查询零部件库存和价格
	 * 
	 * @param part
	 * @return
	 */
	public static List<GenericIEBean> query( WTPart part ){
//		logger.debug("Entering In InventoryPriceIETask.query( WTPart part )......");
		
		List<GenericIEBean> list = new ArrayList<GenericIEBean>();
		
		if( part != null ){
			String where = "item_id = '" + part.getNumber() + "' " ;
			
			//实例化查询对象
			QueryIETask queryIETask = new InventoryPriceIETask() ;
			
			list = queryIETask.query( where );
			
//			logger.debug(queryIETask.getObjectListInfo( list ));
		}
		
//		logger.debug("Existed Out InventoryPriceIETask.query( WTPart part )......");
		
		return list ;
	}
}
