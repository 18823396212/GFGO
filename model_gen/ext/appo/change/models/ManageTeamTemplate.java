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
                        name = "shareTemplate",
                        type = String.class,
                        javaDoc = "共享"
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
                tableName = "APPO_MANAGETEAMTEMPLATE"
        )
)
public class ManageTeamTemplate extends _ManageTeamTemplate {
    static final long serialVersionUID = 1L;

    public static ManageTeamTemplate newManageTeamTemplate() throws WTException {
        ManageTeamTemplate manageTeamTemplate = new ManageTeamTemplate();
        manageTeamTemplate.initialize();
        return manageTeamTemplate;
    }

}