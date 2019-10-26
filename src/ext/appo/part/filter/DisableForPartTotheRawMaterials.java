package ext.appo.part.filter;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;

import wt.fc.Persistable;
import wt.part.WTPart;

public class DisableForPartTotheRawMaterials extends DefaultSimpleValidationFilter {

	@Override
	public UIValidationStatus preValidateAction(UIValidationKey uivalidationkey,
			UIValidationCriteria validationCriteria) {

		System.out.println("into DisableForPartTotheRawMaterials");

		Persistable persistable = validationCriteria.getContextObject().getObject();
		boolean isAdmin = validationCriteria.isSiteAdmin() || validationCriteria.isOrgAdmin();

		// 组织、站点管理员以及指定组成员有权限
		if (isAdmin) {
			return UIValidationStatus.ENABLED;
		}

		if (persistable instanceof WTPart) {
			// mao
			WTPart part = (WTPart) persistable;
			String container = part.getContainer().getName();

			System.out.println("container===" + container);

			if ("原材料库".equals(container)) {
				return UIValidationStatus.HIDDEN;
			}
		}

		return UIValidationStatus.ENABLED;
	}

}
