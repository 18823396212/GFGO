package ext.tools;

import java.util.ArrayList;
import java.util.List;

import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.workflow.NmWorkflowHelper;

import wt.change2.ChangeRecord2;
import wt.change2.WTChangeActivity2;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.httpgw.GatewayAuthenticator;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.pds.StatementSpec;
import wt.pom.PersistenceException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfState;

public class PartComparisonTools {

	public static void main(String[] args) {
		System.out.println("功能：PLM跟K3Part比较工具");
		System.out.println("示例：windchill ext.tools.PartComparisonTools");

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

		if (InvokeK3Tools2.LoginK3()) {
			System.out.println("login success");

			// 导出报表的数据
			List<List<String>> datalist = new ArrayList<List<String>>();
			List<List<String>> k3List = InvokeK3Tools2.queryAllPart();
			for (List<String> list : k3List) {
				String partNumber = list.get(0);
				String version = list.get(1);
				WTPart lastWTPart = getLastestWTPartByNumber(partNumber);
				if (lastWTPart == null) {
					continue;
				}
				if ("OBSOLESCENCE".equals(lastWTPart.getLifeCycleState().toString())) {
					continue;
				}
				WTPart plmpart = getPart(lastWTPart.getMaster());

				if (plmpart.getName().contains("测试") || plmpart.getName().contains("test")
						|| plmpart.getName().contains("Test")) {
					continue;
				}

				if ("OBSOLESCENCE".equals(plmpart.getLifeCycleState().toString())) {
					continue;
				}

				List<String> partList = new ArrayList<String>();
				partList.add(partNumber);
				partList.add("");
				partList.add(plmpart.getVersionIdentifier().getValue());
				partList.add(version);
				partList.add(plmpart.getLifeCycleState().toString());
				if (version.equals(plmpart.getVersionIdentifier().getValue())) {
					partList.add("1");
				} else {
					partList.add("0");
				}
				datalist.add(partList);
			}
			ExportPartItems2 exportPartItems = new ExportPartItems2(datalist);
			exportPartItems.doExport(datalist);

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

	public static WTPart getPart(WTPartMaster master) throws PersistenceException, WTException {

		WTPart part = null;

		QueryResult qrVersions = VersionControlHelper.service.allVersionsOf(master);

		while (qrVersions.hasMoreElements()) {
			part = (WTPart) qrVersions.nextElement();
			String version = part.getVersionIdentifier().getValue();
			String lifeCycleState = part.getLifeCycleState().toString();
			if ("RELEASED".equals(lifeCycleState)) {
				return part;
			} else if ("ARCHIVED".equals(lifeCycleState)) {
				return part;
			} else if (("INWORK".equals(lifeCycleState) || "REWORK".equals(lifeCycleState)) && "A".equals(version)) {
				return part;
			} else if (("INWORK".equals(lifeCycleState) || "REWORK".equals(lifeCycleState))) {
				continue;
			} else {
				if (isHaveECA(part) || isRunningGuiDangWorkflow(part)) {
					continue;
				} else {
					return part;
				}
			}

		}

		return part;
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

}
