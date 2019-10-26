package ext.appo.part.filter;

import java.util.Collection;
import java.util.Iterator;

import com.ptc.core.config.dca.coreResource;
import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;

import ext.pi.PIException;
import ext.pi.core.PIClassificationHelper;
import wt.fc.Persistable;
import wt.fc.WTReference;
import wt.part.WTPart;

public class SaveAsGenerationCodeFilter extends DefaultSimpleValidationFilter {

	@Override
	public UIValidationStatus preValidateAction(UIValidationKey uiValidationKey,
			UIValidationCriteria uivalidationcriteria) {

		UIValidationStatus uivalidationstatus = UIValidationStatus.HIDDEN;

		WTReference wtreference = uivalidationcriteria.getContextObject();
		Persistable per = wtreference.getObject();
		if (per != null && per instanceof WTPart) {
			WTPart part = (WTPart) per;

			String number = part.getNumber();

			Collection<String> classifyNodes = null;
			try {
				classifyNodes = PIClassificationHelper.service.getClassifyNodes(part);
				if (classifyNodes != null) {
					Iterator<String> iterator = classifyNodes.iterator();
					while (iterator.hasNext()) {
						String next = iterator.next();
						String nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(next);
						// 结构件
						if (nodeHierarchy.contains("appo_jgj")) {
							number = number.substring(number.length() - 2, number.length());
							if (number.equals("00")) {
								uivalidationstatus = UIValidationStatus.ENABLED;
							}

						} else if (nodeHierarchy.contains("appo_zcljjzz")
								|| nodeHierarchy.contains("appo_zcljjzdhsb")) {
							number = number.substring(number.length() - 3, number.length());
							if (number.equals("000")) {
								uivalidationstatus = UIValidationStatus.ENABLED;
							}
						}
					}
				}
			} catch (PIException e) {
				e.printStackTrace();
			}
		}
		return uivalidationstatus;
	}

}
