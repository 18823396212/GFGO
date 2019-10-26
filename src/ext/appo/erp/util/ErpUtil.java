package ext.appo.erp.util;

import ext.appo.ecn.pdf.PdfUtil;
import ext.appo.erp.bean.BomCompareBean;
import ext.appo.erp.bean.FTreeEntity;
import ext.com.workflow.WorkflowUtil;
import ext.customer.common.MBAUtil;
import ext.generic.reviewObject.model.ProcessReviewObjectLink;
import ext.generic.reviewObject.model.ProcessReviewObjectLinkHelper;
import org.apache.commons.lang.StringUtils;
import wt.change2.*;
import wt.fc.*;
import wt.fc.collections.WTArrayList;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.part.WTPartSubstituteLink;
import wt.part.WTPartUsageLink;
import wt.pds.StatementSpec;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.workflow.engine.WfProcess;

import java.util.*;

import static ext.appo.erp.util.BomUtil.Intercept;
import static ext.appo.erp.util.BomUtil.getSubstituteLinks;
import static ext.appo.erp.util.BomUtil.getUsageLink;
import static ext.appo.erp.util.KingDeeK3Helper.viewBOMFBomEntryId;

public class ErpUtil {


    // 获取变更前对象
    public static Map<String, WTPart> getBeforeDataWithPart(WTChangeOrder2 eco) throws WTException {
        Map<String, WTPart> allMp = new HashMap<String, WTPart>();
        try {
            if (eco == null) return allMp;
            QueryResult qs = ChangeHelper2.service.getChangeActivities(eco);
            while (qs.hasMoreElements()) {
                WTChangeActivity2 activity = (WTChangeActivity2) qs.nextElement();
                QueryResult qsafter = ChangeHelper2.service.getChangeablesBefore(activity);//变更前对象
                while (qsafter.hasMoreElements()) {
                    Object obj = qsafter.nextElement();
                    if (obj instanceof WTPart) {
                        WTPart part = (WTPart) obj;
                        allMp.put(part.getNumber(), part);
                    }
                }
            }
        } catch (ChangeException2 e) {
            e.printStackTrace();
            throw new WTException(e);
        }
        return allMp;
    }

    // 获取变更后对象
    public static Map<String, WTPart> getAfterDataWithPart(WTChangeOrder2 eco) throws WTException {
        Map<String, WTPart> allMp = new HashMap<String, WTPart>();
        try {
            if (eco == null) return allMp;
            QueryResult qs = ChangeHelper2.service.getChangeActivities(eco);
            while (qs.hasMoreElements()) {
                WTChangeActivity2 activity = (WTChangeActivity2) qs.nextElement();
                QueryResult qsafter = ChangeHelper2.service.getChangeablesAfter(activity);
                while (qsafter.hasMoreElements()) {
                    Object obj = qsafter.nextElement();
                    if (obj instanceof WTPart) {
                        WTPart part = (WTPart) obj;
                        allMp.put(part.getNumber(), part);
                    }
                }
            }

        } catch (ChangeException2 e) {
            e.printStackTrace();
            throw new WTException(e);
        }
        return allMp;
    }


    public static WTPart getLatestPart(String partNum) {
        try {
            if (partNum == null)
                return null;
            WTPartMaster partMaster = BomUtil.getWTPartMaster(partNum);
            if (partMaster != null) {
                QueryResult qrVersions = VersionControlHelper.service.allVersionsOf(partMaster);
                if (qrVersions.hasMoreElements()) {
                    WTPart part = (WTPart) qrVersions.nextElement();
                    return part;
                }
            }
        } catch (QueryException e) {
            e.printStackTrace();
        } catch (WTException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static boolean compare(String oldV, String newV) {
        if (StringUtils.isEmpty(oldV) && StringUtils.isEmpty(newV)) {
            return true;
        } else if (StringUtils.isNotEmpty(oldV) && StringUtils.isEmpty(newV)) {
            return false;
        } else if (StringUtils.isEmpty(oldV) && StringUtils.isNotEmpty(newV)) {
            return false;
        } else {
            return oldV.equals(newV);
        }
    }


    /*
    判断ECN单中所有变更前对象选择的库存处理措施是否一致
     */
    public static Boolean isKcclcsEqual(Map<String,WTPart> map){

        System.out.println("===库存处理措施是否一致===");

        Set<String> set = new HashSet<>();

        for (String key:map.keySet()){
            WTPart part = map.get(key);
            String kcclcs = PartUtil.getKcclcs(part);
            set.add(kcclcs);

            System.out.println("库存处理措施======="+kcclcs);
        }

        if (set.size()==1){
            return true;
        }else if (set.size()==2){

            if (set.contains("报废")&&(set.contains("返工")||set.contains("用完为止"))){
                return false;
            }
        }

        return false;
    }


    /****
     * 判断物料是否有关联的ECN
      * @param part
     * @return
     * @throws WTException
     */
    public static Boolean isHaveECN(WTPart part) throws WTException {

        System.out.println("===是否有ECN====");

        List<WTChangeActivity2> ecas = getEcaWithObject(part);

        System.out.println("=========ecas==========="+ecas);
        for (int i = 0; i <ecas.size() ; i++) {
            System.out.println("ecas=="+i+"==name=="+ecas.get(i).getName()+"==状态=="+ecas.get(i).getState());
        }
        if (ecas!=null&&ecas.size()>0){
            return true;
        }

        return false;

    }

    /****
     * 判断物料是否在eca中
     * @param part
     * @return
     * @throws WTException
     */
    public static Boolean isHaveECA(WTPart part) throws WTException {

        System.out.println("===是否有ECA====");

        List<WTChangeActivity2> ecas = getEcaWithObject(part);

        System.out.println("=========ecas==========="+ecas);
        for (int i = 0; i <ecas.size() ; i++) {
            System.out.println("ecas=="+i+"==name=="+ecas.get(i).getName()+"==状态=="+ecas.get(i).getState());
        }
        if (ecas!=null&&ecas.size()>0){
            for (int i = 0; i <ecas.size() ; i++) {
                if ("IMPLEMENTATION".equals(ecas.get(i).getState().toString())){
                    //实施状态
                    return true;
                }
            }

        }
        return false;
    }


    public static List<WTChangeActivity2> getEcaWithObject(Persistable per) throws WTException {
        List<WTChangeActivity2> ecas = new ArrayList<WTChangeActivity2>();

        QuerySpec qs = new QuerySpec(ChangeRecord2.class);
        SearchCondition sc = new SearchCondition(ChangeRecord2.class, "roleBObjectRef.key", SearchCondition.EQUAL, per.getPersistInfo().getObjectIdentifier());
        qs.appendWhere(sc, new int[] { 0 });
        QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
        while (qr.hasMoreElements()) {
            ChangeRecord2 cr2 = (ChangeRecord2) qr.nextElement();
            WTChangeActivity2 eca = (WTChangeActivity2) cr2.getChangeActivity2();
            ecas.add(eca);
        }

        return ecas;
    }


    /*****
     * 变更前和变更后BOM差异对比（增删改）
     * @param part 变更后物料
     * @param pre 变更前物料
     * @return
     * @throws WTException
     */
    public static List<BomCompareBean> buildCompareBean(WTPart part, WTPart pre) throws Exception {

        System.out.println("=================差异对比==================");
        List<BomCompareBean> lists = new ArrayList<>();
        if(pre == null){
            return lists;
        }

        String majorVersion = part.getVersionIdentifier().getValue();//大版本

        String bomVersion = part.getNumber()+"_"+majorVersion;//BOM版本
        try {
            //当前物料BOM
            Map<WTPartMaster, WTPartUsageLink> current = BomUtil.getAllWTPartUsageLink(part);
            //变更前物料BOM
            Map<WTPartMaster, WTPartUsageLink> previous = BomUtil.getAllWTPartUsageLink(pre);

            for (WTPartMaster wm : current.keySet()) {
                //add
                if (!previous.keySet().contains(wm)) {
                    WTPartUsageLink link = current.get(wm);
                    WTPartUsageLink oldlink = previous.get(wm);
                    WTPart child = getLatestPart(wm.getNumber());
                    BomCompareBean bcb = new BomCompareBean();
                    bcb.setCompareFlag("A");
                    bcb.setChildNumber(wm.getNumber());
                    bcb.setBomVersion(bomVersion);
                    bcb.setChildName(wm.getName());
                    bcb.setChildVersion(BomUtil.getVersion(child));
                    bcb.setOldVersion(BomUtil.getVersion(pre));
                    bcb.setNewVersion(BomUtil.getVersion(part));
                    bcb.setParentNumber(part.getNumber());
                    bcb.setParentName(part.getName());

                    String oldLinkSum=BomUtil.getLinkSum(oldlink);
                    String newLinkSum=BomUtil.getLinkSum(link);
                    String oldSum=Intercept(oldLinkSum,6);//数量截取小数点后6位
                    String newSum=Intercept(newLinkSum,6);//数量截取小数点后6位
                    bcb.setOldSum(oldSum);
                    bcb.setNewSum(newSum);
//                    bcb.setOldSum(BomUtil.getLinkSum(oldlink));
//                    bcb.setNewSum(BomUtil.getLinkSum(link));
                    bcb.setWeihao(BomUtil.getPartReferenceDesignators(link));
                    String kcclcs = PartUtil.getKcclcs(pre);

                    lists.add(bcb);

                } else {

                    //update
                    WTPartUsageLink oldlink = previous.get(wm);
                    WTPartUsageLink newLink = current.get(wm);
                    WTPart child = BomUtil.getLatestPart(wm.getNumber());
                    BomCompareBean bcb = new BomCompareBean();
                    bcb.setCompareFlag("C");
                    bcb.setChildNumber(wm.getNumber());
                    bcb.setBomVersion(bomVersion);
                    bcb.setChildName(wm.getName());
                    bcb.setChildVersion(BomUtil.getVersion(child));
                    bcb.setOldVersion(BomUtil.getVersion(pre));
                    bcb.setNewVersion(BomUtil.getVersion(part));
                    bcb.setParentNumber(part.getNumber());
                    bcb.setParentName(part.getName());
                    String oldLinkSum=BomUtil.getLinkSum(oldlink);
                    String newLinkSum=BomUtil.getLinkSum(newLink);

                    String oldSum=Intercept(oldLinkSum,6);//数量截取小数点后6位
                    String newSum=Intercept(newLinkSum,6);//数量截取小数点后6位
                    bcb.setOldSum(oldSum);
                    bcb.setNewSum(newSum);
//                    bcb.setOldSum(BomUtil.getLinkSum(oldlink));
//                    bcb.setNewSum(BomUtil.getLinkSum(newLink));
                    bcb.setWeihao(BomUtil.compareWH(oldlink, newLink));
                    String kcclcs = PartUtil.getKcclcs(pre);

                    String oldwh = BomUtil.getPartReferenceDesignators(oldlink);
                    String newwh = BomUtil.getPartReferenceDesignators(newLink);
                    bcb.setOldWh(oldwh);
                    bcb.setNewWh(newwh);

//                    //
//                    List<List<Object>> queryFENTRYIDList=queryFENTRYID(parentBOM,childrenNumber,FUSEORGID);
//                    String FBomEntryIdStr="";
//                    //查询父件下BOM子项内码TreeEntity的Id
//                    String result=viewBOMFBomEntryId(bomVersion);
//                    if (!result.equals("0")){
//                        FBomEntryIdStr=result;
//                    }
//                    System.out.println("FBomEntryIdStr==="+FBomEntryIdStr);
//                    bcb.setFBomEntryId(FBomEntryIdStr);

                    if (!compare(bcb.getOldSum(), bcb.getNewSum()) || !compare(newwh, oldwh)) {
                        lists.add(bcb);
                    }

                }
            }
            for (WTPartMaster wm : previous.keySet()) {
                //del
                if (!current.keySet().contains(wm)) {
                    WTPartUsageLink link = previous.get(wm);
                    WTPart child = BomUtil.getLatestPart(wm.getNumber());
                    BomCompareBean bcb = new BomCompareBean();
                    bcb.setCompareFlag("D");
                    bcb.setBomVersion(bomVersion);
                    bcb.setChildNumber(wm.getNumber());
                    bcb.setChildName(wm.getName());
                    bcb.setChildVersion(BomUtil.getVersion(child));
                    bcb.setParentNumber(part.getNumber());
                    bcb.setParentName(part.getName());
                    bcb.setOldVersion(BomUtil.getVersion(pre));
                    bcb.setNewVersion(BomUtil.getVersion(part));
                    String oldLinkSum=BomUtil.getLinkSum(link);
                    String oldSum=Intercept(oldLinkSum,6);//数量截取小数点后6位
                    bcb.setOldSum(oldSum);
//                    bcb.setOldSum(BomUtil.getLinkSum(link));
                    bcb.setNewSum("");
                    bcb.setWeihao(BomUtil.compareWH(link, null));

                    lists.add(bcb);

                }
            }

        } catch (Exception e) {
            throw new Exception(e);
        }


        System.out.println("=====================差异结果："+lists);
        return lists;
    }


    /*****
     * 变更前和变更后BOM差异对比（加替代料）
     * @param part 变更后物料
     * @param pre 变更前物料
     * @return
     * @throws WTException
     */
    public static List<BomCompareBean> buildExhaustMaterialsCompareBean(WTPart part, WTPart pre) throws Exception {

        System.out.println("=================用完旧料差异对比==================");
        List<BomCompareBean> lists = new ArrayList<>();
        if(pre == null){
            return lists;
        }
//        System.out.println("用完旧料==");
        String majorVersion = part.getVersionIdentifier().getValue();//大版本

        String bomVersion = part.getNumber()+"_"+majorVersion;//BOM版本
        try {
            //当前物料BOM
            Map<WTPartMaster, WTPartUsageLink> current = BomUtil.getAllWTPartUsageLink(part);
            //变更前物料BOM
            Map<WTPartMaster, WTPartUsageLink> previous = BomUtil.getAllWTPartUsageLink(pre);

            //循环当前物料BOM每个子项
            //比较变更前后物料BOM子项每个子项增加的特定替代
            for (WTPartMaster wm : current.keySet()) {
                List<WTPartSubstituteLink> previousSubstitute=new ArrayList();
                List<WTPartSubstituteLink> currentSubstitute=new ArrayList();
                //获取当前子项的所有特定替代料
                WTPartUsageLink link = getUsageLink(part, wm);
                if (link!=null){
                    //当前子项特定替代
                    List<WTPartSubstituteLink> substituteLinks = getSubstituteLinks(link);
                    if (substituteLinks!=null){
                        for (WTPartSubstituteLink substitutelink:substituteLinks) {
                            currentSubstitute.add(substitutelink);
                        }
                    }
                }
                //获取变更前对应子项的所有特定替代料
                WTPartUsageLink prelink = getUsageLink(pre, wm);
                if (prelink!=null){
                    //变更前对应子项特定替代
                    List<WTPartSubstituteLink> substituteLinks = getSubstituteLinks(prelink);
                    if (substituteLinks!=null){
                        for (WTPartSubstituteLink substitutelink:substituteLinks) {
                            previousSubstitute.add(substitutelink);
                        }
                    }
                }

                //比较当前子项变更后新增的特定替代料
                List<WTPartSubstituteLink> diffrentList=getDiffrent(previousSubstitute,currentSubstitute);
                if (diffrentList!=null&&diffrentList.size()>0){
                    for (int i = 0; i <diffrentList.size() ; i++) {
                        WTPartUsageLink oldlink = previous.get(wm);
                        WTPartSubstituteLink link2=(WTPartSubstituteLink)diffrentList.get(i);
                        String wh =  (String) MBAUtil.getValue((WTPartSubstituteLink)diffrentList.get(i),"referenceDesignator");//替代料位号
                        //替代料数量
                        String sl="1";
                        if(link2.getQuantity()!=null){
                            if (link2.getQuantity().getAmount()!=null){
                                sl= link2.getQuantity().getAmount().toString();
                            }
                        }

                        WTPart substitutePart =PartUtil.getLastestWTPartByNumber(link2.getSubstitutes().getNumber());//获取替代料
                        WTPart child = BomUtil.getLatestPart(wm.getNumber());
                        BomCompareBean bcb = new BomCompareBean();
                        bcb.setCompareFlag("C");
                        bcb.setChildNumber(wm.getNumber());
                        bcb.setBomVersion(bomVersion);
                        bcb.setChildName(wm.getName());
                        bcb.setChildVersion(BomUtil.getVersion(child));
                        bcb.setOldVersion(BomUtil.getVersion(pre));
                        bcb.setNewVersion(BomUtil.getVersion(part));
                        bcb.setParentNumber(part.getNumber());
                        bcb.setParentName(part.getName());
                        String oldLinkSum=BomUtil.getLinkSum(oldlink);
                        String oldSum=Intercept(oldLinkSum,6);//数量截取小数点后6位
                        String newSum=Intercept(sl,6);//数量截取小数点后6位
                        bcb.setOldSum(oldSum);
                        bcb.setNewSum(newSum);
                        String oldwh = BomUtil.getPartReferenceDesignators(oldlink);
//                        String newwh = BomUtil.getPartReferenceDesignators(newLink);
                        bcb.setOldWh(oldwh);
                        bcb.setNewWh(wh);

                        String  substituteNumber=link2.getSubstitutes().getNumber();//特定替代料编码
                        String substituteVersion=part.getVersionInfo().getIdentifier().getValue();//特定替代料版本
                        String substituteBomVersion = link2.getSubstitutes().getNumber()+"_"+substituteVersion;//特定替代料BOM版本
                        bcb.setSubstituteNumber(substituteNumber);
                        bcb.setSubstituteBomVersion(substituteBomVersion);
                        bcb.setSubstituteChildVersion(substituteVersion);
                        lists.add(bcb);

                    }
                }
            }

        } catch (Exception e) {
            throw new Exception(e);
        }


        System.out.println("=====================用完旧料差异结果："+lists);
        return lists;
    }

    /**
     * 搜集随签列表中的对象
     *
     * @param self
     * @return
     * @throws WTException
     */
    public static WTArrayList collect(ObjectReference self) throws WTException {
        WfProcess wfprocess = null;
        WTArrayList list = new WTArrayList();
        wfprocess = WorkflowUtil.getProcess(self);
        ProcessReviewObjectLink link = null;
        if (wfprocess != null) {
            QueryResult queryresult = ProcessReviewObjectLinkHelper.service
                    .getProcessReviewObjectLinkByRoleA(wfprocess);
            while (queryresult != null && queryresult.hasMoreElements()) {
                link = (ProcessReviewObjectLink) queryresult.nextElement();
                WTObject obj = (WTObject) link.getRoleBObject();
                list.add(obj);
            }
        }
        return list;

    }


    /**
     * 获取两个List中list2中不在list1中的元素
     * @param list1
     * @param list2
     * @return
     */
    private static List<WTPartSubstituteLink> getDiffrent(List<WTPartSubstituteLink> list1, List<WTPartSubstituteLink> list2) {
        List<WTPartSubstituteLink> diff = new ArrayList<WTPartSubstituteLink>();
        Map<String,WTPartSubstituteLink> map1=new HashMap();
        Map<String,WTPartSubstituteLink> map2=new HashMap();
        if (list1!=null&& list1.size()<=0){
            if (list2!=null&&list2.size()>0){
                for (int i = 0; i <list2.size(); i++) {
                    diff.add(list2.get(i));
                }
            }

        }else{
            if (list1!=null&&list1.size()>0){
                for (int i = 0; i <list1.size(); i++) {
                    String number=list1.get(i).getSubstitutes().getNumber();
                    map1.put(number,list1.get(i));
                }
            }

            if (list2!=null&&list2.size()>0){
                for (int i = 0; i <list2.size(); i++) {
                    String number=list2.get(i).getSubstitutes().getNumber();
                    map2.put(number,list2.get(i));
                }
            }

            for(String str:map2.keySet())
            {
                for (String m:map1.keySet()){
                    if (!m.equals(str)){
                        diff.add(map2.get(str));
                    }
                }
            }
        }


        return diff;
    }


}
