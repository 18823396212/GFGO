package ext.appo.change;

import ext.appo.change.interfaces.ModifyService;
import ext.appo.change.services.StandardModifyService;

public class ModifyHelper {

    private static final long serialVersionUID = 1L;
    public static final ModifyService service = getService();

    public static StandardModifyService getService() {
        return new StandardModifyService();
    }

}
