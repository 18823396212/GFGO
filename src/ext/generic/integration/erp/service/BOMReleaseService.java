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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibatis.sqlmap.client.SqlMapClient;

import ext.com.core.CoreUtil;
import ext.com.iba.IBAUtil;
import ext.generic.integration.erp.attributes.AttributeUtil;
import ext.generic.integration.erp.attributes.PartAttribute;
import ext.generic.integration.erp.bean.EBOMInfo;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.generic.integration.erp.ibatis.BomInfoIbatis;
import ext.generic.integration.erp.ibatis.BomStructureIbatis;
import ext.generic.integration.erp.ibatis.IbatisSqlConstant;
import ext.generic.integration.erp.rule.BussinessRule;
import ext.generic.integration.erp.util.BOMStructureUtil;
import ext.generic.integration.erp.util.BusinessRuleXMLConfigUtil;
import ext.generic.integration.erp.util.IbatisUtil;
import ext.generic.integration.erp.util.IntegrationConstant;
import ext.generic.integration.erp.util.PDMIntegrationLogUtil;
import wt.change2.ChangeException2;
import wt.change2.ChangeHelper2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTKeyedHashMap;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.config.ConfigException;
/**
 * BOM信息发布类
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-10-08
 */

public class BOMReleaseService {	
	
	private static final String CLASSNAME = BOMReleaseService.class.getName() ;

	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	/**
	 * 发布PBO单层BOM结构，如果不是WTPart，则不处理
	 * 
	 * @param obj
	 */
	public static void releaseSingleLevel( Object obj ){
		releaseSingleLevel( obj , "" , null ) ;
	}
	
	/**
	 * 发布PBO单层BOM结构，如果不是WTPart，则不处理
	 * 如果不在ECN中，则WTChangeOrder2 ecn设置为null
	 * 
	 * @param obj
	 * @param releaseTime
	 * @param ecn
	 */
	public static void releaseSingleLevel( Object obj , String releaseTime, WTChangeOrder2 ecn ){
		if(obj != null && obj instanceof WTPart){
			WTPart part = ( WTPart ) obj ;
			releaseSingleLevel( part , "" , ecn) ;
		}
	}
	
	/**
	 * 发布WTPart单层BOM结构
	 * 
	 * @param part
	 */
	public static void releaseSingleLevel( WTPart part ){
		releaseSingleLevel( part , "" , null) ;
	}
	
	/**
	 * 发布WTPart单层BOM结构
	 * @param part 父件
	 * @param releaseTime 发布时间
	 * @param wtobj pbo
	 * @param batchNumber 批次号
	 * @return
	 */
	public static WTKeyedHashMap releaseSingleLevel( WTPart part , String releaseTime, WTObject wtobj , String batchNumber){
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if( part == null ){
			return hashMap;
		}
		logger.debug("--------开始发布单层BOM, part is:" + part.getIdentity() + " releaseTime is: " + releaseTime + "--------");

		String ecnNum = "";
		WTChangeOrder2 ecn = getECNByWTObj(wtobj);
		try{
			//获取同一视图下最新版本的零部件
//			part = CoreUtil.getWTPartByNumberAndView(part.getNumber(), part.getViewName()) ;
			if (!part.isLatestIteration())//不是最新版本，先获得最新小版本
				part = (WTPart) VersionControlHelper.service.getLatestIteration(part, false);
			
			//发布ECN信息到中间表。
			if(ecn != null){
				ecnNum = ecn.getNumber();
//				hashMap.putAll(ECNReleaseService.release( ecn , releaseTime, batchNumber)) ;
			}
			
			if(hashMap.size()>0){
				return hashMap;//失败就返回
			}
			
			//发布父件物料信息
			hashMap.putAll(PartReleaseService.release(part, releaseTime, batchNumber,null, ecnNum) );
			if(hashMap.size()>0){
				return hashMap;//失败就返回
			}
			//发布第一层子件和子件关联关系
			releaseFirstLevel( part, releaseTime , wtobj ,batchNumber,  hashMap ) ;
		} catch (WTException e) {
			hashMap.put(part, e.toString());
			e.printStackTrace();
		} finally{
			if(logger.isDebugEnabled()){
				logger.debug("-------结束发布单层BOM, part is:" + part.getIdentity() + ""
						+ " releaseTime is: " + releaseTime + " hashMap is:"+hashMap + "--------");
			}
		}
		
		return hashMap;
	}
	
	/**
	 * 发布List列表中的所有零部件的单层BOM结构
	 * 
	 * @param parts
	 */
	public static void releaseSingleLevel(List<WTPart> parts){
		if( parts == null ){
			return ;
		}
		
		int size = parts.size() ;
		
		for( int i=0 ; i<size ; i++ ){
			WTPart part = parts.get(i) ;
			
			releaseSingleLevel( part ) ;
		}
	}
	
	/**
	 * 发布List列表中的所有零部件的单层BOM结构
	 * 如果不在ECN中，则WTChangeOrder2 ecn设置为null
	 * 
	 * @param parts
	 * @param releaseTime
	 * @param ecn
	 */
	public static void releaseSingleLevel(List<WTPart> parts , String releaseTime, WTChangeOrder2 ecn ){
		if( parts == null ){
			return ;
		}
		
		int size = parts.size() ;
		
		for( int i=0 ; i<size ; i++ ){
			WTPart part = parts.get(i) ;
			
			releaseSingleLevel( part , releaseTime, ecn ) ;
		}
	}
	
	/**
	 * 发布WTArrayList列表中的所有零部件的单层BOM结构
	 * 
	 * @param parts
	 */
	public static void releasePersistableSingleLevel( WTArrayList parts ){
		try {
			releasePersistableSingleLevel( parts , "" ,  null , null) ;
		} catch (WTException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 发布WTArrayList列表中的所有零部件的单层BOM结构
	 * 如果不在ECN中，则WTChangeOrder2 ecn设置为null
	 * 
	 * @param parts
	 * @param releaseTime
	 * @param wtobj
	 * @param batchNumber
	 */
	public static WTKeyedHashMap releasePersistableSingleLevel(WTArrayList parts, String releaseTime, WTObject wtobj,
			String batchNumber) throws WTException {
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if (parts == null) {
			return hashMap;
		}

		try {
			int size = parts.size();
			for (int i = 0; i < size; i++) {
				Persistable persistable = parts.getPersistable(i);
				if (persistable != null && persistable instanceof WTPart) {
					WTPart part = (WTPart) persistable;
					WTKeyedHashMap map = releaseSingleLevel(part, releaseTime, wtobj, batchNumber);
					if (map != null && map.size() > 0) {
						hashMap.putAll(map);
					}
					if (hashMap.size() > 0) {
						return hashMap;// 失败就返回
					}
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		} finally {

		}
		return hashMap;
	}
	
	/**
	 * 发布PBO的所有BOM结构
	 * 
	 * @param obj
	 */
	public static void releaseAllLevel( Object obj ){
		releaseAllLevel( obj , "" , null ) ;
	}
	
	/**
	 * 发布PBO的所有BOM结构
	 * 如果不在ECN中，则WTChangeOrder2 ecn设置为null
	 * 
	 * @param obj
	 * @param releaseTime
	 * @param ecn
	 */
	public static void releaseAllLevel( Object obj , String releaseTime, WTChangeOrder2 ecn ){
		if(obj != null && obj instanceof WTPart){
			WTPart part = ( WTPart ) obj ;
			releaseAllLevel( part , "" , ecn, "") ;
		}
	}
	
	/**
	 * 发布WTPart的所有BOM结构
	 * 
	 * @param part
	 */
	public static void releaseAllLevel( WTPart part ){
		releaseAllLevel(part , "" , null, null) ;
	}
	
	/**
	 * 发布WTPart的所有BOM结构
	 * 如果不在ECN中，则WTChangeOrder2 ecn设置为null
	 * 
	 * @param part
	 * @param releaseTime
	 * @param ecn
	 * @param self
	 */
	public static WTKeyedHashMap releaseAllLevel( WTPart part , String releaseTime, WTObject wtobj , String batchNumber){
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		if( part == null ){
			return hashMap;
		}

		//获取同一视图下最新版本的零部件
		try {
//			part = CoreUtil.getWTPartByNumberAndView(part.getNumber(), part.getViewName());
			if (!part.isLatestIteration())//不是最新版本，先获得最新小版本
				part = (WTPart) VersionControlHelper.service.getLatestIteration(part, false);
		} catch (WTException e) {
			e.printStackTrace();
		}
		
		WTChangeOrder2 ecn = getECNByWTObj(wtobj);

		// 发布ECN信息到中间表。
		WTKeyedHashMap changeMap = ECNReleaseService.release(ecn, releaseTime, batchNumber);
		// 根节点发布出现异常，则程序终止
		if (changeMap.size() > 0) {
//			hashMap.putAll(getChangeErrorInfo(changeMap, part));
			hashMap.putAll(changeMap);
			return hashMap;
		}
		
		//发布根节点物料信息
		WTKeyedHashMap partMap = PartReleaseService.release( part , releaseTime, batchNumber) ;
		//根节点发布出现异常，则程序终止
		if (partMap.size() > 0) {
//			hashMap.putAll(getErrorInfo(partMap, part));
			hashMap.putAll(partMap);
			return hashMap;
		}

		List<WTPart> childList = releaseFirstLevel(part, releaseTime, wtobj, batchNumber, hashMap);

		if (hashMap.size() > 0) {
			return hashMap;
		}

		//必须判断size，否则会导致死循环
		while( ( childList != null ) && ( childList.size() > 0 ) ){
			List<WTPart> tempList = new ArrayList<WTPart>() ;
			tempList.clear();
			
			int childListSize = childList.size() ;
			
			for(int i=0 ; i < childListSize ; i++ ){
				WTPart childPart = childList.get(i) ;
				
				List<WTPart> tempChildList = releaseFirstLevel( childPart , releaseTime , wtobj , batchNumber,  hashMap );
					
				tempList.addAll(tempChildList) ;
				//发布BOM时，一个物料发布失败整个BOM则发布失败
				if( hashMap.size() > 0 ){
					return hashMap;
				}
			}
			
			childList = tempList ;
		}
		
		return hashMap;
	}
	
	/**
	 * 发布List列表中WTPart的所有BOM结构
	 * 
	 * @param parts
	 */
	public static void releaseAllLevel(List<WTPart> parts){
		releaseAllLevel( parts , "" , null ) ;
	}
	
	/**
	 * 发布List列表中WTPart的所有BOM结构
	 * 如果不在ECN中，则WTChangeOrder2 ecn设置为null
	 * 
	 * @param parts
	 * @param releaseTime
	 * @param ecn
	 */
	public static void releaseAllLevel(List<WTPart> parts , String releaseTime , WTChangeOrder2 ecn ){
		if( parts == null ){
			return ;
		}
		
		int size = parts.size() ;
		
		for(int i=0 ; i<size ; i++ ){
			WTPart part = parts.get(i) ;
			
			releaseAllLevel( part , releaseTime, ecn ) ;
		}
	}
	
	/**
	 * 发布WTArrayList列表中WTPart的所有BOM结构
	 * 
	 * @param parts
	 */
//	public static void releasePersistableAllLevel( WTArrayList parts ){
//		releasePersistableAllLevel( parts , "" , null ) ;
//	}
	
	/**
	 * 发布WTArrayList列表中WTPart的所有BOM结构
	 * 如果不在ECN中，则WTChangeOrder2 ecn设置为null
	 * 
	 * @param parts
	 * @param releaseTime
	 * @param ecn
	 * @param batchNumber
	 */
	public static WTKeyedHashMap releasePersistableAllLevel( WTArrayList parts , String releaseTime, WTChangeOrder2 ecn , String batchNumber){
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		if( parts == null ){
			return hashMap;
		}

		try{
			
			int size = parts.size() ;
			logger.debug( "Debug   size : " + size );
			for( int i=0 ; i<size ; i++ ){
				logger.debug( "Debug   i : " + i );
				Persistable persistable = parts.getPersistable(i) ;
				
				if( persistable != null && persistable instanceof WTPart ){
					WTPart part = ( WTPart ) persistable ;
					
					WTKeyedHashMap map = releaseAllLevel( part , releaseTime, ecn , batchNumber );
					
					if( map != null ){
						hashMap.putAll(map);
					}
					
					//流程发布BOM时，一个物料出现问题。程序暂停
//					if( hashMap.size() > 0 ){
//						return hashMap;
//					}
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}finally{
			
		}
		logger.debug( "Debug   hashMap : " + hashMap );
		return hashMap;
	}
	
	/**
	 * 发布单层BOM结构
	 * 
	 * @param part
	 * @param releaseTime
	 * @param wtobj
	 * @param batchNumber
	 * @param hashMap
	 * @return
	 * @throws IntegrationException 
	 */
	private static List<WTPart> releaseFirstLevel( WTPart part , String releaseTime , WTObject wtobj , String batchNumber, WTKeyedHashMap hashMap ){			
		//先判断是否需要发布
		//返回参数part的子件列表
		List<WTPart> childPartList = new ArrayList<WTPart>() ;
		try {
			childPartList = BOMStructureUtil.getFirstLevelChildren(part);
		} catch (ConfigException e1) {
			e1.printStackTrace();
		} catch (WTException e1) {
			e1.printStackTrace();
		}
		
		//如果子件数量为0， 则不发布
		if(childPartList==null || childPartList.size()<=0){
			logger.debug("父件："+part.getNumber()+"没有子件，不需要发布BOM结构");
			return new  ArrayList<WTPart>();
		}
		
		// 先判断是否需要发布
		if(!BussinessRule.isPartCanRelease(part)){//父件允许发布，才能发BOM结构
			//TODO 是略过还是记错
			logger.error("父件："+part.getDisplayIdentifier()+"不符合发布条件");
			return childPartList;
		}
		
		// 添加一个检查，检查bom的一层子件是否存在非发布的情况，有的话，也要提示错误
		String info = BussinessRule.checkChildListState(part, childPartList);
		if(StringUtils.isNotBlank(info)){
			hashMap.put(part, info);
			return childPartList;
		}
		
		//判断当前part是否已经成功发布,如果没有成功发布，则返回false
		boolean isReleasedSuccess = isBOMReleasedSuccess( part ) ;
		
		if( ! isReleasedSuccess ){
			try {
				logger.debug("isReleasedSuccess = " + isReleasedSuccess ) ;
				
				SqlMapClient sqlMap = null;
				sqlMap = IbatisUtil.getSqlMapInstance();
				
				try
				{
					IbatisReplacementReleaseService ibatisReplacementReleaseService = new IbatisReplacementReleaseService();
					sqlMap.startTransaction();
					sqlMap.startBatch();
					
					//先删除数据
					EBOMInfo ebominfo = new EBOMInfo();
					//根据父项的编码、大版本、视图删除子件
					ebominfo.setParentNumber(AttributeUtil.getAttribute(part, PartAttribute.NUMBER));
					ebominfo.setParentMajorVersion(AttributeUtil.getAttribute(part, PartAttribute.MAJOR_VERSION));
					ebominfo.setParentView(AttributeUtil.getAttribute(part, PartAttribute.VIEW));
					sqlMap.delete(IbatisSqlConstant.REMOVE_EBOMRECOMD_BY_PARENT, ebominfo);
					
					List<WTPartUsageLink> usageLinkList = BOMStructureUtil.getFirstLevelUsageLink(part) ;
					for( int i = 0 ; i < usageLinkList.size() ; i++ ){
						WTPartUsageLink usageLink = usageLinkList.get(i) ;
						if(logger.isDebugEnabled()){
							logger.debug("   "+"发布BOM结构信息:\n" + CommonPDMUtil.getWTPartUsageLinkInfo(usageLink) ) ;
						}
						//获取子件
						WTPartMaster childMaster = usageLink.getUses() ;
						WTPart childPart = null;
						try {
							childPart = CoreUtil.getWTPartByMasterAndView(childMaster, part.getViewName());
						} catch (WTException e) {
							
							e.printStackTrace();
							continue;
						}
						//判断子件是否为可发布类型
						if( BussinessRule.canReleasePart( childPart ) ){
							//modify on 20160523, 为提交效率及简化配置，直接插入所有子件Link
							//hashMap.putAll( BomStructureIbatis.add(usageLink, releaseTime,sqlMap) ) ;
							hashMap.putAll(BomStructureIbatis.create( usageLink , releaseTime, batchNumber, sqlMap ));
							//发布特定替代关系
							
							hashMap.putAll( ibatisReplacementReleaseService.releaseSubstitute( usageLink , releaseTime, batchNumber, sqlMap ) ) ;
							
							//发布全局替代关系
							if( BussinessRule.isAlternateLinkRelease() ){
								hashMap.putAll(ibatisReplacementReleaseService.releaseAlternate( usageLink , releaseTime, batchNumber, sqlMap )) ;
							}
						}
					}
					if(hashMap.size()>0){
						return childPartList;
					}
					// 更新BOM表头信息
					WTKeyedHashMap rootMap = BomInfoIbatis.add(part, releaseTime, batchNumber, wtobj, sqlMap);
					hashMap.putAll(rootMap);
					
					sqlMap.commitTransaction();
				}catch (Exception e) {
					hashMap.put(part, PDMIntegrationLogUtil.failedReleaseBOMLogInfo(part) + e.getMessage());
					logger.debug(PDMIntegrationLogUtil.failedReleaseBOMLogInfo(part) + e.getMessage()) ;
					logger.debug(e.getMessage() ) ;
					e.printStackTrace();
				}finally{
					sqlMap.endTransaction();
					IbatisUtil.closeSqlMapInstance();
				}
				
				if(hashMap.size() > 0){
					UpdatePDMDataService.updatePDMFailed( part , BusinessRuleXMLConfigUtil.getInstance().getBomReleaseStatus() );
				}
				else{
					UpdatePDMDataService.updatePDMSucessful( part , BusinessRuleXMLConfigUtil.getInstance().getBomReleaseStatus() );
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return childPartList;
	}

	
//	/**
//	 * 发布单层差异BOM结构
//	 * 
//	 * @param part
//	 * @param releaseTime
//	 * @param ecnNumber
//	 * @return
//	 */
//	private static List<WTPart> releaseDifferenceFirstLevel( WTPart part , String releaseTime , String ecnNumber , String batchNumber,  WTKeyedHashMap hashMap ){			
//
//		//先判断是否需要发布
//		//返回参数part的子件列表
//		List<WTPart> childPartList = new ArrayList<WTPart>() ;
//		try {
//			childPartList = BOMStructureUtil.getFirstLevelChildren(part);
//		} catch (ConfigException e1) {
//			e1.printStackTrace();
//		} catch (WTException e1) {
//			e1.printStackTrace();
//		}
//		//判断当前part是否已经成功发布,如果没有成功发布，则返回false
//		boolean isReleasedSuccess = isBOMReleasedSuccess( part ) ;
//		
//		if( ! isReleasedSuccess ){
//			try {
//				logger.debug("isReleasedSuccess = " + isReleasedSuccess ) ;
//				
//				//差异比较，需要进行创建操作的WTPartUsageLink集合
//				List<WTPartUsageLink> needCreateUsageLinkList = new ArrayList<WTPartUsageLink>() ;
//				
//				//差异比较，需要进行更新操作的WTPartUsageLink集合
//				List<WTPartUsageLink> needUpdateUsageLinkList = new ArrayList<WTPartUsageLink>() ;
//				
//				//差异比较，需要进行删除操作的WTPartUsageLink集合
//				List<WTPartUsageLink> needDeleteUsageLinkList = new ArrayList<WTPartUsageLink>() ;
//				
//				
//				//差异比较，需要进行创建操作的WTPartUsageLink集合
//				List<WTPartSubstituteLink> needCreateSubsituteLinkList = new ArrayList<WTPartSubstituteLink>() ;
//				
//				//差异比较，需要进行更新操作的WTPartUsageLink集合
//				List<WTPartSubstituteLink> needUpdateSubsituteLinkList = new ArrayList<WTPartSubstituteLink>() ;
//				
//				//差异比较，需要进行删除操作的WTPartUsageLink集合
//				List<WTPartSubstituteLink> needDeleteSubsituteLinkList = new ArrayList<WTPartSubstituteLink>() ;
//				
//				
//				//此处做了修改，由根据当前部件的最新版本比较差异改为从历史记录里面拿最新发布成功的版本
//				WTPart oldPart = WTPartUsageLinkCompareService.getPreviousRelease( part ) ;
//				//如果获取到的上一发布版本为null，则表示当前part的BOM结构为初次发布
//				if( oldPart == null ){
//					logger.debug("oldPart == null") ;
//					//初次发布，所有的WTPartUsageLink对象，都为创建操作
//					needCreateUsageLinkList = BOMStructureUtil.getFirstLevelUsageLink( part ) ;
//					needCreateSubsituteLinkList = BOMStructureUtil.getWTPartSubsituteLink( part );
//				}else{
//					logger.debug("=======old part is not null========" + CommonPDMUtil.getMajorVersion(oldPart)+"."+CommonPDMUtil.getMinorVersion(oldPart));
//					//如果获取到的上一发布版本不为null，比较两个版本之间的差异
//					WTPartUsageLinkCompareService.getFirstLevelDifferenceLink( part , oldPart , needCreateUsageLinkList , needUpdateUsageLinkList , needDeleteUsageLinkList ) ;
//					WTPartSubstituteLinkCompareService.getSubsiteteDifferenceLink(part, oldPart, needCreateSubsituteLinkList, needUpdateSubsituteLinkList, needDeleteSubsituteLinkList);
//				}
//
//				if( logger.isDebugEnabled() ){
//					logger.debug("releaseAllLevel: release part is : " + IdentityFactory.getDisplayIdentifier( part ) ) ;
//					
//					logger.debug("needCreateUsageLinkList size : " + needCreateUsageLinkList.size() ) ;
//					logger.debug("needUpdateUsageLinkList size : " + needUpdateUsageLinkList.size() ) ;
//					logger.debug("needDeleteUsageLinkList size : " + needDeleteUsageLinkList.size() ) ;
//					
//					logger.debug("needCreateSubsiteteLinkList size : " + needCreateSubsituteLinkList.size() ) ;
//					logger.debug("needUpdateSubsiteteLinkList size : " + needUpdateSubsituteLinkList.size() ) ;
//					logger.debug("needDeleteSubsiteteLinkList size : " + needDeleteSubsituteLinkList.size() ) ;
//				}
//				
//				//如果三个List中，都没有数据，则不用发布数据
//				if( (needCreateUsageLinkList.size() != 0) || (needUpdateUsageLinkList.size() != 0) || (needDeleteUsageLinkList.size() != 0) 
//						||(needCreateSubsituteLinkList.size() != 0) || (needUpdateSubsituteLinkList.size() != 0) || (needDeleteSubsituteLinkList.size() != 0)){
//					List<EBOMInfo> needInsertLink = new ArrayList<EBOMInfo>() ;
//					List<AlternativeMaterial> needInsertSubsituteLink = new ArrayList<AlternativeMaterial>() ;
//					//需要增加的WTPartUsageLink
//					for(WTPartUsageLink needCreateLink:needCreateUsageLinkList){
//						EBOMInfo info = GenerateBeanInfo.generateEBOMInfoByUserLink(needCreateLink, releaseTime, batchNumber);
////						info.setAction("A");
//						info.setPrimaryKey("A_" + info.getOid());
//						needInsertLink.add(info);
//					}
//					
//					//需要删除的WTPartUsageLink
//					for(WTPartUsageLink needDeleteLink:needDeleteUsageLinkList){
//						EBOMInfo info = GenerateBeanInfo.generateEBOMInfoByUserLink(needDeleteLink, releaseTime, batchNumber);
////						info.setAction("D");
//						info.setPrimaryKey("D_" + info.getOid());
//						needInsertLink.add(info);
//					}
//					
//					//需要更新的WTPartUsageLink
//					for(WTPartUsageLink needUpdateLink:needUpdateUsageLinkList){
//						EBOMInfo info = GenerateBeanInfo.generateEBOMInfoByUserLink(needUpdateLink, releaseTime, batchNumber);
////						info.setAction("U");
//						info.setPrimaryKey("U_" + info.getOid());
//						needInsertLink.add(info);
//					}
//					
//					//需要新增的WTPartSubstituteLink
//					for(WTPartSubstituteLink needCreateSubsituteLink:needCreateSubsituteLinkList){
//						AlternativeMaterial info = AlternativeMaterialUtil.generateAlternativeMaterial(needCreateSubsituteLink, releaseTime, batchNumber);
////						info.setAction("A");
//						info.setPrimaryKey("A_" + info.getOid());
//						needInsertSubsituteLink.add(info);
//					}
//					
//					//需要删除的WTPartSubstituteLink
//					for(WTPartSubstituteLink needDeleteSubsiteteLink:needDeleteSubsituteLinkList){
//						AlternativeMaterial info = AlternativeMaterialUtil.generateAlternativeMaterial(needDeleteSubsiteteLink, releaseTime, batchNumber);
////						info.setAction("D");
//						info.setPrimaryKey("D_" + info.getOid());
//						needInsertSubsituteLink.add(info);
//					}
//					
//					//需要更新的WTPartSubstituteLink
//					for(WTPartSubstituteLink needUpdateSubsituteLink:needUpdateSubsituteLinkList){
//						AlternativeMaterial info = AlternativeMaterialUtil.generateAlternativeMaterial(needUpdateSubsituteLink, releaseTime, batchNumber);
////						info.setAction("U");
//						info.setPrimaryKey("U_" + info.getOid());
//						needInsertSubsituteLink.add(info);
//					}
//					
//					SqlMapClient sqlMap = null;
//					sqlMap = IbatisUtil.getSqlMapInstance();
//					
//					try
//					{
//						IbatisReplacementReleaseService ibatisReplacementReleaseService = new IbatisReplacementReleaseService();
//						sqlMap.startTransaction();
//						sqlMap.startBatch();
//						//插入更新的WTPartUsageLink
//						for(EBOMInfo ebomInfo:needInsertLink){
//							sqlMap.insert(IbatisSqlConstant.INSERT_EBOM_INFO, ebomInfo);
//						}
//						//插入更新的WTPartSubstituteLink
//						for(AlternativeMaterial alternativeMaterial:needInsertSubsituteLink){
//							sqlMap.insert(IbatisSqlConstant.INSERT_ALTERNATIVE_MATERIAL, alternativeMaterial);
//						}
//						
//						//更新BOM表头信息
//						WTKeyedHashMap rootMap = BomInfoIbatis.add(part, releaseTime,batchNumber, ecnNumber,sqlMap) ;
//						hashMap.putAll(rootMap);
//						
//						sqlMap.commitTransaction();
//					}catch (Exception e) {
//						hashMap.put(part, PDMIntegrationLogUtil.failedReleaseBOMLogInfo(part) + e.getMessage());
//						logger.debug(PDMIntegrationLogUtil.failedReleaseBOMLogInfo(part) + e.getMessage()) ;
//						logger.debug(e.getMessage() ) ;
//						e.printStackTrace();
//					}finally{
//						sqlMap.endTransaction();
//						IbatisUtil.closeSqlMapInstance();
//					}
//				}else{
//					logger.debug("releaseAllLevel: release part is : " + IdentityFactory.getDisplayIdentifier( part ) + " No Difference." ) ;
//				}	
//				
//				if(hashMap.size() > 0){
//					UpdatePDMDataService.updatePDMFailed( part , BusinessRuleXMLConfigUtil.getInstance().getBomReleaseStatus() );
//				}
//				else{
//					UpdatePDMDataService.updatePDMSucessful( part , BusinessRuleXMLConfigUtil.getInstance().getBomReleaseStatus() );
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		
//		return childPartList;
//	}
	
	/**
	 * 判断当前零部件是否已经成功发送到ERP
	 * 
	 * @param part
	 * @return
	 */
	private static boolean isBOMReleasedSuccess(WTPart part) {
		logger.debug("Enter In isBOMReleasedSuccess( WTPart part , String releaseTime , String ecnNumber )...") ;
		
		boolean isReleasedSuccess = false ;
		
		boolean pdmRealsed = false;
		//如果BOM发布状态为"PDM已发布"或者"ERP接收成功"，且BOM表头中存在该数据，则认为发布成功。
		try {
			Object obj = IBAUtil.getIBAValue(part, BusinessRuleXMLConfigUtil.getInstance().getBomReleaseStatus());
			
			if( obj != null && 
				(IntegrationConstant.PDM_RELEASE_STATUS_CN.equals(obj.toString())
				|| IntegrationConstant.ERP_PROCESS_SUCCESS_CN.equals(obj.toString()))){
				pdmRealsed = true;
			}
			
		} catch ( Exception e ) {				
			e.printStackTrace();
		} 
		
		//added on 20160627, 先判断是否已经发布成功，如果已经发布，则不再更新
		List existParent =  new ArrayList();
		try {
			existParent = BomInfoIbatis.query(part);
		} catch (WTException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//如果BOMRoot表中存在，且PDM的发布标识为“PDM发布成功”或者“ERP接收成功”，则不在发布BOM
		if(existParent.size() > 0 && pdmRealsed){
			isReleasedSuccess = true;
		}
		
		logger.debug("isBOMReleasedSuccess = " + isReleasedSuccess ) ;
		
		return isReleasedSuccess ;
	}
	
	
	/**
	 * 解析物料发布错误信息(存在问题)
	 * @param partMap
	 * @return
	 */
	private static WTKeyedHashMap getErrorInfo( WTKeyedHashMap partMap , WTPart part ) {
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		Iterator iterator = partMap.keySet().iterator();
		
		while( iterator.hasNext() ){
			Object obj = iterator.next();
			
			if( obj != null ){
				if( obj instanceof ObjectReference ){
					ObjectReference orf = ( ObjectReference )obj;
					obj = orf.getObject();
				}
				
				if( obj instanceof WTPart ){
					WTPart newPart = ( WTPart )obj;
					
					if( newPart != null ){
						try {
							List< WTPartUsageLink > list = BOMStructureUtil.getFirstLevelUsageLink(part) ;
							logger.debug( "Debug   list : " + list );
							if( list != null ){
								Iterator< WTPartUsageLink > listI = list.iterator();
								
								while( listI.hasNext() ){
									WTPartUsageLink usageLink = listI.next();
									
									WTPartMaster sonPartMaster = usageLink.getUses();
									
									if( PersistenceHelper.isEquivalent(part, newPart) || PersistenceHelper.isEquivalent( sonPartMaster , (WTPartMaster)newPart.getMaster() ) ){
										hashMap.put( usageLink , "父件发布异常，请根据后台日志检查" );												
									}
								}
							}
						} catch (ConfigException e) {
							e.printStackTrace();
						} catch (WTException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		return hashMap;
	}
	
	/**
	 * 解析变更发布错误
	 * @param partMap
	 * @param part
	 * @return
	 */
	private static WTKeyedHashMap getChangeErrorInfo( WTKeyedHashMap partMap , WTPart part ) {
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		try {
			List< WTPartUsageLink > list = BOMStructureUtil.getFirstLevelUsageLink(part) ;
							
			if( list != null ){
				Iterator< WTPartUsageLink > listI = list.iterator();
								
				while( listI.hasNext() ){
					WTPartUsageLink usageLink = listI.next();
									
					if( usageLink != null ){
						hashMap.put( usageLink , "父件发布异常，请根据后台日志检查" );
										
						return hashMap;
					}
				}
			}
		} catch (ConfigException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
		
		return hashMap;
	}
	
	/**
	 * 根据wtobj获得ecn
	 * @param wtobj
	 * @return
	 */
	public static WTChangeOrder2 getECNByWTObj(WTObject wtobj){
		WTChangeOrder2 ecn = null;
		if(wtobj == null){
			return ecn;
		}
		if(wtobj instanceof WTChangeOrder2){
			ecn = (WTChangeOrder2) wtobj;
		}else if(wtobj instanceof WTChangeActivity2){
			WTChangeActivity2 eca = (WTChangeActivity2) wtobj;
			//根据eca获得ecn
			try {
				QueryResult ecnResult = ChangeHelper2.service.getLatestChangeOrder(eca);
				if(ecnResult != null && ecnResult.hasMoreElements()){
					logger.debug("ECA:"+eca.getNumber()+" 关联ECN的size="+ecnResult.size());
					ecn = (WTChangeOrder2) ecnResult.nextElement();
				}
			} catch (ChangeException2 e) {
				e.printStackTrace();
			} catch (WTException e) {
				e.printStackTrace();
			}
		}
		return ecn;
	}
	
}
