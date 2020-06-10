package ext.appo.change.beans;

import wt.org.WTUser;

import java.io.Serializable;

public class TeamTemplateBean implements Serializable {
    //模板oid
    private String templateOid = "";
    //名称
    private String templateName = "";
    //显示(true,false)
    private String showTemplate = "";
    //共享(true,false)
    private String shareTemplate = "";
    //创建者
    private String creator = "";
    //创建时间
    private String createData = "";
    //创建用户
    private WTUser user = null;

    public String getTemplateOid() {
        return templateOid;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getShowTemplate() {
        return showTemplate;
    }

    public String getShareTemplate() {
        return shareTemplate;
    }

    public String getCreator() {
        return creator;
    }

    public String getCreateData() {
        return createData;
    }

    public WTUser getUser() {
        return user;
    }

    public void setTemplateOid(String templateOid) {
        this.templateOid = templateOid;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public void setShowTemplate(String showTemplate) {
        this.showTemplate = showTemplate;
    }

    public void setShareTemplate(String shareTemplate) {
        this.shareTemplate = shareTemplate;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setCreateData(String createData) {
        this.createData = createData;
    }

    public void setUser(WTUser user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "TeamTemplateBean{" +
                "templateOid='" + templateOid + '\'' +
                ", templateName='" + templateName + '\'' +
                ", showTemplate='" + showTemplate + '\'' +
                ", shareTemplate='" + shareTemplate + '\'' +
                ", creator='" + creator + '\'' +
                ", createData='" + createData + '\'' +
                ", user=" + user +
                '}';
    }
}
