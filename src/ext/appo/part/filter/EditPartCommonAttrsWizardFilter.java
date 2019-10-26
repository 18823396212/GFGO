package ext.appo.part.filter;

import org.apache.log4j.Logger;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;

import ext.pi.PIException;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIClassificationHelper;
import wt.inf.container.WTContainerHelper;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.session.SessionHelper;
import wt.util.WTException;

public class EditPartCommonAttrsWizardFilter extends DefaultSimpleValidationFilter {
	private static final Logger LOGGER = LogR.getLogger(EditPartCommonAttrsWizardFilter.class.getName());

	/**
	 * Check current user access to remove link between part and doc
	 * 
	 */
	public UIValidationStatus preValidateAction(UIValidationKey key, UIValidationCriteria criteria) {
		WTUser currentUser = null;
		String currentUserName = "";
		try {
			currentUser = (WTUser) SessionHelper.manager.getPrincipal();
			currentUserName = currentUser.getName();
		} catch (WTException e) {
			LOGGER.error(e.getMessage(), e);
		}
		WTPrincipal userPrincipal = null;
		try {
			userPrincipal = SessionHelper.manager.getPrincipal();
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		UIValidationStatus status = UIValidationStatus.HIDDEN;
		Object object = criteria.getContextObject().getObject();

		if (object instanceof WTPart) {
			WTPart part = (WTPart) object;
			String state = part.getState().toString();
			String version = part.getVersionIdentifier().getValue();
			System.out.println("state  ====" + state);
			System.out.println("version  ====" + version);
			if (isAdmin(userPrincipal, part)) {
				return UIValidationStatus.ENABLED;
			} else {// 电子料屏蔽权限
				String nodeHierarchy = "";// 获取分类全路径
				// 获取分类内部值
				String value = "";
				try {
					value = (String) PIAttributeHelper.service.getValue(part, "Classification");
					System.out.println("cls value ====" + value);
					nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(value);
					System.out.println("nodeHierarchy value ===" + nodeHierarchy);

				} catch (PIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!nodeHierarchy.contains("appo_dzl")) {
					return UIValidationStatus.ENABLED;
				} else {
					if (version.startsWith("A")) {
						if (state.startsWith("INWORK") || state.startsWith("REWORK")) {
							return UIValidationStatus.ENABLED;
						}
					}
				}
			}
		}
		return status;
	}

	public static boolean isAdmin(WTPrincipal wtPrincipal, WTPart part) {
		boolean wcadmin = false;
		boolean appoadmin = false;
		boolean admin = false;
		try {
			wcadmin = WTContainerHelper.service.isAdministrator(WTContainerHelper.service.getExchangeRef(),
					wtPrincipal);
			appoadmin = WTContainerHelper.service.isOrgAdministrator(
					WTContainerHelper.service.getOrgContainerRef(part.getOrganization()), wtPrincipal);
		} catch (WTException e) {
			e.printStackTrace();
		}
		if (wcadmin || appoadmin) {
			admin = true;
		}
		return admin;
	}
}
