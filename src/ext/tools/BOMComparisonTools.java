package ext.tools;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.workflow.NmWorkflowHelper;

import wt.change2.ChangeRecord2;
import wt.change2.WTChangeActivity2;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectReference;
import wt.fc.ObjectToObjectLink;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.collections.WTCollection;
import wt.httpgw.GatewayAuthenticator;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.occurrence.Occurrence;
import wt.occurrence.OccurrenceHelper;
import wt.part.Source;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartSubstituteLink;
import wt.part.WTPartUsageLink;
import wt.pds.StatementSpec;
import wt.pom.PersistenceException;
import wt.query.KeywordExpression;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;
import wt.vc.views.View;
import wt.vc.views.ViewHelper;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfState;

public class BOMComparisonTools implements RemoteAccess {

	private static final String ERROR_CHANGJING_0 = "K3不存在当前版本的BOM";
	private static final String ERROR_CHANGJING_1 = "子件缺失：PLM不存在当前子件";
	private static final String ERROR_CHANGJING_2 = "子件缺失：K3不存在当前子件";
	private static final String ERROR_CHANGJING_3 = "子件版本不一致";
	private static final String ERROR_CHANGJING_4 = "子件数量不一致";
	private static final String ERROR_CHANGJING_5 = "子件位号不一致";

	public static void main(String[] args) {

		System.out.println("功能：PLM跟K3BOM比较工具");
		System.out.println("示例：windchill ext.tools.BOMComparisonTools");

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

		if (InvokeK3Tools.LoginK3()) {
			System.out.println("login success");

			List<WTPartMaster> plmMasterList = getAllWTPartMaster();
			// 导出报表的数据
			List<List<String>> datalist = new ArrayList<List<String>>();

			for (WTPartMaster master : plmMasterList) {
				if (!master.getContainerName().equals("01测试产品库") && !master.getContainerName().equals("SL-舞台灯产品库")
						&& !master.getName().contains("test")
						&& (master.getNumber().startsWith("A") || master.getNumber().startsWith("B"))) {

					WTPart parentPart = getParentPart(master);
					if (parentPart != null) {
						// PLM的BOM结构
						List<WTPart> plmPartList = getMonolayerPart(parentPart);
						if (plmPartList == null || plmPartList.size() == 0) {
							continue;
						}
						if ("OBSOLESCENCE".equals(parentPart.getLifeCycleState().toString())) {
							continue;
						}

						String partNumber = parentPart.getNumber();
						String version = parentPart.getVersionInfo().getIdentifier().getValue();
						HashMap<String, List<List<String>>> resultMap = InvokeK3Tools
								.queryAllBOM(partNumber + "_" + version);
						// K3的BOM结构
						List<List<String>> k3PartList = resultMap.get(partNumber + "_" + version);
						// 删除K3重复的替代料信息
						k3PartList = deleteRepeatData(k3PartList);

						if (k3PartList != null && k3PartList.size() > 0) {
							for (List<String> list : k3PartList) {

								// K3单条子件数据
								String BOM = list.get(0);
								String SUBMATERIAL = list.get(1);
								String ITEMVER = list.get(2);
								String FNUMERATOR = subZeroAndDot(list.get(3));
								String FDENOMINATOR = subZeroAndDot(list.get(4));

								Double d_FNUMERATOR = Double.parseDouble(FNUMERATOR);
								Double d_FDENOMINATOR = Double.parseDouble(FDENOMINATOR);

								Double k3num = d_FNUMERATOR / d_FDENOMINATOR;
								String s_k3num = k3num.toString();

								String FPOSITIONNO = list.get(5);
								// PLM单条数据
								WTPart part = getPart(plmPartList, SUBMATERIAL);
								if (part == null) {
									List<String> errorList = new ArrayList<String>();
									errorList.add(BOM);
									errorList.add(SUBMATERIAL);
									errorList.add(ERROR_CHANGJING_1);
									errorList.add("");
									errorList.add(ITEMVER);
									errorList.add("");
									errorList.add(s_k3num);
									errorList.add("");
									errorList.add(FPOSITIONNO);
									errorList.add("");
									errorList.add("");
									datalist.add(errorList);
									continue;
								}
								String plmSum = "";
								String plmWeihao = "";
								WTPartUsageLink link = getUsageLink(parentPart, part.getMaster());
								if (link != null) {
									plmSum = getLinkSum(link);
									plmWeihao = getPartReferenceDesignators(link);
								}

								if (ITEMVER != null && ITEMVER.length() > 0
										&& !ITEMVER.equals(part.getVersionIdentifier().getValue())) {
									List<String> errorList = new ArrayList<String>();
									errorList.add(BOM);
									errorList.add(SUBMATERIAL);
									errorList.add(ERROR_CHANGJING_3);
									errorList.add(part.getVersionIdentifier().getValue());
									errorList.add(ITEMVER);
									errorList.add(plmSum);
									errorList.add(s_k3num);
									errorList.add(FPOSITIONNO);
									errorList.add(plmWeihao);
									errorList.add(parentPart.getLifeCycleState().toString());
									errorList.add(part.getLifeCycleState().toString());
									datalist.add(errorList);
								}
								boolean flag = false;
								// 暂时不处理数量情况
								if (!s_k3num.equals(plmSum) && flag) {
									List<String> errorList = new ArrayList<String>();
									errorList.add(BOM);
									errorList.add(SUBMATERIAL);
									errorList.add(ERROR_CHANGJING_4);
									errorList.add(part.getVersionIdentifier().getValue());
									errorList.add(ITEMVER);
									errorList.add(plmSum);
									errorList.add(s_k3num);
									errorList.add(FPOSITIONNO);
									errorList.add(getPartReferenceDesignators(link));
									errorList.add("");
									errorList.add("");
									datalist.add(errorList);
								}
								// 暂时不处理位号情况
								if (!FPOSITIONNO.equals(plmWeihao) && flag) {
									List<String> errorList = new ArrayList<String>();
									errorList.add(BOM);
									errorList.add(SUBMATERIAL);
									errorList.add(ERROR_CHANGJING_5);
									errorList.add(part.getVersionIdentifier().getValue());
									errorList.add(ITEMVER);
									errorList.add(plmSum);
									errorList.add(s_k3num);
									errorList.add(FPOSITIONNO);
									errorList.add(getPartReferenceDesignators(link));
									errorList.add("");
									errorList.add("");
									datalist.add(errorList);
								}
							}
							// 子料在PLM存在而在K3不存在情况
							for (WTPart part : plmPartList) {
								boolean ff = false;
								b: for (List<String> list : k3PartList) {
									if (part.getNumber().equals(list.get(1))) {
										ff = true;
										break b;
									}
								}
								if (!ff) {
									List<String> errorList = new ArrayList<String>();
									errorList.add(partNumber + "_" + version);
									errorList.add(part.getNumber());
									errorList.add(ERROR_CHANGJING_2);
									errorList.add("");
									errorList.add("");
									errorList.add("");
									errorList.add("");
									errorList.add("");
									errorList.add("");
									errorList.add(parentPart.getLifeCycleState().toString());
									errorList.add(part.getLifeCycleState().toString());

									String parentSourceCN = getSourceCN(parentPart);
									String childSourceCN = getSourceCN(part);
									if ("外购".equals(parentSourceCN)
											&& ("自制".equals(childSourceCN) || "外协".equals(childSourceCN))) {

									} else {
										datalist.add(errorList);
									}
								}
							}

						} else {
							List<String> errorList = new ArrayList<String>();
							errorList.add(partNumber + "_" + version);
							errorList.add("");
							errorList.add(ERROR_CHANGJING_0);
							errorList.add("");
							errorList.add("");
							errorList.add("");
							errorList.add("");
							errorList.add("");
							errorList.add("");
							errorList.add(parentPart.getLifeCycleState().toString());
							errorList.add("");
							datalist.add(errorList);
						}
					}

				}

			}

			ExportPartItems exportPartItems = new ExportPartItems(datalist);
			exportPartItems.doExport(datalist);
			// System.out.println(result);
		}

		return result;
	}

	public static List<List<String>> deleteRepeatData(List<List<String>> k3PartList) {

		List<List<String>> resultList = new ArrayList<List<String>>();

		if (k3PartList != null && k3PartList.size() > 0) {
			for (List<String> list : k3PartList) {

				String BOM = list.get(0);
				String SUBMATERIAL = list.get(1);
				String ITEMVER = list.get(2);
				boolean isadd = true;
				A: for (List<String> list2 : resultList) {
					if (BOM.equals(list2.get(0)) && SUBMATERIAL.equals(list2.get(1))) {
						String ITEMVER2 = list2.get(2);

						if (ITEMVER2 != null && ITEMVER2.contains("V")) {
							resultList.remove(list2);
						} else if (ITEMVER != null && ITEMVER.contains("V")) {
							isadd = false;
						} else if (ITEMVER.compareTo(ITEMVER2) > 0) {
							resultList.remove(list2);
						} else {
							isadd = false;
						}
						break A;
					}
				}
				if (isadd) {
					resultList.add(list);
				}
			}

		}

		return resultList;

	}

	// 返回所有Master
	public static List<WTPartMaster> getAllWTPartMaster() throws WTException {

		List<WTPartMaster> datalist = new ArrayList<WTPartMaster>();
		QuerySpec qs = new QuerySpec(WTPartMaster.class);

		QueryResult qr = PersistenceHelper.manager.find(qs);

		while (qr.hasMoreElements()) {
			WTPartMaster wtPartMaster = (WTPartMaster) qr.nextElement();

			datalist.add(wtPartMaster);
		}

		return datalist;
	}

	public static String subZeroAndDot(String str) {
		if (str != null && str.indexOf(".") > 0) {
			str = str.replaceAll("0+?$", "");// 去掉多余的0
			str = str.replaceAll("[.]$", "");// 如最后一位是.则去掉
		}
		return str;
	}

	// 通过父件个子件获得link
	public static WTPartUsageLink getUsageLink(WTPart parentWTPart, WTPartMaster childWTPartMaster) throws WTException {
		QuerySpec qs = new QuerySpec(WTPartUsageLink.class);
		int[] fromIndicies = { 0, wt.query.FromClause.NULL_INDEX };
		qs.appendWhere(
				new SearchCondition(WTPartUsageLink.class,
						ObjectToObjectLink.ROLE_AOBJECT_REF + "." + ObjectReference.KEY + "." + ObjectIdentifier.ID,
						SearchCondition.EQUAL, PersistenceHelper.getObjectIdentifier(parentWTPart).getId()),
				fromIndicies);
		qs.appendAnd();
		qs.appendWhere(
				new SearchCondition(WTPartUsageLink.class,
						ObjectToObjectLink.ROLE_BOBJECT_REF + "." + ObjectReference.KEY + "." + ObjectIdentifier.ID,
						SearchCondition.EQUAL, PersistenceHelper.getObjectIdentifier(childWTPartMaster).getId()),
				fromIndicies);

		QueryResult qr = PersistenceHelper.manager.find((wt.pds.StatementSpec) qs);
		while (qr.hasMoreElements()) {
			WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
			return link;
		}
		return null;
	}

	/**
	 * 获得位号
	 * 
	 * @param partUsageLink
	 * @return
	 * @throws WTException
	 */
	public static String getPartReferenceDesignators(WTPartUsageLink partUsageLink) throws WTException {
		String result = "";
		QueryResult qr = OccurrenceHelper.service.getUsesOccurrences(partUsageLink);
		int nOccurences = qr.size();
		ArrayList refDesignatorList = new ArrayList(nOccurences);

		while (qr.hasMoreElements()) {
			Occurrence occurrence = (Occurrence) qr.nextElement();
			String occurrenceName = occurrence.getName();
			if (occurrenceName != null) {
				refDesignatorList.add(occurrenceName);
			}
		}

		Collections.sort(refDesignatorList);
		if (!refDesignatorList.isEmpty()) {
			result = StringUtils.join(refDesignatorList, ",");
		}

		return result;
	}

	/**
	 * 获得数量
	 * 
	 * @param link
	 * @return
	 */
	public static String getLinkSum(WTPartUsageLink link) {
		if (link != null) {
			String str = new BigDecimal(link.getQuantity().getAmount() + "").toString();
			// System.out.println("数量中的str"+str);
			if (str.indexOf(".") > 0) {
				str = str.replaceAll("0+$", "").replaceAll("[.]$", "");
			}
			// System.out.println("数量修改的str"+str);
			return str;
		} else {
			return "";
		}
	}

	private static WTPart getPart(List<WTPart> plmPartList, String SUBMATERIAL) {

		WTPart result = null;

		for (WTPart part : plmPartList) {
			if (SUBMATERIAL.equals(part.getNumber())) {
				return part;
			}

		}

		return result;
	}

	public static List<WTPart> getMonolayerPart(WTPart parentPart) {
		List<WTPart> resultList = new ArrayList<WTPart>();
		if (parentPart != null) {
			QueryResult qr = null;
			try {
				qr = WTPartHelper.service.getUsesWTPartMasters(parentPart);

				String parentSourceCN = getSourceCN(parentPart);
				if (qr != null && qr.size() > 0) {

					while (qr.hasMoreElements()) {
						WTPartUsageLink links = (WTPartUsageLink) qr.nextElement();

						List<WTPartSubstituteLink> substituteLinks = getSubstituteLinks(links);
						// 替代料
						boolean flag = false;
						if (substituteLinks != null && substituteLinks.size() > 0 && flag) {

							for (WTPartSubstituteLink link : substituteLinks) {

								QueryResult qrVersions = VersionControlHelper.service
										.allVersionsOf(link.getSubstitutes());
								while (qrVersions.hasMoreElements()) {

									WTPart childpart = (WTPart) qrVersions.nextElement();
									String lifeCycleState = childpart.getLifeCycleState().toString();
									String childSourceCN = getSourceCN(childpart);
									// if ("外购".equals(parentSourceCN) &&
									// "自制".equals(childSourceCN))
									// break;

									if ("RELEASED".equals(lifeCycleState)) {
										resultList.add(childpart);
										break;
									} else if ("ARCHIVED".equals(lifeCycleState)) {
										// if ("RELEASED".equals(parentState)) {
										// continue;
										// } else {
										resultList.add(childpart);
										break;
										// }
									} else if ("INWORK".equals(lifeCycleState) || "REWORK".equals(lifeCycleState)) {
										continue;
									} else {
										if (isHaveECA(childpart) || isRunningGuiDangWorkflow(childpart)) {
											continue;
										} else {
											resultList.add(childpart);
											break;
										}
									}
								}
							}

						}
						// 原料
						WTPartMaster masterChild = links.getUses();
						QueryResult qrVersions = VersionControlHelper.service.allVersionsOf(masterChild);
						while (qrVersions.hasMoreElements()) {

							WTPart childpart = (WTPart) qrVersions.nextElement();
							String childSourceCN = getSourceCN(childpart);
							// if ("外购".equals(parentSourceCN) &&
							// "自制".equals(childSourceCN))
							// break;

							String lifeCycleState = childpart.getLifeCycleState().toString();
							if ("RELEASED".equals(lifeCycleState)) {
								resultList.add(childpart);
								break;
							} else if ("ARCHIVED".equals(lifeCycleState)) {
								// if ("RELEASED".equals(parentState)) {
								// continue;
								// } else {
								resultList.add(childpart);
								break;
								// }
							} else if ("INWORK".equals(lifeCycleState) || "REWORK".equals(lifeCycleState)) {
								continue;
							} else {
								if (isHaveECA(childpart) || isRunningGuiDangWorkflow(childpart)) {
									continue;
								} else {
									resultList.add(childpart);
									break;
								}
							}
						}

					}
				}
			} catch (WTException e) {
				e.printStackTrace();
			}
		}
		return resultList;

	}

	/****
	 * 判断物料是否在eca中
	 * 
	 * @param part
	 * @return
	 * @throws WTException
	 */
	public static Boolean isHaveECA(WTPart part) throws WTException {

		System.out.println("===是否有ECA====");

		List<WTChangeActivity2> ecas = getEcaWithObject(part);

		System.out.println("=========ecas===========" + ecas);
		for (int i = 0; i < ecas.size(); i++) {
			System.out.println("ecas==" + i + "==name==" + ecas.get(i).getName() + "==状态==" + ecas.get(i).getState());
		}
		if (ecas != null && ecas.size() > 0) {
			for (int i = 0; i < ecas.size(); i++) {
				if ("IMPLEMENTATION".equals(ecas.get(i).getState().toString())) {
					// 实施状态
					return true;
				}
			}

		}
		return false;
	}

	public static List<WTChangeActivity2> getEcaWithObject(Persistable per) throws WTException {
		List<WTChangeActivity2> ecas = new ArrayList<WTChangeActivity2>();

		QuerySpec qs = new QuerySpec(ChangeRecord2.class);
		SearchCondition sc = new SearchCondition(ChangeRecord2.class, "roleBObjectRef.key", SearchCondition.EQUAL,
				per.getPersistInfo().getObjectIdentifier());
		qs.appendWhere(sc, new int[] { 0 });
		QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
		while (qr.hasMoreElements()) {
			ChangeRecord2 cr2 = (ChangeRecord2) qr.nextElement();
			WTChangeActivity2 eca = (WTChangeActivity2) cr2.getChangeActivity2();
			ecas.add(eca);
		}

		return ecas;
	}

	public static WTPart getPart(String nmu, String ver, String viewName) throws Exception {
		if (viewName.equals(""))
			viewName = "Design";
		View view = ViewHelper.service.getView(viewName);
		WTPart part = null;
		QuerySpec qs = new QuerySpec(WTPart.class);
		SearchCondition sc = new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL, nmu);
		qs.appendWhere(sc);
		sc = new SearchCondition(WTPart.class, "view.key.id", SearchCondition.EQUAL,
				view.getPersistInfo().getObjectIdentifier().getId());
		qs.appendAnd();
		qs.appendWhere(sc);
		if (!ver.equals("")) {
			qs.appendAnd();
			qs.appendWhere(new SearchCondition(new KeywordExpression("A0.versionIdA2versionInfo"),
					SearchCondition.EQUAL, new KeywordExpression("'" + ver + "'")));
		}
		qs.appendAnd();
		qs.appendWhere(new SearchCondition(new KeywordExpression("A0.latestiterationInfo"), SearchCondition.EQUAL,
				new KeywordExpression("1")));
		qs.appendOrderBy(WTPart.class, "thePersistInfo.createStamp", true);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr.hasMoreElements())
			part = (WTPart) qr.nextElement();

		return part;
	}

	public static WTPart getLastestWTPartByNumber(String numStr) {

		WTPart part = null;
		try {
			QuerySpec queryspec = new QuerySpec(WTPart.class);

			queryspec.appendSearchCondition(
					new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL, numStr));
			QueryResult queryresult = PersistenceHelper.manager.find(queryspec);
			LatestConfigSpec cfg = new LatestConfigSpec();
			QueryResult qr = cfg.process(queryresult);
			System.out.println("11111111111qrVersions size" + qr.size());
			if (qr.hasMoreElements()) {

				part = (WTPart) qr.nextElement();
				String version = part.getVersionInfo().getIdentifier().getValue() + "."
						+ part.getIterationInfo().getIdentifier().getValue();// 物料版本
				System.out.println("number=" + part.getNumber() + "  version=" + version);
				// return (WTPart) qr.nextElement();
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return part;
	}

	public static WTPart getParentPart(WTPartMaster master) throws PersistenceException, WTException {

		WTPart parentPart = null;

		QueryResult qrVersions = VersionControlHelper.service.allVersionsOf(master);

		while (qrVersions.hasMoreElements()) {
			parentPart = (WTPart) qrVersions.nextElement();

			String version = parentPart.getVersionInfo().getIdentifier().getValue();

			if ("Design".equals(parentPart.getViewName()) && parentPart.getNumber().startsWith("A")
					&& version.compareTo("A") > 0) {
				parentPart = null;
				continue;
			}

			String lifeCycleState = parentPart.getLifeCycleState().toString();
			if ("RELEASED".equals(lifeCycleState)) {
				return parentPart;
			} else if ("ARCHIVED".equals(lifeCycleState)) {
				return parentPart;
			} else if ("INWORK".equals(lifeCycleState) || "REWORK".equals(lifeCycleState)) {
				parentPart = null;
				continue;
			} else {
				if (isHaveECA(parentPart) || isRunningGuiDangWorkflow(parentPart)) {
					parentPart = null;
					continue;
				} else {
					return parentPart;
				}
			}

		}

		return parentPart;
	}

	// 获取特定替换件
	public static List<WTPartSubstituteLink> getSubstituteLinks(WTPartUsageLink usageLink) throws WTException {
		List<WTPartSubstituteLink> substituteLinks = new ArrayList<WTPartSubstituteLink>();
		WTCollection collection = WTPartHelper.service.getSubstituteLinks(usageLink);
		for (Object object : collection) {
			if (object instanceof ObjectReference) {
				ObjectReference objref = (ObjectReference) object;
				Object obj = objref.getObject();

				if (obj instanceof WTPartSubstituteLink) {
					WTPartSubstituteLink link = (WTPartSubstituteLink) obj;
					substituteLinks.add(link);
				}
			}
		}

		return substituteLinks;
	}

	/**
	 * 判断是否有正在运行的归档流程
	 * 
	 * @param pbo
	 * @return
	 * @throws WTException
	 */
	private static boolean isRunningGuiDangWorkflow(WTPart pbo) throws WTException {
		boolean processok = false;
		NmOid nmOid = NmOid.newNmOid(PersistenceHelper.getObjectIdentifier(pbo));
		QueryResult qr = NmWorkflowHelper.service.getRoutingHistoryData(nmOid);

		while (qr.hasMoreElements()) {
			WfProcess process = (WfProcess) qr.nextElement();
			String templateName = process.getTemplate().getName();
			System.out.println("templateName==" + templateName + " process.getState()=" + process.getState());
			// 只判断是否存在关联的流程，不再判断流程名称
			if (process.getState().equals(WfState.OPEN_RUNNING)
					&& (templateName.equals("GenericPartWF") || templateName.equals("GenericPartWF_Electronic")
							|| templateName.equals("GenericManufacturingPartWF"))) {
				processok = true;
				break;
			}
		}
		return processok;
	}

	// 获取默认的源属性
	public static String getSourceCN(WTPart wtpart) throws WTException {
		String sourceValue = "";

		if (wtpart != null) {
			Source source = wtpart.getSource();

			if (source != null) {
				sourceValue = source.getDisplay(Locale.CHINA);
			}
		}

		return sourceValue;
	}

}
