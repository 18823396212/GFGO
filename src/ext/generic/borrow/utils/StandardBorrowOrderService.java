package ext.generic.borrow.utils;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import wt.access.AccessControlHelper;
import wt.access.AccessPermission;
import wt.access.AdHocAccessKey;
import wt.access.AdHocControlled;
import wt.change2.ChangeActivity2;
import wt.change2.ChangeHelper2;
import wt.change2.ChangeRecord2;
import wt.change2.WTChangeOrder2;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectNoLongerExistsException;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTList;
import wt.identity.IdentityFactory;
import wt.log4j.LogR;
import wt.org.WTPrincipalReference;
import wt.pom.PersistenceException;
import wt.pom.Transaction;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.services.StandardManager;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import com.ptc.core.foundation.type.server.impl.TypeHelper;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.type.mgmt.server.impl.TypeDomainHelper;

import ext.appo.ecn.common.util.ChangeUtils;
import ext.generic.borrow.BorrowOrder;
import ext.generic.borrow.BorrowOrderReferenceLink;
import ext.generic.borrow.BorrowPermissionType;

public class StandardBorrowOrderService extends StandardManager implements BorrowOrderService , Serializable {

	private static final long serialVersionUID = -2842925604376381479L;

	private static final Logger logger = LogR.getLogger(StandardBorrowOrderService.class.getName());

	private static final long ONE_DAY_TIME = 24*60*60*1000 ;
	/**
	 * MethodServer refectively calls this API during startup
	 * 
	 * @return
	 * @throws WTException
	 */
	public static StandardBorrowOrderService newStandardBorrowOrderService()throws WTException {

		final StandardBorrowOrderService instance = new StandardBorrowOrderService();

		instance.initialize();

		return instance;
	}

	@Override
	public BorrowOrder getBorrowOrder(String number ) {
		BorrowOrder borrowOrder = null ;
		
		try{
			QuerySpec qs = new QuerySpec( BorrowOrder.class) ;
			
			SearchCondition sc = new SearchCondition( BorrowOrder.class, BorrowOrder.NUMBER, SearchCondition.EQUAL , number );
			qs.appendSearchCondition(sc);
			
			QueryResult qr = PersistenceHelper.manager.find(qs);
			
			if ( qr != null && qr.hasMoreElements()) {
				
				borrowOrder = ( BorrowOrder ) qr.nextElement();
			}
		} catch (QueryException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
		
		return borrowOrder ;
	}

	@Override
	public BorrowOrderReferenceLink createLink(BorrowOrder borrowOrder, WTObject wtobject, String accessPermission) {
		logger.debug(" Enter in createLink(BorrowOrder borrowOrder, WTObject wtobject, String accessPermission)...");
		
		BorrowOrderReferenceLink aLink = createLink( borrowOrder , wtobject ) ;
		
		logger.debug(" accessPermission = " + accessPermission );
		
		if( ! isNullString( accessPermission )){
			BorrowPermissionType permissionType = BorrowPermissionType.toBorrowPermissionType( accessPermission ) ;
			
			if( permissionType != null ){
				logger.debug(" set permissionType");
				
				try {
					aLink.setBorrowPermissionType( permissionType ) ;
					
					aLink = ( BorrowOrderReferenceLink ) PersistenceHelper.manager.save( aLink ) ;
				} catch (WTPropertyVetoException e) {
					e.printStackTrace();
				} catch (WTException e) {
					e.printStackTrace();
				}
			}else{
				logger.error(" permissionType == null");
			}
		}
		
		logger.debug(" Exist out createLink(BorrowOrder borrowOrder, WTObject wtobject, String accessPermission)...");
		
		return aLink ;
	}
	
	@Override
	public BorrowOrderReferenceLink createLink(BorrowOrder borrowOrder,WTObject wtobject) {
		logger.debug(" Enter in createLink(BorrowOrder borrowOrder,WTObject wtobject)...");
		
		BorrowOrderReferenceLink newBorrowOrderReferenceLink = null ;
		
		if( borrowOrder == null ){
			logger.error("createLink , but ( BorrowOrder borrowOrder ) is null");
			
			return newBorrowOrderReferenceLink ;
		}
		
		if( wtobject == null ){
			logger.error("createLink , but ( WTObject wtobject ) is null");
			
			return newBorrowOrderReferenceLink ;
		}
		
		if( logger.isDebugEnabled() ){
			logger.debug("Start Create Link..." ) ;
			logger.debug("Borrow Order : " + IdentityFactory.getDisplayIdentifier(borrowOrder) ) ;
			logger.debug("WTObject : " + IdentityFactory.getDisplayIdentifier(wtobject) ) ;
		}
		
		BorrowOrderReferenceLink aLink = getLink(borrowOrder, wtobject) ;
		if( aLink != null ){
			logger.debug(" Get Link is not null");
			
			newBorrowOrderReferenceLink = aLink ;
		}else{
			logger.debug(" Get Link is null");
			
			try {
				newBorrowOrderReferenceLink = BorrowOrderReferenceLink.newBorrowOrderReferenceLink(borrowOrder, wtobject) ;
				
				newBorrowOrderReferenceLink.setBorrowPermissionType( BorrowPermissionType.getBorrowPermissionTypeDefault()) ;
				
				logger.debug(" New a link");
				
				newBorrowOrderReferenceLink = ( BorrowOrderReferenceLink ) PersistenceHelper.manager.save( newBorrowOrderReferenceLink ) ;
				
				logger.debug(" Save the link");
			} catch (WTException e) {
				e.printStackTrace();
			} catch (WTPropertyVetoException e) {
				e.printStackTrace();
			}
		}
		
		logger.debug(" Exist out createLink(BorrowOrder borrowOrder,WTObject wtobject)...");
		
		return newBorrowOrderReferenceLink ;
	}
	
	@Override
	public BorrowOrderReferenceLink updateAccessPermission(BorrowOrderReferenceLink aLink, String accessPermission) {
		logger.debug(" Enter in updateAccessPermission(BorrowOrderReferenceLink aLink, String accessPermission)...");
		
		if( aLink != null ){	
			
			try{
				if( ! PersistenceHelper.isPersistent( aLink ) ){
					aLink = (BorrowOrderReferenceLink) PersistenceHelper.manager.save( aLink ) ;
				}
				
				if( ! isNullString( accessPermission ) ){
					logger.debug("accessPermission = " + accessPermission) ;
					
					accessPermission = accessPermission.substring( accessPermission.lastIndexOf(".") + 1 ,  accessPermission.length() ) ;
					
					BorrowPermissionType permissionType = BorrowPermissionType.toBorrowPermissionType( accessPermission ) ;
					
					if( permissionType != null ){
						aLink.setBorrowPermissionType( permissionType ) ;
						
						aLink = ( BorrowOrderReferenceLink ) PersistenceHelper.manager.save( aLink ) ;
						
						logger.debug(" Save the link");
					}else{
						logger.error("toBorrowPermissionType error...") ;
					}
					
				}else{
					logger.debug("accessPermission is null string") ;
				}
			} catch (WTException e) {
				e.printStackTrace();
			} catch (WTPropertyVetoException e) {
				e.printStackTrace();
			}finally{
				
			}
		}else{
			logger.debug("aLink == null") ;
		}
		
		return aLink ;
	}
	
	@Override
	public BorrowOrder updateBorrowDays(BorrowOrder borrowOrder) throws WTException {
		logger.debug(" Enter in updateBorrowDays(BorrowOrder borrowOrder)...");
		
		if( borrowOrder != null ){
			Timestamp borrowEndDate =borrowOrder.getBorrowEndDate() ;
			
			logger.debug("borrowEndDate = " + borrowEndDate);
			if( borrowEndDate != null ){
				long borrowEndDateTimes = borrowEndDate.getTime() ;
				
				logger.debug("borrowEndDateTimes = " + borrowEndDateTimes);
				
				long currentTimes = System.currentTimeMillis() ;
				
				logger.debug("currentTimes = " + currentTimes);
				
				Long days = ( borrowEndDateTimes - currentTimes ) / ONE_DAY_TIME ;
				
				logger.debug("days = " + days );
				
				int borrowDays = days.intValue() + 1 ;
				
				logger.debug("borrowDays = " + borrowDays);
				if( days > 0 ){
					try {
						borrowOrder.setBorrowDays(borrowDays) ;
						
						borrowOrder = ( BorrowOrder ) PersistenceHelper.manager.save( borrowOrder ) ;
					} catch (WTPropertyVetoException e) {
						e.printStackTrace();
					}
				}else{
					logger.debug("borrowDays <= 0") ;
				}
			}
		}else{
			logger.debug("borrowOrder == null");
		}

		return borrowOrder ;
	}

	@Override
	public QueryResult queryLink(BorrowOrder borrowOrder, WTObject wtobject) {
		logger.debug(" Enter in queryLink(BorrowOrder borrowOrder, WTObject wtobject)...");
		
		QueryResult qr = null ;
		
		ObjectIdentifier roleAObjectIdentifier = getObjectIdentifier( borrowOrder ) ;
		if( roleAObjectIdentifier == null ){
			logger.error("queryLink , but can not get ObjectIdentifier of ( BorrowOrder borrowOrder ).");
			
			return qr ;
		}else{
			logger.debug(" roleAObjectIdentifier = " + roleAObjectIdentifier.getStringValue()) ;
		}
		
		ObjectIdentifier roleBObjectIdentifier = getObjectIdentifier( wtobject ) ;
		if( roleBObjectIdentifier == null ){
			logger.error("queryLink , but can not get ObjectIdentifier of (  WTObject wtobject ).");
			
			return qr ;
		}else{
			logger.debug(" roleBObjectIdentifier = " + roleBObjectIdentifier.getStringValue()) ;
		}
		
		try {		
			QuerySpec querySpec = new QuerySpec( BorrowOrderReferenceLink.class );
			
			int[] intArray = { 0 };
			
			querySpec.appendWhere(new SearchCondition( BorrowOrderReferenceLink.class , "roleAObjectRef.key", "=", roleAObjectIdentifier), intArray );
			querySpec.appendAnd();
			querySpec.appendWhere(new SearchCondition( BorrowOrderReferenceLink.class , "roleBObjectRef.key", "=", roleBObjectIdentifier), intArray );
			
			qr = PersistenceHelper.manager.find( querySpec ) ;
		} catch (WTException e) {
			e.printStackTrace();
		}
		
		logger.debug(" Exist out queryLink(BorrowOrder borrowOrder, WTObject wtobject)...");
		
		return qr ;
	}


	@Override
	public BorrowOrderReferenceLink getLink(BorrowOrder borrowOrder,WTObject wtobject) {
		BorrowOrderReferenceLink aLink = null ;
		
		QueryResult qr = queryLink( borrowOrder , wtobject ) ;
		
		if( qr != null){
			logger.debug("qr.size() = " + qr.size()) ;
			
			if( qr.hasMoreElements() ){
				Object obj = qr.nextElement() ;
				
				if( obj != null && obj instanceof BorrowOrderReferenceLink ){
					logger.debug(" Get BorrowOrderReferenceLink Object");
					
					aLink = (BorrowOrderReferenceLink) obj ;
				}else{
					if( obj == null ){
						logger.warn("queryResult nextElement : obj == null") ;
					}else{
						logger.warn("queryResult nextElement : obj class type is " + obj.getClass()) ;
					}
				}
			}
		}else{
			logger.warn("queryResult is null") ;
		}
		
		return aLink ;
	}


	@Override
	public QueryResult queryLinkByRoleA(BorrowOrder borrowOrder) {
		logger.debug(" Enter in queryLinkByRoleA(BorrowOrder borrowOrder)...");
		
		QueryResult qr = null ;
		
		ObjectIdentifier objectIdentifier = getObjectIdentifier( borrowOrder ) ;
		if( objectIdentifier == null ){
			logger.error("queryLinkByRoleA , but can not get ObjectIdentifier of ( BorrowOrder borrowOrder ).");
			
			return qr ;
		}else{
			logger.debug("queryLinkByRoleA , ObjectIdentifier = " + objectIdentifier.getStringValue()) ;
		}
		
		try {
			QuerySpec querySpec = new QuerySpec( BorrowOrderReferenceLink.class) ;
			
			SearchCondition searchCondition = new SearchCondition( BorrowOrderReferenceLink.class , "roleAObjectRef.key", "=" , objectIdentifier) ;
			
			querySpec.appendWhere( searchCondition , new int[]{0});
			
			qr = PersistenceHelper.manager.find(querySpec) ;
			
		} catch (QueryException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
		
		logger.debug(" Exist out queryLinkByRoleA(BorrowOrder borrowOrder)...");
		
		return qr ;
	}


	@Override
	public List<BorrowOrderReferenceLink> getLinkByRoleA(BorrowOrder borrowOrder) {
		logger.debug(" Enter in getLinkByRoleA(BorrowOrder borrowOrder)...");
		
		List<BorrowOrderReferenceLink> linkList = new ArrayList<BorrowOrderReferenceLink>() ;
		
		QueryResult qr = queryLinkByRoleA( borrowOrder ) ;
		
		if( qr != null){
			logger.debug("qr.size() = " + qr.size()) ;
			
			while( qr.hasMoreElements() ){
				Object obj = qr.nextElement() ;
				
				if( obj != null && obj instanceof BorrowOrderReferenceLink ){
					logger.debug(" Get BorrowOrderReferenceLink Object");
					
					BorrowOrderReferenceLink aLink = (BorrowOrderReferenceLink) obj ;
					
					if( ! linkList.contains( aLink ) ){
						logger.debug(" linkList add BorrowOrderReferenceLink Object");
						
						linkList.add( aLink ) ;
					}else{
						logger.debug(" linkList contains BorrowOrderReferenceLink Object");
					}
				}else{
					if( obj == null ){
						logger.warn("queryResult nextElement : obj == null") ;
					}else{
						logger.warn("queryResult nextElement : obj class type is " + obj.getClass()) ;
					}
				}
			}
		}else{
			logger.warn("queryResult is null") ;
		}
		
		logger.debug(" Exist out getLinkByRoleA(BorrowOrder borrowOrder)...");
		
		return linkList ;
	}

	
	@Override
	public List<WTObject> getBorrowedItems(BorrowOrder borrowOrder) {
		logger.debug(" Enter in getBorrowedItems(BorrowOrder borrowOrder)...");
		
		List<WTObject> borrowedItemList = new ArrayList<WTObject>() ;
		
		List<BorrowOrderReferenceLink> linkList = getLinkByRoleA( borrowOrder ) ;
		
		logger.debug(" Enter in linkList.size() = " + linkList.size());
		
		Iterator<BorrowOrderReferenceLink> linkListIte = linkList.iterator() ;
		while( linkListIte.hasNext() ){
			BorrowOrderReferenceLink aLink = linkListIte.next() ;
			
			WTObject aBorrowedItem = aLink.getBorrowObject() ;
			
			if( ! borrowedItemList.contains( aBorrowedItem ) ){
				borrowedItemList.add( aBorrowedItem ) ;
			}
		}
		
		logger.debug(" Exist out getBorrowedItems(BorrowOrder borrowOrder)...");
		
		return borrowedItemList ;
	}


	@Override
	public QueryResult queryLinkByRoleB(WTObject wtobject) {
		logger.debug(" Enter in queryLinkByRoleB(WTObject wtobject)...");
		
		QueryResult qr = null ;

		ObjectIdentifier objectIdentifier = getObjectIdentifier( wtobject ) ;
		if( objectIdentifier == null ){
			logger.error("queryLinkByRoleB , but can not get ObjectIdentifier of ( WTObject wtobject ).");
			
			return qr ;
		}else{
			logger.debug("queryLinkByRoleB , ObjectIdentifier = " + objectIdentifier.getStringValue()) ;
		}
		
		try {
			QuerySpec querySpec = new QuerySpec( BorrowOrderReferenceLink.class );
			
			SearchCondition searchCondition = new SearchCondition( BorrowOrderReferenceLink.class , "roleBObjectRef.key", "=" , objectIdentifier) ;
			
			querySpec.appendWhere( searchCondition , new int[]{0});
			
			qr = PersistenceHelper.manager.find(querySpec) ;

		} catch (QueryException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
		
		logger.debug(" Exist out queryLinkByRoleB(WTObject wtobject)...");
		
		return qr ;
	}


	@Override
	public List<BorrowOrderReferenceLink> getLinkByRoleB(WTObject wtobject) {
		logger.debug(" Enter in getLinkByRoleB(WTObject wtobject)...");
		
		List<BorrowOrderReferenceLink> linkList = new ArrayList<BorrowOrderReferenceLink>() ;
		
		QueryResult qr = queryLinkByRoleB( wtobject ) ;
		
		if( qr != null){
			logger.debug("qr.size() = " + qr.size()) ;
			
			while( qr.hasMoreElements() ){
				Object obj = qr.nextElement() ;
				
				if( obj != null && obj instanceof BorrowOrderReferenceLink ){
					logger.debug(" Get BorrowOrderReferenceLink Object");
					
					BorrowOrderReferenceLink aLink = (BorrowOrderReferenceLink) obj ;
					
					if( ! linkList.contains( aLink ) ){
						logger.debug(" linkList add BorrowOrderReferenceLink Object");
						
						linkList.add( aLink ) ;
					}else{
						logger.debug(" linkList contains BorrowOrderReferenceLink Object");
					}
				}else{
					if( obj == null ){
						logger.warn("queryResult nextElement : obj == null") ;
					}else{
						logger.warn("queryResult nextElement : obj class type is " + obj.getClass()) ;
					}
				}
			}
		}else{
			logger.warn("queryResult is null") ;
		}
		
		logger.debug(" Exist out getLinkByRoleB(WTObject wtobject)...");
		
		return linkList ;
	}


	@Override
	public List<BorrowOrder> getBorrowOrders(WTObject wtobject) {
		logger.debug(" Enter in getBorrowOrders(WTObject wtobject)...");
		
		List<BorrowOrder> borrowOrderList = new ArrayList<BorrowOrder>() ;
		
		List<BorrowOrderReferenceLink> linkList = getLinkByRoleB( wtobject ) ;
		
		logger.debug(" Enter in linkList.size() = " + linkList.size());
		
		Iterator<BorrowOrderReferenceLink> linkListIte = linkList.iterator() ;
		while( linkListIte.hasNext() ){
			BorrowOrderReferenceLink aLink = linkListIte.next() ;
			
			BorrowOrder borrowOrder = aLink.getBorrowOrder() ;
			
			if( ! borrowOrderList.contains( borrowOrder ) ){
				borrowOrderList.add( borrowOrder ) ;
			}
		}
		
		logger.debug(" Exist out getBorrowOrders(WTObject wtobject)...");
		
		return borrowOrderList ;
	}
	
	
	@Override
	public void removeLink(BorrowOrder borrowOrder, WTObject wtobject) {
		logger.debug("Enter in removeLink(BorrowOrder borrowOrder, WTObject wtobject)...");
		
		BorrowOrderReferenceLink aLink = getLink(borrowOrder, wtobject) ;

		if( aLink != null ){
			try {
				logger.debug("Delete the link");
				
				PersistenceHelper.manager.delete( aLink ) ;
			} catch (WTException e) {
				e.printStackTrace();
			}
		}else{
			logger.warn("link == null");
		}
		
		logger.debug("Exist out removeLink(BorrowOrder borrowOrder, WTObject wtobject)...");
	}


	@Override
	public void removeLinksByRoleA(BorrowOrder borrowOrder) {
		List<BorrowOrderReferenceLink> linkList = getLinkByRoleA(borrowOrder) ;
		
		if( linkList != null ){
			try {
				Iterator<BorrowOrderReferenceLink> linkListIte = linkList.iterator() ;
				
				while( linkListIte.hasNext() ){
					BorrowOrderReferenceLink aLink = linkListIte.next() ;
					
					PersistenceHelper.manager.delete( aLink ) ;
				}
			} catch (WTException e) {
				e.printStackTrace();
			}
		}
	}



	@Override
	public void removeLinksByRoleB(WTObject wtobject) {
		List<BorrowOrderReferenceLink> linkList = getLinkByRoleB(wtobject) ;
		
		if( linkList != null ){
			try {
				Iterator<BorrowOrderReferenceLink> linkListIte = linkList.iterator() ;
				
				while( linkListIte.hasNext() ){
					BorrowOrderReferenceLink aLink = linkListIte.next() ;
					
					PersistenceHelper.manager.delete( aLink ) ;
				}
			} catch (WTException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private boolean isNullString(String str) {
		if (str == null || str.trim().isEmpty()) {
			return true;
		}

		return false;
	}
	
	/**
	 * 获取对象的ObjectIdentifier标识符
	 * 
	 * @param persistable
	 * @return
	 */
	private ObjectIdentifier getObjectIdentifier( Persistable persistable ){
		logger.debug(" Enter in getObjectIdentifier( Persistable persistable )...");
		
		ObjectIdentifier objectIdentifier = null ;
		
		if( persistable == null ){
			logger.error("getObjectIdentifier , but ( Persistable persistable ) is null");
			
			return objectIdentifier ;
		}
		
		if( ! PersistenceHelper.isPersistent( persistable ) ){
			logger.error("getObjectIdentifier, but ( Persistable persistable ) is not Persistent");
			
			return objectIdentifier ;
		}
		
		objectIdentifier = PersistenceHelper.getObjectIdentifier( persistable ) ;
		if( objectIdentifier == null ){
			logger.error("getObjectIdentifier , but can not get ObjectIdentifier of ( Persistable persistable )");
			
			return objectIdentifier ;
		}else{
			logger.debug("getObjectIdentifier ,objectIdentifier = " + objectIdentifier.getStringValue()) ;
		}
		
		logger.debug("getObjectIdentifier , Persistable object display identifier is : " + IdentityFactory.getDisplayIdentifier( persistable )) ;
		
		logger.debug(" Exist out getObjectIdentifier( Persistable persistable )...");
		
		return objectIdentifier ;
	}

	@Override
	public boolean hasExpired( Object pbo ) {
		logger.debug(" Enter in hasExpired( Object pbo )...");
		
		boolean hasExpired = false ;
		
		if( pbo != null && pbo instanceof BorrowOrder ){
			BorrowOrder borrowOrder = ( BorrowOrder ) pbo ;
			
			logger.debug("Borrow Order : " + IdentityFactory.getDisplayIdentifier( borrowOrder ) ) ;
			
			Timestamp borrowEndDate = borrowOrder.getBorrowEndDate() ;
			
			logger.debug("Borrow Order End Date : " + borrowEndDate ) ;
			
			if( borrowEndDate == null ){
				
				hasExpired = true ;
			}else{
				Long borrowEndDateTimes = borrowEndDate.getTime() ;
				Long currentTimes = System.currentTimeMillis() ;
				
				logger.debug("borrowEndDateTimes = " + borrowEndDateTimes ) ;
				logger.debug("currentTimes = " + currentTimes ) ;
				
				if( currentTimes < borrowEndDateTimes ){
					hasExpired = false ;
				}else{
					hasExpired = true ;
				}
			}
		}else{
			if( pbo == null ){
				logger.error("hasExpired , pbo == null");
			}else{
				logger.error("hasExpired , pbo class type is : " + pbo.getClass() ) ;
				logger.error("hasExpired , pbo display identifier : " + IdentityFactory.getDisplayIdentifier( pbo ) ) ;
			}

			hasExpired = true ;
		}
		
		logger.debug("hasExpired = " + hasExpired ) ;
		
		return hasExpired ;
	}
	
	@Override
	public void grantPermissions( Object pbo , boolean add) {
		logger.debug(" Enter in grantPermissions(BorrowOrder borrowOrder, boolean add)...");
		
		if( pbo != null && pbo instanceof BorrowOrder ){
			BorrowOrder borrowOrder = ( BorrowOrder ) pbo ;
			
			if( logger.isDebugEnabled() ){
				logger.debug("Borrow Order : " + IdentityFactory.getDisplayIdentifier( borrowOrder ) ) ;
				logger.debug("boolean : add = " + add ) ;
			}
			
			grantBorrowItemPermissions( borrowOrder , add ) ;
		}else{
			if( pbo == null ){
				logger.error("grantPermissions , pbo == null") ;
			}else{
				logger.error("grantPermissions , pbo class type is : " + pbo.getClass() ) ;
				logger.error("grantPermissions , pbo display identifier : " + IdentityFactory.getDisplayIdentifier( pbo ) ) ;
			}
		}
	}

	private void grantBorrowItemPermissions(BorrowOrder borrowOrder , boolean add ) {
		logger.debug(" Enter in grantBorrowItemPermissions(BorrowOrder borrowOrder , boolean add )...");
		
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		
		Transaction transaction = new Transaction();
		
		try {
			transaction.start();

			WTPrincipalReference creator =  borrowOrder.getCreator() ;
			
			logger.debug("Borrow Order Creator : " + creator.getFullName() ) ;
			
			List<BorrowOrderReferenceLink> linkList = getLinkByRoleA(borrowOrder) ;
			
			Iterator<BorrowOrderReferenceLink> linkListIte = linkList.iterator() ;
			
			while( linkListIte.hasNext() ){
				BorrowOrderReferenceLink aLink = linkListIte.next() ;
				
				WTObject borrowItem = aLink.getBorrowObject() ;
				
				logger.debug("is add Permission : " + add ) ;
				// 刷新对象
				borrowItem = (WTObject) PersistenceHelper.manager.refresh( borrowItem );
				
				// 收集对象关联的ECA及ECN对象
				Collection<Persistable> ptArray = getChangeItems(borrowItem) ;
				
				// 获取授权或收回权限
				Vector<AccessPermission> permissionVector = getPermissions(aLink);
				
				for(Persistable persistable : ptArray){
					logger.debug("Borrow Item : " + IdentityFactory.getDisplayIdentifier(persistable) ) ;
					
					AdHocControlled adhoccontrolled = ( AdHocControlled ) persistable ;
					
					try {
						if( add ){
							adhoccontrolled = AccessControlHelper.manager.addPermissions(adhoccontrolled , creator , permissionVector , AdHocAccessKey.WNC_ACCESS_CONTROL);
						}else{
							adhoccontrolled = AccessControlHelper.manager.removePermissions(adhoccontrolled, creator , permissionVector, AdHocAccessKey.WNC_ACCESS_CONTROL);						
						}
						
						PersistenceServerHelper.manager.update(adhoccontrolled , false );
					} catch (Exception e) {
						e.printStackTrace() ;
					}
				}
			}
			
			transaction.commit();
			transaction = null;
		} catch (PersistenceException e) {
			e.printStackTrace();
		} catch (ObjectNoLongerExistsException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		} finally {
			if (transaction != null){
				transaction.rollback();
			}
			
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
	}

	private Vector<AccessPermission> getPermissions(BorrowOrderReferenceLink aLink) {
		BorrowPermissionType permissionType = aLink.getBorrowPermissionType();
		
		Vector<AccessPermission> permissionVector = new Vector<AccessPermission>();
		
		if( permissionType != null ){
			
			logger.debug("Borrow Permission Type : " + permissionType.getStringValue() ) ;
			
			if( permissionType.getStringValue().equals( BorrowPermissionType.READ.getStringValue() ) ){
				
				permissionVector.add( AccessPermission.READ ) ;
			}else if( permissionType.getStringValue().equals( BorrowPermissionType.READ_ADN_DOWNLOAD.getStringValue() ) ){
				
				permissionVector.add( AccessPermission.READ ) ;
				permissionVector.add( AccessPermission.DOWNLOAD ) ;
			}
		}else{
			logger.error(" permissionType == null ") ;
		}
		
		return permissionVector;
	}

	@Override
	public Vector getResionTypeHashtable() {
		Vector returnvc = new Vector();
		ArrayList disparray=new ArrayList();
		ArrayList vaulearray=new ArrayList();		
		Locale locale;
		try {
			locale = SessionHelper.getLocale();
		
		String domain = TypeDomainHelper.getExchangeDomain();
		disparray.add("全部");
		vaulearray.add("wt.doc.WTDocument|com.ptc.ReferenceDocument");
		// 读取DHF及DHF底下的子类型
		TypeIdentifier typeidentifierDHF = TypeHelper.getTypeIdentifier("wt.doc.WTDocument|com.ptc.ReferenceDocument|" + domain + ".DHFDoc");
		disparray.add("DHF文档");
		vaulearray.add(typeidentifierDHF.getTypename());
		if (typeidentifierDHF != null)
		{
			Set set = com.ptc.core.htmlcomp.util.TypeHelper.getSubTypesForType(typeidentifierDHF, locale);
			Iterator iterator = set.iterator();
			String entryname = null;
			String entrytype = null;
			String preflex = "    ";
			while (iterator.hasNext())
			{
				TypeIdentifier entry = (TypeIdentifier) iterator.next();
				if(typeidentifierDHF.equals(entry))
					continue;
				entryname = TypeHelper.getLocalizedTypeString(entry, locale);
				entrytype = entry.getTypename();
				if (entryname != null && entrytype != null)
				{
					disparray.add(preflex + entryname);
					vaulearray.add(entrytype);
				}
			}
		}
		// 读取DMR及DMR底下的子类型
		TypeIdentifier typeidentifierDMR = TypeHelper.getTypeIdentifier("wt.doc.WTDocument|com.ptc.ReferenceDocument|" + domain + ".DMRDoc");
		disparray.add("DMR文档");
		vaulearray.add(typeidentifierDMR.getTypename());
		if (typeidentifierDMR != null)
		{
			
			Set set = com.ptc.core.htmlcomp.util.TypeHelper.getSubTypesForType(typeidentifierDMR, locale);
			Iterator iterator = set.iterator();
			String entryname = null;
			String entrytype = null;
			String preflex = "    ";
			while (iterator.hasNext())
			{
				TypeIdentifier entry = (TypeIdentifier) iterator.next();
				if(typeidentifierDMR.equals(entry))
					continue;
				entryname = TypeHelper.getLocalizedTypeString(entry, locale);
				entrytype = entry.getTypename();
				if (entryname != null && entrytype != null)
				{
					disparray.add(preflex + entryname);
					vaulearray.add(entrytype);
				}
			}
		}
		disparray.add("全部DMR");
		vaulearray.add("ALLDMR");
		
		disparray.add("部件");
		vaulearray.add("wt.part.WTPart");
		
		disparray.add("EPM文档");
		vaulearray.add("wt.epm.EPMDocument");
		
		disparray.add("全部体系文件");
		vaulearray.add("ALLQualitySystem");
		
		disparray.add("    质量体系文件");
		vaulearray.add("wt.doc.WTDocument|com.ptc.ReferenceDocument|" + domain + ".QualitySystemDocumentsDoc");
		
		disparray.add("    TC质量体系文件");
		vaulearray.add("wt.doc.WTDocument|com.ptc.ReferenceDocument|" + domain + ".TCQualitySystemDocumentsDoc");
		
		returnvc.add(disparray);
		returnvc.add(vaulearray);
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnvc;
	}
	@Override
	public void grantPermissionsAgain(Object pbo) {
		// TODO Auto-generated method stub
		logger.debug(" Enter in grantPermissions(BorrowOrder borrowOrder, boolean add)...");
		
		if( pbo != null && pbo instanceof BorrowOrder ){
			BorrowOrder borrowOrder = ( BorrowOrder ) pbo ;
			
			if( logger.isDebugEnabled() ){
				logger.debug("Borrow Order : " + IdentityFactory.getDisplayIdentifier( borrowOrder ) ) ;
			}
			
			grantBorrowItemPermissionsAgain( borrowOrder) ;
		}else{
			if( pbo == null ){
				logger.error("grantPermissions , pbo == null") ;
			}else{
				logger.error("grantPermissions , pbo class type is : " + pbo.getClass() ) ;
				logger.error("grantPermissions , pbo display identifier : " + IdentityFactory.getDisplayIdentifier( pbo ) ) ;
			}
		}
	}
	
	private void grantBorrowItemPermissionsAgain(BorrowOrder borrowOrder){
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);		
		Transaction transaction = new Transaction();	
		try {
			transaction.start();

			WTPrincipalReference creator =  borrowOrder.getCreator() ;
			
			logger.debug("Borrow Order Creator : " + creator.getFullName() ) ;
			
			List<BorrowOrderReferenceLink> linkList = getLinkByRoleA(borrowOrder) ;
			
			Iterator<BorrowOrderReferenceLink> linkListIte = linkList.iterator() ;
			
			while( linkListIte.hasNext() ){
				BorrowOrderReferenceLink aLink = linkListIte.next() ;
				
				WTObject borrowItem = aLink.getBorrowObject() ;
															
				List<BorrowOrder> listBorrowOrder= getBorrowOrders(borrowItem);
				
				for (int i = 0; i < listBorrowOrder.size(); i++) {
					BorrowOrder bo= listBorrowOrder.get(i);
					if(bo.getLifeCycleState().toString().equals("APPROVED")&&bo.getCreatorName().equals(creator.getName())){
						BorrowOrderReferenceLink link= getLink(bo, borrowItem);
						Vector<AccessPermission> perVector= getPermissions(link);
						WTObject borrowItem1 = (WTObject) PersistenceHelper.manager.refresh( borrowItem );
						if ( borrowItem1 instanceof AdHocControlled ) {
							
							AdHocControlled adhoccontrolled = ( AdHocControlled ) borrowItem1 ;
							try {
								adhoccontrolled = AccessControlHelper.manager.addPermissions(adhoccontrolled , creator , perVector , AdHocAccessKey.WNC_ACCESS_CONTROL);						
								PersistenceServerHelper.manager.update(adhoccontrolled , false );
							} catch (Exception e) {
								e.printStackTrace() ;
							}
						}else{
							logger.warn("borrowItem object type is not AdHocControlled , class type is : " + borrowItem.getClass() ) ;
						}
					}
				}							
			}
			
			transaction.commit();
			transaction = null;
		} catch (PersistenceException e) {
			e.printStackTrace();
		} catch (ObjectNoLongerExistsException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		} finally {
			if (transaction != null){
				transaction.rollback();
			}
			
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
	}
	
	/***
	 * 查询对象关联的ECA及ECN对象
	 * 
	 * @param persistable
	 * @return
	 * @throws WTException
	 */
	public Collection<Persistable> getChangeItems(Persistable persistable) throws WTException{
		Collection<Persistable> returnArray = new HashSet<Persistable>() ;
		if(persistable == null){
			return returnArray ;
		}
		
		returnArray.add(persistable) ;
		
		// 查询管理的产生的对象
		WTList wtList = new WTArrayList() ;
		wtList.add(persistable) ;
		Collection<ChangeRecord2> changeRecord2s = ChangeUtils.getChangeRecord2s(wtList) ;
		for(ChangeRecord2 changeRecord2 : changeRecord2s){
			// 更改任务
			ChangeActivity2 changeActivity2 = changeRecord2.getChangeActivity2() ;
			if(changeActivity2 != null){
				returnArray.add(changeActivity2) ;
				// 查询ECN对象
				QueryResult qr = ChangeHelper2.service.getChangeOrder(changeActivity2) ;
				while(qr.hasMoreElements()){
					Object object = qr.nextElement() ;
					if(object instanceof ObjectReference){
						object = ((ObjectReference)object).getObject() ;
					}
					if(object instanceof WTChangeOrder2){
						returnArray.add((WTChangeOrder2)object) ;
					}
				}
			}
		}
		
		return returnArray ;
	}
}
