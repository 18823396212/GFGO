package ext.appo.change.resource;

import wt.util.resource.RBEntry;
import wt.util.resource.RBUUID;
import wt.util.resource.WTListResourceBundle;

@RBUUID("ext.appo.change.resource.ModifyResource")
public class ModifyResource extends WTListResourceBundle {

    @RBEntry("cache")
    public static final String MY_CUSTOM_1 = "modify.cacheButton.tooltip";
    @RBEntry("cache")
    public static final String MY_CUSTOM_2 = "modify.cacheButton.description";
    @RBEntry("cache")
    public static final String MY_CUSTOM_3 = "modify.cacheButton.title";

    @RBEntry("edit")
    public static final String MY_CUSTOM_4 = "modify.editChangeOrder.description";
    @RBEntry("edit")
    public static final String MY_CUSTOM_5 = "modify.editChangeOrder.title";
    @RBEntry("edit")
    public static final String MY_CUSTOM_6 = "modify.editChangeOrder.tooltip";
    @RBEntry("edit.gif")
    public static final String MY_CUSTOM_7 = "modify.editChangeOrder.icon";

}