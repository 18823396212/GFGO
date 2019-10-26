package ext.appo.erp.service;

import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.ecn.pdf.PdfUtil;
import ext.appo.erp.bean.BomCompareBean;
import ext.appo.erp.bean.BomInfo;
import ext.appo.erp.util.BomUtil;
import ext.appo.erp.util.ErpUtil;
import ext.appo.erp.util.KingDeeK3Helper;
import ext.appo.erp.util.PartUtil;
import ext.com.iba.IBAUtil;
import ext.pi.core.PIAttributeHelper;
import kingdee.bos.json.JSONArray;
import kingdee.bos.json.JSONObject;
import org.apache.log4j.Logger;
import wt.change2.ChangeException2;
import wt.change2.ChangeHelper2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTKeyedHashMap;
import wt.iba.value.IBAHolder;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.util.WTException;
import wt.vc.VersionControlHelper;

import java.io.IOException;
import java.util.*;

import static ext.appo.erp.util.KingDeeK3Helper.fp;
import static ext.appo.erp.util.KingDeeK3Helper.viewBOM;
import static ext.generic.integration.erp.util.SqlHelperUtil.queryBOMFid;

public class BomReleaseService2 {
    private static final Logger logger = LogR.getLogger(BomReleaseService2.class.getName());


    /**
     * 发布WTArrayList列表中的所有零部件的单层BOM结构（发送BOM流程调用入口）
     *
     * @param parts
     * @param pbo
     * @throws IOException
     */
    public static WTKeyedHashMap releasePersistableSingleLevel(WTArrayList parts, WTObject pbo, ObjectReference self) throws Exception {
        WTKeyedHashMap hashMap = new WTKeyedHashMap();

        System.out.println("==============发布BOM========");

        if (parts == null||parts.size()<=0) {
            return hashMap;
        }
        System.out.println("parts===========ERP===============" + parts);
        try {
            System.out.println("pbo=="+pbo);
            //ECN发布后数据
            WTChangeOrder2 ecn = getECNByWTObj(pbo);
            System.out.println("获取ecn"+ecn);
            if (ecn != null) {
                //ECN头部信息集合
                Map<String, String> headInfo = new HashMap<>();
                headInfo.put("ECNNumber", ecn.getNumber());
                //变更原因
                String changeComment = (String) PdfUtil.getIBAObjectValue(ecn, "ChangeCause");
                //变更说明
                String description = ecn.getDescription();
                //变更单名称
                String ecnName = ecn.getName();

                headInfo.put("changeComment", changeComment);
                headInfo.put("description", description);
                headInfo.put("ecnName", ecnName);


                //无差异
                Map<WTPart, List<BomCompareBean>> maps = new HashMap<>();

                //有差异
                //库存处理措施为报废的
                Map<WTPart, List<BomCompareBean>> map1 = new HashMap<>();
                //库存处理措施为返工的
                Map<WTPart, List<BomCompareBean>> map2 = new HashMap<>();


                System.out.println("=======ECN发布BOM到erp=====");

                //获取变更后对象
                QueryResult qs = ChangeHelper2.service.getChangeActivities(ecn);
                while (qs.hasMoreElements()) {
                    WTChangeActivity2 activity = (WTChangeActivity2) qs.nextElement();

                    QueryResult qsafter = ChangeHelper2.service.getChangeablesAfter(activity);
                    while (qsafter.hasMoreElements()) {
                        Object obj = qsafter.nextElement();
                        if (obj instanceof WTPart) {
                            WTPart part = (WTPart) obj;
                            String partNumber = part.getNumber();
                            Object obj1 = partNumber.substring(0, 1);
                            if (obj1.toString().equals("A") || obj1.toString().equals("B")) {
                                if (part.getLifeCycleState().toString().equals(ChangeConstants.RELEASED)||part.getLifeCycleState().toString().equals(ChangeConstants.ARCHIVED)) {
                                    //获取变更前对象
                                    Map<String, WTPart> beforeParts = ErpUtil.getBeforeDataWithPart(ecn);

                                    //获取变更后对象
                                    // Map<String, WTPart> afterParts = ErpUtil.getAfterDataWithPart(ecn);

                                    //库存处理措施是否一致
                                    Boolean is = ErpUtil.isKcclcsEqual(beforeParts);

                                    if (is) {
                                        System.out.println("======处理措施一致======");

                                        for (String beforeNumber : beforeParts.keySet()) {

                                            //  for (String afterNumber : afterParts.keySet()) {
                                            if (beforeNumber.equals(part.getNumber())) {

                                                WTPart prePart = beforeParts.get(beforeNumber);
                                                String kcclcs = PartUtil.getKcclcs(prePart);

                                                if ("用完为止".equals(kcclcs)) {
                                                    headInfo.put("changeType", "0");//0用完废料 1立即变更
                                                    headInfo.put("F_APPO_PLMChangeType","0");
                                                    List<BomCompareBean> bomCompareBeans = ErpUtil.buildExhaustMaterialsCompareBean(part, beforeParts.get(beforeNumber));
                                                    if (bomCompareBeans!=null&&bomCompareBeans.size()>0){
                                                        maps.put(part, bomCompareBeans);
                                                    }
                                                } else {
                                                    headInfo.put("changeType", "1");//0用完废料 1立即变更
                                                    headInfo.put("F_APPO_PLMChangeType","1");
                                                    //对比出的差异结果
                                                    List<BomCompareBean> bomCompareBeans = ErpUtil.buildCompareBean(part, beforeParts.get(beforeNumber));
                                                    if (bomCompareBeans!=null&&bomCompareBeans.size()>0){
                                                        maps.put(part, bomCompareBeans);
                                                    }
                                                }

//                                                if ("报废".equals(kcclcs)) {
//                                                    headInfo.put("changeType", "1");//0用完废料 1立即变更
//                                                    headInfo.put("F_APPO_PLMChangeType","1");
//                                                } else {
//                                                    headInfo.put("changeType", "0");//0用完废料 1立即变更
//                                                    headInfo.put("F_APPO_PLMChangeType","0");
//                                                }

//                                                //对比出的差异结果
//                                                List<BomCompareBean> bomCompareBeans = ErpUtil.buildCompareBean(part, beforeParts.get(beforeNumber));
//                                                if (bomCompareBeans!=null&&bomCompareBeans.size()>0){
//                                                    maps.put(part, bomCompareBeans);
//                                                }

                                            }
                                            // }
                                        }

                                    } else {

                                        System.out.println("======处理措施不同======");


                                        for (String beforeNumber : beforeParts.keySet()) {

                                            WTPart prePart = beforeParts.get(beforeNumber);
                                            String kcclcs = PartUtil.getKcclcs(prePart);

                                            if ("用完为止".equals(kcclcs)) {

                                                // for (String afterNumber : afterParts.keySet()) {
                                                if (beforeNumber.equals(part.getNumber())) {
                                                    //对比出的差异结果
//                                                    List<BomCompareBean> bomCompareBeans = ErpUtil.buildCompareBean(part, beforeParts.get(beforeNumber));
                                                    List<BomCompareBean> bomCompareBeans = ErpUtil.buildExhaustMaterialsCompareBean(part, beforeParts.get(beforeNumber));
                                                    if (bomCompareBeans!=null&&bomCompareBeans.size()>0){
                                                        map1.put(part, bomCompareBeans);
                                                    }
                                                }
                                                //}
                                            } else {

                                                // for (String afterNumber : afterParts.keySet()) {
                                                if (beforeNumber.equals(part.getNumber())) {
                                                    //对比出的差异结果
                                                    List<BomCompareBean> bomCompareBeans = ErpUtil.buildCompareBean(part, beforeParts.get(beforeNumber));
                                                    if (bomCompareBeans!=null&&bomCompareBeans.size()>0){
                                                        map2.put(part, bomCompareBeans);
                                                    }
                                                }
                                                // }

                                            }


                                        }

                                    }

//                                    //无需拆分
//                                    for (String beforeNumber : beforeParts.keySet()) {
//                                        //  for (String afterNumber : afterParts.keySet()) {
//                                        if (beforeNumber.equals(part.getNumber())) {
//
//                                            WTPart prePart = beforeParts.get(beforeNumber);
//
//                                            String kcclcs = PartUtil.getKcclcs(prePart);
//
//                                            if ("用完为止".equals(kcclcs)) {
//                                                    headInfo.put("changeType", "0");//0用完废料 1立即变更
//                                                    headInfo.put("F_APPO_PLMChangeType","0");
//                                            } else {
//                                                    headInfo.put("changeType", "1");//0用完废料 1立即变更
//                                                    headInfo.put("F_APPO_PLMChangeType","1");
//                                           }
//
//                                            //对比出的差异结果
//                                            List<BomCompareBean> bomCompareBeans = ErpUtil.buildCompareBean(part, beforeParts.get(beforeNumber));
//                                            if (bomCompareBeans!=null&&bomCompareBeans.size()>0){
//                                                maps.put(part, bomCompareBeans);
//                                            }
//
//                                        }
//                                    }

                                }
                            }
                        }
                    }
                }

                String company="";//所属公司
                List list=new ArrayList();
                String companyStr="0";//0光峰，1峰米，2光峰和峰米
                int size = parts.size();
//                System.out.println("parts===========ERP===============" + parts + ",size=" + size);
                for (int i = 0; i < size; i++) {
                    Persistable persistable = parts.getPersistable(i);

//                    System.out.println("persistable=========ERP================" + persistable);
                    if (persistable != null && persistable instanceof WTPart) {
                        WTPart part = (WTPart) persistable;
                        // 获取物料所属库上的‘所属公司’属性值
                        Object valueObject = PIAttributeHelper.service.getValue(part.getContainer(), "ssgs") ;
                        company = valueObject == null ? "" : (String)valueObject ;
//                        System.out.println("查询所属库公司："+valueObject);
                        //获取全部公司名
                        list.add(company);
                    }
                }

//                System.out.println("ecn所属公司："+list.toString());
                if (list!=null&&list.size()>0){
                    if (list.size()==1){
                        company=list.get(0).toString();
                        if (company.equals("FM")){
                            companyStr="1";
                        }else if (company.equals("APPO_FM")){
                            companyStr="2";
                        }else{
                            companyStr="0";
                        }
                    }else{
                        for (int i = 0; i <list.size() ; i++) {
                            if (list.get(i).equals("APPO_FM")){
                                companyStr="2";
                                break;
                            }
                        }
                        if (companyStr.equals("")){
                            int count=0;
                            for (int i = 0; i <list.size() ; i++) {
                                if (list.get(i).equals("FM")){
                                    count=count+1;
                                }
                            }
                            if (count==list.size()){
                                companyStr="1";
                            }else{
                                companyStr="2";
                            }

                        }
                    }

                }
                System.out.println("ecn所属公司标识："+companyStr);
//               companyStr  0光峰，1峰米，2光峰和峰米
//                System.out.println("companyStr.equals(2)："+companyStr.equals(2)+"companyStr.equals(\"2\")"+companyStr.equals("2")+"companyStr.equals(2)"+companyStr.trim().equals("2"));

                //无需拆分发送
                if (maps!= null && maps.size() > 0){
                    if (companyStr.trim().equals("1")){
                        //发送无需拆分的ecn（峰米）
                        hashMap.putAll(sendECN(maps, headInfo,"1"));

                    }else if (companyStr.trim().equals("2")){
                        //发送无需拆分的ecn（光峰，峰米）
//                        System.out.println("无需拆分发送光峰0");
                        hashMap.putAll(sendECN(maps, headInfo,"0"));
                        System.out.println("无需拆分结束发送光峰"+hashMap);
    //                    System.out.println("无需拆分发送峰米1");
    //                    hashMap.putAll(sendECN(maps, headInfo,"1"));
    //                    System.out.println("无需拆分结束发送峰米1"+hashMap);

                    }else{
                        //发送无需拆分的ecn（光峰）
                        hashMap.putAll(sendECN(maps, headInfo,"0"));

                    }
                }

                //拆分发送
                if (map1 != null && map1.size() > 0) {

                    String ecnNumber =(String) headInfo.get("ECNNumber");

                    if (map2 != null && map2.size() > 0) {
                        if (ecnNumber.equals(ecn.getNumber())) {
                            headInfo.put("ECNNumber", ecn.getNumber() + "-1");
                        } else {
                            headInfo.put("ECNNumber", ecn.getNumber() + "-2");
                        }
                    }

                    headInfo.put("changeType", "0");//0用完废料 1立即变更
                    headInfo.put("F_APPO_PLMChangeType","0");

                    if (companyStr.trim().equals("1")){
                        //拆分的ecn（峰米）
                        hashMap.putAll(sendECN(map1, headInfo,"1"));

                    }else if (companyStr.trim().equals("2")){
                        //拆分的ecn（光峰，峰米）
//                        System.out.println("拆分发送光峰0");
                        hashMap.putAll(sendECN(map1, headInfo,"0"));
                        System.out.println("拆分结束发送光峰"+hashMap);
//                        System.out.println("拆分发送峰米1");
//                        hashMap.putAll(sendECN(map1, headInfo,"1"));
//                        System.out.println("拆分结束发送峰米1"+hashMap);

                    }else{
                        //拆分的ecn（光峰）
                        hashMap.putAll(sendECN(map1, headInfo,"0"));
                    }


                }


                if (map2 != null && map2.size() > 0) {

                    String ecnNumber = headInfo.get("ECNNumber");

                    if (map1 != null && map1.size() > 0) {
                        if (ecnNumber.equals(ecn.getNumber())) {
                            headInfo.put("ECNNumber", ecn.getNumber() + "-1");
                        } else {
                            headInfo.put("ECNNumber", ecn.getNumber() + "-2");
                        }
                    }

                    headInfo.put("changeType", "1");//0用完废料 1立即变更
                    headInfo.put("F_APPO_PLMChangeType","1");

                    if (companyStr.trim().equals("1")){
                        //拆分的ecn（峰米）
                        hashMap.putAll(sendECN(map2, headInfo,"1"));

                    }else if (companyStr.trim().equals("2")){
                        //拆分的ecn（光峰，峰米）
//                        System.out.println("拆分发送光峰0");
                        hashMap.putAll(sendECN(map2, headInfo,"0"));
                        System.out.println("拆分结束发送光峰"+hashMap);
//                        System.out.println("拆分发送峰米1");
//                        hashMap.putAll(sendECN(map2, headInfo,"1"));
//                        System.out.println("拆分结束发送峰米1"+hashMap);
                    }else{
                        //拆分的ecn（光峰）
                        hashMap.putAll(sendECN(map2, headInfo,"0"));
                    }

                }

            } else {

                //新建物料发送BOM
                int size = parts.size();
                System.out.println("parts===========ERP===============" + parts + ",size=" + size);
                for (int i = 0; i < size; i++) {
                    Persistable persistable = parts.getPersistable(i);

//                    System.out.println("persistable=========ERP================" + persistable);
                    if (persistable != null && persistable instanceof WTPart) {
                        WTPart part = (WTPart) persistable;
                        //如果物料存在有ECN，就不发BOM
                        if (ErpUtil.isHaveECN(part)) {
                            System.out.println("============存在ECN==============");
                            continue;
                        }

                        //如果物料为发布状态，不发BOM
                        if (part.getLifeCycleState().toString().equals(ChangeConstants.RELEASED)) {
                            continue;
                        }

                        String partNumber = part.getNumber();
                        Object obj1 = partNumber.substring(0, 1);
                        if (obj1.toString().equals("A") || obj1.toString().equals("B")) {
//                            System.out.println("part===========ERP========" + part);

//                            //发送BOM
//                            WTKeyedHashMap map = sendBOM(part, pbo, self);

                            //发送BOM
                            WTKeyedHashMap map = sendBOM(part);

//                            System.out.println("map==============ERP==============" + map);
                            if (map != null && map.size() > 0) {
                                hashMap.putAll(map);
                            }

                            //提示信息以U8为主,发K3无需提示信息，后台处理
//                            if (map != null && hashMap.size() > 0) {
//                                //设置BOM发布状态失败
//                                updatePDMFailed(part, "bomReleaseStatus");
//                            } else {
//                                //设置BOM发布状态成功
//                                updatePDMSucessful(part, "bomReleaseStatus");
//                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("error", e.getMessage());

        } finally {
        }
        return hashMap;
    }

    /**
     * 发送单个BOM数据
     *
     * @param part        父件
     * @return
     * @throws IOException
     */
    public static WTKeyedHashMap sendBOM(WTPart part) {
//        System.out.println("part========ERP===============" + part);
        WTKeyedHashMap hashMap = new WTKeyedHashMap();
        if (part == null) {
            return hashMap;
        }
//        String version = part.getVersionInfo().getIdentifier().getValue() + "." + part.getIterationInfo().getIdentifier().getValue();//物料版本
//
//        System.out.println("传过来的物料版本"+version);
        try {
            //获取同一视图下最新版本的零部件
            if (!part.isLatestIteration())//不是最新版本，先获得最新小版本
                part = (WTPart) VersionControlHelper.service.getLatestIteration(part, false);

            //获得子件集合
            Set<WTPart> childParts = BomUtil.getMonolayerPart(part);

            if (childParts == null || childParts.size() <= 0) {
                return hashMap;
            }
//            //获得子件集合
//            Set<WTPart> childParts = new HashSet<>();
//            if("Design".equals(part.getViewName())) {
//                childParts = BomUtil.getMonolayerPart(part);
//            }else {
//                childParts = BomUtil.getMonolayerPartLatestOrMPart(part);//20190628
//            }
////            System.out.println("获取物料"+part.getNumber()+"的子件："+childParts);
//            if (childParts == null || childParts.size() <= 0) {
//                return hashMap;
//            }

//            String version1 = part.getVersionInfo().getIdentifier().getValue() + "." + part.getIterationInfo().getIdentifier().getValue();//物料版本
//
//            System.out.println("结束后的物料版本"+version1);

//            System.out.println("开始创建BOM并转为json方法:"+new Date());
            //获得自定义BOM信息表
            List<BomInfo> infos = BomUtil.createBomBean(part, childParts);

            // 获取物料所属库上的‘所属公司’属性值
            Object valueObject = PIAttributeHelper.service.getValue(part.getContainer(), "ssgs") ;
            String company = valueObject == null ? "" : (String)valueObject ;
//            System.out.println("所属公司："+company);

            String msg ="";


            String dataJson = KingDeeK3Helper.bomInfoToJson(infos,company);
//            System.out.println("结束创建BOM并转为json方法:"+new Date());
//            System.out.println("开始调用金蝶发送BOM接口："+new Date());
            //发送BOM信息
            msg = KingDeeK3Helper.saveMaterialBOM(dataJson);
//            System.out.println("结束调用金蝶发送BOM接口："+new Date());
            //是否发送成功
            Boolean isSuccess =isSendSuccess(msg);

            //峰米FM，FCreateOrgId 创建组织，FUseOrgId 使用组织,FChildSupplyOrgId 供应组织 都为20
            //光峰和峰米APPO_FM
            //其余为光峰，FCreateOrgId 创建组织，FUseOrgId 使用组织,FChildSupplyOrgId 供应组织 都为02
            //PkIds,100146为峰米，1为光峰
            if (company.equals("APPO_FM")&&isSuccess){
//                System.out.println("APPO_FM 发送 FM BOM");
                String orgid="100146"; //组织(内码)，1光峰，100146峰米
                String partNumber=part.getNumber();//编码
                String mversion=part.getVersionIdentifier().getValue();//大版本
                String bomVersion=partNumber+"_"+mversion;
                List<List<Object>> fidList=queryBOMFid(orgid,bomVersion);
                //发峰米，存在就修改，不存在分配
                if (fidList!=null&&fidList.size()>0){
                    //已存在BOM
                    String dataJson2 = KingDeeK3Helper.bomInfoToJson(infos,"FM");
                    //发送BOM信息
                    msg = KingDeeK3Helper.saveMaterialBOM(dataJson2);
                }else{
                    //分配
                    String PkIds=viewBOM(bomVersion);
                    String result= fp(PkIds,"100146");
                    System.out.println("分配给峰米结果："+result);
                    if (result.contains("true")){
//                        System.out.println("BOM："+bomVersion+"分配给峰米成功");
                    }else{
                        System.out.println("BOM："+bomVersion+"分配给峰米失败");
                        hashMap.put(part,"BOM："+bomVersion+"分配给峰米失败");
                    }
                }

//                String partNumber=part.getNumber();//编码
//                String mversion=part.getVersionIdentifier().getValue();//大版本
//                String bomVersion=partNumber+"_"+mversion;
//                String fuseorgid="100146"; //使用组织(内码)，1光峰，100146峰米
//                List<List<Object>> is=queryisBOM(fuseorgid,bomVersion);
//                System.out.println("分配给峰米是否存在"+is.get(0).get(0).equals("1"));
//                if (is!=null&&is.size()>0){
//                    //1就是100146峰米有此BOM版本,有版本就不分配
//                    if (!is.get(0).get(0).equals("1")){
//                        String PkIds=viewBOM(bomVersion);
//                        String result= fp(PkIds,"100146");
////                      System.out.println("分配给峰米结果："+result);
//                        if (result.contains("true")){
//                            System.out.println("BOM："+bomVersion+"分配给峰米成功");
//                        }else{
//                            System.out.println("BOM："+bomVersion+"分配给峰米失败");
//                            hashMap.put(part,"BOM："+bomVersion+"分配给峰米失败");
//                        }
//                    }
//                }


//                //查询BOM版本内码PkIds
//                String CreateOrgId="1";//创建组织内码，1光峰，100146峰米
//                String PkIds=viewBOM(bomVersion);
//                if (!PkIds.equals("0")){
//                    //分配给峰米 内码100146（光峰1）
//                    String result= fp(PkIds,"100146");
////                    System.out.println("分配给峰米结果："+result);
//                    if (result.contains("true")){
//                        System.out.println("BOM："+bomVersion+"分配给峰米成功");
//                    }else{
//                        System.out.println("BOM："+bomVersion+"分配给峰米失败");
//                        hashMap.put(part,"BOM："+bomVersion+"分配给峰米失败");
//                    }
//                }else {
//                    System.out.println("BOM："+bomVersion+"获取BOM内码失败");
//                    hashMap.put(part,"BOM："+bomVersion+"获取BOM内码失败");
//                }

            }

            if (!isSuccess) {
                String error =errorInfo(msg);
                System.out.println("===ERROE===" + error);
                throw new RuntimeException(error);
            }

        } catch (Exception e) {
            hashMap.put(part, e.toString());
            e.printStackTrace();
        } finally {

        }

        return hashMap;
    }

    /**
     * 发送单个BOM数据
     *
     * @param part        父件
     * @param pbo
     * @param self
     * @return
     * @throws IOException
     */
    public static WTKeyedHashMap sendBOM(WTPart part, WTObject pbo, ObjectReference self) throws IOException {
        System.out.println("part========ERP===============" + part);
        WTKeyedHashMap hashMap = new WTKeyedHashMap();
        if (part == null) {
            return hashMap;
        }

        String ecnNum = "";
        WTChangeOrder2 ecn = getECNByWTObj(pbo);
        try {
            //获取同一视图下最新版本的零部件
            if (!part.isLatestIteration())//不是最新版本，先获得最新小版本
                part = (WTPart) VersionControlHelper.service.getLatestIteration(part, false);


//            //获得子件集合
//            Set<WTPart> childParts = new HashSet<>();
//            if("Design".equals(part.getViewName())) {
//                childParts = BomUtil.getMonolayerPart(part);
//            }else {
//                childParts = BomUtil.getMonolayerPartLatestOrMPart(part);//20190628
//            }
////            System.out.println("获取物料"+part.getNumber()+"的子件："+childParts);
//            if (childParts == null || childParts.size() <= 0) {
//                return hashMap;
//            }

            //获得子件集合
            Set<WTPart> childParts = BomUtil.getMonolayerPart(part);

            if (childParts == null || childParts.size() <= 0) {
                return hashMap;
            }

//            System.out.println("开始创建BOM并转为json方法:"+new Date());
            //获得自定义BOM信息表
            List<BomInfo> infos = BomUtil.createBomBean(part, childParts);

            // 获取所属库上的‘所属公司’属性值
            Object valueObject = PIAttributeHelper.service.getValue(part.getContainer(), "ssgs") ;
            String company = valueObject == null ? "" : (String)valueObject ;


            //构造发送的JSON数据
            String dataJson = KingDeeK3Helper.bomInfoToJson(infos,company);
//            System.out.println("结束创建BOM并转为json方法:"+new Date());
//            System.out.println("开始调用金蝶发送物料接口："+new Date());
            //发送BOM信息
            String msg = KingDeeK3Helper.saveMaterialBOM(dataJson);
//            System.out.println("开始调用金蝶发送物料接口："+new Date());
            //是否发送成功
            Boolean isSuccess =isSendSuccess(msg);

            //峰米FM，FCreateOrgId 创建组织，FUseOrgId 使用组织,FChildSupplyOrgId 供应组织 都为20
            //光峰和峰米APPO_FM
            //其余为光峰，FCreateOrgId 创建组织，FUseOrgId 使用组织,FChildSupplyOrgId 供应组织 都为02
            //PkIds,100146为峰米，1为光峰
            if (company.equals("APPO_FM")&&isSuccess){
//                System.out.println("APPO_FM 发送 FM BOM");
                String orgid="100146"; //组织(内码)，1光峰，100146峰米
                String partNumber=part.getNumber();//编码
                String mversion=part.getVersionIdentifier().getValue();//大版本
                String bomVersion=partNumber+"_"+mversion;
                List<List<Object>> fidList=queryBOMFid(orgid,bomVersion);
                //发峰米，存在就修改，不存在分配
                if (fidList!=null&&fidList.size()>0){
                    //已存在BOM
                    String dataJson2 = KingDeeK3Helper.bomInfoToJson(infos,"FM");
                    //发送BOM信息
                    msg = KingDeeK3Helper.saveMaterialBOM(dataJson2);
                }else{
                    //分配
                    String PkIds=viewBOM(bomVersion);
                        String result= fp(PkIds,"100146");
                      System.out.println("分配给峰米结果："+result);
                        if (result.contains("true")){
//                            System.out.println("BOM："+bomVersion+"分配给峰米成功");
                        }else{
//                            System.out.println("BOM："+bomVersion+"分配给峰米失败");
                            hashMap.put(part,"BOM："+bomVersion+"分配给峰米失败");
                        }
                }



//                String partNumber=part.getNumber();//编码
//                String mversion=part.getVersionIdentifier().getValue();//大版本
//                String bomVersion=partNumber+"_"+mversion;
//                String fuseorgid="100146"; //使用组织(内码)，1光峰，100146峰米
//                List<List<Object>> is=queryisBOM(fuseorgid,bomVersion);
//                System.out.println("分配给峰米是否存在"+is.get(0).get(0).equals("1"));
//                if (is!=null&&is.size()>0){
//                    //1就是100146峰米有此BOM版本,有版本就不分配
//                    if (!is.get(0).get(0).equals("1")){
//                        String PkIds=viewBOM(bomVersion);
//                        String result= fp(PkIds,"100146");
////                      System.out.println("分配给峰米结果："+result);
//                        if (result.contains("true")){
//                            System.out.println("BOM："+bomVersion+"分配给峰米成功");
//                        }else{
//                            System.out.println("BOM："+bomVersion+"分配给峰米失败");
//                            hashMap.put(part,"BOM："+bomVersion+"分配给峰米失败");
//                        }
//                    }
//                }
//                //查询光峰的BOM版本内码PkIds
//                String CreateOrgId="1";//创建组织内码，1光峰，100146峰米
//                String PkIds=viewBOM(bomVersion);
//                if (!PkIds.equals("0")){
//                    //分配给峰米
//                    String result= fp(PkIds,"100146");
//                    if (result.contains("true")){
//                        System.out.println("BOM："+bomVersion+"分配给峰米成功");
//                    }else{
//                        System.out.println("BOM："+bomVersion+"分配给峰米失败");
//                    }
//                }else {
//                    System.out.println("BOM："+bomVersion+"获取BOM内码失败");
//                }

            }

            if (!isSuccess) {
                String error =errorInfo(msg);
                System.out.println("===ERROE===" + error);
                throw new RuntimeException(error);
            }

        } catch (Exception e) {
            hashMap.put(part, e.toString());
            e.printStackTrace();
        } finally {

        }

        return hashMap;
    }


    /**
     * 根据wtobj获得ecn
     *
     * @param wtobj
     * @return
     */
    public static WTChangeOrder2 getECNByWTObj(WTObject wtobj) {
        System.out.println("getType=="+wtobj.getType());
        System.out.println("输入的wtobj=="+wtobj);
        WTChangeOrder2 ecn = null;
        if (wtobj == null) {
            return ecn;
        }
        if (wtobj instanceof WTChangeOrder2) {
            System.out.println("wtobj instanceof WTChangeOrder2");
            ecn = (WTChangeOrder2) wtobj;
            System.out.println("wtobj instanceof WTChangeOrder2 ecn=="+ecn);
        } else if (wtobj instanceof WTChangeActivity2) {
            System.out.println("wtobj instanceof WTChangeActivity2");
            WTChangeActivity2 eca = (WTChangeActivity2) wtobj;
            System.out.println("wtobj instanceof WTChangeActivity2 eca=="+eca);
            //根据eca获得ecn
            try {
                QueryResult ecnResult = ChangeHelper2.service.getLatestChangeOrder(eca);
                System.out.println("ecnResult=="+ecnResult);
                if (ecnResult != null && ecnResult.hasMoreElements()) {
                    ecn = (WTChangeOrder2) ecnResult.nextElement();
                }
                System.out.println("ecnResult ecn=="+ecn);
            } catch (ChangeException2 e) {
                e.printStackTrace();
            } catch (WTException e) {
                e.printStackTrace();
            }
        }
        System.out.println("最后得到ecn=="+ecn);
        return ecn;
    }


    public static WTKeyedHashMap sendECN(Map<WTPart, List<BomCompareBean>> maps, Map<String, String> headInfo,String companyStr) throws Exception {
        WTKeyedHashMap hashMap = new WTKeyedHashMap();

        if (maps == null || maps.size() <= 0) {
            return hashMap;
        }


        if (maps != null && maps.size() > 0) {
            //创建发送数据的JSON
            String s = KingDeeK3Helper.ecnInfoToJson(maps, headInfo,companyStr);
            System.out.println("差异对比："+s);

            //发送ECN
            String msg = KingDeeK3Helper.saveECN(s);


            System.out.println("====msg===" + msg);

            //是否发送成功
            Boolean sendSuccess =isSendSuccess(msg);

            if (!sendSuccess) {
                String error = errorInfo(msg);

//                System.out.println("===ERROE===" + error);
                hashMap.put(maps.keySet().iterator().next(), "发布失败!" + error);
            }
//            else {
//
//                System.out.println("成功后发送生效指令");
//
//                String ecnNumber = headInfo.get("ECNNumber");
//
//                String m =  KingDeeK3Helper.ECNEffective(ecnNumber);
//
//                Boolean isOk = isSendSuccess(m);
//
//                if (!isOk){
//                    String error2 = errorInfo(m);
//                    System.out.println("===ERROE===" + error2);
//                    hashMap.put(maps.keySet().iterator().next(), "ECN生效失败!" + error2);
//                }
//
//
//
//            }

        }

        //提示信息以U8为主,发K3无需提示信息，后台处理
//        if (hashMap.size() > 0) {
//            for (WTPart part1 : maps.keySet()) {
//                //设置BOM发布状态失败
//                updatePDMFailed(part1, "bomReleaseStatus");
//            }
//
//        } else {
//            for (WTPart part1 : maps.keySet()) {
//                //设置BOM发布状态成功
//                updatePDMSucessful(part1, "bomReleaseStatus");
//            }
//        }

        return hashMap;

    }


//解析发送失败后返回的失败原因
public static String errorInfo(String data) {

    System.out.println("====MSG===="+data);

    JSONObject jsonObject = new JSONObject(data);
//    System.out.println("jsonObject==="+jsonObject);
    JSONObject result = jsonObject.getJSONObject("Result").getJSONObject("ResponseStatus");
//    System.out.println("result==="+result);
    JSONArray errors = result.getJSONArray("Errors");
//    System.out.println("errors==="+errors);

    String error = "";
//    System.out.println("errors.length()"+errors.length());
    for(int i=0;i<errors.length();i++){
        JSONObject ob = errors.getJSONObject(i);
        error+=ob.getString("Message");
    }
    System.out.println("error==="+error);

    return error;
}




//解析发送后返回的json，返回是否发送成功
public static Boolean isSendSuccess(String data) {

    System.out.println("====MSG===="+data);

    JSONObject jsonObject = new JSONObject(data);

    JSONObject result = jsonObject.getJSONObject("Result").getJSONObject("ResponseStatus");

    Boolean isSuccess = result.getBoolean("IsSuccess");

    return isSuccess;

}

    public static void updatePDMFailed(IBAHolder ibaHolder , String ibaName ){

        updatePDMData( ibaHolder , ibaName , "PDM发布失败"  ) ;

    }

    public static void updatePDMSucessful(IBAHolder ibaHolder , String ibaName ){
        updatePDMData( ibaHolder , ibaName , "PDM发布成功" );
    }


    protected static void updatePDMData(IBAHolder ibaHolder , String ibaName , String ibaValue ) {
        if (ibaHolder != null) {
            try {
                Object obj = IBAUtil.getIBAValue(ibaHolder, ibaName);

                if (obj == null || ((String) obj).trim().isEmpty() || !((String) obj).trim().equals(ibaValue)) {
                    IBAUtil.forceSetIBAValue(ibaHolder, ibaName, ibaValue);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
