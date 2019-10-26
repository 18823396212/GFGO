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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ext.com.core.CoreUtil;
import ext.generic.integration.erp.bean.AlternativeMaterial;
import wt.vc.config.ConfigException;
import wt.vc.config.ConfigSpec;
import wt.vc.config.LatestConfigSpec;
import wt.vc.struct.StructHelper;
import wt.part.WTPart;
import wt.util.WTException;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.collections.WTCollection;
import wt.method.RemoteAccess;
import wt.part.WTPartConfigSpec;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartStandardConfigSpec;
import wt.part.WTPartSubstituteLink;
import wt.part.WTPartUsageLink;
import wt.vc.views.View;
import wt.vc.views.ViewHelper;
import wt.vc.views.ViewReference;
/**
 * BOM结构工具类
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-10-08
 */
public class BOMStructureUtil implements RemoteAccess {
	
	/**
	 * 获取零部件第一层父子件使用关系
	 * 
	 * @param part
	 * @return
	 * @throws ConfigException
	 * @throws WTException
	 */
	public static List<WTPartUsageLink> getFirstLevelUsageLink(WTPart part) throws ConfigException, WTException {
		List<WTPartUsageLink> subPartUsageLinkList = new ArrayList<WTPartUsageLink>() ;
		
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
	 * 根据父项获取第一层子件的特定替代
	 * 
	 * @param part 需要获取特定替代关系的父项
	 * @return
	 * @throws ConfigException
	 * @throws WTException
	 */
	public static List<WTPartSubstituteLink> getWTPartSubsituteLink(WTPart part) throws ConfigException, WTException {
		List<WTPartSubstituteLink> substituteLinkList = new ArrayList<WTPartSubstituteLink>() ;
		
		ConfigSpec latestconfigspec = getConfigSpec(part);
		
		QueryResult qr= StructHelper.service.navigateUsesToIteration(part,WTPartUsageLink.class,false,latestconfigspec);
		
		while(qr != null && qr.hasMoreElements()){
			//每一个element实际是一个persistable数组
			Persistable apersistable[] = ( Persistable[] ) qr.nextElement();  
			
			//数组中第一个对象是usagelink
			WTPartUsageLink partUsageLink= (WTPartUsageLink)apersistable[0];   
			
			//获取usageLink上的所有特定替代关系
			WTCollection wtcol = WTPartHelper.service.getSubstituteLinks( partUsageLink ) ;
			if(wtcol != null){				
				AlternativeMaterial aMaterial = null ;
				
				Iterator ite = wtcol.iterator() ;
				
				while( ite.hasNext()){
					Object obj = ite.next() ;
					
					if( obj != null && obj instanceof ObjectReference){
						ObjectReference objRef = ( ObjectReference ) obj ;
						
						Object tempObj = objRef.getObject() ;
						
						if(tempObj != null && tempObj instanceof WTPartSubstituteLink ){
							WTPartSubstituteLink substituteLink = (WTPartSubstituteLink) tempObj ;
							substituteLinkList.add(substituteLink);
						}
					}
				}
			}
		}
		
		return substituteLinkList;
	}
	
	
	/**
	 * 获取零部件第一层子件
	 * 
	 * @param part
	 * @return
	 * @throws ConfigException
	 * @throws WTException
	 */
	public static List<WTPart> getFirstLevelChildren(WTPart part) throws ConfigException, WTException {
//		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		
		List<WTPart> subPartList = new ArrayList<WTPart>() ;
		
		try{
			ConfigSpec latestconfigspec = getConfigSpec(part);
			
			QueryResult qr= StructHelper.service.navigateUsesToIteration(part,WTPartUsageLink.class,false,latestconfigspec);
			
			while(qr != null && qr.hasMoreElements()){
				//每一个element实际是一个persistable数组
				Persistable apersistable[] = ( Persistable[] ) qr.nextElement();  
				
				//数组中第一个对象是usagelink			
				//数组中第二个对象是子件
				//如果当前用户的权限正常，应该是个WTPart
				//反之返回的是WTPartMaster
				//一般是前一种情况居多，这样可以省掉查询子件最新版本的代码，这样代码的执行效率大大增加
				Object uses= apersistable[1];   
				
				WTPart subPart=null; 
				if( uses instanceof WTPart ){
//					System.out.println("uses instanceof WTPart") ;
					subPart=(WTPart)uses; 
				} else if( uses instanceof WTPartMaster ){
//					System.out.println("uses instanceof WTPartMaster") ;
					WTPartMaster master = (WTPartMaster) uses ;
					subPart = CoreUtil.getWTPartByMasterAndView(master, part.getViewName()) ;
				} else {
//					if( uses != null ){
//						System.out.println("Object uses Type is " + uses.getClass()) ;
//					}else{
//						System.out.println("Object uses == null !") ;
//					}
				}
				
				if( subPart != null ){
					subPartList.add(subPart) ;
				}
			}
		}finally{
//			SessionServerHelper.manager.setAccessEnforced(flag);
		}
		
		return subPartList;
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
}
