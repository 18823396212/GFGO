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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import wt.fc.collections.WTKeyedHashMap;
import wt.log4j.LogR;
import wt.part.WTPartUsageLink;
import wt.util.WTException;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.infoengine.util.IEException;

import ext.generic.integration.erp.bean.AlternativeMaterial;
import ext.generic.integration.erp.util.IbatisUtil;
import ext.generic.integration.erp.util.IntegrationConstant;

/**
 * 替代关系IBATIS类
 * @author Administrator
 *
 */
public class AlternativeMaterialIbatis {
	
	private static final String clazz = AlternativeMaterialIbatis.class.getName() ;
	private static final Logger logger = LogR.getLogger(clazz);
	
	/**
	 * 查询中间表中的替代件关系
	 * 
	 * @param material
	 * @return
	 * @throws WTException 
	 * @throws IEException 
	 * @throws IOException 
	 */
	public static List query( AlternativeMaterial material ) throws SQLException{
		logger.debug("   "+"Entering In query( AlternativeMaterial material )......") ;
		List list = new ArrayList() ;
		if(material==null)
			return list;
		SqlMapClient sqlMap = null;
	    sqlMap = IbatisUtil.getSqlMapInstance();
	    if (sqlMap != null){
	    	try{
	    		logger.debug("   "+"执行替代关系信息查询操作") ;
	    		list = sqlMap.queryForList(IbatisSqlConstant.QUERY_ALTERNATIVE_MATERIAL, material);
	    		logger.debug("   "+"查询替代关系信息任务结束") ;
	    	}catch (SQLException e) {
				
				e.printStackTrace();
				throw new SQLException(e.getMessage());
			}finally{
	     		IbatisUtil.closeSqlMapInstance();
	     	}
	    	
	    }
	    return list;
	}
	
	/**
	 * 查询中间表中的替代件关系
	 * 
	 * @param material
	 * @return
	 * @throws WTException 
	 * @throws IEException 
	 * @throws IOException 
	 */
	public static List query( AlternativeMaterial material,SqlMapClient sqlMap ) throws SQLException{
		logger.debug("   "+"Entering In query( AlternativeMaterial material )......") ;
		List list = new ArrayList() ;
		if(material == null)
			return list;
		logger.debug("   "+"执行替代关系信息查询操作") ;
		list = sqlMap.queryForList(IbatisSqlConstant.QUERY_ALTERNATIVE_MATERIAL, material);
		logger.debug("   "+"查询替代关系信息任务结束") ;
	    return list;
	}
	
	/**
	 * 判断中间表是否有替代关系
	 * 
	 * @param material
	 * @return
	 * @throws SQLException 
	 * @throws IEException 
	 * @throws IOException 
	 */
	public static boolean hasObject( AlternativeMaterial material ) throws SQLException{
		logger.debug("   "+"Entering In hasObject( AlternativeMaterial material )......") ;
		boolean bHasObject = false;
		List list = new ArrayList() ;
		list = query( material );
		if(list != null && list.size() > 0){
			bHasObject = true;
		}
		return bHasObject ;
	}
	
	/**
	 * 判断中间表是否有替代关系
	 * 
	 * @param material
	 * @return
	 * @throws SQLException 
	 * @throws IEException 
	 * @throws IOException 
	 */
	public static boolean hasObject( AlternativeMaterial material ,SqlMapClient sqlMap) throws SQLException{
		logger.debug("   "+"Entering In hasObject( AlternativeMaterial material )......") ;
		boolean bHasObject = false;
		List  list = new ArrayList() ;
		list = query( material,sqlMap );
		if(list != null && list.size() > 0){
			bHasObject = true;
		}
		return bHasObject ;
	}
	
	/**
	 * 向中间表添加替代件关系
	 * 
	 * @param material
	 */
	public static void add( AlternativeMaterial material ){
		add( null , material , "" ) ;
	}
	
	/**
	 * 向中间表添加替代件关系
	 * 
	 * @param material
	 * @param releaseTime
	 */
	public static WTKeyedHashMap add( WTPartUsageLink usageLink , AlternativeMaterial material , String releaseTime,SqlMapClient sqlMap ){
		logger.debug("   "+"Entering In add( AlternativeMaterial material , String releaseTime )......") ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		boolean hasAlternativeMaterial = false;
		try {
			hasAlternativeMaterial = hasObject( material, sqlMap );
		} catch (SQLException e1) {
			e1.printStackTrace();
			hashMap.put( usageLink , "替代料查询报错:"+e1.toString() );
			return hashMap;
		}
		logger.debug("   "+"hasAlternativeMaterial = " + hasAlternativeMaterial ) ;
		try{
			if( hasAlternativeMaterial ){
				logger.debug("   "+"执行更新替代件信息操作") ;
				
				update( material , releaseTime, sqlMap ) ;
			}else{
				logger.debug("   "+"执行创建替代件信息操作") ;
				
				create( material , releaseTime, sqlMap ) ;
			}		
		}catch (SQLException e) {
			e.printStackTrace();
			if( usageLink != null ){
				hashMap.put( usageLink , "替代料发布失败：" + material.getReplacePartNumber() + " 失败原因： " + e.toString() );
			}
			
			e.printStackTrace();
		} 
		logger.debug("   "+"Existed Out add( AlternativeMaterial material , String releaseTime )......") ;
		
		return hashMap;
	}
	
	
	/**
	 * 向中间表添加替代件关系
	 * 
	 * @param material
	 * @param releaseTime
	 */
	public static WTKeyedHashMap add( WTPartUsageLink usageLink , AlternativeMaterial material , String releaseTime ){
		logger.debug("   "+"Entering In add( AlternativeMaterial material , String releaseTime )......") ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		boolean hasAlternativeMaterial = false;
		try {
			hasAlternativeMaterial = hasObject( material );
		} catch (SQLException e1) {
			hashMap.put( usageLink , "替代料查询报错:"+e1.toString() );
			return hashMap;
		}
		logger.debug("   "+"hasAlternativeMaterial = " + hasAlternativeMaterial ) ;
		try{
			if( hasAlternativeMaterial ){
				logger.debug("   "+"执行更新替代件信息操作") ;
				
				update( material , releaseTime ) ;
			}else{
				logger.debug("   "+"执行创建替代件信息操作") ;
				
				create( material , releaseTime ) ;
			}		
		}catch (SQLException e) {
			if( usageLink != null ){
				hashMap.put( usageLink , "替代料发布失败：" + material.getReplacePartNumber() + " 失败原因： " + e.toString() );
			}
			
			e.printStackTrace();
		} 
		logger.debug("   "+"Existed Out add( AlternativeMaterial material , String releaseTime )......") ;
		
		return hashMap;
	}
	
	
	/**
	 * 从中间表中删除替代件的信息
	 * 
	 * @param material
	 * @throws WTException 
	 */
	public static void delete( AlternativeMaterial material ) throws WTException{
		logger.debug("   "+"Entering In delete( AlternativeMaterial material )......") ;
		//WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if( material == null ){
			logger.debug("   "+"delete Method, AlternativeMaterial is NULL");
			return ;
		}
		SqlMapClient sqlMap = null;
		sqlMap = IbatisUtil.getSqlMapInstance();
		material.setPdmWritebackStatus(IntegrationConstant.PDM_WRITE_BACK);
		try { 
			logger.debug("   "+"执行替代关系信息删除操作") ;
			sqlMap.delete(IbatisSqlConstant.DELETE_ALTERNATIVE_MATERIAL2, material);
			logger.debug("   "+"删除替代关系信息任务结束") ;
		}catch (SQLException e) {
			e.printStackTrace();
			//hashMap.put( usageLink, "删除EBOM发生异常：" + e.toString() );
			throw new WTException(e.getMessage());
			
		}finally{
			IbatisUtil.closeSqlMapInstance();
			logger.debug("   "+"Existed Out delete( AlternativeMaterial material )......") ;
		}
	}
	
	/**
	 * 在中间表中创建替代关系
	 * 
	 * @param material
	 * @param releaseTime
	 * @throws SQLException 
	 */
	protected static void create( AlternativeMaterial material , String releaseTime ) throws SQLException {
		logger.debug("   "+"Entering In create( AlternativeMaterial material , String releaseTime )......") ;
		
		if( material == null ){
			logger.debug("   "+"create Method, AlternativeMaterial is NULL");
			return ;
		}
		SqlMapClient sqlMap = null;
	    sqlMap = IbatisUtil.getSqlMapInstance();
	    
		try {
			logger.debug("   "+"执行替代关系信息创建任务") ;
			sqlMap.insert(IbatisSqlConstant.INSERT_ALTERNATIVE_MATERIAL, material);
			logger.debug("   "+"创建替代关系信息任务结束") ;
		} catch (SQLException e) {
			
			e.printStackTrace();
			throw new SQLException(e.getMessage());
		}finally{
			IbatisUtil.closeSqlMapInstance();
			logger.debug("   "+"Existed Out create( AlternativeMaterial material , String releaseTime )......") ;
		}
	}
	
	/**
	 * 在中间表中创建替代关系
	 * 
	 * @param material
	 * @param releaseTime
	 * @throws SQLException 
	 */
	public static void create( AlternativeMaterial material , String releaseTime,SqlMapClient sqlMap ) throws SQLException {
		logger.debug("   "+"Entering In create( AlternativeMaterial material , String releaseTime )......") ;
		
		if( material == null ){
			logger.debug("   "+"create Method, AlternativeMaterial is NULL");
			return ;
		}
		
		logger.debug("   "+"执行替代关系信息创建任务") ;
		sqlMap.insert(IbatisSqlConstant.INSERT_ALTERNATIVE_MATERIAL, material);
		logger.debug("   "+"创建替代关系信息任务结束") ;
	}
	
	/**
	 * 更新中间表中的替代关系
	 * 
	 * @param material
	 * @param releaseTime
	 * @throws IEException 
	 * @throws WTException 
	 * @throws IOException 
	 * @throws SQLException 
	 */
	protected static void update( AlternativeMaterial material , String releaseTime ) throws SQLException{
		logger.debug("   "+"Entering In update( AlternativeMaterial material , String releaseTime )......") ;
		
		if( material == null ){
			logger.debug("   "+"update Method, AlternativeMaterial is NULL");
			return ;
		}	
		
		material.setFlag(IntegrationConstant.PDM_RELEASE_STATUS);
		SqlMapClient sqlMap = null;
	    sqlMap = IbatisUtil.getSqlMapInstance();
		try {
			logger.debug("   "+"执行替代关系信息更新任务") ;
			
			sqlMap.update(IbatisSqlConstant.UPDATE_ALTERNATIVE_MATERIAL, material);
			
			logger.debug("   "+"更新替代关系信息任务结束") ;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e.getMessage());
		}finally{
			IbatisUtil.closeSqlMapInstance();
			logger.debug("   "+"Existed Out update( AlternativeMaterial material , String releaseTime )......") ;
		}
	}
	
	/**
	 * 更新中间表中的替代关系
	 * 
	 * @param material
	 * @param releaseTime
	 * @throws IEException 
	 * @throws WTException 
	 * @throws IOException 
	 * @throws SQLException 
	 */
	protected static void update( AlternativeMaterial material , String releaseTime ,SqlMapClient sqlMap) throws SQLException{
		logger.debug("   "+"Entering In update( AlternativeMaterial material , String releaseTime )......") ;
		
		if( material == null ){
			logger.debug("   "+"update Method, AlternativeMaterial is NULL");
			return ;
		}	
		logger.debug("   "+"执行替代关系信息更新任务") ;
		
		sqlMap.update(IbatisSqlConstant.UPDATE_ALTERNATIVE_MATERIAL, material);
		
		logger.debug("   "+"更新替代关系信息任务结束") ;
	}
}
