package ext.appo.doc.uploadDoc;

import java.util.Objects;

public class SendDocDataBean {

    private String sno;//序号

    private String docType;//文档类型

    private String docTypeName;//文档类型显示名称

//    private String docSmallType;//文档小类

    private String docSmallTypeName;//文档小类显示名称

    private String descOrRef;//文档类别（参考文档、说明文档）

    private String isSendDcc;//是否发送dcc

    private String sendType;//发送类型

    private String comment;//备注

    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
        this.sno = sno;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

//    public String getDocSmallType() {
//        return docSmallType;
//    }
//
//    public void setDocSmallType(String docSmallType) {
//        this.docSmallType = docSmallType;
//    }

    public String getDescOrRef() {
        return descOrRef;
    }

    public void setDescOrRef(String descOrRef) {
        this.descOrRef = descOrRef;
    }

    public String getIsSendDcc() {
        return isSendDcc;
    }

    public void setIsSendDcc(String isSendDcc) {
        this.isSendDcc = isSendDcc;
    }

    public String getSendType() {
        return sendType;
    }

    public void setSendType(String sendType) {
        this.sendType = sendType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDocTypeName() {
        return docTypeName;
    }

    public void setDocTypeName(String docTypeName) {
        this.docTypeName = docTypeName;
    }

    public String getDocSmallTypeName() {
        return docSmallTypeName;
    }

    public void setDocSmallTypeName(String docSmallTypeName) {
        this.docSmallTypeName = docSmallTypeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SendDocDataBean that = (SendDocDataBean) o;
        return Objects.equals(docType, that.docType) &&
                Objects.equals(docSmallTypeName, that.docSmallTypeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(docType, docSmallTypeName);
    }
}
