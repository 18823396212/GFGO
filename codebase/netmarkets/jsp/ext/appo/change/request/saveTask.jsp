<%@ page import="ext.appo.change.util.TransactionECAUtil" %>
<%@ page import="ext.appo.change.beans.ChangeTaskInfoBean" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="wt.change2.WTChangeOrder2" %>
<%@ page import="static ext.appo.change.report.BomChangeReport.getECNByNumber" %>
<%@ page import="ext.lang.PIStringUtils" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="wt.org.WTPrincipal" %>
<%@ page import="wt.session.SessionHelper" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%
    String ecnNumber = request.getParameter("ecn");
    List<String> taskCheckboxs = new ArrayList<>();
    String taskCheckbox = request.getParameter("taskCheckbox");
    if (PIStringUtils.isNotNull(taskCheckbox)) {
        JSONArray jsonArray = new JSONArray(taskCheckbox);
        for (int i = 0; i < jsonArray.length(); i++) {
            taskCheckboxs.add(jsonArray.getString(i));
        }
    }
    List<ChangeTaskInfoBean> ChangeTaskInfoBeans = new ArrayList<>();
    WTChangeOrder2 ecn = null;
    if (ecnNumber != null && taskCheckboxs != null && taskCheckboxs.size() > 0) {
        ecn = getECNByNumber(ecnNumber);
        for (int i = 0; i < taskCheckboxs.size(); i++) {
            ChangeTaskInfoBean changeTaskInfoBean = new ChangeTaskInfoBean();
            String flag = taskCheckboxs.get(i);
            String taskType = request.getParameter("taskType_" + flag);
            String taskTheme = request.getParameter("taskTheme_" + flag);
            String glfs = request.getParameter("glfs_" + flag);
            String changeDescribe = request.getParameter("changeDescribe_" + flag);
            String needDate = request.getParameter("needDate_" + flag);
            String taskOid = request.getParameter("taskOid_" + flag);
            String responsible = request.getParameter("responsible_" + flag + "$label$");
            changeTaskInfoBean.setTaskType(taskType == null ? "" : taskType);
            changeTaskInfoBean.setTaskTheme(taskTheme == null ? "" : taskTheme);
            changeTaskInfoBean.setGlfs(glfs == null ? "" : glfs);
            changeTaskInfoBean.setChangeDescribe(changeDescribe == null ? "" : changeDescribe);
            changeTaskInfoBean.setNeedDate(needDate == null ? "" : needDate);
            changeTaskInfoBean.setResponsible(responsible == null ? "" : responsible);
            if (taskOid != null && !taskOid.isEmpty()) {
                changeTaskInfoBean.setTaskOid(taskOid);
            }
            ChangeTaskInfoBeans.add(changeTaskInfoBean);
        }
        WTPrincipal principal = SessionHelper.manager.getPrincipal();
        SessionHelper.manager.setAdministrator();
        try {
            TransactionECAUtil.saveTask(ecn, ChangeTaskInfoBeans);
        } finally {
            SessionHelper.manager.setPrincipal(principal.getName());
            out.write("true");
        }
    }
    out.write("true");
%>
