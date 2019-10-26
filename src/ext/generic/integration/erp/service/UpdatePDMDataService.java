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

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import ext.com.iba.IBAUtil;
import ext.generic.integration.erp.bean.BOMRootInfo;
import ext.generic.integration.erp.bean.GenericIEBean;
import ext.generic.integration.erp.bean.MaterialInfo;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.generic.integration.erp.ibatis.BomInfoIbatis;
import ext.generic.integration.erp.ibatis.PartIbatis;
import ext.generic.integration.erp.util.BusinessRuleXMLConfigUtil;
import ext.generic.integration.erp.util.IntegrationConstant;
import wt.httpgw.GatewayAuthenticator;
import wt.iba.value.IBAHolder;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.util.WTException;
/**
 * 更新PDM发布状态
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-10-08
 */
public class UpdatePDMDataService implements RemoteAccess{
	private static final String CLASSNAME = UpdatePDMDataService.class.getName() ;
	private static final Logger logger = LogR.getLogger(CLASSNAME);
	
	//错误原因
	private static final String ERPERRORMSG = "erpErrorMsg";
	
	public static void updateERPAcceptStatus(){
		updateERPPartAcceptStatus( BusinessRuleXMLConfigUtil.getInstance().getPartReleaseStatus() ) ;
		
		updateERPBomAcceptStatus( BusinessRuleXMLConfigUtil.getInstance().getBomReleaseStatus() ) ;
	}
	
	public static void updateERPPartAcceptStatus( String ibaName ){
		List list = null;
		try {
			list = PartIbatis.selectERPReceivedMaterial();
			if(list != null){
				for(int i = 0 ; i < list.size()  ; i++ ){
					MaterialInfo materialInfo= (MaterialInfo)list.get(i);
					if(logger.isTraceEnabled()){
						logger.trace(materialInfo);
					}
					updateERPAccptStatus(ibaName, materialInfo.getNumber(), materialInfo.getView(), materialInfo.getVersion(), materialInfo.getFlag(), materialInfo.getErpErrorMsg());
					String flag =  materialInfo.getFlag();
					if( flag != null && flag.equals( IntegrationConstant.ERP_PROCESS_SUCCESS )){
						PartIbatis.updateMaterialWriteBackInfo(materialInfo);
					}
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	
	public static void updateERPBomAcceptStatus( String ibaName ){
		//List<GenericIEBean> list = QueryBomReleaseStatus.query() ;
		List list = null;
		try {
			list = BomInfoIbatis.selectERPReceivedBOMInfo();
			if(list != null){
				for(int i = 0 ; i < list.size()  ; i++ ){
					BOMRootInfo bomRootInfo= (BOMRootInfo)list.get(i);
					if(logger.isTraceEnabled()){
						logger.trace(bomRootInfo);
					}
					updateERPAccptStatus(ibaName, bomRootInfo.getNumber(), bomRootInfo.getView(), bomRootInfo.getVersion(), bomRootInfo.getFlag(), bomRootInfo.getErpErrorMsg());
					String flag = bomRootInfo.getFlag();
					if( flag != null && flag.equals( IntegrationConstant.ERP_PROCESS_SUCCESS )){
						BomInfoIbatis.updateMaterialWriteBackInfo(bomRootInfo);
					}
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//updateERPAcceptStatus(QueryBomReleaseStatus.getTableName(), ibaName, list);
	}
	
	/*
	 * 更新发布状态
	 * 
	 */
	private static void updateERPAccptStatus(String ibaName, String partNumber, String partView, String version, String flag, String erpErrorMsg){
		if( erpErrorMsg == null ){
			erpErrorMsg = "";
		}
		logger.debug("ibaName is :" + ibaName + "partNumber is :"+ partNumber + "partView " + partView +" version" + version + " flag " + flag + "erpErrorMsg " + erpErrorMsg);
		try {
			WTPart part = CommonPDMUtil.getWTPartByNumViewAndVersion( partNumber , partView , version);
			logger.debug("part="+part);
			if( part != null ){
				logger.debug("part.display="+part.getDisplayIdentifier());
				if( flag != null && flag.equals( IntegrationConstant.ERP_PROCESS_SUCCESS )){
					IBAUtil.forceSetIBAValue( part , ibaName , IntegrationConstant.ERP_PROCESS_SUCCESS_CN);
				}else if( flag != null && flag.equals( IntegrationConstant.ERP_PROCESS_FAILED ) ){
					logger.debug("flag is Error");
					IBAUtil.forceSetIBAValue( part , ibaName , IntegrationConstant.ERP_PROCESS_FAILED_CN);
					
					//错误原因更新
					IBAUtil.forceSetIBAValue( part , ERPERRORMSG , erpErrorMsg );
					logger.debug("设置结束");
				}				
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
	}
	
	@Deprecated
	private static void updateERPAcceptStatus(String table, String ibaName, List<GenericIEBean> list ) {
		int size = list.size() ;

		for(int i = 0 ; i < size  ; i++ ){
			GenericIEBean genericIEBean = list.get(i) ;
			
			if(logger.isDebugEnabled()){
				logger.debug("genericIEBean="+genericIEBean);
			}
			
			String partNumber = genericIEBean.getAttribute("item_id") ;
			String partView = genericIEBean.getAttribute("pview") ;
			String version = genericIEBean.getAttribute("version") ;
			String flag = genericIEBean.getAttribute("flag") ;
			//填加错误原因
			String erpErrorMsg = genericIEBean.getAttribute("ERP_ERROR_MSG") ;

			if( erpErrorMsg == null ){
				erpErrorMsg = "";
			}
			
			try {
				WTPart part = CommonPDMUtil.getWTPartByNumViewAndVersion( partNumber , partView , version);
				
				if( part != null ){
					if( flag != null && flag.equals( IntegrationConstant.ERP_PROCESS_SUCCESS )){
						IBAUtil.forceSetIBAValue( part , ibaName , IntegrationConstant.ERP_PROCESS_SUCCESS_CN);
					}else if( flag != null && flag.equals( IntegrationConstant.ERP_PROCESS_FAILED ) ){
						IBAUtil.forceSetIBAValue( part , ibaName , IntegrationConstant.ERP_PROCESS_FAILED_CN);
						
						//错误原因更新
						IBAUtil.forceSetIBAValue( part , ERPERRORMSG , erpErrorMsg );
					}
					
					String where = "item_id='" + partNumber + "' " ;
					where = where + " And " + "pview='" + partView + "' " ;
					where = where + " And " + "version='" + version + "' " ;
					
//					UpdateWriteBackStatusIETask.update( table , where ) ;					
				}
			} catch (WTException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void updatePDMSucessful(IBAHolder ibaHolder , String ibaName ){
		updatePDMData( ibaHolder , ibaName , IntegrationConstant.PDM_RELEASE_STATUS_CN  );
	}
	
	public static void updatePDMFailed(IBAHolder ibaHolder , String ibaName ){
		updatePDMData( ibaHolder , ibaName , ""  ) ;
	}
	
	public static void updateERPSucessful(IBAHolder ibaHolder , String ibaName ){		
		updatePDMData( ibaHolder , ibaName , IntegrationConstant.ERP_PROCESS_SUCCESS_CN  ) ;		
	}
	
	public static void updateERPFailed(IBAHolder ibaHolder , String ibaName ){
		updatePDMData( ibaHolder , ibaName , IntegrationConstant.ERP_PROCESS_FAILED_CN ) ;
	}
	
	protected static void updatePDMData(IBAHolder ibaHolder , String ibaName , String ibaValue ){
		if( ibaHolder != null ){
			try {
				Object obj = IBAUtil.getIBAValue(ibaHolder, ibaName);
				
				if( obj == null || ( (String)obj ).trim().isEmpty() || !( (String)obj ).trim().equals( ibaValue ) ){
					IBAUtil.forceSetIBAValue(ibaHolder, ibaName, ibaValue ) ;
				}
				
			} catch ( Exception e ) {				
				e.printStackTrace();
			} 
		}
	}
	//windchill ext.generic.integration.erp.service.UpdatePDMDataService
	public static void main(String[] args) {
		if(args.length>=0){
			Class[]  clsAry = {};
			Object[] objAry = {};
			String method = "updateERPAcceptStatus";
			try {
				if(!RemoteMethodServer.ServerFlag){
					GatewayAuthenticator auth = new GatewayAuthenticator();
				    auth.setRemoteUser("Administrator");
				    RemoteMethodServer.getDefault().setAuthenticator(auth);
					RemoteMethodServer.getDefault().invoke(method, CLASSNAME, null, clsAry, objAry);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
	
}
