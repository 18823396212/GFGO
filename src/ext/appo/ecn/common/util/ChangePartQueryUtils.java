package ext.appo.ecn.common.util;

import ext.lang.PIStringUtils;
import wt.fc.*;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTList;
import wt.iba.definition.StringDefinition;
import wt.iba.value.StringValue;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartUsageLink;
import wt.query.ArrayExpression;
import wt.query.ClassAttribute;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.AdHocStringVersioned;
import wt.vc.Iterated;
import wt.vc.OneOffVersioned;
import wt.vc.Versioned;
import wt.vc.config.*;
import wt.vc.sessioniteration.SessionEditedIteration;
import wt.vc.views.View;
import wt.vc.views.ViewManageable;
import wt.vc.views.ViewReference;
import wt.vc.wip.Workable;

import java.io.Serializable;
import java.util.*;

/***
 * 部件查询
 *
 * @author kwang
 *
 */
public class ChangePartQueryUtils implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 根据配置筛选父件的视图
     *
     * @param qs
     * @throws WTException
     * @return QuerySpec
     */
    public static QuerySpec appendView(QuerySpec qs, View vw, Integer partIndex) throws WTException {
        try {
            Set<Long> viewIdSet = new HashSet<Long>();
            if (vw != null) {
                viewIdSet.add(PersistenceHelper.getObjectIdentifier(vw).getId());
            }
            if (viewIdSet.size() > 0) {
                Long[] viewIds = new Long[viewIdSet.size()];
                viewIds = viewIdSet.toArray(viewIds);
                ArrayExpression viewArrayExpression = new ArrayExpression(viewIds);
                String viewKeyId = ViewManageable.VIEW + "." + ViewReference.KEY + "." + ObjectIdentifier.ID;
                ClassAttribute stateAttribute = new ClassAttribute(WTPart.class, viewKeyId);
                SearchCondition sc = new SearchCondition(stateAttribute, SearchCondition.IN, viewArrayExpression);
                if (qs.getConditionCount() > 0) {
                    qs.appendAnd();
                }
                qs.appendWhere(sc, new int[]{partIndex});
            }
            return qs;
        } catch (WTException e) {
            throw new WTException(e);
        }
    }

    /***
     * 批量查询部件最顶层父件
     *
     * @param childPartArray
     *            子件集合
     * @param topParentArray
     *            顶层父件集合
     * @throws WTException
     */
    public static void batchQueryTopParentParts(Collection<WTPart> childPartArray, Collection<WTPart> topParentArray) throws WTException {
        if (childPartArray == null || childPartArray.size() == 0 || topParentArray == null) {
            return;
        }
        // 过滤最新部件
        childPartArray = excludeNonLatestVersionsPart(childPartArray);
        if (childPartArray == null || childPartArray.size() == 0) {
            return;
        }
        topParentArray.addAll(childPartArray);
        // 批量查询上层父件
        Map<WTPart, WTPartUsageLink> parentMap = batchQueryParentParts(childPartArray);
        // 清空子件集合
        childPartArray = new HashSet<WTPart>();
        for (Map.Entry<WTPart, WTPartUsageLink> entryMap : parentMap.entrySet()) {
            childPartArray.add(entryMap.getKey());
            // 判断对象是否存在子件，如果存在则移除
            WTPart childPart = null;
            for (WTPart parentPart : topParentArray) {
                if (PersistenceHelper.isEquivalent(parentPart.getMaster(), entryMap.getValue().getUses())) {
                    childPart = parentPart;
                    break;
                }
            }
            if (childPart != null) {
                topParentArray.remove(childPart);
            }
        }
        batchQueryTopParentParts(childPartArray, topParentArray);
    }

    /***
     * 批量查询上层父件
     *
     * @param childPartArray
     *            子件集合
     * @return
     * @throws WTException
     */
    public static Map<WTPart, WTPartUsageLink> batchQueryParentParts(Collection<WTPart> childPartArray) throws WTException {
        Map<WTPart, WTPartUsageLink> parentMap = new HashMap<WTPart, WTPartUsageLink>();
        if (childPartArray == null || childPartArray.size() == 0) {
            return parentMap;
        }

        try {
            QuerySpec qs = new QuerySpec();
            qs.setAdvancedQueryEnabled(true);
            qs.setDescendantQuery(false);
            qs.setQueryLimit(-1);
            int linkIndex = qs.appendClassList(WTPartUsageLink.class, true);
            int partIndex = qs.appendClassList(WTPart.class, true);

            // 子件MasterID集合
            List<Long> masterIdArray = new ArrayList<>();
//			// 视图
//			View vw = null ;
            for (WTPart part : childPartArray) {
                masterIdArray.add(PersistenceHelper.getObjectIdentifier(part.getMaster()).getId());
//				if(vw == null){
//					vw = (View)part.getView().getObject() ;
//				}else{
//					if(!PersistenceHelper.isEquivalent(vw, (View)part.getView().getObject())){
//						throw new WTException("部件视图不一致!") ;
//					}
//				}
            }
//			// 添加视图条件
//			appendView(qs, vw, partIndex) ;
            // 添加部件ID与Link父件相等条件
//			qs.appendAnd();
            String roleAObjectRefKeyId = ObjectToObjectLink.ROLE_AOBJECT_REF + "." + ObjectReference.KEY + "." + ObjectIdentifier.ID;
            String partKeyId = WTPart.PERSIST_INFO + "." + PersistInfo.OBJECT_IDENTIFIER + "." + ObjectIdentifier.ID;
            SearchCondition sc = new SearchCondition(WTPartUsageLink.class, roleAObjectRefKeyId, WTPart.class, partKeyId);
            qs.appendWhere(sc, new int[]{linkIndex, partIndex});
            // 添加Link子件条件
            qs.appendAnd();
            String roleBObjectRefKeyId = ObjectToObjectLink.ROLE_BOBJECT_REF + "." + ObjectReference.KEY + "." + ObjectIdentifier.ID;
            Long[] longAy = new Long[masterIdArray.size()];
            masterIdArray.toArray(longAy);
            ArrayExpression roleBObjectExpression = new ArrayExpression(longAy);
            ClassAttribute roleBObjectAttribute = new ClassAttribute(WTPartUsageLink.class, roleBObjectRefKeyId);
            sc = new SearchCondition(roleBObjectAttribute, SearchCondition.IN, roleBObjectExpression);
            qs.appendWhere(sc, new int[]{linkIndex});

            QueryResult qr = PersistenceServerHelper.manager.query(qs);
            while (qr.hasMoreElements()) {
                Object[] objectArray = (Object[]) qr.nextElement();
                if (objectArray != null && objectArray.length > 0) {
                    parentMap.put((WTPart) objectArray[1], (WTPartUsageLink) objectArray[0]);
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return parentMap;
    }

    /***
     * 批量查询上层父件
     *
     * @param childPartArray
     *            子件集合
     * @return
     * @throws WTException
     */
    public static Map<WTPartUsageLink, WTPart> batchQueryFirstParents(Collection<WTPart> childPartArray) throws WTException {
        Map<WTPartUsageLink, WTPart> parentMap = new HashMap<>();
        if (childPartArray == null || childPartArray.size() == 0) {
            return parentMap;
        }

        try {
            QuerySpec qs = new QuerySpec();
            qs.setAdvancedQueryEnabled(true);
            qs.setDescendantQuery(false);
            qs.setQueryLimit(-1);
            int linkIndex = qs.appendClassList(WTPartUsageLink.class, true);
            int partIndex = qs.appendClassList(WTPart.class, true);

            // 子件MasterID集合
            List<Long> masterIdArray = new ArrayList<Long>();
            for (WTPart part : childPartArray) {
                masterIdArray.add(PersistenceHelper.getObjectIdentifier(part.getMaster()).getId());
            }
            String roleAObjectRefKeyId = ObjectToObjectLink.ROLE_AOBJECT_REF + "." + ObjectReference.KEY + "." + ObjectIdentifier.ID;
            String partKeyId = WTPart.PERSIST_INFO + "." + PersistInfo.OBJECT_IDENTIFIER + "." + ObjectIdentifier.ID;
            SearchCondition sc = new SearchCondition(WTPartUsageLink.class, roleAObjectRefKeyId, WTPart.class, partKeyId);
            qs.appendWhere(sc, new int[]{linkIndex, partIndex});
            // 添加Link子件条件
            qs.appendAnd();
            String roleBObjectRefKeyId = ObjectToObjectLink.ROLE_BOBJECT_REF + "." + ObjectReference.KEY + "." + ObjectIdentifier.ID;
            Long[] longAy = new Long[masterIdArray.size()];
            masterIdArray.toArray(longAy);
            ArrayExpression roleBObjectExpression = new ArrayExpression(longAy);
            ClassAttribute roleBObjectAttribute = new ClassAttribute(WTPartUsageLink.class, roleBObjectRefKeyId);
            sc = new SearchCondition(roleBObjectAttribute, SearchCondition.IN, roleBObjectExpression);
            qs.appendWhere(sc, new int[]{linkIndex});

            QueryResult qr = PersistenceServerHelper.manager.query(qs);
            while (qr.hasMoreElements()) {
                Object[] objectArray = (Object[]) qr.nextElement();
                if (objectArray != null && objectArray.length > 0) {
                    parentMap.put((WTPartUsageLink) objectArray[0], (WTPart) objectArray[1]);
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return parentMap;
    }

    /***
     * 批量查询部件IBA属性值
     *
     * @param partArray
     *            部件集合
     * @param attributeName
     *            IBA属性内部名称
     * @return
     * @throws WTPropertyVetoException
     * @throws WTException
     */
    public static Map<WTPart, String> batchQueryIBAValues(Collection<WTPart> partArray, String attributeName) throws WTException {
        Map<WTPart, String> ibaValueMap = new HashMap<WTPart, String>();
        if (partArray == null || partArray.size() == 0 || PIStringUtils.isNull(attributeName)) {
            return ibaValueMap;
        }

        try {
            QuerySpec qs = new QuerySpec();
            qs.setAdvancedQueryEnabled(true);
            qs.setDescendantQuery(false);
            int valueIndex = qs.appendClassList(StringValue.class, true);
            int definitionIndex = qs.appendClassList(StringDefinition.class, false);
            int partIndex = qs.appendClassList(WTPart.class, true);

            List<Long> longArray = new ArrayList<Long>();
            for (WTPart part : partArray) {
                longArray.add(PersistenceHelper.getObjectIdentifier(part).getId());
            }
            // 添加部件ID条件
            String partKeyId = WTPart.PERSIST_INFO + "." + PersistInfo.OBJECT_IDENTIFIER + "." + ObjectIdentifier.ID;
            Long[] longAy = new Long[longArray.size()];
            longArray.toArray(longAy);
            ArrayExpression ida2a2ArrayExpression = new ArrayExpression(longAy);
            ClassAttribute ida2a2Attribute = new ClassAttribute(WTPart.class, partKeyId);
            SearchCondition sc = new SearchCondition(ida2a2Attribute, SearchCondition.IN, ida2a2ArrayExpression);
            qs.appendWhere(sc, new int[]{partIndex});

            qs.appendAnd();
            String ibaKeyId = StringValue.IBAHOLDER_REFERENCE + "." + ObjectReference.KEY + "." + ObjectIdentifier.ID;
            sc = new SearchCondition(StringValue.class, ibaKeyId, WTPart.class, partKeyId);
            qs.appendWhere(sc, new int[]{valueIndex, partIndex});

            qs.appendAnd();
            String defKeyId = StringValue.DEFINITION_REFERENCE + "." + ObjectReference.KEY + "." + ObjectIdentifier.ID;
            String defIda2a2 = StringDefinition.PERSIST_INFO + "." + PersistInfo.OBJECT_IDENTIFIER + "." + ObjectIdentifier.ID;
            sc = new SearchCondition(StringValue.class, defKeyId, StringDefinition.class, defIda2a2);
            qs.appendWhere(sc, new int[]{valueIndex, definitionIndex});

            qs.appendAnd();
            sc = new SearchCondition(StringDefinition.class, StringDefinition.NAME, SearchCondition.EQUAL, attributeName);
            qs.appendWhere(sc, new int[]{definitionIndex});

            System.out.println("batchueryIBAValues() ... qs : " + qs.toString());
            QueryResult qr = PersistenceServerHelper.manager.query(qs);
            if (qr != null) {
                while (qr.hasMoreElements()) {
                    Object[] nextElement = (Object[]) qr.nextElement();
                    if (nextElement != null && nextElement.length > 0) {
                        ibaValueMap.put((WTPart) nextElement[1], ((StringValue) nextElement[0]).getValue());
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return ibaValueMap;
    }

    /***
     * 排除非最新版本部件
     *
     * @param parentCollection
     *            需要排除的部件集合
     * @return
     * @throws WTException
     */
    public static Collection<WTPart> excludeNonLatestVersionsPart(Collection<WTPart> parentCollection) throws WTException {
        Collection<WTPart> filterCollection = new HashSet<WTPart>();
        if (parentCollection == null || parentCollection.size() == 0) {
            return filterCollection;
        }

        QuerySpec qs = new QuerySpec();
        qs.setAdvancedQueryEnabled(true);
        qs.setQueryLimit(-1);
        int index = qs.appendClassList(WTPart.class, true);
        // 视图
//		View vw = null ;
        List<String> numberArray = new ArrayList<String>();
        for (WTPart part : parentCollection) {
//			if(vw == null){
//				vw = (View)part.getView().getObject() ;
//			}else{
//				if(!PersistenceHelper.isEquivalent(vw, (View)part.getView().getObject())){
//					throw new WTException("视图不同无法查询") ;
//				}
//			}
            numberArray.add(part.getNumber());
        }
        // 添加编码条件
        String partNumberKey = WTPart.NUMBER;
        String[] numberAy = new String[numberArray.size()];
        numberArray.toArray(numberAy);
        ArrayExpression numberArrayExpression = new ArrayExpression(numberAy);
        ClassAttribute numberAttribute = new ClassAttribute(WTPart.class, partNumberKey);
        SearchCondition sc = new SearchCondition(numberAttribute, SearchCondition.IN, numberArrayExpression);
        qs.appendWhere(sc, new int[]{index});
        // 添加视图条件
//		appendView(qs, vw, index) ;
        // 排序
        qs = appendOrderBy(qs);
        QueryResult qr = PersistenceServerHelper.manager.query(qs);
        Map<String, WTPart> lastParentParts = new HashMap<String, WTPart>();
        while (qr.hasMoreElements()) {
            Object obj = qr.nextElement();
            if (obj instanceof Object[]) {
                Object[] objArray = (Object[]) obj;
                WTPart part = (WTPart) objArray[0];
                if (!lastParentParts.containsKey(part.getNumber() + part.getViewName())) {
                    lastParentParts.put(part.getNumber() + part.getViewName(), part);
                }
            }
        }
        for (WTPart part : parentCollection) {
            if (lastParentParts.containsValue(part)) {
                filterCollection.add(part);
            }
        }

        return filterCollection;
    }

    /**
     * 默认排序规则
     *
     * @param qs
     * @throws WTException
     * @return QuerySpec
     */
    private static QuerySpec appendOrderBy(QuerySpec qs) throws WTException {
        try {
            Class<?> clazz = WTPart.class;
            // 按大版本降序排列
            if (Versioned.class.isAssignableFrom(clazz)) {
                (new VersionedOrderByPrimitive()).appendOrderBy(qs, 0, true);
            }
            // 按一次性版本降序排列
            if (OneOffVersioned.class.isAssignableFrom(clazz)) {
                (new OneOffVersionedOrderByPrimitive()).appendOrderBy(qs, 0, true);
            }
            //
            if (AdHocStringVersioned.class.isAssignableFrom(clazz)) {
                (new AdHocStringVersionedOrderByPrimitive()).appendOrderBy(qs, 0, true);
            }
            // 按小版本降序排列
            if (Iterated.class.isAssignableFrom(clazz)) {
                (new IteratedOrderByPrimitive()).appendOrderBy(qs, 0, true);
            }
            // 按检出标识符降序排列
            if (Workable.class.isAssignableFrom(clazz)) {
                (new WorkableOrderByPrimitive()).appendOrderBy(qs, 0, true);
            }
            if (SessionEditedIteration.class.isAssignableFrom(clazz)) {
                (new SessionEditedIterationOrderByPrimitive()).appendOrderBy(qs, 0, true);
            }

            return qs;
        } catch (WTException e) {
            throw new WTException(e);
        }
    }

    /***
     * 查询查询部件下完整子件信息
     *
     * @param parentPart
     *            父件
     * @return
     * @throws WTException
     */
    public static Collection<WTPart> getPartMultiwallStructure(WTPart parentPart) throws WTException {
        Collection<WTPart> childArray = new HashSet<WTPart>();
        if (parentPart == null) {
            return childArray;
        }

        try {
            // 构建父件集合
            WTList parentList = new WTArrayList();
            parentList.add(parentPart);
            // 执行查询动作
            getPartsMultiwallStructure(parentList, new LatestConfigSpec(), childArray);

            return childArray;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }
    }

    /***
     * 批量查询部件下完整子件信息
     *
     * @param parentList
     *            父件集合
     * @param confSpec
     *            查询条件
     * @param childArray
     *            子件集合
     * @throws WTException
     */
    public static void getPartsMultiwallStructure(WTList parentList, ConfigSpec confSpec, Collection<WTPart> childArray) throws WTException {
        if (parentList == null || parentList.size() == 0 || childArray == null) {
            return;
        }
        try {
            //批量获取第一次结构信息
            QueryResult qr = getPartsFirstStructure(parentList, confSpec);
            if (qr != null) {
                WTList childPartList = new WTArrayList();
                while (qr.hasMoreElements()) {
                    Persistable[] persistables = (Persistable[]) qr.nextElement();
                    // 子件对象
                    Object object = persistables[1];
                    if (object instanceof WTPart) {
                        childPartList.add(object);
                        // 数据收集
                        childArray.add((WTPart) object);
                    }
                }
                //递归循环
                getPartsMultiwallStructure(childPartList, confSpec, childArray);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }
    }

    /***
     * 批量查询部件第一层结构信息
     *
     * @param parentList
     *            父件集合
     * @param confSpec
     *            查询条件
     * @return
     * @throws WTException
     */
    public static QueryResult getPartsFirstStructure(WTList parentList, ConfigSpec confSpec) throws WTException {
        QueryResult qr = null;
        if (parentList == null || parentList.size() == 0) {
            return qr;
        }

        try {
            Persistable[][][] persistableArray = WTPartHelper.service.getUsesWTParts(parentList, confSpec);
            Vector<Persistable[]> vector = new Vector<Persistable[]>();
            for (int i = 0; i < persistableArray.length; ++i) {
                Persistable[][] persistables = persistableArray[i];
                if (persistables != null) {
                    for (int j = 0; j < persistables.length; ++j) {
                        vector.add(persistables[j]);
                    }
                }
            }
            qr = new QueryResult(new ObjectVector(vector));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return qr;
    }
}
