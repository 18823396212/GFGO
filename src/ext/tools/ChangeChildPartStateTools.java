package ext.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import ext.appo.ecn.util.AffectedMaterialsUtil;
import ext.appo.erp.service.PartReleaseService;
import ext.appo.erp.util.BomUtil;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.httpgw.GatewayAuthenticator;
import wt.lifecycle.LifeCycleException;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.State;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.query.QuerySpec;
import wt.util.WTException;

/** *********************************************************************** */

/**mao
/*解决成品为已发布状态，但子料还为“已归档”的情况；同时抛送子料到K3 。*/
/*废弃状态物料暂不处理，正在审阅及正在工作暂不处理  */
/** *********************************************************************** */

public class ChangeChildPartStateTools implements RemoteAccess {

	private static final Logger LOGGER = Logger.getLogger(ChangeChildPartStateTools.class);

	public static void main(String[] args) {

		System.out.println("功能：已发布成品下子件状态刷新");
		System.out.println("示例：windchill ext.tools.ChangeChildPartState 部件编码(成品编码)");

		try {

			RemoteMethodServer rms = RemoteMethodServer.getDefault();
			GatewayAuthenticator auth = new GatewayAuthenticator();
			auth.setRemoteUser("wcadmin");
			rms.setAuthenticator(auth);

			// RemoteMethodServer.getDefault().setUserName(args[0]);
			// RemoteMethodServer.getDefault().setPassword(args[1]);

			String partNumbers = "";
			if (args.length == 1)
				partNumbers = args[0];

			System.out.println("命令执行开发start");
			// Class[] argTypes = { String.class };
			// Object[] svrArgs = { partNumbers };

			// String result = (String)
			// RemoteMethodServer.getDefault().invoke("execute",
			// ChangeChildPartStateTools.class.getName(), null, argTypes,
			// svrArgs);
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
		// 获取A开头的所有已经发布的最新成品部件
		List<WTPart> endPartList = getAllWTPartMaster(partNumbers);
		// 获取所有的子件
		Set<WTPart> childPartList = getAllChildPart(endPartList);

		for (WTPart part : childPartList) {
			// 子件为已经归档
			if ("ARCHIVED".equals(part.getState().toString())) {
				System.out.println("number==" + part.getNumber() + " view===" + part.getViewName() + " vision=="
						+ part.getVersionInfo().getIdentifier().getValue() + "."
						+ part.getIterationInfo().getIdentifier().getValue());
				// 更新状态
				setLifecycleState(part, "RELEASED");
				// 抛送
				PartReleaseService.sendMaterial(part);
			}
		}

		// } else {
		// Class[] argTypes = { String.class };
		// Object[] svrArgs = { partNumbers };
		//
		// result = (String) RemoteMethodServer.getDefault().invoke("execute",
		// ChangeChildPartStateTools.class.getName(), null, argTypes, svrArgs);
		// }
		return result;
	}

	public static void setLifecycleState(WTPart part, String stateValue) throws LifeCycleException, WTException {

		// 设置部件相应的生命周期状态
		State state = State.toState(stateValue);
		LifeCycleHelper.service.setLifeCycleState(part, state);
	}

	/**
	 * 查询所有为A开头的，已经发布的最新物料
	 * 
	 * @return
	 * @throws WTException
	 */
	public static List<WTPart> getAllWTPartMaster(String partNumbers) throws WTException {

		List<WTPart> result = new ArrayList<WTPart>();

		if ("".equals(partNumbers) || partNumbers == null) {
			// 获取所有A开头的物料，且已经发布的最新物料
			QuerySpec qs = new QuerySpec(WTPartMaster.class);
			QueryResult qr = PersistenceHelper.manager.find(qs);
			while (qr.hasMoreElements()) {
				WTPartMaster wtPartMaster = (WTPartMaster) qr.nextElement();
				if (wtPartMaster.getNumber().startsWith("A")) {
					WTPart newParentPart = (WTPart) AffectedMaterialsUtil.getLatestVersionByMaster(wtPartMaster);
					if ("RELEASED".equals(newParentPart.getState().toString())) {
						result.add(newParentPart);
					}
				}
			}
		} else {
			// 多个使用,隔开,暂时不处理
			String[] partNumberArr = partNumbers.split(",");
		}

		return result;
	}

	/**
	 * 
	 * 获取成品下的所有子物料
	 * 
	 * @param endPartList
	 *            已经发布的成品
	 * @throws WTException
	 */
	private static Set<WTPart> getAllChildPart(List<WTPart> endPartList) throws WTException {
		HashSet<WTPart> wtPartSet = new HashSet<WTPart>();
		for (WTPart endParentPart : endPartList) {
			BomUtil.getAllChildLatestPart(endParentPart, wtPartSet);
		}

		return wtPartSet;
	}
}
