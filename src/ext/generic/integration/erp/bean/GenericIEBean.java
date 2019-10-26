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
package ext.generic.integration.erp.bean;

import java.util.HashMap;
import java.util.Map;

import ext.generic.integration.erp.common.CommonUtil;
/**
 * 一般对象类
 * 用于保存Info*E从中间表查询出来的数据
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-09-04
 */
public class GenericIEBean {
	//属性Map表
	private Map<String,String>  map = new HashMap<String,String>() ;
	
	public GenericIEBean( ){
		
	}
	
	/**
	 * 设置属性和属性值
	 * 
	 * @param attName
	 * @param attValue
	 */
	public void setAttribute(String attName , String attValue ){
		if( attName != null ){
			attName = attName.trim() ;
			
			if( attValue == null ){
				attValue = "" ;
			}
			
			attValue = attValue.trim() ;
			
			this.map.put( attName , attValue ) ;
		}
	}
	
	/**
	 * 批量设置属性和属性值
	 * 
	 * @param map
	 */
	public void setAllAttributes( Map<String,String> map ){
		if( map != null ){
			this.map.putAll(map) ;
		}
	}

	/**
	 * 获取属性值
	 * 
	 * @param attName
	 * @return
	 */
	public String getAttribute(String attName){
		String attValue = "" ;
		
		if(attName != null ){
			attName = attName.trim() ;
			
			if( this.map.containsKey(attName) ){
				attValue = this.map.get( attName );
			}
		}
		
		if( attValue == null ){
			attValue = "" ;
		}
		
		return attValue ;
	}
	
	/**
	 * 获取属性集合
	 * 
	 * @return
	 */
	public Map<String,String> getAllAttributes(){
		return this.map ;
	}

	@Override
	public String toString() {
		return CommonUtil.getMapStringValue( this.map ) ;
	}
}
