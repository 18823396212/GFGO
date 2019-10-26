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

import java.io.Serializable;

/*
 * 库存价格信息
 * 
 */
public class InventoryPrice implements Serializable {
	private static final long serialVersionUID = 1L;
	// 基本属性
	// 物料编码
	public String item_id = "";
	public String iAveragecost = "";
	public String iSupplycycle = "";
	public String iMoq = "";
	public String iMpq = "";
	public String cpurPerson = "";
	public String cinvPerson = "";
	public String iQuantity = "";
	public String fTransinquantity = "";
	public String fInquantity = "";
	public String cFlag = "";
	public String dreleaseDate = "";
	public String mVersion = "";
	public String company = "";
	public String getItem_id() {
		return item_id;
	}
	public void setItem_id(String item_id) {
		this.item_id = item_id;
	}
	public String getiAveragecost() {
		return iAveragecost;
	}
	public void setiAveragecost(String iAveragecost) {
		this.iAveragecost = iAveragecost;
	}
	public String getiSupplycycle() {
		return iSupplycycle;
	}
	public void setiSupplycycle(String iSupplycycle) {
		this.iSupplycycle = iSupplycycle;
	}
	public String getiMoq() {
		return iMoq;
	}
	public void setiMoq(String iMoq) {
		this.iMoq = iMoq;
	}
	public String getiMpq() {
		return iMpq;
	}
	public void setiMpq(String iMpq) {
		this.iMpq = iMpq;
	}
	public String getCpurPerson() {
		return cpurPerson;
	}
	public void setCpurPerson(String cpurPerson) {
		this.cpurPerson = cpurPerson;
	}
	public String getCinvPerson() {
		return cinvPerson;
	}
	public void setCinvPerson(String cinvPerson) {
		this.cinvPerson = cinvPerson;
	}
	public String getiQuantity() {
		return iQuantity;
	}
	public void setiQuantity(String iQuantity) {
		this.iQuantity = iQuantity;
	}
	public String getfTransinquantity() {
		return fTransinquantity;
	}
	public void setfTransinquantity(String fTransinquantity) {
		this.fTransinquantity = fTransinquantity;
	}
	public String getfInquantity() {
		return fInquantity;
	}
	public void setfInquantity(String fInquantity) {
		this.fInquantity = fInquantity;
	}
	public String getcFlag() {
		return cFlag;
	}
	public void setcFlag(String cFlag) {
		this.cFlag = cFlag;
	}
	public String getDreleaseDate() {
		return dreleaseDate;
	}
	public void setDreleaseDate(String dreleaseDate) {
		this.dreleaseDate = dreleaseDate;
	}
	public String getmVersion() {
		return mVersion;
	}
	public void setmVersion(String mVersion) {
		this.mVersion = mVersion;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}

}
