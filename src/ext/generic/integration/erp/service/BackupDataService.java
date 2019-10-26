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

import java.io.IOException;

import org.apache.log4j.Logger;

import com.infoengine.SAK.Task;
import com.infoengine.object.factory.Group;
import com.infoengine.util.IEException;

import ext.generic.integration.erp.config.ERPPropertiesUtil;
import ext.generic.integration.erp.rule.BussinessRule;
import ext.generic.integration.erp.util.IntegrationConstant;
import ext.generic.integration.erp.util.PDMIntegrationLogUtil;
import wt.log4j.LogR;
/**
 * 中间表数据备份类
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-10-08
 */
public class BackupDataService {
	private static final String clazz = BackupDataService.class.getName() ;
	private static final Logger logger = LogR.getLogger(clazz);
	
	private static String partBackupXMLFile = "" ;
	
	private static String partDeleteWithStatusXMLFile = "" ;
	
	private static String bomStructureBackupXMLFile = "" ;
	
	private static String bomStructureDeleteWithStatusXMLFile = "" ;
	
	/**
	 * 初始化备份使用的Info*E Task文件
	 * 
	 */
	static{
		partBackupXMLFile = (String) ERPPropertiesUtil.getInstance().readProperties().get(IntegrationConstant.PART_BACKUP_IE_XML_FILE ); 
		
		partDeleteWithStatusXMLFile = (String) ERPPropertiesUtil.getInstance().readProperties().get( IntegrationConstant.PART_DELETE_WITH_STATUS_IE_XML_FILE ); 
		
		bomStructureBackupXMLFile = (String) ERPPropertiesUtil.getInstance().readProperties().get(IntegrationConstant.BOM_STRUCTURE_BACKUP_IE_XML_FILE ); 
		
		bomStructureDeleteWithStatusXMLFile = (String) ERPPropertiesUtil.getInstance().readProperties().get( IntegrationConstant.BOM_STRUCTURE_DELETE_WITH_STATUS_IE_XML_FILE ); 
		
		initialPartBackupCheck();
		initialBOMStructureBackupCheck() ;
	}
	
	protected static void initialPartBackupCheck() {
		if( partBackupXMLFile != null){
			partBackupXMLFile = partBackupXMLFile.trim() ;
			logger.debug("Backup Config File : " + partBackupXMLFile);
		}else{
			logger.error("Backup Config File : NULL" );
		}
		
		if( partDeleteWithStatusXMLFile != null){
			partDeleteWithStatusXMLFile = partDeleteWithStatusXMLFile.trim() ;
			logger.debug("Delete Config File : " + partDeleteWithStatusXMLFile);
		}else{
			logger.error("Delete Config File : NULL");
		}
	}
	
	protected static void initialBOMStructureBackupCheck() {
		if( bomStructureBackupXMLFile != null){
			bomStructureBackupXMLFile = bomStructureBackupXMLFile.trim() ;
			logger.debug("Backup Config File : " + bomStructureBackupXMLFile);
		}else{
			logger.error( "Backup Config File : NULL");
			PDMIntegrationLogUtil.printLog(clazz, "Backup Config File : NULL" , IntegrationConstant.LOG_LEVEL_ERROR ) ;
		}
		
		if( bomStructureDeleteWithStatusXMLFile != null){
			bomStructureDeleteWithStatusXMLFile = bomStructureDeleteWithStatusXMLFile.trim() ;
			logger.debug("Delete Config File : " + bomStructureDeleteWithStatusXMLFile);
		}else{
			logger.error("Delete Config File : NULL");
		}
	}
	
	/**
	 * 备份数据方法
	 * 
	 */
	public static void backupData(){
		boolean startBackup = BussinessRule.startBackupData() ;
		
		if( startBackup ){
			backupPartInfo() ;
			
			backupBOMRootInfo() ;
			
			backupBOMStructure() ;
			
			backupECNInfo() ;
		}
	}
	
	/**
	 * 备份零部件信息表
	 * 
	 */
	private  static  void backupPartInfo(){
		backupDataTask( partBackupXMLFile , partDeleteWithStatusXMLFile ) ;
	}
	
	/**
	 * 备份BOM结构中的节点信息表
	 * 
	 */
	private  static  void backupBOMRootInfo(){
		
	}
	
	/**
	 * 备份BOM结构关系表
	 * 
	 */
	private  static  void backupBOMStructure(){
		backupDataTask( bomStructureBackupXMLFile , bomStructureDeleteWithStatusXMLFile ) ;
	}
	
	/**
	 * 备份ECN信息表
	 * 
	 */
	private  static  void backupECNInfo(){
		
	}
	
	/**
	 * 备份的Info*E Task任务
	 * 
	 * @param backupXMLFile 将源表的数据插入到备份表所使用的Info*E Task XML文件
	 * @param deleteXMLFile 将备份过的数据从源表中删除所使用的Info*E Task XML文件
	 */
	private  static  void backupDataTask(String backupXMLFile , String deleteXMLFile ){
		boolean result = false ;
		
		if( backupXMLFile == null || backupXMLFile.equals("")){
			return ;
		}
		
		if( deleteXMLFile == null || deleteXMLFile.equals("")){
			return ;
		}

		try {
			runTask( backupXMLFile ) ;
			
			result = true ;
		} catch (IEException e) {
			result = false ;
			logger.error("Backup Data , Throws IEException , File : " + backupXMLFile );
			logger.error(e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			result = false ;
			logger.error("Backup Data , Throws IOException , File : " + backupXMLFile);
			logger.error(e.toString());
			e.printStackTrace();
		} finally{
			if( result ){
				try {
					runTask( deleteXMLFile ) ;
				} catch (IEException e) {
					logger.error("After Backup Data, When Delete Data , Throws IEException , File : " + deleteXMLFile);
					logger.error(e.toString());
					e.printStackTrace();
				} catch (IOException e) {
					logger.error("After Backup Data, When Delete Data , Throws IOException , File : " + deleteXMLFile);
					logger.error(e.toString());
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 执行Info*E 任务
	 * 
	 * @param xmlFile
	 * @throws IEException
	 * @throws IOException
	 */
	private static void runTask(String xmlFile) throws IEException , IOException{
		//创建任务
		Task task = new Task( xmlFile );
		
		task.invoke() ;
		
		//获取返回值
		Group group =  task.getGroup("DoSql");
		
		if(group == null){
			logger.error(">>>>> Do SQL : group == null !");
		}else{
			logger.error(">>>>> Do SQL : group size = " + group.getElementCount());
		}
	}
}
