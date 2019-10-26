package ext.appo.util;

import java.util.ArrayList;
import java.util.Locale;

import javax.ws.rs.GET;

import org.apache.log4j.Logger;

import wt.fc.ObjectIdentifier;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.httpgw.GatewayAuthenticator;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.services.ServiceFactory;
import wt.session.SessionHelper;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;


import com.ptc.core.lwc.common.AttributeTemplateFlavor;
import com.ptc.core.lwc.common.TypeDefinitionService;
import com.ptc.core.lwc.common.view.PropertyValueReadView;
import com.ptc.core.lwc.common.view.TypeDefinitionReadView;
import com.ptc.core.lwc.server.LWCNormalizedObject;
import com.ptc.core.lwc.server.LWCStructEnumAttTemplate;
import com.ptc.core.meta.common.TypeInstanceIdentifier;
import com.ptc.core.meta.common.UpdateOperationIdentifier;

public class ClassificationUtil {

	private static Logger log = Logger.getLogger(ClassificationUtil.class.getName());
	/**
	 * @param args
	 * @throws WTException 
	 */
	public static void main(String[] args) throws WTException {
		// TODO Auto-generated method stub
		RemoteMethodServer rms = RemoteMethodServer.getDefault();
		GatewayAuthenticator auth = new GatewayAuthenticator();
		auth.setRemoteUser("wcadmin");
		rms.setAuthenticator(auth);
		createPartnode();
	}

	public static LWCStructEnumAttTemplate getLWCStructEnumAttTemplateByName(
			String nodeName) throws WTException {
		LWCStructEnumAttTemplate node = null;
		QuerySpec qs = new QuerySpec(LWCStructEnumAttTemplate.class);
		SearchCondition searchCondition = new SearchCondition(
				LWCStructEnumAttTemplate.class, LWCStructEnumAttTemplate.NAME,
				SearchCondition.EQUAL, nodeName);
		qs.appendSearchCondition(searchCondition);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		while (qr.hasMoreElements()) {
			node = (LWCStructEnumAttTemplate) qr.nextElement();

		}
		return node;
	}
	
	public static void  getLastNodeName(LWCStructEnumAttTemplate node ,ArrayList<String> rootname)
	{
         LWCStructEnumAttTemplate parentNode=(LWCStructEnumAttTemplate) node.getParent();
         System.out.println("part node===="+parentNode);
         if (parentNode.getName().endsWith("CatlRoot")) 
             {
	    	 rootname.add(node.getName());
	    	 System.out.println("last name======"+rootname);
		     }else {
		    	 System.out.println("root name=="+rootname);
				 getLastNodeName(parentNode,rootname);
			}
	}
    public static String getDisplayName(LWCStructEnumAttTemplate lwcsea) {
        String displayName = "";
        try {
            if (lwcsea != null) {
                Locale locale = SessionHelper.getLocale();
                TypeDefinitionService typeDefineService = ServiceFactory.getService(TypeDefinitionService.class);
                ObjectIdentifier lwcOid = lwcsea.getPersistInfo().getObjectIdentifier();
                TypeDefinitionReadView typeDefReadView = typeDefineService.getTypeDefView(AttributeTemplateFlavor.LWCSTRUCT, lwcOid.getId());
                PropertyValueReadView displayNameView = typeDefReadView.getPropertyValueByName("displayName");
                displayName = displayNameView.getValue(locale, false) == null ? "" : displayNameView.getValue(locale, false).toString();
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return displayName;
    }
	public static  void createPartnode() throws WTException {


		WTPart part = (WTPart)PartUtil.getLastestWTPartByNumber("0000000001");

		ReferenceFactory rf = new ReferenceFactory();
		LWCStructEnumAttTemplate classif_ref =getLWCStructEnumAttTemplateByName("52_冷轧钢板");
		TypeInstanceIdentifier classif_TII = TypedUtilityServiceHelper.service
				.getTypeInstanceIdentifier(classif_ref);
		System.out.println(classif_TII);
		LWCNormalizedObject obj = new LWCNormalizedObject(part, null,
				Locale.US, new UpdateOperationIdentifier());
		obj.load("classification.id");
		obj.set("classification.id", classif_TII);
		part = (WTPart) obj.apply();
		PersistenceHelper.manager.modify(part);
	}
}
