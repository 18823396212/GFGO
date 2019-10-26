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
package ext.generic.integration.erp.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import ext.generic.integration.erp.common.FileUtil;

/**
 * 类功能描述：
 * 
 * 读取与ERP集成功能有关的配置信息
 *     
 * <br><br>
 * <b>Revision History</b>
 * <br><b>Rev:</b> 1.0 – 2012-07-12，魏文杰
 * <br><b>Comment:</b> Initial release.
 **/
public class ERPInitialized {
	
	private static final ERPInitialized propUtilSingleton = new ERPInitialized();
	
	private static final String PROPERTIES_CONFIG_FILE = "ERPInitialized.properties" ;
	
	private static Properties prop = null ;
	
	/**
	 * 私有构造方法
	 */
	private ERPInitialized(){
	
	}
	
	/**
	 * 返回类实例
	 * 
	 * <b>Revision History</b> <br>
	 * <b>Rev:</b> 1.0 - 2012-07-12，魏文杰 <br>
	 * <b>Comment:</b> Initial release.
	 */
	public static ERPInitialized getInstance() {
        return propUtilSingleton;
    }
	
	/**
	 * 读取配置文件
	 * 
	 * @return Map
	 * 
	 * <b>Revision History</b> <br>
	 * <b>Rev:</b> 1.0 - 2012-07-12，魏文杰 <br>
	 * <b>Comment:</b> Initial release.
	 */
	public Map readProperties(){
		if(prop != null){
			return prop ;
		}else{
			return readProperties(PROPERTIES_CONFIG_FILE);
		}
	}
	
	/**
	 * 读取配置文件
	 * 
	 * @return Map
	 * 
	 * <b>Revision History</b> <br>
	 * <b>Rev:</b> 1.0 - 2012-07-12，魏文杰 <br>
	 * <b>Comment:</b> Initial release.
	 */
	private Map readProperties(String propFname) {		
		prop = new Properties();
		
		InputStream inStream = null;
	
		String filePath = ERPInitialized.class.getResource("").getPath() ;	
		
		try {
			inStream = new FileInputStream(filePath + propFname);
			prop.load(inStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtil.closeStream(inStream);
		}

		return prop;
	}
	
	public static void main(String[] args) {
		Map map = ERPInitialized.getInstance().readProperties() ;
		
		if( map == null ){
			System.out.println(" PropertiesUtil.getInstance().readProperties() return map is NULL.") ;
		}else{
			System.out.println("\n---------------Print PropertiesUtil Map ---------------- \n") ;
			
			Iterator ite = map.keySet().iterator() ;
			while(ite.hasNext()){
				String key = (String) ite.next() ;
				String value = (String) map.get(key) ;
				
				System.out.println("Key : " + key + " ===> Value : " + value + "\n") ;
			}
			
			System.out.println("-------------------------------------------------------- \n") ;
		}
	}
}
