package ext.generic.integration.erp.query;

import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.session.SessionHelper;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;

/***
 * 控制查询集成数据按钮
 * @author KWang
 *
 */
public class QueryIntegrationDataActionFilter extends DefaultSimpleValidationFilter{
	
	public UIValidationStatus preValidateAction(UIValidationKey uivalidationkey,UIValidationCriteria uivalidationcriteria) {
		
		UIValidationStatus uivalidationstatus = UIValidationStatus.HIDDEN;
		
		
		try {
			//获取当前用户
			WTUser curUser = (WTUser) SessionHelper.manager.getPrincipal();
			WTPrincipalReference curUserRef = WTPrincipalReference.newWTPrincipalReference( curUser );
			//判断当前用户是否为管理员
			if( curUserRef.getName().equals("Administrator") ){
				uivalidationstatus =  UIValidationStatus.ENABLED;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return uivalidationstatus;
	}
}
