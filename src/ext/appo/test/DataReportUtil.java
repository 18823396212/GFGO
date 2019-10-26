package ext.appo.test;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.json.JSONArray;

import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.netmarkets.workflow.NmWorkflowHelper;
import com.ptc.windchill.csm.common.CsmConstants;

import ext.appo.ecn.common.util.ChangeUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.ecn.report.ExportEcadeferentItems;
import ext.appo.ecn.report.Object.ECAObject;
import ext.appo.part.workflow.PartWorkflowUtil;
import ext.appo.update.UpdateServerData;
import ext.appo.util.PartUtil;
import ext.pi.PIException;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIClassificationHelper;
import ext.pi.core.PIPartHelper;
import wt.access.AccessControlHelper;
import wt.access.AccessPermission;
import wt.change2.ChangeHelper2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentRoleType;
import wt.doc.WTDocument;
import wt.facade.classification.ClassificationFacade;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectReference;
import wt.fc.ObjectToObjectLink;
import wt.fc.PersistInfo;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTCollection;
import wt.httpgw.GatewayAuthenticator;
import wt.identity.IdentityFactory;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartSubstituteLink;
import wt.part.WTPartUsageLink;
import wt.pds.StatementSpec;
import wt.query.ArrayExpression;
import wt.query.ClassAttribute;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.WhereExpression;
import wt.session.SessionHelper;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;
import wt.vc.struct.StructHelper;
import wt.vc.views.View;
import wt.vc.views.ViewHelper;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfBlock;
import wt.workflow.engine.WfConnector;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WorkItem;

public class DataReportUtil implements RemoteAccess {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		// windchill ext.appo.test.DataReportUtil -num1 E04021000002
		RemoteMethodServer rms = RemoteMethodServer.getDefault();
		GatewayAuthenticator auth = new GatewayAuthenticator();
		auth.setRemoteUser("wcadmin");
		rms.setAuthenticator(auth);

		String Number = null;

		for (int i = 0; i < args.length; i += 2) {
			if ("-num1".equals(args[i])) {
				Number = args[i + 1];
			}
		}
		/*
		 * WTPart part = PartUtil.getLastestWTPartByNumber(Number);
		 * getalternatepart(part); getSubpart(part);
		 * 
		 * ClassificationFacade facadeInstance =
		 * ClassificationFacade.getInstance(); String localizedName =
		 * facadeInstance.getLocalizedDisplayNameForClassificationNode(
		 * "E0100000", CsmConstants.NAMESPACE, new Locale("en"));
		 * System.out.println("The localized name for FASTENER-BOLT-CUPHEAD is "
		 * + localizedName);
		 */
		// Timestamp createtime=part.getCreateTimestamp();
		// DateFormat dateFormat;
		// dateFormat = new SimpleDateFormat("yyyy/MM/dd", SessionHelper
		// .getLocale());
		// String date1=dateFormat.format(createtime);
		// Boolean kk=false;
		// System.out.println("date1=="+date1);
		// kk=compare_date(date1, "2019/01/20");
		// System.out.println("kk=="+kk);
		// getNOTprintd();
		// getAPPOwithYLX();
		// getYLXnoBom();
		// getnewpartOldpart();
		// getnewpartOldpart2();
		// System.out.println("part ==="+part);
		// getRelasedpartwithArich();
		getRreplacepart();
		// getCPRreplacepart();
	}

	// 导出电容电阻信息，及相关替代料
	public static void getRreplacepart() throws Exception {
		Vector partDesignvector = getAllLatestWTParts("Design", "");
		Vector partManufacturvector = getAllLatestWTParts("Manufacturing", "");
		System.out.println("partDesignvector size====" + partDesignvector.size());
		System.out.println("partManufacturvector size====" + partManufacturvector.size());
		Vector dataList = getEpart(partDesignvector, partManufacturvector);
		System.out.println("dataList size====" + dataList.size());
		ExportEcadeferentItems exportEcadeferentItems = new ExportEcadeferentItems(dataList);
		exportEcadeferentItems.doExport(dataList);
	}

	// 导出结构件，光学件信息，及相关替代料
	public static void getCPRreplacepart() throws Exception {
		Vector partDesignvector = getAllLatestWTParts("Design", "");
		Vector partManufacturvector = getAllLatestWTParts("Manufacturing", "");
		System.out.println("partDesignvector size====" + partDesignvector.size());
		System.out.println("partManufacturvector size====" + partManufacturvector.size());
		Vector dataList = getCPpart(partDesignvector, partManufacturvector);
		System.out.println("dataList size====" + dataList.size());
		ExportEcadeferentItems exportEcadeferentItems = new ExportEcadeferentItems(dataList);
		exportEcadeferentItems.doExport(dataList);
	}

	public static StringBuilder getalternatepart(WTPart part) throws WTException {
		StringBuilder altpartnumber = new StringBuilder();
		QueryResult altpartqr = WTPartHelper.service.getAlternatesWTPartMasters(part.getMaster());
		while (altpartqr.hasMoreElements()) {
			// System.out.println(altpart.nextElement());
			WTPartMaster altpart = (WTPartMaster) altpartqr.nextElement();

			if (altpartnumber.length() > 0) {
				altpartnumber.append(";");
				altpartnumber.append(altpart.getNumber());
			} else {
				altpartnumber.append(altpart.getNumber());
			}
		}
		System.out.println(altpartnumber);
		return altpartnumber;
	}

	public static StringBuilder getSubpart(WTPart part) throws WTException {
		StringBuilder altpartnumber = new StringBuilder();
		List<String> listpartnumber = new ArrayList<>();
		WTCollection wtcol = WTPartHelper.service.getSubstituteLinksAnyAssembly(part.getMaster());
		if (wtcol != null) {
			Iterator<?> iterator = wtcol.iterator();
			while (iterator.hasNext()) {
				Object obj = iterator.next();
				if (obj instanceof ObjectReference) {
					obj = ((ObjectReference) obj).getObject();

					WTPartSubstituteLink sublink = (WTPartSubstituteLink) obj;
					WTPartMaster replaceMaster = sublink.getSubstitutes();
					if (!listpartnumber.contains(replaceMaster.getNumber())) {
						listpartnumber.add(replaceMaster.getNumber());
						if (altpartnumber.length() > 0) {
							altpartnumber.append(";");
							altpartnumber.append(replaceMaster.getNumber());
						} else {
							altpartnumber.append(replaceMaster.getNumber());
						}
					}
				}

			}
		}
		System.out.println(altpartnumber);
		return altpartnumber;
	}

	public static Vector getCPpart(Vector partvector, Vector partManufacturvector) throws WTException {
		if (partvector == null || partvector.size() == 0) {
			System.out.println("data is null!");
			return null;
		}
		DateFormat dateFormat;
		dateFormat = new SimpleDateFormat("yyyy/MM/dd", SessionHelper.getLocale());
		Vector datavector = new Vector();
		for (int i = 0; i < partvector.size(); i++) {
			WTPart part = (WTPart) partvector.get(i);

			if (part.getNumber().startsWith("C") || part.getNumber().startsWith("P")) {// 结构件和光学件
				String nodeHierarchy = "";// 获取分类全路径
				// 获取分类内部值
				String value = "";
				value = (String) PIAttributeHelper.service.getValue(part, "Classification");
				nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(value);
				if (nodeHierarchy.contains("appo_jgj") || nodeHierarchy.contains("appo_gxj")) {
					ECAObject object = new ECAObject();
					ClassificationFacade facadeInstance = ClassificationFacade.getInstance();
					String localizedName = facadeInstance.getLocalizedDisplayNameForClassificationNode(value,
							CsmConstants.NAMESPACE, new Locale("en"));
					object.setECN_number(part.getNumber());
					object.setECN_name(part.getName());
					object.setECN_create_time(localizedName);
					object.setECN_creator(getIBAvalue(part, "cl"));// 材料
					object.setECN_state(getIBAvalue(part, "ccwd"));// 存储温度
					object.setECAnumber(getIBAvalue(part, "ccsd"));// 存储湿度
					object.setECAname(getIBAvalue(part, "hbsx"));// 环保属性
					object.setECA_state(getIBAvalue(part, "wxcc"));// 外形尺寸
					object.setECA_type(getIBAvalue(part, "cz"));// 材质
					object.setECA_after_affectitems(getIBAvalue(part, "bmcl"));// 表面处理
					object.setECA_before_affectitems(getIBAvalue(part, "ys"));// 颜色
					if (part.getNumber().startsWith("C")) {
						object.setECA_before_affectitemsnumber("结构件");// 类型
					} else {
						object.setECA_before_affectitemsnumber("光学件");
					}
					object.setECA_after_version(getSubpart(part).toString());// 特定替代

					object.setECA_new_version(getalternatepart(part).toString());// 全局替代
					object.setECA_new_state(facadeInstance.getLocalizedDisplayNameForClassificationNode(
							part.getState().toString(), CsmConstants.NAMESPACE, new Locale("en")));

					datavector.add(object);
				}
			}
		}
		for (int i = 0; i < partManufacturvector.size(); i++) {
			WTPart part = (WTPart) partManufacturvector.get(i);

			if (part.getNumber().startsWith("C") || part.getNumber().startsWith("P")) {// 结构件和光学件
				String nodeHierarchy = "";// 获取分类全路径
				// 获取分类内部值
				String value = "";
				value = (String) PIAttributeHelper.service.getValue(part, "Classification");
				nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(value);
				if (nodeHierarchy.contains("appo_jgj") || nodeHierarchy.contains("appo_gxj")) {
					ECAObject object = new ECAObject();
					ClassificationFacade facadeInstance = ClassificationFacade.getInstance();
					String localizedName = facadeInstance.getLocalizedDisplayNameForClassificationNode(value,
							CsmConstants.NAMESPACE, new Locale("en"));
					object.setECN_number(part.getNumber());
					object.setECN_name(part.getName());
					object.setECN_create_time(localizedName);
					object.setECN_creator(getIBAvalue(part, "cl"));// 材料
					object.setECN_state(getIBAvalue(part, "ccwd"));// 存储温度
					object.setECAnumber(getIBAvalue(part, "ccsd"));// 存储湿度
					object.setECAname(getIBAvalue(part, "hbsx"));// 环保属性
					object.setECA_state(getIBAvalue(part, "wxcc"));// 外形尺寸
					object.setECA_type(getIBAvalue(part, "cz"));// 材质
					object.setECA_after_affectitems(getIBAvalue(part, "bmcl"));// 表面处理
					object.setECA_before_affectitems(getIBAvalue(part, "ys"));// 颜色
					if (part.getNumber().startsWith("C")) {
						object.setECA_before_affectitemsnumber("结构件");// 类型
					} else {
						object.setECA_before_affectitemsnumber("光学件");
					}
					object.setECA_after_version(getSubpart(part).toString());// 特定替代

					object.setECA_new_version(getalternatepart(part).toString());// 全局替代
					object.setECA_new_state(facadeInstance.getLocalizedDisplayNameForClassificationNode(
							part.getState().toString(), CsmConstants.NAMESPACE, new Locale("en")));

					datavector.add(object);
				}
			}
		}
		return datavector;
	}

	public static Vector getEpart(Vector partvector, Vector partManufacturvector) throws WTException {
		if (partvector == null || partvector.size() == 0) {
			System.out.println("data is null!");
			return null;
		}
		DateFormat dateFormat;
		dateFormat = new SimpleDateFormat("yyyy/MM/dd", SessionHelper.getLocale());
		Vector datavector = new Vector();
		for (int i = 0; i < partvector.size(); i++) {
			WTPart part = (WTPart) partvector.get(i);

			if (part.getNumber().startsWith("E") && !part.getState().toString().startsWith("OBSOLESCENCE")) {// 电子料
				String nodeHierarchy = "";// 获取分类全路径
				// 获取分类内部值
				String value = "";
				value = (String) PIAttributeHelper.service.getValue(part, "Classification");
				nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(value);
				if (nodeHierarchy.contains("E0100000") || nodeHierarchy.contains("E0200000")) {
					ECAObject object = new ECAObject();
					ClassificationFacade facadeInstance = ClassificationFacade.getInstance();
					String localizedName = facadeInstance.getLocalizedDisplayNameForClassificationNode(value,
							CsmConstants.NAMESPACE, new Locale("en"));
					object.setECN_number(part.getNumber());
					object.setECN_name(part.getName());
					object.setECN_create_time(localizedName);
					object.setECN_creator(getIBAvalue(part, "csxh"));// 厂商型号
					object.setECN_state(getIBAvalue(part, "wxcc"));// 外形尺寸
					object.setECAnumber(getIBAvalue(part, "cs"));// 厂商
					object.setECAname(getIBAvalue(part, "hbsx"));// 环保属性
					object.setECA_state(getIBAvalue(part, "yxdj"));// 优选等级
					object.setECA_type(getIBAvalue(part, "zz"));// 阻值
					object.setECA_after_affectitems(getIBAvalue(part, "edgl"));// 额定功率
					object.setECA_before_affectitems(getIBAvalue(part, "jd"));// 精度
					object.setECA_before_affectitemsnumber(getIBAvalue(part, "ggms"));// 规格描述
					object.setECA_before_affectitemsname(getIBAvalue(part, "fzlx"));
					// 封装类型
					/*
					 * object.setECA_before_affectitemsname(getIBAvalue(part,
					 * "datasheet"));// 规格书
					 * object.setECA_before_affectitemsstate(getIBAvalue(part,
					 * "libraryref"));// 符号名称
					 * object.setECA_before_version(getIBAvalue(part,
					 * "librarypath"));// 符号路径
					 * object.setECA_change_type(getIBAvalue(part,
					 * "footprintref"));// 封装名称1
					 * object.setECA_after_affectitemsnumber(getIBAvalue(part,
					 * "footprintpath "));// 封装路径1
					 * object.setECA_after_affectitemsname(getIBAvalue(part,
					 * "footprintref2"));// 封装名称2
					 * object.setECA_after_affectitemsstate(getIBAvalue(part,
					 * "footprintpath2"));// 封装路径2
					 */ object.setECA_after_version(getSubpart(part).toString());// 特定替代

					object.setECA_new_version(getalternatepart(part).toString());// 全局替代
					object.setECA_new_state(facadeInstance.getLocalizedDisplayNameForClassificationNode(
							part.getState().toString(), CsmConstants.NAMESPACE, new Locale("en")));
					object.setCz(getIBAvalue(part, "cz"));
					object.setRz(getIBAvalue(part, "rz"));
					object.setEddy(getIBAvalue(part, "eddy"));
					object.setEdgzwd(getIBAvalue(part, "edgzwd"));
					datavector.add(object);
				}
			}
		}
		for (int i = 0; i < partManufacturvector.size(); i++) {
			WTPart part = (WTPart) partManufacturvector.get(i);

			if (part.getNumber().startsWith("E")) {// 电子料
				String nodeHierarchy = "";// 获取分类全路径
				// 获取分类内部值
				String value = "";
				value = (String) PIAttributeHelper.service.getValue(part, "Classification");
				nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(value);
				if (nodeHierarchy.contains("E0100000") || nodeHierarchy.contains("E0200000")) {
					ECAObject object = new ECAObject();
					ClassificationFacade facadeInstance = ClassificationFacade.getInstance();
					String localizedName = facadeInstance.getLocalizedDisplayNameForClassificationNode(value,
							CsmConstants.NAMESPACE, new Locale("en"));
					object.setECN_number(part.getNumber());
					object.setECN_name(part.getName());
					object.setECN_create_time(localizedName);
					object.setECN_creator(getIBAvalue(part, "csxh"));// 厂商型号
					object.setECN_state(getIBAvalue(part, "wxcc"));// 外形尺寸
					object.setECAnumber(getIBAvalue(part, "cs"));// 厂商
					object.setECAname(getIBAvalue(part, "hbsx"));// 环保属性
					object.setECA_state(getIBAvalue(part, "yxdj"));// 优选等级
					object.setECA_type(getIBAvalue(part, "zz"));// 阻值
					object.setECA_after_affectitems(getIBAvalue(part, "edgl"));// 额定功率
					object.setECA_before_affectitems(getIBAvalue(part, "jd"));// 精度
					object.setECA_before_affectitemsnumber(getIBAvalue(part, "fzlx"));// 封装类型
					object.setECA_before_affectitemsname(getIBAvalue(part, "datasheet"));// 规格书
					object.setECA_before_affectitemsstate(getIBAvalue(part, "libraryref"));// 符号名称
					object.setECA_before_version(getIBAvalue(part, "librarypath"));// 符号路径
					object.setECA_change_type(getIBAvalue(part, "footprintref"));// 封装名称1
					object.setECA_after_affectitemsnumber(getIBAvalue(part, "footprintpath "));// 封装路径1
					object.setECA_after_affectitemsname(getIBAvalue(part, "footprintref2"));// 封装名称2
					object.setECA_after_affectitemsstate(getIBAvalue(part, "footprintpath2"));// 封装路径2
					object.setECA_after_version(getSubpart(part).toString());// 特定替代

					object.setECA_new_version(getalternatepart(part).toString());// 全局替代
					object.setECA_new_state(facadeInstance.getLocalizedDisplayNameForClassificationNode(
							part.getState().toString(), CsmConstants.NAMESPACE, new Locale("en")));
					datavector.add(object);

					datavector.add(object);
				}
			}
		}
		return datavector;
	}

	// 导出目前系统新旧物料编码的统计
	public static void getnewpartOldpart() throws Exception {
		Vector partDesignvector = getAllLatestWTParts("Design", "");
		Vector partManufacturvector = getAllLatestWTParts("Manufacturing", "");
		System.out.println("partDesignvector size====" + partDesignvector.size());
		System.out.println("partManufacturvector size====" + partManufacturvector.size());
		Vector dataList = getnewpartoldpart(partDesignvector, partManufacturvector);
		System.out.println("dataList size====" + dataList.size());
		ExportEcadeferentItems exportEcadeferentItems = new ExportEcadeferentItems(dataList);
		exportEcadeferentItems.doExport(dataList);
	}

	// 导出目前系统新旧物料编码的统计
	public static void getnewpartOldpart2() throws Exception {
		Vector partDesignvector = getAllLatestWTParts("Design", "");
		Vector partManufacturvector = getAllLatestWTParts("Manufacturing", "");
		System.out.println("partDesignvector size====" + partDesignvector.size());
		System.out.println("partManufacturvector size====" + partManufacturvector.size());
		Vector dataList = getnewpartoldpart2(partDesignvector, partManufacturvector);
		System.out.println("dataList size====" + dataList.size());
		ExportEcadeferentItems exportEcadeferentItems = new ExportEcadeferentItems(dataList);
		exportEcadeferentItems.doExport(dataList);
	}

	// 导出绎立EBOM为空的bom
	public static void getYLXnoBom() throws Exception {
		Vector partDesignvector = getAllLatestWTParts("Design", "");
		System.out.println("partDesignvector size====" + partDesignvector.size());
		Vector dataList = getNoBomPart(partDesignvector);
		System.out.println("dataList size====" + dataList.size());
		ExportEcadeferentItems exportEcadeferentItems = new ExportEcadeferentItems(dataList);
		exportEcadeferentItems.doExport(dataList);
	}

	// 导出光峰bom和绎立同时使用到的的物料List<Map> list=new ArrayList<Map>();
	public static void getAPPOwithYLX() throws Exception {
		Vector partDesignvector = getAllLatestWTParts("Design", "");
		Vector partManufacturvector = getAllLatestWTParts("Manufacturing", "");
		System.out.println("partDesignvector size====" + partDesignvector.size());
		System.out.println("partManufacturvector size====" + partManufacturvector.size());
		Vector dataList = getPublicPart(partDesignvector, partManufacturvector);
		System.out.println("dataList size====" + dataList.size());
		ExportEcadeferentItems exportEcadeferentItems = new ExportEcadeferentItems(dataList);
		exportEcadeferentItems.doExport(dataList);
	}

	// 导出Excel表中的物料被那些成品使用;
	public static void getAPPOpartuseby() throws Exception {
		List<Map> list = new ArrayList<Map>();
		list = UpdateServerData.getExcelData();
		System.out.println("partDesignvector size====" + list.size());
		Vector dataList = getappouseby(list);
		System.out.println("dataList size====" + dataList.size());
		ExportEcadeferentItems exportEcadeferentItems = new ExportEcadeferentItems(dataList);
		exportEcadeferentItems.doExport(dataList);
	}

	// 导出，父件发布子件没有发布的物料
	public static void getRelasedpartwithArich() throws Exception {
		Vector partDesignvector = getAllLatestWTParts("Design", "");
		Vector partManufacturvector = getAllLatestWTParts("Manufacturing", "");
		System.out.println("partDesignvector size====" + partDesignvector.size());
		System.out.println("partManufacturvector size====" + partManufacturvector.size());
		Vector dataList = getArchivedPart(partDesignvector, partManufacturvector);
		ExportEcadeferentItems exportEcadeferentItems = new ExportEcadeferentItems(dataList);
		exportEcadeferentItems.doExport(dataList);
	}

	// 导出所有没有发布成功的bom
	public static void getNOTRelasedpart() throws Exception {
		Vector partDesignvector = getAllLatestWTParts("Design", "");
		Vector partManufacturvector = getAllLatestWTParts("Manufacturing", "");
		System.out.println("partDesignvector size====" + partDesignvector.size());
		System.out.println("partManufacturvector size====" + partManufacturvector.size());
		Vector dataList = getNOsetU8Part(partDesignvector, partManufacturvector);
		ExportEcadeferentItems exportEcadeferentItems = new ExportEcadeferentItems(dataList);
		exportEcadeferentItems.doExport(dataList);
	}

	// 导出所有没有电子签名成功的文档
	public static void getNOTprintd() throws Exception {
		// 获取所有文档
		Vector alldoc = getAllLatestWTDocs("");
		// 过滤得到没有电子签名的物料规格书，图纸
		System.out.println("all doc size===" + alldoc.size());
		Vector dataList = getSMnotprintdoc(alldoc);
		System.out.println("dataList  size===" + dataList.size());
		ExportEcadeferentItems exportEcadeferentItems = new ExportEcadeferentItems(dataList);
		exportEcadeferentItems.doExport(dataList);
	}

	public static Vector getSMnotprintdoc(Vector alldoc) {
		Vector docVector = new Vector();
		for (int i = 0; i < alldoc.size(); i++) {

			WTDocument doc = (WTDocument) alldoc.get(i);
			// 图纸，物料规格书
			String drawing = "com.plm.drawingdoc";
			String datasheet = "com.plm.datasheet";
			try {
				String DoctypeStr = CustHelper.service.getObjectType(doc);
				if (DoctypeStr.contains(datasheet) || DoctypeStr.contains(datasheet)) {
					QueryResult processResult = NmWorkflowHelper.service.getAssociatedProcesses(doc, null, null);
					if (processResult.size() > 0) {
						// 是否有电子签名
						QueryResult pdf = ContentHelper.service.getContentsByRole(doc, ContentRoleType.SECONDARY);
						if (pdf.size() == 0) {
							System.out.println("doc  ===" + doc.getNumber());
							ECAObject object = new ECAObject();
							object.setECAname(doc.getName());
							object.setECAnumber(doc.getNumber());
							docVector.addElement(object);
						} else {
							boolean isprint = false;
							while (pdf.hasMoreElements()) {
								ApplicationData data = (ApplicationData) pdf.nextElement();
								String name = data.getFileName();
								if (name.startsWith("PRINT_")) {
									isprint = true;
								}
							}
							if (!isprint) {
								ECAObject object = new ECAObject();
								object.setECAname(doc.getName());
								object.setECAnumber(doc.getNumber());
								docVector.addElement(object);
								System.out.println("doc  ===" + doc.getNumber());
							}
						}
					}
				}
			} catch (WTException e) {
				System.out.println("Debug>>AAA>>can not find the doc type!!!");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return docVector;
	}

	public static String getObjectType(Object object) throws WTException {
		String type = "";
		boolean flag = true;
		try {

			if (object != null) {
				TypeIdentifier ti = TypeIdentifierUtility.getTypeIdentifier(object);
				type = ti.getTypename();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return type;
	}

	public DataReportUtil() {
		// TODO Auto-generated constructor stub
	}

	// 得到所有的文档
	public static Vector getAllLatestWTDocs(String number) throws Exception {
		QuerySpec qs = new QuerySpec(WTDocument.class);
		if (number == null)
			return null;
		if (number.trim().length() > 0) {

			SearchCondition scNumber = new SearchCondition(WTDocument.class, WTDocument.NUMBER, SearchCondition.EQUAL,
					number.toUpperCase());
			qs.appendWhere(scNumber);
		}

		SearchCondition scLatestIteration = new SearchCondition(WTDocument.class, WTAttributeNameIfc.LATEST_ITERATION,
				SearchCondition.IS_TRUE);

		qs.appendWhere(scLatestIteration);

		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr != null && qr.hasMoreElements())
			qr = (new LatestConfigSpec()).process(qr);

		if (qr != null && qr.hasMoreElements())
			return qr.getObjectVectorIfc().getVector();

		return new Vector();
	}

	public static void doexport() {
		Vector partDesignvector;
		try {
			getNOTRelasedpart();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Vector getNOsetU8Part(Vector partvector, Vector partManufacturvector) throws WTException {
		if (partvector == null || partvector.size() == 0) {
			System.out.println("data is null!");
			return null;
		}
		DateFormat dateFormat;
		dateFormat = new SimpleDateFormat("yyyy/MM/dd", SessionHelper.getLocale());
		Vector datavector = new Vector();
		for (int i = 0; i < partvector.size(); i++) {
			WTPart part = (WTPart) partvector.get(i);
			if (part.getState().toString().equalsIgnoreCase("ARCHIVED")
					|| part.getState().toString().equalsIgnoreCase("RELEASED")) {
				// 已发布，已归档的，没有发送物料或者bom成功的，走过归档或发布流程
				if (part.getNumber().startsWith("A") || part.getNumber().startsWith("B")) {
					// 是否有bom结构
					// 获取单层子件
					WTCollection childrens = PIPartHelper.service.findChildren(part);
					if (childrens.size() > 0) {

						QueryResult processResult = NmWorkflowHelper.service.getAssociatedProcesses(part, null, null);
						while (processResult.hasMoreElements()) {
							WfProcess process = (WfProcess) processResult.nextElement();

							if (process.getName().contains("ERP集成")) {
								Timestamp createtime = process.getCreateTimestamp();
								String date1 = dateFormat.format(createtime);
								if (compare_date(date1, "2019/01/24")) {
									String bomflag = getIBAvalue(part, "bomReleaseStatus");
									String partflag = getIBAvalue(part, "partReleaseStatus");
									if (bomflag == null || bomflag.length() == 0 || partflag == null
											|| partflag.length() == 0) {
										ECAObject object = new ECAObject();
										object.setECAnumber(part.getNumber());
										object.setECAname(part.getName());
										object.setECA_state(part.getState().toString());
										object.setECA_type(date1);
										datavector.add(object);
									}
									break;
								}
							}
						}
					}
				}
			}

		}
		for (int i = 0; i < partManufacturvector.size(); i++) {
			WTPart part = (WTPart) partManufacturvector.get(i);
			if (part.getState().toString().equalsIgnoreCase("ARCHIVED")
					|| part.getState().toString().equalsIgnoreCase("RELEASED")) {
				// 已发布，已归档的，没有发送物料或者bom成功的，走过归档或发布流程
				if (part.getNumber().startsWith("A") || part.getNumber().startsWith("B")) {
					// 是否有bom结构
					// 获取单层子件
					WTCollection childrens = PIPartHelper.service.findChildren(part);
					if (childrens.size() > 0) {

						QueryResult processResult = NmWorkflowHelper.service.getAssociatedProcesses(part, null, null);
						while (processResult.hasMoreElements()) {
							WfProcess process = (WfProcess) processResult.nextElement();

							if (process.getName().contains("ERP集成")) {
								Timestamp createtime = process.getCreateTimestamp();
								String date1 = dateFormat.format(createtime);
								if (compare_date(date1, "2019/01/20")) {

									String bomflag = getIBAvalue(part, "bomReleaseStatus");
									String partflag = getIBAvalue(part, "partReleaseStatus");
									if (bomflag == null || bomflag.length() == 0 || partflag == null
											|| partflag.length() == 0) {
										ECAObject object = new ECAObject();
										object.setECAnumber(part.getNumber());
										object.setECAname(part.getName());
										object.setECA_state(part.getState().toString());
										object.setECA_type(date1);
										datavector.add(object);
									}
									break;
								}
							}
						}
					}
				}

			}
		}
		return datavector;
	}

	// 比较时间,date1大于等于date2返回true！
	public static boolean compare_date(String date1, String date2) {
		boolean flag = false;
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd");

		try {
			Date dt1 = df.parse(date1);
			Date dt2 = df.parse(date2);
			if (dt1.getTime() >= dt2.getTime()) {
				flag = true;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}

	public static Vector getArchivedPart(Vector partvector, Vector partManufacturvector) throws WTException {
		if (partvector == null || partvector.size() == 0) {
			System.out.println("data is null!");
			return null;
		}
		Vector datavector = new Vector();
		for (int i = 0; i < partvector.size(); i++) {
			WTPart part = (WTPart) partvector.get(i);
			if (part.getState().toString().equalsIgnoreCase("ARCHIVED")) {

				// 获取所有对象的父件
				QueryResult qr = WTPartHelper.service.getUsedByWTParts((WTPartMaster) part.getMaster());
				while (qr.hasMoreElements()) {
					WTPart parentpart = (WTPart) qr.nextElement();
					if (parentpart.getState().toString().equalsIgnoreCase("RELEASED")) {
						ECAObject object = new ECAObject();
						object.setECAnumber(part.getNumber());
						object.setECAname(part.getName());
						object.setECA_state(part.getState().toString());
						object.setECN_number(parentpart.getNumber());
						object.setECN_name(parentpart.getName());
						object.setECN_state(parentpart.getState().toString());

						// 获取对象所有关联的ECA对象
						QueryResult eCResult = null;
						eCResult = ChangeHelper2.service.getAffectingChangeActivities(part);
						System.out.println("eCResult size=====" + eCResult.size());
						while (eCResult.hasMoreElements()) {
							WTChangeActivity2 changeActivity2 = (WTChangeActivity2) eCResult.nextElement();
							System.out.println("eca numner=====" + changeActivity2.getNumber());
							WTChangeOrder2 order2 = ChangeUtils.getEcnByEca((WTChangeActivity2) changeActivity2);
							System.out.println("order2 numner=====" + order2.getNumber());
							System.out.println("eca name===" + changeActivity2.getNumber() + changeActivity2.getName());
							if ((!ChangeUtils.checkState(order2, ChangeConstants.CANCELLED))
									&& (!ChangeUtils.checkState(order2, ChangeConstants.RESOLVED))) {

								object.setECA_change_type(order2.getNumber());
							}

						}

						QueryResult processResult = NmWorkflowHelper.service.getAssociatedProcesses(parentpart, null,
								null);
						while (processResult.hasMoreElements()) {
							WfProcess process = (WfProcess) processResult.nextElement();

							if (process.getName().startsWith("APPO_Release")) {
								object.setECA_before_affectitems(process.getName());
								;
							}
						}
						datavector.add(object);
					}

				}
			}

		}
		for (int i = 0; i < partManufacturvector.size(); i++) {
			WTPart part = (WTPart) partManufacturvector.get(i);
			if (part.getState().toString().equalsIgnoreCase("ARCHIVED")) {
				// 获取所有对象的父件
				QueryResult qr = WTPartHelper.service.getUsedByWTParts((WTPartMaster) part.getMaster());
				while (qr.hasMoreElements()) {
					WTPart parentpart = (WTPart) qr.nextElement();
					if (parentpart.getState().toString().equalsIgnoreCase("RELEASED")) {
						ECAObject object = new ECAObject();
						object.setECAnumber(part.getNumber());
						object.setECAname(part.getName());
						object.setECA_state(part.getState().toString());
						object.setECN_number(parentpart.getNumber());
						object.setECN_name(parentpart.getName());
						object.setECN_state(parentpart.getState().toString());
						datavector.add(object);
					}

				}
			}

		}
		return datavector;
	}

	public static Vector getNoBomPart(Vector partvector) throws WTException {
		if (partvector == null || partvector.size() == 0) {
			System.out.println("data is null!");
			return null;
		}
		Vector datavector = new Vector();
		for (int i = 0; i < partvector.size(); i++) {

			WTPart part = (WTPart) partvector.get(i);
			if (part.getContainerName().startsWith("SL-舞台灯产品库")) {

				if (part.getNumber().startsWith("A") || part.getNumber().startsWith("B")) {
					if (part.getState().toString().startsWith("RELEASED")
							|| part.getState().toString().startsWith("ARCHIVED")) {

						QueryResult usedByWTParts = WTPartHelper.service.getUsesWTPartMasters(part);
						if (usedByWTParts == null || usedByWTParts.size() == 0) {
							ECAObject object = new ECAObject();
							object.setECAnumber(part.getNumber());
							object.setECAname(part.getName());
							object.setECA_state(part.getState().toString());
							object.setECA_change_type(part.getModifierFullName());
							object.setECA_type(part.getCreateTimestamp().toString());

							datavector.add(object);
						}
					}
				}

			}
		}
		return datavector;
	}

	public static QueryResult getParts(String number) throws WTException {
		StatementSpec stmtSpec = new QuerySpec(WTPart.class);
		WhereExpression where = new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL,
				number.toUpperCase());
		QuerySpec querySpec = (QuerySpec) stmtSpec;
		querySpec.appendWhere(where, new int[] { 0 });
		QueryResult qr = PersistenceServerHelper.manager.query(stmtSpec);
		return qr;
	}

	public static Vector getnewpartoldpart(Vector partvector, Vector partManufacturvector) throws WTException {
		if (partvector == null || partvector.size() == 0) {
			System.out.println("data is null!");
			return null;
		}
		DateFormat dateFormat;
		dateFormat = new SimpleDateFormat("yyyy/MM/dd", SessionHelper.getLocale());
		Vector datavector = new Vector();
		for (int i = 0; i < partvector.size(); i++) {

			WTPart part = (WTPart) partvector.get(i);

			if (part.getNumber().startsWith("A") || part.getNumber().startsWith("B") || part.getNumber().startsWith("C")
					|| part.getNumber().startsWith("E") || part.getNumber().startsWith("P")
					|| part.getNumber().startsWith("K") || part.getNumber().startsWith("S")
					|| part.getNumber().startsWith("T") || part.getNumber().startsWith("X")
					|| part.getNumber().startsWith("H")) {
				ECAObject object = new ECAObject();
				String creator = part.getCreatorName();
				QueryResult partqr = VersionControlHelper.service.allIterationsOf(part.getMaster());
				while (partqr.hasMoreElements()) {
					WTPart oldpart = (WTPart) partqr.nextElement();
					String oldversion = oldpart.getVersionIdentifier().getValue() + "."
							+ oldpart.getIterationIdentifier().getValue();

					if (oldversion.startsWith("A.1")) {
						String oldcreator = oldpart.getCreatorName();
						object.setECA_after_affectitems(oldcreator);
						if (oldcreator.startsWith("Administrator")) {
							object.setECA_type("旧编码");
						} else {
							object.setECA_type("新编码");
						}
						break;
					}

				}

				object.setECAnumber(part.getNumber());
				object.setECAname(part.getName());
				object.setECA_state(part.getState().toString());
				object.setECA_change_type(creator);
				object.setECN_create_time(part.getCreateTimestamp().toString());
				object.setECA_new_state(part.getContainerName());
				object.setECN_creator(part.getFolderPath());

				datavector.add(object);
			}
		}
		for (int i = 0; i < partManufacturvector.size(); i++) {

			WTPart part = (WTPart) partManufacturvector.get(i);
			if (part.getNumber().startsWith("A") || part.getNumber().startsWith("B") || part.getNumber().startsWith("C")
					|| part.getNumber().startsWith("E") || part.getNumber().startsWith("P")
					|| part.getNumber().startsWith("K") || part.getNumber().startsWith("S")
					|| part.getNumber().startsWith("T") || part.getNumber().startsWith("X")
					|| part.getNumber().startsWith("H")) {
				String creator = part.getCreatorName();

				ECAObject object = new ECAObject();
				QueryResult partqr = VersionControlHelper.service.allIterationsOf(part.getMaster());
				while (partqr.hasMoreElements()) {
					WTPart oldpart = (WTPart) partqr.nextElement();
					String oldversion = oldpart.getVersionIdentifier().getValue() + "."
							+ oldpart.getIterationIdentifier().getValue();

					if (oldversion.startsWith("A.1")) {
						String oldcreator = oldpart.getCreatorName();
						object.setECA_after_affectitems(oldcreator);
						if (oldcreator.startsWith("Administrator")) {
							object.setECA_type("旧编码");
						} else {
							object.setECA_type("新编码");
						}
						break;
					}

				}
				object.setECAnumber(part.getNumber());
				object.setECAname(part.getName());
				object.setECA_state(part.getState().toString());
				object.setECA_change_type(creator);
				object.setECN_create_time(part.getCreateTimestamp().toString());
				object.setECA_new_state(part.getContainerName());
				object.setECN_creator(part.getFolderPath());

				datavector.add(object);

			}
		}
		return datavector;
	}

	public static Vector getnewpartoldpart2(Vector partvector, Vector partManufacturvector) throws WTException {
		if (partvector == null || partvector.size() == 0) {
			System.out.println("data is null!");
			return null;
		}
		DateFormat dateFormat;
		dateFormat = new SimpleDateFormat("yyyy/MM/dd", SessionHelper.getLocale());
		Vector datavector = new Vector();
		for (int i = 0; i < partvector.size(); i++) {

			WTPart part = (WTPart) partvector.get(i);

			if (!part.getNumber().startsWith("A") && !part.getNumber().startsWith("B")
					&& !part.getNumber().startsWith("C") && !part.getNumber().startsWith("E")
					&& !part.getNumber().startsWith("P") && !part.getNumber().startsWith("K")
					&& !part.getNumber().startsWith("S") && !part.getNumber().startsWith("T")
					&& !part.getNumber().startsWith("X") && !part.getNumber().startsWith("H")
					&& !part.getNumber().startsWith("Q")) {
				ECAObject object = new ECAObject();
				String creator = part.getCreatorName();
				QueryResult partqr = VersionControlHelper.service.allIterationsOf(part.getMaster());
				while (partqr.hasMoreElements()) {
					WTPart oldpart = (WTPart) partqr.nextElement();
					String oldversion = oldpart.getVersionIdentifier().getValue() + "."
							+ oldpart.getIterationIdentifier().getValue();

					if (oldversion.startsWith("A.1")) {
						String oldcreator = oldpart.getCreatorName();
						object.setECA_after_affectitems(oldcreator);
						if (oldcreator.startsWith("Administrator")) {
							object.setECA_type("旧编码");
						} else {
							object.setECA_type("新编码");
						}
						break;
					}

				}

				object.setECAnumber(part.getNumber());
				object.setECAname(part.getName());
				object.setECA_state(part.getState().toString());
				object.setECA_change_type(creator);
				object.setECN_create_time(part.getCreateTimestamp().toString());
				object.setECA_new_state(part.getContainerName());
				object.setECN_creator(part.getFolderPath());

				datavector.add(object);
			}
		}
		for (int i = 0; i < partManufacturvector.size(); i++) {

			WTPart part = (WTPart) partManufacturvector.get(i);
			if (!part.getNumber().startsWith("A") && !part.getNumber().startsWith("B")
					&& !part.getNumber().startsWith("C") && !part.getNumber().startsWith("E")
					&& !part.getNumber().startsWith("P") && !part.getNumber().startsWith("K")
					&& !part.getNumber().startsWith("S") && !part.getNumber().startsWith("T")
					&& !part.getNumber().startsWith("X") && !part.getNumber().startsWith("H")
					&& !part.getNumber().startsWith("Q")) {
				String creator = part.getCreatorName();

				ECAObject object = new ECAObject();
				QueryResult partqr = VersionControlHelper.service.allIterationsOf(part.getMaster());
				while (partqr.hasMoreElements()) {
					WTPart oldpart = (WTPart) partqr.nextElement();
					String oldversion = oldpart.getVersionIdentifier().getValue() + "."
							+ oldpart.getIterationIdentifier().getValue();

					if (oldversion.startsWith("A.1")) {
						String oldcreator = oldpart.getCreatorName();
						object.setECA_after_affectitems(oldcreator);
						if (oldcreator.startsWith("Administrator")) {
							object.setECA_type("旧编码");
						} else {
							object.setECA_type("新编码");
						}
						break;
					}

				}
				object.setECAnumber(part.getNumber());
				object.setECAname(part.getName());
				object.setECA_state(part.getState().toString());
				object.setECA_change_type(creator);
				object.setECN_create_time(part.getCreateTimestamp().toString());
				object.setECA_new_state(part.getContainerName());
				object.setECN_creator(part.getFolderPath());

				datavector.add(object);

			}
		}
		return datavector;
	}

	public static Vector getappouseby(List<Map> datalist) throws WTException {
		if (datalist == null || datalist.size() == 0) {
			System.out.println("data is null!");
			return null;
		}
		Vector datavector = new Vector();
		for (Map map2 : datalist) {
			Map<Persistable, Map> map = new HashMap<>();
			String partnumber = "";
			String type = "";
			partnumber = (String) map2.get("partnumber");
			type = (String) map2.get("type");
			WTPart part = PartUtil.getLastestWTPartByNumber(partnumber);
			if (part != null) {

				Set<WTPart> toppart = new HashSet<WTPart>();
				;
				getPartTopParentObject(part, toppart);

				List<String> containerlist = new ArrayList<>();
				List<String> partnumberlist = new ArrayList<>();
				String parentnumber = "";
				if (toppart != null) {
					Iterator<?> iterator = toppart.iterator();
					while (iterator.hasNext()) {
						WTPart parentPart = (WTPart) iterator.next();
						/*
						 * if
						 * (!containerlist.contains(parentPart.getContainerName(
						 * ))) {
						 * containerlist.add(parentPart.getContainerName()); }
						 */
						// System.out.println("parentPart.getContainerName()====="+parentPart.getContainerName());
						if (parentPart.getNumber().startsWith("A")
								&& !partnumberlist.contains(parentPart.getNumber())) {
							// parentnumber = parentPart.getNumber() + ";" +
							// parentnumber;
							partnumberlist.add(parentPart.getNumber());
							ECAObject object = new ECAObject();
							object.setECN_creator(part.getNumber());
							object.setECAname(part.getName());
							object.setECA_state(part.getState().toString());
							object.setECA_change_type(parentPart.getContainerName());
							object.setECA_type(parentPart.getNumber());
							object.setECA_before_affectitems(parentPart.getName());
							object.setECA_after_affectitems(getIBAvalue(parentPart, "ggms"));
							object.setECN_state(getIBAvalue(part, "ggms"));
							datavector.add(object);
						}

					}
				}
				System.out.println("containerlist =====" + containerlist.size());

			}
		}
		return datavector;
	}

	// 获取所有顶层父
	public static void getPartTopParentObject(WTPart child, Set<WTPart> allTopParents) throws WTException {
		QueryResult links = StructHelper.service.navigateUsedBy(child.getMaster(), WTPartUsageLink.class, true);
		while (links.hasMoreElements()) {
			Persistable p = (Persistable) links.nextElement();
			if (p instanceof WTPart) {
				WTPart parent = (WTPart) p;

				getPartTopParentObject(parent, allTopParents);

			}
		}
		if (links.size() == 0)
			allTopParents.add(child);
	}

	/***
	 * 批量查询上层父件
	 * 
	 * @param childPartArray
	 *            子件集合
	 * @return
	 * @throws WTException
	 */
	public static Map<WTPart, WTPartUsageLink> batchQueryParentParts(Collection<WTPart> childPartArray)
			throws WTException {
		Map<WTPart, WTPartUsageLink> parentMap = new HashMap<WTPart, WTPartUsageLink>();
		if (childPartArray == null || childPartArray.size() == 0) {
			return parentMap;
		}

		try {
			QuerySpec qs = new QuerySpec();
			qs.setAdvancedQueryEnabled(true);
			qs.setDescendantQuery(false);
			qs.setQueryLimit(-1);
			int linkIndex = qs.appendClassList(WTPartUsageLink.class, true);
			int partIndex = qs.appendClassList(WTPart.class, true);

			// 子件MasterID集合
			List<Long> masterIdArray = new ArrayList<Long>();
			// // 视图
			// View vw = null ;
			for (WTPart part : childPartArray) {
				masterIdArray.add(PersistenceHelper.getObjectIdentifier(part.getMaster()).getId());
				// if(vw == null){
				// vw = (View)part.getView().getObject() ;
				// }else{
				// if(!PersistenceHelper.isEquivalent(vw,
				// (View)part.getView().getObject())){
				// throw new WTException("部件视图不一致!") ;
				// }
				// }
			}
			// // 添加视图条件
			// appendView(qs, vw, partIndex) ;
			// 添加部件ID与Link父件相等条件
			// qs.appendAnd();
			String roleAObjectRefKeyId = ObjectToObjectLink.ROLE_AOBJECT_REF + "." + ObjectReference.KEY + "."
					+ ObjectIdentifier.ID;
			String partKeyId = WTPart.PERSIST_INFO + "." + PersistInfo.OBJECT_IDENTIFIER + "." + ObjectIdentifier.ID;
			SearchCondition sc = new SearchCondition(WTPartUsageLink.class, roleAObjectRefKeyId, WTPart.class,
					partKeyId);
			qs.appendWhere(sc, new int[] { linkIndex, partIndex });
			// 添加Link子件条件
			qs.appendAnd();
			String roleBObjectRefKeyId = ObjectToObjectLink.ROLE_BOBJECT_REF + "." + ObjectReference.KEY + "."
					+ ObjectIdentifier.ID;
			Long[] longAy = new Long[masterIdArray.size()];
			masterIdArray.toArray(longAy);
			ArrayExpression roleBObjectExpression = new ArrayExpression(longAy);
			ClassAttribute roleBObjectAttribute = new ClassAttribute(WTPartUsageLink.class, roleBObjectRefKeyId);
			sc = new SearchCondition(roleBObjectAttribute, SearchCondition.IN, roleBObjectExpression);
			qs.appendWhere(sc, new int[] { linkIndex });

			QueryResult qr = PersistenceServerHelper.manager.query(qs);
			while (qr.hasMoreElements()) {
				Object[] objectArray = (Object[]) qr.nextElement();
				if (objectArray != null && objectArray.length > 0) {
					parentMap.put((WTPart) objectArray[1], (WTPartUsageLink) objectArray[0]);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		}

		return parentMap;
	}

	/***
	 * 批量查询部件最顶层父件
	 * 
	 * @param childPartArray
	 *            子件集合
	 * @param topParentArray
	 *            顶层父件集合
	 * @throws WTException
	 */
	public static void batchQueryTopParentParts(Collection<WTPart> childPartArray, Collection<WTPart> topParentArray)
			throws WTException {
		if (childPartArray == null || childPartArray.size() == 0 || topParentArray == null) {
			return;
		}

		topParentArray.addAll(childPartArray);
		// 批量查询上层父件
		Map<WTPart, WTPartUsageLink> parentMap = batchQueryParentParts(childPartArray);
		// 清空子件集合
		childPartArray = new HashSet<WTPart>();
		for (Map.Entry<WTPart, WTPartUsageLink> entryMap : parentMap.entrySet()) {
			childPartArray.add(entryMap.getKey());
			// 判断对象是否存在子件，如果存在则移除
			WTPart childPart = null;
			for (WTPart parentPart : topParentArray) {
				if (PersistenceHelper.isEquivalent(parentPart.getMaster(), entryMap.getValue().getUses())) {
					childPart = parentPart;
					break;
				}
			}
			if (childPart != null) {
				topParentArray.remove(childPart);
			}
		}
		batchQueryTopParentParts(childPartArray, topParentArray);
	}

	/***
	 * 获取指定部件上最顶层复合要求的父件
	 * 
	 * @param itemOids
	 *            指定部件oid集合
	 * @return
	 */
	public static Collection<WTPart> collectAffectedEndItems(WTPart part) {

		Collection<WTPart> topParentArray = new HashSet<WTPart>();
		try {
			// 子件集合
			Collection<WTPart> childArray = new HashSet<WTPart>();

			childArray.add(part);

			// 父件集合
			JSONArray parentArray = new JSONArray();
			if (childArray.size() > 0) {

				batchQueryTopParentParts(childArray, topParentArray);
				for (WTPart parentPart : topParentArray) {
					if (AccessControlHelper.manager.hasAccess(SessionHelper.manager.getPrincipal(), parentPart,
							AccessPermission.READ)) {
						parentArray.put(PersistenceHelper.getObjectIdentifier(parentPart).toString());
					}
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();

		}
		return topParentArray;
	}

	public static Vector getPublicPart(Vector partvector, Vector partManufacturvector) throws WTException {
		if (partvector == null || partvector.size() == 0) {
			System.out.println("data is null!");
			return null;
		}
		Vector datavector = new Vector();
		for (int i = 0; i < partvector.size(); i++) {

			WTPart part = (WTPart) partvector.get(i);
			QueryResult usedByWTParts = WTPartHelper.service.getUsedByWTParts(part.getMaster());
			List<String> containerlist = new ArrayList<>();
			String parentnumber = "";
			while (usedByWTParts.hasMoreElements()) {

				WTPart parentPart = (WTPart) usedByWTParts.nextElement();
				// System.out.println("parentPart.getContainerName()====="+parentPart.getContainerName());
				if (parentPart.getContainerName().startsWith("SL-舞台灯产品库")) {
					parentnumber = parentPart.getNumber() + ";" + parentnumber;
				}
				if (!containerlist.contains(parentPart.getContainerName())) {
					containerlist.add(parentPart.getContainerName());
				}
			}

			if (containerlist.contains("SL-舞台灯产品库") && containerlist.size() >= 2) {
				System.out.println("containerlist =====" + containerlist.size());
				ECAObject object = new ECAObject();
				object.setECAnumber(part.getNumber());
				object.setECAname(part.getName());
				object.setECA_state(part.getState().toString());
				object.setECA_change_type(containerlist.toString());
				object.setECA_type(parentnumber);

				datavector.add(object);

			}

		}
		for (int i = 0; i < partManufacturvector.size(); i++) {

			WTPart part = (WTPart) partManufacturvector.get(i);
			QueryResult usedByWTParts = WTPartHelper.service.getUsedByWTParts(part.getMaster());
			List<String> containerlist = new ArrayList<>();
			String parentnumber = "";
			while (usedByWTParts.hasMoreElements()) {

				WTPart parentPart = (WTPart) usedByWTParts.nextElement();
				// System.out.println("parentPart.getContainerName()====="+parentPart.getContainerName());
				if (!containerlist.contains(parentPart.getContainerName())) {
					containerlist.add(parentPart.getContainerName());
				}
				if (parentPart.getContainerName().startsWith("SL-舞台灯产品库")) {
					parentnumber = parentPart.getNumber() + ";" + parentnumber;
				}
			}
			if (containerlist.contains("SL-舞台灯产品库") && containerlist.size() >= 2) {
				System.out.println("containerlist =====" + containerlist.size());
				ECAObject object = new ECAObject();
				object.setECAnumber(part.getNumber());
				object.setECAname(part.getName());
				object.setECA_state(part.getState().toString());
				object.setECA_change_type(containerlist.toString());
				object.setECA_type(parentnumber);

				datavector.add(object);
				break;
			}
		}

		return datavector;
	}

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

	/**
	 * 通过对象获取流程
	 * 
	 * @param obj
	 *            流程中业务对象
	 * @return WfProcess 返回工作流对象
	 * @throws Exception
	 */

	public static WfProcess getProcess(Object obj) {
		if (obj == null)
			return null;
		try {
			Persistable persistable = null;
			if (obj instanceof ObjectIdentifier) {
				persistable = PersistenceHelper.manager.refresh((ObjectIdentifier) obj);// 获取业务对象
			} else if (obj instanceof ObjectReference)// 通过Reference获取对象
			{
				persistable = ((ObjectReference) obj).getObject();
			}
			if (persistable instanceof WorkItem)// 通过workitem获取流程
			{
				persistable = ((WorkItem) persistable).getSource().getObject();
			}
			if (persistable instanceof WfActivity)// 通过WfActivity获取流程
			{
				persistable = ((WfActivity) persistable).getParentProcess();
			}
			if (persistable instanceof WfConnector)// 通过WfConnector获取流程
			{
				persistable = ((WfConnector) persistable).getParentProcessRef().getObject();
			}
			if (persistable instanceof WfBlock)// 通过WfBlock获取流程
			{
				persistable = ((WfBlock) persistable).getParentProcess();
			}
			if (persistable instanceof WfProcess)// 转换成流程对象
			{
				return (WfProcess) persistable;
			} else
				return null;
		} catch (Exception e) {
			System.out.println("PLMMUtil.getProcess : error");
			e.printStackTrace();
			return null;
		}
	}

	// 检查特定替代料状态
	public static void checkallSubpart(List<WTObject> errorList, WTPart part) {
		QueryResult qr = null;
		try {
			qr = WTPartHelper.service.getUsesWTPartMasters(part);
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (qr != null) {
			System.out.println("usagelinksize====" + qr.size());
			while (qr.hasMoreElements()) {
				WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
				PartWorkflowUtil.checkSubpart(link, errorList);
			}
		} else {
			System.out.println("usagelink qr size====00");
		}
	}

	public static String getCollectionDisplayInfo(Collection<WTObject> collection) {
		StringBuilder message = new StringBuilder();
		if (collection == null) {
			message.append("collection == null");
			return message.toString();
		} else {
			Iterator ite = collection.iterator();

			while (ite.hasNext()) {
				message.append("\n");
				message.append(IdentityFactory.getDisplayIdentifier(ite.next()));
			}

			return message.toString();
		}
	}

	public static String getIBAvalue(Persistable p, String key) throws PIException {
		Object object = PIAttributeHelper.service.getValue(p, key);
		System.out.println("object====" + object);
		String comment = "";
		if (object == null) {
			return comment;
		}
		if (object instanceof String) {
			String changeComment = (String) PIAttributeHelper.service.getValue(p, key);
			comment = changeComment;
		}
		if (object instanceof Object[]) {
			Object[] objArr = (Object[]) object;
			for (int i = 0; i < objArr.length; i++) {
				comment = comment + objArr[i].toString() + "  ";
			}
		}
		if (object instanceof Boolean) {
			comment = object.toString();
		} else {
			comment = object.toString();
		}
		System.out.println("commnet =========" + comment);
		return comment;
	}
}
