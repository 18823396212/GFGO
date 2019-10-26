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
 * 物料表对应的javaBean
 * 
 * @author Administrator
 *
 */
public class MaterialInfo extends AbstractLightBean {
	private static final long serialVersionUID = 1L;

	@ERPAttributeMapping(attrName = "part_type")
	private String part_type = "";
	// 基本属性
	@FieldMapping(logicalId = "number")
	private String number = "";// 编号

	@FieldMapping(logicalId = "name")
	private String name = "";// 名称

	@ERPAttributeMapping(attrName = "view")
	private String view = "";// 视图

	@FieldMapping(logicalId = "versionInfo.identifier.versionId")
	private String majorVersion = "";// 大版本

	@ERPAttributeMapping(attrName = "version")
	private String version = "";// 版本

	@FieldMapping(logicalId = "state.state")
	private String lifecycle = "";// 生命周期状态

	@ERPAttributeMapping(attrName = "defaultUnit")
	private String defaultUnit = "";// 默认单位

	@ERPAttributeMapping(attrName = "source")
	private String source = "";// 源

	@ERPAttributeMapping(attrName = "modifier")
	private String modifier = "";// 修改者

	@FieldMapping(logicalId = "thePersistInfo.modifyStamp")
	private Date modifyTime;// 修改时间

	// IBA属性
	@FieldMapping(logicalId = "Classification")
	private String Classification = "";// 分类
	
	// IBA属性
	@FieldMapping(logicalId = "ggms")
	private String SpecificationDes = "";// 规格描述

	// IBA属性
	@FieldMapping(logicalId = "xsxh")
	private String SalesModel = "";// 销售型号

	// IBA属性
	@FieldMapping(logicalId = "chdj")
	private String InventoryLevel = "";// 规格描述
	
	// IBA属性
	@FieldMapping(logicalId = "zl")
	private String Weight = "";// 重量

	// IBA属性
	@FieldMapping(logicalId = "zldw")
	private String WeightUnit = "";// 重量单位
	
	// IBA属性
	@ERPAttributeMapping(attrName = "ssgs")
	private String belongProductLine = "";// 所属公司

	// IBA属性
	@ERPAttributeMapping(attrName = "ArticleDispose")
	private String FInHandleMeasures = "";// 在制处理措施

	// IBA属性
	@ERPAttributeMapping(attrName = "PassageDispose")
	private String FTransInHandleMeasures = "";// 在途处理措施

	// IBA属性
	@ERPAttributeMapping(attrName = "InventoryDispose")
	private String IHandleMeasures = "";// 库存处理措施

	// IBA属性
	@ERPAttributeMapping(attrName = "ProductDispose")
	private String FinishedHandleMeasures = "";// 已出货成品处理措施

	// IBA属性
	@ERPAttributeMapping(attrName = "ChangeType")
	private String ChangeType = "";// 变更类型

	// IBA属性
	@FieldMapping(logicalId = "ChangeRemarks")
	private String ChangeRemarks = "";// 变更备注

	// 特殊属性
	private String ecnNumber = "";
	private String batchNumber = ""; // 批次号

	//分类内部名称
	@ERPAttributeMapping(attrName = "ClassificationEnd")
	private String ClassificationEnd = "";
	
	// IBA属性
	@ERPAttributeMapping(attrName="sfxnj")
	private String isInventedPart = "";// 是否虚拟机
	
	// ERP相关集成属性
	private String flag = "";
	private Date releaseDate;// 发布时间
	private String erpErrorMsg = "";
	private Date erpProcessDate;// erp处理时间
	private String pdmWritebackStatus = "";

	/* getter and setter */
	public String getPart_type() {
		return part_type;
	}

	public void setPart_type(String part_type) {
		this.part_type = part_type;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}

	public String getMajorVersion() {
		return majorVersion;
	}

	public void setMajorVersion(String majorVersion) {
		this.majorVersion = majorVersion;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getLifecycle() {
		return lifecycle;
	}

	public void setLifecycle(String lifecycle) {
		this.lifecycle = lifecycle;
	}

	public String getDefaultUnit() {
		return defaultUnit;
	}

	public void setDefaultUnit(String defaultUnit) {
		this.defaultUnit = defaultUnit;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public String getClassification() {
		return Classification;
	}

	public void setClassification(String classification) {
		Classification = classification;
	}

	public String getEcnNumber() {
		return ecnNumber;
	}

	public void setEcnNumber(String ecnNumber) {
		this.ecnNumber = ecnNumber;
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

	public String getSpecificationDes() {
		return SpecificationDes;
	}

	public void setSpecificationDes(String specificationDes) {
		SpecificationDes = specificationDes;
	}

	public String getSalesModel() {
		return SalesModel;
	}

	public void setSalesModel(String salesModel) {
		SalesModel = salesModel;
	}

	public String getInventoryLevel() {
		return InventoryLevel;
	}

	public void setInventoryLevel(String inventoryLevel) {
		InventoryLevel = inventoryLevel;
	}

	public String getWeight() {
		return Weight;
	}

	public void setWeight(String weight) {
		Weight = weight;
	}

	public String getWeightUnit() {
		return WeightUnit;
	}

	public void setWeightUnit(String weightUnit) {
		WeightUnit = weightUnit;
	}

	public String getFInHandleMeasures() {
		return FInHandleMeasures;
	}

	public void setFInHandleMeasures(String fInHandleMeasures) {
		FInHandleMeasures = fInHandleMeasures;
	}

	public String getFTransInHandleMeasures() {
		return FTransInHandleMeasures;
	}

	public void setFTransInHandleMeasures(String fTransInHandleMeasures) {
		FTransInHandleMeasures = fTransInHandleMeasures;
	}

	public String getIHandleMeasures() {
		return IHandleMeasures;
	}

	public void setIHandleMeasures(String iHandleMeasures) {
		IHandleMeasures = iHandleMeasures;
	}

	public String getFinishedHandleMeasures() {
		return FinishedHandleMeasures;
	}

	public void setFinishedHandleMeasures(String finishedHandleMeasures) {
		FinishedHandleMeasures = finishedHandleMeasures;
	}

	public String getChangeType() {
		return ChangeType;
	}

	public void setChangeType(String changeType) {
		ChangeType = changeType;
	}

	public String getChangeRemarks() {
		return ChangeRemarks;
	}

	public void setChangeRemarks(String changeRemarks) {
		ChangeRemarks = changeRemarks;
	}

	public String getClassificationEnd() {
		return ClassificationEnd;
	}

	public void setClassificationEnd(String classificationEnd) {
		ClassificationEnd = classificationEnd;
	}

	public String getBelongProductLine() {
		return belongProductLine;
	}

	public void setBelongProductLine(String belongProductLine) {
		this.belongProductLine = belongProductLine;
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
		builder.append("MaterialInfo [part_type=");
		builder.append(part_type);
		builder.append(", number=");
		builder.append(number);
		builder.append(", name=");
		builder.append(name);
		builder.append(", view=");
		builder.append(view);
		builder.append(", majorVersion=");
		builder.append(majorVersion);
		builder.append(", version=");
		builder.append(version);
		builder.append(", lifecycle=");
		builder.append(lifecycle);
		builder.append(", defaultUnit=");
		builder.append(defaultUnit);
		builder.append(", source=");
		builder.append(source);
		builder.append(", modifier=");
		builder.append(modifier);
		builder.append(", modifyTime=");
		builder.append(modifyTime);
		builder.append(", Classification=");
		builder.append(Classification);
		builder.append(", SpecificationDes=");
		builder.append(SpecificationDes);
		builder.append(", SalesModel=");
		builder.append(SalesModel);
		builder.append(", InventoryLevel=");
		builder.append(InventoryLevel);
		builder.append(", Weight=");
		builder.append(Weight);
		builder.append(", WeightUnit=");
		builder.append(WeightUnit);
		builder.append(", belongProductLine=");
		builder.append(belongProductLine);
		builder.append(", FInHandleMeasures=");
		builder.append(FInHandleMeasures);
		builder.append(", FTransInHandleMeasures=");
		builder.append(FTransInHandleMeasures);
		builder.append(", IHandleMeasures=");
		builder.append(IHandleMeasures);
		builder.append(", FinishedHandleMeasures=");
		builder.append(FinishedHandleMeasures);
		builder.append(", ChangeType=");
		builder.append(ChangeType);
		builder.append(", ChangeRemarks=");
		builder.append(ChangeRemarks);
		builder.append(", ecnNumber=");
		builder.append(ecnNumber);
		builder.append(", batchNumber=");
		builder.append(batchNumber);
		builder.append(", ClassificationEnd=");
		builder.append(ClassificationEnd);
		builder.append(", isInventedPart=");
		builder.append(isInventedPart);
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
		builder.append("]");
		return builder.toString();
	}


}
