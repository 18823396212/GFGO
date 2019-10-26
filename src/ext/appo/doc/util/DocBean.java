package ext.appo.doc.util;

public class DocBean {

	private static long serialVersionUID = 1L;

	// 序号
	private String serial = "";
	// 所属项目
	private String ssxm = "";
	// 文档大类
	private String doctype = "";
	// 文档小类
	private String smalltype = null;
	private String docnumber = null;
	private String docname = null;
	private String version = null;
	private String mainname = null;
	private String docstate = null;
	private String releatepart = null;
	private String doccreator = null;
	private String doccreatime = null;
	private String modifidior = null;
	private String lastmodifytime = null;
	private String remark = null;
	private String productline = null;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DocBean [serial=");
		builder.append(serial);
		builder.append(", ssxm=");
		builder.append(ssxm);
		builder.append(", doctype=");
		builder.append(doctype);
		builder.append(", smalltype=");
		builder.append(smalltype);
		builder.append(", docnumber=");
		builder.append(docnumber);
		builder.append(", docname=");
		builder.append(docname);
		builder.append(", version=");
		builder.append(version);
		builder.append(", mainname=");
		builder.append(mainname);
		builder.append(", docstate=");
		builder.append(docstate);
		builder.append(", releatepart=");
		builder.append(releatepart);
		builder.append(", doccreator=");
		builder.append(doccreator);
		builder.append(", doccreatime=");
		builder.append(doccreatime);
		builder.append(", modifidior=");
		builder.append(modifidior);
		builder.append(", lastmodifytime=");
		builder.append(lastmodifytime);
		builder.append(", remark=");
		builder.append(remark);
		builder.append(", productline=");
		builder.append(productline);
		builder.append("]\n");
		return builder.toString();
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public static void setSerialVersionUID(long serialVersionUID) {
		DocBean.serialVersionUID = serialVersionUID;
	}

	public String getProductline() {
		return productline;
	}

	public void setProductline(String productline) {
		this.productline = productline;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public static void setSerialversionuid(long serialversionuid) {
		serialVersionUID = serialversionuid;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public String getSsxm() {
		return ssxm;
	}

	public void setSsxm(String ssxm) {
		this.ssxm = ssxm;
	}

	public String getDoctype() {
		return doctype;
	}

	public void setDoctype(String doctype) {
		this.doctype = doctype;
	}

	public String getSmalltype() {
		return smalltype;
	}

	public void setSmalltype(String smalltype) {
		this.smalltype = smalltype;
	}

	public String getDocnumber() {
		return docnumber;
	}

	public void setDocnumber(String docnumber) {
		this.docnumber = docnumber;
	}

	public String getDocname() {
		return docname;
	}

	public void setDocname(String docname) {
		this.docname = docname;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getMainname() {
		return mainname;
	}

	public void setMainname(String mainname) {
		this.mainname = mainname;
	}

	public String getDocstate() {
		return docstate;
	}

	public void setDocstate(String docstate) {
		this.docstate = docstate;
	}

	public String getReleatepart() {
		return releatepart;
	}

	public void setReleatepart(String releatepart) {
		this.releatepart = releatepart;
	}

	public String getDoccreator() {
		return doccreator;
	}

	public void setDoccreator(String doccreator) {
		this.doccreator = doccreator;
	}

	public String getDoccreatime() {
		return doccreatime;
	}

	public void setDoccreatime(String doccreatime) {
		this.doccreatime = doccreatime;
	}

	public String getModifidior() {
		return modifidior;
	}

	public void setModifidior(String modifidior) {
		this.modifidior = modifidior;
	}

	public String getLastmodifytime() {
		return lastmodifytime;
	}

	public void setLastmodifytime(String lastmodifytime) {
		this.lastmodifytime = lastmodifytime;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
