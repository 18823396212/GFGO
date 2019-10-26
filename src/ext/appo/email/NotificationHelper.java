package ext.appo.email;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ptc.core.logging.Log;
import com.ptc.core.logging.LogFactory;

import wt.method.RemoteAccess;
import wt.notify.DistributionList;
import wt.notify.Notification;
import wt.notify.SimpleNotification;
import wt.notify.WTDistributionList;
import wt.org.WTUser;
import wt.util.WTException;
import wt.workflow.work.WorkItem;

/**
 * 此类用于发送邮件相关方法
 */
public class NotificationHelper implements Serializable, RemoteAccess {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3368961817818573319L;
	static final Log log = LogFactory.getLog(NotificationHelper.class);
	private static final String BODY_CONTENT = "尊敬的PLM用户，您好！";
	private static final String SUBJECT_1 = "系统通知：PLM系统未完成活动项";
	private static final String NEXTLINE_TAG = "\n";

	/**
	 * 发送超时流程及项目通知
	 */
	public static void sendNotification() {

		sendWorkflowNotification();
	}

	/**
	 * 发送超期流程活动通知
	 */
	public static void sendWorkflowNotification() {

		// 取得需要发送邮件的工作流活动结果集
		List<WorkItem> workItems = WfUncompleteWorkItemsService.getWfUncomplteWorkItems();

		// 根据用户进行分类的工作流活动结果集
		Map<WTUser, List<WorkItem>> map = WfUncompleteWorkItemsService.getWorkItemInfo(workItems);

		if (map == null || map.size() < 0) {
			log.info("没有满足条件的活动项！");
			return;
		} else {
			// 定义遍历map使用的变量 ， 取得所有需要邮件通知的用户
			Set<WTUser> set = map.keySet();
			Iterator<WTUser> it = set.iterator();

			while (it.hasNext()) {
				WTUser user = (WTUser) it.next();
				List<WorkItem> items = map.get(user);
				// 建立需要邮件通知的用户列表
				DistributionList userList = new WTDistributionList();

				try {
					userList.addUser((WTUser) user);
					Notification notification = new SimpleNotification(userList);
					// 设置邮件主题
					((SimpleNotification) notification).setSubject(SUBJECT_1);
					// 邮件发送内容
					String s = getWorkflowBodyContent(items);
					notification.setBody(s);
					wt.notify.NotificationHelper.manager.send(notification);

				} catch (WTException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	/**
	 * 获取流程活动邮件内容
	 * 
	 * @param workItems
	 * @return
	 */
	public static String getWorkflowBodyContent(List<WorkItem> workItems) {

		StringBuffer bodyContend = new StringBuffer(BODY_CONTENT + NEXTLINE_TAG);
		if (workItems == null || workItems.size() < 0) {
			return null;
		}

		// 循环用户所有流程活动项并组成邮件内容
		for (WorkItem workItem : workItems) {
			bodyContend.append(NEXTLINE_TAG);
			bodyContend.append("现有\"");
			bodyContend.append(WfUncompleteWorkItemsService.getActivityByWorkItem(workItem));
			bodyContend.append("\"流程活动需要你处理，请及时点击链接处理：");
			bodyContend.append(NEXTLINE_TAG);
			bodyContend.append(WfUncompleteWorkItemsService.getUrl(workItem));
			bodyContend.append(NEXTLINE_TAG);
		}

		return bodyContend.toString();
	}

}
