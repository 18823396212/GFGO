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

public class ReleaseServiceFactory {
	
	private static String developmentType="";
	
	private static final String IBATIS = "ibatis";
	
	private static final String INFOENGINE = "infoengine";
	static{
//		developmentType = BusinessRuleXMLConfigUtil.getInstance().getDevelopmentType();
		developmentType = IBATIS;
	}
	/**
	 * 获取发布零部件业务类实例
	 * @param type
	 * @return
	 */
	public static PartReleaseServiceImpl getPartReleaseServiceObjcet(){
		PartReleaseServiceImpl partReleaseServiceImpl = null;
		partReleaseServiceImpl = new IbatisPartReleaseService();
//		if(developmentType.equals(IBATIS)){
//			partReleaseServiceImpl = new IbatisPartReleaseService();
//		}else if(developmentType.equals(INFOENGINE)){
////			partReleaseServiceImpl = new InfoEnginePartReleaseService();
//		}else{
//			partReleaseServiceImpl = new IbatisPartReleaseService();
//		}
		return partReleaseServiceImpl;
	}
	
	/**
	 * 获取发布BOM业务类实例
	 * @param type
	 * @return
	 */
	public static BOMReleaseServiceImpl getBomReleaseServiceObjcet(){
		BOMReleaseServiceImpl bomReleaseServiceImpl = null;
		bomReleaseServiceImpl = new IbatisBOMReleaseService();
//		if(developmentType.equals(IBATIS)){
//			bomReleaseServiceImpl = new IbatisBOMReleaseService();
//		}else if(developmentType.equals(INFOENGINE)){
////			bomReleaseServiceImpl = new InfoEngineBOMReleaseService();
//		}else{
//			bomReleaseServiceImpl = new IbatisBOMReleaseService();
//		}
		return bomReleaseServiceImpl;
	}
	
	/**
	 * 获取发布ECN业务类实例
	 * @param type
	 * @return
	 */
	public static ECNReleaseServiceImpl getEcnReleaseServiceObjcet(){
		ECNReleaseServiceImpl ecnReleaseServiceImpl = null;
		ecnReleaseServiceImpl = new IbatisECNReleaseService();
//		if(developmentType.equals(IBATIS)){
//			ecnReleaseServiceImpl = new IbatisECNReleaseService();
//		}
//			else if(developmentType.equals(INFOENGINE)){
//			ecnReleaseServiceImpl = new InfoEngineECNReleaseService();
//		}else{
//			ecnReleaseServiceImpl = new IbatisECNReleaseService();
//		}
		return ecnReleaseServiceImpl;
	}

}
