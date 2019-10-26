package ext.appo.part.builder;

import org.apache.log4j.Logger;

import com.ptc.core.ui.resources.ComponentType;
import com.ptc.jca.mvc.components.AbstractAttributesComponentBuilder;
import com.ptc.jca.mvc.components.JcaAttributeConfig;
import com.ptc.jca.mvc.components.JcaGroupConfig;
import com.ptc.mvc.components.AttributePanelConfig;
import com.ptc.mvc.components.ComponentBuilder;
import com.ptc.mvc.components.ComponentConfigFactory;
import com.ptc.mvc.components.ComponentParams;
import com.ptc.mvc.util.ClientMessageSource;

import wt.log4j.LogR;
import wt.util.WTException;

@ComponentBuilder("ext.appo.part.builder.CISAttributesBuilder")

public class CISAttributesBuilder extends AbstractAttributesComponentBuilder {
	private static final Logger logger = LogR.getLogger(CISAttributesBuilder.class.getName());
	private final ClientMessageSource messageSource = getMessageSource("ext.appo.part.resource.PartResourceRB");

	@Override
	protected AttributePanelConfig buildAttributesComponentConfig(final ComponentParams params) throws WTException {
		logger.debug("enter>>>buildAttributesComponentConfig()");
		final ComponentConfigFactory factory = getComponentConfigFactory();
		final AttributePanelConfig panel;
		{
			// panel =
			// factory.newAttributePanelConfig(ComponentId.ATTRIBUTE_PANEL_ID);
			panel = factory.newAttributePanelConfig("attributePanel");
			panel.setComponentType(ComponentType.WIZARD_ATTRIBUTES_TABLE);
			final JcaGroupConfig group;
			{
				group = (JcaGroupConfig) factory.newGroupConfig();
				group.setId("attributes");
				group.setLabel("CIS属性");
				group.setIsGridLayout(true);
				JcaAttributeConfig attribute = getAttribute("libraryref", factory);
				attribute.setLabel(messageSource.getMessage("libraryref"));
				attribute.setDataUtilityId("cisAttributeDataUtility");
				group.addComponent(attribute);
				JcaAttributeConfig attribute2 = getAttribute("footprintref", factory);
				attribute2.setLabel(messageSource.getMessage("footprintref"));
				attribute2.setDataUtilityId("cisAttributeDataUtility");
				group.addComponent(attribute2);
				JcaAttributeConfig attribute3 = getAttribute("footprintref2", factory);
				attribute3.setLabel(messageSource.getMessage("footprintref2"));
				attribute3.setDataUtilityId("cisAttributeDataUtility");
				group.addComponent(attribute3);
				JcaAttributeConfig attribute5 = getAttribute("datasheet", factory);
				attribute5.setLabel(messageSource.getMessage("datasheet"));
				attribute5.setDataUtilityId("cisAttributeDataUtility");
				group.addComponent(attribute5);
			}
			panel.addComponent(group);
		}
		logger.debug("exit<<<<buildAttributesComponentConfig()");
		return panel;
	}

	JcaAttributeConfig getAttribute(final String id, final ComponentConfigFactory factory) {
		final JcaAttributeConfig attribute = (JcaAttributeConfig) factory.newAttributeConfig();
		attribute.setId(id);
		return attribute;
	}
}
