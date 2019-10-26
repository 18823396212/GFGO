package ext.appo.doc.filter;

import org.apache.log4j.Logger;

import wt.access.AccessControlHelper;
import wt.access.AccessPermission;
import wt.admin.AdminDomainRef;
import wt.doc.WTDocument;
import wt.fc.Persistable;
import wt.folder.Cabinet;
import wt.folder.SubFolder;
import wt.identity.IdentityFactory;
import wt.inf.container.WTContainerHelper;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.part.WTPart;
import wt.session.SessionHelper;
import wt.type.TypedUtility;
import wt.util.WTException;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;

import ext.lang.PIStringUtils;

public class PartProducctNamingNoticeFilter extends DefaultSimpleValidationFilter {
	private static final Logger LOG = LogR.getLogger(PartProducctNamingNoticeFilter.class.getName());
	
	public static final String DOC_PRODUCTNAMINGNOTICE_TYPE = "WCTYPE|wt.doc.WTDocument|com.plm.productNamingNotic" ;
	
	public static final String PART_TYPE = "WCTYPE|wt.part.WTPart" ;

	public UIValidationStatus preValidateAction(UIValidationKey uivalidationkey, UIValidationCriteria uivalidationcriteria) {
		UIValidationStatus uivalidationstatus = UIValidationStatus.HIDDEN;
		
		// 判断当前用户是否具有修改权限
		try {
			Persistable persistable = uivalidationcriteria.getContextObject().getObject();
			// 当前用户
			WTPrincipal curUser = SessionHelper.manager.getPrincipal();
			if(LOG.isDebugEnabled()){
				LOG.debug("ComponentID ：" + uivalidationkey.getComponentID());
				LOG.debug("persistable ：" + persistable);
			}
			if (persistable instanceof WTDocument) {				
			WTDocument doc =(WTDocument)persistable;					
			if (isAdmin(curUser,doc)) {
				return UIValidationStatus.ENABLED;
			}
			}
			if(uivalidationkey.getComponentID().equalsIgnoreCase("productNamingNotice")){
				AdminDomainRef adminDomainRef = null ;
				if(persistable instanceof Cabinet){
					adminDomainRef = ((Cabinet)persistable).getDomainRef() ;
				}else if(persistable instanceof SubFolder){
					adminDomainRef = ((SubFolder)persistable).getDomainRef() ;
				}
				Boolean operability = isOperability(curUser, DOC_PRODUCTNAMINGNOTICE_TYPE, adminDomainRef, null, AccessPermission.CREATE) ;
				if(operability){
					uivalidationstatus = UIValidationStatus.ENABLED;
				}
			}else if (uivalidationkey.getComponentID().equalsIgnoreCase("editProductNamingNotice")){
				if(AccessControlHelper.manager.hasAccess(curUser, persistable, AccessPermission.MODIFY)){
					uivalidationstatus = UIValidationStatus.ENABLED;
				}
			}else if (uivalidationkey.getComponentID().equalsIgnoreCase("exportPartStruct")){
				AdminDomainRef adminDomainRef = null ;
				if(persistable instanceof WTPart){
					SubFolder folder = (SubFolder)((WTPart)persistable).getFolderingInfo().getParentFolder().getObject() ;
					adminDomainRef = folder.getDomainRef();
				}
				Boolean operability = isOperability(curUser, PART_TYPE, adminDomainRef, null, AccessPermission.CREATE) ;
				if(operability){
					uivalidationstatus = UIValidationStatus.ENABLED;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return uivalidationstatus;
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
	public Boolean isOperability(WTPrincipal principal, String type, AdminDomainRef adminDomainRef, State state, AccessPermission accessPermission) throws WTException{
		if(PIStringUtils.isNull(type) || adminDomainRef == null || accessPermission == null){
			return false ;
		}
		
		String displayTypeIdentifier = displayTypeIdentifier(type) ;
		if(LOG.isDebugEnabled()){
			LOG.debug("adminDomainRef : " + IdentityFactory.getDisplayIdentity(adminDomainRef.getObject()));
			LOG.debug("displayTypeIdentifier : " + displayTypeIdentifier);
		}
		if(PIStringUtils.isNotNull(displayTypeIdentifier)){
			return AccessControlHelper.manager.hasAccess(principal, displayTypeIdentifier, adminDomainRef, state, AccessPermission.CREATE);
		}
		
		return false ;
	}

    public static boolean isAdmin(WTPrincipal wtPrincipal,WTDocument persistable) {
    	boolean wcadmin=false;
    	boolean appoadmin=false;
    	boolean admin=false;
        try {
        	wcadmin= WTContainerHelper.service.isAdministrator(WTContainerHelper.service.getExchangeRef(), wtPrincipal);
        	appoadmin= WTContainerHelper.service.isOrgAdministrator(WTContainerHelper.service.getOrgContainerRef(persistable.getOrganization()), wtPrincipal);
        } catch (WTException e) {
            e.printStackTrace();
        }
        if(wcadmin||appoadmin){
        	admin=true;
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
