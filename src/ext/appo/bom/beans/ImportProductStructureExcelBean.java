package ext.appo.bom.beans;

import wt.part.QuantityUnit;
import wt.part.WTPart;
import ext.lang.bean.annotations.ExcelField;
import ext.lang.bean.persistable.ExcelBeanReadable;

public class ImportProductStructureExcelBean implements ExcelBeanReadable{

	private static final long serialVersionUID = 1L;

	// 序号
	@ExcelField(columnIndex = 0)
	private String serial = "" ;
	
	// 阶层
	@ExcelField(columnIndex = 1)
	private String level = "" ;
	
	// 编码
	@ExcelField(columnIndex = 2)
	private String number = "" ;
	
	// 部件对象
	private WTPart part = null ;
	
	// 替代编码
	@ExcelField(columnIndex = 3)
	private String replaceNumber = "" ;
	
	// 替代料
	private WTPart replacePart = null ;
	
	// 替代部件类型
	@ExcelField(columnIndex = 4)
	private String replaceType = "" ;
	
	// 存货版本(部件版本)
	@ExcelField(columnIndex = 5)
	private String partVersion = "" ;
	
	// 存货名称(部件名称)
	@ExcelField(columnIndex = 6)
	private String partName = "" ;
	
	// 规格描述
	@ExcelField(columnIndex = 7)
	private String ggms = "" ;
	
	// 最低存货等级
	@ExcelField(columnIndex = 8)
	private String zdchdj = "" ;
	
	// 单位
	@ExcelField(columnIndex = 9)
	private String unit = "" ;
	
	// 单位
	private QuantityUnit quantityUnit = null ;
	
	// 用量
	@ExcelField(columnIndex = 10)
	private String quantity = "" ;
	
	// 位号
	@ExcelField(columnIndex = 11)
	private String siteLine = "" ;
	
	// BOM备注
	@ExcelField(columnIndex = 12)
	private String remark = "" ;

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public WTPart getPart() {
		return part;
	}

	public void setPart(WTPart part) {
		this.part = part;
	}

	public String getReplaceNumber() {
		return replaceNumber;
	}

	public void setReplaceNumber(String replaceNumber) {
		this.replaceNumber = replaceNumber;
	}

	public WTPart getReplacePart() {
		return replacePart;
	}

	public void setReplacePart(WTPart replacePart) {
		this.replacePart = replacePart;
	}

	public String getReplaceType() {
		return replaceType;
	}

	public void setReplaceType(String replaceType) {
		this.replaceType = replaceType;
	}

	public String getPartVersion() {
		return partVersion;
	}

	public void setPartVersion(String partVersion) {
		this.partVersion = partVersion;
	}

	public String getPartName() {
		return partName;
	}

	public void setPartName(String partName) {
		this.partName = partName;
	}

	public String getGgms() {
		return ggms;
	}

	public void setGgms(String ggms) {
		this.ggms = ggms;
	}

	public String getZdchdj() {
		return zdchdj;
	}

	public void setZdchdj(String zdchdj) {
		this.zdchdj = zdchdj;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public QuantityUnit getQuantityUnit() {
		return quantityUnit;
	}

	public void setQuantityUnit(QuantityUnit quantityUnit) {
		this.quantityUnit = quantityUnit;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getSiteLine() {
		return siteLine;
	}

	public void setSiteLine(String siteLine) {
		this.siteLine = siteLine;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ImportProductStructureExcelBean [serial=");
		builder.append(serial);
		builder.append(", level=");
		builder.append(level);
		builder.append(", number=");
		builder.append(number);
		builder.append(", part=");
		builder.append(part == null ? null : part.getDisplayIdentity());
		builder.append(", replaceNumber=");
		builder.append(replaceNumber);
		builder.append(", replacePart=");
		builder.append(replacePart == null ? null : replacePart.getDisplayIdentity());
		builder.append(", replaceType=");
		builder.append(replaceType);
		builder.append(", partVersion=");
		builder.append(partVersion);
		builder.append(", partName=");
		builder.append(partName);
		builder.append(", ggms=");
		builder.append(ggms);
		builder.append(", zdchdj=");
		builder.append(zdchdj);
		builder.append(", unit=");
		builder.append(unit);
		builder.append(", quantityUnit=");
		builder.append(quantityUnit);
		builder.append(", quantity=");
		builder.append(quantity);
		builder.append(", siteLine=");
		builder.append(siteLine);
		builder.append(", remark=");
		builder.append(remark);
		builder.append("]\n");
		return builder.toString();
	}
}
