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

import wt.fc.WTObject;
/**
 * 类功能描述：
 * 
 * 存放替代关系的POJO类
 *     AlternativeMaterialUtil来组装bean属性
 * <br><br>
 * <b>Revision History</b>
 * <br><b>Rev:</b> 1.0 – 2012-07-12，魏文杰
 * <br><b>Comment:</b> Initial release.
 **/
public class AlternativeMaterial extends AbstractLightBean {
	private static final long serialVersionUID = 1L;

	//父件编号
	private String parentNumber = "" ;
	
	//父件视图
	private String parentView = "" ;
	
	//子件编号
	private String childNumber = "" ;
	
	//替代料编号
	private String replacePartNumber = "" ;
	
	//替代数量
	private Double quantity ;
	
	//替代单位
	private String unit = "" ;
	
	//替代关系类型
	private String replaceRelationship = "" ;
	
	//替代方向
	private String replaceDirection = "" ;
	
	//ERP集成属性
	private String flag = "" ;
	private String pdmWritebackStatus = "" ;
	private Date releaseDate;
	private String erpErrorMsg = "" ;
	private Date erpProcessDate ;
	
	//特殊属性
	private String batchNumber = "";
	private String oid = "";//对应primary key
	

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

	public String getChildNumber() {
		return childNumber;
	}

	public void setChildNumber(String childNumber) {
		this.childNumber = childNumber;
	}

	public String getReplacePartNumber() {
		return replacePartNumber;
	}

	public void setReplacePartNumber(String replacePartNumber) {
		this.replacePartNumber = replacePartNumber;
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

	public String getReplaceRelationship() {
		return replaceRelationship;
	}

	public void setReplaceRelationship(String replaceRelationship) {
		this.replaceRelationship = replaceRelationship;
	}

	public String getReplaceDirection() {
		return replaceDirection;
	}

	public void setReplaceDirection(String replaceDirection) {
		this.replaceDirection = replaceDirection;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getPdmWritebackStatus() {
		return pdmWritebackStatus;
	}

	public void setPdmWritebackStatus(String pdmWritebackStatus) {
		this.pdmWritebackStatus = pdmWritebackStatus;
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

	public String getBatchNumber() {
		return batchNumber;
	}

	public void setBatchNumber(String batchNumber) {
		this.batchNumber = batchNumber;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AlternativeMaterial [parentNumber=");
		builder.append(parentNumber);
		builder.append(", parentView=");
		builder.append(parentView);
		builder.append(", childNumber=");
		builder.append(childNumber);
		builder.append(", replacePartNumber=");
		builder.append(replacePartNumber);
		builder.append(", quantity=");
		builder.append(quantity);
		builder.append(", unit=");
		builder.append(unit);
		builder.append(", replaceRelationship=");
		builder.append(replaceRelationship);
		builder.append(", replaceDirection=");
		builder.append(replaceDirection);
		builder.append(", flag=");
		builder.append(flag);
		builder.append(", pdmWritebackStatus=");
		builder.append(pdmWritebackStatus);
		builder.append(", releaseDate=");
		builder.append(releaseDate);
		builder.append(", erpErrorMsg=");
		builder.append(erpErrorMsg);
		builder.append(", erpProcessDate=");
		builder.append(erpProcessDate);
		builder.append(", batchNumber=");
		builder.append(batchNumber);
		builder.append(", oid=");
		builder.append(oid);
		builder.append("]");
		return builder.toString();
	}
	
}
