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
package ext.generic.integration.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 用于从Windchill系统中取值，
 * 可将Windchill系统的关系型对象，转换成容易使用的轻量级Java Bean对象
 * --主要是一些不能直接获取的属性
 * 
 * @author Administrator
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ERPAttributeMapping {
	// 字段对应的属性的内部名称，如果没有指定逻辑id，就不从Windchill中取值
	String attrName();

	// 指定默认值
	String defaultValue() default "";
	
	// 国际化属性返回那种语言的值zh_CN,en_US
	String language() default "";

	// 获取属性值的方法
	String valueClass() default "ext.generic.integration.erp.attributes.AttributeUtil";

	// 获取属性值的方法
	String valueMethod() default "getAttributeValue";
}
