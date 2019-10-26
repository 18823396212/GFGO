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

import java.util.List;

import wt.fc.ObjectReference;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTKeyedHashMap;
import wt.part.WTPart;
import wt.util.WTException;

public interface PartReleaseServiceImpl {
	
	
	/**
	 * 发布PBO物料信息
	 * 
	 * @param obj
	 */
	public abstract WTKeyedHashMap release(Object obj);
	
	/**
	 * 发布零部件物料信息
	 * 
	 * @param part
	 */
	public abstract WTKeyedHashMap release(WTPart part);
	
	/**
	 * 发布PBO物料信息
	 * 
	 * @param obj
	 * @param releaseTime
	 */
	public abstract WTKeyedHashMap release(Object obj , String releaseTime, String batchNumber);
	
	
	/**
	 * 发布零部件物料信息
	 * 
	 * @param part
	 * @param releaseTime
	 */
	public abstract WTKeyedHashMap release(WTPart part , String releaseTime, String batchNumber);
	
	public abstract WTKeyedHashMap release(WTPart part , String releaseTime, String batchNumber, ObjectReference self, Object... params);
	
	/**
	 * 发布零部件物料信息
	 * 
	 * @param parts
	 */
	public  abstract WTKeyedHashMap releasePersistable( WTArrayList parts)throws Exception;
	
	
	/**
	 * 发布零部件物料信息
	 * 
	 * @param parts
	 * @param releaseTime
	 */
	public  WTKeyedHashMap releasePersistable( WTArrayList parts , String releaseTime , String partNumber)throws Exception;
	
	
	
	/**
	 * 发布零部件物料信息
	 * 
	 * @param parts
	 * @throws IntegrationException 
	 */
	public  WTKeyedHashMap release(List<WTPart> parts)throws Exception;
	
	/**
	 * 发布零部件物料信息
	 * 
	 * @param parts
	 * @param releaseTime
	 * @throws IntegrationException 
	 */
	public  WTKeyedHashMap release(List<WTPart> parts , String releaseTime, String batchNumber)throws Exception;
	

}
