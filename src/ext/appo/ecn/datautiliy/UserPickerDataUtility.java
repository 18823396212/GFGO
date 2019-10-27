package ext.appo.ecn.datautiliy;

import com.ptc.core.components.beans.CreateAndEditWizBean;
import com.ptc.core.components.descriptor.ComponentDescriptor;
import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.dataUtilities.AttributeDataUtilityHelper;
import com.ptc.core.components.rendering.PickerRenderConfigs;
import com.ptc.core.components.rendering.guicomponents.PickerInputComponent;
import com.ptc.windchill.enterprise.change2.dataUtilities.ChangeLinkAttributeDataUtility;
import ext.appo.ecn.beans.ChangeTaskBean;
import ext.appo.ecn.config.UserPickerConfig;
import org.apache.log4j.Logger;
import wt.log4j.LogR;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.util.WTException;

import java.util.Map;

public class UserPickerDataUtility extends ChangeLinkAttributeDataUtility {

    private static final String CLASSNAME = UserPickerDataUtility.class.getName();
    private static final Logger LOG = LogR.getLogger(CLASSNAME);

    @Override
    public Object getDataValue(String paramString, Object paramObject, ModelContext paramModelContext) throws WTException {
        SessionContext previous = SessionContext.newContext();
        try {
            // 当前用户设置为管理员，用于忽略权限
            SessionHelper.manager.setAdministrator();

            // 是否创建及编辑状态
            boolean bool = CreateAndEditWizBean.isCreateEditWizard(paramModelContext.getNmCommandBean());
            if (bool) {
                PickerInputComponent userPicker = (PickerInputComponent) createPickerComponent(paramString, paramObject, paramModelContext, bool);
                //userPicker.addJsAction("onChange", "searchUser(this,0) ;") ;
                return userPicker;
            } else {
                return createDisplayComponent(paramString, paramObject, paramModelContext);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        } finally {
            SessionContext.setContext(previous);
        }
    }

    public Object createPickerComponent(String param, Object object, ModelContext modelContext, Boolean isCreateEdit) throws WTException {
        String label = getLabel(param, modelContext);
        if (label == null) {
            label = "Picker";
        }
        // 初始化Picker数据
        if (object instanceof ChangeTaskBean) {
            setPickerConfig(param, modelContext, modelContext.getRawValue());
        } else {
            // 是否创建及编辑状态
            AffectedItemsDataUtility dataUtility = new AffectedItemsDataUtility();
            setPickerConfig(param, modelContext, dataUtility.getValue(modelContext, object, isCreateEdit, param));
        }

        ComponentDescriptor componentDescriptor = modelContext.getDescriptor();
        Map<Object, Object> propertiesMap = componentDescriptor.getProperties();
        String defaultValue = (String) propertiesMap.get(PickerRenderConfigs.DEFAULT_HIDDEN_VALUE);

        PickerInputComponent component = new PickerInputComponent(label, defaultValue, PickerRenderConfigs.getPickerConfigs(propertiesMap));
        component.setColumnName(AttributeDataUtilityHelper.getColumnName(param, object, modelContext));
        component.setRequired(true);

        return component;
    }

    /***
     * 初始化Picker数据
     *
     * @param param
     * @param modelContext
     * @param value
     * @throws WTException
     */
    public void setPickerConfig(String param, ModelContext modelContext, Object value) throws WTException {
        // 获取参数列表
        Map<Object, Object> propertiesMap = modelContext.getDescriptor().getProperties();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Befor propertiesMap : " + propertiesMap);
        }
        // 添加Picker配置
        UserPickerConfig.setPickerProperties(param, modelContext.getNmCommandBean(), propertiesMap);
        // 添加默认值
        propertiesMap.put(PickerRenderConfigs.DEFAULT_HIDDEN_VALUE, value == null ? "" : (String) value);
        propertiesMap.put(PickerRenderConfigs.DEFAULT_VALUE, value == null ? "" : (String) value);
        if (LOG.isDebugEnabled()) {
            LOG.debug("After propertiesMap : " + propertiesMap);
        }
    }

    /***
     * 获取默认值
     *
     * @param param
     * @param object
     * @param modelContext
     * @return
     * @throws WTException
     */
    public Object createDisplayComponent(String param, Object object, ModelContext modelContext) throws WTException {
        String value = "";

        if (modelContext != null) {
            Object rawValue = modelContext.getRawValue();

            if (rawValue != null) {
                value = rawValue.toString();
            } else {
                value = "";
            }
        }

        return value;
    }
}
