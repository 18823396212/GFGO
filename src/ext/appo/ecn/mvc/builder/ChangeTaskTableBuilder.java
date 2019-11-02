package ext.appo.ecn.mvc.builder;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import wt.change2.ChangeActivityIfc;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.ObjectReference;
import wt.fc.PersistenceHelper;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.util.WTException;

import com.ptc.core.components.beans.CreateAndEditWizBean;
import com.ptc.jca.mvc.components.JcaColumnConfig;
import com.ptc.jca.mvc.components.JcaComponentParams;
import com.ptc.jca.mvc.components.JcaTableConfig;
import com.ptc.mvc.components.AbstractComponentBuilder;
import com.ptc.mvc.components.ComponentBuilder;
import com.ptc.mvc.components.ComponentConfig;
import com.ptc.mvc.components.ComponentConfigFactory;
import com.ptc.mvc.components.ComponentParams;
import com.ptc.mvc.util.ClientMessageSource;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.beans.NmHelperBean;
import com.ptc.windchill.enterprise.change2.ChangeTaskRoleParticipantHelper;

import ext.appo.ecn.beans.ChangeTaskBean;
import ext.appo.ecn.common.util.ChangeUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.lang.PIStringUtils;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PICoreHelper;

@ComponentBuilder("ext.appo.ecn.mvc.builder.ChangeTaskTableBuilder")
public class ChangeTaskTableBuilder extends AbstractComponentBuilder{
	
	private static final String TABLE_ID = "ext.appo.ecn.mvc.builder.ChangeTaskTableBuilder";
	
	private static final String CLASSNAME = ChangeTaskTableBuilder.class.getName() ;
	private static final Logger LOG = LogR.getLogger(CLASSNAME) ;
	
	ClientMessageSource messageChange2ClientResource = getMessageSource("ext.appo.ecn.resource.changeNoticeActionsRB");

	@Override
	public Object buildComponentData(ComponentConfig arg0, ComponentParams arg1) throws Exception {
		Collection<ChangeTaskBean> datasArray = generateChangeTaskBeans(arg1) ;
		
		// 添加原有数据
		addDatasArray(arg1, datasArray);
		
		return  datasArray;
	}

	@Override
	public ComponentConfig buildComponentConfig(ComponentParams arg0) throws WTException {
        ComponentConfigFactory componentconfigfactory = getComponentConfigFactory();
		JcaTableConfig tableConfig = (JcaTableConfig)componentconfigfactory.newTableConfig();
		
		tableConfig.setId (TABLE_ID);
		tableConfig.setLabel(this.messageChange2ClientResource.getMessage("CHANGETASK_TABLE_NAME"));
		tableConfig.setSelectable(true);
        tableConfig.setShowCount(true);
        
        NmHelperBean localNmHelperBean = ((JcaComponentParams) arg0).getHelperBean();
		NmCommandBean localNmCommandBean = localNmHelperBean.getNmCommandBean();
		boolean bool = CreateAndEditWizBean.isCreateEditWizard(localNmCommandBean);
		//if(bool){
			tableConfig.setActionModel("changeTask.table.create_remove_edit");
		//}
        
        //变更主题
		JcaColumnConfig columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig(ChangeConstants.CHANGETHEME_COMPID, true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_CHANGETHEME"));
        columnconfig.setAutoSize(true);
        columnconfig.setWidth(40);
        columnconfig.setDataUtilityId("ChangeTaskDataUtility");
        tableConfig.addComponent(columnconfig);
        
        //变更任务描述
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig(ChangeConstants.CHANGEDESCRIBE_COMPID, true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_CHANGEDESCRIBE"));
        columnconfig.setAutoSize(true);
        columnconfig.setWidth(100);
        columnconfig.setDataUtilityId("ChangeTaskDataUtility");
        tableConfig.addComponent(columnconfig);
        
        //期望完成日期
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig(ChangeConstants.NEEDDATE_COMPID, true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_NEEDDATE"));
        columnconfig.setAutoSize(true);
        columnconfig.setWidth(20);
        columnconfig.setDataUtilityId("ChangeTaskDataUtility");
        tableConfig.addComponent(columnconfig);
        
        //责任人
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig(ChangeConstants.RESPONSIBLE_COMPID, true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_RESPONSIBLE"));
        columnconfig.setAutoSize(true);
        columnconfig.setWidth(20);
        columnconfig.setDataUtilityId("ChangeTaskDataUtility");
        tableConfig.addComponent(columnconfig);
        
        //ECA对象
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig(ChangeConstants.CHANGEACTIVITY2_COMPID, true);
        columnconfig.setLabel("ECA");
        columnconfig.setAutoSize(true);
        columnconfig.setHidden(true);
        tableConfig.addComponent(columnconfig);
        
		return tableConfig;
	}
	
	/***
	 * 将ECN中所有ECA对象转换为ChangeTaskBean对象
	 * 
	 * @param params
	 * @return
	 * @throws WTException
	 */
	public Collection<ChangeTaskBean> generateChangeTaskBeans(ComponentParams params) throws WTException{
		Collection<ChangeTaskBean> datasArray = new HashSet<ChangeTaskBean>() ;
		if(params == null){
			return datasArray;
		}
		
		try {
			NmHelperBean helper = ((JcaComponentParams) params).getHelperBean();
			Object pbo = helper.getNmCommandBean().getActionOid().getRefObject() ;
			if(pbo instanceof WTChangeOrder2){
				// 将ECN中所有ECA对象转换为ChangeTaskBean对象
				for(ChangeActivityIfc changeActivityIfc : ChangeUtils.getChangeActivities((WTChangeOrder2)pbo)){
					WTChangeActivity2 eca = (WTChangeActivity2) changeActivityIfc ;
					// TODO 类型判断是否为‘事务性任务’类型
					if(!PICoreHelper.service.isType(eca, ChangeConstants.TRANSACTIONAL_CHANGEACTIVITY2)){
						continue ;
					}
					// ‘已取消’状态不录入
					if(ChangeUtils.checkState(eca, ChangeConstants.CANCELLED)){
						continue ;
					}
					ChangeTaskBean changeTaskBean = new ChangeTaskBean() ;
					changeTaskBean.setChangeTheme(eca.getName());
					changeTaskBean.setChangeDescribe(eca.getDescription());
					// ECA工作负责人
					String assigneeName = "" ;
					Enumeration<?> roleem = ChangeTaskRoleParticipantHelper.getRoleParticipants(eca, ChangeConstants.ROLE_ASSIGNEE);
					while(roleem.hasMoreElements()){
						Object object = roleem.nextElement() ;
						if(object instanceof ObjectReference){
							object = ((ObjectReference)object).getObject() ;
						}
						WTPrincipal principal = (WTPrincipal)object ;
						if(LOG.isDebugEnabled()){
							LOG.debug("principal : " + principal.getDisplayIdentity());
						}
						if(principal instanceof WTUser){
							if(PIStringUtils.isNull(assigneeName)){
								assigneeName = ((WTUser)principal).getFullName() ;
							}else{
								assigneeName = assigneeName + ";" + ((WTUser)principal).getFullName() ;
							}
						}
					}
					changeTaskBean.setResponsible(assigneeName) ;
					changeTaskBean.setChangeActivity2(PersistenceHelper.getObjectIdentifier(eca).toString());
					Object needDate = PIAttributeHelper.service.getValue(eca, ChangeConstants.COMPLETION_TIME) ;
					changeTaskBean.setNeedDate(needDate == null ? "" : (String)needDate); 
					datasArray.add(changeTaskBean) ;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace(); 
			throw new WTException(e.getLocalizedMessage()) ;
		}
		
		return datasArray ;
	}
	
	/***
	 * 添加表中原有数据
	 * 
	 * @param arg1
	 * @param datasArray
	 * @throws WTException
	 */
	public void addDatasArray(ComponentParams arg1, Collection<ChangeTaskBean> datasArray) throws WTException {
		try {
			// 新增
			NmHelperBean helper = ((JcaComponentParams) arg1).getHelperBean();
			String datasJSON = helper.getNmCommandBean().getRequest().getParameter(ChangeConstants.CHANGETASKBEAN_ID);
			if(PIStringUtils.isNotNull(datasJSON)){
				datasArray.removeAll(datasArray) ;
				JSONObject datasJSONObject = new JSONObject(datasJSON) ;
				Iterator<?> keyIterator = datasJSONObject.keys() ;
				while(keyIterator.hasNext()){
					// 对象ID
					String key = (String)keyIterator.next() ;
					// 对象数据
					JSONObject jsonObject = new JSONObject(datasJSONObject.getString(key)) ;
					ChangeTaskBean changeTaskBean = new ChangeTaskBean(key) ;
					if(jsonObject.has(ChangeConstants.CHANGETHEME_COMPID)){
						changeTaskBean.setChangeTheme(jsonObject.getString(ChangeConstants.CHANGETHEME_COMPID));
					}
					if(jsonObject.has(ChangeConstants.CHANGEDESCRIBE_COMPID)){
						changeTaskBean.setChangeDescribe(jsonObject.getString(ChangeConstants.CHANGEDESCRIBE_COMPID));
					}
					if(jsonObject.has(ChangeConstants.RESPONSIBLE_COMPID)){
						changeTaskBean.setResponsible(jsonObject.getString(ChangeConstants.RESPONSIBLE_COMPID));
					}
					if(jsonObject.has(ChangeConstants.CHANGEACTIVITY2_COMPID)){
						changeTaskBean.setChangeActivity2(jsonObject.getString(ChangeConstants.CHANGEACTIVITY2_COMPID));
					}
					if(jsonObject.has(ChangeConstants.NEEDDATE_COMPID)){
						changeTaskBean.setNeedDate(jsonObject.getString(ChangeConstants.NEEDDATE_COMPID));
					}
					datasArray.add(changeTaskBean) ;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		}
	}
}
