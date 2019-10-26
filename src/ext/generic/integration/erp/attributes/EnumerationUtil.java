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
package ext.generic.integration.erp.attributes;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import wt.access.NotAuthorizedException;
import wt.inf.container.WTContainerException;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.services.ServiceFactory;
import wt.util.WTException;

import com.ptc.core.lwc.common.BaseDefinitionService;
import com.ptc.core.lwc.common.view.EnumerationDefinitionReadView;
import com.ptc.core.lwc.common.view.EnumerationEntryReadView;
import com.ptc.core.lwc.common.view.PropertyValueReadView;
/**
 * 本类作用：获取枚举值方法类
 */
public class EnumerationUtil implements RemoteAccess {
	private static final String CLASSNAME = EnumerationUtil.class.getName();
	private static final Logger logger = LogR.getLogger(CLASSNAME);

	public static String getEnumerationDisplayValue( String enumerationTypeName , String innerValue ){
		Map<String,String> enumerationValues = getEnumerationValues( enumerationTypeName ) ;
		
		String enumerationDisplayValue = enumerationValues.get( innerValue ) ;
		
		return enumerationDisplayValue ;
	}
	
	public static Map<String,String> getEnumerationValues( String enumerationTypeName ) {
		Map<String,String> enumerationValueMap = new HashMap<String,String>();
		
		try{
			BaseDefinitionService seviec = (BaseDefinitionService)ServiceFactory.getService(BaseDefinitionService.class);
			
			EnumerationDefinitionReadView view = seviec.getEnumDefView( enumerationTypeName );
			
			Map<String, EnumerationEntryReadView> map = view.getAllEnumerationEntries();
			
			Iterator<String> keyIte = map.keySet().iterator();
			
			while ( keyIte.hasNext()) {
				String key = keyIte.next();
				
				if( key ==null ) {
					continue;
				}
				
				EnumerationEntryReadView readview= map.get( key );
				
				if(readview==null) {
					continue;
				}
				
				PropertyValueReadView propreadview = readview.getPropertyValueByName("displayName");
				if(propreadview==null||propreadview.getValueAsString()==null) {
					continue;
				}
				if(logger.isDebugEnabled()){
					logger.debug(key + " ====> " + propreadview.getValueAsString()) ;
				}
				
				enumerationValueMap.put( key , propreadview.getValueAsString());
			}
		} catch (NotAuthorizedException e) {
			e.printStackTrace();
		} catch (WTContainerException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}finally{
			
		}
		
		return enumerationValueMap ;
	}

	
	public static void main(String[] args) {
		if( args != null && args[0] != null ){
			if ( ! RemoteMethodServer.ServerFlag ) {
				System.out.println( "Debug   RemoteMethodServer..." );
				String method = "getEnumerationValues";			
			
				try {
					RemoteMethodServer.getDefault().invoke(method, EnumerationUtil.class.getName() , null,
							new Class[] { String.class }, new Object[] { args[0] });
					System.out.println( "Debug  end  RemoteMethodServer..." );
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}else{
				System.out.println( "Debug   importParts..." );
				getEnumerationValues( args[0] ) ;
				System.out.println( "Debug   end  importParts..." );
			}
		}
	}
}
