package ext.appo.ecn.common.util;

import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import ext.appo.ecn.beans.ChangeTaskBean;
import ext.appo.ecn.constants.ChangeConstants;
import ext.lang.PIStringUtils;
import ext.pi.core.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import wt.access.AccessControlHelper;
import wt.access.AccessPermission;
import wt.change2.*;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.*;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTSet;
import wt.folder.FolderHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.log4j.LogR;
import wt.method.RemoteMethodServer;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.PartDocHelper;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.project.Role;
import wt.query.ArrayExpression;
import wt.query.ClassAttribute;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.team.Team;
import wt.team.TeamHelper;
import wt.team.TeamManaged;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChangeUtils implements ChangeConstants {

    private static final String CLASSNAME = ChangeUtils.class.getName();
    private static final Logger LOG = LogR.getLogger(CLASSNAME);

    /***
     * 创建ChangeActivity2对象
     *
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("changeActivity : " + changeActivity);
        }
        return changeActivity;
    }

    /***
     * 更新ChangeActivity2对象属性
     *
     * @param eca
     * @param changeTaskBean
     * @throws WTException
     */
    public static void updateChangeTask(WTChangeActivity2 eca, ChangeTaskBean changeTaskBean) throws WTException {
        if (eca == null) {
            return;
        }

        try {
            // 更改说明修改
            if (!changeTaskBean.getChangeDescribe().equals(eca.getDescription())) {
                eca.setDescription(changeTaskBean.getChangeDescribe());
            }
            // 名称修改
            if (!eca.getName().equals(changeTaskBean.getChangeTheme())) {
                setChangeActivity2Name(eca, changeTaskBean.getChangeTheme());
            }
            // 更改团队
            setChangeActivity2Assignee(eca, changeTaskBean.getResponsible());
            // 更新对象
            PersistenceServerHelper.manager.update(eca);
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }
    }

    /***
     * 设置更改任务的工作负责人
     *
     * @param changeActivity
     * @param assigneeName
     *            工作负责人名称
     * @throws WTException
     */
    public static void setChangeActivity2Assignee(WTChangeActivity2 changeActivity, String assigneeName) throws WTException {
        try {
            Collection<WTPrincipal> membersArray = new HashSet<WTPrincipal>();
            if (PIStringUtils.isNotNull(assigneeName)) {
                membersArray.addAll(findWTUsers(assigneeName));
            }
            setTeamMembers(changeActivity, ROLE_ASSIGNEE, membersArray);
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }
    }

    /***
     * 根据输入的参数查询系统对应的用户对象
     *
     * @param parameter
     * @return
     * @throws WTException
     */
    public static Collection<WTPrincipal> findWTUsers(String parameter) throws WTException {
        Collection<WTPrincipal> userArray = new HashSet<WTPrincipal>();
        if (PIStringUtils.isNull(parameter)) {
            return userArray;
        }

        try {
            StringBuilder sb = new StringBuilder();
            if (parameter.contains(USER_KEYWORD)) {
                String[] parameters = parameter.split(USER_KEYWORD);
                for (String str : parameters) {
                    if (PIStringUtils.isNull(str)) {
                        continue;
                    }
                    WTSet wtSet = PIPrincipalHelper.service.findWTUser(str);
                    if (wtSet == null || wtSet.size() == 0) {
                        sb.append(str + " ");
                        continue;
                    }
                    for (Object object : wtSet) {
                        if (object instanceof ObjectReference) {
                            object = ((ObjectReference) object).getObject();
                        }
                        userArray.add((WTPrincipal) object);
                    }
                }
            } else {
                WTSet wtSet = PIPrincipalHelper.service.findWTUser(parameter);
                if (wtSet == null || wtSet.size() == 0) {
                    sb.append(parameter + " ");
                }
                for (Object object : wtSet) {
                    if (object instanceof ObjectReference) {
                        object = ((ObjectReference) object).getObject();
                    }
                    userArray.add((WTPrincipal) object);
                }
            }
            // if(sb.length() > 0){
            // throw new WTException(sb.toString() + " 用户不存在!") ;
            // }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return userArray;
    }

    /**
     * 设置ChangeActivity2对象名称属性
     *
     * @param eca
     * @param newName
     * @throws WTException
     */
    public static void setChangeActivity2Name(WTChangeActivity2 eca, String newName) throws WTException {
        if (eca == null || PIStringUtils.isNull(newName)) {
            return;
        }
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
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        } finally {
            PIAccessHelper.service.removePersistablePermissions(eca, WTPrincipalReference.newWTPrincipalReference(principal), AccessPermission.MODIFY_IDENTITY, true);
        }
    }

    /**
     * 设置ECN对象编码
     *
     * @param changeOrder2
     * @throws WTException
     */
    public static void setChangeOrder2Number(WTChangeOrder2 changeOrder2) throws WTException {
        if (changeOrder2 == null) {
            return;
        }
        // 获取当前用户
        WTPrincipal principal = SessionHelper.manager.getPrincipal();
        try {
            // 赋予修改编码权限
            PIAccessHelper.service.grantPersistablePermissions(changeOrder2, WTPrincipalReference.newWTPrincipalReference(principal), AccessPermission.MODIFY_IDENTITY, true);

            Identified identified = (Identified) changeOrder2.getMaster();
            WTChangeOrder2MasterIdentity masteridentity = (WTChangeOrder2MasterIdentity) identified.getIdentificationObject();
            masteridentity.setNumber("ECN" + changeOrder2.getNumber());
            // 更新名称
            identified = IdentityHelper.service.changeIdentity(identified, masteridentity);
            PersistenceServerHelper.manager.update(identified);
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        } finally {
            PIAccessHelper.service.removePersistablePermissions(changeOrder2, WTPrincipalReference.newWTPrincipalReference(principal), AccessPermission.MODIFY_IDENTITY, true);
        }
    }

    /***
     * 根据输入的JSONObject数据集批量查询AffectedActivityData对象
     *
     * @param datasJSON
     *            key:ECA对象OID value:更改对象OID
     * @return
     * @throws WTException
     */
    @SuppressWarnings("unchecked")
    public static Collection<AffectedActivityData> getAffectedActivityDatas(JSONObject datasJSON) throws WTException {
        Collection<AffectedActivityData> datasArray = new ArrayList<AffectedActivityData>();
        if (datasJSON == null || datasJSON.length() == 0) {
            return datasArray;
        }

        try {
            QuerySpec qs = new QuerySpec(AffectedActivityData.class);
            Iterator<String> keyIterator = datasJSON.keys();
            while (keyIterator.hasNext()) {
                if (qs.getConditionCount() > 0) {
                    qs.appendOr();
                }
                qs.appendOpenParen();
                // 更改任务OID
                String ecaOID = keyIterator.next();
                qs.appendWhere(new SearchCondition(AffectedActivityData.class, "roleAObjectRef.key.branchId", SearchCondition.EQUAL, Long.parseLong(ecaOID.substring(ecaOID.lastIndexOf(":") + 1, ecaOID.length()))), new int[]{0});
                qs.appendAnd();
                String ptOID = datasJSON.getString(ecaOID);
                qs.appendWhere(new SearchCondition(AffectedActivityData.class, "roleBObjectRef.key.id", SearchCondition.EQUAL, Long.parseLong(ptOID.substring(ptOID.lastIndexOf(":") + 1, ptOID.length()))), new int[]{0});
                qs.appendCloseParen();
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("getAffectedActivityDatas() ... QuerySpec : " + qs.toString());
            }
            QueryResult qr = PersistenceServerHelper.manager.query(qs);
            while (qr.hasMoreElements()) {
                datasArray.add((AffectedActivityData) qr.nextElement());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return datasArray;
    }

    /***
     * 根据输入的数据集批量查询AffectedActivityData对象
     *
     * 注：可以过滤ChangeActivityIfc状态
     *
     * @param datasMap
     * @param forbidState
     *            限制的状态
     * @return
     * @throws WTException
     */
    public static Collection<AffectedActivityData> getAffectedActivityDatas(Map<ChangeActivityIfc, Collection<Changeable2>> datasMap, String forbidState) throws WTException {
        Collection<AffectedActivityData> datasArray = new HashSet<AffectedActivityData>();
        if (datasMap == null || datasMap.size() == 0) {
            return datasArray;
        }

        try {
            QuerySpec qs = new QuerySpec(AffectedActivityData.class);
            ReferenceFactory rf = new ReferenceFactory();
            for (Map.Entry<ChangeActivityIfc, Collection<Changeable2>> entryMap : datasMap.entrySet()) {
                ChangeActivityIfc changeActivityIfc = entryMap.getKey();
                if (ChangeUtils.checkState((WTChangeActivity2) changeActivityIfc, forbidState)) {
                    continue;
                }
                for (Changeable2 changeable2 : entryMap.getValue()) {
                    if (qs.getConditionCount() > 0) {
                        qs.appendOr();
                    }
                    qs.appendOpenParen();
                    String ecaOID = rf.getReferenceString(entryMap.getKey());
                    qs.appendWhere(new SearchCondition(AffectedActivityData.class, "roleAObjectRef.key.branchId", SearchCondition.EQUAL, Long.parseLong(ecaOID.substring(ecaOID.lastIndexOf(":") + 1, ecaOID.length()))), new int[]{0});
                    qs.appendAnd();
                    String ptOID = PersistenceHelper.getObjectIdentifier(changeable2).toString();
                    qs.appendWhere(new SearchCondition(AffectedActivityData.class, "roleBObjectRef.key.id", SearchCondition.EQUAL, Long.parseLong(ptOID.substring(ptOID.lastIndexOf(":") + 1, ptOID.length()))), new int[]{0});
                    qs.appendCloseParen();
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("getAffectedActivityDatas() ... QuerySpec : " + qs.toString());
            }
            if (qs.getConditionCount() == 0) {
                return datasArray;
            }
            QueryResult qr = PersistenceServerHelper.manager.query(qs);
            while (qr.hasMoreElements()) {
                datasArray.add((AffectedActivityData) qr.nextElement());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return datasArray;
    }

    /***
     * 根据输入的数据集批量查询ChangeRecord2对象
     *
     * 注：可以过滤ChangeActivityIfc状态
     *
     * @param datasMap
     * @param forbidState
     *            限制的状态
     * @return
     * @throws WTException
     */
    public static Collection<ChangeRecord2> getChangeRecord2s(Map<ChangeActivityIfc, Collection<Changeable2>> datasMap, String forbidState) throws WTException {
        Collection<ChangeRecord2> datasArray = new HashSet<ChangeRecord2>();
        if (datasMap == null || datasMap.size() == 0) {
            return datasArray;
        }

        try {
            QuerySpec qs = new QuerySpec(ChangeRecord2.class);
            ReferenceFactory rf = new ReferenceFactory();
            for (Map.Entry<ChangeActivityIfc, Collection<Changeable2>> entryMap : datasMap.entrySet()) {
                ChangeActivityIfc changeActivityIfc = entryMap.getKey();
                if (ChangeUtils.checkState((WTChangeActivity2) changeActivityIfc, forbidState)) {
                    continue;
                }
                for (Changeable2 changeable2 : entryMap.getValue()) {
                    if (qs.getConditionCount() > 0) {
                        qs.appendOr();
                    }
                    qs.appendOpenParen();
                    String ecaOID = rf.getReferenceString(entryMap.getKey());
                    qs.appendWhere(new SearchCondition(ChangeRecord2.class, "roleAObjectRef.key.branchId", SearchCondition.EQUAL, Long.parseLong(ecaOID.substring(ecaOID.lastIndexOf(":") + 1, ecaOID.length()))), new int[]{0});
                    qs.appendAnd();
                    String ptOID = PersistenceHelper.getObjectIdentifier(changeable2).toString();
                    qs.appendWhere(new SearchCondition(ChangeRecord2.class, "roleBObjectRef.key.id", SearchCondition.EQUAL, Long.parseLong(ptOID.substring(ptOID.lastIndexOf(":") + 1, ptOID.length()))), new int[]{0});
                    qs.appendCloseParen();
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("getChangeRecord2s() ... QuerySpec : " + qs.toString());
            }
            if (qs.getConditionCount() == 0) {
                return datasArray;
            }
            QueryResult qr = PersistenceServerHelper.manager.query(qs);
            while (qr.hasMoreElements()) {
                datasArray.add((ChangeRecord2) qr.nextElement());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return datasArray;
    }

    /***
     * 根据输入的数据集批量查询ChangeRecord2对象
     *
     * @param ptArray
     *            产生对象集合
     * @return
     * @throws WTException
     */
    public static Collection<ChangeRecord2> getChangeRecord2s(WTCollection ptArray) throws WTException {
        Collection<ChangeRecord2> datasArray = new HashSet<ChangeRecord2>();
        if (ptArray == null || ptArray.size() == 0) {
            return datasArray;
        }

        try {
            QuerySpec qs = new QuerySpec();
            qs.setQueryLimit(-1);
            int linkIndex = qs.appendClassList(ChangeRecord2.class, true);
            List<Long> longArray = new ArrayList<Long>();
            for (Object ptObject : ptArray) {
                if (ptObject instanceof ObjectReference) {
                    ptObject = ((ObjectReference) ptObject).getObject();
                }
                longArray.add(PersistenceHelper.getObjectIdentifier((Persistable) ptObject).getId());
            }
            // 产生的对象
            String ptKeyId = ObjectToObjectLink.ROLE_BOBJECT_REF + "." + ObjectReference.KEY + "." + ObjectIdentifier.ID;
            Long[] longAy = new Long[longArray.size()];
            longArray.toArray(longAy);
            ArrayExpression roleBObjectExpression = new ArrayExpression(longAy);
            ClassAttribute roleBObjectAttribute = new ClassAttribute(ChangeRecord2.class, ptKeyId);
            SearchCondition sc = new SearchCondition(roleBObjectAttribute, SearchCondition.IN, roleBObjectExpression);
            qs.appendWhere(sc, new int[]{linkIndex});
            if (LOG.isDebugEnabled()) {
                LOG.debug("getChangeRecord2s() ... QuerySpec : " + qs.toString());
            }
            QueryResult qr = PersistenceServerHelper.manager.query(qs);
            while (qr.hasMoreElements()) {
                Object[] objArray = (Object[]) qr.nextElement();
                for (int i = 0; i < objArray.length; i++) {
                    datasArray.add((ChangeRecord2) objArray[i]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return datasArray;
    }

    /***
     * 根据ECA及受影响对象获取AffectedActivityData对象
     *
     * @param changeActivityIfc
     *            更改任务
     * @param changeable2
     *            受影响对象
     * @return
     * @throws WTException
     */
    public static AffectedActivityData getAffectedActivity(ChangeActivityIfc changeActivityIfc, Changeable2 changeable2) throws WTException {
        if (changeActivityIfc == null || changeable2 == null) {
            return null;
        }

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
            if (LOG.isDebugEnabled()) {
                LOG.debug("getAffectedActivityDatas() ... QuerySpec : " + qs.toString());
            }
            QueryResult qr = PersistenceServerHelper.manager.query(qs);
            while (qr.hasMoreElements()) {
                return (AffectedActivityData) qr.nextElement();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return null;
    }

    /***
     * 根据受影响对象获取AffectedActivityData对象
     *
     * @param changeActivityIfc
     *            更改任务
     * @param changeable2
     *            受影响对象
     * @return
     * @throws WTException
     */
    public static Collection<AffectedActivityData> getAffectedActivity(Changeable2 changeable2) throws WTException {
        Collection<AffectedActivityData> returnArray = new HashSet<AffectedActivityData>();
        if (changeable2 == null) {
            return returnArray;
        }

        try {
            QuerySpec qs = new QuerySpec(AffectedActivityData.class);
            String ptOID = PersistenceHelper.getObjectIdentifier(changeable2).toString();
            qs.appendWhere(new SearchCondition(AffectedActivityData.class, "roleBObjectRef.key.id", SearchCondition.EQUAL, Long.parseLong(ptOID.substring(ptOID.lastIndexOf(":") + 1, ptOID.length()))), new int[]{0});
            if (LOG.isDebugEnabled()) {
                LOG.debug("getAffectedActivityDatas() ... QuerySpec : " + qs.toString());
            }
            QueryResult qr = PersistenceServerHelper.manager.query(qs);
            while (qr.hasMoreElements()) {
                returnArray.add((AffectedActivityData) qr.nextElement());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return returnArray;
    }

    /***
     * 检查对象已存在变更中
     *
     * @param changeable2
     * @return
     * @throws WTException
     */
    public static Boolean existChanges(Changeable2 changeable2, ChangeOrder2 order) throws WTException {
        Boolean isExist = false;
        if (changeable2 == null) {
            return isExist;
        }
        System.out.println("ECN numner=====" + order.getNumber());
        // 获取对象所有关联的ECA对象
        Collection<AffectedActivityData> affectedActivitys = getAffectedActivity(changeable2);
        for (AffectedActivityData affectedActivityData : affectedActivitys) {
            ChangeActivity2 changeActivity2 = affectedActivityData.getChangeActivity2();
            WTChangeOrder2 order2 = getEcnByEca((WTChangeActivity2) changeActivity2);
            System.out.println("order2 numner=====" + order2.getNumber());
            System.out.println("eca name===" + changeActivity2.getNumber() + changeActivity2.getName());
            if (!order.getNumber().startsWith(order2.getNumber())) {
                if ((!checkState(changeActivity2, ChangeConstants.CANCELLED)) && (!checkState(changeActivity2, ChangeConstants.RESOLVED))) {

                    isExist = true;
                    break;
                }
            }
        }
        return isExist;
    }

    public static WTChangeOrder2 getEcnByEca(WTChangeActivity2 ca) {
        WTChangeOrder2 ecn = null;
        if (ca == null) return ecn;

        try {
            QueryResult result = ChangeHelper2.service.getChangeOrder(ca);
            if (result.hasMoreElements()) {
                return (WTChangeOrder2) result.nextElement();
            }
        } catch (WTException e) {
            e.printStackTrace();
        }

        return ecn;
    }

    /***
     * 获取更改通告中所有受影响对象
     *
     * @param changeOrder2
     *            变更通告
     * @return
     * @throws WTException
     */
    public static Collection<Changeable2> getChangeablesBefore(WTChangeOrder2 changeOrder2) throws WTException {
        Collection<Changeable2> datasArray = new HashSet<Changeable2>();
        if (changeOrder2 == null) {
            return datasArray;
        }

        try {
            // 获取更改通告中所有的受影响对象
            QueryResult qr = ChangeHelper2.service.getChangeablesBefore(changeOrder2);
            while (qr.hasMoreElements()) {
                Object object = qr.nextElement();
                if (object instanceof ObjectReference) {
                    object = ((ObjectReference) object).getObject();
                }
                if (object instanceof Changeable2) {
                    datasArray.add((Changeable2) object);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return datasArray;
    }

    /***
     * 获取ECN中所有ECA与受影响对象
     *
     * @param ecn
     * @return
     * @throws WTException
     */
    public static Map<ChangeActivityIfc, Collection<Changeable2>> getChangeablesBeforeInfo(WTChangeOrder2 ecn) throws WTException {
        Map<ChangeActivityIfc, Collection<Changeable2>> datasMap = new HashMap<ChangeActivityIfc, Collection<Changeable2>>();
        if (ecn == null) {
            return datasMap;
        }

        try {
            // 获取ECN中所有ECA对象
            Collection<ChangeActivityIfc> ecaArray = ChangeUtils.getChangeActivities(ecn);
            for (ChangeActivityIfc changeActivityIfc : ecaArray) {
                // 受影响对象集合
                Collection<Changeable2> datasArray = new HashSet<Changeable2>();
                // 获取ECA中所有受影响对象
                QueryResult qr = ChangeHelper2.service.getChangeablesBefore(changeActivityIfc);
                while (qr.hasMoreElements()) {
                    Object object = qr.nextElement();
                    if (object instanceof ObjectReference) {
                        object = ((ObjectReference) object).getObject();
                    }
                    if (object instanceof Changeable2) {
                        datasArray.add((Changeable2) object);
                    }
                }
                datasMap.put(changeActivityIfc, datasArray);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return datasMap;
    }

    /***
     * 获取ECN中所有ECA与产生对象
     *
     * @param ecn
     * @return
     * @throws WTException
     */
    public static Map<ChangeActivityIfc, Collection<Changeable2>> getChangeablesAfterInfo(WTChangeOrder2 ecn) throws WTException {
        Map<ChangeActivityIfc, Collection<Changeable2>> datasMap = new HashMap<ChangeActivityIfc, Collection<Changeable2>>();
        if (ecn == null) {
            return datasMap;
        }

        try {
            // 获取ECN中所有ECA对象
            Collection<ChangeActivityIfc> ecaArray = ChangeUtils.getChangeActivities(ecn);
            for (ChangeActivityIfc changeActivityIfc : ecaArray) {
                // 产生对象集合
                Collection<Changeable2> datasArray = new HashSet<Changeable2>();
                // 获取ECA中所有产生对象
                QueryResult qr = ChangeHelper2.service.getChangeablesAfter(changeActivityIfc);
                while (qr.hasMoreElements()) {
                    Object object = qr.nextElement();
                    if (object instanceof ObjectReference) {
                        object = ((ObjectReference) object).getObject();
                    }
                    if (object instanceof Changeable2) {
                        datasArray.add((Changeable2) object);
                    }
                }
                datasMap.put(changeActivityIfc, datasArray);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return datasMap;
    }

    /***
     * 获取更改通告中所有产生对象
     *
     * @param changeOrder2
     *            变更通告
     * @return
     * @throws WTException
     */
    public static Collection<Changeable2> getChangeablesAfter(WTChangeOrder2 changeOrder2) throws WTException {
        Collection<Changeable2> datasArray = new HashSet<Changeable2>();
        if (changeOrder2 == null) {
            return datasArray;
        }

        try {
            // 获取更改通告中所有的受影响对象
            QueryResult qr = ChangeHelper2.service.getChangeablesAfter(changeOrder2);
            while (qr.hasMoreElements()) {
                Object object = qr.nextElement();
                if (object instanceof ObjectReference) {
                    object = ((ObjectReference) object).getObject();
                }
                if (object instanceof Changeable2) {
                    datasArray.add((Changeable2) object);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return datasArray;
    }

    /***
     * 获取更改通告中所有更改任务
     *
     * @param changeOrder2
     *            变更通告
     * @return
     * @throws WTException
     */
    public static Collection<ChangeActivityIfc> getChangeActivities(WTChangeOrder2 changeOrder2) throws WTException {
        Collection<ChangeActivityIfc> datasArray = new HashSet<ChangeActivityIfc>();
        if (changeOrder2 == null) {
            return datasArray;
        }

        try {
            // 获取更改通告中所有的受影响对象
            QueryResult qr = ChangeHelper2.service.getChangeActivities(changeOrder2);
            while (qr.hasMoreElements()) {
                Object object = qr.nextElement();
                if (object instanceof ObjectReference) {
                    object = ((ObjectReference) object).getObject();
                }
                if (object instanceof WTChangeActivity2) {
                    datasArray.add((WTChangeActivity2) object);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return datasArray;
    }

    /***
     * 获取页面中build表单移除的标准数据集合
     *
     * @param nmcommandBean
     * @param tableName
     * @return
     * @throws WTException
     */
    public static Collection<Changeable2> getAddedItemsByName(NmCommandBean nmcommandBean, String tableName) throws WTException {
        Collection<Changeable2> datasArray = new HashSet<Changeable2>();
        if (nmcommandBean == null) {
            return datasArray;
        }

        // 受影响对象
        List<NmOid> addAffectedItems = nmcommandBean.getAddedItemsByName(tableName);
        for (NmOid nmOid : addAffectedItems) {
            Object object = nmOid.getLatestIterationObject();
            if (object instanceof ObjectReference) {
                object = ((ObjectReference) object).getObject();
            }
            datasArray.add((Changeable2) object);
        }

        return datasArray;
    }

    /***
     * 获取页面中build表单移除的标准数据集合
     *
     * @param nmcommandBean
     * @param tableName
     *            表单名称
     * @return
     * @throws WTException
     */
    public static Collection<Changeable2> getRemovedItemsByName(NmCommandBean nmcommandBean, String tableName) throws WTException {
        Collection<Changeable2> datasArray = new HashSet<Changeable2>();
        if (nmcommandBean == null) {
            return datasArray;
        }

        // 受影响对象
        List<NmOid> addAffectedItems = nmcommandBean.getRemovedItemsByName(tableName);
        for (NmOid nmOid : addAffectedItems) {
            Object object = nmOid.getLatestIterationObject();
            if (object instanceof ObjectReference) {
                object = ((ObjectReference) object).getObject();
            }
            datasArray.add((Changeable2) object);
        }

        return datasArray;
    }

    /***
     * 根据受影响对象表单保存ECA对象
     *
     * @param nmcommandBean
     *
     * @param changeOrder
     *            变更通告
     *
     * @param ecnDatasMap
     *            变更通告已存在的受影响对象集合
     *
     * @throws WTException
     */
    public static Collection<Changeable2> saveChangeActivity2(NmCommandBean nmcommandBean, WTChangeOrder2 changeOrder, Map<ChangeActivityIfc, Collection<Changeable2>> ecnDatasMap) throws WTException {
        Collection<Changeable2> affectedArray = new HashSet<>();
        if (nmcommandBean == null || changeOrder == null) {
            return affectedArray;
        }

        try {
            // 获取页面中changeTaskArray控件值并根据规则解析为对应集合
            Map<Persistable, Map<String, String>> pageDatasMap = getPageChangeTaskArray(nmcommandBean);
            // 更新IBA属性
            for (Map.Entry<Persistable, Map<String, String>> entryMap : pageDatasMap.entrySet()) {
                Map<String, Object> ibaMap = new HashMap<>();
                for (Map.Entry<String, String> ibaEntryMap : entryMap.getValue().entrySet()) {
                    // 过滤‘描述’信息
                    if (ibaEntryMap.getKey().equals(AADDESCRIPTION_COMPID)) {
                        continue;
                    }
                    ibaMap.put(ibaEntryMap.getKey(), ibaEntryMap.getValue());
                }
                PIAttributeHelper.service.forceUpdateSoftAttributes(entryMap.getKey(), ibaMap);
            }
            // 根据受影响对象表单构建创建ECA时需要填充的数据关系
            Map<Persistable, Collection<Persistable>> constructRelation = constructRelation(pageDatasMap);
            if (constructRelation != null && constructRelation.size() > 0) {
                for (Map.Entry<Persistable, Collection<Persistable>> entryMap : constructRelation.entrySet()) {
                    // 忽略不处理
                    Boolean isNeglect = false;
                    // 受影响对象
                    Changeable2 changeable2 = (Changeable2) entryMap.getKey();
                    if (changeable2 instanceof WTDocument) {
                        if (!PartDocHelper.isReferenceDocument((WTDocument) changeable2)) {
                            throw new WTException(((WTDocument) changeable2).getDisplayIdentity() + " 说明文档不能独立进行变更!");
                        }
                    }
                    // 受影响对象对应的更改任务
                    WTChangeActivity2 eca = null;
                    if (ecnDatasMap != null) {
                        for (Map.Entry<ChangeActivityIfc, Collection<Changeable2>> entryMapEntry : ecnDatasMap.entrySet()) {
                            if (entryMapEntry.getValue().contains(changeable2)) {
                                if (!(changeable2 instanceof WTPart)) {
                                    for (Changeable2 bafore : entryMapEntry.getValue()) {
                                        if (bafore instanceof WTPart) {
                                            isNeglect = true;
                                        }
                                    }
                                }
                                eca = (WTChangeActivity2) entryMapEntry.getKey();
                                // 废弃状态过滤
                                if (checkState(eca, CANCELLED)) {
                                    eca = null;
                                }
                                break;
                            }
                        }
                    }
                    if (isNeglect) {
                        continue;
                    }

                    // 收集需要添加的受影响对象
                    Vector<Changeable2> vector = new Vector<>();
                    if (eca == null) {
                        // 检查对象是否存在变更中
                        // if(existChanges(changeable2,changeOrder)){
                        // throw new
                        // WTException(IdentityFactory.getDisplayIdentifier(changeable2).getLocalizedMessage(Locale.ENGLISH)
                        // + " 存在未完成的变更单!") ;
                        // }
                        // 创建ECA对象
                        eca = createChangeTask(changeOrder, getNumber(changeable2), null, null, null, null);
                        vector.add(changeable2);
                        for (Persistable persistable : entryMap.getValue()) {
                            vector.add((Changeable2) persistable);
                        }
                    } else {
                        // 收集新增的部件说明文档及图纸并添加至受影响对象集合
                        Collection<Persistable> associatedItems = entryMap.getValue();
                        if (ecnDatasMap != null && ecnDatasMap.size() > 0) {
                            if (ecnDatasMap.containsKey(eca)) {
                                Collection<Changeable2> beforeArray = ecnDatasMap.get(eca);
                                if (beforeArray != null && beforeArray.size() > 0) {
                                    for (Persistable persistable : associatedItems) {
                                        Boolean isCollect = true;
                                        for (Changeable2 before : beforeArray) {
                                            if (getNumber(persistable).equals(getNumber(before))) {
                                                isCollect = false;
                                                break;
                                            }
                                        }
                                        if (isCollect) {
                                            vector.add((Changeable2) persistable);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    affectedArray.addAll(vector);
                    if (vector.size() > 0) {
                        // 添加受影响对象
                        addAffectedActivityData(eca, vector);
                    }
                    // 获取用户针对每一列输入的数据
                    Map<String, String> attributesMap = pageDatasMap.get(entryMap.getKey());
                    // 部件‘类型’选择‘替换’时ECA状态设置为‘已发布’,选择‘升级’时ECA状态设置为‘开启’
                    if (attributesMap.containsKey(CHANGETYPE_COMPID)) {
                        String attributeValue = attributesMap.get(CHANGETYPE_COMPID);
                        if (PIStringUtils.isNotNull(attributeValue) && attributeValue.contains("替换")) {
                            eca = (WTChangeActivity2) PICoreHelper.service.setLifeCycleState(eca, RESOLVED);
                        } else if (PIStringUtils.isNotNull(attributeValue) && attributeValue.contains("升版")) {
                            eca = (WTChangeActivity2) PICoreHelper.service.setLifeCycleState(eca, OPEN);
                        }
                    }
                    // 更新责任人
                    if (attributesMap.containsKey(RESPONSIBLEPERSON_COMPID)) {
                        setChangeActivity2Assignee(eca, attributesMap.get(RESPONSIBLEPERSON_COMPID));
                    }
                    // 期望完成日期
                    if (attributesMap.containsKey(ChangeConstants.COMPLETIONTIME_COMPID)) {
                        eca = updateNeedDate(eca, attributesMap.get(ChangeConstants.COMPLETIONTIME_COMPID));
                    }
                    // 更新备注
                    if (attributesMap.containsKey(AADDESCRIPTION_COMPID)) {
                        AffectedActivityData affectedActivityData = getAffectedActivity(eca, changeable2);
                        if (affectedActivityData != null) {
                            affectedActivityData.setDescription(attributesMap.get(AADDESCRIPTION_COMPID));
                            PersistenceHelper.manager.save(affectedActivityData);
                        }
                    }
                    // 更新部件关联的说明文档及图纸备注
                    for (Persistable persistable : entryMap.getValue()) {
                        attributesMap = pageDatasMap.get(persistable);
                        if (attributesMap.containsKey(AADDESCRIPTION_COMPID)) {
                            AffectedActivityData affectedActivityData = getAffectedActivity(eca, (Changeable2) persistable);
                            if (affectedActivityData != null) {
                                affectedActivityData.setDescription(attributesMap.get(AADDESCRIPTION_COMPID));
                                PersistenceHelper.manager.save(affectedActivityData);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return affectedArray;
    }

    /***
     * 获取页面中changeTaskArray控件值并根据规则解析为对应集合
     * @param nmcommandBean
     * 解析结果集合
     * @throws WTException
     */
    public static Map<Persistable, Map<String, String>> getPageChangeTaskArray(NmCommandBean nmcommandBean) throws WTException {
        Map<Persistable, Map<String, String>> changeTaskArray = new HashMap<>();
        if (nmcommandBean == null) {
            return changeTaskArray;
        }

        try {
            // 获取新增数据列
            Map<String, Object> parameterMap = nmcommandBean.getParameterMap();
            if (parameterMap.containsKey(CHANGETASK_ARRAY)) {
                String[] changeTaskArrayStr = (String[]) parameterMap.get(CHANGETASK_ARRAY);
                if (changeTaskArrayStr != null && changeTaskArrayStr.length > 0) {
                    String datasJSON = changeTaskArrayStr[0];
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getChangeTaskArray() ... changeTaskArray : " + datasJSON);
                    }
                    if (PIStringUtils.isNotNull(datasJSON)) {
                        JSONArray jsonArray = new JSONArray(datasJSON);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Persistable persistable = null;
                            // 存储页面属性信息
                            Map<String, String> attributesMap = new HashMap<>();
                            JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                            Iterator<String> keyIterator = jsonObject.keys();
                            while (keyIterator.hasNext()) {
                                // 属性ID
                                String key = keyIterator.next();
                                // 属性值
                                String value = jsonObject.getString(key);
                                if (PIStringUtils.isNotNull(value)) {
                                    if (key.equalsIgnoreCase(OID_COMPID)) {
                                        persistable = (new ReferenceFactory()).getReference(value).getObject();
                                        continue;
                                    }
                                    if (key.equals(COMPLETIONTIME_COMPID)) {
                                        if (value.contains(" ")) {
                                            value = value.substring(0, value.indexOf(" ")).trim();
                                        }
                                        if (value.contains("-")) {
                                            value = (new SimpleDateFormat(ChangeConstants.SIMPLE_DATE_FORMAT_02)).format((new SimpleDateFormat(ChangeConstants.SIMPLE_DATE_FORMAT_03)).parse(value));
                                        }
                                    }
                                    attributesMap.put(key, value);
                                }
                            }
                            if (persistable != null) {
                                changeTaskArray.put(persistable, attributesMap);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return changeTaskArray;
    }

    /***
     * 根据受影响对象表单构建创建ECA时需要填充的数据关系
     *
     * 注：供客制化ECN表单创建及编辑使用
     *
     * @param pageDatasMap
     * @return
     * @throws WTException
     */
    public static Map<Persistable, Collection<Persistable>> constructRelation(Map<Persistable, Map<String, String>> pageDatasMap) throws WTException {
        Map<Persistable, Collection<Persistable>> returnMap = new HashMap<>();
        if (pageDatasMap == null || pageDatasMap.size() == 0) {
            return returnMap;
        }

        try {
            // 需要收集上层父件集合
            Collection<WTPart> childArray = new HashSet<>();
            // 存储部件与图文档关系数据
            Map<WTPart, Collection<Persistable>> partDocInfoMap = new HashMap<>();
            for (Map.Entry<Persistable, Map<String, String>> entryMap : pageDatasMap.entrySet()) {
                Persistable persistable = entryMap.getKey();
                if (persistable instanceof WTPart) {
                    WTPart part = (WTPart) persistable;
                    // 收集图文档
                    Collection<Persistable> associatedItems = new HashSet<>();
                    // 获取部件关联的图文档
                    QueryResult qr = PartDocHelper.service.getAssociatedDocuments(part);
                    while (qr.hasMoreElements()) {
                        Object object = qr.nextElement();
                        if (object instanceof ObjectReference) {
                            object = ((ObjectReference) object).getObject();
                        }
                        if (AccessControlHelper.manager.hasAccess(SessionHelper.manager.getPrincipal(), object, AccessPermission.READ)) {
                            if (object instanceof EPMDocument) {
                                associatedItems.add((Persistable) object);
                            } else if (object instanceof WTDocument) {
                                // 过滤参考文档
                                if (!PartDocHelper.isReferenceDocument((WTDocument) object)) {
                                    associatedItems.add((Persistable) object);
                                }
                            }
                        }
                    }
                    partDocInfoMap.put(part, associatedItems);

                    // 根据用户所选‘类型’为“替换”必须收集上层部件
                    Map<String, String> attributeInfoMap = entryMap.getValue();
                    if (attributeInfoMap.containsKey(CHANGETYPE_COMPID)) {
                        String attributeValue = attributeInfoMap.get(CHANGETYPE_COMPID);
                        if (PIStringUtils.isNotNull(attributeValue) && attributeValue.contains("替换")) {
                            childArray.add(part);
                        }
                    }
                    if (specificNode(part, "PCB") || specificNode(part, "E1500000")) {
                        childArray.add(part);
                    }
                }
            }
            // 批量查询上层父件
            Map<WTPartUsageLink, WTPart> parentMap = ChangePartQueryUtils.batchQueryFirstParents(childArray);
            // 过滤非最新部件
            Collection<WTPart> parentArray = ChangePartQueryUtils.excludeNonLatestVersionsPart(parentMap.values());
            // 存储部件与上层父件关系数据
            Map<String, Collection<String>> linkMap = new HashMap<>();
            if (parentArray != null && parentArray.size() != 0) {
                for (WTPart parentPart : parentArray) {
                    for (Map.Entry<WTPartUsageLink, WTPart> entryMap : parentMap.entrySet()) {
                        if (PersistenceHelper.isEquivalent(parentPart, entryMap.getValue())) {
                            WTPartMaster childMaster = entryMap.getKey().getUses();
                            String number = childMaster.getNumber();
                            Collection<String> parentInfoArray = linkMap.computeIfAbsent(number, k -> new HashSet<>());
                            parentInfoArray.add(parentPart.getNumber() + parentPart.getViewName());
                        }
                    }
                }
            }

            for (Map.Entry<WTPart, Collection<Persistable>> entryMap : partDocInfoMap.entrySet()) {
                WTPart childPart = entryMap.getKey();
                if (linkMap.containsKey(childPart.getNumber())) {
                    // 上层父件信息
                    Collection<String> parentInfoArray = linkMap.get(childPart.getNumber());
                    System.out.println("parentInfoArray : " + parentInfoArray);
                    // PCB及子件变更时，父件必须全部收集
                    if (specificNode(childPart, "PCB") || specificNode(childPart, "E1500000")) {
                        Boolean isError = false;
                        Boolean isExist = false;
                        for (String parentInfo : parentInfoArray) {
                            for (Persistable persistable : pageDatasMap.keySet()) {
                                if (persistable instanceof WTPart) {
                                    WTPart parentPart = (WTPart) persistable;
                                    if ((parentPart.getNumber() + parentPart.getViewName()).equals(parentInfo)) {
                                        isExist = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!isExist) {
                            throw new WTException(childPart.getDisplayIdentity() + " PCB部件上层父件必须至少收集一个!");
                        }
                    } else {
                        // ‘类型’为“替换”上层父件必须收集
                        Boolean isError = true;
                        for (Persistable persistable : pageDatasMap.keySet()) {
                            if (persistable instanceof WTPart) {
                                WTPart parentPart = (WTPart) persistable;
                                System.out.println("parentPart : " + parentPart.getDisplayIdentity());
                                for (String parentInfo : parentInfoArray) {
                                    if ((parentPart.getNumber() + parentPart.getViewName()).equals(parentInfo)) {
                                        isError = false;
                                        break;
                                    }
                                }
                            }
                        }
                        if (isError) {
                            throw new WTException(childPart.getDisplayIdentity() + " 类型：替换 必须收集上层对象!");
                        }
                    }
                }

                // 构建关系
                Collection<Persistable> associatedItems = new HashSet<>();
                for (Persistable docPt : entryMap.getValue()) {
                    for (Persistable persistable : pageDatasMap.keySet()) {
                        if (getNumber(docPt).equals(getNumber(persistable))) {
                            associatedItems.add(persistable);
                            break;
                        }
                    }
                }
                returnMap.put(childPart, associatedItems);
            }
            for (Persistable persistable : pageDatasMap.keySet()) {
                if (persistable instanceof WTPart) {
                    continue;
                }
                Boolean isCollect = true;
                for (Collection<Persistable> docArray : returnMap.values()) {
                    if (docArray.contains(persistable)) {
                        isCollect = false;
                        break;
                    }
                }
                if (isCollect) {
                    returnMap.put(persistable, new HashSet<>());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return returnMap;
    }

    /***
     * 判断物料是否为指定分类
     *
     * @param part
     *            部件
     * @param nodeName
     *            分类
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
            List<String> clfArray = new ArrayList<String>();
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
     * 添加受影响对象
     *
     * @param eca
     *            更改任务
     * @param datasArray
     *            添加对象集合
     * @throws WTException
     */
    public static void addAffectedActivityData(WTChangeActivity2 eca, Collection<Changeable2> datasArray) throws WTException {
        if (datasArray == null || eca == null || datasArray.size() == 0) {
            LOG.error("  addAffectedActivityData data or obj is null ");
            return;
        }
        try {
            // 查询ECA中所有受影响对象
            QueryResult qr = ChangeHelper2.service.getChangeablesBefore(eca);
            if (qr != null) {
                Vector<?> vector = qr.getObjectVectorIfc().getVector();
                Map<String, Changeable2> tempMap = new HashMap<String, Changeable2>();
                Vector<Changeable2> tempVector = new Vector<Changeable2>();
                for (Changeable2 changeable2 : datasArray) {
                    if (!tempVector.contains(changeable2)) {
                        tempMap.put(getNumber(changeable2), changeable2);
                        tempVector.add(changeable2);
                    }
                }
                for (Object object : vector) {
                    if (object instanceof ObjectReference) {
                        object = ((ObjectReference) object).getObject();
                    }
                    Changeable2 changeable2 = (Changeable2) object;
                    if (tempMap.containsKey(getNumber(changeable2))) {
                        tempVector.remove(tempMap.get(getNumber(changeable2)));
                    }
                }
                if (tempVector.size() > 0) {
                    ChangeHelper2.service.storeAssociations(AffectedActivityData.class, eca, tempVector);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e);
        }
    }

    /***
     * 修订并添加产生对象
     *
     * @param eca
     * @param datasArray
     * @throws WTException
     */
    public static void reviseAndAddChangeRecord2(WTChangeActivity2 eca, Collection<Changeable2> datasArray) throws WTException {
        if (datasArray == null || eca == null || datasArray.size() == 0) {
            LOG.error("  reviseAndAddChangeRecord2 data or obj is null ");
            return;
        }

        try {
            // 修订对象
            WTCollection wtCollection = PICoreHelper.service.revise(new WTArrayList(datasArray), false);
            // 查询ECA中所有产生对象
            QueryResult qr = ChangeHelper2.service.getChangeablesAfter(eca);
            if (qr != null) {
                Vector<?> vector = qr.getObjectVectorIfc().getVector();
                Map<String, Changeable2> tempMap = new HashMap<String, Changeable2>();
                Vector<Changeable2> tempVector = new Vector<Changeable2>();
                for (Object object : wtCollection) {
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
                for (Object object : vector) {
                    if (object instanceof ObjectReference) {
                        object = ((ObjectReference) object).getObject();
                    }
                    Changeable2 changeable2 = (Changeable2) object;
                    if (tempMap.containsKey(getNumber(changeable2))) {
                        tempVector.remove(tempMap.get(getNumber(changeable2)));
                    }
                }
                if (tempVector.size() > 0) {
                    ChangeHelper2.service.storeAssociations(ChangeRecord2.class, eca, tempVector);
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }
    }

    /***
     * 创建或更新ECA对象信息
     *
     * @param changeOrder
     * @param nmcommandBean
     * @throws WTException
     */
    public static Collection<ChangeActivityIfc> createEditChangeActivity2(WTChangeOrder2 changeOrder, NmCommandBean nmcommandBean) throws WTException {
        Collection<ChangeActivityIfc> returnArray = new HashSet<>();
        if (changeOrder == null || nmcommandBean == null) {
            return returnArray;
        }
        try {
            Map<String, Object> parameterMap = nmcommandBean.getParameterMap();
            if (parameterMap.containsKey(DATA_ARRAY)) {
                String[] datasArrayStr = (String[]) parameterMap.get(DATA_ARRAY);
                if (datasArrayStr != null && datasArrayStr.length > 0) {
                    String datasJSON = datasArrayStr[0];
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("createEditChangeActivity2() ... datasArray : " + datasJSON);
                    }
                    if (PIStringUtils.isNotNull(datasJSON)) {
                        JSONObject jsonObject = new JSONObject(datasJSON);
                        Iterator<?> keyIterator = jsonObject.keys();
                        while (keyIterator.hasNext()) {
                            Object key = keyIterator.next();
                            // 数据信息
                            JSONObject dataJSONObject = new JSONObject(jsonObject.getString((String) key));
                            WTChangeActivity2 eca = null;
                            // ECA对象OID
                            if (dataJSONObject.has(CHANGEACTIVITY2_COMPID)) {
                                String ecaOID = dataJSONObject.getString(CHANGEACTIVITY2_COMPID);
                                if (PIStringUtils.isNotNull(ecaOID)) {
                                    eca = (WTChangeActivity2) (new ReferenceFactory()).getReference(ecaOID).getObject();
                                }
                            }
                            returnArray.add(createEditChangeActivity2(changeOrder, dataJSONObject, eca));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }
        return returnArray;
    }

    /***
     * 创建或更新ECA对象信息
     *
     * @param changeOrder
     *            ECN
     * @param dataJSONObject
     *            需要更新的数据集
     * @param eca
     * @throws WTException
     */
    public static ChangeActivityIfc createEditChangeActivity2(WTChangeOrder2 changeOrder, JSONObject dataJSONObject, WTChangeActivity2 eca) throws WTException {
        try {
            if (eca == null) {
                // TODO 创建时需把名称带上
                eca = createChangeTask(changeOrder, dataJSONObject.getString(CHANGETHEME_COMPID), null, dataJSONObject.getString(CHANGEDESCRIBE_COMPID), TRANSACTIONAL_CHANGEACTIVITY2, dataJSONObject.getString(RESPONSIBLE_COMPID));
            } else {
                // 名称修改
                String newName = dataJSONObject.getString(CHANGETHEME_COMPID);
                if (!eca.getName().equals(newName)) {
                    setChangeActivity2Name(eca, newName);
                }
                // 工作负责人修改
                setChangeActivity2Assignee(eca, dataJSONObject.getString(RESPONSIBLE_COMPID));
                // 说明修改
                String changeDescribe = dataJSONObject.getString(CHANGEDESCRIBE_COMPID);
                if (!changeDescribe.equals(eca.getDescription())) {
                    eca.setDescription(changeDescribe);
                    eca = (WTChangeActivity2) PersistenceHelper.manager.save(eca);
                }
            }

            // 期望完成日期
            if (dataJSONObject.has(ChangeConstants.NEEDDATE_COMPID)) {
                String needDate = dataJSONObject.getString(ChangeConstants.NEEDDATE_COMPID);
                eca = updateNeedDate(eca, needDate);
            }

            return eca;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }
    }

    /***
     * 更新ECA对象上的‘需要日期’属性
     *
     * @param eca
     *            ChangeActivity2对象
     * @param needDate
     *            格式：yyyy-MM-dd 或者 yyyy/MM/dd
     * @return
     * @throws WTException
     */
    public static WTChangeActivity2 updateNeedDate(WTChangeActivity2 eca, String needDate) throws WTException {
        if (eca == null || PIStringUtils.isNull(needDate)) {
            return eca;
        }

        try {
            if (needDate.contains(" ")) {
                needDate = needDate.substring(0, needDate.indexOf(" ")).trim();
            }
            if (needDate.contains("-")) {
                needDate = (new SimpleDateFormat(ChangeConstants.SIMPLE_DATE_FORMAT_02)).format((new SimpleDateFormat(ChangeConstants.SIMPLE_DATE_FORMAT_03)).parse(needDate));
            }
            // SimpleDateFormat sf = new
            // SimpleDateFormat(ChangeConstants.SIMPLE_DATE_FORMAT_02);
            // Date dt = sf.parse(needDate) ;
            // eca.setNeedDate(new Timestamp(dt.getTime()));
            // eca = (WTChangeActivity2)PersistenceHelper.manager.save(eca) ;
            eca = (WTChangeActivity2) PIAttributeHelper.service.forceUpdateSoftAttribute(eca, COMPLETION_TIME, needDate);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }

        return eca;
    }

    /***
     * 用户批量添加到对象团队角色中
     *
     * @param teamManaged
     *            对象容器
     * @param roleName
     *            角色名称
     * @param membersArray
     *            用户集合
     * @throws WTException
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
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
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        } finally {
            PIAccessHelper.service.removePersistablePermissions(team, WTPrincipalReference.newWTPrincipalReference(SessionHelper.manager.getPrincipal()), AccessPermission.MODIFY, true);
        }
    }

    /***
     * 清空团队指定角色中的所有用户
     *
     * @param team
     * @param role
     * @throws WTException
     */
    public static void cleanRoleByTeam(Team team, Role role) throws WTException {
        if (team == null || role == null) {
            return;
        }

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
            // TODO: handle exception
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        }
    }

    /***
     * 更新或创建ChangeTaskBean对象
     *
     * @param changeTheme
     *            更改主题
     * @param changeDescribe
     *            更改描述
     * @param responsible
     *            责任人
     * @param changeActivity2
     *            更改任务OID
     * @param chaneTaskBeanID
     * @return
     * @throws WTException
     */
    public static ChangeTaskBean createChangeTaskBean(String changeTheme, String changeDescribe, String responsible, String changeActivity2, String chaneTaskBeanID) throws WTException {
        ChangeTaskBean changeTaskBean = null;
        if (PIStringUtils.isNotNull(chaneTaskBeanID)) {
            changeTaskBean = new ChangeTaskBean(chaneTaskBeanID);
        } else {
            changeTaskBean = new ChangeTaskBean();
        }
        changeTaskBean.setChangeTheme(changeTheme);
        changeTaskBean.setChangeDescribe(changeDescribe);
        changeTaskBean.setResponsible(responsible);
        changeTaskBean.setChangeActivity2(changeActivity2);
        return changeTaskBean;
    }

    /***
     * 页面调用创建或编辑ChangeTaskBean对象
     *
     * @param changeTheme
     *            更改主题
     * @param changeDescribe
     *            更改说明
     * @param responsible
     *            责任人
     * @param chaneTaskBeanID
     *            编辑时ChangeTaskBean对象ID
     * @param oidArray
     *            创建及编辑页面中所有ChangeTaskBean对象ID
     * @return
     */
    public static String createEditChangeTaskBean(String changeTheme, String changeDescribe, String responsible, String changeActivity2, String chaneTaskBeanID, String allDatas) {
        try {
            if (!RemoteMethodServer.ServerFlag) {
                Class<?>[] classArray = new Class[]{String.class, String.class, String.class, String.class, String.class, String.class};
                Object[] paramArray = new Object[]{changeTheme, changeDescribe, responsible, changeActivity2, chaneTaskBeanID, allDatas};
                String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
                return (String) RemoteMethodServer.getDefault().invoke(methodName, CLASSNAME, null, classArray, paramArray);
            } else {
                ChangeTaskBean changeTaskBean = createChangeTaskBean(changeTheme, changeDescribe, responsible, changeActivity2, chaneTaskBeanID);
                if (changeTaskBean != null) {
                    JSONObject returnJSON = null;
                    if (PIStringUtils.isNotNull(allDatas)) {
                        returnJSON = new JSONObject(allDatas);
                    } else {
                        returnJSON = new JSONObject();
                    }
                    String newOID = changeTaskBean.getOid().toString();
                    if (returnJSON.has(newOID)) {
                        returnJSON.remove(newOID);
                    }
                    JSONObject datasJSON = new JSONObject();
                    datasJSON.put(CHANGETHEME_COMPID, changeTaskBean.getChangeTheme());
                    datasJSON.put(CHANGEDESCRIBE_COMPID, changeTaskBean.getChangeDescribe());
                    datasJSON.put(RESPONSIBLE_COMPID, changeTaskBean.getResponsible());
                    datasJSON.put(CHANGEACTIVITY2_COMPID, changeTaskBean.getChangeActivity2());
                    returnJSON.put(newOID, datasJSON);
                    return createJSONObject(returnJSON.toJSONString(), null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return createJSONObject(null, e.getLocalizedMessage());
        }
        return createJSONObject(null, null);
    }

    /***
     * 页面调用判断输入的用户名是否存在系统
     *
     * @param parameter
     *            用户信息
     * @return
     */
    public static String searchUser(String parameter) {
        try {
            if (!RemoteMethodServer.ServerFlag) {
                Class<?>[] classArray = new Class[]{String.class};
                Object[] paramArray = new Object[]{parameter};
                String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
                return (String) RemoteMethodServer.getDefault().invoke(methodName, CLASSNAME, null, classArray, paramArray);
            } else {
                if (PIStringUtils.isNull(parameter)) {
                    return createJSONObject(null, "用户信息不能为空!");
                }
                Collection<WTPrincipal> datasArray = findWTUsers(parameter);
                if (datasArray.size() == 0) {
                    return createJSONObject(null, parameter + " 该用户在系统中不存在!");
                }
                StringBuilder sb = new StringBuilder();
                for (WTPrincipal principal : datasArray) {
                    if (principal instanceof WTUser) {
                        if (sb.length() > 0) {
                            sb.append(USER_KEYWORD);
                        }
                        sb.append(((WTUser) principal).getFullName());
                    }
                }
                if (sb.length() == 0) {
                    return createJSONObject(null, parameter + " 该用户在系统中不存在!");
                }
                return createJSONObject(sb.toString(), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return createJSONObject(null, e.getLocalizedMessage());
        }
    }

    /***
     * 页面调用处理数据添加至AffectedEndItemsTableBuilder动作
     *
     * @param itemOids
     *            对象OID集合
     * @param method
     *            调用方法
     * @return
     */
    public static String disposeAffectedEndItems(String itemOids, String methodName) {
        SessionContext previous = SessionContext.newContext();
        try {
            // 当前用户设置为管理员，用于忽略权限
            SessionHelper.manager.setAdministrator();

            if (!RemoteMethodServer.ServerFlag) {
                Class<?>[] classArray = new Class[]{String.class};
                Object[] paramArray = new Object[]{itemOids};
                return (String) RemoteMethodServer.getDefault().invoke(methodName, CLASSNAME, null, classArray, paramArray);
            } else {
                Method method = Class.forName(CLASSNAME).getMethod(methodName, String.class);
                // 调用静态方法
                return (String) method.invoke(null, itemOids);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return createJSONObject(null, e.getLocalizedMessage());
        } finally {
            SessionContext.setContext(previous);
        }
    }

    /***
     * 获取指定部件上最顶层复合要求的父件
     *
     * @param itemOids
     *            指定部件oid集合
     * @return
     */
    public static String collectAffectedEndItems(String itemOids) {
        if (PIStringUtils.isNull(itemOids)) {
            return createJSONObject(null, "对象id集合为空!");
        }

        try {
            // 子件集合
            Collection<WTPart> childArray = new HashSet<WTPart>();
            JSONArray jsonArray = new JSONArray(itemOids);
            for (int i = 0; i < jsonArray.length(); i++) {
                String oid = jsonArray.getString(i);
                if (PIStringUtils.isNull(oid)) {
                    continue;
                }
                if (oid.contains(WTPart.class.getName())) {
                    childArray.add((WTPart) ((new ReferenceFactory()).getReference(oid).getObject()));
                }
            }
            // 父件集合
            JSONArray parentArray = new JSONArray();
            if (childArray.size() > 0) {
                Collection<WTPart> topParentArray = new HashSet<WTPart>();
                ChangePartQueryUtils.batchQueryTopParentParts(childArray, topParentArray);
                for (WTPart parentPart : topParentArray) {
                    if (AccessControlHelper.manager.hasAccess(SessionHelper.manager.getPrincipal(), parentPart, AccessPermission.READ)) {
                        parentArray.put(PersistenceHelper.getObjectIdentifier(parentPart).toString());
                    }
                }
            }
            return checkAffectedEndItems(parentArray.toJSONString());
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return createJSONObject(null, e.getLocalizedMessage());
        }
    }

    /***
     * 批量检查对象是否符合产品要求
     *
     * @param itemOids
     *            对象OID集合
     * @return
     */
    public static String checkAffectedEndItems(String itemOids) {
        if (PIStringUtils.isNull(itemOids)) {
            return createJSONObject(null, "对象id集合为空!");
        }
        // 符合要求的对象
        JSONArray accordArray = new JSONArray();
        // 错误信息
        StringBuilder errorMsg = new StringBuilder();
        try {
            JSONArray jsonArray = new JSONArray(itemOids);
            for (int i = 0; i < jsonArray.length(); i++) {
                String partOid = jsonArray.getString(i);
                if (PIStringUtils.isNull(partOid)) {
                    continue;
                }
                WTPart part = (WTPart) ((new ReferenceFactory()).getReference(partOid).getObject());
                // 是否成品
                if (specificNode(part, "成品") || specificNode(part, "appo_cp")) {
                    // 判断‘产品状态’
                    String jdztValue = PIAttributeHelper.service.getValue(part, JDZT) == null ? "" : (String) PIAttributeHelper.service.getValue(part, JDZT);
                    if (("停止销售").equals(jdztValue) || ("停止生产").equals(jdztValue) || ("停止维护").equals(jdztValue)) {
                        if (errorMsg.length() != 0) {
                            errorMsg.append("\n");
                        }
                        errorMsg.append(part.getDisplayIdentity() + " 产品状态不能为‘停止销售’、‘停止维护’、‘停止生产’");
                        continue;
                    }
                    if ("OBSOLESCENCE".equals(part.getLifeCycleState().toString())) {
                        if (errorMsg.length() != 0) {
                            errorMsg.append("\n");
                        }
                        errorMsg.append(part.getDisplayIdentity() + " 生命周期状态不能为‘报废’");
                        continue;
                    }

                    accordArray.put(partOid);
                } else {
                    if (errorMsg.length() != 0) {
                        errorMsg.append("\n");
                    }
                    errorMsg.append(part.getDisplayIdentity() + " 非产品对象");
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return createJSONObject(null, e.getLocalizedMessage());
        }

        return createJSONObject(accordArray.toJSONString(), errorMsg.toString());
    }

    private static String createJSONObject(String resultDatas, String errorMsg) {
        JSONObject returnJSON = new JSONObject();
        try {
            returnJSON.put(RESULT_DATAS, resultDatas == null ? "" : resultDatas);
            returnJSON.put(ERROR_MESSAGE, errorMsg == null ? "" : errorMsg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return returnJSON.toJSONString();
    }

    /***
     * 产生对象批量设置状态为‘已发布’
     *
     * @param wtObject
     *            WTChangeOrder2
     * @return
     */
    public static Collection<WTPart> setReleaseState(WTObject wtObject) {
        Collection<WTPart> releasePartsArray = new HashSet<WTPart>();
        if (wtObject == null || !(wtObject instanceof WTChangeOrder2)) {
            return releasePartsArray;
        }

        try {
            WTChangeOrder2 ecn = (WTChangeOrder2) wtObject;
            // 获取ECN中所有ECA与受影响对象集合
            Map<ChangeActivityIfc, Collection<Changeable2>> beforeInfoMap = getChangeablesBeforeInfo(ecn);
            if (beforeInfoMap == null || beforeInfoMap.size() == 0) {
                return releasePartsArray;
            }
            // 获取ECN中所有ECA与产生对象集合
            Map<ChangeActivityIfc, Collection<Changeable2>> afterInfoMap = getChangeablesAfterInfo(ecn);
            if (afterInfoMap == null || afterInfoMap.size() == 0) {
                return releasePartsArray;
            }

            for (Map.Entry<ChangeActivityIfc, Collection<Changeable2>> beforeEntryMap : beforeInfoMap.entrySet()) {
                ChangeActivityIfc eca = beforeEntryMap.getKey();
                // 受影响对象集合
                Collection<Changeable2> beforeArray = beforeEntryMap.getValue();
                for (Changeable2 changeable2 : beforeArray) {
                    if (checkState((LifeCycleManaged) changeable2, ChangeConstants.RELEASED)) {
                        // 产生对象集合
                        Collection<Changeable2> afterInfoArray = afterInfoMap.get(eca);
                        for (Changeable2 afterObject : afterInfoArray) {
                            if (getNumber(afterObject).equals(getNumber(changeable2))) {

                                // 设置状态
                                PICoreHelper.service.setLifeCycleState((LifeCycleManaged) afterObject, ChangeConstants.RELEASED);
                                if (afterObject instanceof WTPart) {
                                    // 增加自动随签发布的逻辑，edit by cjt
                                    WTPart part = (WTPart) afterObject;
                                    // 获取部件下所有子件信息
                                    Collection<WTPart> partMultiwallStructure = ChangePartQueryUtils.getPartMultiwallStructure(part);
                                    for (WTPart childpart : partMultiwallStructure) {
                                        if (childpart.getState().toString().equalsIgnoreCase(ChangeConstants.ARCHIVED)) {
                                            // 设置状态
                                            PICoreHelper.service.setLifeCycleState((LifeCycleManaged) childpart, ChangeConstants.RELEASED);
                                            if (!releasePartsArray.contains(childpart)) {
                                                releasePartsArray.add(childpart);
                                            }

                                        }
                                    }
                                    // 收集
                                    releasePartsArray.add((WTPart) afterObject);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        return releasePartsArray;
    }

    /***
     * 获取对象编码
     *
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
     * 判断对象状态是否符合要求
     *
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
}
