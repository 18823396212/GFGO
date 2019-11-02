package ext.appo.ecn.filter;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;
import org.apache.log4j.Logger;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.fc.WTReference;
import wt.log4j.LogR;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class EditECNFilter extends DefaultSimpleValidationFilter {

    private static final Logger LOGGER = LogR.getLogger(EditECNFilter.class.getName());
    private static final String ACTIONNAME_1 = "editEngineeringChangeOrder";
    private static final String ACTIONNAME_2 = "editChangeOrder";
    private static final String TIME = "2019-11-01";
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public UIValidationStatus preValidateAction(UIValidationKey key, UIValidationCriteria uivalidationcriteria) {
        UIValidationStatus uivalidationstatus = UIValidationStatus.HIDDEN;
        WTReference wtreference = uivalidationcriteria.getContextObject();
        if (wtreference != null) {
            Persistable persistable = wtreference.getObject();
            try {
                if (persistable != null && persistable instanceof WTChangeOrder2) {
                    WTChangeOrder2 changeOrder2 = (WTChangeOrder2) persistable;
                    //uivalidationstatus = UIValidationStatus.DISABLED;
                    String stateVal = changeOrder2.getLifeCycleState().toString();
                    // 判断是否是开启
                    if (stateVal.equals("OPEN") || stateVal.equals("REWORK")) {
                        //add by tongwang 20191023 start
                        String actionName = key.getComponentID();
                        Timestamp time = new Timestamp(FORMAT.parse(TIME).getTime());
                        LOGGER.info("=====actionName: " + actionName);
                        LOGGER.info("=====time: " + time);
                        if (ACTIONNAME_1.equals(actionName) && changeOrder2.getCreateTimestamp().before(time)) {
                            uivalidationstatus = UIValidationStatus.ENABLED;
                        } else if (ACTIONNAME_2.equals(actionName) && changeOrder2.getCreateTimestamp().after(time)) {
                            uivalidationstatus = UIValidationStatus.ENABLED;
                        }
                        //add by tongwang 20191023 end
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return uivalidationstatus;
    }
}
