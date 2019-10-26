package ext.appo.part.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import com.ptc.jca.mvc.components.JcaComponentParams;
import com.ptc.mvc.components.AbstractComponentBuilder;
import com.ptc.mvc.components.ColumnConfig;
import com.ptc.mvc.components.ComponentBuilder;
import com.ptc.mvc.components.ComponentConfig;
import com.ptc.mvc.components.ComponentConfigFactory;
import com.ptc.mvc.components.ComponentParams;
import com.ptc.mvc.components.TableConfig;
import com.ptc.mvc.util.ClientMessageSource;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.beans.NmHelperBean;

import ext.generic.license.verify.LicenseVerify;
import ext.generic.reviewObject.cache.ExcelCacheManager;
import ext.generic.reviewObject.constant.ReviewObjectConstant;
import ext.generic.reviewObject.datautility.DataUtilityHelper;
import ext.generic.reviewObject.model.SignedOpinion;
import ext.generic.reviewObject.util.ReviewObjectAndWorkFlowLink;
import ext.generic.reviewObject.util.ReviewObjectUtil;
import wt.change2.ChangeActivity2;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.workflow.definer.WfProcessTemplate;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WfAssignmentState;
import wt.workflow.work.WorkItem;

/**
 * 注册表格ID 构造ReviewObjectTableBuilder表格
 * 
 * @author Yzhang
 */
@ComponentBuilder("ext.appo.part.builder.cisReviewObjectTableBuilder")
public class CisReviewObjectTableBuilder extends AbstractComponentBuilder {
	private ClientMessageSource msgSource = getMessageSource(
			"ext.generic.reviewObject.resource.ReviewObjectResourceRB");
	private static final Logger LOGGER = LogR.getLogger(CisReviewObjectTableBuilder.class.getName());
	private static String WFTEMPLATE_PATH = "";
	private static WTProperties wtproperties;
	private static String codebalocation;
	private static HashMap<String, List<String>> reviewObjConfig = null;
	private static final String DEFAULT = "default";

	static {
		try {
			wtproperties = WTProperties.getLocalProperties();
			WFTEMPLATE_PATH = wtproperties.getProperty("wt.home", "") + ReviewObjectConstant.WFTEMPLATE_PATH;
			String sp = File.separator;
			codebalocation = wtproperties.getProperty("wt.codebase.location", "");
			String excelPath = codebalocation + sp + "ext" + sp + "generic" + sp + "reviewObject" + sp + "config" + sp
					+ "cisReviewObjectColConfig.xlsx";
			reviewObjConfig = ReviewObjectUtil.getMapFromExcel(excelPath);
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@SuppressWarnings("all")
	@Override
	public Object buildComponentData(ComponentConfig componentConfig, ComponentParams componentparams)
			throws Exception {
		// 增加License验证
		LicenseVerify.verifys();
		NmHelperBean localNmHelperBean = ((JcaComponentParams) componentparams).getHelperBean();
		NmCommandBean nmcommandbean = localNmHelperBean.getNmCommandBean();
		WorkItem workItem = (WorkItem) nmcommandbean.getActionOid().getRefObject();// 工作项
		WfAssignedActivity wfassignedactivity = (WfAssignedActivity) workItem.getSource().getObject();// 活动模板
		WfProcess wfprocess = wfassignedactivity.getParentProcess();// 进程

		List<SignedOpinion> signedOpinionList = ExcelCacheManager.setSignedOpinionCacheFromDB(wfprocess);// 随签意见
		HashMap map = nmcommandbean.getMap();// 获取页面信息
		map.put("signedOpinion", signedOpinionList);
		LOGGER.debug(" ReviewObjectDataUtility.setRole ");

		DataUtilityHelper dataUtilityHelper = new DataUtilityHelper(nmcommandbean, wfprocess);
		dataUtilityHelper.getCountersignRole();
		dataUtilityHelper.getUsersByRole();

		WTArrayList list = ReviewObjectAndWorkFlowLink.getReviewObjectByProcess(wfprocess);
		if (list == null) {
			list = new WTArrayList();
		}
		LOGGER.debug("buildComponentData : list : = " + list);
		return list;
	}

	@Override
	public ComponentConfig buildComponentConfig(ComponentParams componentParams) throws WTException {

		NmHelperBean localNmHelperBean = ((JcaComponentParams) componentParams).getHelperBean();
		NmCommandBean nmcommandbean = localNmHelperBean.getNmCommandBean();
		WorkItem workItem = (WorkItem) nmcommandbean.getActionOid().getRefObject();
		WfAssignedActivity wfassignedactivity = (WfAssignedActivity) workItem.getSource().getObject();
		WfProcess wfprocess = wfassignedactivity.getParentProcess();

		ComponentConfigFactory factory = getComponentConfigFactory();
		TableConfig result = factory.newTableConfig();
		result.setConfigurable(false);
		result.setLabel(msgSource.getMessage("REVIEWOBJECT_SIGNOBJECT"));

		WfAssignmentState status = workItem.getStatus();
		WTObject pbo = (WTObject) (wfassignedactivity.getContext().getValue("primaryBusinessObject"));
		if (!status.equals(WfAssignmentState.COMPLETED)) {
			if (pbo instanceof WTPart) {
				result.setActionModel("reviewObjectTableToolbar");
			} else if (pbo instanceof WTDocument || pbo instanceof EPMDocument) {
				result.setActionModel("reviewObjectTableDocToolbar");
			} else if (pbo instanceof ChangeActivity2) {
				result.setActionModel("reviewObjectTableToolbar");
			}

		}
		result.setSelectable(true);

		List<String> columnList = new ArrayList<String>();

		WfProcessTemplate wfprocesstemplate = (WfProcessTemplate) wfprocess.getTemplate().getObject();
		String templateName = wfprocesstemplate.getName();
		LOGGER.debug("流程模板名：" + templateName);
		if (reviewObjConfig.containsKey(templateName)) {
			columnList = reviewObjConfig.get(templateName);
		} else if (reviewObjConfig.containsKey(DEFAULT)) {
			columnList = reviewObjConfig.get(DEFAULT);
		}

		LOGGER.debug("获取到的list:" + columnList);

		for (String column : columnList) {
			String strColumnConfig = column;
			if ("type_icon".equalsIgnoreCase(column)) {
				strColumnConfig = "type";
			} else if (column.equalsIgnoreCase("createStamp")) {
				strColumnConfig = "thePersistInfo.createStamp";
			} else if (column.equalsIgnoreCase("modifyStamp")) {
				strColumnConfig = "thePersistInfo.modifyStamp";
			}
			ColumnConfig columnConfig = factory.newColumnConfig(strColumnConfig, true);
			if ("type_icon".equalsIgnoreCase(column)) {
				columnConfig.setDataUtilityId("type_icon");
			} else if ("new_version".equals(column)) {
				columnConfig.setLabel(this.msgSource.getMessage("NEW_VERSION"));
				columnConfig.setDataUtilityId("reviewObjectDataUtility");
			} else if ("modifier".equals(column)) {
				columnConfig.setLabel(this.msgSource.getMessage("MODIFIER"));
			} else if ("source".equals(column)) {
				columnConfig.setLabel(this.msgSource.getMessage("SOURCE"));
			} else if ("versionInfo.identifier.versionId".equals(column)) {
				columnConfig.setLabel(this.msgSource.getMessage("VERSIONINFO_IDENTIFIER_VERSIONID"));
			} else if ("iterationInfo.creator".equals(column)) {
				columnConfig.setLabel(this.msgSource.getMessage("ITERATIONINFO_CREATOR"));
			} else if ("iterationInfo.modifier".equals(column)) {
				columnConfig.setLabel(this.msgSource.getMessage("ITERATIONINFO_MODIFIER"));
			} else if ("state.state".equals(column)) {
				columnConfig.setLabel(this.msgSource.getMessage("STATE_STATE"));
			}

			if (strColumnConfig.equals("footprintref2") || strColumnConfig.equals("footprintref")
					|| strColumnConfig.equals("libraryref") || strColumnConfig.equals("datasheet")) {
				columnConfig.setDataUtilityId("cisReviewObjectTableAttributeDataUtility");
			}

			columnConfig.setWidth(40);
			columnConfig.setVariableHeight(true);

			result.addComponent(columnConfig);
		}

		// 获取流程模板的名称
		String workFlowNameXML = wfprocess.getTemplate().getName();
		LOGGER.debug("workFlowNameXML: =" + workFlowNameXML);
		// 从缓存中获取
		Hashtable<String, String> signedOpinionList = ExcelCacheManager.getWorkFlowExcelFromCache(workFlowNameXML);
		LOGGER.debug("SignedOpinionList: =" + signedOpinionList);
		// 在随签意见之前添加一列随签意见
		if (signedOpinionList != null && signedOpinionList.size() > 0) {
			// 添加一列历史签审意见
			ColumnConfig signsColumn = factory.newColumnConfig("reviewObjSignOpinions", /* "随签意见", */ true);
			signsColumn.setLabel(msgSource.getMessage("REVIEWOBJ_SIGN_LABLE"));
			signsColumn.setVariableHeight(true);
			signsColumn.setDataUtilityId("reviewObjectDataUtility");
			result.addComponent(signsColumn);
		}
		if (signedOpinionList == null || signedOpinionList.size() == 0) {
			ExcelCacheManager.setWorkFlowExcelCache(WFTEMPLATE_PATH, workFlowNameXML);
			signedOpinionList = ExcelCacheManager.getWorkFlowExcelFromCache(workFlowNameXML);
			if (signedOpinionList == null || signedOpinionList.size() == 0) {
				return result;
			}
		}
		for (String signedOpinion : signedOpinionList.keySet()) {
			result.addComponent(signedOpinionColumn(signedOpinion, factory));
		}
		return result;
	}

	/**
	 * 动态添加列
	 * 
	 * @param signedOpinion
	 *            列名
	 * @param componentConfigFactory
	 *            com
	 * @return ColumnConfig
	 */
	private ColumnConfig signedOpinionColumn(String signedOpinion, ComponentConfigFactory componentConfigFactory) {
		ColumnConfig signedOpinion_column = componentConfigFactory.newColumnConfig(signedOpinion + "__SignedOpinion",
				true);
		signedOpinion_column.setLabel(signedOpinion);
		signedOpinion_column.setVariableHeight(true);
		signedOpinion_column.setDataUtilityId("reviewObjectDataUtility");
		return signedOpinion_column;
	}

}
