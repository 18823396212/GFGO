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

import org.apache.log4j.Logger;

import com.ibatis.sqlmap.client.SqlMapClient;

import ext.com.core.CoreUtil;
import ext.com.workflow.WorkflowUtil;
import ext.generic.integration.erp.bean.MaterialInfo;
import ext.generic.integration.erp.ibatis.IbatisSqlConstant;
import ext.generic.integration.erp.ibatis.PartIbatis;
import ext.generic.integration.erp.rule.BussinessRule;
import ext.generic.integration.erp.util.BusinessRuleXMLConfigUtil;
import ext.generic.integration.erp.util.GenerateBeanInfo;
import ext.generic.integration.erp.util.IbatisUtil;
import wt.change2.WTChangeOrder2;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTKeyedHashMap;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.workflow.engine.WfProcess;

public class IbatisPartReleaseService implements PartReleaseServiceImpl{

	private static final String clazz = IbatisPartReleaseService.class.getName() ;
	private static final Logger logger = LogR.getLogger(clazz);
	
	@Override
	public WTKeyedHashMap release(Object obj) {
		WTKeyedHashMap hashMap =release( obj , "" , "") ;
		return hashMap;
	}

	@Override
	public WTKeyedHashMap release(WTPart part) {
		WTKeyedHashMap hashMap =release( part , "" , "") ;
		return hashMap;
		
	}

	@Override
	public WTKeyedHashMap release(Object obj, String releaseTime, String batchNumber) {
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if( obj != null && obj instanceof WTPart){
			WTPart part = ( WTPart ) obj ;
			
			hashMap = release( part , releaseTime , batchNumber) ;
		}
		return hashMap;
	}
	
	public WTKeyedHashMap release(WTPart part, String releaseTime,String batchNumber){
		return release( part , releaseTime , batchNumber, null) ;
	}
	
	public WTKeyedHashMap release(WTPart part, String releaseTime,String batchNumber,ObjectReference self, Object... params) {
		if (!part.isLatestIteration()){
			try {
				part = (WTPart) VersionControlHelper.service.getLatestIteration(part, false);
			} catch (WTException e2) {
				e2.printStackTrace();
			}
		}
		
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		//判断Part是否已经发布，如果已经发布成功，则不再发布
		if(PartReleaseService.isPartReleasedSuccess(part)){
			return hashMap;
		}
		
		boolean canRelease = BussinessRule.canReleasePart(part) ;
		logger.debug("部件是否可发布："+canRelease);
		if( canRelease ){
			if( part.isPhantom() ){
				//如果部件为虚拟件，判断用户是否需要发布虚拟件
				if( BusinessRuleXMLConfigUtil.getInstance().getIsReleasePhantom().equals("false") ){
					return hashMap;
				}
			}
			if( part.getHidePartInStructure() ){
				//如果部件为收集件，判断用户是否需要发布收集件
				if( BusinessRuleXMLConfigUtil.getInstance().getIsReleaseHidePartInStructure().equals("false") ){
					return hashMap;
				}
			}
			
			MaterialInfo materialInfo=null;
			try {
				materialInfo = GenerateBeanInfo.createMaterilInfoBeanByPart(part, releaseTime, batchNumber);
				String ecnNum = "";
				if(params!=null && params.length>0){
					ecnNum = (String) params[0];
				}else{
					if(self != null){
						WfProcess process = WorkflowUtil.getProcess(self);
						if(process.getBusinessObjReference() != null){
							WTObject wtObj = CoreUtil.getWTObjectByOid(process.getBusinessObjReference());
							if(wtObj != null){
								WTChangeOrder2 ecn = BOMReleaseService.getECNByWTObj(wtObj);
								if(ecn != null){
									ecnNum = ecn.getNumber();
								}
							}
						}
					}
				}
				materialInfo.setEcnNumber(ecnNum);
				
			} catch (Exception e1) {
				e1.printStackTrace();
				hashMap.put( part, "生成materialInfo对象失败：" + e1.toString() );
				return hashMap;
			}
			
			//判断是否存在中间表
			boolean flag = false;
			//获取同一视图下最新版本的零部件
			try {
				logger.debug("   "+"执行查询零部件信息操作") ;
				flag = PartIbatis.hasObject(materialInfo);
			} catch (SQLException e) {
				hashMap.put( part, "查询中间数据库物料发生异常：" + e.toString() );
				e.printStackTrace();
				return hashMap;
			}
			if(flag){
				logger.debug("   "+"执行查询零部件信息操作") ;
				try {
					PartIbatis.updateMaterialInfo(materialInfo);
				} catch (SQLException e) {
					hashMap.put( part, "更新物料发生异常：" + e.toString() );
					e.printStackTrace();
				}
			}else{
				logger.debug("   "+"执行插入零部件信息操作") ;
				try {
					PartIbatis.insertMaterialInfo(materialInfo);
				} catch (SQLException e) {
					hashMap.put( part, "插入物料发生异常：" + e.toString() );
					e.printStackTrace();
				}
			}
			if( hashMap.size() > 0 ){
				logger.debug("   "+"物料发布出现异常，判断物料发布失败") ;
				//发布出现异常，发布状态为：物料发布失败
				//UpdatePDMDataService.updatePDMFailed( part , BusinessRuleXMLConfigUtil.getInstance().getPartReleaseStatus() );
				return hashMap;
			}else{
				//更新时，如果没有出现异常则判断为更新成功
				UpdatePDMDataService.updatePDMSucessful( part , BusinessRuleXMLConfigUtil.getInstance().getPartReleaseStatus() );
				logger.debug("   "+"物料更新成功，PDM系统状态设置为完成");
			}
		}else{
//			logger.debug("   "+PDMIntegrationLogUtil.notReleasePartLogInfo(part)) ;
		}
		
		return hashMap;
	}

	@Override
	public WTKeyedHashMap releasePersistable(WTArrayList parts) throws Exception{
		WTKeyedHashMap hashMap = releasePersistable(parts, "", "");
		return hashMap;
	}

	@Override
	public WTKeyedHashMap releasePersistable(WTArrayList parts,
			String releaseTime, String batchNumber) throws Exception {

		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		WTArrayList newParts = new WTArrayList();
		if( parts == null||parts.size()==0 ){
			return hashMap;
		}
		SqlMapClient sqlMap = null;
		sqlMap = IbatisUtil.getSqlMapInstance();
		try{
			sqlMap.startTransaction();
			sqlMap.startBatch();
			for(int i=0;i<parts.size();i++){
				Persistable p = null;
				try{
					p = parts.getPersistable(i);
				}catch(Exception e){
					e.printStackTrace();
					continue;
				}
				if(p instanceof WTPart){
					WTPart part = (WTPart) p;
					if (!part.isLatestIteration())//不是最新版本，先获得最新小版本
						part = (WTPart) VersionControlHelper.service.getLatestIteration(part, false);
					boolean canRelease = BussinessRule.canReleasePart(part) ;
					if(!canRelease)
						continue;
					
					//判断Part是否已经发布，如果已经发布成功，则不再发布
					if(PartReleaseService.isPartReleasedSuccess(part)){
						continue;
					}
					
					newParts.add(part);
					MaterialInfo materialInfo=null;
					try {
						materialInfo = GenerateBeanInfo.generateMaterilInfoByPart(part, releaseTime, batchNumber);
//						materialInfo.setReleaseDate(releaseTime);
//						materialInfo.setFlag(IntegrationConstant.PDM_RELEASE_STATUS);
					} catch (WTException e1) {
						e1.printStackTrace();
						hashMap.put( part, "生成materialInfo对象失败：" + e1.toString() );
						continue;
					}
					if( part.isPhantom() ){
						//如果部件为虚拟件，判断用户是否需要发布虚拟件
						if( BusinessRuleXMLConfigUtil.getInstance().getIsReleasePhantom().equals("false") ){
							continue;
						}
					}
					if( part.getHidePartInStructure() ){
						//如果部件为收集件，判断用户是否需要发布收集件
						if( BusinessRuleXMLConfigUtil.getInstance().getIsReleaseHidePartInStructure().equals("false") ){
							continue;
						}
					}
//					logger.debug("   "+ PDMIntegrationLogUtil.startReleasePartLogInfo(part)) ;
					//判断是否存在中间表
					boolean flag = false;
					//获取同一视图下最新版本的零部件
					try {
						logger.debug("   "+"执行查询零部件信息操作") ;
						flag = PartIbatis.hasObject(materialInfo,sqlMap);
					} catch (SQLException e) {
						hashMap.put( part, "查询中间数据库物料发生异常：" + e.toString() );
						e.printStackTrace();
						continue;
					}
					if(flag){
						logger.debug("   "+"执行更新物料信息操作") ;
						try {
							sqlMap.update(IbatisSqlConstant.UPDATE_MATERIAL_INFO, materialInfo);
							//updateMaterialInfo(materialInfo);
						} catch (SQLException e) {
							hashMap.put( part, "更新物料发生异常：" + e.toString() );
							e.printStackTrace();
							continue;
						}
					}else{
						logger.debug("   "+"执行插入零部件信息操作") ;
						try {
							sqlMap.insert(IbatisSqlConstant.INSERT_MATERIAL_INFO, materialInfo);
							//insertMaterialInfo(materialInfo);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							hashMap.put( part, "插入物料发生异常：" + e.toString() );
							e.printStackTrace();
							continue;
						}
					}
					/*if( hashMap.size() > 0 ){
						logger.debug("   "+finishReleasePartLogInfo(part)) ;
						logger.debug("   "+"物料发布出现异常，判断物料发布失败") ;
						//发布出现异常，发布状态为：物料发布失败
						UpdatePDMDataService.updatePDMFailed( part , BusinessRuleXMLConfigUtil.getInstance().getPartReleaseStatus() );
						return hashMap;
					}else{
						//更新时，如果没有出现异常则判断为更新成功
						UpdatePDMDataService.updatePDMSucessful( part , BusinessRuleXMLConfigUtil.getInstance().getPartReleaseStatus() );
						logger.debug("   "+"物料更新成功，PDM系统状态设置为完成");
					}*/
				}
			}
			if(hashMap.size()==0){
				sqlMap.executeBatch();
				sqlMap.commitTransaction();
				for(int i=0;i<newParts.size();i++){
					WTPart part = (WTPart) newParts.getPersistable(i);
					UpdatePDMDataService.updatePDMSucessful( part , BusinessRuleXMLConfigUtil.getInstance().getPartReleaseStatus() );
				}	
			}
		}finally{
			sqlMap.endTransaction();
			IbatisUtil.closeSqlMapInstance();
		}
		return hashMap;
	}

	@Override
	public WTKeyedHashMap release(List<WTPart> parts) throws Exception {
		WTKeyedHashMap hashMap =release( parts , "", "") ;
		return hashMap;
	}

	@Override
	public WTKeyedHashMap release(List<WTPart> parts, String releaseTime, String batchNumber) throws Exception {
		WTArrayList list = new WTArrayList();
		list.addAll(parts);
		WTKeyedHashMap hashMap  = releasePersistable(list, releaseTime, batchNumber);
		return hashMap;
	}
	

}
