package ext.appo.change.beans;

import com.ptc.core.components.forms.CreateEditFormProcessorHelper;
import com.ptc.netmarkets.model.NmObject;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.model.NmSimpleOid;

public class CollectItemsBean extends NmObject {

    private NmOid oid;

    public CollectItemsBean() {
        super();
        NmSimpleOid simpleOid = new NmSimpleOid();
        String id = String.valueOf(System.nanoTime());
        simpleOid.setInternalName(CreateEditFormProcessorHelper.NEW_ROW_OBJ + id);
        this.oid = simpleOid;
    }

    public CollectItemsBean(String parameter) {
        NmSimpleOid simpleOid = new NmSimpleOid();
        simpleOid.setInternalName(parameter);
        this.oid = simpleOid;
    }

    /**
     * 名称
     */
    private String name;

    /**
     * 编号
     */
    private String number;

    /**
     * 版本
     */
    private String pVersion;

    /**
     * 状态
     */
    private String pState;

    /**
     * 规格描述
     */
    private String ggms;

    /**
     * 规格描述
     */
    private String collection;

    @Override
    public NmOid getOid() {
        return oid;
    }

    @Override
    public void setOid(NmOid oid) {
        this.oid = oid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getpVersion() {
        return pVersion;
    }

    public void setpVersion(String pVersion) {
        this.pVersion = pVersion;
    }

    public String getpState() {
        return pState;
    }

    public void setpState(String pState) {
        this.pState = pState;
    }

    public String getGgms() {
        return ggms;
    }

    public void setGgms(String ggms) {
        this.ggms = ggms;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CollectItemsBean [oid=");
        builder.append(oid);
        builder.append(", name=");
        builder.append(name);
        builder.append(", number=");
        builder.append(number);
        builder.append(", version=");
        builder.append(pVersion);
        builder.append(", state=");
        builder.append(pState);
        builder.append(", ggms=");
        builder.append(ggms);
        builder.append(", collection=");
        builder.append(collection);
        builder.append("]");
        return builder.toString();
    }
}