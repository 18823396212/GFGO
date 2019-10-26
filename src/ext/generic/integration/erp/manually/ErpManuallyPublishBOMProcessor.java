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
package ext.generic.integration.erp.manually;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.netmarkets.model.NmOid;
import ext.com.iba.IBAUtil;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.generic.integration.erp.common.CommonUtil;
import ext.generic.integration.erp.service.BOMReleaseService;
import org.apache.log4j.Logger;

import wt.fc.collections.WTKeyedHashMap;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.util.WTException;

import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.forms.FormResultAction;
import com.ptc.core.components.util.FeedbackMessage;
import com.ptc.core.ui.resources.FeedbackType;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import static ext.appo.erp.service.BomReleaseService.sendBOM;

import wt.iba.value.IBAHolder;


/**
 * 手工发布BOM到ERP系统集成的中间表
 *
 * @author Kwang
 * @Version 3.0
 * @Date 2014-12-02
 */
public class ErpManuallyPublishBOMProcessor extends DefaultObjectFormProcessor {
	private static final String clazz = ErpManuallyPublishBOMProcessor.class.getName() ;
	private static final Logger logger = LogR.getLogger(clazz);

	public ErpManuallyPublishBOMProcessor() {

	}

	public FormResult doOperation(NmCommandBean nmcommandbean, List list) throws WTException {
		return super.doOperation( nmcommandbean, list);
	}

	public FormResult preProcess(NmCommandBean nmcommandbean, List list) throws WTException {
		logger.debug("=====> 开始执行手工发布BOM到ERP系统") ;
		FormResult formresult = new FormResult();
		formresult.setStatus(FormProcessingStatus.SUCCESS);
		NmOid nmoid = ((ObjectBean) list.get(0)).getActionOid();
		WTPart wtpart = (WTPart) nmoid.getRef();
		//执行手工发布
		if( wtpart != null ){
			// 获取IP地址
			String ip = null;
			try {
				ip = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			System.out.println("IP地址："+ip);
			WTKeyedHashMap hashMap=new WTKeyedHashMap();
			//区别光峰61，70,71服务器，绎立服务器，光峰采用发K3方法，其余采用发中间表方法
			if (ip.equals("172.32.252.61")||ip.equals("172.32.252.70")||ip.equals("172.32.252.71")){
				System.out.println("手动发BOM调用K3接口=="+wtpart.getNumber());
				//发K3
				hashMap=sendBOM(wtpart);
				if (hashMap != null && hashMap.size() > 0) {
					//设置BOM发布状态失败
					updatePDMFailed(wtpart, "bomReleaseStatus");
				} else {
					//设置BOM发布状态成功
					updatePDMSucessful(wtpart, "bomReleaseStatus");
				}
			}else{
				System.out.println("手动发BOM调用中间表接口=="+wtpart.getNumber());
				String releaseTime = CommonUtil.getCurrentTime() ;
				// 发布BOM结构
				hashMap= BOMReleaseService.releaseSingleLevel(wtpart, releaseTime, null, CommonPDMUtil.getBatchNumber(null));
			}
			if (hashMap.size() > 0) {
				logger.error("BOM发布异常结果hashMap=" + hashMap);
				formresult.addFeedbackMessage(new FeedbackMessage(FeedbackType.FAILURE, (Locale)null, "BOM发布异常:"+hashMap, (ArrayList)null, new String[0]));
			} else {
				formresult.addFeedbackMessage(new FeedbackMessage(FeedbackType.SUCCESS, (Locale)null, "BOM发布成功", (ArrayList)null, new String[0]));
			}
			logger.error("手工发布失败：" + hashMap);
		}

//			if( hashMap.size() > 0 ){
//				logger.error("BOM发布异常结果hashMap="+hashMap);
//
//				String isExist="物料"+wtpart.getNumber()+"已经存在";
//				if (hashMap.toString().contains(isExist)){
//					formresult.addFeedbackMessage( new  FeedbackMessage(
//						FeedbackType.FAILURE, null, "物料"+wtpart.getNumber()+"已经存在BOM版本", null, new String[] {} ) );
//				}else{
//					formresult.addFeedbackMessage( new  FeedbackMessage(
//							FeedbackType.FAILURE, null, "BOM发布异常:"+hashMap , null, new String[] {} ) );
//				}
//			}else{
//				formresult.addFeedbackMessage( new  FeedbackMessage(
//						FeedbackType.SUCCESS, null, "BOM发布成功" , null, new String[] {} ) );
//			}


		//刷新页面
		formresult.setNextAction(FormResultAction.REFRESH_CURRENT_PAGE);

		logger.debug( "=====> 结束手工发布BOM到ERP系统") ;
		return formresult;
	}

	public static void updatePDMFailed(IBAHolder ibaHolder , String ibaName ){

		updatePDMData( ibaHolder , ibaName , "PDM发布失败"  ) ;

	}

	public static void updatePDMSucessful(IBAHolder ibaHolder , String ibaName ){
		updatePDMData( ibaHolder , ibaName , "PDM发布成功" );
	}

	protected static void updatePDMData(IBAHolder ibaHolder , String ibaName , String ibaValue ){
		if( ibaHolder != null ){
			try {
				Object obj = IBAUtil.getIBAValue(ibaHolder, ibaName);

				if( obj == null || ( (String)obj ).trim().isEmpty() || !( (String)obj ).trim().equals( ibaValue ) ){
					IBAUtil.forceSetIBAValue(ibaHolder, ibaName, ibaValue ) ;
				}

			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}
	}

//	//批量发BOM
//	public FormResult preProcess(NmCommandBean nmcommandbean, List list) throws WTException {
////		FormResult formresult = new FormResult();
////		formresult.setStatus(FormProcessingStatus.SUCCESS);
////WTPart part =  PartUtil.getLastestWTPartByNumber("A15000000012");
////		Vector MHistoricalDatavector = getHistoricalData(part, "Manufacturing");
////		WTKeyedHashMap map = new WTKeyedHashMap();
////		System.out.println("MHistoricalDatavector==="+MHistoricalDatavector.size());
////		for (int i = 0; i <MHistoricalDatavector.size() ; i++) {
////			map = sendBOM((WTPart) MHistoricalDatavector.get(i));
////
////		}
////		WTPart part = PartUtil.getLastestWTPartByNumber("B06000000237");
////		isBOM(part);
//
//		System.out.println("进入发送全部BOM方法：" + new Date());
//		FormResult formresult = new FormResult();
//		formresult.setStatus(FormProcessingStatus.SUCCESS);
//
//		String partNumber = "";//物料编码
//		String version = "";//物料版本
//		String isSuccess = "";//标志
//		String message = "";//详情
//
//		String count = "";//发送总数
//		String type="BOM发送异常信息";//发送类型
//
//		Vector dataList = new Vector();
//
//		Vector partDesignvector = new Vector();
//		Vector partManufacturvector = new Vector();
//		Vector DPartvector = new Vector();
//		Vector partDesignvector_B=new Vector();
//		try {
//			System.out.println("开始查询D物料：" + new Date());
//			partDesignvector = getAllLatestWTParts("Design", "");
//			System.out.println("结束查询D物料：" + new Date());
//			System.out.println("开始查询M物料：" + new Date());
//			partManufacturvector = getAllLatestWTParts("Manufacturing", "");
//			System.out.println("结束查询M物料：" + new Date());
//		} catch (Exception e) {
////			System.out.println("获取全部物料报错："+e.toString());
//			e.printStackTrace();
//		}
//		if (partDesignvector != null) {
//			System.out.println("partDesignvector size====" + partDesignvector.size());
//		}
//		if (partManufacturvector != null) {
//			System.out.println("partManufacturvector size====" + partManufacturvector.size());
//		}
//
//		Vector AllPartvector = new Vector();
//		if (partDesignvector != null && partDesignvector.size() > 0) {
//			for (int i = 0; i < partDesignvector.size(); i++) {
//				WTPart part=(WTPart) partDesignvector.get(i);
//				String lifeCycleState = part.getLifeCycleState().toString();
//				if (lifeCycleState.equals(ERPConstants.RELEASED)||lifeCycleState.equals(ERPConstants.ARCHIVED)){
//					//已发布或者已归档
//					DPartvector.add(part);
//				}
//			}
//		}
//		if (partDesignvector!=null&&partDesignvector.size()>0){
//			System.out.println("D视图发送总数：" + DPartvector.size());
//			for (int i = 0; i <DPartvector.size() ; i++) {
//				WTPart part=(WTPart) DPartvector.get(i);
//				String partNumber1=part.getNumber();
//				//查D视图的物料是否有同一版本M视图物料，有则不发
//				try {
//					partDesignvector_B = getAllLatestWTParts("Manufacturing", partNumber1);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				if (partDesignvector_B!=null&&partDesignvector_B.size()>0){
//					WTPart dpart=(WTPart) DPartvector.get(i);
//					String dversion=dpart.getVersionIdentifier().getValue();
//					WTPart mpart=(WTPart) partDesignvector_B.get(0);
//					String mversion=mpart.getVersionIdentifier().getValue();
//					int result=dversion.compareTo(mversion);
//					if (result>0){
//						AllPartvector.add(dpart);
//					}
//				}else{
//					AllPartvector.add(DPartvector.get(i));
//				}
//			}
//		}
//
//
//		if (partManufacturvector != null && partManufacturvector.size() > 0) {
//			System.out.println("M视图发送总数：" + partManufacturvector.size());
//			for (int i = 0; i < partManufacturvector.size(); i++) {
//				WTPart part=(WTPart) partManufacturvector.get(i);
//				String lifeCycleState = part.getLifeCycleState().toString();
//				if (lifeCycleState.equals(ERPConstants.RELEASED)||lifeCycleState.equals(ERPConstants.ARCHIVED)){
//					//已发布或者已归档
//					AllPartvector.add(part);
//				}
//			}
//		}
//
////		//循环
////		if (partDesignvector != null && partDesignvector.size() > 0) {
////			System.out.println("开始查询D物料历史版本：" + new Date());
////			for (int i = 0; i < partDesignvector.size(); i++) {
////				Vector DHistoricalDatavector = getHistoricalData((WTPart) partDesignvector.get(i), "Design");
////				if (DHistoricalDatavector!=null&&DHistoricalDatavector.size()>0){
////					for (int j = 0; j < DHistoricalDatavector.size(); j++) {
////						AllPartvector.add(DHistoricalDatavector.get(j));
////					}
////				}
////			}
////			System.out.println("结束查询D物料历史版本：" + new Date());
////		}
////		if (partManufacturvector != null && partManufacturvector.size() > 0) {
////			System.out.println("开始查询M物料历史版本：" + new Date());
////			for (int i = 0; i < partManufacturvector.size(); i++) {
////				Vector MHistoricalDatavector = getHistoricalData((WTPart) partManufacturvector.get(i), "Manufacturing");
////				if (MHistoricalDatavector!=null&&MHistoricalDatavector.size()>0){
////					for (int j = 0; j < MHistoricalDatavector.size(); j++) {
////						AllPartvector.add(MHistoricalDatavector.get(j));
////					}
////				}
////			}
////			System.out.println("结束查询M物料历史版本：" + new Date());
////		}
//		//发送总数
//		int partCount = 0;
//		if (AllPartvector.size() > 0) {
//			System.out.println("发送BOM总数：" + AllPartvector.size());
//			partCount = AllPartvector.size();
//		}
//
//		if (partCount > 0) {
//			count = String.valueOf(partCount);
//
//			for (int i = 0; i < partCount; i++) {
//				SendMessage sendMessage = new SendMessage();
//
//				WTPart part = (WTPart) AllPartvector.get(i);
//
//				System.out.println("part number=====：" + part.getNumber());
//				System.out.println("开始发送单个物料BOM：" + new Date());
//				WTKeyedHashMap map = BomReleaseService.sendBOM(part);
//				System.out.println("结束发送单个物料BOM：" + new Date());
//				partNumber = part.getNumber();
//				version = part.getVersionInfo().getIdentifier().getValue() + "." + part.getIterationInfo().getIdentifier().getValue();//物料版本
//				message = map.toString();
//				if (map != null && map.size() > 0) {
//					isSuccess = "失败";
//				} else {
//					isSuccess = "成功";
//					message = "BOM发送成功";
//				}
//				sendMessage.setPartNumber(partNumber);//物料编码
//				sendMessage.setVersion(version);//物料版本
//				sendMessage.setIsSuccess(isSuccess);//标志
//				sendMessage.setMessage(message);//详情
//
//				dataList.add(sendMessage);
//			}
//			if (dataList != null && dataList.size() > 0) {
//				System.out.println("发送的dataList:" + dataList.size());
//			}
//		}
//
//		int size=dataList.size()/20000+1;
//
//		String date=(new  Date()).toString();
//		String filename="SendAllBOM_"+date;
//		if (dataList.size()<20000) {
////			System.out.println("size<20000 进入ExportAllItems方法：" + new Date());
//			ExportAllItems exportAllItems = new ExportAllItems(dataList);
//			StringBuilder sb = new StringBuilder(filename);//构造一个StringBuilder对象
//			sb.insert(10, 1);//在指定的位置1，插入指定的字符串
//			String filename1 = sb.toString();
////			System.out.println("插入后字符串："+filename1);
//			exportAllItems.doExport(dataList, count, filename1);
//		}else if (dataList.size()>=20000){
//			for (int i=0;i<size;i++){
//				if (i==size-1){
//					Vector dlist=new Vector();
////					System.out.println("最后一个from:"+i * 20000);
////					System.out.println("最后一个to:"+dataList.size());
//					List list1 = dataList.subList(i * 20000, dataList.size());//最后一个不包括
//					list1.add(dataList.get(dataList.size()-1));
//					dlist.addAll(list1);
////					System.out.println("进入最后ExportAllItems方法："+new Date());
//					ExportAllItems exportAllItems = new ExportAllItems(dlist);
//					StringBuilder sb = new StringBuilder(filename);//构造一个StringBuilder对象
//					sb.insert(10, i+1);//在指定的位置1，插入指定的字符串
//					String filename1 = sb.toString();
////					System.out.println("插入后字符串："+filename1);
//					exportAllItems.doExport(dlist, count,filename1);
//				}else{
//					Vector dlist=new Vector();
//					System.out.println("from:"+i  * 20000);
//					System.out.println("to:"+(i + 1) * 20000);
//					List list1 = dataList.subList(i * 20000, (i + 1) * 20000);
//					System.out.println("list1 size:"+list1.size());
//					dlist.addAll(list1);
////					System.out.println("进入ExportAllItems方法："+new Date());
//					ExportAllItems exportAllItems = new ExportAllItems(dlist);
//					StringBuilder sb = new StringBuilder(filename);//构造一个StringBuilder对象
//					sb.insert(10, i+1);//在指定的位置1，插入指定的字符串
//					String filename1 = sb.toString();
////					System.out.println("插入后字符串："+filename1);
//					exportAllItems.doExport(dlist, count,filename1);
//				}
//			}
//		}
//
//		System.out.println("完成ExportAllItems方法："+new Date());
//		formresult.addFeedbackMessage(new FeedbackMessage(
//				FeedbackType.SUCCESS, null, "已发送全部BOM", null, new String[]{}));
//		//刷新页面
//		formresult.setNextAction(FormResultAction.REFRESH_CURRENT_PAGE);
//		System.out.println("结束发送全部BOM方法："+new Date());
//		return formresult;
//	}
//
//
//	public static  boolean isBOM(WTPart part) throws PIException {
//		List<List<Object>> fidList=new ArrayList<>();
//		String bomVersion=part.getNumber()+part.getVersionIdentifier().getValue();//子项BOM版本
//		try {
//			fidList = queryBOMFid("1", bomVersion);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		if (fidList != null && fidList.size() > 0) {
//			//K3已存在BOM
//			return true;
//		}
//		//获得子件集合
//		Set<WTPart> childParts = BomUtil.getMonolayerPart(part);
//
//		if (childParts == null || childParts.size() <= 0) {
//			return true;
//		}else {
//			for (WTPart childPart : childParts) {
//
//					try {
//						String bomVersion2=childPart.getNumber()+childPart.getVersionIdentifier().getValue();//子项BOM版本
//						//获得子件集合
//						Set<WTPart> childParts2 = BomUtil.getMonolayerPart(childPart);
//						List<List<Object>> fidList2 = queryBOMFid("100146", bomVersion2);
//						if (fidList2 != null && fidList2.size() > 0) {
//							//K3已存在BOM
//							continue;
//						}else {
//							//K3没有BOM
//							if (childParts2==null&&childParts2.size()<0){
//								//PLM没BOM
//								continue;
//							}else{
//								isBOM(childPart);
//
//							}
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//
//			}
//			sendBOM(part);
//		}
//		return false;
//	}
//
//
//
//	public static Vector getAllLatestWTParts (String viewName, String number) throws Exception {
//		QuerySpec qs = new QuerySpec(WTPart.class);
//
//		View view = ViewHelper.service.getView(viewName);
//		SearchCondition sc = new SearchCondition(WTPart.class, "view.key.id", SearchCondition.EQUAL,
//				view.getPersistInfo().getObjectIdentifier().getId());
//		qs.appendWhere(sc);
//		if (number.trim().length() > 0) {
//			qs.appendAnd();
//			SearchCondition scNumber = new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL,
//					number.toUpperCase());
//			qs.appendWhere(scNumber);
//		}
//
//		SearchCondition scLatestIteration = new SearchCondition(WTPart.class, WTAttributeNameIfc.LATEST_ITERATION,
//				SearchCondition.IS_TRUE);
//		qs.appendAnd();
//		qs.appendWhere(scLatestIteration);
//
//		QueryResult qr = PersistenceHelper.manager.find(qs);
//		if (qr != null && qr.hasMoreElements())
//			qr = (new LatestConfigSpec()).process(qr);
//
//		if (qr != null && qr.hasMoreElements())
//			return qr.getObjectVectorIfc().getVector();
//
//		return new Vector();
//	}
//
//	//历史版本
//	//bom在plm系统中是发布状态，则发送最新发布版本的bom
//	//bom在plm系统是归档状态，则发送所有归档版本的bom
//	//bom在plm系统是归档状态，历史版本存在发布状态的，则发送发布后的所有归档版本的bom及发送最后一个发布版本
//	public static Vector getHistoricalData (WTPart part, String view){
//		Vector resultVector = new Vector();
//		Vector filterVector=new Vector();
//		QueryResult qrVersions = null;
//		List list = new ArrayList();
//		Vector result = new Vector();
//		try {
//			qrVersions = VersionControlHelper.service.allVersionsOf(part.getMaster());
////			System.out.println("qrVersions size"+qrVersions.size());
//			while (qrVersions.hasMoreElements()) {
//				WTPart t = (WTPart) qrVersions.nextElement();
////				String version = t.getVersionInfo().getIdentifier().getValue() + "." + t.getIterationInfo().getIdentifier().getValue();//物料版本
////				System.out.println("version=========:"+version+";version===="+t.getViewName());
//				//同一视图
//				if (t.getViewName().equals(view)) {
//					resultVector.add(t);
//				}
//			}
//			System.out.println("resultVector size"+resultVector.size());
//			//查询结果是降序排序，D,C,B,A
//			//ERPConstants.RELEASED,已发布;ERPConstants.ARCHIVED已归档
//			//bom在plm系统中是发布状态，则发送最新发布版本的bom
//			//bom在plm系统是归档状态，则发送所有归档版本的bom
//			//bom在plm系统是归档状态，历史版本存在发布状态的，则发送发布后的所有归档版本的bom及发送最后一个发布版本
//
//			if (resultVector.size()>0){
//				//D,C,B,A
//				for (int i = 0; i <resultVector.size() ; i++) {
//					WTPart wpart = (WTPart) resultVector.get(i);
//					//物料状态
//					String lifeCycleState = wpart.getLifeCycleState().toString();
//					if (lifeCycleState.equals(ERPConstants.RELEASED)){
//						//已发布
//						filterVector.add(wpart);
////						break;
//					}else if(lifeCycleState.equals(ERPConstants.ARCHIVED)){
//						//已归档
//						filterVector.add(wpart);
//					}
//
//				}
//			}
////			System.out.println("比较后resultVector size"+resultVector.size());
//			//排序 A,B,C,D（原为D,C,B,A）
//			//排序版本
//			if (filterVector != null && filterVector.size() > 1) {
//				for (int i = 0; i < filterVector.size(); i++) {
//					WTPart wpart = (WTPart) filterVector.get(i);
//					String version = wpart.getVersionInfo().getIdentifier().getValue() + "." + wpart.getIterationInfo().getIdentifier().getValue();//物料版本
////					System.out.println("版本："+version);
//					list.add(version);
//				}
//				Collections.sort(list);
//
//				//与物料比较版本，A,B,C,D
//				for (int y = 0; y < list.size(); y++) {
//					for (int z = 0; z < filterVector.size(); z++) {
//						WTPart wpart = (WTPart) filterVector.get(z);
//						String version = wpart.getVersionInfo().getIdentifier().getValue() + "." + wpart.getIterationInfo().getIdentifier().getValue();//物料版本
//						if (list.get(y).equals(version)) {
//							result.add(wpart);
//						}
//					}
//
//				}
//			} else if (filterVector.size() == 1) {
//				result.add(filterVector.get(0));
//			}
//
//
//		} catch (WTException e) {
//			e.printStackTrace();
//		}
//		return result;
//	}

}