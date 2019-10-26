package ext.generic.mergeproperties.listener;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import ext.generic.mergeproperties.util.MergeProperties;
import wt.build.BuildServiceEvent;
import wt.events.KeyedEvent;
import wt.fc.PersistenceManagerEvent;
import wt.fc.collections.AbstractWTCollection;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.services.ManagerException;
import wt.services.ServiceEventListenerAdapter;
import wt.services.StandardManager;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.VersionControlHelper;
import wt.vc.Versioned;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.WorkInProgressServiceEvent;

/**
 * 创建部件和检入部件时，实现属性合并 ClassificationMergePropertiesService
 * 
 */

public class ClassificationMergePropertiesService extends StandardManager implements MergePropertiesService, Serializable {
	private static final String CLASSNAME = ClassificationMergePropertiesService.class.getName();
	private ServiceEventListenerAdapter listener;
	private static Logger LOGGER = null;

	static {
		LOGGER = LogR.getLogger(CLASSNAME);
	}

	public String getConceptualClassname() {
		return CLASSNAME;
	}

	/**
	 * 创建一个新的服务
	 * 
	 * @return StandardGenericPartService服务
	 * @throws WTException
	 *             exp
	 */
	public static ClassificationMergePropertiesService newClassificationMergePropertiesService() throws WTException {
		ClassificationMergePropertiesService standardMergePropertiesService = new ClassificationMergePropertiesService();
		standardMergePropertiesService.initialize();
		return standardMergePropertiesService;
	}

	/**
	 * 
	 * 实现事件类型
	 * 
	 * @throws ManagerException
	 *             exp
	 */
	protected void performStartupProcess() throws ManagerException {
		listener = new MergePropertiesServiceEventListener(getConceptualClassname());
		getManagerService().addEventListener(listener, PersistenceManagerEvent.generateEventKey("POST_STORE"));
		getManagerService().addEventListener(listener, WorkInProgressServiceEvent.generateEventKey("POST_CHECKIN"));
		//添加creo图纸检入事件监听--参考燕芋君邮件
		getManagerService().addEventListener(listener, BuildServiceEvent.generateEventKey("POST_BUILD"));
	}

	/**
	 * 
	 * 监听实现内部类
	 * 
	 * @author Administrator 2014-12-5
	 * 
	 */
	class MergePropertiesServiceEventListener extends ServiceEventListenerAdapter {

		/**
		 * 实现监听
		 * 
		 * @param obj
		 *            对象
		 * @throws WTException
		 *             exp
		 * @throws WTPropertyVetoException
		 *             exp
		 * @throws IOException
		 *             exp
		 * @throws Exception
		 *             exp
		 */
		public void notifyVetoableEvent(Object obj) throws WTException, WTPropertyVetoException, IOException, Exception {
			KeyedEvent keyedevent = (KeyedEvent) obj;
			Object wtObject = keyedevent.getEventTarget();
			String eventType = keyedevent.getEventType();
			if (obj instanceof PersistenceManagerEvent) {
				if (eventType.equals(PersistenceManagerEvent.POST_STORE)) {
					if (wtObject instanceof WTPart) {
						LOGGER.debug("  进入保存事件。。。。。。");
						WTPart part = (WTPart) wtObject;
						String thelastVersion = VersionControlHelper.getVersionIdentifier((Versioned) part).getValue();
						String lastsmallVersion = VersionControlHelper.getIterationIdentifier((Versioned) part).getValue();
//						if (thelastVersion.equalsIgnoreCase("A") && lastsmallVersion.equals("1")) {
							if (WorkInProgressHelper.isCheckedOut(part) && WorkInProgressHelper.isWorkingCopy(part)) {
								LOGGER.debug(">>>>>对象已经检出/是工作副本>>>>>");
							} else {
								LOGGER.debug(" POST_STORE eneter thelastVersion=" + thelastVersion + " lastsmallVersion=" + lastsmallVersion);
								MergeProperties mergeProperties = new MergeProperties();
								mergeProperties.setProperties(part);
							}
						}
//					}
				}
			}
			if (obj instanceof WorkInProgressServiceEvent) {
				if (eventType.equals(WorkInProgressServiceEvent.POST_CHECKIN)) {
					if (wtObject instanceof WTPart) {
						LOGGER.debug("  进入检入事件。。。。。。");
						WTPart part = (WTPart) wtObject;
						MergeProperties mergeProperties = new MergeProperties();
						mergeProperties.setProperties(part);
					}
				}
			}
			//添加creo图纸检入事件监听--参考燕芋君邮件
			if((obj instanceof BuildServiceEvent) && (eventType.equals(BuildServiceEvent.POST_BUILD))){
				LOGGER.debug("--进入creo检入构建事件--");
				if(wtObject instanceof AbstractWTCollection){
					AbstractWTCollection col = (AbstractWTCollection) wtObject;
					Iterator colIter = col.persistableIterator();
					while(colIter.hasNext()){
						Object nextObj = colIter.next();
						if(nextObj instanceof WTPart){
							WTPart part = (WTPart) nextObj;
							MergeProperties mergeProperties = new MergeProperties();
							mergeProperties.setProperties(part);
						}
					}
				}
			}

		}

		/**
		 * 构建MergepropertiesServiceEventListener使用s作为service id
		 * 
		 * @param s
		 *            :
		 */
		public MergePropertiesServiceEventListener(String s) {
			super(s);
		}

	}

}
