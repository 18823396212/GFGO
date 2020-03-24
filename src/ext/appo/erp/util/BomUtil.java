package ext.appo.erp.util;

import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.workflow.NmWorkflowHelper;
import com.ptc.windchill.enterprise.part.commands.AlternatesSubstitutesCommand;
import ext.appo.ecn.pdf.PdfUtil;
import ext.appo.erp.bean.BomCompareBean;
import ext.appo.erp.bean.BomInfo;
import ext.appo.erp.bean.ECNInfoEntity;
import ext.appo.erp.bean.FTreeEntity;
import ext.appo.erp.constants.ERPConstants;
import ext.customer.common.MBAUtil;
import org.apache.commons.lang3.StringUtils;
import wt.enterprise.RevisionControlled;
import wt.epm.retriever.LatestConfigSpecWithoutWorkingCopies;
import wt.fc.*;
import wt.fc.collections.WTCollection;
import wt.occurrence.Occurrence;
import wt.occurrence.OccurrenceHelper;
import wt.part.*;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfState;

import java.math.BigDecimal;
import java.util.*;

import static ext.appo.erp.util.ErpUtil.getEcaWithObject;
import static ext.appo.erp.util.KingDeeK3Helper.queryMaterielFAuxProperty;
import static ext.appo.erp.util.PartUtil.*;
import static ext.generic.integration.erp.util.SqlHelperUtil.queryFENTRYID;

public class BomUtil {

    public  static  String compareChildPart(List<List<Object>> partsK3List,List partsList){
        String str="";
        if (partsK3List!=null&&partsK3List.size()>0){
            List noExistList=new ArrayList();
            List isTrueList=new ArrayList();
//            System.out.println("partsList.size()==="+partsList.size());
//            System.out.println("partsK3List.size()==="+partsK3List.size());
            if (partsList.size()!=partsK3List.size()){  //PLM子料数量与K3数量不同
                a:for (int i = 0; i < partsList.size(); i++) {
                    for (int j = 0; j <partsK3List.size() ; j++) {
                        //K3是否存在该物料编码
                        if (partsList.get(i).toString().equals(partsK3List.get(j).get(0).toString()))
                            continue a;
                    }
                    noExistList.add(partsList.get(i));
                }
                if (noExistList!=null&&noExistList.size()>0){
                    str="子料"+noExistList.toString()+"在K3不存在！";
                }
            }else{ //数量相同，查询K3物料状态是否为已审核

                for (int j = 0; j <partsK3List.size() ; j++) {
                    if (!partsK3List.get(j).get(1).toString().equals("C")){
                        isTrueList.add(partsK3List.get(j).get(0));
                    }
                }
                if (isTrueList!=null&&isTrueList.size()>0){
                    str="子料"+isTrueList+"在K3不是已审核状态！";
                }
            }
//            System.out.println("noExistList=="+noExistList);
//            System.out.println("isTrueList=="+isTrueList);
        }

        return str;
    }


    /**
     * 获取所有子（最新版）
     *
     * @param parent
     * @param collected
     * @throws WTException
     */
    public static void getAllChildLatestPart(WTPart parent, Set<WTPart> collected) throws WTException {
        QueryResult queryResult = WTPartHelper.service.getUsesWTParts(parent, new LatestConfigSpecWithoutWorkingCopies());
        while (queryResult.hasMoreElements()) {
            Persistable[] persistables = (Persistable[]) queryResult.nextElement();
            if (persistables[1] instanceof WTPart) {
                WTPart child = (WTPart) persistables[1];
                collected.add(child);
                getAllChildLatestPart(child, collected);
            }
        }
    }

    /**
     * 根据编码查找WTPartMaster
     *
     * @param number
     * @return
     * @throws WTException
     */
    public static WTPartMaster queryWTPartMaster(String number) throws WTException {
        QuerySpec qs = new QuerySpec(WTPartMaster.class);
        qs.setAdvancedQueryEnabled(true);
        SearchCondition sc = new SearchCondition(WTPartMaster.class, WTPartMaster.NUMBER, SearchCondition.EQUAL, number);
        qs.appendWhere(sc);
        QueryResult qr = PersistenceHelper.manager.find(qs);
        if (qr.hasMoreElements()) {
            return (WTPartMaster) qr.nextElement();
        }
        return null;
    }


    /**
     * 获取Part所有WTPartUsageLink
     * 即Part的下层子
     *
     * @param part
     * @return
     * @throws WTException
     */
    public static Map<WTPartMaster, WTPartUsageLink> getAllWTPartUsageLink(WTPart part) throws WTException {
        Map<WTPartMaster, WTPartUsageLink> result = new HashMap<>();
        QueryResult queryresult = PersistenceHelper.manager.navigate(part, WTPartUsageLink.USES_ROLE, WTPartUsageLink.class, false);
        while (queryresult.hasMoreElements()) {
            WTPartUsageLink usageLink = (WTPartUsageLink) queryresult.nextElement();
            WTPartMaster master = usageLink.getUses();
            result.put(master, usageLink);
        }
        return result;
    }


    /**
     * 获取版本号
     * @param p
     * @return
     */
    public static String getVersion(Persistable p){
        if (p instanceof RevisionControlled) {
            RevisionControlled rc = (RevisionControlled) p;
            String version = rc.getVersionInfo().getIdentifier().getValue();
            String iteration = rc.getIterationInfo().getIdentifier().getValue();
            return version+"."+iteration;
        }
        return "";
    }

    /**
     * 根据物料编号获得物料
     * @param partNum
     * @return
     */
    public static WTPart getLatestPart(String partNum) {
        try {
            if (partNum == null)
                return null;
            WTPartMaster partMaster = getWTPartMaster(partNum);
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

    /**
     * 根据物料编号获得Master
     * @param partnumber
     * @return
     * @throws WTException
     */
    public static WTPartMaster getWTPartMaster(String partnumber) throws WTException {
        WTPartMaster wtpartmaster = null;
        QuerySpec qs = new QuerySpec(WTPartMaster.class);
        int iIndex = qs.getFromClause().getPosition(WTPartMaster.class);
        SearchCondition sc = new SearchCondition(WTPartMaster.class, WTPartMaster.NUMBER, SearchCondition.EQUAL, partnumber.toUpperCase(), false);
        qs.appendWhere(sc, new int[iIndex]);

        QueryResult qr = PersistenceHelper.manager.find(qs);
        if (qr.hasMoreElements()) {
            wtpartmaster = (WTPartMaster) qr.nextElement();
        }
        return wtpartmaster;
    }

    /**
     * 获得数量
     * @param link
     * @return
     */
    public static String getLinkSum(WTPartUsageLink link) {
        if (link != null) {
            String str = new BigDecimal(link.getQuantity().getAmount() + "").toString();
//            System.out.println("数量中的str"+str);
            if (str.indexOf(".") > 0) {
                str = str.replaceAll("0+$", "").replaceAll("[.]$", "");
            }
//            System.out.println("数量修改的str"+str);
            return str;
        } else {
            return "";
        }
    }

    /**
     * 获得位号
     * @param partUsageLink
     * @return
     * @throws WTException
     */
    public static String getPartReferenceDesignators(WTPartUsageLink partUsageLink) throws WTException {
        String result = "";
        QueryResult qr = OccurrenceHelper.service.getUsesOccurrences(partUsageLink);
        int nOccurences = qr.size();
        ArrayList refDesignatorList = new ArrayList(nOccurences);

        while (qr.hasMoreElements()) {
            Occurrence occurrence = (Occurrence) qr.nextElement();
            String occurrenceName = occurrence.getName();
            if (occurrenceName != null) {
                refDesignatorList.add(occurrenceName);
            }
        }

        Collections.sort(refDesignatorList);
        if (!refDesignatorList.isEmpty()) {
            result = StringUtils.join(refDesignatorList, ",");
        }

        return result;
    }

    public static String compareWH(WTPartUsageLink oldlink, WTPartUsageLink newlink) throws Exception {
        List<String> oldwhs = new ArrayList<>();
        if (oldlink != null) {
            String oldWH = getPartReferenceDesignators(oldlink);
           //oldwhs = WTPartUtils.parsePartReferenceDesignators(oldWH);
        }

        List<String> newwhs = new ArrayList<>();
        if (newlink != null) {
            String newWH = getPartReferenceDesignators(newlink);//null ex
          //  newwhs = WTPartUtils.parsePartReferenceDesignators(newWH);
        }

        List<String> dels = new ArrayList<>();
        for (String s : oldwhs) {
            if (!newwhs.contains(s)) {
                dels.add(s);
            }
        }
        List<String> adds = new ArrayList<>();
        for (String s : newwhs) {
            if (!oldwhs.contains(s)) {
                adds.add(s);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("新增:");
        if (adds.size() > 0) {
            String s = org.apache.commons.lang.StringUtils.join(adds, ",");
            sb.append(s);
        } else {
            sb.append("无");
        }
        sb.append(" ").append("删除:");
        if (dels.size() > 0) {
            String s = org.apache.commons.lang.StringUtils.join(dels, ",");
            sb.append(s);
        } else {
            sb.append("无");
        }
        return sb.toString();
    }

    //通过父件个子件获得link
    public static WTPartUsageLink getUsageLink(WTPart parentWTPart, WTPartMaster childWTPartMaster) throws WTException{
        QuerySpec qs = new QuerySpec(WTPartUsageLink.class);
        int[] fromIndicies = {0, wt.query.FromClause.NULL_INDEX};
        qs.appendWhere(new SearchCondition(WTPartUsageLink.class,
                ObjectToObjectLink.ROLE_AOBJECT_REF + "." + ObjectReference.KEY + "." + ObjectIdentifier.ID,
                SearchCondition.EQUAL, PersistenceHelper.getObjectIdentifier(parentWTPart).getId()), fromIndicies);
        qs.appendAnd();
        qs.appendWhere(new SearchCondition(WTPartUsageLink.class,
                ObjectToObjectLink.ROLE_BOBJECT_REF + "." + ObjectReference.KEY + "." + ObjectIdentifier.ID,
                SearchCondition.EQUAL, PersistenceHelper.getObjectIdentifier(childWTPartMaster).getId()), fromIndicies);

        QueryResult qr = PersistenceHelper.manager.find((wt.pds.StatementSpec) qs);
        while (qr.hasMoreElements()) {
            WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
            return link;
        }
        return null;
    }


    /**
     *
     * @param part 父件
     * @param childParts 子件集合
     * @return
     * @throws WTException
     */
    public static List<BomInfo> createBomBean(WTPart part, Set<WTPart> childParts) throws Exception {
       // WTPartUsageLink link = getUsageLink(part,childPart.getMaster());

        List<BomInfo>list = new ArrayList<>();
        String number = part.getNumber();//父件编码
        String name = part.getName();//父件名称
        String view = part.getViewName();//视图
        String state = part.getLifeCycleState().toString();

        String parentwlfl="";//父件物料分类
         String wlfl = getIBAvalue(part,"Classification");//物料分类
        //获取物料分组对应数据
        if (wlfl!=null&&wlfl!=""){
            Map<String,String> map = getMaterialGroupData();
            parentwlfl=map.get(wlfl);
        }

        if (state.equals(ERPConstants.RELEASED)){
            state = "已发布";
        }else {
            state = "已归档";
        }

        // 获取物料所属库上的‘所属公司’属性值
//        Object valueObject = PIAttributeHelper.service.getValue(part.getContainer(), "ssgs") ;
//        String company = valueObject == null ? "" : (String)valueObject ;
//        System.out.println("所属公司："+company);
//        if (company.equals("APPO")){
//
//        }

        if (childParts==null||childParts.size()<=0){
            return null;
        }

//        System.out.println("part==="+part.getNumber()+"子件数："+childParts.size());
        for(WTPart childPart:childParts){
//            System.out.println("子件:"+childPart.getNumber());
            WTPartUsageLink link = getUsageLink(part,childPart.getMaster());
//            System.out.println("获取到的link==="+link);
            String defaultUnit = getIBAvalue(childPart,"defaultUnit");//默认单位

            //是否虚拟件（工艺虚拟件是true则是虚拟件）
            String sfxnj="0";
            //是否工艺虚拟件
            sfxnj=getIBAvalue(childPart,"sfxnj");
//            System.out.println("获取到的物料虚拟件=="+sfxnj);
            if (sfxnj.equals("true")){
                sfxnj="1";
            }
//            else {
//                boolean hidePartInStructureValue=childPart.getHidePartInStructure();//是否设计虚拟件
//                if (hidePartInStructureValue){
//                    sfxnj="1";
//                }else{
//                    sfxnj="0";
//                }
//            }
//            System.out.println("物料"+childPart.getNumber()+"是否虚拟件===="+sfxnj);
//            boolean hidePartInStructureValue=part.getHidePartInStructure();//是否设计虚拟件
//            String sfxnj="0";//0非虚拟键，1虚拟件
//            if (hidePartInStructureValue){
//                //虚拟件
//                sfxnj="1";
//            }
            String weihao = getPartReferenceDesignators(link);//位号
            String linkSum = getLinkSum(link);//数量
//            System.out.println("获取到的数量："+linkSum);
            if (!defaultUnit.equals("ea")){
                linkSum= Intercept(linkSum,6);//截取小数点后6位
            }
//            System.out.println("截取后的数量："+linkSum);
            String stockGrade = (String) PdfUtil.getIBAObjectValue(link, "stockGrade");//最低存货等级
            String bomNote = (String)PdfUtil.getIBAObjectValue(link, "bom_note");//BOM备注信息

            BomInfo bomInfo = new BomInfo();
            bomInfo.setLink(link);
            bomInfo.setBomRemark(bomNote==null?"":bomNote);
            bomInfo.setMajorVersion(part.getVersionIdentifier().getValue());//父件大版本
            bomInfo.setZdchdj(stockGrade==null?"":stockGrade);
            bomInfo.setParentNumber(number);
            bomInfo.setParentName(name);
            bomInfo.setChildNumber(childPart.getNumber());
            bomInfo.setChildName(childPart.getName());
            bomInfo.setWeihao(weihao == null ? "" :weihao);
            bomInfo.setView(view);
            bomInfo.setUnit(defaultUnit==null?"":defaultUnit);//传默认单位
            bomInfo.setParentwlfl(parentwlfl);//父件物料分类
            bomInfo.setChildrenView(childPart.getViewName());//子件视图

            String childVersion=childPart.getVersionIdentifier().getValue();
            String childState=childPart.getLifeCycleState().toString();
//            System.out.println("子件状态："+childState);

            //add by lzy at 20200324 start
            if (childState.equals(ERPConstants.ARCHIVED)){
                //已归档,是否在ECN流程中
                Boolean isEcnWorkflow=isRunningEcnWorkflow(childPart);
                if (isEcnWorkflow){
                    //物料发布状态是否为"PDM发布成功"
                    String isSend=getIBAvalue(childPart, "partReleaseStatus");
                    if (isSend.contains("PDM发布成功")){
                        //取当前版本
                        bomInfo.setVersion(childVersion);//子件大版本
                    }else{
                        //取上个版本或A版本
                        String EnglishLetter="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                        String strsub="A";
                        if (EnglishLetter.contains(childVersion)){
                            int index=EnglishLetter.indexOf(childVersion);
                            if (index>0){
                                strsub=EnglishLetter.substring(index-1,index);
                            }
                        }
                        bomInfo.setVersion(strsub);//子件大版本
                    }
                }else{
                    //取当前版本
                    bomInfo.setVersion(childVersion);//子件大版本
                }
            }else if (childState.equals(ERPConstants.RELEASED)){
                //已发布，取当前版本
                bomInfo.setVersion(childVersion);//子件大版本
            }else{
                //其他状态,物料发布状态是否为"PDM发布成功"
                String isSend=getIBAvalue(childPart, "partReleaseStatus");
                if (isSend.contains("PDM发布成功")){
                    //取当前版本
                    bomInfo.setVersion(childVersion);//子件大版本
                }else{
                    //取上个版本或A版本
                    String EnglishLetter="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                    String strsub="A";
                    if (EnglishLetter.contains(childVersion)){
                        int index=EnglishLetter.indexOf(childVersion);
                        if (index>0){
                            strsub=EnglishLetter.substring(index-1,index);
                        }
                    }
                    bomInfo.setVersion(strsub);//子件大版本
                }

            }
            //add by lzy at 20200324 end
//            if (childState.equals(ERPConstants.RELEASED)||childState.equals(ERPConstants.ARCHIVED)){
//                bomInfo.setVersion(childVersion);//子件大版本
//            }else{
//                //物料存在ECA
//                if (ErpUtil.isHaveECA(childPart)) {
//                    //存在，取前一个版本
//                    String EnglishLetter="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
//                    String strsub="";
//                    if (EnglishLetter.contains(childVersion)){
//                        int index=EnglishLetter.indexOf(childVersion);
//                        if (index>0){
////                        System.out.println("顺序："+index);
//                            strsub=EnglishLetter.substring(index-1,index);
////                        System.out.println("截取前一字母："+strsub);
//                        }else{
//                            strsub="A";
////                        System.out.println("没找到前一字母："+strsub);
//                        }
//                    }else{
//                        strsub="A";
////                          System.out.println("没找到前一字母："+strsub);
//                    }
//                    bomInfo.setVersion(strsub);//子件大版本
//                    System.out.println("子项物料"+childPart.getNumber()+"发的版本==="+strsub);
//                }else{
//                    //不存在eca取当前版本
//                    bomInfo.setVersion(childVersion);//子件大版本
//                    System.out.println("子项物料"+childPart.getNumber()+"发的版本==="+childVersion);
//                }
//
//            }

            bomInfo.setShuliang(linkSum);
            bomInfo.setSfxnj(sfxnj);
            bomInfo.setChildPart(childPart);
            bomInfo.setState(state);
            list.add(bomInfo);
        }

        return list;


    }

    public static Set<WTPart> getMonolayerPart(WTPart part){
        Set<WTPart> setPart = new HashSet<WTPart>();
        // part =  MversionControlHelper.getLastObjectAndRelease(part.getMaster());
        if(part != null){
            QueryResult qr = null;
            try {
                qr = WTPartHelper.service.getUsesWTPartMasters(part);
                if (qr!=null&&qr.size()>0){
//                    System.out.println("qr==="+qr.size());
                }
                while (qr.hasMoreElements()) {
                    WTPartUsageLink links = (WTPartUsageLink) qr.nextElement();
                    WTPartMaster masterChild = links.getUses();

//                WTPart sunpart = (WTPart) PartUtil.getLastestWTPartByNumber(masterChild.getNumber(),part.getViewName());
                //不需子件与父件同一视图
                WTPart sunpart = (WTPart) PartUtil.getLastestWTPartByNumber(masterChild.getNumber());
//                System.out.println("sunpart==="+sunpart);

                    setPart.add(sunpart);
                }
            } catch (WTException e) {
                e.printStackTrace();
            }
        }
        return setPart;
    }





    /**
     * 通过父项获取子件：单层, 取最新大版本的物料，如果大版本一样，取M视图
     * @param part
     * @return
     * @throws WTException
     */
    public static Set<WTPart> getMonolayerPartLatestOrMPart(WTPart part){
        Set<WTPart> setPart = new HashSet<WTPart>();
       // part =  MversionControlHelper.getLastObjectAndRelease(part.getMaster());
        if(part != null){
            QueryResult qr = null;
            try {
                qr = WTPartHelper.service.getUsesWTPartMasters(part);
                if (qr!=null&&qr.size()>0){
                    System.out.println("qr==="+qr.size());
                }
            while (qr.hasMoreElements()) {
                WTPartUsageLink links = (WTPartUsageLink) qr.nextElement();
                WTPartMaster masterChild = links.getUses();
                /*
//                WTPart sunpart = (WTPart) PartUtil.getLastestWTPartByNumber(masterChild.getNumber(),part.getViewName());
                //不需子件与父件同一视图
                WTPart sunpart = (WTPart) PartUtil.getLastestWTPartByNumber(masterChild.getNumber());
//                System.out.println("sunpart==="+sunpart);
                */
                //2019-06-28 取最新大版本的物料，如果大版本一样，取M视图
                WTPart sunpart = null;
                WTPart ePart = (WTPart) PartUtil.getLastestWTPartByNumber(masterChild.getNumber(),"Design");
                WTPart mPart = (WTPart) PartUtil.getLastestWTPartByNumber(masterChild.getNumber(),"Manufacturing");
                if(ePart != null && mPart != null){
                    if(ePart.getVersionIdentifier().getSeries().greaterThan(mPart.getVersionIdentifier().getSeries())){
                        sunpart = ePart;
                    }else{
                        sunpart = mPart;
                    }
                }else if(ePart == null && mPart != null){
                    sunpart = mPart;
                }else{
                    sunpart = ePart;
                }
                setPart.add(sunpart);
            }
            } catch (WTException e) {
                e.printStackTrace();
            }
        }
        return setPart;
    }


    // 获取特定替换件
    public static List<WTPartSubstituteLink> getSubstituteLinks(WTPartUsageLink usageLink) throws WTException {
        List<WTPartSubstituteLink> substituteLinks = new ArrayList<WTPartSubstituteLink>();
        WTCollection collection = WTPartHelper.service.getSubstituteLinks(usageLink);
        for (Object object : collection) {
            if (object instanceof ObjectReference) {
                ObjectReference objref = (ObjectReference) object;
                Object obj = objref.getObject();

                if (obj instanceof WTPartSubstituteLink) {
                    WTPartSubstituteLink link = (WTPartSubstituteLink) obj;
                    substituteLinks.add(link);
                }
            }
        }

        return substituteLinks;
    }

    // 获取全局替换件
    public static List<WTPartAlternateLink> getAlternateLinks(WTPart part) throws WTException {
        List<WTPartAlternateLink> substituteLinks = new ArrayList<WTPartAlternateLink>();
        WTCollection wtCollection =  AlternatesSubstitutesCommand.getAlternateParts(part);
        for (Object object : wtCollection) {
            if (object instanceof ObjectReference) {
                ObjectReference objref = (ObjectReference) object;
                Object obj = objref.getObject();

                if (obj instanceof WTPartAlternateLink) {
                    WTPartAlternateLink link = (WTPartAlternateLink) obj;
                    substituteLinks.add(link);
                }
            }
        }

        return substituteLinks;
    }


    /*
    发送BOM时，子项JSON字符串转换
     */
    public static String createFTreeEntityJson(List<BomInfo>list,String company) throws Exception {

        List<FTreeEntity> treeEntitys = new ArrayList<>();

        //项次
        int i = 1;
        for (BomInfo info:list){
            //子项大版本
            String version=info.getVersion();
            //子项BOM
            String bomVersion = info.getChildPart().getNumber()+"_"+version;

            //替代优先级初始化
            int j = 1;

            //全局替代不发
//            //全局替代
//            List<WTPartAlternateLink> alternateLinks =getAlternateLinks(info.getChildPart());
            //特定替代
            List<WTPartSubstituteLink> substituteLinks = getSubstituteLinks(info.getLink());


            //判断K3子件是否开启版本
            //FAuxPropertyId,FIsEnable1,FUseOrgId
            // FAuxPropertyId 100001为版本id,100002为等级
            // FIsEnable1 true为开启，false为未开启
            //FUseOrgId 使用组织 1为光峰
            List<List<Object>> list2=queryMaterielFAuxProperty(info.getChildNumber());
            boolean isFIsEnable1=false;
            if (list2!=null&&list2.size()>0){
//                System.out.println("2.size():"+list2.size());
                for (int y = 0; y < list2.size(); y++) {
                    String FAuxPropertyId = String.valueOf(list2.get(y).get(0));
                    String FIsEnable1 = String.valueOf(list2.get(y).get(1));
                    String FUseOrgId = String.valueOf(list2.get(y).get(2));
//                    System.out.println("BOM版本-子件：" + FAuxPropertyId + ";" + FIsEnable1 + ";" + FUseOrgId);
                    if (company.equals("FM")){
                        if (FUseOrgId.trim().equals("100146") && FAuxPropertyId.trim().equals("100001") && FIsEnable1.trim().equals("true") ){
                            System.out.println("true");
                            isFIsEnable1 = true;
                            break;
                        }
                    }else{
                        if (FUseOrgId.trim().equals("1") && FAuxPropertyId.trim().equals("100001") && FIsEnable1.trim().equals("true") ){
                            System.out.println("true");
                            isFIsEnable1 = true;
                            break;
                        }

                    }
                }
//                System.out.println("isFIsEnable1子件"+info.getChildNumber()+"结果：" +isFIsEnable1);
            }

            FTreeEntity fTreeEntity = new FTreeEntity();

            fTreeEntity.setCompany(company);//所属公司
//            System.out.println("所属公司;"+company);
            //是否开启版本,true开启，false未开启
            if (isFIsEnable1){
                fTreeEntity.setIsFIsEnable1("true");
            }else{
                fTreeEntity.setIsFIsEnable1("false");
            }
            String zdchdj=info.getZdchdj();//最低存货等级
//            System.out.println("zdchdj:"+zdchdj);
//            if (zdchdj!=null&&zdchdj.trim()!=""){
//                fTreeEntity.setZdchdj(zdchdj);
//            }else{
//                fTreeEntity.setZdchdj("");
//            }

            String  uuid=getUUID();//行标识


            fTreeEntity.setF_APPO_ZXCHDJ(zdchdj==null?"":zdchdj);//最新存货等级
            fTreeEntity.setVersion(version);//子项大版本
            fTreeEntity.setBomVersion(bomVersion);//子项BOM版本
            fTreeEntity.setFRowId(uuid);//行标识

            fTreeEntity.setFReplaceGroup(i);
            fTreeEntity.setFMATERIALIDCHILD(info.getChildNumber());
            fTreeEntity.setFMATERIALTYPE("1");
            fTreeEntity.setFPOSITIONNO(info.getWeihao()==null?"":info.getWeihao());
            //备注
            String bz=replace(info.getBomRemark());
            fTreeEntity.setFMEMO(bz==null?"":bz);
            fTreeEntity.setFNUMERATOR(info.getShuliang()==null?"":info.getShuliang());//分子
//            System.out.println("子项的数量:"+info.getShuliang());
            if (info.getShuliang()!=null&&!info.getShuliang().isEmpty()){
                if(info.getUnit()!=null&&"ea".equals(info.getUnit())){
                    //转换为分数
                    String sum=info.getShuliang();

                    sum=Intercept(sum,8);//截取小数点后8位
                    String xs2fs=xs2fs(sum);
//                    System.out.println("转化=="+xs2fs);
                    List xs2fsList=split2(xs2fs);
                    if (xs2fsList!=null&&xs2fsList.size()>1){
                            fTreeEntity.setFNUMERATOR(xs2fsList.get(0).toString());//分子
                            fTreeEntity.setFDENOMINATOR(xs2fsList.get(1).toString());//分母
                    }

                }
            }



//            String xnj = info.getSfxnj();
//            if (xnj.equals("1")){
//                xnj="true";
//            }else{
//                xnj="false";
//            }

//            System.out.println("最后物料"+info.getChildNumber()+"是否虚拟件===="+info.getSfxnj());

            //获得子件集合
            Set<WTPart> childParts = BomUtil.getMonolayerPart(info.getChildPart());
            System.out.println("获取物料"+info.getChildPart().getNumber()+"的子件："+childParts);
            String isBOM="0";
            String fisskip="false";//默认不跳层
            if (childParts != null &&childParts.size() > 0) {
                //有BOM
                isBOM="1";
            }

            fTreeEntity.setChildrenView(info.getChildrenView());//子项视图
//            fTreeEntity.setSfxnj(info.getSfxnj()==null?"0":info.getSfxnj());//是否虚拟件
//            //是否工艺虚拟件
//            String xnj=getIBAvalue(info.getChildPart(),"sfxnj");//判断子件是否是虚拟件
//            if (!xnj.equals("true")){
//                xnj="false";
//            }
            //是否虚拟件
            String xnj=info.getSfxnj();
            if (xnj.equals("1")){
                xnj="true";
            }else{
                xnj="false";
            }

            fTreeEntity.setFISSkip(xnj);//是否跳层，虚拟件true，非虚拟键false

            System.out.println("info.getChildPart().getNumber()的isBOM=="+isBOM);
            fTreeEntity.setSfxnj(isBOM);//是否有BOM，0没BOM,1有BOM

            if (substituteLinks!=null&&substituteLinks.size()>0){
                fTreeEntity.setFReplacePolicy("1"); //替代策略
                fTreeEntity.setFReplaceType("1");//替代方式
                fTreeEntity.setFIskeyItem(true);//替代主料
            }


            treeEntitys.add(fTreeEntity);

//            //全局替代
//            for (WTPartAlternateLink alternateLink:alternateLinks){
//                FTreeEntity entity1 = new FTreeEntity();
//                entity1.setFReplaceGroup(i);
//                entity1.setFReplacePriority(j);
//                entity1.setFMRPPriority(j);
//                entity1.setFMATERIALIDCHILD(alternateLink.getAlternates().getNumber());
//                //子项大版本
//                WTPart part=PartUtil.getLastestWTPartByNumber(alternateLink.getAlternates().getNumber());
//                String version1=part.getVersionInfo().getIdentifier().getValue();
//                //子项BOM
//                String bomVersion1 = alternateLink.getAlternates().getNumber()+"_"+version1;
//                entity1.setVersion(version1);//子项大版本
//                entity1.setBomVersion(bomVersion1);//子项BOM版本
//                entity1.setF_APPO_ZXCHDJ("");//最新存货等级
//                entity1.setFMATERIALTYPE("3");//替代件
//            //是否工艺虚拟件
//            String sfxnj="0";
//            sfxnj=getIBAvalue(part,"sfxnj");
//            if (sfxnj.equals("true")){
//                sfxnj="1";
//            }else{
//                sfxnj="0";
//            }
////                String sfxnj="0";
//////                boolean hidePartInStructureValue=part.getHidePartInStructure();//是否设计虚拟件;
//////                if (hidePartInStructureValue){
//////                    //虚拟件
////                    sfxnj="1";
////                }
//                entity1.setSfxnj(sfxnj);//是否虚拟件
//                treeEntitys.add(entity1);
//                j++;
//            }

            //局部替代
            for (WTPartSubstituteLink link:substituteLinks){
                //判断K3子件是否开启版本
                //FAuxPropertyId,FIsEnable1,FUseOrgId
                // FAuxPropertyId 100001为版本id,100002为等级
                // FIsEnable1 true为开启，false为未开启
                //FUseOrgId 使用组织 1为光峰
                List<List<Object>> list3=queryMaterielFAuxProperty(link.getSubstitutes().getNumber()==null?"":link.getSubstitutes().getNumber());
                boolean isFIsEnable2=false;
                if (list3!=null&&list3.size()>0){
//                System.out.println("2.size():"+list2.size());
                    for (int y = 0; y < list3.size(); y++) {
                        String FAuxPropertyId = String.valueOf(list3.get(y).get(0));
                        String FIsEnable1 = String.valueOf(list3.get(y).get(1));
                        String FUseOrgId = String.valueOf(list3.get(y).get(2));
//                    System.out.println("BOM版本-子件：" + FAuxPropertyId + ";" + FIsEnable1 + ";" + FUseOrgId);
                        if (company.equals("FM")){
                            if (FUseOrgId.trim().equals("100146") && FAuxPropertyId.trim().equals("100001") && FIsEnable1.trim().equals("true") ){
                                System.out.println("true");
                                isFIsEnable2 = true;
                                break;
                            }
                        }else{
                            if (FUseOrgId.trim().equals("1") && FAuxPropertyId.trim().equals("100001") && FIsEnable1.trim().equals("true") ){
                                System.out.println("true");
                                isFIsEnable2 = true;
                                break;
                            }

                        }
                    }
                }

                FTreeEntity entity2 = new FTreeEntity();
                //是否开启版本,true开启，false未开启
                if (isFIsEnable2){
                    entity2.setIsFIsEnable1("true");
                }else{
                    entity2.setIsFIsEnable1("false");
                }
                entity2.setFReplaceGroup(i);
                entity2.setFReplacePriority(j);
                entity2.setFMRPPriority(j);
                entity2.setFMATERIALIDCHILD(link.getSubstitutes().getNumber()==null?"":link.getSubstitutes().getNumber());
                entity2.setFMATERIALTYPE("3");//替代件
                String wh =  (String) MBAUtil.getValue(link,"referenceDesignator");
                entity2.setFPOSITIONNO(wh==null?"":wh);
                String bomNote2 = (String)PdfUtil.getIBAObjectValue(link, "bom_note");//BOM备注信息
                entity2.setFMEMO(bomNote2==null?"":bomNote2);
                entity2.setF_APPO_ZXCHDJ("");//最新存货等级


                //父级行主键与被替代料行标识相同
                entity2.setFParentRowId(uuid);
                String  uuid2=getUUID();
                entity2.setFRowId(uuid2);//行标识

                //子项大版本
                WTPart part=PartUtil.getLastestWTPartByNumber(link.getSubstitutes().getNumber());
                String version1=part.getVersionInfo().getIdentifier().getValue();
                //子项BOM
                String bomVersion1 = link.getSubstitutes().getNumber()+"_"+version1;
                entity2.setVersion(version1);//子项大版本
                entity2.setBomVersion(bomVersion1);//子项BOM版本
                String sl = info.getShuliang();
                entity2.setFNUMERATOR(sl==null?"1":sl);//分子
                if(link.getQuantity()!=null){
                    if (link.getQuantity().getAmount()!=null){
                        sl= link.getQuantity().getAmount().toString();
                        System.out.println("替代料"+part.getNumber()+"link=="+sl);
                        if (sl!=null&&!sl.isEmpty()){
                            String defaultUnit = getIBAvalue(part,"defaultUnit");//默认单位
                            if(defaultUnit!=null&&"ea".equals(defaultUnit)){
                                //转换为分数
                                String sum="1";
                                sum=Intercept(sl,8);//截取小数点后8位
                                String xs2fs=xs2fs(sum);
                                System.out.println("转化=="+xs2fs);
                                List xs2fsList=split2(xs2fs);
                                if (xs2fsList!=null&&xs2fsList.size()>1){
                                    entity2.setFNUMERATOR(xs2fsList.get(0).toString());//分子
                                    entity2.setFDENOMINATOR(xs2fsList.get(1).toString());//分母
                                }
                            }else{
                                entity2.setFNUMERATOR(sl);//分子
                            }
                        }
                    }else {
                        System.out.println("getAmount====================null");
                    }

                }else {
                    System.out.println("getQuantity====================null");
                }


//                System.out.println("替代料数量："+sl);
                //是否工艺虚拟件
                String sfxnj="0";
                sfxnj=getIBAvalue(part,"sfxnj");
                String tdxnj="false";
                if (sfxnj.equals("true")){
                    tdxnj=sfxnj;
                    sfxnj="1";
                }
//                else{
//                    boolean hidePartInStructureValue=part.getHidePartInStructure();//是否设计虚拟件
//                    if (hidePartInStructureValue){
//                        tdxnj=sfxnj;
//                        sfxnj="1";
//                    }else{
//                        tdxnj="false";
//                        sfxnj="0";
//                    }
//                }

                entity2.setFISSkip(tdxnj);//是否跳层，虚拟件true，非虚拟键false
//                boolean hidePartInStructureValue=part.getHidePartInStructure();//是否设计虚拟件;
//                if (hidePartInStructureValue){
//                    //虚拟件
//                    sfxnj="1";
//                }

                entity2.setChildrenView(info.getChildrenView());//子项视图
//            fTreeEntity.setSfxnj(info.getSfxnj()==null?"0":info.getSfxnj());//是否虚拟件
                //获得子件集合
                Set<WTPart> childParts2 = BomUtil.getMonolayerPart(part);
//            System.out.println("获取物料"+part.getNumber()+"的子件："+childParts);
                String isBOM2="0";
                if (childParts2 != null &&childParts2.size() > 0) {
                    //有BOM
                    isBOM2="1";
                }
//                entity2.setSfxnj(sfxnj);//是否虚拟件
                entity2.setSfxnj(isBOM2);//是否有BOM，0没BOM,1有BOM

                entity2.setFReplacePolicy("1"); //替代策略
                entity2.setFReplaceType("1");//替代方式
                entity2.setFIskeyItem(true);//替代主料
                treeEntitys.add(entity2);
                j++;
            }
            i++;
        }

       //String json  =  JSONObject.valueToString(treeEntitys);

        String json  =treeEntitys.toString();

//        System.out.println("===FTreeEntityJSON===:"+json);

        return json;
    }


    /**
     * 创建发送变更单JSON数据
     * @param map
     * @return
     */
    public static List<ECNInfoEntity> getECNInfoEntityToJson(Map<WTPart,List<BomCompareBean>> map,String company) throws Exception {


        //company 02光峰，20峰米
        List<ECNInfoEntity>list = new ArrayList<>();

        for (WTPart key:map.keySet()){
            List<BomCompareBean> bomCompareBeans = map.get(key);

            Integer i = 1;

            for (BomCompareBean bean:bomCompareBeans){

                //添加
                if (bean.getCompareFlag().equals("A")){
                    String FBomEntryIdStr="";
                    //ECN行组别(新增需要不同的行组别)
                    String  uuid=getUUID();

                    ECNInfoEntity entity = new ECNInfoEntity();
                    entity.setFRowType("1");//1新增，2修改，3删除
                    entity.setFBOMVERSION(bean.getBomVersion());//BOM版本
                    entity.setFParentMaterialId(bean.getParentNumber());//父物料编码
                    String bz = bean.getBombzxx();
                    entity.setFMEMO(bz==null?"":bz);//备注
                    entity.setFPOSITIONNO(bean.getWeihao());//位号
                    entity.setFNUMERATOR(bean.getNewSum());//分子
                    entity.setFMATERIALIDCHILD(bean.getChildNumber());//子件编码
                    entity.setFChangeLabel("2");
                    entity.setCompany(company);

                    //子项是否开启版本
                    boolean isFIsEnable= isFIsEnable(bean.getChildNumber(),company);
                    entity.setFIsEnable(isFIsEnable);
                    String childrenVersion=bean.getChildVersion().substring(0,1);
                    String version=bean.getChildNumber()+"_"+childrenVersion;//子项BOM版本
                    entity.setVersion(version);
                    entity.setFECNGroup(uuid);//ECN行组别(多行新增需要)

                    list.add(entity);

                }

                //修改
                if (bean.getCompareFlag().equals("C")){
                    //子项是否开启版本
                    boolean isFIsEnable= isFIsEnable(bean.getChildNumber(),company);
                    String childrenVersion=bean.getChildVersion().substring(0,1);
                    String version=bean.getChildNumber()+"_"+childrenVersion;//子项BOM版本

                    String FUSEORGID="1";//默认内码 光峰，1光峰，100146峰米
                    //company  02光峰，20峰米
                    if (company.equals("20")){
                        FUSEORGID="100146";
                    }
                    List<List<Object>> queryFENTRYIDList=queryFENTRYID(bean.getBomVersion(),bean.getChildNumber(),FUSEORGID);
                    String FBomEntryIdStr="";
                    if (queryFENTRYIDList!=null&&queryFENTRYIDList.size()>0){
                        FBomEntryIdStr=String.valueOf(queryFENTRYIDList.get(0).get(0));
                    }
//                    System.out.println("修改FBomEntryIdStr==="+FBomEntryIdStr);

                    //ECN行组别(变更前变更后要相同)
                    String  uuid=getUUID();



                    //变更前
                    ECNInfoEntity entity1 = new ECNInfoEntity();
                    entity1.setFRowType("2");//1新增，2修改，3删除
                    entity1.setFReplaceGroup(i);
                    entity1.setFBOMVERSION(bean.getBomVersion());//BOM版本
                    entity1.setFMATERIALIDCHILD(bean.getChildNumber());
                    entity1.setFParentMaterialId(bean.getParentNumber());//父物料编码
                    entity1.setFNUMERATOR(bean.getOldSum());//数量（分子）
                    entity1.setFPOSITIONNO(bean.getOldWh());
                    entity1.setFChangeLabel("0");//变更前
                    entity1.setCompany(company);
                    entity1.setFIsEnable(isFIsEnable);
                    entity1.setVersion(version);

                    entity1.setFBomEntryId(FBomEntryIdStr==null?"":FBomEntryIdStr);
                    entity1.setFECNGroup(uuid);//ECN行组别(变更前变更后要相同)

                    //变更后
                    ECNInfoEntity entity2 = new ECNInfoEntity();

                    entity2.setFRowType("2");//1新增，2修改，3删除
                    entity2.setFReplaceGroup(i);
                    entity2.setFBOMVERSION(bean.getBomVersion());//BOM版本

                    entity2.setFParentMaterialId(bean.getParentNumber());//父物料编码
                    entity2.setFNUMERATOR(bean.getNewSum());//数量（分子）
                    entity2.setFPOSITIONNO(bean.getNewWh());
                    entity2.setFChangeLabel("1");//变更后
                    entity2.setCompany(company);

                    if (bean.getSubstituteNumber()!=null&&bean.getSubstituteNumber()!=""){
                        //替代料是否开启版本
                        boolean isFIsEnable2= isFIsEnable(bean.getSubstituteNumber(),company);
                        //子项是否开启版本
                        entity2.setFIsEnable(isFIsEnable2);
                    }else{
                        entity2.setFIsEnable(isFIsEnable);
                    }



                    entity2.setFBomEntryId(FBomEntryIdStr==null?"":FBomEntryIdStr);
                    entity2.setFECNGroup(uuid);//ECN行组别(变更前变更后要相同)

                    //替代料编码
                    if (bean.getSubstituteNumber()!=null&&bean.getSubstituteNumber()!=""){
                        entity2.setFMATERIALIDCHILD(bean.getSubstituteNumber());
                    }else{
                        entity2.setFMATERIALIDCHILD(bean.getChildNumber());
                    }
                    //替代料BOM版本
                    if (bean.getSubstituteBomVersion()!=null&&bean.getSubstituteBomVersion()!=""){
                        entity2.setVersion(bean.getSubstituteBomVersion());
                    }else{
                        entity2.setVersion(version);
                    }
                    //替代料版本
                    if (bean.getSubstituteChildVersion()!=null&&bean.getSubstituteChildVersion()!=""){
                        //传前一个版本，如果为A版不发
                        String EnglishLetter="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                        String strsub="";
//                        System.out.println("子项版本："+bean.getSubstituteChildVersion());
                        if (EnglishLetter.contains(bean.getSubstituteChildVersion())){
                            int index=EnglishLetter.indexOf(bean.getSubstituteChildVersion());
                            if (index>0){
//                        System.out.println("顺序："+index);
                                strsub=EnglishLetter.substring(index-1,index);
//                                System.out.println("截取前一字母："+strsub);
                            }else{
                                strsub="";
//                        System.out.println("没找到前一字母："+strsub);
                            }
                        }else{
                            strsub="";
//                    System.out.println("没找到前一字母："+strsub);
                        }
//                        System.out.println("变更版本："+strsub);
                        entity2.setMversion(strsub);//变更后传子项版本（前一个版本，A版则不发）
                    }else{
                        //传前一个版本，如果为A版不发
                        String EnglishLetter="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                        String strsub="";
//                        System.out.println("子项版本："+childrenVersion);
                        if (EnglishLetter.contains(childrenVersion)){
                            int index=EnglishLetter.indexOf(childrenVersion);
                            if (index>0){
//                        System.out.println("顺序："+index);
                                strsub=EnglishLetter.substring(index-1,index);
//                                System.out.println("截取前一字母："+strsub);
                            }else{
                                strsub="";
//                        System.out.println("没找到前一字母："+strsub);
                            }
                        }else{
                            strsub="";
//                    System.out.println("没找到前一字母："+strsub);
                        }
//                        System.out.println("变更版本："+strsub);
                        entity2.setMversion(strsub);//变更后传子项版本（前一个版本，A版则不发）
                    }

                    list.add(entity1);
                    list.add(entity2);
                    i++;
                }

                //删除
                if (bean.getCompareFlag().equals("D")){
                    //ECN行组别(删除需要不同的行组别)
                    String  uuid=getUUID();

                    ECNInfoEntity entity = new ECNInfoEntity();
                    entity.setFRowType("3");//1新增，2修改，3删除
                    entity.setFBOMVERSION(bean.getBomVersion());//BOM版本
                    entity.setFMATERIALIDCHILD(bean.getChildNumber());
                    entity.setFParentMaterialId(bean.getParentNumber());//父物料编码
                    entity.setFNUMERATOR(bean.getOldSum());//分子
                    entity.setFChangeLabel("3");
                    entity.setCompany(company);

                    //子项是否开启版本
                    boolean isFIsEnable= isFIsEnable(bean.getChildNumber(),company);
                    entity.setFIsEnable(isFIsEnable);
                    String childrenVersion=bean.getChildVersion().substring(0,1);
                    String version=bean.getChildNumber()+"_"+childrenVersion;//子项BOM版本
                    entity.setVersion(version);

                    String FUSEORGID="1";//默认内码 光峰，1光峰，100146峰米
                    //company  02光峰，20峰米
                    if (company.equals("20")){
                        FUSEORGID="100146";
                    }
                    List<List<Object>> queryFENTRYIDList=queryFENTRYID(bean.getBomVersion(),bean.getChildNumber(),FUSEORGID);
                    String FBomEntryIdStr="";
                    if (queryFENTRYIDList!=null&&queryFENTRYIDList.size()>0){
                        FBomEntryIdStr=String.valueOf(queryFENTRYIDList.get(0).get(0));
                    }
//                    System.out.println("删除FBomEntryIdStr==="+FBomEntryIdStr);
                    entity.setFBomEntryId(FBomEntryIdStr==null?"":FBomEntryIdStr);
                    entity.setFECNGroup(uuid);//ECN行组别(多行新增需要)

                    list.add(entity);
                }
            }
        }

        return list;
    }

    //是否开启辅助属性版本
    public static boolean isFIsEnable(String partNumber, String company){
//        System.out.println("是否开启版本的company："+company);
        //判断K3子件是否开启版本
        //FAuxPropertyId,FIsEnable1,FUseOrgId
        // FAuxPropertyId 100001为版本id,100002为等级
        // FIsEnable1 true为开启，false为未开启
        //FUseOrgId 使用组织 1为光峰
        List<List<Object>> list2= null;
        try {
            list2 = queryMaterielFAuxProperty(partNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean isFIsEnable1=false;
        if (list2!=null&&list2.size()>0){
//            System.out.println("2.size():"+list2.size());
            for (int y = 0; y < list2.size(); y++) {
                String FAuxPropertyId = String.valueOf(list2.get(y).get(0));
                String FIsEnable1 = String.valueOf(list2.get(y).get(1));
                String FUseOrgId = String.valueOf(list2.get(y).get(2));
//                    System.out.println("BOM版本-子件：" + FAuxPropertyId + ";" + FIsEnable1 + ";" + FUseOrgId);
                //company 20峰米，02光峰
                if (company.equals("20")){
                    if (FUseOrgId.trim().equals("100146") && FAuxPropertyId.trim().equals("100001") && FIsEnable1.trim().equals("true") ){
                        System.out.println("true");
                        isFIsEnable1 = true;
                        break;
                    }
                }else{
                    if (FUseOrgId.trim().equals("1") && FAuxPropertyId.trim().equals("100001") && FIsEnable1.trim().equals("true") ){
                        System.out.println("true");
                        isFIsEnable1 = true;
                        break;
                    }

                }
            }
        }
        return  isFIsEnable1;
    }

    //转化为分数
    public static String xs2fs(String fNumber) {

        String sA = String.valueOf(fNumber);

        if (sA.indexOf(".") < 0) {
            // fNumber is an integer

            return sA;
        }

        String sZsbf = sA.substring(0,sA.indexOf(".") );
        String sXsbf = sA.substring(sA.indexOf(".") + 1);

        int nXsws = sXsbf.length() ; //小数位数

        long lFenmu = 1;
        for (int k=0; k< nXsws; k++)
            lFenmu *= 10;

        long lFenzi = Long.parseLong( sZsbf + sXsbf );

        long lXs = (lFenzi < lFenmu) ? lFenzi : lFenmu;

        long j = 1; //最大公约数
        for (j = lXs; j > 1; j --) {
            if (lFenzi % j ==0 && lFenmu % j == 0) {
                break;
            }
        }

        lFenzi = lFenzi / j;
        lFenmu = lFenmu / j;

        return String.valueOf(lFenzi) + "/" + String.valueOf(lFenmu) ;

    }

    //拆分分数/,
    public static List split2(String param){
        List resultList=new ArrayList();
        String[] list=param.split("/");
        System.out.println("list:"+list.length);
        for (int i = 0; i < list.length; i++) {
            resultList.add(list[i]);
//            System.out.println("拆分"+list[i]);
        }
        return  resultList;
    }

    //截取小数点后几位（str截取字符串，i小数点后几位）
    public static String Intercept(String str,int i){
        String[] numbers=str.split("\\.");
//        System.out.println("numbers length:"+numbers.length);
        if (numbers.length>1){
            if (numbers[1].length()>i){
                String numbers1=numbers[1].substring(0,i);
                String intNumber = numbers[0]+"."+numbers1;
//                System.out.println("intNumber:"+intNumber);
                return  intNumber;
            }else{
                return  str;
            }
        }else{
            return str;
        }
    }

    /*
        通过number获取某视图的最新的物料
         */
    public static WTPart getLastestWTPartByNumber(String numStr,String view){
        QuerySpec queryspec = null;
        try {
            queryspec = new QuerySpec(WTPart.class);

            queryspec.appendSearchCondition(new SearchCondition(WTPart.class,
                    WTPart.NUMBER, SearchCondition.EQUAL, numStr));
            QueryResult queryresult = PersistenceHelper.manager.find(queryspec);
            LatestConfigSpec cfg = new LatestConfigSpec();
            QueryResult qr = cfg.process(queryresult);
            while (qr.hasMoreElements()) {
                WTPart part = (WTPart) qr.nextElement();
                if (!part.getViewName().equals(view)){
                    continue;
                }
                return part;
            }
        } catch (WTException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Vector getBomHistoricalData (WTPart part, String view) throws WTException {
        QueryResult qrVersions = null;
        Vector result = new Vector();
        qrVersions = VersionControlHelper.service.allVersionsOf(part.getMaster());
//			System.out.println("qrVersions size"+qrVersions.size());
        while (qrVersions.hasMoreElements()) {
            WTPart t = (WTPart) qrVersions.nextElement();
//				String version = t.getVersionInfo().getIdentifier().getValue() + "." + t.getIterationInfo().getIdentifier().getValue();//物料版本
//				System.out.println("version=========:"+version+";version===="+t.getViewName());
            //同一视图
            if (t.getViewName().equals(view)) {
                result.add(t);
            }
        }
        return result;
    }


    //UUID
    public static String getUUID() {
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        String uuidStr = str.replace("-", "");
        System.out.println("uuidStr:"+uuidStr);
        return uuidStr;
    }


    /**
     * 判断pbo是否在ECN流程中运行
     * @param pbo
     * @return
     * @throws WTException
     */
    private static boolean isRunningEcnWorkflow(WTPart pbo) throws WTException{
        boolean processok = false;
        NmOid nmOid = NmOid.newNmOid(PersistenceHelper.getObjectIdentifier(pbo));
        QueryResult qr = NmWorkflowHelper.service.getRoutingHistoryData(nmOid);
        while(qr.hasMoreElements()){
            WfProcess process = (WfProcess)qr.nextElement();
            String templateName = process.getTemplate().getName();
            if (process.getState().equals(WfState.OPEN_RUNNING) && templateName.contains("ECNWF")) {
                processok = true;
                break;
            }
        }
        return processok;
    }

}
