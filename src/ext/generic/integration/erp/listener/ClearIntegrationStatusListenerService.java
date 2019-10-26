package ext.generic.integration.erp.listener;

import java.io.Serializable;

import org.apache.log4j.Logger;

import ext.com.iba.IBAUtil;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.generic.integration.erp.rule.BussinessRule;
import ext.generic.integration.erp.util.BusinessRuleXMLConfigUtil;
import wt.events.KeyedEvent;
import wt.events.KeyedEventListener;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.services.ManagerException;
import wt.services.ServiceEventListenerAdapter;
import wt.services.StandardManager;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.VersionControlServiceEvent;
import wt.vc.wip.WorkInProgressException;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.WorkInProgressServiceEvent;
/**
 * 由清除属性监听 替换
 * @author Administrator
 *
 */
@Deprecated 
public class ClearIntegrationStatusListenerService extends StandardManager implements ListenerService, Serializable {

	private static final long serialVersionUID = 1L;

	private static final String CLASSNAME = ClearIntegrationStatusListenerService.class.getName();
	private static final Logger logger = LogR.getLogger(CLASSNAME);

	private KeyedEventListener listener = null ;

	public String getConceptualClassname() {
		return CLASSNAME;
	}

	public static ClearIntegrationStatusListenerService newClearIntegrationStatusListenerService() throws WTException {
		ClearIntegrationStatusListenerService instance = new ClearIntegrationStatusListenerService();
		
		instance.initialize();
		
		return instance;
	}

	/**
	 * 添加需要监听的事件
	 */
	protected void performStartupProcess() throws ManagerException {
		listener = new WCListenerEventListener(this.getConceptualClassname());
		// 添加需要监听的事件,可以添加多个,这里添加的是删除事件POST_DELETE和修改事件 POST_MODIFY
//		getManagerService().addEventListener(listener,PersistenceManagerEvent.generateEventKey(PersistenceManagerEvent.POST_DELETE));
//		getManagerService().addEventListener(listener,PersistenceManagerEvent.generateEventKey(PersistenceManagerEvent.POST_MODIFY));
//		getManagerService().addEventListener(listener,PersistenceManagerEvent.generateEventKey(PersistenceManagerEvent.POST_STORE));
		getManagerService().addEventListener(listener, KeyedEvent.generateEventKey(WorkInProgressServiceEvent.class, WorkInProgressServiceEvent.POST_CHECKIN));
//		getManagerService().addEventListener(listener, KeyedEvent.generateEventKey(WorkInProgressServiceEvent.class , WorkInProgressServiceEvent.POST_CHECKOUT));
		getManagerService().addEventListener(listener, KeyedEvent.generateEventKey(VersionControlServiceEvent.class , VersionControlServiceEvent.NEW_VERSION));
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
		//检出事件
		private String postCheckout = WorkInProgressServiceEvent.POST_CHECKOUT ;
		
		//修订事件
		private String newVersion = VersionControlServiceEvent.NEW_VERSION;
		
		//检入事件
		private String postCheckin = WorkInProgressServiceEvent.POST_CHECKIN ;

		public WCListenerEventListener(String manager_name) {
			super(manager_name);
		}

		public void notifyVetoableEvent(Object eve) throws WTException {
			logger.trace(">>>>> 监听程序已经启动......") ;
			
			// 获取当前触发的事件对象
			KeyedEvent event = (KeyedEvent) eve;
			// 获取当前被操作的持久化对象,如部件,文档,容器等
			Object target = event.getEventTarget();
			
			//获取事件类型
			String eventType = event.getEventType();
			
			/************************ 以下代码可根据实际业务进行修改 ******************************/

			//将ERP集成的状态属性值，在零部件检出，或者修订时，清空掉
			//remove on 20161009, 检出事件不再清空发布状态，主要考虑到撤销检出后状态无法恢复
			//clearIntegrationStatusIfCheckout(target , eventType );
			//end remove
			
			clearIntegrationStatusIfRevise(target , eventType );
			
			clearIntegrationStatusIfCheckin(target , eventType );
			//----- End-------//
		}
		
		/**
		 * 零部件检出时，清空集成状态属性
		 * 
		 * @param target
		 * @param eventType
		 */
		private void clearIntegrationStatusIfCheckout(Object target , String eventType ){
			//判断事件类型
			if( eventType != null && eventType.equalsIgnoreCase( postCheckout )){
				
				//判断对象类型
				if ( target != null && target instanceof WTPart) {
					
					WTPart part = ( WTPart ) target ;
					
					boolean canRelease = BussinessRule.canReleasePart(part) ;
					
					if( canRelease ){
						try{
							boolean isWorkingCopy = WorkInProgressHelper.isWorkingCopy( part ) ;
							
							if( ! isWorkingCopy ){
								part = (WTPart)WorkInProgressHelper.service.workingCopyOf( part );
							}
							
							logger.debug(">>>>> 检出事件，清空对象集成状态属性......") ;
							
							IBAUtil.forceSetIBAValue(part, BusinessRuleXMLConfigUtil.getInstance().getPartReleaseStatus() , "");
							IBAUtil.forceSetIBAValue(part, BusinessRuleXMLConfigUtil.getInstance().getBomReleaseStatus() , "");
							
						} catch (WorkInProgressException e) {
							e.printStackTrace();
						} catch (WTException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		/**
		 * 零部件检出时，清空集成状态属性
		 * 
		 * @param target
		 * @param eventType
		 */
		private void clearIntegrationStatusIfCheckin(Object target , String eventType ){
			//判断事件类型
			if( eventType != null && eventType.equalsIgnoreCase( postCheckin )){
				
				//判断对象类型
				if ( target != null && target instanceof WTPart) {
					
					WTPart part = ( WTPart ) target ;
					
					boolean canRelease = BussinessRule.canReleasePart(part) ;
					
					if( canRelease ){
						try{
							if(!part.isLatestIteration()){
								part = (WTPart) VersionControlHelper.service.getLatestIteration(part, false);
							}
							logger.debug(">>>>> 检入事件，清空对象集成状态属性......") ;
							IBAUtil.forceSetIBAValue(part, BusinessRuleXMLConfigUtil.getInstance().getPartReleaseStatus() , "");
							IBAUtil.forceSetIBAValue(part, BusinessRuleXMLConfigUtil.getInstance().getBomReleaseStatus() , "");
							
						} catch (WorkInProgressException e) {
							e.printStackTrace();
						} catch (WTException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		/**
		 * 零部件修订时，清空集成状态属性
		 * 
		 * @param target
		 * @param eventType
		 */
		private void clearIntegrationStatusIfRevise(Object target , String eventType ){
			//判断事件类型
			if( eventType != null && eventType.equalsIgnoreCase( newVersion )){
				
				//判断对象类型
				if ( target != null && target instanceof WTPart) {
					
					WTPart part = ( WTPart ) target ;
					
					String majorVersion = CommonPDMUtil.getMajorVersion( part ) ;
					String minorVersion = CommonPDMUtil.getMinorVersion( part ) ;
					
					boolean canRelease = BussinessRule.canReleasePart(part) ;
					
					//根据版本处理修订情况
					//如果不加版本判断，在新建零部件的时候，Linux环境下回产生BUG，Windows和AIX环境也有可能。
					if( canRelease ){
						try{							
							logger.debug(">>>>> 修订事件，清空对象集成状态属性......") ;
							
							IBAUtil.forceSetIBAValue(part, BusinessRuleXMLConfigUtil.getInstance().getPartReleaseStatus() , "");
							IBAUtil.forceSetIBAValue(part, BusinessRuleXMLConfigUtil.getInstance().getBomReleaseStatus() , "");
							
						} catch (WorkInProgressException e) {
							e.printStackTrace();
						} catch (WTException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}
