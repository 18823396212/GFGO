package ext.appo.erp.bean;

public class MaterialJson {

	private String name = "";
	private String number = "";
	private String majorVersion = "";
	private String lifecycle = "";
	private String createUser = "";
	private String ggms = "";
	private String Classification = "";
	private String defaultUnit = "";
	private String zldw = "";
	private double zl;
	private String bzcc;
	private String sscpx = "";
	private String ssxm = "";
	private String nbxh = "";
	private String xsxh = "";
	private String cpxl = "";
	private String cpzt = "";
	private String hbsx = "";
	private String xsjs = "";
	private String gylx = "";
	private String brand = "";
	private String zzlcbb = "";// 最新量产版本

	private String F_APPO_CLYJ = "";// 处理意见

	// private String zdchdj=""; //最低存货等级

	private String F_APPO_BZCC = "";// 包装尺寸长*宽*高）(传String类型)

	private String FSpecification;// 规格描述(规格型号)

	private String source;// 源(外购-1:外购，自制-2：自制，外协-3：委外 )

	private String sfxnj = "0";// 是否虚拟键(0非虚拟件，1虚拟件)
	// mao
	private String oldVersion = "";

	public String getOldVersion() {
		return oldVersion;
	}

	public void setOldVersion(String oldVersion) {
		this.oldVersion = oldVersion;
	}

	public String getSfxnj() {
		return sfxnj;
	}

	public void setSfxnj(String sfxnj) {
		this.sfxnj = sfxnj;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getFSpecification() {
		return FSpecification;
	}

	public void setFSpecification(String FSpecification) {
		this.FSpecification = FSpecification;
	}

	public String getF_APPO_BZCC() {
		return F_APPO_BZCC;
	}

	public void setF_APPO_BZCC(String f_APPO_BZCC) {
		F_APPO_BZCC = f_APPO_BZCC;
	}

	// public void setZdchdj(String zdckdj) {
	// this.zdchdj = zdckdj;
	// }
	//
	// public String getZdchdj() { return zdchdj; }

	public String getF_APPO_CLYJ() {
		return F_APPO_CLYJ;
	}

	public void setF_APPO_CLYJ(String f_APPO_CLYJ) {
		F_APPO_CLYJ = f_APPO_CLYJ;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getMajorVersion() {
		return majorVersion;
	}

	public void setMajorVersion(String majorVersion) {
		this.majorVersion = majorVersion;
	}

	public String getLifecycle() {
		return lifecycle;
	}

	public void setLifecycle(String lifecycle) {
		this.lifecycle = lifecycle;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getGgms() {
		return ggms;
	}

	public void setGgms(String ggms) {
		this.ggms = ggms;
	}

	public String getClassification() {
		return Classification;
	}

	public void setClassification(String classification) {
		Classification = classification;
	}

	public String getDefaultUnit() {
		return defaultUnit;
	}

	public void setDefaultUnit(String defaultUnit) {
		this.defaultUnit = defaultUnit;
	}

	public String getZldw() {
		return zldw;
	}

	public void setZldw(String zldw) {
		this.zldw = zldw;
	}

	public double getZl() {
		return zl;
	}

	public void setZl(double zl) {
		this.zl = zl;
	}

	public String getBzcc() {
		return bzcc;
	}

	public void setBzcc(String bzcc) {
		this.bzcc = bzcc;
	}

	public String getSscpx() {
		return sscpx;
	}

	public void setSscpx(String sscpx) {
		this.sscpx = sscpx;
	}

	public String getSsxm() {
		return ssxm;
	}

	public void setSsxm(String ssxm) {
		this.ssxm = ssxm;
	}

	public String getNbxh() {
		return nbxh;
	}

	public void setNbxh(String nbxh) {
		this.nbxh = nbxh;
	}

	public String getXsxh() {
		return xsxh;
	}

	public void setXsxh(String xsxh) {
		this.xsxh = xsxh;
	}

	public String getCpxl() {
		return cpxl;
	}

	public void setCpxl(String cpxl) {
		this.cpxl = cpxl;
	}

	public String getCpzt() {
		return cpzt;
	}

	public void setCpzt(String cpzt) {
		this.cpzt = cpzt;
	}

	public String getHbsx() {
		return hbsx;
	}

	public void setHbsx(String hbsx) {
		this.hbsx = hbsx;
	}

	public String getXsjs() {
		return xsjs;
	}

	public void setXsjs(String xsjs) {
		this.xsjs = xsjs;
	}

	public String getGylx() {
		return gylx;
	}

	public void setGylx(String gylx) {
		this.gylx = gylx;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getZzlcbb() {
		return zzlcbb;
	}

	public void setZzlcbb(String zzlcbb) {
		this.zzlcbb = zzlcbb;
	}

	@Override
	public String toString() {
		return "{\"MaterialsRoot\":{" + "\"name\":\"" + name + "\"\n" + ", \"number\":\"" + number + "\"\n"
				+ ", \"majorVersion\":\"" + majorVersion + "\"\n" + ", \"lifecycle\":\"" + lifecycle + "\"\n"
				+ ", \"createUser\":\"" + createUser + "\"\n" + ", \"ggms\":\"" + ggms + "\"\n"
				+ ", \"Classification\":\"" + Classification + "\"\n" + ", \"defaultUnit\":\"" + defaultUnit + "\"\n"
				+ ", \"zldw\":\"" + zldw + "\"\n" + ", \"zl\":" + zl + "\n" + ", \"bzcc\":\"" + bzcc + "\"\n"
				+ ", \"sscpx\":\"" + sscpx + "\"\n" + ", \"ssxm\":\"" + ssxm + "\"\n" + ", \"nbxh\":\"" + nbxh + "\"\n"
				+ ", \"xsxh\":\"" + xsxh + "\"\n" + ", \"cpxl\":\"" + cpxl + "\"\n" + ", \"cpzt\":\"" + cpzt + "\"\n"
				+ ", \"hbsx\":\"" + hbsx + "\"\n" + ", \"xsjs\":\"" + xsjs + "\"\n" + ", \"gylx\":\"" + gylx + "\"\n"
				+ ", \"brand\":\"" + brand + "\"\n" + ", \"zzlcbb\":\"" + zzlcbb + "\"\n" + ", \"F_APPO_CLYJ\":\""
				+ F_APPO_CLYJ + "\"\n" +
				// ", \"sfxnj\":\"" + sfxnj + "\"\n" +
				// ", \"zdchdj\":\"" + zdchdj + "\"\n" +
				// ", \"F_APPO_BZCC\":\"" + F_APPO_BZCC + "\"\n" +
				// ", \"FSpecification\":\"" + FSpecification + "\"\n" +
		", \"FErpClsID\":\"" + source + "\"\n" + // 物料属性
				", \"oldVersion\":\"" + oldVersion + "\"\n" + // 老版本
		// ", \"FMATERIALID\":\"{\"\n" +
		// ",
		// \"FParentAuxPropId\":{\"FPARENTAUXPROPID__FF100001\":{\"FNumber\":\"D\"}
		// \n" +
		// " }}" +"\n"+
		"}}";
	}
}
