package ext.appo.ecn.beans;

import com.ptc.core.components.forms.CreateEditFormProcessorHelper;
import com.ptc.netmarkets.model.NmObject;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.model.NmSimpleOid;

public class ChangeTaskBean extends NmObject {
	
	private NmOid oid;
	
	public ChangeTaskBean(){
		super() ;
		
		NmSimpleOid simpleOid = new NmSimpleOid();
        String id =  String.valueOf(System.nanoTime());
        simpleOid.setInternalName(CreateEditFormProcessorHelper.NEW_ROW_OBJ +id);
        this.oid = simpleOid;
	}
	
	public ChangeTaskBean(String parameter){
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChangeTaskBean [oid=");
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
		builder.append("]");
		return builder.toString();
	}
}
