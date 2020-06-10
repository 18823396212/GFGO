package ext.appo.change.datautility;

import com.ptc.core.components.beans.CreateAndEditWizBean;
import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.AbstractDataUtility;
import com.ptc.core.components.factory.dataUtilities.AttributeDataUtilityHelper;
import com.ptc.core.components.rendering.guicomponents.*;
import com.ptc.core.components.rendering.guicomponents.DateInputComponent.UI;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import ext.appo.change.beans.UsabilityChangeTaskBean;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.util.ModifyUtils;
import ext.appo.ecn.beans.ChangeTaskBean;
import ext.appo.ecn.constants.ChangeConstants;
import ext.lang.PIStringUtils;
import ext.pi.core.PIAttributeHelper;
import org.apache.log4j.Logger;
import wt.change2.WTChangeActivity2;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.log4j.LogR;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfProcess;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ModifyChangeTaskDataUtility extends AbstractDataUtility implements ChangeConstants, ModifyConstants {

    private static final Logger LOGGER = LogR.getLogger(ModifyChangeTaskDataUtility.class.getName());

    @Override
    public Object getDataValue(String paramString, Object paramObject, ModelContext paramModelContext) throws WTException {
        NmCommandBean nmCommandBean = paramModelContext.getNmCommandBean();
        boolean bool = CreateAndEditWizBean.isCreateEditWizard(nmCommandBean);//是否创建及编辑状态
        GUIComponentArray gui_array = new GUIComponentArray();
        boolean flag = false;
        Persistable persistable = null;
        if (paramObject instanceof ChangeTaskBean) {
            ChangeTaskBean bean = (ChangeTaskBean) paramObject;
            String activity = bean.getChangeActivity2();
            LOGGER.info("=====activity: " + activity);
            persistable = ModifyUtils.getPersistable(activity);
            LOGGER.info("=====persistable: " + persistable);
            flag = persistable != null;
        } else if (paramObject instanceof UsabilityChangeTaskBean) {
            UsabilityChangeTaskBean bean = (UsabilityChangeTaskBean) paramObject;
            String activity = bean.getChangeActivity2();
            persistable = ModifyUtils.getPersistable(activity);
            LOGGER.info("=====persistable: " + persistable);
            flag = persistable != null;
        }
        //是否已存在eca
        if (!flag && bool) {
            if (paramString.equalsIgnoreCase(ChangeConstants.NEEDDATE_COMPID)) {
                DateInputComponent dateInputComponent = generateDateInputComponent(paramModelContext, paramObject, paramString);
                if (flag) dateInputComponent.setEditable(false);
                gui_array.addGUIComponent(dateInputComponent);
            } else if (paramString.equalsIgnoreCase(ChangeConstants.RESPONSIBLE_COMPID)) {
                ModifyUserPickerDataUtility userPickerDU = new ModifyUserPickerDataUtility();
                PickerInputComponent userPicker = userPickerDU.createPickerComponent(paramString, paramObject, paramModelContext, flag);
                if (flag) userPicker.setEditable(false);
                return userPicker;
            } else if (paramString.equalsIgnoreCase(ModifyConstants.TASKSTATE_COMPID) || paramString.equalsIgnoreCase(ModifyConstants.TASKNUMBER_COMPID) || paramString.equalsIgnoreCase(ModifyConstants.ACTUALDATE_COMPID)) {
                String value = "";
                TextDisplayComponent gui = new TextDisplayComponent(paramString);
                if (persistable != null && persistable instanceof WTChangeActivity2) {
                    WTChangeActivity2 eca = (WTChangeActivity2) persistable;
                    WfProcess process = null;
                    //查询ECA流程
                    QueryResult qr = WfEngineHelper.service.getAssociatedProcesses(eca, null, null);
                    while (qr.hasMoreElements()) {
                        process = (WfProcess) qr.nextElement();
                    }
                    if (paramString.equalsIgnoreCase(ModifyConstants.TASKNUMBER_COMPID)) {
                        value = eca.getNumber();
                    } else if (paramString.equalsIgnoreCase(ModifyConstants.TASKSTATE_COMPID)) {
                        String state = eca.getState().toString();
                        if (state.equalsIgnoreCase("CANCELLED") || state.equalsIgnoreCase("RESOLVED")) {
                            //已取消，已完成（已关闭）
                            value = TASK_4;
                        } else {
                            value = TASK_2;
                            //是否已超期
                            Object qwwcsj = PIAttributeHelper.service.getValue(eca, ChangeConstants.COMPLETION_TIME);
                            if (qwwcsj != null) {
                                String needDate = (String) qwwcsj;
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                                String dateString = simpleDateFormat.format(new Date());
                                if (compareDate(dateString, needDate)) {
                                    value = TASK_3;
                                }
                            }
                        }
                    } else if (paramString.equalsIgnoreCase(ModifyConstants.ACTUALDATE_COMPID)) {
                        if (process != null && process.getEndTime() != null) {
                            value = process.getEndTime().toLocaleString();
                        }
                    }
                } else {
                    if (paramString.equalsIgnoreCase(ModifyConstants.TASKSTATE_COMPID)) {
                        value = TASK_6;
                    }
                }
                gui.setValue(value);

                gui.setColumnName(AttributeDataUtilityHelper.getColumnName(paramString, paramObject, paramModelContext));
                return gui;
            } else if (paramString.equalsIgnoreCase(ModifyConstants.VIEWINFO_COMPID)) {
                Persistable persistable1 = null;
                if (paramObject instanceof ChangeTaskBean) {
                    ChangeTaskBean bean = (ChangeTaskBean) paramObject;
                    String activity = bean.getChangeActivity2();
                    persistable1 = ModifyUtils.getPersistable(activity);
                } else if (paramObject instanceof UsabilityChangeTaskBean) {
                    UsabilityChangeTaskBean bean = (UsabilityChangeTaskBean) paramObject;
                    String activity = bean.getChangeActivity2();
                    persistable1 = ModifyUtils.getPersistable(activity);
                }
                if (persistable1 != null && persistable1 instanceof WTChangeActivity2) {
                    WTChangeActivity2 eca = (WTChangeActivity2) persistable1;
                    //取路径
                    wt.httpgw.URLFactory urlFactory = new wt.httpgw.URLFactory();
                    String baseUrl = urlFactory.getBaseHREF();
                    //非创建编辑存在eca，任务单号添加链接
                    String url = "javascript:window.open('" + baseUrl + "app/#ptc1/tcomp/infoPage?oid=OR:" + eca + "')";
//                    String test = "<a href=\"javascript:window.open('" + baseUrl + "app/#ptc1/tcomp/infoPage?oid=OR:" + eca + "')\">" + eca.getNumber() + "</a>";
                    String ImgUrl = "netmarkets/images/details.gif";
                    IconComponent icon = new IconComponent(ImgUrl);
                    icon.setId(paramString + "_icon");
                    icon.addJsAction("onclick", url);
                    return icon;
                } else {
                    return "";
                }
            } else {
                TextBox textBox = generateTextBox(paramModelContext, paramObject, paramString);
                if (flag) textBox.setEditable(false);
                gui_array.addGUIComponent(textBox);
            }
        } else {
            if (paramString.equalsIgnoreCase(ModifyConstants.VIEWINFO_COMPID)) {
                Persistable persistable1 = null;
                if (paramObject instanceof ChangeTaskBean) {
                    ChangeTaskBean bean = (ChangeTaskBean) paramObject;
                    String activity = bean.getChangeActivity2();
                    persistable1 = ModifyUtils.getPersistable(activity);
                } else if (paramObject instanceof UsabilityChangeTaskBean) {
                    UsabilityChangeTaskBean bean = (UsabilityChangeTaskBean) paramObject;
                    String activity = bean.getChangeActivity2();
                    persistable1 = ModifyUtils.getPersistable(activity);
                }
                if (persistable1 != null && persistable1 instanceof WTChangeActivity2) {
                    WTChangeActivity2 eca = (WTChangeActivity2) persistable1;
                    //取路径
                    wt.httpgw.URLFactory urlFactory = new wt.httpgw.URLFactory();
                    String baseUrl = urlFactory.getBaseHREF();
                    //非创建编辑存在eca，任务单号添加链接
                    String url = "javascript:window.open('" + baseUrl + "app/#ptc1/tcomp/infoPage?oid=OR:" + eca + "')";
                    String ImgUrl = "netmarkets/images/details.gif";
                    IconComponent icon = new IconComponent(ImgUrl);
                    icon.setId(paramString + "_icon");
                    icon.addJsAction("onclick", url);
//                        String aa="<a href=\"javascript:window.open('"+baseUrl+"app/#ptc1/tcomp/infoPage?oid=OR:" + eca + "')\">" + eca.getNumber() + "</a>";
                    return icon;
                } else {
                    return "";
                }
            }
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
    public TextDisplayComponent generateTextDisplayComponent(ModelContext paramModelContext, Object
            paramObject, String keyStr) throws WTException {
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
            } else if (keyStr.equalsIgnoreCase(ModifyConstants.TASKSTATE_COMPID)) {
                Persistable persistable = null;
                String value = "";
                if (paramObject instanceof ChangeTaskBean) {
                    ChangeTaskBean bean = (ChangeTaskBean) paramObject;
                    String activity = bean.getChangeActivity2();
                    persistable = ModifyUtils.getPersistable(activity);
                } else if (paramObject instanceof UsabilityChangeTaskBean) {
                    UsabilityChangeTaskBean bean = (UsabilityChangeTaskBean) paramObject;
                    String activity = bean.getChangeActivity2();
                    persistable = ModifyUtils.getPersistable(activity);
                }
                if (persistable != null && persistable instanceof WTChangeActivity2) {
                    WTChangeActivity2 eca = (WTChangeActivity2) persistable;
                    String state = eca.getState().toString();
                    if (state.equalsIgnoreCase("CANCELLED") || state.equalsIgnoreCase("RESOLVED")) {
                        //已取消，已完成（已关闭）
                        value = TASK_4;
                    } else {
                        value = TASK_2;
                        //是否已超期
                        Object qwwcsj = PIAttributeHelper.service.getValue(eca, ChangeConstants.COMPLETION_TIME);
                        if (qwwcsj != null) {
                            String needDate = (String) qwwcsj;
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                            String dateString = simpleDateFormat.format(new Date());
                            if (compareDate(dateString, needDate)) {
                                value = TASK_3;
                            }
                        }
                    }
                } else {
                    value = TASK_6;
                }
                gui.setValue(value);
            } else if (keyStr.equalsIgnoreCase(ModifyConstants.TASKNUMBER_COMPID)) {
                Persistable persistable = null;
                String value = "";
                if (paramObject instanceof ChangeTaskBean) {
                    ChangeTaskBean bean = (ChangeTaskBean) paramObject;
                    String activity = bean.getChangeActivity2();
                    persistable = ModifyUtils.getPersistable(activity);
                } else if (paramObject instanceof UsabilityChangeTaskBean) {
                    UsabilityChangeTaskBean bean = (UsabilityChangeTaskBean) paramObject;
                    String activity = bean.getChangeActivity2();
                    persistable = ModifyUtils.getPersistable(activity);
                }
                if (persistable != null && persistable instanceof WTChangeActivity2) {
                    WTChangeActivity2 eca = (WTChangeActivity2) persistable;
                    value = eca.getNumber();
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
    public TextBox generateTextBox(ModelContext paramModelContext, Object paramObject, String keyStr) throws
            WTException {
        TextBox textBox = new TextBox();
        if (keyStr.equalsIgnoreCase(ChangeConstants.CHANGETHEME_COMPID)) {
            textBox.setWidth(40);
        } else if (keyStr.equalsIgnoreCase(ChangeConstants.CHANGEDESCRIBE_COMPID)) {
            textBox.setWidth(100);
        }
        textBox.addJsAction("onChange", "delaySaveDatasArray() ;");
        textBox.setEnabled(true);
        if (keyStr.equalsIgnoreCase(ModifyConstants.GLFS_COMPID)) {
            textBox.setWidth(60);
            textBox.setRequired(false);
        } else {
            textBox.setRequired(true);
        }
        if (keyStr.equalsIgnoreCase(ModifyConstants.TASKTYPE_COMPID)) {
            textBox.setWidth(40);
        }
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
    public DateInputComponent generateDateInputComponent(ModelContext paramModelContext, Object paramObject, String
            keyStr) throws WTException {
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

    // 比较时间,date1大于等于date2返回true！
    //String必须为"yyyy/MM/dd"格式
    public static boolean compareDate(String date1, String date2) {
        boolean flag = false;
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd");

        try {
            Date dt1 = df.parse(date1);
            Date dt2 = df.parse(date2);
            if (dt1.getTime() > dt2.getTime()) {
                flag = true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return flag;
    }

}
