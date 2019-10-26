//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package ext.generic.integration.erp.mvc.builders;

import com.ptc.mvc.components.AbstractComponentBuilder;
import com.ptc.mvc.components.ColumnConfig;
import com.ptc.mvc.components.ComponentBuilder;
import com.ptc.mvc.components.ComponentConfig;
import com.ptc.mvc.components.ComponentConfigFactory;
import com.ptc.mvc.components.ComponentParams;
import com.ptc.mvc.components.TableConfig;
import com.ptc.mvc.util.ClientMessageSource;
import ext.generic.integration.erp.ibatis.InventoryPriceIbatis;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import ext.generic.integration.erp.util.QueryInventoryPriceUtil;
import org.apache.log4j.Logger;
import wt.log4j.LogR;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.session.SessionHelper;
import wt.util.WTException;

@ComponentBuilder({"query.inventory.price"})
public class QueryInventoryPriceBuilder extends AbstractComponentBuilder {
	private static final String clazz = QueryInventoryPriceBuilder.class.getName();
	private static final Logger logger;
	private static final String RESOURCE = "ext.generic.integration.erp.manually.IntegrationResource";
	private ClientMessageSource messageSource = null;

	public QueryInventoryPriceBuilder() {
		this.messageSource = this.getMessageSource("ext.generic.integration.erp.manually.IntegrationResource");
	}

	public Object buildComponentData(ComponentConfig componentConfig, ComponentParams componentParams) throws Exception {
		List list = null;
		Object contextObject = componentParams.getContextObject();
		if (contextObject != null && contextObject instanceof WTPart) {
			WTPart part = (WTPart)contextObject;
			WTUser user = (WTUser)SessionHelper.manager.getPrincipal();
			String fullName = user.getFullName();
			String company = "";
			String[] split;

			// 获取IP地址
			String ip = InetAddress.getLocalHost().getHostAddress();
			System.out.println("IP地址："+ip+"==用户名称=="+fullName);
			//区别光峰61，70,71服务器，绎立服务器，光峰采用发K3方法，其余采用发中间表方法

			if (fullName.contains("_")) {
				split = fullName.split("_");
				company = split[1];
				System.out.println("company_split==="+company);
//				list = InventoryPriceIbatis.queryInventoryPriceWithCompany(part.getNumber(), company);
				if (ip.equals("172.32.252.61")||ip.equals("172.32.252.70")||ip.equals("172.32.252.71")){
					System.out.println("价格库存调用K3接口==");
					//调用K3接口方法
					list = QueryInventoryPriceUtil.queryInventoryPriceWithCompany(part, company);
				}else{
					System.out.println("价格库存调用中间表接口==");
					list = InventoryPriceIbatis.queryInventoryPriceWithCompany(part.getNumber(), company);
				}
			} else if (fullName.contains("\\|")) {
				System.out.println("company|split==="+company);
				split = fullName.split("|");
				company = split[1];
//				list = InventoryPriceIbatis.queryInventoryPriceWithCompany(part.getNumber(), company);
				if (ip.equals("172.32.252.61")||ip.equals("172.32.252.70")||ip.equals("172.32.252.71")){
					System.out.println("价格库存调用K3接口==");
					//调用K3接口方法
					list = QueryInventoryPriceUtil.queryInventoryPriceWithCompany(part, company);
				}else{
					System.out.println("价格库存调用中间表接口==");
					list = InventoryPriceIbatis.queryInventoryPriceWithCompany(part.getNumber(), company);
				}
			} else {
//				list = InventoryPriceIbatis.queryInventoryPrice(part.getNumber());
				if (ip.equals("172.32.252.61")||ip.equals("172.32.252.70")||ip.equals("172.32.252.71")){
					System.out.println("价格库存调用K3接口==");
					//调用接口方法
					list = QueryInventoryPriceUtil.queryInventoryPrice(part);
				}else{
					System.out.println("价格库存调用中间表接口==");
					list = InventoryPriceIbatis.queryInventoryPrice(part.getNumber());
				}
			}
		} else if (contextObject == null) {
			logger.debug("QueryInventoryPriceBuilder.buildComponentData , contextObject == null");
		}

		if (list == null) {
			logger.debug("QueryInventoryPriceBuilder.buildComponentData , list == null");
			list = new ArrayList();
		}

		return list;
	}

	public ComponentConfig buildComponentConfig(ComponentParams componentParams) throws WTException {
		ComponentConfigFactory componentConfigFactory = this.getComponentConfigFactory();
		TableConfig tableConfig = componentConfigFactory.newTableConfig();
		tableConfig.setId("query.inventory.price");
		tableConfig.setLabel(this.messageSource.getMessage("QUERY_INVENTORY_PRICE_TABLE_LABEL"));
		tableConfig.setConfigurable(false);
		ColumnConfig number = componentConfigFactory.newColumnConfig("item_id", true);
		number.setLabel("存货编码");
		tableConfig.addComponent(number);
		ColumnConfig iAveragecost = componentConfigFactory.newColumnConfig("iAveragecost", false);
		iAveragecost.setLabel("物料成本");
		tableConfig.addComponent(iAveragecost);
		ColumnConfig iSupplyCycle = componentConfigFactory.newColumnConfig("iSupplycycle", false);
		iSupplyCycle.setLabel("供货周期");
		tableConfig.addComponent(iSupplyCycle);
		ColumnConfig iMoq = componentConfigFactory.newColumnConfig("iMoq", false);
		iMoq.setLabel("最小订单量");
		tableConfig.addComponent(iMoq);
		ColumnConfig iMpq = componentConfigFactory.newColumnConfig("iMpq", false);
		iMpq.setLabel("最小包装数量");
		tableConfig.addComponent(iMpq);
		ColumnConfig cpurPerson = componentConfigFactory.newColumnConfig("cpurPerson", false);
		cpurPerson.setLabel("采购员");
		tableConfig.addComponent(cpurPerson);
		ColumnConfig cinvPerson = componentConfigFactory.newColumnConfig("cinvPerson", false);
		cinvPerson.setLabel("计划员");
		tableConfig.addComponent(cinvPerson);
		ColumnConfig iQuantity = componentConfigFactory.newColumnConfig("iQuantity", false);
		iQuantity.setLabel("库存数量");
		tableConfig.addComponent(iQuantity);
		ColumnConfig fTransinquantity = componentConfigFactory.newColumnConfig("fTransinquantity", false);
		fTransinquantity.setLabel("在途");
		tableConfig.addComponent(fTransinquantity);
		ColumnConfig fInquantity = componentConfigFactory.newColumnConfig("fInquantity", false);
		fInquantity.setLabel("在制");
		tableConfig.addComponent(fInquantity);
		ColumnConfig cFlag = componentConfigFactory.newColumnConfig("cFlag", false);
		cFlag.setLabel("发布状态");
		tableConfig.addComponent(cFlag);
		ColumnConfig dreleaseDate = componentConfigFactory.newColumnConfig("dreleaseDate", false);
		dreleaseDate.setLabel("ERP更新时间");
		tableConfig.addComponent(dreleaseDate);
		ColumnConfig mVersion = componentConfigFactory.newColumnConfig("mVersion", false);
		mVersion.setLabel("版本");
		tableConfig.addComponent(mVersion);
		ColumnConfig company = componentConfigFactory.newColumnConfig("company", false);
		company.setLabel("所属公司");
		tableConfig.addComponent(company);
		return tableConfig;
	}

	static {
		logger = LogR.getLogger(clazz);
	}
}
