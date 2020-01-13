package ext.appo.ecn.pdf;

import java.util.*;

import com.ptc.core.lwc.server.LWCNormalizedObject;
import com.ptc.windchill.enterprise.change2.ChangeTaskRoleParticipantHelper;

import ext.appo.change.ModifyHelper;
import ext.appo.change.models.CorrelationObjectLink;
import ext.appo.change.models.TransactionTask;
import ext.appo.change.util.ModifyUtils;
import ext.appo.ecn.beans.ChangeTaskBean;
import ext.appo.ecn.common.util.ChangePartQueryUtils;
import ext.appo.ecn.common.util.ChangeUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.lang.PIStringUtils;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PICoreHelper;
import org.apache.bcel.generic.IF_ACMPEQ;
import org.apache.commons.lang.StringUtils;
import wt.change2.AffectedActivityData;
import wt.change2.ChangeActivityIfc;
import wt.change2.ChangeHelper2;
import wt.change2.Changeable2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.configurablelink.ConfigurableDescribeLink;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTReference;
import wt.lifecycle.State;
import wt.org.WTGroup;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.project.Role;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.team.Team;
import wt.util.WTException;
import wt.workflow.engine.*;
import wt.workflow.work.WorkItem;
import wt.workflow.work.WorkflowHelper;

import static ext.appo.change.constants.ModifyConstants.*;

public class PdfUtil {
	public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";

	public static Persistable getObjectByOid(String oid) throws WTException {
		Persistable p = null;

		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		try {
			ReferenceFactory referencefactory = new ReferenceFactory();
			WTReference wtreference = referencefactory.getReference(oid);
			p = wtreference.getObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SessionServerHelper.manager.setAccessEnforced(flag);
		}

		return p;
	}

	/**
	 * GET OID BY PERSISTABLE
	 *
	 * @param p
	 * @return
	 * @throws WTException
	 */
	public static String getOidByObject(Persistable p) {
		String oid = "";
		if (p instanceof WfProcess) {
			oid = "OR:wt.workflow.engine.WfProcess:" + p.getPersistInfo().getObjectIdentifier().getId();
			return oid;
		}
		if (p != null) {
			oid = "OR:" + p.toString();
		}
		return oid;
	}

	public static PdfBean initPdfBean(WTChangeOrder2 ecn) {
		PdfBean bean = new PdfBean(ecn);
		return bean;
	}

	public static PdfBean testPdfBean(WTChangeOrder2 eco) throws WTException {
		String ecnoid = getOidByObject(eco);
		WTChangeOrder2 ecn = (WTChangeOrder2) PdfUtil.getObjectByOid(ecnoid);
		PdfBean bean = new PdfBean(ecn);
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false); // 忽略权限
		try {

			Collection<Persistable> returnArray = new HashSet<Persistable>();
			//add by lzy at 20200110 start
			String templateName="";
			Map<ChangeActivityIfc, Collection<Changeable2>> ecaDatasMap =new HashMap<>();
			QueryResult result = WfEngineHelper.service.getAssociatedProcesses(ecn, null, null);
			while (result.hasMoreElements()) {
				WfProcess process = (WfProcess) result.nextElement();
				if (process != null) {
					templateName = process.getTemplate().getName();
				}
			}
			//受影响对象
			if ("APPO_ECNWF".equals(templateName)){
//				Set<CorrelationObjectLink> links = ModifyHelper.service.queryCorrelationObjectLinks(ecn, LINKTYPE_1);
//				for (CorrelationObjectLink link : links) {
//					Persistable persistable = link.getPersistable();
//					returnArray.add(persistable);
//				}
				Set<Persistable> Persistables=ModifyHelper.service.queryPersistable(ecn, LINKTYPE_1);
				for (Persistable persistable : Persistables) {
					returnArray.add(persistable);
				}
			}else{
				ecaDatasMap = getChangeablesBeforeInfo(ecn);
				for (ChangeActivityIfc ca : ecaDatasMap.keySet()) {
					Collection<Changeable2> cl = ecaDatasMap.get(ca);
					for (Persistable per : cl) {
						returnArray.add(per);
					}
				}
			}
			//add by lzy at 20200110 end

//			Map<ChangeActivityIfc, Collection<Changeable2>> ecaDatasMap = getChangeablesBeforeInfo(ecn);
//			for (ChangeActivityIfc ca : ecaDatasMap.keySet()) {
//				Collection<Changeable2> cl = ecaDatasMap.get(ca);
//				for (Persistable per : cl) {
//					returnArray.add(per);
//				}
//			}

			if (!returnArray.isEmpty()) {
				List<AffectedItemBean> aibs = new ArrayList<>();
				for (Persistable per : returnArray) {
					if (per instanceof WTPart) {
						WTPart part = (WTPart) per;
						AffectedItemBean aib = new AffectedItemBean();
						aib.setName(part.getName()); // 名称
						aib.setNumber(part.getNumber()); // 编号

						String version = part.getVersionInfo().getIdentifier().getValue();
						String iteration = part.getIterationInfo().getIdentifier().getValue();
						aib.setVersion(version + "." + iteration); // 版本

						State s = State.toState(part.getState().toString());
						String partState = s.getDisplay(SessionHelper.getLocale());
						aib.setState(partState == null ? "" : partState); // 状态

						Object partTypeName = PdfUtil.getIBAObjectValue(part, "ChangeType");
						String typeName = partTypeName == null ? "" : partTypeName.toString();
						String[] typeArr = typeName.split(";");
						if (typeArr.length > 1) // mao add
							aib.setType(typeArr[1]); // 类型
						else
							aib.setType(typeName);

						String des = "";
						Collection<ChangeActivityIfc> changeActivities = ChangeUtils.getChangeActivities(eco);
						for (ChangeActivityIfc changeActivityIfc : changeActivities) {
							AffectedActivityData affectedActivityData = ChangeUtils
									.getAffectedActivity(changeActivityIfc, part);
							if (affectedActivityData != null) {
								des = affectedActivityData.getDescription();
							}
						}
						aib.setDes(des == null ? "" : des); // 说明

						String articleInventory = (String) PdfUtil.getIBAObjectValue(part, "ArticleInventory");
						aib.setInQuantity(articleInventory == null ? "" : articleInventory); // 在制数量

						Object articleDispose = PdfUtil.getIBAObjectValue(part, "ArticleDispose");
						String inTreatment = articleDispose == null ? "" : articleDispose.toString();
						String[] articleArr = inTreatment.split(";");
						if (articleArr.length > 1)
							aib.setInTreatment(articleArr[1]); // 在制处理措施
						else {
							aib.setInTreatment(inTreatment);
						}

						String passageInventory = (String) PdfUtil.getIBAObjectValue(part, "PassageInventory");
						aib.setWayQuantity(passageInventory == null ? "" : passageInventory); // 在途数量

						Object passageDispose = PdfUtil.getIBAObjectValue(part, "PassageDispose");
						String wayTreatment = passageDispose == null ? "" : passageDispose.toString();
						String[] passageArr = wayTreatment.split(";");
						if (passageArr.length > 1)
							aib.setWayTreatment(passageArr[1]); // 在途处理措施
						else
							aib.setWayTreatment(wayTreatment);

						String centralWarehouseInventory = (String) PdfUtil.getIBAObjectValue(part,
								"CentralWarehouseInventory");
						aib.setStockQuantity(centralWarehouseInventory == null ? "" : centralWarehouseInventory); // 库存数量

						Object inventoryDispose = PdfUtil.getIBAObjectValue(part, "InventoryDispose");
						String stockTreatment = inventoryDispose == null ? "" : inventoryDispose.toString();
						String[] inventoryArr = stockTreatment.split(";");
						if (inventoryArr.length > 1)
							aib.setStockTreatment(inventoryArr[1]); // 库存处理措施
						else {
							aib.setStockTreatment("");
						}

						Object productDispose = PdfUtil.getIBAObjectValue(part, "ProductDispose");
						String shipmentsTreatment = productDispose == null ? "" : productDispose.toString();
						String[] productArr = shipmentsTreatment.split(";");

						if (productArr.length > 1)
							aib.setShipmentsTreatment(productArr[1]); // 已出货处理措施
						else {
							aib.setShipmentsTreatment("");
						}

						String completionTime = (String) PdfUtil.getIBAObjectValue(part, "CompletionTime");
						aib.setExpectDate(completionTime == null ? "" : completionTime); // 期望完成时间

						Object responsiblePerson = PdfUtil.getIBAObjectValue(part, "ResponsiblePerson");
						String personLiable = responsiblePerson == null ? "" : responsiblePerson.toString();

						String count = personLiable.replaceAll("\\d+", "");
						String[] arry = count.split("\\|");

						if (arry.length > 0)
							aib.setPersonLiable(arry[0]); // 责任人
						else {
							aib.setPersonLiable("");
						}

						String collecObj = getChildsNumber(part, eco);
						aib.setCollectObj(collecObj == null ? "" : collecObj); // 收集对象

						aibs.add(aib);
					} else if (per instanceof WTDocument) {
						WTDocument doc = (WTDocument) per;

						AffectedItemBean aib = new AffectedItemBean();
						aib.setName(doc.getName()); // 名称
						aib.setNumber(doc.getNumber()); // 编号

						String version = doc.getVersionInfo().getIdentifier().getValue();
						String iteration = doc.getIterationInfo().getIdentifier().getValue();
						aib.setVersion(version + "." + iteration); // 版本

						State s = State.toState(doc.getState().toString());
						String partState = s.getDisplay(SessionHelper.getLocale());
						aib.setState(partState == null ? "" : partState); // 状态

						aib.setType("升版"); // 类型

						String des = "";
						Collection<ChangeActivityIfc> changeActivities = ChangeUtils.getChangeActivities(eco);
						for (ChangeActivityIfc changeActivityIfc : changeActivities) {
							AffectedActivityData affectedActivityData = ChangeUtils
									.getAffectedActivity(changeActivityIfc, doc);
							if (affectedActivityData != null) {
								des = affectedActivityData.getDescription();
							}
						}
						aib.setDes(des == null ? "" : des); // 说明

						aib.setInQuantity(""); // 在制数量
						aib.setInTreatment(""); // 在制处理措施
						aib.setWayQuantity(""); // 在途数量
						aib.setWayTreatment(""); // 在途处理措施
						aib.setStockQuantity(""); // 库存数量
						aib.setStockTreatment(""); // 库存处理措施
						aib.setShipmentsTreatment(""); // 已出货处理措施

						String completionTime = (String) PdfUtil.getIBAObjectValue(doc, "CompletionTime");
						aib.setExpectDate(completionTime == null ? "" : completionTime); // 期望完成时间

						Object responsiblePerson = PdfUtil.getIBAObjectValue(doc, "ResponsiblePerson");
						String personLiable = responsiblePerson == null ? "" : responsiblePerson.toString();

						String count = personLiable.replaceAll("\\d+", "");
						String[] arry = count.split("\\|");
						aib.setPersonLiable(arry[0]); // 责任人

						aib.setCollectObj(""); // 收集对象

						aibs.add(aib);
					} else if (per instanceof EPMDocument) {
						EPMDocument epm = (EPMDocument) per;

						AffectedItemBean aib = new AffectedItemBean();
						aib.setName(epm.getName()); // 名称
						aib.setNumber(epm.getNumber()); // 编号

						String version = epm.getVersionInfo().getIdentifier().getValue();
						String iteration = epm.getIterationInfo().getIdentifier().getValue();
						aib.setVersion(version + "." + iteration); // 版本

						State s = State.toState(epm.getState().toString());
						String partState = s.getDisplay(SessionHelper.getLocale());
						aib.setState(partState == null ? "" : partState); // 状态

						aib.setType("升版"); // 类型

						String des = "";
						Collection<ChangeActivityIfc> changeActivities = ChangeUtils.getChangeActivities(eco);
						for (ChangeActivityIfc changeActivityIfc : changeActivities) {
							AffectedActivityData affectedActivityData = ChangeUtils
									.getAffectedActivity(changeActivityIfc, epm);
							if (affectedActivityData != null) {
								des = affectedActivityData.getDescription();
							}
						}
						aib.setDes(des == null ? "" : des); // 说明

						aib.setInQuantity(""); // 在制数量
						aib.setInTreatment(""); // 在制处理措施
						aib.setWayQuantity(""); // 在途数量
						aib.setWayTreatment(""); // 在途处理措施
						aib.setStockQuantity(""); // 库存数量
						aib.setStockTreatment(""); // 库存处理措施
						aib.setShipmentsTreatment(""); // 已出货处理措施

						// String completionTime =
						// (String)PdfUtil.getIBAObjectValue(epm,
						// "CompletionTime");
						aib.setExpectDate(""); // 期望完成时间
						//
						// Object responsiblePerson =
						// PdfUtil.getIBAObjectValue(epm, "ResponsiblePerson");
						// String personLiable =responsiblePerson.toString() ==
						// null ? "" : responsiblePerson.toString();
						//
						// String count = personLiable.replaceAll("\\d+","");
						// String[] arry = count.split("\\|");
						aib.setPersonLiable(""); // 责任人

						aib.setCollectObj(""); // 收集对象
						aibs.add(aib);
					}
				}
				bean.setAibs(aibs);
			}

			//add by lzy at 20200110 start
			//受影响产品
			if ("APPO_ECNWF".equals(templateName)){
//				Set<CorrelationObjectLink> links = ModifyHelper.service.queryCorrelationObjectLinks(ecn, LINKTYPE_1);
//				for (CorrelationObjectLink link : links) {
//					Persistable persistable = link.getPersistable();
//				}
				Set<Persistable> Persistables=ModifyHelper.service.queryPersistable(ecn, LINKTYPE_1);
				List<AffectedProductBean> apbs = new ArrayList<>();
				for (Persistable persistable : Persistables) {
					if (persistable instanceof WTPart){
						AffectedProductBean apb = new AffectedProductBean((WTPart) persistable);
						apbs.add(apb);
					}
				}
				bean.setApbs(apbs);
			}else{
				Collection<WTPart> datasArray = getEndItemsByChangeOrder2(ecn);
				if (!datasArray.isEmpty()) {
					List<AffectedProductBean> apbs = new ArrayList<>();
					for (WTPart part : datasArray) {
						AffectedProductBean apb = new AffectedProductBean(part);
						apbs.add(apb);
					}
					bean.setApbs(apbs);
				}
			}
			//add by lzy at 20200110 end

//			Collection<WTPart> datasArray = getEndItemsByChangeOrder2(ecn);
//			if (!datasArray.isEmpty()) {
//				List<AffectedProductBean> apbs = new ArrayList<>();
//				for (WTPart part : datasArray) {
//					AffectedProductBean apb = new AffectedProductBean(part);
//					apbs.add(apb);
//				}
//				bean.setApbs(apbs);
//			}

			//add by lzy at 20200110 start
			//事务性任务
			Collection<ChangeTaskBean> generateChangeTaskBeans;
			if ("APPO_ECNWF".equals(templateName)){
				generateChangeTaskBeans=getChangeTaskBeans(ecn);
			}else{
				generateChangeTaskBeans=generateChangeTaskBeans(ecn);
			}
			//add by lzy at 20200110 end

//			Collection<ChangeTaskBean> generateChangeTaskBeans = generateChangeTaskBeans(ecn);

			if (!generateChangeTaskBeans.isEmpty()) {
				List<Map<String, String>> list = new ArrayList<>();
				for (ChangeTaskBean changeTaskBean : generateChangeTaskBeans) {
					Map<String, String> map = new HashMap<>();
					map.put("taskTitle", changeTaskBean.getChangeTheme());
					map.put("taskDesc", changeTaskBean.getChangeDescribe());
					map.put("taskEndTime", changeTaskBean.getNeedDate());
					map.put("taskPersion", changeTaskBean.getResponsible());
					list.add(map);
				}
				bean.setSwxrw(list);
			}

			QueryResult qr = WorkflowHelper.service.getWorkItems(ecn);
			List<ReviewBean> rbs = new ArrayList<>();
			while (qr.hasMoreElements()) {
				WorkItem wi = (WorkItem) qr.nextElement();
				ReviewBean rb = new ReviewBean(wi);
				rbs.add(rb);
			}
			bean.setRbs(rbs);
		} finally {
			SessionServerHelper.manager.setAccessEnforced(flag); // 添加权限
		}
		return bean;
	}

	/***
	 * 根据ChangeOrder2对象查询关联的成品对象
	 * 
	 * @param paramComponentParams
	 * @return
	 */
	public static Collection<WTPart> getEndItemsByChangeOrder2(WTChangeOrder2 eco) {
		Collection<WTPart> datasArray = new HashSet<WTPart>();

		try {
			QueryResult qr = PersistenceHelper.manager.navigate(eco, ConfigurableDescribeLink.DESCRIBED_BY_ROLE,
					ConfigurableDescribeLink.class, true);
			while (qr.hasMoreElements()) {
				Object value = qr.nextElement();
				if (value instanceof WTPart) {
					datasArray.add((WTPart) value);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		return datasArray;
	}

	/***
	 * 将ECN中所有ECA对象转换为ChangeTaskBean对象
	 * 
	 * @param params
	 * @return
	 * @throws WTException
	 */
	public static Collection<ChangeTaskBean> generateChangeTaskBeans(WTChangeOrder2 eco) throws WTException {
		Collection<ChangeTaskBean> datasArray = new HashSet<ChangeTaskBean>();
		try {
			// 将ECN中所有ECA对象转换为ChangeTaskBean对象
			for (ChangeActivityIfc changeActivityIfc : ChangeUtils.getChangeActivities(eco)) {
				WTChangeActivity2 eca = (WTChangeActivity2) changeActivityIfc;
				// TODO 类型判断是否为‘事务性任务’类型
				if (!PICoreHelper.service.isType(eca, ChangeConstants.TRANSACTIONAL_CHANGEACTIVITY2)) {
					continue;
				}
				// ‘已取消’状态不录入
				if (ChangeUtils.checkState(eca, ChangeConstants.CANCELLED)) {
					continue;
				}
				ChangeTaskBean changeTaskBean = new ChangeTaskBean();
				changeTaskBean.setChangeTheme(eca.getName());
				changeTaskBean.setChangeDescribe(eca.getDescription());
				// ECA工作负责人
				String assigneeName = "";
				Enumeration<?> roleem = ChangeTaskRoleParticipantHelper.getRoleParticipants(eca,
						ChangeConstants.ROLE_ASSIGNEE);
				while (roleem.hasMoreElements()) {
					Object object = roleem.nextElement();
					if (object instanceof ObjectReference) {
						object = ((ObjectReference) object).getObject();
					}
					WTPrincipal principal = (WTPrincipal) object;

					if (principal instanceof WTUser) {
						if (PIStringUtils.isNull(assigneeName)) {
							assigneeName = ((WTUser) principal).getFullName();
						} else {
							assigneeName = assigneeName + ";" + ((WTUser) principal).getFullName();
						}
					}
				}
				changeTaskBean.setResponsible(assigneeName);
				changeTaskBean.setChangeActivity2(PersistenceHelper.getObjectIdentifier(eca).toString());
				Object needDate = PIAttributeHelper.service.getValue(eca, ChangeConstants.COMPLETION_TIME);
				changeTaskBean.setNeedDate(needDate == null ? "" : (String) needDate);
				datasArray.add(changeTaskBean);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		}

		return datasArray;
	}

	public static Object getIBAObjectValue(Persistable theObject, String attributeName) {
		Object o;
		try {
			LWCNormalizedObject genericObj = new LWCNormalizedObject(theObject, null, null, null);
			genericObj.load(attributeName);
			o = genericObj.get(attributeName);
		} catch (WTException e) {
			e.printStackTrace();
			String str = "获取属性出错！";
			o = str;
		}
		return o;
	}

	/**
	 * 从工作流中获取指定角色的用户
	 */
	@SuppressWarnings("rawtypes")
	public static List<WTUser> getUsers(WfProcess process, Role targetRole) throws WTException {
		List<WTUser> result = new ArrayList<WTUser>();
		Team team = (Team) (process.getTeamId().getObject());

		Enumeration enumPrin = team.getPrincipalTarget(targetRole);
		while (enumPrin.hasMoreElements()) {
			WTPrincipalReference pr = (WTPrincipalReference) enumPrin.nextElement();
			WTPrincipal pri = pr.getPrincipal();
			if (pri instanceof WTUser) {
				result.add((WTUser) pri);
			} else if (pri instanceof WTGroup) {
				WTGroup gp = (WTGroup) pri;
				getGroupMemberUsers(gp, result);
			}
		}

		return result;
	}

	public static void getGroupMemberUsers(WTGroup group, List<WTUser> userList) throws WTException {
		Enumeration groupMembers = group.members();
		while (groupMembers.hasMoreElements()) {
			WTPrincipal principal = (WTPrincipal) groupMembers.nextElement();
			if (principal instanceof WTUser) {
				WTUser user = (WTUser) principal;
				if (!userList.contains(user)) {
					userList.add(user);
				}
			}
			if (principal instanceof WTGroup) {
				WTGroup thegroup = (WTGroup) principal;
				getGroupMemberUsers(thegroup, userList);
			}
		}
	}

	public static WfProcess getProcess(Object obj) {
		if (obj == null) {
			return null;
		}
		try {
			Persistable persistable = null;
			if (obj instanceof Persistable) {
				persistable = (Persistable) obj;
			} else if (obj instanceof ObjectIdentifier) {
				persistable = PersistenceHelper.manager.refresh((ObjectIdentifier) obj);
			} else if (obj instanceof ObjectReference) {
				persistable = ((ObjectReference) obj).getObject();
			}
			if (persistable == null) {
				return null;
			}
			if (persistable instanceof WorkItem) {
				persistable = ((WorkItem) persistable).getSource().getObject();
			}
			if (persistable instanceof WfActivity) {
				persistable = ((WfActivity) persistable).getParentProcess();
			}
			if (persistable instanceof WfConnector) {
				persistable = ((WfConnector) persistable).getParentProcessRef().getObject();
			}
			if (persistable instanceof WfBlock) {
				persistable = ((WfBlock) persistable).getParentProcess();
			}
			if (persistable instanceof WfProcess) {
				return (WfProcess) persistable;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/***
	 * 获取存在受影响对象列表的部件编码
	 * 
	 * @param paramModelContext
	 * @param parentPart
	 * @return
	 * @throws WTException
	 */
	public static String getChildsNumber(WTPart parentPart, WTChangeOrder2 eco) throws WTException {
		StringBuilder returnStr = new StringBuilder();
		Collection<WTPart> childArray = ChangePartQueryUtils.getPartMultiwallStructure(parentPart);
		if (childArray == null || childArray.size() == 0) {
			return returnStr.toString();
		}

		Collection<Changeable2> changeablesBefore = ChangeUtils.getChangeablesBefore(eco);
		for (Changeable2 changeable2 : changeablesBefore) {
			if (changeable2 instanceof WTPart) {
				for (WTPart childPart : childArray) {
					if (ChangeUtils.getNumber(changeable2).equals(childPart.getNumber())) {
						if (returnStr.length() > 0) {
							returnStr.append(ChangeConstants.USER_KEYWORD);
						}
						returnStr.append(childPart.getNumber());
						break;
					}
				}
			}
		}
		return returnStr.toString();
	}

	/***
	 * 获取ECN中所有ECA与受影响对象
	 *
	 * @param ecn
	 * @return
	 * @throws WTException
	 */
	public static Map<ChangeActivityIfc, Collection<Changeable2>> getChangeablesBeforeInfo(WTChangeOrder2 ecn)
			throws WTException {
		Map<ChangeActivityIfc, Collection<Changeable2>> datasMap = new HashMap<ChangeActivityIfc, Collection<Changeable2>>();
		if (ecn == null) {
			return datasMap;
		}

		try {
			// 获取ECN中所有ECA对象
			Collection<ChangeActivityIfc> ecaArray = ChangeUtils.getChangeActivities(ecn);
			for (ChangeActivityIfc changeActivityIfc : ecaArray) {

				if (changeActivityIfc instanceof WTChangeActivity2) {

					if (ChangeUtils.checkState((WTChangeActivity2) changeActivityIfc, "CANCELLED")) {
						continue;
					}

				}

				// 受影响对象集合
				Collection<Changeable2> datasArray = new HashSet<Changeable2>();
				// 获取ECA中所有受影响对象
				QueryResult qr = ChangeHelper2.service.getChangeablesBefore(changeActivityIfc);
				while (qr.hasMoreElements()) {
					Object object = qr.nextElement();
					if (object instanceof ObjectReference) {
						object = ((ObjectReference) object).getObject();
					}
					if (object instanceof Changeable2) {
						datasArray.add((Changeable2) object);
					}
				}
				datasMap.put(changeActivityIfc, datasArray);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		}

		return datasMap;
	}


	/***
	 * 将新ECN流程APPO_ECNWF中所有事务性任务转换为ChangeTaskBean对象
	 *
	 * @param params
	 * @return
	 * @throws WTException
	 */
	public static Collection<ChangeTaskBean> getChangeTaskBeans(WTChangeOrder2 eco) throws WTException {
		Collection<ChangeTaskBean> datasArray = new HashSet<ChangeTaskBean>();
		Set<CorrelationObjectLink> links = ModifyHelper.service.queryCorrelationObjectLinks(eco, LINKTYPE_3);
		for (CorrelationObjectLink link : links) {
			Persistable persistable = link.getPersistable();
			if (persistable instanceof TransactionTask) {
				TransactionTask task = (TransactionTask) persistable;

				ChangeTaskBean changeTaskBean = new ChangeTaskBean();
				changeTaskBean.setChangeTheme(task.getChangeTheme());
				changeTaskBean.setChangeDescribe(task.getChangeDescribe());
				changeTaskBean.setResponsible(task.getResponsible());
				changeTaskBean.setChangeActivity2(String.valueOf(task.getPersistInfo().getObjectIdentifier().getId()));
				changeTaskBean.setNeedDate(task.getNeedDate());
				datasArray.add(changeTaskBean);
			}
		}

		return datasArray;
	}


}
