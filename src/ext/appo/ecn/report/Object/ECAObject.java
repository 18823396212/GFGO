package ext.appo.ecn.report.Object;

public class ECAObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
		 * 
		 */
	protected String id;
	protected String ECAnumber;
	protected String ECAname;
	protected String ECN_number;
	protected String ECN_name;
	protected String ECN_state;
	protected String ECA_workresponsible;
	protected String ECAcreatetime;
	protected String ECAfinishedtime;
	protected String ECA_before_affectitems;
	protected String ECA_after_affectitems;
	protected String ECA_before_version;
	protected String ECA_after_version;
	protected String ECA_new_version;
	protected String ECA_before_affectitemsnumber;
	protected String ECA_after_affectitemsnumber;
	protected String ECA_before_affectitemsname;
	protected String ECA_after_affectitemsname;
	protected String ECA_before_affectitemsstate;
	protected String ECA_after_affectitemsstate;
	protected String ECA_type;// 更改类型：事务性任务，更改任务
	protected String ECA_state;

	protected String ECA_change_type;
	protected String ECN_creator;
	protected String ECN_create_time;
	protected String ECA_new_state;

	protected String cz;
	protected String rz;
	protected String eddy;
	protected String edgzwd;

	public String getECA_new_state() {
		return ECA_new_state;
	}

	public void setECA_new_state(String eCA_new_state) {
		ECA_new_state = eCA_new_state;
	}

	public ECAObject(String id, String ECAnumber, String ECAname, String ECN_number, String ECA_workresponsible,
			String ECAcreatetime, String ECNfinishedtime, String ECA_before_affectitems, String ECA_after_affectitems,
			String ECA_type, String ECA_state, String ECA_before_affectitemsnumber, String ECA_after_affectitemsnumber,
			String ECA_before_affectitemsname, String ECA_after_affectitemsname, String ECA_before_affectitemsstate,
			String ECN_name, String ECN_state, String ECA_before_version, String ECA_after_version,
			String ECA_new_version, String ECA_change_type, String ECN_creator, String ECN_create_time,
			String ECA_new_state, String ECA_after_affectitemsstate, String cz, String rz, String eddy, String edgzwd) {
		this.cz = cz;
		this.rz = rz;
		this.eddy = eddy;
		this.edgzwd = edgzwd;
		this.id = id;
		this.ECA_change_type = ECA_change_type;
		this.ECN_creator = ECN_creator;
		this.ECN_create_time = ECN_create_time;
		this.ECAnumber = ECAnumber;
		this.ECAname = ECAname;
		this.ECN_number = ECN_number;
		this.ECN_name = ECN_name;
		this.ECN_state = ECN_state;
		this.ECA_workresponsible = ECA_workresponsible;
		this.ECAcreatetime = ECAcreatetime;
		this.ECAfinishedtime = ECAfinishedtime;
		this.ECA_before_affectitems = ECA_before_affectitems;
		this.ECA_after_affectitems = ECA_after_affectitems;
		this.ECA_type = ECA_type;
		this.ECA_state = ECA_state;
		this.ECA_before_affectitemsnumber = ECA_before_affectitemsnumber;
		this.ECA_after_affectitemsnumber = ECA_after_affectitemsnumber;
		this.ECA_before_affectitemsname = ECA_before_affectitemsname;
		this.ECA_after_affectitemsname = ECA_after_affectitemsname;
		this.ECA_before_affectitemsstate = ECA_before_affectitemsstate;
		this.ECA_after_affectitemsstate = ECA_after_affectitemsstate;
		this.ECA_before_version = ECA_before_version;
		this.ECA_after_version = ECA_after_version;
		this.ECA_new_version = ECA_new_version;
		this.ECA_new_version = ECA_new_state;
	}

	public String getCz() {
		return cz;
	}

	public void setCz(String cz) {
		this.cz = cz;
	}

	public String getRz() {
		return rz;
	}

	public void setRz(String rz) {
		this.rz = rz;
	}

	public String getEddy() {
		return eddy;
	}

	public void setEddy(String eddy) {
		this.eddy = eddy;
	}

	public String getEdgzwd() {
		return edgzwd;
	}

	public void setEdgzwd(String edgzwd) {
		this.edgzwd = edgzwd;
	}

	public ECAObject() {
		this.id = "";
		this.ECA_new_state = "";
		this.ECAnumber = "";
		this.ECAname = "";
		this.ECN_number = "";
		this.ECN_name = "";
		this.ECN_state = "";
		this.ECA_workresponsible = "";
		this.ECAcreatetime = "";
		this.ECAfinishedtime = "";
		this.ECA_before_affectitems = "";
		this.ECA_after_affectitems = "";
		this.ECA_type = "";
		this.ECA_state = "";
		this.ECA_before_affectitemsnumber = "";
		this.ECA_after_affectitemsnumber = "";
		this.ECA_before_affectitemsname = "";
		this.ECA_after_affectitemsname = "";
		this.ECA_before_affectitemsstate = "";
		this.ECA_after_affectitemsstate = "";
		this.ECA_before_version = "";
		this.ECA_after_version = "";
		this.ECA_new_version = "";
		this.ECA_change_type = "";
		this.ECN_creator = "";
		this.ECN_create_time = "";
	}

	public String getECA_change_type() {
		return ECA_change_type;
	}

	public void setECA_change_type(String eCA_change_type) {
		ECA_change_type = eCA_change_type;
	}

	public String getECN_creator() {
		return ECN_creator;
	}

	public void setECN_creator(String eCN_creator) {
		ECN_creator = eCN_creator;
	}

	public String getECN_create_time() {
		return ECN_create_time;
	}

	public void setECN_create_time(String eCN_create_time) {
		ECN_create_time = eCN_create_time;
	}

	public String toString() {
		return "id==" + id + "ECAnumber==" + ECAnumber + "ECAname==" + ECAname + "ECA_workresponsible=="
				+ ECA_workresponsible + "ECAcreatetime==" + ECAcreatetime + "ECAfinishedtime==" + ECAfinishedtime
				+ "ECA_before_affectitems size==" + ECA_before_affectitems + "ECA_after_affectitems=="
				+ ECA_after_affectitems + "ECA_type==" + ECA_type + "ECA_state==" + ECA_state
				+ "ECA_before_affectitemsnumber==" + ECA_before_affectitemsnumber + "ECA_after_affectitemsnumber=="
				+ ECA_after_affectitemsnumber + "ECA_before_affectitemsname==" + ECA_before_affectitemsname
				+ "ECA_after_affectitemsname==" + ECA_after_affectitemsname + "ECN_number==" + ECN_number + "ECN_name=="
				+ ECN_name + "ECN_state==" + ECN_state + "ECA_before_affectitemsstate==" + ECA_before_affectitemsstate
				+ "ECA_before_version==" + ECA_before_version + "ECA_after_version==" + ECA_after_version
				+ "ECA_new_version==" + ECA_new_version + "ECA_after_affectitemsstate==" + ECA_after_affectitemsstate;

	}

	public String getECA_before_version() {
		return ECA_before_version;
	}

	public void setECA_before_version(String eCA_before_version) {
		ECA_before_version = eCA_before_version;
	}

	public String getECA_after_version() {
		return ECA_after_version;
	}

	public void setECA_after_version(String eCA_after_version) {
		ECA_after_version = eCA_after_version;
	}

	public String getECA_new_version() {
		return ECA_new_version;
	}

	public void setECA_new_version(String eCA_new_version) {
		ECA_new_version = eCA_new_version;
	}

	public String getECA_before_affectitemsnumber() {
		return ECA_before_affectitemsnumber;
	}

	public void setECA_before_affectitemsnumber(String eCA_before_affectitemsnumber) {
		ECA_before_affectitemsnumber = eCA_before_affectitemsnumber;
	}

	public String getECA_after_affectitemsnumber() {
		return ECA_after_affectitemsnumber;
	}

	public void setECA_after_affectitemsnumber(String eCA_after_affectitemsnumber) {
		ECA_after_affectitemsnumber = eCA_after_affectitemsnumber;
	}

	public String getECA_before_affectitemsname() {
		return ECA_before_affectitemsname;
	}

	public void setECA_before_affectitemsname(String eCA_before_affectitemsname) {
		ECA_before_affectitemsname = eCA_before_affectitemsname;
	}

	public String getECA_after_affectitemsname() {
		return ECA_after_affectitemsname;
	}

	public void setECA_after_affectitemsname(String eCA_after_affectitemsname) {
		ECA_after_affectitemsname = eCA_after_affectitemsname;
	}

	public String getECA_before_affectitemsstate() {
		return ECA_before_affectitemsstate;
	}

	public void setECA_before_affectitemsstate(String eCA_before_affectitemsstate) {
		ECA_before_affectitemsstate = eCA_before_affectitemsstate;
	}

	public String getECA_after_affectitemsstate() {
		return ECA_after_affectitemsstate;
	}

	public void setECA_after_affectitemsstate(String eCA_after_affectitemsstate) {
		ECA_after_affectitemsstate = eCA_after_affectitemsstate;
	}

	public String getECA_state() {
		return ECA_state;
	}

	public void setECA_state(String eCA_state) {
		ECA_state = eCA_state;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getECAnumber() {
		return ECAnumber;
	}

	public void setECAnumber(String eCAnumber) {
		ECAnumber = eCAnumber;
	}

	public String getECAname() {
		return ECAname;
	}

	public void setECAname(String eCAname) {
		ECAname = eCAname;
	}

	public String getECN_number() {
		return ECN_number;
	}

	public void setECN_number(String eCN_number) {
		ECN_number = eCN_number;
	}

	public String getECN_name() {
		return ECN_name;
	}

	public void setECN_name(String eCN_name) {
		ECN_name = eCN_name;
	}

	public String getECN_state() {
		return ECN_state;
	}

	public void setECN_state(String eCN_state) {
		ECN_state = eCN_state;
	}

	public String getECA_workresponsible() {
		return ECA_workresponsible;
	}

	public void setECA_workresponsible(String eCA_workresponsible) {
		ECA_workresponsible = eCA_workresponsible;
	}

	public String getECAcreatetime() {
		return ECAcreatetime;
	}

	public void setECAcreatetime(String eCAcreatetime) {
		ECAcreatetime = eCAcreatetime;
	}

	public String getECAfinishedtime() {
		return ECAfinishedtime;
	}

	public void setECAfinishedtime(String eCAfinishedtime) {
		ECAfinishedtime = eCAfinishedtime;
	}

	public String getECA_before_affectitems() {
		return ECA_before_affectitems;
	}

	public void setECA_before_affectitems(String eCA_before_affectitems) {
		ECA_before_affectitems = eCA_before_affectitems;
	}

	public String getECA_after_affectitems() {
		return ECA_after_affectitems;
	}

	public void setECA_after_affectitems(String eCA_after_affectitems) {
		ECA_after_affectitems = eCA_after_affectitems;
	}

	public String getECA_type() {
		return ECA_type;
	}

	public void setECA_type(String eCA_type) {
		ECA_type = eCA_type;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
