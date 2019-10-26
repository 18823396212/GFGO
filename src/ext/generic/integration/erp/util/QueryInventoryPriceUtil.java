package ext.generic.integration.erp.util;

import com.hp.hpl.jena.reasoner.rulesys.builtins.Equal;
import ext.appo.ecn.util.AffectedItemsUtil;
import ext.customer.common.IBAUtil;
import ext.generic.integration.erp.bean.InventoryPrice;
import ext.generic.integration.erp.util.SqlHelperUtil;
import ext.pi.core.PIAttributeHelper;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.inf.container.WTContained;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.vc.VersionControlHelper;

import java.math.BigDecimal;
import java.rmi.MarshalException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static ext.generic.integration.erp.util.SqlHelperUtil.queryFAuxProperty;
import static ext.generic.integration.erp.util.SqlHelperUtil.queryOrgName;

public class QueryInventoryPriceUtil {
    //通过物料查询
    public static List queryInventoryPrice(WTPart part) throws Exception {
        System.out.println("开始执行库存价格方法=="+new Date());
        List result = new ArrayList();
        List<InventoryPrice> inventoryPrices=AffectedItemsUtil.queryInventoryPrice(part.getNumber());
        if (inventoryPrices!=null&&inventoryPrices.size()>0){
            for (int i = 0; i < inventoryPrices.size(); i++) {
                String item_id=inventoryPrices.get(i).getItem_id()==null?"":inventoryPrices.get(i).getItem_id();
                String iAveragecost=inventoryPrices.get(i).getiAveragecost()==null?"":inventoryPrices.get(i).getiAveragecost();
                String iSupplycycle=inventoryPrices.get(i).getiSupplycycle()==null?"":inventoryPrices.get(i).getiSupplycycle();
                String iMoq=inventoryPrices.get(i).getiMoq()==null?"":inventoryPrices.get(i).getiMoq();
                String iMpq=inventoryPrices.get(i).getiMpq()==null?"":inventoryPrices.get(i).getiMpq();
                String cpurPerson=inventoryPrices.get(i).getCpurPerson()==null?"":inventoryPrices.get(i).getCpurPerson();
                String cinvPerson=inventoryPrices.get(i).getCinvPerson()==null?"":inventoryPrices.get(i).getCinvPerson();
                String iQuantity=inventoryPrices.get(i).getiQuantity()==null?"":inventoryPrices.get(i).getiQuantity();
                String fTransinquantity=inventoryPrices.get(i).getfTransinquantity()==null?"":inventoryPrices.get(i).getfTransinquantity();
                String fInquantity=inventoryPrices.get(i).getfInquantity()==null?"":inventoryPrices.get(i).getfInquantity();
                String dreleaseDate=inventoryPrices.get(i).getDreleaseDate()==null?"":inventoryPrices.get(i).getDreleaseDate();
                String mVersion=inventoryPrices.get(i).getmVersion()==null?"":inventoryPrices.get(i).getmVersion();
                String company=inventoryPrices.get(i).getCompany()==null?"":inventoryPrices.get(i).getCompany();
                //去除多余的0和.
                iAveragecost=subZeroAndDot(iAveragecost.isEmpty()?"0":iAveragecost.trim());
                iSupplycycle=subZeroAndDot(iSupplycycle.isEmpty()?"0":iSupplycycle.trim());
                iMoq=subZeroAndDot(iMoq.isEmpty()?"0":iMoq.trim());
                iMpq=subZeroAndDot(iMpq.isEmpty()?"0":iMpq.trim());
                iQuantity=subZeroAndDot(iQuantity.isEmpty()?"0":iQuantity.trim());
                fTransinquantity=subZeroAndDot(fTransinquantity.isEmpty()?"0":fTransinquantity.trim());
                fInquantity=subZeroAndDot(fInquantity.isEmpty()?"0":fInquantity.trim());

                InventoryPrice inventoryPrice=new InventoryPrice();
                inventoryPrice.setItem_id(item_id);//物料编码
                inventoryPrice.setiAveragecost(iAveragecost);//物料成本
                inventoryPrice.setiSupplycycle(iSupplycycle);//供货周期
                inventoryPrice.setiMoq(iMoq);//最小订单量
                inventoryPrice.setiMpq(iMpq);//最小包装数量
                inventoryPrice.setCpurPerson(cpurPerson);//采购员
                inventoryPrice.setCinvPerson(cinvPerson);//计划员
                inventoryPrice.setiQuantity(iQuantity);//库存数量
                inventoryPrice.setfTransinquantity(fTransinquantity);//在途
                inventoryPrice.setfInquantity(fInquantity);//在制
                inventoryPrice.setDreleaseDate(dreleaseDate);//ERP更新时间
                inventoryPrice.setmVersion(mVersion);//版本
                inventoryPrice.setCompany(company);//所属公司

                if (!item_id.isEmpty()&&!mVersion.isEmpty()){
                    Set<WTPart> parts=getAllWTPart(part);
                    if (parts!=null&&parts.size()>0){
                        for (WTPart wtpart:parts) {
                            String cFlag=wtpart.getLifeCycleState().getDisplay(SessionHelper.getLocale()).toString();
                            String version=wtpart.getVersionInfo().getIdentifier().getValue();
                            if (mVersion.equals(version)){
                                inventoryPrice.setcFlag(cFlag);//发布状态
                            }

                        }
                    }
                }
                result.add(inventoryPrice);
            }

        }

        System.out.println("结束执行库存价格方法=="+new Date());
        return result;
    }


    //通过物料和所属公司查询
    public static List queryInventoryPriceWithCompany(WTPart part, String company) throws Exception{
        System.out.println("物料+"+part.getNumber()+"==compay=="+company);
        List<Object> result=new ArrayList();
        List list=queryInventoryPrice(part);
        if (list != null && list.size()>0){
            for (int i = 0; i <list.size() ; i++ ) {
                InventoryPrice inventoryPrice = (InventoryPrice) list.get(i);
                if (inventoryPrice.getCompany().contains(company)){
                    result.add(inventoryPrice);
                }
            }
        }else{
            return  null;
        }
        System.out.println("最后的result=="+result);
        return result;
    }

    //通过物料编码获取Master
    public static Set<WTPart> getAllWTPart(WTPart part) throws WTException {
        Set<WTPart> set = new HashSet<>();
        QueryResult qr = VersionControlHelper.service.allIterationsOf(part.getMaster());
        while (qr.hasMoreElements()) {
            WTPart t = (WTPart) qr.nextElement();
            set.add(t);
        }
        return set;
    }




////    //通过币别查询与人民币的汇率
////    public static String getRate(String fcurrencyId) throws Exception {
////        String rate="1";
////        if (!fcurrencyId.equals("1")) {
////            //获取不为人民币的币别与人民币的汇率（币别ID为1就是人民币）
////            List<List<Object>> rateList = SqlHelperUtil.queryRate(fcurrencyId);
////            if (rateList != null && rateList.size() > 0) {
////                for (int i = 0; i < rateList.size(); i++) {
////                    String fbegDate = String.valueOf(rateList.get(i).get(3));//失效日期
////                    //转换失效日期格式
////                    fbegDate = getDate(fbegDate);
////                    Boolean isFbegDate = compareDate(fbegDate);
////                    if (isFbegDate) {
////                        //汇率未失效
////                        String fexchangerate = String.valueOf(rateList.get(i).get(1) == null ? "" : rateList.get(i).get(1));//直接汇率
////                        return fexchangerate;
////                    }
////                }
////            }
////        }
////        return rate;
////    }
////
////
////
////    //查询map最小value值对应的key值
////    public static  List minkeyList(HashMap map){
////        List list=new ArrayList();
////        //最小value值对应的key值
////        List mList=new ArrayList();
////        list.addAll(map.values());
////        if (list!=null&&list.size()>0){
////            String min= String.valueOf(Collections.min(list));
////            System.out.println("min=="+min);
////
////            //hashmap全部key值
////            Iterator iterator=map.keySet().iterator();
////            while (iterator.hasNext()){
////                Object key =iterator.next();
//////                System.out.println("map.get(key)=="+map.get(key));
//////                System.out.println("String.valueOf(map.get(key)).equals(min)=="+String.valueOf(map.get(key)).equals(min));
////                if (String.valueOf(map.get(key)).equals(min)){
////                    //value值如果与最低value值相同则取key值
////                    mList.add(key);
////                }
////            }
////
////        }
////        System.out.println("最小key值list=="+mList);
////        return mList;
////    }
////
////    //查询map最新value值（最新审核日期）对应的key值
////    public static  List newkeyList(HashMap map){
////        List list=new ArrayList();
////        //最小value值对应的key值
////        List mList=new ArrayList();
////        list.addAll(map.values());
//////        System.out.println("key=="+map.keySet());
//////        System.out.println("values=="+map.values());
//////        System.out.println("list=="+list);
////        if (list!=null&&list.size()>0){
////            //最新审核日期
////            String max= String.valueOf(Collections.max(list));
//////            System.out.println("max=="+max);
////
////            //hashmap全部key值
////            Iterator iterator=map.keySet().iterator();
////            while (iterator.hasNext()){
////                Object key =iterator.next();
//////                System.out.println("map.get(key)=="+map.get(key));
//////                System.out.println("String.valueOf(map.get(key)).equals(min)=="+String.valueOf(map.get(key)).equals(max));
////                if (String.valueOf(map.get(key)).equals(max)){
////                    //value值如果与最低value值相同则取key值
////                    mList.add(key);
////                }
////            }
////
////        }
////        System.out.println("最新key值list=="+mList);
////        return mList;
////    }
////
////    //是否开启辅助属性版本
////    public static boolean isFIsEnable(String partNumber, String companyId) throws Exception {
//////        System.out.println("是否开启版本的company："+company);
////        //判断K3子件是否开启版本
////        //FAuxPropertyId,FIsEnable1,FUseOrgId
////        // FAuxPropertyId 100001为版本id,100002为等级
////        // FIsEnable1 true为开启，false为未开启
////        //FUseOrgId 使用组织 1为光峰
////        List<List<Object>> list2=null;
////
////        list2 = queryFAuxProperty(partNumber);
////
////        boolean isFIsEnable1=false;
////        if (list2!=null&&list2.size()>0){
//////            System.out.println("2.size():"+list2.size());
////            for (int y = 0; y < list2.size(); y++) {
////                String FAuxPropertyId = String.valueOf(list2.get(y).get(0));
////                String FIsEnable1 = String.valueOf(list2.get(y).get(1));
////                String FUseOrgId = String.valueOf(list2.get(y).get(2));
//////              System.out.println("BOM版本-子件：" + FAuxPropertyId + ";" + FIsEnable1 + ";" + FUseOrgId);
////                //companyId 100146 峰米，1 光峰
////                if (FUseOrgId.trim().equals(companyId) && FAuxPropertyId.trim().equals("100001") && FIsEnable1.trim().equals("true") ){
////                    System.out.println("true");
////                    isFIsEnable1 = true;
////                    break;
////
////                }
////            }
////        }
////        return  isFIsEnable1;
////    }
////
////
////
////    //失效日期转换
////    // 2019-05-16T11:46:12.29 ==>2019-05-16
////    public static String getDate(String date){
////        String date1="";
////        if (!date.isEmpty()&&date.length()>18){
////            date1 = date.substring(0, 10);
////
////        }
//////        System.out.println(date + "比较失效日期==" + date1);
////
////        return date1;
////    }
////
////    //审核日期转换
////    // 2019-05-16T11:46:12.29 ==>2019-05-16 11:46:12
////    public static String getNewDate (String date){
////        String date3="";
////        if (!date.isEmpty()&&date.length()>18){
////            String date1 = date.substring(0, 10);
////            String date2 = date.substring(11, 19);
////            date3 = date1 + " " + date2;
////        }
//////        System.out.println(date + "比较审核日期==" + date3);
////
////        return date3;
////    }
//
//
//    //传空
//    public static void initPrice(InventoryPrice inventoryPrice){
//
//        inventoryPrice.setItem_id("");//物料编码
//        inventoryPrice.setiAveragecost("");//物料成本
//        inventoryPrice.setiSupplycycle("");//供货周期
//        inventoryPrice.setiMoq("");//最小订单量
//        inventoryPrice.setiMpq("");//最小包装数量
//        inventoryPrice.setCpurPerson("");//采购员
//        inventoryPrice.setCinvPerson("");//计划员
//        inventoryPrice.setiQuantity("");//库存数量
//        inventoryPrice.setfTransinquantity("");//在途
//        inventoryPrice.setfInquantity("");//在制
//        inventoryPrice.setcFlag("");//发布状态
//        inventoryPrice.setDreleaseDate("");//ERP更新时间
//        inventoryPrice.setmVersion("");//版本
//        inventoryPrice.setCompany("");//所属公司
//    }
//
//    //比较失效日期(yyyy-MM-dd)与当前日期，如果 失效日期<=当前日期,返回true（已失效）
//    public static  Boolean  compareDate(String compareDate) throws Exception {
////        System.out.println("比较日期："+compareDate);
//
//        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
//        Date compare=sdf.parse(compareDate);//比较日期
//        //当前日期
//        Date date=new Date();
////        System.out.println("当前时间=="+date.getTime()+"比较时间=="+compare.getTime());
//
//        if(compare.getTime()>=date.getTime()) {
//            //失效日期>当前日期（未失效）
//            return  true;
//        }
//
//        return false;
//    }
//
//    public  static InventoryPrice queryInfo(InventoryPrice inventoryPrice,List list) throws Exception {
//        String fMaterialId_GF=String.valueOf(list.get(0) == null ? "" :list.get(0));//光峰内码
//        String fMaterialId_FM=String.valueOf(list.get(1) == null ? "" :list.get(1));//峰米内码
//        String cpurPersonId_GF=String.valueOf(list.get(2) == null ? "" :list.get(2));//光峰采购员ID
//        String cpurPersonId_FM=String.valueOf(list.get(3) == null ? "" :list.get(3));//峰米采购员ID
//        String cinvPersonId_GF=String.valueOf(list.get(4) == null ? "" :list.get(4));//光峰计划员ID
//        String cinvPersonId_FM=String.valueOf(list.get(5) == null ? "" :list.get(5));//峰米计划员ID
//        String cFlag_GF=String.valueOf(list.get(6) == null ? "" :list.get(6));//光峰物料状态
//        String cFlag_FM=String.valueOf(list.get(7) == null ? "" :list.get(7));//峰米物料状态
//        String partNumber=String.valueOf(list.get(8) == null ? "" :list.get(8));//物料编码
//        String version=String.valueOf(list.get(9) == null ? "" :list.get(9));//物料版本
//
//        String iAveragecost=inventoryPrice.getiAveragecost();//成本
//        String companyId=inventoryPrice.getCompany();//所属公司
//        String[] iStr=iAveragecost.split(";");
//        String cost="";
//        String fcurrencyid="";
//        if (iStr!=null&&iStr.length>=2){
//            cost=iStr[0];//真实价格
//            fcurrencyid=iStr[1];//币别ID
//
//            System.out.println("开始查询币别名称=="+new Date());
//            //通过币别ID查询币别名称
//            List<List<Object>> fpkNameList=SqlHelperUtil.queryFpkName(fcurrencyid);
//            System.out.println("结束查询币别名称=="+new Date());
//            if (fpkNameList!= null && fpkNameList.size() > 0) {
//                String fcurrencyName=String.valueOf(fpkNameList.get(0).get(0) == null ? "" :fpkNameList.get(0).get(0));
//                cost=cost+fcurrencyName;
//                inventoryPrice.setiAveragecost(cost);
//            }
//        }
//        System.out.println("开始查询公司名称=="+new Date());
//        //查询公司名称
//        List<List<Object>> companyList=SqlHelperUtil.queryOrgName(companyId);
//        System.out.println("结束查询公司名称=="+new Date());
//        if (companyList!= null && companyList.size() > 0) {
//            String company= String.valueOf(companyList.get(0).get(0)== null ? "" :companyList.get(0).get(0));
//            inventoryPrice.setCompany(company);//所属公司
//        }
//        System.out.println("开始查询采购员，计划员名称=="+new Date());
//        if (companyId.equals("1")){
//            //光峰
//            //通过 采购员，计划员ID sql方式查询员工名称
//            if (cpurPersonId_GF != null && !cpurPersonId_GF.equals("0") && !cpurPersonId_GF.isEmpty()) {
//                List<List<Object>> cpurPersonList = SqlHelperUtil.queryName(cpurPersonId_GF);
//                if (cpurPersonList != null && cpurPersonList.size() > 0) {
//                    //光峰采购员
//                    inventoryPrice.setCpurPerson(String.valueOf(cpurPersonList.get(0).get(0)== null ? "" :cpurPersonList.get(0).get(0)));
//                }
//            }
//            if (cinvPersonId_GF != null && !cinvPersonId_GF.equals("0") && !cinvPersonId_GF.isEmpty()) {
//                List<List<Object>> cinvPersonList = SqlHelperUtil.queryName(cinvPersonId_GF);
//                if (cinvPersonList != null && cinvPersonList.size() > 0) {
//                    //光峰计划员
//                    inventoryPrice.setCpurPerson(String.valueOf(cinvPersonList.get(0).get(0)== null ? "" :cinvPersonList.get(0).get(0)));
//                }
//            }
//            inventoryPrice.setcFlag(cFlag_GF);//光峰发布状态
//
//        }else if(companyId.equals("100146")){
//            //峰米
//            //通过 采购员，计划员ID sql方式查询员工名称
//            if (cpurPersonId_FM != null && !cpurPersonId_FM.equals("0") && !cpurPersonId_FM.isEmpty()) {
//                List<List<Object>> cpurPersonList = SqlHelperUtil.queryName(cpurPersonId_FM);
//                if (cpurPersonList != null && cpurPersonList.size() > 0) {
//                    //峰米采购员
//                    inventoryPrice.setCpurPerson(String.valueOf(cpurPersonList.get(0).get(0)== null ? "" :cpurPersonList.get(0).get(0)));
//                }
//            }
//            if (cinvPersonId_FM != null && !cinvPersonId_FM.equals("0") && !cinvPersonId_FM.isEmpty()) {
//                List<List<Object>> cinvPersonList = SqlHelperUtil.queryName(cinvPersonId_FM);
//                if (cinvPersonList != null && cinvPersonList.size() > 0) {
//                    //峰米计划员
//                    inventoryPrice.setCpurPerson(String.valueOf(cinvPersonList.get(0).get(0)== null ? "" :cinvPersonList.get(0).get(0)));
//                }
//            }
//            inventoryPrice.setcFlag(cFlag_FM);//峰米发布状态
//
//        }
//        System.out.println("结束查询采购员，计划员名称=="+new Date());
//        System.out.println("开始查询即时库存=="+new Date());
//        String iQuantity="0";
//        //即使库存表区分光峰，峰米内码（对应库存组织，目前光峰内码可查全部，峰米内码查询不到 20190723）
//        //查询库存（同一库存组织，同一版本）
//        List<List<Object>> iQuantityList=SqlHelperUtil.queryNewiQuantity(fMaterialId_GF,fMaterialId_FM,companyId,version);
//
//        if (iQuantityList!=null&&iQuantityList.size()>0){
//            iQuantity=String.valueOf(iQuantityList.get(0).get(0)==null?"0":iQuantityList.get(0).get(0));
//            //去除多余的0和.
//            iQuantity=subZeroAndDot(iQuantity.isEmpty()?"0":iQuantity.trim());
//        }
//
//        inventoryPrice.setiQuantity(iQuantity);
//        System.out.println("结束处理即时库存=="+new Date());
//        System.out.println("开始查询在制=="+new Date());
//
//        //查询在制
//        String fInquantity="0";
//        List<List<Object>> fqtyList=SqlHelperUtil.queryFqty(partNumber,version);
//        if (fqtyList!=null&&fqtyList.size()>0){
//            fInquantity=String.valueOf(fqtyList.get(0).get(0)==null?"":fqtyList.get(0).get(0));
//            //去除多余的0和.
//            fInquantity=subZeroAndDot(fInquantity.isEmpty()?"0":fInquantity.trim());
//        }
//        inventoryPrice.setfInquantity(fInquantity);//在制
//        System.out.println("结束处理在制=="+new Date());
//        System.out.println("开始查询在途=="+new Date());
////        //在途数量
////        //采购表区分光峰，峰米内码（对应采购组织）
////        Double fTransinquantity=0d;
////        List<List<Object>> pGFList=SqlHelperUtil.query2Poorder(fMaterialId_GF);
////        List<List<Object>> pFMList=SqlHelperUtil.query2Poorder(fMaterialId_FM);
////        System.out.println("结束查询在途=="+new Date());
////        List<List<Object>> poorderList = new ArrayList<>();
////        if (pGFList!=null&&pGFList.size()>0){
////            for (int m = 0; m <pGFList.size() ; m++) {
////                poorderList.add(pGFList.get(m));
////            }
////            System.out.println("采购表pGFList=="+pGFList);
////        }
////        if (pFMList!=null&&pFMList.size()>0){
////            for (int m = 0; m<pFMList.size() ; m++) {
////                poorderList.add(pFMList.get(m));
////            }
////            System.out.println("采购表pFMList=="+pFMList);
////        }
////        System.out.println("采购表poorderList=="+poorderList);
////        if (poorderList!= null && poorderList.size() > 0) {
////            for (int j = 0; j <poorderList.size() ; j++) {
////                String fpurchaseorgId=String.valueOf(poorderList.get(j).get(5) == null ? "" : poorderList.get(j).get(5));//采购订单表上的采购组织
////                String FAuxPropId=String.valueOf(poorderList.get(j).get(3) == null ? "" : poorderList.get(j).get(3));//辅助属性ID
////                String poorderVersion="";
////                List<List<Object>> fiList=SqlHelperUtil.queryFversion(FAuxPropId);
////                if (fiList!=null&&fiList.size()>0){
////                    poorderVersion= String.valueOf(fiList.get(0).get(0)== null ? "" :fiList.get(0).get(0));//采购订单上版本
////                }
////                System.out.println("采购表版本=="+poorderVersion);
////                if (companyId.equals(fpurchaseorgId)){
////                    //采购订单上的采购组织与价目表上的采购组织相同
////
////                    //开启版本（按版本累加）
////                    if (version.equals(poorderVersion)){
////                        // 采购订单上的采购组织与价目表上的版本相同（累加同一采购组织和版本的在途数量）
////                        String fremainstockinqty=String.valueOf(poorderList.get(j).get(1) == null ? "0" : poorderList.get(j).get(1));//剩余入库数量
////                        fTransinquantity = fTransinquantity +Double.valueOf(fremainstockinqty);
////                    }
////
////
////                }
////
////            }
////        }
////        BigDecimal bdfTransinquantity = new BigDecimal(fTransinquantity);
////        System.out.println("采购表上在途数==="+bdfTransinquantity);
////        inventoryPrice.setfTransinquantity(String.valueOf(bdfTransinquantity));//同一采购组织，同一版本的在途数量
////        System.out.println("结束处理在途=="+new Date());
//
//        //在途数量(同一版本，同一组织,只查询未作废状态和业务终止为正常)
//        //采购表区分光峰，峰米内码（对应采购组织）
//        String fTransinquantity="0";
//        List<List<Object>> poorderList =SqlHelperUtil.queryNewPoorder(fMaterialId_GF,fMaterialId_FM,companyId,version);
//        System.out.println("结束查询在途=="+new Date());
//        if (poorderList!=null&&poorderList.size()>0){
//            fTransinquantity=String.valueOf(poorderList.get(0).get(0)==null?"0":poorderList.get(0).get(0));
//            //去除多余的0和.
//            fTransinquantity=subZeroAndDot(fTransinquantity.isEmpty()?"0":fTransinquantity.trim());
//        }
//        inventoryPrice.setfTransinquantity(String.valueOf(fTransinquantity));//在途
//        System.out.println("结束处理在途=="+new Date());
//        return  inventoryPrice;
//    }

    /**
     * 使用java正则表达式去掉多余的.与0
     * @param str
     * @return
     */
    public static String subZeroAndDot(String str){
        if(str.indexOf(".") > 0){
            str = str.replaceAll("0+?$", "");//去掉多余的0
            str = str.replaceAll("[.]$", "");//如最后一位是.则去掉
        }
        return str;
    }

}
