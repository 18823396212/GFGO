package ext.appo.ecn.beans;

import com.ptc.core.components.forms.CreateEditFormProcessorHelper;
import com.ptc.netmarkets.model.NmObject;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.model.NmSimpleOid;

/***********
 *
 * 受影响物料Bean
 */
public class EDUpperMaterialsBean  {


   private String UUID;
    //ECOOID
    private String ecoOid;
   //流程ID
   private String processoid;
   //受影响物料Oid
    private String partOid;
    //上下层物料Oid
    private String upDownOid;
    //上下层物料状态
    private String state;
    //上下层物料LinkOid
    private String usageLinkoid;
    //创建时间
    private String createTime;


    public EDUpperMaterialsBean(String UUID, String ecoOid, String processoid, String partOid, String upDownOid, String state, String usageLinkoid, String createTime) {
        this.UUID = UUID;
        this.ecoOid = ecoOid;
        this.processoid = processoid;
        this.partOid = partOid;
        this.upDownOid = upDownOid;
        this.state = state;
        this.usageLinkoid = usageLinkoid;
        this.createTime = createTime;
    }

    public EDUpperMaterialsBean() {
    }


    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getEcoOid() {
        return ecoOid;
    }

    public void setEcoOid(String ecoOid) {
        this.ecoOid = ecoOid;
    }

    public String getProcessoid() {
        return processoid;
    }

    public void setProcessoid(String processoid) {
        this.processoid = processoid;
    }

    public String getPartOid() {
        return partOid;
    }

    public void setPartOid(String partOid) {
        this.partOid = partOid;
    }

    public String getUpDownOid() {
        return upDownOid;
    }

    public void setUpDownOid(String upDownOid) {
        this.upDownOid = upDownOid;
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
}
