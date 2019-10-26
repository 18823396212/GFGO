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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ext.com.iba.IBAUtil;
import ext.generic.integration.erp.ibatis.BomInfoIbatis;
import ext.generic.integration.erp.ibatis.BomStructureIbatis;
import ext.generic.integration.erp.ibatis.BomStructureRemoveByParentIbatis;
import ext.generic.integration.erp.rule.BussinessRule;
import ext.generic.integration.erp.util.BOMStructureUtil;
import ext.generic.integration.erp.util.BusinessRuleXMLConfigUtil;
import ext.generic.integration.erp.util.IntegrationConstant;
import ext.generic.integration.erp.util.PDMIntegrationLogUtil;

import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTKeyedHashMap;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.part.WTPartUsageLink;
import wt.util.WTException;
import wt.vc.VersionControlHelper;

public class IbatisBOMReleaseService implements BOMReleaseServiceImpl{

	private static final String clazz = IbatisBOMReleaseService.class.getName() ;
	private static final Logger logger = LogR.getLogger(clazz);
	
	private IbatisPartReleaseService ibatisPartReleaseService =null;
	
	private IbatisECNReleaseService ibatisECNReleaseService =null;
	
	private IbatisReplacementReleaseService ibatisReplacementReleaseService =null;
	
	
	
	public IbatisBOMReleaseService() {
		super();
		this.ibatisPartReleaseService = new IbatisPartReleaseService();
		this.ibatisECNReleaseService = new IbatisECNReleaseService();
		this.ibatisReplacementReleaseService = new IbatisReplacementReleaseService();
	}

	public IbatisPartReleaseService getIbatisPartReleaseService() {
		return ibatisPartReleaseService;
	}

	public void setIbatisPartReleaseService(
			IbatisPartReleaseService ibatisPartReleaseService) {
		this.ibatisPartReleaseService = ibatisPartReleaseService;
	}
	
	
	public IbatisECNReleaseService getIbatisECNReleaseService() {
		return ibatisECNReleaseService;
	}

	public void setIbatisECNReleaseService(
			IbatisECNReleaseService ibatisECNReleaseService) {
		this.ibatisECNReleaseService = ibatisECNReleaseService;
	}

	public IbatisReplacementReleaseService getIbatisReplacementReleaseService() {
		return ibatisReplacementReleaseService;
	}

	public void setIbatisReplacementReleaseService(
			IbatisReplacementReleaseService ibatisReplacementReleaseService) {
		this.ibatisReplacementReleaseService = ibatisReplacementReleaseService;
	}

	@Override
	public WTKeyedHashMap releaseSingleLevel(Object obj) {
		WTKeyedHashMap hashMap =	releaseSingleLevel( obj , "" , "" , null ) ;
		return hashMap;
	}

	@Override
	public WTKeyedHashMap releaseSingleLevel(Object obj, String releaseTime, String batchNumber, 
			WTObject wtobj ) {
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		if(obj != null && obj instanceof WTPart){
			WTPart part = ( WTPart ) obj ;
			hashMap =releaseSingleLevel( part , "" ,  "", wtobj) ;
		}
		return hashMap;
	}

	@Override
	public WTKeyedHashMap releaseSingleLevel(WTPart part) {
		WTKeyedHashMap hashMap =releaseSingleLevel( part , "" , "",  null) ;
		return hashMap;
	}

	
	@Override
	public WTKeyedHashMap releaseSingleLevel(WTPart part, String releaseTime, String batchNumber,
			WTObject wtobj ) {
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		if( part == null ){
			return hashMap;
		}
		String ecnNumber = "" ;
		WTChangeOrder2 ecn = BOMReleaseService.getECNByWTObj(wtobj);
		if(ecn != null){
			ecnNumber = ecn.getNumber() ;
		}
		try{
			//获取同一视图下最新版本的零部件
			//part = CoreUtil.getWTPartByNumberAndView(part.getNumber(), part.getViewName()) ;
			if (!part.isLatestIteration())//不是最新版本，先获得最新小版本
				part = (WTPart) VersionControlHelper.service.getLatestIteration(part, false);
			//发布ECN信息到中间表。
			hashMap = ibatisECNReleaseService.release( ecn , releaseTime, batchNumber) ;
			if(hashMap.size()>0)
				return hashMap;
			//发布父件物料信息
			hashMap =ibatisPartReleaseService.release(part, releaseTime, batchNumber) ;
			
			if(hashMap.size()>0)
				return hashMap;
			
			//发布第一层子件和子件关联关系
			releaseFirstLevel( part, releaseTime, batchNumber, wtobj ,  hashMap ) ;
		} catch (WTException e) {
			e.printStackTrace();
		} finally{
			
		}
		
		return hashMap;
	}

	@Override
	public WTKeyedHashMap releasePersistableSingleLevel(WTArrayList parts) {
		WTKeyedHashMap hashMap  = releasePersistableSingleLevel( parts , "" , "",  null ) ;
		return hashMap;
	}

	@Override
	public WTKeyedHashMap releasePersistableSingleLevel(WTArrayList parts,
			String releaseTime, String batchNumber, WTChangeOrder2 ecn) {
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if( parts == null ){
			return hashMap;
		}
		int size = parts.size() ;

		for( int i=0 ; i<size ; i++ ){
			Persistable persistable = null;
			try{
				persistable = parts.getPersistable(i) ;
			}catch(Exception e){
				e.printStackTrace();
				continue;
			}
			if( persistable != null && persistable instanceof WTPart ){
				WTPart part = ( WTPart ) persistable ;
				if (!part.isLatestIteration()){
					//不是最新版本，先获得最新小版本
					try {
						part = (WTPart) VersionControlHelper.service.getLatestIteration(part, false);
					} catch (WTException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				hashMap.putAll(releaseSingleLevel( part , releaseTime, batchNumber,  ecn ) );
			}
		}
		return hashMap;
	}

	@Override
	public WTKeyedHashMap releaseAllLevel(Object obj) {
		WTKeyedHashMap hashMap = releaseAllLevel( obj , "" , "", null ) ;
		return hashMap;
	}

	@Override
	public WTKeyedHashMap releaseAllLevel(Object obj, String releaseTime, String batchNumber,
			WTObject wtobj) {
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if(obj != null && obj instanceof WTPart){
			WTPart part = ( WTPart ) obj ;
			hashMap  = releaseAllLevel( part , "" , "",  wtobj) ;
		}
		return hashMap;
	}

	@Override
	public WTKeyedHashMap releaseAllLevel(WTPart part) {
		WTKeyedHashMap hashMap =releaseAllLevel(part , "" ,"",  null) ;
		return hashMap;
	}

	@Override
	public WTKeyedHashMap releaseAllLevel(WTPart part, String releaseTime, String batchNumber,
			WTObject wtobj ) {
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		if( part == null ){
			return hashMap;
		}
		
		String ecnNumber = "" ;
		WTChangeOrder2 ecn = BOMReleaseService.getECNByWTObj(wtobj);
		if( ecn != null ){
			ecnNumber = ecn.getNumber() ;
		}
		
		if( ecnNumber == null ){
			ecnNumber = "" ;
		}

		//获取同一视图下最新版本的零部件
		try {
			//part = CoreUtil.getWTPartByNumberAndView(part.getNumber(), part.getViewName()) ;
			if (!part.isLatestIteration())//不是最新版本，先获得最新小版本
				part = (WTPart) VersionControlHelper.service.getLatestIteration(part, false);
		} catch (WTException e) {
			e.printStackTrace();
		}
		
		//发布ECN信息到中间表。
		WTKeyedHashMap changeMap = ibatisECNReleaseService.release( ecn , releaseTime, batchNumber) ;
		//根节点发布出现异常，则程序终止
		if( changeMap.size() > 0 ){
			hashMap.putAll( PDMIntegrationLogUtil.getChangeErrorInfo(changeMap, part) );
			
			return hashMap;
		}
		
		//发布根节点物料信息
		WTKeyedHashMap partMap = ibatisPartReleaseService.release( part , releaseTime, batchNumber) ;
		//根节点发布出现异常，则程序终止
		if( partMap.size() > 0 ){
			hashMap.putAll( PDMIntegrationLogUtil.getErrorInfo( partMap ) );
			
			return hashMap;
		}
		
		List<WTPart> childList =  releaseFirstLevel( part , releaseTime , batchNumber,  wtobj , hashMap );

		//必须判断size，否则会导致死循环
		while( ( childList != null ) && ( childList.size() > 0 ) ){
			List<WTPart> tempList = new ArrayList<WTPart>() ;
			tempList.clear();
			
			int childListSize = childList.size() ;
			for(int i=0 ; i < childListSize ; i++ ){
				WTPart childPart = childList.get(i) ;
				
				List<WTPart> tempChildList = releaseFirstLevel( childPart , releaseTime , batchNumber, wtobj , hashMap );
					
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

	@Override
	public WTKeyedHashMap releasePersistableAllLevel(WTArrayList parts) {
		WTKeyedHashMap hashMap  = releasePersistableAllLevel( parts , "" , "",  null ) ;
		return hashMap;
	}

	@Override
	public WTKeyedHashMap releasePersistableAllLevel(WTArrayList parts,
			String releaseTime, String batchNumber,  WTObject wtobj ) {
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		if( parts == null ){
			return hashMap;
		}
		int size = parts.size() ;
		for( int i=0 ; i<size ; i++ ){
			Persistable persistable = null;
			try{
				persistable = parts.getPersistable(i) ;
			}catch(Exception e){
				e.printStackTrace();
				continue;
			}
			if( persistable != null && persistable instanceof WTPart ){
				WTPart part = ( WTPart ) persistable ;
				if (!part.isLatestIteration()){
					//不是最新版本，先获得最新小版本
					try {
						part = (WTPart) VersionControlHelper.service.getLatestIteration(part, false);
					} catch (WTException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();	
					}
				}
				hashMap.putAll( releaseAllLevel( part , releaseTime, batchNumber,  wtobj ) ) ;
			}
		}
		return hashMap;
	}

	@Override
	public List<WTPart> releaseFirstLevel(WTPart part, String releaseTime,
			 String batchNumber, WTObject wtobj ,  WTKeyedHashMap hashMap) {
		List<WTPart> list = new ArrayList<WTPart>() ;
		if( part == null ){
			return list ;
		}
		if (!part.isLatestIteration()){
			try {
				part = (WTPart) VersionControlHelper.service.getLatestIteration(part, false);
			} catch (WTException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		//如果part为特殊的部件类型，不需要做集成，则此Part的BOM结构也不做集成
		boolean canRelease = BussinessRule.canReleasePart(part) ;
		if( ! canRelease ){
			logger.debug("   "+PDMIntegrationLogUtil.notReleaseBOMLogInfo(part)) ;
			
			return list ;
		}
		try {
			logger.debug("   "+PDMIntegrationLogUtil.startReleaseBOMLogInfo(part)) ;
			//发布子件物料信息
			list = BOMStructureUtil.getFirstLevelChildren(part) ;
			
			//added on 20160627, 先判断是否已经发布成功，如果已经发布，则不再更新
			List existParent = BomInfoIbatis.query(part);
			//判断BOM发布状态
				
			boolean pdmRealsed = false;
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
			
			//如果BOMRoot表中存在，且PDM的发布标识为“PDM发布成功”或者“ERP接收成功”，则不在发布BOM
			if(existParent.size() > 0 && pdmRealsed){
				return list ;
			}
			//end added
			
			
			//发布物料
			WTKeyedHashMap partMap = ibatisPartReleaseService.release( list , releaseTime, batchNumber) ;
			if( partMap.size() > 0 ){
				hashMap.putAll( PDMIntegrationLogUtil.getErrorInfo( partMap ) );
				return list;
			}
			//先删除之前已经发布的同一大版本的usageLink记录，保证中间数据库中，同一时间，只存在一个大版本的记录
			hashMap = BomStructureRemoveByParentIbatis.delete(part) ;
			if(hashMap.size()>0)
				return list;
			//发布父子件关联关系
			List<WTPartUsageLink> usageLinkList = BOMStructureUtil.getFirstLevelUsageLink(part) ;
			int size = usageLinkList.size() ;
			logger.debug("   "+"获取结构关系数量为：" + size ) ;
			
			//发布Link关系
			hashMap.putAll(BomStructureIbatis.releaseBOMInfoOfReplaceLink(part, usageLinkList, releaseTime, batchNumber,  wtobj));
			if(hashMap.size()>0)
				return list;
		} catch (Exception e) {
			hashMap.put(part, PDMIntegrationLogUtil.failedReleaseBOMLogInfo(part));
			logger.debug("   "+PDMIntegrationLogUtil.failedReleaseBOMLogInfo(part)) ;
			logger.debug("   "+e.getMessage() ) ;
			e.printStackTrace();
		}
		
		return list ;
	}

	 
	


}
