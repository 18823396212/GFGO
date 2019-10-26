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
package ext.generic.integration.erp.service;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import ext.com.iba.IBAUtil;
import ext.generic.integration.erp.util.BusinessRuleXMLConfigUtil;
import ext.generic.integration.erp.util.IntegrationConstant;
import ext.pi.core.PIAttributeHelper;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTKeyedHashMap;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.util.WTException;
/**
 * 零部件信息发布类
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-10-08
 */
public class PartReleaseService {
	
	private static final String CLASSNAME = PartReleaseService.class.getName() ;

	private static final Logger logger = LogR.getLogger(CLASSNAME);
	
	
	/**
	 * 发布PBO物料信息
	 * 
	 * @param obj
	 */
	public static void release(Object obj){
		release( obj , "" , "") ;
	}
	
	/**
	 * 发布PBO物料信息
	 * 
	 * @param obj
	 * @param releaseTime
	 */
	public static void release(Object obj , String releaseTime, String batchNumber){
		if( obj != null && obj instanceof WTPart){
			WTPart part = ( WTPart ) obj ;
			
			release( part , releaseTime , batchNumber) ;
		}
	}
	
	/**
	 * 发布零部件物料信息
	 * 
	 * @param part
	 */
	public static void release(WTPart part){
		release( part , "" , "'") ;
	}
	
	/**
	 * 发布零部件物料信息
	 * 
	 * @param part
	 * @param releaseTime
	 */
	public static WTKeyedHashMap release(WTPart part , String releaseTime, String batchNumber){
		
		return release(part, releaseTime, batchNumber, null);
	}
	
	/**
	 * 发布零部件物料信息
	 * @param part 零部件
	 * @param releaseTime 发布时间，暂时基本不用，因为日期为时间类型
	 * @param batchNumber 批次号
	 * @param self 流程
	 * @param params 其他参数
	 * @return
	 */
	public static WTKeyedHashMap release(WTPart part , String releaseTime, String batchNumber, ObjectReference self, Object... params){
		
		IbatisPartReleaseService ibatisPartReleaseService =  new IbatisPartReleaseService();
		return ibatisPartReleaseService.release(part, releaseTime, batchNumber, self, params);
	}
	
	/**
	 * 发布零部件物料信息
	 * 
	 * @param parts
	 * @throws IntegrationException 
	 */
	public static void release(List<WTPart> parts){
		release( parts , "", "") ;
	}
	
	/**
	 * 发布零部件物料信息
	 * 
	 * @param parts
	 * @param releaseTime
	 * @throws IntegrationException 
	 */
	public static WTKeyedHashMap release(List<WTPart> parts , String releaseTime, String batchNumber){
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		if( parts == null ){
			return hashMap;
		}else{
			Iterator<WTPart> ite = parts.iterator() ;
			
			while( ite.hasNext() ){
				WTPart part = ite.next() ;
				
				if( part != null ){
					WTKeyedHashMap map = release( part , releaseTime , batchNumber);

//					System.out.println( "Debug   release map : " + map );
					
					if( map != null ){
						Iterator keyIt =  map.keySet().iterator();
						while( keyIt.hasNext() ){
							Object obj = keyIt.next();
							
							if( obj != null ){
								hashMap.put( obj, map.get(obj) );
							}
						}
					}
					
//					System.out.println( "Debug   release hashMap : " + hashMap );
					//一个物料发布出现异常，整个发布程序停止
//					if( hashMap.size() > 0 ){
//						break;
//					}
				}
			}
		}

//		
//		if( hashMap.size() > 0 ){
//			throw new IntegrationException();
//		}
		
		return hashMap;
	}
	
	/**
	 * 发布零部件物料信息
	 * 
	 * @param parts
	 * @throws Exception 
	 */
	public static void releasePersistable( WTArrayList parts) throws Exception{
		releasePersistable( parts , "" , "") ;
	}
	
	/**
	 * 发布零部件物料信息
	 * 
	 * @param parts
	 * @param releaseTime
	 * @param batchNumber
	 * @throws Exception 
	 */
	public static WTKeyedHashMap releasePersistable( WTArrayList parts , String releaseTime , String batchNumber) throws Exception{
		return releasePersistable( parts , releaseTime , batchNumber, null) ;
	}
	
	/**
	 * 发布零部件物料信息
	 * @param parts 物料集合
	 * @param releaseTime 时间
	 * @param batchNumber 批次
	 * @param self 流程
	 * @return
	 * @throws Exception
	 */
	public static WTKeyedHashMap releasePersistable( WTArrayList parts , String releaseTime , String batchNumber, ObjectReference self) throws Exception{
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		if( parts == null ){
			return hashMap;
		}
		
		try {
			int size = parts.size() ;
			for(int i = 0 ; i < size ; i++ ){
				Persistable persistable = parts.getPersistable(i) ;
				
				if( persistable != null && persistable instanceof WTPart ){
					WTPart part = ( WTPart ) persistable ;
					
					//清空属性
					PIAttributeHelper.service.forceUpdateSoftAttribute(part, "erpErrorMsg", "");
					PIAttributeHelper.service.forceUpdateSoftAttribute(part, "partReleaseStatus", "");
					PIAttributeHelper.service.forceUpdateSoftAttribute(part, "bomReleaseStatus", "");
					
					hashMap.putAll( release( part , releaseTime , batchNumber, self) ) ;
					//TODO 一个物料发布一次，整个发布则停止
//					if( hashMap.size() > 0 ){
//						break;
//					}
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		
		return hashMap;
	}
	
	/**
	 * 判断当前零部件是否已经成功发送到ERP
	 * 
	 * @param part
	 * @return
	 */
	public static boolean isPartReleasedSuccess(WTPart part) {
		logger.debug("Enter In isPartReleasedSuccess( WTPart part , String releaseTime , String ecnNumber )...") ;
		
		boolean isReleasedSuccess = false ;
		
		boolean pdmRealsed = false;
		//如果BOM发布状态为"PDM已发布"或者"ERP接收成功"，且BOM表头中存在该数据，则认为发布成功。
		try {
			Object obj = IBAUtil.getIBAValue(part, BusinessRuleXMLConfigUtil.getInstance().getPartReleaseStatus());
			
			if( obj != null && 
				(IntegrationConstant.PDM_RELEASE_STATUS_CN.equals(obj.toString())
				|| IntegrationConstant.ERP_PROCESS_SUCCESS_CN.equals(obj.toString()))){
				pdmRealsed = true;
				isReleasedSuccess = true;
			}
			
		} catch ( Exception e ) {				
			e.printStackTrace();
		} 
		
		logger.debug("isPartReleasedSuccess = " + isReleasedSuccess ) ;
		
		return isReleasedSuccess ;
	}
}
