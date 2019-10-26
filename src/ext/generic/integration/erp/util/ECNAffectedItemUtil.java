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
package ext.generic.integration.erp.util;

import java.util.Enumeration;

import wt.change2.ChangeException2;
import wt.change2.ChangeHelper2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.QueryResult;
import wt.fc.collections.WTArrayList;
import wt.part.WTPart;
import wt.util.WTException;

public class ECNAffectedItemUtil {

	/**
	 * 收集ECN中，所有ECA产生的对象
	 * 
	 * @param pbo
	 * @return
	 */
	public static WTArrayList getAllECNAffectedItems(Object pbo) {
		WTArrayList affectedItemList = null;

		if ( pbo != null && pbo instanceof WTChangeOrder2 ) {
			WTChangeOrder2 ecn = (WTChangeOrder2) pbo ;

			affectedItemList = getAllECNAffectedItems(ecn) ;
		}

		if (affectedItemList == null) {
			affectedItemList = new WTArrayList() ;
		}

		return affectedItemList;
	}

	/**
	 * 收集ECN中，所有ECA产生的对象
	 * 
	 * @param ecn
	 * @return
	 */
	public static WTArrayList getAllECNAffectedItems(WTChangeOrder2 ecn) {
		WTArrayList affectedItemList = new WTArrayList() ;

		if (ecn == null) {
			return affectedItemList ;
		}

		try {
			Enumeration enumca = ChangeHelper2.service.getChangeActivities(ecn);

			while (enumca.hasMoreElements()) {
				Object obj = enumca.nextElement();

				if (obj != null && obj instanceof WTChangeActivity2) {
					WTChangeActivity2 ca = (WTChangeActivity2) obj ;

					// 查询每个CA改后数据
					QueryResult qr = ChangeHelper2.service.getChangeablesAfter(ca);
					while (qr.hasMoreElements()) {
						Object changeable = qr.nextElement();
						if (changeable != null && changeable instanceof WTPart) {
							WTPart part = (WTPart) changeable;
							if ( ! affectedItemList.contains(part) ) {
								affectedItemList.add( part );
							}
						}
					}
				}
			}
		} catch (ChangeException2 e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}

		return affectedItemList;
	}
}
