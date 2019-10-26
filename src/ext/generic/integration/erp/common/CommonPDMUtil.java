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
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import ext.com.core.CoreUtil;
import ext.com.workflow.WorkflowUtil;
import ext.customer.common.MBAUtil;
import ext.generic.integration.erp.config.UnitMappingUtil;
import ext.generic.integration.erp.util.BOMStructureUtil;
import ext.generic.integration.erp.util.BusinessRuleXMLConfigUtil;
import wt.change2.ChangeException2;
import wt.change2.ChangeHelper2;
import wt.change2.Changeable2;
import wt.change2.VersionableChangeItem;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.configuration.TraceCode;
import wt.enterprise.RevisionControlled;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.folder.CabinetBased;
import wt.inf.container.WTContained;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.maturity.MaturityHelper;
import wt.maturity.PromotionNotice;
import wt.method.RemoteAccess;
import wt.org.OrganizationOwned;
import wt.org.WTOrganization;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.LineNumber;
import wt.part.Quantity;
import wt.part.QuantityUnit;
import wt.part.Source;
import wt.part.SubstituteQuantity;
import wt.part.WTPart;
import wt.part.WTPartConfigSpec;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartStandardConfigSpec;
import wt.part.WTPartUsageLink;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.type.ClientTypedUtility;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.Iterated;
import wt.vc.VersionControlHelper;
import wt.vc.Versioned;
import wt.vc.config.ConfigException;
import wt.vc.config.ConfigSpec;
import wt.vc.config.LatestConfigSpec;
import wt.vc.struct.StructHelper;
import wt.vc.views.View;
import wt.vc.views.ViewHelper;
import wt.vc.views.ViewReference;
import wt.workflow.engine.WfProcess;
/**
 * 一般工具类
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-07-09
 */ 
public class CommonPDMUtil implements RemoteAccess, Serializable{	
	private static final long serialVersionUID = 7823240175163911579L;
	
	private static final String NO_LINE_NUMBER = "" ;
	
	private static final String CLASSNAME = CommonPDMUtil.class.getName() ;

	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	
	/**
	 * 获取WTObject对象的软属性的内部值
	 * 
	 * @param obj
	 * @return
	 */
	public static String getSoftTypeInternal( WTObject obj) {
		String internalName = "";
		
		if(obj!=null) {
			try {
				//获取完整的内部值
				internalName=ClientTypedUtility.getTypeIdentifier(obj).getTypename() ;
				
				if( internalName != null ){
					int startIndex = internalName.lastIndexOf(".") + 1 ;
					
					//截取最后一个"."的后面部分
					internalName = internalName.substring( startIndex ) ;
				}
			} catch(WTException e) {
				e.printStackTrace() ;
			}
		}
		
		return internalName; 
	}
	
	
	/**
	 * 获得对象的软类型显示名称
	 * 
	 * @param     obj  WTObject to be validated
	 * @return    String 软类型名称
	 * @throws WTException
	 */
	public static String getSoftType( WTObject obj) throws WTException {
		//这种locale在手工发布和流程发布时，可能时区不一样，统一成CHINA
//		String softType = getSoftType( obj, SessionHelper.manager.getLocale() ) ;
		String softType = getSoftType( obj, Locale.CHINA ) ;
		return softType ; 
	}
	
	/**
	 * 获得对象的软类型显示名称
	 * 
	 * @param obj
	 * @param locale
	 * @return
	 * @throws WTException
	 */
	public static String getSoftType( WTObject obj,Locale locale) throws WTException { 
		String moduleName="";
		
		if(obj!=null) {
			try {
				//获取对象的软属性显示名
				moduleName=ClientTypedUtility.getLocalizedTypeName(obj,locale);

			} catch(Exception e) {
				throw new WTException(e);
			}
		}
		
		return moduleName; 
	}
	
	/**
	 * 获取对象的大版本
	 * 
	 * @param revisionControlled
	 * @return
	 */
	public static String getMajorVersion(RevisionControlled revisionControlled){
		String mainVersion = "" ;
		
		if(revisionControlled != null){
			mainVersion = revisionControlled.getVersionIdentifier().getValue();
		}
		
		return mainVersion ;
	}
	
	/**
	 * 获取对象的小版本
	 * 
	 * @param revisionControlled
	 * @return
	 */
	public static String getMinorVersion(RevisionControlled revisionControlled){
		String smallVersion = "" ;
		
		if(revisionControlled != null){
			smallVersion = revisionControlled.getIterationIdentifier().getValue();
		}
		
		return smallVersion ;
	}
	
	/**
	 * 获取对象的大小版本组成的版本序列值，形如： A.1
	 * 
	 * @param revisionControlled
	 * @return
	 */
	public static String getVersion(RevisionControlled revisionControlled){

		String version = "";
		
		if( revisionControlled != null ){
			//获取大版本
			String majorVersion = revisionControlled.getVersionIdentifier().getValue();
			
			
			// 获取小版本
			String minorVersion = revisionControlled.getIterationIdentifier().getValue();

			version = majorVersion + "." + minorVersion;
		}
		
		return version;
	}
	
	/**
	 * 获取对象的创建者
	 * 
	 * @param iterated
	 * @return
	 */
	public static WTPrincipal getCreator( Iterated iterated ){
		WTPrincipal principal = null ;
		
		if( iterated == null ){	
			return principal ;
		}else{
			//获取创建者
			WTPrincipalReference creator = iterated.getCreator() ;
			
			try {
				if( creator != null ){
					principal = creator.getPrincipal() ;
				}
				
			} catch (WTException e) {
				e.printStackTrace();
			}
		}
		
		return principal ;
	}
	
	/**
	 * 获取对象的创建者
	 * 
	 * @param iterated
	 * @return
	 */
	public static String getCreatorName( Iterated iterated ){
		String creatorName = "" ;
		
		WTPrincipal principal = getCreator( iterated ) ;
		
		if( principal != null ){
			creatorName = principal.getName() ;
		}
		
		return creatorName ;
	}
	
	/**
	 * 获取对象的创建者
	 * 
	 * @param iterated
	 * @return
	 */
	public static String getCreatorFullName( Iterated iterated ){
		String creatorName = "" ;
		
		WTPrincipal principal = getCreator( iterated ) ;
		
		if( principal != null ){
			if( principal instanceof WTUser ){
				WTUser wtuser = ( WTUser ) principal ;
				creatorName = wtuser.getFullName() ;
			}else {
				creatorName = principal.getName() ;
			}
		}
		
		return creatorName ;
	}
	
	/**
	 * 获取对象的创建时间
	 * 
	 * @param revisionControlled
	 * @return
	 */
	public static String getCreateTime( RevisionControlled revisionControlled ){
		String createTime = "" ;
		
		if( revisionControlled != null ){
			Timestamp createTimestamp = revisionControlled.getCreateTimestamp() ;
			
			if( createTimestamp != null ){
				createTime = CommonUtil.getDateValue( createTimestamp ) ;
			}
		}
		
		return createTime ;
	}
	
	/**
	 * 获取对象的创建时间
	 * 
	 * @param revisionControlled
	 * @return
	 */
	public static String getCreateTime( VersionableChangeItem versionableChangeItem ){
		String createTime = "" ;
		
		if( versionableChangeItem != null ){
			Timestamp createTimestamp = versionableChangeItem.getCreateTimestamp() ;
			
			if( createTimestamp != null ){
				createTime = CommonUtil.getDateValue( createTimestamp ) ;
			}
		}
		
		return createTime ;
	}
	
	/**
	 * 获取对象的修改者
	 * 
	 * @param iterated
	 * @return
	 */
	public static WTPrincipal getModifier( Iterated iterated ){
		WTPrincipal principal = null ;
		
		if( iterated == null ){			
			return principal ;
		}else{
			WTPrincipalReference modifier = iterated.getModifier() ;
			
			try {
				if( modifier != null ){
					principal = modifier.getPrincipal() ;
				}
			} catch (WTException e) {
				e.printStackTrace();
			}
		}
		
		return principal ;
	}
	
	/**
	 * 获取对象的修改者
	 * 
	 * @param iterated
	 * @return
	 */
	public static String getModifierName( Iterated iterated ){
		String modifierName = "" ;
		
		WTPrincipal modifier = getModifier( iterated ) ;
		
		if( modifier != null ){			
			modifierName = modifier.getName() ;
		}
		
		return modifierName ;
	}
	
	public static String getAlternateName( Iterated iterated ){
		String str = "";
		
		WTPrincipal modifier = getModifier( iterated ) ;
		
		if( modifier != null && modifier instanceof WTUser ){
			
			WTUser wtUser = ( WTUser ) modifier ;
			
			try {
				str = getAlternateName( wtUser , "alternateUserName1" );
			} catch (WTException e) {
				e.printStackTrace();
			}
		}
		
		if( str == null ){
			str = modifier.getName();
		}
		
		return str;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static String getAlternateName(WTUser paramWTUser, String paramString) throws WTException {
	    String str = null;
	    Enumeration localEnumeration = paramWTUser.getAttributes().getValues("uid");

	    ArrayList localArrayList = Collections.list(localEnumeration);
	    localArrayList.remove(paramWTUser.getName());
	    if ((paramString.equals("alternateUserName1")) && (localArrayList.size() > 0))
	      str = (String)localArrayList.get(0);
	    else if ((paramString.equals("alternateUserName2")) && (localArrayList.size() > 1))
	      str = (String)localArrayList.get(1);
	    else if ((paramString.equals("alternateUserName3")) && (localArrayList.size() > 2))
	      str = (String)localArrayList.get(2);
	    else if ((paramString.equals("alternateUserName4")) && (localArrayList.size() > 3)) {
	      str = (String)localArrayList.get(3);
	    }
	    
	    return str;
	  }
	
	/**
	 * 获取对象的修改时间
	 * 格式为：yyyy-MM-dd HH:mm:ss
	 * 
	 * @param revisionControlled
	 * @return
	 */
	public static String getModifyTime( RevisionControlled revisionControlled ){
		String modifyTime = "" ;
		
		if( revisionControlled != null ){
			Timestamp modifyTimestamp = revisionControlled.getModifyTimestamp() ;
			
			if(modifyTimestamp != null){
				modifyTime = CommonUtil.getDateValue(modifyTimestamp) ;
			}
		}
		
		return modifyTime ;
	}
	
	/**
	 * 获取对象的修改时间
	 * 格式为：yyyy-MM-dd HH:mm:ss
	 * 
	 * @param revisionControlled
	 * @return
	 */
	public static String getModifyTime( VersionableChangeItem versionableChangeItem ){
		String modifyTime = "" ;
		
		if( versionableChangeItem != null ){
			Timestamp modifyTimestamp = versionableChangeItem.getModifyTimestamp() ;
			
			if(modifyTimestamp != null){
				modifyTime = CommonUtil.getDateValue(modifyTimestamp) ;
			}
		}
		
		return modifyTime ;
	}
	
	/**
	 * 获取对象所属的库名称，包括储存库，产品库，质量库等等
	 * 
	 * @param wtcontained
	 * @return
	 */
	public static String getContainerName(WTContained wtcontained){	
		String containerName = "" ;
		
		if( wtcontained != null ){
			containerName = wtcontained.getContainerName() ;
		}
		
		if( containerName == null ){
			containerName = "" ;
		}else{
			containerName = containerName.trim() ;
		}
		
		return containerName ;
	}
	
	/**
	 * 获取对象所在库的文件夹路径
	 * 
	 * @param cabinetBased
	 * @return
	 */
	public static String getLocation(CabinetBased cabinetBased){
		String location = "" ;
		
		if(cabinetBased != null){
			location = cabinetBased.getLocation() ;
		}
		
		if( location == null){
			location = "" ;
		}else{
			location = location.trim() ;
		}
		
		return location ;
	}
	
	/**
	 * 获取对象所在库的对象路径，包含文件夹路径，最后以对象结尾
	 * 
	 * @param cabinetBased
	 * @return
	 */
	public static String getFolderPath(CabinetBased cabinetBased){
		String folderPath = "" ;
		
		if(cabinetBased != null){
			folderPath = cabinetBased.getFolderPath() ;
		}
		
		if(folderPath == null){
			folderPath = "" ;
		}else{
			folderPath = folderPath.trim() ;
		}
		
		return folderPath ;
	}
	
	/**
	 * 获取对象所属的组织
	 * 
	 * @param organizationOwned
	 * @return
	 */
	public static WTOrganization getOrganization (OrganizationOwned organizationOwned){		
		WTOrganization organization = null ;
		
		if( organizationOwned != null ){
			organization = organizationOwned.getOrganization() ;
		}
		
		return organization ;
	}
	
	/**
	 * 获取对象所属的组织的组织名
	 * 
	 * @param organizationOwned
	 * @return
	 */
	public static String getOrganizationName (OrganizationOwned organizationOwned){		
		String orgName = "" ;
		
		WTOrganization organization = getOrganization ( organizationOwned ) ;
		
		if( organization != null ){
			orgName = organization.getName() ;
		}
		
		return orgName ;
	}
	
	/**
	 * 获取默认的源属性
	 * 
	 * @param source
	 * @return
	 * @throws WTException
	 */
	public static String getSourceInternalValue( WTPart wtpart ) throws WTException {
		String sourceValue = "" ;
		
		if( wtpart != null ){
			Source source = wtpart.getSource() ;
			
			if( source != null ){
				sourceValue = source.getStringValue();
			}
		}

		return sourceValue ;
	}
	
	/**
	 * 获取默认的源属性
	 * 
	 * @param source
	 * @return
	 * @throws WTException
	 */
	public static String getSourceEN( WTPart wtpart ) throws WTException {
		String sourceValue = "" ;
		
		if( wtpart != null ){
			Source source = wtpart.getSource() ;
			sourceValue = source.toString();
			
//			if( source != null ){
//				sourceValue = source.getDisplay(Locale.ENGLISH);   //Modify By ZhouLihua 20160822
//			}
		}

		return sourceValue ;
	}
	
	/**
	 * 获取默认的源属性
	 * 
	 * @param source
	 * @return
	 * @throws WTException
	 */
	public static String getSourceCN( WTPart wtpart ) throws WTException {
		String sourceValue = "" ;
		
		if( wtpart != null ){
			Source source = wtpart.getSource() ;
			
			if( source != null ){
				sourceValue = source.getDisplay(Locale.CHINA);
			}
		}

		return sourceValue ;
	}

	/**
	 * 获取对象的生命周期状态值
	 * 
	 * @param lifeCycleManaged
	 * @return
	 * @throws WTException
	 */
	public static String getLifecycle( LifeCycleManaged lifeCycleManaged ) throws WTException {
		String lifecycle = "" ;
		
		if( lifeCycleManaged != null ){
			State state = lifeCycleManaged.getLifeCycleState() ;
			
			if( state != null ){
				logger.debug("state != null");
				//内部值
				lifecycle = state.toString();
				
			}else{
				logger.debug("state == null");
			}
		}
	
		return lifecycle;
	}
	
	/**
	 * 获取对象的生命周期状态值
	 * 
	 * @param lifeCycleManaged
	 * @return
	 * @throws WTException
	 */
	public static String getLifecycleCN( LifeCycleManaged lifeCycleManaged ) throws WTException {
		String lifecycle = "" ;
		
		if( lifeCycleManaged != null ){
			State state = lifeCycleManaged.getLifeCycleState() ;
			
			if( state != null ){
				logger.debug("state != null");
				//中文显示名称
				lifecycle = state.getDisplay(Locale.CHINA);	
			}else{
				logger.debug("state == null");
			}
		}
	
		return lifecycle;
	}

	/**
	 * 获取默认的单位的内部名称
	 * 
	 * @param unitAssignable
	 * @return
	 */
	public static String getUnitInternalValue( WTPart wtpart ){
		String defaultUnit = "" ;
		
		if( wtpart != null ){
			QuantityUnit quantityUnit = wtpart.getDefaultUnit() ;
			
			defaultUnit = getUnitInternalValue( quantityUnit ) ;
		}

		return defaultUnit;
	}
	
	/**
	 * 获取默认的单位的内部名称
	 * 
	 * @param unitAssignable
	 * @return
	 */
	public static String getUnitInternalValue( QuantityUnit quantityUnit ){
		String defaultUnit = "" ;
		
		if( quantityUnit != null ){
			defaultUnit = quantityUnit.getStringValue() ;
		}

		return defaultUnit;
	}
	
	/**
	 * 获取默认的单位的中文名称
	 * 
	 * @param unitAssignable
	 * @return
	 */
	public static String getUnitCN( WTPart wtpart ){
		String defaultUnit = "" ;
		
		if( wtpart != null ){
			QuantityUnit quantityUnit = wtpart.getDefaultUnit() ;
			
			defaultUnit = getUnitCN( quantityUnit );
		}

		return defaultUnit;
	}
	
	/**
	 * 获取默认的单位的中文名称
	 * 
	 * @param unitAssignable
	 * @return
	 */
	public static String getUnitCN( QuantityUnit quantityUnit ){
		String defaultUnit = "" ;
		
		if( quantityUnit != null ){
			defaultUnit = quantityUnit.getLocalizedMessage(Locale.CHINA) ;
		}

		return defaultUnit;
	}
	
	/**
	 * 获取默认的单位的英文名称
	 * 
	 * @param unitAssignable
	 * @return
	 */
	public static String getUnitEN( WTPart wtpart ){
		String defaultUnit = "" ;
		
		if( wtpart != null ){
			QuantityUnit quantityUnit = wtpart.getDefaultUnit() ;
			defaultUnit = quantityUnit.toString();
		  //defaultUnit = getUnitEN( quantityUnit ); // TODO Modify by ZhouLihua 20160822 (使用rbInfo文件中key做映射)
		}

		return defaultUnit;
	}
	
	/**
	 * 获取默认的单位的英文名称
	 * 
	 * @param unitAssignable
	 * @return
	 */
	public static String getUnitEN( QuantityUnit quantityUnit ){
		String defaultUnit = "" ;
		
		if( quantityUnit != null ){
			// defaultUnit = quantityUnit.getLocalizedMessage(Locale.ENGLISH) ; Modify By ZhouLihua  20140824
			defaultUnit = quantityUnit.toString();
		}

		return defaultUnit;
	}
	
	/**
	 * 获取单位
	 * 
	 * @param usageLink
	 * @return
	 */
	public static String getQuantityUnit(WTPartUsageLink usageLink ) {
		String unit = "" ;
		
		//获取用量
		Quantity quantity = usageLink.getQuantity() ;
		
		if( quantity != null ){
			//获取用量单位
			QuantityUnit quantityUnit = quantity.getUnit() ;
			
			unit = getUnitEN( quantityUnit ) ;
		}
		
		return unit ;
	}
	
	/**
	 * 获取PDM单位对应的ERP单位
	 * 
	 * @param unitAssignable
	 * @return
	 */
	public static String getERPMappingUnit( WTPart wtpart ){
		String unit = getUnitEN(wtpart) ;
		
		logger.debug("getERPMappingUnit1 unit is:" + unit);
		if( unit != null ){
		//	unit = unit.toLowerCase();  //Modify By ZhouLihua 20160822
			
			logger.debug("getERPMappingUnit2 unit is:" + unit);
			unit = UnitMappingUtil.getERPUnit( unit ) ;
			logger.debug("getERPMappingUnit3 unit is:" + unit);
		}
		
		return unit;
	}

	/**
	 * 获取PDM单位对应的ERP单位
	 * 
	 * @param unitAssignable
	 * @return
	 */
	public static String getERPMappingUnit( WTPartUsageLink usageLink ){
		String unit = "" ;
		//获取用量
		Quantity quantity = usageLink.getQuantity() ;
		
		if( quantity != null ){
			//获取用量单位
			QuantityUnit quantityUnit = quantity.getUnit() ;
			
			unit = getUnitEN( quantityUnit ) ;
			logger.debug("getERPMappingUnit1 unit is:" + unit);
			if( unit != null ){
				//unit = unit.toUpperCase(); //Modify By ZhouLihua  20160824
				
				unit = UnitMappingUtil.getERPUnit( unit ) ;
				
				logger.debug("getERPMappingUnit2 unit is:" + unit);
			}
		}

		return unit;
	}
	
	/**
	 * 获取PDM单位对应的ERP单位
	 * 
	 * @param quantity
	 * @return
	 */
	public static String getERPMappingUnit( SubstituteQuantity quantity ){
		String unit = "" ;
		
		if( quantity != null ){
			QuantityUnit quantityUnit = quantity.getUnit() ;
			
			unit = getUnitEN( quantityUnit ) ;
			logger.debug("getERPMappingUnit1 unit is:" + unit);
			if( unit != null ){
				unit = unit.toUpperCase();
				
				unit = UnitMappingUtil.getERPUnit( unit ) ;
				logger.debug("getERPMappingUnit2 unit is:" + unit);
			}
		}
		
		return unit ;
	}
	
	/**
	 * 获取用量
	 * 
	 * @param usageLink
	 * @return
	 */
	public static String getQuantityValue(WTPartUsageLink usageLink ) {
		String value = "0" ;
		
		Quantity quantity = usageLink.getQuantity() ;
		
//		if(quantity != null ){
//			value = quantity.getAmount() + "" ;
//		}
		// Modify by ZhouLihua 20170308
		if (quantity != null) {

			DecimalFormat decimalFormat = new DecimalFormat();
			decimalFormat.setMinimumFractionDigits(0);
			decimalFormat.setMaximumFractionDigits(20);
			value = decimalFormat.format(quantity.getAmount());
			value = value.replaceAll(",", "");
		}
		
		return value ;
	}
	
	public static String getQuantityValue( SubstituteQuantity quantity ){
		String value = "0" ;
		
//		if( quantity != null ){
//			value = quantity.getAmount() + "" ;
//		}
		// Modify by ZhouLihua 20170308
		if (quantity != null) {

			DecimalFormat decimalFormat = new DecimalFormat();
			decimalFormat.setMinimumFractionDigits(0);
			decimalFormat.setMaximumFractionDigits(20);
			value = decimalFormat.format(quantity.getAmount());
			value = value.replaceAll(",", "");
		}
		
		return value ;
	}
	
	/**
	 * 获取默认的追踪代码
	 * 
	 * @param wtpart WTPart
	 * @return
	 */
	public static String getTraceCode( WTPart wtpart ){
		String traceCodeValue = "" ;

		if( wtpart != null ){
			TraceCode traceCode = wtpart.getDefaultTraceCode() ;
			
			if( traceCode != null ){
				traceCodeValue = traceCode.getDisplay( Locale.CHINA ) ;
			}
		}

		return traceCodeValue ;
	}
	
	/**
	 * 获取默认的追踪代码
	 * 
	 * @param usageLink
	 * @return
	 */
	public static String getTraceCode(WTPartUsageLink usageLink) throws WTException {
		String traceCodeValue = "" ;
		
		if( usageLink != null ){
			TraceCode traceCode = usageLink.getTraceCode();
			
			if( traceCode != null ){
				traceCodeValue = traceCode.getDisplay(SessionHelper.getLocale());
			}
		}

		return traceCodeValue ;
	}
	
	/**
	 * 获取父件编号
	 * 
	 * @param usageLink
	 * @return
	 */
	public static String getParentNumber(WTPartUsageLink usageLink){
		String parentNumber = "" ;
		
		if( usageLink != null ){
			WTPart parent = usageLink.getUsedBy() ;
			
			if( parent != null ){
				parentNumber = parent.getNumber() ;
			}
		}
		
		return parentNumber ;
	}
	
	/**
	 * 获取父件视图
	 * 
	 * @param usageLink
	 * @return
	 */
	public static String getParentView( WTPartUsageLink usageLink ){
		String parentView = "" ;
		
		if( usageLink != null ){
			WTPart parent = usageLink.getUsedBy() ;
			
			if( parent != null ){
				parentView = parent.getViewName() ;
			}
		}
		
		return parentView ;
	}
	
	/**
	 * 获取父件版本
	 * 
	 * @param usageLink
	 * @return
	 */
	public static String getParentVersion( WTPartUsageLink usageLink ){
		String parentVersion = "" ;
		
		if( usageLink != null ){
			WTPart parent = usageLink.getUsedBy() ;
			
			if( parent != null ){
				parentVersion = CommonPDMUtil.getVersion( parent ) ;
			}
		}
		
		return parentVersion ;
	}
	
	/**
	 * 获取父件版本
	 * 
	 * @param usageLink
	 * @return
	 */
	public static String getParentMajorVersion( WTPartUsageLink usageLink ){
		String parentMajorVersion = "" ;
		
		if( usageLink != null ){
			WTPart parent = usageLink.getUsedBy() ;
			
			if( parent != null ){
				parentMajorVersion = CommonPDMUtil.getMajorVersion( parent ) ;
			}
		}
		
		return parentMajorVersion ;
	}
	
	/**
	 * 获取子件编号
	 * 
	 * @param usageLink
	 * @return
	 */
	public static String getChildNumber( WTPartUsageLink usageLink ){
		String childNumber = "" ;
		
		if( usageLink != null ){
			WTPartMaster childMaster = usageLink.getUses();
			
			if( childMaster != null ){
				childNumber = childMaster.getNumber() ;
			}
		}
		
		return childNumber ;
	}

	/**
	 * 获取行号
	 * 
	 * @param usageLink
	 * @return
	 */
	public static String getLineNumber(WTPartUsageLink usageLink) {
		String lineNumberValue = "" ;
		
		if( usageLink != null ){
			LineNumber lineNumber = usageLink.getLineNumber() ;
			
			if( lineNumber != null ){
				lineNumberValue = lineNumber.getValue() + "" ;
			}
		}
		
		if( lineNumberValue.trim().equals("") ){
			lineNumberValue = NO_LINE_NUMBER ;
		}
		
		return lineNumberValue;
	}
	
	/***
	 * 检查BOM位号是否存在异常
	 * @param wtObj
	 * @throws WTException
	 * @throws RemoteException 
	 */
	@SuppressWarnings("deprecation")
	public static void checkUsageLinkOccurrence( WTObject wtObj ) throws WTException, RemoteException{
		if( wtObj == null ){
			return;
		}
		//检查数据
		WTArrayList partList = new WTArrayList();
		
		if( wtObj instanceof PromotionNotice ){
			
			//升级请求
			PromotionNotice pn = ( PromotionNotice )wtObj;
			
			//获取升级请求中所有的升级对象
			QueryResult qr = MaturityHelper.getService().getPromotionTargets(pn);
			
			while( qr.hasMoreElements() ){
				Object obj = qr.nextElement();
				
				if( obj != null ){
					
					if( obj instanceof ObjectReference ){
						ObjectReference orf = ( ObjectReference )obj;
						obj = orf.getObject();
					}
					
					if( obj instanceof WTPart ){
						//获取升级对象
						WTPart part = ( WTPart )obj;
						
						//获取Part下所有有结构的物料主数据
						WTArrayList parts = getAllSubPart( part );
						
						//检查行号
						checkUsageLinkOccurrence( parts );
					}
				}
			}		
		}else if( wtObj instanceof WTChangeOrder2 ){
			WTChangeOrder2 ecn = ( WTChangeOrder2 )wtObj;
			
			try {
				//根据变更通告获取变更任务
				QueryResult qr = ChangeHelper2.service.getChangeActivities( ecn );
				
				while( qr.hasMoreElements() ){
					Object obj = qr.nextElement();
					
					if( obj instanceof ObjectReference ){
						ObjectReference orf = ( ObjectReference )obj;
						obj = orf.getObject();
					}
					
					if( obj instanceof WTChangeActivity2 ){
						WTChangeActivity2 eca = ( WTChangeActivity2 )obj;
						
						//获取ECA中所有的产生的对象
						QueryResult afQr = ChangeHelper2.service.getChangeablesAfter(eca);

						while( afQr.hasMoreElements() ){
							Object afOb = afQr.nextElement();
							
							if( afOb instanceof ObjectReference ){
								ObjectReference orf = ( ObjectReference )afOb;
								afOb = orf.getObject();
							}
							
							if( afOb instanceof WTPart ){								
								WTPart part = (WTPart)afOb;
								partList.add(part);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
//		logger.debug( "检查的BOM : " + partList );  
		
		try {
			checkUsageLinkOccurrence( partList );
		} catch (WTException e) {
			throw new WTException(e);
		}
	}
	
	/***
	 * 批量检查BOM上的位号是否存在异常
	 * @param partList
	 * @throws WTException 
	 */
	public static void checkUsageLinkOccurrence( WTArrayList partList ) throws WTException{
		if( partList == null ){
			logger.debug( "checkUsageLinkOccurrence ... 参数为空" );
			return;
		}
		
		logger.debug( "partList : " + partList );
		
		StringBuffer sb = new StringBuffer();
		
		for (int i = 0; i < partList.size(); i++) {
			Object obj = partList.get(i);
			
			if( obj != null ){
				
				if( obj instanceof ObjectReference ){
					ObjectReference orf = ( ObjectReference )obj;
					obj = orf.getObject();
				}
				
				if( obj instanceof WTPart ){
					WTPart part = ( WTPart )obj;
					
//					logger.debug( "checkUsageLinkOccurrence ... part : " + part.getDisplayIdentity() );
					
					//获取零部件下第一层结构
					List<WTPartUsageLink> usageLinkList = null;
					try {
						usageLinkList = BOMStructureUtil.getFirstLevelUsageLink(part);
					} catch (ConfigException e1) {
						e1.printStackTrace();
					} catch (WTException e1) {
						e1.printStackTrace();
					}
					
					if(usageLinkList != null){
						for( int j = 0; j < usageLinkList.size(); j++ ) {
							WTPartUsageLink usageLink = usageLinkList.get(j);
								
							try {
								//获取位号，判断是否有异常，存在异常则记录
								logger.debug(  usageLink.getDisplayIdentity() + ">位号 ：" +  getOccurrence( usageLink ) );
							} catch (WTException e) {
	//							logger.debug( usageLink.getDisplayIdentity() + " 位号获取异常" );
								sb.append( usageLink.getDisplayIdentity() );
								sb.append("\n");
							}
						}
					}
				}
			}
		}
		
		if( sb != null && sb.length() > 0 ){
			sb.append("位号存在异常，请检查后在提交");
			
			throw new WTException( sb.toString() );
		}
	}

	/**
	 * 获取位号
	 * 
	 * @param usageLink
	 * @return
	 * @throws WTException 
	 */
	public static String getOccurrence(WTPartUsageLink usageLink) throws WTException {
		String positionNumber = "" ;
		
//		try {
////			QueryResult usesoccurrences= wt.occurrence.OccurrenceHelper.service.getUsesOccurrences(usageLink);
////			
////			if(usesoccurrences != null){
////				while(usesoccurrences.hasMoreElements()){
////					Object obj = usesoccurrences.nextElement();
//////							logger.debug("获取的位号对象类型为：" + obj.getClass().getName());
////					if(obj instanceof PartUsesOccurrence){
////						PartUsesOccurrence usesOccurrence = (PartUsesOccurrence) obj ;
////						String name = usesOccurrence.getName();	
////						//判断名称是否为空
////						if(name != null && !name.trim().isEmpty()){
////							positionNumber = positionNumber + name + "," ;
////						}
////					}
////				}
////			}
//			PersistableAdapter obj = new PersistableAdapter(usageLink, null, null, null);
//	    	 obj.load("referenceDesignatorRange");
//	    	 Object object = obj.get("referenceDesignatorRange");
//	    	 if(object != null){
//	    		 positionNumber = object.toString();
//	    	 }
//		} catch (WTException e) {
//			
//			e.printStackTrace();
//		}
//
//		if(positionNumber.endsWith(",")){
//			positionNumber = positionNumber.substring(0, positionNumber.length()-1);
//		}

		//remove by zhouhaiwei on 20160927, 位号不进行合并运算
//		try{
//			
//			logger.debug("Long Position Number:" + positionNumber);
//			
//			PartOccurrence partOccurrence = new PartOccurrence( positionNumber ) ;
//			partOccurrence.generate() ;
//			positionNumber = partOccurrence.getShortOccurrence() ;
//			
//			logger.debug("Short Position Number:" + positionNumber);
//			
//		}catch( Exception e ){
//			throw new WTException( " 位号处理异常：" + e );
//		}

		positionNumber = (String) MBAUtil.getValue(usageLink, "referenceDesignatorRange");
		return positionNumber ;
	}
	
	/**
	 * 获取零部件的所有子件	 
	 * 
	 * 符合收集条件的物料：存在BOM结构 
	 *  @param part
	 * @return
	 * @throws ConfigException
	 * @throws WTException
	 * @throws RemoteException
	 */
	public static WTArrayList getAllSubPart( WTPart part ) throws ConfigException, WTException, RemoteException{
		WTArrayList list = new WTArrayList();
		
		if( part == null ){
			return list;
		}
		
		//如果列表中不包含此零部件，则加入列表，并进行递归
		WTArrayList usageList = getFirstLevelUsageLink( part );
		
		if( usageList != null && usageList.size() > 0 ){

			if( !list.contains( part )){
				list.add( part ) ;
			}
				
			getAllSubPart( part , list );
		}				
		return list;
	}
	
	/**
	 * 获取零部件的所有子件
	 * 
	 * 符合收集条件的物料：存在BOM结构 
	 * @param part
	 * @param allSubPartList
	 * @return
	 * @throws WTException
	 * @throws RemoteException 
	 */
	private static WTArrayList getAllSubPart( WTPart part , WTArrayList allSubPartList ) throws WTException, RemoteException{
		if( allSubPartList ==null ){
			allSubPartList = new WTArrayList() ;
		}
		
		ConfigSpec latestconfigspec = getConfigSpec(part);
		
		QueryResult qr= StructHelper.service.navigateUsesToIteration(part,WTPartUsageLink.class,false,latestconfigspec);
		
		while(qr != null && qr.hasMoreElements()){
			//每一个element实际是一个persistable数组
			Persistable apersistable[] = ( Persistable[] ) qr.nextElement();  
			
			Object uses= apersistable[1];   
			
			WTPart subPart=null; 
			if( uses instanceof WTPart ){
				subPart=(WTPart)uses; 
			} else if( uses instanceof WTPartMaster ){
				WTPartMaster master = (WTPartMaster) uses ;
				
				//获取最新版本
				subPart = CoreUtil.getWTPartByMasterAndView(master, part.getViewName()) ;
			}
			
			if( subPart != null ){
				//如果列表中不包含此零部件，则加入列表，并进行递归
				WTArrayList usageList = getFirstLevelUsageLink( subPart );
				
				if( usageList != null && usageList.size() > 0 ){
					
					if( !allSubPartList.contains( subPart )){
						allSubPartList.add( subPart ) ;
					}
					getAllSubPart( subPart , allSubPartList ) ;
				}
			}
		}
		return allSubPartList ;
	}
	
	/**
	 * 获取零部件第一层Usagelink关系
	 * @param part
	 * @return
	 * @throws ConfigException
	 * @throws WTException
	 */
	public static WTArrayList getFirstLevelUsageLink(WTPart part) throws ConfigException, WTException {
		WTArrayList subPartUsageLinkList = new WTArrayList() ;
		
		ConfigSpec latestconfigspec = getConfigSpec(part);
		
		QueryResult qr= StructHelper.service.navigateUsesToIteration(part,WTPartUsageLink.class,false,latestconfigspec);
		
		while(qr != null && qr.hasMoreElements()){
			//每一个element实际是一个persistable数组
			Persistable apersistable[] = ( Persistable[] ) qr.nextElement();  
			
			//数组中第一个对象是usagelink
			WTPartUsageLink partUsageLink= (WTPartUsageLink)apersistable[0];   
			
			if(partUsageLink != null){
				subPartUsageLinkList.add(partUsageLink) ;
			}
		}
		
		return subPartUsageLinkList;
	}

	/**
	 * 
	 * @param part
	 * @return
	 */
	private static ConfigSpec getConfigSpec(WTPart part) {
		ConfigSpec latestconfigspec = new LatestConfigSpec();
		
		String view = "";
		ViewReference viewRef =part.getView();
		if(viewRef != null){
			view = viewRef.getName();
		}
		try{
			if(view != null && view.length()>0){
				View viewObj= ViewHelper.service.getView(view);
				WTPartConfigSpec config = WTPartHelper.service.findWTPartConfigSpec();
				WTPartStandardConfigSpec standardConfig= config.getStandard();
				
				standardConfig.setView(viewObj);		
				latestconfigspec = standardConfig;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return latestconfigspec ;
	}
	
	/**
	 * 获取当前Session中的Principal对象
	 * 
	 */
	public static String getCurrentPrincipalName(){			
		String userLoginId = "" ;
		
		try{
			WTPrincipal principal = SessionHelper.getPrincipal();
			
			if( principal != null ){
				userLoginId = principal.getName() ;
			}
			
		} catch (WTException e) {
			e.printStackTrace();
		} finally{
			
		}

		return userLoginId ;
	}
	
	/**
	 * 根据零部件编号，视图和版本获取零部件对象
	 * 
	 * @param number
	 * @param view
	 * @param version
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings("deprecation")
	public static WTPart getWTPartByNumViewAndVersion(String number , String view , String version) throws WTException {
		WTPart part = null ;

		QuerySpec qs = new QuerySpec(WTPartMaster.class);
		
		//定义搜索条件，以零部件编号方式在master中搜索
		SearchCondition sc = new SearchCondition(WTPartMaster.class, WTPartMaster.NUMBER, SearchCondition.EQUAL, number );
		qs.appendSearchCondition(sc);
		
		QueryResult qr = PersistenceHelper.manager.find(qs);
		
		while (qr.hasMoreElements()) {
			WTPartMaster master = (WTPartMaster) qr.nextElement();
			
			if( master != null ){
				part = getWTPartByNumViewAndVersion( master , view , version) ;
			}
		}

		return part ;
	}
	
	/**
	 * 根据零部件主数据(master)和视图获取对应最新版本零部件
	 * @author Harry Cao
	 * @param master
	 * @param view
	 * @return
	 * @throws WTException
	 */
	public static WTPart getWTPartByNumViewAndVersion(WTPartMaster master , String view , String version) throws WTException {
		WTPart part = null;
		//根据视图名称获取视图对象
		View viewObj = ViewHelper.service.getView(view);

		//如果视图参数有误，则使用默认的设计视图，此代码可能会与实际业务相违背。
		if( viewObj == null ){
			logger.debug(">>>>>> 无效的视图名称：　" + view + ", 使用默认设计视图：" + CommonConstant.D_VIEW );
			
			viewObj = ViewHelper.service.getView( CommonConstant.D_VIEW );
		}
		
//		//根据视图构造产品结构配置规范
//		WTPartStandardConfigSpec standardConfig = WTPartStandardConfigSpec.newWTPartStandardConfigSpec(viewObj, null);
//		try {
//			standardConfig.setView(viewObj);
//		} catch (WTPropertyVetoException wpve) {
//			throw new WTException(wpve);
//		}

		//根据master和视图获取对应最新的视图版本零部件
		//QueryResult qr = ConfigHelper.service.filteredIterationsOf(master, standardConfig);
		
		//获取所有版本的零部件
		QueryResult qr=VersionControlHelper.service.allIterationsOf(master);
		
		//logger.debug(">>>>> 根据编号和视图获取到的零部件对象数量为：" + qr.size()) ;
		
		while ( qr.hasMoreElements()) {
			part = (WTPart) qr.nextElement();
			
			String partVersion = getVersion( part ) ;
			
			//视图相同，并且版本相同
			if( viewObj.getName().equals(part.getViewName()) && partVersion.equals( version )){
				return part ;
			}
		}
		
		return null ;
	}
	
	/*
	 * 设置对象的创建者
	 */
	public static Versioned setCreator(Versioned newVer, WTPrincipalReference principalRef) throws WTException {
		if( principalRef == null ){
			return newVer ;
		}else{
			boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
			
			try {
				newVer = (Versioned)VersionControlHelper.assignIterationCreator(newVer, principalRef);
				return newVer;
			} catch(WTPropertyVetoException wpve) {
				throw new WTException(wpve);
			}finally{
				SessionServerHelper.manager.setAccessEnforced(flag);
			}
		}
	}
	
	/*
	 * 设置新版对象的创建者和更新者
	 */
	public static Versioned setCreatorAndModifier(Versioned newVer, WTPrincipalReference principalRef) throws WTException {        
		if( principalRef == null ){
			return newVer ;
		}else{
			boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
			
			try {
				newVer = (Versioned)VersionControlHelper.assignIterationCreator(newVer, principalRef);
				VersionControlHelper.setIterationModifier(newVer, principalRef);
				return newVer;
			} catch(WTPropertyVetoException wpve) {
				throw new WTException(wpve);
			} finally{
				SessionServerHelper.manager.setAccessEnforced(flag);
			}
		}
		
	}
	
	/**
	 * 获取零部件的一些基本信息
	 * 
	 * @param part
	 * @return
	 */
	public static String getPartInfo( WTPart part ){
		StringBuffer partInfo = new StringBuffer() ;
		
		try{
			partInfo.append("零部件类型：") ;
			partInfo.append( CommonPDMUtil.getSoftType( part )) ;
			partInfo.append("\n") ;
			partInfo.append("零部件编号：") ;
			partInfo.append( part.getNumber() ) ;
			partInfo.append("\n") ;
			partInfo.append("零部件视图：") ;
			partInfo.append( part.getViewName() ) ;
			partInfo.append("\n") ;
			partInfo.append("零部件版本：") ;
			partInfo.append( CommonPDMUtil.getVersion( part )) ;
			partInfo.append("\n") ;
		} catch (WTException e) {
			e.printStackTrace();
		}

		return partInfo.toString() ;
	}
	
	public static String getWTPartUsageLinkInfo( WTPartUsageLink usageLink ){
		StringBuffer partInfo = new StringBuffer() ;
		
		partInfo.append("父件编号：") ;
		partInfo.append( CommonPDMUtil.getParentNumber(usageLink)) ;
		partInfo.append("\n") ;
		partInfo.append("父件视图：") ;
		partInfo.append( CommonPDMUtil.getParentView(usageLink) ) ;
		partInfo.append("\n") ;
		partInfo.append("父件版本：") ;
		partInfo.append( CommonPDMUtil.getParentVersion(usageLink) ) ;
		partInfo.append("\n") ;
		partInfo.append("子件编号：") ;
		partInfo.append( CommonPDMUtil.getChildNumber(usageLink) ) ;
		partInfo.append("\n") ;

		return partInfo.toString() ;
	}
	
	/**
	 * 合并Persistable列表
	 * 
	 * @param wtList1
	 * @param wtList2
	 * @return
	 */
	public static WTArrayList mergeList(WTArrayList wtList1 , WTArrayList wtList2){
		if(wtList1 == null && wtList2 == null){
			return new WTArrayList() ;
		}else if( wtList1 != null && wtList2 == null ){
			return wtList1 ;
		}else if( wtList1 == null && wtList2 != null ){
			return wtList2 ;
		}else{
			int size = wtList2.size() ;
			
			for( int i = 0 ; i < size ; i++ ){
				try {		
					Persistable persistable =  wtList2.getPersistable(i) ;
					
					boolean flag = containsPersistable( wtList1 , persistable) ;
					
					//如果persistable不在wtList1中，则将persistable加入wtList1
					if( ! flag ){
						wtList1.add( persistable ) ;
					}
					
				} catch (WTException e) {
					e.printStackTrace();
				}
			}
		}
		
		return wtList1 ;
	}
	
	/**
	 * 判断对象是否存在于WTArrayList中
	 * 
	 * @param list
	 * @param destination
	 * @return
	 * @throws WTException
	 */
	public static boolean containsPersistable( WTArrayList list, Persistable destination) throws WTException {
		boolean flag = false ;
		if(( list == null) || ( list.size()<=0)){
			flag = false ;
		}else if( list.contains(destination)){	
			flag = true ;	
		}else{		
			int size = list.size() ;	
			for(int i = 0; i < size ; i++) {
				//判断两个Persistable对象是否相同
				flag= PersistenceHelper.isEquivalent( list.getPersistable(i) , destination);
			}
		}
		
		return flag;
	}
	
	public static String getCodebasePath(){
		String path = "" ;
		
		path = CommonPDMUtil.class.getResource("/").getPath();
		
		path = path.replace("\\\\", "/" ) ;
		
		//BUG Fix， 在Linux环境下，代码获取的路径为/ptc/windchill/，而实际上，Linux路径区分大小写，需要转换为/ptc/Windchill/
		//注：AIX环境下也可能存在此问题
		path = path.replace("windchill", "Windchill") ;
		
		return path ;
	}
	
	public static String getWindchillPath(){
		String path = getCodebasePath() ;
		
		path = path.substring(0, path.lastIndexOf( CommonConstant.CODEBASE )) ;

		return path ;
	}
	
	/**
	 * 获取ECN中ECA的所有产生的对象
	 * @param ecn
	 * @return
	 */
	public static WTArrayList addReviewObjs( WTChangeOrder2 ecn ){
		WTArrayList wlist = new WTArrayList();
		List<WTChangeActivity2> listeca = addActivitieArraylist( ecn );
		if( ( listeca != null ) && ( listeca.size()>0 ) ){
			
			for (int i = 0; i < listeca.size(); i++) {
				
				WTChangeActivity2 eca = listeca.get(i);
				if( eca != null ){
					
					try {
						//获取ECA中所有的产生的对象
						QueryResult qr = ChangeHelper2.service.getChangeablesAfter(eca);
						while ( (qr != null) && qr.hasMoreElements()) {
							Changeable2 changeable =  ( Changeable2 )qr.nextElement();
							
							if( ( wlist != null ) && (!wlist.contains( changeable ) ) ){
								wlist.add( changeable );
							}							
						}						
					} catch (ChangeException2 e) {
						e.printStackTrace();
					} catch (WTException e) {
						e.printStackTrace();
					}					
				}				
			}		
		}		
		return wlist;		
	}
	
	/**
	 * 获取ECN中的ECA
	 * @param ecn
	 * @return
	 */
	public static List<WTChangeActivity2> addActivitieArraylist( WTChangeOrder2 ecn ){
		
		List<WTChangeActivity2> list = new ArrayList<WTChangeActivity2>();
		
		if( ecn != null ){
			try {
				//获取ECA
				QueryResult qr = ChangeHelper2.service.getChangeActivities(ecn);
				
				if( qr != null ){
					while( qr.hasMoreElements() ){
						Object object = qr.nextElement();
						
						if( object != null ){
							if( object instanceof WTChangeActivity2 ){
								WTChangeActivity2 eca = ( WTChangeActivity2 )object;
								list.add(eca);
							}
						}						
					}
				}
			} catch (ChangeException2 e) {
				e.printStackTrace();
			} catch (WTException e) {
				e.printStackTrace();
			}			
		}		
		return list;
	}
	
	
	/*
	 * 获取Windchill中可持续对象的ID
	 * @param persistable 需要获取的对象的OID
	 * @return objId 返回对象的ID
	 */
	public static long getObjectID(Persistable  persistable){
		long objId = -1;
		if(persistable != null){
			objId = PersistenceHelper.getObjectIdentifier(persistable).getId();
		}
		
		return objId;
	}
	
	/*
	 * 获取批次号
	 * @param ObjectReference self
	 */
	public static String getBatchNumber(ObjectReference self){
		String batchNumber = "";
		//如果工作流不为空，批次号直接为工作流的ID
		if(self != null){
			WfProcess wfprocess = WorkflowUtil.getProcess(self);
			batchNumber = CommonPDMUtil.getObjectID(wfprocess) + "";
		}
		
		//如果工作流为空，则获取当前用户ID + 当前时间
		if("".equals(batchNumber)){
//			WTPrincipal pricipal = null;
//			try {
//				pricipal = SessionHelper.manager.getPrincipal();
//			} catch (WTException e) {
//				e.printStackTrace();
//			}
//
//			WTUser curUser = (WTUser) pricipal;
//			String userID = curUser.getIdentity();
//			
//			Date now = new Date();
//			long time = now.getTime();
//			
//			batchNumber = userID + "_" +  time;
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			batchNumber = sdf.format(date);
			
		}
		logger.debug("batchNumber is:"+batchNumber);
		return batchNumber ;
	}
	
	/**
	 * Test Case
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if( args != null && args.length == 2 ){
			try {
				WTPart part = CoreUtil.getWTPartByNumberAndView( args[0] , args[1] ) ;
				
				if( part != null ){
					
					logger.debug("获取到零部件：" + part.getNumber() + part.getViewName());
					
					logger.debug( "Container Name:" + CommonPDMUtil.getContainerName(part) ) ;
					logger.debug( "Folder Path:" + CommonPDMUtil.getFolderPath(part) ) ;
					logger.debug( "Location:" + CommonPDMUtil.getLocation(part) ) ;
					
					logger.debug( "Create Time:" + CommonPDMUtil.getCreateTime(part) ) ;
					logger.debug( "Creator Name:" + CommonPDMUtil.getCreatorName(part) ) ;
					
					
					logger.debug( "Lifecycle:" + CommonPDMUtil.getLifecycle(part) ) ;
					
					logger.debug( "Major Version:" + CommonPDMUtil.getMajorVersion(part) ) ;
					logger.debug( "Minor Version:" + CommonPDMUtil.getMinorVersion(part) ) ;
					logger.debug( "Version:" + CommonPDMUtil.getVersion(part) ) ;
					
					logger.debug( "Modifier Name:" + CommonPDMUtil.getAlternateName(part) ) ;
					logger.debug( "Modify Time:" + CommonPDMUtil.getModifyTime(part) ) ;
					
					logger.debug( "Organization Name:" + CommonPDMUtil.getOrganizationName(part) ) ;
					
					logger.debug( "Soft Type:" + CommonPDMUtil.getSoftType(part) ) ;
					logger.debug( "Soft Type Internal:" + CommonPDMUtil.getSoftTypeInternal(part) ) ;
					
					logger.debug( "Source:" + CommonPDMUtil.getSourceCN(part) ) ;
					
					logger.debug( "Codebase Path:" + CommonPDMUtil.getCodebasePath() ) ;
					
					logger.debug( "Windchill Path:" + CommonPDMUtil.getWindchillPath() ) ;
					
				}else{
					logger.debug( "Test Case: " + part == null ) ;
				}
				
			} catch (WTException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 转换科学计数法
	 * @param quantity
	 * @return
	 */
	public static String switchQuantity( String quantity ){
		
		if( quantity == null || quantity.trim().isEmpty() ){
			return "";
		}
			
		if ( quantity.contains( "E" )) {
			BigDecimal bd = new BigDecimal( quantity );
			quantity = bd.toPlainString();
		}
				
		return quantity;
	}
	
	/**
	 * 两数字相乘
	 * @param price
	 * @param priceT
	 * @return
	 * @throws WTException
	 */
	public static String calculatePrice( String price , String priceT ) throws WTException{
		String count = "";
		
		if( price == null || priceT == null ){
			return count;
		}
		
		if(  price.trim().isEmpty() &&  priceT.trim().isEmpty() ){
			return "";
		}
	
		
		//判断报价是否为数字
		if( !checkDigit( price ) ){
			return count;
		}
		
		if( !checkDigit( priceT ) ){
			return count;
		}
		
		double pr = Double.parseDouble( price );
		
		double prT = Double.parseDouble( priceT );
		
//		BigDecimal pr = new BigDecimal( price );
//		
//        BigDecimal prT = new BigDecimal( priceT );
//		
//		count = pr.multiply( prT ) + "" ;
		
		count = Double.toString( pr * prT );
		
		count = switchQuantity( count );
		
		//保留两位小数
		//count = String.format( "%.2f" , Double.parseDouble( count ) );
		
		return count ;
	}
	
	/**
	 * 检查字符串是否可以转换成数字类型
	 * @param digit
	 * @return
	 */
	private static boolean checkDigit( String digit ){
		boolean checkDigit = false;
		
		if( digit == null || digit.trim().isEmpty() ){
			return checkDigit;
		}
		
		checkDigit = digit.matches( "^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$" );
		
		return checkDigit;
	}
	
	/**
	 * 检查零部件是否为特殊部件（虚拟件或收集件），并判断用户需要发布特殊
	 * @param part
	 * @return
	 */
	public static boolean checkPartSpecialHandling( WTPart part ){
		boolean checkPart = false;
		
		if( part == null ){
			return checkPart;
		}
		
		if( part.isPhantom() && BusinessRuleXMLConfigUtil.getInstance().getIsReleasePhantom().equals("false") ){
			checkPart = true;
		}
		
		if( part.getHidePartInStructure() && BusinessRuleXMLConfigUtil.getInstance().getIsReleaseHidePartInStructure().equals("false") ){
			checkPart = true;
		}
		
		return checkPart;
	}
	
	/***
	 * 发布BOM时特殊件处理方法（特殊件：虚拟件，收集件）
	 * @param usageLink
	 * @return
	 */
	public static Hashtable< WTPartUsageLink , String > getPartSpecialHandlingData( WTPartUsageLink usageLink ){
//		PDMIntegrationLogUtil.printLog(clazz, "getPartSpecialHandlingData(WTPartUsageLink usageLink) Starting ......") ;
		
		Hashtable< WTPartUsageLink , String > hashtable = new Hashtable< WTPartUsageLink , String >();
		
		if( usageLink != null ){
			try {
				String query = CommonPDMUtil.getQuantityValue( usageLink );
				
				WTPart sonPart = CoreUtil.getWTPartByMasterAndView( usageLink.getUses() , usageLink.getUsedBy().getViewName() );
				
				if( sonPart != null ){
					getPartSpecialHandlingData( sonPart , hashtable , query );
				}
			} catch (WTException e) {
				e.printStackTrace();
			}
		}
		
		return hashtable;
	}
	
	/***
	 * 发布BOM时特殊件处理方法（特殊件：虚拟件，收集件）
	 * @param part
	 * @param hashtable
	 * @param query
	 * @throws ConfigException
	 * @throws WTException
	 */
	private static void getPartSpecialHandlingData( WTPart part , Hashtable< WTPartUsageLink , String > hashtable , String query ) throws ConfigException, WTException {
//		PDMIntegrationLogUtil.printLog(clazz, "getPartSpecialHandlingData(WTPart part , Hashtable< WTPartUsageLink , String > hashtable , String query) Starting ......") ;
		
		//获取物料下的第一 成UsageLink
		List< WTPartUsageLink > usageLinkList = BOMStructureUtil.getFirstLevelUsageLink( part ) ;
		
		if( usageLinkList != null && usageLinkList.size() > 0 ){
			Iterator< WTPartUsageLink > iterator = usageLinkList.iterator();
			
			while( iterator.hasNext() ){
				WTPartUsageLink usageLink = iterator.next();
				
				String strQuery = query;
				
				if( usageLink != null ){
					String usageQuery = CommonPDMUtil.getQuantityValue( usageLink );
					
					WTPart sonPart = CoreUtil.getWTPartByMasterAndView( usageLink.getUses() , usageLink.getUsedBy().getViewName() );
					
					if( sonPart != null ){
						//数量累成
						strQuery = CommonPDMUtil.calculatePrice( strQuery , usageQuery );
						
						if( checkPartSpecialHandling( sonPart ) ){
							getPartSpecialHandlingData( sonPart , hashtable , strQuery );
						}else{
//							PDMIntegrationLogUtil.printLog(clazz, "累成等到的数量，strQuery: " + strQuery, IntegrationConstant.LOG_LEVEL_NORMAL ) ;
							hashtable.put( usageLink , strQuery ); 
						}
					}					
				}
			}
		}
		
//		PDMIntegrationLogUtil.printLog(clazz, "getPartSpecialHandlingData(WTPart part , Hashtable< WTPartUsageLink , String > hashtable , String query) Finished ......") ;
	}
	
}
