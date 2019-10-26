package ext.appo.part.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.workflow.NmWorkflowHelper;

import ext.com.workflow.WorkflowUtil;
import ext.generic.partpromotion.util.PartPromotionConstant;
import ext.generic.partpromotion.util.PartPromotionXMLConfigUtil;
import ext.lang.PIStringUtils;
import ext.pi.PIException;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIClassificationHelper;
import wt.access.AccessControlHelper;
import wt.access.AccessPermission;
import wt.admin.AdminDomainRef;
import wt.enterprise.RevisionControlled;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.WTReference;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.session.SessionHelper;
import wt.type.TypedUtility;
import wt.util.WTException;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfState;

public class StandardPartAttrChangeFilter extends DefaultSimpleValidationFilter {
	private static String CLASSNAME = StandardPartAttrChangeFilter.class.getName();
	private static final Logger LOGGER = LogR.getLogger(CLASSNAME);
	public static final String PART_TYPE = "WCTYPE|wt.part.WTPart";

	@Override
	public UIValidationStatus preValidateAction(UIValidationKey uivalidationkey,
			UIValidationCriteria uivalidationcriteria) {
		LOGGER.debug(" ===ManualStartProcessValidator===");

		System.out.println("into StandardPartAttrChangeFilter");
		// UIValidationStatus uivalidationstatus = UIValidationStatus.DISABLED;
		UIValidationStatus uivalidationstatus = UIValidationStatus.HIDDEN;
		WTReference wtreference = uivalidationcriteria.getContextObject();
		if (wtreference != null) {
			Persistable persistable = wtreference.getObject();
			try {
				if (persistable != null && persistable instanceof WTPart) {
					WTPart wtpart = (WTPart) persistable;
					// 是否显示手动启动流程入口
					if (!WorkflowUtil.isObjectCheckedOut(wtpart)) {
						boolean result = showManualStartProcess(wtpart, uivalidationcriteria, uivalidationkey);
						if (result) {
							uivalidationstatus = UIValidationStatus.ENABLED;
						} else {
							// uivalidationstatus = UIValidationStatus.DISABLED;
							uivalidationstatus = UIValidationStatus.HIDDEN;
						}
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return uivalidationstatus;
	}

	/**
	 * 判断是否显示入口
	 * 
	 * @param part
	 *            pbo
	 * @return boolean
	 * @throws WTException
	 *             异常
	 */
	private static boolean showManualStartProcess(WTPart part, UIValidationCriteria uivalidationcriteria,
			UIValidationKey uivalidationkey) throws WTException {

		boolean isManualStart = false;

		// 判断是否是标准件
		Boolean isStandartpart = checkIsStandardparse(part);
		if (!isStandartpart) {
			return false;
		}

		PartPromotionXMLConfigUtil partPromotionXMLConfigUtil = new PartPromotionXMLConfigUtil();
		partPromotionXMLConfigUtil.partPromotionParser();
		Map partPromotionRuleMap = partPromotionXMLConfigUtil.getPartPromotionRuleMap();
		if (partPromotionRuleMap == null || partPromotionRuleMap.size() == 0) {
			LOGGER.debug(" partPromotionRuleMap : = " + partPromotionRuleMap);
			return false;
		}

		// 判断是否为最新版本
		boolean getlatest = (Boolean) partPromotionRuleMap.get(PartPromotionConstant.NODE_NAME_LATESTED);
		LOGGER.debug(" getlatest : = " + getlatest);
		if (getlatest) {
			if (!isLatestObject(part)) {
				return false;
			}
		}

		// 判断是否满足xml配置的生命周期状态
		// Vector valitatorStates = (Vector)
		// partPromotionRuleMap.get(PartPromotionConstant.NODE_NAME_VALIDATE_PART_STATE);
		// LOGGER.debug( " valitatorStates : = " + valitatorStates);
		String partState = part.getLifeCycleState().toString();
		if (!partState.equals("ARCHIVED") && !partState.equals("RELEASED")) {
			return false;
		}
		System.out.println("part number==" + part.getNumber());
		if (isRunningWorkflow(part)) {
			return false;
		}

		boolean isAdmin = uivalidationcriteria.isSiteAdmin() || uivalidationcriteria.isOrgAdmin();
		// 组织、站点管理员以及指定组成员有权限
		if (isAdmin) {
			return true;
		}

		// 获取当前用户，判断是否有物料创建权限
		WTUser curUser = (WTUser) SessionHelper.manager.getPrincipal();
		WTPrincipalReference curUserRef = WTPrincipalReference.newWTPrincipalReference(curUser);

		AdminDomainRef adminDomainRef = null;
		adminDomainRef = part.getDomainRef();
		Boolean operability = isOperability(curUser, PART_TYPE, adminDomainRef, State.toState("INWORK"),
				AccessPermission.CREATE);

		if (operability) {
			return true;
		}

		return isManualStart;
	}

	/**
	 * 判断部件是不是最新版本
	 * 
	 * @param oldObj
	 *            当前对象
	 * @return boolean
	 */
	private static boolean isLatestObject(WTObject oldObj) {
		boolean isLstest = true; // 是最新
		RevisionControlled iterated = null;
		if (oldObj instanceof RevisionControlled) {
			iterated = (RevisionControlled) oldObj;
			if (!iterated.isLatestIteration()) {
				isLstest = false;
			}
		}
		return isLstest;
	}

	/***
	 * 检查受影响对象是是否为标准件
	 * 
	 * @param changeOrder2
	 * @param affectedArray
	 * @throws WTException
	 */
	public static Boolean checkIsStandardparse(WTPart part) throws WTException {

		Boolean isStandartpart = false;

		StandardPartsRevise re = new StandardPartsRevise();
		List<Map> list = new ArrayList<Map>();
		try {
			list = re.getExcelData();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (WTException e1) {
			e1.printStackTrace();
		}

		isStandartpart = isStandartpart(list, part);

		return isStandartpart;
	}

	public static Boolean isStandartpart(List<Map> datalist, WTPart part) {
		Boolean isStandardpart = false;
		// 获取分类内部值
		String value = "";
		String nodeHierarchy = "";// 获取分类全路径
		try {
			value = (String) PIAttributeHelper.service.getValue(part, "Classification");
			System.out.println("cls value ====" + value);
			nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(value);
			System.out.println("nodeHierarchy value ===" + nodeHierarchy);
		} catch (PIException e2) {
			e2.printStackTrace();
		}
		if (datalist != null && datalist.size() > 0) {
			for (Map map2 : datalist) {
				Map<Persistable, Map> map = new HashMap<>();
				String type = "";
				type = (String) map2.get("type");
				System.out.println("type===" + type + "-----------" + nodeHierarchy);
				if (type.length() > 0 && nodeHierarchy.contains(type)) {
					isStandardpart = true;
					break;
				}
			}
		}
		return isStandardpart;
	}

	/***
	 * 检查用户在指定库中对某种类型的某种状态是否具有相应权限
	 * 
	 * @param principal
	 *            用户
	 * @param type
	 *            类型 (列如:WCTYPE|wt.change2.WTChangeOrder2)
	 * @param adminDomainRef
	 *            静态权限域
	 * @param state
	 *            指定状态
	 * @param accessPermission
	 *            相应权限
	 * @return
	 * @throws WTException
	 */
	public static Boolean isOperability(WTPrincipal principal, String type, AdminDomainRef adminDomainRef, State state,
			AccessPermission accessPermission) throws WTException {
		if (PIStringUtils.isNull(type) || adminDomainRef == null || accessPermission == null) {
			return false;
		}

		String displayTypeIdentifier = displayTypeIdentifier(type);
		if (PIStringUtils.isNotNull(displayTypeIdentifier)) {
			return AccessControlHelper.manager.hasAccess(principal, displayTypeIdentifier, adminDomainRef, state,
					AccessPermission.CREATE);
		}

		return false;
	}

	/***
	 * 获取类型定义
	 * 
	 * @param paramString
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings("deprecation")
	public static String displayTypeIdentifier(String paramString) throws WTException {
		if (paramString == null) {
			return null;
		}

		String str1 = TypedUtility.getExternalTypeIdentifier(paramString);
		if (str1 == null) {
			String str2 = TypedUtility.getPersistedType(paramString);
			if (str2 != null) {
				str1 = TypedUtility.getExternalTypeIdentifier(str2);
			}
		}
		return str1 == null ? paramString : str1;
	}

	/**
	 * 判断pbo是否在其他流程中运行
	 * 
	 * @param pbo
	 * @return
	 * @throws WTException
	 */
	private static boolean isRunningWorkflow(WTPart pbo) throws WTException {
		boolean processok = false;
		NmOid nmOid = NmOid.newNmOid(PersistenceHelper.getObjectIdentifier(pbo));
		QueryResult qr = NmWorkflowHelper.service.getRoutingHistoryData(nmOid);

		while (qr.hasMoreElements()) {
			WfProcess process = (WfProcess) qr.nextElement();
			String templateName = process.getTemplate().getName();
			System.out.println("templateName==" + templateName + " process.getState()=" + process.getState());
			// 只判断是否存在关联的流程，不再判断流程名称
			if (process.getState().equals(WfState.OPEN_RUNNING)) {
				processok = true;
				break;
			}
		}
		return processok;
	}

}
