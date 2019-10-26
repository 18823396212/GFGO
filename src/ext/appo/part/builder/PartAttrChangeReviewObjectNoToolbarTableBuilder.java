package ext.appo.part.builder;

import com.ptc.jca.mvc.components.JcaComponentConfigFactory;
import com.ptc.jca.mvc.components.JcaComponentParams;
import com.ptc.jca.mvc.components.JcaTableConfig;
import com.ptc.mvc.components.*;
import com.ptc.mvc.components.ds.DataSourceMode;
import com.ptc.mvc.util.ClientMessageSource;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.beans.NmHelperBean;
import ext.generic.license.verify.LicenseVerify;
import ext.generic.reviewObject.cache.ExcelCacheManager;
import ext.generic.reviewObject.constant.ReviewObjectConstant;
import ext.generic.reviewObject.datautility.DataUtilityHelper;
import ext.generic.reviewObject.model.ProcessReviewObjectLink;
import ext.generic.reviewObject.model.ProcessReviewObjectLinkHelper;
import ext.generic.reviewObject.model.SignedOpinion;
import ext.generic.reviewObject.util.ReviewObjectUtil;
import org.apache.log4j.Logger;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.workflow.definer.WfProcessTemplate;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;

import javax.xml.registry.infomodel.Classification;
import java.io.File;
import java.util.*;

/**
 * 注册表格ID
 * 构造ReviewObjectTableBuilder表格
 * @author Yzhang
 */
@ComponentBuilder("ext.appo.part.builder.PartAttrChangeReviewObjectNoToolbarTableBuilder")
public class PartAttrChangeReviewObjectNoToolbarTableBuilder extends AbstractComponentBuilder {
	private ClientMessageSource msgSource = getMessageSource("ext.generic.reviewObject.resource.ReviewObjectResourceRB");
	private  ClientMessageSource messageChange2ClientResource = getMessageSource("ext.appo.ecn.resource.changeNoticeActionsRB");
	private static final Logger LOGGER = LogR.getLogger(PartAttrChangeReviewObjectNoToolbarTableBuilder.class.getName());
	private static String WFTEMPLATE_PATH = "";
	private static WTProperties wtproperties;
	private static String codebalocation;
	private static HashMap<String, List<String>> reviewObjConfig = null;
	private static final String DEFAULT = "default";

	static {
		try {
			wtproperties = WTProperties.getLocalProperties();
			WFTEMPLATE_PATH = wtproperties.getProperty("wt.home", "")
					+ ReviewObjectConstant.WFTEMPLATE_PATH;// Windchill路径
			String sp = File.separator;
			codebalocation = wtproperties.getProperty("wt.codebase.location","");
			String excelPath = codebalocation + sp + "ext" + sp + "generic" + sp + "reviewObject" + sp + "config" + sp + "itemReviewObjectColConfig.xlsx";
			reviewObjConfig = ReviewObjectUtil.getMapFromExcel(excelPath);
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		}
	}

	
	@SuppressWarnings("all")
	@Override
	public Object buildComponentData(ComponentConfig componentConfig, ComponentParams componentparams)throws Exception {
		//增加License验证
//		LicenseVerify.verifys();
		
		NmHelperBean localNmHelperBean = ((JcaComponentParams) componentparams).getHelperBean();
		NmCommandBean nmcommandbean = localNmHelperBean.getNmCommandBean();
		WorkItem workItem = (WorkItem) nmcommandbean.getActionOid().getRefObject();
		
		WfAssignedActivity wfassignedactivity = (WfAssignedActivity) workItem.getSource().getObject();
		WfProcess wfprocess  = wfassignedactivity.getParentProcess();
		List<SignedOpinion> signedOpinionList = ExcelCacheManager.setSignedOpinionCacheFromDB(wfprocess);
		HashMap map = nmcommandbean.getMap();
		map.put("signedOpinion", signedOpinionList);
		DataUtilityHelper dataUtilityHelper = new DataUtilityHelper(nmcommandbean,wfprocess);
		dataUtilityHelper.getCountersignRole();
		dataUtilityHelper.getUsersByRole();
		List<String> oldAssigneeList = dataUtilityHelper.assignmentHistory();
		WTPrincipal principal = SessionHelper.getPrincipal();

		List<WTObject> reviewObjList = new ArrayList<WTObject>();
		ProcessReviewObjectLink link = null;
		if(wfprocess != null){
			QueryResult queryresult = ProcessReviewObjectLinkHelper.service.getProcessReviewObjectLinkByRoleA(wfprocess);
			LOGGER.debug("queryresult==="+queryresult.size());
			while (queryresult != null && queryresult.hasMoreElements()) {
				link = (ProcessReviewObjectLink) queryresult.nextElement();
				LOGGER.debug("link==="+link);
				WTObject obj = (WTObject)link.getRoleBObject();
				LOGGER.debug("obj==="+obj);
				String userName = link.getStandby1();
				if(!(dataUtilityHelper.isSign(wfassignedactivity.getName(),dataUtilityHelper.isSignHashtable(wfprocess))) || userName == null || userName.isEmpty() ){
					reviewObjList.add(obj);
				}else{
					if( userName.equals(principal.getName() + "_" + ((WTUser)principal).getFullName()) || oldAssigneeList.contains(userName)){		
						reviewObjList.add(obj);
					}
				}
			}
		}

		return reviewObjList;
	}

	@Override
	public ComponentConfig buildComponentConfig(ComponentParams componentParams) throws WTException {

		NmHelperBean localNmHelperBean = ((JcaComponentParams) componentParams).getHelperBean();
		NmCommandBean nmcommandbean = localNmHelperBean.getNmCommandBean();
		WorkItem workItem = (WorkItem) nmcommandbean.getActionOid().getRefObject();
		WfAssignedActivity wfassignedactivity = (WfAssignedActivity) workItem.getSource().getObject();
		WfProcess wfprocess  = wfassignedactivity.getParentProcess();

		ComponentConfigFactory factory = getComponentConfigFactory();
		//modify by zhujian 解决 MVC Table失填值会被刷掉的问题
		JcaComponentConfigFactory factory2 = JcaComponentConfigFactory.getInstance();
		LOGGER.debug("防止数据丢失，使用JcaTableConfig");
		//TableConfig result = factory.newTableConfig();
		JcaTableConfig result = factory2.newTableConfig();
		result.setDataSourceMode(DataSourceMode.SYNCHRONOUS);
		result.setPtypes(Arrays.asList(new String[] {"detailspopupgrid", "wizardgrid", "gridfileinputhandler" }));
		
		result.setConfigurable(false);
		result.setLabel(msgSource.getMessage("REVIEWOBJECT_SIGNOBJECT"));
		result.setSelectable(true);
		
		List<String> columnList = new ArrayList<String>();
		
		WfProcessTemplate wfprocesstemplate = (WfProcessTemplate) wfprocess.getTemplate().getObject();
		String templateName = wfprocesstemplate.getName();
		LOGGER.debug("流程模板名："+templateName);
		
		if (reviewObjConfig.containsKey(templateName)) {
			columnList = reviewObjConfig.get(templateName);		
		} else  if(reviewObjConfig.containsKey(DEFAULT)){		
			columnList = reviewObjConfig.get(DEFAULT);		
		}
		
		for(String column : columnList){
			String strColumnConfig = column;
			if("type_icon".equalsIgnoreCase(column)){
				strColumnConfig = "type";
			}else if(column.equalsIgnoreCase("createStamp")){
				strColumnConfig = "thePersistInfo.createStamp";
			}else if(column.equalsIgnoreCase("modifyStamp")){
				strColumnConfig = "thePersistInfo.modifyStamp";
			}
			
			
			ColumnConfig  columnConfig= factory.newColumnConfig(strColumnConfig, true);
			//设置显示
			if("type_icon".equalsIgnoreCase(column)){
				columnConfig.setDataUtilityId("type_icon");
			}else if("new_version".equals(column)){
				columnConfig.setLabel(msgSource.getMessage("NEW_VERSION"));
				columnConfig.setDataUtilityId("reviewObjectDataUtility");
			}else if("modifier".equals(column)){
				columnConfig.setLabel(msgSource.getMessage("MODIFIER"));
			}else if("source".equals(column)){
				columnConfig.setLabel(msgSource.getMessage("SOURCE"));
			}else if ("ChangeReason".equals(column)){
				columnConfig.setLabel(this.msgSource.getMessage("CHANGEREASON"));
				columnConfig.setDataUtilityId("newReviewObjectDataUtility");
				columnConfig.setAutoSize(true);
			}else if ("PreChangeContent".equals(column)){
				columnConfig.setLabel(this.msgSource.getMessage("PRECHANGECONTENT"));
				columnConfig.setDataUtilityId("newReviewObjectDataUtility");
				columnConfig.setAutoSize(true);
			}else if ("PostChangeContent".equals(column)){
				columnConfig.setLabel(this.msgSource.getMessage("POSTCHANGECONTENT"));
				columnConfig.setDataUtilityId("newReviewObjectDataUtility");
				columnConfig.setAutoSize(true);
			}
//			else if ("SpecNo".equals(column)){
//				columnConfig.setLabel(this.msgSource.getMessage("SPECNO"));
//				columnConfig.setDataUtilityId("newReviewObjectDataUtility");
//				columnConfig.setAutoSize(true);
//			}
			else if("Classification".equals(column)){
				columnConfig.setLabel(this.msgSource.getMessage("CLASSIFICATION"));
			}
			
		      if (column.equals("cpzt")) {
		    	  columnConfig.setDataUtilityId("itemStageControlDataUtility");
		      }
			
		      
		      columnConfig.setWidth(50);
		      columnConfig.setVariableHeight(true);
			
			result.addComponent(columnConfig);
			
		}
				
				
		//签审活动
		//获取流程模板的名称

		String workFlowNameXML = wfprocess.getTemplate().getName();
		LOGGER.debug("workFlowNameXML: =" + workFlowNameXML);
		//从缓存中获取
		Hashtable<String,String> signedOpinionList = ExcelCacheManager.getWorkFlowExcelFromCache(workFlowNameXML);
		LOGGER.debug("buildComponentConfig SignedOpinionList: =" + signedOpinionList);
		//在随签意见之前添加一列随签意见
		if(signedOpinionList != null && signedOpinionList.size()>0){
			//添加一列历史签审意见
			ColumnConfig signsColumn =  factory.newColumnConfig("reviewObjSignOpinions",/* "随签意见",*/ true);
			signsColumn.setLabel(msgSource.getMessage("REVIEWOBJ_SIGN_LABLE"));
			signsColumn.setVariableHeight(true);
			signsColumn.setDataUtilityId("reviewObjectDataUtility");
			result.addComponent(signsColumn);
		}
		if(signedOpinionList == null || signedOpinionList.size() == 0){
			ExcelCacheManager.setWorkFlowExcelCache(WFTEMPLATE_PATH, workFlowNameXML);
			signedOpinionList = ExcelCacheManager.getWorkFlowExcelFromCache(workFlowNameXML);
			if(signedOpinionList == null || signedOpinionList.size() == 0){
				return result;
			}
		}
		for(String signedOpinion : signedOpinionList.keySet()){
			result.addComponent(signedOpinionColumn(signedOpinion,factory));
		}
		return result;
	}

	/**
	 * 动态添加列
	 * @param signedOpinion 列名
	 * @param componentConfigFactory com
	 * @return ColumnConfig
	 */
	private ColumnConfig signedOpinionColumn(String signedOpinion,ComponentConfigFactory componentConfigFactory){
		ColumnConfig signedOpinion_column = componentConfigFactory.newColumnConfig(signedOpinion + "__SignedOpinion", true);
		signedOpinion_column.setLabel(signedOpinion);
		signedOpinion_column.setVariableHeight(true);
		signedOpinion_column.setDataUtilityId("reviewObjectDataUtility");
		return signedOpinion_column;
	}

}
