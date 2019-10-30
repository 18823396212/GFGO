package ext.appo.change.util;

import ext.appo.change.constants.ModifyConstants;
import ext.appo.ecn.constants.ChangeConstants;
import ext.lang.PIStringUtils;
import ext.pi.core.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import wt.access.AccessPermission;
import wt.change2.*;
import wt.configurablelink.ConfigurableDescribeLink;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.*;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTSet;
import wt.folder.FolderHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.part.WTPart;
import wt.part.WTPartUsageLink;
import wt.pds.StatementSpec;
import wt.project.Role;
import wt.query.ArrayExpression;
import wt.query.ClassAttribute;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.team.Team;
import wt.team.TeamHelper;
import wt.team.TeamManaged;
import wt.type.ClientTypedUtility;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;
import wt.vc.AdHocStringVersioned;
import wt.vc.Iterated;
import wt.vc.OneOffVersioned;
import wt.vc.Versioned;
import wt.vc.config.*;
import wt.vc.sessioniteration.SessionEditedIteration;
import wt.vc.wip.Workable;

import java.text.SimpleDateFormat;
import java.util.*;

public class ModifyUtils implements ChangeConstants {

    private static final Logger LOGGER = LogR.getLogger(ModifyUtils.class.getName());

    /**
     * 获取属性值
     * @param persistable
     * @param attribute
     * @return
     * @throws WTException
     */
    public static String getValue(Persistable persistable, String attribute) throws WTException {
        Object object = PIAttributeHelper.service.getValue(persistable, attribute);
        return object == null ? "" : object.toString();
    }

    /***
     * 判断物料是否为指定分类
     * @param part 部件
     * @param nodeName 分类
     * @return
     * @throws WTException
     */
    public static Boolean specificNode(WTPart part, String nodeName) throws WTException {
        Boolean isSpecificNode = false;
        if (part == null || PIStringUtils.isNull(nodeName)) {
            return isSpecificNode;
        }
        // TODO 分类为‘PCB’及子件时必须收集父件
        Collection<String> classifyNodeArray = PIClassificationHelper.service.getClassifyNodes(part);
        // 是否PCB
        for (String classifyNode : classifyNodeArray) {
            // 分类完整路径
            String classifyNodePath = PIClassificationHelper.service.getNodeLocalizedHierarchy(classifyNode, SessionHelper.getLocale());
            // 判断是否为指定分类部件
            List<String> clfArray = new ArrayList<>();
            if (classifyNodePath.contains(USER_KEYWORD4)) {
                clfArray = Arrays.asList(classifyNodePath.split(USER_KEYWORD4));
            } else {
                clfArray.add(classifyNodePath);
            }
            if (clfArray.contains(nodeName)) {
                isSpecificNode = true;
                break;
            }
        }
        return isSpecificNode;
    }

    /***
     * 获取对象编码
     * @param persistable
     * @return
     */
    public static String getNumber(Persistable persistable) {
        if (persistable instanceof ObjectReference) {
            persistable = ((ObjectReference) persistable).getObject();
        }

        String number = "";

        if (persistable instanceof WTPart) {
            number = ((WTPart) persistable).getNumber();
        } else if (persistable instanceof EPMDocument) {
            number = ((EPMDocument) persistable).getNumber();
        } else if (persistable instanceof WTDocument) {
            number = ((WTDocument) persistable).getNumber();
        } else if (persistable instanceof WTChangeRequest2) {
            number = ((WTChangeRequest2) persistable).getNumber();
        } else if (persistable instanceof WTChangeOrder2) {
            number = ((WTChangeOrder2) persistable).getNumber();
        } else if (persistable instanceof WTChangeActivity2) {
            number = ((WTChangeActivity2) persistable).getNumber();
        }

        return number;
    }

    /***
     * 获取对象编码
     * @param persistable
     * @return
     */
    public static String geBranchId(Persistable persistable) {
        if (persistable instanceof ObjectReference) {
            persistable = ((ObjectReference) persistable).getObject();
        }

        String branchId = "";
        if (persistable instanceof WTPart) {
            branchId = String.valueOf(((WTPart) persistable).getBranchIdentifier());
        } else if (persistable instanceof EPMDocument) {
            branchId = String.valueOf(((EPMDocument) persistable).getBranchIdentifier());
        } else if (persistable instanceof WTDocument) {
            branchId = String.valueOf(((WTDocument) persistable).getBranchIdentifier());
        } else if (persistable instanceof WTChangeRequest2) {
            branchId = String.valueOf(((WTChangeRequest2) persistable).getBranchIdentifier());
        } else if (persistable instanceof WTChangeOrder2) {
            branchId = String.valueOf(((WTChangeOrder2) persistable).getBranchIdentifier());
        } else if (persistable instanceof WTChangeActivity2) {
            branchId = String.valueOf(((WTChangeActivity2) persistable).getBranchIdentifier());
        }
        return branchId;
    }

    /***
     * 批量查询上层父件
     * @param collection 子件集合
     * @throws WTException
     */
    public static Map<WTPartUsageLink, WTPart> batchQueryFirstParents(Collection<WTPart> collection) throws WTException {
        Map<WTPartUsageLink, WTPart> parentMap = new HashMap<>();
        if (collection == null || collection.size() == 0) return parentMap;

        try {
            QuerySpec qs = new QuerySpec();
            qs.setAdvancedQueryEnabled(true);
            qs.setDescendantQuery(false);
            qs.setQueryLimit(-1);
            int linkIndex = qs.appendClassList(WTPartUsageLink.class, true);
            int partIndex = qs.appendClassList(WTPart.class, true);

            // 子件MasterID集合
            List<Long> masterIdArray = new ArrayList<>();
            for (WTPart part : collection) {
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
            throw new WTException(e.getStackTrace());
        }

        return parentMap;
    }

    /***
     * 排除非最新版本部件
     * @param parentCollection
     *            需要排除的部件集合
     * @return
     * @throws WTException
     */
    public static Collection<WTPart> excludeNonLatestVersionsPart(Collection<WTPart> parentCollection) throws WTException {
        Collection<WTPart> filterCollection = new HashSet<>();
        if (parentCollection == null || parentCollection.size() == 0) {
            return filterCollection;
        }

        QuerySpec qs = new QuerySpec();
        qs.setAdvancedQueryEnabled(true);
        qs.setQueryLimit(-1);
        int index = qs.appendClassList(WTPart.class, true);

        List<String> numberArray = new ArrayList<>();
        for (WTPart part : parentCollection) {
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

        qs = appendOrderBy(qs);
        QueryResult qr = PersistenceServerHelper.manager.query(qs);
        Map<String, WTPart> lastParentParts = new HashMap<>();
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
     * @param qs
     * @return	QuerySpec
     * @throws WTException
     */
    private static QuerySpec appendOrderBy(QuerySpec qs) throws WTException {
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
    }

    /***
     * 判断对象状态是否符合要求
     * @param lifecycleManaged
     * @param state
     * @return
     */
    public static Boolean checkState(LifeCycleManaged lifecycleManaged, String state) {
        if (lifecycleManaged == null || PIStringUtils.isNull(state)) {
            return false;
        }
        return lifecycleManaged.getLifeCycleState().toString().equalsIgnoreCase(state);
    }

    /**
     * 判断是否标准件
     * @param datalist
     * @param part
     * @return
     */
    public static Boolean isStandardPart(List<Map> datalist, WTPart part) {
        Boolean isStandardPart = false;
        try {
            String value = ModifyUtils.getValue(part, ModifyConstants.ATTRIBUTE_1);//获取分类内部值
            LOGGER.info(">>>>>>>>>>value:" + value);
            String nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(value);//获取分类全路径
            if (datalist != null && datalist.size() > 0) {
                for (Map map2 : datalist) {
                    String type = (String) map2.get("type");
                    LOGGER.info(">>>>>>>>>>type: " + type + " >>>>>>>>>>nodeHierarchy:" + nodeHierarchy);
                    if (type.length() > 0 && nodeHierarchy.contains(type)) {
                        isStandardPart = true;
                        break;
                    }
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return isStandardPart;
    }

    /***
     * 创建ChangeActivity2对象
     * @param changeNotice
     *            更改通告对象
     *
     * @param name
     *            eca名称
     *
     * @param number
     *            eca编码
     *
     * @param description
     *            eca说明
     *
     * @param softType
     *            eca类型
     *
     * @param assigneeName
     *            责任人名称
     * @return
     * @throws WTException
     */
    public static WTChangeActivity2 createChangeTask(WTChangeOrder2 changeNotice, String name, String number, String description, String softType, String assigneeName) throws WTException {
        WTChangeActivity2 changeActivity = null;

        if (changeNotice == null || StringUtils.isBlank(name)) {
            return changeActivity;
        }

        try {
            changeActivity = WTChangeActivity2.newWTChangeActivity2(name);
            if (StringUtils.isNotBlank(number)) {
                changeActivity.setNumber(number);
            }
            // 设置说明
            if (StringUtils.isNotBlank(description)) {
                changeActivity.setDescription(description);
            }
            // 设置类型
            if (StringUtils.isNotBlank(softType)) {
                TypeDefinitionReference typeDefinitionReference = TypedUtilityServiceHelper.service.getTypeDefinitionReference(softType);
                if (typeDefinitionReference == null) {
                    typeDefinitionReference = TypeDefinitionReference.newTypeDefinitionReference();
                }
                changeActivity.setTypeDefinitionReference(typeDefinitionReference);
            }
            // 设置所在库
            changeActivity.setContainerReference(changeNotice.getContainerReference());
            // 设置文件夹
            FolderHelper.assignLocation(changeActivity, changeNotice.getFolderingInfo().getFolder());
            // 保存对象
            changeActivity = (WTChangeActivity2) ChangeHelper2.service.saveChangeActivity(changeNotice, changeActivity);
            // 工作负责人
            setChangeActivity2Assignee(changeActivity, assigneeName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }
        LOGGER.info(">>>>>>>>>>changeActivity:" + changeActivity);
        return changeActivity;
    }

    /***
     * 设置更改任务的工作负责人
     * @param changeActivity
     * @param assigneeName
     *            工作负责人名称
     * @throws WTException
     */
    public static void setChangeActivity2Assignee(WTChangeActivity2 changeActivity, String assigneeName) throws WTException {
        try {
            Collection<WTPrincipal> membersArray = new HashSet<>();
            if (PIStringUtils.isNotNull(assigneeName)) {
                membersArray.addAll(findWTUsers(assigneeName));
            }
            setTeamMembers(changeActivity, ROLE_ASSIGNEE, membersArray);
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getStackTrace());
        }
    }

    /***
     * 根据输入的参数查询系统对应的用户对象
     * @param parameter
     * @return
     * @throws WTException
     */
    public static Collection<WTPrincipal> findWTUsers(String parameter) throws WTException {
        Collection<WTPrincipal> userArray = new HashSet<>();
        if (PIStringUtils.isNull(parameter)) return userArray;

        try {
            if (parameter.contains(USER_KEYWORD)) {
                String[] parameters = parameter.split(USER_KEYWORD);
                for (String str : parameters) {
                    if (PIStringUtils.isNull(str)) {
                        continue;
                    }
                    WTSet wtSet = PIPrincipalHelper.service.findWTUser(str);
                    for (Object object : wtSet) {
                        if (object instanceof ObjectReference) {
                            object = ((ObjectReference) object).getObject();
                        }
                        userArray.add((WTPrincipal) object);
                    }
                }
            } else {
                WTSet wtSet = PIPrincipalHelper.service.findWTUser(parameter);
                for (Object object : wtSet) {
                    if (object instanceof ObjectReference) {
                        object = ((ObjectReference) object).getObject();
                    }
                    userArray.add((WTPrincipal) object);
                }
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }

        return userArray;
    }

    /***
     * 用户批量添加到对象团队角色中
     * @param teamManaged
     *            对象容器
     * @param roleName
     *            角色名称
     * @param membersArray
     *            用户集合
     * @throws WTException
     */
    @SuppressWarnings({"rawtypes"})
    public static void setTeamMembers(TeamManaged teamManaged, String roleName, Collection<WTPrincipal> membersArray) throws WTException {
        if (teamManaged == null || membersArray == null || membersArray.size() == 0 || PIStringUtils.isNull(roleName)) {
            return;
        }

        // 获取团队
        Team team = null;
        try {
            team = TeamHelper.service.getTeam(teamManaged);
            // 授予权限
            PIAccessHelper.service.grantPersistablePermissions(team, WTPrincipalReference.newWTPrincipalReference(SessionHelper.manager.getPrincipal()), AccessPermission.MODIFY, true);
            // 获取团队中所有角色
            Vector<?> vector = team.getRoles();
            // 需要添加的角色
            Role role = null;
            for (Object object : vector) {
                if (object instanceof ObjectReference) {
                    object = ((ObjectReference) object).getObject();
                }
                role = (Role) object;
                if (role.toString().equalsIgnoreCase(roleName)) {
                    break;
                } else {
                    role = null;
                }
            }
            if (role != null) {
                // 清空角色
                cleanRoleByTeam(team, role);
                // 重新设置用户
                HashMap principalMap = new HashMap();
                principalMap.put(role, membersArray);
                team.addPrincipals(principalMap);
                team = (Team) PersistenceHelper.manager.refresh(team);
                team = (Team) PersistenceHelper.manager.save(team);
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        } finally {
            PIAccessHelper.service.removePersistablePermissions(team, WTPrincipalReference.newWTPrincipalReference(SessionHelper.manager.getPrincipal()), AccessPermission.MODIFY, true);
        }
    }

    /***
     * 清空团队指定角色中的所有用户
     * @param team
     * @param role
     * @throws WTException
     */
    public static void cleanRoleByTeam(Team team, Role role) throws WTException {
        if (team == null || role == null) return;

        try {
            Enumeration<?> rolePrincipal = team.getPrincipalTarget(role);
            if (rolePrincipal != null) {
                while (rolePrincipal.hasMoreElements()) {
                    Object object = rolePrincipal.nextElement();
                    if (object instanceof ObjectReference) {
                        object = ((ObjectReference) object).getObject();
                    }
                    team.deletePrincipalTarget(role, (WTPrincipal) object);
                }
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
    }

    /**
     * 设置ChangeActivity2对象名称属性
     * @param eca
     * @param newName
     * @throws WTException
     */
    public static void setChangeActivity2Name(WTChangeActivity2 eca, String newName) throws WTException {
        if (eca == null || PIStringUtils.isNull(newName)) return;
        // 获取当前用户
        WTPrincipal principal = SessionHelper.manager.getPrincipal();
        try {
            // 赋予修改编码权限
            PIAccessHelper.service.grantPersistablePermissions(eca, WTPrincipalReference.newWTPrincipalReference(principal), AccessPermission.MODIFY_IDENTITY, true);

            Identified identified = (Identified) eca.getMaster();
            WTChangeActivity2MasterIdentity masteridentity = (WTChangeActivity2MasterIdentity) identified.getIdentificationObject();
            masteridentity.setName(newName);
            // 更新名称
            identified = IdentityHelper.service.changeIdentity(identified, masteridentity);
            PersistenceServerHelper.manager.update(identified);
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        } finally {
            PIAccessHelper.service.removePersistablePermissions(eca, WTPrincipalReference.newWTPrincipalReference(principal), AccessPermission.MODIFY_IDENTITY, true);
        }
    }

    /***
     * 更新ECA对象上的‘需要日期’属性
     * @param eca
     *            ChangeActivity2对象
     * @param needDate
     *            格式：yyyy-MM-dd 或者 yyyy/MM/dd
     * @return
     * @throws WTException
     */
    public static WTChangeActivity2 updateNeedDate(WTChangeActivity2 eca, String needDate) throws WTException {
        if (eca == null || PIStringUtils.isNull(needDate)) return eca;

        try {
            if (needDate.contains(" ")) needDate = needDate.substring(0, needDate.indexOf(" ")).trim();
            if (needDate.contains("-"))
                needDate = (new SimpleDateFormat(ChangeConstants.SIMPLE_DATE_FORMAT_02)).format((new SimpleDateFormat(ChangeConstants.SIMPLE_DATE_FORMAT_03)).parse(needDate));
            eca = (WTChangeActivity2) PIAttributeHelper.service.forceUpdateSoftAttribute(eca, COMPLETION_TIME, needDate);
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }

        return eca;
    }

    /***
     * 添加受影响对象
     * @param eca
     *            更改任务
     * @param collection
     *            添加对象集合
     * @throws WTException
     */
    public static void addAffectedActivityData(WTChangeActivity2 eca, Collection<Changeable2> collection) throws WTException {
        if (collection == null || eca == null || collection.size() == 0) return;
        try {
            // 查询ECA中所有受影响对象
            QueryResult result = ChangeHelper2.service.getChangeablesBefore(eca);
            if (result != null) {
                Vector<?> vector = result.getObjectVectorIfc().getVector();
                LOGGER.info(">>>>>>>>>>addAffectedActivityData.vector:" + vector);

                Map<String, Changeable2> tempMap = new HashMap<>();
                Vector<Changeable2> tempVector = new Vector<>();
                for (Changeable2 changeable2 : collection) {
                    if (!tempVector.contains(changeable2)) {
                        tempMap.put(getNumber(changeable2), changeable2);
                        tempVector.add(changeable2);
                    }
                }
                LOGGER.info(">>>>>>>>>>addAffectedActivityData.tempMap:" + tempMap);
                LOGGER.info(">>>>>>>>>>addAffectedActivityData.tempVector1:" + tempVector);

                for (Object object : vector) {
                    if (object instanceof ObjectReference) {
                        object = ((ObjectReference) object).getObject();
                    }
                    Changeable2 changeable2 = (Changeable2) object;
                    if (tempMap.containsKey(getNumber(changeable2))) {
                        tempVector.remove(tempMap.get(getNumber(changeable2)));
                    }
                }

                LOGGER.info(">>>>>>>>>>addAffectedActivityData.tempVector2:" + tempVector);
                if (tempVector.size() > 0) {
                    ChangeHelper2.service.storeAssociations(AffectedActivityData.class, eca, tempVector);
                }
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
    }

    /***
     * 根据ECA及受影响对象获取AffectedActivityData对象
     * @param changeActivityIfc
     *            更改任务
     * @param changeable2
     *            受影响对象
     * @return
     * @throws WTException
     */
    public static AffectedActivityData getAffectedActivity(ChangeActivityIfc changeActivityIfc, Changeable2 changeable2) throws WTException {
        if (changeActivityIfc == null || changeable2 == null) return null;

        try {
            QuerySpec qs = new QuerySpec(AffectedActivityData.class);
            ReferenceFactory rf = new ReferenceFactory();
            qs.appendOpenParen();
            String ecaOID = rf.getReferenceString(changeActivityIfc);
            qs.appendWhere(new SearchCondition(AffectedActivityData.class, "roleAObjectRef.key.branchId", SearchCondition.EQUAL, Long.parseLong(ecaOID.substring(ecaOID.lastIndexOf(":") + 1, ecaOID.length()))), new int[]{0});
            qs.appendAnd();
            String ptOID = PersistenceHelper.getObjectIdentifier(changeable2).toString();
            qs.appendWhere(new SearchCondition(AffectedActivityData.class, "roleBObjectRef.key.id", SearchCondition.EQUAL, Long.parseLong(ptOID.substring(ptOID.lastIndexOf(":") + 1, ptOID.length()))), new int[]{0});
            qs.appendCloseParen();
            LOGGER.info(">>>>>>>>>>getAffectedActivity.qs:" + qs.toString());

            QueryResult qr = PersistenceServerHelper.manager.query(qs);
            if (qr.hasMoreElements()) {
                return (AffectedActivityData) qr.nextElement();
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }

        return null;
    }

    /**
     * 修订
     * @param vector
     * @param reviseMap
     * @return
     * @throws WTException
     */
    public static WTCollection revise(Vector<Changeable2> vector, Map<String, Changeable2> reviseMap) throws WTException {
        WTCollection collection = new WTArrayList();
        if (vector == null || vector.size() < 1) return collection;

        LOGGER.info(">>>>>>>>>>revise.reviseMap:" + reviseMap);
        if (reviseMap != null) {
            for (Changeable2 changeable2 : vector) {
                LOGGER.info(">>>>>>>>>>revise.changeable2:" + changeable2.getClass());
                if (changeable2 instanceof ObjectReference) {
                    ObjectReference objectReference = (ObjectReference) changeable2;
                    changeable2 = (Changeable2) objectReference.getObject();
                }

                String number = getNumber(changeable2);
                LOGGER.info(">>>>>>>>>>revise.number:" + number);
                if (reviseMap.containsKey(number)) {
                    collection.add(reviseMap.get(number));
                } else {
                    Versioned versioned = PICoreHelper.service.revise(changeable2);
                    LOGGER.info(">>>>>>>>>>revise.versioned:" + versioned);
                    if (versioned instanceof Changeable2) {
                        collection.add(versioned);
                        reviseMap.put(number, (Changeable2) versioned);
                    }
                }
            }
        } else {
            WTCollection list = new WTArrayList();
            for (Changeable2 changeable2 : vector) {
                LOGGER.info(">>>>>>>>>>revise.changeable2:" + changeable2.getClass());
                if (changeable2 instanceof ObjectReference) {
                    ObjectReference objectReference = (ObjectReference) changeable2;
                    changeable2 = (Changeable2) objectReference.getObject();
                }
                list.add(changeable2);
            }
            collection.addAll(PICoreHelper.service.revise(list, false));
        }
        return collection;
    }

    /***
     * 添加对象到ECA产生对象
     * @param eca
     * @param collection
     * @throws WTException
     */
    public static void AddChangeRecord2(WTChangeActivity2 eca, WTCollection collection) throws WTException {
        if (collection == null || eca == null || collection.size() == 0) return;

        // 查询ECA中所有产生对象
        QueryResult result = ChangeHelper2.service.getChangeablesAfter(eca);
        LOGGER.info(">>>>>>>>>>AddChangeRecord2.result:" + result.size());
        if (result != null) {
            Vector<?> vector = result.getObjectVectorIfc().getVector();
            LOGGER.info(">>>>>>>>>>AddChangeRecord2.vector:" + vector);

            Map<String, Changeable2> tempMap = new HashMap<>();
            Vector<Changeable2> tempVector = new Vector<>();
            for (Object object : collection) {
                if (object instanceof ObjectReference) {
                    object = ((ObjectReference) object).getObject();
                }
                if (object instanceof Changeable2) {
                    Changeable2 changeable2 = (Changeable2) object;
                    if (!tempVector.contains(changeable2)) {
                        tempMap.put(getNumber(changeable2), changeable2);
                        tempVector.add(changeable2);
                    }
                }
            }
            LOGGER.info(">>>>>>>>>>AddChangeRecord2.tempMap:" + tempMap);

            for (Object object : vector) {
                if (object instanceof ObjectReference) {
                    object = ((ObjectReference) object).getObject();
                }
                Changeable2 changeable2 = (Changeable2) object;
                if (tempMap.containsKey(getNumber(changeable2))) {
                    tempVector.remove(tempMap.get(getNumber(changeable2)));
                }
            }
            LOGGER.info(">>>>>>>>>>AddChangeRecord2.tempVector:" + tempVector);

            if (tempVector.size() > 0) {
                ChangeHelper2.service.storeAssociations(ChangeRecord2.class, eca, tempVector);
            }
        }
    }

    /**
     * 查询对象(RoleA)关联的某一类型的ConfigurableDescribeLink
     * @param persistable
     * @param type
     * @return
     * @throws WTException
     */
    public static Map<Persistable, ConfigurableDescribeLink> getDescribedBy(Persistable persistable, String type) throws WTException {
        Map<Persistable, ConfigurableDescribeLink> linkMap = new HashMap<>();
        try {
            QuerySpec querySpec = new QuerySpec(ConfigurableDescribeLink.class);
            querySpec.setAdvancedQueryEnabled(true);

            SearchCondition sc = new SearchCondition(ConfigurableDescribeLink.class, "roleAObjectRef.key.id", SearchCondition.EQUAL, persistable.getPersistInfo().getObjectIdentifier().getId());
            querySpec.appendWhere(sc, new int[]{0});

            querySpec.appendAnd();
            TypeDefinitionReference tdr = ClientTypedUtility.getTypeDefinitionReference(type);
            sc = new SearchCondition(ConfigurableDescribeLink.class, "typeDefinitionReference.key.branchId", SearchCondition.EQUAL, tdr.getKey().getBranchId());
            querySpec.appendWhere(sc, new int[]{0});

            LOGGER.info(">>>>>>>>>>getRelationDocLink.querySpec:" + querySpec.toString());
            QueryResult result = PersistenceHelper.manager.find((StatementSpec) querySpec);
            LOGGER.info(">>>>>>>>>>getRelationDocLink.result:" + result.size());
            while (result.hasMoreElements()) {
                Object object = result.nextElement();
                if (object instanceof ConfigurableDescribeLink) {
                    ConfigurableDescribeLink link = (ConfigurableDescribeLink) object;
                    linkMap.put(link.getRoleBObject(), link);
                }
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
        return linkMap;
    }

    /**
     * 查询对象(RoleB)关联的某一类型的ConfigurableDescribeLink
     * @param persistable
     * @param type
     * @return
     * @throws WTException
     */
    public static Map<Persistable, ConfigurableDescribeLink> getDescribed(Persistable persistable, String type) throws WTException {
        Map<Persistable, ConfigurableDescribeLink> linkMap = new HashMap<>();
        try {
            QuerySpec querySpec = new QuerySpec(ConfigurableDescribeLink.class);
            querySpec.setAdvancedQueryEnabled(true);

            SearchCondition sc = new SearchCondition(ConfigurableDescribeLink.class, "roleBObjectRef.key.id", SearchCondition.EQUAL, persistable.getPersistInfo().getObjectIdentifier().getId());
            querySpec.appendWhere(sc, new int[]{0});

            querySpec.appendAnd();
            TypeDefinitionReference tdr = ClientTypedUtility.getTypeDefinitionReference(type);
            sc = new SearchCondition(ConfigurableDescribeLink.class, "typeDefinitionReference.key.branchId", SearchCondition.EQUAL, tdr.getKey().getBranchId());
            querySpec.appendWhere(sc, new int[]{0});

            LOGGER.info(">>>>>>>>>>getRelationDocLink.querySpec:" + querySpec.toString());
            QueryResult result = PersistenceHelper.manager.find((StatementSpec) querySpec);
            LOGGER.info(">>>>>>>>>>getRelationDocLink.result:" + result.size());
            while (result.hasMoreElements()) {
                Object object = result.nextElement();
                if (object instanceof ConfigurableDescribeLink) {
                    ConfigurableDescribeLink link = (ConfigurableDescribeLink) object;
                    linkMap.put(link.getRoleAObject(), link);
                }
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
        return linkMap;
    }

}
