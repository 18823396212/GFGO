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
 * 对应BOM头表
 * @author Administrator
 *
 */
public class BOMRootInfo extends AbstractLightBean{
	private static final long serialVersionUID = 1L;
	
	@ERPAttributeMapping(attrName="oid")
	private String oid ; //对应primary_key
	
	@FieldMapping(logicalId="number")
	private String number ="";//物料编码
	
	@ERPAttributeMapping(attrName="view")
	private String view =""; //物料视图
	
	@FieldMapping(logicalId="versionInfo.identifier.versionId")
	private String majorVersion ="";//PDM系统的零部件大版本';
	
	@ERPAttributeMapping(attrName="version")
	private String version ="";//发布的BOM结构，根节点的版本';
	
	// IBA属性
	@ERPAttributeMapping(attrName = "ssgs")
	private String belongProductLine = "";// 所属公司

	
	@FieldMapping(logicalId = "state.state")
	private String lifecycle = "";// 生命周期状态
	
	//特殊属性
	private String ecnNumber ="";//BOM结构关联的ECN单号';
	private String batchNumber = "";//批次号
	
	@ERPAttributeMapping(attrName="baselineNumber")
	private String baselineNumber = "";// 基线号
	
	//ERP集成字段
	private String flag ="";//标识符，标识当前PDM或者ERP能否更新数据';
	private Date releaseDate ;//PDM发布时间';
	private String pdmWritebackStatus ="";//'PDM数据回传状态';
	private String erpErrorMsg ="";//ERP处理返回的错误信息';
	private Date erpProcessDate;//'ERP处理时间';
	
	public String getOid() {
		return oid;
	}
	public void setOid(String oid) {
		this.oid = oid;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
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
	public String getPdmWritebackStatus() {
		return pdmWritebackStatus;
	}
	public void setPdmWritebackStatus(String pdmWritebackStatus) {
		this.pdmWritebackStatus = pdmWritebackStatus;
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
	public String getBatchNumber() {
		return batchNumber;
	}
	public void setBatchNumber(String batchNumber) {
		this.batchNumber = batchNumber;
	}
	public String getBelongProductLine() {
		return belongProductLine;
	}
	public void setBelongProductLine(String belongProductLine) {
		this.belongProductLine = belongProductLine;
	}
	public String getLifecycle() {
		return lifecycle;
	}
	public void setLifecycle(String lifecycle) {
		this.lifecycle = lifecycle;
	}
	public String getBaselineNumber() {
		return baselineNumber;
	}
	public void setBaselineNumber(String baselineNumber) {
		this.baselineNumber = baselineNumber;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BOMRootInfo [oid=");
		builder.append(oid);
		builder.append(", number=");
		builder.append(number);
		builder.append(", view=");
		builder.append(view);
		builder.append(", majorVersion=");
		builder.append(majorVersion);
		builder.append(", version=");
		builder.append(version);
		builder.append(", belongProductLine=");
		builder.append(belongProductLine);
		builder.append(", lifecycle=");
		builder.append(lifecycle);
		builder.append(", ecnNumber=");
		builder.append(ecnNumber);
		builder.append(", batchNumber=");
		builder.append(batchNumber);
		builder.append(", flag=");
		builder.append(flag);
		builder.append(", releaseDate=");
		builder.append(releaseDate);
		builder.append(", pdmWritebackStatus=");
		builder.append(pdmWritebackStatus);
		builder.append(", erpErrorMsg=");
		builder.append(erpErrorMsg);
		builder.append(", erpProcessDate=");
		builder.append(erpProcessDate);
		builder.append(", baselineNumber=");
		builder.append(baselineNumber);
		builder.append("]");
		return builder.toString();
	}

}
