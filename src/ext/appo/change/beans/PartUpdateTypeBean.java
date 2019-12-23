package ext.appo.change.beans;

import wt.change2.WTChangeOrder2;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PartUpdateTypeBean implements Serializable {

    //物料升版类型
    private String updateType="";
    //版本变化（大版本）(key：before 变更前版本;after 变更后版本)
    private Map<String,String> changeVersion=new HashMap<>();
    //物料编码
    private String partNumber="";
    //升版关联的ECN
    private WTChangeOrder2 ecn;

    private String view="";

    public String getUpdateType() {
        return updateType;
    }

    public Map<String, String> getChangeVersion() {
        return changeVersion;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public WTChangeOrder2 getEcn() {
        return ecn;
    }

    public String getView() {
        return view;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    public void setChangeVersion(Map<String, String> changeVersion) {
        this.changeVersion = changeVersion;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public void setEcn(WTChangeOrder2 ecn) {
        this.ecn = ecn;
    }

    public void setView(String view) {
        this.view = view;
    }

    @Override
    public String toString() {
        return "PartUpdateTypeBean{" +
                "updateType='" + updateType + '\'' +
                ", changeVersion=" + changeVersion +
                ", partNumber='" + partNumber + '\'' +
                ", ecn=" + ecn +
                ", view='" + view + '\'' +
                '}';
    }
}
