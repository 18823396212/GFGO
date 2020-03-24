package ext.appo.change.datautility;

import com.ptc.core.components.beans.CreateAndEditWizBean;
import com.ptc.core.components.descriptor.ComponentDescriptor;
import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.dataUtilities.AttributeDataUtilityHelper;
import com.ptc.core.components.rendering.PickerRenderConfigs;
import com.ptc.core.components.rendering.guicomponents.PickerInputComponent;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.windchill.enterprise.change2.dataUtilities.ChangeLinkAttributeDataUtility;
import ext.appo.change.ModifyHelper;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.models.CorrelationObjectLink;
import ext.appo.ecn.beans.ChangeTaskBean;
import ext.appo.ecn.config.UserPickerConfig;
import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.ecn.datautiliy.AffectedItemsDataUtility;
import ext.pi.core.PICoreHelper;
import org.apache.log4j.Logger;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.util.WTException;

import java.util.HashMap;
import java.util.Map;

public class ModifyUserPickerDataUtility extends ChangeLinkAttributeDataUtility implements ChangeConstants, ModifyConstants {

    private static final Logger LOGGER = LogR.getLogger(ModifyUserPickerDataUtility.class.getName());

    @Override
    public Object getDataValue(String paramString, Object paramObject, ModelContext paramModelContext) throws WTException {
        SessionContext previous = SessionContext.newContext();
        //add by lzy at 20191216 start
        // 获取当前用户
        WTPrincipal principal = SessionHelper.manager.getPrincipal();
        WTUser user=(WTUser) principal;
        String fullName=user.getFullName();
//        System.out.println("fullName=="+fullName);
        //add by lzy at 20191216 end
        // 当前用户设置为管理员，用于忽略权限
        SessionHelper.manager.setAdministrator();
        try {
            NmCommandBean nmCommandBean = paramModelContext.getNmCommandBean();
            boolean bool = CreateAndEditWizBean.isCreateEditWizard(nmCommandBean);//是否创建及编辑状态
            if (bool) {
                Object actionObject = nmCommandBean.getActionOid().getRefObject();
                LOGGER.info("=====actionObject: " + actionObject);
                String ecnVid = String.valueOf(PICoreHelper.service.getBranchId(actionObject));
                String branchId = String.valueOf(PICoreHelper.service.getBranchId(paramObject));
                LOGGER.info("=====ecnVid: " + ecnVid + " >>>>>branchId: " + branchId);
                CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, branchId, LINKTYPE_1);
                LOGGER.info("=====link: " + link);
                boolean flag = false;
                if (link != null) flag = ROUTING_1.equals(link.getRouting()) || ROUTING_3.equals(link.getRouting());
                LOGGER.info("=====flag: " + flag);

//                add by lzy at 20191216 start
//                PickerInputComponent userPicker = createPickerComponent(paramString, paramObject, paramModelContext, bool);

                //add by lzy at 20200316 start
                PickerInputComponent userPicker;
                HashMap<String, Object> parameterMap = nmCommandBean.getParameterMap();
                Object userPickerObj = parameterMap.get("userPicker");
                String responsible= (String) userPickerObj;//责任人
                if (!flag&&responsible!=null&&!responsible.isEmpty()){
                    userPicker = createPickerUserComponent(paramString, paramObject, paramModelContext, bool,"",responsible);
                }else{
                    userPicker = createPickerUserComponent(paramString, paramObject, paramModelContext, bool,fullName,"");
                }
                //add by lzy at 20200316 end
//                PickerInputComponent userPicker = createPickerUserComponent(paramString, paramObject, paramModelContext, bool,fullName);
//                add by lzy at 20191216 end
                if (flag) userPicker.setEditable(false);
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

    public PickerInputComponent createPickerComponent(String param, Object object, ModelContext modelContext, Boolean isCreateEdit) throws WTException {
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
     * @param param
     * @param modelContext
     * @param value
     * @throws WTException
     */
    public void setPickerConfig(String param, ModelContext modelContext, Object value) throws WTException {
        // 获取参数列表
        Map<Object, Object> propertiesMap = modelContext.getDescriptor().getProperties();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Befor propertiesMap : " + propertiesMap);
        }
        // 添加Picker配置
        UserPickerConfig.setPickerProperties(param, modelContext.getNmCommandBean(), propertiesMap);
        // 添加默认值
        propertiesMap.put(PickerRenderConfigs.DEFAULT_HIDDEN_VALUE, value == null ? "" : (String) value);
        propertiesMap.put(PickerRenderConfigs.DEFAULT_VALUE, value == null ? "" : (String) value);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("After propertiesMap : " + propertiesMap);
        }
    }

    /***
     * 获取默认值
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


    /**
     * 传当前用户名
     * @param param
     * @param object
     * @param modelContext
     * @param isCreateEdit
     * @param fullName
     * @return
     * @throws WTException
     */
    public PickerInputComponent createPickerUserComponent(String param, Object object, ModelContext modelContext, Boolean isCreateEdit,String fullName,String responsible) throws WTException {
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
            setPickerUserConfig(param, modelContext, dataUtility.getValue(modelContext, object, isCreateEdit, param),fullName,responsible);
        }

        ComponentDescriptor componentDescriptor = modelContext.getDescriptor();
        Map<Object, Object> propertiesMap = componentDescriptor.getProperties();
        String defaultValue = (String) propertiesMap.get(PickerRenderConfigs.DEFAULT_HIDDEN_VALUE);

//        //add by lzy at 20200316 start
//        if (fullName!=null&&!fullName.isEmpty()){
//            PickerInputComponent component = new PickerInputComponent(label, fullName, PickerRenderConfigs.getPickerConfigs(propertiesMap));
//            component.setColumnName(AttributeDataUtilityHelper.getColumnName(param, object, modelContext));
//            component.setRequired(true);
//
//            return component;
//        }else{
//            PickerInputComponent component = new PickerInputComponent(label, defaultValue, PickerRenderConfigs.getPickerConfigs(propertiesMap));
//            component.setColumnName(AttributeDataUtilityHelper.getColumnName(param, object, modelContext));
//            component.setRequired(true);
//
//            return component;
//        }
        //add by lzy at 20200316 end
        PickerInputComponent component = new PickerInputComponent(label, defaultValue, PickerRenderConfigs.getPickerConfigs(propertiesMap));

        component.setColumnName(AttributeDataUtilityHelper.getColumnName(param, object, modelContext));
        component.setRequired(true);

        return component;
    }

    /***
     * 初始化Picker数据(传当前用户名)
     * @param param
     * @param modelContext
     * @param value
     * @param fullName
     * @throws WTException
     */
    //add by lzy at 20191216
    public void setPickerUserConfig(String param, ModelContext modelContext, Object value,String fullName,String responsible) throws WTException {
        // 获取参数列表
        Map<Object, Object> propertiesMap = modelContext.getDescriptor().getProperties();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Befor propertiesMap : " + propertiesMap);
        }
        // 添加Picker配置
        UserPickerConfig.setPickerProperties(param, modelContext.getNmCommandBean(), propertiesMap);

        //add by lzy at 20200316 start
        Boolean a=responsible!=null&&!responsible.trim().isEmpty();
        Boolean b=responsible!=null&&!responsible.isEmpty();
        if (responsible!=null&&!responsible.trim().isEmpty()){
            propertiesMap.put(PickerRenderConfigs.DEFAULT_HIDDEN_VALUE, responsible == null ? "" : responsible);
            propertiesMap.put(PickerRenderConfigs.DEFAULT_VALUE, responsible == null ? "" : responsible);
        }else{
            propertiesMap.put(PickerRenderConfigs.DEFAULT_HIDDEN_VALUE, value == null ? fullName : (String) value);
            propertiesMap.put(PickerRenderConfigs.DEFAULT_VALUE, value == null ? fullName : (String) value);
        }
        //add by lzy at 20200316 end

//        propertiesMap.put(PickerRenderConfigs.DEFAULT_HIDDEN_VALUE, value == null ? fullName : (String) value);
//        propertiesMap.put(PickerRenderConfigs.DEFAULT_VALUE, value == null ? fullName : (String) value);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("After propertiesMap : " + propertiesMap);
        }
    }

}
