package ext.appo.change.beans;

import java.io.Serializable;

public class AffectedParentPartsBean implements Serializable {

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
    //变更类型
    private String changeType="";
    //完成时间
    private String expectDate="";

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

    public String getChangeType() {
        return changeType;
    }

    public String getExpectDate() {
        return expectDate;
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

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public void setExpectDate(String expectDate) {
        this.expectDate = expectDate;
    }

    @Override
    public String toString() {
        return "AffectedParentPartsBean{" +
                "effectObjectNumber='" + effectObjectNumber + '\'' +
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
                ", changeType='" + changeType + '\'' +
                ", expectDate='" + expectDate + '\'' +
                '}';
    }
}
