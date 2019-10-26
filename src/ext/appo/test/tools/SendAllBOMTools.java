package ext.appo.test.tools;

import ext.appo.common.util.Excel2007Handler;
import ext.appo.erp.constants.ERPConstants;
import ext.appo.erp.util.BomUtil;
import ext.customer.common.IBAUtil;
import ext.generic.generatenumber.rule.model.MergeAttribute;
import org.apache.commons.lang.StringUtils;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.pom.Transaction;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionServerHelper;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;
import wt.vc.views.View;
import wt.vc.views.ViewHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class SendAllBOMTools {

    //返回所有Master
    public static Vector getAllWTPartMaster() throws WTException
    {

        Vector result=new Vector();
        QuerySpec qs = new QuerySpec(WTPartMaster.class);

        QueryResult qr = PersistenceHelper.manager.find(qs);

        while(qr.hasMoreElements())
        {
            WTPartMaster wtPartMaster = (WTPartMaster) qr.nextElement();

            result.add(wtPartMaster);
        }

        return result;
    }

    //历史版本
    //bom在plm系统中只发第一个已归档或已发布状态BOM
    public static Vector getBomHistoricalData (WTPart part, String view){
        Vector resultVector = new Vector();
        QueryResult qrVersions = null;
        Vector result = new Vector();
        try {
            qrVersions = VersionControlHelper.service.allVersionsOf(part.getMaster());
//			System.out.println("qrVersions size"+qrVersions.size());
            while (qrVersions.hasMoreElements()) {
                WTPart t = (WTPart) qrVersions.nextElement();
//				String version = t.getVersionInfo().getIdentifier().getValue() + "." + t.getIterationInfo().getIdentifier().getValue();//物料版本
//				System.out.println("version=========:"+version+";version===="+t.getViewName());
                //同一视图
                if (t.getViewName().equals(view)) {
                    resultVector.add(t);
                }
            }
//            System.out.println("resultVector size=="+resultVector.size());
            //查询结果是降序排序，D,C,B,A
            //只发第一个已归档或已发布状态BOM
            if (resultVector.size()>0){
                //D,C,B,A
                for (int i = 0; i <resultVector.size() ; i++) {
                    WTPart wpart = (WTPart) resultVector.get(i);
                    //物料状态
                    String lifeCycleState = wpart.getLifeCycleState().toString();
                    if (lifeCycleState.equals(ERPConstants.RELEASED)){
                        //已发布
                        result.add(wpart);
						break;
                    }else if(lifeCycleState.equals(ERPConstants.ARCHIVED)){
                        //已归档
                        result.add(wpart);
                        break;
                    }

                }
            }

        } catch (WTException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Vector getAllLatestWTParts (String viewName, String number) throws Exception {
        QuerySpec qs = new QuerySpec(WTPart.class);

        View view = ViewHelper.service.getView(viewName);
        SearchCondition sc = new SearchCondition(WTPart.class, "view.key.id", SearchCondition.EQUAL,
                view.getPersistInfo().getObjectIdentifier().getId());
        qs.appendWhere(sc);
        if (number.trim().length() > 0) {
            qs.appendAnd();
            SearchCondition scNumber = new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL,
                    number.toUpperCase());
            qs.appendWhere(scNumber);
        }

        SearchCondition scLatestIteration = new SearchCondition(WTPart.class, WTAttributeNameIfc.LATEST_ITERATION,
                SearchCondition.IS_TRUE);
        qs.appendAnd();
        qs.appendWhere(scLatestIteration);

        QueryResult qr = PersistenceHelper.manager.find(qs);
        if (qr != null && qr.hasMoreElements())
            qr = (new LatestConfigSpec()).process(qr);

        if (qr != null && qr.hasMoreElements())
            return qr.getObjectVectorIfc().getVector();

        return new Vector();
    }


}
