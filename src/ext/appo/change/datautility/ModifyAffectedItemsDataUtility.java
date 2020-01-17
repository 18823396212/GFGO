package ext.appo.change.datautility;

import com.ptc.core.components.beans.CreateAndEditWizBean;
import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.dataUtilities.AttributeDataUtilityHelper;
import com.ptc.core.components.rendering.guicomponents.*;
import com.ptc.core.components.rendering.guicomponents.DateInputComponent.UI;
import com.ptc.core.lwc.common.view.EnumerationDefinitionReadView;
import com.ptc.core.lwc.common.view.EnumerationMembershipReadView;
import com.ptc.core.lwc.common.view.PropertyHolderHelper;
import com.ptc.core.lwc.common.view.PropertyValueReadView;
import com.ptc.core.lwc.server.TypeDefinitionServiceHelper;
import com.ptc.netmarkets.util.beans.HTTPRequestData;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.windchill.enterprise.change2.dataUtilities.ChangeLinkAttributeDataUtility;
import com.ptc.windchill.enterprise.changeable.ChangeableObjectBean;
import ext.appo.change.ModifyHelper;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.models.CorrelationObjectLink;
import ext.appo.change.util.AffectedObjectUtil;
import ext.appo.ecn.common.util.ChangePartQueryUtils;
import ext.appo.ecn.common.util.ChangeUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.ecn.util.AffectedItemsUtil;
import ext.appo.util.StringUtil;
import ext.generic.borrow.common.BorrowOrderConstants;
import ext.generic.integration.erp.bean.InventoryPrice;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.generic.integration.erp.ibatis.InventoryPriceIbatis;
import ext.lang.PIStringUtils;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PICoreHelper;
import ext.pi.core.PIWorkflowHelper;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import wt.change2.*;
import wt.enterprise.RevisionControlled;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.lifecycle.LifeCycleManaged;
import wt.log4j.LogR;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WorkItem;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ModifyAffectedItemsDataUtility extends ChangeLinkAttributeDataUtility implements ChangeConstants, ModifyConstants {

    private static final Logger LOGGER = LogR.getLogger(ModifyAffectedItemsDataUtility.class.getName());

    @Override
    public Object getDataValue(String paramString, Object paramObject, ModelContext paramModelContext) throws WTException {
        SessionContext previous = SessionContext.newContext();
        // 当前用户设置为管理员，用于忽略权限
        SessionHelper.manager.setAdministrator();
        try {
            NmCommandBean nmCommandBean = paramModelContext.getNmCommandBean();
            boolean bool = CreateAndEditWizBean.isCreateEditWizard(nmCommandBean);//是否创建及编辑状态
            LOGGER.info("=====bool: " + bool);
            Object actionObject = nmCommandBean.getActionOid().getRefObject();
            LOGGER.info("=====actionObject: " + actionObject);
            if (actionObject instanceof WorkItem) {
                //在流程中
                WorkItem workItem = (WorkItem) actionObject;
                WfProcess wfprocess = PIWorkflowHelper.service.getParentProcess(workItem);
                Object[] objects = wfprocess.getContext().getObjects();
                if (objects.length > 0) {
                    objects:
                    for (int i = 0; i < objects.length; i++) {
                        if (objects[i] instanceof WTChangeActivity2) { //eca
                            WTChangeActivity2 eca = (WTChangeActivity2) objects[i];
                            QueryResult ecaqr = ChangeHelper2.service.getChangeOrder(eca);
                            while (ecaqr.hasMoreElements()) {
                                Object ecn = ecaqr.nextElement();
                                if (ecn instanceof WTChangeOrder2) {
                                    actionObject = (WTChangeOrder2) ecn;
                                    break objects;
                                }
                            }

                        }
                    }
                }
            }
            String ecnVid = String.valueOf(PICoreHelper.service.getBranchId(actionObject));
            String branchId = String.valueOf(PICoreHelper.service.getBranchId(paramObject));
            LOGGER.info("=====ecnVid: " + ecnVid + " >>>>>branchId: " + branchId);
            CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, branchId, LINKTYPE_1);
            LOGGER.info("=====link: " + link);
            if (bool) {
                boolean flag = false;
                if (link != null) flag = ROUTING_1.equals(link.getRouting()) || ROUTING_3.equals(link.getRouting());
                LOGGER.info("=====flag: " + flag);
                if (paramString.contains(ARTICLEINVENTORY_COMPID) || paramString.contains(CENTRALWAREHOUSEINVENTORY_COMPID) || paramString.contains(PASSAGEINVENTORY_COMPID)) {
                    if (paramObject instanceof WTPart) {
                        GUIComponentArray gui_array = new GUIComponentArray();
                        gui_array.addGUIComponent(generateTextDisplayComponent(paramModelContext, paramObject, paramString, getValue(paramModelContext, paramObject, bool, paramString)));
//                        gui_array.addGUIComponent(generateTextDisplayComponent(paramModelContext, paramObject, paramString, null));
                        return gui_array;
                    }
                } else if (paramString.contains(ARTICLEDISPOSE_COMPID) || paramString.contains(INVENTORYDISPOSE_COMPID) || paramString.contains(PASSAGEDISPOSE_COMPID) || paramString.contains(PRODUCTDISPOSE_COMPID) || paramString.contains(CHANGETYPE_COMPID) || paramString.contains(ATTRIBUTE_7)) {
                    if (paramObject instanceof WTPart) {
                        GUIComponentArray gui_array = new GUIComponentArray();
                        ComboBox comboBox = generateComboBox(paramModelContext, paramObject, paramString, getValue(paramModelContext, paramObject, bool, paramString));
                        if (flag) comboBox.setEditable(false);
                        gui_array.addGUIComponent(comboBox);
                        return gui_array;
                    } else {
                        return "";//非部件不显示「变更对象类型」的值
                    }
                } else if (paramString.contains(COMPLETIONTIME_COMPID)) {
                    GUIComponentArray gui_array = new GUIComponentArray();
                    DateInputComponent dateInputComponent = generateDateInputComponent(paramModelContext, paramObject, paramString, getValue(paramModelContext, paramObject, bool, paramString));
                    if (flag) dateInputComponent.setEditable(false);
                    gui_array.addGUIComponent(dateInputComponent);
                    return gui_array;
                } else if (paramString.contains(AADDESCRIPTION_COMPID)) {
                    GUIComponentArray gui_array = new GUIComponentArray();
                    TextBox textBox = generateTextBox(paramModelContext, paramObject, paramString, getValue(paramModelContext, paramObject, bool, paramString));
                    if (flag) textBox.setEditable(false);
                    gui_array.addGUIComponent(textBox);
                    return gui_array;
                } else if (paramString.contains(ATTRIBUTE_9)) {
                    GUIComponentArray gui_array = new GUIComponentArray();
                    ComboBox comboBox = generateComboBox(paramModelContext, paramObject, paramString, getValue(paramModelContext, paramObject, bool, paramString));
                    gui_array.addGUIComponent(comboBox);
                    return gui_array;
                } else if (paramString.contains(ATTRIBUTE_10)) {
                    GUIComponentArray gui_array = new GUIComponentArray();
                    TextBox textBox = generateTextBox(paramModelContext, paramObject, paramString, getValue(paramModelContext, paramObject, bool, paramString));
                    gui_array.addGUIComponent(textBox);
                    return gui_array;
                } else if (paramString.contains(ATTRIBUTE_12)) {
                    Object value = null;
                    HashMap<String, Object> parameterMap = nmCommandBean.getParameterMap();
                    Object object = parameterMap.get("collectionObjectMap");
                    if (object instanceof Map) {
                        Map map = (Map) object;
                        value = map.get(PICoreHelper.service.getOid(paramObject));
                        if (null == value) {
                            AffectedObjectUtil affectedObjectUtil = new AffectedObjectUtil(nmCommandBean, null);
                            Map<Persistable, Map<String, String>> pageDataMap = affectedObjectUtil.PAGEDATAMAP;
                            Map<String, String> attributeMap = pageDataMap.get(paramObject);
                            value = attributeMap == null ? null : attributeMap.get(ATTRIBUTE_12);
                        }
                        if (null == value) if (link != null) value = link.getCollection();
                    }
                    GUIComponentArray gui_array = new GUIComponentArray();
                    TextBox textBox = generateTextBox(paramModelContext, paramObject, paramString, value);
                    //add by xiebowen at 2020/1/8  start
                    if (flag){
                        //路由已创建或已完成不能编辑
                        textBox.setEditable(false);
                    }else{
                        //其他路由有值不能编辑，没值可编辑
                        if (value!=null){
                            textBox.setEditable(false);
                        }
                    }

//                    HTTPRequestData requestData = nmCommandBean.getRequestData();
//                    HashMap<String, Object> parameterMap1 = requestData.getParameterMap();
//                    Object changeMode = parameterMap1.get("changeMode");
//                    String mode = "";
//                    if (changeMode instanceof String){
//                        mode = (String)changeMode;
//                    }else if (changeMode instanceof String[]){
//                        mode = ((String[])changeMode)[0];
//                    }
//                    if ("EDIT".equalsIgnoreCase(mode)){
//                        if (value==null){
//                            textBox.setEditable(false);
//                        }
//                    }else {
//                        if (value!=null){
//                            textBox.setEditable(false);
//                        }
//                    }
                    //add by xiebowen at 2020/1/8  end
                    textBox.setWidth(50);
                    textBox.setRequired(false);
                    gui_array.addGUIComponent(textBox);
                    return gui_array;
                } else {
                    if (paramObject instanceof WTPart) {
                        GUIComponentArray gui_array = new GUIComponentArray();
                        gui_array.addGUIComponent(generateTextDisplayComponent(paramModelContext, paramObject, paramString, getValue(paramModelContext, paramObject, bool, paramString)));
                        return gui_array;
                    }
                }
            } else {
                if (paramString.contains(ATTRIBUTE_12)) {
                    GUIComponentArray gui_array = new GUIComponentArray();
                    gui_array.addGUIComponent(generateTextDisplayComponent(paramModelContext, paramObject, paramString, link.getCollection()));
                    return gui_array;
                }
            }

            GUIComponentArray gui_array = new GUIComponentArray();
            gui_array.addGUIComponent(generateTextDisplayComponent(paramModelContext, paramObject, paramString, getValue(paramModelContext, paramObject, false, paramString)));
            return gui_array;
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        } finally {
            SessionContext.setContext(previous);
        }
    }

    /***
     * 构建文档显示框
     * @param paramModelContext
     * @param paramObject
     *            表单行对象
     * @param keyStr
     *            表单列名称
     * @param value
     *            填充值
     * @return
     * @throws WTException
     */
    public TextDisplayComponent generateTextDisplayComponent(ModelContext paramModelContext, Object paramObject, String keyStr, Object value) throws WTException {
        TextDisplayComponent gui = new TextDisplayComponent(keyStr);
        gui.setId(keyStr);
        gui.setValue(value == null ? "" : (String) value);
        gui.setName(AttributeDataUtilityHelper.getColumnName(keyStr, paramObject, paramModelContext));
        return gui;
    }

    /***
     * 构建文本输入框
     * @param paramModelContext
     * @param paramObject
     *            表单行对象
     * @param keyStr
     *            表单列名称
     * @param value
     *            输入框填充值
     * @return
     * @throws WTException
     */
    public TextBox generateTextBox(ModelContext paramModelContext, Object paramObject, String keyStr, Object value) throws WTException {
        TextBox textBox = new TextBox();
        textBox.setWidth(100);
        if (keyStr.contains(ATTRIBUTE_10)) {

        } else {
            textBox.addJsAction("onChange", "delaySaveChangeTaskArray();");
        }
        textBox.setEnabled(true);
        textBox.setRequired(true);
        textBox.setId(keyStr);
        textBox.setValue(value == null ? "" : (String) value);
        textBox.setColumnName(AttributeDataUtilityHelper.getColumnName(keyStr, paramObject, paramModelContext));
        return textBox;
    }

    /***
     * 构建下拉输入框
     * @param paramModelContext
     * @param paramObject
     *            表单行对象
     * @param keyStr
     *            表单列名称
     * @param value
     *            输入框填充值
     * @return
     * @throws WTException
     */
    public ComboBox generateComboBox(ModelContext paramModelContext, Object paramObject, String keyStr, Object value) throws WTException {
        // 枚举定义
        Map<ArrayList<String>, ArrayList<String>> enumMap;
        if (keyStr.contains(CHANGETYPE_COMPID)) {
            enumMap = getEnumeratedMap(CHANGETYPE_COMPID);
        } else if (keyStr.contains(PRODUCTDISPOSE_COMPID)) {
            enumMap = getEnumeratedMap(PRODUCTDISPOSE_COMPID);
        }
        //add by tongwang 20191023 start
        else if (keyStr.contains(ATTRIBUTE_7)) {
            enumMap = getEnumeratedMap(ATTRIBUTE_7);
        }
        else if (keyStr.contains(ATTRIBUTE_9)) {
            enumMap = getEnumeratedMap(ATTRIBUTE_9);
        }
        //add by tongwang 20191023 end
        else {
            enumMap = getEnumeratedMap(ARTICLEDISPOSE_COMPID);
        }
        // 内部名称
        ArrayList<String> keyArray = enumMap.keySet().iterator().next();
        // 显示名称
        ArrayList<String> displayArray = enumMap.get(keyArray);
        // 选取值
        ArrayList<String> selectArray = new ArrayList<>();
        if (value != null && value != "") {
            selectArray.add(value == null ? "" : (String) value);
        } else {
            if (keyArray.size() > 0) {
                selectArray.add(keyArray.get(0));
            }
        }
        ComboBox box = new ComboBox(keyArray, displayArray, selectArray);
        box.setId(keyStr);
        box.setColumnName(AttributeDataUtilityHelper.getColumnName(keyStr, paramObject, paramModelContext));
        box.setMultiSelect(false);
        if (keyStr.contains(ATTRIBUTE_9)) {

        } else {
            box.addJsAction("onChange", "delaySaveChangeTaskArray();");
        }
        return box;
    }

    /***
     * 构建日期输入框
     * @param paramModelContext
     * @param paramObject
     *            表单行对象
     * @param keyStr
     *            表单列名称
     * @param value
     *            输入框填充值
     * @return
     * @throws WTException
     */
    public DateInputComponent generateDateInputComponent(ModelContext paramModelContext, Object paramObject, String keyStr, Object value) throws WTException {
        DateInputComponent component = new DateInputComponent(keyStr, DateInputComponent.ValueType.DATE_ONLY);
        component.setColumnName(AttributeDataUtilityHelper.getColumnName(keyStr, paramObject, paramModelContext));
        try {
            DateFormat format = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
            if (value != null) {
                String valueStr = (String) value;
                if (valueStr.contains(" ")) {
                    valueStr = valueStr.substring(0, valueStr.indexOf(" ")).trim();
                }
                if (valueStr.contains("-")) {
                    valueStr = (new SimpleDateFormat(SIMPLE_DATE_FORMAT_02)).format((new SimpleDateFormat(SIMPLE_DATE_FORMAT_03)).parse(valueStr));
                }
                component.setValue(Timestamp.valueOf((new SimpleDateFormat(SIMPLE_DATE_FORMAT)).format((new SimpleDateFormat(SIMPLE_DATE_FORMAT_02)).parse(valueStr))));
            } else {
                //add by lzuy at 20191216 start
//                component.setValue(Timestamp.valueOf(format.format(new Date())));
                //期望完成时间默认推迟一周
                Calendar curr = Calendar.getInstance();
                curr.set(Calendar.DAY_OF_MONTH, curr.get(Calendar.DAY_OF_MONTH) + 7);
                Date date = curr.getTime();
                component.setValue(Timestamp.valueOf(format.format(date)));
                //add by lzuy at 20191216 end
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getStackTrace());
        }
        component.setEditable(true);
        component.setRequired(true);
        component.addJsAction("onChange", "delaySaveChangeTaskArray() ;", UI.HOUR_UI);
        component.addJsAction("onChange", "delaySaveChangeTaskArray() ;", UI.MINUTE_UI);
        component.addJsAction("onChange", "delaySaveChangeTaskArray() ;", UI.DATE_UI);
        return component;
    }

    /***
     * 获取每一列的属性值
     * @param paramModelContext
     * @param paramObject
     *            表单行对象
     * @param isCreateEdit
     *            是否为创建编辑状态
     * @param compid
     *            表单列名称
     * @return
     * @throws WTException
     */
    public Object getValue(ModelContext paramModelContext, Object paramObject, Boolean isCreateEdit, String compid) throws WTException {
        try {
            /* 受影响对象 表单数据处理 */
            if (isCreateEdit) {
                if (compid.contains(ARTICLEINVENTORY_COMPID) || compid.contains(CENTRALWAREHOUSEINVENTORY_COMPID) || compid.contains(PASSAGEINVENTORY_COMPID)) {
                    // 对象为部件时查询
                    if (!(paramObject instanceof WTPart)) {
                        return "";
                    }
                    WTPart part = (WTPart) paramObject;

                    // 获取IP地址
                    String ip = InetAddress.getLocalHost().getHostAddress();
                    System.out.println("IP地址：" + ip);
                    // 区别光峰61，70,71服务器，绎立服务器，光峰采用发K3方法，其余采用发中间表方法
                    String mVersion = part.getVersionIdentifier().getValue();// 物料大版本
                    if (ip.equals("172.32.252.61") || ip.equals("172.32.252.70") || ip.equals("172.32.252.71")) {
                        // 查询数据库表 EXT_STOCK_TAB
                        // WTPARTNUMBER 物料编码,VERSIONINFO 大版本,ZZNUM 在制数量,KCNUM
                        // 库存数量,ZTNUM 在途数量
                        List rList = AffectedItemsUtil.queryAmount(part.getNumber(), mVersion);
                        if (rList != null && rList.size() > 0) {
                            System.out.println("查询数据库表 EXT_STOCK_TAB下物料==" + part.getNumber() + "==大版本==" + mVersion + "==在制数量==" + rList.get(0) + "==库存数量==" + rList.get(1) + "==在途数量==" + rList.get(2));
                            if (compid.contains(ARTICLEINVENTORY_COMPID)) {
                                return rList.get(0) == null ? "" : rList.get(0);// 在制数量
                            } else if (compid.contains(CENTRALWAREHOUSEINVENTORY_COMPID)) {
                                return rList.get(1) == null ? "" : rList.get(1);// 库存数量
                            } else if (compid.contains(PASSAGEINVENTORY_COMPID)) {
                                return rList.get(2) == null ? "" : rList.get(2);// 在途数量
                            }
                        } else {
                            if (compid.contains(ARTICLEINVENTORY_COMPID)) {
                                return "";
                            } else if (compid.contains(CENTRALWAREHOUSEINVENTORY_COMPID)) {
                                return "";
                            } else if (compid.contains(PASSAGEINVENTORY_COMPID)) {
                                return "";
                            }
                        }
                    } else {
                        System.out.println("ECN在制，库存，在途调用中间表接口==");
                        List<?> datasList = InventoryPriceIbatis.queryInventoryPrice(part.getNumber(), "total", (WTUser) SessionHelper.getPrincipal());
                        for (Object inventoryPriceObject : datasList) {
                            InventoryPrice inventoryPrice = (InventoryPrice) inventoryPriceObject;
                            if (compid.contains(ARTICLEINVENTORY_COMPID)) {
                                return inventoryPrice.getfInquantity();
                            } else if (compid.contains(CENTRALWAREHOUSEINVENTORY_COMPID)) {
                                return inventoryPrice.getiQuantity();
                            } else if (compid.contains(PASSAGEINVENTORY_COMPID)) {
                                return inventoryPrice.getfTransinquantity();
                            }
                        }
                    }
                }
            }

            Object value = null;
            /* 受影响产品 表单数据处理 */
            if (compid.contains("childNumber")) {
                if (paramObject instanceof WTPart) {
                    return getChildsNumber(paramModelContext, (WTPart) paramObject);
                }
                return value;
            } else if (compid.equals("ggms") || compid.contains("sscpx") || compid.contains("ssxm") || compid.contains("xsxh") || compid.contains("cpzt") || compid.contains("nbxh")) {
                if (paramObject instanceof WTPart) {
                    return PIAttributeHelper.service.getDisplayValue((WTPart) paramObject, compid, Locale.CHINA);
                }
            }

            // 受影响对象列表中的‘收集对象’处理
            if (compid.contains("CollectionNumber")) {
                if (paramObject instanceof WTPart) {
                    return getChildsNumber(paramModelContext, (WTPart) paramObject);
                }
                return value;
            }

            /* 受影响对象 表单数据处理 */
            if (!isCreateEdit) {
                if (compid.equals(BorrowOrderConstants.VERSION)) {
                    if (paramObject instanceof WTPart) {
                        WTPart part = (WTPart) paramObject;
                        return CommonPDMUtil.getVersion(part) + "(" + part.getViewName() + ")";
                    } else if (paramObject instanceof RevisionControlled) {
                        return CommonPDMUtil.getVersion((RevisionControlled) paramObject);
                    }
                } else if (compid.equals(BorrowOrderConstants.STATE)) {
                    return CommonPDMUtil.getLifecycleCN((LifeCycleManaged) paramObject);
                } else {
                    value = paramModelContext.getRawValue();
                }
            } else {
                value = getValue(paramModelContext, paramObject, compid);
            }
            if (value == null) {
                if (compid.contains(AADDESCRIPTION_COMPID) || compid.contains(CRDESCRIPTION_COMPID)) {
                    Object modelObject = paramModelContext.getModelObject();
                    if (modelObject instanceof ChangeableObjectBean) {
                        ChangeableObjectBean localChangeableObjectBean = (ChangeableObjectBean) modelObject;
                        // 获取受影响的或产生的活动对象
                        if (localChangeableObjectBean.getRelatedProductData() != null) {
                            RelatedProductData relatedProductData = localChangeableObjectBean.getRelatedProductData();
                            // 获取备注
                            return relatedProductData.getDescription();
                        }
                    } else if (modelObject instanceof Changeable2) {
                        //add by tongwang 20191023 start
                        Changeable2 changeable2 = (Changeable2) modelObject;
                        Object object = paramModelContext.getNmCommandBean().getActionOid().getRefObject();
                        if (object instanceof WTChangeOrder2) {
                            WTChangeOrder2 changeOrder2 = (WTChangeOrder2) object;
                            Collection<ChangeActivityIfc> changeActivities = ChangeUtils.getChangeActivities(changeOrder2);
                            for (ChangeActivityIfc changeActivityIfc : changeActivities) {
                                AffectedActivityData affectedActivityData = ChangeUtils.getAffectedActivity(changeActivityIfc, changeable2);
                                if (affectedActivityData != null) {
                                    return affectedActivityData.getDescription();
                                }
                            }
                            //从Link上获取属性
                            String ecnVid = String.valueOf(PICoreHelper.service.getBranchId(changeOrder2));
                            String branchId = String.valueOf(PICoreHelper.service.getBranchId(changeable2));
                            CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, branchId, ModifyConstants.LINKTYPE_1);
                            if (link != null) {
                                return link.getAadDescription();
                            }
                        }
                        //add by tongwang 20191023 end
                        //add by lzy at 20200117 start
                        else if (object instanceof WorkItem) {
                            //在流程中
                            WTChangeOrder2 changeOrder2 = new WTChangeOrder2();
                            WorkItem workItem = (WorkItem) object;
                            WfProcess wfprocess = PIWorkflowHelper.service.getParentProcess(workItem);
                            Object[] objects = wfprocess.getContext().getObjects();
                            if (objects.length > 0) {
                                objects:for (int i = 0; i < objects.length; i++) {
                                    if (objects[i] instanceof WTChangeActivity2) { //eca
                                        WTChangeActivity2 eca = (WTChangeActivity2) objects[i];
                                        QueryResult ecaqr = ChangeHelper2.service.getChangeOrder(eca);
                                        while (ecaqr.hasMoreElements()) {
                                            Object ecn = ecaqr.nextElement();
                                            if (ecn instanceof WTChangeOrder2) {
                                                changeOrder2 = (WTChangeOrder2) ecn;
                                                break objects;
                                            }
                                        }

                                    }
                                }
                            }
                            Collection<ChangeActivityIfc> changeActivities = ChangeUtils.getChangeActivities(changeOrder2);
                            for (ChangeActivityIfc changeActivityIfc : changeActivities) {
                                AffectedActivityData affectedActivityData = ChangeUtils.getAffectedActivity(changeActivityIfc, changeable2);
                                if (affectedActivityData != null) {
                                    return affectedActivityData.getDescription();
                                }
                            }
                            //从Link上获取属性
                            String ecnVid = String.valueOf(PICoreHelper.service.getBranchId(changeOrder2));
                            String branchId = String.valueOf(PICoreHelper.service.getBranchId(changeable2));
                            CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, branchId, ModifyConstants.LINKTYPE_1);
                            if (link != null) {
                                return link.getAadDescription();
                            }
                        }
                        //add by lzy at 20200117 end
                    }
                }
            } else {
                if (compid.contains(ARTICLEDISPOSE_COMPID) || compid.contains(INVENTORYDISPOSE_COMPID) || compid.contains(PASSAGEDISPOSE_COMPID) || compid.contains(PRODUCTDISPOSE_COMPID) || compid.contains(CHANGETYPE_COMPID)) {
                    if (!isCreateEdit) {
                        String ibaValue = (String) value;
                        if (ibaValue.contains(USER_KEYWORD)) {
                            ibaValue = ibaValue.substring(ibaValue.lastIndexOf(USER_KEYWORD) + 1);
                        }
                        return ibaValue;
                    }
                }
            }

            return value;
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }
    }

    /***
     * 获取页面已存的数据
     * @param paramModelContext
     * @param paramObject
     * @param compid
     * @return
     * @throws WTException
     */
    public Object getValue(ModelContext paramModelContext, Object paramObject, String compid) throws WTException {
        if (paramModelContext == null || paramObject == null || PIStringUtils.isNull(compid)) {
            return null;
        }

        if (!(paramObject instanceof Persistable)) {
            return null;
        }
        NmCommandBean nmCommandBean = paramModelContext.getNmCommandBean();
        try {
            if (compid.contains(USER_KEYWORD3)) {
                compid = compid.replace(USER_KEYWORD3, "");
            }
            // 添加原有数据
            Map<String, Object> parameterMap = nmCommandBean.getParameterMap();
            if (parameterMap.containsKey(CHANGETASK_ARRAY)) {
                String[] changeTaskArrayStr = (String[]) parameterMap.get(CHANGETASK_ARRAY);
                if (changeTaskArrayStr != null && changeTaskArrayStr.length > 0) {
                    String datasJSON = changeTaskArrayStr[0];
                    if (PIStringUtils.isNotNull(datasJSON)) {
                        JSONArray jsonArray = new JSONArray(datasJSON);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                            // 获取主对象
                            Persistable persistable = (new ReferenceFactory()).getReference(jsonObject.getString(OID_COMPID)).getObject();
                            if (PersistenceHelper.isEquivalent(persistable, (Persistable) paramObject)) {
                                String value = jsonObject.getString(compid);
                                if (PIStringUtils.isNull(value)) {
                                    return null;
                                }
                                return value;
                            }
                        }
                    }
                }
            } else {
                String str = CreateAndEditWizBean.getOperation(nmCommandBean);
                if ((str.equals(CreateAndEditWizBean.EDIT))) {
                    return paramModelContext.getRawValue();
                }
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }
    }

    /***
     * 获取全局枚举定义
     * @param enumName
     *            全局枚举内部名称
     * @return key:枚举定义内部名称 value:枚举定义显示名称
     * @throws WTException
     */
    private Map<ArrayList<String>, ArrayList<String>> getEnumeratedMap(String enumName) throws WTException {
        Map<ArrayList<String>, ArrayList<String>> enumMap = new HashMap<>();
        if (PIStringUtils.isNull(enumName)) {
            return enumMap;
        }

        try {
            // 获取全局枚举定义
            EnumerationDefinitionReadView enumDef = TypeDefinitionServiceHelper.service.getEnumDefView(enumName);
            if (enumDef != null) {
                Collection<EnumerationMembershipReadView> datasArray = enumDef.getAllMemberships();
                if (datasArray != null) {
                    // 存储数据
                    Map<Integer, Map<String, String>> datasMap = new TreeMap<>();
                    for (EnumerationMembershipReadView enumerationMembershipReadView : datasArray) {
                        // 内部名称
                        String interiorName = enumerationMembershipReadView.getName();
                        Collection<PropertyValueReadView> array = enumerationMembershipReadView.getAllProperties();
                        for (PropertyValueReadView propertyValueReadView : array) {
                            Integer index = Integer.parseInt(propertyValueReadView.getValueAsString());
                            // 存储枚举定义
                            Map<String, String> enumInfo = new HashMap<>();
                            enumInfo.put(interiorName, PropertyHolderHelper.getDisplayName(enumerationMembershipReadView.getMember(), SessionHelper.getLocale()));
                            datasMap.put(index, enumInfo);
                        }
                    }
                    // 显示名称
                    ArrayList<String> displayArray = new ArrayList<>();
                    // 内部名称
                    ArrayList<String> interiorArray = new ArrayList<>();
                    for (Map.Entry<Integer, Map<String, String>> entryMap : datasMap.entrySet()) {
                        for (Map.Entry<String, String> enumEntryMap : entryMap.getValue().entrySet()) {
                            interiorArray.add(enumEntryMap.getKey() + USER_KEYWORD + enumEntryMap.getValue());
                            displayArray.add(enumEntryMap.getValue());
                        }
                    }
                    enumMap.put(interiorArray, displayArray);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getStackTrace());
        }

        return enumMap;
    }

    /***
     * 获取存在受影响对象列表的部件编码
     * @param paramModelContext
     * @param parentPart
     * @return
     * @throws WTException
     */
    public String getChildsNumber(ModelContext paramModelContext, WTPart parentPart) throws WTException {
        StringBuilder returnStr = new StringBuilder();
        if (paramModelContext == null || parentPart == null) {
            return returnStr.toString();
        }

        // 查询部件下所有子件对象
        Collection<WTPart> childArray = ChangePartQueryUtils.getPartMultiwallStructure(parentPart);
        if (childArray == null || childArray.size() == 0) {
            return returnStr.toString();
        }

        // 获取页面添加数据
        Map<Persistable, Map<String, String>> pageChangeTaskArray = ChangeUtils.getPageChangeTaskArray(paramModelContext.getNmCommandBean());
        if (pageChangeTaskArray != null && pageChangeTaskArray.size() > 0) {
            for (Map.Entry<Persistable, Map<String, String>> entryMap : pageChangeTaskArray.entrySet()) {
                Persistable persistable = entryMap.getKey();
                if (persistable instanceof WTPart) {
                    for (WTPart childPart : childArray) {
                        if (ChangeUtils.getNumber(persistable).equals(childPart.getNumber())) {
                            if (returnStr.length() > 0) {
                                returnStr.append(USER_KEYWORD);
                            }
                            returnStr.append(childPart.getNumber());
                            break;
                        }
                    }
                }
            }
        } else {
            // 编辑添加数据过滤问题
            Map<String, Object> parameterMap = paramModelContext.getNmCommandBean().getParameterMap();
            if (parameterMap.containsKey(CHANGETASK_ARRAY)) {
                return returnStr.toString();
            }
            Object object = paramModelContext.getNmCommandBean().getActionOid().getRefObject();
            if (object instanceof WTChangeOrder2) {
                // 获取所有受影响对象
                Collection<Changeable2> changeablesBefore = ChangeUtils.getChangeablesBefore((WTChangeOrder2) object);
                for (Changeable2 changeable2 : changeablesBefore) {
                    if (changeable2 instanceof WTPart) {
                        for (WTPart childPart : childArray) {
                            if (ChangeUtils.getNumber(changeable2).equals(childPart.getNumber())) {
                                if (returnStr.length() > 0) {
                                    returnStr.append(USER_KEYWORD);
                                }
                                returnStr.append(childPart.getNumber());
                                break;
                            }
                        }
                    }
                }
            }
            //add by lzy at 20200117 start
            else if (object instanceof WorkItem) {
                WTChangeOrder2 changeOrder2 = new WTChangeOrder2();
                WorkItem workItem = (WorkItem) object;
                WfProcess wfprocess = PIWorkflowHelper.service.getParentProcess(workItem);
                Object[] objects = wfprocess.getContext().getObjects();
                if (objects.length > 0) {
                    objects:for (int i = 0; i < objects.length; i++) {
                        if (objects[i] instanceof WTChangeActivity2) { //eca
                            WTChangeActivity2 eca = (WTChangeActivity2) objects[i];
                            QueryResult ecaqr = ChangeHelper2.service.getChangeOrder(eca);
                            while (ecaqr.hasMoreElements()) {
                                Object ecn = ecaqr.nextElement();
                                if (ecn instanceof WTChangeOrder2) {
                                    changeOrder2 = (WTChangeOrder2) ecn;
                                    break objects;
                                }
                            }

                        }
                    }
                }
                // 获取所有受影响对象
                Collection<Changeable2> changeablesBefore = ChangeUtils.getChangeablesBefore(changeOrder2);
                for (Changeable2 changeable2 : changeablesBefore) {
                    if (changeable2 instanceof WTPart) {
                        for (WTPart childPart : childArray) {
                            if (ChangeUtils.getNumber(changeable2).equals(childPart.getNumber())) {
                                if (returnStr.length() > 0) {
                                    returnStr.append(USER_KEYWORD);
                                }
                                returnStr.append(childPart.getNumber());
                                break;
                            }
                        }
                    }
                }
            }
            //add by lzy at 20200117 end
        }
        return returnStr.toString();
    }
}
