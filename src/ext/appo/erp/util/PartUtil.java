package ext.appo.erp.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import com.ptc.core.lwc.common.view.EnumerationDefinitionReadView;
import com.ptc.core.lwc.common.view.EnumerationMembershipReadView;
import com.ptc.core.lwc.server.TypeDefinitionServiceHelper;

import ext.appo.common.util.Excel2007Handler;
import ext.appo.ecn.pdf.PdfUtil;
import ext.appo.erp.bean.BomCompareBean;
import ext.appo.erp.bean.MaterialInfo;
import ext.appo.erp.constants.ERPConstants;
import ext.pi.PIException;
import ext.pi.core.PIAttributeHelper;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.part.Source;
import wt.part.WTPart;
import wt.part.WTPartUsageLink;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.vc.config.LatestConfigSpec;

public class PartUtil {

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

	/*
	 * 通过number获取某视图的最新的物料
	 */
	public static WTPart getLastestWTPartByNumber(String numStr) {
		QuerySpec queryspec = null;
		try {
			queryspec = new QuerySpec(WTPart.class);

			queryspec.appendSearchCondition(
					new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL, numStr));
			QueryResult queryresult = PersistenceHelper.manager.find(queryspec);
			LatestConfigSpec cfg = new LatestConfigSpec();
			QueryResult qr = cfg.process(queryresult);
			while (qr.hasMoreElements()) {
				WTPart part = (WTPart) qr.nextElement();
				return part;
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * 通过number获取某视图的最新的物料
	 */
	public static WTPart getLastestWTPartByNumber(String numStr, String view) {
		QuerySpec queryspec = null;
		try {
			queryspec = new QuerySpec(WTPart.class);

			queryspec.appendSearchCondition(
					new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL, numStr));
			QueryResult queryresult = PersistenceHelper.manager.find(queryspec);
			LatestConfigSpec cfg = new LatestConfigSpec();
			QueryResult qr = cfg.process(queryresult);
			while (qr.hasMoreElements()) {
				WTPart part = (WTPart) qr.nextElement();
				if (!view.equals(part.getViewName())) {
					continue;
				}
				return part;
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return null;
	}

	// 获取ECN创建时选择的库存处理措施
	public static String getKcclcs(WTPart part) {
		// System.out.println("库存处理措施方法");
		String inventoryDispose = (String) PdfUtil.getIBAObjectValue(part, "InventoryDispose");
		// System.out.println("获取库存处理措施："+inventoryDispose);
		if (inventoryDispose != null) {
			String[] inventoryArr = inventoryDispose.split(";");
			System.out.println("库存处理措施：" + inventoryArr);
			return inventoryArr[1];
		}

		return "";
	}

	public static MaterialInfo createMaterilInfoBeanByPart(WTPart part) throws Exception {
		MaterialInfo materialInfo = new MaterialInfo();

		String ggms = "";// 规格描述
		String hbsx = "";// 环保属性
		String nbxh = "";// 内部型号
		String xsxh = "";// 销售型号
		String zldw = "";// 重量单位
		String zl = "";// 重量
		String sscpx = "";// 所属产品类别
		String sscpx1 = "";// 所属产品类别
		String wlfl = "";// 物料分类
		String defaultUnit = "";// 默认单位
		String ssxm = "";// 所属项目
		String cpzt = "";// 产品状态
		String gylx = "";// 光源类型
		String cpxl = "";// 产品系列
		String bzcc = "";// 包装尺寸（长*宽*高）
		String xsjs = "";// 显示技术
		String brand = "";// 品牌
		// String F_APPO_BZCC="";//包装尺寸
		// String zdchdj="";//最低存货等级
		String F_APPO_CLYJ = "";// 库存处理措施
		/**
		 * 因为治具类物料在K3没有启动版本，需要PR下达能有版本信息给到采购。
		 * 所以新增需求：物料接口增加字段【旧版本】，来保存治具类版本信息，治具类物料PLM抛送（含升版）时会传【旧版本】数据到K3，K3需要同步保存
		 */
		String oldVersion = "";// 旧版本

		// 是否虚拟件（工艺虚拟件是true则是虚拟件）
		String sfxnj = "0";
		// 是否工艺虚拟件
		sfxnj = getIBAvalue(part, "sfxnj");

		// System.out.println("物料"+part.getNumber()+"是否工艺虚拟件==="+sfxnj);
		if (sfxnj.equals("true")) {
			sfxnj = "1";
		}
		// else{
		// boolean
		// hidePartInStructureValue=part.getHidePartInStructure();//是否设计虚拟件
		//// System.out.println("物料"+part.getNumber()+"是否设计虚拟件==="+hidePartInStructureValue);
		// if (hidePartInStructureValue){
		// sfxnj="1";
		// }else{
		// sfxnj="0";
		// }
		// }
		System.out.println("物料" + part.getNumber() + "是否虚拟件===" + sfxnj);

		// boolean
		// hidePartInStructureValue=part.getHidePartInStructure();//是否设计虚拟件
		// System.out.println("物料"+part.getNumber()+"是否虚拟件："+hidePartInStructureValue);
		// if (hidePartInStructureValue){
		// //虚拟件
		// sfxnj="1";
		// }

		// //是否成品（A开头物料传IBA属性，其余不传IBA属性），
		// String endItem=part.getNumber().substring(0,1);
		// System.out.println("是否A开头成品物料："+endItem);
		// if (endItem=="A"){

		wlfl = getIBAvalue(part, "Classification");// 物料分类
		// System.out.println("获取到的物料分类："+wlfl);
		Map<String, String> map = getMaterialGroupData();// 对应K3分组
		String wlfl1 = map.get(wlfl);// 物料分类

		Map<String, String> fidmap = getMaterialGroupFidData();// 获取对应的父id
		// System.out.println("fidmap size=="+fidmap.size());
		String wlflfidStr = "";
		String wlflid = wlfl;
		for (int i = 0; i < fidmap.size(); i++) {
			System.out.println("wlflid:" + wlflid);
			if (wlflid.equals("appo_zcl") || wlflid.equals("Z01") || wlflid.equals("Z02")) {
				if (wlflid.trim().equals("appo_zcl")) {
					wlflfidStr = "10";
					break;
				}
				if (wlflid.trim().equals("Z01")) {
					wlflfidStr = "11";
					break;
				}
				if (wlflid.trim().equals("Z02")) {
					wlflfidStr = "6";
					break;
				}
			} else {
				String wlflfid = fidmap.get(wlflid);// 获取物料分类父ID
				// 虚拟件优先，
				// 如果上层有appo_zcljj 夹具（如果有是在资产类下层），则取源属性，直接跳出即可
				// 查询是否在appo_zcl 资产类，Z01 费用，Z02 服务下
				// 源对应传值
				// 资产类 10 资产
				// 费用 11 费用
				// 服务 6 服务
				// System.out.println("获取物料分类父ID:"+wlflfid);
				if (wlflfid == null || wlflfid.trim().equals("APPO")) {
					break;
				}
				if (wlflfid.trim().equals("appo_zcl")) {
					wlflfidStr = "10";
					break;
				}
				if (wlflfid.trim().equals("appo_zcljj")) {
					wlflfidStr = "";
					break;
				}
				if (wlflfid.trim().equals("Z01")) {
					wlflfidStr = "11";
					break;
				}
				if (wlflfid.trim().equals("Z02")) {
					wlflfidStr = "6";
					break;
				}
				wlflid = wlflfid;
			}

		}

		// String sourceKey=getIBAvalue(part, "source") ;//源
		String source = getSourceCN(part);
		// System.out.println("源："+source);

		if (sfxnj.trim().equals("1")) {
			// 是虚拟件
			source = "5";// 虚拟
		} else {
			if (wlflfidStr.trim() != null && wlflfidStr != "") {
				if (wlflfidStr.trim().equals("10")) {
					source = "10";
				} else if (wlflfidStr.trim().equals("11")) {
					source = "11";
				} else if (wlflfidStr.trim().equals("6")) {
					source = "6";
				} else {
					if (source.equals("外购")) {
						source = "1";
					} else if (source.equals("自制")) {
						source = "2";
					} else if (source.equals("外协")) {
						source = "3";
					} else {
						source = "";
					}
				}

			} else {
				if (source.equals("外购")) {
					source = "1";
				} else if (source.equals("自制")) {
					source = "2";
				} else if (source.equals("外协")) {
					source = "3";
				} else {
					source = "";
				}
			}

		}

		// System.out.println("源K3枚举："+source);

		// String hbsxKey= getIBAvalue(part, "hbsx") ;//环保属性
		String hbsxKey = getHBSX(part);
		// System.out.println("获取到的环保key:"+hbsxKey);
		Map<String, String> hbsxMap = getEnumerationMembership("hbsx", true);
		if (hbsxKey != null && hbsxKey.trim() != "") {
			if (hbsxMap != null && hbsxMap.size() > 0) {
				List hbsxList = split(hbsxKey);
				if (hbsxList != null && hbsxList.size() > 0) {
					for (int i = 0; i < hbsxList.size(); i++) {
						hbsx = hbsx + hbsxMap.get(hbsxList.get(i)) + ",";
					}
					hbsx = hbsx.substring(0, hbsx.length() - 1);
					// System.out.println("环保属性："+hbsx);
				}
			} else {
				hbsx = "";
			}
		} else {
			hbsx = "";
		}

		// Map<String,String> hbsxMap=getEnumerationMembership("hbsx",true);
		// if (hbsxKey!=null){
		// hbsx=hbsxMap.get(hbsxKey);
		// }
		// System.out.println("环保属性："+hbsx);

		// Object ibahbsx = IBAUtil.getIBAValue(part,"hbsx");
		// System.out.println("ibahbsx:"+ibahbsx);

		xsxh = getIBAvalue(part, "xsxh");// 销售型号
		zldw = getIBAvalue(part, "zldw");// 重量单位
		zl = getIBAvalue(part, "zl");// 重量
		sscpx = getIBAvalue(part, "sscpx");// 所属产品类别

		defaultUnit = getIBAvalue(part, "defaultUnit");// 默认单位
		ssxm = getIBAvalue(part, "ssxm");// 所属项目
		cpzt = getIBAvalue(part, "cpzt");// 产品状态
		gylx = getIBAvalue(part, "gylx");// 光源类型
		cpxl = getIBAvalue(part, "cpxl");// 产品系列
		bzcc = getIBAvalue(part, "bzcc");// 包装尺寸（长*宽*高）
		// System.out.println("包装尺寸："+bzcc);
		xsjs = getIBAvalue(part, "xsjs");// 显示技术
		brand = getIBAvalue(part, "brand");// 品牌
		// F_APPO_BZCC=getIBAvalue(part,"bzcc");//包装尺寸
		// System.out.println("包装尺寸："+F_APPO_BZCC);
		F_APPO_CLYJ = PartUtil.getKcclcs(part);// 获取库存处理措施

		// List list=new ArrayList();
		// QueryResult qr = WTPartHelper.service.getUsesWTPartMasters(part);
		//// System.out.println("qr结果："+qr);
		// while (qr.hasMoreElements()) {
		// WTPartUsageLink links = (WTPartUsageLink) qr.nextElement();
		//// System.out.println("获取到的links:"+links);
		// String stockGrade = (String) PdfUtil.getIBAObjectValue(links,
		// "stockGrade");//最低存货等级
		//// System.out.println("最低stockGrade："+stockGrade);
		// if (stockGrade!=null){
		// list.add(stockGrade);
		// }
		// }
		//
		// if (list!=null&&list.size()>0){
		// zdchdj= (String) Collections.min(list); //最低存货等级
		// }

		// System.out.println("最低存货等级："+zdchdj);

		Map<String, String> cpxTable = getCPXTable();
		// System.out.println("cpxTable:"+cpxTable);
		// System.out.println("产品线-产品类别："+sscpx);
		sscpx1 = cpxTable.get(sscpx);
		// System.out.println("产品线-产品类别值："+sscpx1);

		// 读取不到，读第一列
		if (sscpx1 == null || sscpx1 == "") {
			Map<String, String> cpxTable2 = getCPXTable2();
			// System.out.println("cpxTable2:"+cpxTable2);
			sscpx1 = cpxTable2.get(sscpx);
			// System.out.println("产品线-产品类别2："+sscpx);
			// System.out.println("产品线-产品类别值2："+sscpx1);
		}
		// }

		String majorVersion = part.getVersionIdentifier().getValue();// 大版本
		String modifier = part.getModifierFullName();// 修改者
		String creator = part.getCreatorFullName();// 创建者
		String lifeCycleState = part.getLifeCycleState().toString();
		// System.out.println("物料状态："+lifeCycleState);
		if (lifeCycleState.equals(ERPConstants.RELEASED)) {
			materialInfo.setZxlcbb(majorVersion);// 最新量产版本
			lifeCycleState = "已发布";
		} else if (lifeCycleState.equals(ERPConstants.ARCHIVED)) {
			lifeCycleState = "已归档";
			materialInfo.setZxlcbb("");
		} else if (lifeCycleState.equals(ERPConstants.OBSOLESCENCE)) {
			lifeCycleState = "废弃";
			materialInfo.setZxlcbb("");
		} else {
			lifeCycleState = "";
			materialInfo.setZxlcbb("");
		}

		// System.out.println("最新量产版本："+materialInfo.getZxlcbb());

		// ClassificationFacade facadeInstance =
		// ClassificationFacade.getInstance();
		// String localizedName =
		// facadeInstance.getLocalizedDisplayNameForClassificationNode(sscpx,
		// CsmConstants.NAMESPACE, new Locale("en"));
		// System.out.println("=======The localized name for
		// FASTENER-BOLT-CUPHEAD is==== " + localizedName);

		// sscpx = lifeCycleState;

		Object obj = getIBAvalue(part, "ggms");// 规格描述
		ggms = obj == null ? "" : (String) obj;
		nbxh = getIBAvalue(part, "nbxh");// 内部型号

		Map<String, String> unitTable = getUnitMatchingTable();

		// System.out.println("物料分类："+wlfl);
		// System.out.println("物料分类值："+map.get(wlfl));
		// System.out.println("默认单位："+defaultUnit);
		// System.out.println("默认单位值："+unitTable.get(defaultUnit));
		// System.out.println("重量单位:"+zldw);
		// System.out.println("重量单位值:"+unitTable.get(zldw));

		defaultUnit = unitTable.get(defaultUnit);
		zldw = unitTable.get(zldw);

		String version = part.getVersionIdentifier().getValue() + "." + part.getIterationIdentifier().getValue();

		// 将 \和"特殊字符 转化为 |和'
		String partNumber = replace(part.getNumber());
		String name = replace(part.getName());
		// 如果名称超过50，截取到50
		if (name.length() >= 50) {
			name = name.substring(0, 50);
		}

		String viewName = replace(part.getViewName());
		majorVersion = replace(majorVersion);
		version = replace(version);
		lifeCycleState = replace(lifeCycleState);
		brand = replace(brand);
		bzcc = replace(bzcc);
		cpxl = replace(cpxl);
		cpzt = replace(cpzt);
		gylx = replace(gylx);
		hbsx = replace(hbsx);
		zldw = replace(zldw);
		zl = replace(zl);
		xsxh = replace(xsxh);
		ssxm = replace(ssxm);
		xsjs = replace(xsjs);
		nbxh = replace(nbxh);
		sscpx1 = replace(sscpx1);
		modifier = replace(modifier);
		wlfl1 = replace(wlfl1);
		defaultUnit = replace(defaultUnit);
		creator = replace(creator);
		F_APPO_CLYJ = replace(F_APPO_CLYJ);
		// zdchdj=replace(zdchdj);
		ggms = replace(ggms);
		// source=replace(source);

		if ("appo_zcljjzz".equals(wlfl)) {
			oldVersion = majorVersion == null ? "" : majorVersion;
		}

		materialInfo.setNumber(partNumber);
		materialInfo.setName(name == null ? "" : name);
		materialInfo.setView(viewName == null ? "" : viewName);
		materialInfo.setMajorVersion(majorVersion == null ? "" : majorVersion);
		materialInfo.setVersion(version == null ? "" : version);
		materialInfo.setLifecycle(lifeCycleState == null ? "" : lifeCycleState);
		materialInfo.setBrand(brand == null ? "" : brand);
		materialInfo.setBzcc(bzcc == null ? "" : bzcc);
		materialInfo.setCpxl(cpxl == null ? "" : cpxl);
		materialInfo.setCpzt(cpzt == null ? "" : cpzt);
		materialInfo.setGylx(gylx == null ? "" : gylx);
		materialInfo.setHbsx(hbsx == null ? "" : hbsx);
		materialInfo.setZldw(zldw == null ? "" : zldw);
		materialInfo.setZl(zl == null ? "0" : zl);
		materialInfo.setXsxh(xsxh == null ? "" : xsxh);
		materialInfo.setSsxm(ssxm == null ? "" : ssxm);
		materialInfo.setXsjs(xsjs == null ? "" : xsjs);
		materialInfo.setNbxh(nbxh == null ? "" : nbxh);
		materialInfo.setSscpx(sscpx1 == null ? "" : sscpx1);
		materialInfo.setModifier(modifier == null ? "" : modifier);
		materialInfo.setClassification(wlfl1 == null ? wlfl : wlfl1);
		materialInfo.setDefaultUnit(defaultUnit == null ? "" : defaultUnit);
		materialInfo.setCreateUser(creator == null ? "" : creator);
		materialInfo.setF_APPO_CLYJ(F_APPO_CLYJ == null ? "" : F_APPO_CLYJ);// 库存处理措施
		// materialInfo.setZdchdj(zdchdj==null?"":zdchdj);//最低存货等级
		materialInfo.setGgms(ggms);
		// materialInfo.setF_APPO_BZCC(F_APPO_BZCC==null?"":F_APPO_BZCC);//包装尺寸
		// materialInfo.setBzcc(bzcc);
		// materialInfo.setFSpecification(ggms==null?"":ggms);
		materialInfo.setSource(source == null ? "" : source);// 源

		materialInfo.setSfxnj(sfxnj == null ? "0" : sfxnj);
		// mao
		materialInfo.setOldVersion(oldVersion);

		return materialInfo;

	}

	/******
	 * 通过父件子件创建定义的BOM实体
	 * 
	 * @param part
	 *            父件
	 * @param childPart
	 *            子件
	 * @param flag
	 *            标识
	 * @return
	 * @throws WTException
	 */
	public static BomCompareBean createBomCompareBeanByPart(WTPart part, WTPart childPart, String flag)
			throws WTException {
		BomCompareBean bomCompareBean = new BomCompareBean();
		WTPartUsageLink link = BomUtil.getUsageLink(part, childPart.getMaster());

		String stockGrade = (String) PdfUtil.getIBAObjectValue(link, "stockGrade");
		String bomNote = (String) PdfUtil.getIBAObjectValue(link, "bom_note");

		bomCompareBean.setCompareFlag(flag);// 标记
		bomCompareBean.setChildNumber(childPart.getNumber());// 子件编号
		bomCompareBean.setChildName(childPart.getName());// 子件名称
		bomCompareBean.setChildVersion(
				childPart.getVersionIdentifier().getValue() + "." + childPart.getIterationIdentifier().getValue());// 子件版本
		bomCompareBean.setParentName(part.getName());// 父件名称
		bomCompareBean.setParentNumber(part.getNumber());// 父件编号
		bomCompareBean.setParentView(part.getViewName());// 父件视图
		bomCompareBean.setWeihao(BomUtil.getPartReferenceDesignators(link));// 位号
		bomCompareBean.setUnit(link.getQuantity().getUnit().getDisplay(SessionHelper.getLocale()));// 单位
		bomCompareBean.setZdchdj(stockGrade == null ? "" : stockGrade);// 最低存货等级
		bomCompareBean.setBombzxx(bomNote == null ? "" : bomNote);// BOM备注信息

		return bomCompareBean;
	}

	public static List split(String param) {
		List resultList = new ArrayList();
		String[] list = param.split("\\|");
		System.out.println("list:" + list.length);
		for (int i = 0; i < list.length; i++) {
			resultList.add(list[i]);
			// System.out.println("拆分"+list[i]);
		}
		return resultList;
	}

	public static String getIBAvalue(Persistable p, String key) throws PIException {
		Object object = PIAttributeHelper.service.getValue(p, key);
		// System.out.println(key+" object物料属性===="+object);
		String comment = "";
		if (object == null) {
			return comment;
		}
		if (object instanceof String) {
			// System.out.println("object为String:"+object);
			String changeComment = (String) PIAttributeHelper.service.getValue(p, key);
			comment = changeComment;
		}
		if (object instanceof Object[]) {
			Object[] objArr = (Object[]) object;
			for (int i = 0; i < objArr.length; i++) {
				// System.out.println("object[]："+objArr[i].toString());
				comment = comment + objArr[i].toString() + ",";
			}
		}
		if (object instanceof Boolean) {
			// System.out.println("IBA为Boolean:"+object.toString());
			comment = object.toString();
		} else {
			// System.out.println("IBA为else:"+object.toString());
			comment = object.toString();
		}
		// System.out.println(key+" commnet物料属性输出 ========="+comment);
		return comment;
	}

	// 获取环保属性IBA属性，多值
	public static String getHBSX(WTPart part) {
		// System.out.println("进入环保属性IBA属性值:"+part.getNumber());
		Object object = PdfUtil.getIBAObjectValue(part, "hbsx");
		// System.out.println("object环保属性============"+object);
		if (object instanceof String) {
			// System.out.println("object环保属性为String:"+object);
			String hbsx = (String) PdfUtil.getIBAObjectValue(part, "hbsx");
			return hbsx == null ? "" : hbsx;
		}
		if (object instanceof String[]) {
			// System.out.println("object环保属性为String[]:"+object);
			String[] hbsxList = (String[]) PdfUtil.getIBAObjectValue(part, "hbsx");
			String hbsx = "";
			for (int i = 0; i < hbsxList.length; i++) {
				hbsx = hbsx + hbsxList[i].toString() + ",";
			}
			// System.out.println("object环保属性为String[] hbsx:"+hbsx);
			return hbsx == null ? "" : hbsx;
		}
		if (object instanceof List) {
			// System.out.println("object环保属性为List:"+object);
			List hbsxList = (List) PdfUtil.getIBAObjectValue(part, "hbsx");
			String hbsx = "";
			for (int i = 0; i < hbsxList.size(); i++) {
				hbsx = hbsx + hbsxList.get(i).toString() + ",";
			}
			// System.out.println("object环保属性为List hbsx:"+hbsx);
			return hbsx == null ? "" : hbsx;
		}
		if (object instanceof Vector) {
			// System.out.println("object环保属性为Vector:"+object);
			Vector hbsxList = (Vector) PdfUtil.getIBAObjectValue(part, "hbsx");
			String hbsx = "";
			for (int i = 0; i < hbsxList.size(); i++) {
				hbsx = hbsx + hbsxList.get(i).toString() + "|";
			}
			// System.out.println("object环保属性为Vector hbsx:"+hbsx);
			return hbsx == null ? "" : hbsx;
		}
		if (object instanceof Object[]) {
			// System.out.println("object环保属性为Object[]:"+object);
			Object[] objects = (Object[]) PdfUtil.getIBAObjectValue(part, "hbsx");
			String hbsx = "";
			for (int i = 0; i < objects.length; i++) {

				hbsx = hbsx + objects[i].toString() + "|";
			}
			// System.out.println("object环保属性为Object[] hbsx:"+hbsx);
			return hbsx == null ? "" : hbsx;
		}

		// System.out.println("4种方法都没进："+object);
		return "";
	}

	/*****
	 * 读取物料分组Id表
	 * 
	 * @return
	 * @throws IOException
	 */
	public static Map<String, String> getMaterialGroupData() throws IOException {
		Map<String, String> map = new HashMap<>();

		String filePath = WT_CODEBASE + File.separator + "config" + File.separator + "custom" + File.separator
				+ "MaterialGroup.xlsx";
		Excel2007Handler handler = new Excel2007Handler(filePath);
		// Excel2007Handler handler = new
		// Excel2007Handler("D:\\PLM\\GFGD_PLM\\src\\config\\custom\\MaterialGroup.xlsx");
		handler.switchCurrentSheet(0);
		int rowCount = handler.getSheetRowCount();
		for (int i = 1; i < rowCount; i++) {

			map.put(null2blank(handler.getStringValue(i, 1)), null2blank(handler.getStringValue(i, 3)));

		}
		return map;
	}

	/*****
	 * 读取物料分组Id表(父ID)
	 * 
	 * @return
	 * @throws IOException
	 */
	public static Map<String, String> getMaterialGroupFidData() throws IOException {
		Map<String, String> map = new HashMap<>();

		String filePath = WT_CODEBASE + File.separator + "config" + File.separator + "custom" + File.separator
				+ "MaterialGroup.xlsx";
		Excel2007Handler handler = new Excel2007Handler(filePath);
		// Excel2007Handler handler = new
		// Excel2007Handler("D:\\PLM\\GFGD_PLM\\src\\config\\custom\\MaterialGroup.xlsx");
		handler.switchCurrentSheet(0);
		int rowCount = handler.getSheetRowCount();
		for (int i = 1; i < rowCount; i++) {

			map.put(null2blank(handler.getStringValue(i, 1)), null2blank(handler.getStringValue(i, 2)));

		}
		return map;
	}

	/*****
	 * 读取单位对应表
	 * 
	 * @return
	 * @throws IOException
	 */
	public static Map<String, String> getUnitMatchingTable() throws IOException {
		Map<String, String> map = new HashMap<>();

		String filePath = WT_CODEBASE + File.separator + "config" + File.separator + "custom" + File.separator
				+ "UnitMatchingTable.xlsx";
		Excel2007Handler handler = new Excel2007Handler(filePath);
		handler.switchCurrentSheet(0);
		int rowCount = handler.getSheetRowCount();
		for (int i = 1; i < rowCount; i++) {

			map.put(null2blank(handler.getStringValue(i, 0)), null2blank(handler.getStringValue(i, 1)));

		}
		return map;
	}

	/*****
	 * 读取产品线对应表
	 * 
	 * @return
	 * @throws IOException
	 */
	public static Map<String, String> getCPXTable() throws IOException {
		Map<String, String> map = new HashMap<>();

		String filePath = WT_CODEBASE + File.separator + "config" + File.separator + "custom" + File.separator
				+ "cpxtable.xlsx";
		// System.out.println("产品线表地址："+filePath);
		Excel2007Handler handler = new Excel2007Handler(filePath);
		handler.switchCurrentSheet(0);
		int rowCount = handler.getSheetRowCount();
		// System.out.println("产品线读表rowCount:"+rowCount);
		for (int i = 1; i < rowCount; i++) {

			map.put(null2blank(handler.getStringValue(i, 1)), null2blank(handler.getStringValue(i, 2)));
		}
		return map;
	}

	/*****
	 * 读取产品线对应表(1不行，读取0)
	 * 
	 * @return
	 * @throws IOException
	 */
	public static Map<String, String> getCPXTable2() throws IOException {
		Map<String, String> map = new HashMap<>();

		String filePath = WT_CODEBASE + File.separator + "config" + File.separator + "custom" + File.separator
				+ "cpxtable.xlsx";
		// System.out.println("产品线表地址："+filePath);
		Excel2007Handler handler = new Excel2007Handler(filePath);
		handler.switchCurrentSheet(0);
		int rowCount = handler.getSheetRowCount();
		// System.out.println("产品线读表rowCount:"+rowCount);
		for (int i = 1; i < rowCount; i++) {

			map.put(null2blank(handler.getStringValue(i, 0)), null2blank(handler.getStringValue(i, 2)));
		}
		return map;
	}

	/**
	 * 获取产品线
	 * 
	 * @return
	 */
	public static String getCPX(String cpx) throws IOException {

		String ssxpx = "";

		Map<String, String> map = getCPXTable();

		String substring = cpx.substring(0, 1);

		if ("H".equals(substring) || "X".equals(substring)) {

			ssxpx = map.get(substring);
		} else {
			ssxpx = map.get(cpx);
		}

		return ssxpx;
	}

	public static String null2blank(Object obj) {
		if (obj == null) {
			return "";
		} else {
			String tmp = obj.toString();
			return tmp.trim();
		}
	}

	// 将 \和"特殊字符 转化为 |和'，传json时
	public static String replace(String str) {
		String str1 = "";
		String str2 = "";
		if (str != null) {
			str1 = str.replace('\\', '/');
			str2 = str1.replace('\"', '\'');
			// System.out.println("替换后:"+str1);
			// System.out.println("替换后:"+str2);
		}

		return str2;
	}

	/**
	 * 获取某个枚举所有成员的显示值和内部值 flag为true返回<内部值，显示值> flag为false返回<显示值，内部值>
	 *
	 * @param enumInterValue
	 * @param flag
	 * @return
	 * @throws WTException
	 */
	public static Map<String, String> getEnumerationMembership(String enumInterValue, boolean flag) throws WTException {
		Map<String, String> map = new HashMap<>();
		EnumerationDefinitionReadView edr = TypeDefinitionServiceHelper.service.getEnumDefView(enumInterValue);
		Collection<EnumerationMembershipReadView> list = edr.getAllMemberships();
		for (EnumerationMembershipReadView enumer : list) {
			Object temp = enumer.getMember().getPropertyValueByName("displayName").getValue();
			String displayName = temp == null ? "" : temp.toString();
			if (flag) {
				map.put(enumer.getMember().getName(), displayName);
			} else {
				map.put(displayName, enumer.getMember().getName());
			}
		}
		return map;
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
