package ext.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.ptc.windchill.csm.common.CsmConstants;

import ext.appo.ecn.report.ExportEcadeferentItems;
import ext.appo.ecn.report.Object.ECAObject;
import ext.pi.PIException;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIClassificationHelper;
import wt.facade.classification.ClassificationFacade;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.httpgw.GatewayAuthenticator;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.vc.config.LatestConfigSpec;
import wt.vc.views.View;
import wt.vc.views.ViewHelper;

public class DataReportDianrongTools1 implements RemoteAccess {

	private static final Logger LOGGER = Logger.getLogger(DataReportDianrongTools1.class);

	public static void main(String[] args) {

		System.out.println("功能：导出电容信息");
		System.out.println("示例：windchill ext.tools.DataReportDianrongTools");

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
		// if (RemoteMethodServer.ServerFlag) {
		// if ("".equals(partNumbers) || partNumbers == null) {
		Vector partDesignvector = getAllLatestWTParts("Design", "");
		Vector partManufacturvector = getAllLatestWTParts("Manufacturing", "");
		System.out.println("partDesignvector size====" + partDesignvector.size());
		System.out.println("partManufacturvector size====" + partManufacturvector.size());
		Vector dataList = getEpart(partDesignvector, partManufacturvector);
		System.out.println("dataList size====" + dataList.size());
		ExportEcadeferentItems exportEcadeferentItems = new ExportEcadeferentItems(dataList);
		exportEcadeferentItems.doExport(dataList);
		// }
		// } else {
		// Class[] argTypes = { String.class };
		// Object[] svrArgs = { partNumbers };
		//
		// result = (String) RemoteMethodServer.getDefault().invoke("execute",
		// ChangeChildPartState.class.getName(),
		// null, argTypes, svrArgs);
		// }
		return result;
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
				if (nodeHierarchy.contains("E0200000")) {
					ECAObject object = new ECAObject();
					ClassificationFacade facadeInstance = ClassificationFacade.getInstance();
					String localizedName = facadeInstance.getLocalizedDisplayNameForClassificationNode(value,
							CsmConstants.NAMESPACE, new Locale("en"));
					object.setECN_number(part.getNumber()); // 编号
					object.setECN_name(part.getName()); // 名称
					object.setECN_create_time(localizedName); // 小类名称
					object.setECN_creator(getIBAvalue(part, "ggms"));// 规格描述
					object.setECN_state(getIBAvalue(part, "csxh"));// 厂商型号
					object.setECAnumber(getIBAvalue(part, "wxcc"));// 外形尺寸
					object.setECAname(getIBAvalue(part, "cs"));// 厂商
					object.setECA_state(getIBAvalue(part, "hbsx"));// 环保属性
					object.setECA_type(getIBAvalue(part, "yxdj"));// 优选等级
					object.setECA_before_affectitems(getIBAvalue(part, "cz"));// 材质
					object.setECA_after_affectitems(getIBAvalue(part, "rz"));// 容值
					object.setECA_before_affectitemsnumber(getIBAvalue(part, "jd"));// 精度
					object.setECA_before_affectitemsname(getIBAvalue(part, "eddy"));// 额定电压
					object.setECA_before_affectitemsstate(getIBAvalue(part, "fzlx"));// 封装类型
					object.setECA_before_version(getIBAvalue(part, "edgzwd"));// 额度工作温度
					object.setECA_change_type(facadeInstance.getLocalizedDisplayNameForClassificationNode(
							part.getState().toString(), CsmConstants.NAMESPACE, new Locale("en")));// 状态

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
				if (nodeHierarchy.contains("E0200000")) {
					ECAObject object = new ECAObject();
					ClassificationFacade facadeInstance = ClassificationFacade.getInstance();
					String localizedName = facadeInstance.getLocalizedDisplayNameForClassificationNode(value,
							CsmConstants.NAMESPACE, new Locale("en"));
					object.setECN_number(part.getNumber()); // 编号
					object.setECN_name(part.getName()); // 名称
					object.setECN_create_time(localizedName); // 小类名称
					object.setECN_creator(getIBAvalue(part, "ggms"));// 规格描述
					object.setECN_state(getIBAvalue(part, "csxh"));// 厂商型号
					object.setECAnumber(getIBAvalue(part, "wxcc"));// 外形尺寸
					object.setECAname(getIBAvalue(part, "cs"));// 厂商
					object.setECA_state(getIBAvalue(part, "hbsx"));// 环保属性
					object.setECA_type(getIBAvalue(part, "yxdj"));// 优选等级
					object.setECA_before_affectitems(getIBAvalue(part, "cz"));// 材质
					object.setECA_after_affectitems(getIBAvalue(part, "rz"));// 容值
					object.setECA_before_affectitemsnumber(getIBAvalue(part, "jd"));// 精度
					object.setECA_before_affectitemsname(getIBAvalue(part, "eddy"));// 额定电压
					object.setECA_before_affectitemsstate(getIBAvalue(part, "fzlx"));// 封装类型
					object.setECA_before_version(getIBAvalue(part, "edgzwd"));// 额度工作温度
					object.setECA_change_type(facadeInstance.getLocalizedDisplayNameForClassificationNode(
							part.getState().toString(), CsmConstants.NAMESPACE, new Locale("en")));// 状态

					datavector.add(object);
				}
			}
		}
		return datavector;
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
