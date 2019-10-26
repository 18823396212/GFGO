/**
 *
 * Copyright (c) 2017-2030 上海湃睿信息科技有限公司 (PISX). All Rights Reserved.
 *
 * This software is the confidential and proprietary information of PISX
 * and is subject to the terms of a software license agreement. You shall
 * not disclose such confidential information and shall use it only in accordance
 * with the terms of the license agreement.
 *
 */
package ext.generic.integration.erp.bean;

import java.util.Date;

import ext.generic.integration.annotations.ERPAttributeMapping;
import ext.generic.integration.annotations.FieldMapping;

/**
 * 对应BOM结构表
 * @author Administrator
 *
 */
public class EBOMInfo extends AbstractLightBean{
	private static final long serialVersionUID = 1L;
	//基本属性
	@ERPAttributeMapping(attrName="oid")
	private String primaryKey; //主键标识---oid
	
	@ERPAttributeMapping(attrName="oid")
	private String oid ;	//WTPartUsageLink OID
	
	@ERPAttributeMapping(attrName="parentNumber")
	private String parentNumber ="";//父件编号
	
	@ERPAttributeMapping(attrName="parentView")
	private String parentView ="";//父件视图
	
	@ERPAttributeMapping(attrName="parentMajorVersion")
	private String parentMajorVersion ="";//父件大版本
	
	@ERPAttributeMapping(attrName="parentVersion")
	private String parentVersion ="";//父件版本
	
	@ERPAttributeMapping(attrName="childNumber")
	private String childNumber ="";//子件编号
	
	@ERPAttributeMapping(attrName="childVersion")
	private String childVersion ="";//子件版本
	
	@FieldMapping(logicalId="lineNumber.value")
	private Long lineNumber ; //行号
	
	@FieldMapping(logicalId="referenceDesignatorRange")
	private String occurrence ="";  //位号 
	
	@FieldMapping(logicalId="quantity.amount")
	private Double quantity;    //数量 
	
	@ERPAttributeMapping(attrName="state.state")
	private String lifecycle = "";// 生命周期状态
	
	@ERPAttributeMapping(attrName="unit")
	private String unit ="";  //单位--替换为ERP的单位
	
	@FieldMapping(logicalId="bom_note")
	private String bom_note="";     //TODO BOM结构中的备注信息(iba 软属性) : bom_note
	
	@FieldMapping(logicalId="stockGrade")
	private String stockGrade="";     //TODO BOM结构中的备注信息(iba 软属性) : stockGrade
	
	// IBA属性
	@ERPAttributeMapping(attrName= "sfxnj")
	private String isInventedPart = "";// 是否虚拟机

	//特殊属性
	private String batchNumber = ""; //批次号
	
	//ERP集成属性信息
	private String flag ="";
	private Date releaseDate;
	private String erpErrorMsg ="";
	private Date erpProcessDate ;
	private String pdmWritebackStatus ="";
	

	public String getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	public String getParentNumber() {
		return parentNumber;
	}

	public void setParentNumber(String parentNumber) {
		this.parentNumber = parentNumber;
	}

	public String getParentView() {
		return parentView;
	}

	public void setParentView(String parentView) {
		this.parentView = parentView;
	}

	public String getParentMajorVersion() {
		return parentMajorVersion;
	}

	public void setParentMajorVersion(String parentMajorVersion) {
		this.parentMajorVersion = parentMajorVersion;
	}

	public String getParentVersion() {
		return parentVersion;
	}

	public void setParentVersion(String parentVersion) {
		this.parentVersion = parentVersion;
	}

	public String getChildNumber() {
		return childNumber;
	}

	public void setChildNumber(String childNumber) {
		this.childNumber = childNumber;
	}

	public String getChildVersion() {
		return childVersion;
	}

	public void setChildVersion(String childVersion) {
		this.childVersion = childVersion;
	}

	public Long getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(Long lineNumber) {
		this.lineNumber = lineNumber;
	}

	public String getOccurrence() {
		return occurrence;
	}

	public void setOccurrence(String occurrence) {
		this.occurrence = occurrence;
	}

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getBom_note() {
		return bom_note;
	}

	public void setBom_note(String bom_note) {
		this.bom_note = bom_note;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getErpErrorMsg() {
		return erpErrorMsg;
	}

	public void setErpErrorMsg(String erpErrorMsg) {
		this.erpErrorMsg = erpErrorMsg;
	}

	public Date getErpProcessDate() {
		return erpProcessDate;
	}

	public void setErpProcessDate(Date erpProcessDate) {
		this.erpProcessDate = erpProcessDate;
	}

	public String getPdmWritebackStatus() {
		return pdmWritebackStatus;
	}

	public void setPdmWritebackStatus(String pdmWritebackStatus) {
		this.pdmWritebackStatus = pdmWritebackStatus;
	}

	public String getBatchNumber() {
		return batchNumber;
	}

	public void setBatchNumber(String batchNumber) {
		this.batchNumber = batchNumber;
	}

	public String getLifecycle() {
		return lifecycle;
	}

	public void setLifecycle(String lifecycle) {
		this.lifecycle = lifecycle;
	}

	public String getStockGrade() {
		return stockGrade;
	}

	public void setStockGrade(String stockGrade) {
		this.stockGrade = stockGrade;
	}

	public String getIsInventedPart() {
		return isInventedPart;
	}

	public void setIsInventedPart(String isInventedPart) {
		this.isInventedPart = isInventedPart;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EBOMInfo [primaryKey=");
		builder.append(primaryKey);
		builder.append(", oid=");
		builder.append(oid);
		builder.append(", parentNumber=");
		builder.append(parentNumber);
		builder.append(", parentView=");
		builder.append(parentView);
		builder.append(", parentMajorVersion=");
		builder.append(parentMajorVersion);
		builder.append(", parentVersion=");
		builder.append(parentVersion);
		builder.append(", childNumber=");
		builder.append(childNumber);
		builder.append(", childVersion=");
		builder.append(childVersion);
		builder.append(", lineNumber=");
		builder.append(lineNumber);
		builder.append(", occurrence=");
		builder.append(occurrence);
		builder.append(", quantity=");
		builder.append(quantity);
		builder.append(", lifecycle=");
		builder.append(lifecycle);
		builder.append(", unit=");
		builder.append(unit);
		builder.append(", bom_note=");
		builder.append(bom_note);
		builder.append(", stockGrade=");
		builder.append(stockGrade);
		builder.append(", isInventedPart=");
		builder.append(isInventedPart);
		builder.append(", batchNumber=");
		builder.append(batchNumber);
		builder.append(", flag=");
		builder.append(flag);
		builder.append(", releaseDate=");
		builder.append(releaseDate);
		builder.append(", erpErrorMsg=");
		builder.append(erpErrorMsg);
		builder.append(", erpProcessDate=");
		builder.append(erpProcessDate);
		builder.append(", pdmWritebackStatus=");
		builder.append(pdmWritebackStatus);
		builder.append("]\n");
		return builder.toString();
	}
}
