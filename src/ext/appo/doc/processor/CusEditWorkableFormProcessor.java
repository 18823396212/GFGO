package ext.appo.doc.processor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import wt.fc.Persistable;
import wt.util.WTException;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.EditWorkableFormProcessor;
import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.pi.core.PIAttributeHelper;
/**
 * 新增产品线和项目的处理
 * 
 * @author HYJ&NJH
 *
 */
public class CusEditWorkableFormProcessor extends EditWorkableFormProcessor {
	@Override
	public FormResult postProcess(NmCommandBean nmcommandBean, List<ObjectBean> arg1) throws WTException {
		FormResult postProcess = super.postProcess(nmcommandBean, arg1);

		ObjectBean objectBean = arg1.get(0);
		Persistable persistable = (Persistable) objectBean.getObject();
		
		if (persistable != null) {
			// 回填所属产品线及所属项目
			HashMap<?, ?> comboBox = nmcommandBean.getComboBox();
			Iterator<?> iterator = comboBox.keySet().iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				if (key.contains("sscpx")) {
					Object value = comboBox.get(key);
					if(value instanceof List){
						List<?> list = (List<?>)value ;
						if(list.size() > 0){
							PIAttributeHelper.service.forceUpdateSoftAttribute(persistable, "sscpx", list.get(0));
						}
					}else if (value != null) {
						PIAttributeHelper.service.forceUpdateSoftAttribute(persistable, "sscpx", value);
					}
				} else if (key.contains("ssxm")) {
					Object value = comboBox.get(key);
					if(value instanceof List){
						List<?> list = (List<?>)value ;
						if(list.size() > 0){
							PIAttributeHelper.service.forceUpdateSoftAttribute(persistable, "ssxm", list.get(0));
						}
					}else if (value != null) {
						PIAttributeHelper.service.forceUpdateSoftAttribute(persistable, "ssxm", value);
					}
				}
			}

		}
		return postProcess;
	}
}
