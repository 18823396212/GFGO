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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ptc.core.meta.common.FloatingPoint;

import ext.customer.common.MBAUtil;
import ext.generic.integration.erp.attributes.AttributeUtil;
import ext.generic.integration.erp.attributes.PartAttribute;
import ext.generic.integration.erp.bean.AbstractLightBean;
import ext.generic.integration.erp.bean.BOMRootInfo;
import ext.generic.integration.erp.bean.EBOMInfo;
import ext.generic.integration.erp.bean.ECNInfo;
import ext.generic.integration.erp.bean.MaterialInfo;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.generic.integration.erp.common.CommonUtil;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.util.WTException;
import wt.vc.VersionControlHelper;


public class GenerateBeanInfo implements RemoteAccess{
	
	private static final String CLASSNAME = GenerateBeanInfo.class.getName();
	private static final Logger logger = LogR.getLogger(CLASSNAME);
	
	/**
	 * 根据部件创建物料信息Bean
	 * @param part 物料
	 * @param releaseTime 发布时间
	 * @param params 其它参数信息
	 * @return
	 * @throws WTException 
	 */
	public static MaterialInfo createMaterilInfoBeanByPart(WTPart part, String releaseTime,String batchNumber, Object... params) throws WTException{
		MaterialInfo bean = new MaterialInfo();
		bean.initialize();//处理默认值
		//处理内部值
		Collection<String> fieldNames = bean.getAnnotationFieldNames() ;
		for(String name : fieldNames){
			setFieldValue(bean, name, part) ;//处理内部值
		}
		//处理非直接获取属性值
		Collection<String> attrNames = bean.getAnnotationAttributeNames();
		for(String name : attrNames){
			setFieldAttributeValue(bean, name, part);
		}
		
		bean.setFlag(IntegrationConstant.PDM_RELEASE_STATUS);//pdm状态
		bean.setReleaseDate(CommonUtil.getCurrentDate());//pdm发布时间
		bean.setBatchNumber(batchNumber);//批次号
		//WriteBackStatus需要设置为空格
		bean.setPdmWritebackStatus(" ");
		logger.debug("Exit .createMaterilInfoBeanByPart() bean="+bean);
		return bean;
	}
	
	/**
	 * 将部件封装成MaterialInfo对象
	 * @param part
	 * @return
	 * @throws RemoteException
	 * @throws WTException
	 */
	@Deprecated
	public static MaterialInfo generateMaterilInfoByPart(WTPart part, String releaseDate, String batchNumber) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, WTException{
		Class cla=MaterialInfo.class;
		MaterialInfo info =  new MaterialInfo();
		Field[] fields = cla.getDeclaredFields();
		for (Field field : fields) {
			String attributeId = field.getName();
			
			//modify on 20160526, 为提高效率，过滤掉ERP相关的属性
			if(isAttribute(attributeId)){
				String mestodName = getSetMthodName(attributeId);
				Method method = cla.getMethod(mestodName,String.class);
				
				//实现默认值的设置，如果无法获取到属性，则不设置
				String attributeValue = AttributeUtil.getAttribute(part, attributeId);
				if(attributeValue != null && !attributeValue.isEmpty()){
					method.invoke(info, attributeValue);
				}
			}
		}
		
		info.setFlag(IntegrationConstant.PDM_RELEASE_STATUS);
//		info.setReleaseDate(releaseDate);
		info.setBatchNumber(batchNumber);
		//WriteBackStatus需要设置为空
		info.setPdmWritebackStatus(" ");
		
		return info;
	}
	/**
	 * 创建bom头表
	 * @param part
	 * @param batchNumber
	 * @param params
	 * @return
	 * @throws WTException 
	 */
	public static BOMRootInfo createBOMRootInfoBeanByPart(WTPart part, String releaseTime, String batchNumber, Object... params) throws WTException{
		BOMRootInfo bean = new BOMRootInfo();
		bean.initialize();//处理默认值
		Collection<String> fieldNames = bean.getAnnotationFieldNames() ;
		for(String name : fieldNames){
			setFieldValue(bean, name, part) ;//处理内部值
		}
		
		//处理非直接获取属性值
		Collection<String> attrNames = bean.getAnnotationAttributeNames();
		for(String name : attrNames){
			setFieldAttributeValue(bean, name, part);
		}
		//处理特殊值
		bean.setBatchNumber(batchNumber);
		
		bean.setFlag(IntegrationConstant.PDM_RELEASE_STATUS);//pdm状态
		bean.setReleaseDate(CommonUtil.getCurrentDate());//pdm发布时间
		//WriteBackStatus需要设置为空格
		bean.setPdmWritebackStatus(" ");
		logger.debug("Exit .createBOMRootInfoBeanByPart() bean="+bean);
		return bean;
	}
	
	/**
	 * 
	 * @param link
	 * @param releaseDate
	 * @param batchNumber
	 * @return
	 * @throws WTException 
	 */
	public static EBOMInfo createEBOMInfoByUserLink(WTPartUsageLink link, String releaseDate, String batchNumber) throws WTException {
		EBOMInfo bean =  new EBOMInfo();
		bean.initialize();//处理默认值
		if(link != null){
			Collection<String> fieldNames = bean.getAnnotationFieldNames() ;
			for(String name : fieldNames){
				setFieldValue(bean, name, link) ;//处理内部值
			}
			
			//处理非直接获取属性值
			Collection<String> attrNames = bean.getAnnotationAttributeNames();
			for(String name : attrNames){
				setFieldAttributeValue(bean, name, link);
			}
		}
		bean.setBatchNumber(batchNumber);
		bean.setFlag(IntegrationConstant.PDM_RELEASE_STATUS);
		bean.setReleaseDate(CommonUtil.getCurrentDate());
		//WriteBackStatus需要设置为空格
		bean.setPdmWritebackStatus(" ");
		logger.debug("Exit .generateEBOMInfoByUserLink() bean="+bean);
		return bean;
	}
	
	/**
	 * 生成BOMinfo 实例
	 * @param link 关联关系
	 * @param fieldMap 属性结合
	 * @return BOMinfo 实例
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws WTException
	 */
	@Deprecated
	public static EBOMInfo generateEBOMInfoByUserLink(WTPartUsageLink link, String releaseDate, String batchNumber) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, WTException{
		Class cla=EBOMInfo.class;
		EBOMInfo info =  new EBOMInfo();
		Field[] fields = cla.getDeclaredFields();
		for (Field field : fields) {
			String attributeId = field.getName();
			
			//add on 20160526, 为提高效率，过滤掉ERP相关的属性
			if(isAttribute(attributeId)){
				if("substitutionSeq".equals(attributeId)){
					//info.setSubstitutionSeq(100);
					continue;
				}
				
				String mestodName = getSetMthodName(attributeId);
				Method method = cla.getMethod(mestodName,String.class);
				
				//实现默认值的设置，如果无法获取到属性，则不设置
				String attributeValue = AttributeUtil.getAttribute(link, attributeId);
				if(attributeValue != null && !attributeValue.isEmpty()){
					method.invoke(info, attributeValue);
				}
			}
		}
		
		info.setFlag(IntegrationConstant.PDM_RELEASE_STATUS);
		info.setReleaseDate(CommonUtil.getCurrentDate());
		info.setBatchNumber(batchNumber);
		//WriteBackStatus需要设置为空
		info.setPdmWritebackStatus(" ");

		return info;
	}
	
	/**
	 * 生成BOMinfo 实例
	 * @param link 关联关系
	 * @param fieldMap 属性结合
	 * @return BOMinfo 实例
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws WTException
	 */
	@Deprecated
	public static BOMRootInfo generateBOMRootInfoPart(WTPart part, String releaseDate, String batchNumber) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, WTException{
		Class cla=BOMRootInfo.class;
		BOMRootInfo info =  new BOMRootInfo();
		Field[] fields = cla.getDeclaredFields();
		for (Field field : fields) {
			//String dbField = ite.next();
			String attributeId = field.getName();
			String mestodName = getSetMthodName(attributeId);
			Method method = cla.getMethod(mestodName,String.class);
			
			//add on 20160526, 为提高效率，过滤掉ERP相关的属性
			if(isAttribute(attributeId)){
				//实现默认值的设置，如果无法获取到属性，则不设置
				String attributeValue = AttributeUtil.getAttribute(part, attributeId);
				if(attributeValue != null && !attributeValue.isEmpty()){
					method.invoke(info, attributeValue);
				}			
			}
		}
		
		info.setFlag(IntegrationConstant.PDM_RELEASE_STATUS);
		info.setReleaseDate(CommonUtil.getCurrentDate());
		info.setBatchNumber(batchNumber);
		//WriteBackStatus需要设置为空
		info.setPdmWritebackStatus(" ");
		
		return info;
	
		
	}
	
	/**
	 * 根据ECN获得ecn的信息
	 * @param ecn
	 * @param releaseDate
	 * @param batchNumber
	 * @return
	 */
	public static ECNInfo createECNnfoByECN(WTChangeOrder2 ecn, String releaseDate, String batchNumber){
		ECNInfo info =  new ECNInfo();
		info.initialize();//处理默认值
		Collection<String> fieldNames = info.getAnnotationFieldNames() ;
		for(String name : fieldNames){
			setFieldValue(info, name, ecn) ;//处理内部值
		}
		//处理特殊值
		info.setBatchNumber(batchNumber);
		
		info.setFlag(IntegrationConstant.PDM_RELEASE_STATUS);
		info.setReleaseDate(CommonUtil.getCurrentDate());
		//设置成空格
		info.setPdmWritebackStatus(" ");
		logger.debug("Exit .createECNnfoByECN() info="+info);
		return info;
	}
	
	/**
	 * 
	 * @param ecn
	 * @param releaseDate
	 * @param batchNumber
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws WTException
	 */
	@Deprecated
	public static ECNInfo generateECNnfoByECN(WTChangeOrder2 ecn, String releaseDate, String batchNumber) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, WTException{
		Class cla=ECNInfo.class;
		ECNInfo info =  new ECNInfo();
		Field[] fields = cla.getDeclaredFields();
		for (Field field : fields) {
			//String dbField = ite.next();
			String attributeId = field.getName();
			String mestodName = getSetMthodName(attributeId);
			Method method = cla.getMethod(mestodName,String.class);
			//add on 20160526, 为提高效率，过滤掉ERP相关的属性
			if(isAttribute(attributeId)){
				method.invoke(info,AttributeUtil.getAttribute(ecn, attributeId));
			}
		}
		
		info.setReleaseDate(CommonUtil.getCurrentDate());
		info.setBatchNumber(batchNumber);
		info.setPdmWritebackStatus(" ");
		
		return info;
	
		
	}
	
	/**
	 * 生成EBOM信息 
	 * @param parent 父件
	 * @param childMaster子健
	 * @param lineNumber 行号
	 * @return  EBOM对象
	 * @throws WTException
	 */
	public static EBOMInfo generateEBOMInfo(WTPart parent , WTPartMaster childMaster , String lineNumber) throws WTException{
		EBOMInfo info =  new EBOMInfo();
		info.setParentNumber(parent.getNumber());
		info.setParentVersion(AttributeUtil.getAttribute(parent, PartAttribute.VERSION));
		info.setChildNumber(childMaster.getNumber());
		info.setLineNumber(Long.parseLong(lineNumber));
		info.setPdmWritebackStatus(" ");
		return info;
	}
	
	/**
	 * 根据字段名生成set方法名
	 * @param fildName
	 * @return
	 */
	public static String getSetMthodName(String fildName){
		
		fildName = "set"+fildName.substring(0, 1).toUpperCase()+fildName.substring(1);
		return fildName;
	}
	
	/*
	 * 判断是否为需要获取的对象属性
	 * @param attributeId 属性ID
	 * @return 是否为需要获取的对象属性
	 */
	private static boolean isAttribute(String attributeId){
		boolean isAttribute = true;
		if("flag".equals(attributeId) || "releaseDate".equals(attributeId) || "erpErrorMsg".equals(attributeId)
				|| "erpProcessDate".equals(attributeId) || "pdmWritebackStatus".equals(attributeId) || "batchNumber".equals(attributeId)){
			isAttribute = false;
		}
		return isAttribute;
	}
	
	/**
	 * 获得由内部值定义的属性值
	 * @param bean
	 * @param name
	 * @param persistable
	 * @param params
	 */
	public static void setFieldValue(AbstractLightBean bean, String name, Persistable persistable, Object... params) {
		Field field = bean.getDeclaredFieldWithAnnotation(name) ;
		if( field != null ){
			Class<?> valueClass = bean.getValueClass(field) ;
			String valueMethod = bean.getValueMethod(field) ;
			String logicalId = bean.getLogicalId(field) ;
			
			if( StringUtils.isBlank(logicalId) ){
				return ;
			}
			
			try {
				Method method = valueClass.getDeclaredMethod(valueMethod, Persistable.class, String.class) ;
				Object valueClassInstance = valueClass.newInstance() ;
				Object value = method.invoke(valueClassInstance, persistable, logicalId) ;
				if(value instanceof FloatingPoint){//实数直接转Double报错
					FloatingPoint fp = (FloatingPoint) value;
					value = fp.doubleValue();
				}else if(value instanceof Object[]){//多值处理
					Object[] objAry = (Object[]) value;
					value = StringUtils.join(objAry);
				}
				bean.setFieldValue(field, value);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}else{
			logger.error("Field is null, name="+name);
		}
	}
	
	/**
	 * 获得特殊属性值的方法
	 * @param bean
	 * @param name
	 * @param persistable
	 * @param params
	 */
	public static void setFieldAttributeValue(AbstractLightBean bean, String name, Persistable persistable, Object... params) {
		Field field = bean.getDeclaredFieldWithAttributeAnnotation(name);
		if( field != null ){
			Class<?> valueClass = bean.getAttrValueClass(field) ;
			String valueMethod = bean.getAttrValueMethod(field) ;
			String attrName = bean.getAttrName(field);
			
			if( StringUtils.isBlank(attrName) ){
				return ;
			}
			
			try {
				Method method = valueClass.getDeclaredMethod(valueMethod, Object.class, String.class) ;
				Object valueClassInstance = valueClass.newInstance() ;
				Object value = method.invoke(valueClassInstance, persistable, attrName) ;
				if(value instanceof FloatingPoint){//实数直接转Double报错
					FloatingPoint fp = (FloatingPoint) value;
					value = fp.doubleValue();
				}else if(value instanceof Object[]){//多值处理
					Object[] objAry = (Object[]) value;
					value = StringUtils.join(objAry);
				}
				bean.setFieldValue(field, value);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}else{
			logger.error("Field is null, name="+name);
		}
	}

}
