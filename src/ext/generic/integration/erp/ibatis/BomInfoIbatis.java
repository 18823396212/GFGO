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

import com.ibatis.sqlmap.client.SqlMapClient;
import com.infoengine.util.IEException;

import ext.generic.integration.erp.attributes.AttributeUtil;
import ext.generic.integration.erp.attributes.PartAttribute;
import ext.generic.integration.erp.bean.BOMRootInfo;
import ext.generic.integration.erp.service.BOMReleaseService;
import ext.generic.integration.erp.util.GenerateBeanInfo;
import ext.generic.integration.erp.util.IbatisUtil;
import ext.generic.integration.erp.util.IntegrationConstant;
import wt.change2.WTChangeOrder2;
import wt.fc.WTObject;
import wt.fc.collections.WTKeyedHashMap;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.util.WTException;
/**
 * BOM节点信息IBATIS类
 * 
 */
public class BomInfoIbatis {
	
	private static final String clazz = BomInfoIbatis.class.getName() ;
	
	private static final Logger logger = LogR.getLogger(clazz);
	
	/**
	 * 查询BOM根节点信息
	 * 
	 * @param part
	 * @return
	 * @throws WTException 
	 * @throws SQLException 
	 * @throws IEException 
	 * @throws IOException 
	 */
	public static List query(WTPart part) throws WTException, SQLException{
		logger.debug("   "+"Entering In query(WTPart part)......") ;
		
		List list = new ArrayList() ;
		SqlMapClient sqlMap = null;
		sqlMap = IbatisUtil.getSqlMapInstance();
		BOMRootInfo info = new BOMRootInfo();
		info.setNumber(AttributeUtil.getAttribute(part, PartAttribute.NUMBER));
		info.setMajorVersion(AttributeUtil.getAttribute(part, PartAttribute.MAJOR_VERSION));
		info.setView(AttributeUtil.getAttribute(part, PartAttribute.VIEW));
		//执行任务
		try {
			logger.debug("   "+"准备执行BOM Root信息查询Task......") ;
			list= sqlMap.queryForList(IbatisSqlConstant.QUERY_BOM_ROOT_INFO, info);
			logger.debug("   "+"BOM Root信息查询Task执行完成......") ;
		}finally{
			IbatisUtil.closeSqlMapInstance();
		}
		
		logger.debug("   "+"Existed Out query(WTPart part)......") ;
		
		return list ;
	}
	
	/**
	 * 查询BOM根节点信息
	 * 
	 * @param part
	 * @return
	 * @throws WTException 
	 * @throws SQLException 
	 * @throws IEException 
	 * @throws IOException 
	 */
	public static List query(WTPart part,SqlMapClient sqlMap ) throws WTException, SQLException{
		logger.debug("   "+"Entering In query(WTPart part)......") ;
		
		List list = new ArrayList() ;
		BOMRootInfo info = new BOMRootInfo();
		info.setNumber(AttributeUtil.getAttribute(part, PartAttribute.NUMBER));
		info.setMajorVersion(AttributeUtil.getAttribute(part, PartAttribute.MAJOR_VERSION));
		info.setView(AttributeUtil.getAttribute(part, PartAttribute.VIEW));
		//执行任务
		logger.debug("   "+"准备执行BOM Root信息查询Task......") ;
		list = sqlMap.queryForList(IbatisSqlConstant.QUERY_BOM_ROOT_INFO, info);
		logger.debug("   "+"BOM Root信息查询Task执行完成......") ;
		
		logger.debug("   "+"Existed Out query(WTPart part)......") ;
		
		return list ;
	}
	
	/**
	 * 判断BOM根节点信息是否已经发布
	 * 
	 * @param part
	 * @return
	 * @throws IEException 
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws WTException 
	 */
	public static boolean hasObject(WTPart part) throws WTException, SQLException{
		logger.debug("   "+"Entering In hasObject(WTPart part)......") ;
		
		List list = null;
		list = query( part );
		
		boolean hasBOMRootInfo = false;
		
		if(list!=null && list.size()>0){
			hasBOMRootInfo = true;
		}
		
		return hasBOMRootInfo ;
	}
	
	/**
	 * 判断BOM根节点信息是否已经发布
	 * 
	 * @param part
	 * @return
	 * @throws IEException 
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws WTException 
	 */
	public static boolean hasObject(WTPart part,SqlMapClient sqlMap) throws WTException, SQLException{
		logger.debug("   "+"Entering In hasObject(WTPart part)......") ;
		boolean hasBOMRootInfo = false;
		
		List list = null;
		list = query( part, sqlMap );
		
		if(list!=null&&list.size()>0){
			hasBOMRootInfo = true;
		}
		return hasBOMRootInfo ;
	}
	
	/**
	 * 在中间表中添加BOM根节点信息
	 * 
	 * @param part
	 * @param ecnNumber
	 */
	public static void add(WTPart part , String ecnNumber){
		add( part , "" , "", ecnNumber) ;
	}
	
	/**
	 * 在中间表中添加BOM根节点信息
	 * 
	 * @param part
	 * @param releaseTime
	 * @param ecnNumber
	 */
	public static WTKeyedHashMap add(WTPart part , String releaseTime , String batchNumber, String ecnNumber){
		logger.debug("   "+"Entering In add(WTPart part , String releaseTime , String ecnNumber)......") ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		boolean hasBOMRootInfo = false;
		try {
			hasBOMRootInfo = hasObject( part );
		} catch (Exception e) {
			hashMap.put( part , "查询BOMRoot表时发生异常：" + e.toString() );
			e.printStackTrace();
			
			return hashMap;
		}
		logger.debug("   "+"hasBOMRootInfo = " + hasBOMRootInfo ) ;
		if( hasBOMRootInfo ){
			logger.debug("   "+"执行更新BOM Root信息操作") ;
			try {
				hashMap.putAll(update(part , releaseTime , batchNumber)) ;
			} catch (SQLException e) {
				hashMap.put( part , "更新数据到BOMRoot表时发生异常：" + e.toString() );
				e.printStackTrace();
			}
		}else{
			logger.debug("   "+"执行创建BOM Root信息操作") ;
			try {
				hashMap.putAll(create(part , releaseTime , batchNumber,  ecnNumber)) ;
			}catch (SQLException e) {
				hashMap.put( part , "发布数据到BOMRoot表时发生异常：" + e.toString() );
				
				e.printStackTrace();
			}
		}
		
		logger.debug("   "+"Existed Out add(WTPart part , String releaseTime , String ecnNumber)......") ;
		return hashMap;
	}
	
	/**
	 * 在中间表中添加BOM根节点信息
	 * 
	 * @param part
	 * @param releaseTime
	 * @param ecnNumber
	 */
	public static WTKeyedHashMap add(WTPart part , String releaseTime ,String batchNumber,  WTObject wtobj,SqlMapClient sqlMap){
		logger.debug("   "+"Entering In add(WTPart part , String releaseTime , String ecnNumber)......") ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		try {
			hashMap.putAll(create(part , releaseTime , batchNumber, wtobj, sqlMap)) ;
		}catch (SQLException e) {
			hashMap.put( part , "发布数据到BOMRoot表时发生异常：" + e.toString() );
			
			e.printStackTrace();
		}
		
		logger.debug("   "+"Existed Out add(WTPart part , String releaseTime , String ecnNumber)......") ;
		return hashMap;
	}
	
	/**
	 * 删除中间表中的BOM根节点信息
	 * 
	 * @param part
	 * @throws SQLException 
	 */
	public static WTKeyedHashMap delete(WTPart part) throws SQLException{
		
		logger.debug("   "+"Entering In delete(WTPart part)......") ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if( part == null ){
			logger.debug("   "+"delete Method, Part is NULL");
			return hashMap;
		}
		BOMRootInfo info  = new BOMRootInfo();
		try {
			info.setNumber(AttributeUtil.getAttribute(part, PartAttribute.NUMBER));
			info.setMajorVersion(AttributeUtil.getAttribute(part, PartAttribute.MAJOR_VERSION));
			info.setView(AttributeUtil.getAttribute(part, PartAttribute.VIEW));
			info.setPdmWritebackStatus( IntegrationConstant.PDM_WRITE_BACK );
		} catch (WTException e1) {
			
			e1.printStackTrace();
			hashMap.put(part, "生成BOMRootInfo对象错误："+e1.getMessage());
			return hashMap;
		}
		
		SqlMapClient sqlMap = null;
		sqlMap = IbatisUtil.getSqlMapInstance();
		try {
			sqlMap.startTransaction();
			logger.debug("   "+"执行BOM Root信息删除操作") ;
			sqlMap.delete(IbatisSqlConstant.DELETE_BOM_ROOT_INFO, info);
			sqlMap.commitTransaction(); 
			logger.debug("   "+"删除BOM Root信息任务结束") ;
		}catch (SQLException e) {
			
			hashMap.put(part, "删除该父件信息出错："+e.getMessage());
			e.printStackTrace();
		} finally{
			sqlMap.endTransaction();
			IbatisUtil.closeSqlMapInstance();
			logger.debug("   "+"Existed Out delete(WTPart part)......") ;
		}
		return hashMap;
	}
	
	/**
	 * 在中间表中创建BOM根节点记录
	 * 
	 * @param part
	 * @param releaseTime
	 * @param ecnNumber
	 * @throws SQLException 
	 * @throws IEException 
	 * @throws IOException 
	 * @throws WTException 
	 */
	protected static WTKeyedHashMap create(WTPart part , String releaseTime , String batchNumber,  String ecnNumber ) throws SQLException{
		logger.debug("   "+"Entering In create(WTPart part , String releaseTime , String ecnNumber )......") ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if( part == null ){
			logger.debug("   "+"create Method, Part is NULL");
			return hashMap;
		}
		
		BOMRootInfo info = null;;
		try {
			info = GenerateBeanInfo.createBOMRootInfoBeanByPart(part, releaseTime, batchNumber);
		}catch (Exception e) {
			hashMap.put( part, "生成BOMRootInfo对象错误：" + e.toString() );
			e.printStackTrace();
			return hashMap;
		}
		info.setEcnNumber(ecnNumber);
		info.setFlag(IntegrationConstant.PDM_RELEASE_STATUS );
		SqlMapClient sqlMap = null;
	    sqlMap = IbatisUtil.getSqlMapInstance();
		try {
			logger.debug("   "+"执行BOM Root信息创建任务") ;
			sqlMap.startTransaction();
			sqlMap.insert(IbatisSqlConstant.INSERT_BOM_ROOT_INFO, info);
			sqlMap.commitTransaction();
			logger.debug("   "+"创建BOM Root信息任务结束") ;
		} catch (SQLException e) {
			hashMap.put( part, "插入BOM父节点发生异常：" + e.toString() );
			e.printStackTrace();
		}finally{
			sqlMap.endTransaction();
	    	IbatisUtil.closeSqlMapInstance();
			logger.debug("   "+"Existed Out create(WTPart part , String releaseTime , String ecnNumber )......") ;
		}
		return hashMap;
	}
	
	/**
	 * 在中间表中创建BOM根节点记录
	 * 
	 * @param part
	 * @param releaseTime
	 * @param ecnNumber
	 * @throws SQLException 
	 * @throws IEException 
	 * @throws IOException 
	 * @throws WTException 
	 */
	protected static WTKeyedHashMap create(WTPart part , String releaseTime , String batchNumber, WTObject wtobj,SqlMapClient sqlMap ) throws SQLException{
		logger.debug("   "+"Entering In create(WTPart part , String releaseTime , String ecnNumber )......") ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if( part == null ){
			logger.debug("   "+"create Method, Part is NULL");
			return hashMap;
		}
		
		BOMRootInfo info = null;;
		try {
			info = GenerateBeanInfo.createBOMRootInfoBeanByPart(part, releaseTime, batchNumber);
		}catch (Exception e) {
			hashMap.put( part, "生成BOMRootInfo对象错误：" + e.toString() );
			e.printStackTrace();
			return hashMap;
		}
		WTChangeOrder2 ecn = BOMReleaseService.getECNByWTObj(wtobj);
		if(ecn != null){
			info.setEcnNumber(ecn.getNumber());
		}
		
		try {
			logger.debug("   "+"执行BOM Root信息创建任务") ;
			sqlMap.insert(IbatisSqlConstant.INSERT_BOM_ROOT_INFO, info);
			logger.debug("   "+"创建BOM Root信息任务结束") ;
		} catch (SQLException e) {
			hashMap.put( part, "插入BOM父节点发生异常：" + e.toString() );
			e.printStackTrace();
		}
		return hashMap;
	}
	
	
	/**
	 * 在中间表中更新BOM根节点记录
	 * 
	 * @param part
	 * @param releaseTime
	 * @throws IEException 
	 * @throws SQLException 
	 * @throws IOException 
	 * @throws WTException 
	 */
	protected static WTKeyedHashMap update(WTPart part , String releaseTime, String batchNumber) throws  SQLException{
		logger.debug("   "+"Entering In update(WTPart part , String releaseTime)......") ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if( part == null ){
			logger.debug("   "+"update Method, Part is NULL");
			return hashMap;
		}
		BOMRootInfo info = null;;
		try {
			info = GenerateBeanInfo.createBOMRootInfoBeanByPart(part, releaseTime, batchNumber);
		}catch (Exception e) {
			hashMap.put( part, "生成BOMRootInfo对象错误：" + e.toString() );
			e.printStackTrace();
			return hashMap;
		}
		
		SqlMapClient sqlMap = null;
	    sqlMap = IbatisUtil.getSqlMapInstance();
		
		try {
			logger.debug("   "+"执行BOM Root信息更新任务") ;
			sqlMap.startTransaction();
			sqlMap.update(IbatisSqlConstant.UPDATE_BOM_ROOT_INFO, info);
			sqlMap.commitTransaction();
			logger.debug("   "+"更新BOM Root信息任务结束") ;
		} catch (SQLException e) {
			hashMap.put( part, "更新BOM父节点发生异常：" + e.toString() );
			e.printStackTrace();
			throw new SQLException(e);
		} finally{
			sqlMap.endTransaction();
	    	IbatisUtil.closeSqlMapInstance();
			logger.debug("   "+"Existed Out update(WTPart part , String releaseTime)......") ;
		}
		return hashMap;
	}
	
	/**
	 * 在中间表中更新BOM根节点记录
	 * 
	 * @param part
	 * @param releaseTime
	 * @throws IEException 
	 * @throws SQLException 
	 * @throws IOException 
	 * @throws WTException 
	 */
	protected static WTKeyedHashMap update(WTPart part , String releaseTime, String batchNumber, SqlMapClient sqlMap) throws  SQLException{
		logger.debug("   "+"Entering In update(WTPart part , String releaseTime)......") ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if( part == null ){
			logger.debug("   "+"update Method, Part is NULL");
			return hashMap;
		}
		BOMRootInfo info = null;;
		try {
			info = GenerateBeanInfo.createBOMRootInfoBeanByPart(part, releaseTime, batchNumber);
		}catch (Exception e) {
			hashMap.put( part, "生成BOMRootInfo对象错误：" + e.toString() );
			e.printStackTrace();
			return hashMap;
		}
		
		try {
			logger.debug("   "+"执行BOM Root信息更新任务") ;
			sqlMap.update(IbatisSqlConstant.UPDATE_BOM_ROOT_INFO, info);
			logger.debug("   "+"更新BOM Root信息任务结束") ;
		} catch (SQLException e) {
			hashMap.put( part, "更新BOM父节点发生异常：" + e.toString() );
			e.printStackTrace();
			throw new SQLException(e);
		}
		return hashMap;
	}
	
	/**
	 * 查询中间表中ERP已经处理过的BOM表头信息
	 * @return 满足条件的BOM表头信息
	 * @throws WTException
	 * @throws SQLException 
	 */
	public static List selectERPReceivedBOMInfo() throws  SQLException {
		SqlMapClient sqlMap = null;
		sqlMap = IbatisUtil.getSqlMapInstance();
		List list=new ArrayList();
		if (sqlMap!=null){
			try{
				list = sqlMap.queryForList(IbatisSqlConstant.GET_ERP_RECEIVED_BOMINFO);
			}catch (SQLException e){
				e.printStackTrace();
				throw new SQLException(e.fillInStackTrace().getMessage());
			}
		}
		IbatisUtil.closeSqlMapInstance();
		return	list;
	}
	
	/**
	 * 更新WriteBack信息
	 * @param materialInfo
	 * @return
	 * @throws WTException
	 * @throws SQLException 
	 */
	public static int updateMaterialWriteBackInfo(BOMRootInfo bomRootInfo) throws SQLException {
		SqlMapClient sqlMap = null;
		int result = 0;
	    sqlMap = IbatisUtil.getSqlMapInstance();
	    if (sqlMap != null){
	      try{
	    	  sqlMap.startTransaction();
	    	  result = sqlMap.update(IbatisSqlConstant.UPDATE_BOMINFO_WRITEBACK, bomRootInfo);
	    	  sqlMap.commitTransaction();
	      }
	      catch (SQLException e){
	        e.printStackTrace();
	        throw new SQLException(e.getMessage());
	      }finally{
	    	  sqlMap.endTransaction();
	    	  IbatisUtil.closeSqlMapInstance();
	      }
	    }
		return result;
	}
	
}
