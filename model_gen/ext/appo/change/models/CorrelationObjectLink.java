package ext.appo.change.models;

import com.ptc.windchill.annotations.metadata.*;
import wt.change2.WTChangeOrder2;
import wt.fc.ObjectToObjectLink;
import wt.fc.Persistable;
import wt.fc.WTObject;
import wt.util.WTException;
import wt.workflow.engine.WfProcess;

@GenAsBinaryLink(
        superClass = ObjectToObjectLink.class,
        extendable = true,
        roleA = @GeneratedRole(
                name = "changeOrder2",
                type = WTChangeOrder2.class
        ),
        roleB = @GeneratedRole(
                name = "persistable",
                type = Persistable.class
        ),
        properties = {
                @GeneratedProperty(
                    name = "ecnBranchIdentifier",
                    type = String.class,
                    javaDoc = "ECA对象VID"
                ),
                @GeneratedProperty(
                        name = "perBranchIdentifier",
                        type = String.class,
                        javaDoc = "Persistable对象VID"
                ),
                @GeneratedProperty(
                        name = "linkType",
                        type = String.class,
                        javaDoc = "类型"
                ),
                @GeneratedProperty(
                        name = "aadDescription",
                        type = String.class,
                        javaDoc = "更改详细描述"
                ),
                @GeneratedProperty(
                        name = "routing",
                        type = String.class,
                        javaDoc = "路由"
                )
        },
        tableProperties = @TableProperties(
                tableName = "GFGO_CORRELATIONOBJECTLINK"
        )
)
public class CorrelationObjectLink extends _CorrelationObjectLink {
    static final long serialVersionUID = 1L;

    public static CorrelationObjectLink newCorrelationObjectLink(WTChangeOrder2 changeOrder2, Persistable persistable) throws WTException {
        CorrelationObjectLink link = new CorrelationObjectLink();
        link.initialize(changeOrder2, persistable);
        return link;
    }

}
