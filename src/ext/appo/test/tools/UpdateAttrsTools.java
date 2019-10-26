package ext.appo.test.tools;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

import ext.appo.common.util.Excel2007Handler;
import ext.appo.erp.util.BomUtil;
import ext.customer.common.IBAUtil;
import ext.generic.generatenumber.rule.model.MergeAttribute;
import ext.generic.generatenumber.rule.model.NumberAttrRule;
import ext.generic.generatenumber.rule.resource.NORuleOperRB;
import ext.generic.generatenumber.rule.util.NORuleUtil;
import ext.pi.PIException;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIClassificationHelper;
import wt.fc.QueryResult;
import wt.fc.collections.WTArrayList;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.pom.Transaction;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.units.FloatingPointWithUnits;
import wt.util.WTException;
import wt.util.WTMessage;
import wt.util.WTProperties;
import wt.vc.VersionControlHelper;

public class UpdateAttrsTools {

	public static final String WT_CODEBASE;

	// //excel-1
	// private final static String attr1Key = "";//excelA的属性的内部名称
	//
	// //exce2
	// private final static String attr2Key = "";//excelB的属性的内部名称
	// private final static String attr3Key = "";//excelB的属性的内部名称
	// private final static String attr4Key = "";//excelB的属性的内部名称
	// private final static String attr5Key = "";//excelB的属性的内部名称

	// excel-1
	private final static String attr1Key = "";// excelA的属性的内部名称

	// exce2
	private final static String attr2Key = "sscpx";// excelB的属性的内部名称（产品线）
	private final static String attr3Key = "nbxh";// excelB的属性的内部名称（内部型号）
	private final static String attr4Key = "xsxh";// excelA的属性的内部名称（销售型号）
	private final static String attr5Key = "brand";// excelB的属性的内部名称（品牌）
	private final static String attr6Key = "cpzt";// excelB的属性的内部名称（产品状态）
	private final static String attr7Key = "cpxl";// excelB的属性的内部名称（产品系列）
	private final static String attr8Key = "sfzycp";// excelB的属性的内部名称（是否自研成品）
	private final static String attr9Key = "sfxnj";// excelB的属性的内部名称（是否工艺虚拟件）
	private final static String attr10Key = "ggms";// excelB的属性的内部名称（规格描述）
	private final static String attr11Key = "hbsx";// excelB的属性的内部名称（环保属性）
	private final static String attr12Key = "csxh";// excelB的属性的内部名称（厂商型号）
	private final static String attr13Key = "bz";// excelB的属性的内部名称（备注,可存放供应商等）

	static {
		try {
			WTProperties wtproperties = WTProperties.getLocalProperties();
			WT_CODEBASE = wtproperties.getProperty("wt.codebase.location");
		} catch (Throwable throwable) {
			throwable.printStackTrace(System.err);
			throw new ExceptionInInitializerError(throwable);
		}
	}

	public static List<WTPart> getAllPart(WTPartMaster partMaster) throws WTException {
		List<WTPart> list = new ArrayList<>();
		if (partMaster != null) {
			QueryResult qrVersions = VersionControlHelper.service.allIterationsOf(partMaster);
			while (qrVersions.hasMoreElements()) {
				WTPart part = (WTPart) qrVersions.nextElement();
				list.add(part);
			}
		}

		return list;
	}

	public static String null2blank(Object obj) {
		if (obj == null)
			return "";
		if (StringUtils.isEmpty(obj.toString().trim()))
			return "";
		return obj.toString().trim();
	}

	private static final String configPath_A = WT_CODEBASE + File.separator + "ext" + File.separator + "appo"
			+ File.separator + "test" + File.separator + "tools" + File.separator + "excelA.xlsx";

	public static List<PartAttrsBean> buildExcelA() throws WTException {
		List<PartAttrsBean> list = new ArrayList<>();
		try {
			File file = new File(configPath_A);
			FileInputStream fis = new FileInputStream(file);
			Excel2007Handler excelHander = new Excel2007Handler(fis);
			excelHander.switchCurrentSheet(0);
			int sheetRowCount = excelHander.getSheetRowCount();
			for (int i = 1; i < sheetRowCount; i++) {
				String col1 = excelHander.getStringValue(i, 0);// 物料编码
				String col2 = excelHander.getStringValue(i, 1);// 属性1
				PartAttrsBean pab = new PartAttrsBean();
				pab.setPartNumber(null2blank(col1));
				pab.setAttr1(null2blank(col2));
				pab.setRowNum(String.valueOf(i + 1));
				list.add(pab);
			}
		} catch (Exception e) {
			throw new WTException(e);
		}
		return list;
	}

	private static final String configPath_B = WT_CODEBASE + File.separator + "ext" + File.separator + "appo"
			+ File.separator + "test" + File.separator + "tools" + File.separator + "excelB.xlsx";

	public static List<PartAttrsBean> buildExcelB() throws WTException {
		List<PartAttrsBean> list = new ArrayList<>();
		try {
			File file = new File(configPath_B);
			FileInputStream fis = new FileInputStream(file);
			Excel2007Handler excelHander = new Excel2007Handler(fis);
			excelHander.switchCurrentSheet(0);
			int sheetRowCount = excelHander.getSheetRowCount();
			for (int i = 1; i < sheetRowCount; i++) {
				String col1 = excelHander.getStringValue(i, 0);// 物料编码
				String col2 = excelHander.getStringValue(i, 1);// 属性1(sscpx 产品线）
				String col3 = excelHander.getStringValue(i, 2);// 属性2（nbxh 内部型号）
				String col4 = excelHander.getStringValue(i, 3);// 属性3（xsxh 销售型号）
				String col5 = excelHander.getStringValue(i, 4);// 属性4（brand 品牌）
				String col6 = excelHander.getStringValue(i, 5);// 属性5（cpzt 产品状态）
				String col7 = excelHander.getStringValue(i, 6);// 属性6（cpxl 产品系列）
				String col8 = excelHander.getStringValue(i, 7);// 属性7（sfzycp
																// 是否自研成品）
				String col9 = excelHander.getStringValue(i, 8);// 属性8（sfxnj
																// 是否工艺虚拟件）

				String col10 = excelHander.getStringValue(i, 9);// 属性9（规格描述
				String col11 = excelHander.getStringValue(i, 10);// 属性10（环保属性
				String col12 = excelHander.getStringValue(i, 11);// 属性11（厂商型号
				String col13 = excelHander.getStringValue(i, 12);// 属性12（备注

				PartAttrsBean pab = new PartAttrsBean();
				pab.setPartNumber(null2blank(col1));
				pab.setAttr2(null2blank(col2));
				pab.setAttr3(null2blank(col3));
				pab.setAttr4(null2blank(col4));
				pab.setAttr5(null2blank(col5));
				pab.setAttr6(null2blank(col6));
				pab.setAttr7(null2blank(col7));
				pab.setAttr8(null2blank(col8));
				pab.setAttr9(null2blank(col9));
				pab.setAttr10(null2blank(col10));
				pab.setAttr11(null2blank(col11));
				pab.setAttr12(null2blank(col12));
				pab.setAttr13(null2blank(col13));
				pab.setRowNum(String.valueOf(i + 1));
				list.add(pab);
			}
		} catch (Exception e) {
			throw new WTException(e);
		}
		return list;
	}

	public static List<String> updateExcelA() throws Exception {
		List<PartAttrsBean> pabs = buildExcelA();
		List<String> msgs = new ArrayList<>();
		Map<WTPartMaster, PartAttrsBean> map = new HashMap<>();
		for (PartAttrsBean pab : pabs) {
			String partNumber = pab.getPartNumber();
			String attr1 = pab.getAttr1();
			if (StringUtils.isEmpty(partNumber) || StringUtils.isEmpty(attr1)) {
				String msg = "第" + pab.getRowNum() + "行数据，编码或属性值为空，请检查!";
				msgs.add(msg);
				continue;
			}
			WTPartMaster partMaster = BomUtil.getWTPartMaster(partNumber);
			if (partMaster == null) {
				String msg = "第" + pab.getRowNum() + "行数据，找不到物料，请检查!";
				msgs.add(msg);
				continue;
			}
			map.put(partMaster, pab);
		}
		if (msgs.size() > 0) {
			return msgs;
		}

		for (WTPartMaster partMaster : map.keySet()) {
			System.out
					.println("====A========" + partMaster.getNumber() + ":" + partMaster.getName() + "=========start");
			PartAttrsBean pab = map.get(partMaster);
			String attr1 = pab.getAttr1();
			List<WTPart> parts = getAllPart(partMaster);
			for (WTPart part : parts) {
				IBAUtil.setIBAAnyValue(part, attr1Key, attr1);
			}
		}

		return msgs;
	}

	public static List<String> updateExcelB() throws Exception {
		List<PartAttrsBean> pabs = buildExcelB();
		List<String> msgs = new ArrayList<>();
		Map<WTPartMaster, PartAttrsBean> map = new HashMap<>();
		for (PartAttrsBean pab : pabs) {
			String partNumber = pab.getPartNumber();
			if (StringUtils.isEmpty(partNumber)) {
				String msg = "第" + pab.getRowNum() + "行数据，物料编码为空，请检查!";
				msgs.add(msg);
				continue;
			}
			WTPartMaster partMaster = BomUtil.getWTPartMaster(partNumber);
			if (partMaster == null) {
				String msg = "第" + pab.getRowNum() + "行数据，找不到物料，请检查!";
				msgs.add(msg);
				continue;
			}
			map.put(partMaster, pab);
		}
		if (msgs.size() > 0) {
			return msgs;
		}

		for (WTPartMaster partMaster : map.keySet()) {
			System.out
					.println("====B========" + partMaster.getNumber() + ":" + partMaster.getName() + "=========start");
			PartAttrsBean pab = map.get(partMaster);
			String attr2 = pab.getAttr2();
			String attr3 = pab.getAttr3();
			String attr4 = pab.getAttr4();
			String attr5 = pab.getAttr5();
			String attr6 = pab.getAttr6();
			String attr7 = pab.getAttr7();
			String attr8 = pab.getAttr8();
			String attr9 = pab.getAttr9();
			String attr10 = pab.getAttr10();
			String attr11 = pab.getAttr11();
			String attr12 = pab.getAttr12();
			String attr13 = pab.getAttr13();

			List<WTPart> parts = getAllPart(partMaster);
			for (WTPart part : parts) {
				if (StringUtils.isNotEmpty(attr2)) {
					IBAUtil.setIBAAnyValue(part, attr2Key, attr2);
				}
				if (StringUtils.isNotEmpty(attr3)) {
					IBAUtil.setIBAAnyValue(part, attr3Key, attr3);
				}
				if (StringUtils.isNotEmpty(attr4)) {
					IBAUtil.setIBAAnyValue(part, attr4Key, attr4);
				}
				if (StringUtils.isNotEmpty(attr5)) {
					IBAUtil.setIBAAnyValue(part, attr5Key, attr5);
				}
				if (StringUtils.isNotEmpty(attr6)) {
					IBAUtil.setIBAAnyValue(part, attr6Key, attr6);
				}
				if (StringUtils.isNotEmpty(attr7)) {
					IBAUtil.setIBAAnyValue(part, attr7Key, attr7);
				}
				if (StringUtils.isNotEmpty(attr8)) {
					IBAUtil.setIBAAnyValue(part, attr8Key, attr8);
				}
				if (StringUtils.isNotEmpty(attr9)) {

					System.out.println("attr9===" + attr9);
					boolean b_attr9 = true;
					if ("1".equals(attr9) || "1.0".equals(attr9)) {
						b_attr9 = true;
					} else if ("0".equals(attr9)) {
						b_attr9 = false;
					}
					IBAUtil.setIBAAnyValue(part, attr9Key, b_attr9);
				}

				if (StringUtils.isNotEmpty(attr10)) {
					System.out.println("attr10===" + attr10);
					IBAUtil.setIBAAnyValue(part, attr10Key, attr10);
				}
				if (StringUtils.isNotEmpty(attr11)) {
					System.out.println("attr11===" + attr11);
					IBAUtil.setIBAAnyValue(part, attr11Key, attr11);
				}
				if (StringUtils.isNotEmpty(attr12)) {
					System.out.println("attr12===" + attr12);
					IBAUtil.setIBAAnyValue(part, attr12Key, attr12);
				}
				if (StringUtils.isNotEmpty(attr13)) {
					System.out.println("attr13===" + attr13);
					IBAUtil.setIBAAnyValue(part, attr13Key, attr13);
				}

				// 更新规格描述,add mao
				// MergeProperties mergeProperties = new MergeProperties();
				// mergeProperties.setProperties(part);
				// NumberAttrRule noRule = getNumberAttrRule(part);
				//
				// String ggms = getMergeValueByRule(part, noRule);
				// System.out.println("ggms===" + ggms);
				//
				// PIAttributeHelper.service.forceUpdateSoftAttribute(part,
				// "ggms", ggms);

			}
		}

		return msgs;
	}

	public static String getMergeValueByRule(WTPart part, NumberAttrRule rule) throws WTException {
		Locale locale = SessionHelper.manager.getServerLocale();
		String mergeValue = "";
		String RESOURCE = NORuleOperRB.class.getName();
		if (part != null && rule != null) {
			QueryResult attrs = NORuleUtil.queryRefMergeAttrsByRule(rule);
			if (attrs != null && attrs.size() > 0) {
				ArrayList<String> values = new ArrayList<String>();
				StringBuffer errorMessage = new StringBuffer();
				while (attrs.hasMoreElements()) {
					MergeAttribute maxLen = (MergeAttribute) attrs.nextElement();
					String numLen = getValueByMergeAttribute(part, maxLen);
					int mergeLength = maxLen.getMergeLength() == null ? 0 : maxLen.getMergeLength().intValue();
					if (StringUtils.isNotBlank(numLen)) {
						int len = numLen.getBytes().length;
						if (mergeLength > 0 && len > mergeLength) {
							errorMessage.append(WTMessage.getLocalizedMessage(RESOURCE, "RULE_ATTR_VALUE_OVERLENGTH",
									new Object[] { maxLen.getMergeName(), Integer.valueOf(mergeLength) }, locale))
									.append("，");
						}
					}

					if (errorMessage.length() > 0) {
						break;
					}

					values.add(numLen);
				}

				if (errorMessage.length() == 0) {

					mergeValue = StringUtils.join(values, "");
				}
			}
		}

		return mergeValue;

	}

	private static String getValueByMergeAttribute(WTPart p, MergeAttribute attr) throws WTException {
		String value = "";
		Locale locale = SessionHelper.manager.getServerLocale();
		if (attr != null) {
			String mergeType = attr.getMergeType();
			String prefix = attr.getMergePrefix();
			prefix = prefix == null ? "" : prefix;
			String suffix = attr.getMergeSuffix();
			suffix = suffix == null ? "" : suffix;
			if ("CONSTANT".equals(mergeType)) {
				value = attr.getMergeValue();
			} else {
				String attrName;
				if ("DATE".equals(mergeType)) {
					attrName = attr.getMergeValue();
					value = getCurTimeByFormat(attrName);
				} else {
					attrName = attr.getMergeValue();
					String attrType = attr.getActualMergeValue();

					try {
						Object e = PIAttributeHelper.service.getValue(p, attrName);
						if (e instanceof FloatingPointWithUnits) {
							e = PIAttributeHelper.service.getDisplayValue(p, attrName, locale);
						}

						if (e != null) {
							Object[] time;
							if (StringUtils.isBlank(attrType)) {
								if (e instanceof Object[]) {
									time = (Object[]) e;
									Arrays.sort(time);
									value = StringUtils.join(time, ",");
								} else {
									value = e.toString();
								}
							} else if ("INTERNAL_VALUE".equals(attrType)) {
								if (e instanceof Object[]) {
									time = (Object[]) e;
									Arrays.sort(time);
									value = StringUtils.join(time, ",");
								} else {
									value = e.toString();
								}
							} else if ("DISPLAY_VALUE".equals(attrType)) {
								value = PIAttributeHelper.service.getDisplayValue(p, attrName, locale);
							} else if ("UNIT_ATTR_WITHUNIT".equals(attrType)) {
								value = e.toString();
							} else if ("UNIT_ATTR_WITHOUTUNIT".equals(attrType)) {
								value = e.toString();
								if (value.contains(" ")) {
									value = value.substring(0, value.indexOf(" "));
								}
							} else if (e instanceof Date) {
								Date time1 = (Date) e;
								value = getTimeByFormat(time1, attrType);
							}
						}
					} catch (PIException arg11) {
						arg11.printStackTrace();
						throw new WTException(arg11);
					}
				}
			}

			value = prefix + value + suffix;

			return value;
		} else {
			return value;
		}
	}

	private static String getCurTimeByFormat(String format) {
		GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		return sdf.format(cal.getTime());
	}

	public static NumberAttrRule getNumberAttrRule(WTPart part) {
		NumberAttrRule noRule = null;

		try {
			String classificationName = PIClassificationHelper.service.getBindingAttribute(part);
			String nodeName = "";
			if (StringUtils.isNotBlank(classificationName)) {
				nodeName = (String) PIAttributeHelper.service.getValue(part, classificationName);
			}
			if (StringUtils.isNotBlank(nodeName)) {
				WTArrayList e = NORuleUtil.queryAttrRuleByClfAndType(part, nodeName, "RULE_MERGE");
				if (e != null && e.size() > 0) {
					noRule = (NumberAttrRule) e.getPersistable(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return noRule;
	}

	private static String getTimeByFormat(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		return sdf.format(date);
	}

	public static List<String> updateAttrsTransA() {
		List<String> msgs = new ArrayList<>();
		Transaction tx = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		try {
			tx = new Transaction();
			tx.start();

			System.out.println("update excelA start ================");
			msgs = updateExcelA();
			for (String str : msgs) {
				System.out.println(str);
			}
			System.out.println("update excelA end ================");

			tx.commit();
			tx = null;
		} catch (Exception e) {
			e.printStackTrace();
			msgs.add("系统异常，请查看后台日志。");
		} finally {
			if (tx != null) {
				tx.rollback();
			}
			SessionServerHelper.manager.setAccessEnforced(enforce);
		}
		return msgs;
	}

	public static List<String> updateAttrsTransB() {
		List<String> msgs = new ArrayList<>();
		Transaction tx = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		try {
			tx = new Transaction();
			tx.start();

			System.out.println("update excelB start ================");
			msgs = updateExcelB();
			for (String str : msgs) {
				System.out.println(str);
			}
			System.out.println("update excelB end ================");

			tx.commit();
			tx = null;
		} catch (Exception e) {
			e.printStackTrace();
			msgs.add("系统异常，请查看后台日志。");
		} finally {
			if (tx != null) {
				tx.rollback();
			}
			SessionServerHelper.manager.setAccessEnforced(enforce);
		}
		return msgs;
	}
}
