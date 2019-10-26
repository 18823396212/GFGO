package ext.tools;

import java.util.Objects;

public class PartAttrsBean {

	private String rowNum;

	private String partNumber;

	// excel
	private String attr1;//

	// exce2
	private String attr2;// sscpx 产品线

	private String attr3;// nbxh 内部型号

	private String attr4;// xsxh 销售型号

	private String attr5;// brand 品牌

	private String attr6;// cpzt 产品状态

	private String attr7;// cpxl 产品系列

	private String attr8;// sfzycp 是否自研产品

	private String attr9;// sfxnj 是否工艺虚拟件

	private String attr10;
	private String attr11;
	private String attr12;
	private String attr13;
	private String attr14;
	private String attr15;
	private String attr16;
	private String attr17;
	private String attr18;
	private String attr19;

	public String getAttr10() {
		return attr10;
	}

	public void setAttr10(String attr10) {
		this.attr10 = attr10;
	}

	public String getAttr11() {
		return attr11;
	}

	public void setAttr11(String attr11) {
		this.attr11 = attr11;
	}

	public String getAttr12() {
		return attr12;
	}

	public void setAttr12(String attr12) {
		this.attr12 = attr12;
	}

	public String getAttr13() {
		return attr13;
	}

	public void setAttr13(String attr13) {
		this.attr13 = attr13;
	}

	public String getAttr14() {
		return attr14;
	}

	public void setAttr14(String attr14) {
		this.attr14 = attr14;
	}

	public String getAttr15() {
		return attr15;
	}

	public void setAttr15(String attr15) {
		this.attr15 = attr15;
	}

	public String getAttr16() {
		return attr16;
	}

	public void setAttr16(String attr16) {
		this.attr16 = attr16;
	}

	public String getAttr17() {
		return attr17;
	}

	public void setAttr17(String attr17) {
		this.attr17 = attr17;
	}

	public String getAttr18() {
		return attr18;
	}

	public void setAttr18(String attr18) {
		this.attr18 = attr18;
	}

	public String getAttr19() {
		return attr19;
	}

	public void setAttr19(String attr19) {
		this.attr19 = attr19;
	}

	public String getAttr9() {
		return attr9;
	}

	public void setAttr9(String attr9) {
		this.attr9 = attr9;
	}

	public void setAttr7(String attr7) {
		this.attr7 = attr7;
	}

	public void setAttr8(String attr8) {
		this.attr8 = attr8;
	}

	public String getAttr7() {
		return attr7;
	}

	public String getAttr8() {
		return attr8;
	}

	public String getAttr6() {
		return attr6;
	}

	public void setAttr6(String attr6) {
		this.attr6 = attr6;
	}

	public String getPartNumber() {
		return partNumber;
	}

	public void setPartNumber(String partNumber) {
		this.partNumber = partNumber;
	}

	public String getAttr1() {
		return attr1;
	}

	public void setAttr1(String attr1) {
		this.attr1 = attr1;
	}

	public String getAttr2() {
		return attr2;
	}

	public void setAttr2(String attr2) {
		this.attr2 = attr2;
	}

	public String getAttr3() {
		return attr3;
	}

	public void setAttr3(String attr3) {
		this.attr3 = attr3;
	}

	public String getAttr4() {
		return attr4;
	}

	public void setAttr4(String attr4) {
		this.attr4 = attr4;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PartAttrsBean that = (PartAttrsBean) o;
		return Objects.equals(partNumber, that.partNumber);
	}

	@Override
	public int hashCode() {
		return Objects.hash(partNumber);
	}

	public String getRowNum() {
		return rowNum;
	}

	public void setRowNum(String rowNum) {
		this.rowNum = rowNum;
	}

	public String getAttr5() {
		return attr5;
	}

	public void setAttr5(String attr5) {
		this.attr5 = attr5;
	}
}
