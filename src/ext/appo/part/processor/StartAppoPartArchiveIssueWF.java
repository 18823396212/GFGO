
/**
 * <pre>
 * 修改记录：05
 * 修改日期：2019-11-01 
 * 修   改  人：毛兵义
 * 关联活动：
 * 修改内容：修改发布成品BOM的权限（已归档），以前为修改者，现在更改为，对正在工作状态有修改权限的用户
 * </pre>
 */

/**
 * <pre>
 * 修改记录：06
 * 修改日期：2019-11-22 
 * 修   改  人：毛兵义
 * 关联活动：
 * 修改内容：去掉设计视图中不能包含制造视图的限制
 * </pre>
 */
package ext.appo.part.processor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ext.appo.ecn.common.util.ChangeUtils;
import org.apache.log4j.Logger;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.util.FeedbackMessage;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.core.ui.resources.FeedbackType;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.workflow.NmWorkflowHelper;

import ext.appo.util.PartUtil;
import ext.com.workflow.WorkflowUtil;
import ext.generic.workflow.WorkFlowBase;
import ext.lang.PIStringUtils;
import ext.pi.PIException;
import ext.pi.core.PIAttributeHelper;
import wt.access.AccessControlHelper;
import wt.access.AccessPermission;
import wt.access.AdHocAccessKey;
import wt.access.AdHocControlled;
import wt.admin.AdminDomainRef;
import wt.change2.ChangeHelper2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.doc.DocumentVersion;
import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.fc.ObjectVector;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTKeyedMap;
import wt.folder.SubFolder;
import wt.identity.IdentityFactory;
import wt.inf.container.WTContained;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerRef;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.LifeCycleTemplateReference;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.PartDocHelper;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.pds.StatementSpec;
import wt.pom.PersistentObjectManager;
import wt.pom.Transaction;
import wt.query.ClassAttribute;
import wt.query.OrderBy;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.WhereExpression;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.team.TeamManaged;
import wt.team.TeamReference;
import wt.type.TypedUtility;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.workflow.definer.WfDefinerHelper;
import wt.workflow.definer.WfProcessTemplate;
import wt.workflow.definer.WfProcessTemplateMaster;
import wt.workflow.engine.ProcessData;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfEngineServerHelper;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfState;

/**
 * @author HYJ&NJH 四合一流程启动入口处理类
 */
public class StartAppoPartArchiveIssueWF extends DefaultObjectFormProcessor {

	private static String CLASSNAME = StartAppoPartArchiveIssueWF.class.getName();
	private static final Logger LOGGER = LogR.getLogger(CLASSNAME);
	// 设计部件归档ManualStartProcessor
	private final String DA = "DA";
	// 设计部件发布DesignPartStartProcessor
	private final String DR = "DR";
	// 工程部件归档ManualStartManufacturingProcessor
	private final String MA = "MA";
	// 工程部件发布EngineerPartStartProcessor
	private final String MR = "MR";

	@Override
	public FormResult doOperation(NmCommandBean nmCommandBean, List<ObjectBean> objectBeans) throws WTException {
		// 获取对象
		FormResult doOperation = super.doOperation(nmCommandBean, objectBeans);
		Object refObject = nmCommandBean.getActionOid().getRefObject();
		// 判断对象类型
		if (refObject != null && refObject instanceof WTPart) {
			WTPart part = (WTPart) refObject;
			// if(ChangeUtils.specificNode(part, "资产类") ||
			// ChangeUtils.specificNode(part, "appo_zcl")){
			// // 设置状态
			// part = (WTPart)PICoreHelper.service.setLifeCycleState(part,
			// "RELEASED") ;
			// // 发布数据
			// WTArrayList releaseArray = new WTArrayList() ;
			// releaseArray.add(part) ;
			// startWfProcess(findWfProcessTemplate("ERP集成"), part,
			// releaseArray) ;
			// }else{
			String stateVal = part.getLifeCycleState().toString();
			// 必须是正在工作或已归档才可以启动流程
			if (stateVal.equals("INWORK") || stateVal.equals("ARCHIVED")) {
				// 首先部件不能被检出
				if (!WorkflowUtil.isObjectCheckedOut(part)) {
					// 部件必须是最新的
					if (isLatestObject(part)) {
						String doctype = getObjectType(part);
						String type = "WCTYPE|" + doctype;
						SubFolder folder = (SubFolder) (part.getFolderingInfo().getParentFolder().getObject());
						AdminDomainRef adminDomainRef = null;
						if (folder != null) {
							adminDomainRef = folder.getDomainRef();
						}
						Boolean operability = isOperability(SessionHelper.manager.getPrincipal(), type, adminDomainRef,
								State.INWORK, AccessPermission.MODIFY);
						// 如果是正在工作状态，必须是修改者；如果是已归档状态，需要针对，正在工作的当前对象有修改权限
						if ((isEqualCreator(part) && stateVal.equals("INWORK"))
								|| (stateVal.equals("ARCHIVED") && operability)
								|| (stateVal.equals("ARCHIVED") && isEqualCreator(part))) {
							// 存在视图
							String viewName = part.getViewName();
							if (viewName != null && !"".equals(viewName.trim())) {
								//add by lzy at 20191213 start
								Boolean flag=isRunningNewEcnWorkflow(part);
								System.out.println("flag=="+flag);
								if(!flag){
									//add by lzy at 20191213 end
									String wfShortName = getWFShortName(viewName, stateVal);

									WorkFlowBase workFlowBase = new WorkFlowBase();
									// 设计部件归档ManualStartProcessor
									if (wfShortName.equals(DA)) {
										doOperation = startDAWF(nmCommandBean, doOperation, part, workFlowBase);
									}
									// 设计部件发布DesignPartStartProcessor
									else if (wfShortName.equals(DR)) {
										doOperation = startDRWF(nmCommandBean, doOperation, part, workFlowBase);
									}
									// 工程部件归档ManualStartManufacturingProcessor
									else if (wfShortName.equals(MA)) {
										doOperation = startMAWF(nmCommandBean, doOperation, part, workFlowBase);
									}
									// 工程部件发布EngineerPartStartProcessor
									else if (wfShortName.equals(MR)) {
										doOperation = startMRWF(nmCommandBean, doOperation, part, workFlowBase);
									}
									//add by lzy at 20191213 start
								}else{
									doOperation = setFeedbackMessage(doOperation, nmCommandBean, false,
											"当前部件存在于ECN流程中,无法启动流程!");
								}
								//add by lzy at 20191213 end
							} else {
								doOperation = setFeedbackMessage(doOperation, nmCommandBean, false,
										"当前部件不存在'视图'，无法启动流程!");
							}
						} else {
							if (stateVal.equals("INWORK")) {
								doOperation = setFeedbackMessage(doOperation, nmCommandBean, false,
										"您不是该当前部件的修改者，无法启动流程!");
							} else {
								doOperation = setFeedbackMessage(doOperation, nmCommandBean, false,
										"您没有当前部件的修改权限，无法启动流程!");
							}
						}
					} else {
						doOperation = setFeedbackMessage(doOperation, nmCommandBean, false, "当前部件被不是最新版本，无法启动流程!");
					}
				} else {
					doOperation = setFeedbackMessage(doOperation, nmCommandBean, false, "当前部件被检出，无法启动流程!");
				}
			} else {
				doOperation = setFeedbackMessage(doOperation, nmCommandBean, false, "当前部件非'正在工作'或'已归档'状态，无法启动流程!");
			}
			// }
		}

		return doOperation;
	}

	private FormResult startMRWF(NmCommandBean nmCommandBean, FormResult doOperation, WTPart part,
								 WorkFlowBase workFlowBase) throws PIException, WTException {
		// 只有产品和半成品才能发布
		if (part.getNumber().startsWith("A") || part.getNumber().startsWith("B") || part.getNumber().startsWith("X")) {
			// 校验子件状态
			String message = checkSonState(part, false);
			// 检查属性完整性
			message = message + checkObjectIBAvalue(part);
			if (message.length() == 0) {
				// 不在其它流程中,然后启动流程
				if (!isRunningWorkflow(part, false)) {
					String workFlowName = "APPO_ReleasedManufacturingPartWF";
					if (workFlowBase.startWorkFlow(part, workFlowName)) {
						doOperation = setFeedbackMessage(doOperation, nmCommandBean, true, "启动工程部件发布流程成功!");

					} else {
						if (workFlowBase.isRelatedRunningProcess(part))
							doOperation = setFeedbackMessage(doOperation, nmCommandBean, false,
									"当前部件存在于其它流程中,无法启动工程部件发布流程!");
						else
							doOperation = setFeedbackMessage(doOperation, nmCommandBean, false, "启动工程部件发布流程失败!");

					}
				} else {
					doOperation = setFeedbackMessage(doOperation, nmCommandBean, false, "当前部件存在于其它流程中,无法启动工程部件发布流程!");
				}
			} else {
				doOperation = setFeedbackMessage(doOperation, nmCommandBean, false, message);
			}
		} else {
			doOperation = setFeedbackMessage(doOperation, nmCommandBean, false, "单个物料不允许进行发布,只有成品和半成品才可以发布!");

		}
		return doOperation;
	}

	private FormResult startMAWF(NmCommandBean nmCommandBean, FormResult doOperation, WTPart part,
								 WorkFlowBase workFlowBase) throws PIException, WTException {
		// 校验子件状态
		String message = checkSonHistoryversion(part);
		// 检查属性完整性
		message = message + checkObjectIBAvalue(part);
		if (message.length() == 0) {
			// 不在其它流程中,然后启动流程
			if (!isRunningWorkflow(part, true)) {
				String workFlowName = "GenericManufacturingPartWF";
				if (workFlowBase.startWorkFlow(part, workFlowName)) {
					doOperation = setFeedbackMessage(doOperation, nmCommandBean, true, "启动工程部件归档流程成功!");

				} else {
					if (workFlowBase.isRelatedRunningProcess(part))
						doOperation = setFeedbackMessage(doOperation, nmCommandBean, false,
								"当前部件存在于其它流程中,无法启动工程部件归档流程!");
					else
						doOperation = setFeedbackMessage(doOperation, nmCommandBean, false, "启动工程部件归档流程失败!");
				}
			} else {
				doOperation = setFeedbackMessage(doOperation, nmCommandBean, false, "当前部件存在于其它流程中,无法启动工程部件归档流程!");
			}
		} else {
			doOperation = setFeedbackMessage(doOperation, nmCommandBean, false, message);
		}
		return doOperation;
	}

	private FormResult startDRWF(NmCommandBean nmCommandBean, FormResult doOperation, WTPart part,
								 WorkFlowBase workFlowBase) throws PIException, WTException {
		// 只有产品和半成品才能发布
		if (part.getNumber().startsWith("A") || part.getNumber().startsWith("B") || part.getNumber().startsWith("X")) {
			// 校验子件状态
			String message = checkSonState(part, false);
			// 检查属性完整性
			message = message + checkObjectIBAvalue(part);
			if (message.length() == 0) {
				// 不在其它流程中,然后启动流程
				if (!isRunningWorkflow(part, false)) {
					String workFlowName = "APPO_ReleasedPartWF";
					if (workFlowBase.startWorkFlow(part, workFlowName)) {
						doOperation = setFeedbackMessage(doOperation, nmCommandBean, true, "启动设计部件发布流程成功!");

					} else {
						if (workFlowBase.isRelatedRunningProcess(part))
							doOperation = setFeedbackMessage(doOperation, nmCommandBean, false,
									"当前部件存在于其它流程中,无法启动设计部件发布流程!");
						else
							doOperation = setFeedbackMessage(doOperation, nmCommandBean, false, "启动设计部件发布流程失败!");
					}
				} else {
					doOperation = setFeedbackMessage(doOperation, nmCommandBean, false, "当前部件存在于其它流程中,无法启动设计部件发布流程!");
				}
			} else {
				doOperation = setFeedbackMessage(doOperation, nmCommandBean, false, message);
			}
		} else {
			doOperation = setFeedbackMessage(doOperation, nmCommandBean, false, "单个物料不允许进行发布,只有成品和半成品才可以发布!");
		}
		return doOperation;
	}

	private FormResult startDAWF(NmCommandBean nmCommandBean, FormResult doOperation, WTPart part,
								 WorkFlowBase workFlowBase) throws PIException, WTException {
		// 校验子件状态
		String message = checkSonState(part, true);
		// 检查属性完整性
		message = message + checkObjectIBAvalue(part);
		System.out.println("message====" + message);
		if (message.length() == 0) {
			// 不在其它流程中,然后启动流程
			if (!isRunningWorkflow(part, true)) {
				String workFlowName = "GenericPartWF";
				if (workFlowBase.startWorkFlow(part, workFlowName)) {
					doOperation = setFeedbackMessage(doOperation, nmCommandBean, true, "启动设计部件归档流程成功!");

				} else {
					if (workFlowBase.isRelatedRunningProcess(part))
						doOperation = setFeedbackMessage(doOperation, nmCommandBean, false,
								"当前部件存在于其它流程中,无法启动设计部件归档流程!");
					else
						doOperation = setFeedbackMessage(doOperation, nmCommandBean, false, "启动设计部件归档流程失败!");
				}
			} else {
				doOperation = setFeedbackMessage(doOperation, nmCommandBean, false, "当前部件存在于其它流程中,无法启动设计部件归档流程!");
			}
		} else {
			doOperation = setFeedbackMessage(doOperation, nmCommandBean, false, message);
		}
		return doOperation;
	}

	public static String getIBAvalue(Persistable p, String key) throws PIException {
		Object object = PIAttributeHelper.service.getValue(p, key);
		System.out.println("object====" + object);
		String comment = "";
		if (object == null) {
			return comment;
		}
		if (object instanceof String) {
			String changeComment = (String) PIAttributeHelper.service.getValue(p, key);
			comment = changeComment;
		}
		if (object instanceof Object[]) {
			Object[] objArr = (Object[]) object;
			for (int i = 0; i < objArr.length; i++) {
				comment = comment + objArr[i].toString() + "  ";
			}
		}
		if (object instanceof Boolean) {
			comment = object.toString();
		} else {
			comment = object.toString();
		}
		System.out.println("commnet =========" + comment);
		return comment;
	}

	// 检查成品和半成品的属性是否为空，图纸的属性：所属产品类别，所属项目是否为空
	/*
	 * 1、 半成品：所属产品类别、是否工艺虚拟件、环保属性 2、 成品：内部型号、销售型号、环保属性、重量、重量单位 3、
	 * 原材料库中的电子料、结构件、光学件、包材件、公共类、辅料：环保属性 4、
	 * 原材料库中的电子料、结构件、光学件、包材件的说明方文档---图纸：所属产品类别、所属项目 5、 软件部件：软件版本
	 */

	public static String checkObjectIBAvalue(WTPart part) throws WTException {
		StringBuilder message = new StringBuilder();

		try {
			if (part.getNumber().startsWith("A")) {

				String hbsx = (String) getIBAvalue(part, "hbsx");// 环保属性
				String nbxh = (String) getIBAvalue(part, "nbxh");// 内部型号
				String xsxh = (String) getIBAvalue(part, "nbxh");// 销售型号
				String zldw = (String) getIBAvalue(part, "zldw");// 重量单位
				String zl = (String) getIBAvalue(part, "zl");// 重量
				if (hbsx.length() == 0 || hbsx == null) {
					message.append("属性：'环保属性',为空");
				}
				if (nbxh.length() == 0 || nbxh == null) {
					message.append("属性：'内部型号',为空");
				}
				if (xsxh.length() == 0 || xsxh == null) {
					message.append("属性：'销售型号',为空");
				}
				if (!part.getContainerName().startsWith("SL-舞台灯产品库")) {
					if (zldw.length() == 0 || zldw == null) {
						message.append("属性：'重量单位',为空");
					}
					if (zl.length() == 0 || zl == null) {
						message.append("属性：'重量',为空");
					}
				}

			}
			if (part.getNumber().startsWith("B")) {
				String sscpx = getIBAvalue(part, "sscpx");// 所属产品类别
				String sfxnj = getIBAvalue(part, "sfxnj");// 是否为工艺虚拟件
				String hbsx = getIBAvalue(part, "hbsx");// 环保属性
				if (hbsx.length() == 0 || hbsx == null) {
					message.append("属性：'环保属性',为空");
				}
				if (sfxnj.length() == 0 || sfxnj == null) {
					message.append("属性：'是否为工艺虚拟件',为空");
				}
				if (sscpx.length() == 0 || sscpx == null) {
					message.append("属性：'所属产品类别',为空");
				}
			}
			if (part.getNumber().startsWith("E") || part.getNumber().startsWith("C") || part.getNumber().startsWith("P")
					|| part.getNumber().startsWith("T") || part.getNumber().startsWith("D")
					|| part.getNumber().startsWith("S")) {
				String hbsx = getIBAvalue(part, "hbsx");// 环保属性
				if (hbsx.length() == 0 || hbsx == null) {
					message.append("属性：'环保属性',为空");
				}
			}
			if (part.getNumber().startsWith("X")) {
				String rjbb = getIBAvalue(part, "rjbb");// 软件版本
				if (rjbb.length() == 0 || rjbb == null) {
					message.append("属性：'软件版本',为空");
				}
			}
			// 检查说明文档的IBA属性
			if (part.getNumber().startsWith("E") || part.getNumber().startsWith("C") || part.getNumber().startsWith("P")
					|| part.getNumber().startsWith("D")) {
				QueryResult desdocquery = getAssociatedDescribeDocuments(part);

				while (desdocquery.hasMoreElements()) {
					WTDocument document = (WTDocument) desdocquery.nextElement();
					if (document.getState().toString().equalsIgnoreCase("INWORK")
							|| document.getState().toString().equalsIgnoreCase("REWORK")) {

						TypeIdentifier ti = TypeIdentifierUtility.getTypeIdentifier(document);
						String type = ti.getTypename();
						if (type.contains("drawingdoc")) {// 图纸
							String sscpx = getIBAvalue(document, "sscpx");// 所属产品类别
							String ssxm = getIBAvalue(document, "ssxm");// 所属项目
							if (sscpx.length() == 0 || sscpx == null) {
								message.append("相关对象的文档（" + document.getNumber() + "）属性”所属产品类别“为空值；");
							}
							if (ssxm.length() == 0 || ssxm == null) {
								message.append("属性“所属项目”为空值，请填写后提交；");
								message.append("\n");
							}
						}
					}
				}

			}

		} catch (PIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return message.toString();
	}

	/*
	 * 通过部件得到所有的描述文档
	 */

	public static QueryResult getAssociatedDescribeDocuments(WTPart wtpart) throws WTException {
		WTArrayList wtarraylist = new WTArrayList();
		wtarraylist.add(wtpart);
		WTKeyedMap wtkeyedmap = PartDocHelper.service.getAssociatedDescribeDocuments(wtarraylist);
		WTCollection wtcollection = (WTCollection) wtkeyedmap.get(wtpart);
		return getDocs(wtcollection);
	}

	private static QueryResult getDocs(WTCollection wtcollection) {
		QueryResult queryresult = new QueryResult();
		try {
			if (wtcollection != null) {
				ObjectVector objectvector = new ObjectVector();
				DocumentVersion documentversion;
				for (Iterator iterator = wtcollection.persistableIterator(); iterator.hasNext(); objectvector
						.addElement(documentversion))
					documentversion = (DocumentVersion) iterator.next();

				queryresult.appendObjectVector(objectvector);
			}
		} catch (WTException wtexception) {
			wtexception.printStackTrace();
		}
		return queryresult;
	}

	/**
	 * 判断部件是不是最新版本
	 *
	 * @param oldObj
	 *            当前对象
	 * @return boolean
	 */
	private boolean isLatestObject(WTObject oldObj) {
		boolean isLstest = true; // 是最新
		RevisionControlled iterated = null;
		if (oldObj instanceof RevisionControlled) {
			iterated = (RevisionControlled) oldObj;
			if (!iterated.isLatestIteration()) {
				isLstest = false;
			}
		}
		return isLstest;
	}

	/**
	 * 判断是否修改者
	 *
	 * @param pbo
	 *            pbo
	 * @return boolean
	 * @throws WTException
	 *             异常
	 */
	private boolean isEqualCreator(WTPart pbo) throws WTException {
		boolean isequalCreator = false;
		String principalName = SessionHelper.getPrincipal().getName();// 当前用户
		String thePartprincipalName = pbo.getModifier().getPrincipal().getName();// 部件修改者
		if (principalName.equals(thePartprincipalName)) {
			isequalCreator = true;
		}
		return isequalCreator;
	}

	/**
	 * 判断pbo是否在其他流程中运行，归档流程需过滤eca流程
	 *
	 * @author HYJ&NJH
	 * @param pbo
	 * @param isArchive
	 *            是否是归档
	 * @return
	 * @throws WTException
	 */
	private static boolean isRunningWorkflow(WTPart pbo, Boolean isArchive) throws WTException {
		boolean processok = false;
		NmOid nmOid = NmOid.newNmOid(PersistenceHelper.getObjectIdentifier(pbo));
		QueryResult qr = NmWorkflowHelper.service.getRoutingHistoryData(nmOid);

		String version = pbo.getVersionInfo().getIdentifier().getValue() + "."
				+ pbo.getIterationInfo().getIdentifier().getValue();// 物料版本
		System.out.println("number=" + pbo.getNumber() + "  version=" + version);
		// 获取所有开启的流程模板名称
		Set<String> set = new HashSet<>();
		while (qr.hasMoreElements()) {
			WfProcess process = (WfProcess) qr.nextElement();

			System.out.println("process===" + process.getName());

			String templateName = process.getTemplate().getName();
			if (process.getState()
					.equals(WfState.OPEN_RUNNING) /*
			 * && templateName.contains(
			 * "GenericPartWF")
			 */ ) {
				set.add(templateName);
			}
		}
		LOGGER.debug("当前对象：" + pbo.getName() + "开启的流程为：" + set);
		// 存在开启的流程
		if (set.size() > 0) {
			// 需过滤eca
			if (isArchive) {
				// 移除对应流程名称,PS:GenericECAWF
				set.remove("GenericECNWF");
				set.remove("GenericECAWF");
				if (set.size() > 0) {
					processok = true;
				}
			} else {
				processok = true;
			}
		}
		return processok;
	}

	public static void getBomallchildpart(WTPart part, ArrayList<WTPart> childlist) throws Exception {
		String state = "";
		// System.out.println("part number===="+part.getNumber());
		QueryResult qr = new QueryResult();
		qr = WTPartHelper.service.getUsesWTPartMasters(part);
		// System.out.println("get uses wtpart master size==="+qr.size());
		while (qr.hasMoreElements()) {
			WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
			WTPartMaster master = link.getUses();
			WTPart sunpart = (WTPart) PartUtil.getLastestWTPartByNumber(master.getNumber());
			// System.out.println("sun part numer=="+sunpart.getNumber());
			childlist.add(sunpart);
			getBomallchildpart(sunpart, childlist);
		}
	}

	/**
	 * 检查子类状态
	 *
	 * @author HYJ&NJH
	 * @param part
	 * @param isArchive
	 *            是否归档流程
	 * @return
	 * @throws WTException
	 */
	private String checkSonState(WTPart part, Boolean isArchive) throws WTException {
		// boolean stateIsOK = true;
		String message = "";
		WTUser previous = (WTUser) SessionHelper.manager.getPrincipal();
		// 当前用户设置为管理员，用于忽略权限
		try {
			WTPrincipal wtadministrator = SessionHelper.manager.getAdministrator();
			// 取得当前用户
			SessionContext.setEffectivePrincipal(wtadministrator);
			WTPrincipalReference wtprincipalreference = (WTPrincipalReference) (new ReferenceFactory())
					.getReference(previous);
			AccessControlHelper.manager.addPermission((AdHocControlled) part, wtprincipalreference,
					AccessPermission.MODIFY_IDENTITY, AdHocAccessKey.WNC_ACCESS_CONTROL);
		} catch (WTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// 获取单层子件
		// WTCollection childrens = PIPartHelper.service.findChildren(part);
		// 获取所有子件
		ArrayList<WTPart> childpartList = new ArrayList<WTPart>();
		String parentview = part.getViewName();
		try {
			getBomallchildpart(part, childpartList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SessionContext.setEffectivePrincipal(previous);
		// System.out.println("childpartList.size()===="+childpartList.size());
		for (int i = 0; i < childpartList.size(); i++) {
			WTPart son = (WTPart) childpartList.get(i);
			// 检查D视图下，是否有M视图的部件
			String viewname = son.getViewName();
			if (parentview.equalsIgnoreCase("Design") && viewname.equalsIgnoreCase("Manufacturing")) {
				// return "子件:" + son.getNumber() +
				// ",为制造视图(Manufacturing)，不能加到设计视图的bom中,无法启动设计部件归档流程!";
			}
			String stateVal = son.getLifeCycleState().toString();
			if (son.getNumber().startsWith("X") && !isArchive) {
				System.out.println("isArchive==" + isArchive);
				if (!stateVal.equals("RELEASED")) {
					// stateIsOK = false;
					return "软件部件:" + son.getNumber() + "未发布,无法启动设计部件发布流程!";
				}
			}
			if (stateVal.equals("ARCHIVED") || stateVal.equals("RELEASED")) {
			} else {
				// stateIsOK = false;
				if (isArchive) {
					message = message + "子件:" + son.getNumber() + "未归档或未发布,无法启动设计部件归档流程!" + "\n";
				} else {
					message = message + "子件:" + son.getNumber() + "未归档或未发布,无法启动设计部件发布流程!" + "\n";
				}
			}
		}
		// Iterator iterator = childrens.iterator();
		// while (iterator.hasNext()) {
		// ObjectReference or = (ObjectReference) iterator.next();
		// WTPart son = (WTPart) or.getObject();
		// String stateVal = son.getLifeCycleState().toString();
		// if (isArchive) {
		// if (stateVal.equals("ARCHIVED") || stateVal.equals("RELEASED")) {
		// } else {
		// stateIsOK = false;
		// }
		// } else {
		// if (stateVal.equals("RELEASED")) {
		// } else {
		// stateIsOK = false;
		// }
		// }
		// }
		return message;
	}

	/**
	 * 检查子类状态
	 *
	 * @author HYJ&NJH
	 * @param part
	 * @param isArchive
	 *            是否归档流程
	 * @return
	 * @throws WTException
	 */
	private String checkSonHistoryversion(WTPart part) throws WTException {
		// boolean stateIsOK = true;
		String message = "";
		List partlist = new ArrayList<String>();
		WTUser previous = (WTUser) SessionHelper.manager.getPrincipal();

		// 当前用户设置为管理员，用于忽略权限
		try {
			WTPrincipal wtadministrator = SessionHelper.manager.getAdministrator();
			// 取得当前用户
			SessionContext.setEffectivePrincipal(wtadministrator);
			WTPrincipalReference wtprincipalreference = (WTPrincipalReference) (new ReferenceFactory())
					.getReference(previous);
			AccessControlHelper.manager.addPermission((AdHocControlled) part, wtprincipalreference,
					AccessPermission.MODIFY_IDENTITY, AdHocAccessKey.WNC_ACCESS_CONTROL);
		} catch (WTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// 获取单层子件
		// WTCollection childrens = PIPartHelper.service.findChildren(part);
		// 获取所有子件
		ArrayList<WTPart> childpartList = new ArrayList<WTPart>();
		try {
			getBomallchildpart(part, childpartList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("childpartList.size()====" + childpartList.size());
		for (int i = 0; i < childpartList.size(); i++) {
			WTPart son = (WTPart) childpartList.get(i);
			String stateVal = son.getLifeCycleState().toString();

			if (stateVal.equals("ARCHIVED") || stateVal.equals("RELEASED")) {
			} else {
				QueryResult partqr = getParts(son.getNumber());
				while (partqr.hasMoreElements()) {
					WTPart oldpart = (WTPart) partqr.nextElement();
					String state = oldpart.getState().toString();
					System.out.println("version=====" + state);
					if (!partlist.contains(state)) {
						partlist.add(state);
					}

				} // 存在有已归档或已发布的版本，
				if (partlist.contains("ARCHIVED") || partlist.contains("RELEASED")) {
				} else {
					message = message + "子件:" + son.getNumber() + "未存在已归档或已发布的版本,无法启动设计部件归档流程!" + "\n";
				}
			}
		}
		SessionContext.setEffectivePrincipal(previous);
		return message;
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

	/**
	 * 检查部件可以启动什么流程
	 * <p>
	 * 当对象部件是正在工作状态、D视图时，点击按钮后触发设计部件归档流程启动条件校验，符合条件后启动设计部件归档流程。
	 * </p>
	 * <p>
	 * 当对象部件是已归档状态、D视图时，点击按钮后触发设计部件发布流程启动条件校验，符合条件后启动设计部件发布流程。
	 * </p>
	 * <p>
	 * 当对象部件是正在工作状态、M视图时，点击按钮后触发工程部件归档流程启动条件校验，符合条件后启动工程部件归档流程。
	 * </p>
	 * <p>
	 * 当对象部件是已归档状态、M视图时，点击按钮后触发工程部件发布流程启动条件校验，符合条件后启动工程部件发布流程。
	 * </p>
	 *
	 * @author HYJ&NJH
	 * @param viewName
	 * @param stateVal
	 *            状态
	 * @return D&M:视图;A:归档;R:发布;
	 */
	private String getWFShortName(String viewName, String stateVal) {
		String wfShortName = "";

		if (viewName.equals("Design")) {
			if (stateVal.equals("INWORK")) {
				wfShortName = DA;
			} else if (stateVal.equals("ARCHIVED")) {
				wfShortName = DR;
			}
		} else if (viewName.equals("Manufacturing")) {
			if (stateVal.equals("INWORK")) {
				wfShortName = MA;
			} else if (stateVal.equals("ARCHIVED")) {
				wfShortName = MR;
			}
		}

		return wfShortName;
	}

	/**
	 * 设置回显信息
	 *
	 * @author HYJ&NJH
	 * @param doOperation
	 * @param nmCommandBean
	 * @param flag
	 *            是否成功
	 * @param message
	 *            信息内容
	 * @return
	 * @throws WTException
	 */
	public FormResult setFeedbackMessage(FormResult doOperation, NmCommandBean nmCommandBean, boolean flag,
										 String message) throws WTException {

		FeedbackType type = FeedbackType.FAILURE;
		if (flag) {
			type = FeedbackType.SUCCESS;
		}

		FeedbackMessage feedbackMessage = new FeedbackMessage(type, nmCommandBean.getLocale(), message, null,
				new String[] {});
		doOperation.addFeedbackMessage(feedbackMessage);

		return doOperation;

	}

	/**
	 * 通过名称查询工作流模板的最新版本
	 *
	 * @param name
	 * @return
	 * @throws WTException
	 */
	public WfProcessTemplate findWfProcessTemplate(String name) throws WTException {
		try {
			// 搜索流程模板
			QuerySpec querysearch = new QuerySpec(WfProcessTemplate.class);
			querysearch.appendWhere(new SearchCondition(WfProcessTemplate.class, "name", "=", name), new int[] { 0 });
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
			}
			return wfprocesstemplate;
		} catch (WTException e) {
			e.printStackTrace();
			throw e;
		}
	}

	/***
	 * 启动ERP发布流程
	 *
	 * @param processTemplate
	 *            ERP发布流程模板
	 * @param primaryBusinessObject
	 *            流程主对象
	 * @param releaseArray
	 *            发布ERP数据集合
	 * @return
	 */
	@SuppressWarnings({ "deprecation" })
	public WfProcess startWfProcess(WfProcessTemplate processTemplate, WTObject primaryBusinessObject,
									WTArrayList releaseArray) {
		if (primaryBusinessObject == null) {
			return null;
		}

		// 忽略权限
		boolean access = SessionServerHelper.manager.setAccessEnforced(false);

		Transaction tx = null;
		boolean canCommit = false;

		try {
			// 没有可用的活动的事务，则启动新事务
			if (!PersistentObjectManager.getPom().isTransactionActive()) {
				tx = new Transaction();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(">>> startWfProcess, start new Transaction for start a WfProcess ...");
				}
				tx.start();
				canCommit = true;
			}

			LifeCycleTemplateReference lifeCycleTemplateRef = getLifeCycleTemplateReference(primaryBusinessObject);
			WTContainerRef containerRef = getWTContainerRef(primaryBusinessObject);

			WfProcess newProcess = WfEngineHelper.service.createProcess(processTemplate, lifeCycleTemplateRef,
					containerRef);
			if (newProcess == null) {
				throw new WTException("create a new process but return a null object.");
			}
			// 设置进程主对象
			newProcess = WfEngineServerHelper.service.setPrimaryBusinessObject(newProcess, primaryBusinessObject);
			// 设置流程实例的名称
			newProcess.setName(processTemplate.getName() + "_" + primaryBusinessObject.getIdentity());
			// 设置流程实例的创建者为管理员
			WTPrincipal adminPrincipal = SessionHelper.manager.getAdministrator();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(">>> startWfProcess, get Administrator is "
						+ (adminPrincipal == null ? "null" : adminPrincipal.getName()));
			}
			WTPrincipalReference principalRef = WTPrincipalReference.newWTPrincipalReference(adminPrincipal);
			newProcess.setCreator(principalRef);
			// 设置工作流进程的描述
			newProcess.setDescription("手工启动ERP发布流程!");
			// 设置流程团队
			TeamReference team = getTeamReference(primaryBusinessObject);
			if (team != null) {
				newProcess.setTeamId(team);
			}
			// 设置流程变量
			newProcess = setProcessData(primaryBusinessObject, newProcess, releaseArray);

			newProcess = (WfProcess) PersistenceHelper.manager.save(newProcess);
			// 启动该工作流实例
			newProcess.start(newProcess.getContext(), true, containerRef);

			if (canCommit) {
				tx.commit();
				tx = null;
			}
			return newProcess;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (canCommit) {
				if (tx != null) {
					tx.rollback();
					tx = null;
				}
			}
			SessionServerHelper.manager.setAccessEnforced(access);
		}
		return null;
	}

	/***
	 * 获取工作流进程相关的额生命周期模板
	 *
	 * @param primaryBusinessObject
	 * @return
	 */
	public LifeCycleTemplateReference getLifeCycleTemplateReference(WTObject primaryBusinessObject) {
		LifeCycleTemplateReference lifeCycleTemplateRef = null;
		if (primaryBusinessObject != null && primaryBusinessObject instanceof LifeCycleManaged) {
			LifeCycleManaged lc = (LifeCycleManaged) primaryBusinessObject;
			lifeCycleTemplateRef = lc.getLifeCycleTemplate();
		}
		return lifeCycleTemplateRef;
	}

	/***
	 * 获取工作流进程所属的上下文容器
	 *
	 * @param primaryBusinessObject
	 * @return
	 * @throws WTException
	 */
	protected WTContainerRef getWTContainerRef(WTObject primaryBusinessObject) throws WTException {
		WTContainerRef containerRef = null;
		try {
			if (primaryBusinessObject != null && primaryBusinessObject instanceof WTContained) {
				WTContained contained = (WTContained) primaryBusinessObject;
				containerRef = contained.getContainerReference();
			} else {
				containerRef = WTContainerHelper.service.getExchangeRef();
			}
			return containerRef;
		} catch (WTException e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * 获取工作流进程的团队
	 *
	 * @param primaryBusinessObject
	 * @param processConfig
	 * @return
	 * @throws PIException
	 */
	protected TeamReference getTeamReference(WTObject primaryBusinessObject) {
		TeamReference teamRef = null;
		if (primaryBusinessObject != null && primaryBusinessObject instanceof TeamManaged) {
			TeamManaged teamManaged = (TeamManaged) primaryBusinessObject;
			teamRef = teamManaged.getTeamId();
		}
		return teamRef;
	}

	/**
	 * 设置流程变量
	 *
	 * @param newProcess
	 * @param releaseArray
	 * @return
	 * @throws WTException
	 */
	public WfProcess setProcessData(WTObject primaryBusinessObject, WfProcess newProcess, WTArrayList releaseArray)
			throws WTException {
		if (primaryBusinessObject instanceof WTPart) {
			ProcessData newProcessdata = newProcess.getContext();
			if (newProcessdata != null) {
				newProcessdata.setValue("erpObjs", releaseArray);
			}
		} else {
			throw new WTException("类型错误无法启动流程!");
		}
		return newProcess;
	}

	/***
	 * 检查用户在指定库中对某种类型的某种状态是否具有相应权限
	 *
	 * @param principal
	 *            用户
	 * @param type
	 *            类型 (列如:WCTYPE|wt.change2.WTChangeOrder2)
	 * @param adminDomainRef
	 *            静态权限域
	 * @param state
	 *            指定状态
	 * @param accessPermission
	 *            相应权限
	 * @return
	 * @throws WTException
	 */
	public Boolean isOperability(WTPrincipal principal, String type, AdminDomainRef adminDomainRef, State state,
								 AccessPermission accessPermission) throws WTException {
		if (PIStringUtils.isNull(type) || adminDomainRef == null || accessPermission == null) {
			return false;
		}

		String displayTypeIdentifier = displayTypeIdentifier(type);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("adminDomainRef : " + IdentityFactory.getDisplayIdentity(adminDomainRef.getObject()));
			LOGGER.debug("displayTypeIdentifier : " + displayTypeIdentifier);
		}
		if (PIStringUtils.isNotNull(displayTypeIdentifier)) {
			return AccessControlHelper.manager.hasAccess(principal, displayTypeIdentifier, adminDomainRef, state,
					AccessPermission.CREATE);
		}

		return false;
	}

	/***
	 * 获取类型定义
	 *
	 * @param paramString
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings("deprecation")
	public String displayTypeIdentifier(String paramString) throws WTException {
		if (paramString == null) {
			return null;
		}

		String str1 = TypedUtility.getExternalTypeIdentifier(paramString);
		if (str1 == null) {
			String str2 = TypedUtility.getPersistedType(paramString);
			if (str2 != null) {
				str1 = TypedUtility.getExternalTypeIdentifier(str2);
			}
		}
		return str1 == null ? paramString : str1;
	}

	public static String getObjectType(Object object) throws WTException {
		String type = "";
		boolean flag = true;
		try {
			flag = SessionServerHelper.manager.isAccessEnforced();
			SessionServerHelper.manager.setAccessEnforced(false);

			if (object != null) {
				TypeIdentifier ti = TypeIdentifierUtility.getTypeIdentifier(object);
				type = ti.getTypename();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
		return type;
	}


	/**
	 * add by lzy
	 * 判断pbo所有大版本的最新小版本是否在新ECN流程APPO_ECNWF中运行
	 * @param pbo
	 * @return
	 * @throws WTException
	 */
	private static boolean isRunningNewEcnWorkflow(WTPart pbo) throws WTException {
		boolean flag = false;
		QueryResult queryResult = VersionControlHelper.service.allVersionsOf(pbo.getMaster());//获取所有大版本的最新小版本
		all:while (queryResult.hasMoreElements()) {
			WTPart oldPart = (WTPart) queryResult.nextElement();
			//获取对象所有关联的ECA对象
			QueryResult result = ChangeHelper2.service.getAffectingChangeActivities(oldPart);
			while (result.hasMoreElements()) {
				WTChangeActivity2 changeActivity2 = (WTChangeActivity2) result.nextElement();
				WTChangeOrder2 changeOrder2 = ChangeUtils.getEcnByEca(changeActivity2);
				//ECN进程
				QueryResult qr = WfEngineHelper.service.getAssociatedProcesses(changeOrder2, null, null);
				while (qr.hasMoreElements()) {
					WfProcess process = (WfProcess) qr.nextElement();
					String templateName = process.getTemplate().getName();
					String state = String.valueOf(changeOrder2.getLifeCycleState());
					//不是已取消、已解决的新ECN流程APPO_ECNWF
					if (templateName.equals("APPO_ECNWF")) {
						if (!state.equals("CANCELLED") && !state.equals("RESOLVED")) {
							flag = true;
							break all;
						}
					}
				}
			}
		}

		return flag;
	}
}
