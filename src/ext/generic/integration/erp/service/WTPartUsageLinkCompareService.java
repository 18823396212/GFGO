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

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import wt.fc.QueryResult;
import wt.identity.IdentityFactory;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.part.WTPartUsageLink;
import wt.pom.PersistenceException;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.config.ConfigException;
import ext.com.iba.IBAUtil;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.generic.integration.erp.util.BOMStructureUtil;
import ext.generic.integration.erp.util.BusinessRuleXMLConfigUtil;
import ext.generic.integration.erp.util.IntegrationConstant;

/*
 * 比较BOM差异
 * 
 */
public class WTPartUsageLinkCompareService {
	
	private static final String CLASSNAME =  WTPartUsageLinkCompareService.class.getName();
	private static final Logger logger = LogR.getLogger(CLASSNAME);
	/**
	 * 比较aPart和oldPart第一层BOM结构的差异
	 * 
	 * @param aPart
	 * @param oldPart
	 * @param needCreateUsageLinkList
	 * @param needUpdateUsageLinkList
	 * @param needDeleteUsageLinkList
	 */
	public static void getFirstLevelDifferenceLink(WTPart aPart ,WTPart oldPart, 
			List<WTPartUsageLink> needCreateUsageLinkList,
			List<WTPartUsageLink> needUpdateUsageLinkList,
			List<WTPartUsageLink> needDeleteUsageLinkList) {
		
		logger.debug("Enter In getFirstLevelDifferenceLink()...") ;
		
		try {
			
			if(logger.isDebugEnabled() ){
				logger.debug("current release part is : " + IdentityFactory.getDisplayIdentifier( aPart ) ) ;
				logger.debug("Previous release part is : " + IdentityFactory.getDisplayIdentifier( oldPart ) ) ;
			}
			
			//获取aPart的第一层BOM结构的WTPartUsageLink对象
			List<WTPartUsageLink> aPartUsageLinks = BOMStructureUtil.getFirstLevelUsageLink( aPart ) ;
			
			logger.debug("aPartUsageLinks size = " + aPartUsageLinks.size() ) ;
			
			//获取oldPart的第一层BOM结构的WTPartUsageLink对象
			List<WTPartUsageLink> oldPartUsageLinks = BOMStructureUtil.getFirstLevelUsageLink( oldPart ) ;
			
			logger.debug("oldPartUsageLinks size = " + oldPartUsageLinks.size() ) ;
			
			//遍历aPart第一层BOM结构
			Iterator<WTPartUsageLink> aPartUsageLinksIte = aPartUsageLinks.iterator() ;
			while( aPartUsageLinksIte.hasNext() ){
				//获取aPart第一层BOM结构中的某一个WTPartUsageLink对象
				WTPartUsageLink aLink = aPartUsageLinksIte.next() ;
				
				if(logger.isDebugEnabled() ){
					logger.debug("current release part WTPartUsageLink is : " + IdentityFactory.getDisplayIdentifier( aLink ) ) ;
				}
				
				//从oldPart的第一层BOM结构中获取与aLink对应的WTPartUsageLink对象
				WTPartUsageLink oldPartLink = getOldLink( oldPartUsageLinks , aLink ) ;
				
				if( oldPartLink == null ){
					if(logger.isDebugEnabled() ){
						logger.error("Previous release part WTPartUsageLink is null") ;
					}
					
					//如果oldPart的第一层BOM结构没有与aLink对应的WTPartUsageLink对象，则aLink在aPart的第一层BOM结构属于新增的Link关系
					needCreateUsageLinkList.add( aLink ) ;
				}else {
					if(logger.isDebugEnabled() ){
						logger.debug("Previous release part WTPartUsageLink is : " + IdentityFactory.getDisplayIdentifier( oldPartLink ) ) ;
					}
					
					boolean isEqual = isEqual( aLink , oldPartLink ) ;
					logger.debug("isEqual = " + isEqual ) ;
					
					//如果oldPart的第一层BOM结构存在与aLink对应的WTPartUsageLink对象oldPartLink，则比较aLink和oldPartLink对象是否一样
					if( ! isEqual ){
						
						//如果不一样，则表示aLink在aPart的第一层BOM结构做过更新
						needUpdateUsageLinkList.add( aLink ) ;
					}
					
					///如果oldPart的第一层BOM结构存在与aLink对应的WTPartUsageLink对象oldPartLink，则比较完成后，将oldPartLink从List中移除，表明此Link关系已经比较过了
					oldPartUsageLinks.remove( oldPartLink ) ;
				}
			}
			
			//oldPartUsageLinks中剩下的Link关系，都在aPartUsageLinks没有对应的Link关系存在，因此，这部分Link关系，已经在aPart第一层BOM结构移除掉了
			needDeleteUsageLinkList.addAll(oldPartUsageLinks) ;

		} catch (ConfigException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * 相等，表示两个对象的所有属性必须完全相等
	 * 
	 * @param link1
	 * @param link2
	 * @return
	 * @throws WTException 
	 * @throws IntegrationException 
	 */
	public static boolean isEqual(WTPartUsageLink aLink, WTPartUsageLink oldPartLink) {
		boolean isSameLink = true ;
		try {
			String childNumber = aLink.getUses().getNumber();
			String oldChildNumber = oldPartLink.getUses().getNumber();
			if(!childNumber.equalsIgnoreCase(oldChildNumber)){
				isSameLink = false;
			}
			String quantity = CommonPDMUtil.getQuantityValue(aLink) ;
			String quantityOld = CommonPDMUtil.getQuantityValue(oldPartLink) ;
			if(!quantity.equalsIgnoreCase(quantityOld)){
				isSameLink = false;
			}
			
			String unit = CommonPDMUtil.getUnitInternalValue( aLink.getUses().getDefaultUnit() ) ;
			
			String oldUnit = CommonPDMUtil.getUnitInternalValue( oldPartLink.getUses().getDefaultUnit()) ;
			
			if(!unit.equalsIgnoreCase(oldUnit)){
				isSameLink = false;
				
			}
		} catch (Exception e) {
			isSameLink = false;
			e.printStackTrace();
		}					
		return isSameLink ;
	}
	
	/**
	 * 对象唯一标示字符串
	 * 
	 * 如果此字符串相同，则表示两个WTPartUsageLink对象是同一个对象，但是业务属性可以不同
	 * 
	 * @param t
	 * @return
	 * @throws WTException 
	 */
	public static String uniqueKey(WTPartUsageLink usageLink) throws WTException {
		if(usageLink == null){
			throw new WTException("Object must not be null") ;
		}
		return usageLink.getComponentId();
	}
	
	/**
	 * 相似，表示两个对象的标志属性完全相同，但是业务属性可以相同，也可以不同
	 * WTPartUsageLink默认的标示属性是 componentId
	 * 具体可参见系统默认配置文件：codebase/com/ptc/core/ocmp/config/ObjComparisonConfig.xml
	 * 
	 * @param link1
	 * @param link2
	 * @return
	 * @throws WTException 
	 */
	public static boolean isSameLink(WTPartUsageLink link1, WTPartUsageLink link2) throws WTException {
		if(link1 == null || link2 == null) {
			return false ;
		}else{
			String uniqueKey1 = uniqueKey(link1);
			String uniqueKey2 = uniqueKey(link2) ;
			if(uniqueKey1 == null){
				return false ;
			}else{
				return uniqueKey1.equals(uniqueKey2) ;
			}
		}
	}

	/**
	 * 获取aLink对象在oldPartUsageLinks对应的Link关系
	 * 
	 * @param oldPartUsageLinks
	 * @param aLink
	 * @return
	 * @throws WTException 
	 */
	private static WTPartUsageLink getOldLink(List<WTPartUsageLink> oldPartUsageLinks, WTPartUsageLink aLink) throws WTException {
		logger.debug("Enter In getOldLink(List<WTPartUsageLink> oldPartUsageLinks, WTPartUsageLink aLink)...") ;
		
		WTPartUsageLink oldLink = null ;
		
		//查找相同的Link
		Iterator<WTPartUsageLink> oldPartUsageLinksIte = oldPartUsageLinks.iterator() ;
		while( oldPartUsageLinksIte.hasNext() ){
			WTPartUsageLink tempLink = oldPartUsageLinksIte.next() ;
			
			if(isSameLink( tempLink, aLink)){
				oldLink = tempLink ;
				break;
			}
		}
		
		
		return oldLink ;
	}
	
	/**
	 * 获取发布成功的历史版本，如果没有，则返回空
	 * @param part
	 * @return
	 * @throws WTException
	 * @throws PersistenceException
	 * @throws RemoteException
	 */
	public static WTPart findHistoryParts(WTPart part) throws WTException,PersistenceException, RemoteException {
	//根据part的主数据获取其所有的版本历史记录部件，且这些历史版本部件都已排序，排序方式为：B2、B1、A2、A1
		 QueryResult result=VersionControlHelper.service.allIterationsOf(part.getMaster());
		 String viewName = part.getViewName();
		 WTPart returnpart = null;
		 logger.debug("========result "+result.size());
		 while(result.hasMoreElements()){
			 WTPart hisPart=(WTPart)result.nextElement();
			 String hisViewName=hisPart.getViewName();
			 logger.debug("===hisViewName.view "+hisPart.getNumber()+" "+hisPart.getViewName()+" "+hisPart.getVersionIdentifier().getValue()+hisPart.getIterationIdentifier().getValue());
				
			 if(hisViewName.equals(viewName)){
				String bomPublishState="";
				Object iba = IBAUtil.getIBAValue(hisPart, BusinessRuleXMLConfigUtil.getInstance().getBomReleaseStatus());
				if(iba !=null){
					bomPublishState = iba.toString();
				}
				
				if(IntegrationConstant.PDM_RELEASE_STATUS_CN.equals(bomPublishState)
				   || IntegrationConstant.ERP_PROCESS_SUCCESS_CN.equals(bomPublishState)){
							returnpart = hisPart;
							break;							
				}
			 }
		 }
		 return returnpart;
	}
	/**
	 * 如果当前零部件的大版本为初始大版本，则当前零部件是第一次发布
	 * 
	 * 如果当前零部件的大版本不是初始大版本，则当前零部件的大版本的初始小版本就是上一个发布版本
	 * 
	 * @param part
	 * @return
	 */
	public static WTPart getPreviousRelease(WTPart part) {
		logger.debug("Enter In getPreviousRelease(WTPart part)...") ;
		
		if(logger.isDebugEnabled() ){
			logger.debug("current release part is : " + IdentityFactory.getDisplayIdentifier( part ) ) ;
		}
		
		WTPart oldPart = null ;
		/**
		 * 获取历史记录里面已经发布成功的版本部件，如果没有历史记录，则返回空，如果有历史记录但是发布都是失败的，则
		 * 返回空，如果历史记录有发布成功的，则对所有成功的进行排序，然后返回发布成功的最新版本
		*/
	
		try {
			logger.debug("===part "+part.getNumber()+" "+part.getViewName()+" "+part.getVersionIdentifier().getValue()+part.getIterationIdentifier().getValue());
			
			oldPart = findHistoryParts( part);
			
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return oldPart ;
	}

//	/**
//	 * 判断当前的零部件是否为初始大版本，例如A版本，初始大版本配置在文件ext.generic.integration.erp.config.ERPInitialized.properties文件中
//	 * Key值作为常量，定义为ext.generic.integration.erp.config.ERPInitializedConstant.INIT_PART_MAJOR_VERSION
//	 * 
//	 * @param part
//	 * @return
//	 */
//	private static boolean isInitializedVersion(WTPart part) {
//		logger.debug("Enter In isInitializedVersion(WTPart part)...") ;
//		
//		boolean isInitializedVersion = false ;
//
//		//获取配置文件中的初始大版本
//		String initializedPartMajorVersion = (String) ERPInitialized.getInstance().readProperties().get( ERPInitializedConstant.INIT_PART_MAJOR_VERSION ) ;
//		
//		logger.debug("initializedPartMajorVersion = " + initializedPartMajorVersion) ;
//		
//		//获取当前零部件的大版本
//		String partMajorVersion = CommonPDMUtil.getMajorVersion( part ) ;
//		
//		logger.debug("partMajorVersion = " + partMajorVersion) ;
//		
//		if( ( partMajorVersion != null ) && partMajorVersion.equals( initializedPartMajorVersion ) ) {
//			isInitializedVersion = true ;
//		}
//		
//		return isInitializedVersion ;
//	}
}
