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
                    javaDoc = "ECN对象VID"
                ),
                @GeneratedProperty(
                        name = "perBranchIdentifier",
                        type = String.class,
                        javaDoc = "Persistable对象VID"
                ),
                @GeneratedProperty(
                        name = "ecaIdentifier",
                        type = String.class,
                        javaDoc = "ECA对象VID"
                ),
                @GeneratedProperty(
                        name = "linkType",
                        type = String.class,
                        javaDoc = "类型"
                ),
                @GeneratedProperty(
                        name = "aadDescription",
                        type = String.class,
                        javaDoc = "更改详细描述",
                        constraints = @PropertyConstraints(upperLimit = 2000)
                ),
                @GeneratedProperty(
                        name = "routing",
                        type = String.class,
                        javaDoc = "路由"
                ),
                @GeneratedProperty(
                        name = "approvalOpinion",
                        type = String.class,
                        javaDoc = "审批意见"
                ),
                @GeneratedProperty(
                        name = "remark",
                        type = String.class,
                        javaDoc = "备注（驳回必填）",
                        constraints = @PropertyConstraints(upperLimit = 2000)
                ),
                @GeneratedProperty(
                        name = "collection",
                        type = String.class,
                        javaDoc = "收集对象",
                        constraints = @PropertyConstraints(upperLimit = 2000)
                ),
                @GeneratedProperty(
                        name = "treatment",
                        type = String.class,
                        javaDoc = "处理方式",
                        constraints = @PropertyConstraints(upperLimit = 2000)
                )
        },
        tableProperties = @TableProperties(
                tableName = "APPO_CORRELATIONOBJECTLINK"
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
