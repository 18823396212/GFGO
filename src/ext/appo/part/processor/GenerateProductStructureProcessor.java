package ext.appo.part.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.lwc.common.view.PropertyHolderHelper;
import com.ptc.core.lwc.common.view.TypeDefinitionReadView;
import com.ptc.core.lwc.server.LWCLocalizablePropertyValue;
import com.ptc.core.lwc.server.LWCStructEnumAttTemplate;
import com.ptc.core.lwc.server.TypeDefinitionServiceHelper;
import com.ptc.core.meta.type.mgmt.server.impl.WTTypeDefinition;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.appo.ecn.common.util.ChangeUtils;
import ext.appo.part.beans.ProductStructureTemplateExcelBean;
import ext.appo.part.dataUtilities.ProductNamingNoticDataUtility;
import ext.appo.part.util.ProductStructureTemplateExcelReader;
import ext.appo.part.workflow.PartWorkflowUtil;
import ext.appo.util.excel.AppoExcelUtil;
import ext.generic.reviewObject.cache.ExcelCacheManager;
import ext.generic.workflow.WorkFlowBase;
import ext.lang.PIStringUtils;
import ext.lang.file.FileConstantIfc;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIClassificationHelper;
import ext.pi.core.PICoreHelper;
import ext.pi.core.PIDocumentHelper;
import ext.pi.core.PIPartHelper;
import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.inf.container.WTContained;
import wt.inf.container.WTContainerRef;
import wt.lifecycle.LifeCycleManaged;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.PartDocHelper;
import wt.part.Source;
import wt.part.WTPart;
import wt.part.WTPartDescribeLink;
import wt.part.WTPartMaster;
import wt.part.WTPartReferenceLink;
import wt.part.WTPartUsageLink;
import wt.query.ArrayExpression;
import wt.query.ClassAttribute;
import wt.query.OrderBy;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.team.TeamReference;
import wt.team._TeamManaged;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.vc.config.LatestConfigSpec;
import wt.vc.views.View;
import wt.vc.views.ViewHelper;
import wt.vc.views.ViewReference;
import wt.workflow.definer.WfDefinerHelper;
import wt.workflow.definer.WfProcessTemplate;
import wt.workflow.definer.WfProcessTemplateMaster;
import wt.workflow.engine.ProcessData;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfEngineServerHelper;
import wt.workflow.engine.WfProcess;

public class GenerateProductStructureProcessor extends DefaultObjectFormProcessor {

	private static final String CLASSNAME = GenerateProductStructureProcessor.class.getName();

	private static final Logger LOG = LogR.getLogger(CLASSNAME);
	// 部件IBA：物料分类
	private static final String CLASSIFICATION = "Classification";
	// 部件IBA：备注
	private static final String REMARKS = "bz";
	// 文档IBA：文档小类
	private static final String SMALLDOCTYPE = "SmallDocType";

	// 部件归档流程名称
	private static final String PART_WORKFLOWNAME = "GenericPartWF";
	// 文档归档流程名称
	private static final String DOC_WORKFLOWNAME = "APPO_DocArchivedWF";

	@Override
	public FormResult postProcess(NmCommandBean nmCommandBean, List<ObjectBean> objectBeans) throws WTException {
		FormResult formResult = new FormResult();
		formResult.setStatus(FormProcessingStatus.SUCCESS);

		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		try {
			WTPart parentPart = (WTPart) nmCommandBean.getActionOid().getRefObject();
			if (LOG.isDebugEnabled()) {
				LOG.debug("parentPart : " + parentPart.getDisplayIdentity());
			}
			// 获取用户上传的文件对象
			HttpServletRequest request = nmCommandBean.getRequest();
			File file = (File) request.getAttribute("txtFile");
			// 解析上传文件
			ProductStructureTemplateExcelReader reader = new ProductStructureTemplateExcelReader(file);
			reader.read();
			List<ProductStructureTemplateExcelBean> datasArray = reader.getBeanList();
			if (datasArray == null || datasArray.size() < 3) {
				throw new WTException("上传表单数据为空!");
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("datasArray : " + datasArray);
			}
			// 检查本次导入数据是否存在异常
			checkDatas(parentPart, datasArray, getAllTypeInfo(), getClassifications(datasArray),
					(new PartWorkflowUtil()).getEnumeratedMap("sscpx"));
			// 批量创建部件对象，并构建BOM结构
			Map<ProductStructureTemplateExcelBean, WTPart> partsMap = createParts(parentPart, datasArray);
			// 批量创建文档对象，并构建部件与文档关系
			createDocs(parentPart, datasArray, partsMap);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		} finally {
			SessionServerHelper.manager.setAccessEnforced(flag);
		}

		return formResult;
	}

	/***
	 * 批量创建部件对象，并构建BOM结构
	 * 
	 * @param parentPart
	 *            顶层部件对象
	 * @param datasArray
	 * @return
	 * @throws WTException
	 */
	public Map<ProductStructureTemplateExcelBean, WTPart> createParts(WTPart parentPart,
			List<ProductStructureTemplateExcelBean> datasArray) throws WTException {
		Map<ProductStructureTemplateExcelBean, WTPart> resultMap = new HashMap<ProductStructureTemplateExcelBean, WTPart>();
		// 提取部件与部件关系数据
		Map<ProductStructureTemplateExcelBean, Collection<ProductStructureTemplateExcelBean>> partLinkMap = extractPartLinks(
				datasArray);
		if (partLinkMap == null || partLinkMap.size() == 0) {
			LOG.error("本地导入未提取出BOM结构! " + datasArray);
			return resultMap;
		}
		// 父件视图
		ViewReference viewReference = parentPart.getView();
		// 当前用户
		WTPrincipal principal = SessionHelper.manager.getPrincipal();
		try {
			for (Map.Entry<ProductStructureTemplateExcelBean, Collection<ProductStructureTemplateExcelBean>> entryMap : partLinkMap
					.entrySet()) {
				// 父件
				WTPart newParentPart = null;
				ProductStructureTemplateExcelBean parentExcelBean = entryMap.getKey();
				// 设置当前操作用户
				if (parentExcelBean.getUser() != null) {
					SessionHelper.manager.setPrincipal(parentExcelBean.getUser().getName());
				}
				if (parentExcelBean.getLevel().equals("0")) {
					newParentPart = parentPart;
				} else {
					if (!resultMap.containsKey(parentExcelBean)) {
						if (PIStringUtils.isNull(parentExcelBean.getNumber())) {
							newParentPart = createPart(parentExcelBean, viewReference);
						} else {
							newParentPart = PIPartHelper.service.findWTPart(parentExcelBean.getNumber(),
									(View) viewReference.getObject());
							if (newParentPart == null) {
								throw new WTException((parentExcelBean.getUser() == null ? (WTUser) principal
										: parentExcelBean.getUser()).getFullName() + " 无法查看："
										+ parentExcelBean.getNumber() + " 部件!");
							}
						}
					} else {
						newParentPart = resultMap.get(parentExcelBean);
					}
				}
				// 子件集合处理
				for (ProductStructureTemplateExcelBean childBean : entryMap.getValue()) {
					// 设置当前操作用户
					if (childBean.getUser() != null) {
						SessionHelper.manager.setPrincipal(childBean.getUser().getName());
					}
					WTPart newChildPart = null;
					if (!resultMap.containsKey(childBean)) {
						if (PIStringUtils.isNull(childBean.getNumber())) {
							newChildPart = createPart(childBean, viewReference);
						} else {
							newChildPart = PIPartHelper.service.findWTPart(childBean.getNumber(),
									(View) viewReference.getObject());
						}
						resultMap.put(childBean, newChildPart);
					} else {
						newChildPart = resultMap.get(childBean);
					}
					// 检出父件：该方法如果父件被检出获取父件副本，未检出则检出
					newParentPart = (WTPart) PICoreHelper.service.checkoutObject(newParentPart);
					// 创建WTPartUsageLink
					WTPartUsageLink usageLink = WTPartUsageLink.newWTPartUsageLink(newParentPart,
							(WTPartMaster) newChildPart.getMaster());
					// 保存Link
					PersistenceHelper.manager.save(usageLink);
				}
				// 检入父件
				newParentPart = (WTPart) PICoreHelper.service.checkinObject(newParentPart);
				resultMap.put(parentExcelBean, newParentPart);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		} finally {
			SessionHelper.manager.setPrincipal(principal.getName());
		}

		return resultMap;
	}

	/***
	 * 创建部件对象
	 * 
	 * @param excelBean
	 * @param viewReference
	 * @return
	 * @throws WTException
	 */
	public WTPart createPart(ProductStructureTemplateExcelBean excelBean, ViewReference viewReference)
			throws WTException {
		if (excelBean == null) {
			return null;
		}

		try {
			WTPart newPart = WTPart.newWTPart();
			// 设置名称
			newPart.setName(excelBean.getName());
			// 设置视图
			newPart.setView(viewReference);
			// 设置文件夹
			FolderHelper.assignLocation((FolderEntry) newPart, excelBean.getFolder());
			// 收集部件
			if (PIStringUtils.isNotNull(excelBean.getCollectPart())) {
				newPart.setHidePartInStructure(("是").equals(excelBean.getCollectPart()));
			}
			// 设置‘视图’
			newPart.setView(ViewReference.newViewReference(ViewHelper.service.getView("Design")));
			// 设置‘源’
			Source source = Source.toSource("make");
			newPart.setSource(source);
			// 保存对象
			newPart = (WTPart) PersistenceHelper.manager.save(newPart);
			// 设置属性
			Map<String, Object> ibaMap = new HashMap<String, Object>();
			ibaMap.put(CLASSIFICATION, excelBean.getPartClassification());
			ibaMap.put(REMARKS, excelBean.getPartRemarks());
			newPart = (WTPart) PIAttributeHelper.service.forceUpdateSoftAttributes(newPart, ibaMap);
			// 设置 所属产品线 分类属性
			ibaMap = new HashMap<String, Object>();
			ibaMap.put("sscpx", excelBean.getProductCategoryInterior());
			newPart = (WTPart) PIAttributeHelper.service.forceUpdateSoftAttributes(newPart, ibaMap);
			// 设置编码
			CusCreatePartAndCADDocFormProcessor processor = new CusCreatePartAndCADDocFormProcessor();
			processor.setNumber(newPart);
			// 部件启动归档流程
			startWorkFlow(newPart, PART_WORKFLOWNAME, excelBean.getUser());
			return newPart;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		}
	}

	/***
	 * 批量创建文档对象，并构建部件与文档关系
	 * 
	 * @param parentPart
	 * @param datasArray
	 * @param partsMap
	 * @throws WTException
	 */
	public void createDocs(WTPart parentPart, List<ProductStructureTemplateExcelBean> datasArray,
			Map<ProductStructureTemplateExcelBean, WTPart> partsMap) throws WTException {
		if (datasArray == null || datasArray.size() == 0) {
			return;
		}

		// 提取部件数据与文档数据关系
		Map<ProductStructureTemplateExcelBean, Collection<ProductStructureTemplateExcelBean>> partDocLinkMap = extractPartDocumentLinks(
				datasArray);
		if (partDocLinkMap == null || partDocLinkMap.size() == 0) {
			LOG.error("部件与文档关系提取异常! " + datasArray);
			return;
		}

		// 当前用户
		WTPrincipal principal = SessionHelper.manager.getPrincipal();
		try {
			for (Map.Entry<ProductStructureTemplateExcelBean, Collection<ProductStructureTemplateExcelBean>> entryMap : partDocLinkMap
					.entrySet()) {
				// 部件
				WTPart part = null;
				if (partsMap.containsKey(entryMap.getKey())) {
					part = partsMap.get(entryMap.getKey());
				} else {
					part = parentPart;
				}
				if (part == null) {
					throw new WTException(entryMap.getKey().toString() + " 对应部件创建失败!");
				}
				if (ChangeUtils.getNumber(part).equals(ChangeUtils.getNumber(parentPart))) {
					if (entryMap.getValue().size() > 0) {
						throw new WTException("部件编码：" + ChangeUtils.getNumber(parentPart) + " 引用部件不允许关联文档!");
					}
				}
				for (ProductStructureTemplateExcelBean excelBean : entryMap.getValue()) {
					// 设置当前操作用户
					if (excelBean.getUser() != null) {
						SessionHelper.manager.setPrincipal(excelBean.getUser().getName());
					}
					// 文档对象
					WTDocument newDoc = null;
					if (PIStringUtils.isNotNull(excelBean.getNumber())) {
						newDoc = PIDocumentHelper.service.findWTDocument(excelBean.getNumber());
					} else {
						newDoc = createDoc(excelBean);
					}
					// 创建关系
					createPartDocLink(part, newDoc);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		} finally {
			SessionHelper.manager.setPrincipal(principal.getName());
		}
	}

	/***
	 * 创建文档对象
	 * 
	 * @param excelBean
	 * @return
	 * @throws WTException
	 */
	public WTDocument createDoc(ProductStructureTemplateExcelBean excelBean) throws WTException {
		if (excelBean == null) {
			return null;
		}

		try {
			WTDocument wtDoc = WTDocument.newWTDocument();
			// 设置名称
			wtDoc.setName(excelBean.getName());
			// 设置类型
			wtDoc.setTypeDefinitionReference(excelBean.getTypeReference());
			// 设置文件夹路径
			FolderHelper.assignLocation((FolderEntry) wtDoc, excelBean.getFolder());
			// 保存对象
			wtDoc = (WTDocument) PersistenceHelper.manager.save(wtDoc);
			if (PIStringUtils.isNotNull(excelBean.getDocSubclass())) {
				wtDoc = (WTDocument) PIAttributeHelper.service.forceUpdateSoftAttribute(wtDoc, SMALLDOCTYPE,
						excelBean.getDocSubclass());
			}
			// 文档启动归档流程
			startWorkFlow(wtDoc, DOC_WORKFLOWNAME, excelBean.getUser());
			return wtDoc;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		}
	}

	/***
	 * 创建部件与文档之前的关系
	 * 
	 * @param part
	 *            部件对象
	 * @param wtDoc
	 *            文档对象
	 * @throws WTException
	 */
	public void createPartDocLink(WTPart part, WTDocument wtDoc) throws WTException {
		if (part == null || wtDoc == null) {
			return;
		}

		try {
			// 判断对象是否为参考文档类型
			Boolean isReferenceDoc = PartDocHelper.isReferenceDocument(wtDoc);
			if (isReferenceDoc) {
				QueryResult qr = PersistenceHelper.manager.find(WTPartReferenceLink.class, part,
						WTPartReferenceLink.ROLE_AOBJECT_ROLE, wtDoc.getMaster());
				if (!qr.hasMoreElements()) {
					PersistenceServerHelper.manager.insert(
							WTPartReferenceLink.newWTPartReferenceLink(part, (WTDocumentMaster) wtDoc.getMaster()));
				}
			} else {
				QueryResult qr = PersistenceHelper.manager.find(WTPartDescribeLink.class, part,
						WTPartDescribeLink.DESCRIBES_ROLE, wtDoc);
				if (!qr.hasMoreElements()) {
					PersistenceServerHelper.manager.insert(WTPartDescribeLink.newWTPartDescribeLink(part, wtDoc));
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		}
	}

	/***
	 * 提取部件与部件关系数据
	 * 
	 * @param datasArray
	 * @return
	 */
	public Map<ProductStructureTemplateExcelBean, Collection<ProductStructureTemplateExcelBean>> extractPartLinks(
			List<ProductStructureTemplateExcelBean> datasArray) {
		Map<ProductStructureTemplateExcelBean, Collection<ProductStructureTemplateExcelBean>> partLinkMap = new HashMap<ProductStructureTemplateExcelBean, Collection<ProductStructureTemplateExcelBean>>();
		if (datasArray == null || datasArray.size() < 3) {
			return partLinkMap;
		}
		// 临时数据
		List<ProductStructureTemplateExcelBean> tempArray = datasArray;

		Collection<ProductStructureTemplateExcelBean> partArray = null;
		;
		for (int i = datasArray.size() - 1; i > 0; i--) {
			ProductStructureTemplateExcelBean excelBean = datasArray.get(i);
			if (PIStringUtils.isNull(excelBean.getLevel())) {
				continue;
			}
			// 父件
			ProductStructureTemplateExcelBean parentExcelBean = null;
			Integer level = Integer.parseInt(excelBean.getLevel()) - 1;
			if (level == -1) {
				continue;
			}
			Boolean bol = false;
			String parentLevel = String.valueOf(level);
			for (int j = tempArray.size() - 1; j > 0; j--) {
				parentExcelBean = tempArray.get(j);
				if (excelBean.equals(parentExcelBean)) {
					bol = true;
				}
				if (bol) {
					if (parentLevel.equals(parentExcelBean.getLevel())) {
						break;
					}
				}
				parentExcelBean = null;
			}
			if (parentExcelBean != null) {
				if (partLinkMap.containsKey(parentExcelBean)) {
					partArray = partLinkMap.get(parentExcelBean);
				} else {
					partArray = new HashSet<ProductStructureTemplateExcelBean>();
				}
				partArray.add(excelBean);
				partLinkMap.put(parentExcelBean, partArray);
			}
		}

		return partLinkMap;
	}

	/***
	 * 提取部件数据与文档数据关系
	 * 
	 * @param datasArray
	 * @return
	 * @throws WTException
	 */
	public Map<ProductStructureTemplateExcelBean, Collection<ProductStructureTemplateExcelBean>> extractPartDocumentLinks(
			List<ProductStructureTemplateExcelBean> datasArray) throws WTException {
		Map<ProductStructureTemplateExcelBean, Collection<ProductStructureTemplateExcelBean>> partDocLinkMap = new HashMap<ProductStructureTemplateExcelBean, Collection<ProductStructureTemplateExcelBean>>();
		if (datasArray == null || datasArray.size() == 0) {
			return partDocLinkMap;
		}

		Collection<ProductStructureTemplateExcelBean> docArray = new HashSet<ProductStructureTemplateExcelBean>();
		for (int i = 1; i < datasArray.size(); i++) {
			ProductStructureTemplateExcelBean excelBean = datasArray.get(i);
			if (PIStringUtils.isNotNull(excelBean.getLevel())) {
				docArray = new HashSet<ProductStructureTemplateExcelBean>();
				partDocLinkMap.put(excelBean, docArray);
				continue;
			}
			docArray.add(excelBean);
		}

		// 产品结构搭建，引用的部件下不允许新建文档
		for (Map.Entry<ProductStructureTemplateExcelBean, Collection<ProductStructureTemplateExcelBean>> entryMap : partDocLinkMap
				.entrySet()) {
			String partNumber = entryMap.getKey().getNumber();
			if (PIStringUtils.isNotNull(partNumber)) {
				if (entryMap.getValue().size() > 0) {
					throw new WTException("部件编码：" + partNumber + " 引用部件不允许关联文档!");
				}
			}
		}

		return partDocLinkMap;
	}

	/***
	 * 检查本次导入数据是否存在异常
	 * 
	 * @param parentPart
	 *            操作部件
	 * @param datasArray
	 *            用户传入的数据
	 * @param typeInfoMap
	 *            系统类型
	 * @param clfInfoMap
	 *            本次涉及的分类信息
	 * @param enumeratedMap
	 *            所属产品线枚举
	 * @throws WTException
	 */
	public void checkDatas(WTPart parentPart, List<ProductStructureTemplateExcelBean> datasArray,
			Map<String, Map<TypeDefinitionReference, TypeDefinitionReadView>> typeInfoMap,
			Map<String, String> clfInfoMap, Map<ArrayList<String>, ArrayList<String>> enumeratedMap)
					throws WTException {
		if (datasArray == null || datasArray.size() < 3) {
			throw new WTException("上传表单数据为空!");
		}

		if (typeInfoMap == null || typeInfoMap.size() == 0) {
			throw new WTException("系统类型获取异常!");
		}
		// 父件视图
		View view = (View) parentPart.getView().getObject();
		if (LOG.isDebugEnabled()) {
			LOG.debug("View : " + view.toString());
		}
		try {
			StringBuilder errorMsg = new StringBuilder();
			for (int i = 2; i < datasArray.size(); i++) {
				ProductStructureTemplateExcelBean excelBean = datasArray.get(i);
				// 编码检查
				String number = excelBean.getNumber();
				if (PIStringUtils.isNotNull(number)) {
					if (PIStringUtils.isNull(excelBean.getLevel())) {
						// 文档对象
						if (PIDocumentHelper.service.findWTDocument(number) == null) {
							errorMsg.append("第" + (i + 1) + "行：编码 " + number + " 不存在对应文档!");
							errorMsg.append("\n");
							LOG.error("第" + (i + 1) + "行：" + excelBean);
						} else {
							continue;
						}
					} else {
						// 部件对象
						if (PIPartHelper.service.findWTPart(number, view) == null) {
							errorMsg.append("第" + (i + 1) + "行：编码 " + number + " 不存在对应部件!");
							errorMsg.append("\n");
							LOG.error("第" + (i + 1) + "行：" + excelBean);
						} else {
							continue;
						}
					}
				}
				// 必填属性
				if ((PIStringUtils.isNull(excelBean.getLevel())
						&& PIStringUtils.isNotNull(excelBean.getPartClassification()))
						|| (PIStringUtils.isNotNull(excelBean.getLevel())
								&& PIStringUtils.isNull(excelBean.getPartClassification()))) {
					if (PIStringUtils.isNull(excelBean.getLevel())) {
						errorMsg.append("第" + (i + 1) + "行：层次 不能为空!");
						errorMsg.append("\n");
					} else if (PIStringUtils.isNull(excelBean.getPartClassification())) {
						errorMsg.append("第" + (i + 1) + "行：部件分类 不能为空!");
						errorMsg.append("\n");
					}
					LOG.error("第" + (i + 1) + "行：" + excelBean);
				}
				// 名称检查
				if (PIStringUtils.isNull(number)) {
					if (PIStringUtils.isNull(excelBean.getName())) {
						errorMsg.append("第" + (i + 1) + "行：名称为空!");
						errorMsg.append("\n");
						LOG.error("第" + (i + 1) + "行：" + excelBean);
					}
				}
				// 分类全路径
				String nodeHierarchy = null;
				// 部件分类检查
				if (PIStringUtils.isNull(number) && PIStringUtils.isNotNull(excelBean.getLevel())) {
					String clfDisplay = excelBean.getPartClassification();
					if (PIStringUtils.isNotNull(clfDisplay)) {
						if (clfInfoMap == null || clfInfoMap.size() == 0) {
							throw new WTException("分类节点获取失败!");
						}
						if (clfInfoMap.containsKey(clfDisplay)) {
							excelBean.setPartClassification(clfInfoMap.get(clfDisplay));
							// TODO 检查路径是否正确
							// 获取分类全路径
							nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(clfInfoMap.get(clfDisplay));
							// 分类中大类的内部值
							String nodeSecondInsideValue = "";
							if (nodeHierarchy != null) {
								if (nodeHierarchy.indexOf(FileConstantIfc.UNIX_PATH_SEPARATOR) > -1) {
									String[] split = nodeHierarchy.split(FileConstantIfc.UNIX_PATH_SEPARATOR);
									nodeSecondInsideValue = split[1];
								} else {
									nodeSecondInsideValue = nodeHierarchy;
								}
							}
							// 配置表中的文件夹名称
							Map<String, String> readSheet5 = new AppoExcelUtil().readSheet5();
							// 合规文件夹
							String folderNameForExcel = readSheet5.get(nodeSecondInsideValue);
							if (PIStringUtils.isNotNull(excelBean.getFolderPath())) {
								if (!excelBean.getFolderPath().contains(folderNameForExcel)) {
									errorMsg.append("第" + (i + 1) + "行：位置 " + excelBean.getFolderPath() + " 不能存放 分类为："
											+ clfDisplay + " 的部件!");
									errorMsg.append("\n");
									LOG.error("第" + (i + 1) + "行：" + excelBean);
								}
							}
						} else {
							errorMsg.append("第" + (i + 1) + "行：部件分类 " + clfDisplay + " 系统不存在!");
							errorMsg.append("\n");
							LOG.error("第" + (i + 1) + "行：" + excelBean);
						}
					}
				}
				// 所属产品类别检查
				if (PIStringUtils.isNotNull(nodeHierarchy)) {
					Boolean isCheck = false;
					if (nodeHierarchy.indexOf(FileConstantIfc.UNIX_PATH_SEPARATOR) > -1) {
						String[] split = nodeHierarchy.split(FileConstantIfc.UNIX_PATH_SEPARATOR);
						for (int j = 0; j < split.length; j++) {
							String nodeValue = split[j];
							if (("半成品").equals(nodeValue) || ("appo_bcp").equals(nodeValue)) {
								isCheck = true;
								break;
							}
						}
					} else {
						if (("半成品").equals(nodeHierarchy) || ("appo_bcp").equals(nodeHierarchy)) {
							isCheck = true;
						}
					}
					if (isCheck) {
						String productCategory = excelBean.getProductCategory();
						if (PIStringUtils.isNotNull(productCategory)) {
							if (enumeratedMap != null) {
								for (Map.Entry<ArrayList<String>, ArrayList<String>> entryMap : enumeratedMap
										.entrySet()) {
									// 显示名称集合
									ArrayList<String> displayArray = entryMap.getValue();
									// 内部名称
									ArrayList<String> interiorArray = entryMap.getKey();
									for (int j = 0; j < displayArray.size(); j++) {
										if (productCategory.equals(displayArray.get(j))) {
											excelBean.setProductCategoryInterior(interiorArray.get(j));
											break;
										}
									}
								}
							}
							if (PIStringUtils.isNull(excelBean.getProductCategoryInterior())) {
								errorMsg.append("第" + (i + 1) + "行：所属产品类别 " + productCategory + " 为无效属性值!");
								errorMsg.append("\n");
								LOG.error("第" + (i + 1) + "行：" + excelBean);
							}
							// 检查所属产品类别 是否符合要求
							Map<String, List<String>> readSheet4 = new AppoExcelUtil().readSheet4();
							if (readSheet4 != null && readSheet4.size() > 0) {
								if (readSheet4.containsKey(parentPart.getContainer().getName())) {
									List<String> list = readSheet4.get(parentPart.getContainer().getName());
									if (list != null) {
										if (!list.contains(productCategory)) {
											errorMsg.append("第" + (i + 1) + "行：所属产品类别 " + productCategory + " 无法在库："
													+ parentPart.getContainer().getName() + " 下创建!");
											errorMsg.append("\n");
											LOG.error("第" + (i + 1) + "行：" + excelBean);
										}
									}
								}
							}
						} else {
							errorMsg.append("第" + (i + 1) + "行：所属产品类别 不能为空!");
							errorMsg.append("\n");
							LOG.error("第" + (i + 1) + "行：" + excelBean);
						}
					}
				}
				// 位置检查
				if (PIStringUtils.isNull(number)) {
					if (!checkFolderPathExist(excelBean, parentPart.getContainerReference())) {
						errorMsg.append("第" + (i + 1) + "行：位置 " + excelBean.getFolderPath() + " 不存在!");
						errorMsg.append("\n");
						LOG.error("第" + (i + 1) + "行：" + excelBean);
					}
				}
				// 拟制人检查
				if (PIStringUtils.isNull(number)) {
					Collection<WTPrincipal> userArray = ChangeUtils.findWTUsers(excelBean.getFictitiousPerson());
					if (userArray == null || userArray.size() == 0) {
						errorMsg.append("第" + (i + 1) + "行：拟制人 " + excelBean.getFictitiousPerson() + " 不存在!");
						errorMsg.append("\n");
						LOG.error("第" + (i + 1) + "行：" + excelBean);
					} else {
						excelBean.setUser((WTUser) userArray.iterator().next());
					}
				}
				// 文档类型检查
				if (PIStringUtils.isNull(excelBean.getLevel())) {
					// 文档大类
					String docType = excelBean.getDocType();
					if (typeInfoMap.containsKey(excelBean.getDocType())) {
						TypeDefinitionReference typtReference = typeInfoMap.get(docType).keySet().iterator().next();
						excelBean.setTypeReference(typtReference);

						// 文档小类是否符合要求
						Map<ArrayList<String>, ArrayList<String>> ruleMap = new HashMap<ArrayList<String>, ArrayList<String>>();
						ProductNamingNoticDataUtility dataUtility = new ProductNamingNoticDataUtility();
						dataUtility.getAttributeRule(typeInfoMap.get(docType).get(typtReference), SMALLDOCTYPE,
								ruleMap);
						if (ruleMap.size() > 0) {
							ArrayList<String> displayArray = ruleMap.values().iterator().next();
							if (displayArray.size() > 0) {
								String smallDocType = excelBean.getDocSubclass() == null ? ""
										: excelBean.getDocSubclass();
								if (!displayArray.contains(smallDocType)) {
									errorMsg.append("第" + (i + 1) + "行：文档小类 " + smallDocType + " 不符合系统要求!");
									errorMsg.append("\n");
									LOG.error("第" + (i + 1) + "行：" + excelBean);
								}
							}
						}
					} else {
						errorMsg.append("第" + (i + 1) + "行：文档大类 " + docType + " 系统不存在!");
						errorMsg.append("\n");
						LOG.error("第" + (i + 1) + "行：" + excelBean);
					}
				}
			}
			if (errorMsg.length() > 0) {
				throw new WTException(errorMsg.toString());
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		}
	}

	/***
	 * 检查文档对象是否存在
	 * 
	 * @param excelBean
	 * @param container
	 *            库
	 * @return
	 * @throws WTException
	 */
	public Boolean checkFolderPathExist(ProductStructureTemplateExcelBean excelBean, WTContainerRef containerRef)
			throws WTException {
		String folderPath = excelBean.getFolderPath();
		if (PIStringUtils.isNull(folderPath)) {
			return false;
		}

		Folder folder = FolderHelper.service.getFolder(folderPath, containerRef);
		if (folder == null) {
			return false;
		} else {
			excelBean.setFolder(folder);
		}

		return true;
	}

	/***
	 * 查询系统中所有类型
	 * 
	 * @return
	 * @throws WTException
	 */
	public Map<String, Map<TypeDefinitionReference, TypeDefinitionReadView>> getAllTypeInfo() throws WTException {
		Map<String, Map<TypeDefinitionReference, TypeDefinitionReadView>> datasMap = new HashMap<String, Map<TypeDefinitionReference, TypeDefinitionReadView>>();

		try {
			QuerySpec qs = new QuerySpec(WTTypeDefinition.class);
			QueryResult qr = PersistenceServerHelper.manager.query(qs);
			LatestConfigSpec ls = new LatestConfigSpec();
			qr = ls.process(qr);
			while (qr.hasMoreElements()) {
				WTTypeDefinition typeDef = (WTTypeDefinition) qr.nextElement();
				// 类型逻辑标识符
				String logicalIdentifier = typeDef.getLogicalIdentifier();
				// 类型定义
				TypeDefinitionReadView readView = TypeDefinitionServiceHelper.service.getTypeDefView(logicalIdentifier);
				// 类型存储
				Map<TypeDefinitionReference, TypeDefinitionReadView> collectMap = new HashMap<TypeDefinitionReference, TypeDefinitionReadView>();
				collectMap.put(TypedUtilityServiceHelper.service.getTypeDefinitionReference(logicalIdentifier),
						readView);
				datasMap.put(PropertyHolderHelper.getDisplayName(readView, Locale.CHINESE), collectMap);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("getAllTypeInfo() : " + datasMap);
		}
		return datasMap;
	}

	/***
	 * 查询指定分类信息
	 * 
	 * @param datasArray
	 * @return key : 分类节点显示名称 value : 分类节点内部名称
	 * @throws WTException
	 */
	private Map<String, String> getClassifications(List<ProductStructureTemplateExcelBean> datasArray)
			throws WTException {
		Map<String, String> resultMap = new HashMap<String, String>();
		if (datasArray == null || datasArray.size() == 0) {
			return resultMap;
		}

		Collection<String> displayArray = new HashSet<String>();
		int index = 1;
		for (ProductStructureTemplateExcelBean excelBean : datasArray) {
			if (index < 3) {
				index++;
				continue;
			}
			if (PIStringUtils.isNotNull(excelBean.getPartClassification())) {
				displayArray.add(excelBean.getPartClassification());
			}
			index++;
		}
		resultMap = getClassificationsByDisplayNames(displayArray);

		return resultMap;
	}

	/***
	 * 查询系统分类信息
	 * 
	 * @param displayArray
	 *            分类显示名称
	 * @return key : 分类节点显示名称 value : 分类节点内部名称
	 * @throws WTException
	 */
	public Map<String, String> getClassificationsByDisplayNames(Collection<String> displayArray) throws WTException {
		Map<String, String> resultMap = new HashMap<String, String>();
		if (displayArray == null || displayArray.size() == 0) {
			return resultMap;
		}

		QuerySpec qs = new QuerySpec();
		qs.addClassList(LWCStructEnumAttTemplate.class, true);
		qs.addClassList(LWCLocalizablePropertyValue.class, true);

		String[] displayStr = new String[displayArray.size()];
		displayArray.toArray(displayStr);
		ArrayExpression displayArrayExpression = new ArrayExpression(displayStr);
		ClassAttribute displayAttribute = new ClassAttribute(LWCLocalizablePropertyValue.class,
				LWCLocalizablePropertyValue.VALUE);
		SearchCondition sc = new SearchCondition(displayAttribute, SearchCondition.IN, displayArrayExpression);
		qs.appendWhere(sc, new int[] { 1 });
		qs.appendAnd();
		sc = new SearchCondition(LWCStructEnumAttTemplate.class, "thePersistInfo.theObjectIdentifier.id",
				LWCLocalizablePropertyValue.class, "holderReference.key.id");
		qs.appendWhere(sc, new int[] { 0, 1 });
		QueryResult qr = PersistenceServerHelper.manager.query(qs);
		while (qr.hasMoreElements()) {
			Object[] objs = (Object[]) qr.nextElement();
			if (objs != null) {
				resultMap.put(((LWCLocalizablePropertyValue) objs[1]).getValue(),
						((LWCStructEnumAttTemplate) objs[0]).getName());
			}
		}

		return resultMap;
	}

	/***
	 * 启动指定流程模板
	 * 
	 * @param pbo
	 *            流程主题
	 * @param workFlowName
	 *            流程模板
	 * @param processCreator
	 *            流程启动者
	 * @return
	 * @throws WTException
	 */
	public boolean startWorkFlow(WTObject pbo, String workFlowName, WTPrincipal processCreator) throws WTException {
		boolean startSuccess = false;
		if (pbo == null || PIStringUtils.isNull(workFlowName)) {
			return startSuccess;
		}

		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		try {
			WorkFlowBase workFlowBase = new WorkFlowBase();
			workFlowBase.isRelatedRunningProcess(pbo);
			workFlowBase.getErrorMessge();

			WTProperties wtproperties = WTProperties.getLocalProperties();
			String WFTEMPLATE_PATH = wtproperties.getProperty("wt.home", "")
					+ "/codebase/ext/generic/cfg/WFTemplate.xlsx";
			ExcelCacheManager.setWorkFlowExcelCache(WFTEMPLATE_PATH, workFlowName);

			// 搜索流程模板
			QuerySpec querysearch = new QuerySpec(WfProcessTemplate.class);
			querysearch.appendWhere(new SearchCondition(WfProcessTemplate.class, "name", "=", workFlowName),
					new int[] { 0 });
			OrderBy orderby = new OrderBy(new ClassAttribute(WfProcessTemplate.class, "thePersistInfo.modifyStamp"),
					true);
			querysearch.appendOrderBy(orderby, new int[] { 0 });
			QueryResult queryresult = PersistenceHelper.manager.find(querysearch);
			WfProcessTemplate wfprocesstemplate = null;
			if (queryresult.hasMoreElements()) {
				wfprocesstemplate = (WfProcessTemplate) queryresult.nextElement();
				// 最新流程模板
				wfprocesstemplate = WfDefinerHelper.service
						.getLatestIteration((WfProcessTemplateMaster) wfprocesstemplate.getMaster());
				// 流程创建者
				WTPrincipalReference wtprincipalreference = null;
				if (processCreator != null) {
					wtprincipalreference = WTPrincipalReference.newWTPrincipalReference(processCreator);
				} else {
					wtprincipalreference = WTPrincipalReference
							.newWTPrincipalReference(SessionHelper.manager.getPrincipal());
				}
				// 流程名称
				String processName = workFlowName + "_" + pbo.getDisplayIdentifier();
				// 创建进程对象
				WfProcess wfprocess = WfEngineHelper.service.createProcess(wfprocesstemplate,
						((LifeCycleManaged) pbo).getLifeCycleTemplate(), ((WTContained) pbo).getContainerReference());
				WfEngineServerHelper.service.setPrimaryBusinessObject(wfprocess, pbo);
				ProcessData processdata = wfprocess.getContext();
				wfprocess.setName(processName);
				wfprocess.setCreator(wtprincipalreference);
				wfprocess.setDescription(wfprocesstemplate.getDescription());
				TeamReference team = ((_TeamManaged) pbo).getTeamId();
				wfprocess.setTeamId(team);
				wfprocess = (WfProcess) PersistenceHelper.manager.save(wfprocess);
				wfprocess = wfprocess.start(processdata, true, ((WTContained) pbo).getContainerReference());
				if (wfprocess != null) {
					startSuccess = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		} finally {
			SessionServerHelper.manager.setAccessEnforced(flag);
		}

		return startSuccess;
	}
}
