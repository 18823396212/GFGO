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
package ext.generic.integration.erp.datautilities;

import wt.fc.ObjectReference;
import wt.fc.WTObject;
import wt.fc.collections.WTKeyedHashMap;
import wt.log4j.LogR;
import wt.part.WTPartUsageLink;
import wt.util.WTException;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;

import org.apache.log4j.Logger;

import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.AbstractDataUtility;
import com.ptc.core.components.rendering.AbstractGuiComponent;
import com.ptc.core.components.rendering.guicomponents.Label;
import com.ptc.netmarkets.model.NmOid;

import ext.generic.integration.erp.common.CommonPDMUtil;
/**
 * ERP集成失败datautility
 * @author Administrator
 *
 */
public class IntegrationFailedInfoDataUtility extends AbstractDataUtility {
	private static final String CLASSNAME = IntegrationFailedInfoDataUtility.class.getName();
	private static final Logger logger = LogR.getLogger(CLASSNAME);

	@Override
	public Object getDataValue( String str , Object object , ModelContext modelContext )throws WTException {
		AbstractGuiComponent obj = null;
		
		NmOid oid = modelContext.getNmCommandBean().getActionOid();
		WorkItem workitem = (WorkItem) oid.getRef();
		WfAssignedActivity wfassignedactivity = (WfAssignedActivity) workitem.getSource().getObject();
		
		WTKeyedHashMap failedPrintObjectsInfo = ( WTKeyedHashMap )(wfassignedactivity.getContext().getValue("failedIntegrationObjects"));
		
		Object info = "";
		
		if( failedPrintObjectsInfo != null ){	
			
			if( object instanceof ObjectReference ){
				ObjectReference orf = ( ObjectReference )object;
				object = orf.getObject();
			}
			
			if( object instanceof WTPartUsageLink ){
				WTPartUsageLink usageLink = (WTPartUsageLink) object;
				
				if( str.equals("parentnumber") ){
					info = usageLink.getUsedBy().getNumber();
				}else if( str.equals("parentview") ){
					info = usageLink.getUsedBy().getViewName();
				}else if( str.equals("parentversion") ){
					info = CommonPDMUtil.getVersion( usageLink.getUsedBy() );
				}else if( str.equals("sonnumber") ){
					info = usageLink.getUses().getNumber();
				}
			}
			
			WTObject wtObj = ( WTObject )object; 
			if( str.equals("integrationErrInfo") ){
				info = failedPrintObjectsInfo.get( wtObj );
			}
		}
		
		if(info == null)
			info = "";
			
		logger.debug( "str="+str+",,,value=" + info );
		obj = new Label(info.toString());

		return obj;
	}
}
