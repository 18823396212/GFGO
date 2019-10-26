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

import wt.change2.WTChangeOrder2;
import wt.fc.ObjectReference;
import wt.fc.collections.WTKeyedHashMap;
import ext.generic.integration.erp.ibatis.ECNIbatis;

/**
 * ECN信息发布类
 * 
 */
public class IbatisECNReleaseService implements ECNReleaseServiceImpl{
	/**
	 * 发布ECN信息
	 */
	public  WTKeyedHashMap release(WTChangeOrder2 ecn , String releaseTime, String batchNumber){
		return release(ecn, releaseTime, batchNumber, null);
	}
	/**
	 * 发布ECN信息
	 * @param ecn
	 * @param releaseTime
	 * @param batchNumber
	 * @param self
	 * @param params
	 * @return
	 */
	public  WTKeyedHashMap release(WTChangeOrder2 ecn , String releaseTime, String batchNumber, ObjectReference self, Object... params){
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		if( ecn != null ){
			hashMap.putAll( ECNIbatis.add( ecn , releaseTime,  batchNumber) );
		}
		
		return hashMap;
	}
}
