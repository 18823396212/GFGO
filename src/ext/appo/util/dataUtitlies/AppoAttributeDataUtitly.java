package ext.appo.util.dataUtitlies;

import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.AbstractDataUtility;
import com.ptc.core.components.factory.dataUtilities.AttributeDataUtilityHelper;
import com.ptc.core.components.rendering.guicomponents.ComboBox;
import com.ptc.core.ui.resources.ComponentMode;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import ext.appo.part.workflow.PartWorkflowUtil;
import ext.appo.util.excel.AppoExcelUtil;
import ext.pi.core.PIAttributeHelper;
import org.apache.log4j.Logger;
import wt.fc.Persistable;
import wt.inf.container.WTContainer;
import wt.log4j.LogR;
import wt.util.WTException;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 光峰属性级联类
 *
 * @author HYJ&NJH
 *
 */
public class AppoAttributeDataUtitly extends AbstractDataUtility {

    private static final Logger logger = LogR.getLogger(AppoAttributeDataUtitly.class.getName());

    @Override
    public Object getDataValue(String componentId, Object datum, ModelContext mc) throws WTException {

        ComponentMode cModel = mc.getDescriptorMode();
        System.out.println("into AppoAttributeDataUtitly test");
        if (componentId.contains("sscpx")) {
            if (ComponentMode.CREATE.equals(cModel)) {
                System.out.println("into create 1");
                ComboBox cb = getSscpxComboBox(componentId, datum, mc);
                cb.setSelected("");
                return cb;
            } else if (ComponentMode.EDIT.equals(cModel)) {
                System.out.println("into edit 2");
                ComboBox cb = getSscpxComboBox(componentId, datum, mc);
                String value = (String) mc.getRawValue();
                cb.setSelected(value);
                return cb;
            } else {
                return PIAttributeHelper.service.getDisplayValue((Persistable) datum, "sscpx", Locale.CHINA);
            }
        } else if (componentId.contains("ssxm")) {
            if (ComponentMode.CREATE.equals(cModel) || ComponentMode.EDIT.equals(cModel)) {
                // 读取配置表
                Map<String, List<String>> readSheet4 = new AppoExcelUtil().readSheet4();
                // 页面对象是否Persistable
                if (datum instanceof Persistable) {
                } else {
                    datum = mc.getNmCommandBean().getPageOid().getRefObject();
                }
                // 获取sscpx值
                String displayValue = PIAttributeHelper.service.getDisplayValue((Persistable) datum, "sscpx", Locale.CHINA);
                ArrayList<String> list = new ArrayList<>();
                if (displayValue != null && !"".equals(displayValue)) {
                    list = (ArrayList<String>) readSheet4.get(displayValue);
                }

                ComboBox cb = new ComboBox();
                cb.setId(componentId);
                cb.setName(componentId);
                cb.setRequired(true);
                cb.setValues(list);
                String value = (String) mc.getRawValue();
                cb.setSelected(value);

                return cb;
            } else {
                return PIAttributeHelper.service.getDisplayValue((Persistable) datum, "ssxm", Locale.CHINA);
            }
        }
        return null;
    }

    /**
     * 创建页面控件
     * @param componentId
     * @param datum
     * @param mc
     * @return
     * @throws WTException
     * @author HYJ&NJH
     */
    private ComboBox getSscpxComboBox(String componentId, Object datum, ModelContext mc) throws WTException {
        // 获取库名称
        NmCommandBean nmCommandBean = mc.getNmCommandBean();
        NmOid sharedContextOid = nmCommandBean.getSharedContextOid();
        Object refObject = sharedContextOid.getRefObject();
        WTContainer wtContainer = (WTContainer) refObject;
        String wtContainerName = wtContainer.getName();
        System.out.println("wtContainerName==" + wtContainerName);

        Map<List<String>, List<String>> cbMap = getcbMap(wtContainerName);

        Set<List<String>> set = cbMap.keySet();
        // 未处理的列表
        ArrayList<String> interiorArray = (ArrayList<String>) set.iterator().next();
        ArrayList<String> displayArray = (ArrayList<String>) cbMap.get(interiorArray);
        // 构建下拉框
        ComboBox cb = new ComboBox(interiorArray, displayArray, new ArrayList<>());
        cb.setName(componentId);
        cb.setId(componentId);
        cb.setColumnName(AttributeDataUtilityHelper.getColumnName(componentId, datum, mc));
        cb.setRequired(true);
        cb.addJsAction("onChange", getReloadOptionsFunction());

        return cb;
    }

    /**
     * 获取配置表中数据
     * @param wtContainerName
     * @return
     * @throws WTException
     * @author HYJ&NJH
     */
    public Map<List<String>, List<String>> getcbMap(String wtContainerName) throws WTException {
        Map<List<String>, List<String>> cbMap = new HashMap<>();
        // 读取配置表
        Map<String, List<String>> readSheet4 = new AppoExcelUtil().readSheet4();
        List<String> list = null;
        // 库名称无配置的，取所有
        if (readSheet4.containsKey(wtContainerName)) {
            System.out.println("wtContainerName1111==" + wtContainerName);
            // 通过库名称获取库可选择的产品线集合
            list = readSheet4.get(wtContainerName);
        } else {
            System.out.println("wtContainerName2222==" + wtContainerName);
            // 获取所有
            list = readSheet4.get("all");
        }

        // 取出系统中的枚举的定义
        Map<ArrayList<String>, ArrayList<String>> enumeratedMap = new PartWorkflowUtil().getEnumeratedMap("sscpx");
        Set<ArrayList<String>> set = enumeratedMap.keySet();
        // 未处理的列表
        ArrayList<String> oldInteriorArray = set.iterator().next();
        ArrayList<String> oldDisplayArray = enumeratedMap.get(oldInteriorArray);
        // 处理后的列表
        ArrayList<String> interiorArray = new ArrayList<>();
        interiorArray.add("");
        ArrayList<String> displayArray = new ArrayList<>();
        displayArray.add("");
        // 处理,存在则显示
        for (int i = 0; i < oldDisplayArray.size(); i++) {
            String displayVal = oldDisplayArray.get(i);
            if (list.contains(displayVal)) {
                displayArray.add(displayVal);
                interiorArray.add(oldInteriorArray.get(i));
            }
        }
        cbMap.put(interiorArray, displayArray);
        return cbMap;
    }

    /**
     * 联动值
     *
     * @param request
     * @return
     * @author HYJ&NJH
     */
    public String getOptionsVal(HttpServletRequest request) {
        // 读取配置表
        Map<String, List<String>> readSheet4 = new AppoExcelUtil().readSheet4();
        String[] values = request.getParameterValues("sscpx");
        String optionsVal = "";
        String sscpx = values[0];
        List<String> list = readSheet4.get(sscpx);
        if (list != null) {
            if (list.size() > 1) {
                String listString = list.toString();
                optionsVal = listString.substring(1, listString.length() - 1);
            } else if (list.size() == 1) {
                optionsVal = list.get(0);
            } else {
                optionsVal = "读取配置表失败,请联系管理员!";
            }
        }
        return optionsVal;
    }

	private String getReloadOptionsFunction() {
		String reloadOptionsFunction = "reloadSSXM(this);" + "function reloadSSXM(obj) {\r\n"
				+ "		var selected = obj.selected;\r\n" + "		var index = obj.selectedIndex;\r\n"
				+ "		var value = obj.options[index].text;\r\n" + "		var params = {\r\n"
				+ "			\"sscpx\" : value\r\n" + "		}\r\n" + "		var options = {\r\n"
				+ "			asynchronous : false,\r\n" + "			parameters : params,\r\n"
				+ "			method : 'POST'\r\n" + "		};\r\n"
				+ "		var url = \"/Windchill/ptc1/netmarkets/jsp/ext/appo/util/appoAttribute.jsp\";\r\n"
				+ "		var transport = requestHandler.doRequest(url, options);\r\n"
				+ "		var responseText = transport.responseText;\r\n"
				+ "		var inplen = document.getElementsByTagName(\"select\");\r\n"
				+ "		for (var i = 0; i < inplen.length; i++) {\r\n" + "			var select = inplen[i];\r\n"
				+ "			var selectId = select.id;\r\n" + "			if (selectId.indexOf(\"ssxm\") != -1) {\r\n"
				+ "				//clear\r\n" + "				select.options.length = 0;\r\n"
				+ "				//add\r\n" + "				if (responseText.indexOf(\",\") != -1) {\r\n"
				+ "					var optionVals = responseText.split(\",\");\r\n"
				+ "					for(var j= 0;j<optionVals.length;j++){\r\n"
				+ "						select.options.add(new Option(optionVals[j]));\r\n" + "					}\r\n"
				+ "				} else {\r\n" + "					select.options.add(new Option(responseText));\r\n"
				+ "				}\r\n" + "\r\n" + "			}\r\n" + "		}\r\n" + "	}";

		return reloadOptionsFunction;
	}
}