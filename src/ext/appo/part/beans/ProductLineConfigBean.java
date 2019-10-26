package ext.appo.part.beans;

import ext.lang.bean.annotations.ExcelField;
import ext.lang.bean.persistable.ExcelBeanReadable;

public class ProductLineConfigBean implements ExcelBeanReadable{

	private static final long serialVersionUID = 1L;

	// 枚举内部名称
	@ExcelField(columnIndex = 0)
	private String enumName = null ;
	
	// 分类节点内部名称
	@ExcelField(columnIndex = 1)
	private String nodeName = null ;
	
	// 说明
	@ExcelField(columnIndex = 2)
	private String describe = null ;

	public String getEnumName() {
		return enumName;
	}

	public void setEnumName(String enumName) {
		this.enumName = enumName;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ProductLineConfigBean [enumName=");
		builder.append(enumName);
		builder.append(", nodeName=");
		builder.append(nodeName);
		builder.append(", describe=");
		builder.append(describe);
		builder.append("]\n");
		return builder.toString();
	}
}
