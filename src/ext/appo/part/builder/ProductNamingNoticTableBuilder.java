package ext.appo.part.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import wt.doc.WTDocument;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.util.WTException;

import com.ptc.jca.mvc.components.JcaComponentParams;
import com.ptc.mvc.components.AbstractComponentBuilder;
import com.ptc.mvc.components.ColumnConfig;
import com.ptc.mvc.components.ComponentBuilder;
import com.ptc.mvc.components.ComponentConfig;
import com.ptc.mvc.components.ComponentConfigFactory;
import com.ptc.mvc.components.ComponentParams;
import com.ptc.mvc.components.TableConfig;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.beans.NmHelperBean;

import ext.appo.part.beans.ProductLineConfigBean;
import ext.appo.part.beans.ProductNamingNoticBean;
import ext.appo.part.dataUtilities.ProductNamingNoticDataUtility;
import ext.appo.part.workflow.ReadExcelData;
import ext.lang.PIStringUtils;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIClassificationHelper;
import ext.pi.core.PIDocumentHelper;

@ComponentBuilder("ext.appo.part.builder.ProductNamingNoticTableBuilder")
public class ProductNamingNoticTableBuilder extends AbstractComponentBuilder {

	private static final Logger LOGGER = LogR.getLogger(ProductNamingNoticTableBuilder.class.getName());
	@SuppressWarnings("all")
	@Override
	public Object buildComponentData(ComponentConfig componentConfig, ComponentParams componentparams)
			throws WTException {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		NmHelperBean localNmHelperBean = ((JcaComponentParams) componentparams).getHelperBean();
		NmCommandBean nmcommandbean = localNmHelperBean.getNmCommandBean();
		String parameter = nmcommandbean.getOpenerElemAddress();
		if (PIStringUtils.isNotNull(parameter) && !parameter.equals("none")) {
			parameter = parameter.substring(0, parameter.length() -2);
			WTDocument document = (WTDocument)((new ReferenceFactory()).getReference(parameter).getObject()) ;
				QueryResult findAssociatedParts = PIDocumentHelper.service.findAssociatedParts(document);
				while (findAssociatedParts.hasMoreElements()) {
					Map<String, String> map = new HashMap<String, String>();
					WTPart wtPart = (WTPart) findAssociatedParts.nextElement();
					Collection<String> softAttributeNames = PIAttributeHelper.service.getSoftAttributeNames(wtPart);
					Iterator<String> iterator = softAttributeNames.iterator();
					while (iterator.hasNext()) {
						String attri = (String) iterator.next();
						String value = (String) PIAttributeHelper.service.getValue(wtPart, attri);
						map.put(attri, value);
					}
					map.put("name", wtPart.getName());
					map.put("oid", (new ProductNamingNoticBean()).getOid().toString());
					map.put("partOid", ((new ReferenceFactory()).getReferenceString(wtPart)));
					list.add(map);
				}
				

		}
		return list;
	}

	@Override
	public ComponentConfig buildComponentConfig(ComponentParams componentParams) throws WTException {

		ComponentConfigFactory componentconfigfactory = getComponentConfigFactory();
		TableConfig tableconfig = componentconfigfactory.newTableConfig();
		tableconfig.setConfigurable(true);
		tableconfig.setSelectable(true);
		tableconfig.setActionModel("Product_Naming_Notic");

		ColumnConfig columnConfig = componentconfigfactory.newColumnConfig("nbxh", true);
		columnConfig.setLabel("*内部型号");
		columnConfig.setWidth(50);
		columnConfig.setVariableHeight(true);
		columnConfig.setAutoSize(true);
		columnConfig.setDataUtilityId("productNamingNoticDataUtility");
		tableconfig.addComponent(columnConfig);

		columnConfig = componentconfigfactory.newColumnConfig("name", true);
		columnConfig.setLabel("*名称");
		columnConfig.setWidth(50);
		columnConfig.setVariableHeight(true);
		columnConfig.setAutoSize(true);
		columnConfig.setDataUtilityId("productNamingNoticDataUtility");
		tableconfig.addComponent(columnConfig);

		String productLine = (String) componentParams.getParameter("productLine");
		if (productLine != null && !productLine.equals("")) {
			// 读取‘所属产品线’属性配置
			List<ProductLineConfigBean> productLineConfigArray = ReadExcelData.getProductLineConfig() ;
			for(ProductLineConfigBean bean : productLineConfigArray){
				if(bean.getEnumName().equalsIgnoreCase(productLine)){
					if(PIStringUtils.isNotNull(bean.getNodeName())){
						productLine = bean.getNodeName() ;
					}
					break ;
				}
			}
			Collection<String> ibaAttributeNames = PIClassificationHelper.service.getAttributeNames(productLine);
			Map<String, String> attributeDisplayNames = PIClassificationHelper.service
					.getAttributeDisplayNames(productLine, ibaAttributeNames, Locale.CHINA);

			List<String> readExcel = ReadExcelData.readExcel(productLine);
			for (int i = 0; i < readExcel.size(); i++) {
				String value = readExcel.get(i);

				Iterator it = attributeDisplayNames.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry entry = (Map.Entry) it.next();
					String key = (String) entry.getKey();
					String display = (String) entry.getValue();
					if (display.equals(value)) {
						ColumnConfig columnConfig3 = componentconfigfactory.newColumnConfig(key, true);
						// 必填属性添加'*'
						ProductNamingNoticDataUtility dataUtility = new ProductNamingNoticDataUtility() ;
						Boolean required = dataUtility.checkAttributeRequired(productLine, key) ;
						if(required){
							columnConfig3.setLabel("*"+display);
						}else{
							columnConfig3.setLabel(display);
						}
						columnConfig3.setWidth(50);
						columnConfig3.setVariableHeight(true);
						columnConfig3.setAutoSize(true);
						columnConfig3.setDataUtilityId("productNamingNoticDataUtility");
						tableconfig.addComponent(columnConfig3);
					}
				}
			}
		}
		
		columnConfig = componentconfigfactory.newColumnConfig("bz", true);
		columnConfig.setLabel("备注");
		columnConfig.setWidth(50);
		columnConfig.setVariableHeight(true);
		columnConfig.setAutoSize(true);
		columnConfig.setDataUtilityId("productNamingNoticDataUtility");
		tableconfig.addComponent(columnConfig);

		columnConfig = componentconfigfactory.newColumnConfig("partOid", true);
		columnConfig.setLabel("partOid");
		columnConfig.setHidden(true);
		tableconfig.addComponent(columnConfig);

		return tableconfig;
	}

}
