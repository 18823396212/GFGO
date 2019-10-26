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

/**
 * ECN信息发布类
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-10-08
 */
public class ECNReleaseService {
	public static WTKeyedHashMap release(WTChangeOrder2 ecn , String releaseTime, String batchNumber){
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		if( ecn != null ){
//			hashMap.putAll( ECNIbatis.add( ecn , releaseTime , batchNumber) );
			IbatisECNReleaseService ecnService = new IbatisECNReleaseService();
			return ecnService.release(ecn, releaseTime, batchNumber);
		}
		
		return hashMap;
	}
	
	public static WTKeyedHashMap release(WTChangeOrder2 ecn , String releaseTime, String batchNumber, ObjectReference self){
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		
		if( ecn != null ){
//			hashMap.putAll( ECNIbatis.add( ecn , releaseTime , batchNumber) );
			IbatisECNReleaseService ecnService = new IbatisECNReleaseService();
			return ecnService.release(ecn, releaseTime, batchNumber, self);
		}
		
		return hashMap;
	}
}
