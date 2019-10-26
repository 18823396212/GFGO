package ext.appo.part.filter;
import org.apache.log4j.Logger;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;

import wt.fc.Persistable;
import wt.inf.container.WTContainerHelper;
import wt.org.WTPrincipal;
import wt.part.WTPart;
import wt.session.SessionHelper;
import wt.util.WTException;

public class PartReplacementFilter extends DefaultSimpleValidationFilter{
	private static Logger logger=Logger.getLogger(PartReplacementFilter.class.getName());
	@Override
	public UIValidationStatus preValidateAction(UIValidationKey key, UIValidationCriteria validationCriteria){
		Persistable persistable = validationCriteria.getContextObject().getObject();
		 WTPrincipal userPrincipal=null;
		try {
			userPrincipal = SessionHelper.manager.getPrincipal();
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//logger.debug("now user is=="+userPrincipal.getName());
		if(persistable instanceof WTPart){
			WTPart  part=(WTPart)persistable;
		if (isAdmin(userPrincipal,part)) {
			return UIValidationStatus.ENABLED;
		}
		}
		return UIValidationStatus.HIDDEN;
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
