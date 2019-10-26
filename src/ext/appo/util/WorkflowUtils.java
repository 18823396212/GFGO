/** *********************************************************************** */
/*                                                                        */
/* Copyright (c) 2008-2012 YULONG Company */
/* 宇龙计算机通信科技（深圳）有限公司版权所有 2008-2012 */
/*                                                                        */
/* PROPRIETARY RIGHTS of YULONG Company are involved in the */
/* subject matter of this material. All manufacturing, reproduction, use, */
/* and sales rights pertaining to this subject matter are governed by the */
/* license agreement. The recipient of this software implicitly accepts */
/* the terms of the license. */
/* 本软件文档资料是宇龙公司的资产，任何人士阅读和使用本资料必须获得 */
/* 相应的书面授权，承担保密责任和接受相应的法律约束。 */
/*                                                                        */
/** *********************************************************************** */

/**
 * <pre>
 * 系统缩写：PLM 
 * 系统名称：产品生命周期管理系统 
 * 组件编号：C_流程管理 
 * 组件名称：流程管理 
 * 文件名称：WorkflowUtils.java 
 * 作 者: 毛兵义 
 * 生成日期：2011-05-06
 * </pre>
 */

package ext.appo.util;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import wt.access.AccessControlHelper;
import wt.access.AccessPermission;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.ReferenceFactory;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTUser;
import wt.pom.Transaction;
import wt.util.WTException;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;
import wt.workflow.engine.ProcessData;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfBlock;
import wt.workflow.engine.WfContainer;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;

/**
 * 
 * 与工作流程相关的基本方法
 * 
 */
public class WorkflowUtils implements RemoteAccess {

	private static final Logger LOGGER = Logger.getLogger(WorkflowUtils.class);
	private static final String CLASS_NAME = WorkflowUtils.class.getName();
	static final boolean SERVER = RemoteMethodServer.ServerFlag;

	/**
	 * 通过活动节点获取流程对象
	 * 
	 * @param wfAct
	 *            活动节点
	 * @return
	 */
	public static WfProcess getProcessByActivity(WfActivity wfAct) {

		WfContainer wfcont = (WfContainer) wfAct.getParentProcessRef().getObject();
		if (wfcont instanceof WfBlock) {
			return null;
		}
		return (WfProcess) wfcont;

	}

	/**
	 * 设置流程中定义的全局变量的值
	 * 
	 * @param workItem
	 *            流程工作项对象
	 * @param variable
	 *            流程中定义的全局变量
	 * @param value
	 *            设置的值
	 * @return
	 */
	public static Boolean setProcessVariableValue(WorkItem workItem, String variable, Object value) {

		final String METHOD_NAME = "setProcessVariableValue(WorkItem workItem, String variable, Object value)";
		// 使用事务
		Transaction tx = new Transaction();
		// 变更到管理员
		WTUser wtUser = ChangeSession.changeToAdministratorSession();
		try {
			tx.start();
			WfAssignedActivity activity = (WfAssignedActivity) workItem.getSource().getObject();
			WfProcess process = getProcessByActivity(activity);
			ProcessData pd = process.getContext();
			pd.setValue(variable, value);

			PersistenceHelper.manager.refresh(process);
			PersistenceHelper.manager.save(process);

			tx.commit();
			tx = null;
			ChangeSession.changeToPreviousSession(wtUser);
			return new Boolean(true);
		} catch (Exception e) {
			LOGGER.error(CLASS_NAME + " " + METHOD_NAME + " error", e);
			return new Boolean(false);
		} finally {
			if (tx != null) {
				tx.rollback();
				tx = null;
			}
			ChangeSession.changeToPreviousSession(wtUser);
		}
	}

	/**
	 * 获取流程对象中指定全局变量的值
	 * 
	 * @param reference
	 *            流程对象的参考
	 * @param variable
	 *            流程全局变量
	 * @return
	 */
	public static String getProcessVariableValue(ObjectReference reference, String variable) {

		final String METHOD_NAME = "getProcessVariableValue(ObjectReference reference, String variable)";
		String result = "";
		try {
			Object object = reference.getObject();
			if (object != null && object instanceof WfAssignedActivity) {
				WfAssignedActivity activity = (WfAssignedActivity) object;
				WfProcess process = getProcessByActivity(activity);
				result = (String) process.getContext().getValue(variable);

			}
		} catch (Exception e) {
			LOGGER.error(CLASS_NAME + " " + METHOD_NAME + " error", e);
		}
		return result;
	}

	/**
	 * 获取流程对象中指定全局变量的值
	 * 
	 * @param oid
	 *            流程对象的oid
	 * @param variable
	 *            流程全局变量
	 * @return
	 */
	public static String getProcessVariableValue(String oid, String variable) {

		final String METHOD_NAME = "getProcessVariableValue(String oid, String variable)";
		String result = "";

		try {
			ReferenceFactory rf = new ReferenceFactory();
			if (rf.getReference(oid) != null) {
				Object item = rf.getReference(oid).getObject();

				if (item != null && item instanceof WorkItem) {
					WorkItem workItem = (WorkItem) item;
					WfAssignedActivity activity = (WfAssignedActivity) workItem.getSource().getObject();
					WfProcess process = getProcessByActivity(activity);
					result = (String) process.getContext().getValue(variable);
				}
			}
		} catch (Exception e) {
			LOGGER.error(CLASS_NAME + " " + METHOD_NAME + " error", e);
		}

		return result;
	}

	/**
	 * 判断流程中主对象是否为checkOut
	 * 
	 * @param workItem
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean isCheckOut(String oid) {

		final String METHOD_NAME = "isCheckOut(String oid)";
		boolean isCheckOut = false;
		if (SERVER) {
			// 变更到管理员
			WTUser wtUser = ChangeSession.changeToAdministratorSession();
			try {
				WorkItem workItem = (WorkItem) WTObjectUtil.getObjectByOid(oid);
				Persistable persistable = workItem.getPrimaryBusinessObject().getObject();
				if (persistable instanceof Workable) {
					Workable workable = (Workable) persistable;
					isCheckOut = WorkInProgressHelper.isCheckedOut(workable);
				}
			} catch (WTException e) {
				LOGGER.error(CLASS_NAME + " " + METHOD_NAME + " error", e);
			} finally {
				ChangeSession.changeToPreviousSession(wtUser);
			}
		} else {
			try {
				Class argTypes[] = { String.class };
				Object args[] = { oid };
				return ((Boolean) RemoteMethodServer.getDefault().invoke("isCheckOut", WorkflowUtils.class.getName(),
						null, argTypes, args));
			} catch (InvocationTargetException e) {
				LOGGER.error(CLASS_NAME + " " + METHOD_NAME + " error", e);
			} catch (RemoteException rme) {
				LOGGER.error(CLASS_NAME + " " + METHOD_NAME + " error", rme);
			}
		}
		return isCheckOut;
	}

	/**
	 * 判断当前用户是否有流程中主对象的读取权限
	 * 
	 * @param oid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean haveAccessPermission(String oid) {

		final String METHOD_NAME = "haveAccessPermission(String oid)";
		boolean haveAccessPermission = false;
		if (SERVER) {
			// 变更到管理员
			WTUser wtUser = ChangeSession.changeToAdministratorSession();
			try {
				WorkItem workItem = (WorkItem) WTObjectUtil.getObjectByOid(oid);
				Persistable persistable = workItem.getPrimaryBusinessObject().getObject();
				ChangeSession.changeToPreviousSession(wtUser);
				haveAccessPermission = AccessControlHelper.manager.hasAccess(wtUser, persistable,
						AccessPermission.READ);
			} catch (WTException e) {
				LOGGER.error(CLASS_NAME + " " + METHOD_NAME + " error", e);
			} finally {
				ChangeSession.changeToPreviousSession(wtUser);
			}
		} else {
			try {
				Class argTypes[] = { String.class };
				Object args[] = { oid };
				return ((Boolean) RemoteMethodServer.getDefault().invoke("haveAccessPermission",
						WorkflowUtils.class.getName(), null, argTypes, args));
			} catch (InvocationTargetException e) {
				LOGGER.error(CLASS_NAME + " " + METHOD_NAME + " error", e);
			} catch (RemoteException rme) {
				LOGGER.error(CLASS_NAME + " " + METHOD_NAME + " error", rme);
			}
		}
		return haveAccessPermission;
	}

}
