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
}
