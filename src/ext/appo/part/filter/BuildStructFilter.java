package ext.appo.part.filter;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;

import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIClassificationHelper;
import wt.fc.Persistable;
import wt.fc.WTReference;
import wt.part.WTPart;

/**
 * 产品结构搭建的拦截器
 * @author HYJ&NJH
 *
 */
public class BuildStructFilter extends DefaultSimpleValidationFilter{
	
	@Override
	public UIValidationStatus preValidateAction(UIValidationKey uivalidationkey, UIValidationCriteria uivalidationcriteria) {
//		UIValidationStatus uivalidationstatus = UIValidationStatus.DISABLED;
		UIValidationStatus uivalidationstatus = UIValidationStatus.HIDDEN;
		WTReference wtreference = uivalidationcriteria.getContextObject();
		if (wtreference != null) {
			Persistable persistable = wtreference.getObject();
			try {
				if ( persistable != null && persistable instanceof WTPart) {
					WTPart part = (WTPart) persistable;					
					//是否是成品
					String classification = (String) PIAttributeHelper.service.getValue(part, "Classification");
					String nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(classification);
					// 判断是否是成品
					if (nodeHierarchy.contains("appo_cp")) {
						uivalidationstatus = UIValidationStatus.DISABLED;
						String stateVal = part.getLifeCycleState().toString();
						String view=part.getViewName();
						System.out.print("view==========="+view);
						//判断是否是正在工作
						if(stateVal.equals("INWORK")) {
							//判断是否为设计视图
							if(view.startsWith("Design")){
							uivalidationstatus = UIValidationStatus.ENABLED;
							}
						}
					}
				}					
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return uivalidationstatus;
	}
}
