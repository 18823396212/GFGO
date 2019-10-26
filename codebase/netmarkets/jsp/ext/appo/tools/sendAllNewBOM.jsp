<%@ page import="wt.org.WTPrincipal" %>
<%@ page import="wt.session.SessionHelper" %>
<%@ page import="ext.appo.test.tools.UpdateAttrsTools" %>
<%@ page import="wt.part.WTPart" %>
<%@ page import="ext.appo.erp.constants.ERPConstants" %>
<%@ page import="ext.appo.erp.bean.SendMessage" %>
<%@ page import="wt.fc.collections.WTKeyedHashMap" %>
<%@ page import="ext.appo.erp.service.BomReleaseService" %>
<%@ page import="static ext.appo.test.tools.SendAllBOMTools.getAllWTPartMaster" %>
<%@ page import="wt.enterprise.Master" %>
<%@ page import="wt.part.WTPartMaster" %>
<%@ page import="static ext.appo.test.tools.SendAllBOMTools.getBomHistoricalData" %>
<%@ page import="static ext.appo.test.tools.SendAllBOMTools.getAllLatestWTParts" %>
<%@ page import="java.util.*" %>
<%@ page import="ext.appo.erp.util.BomUtil" %>
<%@ page import="ext.appo.test.tools.ExportItems" %>
<%@ page import="ext.appo.util.PartUtil" %>

<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!--master查M视图最新已归档/已发布物料有BOM发BOM，没有则发D视图最新已归档/已发布的BOM-->
<%
	out.println("========send BOM start========");
	out.println("<br>");
	System.out.println("开始发送最新已归档或已发布BOM方法"+ new Date());
	WTPrincipal sessionUser = SessionHelper.manager.getPrincipal();
	SessionHelper.manager.setAdministrator();
	Vector AllPartvector = new Vector();
	Vector latestMVector= new Vector();
	Vector HistoricalMVector=new Vector();
	Vector latestDVector=new Vector();
	Vector HistoricalDVector=new Vector();
	Vector masterVector=new Vector();
	//查询所有Master

//	WTPart childpart = PartUtil.getLastestWTPartByNumber("A15000000022");
//	String test=childpart.getContainerName();
//	out.println("产品库=="+test);
//	out.println(childpart.getNumber()+"产品库是否为测试产品库==="+test.equals("01测试产品库"));
	masterVector=getAllWTPartMaster();
//	masterVector.add(childpart.getMaster());
	System.out.println("mast的isBOMer数量=="+masterVector.size());

	for (int i = 0; i <masterVector.size() ; i++) {
//		System.out.println("((WTPartMaster) masterVector.get(i)).getNumber()是否在测试产品库中=="+((WTPartMaster) masterVector.get(i)).getContainerName().equals("01测试产品库"));
		if (!((WTPartMaster) masterVector.get(i)).getContainerName().equals("01测试产品库")){

			//查询M视图最新版本wtpart
			latestMVector = getAllLatestWTParts("Manufacturing", ((WTPartMaster) masterVector.get(i)).getNumber());
			if (latestMVector != null && latestMVector.size() > 0) {
				//有M视图
				WTPart part = (WTPart) latestMVector.get(0);
				//查询历史数据第一个已归档或已发布wtpart
				HistoricalMVector = getBomHistoricalData(part, "Manufacturing");
				if (HistoricalMVector != null && HistoricalMVector.size() > 0) {
					WTPart mpart =(WTPart)HistoricalMVector.get(0);
					//获得子件集合
					Set<WTPart> childParts = BomUtil.getMonolayerPart(mpart);
					if (childParts != null && childParts.size() > 0) {
						//有BOM
						AllPartvector.add(mpart);
					}else{
						//没有BOM，查D视图
						latestDVector = getAllLatestWTParts("Design", ((WTPartMaster) masterVector.get(i)).getNumber());
						if (latestDVector != null && latestDVector.size() > 0) {
							//有D视图
							WTPart wtpart = (WTPart) latestDVector.get(0);
							//查询历史数据第一个已归档或已发布wtpart
							HistoricalDVector = getBomHistoricalData(wtpart, "Design");
							if (HistoricalDVector != null && HistoricalDVector.size() > 0) {
								WTPart dpart = (WTPart) HistoricalDVector.get(0);
								//获得子件集合
								Set<WTPart> parts = BomUtil.getMonolayerPart(dpart);
								if (parts != null && parts.size() > 0) {
									//有BOM
									AllPartvector.add(dpart);
								}
							}
						}
					}
				}else {
					//M视图历史数据没有已发布或已归档，查D视图
					latestDVector = getAllLatestWTParts("Design", ((WTPartMaster) masterVector.get(i)).getNumber());
					if (latestDVector != null && latestDVector.size() > 0) {
						//有D视图
						WTPart wtpart = (WTPart) latestDVector.get(0);
						//查询历史数据第一个已归档或已发布wtpart
						HistoricalDVector = getBomHistoricalData(wtpart, "Design");
						if (HistoricalDVector != null && HistoricalDVector.size() > 0) {
							WTPart dpart = (WTPart) HistoricalDVector.get(0);
							//获得子件集合
							Set<WTPart> childParts = BomUtil.getMonolayerPart(dpart);
							if (childParts != null && childParts.size() > 0) {
								//有BOM
								AllPartvector.add(dpart);
							}
						}
					}
				}
			}else {
				//没有M视图,查D视图
				latestDVector = getAllLatestWTParts("Design", ((WTPartMaster) masterVector.get(i)).getNumber());
				if (latestDVector != null && latestDVector.size() > 0) {
					//有D视图
					WTPart wtpart = (WTPart) latestDVector.get(0);
					//查询历史数据第一个已归档或已发布wtpart
					HistoricalDVector = getBomHistoricalData(wtpart, "Design");
					if (HistoricalDVector != null && HistoricalDVector.size() > 0) {
						WTPart dpart = (WTPart) HistoricalDVector.get(0);
						//获得子件集合
						Set<WTPart> childParts = BomUtil.getMonolayerPart(dpart);
						if (childParts != null && childParts.size() > 0) {
							//有BOM
							AllPartvector.add(dpart);
						}
					}
				}
			}
		}
	}

	String partNumber = "";//物料编码
	String version = "";//物料版本
	String isSuccess = "";//标志
	String message = "";//详情
	String count = "";//发送总数
	String view="";//视图
	Vector dataList = new Vector();
	//发送总数
	int partCount = 0;
	if (AllPartvector.size() > 0) {
		System.out.println("发送BOM总数：" + AllPartvector.size());
		partCount = AllPartvector.size();
	}

	if (partCount > 0) {
		count = String.valueOf(partCount);

		for (int i = 0; i < partCount; i++) {
			SendMessage sendMessage = new SendMessage();

			WTPart part = (WTPart) AllPartvector.get(i);
			System.out.println("开始==总数为=="+partCount+"发送"+part.getNumber()+"第"+i+"get"+"单个物料BOM：" + new Date());
			WTKeyedHashMap map = BomReleaseService.sendGFBOM(part);
			System.out.println("结束发送"+part.getNumber()+"第"+i+"get"+"单个物料BOM：" + new Date());
			partNumber = part.getNumber();
			version = part.getVersionInfo().getIdentifier().getValue() + "." + part.getIterationInfo().getIdentifier().getValue();//物料版本
			view=part.getViewName();
			message = map.toString();
			if (map != null && map.size() > 0) {
				isSuccess = "失败";
			} else {
				isSuccess = "成功";
				message = "BOM发送成功";
			}
			sendMessage.setPartNumber(partNumber);//物料编码
			sendMessage.setVersion(version);//物料版本
			sendMessage.setIsSuccess(isSuccess);//标志
			sendMessage.setMessage(message);//详情
			sendMessage.setView(view);//视图

			dataList.add(sendMessage);
		}
		if (dataList != null && dataList.size() > 0) {
			System.out.println("发送的dataList:" + dataList.size());
		}
	}

	int size=dataList.size()/20000+1;

	String date=(new  Date()).toString();
	String filename="SendAllNewBOM_"+date;
	if (dataList.size()<20000) {
//			System.out.println("size<20000 进入ExportAllItems方法：" + new Date());
		ExportItems exportItems = new ExportItems(dataList);
		StringBuilder sb = new StringBuilder(filename);//构造一个StringBuilder对象
		sb.insert(13, 1);//在指定的位置1，插入指定的字符串
		String filename1 = sb.toString();
//			System.out.println("插入后字符串："+filename1);
		exportItems.doExport(dataList, count, filename1);
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
				ExportItems exportItems = new ExportItems(dlist);
				StringBuilder sb = new StringBuilder(filename);//构造一个StringBuilder对象
				sb.insert(13, i+1);//在指定的位置1，插入指定的字符串
				String filename1 = sb.toString();
//					System.out.println("插入后字符串："+filename1);
				exportItems.doExport(dlist, count,filename1);
			}else{
				Vector dlist=new Vector();
				System.out.println("from:"+i  * 20000);
				System.out.println("to:"+(i + 1) * 20000);
				List list1 = dataList.subList(i * 20000, (i + 1) * 20000);
				System.out.println("list1 size:"+list1.size());
				dlist.addAll(list1);
//					System.out.println("进入ExportAllItems方法："+new Date());
				ExportItems exportItems = new ExportItems(dlist);
				StringBuilder sb = new StringBuilder(filename);//构造一个StringBuilder对象
				sb.insert(13, i+1);//在指定的位置1，插入指定的字符串
				String filename1 = sb.toString();
//					System.out.println("插入后字符串："+filename1);
				exportItems.doExport(dlist, count,filename1);
			}
		}
	}
	SessionHelper.manager.setPrincipal(sessionUser.getName());

	System.out.println("完成发送最新已归档或已发布BOM方法"+ new Date());
	out.println("<br>");
	out.println("========send BOM end========");
%>