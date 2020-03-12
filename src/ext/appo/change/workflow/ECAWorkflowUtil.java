package ext.appo.change.workflow;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.lowagie.text.DocumentException;
import com.ptc.windchill.enterprise.team.server.TeamCCHelper;

import ext.appo.change.ModifyHelper;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.models.CorrelationObjectLink;
import ext.appo.change.util.ModifyUtils;
import ext.appo.ecn.common.util.ChangePartQueryUtils;
import ext.appo.ecn.common.util.ChangeUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.part.beans.EffectiveBaselineBean;
import ext.appo.part.util.EffecitveBaselineUtil;
import ext.appo.part.util.MversionControlHelper;
import ext.appo.part.workflow.PartWorkflowUtil;
import ext.appo.util.PartUtil;
import ext.com.workflow.WorkflowUtil;
import ext.generic.integration.cis.constant.CISConstant;
import ext.generic.integration.cis.rule.CISBusinessRuleXML;
import ext.generic.integration.cis.util.OracleUtil;
import ext.generic.integration.cis.util.SQLServerUtil;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.lang.PIStringUtils;
import ext.pi.PIException;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIClassificationHelper;
import ext.pi.core.PICoreHelper;
import wt.change2.ChangeActivityIfc;
import wt.change2.ChangeHelper2;
import wt.change2.Changeable2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.doc.WTDocument;
import wt.fc.ObjectReference;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTCollection;
import wt.inf.container.WTContainer;
import wt.inf.team.ContainerTeam;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.team.ContainerTeamManaged;
import wt.lifecycle.LifeCycleManaged;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.part.WTPartAlternateLink;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartSubstituteLink;
import wt.part.WTPartUsageLink;
import wt.pds.StatementSpec;
import wt.project.Role;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.WhereExpression;
import wt.session.SessionServerHelper;
import wt.team.Team;
import wt.team.WTRoleHolder2;
import wt.type.ClientTypedUtility;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.wip.Workable;
import wt.workflow.engine.WfProcess;

public class ECAWorkflowUtil implements ChangeConstants, ModifyConstants {

	private static Logger LOGGER = LogR.getLogger(ECAWorkflowUtil.class.getName());
	private Set<String> MESSAGES = new HashSet<>();

	/***
	 * 检查子件状态是否为‘已归档’或‘已发布’（设计视图）
	 *
	 * @param pbo
	 * @throws WTException
	 */
	public void checkChildState(WTObject pbo) throws WTException {
		if (!(pbo instanceof WTChangeActivity2))
			return;

		Collection<Changeable2> changeable2s = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
		LOGGER.info("=====checkChildState.changeable2s: " + changeable2s.size());
		for (Changeable2 changeable2 : changeable2s) {
			LOGGER.info("=====checkChildState.changeable2: " + changeable2);
			if (changeable2 instanceof WTPart) {
				WTPart parent = (WTPart) changeable2;
				PartWorkflowUtil partUtil = new PartWorkflowUtil();
				partUtil.checkChildsState(parent);

				// String parentNumber = parent.getNumber();
				// LOGGER.info("=====checkChildState.parentNumber: " +
				// parentNumber);
				// String viewname = parent.getViewName();
				// if (viewname.contains("Design")) {
				// //获取部件下所有子件信息
				// Collection<WTPart> collection =
				// ChangePartQueryUtils.getPartMultiwallStructure(parent);
				// LOGGER.info("=====checkChildState.collection: " +
				// collection);
				// if (collection.isEmpty()) return;
				//
				// if (!parentNumber.startsWith("A") &&
				// !parentNumber.startsWith("B"))
				// throw new WTException("除成品、半成品外不允许搭BOM！");
				//
				// //检查首层子件特定替代料状态
				// MESSAGES.addAll(checkAllSubstitute(parent));
				//
				// String parentView = parent.getViewName();
				// LOGGER.info("=====checkChildState.parentView: " +
				// parentView);
				// for (WTPart part : collection) {
				// String childNumber = part.getNumber();
				// LOGGER.info("=====checkChildState.childNumber: " +
				// childNumber);
				// String state = part.getLifeCycleState().toString();
				// LOGGER.info("=====checkChildState.state: " + state);
				// String view = part.getViewName();
				// LOGGER.info("=====checkChildState.view: " + view);
				//
				// if (parentView.equalsIgnoreCase("Design") &&
				// view.equalsIgnoreCase("Manufacturing")) {
				//// MESSAGES.add("子件「" + childNumber +
				// "」为制造视图(Manufacturing)，不能加到设计视图(Design)「" + parentNumber +
				// "」的BOM中！");
				// }
				//
				// if (!state.equals(ARCHIVED) && !state.equals(RELEASED)) {
				// MESSAGES.add("子件「" + childNumber + "」的状态不为“已归档”或“已发布”！");
				// }
				//
				// if (childNumber.startsWith("Q")) {
				// MESSAGES.add("子件「" + childNumber + "」为临时编码，不能添加到「" +
				// parentNumber + "」的BOM中！");
				// }
				//
				// //检查子件特定替代料
				// MESSAGES.addAll(checkAllSubstitute(part));
				//
				//
				// //检查全局替代料状态
				// MESSAGES.addAll(checkAlternateLinks(part));
				// }
				// }

			}
		}
	}

	/***
	 * 检查BOM结构中位号是否包含中文字符
	 * 
	 * @ PCBA位号增加校验，需添加校验：第一位为字母第二位不允许为0 -mao
	 * @param pbo
	 * @throws WTException
	 */
	public void checkReferenceDesignatorRange(WTObject pbo) throws WTException {
		if (!(pbo instanceof WTChangeActivity2))
			return;

		Collection<Changeable2> changeable2s = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
		LOGGER.info("=====checkReferenceDesignatorRange.changeable2s: " + changeable2s.size());
		for (Changeable2 changeable2 : changeable2s) {
			LOGGER.info("=====checkReferenceDesignatorRange.changeable2: " + changeable2);
			if (changeable2 instanceof WTPart) {
				WTPart parent = (WTPart) changeable2;
				PartWorkflowUtil partUtil = new PartWorkflowUtil();
				partUtil.checkReferenceDesignatorRange(parent);

				// WTCollection collection =
				// PIPartHelper.service.findChildrenLinks(parent);//获取BOM结构
				// LOGGER.info("=====checkReferenceDesignatorRange.collection: "
				// + collection.size());
				// for (Object object : collection) {
				// if (object instanceof ObjectReference) {
				// object = ((ObjectReference) object).getObject();
				// }
				// LOGGER.info("=====checkReferenceDesignatorRange.object: " +
				// object);
				// if (object instanceof WTPartUsageLink) {
				// WTPartUsageLink usageLink = (WTPartUsageLink) object;
				// String value = MBAUtil.getValue(usageLink, ATTRIBUTE_5) ==
				// null ? "" : (String) MBAUtil.getValue(usageLink,
				// ATTRIBUTE_5);
				// LOGGER.info("=====checkReferenceDesignatorRange.value: " +
				// value);
				// // 检查位号是否存在中文字符
				// char[] valueArray = value.toCharArray();
				// for (char chinese : valueArray) {
				// if (isChinese(chinese)) {
				// MESSAGES.add(usageLink.getDisplayIdentity() +
				// "「位号」中含有中文字符！");
				// }
				// }
				// if (valueArray.length >= 2) {
				// char c1 = valueArray[0];
				// char c2 = valueArray[1];
				// if (!Character.isLetter(c1)) {
				// MESSAGES.add(usageLink.getDisplayIdentity() +
				// "「位号」第一位需为字母！");
				// }
				// if (c2 == '0') {
				// MESSAGES.add(usageLink.getDisplayIdentity() +
				// "「位号」第二位不能为0！");
				// }
				// }
				// }
				// }
			}
		}
	}

	/***
	 * 检查BOM中一个子件不能存在多行
	 * 
	 * @param pbo
	 * @throws WTException
	 */
	public void checkSameChildParts(WTObject pbo) throws WTException {
		if (!(pbo instanceof WTChangeActivity2))
			return;

		Collection<Changeable2> changeable2s = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
		LOGGER.info("=====checkSameChildParts.changeable2s: " + changeable2s.size());
		for (Changeable2 changeable2 : changeable2s) {
			LOGGER.info("=====checkSameChildParts.changeable2: " + changeable2);
			if (changeable2 instanceof WTPart) {
				WTPart parent = (WTPart) changeable2;
				PartWorkflowUtil partUtil = new PartWorkflowUtil();
				partUtil.checkSameChildParts(parent);

				// Collection<WTPart> childArray = new HashSet<>();//所有子件集合
				// Set<WTPart> repetitions = new HashSet<>();//重复子件集合
				// // 查询第一层结构信息
				// WTList list = new WTArrayList();
				// list.add(parent);
				// QueryResult queryResult =
				// ChangePartQueryUtils.getPartsFirstStructure(list, new
				// LatestConfigSpec());
				// LOGGER.info("=====checkSameChildParts.queryResult: " +
				// queryResult.size());
				// while (queryResult.hasMoreElements()) {
				// Persistable[] persistables = (Persistable[])
				// queryResult.nextElement();
				// LOGGER.info("=====checkSameChildParts.persistables: " +
				// persistables.length);
				// Object object = persistables[1];//子件对象
				// LOGGER.info("=====checkSameChildParts.object: " + object);
				// if (object instanceof WTPart) {
				// WTPart childPart = (WTPart) object;
				// LOGGER.info("=====checkSameChildParts.childArray: " +
				// childArray);
				// if (childArray.contains(childPart)) {
				// repetitions.add(childPart);
				// } else {
				// childArray.add(childPart);
				// }
				// }
				// }
				//
				// for (WTPart part : repetitions) {
				// MESSAGES.add("BOM「" + parent.getNumber() + "」的子件「" +
				// part.getNumber() + "」存在多行！");
				// }
			}
		}
	}

	/**
	 * 检查BOM中子件的单位和物料的单位是否一致
	 * 
	 * @param pbo
	 * @throws WTException
	 */
	public void checkBOMUnitBySelf(ObjectReference self1) throws WTException {
		PartWorkflowUtil partUtil = new PartWorkflowUtil();
		partUtil.CheckBOMUnit(self1);

	}

	/**
	 * 检查BOM中子件的单位和物料的单位是否一致
	 * 
	 * @param pbo
	 * @throws WTException
	 */
	public void checkBOMUnit(WTObject pbo) throws WTException {
		if (!(pbo instanceof WTChangeActivity2))
			return;

		Collection<Changeable2> changeable2s = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
		LOGGER.info("=====checkBOMUnit.changeable2s: " + changeable2s.size());
		for (Changeable2 changeable2 : changeable2s) {
			LOGGER.info("=====checkBOMUnit.changeable2: " + changeable2);
			if (changeable2 instanceof WTPart) {
				WTPart parent = (WTPart) changeable2;
				QueryResult result = WTPartHelper.service.getUsesWTPartMasters(parent);
				while (result.hasMoreElements()) {
					WTPartUsageLink link = (WTPartUsageLink) result.nextElement();
					String childNumber = link.getUses().getNumber();
					LOGGER.info("=====checkBOMUnit.childNumber: " + childNumber);

					WTPart child = PartUtil.getLastestWTPartByNumber(childNumber);
					LOGGER.info("=====checkBOMUnit.child: " + child);

					String defaultUnit = CommonPDMUtil.getUnitCN(child.getDefaultUnit());
					LOGGER.info("=====checkBOMUnit.defaultUnit: " + defaultUnit);

					String unit = CommonPDMUtil.getUnitCN(link.getQuantity().getUnit());
					LOGGER.info("=====checkBOMUnit.unit: " + unit);

					if (unit.contains("item") && defaultUnit.contains("PCS")) {

					} else {
						if (!defaultUnit.equalsIgnoreCase(unit)) {
							MESSAGES.add(parent.getNumber() + "的子件：" + childNumber + "单位: " + unit + " ，与物料单位："
									+ defaultUnit + "不符！");
						}
					}
				}
			}
		}
	}

	/**
	 * 检查受影响对象、产生对象是否检出
	 * 
	 * @param pbo
	 * @throws WTException
	 */
	public void isCheckout(WTObject pbo) throws WTException {
		if (!(pbo instanceof WTChangeActivity2))
			return;

		Collection<Changeable2> befores = ModifyUtils.getChangeablesBefore((WTChangeActivity2) pbo);
		LOGGER.info("=====isCheckout.befores: " + befores.size());
		for (Changeable2 before : befores) {
			LOGGER.info("=====isCheckout.before: " + before);
			if (!(before instanceof Workable))
				continue;
			if (PICoreHelper.service.isCheckout((Workable) before)) {
				MESSAGES.add("受影响对象「" + ModifyUtils.getNumber(before) + "」被检出！");
			}
		}

		Collection<Changeable2> afters = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
		LOGGER.info("=====isCheckout.afters: " + afters.size());
		for (Changeable2 after : afters) {
			LOGGER.info("=====isCheckout.after: " + after);
			if (!(after instanceof Workable))
				continue;
			if (PICoreHelper.service.isCheckout((Workable) after)) {
				MESSAGES.add("产生对象「" + ModifyUtils.getNumber(after) + "」被检出！");
			}
		}
	}

	/**
	 * 判断ECN流程中是否强制选择会签人员
	 * 
	 * @param pbo
	 * @param self
	 * @return
	 * @throws WTException
	 */
	public String checkPartType(WTObject pbo, ObjectReference self) throws WTException {
		List<String> isRDPart = new ArrayList<>();
		if (pbo instanceof WTChangeActivity2) {
			Collection<Changeable2> collection = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
			LOGGER.info("=====checkPartType.collection: " + collection);
			for (Changeable2 changeable2 : collection) {
				if (changeable2 instanceof WTPart) {
					WTPart part = (WTPart) changeable2;
					LOGGER.info("=====checkPartType.part: " + part.getNumber());
					if (part.getNumber().startsWith("A") || part.getNumber().startsWith("C")
							|| part.getNumber().startsWith("D") || part.getNumber().startsWith("E")
							|| part.getNumber().startsWith("P") || part.getNumber().startsWith("T")
							|| part.getNumber().startsWith("S") || part.getNumber().startsWith("K")) {
						return "RD";
					}
					if (part.getNumber().startsWith("B")) {
						String value = (String) PIAttributeHelper.service.getValue(part, "Classification");
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
		LOGGER.info("=====checkPartType.isRDPart: " + isRDPart);
		return "NOTRD";
	}

	/**
	 * 检查团队角色
	 * 
	 * @param pbo
	 * @param rolename
	 * @param reviewname
	 * @throws WTException
	 */
	public void checkTeam(WTObject pbo, String rolename, String reviewname) throws WTException {
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		Role role = Role.toRole(reviewname);
		Role selectrole = Role.toRole(rolename);
		Boolean isrole = false;

		if (pbo instanceof WTChangeOrder2) {
			WTChangeOrder2 ecn = (WTChangeOrder2) pbo;
			Team processTeam = WorkflowUtil.getTeam(ecn);
			if (processTeam != null) {
				Enumeration enumPrin = processTeam.getPrincipalTarget(role);// 会签者

				while (enumPrin.hasMoreElements()) {
					WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
					WTPrincipal principal = tempPrinRef.getPrincipal();
					LOGGER.info("=====checkTeam.principal: " + principal.getName());
					isrole = checkRole(pbo, rolename, principal);
					LOGGER.info("=====checkTeam.isrole: " + isrole);
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
		} else if (pbo instanceof WTChangeActivity2) {
			WTChangeActivity2 activity2 = (WTChangeActivity2) pbo;
			Team processTeam = WorkflowUtil.getTeam(activity2);
			if (processTeam != null) {
				Enumeration enumPrin = processTeam.getPrincipalTarget(role);// 会签者

				while (enumPrin.hasMoreElements()) {
					WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
					WTPrincipal principal = tempPrinRef.getPrincipal();
					LOGGER.info("=====checkTeam.principal: " + principal.getName());
					isrole = checkRole(pbo, rolename, principal);
					LOGGER.info("=====checkTeam.isrole: " + isrole);
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
		} else if (pbo instanceof WTPart) {
			WTPart part = (WTPart) pbo;
			Team processTeam = WorkflowUtil.getTeam(part);
			if (processTeam != null) {
				Enumeration enumPrin = processTeam.getPrincipalTarget(role);// 会签者
				while (enumPrin.hasMoreElements()) {
					WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
					WTPrincipal principal = tempPrinRef.getPrincipal();
					LOGGER.info("=====checkTeam.principal: " + principal.getName());
					isrole = checkRole(pbo, rolename, principal);
					LOGGER.info("=====checkTeam.isrole: " + isrole);
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
		} else if (pbo instanceof WTDocument) {
			WTDocument doc = (WTDocument) pbo;
			Team processTeam = WorkflowUtil.getTeam(doc);
			if (processTeam != null) {
				Enumeration enumPrin = processTeam.getPrincipalTarget(role);// 会签者
				while (enumPrin.hasMoreElements()) {
					WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
					WTPrincipal principal = tempPrinRef.getPrincipal();
					LOGGER.info("=====checkTeam.principal: " + principal.getName());
					isrole = checkRole(pbo, rolename, principal);
					LOGGER.info("=====checkTeam.isrole: " + isrole);
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

	/**
	 * 检查指定角色是否在团队中
	 * 
	 * @param pbo
	 * @param rolename
	 * @param principal
	 * @return
	 */
	@SuppressWarnings("SuspiciousMethodCalls")
	public static Boolean checkRole(WTObject pbo, String rolename, WTPrincipal principal) {
		Boolean isrole = false;

		Role role = Role.toRole(rolename);
		System.out.println("role===" + role.getShortDescription());
		System.out.println("pbo===" + pbo);
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
		// add by lzy at 20191212 start
		if (pbo instanceof WTChangeActivity2) {
			WTChangeActivity2 object = (WTChangeActivity2) pbo;
			container = object.getContainer();
		}
		// add by lzy at 20191212 end
		if (container != null) {
			try {
				WTRoleHolder2 wtroleholder2 = TeamCCHelper.getTeamFromObject(container);
				ContainerTeam containerTeam = ContainerTeamHelper.service
						.getContainerTeam((ContainerTeamManaged) container);
				Enumeration enumPrin = containerTeam.getPrincipalTarget(role);
				Set<WTUser> userSet = new HashSet<>();
				while (enumPrin.hasMoreElements()) {
					WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
					WTPrincipal wtPrincipal = tempPrinRef.getPrincipal();
					System.out.println("wtprincipal====" + wtPrincipal.getName());

					if (wtPrincipal instanceof WTUser) {
						userSet.add((WTUser) wtPrincipal);
					} else if (wtPrincipal instanceof wt.org.WTGroup) {
						userSet.addAll(PartWorkflowUtil.getGroupMembers((wt.org.WTGroup) wtPrincipal));
					}
				}

				if (userSet != null && userSet.size() > 0 && userSet.contains(principal)) {
					isrole = true;
				}
			} catch (WTException e) {
				e.printStackTrace();
			} // 获取容器的人

		}

		return isrole;
	}

	/**
	 * 根据产品类别，判断是否需要加人
	 * 
	 * @param pbo
	 * @return
	 */
	public Boolean checkContainer(WTObject pbo) {
		Boolean isok = false;
		if (pbo instanceof WTChangeOrder2) {
			WTChangeOrder2 ecn = (WTChangeOrder2) pbo;
			String productline = "";
			try {
				productline = (String) PIAttributeHelper.service.getValue(ecn, ATTRIBUTE_2);
			} catch (PIException e) {
				e.printStackTrace();
			}
			LOGGER.info("=====checkContainer.productline: " + productline);
			// 微投--40 激光电视--60
			if (productline.equalsIgnoreCase("40") || productline.equalsIgnoreCase("60")) {
				isok = true;
			}
		}
		LOGGER.info("=====checkContainer.isok: " + isok);
		return isok;
	}

	/**
	 * 给流程节点角色添加固定人员 删除用户为空抛出异常
	 * 
	 * @param pbo
	 * @param rolename
	 * @param principalname
	 * @throws WTException
	 */
	public void addTeamRole(WTObject pbo, String rolename, String principalname) throws WTException {
		Role role = Role.toRole(rolename);
		WTPrincipal wtprincipal = PartWorkflowUtil.getUserFromName(principalname);
		if (wtprincipal != null) {
			LOGGER.info("=====addTeamRole.wtprincipal: " + wtprincipal.getName());
			if (pbo instanceof WTChangeOrder2) {
				WTChangeOrder2 ecn = (WTChangeOrder2) pbo;
				Team processTeam = WorkflowUtil.getTeam(ecn);
				if (processTeam != null) {
					processTeam.addPrincipal(role, wtprincipal);
					LOGGER.info("=====addTeamRole.end add people success: ");
				}
			} else if (pbo instanceof WTPart) {
				WTPart wtPart = (WTPart) pbo;
				Team processTeam = WorkflowUtil.getTeam(wtPart);
				if (processTeam != null) {
					processTeam.addPrincipal(role, wtprincipal);
					System.out.println("end add people success");
				}
			} else if (pbo instanceof WTChangeActivity2) {
				WTChangeActivity2 eca = (WTChangeActivity2) pbo;
				Team processTeam = WorkflowUtil.getTeam(eca);
				if (processTeam != null) {
					processTeam.addPrincipal(role, wtprincipal);
					System.out.println("end add people success");
				}
			}
		}
	}

	/**
	 * 检查流程中的角色是否在上下文团队中
	 * 
	 * @param pbo
	 * @throws WTException
	 */
	public void checkUserPermission(WTObject pbo) throws WTException {
		StringBuffer result = new StringBuffer();
		if (pbo instanceof WTChangeActivity2) {
			WTChangeActivity2 activity2 = (WTChangeActivity2) pbo;
			PartWorkflowUtil partWorkflowUtil = new PartWorkflowUtil();
			partWorkflowUtil.checkECNUserPermission(pbo);
		}
	}

	/**
	 * 检查全局替代料状态的状态是否为已归档或已发布
	 * 
	 * @param part
	 * @return
	 * @throws WTException
	 */
	private Set<String> checkAlternateLinks(WTPart part) throws WTException {
		Set<String> messages = new HashSet<>();
		WTCollection collection = WTPartHelper.service.getAlternateLinks(part.getMaster());
		LOGGER.info("=====checkAlternateLinks.collection: " + collection.size());
		for (Object object : collection) {
			if (object instanceof ObjectReference) {
				ObjectReference reference = (ObjectReference) object;
				object = reference.getObject();
			}
			LOGGER.info("=====checkAlternateLinks.object: " + object);
			if (object instanceof WTPartAlternateLink) {
				WTPartAlternateLink link = (WTPartAlternateLink) object;
				WTPartMaster alternateFor = link.getAlternateFor();
				WTPartMaster alternate = link.getAlternates();
				LOGGER.info("=====checkAlternateLinks.alternateFor: " + alternateFor);
				LOGGER.info("=====checkAlternateLinks.alternate: " + alternate);

				WTPart alternatePart = PartUtil.getLastestWTPartByNumber(alternate.getNumber());
				LOGGER.info("=====checkAlternateLinks.alternatePart: " + alternatePart);
				String state = alternatePart.getState().toString();
				LOGGER.info("=====checkAlternateLinks.state: " + state);
				if (!state.equalsIgnoreCase(RELEASED) && !state.equalsIgnoreCase(ARCHIVED)) {
					messages.add("部件「" + alternateFor.getNumber() + "」的全局替代料「" + alternatePart.getNumber()
							+ "」状态不为“已归档”或“已发布”！");
				}
			}
		}
		return messages;
	}

	/**
	 * 检查特定替代料的状态是否为已归档或已发布
	 * 
	 * @param part
	 * @return
	 * @throws WTException
	 */
	private Set<String> checkAllSubstitute(WTPart part) throws WTException {
		Set<String> messages = new HashSet<>();
		Map<WTPartMaster, Set<WTPart>> substituteMap = checkAllSubstitutePart(part);
		LOGGER.info("=====checkAllSubstitute.substituteMap: " + substituteMap);
		for (Map.Entry<WTPartMaster, Set<WTPart>> entry : substituteMap.entrySet()) {
			WTPartMaster master = entry.getKey();
			Set<WTPart> result = entry.getValue();
			LOGGER.info("=====checkAllSubstitute.master: " + master);
			LOGGER.info("=====checkAllSubstitute.result: " + result);
			for (WTPart substitute : result) {
				messages.add("部件「" + master.getNumber() + "」的特定替代料「" + substitute.getNumber() + "」状态不为“已归档”或“已发布”！");
			}
		}
		return messages;
	}

	/**
	 * 获取状态不为已归档、已发布的特定替代料
	 * 
	 * @param part
	 * @return
	 * @throws WTException
	 */
	private Map<WTPartMaster, Set<WTPart>> checkAllSubstitutePart(WTPart part) throws WTException {
		Map<WTPartMaster, Set<WTPart>> map = new HashMap<>();

		QueryResult qr = WTPartHelper.service.getUsesWTPartMasters(part);
		LOGGER.info("=====checkAllSubstitutePart.qr: " + qr.size());
		while (qr.hasMoreElements()) {
			Object object = qr.nextElement();
			LOGGER.info("=====checkAllSubstitutePart.object: " + object);
			if (object instanceof WTPartUsageLink) {
				WTPartUsageLink link = (WTPartUsageLink) object;
				WTPartMaster master = link.getUses();
				map.put(master, checkSubstitutePart((WTPartUsageLink) object));
			}
		}
		LOGGER.info("=====checkAllSubstitutePart.map: " + map);

		return map;
	}

	/**
	 * 获取状态不为已归档、已发布的特定替代料
	 * 
	 * @param usageLink
	 * @return
	 * @throws WTException
	 */
	private Set<WTPart> checkSubstitutePart(WTPartUsageLink usageLink) throws WTException {
		Set<WTPart> result = new HashSet<>();

		ArrayList<String> list = getSubstitutePart(usageLink);
		for (String number : list) {
			WTPart part = PartUtil.getLastestWTPartByNumber(number);
			LOGGER.info("=====checkSubstitutePart.part: " + part);
			String state = part.getState().toString();
			LOGGER.info("=====checkSubstitutePart.state: " + state);
			if (!state.equalsIgnoreCase(RELEASED) && !state.equalsIgnoreCase(ARCHIVED)) {
				result.add(part);
			}
		}

		return result;
	}

	/**
	 * 获取特定替代件
	 * 
	 * @param link
	 * @return
	 * @throws WTException
	 */
	private ArrayList<String> getSubstitutePart(WTPartUsageLink link) throws WTException {
		ArrayList<String> list = new ArrayList<>();

		WTCollection collection = WTPartHelper.service.getSubstituteLinks(link);
		LOGGER.info("=====getSubstitutePart.collection: " + collection.size());
		for (Object object : collection) {
			LOGGER.info("=====getSubstitutePart.object1: " + object);
			if (object instanceof ObjectReference)
				object = ((ObjectReference) object).getObject();

			LOGGER.info("=====getSubstitutePart.object2: " + object);
			if (object instanceof WTPartSubstituteLink) {
				WTPartSubstituteLink subLink = (WTPartSubstituteLink) object;
				WTPartMaster master = subLink.getSubstitutes();
				list.add(master.getNumber());
			}
		}
		LOGGER.info("=====getSubstitutePart.list: " + list);
		return list;
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

	/**
	 * 设置产生对象的生命周期状态
	 * 
	 * @param pbo
	 * @param state
	 * @throws WTException
	 */
	public void setChangeableAfterStates(WTObject pbo, String state) throws WTException {
		if (!(pbo instanceof WTChangeActivity2))
			return;

		Collection<Changeable2> afters = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
		LOGGER.info("=====setChangeableAfterStates.afters: " + afters.size());
		for (Changeable2 after : afters) {
			LOGGER.info("=====setChangeableAfterStates.after: " + after);
			if (!(after instanceof LifeCycleManaged))
				continue;

			PICoreHelper.service.setLifeCycleState((LifeCycleManaged) after, state);
		}
	}

	/**
	 * 添加有效基线数据----归档流程-(首版)
	 * 
	 * @param pbo
	 * @param self
	 * @throws WTException
	 * @throws IOException
	 */
	public void insertGDEffecitveBaseline(WTObject pbo, ObjectReference self)
			throws WTException, IOException, ParseException, DocumentException {
		Set<WTPart> result = new HashSet<>();// 成品集合

		Collection<Changeable2> collection = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
		LOGGER.info("=====insertGDEffecitveBaseline.collection: " + collection);
		for (Changeable2 changeable2 : collection) {
			LOGGER.info("=====insertGDEffecitveBaseline.changeable2: " + changeable2);
			if (changeable2 instanceof WTPart) {
				WTPart part = (WTPart) changeable2;
				String number = part.getNumber();
				LOGGER.info("=====insertGDEffecitveBaseline.number: " + number);
				// 判断是否成品
				if (number.startsWith("A") || number.startsWith("B")) {
					result.add(part);
				}
			}
		}

		WfProcess process = (WfProcess) self.getObject();
		List<EffectiveBaselineBean> listBean = MversionControlHelper.buildAllEffectiveParts(process, result);
		LOGGER.info("=====insertGDEffecitveBaseline.listBean: " + listBean);
		if (!listBean.isEmpty()) {
			EffecitveBaselineUtil.insertEffectiveBaselineBean(listBean);
		}
	}

	/**
	 * 角色校验、添加角色成员 原GenericPartWF提交逻辑
	 * 
	 * @param pbo
	 * @param self
	 * @param processName
	 * @param username
	 * @param containerNames
	 * @throws WTException
	 */
	public void checkSoftTypeSetRole(WTObject pbo, ObjectReference self, String processName, String username,
			String containerNames) throws WTException {
		PartWorkflowUtil partWorkflowUtil = new PartWorkflowUtil();
		// pbo为ECA，获取产生对象的随机一个部件作为pbo
		LOGGER.info("=====checkSoftTypeSetRole.pbo: " + pbo);
		// if (pbo instanceof WTChangeActivity2) {
		// Collection<Changeable2> collection =
		// ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
		// LOGGER.info("=====checkSoftTypeSetRole.collection: " + collection);
		// for (Changeable2 changeable2 : collection) {
		// LOGGER.info("=====checkSoftTypeSetRole.changeable2: " + changeable2);
		// if (changeable2 instanceof WTPart) {
		// pbo = (WTPart) changeable2;
		// break;
		// }
		// }
		// }
		PartWorkflowUtil partUtil = new PartWorkflowUtil();
		partUtil.checkSoftTypeSetRole(pbo, self, processName, username, containerNames);

		// if (pbo != null && pbo instanceof WTPart) {
		// WTPart part = (WTPart) pbo;
		// LOGGER.info("=====checkSoftTypeSetRole.part: " + part.getNumber());
		// if (part.getNumber().startsWith("X")) {
		// Object valueObject =
		// PIAttributeHelper.service.getValue(part.getContainer(), "ssgs");
		// String company = valueObject == null ? "" : (String) valueObject;
		// LOGGER.info("=====checkSoftTypeSetRole.company: " + company);
		// if ("APPO".equals(company)) {
		// String nodeHierarchy = "";// 获取分类全路径
		// // 获取分类内部值
		// String value = "";
		// value = (String) PIAttributeHelper.service.getValue(part,
		// "Classification");
		// nodeHierarchy =
		// PIClassificationHelper.service.getNodeHierarchy(value);
		// LOGGER.info("=====checkSoftTypeSetRole.nodeHierarchy: " +
		// nodeHierarchy);
		// if (nodeHierarchy.contains("appo_rj01") ||
		// nodeHierarchy.contains("appo_rj02") ||
		// nodeHierarchy.contains("appo_rj03") ||
		// nodeHierarchy.contains("appo_rj04") ||
		// nodeHierarchy.contains("appo_rj06")) {
		// // 归档流程
		// if (processName != null && processName.contains("GenericPartWF")) {
		// partWorkflowUtil.CheckTeam(pbo, "manufacturing_manager", "Receiver");
		// partWorkflowUtil.CheckTeam(pbo, "hardware_development", "Receiver");
		// // 判断是否是光机产品库，且是否添加程国平，若无添加程国平00409
		// if (containerNames.contains(part.getContainerName())) {
		// if (partWorkflowUtil.CheckProcessTeamUser(pbo, username, "Receiver"))
		// {
		// } else {
		// // 添加
		// partWorkflowUtil.AddTeamRole(pbo, "Receiver", username);
		// }
		// }
		// }
		// // 发布流程
		// else if (processName != null &&
		// processName.contains("APPO_ReleasedPartWF")) {
		// partWorkflowUtil.CheckTeam(pbo, "project_manager", "Receiver");
		// // 判断是否是光机产品库，且是否添加程国平，若无添加程国平000409
		// if (containerNames.contains(part.getContainerName())) {
		// if (partWorkflowUtil.CheckProcessTeamUser(pbo, username, "Receiver"))
		// {
		// } else {
		// // 添加
		// partWorkflowUtil.AddTeamRole(pbo, "Receiver", username);
		// }
		// }
		// }
		// } else if (nodeHierarchy.contains("appo_rj05") ||
		// nodeHierarchy.contains("appo_rj07")) {
		// if (processName != null && processName.contains("GenericPartWF")) {
		// partWorkflowUtil.CheckTeam(pbo, "manufacturing_representative",
		// "Receiver");
		// } else if (processName != null &&
		// processName.contains("APPO_ReleasedPartWF")) {
		// partWorkflowUtil.CheckTeam(pbo, "project_manager", "Receiver");
		// }
		// }
		// }
		// }
		// }
	}

	/**
	 * 发布多条数据-CIS
	 *
	 * @param pbo
	 * @param self
	 * @return
	 * @throws SQLException
	 * @throws WTException
	 */
	public String publishAllData(WTObject pbo, ObjectReference self) throws SQLException, WTException {
		// 原GenericPartWF流程返回值永远为空，也就是流程不管CIS有没有写到数据库
		StringBuffer buffer = new StringBuffer();
		Connection connection = null;

		if (pbo instanceof WTChangeOrder2){
			WTChangeOrder2 ecn=(WTChangeOrder2)pbo;
			QueryResult qr = ChangeHelper2.service.getChangeActivities(ecn);
			while (qr.hasMoreElements()) {
				Object object = qr.nextElement();
				if (object != null) {
					if (object instanceof WTChangeActivity2) {
						WTChangeActivity2 eca = (WTChangeActivity2) object;
						Collection<Changeable2> collection = ModifyUtils.getChangeablesAfter(eca);
						LOGGER.info("=====publishAllData.collection: " + collection);
						if (!collection.isEmpty()) {
							try {
								String name = CISBusinessRuleXML.getInstance().getDataBaseName();
								LOGGER.debug("=====publishAllData.name" + name);
								if (name.equals(CISConstant.ORACLE))
									connection = OracleUtil.getConnection();
								else if (name.equals(CISConstant.SQLSERVER))
									connection = SQLServerUtil.getConnection();

								for (Changeable2 changeable2 : collection) {
									LOGGER.info("=====publishAllData.changeable2: " + changeable2);
									if (changeable2 instanceof WTPart) {
										WTPart part = (WTPart) changeable2;
										// if (WorkflowUtil.checkLibrary(part))
										// buffer.append(WorkflowUtil.publishData(part,
										// connection));
										if (ext.generic.integration.cis.workflow.WorkflowUtil.checkLibrary(part))
											ext.generic.integration.cis.workflow.WorkflowUtil.publishData(part, connection);
									} else if (changeable2 instanceof WTDocument) {
										WTDocument document = (WTDocument) changeable2;
										// buffer.append(WorkflowUtil.publishData(document,
										// connection));
										ext.generic.integration.cis.workflow.WorkflowUtil.publishData(document, connection);
									}
								}
							} finally {
								if (connection != null) {
									connection.close();
									connection = null;
								}
							}

						}
					}
				}
			}
		}
		return buffer.toString();
	}

	/**
	 * 更新受影响对象Link的路由为「已完成」
	 * 
	 * @param pbo
	 * @param self
	 * @throws WTException
	 */
	public void updateCorrelationObjectLink(WTObject pbo, ObjectReference self) throws WTException {
		if (pbo instanceof WTChangeActivity2) {
			WTChangeActivity2 activity2 = (WTChangeActivity2) pbo;
			Set<CorrelationObjectLink> links = ModifyHelper.service.queryCorrelationObjectLinks(activity2, LINKTYPE_1);
			for (CorrelationObjectLink link : links) {
				ModifyHelper.service.updateCorrelationObjectLink(link, link.getEcaIdentifier(),
						link.getAadDescription(), ROUTING_3);
			}
		}
	}

	/**
	 * 合成错误信息
	 * 
	 * @return
	 */
	public void compoundMessage() throws WTException {
		StringBuilder builder = new StringBuilder();
		if (MESSAGES.size() > 0) {
			builder.append("无法提交流程，存在以下问题：").append("\n");
			int i = 1;
			for (String message : MESSAGES) {
				builder.append(i++).append(". ").append(message).append("\n");
			}
		}

		if (builder.length() > 0) {
			throw new WTException(builder.toString());
		}
	}

	/**
	 * 变更前BOM状态为已发布，变更后BOM添加软件历史版本必须存在已发布状态
	 * 
	 * @param pbo
	 * @throws WTException
	 */
	public void checkSoftwareState(WTObject pbo) throws WTException {
		if (!(pbo instanceof WTChangeActivity2))
			return;
		Collection<Changeable2> befores = ModifyUtils.getChangeablesBefore((WTChangeActivity2) pbo);
		Collection<Changeable2> afters = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
		for (Changeable2 before : befores) {
			if (before instanceof WTPart) {
				WTPart beforePart = (WTPart) before;
				String state = beforePart.getLifeCycleState().toString();
				String beforePartNumber = beforePart.getNumber();
				// 变更前物料是否为发布状态
				if (state.equals(RELEASED)) {
					// 对应的变更后对象软件是否为已发布
					for (Changeable2 after : afters) {
						if (after instanceof WTPart) {
							WTPart afterPart = (WTPart) after;
							String afterPartNumber = afterPart.getNumber();
							if (beforePartNumber.equals(afterPartNumber)) {
								// 获取部件下所有子件信息
								Collection<WTPart> collection = ChangePartQueryUtils
										.getPartMultiwallStructure(afterPart);
								if (collection.isEmpty())
									return;
								// 软件不是发布状态，添加报错信息
								for (WTPart childPart : collection) {
									String childNumber = childPart.getNumber();
									if (childNumber.startsWith("X")) {
										List<String> partlist = new ArrayList<>();
										QueryResult partqr = getParts(childNumber);
										while (partqr.hasMoreElements()) {
											WTPart oldpart = (WTPart) partqr.nextElement();
											String oldState = oldpart.getState().toString();
											System.out.println("version=====" + oldState);
											if (!partlist.contains(oldState)) {
												partlist.add(oldState);
											}

										}
										// 不存在有已发布的版本，
										if (!partlist.contains(RELEASED)) {
											MESSAGES.add("软件" + childNumber + "不存在已发布状态，不能添加到已发布的BOM" + afterPartNumber
													+ "中，请先软件发布后再添加到BOM中！");
										}
									}

								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 事务性ECA为初始状态开启时设置事务性ECA为实施
	 * 
	 * @param pbo
	 * @param targetState
	 * @param orgState
	 * @throws WTException
	 */
	public static void setTransactionTaskState(WTObject pbo, String targetState, String orgState) throws WTException {
		if (pbo == null || !(pbo instanceof WTChangeOrder2) || PIStringUtils.isNull(targetState)) {
			return;
		}
		try {
			WTChangeOrder2 changeOrder2 = (WTChangeOrder2) pbo;
			// 获取ECN中所有ECA对象
			Collection<ChangeActivityIfc> ecaArray = ChangeUtils.getChangeActivities(changeOrder2);
			for (ChangeActivityIfc changeActivityIfc : ecaArray) {
				if (changeActivityIfc instanceof LifeCycleManaged) {
					LifeCycleManaged lifeCycleManaged = (LifeCycleManaged) changeActivityIfc;
					// 获取完整的内部值
					String internalName = ClientTypedUtility.getTypeIdentifier(changeActivityIfc).getTypename();
					LOGGER.info("=====internalName==" + internalName);
					System.out.println("internalName==" + internalName);
					String[] str = internalName.split("\\|");
					if (str.length > 1) {
						internalName = str[1];
					}
					System.out.println("internalName2==" + internalName);
					LOGGER.info("=====internalName2==" + internalName);
					// 任务性内部值
					if (internalName.contains(TYPE_3)) {
						if (PIStringUtils.isNotNull(orgState)) {
							if (lifeCycleManaged.getState().toString().equalsIgnoreCase(orgState)) {
								WorkflowUtil.setLifeCycleState(lifeCycleManaged, targetState);

							}
						} else {
							WorkflowUtil.setLifeCycleState(lifeCycleManaged, targetState);
						}

					}

				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		}
	}

	/**
	 * 检查光峰的BOM结构中不能有小米定制的物料（除“激光产品库”“微投产品库”外）
	 *
	 * @param pbo
	 * @throws WTException
	 */
	public void checkMICustomizeorNot(WTObject pbo) throws WTException {
		if (pbo == null || !(pbo instanceof WTChangeActivity2)) {
			return;
		}
		Collection<Changeable2> changeable2s = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
		for (Changeable2 changeable2 : changeable2s) {
			if (changeable2 instanceof WTPart) {
				WTPart parentPart = (WTPart) changeable2;
				PartWorkflowUtil partUtil = new PartWorkflowUtil();
				partUtil.checkMICustomizeorNot(parentPart);
				// String containerName = parentPart.getContainerName();
				// if (!"激光电视产品库".equals(containerName) &&
				// !"微投产品库".equals(containerName)) {
				//
				// // 查询第一层结构信息
				// WTList wtList = new WTArrayList();
				// wtList.add(parentPart);
				// QueryResult queryResult =
				// ChangePartQueryUtils.getPartsFirstStructure(wtList, new
				// LatestConfigSpec());
				// while (queryResult.hasMoreElements()) {
				// Persistable[] persistables = (Persistable[])
				// queryResult.nextElement();
				// // 子件对象
				// Object object = persistables[1];
				// if (object instanceof WTPart) {
				// WTPart childPart = (WTPart) object;
				// Object valueObject =
				// PIAttributeHelper.service.getValue(childPart,
				// "MI_Customize_orNot");
				// String MICustomizeorNot = valueObject == null ? "" : (String)
				// valueObject;
				// if ("是".equals(MICustomizeorNot)) {
				// MESSAGES.add("BOM编码" + (parentPart.getNumber() + "的子件" +
				// childPart.getNumber() + "是小米定制物料，请删除！"));
				// }
				// }
				// }
				// }
			}
		}

	}

	/**
	 * 检查子类状态（制造视图）
	 *
	 * @author HYJ&NJH
	 * @param part
	 * @param isArchive
	 * @return
	 * @throws WTException
	 */
	public void checkSonHistoryversionMAf(WTObject pbo) throws WTException {
		if (pbo == null || !(pbo instanceof WTChangeActivity2)) {
			return;
		}
		Collection<Changeable2> changeable2s = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
		for (Changeable2 changeable2 : changeable2s) {
			if (changeable2 instanceof WTPart) {
				WTPart part = (WTPart) changeable2;
				String viewname = part.getViewName();
				if (viewname.contains("Manufacturing")) {
					PartWorkflowUtil partUtil = new PartWorkflowUtil();
					partUtil.checkSonHistoryversionMAf(part);
					//
					// String message = "";
					// List<String> partlist = new ArrayList<String>();
					//
					// WTUser previous = (WTUser)
					// SessionHelper.manager.getPrincipal();
					//
					// // 当前用户设置为管理员，用于忽略权限
					// try {
					// WTPrincipal wtadministrator =
					// SessionHelper.manager.getAdministrator();
					// // 取得当前用户
					// SessionContext.setEffectivePrincipal(wtadministrator);
					// WTPrincipalReference wtprincipalreference =
					// (WTPrincipalReference) (new ReferenceFactory())
					// .getReference(previous);
					// AccessControlHelper.manager.addPermission((AdHocControlled)
					// part, wtprincipalreference,
					// AccessPermission.MODIFY_IDENTITY,
					// AdHocAccessKey.WNC_ACCESS_CONTROL);
					// } catch (WTException e1) {
					// // TODO Auto-generated catch block
					// e1.printStackTrace();
					// }
					// // 获取单层子件
					// // WTCollection childrens =
					// PIPartHelper.service.findChildren(part);
					// // 获取所有子件
					// ArrayList<WTPart> childpartList = new
					// ArrayList<WTPart>();
					// try {
					// StartAppoPartArchiveIssueWF.getBomallchildpart(part,
					// childpartList);
					// } catch (Exception e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }
					// System.out.println("childpartList.size()====" +
					// childpartList.size());
					// if (!part.getNumber().startsWith("A") &&
					// !part.getNumber().startsWith("B")) {
					// if (childpartList.size() > 0) {
					// MESSAGES.add("除成品、半成品外不允许搭BOM！");
					// }
					// }
					// for (int i = 0; i < childpartList.size(); i++) {
					// WTPart son = (WTPart) childpartList.get(i);
					// String stateVal = son.getLifeCycleState().toString();
					//
					// if
					// (part.getVersionIdentifier().getValue().startsWith("A"))
					// {
					// System.out.println("part container name====" +
					// part.getContainerName());
					// if (!part.getContainerName().startsWith("微投产品库") &&
					// !part.getContainerName().startsWith("激光电视产品库")) {
					// // 检出子件是否有总成
					// String cls = (String)
					// PIAttributeHelper.service.getValue(son,
					// "Classification");
					// System.out.println("cls ====" + cls);
					// if (cls.startsWith("appo_bcp17") ||
					// cls.startsWith("appo_bcp21") ||
					// cls.startsWith("appo_bcp22")
					// || cls.startsWith("appo_bcp24") ||
					// cls.startsWith("appo_bcp11")
					// || cls.startsWith("appo_bcp25") ||
					// cls.startsWith("appo_bcp19")
					// || cls.startsWith("appo_bcp16")) {
					// MESSAGES.add("制造BOM(Manufacturing)下面不能直接引用设计虚拟件，如“硬件总成”等！");
					// }
					// }
					// }
					// if (stateVal.equals("ARCHIVED") ||
					// stateVal.equals("RELEASED")) {
					// } else {
					// QueryResult partqr = getParts(son.getNumber());
					// while (partqr.hasMoreElements()) {
					// WTPart oldpart = (WTPart) partqr.nextElement();
					// String state = oldpart.getState().toString();
					// System.out.println("version=====" + state);
					// if (!partlist.contains(state)) {
					// partlist.add(state);
					// }
					//
					// } // 存在有已归档或已发布的版本，
					// if (partlist.contains("ARCHIVED") ||
					// partlist.contains("RELEASED")) {
					// } else {
					// message = message + "子件:" + son.getNumber() +
					// "未存在已归档或已发布的版本,不符合归档逻辑，无法提交归档流程!" + "\n";
					// }
					// }
					// // 检出全局替代
					// WTCollection wtcol =
					// WTPartHelper.service.getAlternateLinks((WTPartMaster)
					// son.getMaster());
					// Iterator ite = wtcol.iterator();
					// while (ite.hasNext()) {
					// Object obj = ite.next();
					//
					// if (obj != null && obj instanceof ObjectReference) {
					// ObjectReference objRef = (ObjectReference) obj;
					//
					// Object tempObj = objRef.getObject();
					//
					// if (tempObj != null && tempObj instanceof
					// WTPartAlternateLink) {
					// WTPartAlternateLink alternateLink = (WTPartAlternateLink)
					// tempObj;
					//
					// WTPartMaster alternatePartMaster =
					// alternateLink.getAlternates();
					// System.out.println("alternatePartMaster number=" +
					// alternatePartMaster.getNumber());
					// WTPart alPart =
					// PartUtil.getLastestWTPartByNumber(alternatePartMaster.getNumber());
					// String alpartstate = alPart.getState().toString();
					// if (!alpartstate.equalsIgnoreCase("RELEASED") &&
					// !alpartstate.equalsIgnoreCase("ARCHIVED")) {
					// QueryResult partqr = getParts(alPart.getNumber());
					// List<String> partlistA = new ArrayList<String>();
					// while (partqr.hasMoreElements()) {
					// WTPart oldpart = (WTPart) partqr.nextElement();
					// String statea = oldpart.getState().toString();
					// System.out.println("version=====" + statea);
					// if (!partlistA.contains(statea)) {
					// partlistA.add(statea);
					// }
					//
					// } // 存在有已归档或已发布的版本，
					// if (partlistA.contains("ARCHIVED") ||
					// partlistA.contains("RELEASED")) {
					// } else {
					// message = message + "全局替代料:" + alPart.getNumber() +
					// "未存在已归档或已发布的版本,不符合归档逻辑，无法提交归档流程!";
					// }
					// }
					// }
					// }
					// }
					// }
					// // 检查特定替代料状态
					// QueryResult qr =
					// WTPartHelper.service.getUsesWTPartMasters(part);
					// System.out.println("qr size=========" + qr.size());
					// ArrayList<String> subpartList = new ArrayList<String>();
					// while (qr.hasMoreElements()) {
					// WTPartUsageLink link = (WTPartUsageLink)
					// qr.nextElement();
					// subpartList = getSubstitutePart(link);
					// System.out.println("subpartList=========" +
					// subpartList.size());
					// for (int j = 0; j < subpartList.size(); j++) {
					// String number = subpartList.get(j);
					// WTPart subpart =
					// PartUtil.getLastestWTPartByNumber(number);
					// String state = subpart.getState().toString();
					// System.out.println(" number =========" + number);
					// System.out.println(" subpart state=========" + state);
					// if (!state.equalsIgnoreCase("RELEASED") &&
					// !state.equalsIgnoreCase("ARCHIVED")) {
					//
					// QueryResult partqr = getParts(subpart.getNumber());
					// List<String> partlistS = new ArrayList<String>();
					// while (partqr.hasMoreElements()) {
					// WTPart oldpart = (WTPart) partqr.nextElement();
					// String stateold = oldpart.getState().toString();
					// System.out.println("version=====" + stateold);
					// if (!partlistS.contains(stateold)) {
					// partlistS.add(stateold);
					// }
					//
					// } // 存在有已归档或已发布的版本，
					// if (partlistS.contains("ARCHIVED") ||
					// partlistS.contains("RELEASED")) {
					// } else {
					// message = message + "特定替代料:" + subpart.getNumber() +
					// "未存在已归档或已发布的版本,不符合归档逻辑，无法提交归档流程!";
					// }
					//
					// }
					// }
					// }
					//
					// SessionContext.setEffectivePrincipal(previous);
					// if (message.length() > 0) {
					// MESSAGES.add(message);
					// }
				}
			}
		}
	}

	/**
	 * D视图添加校验：分类为组件的下面不允许挂总成
	 *
	 * @param part
	 * @throws WTException
	 */
	public void checkSonHistoryversionDesign(WTObject pbo) throws WTException {
		if (pbo == null || !(pbo instanceof WTChangeActivity2)) {
			return;
		}
		Collection<Changeable2> changeable2s = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
		for (Changeable2 changeable2 : changeable2s) {
			if (changeable2 instanceof WTPart) {
				WTPart part = (WTPart) changeable2;
				PartWorkflowUtil partUtil = new PartWorkflowUtil();
				partUtil.checkSonHistoryversionDesign(part);

				// View view = (View) part.getView().getObject();
				// StringBuffer message = new StringBuffer();
				// Boolean flag="Design".equals(view.getName());
				// System.out.println("view.getName()=="+view.getName()+"==flag=="+flag);
				// if (view != null && "Design".equals(view.getName())) {
				// WTUser previous = (WTUser)
				// SessionHelper.manager.getPrincipal();
				// try {
				// WTPrincipal wtadministrator =
				// SessionHelper.manager.getAdministrator();
				// // 取得当前用户
				// SessionContext.setEffectivePrincipal(wtadministrator);
				//
				// String clsParent = (String)
				// PIAttributeHelper.service.getValue(part, "Classification");
				// System.out.println("clsParent ====" + clsParent);
				// if (clsParent.startsWith("appo_bcp01") ||
				// clsParent.startsWith("appo_bcp10")
				// || clsParent.startsWith("appo_bcp13") ||
				// clsParent.startsWith("appo_bcp05")
				// || clsParent.startsWith("appo_bcp20") ||
				// clsParent.startsWith("appo_bcp14")
				// || clsParent.startsWith("appo_bcp06") ||
				// clsParent.startsWith("appo_bcp04")) {
				// // 获取所有子件
				// ArrayList<WTPart> childpartList = new ArrayList<WTPart>();
				// StartAppoPartArchiveIssueWF.getBomallchildpart(part,
				// childpartList);
				//
				// for (int i = 0; i < childpartList.size(); i++) {
				// WTPart son = (WTPart) childpartList.get(i);
				//
				// System.out.println("part container name====" +
				// part.getContainerName());
				// if (!part.getContainerName().startsWith("微投产品库")
				// && !part.getContainerName().startsWith("激光电视产品库")) {
				// // 检出子件是否有总成
				// String cls = (String) PIAttributeHelper.service.getValue(son,
				// "Classification");
				// System.out.println("cls ====" + cls);
				// if (cls.startsWith("appo_bcp17") ||
				// cls.startsWith("appo_bcp21")
				// || cls.startsWith("appo_bcp22") ||
				// cls.startsWith("appo_bcp24")
				// || cls.startsWith("appo_bcp11") ||
				// cls.startsWith("appo_bcp25")
				// || cls.startsWith("appo_bcp19") ||
				// cls.startsWith("appo_bcp16")) {
				// if (message.toString() != null && message.toString().length()
				// > 0) {
				// message.append("\n");
				// }
				// message.append("设计视图组件【" + part.getNumber() + "】，不能引用总成的子件【"
				// + son.getNumber() + "】.");
				// }
				// }
				// }
				// }
				//
				// } catch (Exception e) {
				// e.printStackTrace();
				// } finally {
				// SessionContext.setEffectivePrincipal(previous);
				// }
				//
				// }
				//
				// if (message.toString() != null && message.toString().length()
				// > 0) {
				// MESSAGES.add(message.toString());
				// }
			}
		}

	}

	/**
	 * 检查光峰的BOM归档发布时，结构中不能【新增】优选等级为“禁选”的物料
	 * 
	 * @param pbo
	 * @throws WTException
	 */
	public void checkPartYxdj(WTObject pbo) throws WTException {

		if (pbo == null || !(pbo instanceof WTChangeActivity2)) {
			return;
		}
		Collection<Changeable2> changeable2s = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
		for (Changeable2 changeable2 : changeable2s) {
			if (changeable2 instanceof WTPart) {
				WTPart parentPart = (WTPart) changeable2;
				PartWorkflowUtil partUtil = new PartWorkflowUtil();
				partUtil.checkPartYxdj(parentPart);
			}
		}
	}

	// 获取所有版本物料
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

	// 初始化流程角色：相同对象的ECA流程节点人从先前的ECA中获取
	public void initTeamRole(WTObject pbo) {
		if (pbo instanceof WTChangeActivity2) {
			boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
			try {
				WTChangeActivity2 eca = (WTChangeActivity2) pbo;
				System.out.println("eca number==" + eca.getNumber());
				// 通过eca获取ecn
				WTChangeOrder2 ecn = null;
				QueryResult ecaqr = null;
				ecaqr = ChangeHelper2.service.getChangeOrder(eca);
				while (ecaqr.hasMoreElements()) {
					Object object = ecaqr.nextElement();
					if (object instanceof WTChangeOrder2) {
						ecn = (WTChangeOrder2) object;
						System.out.println("ecn number==" + ecn.getNumber());
						break;
					}
				}

				if (ecn != null) {
					System.out.println("ecn number222==" + ecn.getNumber());
					WTChangeActivity2 beforeEca = null;
					// 获取其他ECA
					QueryResult qr = ChangeHelper2.service.getChangeActivities(ecn);
					while (qr.hasMoreElements()) {
						Object object = qr.nextElement();
						if (object != null) {
							if (object instanceof WTChangeActivity2) {
								WTChangeActivity2 wtChangeActivity2 = (WTChangeActivity2) object;
								if (wtChangeActivity2.getName().equals(eca.getName())
										&& !eca.getNumber().equals(wtChangeActivity2.getNumber())) {
									beforeEca = wtChangeActivity2;
									System.out.println("beforeEca number==" + beforeEca.getNumber());
									break;
								}
							}
						}
					}

					if (beforeEca != null) {
						System.out.println("beforeEca number222==" + beforeEca.getNumber());
						Team processTeam = WorkflowUtil.getTeam(beforeEca);
						if (processTeam != null) {
							if (processTeam != null) {
								List<String> rolenames = new ArrayList<>();
								rolenames.add("Assignee");// 数据更改
								rolenames.add("Corrector");// 校对者
								rolenames.add("Normalizer");// 标准化审查者
								rolenames.add("Assessor");// 审核者
								rolenames.add("Signer");// 会签者
								rolenames.add("Approver");// 批准者
								rolenames.add("Receiver");// 接收者
								// 给当前ECA角色设置以前ECA的角色
								addTeamRoleByBefore(eca, processTeam, rolenames);
							}
						}
					}
				}
			} catch (WTException e) {
				e.printStackTrace();
			} finally {
				SessionServerHelper.manager.setAccessEnforced(enforce);
			}
		}
	}

	// 给当前ECA角色设置以前ECA的角色
	public static void addTeamRoleByBefore(WTChangeActivity2 eca, Team processTeam, List<String> rolenames) {
		if (rolenames != null && rolenames.size() > 0) {
			try {
				PartWorkflowUtil partwork = new PartWorkflowUtil();
				for (String rolename : rolenames) {
					Role role = Role.toRole(rolename);
					System.out.println("rolename====" + rolename);
					Enumeration enumPrin = processTeam.getPrincipalTarget(role);
					while (enumPrin.hasMoreElements()) {
						WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
						WTPrincipal principal = tempPrinRef.getPrincipal();
						String principalname = principal.getName();
						System.out.println("principalname====" + principalname);
						partwork.AddTeamRole(eca, rolename, principalname);
					}
				}
			} catch (WTException e) {
				e.printStackTrace();
			}
		}
	}

}
