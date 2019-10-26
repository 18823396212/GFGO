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
package ext.generic.integration.erp.common;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

import wt.fc.ObjectReference;
import wt.fc.collections.WTKeyedHashMap;
import wt.method.RemoteAccess;
import wt.part.WTPart;
import wt.part.WTPartUsageLink;
import wt.util.WTException;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;

@SuppressWarnings("serial")
public class IntegrationCommand implements RemoteAccess, Serializable{
	
	/**
	 * 获取ERP发布失败的对象列表
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Vector getFailedPrintObjects( WorkItem workItem ) throws WTException,Exception {
		WfAssignedActivity wfassignedactivity = (WfAssignedActivity) workItem.getSource().getObject();

		Vector vector = new Vector();
		
		//获取ERP发布失败的对象列表中的值
		WTKeyedHashMap a = ( WTKeyedHashMap ) ( wfassignedactivity.getContext().getValue("failedIntegrationObjects") );
		Iterator iter = a.keySet().iterator();
		
		while ( iter.hasNext() ) {
			Object key = iter.next();
			
			if( key != null ){
				
				if( key instanceof ObjectReference ){
					ObjectReference orf = ( ObjectReference )key;
					key = orf.getObject();
				}
				
				if( key instanceof WTPart ){
					WTPart part = ( WTPart )key;
					
					vector.add( part );
				}
				
				if( key instanceof WTPartUsageLink ){
					WTPartUsageLink usageLink = ( WTPartUsageLink )key;
					
					vector.add( usageLink );
				}
			}
		}
		return vector;
	}
}
