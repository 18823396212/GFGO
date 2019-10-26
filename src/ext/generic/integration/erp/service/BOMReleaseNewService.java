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

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibatis.sqlmap.client.SqlMapClient;

import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.part.beans.EffectiveBaselineBean;
import ext.appo.part.util.EffecitveBaselineUtil;
import ext.appo.part.util.MversionControlHelper;
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
import wt.workflow.engine.WfProcess;
/**
 * BOM信息发布类
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-10-08
 */

public class BOMReleaseNewService {	
	
	private static final String CLASSNAME = BOMReleaseNewService.class.getName() ;

	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	
	
	
	
	public static WTKeyedHashMap releaseSingleLevelNewECN( WTPart part , String releaseTime, WTObject wtobj , String batchNumber, ObjectReference self,WTChangeOrder2 eco) throws IOException{
		System.out.println("part========ERP==============="+part);
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
			//releaseFirstLevel( part, releaseTime , wtobj ,batchNumber,  hashMap ) ;
			//20181226 用新的发布方法
			releaseFirstLevelNew( part, releaseTime , wtobj ,batchNumber,  hashMap, self,eco) ;

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
	

	public static WTKeyedHashMap releaseSingleLevelNew( WTPart part , String releaseTime, WTObject wtobj , String batchNumber, ObjectReference self) throws IOException{
		System.out.println("part========ERP==============="+part);
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
			//releaseFirstLevel( part, releaseTime , wtobj ,batchNumber,  hashMap ) ;
			//20181226 用新的发布方法
			releaseFirstLevelNew( part, releaseTime , wtobj ,batchNumber,  hashMap, self,ecn) ;

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
	 * 发布WTArrayList列表中的所有零部件的单层BOM结构
	 * 如果不在ECN中，则WTChangeOrder2 ecn设置为null
	 * 
	 * @param parts
	 * @param releaseTime
	 * @param wtobj
	 * @param batchNumber
	 * @throws IOException 
	 */
	public static WTKeyedHashMap releasePersistableSingleLevelNew(WTArrayList parts, String releaseTime, WTObject wtobj,
			String batchNumber, ObjectReference self) throws WTException, IOException {
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if (parts == null) {
			return hashMap;
		}

		try {
			
			//ECN发布后数据
				WTChangeOrder2 ecn = getECNByWTObj(wtobj);
				if(ecn != null) {
				  QueryResult qs = ChangeHelper2.service.getChangeActivities(ecn);
			      while (qs.hasMoreElements()) {
		                WTChangeActivity2 activity = (WTChangeActivity2) qs.nextElement();
		                System.out.println("eca???==========================================="+activity);
		                QueryResult qsafter = ChangeHelper2.service.getChangeablesAfter(activity);
		                while (qsafter.hasMoreElements()) {
		                    Object obj = qsafter.nextElement();
		                    if (obj instanceof WTPart) {
		                        WTPart part = (WTPart) obj;
		                        
		                        String partNumber = part.getNumber();
				    			Object obj1 = partNumber.substring(0,1);
				    			if(obj1.toString().equals("A") || obj1.toString().equals("B")){
		                        
			                        if (part.getLifeCycleState().toString().equals(ChangeConstants.RELEASED)) {
										WTKeyedHashMap map = releaseSingleLevelNew(part, releaseTime, wtobj, batchNumber,self);
										System.out.println("map==============ERP=============="+map);
										if (map != null && map.size() > 0) {
											hashMap.putAll(map);
										}
										if (hashMap.size() > 0) {
											return hashMap;// 失败就返回
										}
			                        }
				    			}
						}
		       }
		    }
			}else{
				
				int size = parts.size();
				System.out.println("parts===========ERP==============="+parts+",size="+size);
				for (int i = 0; i < size; i++) {
					Persistable persistable = parts.getPersistable(i);
					System.out.println("persistable=========ERP================"+persistable);
					if (persistable != null && persistable instanceof WTPart) {
						WTPart part = (WTPart) persistable;
						
						String partNumber = part.getNumber();
		    			Object obj1 = partNumber.substring(0,1);
		    			if(obj1.toString().equals("A") || obj1.toString().equals("B")){
							System.out.println("part===========ERP========"+part);
							WTKeyedHashMap map = releaseSingleLevelNew(part, releaseTime, wtobj, batchNumber,self);
							System.out.println("map==============ERP=============="+map);
							if (map != null && map.size() > 0) {
								hashMap.putAll(map);
							}
							if (hashMap.size() > 0) {
								return hashMap;// 失败就返回
							}
			    		}
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
	


	/**
	 * 发布单层BOM结构
	 *
	 * @param part
	 * @param releaseTime
	 * @param wtobj
	 * @param batchNumber
	 * @param hashMap
	 * @return
	 * @throws IOException 
	 * @throws WTException 
	 */
	private static List<WTPart> releaseFirstLevelNew( WTPart part , String releaseTime , WTObject wtobj , String batchNumber, WTKeyedHashMap hashMap , ObjectReference self,WTChangeOrder2 ecn) throws IOException, WTException{
		//先判断是否需要发布
		//返回参数part的子件列表
		List<WTPart> childPartList = new ArrayList<WTPart>() ;

//		 WfProcess process = (WfProcess) self.getObject();
//		 String pid = MversionControlHelper.getOidByObject(process);
		Set<EffectiveBaselineBean> listBean = EffecitveBaselineUtil.checkSendEffectiveBaselineData(part);
		System.out.println("listBean===================ERP===================="+listBean);
		//20181226取提交时抓的基线物料
		Map<WTPartUsageLink, WTPart> linkParts = new HashMap<WTPartUsageLink, WTPart>();
		for(EffectiveBaselineBean bean : listBean){
		    if(bean.getUsageLinkoid() != null && bean.getUsageLinkoid() != "") {			
		    	WTPart sonPart = (WTPart)EffecitveBaselineUtil.getObjectByOid(bean.getPartoid());
		      	WTPartUsageLink  link=(WTPartUsageLink)EffecitveBaselineUtil.getObjectByOid(bean.getUsageLinkoid());
		      	linkParts.put(link, sonPart);
		    }
		}
		System.out.println("linkParts========ERP================"+linkParts.toString());

		//try {
//			childPartList = BOMStructureUtil.getFirstLevelChildren(part);
			//20181226此处获取提交后保存的基线物料的第一层子物料
			childPartList.addAll(linkParts.values());
		//} catch (ConfigException e1) {
		//	e1.printStackTrace();
		//} catch (WTException e1) {
		//	e1.printStackTrace();
		//}

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

					//List<WTPartUsageLink> usageLinkList = BOMStructureUtil.getFirstLevelUsageLink(part) ;
					//20181226 需要获取抓取的基线物料中的UsageLink
					List<WTPartUsageLink> usageLinkList = new ArrayList<>();
					usageLinkList.addAll(linkParts.keySet());
                    System.out.println("usageLinkList==================="+usageLinkList);
					
					for( int i = 0 ; i < usageLinkList.size() ; i++ ){
						WTPartUsageLink usageLink = usageLinkList.get(i) ;
						if(logger.isDebugEnabled()){
							logger.debug("   "+"发布BOM结构信息:\n" + CommonPDMUtil.getWTPartUsageLinkInfo(usageLink) ) ;
						}
						//获取子件
						WTPartMaster childMaster = usageLink.getUses() ;
						WTPart childPart = null;
						//try {
							//childPart = CoreUtil.getWTPartByMasterAndView(childMaster, part.getViewName());
							//20181226 此处取基线中物料记录的物料
							childPart = linkParts.get(usageLink);
							System.out.println("childPart====================="+childPart);
						//} catch (WTException e) {

						//	e.printStackTrace();
						//	continue;
						//}
						//判断子件是否为可发布类型
						if( BussinessRule.canReleasePart( childPart ) ){
							System.out.println("isSendChild==================Y?");
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

					System.out.println("childPartList=========ERP=========="+childPartList);
					
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

	
}
