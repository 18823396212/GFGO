package ext.appo.part.dataUtilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.AbstractDataUtility;
import com.ptc.core.components.factory.dataUtilities.AttributeDataUtilityHelper;
import com.ptc.core.components.rendering.guicomponents.ComboBox;
import com.ptc.core.components.rendering.guicomponents.GUIComponentArray;
import com.ptc.core.components.rendering.guicomponents.TextBox;
import com.ptc.core.lwc.common.view.AttributeDefinitionReadView;
import com.ptc.core.lwc.common.view.ConstraintDefinitionReadView;
import com.ptc.core.lwc.common.view.ConstraintDefinitionReadView.RuleDataObject;
import com.ptc.core.lwc.common.view.EnumerationDefinitionReadView;
import com.ptc.core.lwc.common.view.EnumerationEntryReadView;
import com.ptc.core.lwc.common.view.TypeDefinitionReadView;
import com.ptc.core.lwc.server.LWCEnumerationBasedConstraint;
import com.ptc.core.meta.common.DiscreteSet;
import com.ptc.core.meta.container.common.impl.DiscreteSetConstraint;
import com.ptc.core.meta.container.common.impl.ValueRequiredConstraint;

import ext.appo.part.beans.ProductLineConfigBean;
import ext.appo.part.workflow.ReadExcelData;
import ext.lang.PIStringUtils;
import ext.pi.core.PIClassificationHelper;
import wt.log4j.LogR;
import wt.util.WTException;

public class ProductNamingNoticDataUtility extends AbstractDataUtility {

	private static final Logger LOG = LogR.getLogger(ProductNamingNoticDataUtility.class.getName());

	@Override
	public Object getDataValue(String componentId, Object datum, ModelContext mc) throws WTException {

		Map<String, String[]> parameterMap = mc.getNmCommandBean().getRequest().getParameterMap();
		// 获取产品线
		String productLine = "";
		if (parameterMap.containsKey("productLine")) {
			String[] str = parameterMap.get("productLine");
			if (str != null && str.length > 0) {
				productLine = str[0];
			}
		}
		// 读取‘所属产品线’属性配置
		List<ProductLineConfigBean> productLineConfigArray = ReadExcelData.getProductLineConfig();
		for (ProductLineConfigBean bean : productLineConfigArray) {
			if (bean.getEnumName().equalsIgnoreCase(productLine)) {
				if (PIStringUtils.isNotNull(bean.getNodeName())) {
					productLine = bean.getNodeName();
				}
				break;
			}
		}
		GUIComponentArray guiComponentArray = new GUIComponentArray();
		// 获取分类属性定义的合法值或枚举值列表
		Map<ArrayList<String>, ArrayList<String>> ruleMap = getClfAttributeRule(productLine, componentId);
		if (LOG.isDebugEnabled()) {
			LOG.debug("productLine : " + productLine);
			LOG.debug("componentId : " + componentId);
			LOG.debug("ruleMap : " + ruleMap);
		}
		if (ruleMap.size() > 0) {
			guiComponentArray.addGUIComponent(generateComboBox(mc, datum, componentId, ruleMap));
		} else {
			guiComponentArray.addGUIComponent(generateTextBox(mc, datum, productLine, componentId));
		}

		// TextBox tb = new TextBox();
		// tb.setName(componentId);
		// tb.setId(componentId);
		// tb.setWidth(20);
		// String value = "";
		// try {
		// if(datum instanceof Map){
		// value = ((Map<String, String>) datum).get(componentId);
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// tb.setValue(value);
		// tb.addJsAction("onChange", "saveInputValue();") ;
		// guiComponentArray.addGUIComponent(tb);
		return guiComponentArray;
	}

	@SuppressWarnings("unchecked")
	public TextBox generateTextBox(ModelContext paramModelContext, Object paramObject, String nodeName, String keyStr)
			throws WTException {
		TextBox textBox = new TextBox();
		textBox.setWidth(20);
		textBox.addJsAction("onChange", "delaySaveInputValue();");
		textBox.setId(keyStr);
		textBox.setName(keyStr);
		// 属性值
		String value = "";
		if (paramObject instanceof Map) {
			value = ((Map<String, String>) paramObject).get(keyStr);
		}
		textBox.setValue(value);
		textBox.setColumnName(AttributeDataUtilityHelper.getColumnName(keyStr, paramObject, paramModelContext));
		if (keyStr.equals("name")) {
			textBox.setRequired(true);
		} else {
			textBox.setRequired(checkAttributeRequired(nodeName, keyStr));
		}
		return textBox;
	}

	@SuppressWarnings("unchecked")
	public ComboBox generateComboBox(ModelContext paramModelContext, Object paramObject, String keyStr,
			Map<ArrayList<String>, ArrayList<String>> enumMap) throws WTException {
		// 内部名称
		ArrayList<String> keyArray = enumMap.keySet().iterator().next();
		// 显示名称
		ArrayList<String> displayArray = enumMap.get(keyArray);
		// 选取值
		ArrayList<String> selectArray = new ArrayList<String>();
		// 属性值
		String value = "";
		if (paramObject instanceof Map) {
			value = ((Map<String, String>) paramObject).get(keyStr);
		}
		if (value != null && value != "") {
			selectArray.add(value == null ? "" : (String) value);
		} else {
			if (keyArray.size() > 0) {
				selectArray.add(keyArray.get(0));
			}
		}
		ComboBox box = new ComboBox(keyArray, displayArray, selectArray);
		box.setId(keyStr);
		box.setName(keyStr);
		box.setColumnName(AttributeDataUtilityHelper.getColumnName(keyStr, paramObject, paramModelContext));
		box.setMultiSelect(false);
		box.addJsAction("onChange", "delaySaveInputValue();");
		return box;
	}

	/***
	 * 获取分类属性定义的合法值或枚举值列表
	 * 
	 * @param nodeName
	 *            分类节点内部名称
	 * @param attributeName
	 *            分类属性内部名称
	 * @return
	 * @throws WTException
	 */
	public Map<ArrayList<String>, ArrayList<String>> getClfAttributeRule(String nodeName, String attributeName)
			throws WTException {
		Map<ArrayList<String>, ArrayList<String>> ruleMap = new HashMap<ArrayList<String>, ArrayList<String>>();
		if (PIStringUtils.isNull(nodeName) || PIStringUtils.isNull(attributeName)) {
			return ruleMap;
		}

		// 分类节点属性合法值验证
		TypeDefinitionReadView readView = PIClassificationHelper.service.getNode(nodeName);
		if (readView != null) {
			getAttributeRule(readView, attributeName, ruleMap);
		}

		return ruleMap;
	}

	/***
	 * 获取类型上属性定义的合法值或枚举值列表
	 * 
	 * @param readView
	 * @param attributeName
	 * @param ruleMap
	 */
	public void getAttributeRule(TypeDefinitionReadView readView, String attributeName,
			Map<ArrayList<String>, ArrayList<String>> ruleMap) {
		if (readView == null || PIStringUtils.isNull(attributeName) || ruleMap == null) {
			return;
		}

		// 获取属性定义
		AttributeDefinitionReadView av = readView.getAttributeByName(attributeName);
		if (av != null) {
			Collection<ConstraintDefinitionReadView> constraints = av.getAllConstraints();
			for (ConstraintDefinitionReadView cdReadView : constraints) {
				String rule = cdReadView.getRule().getKey().toString();
				if (rule.contains(DiscreteSetConstraint.class.getName())) {
					// 获取合法值集
					RuleDataObject rd = cdReadView.getRuleDataObj();
					if (rd != null) {
						Object obj = rd.getRuleData();
						if (obj != null && obj instanceof DiscreteSet) {
							// 存储合法值
							ArrayList<String> valueArray = new ArrayList<String>();
							for (Object object : ((DiscreteSet) obj).getElements()) {
								if ("cpzt".equals(attributeName)) {
									if ("研发状态".equals(object.toString())) {
										valueArray.add(object.toString());
									}
								} else {
									valueArray.add(object.toString());
								}
							}
							if (valueArray.size() > 0) {
								ruleMap.put(valueArray, valueArray);
							}
						} else {
							// 处理属性关联的全局枚举
							EnumerationDefinitionReadView eReadView = rd.getEnumDef();
							if (eReadView != null) {
								// 内部名称集合
								ArrayList<String> interiorArray = new ArrayList<String>();
								// 显示名称
								ArrayList<String> displayArray = new ArrayList<String>();
								for (Map.Entry<String, EnumerationEntryReadView> entryMap : eReadView
										.getAllEnumerationEntries().entrySet()) {
									EnumerationEntryReadView eeReadView = entryMap.getValue();
									// 内部名称
									String enumKey = eeReadView.getName();
									// 显示名称
									String enumName = eeReadView.getPropertyValueByName("displayName").getValue()
											.toString();
									// 是否可用
									String selectable = eeReadView.getPropertyValueByName("selectable").getValue()
											.toString();
									if (LOG.isDebugEnabled()) {
										LOG.debug("EnumerationName : " + av.getDisplayName());
										LOG.debug("enumName : " + enumName);
										LOG.debug("selectable : " + selectable);
									}
									if ("true".equals(selectable)) {

										interiorArray.add(enumKey);
										displayArray.add(enumName);
									}
								}
								if (interiorArray.size() > 0) {
									ruleMap.put(interiorArray, displayArray);
								}
							}
						}
					}
				} else if (rule.contains(LWCEnumerationBasedConstraint.class.getName())) { // 处理属性本身定义的枚举
					RuleDataObject rd = cdReadView.getRuleDataObj();
					if (rd != null) {
						// 内部名称集合
						ArrayList<String> interiorArray = new ArrayList<String>();
						// 显示名称
						ArrayList<String> displayArray = new ArrayList<String>();
						// 获取枚举值集
						Collection<EnumerationEntryReadView> coll = rd.getEnumDef().getAllEnumerationEntries().values();
						for (EnumerationEntryReadView eReadView : coll) {
							// 内部名称
							String enumKey = eReadView.getName();
							// 显示名称
							String enumName = eReadView.getPropertyValueByName("displayName").getValue().toString();
							// 是否可用
							String selectable = eReadView.getPropertyValueByName("selectable").getValue().toString();
							if (LOG.isDebugEnabled()) {
								LOG.debug("EnumerationName : " + av.getDisplayName());
								LOG.debug("enumName : " + enumName);
								LOG.debug("selectable : " + selectable);
							}
							if ("true".equals(selectable)) {
								interiorArray.add(enumKey);
								displayArray.add(enumName);
							}
						}
						if (interiorArray.size() > 0) {
							ruleMap.put(interiorArray, displayArray);
						}
					}
				}
			}
		}
	}

	/***
	 * 获取类型上属性定义是否必填
	 * 
	 * @param nodeName
	 * @param attributeName
	 * @return
	 * @throws WTException
	 */
	public Boolean checkAttributeRequired(String nodeName, String attributeName) throws WTException {
		Boolean required = false;
		if (PIStringUtils.isNull(nodeName) || PIStringUtils.isNull(attributeName)) {
			return required;
		}

		// 分类节点属性合法值验证
		TypeDefinitionReadView readView = PIClassificationHelper.service.getNode(nodeName);
		if (readView != null) {
			required = getAttributeRequiredRule(readView, attributeName);
		}

		return required;
	}

	/***
	 * 获取类型上属性定义是否必填
	 * 
	 * @param readView
	 * @param attributeName
	 * @return
	 */
	public Boolean getAttributeRequiredRule(TypeDefinitionReadView readView, String attributeName) {
		Boolean required = false;
		if (readView == null || PIStringUtils.isNull(attributeName)) {
			return required;
		}

		// 获取属性定义
		AttributeDefinitionReadView av = readView.getAttributeByName(attributeName);
		if (av != null) {
			Collection<ConstraintDefinitionReadView> constraints = av.getAllConstraints();
			for (ConstraintDefinitionReadView cdReadView : constraints) {
				String rule = cdReadView.getRule().getKey().toString();
				if (rule.contains(ValueRequiredConstraint.class.getName())) {
					required = true;
					break;
				}
			}
		}

		return required;
	}
}
