package ext.appo.change.beans;

import wt.change2.WTChangeOrder2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//历史事务性任务bean
public class EcnChangeTaskBean implements Serializable {

    //ECN
    private WTChangeOrder2 ecn;
    private String ecnNumber = "";//ECN编号
    private String changeDescription = "";//ECN变更原因说明

    //关联的事务性任务eca
    List<ChangeTaskInfoBean> changeTaskBeans=new ArrayList<>();

    public WTChangeOrder2 getEcn() {
        return ecn;
    }

    public String getEcnNumber() {
        return ecnNumber;
    }

    public String getChangeDescription() {
        return changeDescription;
    }

    public List<ChangeTaskInfoBean> getChangeTaskBeans() {
        return changeTaskBeans;
    }

    public void setEcn(WTChangeOrder2 ecn) {
        this.ecn = ecn;
    }

    public void setEcnNumber(String ecnNumber) {
        this.ecnNumber = ecnNumber;
    }

    public void setChangeDescription(String changeDescription) {
        this.changeDescription = changeDescription;
    }

    public void setChangeTaskBeans(List<ChangeTaskInfoBean> changeTaskBeans) {
        this.changeTaskBeans = changeTaskBeans;
    }

    @Override
    public String toString() {
        return "EcnChangeTaskBean{" +
                "ecn=" + ecn +
                ", ecnNumber='" + ecnNumber + '\'' +
                ", changeDescription='" + changeDescription + '\'' +
                ", changeTaskBeans=" + changeTaskBeans +
                '}';
    }
}
