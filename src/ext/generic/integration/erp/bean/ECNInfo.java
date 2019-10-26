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

import ext.generic.integration.annotations.FieldMapping;

/**
 * ECN信息表对应的javaBean
 * @author Administrator
 *
 */
public class ECNInfo extends AbstractLightBean{

	private static final long serialVersionUID = 1L;
	
	@FieldMapping(logicalId="number")
	private String number ="";//ECN编号
	
	@FieldMapping(logicalId="name")
	private String name ="";//ECN名称
	
	@FieldMapping(logicalId="description")
	private String description ="";//ECN描述
	
	@FieldMapping(logicalId="needDate")
	private Date needDate ;//ECN的有效日期
	
	//ERP集成属性信息
	private String flag ="";//标识符
	private Date releaseDate;//发布日期
	private String erpErrorMsg ="";//ERP处理返回的错误信息
	private Date erpProcessDate;//ERP处理时间
	private String pdmWritebackStatus ="";
	
	//特殊属性
	private String batchNumber = "";

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getNeedDate() {
		return needDate;
	}

	public void setNeedDate(Date needDate) {
		this.needDate = needDate;
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ECNInfo [number=");
		builder.append(number);
		builder.append(", name=");
		builder.append(name);
		builder.append(", description=");
		builder.append(description);
		builder.append(", needDate=");
		builder.append(needDate);
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
		builder.append(", batchNumber=");
		builder.append(batchNumber);
		builder.append("]");
		return builder.toString();
	}
	
}
