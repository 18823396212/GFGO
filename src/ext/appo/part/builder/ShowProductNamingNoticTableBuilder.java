package ext.appo.part.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ptc.core.components.descriptor.DescriptorConstants.ColumnIdentifiers;
import com.ptc.jca.mvc.components.JcaColumnConfig;
import com.ptc.jca.mvc.components.JcaComponentParams;
import com.ptc.mvc.components.AbstractComponentBuilder;
import com.ptc.mvc.components.ColumnConfig;
import com.ptc.mvc.components.ComponentBuilder;
import com.ptc.mvc.components.ComponentConfig;
import com.ptc.mvc.components.ComponentConfigFactory;
import com.ptc.mvc.components.ComponentParams;
import com.ptc.mvc.components.TableConfig;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.beans.NmHelperBean;

import ext.appo.part.beans.ProductLineConfigBean;
import ext.appo.part.dataUtilities.ProductNamingNoticDataUtility;
import ext.appo.part.workflow.ReadExcelData;
import ext.lang.PIStringUtils;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIClassificationHelper;
import ext.pi.core.PIDocumentHelper;
import wt.doc.WTDocument;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.workflow.work.WorkItem;

@ComponentBuilder("ext.appo.part.builder.ShowProductNamingNoticTableBuilder")
public class ShowProductNamingNoticTableBuilder extends AbstractComponentBuilder {

	private static final Logger LOGGER = LogR.getLogger(ShowProductNamingNoticTableBuilder.class.getName());

	@SuppressWarnings("all")
	@Override
	public Object buildComponentData(ComponentConfig componentConfig, ComponentParams componentparams)
			throws WTException {
		List<WTPart> list = new ArrayList<WTPart>();
		List<String> listpartnumber = new ArrayList<String>();
		SessionContext previous = SessionContext.newContext();
		try {
			// 当前用户设置为管理员
			SessionHelper.manager.setAdministrator();

			NmHelperBean localNmHelperBean = ((JcaComponentParams) componentparams).getHelperBean();
			NmCommandBean nmcommandbean = localNmHelperBean.getNmCommandBean();
			NmOid pageOid = nmcommandbean.getPageOid();
			Object refObject = pageOid.getRefObject();
			WTDocument document = null;
			if (refObject instanceof WTDocument) {
				document = (WTDocument) refObject;
			} else if (refObject instanceof WorkItem) {
				WorkItem workItem = (WorkItem) refObject;
				Persistable persistable = workItem.getPrimaryBusinessObject().getObject();
				if (persistable != null) {
					if (persistable instanceof WTDocument) {
						document = (WTDocument) persistable;
					}
				}
			}

			if (document != null) {
				QueryResult findAssociatedParts = PIDocumentHelper.service.findAssociatedParts(document);
				while (findAssociatedParts.hasMoreElements()) {
					Map<String, String> map = new HashMap<String, String>();
					WTPart wtPart = (WTPart) findAssociatedParts.nextElement();
					System.out.println("wtpart number===" + wtPart.getNumber());
					if (!listpartnumber.contains(wtPart.getNumber())) {
						listpartnumber.add(wtPart.getNumber());
						list.add(wtPart);
					}

				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			SessionContext.setContext(previous);
		}

		return list;
	}

	@Override
	public ComponentConfig buildComponentConfig(ComponentParams componentParams) throws WTException {

		ComponentConfigFactory componentconfigfactory = getComponentConfigFactory();
		TableConfig tableconfig = componentconfigfactory.newTableConfig();
		tableconfig.setSelectable(true);
		tableconfig.setType(WTPart.class.getName());
		tableconfig.setLabel("产品命名通知单");

		ColumnConfig columnConfig = (JcaColumnConfig) componentconfigfactory.newColumnConfig("type_icon", true);
		tableconfig.addComponent(columnConfig);

		columnConfig = componentconfigfactory.newColumnConfig(ColumnIdentifiers.INFO_ACTION, true);
		columnConfig.setSortable(false);
		tableconfig.addComponent(columnConfig);

		columnConfig = componentconfigfactory.newColumnConfig("name", true);
		columnConfig.setLabel("*名称");
		columnConfig.setWidth(50);
		columnConfig.setVariableHeight(true);
		columnConfig.setAutoSize(true);
		columnConfig.setDataUtilityId("showProductNamingNoticDataUtility");
		tableconfig.addComponent(columnConfig);

		columnConfig = componentconfigfactory.newColumnConfig("number", true);
		columnConfig.setLabel("编号");
		columnConfig.setWidth(50);
		columnConfig.setVariableHeight(true);
		columnConfig.setAutoSize(true);
		columnConfig.setDataUtilityId("showProductNamingNoticDataUtility");
		tableconfig.addComponent(columnConfig);

		columnConfig = componentconfigfactory.newColumnConfig("nbxh", true);
		columnConfig.setLabel("*内部型号");
		columnConfig.setWidth(50);
		columnConfig.setVariableHeight(true);
		columnConfig.setAutoSize(true);
		columnConfig.setDataUtilityId("showProductNamingNoticDataUtility");
		tableconfig.addComponent(columnConfig);

		NmHelperBean localNmHelperBean = ((JcaComponentParams) componentParams).getHelperBean();
		NmCommandBean nmcommandbean = localNmHelperBean.getNmCommandBean();
		NmOid pageOid = nmcommandbean.getPageOid();
		Object refObject = pageOid.getRefObject();
		WTDocument document = null;
		if (refObject instanceof WTDocument) {
			document = (WTDocument) refObject;
		} else if (refObject instanceof WorkItem) {
			WorkItem workItem = (WorkItem) refObject;
			Persistable persistable = workItem.getPrimaryBusinessObject().getObject();
			if (persistable != null) {
				if (persistable instanceof WTDocument) {
					document = (WTDocument) persistable;
				}
			}
		}
		String productLine = (String) PIAttributeHelper.service.getValue(document, "sscpx");
		if (productLine != null && !productLine.equals("")) {
			// 读取‘所属产品线’属性配置
			List<ProductLineConfigBean> productLineConfigArray = ReadExcelData.getProductLineConfig();
			for (ProductLineConfigBean bean : productLineConfigArray) {
				if (bean.getEnumName().equalsIgnoreCase(productLine)) {
					if (PIStringUtils.isNotNull(bean.getNodeName())) {
						productLine = bean.getNodeName();
					}
					break;
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
						ProductNamingNoticDataUtility dataUtility = new ProductNamingNoticDataUtility();
						Boolean required = dataUtility.checkAttributeRequired(productLine, key);
						if (required) {
							columnConfig3.setLabel("*" + display);
						} else {
							columnConfig3.setLabel(display);
						}
						columnConfig3.setWidth(50);
						columnConfig3.setVariableHeight(true);
						columnConfig3.setAutoSize(true);
						columnConfig3.setDataUtilityId("showProductNamingNoticDataUtility");
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
		columnConfig.setDataUtilityId("showProductNamingNoticDataUtility");
		tableconfig.addComponent(columnConfig);

		return tableconfig;
	}

}
