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
package ext.generic.integration.erp.ibatis;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.infoengine.util.IEException;

import ext.generic.integration.erp.attributes.AttributeUtil;
import ext.generic.integration.erp.attributes.PartUsageLinkAttribute;
import ext.generic.integration.erp.bean.AlternativeMaterial;
import ext.generic.integration.erp.util.IbatisUtil;
import ext.generic.integration.erp.util.IntegrationConstant;
import wt.fc.collections.WTKeyedHashMap;
import wt.log4j.LogR;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.util.WTException;
/**
 * 替代关系iBATIS类
 * 
 */
public class AlternativeMaterialDeleteByChildIbatis {
	
	private static final String clazz = AlternativeMaterialDeleteByChildIbatis.class.getName() ;
	private static final Logger logger = LogR.getLogger(clazz);
	
	/**
	 * 从中间表中删除替代件的信息
	 * 
	 * @param material
	 * @throws SQLException 
	 * @throws IEException 
	 * @throws IOException 
	 */
	public static WTKeyedHashMap delete( WTPartUsageLink wtUsageLink ) throws SQLException{
		logger.debug("   Entering In delete( WTPartUsageLink wtUsageLink )......") ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if( wtUsageLink == null ){
			logger.error("delete Method, WTPartUsageLink is NULL");
			return hashMap;
		}
		SqlMapClient sqlMap = null;
	    sqlMap = IbatisUtil.getSqlMapInstance();
	    AlternativeMaterial alternativeMaterial = new AlternativeMaterial();
	    
	    try {
			alternativeMaterial.setParentNumber(AttributeUtil.getAttribute(wtUsageLink, PartUsageLinkAttribute.PARENT_PART_NUMBER));
			alternativeMaterial.setChildNumber(AttributeUtil.getAttribute(wtUsageLink, PartUsageLinkAttribute.CHILD_PART_NUMBER));
			alternativeMaterial.setParentView(AttributeUtil.getAttribute(wtUsageLink, PartUsageLinkAttribute.PARENT_PART_VIEW));
	    } catch (WTException e1) {
			
	    	e1.printStackTrace();
			hashMap.put(wtUsageLink, "生成AlternativeMaterial对象错误："+e1.getMessage());
			return hashMap;
		}
	    try {
			
			logger.debug("   "+"执行替代关系信息删除操作") ;
			sqlMap.startTransaction();
			try {
				sqlMap.delete(IbatisSqlConstant.DELETE_ALTERNATIVE_MATERIAL,alternativeMaterial);
			} catch (SQLException e) {
				
				e.printStackTrace();
				hashMap.put(wtUsageLink, "删除替代关系出错:"+e.getMessage());
			}
			if(hashMap.size()==0)
				sqlMap.commitTransaction();
			logger.debug("   "+"删除替代关系信息任务结束") ;
		}finally{
			sqlMap.endTransaction();
			IbatisUtil.closeSqlMapInstance();
			logger.debug("   "+"Existed Out delete( WTPartUsageLink wtUsageLink )......") ;
		}
	    return hashMap;
	}
	
	
	/**
	 * 从中间表中删除替代件的信息
	 * 
	 * @param material
	 * @throws SQLException 
	 * @throws IEException 
	 * @throws IOException 
	 */
	public static WTKeyedHashMap delete( WTPartUsageLink wtUsageLink,SqlMapClient sqlMap  ) throws SQLException{
		logger.debug("   "+"Entering In delete( WTPartUsageLink wtUsageLink )......") ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if( wtUsageLink == null ){
			logger.error("delete Method, WTPartUsageLink is NULL");
			return hashMap;
		}
	    AlternativeMaterial alternativeMaterial = new AlternativeMaterial();
	    
	    try {
			alternativeMaterial.setParentNumber(AttributeUtil.getAttribute(wtUsageLink, PartUsageLinkAttribute.PARENT_PART_NUMBER));
			alternativeMaterial.setChildNumber(AttributeUtil.getAttribute(wtUsageLink, PartUsageLinkAttribute.CHILD_PART_NUMBER));
			alternativeMaterial.setParentView(AttributeUtil.getAttribute(wtUsageLink, PartUsageLinkAttribute.PARENT_PART_VIEW));
			//设置替代类型为S
			alternativeMaterial.setReplaceRelationship("S");
	    } catch (WTException e1) {
			
	    	e1.printStackTrace();
			hashMap.put(wtUsageLink, "生成AlternativeMaterial对象错误："+e1.getMessage());
			return hashMap;
		}
	    try {
			sqlMap.delete(IbatisSqlConstant.DELETE_SUBSTITUTE_MATERIAL,alternativeMaterial);
		} catch (SQLException e) {
			e.printStackTrace();
			hashMap.put(wtUsageLink, "删除替代关系出错:"+e.getMessage());
		}
	    return hashMap;
	}
	
	
	/**
	 * 从中间表中删除全局替代件的信息
	 * 
	 * @param material
	 * @throws SQLException 
	 */
	public static WTKeyedHashMap delete( WTPartMaster master ) throws SQLException{
		logger.debug("   "+"Entering In delete( WTPartMaster master )......") ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if( master == null ){
			logger.debug("   "+"delete Method, WTPartMaster is NULL");
			return hashMap ;
		}
		
		//创建任务
		SqlMapClient sqlMap = null;
	    sqlMap = IbatisUtil.getSqlMapInstance();
	    AlternativeMaterial alternativeMaterial = new AlternativeMaterial();
	    
	    alternativeMaterial.setParentNumber(IntegrationConstant.NO_VALUE);
	    alternativeMaterial.setParentView(IntegrationConstant.NO_VALUE);
		alternativeMaterial.setChildNumber(master.getNumber());
		
		
		try {
			logger.debug("   "+"执行替代关系信息删除操作") ;
			sqlMap.startTransaction();
			try {
				sqlMap.delete(IbatisSqlConstant.DELETE_ALTERNATIVE_MATERIAL,alternativeMaterial);
			} catch (SQLException e) {
				
				e.printStackTrace();
				hashMap.put(master, "删除替代关系出错:"+e.getMessage());
			}
			if(hashMap.size()==0)
				sqlMap.commitTransaction();
			logger.debug("   "+"删除替代关系信息任务结束") ;
		}finally{
			sqlMap.endTransaction();
			IbatisUtil.closeSqlMapInstance();
			logger.debug("   "+"Existed Out delete( WTPartMaster master )......") ;
		}
		return hashMap;
	}
	
	/**
	 * 从中间表中删除全局替代件的信息
	 * 
	 * @param material
	 * @throws SQLException 
	 */
	public static WTKeyedHashMap delete( WTPartMaster master ,SqlMapClient sqlMap) throws SQLException{
		logger.debug("   "+"Entering In delete( WTPartMaster master )......") ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if( master == null ){
			logger.debug("   "+"delete Method, WTPartMaster is NULL");
			return hashMap ;
		}
		
		//创建任务
		//SqlMapClient sqlMap = null;
	   //sqlMap = IbatisUtil.getSqlMapInstance();
	    AlternativeMaterial alternativeMaterial = new AlternativeMaterial();
	    
	    alternativeMaterial.setParentNumber(IntegrationConstant.NO_VALUE);
	    alternativeMaterial.setParentView(IntegrationConstant.NO_VALUE);
		alternativeMaterial.setChildNumber(master.getNumber());
		alternativeMaterial.setReplaceRelationship("A");
		
		try {
			sqlMap.delete(IbatisSqlConstant.DELETE_ALTERNATIVE_MATERIAL,alternativeMaterial);
		} catch (SQLException e) {
			
			e.printStackTrace();
			hashMap.put(master, "删除替代关系出错:"+e.getMessage());
		}
		return hashMap;
	}
}
