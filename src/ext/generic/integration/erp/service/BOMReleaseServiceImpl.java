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

import wt.change2.WTChangeOrder2;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTKeyedHashMap;
import wt.part.WTPart;

public interface BOMReleaseServiceImpl {
	
	/**
	 * 发布PBO单层BOM结构，如果不是WTPart，则不处理
	 * 
	 * @param obj
	 */
	public abstract WTKeyedHashMap releaseSingleLevel( Object obj );
	
	/**
	 * 发布PBO单层BOM结构，如果不是WTPart，则不处理
	 * 如果不在ECN中，则WTChangeOrder2 ecn设置为null
	 * 
	 * @param obj
	 * @param releaseTime
	 * @param ecn
	 */
	public abstract WTKeyedHashMap releaseSingleLevel( Object obj , String releaseTime, String batchNumber, WTObject wtobj  );
	
	/**
	 * 发布WTPart单层BOM结构
	 * 
	 * @param part
	 */
	public abstract WTKeyedHashMap releaseSingleLevel( WTPart part );
	
	/**
	 * 发布WTPart单层BOM结构
	 * 如果不在ECN中，则WTChangeOrder2 ecn设置为null
	 * 
	 * @param part
	 * @param releaseTime
	 * @param ecn
	 */
	public abstract WTKeyedHashMap releaseSingleLevel( WTPart part , String releaseTime, String batchNumber,  WTObject wtobj  ) throws SQLException ;
	
	/**
	 * 发布WTArrayList列表中的所有零部件的单层BOM结构
	 * 
	 * @param parts
	 */
	public abstract WTKeyedHashMap releasePersistableSingleLevel( WTArrayList parts );
	
	/**
	 * 发布WTArrayList列表中的所有零部件的单层BOM结构
	 * 如果不在ECN中，则WTChangeOrder2 ecn设置为null
	 * 
	 * @param parts
	 * @param releaseTime
	 * @param ecn
	 */
	public abstract WTKeyedHashMap releasePersistableSingleLevel( WTArrayList parts , String releaseTime, String batchNumber, WTChangeOrder2 ecn );

	/**
	 * 发布PBO的所有BOM结构
	 * 
	 * @param obj
	 */
	public abstract WTKeyedHashMap releaseAllLevel( Object obj );
	
	/**
	 * 发布PBO的所有BOM结构
	 * 如果不在ECN中，则WTChangeOrder2 ecn设置为null
	 * 
	 * @param obj
	 * @param releaseTime
	 * @param ecn
	 */
	public abstract WTKeyedHashMap releaseAllLevel( Object obj , String releaseTime, String batchNumber, WTObject wtobj );
	
	/**
	 * 发布WTPart的所有BOM结构
	 * 
	 * @param part
	 */
	public abstract WTKeyedHashMap releaseAllLevel( WTPart part );
	
	/**
	 * 发布WTPart的所有BOM结构
	 * 如果不在ECN中，则WTChangeOrder2 ecn设置为null
	 * 
	 * @param part
	 * @param releaseTime
	 * @param wtobj
	 */
	public abstract WTKeyedHashMap releaseAllLevel( WTPart part , String releaseTime, String batchNumber, WTObject wtobj );
	
	/**
	 * 发布WTArrayList列表中WTPart的所有BOM结构
	 * 
	 * @param parts
	 */
	public abstract WTKeyedHashMap releasePersistableAllLevel( WTArrayList parts );
	
	/**
	 * 发布WTArrayList列表中WTPart的所有BOM结构
	 * 如果不在ECN中，则WTChangeOrder2 ecn设置为null
	 * 
	 * @param parts
	 * @param releaseTime
	 * @param ecn
	 */
	public abstract WTKeyedHashMap releasePersistableAllLevel( WTArrayList parts , String releaseTime, String batchNumber, WTObject wtobj );
	
	/**
	 * 发布单层BOM结构
	 * 
	 * @param part
	 * @param releaseTime
	 * @param batchNumber
	 * @param wtobj
	 * @param hashMap
	 * @return
	 */
	public abstract List<WTPart> releaseFirstLevel( WTPart part , String releaseTime , String batchNumber,  WTObject wtobj , WTKeyedHashMap hashMap );
}
