package ext.appo.part.builder;

import com.ptc.core.components.rendering.guicomponents.TextBox;
import com.ptc.jca.mvc.components.JcaColumnConfig;
import com.ptc.jca.mvc.components.JcaComponentParams;
import com.ptc.mvc.components.*;
import com.ptc.mvc.util.ClientMessageSource;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.beans.NmHelperBean;
import ext.appo.ecn.mvc.builder.AffectedItemsTableBuilder;
import ext.appo.util.PartUtil;
import ext.customer.common.IBAUtil;
import ext.generic.license.verify.LicenseVerify;
import ext.generic.reviewObject.cache.ExcelCacheManager;
import ext.generic.reviewObject.constant.ReviewObjectConstant;
import ext.generic.reviewObject.datautility.DataUtilityHelper;
import ext.generic.reviewObject.model.ProcessReviewObjectLinkHelper;
import ext.generic.reviewObject.model.SignedOpinion;
import ext.generic.reviewObject.util.ReviewObjectAndWorkFlowLink;
import ext.generic.reviewObject.util.ReviewObjectUtil;
import ext.lang.PIStringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import wt.change2.ChangeActivity2;
import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMDocument;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.vc.Mastered;
import wt.workflow.definer.WfProcessTemplate;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WfAssignmentState;
import wt.workflow.work.WorkItem;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import static wt.doc.LoadDoc.getDocument;

/**
 * 注册表格ID
 * 构造ReviewObjectTableBuilder表格
 * @author Yzhang
 */
@ComponentBuilder("ext.appo.part.builder.PartAttrChangeReviewObjectTableBuilder")
public class PartAttrChangeReviewObjectTableBuilder extends AbstractComponentBuilder {
	private ClientMessageSource msgSource = getMessageSource("ext.generic.reviewObject.resource.ReviewObjectResourceRB");
	private  ClientMessageSource messageChange2ClientResource = getMessageSource("ext.appo.ecn.resource.changeNoticeActionsRB");
	private static final Logger LOGGER = LogR.getLogger(PartAttrChangeReviewObjectTableBuilder.class.getName());
	private static String WFTEMPLATE_PATH = "";
	private static WTProperties wtproperties;
	private static String codebalocation;
	private static HashMap<String, List<String>> reviewObjConfig = null;
	private static final String DEFAULT = "default";

	static {
		try {
			wtproperties = WTProperties.getLocalProperties();
			WFTEMPLATE_PATH = wtproperties.getProperty("wt.home", "")
					+ ReviewObjectConstant.WFTEMPLATE_PATH;
			String sp = File.separator;
			codebalocation = wtproperties.getProperty("wt.codebase.location","");
			String excelPath = codebalocation + sp + "ext" + sp + "generic" + sp + "reviewObject" + sp + "config" + sp + "itemReviewObjectColConfig.xlsx";
			reviewObjConfig = ReviewObjectUtil.getMapFromExcel(excelPath);
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private static final String CLASSNAME = PartAttrChangeReviewObjectTableBuilder.class.getName() ;
	private static final Logger LOG = LogR.getLogger(CLASSNAME) ;

	@SuppressWarnings("all")
	@Override
	public Object buildComponentData(ComponentConfig componentConfig, ComponentParams componentparams)throws Exception {
		//增加License验证
//		LicenseVerify.verifys();
		NmHelperBean localNmHelperBean = ((JcaComponentParams) componentparams).getHelperBean();
		NmCommandBean nmcommandbean = localNmHelperBean.getNmCommandBean();
		WorkItem workItem = (WorkItem) nmcommandbean.getActionOid().getRefObject();//工作项
		WfAssignedActivity wfassignedactivity = (WfAssignedActivity) workItem.getSource().getObject();//活动模板
		WfProcess wfprocess  = wfassignedactivity.getParentProcess();//进程

		List<SignedOpinion> signedOpinionList = ExcelCacheManager.setSignedOpinionCacheFromDB(wfprocess);//随签意见
		HashMap map = nmcommandbean.getMap();//  获取页面信息
		map.put("signedOpinion", signedOpinionList);
        System.out.println(" ReviewObjectDataUtility.setRole ");
		
		DataUtilityHelper dataUtilityHelper = new DataUtilityHelper(nmcommandbean,wfprocess);
		dataUtilityHelper.getCountersignRole();
		dataUtilityHelper.getUsersByRole();
		
		WTArrayList wtList=new WTArrayList();
		// 获取新增数据
		HttpServletRequest request = nmcommandbean.getRequest();
		//获取选择的部件Oid
		String selectOids = request.getParameter("selectOids");
		if(PIStringUtils.isNotNull(selectOids)){
			JSONArray jsonArray = new JSONArray(selectOids) ;
			for (int i = 0; i < jsonArray.length(); i++) {
				wtList.add((new ReferenceFactory()).getReference(jsonArray.getString(i)).getObject()) ;
			}
		}

		//将新增的数据添加到随签对象
		if (wfprocess != null) {
			for(int i = 0; i < wtList.size(); ++i) {
				WTObject wtobj = (WTObject)wtList.getPersistable(i);
				Mastered mastered = ((RevisionControlled)wtobj).getMaster();
				if (!ReviewObjectUtil.queryIsEqual(wfprocess, wtobj)) {
					try {
						ProcessReviewObjectLinkHelper.service.newProcessorVersionLink(wfprocess, wtobj, (WTObject)mastered);
					} catch (WTPropertyVetoException e) {
						e.printStackTrace();
					}
				}
			}
		}

		WTArrayList list = ReviewObjectAndWorkFlowLink.getReviewObjectByProcess(wfprocess);
		if(list == null){
			list = new WTArrayList();
		}
        System.out.println("buildComponentData : list : = " + list);
		return list;
	}

	@Override
	public ComponentConfig buildComponentConfig(ComponentParams componentParams) throws WTException {

		NmHelperBean localNmHelperBean = ((JcaComponentParams) componentParams).getHelperBean();
		NmCommandBean nmcommandbean = localNmHelperBean.getNmCommandBean();
		WorkItem workItem = (WorkItem) nmcommandbean.getActionOid().getRefObject();
		WfAssignedActivity wfassignedactivity = (WfAssignedActivity) workItem.getSource().getObject();
		WfProcess wfprocess  = wfassignedactivity.getParentProcess();

		ComponentConfigFactory factory = getComponentConfigFactory();
		TableConfig result = factory.newTableConfig();
		result.setConfigurable(false);
		result.setLabel(msgSource.getMessage("REVIEWOBJECT_SIGNOBJECT"));

		WfAssignmentState status = workItem.getStatus();
		WTObject pbo = (WTObject) (wfassignedactivity.getContext().getValue("primaryBusinessObject"));
		if(!status.equals(WfAssignmentState.COMPLETED) ) {
			if(pbo instanceof WTPart){
				result.setActionModel("partAttrChangeTableToolbar");
			}else if(pbo instanceof WTDocument|| pbo instanceof EPMDocument){
				result.setActionModel("partAttrChangeTableToolbar");
			}else if(pbo instanceof ChangeActivity2){
				result.setActionModel("partAttrChangeTableToolbar");
			}
			
		}
		result.setSelectable(true);
		
		List<String> columnList = new ArrayList<String>();
		
		WfProcessTemplate wfprocesstemplate = (WfProcessTemplate) wfprocess.getTemplate().getObject();
		String templateName = wfprocesstemplate.getName();
		System.out.println("流程模板名："+templateName);
		if (reviewObjConfig.containsKey(templateName)) {
			columnList = reviewObjConfig.get(templateName);	
		} else  if(reviewObjConfig.containsKey(DEFAULT)){	
			columnList = reviewObjConfig.get(DEFAULT);
		} 
		
		LOGGER.debug("获取到的list:"+columnList);
		
	    for (String column : columnList){
	      String strColumnConfig = column;
	      if ("type_icon".equalsIgnoreCase(column)) {
	        strColumnConfig = "type";
	      } else if (column.equalsIgnoreCase("createStamp")){
		        strColumnConfig = "thePersistInfo.createStamp";
	      } else if (column.equalsIgnoreCase("modifyStamp")){
		        strColumnConfig = "thePersistInfo.modifyStamp";
	      }
	      ColumnConfig columnConfig = factory.newColumnConfig(strColumnConfig, true);
	      if ("type_icon".equalsIgnoreCase(column)){
	        columnConfig.setDataUtilityId("type_icon");
	      } else if ("new_version".equals(column)){
	        columnConfig.setLabel(this.msgSource.getMessage("NEW_VERSION"));
	        columnConfig.setDataUtilityId("reviewObjectDataUtility");
	        columnConfig.setAutoSize(true);
	      } else if ("ChangeReason".equals(column)){
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
//	      else if ("SpecNo".equals(column)){
//			  columnConfig.setLabel(this.msgSource.getMessage("SPECNO"));
//			  columnConfig.setDataUtilityId("newReviewObjectDataUtility");
//			  columnConfig.setAutoSize(true);
//		  }
	      else if ("modifier".equals(column)) {
	        columnConfig.setLabel(this.msgSource.getMessage("MODIFIER"));
	      } else if ("source".equals(column)){      
	        columnConfig.setLabel(this.msgSource.getMessage("SOURCE"));
	      } else if("versionInfo.identifier.versionId".equals(column)){
	    	  columnConfig.setLabel(this.msgSource.getMessage("VERSIONINFO_IDENTIFIER_VERSIONID"));
	      } else if("iterationInfo.creator".equals(column)){
	    	  columnConfig.setLabel(this.msgSource.getMessage("ITERATIONINFO_CREATOR"));
	      } else if("iterationInfo.modifier".equals(column)){
	    	  columnConfig.setLabel(this.msgSource.getMessage("ITERATIONINFO_MODIFIER"));
	      } else if("state.state".equals(column)){
	    	  columnConfig.setLabel(this.msgSource.getMessage("STATE_STATE"));
	      }else if("Classification".equals(column)){
              columnConfig.setLabel(this.msgSource.getMessage("CLASSIFICATION"));
          }
	      
	      if (strColumnConfig.equals("cpzt")) {
	    	  columnConfig.setDataUtilityId("itemStageControlDataUtility");
	      }
	      
	      columnConfig.setWidth(50);
	      columnConfig.setVariableHeight(true);
	      
	      
	      result.addComponent(columnConfig);
	    }
		
		
		//获取流程模板的名称
		String workFlowNameXML = wfprocess.getTemplate().getName();
		LOGGER.debug("workFlowNameXML: =" + workFlowNameXML);
		//从缓存中获取
		Hashtable<String,String> signedOpinionList = ExcelCacheManager.getWorkFlowExcelFromCache(workFlowNameXML);
		LOGGER.debug("SignedOpinionList: =" + signedOpinionList);
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
