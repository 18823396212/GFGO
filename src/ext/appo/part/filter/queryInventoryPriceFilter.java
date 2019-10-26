package ext.appo.part.filter;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;

import ext.appo.update.ExcelReaderUtil;
import ext.lang.PIStringUtils;
import wt.access.AccessControlHelper;
import wt.access.AccessPermission;
import wt.admin.AdminDomainRef;
import wt.fc.Persistable;
import wt.identity.IdentityFactory;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerHelper;
import wt.inf.team.ContainerTeam;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.team.ContainerTeamManaged;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.org.OrganizationServicesHelper;
import wt.org.WTGroup;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.project.Role;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.type.TypedUtility;
import wt.util.WTException;

public class queryInventoryPriceFilter extends DefaultSimpleValidationFilter {
	private static final Logger LOG = LogR.getLogger(queryInventoryPriceFilter.class.getName());

	public static final String DOC_PRODUCTNAMINGNOTICE_TYPE = "WCTYPE|wt.doc.WTDocument|com.plm.productNamingNotic";

	public static final String PART_TYPE = "WCTYPE|wt.part.WTPart";

	public UIValidationStatus preValidateAction(UIValidationKey uivalidationkey,
			UIValidationCriteria uivalidationcriteria) {
		UIValidationStatus uivalidationstatus = UIValidationStatus.HIDDEN;

		// 判断当前用户是否具有修改权限
		try {
			Persistable persistable = uivalidationcriteria.getContextObject().getObject();
			// 当前用户
			WTPrincipal curUser = SessionHelper.manager.getPrincipal();
			if (LOG.isDebugEnabled()) {
				LOG.debug("ComponentID ：" + uivalidationkey.getComponentID());
				LOG.debug("persistable ：" + persistable);
			}
			if (persistable instanceof WTPart) {
				WTPart part = (WTPart) persistable;
				if (isAdmin(curUser, part)) {
					return UIValidationStatus.ENABLED;
				}
				AdminDomainRef adminDomainRef = null;
				adminDomainRef = part.getDomainRef();
				Boolean operability = isOperability(curUser, PART_TYPE, adminDomainRef, State.toState("INWORK"),
						AccessPermission.CREATE);
				ExcelReaderUtil excelReaderUtil = new ExcelReaderUtil();
				List<Map> rolelist = excelReaderUtil.getExcelData("RDrole.xlsx");
				Boolean isrole = checkRole(curUser, rolelist, part);
				if (operability && isrole) {
					uivalidationstatus = UIValidationStatus.ENABLED;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return uivalidationstatus;
	}

	public Boolean checkRole(WTPrincipal curUser, List<Map> rolelist, WTPart part) {
		Boolean isrole = false;
		try {
			List<String> roles = getUserRolesByContainer((WTUser) curUser, part.getContainer(),
					SessionHelper.manager.getLocale());
			System.out.println("user roles===" + roles.get(0).toString());
			for (Map map2 : rolelist) {
				Map<Persistable, Map> map = new HashMap<>();
				String role = "";
				String remark = "";
				role = (String) map2.get("角色名称");
				remark = (String) map2.get("备注");
				System.out.println("role======" + role);
				if (roles.contains(role) && remark.startsWith("研发人员")) {
					isrole = true;
				}

			}
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isrole;
	}

	/**
	 * 获得用户所属的角色
	 * 
	 * @param user
	 *            用户
	 * @param container
	 *            上下文
	 * @param locale
	 *            时区
	 * @return
	 */
	private List<String> getUserRolesByContainer(WTUser user, WTContainer container, Locale locale) {
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		List<String> roles = new ArrayList<String>();
		try {
			ContainerTeam team = ContainerTeamHelper.service.getContainerTeam((ContainerTeamManaged) container);
			Vector roleVector = team.getRoles();
			// 若角色下有用户(组除外)，则添加到下拉列表
			for (int i = 0; i < roleVector.size(); i++) {
				Role role = (Role) roleVector.get(i);
				Enumeration principalEnum = team.getPrincipalTarget(role);
				while (principalEnum.hasMoreElements()) {
					WTPrincipalReference reference = (WTPrincipalReference) principalEnum.nextElement();
					WTPrincipal principal = reference.getPrincipal();
					if (principal instanceof WTUser) {
						if (user.equals((WTUser) principal)) {
							roles.add(role.getDisplay(locale));
							break;
						}
					} else if (principal instanceof WTGroup) {
						boolean isTeam = OrganizationServicesHelper.manager.isMember((WTGroup) principal, user);
						if (isTeam) {
							roles.add(role.getDisplay(locale));
							break;
						}
					}
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		} finally {
			SessionServerHelper.manager.setAccessEnforced(enforce);
		}
		// LOGGER.debug("Exit .getUserRolesByContainer() roles="+roles);
		return roles;
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
		if (LOG.isDebugEnabled()) {
			LOG.debug("adminDomainRef : " + IdentityFactory.getDisplayIdentity(adminDomainRef.getObject()));
			LOG.debug("displayTypeIdentifier : " + displayTypeIdentifier);
		}
		if (PIStringUtils.isNotNull(displayTypeIdentifier)) {
			return AccessControlHelper.manager.hasAccess(principal, displayTypeIdentifier, adminDomainRef, state,
					AccessPermission.CREATE);
		}

		return false;
	}

	public static boolean isAdmin(WTPrincipal wtPrincipal, WTPart persistable) {
		boolean wcadmin = false;
		boolean appoadmin = false;
		boolean admin = false;
		try {
			wcadmin = WTContainerHelper.service.isAdministrator(WTContainerHelper.service.getExchangeRef(),
					wtPrincipal);
			appoadmin = WTContainerHelper.service.isOrgAdministrator(
					WTContainerHelper.service.getOrgContainerRef(persistable.getOrganization()), wtPrincipal);
		} catch (WTException e) {
			e.printStackTrace();
		}
		if (wcadmin || appoadmin) {
			admin = true;
		}
		return admin;
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
}
