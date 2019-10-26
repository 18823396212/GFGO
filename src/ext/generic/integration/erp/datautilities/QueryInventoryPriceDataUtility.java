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

import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.AbstractDataUtility;
import com.ptc.mvc.util.ClientMessageSource;
import com.ptc.mvc.util.ResourceBundleClientMessageSource;

import ext.generic.integration.erp.bean.InventoryPrice;
import ext.generic.integration.erp.rule.BussinessRule;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import wt.log4j.LogR;
import wt.util.WTException;
/*
 * 库存价格显示的DataUtility
 * 
 */
public class QueryInventoryPriceDataUtility extends AbstractDataUtility {
	private static String clazz = QueryInventoryPriceDataUtility.class.getName();
	private static final Logger logger = LogR.getLogger(clazz);
	private static ClientMessageSource messageSource = new ResourceBundleClientMessageSource(
			"ext.generic.integration.erp.manually.IntegrationResource");

	public Object getDataValue(String param, Object object, ModelContext modelContext) throws WTException {
		Object obj = null;

		if ((object != null) && ((object instanceof InventoryPrice))) {
			InventoryPrice inventoryPrice = (InventoryPrice) object;

			String value = "";
			Class<?> clazz = inventoryPrice.getClass();

			try {
				Field field = clazz.getDeclaredField(param);
				if (field != null) {
					PropertyDescriptor pd = new PropertyDescriptor(field.getName(), clazz);
					Method getMethod = pd.getReadMethod();
					if (getMethod.invoke(inventoryPrice) != null) {
						value = (String) getMethod.invoke(inventoryPrice);
					}
				}
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (IntrospectionException e) {
				e.printStackTrace();
			}

			// 如果是价格，需要先判断是否可以显示
			if (param.equals("iAveragecost")) {
				if (BussinessRule.canReadPrice()) {
					obj = value;
				} else {
					obj = messageSource.getMessage("QUERY_INVENTORY_PRICE_TABLE_NO_PRIVILEGE");
				}
			} else {
				obj = value;
			}
		}

		logger.debug("param is:" + param + " obj is: " + obj);

		return obj;
	}
}