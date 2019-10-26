package ext.appo.erp.bean;


import wt.part.WTPart;
import wt.util.WTException;

import static ext.appo.erp.util.BomUtil.getLastestWTPartByNumber;
import static ext.appo.erp.util.PartUtil.getSourceCN;

public class FTreeEntity {


    private Integer FReplaceGroup;//项次

    private String FMATERIALIDCHILD;//子件编码

    private String FMATERIALTYPE;//子项类型(3替代件或1标准件)

    private String FNUMERATOR="1";//分子

    private String FDENOMINATOR="1";//分母

    private String FReplacePolicy="";//替代策略

    private String FReplaceType="";//替代方式

    private Boolean FIskeyItem= false;//替代主料

    private String FPOSITIONNO="";//位置号

    private String FMEMO="";//备注

    private String FISSkip="false";//跳层

    private Integer FReplacePriority=0;//替代优先级

    private Integer FMRPPriority=0;//动态优先级

    private String FTIMEUNIT="1";//时间单位

    private String FOWNERTYPEID = "BD_OwnerOrg";//货主类型

    private String F_APPO_ZXCHDJ=""; //最新存货等级

    private String bomVersion="";//子项BOM版本

    private  String version="";//子项大版本

    private  String company="";//所属公司

    private  String childrenView="";//子项视图

    private  String isFIsEnable1="";//是否开启版本

    private  String sfxnj="";//是否虚拟件

    private  String FParentRowId="";//父级行主键

    private  String FRowId;//行标识

    public String getFParentRowId() {
        return FParentRowId;
    }

    public String getFRowId() {
        return FRowId;
    }

    public void setFParentRowId(String FParentRowId) {
        this.FParentRowId = FParentRowId;
    }

    public void setFRowId(String FRowId) {
        this.FRowId = FRowId;
    }



    public String getSfxnj() { return sfxnj; }

    public void setSfxnj(String sfxnj) { this.sfxnj = sfxnj; }

    public String getChildrenView() { return childrenView; }

    public void setChildrenView(String childrenView) { this.childrenView = childrenView; }

    public String getIsFIsEnable1() { return isFIsEnable1;
    }

    public void setIsFIsEnable1(String isFIsEnable1) { this.isFIsEnable1 = isFIsEnable1; }



    public String getCompany() { return company; }

    public void setCompany(String company) { this.company = company; }

    public String getVersion() { return version; }

    public void setVersion(String version) { this.version = version; }

    public String getBomVersion() { return bomVersion; }

    public void setBomVersion(String bomVersion) { this.bomVersion = bomVersion; }

    public String getF_APPO_ZXCHDJ() { return F_APPO_ZXCHDJ; }

    public void setF_APPO_ZXCHDJ(String f_APPO_ZXCHDJ) { F_APPO_ZXCHDJ = f_APPO_ZXCHDJ; }

    public Integer getFReplaceGroup() {
        return FReplaceGroup;
    }

    public void setFReplaceGroup(Integer FReplaceGroup) {
        this.FReplaceGroup = FReplaceGroup;
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

    public String getFReplacePolicy() {
        return FReplacePolicy;
    }

    public void setFReplacePolicy(String FReplacePolicy) {
        this.FReplacePolicy = FReplacePolicy;
    }

    public String getFReplaceType() {
        return FReplaceType;
    }

    public void setFReplaceType(String FReplaceType) {
        this.FReplaceType = FReplaceType;
    }

    public Boolean getFIskeyItem() {
        return FIskeyItem;
    }

    public void setFIskeyItem(Boolean FIskeyItem) {
        this.FIskeyItem = FIskeyItem;
    }


    public String getFTIMEUNIT() {
        return FTIMEUNIT;
    }

    public void setFTIMEUNIT(String FTIMEUNIT) {
        this.FTIMEUNIT = FTIMEUNIT;
    }

    public String getFOWNERTYPEID() {
        return FOWNERTYPEID;
    }

    public void setFOWNERTYPEID(String FOWNERTYPEID) {
        this.FOWNERTYPEID = FOWNERTYPEID;
    }


    public String getFPOSITIONNO() {
        return FPOSITIONNO;
    }

    public void setFPOSITIONNO(String FPOSITIONNO) {
        this.FPOSITIONNO = FPOSITIONNO;
    }

    public String getFMEMO() {
        return FMEMO;
    }

    public void setFMEMO(String FMEMO) {
        this.FMEMO = FMEMO;
    }

    public Integer getFReplacePriority() {
        return FReplacePriority;
    }

    public void setFReplacePriority(Integer FReplacePriority) {
        this.FReplacePriority = FReplacePriority;
    }

    public Integer getFMRPPriority() {
        return FMRPPriority;
    }

    public void setFMRPPriority(Integer FMRPPriority) {
        this.FMRPPriority = FMRPPriority;
    }

    public String getFISSkip() {
        return FISSkip;
    }

    public void setFISSkip(String FISSkip) {
        this.FISSkip = FISSkip;
    }

    @Override
    public String toString() {


//        FTreeEntity fTreeEntity=new FTreeEntity();
//        String isFIsEnable1=fTreeEntity.getIsFIsEnable1();
//        String FMATERIALIDCHILD=fTreeEntity.getFMATERIALIDCHILD();
//        Integer FReplaceGroup=fTreeEntity.getFReplaceGroup();
//        String FMATERIALTYPE=fTreeEntity.getFMATERIALTYPE();
//        Integer FReplacePriority=fTreeEntity.getFReplacePriority();
//        Integer FMRPPriority=fTreeEntity.getFMRPPriority();
//        String FISSkip=fTreeEntity.getFISSkip();
//        String FNUMERATOR=fTreeEntity.getFNUMERATOR();
//        String FDENOMINATOR=fTreeEntity.getFDENOMINATOR();
//        String FReplacePolicy=fTreeEntity.getFReplacePolicy();
//        String FReplaceType=fTreeEntity.getFReplaceType();
//        Boolean FIskeyItem=fTreeEntity.getFIskeyItem();
//        String FPOSITIONNO=fTreeEntity.getFPOSITIONNO();
//        String FMEMO=fTreeEntity.getFMEMO();
//        String FTIMEUNIT=fTreeEntity.getFTIMEUNIT();
//        String FOWNERTYPEID=fTreeEntity.getFOWNERTYPEID();
//        String F_APPO_ZXCHDJ=fTreeEntity.getF_APPO_ZXCHDJ();
//        String bomVersion=fTreeEntity.getBomVersion();
//        String version=fTreeEntity.getVersion();


//        WTPart part=getLastestWTPartByNumber(FMATERIALIDCHILD,childrenView);
//        String source= null;
//        try {
//            source = getSourceCN(part)==null?"":getSourceCN(part);
//        } catch (WTException e) {
//            e.printStackTrace();
//        }
//        System.out.println("源："+source);
        //FM峰米
//        System.out.println("公司："+company);
//        System.out.println("是否虚拟件"+sfxnj);
        System.out.println("是否有BOM"+sfxnj);//改为是否有BOM，0没有BOM，1有BOM
        //子项json
        if (("FM").equals(company)){
            if (isFIsEnable1.equals("true")){
                //FM开启版本
                //子项BOM版本传子项有BOM的
//                if (FMATERIALIDCHILD.substring(0,1).equals("A")||FMATERIALIDCHILD.substring(0,1).equals("B")){
                    if (sfxnj.equals("1")){
                        return "{" +
                                "\"FReplaceGroup\":" + FReplaceGroup + "\n" +
                                ", \"FParentRowId\":\"" + FParentRowId + "\"\n" +//父级行主键
                                ", \"FRowId\":\"" + FRowId + "\"" +//行标识
                                ", \"FMATERIALIDCHILD\":{\"FNumber\":\"" + FMATERIALIDCHILD + "\"}\n" +
                                ", \"FMATERIALTYPE\":\"" + FMATERIALTYPE + "\"\n" +
                                ", \"FReplacePriority\":" + FReplacePriority + "\n" +
                                ", \"FMRPPriority\":" + FMRPPriority + "\n" +
                                ", \"FISSkip\":\"" + FISSkip + "\"\n" +
                                ", \"FNUMERATOR\":" + FNUMERATOR + "\n" +
                                ", \"FDENOMINATOR\":" + FDENOMINATOR + "\n" +
                                ", \"FReplacePolicy\":\"" + FReplacePolicy + "\"\n" +
                                ", \"FReplaceType\":\"" + FReplaceType + "\"\n" +
                                ", \"FIskeyItem\":\"" + FIskeyItem + "\"\n" +
                                ", \"FPOSITIONNO\":\"" + FPOSITIONNO + "\"\n" +
                                ", \"FMEMO\":\"" + FMEMO + "\"\n" +
                                ", \"FTIMEUNIT\":\"" + FTIMEUNIT + "\"\n" +
                                ", \"FOWNERTYPEID\":\"" + FOWNERTYPEID + "\"\n" +
                                ", \"F_APPO_ZXCHDJ\":\"" + F_APPO_ZXCHDJ + "\"\n" +
                                ", \"FChildSupplyOrgId\":{\"FNumber\":\"20\"}\n" +//供应组织，峰米
                                ", \"FSUPPLYORG\":{\"FNumber\":\"20\"}\n" +//发料组织，峰米
                                ", \"FAuxPropId\":{\"FAUXPROPID__FF100001\":{\n" +
                                " \"FNumber\":\""+version+"\"\n" + //大版本
                                " }}\n" +
                                ", \"FBOMID\":{\"FNumber\":\"" + bomVersion + "\"}\n" + //子项BOM版本
                                '}';
                    }else{
                        //不传子项BOM版本
                        return "{" +
                                "\"FReplaceGroup\":" + FReplaceGroup + "\n" +
                                ", \"FParentRowId\":\"" + FParentRowId + "\"\n" +//父级行主键
                                ", \"FRowId\":\"" + FRowId + "\"" +//行标识
                                ", \"FMATERIALIDCHILD\":{\"FNumber\":\"" + FMATERIALIDCHILD + "\"}\n" +
                                ", \"FMATERIALTYPE\":\"" + FMATERIALTYPE + "\"\n" +
                                ", \"FReplacePriority\":" + FReplacePriority + "\n" +
                                ", \"FMRPPriority\":" + FMRPPriority + "\n" +
                                ", \"FISSkip\":\"" + FISSkip + "\"\n" +
                                ", \"FNUMERATOR\":" + FNUMERATOR + "\n" +
                                ", \"FDENOMINATOR\":" + FDENOMINATOR + "\n" +
                                ", \"FReplacePolicy\":\"" + FReplacePolicy + "\"\n" +
                                ", \"FReplaceType\":\"" + FReplaceType + "\"\n" +
                                ", \"FIskeyItem\":\"" + FIskeyItem + "\"\n" +
                                ", \"FPOSITIONNO\":\"" + FPOSITIONNO + "\"\n" +
                                ", \"FMEMO\":\"" + FMEMO + "\"\n" +
                                ", \"FTIMEUNIT\":\"" + FTIMEUNIT + "\"\n" +
                                ", \"FOWNERTYPEID\":\"" + FOWNERTYPEID + "\"\n" +
                                ", \"F_APPO_ZXCHDJ\":\"" + F_APPO_ZXCHDJ + "\"\n" +
                                ", \"FChildSupplyOrgId\":{\"FNumber\":\"20\"}\n" +//供应组织，峰米
                                ", \"FSUPPLYORG\":{\"FNumber\":\"20\"}\n" +//发料组织，峰米
                                ", \"FAuxPropId\":{\"FAUXPROPID__FF100001\":{\n" +
                                " \"FNumber\":\""+version+"\"\n" + //大版本
                                " }}\n" +
                                '}';
                    }

            }else{
                //FM未开启版本
                //子项BOM版本传子项有BOM的
//                if (FMATERIALIDCHILD.substring(0,1).equals("A")||FMATERIALIDCHILD.substring(0,1).equals("B")){
                    if (sfxnj.equals("1")){
                        return "{" +
                                "\"FReplaceGroup\":" + FReplaceGroup + "\n" +
                                ", \"FParentRowId\":\"" + FParentRowId + "\"\n" +//父级行主键
                                ", \"FRowId\":\"" + FRowId + "\"" +//行标识
                                ", \"FMATERIALIDCHILD\":{\"FNumber\":\"" + FMATERIALIDCHILD + "\"}\n" +
                                ", \"FMATERIALTYPE\":\"" + FMATERIALTYPE + "\"\n" +
                                ", \"FReplacePriority\":" + FReplacePriority + "\n" +
                                ", \"FMRPPriority\":" + FMRPPriority + "\n" +
                                ", \"FISSkip\":\"" + FISSkip + "\"\n" +
                                ", \"FNUMERATOR\":" + FNUMERATOR + "\n" +
                                ", \"FDENOMINATOR\":" + FDENOMINATOR + "\n" +
                                ", \"FReplacePolicy\":\"" + FReplacePolicy + "\"\n" +
                                ", \"FReplaceType\":\"" + FReplaceType + "\"\n" +
                                ", \"FIskeyItem\":\"" + FIskeyItem + "\"\n" +
                                ", \"FPOSITIONNO\":\"" + FPOSITIONNO + "\"\n" +
                                ", \"FMEMO\":\"" + FMEMO + "\"\n" +
                                ", \"FTIMEUNIT\":\"" + FTIMEUNIT + "\"\n" +
                                ", \"FOWNERTYPEID\":\"" + FOWNERTYPEID + "\"\n" +
                                ", \"F_APPO_ZXCHDJ\":\"" + F_APPO_ZXCHDJ + "\"\n" +
                                ", \"FChildSupplyOrgId\":{\"FNumber\":\"20\"}\n" +//供应组织，峰米
                                ", \"FBOMID\":{\"FNumber\":\"" + bomVersion + "\"}\n" + //子项BOM版本
                                ", \"FSUPPLYORG\":{\"FNumber\":\"20\"}\n" +//发料组织，峰米
                                '}';
                    }else{
                        //不传子项BOM版本
                        return"{" +
                                "\"FReplaceGroup\":" + FReplaceGroup + "\n" +
                                ", \"FParentRowId\":\"" + FParentRowId + "\"\n" +//父级行主键
                                ", \"FRowId\":\"" + FRowId + "\"" +//行标识
                                ", \"FMATERIALIDCHILD\":{\"FNumber\":\"" + FMATERIALIDCHILD + "\"}\n" +
                                ", \"FMATERIALTYPE\":\"" + FMATERIALTYPE + "\"\n" +
                                ", \"FReplacePriority\":" + FReplacePriority + "\n" +
                                ", \"FMRPPriority\":" + FMRPPriority + "\n" +
                                ", \"FISSkip\":\"" + FISSkip + "\"\n" +
                                ", \"FNUMERATOR\":" + FNUMERATOR + "\n" +
                                ", \"FDENOMINATOR\":" + FDENOMINATOR + "\n" +
                                ", \"FReplacePolicy\":\"" + FReplacePolicy + "\"\n" +
                                ", \"FReplaceType\":\"" + FReplaceType + "\"\n" +
                                ", \"FIskeyItem\":\"" + FIskeyItem + "\"\n" +
                                ", \"FPOSITIONNO\":\"" + FPOSITIONNO + "\"\n" +
                                ", \"FMEMO\":\"" + FMEMO + "\"\n" +
                                ", \"FTIMEUNIT\":\"" + FTIMEUNIT + "\"\n" +
                                ", \"FOWNERTYPEID\":\"" + FOWNERTYPEID + "\"\n" +
                                ", \"F_APPO_ZXCHDJ\":\"" + F_APPO_ZXCHDJ + "\"\n" +
                                ", \"FChildSupplyOrgId\":{\"FNumber\":\"20\"}\n" +//供应组织，峰米
                                ", \"FSUPPLYORG\":{\"FNumber\":\"20\"}\n" +//发料组织，峰米
                                '}';
                    }
            }

        }else if (("CINEAPPO").equals(company)){
            if (isFIsEnable1.equals("true")){
                //FM开启版本
                //子项BOM版本传子项有BOM的
//                if (FMATERIALIDCHILD.substring(0,1).equals("A")||FMATERIALIDCHILD.substring(0,1).equals("B")){
                if (sfxnj.equals("1")){
                    return "{" +
                            "\"FReplaceGroup\":" + FReplaceGroup + "\n" +
                            ", \"FParentRowId\":\"" + FParentRowId + "\"\n" +//父级行主键
                            ", \"FRowId\":\"" + FRowId + "\"" +//行标识
                            ", \"FMATERIALIDCHILD\":{\"FNumber\":\"" + FMATERIALIDCHILD + "\"}\n" +
                            ", \"FMATERIALTYPE\":\"" + FMATERIALTYPE + "\"\n" +
                            ", \"FReplacePriority\":" + FReplacePriority + "\n" +
                            ", \"FMRPPriority\":" + FMRPPriority + "\n" +
                            ", \"FISSkip\":\"" + FISSkip + "\"\n" +
                            ", \"FNUMERATOR\":" + FNUMERATOR + "\n" +
                            ", \"FDENOMINATOR\":" + FDENOMINATOR + "\n" +
                            ", \"FReplacePolicy\":\"" + FReplacePolicy + "\"\n" +
                            ", \"FReplaceType\":\"" + FReplaceType + "\"\n" +
                            ", \"FIskeyItem\":\"" + FIskeyItem + "\"\n" +
                            ", \"FPOSITIONNO\":\"" + FPOSITIONNO + "\"\n" +
                            ", \"FMEMO\":\"" + FMEMO + "\"\n" +
                            ", \"FTIMEUNIT\":\"" + FTIMEUNIT + "\"\n" +
                            ", \"FOWNERTYPEID\":\"" + FOWNERTYPEID + "\"\n" +
                            ", \"F_APPO_ZXCHDJ\":\"" + F_APPO_ZXCHDJ + "\"\n" +
                            ", \"FChildSupplyOrgId\":{\"FNumber\":\"50\"}\n" +//供应组织，中影光峰
                            ", \"FSUPPLYORG\":{\"FNumber\":\"50\"}\n" +//发料组织，中影光峰
                            ", \"FAuxPropId\":{\"FAUXPROPID__FF100001\":{\n" +
                            " \"FNumber\":\""+version+"\"\n" + //大版本
                            " }}\n" +
                            ", \"FBOMID\":{\"FNumber\":\"" + bomVersion + "\"}\n" + //子项BOM版本
                            '}';
                }else{
                    //不传子项BOM版本
                    return "{" +
                            "\"FReplaceGroup\":" + FReplaceGroup + "\n" +
                            ", \"FParentRowId\":\"" + FParentRowId + "\"\n" +//父级行主键
                            ", \"FRowId\":\"" + FRowId + "\"" +//行标识
                            ", \"FMATERIALIDCHILD\":{\"FNumber\":\"" + FMATERIALIDCHILD + "\"}\n" +
                            ", \"FMATERIALTYPE\":\"" + FMATERIALTYPE + "\"\n" +
                            ", \"FReplacePriority\":" + FReplacePriority + "\n" +
                            ", \"FMRPPriority\":" + FMRPPriority + "\n" +
                            ", \"FISSkip\":\"" + FISSkip + "\"\n" +
                            ", \"FNUMERATOR\":" + FNUMERATOR + "\n" +
                            ", \"FDENOMINATOR\":" + FDENOMINATOR + "\n" +
                            ", \"FReplacePolicy\":\"" + FReplacePolicy + "\"\n" +
                            ", \"FReplaceType\":\"" + FReplaceType + "\"\n" +
                            ", \"FIskeyItem\":\"" + FIskeyItem + "\"\n" +
                            ", \"FPOSITIONNO\":\"" + FPOSITIONNO + "\"\n" +
                            ", \"FMEMO\":\"" + FMEMO + "\"\n" +
                            ", \"FTIMEUNIT\":\"" + FTIMEUNIT + "\"\n" +
                            ", \"FOWNERTYPEID\":\"" + FOWNERTYPEID + "\"\n" +
                            ", \"F_APPO_ZXCHDJ\":\"" + F_APPO_ZXCHDJ + "\"\n" +
                            ", \"FChildSupplyOrgId\":{\"FNumber\":\"50\"}\n" +//供应组织，中影光峰
                            ", \"FSUPPLYORG\":{\"FNumber\":\"50\"}\n" +//发料组织，中影光峰
                            ", \"FAuxPropId\":{\"FAUXPROPID__FF100001\":{\n" +
                            " \"FNumber\":\""+version+"\"\n" + //大版本
                            " }}\n" +
                            '}';
                }

            }else{
                //FM未开启版本
                //子项BOM版本传子项有BOM的
//                if (FMATERIALIDCHILD.substring(0,1).equals("A")||FMATERIALIDCHILD.substring(0,1).equals("B")){
                if (sfxnj.equals("1")){
                    return "{" +
                            "\"FReplaceGroup\":" + FReplaceGroup + "\n" +
                            ", \"FParentRowId\":\"" + FParentRowId + "\"\n" +//父级行主键
                            ", \"FRowId\":\"" + FRowId + "\"" +//行标识
                            ", \"FMATERIALIDCHILD\":{\"FNumber\":\"" + FMATERIALIDCHILD + "\"}\n" +
                            ", \"FMATERIALTYPE\":\"" + FMATERIALTYPE + "\"\n" +
                            ", \"FReplacePriority\":" + FReplacePriority + "\n" +
                            ", \"FMRPPriority\":" + FMRPPriority + "\n" +
                            ", \"FISSkip\":\"" + FISSkip + "\"\n" +
                            ", \"FNUMERATOR\":" + FNUMERATOR + "\n" +
                            ", \"FDENOMINATOR\":" + FDENOMINATOR + "\n" +
                            ", \"FReplacePolicy\":\"" + FReplacePolicy + "\"\n" +
                            ", \"FReplaceType\":\"" + FReplaceType + "\"\n" +
                            ", \"FIskeyItem\":\"" + FIskeyItem + "\"\n" +
                            ", \"FPOSITIONNO\":\"" + FPOSITIONNO + "\"\n" +
                            ", \"FMEMO\":\"" + FMEMO + "\"\n" +
                            ", \"FTIMEUNIT\":\"" + FTIMEUNIT + "\"\n" +
                            ", \"FOWNERTYPEID\":\"" + FOWNERTYPEID + "\"\n" +
                            ", \"F_APPO_ZXCHDJ\":\"" + F_APPO_ZXCHDJ + "\"\n" +
                            ", \"FChildSupplyOrgId\":{\"FNumber\":\"50\"}\n" +//供应组织，中影光峰
                            ", \"FBOMID\":{\"FNumber\":\"" + bomVersion + "\"}\n" + //子项BOM版本
                            ", \"FSUPPLYORG\":{\"FNumber\":\"50\"}\n" +//发料组织，中影光峰
                            '}';
                }else{
                    //不传子项BOM版本
                    return"{" +
                            "\"FReplaceGroup\":" + FReplaceGroup + "\n" +
                            ", \"FParentRowId\":\"" + FParentRowId + "\"\n" +//父级行主键
                            ", \"FRowId\":\"" + FRowId + "\"" +//行标识
                            ", \"FMATERIALIDCHILD\":{\"FNumber\":\"" + FMATERIALIDCHILD + "\"}\n" +
                            ", \"FMATERIALTYPE\":\"" + FMATERIALTYPE + "\"\n" +
                            ", \"FReplacePriority\":" + FReplacePriority + "\n" +
                            ", \"FMRPPriority\":" + FMRPPriority + "\n" +
                            ", \"FISSkip\":\"" + FISSkip + "\"\n" +
                            ", \"FNUMERATOR\":" + FNUMERATOR + "\n" +
                            ", \"FDENOMINATOR\":" + FDENOMINATOR + "\n" +
                            ", \"FReplacePolicy\":\"" + FReplacePolicy + "\"\n" +
                            ", \"FReplaceType\":\"" + FReplaceType + "\"\n" +
                            ", \"FIskeyItem\":\"" + FIskeyItem + "\"\n" +
                            ", \"FPOSITIONNO\":\"" + FPOSITIONNO + "\"\n" +
                            ", \"FMEMO\":\"" + FMEMO + "\"\n" +
                            ", \"FTIMEUNIT\":\"" + FTIMEUNIT + "\"\n" +
                            ", \"FOWNERTYPEID\":\"" + FOWNERTYPEID + "\"\n" +
                            ", \"F_APPO_ZXCHDJ\":\"" + F_APPO_ZXCHDJ + "\"\n" +
                            ", \"FChildSupplyOrgId\":{\"FNumber\":\"50\"}\n" +//供应组织，中影光峰
                            ", \"FSUPPLYORG\":{\"FNumber\":\"50\"}\n" +//发料组织，中影光峰
                            '}';
                }

            }

        }else {
            if (isFIsEnable1.equals("true")) {
                //光峰开启版本
                //子项BOM版本传子项有BOM的
//                if (FMATERIALIDCHILD.substring(0, 1).equals("A") || FMATERIALIDCHILD.substring(0, 1).equals("B")) {
                    if (sfxnj.equals("1")) {
                        return "{" +
                                "\"FReplaceGroup\":" + FReplaceGroup + "\n" +
                                ", \"FParentRowId\":\"" + FParentRowId + "\"\n" +//父级行主键
                                ", \"FRowId\":\"" + FRowId + "\"" +//行标识
                                ", \"FMATERIALIDCHILD\":{\"FNumber\":\"" + FMATERIALIDCHILD + "\"}\n" +
                                ", \"FMATERIALTYPE\":\"" + FMATERIALTYPE + "\"\n" +
                                ", \"FReplacePriority\":" + FReplacePriority + "\n" +
                                ", \"FMRPPriority\":" + FMRPPriority + "\n" +
                                ", \"FISSkip\":\"" + FISSkip + "\"\n" +
                                ", \"FNUMERATOR\":" + FNUMERATOR + "\n" +
                                ", \"FDENOMINATOR\":" + FDENOMINATOR + "\n" +
                                ", \"FReplacePolicy\":\"" + FReplacePolicy + "\"\n" +
                                ", \"FReplaceType\":\"" + FReplaceType + "\"\n" +
                                ", \"FIskeyItem\":\"" + FIskeyItem + "\"\n" +
                                ", \"FPOSITIONNO\":\"" + FPOSITIONNO + "\"\n" +
                                ", \"FMEMO\":\"" + FMEMO + "\"\n" +
                                ", \"FTIMEUNIT\":\"" + FTIMEUNIT + "\"\n" +
                                ", \"FOWNERTYPEID\":\"" + FOWNERTYPEID + "\"\n" +
                                ", \"F_APPO_ZXCHDJ\":\"" + F_APPO_ZXCHDJ + "\"\n" +
                                ", \"FChildSupplyOrgId\":{\"FNumber\":\"02\"}\n" +//供应组织，光峰
                                ", \"FSUPPLYORG\":{\"FNumber\":\"02\"}\n" +//发料组织，光峰
                                ", \"FAuxPropId\":{\"FAUXPROPID__FF100001\":{\n" +
                                " \"FNumber\":\"" + version + "\"\n" + //大版本
                                " }}\n" +
                                ", \"FBOMID\":{\"FNumber\":\"" + bomVersion + "\"}\n" + //子项BOM版本
                                '}';
                    } else {
                        //不传子项BOM版本
                        return "{" +
                                "\"FReplaceGroup\":" + FReplaceGroup + "\n" +
                                ", \"FParentRowId\":\"" + FParentRowId + "\"\n" +//父级行主键
                                ", \"FRowId\":\"" + FRowId + "\"" +//行标识
                                ", \"FMATERIALIDCHILD\":{\"FNumber\":\"" + FMATERIALIDCHILD + "\"}\n" +
                                ", \"FMATERIALTYPE\":\"" + FMATERIALTYPE + "\"\n" +
                                ", \"FReplacePriority\":" + FReplacePriority + "\n" +
                                ", \"FMRPPriority\":" + FMRPPriority + "\n" +
                                ", \"FISSkip\":\"" + FISSkip + "\"\n" +
                                ", \"FNUMERATOR\":" + FNUMERATOR + "\n" +
                                ", \"FDENOMINATOR\":" + FDENOMINATOR + "\n" +
                                ", \"FReplacePolicy\":\"" + FReplacePolicy + "\"\n" +
                                ", \"FReplaceType\":\"" + FReplaceType + "\"\n" +
                                ", \"FIskeyItem\":\"" + FIskeyItem + "\"\n" +
                                ", \"FPOSITIONNO\":\"" + FPOSITIONNO + "\"\n" +
                                ", \"FMEMO\":\"" + FMEMO + "\"\n" +
                                ", \"FTIMEUNIT\":\"" + FTIMEUNIT + "\"\n" +
                                ", \"FOWNERTYPEID\":\"" + FOWNERTYPEID + "\"\n" +
                                ", \"F_APPO_ZXCHDJ\":\"" + F_APPO_ZXCHDJ + "\"\n" +
                                ", \"FChildSupplyOrgId\":{\"FNumber\":\"02\"}\n" +//供应组织，光峰
                                ", \"FSUPPLYORG\":{\"FNumber\":\"02\"}\n" +//发料组织，光峰
                                ", \"FAuxPropId\":{\"FAUXPROPID__FF100001\":{\n" +
                                " \"FNumber\":\"" + version + "\"\n" + //大版本
                                " }}\n" +
                                '}';
                    }

            } else {
                //光峰未开启版本
                //子项BOM版本传子项有BOM的
//                if (FMATERIALIDCHILD.substring(0, 1).equals("A") || FMATERIALIDCHILD.substring(0, 1).equals("B")) {
                    if (sfxnj.equals("1")) {
                        return "{" +
                                "\"FReplaceGroup\":" + FReplaceGroup + "\n" +
                                ", \"FParentRowId\":\"" + FParentRowId + "\"\n" +//父级行主键
                                ", \"FRowId\":\"" + FRowId + "\"" +//行标识
                                ", \"FMATERIALIDCHILD\":{\"FNumber\":\"" + FMATERIALIDCHILD + "\"}\n" +
                                ", \"FMATERIALTYPE\":\"" + FMATERIALTYPE + "\"\n" +
                                ", \"FReplacePriority\":" + FReplacePriority + "\n" +
                                ", \"FMRPPriority\":" + FMRPPriority + "\n" +
                                ", \"FISSkip\":\"" + FISSkip + "\"\n" +
                                ", \"FNUMERATOR\":" + FNUMERATOR + "\n" +
                                ", \"FDENOMINATOR\":" + FDENOMINATOR + "\n" +
                                ", \"FReplacePolicy\":\"" + FReplacePolicy + "\"\n" +
                                ", \"FReplaceType\":\"" + FReplaceType + "\"\n" +
                                ", \"FIskeyItem\":\"" + FIskeyItem + "\"\n" +
                                ", \"FPOSITIONNO\":\"" + FPOSITIONNO + "\"\n" +
                                ", \"FMEMO\":\"" + FMEMO + "\"\n" +
                                ", \"FTIMEUNIT\":\"" + FTIMEUNIT + "\"\n" +
                                ", \"FOWNERTYPEID\":\"" + FOWNERTYPEID + "\"\n" +
                                ", \"F_APPO_ZXCHDJ\":\"" + F_APPO_ZXCHDJ + "\"\n" +
                                ", \"FChildSupplyOrgId\":{\"FNumber\":\"02\"}\n" +//供应组织，光峰
                                ", \"FBOMID\":{\"FNumber\":\"" + bomVersion + "\"}\n" + //子项BOM版本
                                ", \"FSUPPLYORG\":{\"FNumber\":\"02\"}\n" +//发料组织，光峰
                                '}';
                    } else {
                        //不传子项BOM版本
                        return "{" +
                                "\"FReplaceGroup\":" + FReplaceGroup + "\n" +
                                ", \"FParentRowId\":\"" + FParentRowId + "\"\n" +//父级行主键
                                ", \"FRowId\":\"" + FRowId + "\"" +//行标识
                                ", \"FMATERIALIDCHILD\":{\"FNumber\":\"" + FMATERIALIDCHILD + "\"}\n" +
                                ", \"FMATERIALTYPE\":\"" + FMATERIALTYPE + "\"\n" +
                                ", \"FReplacePriority\":" + FReplacePriority + "\n" +
                                ", \"FMRPPriority\":" + FMRPPriority + "\n" +
                                ", \"FISSkip\":\"" + FISSkip + "\"\n" +
                                ", \"FNUMERATOR\":" + FNUMERATOR + "\n" +
                                ", \"FDENOMINATOR\":" + FDENOMINATOR + "\n" +
                                ", \"FReplacePolicy\":\"" + FReplacePolicy + "\"\n" +
                                ", \"FReplaceType\":\"" + FReplaceType + "\"\n" +
                                ", \"FIskeyItem\":\"" + FIskeyItem + "\"\n" +
                                ", \"FPOSITIONNO\":\"" + FPOSITIONNO + "\"\n" +
                                ", \"FMEMO\":\"" + FMEMO + "\"\n" +
                                ", \"FTIMEUNIT\":\"" + FTIMEUNIT + "\"\n" +
                                ", \"FOWNERTYPEID\":\"" + FOWNERTYPEID + "\"\n" +
                                ", \"F_APPO_ZXCHDJ\":\"" + F_APPO_ZXCHDJ + "\"\n" +
                                ", \"FChildSupplyOrgId\":{\"FNumber\":\"02\"}\n" +//供应组织，光峰
                                ", \"FSUPPLYORG\":{\"FNumber\":\"02\"}\n" +//发料组织，光峰
                                '}';
                    }
            }
        }
    }


}