package ext.appo.change.models;

import com.ptc.windchill.annotations.metadata.GenAsPersistable;
import com.ptc.windchill.annotations.metadata.GeneratedProperty;
import com.ptc.windchill.annotations.metadata.TableProperties;
import wt.fc.ObjectReference;
import wt.fc.WTObject;
import wt.util.WTException;

@GenAsPersistable(
        superClass = WTObject.class,
        properties = {
                @GeneratedProperty(
                        name = "templateOid",
                        type = String.class,
                        javaDoc = "模板ID"
                ),
                @GeneratedProperty(
                        name = "templateName",
                        type = String.class,
                        javaDoc = "模板名称"
                ),
                @GeneratedProperty(
                        name = "showTemplate",
                        type = String.class,
                        javaDoc = "显示"
                ),
                @GeneratedProperty(
                        name = "userName",
                        type = String.class,
                        javaDoc = "用户名"
                ),
                @GeneratedProperty(
                        name = "userFullName",
                        type = String.class,
                        javaDoc = "用户全名"
                ),
                @GeneratedProperty(
                        name = "saveUser",
                        type = ObjectReference.class,
                        javaDoc = "保存用户"
                ),
        },
        tableProperties = @TableProperties(
                tableName = "APPO_MANAGETEAMTEMPLATE_SHOW"
        )
)
public class ManageTeamTemplateShow extends _ManageTeamTemplateShow {
    static final long serialVersionUID = 1L;

    public static ManageTeamTemplateShow newManageTeamTemplateShow() throws WTException {
        ManageTeamTemplateShow manageTeamTemplateShow = new ManageTeamTemplateShow();
        manageTeamTemplateShow.initialize();
        return manageTeamTemplateShow;
    }

}