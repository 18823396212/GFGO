package ext.appo.ecn.report.Object;

import java.io.Serializable;
import java.util.List;


public class ECNObject implements Serializable,Cloneable{
  
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
/**
	 * 
	 */
  protected String id;
  protected String ECNnumber;
  protected String ECNname;
  protected String ECNdes;
  protected String ECNcreater;
  protected String ECNcreatetime;
  protected String ECNfinishedtime;
  protected String ECN_productline;
  protected String ECN_ownproject;
  protected String ECN_change_type;//变更类型
  protected String ECN_change_reason;
  protected String ECN_change_phase;//变更阶段
  protected String ECN_change_draw;//
  protected String ECN_state;//
  protected List<ECAObject> ecalist;//


  
  public ECNObject(String id,String ECNnumber,String ECNname,String ECNdes,String ECNcreater,String ECNcreatetime,
		  String ECNfinishedtime,String ECN_productline,String ECN_ownproject,String ECN_change_type,
		  String ECN_change_reason,String ECN_change_phase,String ECN_change_draw,String ECN_state,List<ECAObject> ecalist)
  {
	  this.id=id;
	 this.ECNnumber=ECNnumber;
	 this.ECNname=ECNname;
	 this.ECNdes=ECNdes;
	 this.ECNcreater=ECNcreater;
	 this.ECNcreatetime=ECNcreatetime;
	 this.ECNfinishedtime=ECNfinishedtime;
	 this.ECN_productline=ECN_productline;
	 this.ECN_ownproject=ECN_ownproject;
	 this.ECN_change_type=ECN_change_type;
	 this.ECN_change_reason=ECN_change_reason;
	 this.ECN_change_phase=ECN_change_phase;
	 this.ECN_change_draw=ECN_change_draw;
	 this.ECN_state=ECN_state;
	 this.ecalist=ecalist;
  }
  public ECNObject()
  {
	  this.id="";
	 this.ECNnumber="";
	 this.ECNname="";
	 this.ECNdes="";
	 this.ECNcreater="";
	 this.ECNcreatetime="";
	 this.ECNfinishedtime="";
	 this.ECN_productline="";
	 this.ECN_ownproject="";
	 this.ECN_change_type="";
	 this.ECN_change_reason="";
	 this.ECN_change_phase="";
	 this.ECN_change_draw="";
	 this.ECN_state="";
	 this.ecalist=null;
  }
  public List<ECAObject> getEcalist() {
	return ecalist;
}
public void setEcalist(List<ECAObject> ecalist) {
	this.ecalist = ecalist;
}
public String toString()
  {
	  return "id=="+id+"ECNnumber=="+ECNnumber+"ECNname=="+ECNname+"ECNdes=="+ECNdes
	  +"ECNcreater=="+ECNcreater+"ECNcreatetime=="+ECNcreatetime+"ECNfinishedtime=="+ECNfinishedtime+
	  "ECN_productline=="+ECN_productline+"ECN_ownproject=="+ECN_ownproject+"ECN_change_type=="+ECN_change_type+
	  "ECN_change_reason=="+ECN_change_reason+"ECN_change_phase=="+ECN_change_phase
	  +"ECN_change_draw=="+ECN_change_draw+"ECN_state=="+ECN_state+"ecalist=="+ecalist;

  }
public String getECN_state() {
	return ECN_state;
}
public void setECN_state(String eCN_state) {
	ECN_state = eCN_state;
}
public String getId() {
	return id;
}
public void setId(String id) {
	this.id = id;
}
public String getECNnumber() {
	return ECNnumber;
}
public void setECNnumber(String eCNnumber) {
	ECNnumber = eCNnumber;
}
public String getECNname() {
	return ECNname;
}
public void setECNname(String eCNname) {
	ECNname = eCNname;
}
public String getECNdes() {
	return ECNdes;
}
public void setECNdes(String eCNdes) {
	ECNdes = eCNdes;
}
public String getECNcreater() {
	return ECNcreater;
}
public void setECNcreater(String eCNcreater) {
	ECNcreater = eCNcreater;
}
public String getECNcreatetime() {
	return ECNcreatetime;
}
public void setECNcreatetime(String eCNcreatetime) {
	ECNcreatetime = eCNcreatetime;
}
public String getECNfinishedtime() {
	return ECNfinishedtime;
}
public void setECNfinishedtime(String eCNfinishedtime) {
	ECNfinishedtime = eCNfinishedtime;
}
public String getECN_productline() {
	return ECN_productline;
}
public void setECN_productline(String eCN_productline) {
	ECN_productline = eCN_productline;
}
public String getECN_ownproject() {
	return ECN_ownproject;
}
public void setECN_ownproject(String eCN_ownproject) {
	ECN_ownproject = eCN_ownproject;
}
public String getECN_change_type() {
	return ECN_change_type;
}
public void setECN_change_type(String eCN_change_type) {
	ECN_change_type = eCN_change_type;
}
public String getECN_change_reason() {
	return ECN_change_reason;
}
public void setECN_change_reason(String eCN_change_reason) {
	ECN_change_reason = eCN_change_reason;
}
public String getECN_change_phase() {
	return ECN_change_phase;
}
public void setECN_change_phase(String eCN_change_phase) {
	ECN_change_phase = eCN_change_phase;
}
public String getECN_change_draw() {
	return ECN_change_draw;
}
public void setECN_change_draw(String eCN_change_draw) {
	ECN_change_draw = eCN_change_draw;
}
public static long getSerialversionuid() {
	return serialVersionUID;
}

}
