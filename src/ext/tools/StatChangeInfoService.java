
/**
 * <pre>
 * 修改记录：01 
 * 修改日期：2019-08-30
 * 修  改   人：毛兵义
 * 关联活动：PLM-69  实现ECN的报表查询功能 ---方便用户查询库存，在制，在途等信息
 * 修改内容：初始化
 * </pre>
 */

package ext.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ext.appo.ecn.beans.QuantitativePriceBean;
import ext.appo.ecn.pdf.PdfBean;
import ext.appo.ecn.pdf.PdfUtil;
import ext.appo.ecn.util.AffectedItemsUtil;
import ext.appo.statReport.statChangeInfo.bean.ChangeInfoBean;
import wt.change2.ChangeHelper2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.httpgw.GatewayAuthenticator;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.util.WTException;

/**
 * 此类用于统计变更信息报表提供相关的方法
 */
public class StatChangeInfoService implements RemoteAccess {

	public static void main(String[] args) {

		System.out.println("功能：统计变更测试");
		System.out.println("示例：windchill ext.tools.StatChangeInfoService");

		try {

			RemoteMethodServer rms = RemoteMethodServer.getDefault();
			GatewayAuthenticator auth = new GatewayAuthenticator();
			auth.setRemoteUser("wcadmin");
			rms.setAuthenticator(auth);

			String partNumbers = "";
			if (args.length == 1)
				partNumbers = args[0];

			System.out.println("命令执行开发start");
			// Class[] argTypes = { String.class };
			// Object[] svrArgs = { partNumbers };
			//
			// String result = (String)
			// RemoteMethodServer.getDefault().invoke("execute",
			// ChangeChildPartState.class.getName(), null, argTypes, svrArgs);

			String result = execute(partNumbers);
			System.out.println("命令执行完成finish" + result);
			System.exit(0);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String execute(String partNumbers) throws Exception {
		String result = "success";
		List<ChangeInfoBean> list = getSearchResult("CN0000004059", "", "", "", "");
		System.out.println("list size==" + list.size());
		return result;
	}

	/**
	 * 根据查询条件，获取最后的查询结果
	 * 
	 * @param searchObj
	 * @param checkedStateStr
	 * @param fromTime
	 * @param toTime
	 */
	public static List<ChangeInfoBean> getSearchResult(String ecnNumberparam, String effectObjectNoparam,
			String projectNameparam, String fromTime, String toTime) {

		List<ChangeInfoBean> resultList = new ArrayList<ChangeInfoBean>();
		try {

			List<WTChangeOrder2> ecnList = getAllECN();

			System.out.println("ecnList size==" + ecnList.size());

			List<QuantitativePriceBean> qpbList = AffectedItemsUtil.queryAll();

			System.out.println("qpbList size==" + qpbList.size());
			HashMap<String, Integer> map = new HashMap<String, Integer>();

			for (WTChangeOrder2 changeOrder2 : ecnList) {
				int i = 1;
				if (ecnNumberparam != null && !"".equals(ecnNumberparam)) {
					if (!ecnNumberparam.equals(changeOrder2.getNumber())) {
						continue;
					}
				}
				// 获取更改通告中所有的受影响对象
				QueryResult ecaqr = ChangeHelper2.service.getChangeActivities(changeOrder2);

				System.out.println("ecaqr size==" + ecaqr.size());

				while (ecaqr.hasMoreElements()) {
					Object ecaobject = ecaqr.nextElement();
					if (ecaobject instanceof WTChangeActivity2) {
						WTChangeActivity2 eca = ((WTChangeActivity2) ecaobject);

						System.out.println("eca==" + eca.getNumber());
						// if
						// (eca.getState().toString().equalsIgnoreCase("OPEN"))
						// {
						// 查询ECA中所有产生对象
						QueryResult qr = ChangeHelper2.service.getChangeablesBefore(eca);

						System.out.println("qr size==" + qr.size());
						while (qr.hasMoreElements()) {
							Object object = (Object) qr.nextElement();

							if (object instanceof WTPart) {
								WTPart part = (WTPart) object;

								System.out.println("part==" + part.getNumber());

								// 查询在制，在途，库存
								// List list =
								// AffectedItemsUtil.queryAmount(part.getNumber(),
								// part.getVersionInfo().getIdentifier().getValue());
								QuantitativePriceBean quantitativePriceBean = getQPBean(qpbList, part.getNumber(),
										part.getVersionInfo().getIdentifier().getValue());

								if (quantitativePriceBean != null) {

									System.out.println("into==");
									// 在制
									// String zz = subZeroAndDot((String)
									// list.get(0));
									String zz = subZeroAndDot(quantitativePriceBean.getZznum());
									// 在途
									// String zt = subZeroAndDot((String)
									// list.get(1));
									String zt = subZeroAndDot(quantitativePriceBean.getZtnum());
									// 库存
									// String kc = subZeroAndDot((String)
									// list.get(2));
									String kc = subZeroAndDot(quantitativePriceBean.getKcnum());
									System.out.println("into==" + zz);
									System.out.println("into==" + zt);
									System.out.println("into==" + kc);
									if ("0".equals(zz) && "0".equals(zt) && "0".equals(kc)) {
									} else {
										ChangeInfoBean bean = new ChangeInfoBean();
										PdfBean pdfBean = new PdfBean(changeOrder2);
										String ecnCreator = pdfBean.getSendPersion();
										String ecnNumber = pdfBean.getECNumber();
										String ecnName = pdfBean.getECName();
										String changeReason = pdfBean.getChangeComment();
										String changeReasonDes = pdfBean.getComment();
										String productType = pdfBean.getChangeAtt("sscpx");
										if (projectNameparam != null && !"".equals(projectNameparam)) {
											if (!projectNameparam.equals(pdfBean.getChangeAtt("ssxm"))) {
												continue;
											}
										}
										String projectName = pdfBean.getChangeAtt("ssxm");
										// ECN创建者
										bean.setEcnCreator(ecnCreator);
										// ECN编号
										bean.setEcnNumber(ecnNumber);
										// ECN名称
										bean.setEcnName(ecnName);
										// 变更原因
										bean.setChangeReason(changeReason);
										// 变更原因说明
										bean.setChangeReasonDes(changeReasonDes);
										// 所属产品类别
										bean.setProductType(productType);
										// 所属项目
										bean.setProjectName(projectName);
										// 受影响对象状态
										String effectObjectState = part.getState().toString();
										bean.setEffectObjectState(effectObjectState);
										// 受影响对象编号
										if (effectObjectNoparam != null && !"".equals(effectObjectNoparam)) {
											if (!effectObjectNoparam.equals(part.getNumber())) {
												continue;
											}
										}
										String effectObjectNo = part.getNumber();
										bean.setEffectObjectNo(effectObjectNo);
										// 受影响对象名称
										String effectObjectName = part.getName();
										bean.setEffectObjectName(effectObjectName);
										// 受影响对象版本
										String effectObjectVesion = part.getVersionInfo().getIdentifier().getValue();
										bean.setEffectObjectVesion(effectObjectVesion);
										// 在制数量
										String inProcessQuantities = zz;
										bean.setInProcessQuantities(inProcessQuantities);
										// 在制处理措施
										Object articleDispose = PdfUtil.getIBAObjectValue(part, "ArticleDispose");
										String inTreatment = articleDispose == null ? "" : articleDispose.toString();
										String[] articleArr = inTreatment.split(";");
										String processingMeasures = articleArr[1];
										bean.setProcessingMeasures(processingMeasures);
										// 在途数量
										String onthewayQuantity = zt;
										bean.setOnthewayQuantity(onthewayQuantity);
										// 在途处理措施
										Object passageDispose = PdfUtil.getIBAObjectValue(part, "PassageDispose");
										String wayTreatment = passageDispose == null ? "" : passageDispose.toString();
										String[] passageArr = wayTreatment.split(";");
										String onthewayTreatmentMeasure = passageArr[1];
										bean.setOnthewayTreatmentMeasure(onthewayTreatmentMeasure);
										// 库存数量
										String stockQuantity = kc;
										bean.setStockQuantity(stockQuantity);
										// 库存处理措施
										String stockTreatmentMeasure = "";
										Object inventoryDispose = PdfUtil.getIBAObjectValue(part, "InventoryDispose");
										String stockTreatment = inventoryDispose == null ? ""
												: inventoryDispose.toString();
										String[] inventoryArr = stockTreatment.split(";");
										if (inventoryArr.length > 1)
											stockTreatmentMeasure = inventoryArr[1]; // 库存处理措施
										bean.setStockTreatmentMeasure(stockTreatmentMeasure);

										map.put(ecnNumber, i);
										i++;
										resultList.add(bean);
									}
								}
							}
						}
					}
				}

			}
			// 增加行数
			if (resultList != null && resultList.size() > 0) {
				for (ChangeInfoBean bean : resultList) {
					bean.setLine(map.get(bean.getEcnNumber()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return resultList;

	}

	private static QuantitativePriceBean getQPBean(List<QuantitativePriceBean> qpbList, String partnumber,
			String version) {
		QuantitativePriceBean result = null;
		System.out.println("getQPBean==" + partnumber + "  version==" + version);
		if (qpbList != null && qpbList.size() > 0) {
			for (QuantitativePriceBean bean : qpbList) {
				if (bean.getWtpartNumber().equals(partnumber)) {
					System.out.println("into-----" + bean.getVersionInfo());
				}
				if (partnumber.equals(bean.getWtpartNumber()) && version.equals(bean.getVersionInfo())) {
					System.out.println(
							"into2222-----" + bean.getZtnum() + "  " + bean.getZznum() + "  " + bean.getKcnum());
					return bean;
				}
			}
		}

		return result;
	}

	// 返回所有ecn
	public static List<WTChangeOrder2> getAllECN() throws WTException {

		List<WTChangeOrder2> resultList = new ArrayList<WTChangeOrder2>();
		QuerySpec qs = new QuerySpec(WTChangeOrder2.class);
		QueryResult qr = PersistenceHelper.manager.find(qs);

		while (qr.hasMoreElements()) {
			WTChangeOrder2 wtchangeorder2 = (WTChangeOrder2) qr.nextElement();

			resultList.add(wtchangeorder2);
		}

		return resultList;
	}

	/***
	 * 使用java正则表达式去掉多余的.与0*
	 * 
	 * @param str
	 * @return
	 */
	public static String subZeroAndDot(String str) {
		if (str != null && str.indexOf(".") > 0) {
			str = str.replaceAll("0+?$", "");// 去掉多余的0
			str = str.replaceAll("[.]$", "");// 如最后一位是.则去掉
		}
		return str;
	}

}
