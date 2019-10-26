package ext.appo.erp.bean;

public class ECNInfoEntity {

    private Integer FReplaceGroup=0;

    private String FRowType="";

    private String FChangeLabel=""; //变更标识 2新增。3删除

    private String FBOMVERSION;//BOM版本

    private String FMATERIALIDCHILD;//子项编码

    private String FParentMaterialId;//父项编码

    private String FMATERIALTYPE="";//子项类型 标准件、替代件

    private String FNUMERATOR="1";//分子

    private String FDENOMINATOR="1";//分母

    private String FMEMO="";//备注

    private String FPOSITIONNO="";//位号

    private  String company="02";//所属公司

    private  boolean isFIsEnable=false;//是否开启辅助属性版本

    private  String version="";//子项BOM版本

    private  String mversion="";//子项大版本
    private String FBomEntryId="";//BOM子项内码（查父项view接口结果下TreeEntity的Id）
//    "  \"FBomEntryId\": \""+FBomEntryIdStr+"\",\n" +//BOM子项内码（查父项view接口结果下TreeEntity的Id）

    private String FECNGroup;////ECN行组别

    public String getFECNGroup() { return FECNGroup; }

    public void setFECNGroup(String FECNGroup) { this.FECNGroup = FECNGroup; }

    public String getFBomEntryId() { return FBomEntryId; }

    public void setFBomEntryId(String FBomEntryId) { this.FBomEntryId = FBomEntryId; }

    public String getMversion() { return mversion; }

    public void setMversion(String mversion) { this.mversion = mversion; }

    public void setVersion(String version) { this.version = version; }

    public String getVersion() { return version; }

    public boolean isFIsEnable() { return isFIsEnable; }

    public void setFIsEnable(boolean FIsEnable) { isFIsEnable = FIsEnable; }

    public String getCompany() { return company; }

    public void setCompany(String company) { this.company = company; }

    public Integer getFReplaceGroup() {
        return FReplaceGroup;
    }

    public void setFReplaceGroup(Integer FReplaceGroup) {
        this.FReplaceGroup = FReplaceGroup;
    }

    public String getFRowType() {
        return FRowType;
    }

    public void setFRowType(String FRowType) {
        this.FRowType = FRowType;
    }

    public String getFChangeLabel() {
        return FChangeLabel;
    }

    public void setFChangeLabel(String FChangeLabel) {
        this.FChangeLabel = FChangeLabel;
    }

    public String getFBOMVERSION() {
        return FBOMVERSION;
    }

    public void setFBOMVERSION(String FBOMVERSION) {
        this.FBOMVERSION = FBOMVERSION;
    }

    public String getFMATERIALIDCHILD() {
        return FMATERIALIDCHILD;
    }

    public void setFMATERIALIDCHILD(String FMATERIALIDCHILD) {
        this.FMATERIALIDCHILD = FMATERIALIDCHILD;
    }

    public String getFMATERIALTYPE() {
        return FMATERIALTYPE;
    }

    public void setFMATERIALTYPE(String FMATERIALTYPE) {
        this.FMATERIALTYPE = FMATERIALTYPE;
    }

    public String getFNUMERATOR() {
        return FNUMERATOR;
    }

    public void setFNUMERATOR(String FNUMERATOR) {
        this.FNUMERATOR = FNUMERATOR;
    }

    public String getFDENOMINATOR() {
        return FDENOMINATOR;
    }

    public void setFDENOMINATOR(String FDENOMINATOR) {
        this.FDENOMINATOR = FDENOMINATOR;
    }

    public String getFMEMO() {
        return FMEMO;
    }

    public void setFMEMO(String FMEMO) {
        this.FMEMO = FMEMO;
    }

    public String getFPOSITIONNO() {
        return FPOSITIONNO;
    }

    public void setFPOSITIONNO(String FPOSITIONNO) {
        this.FPOSITIONNO = FPOSITIONNO;
    }

    public String getFParentMaterialId() {
        return FParentMaterialId;
    }

    public void setFParentMaterialId(String FParentMaterialId) {
        this.FParentMaterialId = FParentMaterialId;
    }

    @Override
    public String toString() {

        String FReplaceGroupStr="";

        if (FReplaceGroup==0||FReplaceGroup==null){
            FReplaceGroupStr ="";
        }else {
            FReplaceGroupStr ="\"FReplaceGroup\":" + FReplaceGroup+"\n,";
        }

        //1光峰，0峰米
        System.out.println("公司："+company);

        //子项BOM版本
        String childrenBOm="";

        //子项BOM版本只传 A,B
        if (FMATERIALIDCHILD.substring(0,1).equals("A")||FMATERIALIDCHILD.substring(0,1).equals("B")) {
            //子项BOM版本
            childrenBOm=" ,\"FBOMID\": {\n" +
                    "            \"FNumber\": \""+version+"\"\n" +
                    "        }\n";
        }


        //子项版本
        String FAuxPropIdStr="";
        if (isFIsEnable&&mversion!=null&&mversion!=""){
            FAuxPropIdStr=",\"FAuxPropId\": {\n" +
                    "                \"FAUXPROPID__FF100001\": {\n" +
                    "                    \"FNumber\": \""+mversion+"\"\n" +
                    "                }\n" +
                    "            }";

        }

        //ECN行组别(变更前变更后要相同)
        String FECNGroupStr="";
        if (FECNGroup!=null&& FECNGroup.trim()!=""){
             FECNGroupStr="\"FECNGroup\":\""+FECNGroup+"\",";
        }


        String FBomEntryIdStr="";
        if (FBomEntryId!=null&&FBomEntryId.trim()!=""){
            FBomEntryIdStr="\"FBomEntryId\":\""+FBomEntryId+"\",";//BOM子项内码（查父项view接口结果下TreeEntity的Id）
        }

        return "{\n" +
                 FReplaceGroupStr+
                FBomEntryIdStr+
                FECNGroupStr+
                " \"FRowType\":\"" + FRowType  +"\"\n"+
                ", \"FChangeLabel\":\"" + FChangeLabel  +"\"\n"+
                ", \"FBOMVERSION\":{\n\"FNumber\":\"" + FBOMVERSION + "\"\n}\n" +
                ", \"FParentMaterialId\":{\n\"FNumber\":\"" + FParentMaterialId + "\"\n}\n" +
                ", \"FMATERIALIDCHILD\":{\n\"FNumber\":\"" + FMATERIALIDCHILD + "\"\n}\n" +
                ", \"FMATERIALTYPE\":\"" + FMATERIALTYPE  +"\"\n"+
                ", \"FNUMERATOR\":\"" + FNUMERATOR  +"\"\n"+
                ", \"FDENOMINATOR\":\"" + FDENOMINATOR  +"\"\n"+
                ", \"FMEMO\":\"" + FMEMO + "\"" +"\n"+
                ", \"FPOSITIONNO\":\"" + FPOSITIONNO + "\"" +"\n"+
                ",\"FChildSupplyOrgId\": {\n" +
                "           \"FNumber\": \""+company+"\"\n" +
                "       }"+//供应组织
                childrenBOm+
//                FAuxPropIdStr+//子项版本
                '}';
    }
}
