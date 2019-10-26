package ext.appo.part.dataUtilities;


import org.apache.log4j.Logger;

import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.AbstractDataUtility;
import com.ptc.core.components.factory.dataUtilities.AttributeDataUtilityHelper;
import com.ptc.core.components.rendering.guicomponents.StringInputComponent;

import wt.log4j.LogR;
import wt.util.WTException;

public class CISAttributeDataUtility extends AbstractDataUtility {

	private static final Logger logger = LogR.getLogger(CISAttributeDataUtility.class.getName());

	public Object getDataValue(String componentId, Object datum, ModelContext mc) throws WTException {
		StringInputComponent stringInputComponent = new StringInputComponent();
		stringInputComponent.setId(componentId);
		
		String columnName = AttributeDataUtilityHelper.getColumnName(componentId, datum, mc);
		stringInputComponent.setColumnName(columnName == null ? componentId : columnName);
		
		stringInputComponent.setEditable(true);
		stringInputComponent.setEnabled(false);
		stringInputComponent.setLabel(componentId);
		stringInputComponent.setName(componentId);
		
		
		boolean inputRequired = AttributeDataUtilityHelper.isInputRequired(mc);
		stringInputComponent.setRequired(inputRequired);
		
		
		return stringInputComponent;
	}
}
