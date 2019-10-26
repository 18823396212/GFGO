package ext.appo.part.dataUtilities;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.AbstractDataUtility;
import com.ptc.core.components.rendering.guicomponents.ComboBox;
import com.ptc.core.lwc.common.view.TypeDefinitionReadView;
import com.ptc.core.meta.common.DiscreteSet;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIClassificationHelper;
import ext.pi.core.PIWorkflowHelper;
import wt.part.WTPart;
import wt.util.WTException;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;

public class ItemStageControlDataUtility extends AbstractDataUtility {

	// private static final Logger logger =
	// LogR.getLogger(ItemStageControlDataUtility.class.getName());

	public Object getDataValue(String componentId, Object datum, ModelContext mc) throws WTException {
		if (datum instanceof WTPart) {
			NmCommandBean nmCommandBean = mc.getNmCommandBean();
			NmOid pageOid = nmCommandBean.getPageOid();
			Object refObject = pageOid.getRefObject();
			WTPart part = (WTPart) datum;
			if (refObject != null && refObject instanceof WorkItem) {
				WorkItem item = (WorkItem) refObject;
				WfAssignedActivity wfassignedactivity = (WfAssignedActivity) item.getSource().getObject();
				String wfactivityname = wfassignedactivity.getName();
				if (wfactivityname.equals("编制") || wfactivityname.equals("修改")) {
					String number = part.getNumber();
					// TypeIdentifier typeIdentifier = ClientTypedUtility.getTypeIdentifier(part);
					// TypeDefinitionReadView typeDefView =
					// TypeDefinitionServiceHelper.service.getTypeDefView(typeIdentifier);
					String classification = (String) PIAttributeHelper.service.getValue(part, "Classification");
					String nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(classification);
					// 判断是否是成品
					if (nodeHierarchy.contains("appo_cp")) {
						TypeDefinitionReadView node = PIClassificationHelper.service.getNode(classification);
						ComboBox combobox = new ComboBox();
						combobox.setId(componentId + number);
						combobox.setName(componentId + number);
						ArrayList<String> values = new ArrayList<>();
						DiscreteSet legalValues = PIAttributeHelper.service.getLegalValues(node, "cpzt");
						Object[] elements = legalValues.getElements();
						for (int j = 0; elements != null && j < elements.length; j++) {
							values.add(String.valueOf(elements[j]));
						}
						combobox.setValues(values);
						if (wfactivityname.equals("修改")) {
							WfProcess process = PIWorkflowHelper.service.getParentProcess(item);
							String cpztValues = (String) process.getContext().getValue("cpztValues");
							if (cpztValues == null) {
								try {
									JSONObject jsonObject = new JSONObject(cpztValues);
									String value = (String) jsonObject.get(part.getNumber());
									combobox.setSelected(value);
								} catch (JSONException e) {
									e.printStackTrace();
								}
							} else {
								String value = (String) PIAttributeHelper.service.getValue(part, "cpzt");
								combobox.setSelected(value);
							}
						} else {
							String value = (String) PIAttributeHelper.service.getValue(part, "cpzt");
							combobox.setSelected(value);
						}
						return combobox;
					}
					return null;
				} else {
					WfProcess process = PIWorkflowHelper.service.getParentProcess(item);
					// Object value = PIAttributeHelper.service.getValue(part, "cpzt");
					String cpztValues = (String) process.getContext().getValue("cpztValues");
					try {
						JSONObject jsonObject = new JSONObject(cpztValues);
						Object object = jsonObject.get(part.getNumber());
						return object;
					} catch (JSONException e) {
						e.printStackTrace();
					}
					return null;
				}
			}
		}
		// return DataUtilityHelper.getDefaultDataValue(componentId, datum, mc);
		return null;
	}
}
