package ext.appo.part.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ext.appo.doc.uploadDoc.UploadAttachmentUtil;
import ext.appo.util.excel.AppoExcelUtil;
import ext.com.core.CoreUtil;
import ext.com.iba.IBAUtil;
import ext.com.workflow.WorkflowUtil;
import ext.customer.common.MBAUtil;
import ext.generic.changephase.bean.ChangePhaseBean;
import ext.generic.changephase.util.ChangePhaseXMLConfigUtil;
import ext.generic.generatenumber.bean.SetTargetStateBean;
import ext.generic.generatenumber.constant.GenerateNumberConstant;
import ext.generic.generatenumber.util.GenerateNumberUtil;
import ext.generic.partpromotion.util.PartPromotionXMLConfigUtil;
import ext.generic.reviewObject.model.ProcessReviewObjectLink;
import ext.generic.reviewObject.model.ProcessReviewObjectLinkHelper;
import ext.generic.reviewObject.util.ReviewObjectAndWorkFlowLink;
import ext.generic.workflow.WorkFlowBase;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIClassificationHelper;
import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMDocument;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.iba.value.IBAHolder;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartUsageLink;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTMessage;
import wt.util.WTPropertyVetoException;
import wt.vc.VersionControlHelper;
import wt.workflow.engine.WfProcess;

/**
 * 部件发布流程类
 */
public class PartReleasedWorkFlow extends WorkFlowBase {

	private static final String RESOURCE = "ext.generic.reviewObject.resource.ReviewObjectResourceRB";

	private static final String CLASSNAME = PartReleasedWorkFlow.class.getName();
	private static final Logger LOGGER = LogR.getLogger(CLASSNAME);

	public PartReleasedWorkFlow() {
	}

	public PartReleasedWorkFlow(WTObject pbo, ObjectReference self) {
		if (pbo != null && self != null) {
			this.pbo = pbo;
			this.self = self;
		}
	}

	/**
	 * 工程发布流程检查随签对象
	 * 
	 * @param list
	 * @throws WTException
	 */
	public void check() throws WTException {
		if (this.self != null && this.pbo != null) {
			WTArrayList list = collect();

			// 检查视图是否为空
			isObjsViewEmpty(list);

			// 检查对象是否检出
			isObjsCheckedOut(list);

			// 检查是为最新版本
			checkISlatestVersion(list);

			checkReviewObjectStates(list);

			checkIsAssociatedRunningProcesses(list);

			getErrorMessge();
			if (!checkPboIsDelect(list)) {
				// errorMessage.append("\n 随签列表中没有主对象，请点击重新加载随签对象按钮");
				Locale locale = SessionHelper.manager.getLocale();
				errorMessage.append(WTMessage.getLocalizedMessage(RESOURCE, "WF_PBO_NULL", null, locale));
				getErrorMessge();
			}
			WTArrayList oldReviewObjs = getReivewObjects();
			compareStructure(oldReviewObjs, list);
		}

	}

	/**
	 * 设计发布流程编制节点校验
	 * 
	 * @throws WTException
	 */
	public void checkDesign() throws WTException {
		if (this.self != null && this.pbo != null) {
			WTArrayList list = collect();

			// 检查视图是否为空
			isObjsViewEmpty(list);

			// 检查对象是否检出
			isObjsCheckedOut(list);

			// 检查是为最新版本
			checkISlatestVersion(list);

			checkReviewObjectStates(list);

			checkIsAssociatedRunningProcesses(list);

			getErrorMessge();
			if (!checkPboIsDelect(list)) {
				// errorMessage.append("\n 随签列表中没有主对象，请点击重新加载随签对象按钮");
				Locale locale = SessionHelper.manager.getLocale();
				errorMessage.append(WTMessage.getLocalizedMessage(RESOURCE, "WF_PBO_NULL", null, locale));
				getErrorMessge();
			}
			WTArrayList oldReviewObjs = getReivewObjects();
			compareStructure(oldReviewObjs, list);
		}

	}

	/**
	 * 工程归档编制节点校验
	 * 
	 * @throws WTException
	 */
	public void checkEnginer() throws WTException {
		if (this.self != null && this.pbo != null) {
			WTArrayList list = collect();

			// 检查视图是否为空
			isObjsViewEmpty(list);

			// 检查对象是否检出
			isObjsCheckedOut(list);

			// 检查是为最新版本
			checkISlatestVersion(list);

			// checkReviewObjectStates(list);

			checkIsAssociatedRunningProcesses(list);

			getErrorMessge();
			if (!checkPboIsDelect(list)) {
				// errorMessage.append("\n 随签列表中没有主对象，请点击重新加载随签对象按钮");
				Locale locale = SessionHelper.manager.getLocale();
				errorMessage.append(WTMessage.getLocalizedMessage(RESOURCE, "WF_PBO_NULL", null, locale));
				getErrorMessge();
			}
			WTArrayList oldReviewObjs = getReivewObjects();
			compareStructure(oldReviewObjs, list);
		}

	}

	public void checkReviewObjectItem() throws WTException {
		if (this.self != null && this.pbo != null) {
			WTArrayList list = collect();

			// 检查视图是否为空
			isObjsViewEmpty(list);

			// 检查对象是否检出
			isObjsCheckedOut(list);

			// 检查是为最新版本
			checkISlatestVersion(list);

			// 成品状态控制
			checkReviewObjectStatesItem(list);

			checkIsAssociatedRunningProcesses(list);

			getErrorMessge();
			if (!checkPboIsDelect(list)) {
				// errorMessage.append("\n 随签列表中没有主对象，请点击重新加载随签对象按钮");
				Locale locale = SessionHelper.manager.getLocale();
				errorMessage.append(WTMessage.getLocalizedMessage(RESOURCE, "WF_PBO_NULL", null, locale));
				getErrorMessge();
			}
			WTArrayList oldReviewObjs = getReivewObjects();
			compareStructure(oldReviewObjs, list);
		}

	}

	/**
	 * 验证生命周期状态
	 * 
	 * @param list
	 * @return
	 * @throws WTException
	 */
	public void checkReviewObjectStatesItem(WTArrayList list) throws WTException {
		WTPart part = (WTPart) pbo;
		String state = part.getLifeCycleState().toString();
		List<WTObject> tempReviewObjsList = null;
		if (list != null && list.size() > 0) {
			tempReviewObjsList = new ArrayList<WTObject>(list.size());
			for (int i = 0; i < list.size(); i++) {
				Persistable persistable = list.getPersistable(i);
				if (persistable instanceof WTPart) {
					WTPart reviewObj = (WTPart) persistable;
					String reviewObjectState = reviewObj.getLifeCycleState().toString();
					if (!state.equals(reviewObjectState)) {
						tempReviewObjsList.add(reviewObj);
					}
				}
			}
		}
		if (tempReviewObjsList.size() > 0) {
			errorMessage.append("\n PI-260002: 随迁对象的的状态和流程主对象状态不一致！");
			errorMessage.append(getCollectionDisplayInfo(tempReviewObjsList));
		}
	}

	/**
	 * 结构比较
	 * 
	 * @param oldReviewObjs
	 * @param list
	 * @throws WTException
	 */
	protected void compareStructure(WTArrayList oldReviewObjs, WTArrayList list) throws WTException {
		Collection<WTObject> checkList = new ArrayList<WTObject>();

		LOGGER.debug("checkReviewObject oldReviewObjs list : = " + list + "=======" + oldReviewObjs);
		for (int i = 0; i < oldReviewObjs.size(); i++) {
			Object obj = oldReviewObjs.get(i);
			if (!list.contains(obj)) {
				WTObject reviewObj = (WTObject) oldReviewObjs.getPersistable(i);
				if (!(reviewObj instanceof WTDocument)) {
					if (reviewObj instanceof LifeCycleManaged) {
						String state = ((LifeCycleManaged) reviewObj).getLifeCycleState().toString();
						if (state.equals("ARCHIVED") || state.equals("UNDERREVIEW")) {
							checkList.add(reviewObj);
						}
					}
				}
			}
		}
		if (checkList.size() > 0) {
			// errorMessage.append("结构下如下对象没有发布，请检查，如果需要继续提交流程，请选择强制提交。");
			Locale locale = SessionHelper.manager.getLocale();
			errorMessage
					.append(WTMessage.getLocalizedMessage(RESOURCE, "WF_CHILDREN_STATE_NOT_RELEASED", null, locale));
			errorMessage.append(displayObjsWithoutLink(checkList));
		}
		getErrorMessge();
		wt.vc.views.ViewHelper.service.getAllViews();

	}

	/**
	 * 搜集随签列表中的对象
	 * 
	 * @param self
	 * @return
	 * @throws WTException
	 */
	public WTArrayList collect() throws WTException {
		WfProcess wfprocess = null;
		WTArrayList list = new WTArrayList();
		wfprocess = WorkflowUtil.getProcess(this.self);
		ProcessReviewObjectLink link = null;
		if (wfprocess != null) {
			QueryResult queryresult = ProcessReviewObjectLinkHelper.service
					.getProcessReviewObjectLinkByRoleA(wfprocess);
			while (queryresult != null && queryresult.hasMoreElements()) {
				link = (ProcessReviewObjectLink) queryresult.nextElement();
				WTObject obj = (WTObject) link.getRoleBObject();
				list.add(obj);
			}
		}
		return list;

	}

	/**
	 * 判断随迁对象是否成品
	 * 
	 * @author HYJ&NJH
	 * @throws WTException
	 */
	public void checkObjectIsEndProduct() throws WTException {
		WTArrayList collect = collect();
		boolean isEndProduct = true;
		for (int i = 0; i < collect.size(); i++) {
			Persistable persistable = collect.getPersistable(i);
			if (persistable instanceof WTPart) {
				WTPart part = (WTPart) persistable;
				String classification = (String) PIAttributeHelper.service.getValue(part, "Classification");
				String nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(classification);
				// 判断是否是成品
				if (!nodeHierarchy.contains("appo_cp")) {
					isEndProduct = false;
				}
			} else {
				isEndProduct = false;
			}
		}
		if (!isEndProduct) {
			errorMessage.append("\n 随迁表格存在非成品对象，请移除!\n");
		}
	}

	/**
	 * 检查pbo是否被删除
	 * 
	 * @param list
	 * @return
	 * @throws WTException
	 */
	public boolean checkPboIsDelect(WTArrayList list) throws WTException {
		boolean hasPbo = false;
		if (this.pbo != null) {
			for (int i = 0; i < list.size(); i++) {
				WTObject obj = (WTObject) list.getPersistable(i);
				if (PersistenceHelper.isEquivalent(this.pbo, obj)) {
					hasPbo = true;
					break;
				}
			}
		}
		return hasPbo;

	}

	/**
	 * 验证生命周期状态
	 * 
	 * @param list
	 * @return
	 * @throws WTException
	 */
	public void checkReviewObjectStates(WTArrayList list) throws WTException {

		List<WTObject> tempReviewObjsList = null;
		if (list != null && list.size() > 0) {
			tempReviewObjsList = new ArrayList<WTObject>(list.size());
			for (int i = 0; i < list.size(); i++) {
				WTObject reviewObj = (WTObject) list.getPersistable(i);
				if (reviewObj != null && (reviewObj instanceof LifeCycleManaged)) {
					LifeCycleManaged lcm = (LifeCycleManaged) reviewObj;
					String state = lcm.getLifeCycleState().toString();
					if (!state.equals("ARCHIVED")) {
						tempReviewObjsList.add(reviewObj);
					}
				}
				if (reviewObj instanceof WTPart && this.pbo instanceof WTPart) {
					WTPart part = (WTPart) reviewObj;
					WTPart partpbo = (WTPart) this.pbo;
					if (part.getNumber().startsWith("X") && !partpbo.getNumber().startsWith("X")) {
						errorMessage.append("物料:" + part.getNumber() + "是软件料号，不能添加到随签对象中，请移除！");
					}
				}
			}
			if (tempReviewObjsList.size() > 0) {
				errorMessage.append("\n PI-260002: 对象的生命周期状态只能为“已归档”");
				errorMessage.append(getCollectionDisplayInfo(tempReviewObjsList));
			}
		}

	}

	/**
	 * 获取最新版本
	 * 
	 * @param reviewObjsList
	 *            随签对象集合
	 * @throws WTException
	 *             异常
	 */
	public void checkISlatestVersion(WTArrayList reviewObjsList) throws WTException {
		List<WTObject> tempReviewObjsList = null;
		if (reviewObjsList != null && reviewObjsList.size() > 0) {
			tempReviewObjsList = new ArrayList<WTObject>(reviewObjsList.size());
			for (int i = 0; i < reviewObjsList.size(); i++) {

				WTObject reviewObj = (WTObject) reviewObjsList.getPersistable(i);

				if (reviewObj instanceof RevisionControlled) {
					RevisionControlled revisionControlled = (RevisionControlled) reviewObj;
					if (!VersionControlHelper.isLatestIteration((revisionControlled))) {
						tempReviewObjsList.add(revisionControlled);
					}
				}
			}

			if (tempReviewObjsList.size() > 0) {
				errorMessage.append(WTMessage.getLocalizedMessage(RESOURCE, "WF_TASK_REVIEWOBJECT_IS_LASTESD_VERSION",
						null, Locale.CHINA));
				errorMessage.append(getCollectionDisplayInfo(tempReviewObjsList));
			}
		}
	}

	/**
	 * 检查随签对象
	 * 
	 * @param list
	 * @throws WTException
	 */
	public void checkForce() throws WTException {
		if (this.pbo != null && this.self != null) {
			WTArrayList list = collect();

			isObjsCheckedOut(list);

			checkISlatestVersion(list);

			checkReviewObjectStates(list);

			checkIsAssociatedRunningProcesses(list);

			getErrorMessge();
			if (!checkPboIsDelect(list)) {
				// errorMessage.append("\n 随签列表中没有主对象，请点击重新加载随签对象按钮");
				Locale locale = SessionHelper.manager.getLocale();
				errorMessage.append(WTMessage.getLocalizedMessage(RESOURCE, "WF_PBO_NULL", null, locale));
				getErrorMessge();
			}
		}
	}

	/**
	 * 对关联流程的对象进行设置生命周期状态
	 * 
	 * @param self
	 *            流程本体
	 * @param states
	 *            状态
	 * @throws WTException
	 *             异常
	 */

	public void setReviewObjectStates(String state) throws WTException {

		if (self != null) {
			WfProcess process = WorkflowUtil.getProcess(self);
			if (process != null) {
				ProcessReviewObjectLink link = null;
				QueryResult queryresult = ProcessReviewObjectLinkHelper.service
						.getProcessReviewObjectLinkByRoleA(process);
				while (queryresult != null && queryresult.hasMoreElements()) {
					link = (ProcessReviewObjectLink) queryresult.nextElement();
					WTObject obj = (WTObject) link.getRoleBObject();
					if (obj != null && (obj instanceof LifeCycleManaged)) {
						setState((LifeCycleManaged) obj, state);
					}
				}
			}
		}
	}

	/**
	 * 对关联流程的对象记录生命周期初始状态--标准部件属性修改流程mao
	 * 
	 * @param self
	 *            流程本体
	 * @param states
	 *            状态
	 * @throws WTException
	 *             异常
	 */
	public String getStaReviewObjectStates() throws WTException {

		StringBuffer returnVer = new StringBuffer();
		if (self != null) {
			WfProcess process = WorkflowUtil.getProcess(self);
			if (process != null) {
				ProcessReviewObjectLink link = null;
				QueryResult queryresult = ProcessReviewObjectLinkHelper.service
						.getProcessReviewObjectLinkByRoleA(process);
				while (queryresult != null && queryresult.hasMoreElements()) {
					link = (ProcessReviewObjectLink) queryresult.nextElement();
					WTObject obj = (WTObject) link.getRoleBObject();

					if (obj != null && (obj instanceof LifeCycleManaged)) {
						if (obj instanceof WTPart) {
							WTPart part = (WTPart) obj;
							WTPart newPart = ext.appo.util.PartUtil.getLastestWTPartByNumber(part.getNumber());
							String partNumber = newPart.getNumber();
							String state = newPart.getState().toString();
							returnVer.append(partNumber + "," + state + ";");
						} else if (obj instanceof WTDocument) {
							WTDocument document = (WTDocument) obj;
							WTDocument newDocument = ext.appo.util.DocUtil.getLatestWTDocument(document.getNumber());
							String docNumber = newDocument.getNumber();
							String state = newDocument.getState().toString();
							returnVer.append(docNumber + "," + state + ";");
						}
					}
				}
			}
		}
		System.out.println("state ver===" + returnVer.toString());
		return returnVer.toString();
	}

	/**
	 * 对关联流程的对象进行设置wancheng生命周期状态--标准部件属性修改流程mao
	 * 
	 * @param self
	 *            流程本体
	 * @param states
	 *            状态
	 * @throws WTException
	 *             异常
	 */
	public void setFinalStaReviewObjectStates(String returnVer, String state) throws WTException {

		if (self != null) {
			WfProcess process = WorkflowUtil.getProcess(self);
			if (process != null) {
				ProcessReviewObjectLink link = null;
				QueryResult queryresult = ProcessReviewObjectLinkHelper.service
						.getProcessReviewObjectLinkByRoleA(process);
				while (queryresult != null && queryresult.hasMoreElements()) {
					link = (ProcessReviewObjectLink) queryresult.nextElement();
					WTObject obj = (WTObject) link.getRoleBObject();

					if (obj != null && (obj instanceof LifeCycleManaged)) {
						if (obj instanceof WTPart) {
							WTPart part = (WTPart) obj;
							WTPart newPart = ext.appo.util.PartUtil.getLastestWTPartByNumber(part.getNumber());
							String newState = getState(newPart.getNumber(), returnVer, state);
							setState(newPart, newState);

							// 说明文档
							QueryResult qrs = UploadAttachmentUtil.getAssociatedDescribeDocuments(newPart);
							// while (qrs.hasMoreElements()) {
							// WTDocument doc = (WTDocument) qrs.nextElement();
							// if
							// ("UNDERREVIEW".equals(doc.getLifeCycleState().toString()))
							// {
							// setState(doc, newState);
							// }
							// }

						} else if (obj instanceof WTDocument) {
							WTDocument document = (WTDocument) obj;
							WTDocument newDocument = ext.appo.util.DocUtil.getLatestWTDocument(document.getNumber());
							setState(newDocument, "ARCHIVED");
						} else {
							setState((LifeCycleManaged) obj, "ARCHIVED");
						}
					}
				}
			}
		}
	}

	/**
	 * 检查随签文档是否关联部件
	 * 
	 * @return
	 */
	public String checkDocRePart() {

		StringBuffer result = new StringBuffer();
		try {
			if (self != null) {
				WfProcess process = WorkflowUtil.getProcess(self);
				if (process != null) {
					ProcessReviewObjectLink link = null;
					QueryResult queryresult = ProcessReviewObjectLinkHelper.service
							.getProcessReviewObjectLinkByRoleA(process);
					// 部件关联文档的List
					List<String> tempReDocList = new ArrayList<String>();
					// 随签文档的List
					List<String> suijianDocList = new ArrayList<String>();
					while (queryresult != null && queryresult.hasMoreElements()) {
						link = (ProcessReviewObjectLink) queryresult.nextElement();
						WTObject obj = (WTObject) link.getRoleBObject();

						if (obj != null && obj instanceof WTPart) {
							WTPart part = (WTPart) obj;
							WTPart newPart = ext.appo.util.PartUtil.getLastestWTPartByNumber(part.getNumber());

							// 说明文档
							QueryResult qrs = UploadAttachmentUtil.getAssociatedDescribeDocuments(newPart);
							while (qrs.hasMoreElements()) {
								WTDocument doc = (WTDocument) qrs.nextElement();
								tempReDocList.add(doc.getNumber());
							}

						}

						if (obj != null && obj instanceof WTDocument) {
							WTDocument document = (WTDocument) obj;
							suijianDocList.add(document.getNumber());
						}

					}

					for (String suijianDocNumber : suijianDocList) {

						boolean flag = false;
						for (String s : tempReDocList) {
							if (suijianDocNumber.equals(s)) {
								flag = true;
								break;
							}
						}
						if (!flag) {
							result.append("文档" + suijianDocNumber + "没有跟部件关联！" + "\n");
						}
					}
				}
			}
		} catch (WTException e) {
			result.append("异常错误，请联系管理员！");
			e.printStackTrace();
		}
		return result.toString();

	}

	/**
	 * 对关联流程的对象进行设置bohui生命周期状态--标准部件属性修改流程mao
	 * 
	 * @param self
	 *            流程本体
	 * @param states
	 *            状态
	 * @throws WTException
	 *             异常
	 */
	public void setInitStaReviewObjectStates(String returnVer, String state) throws WTException {

		if (self != null) {
			WfProcess process = WorkflowUtil.getProcess(self);
			if (process != null) {
				ProcessReviewObjectLink link = null;
				QueryResult queryresult = ProcessReviewObjectLinkHelper.service
						.getProcessReviewObjectLinkByRoleA(process);
				while (queryresult != null && queryresult.hasMoreElements()) {
					link = (ProcessReviewObjectLink) queryresult.nextElement();
					WTObject obj = (WTObject) link.getRoleBObject();

					if (obj != null && (obj instanceof LifeCycleManaged)) {
						if (obj instanceof WTPart) {
							WTPart part = (WTPart) obj;
							WTPart newPart = ext.appo.util.PartUtil.getLastestWTPartByNumber(part.getNumber());
							String newState = getState(newPart.getNumber(), returnVer, state);
							setState(newPart, newState);

						} else if (obj instanceof WTDocument) {
							WTDocument document = (WTDocument) obj;
							WTDocument newDocument = ext.appo.util.DocUtil.getLatestWTDocument(document.getNumber());
							String newState = getState(newDocument.getNumber(), returnVer, state);
							setState(newDocument, newState);
						}
					}
				}
			}
		}
	}

	private static String getState(String number, String returnVer, String state) {
		String newState = state;
		System.out.println("returnVer===" + returnVer);
		if (returnVer != null && returnVer.length() > 0) {
			String[] teamV = returnVer.split(";");
			for (String ss : teamV) {
				if (ss != null && ss.length() > 0) {
					String[] tempVV = ss.split(",");
					if (tempVV != null && tempVV.length > 1) {
						if (number.equals(tempVV[0])) {
							return tempVV[1];
						}
					}
				}
			}
		}
		System.out.println("newState===" + newState);
		return newState;
	}

	/**
	 * 检查随签对象是否已经在其他流程中正在运行
	 * 
	 * @param self
	 *            流程
	 * @throws WTException
	 *             异常
	 */
	public void checkIsAssociatedRunningProcesses(WTArrayList list) throws WTException {
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		try {
			List<WTObject> tempReviewObjsList = null;
			WfProcess wfprocess = WorkflowUtil.getProcess(this.self);
			for (int i = 0; i < list.size(); i++) {
				tempReviewObjsList = new ArrayList<WTObject>(list.size());
				WTObject obj = (WTObject) list.getPersistable(i);
				// if(obj!=null&& (obj instanceof LifeCycleManaged)){
				// LifeCycleManaged lcm = (LifeCycleManaged) obj;
				// String state = lcm.getLifeCycleState().toString();
				// if( state.equals("UNDERREVIEW")){
				// tempReviewObjsList.add(obj);
				// }
				// }
				QueryResult rs = queryObjRunningProcesses(obj, wfprocess);
				LOGGER.debug("--queryObjRunningProcesses.rs=" + rs);
				if (rs != null && rs.size() > 0) {
					tempReviewObjsList.add(obj);
				}
				if (tempReviewObjsList.size() > 0) {
					errorMessage.append(WTMessage.getLocalizedMessage(RESOURCE,
							"WF_TASK_REVIEWOBJECT_IS_ASSOCIATEDPROCESSES", null, Locale.CHINA));
					errorMessage.append(getCollectionDisplayInfo(tempReviewObjsList));
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
			throw e;
		} finally {
			SessionServerHelper.manager.setAccessEnforced(enforce);
		}
	}

	/**
	 * 在编制节点，路由选择为取消时，对重新工作的对象设置状态
	 * 
	 * @throws WTException
	 */
	public void setCancelState() throws WTException {
		if (this.pbo != null && this.self != null) {
			WTArrayList reviewObjs = ReviewObjectAndWorkFlowLink.getReviewObjectByProcess(this.self);
			for (int i = 0; i < reviewObjs.size(); i++) {
				WTObject obj = (WTObject) reviewObjs.getPersistable(i);
				if (obj instanceof LifeCycleManaged) {
					LifeCycleManaged lcm = (LifeCycleManaged) obj;
					String state = ((LifeCycleManaged) obj).getLifeCycleState().toString();
					if (state.equals("ARCHIVED")) {
						setState(lcm, "ARCHIVED");
					}
				}
			}
		}
	}

	/**
	 * 检查关联的子件的生命周期是否符合
	 * 
	 * @param list
	 *            需要校验的部件集合，一般为随签对象列表
	 * @param validateStates
	 *            有效的子件状态
	 * @throws WTException
	 */
	public void checkSubPart(WTArrayList list, List validateStates) throws WTException {
		if (this.pbo != null && this.self != null) {
			Locale locale = SessionHelper.getLocale();
			for (int i = 0; i < list.size(); i++) {
				WTObject obj = (WTObject) list.getPersistable(i);
				if (obj instanceof WTPart) {
					// 获取关联的子件，如果不在随签列表中，判断其状态
					WTPart part = (WTPart) obj;
					QueryResult qr = WTPartHelper.service.getUsesWTPartMasters(part);
					if (qr != null) {
						while (qr.hasMoreElements()) {
							WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
							// LOGGER.debug("link -------" +
							// IdentityFactory.getDisplayIdentifier(link));

							WTPart child = CoreUtil.getWTPartByMasterAndView(link.getUses(), part.getViewName());
							if (!list.contains(child)) {
								// 判断生命周期状态
								String state = ((LifeCycleManaged) child).getLifeCycleState().toString();
								if (!validateStates.contains(state)) {
									errorMessage.append("WTPart:");
									errorMessage.append(getDisplayInfo(part));
									// errorMessage.append("中的以下子件生命周期状态不符合要求:");
									errorMessage.append(WTMessage.getLocalizedMessage(RESOURCE,
											"WF_CHILDREN_STATE_WRONG", null, locale));
									errorMessage.append(getDisplayInfo(child));
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 添加一个方法，校验流程取消路由
	 * 
	 * @throws Exception
	 */
	public void checkCancel() throws Exception {
		if (this.self != null && this.pbo != null) {
			// 先刷新
			refreshReviewObjs();
			// 收集
			WTArrayList list = collect();
			// 校验
			isObjsCheckedOut(list);
			getErrorMessge();
		}
	}

	/**
	 * 刷新随签对象
	 * 
	 * @throws WTException
	 */
	public void refreshReviewObjs() throws WTException {
		WfProcess wfprocess = null;
		wfprocess = WorkflowUtil.getProcess(this.self);
		ProcessReviewObjectLink link = null;
		if (wfprocess != null) {
			try {
				QueryResult queryresult = ProcessReviewObjectLinkHelper.service
						.getProcessReviewObjectLinkByRoleA(wfprocess);
				while (queryresult != null && queryresult.hasMoreElements()) {
					link = (ProcessReviewObjectLink) queryresult.nextElement();
					WTObject obj = (WTObject) link.getRoleBObject();
					String rejectState = link.getRejectState();
					if (obj instanceof RevisionControlled) {
						RevisionControlled rev = (RevisionControlled) obj;
						if (!VersionControlHelper.isLatestIteration(rev)) {
							ProcessReviewObjectLinkHelper.service.removeProcessorVersionLink(link);
							rev = (RevisionControlled) VersionControlHelper.service.getLatestIteration(rev, false);
							ProcessReviewObjectLink newLink = ProcessReviewObjectLinkHelper.service
									.newProcessorVersionLink(wfprocess, (WTObject) rev, (WTObject) rev.getMaster());
							newLink.setRejectState(rejectState);
							PersistenceServerHelper.manager.update(newLink);
							LOGGER.debug("更新了随签对象=" + obj.getDisplayIdentifier());
						}
					}
				}
			} catch (WTPropertyVetoException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 特殊类型随签设置起目标状态或目标阶段
	 * 
	 * @throws WTException
	 */
	public void setSpecialTargetStates() throws WTException {
		if (self != null && pbo != null) {
			WTArrayList list = ReviewObjectAndWorkFlowLink.getReviewObjectByProcess(this.self);
			// TODO 零部件签审读取partPromotionRule.xml，mbom读取partManufacturerRule.xml
			WfProcess process = WorkflowUtil.getProcess(this.self);
			String wfTemplateName = "";
			if (process != null) {
				wfTemplateName = process.getTemplate().getName();
			}
			PartPromotionXMLConfigUtil partPromotionXMLConfigUtil = null;
			if (StringUtils.isNotBlank(wfTemplateName) && "GenericManufacturingPartWF".equals(wfTemplateName)) {
				// mbom流程
				partPromotionXMLConfigUtil = new PartPromotionXMLConfigUtil("partManufacturerRule.xml");
			} else {
				partPromotionXMLConfigUtil = new PartPromotionXMLConfigUtil();
			}
			partPromotionXMLConfigUtil.partPromotionParser();
			Map<String, List<SetTargetStateBean>> typeMap = partPromotionXMLConfigUtil.getTypeTargetStateMap();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("typeMap=" + typeMap);
			}
			ChangePhaseBean changePhaseBean = ChangePhaseXMLConfigUtil.getInstance().getChangePhaseBean();
			for (int w = 0; w < list.size(); w++) {
				WTObject wtobj = (WTObject) list.getPersistable(w);
				String softType = GenerateNumberUtil.getSoftTypeName(wtobj);
				LOGGER.debug("wtobj.display=" + wtobj.getDisplayIdentifier() + ",,softType=" + softType);
				if (!typeMap.containsKey(softType)) {
					if (wtobj instanceof WTPart) {
						softType = "wt.part.WTPart";
					} else if (wtobj instanceof EPMDocument) {
						softType = "wt.epm.EPMDocument";
					} else if (wtobj instanceof WTDocument) {
						softType = "wt.doc.WTDocument";
					}
				}

				if (!typeMap.containsKey(softType)) {
					continue;
				}

				List<SetTargetStateBean> beans = typeMap.get(softType);
				SetTargetStateBean bean = getMatchBean(wtobj, beans);
				if (bean != null) {
					LOGGER.debug("bean=" + bean);
					if (StringUtils.isNotBlank(bean.getTargetState())) {
						LifeCycleHelper.service.setLifeCycleState((LifeCycleManaged) wtobj,
								State.toState(bean.getTargetState()));
					}
					if (StringUtils.isNotBlank(bean.getTagerPhaseIBA())) {
						if (StringUtils.isNotBlank(changePhaseBean.getIbaNme())) {
							IBAUtil.forceSetIBAValue((IBAHolder) wtobj, changePhaseBean.getIbaNme(),
									bean.getTagerPhaseIBA());
						}
					}
				}
			}
		}
	}

	/**
	 * 特殊类型随签设置起目标状态或目标阶段
	 * 
	 * @param xmlFile
	 *            考虑到零部件签审和mbom流程模板名称可能修改
	 * @throws WTException
	 */
	public void setSpecialTargetStates(String xmlFile) throws WTException {
		if (self != null && pbo != null) {
			WTArrayList list = ReviewObjectAndWorkFlowLink.getReviewObjectByProcess(this.self);
			// TODO 零部件签审读取partPromotionRule.xml，mbom读取partManufacturerRule.xml
			PartPromotionXMLConfigUtil partPromotionXMLConfigUtil = null;
			if (StringUtils.isNotBlank(xmlFile)) {
				// mbom流程
				partPromotionXMLConfigUtil = new PartPromotionXMLConfigUtil(xmlFile);
			} else {
				partPromotionXMLConfigUtil = new PartPromotionXMLConfigUtil();
			}
			partPromotionXMLConfigUtil.partPromotionParser();
			Map<String, List<SetTargetStateBean>> typeMap = partPromotionXMLConfigUtil.getTypeTargetStateMap();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("typeMap=" + typeMap);
			}
			ChangePhaseBean changePhaseBean = ChangePhaseXMLConfigUtil.getInstance().getChangePhaseBean();
			for (int w = 0; w < list.size(); w++) {
				WTObject wtobj = (WTObject) list.getPersistable(w);
				String softType = GenerateNumberUtil.getSoftTypeName(wtobj);
				LOGGER.debug("wtobj.display=" + wtobj.getDisplayIdentifier() + ",,softType=" + softType);
				if (!typeMap.containsKey(softType)) {
					if (wtobj instanceof WTPart) {
						softType = "wt.part.WTPart";
					} else if (wtobj instanceof EPMDocument) {
						softType = "wt.epm.EPMDocument";
					} else if (wtobj instanceof WTDocument) {
						softType = "wt.doc.WTDocument";
					}
				}

				if (!typeMap.containsKey(softType)) {
					continue;
				}

				List<SetTargetStateBean> beans = typeMap.get(softType);
				SetTargetStateBean bean = getMatchBean(wtobj, beans);
				if (bean != null) {
					LOGGER.debug("bean=" + bean);
					if (StringUtils.isNotBlank(bean.getTargetState())) {
						LifeCycleHelper.service.setLifeCycleState((LifeCycleManaged) wtobj,
								State.toState(bean.getTargetState()));
					}
					if (StringUtils.isNotBlank(bean.getTagerPhaseIBA())) {
						if (StringUtils.isNotBlank(changePhaseBean.getIbaNme())) {
							IBAUtil.forceSetIBAValue((IBAHolder) wtobj, changePhaseBean.getIbaNme(),
									bean.getTagerPhaseIBA());
						}
					}
				}
			}
		}
	}

	/**
	 * 获得条件符合的bean
	 * 
	 * @param wtobj
	 * @param beans
	 * @return
	 * @throws WTException
	 */
	private SetTargetStateBean getMatchBean(WTObject wtobj, List<SetTargetStateBean> beans) throws WTException {
		SetTargetStateBean tbean = null;
		if (wtobj != null && beans.size() > 0) {
			for (SetTargetStateBean bean : beans) {
				if (StringUtils.isBlank(bean.getIbaName()) && StringUtils.isBlank(bean.getClassification())) {
					continue;
				}

				boolean ibaflag = false;
				if (StringUtils.isNotBlank(bean.getIbaName()) && StringUtils.isNotBlank(bean.getIbaValue())) {
					String ibaValue = (String) MBAUtil.getValue(wtobj, bean.getIbaName());
					if (ibaValue != null && ibaValue.equals(bean.getIbaValue())) {
						ibaflag = true;
					}
				} else {
					ibaflag = true;
				}
				boolean clfFlag = false;
				if (StringUtils.isNotBlank(bean.getClassification()) && (wtobj instanceof WTPart)) {
					String clf = (String) MBAUtil.getValue(wtobj, GenerateNumberConstant.CLASSIFICATION);
					if (clf != null && clf.equals(bean.getClassification())) {
						clfFlag = true;
					}
				} else {
					clfFlag = true;
				}
				if (ibaflag && clfFlag) {
					tbean = bean;
					break;
				}
			}

			if (tbean == null) {
				if (wtobj instanceof EPMDocument) {
					EPMDocument epm = (EPMDocument) wtobj;
					ArrayList parts = CoreUtil.getAssociatedParts(epm);
					if (parts != null && parts.size() > 0) {
						WTPart part = (WTPart) parts.get(0);
						tbean = getMatchBean(part, beans);
					}
				}
			}

		}
		return tbean;
	}

	/**
	 * 判断部件是否需要维护CIS库
	 * 
	 * @return
	 * @throws IOException
	 * @throws WTException
	 */
	public boolean isMaintainCisLibrary() throws IOException, WTException {

		List<String> readSheet2 = new AppoExcelUtil().readSheet2();

		if (self != null) {
			WfProcess process = WorkflowUtil.getProcess(self);
			if (process != null) {
				ProcessReviewObjectLink link = null;
				QueryResult queryresult = ProcessReviewObjectLinkHelper.service
						.getProcessReviewObjectLinkByRoleA(process);
				while (queryresult != null && queryresult.hasMoreElements()) {
					link = (ProcessReviewObjectLink) queryresult.nextElement();
					WTObject obj = (WTObject) link.getRoleBObject();
					if (obj instanceof WTPart) {
						String number = ((WTPart) obj).getNumber();
						for (String key : readSheet2) {
							if (number.startsWith(key)) {
								return true;
							}
						}
					}
				}

			}
		}
		return false;
	}
}
