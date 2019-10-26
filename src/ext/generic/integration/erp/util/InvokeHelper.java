package ext.generic.integration.erp.util;

import ext.appo.erp.util.ReadProperty;
import kingdee.bos.json.JSONArray;
import kingdee.bos.json.JSONObject;
import kingdee.bos.webapi.client.K3CloudApiClient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class InvokeHelper {

    // K3 Cloud WebSite URL Example "http://192.168.19.113/K3Cloud/"
//    public static String POST_K3CloudURL = "https://appo.test.ik3cloud.com/K3cloud/";
//    public static String POST_K3CloudURL = "http://10.32.252.99/k3cloud/";

    public static String POST_K3CloudURL = "";

    static {
        try {
            Map<String,String> map=ReadProperty.ReadProperty();
            POST_K3CloudURL=map.get("K3CloudURL");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Cookie 值
    public static String CookieVal = null;
    //BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc
    private static Map map = new HashMap();
    static {
        map.put("Query",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        map.put("Query2",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        map.put("Query3",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        map.put("QueryAll",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        map.put("QueryisBOM",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        map.put("QueryBOMFid",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        //分配
        map.put("Allocate",
                "Kingdee.BOS.WebApi.ServicesStub.DynamicFormService.Allocate.common.kdsvc");
        //查询价目表
        map.put("QueryNewPriceCategory",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        //查询币别名称
        map.put("QueryFpkName",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        //查询采购表
        map.put("QueryNewPoorder",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        //查询即使库存表
        map.put("QueryNewiQuantity",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        //查询辅助属性版本
        map.put("QueryFversion",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        //查询库存同一版本和
        map.put("QueryIqty",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        //查询在途同一版本和
        map.put("QueryPoqty",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        //查询在制同一版本和
        map.put("QueryFqty",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        //查询物料信息
        map.put("Query2Materiel",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        //查询全部在制，在途，库存
        map.put("Query2All",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        //查询全部在制，在途，库存 总数
        map.put("Query2AllCount",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        //查询同一物料编码，版本在制
        map.put("QueryNewFqty",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        //查询同一物料编码，版本库存
        map.put("QueryNewIqty",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        //查询同一物料编码，版本在途
        map.put("QueryNewPoqty",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");

        //查询同一物料编码，版本在制 总数
        map.put("QueryNewFqtyCount",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        //查询同一物料编码，版本 库存 总数
        map.put("QueryNewIqtyCount",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        //查询同一物料编码，版本 在途 总数
        map.put("QueryNewPoqtyCount",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        //查询同一物料编码，版本下最新审核日期的最最低价格 价目表总数
        map.put("QueryNewPriceCategoryCount",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        //查询价目表（新）
        map.put("QueryNewPriceCategory2",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        //查询组织编码
        map.put("QueryOrg",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        //查询所有物料信息
        map.put("QueryAllMateriel",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");
        //查询所有物料总数
        map.put("QueryAllMaterielCount",
                "BGJ.K3.CRM.WebApi.Plugin.Service.ERPGetCurrencyJson,BGJ.K3.CRM.WebApi.Plugin.common.kdsvc");

    }

    // HttpURLConnection
    private static HttpURLConnection initUrlConn(String url, String paras)
            throws Exception {
//        HashMap map= (HashMap) readProperty.readProperty();
//        String POST_K3CloudURL=(String) map.get("K3CloudURL");
        URL postUrl = new URL(POST_K3CloudURL.concat(url));
        HttpURLConnection connection = (HttpURLConnection) postUrl
                .openConnection();
        if (CookieVal != null) {
            connection.setRequestProperty("Cookie", CookieVal);
        }
        if (!connection.getDoOutput()) {
            connection.setDoOutput(true);
        }
        connection.setRequestMethod("POST");
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("Content-Type", "application/json");
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());

        UUID uuid = UUID.randomUUID();
        int hashCode = uuid.toString().hashCode();

        JSONObject jObj = new JSONObject();

        jObj.put("format", 1);
        jObj.put("useragent", "ApiClient");
        jObj.put("rid", hashCode);
        jObj.put("parameters", chinaToUnicode(paras));
        jObj.put("timestamp", new Date().toString());
        jObj.put("v", "1.0");

        out.writeBytes(jObj.toString());
//        System.out.println("-----------------------------------");
//        System.out.println(jObj.toString());
//        System.out.println("-----------------------------------");
        out.flush();
        out.close();

        return connection;
    }

    // Query
    public static List<List<Object>> Query(String sql) throws Exception {
        return Invoke("Query",sql);
    }
    // Query2
    public static List<List<Object>> Query2(String sql) throws Exception {
        return Invoke("Query2",sql);
    }
    //Query3
    public static  List<List<Object>> Query3(String sql) throws Exception {
        return Invoke("Query3",sql);
    }
    // QueryAll
    public static List<List<Object>> QueryAll(String sql) throws Exception {
        return Invoke("QueryAll",sql);
    }
    //QueryisBOM
    public static List<List<Object>> QueryisBOM(String sql) throws Exception {
        return  Invoke("QueryisBOM",sql);
    }
    //QueryBOMFid
    public static List<List<Object>> QueryBOMFid(String sql) throws Exception {
        return  Invoke("QueryBOMFid",sql);
    }
    //Allocate
    public static String Allocate(String formId,String content) throws Exception {
        return  Invoke("Allocate",formId,content);
    }
    //QueryNewPriceCategory
    public static List<List<Object>> QueryNewPriceCategory(String sql) throws Exception {
        return Invoke("QueryNewPriceCategory",sql);
    }
    //QueryFpkName
    public static List<List<Object>> QueryFpkName(String sql) throws Exception {
        return Invoke("QueryFpkName",sql);
    }
    //QueryNewPoorder
    public static List<List<Object>> QueryNewPoorder(String sql) throws Exception {
        return Invoke("QueryNewPoorder",sql);
    }
    //QueryNewiQuantity
    public static List<List<Object>> QueryNewiQuantity(String sql) throws Exception {
        return Invoke("QueryNewiQuantity",sql);
    }
    //QueryFversion
    public static List<List<Object>> QueryFversion(String sql) throws Exception {
        return Invoke("QueryFversion",sql);
    }
    //QueryIqty
    public static List<List<Object>> QueryIqty(String sql) throws Exception {
        return Invoke("QueryIqty",sql);
    }
    //QueryPoqty
    public static List<List<Object>> QueryPoqty(String sql) throws Exception {
        return Invoke("QueryPoqty",sql);
    }
    //QueryFqty
    public static List<List<Object>> QueryFqty(String sql) throws Exception {
        return Invoke("QueryFqty",sql);
    }
    //Query2Materiel
    public static List<List<Object>> Query2Materiel(String sql) throws Exception {
        return Invoke("Query2Materiel",sql);
    }
    //Query2All
    public static List<List<Object>> Query2All(String sql) throws Exception {
        return Invoke("Query2All",sql);
    }
    //Query2AllCount
    public static List<List<Object>> Query2AllCount(String sql) throws Exception {
        return Invoke("Query2AllCount",sql);
    }
    //QueryNewFqty
    public static List<List<Object>> QueryNewFqty(String sql) throws Exception {
        return Invoke("QueryNewFqty",sql);
    }
    //QueryNewIqty
    public static List<List<Object>> QueryNewIqty(String sql) throws Exception {
        return Invoke("QueryNewIqty",sql);
    }
    //QueryNewPoqty
    public static List<List<Object>> QueryNewPoqty(String sql) throws Exception {
        return Invoke("QueryNewPoqty",sql);
    }
    //QueryNewFqtyCount
    public static List<List<Object>> QueryNewFqtyCount(String sql) throws Exception {
        return Invoke("QueryNewFqtyCount",sql);
    }
    //QueryNewIqtyCount
    public static List<List<Object>> QueryNewIqtyCount(String sql) throws Exception {
        return Invoke("QueryNewIqtyCount",sql);
    }
    //QueryNewPoqtyCount
    public static List<List<Object>> QueryNewPoqtyCount(String sql) throws Exception {
        return Invoke("QueryNewPoqtyCount",sql);
    }
    //QueryNewPriceCategoryCount
    public static List<List<Object>> QueryNewPriceCategoryCount(String sql) throws Exception {
        return Invoke("QueryNewPriceCategoryCount",sql);
    }
    //QueryNewPriceCategory2
    public static List<List<Object>> QueryNewPriceCategory2(String sql) throws Exception {
        return Invoke("QueryNewPriceCategory2",sql);
    }
    //QueryOrg
    public static List<List<Object>> QueryOrg(String sql) throws Exception {
        return Invoke("QueryOrg",sql);
    }
    //QueryAllMateriel
    public static List<List<Object>> QueryAllMateriel(String sql) throws Exception {
        return Invoke("QueryAllMateriel",sql);
    }
    //QueryAllMaterielCount
    public static List<List<Object>> QueryAllMaterielCount(String sql) throws Exception {
        return Invoke("QueryAllMaterielCount",sql);
    }


    private static String Invoke(String deal, String formId, String content)
            throws Exception {

        String sUrl = map.get(deal).toString();
        JSONArray jParas = new JSONArray();
        HttpURLConnection connectionInvoke = null;
        if(null != formId && content != null){
            jParas.put(formId);
            jParas.put(content);
            connectionInvoke = initUrlConn(sUrl, jParas.toString());
        }else{
            connectionInvoke = initUrlConn(sUrl, content);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                connectionInvoke.getInputStream()));

        String line;
        String sResult ="";
//        System.out.println(" ============================= ");
//        System.out.println(" Contents of post request ");
//        System.out.println(" ============================= ");
        while ((line = reader.readLine()) != null) {
            sResult = new String(line.getBytes(), "utf-8");
        }
//        System.out.println(" ============================= ");
//        System.out.println(" Contents of post request ends ");
//        System.out.println(" ============================= ");
        reader.close();

        connectionInvoke.disconnect();

        return sResult;
    }

    private static List<List<Object>> Invoke(String deal, String sql)
            throws Exception {

        List resultList=new ArrayList();

        String sUrl = map.get(deal).toString();
        JSONArray jParas = new JSONArray();
        HttpURLConnection connectionInvoke = null;
        if(null != sql){
            jParas.put(sql);
            connectionInvoke = initUrlConn(sUrl, jParas.toString());
        }else{
//            connectionInvoke = initUrlConn(sUrl, sql);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connectionInvoke.getInputStream()));

        String line;
//        System.out.println(" ============================= ");
//        System.out.println(" Contents of post request ");
//        System.out.println(" ============================= ");
        while ((line = reader.readLine()) != null) {
            String sResult = new String(line.getBytes(), "utf-8");
            if (sResult.contains("true")) {
                String result = sResult.substring(17, sResult.length() - 14);
//                System.out.println("deal==="+deal+";我的查询表==="+result);
                JSONObject jsonObject = new JSONObject(result);
                JSONArray table = jsonObject.getJSONArray("Table");


                if (deal.equals("Query")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        //FBASEUNITQTY 基本单位数量  //FSTOCKBASEQTY 库存基本数量 //FSTOCKQTY 库存数量 //FPURCHASERID 采购员 //FDATE 采购日期

                        String FBASEUNITQTY = jsonObject1.getString("FBASEUNITQTY");//基本单位数量
                        String FSTOCKBASEQTY = jsonObject1.getString("FSTOCKBASEQTY");//库存基本数量
                        String FSTOCKQTY = jsonObject1.getString("FSTOCKQTY"); //FSTOCKQTY 库存数量
                        String FPURCHASERID = jsonObject1.getString("FPURCHASERID");//FPURCHASERID 采购员
                        String FDATE = jsonObject1.getString("FDATE");//FDATE 采购日期

                        list.add(FBASEUNITQTY);
                        list.add(FSTOCKBASEQTY);
                        list.add(FSTOCKQTY);
                        list.add(FPURCHASERID);
                        list.add(FDATE);

                        resultList.add(list);
                    }
//                    System.out.println("Query==="+resultList);
                } else if (deal.equals("Query2")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String FNAME = jsonObject1.getString("FNAME");//员工名称

                        list.add(FNAME);

                        resultList.add(list);
                    }
//                        System.out.println("Query2==="+resultList);
                } else if (deal.equals("Query3")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String FQTY = jsonObject1.getString("FQTY");//基本单位数量(在制)
                        String FVERNUMBER = jsonObject1.getString("FVERNUMBER");//版本编码
                        String FMATNUMBER = jsonObject1.getString("FMATNUMBER");//物料编码

                        list.add(FQTY);
                        list.add(FVERNUMBER);
                        list.add(FMATNUMBER);

                        resultList.add(list);
                    }
//                        System.out.println("Query3==="+resultList);
                } else if (deal.equals("QueryAll")) {
                    List list = new ArrayList();
                    JSONObject jsonObject1 = table.getJSONObject(0);

                    String FENTRYID = jsonObject1.getString("FENTRYID");

                    list.add(FENTRYID);

                    resultList.add(list);
//                    System.out.println("QueryAll==="+resultList);
                } else if (deal.equals("QueryisBOM")) {
                    List list = new ArrayList();
                    //存在返回1
                    list.add("1");

                    resultList.add(list);
                } else if (deal.equals("QueryBOMFid")) {
                    List list = new ArrayList();
                    JSONObject jsonObject1 = table.getJSONObject(0);
                    String FID = jsonObject1.getString("FID");
                    list.add(FID);

                    resultList.add(list);
//                    System.out.println("QueryBOMFid==="+resultList);
                } else if (deal.equals("QueryNewPriceCategory")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String FCREATEORGID = jsonObject1.getString("FCREATEORGID");//采购组织
                        String FApproveDate = jsonObject1.getString("FApproveDate");//审核日期
                        String FAUXPTYNUMBER = jsonObject1.getString("FAUXPTYNUMBER");//版本
                        String F_APPO_MINPOQTY = jsonObject1.getString("F_APPO_MINPOQTY");//最小订货量
                        String F_APPO_INCREASEQTY = jsonObject1.getString("F_APPO_INCREASEQTY");//最小包装量
                        String F_APPO_FIXLEADTIME = jsonObject1.getString("F_APPO_FIXLEADTIME");//固定提前期
                        String FTaxPrice = jsonObject1.getString("FTaxPrice");//含税单价
                        String FCURRENCYID = jsonObject1.getString("FCURRENCYID");//币别

                        list.add(FCREATEORGID);
                        list.add(FApproveDate);
                        list.add(FAUXPTYNUMBER);
                        list.add(F_APPO_MINPOQTY);
                        list.add(F_APPO_INCREASEQTY);
                        list.add(F_APPO_FIXLEADTIME);
                        list.add(FTaxPrice);
                        list.add(FCURRENCYID);

                        resultList.add(list);
                    }
//                        System.out.println("QueryNewPriceCategory==="+resultList);
                } else if (deal.equals("QueryFpkName")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String QueryFpkName = jsonObject1.getString("FNAME");//币别名称

                        list.add(QueryFpkName);

                        resultList.add(list);
                    }
//                        System.out.println("QueryFpkName===" + resultList);
                } else if (deal.equals("QueryNewPoorder")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String FRemainStockINQty = jsonObject1.getString("fremainstockinqty");//剩余入库数量（在途）

                        list.add(FRemainStockINQty);

                        resultList.add(list);
                    }
//                    System.out.println("QueryNewPoorder====" + resultList);
                } else if (deal.equals("QueryNewiQuantity")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String fbaseqty = jsonObject1.getString("fbaseqty");//库存量(基本单位)

                        list.add(fbaseqty);

                        resultList.add(list);
                    }
//                    System.out.println("QueryNewiQuantity:" + resultList);
                } else if (deal.equals("QueryFversion")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String FAUXPTYNUMBER = jsonObject1.getString("FAUXPTYNUMBER");//版本

                        list.add(FAUXPTYNUMBER);

                        resultList.add(list);
                    }
//                    System.out.println("QueryFversion:" + resultList);
                } else if (deal.equals("QueryIqty")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String iqty = jsonObject1.getString("iqty");//库存和

                        list.add(iqty);

                        resultList.add(list);
                    }
                } else if (deal.equals("QueryPoqty")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String poqty = jsonObject1.getString("poqty");//在途和

                        list.add(poqty);

                        resultList.add(list);
                    }
                } else if (deal.equals("QueryFqty")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String fqty = jsonObject1.getString("fqty");//在制和

                        list.add(fqty);

                        resultList.add(list);
                    }
                } else if (deal.equals("Query2Materiel")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String fmaterialid = jsonObject1.getString("FMATERIALID");//物料内码
                        String cFlag = jsonObject1.getString("F_APPO_ZT");//物料状态
                        String fplanerid = jsonObject1.getString("FPLANERID");//计划员
                        String fpurchaserid = jsonObject1.getString("FPURCHASERID");//采购员
                        String fuseorgid = jsonObject1.getString("FUSEORGID");//使用组织

                        list.add(fmaterialid);
                        list.add(cFlag);
                        list.add(fplanerid);
                        list.add(fpurchaserid);
                        list.add(fuseorgid);

                        resultList.add(list);
                    }
                } else if (deal.equals("Query2All")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String iqty = jsonObject1.getString("iqty");//同一编码，版本库存和
                        String fqty = jsonObject1.getString("fqty");//同一编码，版本在制和
                        String poqty = jsonObject1.getString("poqty");//同一编码，版本在途和
                        String itemnum = jsonObject1.getString("itemnum");//物料编码
                        String itemver = jsonObject1.getString("itemver");//版本

                        list.add(iqty);
                        list.add(fqty);
                        list.add(poqty);
                        list.add(itemnum);
                        list.add(itemver);

                        resultList.add(list);
                    }
                } else if (deal.equals("Query2AllCount")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String count = jsonObject1.getString("count");//库存,在制，在途表拼接总数

                        list.add(count);

                        resultList.add(list);
                    }
                } else if (deal.equals("QueryNewFqty")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String fqty = jsonObject1.getString("fqty");//在制和
                        String fmatnumber = jsonObject1.getString("FMATNUMBER");//物料编码
                        String fvernumber = jsonObject1.getString("FVERNUMBER");//版本
                        String fplanername= jsonObject1.getString("FPLANERNAME");//计划员名称
                        String fpurchasername= jsonObject1.getString("FPURCHASERNAME");//采购员名称

                        list.add(fqty);
                        list.add(fmatnumber);
                        list.add(fvernumber);
                        list.add(fplanername);
                        list.add(fpurchasername);

                        resultList.add(list);
                    }
                } else if (deal.equals("QueryNewIqty")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String iqty = jsonObject1.getString("iqty");//库存和
                        String itemnum = jsonObject1.getString("ItemNum");//物料编码
                        String itemver = jsonObject1.getString("ITEMVER");//版本
                        String onum = jsonObject1.getString("ONum");//库存组织ID
                        String oname = jsonObject1.getString("OName");//库存组织名称
                        String fplanername= jsonObject1.getString("FPLANERNAME");//计划员名称
                        String fpurchasername= jsonObject1.getString("FPURCHASERNAME");//采购员名称

                        list.add(iqty);
                        list.add(itemnum);
                        list.add(itemver);
                        list.add(onum);
                        list.add(oname);
                        list.add(fplanername);
                        list.add(fpurchasername);

                        resultList.add(list);
                    }
                } else if (deal.equals("QueryNewPoqty")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String poqty = jsonObject1.getString("poqty");//在途和
                        String itemnum = jsonObject1.getString("ItemNum");//物料编码
                        String itemver = jsonObject1.getString("ITEMVER");//版本
                        String onum = jsonObject1.getString("ONum");//库存组织ID
                        String oname = jsonObject1.getString("OName");//库存组织名称
                        String fplanername= jsonObject1.getString("FPLANERNAME");//计划员名称
                        String fpurchasername= jsonObject1.getString("FPURCHASERNAME");//采购员名称

                        list.add(poqty);
                        list.add(itemnum);
                        list.add(itemver);
                        list.add(onum);
                        list.add(oname);
                        list.add(fplanername);
                        list.add(fpurchasername);

                        resultList.add(list);
                    }
                } else if (deal.equals("QueryNewPoqtyCount")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String count = jsonObject1.getString("count");//在途和 总数

                        list.add(count);

                        resultList.add(list);
                    }
                } else if (deal.equals("QueryNewFqtyCount")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String count = jsonObject1.getString("count");//在制和 总数

                        list.add(count);

                        resultList.add(list);
                    }
                } else if (deal.equals("QueryNewIqtyCount")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String count = jsonObject1.getString("count");//在库存和 总数

                        list.add(count);

                        resultList.add(list);
                    }
                } else if (deal.equals("QueryNewPriceCategoryCount")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String count = jsonObject1.getString("count");//查询同一物料编码，版本下最新审核日期的最最低价格 价目表总数

                        list.add(count);

                        resultList.add(list);
                    }
                } else if (deal.equals("QueryNewPriceCategory2")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String FCREATEORGID = jsonObject1.getString("FCREATEORGID");//采购组织ID
                        String FApproveDate = jsonObject1.getString("FApproveDate");//审核日期
                        String FAUXPTYNUMBER = jsonObject1.getString("FAUXPTYNUMBER");//版本
                        String F_APPO_MINPOQTY = jsonObject1.getString("F_APPO_MINPOQTY");//最小订货量
                        String F_APPO_INCREASEQTY = jsonObject1.getString("F_APPO_INCREASEQTY");//最小包装量
                        String F_APPO_FIXLEADTIME = jsonObject1.getString("F_APPO_FIXLEADTIME");//固定提前期
                        String FTaxPrice = jsonObject1.getString("FTaxPrice");//含税单价
                        String FNUMBER = jsonObject1.getString("FNUMBER");//物料编码
                        String FORGNUMBER= jsonObject1.getString("FORGNUMBER");//组织编码(02光峰，20 峰米...)
                        String FORGNAME=jsonObject1.getString("FORGNAME");//组织名称
                        String fplanername= jsonObject1.getString("fplanername");//计划员名称
                        String fpurchasername= jsonObject1.getString("fpurchasername");//采购员名称
                        String fcurrencynane= jsonObject1.getString("fcurrencynane");//币别名称


                        list.add(FCREATEORGID);
                        list.add(FApproveDate);
                        list.add(FAUXPTYNUMBER);
                        list.add(F_APPO_MINPOQTY);
                        list.add(F_APPO_INCREASEQTY);
                        list.add(F_APPO_FIXLEADTIME);
                        list.add(FTaxPrice);
                        list.add(FNUMBER);
                        list.add(FORGNUMBER);
                        list.add(FORGNAME);
                        list.add(fplanername);
                        list.add(fpurchasername);
                        list.add(fcurrencynane);

                        resultList.add(list);
                    }
                } else if (deal.equals("QueryOrg")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String fnumber = jsonObject1.getString("FNUMBER");//组织编码
                        String fname=jsonObject1.getString("FNUMBER");//组织名称

                        list.add(fnumber);
                        list.add(fname);

                        resultList.add(list);
                    }
                }else if(deal.equals("QueryAllMateriel")){
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String fmaterialid = jsonObject1.getString("FMATERIALID");//物料内码
                        String fplanerid = jsonObject1.getString("FPLANERID");//计划员
                        String fpurchaserid = jsonObject1.getString("FPURCHASERID");//采购员
                        String fuseorgid = jsonObject1.getString("FUSEORGID");//使用组织
                        String fnumber = jsonObject1.getString("FNUMBER");//物料编码
                        String onum = jsonObject1.getString("ONum");//组织编码(02 光峰，20 峰米)
                        String oname = jsonObject1.getString("OName");//组织名称
                        String fplanername = jsonObject1.getString("FPLANERNAME");//计划员名称
                        String fpurchasername = jsonObject1.getString("FPURCHASERNAME");//采购员名称

                        list.add(fmaterialid);
                        list.add(fplanerid);
                        list.add(fpurchaserid);
                        list.add(fuseorgid);
                        list.add(fnumber);
                        list.add(onum);
                        list.add(oname);
                        list.add(fplanername);
                        list.add(fpurchasername);

                        resultList.add(list);
                    }
                }else if (deal.equals("QueryAllMaterielCount")) {
                    for (int i = 0; i < table.length(); i++) {
                        List list = new ArrayList();
                        JSONObject jsonObject1 = table.getJSONObject(i);

                        String count = jsonObject1.getString("count");//查询所有物料总数

                        list.add(count);

                        resultList.add(list);
                    }
                }else {
                    System.out.println("查询失败");
//                  System.out.println("失败原因："+sResult);
                    return null;
                }
            }
        }
//        System.out.println(" ============================= ");
//        System.out.println(" Contents of post request ends ");
//        System.out.println(" ============================= ");
        reader.close();

        connectionInvoke.disconnect();

        return  resultList;
    }

    /**
     * 把中文转成Unicode码
     *
     * @param str
     * @return
     */
    public static String chinaToUnicode(String str) {
        String result = "";
        for (int i = 0; i < str.length(); i++) {
            int chr1 = (char) str.charAt(i);
            if (chr1 >= 19968 && chr1 <= 171941) {// 汉字范围 \u4e00-\u9fa5 (中文)
                result += "\\u" + Integer.toHexString(chr1);
            } else {
                result += str.charAt(i);
            }
        }
        return result;
    }

    // Login
    public static boolean Login(String dbId, String user, String pwd, int lang)
            throws Exception {

        boolean bResult = false;

        String sUrl = "Kingdee.BOS.WebApi.ServicesStub.AuthService.ValidateUser.common.kdsvc";

        JSONArray jParas = new JSONArray();
        jParas.put(dbId);// 帐套Id
        jParas.put(user);// 用户名
        jParas.put(pwd);// 密码
        jParas.put(lang);// 语言

        HttpURLConnection connection = initUrlConn(sUrl, jParas.toString());
        // 获取Cookie
        String key = null;
        for (int i = 1; (key = connection.getHeaderFieldKey(i)) != null; i++) {
            if (key.equalsIgnoreCase("Set-Cookie")) {
                String tempCookieVal = connection.getHeaderField(i);
                if (tempCookieVal.startsWith("kdservice-sessionid")) {
                    CookieVal = tempCookieVal;
                    break;
                }
            }
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));
        String line;
//        System.out.println(" ============================= ");
//        System.out.println(" Contents of post request ");
//        System.out.println(" ============================= ");
        while ((line = reader.readLine()) != null) {
//            String sResult = new String(line.getBytes(), "utf-8");
//            System.out.println(sResult);
            bResult = line.contains("\"LoginResultType\":1");
        }
//        System.out.println(" ============================= ");
//        System.out.println(" Contents of post request ends ");
//        System.out.println(" ============================= ");
        reader.close();

        connection.disconnect();

        return bResult;
    }

}
