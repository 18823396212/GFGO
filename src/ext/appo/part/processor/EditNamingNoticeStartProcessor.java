package ext.appo.part.processor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import wt.access.AccessPermission;
import wt.doc.WTDocument;
import wt.fc.Identified;
import wt.fc.IdentityHelper;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.ReferenceFactory;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.Source;
import wt.part.WTPart;
import wt.part.WTPartMasterIdentity;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.views.ViewHelper;
import wt.vc.views.ViewReference;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.lwc.common.view.AttributeDefinitionReadView;
import com.ptc.core.lwc.common.view.ConstraintDefinitionReadView;
import com.ptc.core.lwc.common.view.ConstraintDefinitionReadView.RuleDataObject;
import com.ptc.core.lwc.common.view.EnumerationDefinitionReadView;
import com.ptc.core.lwc.common.view.EnumerationEntryReadView;
import com.ptc.core.lwc.common.view.TypeDefinitionReadView;
import com.ptc.core.lwc.server.LWCEnumerationBasedConstraint;
import com.ptc.core.meta.common.DiscreteSet;
import com.ptc.core.meta.container.common.impl.DiscreteSetConstraint;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.appo.ecn.common.util.ChangeWorkFlowUtils;
import ext.appo.part.beans.ProductLineConfigBean;
import ext.appo.part.workflow.PartWorkflowUtil;
import ext.appo.part.workflow.ReadExcelData;
import ext.com.iba.IBAUtil;
import ext.com.workflow.WorkflowUtil;
import ext.lang.PIStringUtils;
import ext.pi.PIException;
import ext.pi.core.PIAccessHelper;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIClassificationHelper;

/***
 * 产品命名单据编辑调用
 * 
 * @author Administrator
 *
 */
public class EditNamingNoticeStartProcessor extends DefaultObjectFormProcessor {
	private static String CLASSNAME = EditNamingNoticeStartProcessor.class.getName();
	private static final Logger LOGGER = LogR.getLogger(CLASSNAME);

	@Override
	public FormResult postProcess(NmCommandBean nmCommandBean, List<ObjectBean> arg1) throws WTException {

		LOGGER.debug("编辑产品命名通知单！！！！！");
		NmOid pageOid = nmCommandBean.getPageOid();
		WTDocument doc = (WTDocument) pageOid.getRefObject();
		LOGGER.debug("doc====" + doc.getDisplayIdentity());
		// 获取文档存储位置
		Folder folder = doc.getFolderingInfo().getFolder() ;
		if(folder == null){
			Persistable persistable = doc.getFolderingInfo().getParentFolder().getObject() ;
			if(persistable != null && persistable instanceof Folder){
				folder = (Folder)persistable ;
			}
		}
		if(folder != null){
			LOGGER.debug("Folder : " + folder.getFolderPath());
		}
		
		String productLine = (String) PIAttributeHelper.service.getValue(doc, "sscpx");
		LOGGER.debug("productLine=====" + productLine);
		String project = (String) PIAttributeHelper.service.getValue(doc, "ssxm");
		LOGGER.debug("project=====" + project);
		Map<String, String[]> parameterMap = nmCommandBean.getRequest().getParameterMap();
		String[] datasArray = parameterMap.get("datasArray");
		String[] removeArray = parameterMap.get("removeArray");
		String value = datasArray[0];
		String removeValue = removeArray[0];
		// 收集创建的部件
		Vector newArray = new Vector() ;
		try {
			// 收集用户填写时，输入内容不符合系统定义的信息
			StringBuilder errorMsg = new StringBuilder() ;
			if (PIStringUtils.isNotNull(value)) {
				JSONObject jsonObject = new JSONObject(value);
				Iterator keys = jsonObject.keys();
				while (keys.hasNext()) {
					JSONObject valueJson = new JSONObject(jsonObject.getString((String) keys.next()));
					LOGGER.debug("valueJson=====" + valueJson.toString());
					String partOid = "";
					if(valueJson.has("partOid")){
						partOid = (String) valueJson.get("partOid") ;
					}
					if (PIStringUtils.isNull(partOid)) {
						WTPart part = WTPart.newWTPart();
						// 设置文件夹
						LOGGER.debug("Folder : " + folder);
						if(folder != null){
							FolderHelper.assignLocation((FolderEntry)part, folder) ;
						}else{
							part.setContainerReference(doc.getContainerReference());
						}
						// 设置‘源’
						Source source = Source.toSource("make") ;
						part.setSource(source);
						// 设置‘视图’
						part.setView(ViewReference.newViewReference(ViewHelper.service.getView("Design")));
						// 设置部件名称
						String name = (String) valueJson.get("name");
						part.setName(name);
						// 设置‘是否成品’
						part.setEndItem(true);
						part = (WTPart) PersistenceHelper.manager.save(part);
						// 分类初始化
						String partClassification = productLine ;
						// 读取‘所属产品线’属性配置
						List<ProductLineConfigBean> productLineConfigArray = ReadExcelData.getProductLineConfig() ;
						for(ProductLineConfigBean bean : productLineConfigArray){
							if(bean.getEnumName().equalsIgnoreCase(partClassification)){
								if(PIStringUtils.isNotNull(bean.getNodeName())){
									partClassification = bean.getNodeName() ;
								}
								break ;
							}
						}
						// 设置分类
						IBAUtil.forceSetIBAValue(part, "Classification", partClassification);
						PartWorkflowUtil util = new PartWorkflowUtil() ;
						// 设置物料所属产品线
						String partProductLine = "" ;
						Map<ArrayList<String>, ArrayList<String>> returnMap = util.getEnumeratedMap("sscpx") ;
						for(Map.Entry<ArrayList<String>, ArrayList<String>> entryMap : returnMap.entrySet()){
							// 内部名称
							ArrayList<String> interiorArray = entryMap.getKey() ;
							for (int i = 0; i < interiorArray.size(); i++) {
								if(interiorArray.get(i).equals(productLine)){
									partProductLine = entryMap.getValue().get(i) ;
									break ;
								}
							}
						}
						if(PIStringUtils.isNotNull(partProductLine)){
							IBAUtil.forceSetIBAValue(part, "sscpx", partProductLine);
						}
						IBAUtil.forceSetIBAValue(part, "ssxm", project);
						Iterator keys2 = valueJson.keys();
						while (keys2.hasNext()) {
							String attri = (String) keys2.next();
							String attriValue = valueJson.getString(attri);
							if (attriValue.equals("null")) {
								attriValue = "";
							}
							if(attriValue.startsWith("[") && attriValue.endsWith("]")){
								attriValue = attriValue.substring(attriValue.indexOf("[") + 1, attriValue.length() - 1) ;
							}
							if (!attri.equals("name") && !attri.equals("partOid") && !attri.equals("sscpx")) {
								// 检查属性值是否满足合法值列表
								attriValue = checkAttributeValue(productLine, attri, attriValue, errorMsg);
								// 设置IBA属性
								IBAUtil.forceSetIBAValue(part, attri, attriValue);
							}
						}

						// 设置编码
						CusCreatePartAndCADDocFormProcessor partProcessor = new CusCreatePartAndCADDocFormProcessor();
						partProcessor.setNumber(part);

						GenerateProductStructureProcessor processor = new GenerateProductStructureProcessor();
						processor.createPartDocLink(part, doc);
						
						newArray.add(part) ;
					} else {
						WTPart part = (WTPart) ((new ReferenceFactory()).getReference(partOid).getObject());
						String name = (String) valueJson.get("name");
						// 设置部件名称
						setWTPartName(part, name);
						Iterator keys2 = valueJson.keys();
						while (keys2.hasNext()) {
							String attri = (String) keys2.next();
							String attriValue = valueJson.getString(attri);
							if (attriValue.equals("null")) {
								attriValue = "";
							}
							if(attriValue.startsWith("[") && attriValue.endsWith("]")){
								attriValue = attriValue.substring(attriValue.indexOf("[") + 1, attriValue.length() - 1) ;
							}
							if (!attri.equals("name") && !attri.equals("partOid") && !attri.equals("ssxcp")) {
								// 检查属性值是否满足合法值列表
								attriValue = checkAttributeValue(productLine, attri, attriValue, errorMsg);
								// 设置IBA属性
								IBAUtil.forceSetIBAValue(part, attri, attriValue);
							}
						}
					}
					if(newArray != null && newArray.size() > 0){
						Vector refVector = new Vector() ;
						// 赋予设置状态权限
						ChangeWorkFlowUtils.grantPersistablePermissions(newArray, (WTUser)SessionHelper.manager.getPrincipal(), 15);
						WorkflowUtil.setLifeCycleState(newArray, "UNDERREVIEW");
						for(Object object : newArray){
							refVector.add(PersistenceHelper.manager.refresh((Persistable)object)) ;
						}
						newArray = refVector ;
					}
				}
			}
			
			if(errorMsg.length() > 0){
				throw new WTException(errorMsg.toString()) ;
			}

			if (PIStringUtils.isNotNull(removeValue)) {
				SessionContext previous = SessionContext.newContext();
				try {
					// 当前用户设置为管理员，用于忽略权限
					SessionHelper.manager.setAdministrator();
					
					JSONObject removeObject = new JSONObject(removeValue);
					LOGGER.debug("removeObject=====" + removeObject.toString());
					Iterator keys2 = removeObject.keys();
					while (keys2.hasNext()) {
						String partOid = removeObject.getString((String) keys2.next());
						if (PIStringUtils.isNotNull(partOid)) {
							WTPart part = (WTPart) ((new ReferenceFactory()).getReference(partOid).getObject());
							PersistenceHelper.manager.delete(part);
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				} finally{
					SessionContext.setContext(previous);
				}
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
		} catch (WTPropertyVetoException e) {
			e.printStackTrace();
		} finally{
			if(newArray != null && newArray.size() > 0){
				// 收回设置状态权限
				ChangeWorkFlowUtils.removePersistablePermissions(newArray, (WTUser)SessionHelper.manager.getPrincipal(), 15);
			}
		}

		FormResult localFormResult = new FormResult();
		localFormResult.setStatus(FormProcessingStatus.SUCCESS);
		// localFormResult.setNextAction(FormResultAction.REFRESH_CURRENT_PAGE);
		return localFormResult;

	}
	
	/***
	 * 检查属性是否满足属性定义的合法值或枚举列表
	 * 
	 * @param typeName
	 *            对象类型内部名称或分类节点内部名称
	 * @param attributeName
	 *            对象类型或分类节点上定义的IBA属性名称
	 * @param attributeValue
	 *            IBA属性值
	 * @throws PIException
	 * @throws WTException
	 */
	public String checkAttributeValue(String typeName, String attributeName, String attributeValue, StringBuilder errorMsg) throws PIException, WTException {
		if(PIStringUtils.isNull(typeName) || PIStringUtils.isNull(attributeName) || PIStringUtils.isNull(attributeValue) || errorMsg == null){
			return attributeValue ;
		}
		
		// 分类节点属性合法值验证
		TypeDefinitionReadView readView = PIClassificationHelper.service.getNode(typeName);
		
		if(readView != null){
			// 获取属性定义
			AttributeDefinitionReadView av = readView.getAttributeByName(attributeName);
			if(av != null){
				Collection<ConstraintDefinitionReadView> constraints = av.getAllConstraints();
				for(ConstraintDefinitionReadView cdReadView : constraints){
					String rule = cdReadView.getRule().getKey().toString();
					if(rule.contains(DiscreteSetConstraint.class.getName())){
						//获取合法值集
		    			RuleDataObject rd = cdReadView.getRuleDataObj();
		    			if(rd != null){
		    				Object obj = rd.getRuleData();
		    				if (obj != null && obj instanceof DiscreteSet) {
		    					// 存储合法值
		    					Collection<String> valueArray = new HashSet<String>() ;
								for (Object object : ((DiscreteSet) obj).getElements()) {
									valueArray.add(object.toString()) ;
								}
								if(valueArray.size() > 0 && (!valueArray.contains(attributeValue))){
									errorMsg.append(av.getDisplayName() + " 属性值: " + attributeValue + " ,不满足定义的属性值列表：" + valueArray) ;
									errorMsg.append("\n") ;
								}
							}else{
								// 处理属性关联的全局枚举
								EnumerationDefinitionReadView eReadView = rd.getEnumDef() ;
								if(eReadView != null){
									Map< String , String > map = new HashMap<String, String>();
									for(Map.Entry<String, EnumerationEntryReadView> entryMap : eReadView.getAllEnumerationEntries().entrySet()){
										EnumerationEntryReadView eeReadView = entryMap.getValue() ;
										//内部名称
										String enumKey = eeReadView.getName();
										//显示名称
										String enumName = eeReadView.getPropertyValueByName("displayName").getValue().toString();
										//是否可用
										String selectable = eeReadView.getPropertyValueByName("selectable").getValue().toString();
										if ( "true".equals(selectable) ) {
											map.put(enumKey, enumName);
										}
									}
									if(map.size() > 0 && (!map.containsKey(attributeValue))){
										errorMsg.append(av.getDisplayName() + " 属性值: " + attributeValue + " ,不满足定义的属性值列表：" + map.keySet()) ;
				    					errorMsg.append("\n") ;
				    				}else{
				    					attributeValue = map.get(attributeValue) ;
				    				}
								}
							}
		    			}
					}else if(rule.contains(LWCEnumerationBasedConstraint.class.getName())){ // 处理属性本身定义的枚举
						RuleDataObject rd = cdReadView.getRuleDataObj();
		    			if( rd != null ){
		    				Map< String , String > map = new HashMap<String, String>();
		    				//获取枚举值集
		    				Collection<EnumerationEntryReadView> coll = rd.getEnumDef().getAllEnumerationEntries().values();
		    				for (EnumerationEntryReadView eReadView : coll) {
								//内部名称
								String enumKey = eReadView.getName();
								//显示名称
								String enumName = eReadView.getPropertyValueByName("displayName").getValue().toString();
								//是否可用
								String selectable = eReadView.getPropertyValueByName("selectable").getValue().toString();
								if ( "true".equals(selectable) ) {
									map.put(enumKey, enumName);
								}
							}
		    				if(map.size() > 0 && (!map.containsKey(attributeValue))){
		    					errorMsg.append(av.getDisplayName() + " 属性值: " + attributeValue + " ,不满足定义的属性值列表：" + map.keySet()) ;
		    					errorMsg.append("\n") ;
		    				}else{
		    					attributeValue = map.get(attributeValue) ;
		    				}
		    			}
					}
				}
			}
		}
		
		return attributeValue ;
	}
	
	/**
	 * 设置部件名称
	 * @param part 部件
	 * @param newName 新名称
	 * @throws WTException
	 */
	public static void setWTPartName(WTPart part, String newName) throws WTException {
		if(part == null || PIStringUtils.isNull(newName)){
			return ;
		}
		// 获取当前用户
		WTPrincipal principal = SessionHelper.manager.getPrincipal() ;
		try {
			// 赋予修改编码权限
			PIAccessHelper.service.grantPersistablePermissions(part, 
					WTPrincipalReference.newWTPrincipalReference(principal), AccessPermission.MODIFY_IDENTITY, true);
			
			Identified identified = (Identified)part.getMaster() ;
			WTPartMasterIdentity masteridentity = (WTPartMasterIdentity)identified.getIdentificationObject();
			masteridentity.setName(newName) ;
			//更新名称
			identified = IdentityHelper.service.changeIdentity(identified, masteridentity); 
			PersistenceServerHelper.manager.update(identified);
		} catch (Exception e) {
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage()) ;
		} finally{
			PIAccessHelper.service.removePersistablePermissions(part, 
					WTPrincipalReference.newWTPrincipalReference(principal), AccessPermission.MODIFY_IDENTITY, true);
		}
	}

	
	public static String getCurrentTime() {

		String currentTime = "";

		Date currentDate = new Date();

		DateFormat sdf = getDateFormat();

		try {
			currentTime = sdf.format(currentDate);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return currentTime;
	}

	private static DateFormat getDateFormat() {
		// 设置日期格式
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// 设置中国时区
		TimeZone timeZone = TimeZone.getTimeZone("Asia/Shanghai");

		if (timeZone != null) {
			sdf.setTimeZone(timeZone);
		}

		return sdf;
	}
}
