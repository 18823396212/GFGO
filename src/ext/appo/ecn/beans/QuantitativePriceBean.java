package ext.appo.ecn.beans;

import java.io.Serializable;
/*
 * 数量价格信息
 *
 */
public class QuantitativePriceBean implements Serializable {

    public String id = "";//表Id
    public String wtpartNumber="";//物料编码
    public String versionInfo="";//大版本
    public String zznum="";//在制数量
    public String ztnum="";//在途数量
    public String kcnum="";//库存数量
    public String iAveragecost="";//物料成本
    public String iSupplycycle="";//供货周期
    public String iMoq="";//最小订单量
    public String iMpq="";//最小包装数量
    public String cpurPerson="";//采购员
    public String cinvPerson="";//计划员
    public String cFlag="";//发布状态
    public String dreleaseDate="";//ERP更新时间
    public String companyId="";//所属公司ID
    public String company="";//所属公司
    public String fpkId="";//币别ID
    public String fpkName="";//币别名称
    public String enabledFlag="";//是否有效
    public String createdBy="";//创建者
    public String createdDate="";//创建日期
    public String updatedBy="";//最后修改者
    public String updatedDate="";//最后修改日期

    public String getId() {
        return id;
    }

    public String getWtpartNumber() {
        return wtpartNumber;
    }

    public String getVersionInfo() {
        return versionInfo;
    }

    public String getZznum() {
        return zznum;
    }

    public String getZtnum() {
        return ztnum;
    }

    public String getKcnum() {
        return kcnum;
    }

    public String getiAveragecost() {
        return iAveragecost;
    }

    public String getiSupplycycle() {
        return iSupplycycle;
    }

    public String getiMoq() {
        return iMoq;
    }

    public String getiMpq() {
        return iMpq;
    }

    public String getCpurPerson() {
        return cpurPerson;
    }

    public String getCinvPerson() {
        return cinvPerson;
    }

    public String getcFlag() {
        return cFlag;
    }

    public String getDreleaseDate() {
        return dreleaseDate;
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getCompany() {
        return company;
    }

    public String getFpkId() {
        return fpkId;
    }

    public String getFpkName() {
        return fpkName;
    }

    public String getEnabledFlag() {
        return enabledFlag;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setWtpartNumber(String wtpartNumber) {
        this.wtpartNumber = wtpartNumber;
    }

    public void setVersionInfo(String versionInfo) {
        this.versionInfo = versionInfo;
    }

    public void setZznum(String zznum) {
        this.zznum = zznum;
    }

    public void setZtnum(String ztnum) {
        this.ztnum = ztnum;
    }

    public void setKcnum(String kcnum) {
        this.kcnum = kcnum;
    }

    public void setiAveragecost(String iAveragecost) {
        this.iAveragecost = iAveragecost;
    }

    public void setiSupplycycle(String iSupplycycle) {
        this.iSupplycycle = iSupplycycle;
    }

    public void setiMoq(String iMoq) {
        this.iMoq = iMoq;
    }

    public void setiMpq(String iMpq) {
        this.iMpq = iMpq;
    }

    public void setCpurPerson(String cpurPerson) {
        this.cpurPerson = cpurPerson;
    }

    public void setCinvPerson(String cinvPerson) {
        this.cinvPerson = cinvPerson;
    }

    public void setcFlag(String cFlag) {
        this.cFlag = cFlag;
    }

    public void setDreleaseDate(String dreleaseDate) {
        this.dreleaseDate = dreleaseDate;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setFpkId(String fpkId) {
        this.fpkId = fpkId;
    }

    public void setFpkName(String fpkName) {
        this.fpkName = fpkName;
    }

    public void setEnabledFlag(String enabledFlag) {
        this.enabledFlag = enabledFlag;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }
}
