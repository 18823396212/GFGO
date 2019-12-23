package ext.appo.change.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PartUpdateInfoBean implements Serializable {

    //ECN信息
    //ECN编码
    private String ecnNumber = "";
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

    //受影响部件信息
    // 受影响母件编号
    private String effectObjectNumber = "";
    // 受影响母件名称
    private String effectObjectName = "";
    // 受影响母件版本
    private String effectObjectVersion = "";
    // 受影响母件状态
    private String effectObjectState = "";
    //更改详细描述
    private String changeDetailedDescription="";
    // 在制数量
    private String inProcessQuantities = "";
    // 在制处理措施
    private String processingMeasures = "";
    // 在途数量
    private String onthewayQuantity = "";
    // 在途处理措施
    private String onthewayTreatmentMeasure = "";
    // 库存数量
    private String stockQuantity = "";
    // 库存处理措施
    private String stockTreatmentMeasure = "";
    // 已出货成品处理措施
    private String finishedHandleMeasures = "";
    //受影响母件变更类型
    private String effectObjectChangeType="";
    //完成时间
    private String expectDate="";


    public String getEcnNumber() {
        return ecnNumber;
    }

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

    public String getEffectObjectNumber() {
        return effectObjectNumber;
    }

    public String getEffectObjectName() {
        return effectObjectName;
    }

    public String getEffectObjectVersion() {
        return effectObjectVersion;
    }

    public String getEffectObjectState() {
        return effectObjectState;
    }

    public String getChangeDetailedDescription() {
        return changeDetailedDescription;
    }

    public String getInProcessQuantities() {
        return inProcessQuantities;
    }

    public String getProcessingMeasures() {
        return processingMeasures;
    }

    public String getOnthewayQuantity() {
        return onthewayQuantity;
    }

    public String getOnthewayTreatmentMeasure() {
        return onthewayTreatmentMeasure;
    }

    public String getStockQuantity() {
        return stockQuantity;
    }

    public String getStockTreatmentMeasure() {
        return stockTreatmentMeasure;
    }

    public String getFinishedHandleMeasures() {
        return finishedHandleMeasures;
    }

    public String getEffectObjectChangeType() {
        return effectObjectChangeType;
    }

    public String getExpectDate() {
        return expectDate;
    }

    public void setEcnNumber(String ecnNumber) {
        this.ecnNumber = ecnNumber;
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

    public void setEffectObjectNumber(String effectObjectNumber) {
        this.effectObjectNumber = effectObjectNumber;
    }

    public void setEffectObjectName(String effectObjectName) {
        this.effectObjectName = effectObjectName;
    }

    public void setEffectObjectVersion(String effectObjectVersion) {
        this.effectObjectVersion = effectObjectVersion;
    }

    public void setEffectObjectState(String effectObjectState) {
        this.effectObjectState = effectObjectState;
    }

    public void setChangeDetailedDescription(String changeDetailedDescription) {
        this.changeDetailedDescription = changeDetailedDescription;
    }

    public void setInProcessQuantities(String inProcessQuantities) {
        this.inProcessQuantities = inProcessQuantities;
    }

    public void setProcessingMeasures(String processingMeasures) {
        this.processingMeasures = processingMeasures;
    }

    public void setOnthewayQuantity(String onthewayQuantity) {
        this.onthewayQuantity = onthewayQuantity;
    }

    public void setOnthewayTreatmentMeasure(String onthewayTreatmentMeasure) {
        this.onthewayTreatmentMeasure = onthewayTreatmentMeasure;
    }

    public void setStockQuantity(String stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public void setStockTreatmentMeasure(String stockTreatmentMeasure) {
        this.stockTreatmentMeasure = stockTreatmentMeasure;
    }

    public void setFinishedHandleMeasures(String finishedHandleMeasures) {
        this.finishedHandleMeasures = finishedHandleMeasures;
    }

    public void setEffectObjectChangeType(String effectObjectChangeType) {
        this.effectObjectChangeType = effectObjectChangeType;
    }

    public void setExpectDate(String expectDate) {
        this.expectDate = expectDate;
    }


    @Override
    public String toString() {
        return "PartUpdateInfoBean{" +
                ", ecnNumber='" + ecnNumber + '\'' +
                ", ecnCreator='" + ecnCreator + '\'' +
                ", ecnStartTime='" + ecnStartTime + '\'' +
                ", productType='" + productType + '\'' +
                ", projectName='" + projectName + '\'' +
                ", changeType='" + changeType + '\'' +
                ", changeReason='" + changeReason + '\'' +
                ", changePhase='" + changePhase + '\'' +
                ", isChangeDrawing='" + isChangeDrawing + '\'' +
                ", changeDescription='" + changeDescription + '\'' +
                ", effectObjectNumber='" + effectObjectNumber + '\'' +
                ", effectObjectName='" + effectObjectName + '\'' +
                ", effectObjectVersion='" + effectObjectVersion + '\'' +
                ", effectObjectState='" + effectObjectState + '\'' +
                ", changeDetailedDescription='" + changeDetailedDescription + '\'' +
                ", inProcessQuantities='" + inProcessQuantities + '\'' +
                ", processingMeasures='" + processingMeasures + '\'' +
                ", onthewayQuantity='" + onthewayQuantity + '\'' +
                ", onthewayTreatmentMeasure='" + onthewayTreatmentMeasure + '\'' +
                ", stockQuantity='" + stockQuantity + '\'' +
                ", stockTreatmentMeasure='" + stockTreatmentMeasure + '\'' +
                ", finishedHandleMeasures='" + finishedHandleMeasures + '\'' +
                ", effectObjectChangeType='" + effectObjectChangeType + '\'' +
                ", expectDate='" + expectDate + '\'' +
                '}';
    }
}
