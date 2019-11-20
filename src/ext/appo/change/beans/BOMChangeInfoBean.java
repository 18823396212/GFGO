package ext.appo.change.beans;

import java.io.Serializable;

public class BOMChangeInfoBean implements Serializable {

    //变更类型
    private String changeType="";
    //物料编码
    private String number="";
    // 名称
    private String name = "";
    //规格描述
    private String specification="";
    //位号
    private String placeNumber="";
    //数量
    private String quantit="";
    //替代料
    private String replacePart="";

    public String getChangeType() {
        return changeType;
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public String getSpecification() {
        return specification;
    }

    public String getPlaceNumber() {
        return placeNumber;
    }

    public String getQuantit() {
        return quantit;
    }

    public String getReplacePart() {
        return replacePart;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public void setPlaceNumber(String placeNumber) {
        this.placeNumber = placeNumber;
    }

    public void setQuantit(String quantit) {
        this.quantit = quantit;
    }

    public void setReplacePart(String replacePart) {
        this.replacePart = replacePart;
    }
}
