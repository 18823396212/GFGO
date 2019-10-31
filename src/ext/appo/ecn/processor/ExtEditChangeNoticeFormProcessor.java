package ext.appo.ecn.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.windchill.enterprise.change2.forms.processors.EditChangeNoticeFormProcessor;

import ext.appo.ecn.common.util.ChangeUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.part.filter.StandardPartsRevise;
import ext.appo.util.PartUtil;
import ext.lang.PIStringUtils;
import ext.pi.PIException;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIClassificationHelper;
import wt.change2.ChangeHelper2;
import wt.change2.ChangeOrder2;
import wt.change2.Changeable2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.configurablelink.ConfigurableDescribeLink;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTSet;
import wt.identity.IdentityFactory;
import wt.inf.container.WTContainer;
import wt.inf.library.WTLibrary;
import wt.lifecycle.LifeCycleManaged;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.pdmlink.PDMLinkProduct;
import wt.pds.StatementSpec;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.WhereExpression;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;
import wt.vc.Iterated;

/**
 * 
 * 
 * @author APPO mao
 *
 */
public class ExtEditChangeNoticeFormProcessor extends EditChangeNoticeFormProcessor {

	private static final String CLASSNAME = EditChangeNoticeFormProcessor.class.getName();
	private static final Logger LOG = LogR.getLogger(CLASSNAME);

	@Override
	public FormResult postProcess(NmCommandBean nmcommandBean, List<ObjectBean> objectBeans) throws WTException {
		FormResult result = new FormResult();
		result.setStatus(FormProcessingStatus.SUCCESS);

		SessionContext previous = SessionContext.newContext();
		try {
			// 当前用户设置为管理员，用于忽略权限
			SessionHelper.manager.setAdministrator();

			WTChangeOrder2 changeOrder2 = (WTChangeOrder2) objectBeans.get(0).getObject();

			// 检查是否存在未结束的ECN
			checkIsExistECN(nmcommandBean, changeOrder2);

			// 根据受影响对象表单中的数据创建ECA对象
			Collection<Changeable2> affectedArray = ChangeUtils.saveChangeActivity2(nmcommandBean, changeOrder2, null);
			if (LOG.isDebugEnabled()) {
				LOG.debug("affectedArray : " + affectedArray.size());
			}
			// 检查受影响对象是否符合变更要求
			checkAffectedItems(changeOrder2, affectedArray);

			// 检查是否为标准件
			checkIsStandardparse(nmcommandBean, affectedArray);
			// 根据‘事务性任务’表数据创建ECA对象
			ChangeUtils.createEditChangeActivity2(changeOrder2, nmcommandBean);

			// 新增ChangeOrder2与产品关系
			saveAffectedEndItems(nmcommandBean, changeOrder2);

			// 回填所属产品线及所属项目
			HashMap<?, ?> comboBox = nmcommandBean.getComboBox();
			Iterator<?> iterator = comboBox.keySet().iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				if (key.contains("sscpx")) {
					Object value = comboBox.get(key);
					if (value instanceof List) {
						List<?> list = (List<?>) value;
						if (list.size() > 0) {
							PIAttributeHelper.service.forceUpdateSoftAttribute(changeOrder2, "sscpx", list.get(0));
						}
					} else if (value != null) {
						PIAttributeHelper.service.forceUpdateSoftAttribute(changeOrder2, "sscpx", value);
					}
				} else if (key.contains("ssxm")) {
					Object value = comboBox.get(key);
					if (value instanceof List) {
						List<?> list = (List<?>) value;
						if (list.size() > 0) {
							PIAttributeHelper.service.forceUpdateSoftAttribute(changeOrder2, "ssxm", list.get(0));
						}
					} else if (value != null) {
						PIAttributeHelper.service.forceUpdateSoftAttribute(changeOrder2, "ssxm", value);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		} finally {
			SessionContext.setContext(previous);
		}

		return result;
	}

	/***
	 * 检查受影响对象是是否为标准件
	 * 
	 * @param changeOrder2
	 * @param affectedArray
	 * @throws WTException
	 */
	public void checkIsStandardparse(NmCommandBean nmcommandBean, Collection<Changeable2> affectedArray)
			throws WTException {
		// 受影响对象列表不能为空
		if (affectedArray == null || affectedArray.size() == 0) {
			throw new WTException("受影响对象列表不能为空!");
		}
		ArrayList<String> partlist = new ArrayList<>();
		HashMap<?, ?> comboBox = nmcommandBean.getComboBox();
		Iterator<?> iterator = comboBox.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			System.out.println("comboBox key=====" + key);
			if (key.contains("ChangeType")) {
				Object value = comboBox.get(key);
				String partoid = StringUtils.substringBefore(key, "_");
				WTPart part = (WTPart) PartUtil.getPersistableByOid(partoid);
				List<?> list = (List<?>) value;
				if (list.size() > 0) {
					String typevalue = list.get(0).toString();
					if (typevalue.contains("升版")) {
						partlist.add(part.getNumber());
					}
				}
			}
		}
		Boolean isStandartpart = false;
		StringBuilder errorMsg = new StringBuilder();
		for (Changeable2 changeable2 : affectedArray) {
			if (changeable2 instanceof WTPart) {
				WTPart part = (WTPart) changeable2;
				StandardPartsRevise re = new StandardPartsRevise();
				List<Map> list = new ArrayList<Map>();
				try {
					list = re.getExcelData();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (WTException e1) {
					e1.printStackTrace();
				}
				isStandartpart = isStandartpart(list, part);
				if (isStandartpart && partlist.contains(part.getNumber())) {
					errorMsg.append(part.getNumber() + ",");
				}
			}

		}
		if (errorMsg.length() > 0) {

			throw new WTException(errorMsg.toString() + " 业务定义为标准件，不能变更升版");
		}
	}

	/***
	 * 检查受影响对象是是否存在未结束的ECN
	 * 
	 * @param changeOrder2
	 * @param affectedArray
	 * @throws WTException
	 */
	public void checkIsExistECN(NmCommandBean nmcommandBean, ChangeOrder2 order) throws WTException {

		// 获取页面中changeTaskArray控件值并根据规则解析为对应集合
		Map<Persistable, Map<String, String>> pageDatasMap = ChangeUtils.getPageChangeTaskArray(nmcommandBean);
		System.out.println("pageDatasMap=====" + pageDatasMap);
		WTArrayList partlist = new WTArrayList();
		// 根据受影响对象表单构建创建ECA时需要填充的数据关系
		Map<Persistable, Collection<Persistable>> constructRelation = ChangeUtils.constructRelation(pageDatasMap);
		System.out.println("constructRelation=====" + constructRelation);
		if (constructRelation != null && constructRelation.size() > 0) {
			for (Map.Entry<Persistable, Collection<Persistable>> entryMap : constructRelation.entrySet()) {
				// 忽略不处理
				Boolean isNeglect = false;
				// 受影响对象
				Changeable2 changeable2 = (Changeable2) entryMap.getKey();

				if (changeable2 instanceof WTPart) {
					WTPart part = (WTPart) changeable2;
					System.out.println("part numner=====" + part.getNumber());
					QueryResult partqr = getParts(part.getNumber());
					while (partqr.hasMoreElements()) {
						WTPart oldpart = (WTPart) partqr.nextElement();
						String version = oldpart.getVersionIdentifier().getValue();
						System.out.println("version=====" + version);
						if (!partlist.contains(version)) {
							// 获取对象所有关联的ECA对象
							QueryResult eCResult = null;
							eCResult = ChangeHelper2.service.getAffectingChangeActivities(oldpart);
							System.out.println("eCResult size=====" + eCResult.size());
							while (eCResult.hasMoreElements()) {
								WTChangeActivity2 changeActivity2 = (WTChangeActivity2) eCResult.nextElement();
								System.out.println("eca numner=====" + changeActivity2.getNumber());
								WTChangeOrder2 order2 = ChangeUtils.getEcnByEca((WTChangeActivity2) changeActivity2);
								System.out.println("order2 numner=====" + order2.getNumber());
								System.out.println(
										"eca name===" + changeActivity2.getNumber() + changeActivity2.getName());
								if (!order.getNumber().startsWith(order2.getNumber())) {
									if ((!ChangeUtils.checkState(order2, ChangeConstants.CANCELLED))
											&& (!ChangeUtils.checkState(order2, ChangeConstants.RESOLVED))) {

										throw new WTException("物料：" + part.getNumber() + ",存在没解决的ECN:"
												+ order2.getNumber() + "不能同时提交两个ECN！");
									}
								}
							}
						} else {
							partlist.add(version);
						}
					}

				}

			}

		}

	}

	private QueryResult getParts(String number) throws WTException {
		StatementSpec stmtSpec = new QuerySpec(WTPart.class);
		WhereExpression where = new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL,
				number.toUpperCase());
		QuerySpec querySpec = (QuerySpec) stmtSpec;
		querySpec.appendWhere(where, new int[] { 0 });
		QueryResult qr = PersistenceServerHelper.manager.query(stmtSpec);

		return qr;
	}

	public static Boolean isStandartpart(List<Map> datalist, WTPart part) {
		Boolean isStandardpart = false;
		// 获取分类内部值
		String value = "";
		String nodeHierarchy = "";// 获取分类全路径
		try {
			value = (String) PIAttributeHelper.service.getValue(part, "Classification");
			System.out.println("cls value ====" + value);
			nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(value);
			System.out.println("nodeHierarchy value ===" + nodeHierarchy);
		} catch (PIException e2) {
			e2.printStackTrace();
		}
		if (datalist != null && datalist.size() > 0) {
			for (Map map2 : datalist) {
				Map<Persistable, Map> map = new HashMap<>();
				String type = "";
				type = (String) map2.get("type");
				System.out.println("type===" + type + "-----------" + nodeHierarchy);
				if (type.length() > 0 && nodeHierarchy.contains(type)) {
					isStandardpart = true;
					break;
				}
			}
		}
		return isStandardpart;
	}

	/***
	 * 检查受影响对象是否符合变更要求
	 * 
	 * @param changeOrder2
	 * @param affectedArray
	 * @throws WTException
	 */
	public void checkAffectedItems(WTChangeOrder2 changeOrder2, Collection<Changeable2> affectedArray)
			throws WTException {
		if (changeOrder2 == null) {
			return;
		}

		// 受影响对象列表不能为空
		if (affectedArray == null || affectedArray.size() == 0) {
			throw new WTException("受影响对象列表不能为空!");
		}

		StringBuilder errorMsg = new StringBuilder();
		for (Changeable2 changeable2 : affectedArray) {
			if ((!ChangeUtils.checkState((LifeCycleManaged) changeable2, ChangeConstants.ARCHIVED))
					&& (!ChangeUtils.checkState((LifeCycleManaged) changeable2, ChangeConstants.RELEASED))) {
				if (errorMsg.length() > 0) {
					errorMsg.append("\n");
				}
				errorMsg.append(IdentityFactory.getDisplayIdentifier(changeable2));
			}
		}
		if (errorMsg.length() > 0) {
			throw new WTException(errorMsg.toString() + " 状态不满足：已归档或已发布");
		}
	}

	/***
	 * 新增ChangeOrder2与产品关系
	 * 
	 * @param nmcommandBean
	 * @param changeOrder2
	 * @throws WTException
	 */
	public void saveAffectedEndItems(NmCommandBean nmcommandBean, WTChangeOrder2 changeOrder2) throws WTException {
		if (nmcommandBean == null) {
			return;
		}

		try {
			Map<String, Object> parameterMap = nmcommandBean.getParameterMap();
			if (parameterMap.containsKey(ChangeConstants.AFFECTED_PRODUCT_ID)) {
				String[] endItemsArrayStr = (String[]) parameterMap.get(ChangeConstants.AFFECTED_PRODUCT_ID);
				if (endItemsArrayStr != null && endItemsArrayStr.length > 0) {
					String endItemsJSON = endItemsArrayStr[0];
					if (LOG.isDebugEnabled()) {
						LOG.debug("endItemsJSON : " + endItemsJSON);
					}
					if (PIStringUtils.isNull(endItemsJSON)) {
						return;
					}
					// 页面表单中所有产品对象
					Collection<WTPart> parentArray = new HashSet<WTPart>();
					JSONArray jsonArray = new JSONArray(endItemsJSON);
					for (int i = 0; i < jsonArray.length(); i++) {
						String oid = jsonArray.getString(i);
						if (PIStringUtils.isNotNull(oid)) {
							if (!oid.contains(WTPart.class.getName())) {
								continue;
							}
							parentArray.add((WTPart) ((new ReferenceFactory()).getReference(oid).getObject()));
						}
					}
					// 存储需要移除的数据
					Collection<ConfigurableDescribeLink> removeArray = new HashSet<ConfigurableDescribeLink>();
					// 获取changeOrder2关联的Link
					QueryResult qr = PersistenceHelper.manager.navigate(changeOrder2,
							ConfigurableDescribeLink.DESCRIBED_BY_ROLE, ConfigurableDescribeLink.class, false);
					while (qr.hasMoreElements()) {
						ConfigurableDescribeLink link = (ConfigurableDescribeLink) qr.nextElement();
						if (link != null) {
							Iterated iterated = link.getDescribedBy();
							if (iterated instanceof WTPart) {
								WTPart parentPart = (WTPart) iterated;
								if (parentArray.contains(parentPart)) {
									parentArray.remove(parentPart);
								} else {
									removeArray.add(link);
								}
							}
						}
					}
					if (removeArray.size() > 0) {
						PersistenceHelper.manager.delete(new WTHashSet(removeArray));
					}
					if (parentArray.size() > 0) {
						TypeDefinitionReference td = TypedUtilityServiceHelper.service
								.getTypeDefinitionReference(ChangeConstants.CHANGEORDER2_ENDITEMS_LINK_TYPE);
						if (td == null) {
							throw new WTException(ChangeConstants.CHANGEORDER2_ENDITEMS_LINK_TYPE + " 可配置Link软类型未创建!");
						}
						WTSet wtSet = new WTHashSet();
						for (WTPart parentPart : parentArray) {
							wtSet.add(
									ConfigurableDescribeLink.newConfigurableDescribeLink(changeOrder2, parentPart, td));
						}
						PersistenceHelper.manager.save(wtSet);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		}
	}

	/***
	 * 设置ECN上下文容器
	 * 
	 * @param nmCommandBean
	 * @param changeOrder2
	 *            ChangeOrder2对象
	 * @return
	 * @throws WTException
	 */
	public WTChangeOrder2 setContainer(NmCommandBean nmCommandBean, WTChangeOrder2 changeOrder2) throws WTException {
		if (nmCommandBean == null || changeOrder2 == null) {
			return changeOrder2;
		}

		try {
			// TODO 添加获取对象上下文逻辑
			changeOrder2.setContainer(getWTContainer("影院产品库"));
			changeOrder2 = (WTChangeOrder2) PersistenceHelper.manager.save(changeOrder2);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException("无法在 影院产品库 库中创建更改通告对象!");
		}

		return changeOrder2;
	}

	/***
	 * 根据上下文名称查询上下文容器
	 * 
	 * @param containerName
	 *            容器名称
	 * @return
	 * @throws WTException
	 */
	public WTContainer getWTContainer(String containerName) throws WTException {
		WTContainer wtContainer = null;

		try {
			QuerySpec querySpec = new QuerySpec(PDMLinkProduct.class);
			querySpec.appendWhere(
					new SearchCondition(WTContainer.class, WTContainer.NAME, SearchCondition.EQUAL, containerName),
					new int[] { 0 });
			QueryResult qr = PersistenceServerHelper.manager.query(querySpec);
			if (qr.hasMoreElements()) {
				wtContainer = (WTContainer) qr.nextElement();
			}
			if (wtContainer == null) {
				querySpec = new QuerySpec(WTLibrary.class);
				querySpec.appendWhere(
						new SearchCondition(WTContainer.class, WTContainer.NAME, SearchCondition.EQUAL, containerName),
						new int[] { 0 });
				qr = PersistenceServerHelper.manager.query(querySpec);
				if (qr.hasMoreElements()) {
					wtContainer = (WTContainer) qr.nextElement();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		}

		return wtContainer;
	}
}
