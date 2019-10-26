package ext.appo.erp.bean;

public class MaterialInfo {

	private Integer id;// erp中物料对应id
	// 基本属性
	private String number = "";// 编号

	private String name = "";// 名称

	private String view = "";// 视图

	private String majorVersion = "";// 大版本

	private String version = "";// 版本

	private String lifecycle = "";// 生命周期状态

	private String defaultUnit = "";// 默认单位

	private String modifier = "";// 修改者

	// IBA属性
	private String Classification = "";// 分类

	// 分类内部名称
	private String ClassificationEnd = "";

	// IBA属性
	private String sscpx = ""; // 所属产品线

	// IBA属性
	private String ssxm = ""; // 所属项目

	// IBA属性
	private String nbxh = ""; // 内部型号

	// IBA属性
	private String cpzt = ""; // 产品状态

	// IBA属性
	private String xsxh = ""; // 销售型号

	private String ggms;// 规格描述

	private String hbsx;// 环保属性

	private String zldw;// 重量单位

	private String zl;// 重量

	private String gylx;// 光源类型

	private String cpxl;// 产品系列

	private String bzcc;// 包装尺寸（长*宽*高）

	private String xsjs;// 显示技术

	private String brand;// 品牌

	private String createUser;// 创建者

	private String zxlcbb;// 最新量产版本

	private String F_APPO_CLYJ;// 处理意见

	// private String zdchdj;//最低存货等级

	private String F_APPO_BZCC;// 包装尺寸长*宽*高）(传String类型)

	private String FSpecification;// 规格描述(规格型号)

	private String source;// 源(外购-1:外购，自制-2：自制，外协-3：委外 )

	// mao
	private String oldVersion;

	private String sfxnj = "0";// 是否虚拟件(0非虚拟件，1虚拟件,工艺虚拟件或设计虚拟件是true则是虚拟件，先查工艺虚拟件再查设计虚拟键)

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

	// public String getZdchdj() { return zdchdj; }

	// public void setZdchdj(String zdckdj) { this.zdchdj = zdchdj; }

	public String getF_APPO_CLYJ() {
		return F_APPO_CLYJ;
	}

	public void setF_APPO_CLYJ(String f_APPO_CLYJ) {
		F_APPO_CLYJ = f_APPO_CLYJ;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public String getClassification() {
		return Classification;
	}

	public void setClassification(String classification) {
		Classification = classification;
	}

	public String getClassificationEnd() {
		return ClassificationEnd;
	}

	public void setClassificationEnd(String classificationEnd) {
		ClassificationEnd = classificationEnd;
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

	public String getCpzt() {
		return cpzt;
	}

	public void setCpzt(String cpzt) {
		this.cpzt = cpzt;
	}

	public String getXsxh() {
		return xsxh;
	}

	public void setXsxh(String xsxh) {
		this.xsxh = xsxh;
	}

	public String getGgms() {
		return ggms;
	}

	public void setGgms(String ggms) {
		this.ggms = ggms;
	}

	public String getHbsx() {
		return hbsx;
	}

	public void setHbsx(String hbsx) {
		this.hbsx = hbsx;
	}

	public String getZldw() {
		return zldw;
	}

	public void setZldw(String zldw) {
		this.zldw = zldw;
	}

	public String getZl() {
		return zl;
	}

	public void setZl(String zl) {
		this.zl = zl;
	}

	public String getGylx() {
		return gylx;
	}

	public void setGylx(String gylx) {
		this.gylx = gylx;
	}

	public String getCpxl() {
		return cpxl;
	}

	public void setCpxl(String cpxl) {
		this.cpxl = cpxl;
	}

	public String getBzcc() {
		return bzcc;
	}

	public void setBzcc(String bzcc) {
		this.bzcc = bzcc;
	}

	public String getXsjs() {
		return xsjs;
	}

	public void setXsjs(String xsjs) {
		this.xsjs = xsjs;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getZxlcbb() {
		return zxlcbb;
	}

	public void setZxlcbb(String zxlcbb) {
		this.zxlcbb = zxlcbb;
	}

	@Override
	public String toString() {
		return "MaterialInfo{" + "id=" + id + ", number='" + number + '\'' + ", name='" + name + '\'' + ", view='"
				+ view + '\'' + ", majorVersion='" + majorVersion + '\'' +
				// ", version='" + version + '\'' +
				", lifecycle='" + lifecycle + '\'' + ", defaultUnit='" + defaultUnit + '\'' + ", modifier='" + modifier
				+ '\'' + ", Classification='" + Classification + '\'' + ", ClassificationEnd='" + ClassificationEnd
				+ '\'' + ", sscpx='" + sscpx + '\'' + ", ssxm='" + ssxm + '\'' + ", nbxh='" + nbxh + '\'' + ", cpzt='"
				+ cpzt + '\'' + ", xsxh='" + xsxh + '\'' + ", ggms='" + ggms + '\'' + ", hbsx='" + hbsx + '\''
				+ ", zldw='" + zldw + '\'' + ", zl='" + zl + '\'' + ", gylx='" + gylx + '\'' + ", cpxl='" + cpxl + '\''
				+ ", bzcc='" + bzcc + '\'' + ", xsjs='" + xsjs + '\'' + ", brand='" + brand + '\'' + ", createUser='"
				+ createUser + '\'' + ", F_APPO_CLYJ='" + F_APPO_CLYJ + '\'' +
				// ", zdchdj='" + zdchdj + '\'' +
				// ", F_APPO_BZCC='" + F_APPO_BZCC + '\'' +
				// ", FSpecification='" + FSpecification + '\'' +
				// ", source='" + source + '\'' +

		'}';
	}
}
