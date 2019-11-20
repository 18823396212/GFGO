package ext.appo.change.report;

import com.ptc.core.lwc.server.LWCNormalizedObject;
import ext.appo.change.ModifyHelper;
import ext.appo.change.beans.AffectedParentPartsBean;
import ext.appo.change.beans.ECNInfoBean;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.models.CorrelationObjectLink;
import ext.generic.reviewObject.model.ProcessReviewObjectLinkHelper;
import ext.pi.PIException;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PICoreHelper;
import ext.pi.core.PIWorkflowHelper;
import wt.change2.ChangeHelper2;
import wt.change2.ChangeOrder2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.*;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;

import java.text.SimpleDateFormat;
import java.util.*;

public class BomChangeReport {

    //通过Oid获取流程
    public static Persistable getObjectByOid(String oid) throws WTException {
        Persistable p = null;

        boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
        try {
            ReferenceFactory referencefactory = new ReferenceFactory();
            WTReference wtreference = referencefactory.getReference(oid);
            p = wtreference.getObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SessionServerHelper.manager.setAccessEnforced(flag);
        }
        return p;
    }

    public static ECNInfoBean getECNInfo(WTChangeOrder2 ecn) throws Exception {
        ECNInfoBean ecnInfoBean=new ECNInfoBean();
        String ecnCreator=getSendPersion(ecn);
        String ecnStartTime=getSendDate(ecn);
        String productType=getChangeAtt(ecn,"sscpx");
        String projectName=getChangeAtt(ecn,"ssxm");
        String changeType=getChangeAtt(ecn,"ChangeItemType");
        String changeReason=getChangeAtt(ecn,"ChangeCause");
        String changePhase=getChangeAtt(ecn,"bgjd");
        String isChangeDrawing=getChangeAtt(ecn,"change_dwg_ornot");
        String changeDescription=ecn.getDescription();

        ecnInfoBean.setEcnCreator(ecnCreator);
        ecnInfoBean.setEcnStartTime(ecnStartTime);
        ecnInfoBean.setProductType(productType);
        ecnInfoBean.setProjectName(projectName);
        ecnInfoBean.setChangeType(changeType);
        ecnInfoBean.setChangeReason(changeReason);
        ecnInfoBean.setChangePhase(changePhase);
        ecnInfoBean.setIsChangeDrawing(isChangeDrawing);
        ecnInfoBean.setChangeDescription(changeDescription);

        return ecnInfoBean;
    }


    // 获取ECN变更属性
    public static String getChangeAtt(Persistable persistable, String para) {
        String comment = "";

        WTChangeOrder2 ecn= (WTChangeOrder2) persistable;

        if ("sscpx".equals(para)) {
            try {
                comment = PIAttributeHelper.service.getDisplayValue(ecn, "sscpx", Locale.CHINA);
                return comment;
            } catch (PIException e) {
                e.printStackTrace();
            }
        }

        Object object = getIBAObjectValue(ecn, para);
//        System.out.println("object====" + object);

        if (object instanceof String) {
            String changeComment = (String) object;
            comment = changeComment;
        }
        if (object instanceof Object[]) {
            Object[] objArr = (Object[]) object;
            for (int i = 0; i < objArr.length; i++) {
                comment = comment + objArr[i].toString() + "  ";
            }
        }
        return comment;
    }


    public static Object getIBAObjectValue(Persistable theObject, String attributeName) {
        Object o;
        try {
            LWCNormalizedObject genericObj = new LWCNormalizedObject(theObject, null, null, null);
            genericObj.load(attributeName);
            o = genericObj.get(attributeName);
        } catch (WTException e) {
            e.printStackTrace();
            String str = "获取属性出错！";
            o = str;
        }
        return o;
    }

    //ECN发起人
    public static String getSendPersion(WTChangeOrder2 ecn) {
        String sendPersion = ecn.getCreatorFullName();
        String count = sendPersion.replaceAll("\\d+", "");
        String[] arry = count.split("\\|");
        return arry[0];
    }

    //发出日期
    public static String getSendDate(WTChangeOrder2 ecn) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return sdf.format(ecn.getCreateTimestamp());
    }

    // 返回对应的流程ecn
    public static WTChangeOrder2 getECNByNumber(String number) throws WTException {

        WTChangeOrder2 result = new WTChangeOrder2();
        QuerySpec qs = new QuerySpec(WTChangeOrder2.class);
        if (!number.isEmpty()){
            SearchCondition scNumber = new SearchCondition(WTChangeOrder2.class, WTChangeOrder2.NUMBER, SearchCondition.EQUAL, number.toUpperCase());
            qs.appendWhere(scNumber);
        }

        QueryResult qr = PersistenceHelper.manager.find(qs);

        while (qr.hasMoreElements()) {
            result = (WTChangeOrder2) qr.nextElement();

        }

        return result;
    }

    //受影响母件信息
    public static List<AffectedParentPartsBean> getAffectedInfo(WTChangeOrder2 ecn) throws WTException {
        List<AffectedParentPartsBean> resultList=new ArrayList<>();
        List<WTPart> parts=getAffectedParts(ecn);
        if (parts!=null&&parts.size()>0){
            for (int i = 0; i < parts.size(); i++) {
                AffectedParentPartsBean affectedInfo=new AffectedParentPartsBean();
                WTPart part=parts.get(i);
                String number=part.getNumber();
                String name=part.getName();
                String version=part.getVersionInfo().getIdentifier().getValue() + "." + part.getIterationInfo().getIdentifier().getValue();
                String state=part.getLifeCycleState().getDisplay(SessionHelper.getLocale());
                String changeDetailedDescription= getIBAvalue(part,"aadDescription");

                affectedInfo.setEffectObjectNumber(number);
                affectedInfo.setEffectObjectName(name);
                affectedInfo.setEffectObjectVersion(version);
                affectedInfo.setEffectObjectState(state);
                affectedInfo.setChangeDetailedDescription(changeDetailedDescription);
                resultList.add(affectedInfo);
            }
        }

        return resultList;
    }


    //获取更改通告中所有的受影响对象
    public static List<WTPart> getAffectedParts(WTChangeOrder2 ecn) throws WTException {
        List<WTPart> parts=new ArrayList<>();
        QueryResult ecaqr = ChangeHelper2.service.getChangeActivities(ecn);

        while (ecaqr.hasMoreElements()) {
            Object ecaobject = ecaqr.nextElement();
            if (ecaobject instanceof WTChangeActivity2) {
                WTChangeActivity2 eca = ((WTChangeActivity2) ecaobject);
                // 查询ECA中所有受影响对象
                QueryResult qr = ChangeHelper2.service.getChangeablesBefore(eca);
                while (qr.hasMoreElements()) {
                    Object object = qr.nextElement();

                    if (object instanceof WTPart) {
                        WTPart part = (WTPart) object;
                        parts.add(part);
                    }
                }
            }
        }
        return parts;
    }

    public static String getIBAvalue(Persistable p, String key) throws PIException {
        Object object = PIAttributeHelper.service.getValue(p, key);
         System.out.println(key+" object物料属性===="+object);
        String comment = "";
        if (object == null) {
            return comment;
        }
        if (object instanceof String) {
            // System.out.println("object为String:"+object);
            String changeComment = (String) PIAttributeHelper.service.getValue(p, key);
            comment = changeComment;
        }
        if (object instanceof Object[]) {
            Object[] objArr = (Object[]) object;
            for (int i = 0; i < objArr.length; i++) {
                // System.out.println("object[]："+objArr[i].toString());
                comment = comment + objArr[i].toString() + ",";
            }
        }
        if (object instanceof Boolean) {
            // System.out.println("IBA为Boolean:"+object.toString());
            comment = object.toString();
        } else {
            // System.out.println("IBA为else:"+object.toString());
            comment = object.toString();
        }
        // System.out.println(key+" commnet物料属性输出 ========="+comment);
        return comment;
    }
}
