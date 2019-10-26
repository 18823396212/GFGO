package ext.appo.part.processor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.windchill.enterprise.part.forms.EditPartFormProcessor;

import ext.pi.core.PIAttributeHelper;
import wt.inf.container.WTContainerHelper;
import wt.org.WTPrincipal;
import wt.part.WTPart;
import wt.session.SessionHelper;
import wt.util.WTException;

public class CusEditPartFormProcessor extends EditPartFormProcessor{

	@Override
	public FormResult postProcess(NmCommandBean nmcommandBean, List<ObjectBean> arg1) throws WTException {
		FormResult postProcess = super.postProcess(nmcommandBean, arg1);
		 WTPrincipal userPrincipal=null;
		try {
			userPrincipal = SessionHelper.manager.getPrincipal();
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("nmcommandBean.getTex()=="+nmcommandBean.getText());
		System.out.println("nmcommandBean.getTextArea()=="+nmcommandBean.getTextArea());
		ObjectBean objectBean = arg1.get(0);
		WTPart part = (WTPart) objectBean.getObject();
		System.out.println("version=="+part.getVersionIdentifier().getValue()+part.getIterationIdentifier().getValue());
		if(part!=null) {
			HashMap comboBox = nmcommandBean.getComboBox();
            if (!part.getVersionIdentifier().getValue().toString().equalsIgnoreCase("A")&&
            		!isAdmin(userPrincipal,part)) {


			HashMap radio =nmcommandBean.getRadio();
			HashMap OldRadio =nmcommandBean.getOldRadio();
            
			String radio1="";
			String radio2="";
			
			Set keySet1 = radio.keySet();
			Iterator iterator1 =keySet1.iterator();
			System.out.println("iterator1==="+iterator1);
			while(iterator1.hasNext()) {
				String key = (String) iterator1.next();
				if(key.contains("sfxnj")) {
					radio1 = radio.get(key).toString();
				}
			}
			Set keySetold = OldRadio.keySet();
			Iterator iterator2 =keySetold.iterator();
			System.out.println("iterator1==="+iterator2);
			while(iterator2.hasNext()) {
				String key = (String) iterator2.next();
				if(key.contains("sfxnj")) {
                radio2 = OldRadio.get(key).toString();
				}
			}
			System.out.println("radio1==="+radio1);
			System.out.println("radio2==="+radio2);
		    if (!radio1.equalsIgnoreCase(radio2)) {
		    	throw new WTException("'是否工艺虚拟件'属性不能修改，请在OA申请修改！");
			}
			
		}
			Set keySet = comboBox.keySet();
			Iterator iterator = keySet.iterator();
			while(iterator.hasNext()) {
				String key = (String) iterator.next();
				if(key.contains("sscpx")) {
					Object value = comboBox.get(key);
					if(value!=null) {
						part = (WTPart) PIAttributeHelper.service.forceUpdateSoftAttribute(part, "sscpx", value);
					}
				}else if(key.contains("ssxm")){
					Object value = comboBox.get(key);
					if(value!=null) {
						part = (WTPart) PIAttributeHelper.service.forceUpdateSoftAttribute(part, "ssxm", value);
					}
				}
				
			}
		}
		return postProcess;
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
