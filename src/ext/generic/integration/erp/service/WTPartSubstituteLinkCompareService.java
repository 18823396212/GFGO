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

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import wt.identity.IdentityFactory;
import wt.log4j.LogR;
import wt.part.SubstituteQuantity;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.part.WTPartSubstituteLink;
import wt.part.WTPartUsageLink;
import wt.util.WTException;
import wt.vc.config.ConfigException;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.generic.integration.erp.util.BOMStructureUtil;

/*
 * 替代件比较差异
 * 
 */
public class WTPartSubstituteLinkCompareService {
	
	private static final String CLASSNAME =  WTPartSubstituteLinkCompareService.class.getName();
	private static final Logger logger = LogR.getLogger(CLASSNAME);
	
	public static final String SEPARATOR = "@@@";
	/**
	 * 比较aPart和oldPart第一层BOM的特定替代的差异
	 * 
	 * @param aPart
	 * @param oldPart
	 * @param needCreateSubsituteLinkList
	 * @param needUpdateSubsituteLinkList
	 * @param needDeleteSubsituteLinkList
	 */
	public static void getSubsiteteDifferenceLink(WTPart aPart ,WTPart oldPart, 
			List<WTPartSubstituteLink> needCreateSubsituteLinkList,
			List<WTPartSubstituteLink> needUpdateSubsituteLinkList,
			List<WTPartSubstituteLink> needDeleteSubsituteLinkList) {
		
		logger.debug("Enter In getSubsiteteDifferenceLink()...") ;
		
		try {
			
			if(logger.isDebugEnabled() ){
				logger.debug("current release part is : " + IdentityFactory.getDisplayIdentifier( aPart ) ) ;
				logger.debug("Previous release part is : " + IdentityFactory.getDisplayIdentifier( oldPart ) ) ;
			}
			
			//获取aPart的第一层BOM结构的WTPartUsageLink对象
			List<WTPartSubstituteLink> aPartUsageLinks = BOMStructureUtil.getWTPartSubsituteLink( aPart ) ;
			
			logger.debug("aPartUsageLinks size = " + aPartUsageLinks.size() ) ;
			
			//获取oldPart的第一层BOM结构的WTPartUsageLink对象
			List<WTPartSubstituteLink> oldPartUsageLinks = BOMStructureUtil.getWTPartSubsituteLink( oldPart ) ;
			
			logger.debug("oldPartUsageLinks size = " + oldPartUsageLinks.size() ) ;
			
			//遍历aPart第一层BOM结构
			Iterator<WTPartSubstituteLink> aPartUsageLinksIte = aPartUsageLinks.iterator() ;
			while( aPartUsageLinksIte.hasNext() ){
				//获取aPart第一层BOM结构中的某一个WTPartUsageLink对象
				WTPartSubstituteLink aLink = aPartUsageLinksIte.next() ;
				
				if(logger.isDebugEnabled() ){
					logger.debug("current release part WTPartUsageLink is : " + IdentityFactory.getDisplayIdentifier( aLink ) ) ;
				}
				
				//从oldPart的第一层BOM结构中获取与aLink对应的WTPartUsageLink对象
				WTPartSubstituteLink oldPartLink = getOldLink( oldPartUsageLinks , aLink ) ;
				
				if( oldPartLink == null ){
					if(logger.isDebugEnabled() ){
						logger.debug("Previous release part WTPartUsageLink is null") ;
					}
					
					//如果oldPart的第一层BOM结构没有与aLink对应的WTPartUsageLink对象，则aLink在aPart的第一层BOM结构属于新增的Link关系
					needCreateSubsituteLinkList.add( aLink ) ;
				}else {
					if(logger.isDebugEnabled() ){
						logger.debug("Previous release part WTPartUsageLink is : " + IdentityFactory.getDisplayIdentifier( oldPartLink ) ) ;
					}
					
					boolean isEqual = isEqual( aLink , oldPartLink ) ;
					logger.debug("isEqual = " + isEqual ) ;
					
					//如果oldPart的第一层BOM结构存在与aLink对应的WTPartUsageLink对象oldPartLink，则比较aLink和oldPartLink对象是否一样
					if( ! isEqual ){
						
						//如果不一样，则表示aLink在aPart的第一层BOM结构做过更新
						needUpdateSubsituteLinkList.add( aLink ) ;
					}
					
					///如果oldPart的第一层BOM结构存在与aLink对应的WTPartUsageLink对象oldPartLink，则比较完成后，将oldPartLink从List中移除，表明此Link关系已经比较过了
					oldPartUsageLinks.remove( oldPartLink ) ;
				}
			}
			
			//oldPartUsageLinks中剩下的Link关系，都在aPartUsageLinks没有对应的Link关系存在，因此，这部分Link关系，已经在aPart第一层BOM结构移除掉了
			needDeleteSubsituteLinkList.addAll(oldPartUsageLinks) ;

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
	public static boolean isEqual(WTPartSubstituteLink aSubstituteLink, WTPartSubstituteLink oldSubstituteLink) {
		boolean isSameLink = true ;
		try {
			//判断替代料的编码是否一致
			WTPartMaster aSubstitutePartMaster = aSubstituteLink.getSubstitutes() ;
			String aSubstituteNumber = aSubstitutePartMaster.getNumber();
			
			
			WTPartMaster oldSubstitutePartMaster = oldSubstituteLink.getSubstitutes() ;
			String oldSubstituteNumber = oldSubstitutePartMaster.getNumber();
			
			if(!aSubstituteNumber.equals(oldSubstituteNumber)){
				isSameLink = false;
			}
			
			//判断替代料的数量是否一致
			SubstituteQuantity aSubstituteQuantity = aSubstituteLink.getQuantity();
			String quantity = CommonPDMUtil.getQuantityValue(aSubstituteQuantity);
			
			SubstituteQuantity oldSubstituteQuantity = oldSubstituteLink.getQuantity();
			String quantityOld = CommonPDMUtil.getQuantityValue(oldSubstituteQuantity) ;
			if(!quantity.equalsIgnoreCase(quantityOld)){
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
	public static String uniqueKey(WTPartSubstituteLink link) throws WTException {
		if (link == null) {
			throw new WTException("Object must not be null");
		}
		WTPartUsageLink usageLink = link.getSubstituteFor();
		WTPartMaster partMaster = link.getSubstitutes();

		return usageLink.getComponentId() + SEPARATOR + partMaster.getNumber();
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
	public static boolean isSameLink(WTPartSubstituteLink link1, WTPartSubstituteLink link2) throws WTException {
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
	private static WTPartSubstituteLink getOldLink(List<WTPartSubstituteLink> oldPartUsageLinks, WTPartSubstituteLink aLink) throws WTException {
		logger.debug("Enter In getOldLink(List<WTPartUsageLink> oldPartUsageLinks, WTPartUsageLink aLink)...") ;
		
		WTPartSubstituteLink oldLink = null ;
		
		//查找相同的Link
		Iterator<WTPartSubstituteLink> oldPartUsageLinksIte = oldPartUsageLinks.iterator() ;
		while( oldPartUsageLinksIte.hasNext() ){
			WTPartSubstituteLink tempLink = oldPartUsageLinksIte.next() ;
			
			if(isSameLink( tempLink, aLink)){
				oldLink = tempLink ;
				break;
			}
		}
		
		
		return oldLink ;
	}
}
