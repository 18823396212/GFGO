package ext.appo.util;


public class ResultMessage {
	boolean succeed;
	
	boolean has_countersign_people_role;
	public boolean isHas_countersign_people_role() {
		return has_countersign_people_role;
	}
	public void setHas_countersign_people_role(boolean has_countersign_people_role) {
		this.has_countersign_people_role = has_countersign_people_role;
	}
	public boolean isHas_SE_role() {
		return has_SE_role;
	}
	public void setHas_SE_role(boolean has_SE_role) {
		this.has_SE_role = has_SE_role;
	}
	boolean has_SE_role;
	public boolean isSucceed() {
		return succeed;
	}
	public void setSucceed(boolean succeed) {
		this.succeed = succeed;
	}
	public StringBuffer getMessage() {
		return message;
	}
	public void setMessage(StringBuffer message) {
		this.message = message;
	}
	StringBuffer message;
	
	

}
