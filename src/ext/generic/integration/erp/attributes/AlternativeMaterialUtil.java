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
package ext.generic.integration.erp.attributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import ext.generic.integration.erp.bean.AlternativeMaterial;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.generic.integration.erp.common.CommonUtil;
import ext.generic.integration.erp.util.GenerateBeanInfo;
import ext.generic.integration.erp.util.IntegrationConstant;
import wt.fc.ObjectReference;
import wt.fc.collections.WTCollection;
import wt.log4j.LogR;
import wt.part.SubstituteQuantity;
import wt.part.WTPart;
import wt.part.WTPartAlternateLink;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartSubstituteLink;
import wt.part.WTPartUsageLink;
import wt.util.WTException;
/**
 * 
 * 本类作用：获取替代关系的各个属性值
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-10-08
 */
public class AlternativeMaterialUtil {
	
	private static final String CLASSNAME = AlternativeMaterialUtil.class.getName() ;

	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	//父件编号
	private static final String PARENT_NUMBER =  "parentNumber" ;
	
	//父件视图
	private static final String PARENT_VIEW =  "parentView" ;
	
	//父件大版本
	private static final String MAJOR_VERSION = "majorVersion";
	
	//子件编号
	private static final String CHILD_NUMBER = "childNumber" ;
	
	//替代件编号
	private static final String REPLACE_PART_NUMBER = "replacePartNumber" ;
	
	/**
	 * 替代数量
	 * 
	 * 在10.1 M040版本的OOTB系统中，特定替代可以维护替代数量，全局替代的替代数量默认为1
	 */
	private static final String QUANTIRY = "quantity" ;
	
	/**
	 * 替代单位
	 * 
	 * 在10.1 M040版本的OOTB系统中，特定替代可以维护替代单位，全局替代不能维护替代单位
	 */
	private static final String UNIT = "unit" ;
	
	/**
	 * 替代关系类型
	 * 
	 * 默认情况下，代码将全局替代设值为A，特定替代设值为S
	 */
	private static final String RELACE_RELATIONSHIP = "replaceRelationship" ;
	
	/**
	 * 替代方向
	 * 
	 * 在10.1 M040版本的OOTB系统中，全局替代可以维护替代关系的方向：单向或者双向
	 * 
	 * 特定替代不能维护方向
	 */
	private static final String REPLACE_DIRECTION = "replaceDirection" ;
	
	/**
	 * 获取属性值
	 * 
	 * @param replaceMaterial	AlternativeMaterial 自定义Java Bean类型，统一维护特定替代和全局替代的属性
	 * @param attributeName
	 * @return String
	 */
	protected static String getAttribute( AlternativeMaterial replaceMaterial , String attributeName ) {
		String attributeValue = "" ;

		try{
			if( attributeName.equals( PARENT_NUMBER ) ){	
				
				attributeValue = replaceMaterial.getParentNumber() ;
				
			}else if( attributeName.equals( PARENT_VIEW ) ){
				
				attributeValue = replaceMaterial.getParentView() ;
				
			}else if( attributeName.equals( MAJOR_VERSION ) ){
				
				//attributeValue = replaceMaterial.g();
				
			}else if( attributeName.equals( CHILD_NUMBER ) ){
				
				attributeValue = replaceMaterial.getChildNumber() ;
				
			}else if( attributeName.equals( REPLACE_PART_NUMBER ) ){
				
				attributeValue = replaceMaterial.getReplacePartNumber() ;
				
			} else if( attributeName.equals( QUANTIRY ) ){
				
//				attributeValue = replaceMaterial.getQuantity() ;
				
			}else if( attributeName.equals( UNIT ) ){
				
				attributeValue = replaceMaterial.getUnit() ;
				
			}else if( attributeName.equals( RELACE_RELATIONSHIP ) ){
				
				attributeValue = replaceMaterial.getReplaceRelationship() ;
				
			}else if( attributeName.equals( REPLACE_DIRECTION ) ){
				
				attributeValue = replaceMaterial.getReplaceDirection() ;
				
			}else{
				attributeValue =  "" ;
			}
		}catch(Exception e){
			e.printStackTrace() ;
		}finally{
			
		}

		return attributeValue ;
	}
	
	/**
	 * 获取Part的全局替代关系
	 * 
	 * @param partMaster WTPartMaster  添加的对象
	 * 
	 * @return List 全局替代关系列表
	 * 
	 * <b>Revision History</b> <br>
	 * <b>Rev:</b> 1.0 - 2012-07-12，魏文杰 <br>
	 * <b>Comment:</b> Initial release.
	 */
	@SuppressWarnings("rawtypes")
	public static List<AlternativeMaterial> getAlternateLink(WTPartMaster partMaster){
		List<AlternativeMaterial> list = new ArrayList<AlternativeMaterial>() ;
		
		if(partMaster != null){

			//获取全局替换部件
			try {
				WTCollection wtcol = WTPartHelper.service.getAlternateLinks( partMaster );
				
				if(wtcol != null){
//					if(verbose){
//						String partMasterNumber = partMaster.getNumber() ;
//						String logInfo = "零部件：" + partMasterNumber + "的全局替代关系数量为：" + wtcol.size() ;
//						PDMIntegrationLogUtil.printLog( clazz , logInfo , IntegrationConstant.LOG_LEVEL_WARN) ;
//					}
					logger.debug("零部件：" + partMaster.getNumber()  + "的全局替代关系数量为：" + wtcol.size());

					AlternativeMaterial aMaterial = null ;
					
					Iterator ite = wtcol.iterator() ;
					
					while( ite.hasNext()){
						Object obj = ite.next() ;
						
						if( obj != null && obj instanceof ObjectReference){
							ObjectReference objRef = ( ObjectReference ) obj ;
							
							Object tempObj = objRef.getObject() ;
							
							if(tempObj != null && tempObj instanceof WTPartAlternateLink ){
								WTPartAlternateLink alternateLink = ( WTPartAlternateLink ) tempObj ;
																
								WTPartMaster alternatePartMaster = alternateLink.getAlternates();
								
								aMaterial = new AlternativeMaterial() ;
								aMaterial.initialize();//处理默认值
								Collection<String> fieldNames = aMaterial.getAnnotationFieldNames() ;
								for(String name : fieldNames){
									GenerateBeanInfo.setFieldValue(aMaterial, name, alternateLink) ;//处理内部值
								}
								//全局替代没有父件编号属性
								aMaterial.setParentNumber( IntegrationConstant.NO_VALUE ) ;
								
								//全局替代没有父件视图属性
								aMaterial.setParentView( IntegrationConstant.NO_VALUE ) ;
								
								aMaterial.setChildNumber( partMaster.getNumber() ) ;
								aMaterial.setReplacePartNumber( alternatePartMaster.getNumber() ) ;	
								
								//全局替代不维护替代数量
//								aMaterial.setQuantity( "" ) ;
								
								//全局替代不维护替代单位
								aMaterial.setUnit( "" ) ;
								
								aMaterial.setReplaceRelationship("A") ;
								
								//全局替代维护替代方向，暂时没有查到获取替代方向的API代码。
								aMaterial.setReplaceDirection( "") ;
								
								aMaterial.setFlag(IntegrationConstant.PDM_RELEASE_STATUS);
								aMaterial.setReleaseDate(CommonUtil.getCurrentDate());
								//设置空格
								aMaterial.setPdmWritebackStatus(" ");
								aMaterial.setOid(CommonPDMUtil.getObjectID(alternateLink) + "");
								
								list.add(aMaterial) ;
							}else{
								
							}
						}else{
							
						}
					}
				}
			} catch (WTException e) {
				e.printStackTrace();
			}finally{
				
			}
		}
		return list ;
	}
	
	/**
	 * 获取Part的特定替代关系
	 * 
	 * @param part WTPart 添加的对象
	 * @param usageLink WTPartUsageLink 添加的对象
	 * 
	 * @return List 局部替代关系列表
	 * <b>Revision History</b> <br>
	 * <b>Rev:</b> 1.0 - 2012-07-12，魏文杰 <br>
	 * <b>Comment:</b> Initial release.
	 */
	@SuppressWarnings("rawtypes")
	public static List<AlternativeMaterial> getSubstituteLink(WTPartUsageLink usageLink){
		
		List<AlternativeMaterial> list = new ArrayList<AlternativeMaterial>() ;
		
		//这个获取特定替换部件
		if( usageLink != null ){
			try {
				WTPart parentPart = usageLink.getUsedBy();
				WTPartMaster childPartMaster = usageLink.getUses() ;
				
				String parentPartNumber = parentPart.getNumber() ;
				String parentPartView = parentPart.getViewName() ;
				String majorVersion = parentPart.getVersionIdentifier().getValue();
				String childPartNumber = childPartMaster.getNumber() ;
				
				//获取usageLink上的所有特定替代关系
				WTCollection wtcol = WTPartHelper.service.getSubstituteLinks( usageLink ) ;
				
				if(wtcol != null){
					
					logger.debug("父件零部件：" + parentPartNumber + "," + parentPartView +","+majorVersion+ "子件零部件" + childPartNumber + "的特定替代关系数量为：" + wtcol.size());
					
					AlternativeMaterial aMaterial = null ;
					
					Iterator ite = wtcol.iterator() ;
					
					while( ite.hasNext()){
						Object obj = ite.next() ;
						
						if( obj != null && obj instanceof ObjectReference){
							ObjectReference objRef = ( ObjectReference ) obj ;
							
							Object tempObj = objRef.getObject() ;
							
							if(tempObj != null && tempObj instanceof WTPartSubstituteLink ){
								WTPartSubstituteLink substituteLink = (WTPartSubstituteLink) tempObj ;
								
								aMaterial = generateAlternativeMaterial(substituteLink, "", "");
								
								list.add(aMaterial) ;
							}else{
								
								if( tempObj == null ){
									logger.debug("SubstituteLink , tempObj == null ");
								}else{
									logger.debug("SubstituteLink , tempObj type is " + tempObj.getClass());
								}
							}
							
						}else{
							if( obj == null ){
								logger.debug("SubstituteLink , obj == null ");
							}else{
								logger.debug("SubstituteLink , obj type is " + obj.getClass());
							}
						}
					}
				}
			} catch (WTException e) {
				e.printStackTrace();
			}
		}
		
		return list ;
	}
	
	/*
	 * 根据WTPartSubstituteLink生成AlternativeMaterial 数据
	 * @param substituteLink 需要转换的WTPartSubstituteLink
	 * @return 根据substituteLink转换后的AlternativeMaterial
	 */
	public static AlternativeMaterial generateAlternativeMaterial( WTPartSubstituteLink substituteLink, String releaseDate, String batchNumber) {
		AlternativeMaterial aMaterial = new AlternativeMaterial();
		if(substituteLink != null){
			aMaterial.initialize();//处理默认值
			Collection<String> fieldNames = aMaterial.getAnnotationFieldNames() ;
			for(String name : fieldNames){
				GenerateBeanInfo.setFieldValue(aMaterial, name, substituteLink) ;//处理内部值
			}
			WTPartUsageLink usageLink = substituteLink.getSubstituteFor();
			WTPart parentPart = usageLink.getUsedBy();
			String childPartNumber = usageLink.getUses().getNumber();
			SubstituteQuantity quantity = substituteLink.getQuantity();
						
			String parentPartNumber = parentPart.getNumber();
			String parentPartView = parentPart.getViewName() ;
//			String majorVersion = parentPart.getVersionIdentifier().getValue();
			
			aMaterial.setParentNumber( parentPartNumber ) ;
			aMaterial.setParentView( parentPartView ) ;
			
			WTPartMaster substitutePartMaster = substituteLink.getSubstitutes() ;
			
			//新增父件大版本
			//aMaterial.setMajorVersion( majorVersion );
			aMaterial.setChildNumber( childPartNumber ) ;
			aMaterial.setReplacePartNumber( substitutePartMaster.getNumber() ) ;
			if(quantity != null){
				aMaterial.setQuantity( quantity.getAmount() ) ;
				//TODO 是否和ERP对照单位
//				aMaterial.setUnit( CommonPDMUtil.getERPMappingUnit(quantity) ) ;
				if(quantity.getUnit()!=null){
					aMaterial.setUnit(quantity.getUnit().toString());
				}
			}
			
			aMaterial.setReplaceRelationship("S") ;
			
			//特定替代不维护替代方向
			aMaterial.setReplaceDirection("") ;
			
			aMaterial.setFlag(IntegrationConstant.PDM_RELEASE_STATUS);
			
			aMaterial.setReleaseDate(CommonUtil.getCurrentDate());
			
			aMaterial.setBatchNumber(batchNumber);
			
			aMaterial.setOid(CommonPDMUtil.getObjectID(substituteLink) + "");
			//设置空格
			aMaterial.setPdmWritebackStatus(" ");
			if(logger.isDebugEnabled()){
				logger.debug("aMaterial is: "+ aMaterial);
			}
		}
		return aMaterial;
	}
}
