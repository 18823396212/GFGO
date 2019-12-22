package ext.appo.change.datautility;

import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.dataUtilities.AttributeDataUtilityHelper;
import com.ptc.core.components.rendering.guicomponents.*;
import com.ptc.core.components.rendering.guicomponents.DateInputComponent.UI;
import com.ptc.core.lwc.common.view.EnumerationDefinitionReadView;
import com.ptc.core.lwc.common.view.EnumerationMembershipReadView;
import com.ptc.core.lwc.common.view.PropertyHolderHelper;
import com.ptc.core.lwc.common.view.PropertyValueReadView;
import com.ptc.core.lwc.server.TypeDefinitionServiceHelper;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.windchill.enterprise.change2.dataUtilities.ChangeLinkAttributeDataUtility;
import ext.appo.change.ModifyHelper;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.models.CorrelationObjectLink;
import ext.appo.ecn.common.util.ChangePartQueryUtils;
import ext.appo.ecn.common.util.ChangeUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.ecn.util.AffectedItemsUtil;
import ext.generic.integration.erp.bean.InventoryPrice;
import ext.generic.integration.erp.ibatis.InventoryPriceIbatis;
import ext.lang.PIStringUtils;
import ext.pi.core.PICoreHelper;
import org.apache.log4j.Logger;
import wt.change2.AffectedActivityData;
import wt.change2.ChangeActivityIfc;
import wt.change2.Changeable2;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.log4j.LogR;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.workflow.work.WorkItem;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class AffectedDataUtility extends ChangeLinkAttributeDataUtility implements ChangeConstants, ModifyConstants {

    private static final Logger LOGGER = LogR.getLogger(AffectedDataUtility.class.getName());

    @Override
    public Object getDataValue(String paramString, Object paramObject, ModelContext paramModelContext) throws WTException {
        SessionContext previous = SessionContext.newContext();
        // 当前用户设置为管理员，用于忽略权限
        SessionHelper.manager.setAdministrator();
        try {
            if (paramString.contains(ARTICLEINVENTORY_COMPID) || paramString.contains(CENTRALWAREHOUSEINVENTORY_COMPID) || paramString.contains(PASSAGEINVENTORY_COMPID)) {
                if (paramObject instanceof WTPart) {
                    GUIComponentArray gui_array = new GUIComponentArray();
                    //gui_array.addGUIComponent(generateTextDisplayComponent(paramModelContext, paramObject, paramString, getValue(paramModelContext, paramObject, paramString)));
                    gui_array.addGUIComponent(generateTextDisplayComponent(paramModelContext, paramObject, paramString, null));
                    return gui_array;
                }
            } else if (paramString.contains(ARTICLEDISPOSE_COMPID) || paramString.contains(INVENTORYDISPOSE_COMPID) || paramString.contains(PASSAGEDISPOSE_COMPID) || paramString.contains(PRODUCTDISPOSE_COMPID) || paramString.contains(AADDESCRIPTION_COMPID)) {
                GUIComponentArray gui_array = new GUIComponentArray();
                gui_array.addGUIComponent(generateTextDisplayComponent(paramModelContext, paramObject, paramString, getValue(paramModelContext, paramObject, paramString)));
                return gui_array;
            } else if (paramString.contains(ATTRIBUTE_9)) {
                GUIComponentArray gui_array = new GUIComponentArray();
                ComboBox comboBox = generateComboBox(paramModelContext, paramObject, paramString, getApprovalOpinion(paramModelContext, paramObject));
                gui_array.addGUIComponent(comboBox);
                return gui_array;
            } else if (paramString.contains(ATTRIBUTE_10)) {
                GUIComponentArray gui_array = new GUIComponentArray();
                TextBox textBox = generateTextBox(paramModelContext, paramObject, paramString, getRemark(paramModelContext, paramObject));
                textBox.setRequired(false);
                gui_array.addGUIComponent(textBox);
                return gui_array;
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        } finally {
            SessionContext.setContext(previous);
        }
        return super.getDataValue(paramString, paramObject, paramModelContext);
    }

    /**
     * 获取审批意见
     * @param paramModelContext
     * @param paramObject
     * @return
     * @throws WTException
     */
    public String getApprovalOpinion(ModelContext paramModelContext, Object paramObject) throws WTException {
        String approvalOpinion = "";
        NmCommandBean nmCommandBean = paramModelContext.getNmCommandBean();
        Object object = nmCommandBean.getPageOid().getRefObject();
        LOGGER.info("=====object: " + object);
        if (object instanceof WorkItem) {
            WorkItem workItem = (WorkItem) object;
            Persistable persistable = workItem.getPrimaryBusinessObject().getObject();
            if (persistable instanceof WTChangeOrder2) {
                WTChangeOrder2 changeOrder2 = (WTChangeOrder2) persistable;
                String ecnVid = String.valueOf(PICoreHelper.service.getBranchId(changeOrder2));
                String branchId = String.valueOf(PICoreHelper.service.getBranchId(paramObject));
                LOGGER.info("=====ecnVid: " + ecnVid + " >>>>>branchId: " + branchId);
                CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, branchId, LINKTYPE_1);
                if (null == link && paramObject instanceof Map) {
                    Map map = (Map) paramObject;
                    Object number = map.get("number");
                    if (CONSTANTS_9.equals(number))
                        link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, CONSTANTS_7, CONSTANTS_7);
                }
                LOGGER.info("=====link: " + link);
                if (null != link)
                    approvalOpinion = PIStringUtils.isNull(link.getApprovalOpinion()) ? "" : link.getApprovalOpinion();
            }
        }
        return approvalOpinion;
    }

    /**
     * 获取备注（驳回必填）
     * @param paramModelContext
     * @param paramObject
     * @return
     * @throws WTException
     */
    public String getRemark(ModelContext paramModelContext, Object paramObject) throws WTException {
        String remark = "";
        NmCommandBean nmCommandBean = paramModelContext.getNmCommandBean();
        Object object = nmCommandBean.getPageOid().getRefObject();
        LOGGER.info("=====object: " + object);
        if (object instanceof WorkItem) {
            WorkItem workItem = (WorkItem) object;
            Persistable persistable = workItem.getPrimaryBusinessObject().getObject();
            if (persistable instanceof WTChangeOrder2) {
                WTChangeOrder2 changeOrder2 = (WTChangeOrder2) persistable;
                String ecnVid = String.valueOf(PICoreHelper.service.getBranchId(changeOrder2));
                String branchId = String.valueOf(PICoreHelper.service.getBranchId(paramObject));
                LOGGER.info("=====ecnVid: " + ecnVid + " >>>>>branchId: " + branchId);
                CorrelationObjectLink link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, branchId, LINKTYPE_1);
                if (null == link && paramObject instanceof Map) {
                    Map map = (Map) paramObject;
                    Object number = map.get("number");
                    if (CONSTANTS_9.equals(number))
                        link = ModifyHelper.service.queryCorrelationObjectLink(ecnVid, CONSTANTS_7, CONSTANTS_7);
                }
                LOGGER.info("=====link: " + link);
                if (null != link) remark = PIStringUtils.isNull(link.getRemark()) ? "" : link.getRemark();
            }
        }
        return remark;
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
        } else if (keyStr.contains(ATTRIBUTE_9)) {
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
     * @param compid
     *            表单列名称
     * @return
     * @throws WTException
     */
    public Object getValue(ModelContext paramModelContext, Object paramObject, String compid) throws WTException {
        try {
            String rawValue = paramModelContext.getRawValue() == null ? "" : paramModelContext.getRawValue().toString();

            /* 在制数量、库存数量、在途数量 */
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
            /* 更改详细描述 */
            if (compid.contains(AADDESCRIPTION_COMPID)) {
                Object modelObject = paramModelContext.getModelObject();
                if (modelObject instanceof Changeable2) {
                    Changeable2 changeable2 = (Changeable2) modelObject;
                    Object object = paramModelContext.getNmCommandBean().getActionOid().getRefObject();
                    if (object instanceof WorkItem) {
                        WorkItem workItem = (WorkItem) object;
                        Object pbo = workItem.getPrimaryBusinessObject().getObject();
                        if (pbo instanceof WTChangeOrder2) {
                            WTChangeOrder2 changeOrder2 = (WTChangeOrder2) pbo;
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
                    }
                }
            }
            /* *在制处理措施、*库存处理措施、*在途处理措施、*已出货成品处理措施 */
            if (compid.contains(ARTICLEDISPOSE_COMPID) || compid.contains(INVENTORYDISPOSE_COMPID) || compid.contains(PASSAGEDISPOSE_COMPID) || compid.contains(PRODUCTDISPOSE_COMPID)) {
                if (rawValue.contains(USER_KEYWORD)) {
                    rawValue = rawValue.substring(rawValue.lastIndexOf(USER_KEYWORD) + 1);
                }
                return rawValue;
            }

            return rawValue;
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
        }

        return returnStr.toString();
    }
}
