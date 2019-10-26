package ext.generic.mergeproperties.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;

import wt.configuration.TraceCode;
import wt.enterprise.RevisionControlled;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.folder.CabinetBased;
import wt.inf.container.WTContained;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.State;
import wt.method.RemoteAccess;
import wt.org.OrganizationOwned;
import wt.org.WTOrganization;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.LineNumber;
import wt.part.PartUsesOccurrence;
import wt.part.Quantity;
import wt.part.QuantityUnit;
import wt.part.Source;
import wt.part.SubstituteQuantity;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.type.ClientTypedUtility;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.Iterated;
import wt.vc.VersionControlHelper;
import wt.vc.Versioned;
import ext.com.core.CoreUtil;
import ext.customer.common.MBAUtil;

/**
 *  一般工具类
 * CommonPDMUtil
 *  @author Administrator
 *	2014-12-5
 *
 */

public class CommonPDMUtil implements RemoteAccess, Serializable{	
	private static final long serialVersionUID = 7823240175163911579L;
	
	private static final String NO_LINE_NUMBER = "NoLNum" ;
	
	/**
	 * 获取WTObject对象的软属性的内部值
	 * 
	 * @param obj 对象
	 * @return 内部值
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
	 * @throws WTException exp
	 */
	public static String getSoftType( WTObject obj) throws WTException {
		String softType = getSoftType( obj, SessionHelper.manager.getLocale() ) ;
		
		return softType ; 
	}
	
	/**
	 * 获得对象的软类型显示名称
	 * 
	 * @param obj 对象
	 * @param locale 本地
	 * @return 显示名称
	 * @throws WTException exp
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
	 * @param revisionControlled 对象
	 * @return  大版本
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
	 * @param revisionControlled 对象
	 * @return 小版本
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
	 * @param revisionControlled 对象
	 * @return 大小版本
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
	 * @param iterated 对象
	 * @return 创建者
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
	 * @param iterated 对象
	 * @return  创建者
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
	 * @param iterated 对象
	 * @return 创建者
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
	 * 获取对象的修改者
	 * 
	 * @param iterated 对象
	 * @return 修改者
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
	 * @param iterated 对象
	 * @return 修改者
	 */
	public static String getModifierName( Iterated iterated ){
		String modifierName = "" ;
		
		WTPrincipal modifier = getModifier( iterated ) ;
		
		if( modifier != null ){			
			modifierName = modifier.getName() ;
		}
		
		return modifierName ;
	}
	
	/**
	 * 获得用户替换名
	 * @param iterated 用户
	 * @return 替换名
	 */
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
	
	/**
	 * 获得用户替换名
	 * @param paramWTUser 用户
	 * @param paramString  替换名
	 * @return 替换名
	 * @throws WTException exp
	 */
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
	 * 获取对象所属的库名称，包括储存库，产品库，质量库等等
	 * 
	 * @param wtcontained 对象库
	 * @return 库名称
	 */
	public static String getContainerName(WTContained wtcontained){	
		String containerName = "" ;
		
		if( wtcontained != null ){
			containerName = wtcontained.getContainerName() ;
		}
		
		if( containerName == null ){
			containerName = "" ;
		}else{
			containerName.trim() ;
		}
		
		return containerName ;
	}
	
	/**
	 * 获取对象所在库的文件夹路径
	 * 
	 * @param cabinetBased 对象所在库
	 * @return 文件夹路径
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
	 * @param cabinetBased 对象所在库
	 * @return 路径
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
	 * @param organizationOwned 对象
	 * @return 所属的组织
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
	 * @param organizationOwned 对象
	 * @return 组织名
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
	 * @param  wtpart 部件
	 * @return 默认的源属性
	 * @throws WTException exp
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
	 * @param  wtpart 部件
	 * @return 默认的源属性
	 * @throws WTException exp
	 */
	public static String getSourceEN( WTPart wtpart ) throws WTException {
		String sourceValue = "" ;
		
		if( wtpart != null ){
			Source source = wtpart.getSource() ;
			
			if( source != null ){
				sourceValue = source.getDisplay(Locale.ENGLISH);
			}
		}

		return sourceValue ;
	}
	
	/**
	 * 获取默认的源属性
	 * 
	 * @param wtpart 部件
	 * @return 源属性
	 * @throws WTException exp
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
	 * @param lifeCycleManaged 对象的生命周期
	 * @return 状态值
	 * @throws WTException exp
	 */
	public static String getLifecycle( LifeCycleManaged lifeCycleManaged ) throws WTException {
		String lifecycle = "" ;
		
		if( lifeCycleManaged != null ){
			State state = lifeCycleManaged.getLifeCycleState() ;
			
			if( state != null ){
				System.out.println("state != null");
				//内部值
				lifecycle = state.toString();
				
			}else{
				System.out.println("state == null");
			}
		}
	
		return lifecycle;
	}
	
	/**
	 * 获取对象的生命周期状态值
	 * 
	 * @param lifeCycleManaged 对象的生命周期
	 * @return 状态值
	 * @throws WTException exp
	 */
	public static String getLifecycleCN( LifeCycleManaged lifeCycleManaged ) throws WTException {
		String lifecycle = "" ;
		
		if( lifeCycleManaged != null ){
			State state = lifeCycleManaged.getLifeCycleState() ;
			
			if( state != null ){
				System.out.println("state != null");
				//中文显示名称
				lifecycle = state.getDisplay(Locale.CHINA);	
			}else{
				System.out.println("state == null");
			}
		}
	
		return lifecycle;
	}

	/**
	 * 获取默认的单位的内部名称
	 * 
	 * @param wtpart 部件
	 * @return 默认的单位的内部名称
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
	 * @param quantityUnit 重量单位
	 * @return 默认的单位的内部名称
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
	 * @param wtpart 部件
	 * @return 默认的单位的中文名称
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
	 * @param quantityUnit 单位
	 * @return  默认的单位的中文名称
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
	 * @param wtpart 部件
	 * @return 默认的单位的英文名称
	 */
	public static String getUnitEN( WTPart wtpart ){
		String defaultUnit = "" ;
		
		if( wtpart != null ){
			QuantityUnit quantityUnit = wtpart.getDefaultUnit() ;
			
			defaultUnit = getUnitEN( quantityUnit ) ;
		}

		return defaultUnit;
	}
	
	/**
	 * 获取默认的单位的英文名称
	 * 
	 * @param quantityUnit 单位
	 * @return 默认的单位的英文名称
	 */
	public static String getUnitEN( QuantityUnit quantityUnit ){
		String defaultUnit = "" ;
		
		if( quantityUnit != null ){
			defaultUnit = quantityUnit.getLocalizedMessage(Locale.ENGLISH) ;
		}

		return defaultUnit;
	}
	
	/**
	 * 获取单位
	 * 
	 * @param usageLink link
	 * @return  获取单位
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
	 * 获取用量
	 * 
	 * @param usageLink link
	 * @return 用量
	 */
	public static String getQuantityValue(WTPartUsageLink usageLink ) {
		String value = "0" ;
		
		Quantity quantity = usageLink.getQuantity() ;
		
		if(quantity != null ){
			value = quantity.getAmount() + "" ;
		}
		
		return value ;
	}
	
	/**
	 * 获取用量
	 * 
	 * @param quantity 数量
	 * @return 用量
	 */
	public static String getQuantityValue( SubstituteQuantity quantity ){
		String value = "0" ;
		
		if( quantity != null ){
			value = quantity.getAmount() + "" ;
		}
		
		return value ;
	}
	
	/**
	 * 获取默认的追踪代码
	 * 
	 * @param wtpart WTPart
	 * @return  默认的追踪代码
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
	 * @param usageLink link
	 * @return  默认的追踪代码
	 * @throws WTException exp
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
	 * @param usageLink link
	 * @return 父件编号
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
	 * @param usageLink link
	 * @return 父件视图
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
	 * 获取 
	 * 
	 * @param usageLink link
	 * @return 父件版本
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
	 * @param usageLink link
	 * @return 父件版本
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
	 * @param usageLink link
	 * @return 子件编号
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
	 * @param usageLink link
	 * @return 行号
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

	/**
	 * 获取位号
	 * 
	 * @param usageLink link
	 * @return 位号
	 */
	public static String getOccurrence(WTPartUsageLink usageLink) {
		String positionNumber = "" ;
//		
//		try {
//			QueryResult usesoccurrences= wt.occurrence.OccurrenceHelper.service.getUsesOccurrences(usageLink);
//			
//			if(usesoccurrences != null){
//				while(usesoccurrences.hasMoreElements()){
//					Object obj = usesoccurrences.nextElement();
////							System.out.println("获取的位号对象类型为：" + obj.getClass().getName());
//					if(obj instanceof PartUsesOccurrence){
//						PartUsesOccurrence usesOccurrence = (PartUsesOccurrence) obj ;
//						String name = usesOccurrence.getName();					
//						positionNumber = positionNumber + name + "," ;
//					}
//				}
//			}
//		} catch (WTException e) {
//			
//			e.printStackTrace();
//		}
//		
//		System.out.println("Position Number:" + positionNumber);
//		
//		if(positionNumber.endsWith(",")){
//			positionNumber = positionNumber.substring(0, positionNumber.length()-1);
//		}
		
		positionNumber = (String) MBAUtil.getValue(usageLink, "referenceDesignatorRange");
		return positionNumber ;
	}
	
	/**
	 * 获取当前Session中的Principal对象
	 * @return Principal对象
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
	 * 设置对象的创建者
	 * @param newVer 对象
	 * @param principalRef  用户
	 * @return  对象 
	 * @throws WTException exp
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
	
	/**
	 * 设置新版对象的创建者和更新者
	 * @param newVer 对象
	 * @param principalRef  用户
	 * @return  对象 
	 * @throws WTException exp
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
	 * @param part 部件
	 * @return 基本信息
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
	
	/**
	 * 获取link的一些基本信息
	 * 
	 * @param usageLink link
	 * @return 基本信息
	 */
	public static String getWTPartUsageLinkInfo( WTPartUsageLink usageLink ){
		StringBuffer partInfo = new StringBuffer() ;
		
		partInfo.append("父件编号：") ;
		partInfo.append( CommonPDMUtil.getParentMajorVersion(usageLink)) ;
		partInfo.append("\n") ;
		partInfo.append("父件视图：") ;
		partInfo.append( CommonPDMUtil.getParentVersion(usageLink) ) ;
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
	 * @param wtList1  列表1
	 * @param wtList2 列表2
	 * @return 合并Persistable列表
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
	 * @param list 列表
	 * @param destination 对象
	 * @return  是否存在于WTArrayList中
	 * @throws WTException exp
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
	
	/**
	 * 获取codebase 路径
	 * @return 路径
	 */
	public static String getCodebasePath(){
		String path = "" ;
		
		path = CommonPDMUtil.class.getResource("/").getPath();
		
		path = path.replace("\\\\", "/" ) ;
		
		//BUG Fix， 在Linux环境下，代码获取的路径为/ptc/windchill/，而实际上，Linux路径区分大小写，需要转换为/ptc/Windchill/
		//注：AIX环境下也可能存在此问题
		path = path.replace("windchill", "Windchill") ;
		
		return path ;
	}
	
	
	/**
	 * Test Case
	 * 
	 * @param args 参数
	 */
	public static void main(String[] args) {
		if( args != null && args.length == 2 ){
			try {
				WTPart part = CoreUtil.getWTPartByNumberAndView( args[0] , args[1] ) ;
				
				if( part != null ){
					
					System.out.println("获取到零部件：" + part.getNumber() + part.getViewName());
					
					System.out.println( "Container Name:" + CommonPDMUtil.getContainerName(part) ) ;
					System.out.println( "Folder Path:" + CommonPDMUtil.getFolderPath(part) ) ;
					System.out.println( "Location:" + CommonPDMUtil.getLocation(part) ) ;
					
					System.out.println( "Creator Name:" + CommonPDMUtil.getCreatorName(part) ) ;
					
					
					System.out.println( "Lifecycle:" + CommonPDMUtil.getLifecycle(part) ) ;
					
					System.out.println( "Major Version:" + CommonPDMUtil.getMajorVersion(part) ) ;
					System.out.println( "Minor Version:" + CommonPDMUtil.getMinorVersion(part) ) ;
					System.out.println( "Version:" + CommonPDMUtil.getVersion(part) ) ;
					
					System.out.println( "Modifier Name:" + CommonPDMUtil.getAlternateName(part) ) ;
					
					System.out.println( "Organization Name:" + CommonPDMUtil.getOrganizationName(part) ) ;
					
					System.out.println( "Soft Type:" + CommonPDMUtil.getSoftType(part) ) ;
					System.out.println( "Soft Type Internal:" + CommonPDMUtil.getSoftTypeInternal(part) ) ;
					
					System.out.println( "Source:" + CommonPDMUtil.getSourceCN(part) ) ;
					
					System.out.println( "Codebase Path:" + CommonPDMUtil.getCodebasePath() ) ;
					
					
				}else{
					System.out.println( "Test Case: part is null " ) ;
				}
				
			} catch (WTException e) {
				e.printStackTrace();
			}
		}
	}
}
