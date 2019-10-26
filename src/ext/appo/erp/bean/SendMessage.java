package ext.appo.erp.bean;

public class SendMessage {

    private String partNumber;//物料编码
    private String version;//物料版本
    private String isSuccess;//标志
    private String message;//详情
    private String view;//视图

    public String getView() { return view; }

    public void setView(String view) { this.view = view; }

    public String getVersion() { return version; }

    public void setVersion(String version) { this.version = version; }

    public String getPartNumber() {
        return partNumber;
    }

    public String getIsSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public void setIsSuccess(String isSuccess) {
        this.isSuccess = isSuccess;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
