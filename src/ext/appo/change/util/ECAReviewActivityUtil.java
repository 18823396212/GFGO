package ext.appo.change.util;

import com.ptc.core.meta.common.TypeIdentifier;
import ext.appo.change.workflow.ECNWorkflowUtil;
import ext.com.csm.CSMUtil;
import ext.com.iba.IBAUtil;
import ext.com.workflow.WorkflowUtil;
import ext.generic.config.ReviewprincipalRuleUtil;
import ext.generic.excel.bean.CellBean;
import ext.generic.excel.bean.RowBean;
import ext.generic.excel.bean.SheetBean;
import ext.generic.excel.bean.WorkbookBean;
import ext.generic.reviewprincipal.util.ReviewPrincipalUtil;
import ext.generic.workflow.util.ReviewActivityUtil;
import ext.generic.workflow.util.ReviewCacheUtil;
import ext.generic.workflow.util.WTPrincipalUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import wt.change2.Changeable2;
import wt.change2.WTChangeActivity2;
import wt.fc.*;
import wt.iba.value.IBAHolder;
import wt.inf.container.WTContained;
import wt.inf.container.WTContainer;
import wt.lifecycle.LifeCycleManaged;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.project.Role;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.team.Team;
import wt.type.TypedUtility;
import wt.util.WTException;
import wt.util.WTMessage;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.workflow.definer.WfAssignedActivityTemplate;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WfAssignedActivity;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;

public class ECAReviewActivityUtil {

    private static Logger LOGGER = LogR.getLogger(ECNWorkflowUtil.class.getName());
    private static String DEFAULTSHEET;
    private static String DEFAULTROW;
    private static String SEPARATE;
    public static WTProperties wtproperties;
    public static String RESOURCE;

    static {
        DEFAULTSHEET = "DEFAULT";
        DEFAULTROW = "default";
        SEPARATE = ";;;qqq";
        String configFile = "";
        RESOURCE = "ext.generic.workflow.resource.WorkflowResource";
        try {
            wtproperties = WTProperties.getLocalProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getSheetName(WTObject pbo) {
        return getSheetName(pbo, true);
    }

    public static RowBean getRowBean(ObjectReference self, WfProcess process, WTObject pbo, String sheetName) throws WTException {
        SheetBean sheetBean = getSheetBean(self, process, sheetName);
        if (sheetBean == null && !DEFAULTSHEET.equals(sheetName)) {
            sheetBean = getSheetBean(self, process, DEFAULTSHEET);
        }
        return getRowBean(sheetBean, pbo, true, true, true);
    }

    public static WorkbookBean getWorkBean(ObjectReference self, WfProcess process) {
        if (process == null) {
            process = WorkflowUtil.getProcess(self);
        }
        String processTemplate = process.getTemplate().getName();
        ReviewCacheUtil cacheUtil = new ReviewCacheUtil();
        return cacheUtil.setExcelCache(null, processTemplate);
    }

    public static SheetBean getSheetBean(ObjectReference self, WfProcess process, String sheetName) {
        WorkbookBean workBean = getWorkBean(self, process);
        return getSheetBean(workBean, sheetName);
    }

    public static CellBean getCellBean(WTObject pbo, ObjectReference self, WfProcess process, String activityName) throws WTException {
        new CellBean();
        new ReviewActivityUtil();
        String sheetName = getSheetName(pbo);
        RowBean rowBean = getRowBean(self, process, pbo, sheetName);
        return getCellBean(activityName, rowBean);
    }

    public static CellBean getCellBean(RowBean rowBean, String activityName) {
        new CellBean();
        return getCellBean(activityName, rowBean);
    }

    public static ArrayList<String> getNextActivity(ObjectReference self, WfProcess process, WTObject pbo, String augmentActivities, String activityName) throws WTException {
        new ReviewActivityUtil();
        String sheetName = getSheetName(pbo);
        RowBean rowBean = getRowBean(self, process, pbo, sheetName);
        return getNextActivity(rowBean, augmentActivities, activityName);
    }

    public static String getNextActivityToStr(ObjectReference self, WfProcess process, WTObject pbo, String augmentActivities, String activityName) throws WTException {
        StringBuilder activities = new StringBuilder();
        ArrayList<String> resultActivitie = getNextActivity(self, process, pbo, augmentActivities, activityName);
        for (String activity : resultActivitie) {
            if (activities.length() == 0) {
                activities = new StringBuilder(activity);
            } else {
                activities.append(SEPARATE).append(activity);
            }
        }
        return activities.toString();
    }

    public static Hashtable<String, CellBean> getExcelContentByActivity(ArrayList<String> activityNames, RowBean rowBean) {
        Hashtable<String, CellBean> celltable = new Hashtable();
        new CellBean();
        if (rowBean != null) {
            List<CellBean> cellBeans = rowBean.getCellList();
            if (cellBeans != null) {
                for (CellBean cellBean : cellBeans) {
                    String evActivityName = cellBean.getActivityName();
                    if (activityNames.contains(evActivityName)) {
                        celltable.put(evActivityName, cellBean);
                    }
                }
            }
        }
        LOGGER.debug("    current cell contents: " + celltable.size());
        return celltable;
    }

    public static void getInitialUsers(WTObject pbo, ObjectReference self, String augmentActivities, String activityName) throws WTException {
        boolean bool = SessionServerHelper.manager.isAccessEnforced();

        try {
            SessionServerHelper.manager.setAccessEnforced(false);

            //pbo为ECA，获取受影响对象的随机一个部件作为pbo；没有部件则取任意一个对象
            if (pbo instanceof WTChangeActivity2) {
                Collection<Changeable2> collection = ModifyUtils.getChangeablesBefore((WTChangeActivity2) pbo);
                LOGGER.info("=====getInitialUsers.collection: " + collection);
                for (Changeable2 changeable2 : collection) {
                    LOGGER.info("=====getInitialUsers.changeable2: " + changeable2);
                    if (changeable2 instanceof WTPart) {
                        pbo = (WTPart) changeable2;
                        break;
                    } else {
                        pbo = (WTObject) changeable2;
                    }
                }
            }
            WTContainer container = ((WTContained) pbo).getContainer();

            WfProcess process = WorkflowUtil.getProcess(self);
            String sheetName = getSheetName(pbo);
            RowBean rowBean = getRowBean(self, process, pbo, sheetName);
            Team team = WorkflowUtil.getTeam(process);
            WfAssignedActivity assignActivity = getWfAssignedActivity(process, activityName);
            String isAllActivties = null;
            if (assignActivity != null && assignActivity.getContext() != null) {
                isAllActivties = (String) assignActivity.getContext().getValue("isAllActivties");
            }

            ArrayList<String> nextActivitys = new ArrayList();
            if (isAllActivties == null) {
                nextActivitys = getNextActivity(self, process, pbo, augmentActivities, activityName);
            } else {
                String tempAllActivities;
                String[] tempAuge;
                if ("YES".equals(isAllActivties)) {
                    tempAllActivities = augmentActivities.replaceAll(">>AND<<", ";;;qqq");
                    tempAuge = tempAllActivities.split(";;;qqq");
                    if (tempAuge != null) {
                        Collections.addAll(nextActivitys, tempAuge);
                    }
                } else if (!"".equals(isAllActivties)) {
                    tempAllActivities = augmentActivities.replaceAll(">>AND<<", ";;;qqq");
                    tempAuge = tempAllActivities.split(";;;qqq");
                    List<String> tempAllArrys = Arrays.asList(tempAuge);
                    tempAuge = isAllActivties.split(";;;qqq");
                    if (tempAuge != null) {
                        for (String tempEv : tempAuge) {
                            if (tempAllArrys.contains(tempEv)) {
                                nextActivitys.add(tempEv);
                            }
                        }
                    }
                }
            }

            boolean isNeedUpdate = false;

            for (String activity : nextActivitys) {
                List<WTUser> users = new ArrayList();
                CellBean cellBean = getCellBean(activity, rowBean);
                Role role = getRoleByActivity(activity, process);
                if (role != null && team != null) {
                    Enumeration participants = team.getPrincipalTarget(role);
                    if (participants != null) {
                        while (participants.hasMoreElements()) {
                            WTPrincipal principal = ((WTPrincipalReference) participants.nextElement()).getPrincipal();
                            users = WTPrincipalUtil.getWTUserFromWTPrincipal((List) users, principal);
                        }
                    }
                }

                if ((users).size() == 0) {
                    List<WTUser> userTemp = new ArrayList();
                    userTemp = WTPrincipalUtil.getUserByCell(cellBean, userTemp, container, process, augmentActivities, rowBean);
                    if (userTemp.size() == 1) {
                        WTUser user = userTemp.get(0);
                        team.addPrincipal(role, user);
                        isNeedUpdate = true;
                    }
                }
            }

            if (isNeedUpdate) {
                ReviewPrincipalUtil.updateTeam(team, (LifeCycleManaged) pbo);
            }
        } finally {
            SessionServerHelper.manager.setAccessEnforced(bool);
        }

    }

    public static void checkUsersOnlyToCommit(WTObject pbo, ObjectReference self, String augmentActivities, String activityName) throws WTException {
        boolean bool = SessionServerHelper.manager.isAccessEnforced();

        try {
            SessionServerHelper.manager.setAccessEnforced(false);
            WTContainer container = ((WTContained) pbo).getContainer();
            WfProcess process = WorkflowUtil.getProcess(self);
            String sheetName = getSheetName(pbo);
            RowBean rowBean = getRowBean(self, process, pbo, sheetName);
            Team team = WorkflowUtil.getTeam(process);
            ArrayList<String> nextActivitys = new ArrayList();
            WfAssignedActivity assignActivity = getWfAssignedActivity(process, activityName);
            if (assignActivity == null) {
                throw new WTException(WTMessage.getLocalizedMessage(RESOURCE, "ACTIVITY_ERROR_CONFIG", new Object[]{activityName}, SessionHelper.getLocale()));
            }

            String isAllActivties = (String) assignActivity.getContext().getValue("isAllActivties");
            if (isAllActivties == null) {
                nextActivitys = getNextActivity(self, process, pbo, augmentActivities, activityName);
            } else {
                String tempAllActivities;
                String[] tempAuge;
                if ("YES".equals(isAllActivties)) {
                    tempAllActivities = augmentActivities.replaceAll(">>AND<<", ";;;qqq");
                    tempAuge = tempAllActivities.split(";;;qqq");
                    if (tempAuge != null) {
                        Collections.addAll(nextActivitys, tempAuge);
                    }
                } else if (!"".equals(isAllActivties)) {
                    tempAllActivities = augmentActivities.replaceAll(">>AND<<", ";;;qqq");
                    tempAuge = tempAllActivities.split(";;;qqq");
                    List<String> tempAllArrys = Arrays.asList(tempAuge);
                    tempAuge = isAllActivties.split(";;;qqq");
                    if (tempAuge != null) {
                        for (String tempEv : tempAuge) {
                            if (tempAllArrys.contains(tempEv)) {
                                nextActivitys.add(tempEv);
                            }
                        }
                    }
                }
            }

            StringBuffer sbf = new StringBuffer();
            for (String activity : nextActivitys) {
                List<WTUser> users = new ArrayList();
                CellBean cellBean = getCellBean(activity, rowBean);
                Role role = getRoleByActivity(activity, process);
                if (role != null && team != null) {
                    Enumeration participants = team.getPrincipalTarget(role);
                    if (participants != null) {
                        while (participants.hasMoreElements()) {
                            WTPrincipal principal = ((WTPrincipalReference) participants.nextElement()).getPrincipal();
                            users = WTPrincipalUtil.getWTUserFromWTPrincipal((List) users, principal);
                        }
                    }

                    if (users.size() == 0) {
                        if (cellBean == null) {
                            sbf.append(WTMessage.getLocalizedMessage(RESOURCE, "6", new Object[]{activityName}, SessionHelper.getLocale())).append("\n");
                        }

                        sbf.append(WTMessage.getLocalizedMessage(RESOURCE, "8", new Object[]{activity}, SessionHelper.getLocale())).append("\n");
                    } else if (users.size() > 1) {
                        boolean isNotice = cellBean.isNotice();
                        boolean isSign = cellBean.isSign();
                        if (!isNotice && !isSign) {
                            sbf.append(WTMessage.getLocalizedMessage(RESOURCE, "9", new Object[]{activity}, SessionHelper.getLocale())).append("\n");
                        }
                    }
                }
            }

            if (sbf.length() > 0) {
                throw new WTException(sbf.toString());
            }
        } finally {
            SessionServerHelper.manager.setAccessEnforced(bool);
        }

    }

    public static boolean isNextActivity(String augmentActivities, String activName) {
        String[] augmentActs = augmentActivities.split(">>AND<<");

        for (String augmentActivity : augmentActs) {
            LOGGER.debug("  augmentActivity: " + augmentActivity);
            if (augmentActivity.contains(";;;qqq")) {
                String[] activity = augmentActivity.split(";;;qqq");

                for (String s : activity) {
                    if (s.equals(activName)) {
                        return true;
                    }
                }
            } else if (augmentActivity.equals(activName)) {
                return true;
            }
        }

        return false;
    }

    private static String getSheetName(WTObject pbo, boolean isDefault) {
        if (isDefault) {
            return DEFAULTSHEET;
        } else if (pbo instanceof WTContained) {
            WTContained contained = (WTContained) pbo;
            return contained.getContainerName();
        } else {
            return "";
        }
    }

    private static SheetBean getSheetBean(WorkbookBean workBean, String sheetName) {
        SheetBean sheetBean = null;
        if (workBean != null) {
            Map<String, SheetBean> sheetBeans = workBean.getSheetMap();
            if (sheetBeans != null && sheetBeans.size() > 0) {
                sheetBean = sheetBeans.get(sheetName);
            }
        }
        return sheetBean;
    }

    @SuppressWarnings("deprecation")
    private static RowBean getRowBean(SheetBean sheetBean, WTObject pbo, boolean bigTypeNeed, boolean middleTypeNeed, boolean smallTypeNeed) throws WTException {
        RowBean rowBean = null;
        RowBean defRow = null;
        if (sheetBean != null) {

            //pbo为ECA，获取受影响对象的随机一个部件作为pbo
            LOGGER.info("=====getRowBean.pbo: " + pbo);
            if (pbo instanceof WTChangeActivity2) {
                Collection<Changeable2> collection = ModifyUtils.getChangeablesBefore((WTChangeActivity2) pbo);
                LOGGER.info("=====getRowBean.collection: " + collection);
                for (Changeable2 changeable2 : collection) {
                    LOGGER.info("=====getRowBean.changeable2: " + changeable2);
                    if (changeable2 instanceof WTPart) {
                        pbo = (WTPart) changeable2;
                        break;
                    } else {
                        pbo = (WTObject) changeable2;
                    }
                }
            }

            List<RowBean> rowBeans = sheetBean.getAllRows();
            if (rowBeans != null) {
                String bigType = DEFAULTROW;
                String middleType = "";
                String disName;
                if (bigTypeNeed) {
                    TypeIdentifier typeidentifier = TypedUtility.getTypeIdentifier(pbo);
                    Locale locale = SessionHelper.manager.getLocale();
                    disName = TypedUtility.getLocalizedTypeName(pbo, Locale.CHINA);
                    bigType = disName;
                    LOGGER.debug(">>>>>>>>>>>>>>get pbo bigType:" + disName);
                }

                int pos;
                if (middleTypeNeed) {
                    if (pbo instanceof WTPart) {
                        WTPart part = (WTPart) pbo;
                        String prefix = "";
                        prefix = ReviewprincipalRuleUtil.getInstance().getClassificationRule();
                        LOGGER.debug(">>>>>>>>> prefix:" + prefix);
                        if ("ClassNodeNameBefore_".equals(prefix)) {
                            disName = CSMUtil.getOneLastClfNodeDisplayNameByWTPart(part);
                            if (disName != null && disName.length() > 0) {
                                pos = disName.indexOf("_");
                                if (pos <= 0) {
                                    throw new WTException(WTMessage.getLocalizedMessage(RESOURCE, "10", new Object[0], SessionHelper.getLocale()));
                                }

                                middleType = disName.substring(0, pos);
                            }
                        } else if ("ClassNodeInternalName".equals(prefix)) {
                            disName = CSMUtil.getOneLastClfNodeInternalNameByWTPart(part);
                            if (disName == null || disName.length() <= 0) {
                                throw new WTException(WTMessage.getLocalizedMessage(RESOURCE, "11", new Object[0], SessionHelper.getLocale()));
                            }

                            middleType = disName;
                        } else if ("ClassNodeName".equals(prefix)) {
                            disName = CSMUtil.getOneLastClfNodeDisplayNameByWTPart(part);
                            if (disName != null && disName.length() > 0) {
                                middleType = disName;
                            }
                        }
                    }

                    LOGGER.debug(">>>>>>>>>>>>>>get pbo middleType:" + middleType);
                }

                if (smallTypeNeed) {
                    ReferenceFactory rf = new ReferenceFactory();
                    ObjectIdentifier objId = ObjectIdentifier.newObjectIdentifier(rf.getReferenceString(pbo));
                    Class pboClass = pbo.getClass();

                    for (pos = 0; pos < rowBeans.size(); ++pos) {
                        RowBean evRowBean = rowBeans.get(pos);
                        String evBigType = evRowBean.getBigType();
                        String evMidType = evRowBean.getMiddleType();
                        if (bigType.equals(evBigType) && middleType.equals(evMidType)) {
                            Map<String, String> ibaValues = evRowBean.getIbaValue();
                            if (ibaValues.size() == 0) {
                                defRow = evRowBean;
                                LOGGER.debug(">>>>>>>>>>>>>>get pbo defRow:" + evRowBean.toString());
                            } else {
                                Set<String> keySets = ibaValues.keySet();
                                Iterator<String> iter = keySets.iterator();
                                boolean samllTypeflag = true;

                                while (iter.hasNext()) {
                                    String key = iter.next();
                                    String value = ibaValues.get(key);
                                    String realValue;
                                    Object[] obj;
                                    if ("number".equals(key)) {
                                        realValue = "";
                                        obj = (Object[]) getAtttributeByClass(pboClass, "master>number", objId);
                                        if (obj != null && obj.length > 0 && obj[0] instanceof String) {
                                            realValue = (String) obj[0];
                                        }

                                        if (!value.equals(realValue)) {
                                            samllTypeflag = false;
                                            break;
                                        }
                                    } else if ("name".equals(key)) {
                                        realValue = "";
                                        obj = (Object[]) getAtttributeByClass(pboClass, "master>name", objId);
                                        if (obj != null && obj.length > 0 && obj[0] instanceof String) {
                                            realValue = (String) obj[0];
                                        }

                                        if (!value.equals(realValue)) {
                                            samllTypeflag = false;
                                            break;
                                        }
                                    } else if ("source".equals(key)) {
                                        if (pbo instanceof WTPart) {
                                            WTPart part = (WTPart) pbo;
                                            String source = part.getSource().getDisplay();
                                            source = source == null ? "" : source.trim();
                                            if (value.equals(source)) {
                                                continue;
                                            }

                                            samllTypeflag = false;
                                            break;
                                        }

                                        samllTypeflag = false;
                                        break;
                                    } else if ("view".equals(key)) {
                                        if (pbo instanceof WTPart) {
                                            WTPart part = (WTPart) pbo;
                                            if (value.equals(part.getViewName())) {
                                                continue;
                                            }

                                            samllTypeflag = false;
                                            break;
                                        }

                                        samllTypeflag = false;
                                        break;
                                    } else {
                                        realValue = "";

                                        try {
                                            realValue = (String) IBAUtil.getIBAValue((IBAHolder) pbo, key);
                                            realValue = realValue == null ? "" : realValue.trim();
                                        } catch (RemoteException var25) {
                                            var25.printStackTrace();
                                        }

                                        if (!value.equals(realValue)) {
                                            samllTypeflag = false;
                                            break;
                                        }
                                    }
                                }

                                if (samllTypeflag) {
                                    LOGGER.debug(">>>>>>>>>>>>>>>读到匹配行");
                                    rowBean = evRowBean;
                                    break;
                                }
                            }
                        }
                    }
                }

                if (rowBean == null) {
                    if (defRow != null) {
                        rowBean = defRow;
                        LOGGER.debug(">>>>>>>>>使用满足大类和中类的默认行>>>>>");
                    } else {
                        for (RowBean evRowBean : rowBeans) {
                            disName = evRowBean.getBigType();
                            String evMidType = evRowBean.getMiddleType();
                            if (DEFAULTROW.equals(disName) && "".equals(evMidType)) {
                                rowBean = evRowBean;
                                break;
                            }
                        }
                    }
                }

                LOGGER.debug(">>>>>>>>>>>>rowBean: " + rowBean);
            }
        }
        return rowBean;
    }

    @SuppressWarnings("deprecation")
    private static Object getAtttributeByClass(Class pboClass, String conditionStr, ObjectIdentifier objId) throws WTException {
        String oid = objId.toString();
        int lastIndex = oid.lastIndexOf(":");
        String id = oid.substring(lastIndex + 1);
        QuerySpec qs = new QuerySpec();
        int i = qs.addClassList(pboClass, false);
        qs.appendSelectAttribute(conditionStr, i, false);
        SearchCondition sc;
        if (oid.startsWith("O")) {
            sc = new SearchCondition(pboClass, "thePersistInfo.theObjectIdentifier.id", "=", Long.parseLong(id));
            qs.appendSearchCondition(sc);
        } else {
            sc = new SearchCondition(pboClass, "iterationInfo.branchId", "=", Long.parseLong(id));
            qs.appendSearchCondition(sc);
        }

        QueryResult localQueryResult = PersistenceHelper.manager.find(qs);
        if (localQueryResult.hasMoreElements()) {
            return localQueryResult.nextElement();
        } else {
            return null;
        }
    }

    private static CellBean getCellBean(String activityName, RowBean rowBean) {
        CellBean resultCellBean = new CellBean();
        if (rowBean != null) {
            List<CellBean> cellBeans = rowBean.getCellList();
            if (cellBeans != null) {
                for (CellBean bean : cellBeans) {
                    String evActivityName = bean.getActivityName();
                    if (evActivityName.equals(activityName)) {
                        resultCellBean = bean;
                        break;
                    }
                }
            }
        }

        LOGGER.debug("    current cell content: " + resultCellBean);
        return resultCellBean;
    }

    private static ArrayList<String> getNextActivity(RowBean rowBean, String augmentActivities, String activityName) throws WTException {
        ArrayList<String> resultActivitie = new ArrayList();
        if (augmentActivities != null && !"".equals(augmentActivities)) {
            TreeMap<Integer, ArrayList<String>> treeMap = new TreeMap();
            Integer nowOrder = 0;
            if (rowBean != null) {
                List<CellBean> cellBeans = rowBean.getCellList();
                if (cellBeans != null) {
                    for (CellBean cellBean : cellBeans) {
                        String order = cellBean.getOrder();
                        String evActivityName = cellBean.getActivityName();
                        if (evActivityName != null && !"".equals(evActivityName)) {
                            Integer orderNum;

                            try {
                                orderNum = Integer.parseInt(order);
                            } catch (NumberFormatException var13) {
                                var13.printStackTrace();
                                throw new WTException(WTMessage.getLocalizedMessage(RESOURCE, "12", new Object[0], SessionHelper.getLocale()));
                            }

                            if (activityName.equals(evActivityName)) {
                                nowOrder = orderNum;
                            }

                            ArrayList<String> activities = treeMap.get(orderNum);
                            if (activities == null) {
                                activities = new ArrayList();
                            }

                            activities.add(evActivityName);
                            treeMap.put(orderNum, activities);
                        }
                    }
                }
            }

            if (treeMap.size() > 0) {
                Set<Integer> keySets = treeMap.keySet();
                Iterator<Integer> iter = keySets.iterator();
                boolean tempFlag = false;

                while (iter.hasNext()) {
                    Integer key = iter.next();
                    if (nowOrder == 0) {
                        tempFlag = true;
                    }

                    if (tempFlag) {
                        ArrayList<String> needActivities = treeMap.get(key);
                        resultActivitie = getNextActivityBack(needActivities, augmentActivities, resultActivitie);
                        if (resultActivitie.size() > 0) {
                            break;
                        }
                    }

                    if (nowOrder.equals(key)) {
                        tempFlag = true;
                    }
                }
            }
        }

        LOGGER.debug(">>>>>>>>>>>>>>next activities :" + resultActivitie);
        return resultActivitie;
    }

    private static ArrayList<String> getNextActivityBack(ArrayList<String> needActivities, String augmentActivities, ArrayList<String> resultActivitie) {
        if (resultActivitie == null) {
            return new ArrayList();
        } else {
            if (needActivities != null && needActivities.size() > 0) {
                for (String evActivityName : needActivities) {
                    boolean isResultNeed = augmentActivities.startsWith(evActivityName + ">>AND<<") || augmentActivities.startsWith(evActivityName + ";;;qqq") || augmentActivities.endsWith(";;;qqq" + evActivityName) || augmentActivities.endsWith(">>AND<<" + evActivityName) || augmentActivities.indexOf(";;;qqq" + evActivityName + ";;;qqq") > 0 || augmentActivities.indexOf(">>AND<<" + evActivityName + ";;;qqq") > 0 || augmentActivities.indexOf(";;;qqq" + evActivityName + ">>AND<<") > 0 || augmentActivities.indexOf(">>AND<<" + evActivityName + ">>AND<<") > 0;
                    if (isResultNeed) {
                        resultActivitie.add(evActivityName);
                    }
                }
            }

            return resultActivitie;
        }
    }

    @SuppressWarnings("deprecation")
    private static Enumeration getRoleByActivity(String activityName, Persistable template) throws WTException {
        Enumeration enums = null;
        QuerySpec qs = new QuerySpec(WfAssignedActivityTemplate.class);
        SearchCondition sc = new SearchCondition(WfAssignedActivityTemplate.class, "name", "=", activityName);
        qs.appendSearchCondition(sc);
        qs.appendAnd();
        sc = new SearchCondition(WfAssignedActivityTemplate.class, "parentTemplate.key", "=", template.getPersistInfo().getObjectIdentifier());
        qs.appendSearchCondition(sc);
        QueryResult qr = PersistenceHelper.manager.find(qs);
        if (qr != null && qr.hasMoreElements()) {
            WfAssignedActivityTemplate activityTemplate = (WfAssignedActivityTemplate) qr.nextElement();
            enums = activityTemplate.getRoles();
        }

        return enums;
    }

    public static Role getRoleByActivity(String activityName, WfProcess process) throws WTException {
        Role role = null;
        if (process != null && activityName != null) {
            Persistable template = process.getTemplate().getObject();
            Enumeration em = getRoleByActivity(activityName, template);
            if (em != null && em.hasMoreElements()) {
                role = (Role) em.nextElement();
            }
        }

        return role;
    }

    public static Role getRoleByActivity(String activityName, ObjectReference self) throws WTException {
        WfProcess process = WorkflowUtil.getProcess(self);
        return getRoleByActivity(activityName, process);
    }

    public static Hashtable<String, String> getAugumentTable(WTObject pbo, ObjectReference self, WfProcess process) throws WTException {
        Hashtable<String, String> table = new Hashtable();
        StringBuilder augmentRoles = new StringBuilder();
        StringBuilder augmentActivities = new StringBuilder();
        String sheetName = getSheetName(pbo);
        RowBean rowBean = getRowBean(self, process, pbo, sheetName);
        if (rowBean != null) {
            List<CellBean> cellBeans = rowBean.getCellList();

            for (CellBean cellBean : cellBeans) {
                String activityName = cellBean.getActivityName();
                if (augmentRoles.length() > 0) {
                    augmentRoles.append(SEPARATE).append(activityName);
                } else {
                    augmentRoles = new StringBuilder(activityName);
                }

                if (cellBean.isMustSelected()) {
                    augmentActivities.append(activityName).append(SEPARATE);
                }
            }
        } else {
            LOGGER.debug(">>>>>>rowBean is null");
        }

        table.put("augmentRoles", augmentRoles.toString());
        table.put("augmentActivities", augmentActivities.toString());
        return table;
    }

    private static WfAssignedActivity getWfAssignedActivity(WfProcess wfprocess, String activity) {
        if (wfprocess != null && activity != null && activity.trim().length() != 0) {
            try {
                QueryResult qr = wfprocess.getContainerNodes();

                while (qr.hasMoreElements()) {
                    Object obj = qr.nextElement();
                    if (obj instanceof WfAssignedActivity) {
                        WfAssignedActivity wfaa = (WfAssignedActivity) obj;
                        if (activity.equals(wfaa.getName().trim())) {
                            return wfaa;
                        }
                    }
                }
            } catch (WTException var5) {
                var5.getMessage();
            }

            return null;
        } else {
            return null;
        }
    }

    public static void checkSignRole(WTObject pbo, ObjectReference self, String augmentActivities, String activityName) throws WTException {
        boolean bool = SessionServerHelper.manager.isAccessEnforced();

        try {
            SessionServerHelper.manager.setAccessEnforced(false);
            WTContainer container = ((WTContained) pbo).getContainer();
            WfProcess process = WorkflowUtil.getProcess(self);
            String sheetName = getSheetName(pbo);
            RowBean rowBean = getRowBean(self, process, pbo, sheetName);
            Team team = WorkflowUtil.getTeam(process);
            ArrayList<String> nextActivitys = new ArrayList();
            WfAssignedActivity assignActivity = getWfAssignedActivity(process, activityName);
            if (assignActivity == null) {
                throw new WTException(WTMessage.getLocalizedMessage(RESOURCE, "ACTIVITY_ERROR_CONFIG", new Object[]{activityName}, SessionHelper.getLocale()));
            }

            String isAllActivties = (String) assignActivity.getContext().getValue("isAllActivties");
            if (isAllActivties == null) {
                nextActivitys = getNextActivity(self, process, pbo, augmentActivities, activityName);
            } else {
                String tempAllActivities;
                String[] tempAuge;
                if ("YES".equals(isAllActivties)) {
                    tempAllActivities = augmentActivities.replaceAll(">>AND<<", ";;;qqq");
                    tempAuge = tempAllActivities.split(";;;qqq");
                    if (tempAuge != null) {
                        Collections.addAll(nextActivitys, tempAuge);
                    }
                } else if (!"".equals(isAllActivties)) {
                    tempAllActivities = augmentActivities.replaceAll(">>AND<<", ";;;qqq");
                    tempAuge = tempAllActivities.split(";;;qqq");
                    List<String> tempAllArrys = Arrays.asList(tempAuge);
                    tempAuge = isAllActivties.split(";;;qqq");
                    if (tempAuge != null) {
                        for (String tempEv : tempAuge) {
                            if (tempAllArrys.contains(tempEv)) {
                                nextActivitys.add(tempEv);
                            }
                        }
                    }
                }
            }

            StringBuffer sbf = new StringBuffer("");

            for (String activity : nextActivitys) {
                List<WTUser> users = new ArrayList();
                CellBean cellBean = getCellBean(activity, rowBean);
                Role role = getRoleByActivity(activity, process);
                if (role != null && team != null) {
                    Enumeration participants = team.getPrincipalTarget(role);
                    if (participants != null) {
                        while (participants.hasMoreElements()) {
                            WTPrincipal principal = ((WTPrincipalReference) participants.nextElement()).getPrincipal();
                            users = WTPrincipalUtil.getWTUserFromWTPrincipal((List) users, principal);
                        }
                    }

                    boolean isSign = cellBean.isSign();
                    if (isSign) {
                        List<String> roles = cellBean.getRoleList();

                        for (String signRole : roles) {
                            boolean hasRoleUser = false;
                            LOGGER.debug("signRole=" + signRole);
                            if (StringUtils.isBlank(signRole)) {
                                LOGGER.debug("sign Role is Blank");
                            } else {
                                List<WTUser> roleUsers = WTPrincipalUtil.getActorsByRole(container, signRole);

                                for (WTUser user : users) {
                                    if (roleUsers.contains(user)) {
                                        hasRoleUser = true;
                                        break;
                                    }
                                }

                                if (!hasRoleUser) {
                                    Role r = Role.toRole(signRole);
                                    sbf.append("角色检查：活动节点“").append((String) activity).append("”缺少“").append(r.getDisplay()).append("”角色的会签者\n");
                                }
                            }
                        }
                    }
                }
            }

            if (sbf.length() > 0) {
                throw new WTException(sbf.toString());
            }
        } finally {
            SessionServerHelper.manager.setAccessEnforced(bool);
        }

    }

    public static void setWorkflowCurrentSubmitUser(ObjectReference self, String rolename) throws WTPropertyVetoException {
        try {
            WTPrincipal wtprincipal = SessionHelper.manager.getPrincipal();
            WTPrincipalReference wtprincipalreference = WTPrincipalReference.newWTPrincipalReference(wtprincipal);
            WfProcess process = WorkflowUtil.getProcess(self);
            if ("CREATOR".equalsIgnoreCase(rolename)) {
                process.setCreator(wtprincipalreference);
                PersistenceHelper.manager.save(process);
            } else {
                Team team = (Team) process.getTeamId().getObject();
                Role role = Role.toRole(rolename);
                Enumeration participants = team.getPrincipalTarget(role);

                while (participants != null && participants.hasMoreElements()) {
                    WTPrincipal principal = ((WTPrincipalReference) participants.nextElement()).getPrincipal();
                    team.deletePrincipalTarget(role, principal);
                }

                team.addPrincipal(role, wtprincipal);
                WTObject pbo = ReviewPrincipalUtil.getPBOByWfProcess(process);
                ReviewPrincipalUtil.updateTeam(team, (LifeCycleManaged) pbo);
            }
        } catch (WTException var9) {
            var9.printStackTrace();
        }

    }

}
