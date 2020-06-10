package ext.appo.change.beans;

import wt.change2.WTChangeOrder2;
import wt.part.WTPart;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangeHistoryReportBean implements Serializable {

    //当前物料
    private WTPart part;
    //当前层数(0,1,2,...)
    private int level;
    //序号ID
    private String id = "";
    //父ID
    private String parentId = "";
    //物料编码
    private String partNumber = "";
    //物料名称
    private String partName = "";

    //变更版本记录(key:1,2,3...排序,Map<String, String>==key：before 变更前版本;after 变更后版本)
    private Map<String, Map<String, String>> changeVersionRecord = new HashMap<>();
    //关联ECN
    private Map<String, WTChangeOrder2> ecn = new HashMap<>();
    //ECN编号或修订(变更类型)
    private Map<String, String> updateType = new HashMap<>();
    //变更申请人
    private Map<String, String> ecnCreator = new HashMap<>();
    //申请时间
    private Map<String, String> ecnStartTime = new HashMap<>();
    //所属产品类别
    private Map<String, String> productType = new HashMap<>();
    //所属项目
    private Map<String, String> projectName = new HashMap<>();
    //ECN变更类型
    private Map<String, String> changeType = new HashMap<>();
    //变更原因
    private Map<String, String> changeReason = new HashMap<>();
    //变更阶段
    private Map<String, String> changePhase = new HashMap<>();
    //是否变更图纸
    private Map<String, String> isChangeDrawing = new HashMap<>();
    //变更说明
    private Map<String, String> changeDescription = new HashMap<>();
    //当前物料更改说明(更改详细描述)
    private Map<String, String> changeDetailedDescription = new HashMap<>();

    //BOM变更内容
    private Map<String, List<BOMChangeInfoBean>> bomChangeInfo = new HashMap<>();

    public String getParentId() {
        return parentId;
    }

    public WTPart getPart() {
        return part;
    }

    public int getLevel() {
        return level;
    }

    public String getId() {
        return id;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public String getPartName() {
        return partName;
    }

    public Map<String, Map<String, String>> getChangeVersionRecord() {
        return changeVersionRecord;
    }

    public Map<String, WTChangeOrder2> getEcn() {
        return ecn;
    }

    public Map<String, String> getUpdateType() {
        return updateType;
    }

    public Map<String, String> getEcnCreator() {
        return ecnCreator;
    }

    public Map<String, String> getEcnStartTime() {
        return ecnStartTime;
    }

    public Map<String, String> getProductType() {
        return productType;
    }

    public Map<String, String> getProjectName() {
        return projectName;
    }

    public Map<String, String> getChangeType() {
        return changeType;
    }

    public Map<String, String> getChangeReason() {
        return changeReason;
    }

    public Map<String, String> getChangePhase() {
        return changePhase;
    }

    public Map<String, String> getIsChangeDrawing() {
        return isChangeDrawing;
    }

    public Map<String, String> getChangeDescription() {
        return changeDescription;
    }

    public Map<String, String> getChangeDetailedDescription() {
        return changeDetailedDescription;
    }

    public Map<String, List<BOMChangeInfoBean>> getBomChangeInfo() {
        return bomChangeInfo;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setPart(WTPart part) {
        this.part = part;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public void setChangeVersionRecord(Map<String, Map<String, String>> changeVersionRecord) {
        this.changeVersionRecord = changeVersionRecord;
    }

    public void setEcn(Map<String, WTChangeOrder2> ecn) {
        this.ecn = ecn;
    }

    public void setUpdateType(Map<String, String> updateType) {
        this.updateType = updateType;
    }

    public void setEcnCreator(Map<String, String> ecnCreator) {
        this.ecnCreator = ecnCreator;
    }

    public void setEcnStartTime(Map<String, String> ecnStartTime) {
        this.ecnStartTime = ecnStartTime;
    }

    public void setProductType(Map<String, String> productType) {
        this.productType = productType;
    }

    public void setProjectName(Map<String, String> projectName) {
        this.projectName = projectName;
    }

    public void setChangeType(Map<String, String> changeType) {
        this.changeType = changeType;
    }

    public void setChangeReason(Map<String, String> changeReason) {
        this.changeReason = changeReason;
    }

    public void setChangePhase(Map<String, String> changePhase) {
        this.changePhase = changePhase;
    }

    public void setIsChangeDrawing(Map<String, String> isChangeDrawing) {
        this.isChangeDrawing = isChangeDrawing;
    }

    public void setChangeDescription(Map<String, String> changeDescription) {
        this.changeDescription = changeDescription;
    }

    public void setChangeDetailedDescription(Map<String, String> changeDetailedDescription) {
        this.changeDetailedDescription = changeDetailedDescription;
    }

    public void setBomChangeInfo(Map<String, List<BOMChangeInfoBean>> bomChangeInfo) {
        this.bomChangeInfo = bomChangeInfo;
    }

    @Override
    public String toString() {
        return "ChangeHistoryReportBean{" +
                "parentId='" + parentId + '\'' +
                ", part=" + part +
                ", level=" + level +
                ", id='" + id + '\'' +
                ", partNumber='" + partNumber + '\'' +
                ", partName='" + partName + '\'' +
                ", changeVersionRecord=" + changeVersionRecord +
                ", ecn=" + ecn +
                ", updateType=" + updateType +
                ", ecnCreator=" + ecnCreator +
                ", ecnStartTime=" + ecnStartTime +
                ", productType=" + productType +
                ", projectName=" + projectName +
                ", changeType=" + changeType +
                ", changeReason=" + changeReason +
                ", changePhase=" + changePhase +
                ", isChangeDrawing=" + isChangeDrawing +
                ", changeDescription=" + changeDescription +
                ", changeDetailedDescription=" + changeDetailedDescription +
                ", bomChangeInfo=" + bomChangeInfo +
                '}';
    }
}
