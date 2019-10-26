package ext.appo.part.processor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;

import ext.appo.erp.service.PartReleaseService;
import ext.generic.integration.erp.common.CommonPDMUtil;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.util.FeedbackMessage;
import com.ptc.core.lwc.common.view.AttributeDefinitionReadView;
import com.ptc.core.lwc.common.view.ConstraintDefinitionReadView;
import com.ptc.core.lwc.common.view.ConstraintDefinitionReadView.RuleDataObject;
import com.ptc.core.lwc.common.view.EnumerationDefinitionReadView;
import com.ptc.core.lwc.common.view.EnumerationEntryReadView;
import com.ptc.core.lwc.common.view.TypeDefinitionReadView;
import com.ptc.core.lwc.server.LWCEnumerationBasedConstraint;
import com.ptc.core.meta.common.DiscreteSet;
import com.ptc.core.meta.container.common.impl.DiscreteSetConstraint;
import com.ptc.core.ui.resources.FeedbackType;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.appo.ecn.common.util.ChangeWorkFlowUtils;
import ext.appo.part.beans.ProductLineConfigBean;
import ext.appo.part.workflow.PartWorkflowUtil;
import ext.appo.part.workflow.ReadExcelData;
import ext.com.iba.IBAUtil;
import ext.com.workflow.WorkflowUtil;
import ext.generic.integration.erp.common.CommonUtil;
import ext.generic.workflow.WorkFlowBase;
import ext.lang.PIStringUtils;
import ext.pi.PIException;
import ext.pi.core.PIClassificationHelper;
import wt.access.AccessControlHelper;
import wt.access.AccessPermission;
import wt.access.AdHocAccessKey;
import wt.access.AdHocControlled;
import wt.doc.WTDocument;
import wt.fc.*;
import wt.fc.collections.WTKeyedHashMap;
import wt.folder.Cabinet;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.folder.SubFolder;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.QuantityUnit;
import wt.part.Source;
import wt.part.WTPart;
import wt.pom.Transaction;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;
import wt.util.WTMessage;
import wt.util.WTPropertyVetoException;
import wt.vc.views.ViewHelper;
import wt.vc.views.ViewReference;

/**
 * 手动设计发布流程
 */
public class ProductNamingNoticeStartProcessor extends DefaultObjectFormProcessor {
	private static String CLASSNAME = ProductNamingNoticeStartProcessor.class.getName();
	private static final Logger LOGGER = LogR.getLogger(CLASSNAME);
	private static final String RESOURCE = "ext.generic.partpromotion.resource.PartPromotionResourceRB";

	// 文档软类型：产品命名通知单
	private static final String DOC_TYPE_PRODUCTNAMINGNOTIC = "com.plm.productNamingNotic";

	@Override
	public FormResult doOperation(NmCommandBean nmCommandBean, List<ObjectBean> arg1) throws WTException {

		NmOid pageOid = nmCommandBean.getPageOid();
		Object refObject = pageOid.getRefObject();
		LOGGER.debug("refObject ==== " + refObject);
		Folder folder = null;
		if (refObject != null && refObject instanceof Cabinet) {
			Cabinet cabinet = (Cabinet) refObject;
			WTContainerRef containerReference = cabinet.getContainerReference();
			folder = FolderHelper.service.getFolder("/Default", containerReference);
		} else if (refObject != null && refObject instanceof SubFolder) {
			folder = (SubFolder) refObject;
		} else {
			String containerOid = nmCommandBean.getRequest().getParameter("ContainerOid");
			LOGGER.debug("containerOid ==== " + containerOid);
			if (PIStringUtils.isNotNull(containerOid)) {
				WTContainer wtContainer = (WTContainer) ((new ReferenceFactory()).getReference(containerOid)
						.getObject());
				WTContainerRef containerReference = wtContainer.getContainerReference();
				folder = FolderHelper.service.getFolder("/Default", containerReference);
			}
		}

		WTDocument doc = WTDocument.newWTDocument();
		try {
			// 设置类型
			TypeDefinitionReference typeDefinitionReference = TypedUtilityServiceHelper.service
					.getTypeDefinitionReference(DOC_TYPE_PRODUCTNAMINGNOTIC);
			if (typeDefinitionReference == null) {
				typeDefinitionReference = TypeDefinitionReference.newTypeDefinitionReference();
			}
			doc.setTypeDefinitionReference(typeDefinitionReference);
			// 设置文档名称
			doc.setName("产品命名通知单_" + getCurrentTime());
			// 设置文件夹
			FolderHelper.assignLocation((FolderEntry) doc, folder);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		doc = (WTDocument) PersistenceHelper.manager.save(doc);
		LOGGER.debug("doc====" + doc.getName());
		String productLine = nmCommandBean.getRequest().getParameter("productLine");
		LOGGER.debug("productLine====" + productLine);
		String project = nmCommandBean.getRequest().getParameter("project");
		LOGGER.debug("project====" + project);
		IBAUtil.forceSetIBAValue(doc, "sscpx", productLine);
		IBAUtil.forceSetIBAValue(doc, "ssxm", project);
		Map<String, String[]> parameterMap = nmCommandBean.getRequest().getParameterMap();
		String[] strings = parameterMap.get("datasArray");
		String value = strings[0];
		// 收集创建的部件
		Vector newArray = new Vector();
		FormResult localFormResult = new FormResult();
		try {
			JSONObject jsonObject = new JSONObject(value);
			System.out.println("jsonObject======" + jsonObject.toString() + jsonObject.toString().length());
			if (jsonObject.toString().length() <= 2) {
				throw new WTException("产品属性栏为空，请点击'添加'后，填写相关属性再提交！");
			}
			Iterator keys = jsonObject.keys();
			while (keys.hasNext()) {
				JSONObject valueJson = new JSONObject(jsonObject.getString((String) keys.next()));

				WTPart part = WTPart.newWTPart();
				// 设置‘源’
				Source source = Source.toSource("make");
				part.setSource(source);
				// 设置‘视图’
				part.setView(ViewReference.newViewReference(ViewHelper.service.getView("Design")));
				// 设置文件夹
				FolderHelper.assignLocation((FolderEntry) part, folder);
				String name = (String) valueJson.get("name");
				// 设置部件名称
				part.setName(name);
				// 设置‘是否成品’
				part.setEndItem(true);

				part.setDefaultUnit(QuantityUnit.toQuantityUnit("ea"));
				System.out.println("part====" + part);
				part = (WTPart) PersistenceHelper.manager.save(part);
				LOGGER.debug("part====" + part.getName());
				// 分类初始化
				String partClassification = productLine;
				// 读取‘所属产品线’属性配置
				List<ProductLineConfigBean> productLineConfigArray = ReadExcelData.getProductLineConfig();
				for (ProductLineConfigBean bean : productLineConfigArray) {
					if (bean.getEnumName().equalsIgnoreCase(partClassification)) {
						if (PIStringUtils.isNotNull(bean.getNodeName())) {
							partClassification = bean.getNodeName();
						}
						break;
					}
				}
				// 设置分类
				IBAUtil.forceSetIBAValue(part, "Classification", partClassification);

				// 收集用户填写时，输入内容不符合系统定义的信息
				StringBuilder errorMsg = new StringBuilder();

				// 设置物料所属产品线
				String partProductLine = "";
				PartWorkflowUtil util = new PartWorkflowUtil();
				Map<ArrayList<String>, ArrayList<String>> returnMap = util.getEnumeratedMap("sscpx");
				for (Map.Entry<ArrayList<String>, ArrayList<String>> entryMap : returnMap.entrySet()) {
					// 内部名称
					ArrayList<String> interiorArray = entryMap.getKey();
					for (int i = 0; i < interiorArray.size(); i++) {
						if (interiorArray.get(i).equals(productLine)) {
							partProductLine = entryMap.getValue().get(i);
							break;
						}
					}
				}
				IBAUtil.forceSetIBAValue(part, "sscpx", partProductLine);
				IBAUtil.forceSetIBAValue(part, "ssxm", project);
				Iterator keys2 = valueJson.keys();
				while (keys2.hasNext()) {
					String attri = (String) keys2.next();
					String attriValue = valueJson.getString(attri);
					if (attriValue.equals("null")) {
						attriValue = "";
					}
					if (attriValue.startsWith("[") && attriValue.endsWith("]")) {
						attriValue = attriValue.substring(attriValue.indexOf("[") + 1, attriValue.length() - 1);
					}
					if (!attri.equals("name") && !attri.equals("partOid") && !attri.equals("sscpx")) {
						// 检查属性是否满足属性定义的合法值或枚举列表
						attriValue = checkAttributeValue(productLine, attri, attriValue, errorMsg);
						// 设置IBA属性
						IBAUtil.forceSetIBAValue(part, attri, attriValue);
					}
				}

				if (errorMsg.length() > 0) {
					throw new WTException(errorMsg.toString());
				}

				// 设置部件编码
				CusCreatePartAndCADDocFormProcessor partProcessor = new CusCreatePartAndCADDocFormProcessor();
				partProcessor.setNumber(part);
				// 创建部件和文档关联关系
				GenerateProductStructureProcessor processor = new GenerateProductStructureProcessor();
				processor.createPartDocLink(part, doc);
				// 当前用户设置为管理员，用于忽略权限
				try {
					WTPrincipal wtadministrator = SessionHelper.manager.getAdministrator();
					// 取得当前用户
					SessionContext.setEffectivePrincipal(wtadministrator);
					WTPrincipalReference wtprincipalreference = (WTPrincipalReference) (new ReferenceFactory())
							.getReference(SessionHelper.manager.getPrincipal());
					AccessControlHelper.manager.addPermission((AdHocControlled) part, wtprincipalreference,
							AccessPermission.MODIFY_IDENTITY, AdHocAccessKey.WNC_ACCESS_CONTROL);
				} catch (WTException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				WTPart mpPart = (WTPart) ViewHelper.service.newBranchForView(part, "Manufacturing");
				PersistenceHelper.manager.store(mpPart);
				WorkflowUtil.setLifeCycleState(mpPart, "UNDERREVIEW");// 设置状态为正在审阅
				SessionContext.setEffectivePrincipal(SessionHelper.manager.getPrincipal());
				// 执行手工发布
				if (part != null) {
					// 获取IP地址
					String ip = null;
					try {
						ip = InetAddress.getLocalHost().getHostAddress();
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
					System.out.println("IP地址："+ip);
					WTKeyedHashMap hashMap =new WTKeyedHashMap();
					//区别光峰61，70,71服务器，绎立服务器，光峰采用发K3方法，其余采用发中间表方法
					if (ip.equals("172.32.252.61")||ip.equals("172.32.252.70")||ip.equals("172.32.252.71")){
						System.out.println("产品命名通知单调用K3接口==");
						// 发布物料信息 发K3
						hashMap = ext.appo.erp.service.PartReleaseService.sendMaterial(part);
					}else{
						System.out.println("产品命名通知单调用中间表接口==");
						String releaseTime = CommonUtil.getCurrentTime();
						hashMap = ext.generic.integration.erp.service.PartReleaseService.release(part, releaseTime, CommonPDMUtil.getBatchNumber((ObjectReference)null));
					}

					if (hashMap != null && hashMap.size() > 0) {
						localFormResult.addFeedbackMessage(new FeedbackMessage(FeedbackType.FAILURE, null, "物料发布异常",
								null, new String[] { (String) hashMap.get(part) }));

						System.out.println("手工发布物料异常：" + part.getDisplayIdentity() + "...异常：" + hashMap);
					} else {
						localFormResult.addFeedbackMessage(
								new FeedbackMessage(FeedbackType.SUCCESS, null, "物料发布成功", null, new String[] {}));
					}
				}
				newArray.add(part);
			}
			if (newArray != null && newArray.size() > 0) {
				Vector refVector = new Vector();
				// 赋予设置状态权限
				ChangeWorkFlowUtils.grantPersistablePermissions(newArray, (WTUser) SessionHelper.manager.getPrincipal(),
						15);
				WorkflowUtil.setLifeCycleState(newArray, "UNDERREVIEW");
				for (Object object : newArray) {
					refVector.add(PersistenceHelper.manager.refresh((Persistable) object));
				}
				newArray = refVector;
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
		} catch (WTPropertyVetoException e) {
			e.printStackTrace();
		} finally {
			if (newArray != null && newArray.size() > 0) {
				// 收回设置状态权限
				ChangeWorkFlowUtils.removePersistablePermissions(newArray,
						(WTUser) SessionHelper.manager.getPrincipal(), 15);
			}
		}

		// 创建流程

		localFormResult.setStatus(FormProcessingStatus.SUCCESS);
		FeedbackMessage feedbackmessage = null;
		Locale locale = nmCommandBean.getLocale();

		String workFlowName = "APPO_ProdutNameNoticeWF";
		try {
			WorkFlowBase workFlowBase = new WorkFlowBase();
			if (workFlowBase.startWorkFlow(doc, workFlowName)) {
				String msg = WTMessage.getLocalizedMessage("ext.generic.doc.resource.WorkflowRelatedRB",
						"START_DOCSIGN_MSG", null, locale);
				// feedbackmessage = new FeedbackMessage(FeedbackType.SUCCESS,
				// null, msg, null, new String[0]);
				feedbackmessage = new FeedbackMessage(FeedbackType.SUCCESS, nmCommandBean.getLocale(), msg, null,
						new String[] {});
				feedbackmessage.setShowItemIdAsText(false);
				feedbackmessage.addOidIdentityPair(doc, nmCommandBean.getLocale());
				localFormResult.addFeedbackMessage(feedbackmessage);
			}
		} catch (Exception e) {
			e.printStackTrace();
			String error = WTMessage.getLocalizedMessage("ext.generic.doc.resource.WorkflowRelatedRB",
					"START_DOCSIGN_ERR", null, locale);
			feedbackmessage = new FeedbackMessage(FeedbackType.FAILURE, null, error + e.toString(), null,
					new String[0]);
		}

		if (feedbackmessage == null) {
			String error = WTMessage.getLocalizedMessage("ext.generic.doc.resource.WorkflowRelatedRB",
					"START_DOCSIGN_ERR", null, locale);
			feedbackmessage = new FeedbackMessage(FeedbackType.FAILURE, null, error, null, new String[0]);
		}

		// localFormResult.setNextAction(FormResultAction.REFRESH_CURRENT_PAGE);
		return localFormResult;

	}

	/**
	 * 设置对象访问权限
	 * 
	 * @param persistable
	 * @param wtprincipal
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 * @return
	 */
	public static Persistable setObjectAccess(Persistable persistable, WTPrincipal wtprincipal)
			throws WTException, WTPropertyVetoException {

		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		Transaction transaction = new Transaction();
		try {
			transaction.start();
			WTPrincipalReference wtprincipalreference = WTPrincipalReference.newWTPrincipalReference(wtprincipal);

			Vector vector = new Vector();
			vector.add(AccessPermission.READ);
			vector.add(AccessPermission.DOWNLOAD);
			vector.add(AccessPermission.NEW_VIEW_VERSION);
			vector.add(AccessPermission.CREATE);

			persistable = PersistenceHelper.manager.refresh(persistable);
			if (persistable instanceof AdHocControlled) {
				AdHocControlled adhoccontrolled = (AdHocControlled) persistable;
				try {
					adhoccontrolled = AccessControlHelper.manager.addPermissions(adhoccontrolled, wtprincipalreference,
							vector, AdHocAccessKey.WNC_ACCESS_CONTROL);
					PersistenceServerHelper.manager.update(adhoccontrolled, false);

				} catch (Exception exception) {
					System.out.println(exception.getLocalizedMessage());
				}
			}
			persistable = PersistenceHelper.manager.refresh(persistable);

			transaction.commit();
			transaction = null;
			Persistable persistable1 = persistable;
			return persistable1;
		} finally {
			if (transaction != null)
				transaction.rollback();
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
	}

	public static Persistable removeObjectAccess(Persistable persistable, WTPrincipal wtprincipal)
			throws WTException, WTPropertyVetoException {

		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		Transaction transaction = new Transaction();
		try {
			transaction.start();
			WTPrincipalReference wtprincipalreference = WTPrincipalReference.newWTPrincipalReference(wtprincipal);

			Vector vector = new Vector();
			vector.add(AccessPermission.READ);
			vector.add(AccessPermission.DOWNLOAD);

			persistable = PersistenceHelper.manager.refresh(persistable);
			if (persistable instanceof AdHocControlled) {
				AdHocControlled adhoccontrolled = (AdHocControlled) persistable;
				try {
					adhoccontrolled = AccessControlHelper.manager.removePermissions(adhoccontrolled,
							wtprincipalreference, vector, AdHocAccessKey.WNC_ACCESS_CONTROL);
					PersistenceServerHelper.manager.update(adhoccontrolled, false);
				} catch (Exception exception) {
					System.out.println(exception.getLocalizedMessage());
					System.out.println("Failed on object: " + adhoccontrolled);
				}
			}
			transaction.commit();
			transaction = null;
			Persistable persistable1 = persistable;
			return persistable1;
		} finally {
			if (transaction != null)
				transaction.rollback();
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
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
	public String checkAttributeValue(String typeName, String attributeName, String attributeValue,
			StringBuilder errorMsg) throws PIException, WTException {
		if (PIStringUtils.isNull(typeName) || PIStringUtils.isNull(attributeName)
				|| PIStringUtils.isNull(attributeValue) || errorMsg == null) {
			return attributeValue;
		}

		// 分类节点属性合法值验证
		TypeDefinitionReadView readView = PIClassificationHelper.service.getNode(typeName);

		if (readView != null) {
			// 获取属性定义
			AttributeDefinitionReadView av = readView.getAttributeByName(attributeName);
			if (av != null) {
				Collection<ConstraintDefinitionReadView> constraints = av.getAllConstraints();
				for (ConstraintDefinitionReadView cdReadView : constraints) {
					String rule = cdReadView.getRule().getKey().toString();
					if (rule.contains(DiscreteSetConstraint.class.getName())) {
						// 获取合法值集
						RuleDataObject rd = cdReadView.getRuleDataObj();
						if (rd != null) {
							Object obj = rd.getRuleData();
							if (obj != null && obj instanceof DiscreteSet) {
								// 存储合法值
								Collection<String> valueArray = new HashSet<String>();
								for (Object object : ((DiscreteSet) obj).getElements()) {
									valueArray.add(object.toString());
								}
								if (valueArray.size() > 0 && (!valueArray.contains(attributeValue))) {
									errorMsg.append(av.getDisplayName() + " 属性值: " + attributeValue + " ,不满足定义的属性值列表："
											+ valueArray);
									errorMsg.append("\n");
								}
							} else {
								// 处理属性关联的全局枚举
								EnumerationDefinitionReadView eReadView = rd.getEnumDef();
								if (eReadView != null) {
									Map<String, String> map = new HashMap<String, String>();
									for (Map.Entry<String, EnumerationEntryReadView> entryMap : eReadView
											.getAllEnumerationEntries().entrySet()) {
										EnumerationEntryReadView eeReadView = entryMap.getValue();
										// 内部名称
										String enumKey = eeReadView.getName();
										// 显示名称
										String enumName = eeReadView.getPropertyValueByName("displayName").getValue()
												.toString();
										// 是否可用
										String selectable = eeReadView.getPropertyValueByName("selectable").getValue()
												.toString();
										if ("true".equals(selectable)) {
											map.put(enumKey, enumName);
										}
									}
									if (map.size() > 0 && (!map.containsKey(attributeValue))) {
										errorMsg.append(av.getDisplayName() + " 属性值: " + attributeValue
												+ " ,不满足定义的属性值列表：" + map.keySet());
										errorMsg.append("\n");
									} else {
										attributeValue = map.get(attributeValue);
									}
								}
							}
						}
					} else if (rule.contains(LWCEnumerationBasedConstraint.class.getName())) { // 处理属性本身定义的枚举
						RuleDataObject rd = cdReadView.getRuleDataObj();
						if (rd != null) {
							Map<String, String> map = new HashMap<String, String>();
							// 获取枚举值集
							Collection<EnumerationEntryReadView> coll = rd.getEnumDef().getAllEnumerationEntries()
									.values();
							for (EnumerationEntryReadView eReadView : coll) {
								// 内部名称
								String enumKey = eReadView.getName();
								// 显示名称
								String enumName = eReadView.getPropertyValueByName("displayName").getValue().toString();
								// 是否可用
								String selectable = eReadView.getPropertyValueByName("selectable").getValue()
										.toString();
								if ("true".equals(selectable)) {
									map.put(enumKey, enumName);
								}
							}
							if (map.size() > 0 && (!map.containsKey(attributeValue))) {
								errorMsg.append(av.getDisplayName() + " 属性值: " + attributeValue + " ,不满足定义的属性值列表："
										+ map.keySet());
								errorMsg.append("\n");
							} else {
								attributeValue = map.get(attributeValue);
							}
						}
					}
				}
			}
		}

		return attributeValue;
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
