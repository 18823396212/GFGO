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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ext.generic.integration.erp.bean.AlternativeMaterial;
import wt.change2.WTChangeOrder2;
import wt.iba.value.DefaultAttributeContainer;
import wt.iba.value.IBAHolder;
import wt.iba.value.litevalue.AbstractValueView;
import wt.iba.value.service.IBAValueHelper;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.part.WTPartUsageLink;
import wt.util.WTException;
/**
 * 
 * 本类作用：为获取对象的属性提供统一的代码
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-10-08
 */
public class AttributeUtil {
	
	private static final String CLASSNAME = AlternativeMaterialUtil.class.getName() ;

	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	/**
	 * 根据对象和属性名获得属性值
	 * @param obj 对象
	 * @param attrName 属性名称
	 * @return 属性值
	 * @throws WTException 
	 */
	public static Object getAttributeValue(Object obj, String attrName) throws WTException{
		logger.debug("Enter .getAttributeValue() obj="+obj + ",,,attrName=" +attrName);
		Object value = null;
		if(obj == null){
			return value;
		}
		if( attrName == null || attrName.trim().equals("") ){
			logger.debug(" attrName is NULL!");
			return value ;
		}
		if( obj instanceof WTPart ){
			//获取WTPart的属性值
			WTPart part = ( WTPart ) obj ;
			value = PartAttribute.getAttribute(part, attrName) ;
		}else if( obj instanceof WTPartUsageLink ){
			WTPartUsageLink usageLink = ( WTPartUsageLink ) obj ;
			value = PartUsageLinkAttribute.getAttribute(usageLink, attrName) ;
		}else if( obj instanceof WTChangeOrder2 ){
			WTChangeOrder2 ecn = ( WTChangeOrder2 ) obj ;
			value = ECNAttribute.getAttribute(ecn, attrName) ;
		}else if( obj instanceof AlternativeMaterial ){
			AlternativeMaterial replaceMaterial = ( AlternativeMaterial ) obj ;
			value = AlternativeMaterialUtil.getAttribute(replaceMaterial, attrName) ;
		}
		logger.debug("Exit .getAttributeValue() value="+value);
		return value;
	}
	
	/**
	 * 获取对象的属性值
	 * 
	 * @param obj
	 * @param attributeName
	 * @return String
	 * @throws WTException 
	 */
	public static String getAttribute( Object obj , String attributeName ) throws WTException{
		String attributeValue = "" ;
		
		try{
			if(obj == null){
				logger.debug("getAttribute( Object obj , String attributeName ), obj is NULL!");
				return attributeValue ;
			}else if( attributeName == null || attributeName.trim().equals("") ){
				logger.debug("getAttribute( Object obj , String attributeName ), attributeName is NULL!");
				
				return attributeValue ;
			}else{
				if( obj instanceof WTPart ){
					//获取WTPart的属性值
					WTPart part = ( WTPart ) obj ;
					
					attributeValue = PartAttribute.getAttribute(part, attributeName) ;
				}else if( obj instanceof WTPartUsageLink ){
					
					WTPartUsageLink usageLink = ( WTPartUsageLink ) obj ;
					
					attributeValue = PartUsageLinkAttribute.getAttribute(usageLink, attributeName) ;
				}else if( obj instanceof WTChangeOrder2 ){
					
					WTChangeOrder2 ecn = ( WTChangeOrder2 ) obj ;
					
					attributeValue = ECNAttribute.getAttribute(ecn, attributeName) ;
				}else if( obj instanceof AlternativeMaterial ){
					
					AlternativeMaterial replaceMaterial = ( AlternativeMaterial ) obj ;
					
					attributeValue = AlternativeMaterialUtil.getAttribute(replaceMaterial, attributeName) ;
				}else {
					attributeValue = "" ;
				}
			}
			
			//added by zhouhaiwei on 2014-12-11,由于Oracle数据库不支持单引号，去掉属性中的单引号。
			if(attributeValue  != null)
			{
				//使用iBATIS不需要进行转义处理，去掉如下代码
				//attributeValue = attributeValue.replaceAll("\\'","\\'\\'");
				
				//attributeValue = attributeValue.replaceAll("&"," and ");
			}
			else
			{
				attributeValue = "";
			}
			//end added
		}
		//拦截异常并抛出
		catch ( Exception e ) {
			e.printStackTrace();
			throw new WTException( e );
		}finally{
			
		}
		
		return attributeValue ;
	}
	
	/**
	 * 获取IBA属性值表
	 * @param ibaHolder IBA储存器，例如文档、零部件、图档等
	 * @return
	 * @throws WTException
	 * @throws java.rmi.RemoteException
	 */
	public static List<String> getIBANamesList(IBAHolder ibaHolder) {
		List<String> ibaNamesList = new ArrayList<String>() ;
		
		try{
			ibaHolder = IBAValueHelper.service.refreshAttributeContainerWithoutConstraints(ibaHolder);
			DefaultAttributeContainer dac = (DefaultAttributeContainer) ibaHolder.getAttributeContainer();
			AbstractValueView valueViews[] = dac.getAttributeValues();
			
			for (int j = 0; j < valueViews.length; j++) {
				AbstractValueView avv = valueViews[j];
				
				String ibaLogicalID =  avv.getDefinition().getLogicalIdentifier();
				
				ibaNamesList.add(ibaLogicalID) ;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}finally{
			
		}
		
		return ibaNamesList ;
	}
	
	
	/**
	 * 取IBAHolder的所有IBA属性值:
	 *
	 * String result[i][0]: 第i个属性的名称<br>
	 * String result[i][1]: 第i个属性的值
	 *
	 * @param ibaHolder
	 * @return
	 */
	public static List<String> getIBANamesLite(IBAHolder ibaHolder){
		List<String> ibaNameList = new ArrayList<String>() ;
		
		if( ibaHolder == null ){
			return ibaNameList ;
		}
		
		DefaultAttributeContainer dac = (DefaultAttributeContainer) ibaHolder.getAttributeContainer();
		if (dac == null) {
			try {
				ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder, null, null, null);
				dac = (DefaultAttributeContainer) ibaHolder.getAttributeContainer();
			} catch (Exception e) {
				e.printStackTrace();
				return ibaNameList ;
			}
		}

		if( dac == null ){
			return ibaNameList ;
		}	
			
		AbstractValueView[] avv = dac.getAttributeValues();
		
		if ( avv == null){
			return ibaNameList ;	
		}

		for (int i = 0; i < avv.length; i++) {
			String ibaName = avv[i].getDefinition().getLogicalIdentifier() ;
			
			ibaNameList.add( ibaName ) ;
		}

		return ibaNameList;
	}
}
