package ext.appo.change.util;

import ext.generic.wfbaseline.bean.BaselineTopRulesInfoBean;
import ext.generic.wfbaseline.util.BaselineGenerationUtil;
import ext.generic.wfbaseline.util.BaselineUtil;
import org.apache.log4j.Logger;
import wt.change2.Changeable2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.log4j.LogR;
import wt.util.WTException;

import java.util.*;

public class ExtBaselineGenerationUtil extends BaselineGenerationUtil {

    private static Logger LOGGER = LogR.getLogger(ExtBaselineGenerationUtil.class.getName());

    @Override
    public Map<Persistable, Set<Persistable>> collectObject(Persistable persistable, Object self, List<BaselineTopRulesInfoBean> list) throws WTException {
        HashMap objMap = new HashMap();
        try {
            if (persistable != null && self != null && list != null && list.size() != 0) {
                if (persistable instanceof WTChangeOrder2) {
                    WTChangeOrder2 ecn = (WTChangeOrder2) persistable;
                    Set<Persistable> afterSet = new BaselineUtil().getChangeablesAfter(ecn);
                    if (afterSet != null) {
                        for (Persistable afterPt : afterSet) {
                            Set<Persistable> collectSet = this.collectObject(afterPt, list);
                            if (collectSet != null && collectSet.size() > 0) {
                                objMap.put(afterPt, collectSet);
                            }
                        }
                    }
                } else if (persistable instanceof WTChangeActivity2) {
                    WTChangeActivity2 activity2 = (WTChangeActivity2) persistable;
                    Collection<Changeable2> collection = ModifyUtils.getChangeablesAfter(activity2);
                    LOGGER.info("=====collectObject.collection: " + collection);
                    for (Changeable2 changeable2 : collection) {
                        Set<Persistable> collectSet = this.collectObject(changeable2, list);
                        if (collectSet != null && collectSet.size() > 0) {
                            objMap.put(changeable2, collectSet);
                        }
                    }
                } else {
                    Set<Persistable> collectSet = this.collectObject(persistable, list);
                    if (collectSet != null && collectSet.size() > 0) {
                        objMap.put(persistable, collectSet);
                    }
                }
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
        return objMap;
    }

}