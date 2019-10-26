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

import java.sql.SQLException;
import java.util.List;

import wt.fc.collections.WTKeyedHashMap;
import org.apache.log4j.Logger;

import wt.log4j.LogR;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;

import com.ibatis.sqlmap.client.SqlMapClient;

import ext.generic.integration.erp.attributes.AlternativeMaterialUtil;
import ext.generic.integration.erp.bean.AlternativeMaterial;
import ext.generic.integration.erp.common.CommonUtil;
import ext.generic.integration.erp.ibatis.AlternativeMaterialDeleteByChildIbatis;
import ext.generic.integration.erp.ibatis.AlternativeMaterialIbatis;
/**
 * 替代关系信息发布类
 * 
 */
public class IbatisReplacementReleaseService implements ReplacementReleaseServiceImpl{
	
	private static final String clazz = IbatisReplacementReleaseService.class.getName() ;
	
	private static final Logger logger = LogR.getLogger(clazz);
	
	/**
	 * 发布特定替代关系
	 * 
	 * @param usageLink
	 */
	public  void releaseSubstitute( WTPartUsageLink usageLink){
		releaseSubstitute( usageLink , "" );
	}
	
	/**
	 * 发布特定替代关系
	 * 
	 * @param usageLink
	 */
	public  WTKeyedHashMap releaseSubstitute( WTPartUsageLink usageLink , String releaseTime){
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		//先删除之前已经发布的同视图版本的usageLink中的所有特定替代，保证中间数据库中不会出现无效的数据。
		try {
			AlternativeMaterialDeleteByChildIbatis.delete( usageLink );
		} catch (SQLException e) {
			hashMap.put( usageLink , "替代料删除报错：" + e.toString() );
			
			e.printStackTrace();
			
			return hashMap;
		} 
		List<AlternativeMaterial> list = AlternativeMaterialUtil.getSubstituteLink(usageLink) ;
		
		int size = list.size() ;
		
		for(int i=0 ; i < size ; i++ ){
			AlternativeMaterial aMaterial = list.get(i) ;
			
			hashMap.putAll( AlternativeMaterialIbatis.add( usageLink , aMaterial , releaseTime ) ) ;
			//替代料一个发布错误，发布程序终止
			if( hashMap.size() > 0 ){
				return hashMap;
			}
		}
		
		return hashMap;
	}
	
	/**
	 * 发布特定替代关系
	 * 
	 * @param usageLink
	 */
	public  WTKeyedHashMap releaseSubstitute( WTPartUsageLink usageLink , String releaseTime,
		String batchNumber,SqlMapClient sqlMap){
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		logger.debug("Enter releaseSubstitute usageLink is:" +  usageLink + " releaseTime is:" + releaseTime);
		//先删除之前已经发布的同视图版本的usageLink中的所有特定替代，保证中间数据库中不会出现无效的数据。
		try {
			AlternativeMaterialDeleteByChildIbatis.delete( usageLink,sqlMap );
		} catch (SQLException e) {
			hashMap.put( usageLink , "替代料删除报错：" + e.toString() );
			
			e.printStackTrace();
			
			return hashMap;
		} 
		List<AlternativeMaterial> list = AlternativeMaterialUtil.getSubstituteLink(usageLink) ;
		
		int size = list.size() ;
		
		logger.debug("releaseSubstitute size is:" + size);
		for(int i=0 ; i < size ; i++ ){
			AlternativeMaterial aMaterial = list.get(i) ;
			//modify on 20160524,替代关系直接插入，不再判断是否存在
			//由于在同一个事物里面，不能同时对同一条记录执行删除更新操作
//			hashMap.putAll( AlternativeMaterialIbatis.add( usageLink , aMaterial , releaseTime,sqlMap ) ) ;
			try {
				aMaterial.setBatchNumber(batchNumber);
				AlternativeMaterialIbatis.create( aMaterial , releaseTime, sqlMap ) ;
			} catch (SQLException e) {				
				e.printStackTrace();
				hashMap.put(usageLink, e.toString());
			}
			
			//替代料一个发布错误，发布程序终止
			if( hashMap.size() > 0 ){
				return hashMap;
			}
		}
		
		return hashMap;
	}
	
	/**
	 * 发布全局替代关系
	 * 
	 * @param part
	 * @throws SQLException 
	 */
	public  WTKeyedHashMap releaseAlternate( WTPart part) throws Exception{
		return releaseAlternate( part , "" ) ;
		
	}
	
	/**
	 * 发布全局替代关系
	 * 
	 * @param part
	 * @throws SQLException 
	 */
	public  WTKeyedHashMap releaseAlternate( WTPart part , String releaseTime ) throws Exception{
		WTPartMaster partMaster = (WTPartMaster) part.getMaster() ;
		 return releaseAlternate( partMaster , releaseTime ) ;
		
	}
	
	/**
	 * 发布全局替代关系
	 * 
	 * @param partMaster
	 * @throws SQLException 
	 */
	public  WTKeyedHashMap releaseAlternate( WTPartMaster partMaster ) throws Exception{
		return releaseAlternate( partMaster , "" ) ;
	}
	
	/**
	 * 发布全局替代关系
	 * 
	 * @param partMaster
	 * @throws SQLException 
	 */
	public  WTKeyedHashMap releaseAlternate( WTPartUsageLink usageLink  , String releaseTime,
			String batchNumber,SqlMapClient sqlMap ) throws Exception{
		//先删除之前已经发布的同视图版本的usageLink中的所有全局替代，保证中间数据库中不会出现无效的数据。
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		AlternativeMaterialDeleteByChildIbatis.delete( usageLink.getUses(), sqlMap ) ;
		
		List<AlternativeMaterial> list = AlternativeMaterialUtil.getAlternateLink(usageLink.getUses()) ;
		
		int size = list.size() ;
		
//		System.out.println("获取全局替代关系数量为：" + size ) ;
		
		for(int i=0 ; i < size ; i++ ){
			AlternativeMaterial aMaterial = list.get(i) ;
			aMaterial.setBatchNumber(batchNumber);
			//不再执行查询操作，直接插入
			aMaterial.setParentNumber(usageLink.getUsedBy().getNumber());
			aMaterial.setParentView("Design");
			hashMap.putAll(AlternativeMaterialIbatis.add( usageLink , aMaterial , releaseTime, sqlMap ) );
//			try{					
//				aMaterial.setParentNumber(usageLink.getUsedBy().getNumber());
//				aMaterial.setParentView("Design");
//				AlternativeMaterialIbatis.create( aMaterial , releaseTime, sqlMap ) ;	
//			}catch (SQLException e) {
//				hashMap.put(usageLink, e.toString());
//				e.printStackTrace();
//			}
		}
		return hashMap;
	}
	
	/**
	 * 发布全局替代关系
	 * 
	 * @param partMaster
	 * @throws SQLException 
	 */
	public  WTKeyedHashMap releaseAlternate( WTPartMaster partMaster  , String releaseTime ) throws Exception{
		//先删除之前已经发布的同视图版本的usageLink中的所有全局替代，保证中间数据库中不会出现无效的数据。
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		AlternativeMaterialDeleteByChildIbatis.delete( partMaster ) ;
		
		List<AlternativeMaterial> list = AlternativeMaterialUtil.getAlternateLink(partMaster) ;
		
		int size = list.size() ;
		
//		System.out.println("获取全局替代关系数量为：" + size ) ;
		
		for(int i=0 ; i < size ; i++ ){
			AlternativeMaterial aMaterial = list.get(i) ;
			
			hashMap.putAll(AlternativeMaterialIbatis.add( null , aMaterial , releaseTime ) );
		}
		return hashMap;
	}
}
