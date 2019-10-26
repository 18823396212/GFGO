package ext.appo.email;

/** *********************************************************************** */
/*                                                                          */
/* Copyright (c) 2008-2012 YULONG Company */
/* 宇龙计算机通信科技（深圳）有限公司版权所有 2008-2012 */
/*                                                                          */
/* PROPRIETARY RIGHTS of YULONG Company are involved in the */
/* subject matter of this material. All manufacturing, reproduction, use, */
/* and sales rights pertaining to this subject matter are governed by the */
/* license agreement. The recipient of this software implicitly accepts */
/* the terms of the license. */
/* 本软件文档资料是宇龙公司的资产，任何人士阅读和使用本资料必须获得 */
/* 相应的书面授权，承担保密责任和接受相应的法律约束。 */
/*                                                                          */
/** *********************************************************************** */

/**
 * <pre>
 * 系统缩写：PLM 
 * 系统名称：产品生命周期管理系统 
 * 组件编号：C_系统管理
 * 组件名称：系统管理
 * 文件名称：WfUncompleteWorkItemsService.java 
 * 作         者: 裴均宇
 * 生成日期：2011-09-23
 * </pre>
 */

/**
 * <pre>
 * 修改记录：01 
 * 修改日期：2011-09-23
 * 修  改   人：裴均宇
 * 关联活动：IT-DB00040918 C_系统管理_邮件跟催_代码开发_peijunyu
 * 修改内容：初始化
 * </pre>
 */
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ext.appo.email.bean.ProcessBean;
import ext.appo.util.DateUtil;
import ext.appo.util.NumberUtil;
import ext.appo.util.WorkflowUtils;
import wt.fc.QueryResult;
import wt.httpgw.URLFactory;
import wt.org.WTUser;
import wt.util.WTException;
import wt.workflow.WfException;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;
import wt.workflow.work.WorkflowHelper;

/**
 * 此类提供获取超期流程流动统计方法
 */
public class WfUncompleteWorkItemsService {

	private static final Logger LOGGER = Logger.getLogger(WfUncompleteWorkItemsService.class);
	private static final String CLASS_NAME = WfUncompleteWorkItemsService.class.getName();
	public static final String URL_PREFIX = "servlet/TypeBasedIncludeServlet?";
	public static final String URL_SUFFIX = "&u8=1";

	/**
	 * 获取系统超出默认时间未完成流程活动项
	 * 
	 * @return
	 */
	public static List<WorkItem> getWfUncomplteWorkItems() {

		// 未完成流程活动项
		List<WorkItem> workItems = new ArrayList<WorkItem>();
		QueryResult qr;

		try {

			// 获取系统所有未完成流程活动项
			qr = WorkflowHelper.service.getUncompletedWorkItems();

			// 配置的活动项列表
			WorkflowConfigFactory facotry = new WorkflowConfigFactory();
			List<ProcessBean> beans = facotry.parseXML();
			// 筛选满足条件的活动项
			while (qr.hasMoreElements()) {
				WorkItem workItem = (WorkItem) qr.nextElement();

				// 检查活动项是否配置为不检查
				if (checkWorkItemIsConfig(workItem, beans)) {
					workItems.add(workItem);
				}
			}
		} catch (WfException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.error(CLASS_NAME + " error", e);
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.error(CLASS_NAME + " error", e);
		}

		return workItems;
	}

	/**
	 * 获取流程活动项默认完成的日期
	 * 
	 * @param date
	 * @param timeSpace
	 * @return
	 */
	public static Date getAfterDate(Date date, String timeSpace) {

		if (NumberUtil.isNumberic(timeSpace)) {
			int space = Integer.parseInt(timeSpace);
			return DateUtil.getDateAfter(date, space);
		} else {
			return date;
		}
	}

	/**
	 * 针对用户对流程项进行划分
	 * 
	 * @param workItems
	 * @return
	 */
	public static Map<WTUser, List<WorkItem>> getWorkItemInfo(List<WorkItem> workItems) {

		Map<WTUser, List<WorkItem>> workItemInfo = new HashMap<WTUser, List<WorkItem>>();

		Set<WTUser> keys = workItemInfo.keySet();

		for (WorkItem workItem : workItems) {
			// 获取流程项对应的用户
			WTUser user = (WTUser) workItem.getOwnership().getOwner().getObject();

			// 检查map是否包含该用户key值
			if (keys.contains(user)) {
				List<WorkItem> itemList = workItemInfo.get(user);
				itemList.add(workItem);
			} else {
				List<WorkItem> itemList = new ArrayList<WorkItem>();
				itemList.add(workItem);
				workItemInfo.put(user, itemList);
			}

		}
		return workItemInfo;
	}

	/**
	 * 检查流程项是否配置为不检查
	 * 
	 * @param workItem
	 * @return
	 */
	public static boolean checkWorkItemIsConfig(WorkItem workItem, List<ProcessBean> beans) {

		// 默认活动项为需要检查
		boolean flag = true;

		// 默认活动项超期时间
		String endTime = WorkflowConfigFactory.DEFAULT_ENDTIME;

		try {
			// 取得活动项对应的活动
			WfAssignedActivity activity = (WfAssignedActivity) workItem.getSource().getObject();

			// Monitor活动不需要发送邮件
			if (activity.getName().contains("Monitor") || activity.getName().contains("monitor")) {
				return flag = false;
			}

			if (activity != null) {
				// 取得活动对应的流程实例
				WfProcess process = WorkflowUtils.getProcessByActivity(activity);

				if (process != null) {
					String processName = process.getTemplate().getName();
					Date date = activity.getStartTime();

					for (ProcessBean bean : beans) {
						String wf_name = bean.getProcessName();
						String activity_name = bean.getActivityName();
						String type = bean.getType();

						// 检查活动项的流程名称和活动名称是否配置为不检查,如果匹配为不检查则跳出循环并flag赋值为flase
						if (wf_name.equals(processName)
								&& (activity_name.equals(activity.getName()) || activity_name.equals("*"))
								&& type.equals("1")) {
							flag = false;
							break;
						}
						// 循环配置列表获取相应活动项的配置超期时间，如果没有匹配则返回默认配置时间DEFAULT_ENDTIME
						else if (wf_name.equals(processName)
								&& (activity_name.equals(activity.getName()) || activity_name.equals("*"))
								&& type.equals("0")) {
							endTime = bean.getEndTimeSpace();
						}
					}

					// 取得活动项超期时间
					date = getAfterDate(date, endTime);

					if (flag && date != null && date.after(new Date())) {
						// 如果活动项超期时间在当前时间后，则不发邮件
						flag = false;
					}
				} else {
					flag = false;
				}
			} else {
				flag = false;
			}
		} catch (Exception e) {
			flag = false;
			// TODO Auto-generated catch block
			LOGGER.error(CLASS_NAME + " error", e);
		}

		return flag;
	}

	/**
	 * 检查流程项是否发邮件给主管
	 * 
	 * @param workItem
	 * @return
	 */
	public static boolean checkSendManagerEmail(WorkItem workItem, List<ProcessBean> beans) {

		// 默认不给主管发送邮件
		boolean flag = false;

		try {
			// 取得活动项对应的活动
			WfAssignedActivity activity = (WfAssignedActivity) workItem.getSource().getObject();

			if (activity != null) {
				// 取得活动对应的流程实例
				WfProcess process = WorkflowUtils.getProcessByActivity(activity);

				if (process != null) {
					String processName = process.getTemplate().getName();
					Date date = activity.getStartTime();
					// // 配置的活动项列表
					// WorkflowConfigFactory facotry = new
					// WorkflowConfigFactory();
					// List<ProcessBean> beans = facotry.parseXML();

					// 默认活动项主管超期时间
					String managerEndTime = WorkflowConfigFactory.DEFAULT_MANAGERENDTIME;

					for (ProcessBean bean : beans) {
						String wf_name = bean.getProcessName();
						String activity_name = bean.getActivityName();
						String type = bean.getType();

						// 循环配置列表获取相应活动项的配置超期时间，如果没有匹配则返回默认配置时间DEFAULT_MANAGERENDTIME
						if (wf_name.equals(processName)
								&& (activity_name.equals(activity.getName()) || activity_name.equals("*"))
								&& type.equals("0")) {
							managerEndTime = bean.getManagerEndTimeSpace();
							break;
						}
					}

					// 取得活动项超期时间
					date = getAfterDate(date, managerEndTime);

					if (date != null && date.before(new Date())) {
						// 如果活动项超期时间在当前时间前，则需求发送主管邮件
						flag = true;
					}
				}
			}
		} catch (Exception e) {
			flag = false;
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.error(CLASS_NAME + " error", e);
		}
		return flag;
	}

	/**
	 * 获取流程项对应RUL
	 * 
	 * @param workItem
	 * @return
	 */
	public static String getUrl(WorkItem workItem) {

		StringBuffer url = new StringBuffer();
		if (workItem == null) {
			return null;
		}

		String taskUrlInfo = workItem.getTaskURLPathInfo();

		if (taskUrlInfo == null) {
			return null;
		}
		try {
			taskUrlInfo = taskUrlInfo.substring(taskUrlInfo.indexOf("oid"), taskUrlInfo.indexOf("&action"));
			URLFactory factory = new URLFactory();
			url.append(factory.getBaseHREF());
			url.append(URL_PREFIX);
			url.append(taskUrlInfo);
			url.append(URL_SUFFIX);
			return url.toString();
		} catch (WTException e) {
			e.printStackTrace();
			LOGGER.error(CLASS_NAME + " error", e);
			return null;
		}
	}

	/**
	 * 根据流程项获取流程名称
	 * 
	 * @param workItem
	 * @return
	 */
	public static String getActivityByWorkItem(WorkItem workItem) {

		if (workItem == null) {
			return null;
		}

		WfAssignedActivity activity = (WfAssignedActivity) workItem.getSource().getObject();

		if (activity != null) {
			return activity.getName();
		} else {
			return null;
		}
	}

}
