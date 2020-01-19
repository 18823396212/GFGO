package ext.appo.change.util;

import com.ptc.core.lwc.common.view.EnumerationDefinitionReadView;
import com.ptc.core.lwc.common.view.EnumerationMembershipReadView;
import com.ptc.core.lwc.common.view.PropertyHolderHelper;
import com.ptc.core.lwc.common.view.PropertyValueReadView;
import com.ptc.core.lwc.server.TypeDefinitionServiceHelper;
import ext.appo.ecn.constants.ChangeConstants;
import ext.lang.PIStringUtils;
import wt.session.SessionHelper;
import wt.util.WTException;

import java.util.*;

public class EnumeratedMap {

    /***
     * 获取全局枚举定义
     *
     * @param enumName
     *            全局枚举内部名称
     *
     * @return key:枚举定义内部名称 value:枚举定义显示名称
     *
     * @throws WTException
     */
    public static Map<ArrayList<String>, ArrayList<String>> getEnumeratedMap(String enumName) throws WTException {
        Map<ArrayList<String>, ArrayList<String>> enumMap = new HashMap<ArrayList<String>, ArrayList<String>>();
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
                    Map<Integer, Map<String, String>> datasMap = new TreeMap<Integer, Map<String, String>>();
                    for (EnumerationMembershipReadView enumerationMembershipReadView : datasArray) {
                        // 内部名称
                        String interiorName = enumerationMembershipReadView.getName();
                        Collection<PropertyValueReadView> array = enumerationMembershipReadView.getAllProperties();
                        for (PropertyValueReadView propertyValueReadView : array) {
                            Integer index = Integer.parseInt(propertyValueReadView.getValueAsString());
                            // 存储枚举定义
                            Map<String, String> enumInfo = new HashMap<String, String>();
                            enumInfo.put(interiorName, PropertyHolderHelper.getDisplayName(enumerationMembershipReadView.getMember(), SessionHelper.getLocale()));
                            datasMap.put(index, enumInfo);
                        }
                    }
                    // 显示名称
                    ArrayList<String> displayArray = new ArrayList<String>();
                    // 内部名称
                    ArrayList<String> interiorArray = new ArrayList<String>();
                    for (Map.Entry<Integer, Map<String, String>> entryMap : datasMap.entrySet()) {
                        for (Map.Entry<String, String> enumEntryMap : entryMap.getValue().entrySet()) {
                            interiorArray.add(enumEntryMap.getKey() + ChangeConstants.USER_KEYWORD + enumEntryMap.getValue());
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
}
