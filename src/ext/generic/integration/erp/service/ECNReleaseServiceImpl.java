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

public interface ECNReleaseServiceImpl {
	
	
	public abstract WTKeyedHashMap release(WTChangeOrder2 ecn , String releaseTime, String batchNumber);
	
	public abstract WTKeyedHashMap release(WTChangeOrder2 ecn , String releaseTime, String batchNumber,
			ObjectReference self, Object... params);
}
