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

import wt.fc.collections.WTKeyedHashMap;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;

public interface ReplacementReleaseServiceImpl {
	
	/**
	 * 发布特定替代关系
	 * 
	 * @param usageLink
	 */
	public abstract void releaseSubstitute( WTPartUsageLink usageLink);
	
	
	/**
	 * 发布特定替代关系
	 * 
	 * @param usageLink
	 */
	public abstract WTKeyedHashMap releaseSubstitute( WTPartUsageLink usageLink , String releaseTime);
	
	
	/**
	 * 发布全局替代关系
	 * 
	 * @param part
	 */
	public abstract WTKeyedHashMap releaseAlternate( WTPart part)throws Exception;
	
	
	/**
	 * 发布全局替代关系
	 * 
	 * @param part
	 */
	public abstract WTKeyedHashMap releaseAlternate( WTPart part , String releaseTime )throws Exception;
	
	
	/**
	 * 发布全局替代关系
	 * 
	 * @param partMaster
	 */
	public abstract WTKeyedHashMap releaseAlternate( WTPartMaster partMaster )throws Exception;
	
	
	/**
	 * 发布全局替代关系
	 * 
	 * @param partMaster
	 */
	public abstract WTKeyedHashMap releaseAlternate( WTPartMaster partMaster  , String releaseTime )throws Exception;
}
