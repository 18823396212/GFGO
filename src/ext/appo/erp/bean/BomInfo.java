package ext.appo.erp.bean;
import wt.part.WTPart;
import wt.part.WTPartUsageLink;



public class BomInfo {

    private  String parentNumber;//父项编码

    private  String parentName;//父项名称

    private String majorVersion;//父项大版本

    private String state;//父件状态

    private  String parentwlfl;//父项物料分类

    private  String view;//视图

    private String childNumber;//子项物料编码

    private  String childName;//子项物料名称

    private String weihao;//位号

    private String shuliang;//数量

    private String unit;//单位

    private String version;//子件大版本

    private String bomRemark;//BOM备注

    private String zdchdj;//最低存货等级

    private String sfxnj;//是否为工艺虚拟件

    private WTPartUsageLink link;

    private WTPart childPart;

    private  String childrenView;//子项视图

    public String getParentwlfl() { return parentwlfl; }

    public void setParentwlfl(String parentwlfl) { this.parentwlfl = parentwlfl; }

    public void setChildrenView(String childrenView) { this.childrenView = childrenView; }

    public String getChildrenView() { return childrenView; }

    public String getParentNumber() {
        return parentNumber;
    }

    public void setParentNumber(String parentNumber) {
        this.parentNumber = parentNumber;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }


    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }


    public String getWeihao() {
        return weihao;
    }

    public void setWeihao(String weihao) {
        this.weihao = weihao;
    }

    public String getShuliang() {
        return shuliang;
    }

    public void setShuliang(String shuliang) {
        this.shuliang = shuliang;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBomRemark() {
        return bomRemark;
    }

    public void setBomRemark(String bomRemark) {
        this.bomRemark = bomRemark;
    }

    public String getZdchdj() {
        return zdchdj;
    }

    public void setZdchdj(String zdchdj) {
        this.zdchdj = zdchdj;
    }

    public String getSfxnj() {
        return sfxnj;
    }

    public void setSfxnj(String sfxnj) {
        this.sfxnj = sfxnj;
    }

    public String getChildNumber() {
        return childNumber;
    }

    public void setChildNumber(String childNumber) {
        this.childNumber = childNumber;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public WTPartUsageLink getLink() {
        return link;
    }

    public void setLink(WTPartUsageLink link) {
        this.link = link;
    }

    public String getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(String majorVersion) {
        this.majorVersion = majorVersion;
    }

    public WTPart getChildPart() {
        return childPart;
    }

    public void setChildPart(WTPart childPart) {
        this.childPart = childPart;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "BomInfo{" +
                "parentNumber='" + parentNumber + '\'' +
                ", parentName='" + parentName + '\'' +
                ", view='" + view + '\'' +
                ", childNumber='" + childNumber + '\'' +
                ", childName='" + childName + '\'' +
                ", weihao='" + weihao + '\'' +
                ", shuliang='" + shuliang + '\'' +
                ", unit='" + unit + '\'' +
                ", version='" + version + '\'' +
                ", bomRemark='" + bomRemark + '\'' +
                ", zdchdj='" + zdchdj + '\'' +
                ", sfxnj='" + sfxnj + '\'' +
                '}';
    }
}
