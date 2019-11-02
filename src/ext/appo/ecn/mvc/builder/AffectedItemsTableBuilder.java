package ext.appo.ecn.mvc.builder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONArray;

import wt.change2.AffectedActivityData;
import wt.change2.ChangeActivityIfc;
import wt.change2.Changeable2;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.fc.ReferenceFactory;
import wt.log4j.LogR;
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

import ext.appo.ecn.common.util.ChangeUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.lang.PIStringUtils;

@ComponentBuilder("ext.appo.ecn.mvc.builder.AffectedItemsTableBuilder")
public class AffectedItemsTableBuilder extends AbstractComponentBuilder{
	
	private static final String CLASSNAME = AffectedItemsTableBuilder.class.getName() ;
	private static final Logger LOG = LogR.getLogger(CLASSNAME) ;
	
	private static final String TABLE_ID = "ext.appo.ecn.mvc.builder.AffectedItemsTableBuilder";
	
	ClientMessageSource messageChange2ClientResource = getMessageSource("ext.appo.ecn.resource.changeNoticeActionsRB");

	@Override
	public Object buildComponentData(ComponentConfig paramComponentConfig, ComponentParams paramComponentParams)throws Exception {
		SessionContext previous = SessionContext.newContext();
		try {
			// 当前用户设置为管理员，用于忽略权限
			SessionHelper.manager.setAdministrator();
			
			// 收集对象
			Collection<Persistable> returnArray = new HashSet<Persistable>() ;
			NmHelperBean nmhelperbean = ((JcaComponentParams) paramComponentParams).getHelperBean();
	        NmCommandBean commandbean = nmhelperbean.getNmCommandBean();
	        // 获取操作对象
	        Object object = commandbean.getActionOid().getRefObject() ;
	        if(LOG.isDebugEnabled()){
	        	LOG.debug("object :" +object);
	        }
	        // 获取页面添加数据
        	Map<Persistable, Map<String, String>> pageChangeTaskArray = ChangeUtils.getPageChangeTaskArray(commandbean) ;
        	if(pageChangeTaskArray == null || pageChangeTaskArray.size() == 0){
        		// 编辑添加数据过滤问题
    			Map<String, Object> parameterMap = commandbean.getParameterMap() ;
    			if(!parameterMap.containsKey(ChangeConstants.CHANGETASK_ARRAY)){
    				if(object instanceof WTChangeOrder2){
    					// 获取ECN中所有受影响对象
    					Map<ChangeActivityIfc, Collection<Changeable2>> ecaDatasMap = ChangeUtils.getChangeablesBeforeInfo((WTChangeOrder2)object) ;
    					// 查询受影响的活动对象，限制ECA状态不为‘已取消’
    					Collection<AffectedActivityData> adArray = ChangeUtils.getAffectedActivityDatas(ecaDatasMap, ChangeConstants.CANCELLED) ;
    					for(AffectedActivityData affectedActivityData : adArray){
    						returnArray.add(affectedActivityData.getRoleBObject()) ;
    					}
    				}
    			}
        	}else{
        		for(Map.Entry<Persistable, Map<String, String>> entryMap : pageChangeTaskArray.entrySet()){
        			returnArray.add(entryMap.getKey()) ;
        		}
        	}
	        // 获取新增数据
			HttpServletRequest request = commandbean.getRequest();
			//获取选择的部件Oid
			String selectOids = request.getParameter("selectOids");
			if(LOG.isDebugEnabled()){
				LOG.debug("selectOids = " + selectOids);
			}
			if(PIStringUtils.isNotNull(selectOids)){
				JSONArray jsonArray = new JSONArray(selectOids) ;
				for (int i = 0; i < jsonArray.length(); i++) {
					returnArray.add((new ReferenceFactory()).getReference(jsonArray.getString(i)).getObject()) ;
				}
			}
			
			return returnArray ;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace(); 
		} finally{
			SessionContext.setContext(previous);
		}
		return null ;
	}

	@Override
	public ComponentConfig buildComponentConfig(ComponentParams paramComponentParams)throws WTException {
		ComponentConfigFactory componentconfigfactory = getComponentConfigFactory();
		JcaTableConfig tableConfig = (JcaTableConfig)componentconfigfactory.newTableConfig();
		
		tableConfig.setLabel(this.messageChange2ClientResource.getMessage("changeNotice.affectedItemsTableBuilder.description"));
		tableConfig.setId (TABLE_ID);
		tableConfig.setSelectable(true);
        tableConfig.setShowCount(true);
        
        NmHelperBean localNmHelperBean = ((JcaComponentParams) paramComponentParams).getHelperBean();
		NmCommandBean localNmCommandBean = localNmHelperBean.getNmCommandBean();
		boolean bool = CreateAndEditWizBean.isCreateEditWizard(localNmCommandBean);
		if(bool){
			tableConfig.setActionModel("dcn.affectedItems.table.create_edit");
		}

		ColumnConfig columnconfig =  (JcaColumnConfig)componentconfigfactory.newColumnConfig("name", true);
		columnconfig.setAutoSize(true);
		tableConfig.addComponent(columnconfig);

		columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("number", true);
		columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);
        
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("version", true);
        columnconfig.setAutoSize(true);
		tableConfig.addComponent(columnconfig);
		
		columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("state", true);
		columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);
        
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("ArticleInventory", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("ARTICLEINVENTORY"));
        columnconfig.setDataUtilityId("customizationDataUtility");
		columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);
        
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("ArticleDispose", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("ARTICLEDISPOSE"));
        columnconfig.setDataUtilityId("customizationDataUtility");
		columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);
        
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("PassageInventory", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("PASSAGEINVENTORY"));
        columnconfig.setDataUtilityId("customizationDataUtility");
		columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);
        
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("PassageDispose", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("PASSAGEDISPOSE"));
        columnconfig.setDataUtilityId("customizationDataUtility");
		columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);
        
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("CentralWarehouseInventory", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("CENTRALWAREHOUSEINVENTORY"));
        columnconfig.setDataUtilityId("customizationDataUtility");
		columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);
        
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("InventoryDispose", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("INVENTORYDISPOSE"));
        columnconfig.setDataUtilityId("customizationDataUtility");
		columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);
        
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("ProductDispose", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("PRODUCTDISPOSE"));
        columnconfig.setDataUtilityId("customizationDataUtility");
		columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);
        
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("ChangeType", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("CHANGETYPE"));
        columnconfig.setDataUtilityId("customizationDataUtility");
		columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);
        
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("CompletionTime", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COMPLETIONTIME"));
        columnconfig.setDataUtilityId("customizationDataUtility");
		columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);
        
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("ResponsiblePerson", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("RESPONSIBLEPERSON"));
        columnconfig.setDataUtilityId("IBA|ResponsiblePerson");
		columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);
        
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("aadDescription", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("AADDESCRIPTION"));
        columnconfig.setDataUtilityId("customizationDataUtility");
		columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);
        
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("CollectionNumber", true);
        columnconfig.setLabel("收集对象");
        columnconfig.setDataUtilityId("customizationDataUtility");
		columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);
		
		return tableConfig;
	}

}
