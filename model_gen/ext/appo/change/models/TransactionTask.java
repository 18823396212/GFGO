package ext.appo.change.models;

import com.ptc.windchill.annotations.metadata.GenAsPersistable;
import com.ptc.windchill.annotations.metadata.GeneratedProperty;
import com.ptc.windchill.annotations.metadata.TableProperties;
import wt.fc.WTObject;
import wt.util.WTException;

@GenAsPersistable(
        superClass = WTObject.class,
        properties = {
                @GeneratedProperty(
                        name = "changeTheme",
                        type = String.class,
                        javaDoc = "变更主题"
                ),
                @GeneratedProperty(
                        name = "changeDescribe",
                        type = String.class,
                        javaDoc = "变更任务描述"
                ),
                @GeneratedProperty(
                        name = "responsible",
                        type = String.class,
                        javaDoc = "责任人"
                ),
                @GeneratedProperty(
                        name = "needDate",
                        type = String.class,
                        javaDoc = "期望完成日期"
                ),
                @GeneratedProperty(
                        name = "changeActivity2",
                        type = String.class,
                        javaDoc = "ECA对象VID"
                )
        },
        tableProperties = @TableProperties(
                tableName = "GFGO_TRANSACTIONTASK"
        )
)
public class TransactionTask extends _TransactionTask {
    static final long serialVersionUID = 1L;

    public static TransactionTask newTransactionTask() throws WTException {
        TransactionTask task = new TransactionTask();
        task.initialize();
        return task;
    }

}