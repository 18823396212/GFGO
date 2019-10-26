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
package ext.generic.integration.erp.bean;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import ext.generic.integration.annotations.ERPAttributeMapping;
import ext.generic.integration.annotations.FieldMapping;
import wt.log4j.LogR;

/**
 * 轻量级Java Bean抽象基类
 * 
 * @author WeiWenJie
 *
 */
public abstract class AbstractLightBean implements Serializable {
	static final long serialVersionUID = 1L;
	private static final String CLASSNAME = AbstractLightBean.class.getName();
	private static final Logger logger = LogR.getLogger(CLASSNAME);
	
	/**
	 * 
	 * annotationFieldNames 字段存储的是Java Bean中所有标注了FieldMapping注解的字段集合
	 */
	private Collection<String> annotationFieldNames = new ArrayList<String>() ;
	/**
	 * 
	 * annotationAttributeNames 字段存储的是Java Bean中所有标注了ERPAttributeMapping注解的字段集合
	 */
	private Collection<String> annotationAttributeNames = new ArrayList<String>();
	
	/**
	 * 初始化Bean对象：
	 * 1. 设置字段默认值
	 * 2. 获取需要转换的属性列表
	 * 
	 * @throws IntegrationException 
	 */
	public void initialize() {
		Field[] fields = this.getClass().getDeclaredFields() ;
		if(fields != null){
			for(Field field : fields){
				setFieldDefaultValue(field) ;
				addToCollection(field) ;
			}
		}
	}

	/**
	 * 初始化字段默认值
	 * 
	 * @param field
	 * @throws IntegrationException 
	 */
	public void setFieldDefaultValue(Field field) {
		if(field == null){
			return ;
		}
		
		FieldMapping annotation = field.getAnnotation( FieldMapping.class ) ;
		if( annotation != null){
			String defaultStringValue = annotation.defaultValue() ;
			//如果默认值为null或者""，则不在重新设置默认值
			if( isNull(defaultStringValue) ){
				return ;
			}
			//TODO
//			Object defaultValue = toObject(field, defaultStringValue) ;
			setFieldValue(field, defaultStringValue);
		}
	}

	/**
	 * 设置字段的值
	 * 
	 * @param field		字段
	 * @param value		值
	 * @throws IntegrationException 
	 */
	public void setFieldValue(Field field, Object value){
		if(field == null){
			return ;
		}
		
		try {
			if(!field.isAccessible()){
				field.setAccessible(true);
				field.isAccessible();
				field.set(this, value);
				field.setAccessible(false);
			}else{
				field.set(this, value);
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 将被注解标注过得字段名称收集到一个集合中
	 * 
	 * @param field
	 */
	public void addToCollection(Field field) {
		if(field == null){
			return ;
		}
		
		FieldMapping annotation = field.getAnnotation( FieldMapping.class ) ;
		if( annotation != null){
			this.annotationFieldNames.add( field.getName() ) ;
		}
		ERPAttributeMapping atrnotation = field.getAnnotation(ERPAttributeMapping.class);
		if( atrnotation != null){
			this.annotationAttributeNames.add(field.getName());
		}
	}

	
	/**
	 * 获取带有指定注解的所有Field的名称
	 * 
	 * @param clazz
	 * @return
	 */
	public Collection<Field> getFieldNamesWithAnnotation(Class<? extends Annotation> clazz) {
		Collection<Field> fieldList = new ArrayList<Field>() ;
		Field[] fields = this.getClass().getDeclaredFields() ;
		if( fields != null ){
			for(Field field : fields){
				Annotation annotation = field.getAnnotation( clazz ) ;
				if( annotation != null ){
					fieldList.add( field ) ;
				}
			}
		}
		return fieldList ;
		
	}
	
	/**
	 * 通过字段名称获取字段对象，如果字段没有使用FieldMapping注解声明，则抛出异常
	 * 
	 * @param name		字段名称
	 * 
	 * @return
	 * @throws IntegrationException
	 */
	public Field getDeclaredFieldWithAnnotation(String name) {
		try {
			Field field = this.getClass().getDeclaredField(name) ;
			if( field != null ){
				FieldMapping annotation = field.getAnnotation( FieldMapping.class ) ;
				if( annotation != null){
					return field ;
				}
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * AttributeMapping注解声明的字段
	 * @param name
	 * @return
	 */
	public Field getDeclaredFieldWithAttributeAnnotation(String name) {
		try {
			Field field = this.getClass().getDeclaredField(name) ;
			if( field != null ){
				ERPAttributeMapping annotation = field.getAnnotation( ERPAttributeMapping.class ) ;
				if( annotation != null){
					return field ;
				}
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取注解中指定的defaultValue
	 * 
	 * @param field
	 * @return
	 * @throws IntegrationException
	 */
	public String getDefaultValue(Field field) {
		if( field != null ){
			FieldMapping annotation = field.getAnnotation( FieldMapping.class ) ;
			if( annotation != null){
				return annotation.defaultValue() ;
			}
		}
		return null;
	}
	
	/**
	 * 获取注解中指定的、用于从Windchill系统中取值的Class
	 * 
	 * @param field
	 * @return
	 * @throws IntegrationException
	 */
	public Class<?> getValueClass(Field field) {
		if( field == null ){
			return null;
		}
		
		FieldMapping annotation = field.getAnnotation( FieldMapping.class ) ;
		if( annotation != null){
			try {
				Class<?> clazz = null ;
				String className = annotation.valueClass() ;
				if( ! isNull(className )){
					clazz = Class.forName(className) ;
				}
				
				return clazz ;
			} catch (ClassNotFoundException e) {
			}
		}
		return null;
	}
	/**
	 * 
	 * @param field
	 * @return
	 */
	public Class<?> getAttrValueClass(Field field) {
		if( field == null ){
			return null;
		}
		
		ERPAttributeMapping annotation = field.getAnnotation( ERPAttributeMapping.class ) ;
		if( annotation != null){
			try {
				Class<?> clazz = null ;
				String className = annotation.valueClass() ;
				if( ! isNull(className )){
					clazz = Class.forName(className) ;
				}
				
				return clazz ;
			} catch (ClassNotFoundException e) {
			}
		}
		return null;
	}
	
	/**
	 * 获取注解中指定的、用于从Windchill系统中取值的Method
	 * 
	 * @param field
	 * @return
	 * @throws IntegrationException
	 */
	public String getValueMethod(Field field){
		if(field != null){
			FieldMapping annotation = field.getAnnotation( FieldMapping.class ) ;
			if( annotation != null){
				return annotation.valueMethod() ;
			}
		}
		return null;
	}
	/**
	 * 
	 * @param field
	 * @return
	 */
	public String getAttrValueMethod(Field field){
		if(field != null){
			ERPAttributeMapping annotation = field.getAnnotation( ERPAttributeMapping.class ) ;
			if( annotation != null){
				return annotation.valueMethod() ;
			}
		}
		return null;
	}
	
	/**
	 * 获取注解中指定的language
	 * 
	 * @param field
	 * @return
	 * @throws IntegrationException
	 */
	public String getLanguage(Field field)  {
		if( field != null ){
			FieldMapping annotation = field.getAnnotation( FieldMapping.class ) ;
			if( annotation != null){
				return annotation.language() ;
			}
		}
		return null;
	}
	
	/**
	 * 获取注解中指定的logicalId
	 * 
	 * @param field
	 * @return
	 * @throws IntegrationException
	 */
	public String getLogicalId(Field field)  {
		if( field != null ){
			FieldMapping annotation = field.getAnnotation( FieldMapping.class ) ;
			if( annotation != null){
				return annotation.logicalId() ;
			}
		}
		return null;
	}
	/**
	 * 获得注解中的attrName
	 * @param field
	 * @return
	 */
	public String getAttrName(Field field)  {
		if( field != null ){
			ERPAttributeMapping annotation = field.getAnnotation( ERPAttributeMapping.class ) ;
			if( annotation != null){
				return annotation.attrName() ;
			}
		}
		return null;
	}
	
	/**
	 * 获取字段的值
	 * 
	 * @param field
	 * @return
	 * @throws IntegrationException
	 */
	public Object getFieldValue(Field field)  {
		Object value = null ;
		if( field == null){
			return value ;
		}
		
		try {
			if( field.isAccessible() ){
				value = field.get(this) ; 
			}else{
				field.setAccessible(true);
				value = field.get(this) ; 
				field.setAccessible(false);
			}
		} catch (SecurityException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		}
		return value ;
	}
	
	/**
	 * 判断String是否为空字符串
	 * 
	 * @param value
	 * @return
	 */
	public boolean isNull(String value) {
		return ( value == null || value.trim().isEmpty() );
	}
	
	public Collection<String> getAnnotationFieldNames() {
		return annotationFieldNames;
	}

	public void setAnnotationFieldNames(Collection<String> annotationFieldNames) {
		this.annotationFieldNames = annotationFieldNames;
	}

	public Collection<String> getAnnotationAttributeNames() {
		return annotationAttributeNames;
	}

	public void setAnnotationAttributeNames(
			Collection<String> annotationAttributeNames) {
		this.annotationAttributeNames = annotationAttributeNames;
	}
	
}
