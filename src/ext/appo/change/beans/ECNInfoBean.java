package ext.appo.change.beans;

import java.io.Serializable;

public class ECNInfoBean implements Serializable {

    //变更申请人
    private String ecnCreator = "";
    //申请时间
    private String ecnStartTime = "";
    // 所属产品类别
    private String productType = "";
    // 所属项目
    private String projectName = "";
    //变更类型
    private String changeType="";
    // 变更原因
    private String changeReason = "";
    //变更阶段
    private String changePhase="";
    //是否变更图纸
    private String isChangeDrawing="";
    // 变更说明
    private String changeDescription = "";

    public String getEcnCreator() {
        return ecnCreator;
    }

    public String getEcnStartTime() {
        return ecnStartTime;
    }

    public String getProductType() {
        return productType;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getChangeType() {
        return changeType;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public String getChangePhase() {
        return changePhase;
    }

    public String getIsChangeDrawing() {
        return isChangeDrawing;
    }

    public String getChangeDescription() {
        return changeDescription;
    }

    public void setEcnCreator(String ecnCreator) {
        this.ecnCreator = ecnCreator;
    }

    public void setEcnStartTime(String ecnStartTime) {
        this.ecnStartTime = ecnStartTime;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    public void setChangePhase(String changePhase) {
        this.changePhase = changePhase;
    }

    public void setIsChangeDrawing(String isChangeDrawing) {
        this.isChangeDrawing = isChangeDrawing;
    }

    public void setChangeDescription(String changeDescription) {
        this.changeDescription = changeDescription;
    }
}
