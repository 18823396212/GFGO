<%@ page import="wt.org.WTPrincipal" %>
<%@ page import="wt.session.SessionHelper" %>
<%@ page import="java.util.List" %>
<%@ page import="ext.appo.test.tools.UpdateAttrsTools" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Vector" %>
<%@ page import="wt.part.WTPart" %>
<%@ page import="ext.appo.erp.constants.ERPConstants" %>
<%@ page import="static ext.appo.doc.uploadDoc.UploadHistoricalData.getAllLatestWTParts" %>
<%@ page import="ext.appo.erp.bean.SendMessage" %>
<%@ page import="wt.fc.collections.WTKeyedHashMap" %>
<%@ page import="ext.appo.erp.service.BomReleaseService" %>
<%@ page import="ext.appo.erp.util.ExportAllItems" %>
<%@ page import="static ext.appo.test.tools.ReadPartExcel.ReadPartNumber" %>
<%@ page import="ext.appo.util.PartUtil" %>
<%@ page import="ext.appo.erp.service.PartReleaseService" %>

<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!--发送BOM，取最新版本，状态为已归档，已发布则发送，否则不发送，发D视图时判断是否有同一版本M视图物料，有则不发-->
<%
	out.println("========send Part start========");
	out.println("<br>");
	System.out.println("开始发送抓取到的物料");
	WTPrincipal sessionUser = SessionHelper.manager.getPrincipal();
	SessionHelper.manager.setAdministrator();

	Vector AllPartvector = new Vector();
	List list=ReadPartNumber();//读取物料表
	AllPartvector.addAll(list);
	if (AllPartvector!=null&&AllPartvector.size()>0){
		out.println("发送总数=="+AllPartvector.size());
		out.println("<br>");
	}

	String partNumber = "";//物料编码
	String version = "";//物料版本
	String isSuccess = "";//标志
	String message = "";//详情
	String count = "";//发送总数
	Vector dataList = new Vector();
	//发送总数
	int partCount = 0;
	if (AllPartvector.size() > 0) {
		System.out.println("发送物料总数：" + AllPartvector.size());
		partCount = AllPartvector.size();
	}

	if (partCount > 0) {
		count = String.valueOf(partCount);

		for (int i = 0; i < partCount; i++) {
			SendMessage sendMessage = new SendMessage();
			//获取最新版本物料
			WTPart part = PartUtil.getLastestWTPartByNumber(String.valueOf(AllPartvector.get(i)));
			System.out.println("物料=="+part.getNumber()+"===版本=="+part.getVersionInfo().getIdentifier().getValue() + "." + part.getIterationInfo().getIdentifier().getValue());
			System.out.println("开始发送单个物料：" + new Date());
			WTKeyedHashMap map = PartReleaseService.sendMaterial(part);
			System.out.println("结束发送单个物料：" + new Date());
			partNumber = part.getNumber();
			version = part.getVersionInfo().getIdentifier().getValue() + "." + part.getIterationInfo().getIdentifier().getValue();//物料版本
			message = map.toString();
			if (map != null && map.size() > 0) {
				isSuccess = "失败";
			} else {
				isSuccess = "成功";
				message = "Part发送成功";
			}
			sendMessage.setPartNumber(partNumber);//物料编码
			sendMessage.setVersion(version);//物料版本
			sendMessage.setIsSuccess(isSuccess);//标志
			sendMessage.setMessage(message);//详情

			dataList.add(sendMessage);
		}
		if (dataList != null && dataList.size() > 0) {
			System.out.println("发送的dataList:" + dataList.size());
		}
	}

	int size=dataList.size()/20000+1;

	String date=(new  Date()).toString();
	String filename="SendNewPart_"+date;
	if (dataList.size()<20000) {
//			System.out.println("size<20000 进入ExportAllItems方法：" + new Date());
		ExportAllItems exportAllItems = new ExportAllItems(dataList);
		StringBuilder sb = new StringBuilder(filename);//构造一个StringBuilder对象
		sb.insert(11, 1);//在指定的位置1，插入指定的字符串
		String filename1 = sb.toString();
//			System.out.println("插入后字符串："+filename1);
		exportAllItems.doExport(dataList, count, filename1);
	}else if (dataList.size()>=20000){
		for (int i=0;i<size;i++){
			if (i==size-1){
				Vector dlist=new Vector();
//					System.out.println("最后一个from:"+i * 20000);
//					System.out.println("最后一个to:"+dataList.size());
				List list1 = dataList.subList(i * 20000, dataList.size());//最后一个不包括
				list1.add(dataList.get(dataList.size()-1));
				dlist.addAll(list1);
//					System.out.println("进入最后ExportAllItems方法："+new Date());
				ExportAllItems exportAllItems = new ExportAllItems(dlist);
				StringBuilder sb = new StringBuilder(filename);//构造一个StringBuilder对象
				sb.insert(11, i+1);//在指定的位置1，插入指定的字符串
				String filename1 = sb.toString();
//					System.out.println("插入后字符串："+filename1);
				exportAllItems.doExport(dlist, count,filename1);
			}else{
				Vector dlist=new Vector();
				System.out.println("from:"+i  * 20000);
				System.out.println("to:"+(i + 1) * 20000);
				List list1 = dataList.subList(i * 20000, (i + 1) * 20000);
				System.out.println("list1 size:"+list1.size());
				dlist.addAll(list1);
//					System.out.println("进入ExportAllItems方法："+new Date());
				ExportAllItems exportAllItems = new ExportAllItems(dlist);
				StringBuilder sb = new StringBuilder(filename);//构造一个StringBuilder对象
				sb.insert(11, i+1);//在指定的位置1，插入指定的字符串
				String filename1 = sb.toString();
//					System.out.println("插入后字符串："+filename1);
				exportAllItems.doExport(dlist, count,filename1);
			}
		}
	}
	SessionHelper.manager.setPrincipal(sessionUser.getName());

	System.out.println("完成发送抓取到的物料");
	out.println("<br>");
	out.println("========send Part end========");
%>