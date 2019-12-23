package ext.appo.change.datautility;

import com.ptc.core.components.beans.CreateAndEditWizBean;
import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.AbstractDataUtility;
import com.ptc.core.components.factory.dataUtilities.AttributeDataUtilityHelper;
import com.ptc.core.components.rendering.guicomponents.*;
import com.ptc.core.components.rendering.guicomponents.DateInputComponent.UI;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.util.ModifyUtils;
import ext.appo.ecn.beans.ChangeTaskBean;
import ext.appo.ecn.constants.ChangeConstants;
import ext.lang.PIStringUtils;
import org.apache.log4j.Logger;
import wt.fc.Persistable;
import wt.log4j.LogR;
import wt.util.WTException;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ModifyChangeTaskDataUtility extends AbstractDataUtility implements ChangeConstants, ModifyConstants {

    private static final Logger LOGGER = LogR.getLogger(ModifyChangeTaskDataUtility.class.getName());

    @Override
    public Object getDataValue(String paramString, Object paramObject, ModelContext paramModelContext) throws WTException {
        NmCommandBean nmCommandBean = paramModelContext.getNmCommandBean();
        boolean bool = CreateAndEditWizBean.isCreateEditWizard(nmCommandBean);//是否创建及编辑状态
        GUIComponentArray gui_array = new GUIComponentArray();
        if (bool) {
            boolean flag = false;
            if (paramObject instanceof ChangeTaskBean) {
                ChangeTaskBean bean = (ChangeTaskBean) paramObject;
                String activity = bean.getChangeActivity2();
                LOGGER.info("=====activity: " + activity);
                Persistable persistable = ModifyUtils.getPersistable(activity);
                LOGGER.info("=====persistable: " + persistable);
                flag = persistable != null;
            }
            LOGGER.info("=====flag: " + flag);

            if (paramString.equalsIgnoreCase(ChangeConstants.NEEDDATE_COMPID)) {
                DateInputComponent dateInputComponent = generateDateInputComponent(paramModelContext, paramObject, paramString);
                if (flag) dateInputComponent.setEditable(false);
                gui_array.addGUIComponent(dateInputComponent);
            } else if (paramString.equalsIgnoreCase(ChangeConstants.RESPONSIBLE_COMPID)) {
                ModifyUserPickerDataUtility userPickerDU = new ModifyUserPickerDataUtility();
                PickerInputComponent userPicker = userPickerDU.createPickerComponent(paramString, paramObject, paramModelContext, bool);
                if (flag) userPicker.setEditable(false);
                return userPicker;
            } else {
                TextBox textBox = generateTextBox(paramModelContext, paramObject, paramString);
                if (flag) textBox.setEditable(false);
                gui_array.addGUIComponent(textBox);
            }
        } else {
            gui_array.addGUIComponent(generateTextDisplayComponent(paramModelContext, paramObject, paramString));
        }
        return gui_array;
    }

    /***
     * 构建文档显示框
     * @param paramModelContext
     * @param paramObject
     *            表单行对象
     * @param keyStr
     *            表单列名称
     * @return
     * @throws WTException
     */
    public TextDisplayComponent generateTextDisplayComponent(ModelContext paramModelContext, Object paramObject, String keyStr) throws WTException {
        TextDisplayComponent gui = new TextDisplayComponent(keyStr);
        try {
            gui.setId(keyStr);
            if (keyStr.equalsIgnoreCase(ChangeConstants.NEEDDATE_COMPID)) {
                String value = paramModelContext.getRawValue() == null ? "" : (String) paramModelContext.getRawValue();
                if (value.contains(" ")) {
                    value = value.substring(0, value.indexOf(" ")).trim();
                }
                if (value.contains("-")) {
                    value = (new SimpleDateFormat(ChangeConstants.SIMPLE_DATE_FORMAT_02)).format((new SimpleDateFormat(ChangeConstants.SIMPLE_DATE_FORMAT_03)).parse(value));
                }
                gui.setValue(value);
            } else {
                gui.setValue(paramModelContext.getRawValue() == null ? "" : (String) paramModelContext.getRawValue());
            }
            gui.setColumnName(AttributeDataUtilityHelper.getColumnName(keyStr, paramObject, paramModelContext));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }
        return gui;
    }

    /***
     * 构建文本输入框
     * @param paramModelContext
     * @param paramObject
     *            表单行对象
     * @param keyStr
     *            表单列名称
     * @return
     * @throws WTException
     */
    public TextBox generateTextBox(ModelContext paramModelContext, Object paramObject, String keyStr) throws WTException {
        TextBox textBox = new TextBox();
        if (keyStr.equalsIgnoreCase(ChangeConstants.CHANGETHEME_COMPID)) {
            textBox.setWidth(40);
        } else if (keyStr.equalsIgnoreCase(ChangeConstants.CHANGEDESCRIBE_COMPID)) {
            textBox.setWidth(100);
        }
        textBox.addJsAction("onChange", "delaySaveDatasArray() ;");
        textBox.setEnabled(true);
        textBox.setRequired(true);
        textBox.setId(keyStr);
        textBox.setValue(paramModelContext.getRawValue() == null ? "" : (String) paramModelContext.getRawValue());
        textBox.setColumnName(AttributeDataUtilityHelper.getColumnName(keyStr, paramObject, paramModelContext));
        return textBox;
    }

    /***
     * 构建日期输入框
     * @param paramModelContext
     * @param paramObject
     *            表单行对象
     * @param keyStr
     *            表单列名称
     * @return
     * @throws WTException
     */
    public DateInputComponent generateDateInputComponent(ModelContext paramModelContext, Object paramObject, String keyStr) throws WTException {
        DateInputComponent component = new DateInputComponent(keyStr, DateInputComponent.ValueType.DATE_ONLY);
        component.setColumnName(AttributeDataUtilityHelper.getColumnName(keyStr, paramObject, paramModelContext));
        try {
            DateFormat format = new SimpleDateFormat(ChangeConstants.SIMPLE_DATE_FORMAT);
            String value = paramModelContext.getRawValue() == null ? "" : (String) paramModelContext.getRawValue();
            if (PIStringUtils.isNotNull(value)) {
                if (value.contains(" ")) {
                    value = value.substring(0, value.indexOf(" ")).trim();
                }
                if (value.contains("-")) {
                    value = (new SimpleDateFormat(ChangeConstants.SIMPLE_DATE_FORMAT_02)).format((new SimpleDateFormat(ChangeConstants.SIMPLE_DATE_FORMAT_03)).parse(value));
                }
                component.setValue(Timestamp.valueOf((new SimpleDateFormat(ChangeConstants.SIMPLE_DATE_FORMAT)).format((new SimpleDateFormat(ChangeConstants.SIMPLE_DATE_FORMAT_02)).parse((String) value))));
            } else {
                component.setValue(Timestamp.valueOf(format.format(new Date())));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }
        component.setEditable(true);
        component.setRequired(true);
        component.addJsAction("onChange", "delaySaveDatasArray() ;", UI.HOUR_UI);
        component.addJsAction("onChange", "delaySaveDatasArray() ;", UI.MINUTE_UI);
        component.addJsAction("onChange", "delaySaveDatasArray() ;", UI.DATE_UI);
        return component;
    }
}
