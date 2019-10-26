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

public class PartDocRelationValidator extends DefaultSimpleValidationFilter
{
	private static final Logger LOGGER = LogR.getLogger(PartDocRelationValidator.class.getName());

	/**
	 * Check current user access to remove link between part and doc
	 * 
	 */
	public UIValidationStatus preValidateAction(UIValidationKey key, UIValidationCriteria criteria)
	{
		WTUser currentUser = null;
		String currentUserName = "";
		try
		{
			currentUser = (WTUser) SessionHelper.manager.getPrincipal();
			currentUserName = currentUser.getName();
		} catch (WTException e)
		{
			LOGGER.error(e.getMessage(), e);
		}
		 WTPrincipal userPrincipal=null;
		try {
			userPrincipal = SessionHelper.manager.getPrincipal();
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		UIValidationStatus status = UIValidationStatus.HIDDEN;
		Object object = criteria.getContextObject().getObject();

		if(object instanceof WTPart){
			WTPart  part=(WTPart)object;
			String state=part.getState().toString();
			String createName = ((WTPart) object).getCreatorName();
			String Modifier =part.getModifierName();
			if (isAdmin(userPrincipal,part)) {
				return UIValidationStatus.ENABLED;
			}
			System.out.println("Modifier name is==" + Modifier + ";current user name is====" + currentUserName);
			if(state.endsWith("INWORK")||state.endsWith("REWORK")){
				if (Modifier.equals(currentUserName))
				{
					status = UIValidationStatus.ENABLED;
				}else {
					status = UIValidationStatus.DISABLED;
				}
			}else{
				status = UIValidationStatus.DISABLED;
			}

		}
		return status;
	}
    public static boolean isAdmin(WTPrincipal wtPrincipal,WTPart part) {
    	boolean wcadmin=false;
    	boolean appoadmin=false;
    	boolean admin=false;
        try {
        	wcadmin= WTContainerHelper.service.isAdministrator(WTContainerHelper.service.getExchangeRef(), wtPrincipal);
        	appoadmin= WTContainerHelper.service.isOrgAdministrator(WTContainerHelper.service.getOrgContainerRef(part.getOrganization()), wtPrincipal);
        } catch (WTException e) {
            e.printStackTrace();
        }
        if(wcadmin||appoadmin){
        	admin=true;
        }
        return admin;
    }
}
