package ext.generic.integration.cis.resource;  

import wt.util.resource.RBEntry;
import wt.util.resource.RBUUID;
import wt.util.resource.WTListResourceBundle;
  
@RBUUID("ext.generic.integration.cis.resource.CisActionsRB")
public class CisActionsRB extends WTListResourceBundle {
	
	@RBEntry("sendToCis")
	public static final String CIS_SENDTOCIS_TITLE = "cis.sendToCis.title";

	@RBEntry("sendToCis")
	public static final String CIS_SENDTOCIS_DESC = "cis.sendToCis.description";

	@RBEntry("sendToCis")
	public static final String CIS_SENDTOCIS_TOOLTIP = "cis.sendToCis.tooltip";
	
}
