package ext.appo.change.beans;

import com.ptc.core.components.forms.CreateEditFormProcessorHelper;
import com.ptc.netmarkets.model.NmObject;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.model.NmSimpleOid;

public class UsabilityChangeTaskBean extends NmObject {

	private NmOid oid;

	public UsabilityChangeTaskBean(){
		super() ;

		NmSimpleOid simpleOid = new NmSimpleOid();
        String id =  String.valueOf(System.nanoTime());
        simpleOid.setInternalName(CreateEditFormProcessorHelper.NEW_ROW_OBJ +id);
        this.oid = simpleOid;
	}

	public UsabilityChangeTaskBean(String parameter){
		NmSimpleOid simpleOid = new NmSimpleOid();
        simpleOid.setInternalName(parameter);
        this.oid = simpleOid;
	}
	
	// 变更主题
	private String changeTheme ;
	
	// 变更任务描述
	private String changeDescribe ;
	
	// 责任人
	private String responsible ;
	
	// 期望完成日期
	private String needDate ;
	
	// ECA对象 OID
	private String changeActivity2;

	//任务类型
	private String taskType;

	//管理方式
	private String glfs="";

	//任务单号
	private String taskNumber="";

	public NmOid getOid() {
		return oid;
	}

	public void setOid(NmOid oid) {
		this.oid = oid;
	}

	public String getChangeTheme() {
		return changeTheme;
	}

	public void setChangeTheme(String changeTheme) {
		this.changeTheme = changeTheme;
	}

	public String getChangeDescribe() {
		return changeDescribe;
	}

	public void setChangeDescribe(String changeDescribe) {
		this.changeDescribe = changeDescribe;
	}

	public String getResponsible() {
		return responsible;
	}

	public void setResponsible(String responsible) {
		this.responsible = responsible;
	}

	public String getChangeActivity2() {
		return changeActivity2;
	}

	public void setChangeActivity2(String changeActivity2) {
		this.changeActivity2 = changeActivity2;
	}

	public String getNeedDate() {
		return needDate;
	}

	public void setNeedDate(String needDate) {
		this.needDate = needDate;
	}

	public String getTaskType() {
		return taskType;
	}

	public String getGlfs() {
		return glfs;
	}

	public String getTaskNumber() {
		return taskNumber;
	}

	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}

	public void setGlfs(String glfs) {
		this.glfs = glfs;
	}

	public void setTaskNumber(String taskNumber) {
		this.taskNumber = taskNumber;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UsabilityChangeTaskBean [oid=");
		builder.append(oid);
		builder.append(", changeTheme=");
		builder.append(changeTheme);
		builder.append(", changeDescribe=");
		builder.append(changeDescribe);
		builder.append(", responsible=");
		builder.append(responsible);
		builder.append(", needDate=");
		builder.append(needDate);
		builder.append(", changeActivity2=");
		builder.append(changeActivity2);
		builder.append(", taskType=");
		builder.append(taskType);
		builder.append(", glfs=");
		builder.append(glfs);
		builder.append(", taskNumber=");
		builder.append(taskNumber);
		builder.append("]");
		return builder.toString();
	}
}
