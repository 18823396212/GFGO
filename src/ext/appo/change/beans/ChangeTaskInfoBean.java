package ext.appo.change.beans;

import ext.appo.change.models.TransactionTask;
import wt.change2.WTChangeActivity2;

//事务性任务报表bean
public class ChangeTaskInfoBean {
    //事务性任务eca
    private WTChangeActivity2 eca;
    //任务类型
    private String taskType = "";
    //任务主题
    private String taskTheme = "";
    //管理方式
    private String glfs = "";
    //任务描述
    private String changeDescribe = "";
    //计划完成时间
    private String needDate = "";
    //责任人
    private String responsible = "";
    //状态
    private String taskState = "";
    //任务单号
    private String taskNumber = "";
    //实际完成时间
    private String actualDate = "";
    //模型
    String taskOid = "";

    public WTChangeActivity2 getEca() {
        return eca;
    }

    public String getTaskType() {
        return taskType;
    }

    public String getTaskTheme() {
        return taskTheme;
    }

    public String getGlfs() {
        return glfs;
    }

    public String getChangeDescribe() {
        return changeDescribe;
    }

    public String getNeedDate() {
        return needDate;
    }

    public String getResponsible() {
        return responsible;
    }

    public String getTaskState() {
        return taskState;
    }

    public String getTaskNumber() {
        return taskNumber;
    }

    public String getActualDate() {
        return actualDate;
    }

    public String getTaskOid() {
        return taskOid;
    }

    public void setEca(WTChangeActivity2 eca) {
        this.eca = eca;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public void setTaskTheme(String taskTheme) {
        this.taskTheme = taskTheme;
    }

    public void setGlfs(String glfs) {
        this.glfs = glfs;
    }

    public void setChangeDescribe(String changeDescribe) {
        this.changeDescribe = changeDescribe;
    }

    public void setNeedDate(String needDate) {
        this.needDate = needDate;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }

    public void setTaskState(String taskState) {
        this.taskState = taskState;
    }

    public void setTaskNumber(String taskNumber) {
        this.taskNumber = taskNumber;
    }

    public void setActualDate(String actualDate) {
        this.actualDate = actualDate;
    }

    public void setTaskOid(String taskOid) {
        this.taskOid = taskOid;
    }

    @Override
    public String toString() {
        return "ChangeTaskInfoBean{" +
                "eca=" + eca +
                ", taskType='" + taskType + '\'' +
                ", taskTheme='" + taskTheme + '\'' +
                ", glfs='" + glfs + '\'' +
                ", changeDescribe='" + changeDescribe + '\'' +
                ", needDate='" + needDate + '\'' +
                ", responsible='" + responsible + '\'' +
                ", taskState='" + taskState + '\'' +
                ", taskNumber='" + taskNumber + '\'' +
                ", actualDate='" + actualDate + '\'' +
                ", taskOid=" + taskOid +
                '}';
    }
}
