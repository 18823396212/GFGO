package ext.appo.part.filter;

import org.apache.log4j.Logger;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;

import wt.inf.container.WTContainerHelper;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.session.SessionHelper;
import wt.util.WTException;

public class IsAdministratorFilter extends DefaultSimpleValidationFilter {
	private static final Logger LOGGER = LogR.getLogger(IsAdministratorFilter.class.getName());

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
			if (isAdmin(userPrincipal, part)) {
				return UIValidationStatus.ENABLED;
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
		System.out.println("admin==" + admin);
		return admin;
	}
}
