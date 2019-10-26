package ext.appo.part.listener;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import wt.access.AccessPermission;
import wt.enterprise.MadeFromLink;
import wt.enterprise.RevisionControlled;
import wt.events.KeyedEvent;
import wt.events.KeyedEventListener;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManagerEvent;
import wt.fc.QueryResult;
import wt.log4j.LogR;
import wt.org.WTPrincipalReference;
import wt.part.WTPart;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.services.ManagerException;
import wt.services.ServiceEventListenerAdapter;
import wt.services.StandardManager;
import wt.session.SessionHelper;
import wt.util.WTException;
import ext.appo.part.processor.CusCreatePartAndCADDocFormProcessor;
import ext.com.core.CoreUtil;
import ext.lang.PIStringUtils;
import ext.pi.core.PIAccessHelper;
import ext.pi.core.PIClassificationHelper;

public class ChangePartNumberListener extends StandardManager implements ListenerService, Serializable {

	private static final long serialVersionUID = 1L;

	private static final String CLASSNAME = ChangePartNumberListener.class.getName();
	private static Logger LOGGER = LogR.getLogger(CLASSNAME);

	private KeyedEventListener listener = null;

	public String getConceptualClassname() {
		return CLASSNAME;
	}

	public static ChangePartNumberListener newChangePartNumberListener() throws WTException {
		ChangePartNumberListener instance = new ChangePartNumberListener();

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
				PersistenceManagerEvent.generateEventKey(PersistenceManagerEvent.POST_STORE));
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
		private String postStore = PersistenceManagerEvent.POST_STORE;

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
			// 另存的部件
			WTPart saveAs =  null ;
			try {
				// 判断事件类型
				if (eventType != null && eventType.equalsIgnoreCase(postStore)) {

					// 判断对象类型
					if (target != null && target instanceof MadeFromLink) {
						MadeFromLink madeFromLink = (MadeFromLink) target;
						RevisionControlled copy = madeFromLink.getCopy();
						if (copy instanceof WTPart) {
							saveAs = (WTPart) copy;
							// 赋予修改逻辑标识符权限
							PIAccessHelper.service.grantPersistablePermissions(saveAs,
									WTPrincipalReference.newWTPrincipalReference(SessionHelper.manager.getPrincipal()), AccessPermission.MODIFY_IDENTITY, true);
							// 原部件
							RevisionControlled original = madeFromLink.getOriginal();
							WTPart origPart = (WTPart) original;
							// 原部件编号
							String origPartNumber = origPart.getNumber();
							Collection<String> classifyNodes = PIClassificationHelper.service.getClassifyNodes(saveAs);
							if (classifyNodes != null) {
								Iterator<String> iterator = classifyNodes.iterator();
								while (iterator.hasNext()) {
									String next = iterator.next();
									String nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(next);
									// 默认值
									String defaultValue = "";
									// 结构件
									if (nodeHierarchy.contains("appo_jgj") && origPartNumber.startsWith("C55")) {
										origPartNumber = origPartNumber.substring(0, origPartNumber.length() - 2);
										int queryPartNum = queryPartNum(origPartNumber);
										if (queryPartNum <= 9) {
											defaultValue = "0" + String.valueOf(queryPartNum);
										}
										String newNumber = origPartNumber + defaultValue;
										CoreUtil.changeNumber(saveAs, newNumber);
										// 治具和自动化设备
									} else if ((nodeHierarchy.contains("appo_zcljjzz") || nodeHierarchy.contains("appo_zcljjzdhsb"))) {
										origPartNumber = origPartNumber.substring(0, origPartNumber.length() - 3);
										int queryPartNum = queryPartNum(origPartNumber);
										if (queryPartNum <= 9) {
											defaultValue = "00" + String.valueOf(queryPartNum);
										}else if (queryPartNum > 9 && queryPartNum < 100) {
											defaultValue = "0" + String.valueOf(queryPartNum);
										}
										String newNumber = origPartNumber + defaultValue;
										CoreUtil.changeNumber(saveAs, newNumber);
									} else{
										// 设置编码
										CusCreatePartAndCADDocFormProcessor processor = new CusCreatePartAndCADDocFormProcessor() ;
										processor.setNumber(saveAs);
									}
								}
							}
						}
					}
				}
			} finally {
				if(saveAs != null){
					PIAccessHelper.service.removePersistablePermissions(saveAs,
							WTPrincipalReference.newWTPrincipalReference(SessionHelper.manager.getPrincipal()), AccessPermission.MODIFY_IDENTITY, true);
				}
			}
		}

		
		
		public int queryPartNum(String number){
			try {
				TreeMap<String, String> map = new TreeMap<String, String>() ;
				
				QuerySpec qs= new QuerySpec(WTPart.class);
				SearchCondition sc= new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.LIKE, number + "%");
				int ai[] = {0, 1};
				qs.appendWhere(sc, ai);
				QueryResult qr = PersistenceHelper.manager.find(qs);
			    while(qr.hasMoreElements()){
			    	Object object = qr.nextElement() ;
			    	if(object instanceof WTPart){
			    		// 部件编码
			    		String partNumber = ((WTPart)object).getNumber() ;
			    		if(!map.containsKey(partNumber)){
			    			map.put(partNumber, partNumber) ;
			    		}
			    	}
			    }
			    
			    String partNumber = map.lastKey() ;
			    if(PIStringUtils.isNotNull(partNumber)){
			    	if (partNumber.startsWith("C55")) {
			    		return (Integer.parseInt(partNumber.substring(partNumber.length() - 3, partNumber.length())) + 1) ;	
			    	}else{
			    	return (Integer.parseInt(partNumber.substring(partNumber.length() - 3, partNumber.length())) + 1) ;
			    	}
			    }
			} catch (QueryException e) {
				e.printStackTrace();
			} catch (WTException e) {
				e.printStackTrace();
			}
			
			return 1 ;
		}
		
		/**
		 * 设置分类属性默认值
		 * @param typeInternalValue 分类的内部名称
		 * @param internalValue 属性内部值 
		 * @param length 长度
		 * @throws NotAuthorizedException
		 * @throws WTContainerException
		 * @throws WTException
		 * @throws ParseException
		 */
//		public void setPartTypeDefinition(String typeInternalValue, String internalValue, String length)
//				throws NotAuthorizedException, WTContainerException, WTException, ParseException {
//
//			TypeDefinitionReadView nodeView = TypeDefinitionServiceHelper.service.getTypeDefView(
//					AttributeTemplateFlavor.LWCSTRUCT, "com.ptc.csm.default_clf_namespace", typeInternalValue);
//
//			TypeDefinitionWriteView paramTypeDefinitionWriteView = nodeView.getWritableView();
//			AttributeDefinitionReadView re = paramTypeDefinitionWriteView.getAttributeByName(internalValue);
//
//			AttributeDefinitionWriteView we = re.getWritableView();
//			Collection<AttributeDefaultValueReadView> c = we.getAllDefaultValues();
//			Collection<AttributeDefaultValueReadView> cv = new ArrayList<AttributeDefaultValueReadView>();
//			String defaultValue = "";
//			int devalue = 0;
//			String newDefualtValue = "";
//			for (AttributeDefaultValueReadView ar : c) {
//				if (ar.getValue() != null) {
//					defaultValue = ar.getValue().toString();
//					devalue = Integer.valueOf(defaultValue).intValue();
//					devalue = devalue + 1;
//					newDefualtValue = String.valueOf(devalue);
//					if (newDefualtValue.length() == 1 && length.equals("2")) {
//						newDefualtValue = "0" + newDefualtValue;
//					} else if (newDefualtValue.length() == 1 && length.equals("3")) {
//						newDefualtValue = "00" + newDefualtValue;
//					} else if (newDefualtValue.length() == 2) {
//						newDefualtValue = "0" + newDefualtValue;
//					}
//
//					AttributeDefaultValueWriteView wv = ar.getWritableView();
//					wv.setDeleted(true);
//					cv.add(wv);
//				}
//			}
//			String str1 = we.getDatatype().getName();
//			Locale localLocale = SessionHelper.getLocale();
//			Object localObject = (Serializable) DataTypesExternalizer.fromString(newDefualtValue, str1, localLocale);
//			cv.add(new AttributeDefaultValueWriteView((Serializable) localObject, null));
//			we.setDefaultValues(cv);
//
//			paramTypeDefinitionWriteView.setAttribute(we);
//
//			nodeView = TypeDefinitionServiceHelper.service.updateTypeDef(paramTypeDefinitionWriteView);
//		}

	}
}
