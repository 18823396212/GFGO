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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import ext.appo.common.util.Excel2003Handler;
import ext.appo.erp.constants.ERPConstants;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.generic.integration.erp.common.CommonUtil;
import org.apache.log4j.Logger;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.forms.FormResultAction;
import com.ptc.core.components.util.FeedbackMessage;
import com.ptc.core.ui.resources.FeedbackType;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.collections.WTKeyedHashMap;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;
import wt.vc.views.View;
import wt.vc.views.ViewHelper;

/**
 * 手工发布物料到ERP系统集成的中间表
 *
 * @author Kwang
 * @Version 3.0
 * @Date 2014-12-02
 */
public class ErpManuallyPublishPartProcessor extends DefaultObjectFormProcessor {

	private static String WT_CODEBASE = "";

	static {
		WTProperties wtproperties;
		try {
			wtproperties = WTProperties.getLocalProperties();
			WT_CODEBASE = wtproperties.getProperty("wt.codebase.location");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static final String CLASSNAME = ErpManuallyPublishPartProcessor.class.getName() ;

	private static final Logger logger = LogR.getLogger(CLASSNAME);

	public FormResult doOperation(NmCommandBean nmcommandbean, List list) throws WTException {
		return super.doOperation( nmcommandbean, list);
	}

	public FormResult preProcess(NmCommandBean nmcommandbean, List list) throws WTException {
		logger.debug("=====> 开始执行手工发布物料到ERP系统");
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
			WTKeyedHashMap hashMap =new WTKeyedHashMap();
			//区别光峰61，70,71服务器，绎立服务器，光峰采用发K3方法，其余采用发中间表方法
			if (ip.equals("172.32.252.61")||ip.equals("172.32.252.70")||ip.equals("172.32.252.71")){
				System.out.println("手动发物料调用K3接口=="+wtpart.getNumber());
				//发K3
				hashMap =ext.appo.erp.service.PartReleaseService.sendMaterial(wtpart);
			}else{
				System.out.println("手动发物料调用中间表接口=="+wtpart.getNumber());
				String releaseTime = CommonUtil.getCurrentTime() ;
				// 发布物料信息
				hashMap = ext.generic.integration.erp.service.PartReleaseService.release( wtpart , releaseTime , CommonPDMUtil.getBatchNumber(null));
			}
			if( hashMap != null && hashMap.size() > 0 ){
				formresult.addFeedbackMessage( new  FeedbackMessage(
						FeedbackType.FAILURE, null, "物料发布异常", null, new String[] {(String) hashMap.get(wtpart)} ) );

				logger.error("手工发布物料异常：" + wtpart.getDisplayIdentity() + "...异常：" + hashMap);
			}else{
				formresult.addFeedbackMessage( new  FeedbackMessage(
						FeedbackType.SUCCESS, null, "物料发布成功", null, new String[] {} ) );
			}
		}

		//刷新页面
		formresult.setNextAction(FormResultAction.REFRESH_CURRENT_PAGE);

		logger.debug("=====> 结束手工发布物料到ERP系统");
		return formresult;
	}

	//public class ErpManuallyPublishPartProcessor extends DefaultObjectFormProcessor {
//
//	private static String WT_CODEBASE = "";
//
//	static {
//		WTProperties wtproperties;
//		try {
//			wtproperties = WTProperties.getLocalProperties();
//			WT_CODEBASE = wtproperties.getProperty("wt.codebase.location");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//
//	private static final String CLASSNAME = ErpManuallyPublishPartProcessor.class.getName();
//
//	private static final Logger logger = LogR.getLogger(CLASSNAME);
//
//	public ErpManuallyPublishPartProcessor() {
//
//	}
//
//	public FormResult doOperation(NmCommandBean nmcommandbean, List list) throws WTException {
//		return super.doOperation(nmcommandbean, list);
//	}
//
//	// 发送全部物料
//	//1.所有历史版本都传过去
//	//2.只发 已归档，已发布，作废
//	public FormResult preProcess(NmCommandBean nmcommandbean, List list) throws WTException {

//		WTKeyedHashMap hashMap=new WTKeyedHashMap();
//		String partNumber1 = "";//物料编码
//		String isSuccess1 = "";//标志
//		String message1 = "";//详情
//
//		List list2=new ArrayList();
//		Vector dataList1=new Vector();
//		String date1=new Date().toString();
//		String count1="0";
//		String type1="BOMupdate_" + date1;
//
//		try {
//			list2=getChangebomTable();
//			System.out.println("获取到的表父件编码数量："+list2.size());
//			for (int i = 0; i <list2.size() ; i++) {
//				System.out.println("list1"+i+"==="+list2.get(i));
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		if (list2!=null&&list2.size()>1){
//			//去除重复元素
//			list2=removeDuplicate(list2);
//		}
//		System.out.println("去重复后list数量："+list2.size());
//		for (int i = 0; i <list2.size() ; i++) {
//			System.out.println("去重复后list1"+i+"==="+list2.get(i));
//		}
//
//		for (int i = 0; i <list2.size() ; i++) {
//			System.out.println("获取前:"+list2.get(i));
//			//通过物料编码获取物料
//			WTPart part= ext.appo.util.PartUtil.getLastestWTPartByNumber( String.valueOf(list2.get(i)));
//			System.out.println("获取到的part==="+part+";获取到的partNumber==="+part.getNumber());
//			partNumber1=part.getNumber();
//
//			//将更新的BOM发到中间表
//			// 发布BOM结构
//			String releaseTime = CommonUtil.getCurrentTime() ;
//			System.out.println("获取到的releaseTime==="+releaseTime);
//			hashMap = BOMReleaseService.releaseSingleLevel(part, releaseTime, null, CommonPDMUtil.getBatchNumber(null));
//			System.out.println("获取到的hashMap==="+hashMap);
//			SendMessage sendMessage = new SendMessage();
//			message1 = hashMap.toString();
//			if( hashMap!= null&&hashMap.size() > 0 ){
//				isSuccess1 = "失败";
//			} else {
//				isSuccess1 = "成功";
//				message1 = "BOM发送成功";
//			}
//			sendMessage.setPartNumber(partNumber1);//物料编码
//			sendMessage.setIsSuccess(isSuccess1);//标志
//			sendMessage.setMessage(message1);//详情
//			dataList1.add(sendMessage);
//		}
//
//		if (dataList1.size() > 0){
//			count1=String.valueOf(dataList1.size());
//		}
//
//		ExportAllItems exportAllItems2 = new ExportAllItems(dataList1);
//		exportAllItems2.doExport(dataList1,count1,type1);
//		FormResult formresult = new FormResult();
//
//		formresult.setStatus(FormProcessingStatus.SUCCESS);
//		formresult.addFeedbackMessage(new FeedbackMessage(
//				FeedbackType.SUCCESS, null, "已发送更新的BOM到中间表", null, new String[]{}));
//		//刷新页面
//		formresult.setNextAction(FormResultAction.REFRESH_CURRENT_PAGE);
//		System.out.println("结束发送更新的BOM到中间表："+new Date());
////		logger.debug("=====> 结束手工发布物料到ERP系统");
//		return formresult;



	//		System.out.println("进入发送全部物料方法："+new Date());
//		FormResult formresult = new FormResult();
//		formresult.setStatus(FormProcessingStatus.SUCCESS);
//
//		String partNumber = "";//物料编码
//		String version = "";//物料版本
//		String isSuccess = "";//标志
//		String message = "";//详情
//
//		String count = "";//发送总数
//		String type="物料发送异常信息";//发送类型
//
//		Vector dataList = new Vector();
//		WTKeyedHashMap map = new WTKeyedHashMap();
//
//		Vector partDesignvector = new Vector();
//		Vector partManufacturvector=new Vector();
//		try {
//			//只发M视图
//			System.out.println("开始查询D物料："+new Date());
//			partDesignvector = getAllLatestWTParts("Design", "");
//			System.out.println("结束查询D物料："+new Date());
//			System.out.println("开始查询M物料："+new Date());
//			partManufacturvector = getAllLatestWTParts("Manufacturing", "");
//			System.out.println("结束查询M物料："+new Date());
//		} catch (Exception e) {
////			System.out.println("获取全部物料报错："+e.toString());
//			e.printStackTrace();
//		}
//		if (partDesignvector!=null){
//			System.out.println("partDesignvector size====" + partDesignvector.size());
//		}
//		if(partManufacturvector!=null){
//			System.out.println("partManufacturvector size====" + partManufacturvector.size());
//		}
//
//		Vector AllPartvector = new Vector();
//		//循环
//		if (partDesignvector!=null&&partDesignvector.size() > 0) {
//			System.out.println("开始查询D物料历史版本："+new Date());
//			for (int i = 0; i < partDesignvector.size(); i++) {
//				Vector DHistoricalDatavector = getHistoricalData((WTPart) partDesignvector.get(i),"Design");
//				if (DHistoricalDatavector!=null&&DHistoricalDatavector.size()>0){
//					for (int j = 0; j <DHistoricalDatavector.size() ; j++) {
//						AllPartvector.add(DHistoricalDatavector.get(j));
//
//					}
//
//				}
//			}
//			System.out.println("结束查询D物料历史版本："+new Date());
//		}
//
//		if (partManufacturvector!=null&&partManufacturvector.size() > 0) {
//			System.out.println("开始查询M物料历史版本："+new Date());
//			for (int i = 0; i < partManufacturvector.size(); i++) {
//				Vector MHistoricalDatavector = getHistoricalData((WTPart) partManufacturvector.get(i),"Manufacturing");
//				if (MHistoricalDatavector!=null&&MHistoricalDatavector.size()>0){
//					for (int j = 0; j <MHistoricalDatavector.size() ; j++) {
//						AllPartvector.add(MHistoricalDatavector.get(j));
//					}
//				}
//			}
//			System.out.println("结束查询M物料历史版本："+new Date());
//		}
//
//		//发送总数
//		int partCount = 0;
//		if (AllPartvector.size() > 0) {
//			System.out.println("发送物料总数：" + AllPartvector.size());
//			partCount = AllPartvector.size();
//		}
//
//		if (partCount> 0) {
//			count = String.valueOf(partCount);
//			for (int i = 0; i <partCount; i++) {
//				SendMessage sendMessage = new SendMessage();
//
//				WTPart part = (WTPart) AllPartvector.get(i);
////				System.out.println("part number=====："+part.getNumber());
//				System.out.println("开始发送单个物料："+new Date());
//				map = sendMaterial(part);
//				System.out.println("结束发送单个物料："+new Date());
//				partNumber = part.getNumber();
//				version = part.getVersionInfo().getIdentifier().getValue() + "." + part.getIterationInfo().getIdentifier().getValue();//物料版本
//				message = map.toString();
//
//				if (map != null && map.size() > 0) {
//					isSuccess = "失败";
//				} else {
//					isSuccess = "成功";
//					message = "物料发送成功";
//				}
//				sendMessage.setPartNumber(partNumber);//物料编码
//				sendMessage.setVersion(version);//物料版本
//				sendMessage.setIsSuccess(isSuccess);//标志
//				sendMessage.setMessage(message);//详情
//				String lifeCycleState = part.getLifeCycleState().toString();
//				System.out.println("物料编码==="+partNumber+"物料版本==="+version+"标志==="+isSuccess+"物料状态==="+lifeCycleState);
//				dataList.add(sendMessage);
//			}
//			if (dataList!=null&&dataList.size()>0){
//				System.out.println("发送的dataList:" + dataList.size());
//			}
//		}
//
//		int size=dataList.size()/20000+1;
//
//		String date=(new  Date()).toString();
//		String filename="SendAllPart_"+date;
//		if (dataList.size()<20000) {
////			System.out.println("size<20000 进入ExportAllItems方法：" + new Date());
//			ExportAllItems exportAllItems = new ExportAllItems(dataList);
//			StringBuilder sb = new StringBuilder(filename);//构造一个StringBuilder对象
//			sb.insert(11, 1);//在指定的位置1，插入指定的字符串
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
//					sb.insert(11, i+1);//在指定的位置1，插入指定的字符串
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
//					sb.insert(11, i+1);//在指定的位置1，插入指定的字符串
//					String filename1 = sb.toString();
////					System.out.println("插入后字符串："+filename1);
//					exportAllItems.doExport(dlist, count,filename1);
//				}
//			}
//		}
//
//		System.out.println("完成ExportAllItems方法："+new Date());
//
//		formresult.addFeedbackMessage(new FeedbackMessage(
//				FeedbackType.SUCCESS, null, "已发送全部物料", null, new String[]{}));
//		//刷新页面
//		formresult.setNextAction(FormResultAction.REFRESH_CURRENT_PAGE);
//		System.out.println("结束发送全部物料方法："+new Date());
//		return formresult;
//
//	}
//
///**
// * 手工发布物料到K3
// *
// * @author Kwang
// * @Version 3.0
// * @Date 2014-12-02
// */
//public class ErpManuallyPublishPartProcessor extends DefaultObjectFormProcessor {
//
//	private static final String CLASSNAME = ErpManuallyPublishPartProcessor.class.getName() ;
//
//	private static final Logger logger = LogR.getLogger(CLASSNAME);
//
//	public ErpManuallyPublishPartProcessor() {
//
//	}
//
//	public FormResult doOperation(NmCommandBean nmcommandbean, List list) throws WTException {
//		return super.doOperation( nmcommandbean, list);
//	}
//
//	public FormResult preProcess(NmCommandBean nmcommandbean, List list) throws WTException {
//		logger.debug("=====> 开始执行手工发布物料到ERP系统");
//
//		FormResult formresult = new FormResult();
//		formresult.setStatus(FormProcessingStatus.SUCCESS);
//
//		NmOid nmoid = ((ObjectBean) list.get(0)).getActionOid();
//		WTPart wtpart = (WTPart) nmoid.getRef();
//
//		System.out.println("进入手工发布物料方法");
//		System.out.println("手工发布物料:"+wtpart);
//		//执行手工发布
//		if( wtpart != null ) {
//			String releaseTime = CommonUtil.getCurrentTime();
//
//			// 发布物料信息
//			WTKeyedHashMap hashMap=new WTKeyedHashMap();
//			hashMap.putAll(sendMaterial(wtpart));
//			System.out.println("手工发布物料返回结果："+hashMap);
//
//		 //发布物料信息
//			WTKeyedHashMap hashMap2 = ext.generic.integration.erp.service.PartReleaseService.release( wtpart , releaseTime , CommonPDMUtil.getBatchNumber(null));
//			System.out.println("U8信息："+hashMap2);
//
//			String kerror="物料发布异常";
//			String uerror="物料发布异常";
//
//			hashMap.putAll(hashMap2);
//
//			if( hashMap != null && hashMap.size() > 0 ){
////				if (hashMap2 != null && hashMap2.size() > 0 ){
////					formresult.addFeedbackMessage( new  FeedbackMessage(
////							FeedbackType.FAILURE, null, kerror+";"+uerror, null, new String[] {(String) hashMap.get(wtpart)} ) );
////
////				}
//				formresult.addFeedbackMessage( new  FeedbackMessage(
//						FeedbackType.FAILURE, null, "物料发布异常", null, new String[] {(String) hashMap.get(wtpart)} ) );
//
//				logger.error("手工发布物料异常：" + wtpart.getDisplayIdentity() + "...异常：" + hashMap);
//			}else{
//				formresult.addFeedbackMessage( new  FeedbackMessage(
//						FeedbackType.SUCCESS, null, "物料发布成功", null, new String[] {} ) );
//			}
//		}
//
//		//刷新页面
//		formresult.setNextAction(FormResultAction.REFRESH_CURRENT_PAGE);
//
//		logger.debug("=====> 结束手工发布物料到ERP系统");
//		return formresult;
//	}
//
//
	public static Vector getAllLatestWTParts(String viewName, String number) throws Exception {
		QuerySpec qs = new QuerySpec(WTPart.class);

		View view = ViewHelper.service.getView(viewName);
		SearchCondition sc = new SearchCondition(WTPart.class, "view.key.id", SearchCondition.EQUAL,
				view.getPersistInfo().getObjectIdentifier().getId());
		qs.appendWhere(sc);
		if (number.trim().length() > 0) {
			qs.appendAnd();
			SearchCondition scNumber = new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL,
					number.toUpperCase());
			qs.appendWhere(scNumber);
		}

		SearchCondition scLatestIteration = new SearchCondition(WTPart.class, WTAttributeNameIfc.LATEST_ITERATION,
				SearchCondition.IS_TRUE);
		qs.appendAnd();
		qs.appendWhere(scLatestIteration);

		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr != null && qr.hasMoreElements())
			qr = (new LatestConfigSpec()).process(qr);

		if (qr != null && qr.hasMoreElements())
			return qr.getObjectVectorIfc().getVector();

		return new Vector();
	}
	//历史版本
    public static Vector getHistoricalData(WTPart part,String view){
        Vector resultVector = new Vector();
		QueryResult qrVersions = null;
		List list=new ArrayList();
		Vector result=new Vector();
		try {
			qrVersions = VersionControlHelper.service.allVersionsOf(part.getMaster());
//			System.out.println("qrVersions size"+qrVersions.size());
			while (qrVersions.hasMoreElements()) {
				WTPart t = (WTPart) qrVersions.nextElement();
				String version= t.getVersionInfo().getIdentifier().getValue() + "." + t.getIterationInfo().getIdentifier().getValue();//物料版本
//				System.out.println("version=========:"+version+";version===="+t.getViewName());
//				String wlfl = getIBAvalue(part,"Classification");//物料分类appo_lsbm
////				System.out.println("批量发物料分类:"+wlfl);
				String mversion= t.getVersionInfo().getIdentifier().getValue();
				String lifeCycleState = t.getLifeCycleState().toString();
				if (t.getViewName().equals(view)&&lifeCycleState.equals(ERPConstants.INWORK)&&mversion.equals("A")){
					resultVector.add(t);
				}
			}
//			System.out.println("resultVector size"+resultVector.size());
			//排序 A,B,C,D（原为D,C,B,A）
			//排序版本

//				WTPart part1= (WTPart) resultVector.get(0);
//				String lifeCycleState = part1.getLifeCycleState().toString();
//				//已发布//已归档//废弃
//				if (!lifeCycleState.equals(ERPConstants.RELEASED)&&!lifeCycleState.equals(ERPConstants.ARCHIVED)&&!lifeCycleState.equals(ERPConstants.OBSOLESCENCE)){
//					resultVector.remove(0);
//				}
//				if (resultVector!=null&&resultVector.size()>1){
//					for (int i = 0; i <resultVector.size() ; i++) {
//					WTPart wpart=(WTPart) resultVector.get(i);
//					String version= wpart.getVersionInfo().getIdentifier().getValue() + "." + wpart.getIterationInfo().getIdentifier().getValue();//物料版本
//					//System.out.println("版本："+version);
//					list.add(version);
//				}
//				Collections.sort(list);
//
//				//与物料比较版本，A,B,C,D
//				for (int y = 0; y <list.size(); y++) {
//					for (int z = 0; z <resultVector.size() ; z++) {
//						WTPart wpart=(WTPart) resultVector.get(z);
//						String version= wpart.getVersionInfo().getIdentifier().getValue() + "." + wpart.getIterationInfo().getIdentifier().getValue();//物料版本
//						if (list.get(y).equals(version)){
//							result.add(wpart);
//						}
//					}
//
//				}
//
//			}else if (resultVector.size()==1){
//				result.add(resultVector.get(0));
//			}


		} catch (WTException e) {
			e.printStackTrace();
		}
        return resultVector;
    }

	//去除重复元素
	public static List removeDuplicate(List list) {
		for (int i = 0; i < list.size() - 1; i++) {

			for (int j = list.size() - 1; j > i; j--) {

				if (list.get(j).equals(list.get(i))) {

					list.remove(j);
				}
			}
		}
		return  list;
	}

	/*****
	 * 读取changebom表
	 * @return
	 * @throws IOException
	 */
	public static List getChangebomTable() throws IOException {
		List list=new ArrayList();
		System.out.println("读取changebom.xls地址");
		String filePath = WT_CODEBASE + File.separator + "temp" +  File.separator + "changebom.xls";
		System.out.println("更新BOM地址："+filePath);
		Excel2003Handler handler = new Excel2003Handler(filePath);
		handler.switchCurrentSheet(0);
		int rowCount = handler.getSheetRowCount();
//    System.out.println("产品线读表rowCount:"+rowCount);
		for (int i = 1; i < rowCount; i++) {
			list.add(null2blank(handler.getStringValue(i, 1)));
		}
		return list;
	}

	public static String null2blank(Object obj){
		if(obj == null){
			return "";
		}else{
			String tmp = obj.toString();
			return tmp.trim();
		}
	}


}