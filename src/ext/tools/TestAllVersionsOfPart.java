package ext.tools;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.httpgw.GatewayAuthenticator;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;

public class TestAllVersionsOfPart implements RemoteAccess {

	public static void main(String[] args) {
		System.out.println("功能版本的显示打印工具");
		System.out.println("示例：windchill ext.tools.TestAllVersionsOfPart A15000000015");

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
		WTPart part = getLastestWTPartByNumber(partNumbers);

		QueryResult qrVersions = VersionControlHelper.service.allVersionsOf(part.getMaster());
		System.out.println("2222222qrVersions size" + qrVersions.size());
		while (qrVersions.hasMoreElements()) {
			WTPart t = (WTPart) qrVersions.nextElement();
			String version = t.getVersionInfo().getIdentifier().getValue() + "."
					+ t.getIterationInfo().getIdentifier().getValue();// 物料版本
			System.out.println("number=" + t.getNumber() + "  version=" + version);
		}
		return result;
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

}
