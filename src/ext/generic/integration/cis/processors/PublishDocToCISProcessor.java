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
package ext.generic.integration.cis.processors;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import wt.fc.collections.WTArrayList;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.util.WTException;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.forms.FormResultAction;
import com.ptc.core.components.util.FeedbackMessage;
import com.ptc.core.ui.resources.FeedbackType;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.com.iba.IBAUtil;
import ext.generic.integration.cis.constant.CISConstant;
import ext.generic.integration.cis.rule.CISBusinessRuleXML;
import ext.generic.integration.cis.util.OracleUtil;
import ext.generic.integration.cis.util.SQLServerUtil;
import ext.generic.integration.cis.workflow.WorkflowUtil;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIClassificationHelper;
/**
 * 手工发布cis
 * @author Administrator
 *
 */
public class PublishDocToCISProcessor extends DefaultObjectFormProcessor {
	private static final String CLASSNAME = PublishDocToCISProcessor.class.getName();
	private static final Logger logger = LogR.getLogger(CLASSNAME);
	
	@Override
	public FormResult doOperation(NmCommandBean commandBean, List<ObjectBean> beans)
			throws WTException {
		FormResult formresult = new FormResult();
		formresult.setStatus(FormProcessingStatus.SUCCESS);
		Object actionObj = commandBean.getActionOid().getRefObject();
		logger.debug("actionObj="+actionObj.getClass());
		
		HashMap text = commandBean.getText();
		
		String libraryref = text.get("libraryref").toString();
		String footprintref = text.get("footprintref").toString();
		String footprintref2 = text.get("footprintref2").toString();
		String datasheet = text.get("datasheet").toString();
		
		Connection connection = null;
		if(actionObj instanceof WTPart){
			WTPart part = (WTPart) actionObj;
			
			PIAttributeHelper.service.forceUpdateSoftAttribute(part, "libraryref", libraryref);
			PIAttributeHelper.service.forceUpdateSoftAttribute(part, "footprintref", footprintref);
			PIAttributeHelper.service.forceUpdateSoftAttribute(part, "footprintref2", footprintref2);
			PIAttributeHelper.service.forceUpdateSoftAttribute(part, "datasheet", datasheet);
			
			logger.debug(" >>>>>>>>>>.. part:"+part.getDisplayIdentifier());
			String name = CISBusinessRuleXML.getInstance().getDataBaseName();

			String error = null;
			try {
				if (name.equals(CISConstant.ORACLE))
					connection = OracleUtil.getConnection();
				else if (name.equals(CISConstant.SQLSERVER))
					connection = SQLServerUtil.getConnection();
				error = WorkflowUtil.publishData(part,connection);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally{
				if(connection!=null){
					try {
						connection.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					connection=null;
				}
			}
			
			if( error != null && error.length() > 0 ){
				formresult.addFeedbackMessage( new  FeedbackMessage(
						FeedbackType.FAILURE, null, "发布CIS异常", null, new String[] {error} ) );
			}else{
				formresult.addFeedbackMessage( new  FeedbackMessage(
						FeedbackType.SUCCESS, null, "CIS发布成功", null, new String[] {} ) );
			}
		}
		//刷新页面
//		formresult.setNextAction(FormResultAction.REFRESH_CURRENT_PAGE);
		return formresult;
	}
	
}
