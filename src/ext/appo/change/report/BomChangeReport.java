package ext.appo.change.report;

import com.ptc.core.lwc.server.LWCNormalizedObject;
import ext.appo.change.beans.AffectedParentPartsBean;
import ext.appo.change.beans.BOMChangeInfoBean;
import ext.appo.change.beans.ECNInfoBean;
import ext.appo.ecn.common.util.ChangeUtils;
import ext.pi.PIException;
import ext.pi.core.PIAttributeHelper;
import wt.change2.*;
import wt.fc.*;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;

import java.text.SimpleDateFormat;
import java.util.*;

public class BomChangeReport {

    //通过Oid获取流程
    public static Persistable getObjectByOid(String oid) throws WTException {
        Persistable p = null;

        boolean flag = SessionServerHelper.manager.setAccessEnforced(false);// 忽略权限
        try {
            ReferenceFactory referencefactory = new ReferenceFactory();
            WTReference wtreference = referencefactory.getReference(oid);
            p = wtreference.getObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SessionServerHelper.manager.setAccessEnforced(flag);// 添加权限
        }
        return p;
    }

    public static ECNInfoBean getECNInfo(WTChangeOrder2 ecn){
        boolean flag = SessionServerHelper.manager.setAccessEnforced(false);// 忽略权限
        ECNInfoBean ecnInfoBean=new ECNInfoBean();
        try {
            if (ecn!=null){
                String ecnCreator=getSendPersion(ecn);
                String ecnStartTime=addDate(ecn.getCreateTimestamp().toLocaleString(),8);
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SessionServerHelper.manager.setAccessEnforced(flag);// 添加权限
        }

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
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));//TimeZone时区
        return sdf.format(ecn.getCreateTimestamp())==null?"":sdf.format(ecn.getCreateTimestamp());
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
        boolean flag = SessionServerHelper.manager.setAccessEnforced(false);// 忽略权限

        try {
            if (ecn!=null){
                List<WTPart> parts=getAffectedParts(ecn);
                if (parts!=null&&parts.size()>0){
                    for (int i = 0; i < parts.size(); i++) {
                        String changeDetailedDescription="";
                        //获取备注
                        Collection<ChangeActivityIfc> changeActivities = ChangeUtils.getChangeActivities(ecn);
                        for (ChangeActivityIfc changeActivityIfc : changeActivities) {
                            AffectedActivityData affectedActivityData = ChangeUtils.getAffectedActivity(changeActivityIfc,  parts.get(i));
                            if (affectedActivityData != null) {
                                changeDetailedDescription=affectedActivityData.getDescription()==null?"":affectedActivityData.getDescription();
                            }
                        }
                        AffectedParentPartsBean affectedInfo=new AffectedParentPartsBean();
                        WTPart part=parts.get(i);
                        String number=part.getNumber();
                        String name=part.getName();
                        String version=part.getVersionInfo().getIdentifier().getValue() + "." + part.getIterationInfo().getIdentifier().getValue();
                        String state=part.getLifeCycleState().getDisplay(SessionHelper.getLocale());
                        // 在制数量
                        String inProcessQuantities = "";
                        Object inProcessQuantitiesObj=getIBAObjectValue(part,"ArticleInventory");
                        if (inProcessQuantitiesObj!=null){
                            inProcessQuantities= (String) inProcessQuantitiesObj;
                        }
                        // 在制处理措施
                        String processingMeasures = "";
                        Object processingMeasuresObj =getIBAObjectValue(part,"ArticleDispose");
                        if (processingMeasuresObj!=null){
                            processingMeasures= getMeasure(String.valueOf(processingMeasuresObj));
                        }
                        // 在途数量
                        String onthewayQuantity = "";
                        Object onthewayQuantityObj =getIBAObjectValue(part,"PassageInventory");
                        if (onthewayQuantityObj!=null){
                            onthewayQuantity=(String)onthewayQuantityObj;
                        }
                        // 在途处理措施
                        String onthewayTreatmentMeasure = "";
                        Object onthewayTreatmentMeasureObj =getIBAObjectValue(part,"PassageDispose");
                        if (onthewayTreatmentMeasureObj!=null){
                            onthewayTreatmentMeasure=getMeasure(String.valueOf(onthewayTreatmentMeasureObj));
                        }
                        // 库存数量
                        String stockQuantity = "";
                        Object stockQuantityObj =getIBAObjectValue(part,"CentralWarehouseInventory");
                        if (stockQuantityObj!=null){
                            stockQuantity=(String)stockQuantityObj;
                        }
                        // 库存处理措施
                        String stockTreatmentMeasure = "";
                        Object stockTreatmentMeasureObj =getIBAObjectValue(part,"InventoryDispose");
                        if (stockTreatmentMeasureObj!=null){
                            stockTreatmentMeasure=getMeasure(String.valueOf(stockTreatmentMeasureObj));
                        }
                        // 已出货成品处理措施
                        String finishedHandleMeasures = "";
                        Object finishedHandleMeasuresObj =getIBAObjectValue(part,"ProductDispose");
                        if (finishedHandleMeasuresObj!=null){
                            finishedHandleMeasures=getMeasure(String.valueOf(finishedHandleMeasuresObj));
                        }
                        //变更类型
                        String changeType="";
                        Object changeTypeObj =getIBAObjectValue(part,"ChangeType");
                        if (changeTypeObj!=null){
                            changeType=getMeasure(String.valueOf(changeTypeObj));
                        }
                        //完成时间
                        String expectDate="";
                        Object expectDateObj =getIBAObjectValue(part,"CompletionTime");
                        if (expectDateObj!=null){
                            expectDate=(String)expectDateObj;
                        }

                        affectedInfo.setEffectObjectNumber(number);
                        affectedInfo.setEffectObjectName(name);
                        affectedInfo.setEffectObjectVersion(version);
                        affectedInfo.setEffectObjectState(state);
                        affectedInfo.setChangeDetailedDescription(changeDetailedDescription);
                        affectedInfo.setInProcessQuantities(inProcessQuantities);
                        affectedInfo.setProcessingMeasures(processingMeasures);
                        affectedInfo.setOnthewayQuantity(onthewayQuantity);
                        affectedInfo.setOnthewayTreatmentMeasure(onthewayTreatmentMeasure);
                        affectedInfo.setStockQuantity(stockQuantity);
                        affectedInfo.setStockTreatmentMeasure(stockTreatmentMeasure);
                        affectedInfo.setFinishedHandleMeasures(finishedHandleMeasures);
                        affectedInfo.setChangeType(changeType);
                        affectedInfo.setExpectDate(expectDate);

                        resultList.add(affectedInfo);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SessionServerHelper.manager.setAccessEnforced(flag);// 添加权限
        }

        return resultList;
    }


    //获取ECN中所有的受影响对象
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

    //获取ECN中所有的产生对象
    public static List<WTPart> getProduceParts(WTChangeOrder2 ecn) throws WTException {
        List<WTPart> parts=new ArrayList<>();
        QueryResult ecaqr = ChangeHelper2.service.getChangeActivities(ecn);

        while (ecaqr.hasMoreElements()) {
            Object ecaobject = ecaqr.nextElement();
            if (ecaobject instanceof WTChangeActivity2) {
                WTChangeActivity2 eca = ((WTChangeActivity2) ecaobject);
                // 查询ECA中所有受影响对象
                QueryResult qr = ChangeHelper2.service.getChangeablesAfter(eca);
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

    //获取IBA属性
    public static String getIBAvalue(Persistable p, String key) throws PIException {
        Object object = PIAttributeHelper.service.getValue(p, key);
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

    //通过cea获取ecn
    public static WTChangeOrder2 getECNbyECA(WTChangeActivity2 wtChangeActivity2) throws WTException {
        WTChangeOrder2 ecn=new WTChangeOrder2();
        QueryResult ecaqr=ChangeHelper2.service.getChangeOrder(wtChangeActivity2);
        while (ecaqr.hasMoreElements()) {
            Object object = ecaqr.nextElement() ;
            if(object instanceof WTChangeOrder2){
                ecn= (WTChangeOrder2) object;
            }
        }

        return  ecn;
    }

    //获取ecn所有受影响对象和产生对象的BOM差异<物料编码，...>
    public static Map<String,List<BOMChangeInfoBean>> getBomChangeInfos(WTChangeOrder2 ecn){
        Map<String,List<BOMChangeInfoBean>> bomChangeInfos=new HashMap<>();
        boolean flag = SessionServerHelper.manager.setAccessEnforced(false);// 忽略权限
        try {
            List<WTPart> affectedParts=getAffectedParts(ecn);//所有受影响对象
            List<WTPart> produceParts=getProduceParts(ecn);//所有产生对象
            System.out.println("affectedParts=="+affectedParts+"==produceParts=="+produceParts);
            if (affectedParts!=null&&affectedParts.size()>0&&produceParts!=null&&affectedParts.size()>0){
                for (int i = 0; i < affectedParts.size(); i++) {
                    for (int j = 0; j < produceParts.size(); j++) {
                        WTPart affectedPart=affectedParts.get(i);
                        WTPart producePart=produceParts.get(j);
                        String affectedNumber=affectedPart.getNumber();
                        String produceNumber=producePart.getNumber();
                        //同一物料名称，比较BOM差异
                        if (affectedNumber.equals(produceNumber)){
                            List<BOMChangeInfoBean> bomChangeInfo=CompareBom.getBomChangeInfo(affectedPart,producePart);
                            if (bomChangeInfo!=null){
                                bomChangeInfos.put(affectedNumber,bomChangeInfo);
                            }
                        }

                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SessionServerHelper.manager.setAccessEnforced(flag);// 添加权限
        }

        return bomChangeInfos;
    }


    /**
     * 获取ECA的受影响对象
     * @param activity2
     * @return
     * @throws WTException
     */
    public static Collection<Changeable2> getChangeablesBefore(WTChangeActivity2 activity2) throws WTException {
        Collection<Changeable2> changeable2s = new HashSet<>();
        // 获取ECA中所有受影响对象
        QueryResult result = ChangeHelper2.service.getChangeablesBefore(activity2);
        while (result.hasMoreElements()) {
            Object object = result.nextElement();
            if (object instanceof ObjectReference) {
                object = ((ObjectReference) object).getObject();
            }
            if (object instanceof Changeable2) {
                changeable2s.add((Changeable2) object);
            }
        }
        return changeable2s;
    }
    //增加时间（小时）
    public static String addDate(String day, int hour){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(day);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (date == null)
            return "";
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR, hour);// 24小时制
        date = cal.getTime();
        cal = null;
        return format.format(date);
    }
    //获取处理措施选项值
    public static String getMeasure(String str){
        String result="";
        String[] arr = str.split(";");
        if (arr.length > 1){
            result = arr[1];
        } else{
            result = arr[0];
        }
        return  result;
    }

}
