package ext.appo.part.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.util.FeedbackMessage;
import com.ptc.core.ui.resources.FeedbackType;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.workflow.NmWorkflowHelper;

import ext.appo.part.filter.StandardPartsRevise;
import ext.appo.part.filter.StandardPartsReviseFilter;
import ext.com.workflow.WorkflowUtil;
import ext.generic.workflow.WorkFlowBase;
import ext.pi.PIException;
import ext.pi.core.PIAttributeHelper;
import wt.doc.DocumentVersion;
import wt.enterprise.RevisionControlled;
import wt.fc.ObjectVector;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTKeyedMap;
import wt.inf.container.WTContained;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerRef;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.LifeCycleTemplateReference;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.part.PartDocHelper;
import wt.part.WTPart;
import wt.pom.PersistentObjectManager;
import wt.pom.Transaction;
import wt.query.ClassAttribute;
import wt.query.OrderBy;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.team.TeamManaged;
import wt.team.TeamReference;
import wt.util.WTException;
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
public class editStandardpartAtr extends DefaultObjectFormProcessor {

	private static String CLASSNAME = editStandardpartAtr.class.getName();
	private static final Logger LOGGER = LogR.getLogger(CLASSNAME);

	@Override
	public FormResult doOperation(NmCommandBean nmCommandBean, List<ObjectBean> objectBeans) throws WTException {
		// 获取对象
		FormResult doOperation = super.doOperation(nmCommandBean, objectBeans);
		Object refObject = nmCommandBean.getActionOid().getRefObject();
		// 判断对象类型
		if (refObject != null && refObject instanceof WTPart) {
			WTPart part = (WTPart) refObject;

			String stateVal = part.getLifeCycleState().toString();
			// 必须是已发布或已归档才可以启动流程
			if (stateVal.equals("RELEASED") || stateVal.equals("ARCHIVED")) {
				// 首先部件不能被检出
				if (!WorkflowUtil.isObjectCheckedOut(part)) {
					// 部件必须是最新的
					if (isLatestObject(part)) {
						// 是修改者
						if (isEqualCreator(part)) {
							StandardPartsRevise re = new StandardPartsRevise();
							List<Map> list = new ArrayList<Map>();
							try {
								list = re.getExcelData();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							Boolean isStandartpart = StandardPartsReviseFilter.isStandartpart(list, part);
							if (isStandartpart) {
								WorkFlowBase workFlowBase = new WorkFlowBase();

								doOperation = starteditStandardpartAtrWF(nmCommandBean, doOperation, part,
										workFlowBase);

							} else {
								doOperation = setFeedbackMessage(doOperation, nmCommandBean, false, "只有标准件才能启动该流程!");

							}

						} else {
							doOperation = setFeedbackMessage(doOperation, nmCommandBean, false, "您不是该当前部件的修改者，无法启动流程!");
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

	private FormResult starteditStandardpartAtrWF(NmCommandBean nmCommandBean, FormResult doOperation, WTPart part,
			WorkFlowBase workFlowBase) throws PIException, WTException {

		// 不在其它流程中,然后启动流程
		if (!isRunningWorkflow(part, true)) {
			String workFlowName = "APPO_EditStandardpartAtrWF";
			if (workFlowBase.startWorkFlow(part, workFlowName)) {
				doOperation = setFeedbackMessage(doOperation, nmCommandBean, true, "启动设计部件归档流程成功!");

			} else {
				doOperation = setFeedbackMessage(doOperation, nmCommandBean, false, "启动设计部件归档流程失败!");
			}
		} else {
			doOperation = setFeedbackMessage(doOperation, nmCommandBean, false, "当前部件存在于其它流程中,无法启动设计部件归档流程!");
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
		// 获取所有开启的流程模板名称
		Set<String> set = new HashSet<>();
		while (qr.hasMoreElements()) {
			WfProcess process = (WfProcess) qr.nextElement();
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
}
