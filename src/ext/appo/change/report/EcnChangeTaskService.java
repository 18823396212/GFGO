package ext.appo.change.report;

import com.ptc.windchill.enterprise.change2.ChangeTaskRoleParticipantHelper;
import ext.appo.change.ModifyHelper;
import ext.appo.change.beans.ChangeTaskInfoBean;
import ext.appo.change.beans.EcnChangeTaskBean;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.models.TransactionTask;
import ext.appo.change.util.ModifyUtils;
import ext.appo.common.util.Excel2007Handler;
import ext.appo.ecn.constants.ChangeConstants;
import ext.lang.PIStringUtils;
import ext.pi.PIException;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PICoreHelper;
import wt.change2.*;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.*;
import wt.method.RemoteAccess;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfProcess;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static ext.appo.change.constants.ModifyConstants.*;

/**
 * 事务性任务查询方法
 */
public class EcnChangeTaskService implements RemoteAccess {
    /**
     * 申请人,ECN编号,变更原因说明（支持模糊查询）,任务状态-进行中,任务状态-已超期,任务状态-已关闭,责任人,任务主题（支持模糊查询）,期望完成日期-开始时间
     * ,期望完成日期-结束时间,实际完成日期-开始时间,实际完成日期-结束时间,受影响的产品编号,所属产品类别（支持模糊查询）,所属项目（支持模糊查询）
     */
    public static List<EcnChangeTaskBean> getSearchResult(String ecnCreator, String ecnNumber, String changeReasonDes, String taskState_inProgress, String taskState_overdue, String taskState_closed
            , String responsible, String taskTheme, String startNeedDate, String endNeedDate, String startActualDate, String endActualDate, String affectedProductNumber, String productType, String projectName) {
        boolean flag = SessionServerHelper.manager.setAccessEnforced(false); // 忽略权限
        List<EcnChangeTaskBean> resultList = new ArrayList<>();
        try {
            List<String> state = new ArrayList<>();
            if (taskState_inProgress != null && !"".equals(taskState_inProgress) && "checked".equals(taskState_inProgress)) {
                state.add(ModifyConstants.TASK_2);
            }
            if (taskState_overdue != null && !"".equals(taskState_overdue) && "checked".equals(taskState_overdue)) {
                state.add(ModifyConstants.TASK_3);
            }
            if (taskState_closed != null && !"".equals(taskState_closed) && "checked".equals(taskState_closed)) {
                state.add(ModifyConstants.TASK_4);
            }
            List<WTChangeOrder2> ecnList = getAllECN();
            for (WTChangeOrder2 changeOrder2 : ecnList) {
                //ECN关联的事务性任务eca
                List<ChangeTaskInfoBean> changeTaskBeans = new ArrayList<>();
                if (ecnCreator != null && !"".equals(ecnCreator)) {
                    if (!ecnCreator.equals(changeOrder2.getCreatorFullName())) {
                        continue;
                    }
                }
                if (ecnNumber != null && !"".equals(ecnNumber)) {
                    if (!ecnNumber.equals(changeOrder2.getNumber())) {
                        continue;
                    }
                }
                if (changeReasonDes != null && !"".equals(changeReasonDes)) {
                    if (!changeOrder2.getDescription().contains(changeReasonDes)) {
                        continue;
                    }
                }
                String sscpx = PIAttributeHelper.service.getDisplayValue(changeOrder2, "sscpx", Locale.CHINA);//所属产品类别
                if (productType != null && !"".equals(productType)) {
                    if (sscpx != null && !sscpx.contains(productType)) {
                        continue;
                    }
                }
                String ssxm = PIAttributeHelper.service.getDisplayValue(changeOrder2, "ssxm", Locale.CHINA);//所属项目
                if (projectName != null && !"".equals(projectName)) {
                    if (ssxm != null && !ssxm.contains(projectName)) {
                        continue;
                    }
                }
                //获取ECN中所有事务性任务ECA
                QueryResult ecaqr = ChangeHelper2.service.getChangeActivities(changeOrder2);
                while (ecaqr.hasMoreElements()) {
                    Object ecaobject = ecaqr.nextElement();
                    if (ecaobject instanceof WTChangeActivity2) {
                        WTChangeActivity2 eca = ((WTChangeActivity2) ecaobject);
                        //事务性任务eca
                        if (PICoreHelper.service.isType(eca, ChangeConstants.TRANSACTIONAL_CHANGEACTIVITY2) || PICoreHelper.service.isType(eca, TYPE_3)) {
                            ChangeTaskInfoBean changeTaskBean = new ChangeTaskInfoBean();
                            if (affectedProductNumber != null && !"".equals(affectedProductNumber)) {
                                Set<String> affectedNumber = new HashSet<>();
                                Collection<Changeable2> collection = ModifyUtils.getChangeablesBefore(eca);//获取受影响对象
                                for (Changeable2 changeable2 : collection) {
                                    affectedNumber.add(getNumber(changeable2));
                                }
                                if (!affectedNumber.contains(affectedProductNumber)) continue;
                            }
                            //获取ECA任务类型，管理方式对应值
                            TransactionTask task = ModifyHelper.service.queryTransactionTask(changeOrder2, eca, "");
                            String taskType = "";
                            String glfs = "";
                            if (task != null) {
                                taskType = task.getTaskType() == null ? "" : task.getTaskType();
                                glfs = task.getManagementStyle() == null ? "" : task.getManagementStyle();
                            }
                            String ecaTaskTheme = eca.getName() == null ? "" : eca.getName();//任务主题
                            if (taskTheme != null && !"".equals(taskTheme) && !ecaTaskTheme.contains(taskTheme)) {
                                continue;
                            }
                            String changeDescribe = eca.getDescription() == null ? "" : eca.getDescription();//任务描述
                            Object needDateObj = PIAttributeHelper.service.getValue(eca, ChangeConstants.COMPLETION_TIME);//期望完成时间
                            String needDate = needDateObj == null ? "" : (String) needDateObj;
                            if (!needDate.isEmpty()) {
                                boolean isDate = isBetweenDate(needDate, startNeedDate, endNeedDate);
                                if (!isDate) continue;
                            } else {
                                if (startNeedDate != null && !startNeedDate.isEmpty() && endNeedDate != null && !endNeedDate.isEmpty()) {
                                    continue;
                                }
                            }
                            String ecaResponsible = getAssigneeName(eca);//责任人
                            if (responsible != null && !"".equals(responsible) && !responsible.equals(ecaResponsible)) {
                                continue;
                            }
                            String taskState = getEcaState(eca);//状态
                            if (state != null && state.size() > 0 && !state.contains(taskState)) {
                                continue;
                            }
                            String taskNumber = eca.getNumber() == null ? "" : eca.getNumber();//任务单号
                            String actualDate = "";//实际完成时间
                            //查询ECA流程
                            QueryResult qr = WfEngineHelper.service.getAssociatedProcesses(eca, null, null);
                            while (qr.hasMoreElements()) {
                                WfProcess process = (WfProcess) qr.nextElement();
                                if (process != null && process.getEndTime() != null) {
                                    actualDate = process.getEndTime().toLocaleString();
                                }
                            }
                            if (!actualDate.isEmpty()) {
                                boolean isDate = isBetweenDate(actualDate, startActualDate, endActualDate);
                                if (!isDate) continue;
                            } else {
                                if (startActualDate != null && !startActualDate.isEmpty() && endActualDate != null && !endActualDate.isEmpty()) {
                                    continue;
                                }
                            }
                            changeTaskBean.setEca(eca);
                            changeTaskBean.setTaskType(taskType);
                            changeTaskBean.setTaskTheme(ecaTaskTheme);
                            changeTaskBean.setGlfs(glfs);
                            changeTaskBean.setChangeDescribe(changeDescribe);
                            changeTaskBean.setNeedDate(needDate);
                            changeTaskBean.setResponsible(ecaResponsible);
                            changeTaskBean.setTaskState(taskState);
                            changeTaskBean.setTaskNumber(taskNumber);
                            changeTaskBean.setActualDate(actualDate);

                            changeTaskBeans.add(changeTaskBean);
                        }
                    }
                }
                if (changeTaskBeans != null && changeTaskBeans.size() > 0) {
                    String number = changeOrder2.getNumber();//ECN编号
                    String changeDescription = changeOrder2.getDescription();//ECN变更原因说明
                    EcnChangeTaskBean ecnChangeTaskBean = new EcnChangeTaskBean();
                    ecnChangeTaskBean.setEcn(changeOrder2);
                    ecnChangeTaskBean.setEcnNumber(number);
                    ecnChangeTaskBean.setChangeDescription(changeDescription);
                    ecnChangeTaskBean.setChangeTaskBeans(changeTaskBeans);
                    resultList.add(ecnChangeTaskBean);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SessionServerHelper.manager.setAccessEnforced(flag); // 添加权限
        }
        return resultList;

    }

    //获取事务性任务(所有eca和未启动eca暂存的事务性任务)
    public static List<ChangeTaskInfoBean> getEcnChangeTask(WTChangeOrder2 changeOrder2) {
        List<ChangeTaskInfoBean> changeTaskInfoBeans = new ArrayList<>();
        boolean flag = SessionServerHelper.manager.setAccessEnforced(false); // 忽略权限
        try {
            //获取ECN中所有事务性任务ECA
            QueryResult ecaqr = ChangeHelper2.service.getChangeActivities(changeOrder2);
            while (ecaqr.hasMoreElements()) {
                Object ecaobject = ecaqr.nextElement();
                if (ecaobject instanceof WTChangeActivity2) {
                    WTChangeActivity2 eca = ((WTChangeActivity2) ecaobject);
                    //事务性任务eca
                    if (PICoreHelper.service.isType(eca, ModifyConstants.TYPE_3)) {
                        ChangeTaskInfoBean changeTaskBean = new ChangeTaskInfoBean();
                        //获取ECA任务类型，管理方式对应值
                        TransactionTask task = ModifyHelper.service.queryTransactionTask(changeOrder2, eca, "");
                        String taskType = "";
                        String glfs = "";
                        if (task != null) {
                            taskType = task.getTaskType() == null ? "" : task.getTaskType();
                            glfs = task.getManagementStyle() == null ? "" : task.getManagementStyle();
                        }
                        String ecaTaskTheme = eca.getName() == null ? "" : eca.getName();//任务主题
                        String changeDescribe = eca.getDescription() == null ? "" : eca.getDescription();//任务描述
                        Object needDateObj = PIAttributeHelper.service.getValue(eca, ChangeConstants.COMPLETION_TIME);//期望完成时间
                        String needDate = needDateObj == null ? "" : (String) needDateObj;
                        String ecaResponsible = getAssigneeName(eca);//责任人
                        String taskState = getEcaState(eca);//状态
                        String taskNumber = eca.getNumber() == null ? "" : eca.getNumber();//任务单号
                        String actualDate = "";//实际完成时间
                        //查询ECA流程
                        QueryResult qr = WfEngineHelper.service.getAssociatedProcesses(eca, null, null);
                        while (qr.hasMoreElements()) {
                            WfProcess process = (WfProcess) qr.nextElement();
                            if (process != null && process.getEndTime() != null) {
                                actualDate = process.getEndTime().toLocaleString();
                            }
                        }
                        changeTaskBean.setEca(eca);
                        changeTaskBean.setTaskType(taskType == null ? "" : taskType);
                        changeTaskBean.setTaskTheme(ecaTaskTheme == null ? "" : ecaTaskTheme);
                        changeTaskBean.setGlfs(glfs == null ? "" : glfs);
                        changeTaskBean.setChangeDescribe(changeDescribe == null ? "" : changeDescribe);
                        changeTaskBean.setNeedDate(needDate == null ? "" : needDate);
                        changeTaskBean.setResponsible(ecaResponsible == null ? "" : ecaResponsible);
                        changeTaskBean.setTaskState(taskState == null ? "" : taskState);
                        changeTaskBean.setTaskNumber(taskNumber == null ? "" : taskNumber);
                        changeTaskBean.setActualDate(actualDate == null ? "" : actualDate);
                        changeTaskInfoBeans.add(changeTaskBean);
                    }
                }
            }
            //获取暂存的事务性任务(不存在eca的事务性任务)
            Set<Persistable> result = ModifyHelper.service.queryPersistable(changeOrder2, LINKTYPE_3);
            for (Persistable persistable : result) {
                if (persistable instanceof TransactionTask) {
                    TransactionTask task = (TransactionTask) persistable;
                    String changeActivity2 = task.getChangeActivity2();
                    if (changeActivity2 != null && !changeActivity2.isEmpty()) {
                        continue;
                    }
                    ChangeTaskInfoBean changeTaskBean = new ChangeTaskInfoBean();
                    changeTaskBean.setTaskType(task.getTaskType() == null ? "" : task.getTaskType());
                    changeTaskBean.setTaskTheme(task.getChangeTheme() == null ? "" : task.getChangeTheme());
                    changeTaskBean.setGlfs(task.getManagementStyle() == null ? "" : task.getManagementStyle());
                    changeTaskBean.setChangeDescribe(task.getChangeDescribe() == null ? "" : task.getChangeDescribe());
                    if (task.getChangeTheme() != null && task.getChangeTheme().trim() != "") {
                        changeTaskBean.setNeedDate(task.getNeedDate() == null ? "" : task.getNeedDate());
                    }
                    changeTaskBean.setResponsible(task.getResponsible() == null ? "" : task.getResponsible());
                    changeTaskBean.setTaskOid(String.valueOf(task.getPersistInfo().getObjectIdentifier().getId()));
                    changeTaskBean.setTaskState(ModifyConstants.TASK_6);
                    changeTaskInfoBeans.add(changeTaskBean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SessionServerHelper.manager.setAccessEnforced(flag); // 添加权限
        }
        return changeTaskInfoBeans;
    }

    // 返回所有ecn
    public static List<WTChangeOrder2> getAllECN() throws WTException {

        List<WTChangeOrder2> resultList = new ArrayList<WTChangeOrder2>();
        QuerySpec qs = new QuerySpec(WTChangeOrder2.class);
        QueryResult qr = PersistenceHelper.manager.find(qs);

        while (qr.hasMoreElements()) {
            WTChangeOrder2 wtchangeorder2 = (WTChangeOrder2) qr.nextElement();

            resultList.add(wtchangeorder2);
        }

        return resultList;
    }

    // ECA工作负责人
    public static String getAssigneeName(WTChangeActivity2 eca) throws WTException {
        String assigneeName = "";
        Enumeration<?> roleem = ChangeTaskRoleParticipantHelper.getRoleParticipants(eca, ChangeConstants.ROLE_ASSIGNEE);
        while (roleem.hasMoreElements()) {
            Object object = roleem.nextElement();
            if (object instanceof ObjectReference) {
                object = ((ObjectReference) object).getObject();
            }
            WTPrincipal principal = (WTPrincipal) object;
            if (principal instanceof WTUser) {
                if (PIStringUtils.isNull(assigneeName)) {
                    assigneeName = ((WTUser) principal).getFullName();
                } else {
                    assigneeName = assigneeName + ";" + ((WTUser) principal).getFullName();
                }
            }
        }
        return assigneeName;
    }

    //获取ECA状态，转换为对应自定义常量
    public static String getEcaState(WTChangeActivity2 eca) throws PIException {
        String value = "";
        String state = eca.getState().toString();
        if (state.equalsIgnoreCase("CANCELLED") || state.equalsIgnoreCase("RESOLVED")) {
            //已取消,已完成（已关闭）
            value = ModifyConstants.TASK_4;
        } else {
            value = ModifyConstants.TASK_2;
            //是否已超期
            Object qwwcsj = PIAttributeHelper.service.getValue(eca, ChangeConstants.COMPLETION_TIME);
            if (qwwcsj != null) {
                String needDate = (String) qwwcsj;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                String dateString = simpleDateFormat.format(new Date());
                if (compareDate(dateString, needDate)) {
                    value = ModifyConstants.TASK_3;
                }
            }
        }
        return value;
    }

    // 比较时间,date1大于等于date2返回true！
    //String必须为"yyyy/MM/dd"格式
    public static boolean compareDate(String date1, String date2) {
        boolean flag = false;
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd");

        try {
            Date dt1 = df.parse(date1);
            Date dt2 = df.parse(date2);
            if (dt1.getTime() > dt2.getTime()) {
                flag = true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return flag;
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

    // 比较时间,date如果在startDate和endDate之间返回true！
    //转化为"yyyy/MM/dd"格式比较
    public static boolean isBetweenDate(String date, String startDate, String endDate) {
        boolean flag = false;
        try {
            date = date.replace("-", "/");
            startDate = startDate.replace("-", "/");
            endDate = endDate.replace("-", "/");
            DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
            Date dt1 = df.parse(date);
            if (startDate != null && !startDate.isEmpty()) {
                Date dt2 = df.parse(startDate);
                if (dt1.getTime() >= dt2.getTime()) {
                    flag = true;
                } else {
                    return false;
                }
            } else {
                flag = true;
            }
            if (endDate != null && !endDate.isEmpty()) {
                Date dt3 = df.parse(endDate);
                if (dt1.getTime() <= dt3.getTime()) {
                    flag = true;
                } else {
                    return false;
                }
            } else {
                flag = true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return flag;
    }

    //通过Oid获取对象
    public static Persistable getObjectByOid(String oid) {
        Persistable p = null;

        boolean flag = SessionServerHelper.manager.setAccessEnforced(false);// 忽略权限
        try {
            ReferenceFactory referencefactory = new ReferenceFactory();
            WTReference wtreference = referencefactory.getReference(oid);
            p = wtreference.getObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SessionServerHelper.manager.setAccessEnforced(flag);// 添加权限
        }
        return p;
    }

    /**
     * 读取事务性任务模板表
     *
     * @return
     * @throws IOException
     */
    public static List<ChangeTaskInfoBean> getChangeTaskTemplate() throws IOException {
        List<ChangeTaskInfoBean> changeTaskInfoBeans = new ArrayList<>();
        boolean flag = SessionServerHelper.manager.setAccessEnforced(false);// 忽略权限
        try {
            String codebase = WTProperties.getLocalProperties().getProperty("wt.codebase.location");
            String filePath = codebase + File.separator + "ext" + File.separator + "appo" + File.separator
                    + "change" + File.separator + "report" + File.separator + "ChangeTaskTemplate.xlsx";
            Excel2007Handler handler = new Excel2007Handler(filePath);
            handler.switchCurrentSheet(0);
            int rowCount = handler.getSheetRowCount();
            // System.out.println("产品线读表rowCount:"+rowCount);
            for (int i = 1; i < rowCount; i++) {
                ChangeTaskInfoBean changeTaskInfoBean = new ChangeTaskInfoBean();
                Integer[] flag1 = {2, 3, 4, 5, 6, 7, 8};
                Integer[] flag2 = new Integer[18];
                for (int j = 9; j < 27; j++) {
                    flag2[j - 9] = j;
                }
                String taskType = "";
                String glfs = "";
                Boolean isFlag1 = isContains(flag1, i);
                if (isFlag1) {
                    taskType = null2blank(handler.getStringValue(flag1[0], 0)); //任务类型
                    glfs = null2blank(handler.getStringValue(flag1[0], 2));  //管理方式
                } else {
                    taskType = null2blank(handler.getStringValue(i, 0)); //任务类型
                    glfs = null2blank(handler.getStringValue(i, 2));  //管理方式
                }
                Boolean isFlag2 = isContains(flag2, i);
                if (isFlag2) {
                    taskType = null2blank(handler.getStringValue(flag2[0], 0)); //任务类型
                }

                String taskTheme = null2blank(handler.getStringValue(i, 1));//任务主题

                changeTaskInfoBean.setTaskType(taskType == null ? "" : taskType);
                changeTaskInfoBean.setTaskTheme(taskTheme == null ? "" : taskTheme);
                changeTaskInfoBean.setGlfs(glfs == null ? "" : glfs);
                changeTaskInfoBean.setTaskState(TASK_1);
                changeTaskInfoBeans.add(changeTaskInfoBean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SessionServerHelper.manager.setAccessEnforced(flag);// 添加权限
        }
        return changeTaskInfoBeans;
    }

    /**
     * 并模板表和事务性任务ChangeTaskInfoBean,是否存在相同任务主题,存在则不显示模板表对应ChangeTaskInfoBean
     * changeTaskTemplateBeans:模板bean,changeTaskInfoBeans:事务性任务bean
     *
     * @param changeTaskTemplateBeans
     * @param changeTaskInfoBeans
     * @return
     */
    public static List<ChangeTaskInfoBean> mergeChangeTaskInfoBean(List<ChangeTaskInfoBean> changeTaskTemplateBeans, List<ChangeTaskInfoBean> changeTaskInfoBeans) {
        List<ChangeTaskInfoBean> changeTaskInfoBeanList = new ArrayList<>();

        if (changeTaskInfoBeans != null && changeTaskInfoBeans.size() > 0)
            changeTaskInfoBeanList.addAll(changeTaskInfoBeans);

        changeTask:
        for (ChangeTaskInfoBean changeTaskInfoBean : changeTaskTemplateBeans) {
            for (ChangeTaskInfoBean changeTaskInfoBean1 : changeTaskInfoBeanList) {
                if (changeTaskInfoBean.getTaskTheme().equals(changeTaskInfoBean1.getTaskTheme())) continue changeTask;
            }
            changeTaskInfoBeanList.add(changeTaskInfoBean);
        }

        return changeTaskInfoBeanList;
    }

    public static String null2blank(Object obj) {
        if (obj == null) {
            return "";
        } else {
            String tmp = obj.toString();
            return tmp.trim();
        }
    }

    //是否Int[]数组是否包含某个int
    public static Boolean isContains(Integer[] integers, Integer integer) {
        if (integers != null && integers.length > 0) {
            for (int i = 0; i < integers.length; i++) {
                if (integers[i] == integer) return true;
            }
        }
        return false;
    }
}
