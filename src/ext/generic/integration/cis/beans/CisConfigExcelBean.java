package ext.generic.integration.cis.beans;

import ext.lang.bean.annotations.ExcelField;
import ext.lang.bean.persistable.ExcelBeanReadable;

public class CisConfigExcelBean implements ExcelBeanReadable{

	private static final long serialVersionUID = 1L;

	// 层次
	@ExcelField(columnIndex = 0)
	private String nodeNumber = null ;
	
	// 系统字段
	@ExcelField(columnIndex = 1)
	private String plmAttributeName = null ;
	
	// cis字段
	@ExcelField(columnIndex = 2)
	private String cisAttributeName = null ;
	
	// plm字段类型
	@ExcelField(columnIndex = 3)
	private String attributeType = null ;
	
	// cis表
	@ExcelField(columnIndex = 4)
	private String tableName = null ;
	
	// 字段说明
	@ExcelField(columnIndex = 5)
	private String accountfor = null ;

	public String getNodeNumber() {
		return nodeNumber;
	}

	public void setNodeNumber(String nodeNumber) {
		this.nodeNumber = nodeNumber;
	}

	public String getPlmAttributeName() {
		return plmAttributeName;
	}

	public void setPlmAttributeName(String plmAttributeName) {
		this.plmAttributeName = plmAttributeName;
	}

	public String getCisAttributeName() {
		return cisAttributeName;
	}

	public void setCisAttributeName(String cisAttributeName) {
		this.cisAttributeName = cisAttributeName;
	}

	public String getAttributeType() {
		return attributeType;
	}

	public void setAttributeType(String attributeType) {
		this.attributeType = attributeType;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getAccountfor() {
		return accountfor;
	}

	public void setAccountfor(String accountfor) {
		this.accountfor = accountfor;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CisConfigExcelBean [nodeNumber=");
		builder.append(nodeNumber);
		builder.append(", plmAttributeName=");
		builder.append(plmAttributeName);
		builder.append(", cisAttributeName=");
		builder.append(cisAttributeName);
		builder.append(", attributeType=");
		builder.append(attributeType);
		builder.append(", tableName=");
		builder.append(tableName);
		builder.append(", accountfor=");
		builder.append(accountfor);
		builder.append("]\n");
		return builder.toString();
	}
}
