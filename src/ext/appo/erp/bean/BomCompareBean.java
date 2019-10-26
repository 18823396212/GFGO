package ext.appo.erp.bean;


public class BomCompareBean {

	private String compareFlag;//标识

	private String childNumber;//物料编码

	private String childName;//名称

	private String ChildVersion;//版本

	private String parentNumber;//父编码

	private String parentName;//父名称

	private String parentView ;//父件视图

	private String bomVersion;//BOM版本

	private String oldVersion;//版本（旧）

	private String newVersion;//版本（新）

	private String oldSum;//数量（旧）

	private String newSum;//数量（新）

	private  String oldWh;//老位号

	private String newWh;//新位号

	private String weihao;//位号变更

	private Long lineNumber ; //行号

	private String zdchdj;//最低存货等级

	private String bombzxx;//bom备注信息

	private String unit ;  //单位

	private  String FBomEntryId;//


	private  String substituteNumber;//用完旧料替代料编码
	private String substituteChildVersion;//用完旧料替代料BOM版本
	private String substituteBomVersion;//用完旧料替代料版本
	private String substituteSum;//用完旧料替代料数量
	private  String substituteWh;//用完旧料替代料位号

	public String getSubstituteNumber() {
		return substituteNumber;
	}

	public void setSubstituteNumber(String substituteNumber) {
		this.substituteNumber = substituteNumber;
	}

	public String getSubstituteChildVersion() {
		return substituteChildVersion;
	}

	public String getSubstituteBomVersion() {
		return substituteBomVersion;
	}

	public String getSubstituteSum() {
		return substituteSum;
	}

	public String getSubstituteWh() {
		return substituteWh;
	}



	public void setSubstituteChildVersion(String substituteChildVersion) {
		this.substituteChildVersion = substituteChildVersion;
	}

	public void setSubstituteBomVersion(String substituteBomVersion) {
		this.substituteBomVersion = substituteBomVersion;
	}

	public void setSubstituteSum(String substituteSum) {
		this.substituteSum = substituteSum;
	}

	public void setSubstituteWh(String substituteWh) {
		this.substituteWh = substituteWh;
	}


	public String getFBomEntryId() {
		return FBomEntryId;
	}

	public void setFBomEntryId(String FBomEntryId) {
		this.FBomEntryId = FBomEntryId;
	}

	public String getCompareFlag() {
		return compareFlag;
	}

	public void setCompareFlag(String compareFlag) {
		this.compareFlag = compareFlag;
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

	public String getOldVersion() {
		return oldVersion;
	}

	public void setOldVersion(String oldVersion) {
		this.oldVersion = oldVersion;
	}

	public String getNewVersion() {
		return newVersion;
	}

	public void setNewVersion(String newVersion) {
		this.newVersion = newVersion;
	}

	public String getOldSum() {
		return oldSum;
	}

	public void setOldSum(String oldSum) {
		this.oldSum = oldSum;
	}

	public String getNewSum() {
		return newSum;
	}

	public void setNewSum(String newSum) {
		this.newSum = newSum;
	}

	public String getWeihao() {
		return weihao;
	}

	public void setWeihao(String weihao) {
		this.weihao = weihao;
	}


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

	public String getChildVersion() {
		return ChildVersion;
	}

	public void setChildVersion(String childVersion) {
		ChildVersion = childVersion;
	}

	public String getParentView() {
		return parentView;
	}

	public void setParentView(String parentView) {
		this.parentView = parentView;
	}

	public Long getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(Long lineNumber) {
		this.lineNumber = lineNumber;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getZdchdj() {
		return zdchdj;
	}

	public void setZdchdj(String zdchdj) {
		this.zdchdj = zdchdj;
	}

	public String getBombzxx() {
		return bombzxx;
	}

	public void setBombzxx(String bombzxx) {
		this.bombzxx = bombzxx;
	}

	public String getOldWh() {
		return oldWh;
	}

	public void setOldWh(String oldWh) {
		this.oldWh = oldWh;
	}

	public String getNewWh() {
		return newWh;
	}

	public void setNewWh(String newWh) {
		this.newWh = newWh;
	}

	public String getBomVersion() {
		return bomVersion;
	}

	public void setBomVersion(String bomVersion) {
		this.bomVersion = bomVersion;
	}

	@Override
	public String toString() {
		return "BomCompareBean{" +
				"compareFlag='" + compareFlag + '\'' +
				", childNumber='" + childNumber + '\'' +
				", childName='" + childName + '\'' +
				", ChildVersion='" + ChildVersion + '\'' +
				", parentNumber='" + parentNumber + '\'' +
				", parentName='" + parentName + '\'' +
				", parentView='" + parentView + '\'' +
				", bomVersion='" + bomVersion + '\'' +
				", oldVersion='" + oldVersion + '\'' +
				", newVersion='" + newVersion + '\'' +
				", oldSum='" + oldSum + '\'' +
				", newSum='" + newSum + '\'' +
				", oldWh='" + oldWh + '\'' +
				", newWh='" + newWh + '\'' +
				", weihao='" + weihao + '\'' +
				", lineNumber=" + lineNumber +
				", zdchdj='" + zdchdj + '\'' +
				", bombzxx='" + bombzxx + '\'' +
				", unit='" + unit + '\'' +
				'}';
	}
}
