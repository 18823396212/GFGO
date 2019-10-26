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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import wt.fc.WTObject;
import wt.fc.collections.WTKeyedHashMap;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.util.WTException;

import com.ibatis.sqlmap.client.SqlMapClient;

import ext.com.core.CoreUtil;
import ext.generic.integration.erp.attributes.AttributeUtil;
import ext.generic.integration.erp.attributes.PartAttribute;
import ext.generic.integration.erp.attributes.PartUsageLinkAttribute;
import ext.generic.integration.erp.bean.EBOMInfo;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.generic.integration.erp.common.CommonUtil;
import ext.generic.integration.erp.rule.BussinessRule;
import ext.generic.integration.erp.service.IbatisReplacementReleaseService;
import ext.generic.integration.erp.service.UpdatePDMDataService;
import ext.generic.integration.erp.util.BusinessRuleXMLConfigUtil;
import ext.generic.integration.erp.util.GenerateBeanInfo;
import ext.generic.integration.erp.util.IbatisUtil;
import ext.generic.integration.erp.util.IntegrationConstant;
import ext.generic.integration.erp.util.PDMIntegrationLogUtil;
/**
 * BOM结构信息IBatis类
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-10-08
 */
public class BomStructureIbatis {
	
	private static final String clazz = BomStructureIbatis.class.getName() ;
	
	private static final Logger logger = LogR.getLogger(clazz);
	
//	public static Map<String,String> fieldMap  = null;
	
	/**
	 * 查询中间表中的BOM结构关系
	 * 
	 * @param usageLink
	 * @return
	 * @throws IEException 
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws WTException 
	 */
	public static List query( WTPartUsageLink usageLink ) throws SQLException, WTException{
		logger.debug("   "+"Entering In query( WTPartUsageLink usageLink )......") ;
		List list  = new ArrayList();
		if( usageLink == null ){
			logger.debug("   "+"delete Method,WTPartUsageLink is NULL");
			return list;
		}
		EBOMInfo ebominfo = new EBOMInfo();
		ebominfo.setParentNumber(AttributeUtil.getAttribute(usageLink, PartUsageLinkAttribute.PARENT_PART_NUMBER));
		ebominfo.setParentVersion(AttributeUtil.getAttribute(usageLink, PartUsageLinkAttribute.PARENT_PART_VERSION));
		ebominfo.setParentView(AttributeUtil.getAttribute(usageLink, PartUsageLinkAttribute.PARENT_PART_VIEW));
//		ebominfo.setLineNumber(AttributeUtil.getAttribute(usageLink, PartUsageLinkAttribute.LINE_NUMBER));
		ebominfo.setChildNumber(AttributeUtil.getAttribute(usageLink, PartUsageLinkAttribute.CHILD_PART_NUMBER));
		SqlMapClient sqlMap = null;
	    sqlMap = IbatisUtil.getSqlMapInstance();
	    if (sqlMap != null){
	    	try{
	    		logger.debug("   "+"执行BOM结构关系信息查询操作") ;
	    		sqlMap.startTransaction();
	    		list = sqlMap.queryForList(IbatisSqlConstant.QUERY_EBOMRECOMD_BY_CONDITION, ebominfo);
	    		sqlMap.commitTransaction();
	    		logger.debug("   "+"查询BOM结构关系信息任务结束") ;
	    	}
	    	catch (SQLException e){
	    		e.printStackTrace();
	    		throw new SQLException(e.getMessage());
	     	}finally{
	     		sqlMap.endTransaction();
	     		IbatisUtil.closeSqlMapInstance();
	     	}
	    	
	    }
	    return list;
	}
	
	/**
	 * 查询中间表中的BOM结构关系
	 * 
	 * @param usageLink
	 * @return
	 * @throws IEException 
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws WTException 
	 */
	public static List query( WTPartUsageLink usageLink,SqlMapClient sqlMap ) throws SQLException, WTException{
		logger.debug("   "+"Entering In query( WTPartUsageLink usageLink )......") ;
		List list  = new ArrayList();
		if( usageLink == null ){
			logger.debug("   "+"delete Method,WTPartUsageLink is NULL");
			return list;
		}
		EBOMInfo ebominfo = new EBOMInfo();
		ebominfo.setParentNumber(AttributeUtil.getAttribute(usageLink, PartUsageLinkAttribute.PARENT_PART_NUMBER));
		ebominfo.setParentMajorVersion(AttributeUtil.getAttribute(usageLink, PartUsageLinkAttribute.PARENT_PART_MAJOR_VERSION));
//		ebominfo.setParentVersion(AttributeUtil.getAttribute(usageLink, PartUsageLinkAttribute.PARENT_PART_VERSION));
//		ebominfo.setLineNumber(AttributeUtil.getAttribute(usageLink, PartUsageLinkAttribute.LINE_NUMBER));
		ebominfo.setChildNumber(AttributeUtil.getAttribute(usageLink, PartUsageLinkAttribute.CHILD_PART_NUMBER));
		//SqlMapClient sqlMap = null;
	    //sqlMap = IbatisUtil.getSqlMapInstance();
	    if (sqlMap != null){
	    	try{
	    		logger.debug("   "+"执行BOM结构关系信息查询操作") ;
	    		//sqlMap.startTransaction();
	    		list = sqlMap.queryForList(IbatisSqlConstant.QUERY_EBOMRECOMD_BY_CONDITION, ebominfo);
	    		//sqlMap.commitTransaction();
	    		logger.debug("   "+"查询BOM结构关系信息任务结束") ;
	    	}
	    	catch (SQLException e){
	    		e.printStackTrace();
	    		throw new SQLException(e.getMessage());
	     	}finally{
	     		//sqlMap.endTransaction();
	     		//IbatisUtil.closeSqlMapInstance();
	     	}
	    	
	    }
	    return list;
	}
	
	/**
	 * 判断中间表是否有BOM结构关系
	 * 
	 * @param usageLink
	 * @return
	 * @throws IEException 
	 * @throws IOException 
	 * @throws WTException 
	 * @throws SQLException 
	 */
	public static boolean hasObject( WTPartUsageLink usageLink ) throws SQLException, WTException{
		logger.debug("   "+"Entering In hasObject( WTPartUsageLink usageLink )......") ;
		
		List list = new ArrayList();
		try {
			list = query( usageLink );
		} catch (SQLException e) {
			throw new SQLException(e);
		}
		if(list!=null&&list.size()>0)
			return true;
		logger.debug("   "+"Existed Out hasObject( WTPartUsageLink usageLink )......") ;
		
		return false ;
	}
	
	/**
	 * 判断中间表是否有BOM结构关系
	 * 
	 * @param usageLink
	 * @return
	 * @throws IEException 
	 * @throws IOException 
	 * @throws WTException 
	 * @throws SQLException 
	 */
	public static boolean hasObject( WTPartUsageLink usageLink ,SqlMapClient sqlMap) throws SQLException, WTException{
		logger.debug("   "+"Entering In hasObject( WTPartUsageLink usageLink )......") ;
		
		List list = null;
		try {
			list = query( usageLink,sqlMap );
		} catch (SQLException e) {
			throw new SQLException(e);
		}
		if(list!=null&&list.size()>0)
			return true;
		logger.debug("   "+"Existed Out hasObject( WTPartUsageLink usageLink )......") ;
		
		return false ;
	}
	
	
	/**
	 * 向中间表添加BOM结构关系记录
	 * 
	 * @param usageLink
	 */
	public static void add( WTPartUsageLink usageLink ){
		add( usageLink , "" , "") ;
	}

	/**
	 * 向中间表添加BOM结构关系记录
	 * 
	 * @param usageLink
	 * @param releaseTime
	 * @throws WTException 
	 * @throws SQLException 
	 */
	public static WTKeyedHashMap add( WTPartUsageLink usageLink , String releaseTime , String batchNumber){
		logger.debug("   "+"Entering In add( WTPartUsageLink usageLink , String releaseTime  )......") ;
		
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		if(usageLink==null)
			return hashMap;
		boolean hasBOMStructure = false;
		try {
			hasBOMStructure = hasObject( usageLink );
			
		} catch (Exception ie) {
			//查询出现异常，程序终止运行
			hashMap.put(usageLink, "BOM查询报错：" + ie.toString() );
			
			ie.printStackTrace();
			
			return hashMap;
		} 
		logger.debug("   "+"hasBOMStructure = " + hasBOMStructure ) ;
		
		if( hasBOMStructure ){
			logger.debug("   "+"执行更新BOM结构关系信息操作") ;
			
			try {
				hashMap.putAll(update( usageLink , releaseTime , batchNumber)) ;
			} catch (SQLException e) {
				hashMap.put( usageLink , "更新BOM关系出错："+e.toString() );
				e.printStackTrace();
			}
			logger.debug("   "+"BOM以存在无需更新操作") ;
		}else{
			logger.debug("   "+"执行创建BOM结构关系信息操作") ;
			WTPart parentPart = usageLink.getUsedBy();
			
			//检查附件是否为特殊件，并判断用户是否需要发布特殊件（特殊件：虚拟件，收集件）
			if( CommonPDMUtil.checkPartSpecialHandling( parentPart ) ){
				return hashMap;
			}
			
			WTPart sonPart = null;
			try {
				sonPart = CoreUtil.getWTPartByMasterAndView( usageLink.getUses(), parentPart.getViewName() );
			} catch (WTException e) {
				e.printStackTrace();
				hashMap.put(usageLink, "获取子件报错：" + e.toString() );
				return hashMap;
			}
			
			if( sonPart != null ){
				//判断子件是否为特殊件，如果是特殊件判断用户是否需要发布
				if( CommonPDMUtil.checkPartSpecialHandling( sonPart ) ){
					try {
						hashMap.putAll( createPartSpecialHandlingData( usageLink , releaseTime, batchNumber ) );
					} catch (SQLException e) {
						hashMap.put( usageLink , e.toString() );
						e.printStackTrace();
					}
				}else{
					try {
						hashMap.putAll(create( usageLink , releaseTime ,batchNumber)) ;
					} catch (Exception e) {
						hashMap.put( usageLink , "创建BOM关系出错："+e.toString() );
						
						e.printStackTrace();
					} 
				}
			}
			
		}
		
		logger.debug("   "+"Existed Out add( WTPartUsageLink usageLink , String releaseTime  )......") ;
		
		return hashMap;
	}
	
	/**
	 * 向中间表添加BOM结构关系记录
	 * 
	 * @param usageLink
	 * @param releaseTime
	 * @throws WTException 
	 * @throws SQLException 
	 */
	public static WTKeyedHashMap add( WTPartUsageLink usageLink , String releaseTime , String batchNumber, SqlMapClient sqlMap){
		logger.debug("   "+"Entering In add( WTPartUsageLink usageLink , String releaseTime  )......") ;
		
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		if(usageLink==null)
			return hashMap;
		boolean hasBOMStructure = false;
		try {
			hasBOMStructure = hasObject( usageLink,sqlMap);
			
		} catch (Exception ie) {
			//查询出现异常，程序终止运行
			hashMap.put(usageLink, "BOM查询报错：" + ie.toString() );
			
			ie.printStackTrace();
			
			return hashMap;
		} 
		logger.debug("   "+"hasBOMStructure = " + hasBOMStructure ) ;
		
		if( hasBOMStructure ){
			logger.debug("   "+"执行更新BOM结构关系信息操作") ;
			
			try {
				hashMap.putAll(update( usageLink , releaseTime , batchNumber, sqlMap)) ;
			} catch (SQLException e) {
				hashMap.put( usageLink , "更新BOM关系出错："+e.toString() );
				e.printStackTrace();
			}
			logger.debug("   "+"BOM以存在无需更新操作") ;
		}else{
			logger.debug("   "+"执行创建BOM结构关系信息操作") ;
			WTPart parentPart = usageLink.getUsedBy();
			
			//检查附件是否为特殊件，并判断用户是否需要发布特殊件（特殊件：虚拟件，收集件）
			if( CommonPDMUtil.checkPartSpecialHandling( parentPart ) ){
				return hashMap;
			}
			
			WTPart sonPart = null;
			try {
				sonPart = CoreUtil.getWTPartByMasterAndView( usageLink.getUses(), parentPart.getViewName() );
			} catch (WTException e) {
				e.printStackTrace();
				hashMap.put(usageLink, "获取子件报错：" + e.toString() );
				return hashMap;
			}
			
			if( sonPart != null ){
				//判断子件是否为特殊件，如果是特殊件判断用户是否需要发布
				if( CommonPDMUtil.checkPartSpecialHandling( sonPart ) ){
					try {
						hashMap.putAll( createPartSpecialHandlingData( usageLink , releaseTime, batchNumber, sqlMap ) );
					} catch (SQLException e) {
						hashMap.put( usageLink , e.toString() );
						e.printStackTrace();
					}
				}else{
					try {
						hashMap.putAll(create( usageLink , releaseTime, batchNumber, sqlMap )) ;
					} catch (Exception e) {
						hashMap.put( usageLink , "创建BOM关系出错："+e.toString() );
						
						e.printStackTrace();
					} 
				}
			}
			
		}
		
		logger.debug("   "+"Existed Out add( WTPartUsageLink usageLink , String releaseTime  )......") ;
		
		return hashMap;
	}
	
	/**
	 * 向中间表添加BOM结构关系记录 考虑特殊件 【特殊件：虚拟件，收集件】
	 * 
	 * @param usageLink
	 * @param releaseTime
	 * @throws WTException 
	 * @throws SQLException 
	 */
	public static WTKeyedHashMap releaseBOMInfoSpecialHandling( List<WTPartUsageLink> usageLinkList  , String releaseTime , String batchNumber) throws SQLException{
		logger.debug("   "+"Entering In add( WTPartUsageLink usageLink , String releaseTime  )......") ;
		
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		if(usageLinkList==null||usageLinkList.size()==0)
			return hashMap;
		
		SqlMapClient sqlMap = null;
		sqlMap = IbatisUtil.getSqlMapInstance();
		try {
			sqlMap.startTransaction();
			sqlMap.startBatch();
			for( int i = 0 ; i < usageLinkList.size() ; i++ ){
				WTPartUsageLink usageLink = usageLinkList.get(i) ;
				logger.debug("   "+"发布BOM结构信息:\n" + CommonPDMUtil.getWTPartUsageLinkInfo(usageLink) ) ;
				boolean hasBOMStructure = false;
				try {
					hasBOMStructure = hasObject( usageLink,sqlMap);
					
				} catch (Exception ie) {
					//查询出现异常，程序终止运行
					hashMap.put(usageLink, "BOM查询报错：" + ie.toString() );
					
					ie.printStackTrace();
					
					return hashMap;
				} 
				logger.debug("   "+"hasBOMStructure = " + hasBOMStructure ) ;
				
				if( hasBOMStructure ){
					logger.debug("   "+"执行更新BOM结构关系信息操作") ;
					
					try {
						hashMap.putAll(update( usageLink , releaseTime, batchNumber, sqlMap)) ;
					} catch (SQLException e) {
						hashMap.put( usageLink , "更新BOM关系出错："+e.toString() );
						e.printStackTrace();
					}
					logger.debug("   "+"BOM以存在无需更新操作") ;
				}else{
					logger.debug("   "+"执行创建BOM结构关系信息操作") ;
					WTPart parentPart = usageLink.getUsedBy();
					
					//检查附件是否为特殊件，并判断用户是否需要发布特殊件（特殊件：虚拟件，收集件）
					if( CommonPDMUtil.checkPartSpecialHandling( parentPart ) ){
						return hashMap;
					}
					
					WTPart sonPart = null;
					try {
						sonPart = CoreUtil.getWTPartByMasterAndView( usageLink.getUses(), parentPart.getViewName() );
					} catch (WTException e) {
						e.printStackTrace();
						hashMap.put(usageLink, "获取子件报错：" + e.toString() );
						return hashMap;
					}
					
					if( sonPart != null ){
						//判断子件是否为特殊件，如果是特殊件判断用户是否需要发布
						if( CommonPDMUtil.checkPartSpecialHandling( sonPart ) ){
							try {
								hashMap.putAll( createPartSpecialHandlingData( usageLink , releaseTime, batchNumber, sqlMap ) );
							} catch (SQLException e) {
								hashMap.put( usageLink , e.toString() );
								e.printStackTrace();
							}
						}else{
							try {
								hashMap.putAll(create( usageLink , releaseTime, batchNumber,  sqlMap ) );
							} catch (Exception e) {
								hashMap.put( usageLink , "创建BOM关系出错："+e.toString() );
								
								e.printStackTrace();
							} 
						}
					}
				}
			}
			if(hashMap.size()==0){
				sqlMap.executeBatch();
				sqlMap.commitTransaction();
				for(int i=0;i<usageLinkList.size();i++){
					WTPartUsageLink link = (WTPartUsageLink)usageLinkList.get(i);
					UpdatePDMDataService.updatePDMSucessful( link.getUsedBy() , BusinessRuleXMLConfigUtil.getInstance().getPartReleaseStatus() );
				}
			}
		}finally{
			sqlMap.endTransaction();
			IbatisUtil.closeSqlMapInstance();
		}
		logger.debug("   "+"Existed Out add( WTPartUsageLink usageLink , String releaseTime  )......") ;
		
		return hashMap;
	}
	
	/**
	 * 只发布BOM信息  不考虑特殊件   【特殊件：虚拟件，收集件】
	 * @param usageLinkList
	 * @param releaseTime
	 * @param ecnNumber
	 * @return
	 * @throws SQLException
	 */
	public static WTKeyedHashMap releaseBOMInfo(List<WTPartUsageLink>  usageLinkList, String releaseTime, String batchNumber) throws SQLException{
		logger.debug("   releaseFirstLevel  "+"获取结构关系数量为：" + usageLinkList.size() ) ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if( usageLinkList == null||usageLinkList.size()==0 ){
			return hashMap;
		}
		SqlMapClient sqlMap = null;
		sqlMap = IbatisUtil.getSqlMapInstance();
		try {
			sqlMap.startTransaction();
			sqlMap.startBatch();
			for( int i = 0 ; i < usageLinkList.size() ; i++ ){
				WTPartUsageLink usageLink = usageLinkList.get(i) ;
				logger.debug("   "+"发布BOM结构信息:\n" + CommonPDMUtil.getWTPartUsageLinkInfo(usageLink) ) ;
				//获取子件
				WTPartMaster  childMaster = usageLink.getUses() ;
				WTPart childPart=null;;
				try {
					childPart = CoreUtil.getWTPartByMasterAndView(childMaster, usageLink.getUsedBy().getViewName());
				} catch (WTException e) {
					
					hashMap.put(childMaster, "没有获取到对应的物料版本");
					e.printStackTrace();
					continue;
				}
				
				//判断子件是否为可发布类型
				if( BussinessRule.canReleasePart( childPart ) ){
					EBOMInfo info = null;
					try {
						info = GenerateBeanInfo.generateEBOMInfoByUserLink(usageLink, releaseTime, batchNumber);
						info.setFlag(IntegrationConstant.PDM_RELEASE_STATUS);
					} catch (Exception e) {
						hashMap.put(usageLink, "生成EBOM对象出错："+e.getMessage());
						e.printStackTrace();
						continue;
					} 
					
					// modify on 20160523, 为提高执行效率，方便配置，这里不再执行查询，直接插入
//					List list=null;
//					try {
//						list = (ArrayList) sqlMap.queryForList(IbatisSqlConstant.QUERY_EBOMRECOMD_BY_CONDITION, info);
//					} catch (SQLException e) {
//						hashMap.put(usageLink, "查询EBOM对象出错："+e.getMessage());
//						e.printStackTrace();
//						continue;
//					}
//					if(list!=null&&list.size()>0){
//						try {
//							sqlMap.update(IbatisSqlConstant.UPDATE_EBOM_INFO, info);
//						} catch (SQLException e) {
//							hashMap.put(usageLink, "更新EBOM对象出错："+e.getMessage());
//							e.printStackTrace();
//							continue;
//						}
//					}else{
						try {
							sqlMap.insert(IbatisSqlConstant.INSERT_EBOM_INFO, info);
						} catch (SQLException e) {
							hashMap.put(usageLink, "创建EBOM对象出错："+e.getMessage());
							e.printStackTrace();
							continue;
						}
//					}
				}
			}
			if(hashMap.size()==0){
				sqlMap.executeBatch();
				sqlMap.commitTransaction();
				for(int i=0;i<usageLinkList.size();i++){
					WTPartUsageLink link = (WTPartUsageLink)usageLinkList.get(i);
					UpdatePDMDataService.updatePDMSucessful( link.getUsedBy() , BusinessRuleXMLConfigUtil.getInstance().getPartReleaseStatus() );
				}
			}
		}finally{
			sqlMap.endTransaction();
			IbatisUtil.closeSqlMapInstance();
		}
		
		return hashMap;
	}
	
	
	/**
	 * @throws SQLException 
	 * @throws Exception 
	 * @throws SQLException 
	 * 只发布BOM信息  不考虑特殊件   【特殊件：虚拟件，收集件】
	 * @param usageLinkList
	 * @param releaseTime
	 * @param ecnNumber
	 * @return
	 * @throws  
	 */
	public static WTKeyedHashMap releaseBOMInfoOfReplaceLink(WTPart parent,List<WTPartUsageLink>  usageLinkList, String releaseTime, String batchNumber, WTObject wtobj) throws SQLException{		
		WTKeyedHashMap hashMap = new WTKeyedHashMap(); 
		if( usageLinkList == null||usageLinkList.size()==0 ){
			return hashMap;
		}
		
		logger.debug("   releaseFirstLevel  "+"获取结构关系数量为 ：" + usageLinkList.size() ) ;
		int size = usageLinkList.size() ;
		SqlMapClient sqlMap = null;
		sqlMap = IbatisUtil.getSqlMapInstance();
		try {
			IbatisReplacementReleaseService ibatisReplacementReleaseService = new IbatisReplacementReleaseService();
			sqlMap.startTransaction();
			sqlMap.startBatch();
			for( int i = 0 ; i < size ; i++ ){
				WTPartUsageLink usageLink = usageLinkList.get(i) ;
				logger.debug("   "+"发布BOM结构信息:\n" + CommonPDMUtil.getWTPartUsageLinkInfo(usageLink) ) ;
				//获取子件
				WTPartMaster childMaster = usageLink.getUses() ;
				WTPart childPart = null;
				try {
					childPart = CoreUtil.getWTPartByMasterAndView(childMaster, parent.getViewName());
				} catch (WTException e) {
					
					e.printStackTrace();
					continue;
				}
				//判断子件是否为可发布类型
				if( BussinessRule.canReleasePart( childPart ) ){
					//modify on 20160523, 为提交效率及简化配置，直接插入所有子件Link
					//hashMap.putAll( BomStructureIbatis.add(usageLink, releaseTime,sqlMap) ) ;
					hashMap.putAll(create( usageLink , releaseTime, batchNumber, sqlMap ));
					//发布特定替代关系
					
					hashMap.putAll( ibatisReplacementReleaseService.releaseSubstitute( usageLink , releaseTime,batchNumber,sqlMap ) ) ;
					
					//发布全局替代关系
					if( BussinessRule.isAlternateLinkRelease() ){
						hashMap.putAll(ibatisReplacementReleaseService.releaseAlternate( usageLink , releaseTime,batchNumber, sqlMap )) ;
					}
				}
			}
			if(hashMap.size()==0){
				if( !CommonPDMUtil.checkPartSpecialHandling(parent) ){
					WTKeyedHashMap rootMap = BomInfoIbatis.add(parent, releaseTime, batchNumber, wtobj,sqlMap) ;
					System.out.println( "Debug   rootMap :" + rootMap );
					if( rootMap.size() > 0 ){
						hashMap.putAll( PDMIntegrationLogUtil.getErrorInfo(rootMap) );
						System.out.println( "Debug   hashMap01 : " + hashMap );
						return hashMap;
					}
				}
				logger.debug("   "+PDMIntegrationLogUtil.finishReleaseBOMLogInfo(parent)) ;
				sqlMap.executeBatch();
				sqlMap.commitTransaction();
				
			}
			//如果当前层的BOM结构已经成功发布，则更新系统的BOM发布状态
//			boolean hasBom = IETaskCommonUtil.hasBOMObject( BomStructureIbatis.isQuery( parent ) , size ) ; 
			if( BomStructureIbatis.isQuery( parent ).size() ==  size){
				UpdatePDMDataService.updatePDMSucessful( parent , BusinessRuleXMLConfigUtil.getInstance().getBomReleaseStatus() );
			}else{
				UpdatePDMDataService.updatePDMFailed( parent , BusinessRuleXMLConfigUtil.getInstance().getBomReleaseStatus() );
			}
		}catch (Exception e) {
			hashMap.put(parent, PDMIntegrationLogUtil.failedReleaseBOMLogInfo(parent));
			logger.debug("   "+PDMIntegrationLogUtil.failedReleaseBOMLogInfo(parent)) ;
			logger.debug("   "+e.getMessage() ) ;
			e.printStackTrace();
		}finally{
			sqlMap.endTransaction();
			IbatisUtil.closeSqlMapInstance();
		}
		
		return hashMap;
	}
	
	
	/**
	 * 创建特殊子健的BOM信息
	 * @param usageLink 使用关系
	 * @param releaseTime 发布时间
	 * @return 错误信息
	 * @throws SQLException
	 */
	private static WTKeyedHashMap createPartSpecialHandlingData( WTPartUsageLink usageLink , String releaseTime , String batchNumber) throws SQLException {
		logger.debug("   "+"Entering In createPhantom( WTPartUsageLink usageLink , String releaseTime )......") ;
		
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		//从IE Task的XML文件中解析属性映射关系
		//Map<String,String> fieldMap = ieXMLUtil.parserXml(IETaskCommonUtil.TASK_FILE_DIRECOTRY + ieXMLFileCreate );
		
		
//		logger.debug("   "+CommonUtil.getMapStringValue( fieldMap )  ) ;
		
		Hashtable< WTPartUsageLink , String > hashtable = CommonPDMUtil.getPartSpecialHandlingData( usageLink );
		
		if( hashtable != null && hashtable.size() > 0 ){
			Iterator< WTPartUsageLink > iterator = hashtable.keySet().iterator();
			SqlMapClient sqlMap = null;
			sqlMap = IbatisUtil.getSqlMapInstance();
			try{
				sqlMap.startTransaction();
				sqlMap.startBatch();
				while( iterator.hasNext() ){
					WTPartUsageLink sonLink = iterator.next();
					if( sonLink != null ){
						
						EBOMInfo info = null;
						try {
							info = GenerateBeanInfo.generateEBOMInfoByUserLink(sonLink, releaseTime, batchNumber);
						} catch (Exception e) {
							
							e.printStackTrace();
							hashMap.put( sonLink, "生成materialInfo对象失败：" + e.toString() );
							continue;
						}
						if(releaseTime == null || releaseTime.trim().equals("")){
							releaseTime = CommonUtil.getCurrentTime() ;
						}
						info.setReleaseDate(CommonUtil.getCurrentDate());
						info.setFlag(IntegrationConstant.PDM_RELEASE_STATUS);
						try {
							sqlMap.insert(IbatisSqlConstant.INSERT_EBOM_INFO, info);
						} catch (SQLException e) {
							hashMap.put( sonLink, "插入EBOM发生异常：" + e.toString() );
							e.printStackTrace();
							continue;
						}
						
					}
				}
				if(hashMap.size()==0){
					sqlMap.executeBatch();
					sqlMap.commitTransaction();
				}
				
			}finally{
				sqlMap.endTransaction();
				IbatisUtil.closeSqlMapInstance();
			}
			
		}
		return hashMap;
	}

	/**
	 * 创建特殊子健的BOM信息
	 * @param usageLink 使用关系
	 * @param releaseTime 发布时间
	 * @return 错误信息
	 * @throws SQLException
	 */
	private static WTKeyedHashMap createPartSpecialHandlingData( WTPartUsageLink usageLink , String releaseTime, String batchNumber, SqlMapClient sqlMap ) throws SQLException {
		logger.debug("   "+"Entering In createPhantom( WTPartUsageLink usageLink , String releaseTime )......") ;
		
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		//从IE Task的XML文件中解析属性映射关系
		//Map<String,String> fieldMap = ieXMLUtil.parserXml(IETaskCommonUtil.TASK_FILE_DIRECOTRY + ieXMLFileCreate );
		
		
//		logger.debug("   "+CommonUtil.getMapStringValue( fieldMap )  ) ;
		
		Hashtable< WTPartUsageLink , String > hashtable = CommonPDMUtil.getPartSpecialHandlingData( usageLink );
		
		if( hashtable != null && hashtable.size() > 0 ){
			Iterator< WTPartUsageLink > iterator = hashtable.keySet().iterator();
			while( iterator.hasNext() ){
				WTPartUsageLink sonLink = iterator.next();
				if( sonLink != null ){
					
					EBOMInfo info = null;
					try {
						info = GenerateBeanInfo.generateEBOMInfoByUserLink(sonLink, releaseTime, batchNumber);
					} catch (Exception e) {
						
						e.printStackTrace();
						hashMap.put( sonLink, "生成materialInfo对象失败：" + e.toString() );
						continue;
					}
					if(releaseTime == null || releaseTime.trim().equals("")){
						releaseTime = CommonUtil.getCurrentTime() ;
					}
					info.setReleaseDate(CommonUtil.getCurrentDate());
					info.setFlag(IntegrationConstant.PDM_RELEASE_STATUS);
					/*info.setPdmWritebackStatus( IntegrationConstant.PDM_RELEASE_STATUS);
					info.setChildNumber(sonLink.getUses().getNumber());
					info.setQuantity( hashtable.get( sonLink ));*/
					try {
						sqlMap.insert(IbatisSqlConstant.INSERT_EBOM_INFO, info);
					} catch (SQLException e) {
						hashMap.put( sonLink, "插入EBOM发生异常：" + e.toString() );
						e.printStackTrace();
						continue;
					}
					
				}
			}
			
		}
		return hashMap;
	}
	
	/**
	 * 从中间表中删除BOM结构关系的信息
	 * 
	 * @param usageLink
	 * @throws WTException 
	 * @throws SQLException 
	 */
	public static WTKeyedHashMap delete( WTPartUsageLink usageLink ) throws WTException, SQLException{
		logger.debug("   "+"Entering In delete( WTPartUsageLink usageLink )......") ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if( usageLink == null ){
			logger.debug("   "+"delete Method,WTPartUsageLink is NULL");
			return hashMap;
		}
		WTPart parent = usageLink.getUsedBy() ;
		WTPartMaster childMaster = usageLink.getUses() ;
		String lineNumber = AttributeUtil.getAttribute(usageLink, PartUsageLinkAttribute.LINE_NUMBER ) ;
		EBOMInfo info =  GenerateBeanInfo.generateEBOMInfo(parent, childMaster, lineNumber);
		info.setParentView(AttributeUtil.getAttribute(usageLink, PartUsageLinkAttribute.PARENT_PART_VIEW));
		SqlMapClient sqlMap = null;
		sqlMap = IbatisUtil.getSqlMapInstance();
		try {
			 sqlMap.startTransaction(); 
			logger.debug("   "+"执行BOM结构关系信息删除操作") ;
			try {
				
				sqlMap.delete(IbatisSqlConstant.DELETE_EBOM_INFO, info);
			} catch (SQLException e) {
				e.printStackTrace();
				hashMap.put( usageLink, "删除EBOM发生异常：" + e.toString() );
				
			}
			sqlMap.commitTransaction(); 
			
			logger.debug("   "+"删除BOM结构关系信息任务结束") ;
		} finally{
			sqlMap.endTransaction();
			IbatisUtil.closeSqlMapInstance();
			logger.debug("   "+"Existed Out delete( WTPartUsageLink usageLink )......") ;
		}
		return hashMap;
	}
	
	/**
	 * 在中间表中创建BOM结构关系
	 * 
	 * @param usageLink
	 * @param releaseTime
	 * @throws IEException 
	 * @throws WTException 
	 * @throws IOException 
	 * @throws SQLException 
	 */
	protected static WTKeyedHashMap create( WTPartUsageLink usageLink , String releaseTime , String batchNumber) throws WTException, IOException, SQLException {
		logger.debug("   "+"Entering In create( WTPartUsageLink usageLink , String releaseTime )......") ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if( usageLink == null ){
			logger.debug("   "+"create Method, WTPartUsageLink is NULL");
			return hashMap;
		}
		
		EBOMInfo info = null;;
		try {
			info = GenerateBeanInfo.generateEBOMInfoByUserLink(usageLink, releaseTime, batchNumber);
		}catch (Exception e) {
			hashMap.put( usageLink, "生成EBOM对象错误：" + e.toString() );
			e.printStackTrace();
			return hashMap;
		}
		
		info.setReleaseDate(CommonUtil.getCurrentDate());
		
		info.setFlag(IntegrationConstant.PDM_RELEASE_STATUS);
		SqlMapClient sqlMap = null;
	    sqlMap = IbatisUtil.getSqlMapInstance();
		try {
			sqlMap.startTransaction();
			logger.debug("   "+"执行BOM结构关系信息创建任务") ;
			try {
				sqlMap.insert(IbatisSqlConstant.INSERT_EBOM_INFO, info);
			} catch (SQLException e) {
				hashMap.put( usageLink, "插入EBOM发生异常：" + e.toString() );
				e.printStackTrace();
			}
			if(hashMap==null||hashMap.size()==0){
				sqlMap.commitTransaction(); 
			}
			
			logger.debug("   "+"创建BOM结构关系信息任务结束") ;
		}finally{
			sqlMap.endTransaction();
	    	IbatisUtil.closeSqlMapInstance();
			logger.debug("   "+ "Existed Out create( WTPartUsageLink usageLink , String releaseTime )......") ;
		}
		return hashMap;
	}
	
	/**
	 * 在中间表中创建BOM结构关系
	 * 
	 * @param usageLink
	 * @param releaseTime
	 * @throws IEException 
	 * @throws WTException 
	 * @throws IOException 
	 * @throws SQLException 
	 */
	public static WTKeyedHashMap create( WTPartUsageLink usageLink , String releaseTime, String batchNumber, SqlMapClient sqlMap ) throws  WTException, IOException, SQLException {
		logger.debug("   "+"Entering In create( WTPartUsageLink usageLink , String releaseTime )......") ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if( usageLink == null ){
			logger.debug("   "+"create Method, WTPartUsageLink is NULL");
			return hashMap;
		}
		
		EBOMInfo info = null;;
		try {
			info = GenerateBeanInfo.createEBOMInfoByUserLink(usageLink, releaseTime, batchNumber);
		}catch (Exception e) {
			hashMap.put( usageLink, "生成EBOM对象错误：" + e.toString() );
			e.printStackTrace();
			return hashMap;
		}
		
		logger.debug("   "+"执行BOM结构关系信息创建任务") ;
		try {
			sqlMap.insert(IbatisSqlConstant.INSERT_EBOM_INFO, info);
		} catch (SQLException e) {
			hashMap.put( usageLink, "插入EBOM发生异常：" + e.toString() );
			e.printStackTrace();
		}
		logger.debug("   "+"创建BOM结构关系信息任务结束") ;
		return hashMap;
	}
	
	/**
	 * 更新中间表中的BOM结构关系
	 * 
	 * @param usageLink
	 * @param releaseTime
	 * @throws SQLException 
	 */
	protected static WTKeyedHashMap update( WTPartUsageLink usageLink , String releaseTime , String batchNumber) throws SQLException{
		logger.debug("   "+ "Entering In update( WTPartUsageLink usageLink , String releaseTime )......") ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if( usageLink == null ){
			logger.debug("   "+ "update Method, WTPartUsageLink is NULL");
			return hashMap;
		}
		
		EBOMInfo info = null;;
		try {
			info = GenerateBeanInfo.generateEBOMInfoByUserLink(usageLink, releaseTime, batchNumber);
		}catch (Exception e) {
			hashMap.put( usageLink, "生成EBOM对象错误：" + e.toString() );
			e.printStackTrace();
			return hashMap;
		}
		SqlMapClient sqlMap = null;
	    sqlMap = IbatisUtil.getSqlMapInstance();
		info.setReleaseDate(CommonUtil.getCurrentDate());
		info.setFlag(IntegrationConstant.PDM_RELEASE_STATUS);	
		try {
			logger.debug("   "+ "执行BOM结构关系信息更新任务") ;
			sqlMap.startTransaction();
			sqlMap.update(IbatisSqlConstant.UPDATE_EBOM_INFO, info);
			sqlMap.commitTransaction();
			
			logger.debug("   "+ "更新BOM结构关系信息任务结束") ;
		}catch (SQLException e) {
			hashMap.put( usageLink, "更新EBOM发生异常：" + e.toString() );
			e.printStackTrace();
		} finally{
			sqlMap.endTransaction();
	    	IbatisUtil.closeSqlMapInstance();
			logger.debug("   "+ "Existed Out update( WTPartUsageLink usageLink , String releaseTime )......") ;
		}
		return hashMap;
	}
	
	/**
	 * 更新中间表中的BOM结构关系
	 * 
	 * @param usageLink
	 * @param releaseTime
	 * @throws SQLException 
	 */
	protected static WTKeyedHashMap update( WTPartUsageLink usageLink , String releaseTime, String batchNumber, SqlMapClient sqlMap) throws SQLException{
		logger.debug("   "+ "Entering In update( WTPartUsageLink usageLink , String releaseTime )......") ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if( usageLink == null ){
			logger.debug("   "+ "update Method, WTPartUsageLink is NULL");
			return hashMap;
		}
		
		EBOMInfo info = null;;
		try {
			info = GenerateBeanInfo.generateEBOMInfoByUserLink(usageLink, releaseTime, batchNumber );
		}catch (Exception e) {
			hashMap.put( usageLink, "生成EBOM对象错误：" + e.toString() );
			e.printStackTrace();
			return hashMap;
		}
		//SqlMapClient sqlMap = null;
	    //sqlMap = IbatisUtil.getSqlMapInstance();
		info.setReleaseDate(CommonUtil.getCurrentDate());
		info.setFlag(IntegrationConstant.PDM_RELEASE_STATUS);	
		try {
			logger.debug("   "+ "执行BOM结构关系信息更新任务") ;
			//sqlMap.startTransaction();
			sqlMap.update(IbatisSqlConstant.UPDATE_EBOM_INFO, info);
			//sqlMap.commitTransaction();
			
			logger.debug("   "+ "更新BOM结构关系信息任务结束") ;
		}catch (SQLException e) {
			hashMap.put( usageLink, "更新EBOM发生异常：" + e.toString() );
			e.printStackTrace();
		} finally{
			//sqlMap.endTransaction();
	    	//IbatisUtil.closeSqlMapInstance();
			logger.debug("   "+ "Existed Out update( WTPartUsageLink usageLink , String releaseTime )......") ;
		}
		return hashMap;
	}
	
	
	
	/**
	 * 查询中间表中的BOM结构关系
	 * 
	 * @param usageLink
	 * @return
	 * @throws WTException 
	 */
	public static List isQuery( WTPart part ) throws WTException{
		logger.debug("   "+ "Entering In query( WTPartUsageLink usageLink )......") ;
		
		List list = new ArrayList(); ;
		if(part==null)
			return list;
		EBOMInfo info =  new EBOMInfo();
		try {
			info.setParentNumber(AttributeUtil.getAttribute(part, PartAttribute.NUMBER));
			info.setParentMajorVersion(AttributeUtil.getAttribute(part, PartAttribute.MAJOR_VERSION));
			info.setParentView(AttributeUtil.getAttribute(part, PartAttribute.VIEW));
		} catch (WTException e1) {
			
			e1.printStackTrace();
			throw new WTException("设置EBOM属性错误："+e1.getMessage());
		}
		
		//执行任务
		SqlMapClient sqlMap = null;
	    sqlMap = IbatisUtil.getSqlMapInstance();
	    logger.debug("   "+ "准备执行BOM结构关系信息查询Task......") ;
		
		try {
			sqlMap.startTransaction();
			list = sqlMap.queryForList(IbatisSqlConstant.QUERY_ALLEBOMRECOMD_BY_CONDITION,info);
			sqlMap.commitTransaction();
		} catch (SQLException e) {
			
			e.printStackTrace();
			throw new WTException("查询数据问题："+e.getMessage());
		}
		logger.debug("   "+ "BOM结构关系信息查询Task执行完成......") ;
		
		logger.debug("   "+ "Existed Out query( WTPartUsageLink usageLink )......") ;
		
		return list ;
	}
	
	

}
