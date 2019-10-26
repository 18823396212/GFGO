package ext.tools;

import java.util.Vector;

import ext.pi.PIException;
import ext.pi.core.PIAttributeHelper;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.httpgw.GatewayAuthenticator;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.vc.config.LatestConfigSpec;

public class PartReportUtil implements RemoteAccess {

	public static void main(String[] args) throws Exception {
		// windchill ext.appo.tools.PartReportUtil
		RemoteMethodServer rms = RemoteMethodServer.getDefault();
		GatewayAuthenticator auth = new GatewayAuthenticator();
		auth.setRemoteUser("wcadmin");
		rms.setAuthenticator(auth);

		System.out.println("开始查询物料方法=====");
		getPartMessage();
	}

	// 导出物料信息
	// A开头物料（成品）最新版本信息
	public static void getPartMessage() throws Exception {

		Vector dataList = new Vector();
		Vector allMasterVector = getAllWTPartMaster();
		if (allMasterVector != null && allMasterVector.size() > 0) {
			for (int i = 0; i < allMasterVector.size(); i++) {
				String number = ((WTPartMaster) allMasterVector.get(i)).getNumber();
				String numberStr = number.substring(0, 1);
				if (numberStr.equals("A")) {
					WTPart part = getLastestWTPartByNumber(number);
					Vector partVector = getPart(part);
					dataList.add(partVector);

				}

			}
		}

		System.out.println("dataList size====" + dataList.size());
		ExportPartItems exportpartitems = new ExportPartItems(dataList);
		exportpartitems.doExport(dataList);
	}

	public static Vector getPart(WTPart part) throws PIException {

		Vector resultVector = new Vector();
		String number = part.getNumber();// 编码
		String mVersion = part.getVersionIdentifier().getValue();// 物料大版本
		String sscpx = getIBAvalue(part, "sscpx");// 所属产品线
		String nbxh = getIBAvalue(part, "nbxh");// 内部型号
		String xsxh = getIBAvalue(part, "xsxh");// 销售型号
		String brand = getIBAvalue(part, "brand");// 品牌
		String cpzt = getIBAvalue(part, "cpzt");// 产品状态
		String cpxl = getIBAvalue(part, "cpxl");// 产品系列
		String sfzycp = getIBAvalue(part, "sfzycp");// 是否自研成品

		resultVector.add(number);
		resultVector.add(sscpx);
		resultVector.add(mVersion);
		resultVector.add(nbxh);
		resultVector.add(xsxh);
		resultVector.add(brand);
		resultVector.add(cpzt);
		resultVector.add(cpxl);
		resultVector.add(sfzycp);

		return resultVector;
	}

	// 返回所有Master
	public static Vector getAllWTPartMaster() throws WTException {

		Vector result = new Vector();
		QuerySpec qs = new QuerySpec(WTPartMaster.class);

		QueryResult qr = PersistenceHelper.manager.find(qs);

		while (qr.hasMoreElements()) {
			WTPartMaster wtPartMaster = (WTPartMaster) qr.nextElement();

			result.add(wtPartMaster);
		}

		return result;
	}

	// 通过物料编码返回最新wtpart
	public static WTPart getLastestWTPartByNumber(String numStr) {
		try {
			QuerySpec queryspec = new QuerySpec(WTPart.class);

			queryspec.appendSearchCondition(
					new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL, numStr));
			QueryResult queryresult = PersistenceHelper.manager.find(queryspec);
			LatestConfigSpec cfg = new LatestConfigSpec();
			QueryResult qr = cfg.process(queryresult);
			if (qr.hasMoreElements()) {
				return (WTPart) qr.nextElement();
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return null;
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
}
