package ext.appo.part.dataUtilities;


import org.apache.log4j.Logger;

import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.AbstractDataUtility;
import com.ptc.core.components.rendering.guicomponents.GUIComponentArray;
import com.ptc.core.components.rendering.guicomponents.TextBox;

import wt.log4j.LogR;
import wt.part.WTPart;
import wt.util.WTException;

public class CISReviewObjectTableAttributeDataUtility extends AbstractDataUtility {

	private static final Logger logger = LogR.getLogger(CISReviewObjectTableAttributeDataUtility.class.getName());

	public Object getDataValue(String componentId, Object datum, ModelContext mc) throws WTException {
		GUIComponentArray guiComponentArray = new GUIComponentArray();
		
		if (datum instanceof WTPart) {
			WTPart part = (WTPart) datum;
			TextBox tb = new TextBox();
			tb.setName(componentId + part.getNumber());
			tb.setId(componentId + part.getNumber());
			tb.setWidth(40);	
			guiComponentArray.addGUIComponent(tb);
		}

		
		return guiComponentArray;
	}
}
