package ext.appo.change.beans;

import java.io.Serializable;
import java.util.*;

public class BOMChangeInfoBean implements Serializable {

    //变更类型
    private Set changeType=new HashSet();

    //物料编码
    private String number="";
    // 名称
    private String name = "";
    //规格描述(子料)
    private String specification="";
    //位号(key：before 变更前;after 变更后)
    private Map<String,String> placeNumber=new HashMap<>();
    //数量(key：before 变更前;after 变更后)
    private Map<String,String> quantit=new HashMap<>();
    //替代料(key：delete 删除的替代料;add 新增的替代料)
    private Map<String,List<String>> replacePartNumbers=new HashMap<>();
    //规格描述(父件。key：before 变更前;after 变更后)
    private Map<String,String> parentSpecification=new HashMap<>();

    public Set getChangeType() { return changeType; }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public String getSpecification() {
        return specification;
    }

    public Map<String,String> getPlaceNumber() {
        return placeNumber;
    }

    public Map<String,String> getQuantit() {
        return quantit;
    }

    public Map<String,List<String>> getReplacePartNumbers() { return replacePartNumbers; }

    public Map<String, String> getParentSpecification() { return parentSpecification; }

    public void setChangeType(Set changeType) {
        this.changeType = changeType;
    }

    public void setNumber(String number) { this.number = number; }

    public void setName(String name) {
        this.name = name;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public void setPlaceNumber(Map<String,String> placeNumber) {
        this.placeNumber = placeNumber;
    }

    public void setQuantit(Map<String,String> quantit) {
        this.quantit = quantit;
    }

    public void setReplacePartNumbers(Map<String,List<String>> replacePartNumbers) {this.replacePartNumbers = replacePartNumbers;}

    public void setParentSpecification(Map<String, String> parentSpecification) { this.parentSpecification = parentSpecification; }

    @Override
    public String toString() {
        return "BOMChangeInfoBean{" +
                "changeType=" + changeType +
                ", number='" + number + '\'' +
                ", name='" + name + '\'' +
                ", specification='" + specification + '\'' +
                ", placeNumber=" + placeNumber +
                ", quantit=" + quantit +
                ", replacePartNumbers=" + replacePartNumbers +
                ", parentSpecification=" + parentSpecification +
                '}';
    }
}
