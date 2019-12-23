package ext.appo.change.report;

import ext.appo.change.beans.*;
import ext.appo.change.constants.BomChangeConstants;
import ext.appo.ecn.common.util.ChangeUtils;
import wt.change2.*;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;
import wt.vc.views.View;
import wt.vc.views.ViewHelper;

import java.util.*;

//变更履历
public class ChangeHistoryReport {

    //返回物料升版各阶段信息（有制造视图和设计视图判断如果制造视图版本升版过则显示制造视图升版,否则显示设计视图升版）,没变更则为修订
    //降序
    public static List<PartUpdateTypeBean> getPartUpdateType(WTPart part) throws WTException {
        List<PartUpdateTypeBean> partUpdateTypeBeans=new ArrayList<>();
        String view=part.getViewName();
        Boolean flag=isOneView(part);
        if (!flag){
            //存在多视图
            List<WTPart> mParts=getAllPartsByViewName(part,"Manufacturing");//降序排序
            //制造视图存在多版本,取制造视图升版信息,不显示设计视图信息
            if (mParts!=null&&mParts.size()>1){
                List<PartUpdateTypeBean> partUpdateTypeBeanList=getPartUpdateTypeBeans(mParts);
                partUpdateTypeBeans.addAll(partUpdateTypeBeanList);
            }else{
                //制造视图没变更升版，取设计视图
                List<WTPart> dParts=getAllPartsByViewName(part,"Design");//降序排序
                List<PartUpdateTypeBean> partUpdateTypeBeanList=getPartUpdateTypeBeans(dParts);
                partUpdateTypeBeans.addAll(partUpdateTypeBeanList);
            }

        }else{
            //只有一个视图
            List<WTPart> parts=getAllPartsByViewName(part,view);//降序排序
            if (parts!=null&&parts.size()>1){
                List<PartUpdateTypeBean> partUpdateTypeBeanList=getPartUpdateTypeBeans(parts);
                partUpdateTypeBeans.addAll(partUpdateTypeBeanList);
            }

        }

        return partUpdateTypeBeans;
    }


    //获取ECN,受影响物料的合并信息<ENC编码,PartUpdateInfoBean>
    public static Map<String, PartUpdateInfoBean> getUpdateInfo(Set<WTChangeOrder2> wtChangeOrder2s, String partNumber) throws WTException {
        Map<String,PartUpdateInfoBean> updateInfos=new HashMap<>();
        if (wtChangeOrder2s!=null&&wtChangeOrder2s.size()>0) {
            for (WTChangeOrder2 ecn : wtChangeOrder2s) {
                PartUpdateInfoBean partUpdateInfo=new PartUpdateInfoBean();
                ECNInfoBean ecnInfo=BomChangeReport.getECNInfo(ecn);
                AffectedParentPartsBean affectedInfo=getAffectedInfoByNumber(ecn,partNumber);
                WTPart affectedPart=getAffectedPartByNumber(ecn,partNumber);//受影响对象
                WTPart producePart=getProducePartByNumber(ecn,partNumber);//产生对象
                Map<String,String> changeVersion=new HashMap<>();
                if (affectedPart!=null&&producePart!=null){
                    changeVersion.put("before",affectedPart.getVersionIdentifier().getValue());
                    changeVersion.put("after",producePart.getVersionIdentifier().getValue());
                }
                if (ecnInfo!=null&&affectedInfo!=null){

                    partUpdateInfo.setEcnNumber(ecnInfo.getEcnNumber());
                    partUpdateInfo.setEcnCreator(ecnInfo.getEcnCreator());
                    partUpdateInfo.setEcnStartTime(ecnInfo.getEcnStartTime());
                    partUpdateInfo.setProductType(ecnInfo.getProductType());
                    partUpdateInfo.setProjectName(ecnInfo.getProjectName());
                    partUpdateInfo.setChangeType(ecnInfo.getChangeType());
                    partUpdateInfo.setChangeReason(ecnInfo.getChangeReason());
                    partUpdateInfo.setChangePhase(ecnInfo.getChangePhase());
                    partUpdateInfo.setIsChangeDrawing(ecnInfo.getIsChangeDrawing());
                    partUpdateInfo.setChangeDescription(ecnInfo.getChangeDescription());

                    partUpdateInfo.setEffectObjectNumber(affectedInfo.getEffectObjectNumber());
                    partUpdateInfo.setEffectObjectName(affectedInfo.getEffectObjectName());
                    partUpdateInfo.setEffectObjectVersion(affectedInfo.getEffectObjectVersion());
                    partUpdateInfo.setEffectObjectState(affectedInfo.getEffectObjectState());
                    partUpdateInfo.setChangeDetailedDescription(affectedInfo.getChangeDetailedDescription());
                    partUpdateInfo.setInProcessQuantities(affectedInfo.getInProcessQuantities());
                    partUpdateInfo.setProcessingMeasures(affectedInfo.getProcessingMeasures());
                    partUpdateInfo.setOnthewayQuantity(affectedInfo.getOnthewayQuantity());
                    partUpdateInfo.setOnthewayTreatmentMeasure(affectedInfo.getOnthewayTreatmentMeasure());
                    partUpdateInfo.setStockQuantity(affectedInfo.getStockQuantity());
                    partUpdateInfo.setStockTreatmentMeasure(affectedInfo.getStockTreatmentMeasure());
                    partUpdateInfo.setFinishedHandleMeasures(affectedInfo.getFinishedHandleMeasures());
                    partUpdateInfo.setEffectObjectChangeType(affectedInfo.getChangeType());
                    partUpdateInfo.setExpectDate(affectedInfo.getExpectDate());
                }
                updateInfos.put(ecn.getNumber(),partUpdateInfo);
            }
        }

        return  updateInfos;
    }

    //物料受影响对象和产生对象的BOM差异
    public static List<BOMChangeInfoBean> getBomChangeInfoByPart(WTPart beforePart,WTPart afterPart) throws Exception {
        List<BOMChangeInfoBean> bomChangeInfos=new ArrayList<>();
        if (beforePart!=null&&afterPart!=null){
            bomChangeInfos=CompareBom.getBomChangeInfo(beforePart,afterPart);
        }

        return bomChangeInfos;
    }


    //查询物料所有ECN
    public static Set<WTChangeOrder2>  getAllECN(WTPart part) throws WTException {
        Set<WTChangeOrder2> wtChangeOrder2s = new HashSet<>();
        QueryResult queryResult = VersionControlHelper.service.allVersionsOf(part.getMaster());
        while (queryResult.hasMoreElements()) {
            Object object = queryResult.nextElement();
            QueryResult result = ChangeHelper2.service.getAffectingChangeActivities((Changeable2) object);
            while (result.hasMoreElements()) {
                WTChangeActivity2 changeActivity2 = (WTChangeActivity2) result.nextElement();
                WTChangeOrder2 changeOrder2 = ChangeUtils.getEcnByEca(changeActivity2);
                wtChangeOrder2s.add(changeOrder2);
            }
        }
        return wtChangeOrder2s;
    }

    //通过物料编码,ECN查询ECN中受影响物料
    public static WTPart getAffectedPartByNumber(WTChangeOrder2 ecn,String number) throws WTException {
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
                        if (number.equals(part.getNumber())){
                            return  part;
                        }
                    }
                }
            }
        }
        return null;
    }

    //通过物料编码,ECN查询ECN中产生的物料
    public static WTPart getProducePartByNumber(WTChangeOrder2 ecn,String number) throws WTException {
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
                        if (number.equals(part.getNumber())){
                            return  part;
                        }
                    }
                }
            }
        }
        return null;
    }

    //通过物料编码获取受影响对象信息
    public static AffectedParentPartsBean getAffectedInfoByNumber(WTChangeOrder2 ecn,String number){
        boolean flag = SessionServerHelper.manager.setAccessEnforced(false);// 忽略权限
        AffectedParentPartsBean affectedInfo=new AffectedParentPartsBean();
        try {
            if (ecn!=null){
                WTPart part=getAffectedPartByNumber(ecn,number);
                if (part!=null){
                    String changeDetailedDescription="";
                    //获取备注
                    Collection<ChangeActivityIfc> changeActivities = ChangeUtils.getChangeActivities(ecn);
                    for (ChangeActivityIfc changeActivityIfc : changeActivities) {
                        AffectedActivityData affectedActivityData = ChangeUtils.getAffectedActivity(changeActivityIfc,  part);
                        if (affectedActivityData != null) {
                            changeDetailedDescription=affectedActivityData.getDescription()==null?"":affectedActivityData.getDescription();
                        }
                    }

                    String name=part.getName();
                    String version=part.getVersionInfo().getIdentifier().getValue() + "." + part.getIterationInfo().getIdentifier().getValue();
                    String state=part.getLifeCycleState().getDisplay(SessionHelper.getLocale());
                    // 在制数量
                    String inProcessQuantities = "";
                    Object inProcessQuantitiesObj=BomChangeReport.getIBAObjectValue(part,"ArticleInventory");
                    if (inProcessQuantitiesObj!=null){
                        inProcessQuantities= (String) inProcessQuantitiesObj;
                    }
                    // 在制处理措施
                    String processingMeasures = "";
                    Object processingMeasuresObj =BomChangeReport.getIBAObjectValue(part,"ArticleDispose");
                    if (processingMeasuresObj!=null){
                        processingMeasures= BomChangeReport.getMeasure(String.valueOf(processingMeasuresObj));
                    }
                    // 在途数量
                    String onthewayQuantity = "";
                    Object onthewayQuantityObj =BomChangeReport.getIBAObjectValue(part,"PassageInventory");
                    if (onthewayQuantityObj!=null){
                        onthewayQuantity=(String)onthewayQuantityObj;
                    }
                    // 在途处理措施
                    String onthewayTreatmentMeasure = "";
                    Object onthewayTreatmentMeasureObj =BomChangeReport.getIBAObjectValue(part,"PassageDispose");
                    if (onthewayTreatmentMeasureObj!=null){
                        onthewayTreatmentMeasure=BomChangeReport.getMeasure(String.valueOf(onthewayTreatmentMeasureObj));
                    }
                    // 库存数量
                    String stockQuantity = "";
                    Object stockQuantityObj =BomChangeReport.getIBAObjectValue(part,"CentralWarehouseInventory");
                    if (stockQuantityObj!=null){
                        stockQuantity=(String)stockQuantityObj;
                    }
                    // 库存处理措施
                    String stockTreatmentMeasure = "";
                    Object stockTreatmentMeasureObj =BomChangeReport.getIBAObjectValue(part,"InventoryDispose");
                    if (stockTreatmentMeasureObj!=null){
                        stockTreatmentMeasure=BomChangeReport.getMeasure(String.valueOf(stockTreatmentMeasureObj));
                    }
                    // 已出货成品处理措施
                    String finishedHandleMeasures = "";
                    Object finishedHandleMeasuresObj =BomChangeReport.getIBAObjectValue(part,"ProductDispose");
                    if (finishedHandleMeasuresObj!=null){
                        finishedHandleMeasures=BomChangeReport.getMeasure(String.valueOf(finishedHandleMeasuresObj));
                    }
                    //变更类型
                    String changeType="";
                    Object changeTypeObj =BomChangeReport.getIBAObjectValue(part,"ChangeType");
                    if (changeTypeObj!=null){
                        changeType=BomChangeReport.getMeasure(String.valueOf(changeTypeObj));
                    }
                    //完成时间
                    String expectDate="";
                    Object expectDateObj =BomChangeReport.getIBAObjectValue(part,"CompletionTime");
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

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SessionServerHelper.manager.setAccessEnforced(flag);// 添加权限
        }

        return  affectedInfo;
    }



    /*
     * 通过number获取某视图的所有最新大版本物料(按降序排序)
     */
    public static List<WTPart> getAllPartsByViewName(WTPart part,String viewName) throws WTException {
        List<WTPart> parts=new ArrayList<>();
        QueryResult queryResult = VersionControlHelper.service.allVersionsOf(part.getMaster());//获取所有大版本的最新小版本
        while (queryResult.hasMoreElements()) {
            WTPart wtpart = (WTPart) queryResult.nextElement();
            String view=wtpart.getViewName();
            if (view.equals(viewName)){
                parts.add(wtpart);
            }
        }
        return parts;
    }

    //是否只有一个视图
    public static Boolean isOneView(WTPart part) throws WTException {
        Boolean flag=true;
        Set viewName=new HashSet();
        QueryResult queryResult = VersionControlHelper.service.allVersionsOf(part.getMaster());//获取所有大版本的最新小版本
        while (queryResult.hasMoreElements()) {
            WTPart wtpart = (WTPart) queryResult.nextElement();
            String view=wtpart.getViewName();
            viewName.add(view);
        }
        //存在多视图
        if (viewName!=null&&viewName.size()>1){
            flag=false;
        }
        return flag;
    }

    /*
     * 通过number获取某视图的所有版本物料（包括小版本）
     */
    public static List<WTPart> getAllParts(String viewName, String number) throws WTException {
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
        QueryResult qr = PersistenceHelper.manager.find(qs);
        if (qr != null && qr.hasMoreElements())
            return  qr.getObjectVectorIfc().getVector();

        return new Vector();
    }
    /*
     * 通过number获取某视图的最新的物料
     */
    public static Vector getAllLatestWTParts(String viewName, String number) throws WTException {
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

    //比较同一物料每个大版本之间的变更类型
    public static List<PartUpdateTypeBean> getPartUpdateTypeBeans(List<WTPart> parts) throws WTException {
        List<PartUpdateTypeBean> partUpdateTypeBeans=new ArrayList<>();

        //最新大版本没有升版变更,过滤
        for (int i = 1; i <parts.size() ; i++) {//降序
            WTPart part=parts.get(i);
            String partNumber=part.getNumber();
            String viewName=part.getViewName();
            String beforeVersion = part.getVersionIdentifier().getValue();// 变更前物料大版本
            String afterVersion = parts.get(i-1).getVersionIdentifier().getValue();// 变更后物料大版本
            Map<String,String> changeVersion=new HashMap<>();
            changeVersion.put("before",beforeVersion);
            changeVersion.put("after",afterVersion);

            PartUpdateTypeBean partUpdateTypeBean=new PartUpdateTypeBean();
            //查询版本在ECN中是否存在升版，存在ECN升版，不存在则为修订升版
            Map<WTPart,WTChangeOrder2> wtPartWTChangeOrder2Map=getAllECNAffectedParts(part);
            Boolean isEcn=false;//默认没有ECN,修订升版
            for (WTPart wtPart:wtPartWTChangeOrder2Map.keySet()){
                String version=wtPart.getVersionIdentifier().getValue();//物料大版本
                String view=wtPart.getViewName();
                if (beforeVersion.equals(version)&&viewName.equals(view)){
                    //该大版本存在升版ECN，ECN变更
                    WTChangeOrder2 ecn=wtPartWTChangeOrder2Map.get(wtPart);

                    partUpdateTypeBean.setUpdateType(BomChangeConstants.TYPE_10);
                    partUpdateTypeBean.setChangeVersion(changeVersion);
                    partUpdateTypeBean.setPartNumber(wtPart.getNumber());
                    partUpdateTypeBean.setView(wtPart.getViewName());
                    partUpdateTypeBean.setEcn(ecn);

                    partUpdateTypeBeans.add(partUpdateTypeBean);
                    isEcn=true;
                    break;
                }
            }
            if (!isEcn){
                //修订升版
                partUpdateTypeBean.setUpdateType(BomChangeConstants.TYPE_11);
                partUpdateTypeBean.setChangeVersion(changeVersion);
                partUpdateTypeBean.setPartNumber(partNumber);
                partUpdateTypeBean.setView(part.getViewName());

                partUpdateTypeBeans.add(partUpdateTypeBean);
            }

        }
        return partUpdateTypeBeans;
    }

    //通过物料编码,视图,大版本获取当前大版本最新小版本物料
    public static WTPart getLatestPart(String partNumber,String viewName,String mVersion) throws WTException {
        WTPart part=new WTPart();
        Vector partVecor=getAllLatestWTParts(viewName,partNumber);//获取最新物料
        if (partVecor!=null&&partVecor.size()>0){
            QueryResult queryResult = VersionControlHelper.service.allVersionsOf(((WTPart)partVecor.get(0)).getMaster());//获取所有大版本的最新小版本
            while (queryResult.hasMoreElements()) {
                WTPart wtpart = (WTPart) queryResult.nextElement();
                String view=wtpart.getViewName();//视图
                String version=wtpart.getVersionIdentifier().getValue();//大版本
                if(viewName.equals(view)&&mVersion.equals(version)){
                    //同一视图，同一版本
                    part=wtpart;
                    break;
                }
            }
        }
        return part;
    }

    //查询物料所有升版ECN（存在受影响对象和产生对象）,对应受影响物料(不同版本)<受影响部件,ECN>
    public static Map<WTPart,WTChangeOrder2>  getAllECNAffectedParts(WTPart part) throws WTException {
        Map<WTPart,WTChangeOrder2> allECNAffectedParts=new HashMap<>();
        Set<WTChangeOrder2> wtChangeOrder2s=getAllECN(part);
        for (WTChangeOrder2 ecn : wtChangeOrder2s){
            WTPart affectedPart=getAffectedPartByNumber(ecn,part.getNumber());//受影响对象
            WTPart producePart=getProducePartByNumber(ecn,part.getNumber());//产生对象
            //ECN中该物料受影响的对象和产生的对象
            if (affectedPart!=null&&producePart!=null){
                allECNAffectedParts.put(affectedPart,ecn);
            }
        }

        return allECNAffectedParts;
    }

}
