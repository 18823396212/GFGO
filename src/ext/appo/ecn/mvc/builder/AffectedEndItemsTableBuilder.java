package ext.appo.ecn.mvc.builder;

import java.util.Collection;
import java.util.HashSet;

import org.json.JSONArray;

import wt.change2.WTChangeOrder2;
import wt.configurablelink.ConfigurableDescribeLink;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.part.WTPart;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.util.WTException;

import com.ptc.core.components.beans.CreateAndEditWizBean;
import com.ptc.jca.mvc.components.JcaColumnConfig;
import com.ptc.jca.mvc.components.JcaComponentParams;
import com.ptc.jca.mvc.components.JcaTableConfig;
import com.ptc.mvc.components.AbstractComponentBuilder;
import com.ptc.mvc.components.ColumnConfig;
import com.ptc.mvc.components.ComponentBuilder;
import com.ptc.mvc.components.ComponentConfig;
import com.ptc.mvc.components.ComponentConfigFactory;
import com.ptc.mvc.components.ComponentParams;
import com.ptc.mvc.util.ClientMessageSource;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.beans.NmHelperBean;

import ext.appo.ecn.constants.ChangeConstants;
import ext.lang.PIStringUtils;

@ComponentBuilder("ext.appo.ecn.mvc.builder.AffectedEndItemsTableBuilder")
public class AffectedEndItemsTableBuilder extends AbstractComponentBuilder{
	
	private static final String TABLE_ID = "ext.appo.ecn.mvc.builder.AffectedEndItemsTableBuilder";
	
	ClientMessageSource messageChange2ClientResource = getMessageSource("ext.appo.ecn.resource.changeNoticeActionsRB");

	@Override
	public Object buildComponentData(ComponentConfig paramComponentConfig, ComponentParams paramComponentParams) throws Exception {
		SessionContext previous = SessionContext.newContext();
		try {
			// 当前用户设置为管理员，用于忽略权限
			SessionHelper.manager.setAdministrator();
			
			Collection<WTPart> datasArray = getEndItemsByChangeOrder2(paramComponentParams) ;
			
			// 添加数据
			addDatasArray(paramComponentParams, datasArray) ;
			
			return datasArray;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace(); 
		} finally{
			SessionContext.setContext(previous);
		}
		return null ;
	}

	@Override
	public ComponentConfig buildComponentConfig(ComponentParams paramComponentParams) throws WTException {
		ComponentConfigFactory componentconfigfactory = getComponentConfigFactory();
		JcaTableConfig tableConfig = (JcaTableConfig)componentconfigfactory.newTableConfig();
		
		tableConfig.setLabel(this.messageChange2ClientResource.getMessage("changeNotice.affectedEndItemsTableBuilder.description"));
		tableConfig.setId (TABLE_ID);
		tableConfig.setSelectable(true);
        tableConfig.setShowCount(true);
        
        NmHelperBean localNmHelperBean = ((JcaComponentParams) paramComponentParams).getHelperBean();
		NmCommandBean localNmCommandBean = localNmHelperBean.getNmCommandBean();
		boolean bool = CreateAndEditWizBean.isCreateEditWizard(localNmCommandBean);
		if(bool){
			tableConfig.setActionModel("affectedEndItems.table.create_remove");
		}
		
		ColumnConfig columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("type_icon", true);
		tableConfig.addComponent(columnconfig);
		
		columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("name", true);
		columnconfig.setAutoSize(true);
		tableConfig.addComponent(columnconfig);

		columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("number", true);
		columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);
        
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("version", true);
        columnconfig.setAutoSize(true);
		tableConfig.addComponent(columnconfig);
		
		columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("infoPageAction", false);
		columnconfig.setAutoSize(true);        
		tableConfig.addComponent(columnconfig);

		columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("state", true);
		columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);
        
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("ggms", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_GGMS"));
		columnconfig.setAutoSize(true);
		columnconfig.setDataUtilityId("customizationDataUtility");
        tableConfig.addComponent(columnconfig);
        
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("sscpx", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_SSCPX"));
		columnconfig.setAutoSize(true);
		columnconfig.setDataUtilityId("customizationDataUtility");
        tableConfig.addComponent(columnconfig);
        
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("ssxm", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_SSXM"));
		columnconfig.setAutoSize(true);
		columnconfig.setDataUtilityId("customizationDataUtility");
        tableConfig.addComponent(columnconfig);
        
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("xsxh", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_XSXH"));
		columnconfig.setAutoSize(true);
		columnconfig.setDataUtilityId("customizationDataUtility");
        tableConfig.addComponent(columnconfig);
        
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("cpzt", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_CPZT"));
		columnconfig.setAutoSize(true);
		columnconfig.setDataUtilityId("customizationDataUtility");
        tableConfig.addComponent(columnconfig);
        
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("nbxh", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_NBXH"));
		columnconfig.setAutoSize(true);
		columnconfig.setDataUtilityId("customizationDataUtility");
        tableConfig.addComponent(columnconfig);
        
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("childNumber", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_CHILDNUMBER"));
        columnconfig.setDataUtilityId("customizationDataUtility");
		columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);
		
		return tableConfig;
	}

	/***
	 * 根据ChangeOrder2对象查询关联的成品对象
	 * 
	 * @param paramComponentParams
	 * @return
	 */
	public Collection<WTPart> getEndItemsByChangeOrder2(ComponentParams paramComponentParams){
		Collection<WTPart> datasArray = new HashSet<WTPart>() ;
		if(paramComponentParams == null){
			return datasArray ;
		}
		
		try {
			NmHelperBean helper = ((JcaComponentParams) paramComponentParams).getHelperBean();
			Object pbo = helper.getNmCommandBean().getActionOid().getRefObject() ;
			if(pbo instanceof WTChangeOrder2){
				QueryResult qr = PersistenceHelper.manager.navigate((WTChangeOrder2)pbo , ConfigurableDescribeLink.DESCRIBED_BY_ROLE, ConfigurableDescribeLink.class, true );
				while(qr.hasMoreElements()){
					Object value = qr.nextElement() ;
					if(value instanceof WTPart){
						datasArray.add((WTPart) value) ;
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return datasArray ;
	}
	
	/***
	 * 新增数据
	 * 
	 * @param paramComponentParams
	 * @param datasArray
	 *            新增产品OID集合
	 * @throws WTException
	 */
	public void addDatasArray(ComponentParams paramComponentParams, Collection<WTPart> datasArray) throws WTException {
		try {
			// 新增
			NmHelperBean helper = ((JcaComponentParams) paramComponentParams).getHelperBean();
			String datasJSON = helper.getNmCommandBean().getRequest().getParameter(ChangeConstants.AFFECTED_PRODUCT_ID);
			if(PIStringUtils.isNotNull(datasJSON)){
				JSONArray jsonArray = new JSONArray(datasJSON) ;
				for (int i = 0; i < jsonArray.length(); i++) {
					String partOID = jsonArray.getString(i) ;
					if(PIStringUtils.isNull(partOID)){
						continue ;
					}
					WTPart part = (WTPart)((new ReferenceFactory()).getReference(partOID).getObject()) ;
					if(!datasArray.contains(part)){
						datasArray.add(part) ;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		}
	}
}
