package ext.appo.part.dataUtilities;

import org.apache.log4j.Logger;

import wt.log4j.LogR;
import wt.part.WTPart;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.util.WTException;

import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.AbstractDataUtility;
import com.ptc.core.components.rendering.guicomponents.UrlDisplayComponent;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmURLFactoryBean;
import com.ptc.netmarkets.util.misc.NetmarketURL;

public class ShowProductNamingNoticDataUtility extends AbstractDataUtility {

	private static final Logger logger = LogR.getLogger(ShowProductNamingNoticDataUtility.class.getName());
	
	@Override
	public Object getDataValue(String componentId, Object datum, ModelContext mc) throws WTException {
		WTPart wtPart = (WTPart) datum;
		if(componentId.equals("name")){
			String name = wtPart.getName();
			UrlDisplayComponent urlDisplayComponent = new UrlDisplayComponent(name, name,
					NetmarketURL.buildURL(new NmURLFactoryBean(), "object", "view",
							NmOid.newNmOid(wtPart.getPersistInfo().getObjectIdentifier()), null));
			return urlDisplayComponent ;
		}else{
			SessionContext previous = SessionContext.newContext();
			try {
				// 当前用户设置为管理员，用于忽略权限
				SessionHelper.manager.setAdministrator();
				
				return mc.getRawValue() ;
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				throw new WTException(e.getLocalizedMessage()) ;
			} finally{
				SessionContext.setContext(previous);
			}
		}
	}

}
