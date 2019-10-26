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

import java.rmi.RemoteException;
import java.sql.Timestamp;

import ext.com.iba.IBAUtil;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.generic.integration.erp.common.CommonUtil;
import wt.change2.WTChangeOrder2;
import wt.method.RemoteAccess;
import wt.util.WTException;
/**
 * 
 * 本类作用：获取ECN对象的属性
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-10-08
 */

public class ECNAttribute implements RemoteAccess{
	
	public static final String CREATOR = "creator" ;
	
	public static final String CREATE_TIME = "createTime" ;
	
	public static final String DESCRIPTION = "description" ;
	
	public static final String LIFECYCLE = "lifeCycle" ;
	
	public static final String MODIFIER = "modifier" ;
	
	public static final String MODIFY_TIME = "modifyTime" ;
	
	public static final String NAME = "name" ;
	
	public static final String NUMBER = "number" ;
	
	public static final String NEED_DATE = "needDate" ;
	
	/**
	 * 获取属性值
	 * 
	 * @param ecn WTChangeOrder2
	 * @param attributeName
	 * @return String
	 */
	protected static String getAttribute( WTChangeOrder2 ecn , String attributeName ) {	
		String attributeValue = "" ;

		try{
			if( attributeName.equals( CREATOR )) {	
				
				attributeValue = CommonPDMUtil.getCreatorName(ecn) ;
				
			}else if( attributeName.equals( CREATE_TIME )){
				
				attributeValue = CommonPDMUtil.getCreateTime(ecn);
				
			}else if( attributeName.equals( DESCRIPTION ) ){
				
				attributeValue = ecn.getDescription() ;
				
			}else if( attributeName.equals( LIFECYCLE )){
				
				attributeValue = CommonPDMUtil.getLifecycle(ecn) ;
				
			}else if( attributeName.equals( MODIFIER )){
				
				attributeValue = CommonPDMUtil.getModifierName(ecn) ;
				
			}else if( attributeName.equals( MODIFY_TIME )){
				
				attributeValue = CommonPDMUtil.getModifyTime(ecn) ;
				
			}else if( attributeName.equals( NAME )){
				
				attributeValue = ecn.getName();
				
			}else if( attributeName.equals( NUMBER )){
				
				attributeValue = ecn.getNumber() ;
				
			}else if( attributeName.equals( NEED_DATE )){
				Timestamp needDate = ecn.getNeedDate();
				if( needDate != null ){
					attributeValue = CommonUtil.getDateValue( needDate );
				}else{
					attributeValue = "" ;
				}
			}else{
				attributeValue = (String) IBAUtil.getIBAValue( ecn, attributeName);
			}
		}catch(WTException wte){
			wte.printStackTrace() ;
		} catch (RemoteException e) {
			e.printStackTrace();
		}finally{
			
		}
		
		return attributeValue ;
	}
}
