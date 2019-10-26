package ext.appo.part.resource;

import wt.util.resource.RBEntry;
import wt.util.resource.RBUUID;
import wt.util.resource.WTListResourceBundle;

@RBUUID("ext.appo.part.resource.BaselineResourceRB")
public class BaselineResourceRB extends WTListResourceBundle {

	@RBEntry("Baseline Material Information Table")
	public static final String showBaseline_10 = "showBaseline.showBaselineMaterialsTable.description";

	@RBEntry("Export Effective baseline data")
	public static final String showBaseline_20 = "showBaseline.exportBaselineMaterials.description";

	@RBEntry("Export Effective baseline data")
	public static final String showBaseline_30 = "showBaseline.exportBaselineMaterials.tooltip";

	@RBEntry("netmarkets/images/export.gif")
	public static final String showBaseline_40 = "showBaseline.exportBaselineMaterials.icon";

	@RBEntry("ChangeStateECNTableBuilder")
	public static final String PRIVATE_CONSTANT_34 = "ChangeStateECN.ChangeStateECNTableBuilder.description";

	@RBEntry("Manual Baseline Sending")
	public static final String showBaseline_50 = "showBaseline.manualSendEffecitveBaseline.description";

	@RBEntry("ECN proess must select pmc，test ，purchase，quantity，manufactureing role,if it must be choose")
	public static final String message_35 = "select role message";
}