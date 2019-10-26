package ext.appo.part.beans;

import ext.appo.part.util.EffecitveBaselineUtil;
import ext.appo.part.util.MversionControlHelper;
import wt.part.WTPart;
import wt.part.WTPartAlternateLink;
import wt.part.WTPartMaster;
import wt.part.WTPartSubstituteLink;
import wt.part.WTPartUsageLink;
import wt.util.WTException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

public class EffectiveBaselineBean {

    private String processoid;
    private String beanUID;
    private String upUID;
    private String partoid;
    private String partvid;
    private String partNumber;
    private String partName;
    private String partState;
	private String parentoid;
    private String parentvid;
    private String parenNumber;
    private String parenName;
    private String parenState;
    private String usageLinkoid;
    private String createTime;
	private String substituteLinkoid;
    private String alternateLinkoid;
    
    private String level = "Default";

    public EffectiveBaselineBean(String processoid, WTPart part) throws WTException{
    	this.processoid = processoid;
        this.beanUID = UUID.randomUUID().toString();
        this.partoid = MversionControlHelper.getOidByObject(part);
        this.partvid = MversionControlHelper.getVidByObject(part);
        this.partNumber = part.getNumber();
        this.partName = part.getName();
        this.partState = part.getState().toString();
        this.createTime = MversionControlHelper.getCurrentDate();
        
        List<String> listAlt = new ArrayList<>();
        List<WTPartAlternateLink> altLinkList = EffecitveBaselineUtil.getAlternateLinks(part);
    	if(!altLinkList.isEmpty()) {
  		  for(WTPartAlternateLink altLink : altLinkList) {
  			  WTPartMaster sm = altLink.getAlternates();
  			  listAlt.add(sm.getNumber());
  		  }
  		 }
    	this.alternateLinkoid =	StringUtils.strip(listAlt.toString(),"[]")	== null ? "" :  StringUtils.strip(listAlt.toString(),"[]");
    }
    public EffectiveBaselineBean(String processoid, String upUID, WTPart parent, WTPart part, String usageLinkoid) throws WTException{
    	this.processoid = processoid;
        this.upUID = upUID;
        this.beanUID = UUID.randomUUID().toString();
        this.partoid = MversionControlHelper.getOidByObject(part);
        this.partvid = MversionControlHelper.getVidByObject(part);
        this.partNumber = part.getNumber();
        this.partName = part.getName();
        this.partState = part.getLifeCycleState().toString();
        this.parentoid = MversionControlHelper.getOidByObject(parent);
        this.parentvid = MversionControlHelper.getVidByObject(parent);
        this.parenNumber = parent.getNumber();
        this.parenName = parent.getName();
        this.parenState = parent.getState().toString();
        this.usageLinkoid = usageLinkoid;
        this.createTime = MversionControlHelper.getCurrentDate();
        
        List<String> listAlt = new ArrayList<>();
        List<WTPartAlternateLink> altLinkList = EffecitveBaselineUtil.getAlternateLinks(part);
    	if(!altLinkList.isEmpty()) {
  		  for(WTPartAlternateLink altLink : altLinkList) {
  			  WTPartMaster sm = altLink.getAlternates();
  			  listAlt.add(sm.getNumber());
  		  }
  		 }
    	this.alternateLinkoid =	StringUtils.strip(listAlt.toString(),"[]") == null ? "" :  StringUtils.strip(listAlt.toString(),"[]");
    	
    	List<String> listSub = new ArrayList<>();
    	WTPartUsageLink link = (WTPartUsageLink)EffecitveBaselineUtil.getObjectByOid(usageLinkoid);
    	if(link != null) {
    	List<WTPartSubstituteLink> subLinkList = EffecitveBaselineUtil.getSubstituteLinks(link);
    	if(!subLinkList.isEmpty()) {
    		  for(WTPartSubstituteLink subLink : subLinkList) {
    			  WTPartMaster sm = subLink.getSubstitutes();
    			  listSub.add(sm.getNumber());
    		  }
    	}
    	}
    	this.substituteLinkoid =	StringUtils.strip(listSub.toString(),"[]") == null ? "" :  StringUtils.strip(listSub.toString(),"[]");
    }
    
    
    public EffectiveBaselineBean(String processoid, WTPart part,WTPartAlternateLink alternateLink,String upUID){
    	this.processoid = processoid;
        this.beanUID = UUID.randomUUID().toString();
        this.partoid = MversionControlHelper.getOidByObject(part);
        this.partvid = MversionControlHelper.getVidByObject(part);
        this.upUID = upUID;
        this.partNumber = part.getNumber();
        this.partName = part.getName();
        this.partState = part.getState().toString();
        this.createTime = MversionControlHelper.getCurrentDate();
        this.alternateLinkoid = MversionControlHelper.getOidByObject(alternateLink);
    }
    
    public EffectiveBaselineBean(String processoid, String upUID, WTPart parent, WTPart part, String usageLinkoid,WTPartSubstituteLink substituteLink){
    	this.processoid = processoid;
        this.upUID = upUID;
        this.beanUID = UUID.randomUUID().toString();
        this.partoid = MversionControlHelper.getOidByObject(part);
        this.partvid = MversionControlHelper.getVidByObject(part);
        this.partNumber = part.getNumber();
        this.partName = part.getName();
        this.partState = part.getLifeCycleState().toString();
        this.parentoid = MversionControlHelper.getOidByObject(parent);
        this.parentvid = MversionControlHelper.getVidByObject(parent);
        this.parenNumber = parent.getNumber();
        this.parenName = parent.getName();
        this.parenState = parent.getState().toString();
        this.usageLinkoid = usageLinkoid;
        this.createTime = MversionControlHelper.getCurrentDate();
        this.substituteLinkoid = MversionControlHelper.getOidByObject(substituteLink);
    }
    
    public EffectiveBaselineBean(String processoid, String upUID, WTPart parent, WTPart part, String usageLinkoid,WTPartAlternateLink alternateLink){
    	this.processoid = processoid;
        this.upUID = upUID;
        this.beanUID = UUID.randomUUID().toString();
        this.partoid = MversionControlHelper.getOidByObject(part);
        this.partvid = MversionControlHelper.getVidByObject(part);
        this.partNumber = part.getNumber();
        this.partName = part.getName();
        this.partState = part.getLifeCycleState().toString();
        this.parentoid = MversionControlHelper.getOidByObject(parent);
        this.parentvid = MversionControlHelper.getVidByObject(parent);
        this.parenNumber = parent.getNumber();
        this.parenName = parent.getName();
        this.parenState = parent.getState().toString();
        this.usageLinkoid = usageLinkoid;
        this.createTime = MversionControlHelper.getCurrentDate();
        this.alternateLinkoid = MversionControlHelper.getOidByObject(alternateLink);
    }

    public EffectiveBaselineBean(){
    }

    public String getoid(){
        return this.processoid+"___"+this.beanUID+"___"+this.level+"___"+this.upUID+"___"+this.partoid+"___"+this.partvid+"___"+this.partNumber
                +"___"+this.parentoid+"___"+this.parentvid+"___"+this.parenNumber+"___"+this.usageLinkoid+"___"+this.partState+"___"+this.parenState+"___"+this.createTime+"___"+this.substituteLinkoid+"___"+this.alternateLinkoid;
    }
    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getProcessoid() {
        return processoid;
    }

    public void setProcessoid(String processoid) {
        this.processoid = processoid;
    }

    public String getBeanUID() {
        return beanUID;
    }

    public void setBeanUID(String beanUID) {
        this.beanUID = beanUID;
    }

    public String getUpUID() {
        return upUID;
    }

    public void setUpUID(String upUID) {
        this.upUID = upUID;
    }

    public String getPartoid() {
        return partoid;
    }

    public void setPartoid(String partoid) {
        this.partoid = partoid;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public String getParentoid() {
        return parentoid;
    }

    public void setParentoid(String parentoid) {
        this.parentoid = parentoid;
    }

    public String getParenNumber() {
        return parenNumber;
    }

    public void setParenNumber(String parenNumber) {
        this.parenNumber = parenNumber;
    }

    public String getParenName() {
        return parenName;
    }

    public void setParenName(String parenName) {
        this.parenName = parenName;
    }

    public String getUsageLinkoid() {
        return usageLinkoid;
    }

    public void setUsageLinkoid(String usageLinkoid) {
        this.usageLinkoid = usageLinkoid;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getPartvid() {
        return partvid;
    }

    public void setPartvid(String partvid) {
        this.partvid = partvid;
    }

    public String getParentvid() {
        return parentvid;
    }

    public void setParentvid(String parentvid) {
        this.parentvid = parentvid;
    }

    public String getPartState() {
		return partState;
	}
	public void setPartState(String partState) {
		this.partState = partState;
	}
	public String getParenState() {
		return parenState;
	}
	public void setParenState(String parenState) {
		this.parenState = parenState;
	}
	
    public String getSubstituteLinkoid() {
		return substituteLinkoid;
	}
	public void setSubstituteLinkoid(String substituteLinkoid) {
		this.substituteLinkoid = substituteLinkoid;
	}
	public String getAlternateLinkoid() {
		return alternateLinkoid;
	}
	public void setAlternateLinkoid(String alternateLinkoid) {
		this.alternateLinkoid = alternateLinkoid;
	}
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EffectiveBaselineBean that = (EffectiveBaselineBean) o;
        return Objects.equals(beanUID, that.beanUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beanUID);
    }

    @Override
    public String toString() {
        return "EffectiveBaselineBean{" +
                "processoid='" + processoid + '\'' +
                ", beanUID='" + beanUID + '\'' +
                ", upUID='" + upUID + '\'' +
                ", partoid='" + partoid + '\'' +
                ", partvid='" + partvid + '\'' +
                ", partNumber='" + partNumber + '\'' +
                ", parentoid='" + parentoid + '\'' +
                ", parentvid='" + parentvid + '\'' +
                ", parenNumber='" + parenNumber + '\'' +
                ", partState='" + partState + '\'' +
                ", parenState='" + parenState + '\'' +
                ", createTime='" + createTime + '\'' +
                ", substituteLinkoid='" + substituteLinkoid + '\'' +
                ", alternateLinkoid='" + alternateLinkoid + '\'' +
                '}';
    }
}
