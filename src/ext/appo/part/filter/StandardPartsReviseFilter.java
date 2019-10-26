package ext.appo.part.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;

import ext.lang.PIStringUtils;
import ext.pi.PIException;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIClassificationHelper;
import wt.access.AccessControlHelper;
import wt.access.AccessPermission;
import wt.admin.AdminDomainRef;
import wt.change2.ChangeHelper2;
import wt.change2.Changeable2;
import wt.change2.WTChangeActivity2;
import wt.doc.WTDocument;
import wt.enterprise.Master;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.folder.SubFolder;
import wt.identity.IdentityFactory;
import wt.inf.container.WTContainerHelper;
import wt.lifecycle.State;
import wt.org.WTPrincipal;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.query.QueryException;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.type.TypedUtility;
import wt.util.WTException;
import wt.vc.OneOffVersioned;
import wt.vc.VersionControlHelper;
import wt.workflow.engine.WfProcess;

public class StandardPartsReviseFilter extends DefaultSimpleValidationFilter {
	private static Logger logger = Logger.getLogger(StandardPartsReviseFilter.class.getName());

	@Override
	public UIValidationStatus preValidateAction(UIValidationKey key, UIValidationCriteria validationCriteria) {
		Persistable persistable = validationCriteria.getContextObject().getObject();
		WTPrincipal userPrincipal = null;
		try {
			userPrincipal = SessionHelper.manager.getPrincipal();
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// logger.debug("now user is=="+userPrincipal.getName());
		if (isSiteAdmin(userPrincipal)) {
			return UIValidationStatus.ENABLED;
		}

		if (persistable instanceof WTPart) {
			StandardPartsRevise re = new StandardPartsRevise();
			List<Map> list = new ArrayList<Map>();
			try {
				list = re.getExcelData();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (WTException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			QueryResult parentResult = null;
			List<WTPart> partList = null;
			WTPart part = (WTPart) persistable;
			Boolean isbyuse = false;
			try {
				parentResult = WTPartHelper.service.getUsedByWTParts((WTPartMaster) part.getMaster());
				// 剔除非最新版本的BOM,剔除掉“A版本”“正在工作”的BOM
				// mao
				partList = this.deleteAAndInworkData(parentResult);

			} catch (WTException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if (partList == null || partList.size() == 0) {
				isbyuse = true;
			}
			if (part.getNumber().startsWith("A") || part.getNumber().startsWith("B")) {

				String cls = "";
				try {
					cls = (String) PIAttributeHelper.service.getValue(part, "Classification");
				} catch (PIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				AdminDomainRef adminDomainRef = null;
				String doctype = "";
				try {

					doctype = getObjectType(part);
					String type = "WCTYPE|" + doctype;
					SubFolder folder = (SubFolder) ((WTPart) persistable).getFolderingInfo().getParentFolder()
							.getObject();
					adminDomainRef = folder.getDomainRef();
					Boolean operability = isOperability(userPrincipal, type, adminDomainRef, null,
							AccessPermission.CREATE);
					System.out.println("cls===" + cls);
					if (cls.contains("appo_bcp01") && operability && isbyuse
							&& part.getState().toString().endsWith("ARCHIVED")) { // PCBA归档状态，且有创建的权限,没有被bom使用过的
						return UIValidationStatus.ENABLED;

					} else {
						return UIValidationStatus.DISABLED;
					}

				} catch (WTException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (part.getState().toString().endsWith("ARCHIVED")) {
				if (part.getNumber().startsWith("X")) {
					return UIValidationStatus.ENABLED;
				}
			}

			if (part.getState().toString().endsWith("ARCHIVED")) {
				Boolean isStandartpart = isStandartpart(list, part);
				System.out.println("isStandartpart==========" + isStandartpart);

				try {
					QueryResult ecaResult = new QueryResult();
					String ecaState = "";
					int count = 0;
					ecaResult = ChangeHelper2.service.getAffectingChangeActivities((Changeable2) persistable);
					// logger.debug("ecaResult size==="+ecaResult.size());
					while (ecaResult.hasMoreElements()) {
						WTChangeActivity2 eca = (WTChangeActivity2) ecaResult.nextElement();
						ecaState = eca.getState().toString();
						if (ecaState.endsWith("OPEN") || ecaState.endsWith("UNDERREVIEW")
								|| ecaState.endsWith("IMPLEMENTATION")) {// 开启，正在审阅，实施
							count++;
						}
					}
					System.out.println("count==========" + count);
					AdminDomainRef adminDomainRef = null;

					String doctype = getObjectType(part);
					String type = "WCTYPE|" + doctype;
					SubFolder folder = (SubFolder) ((WTPart) persistable).getFolderingInfo().getParentFolder()
							.getObject();
					adminDomainRef = folder.getDomainRef();
					Boolean operability = isOperability(userPrincipal, type, adminDomainRef, null,
							AccessPermission.CREATE);
					System.out.println("operability=====" + operability);
					// 没有正在进行的ECA，不是标准件，没有被bom使用到，有创建权限的才能修订
					if (count == 0 && !isStandartpart && isbyuse && operability) {
						return UIValidationStatus.ENABLED;
					} else if (count > 0 || isStandartpart || !isbyuse || !operability) {
						return UIValidationStatus.DISABLED;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				return UIValidationStatus.DISABLED;
			}
		}
		System.out.println("persistable========" + persistable);
		if (persistable instanceof WTDocument) {
			WTDocument document = (WTDocument) persistable;
			String state = document.getState().toString();
			System.out.println("doc state===" + state);
			if (state.endsWith("RELEASED") || state.endsWith("ARCHIVED")) {
				AdminDomainRef adminDomainRef = null;
				SubFolder folder = (SubFolder) ((WTDocument) persistable).getFolderingInfo().getParentFolder()
						.getObject();
				adminDomainRef = folder.getDomainRef();
				try {
					String doctype = getObjectType(document);
					String type = "WCTYPE|" + doctype;
					System.out.println("type=====" + type);
					// 是否有创建的权限
					Boolean operability = isOperability(userPrincipal, type, adminDomainRef, null,
							AccessPermission.CREATE);
					System.out.println("operability=====" + operability);
					// 参考文档可以修订
					if (doctype.contains("com.ptc.ReferenceDocument") && operability) {
						return UIValidationStatus.ENABLED;
					}
				} catch (WTException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			return UIValidationStatus.HIDDEN;
		}

		return UIValidationStatus.HIDDEN;
	}

	/**
	 * 过滤掉非最新版本的母件，过滤掉A版本正在工作的母件
	 * 
	 * @param qr
	 * @return
	 */
	public List<WTPart> deleteAAndInworkData(QueryResult qr) {
		List<WTPart> parentParts = new ArrayList<WTPart>();

		while (qr != null && qr.hasMoreElements()) {
			Object obj = qr.nextElement();
			if (obj instanceof WTPart) {
				WTPart parentPart = (WTPart) obj;
				WTPart newParentPart = (WTPart) getLatestVersionByMaster(parentPart.getMaster());
				// 过滤不是最新版本的物料
				if (!getOidByObject(parentPart).equals(getOidByObject(newParentPart))) {
					continue;
				}
				// 过滤掉为A版本正在工作的物料
				String version = parentPart.getVersionInfo().getIdentifier().getValue();
				String state = parentPart.getState().toString();

				if ("INWORK".equals(state) && "A".equals(version)) {
					continue;
				}
				parentParts.add(parentPart);
			}
		}

		return parentParts;
	}

	public static String getObjectType(Object object) throws WTException {
		String type = "";
		boolean flag = true;
		try {
			flag = SessionServerHelper.manager.isAccessEnforced();
			SessionServerHelper.manager.setAccessEnforced(false);

			if (object != null) {
				TypeIdentifier ti = TypeIdentifierUtility.getTypeIdentifier(object);
				type = ti.getTypename();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
		return type;
	}

	public static Boolean isStandartpart(List<Map> datalist, WTPart part) {
		Boolean isStandardpart = false;
		// 获取分类内部值
		String value = "";
		String nodeHierarchy = "";// 获取分类全路径
		try {
			value = (String) PIAttributeHelper.service.getValue(part, "Classification");
			System.out.println("cls value ===" + value);
			nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(value);
			System.out.println("nodeHierarchy value ===" + nodeHierarchy);
		} catch (PIException e2) {
			// TODO Auto-generated catch block
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
	public Boolean isOperability(WTPrincipal principal, String type, AdminDomainRef adminDomainRef, State state,
			AccessPermission accessPermission) throws WTException {
		if (PIStringUtils.isNull(type) || adminDomainRef == null || accessPermission == null) {
			return false;
		}

		String displayTypeIdentifier = displayTypeIdentifier(type);
		if (logger.isDebugEnabled()) {
			logger.debug("adminDomainRef : " + IdentityFactory.getDisplayIdentity(adminDomainRef.getObject()));
			logger.debug("displayTypeIdentifier : " + displayTypeIdentifier);
		}
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
	public String displayTypeIdentifier(String paramString) throws WTException {
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

	public static boolean isSiteAdmin(WTPrincipal wtPrincipal) {
		try {
			return WTContainerHelper.service.isAdministrator(WTContainerHelper.service.getExchangeRef(), wtPrincipal);
		} catch (WTException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 获取最新大版本的最新小版本
	 *
	 * @param master
	 * @return
	 */
	public static Persistable getLatestVersionByMaster(Master master) {
		try {
			if (master != null) {
				QueryResult qrVersions = VersionControlHelper.service.allVersionsOf(master);
				while (qrVersions.hasMoreElements()) {
					Persistable p = (Persistable) qrVersions.nextElement();
					if (!VersionControlHelper.isAOneOff((OneOffVersioned) p)) {
						return p;
					}
				}
			}
		} catch (QueryException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
		return null;
	}

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
}
