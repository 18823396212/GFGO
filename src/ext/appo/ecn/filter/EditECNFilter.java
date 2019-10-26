package ext.appo.ecn.filter;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;

import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.fc.WTReference;

public class EditECNFilter extends DefaultSimpleValidationFilter {

	@Override
	public UIValidationStatus preValidateAction(UIValidationKey uivalidationkey,
			UIValidationCriteria uivalidationcriteria) {
		// UIValidationStatus uivalidationstatus = UIValidationStatus.DISABLED;
		UIValidationStatus uivalidationstatus = UIValidationStatus.HIDDEN;
		WTReference wtreference = uivalidationcriteria.getContextObject();
		if (wtreference != null) {
			Persistable persistable = wtreference.getObject();
			try {
				if (persistable != null && persistable instanceof WTChangeOrder2) {
					WTChangeOrder2 changeOrder2 = (WTChangeOrder2) persistable;
					uivalidationstatus = UIValidationStatus.DISABLED;
					String stateVal = changeOrder2.getLifeCycleState().toString();
					// 判断是否是开启
					if (stateVal.equals("OPEN")||stateVal.equals("REWORK")) {
						uivalidationstatus = UIValidationStatus.ENABLED;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return uivalidationstatus;
	}
}
