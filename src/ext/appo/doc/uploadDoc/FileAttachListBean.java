package ext.appo.doc.uploadDoc;

public class FileAttachListBean {
	
	  
		 private String fileID;                    
         
         private String attachName ;                //附件名
         
         private String guidName ;                  //uuid
         
         private String extensionName ;             //后缀名
         
         private String uerNo;                      //料号
         
         private String fileSize;                   //文件大小
         
         public FileAttachListBean() {
     		super();
     	}
         
         public String getFileID() {
     		return fileID;
     	}

     	public void setFileID(String fileID) {
     		this.fileID = fileID;
     	}

     	public String getAttachName() {
     		return attachName;
     	}

     	public void setAttachName(String attachName) {
     		this.attachName = attachName;
     	}

     	public String getGuidName() {
     		return guidName;
     	}

     	public void setGuidName(String guidName) {
     		this.guidName = guidName;
     	}

     	public String getExtensionName() {
     		return extensionName;
     	}

     	public void setExtensionName(String extensionName) {
     		this.extensionName = extensionName;
     	}

     	public String getUerNo() {
     		return uerNo;
     	}

     	public void setUerNo(String uerNo) {
     		this.uerNo = uerNo;
     	}

     	public String getFileSize() {
     		return fileSize;
     	}

     	public void setFileSize(String fileSize) {
     		this.fileSize = fileSize;
     	}
     	
     	public FileAttachListBean(String fileID, String attachName, String guidName, String extensionName, String uerNo,
    			String fileSize) {
    		super();
    		this.fileID = fileID;
    		this.attachName = attachName;
    		this.guidName = guidName;
    		this.extensionName = extensionName;
    		this.uerNo = uerNo;
    		this.fileSize = fileSize;
    	}
         
}
