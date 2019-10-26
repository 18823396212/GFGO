package ext.appo.part.beans;

import com.ptc.core.components.forms.CreateEditFormProcessorHelper;
import com.ptc.netmarkets.model.NmObject;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.model.NmSimpleOid;

public class ProductNamingNoticBean extends NmObject {
	
	private NmOid oid;

	public ProductNamingNoticBean(){
		super() ;
		NmSimpleOid simpleOid = new NmSimpleOid();
        String id =  String.valueOf(System.nanoTime());
        simpleOid.setInternalName(CreateEditFormProcessorHelper.NEW_ROW_OBJ +id);
        this.oid = simpleOid;
	}
	
	public NmOid getOid() {
		return oid;
	}

	public void setOid(NmOid oid) {
		this.oid = oid;
	}
}
