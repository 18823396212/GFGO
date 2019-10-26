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
import java.util.Map;

import org.apache.log4j.Logger;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.infoengine.util.IEException;

import ext.generic.integration.erp.attributes.AttributeUtil;
import ext.generic.integration.erp.attributes.ECNAttribute;
import ext.generic.integration.erp.bean.ECNInfo;
import ext.generic.integration.erp.util.GenerateBeanInfo;
import ext.generic.integration.erp.util.IbatisUtil;
import ext.generic.integration.erp.util.IntegrationConstant;
import wt.change2.ChangeOrder2;
import wt.change2.WTChangeOrder2;
import wt.fc.collections.WTKeyedHashMap;
import wt.log4j.LogR;
import wt.util.WTException;
/**
 * ECN信息Info*E Task类
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-10-08
 */
public class ECNIbatis {
	
	private static final String clazz = ECNIbatis.class.getName() ;
	
	private static final Logger logger = LogR.getLogger(clazz);
	
	private static Map<String,String> fieldMap  = null;
	
//	static {
//		getFieldMap();
//	}
	
//	public static void  getFieldMap(){
//		QueryXMLConfigUtil configUtil =  new QueryXMLConfigUtil(IETaskCommonUtil.getConfigFilePath()+QueryECNAttributes.CONFIG_XML_FILE);
//		fieldMap = configUtil.getAttMapping();
//	}
	/**
	 * 查询中间表中的ECN信息
	 * 
	 * @param ecn
	 * @return
	 * @throws WTException 
	 * @throws SQLException 
	 * @throws IEException 
	 * @throws IOException 
	 */
	public static List query(ChangeOrder2 ecn) throws WTException, SQLException{
		logger.debug("   "+"Entering In query(ChangeOrder2 ecn)......") ;
		List list = new ArrayList() ;
		SqlMapClient sqlMap = null;
		sqlMap = IbatisUtil.getSqlMapInstance();
		ECNInfo info =  new ECNInfo();
		info.setNumber(AttributeUtil.getAttribute(ecn, ECNAttribute.NUMBER));
		//执行任务
		try {
			logger.debug("   "+"准备执行ECN信息查询Task......") ;
			
			list = sqlMap.queryForList(IbatisSqlConstant.QUERY_ECN__INFO, info);
			
			logger.debug("   "+"ECN信息查询Task执行完成......") ;
		}finally{
			IbatisUtil.closeSqlMapInstance();
		}
		//获取返回值
		logger.debug("   "+"Existed Out query(ChangeOrder2 ecn)......") ;
		
		return list ;
	}

	/**
	 * 判断中间表是否有ECN
	 * 
	 * @param ecn
	 * @return
	 * @throws SQLException 
	 * @throws WTException 
	 * @throws IEException 
	 * @throws IOException 
	 */
	public static boolean hasObject(ChangeOrder2 ecn) throws WTException, SQLException{
		logger.debug("   "+"Entering In hasObject(ChangeOrder2 ecn)......") ;
		
		List list = null;
		list = query( ecn );
		
		boolean hasECN = false;
		if(list != null && list.size() > 0 ){
			hasECN = true;
		}
		logger.debug("   "+"hasECN = " + hasECN ) ;
		logger.debug("   "+"Existed Out hasObject(ChangeOrder2 ecn)......") ;
		
		return hasECN ;
	}
	
	/**
	 * 向中间表添加ECN
	 * 
	 * @param ecn
	 */
	public static void add( WTChangeOrder2 ecn ){
		add( ecn , "" , "") ;
	}
	
	/**
	 * 向中间表添加ECN
	 * 
	 * @param ecn
	 * @param releaseTime
	 * @param batchNumber
	 */
	public static WTKeyedHashMap add(WTChangeOrder2 ecn , String releaseTime, String batchNumber){
		logger.debug("   "+"Entering In add(ChangeOrder2 ecn , String releaseTime)......") ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		boolean hasECN = false;
		try {
			hasECN = hasObject( ecn );
		} catch (Exception e) {
			hashMap.put(ecn, "查询ECN信息出错："+e.getMessage());
			e.printStackTrace();
		}
		logger.debug("   "+"hasECN = " + hasECN ) ;
		if( hasECN ){
			//已存在，暂时不更新
//			logger.debug("   "+"执行更新ECN信息操作") ;
//			try {
//				update( ecn , releaseTime , batchNumber) ;
//			} catch (SQLException e) {
//				hashMap.put(ecn, "更新ECN信息出错："+e.getMessage());
//				e.printStackTrace();
//			}
		}else{
			logger.debug("   "+"执行创建ECN信息操作") ;
			try {
				create( ecn , releaseTime , batchNumber) ;
			} catch (SQLException e) {
				hashMap.put(ecn, "插入ECN信息出错："+e.getMessage());
				e.printStackTrace();
			}
		}
		
		logger.debug("   "+"Existed Out add(ChangeOrder2 ecn , String releaseTime)......") ;
		return hashMap;
	}
	
	/**
	 * 从中间表删除ECN信息
	 * 
	 * @param ecn
	 * @throws SQLException 
	 */
	public static WTKeyedHashMap delete( ChangeOrder2 ecn ) throws SQLException{
		logger.debug("   "+"Entering In delete( ChangeOrder2 ecn )......") ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if( ecn == null ){
			logger.debug("   "+"delete Method, ecn is NULL");
			return hashMap ;
		}
		//创建任务
		ECNInfo info = new ECNInfo();
		try {
			info.setNumber(AttributeUtil.getAttribute(ecn, ECNAttribute.NUMBER));
		} catch (WTException e1) {
			hashMap.put(ecn, "生成ECN对象出错："+e1.getMessage());
			e1.printStackTrace();
		}
		info.setPdmWritebackStatus(IntegrationConstant.PDM_WRITE_BACK);
		
		SqlMapClient sqlMap = null;
		sqlMap = IbatisUtil.getSqlMapInstance();
		
		try {
			logger.debug("   "+"执行ECN信息删除操作") ;
			sqlMap.startTransaction();
			sqlMap.delete(IbatisSqlConstant.DELETE_ECN_INFO, info);
			sqlMap.commitTransaction(); 
			logger.debug("   "+"删除ECN信息任务结束") ;
		}catch (SQLException e) {
			
			hashMap.put(ecn, "删除ECN信息出错："+e.getMessage());
			e.printStackTrace();
		}  finally{
			sqlMap.endTransaction();
			IbatisUtil.closeSqlMapInstance();
			logger.debug("   "+"Existed Out delete( ChangeOrder2 ecn )......") ;
		}
		return hashMap;
	}

	/**
	 * 在中间表中创建ECN信息
	 * 
	 * @param ecn
	 * @param releaseTime
	 * @throws SQLException 
	 * @throws IEException 
	 * @throws IOException 
	 */
	protected static WTKeyedHashMap create( WTChangeOrder2 ecn , String releaseTime , String batchNumber) throws SQLException  {
		logger.debug("   "+"Entering In create( ChangeOrder2 ecn , String releaseTime )......") ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if( ecn == null ){
			logger.debug("   "+"create Method, ECN is NULL");
			return hashMap;
		}
		
		ECNInfo info = new ECNInfo();
		try {
			info = GenerateBeanInfo.createECNnfoByECN(ecn, releaseTime, batchNumber);
		} catch (Exception e1) {
			hashMap.put(ecn, "生成ECNInfo对象出错："+e1.getMessage());
			e1.printStackTrace();
		} 
		
		if(info != null){
			SqlMapClient sqlMap = null;
		    sqlMap = IbatisUtil.getSqlMapInstance();
			try {
				logger.debug("   "+"执行ECN信息创建任务") ;
				
				sqlMap.startTransaction();
				sqlMap.insert(IbatisSqlConstant.INSERT_ECN_INFO, info);
				sqlMap.commitTransaction();
				
				logger.debug("   "+"创建ECN信息任务结束") ;
			} catch (SQLException e) {
				hashMap.put(ecn, "写入ECN记录出错："+e.getMessage());
				e.printStackTrace();
			} finally{
				sqlMap.endTransaction();
		    	IbatisUtil.closeSqlMapInstance();
				logger.debug("   "+"Existed Out create( ChangeOrder2 ecn , String releaseTime )......") ;
			}
		}
		return hashMap;
	}
	
	/**
	 * 更新中间表的ECN信息
	 * 
	 * @param ecn
	 * @param releaseTime
	 * @param batchNumber 批次号
	 * @throws SQLException 
	 * @throws IEException 
	 * @throws IOException 
	 */
	protected static WTKeyedHashMap update(WTChangeOrder2 ecn , String releaseTime , String batchNumber) throws SQLException{
		logger.debug("   "+"Entering In update( ChangeOrder2 ecn , String releaseTime )......") ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if( ecn == null ){
			logger.debug("   "+"update Method, ECN is NULL");
			return hashMap;
		}
		
		ECNInfo info = null;
		try {
			info = GenerateBeanInfo.createECNnfoByECN(ecn, releaseTime, batchNumber);
		} catch (Exception e1) {
			hashMap.put(ecn, "生成ECNInfo对象出错："+e1.getMessage());
			e1.printStackTrace();
		}
		if(info != null){
			
			SqlMapClient sqlMap = null;
		    sqlMap = IbatisUtil.getSqlMapInstance();
			try {
				logger.debug("   "+"执行ECN信息更新任务") ;
				
				sqlMap.startTransaction();
				sqlMap.update(IbatisSqlConstant.UPDATE_ECN_INFO, info);
				sqlMap.commitTransaction();
				
				logger.debug("   "+"更新ECN信息任务结束") ;
			} catch (SQLException e) {
				hashMap.put(ecn, "写入ECN记录出错："+e.getMessage());
				e.printStackTrace();
			} finally{
				sqlMap.endTransaction();
		    	IbatisUtil.closeSqlMapInstance();
				logger.debug("   "+"Existed Out update( ChangeOrder2 ecn , String releaseTime )......") ;
			}
		}
		return hashMap;
	}
}
