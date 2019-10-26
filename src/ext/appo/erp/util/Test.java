package ext.appo.erp.util;

import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.ecn.util.AffectedMaterialsUtil;
import ext.appo.erp.service.BomReleaseService;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.part.WTPart;
import wt.query.AttributeRange;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Test {


public static Timestamp getDateEndTimeStamp(String dateStr) {
    Timestamp timestamp = null;
    String dateStartStr = "";
    if (dateStr != null && !dateStr.equals("")) {
        dateStartStr = dateStr.replace('\\', '-');
        dateStartStr = dateStartStr.replace('/', '-');
        dateStartStr = dateStartStr + " 23:59:59.999999999";
        timestamp = Timestamp.valueOf(dateStartStr);
    }
    return timestamp;
}

public static Timestamp getDateBeginTimeStamp(String dateStr) {
    Timestamp timestamp = null;
    String dateStartStr = "";
    if (dateStr != null && !dateStr.equals("")) {
        dateStartStr = dateStr.replace('\\', '-');
        dateStartStr = dateStartStr.replace('/', '-');
        dateStartStr = dateStartStr + " 00:00:00.000000001";
        timestamp = Timestamp.valueOf(dateStartStr);
    }
    return timestamp;
}


/**
 * 添加时间搜索范围
 *
 * @param search_class
 *            Class搜索的对象类型
 * @param strValue
 *            　String搜索的条件，比如WTAttributeNameIfc.CREATE_STAMP_NAME对象创建时间
 * @param startData
 *            String开始时间
 * @param endData
 *            String结束时间
 * @return SearchCondition
 * @throws QueryException
 */
public static SearchCondition getTimeRangeSearchCondition(Class search_class, String strValue, String startData, String endData) throws WTException {
    if (startData != null && !startData.equals("")) {
        Timestamp beginTimestamp = getDateBeginTimeStamp(startData);
        if (endData != null && !endData.equals("")) {
            Timestamp endTimestamp = getDateEndTimeStamp(endData);
            AttributeRange timeRange = new AttributeRange(beginTimestamp, endTimestamp);
            if (timeRange != null) {
                SearchCondition scTimeRange = new SearchCondition(search_class, strValue, true, timeRange);
                return scTimeRange;
            }
        } else {
            // endTimestamp = new Timestamp(System.currentTimeMillis());
            SearchCondition scTimeRange = new SearchCondition(search_class, strValue, SearchCondition.GREATER_THAN_OR_EQUAL, beginTimestamp);
            return scTimeRange;
        }
    } else {
        if (endData != null && !endData.equals("")) {
            Timestamp endTimestamp = getDateEndTimeStamp(endData);
            SearchCondition scTimeRange = new SearchCondition(search_class, strValue, SearchCondition.LESS_THAN_OR_EQUAL, endTimestamp);
            return scTimeRange;
        }
    }
    return null;
}



    public static Set<WTPart> test() throws WTException {

        QuerySpec qs = new QuerySpec(WTPart.class);
        SearchCondition sc = getTimeRangeSearchCondition(WTPart.class, WTAttributeNameIfc.MODIFY_STAMP_NAME, "2019/6/30", "2019/7/6");
        qs.appendWhere(sc);
        QueryResult qr = PersistenceHelper.manager.find(qs);


        Set<WTPart> set = new HashSet<>();
        if (qr != null) {
            while (qr.hasMoreElements()) {
                WTPart part = (WTPart) qr.nextElement();

                String number = part.getNumber();

                String n = number.substring(0, 1);

                if (n.equals("A") || n.equals("B")) {
                    WTPart newParentPart = (WTPart) AffectedMaterialsUtil.getLatestVersionByMaster(part.getMaster());
                    if (newParentPart.getLifeCycleState().toString().equals(ChangeConstants.RELEASED)||newParentPart.getLifeCycleState().toString().equals(ChangeConstants.ARCHIVED)){

                        System.out.println("==========Part============="+number);
                        set.add(newParentPart);
                    }

                }

            }
        }

        System.out.println("=========================lsit size================="+set.size());

        return set;

    }


public  static  void  sendBOM() throws WTException {


    Set<WTPart> parts=test();
    for (WTPart p :parts) {
        BomReleaseService.sendBOM(p);
    }


}







}