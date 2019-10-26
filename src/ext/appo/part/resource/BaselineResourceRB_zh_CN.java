package ext.appo.part.resource;

import wt.util.resource.RBEntry;
import wt.util.resource.RBUUID;
import wt.util.resource.WTListResourceBundle;

@RBUUID("ext.appo.part.resource.BaselineResourceRB")
public class BaselineResourceRB_zh_CN extends WTListResourceBundle {

	@RBEntry("基线物料信息表")
	public static final String showBaseline_10 = "showBaseline.showBaselineMaterialsTable.description";

	@RBEntry("导出有效基线数据")
	public static final String showBaseline_20 = "showBaseline.exportBaselineMaterials.description";

	@RBEntry("导出有效基线数据")
	public static final String showBaseline_30 = "showBaseline.exportBaselineMaterials.tooltip";

	@RBEntry("netmarkets/images/export.gif")
	public static final String showBaseline_40 = "showBaseline.exportBaselineMaterials.icon";

	@RBEntry("更改物料状态的ECN")
	public static final String PRIVATE_CONSTANT_34 = "ChangeStateECN.ChangeStateECNTableBuilder.description";

	@RBEntry("手工发送基线")
	public static final String showBaseline_50 = "showBaseline.manualSendEffecitveBaseline.description";

	@RBEntry("1、ECN发起者要统筹影响范围确定(线下做影响评估)，确保ECN涉及到的所有产品得到充分变更，涉及其它产品要会签到相关研发代表。      "
			+ "2、已销售或PMC已介入备料的产品设计变更必须选择:NPI代表、PMC代表、采购代表、质量代表、测试代表会签，软件变更可不选择采购代表。")
	public static final String message_35 = "select role message";
}
