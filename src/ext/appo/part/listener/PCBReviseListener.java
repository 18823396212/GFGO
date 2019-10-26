package ext.appo.part.listener;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import ext.pi.core.PIClassificationHelper;
import wt.events.KeyedEvent;
import wt.events.KeyedEventListener;
import wt.fc.QueryResult;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.services.ManagerException;
import wt.services.ServiceEventListenerAdapter;
import wt.services.StandardManager;
import wt.util.WTException;
import wt.vc.VersionControlServiceEvent;
import wt.vc.wip.WorkInProgressHelper;

public class PCBReviseListener extends StandardManager implements ListenerService, Serializable {

	private static final long serialVersionUID = 1L;

	private static final String CLASSNAME = PCBReviseListener.class.getName();
	private static Logger LOGGER = LogR.getLogger(CLASSNAME);

	private KeyedEventListener listener = null;

	public String getConceptualClassname() {
		return CLASSNAME;
	}

	public static PCBReviseListener newChangePartNumberListener() throws WTException {
		PCBReviseListener instance = new PCBReviseListener();

		instance.initialize();

		return instance;
	}

	/**
	 * 添加需要监听的事件
	 */
	protected void performStartupProcess() throws ManagerException {
		listener = new WCListenerEventListener(this.getConceptualClassname());
		// 添加需要监听的事件,可以添加多个,这里添加的是删除事件POST_DELETE和修改事件 POST_MODIFY
		// getManagerService().addEventListener(listener,PersistenceManagerEvent.generateEventKey(PersistenceManagerEvent.POST_DELETE));
		// getManagerService().addEventListener(listener,PersistenceManagerEvent.generateEventKey(PersistenceManagerEvent.POST_MODIFY));
		getManagerService().addEventListener(listener,
				VersionControlServiceEvent.generateEventKey(VersionControlServiceEvent.NEW_VERSION));
	}

	/**
	 * 定义内部类，用来处理相应的事件
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @author:
	 * @time: May 18, 2010 5:53:55 PM
	 * @version 1.0
	 */
	class WCListenerEventListener extends ServiceEventListenerAdapter {
		// 检入事件
		private String newVersion = VersionControlServiceEvent.NEW_VERSION;

		public WCListenerEventListener(String manager_name) {
			super(manager_name);
		}

		public void notifyVetoableEvent(Object eve) throws WTException, RemoteException {
			// 获取当前触发的事件对象
			KeyedEvent event = (KeyedEvent) eve;
			// 获取当前被操作的持久化对象,如部件,文档,容器等
			Object target = event.getEventTarget();

			// 获取事件类型
			String eventType = event.getEventType();

			/************************ 以下代码可根据实际业务进行修改 ******************************/
			partNumberChange(target, eventType);

		}

		private void partNumberChange(Object target, String eventType) throws RemoteException, WTException {
			// 判断事件类型
			if (eventType != null && eventType.equalsIgnoreCase(newVersion)) {

				// 判断对象类型
				if (target != null && target instanceof WTPart) {
					WTPart part = (WTPart) target;
					Collection<String> classifyNodes = PIClassificationHelper.service.getClassifyNodes(part);
					if (classifyNodes != null) {
						Iterator<String> iterator = classifyNodes.iterator();
						while (iterator.hasNext()) {
							String next = iterator.next();
							String nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(next);
							//pcb
							if (nodeHierarchy.contains("E1500000")) {
								QueryResult usedByWTParts = WTPartHelper.service.getUsedByWTParts((WTPartMaster) part.getMaster());
								while (usedByWTParts.hasMoreElements()) {
									WTPart parentPart = (WTPart) usedByWTParts.nextElement();
									if (!WorkInProgressHelper.isCheckedOut(parentPart)) {
										throw new WTException("父件" + parentPart.getDisplayIdentifier() + "不是检出状态！");
									}
								}
							}
						}
					}
				}
			}
		}

	}

}
