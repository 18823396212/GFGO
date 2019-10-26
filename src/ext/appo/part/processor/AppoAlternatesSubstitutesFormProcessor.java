package ext.appo.part.processor;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.windchill.enterprise.part.forms.AlternatesSubstitutesFormProcessor;

import wt.fc.collections.WTHashSet;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.part.WTPartUsageLink;
import wt.util.WTException;

public class AppoAlternatesSubstitutesFormProcessor extends AlternatesSubstitutesFormProcessor {
	private static final Logger log = LogR.getLogger(AppoAlternatesSubstitutesFormProcessor.class.getName());
	private static final String REPLACEMENT_TABLE_ID = "netmarkets.replacements.list";
	private static final String RESOURCE = "wt.part.partResource";
	private static final String OK_BUTTON = "Ok";
	private static final String CANCEL_BUTTON = "Cancel";
	private HashMap updatedItems;
	private HashMap newItems = new HashMap();
	private List<NmOid> addItemOids;
	private List<NmOid> removedItemOids;
	private Boolean checkedOutParent = Boolean.valueOf(false);
	private String whichReplacementsButton = "Ok";
	private WTPartUsageLink checkedOutParentUsageLink = null;
	private WTHashSet addedItemSet = new WTHashSet();
	private WTHashSet removedItemSet = new WTHashSet();
	private WTPart parentWip = null;
	private String redirectURL = null;

	@Override
	public FormResult preProcess(NmCommandBean arg0, List<ObjectBean> arg1) throws WTException {
		log.debug("In preProcess");
		this.whichReplacementsButton = arg0.getTextParameter("whichReplacementsButton");
		if (!this.isCancelButton()) {
			this.updatedItems = getFormData(arg0);
			this.addItemOids = arg0.getAddedItemsByName("netmarkets.replacements.list");
			this.removedItemOids = arg0.getRemovedItemsByName("netmarkets.replacements.list");
		}
         System.out.println("updatedItems===="+updatedItems.size()+"||"+updatedItems);
         System.out.println("addItemOids===="+addItemOids.size()+"||"+addItemOids);
         System.out.println("removedItemOids===="+removedItemOids.size()+"||"+removedItemOids);
		return null;
	}

	
}