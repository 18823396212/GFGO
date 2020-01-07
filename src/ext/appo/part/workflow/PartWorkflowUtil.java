/**
 * <pre>
 * 修改记录：05
 * 修改日期：2019-11-01 
 * 修   改  人：毛兵义
 * 关联活动：
 * 修改内容：检查光峰的BOM归档发布时，结构中不能有小米定制的物料（除“激光产品库”“微投产品库”外）
 * </pre>
 */

/**
 * <pre>
 * 修改记录：06
 * 修改日期：2019-11-22 
 * 修   改  人：毛兵义
 * 关联活动：
 * 修改内容：去掉设计视图中不能包含制造视图的限制
 * </pre>
 */

/**
 * <pre>
 * 修改记录：07
 * 修改日期：2019-12-20 
 * 修   改  人：毛兵义
 * 关联活动：
 * 修改内容：D视图添加校验：分类为组件的下面不允许挂总成
 * </pre>
 */

/**
 * <pre>
 * 修改记录：08
 * 修改日期：2019-12-24 
 * 修   改  人：毛兵义
 * 关联活动：
 * 修改内容：是否小米定制以前使用公司，现在直接根据产品库来去除峰米
 * 修改内容：优选等级，新的BOM不能添加【禁选】的物料
 * </pre>
 */

package ext.appo.part.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.ptc.core.lwc.common.view.EnumerationDefinitionReadView;
import com.ptc.core.lwc.common.view.EnumerationMembershipReadView;
import com.ptc.core.lwc.common.view.PropertyHolderHelper;
import com.ptc.core.lwc.common.view.PropertyValueReadView;
import com.ptc.core.lwc.server.TypeDefinitionServiceHelper;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.workflow.NmWorkflowHelper;
import com.ptc.windchill.enterprise.change2.commands.RelatedChangesQueryCommands;
import com.ptc.windchill.enterprise.team.server.TeamCCHelper;

import ext.appo.ecn.common.util.ChangePartQueryUtils;
import ext.appo.ecn.common.util.ChangeUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.ecn.util.AffectedMaterialsUtil;
import ext.appo.part.beans.ProductLineConfigBean;
import ext.appo.part.processor.StartAppoPartArchiveIssueWF;
import ext.appo.util.PartUtil;
import ext.com.workflow.WorkflowUtil;
import ext.customer.common.MBAUtil;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.generic.partpromotion.util.PartReleasedWorkFlow;
import ext.generic.reviewObject.model.ProcessReviewObjectLink;
import ext.generic.reviewObject.model.ProcessReviewObjectLinkHelper;
import ext.generic.reviewObject.util.ReviewObjectAndWorkFlowLink;
import ext.lang.PIStringUtils;
import ext.pi.PIException;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIClassificationHelper;
import ext.pi.core.PICoreHelper;
import ext.pi.core.PIPartHelper;
import wt.access.AccessControlHelper;
import wt.access.AccessPermission;
import wt.access.AdHocAccessKey;
import wt.access.AdHocControlled;
import wt.change2.ChangeActivity2;
import wt.change2.ChangeActivityIfc;
import wt.change2.ChangeHelper2;
import wt.change2.ChangeRecord2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.clients.vc.CheckInOutTaskLogic;
import wt.doc.DocumentVersion;
import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectReference;
import wt.fc.ObjectVector;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTKeyedMap;
import wt.fc.collections.WTList;
import wt.inf.container.WTContainer;
import wt.inf.team.ContainerTeam;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.team.ContainerTeamManaged;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.org.OrganizationServicesHelper;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.PartDocHelper;
import wt.part.Quantity;
import wt.part.WTPart;
import wt.part.WTPartAlternateLink;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartSubstituteLink;
import wt.part.WTPartUsageLink;
import wt.pds.StatementSpec;
import wt.project.Role;
import wt.query.CompositeWhereExpression;
import wt.query.ConstantExpression;
import wt.query.LogicalOperator;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.TableColumn;
import wt.query.WhereExpression;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.team.Team;
import wt.team.WTRoleHolder2;
import wt.util.WTException;
import wt.util.WTMessage;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;
import wt.vc.views.View;
import wt.vc.wip.WorkInProgressException;
import wt.vc.wip.WorkInProgressHelper;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfBlock;
import wt.workflow.engine.WfConnector;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfState;
import wt.workflow.work.WorkItem;

public class PartWorkflowUtil extends PartReleasedWorkFlow {

	private static final String CLASSNAME = PartWorkflowUtil.class.getName();
	private static final Logger logger = LogR.getLogger(CLASSNAME);

	public PartWorkflowUtil(WTObject pbo, ObjectReference self) {
		if ((pbo != null) && (self != null)) {
			this.pbo = pbo;
			this.self = self;
		}
	}

	public PartWorkflowUtil() {
	}

	/**
	 * 软件部件发布，通知节点必须选择项目经理 *
	 * 
	 * @param object
	 * @return
	 * @throws WTException
	 */
	public void checksoftWarePart(WTObject object) throws WTException {
		if (object != null && object instanceof WTPart) {
			WTPart part = (WTPart) object;
			System.out.println("p number==" + part.getNumber());
			if (part.getNumber().startsWith("X")) {
				CheckTeam(object, "project_manager", "Receiver");
			}

		}
	}

	/**
	 * 得到初始状态
	 * 
	 * @param object
	 * @return
	 * @throws WTException
	 */
	public String getInitialstage(WTObject object) throws WTException {
		if (object != null && object instanceof WTPart) {
			WTPart part = (WTPart) object;
			if (part.getNumber().startsWith("X")) {

			}
			String state = part.getLifeCycleState().toString();
			return state;
		}
		return null;
	}

	/**
	 * 统计归档状态的子件
	 * 
	 * @author cjt
	 * @param part
	 * @return
	 * @throws WTException
	 */
	public void checkSonStateInReviewlist(WTObject pbo) throws WTException {
		// boolean stateIsOK = true;
		WTUser previous = (WTUser) SessionHelper.manager.getPrincipal();
		WTPart part = (WTPart) pbo;

		// 当前用户设置为管理员，用于忽略权限
		try {
			WTPrincipal wtadministrator = SessionHelper.manager.getAdministrator();
			// 取得当前用户
			SessionContext.setEffectivePrincipal(wtadministrator);
			WTPrincipalReference wtprincipalreference = (WTPrincipalReference) (new ReferenceFactory())
					.getReference(previous);
			AccessControlHelper.manager.addPermission((AdHocControlled) part, wtprincipalreference,
					AccessPermission.MODIFY_IDENTITY, AdHocAccessKey.WNC_ACCESS_CONTROL);
		} catch (WTException e1) {
			e1.printStackTrace();
		}
		// 获取单层子件
		// WTCollection childrens = PIPartHelper.service.findChildren(part);
		// 获取所有子件
		ArrayList<WTPart> childpartList = new ArrayList<WTPart>();
		String parentview = part.getViewName();
		try {
			StartAppoPartArchiveIssueWF.getBomallchildpart(part, childpartList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		SessionContext.setEffectivePrincipal(previous);
		// System.out.println("childpartList.size()===="+childpartList.size());
		WTArrayList partlist = new WTArrayList();
		for (int i = 0; i < childpartList.size(); i++) {
			WTPart son = (WTPart) childpartList.get(i);
			if (son.getState().toString().startsWith("ARCHIVED")) {

			}
		}

	}

	// 判断bom发布流程中，归档的子件是否都添加到随签列表中
	public List<String> getReviewparts(WTObject pbo, ObjectReference s) {
		List<String> reviewlist = new ArrayList<String>();
		try {
			// list = checkSonState((WTPart) pbo);
			// 获取随迁对象
			WTArrayList reviewObjectByProcess = ReviewObjectAndWorkFlowLink.getReviewObjectByProcess(s);
			for (int i = 0; i < reviewObjectByProcess.size(); i++) {
				WTObject obj = (WTObject) reviewObjectByProcess.getPersistable(i);
				if (obj instanceof WTPart) {
					WTPart part = (WTPart) obj;
					String state = ((LifeCycleManaged) obj).getLifeCycleState().toString();
					if (state.equals("ARCHIVED")) {
						reviewlist.add(part.getNumber());
					}
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return reviewlist;
	}

	// 检查ECN流程中受影响对象是否为最新版本，-mao
	public String checkAffectObjectVesionNew(WTObject pbo, ObjectReference self) {
		StringBuffer sb = new StringBuffer();

		if (pbo instanceof WTChangeOrder2) {

			try {
				// 获取更改通告中所有的受影响对象
				QueryResult ecaqr = ChangeHelper2.service.getChangeActivities((WTChangeOrder2) pbo);
				while (ecaqr.hasMoreElements()) {
					Object ecaobject = ecaqr.nextElement();
					if (ecaobject instanceof WTChangeActivity2) {
						WTChangeActivity2 eca = ((WTChangeActivity2) ecaobject);
						if (eca.getState().toString().equalsIgnoreCase("OPEN")) {
							// 查询ECA中所有产生对象
							QueryResult qr = ChangeHelper2.service.getChangeablesBefore(eca);
							System.out.println("before qr==" + qr.size());
							while (qr.hasMoreElements()) {
								Object object = (Object) qr.nextElement();

								if (object instanceof WTPart) {
									WTPart part = (WTPart) object;
									System.out.println("change part number ===" + part.getNumber());

									WTPart newParentPart = (WTPart) AffectedMaterialsUtil
											.getLatestVersionByMaster(part.getMaster());
									if (part.getViewName() != null
											&& part.getViewName().equals(newParentPart.getViewName())) {
										if (!AffectedMaterialsUtil.getOidByObject(part)
												.equals(AffectedMaterialsUtil.getOidByObject(newParentPart))
												|| !part.isLatestIteration()) {
											System.out.println("old version");
											sb.append("物料");
											sb.append(part.getNumber());
											sb.append(",");
										}
									}

								}
								if (object instanceof WTDocument) {
									WTDocument document = (WTDocument) object;

									WTDocument neWtDocument = (WTDocument) AffectedMaterialsUtil
											.getLatestVersionByMaster((WTDocumentMaster) document.getMaster());

									if (!AffectedMaterialsUtil.getOidByObject(document)
											.equals(AffectedMaterialsUtil.getOidByObject(neWtDocument))
											|| !document.isLatestIteration()) {
										sb.append("文档");
										sb.append(document.getNumber());
										sb.append(",");
									}

								}
							}
						}
					}
				}
				if (sb.toString() != null && sb.toString().length() > 0) {
					sb.append("在受影响对象中非最新版本，请使用最新版本进行变更。");
				}
			} catch (WTException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	// 判断ECN流程中是否强制选择会签人员
	public String CheckECNpartType(WTObject pbo, ObjectReference self) {
		List<String> isRDPart = new ArrayList<String>();
		if (pbo instanceof WTChangeOrder2) {

			try {
				// 获取更改通告中所有的受影响对象
				QueryResult ecaqr = ChangeHelper2.service.getChangeActivities((WTChangeOrder2) pbo);
				while (ecaqr.hasMoreElements()) {
					Object ecaobject = ecaqr.nextElement();
					if (ecaobject instanceof WTChangeActivity2) {
						WTChangeActivity2 eca = ((WTChangeActivity2) ecaobject);
						if (eca.getState().toString().equalsIgnoreCase("OPEN")) {
							// 查询ECA中所有产生对象
							QueryResult qr = ChangeHelper2.service.getChangeablesBefore(eca);
							System.out.println("before qr==" + qr.size());
							while (qr.hasMoreElements()) {
								Object object = (Object) qr.nextElement();

								if (object instanceof WTPart) {
									WTPart part = (WTPart) object;
									System.out.println("change part number ===" + part.getNumber());
									if (part.getNumber().startsWith("A") || part.getNumber().startsWith("C")
											|| part.getNumber().startsWith("D") || part.getNumber().startsWith("E")
											|| part.getNumber().startsWith("P") || part.getNumber().startsWith("T")
											|| part.getNumber().startsWith("S") || part.getNumber().startsWith("K")) {

										return "RD";
									}
									if (part.getNumber().startsWith("B")) {
										String value = (String) PIAttributeHelper.service.getValue(part,
												"Classification");
										String nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(value);
										if (nodeHierarchy.contains("appo_bcp22")) {// 软件总成
											isRDPart.add("NOTRD");
										} else {
											return "RD";
										}
									}
									if (part.getNumber().startsWith("X")) {
										isRDPart.add("NOTRD");
									}
									if (part.getNumber().startsWith("N") || part.getNumber().startsWith("M")) {
										return "NM";
									}
								}
							}
						}
					}
				}
			} catch (WTException e) {
				e.printStackTrace();
			}

		}
		System.out.println("is not RD part ==" + isRDPart.size());
		return "NOTRD";
	}

	// 检查bom中子件的单位和物料的单位是否一致
	public void CheckBOMUnit(ObjectReference self1) throws WTException {
		StringBuilder errorMsg = new StringBuilder();
		// WTArrayList reviewlist = new WTArrayList();
		// 获取随迁对象
		WTArrayList reviewObjectByProcess = ReviewObjectAndWorkFlowLink.getReviewObjectByProcess(self1);
		for (int i = 0; i < reviewObjectByProcess.size(); i++) {
			Persistable persistable = reviewObjectByProcess.getPersistable(i);
			if (persistable instanceof WTPart) {
				WTPart part = (WTPart) persistable;
				System.out.println("review  part=====" + part.getNumber());
				// 如果随签对象存在说明文档，正在工作状态下必须随签
				QueryResult docresult = getAssociatedDescribeDocuments(part);
				if (docresult != null) {
					while (docresult.hasMoreElements()) {
						WTDocument document = (WTDocument) docresult.nextElement();
						String state = document.getState().toString();
						if (state.equalsIgnoreCase("INWORK")) {
							if (!reviewObjectByProcess.contains(document)) {
								errorMsg.append(
										"部件:" + part.getNumber() + "下的说明文档：" + document.getNumber() + "必须加到随签对象中!");
								errorMsg.append("\n");
							}
						}
					}
				}

				QueryResult qr = WTPartHelper.service.getUsesWTPartMasters(part);
				while (qr.hasMoreElements()) {
					WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
					WTPartMaster master = link.getUses();
					WTPart childpart = PartUtil.getLastestWTPartByNumber(master.getNumber());
					String defaultunit = CommonPDMUtil.getUnitCN(childpart.getDefaultUnit());
					Quantity quantity = link.getQuantity();
					String unit = CommonPDMUtil.getUnitCN(quantity.getUnit());
					System.out.println("defaultunit==" + defaultunit);
					System.out.println("=======unit==" + unit);
					if (unit.contains("item") && defaultunit.contains("PCS")) {
					} else {
						if (!defaultunit.equalsIgnoreCase(unit)) {
							errorMsg.append(part.getNumber() + "的子件：" + master.getNumber() + "单位: " + unit + " ，与物料单位："
									+ defaultunit + "不符");
							errorMsg.append("\n");
						}
					}
				}

			}

		}
		if (errorMsg.length() > 0) {
			throw new WTException(errorMsg.toString());
		}

	}

	public static QueryResult getAssociatedDescribeDocuments(WTPart wtpart) throws WTException {
		WTArrayList wtarraylist = new WTArrayList();
		wtarraylist.add(wtpart);
		WTKeyedMap wtkeyedmap = PartDocHelper.service.getAssociatedDescribeDocuments(wtarraylist);
		WTCollection wtcollection = (WTCollection) wtkeyedmap.get(wtpart);
		return getDocs(wtcollection);
	}

	private static QueryResult getDocs(WTCollection wtcollection) {
		QueryResult queryresult = new QueryResult();
		try {
			if (wtcollection != null) {
				ObjectVector objectvector = new ObjectVector();
				DocumentVersion documentversion;
				for (Iterator iterator = wtcollection.persistableIterator(); iterator.hasNext(); objectvector
						.addElement(documentversion))
					documentversion = (DocumentVersion) iterator.next();

				queryresult.appendObjectVector(objectvector);
			}
		} catch (WTException wtexception) {
			wtexception.printStackTrace();
		}
		return queryresult;
	}

	// 判断对象是否检出 mao
	public Boolean IsCheckOut(WTObject pbo) throws WorkInProgressException, WTException {
		Boolean isok = false;
		if (pbo instanceof WTDocument) {

			if (CheckInOutTaskLogic.isCheckedOut((WTDocument) pbo)) {
				isok = true;
				throw new WTException("对象处于检出状态，请检入！再提交流程");
			}
		} else if (pbo instanceof WTPart) {

			if (WorkInProgressHelper.isWorkingCopy((WTPart) pbo) || WorkInProgressHelper.isCheckedOut((WTPart) pbo)) {
				isok = true;
				throw new WTException("对象处于检出状态，请检入！再提交流程");
			}
		}

		System.out.println("isok===" + isok);
		return isok;
	}

	// 判断对象是否检出 mao
	public String isCheckout(WTObject pbo, ObjectReference self) throws WTException {
		StringBuffer result = new StringBuffer();

		if (self != null) {
			WfProcess process = WorkflowUtil.getProcess(self);
			if (process != null) {
				ProcessReviewObjectLink link = null;
				QueryResult queryresult = ProcessReviewObjectLinkHelper.service
						.getProcessReviewObjectLinkByRoleA(process);
				while (queryresult != null && queryresult.hasMoreElements()) {
					link = (ProcessReviewObjectLink) queryresult.nextElement();
					WTObject obj = (WTObject) link.getRoleBObject();
					if (obj != null && IsCheckOut(obj)) {
						if (pbo instanceof WTDocument) {
							WTDocument document = (WTDocument) pbo;
							result.append("文档" + document.getNumber() + "是检出状态，请先检入！" + "\n");
						}
						if (pbo instanceof WTPart) {
							WTPart part = (WTPart) pbo;
							result.append("部件" + part.getNumber() + "是检出状态，请先检入！" + "\n");
						}
					}
				}
			}
		}

		return result.toString();
	}

	// 根据产品类别，判断是否需要加人
	public Boolean checkcontainer(WTObject pbo) {
		Boolean isok = false;
		if (pbo instanceof WTChangeOrder2) {
			WTChangeOrder2 ecn = (WTChangeOrder2) pbo;
			String productline = "";
			try {
				productline = (String) PIAttributeHelper.service.getValue(ecn, "sscpx");
				System.out.println("productline===" + productline);
			} catch (PIException e) {
				e.printStackTrace();
			}
			// 微投--40 激光电视--60
			if (productline.equalsIgnoreCase("40") || productline.equalsIgnoreCase("60")) {
				isok = true;

			}
		}

		System.out.println("isok===" + isok);
		return isok;
	}

	// 根据产品类别+公司，判断是否需要加人 -mao
	public Boolean checkcontainer(WTObject pbo, String company) {
		Boolean isok = false;
		if (pbo instanceof WTChangeOrder2) {
			WTChangeOrder2 ecn = (WTChangeOrder2) pbo;
			String productline = "";
			try {
				productline = (String) PIAttributeHelper.service.getValue(ecn, "sscpx");
				System.out.println("productline===" + productline);
			} catch (PIException e) {
				e.printStackTrace();
			}
			// 微投--40 激光电视--60
			if ("fengmi".equals(company)
					&& (productline.equalsIgnoreCase("40") || productline.equalsIgnoreCase("60"))) {
				isok = true;
			} else if ("guangfeng".equals(company) && (productline.equalsIgnoreCase("13")
					|| productline.equalsIgnoreCase("14") || productline.equalsIgnoreCase("15")
					|| productline.equalsIgnoreCase("16") || productline.equalsIgnoreCase("20")
					|| productline.equalsIgnoreCase("38") || productline.equalsIgnoreCase("50")
					|| productline.equalsIgnoreCase("70") || productline.equalsIgnoreCase("80")
					|| productline.equalsIgnoreCase("90") || productline.equalsIgnoreCase("91")
					|| productline.equalsIgnoreCase("92") || productline.equalsIgnoreCase("93")
					|| productline.equalsIgnoreCase("94") || productline.equalsIgnoreCase("95"))) {
				isok = true;
			}
		}

		System.out.println("isok===" + isok);
		return isok;
	}

	/**
	 * *mao 检查流程中的角色是否在上下文团队中
	 * 
	 * @param pbo
	 * @param rolename
	 * @param principalname
	 * @throws WTException
	 */
	public String checkECNUserPermission(WTObject pbo) throws WTException {
		StringBuffer result = new StringBuffer();
		if (pbo instanceof WTChangeOrder2) {
			WTChangeOrder2 ecn = (WTChangeOrder2) pbo;

			WTContainer container = ecn.getContainer();

			ContainerTeam containerTeam = ContainerTeamHelper.service
					.getContainerTeam((ContainerTeamManaged) container);
			Set<WTUser> userSet = getAllMembers(containerTeam);

			Team processTeam = WorkflowUtil.getTeam(ecn);
			// 流程实例所有角色
			Vector processRoleVector = processTeam.getRoles();
			a: for (int i = 0; i < processRoleVector.size(); i++) {
				Role processRole = (Role) processRoleVector.get(i);
				if ("Normalizer".equals(processRole.toString()) || "Assessor".equals(processRole.toString())
						|| "Signer".equals(processRole.toString()) || "Receiver".equals(processRole.toString())
						|| "Approver".equals(processRole.toString())) {

					Enumeration enumPrin = processTeam.getPrincipalTarget(processRole);// 会签者

					while (enumPrin.hasMoreElements()) {
						WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
						WTPrincipal principal = tempPrinRef.getPrincipal();
						if (principal instanceof WTUser) {
							WTUser user = (WTUser) principal;
							if (userSet != null && userSet.size() > 0 && !userSet.contains(user)) {
								result.append("用户" + user.getFullName() + "不在" + container.getContainerName()
										+ "团队中，请另外选择人员或通知业务管理员设置用户权限！");
								break a;
							}
						}
					}
				}
			}
		} else if (pbo instanceof WTPart) {
			WTPart part = (WTPart) pbo;

			WTContainer container = part.getContainer();

			ContainerTeam containerTeam = ContainerTeamHelper.service
					.getContainerTeam((ContainerTeamManaged) container);
			Set<WTUser> userSet = getAllMembers(containerTeam);

			Team processTeam = WorkflowUtil.getTeam(part);
			// 流程实例所有角色
			Vector processRoleVector = processTeam.getRoles();
			a: for (int i = 0; i < processRoleVector.size(); i++) {
				Role processRole = (Role) processRoleVector.get(i);
				Enumeration enumPrin = processTeam.getPrincipalTarget(processRole);// 会签者

				if ("Normalizer".equals(processRole.toString()) || "Assessor".equals(processRole.toString())
						|| "Signer".equals(processRole.toString()) || "Receiver".equals(processRole.toString())
						|| "Approver".equals(processRole.toString())) {
					while (enumPrin.hasMoreElements()) {
						WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
						WTPrincipal principal = tempPrinRef.getPrincipal();
						if (principal instanceof WTUser) {
							WTUser user = (WTUser) principal;
							if (userSet != null && userSet.size() > 0 && !userSet.contains(user)) {
								result.append("用户" + user.getFullName() + "不在" + container.getContainerName()
										+ "团队中，请另外选择人员或通知业务管理员设置用户权限！");
								break a;
							}
						}
					}
				}
			}
		}

		return result.toString();

	}

	/**
	 * 获得上下文团队中的所有用户
	 * 
	 * @param container
	 * @return
	 * @throws WTException
	 */
	public static Set<WTUser> getAllMembers(ContainerTeam containerTeam) throws WTException {
		Set<WTUser> users = new HashSet<WTUser>();
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		try {

			HashMap map = containerTeam.getAllMembers();
			WTPrincipal principal = null;
			for (Object obj : map.keySet()) {
				principal = (WTPrincipal) ((WTPrincipalReference) obj).getObject();
				if (principal instanceof WTUser) {
					users.add((WTUser) principal);
				} else if (principal instanceof wt.org.WTGroup) {
					users.addAll(getGroupMembers((wt.org.WTGroup) principal));
				}
			}
		} finally {
			SessionServerHelper.manager.setAccessEnforced(enforce);
		}
		return users;
	}

	/**
	 * 获得组中的所有用户
	 * 
	 * @param group
	 * @return
	 */
	public static Set<WTUser> getGroupMembers(wt.org.WTGroup group) {
		Set<WTUser> users = new HashSet<WTUser>();
		try {
			Enumeration member = group.members();
			while (member.hasMoreElements()) {
				WTPrincipal principal = (WTPrincipal) member.nextElement();
				if (principal instanceof WTUser) {
					users.add((WTUser) principal);
				} else if (principal instanceof wt.org.WTGroup) {
					Set<WTUser> ausers = getGroupMembers((wt.org.WTGroup) principal);
					users.addAll(ausers);
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return users;
	}

	// 给流程节点角色添加固定人员
	// 删除用户为空抛出异常 mao
	public void AddTeamRole(WTObject pbo, String rolename, String principalname) throws WTException {
		Role addrole = Role.toRole(rolename);
		WTPrincipal wtprincipal = getUserFromName(principalname);
		if (wtprincipal != null) {
			System.out.println("start add people===" + wtprincipal.getName());
			if (pbo instanceof WTChangeOrder2) {
				WTChangeOrder2 ecn = (WTChangeOrder2) pbo;
				Team processTeam = WorkflowUtil.getTeam(ecn);
				if (processTeam != null) {
					processTeam.addPrincipal(addrole, wtprincipal);
					System.out.println("end add people success");
				}
			} else if (pbo instanceof WTPart) {
				WTPart wtPart = (WTPart) pbo;
				Team processTeam = WorkflowUtil.getTeam(wtPart);
				if (processTeam != null) {
					processTeam.addPrincipal(addrole, wtprincipal);
					System.out.println("end add people success");
				}
			}
		}
	}

	// 删除用户为空抛出异常 mao
	public static WTUser getUserFromName(String name) throws WTException {
		WTUser user = null;
		if ("".equals(name) || null == name) {
			throw new WTException("用户名不能为null");
		}
		Enumeration enumUser = OrganizationServicesHelper.manager.findUser(WTUser.NAME, name);

		if (enumUser.hasMoreElements())
			user = (WTUser) enumUser.nextElement();
		if (user == null) {
			enumUser = OrganizationServicesHelper.manager.findUser(WTUser.FULL_NAME, name);
			if (enumUser.hasMoreElements())
				user = (WTUser) enumUser.nextElement();
		}
		if (user == null) {
			// throw new WTException("系统中不存在用户名为'" + name + "'的用户！");
		}
		return user;
	}

	public void CheckTeam(WTObject pbo, String rolename, String reviewname) throws WTException {
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		Role role = Role.toRole(reviewname);// 会签者
		Role selectrole = Role.toRole(rolename);//
		Boolean isrole = false;

		WTArrayList arrayList = new WTArrayList();

		if (pbo instanceof WTChangeOrder2) {
			WTChangeOrder2 ecn = (WTChangeOrder2) pbo;
			Team processTeam = WorkflowUtil.getTeam(ecn);
			if (processTeam != null) {
				Enumeration enumPrin = processTeam.getPrincipalTarget(role);// 会签者

				while (enumPrin.hasMoreElements()) {
					WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
					WTPrincipal principal = tempPrinRef.getPrincipal();
					System.out.println("principal name===" + principal.getName());
					isrole = checkifRole(pbo, rolename, principal);
					System.out.println("is role===" + isrole);
					if (isrole) {
						break;
					}
				}
				if (!isrole && reviewname.equalsIgnoreCase("Signer")) {
					throw new WTException("会签者/会签节点必须选择:" + selectrole.getShortDescription() + "角色");
				}
				if (!isrole && reviewname.equalsIgnoreCase("Receiver")) {
					throw new WTException("接收者/通知节点必须选择:" + selectrole.getShortDescription() + "角色");
				}
				if (!isrole && reviewname.equalsIgnoreCase("Approver")) {
					throw new WTException("批准者/批准节点必须选择:" + selectrole.getShortDescription() + "角色");
				}
				if (!isrole && reviewname.equalsIgnoreCase("Receiver")) {
					throw new WTException("审核者/审核节点必须选择:" + selectrole.getShortDescription() + "角色");
				}
			}
		}
		if (pbo instanceof WTPart) {
			WTPart part = (WTPart) pbo;
			Team processTeam = WorkflowUtil.getTeam(part);
			if (processTeam != null) {
				Enumeration enumPrin = processTeam.getPrincipalTarget(role);// 会签者

				while (enumPrin.hasMoreElements()) {
					WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
					WTPrincipal principal = tempPrinRef.getPrincipal();
					System.out.println("principal name===" + principal.getName());
					isrole = checkifRole(pbo, rolename, principal);
					System.out.println("is role===" + isrole);
					if (isrole) {
						break;
					}
				}
				if (!isrole && reviewname.equalsIgnoreCase("Signer")) {
					throw new WTException("会签者/会签节点必须选择:" + selectrole.getShortDescription() + "角色");
				}
				if (!isrole && reviewname.equalsIgnoreCase("Receiver")) {
					throw new WTException("接收者/通知节点必须选择:" + selectrole.getShortDescription() + "角色");
				}
				if (!isrole && reviewname.equalsIgnoreCase("Approver")) {
					throw new WTException("批准者/批准节点必须选择:" + selectrole.getShortDescription() + "角色");
				}
				if (!isrole && reviewname.equalsIgnoreCase("Receiver")) {
					throw new WTException("审核者/审核节点必须选择:" + selectrole.getShortDescription() + "角色");
				}
			}
		}
		if (pbo instanceof WTDocument) {
			WTDocument doc = (WTDocument) pbo;
			Team processTeam = WorkflowUtil.getTeam(doc);
			if (processTeam != null) {
				Enumeration enumPrin = processTeam.getPrincipalTarget(role);// 会签者

				while (enumPrin.hasMoreElements()) {
					WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
					WTPrincipal principal = tempPrinRef.getPrincipal();
					System.out.println("principal name===" + principal.getName());
					isrole = checkifRole(pbo, rolename, principal);
					System.out.println("is role===" + isrole);
					if (isrole) {
						break;
					}
				}
				if (!isrole && reviewname.equalsIgnoreCase("Signer")) {
					throw new WTException("会签者/会签节点必须选择:" + selectrole.getShortDescription() + "角色");
				}
				if (!isrole && reviewname.equalsIgnoreCase("Receiver")) {
					throw new WTException("接收者/通知节点必须选择:" + selectrole.getShortDescription() + "角色");
				}
				if (!isrole && reviewname.equalsIgnoreCase("Approver")) {
					throw new WTException("批准者/批准节点必须选择:" + selectrole.getShortDescription() + "角色");
				}
				if (!isrole && reviewname.equalsIgnoreCase("Receiver")) {
					throw new WTException("审核者/审核节点必须选择:" + selectrole.getShortDescription() + "角色");
				}
			}
		}

	}

	// 检查流程团队中，对应角色中用户是否存在，mao
	public boolean CheckProcessTeamUser(WTObject pbo, String username, String reviewname) throws WTException {
		Boolean result = false;

		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		Role role = Role.toRole(reviewname);// 会签者

		if (pbo instanceof WTPart) {
			WTPart part = (WTPart) pbo;
			Team processTeam = WorkflowUtil.getTeam(part);
			if (processTeam != null) {
				Enumeration enumPrin = processTeam.getPrincipalTarget(role);// 会签者

				while (enumPrin.hasMoreElements()) {
					WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
					WTPrincipal principal = tempPrinRef.getPrincipal();
					System.out.println("principal name===" + principal.getName());
					if (principal instanceof WTUser) {
						WTUser user = (WTUser) principal;
						if (username.equals(user.getName())) {
							result = true;
							break;
						}
					}
				}
			}
		}
		return result;
	}

	// 检查指定角色是否在团队中
	public static Boolean checkifRole(WTObject pbo, String rolename, WTPrincipal principal) {
		Boolean isrole = false;
		// Role purchasingrole = Role.toRole("purchasing_representative");//采购代表
		// Role qualityrole = Role.toRole("quality_representative");//质量代表
		// Role manufacturingrole =
		// Role.toRole("manufacturing_representative");//制造代表
		// Role pmcrole = Role.toRole("pmc_representative");//pmc代表
		// Role testrole = Role.toRole("test_representative");//测试代表

		Role role = Role.toRole(rolename);
		System.out.println("role===" + role.getShortDescription());
		WTContainer container = null;
		if (pbo instanceof WTChangeOrder2) {
			WTChangeOrder2 object = (WTChangeOrder2) pbo;
			container = object.getContainer();
		}
		if (pbo instanceof WTPart) {
			WTPart object = (WTPart) pbo;
			container = object.getContainer();
		}
		if (pbo instanceof WTDocument) {
			WTDocument object = (WTDocument) pbo;
			container = object.getContainer();
		}
		if (container != null) {
			try {
				WTRoleHolder2 wtroleholder2 = TeamCCHelper.getTeamFromObject(container);
				ContainerTeam containerTeam = ContainerTeamHelper.service
						.getContainerTeam((ContainerTeamManaged) container);
				Enumeration enumPrin = containerTeam.getPrincipalTarget(role);
				// mao
				Set<WTUser> userSet = new HashSet<WTUser>();

				while (enumPrin.hasMoreElements()) {
					WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
					WTPrincipal wtPrincipal = tempPrinRef.getPrincipal();
					System.out.println("wtprincipal====" + wtPrincipal.getName());

					if (wtPrincipal instanceof WTUser) {
						userSet.add((WTUser) wtPrincipal);
					} else if (wtPrincipal instanceof wt.org.WTGroup) {
						userSet.addAll(getGroupMembers((wt.org.WTGroup) wtPrincipal));
					}
					// if (wtPrincipal.getName().equals(principal.getName())) {
					// isrole = true;
					// }
					// if (isrole) {
					// break;
					// }
				}

				if (userSet != null && userSet.size() > 0 && userSet.contains((WTUser) principal)) {
					isrole = true;
				}

			} catch (WTException e) {
				e.printStackTrace();
			} // 获取容器的人

		}

		return isrole;
	}

	// ECN流程中取消后，修改eca任务为取消状态
	public void setECAcancelled(WTObject pbo) throws WTException {
		WTChangeOrder2 ecChangeOrder2 = (WTChangeOrder2) pbo;
		Collection<ChangeActivityIfc> changeActivityIfcslist = ChangeUtils.getChangeActivities(ecChangeOrder2);
		for (ChangeActivityIfc changeActivityIfc : changeActivityIfcslist) {
			ChangeActivity2 changeActivity2 = (ChangeActivity2) changeActivityIfc;
			// 设置已解决
			LifeCycleHelper.service.setLifeCycleState(changeActivity2, State.toState("CANCELLED"), true);
		}
	}

	/**
	 * 设置ChangeActivity2状态
	 * 
	 * @param arrayList
	 * @throws WTException
	 */
	public void closeWorkfolw(WTArrayList arrayList) throws WTException {
		Collection<ChangeRecord2> changeRecord2s = ChangeUtils.getChangeRecord2s(arrayList);
		logger.debug("changeRecord2s===" + changeRecord2s.size());
		Iterator<ChangeRecord2> iterator = changeRecord2s.iterator();
		while (iterator.hasNext()) {
			ChangeRecord2 next = iterator.next();
			ChangeActivity2 changeActivity2 = next.getChangeActivity2();
			logger.debug("changeActivity2===" + changeActivity2.getName());
			// 设置已解决
			LifeCycleHelper.service.setLifeCycleState(changeActivity2, State.toState("RESOLVED"), true);
		}
	}

	public Map<ArrayList<String>, ArrayList<String>> getEnumeratedMap(String enumName) throws WTException {
		Map<ArrayList<String>, ArrayList<String>> enumMap = new HashMap<ArrayList<String>, ArrayList<String>>();
		if (PIStringUtils.isNull(enumName)) {
			return enumMap;
		}

		try {
			// 获取全局枚举定义
			EnumerationDefinitionReadView enumDef = TypeDefinitionServiceHelper.service.getEnumDefView(enumName);
			if (enumDef != null) {
				Collection<EnumerationMembershipReadView> datasArray = enumDef.getAllMemberships();
				if (datasArray != null) {
					// 存储数据
					Map<Integer, Map<String, String>> datasMap = new TreeMap<Integer, Map<String, String>>();
					for (EnumerationMembershipReadView enumerationMembershipReadView : datasArray) {
						// 内部名称
						String interiorName = enumerationMembershipReadView.getName();
						Collection<PropertyValueReadView> array = enumerationMembershipReadView.getAllProperties();
						for (PropertyValueReadView propertyValueReadView : array) {
							Integer index = Integer.parseInt(propertyValueReadView.getValueAsString());
							// 存储枚举定义
							Map<String, String> enumInfo = new HashMap<String, String>();
							enumInfo.put(interiorName, PropertyHolderHelper.getDisplayName(
									enumerationMembershipReadView.getMember(), SessionHelper.getLocale()));
							datasMap.put(index, enumInfo);
						}
					}
					// 显示名称
					ArrayList<String> displayArray = new ArrayList<String>();
					// 内部名称
					ArrayList<String> interiorArray = new ArrayList<String>();
					for (Map.Entry<Integer, Map<String, String>> entryMap : datasMap.entrySet()) {
						for (Map.Entry<String, String> enumEntryMap : entryMap.getValue().entrySet()) {
							interiorArray.add(enumEntryMap.getKey());
							displayArray.add(enumEntryMap.getValue());
						}
					}
					enumMap.put(interiorArray, displayArray);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		}

		return enumMap;
	}

	/**
	 * 
	 * @param cb
	 * @return
	 * @throws WTException
	 */
	public String getProductLineAndProject(NmCommandBean cb) throws WTException {
		String result = "";
		NmOid pageOid = cb.getPageOid();
		Object refObject = pageOid.getRefObject();
		if (refObject != null && refObject instanceof WTDocument) {
			WTDocument document = (WTDocument) refObject;
			logger.debug("document===" + document.getName());
			String producLine = (String) PIAttributeHelper.service.getValue(document, "sscpx");
			String project = (String) PIAttributeHelper.service.getValue(document, "ssxm");
			result = producLine + ";" + project;
		}
		logger.debug("result===" + result);
		return result;
	}

	/**
	 * 
	 * @param cb
	 * @return
	 * @throws WTException
	 */
	public String getProductLineAndProjectDiesplay(NmCommandBean cb) throws WTException {
		String result = "";
		NmOid pageOid = cb.getPageOid();
		Object refObject = pageOid.getRefObject();
		if (refObject != null && refObject instanceof WTDocument) {
			WTDocument document = (WTDocument) refObject;
			String producLine = (String) PIAttributeHelper.service.getValue(document, "sscpx");
			// 读取‘所属产品线’属性配置
			List<ProductLineConfigBean> productLineConfigArray = ReadExcelData.getProductLineConfig();
			for (ProductLineConfigBean bean : productLineConfigArray) {
				if (bean.getEnumName().equalsIgnoreCase(producLine)) {
					if (PIStringUtils.isNotNull(bean.getNodeName())) {
						producLine = bean.getNodeName();
					}
					break;
				}
			}
			String nodeLocalizedHierarchy = PIClassificationHelper.service.getNodeLocalizedName(producLine,
					Locale.CHINA);
			String project = (String) PIAttributeHelper.service.getValue(document, "ssxm");

			String projectValue = project;
			EnumerationDefinitionReadView enumDef = TypeDefinitionServiceHelper.service.getEnumDefView("ssxm");
			if (enumDef != null) {
				Collection<EnumerationMembershipReadView> datasArray = enumDef.getAllMemberships();
				if (datasArray != null) {
					// 存储数据
					Map<Integer, Map<String, String>> datasMap = new TreeMap<Integer, Map<String, String>>();
					for (EnumerationMembershipReadView enumerationMembershipReadView : datasArray) {
						// 内部名称
						String interiorName = enumerationMembershipReadView.getName();
						Collection<PropertyValueReadView> array = enumerationMembershipReadView.getAllProperties();
						for (PropertyValueReadView propertyValueReadView : array) {
							Integer index = Integer.parseInt(propertyValueReadView.getValueAsString());
							// 存储枚举定义
							Map<String, String> enumInfo = new HashMap<String, String>();
							enumInfo.put(interiorName, PropertyHolderHelper.getDisplayName(
									enumerationMembershipReadView.getMember(), SessionHelper.getLocale()));
							datasMap.put(index, enumInfo);
						}
					}
					for (Map.Entry<Integer, Map<String, String>> entryMap : datasMap.entrySet()) {
						for (Map.Entry<String, String> enumEntryMap : entryMap.getValue().entrySet()) {
							if (enumEntryMap.getKey().equals(project)) {
								projectValue = enumEntryMap.getValue();
								break;
							}
						}
					}
				}

			}
			result = nodeLocalizedHierarchy + ";" + projectValue;
		}
		return result;
	}

	public void checkReviewObject(ObjectReference self) throws WTException {
		// StringBuffer sBuffer = new StringBuffer();
		// WTArrayList reviewObjectByProcess =
		// ReviewObjectAndWorkFlowLink.getReviewObjectByProcess(self);
		// for (int i = 0; i < reviewObjectByProcess.size(); i++) {
		// WTDocument doc = (WTDocument)
		// reviewObjectByProcess.getPersistable(i);
		//
		// TypeIdentifier typeIdentifier =
		// TypeIdentifierUtility.getTypeIdentifier(doc);
		// String typename = typeIdentifier.getTypename();
		// if (!typename.contains("com.ptc.ReferenceDocument")) {
		// sBuffer.append("随迁对象中的 " + doc.getDisplayIdentifier() +
		// "文档不是参考文档子类型。\n");
		// }
		//
		// }
		//
		// if (sBuffer.length() > 0) {
		// throw new WTException(sBuffer.toString());
		// }
	}

	public void checkPublishReviewObject(ObjectReference self) throws WTException {
		StringBuffer sBuffer = new StringBuffer();
		WTArrayList reviewObjectByProcess = ReviewObjectAndWorkFlowLink.getReviewObjectByProcess(self);
		for (int i = 0; i < reviewObjectByProcess.size(); i++) {
			WTDocument doc = (WTDocument) reviewObjectByProcess.getPersistable(i);

			TypeIdentifier typeIdentifier = TypeIdentifierUtility.getTypeIdentifier(doc);
			String typename = typeIdentifier.getTypename();
			if (!typename.contains("com.ptc.ReferenceDocument")) {
				sBuffer.append("随迁对象中的 " + doc.getDisplayIdentifier() + "文档不是参考文档子类型。\n");
			}
		}

		if (sBuffer.length() > 0) {
			throw new WTException(sBuffer.toString());
		}
	}

	/**
	 * 修改部件的产品状态属性
	 * 
	 * @author HYJ&NJH
	 * @param self
	 * @throws WTException
	 * @throws JSONException
	 */
	public void changePartCpzt(ObjectReference self) throws WTException, JSONException {
		WfProcess process = WorkflowUtil.getProcess(self);
		String cpztValues = (String) process.getContext().getValue("cpztValues");
		// 获取随迁对象
		WTArrayList reviewObjectByProcess = ReviewObjectAndWorkFlowLink.getReviewObjectByProcess(self);
		for (int i = 0; i < reviewObjectByProcess.size(); i++) {
			Persistable persistable = reviewObjectByProcess.getPersistable(i);
			if (persistable instanceof WTPart) {
				WTPart part = (WTPart) persistable;
				// 升级小版本
				part = (WTPart) PICoreHelper.service.checkoutObject(part);
				part = (WTPart) PICoreHelper.service.checkinObject(part, "修改部件的产品状态属性升级小版本!");
				if (cpztValues != null) {
					JSONObject jsonObject = new JSONObject(cpztValues);
					String value = (String) jsonObject.get(part.getNumber());
					if (value != null) {
						Object oldValue = PIAttributeHelper.service.getValue(part, "cpzt");
						PIAttributeHelper.service.forceUpdateSoftAttribute(part, "oldCpzt", oldValue);
						PIAttributeHelper.service.forceUpdateSoftAttribute(part, "cpzt", value);
					}
				}
			}
		}
	}

	/**
	 * 检查是否父件与子件一起归档
	 * 
	 * @author HYJ&NJH
	 * @param self
	 * @throws WTException
	 * @throws JSONException
	 */
	public void checkParentPartWithchild(ObjectReference self) throws WTException {

		WTArrayList reviewlist = new WTArrayList();
		// 获取随迁对象
		WTArrayList reviewObjectByProcess = ReviewObjectAndWorkFlowLink.getReviewObjectByProcess(self);
		for (int i = 0; i < reviewObjectByProcess.size(); i++) {
			Persistable persistable = reviewObjectByProcess.getPersistable(i);
			if (persistable instanceof WTPart) {
				WTPart part = (WTPart) persistable;
				System.out.println("review  part=====" + part.getNumber());
				// 获取所有随签对象的子件
				Collection<WTPart> childArray = ChangePartQueryUtils.getPartMultiwallStructure(part);
				for (WTPart childPart : childArray) {
					if (!reviewlist.contains(childPart)) {
						reviewlist.add(childPart);
					}
				}
				// 获取所有随签对象的父件
				QueryResult qr = WTPartHelper.service.getUsedByWTParts((WTPartMaster) part.getMaster());
				// ArrayList<WTPart> parentList = new ArrayList<WTPart>();
				while (qr.hasMoreElements()) {
					WTPart parent = (WTPart) qr.nextElement();
					if (!reviewlist.contains(parent)) {
						reviewlist.add(parent);
					}
				}

			}
		}
		for (int i = 0; i < reviewlist.size(); i++) {
			// System.out.println("part====="+childlist.get(i));
			Object part = (Object) reviewlist.get(i);
			// System.out.println("child or parent
			// part====="+ChangeUtils.getNumber( part));

			if (reviewObjectByProcess.contains(part)) {
				if (part instanceof ObjectReference) {
					Object obj = ((ObjectReference) part).getObject();

					throw new WTException(((WTPart) obj).getNumber() + ":与随签对象存在父子关系，请移除后提交！");
				}
			}
		}
	}

	public void checkInArchived() throws WTException {
		if (this.self != null && this.pbo != null) {
			WTArrayList list = this.collect();
			this.isObjsCheckedOut(list);
			this.checkISlatestVersion(list);
			this.checkReviewObjectStates(list);
			// this.checkIsAssociatedRunningProcesses(list);
			this.isRunningWorkflow(list);
			this.getErrorMessge();
			if (!this.checkPboIsDelect(list)) {
				Locale locale = SessionHelper.manager.getLocale();
				this.errorMessage.append(
						WTMessage.getLocalizedMessage("ext.generic.reviewObject.resource.ReviewObjectResourceRB",
								"WF_PBO_NULL", (Object[]) null, locale));
				this.getErrorMessge();
			}
		}
	}

	/**
	 * 归档过滤ecn,eca流程
	 * 
	 * @author HYJ&NJH
	 * @param list
	 * @throws WTException
	 */
	private void isRunningWorkflow(WTArrayList list) throws WTException {
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);

		try {
			ArrayList<WTObject> e = new ArrayList<WTObject>(list.size());

			for (int i = 0; i < list.size(); ++i) {
				WTObject obj = (WTObject) list.getPersistable(i);
				if (PersistenceHelper.isEquivalent(obj, this.pbo)) {
					continue;
				}
				NmOid nmOid = NmOid.newNmOid(PersistenceHelper.getObjectIdentifier(obj));
				QueryResult qr = NmWorkflowHelper.service.getRoutingHistoryData(nmOid);
				while (qr.hasMoreElements()) {
					WfProcess process = (WfProcess) qr.nextElement();
					String templateName = process.getTemplate().getName();
					if (process.getState().equals(
							WfState.OPEN_RUNNING) /*
													 * && templateName.contains(
													 * "GenericPartWF")
													 */ ) {
						if (templateName.equals("GenericECNWF") || templateName.equals("GenericECAWF")) {
						} else {
							e.add(obj);
						}
					}
				}

				if (e.size() > 0) {
					this.errorMessage.append(
							WTMessage.getLocalizedMessage("ext.generic.reviewObject.resource.ReviewObjectResourceRB",
									"WF_TASK_REVIEWOBJECT_IS_ASSOCIATEDPROCESSES", (Object[]) null, Locale.CHINA));
					this.errorMessage.append(this.getCollectionDisplayInfo(e));
				}
			}
		} catch (WTException arg10) {
			arg10.printStackTrace();
			throw arg10;
		} finally {
			SessionServerHelper.manager.setAccessEnforced(enforce);
		}
	}

	/***
	 * 获取特定替代件
	 * 
	 * @param pbo
	 * @throws WTException
	 */

	private static ArrayList<String> getSubstitutePart(WTPartUsageLink link) throws WTException {
		ArrayList<String> substitutepartlList = new ArrayList<String>();
		WTCollection collection = WTPartHelper.service.getSubstituteLinks(link);
		System.out.println("collection size====" + collection.size());
		if (!collection.isEmpty()) {
			Iterator itr = collection.iterator();
			while (itr.hasNext()) {
				ObjectReference objReference = (ObjectReference) itr.next();
				WTPartSubstituteLink subLink = (WTPartSubstituteLink) objReference.getObject();
				WTPartMaster partMaster = (WTPartMaster) subLink.getSubstitutes();
				substitutepartlList.add(partMaster.getNumber());
				System.out.println("sub part number===" + partMaster.getNumber());
			}
		}
		return substitutepartlList;
	}

	public static void checkSubpart(WTPartUsageLink usageLink, List<WTObject> listmessage) {
		ArrayList<String> subpartList = new ArrayList<String>();
		try {
			subpartList = getSubstitutePart(usageLink);
		} catch (WTException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < subpartList.size(); i++) {
			String number = subpartList.get(i);
			WTPart subpart = PartUtil.getLastestWTPartByNumber(number);
			String state = subpart.getState().toString();
			if (!state.equalsIgnoreCase("RELEASED") && !state.equalsIgnoreCase("ARCHIVED")) {
				listmessage.add(subpart);

			}
		}

	}

	/**
	 * D视图添加校验：分类为组件的下面不允许挂总成
	 * 
	 * @param part
	 * @throws WTException
	 */
	public void checkSonHistoryversionDesign(WTPart part) throws WTException {
		View view = (View) part.getView().getObject();
		StringBuffer message = new StringBuffer();
		if (view != null && "Design".equals(view.getName())) {
			WTUser previous = (WTUser) SessionHelper.manager.getPrincipal();
			try {
				WTPrincipal wtadministrator = SessionHelper.manager.getAdministrator();
				// 取得当前用户
				SessionContext.setEffectivePrincipal(wtadministrator);

				String clsParent = (String) PIAttributeHelper.service.getValue(part, "Classification");
				System.out.println("clsParent ====" + clsParent);
				if (clsParent.startsWith("appo_bcp01") || clsParent.startsWith("appo_bcp10")
						|| clsParent.startsWith("appo_bcp13") || clsParent.startsWith("appo_bcp05")
						|| clsParent.startsWith("appo_bcp20") || clsParent.startsWith("appo_bcp14")
						|| clsParent.startsWith("appo_bcp06") || clsParent.startsWith("appo_bcp04")) {
					// 获取所有子件
					ArrayList<WTPart> childpartList = new ArrayList<WTPart>();
					StartAppoPartArchiveIssueWF.getBomallchildpart(part, childpartList);

					for (int i = 0; i < childpartList.size(); i++) {
						WTPart son = (WTPart) childpartList.get(i);

						System.out.println("part container name====" + part.getContainerName());
						if (!part.getContainerName().startsWith("微投产品库")
								&& !part.getContainerName().startsWith("激光电视产品库")) {
							// 检出子件是否有总成
							String cls = (String) PIAttributeHelper.service.getValue(son, "Classification");
							System.out.println("cls ====" + cls);
							if (cls.startsWith("appo_bcp17") || cls.startsWith("appo_bcp21")
									|| cls.startsWith("appo_bcp22") || cls.startsWith("appo_bcp24")
									|| cls.startsWith("appo_bcp11") || cls.startsWith("appo_bcp25")
									|| cls.startsWith("appo_bcp19") || cls.startsWith("appo_bcp16")) {
								if (message.toString() != null && message.toString().length() > 0) {
									message.append("\n");
								}
								message.append("设计视图组件【" + part.getNumber() + "】，不能引用总成的子件【" + son.getNumber() + "】.");
							}
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				SessionContext.setEffectivePrincipal(previous);
			}

		}

		if (message.toString() != null && message.toString().length() > 0) {
			throw new WTException(message.toString());
		}
	}

	/**
	 * 检查子类状态
	 * 
	 * @author HYJ&NJH
	 * @param part
	 * @param isArchive
	 *            是否归档流程
	 * @return
	 * @throws WTException
	 */
	public void checkSonHistoryversionMAf(WTPart part) throws WTException {
		// boolean stateIsOK = true;
		String message = "";
		List<String> partlist = new ArrayList<String>();

		WTUser previous = (WTUser) SessionHelper.manager.getPrincipal();

		// 当前用户设置为管理员，用于忽略权限
		try {
			WTPrincipal wtadministrator = SessionHelper.manager.getAdministrator();
			// 取得当前用户
			SessionContext.setEffectivePrincipal(wtadministrator);
			WTPrincipalReference wtprincipalreference = (WTPrincipalReference) (new ReferenceFactory())
					.getReference(previous);
			AccessControlHelper.manager.addPermission((AdHocControlled) part, wtprincipalreference,
					AccessPermission.MODIFY_IDENTITY, AdHocAccessKey.WNC_ACCESS_CONTROL);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		// 获取单层子件
		// WTCollection childrens = PIPartHelper.service.findChildren(part);
		// 获取所有子件
		ArrayList<WTPart> childpartList = new ArrayList<WTPart>();
		try {
			StartAppoPartArchiveIssueWF.getBomallchildpart(part, childpartList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("childpartList.size()====" + childpartList.size());
		if (!part.getNumber().startsWith("A") && !part.getNumber().startsWith("B")) {
			if (childpartList.size() > 0) {
				throw new WTException("除成品、半成品外不允许搭BOM！");
			}
		}
		for (int i = 0; i < childpartList.size(); i++) {
			WTPart son = (WTPart) childpartList.get(i);
			String stateVal = son.getLifeCycleState().toString();

			// if (part.getVersionIdentifier().getValue().startsWith("A")) {
			System.out.println("part container name====" + part.getContainerName());
			if (!part.getContainerName().startsWith("微投产品库") && !part.getContainerName().startsWith("激光电视产品库")) {
				// 检出子件是否有总成
				String cls = (String) PIAttributeHelper.service.getValue(son, "Classification");
				System.out.println("cls ====" + cls);
				if (cls.startsWith("appo_bcp17") || cls.startsWith("appo_bcp21") || cls.startsWith("appo_bcp22")
						|| cls.startsWith("appo_bcp24") || cls.startsWith("appo_bcp11") || cls.startsWith("appo_bcp25")
						|| cls.startsWith("appo_bcp19") || cls.startsWith("appo_bcp16")) {
					throw new WTException("制造BOM【" + part.getNumber() + "】不能引用总成的子件【" + son.getNumber() + "】.");
				}
			}
			// }
			if (stateVal.equals("ARCHIVED") || stateVal.equals("RELEASED")) {
			} else {
				QueryResult partqr = getParts(son.getNumber());
				while (partqr.hasMoreElements()) {
					WTPart oldpart = (WTPart) partqr.nextElement();
					String state = oldpart.getState().toString();
					System.out.println("version=====" + state);
					if (!partlist.contains(state)) {
						partlist.add(state);
					}

				} // 存在有已归档或已发布的版本，
				if (partlist.contains("ARCHIVED") || partlist.contains("RELEASED")) {
				} else {
					message = message + "子件:" + son.getNumber() + "未存在已归档或已发布的版本,不符合归档逻辑，无法提交归档流程!" + "\n";
				}
			}
			// 检出全局替代
			WTCollection wtcol = WTPartHelper.service.getAlternateLinks((WTPartMaster) son.getMaster());
			Iterator ite = wtcol.iterator();
			while (ite.hasNext()) {
				Object obj = ite.next();

				if (obj != null && obj instanceof ObjectReference) {
					ObjectReference objRef = (ObjectReference) obj;

					Object tempObj = objRef.getObject();

					if (tempObj != null && tempObj instanceof WTPartAlternateLink) {
						WTPartAlternateLink alternateLink = (WTPartAlternateLink) tempObj;

						WTPartMaster alternatePartMaster = alternateLink.getAlternates();
						System.out.println("alternatePartMaster number=" + alternatePartMaster.getNumber());
						WTPart alPart = PartUtil.getLastestWTPartByNumber(alternatePartMaster.getNumber());
						String alpartstate = alPart.getState().toString();
						if (!alpartstate.equalsIgnoreCase("RELEASED") && !alpartstate.equalsIgnoreCase("ARCHIVED")) {
							QueryResult partqr = getParts(alPart.getNumber());
							List<String> partlistA = new ArrayList<String>();
							while (partqr.hasMoreElements()) {
								WTPart oldpart = (WTPart) partqr.nextElement();
								String statea = oldpart.getState().toString();
								System.out.println("version=====" + statea);
								if (!partlistA.contains(statea)) {
									partlistA.add(statea);
								}

							} // 存在有已归档或已发布的版本，
							if (partlistA.contains("ARCHIVED") || partlistA.contains("RELEASED")) {
							} else {
								message = message + "全局替代料:" + alPart.getNumber() + "未存在已归档或已发布的版本,不符合归档逻辑，无法提交归档流程!";
							}
						}
					}
				}
			}
		}
		// 检查特定替代料状态
		QueryResult qr = WTPartHelper.service.getUsesWTPartMasters(part);
		System.out.println("qr size=========" + qr.size());
		ArrayList<String> subpartList = new ArrayList<String>();
		while (qr.hasMoreElements()) {
			WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
			subpartList = getSubstitutePart(link);
			System.out.println("subpartList=========" + subpartList.size());
			for (int j = 0; j < subpartList.size(); j++) {
				String number = subpartList.get(j);
				WTPart subpart = PartUtil.getLastestWTPartByNumber(number);
				String state = subpart.getState().toString();
				System.out.println(" number  =========" + number);
				System.out.println(" subpart   state=========" + state);
				if (!state.equalsIgnoreCase("RELEASED") && !state.equalsIgnoreCase("ARCHIVED")) {

					QueryResult partqr = getParts(subpart.getNumber());
					List<String> partlistS = new ArrayList<String>();
					while (partqr.hasMoreElements()) {
						WTPart oldpart = (WTPart) partqr.nextElement();
						String stateold = oldpart.getState().toString();
						System.out.println("version=====" + stateold);
						if (!partlistS.contains(stateold)) {
							partlistS.add(stateold);
						}

					} // 存在有已归档或已发布的版本，
					if (partlistS.contains("ARCHIVED") || partlistS.contains("RELEASED")) {
					} else {
						message = message + "特定替代料:" + subpart.getNumber() + "未存在已归档或已发布的版本,不符合归档逻辑，无法提交归档流程!";
					}

				}
			}
		}

		SessionContext.setEffectivePrincipal(previous);
		if (message.length() > 0) {
			throw new WTException(message);
		}
	}

	/**
	 * 检查子类状态
	 * 
	 * @author HYJ&NJH
	 * @param part
	 * @param isArchive
	 *            是否归档流程
	 * @return
	 * @throws WTException
	 */
	public static String checkSonHistoryversion(WTPart part) throws WTException {
		// boolean stateIsOK = true;
		String message = "";
		List partlist = new ArrayList<String>();
		WTUser previous = (WTUser) SessionHelper.manager.getPrincipal();

		// 当前用户设置为管理员，用于忽略权限
		try {
			WTPrincipal wtadministrator = SessionHelper.manager.getAdministrator();
			// 取得当前用户
			SessionContext.setEffectivePrincipal(wtadministrator);
			WTPrincipalReference wtprincipalreference = (WTPrincipalReference) (new ReferenceFactory())
					.getReference(previous);
			AccessControlHelper.manager.addPermission((AdHocControlled) part, wtprincipalreference,
					AccessPermission.MODIFY_IDENTITY, AdHocAccessKey.WNC_ACCESS_CONTROL);
		} catch (WTException e1) {
			e1.printStackTrace();
		}
		// 获取单层子件
		// WTCollection childrens = PIPartHelper.service.findChildren(part);
		// 获取所有子件
		ArrayList<WTPart> childpartList = new ArrayList<WTPart>();
		try {
			StartAppoPartArchiveIssueWF.getBomallchildpart(part, childpartList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("childpartList.size()====" + childpartList.size());
		for (int i = 0; i < childpartList.size(); i++) {
			WTPart son = (WTPart) childpartList.get(i);
			String stateVal = son.getLifeCycleState().toString();

			if (stateVal.equals("ARCHIVED") || stateVal.equals("RELEASED")) {
			} else {
				QueryResult partqr = getParts(son.getNumber());
				while (partqr.hasMoreElements()) {
					WTPart oldpart = (WTPart) partqr.nextElement();
					String state = oldpart.getState().toString();
					System.out.println("version=====" + state);
					if (!partlist.contains(state)) {
						partlist.add(state);
					}

				} // 存在有已归档或已发布的版本，
				if (partlist.contains("ARCHIVED") || partlist.contains("RELEASED")) {
				} else {
					message = message + "子件:" + son.getNumber() + "未存在已归档或已发布的版本,无法启动设计部件归档流程!" + "\n";
				}
			}
		}
		SessionContext.setEffectivePrincipal(previous);
		return message;
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

	/**
	 * 获取流程对象的通用方法 。
	 */
	public static WfProcess getWF(Object obj) throws Exception {
		Object obj1 = null;
		try {
			if (obj instanceof ObjectIdentifier) {
				obj1 = PersistenceHelper.manager.refresh((ObjectIdentifier) obj);
			} else if (obj instanceof ObjectReference) {
				obj1 = ((ObjectReference) obj).getObject();
			} else if (obj instanceof WorkItem) {
				obj1 = ((WorkItem) obj).getSource().getObject();
			} else if (obj instanceof WfActivity) {
				obj1 = ((WfActivity) obj).getParentProcess();
			} else if (obj instanceof WfConnector) {
				obj1 = ((WfConnector) obj).getParentProcessRef().getObject();
			} else if (obj instanceof WfBlock) {
				obj1 = ((WfBlock) obj).getParentProcess();
			} else if (obj instanceof WfProcess) {
				obj1 = (WfProcess) obj;
			}
			return (WfProcess) obj1;
		} catch (Exception e) {
			System.out.println("================================ Exception =====================================");
			System.out.println("@Class: ext.invt.change.ChangeUtil");
			System.out.println("Method: public static WfProcess getWF(Object obj)");
			System.out.println("Variables:");
			System.out.println((new StringBuilder("WTObject  obj1 = ")).append(obj1).toString());
			System.out.println("================================================================================");
			System.out.println("Exception Messages:");
			e.getLocalizedMessage();
			System.out.println("================================================================================");
			System.out.println("Stack Trace:");
			e.printStackTrace();
			System.out.println("================================================================================");
			return (WfProcess) obj1;
		}
	}

	/***
	 * 检查子件状态是否为‘已归档’或‘已发布’
	 * 
	 * @param pbo
	 * @throws WTException
	 */
	public void checkChildsState(WTObject pbo) throws WTException {
		if (pbo == null || !(pbo instanceof WTPart)) {
			return;
		}

		// 获取部件下所有子件信息
		Collection<WTPart> partMultiwallStructure = ChangePartQueryUtils.getPartMultiwallStructure((WTPart) pbo);
		if (partMultiwallStructure == null || partMultiwallStructure.size() == 0) {
			return;
		}
		if (!((WTPart) pbo).getNumber().startsWith("A") && !((WTPart) pbo).getNumber().startsWith("B")) {
			if (partMultiwallStructure.size() > 0) {
				throw new WTException("除成品、半成品外不允许搭BOM！");
			}
		}

		String parentView = ((WTPart) pbo).getViewName();
		System.out.println("partMultiwallStructure qr size====" + partMultiwallStructure.size());
		List<WTObject> errorList = new ArrayList<WTObject>();
		List<WTObject> errorList2 = new ArrayList<WTObject>();
		for (WTPart part : partMultiwallStructure) {
			String state = part.getLifeCycleState().toString();
			String view = part.getViewName();
			if (parentView.equalsIgnoreCase("Design") && view.equalsIgnoreCase("Manufacturing")) {
				// throw new WTException("子件:" + part.getNumber() +
				// ",为制造视图(Manufacturing)，不能加到设计视图的bom中,无法启动设计部件归档流程!");
			}
			System.out.println("state========" + state);
			if ((!state.equals(ChangeConstants.ARCHIVED)) && (!state.equals(ChangeConstants.RELEASED))) {
				errorList.add(part);
			}

			if (part.getNumber().startsWith("Q")) {
				throw new WTException(part.getNumber() + ":为临时编码，不能添加到BOM中，请移除或替换后提交！");
			}

			System.out.println("errorList=1=======" + errorList.size());
			checkallSubpart(errorList, part);
			System.out.println("errorList=2=======" + errorList.size());

			// 检查全局替代料状态
			WTCollection wtcol = WTPartHelper.service.getAlternateLinks((WTPartMaster) part.getMaster());
			Iterator ite = wtcol.iterator();
			while (ite.hasNext()) {
				Object obj = ite.next();

				if (obj != null && obj instanceof ObjectReference) {
					ObjectReference objRef = (ObjectReference) obj;

					Object tempObj = objRef.getObject();

					if (tempObj != null && tempObj instanceof WTPartAlternateLink) {
						WTPartAlternateLink alternateLink = (WTPartAlternateLink) tempObj;

						WTPartMaster alternatePartMaster = alternateLink.getAlternates();
						System.out.println("alternatePartMaster number=" + alternatePartMaster.getNumber());
						WTPart alPart = PartUtil.getLastestWTPartByNumber(alternatePartMaster.getNumber());
						String alpartstate = alPart.getState().toString();
						if (!alpartstate.equalsIgnoreCase("RELEASED") && !alpartstate.equalsIgnoreCase("ARCHIVED")) {
							errorList.add(alPart);

						}
					}
				}
			}

		}
		System.out.println("errorList=3=======" + errorList.size());
		// 检查特定替代料状态
		QueryResult qr = WTPartHelper.service.getUsesWTPartMasters((WTPart) pbo);
		while (qr.hasMoreElements()) {
			WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
			checkSubpart(link, errorList);
		}
		System.out.println("errorList=4=======" + errorList.size());
		if (errorList.size() > 0) {
			StringBuilder errorMsg = new StringBuilder();
			errorMsg.append("状态不为‘已归档’或‘已发布’的子件或替代料：");
			errorMsg.append(getCollectionDisplayInfo(errorList));
			throw new WTException(errorMsg.toString());
		}

	}

	/***
	 * 检查子件状态是否为‘已归档’或‘已发布’
	 * 
	 * @param pbo
	 * @throws WTException
	 */
	public void checkChildsState(WTObject pbo, ObjectReference self) throws WTException {
		if (pbo == null || !(pbo instanceof WTPart)) {
			return;
		}

		// 获取部件下所有子件信息
		Collection<WTPart> partMultiwallStructure = ChangePartQueryUtils.getPartMultiwallStructure((WTPart) pbo);
		if (partMultiwallStructure == null || partMultiwallStructure.size() == 0) {
			return;
		}
		if (!((WTPart) pbo).getNumber().startsWith("A") && !((WTPart) pbo).getNumber().startsWith("B")) {
			if (partMultiwallStructure.size() > 0) {
				throw new WTException("除成品、半成品外不允许搭BOM！");
			}
		}
		List<String> reviewlist = getReviewparts(pbo, self);
		String parentView = ((WTPart) pbo).getViewName();
		System.out.println("partMultiwallStructure qr size====" + partMultiwallStructure.size());
		List<WTObject> errorList = new ArrayList<WTObject>();
		List<WTObject> errorList2 = new ArrayList<WTObject>();
		for (WTPart part : partMultiwallStructure) {
			String state = part.getLifeCycleState().toString();
			String view = part.getViewName();
			if (parentView.equalsIgnoreCase("Design") && view.equalsIgnoreCase("Manufacturing")) {
				// throw new WTException("子件:" + part.getNumber() +
				// ",为制造视图(Manufacturing)，不能加到设计视图的bom中,无法启动设计部件归档流程!");
			}
			System.out.println("state========" + state);
			if ((!state.equals(ChangeConstants.ARCHIVED)) && (!state.equals(ChangeConstants.RELEASED))) {
				errorList.add(part);
			}
			if (state.equals(ChangeConstants.ARCHIVED)) {
				System.out.println("part.getNumber()===:" + part.getNumber());
				// System.out.println("reviewlist===:" +
				// getCollectionDisplayInfo(reviewlist));
				System.out.println("reviewlist===:" + reviewlist.size());
				if (!reviewlist.contains(part.getNumber())) {
					errorList2.add(part);
				}
			}
			if (part.getNumber().startsWith("Q")) {
				throw new WTException(part.getNumber() + ":为临时编码，不能添加到BOM中，请移除或替换后提交！");
			}

			System.out.println("errorList=1=======" + errorList.size());
			checkallSubpart(errorList, part);
			System.out.println("errorList=2=======" + errorList.size());

			// 检查全局替代料状态
			WTCollection wtcol = WTPartHelper.service.getAlternateLinks((WTPartMaster) part.getMaster());
			Iterator ite = wtcol.iterator();
			while (ite.hasNext()) {
				Object obj = ite.next();

				if (obj != null && obj instanceof ObjectReference) {
					ObjectReference objRef = (ObjectReference) obj;

					Object tempObj = objRef.getObject();

					if (tempObj != null && tempObj instanceof WTPartAlternateLink) {
						WTPartAlternateLink alternateLink = (WTPartAlternateLink) tempObj;

						WTPartMaster alternatePartMaster = alternateLink.getAlternates();
						System.out.println("alternatePartMaster number=" + alternatePartMaster.getNumber());
						WTPart alPart = PartUtil.getLastestWTPartByNumber(alternatePartMaster.getNumber());
						String alpartstate = alPart.getState().toString();
						if (!alpartstate.equalsIgnoreCase("RELEASED") && !alpartstate.equalsIgnoreCase("ARCHIVED")) {
							errorList.add(alPart);

						}
					}
				}
			}

		}
		System.out.println("errorList=3=======" + errorList.size());
		// 检查特定替代料状态
		QueryResult qr = WTPartHelper.service.getUsesWTPartMasters((WTPart) pbo);
		while (qr.hasMoreElements()) {
			WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
			checkSubpart(link, errorList);
		}
		System.out.println("errorList=4=======" + errorList.size());
		if (errorList.size() > 0) {
			StringBuilder errorMsg = new StringBuilder();
			errorMsg.append("状态不为‘已归档’或‘已发布’的子件或替代料：");
			errorMsg.append(getCollectionDisplayInfo(errorList));
			throw new WTException(errorMsg.toString());
		}
		if (errorList2.size() > 0) {
			StringBuilder errorMsg = new StringBuilder();
			errorMsg.append("状态为‘已归档’的部件应当加到随签对象中：");
			errorMsg.append(getCollectionDisplayInfo(errorList2));
			throw new WTException(errorMsg.toString());
		}
	}

	// 检查特定替代料状态
	public static void checkallSubpart(List<WTObject> errorList, WTPart part) {
		QueryResult qr = null;
		try {
			qr = WTPartHelper.service.getUsesWTPartMasters(part);
		} catch (WTException e) {
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

	/**
	 * 获取所有的全局替代关系
	 * 
	 * @param partMaster
	 * @return
	 */
	public static QueryResult getAltLink(WTPartMaster partMaster) {
		long id2a2 = PersistenceHelper.getObjectIdentifier(partMaster).getId();
		try {
			QuerySpec queryspec = new QuerySpec();
			int b = queryspec.appendClassList(WTPartAlternateLink.class, true);
			queryspec.setAdvancedQueryEnabled(true);
			String[] aliases = new String[1];
			aliases[0] = queryspec.getFromClause().getAliasAt(b);
			TableColumn tc1 = new TableColumn(aliases[0], "IDA3A5");
			CompositeWhereExpression andExpression = new CompositeWhereExpression(LogicalOperator.AND);
			andExpression.append(new SearchCondition(tc1, "=", new ConstantExpression(new Long(id2a2))));
			queryspec.appendWhere(andExpression, null);
			QueryResult qs = PersistenceHelper.manager.find(queryspec);
			if (qs.hasMoreElements()) {
				return qs;
			}
		} catch (QueryException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
		return null;

	}

	// 检查pbo是否存在未完成的变更
	public static Boolean checkExistEC(WTObject pbo) {
		Boolean isworkECN = true;
		try {
			WTCollection collection = RelatedChangesQueryCommands.getRelatedResultingChangeNotices((WTPart) pbo);
			if (!collection.isEmpty()) {
				Iterator iterator = collection.iterator();
				while (iterator.hasNext()) {
					ObjectReference objReference = (ObjectReference) iterator.next();
					WTChangeOrder2 ecn = (WTChangeOrder2) objReference.getObject();
					System.out.println("ecn number==" + ecn.getNumber());

					System.out.println(
							"ecn number===" + ecn.getNumber() + "state=" + ecn.getState().getState().toString());
					if (ecn.getState().toString().equalsIgnoreCase("UNDERREVIEW")
							|| ecn.getState().toString().equalsIgnoreCase("OPEN")
							|| ecn.getState().toString().equalsIgnoreCase("IMPLEMENTATION")
							|| ecn.getState().toString().equalsIgnoreCase("REWORK")) {
						isworkECN = false;
					}
				}

			}
		} catch (WTException e) {
			e.printStackTrace();
		}

		return isworkECN;
	}

	/***
	 * 检查BOM结构中位号是否包含中文字符
	 * 
	 * @ PCBA位号增加校验，需添加校验：第一位为字母第二位不允许为0 -mao
	 * @param pbo
	 * @throws WTException
	 */
	public void checkReferenceDesignatorRange(WTObject pbo) throws WTException {
		if (pbo == null || !(pbo instanceof WTPart)) {
			return;
		}

		// 获取BOM结构
		WTCollection childrenLinks = PIPartHelper.service.findChildrenLinks((WTPart) pbo);
		// 收集错误信息
		StringBuilder errorMsg = new StringBuilder();

		StringBuilder errorMsg2 = new StringBuilder();
		for (Object object : childrenLinks) {
			if (object instanceof ObjectReference) {
				object = ((ObjectReference) object).getObject();
			}
			if (object instanceof WTPartUsageLink) {
				String value = MBAUtil.getValue((WTPartUsageLink) object, "referenceDesignatorRange") == null ? ""
						: (String) MBAUtil.getValue((WTPartUsageLink) object, "referenceDesignatorRange");
				// 检查位号是否存在中文字符
				char[] valueArray = value.toCharArray();
				for (int i = 0; i < valueArray.length; i++) {
					char chinese = valueArray[i];
					if (isChinese(chinese)) {
						errorMsg.append(((WTPartUsageLink) object).getDisplayIdentity() + "\n");
					}
				}

				if (valueArray.length >= 2) {
					char c1 = valueArray[0];
					char c2 = valueArray[1];
					if (!Character.isLetter(c1)) {
						errorMsg2.append(((WTPartUsageLink) object).getDisplayIdentity() + "第一位需为字母" + "\n");
					}
					if (c2 == '0') {
						errorMsg2.append(((WTPartUsageLink) object).getDisplayIdentity() + "第二位不能为0" + "\n");
					}

				}
			}
		}
		if (errorMsg.length() > 0) {
			errorMsg.append("以上部件关系中的位号中含有中文字符，无法提交流程!");
			throw new WTException(errorMsg.toString());
		}

		if (errorMsg2.length() > 0) {
			throw new WTException(errorMsg2.toString());
		}
	}

	/***
	 * 检查BOM中一个子件不能存在多行
	 * 
	 * @param pbo
	 * @throws WTException
	 */
	public void checkSameChildParts(WTObject pbo) throws WTException {
		if (pbo == null || !(pbo instanceof WTPart)) {
			return;
		}

		Collection<WTPart> childArray = new HashSet<WTPart>();
		// 查询第一层结构信息
		WTList wtList = new WTArrayList();
		wtList.add((WTPart) pbo);
		QueryResult queryResult = ChangePartQueryUtils.getPartsFirstStructure(wtList, new LatestConfigSpec());
		while (queryResult.hasMoreElements()) {
			Persistable[] persistables = (Persistable[]) queryResult.nextElement();
			// 子件对象
			Object object = persistables[1];
			if (object instanceof WTPart) {
				WTPart childPart = (WTPart) object;
				if (childArray.contains(childPart)) {
					throw new WTException(
							"BOM编码" + ((WTPart) pbo).getNumber() + "的子件" + childPart.getNumber() + "存在多行，不允许提交！");
				}
				childArray.add(childPart);
			}
		}
	}

	/**
	 * 判定输入的是否是汉字
	 *
	 * @param c
	 *            被校验的字符
	 * @return true 代表是汉字
	 */
	public boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	public String checkSoftTypeSetRole(WTObject pbo, ObjectReference self, String processName, String username,
			String containerNames) throws WTException {
		StringBuffer result = new StringBuffer();

		if (pbo != null && pbo instanceof WTPart) {
			WTPart part = (WTPart) pbo;
			System.out.println("p number==" + part.getNumber());

			if (part.getNumber().startsWith("X")) {

				// Object valueObject =
				// PIAttributeHelper.service.getValue(part.getContainer(),
				// "ssgs");
				// String company = valueObject == null ? "" : (String)
				// valueObject;
				// System.out.println("p company==" + company);

				String containerName = part.getContainerName();
				if (!"激光电视产品库".equals(containerName) && !"微投产品库".equals(containerName)) {

					// if ("APPO".equals(company)) {

					String nodeHierarchy = "";// 获取分类全路径
					// 获取分类内部值
					String value = "";
					value = (String) PIAttributeHelper.service.getValue(part, "Classification");
					nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(value);

					if (nodeHierarchy.contains("appo_rj01") || nodeHierarchy.contains("appo_rj02")
							|| nodeHierarchy.contains("appo_rj03") || nodeHierarchy.contains("appo_rj04")
							|| nodeHierarchy.contains("appo_rj06")) {
						// 归档流程
						if (processName != null && processName.contains("GenericPartWF")) {
							CheckTeam(pbo, "manufacturing_manager", "Receiver");
							CheckTeam(pbo, "hardware_development", "Receiver");
							// 判断是否是光机产品库，且是否添加程国平，若无添加程国平00409
							if (containerNames.contains(part.getContainerName())) {

								if (CheckProcessTeamUser(pbo, username, "Receiver")) {
								} else {
									// 添加
									AddTeamRole(pbo, "Receiver", username);
								}
							}

						}
						// 发布流程
						else if (processName != null && processName.contains("APPO_ReleasedPartWF")) {
							CheckTeam(pbo, "project_manager", "Receiver");
							// 判断是否是光机产品库，且是否添加程国平，若无添加程国平000409
							if (containerNames.contains(part.getContainerName())) {

								if (CheckProcessTeamUser(pbo, username, "Receiver")) {
								} else {
									// 添加
									AddTeamRole(pbo, "Receiver", username);
								}
							}
						}
					} else if (nodeHierarchy.contains("appo_rj05") || nodeHierarchy.contains("appo_rj07")) {
						if (processName != null && processName.contains("GenericPartWF")) {
							CheckTeam(pbo, "manufacturing_representative", "Receiver");
						} else if (processName != null && processName.contains("APPO_ReleasedPartWF")) {
							CheckTeam(pbo, "project_manager", "Receiver");
						}
					}
				}
			}

		}

		return result.toString();
	}

	public String getPartSSGS(WTObject pbo) throws PIException {
		String company = "APPO";
		if (pbo instanceof WTPart) {
			WTPart part = (WTPart) pbo;
			part.getNumber().startsWith("E");
			Object valueObject = PIAttributeHelper.service.getValue(part, "ssgs");
			company = valueObject == null ? "" : (String) valueObject;
		}
		return company;
	}

	/**
	 * 检查光峰的BOM归档发布时，结构中不能有小米定制的物料（除“激光产品库”“微投产品库”外）
	 * 
	 * @param pbo
	 * @throws WTException
	 */
	public void checkMICustomizeorNot(WTObject pbo) throws WTException {
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);

		try {
			if (pbo == null || !(pbo instanceof WTPart)) {
				return;
			}
			WTPart parentPart = (WTPart) pbo;
			String containerName = parentPart.getContainerName();
			if (!"激光电视产品库".equals(containerName) && !"微投产品库".equals(containerName)) {

				// 查询第一层结构信息
				WTList wtList = new WTArrayList();
				wtList.add(parentPart);
				QueryResult queryResult = ChangePartQueryUtils.getPartsFirstStructure(wtList, new LatestConfigSpec());
				while (queryResult.hasMoreElements()) {
					Persistable[] persistables = (Persistable[]) queryResult.nextElement();
					// 子件对象
					Object object = persistables[1];
					if (object instanceof WTPart) {
						WTPart childPart = (WTPart) object;
						Object valueObject = PIAttributeHelper.service.getValue(childPart,
								"MI_Customize_orNot");
						String MICustomizeorNot = valueObject == null ? "" : (String) valueObject;
						if ("是".equals(MICustomizeorNot)) {
							throw new WTException("BOM编码"
									+ (parentPart.getNumber() + "的子件" + childPart.getNumber() + "是小米定制物料，请删除！"));
						}
					}
				}
			}
		} finally {
			SessionServerHelper.manager.setAccessEnforced(enforce);
		}
	}

	/**
	 * 检查光峰的BOM归档发布时，结构中不能【新增】优选等级为“禁选”的物料
	 * 
	 * @param pbo
	 * @throws WTException
	 */
	public void checkPartYxdj(WTObject pbo) throws WTException {

		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);

		try {

			if (pbo == null || !(pbo instanceof WTPart)) {
				return;
			}
			WTPart parentPart = (WTPart) pbo;

			String version = parentPart.getVersionInfo().getIdentifier().getValue();

			WTPart prioParentPart = getPrioPart(parentPart, parentPart.getViewName());
			Set<WTPartMaster> prioParentPartSet = getMonolayerPart(prioParentPart);

			// 查询第一层结构信息
			WTList wtList = new WTArrayList();
			wtList.add(parentPart);
			QueryResult queryResult = ChangePartQueryUtils.getPartsFirstStructure(wtList, new LatestConfigSpec());
			while (queryResult.hasMoreElements()) {
				Persistable[] persistables = (Persistable[]) queryResult.nextElement();
				// 子件对象
				Object object = persistables[1];
				if (object instanceof WTPart) {
					WTPart childPart = (WTPart) object;
					Object valueObject = PIAttributeHelper.service.getValue(childPart, "yxdj");
					String yxdj = valueObject == null ? "" : (String) valueObject;
					if ("禁选".equals(yxdj) && "A".equals(version)) {
						throw new WTException(
								"BOM编码" + (parentPart.getNumber() + "的子件" + childPart.getNumber() + "优选等级是【禁选】，请删除！"));
					} else if ("禁选".equals(yxdj) && !checkPrioBOMHavePart(prioParentPartSet, childPart)) {
						throw new WTException(
								"BOM编码" + (parentPart.getNumber() + "的子件" + childPart.getNumber() + "优选等级是【禁选】，请删除！"));
					}
				}
			}
		} finally {
			SessionServerHelper.manager.setAccessEnforced(enforce);
		}
	}

	/**
	 * 检查上一个版本的BOM中是否存在当前的子件
	 * 
	 * @param prioParentPartSet
	 * @param childPart
	 * @return
	 */
	private static boolean checkPrioBOMHavePart(Set<WTPartMaster> prioParentPartSet, WTPart childPart) {
		boolean result = false;

		if (prioParentPartSet != null && prioParentPartSet.size() > 0) {

			for (WTPartMaster master : prioParentPartSet) {
				if (childPart.getNumber().equals(master.getNumber())) {
					return true;
				}
			}

		}

		return result;
	}

	/**
	 * 获取上一个同视图的版本
	 * 
	 * @param part
	 * @param viewName
	 * @return
	 * @throws WTException
	 */
	private static WTPart getPrioPart(WTPart part, String viewName) throws WTException {
		// 获取所有大版本的最新小版本
		QueryResult queryResult = VersionControlHelper.service.allVersionsOf(part.getMaster());
		String version = part.getVersionInfo().getIdentifier().getValue();
		System.out.println("version====" + version + "  viewName==" + viewName);
		while (queryResult.hasMoreElements()) {
			WTPart wtpart = (WTPart) queryResult.nextElement();
			String version2 = wtpart.getVersionInfo().getIdentifier().getValue();
			String view = wtpart.getViewName();

			System.out.println("version2====" + version2 + "  view==" + view);
			if (view.equals(viewName) && !version2.equals(version)) {
				return wtpart;
			}
		}
		return null;
	}

	private static Set<WTPartMaster> getMonolayerPart(WTPart part) {
		Set<WTPartMaster> setPart = new HashSet<WTPartMaster>();
		if (part != null) {

			try {
				QueryResult qr = WTPartHelper.service.getUsesWTPartMasters(part);
				if (qr != null && qr.size() > 0) {
					//
					while (qr.hasMoreElements()) {
						WTPartUsageLink links = (WTPartUsageLink) qr.nextElement();
						WTPartMaster masterChild = links.getUses();

						setPart.add(masterChild);
					}
				}
			} catch (WTException e) {
				e.printStackTrace();
			}
		}
		return setPart;
	}

}
