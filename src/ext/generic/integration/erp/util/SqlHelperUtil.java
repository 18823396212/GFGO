package ext.generic.integration.erp.util; /**
 * 调用金蝶接口查询数据
 */

import com.sun.xml.rpc.processor.modeler.j2ee.xml.string;
import ext.appo.erp.util.ReadProperty;
import ext.generic.integration.erp.util.InvokeHelper;
import kingdee.bos.webapi.client.K3CloudApiClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlHelperUtil {

//    static String K3CloudURL = "https://appo.test.ik3cloud.com/K3cloud/";
//    static String dbId = "20190329100129";
//    static String uid = "PLM1";
//    static String pwd = "abcd1234!@#$";
//    static int lang = 2052;

//    static String K3CloudURL = "http://10.32.252.99/k3cloud/";
//    static String dbId = "5ccf99667c66f9";
//    static String uid = "PLM1";
//    static String pwd = "abcd1234!@#$";
//    static int lang = 2052;

    static String K3CloudURL = "";
    static String dbId = "";
    static String uid = "";
    static String pwd = "";
    static int lang ;


    static {
        try {
            Map<String,String> map=ReadProperty.ReadProperty();
            K3CloudURL=map.get("K3CloudURL");
            dbId = map.get("dbId");
            uid = map.get("uid");
            pwd = map.get("pwd");
            lang = Integer.parseInt(map.get("lang"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    //通过物料编码List查询物料表返回物料状态,使用组织,编码,物料内码
    //FNumber 编码，FUseOrgId 使用组织 100146 峰米 1 光峰，FDocumentStatus 数据状态（C 已审核），FMATERIALID 物料内码
    public static List<List<Object>> queryPartsList(List partsList,String copanyId) throws Exception {
        K3CloudApiClient client = new K3CloudApiClient(K3CloudURL);
        Boolean result = client.login(dbId, uid, pwd, lang);
        if (result) {
            String partStr="";
            if (partsList!=null&&partsList.size()>0){
                for (int i = 0; i <partsList.size() ; i++) {
                    if (i==partsList.size()-1){
                        partStr+="'"+partsList.get(i)+"'";
                    }else{
                        partStr+="'"+partsList.get(i)+"',";
                    }
                }
//                System.out.println("最后的partStr==="+partStr);
                String sContent = "{\"FormId\":\"BD_MATERIAL\"," +// 物料formid
                        "\"FilterString\":\"FNumber in ("+partStr+") and FUseOrgId='"+copanyId+"'\","+// 过滤条件
                        "\"FieldKeys\":\"FNumber,FDocumentStatus,FUseOrgId,FMATERIALID\"}";
                System.out.println("sContent==="+sContent);
                List<List<Object>> sResult = client.executeBillQuery(sContent);
                System.out.println("sResult==="+sResult);
                return  sResult;
            }

        }else {
            throw  new RuntimeException("ERP连接失败，请联系管理员！");
        }
        return null;
    }


    //查询物料表总数
    public static List<List<Object>> queryAllMaterielCount() throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {

            String sql="SELECT COUNT(*) AS count\n" +
                    "FROM (\n" +
                    "\tSELECT a.FMATERIALID, b.FPLANERID, c.FPURCHASERID, a.FUSEORGID, a.FNUMBER\n" +
                    "\tFROM T_BD_MATERIAL a\n" +
                    "\tLEFT JOIN t_BD_MaterialPlan b ON a.FMATERIALID = b.FMATERIALID \n" +
                    "\t\tLEFT JOIN t_bd_MaterialPurchase c ON a.FMATERIALID = c.FMATERIALID \n" +
                    ") d";
            List<List<Object>> sResult = InvokeHelper.QueryAllMaterielCount(sql);
            return sResult;
        }
        return null;
    }

    //物料信息
    //FMATERIALID 物料内码
    //FPLANERID 计划员
    //FPURCHASERID 采购员
    //FUSEORGID 使用组织
    //FNUMBER 物料编码
    //ONum 组织编码(02 光峰，20 峰米)
    //OName 组织名称
    //FPLANERNAME 计划员名称
    //FPURCHASERNAME 采购员名称
    public static List<List<Object>> queryAllMateriel(String begin,String end) throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {

            String sql="SELECT h.FMATERIALID, h.FPLANERID, h.FPURCHASERID, h.FUSEORGID, h.FNUMBER\n" +
                    "\t, h.ONum, h.OName, h.FPLANERNAME, h.FPURCHASERNAME\n" +
                    "FROM (\n" +
                    "\tSELECT d.FMATERIALID, d.FPLANERID, d.FPURCHASERID, d.FUSEORGID, d.FNUMBER\n" +
                    "\t\t, e.FNUMBER AS ONum, e.FNAME AS OName, f.FNAME AS FPLANERNAME, g.FNAME AS FPURCHASERNAME\n" +
                    "\tFROM (\n" +
                    "\t\tSELECT a.FMATERIALID, b.FPLANERID, c.FPURCHASERID, a.FUSEORGID, a.FNUMBER\n" +
                    "\t\tFROM T_BD_MATERIAL a\n" +
                    "\t\tLEFT JOIN t_BD_MaterialPlan b ON a.FMATERIALID = b.FMATERIALID \n" +
                    "\t\t\tLEFT JOIN t_bd_MaterialPurchase c ON a.FMATERIALID = c.FMATERIALID \n" +
                    "\t) d\n" +
                    "\tLEFT JOIN (\n" +
                    "\t\tSELECT a.FORGID, a.FNUMBER, b.FNAME\n" +
                    "\t\tFROM T_ORG_Organizations a\n" +
                    "\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\tSELECT FORGID, FNAME\n" +
                    "\t\t\t\tFROM T_ORG_Organizations_L\n" +
                    "\t\t\t\tWHERE FLOCALEID = '2052'\n" +
                    "\t\t\t) b ON a.FORGID = b.FORGID \n" +
                    "\t) e ON d.FUSEORGID = e.FORGID \n" +
                    "\tLEFT JOIN (\n" +
                    "\t\tSELECT b.FNAME, a.FENTRYID\n" +
                    "\t\tFROM T_BD_OPERATORENTRY a\n" +
                    "\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID \n" +
                    "\t) f ON d.FPLANERID = f.FENTRYID \n" +
                    "\t\tLEFT JOIN (\n" +
                    "\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                    "\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                    "\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID \n" +
                    "\t\t) g ON d.FPURCHASERID = g.FENTRYID \n" +
                    ") h";
            if (!begin.isEmpty()&&!end.isEmpty()) {
                sql="SELECT *\n" +
                        "FROM (\n" +
                        "\tSELECT ROW_NUMBER() OVER (ORDER BY getdate()) AS init, *\n" +
                        "\tFROM (\n" +
                        "\t\tSELECT h.FMATERIALID, h.FPLANERID, h.FPURCHASERID, h.FUSEORGID, h.FNUMBER\n" +
                        "\t\t\t, h.ONum, h.OName, h.FPLANERNAME, h.FPURCHASERNAME\n" +
                        "\t\tFROM (\n" +
                        "\t\t\tSELECT d.FMATERIALID, d.FPLANERID, d.FPURCHASERID, d.FUSEORGID, d.FNUMBER\n" +
                        "\t\t\t\t, e.FNUMBER AS ONum, e.FNAME AS OName, f.FNAME AS FPLANERNAME, g.FNAME AS FPURCHASERNAME\n" +
                        "\t\t\tFROM (\n" +
                        "\t\t\t\tSELECT a.FMATERIALID, b.FPLANERID, c.FPURCHASERID, a.FUSEORGID, a.FNUMBER\n" +
                        "\t\t\t\tFROM T_BD_MATERIAL a\n" +
                        "\t\t\t\tLEFT JOIN t_BD_MaterialPlan b ON a.FMATERIALID = b.FMATERIALID \n" +
                        "\t\t\t\t\tLEFT JOIN t_bd_MaterialPurchase c ON a.FMATERIALID = c.FMATERIALID \n" +
                        "\t\t\t) d\n" +
                        "\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\tSELECT a.FORGID, a.FNUMBER, b.FNAME\n" +
                        "\t\t\t\tFROM T_ORG_Organizations a\n" +
                        "\t\t\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\t\t\tSELECT FORGID, FNAME\n" +
                        "\t\t\t\t\t\tFROM T_ORG_Organizations_L\n" +
                        "\t\t\t\t\t\tWHERE FLOCALEID = '2052'\n" +
                        "\t\t\t\t\t) b ON a.FORGID = b.FORGID \n" +
                        "\t\t\t) e ON d.FUSEORGID = e.FORGID \n" +
                        "\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                        "\t\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                        "\t\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID \n" +
                        "\t\t\t) f ON d.FPLANERID = f.FENTRYID \n" +
                        "\t\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                        "\t\t\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                        "\t\t\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID \n" +
                        "\t\t\t\t) g ON d.FPURCHASERID = g.FENTRYID \n" +
                        "\t\t) h\n" +
                        "\t) i\n" +
                        ") j" +
                        "WHERE j.init BETWEEN "+begin+" AND "+end;
            }

            List<List<Object>> sResult = InvokeHelper.QueryAllMateriel(sql);
            return sResult;
        }
        return null;
    }

    //T_ORG_Organizations 组织机构表
    //FORGID 组织ID
    //FNUMBER 组织编码
    //FNAME 组织名称
    //通过组织ID查询组织编码,组织名称（{"FORGID": "1","FNUMBER": "02"} 光峰；{"FORGID": "100146","FNUMBER": "20"} 峰米）
    public static List<List<Object>> queryOrg(String forgid) throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
            String sql="SELECT FNUMBER, FNAME\n" +
                    "FROM (\n" +
                    "\tSELECT a.FORGID, a.FNUMBER, b.FNAME\n" +
                    "\tFROM T_ORG_Organizations a\n" +
                    "\t\tLEFT JOIN (\n" +
                    "\t\t\tSELECT FORGID, FNAME\n" +
                    "\t\t\tFROM T_ORG_Organizations_L\n" +
                    "\t\t\tWHERE FLOCALEID = '2052'\n" +
                    "\t\t) b\n" +
                    "\t\tON a.FORGID = b.FORGID\n" +
                    ") c\n" +
                    "WHERE FORGID = '"+forgid+"'";
            List<List<Object>> sResult = InvokeHelper.QueryOrg(sql);
            return sResult;
        }
        return  null;
    }



    //查询处理后物料表，价目表拼接视图，查询结果
    //init 自增长字段
    //FCREATEORGID 采购组织(1为光峰，100146为峰米)
    //FApproveDate 审核日期
    //FAUXPTYNUMBER 版本
    //F_APPO_MINPOQTY 最小订货量
    //F_APPO_INCREASEQTY 最小包装量
    //F_APPO_FIXLEADTIME 固定提前期
    //FTaxPrice 含税单价
    //FCURRENCYID 币别
    //FEXPIRYDATE 失效日期
    //FMATERIALID 物料内码
    //FNUMBER 物料编码
    //查询同一物料编码，版本下最新审核日期的最最低价格 价目表
    public static List<List<Object>> queryNewPriceCategory(String begin,String end) throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
            String sql="SELECT *\n" +
                    "FROM (\n" +
                    "\tSELECT n.FCREATEORGID, n.FApproveDate, n.FAUXPTYNUMBER, n.F_APPO_MINPOQTY, n.F_APPO_INCREASEQTY\n" +
                    "\t\t, n.F_APPO_FIXLEADTIME, n.FTaxPrice, n.FCURRENCYID, n.FEXPIRYDATE, n.FMATERIALID\n" +
                    "\t\t, n.FNUMBER, n.FPLANERID, n.FPURCHASERID, n.FORGNUMBER, n.FORGNAME\n" +
                    "\t\t, q.FNAME AS FPLANERNAME, r.FNAME AS FPURCHASERNAME, s.FNAME AS FCURRENCYNANE\n" +
                    "\tFROM (\n" +
                    "\t\tSELECT k.FCREATEORGID, k.FApproveDate, k.FAUXPTYNUMBER, k.F_APPO_MINPOQTY, k.F_APPO_INCREASEQTY\n" +
                    "\t\t\t, k.F_APPO_FIXLEADTIME, k.FTaxPrice, k.FCURRENCYID, k.FEXPIRYDATE, k.FMATERIALID\n" +
                    "\t\t\t, m.FNUMBER, m.FPLANERID, m.FPURCHASERID, p.FNUMBER AS FORGNUMBER, p.FName AS FORGNAME\n" +
                    "\t\tFROM (\n" +
                    "\t\t\tSELECT j.FCREATEORGID, j.FAUXPTYNUMBER, j.FMATERIALID, j.RatePrice, i.FApproveDate\n" +
                    "\t\t\t\t, i.F_APPO_MINPOQTY, i.F_APPO_INCREASEQTY, i.F_APPO_FIXLEADTIME, i.FTaxPrice, i.FCURRENCYID\n" +
                    "\t\t\t\t, i.FEXPIRYDATE, i.FEXCHANGERATE\n" +
                    "\t\t\tFROM (\n" +
                    "\t\t\t\tSELECT i.FCREATEORGID, i.FAUXPTYNUMBER, i.FMATERIALID, MIN(i.RatePrice) AS RatePrice\n" +
                    "\t\t\t\tFROM (\n" +
                    "\t\t\t\t\tSELECT h.FCREATEORGID, h.FApproveDate,isNull(h.FAUXPTYNUMBER,'empty') as FAUXPTYNUMBER, h.F_APPO_MINPOQTY, h.F_APPO_INCREASEQTY\n" +
                    "\t\t\t\t\t\t, h.F_APPO_FIXLEADTIME, h.FTaxPrice, h.FCURRENCYID, h.FEXPIRYDATE, h.FMATERIALID\n" +
                    "\t\t\t\t\t\t, h.FEXCHANGERATE, FTaxPrice * FEXCHANGERATE AS RatePrice\n" +
                    "\t\t\t\t\tFROM (\n" +
                    "\t\t\t\t\t\tSELECT f.FCREATEORGID, f.FApproveDate, f.FAUXPTYNUMBER, f.F_APPO_MINPOQTY, f.F_APPO_INCREASEQTY\n" +
                    "\t\t\t\t\t\t\t, f.F_APPO_FIXLEADTIME, f.FTaxPrice, f.FCURRENCYID, f.FEXPIRYDATE, f.FMATERIALID\n" +
                    "\t\t\t\t\t\t\t, isNull(g.FEXCHANGERATE, 1) AS FEXCHANGERATE\n" +
                    "\t\t\t\t\t\tFROM (\n" +
                    "\t\t\t\t\t\t\tSELECT b.FCREATEORGID, b.FApproveDate, d.FAUXPTYNUMBER, a.F_APPO_MINPOQTY, a.F_APPO_INCREASEQTY\n" +
                    "\t\t\t\t\t\t\t\t, a.F_APPO_FIXLEADTIME, a.FTaxPrice, b.FCURRENCYID, a.FEXPIRYDATE, a.FMATERIALID\n" +
                    "\t\t\t\t\t\t\tFROM t_PUR_PriceListEntry a\n" +
                    "\t\t\t\t\t\t\t\tLEFT JOIN t_PUR_PriceList b ON a.fid = b.fid\n" +
                    "\t\t\t\t\t\t\t\tLEFT JOIN T_BD_FLEXSITEMDETAILV c ON a.FAUXPROPID = c.fid\n" +
                    "\t\t\t\t\t\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\t\t\t\t\t\tSELECT *\n" +
                    "\t\t\t\t\t\t\t\t\tFROM T_BD_AUXPTYVALUEENTITY\n" +
                    "\t\t\t\t\t\t\t\t\tWHERE FENTRYID IN (\n" +
                    "\t\t\t\t\t\t\t\t\t\tSELECT MIN(FENTRYID)\n" +
                    "\t\t\t\t\t\t\t\t\t\tFROM T_BD_AUXPTYVALUEENTITY\n" +
                    "\t\t\t\t\t\t\t\t\t\tGROUP BY FAUXPTYID\n" +
                    "\t\t\t\t\t\t\t\t\t)\n" +
                    "\t\t\t\t\t\t\t\t) d\n" +
                    "\t\t\t\t\t\t\t\tON c.ff100001 = d.FAUXPTYID\n" +
                    "\t\t\t\t\t\t\tWHERE getdate() < a.FEXPIRYDATE\n" +
                    "\t\t\t\t\t\t\t\tAND b.FDOCUMENTSTATUS = 'C'\n" +
                    "\t\t\t\t\t\t) f\n" +
                    "\t\t\t\t\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\t\t\t\t\tSELECT *\n" +
                    "\t\t\t\t\t\t\t\tFROM t_bd_rate\n" +
                    "\t\t\t\t\t\t\t\tWHERE (FRATETYPEID = '1'\n" +
                    "\t\t\t\t\t\t\t\t\tAND FCYTOID = '1'\n" +
                    "\t\t\t\t\t\t\t\t\tAND getdate() < FENDDATE)\n" +
                    "\t\t\t\t\t\t\t) g\n" +
                    "\t\t\t\t\t\t\tON f.FCURRENCYID = g.FCYFORID\n" +
                    "\t\t\t\t\t) h\n" +
                    "\t\t\t\t) i\n" +
                    "\t\t\t\tGROUP BY FCREATEORGID, FMATERIALID, FAUXPTYNUMBER\n" +
                    "\t\t\t) j\n" +
                    "\t\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\t\tSELECT h.FCREATEORGID, h.FApproveDate, isNull(h.FAUXPTYNUMBER,'empty') as FAUXPTYNUMBER, h.F_APPO_MINPOQTY, h.F_APPO_INCREASEQTY\n" +
                    "\t\t\t\t\t\t, h.F_APPO_FIXLEADTIME, h.FTaxPrice, h.FCURRENCYID, h.FEXPIRYDATE, h.FMATERIALID\n" +
                    "\t\t\t\t\t\t, h.FEXCHANGERATE, FTaxPrice * FEXCHANGERATE AS RatePrice\n" +
                    "\t\t\t\t\tFROM (\n" +
                    "\t\t\t\t\t\tSELECT f.FCREATEORGID, f.FApproveDate, f.FAUXPTYNUMBER, f.F_APPO_MINPOQTY, f.F_APPO_INCREASEQTY\n" +
                    "\t\t\t\t\t\t\t, f.F_APPO_FIXLEADTIME, f.FTaxPrice, f.FCURRENCYID, f.FEXPIRYDATE, f.FMATERIALID\n" +
                    "\t\t\t\t\t\t\t, isNull(g.FEXCHANGERATE, 1) AS FEXCHANGERATE\n" +
                    "\t\t\t\t\t\tFROM (\n" +
                    "\t\t\t\t\t\t\tSELECT b.FCREATEORGID, b.FApproveDate, d.FAUXPTYNUMBER, a.F_APPO_MINPOQTY, a.F_APPO_INCREASEQTY\n" +
                    "\t\t\t\t\t\t\t\t, a.F_APPO_FIXLEADTIME, a.FTaxPrice, b.FCURRENCYID, a.FEXPIRYDATE, a.FMATERIALID\n" +
                    "\t\t\t\t\t\t\tFROM t_PUR_PriceListEntry a\n" +
                    "\t\t\t\t\t\t\t\tLEFT JOIN t_PUR_PriceList b ON a.fid = b.fid\n" +
                    "\t\t\t\t\t\t\t\tLEFT JOIN T_BD_FLEXSITEMDETAILV c ON a.FAUXPROPID = c.fid\n" +
                    "\t\t\t\t\t\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\t\t\t\t\t\tSELECT *\n" +
                    "\t\t\t\t\t\t\t\t\tFROM T_BD_AUXPTYVALUEENTITY\n" +
                    "\t\t\t\t\t\t\t\t\tWHERE FENTRYID IN (\n" +
                    "\t\t\t\t\t\t\t\t\t\tSELECT MIN(FENTRYID)\n" +
                    "\t\t\t\t\t\t\t\t\t\tFROM T_BD_AUXPTYVALUEENTITY\n" +
                    "\t\t\t\t\t\t\t\t\t\tGROUP BY FAUXPTYID\n" +
                    "\t\t\t\t\t\t\t\t\t)\n" +
                    "\t\t\t\t\t\t\t\t) d\n" +
                    "\t\t\t\t\t\t\t\tON c.ff100001 = d.FAUXPTYID\n" +
                    "\t\t\t\t\t\t\tWHERE getdate() < a.FEXPIRYDATE\n" +
                    "\t\t\t\t\t\t\t\tAND b.FDOCUMENTSTATUS = 'C'\n" +
                    "\t\t\t\t\t\t) f\n" +
                    "\t\t\t\t\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\t\t\t\t\tSELECT *\n" +
                    "\t\t\t\t\t\t\t\tFROM t_bd_rate\n" +
                    "\t\t\t\t\t\t\t\tWHERE (FRATETYPEID = '1'\n" +
                    "\t\t\t\t\t\t\t\t\tAND FCYTOID = '1'\n" +
                    "\t\t\t\t\t\t\t\t\tAND getdate() < FENDDATE)\n" +
                    "\t\t\t\t\t\t\t) g\n" +
                    "\t\t\t\t\t\t\tON f.FCURRENCYID = g.FCYFORID\n" +
                    "\t\t\t\t\t) h\n" +
                    "\t\t\t\t) i\n" +
                    "\t\t\t\tON (j.RatePrice = i.RatePrice\n" +
                    "\t\t\t\t\tAND j.FCREATEORGID = i.FCREATEORGID\n" +
                    "\t\t\t\t\tAND j.FAUXPTYNUMBER = i.FAUXPTYNUMBER\n" +
                    "\t\t\t\t\tAND j.FMATERIALID = i.FMATERIALID)\n" +
                    "\t\t) k\n" +
                    "\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\tSELECT a.FMATERIALID, a.FNUMBER, b.FPLANERID, c.FPURCHASERID, a.FUSEORGID\n" +
                    "\t\t\t\tFROM T_BD_MATERIAL a\n" +
                    "\t\t\t\t\tLEFT JOIN t_BD_MaterialPlan b ON a.FMATERIALID = b.FMATERIALID\n" +
                    "\t\t\t\t\tLEFT JOIN t_bd_MaterialPurchase c ON a.FMATERIALID = c.FMATERIALID\n" +
                    "\t\t\t) m\n" +
                    "\t\t\tON k.FMATERIALID = m.FMATERIALID\n" +
                    "\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\tSELECT a.FORGID, a.FNUMBER, b.FNAME\n" +
                    "\t\t\t\tFROM T_ORG_Organizations a\n" +
                    "\t\t\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\t\t\tSELECT FORGID, FNAME\n" +
                    "\t\t\t\t\t\tFROM T_ORG_Organizations_L\n" +
                    "\t\t\t\t\t\tWHERE FLOCALEID = '2052'\n" +
                    "\t\t\t\t\t) b\n" +
                    "\t\t\t\t\tON a.FORGID = b.FORGID\n" +
                    "\t\t\t) p\n" +
                    "\t\t\tON k.FCREATEORGID = p.FORGID\n" +
                    "\t) n\n" +
                    "\t\tLEFT JOIN (\n" +
                    "\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                    "\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                    "\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID\n" +
                    "\t\t) q\n" +
                    "\t\tON n.FPLANERID = q.FENTRYID\n" +
                    "\t\tLEFT JOIN (\n" +
                    "\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                    "\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                    "\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID\n" +
                    "\t\t) r\n" +
                    "\t\tON n.FPURCHASERID = r.FENTRYID\n" +
                    "\t\tLEFT JOIN (\n" +
                    "\t\t\tSELECT FNAME, FCURRENCYID, FLOCALEID\n" +
                    "\t\t\tFROM t_bd_currency_l\n" +
                    "\t\t\tWHERE FLOCALEID = '2052'\n" +
                    "\t\t) s\n" +
                    "\t\tON n.FCURRENCYID = s.FCURRENCYID\n" +
                    ") u";
            if (!begin.isEmpty()&&!end.isEmpty()) {
                sql = "SELECT *\n" +
                        "FROM (\n" +
                        "\tSELECT ROW_NUMBER() OVER (ORDER BY getdate()) AS init, *\n" +
                        "\tFROM (\n" +
                        "\t\tSELECT n.FCREATEORGID, n.FApproveDate, n.FAUXPTYNUMBER, n.F_APPO_MINPOQTY, n.F_APPO_INCREASEQTY\n" +
                        "\t\t\t, n.F_APPO_FIXLEADTIME, n.FTaxPrice, n.FCURRENCYID, n.FEXPIRYDATE, n.FMATERIALID\n" +
                        "\t\t\t, n.FNUMBER, n.FPLANERID, n.FPURCHASERID, n.FORGNUMBER, n.FORGNAME\n" +
                        "\t\t\t, q.FNAME AS FPLANERNAME, r.FNAME AS FPURCHASERNAME, s.FNAME AS FCURRENCYNANE\n" +
                        "\t\tFROM (\n" +
                        "\t\t\tSELECT k.FCREATEORGID, k.FApproveDate, k.FAUXPTYNUMBER, k.F_APPO_MINPOQTY, k.F_APPO_INCREASEQTY\n" +
                        "\t\t\t\t, k.F_APPO_FIXLEADTIME, k.FTaxPrice, k.FCURRENCYID, k.FEXPIRYDATE, k.FMATERIALID\n" +
                        "\t\t\t\t, m.FNUMBER, m.FPLANERID, m.FPURCHASERID, p.FNUMBER AS FORGNUMBER, p.FName AS FORGNAME\n" +
                        "\t\t\tFROM (\n" +
                        "\t\t\t\tSELECT j.FCREATEORGID, j.FAUXPTYNUMBER, j.FMATERIALID, j.RatePrice, i.FApproveDate\n" +
                        "\t\t\t\t\t, i.F_APPO_MINPOQTY, i.F_APPO_INCREASEQTY, i.F_APPO_FIXLEADTIME, i.FTaxPrice, i.FCURRENCYID\n" +
                        "\t\t\t\t\t, i.FEXPIRYDATE, i.FEXCHANGERATE\n" +
                        "\t\t\t\tFROM (\n" +
                        "\t\t\t\t\tSELECT i.FCREATEORGID, i.FAUXPTYNUMBER, i.FMATERIALID, MIN(i.RatePrice) AS RatePrice\n" +
                        "\t\t\t\t\tFROM (\n" +
                        "\t\t\t\t\t\tSELECT h.FCREATEORGID, h.FApproveDate, isNull(h.FAUXPTYNUMBER,'empty') as FAUXPTYNUMBER, h.F_APPO_MINPOQTY, h.F_APPO_INCREASEQTY\n" +
                        "\t\t\t\t\t\t\t, h.F_APPO_FIXLEADTIME, h.FTaxPrice, h.FCURRENCYID, h.FEXPIRYDATE, h.FMATERIALID\n" +
                        "\t\t\t\t\t\t\t, h.FEXCHANGERATE, FTaxPrice * FEXCHANGERATE AS RatePrice\n" +
                        "\t\t\t\t\t\tFROM (\n" +
                        "\t\t\t\t\t\t\tSELECT f.FCREATEORGID, f.FApproveDate, f.FAUXPTYNUMBER, f.F_APPO_MINPOQTY, f.F_APPO_INCREASEQTY\n" +
                        "\t\t\t\t\t\t\t\t, f.F_APPO_FIXLEADTIME, f.FTaxPrice, f.FCURRENCYID, f.FEXPIRYDATE, f.FMATERIALID\n" +
                        "\t\t\t\t\t\t\t\t, isNull(g.FEXCHANGERATE, 1) AS FEXCHANGERATE\n" +
                        "\t\t\t\t\t\t\tFROM (\n" +
                        "\t\t\t\t\t\t\t\tSELECT b.FCREATEORGID, b.FApproveDate, d.FAUXPTYNUMBER, a.F_APPO_MINPOQTY, a.F_APPO_INCREASEQTY\n" +
                        "\t\t\t\t\t\t\t\t\t, a.F_APPO_FIXLEADTIME, a.FTaxPrice, b.FCURRENCYID, a.FEXPIRYDATE, a.FMATERIALID\n" +
                        "\t\t\t\t\t\t\t\tFROM t_PUR_PriceListEntry a\n" +
                        "\t\t\t\t\t\t\t\t\tLEFT JOIN t_PUR_PriceList b ON a.fid = b.fid\n" +
                        "\t\t\t\t\t\t\t\t\tLEFT JOIN T_BD_FLEXSITEMDETAILV c ON a.FAUXPROPID = c.fid\n" +
                        "\t\t\t\t\t\t\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\t\t\t\t\t\t\tSELECT *\n" +
                        "\t\t\t\t\t\t\t\t\t\tFROM T_BD_AUXPTYVALUEENTITY\n" +
                        "\t\t\t\t\t\t\t\t\t\tWHERE FENTRYID IN (\n" +
                        "\t\t\t\t\t\t\t\t\t\t\tSELECT MIN(FENTRYID)\n" +
                        "\t\t\t\t\t\t\t\t\t\t\tFROM T_BD_AUXPTYVALUEENTITY\n" +
                        "\t\t\t\t\t\t\t\t\t\t\tGROUP BY FAUXPTYID\n" +
                        "\t\t\t\t\t\t\t\t\t\t)\n" +
                        "\t\t\t\t\t\t\t\t\t) d\n" +
                        "\t\t\t\t\t\t\t\t\tON c.ff100001 = d.FAUXPTYID\n" +
                        "\t\t\t\t\t\t\t\tWHERE getdate() < a.FEXPIRYDATE\n" +
                        "\t\t\t\t\t\t\t\t\tAND b.FDOCUMENTSTATUS = 'C'\n" +
                        "\t\t\t\t\t\t\t) f\n" +
                        "\t\t\t\t\t\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\t\t\t\t\t\tSELECT *\n" +
                        "\t\t\t\t\t\t\t\t\tFROM t_bd_rate\n" +
                        "\t\t\t\t\t\t\t\t\tWHERE (FRATETYPEID = '1'\n" +
                        "\t\t\t\t\t\t\t\t\t\tAND FCYTOID = '1'\n" +
                        "\t\t\t\t\t\t\t\t\t\tAND getdate() < FENDDATE)\n" +
                        "\t\t\t\t\t\t\t\t) g\n" +
                        "\t\t\t\t\t\t\t\tON f.FCURRENCYID = g.FCYFORID\n" +
                        "\t\t\t\t\t\t) h\n" +
                        "\t\t\t\t\t) i\n" +
                        "\t\t\t\t\tGROUP BY FCREATEORGID, FMATERIALID, FAUXPTYNUMBER\n" +
                        "\t\t\t\t) j\n" +
                        "\t\t\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\t\t\tSELECT h.FCREATEORGID, h.FApproveDate, isNull(h.FAUXPTYNUMBER,'empty') as FAUXPTYNUMBER,h.F_APPO_MINPOQTY, h.F_APPO_INCREASEQTY\n" +
                        "\t\t\t\t\t\t\t, h.F_APPO_FIXLEADTIME, h.FTaxPrice, h.FCURRENCYID, h.FEXPIRYDATE, h.FMATERIALID\n" +
                        "\t\t\t\t\t\t\t, h.FEXCHANGERATE, FTaxPrice * FEXCHANGERATE AS RatePrice\n" +
                        "\t\t\t\t\t\tFROM (\n" +
                        "\t\t\t\t\t\t\tSELECT f.FCREATEORGID, f.FApproveDate, f.FAUXPTYNUMBER, f.F_APPO_MINPOQTY, f.F_APPO_INCREASEQTY\n" +
                        "\t\t\t\t\t\t\t\t, f.F_APPO_FIXLEADTIME, f.FTaxPrice, f.FCURRENCYID, f.FEXPIRYDATE, f.FMATERIALID\n" +
                        "\t\t\t\t\t\t\t\t, isNull(g.FEXCHANGERATE, 1) AS FEXCHANGERATE\n" +
                        "\t\t\t\t\t\t\tFROM (\n" +
                        "\t\t\t\t\t\t\t\tSELECT b.FCREATEORGID, b.FApproveDate, d.FAUXPTYNUMBER, a.F_APPO_MINPOQTY, a.F_APPO_INCREASEQTY\n" +
                        "\t\t\t\t\t\t\t\t\t, a.F_APPO_FIXLEADTIME, a.FTaxPrice, b.FCURRENCYID, a.FEXPIRYDATE, a.FMATERIALID\n" +
                        "\t\t\t\t\t\t\t\tFROM t_PUR_PriceListEntry a\n" +
                        "\t\t\t\t\t\t\t\t\tLEFT JOIN t_PUR_PriceList b ON a.fid = b.fid\n" +
                        "\t\t\t\t\t\t\t\t\tLEFT JOIN T_BD_FLEXSITEMDETAILV c ON a.FAUXPROPID = c.fid\n" +
                        "\t\t\t\t\t\t\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\t\t\t\t\t\t\tSELECT *\n" +
                        "\t\t\t\t\t\t\t\t\t\tFROM T_BD_AUXPTYVALUEENTITY\n" +
                        "\t\t\t\t\t\t\t\t\t\tWHERE FENTRYID IN (\n" +
                        "\t\t\t\t\t\t\t\t\t\t\tSELECT MIN(FENTRYID)\n" +
                        "\t\t\t\t\t\t\t\t\t\t\tFROM T_BD_AUXPTYVALUEENTITY\n" +
                        "\t\t\t\t\t\t\t\t\t\t\tGROUP BY FAUXPTYID\n" +
                        "\t\t\t\t\t\t\t\t\t\t)\n" +
                        "\t\t\t\t\t\t\t\t\t) d\n" +
                        "\t\t\t\t\t\t\t\t\tON c.ff100001 = d.FAUXPTYID\n" +
                        "\t\t\t\t\t\t\t\tWHERE getdate() < a.FEXPIRYDATE\n" +
                        "\t\t\t\t\t\t\t\t\tAND b.FDOCUMENTSTATUS = 'C'\n" +
                        "\t\t\t\t\t\t\t) f\n" +
                        "\t\t\t\t\t\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\t\t\t\t\t\tSELECT *\n" +
                        "\t\t\t\t\t\t\t\t\tFROM t_bd_rate\n" +
                        "\t\t\t\t\t\t\t\t\tWHERE (FRATETYPEID = '1'\n" +
                        "\t\t\t\t\t\t\t\t\t\tAND FCYTOID = '1'\n" +
                        "\t\t\t\t\t\t\t\t\t\tAND getdate() < FENDDATE)\n" +
                        "\t\t\t\t\t\t\t\t) g\n" +
                        "\t\t\t\t\t\t\t\tON f.FCURRENCYID = g.FCYFORID\n" +
                        "\t\t\t\t\t\t) h\n" +
                        "\t\t\t\t\t) i\n" +
                        "\t\t\t\t\tON (j.RatePrice = i.RatePrice\n" +
                        "\t\t\t\t\t\tAND j.FCREATEORGID = i.FCREATEORGID\n" +
                        "\t\t\t\t\t\tAND j.FAUXPTYNUMBER = i.FAUXPTYNUMBER\n" +
                        "\t\t\t\t\t\tAND j.FMATERIALID = i.FMATERIALID)\n" +
                        "\t\t\t) k\n" +
                        "\t\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\t\tSELECT a.FMATERIALID, a.FNUMBER, b.FPLANERID, c.FPURCHASERID, a.FUSEORGID\n" +
                        "\t\t\t\t\tFROM T_BD_MATERIAL a\n" +
                        "\t\t\t\t\t\tLEFT JOIN t_BD_MaterialPlan b ON a.FMATERIALID = b.FMATERIALID\n" +
                        "\t\t\t\t\t\tLEFT JOIN t_bd_MaterialPurchase c ON a.FMATERIALID = c.FMATERIALID\n" +
                        "\t\t\t\t) m\n" +
                        "\t\t\t\tON k.FMATERIALID = m.FMATERIALID\n" +
                        "\t\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\t\tSELECT a.FORGID, a.FNUMBER, b.FNAME\n" +
                        "\t\t\t\t\tFROM T_ORG_Organizations a\n" +
                        "\t\t\t\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\t\t\t\tSELECT FORGID, FNAME\n" +
                        "\t\t\t\t\t\t\tFROM T_ORG_Organizations_L\n" +
                        "\t\t\t\t\t\t\tWHERE FLOCALEID = '2052'\n" +
                        "\t\t\t\t\t\t) b\n" +
                        "\t\t\t\t\t\tON a.FORGID = b.FORGID\n" +
                        "\t\t\t\t) p\n" +
                        "\t\t\t\tON k.FCREATEORGID = p.FORGID\n" +
                        "\t\t) n\n" +
                        "\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                        "\t\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                        "\t\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID\n" +
                        "\t\t\t) q\n" +
                        "\t\t\tON n.FPLANERID = q.FENTRYID\n" +
                        "\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                        "\t\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                        "\t\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID\n" +
                        "\t\t\t) r\n" +
                        "\t\t\tON n.FPURCHASERID = r.FENTRYID\n" +
                        "\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\tSELECT FNAME, FCURRENCYID, FLOCALEID\n" +
                        "\t\t\t\tFROM t_bd_currency_l\n" +
                        "\t\t\t\tWHERE FLOCALEID = '2052'\n" +
                        "\t\t\t) s\n" +
                        "\t\t\tON n.FCURRENCYID = s.FCURRENCYID\n" +
                        "\t) u\n" +
                        ") v" +
                        "WHERE v.init between "+begin+" and "+end;
            }

            List<List<Object>> sResult = InvokeHelper.QueryNewPriceCategory2(sql);
            return sResult;
        }
        return null;
    }



    //查询同一物料编码，版本下最低价格(存在多值情况) 价目表总数
    public static List<List<Object>> queryNewPriceCategoryCount() throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
            String sql="SELECT count(*) as count \n" +
                    "FROM (\n" +
                    "\tSELECT n.FCREATEORGID, n.FApproveDate, n.FAUXPTYNUMBER, n.F_APPO_MINPOQTY, n.F_APPO_INCREASEQTY\n" +
                    "\t\t, n.F_APPO_FIXLEADTIME, n.FTaxPrice, n.FCURRENCYID, n.FEXPIRYDATE, n.FMATERIALID\n" +
                    "\t\t, n.FNUMBER, n.FPLANERID, n.FPURCHASERID, n.FORGNUMBER, n.FORGNAME\n" +
                    "\t\t, q.FNAME AS FPLANERNAME, r.FNAME AS FPURCHASERNAME, s.FNAME AS FCURRENCYNANE\n" +
                    "\tFROM (\n" +
                    "\t\tSELECT k.FCREATEORGID, k.FApproveDate, k.FAUXPTYNUMBER, k.F_APPO_MINPOQTY, k.F_APPO_INCREASEQTY\n" +
                    "\t\t\t, k.F_APPO_FIXLEADTIME, k.FTaxPrice, k.FCURRENCYID, k.FEXPIRYDATE, k.FMATERIALID\n" +
                    "\t\t\t, m.FNUMBER, m.FPLANERID, m.FPURCHASERID, p.FNUMBER AS FORGNUMBER, p.FName AS FORGNAME\n" +
                    "\t\tFROM (\n" +
                    "\t\t\tSELECT j.FCREATEORGID, j.FAUXPTYNUMBER, j.FMATERIALID, j.RatePrice, i.FApproveDate\n" +
                    "\t\t\t\t, i.F_APPO_MINPOQTY, i.F_APPO_INCREASEQTY, i.F_APPO_FIXLEADTIME, i.FTaxPrice, i.FCURRENCYID\n" +
                    "\t\t\t\t, i.FEXPIRYDATE, i.FEXCHANGERATE\n" +
                    "\t\t\tFROM (\n" +
                    "\t\t\t\tSELECT i.FCREATEORGID, i.FAUXPTYNUMBER, i.FMATERIALID, MIN(i.RatePrice) AS RatePrice\n" +
                    "\t\t\t\tFROM (\n" +
                    "\t\t\t\t\tSELECT h.FCREATEORGID, h.FApproveDate,isNull(h.FAUXPTYNUMBER,'empty') as FAUXPTYNUMBER, h.F_APPO_MINPOQTY, h.F_APPO_INCREASEQTY\n" +
                    "\t\t\t\t\t\t, h.F_APPO_FIXLEADTIME, h.FTaxPrice, h.FCURRENCYID, h.FEXPIRYDATE, h.FMATERIALID\n" +
                    "\t\t\t\t\t\t, h.FEXCHANGERATE, FTaxPrice * FEXCHANGERATE AS RatePrice\n" +
                    "\t\t\t\t\tFROM (\n" +
                    "\t\t\t\t\t\tSELECT f.FCREATEORGID, f.FApproveDate, f.FAUXPTYNUMBER, f.F_APPO_MINPOQTY, f.F_APPO_INCREASEQTY\n" +
                    "\t\t\t\t\t\t\t, f.F_APPO_FIXLEADTIME, f.FTaxPrice, f.FCURRENCYID, f.FEXPIRYDATE, f.FMATERIALID\n" +
                    "\t\t\t\t\t\t\t, isNull(g.FEXCHANGERATE, 1) AS FEXCHANGERATE\n" +
                    "\t\t\t\t\t\tFROM (\n" +
                    "\t\t\t\t\t\t\tSELECT b.FCREATEORGID, b.FApproveDate, d.FAUXPTYNUMBER, a.F_APPO_MINPOQTY, a.F_APPO_INCREASEQTY\n" +
                    "\t\t\t\t\t\t\t\t, a.F_APPO_FIXLEADTIME, a.FTaxPrice, b.FCURRENCYID, a.FEXPIRYDATE, a.FMATERIALID\n" +
                    "\t\t\t\t\t\t\tFROM t_PUR_PriceListEntry a\n" +
                    "\t\t\t\t\t\t\t\tLEFT JOIN t_PUR_PriceList b ON a.fid = b.fid\n" +
                    "\t\t\t\t\t\t\t\tLEFT JOIN T_BD_FLEXSITEMDETAILV c ON a.FAUXPROPID = c.fid\n" +
                    "\t\t\t\t\t\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\t\t\t\t\t\tSELECT *\n" +
                    "\t\t\t\t\t\t\t\t\tFROM T_BD_AUXPTYVALUEENTITY\n" +
                    "\t\t\t\t\t\t\t\t\tWHERE FENTRYID IN (\n" +
                    "\t\t\t\t\t\t\t\t\t\tSELECT MIN(FENTRYID)\n" +
                    "\t\t\t\t\t\t\t\t\t\tFROM T_BD_AUXPTYVALUEENTITY\n" +
                    "\t\t\t\t\t\t\t\t\t\tGROUP BY FAUXPTYID\n" +
                    "\t\t\t\t\t\t\t\t\t)\n" +
                    "\t\t\t\t\t\t\t\t) d\n" +
                    "\t\t\t\t\t\t\t\tON c.ff100001 = d.FAUXPTYID\n" +
                    "\t\t\t\t\t\t\tWHERE getdate() < a.FEXPIRYDATE\n" +
                    "\t\t\t\t\t\t\t\tAND b.FDOCUMENTSTATUS = 'C'\n" +
                    "\t\t\t\t\t\t) f\n" +
                    "\t\t\t\t\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\t\t\t\t\tSELECT *\n" +
                    "\t\t\t\t\t\t\t\tFROM t_bd_rate\n" +
                    "\t\t\t\t\t\t\t\tWHERE (FRATETYPEID = '1'\n" +
                    "\t\t\t\t\t\t\t\t\tAND FCYTOID = '1'\n" +
                    "\t\t\t\t\t\t\t\t\tAND getdate() < FENDDATE)\n" +
                    "\t\t\t\t\t\t\t) g\n" +
                    "\t\t\t\t\t\t\tON f.FCURRENCYID = g.FCYFORID\n" +
                    "\t\t\t\t\t) h\n" +
                    "\t\t\t\t) i\n" +
                    "\t\t\t\tGROUP BY FCREATEORGID, FMATERIALID, FAUXPTYNUMBER\n" +
                    "\t\t\t) j\n" +
                    "\t\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\t\tSELECT h.FCREATEORGID, h.FApproveDate, isNull(h.FAUXPTYNUMBER,'empty') as FAUXPTYNUMBER, h.F_APPO_MINPOQTY, h.F_APPO_INCREASEQTY\n" +
                    "\t\t\t\t\t\t, h.F_APPO_FIXLEADTIME, h.FTaxPrice, h.FCURRENCYID, h.FEXPIRYDATE, h.FMATERIALID\n" +
                    "\t\t\t\t\t\t, h.FEXCHANGERATE, FTaxPrice * FEXCHANGERATE AS RatePrice\n" +
                    "\t\t\t\t\tFROM (\n" +
                    "\t\t\t\t\t\tSELECT f.FCREATEORGID, f.FApproveDate, f.FAUXPTYNUMBER, f.F_APPO_MINPOQTY, f.F_APPO_INCREASEQTY\n" +
                    "\t\t\t\t\t\t\t, f.F_APPO_FIXLEADTIME, f.FTaxPrice, f.FCURRENCYID, f.FEXPIRYDATE, f.FMATERIALID\n" +
                    "\t\t\t\t\t\t\t, isNull(g.FEXCHANGERATE, 1) AS FEXCHANGERATE\n" +
                    "\t\t\t\t\t\tFROM (\n" +
                    "\t\t\t\t\t\t\tSELECT b.FCREATEORGID, b.FApproveDate, d.FAUXPTYNUMBER, a.F_APPO_MINPOQTY, a.F_APPO_INCREASEQTY\n" +
                    "\t\t\t\t\t\t\t\t, a.F_APPO_FIXLEADTIME, a.FTaxPrice, b.FCURRENCYID, a.FEXPIRYDATE, a.FMATERIALID\n" +
                    "\t\t\t\t\t\t\tFROM t_PUR_PriceListEntry a\n" +
                    "\t\t\t\t\t\t\t\tLEFT JOIN t_PUR_PriceList b ON a.fid = b.fid\n" +
                    "\t\t\t\t\t\t\t\tLEFT JOIN T_BD_FLEXSITEMDETAILV c ON a.FAUXPROPID = c.fid\n" +
                    "\t\t\t\t\t\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\t\t\t\t\t\tSELECT *\n" +
                    "\t\t\t\t\t\t\t\t\tFROM T_BD_AUXPTYVALUEENTITY\n" +
                    "\t\t\t\t\t\t\t\t\tWHERE FENTRYID IN (\n" +
                    "\t\t\t\t\t\t\t\t\t\tSELECT MIN(FENTRYID)\n" +
                    "\t\t\t\t\t\t\t\t\t\tFROM T_BD_AUXPTYVALUEENTITY\n" +
                    "\t\t\t\t\t\t\t\t\t\tGROUP BY FAUXPTYID\n" +
                    "\t\t\t\t\t\t\t\t\t)\n" +
                    "\t\t\t\t\t\t\t\t) d\n" +
                    "\t\t\t\t\t\t\t\tON c.ff100001 = d.FAUXPTYID\n" +
                    "\t\t\t\t\t\t\tWHERE getdate() < a.FEXPIRYDATE\n" +
                    "\t\t\t\t\t\t\t\tAND b.FDOCUMENTSTATUS = 'C'\n" +
                    "\t\t\t\t\t\t) f\n" +
                    "\t\t\t\t\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\t\t\t\t\tSELECT *\n" +
                    "\t\t\t\t\t\t\t\tFROM t_bd_rate\n" +
                    "\t\t\t\t\t\t\t\tWHERE (FRATETYPEID = '1'\n" +
                    "\t\t\t\t\t\t\t\t\tAND FCYTOID = '1'\n" +
                    "\t\t\t\t\t\t\t\t\tAND getdate() < FENDDATE)\n" +
                    "\t\t\t\t\t\t\t) g\n" +
                    "\t\t\t\t\t\t\tON f.FCURRENCYID = g.FCYFORID\n" +
                    "\t\t\t\t\t) h\n" +
                    "\t\t\t\t) i\n" +
                    "\t\t\t\tON (j.RatePrice = i.RatePrice\n" +
                    "\t\t\t\t\tAND j.FCREATEORGID = i.FCREATEORGID\n" +
                    "\t\t\t\t\tAND j.FAUXPTYNUMBER = i.FAUXPTYNUMBER\n" +
                    "\t\t\t\t\tAND j.FMATERIALID = i.FMATERIALID)\n" +
                    "\t\t) k\n" +
                    "\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\tSELECT a.FMATERIALID, a.FNUMBER, b.FPLANERID, c.FPURCHASERID, a.FUSEORGID\n" +
                    "\t\t\t\tFROM T_BD_MATERIAL a\n" +
                    "\t\t\t\t\tLEFT JOIN t_BD_MaterialPlan b ON a.FMATERIALID = b.FMATERIALID\n" +
                    "\t\t\t\t\tLEFT JOIN t_bd_MaterialPurchase c ON a.FMATERIALID = c.FMATERIALID\n" +
                    "\t\t\t) m\n" +
                    "\t\t\tON k.FMATERIALID = m.FMATERIALID\n" +
                    "\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\tSELECT a.FORGID, a.FNUMBER, b.FNAME\n" +
                    "\t\t\t\tFROM T_ORG_Organizations a\n" +
                    "\t\t\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\t\t\tSELECT FORGID, FNAME\n" +
                    "\t\t\t\t\t\tFROM T_ORG_Organizations_L\n" +
                    "\t\t\t\t\t\tWHERE FLOCALEID = '2052'\n" +
                    "\t\t\t\t\t) b\n" +
                    "\t\t\t\t\tON a.FORGID = b.FORGID\n" +
                    "\t\t\t) p\n" +
                    "\t\t\tON k.FCREATEORGID = p.FORGID\n" +
                    "\t) n\n" +
                    "\t\tLEFT JOIN (\n" +
                    "\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                    "\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                    "\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID\n" +
                    "\t\t) q\n" +
                    "\t\tON n.FPLANERID = q.FENTRYID\n" +
                    "\t\tLEFT JOIN (\n" +
                    "\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                    "\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                    "\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID\n" +
                    "\t\t) r\n" +
                    "\t\tON n.FPURCHASERID = r.FENTRYID\n" +
                    "\t\tLEFT JOIN (\n" +
                    "\t\t\tSELECT FNAME, FCURRENCYID, FLOCALEID\n" +
                    "\t\t\tFROM t_bd_currency_l\n" +
                    "\t\t\tWHERE FLOCALEID = '2052'\n" +
                    "\t\t) s\n" +
                    "\t\tON n.FCURRENCYID = s.FCURRENCYID\n" +
                    ") u";
            List<List<Object>> sResult = InvokeHelper.QueryNewPriceCategoryCount(sql);
            return sResult;
        }
        return null;
    }



    //查询同一编码，同一版本在制和（峰米没有在制，只有光峰有在制）
    public static List<List<Object>> queryNewFqty(String begin, String end) throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
            String sql="SELECT a.fqty, a.FMATNUMBER, a.FVERNUMBER, b.FPLANERNAME, b.FPURCHASERNAME\n" +
                    "FROM (\n" +
                    "\tSELECT SUM(FQTY) AS fqty, FMATNUMBER, FVERNUMBER\n" +
                    "\tFROM V_APPO_WIPQTY\n" +
                    "\tGROUP BY FMATNUMBER, FVERNUMBER\n" +
                    ") a\n" +
                    "\tLEFT JOIN (\n" +
                    "\t\tSELECT d.FMATERIALID, d.FPLANERID, d.FPURCHASERID, d.FUSEORGID, d.FNUMBER\n" +
                    "\t\t\t, e.FNAME AS FPLANERNAME, f.FNAME AS FPURCHASERNAME\n" +
                    "\t\tFROM (\n" +
                    "\t\t\tSELECT a.FMATERIALID, b.FPLANERID, c.FPURCHASERID, a.FUSEORGID, a.FNUMBER\n" +
                    "\t\t\tFROM T_BD_MATERIAL a\n" +
                    "\t\t\tLEFT JOIN t_BD_MaterialPlan b ON a.FMATERIALID = b.FMATERIALID \n" +
                    "\t\t\t\tLEFT JOIN t_bd_MaterialPurchase c ON a.FMATERIALID = c.FMATERIALID \n" +
                    "\t\t) d\n" +
                    "\t\tLEFT JOIN (\n" +
                    "\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                    "\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                    "\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID \n" +
                    "\t\t) e ON d.FPLANERID = e.FENTRYID \n" +
                    "\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                    "\t\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                    "\t\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID \n" +
                    "\t\t\t) f ON d.FPURCHASERID = f.FENTRYID \n" +
                    "\t) b ON a.FMATNUMBER = b.FNUMBER\n" +
                    "AND b.FUSEORGID = '100146'";
            if (!begin.isEmpty()&&!end.isEmpty()) {
                sql = "SELECT *\n" +
                        "FROM (\n" +
                        "\tSELECT ROW_NUMBER() OVER (ORDER BY getdate()) AS init, *\n" +
                        "\tFROM (\n" +
                        "\t\tSELECT a.fqty, a.FMATNUMBER, a.FVERNUMBER, b.FPLANERNAME, b.FPURCHASERNAME\n" +
                        "\t\tFROM (\n" +
                        "\t\t\tSELECT SUM(FQTY) AS fqty, FMATNUMBER, FVERNUMBER\n" +
                        "\t\t\tFROM V_APPO_WIPQTY\n" +
                        "\t\t\tGROUP BY FMATNUMBER, FVERNUMBER\n" +
                        "\t\t) a\n" +
                        "\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\tSELECT d.FMATERIALID, d.FPLANERID, d.FPURCHASERID, d.FUSEORGID, d.FNUMBER\n" +
                        "\t\t\t\t\t, e.FNAME AS FPLANERNAME, f.FNAME AS FPURCHASERNAME\n" +
                        "\t\t\t\tFROM (\n" +
                        "\t\t\t\t\tSELECT a.FMATERIALID, b.FPLANERID, c.FPURCHASERID, a.FUSEORGID, a.FNUMBER\n" +
                        "\t\t\t\t\tFROM T_BD_MATERIAL a\n" +
                        "\t\t\t\t\tLEFT JOIN t_BD_MaterialPlan b ON a.FMATERIALID = b.FMATERIALID \n" +
                        "\t\t\t\t\t\tLEFT JOIN t_bd_MaterialPurchase c ON a.FMATERIALID = c.FMATERIALID \n" +
                        "\t\t\t\t) d\n" +
                        "\t\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                        "\t\t\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                        "\t\t\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID \n" +
                        "\t\t\t\t) e ON d.FPLANERID = e.FENTRYID \n" +
                        "\t\t\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                        "\t\t\t\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                        "\t\t\t\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID \n" +
                        "\t\t\t\t\t) f ON d.FPURCHASERID = f.FENTRYID \n" +
                        "\t\t\t) b ON a.FMATNUMBER = b.FNUMBER\n" +
                        "\t\tAND b.FUSEORGID = '100146' \n" +
                        "\t) h\n" +
                        ") i\n" +
                        "WHERE i.init BETWEEN "+begin+" AND "+end;
            }
            List<List<Object>> sResult = InvokeHelper.QueryNewFqty(sql);
            return sResult;
        }
        return  null;
    }

    //查询同一编码，同一版本,同一组织 在途和
    public static List<List<Object>> queryNewPoqty(String begin, String end) throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
            String sql="SELECT a.poqty, a.ItemNum, a.ITEMVER, a.ONum, a.OName\n" +
                    "\t, b.FPLANERNAME, b.FPURCHASERNAME\n" +
                    "FROM (\n" +
                    "\tSELECT SUM(POQTY) AS poqty, ItemNum, ITEMVER, ONum\n" +
                    "\t\t, OName\n" +
                    "\tFROM V_APPO_INVALLQRY_POQTY\n" +
                    "\tGROUP BY ItemNum, ITEMVER, ONum, OName\n" +
                    ") a\n" +
                    "\tLEFT JOIN (\n" +
                    "\t\tSELECT d.FMATERIALID, d.FPLANERID, d.FPURCHASERID, d.FUSEORGID, d.FNUMBER\n" +
                    "\t\t\t, e.FNAME AS FPLANERNAME, f.FNAME AS FPURCHASERNAME, g.FNUMBER AS ONum\n" +
                    "\t\tFROM (\n" +
                    "\t\t\tSELECT a.FMATERIALID, b.FPLANERID, c.FPURCHASERID, a.FUSEORGID, a.FNUMBER\n" +
                    "\t\t\tFROM T_BD_MATERIAL a\n" +
                    "\t\t\tLEFT JOIN t_BD_MaterialPlan b ON a.FMATERIALID = b.FMATERIALID \n" +
                    "\t\t\t\tLEFT JOIN t_bd_MaterialPurchase c ON a.FMATERIALID = c.FMATERIALID \n" +
                    "\t\t) d\n" +
                    "\t\tLEFT JOIN (\n" +
                    "\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                    "\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                    "\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID \n" +
                    "\t\t) e ON d.FPLANERID = e.FENTRYID \n" +
                    "\t\tLEFT JOIN (\n" +
                    "\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                    "\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                    "\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID \n" +
                    "\t\t) f ON d.FPURCHASERID = f.FENTRYID \n" +
                    "\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\tSELECT FORGID, FNUMBER\n" +
                    "\t\t\t\tFROM T_ORG_Organizations\n" +
                    "\t\t\t) g ON d.FUSEORGID = g.FORGID \n" +
                    "\t) b ON a.ItemNum = b.FNUMBER\n" +
                    "AND a.ONum = b.ONum";
            if (!begin.isEmpty()&&!end.isEmpty()){
                sql="SELECT *\n" +
                        "FROM (\n" +
                        "\tSELECT ROW_NUMBER() OVER (ORDER BY getdate()) AS init, *\n" +
                        "\tFROM (\n" +
                        "\t\tSELECT a.poqty, a.ItemNum, a.ITEMVER, a.ONum, a.OName\n" +
                        "\t\t\t, b.FPLANERNAME, b.FPURCHASERNAME\n" +
                        "\t\tFROM (\n" +
                        "\t\t\tSELECT SUM(POQTY) AS poqty, ItemNum, ITEMVER, ONum\n" +
                        "\t\t\t\t, OName\n" +
                        "\t\t\tFROM V_APPO_INVALLQRY_POQTY\n" +
                        "\t\t\tGROUP BY ItemNum, ITEMVER, ONum, OName\n" +
                        "\t\t) a\n" +
                        "\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\tSELECT d.FMATERIALID, d.FPLANERID, d.FPURCHASERID, d.FUSEORGID, d.FNUMBER\n" +
                        "\t\t\t\t\t, e.FNAME AS FPLANERNAME, f.FNAME AS FPURCHASERNAME, g.FNUMBER AS ONum\n" +
                        "\t\t\t\tFROM (\n" +
                        "\t\t\t\t\tSELECT a.FMATERIALID, b.FPLANERID, c.FPURCHASERID, a.FUSEORGID, a.FNUMBER\n" +
                        "\t\t\t\t\tFROM T_BD_MATERIAL a\n" +
                        "\t\t\t\t\tLEFT JOIN t_BD_MaterialPlan b ON a.FMATERIALID = b.FMATERIALID \n" +
                        "\t\t\t\t\t\tLEFT JOIN t_bd_MaterialPurchase c ON a.FMATERIALID = c.FMATERIALID \n" +
                        "\t\t\t\t) d\n" +
                        "\t\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                        "\t\t\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                        "\t\t\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID \n" +
                        "\t\t\t\t) e ON d.FPLANERID = e.FENTRYID \n" +
                        "\t\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                        "\t\t\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                        "\t\t\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID \n" +
                        "\t\t\t\t) f ON d.FPURCHASERID = f.FENTRYID \n" +
                        "\t\t\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\t\t\tSELECT FORGID, FNUMBER\n" +
                        "\t\t\t\t\t\tFROM T_ORG_Organizations\n" +
                        "\t\t\t\t\t) g ON d.FUSEORGID = g.FORGID \n" +
                        "\t\t\t) b ON a.ItemNum = b.FNUMBER\n" +
                        "\t\tAND a.ONum = b.ONum \n" +
                        "\t) h\n" +
                        ") i\n" +
                        "WHERE i.init BETWEEN "+begin+" AND "+end;
            }
            List<List<Object>> sResult = InvokeHelper.QueryNewPoqty(sql);
            return sResult;
        }
        return  null;
    }

    //查询同一编码，同一版本，同一组织库存和
    public static List<List<Object>> queryNewIqty(String begin, String end) throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
            String sql="SELECT a.iqty, a.ItemNum, a.ITEMVER, a.ONum, a.OName\n" +
                    "\t, b.FPLANERNAME, b.FPURCHASERNAME\n" +
                    "FROM (\n" +
                    "\tSELECT SUM(IQTY) AS iqty, ItemNum, ITEMVER, ONum\n" +
                    "\t\t, OName\n" +
                    "\tFROM V_APPO_INVALLQRY_INV\n" +
                    "\tGROUP BY ItemNum, ITEMVER, ONum, OName\n" +
                    ") a\n" +
                    "\tLEFT JOIN (\n" +
                    "\t\tSELECT d.FMATERIALID, d.FPLANERID, d.FPURCHASERID, d.FUSEORGID, d.FNUMBER\n" +
                    "\t\t\t, e.FNAME AS FPLANERNAME, f.FNAME AS FPURCHASERNAME, g.FNUMBER AS ONum\n" +
                    "\t\tFROM (\n" +
                    "\t\t\tSELECT a.FMATERIALID, b.FPLANERID, c.FPURCHASERID, a.FUSEORGID, a.FNUMBER\n" +
                    "\t\t\tFROM T_BD_MATERIAL a\n" +
                    "\t\t\tLEFT JOIN t_BD_MaterialPlan b ON a.FMATERIALID = b.FMATERIALID \n" +
                    "\t\t\t\tLEFT JOIN t_bd_MaterialPurchase c ON a.FMATERIALID = c.FMATERIALID \n" +
                    "\t\t) d\n" +
                    "\t\tLEFT JOIN (\n" +
                    "\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                    "\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                    "\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID \n" +
                    "\t\t) e ON d.FPLANERID = e.FENTRYID \n" +
                    "\t\tLEFT JOIN (\n" +
                    "\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                    "\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                    "\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID \n" +
                    "\t\t) f ON d.FPURCHASERID = f.FENTRYID \n" +
                    "\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\tSELECT FORGID, FNUMBER\n" +
                    "\t\t\t\tFROM T_ORG_Organizations\n" +
                    "\t\t\t) g ON d.FUSEORGID = g.FORGID \n" +
                    "\t) b ON a.ItemNum = b.FNUMBER\n" +
                    "AND a.ONum = b.ONum ";
            if (!begin.isEmpty()&&!end.isEmpty()){
                sql="SELECT *\n" +
                        "FROM (\n" +
                        "\tSELECT ROW_NUMBER() OVER (ORDER BY getdate()) AS init, *\n" +
                        "\tFROM (\n" +
                        "\t\tSELECT a.iqty, a.ItemNum, a.ITEMVER, a.ONum, a.OName\n" +
                        "\t\t\t, b.FPLANERNAME, b.FPURCHASERNAME\n" +
                        "\t\tFROM (\n" +
                        "\t\t\tSELECT SUM(IQTY) AS iqty, ItemNum, ITEMVER, ONum\n" +
                        "\t\t\t\t, OName\n" +
                        "\t\t\tFROM V_APPO_INVALLQRY_INV\n" +
                        "\t\t\tGROUP BY ItemNum, ITEMVER, ONum, OName\n" +
                        "\t\t) a\n" +
                        "\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\tSELECT d.FMATERIALID, d.FPLANERID, d.FPURCHASERID, d.FUSEORGID, d.FNUMBER\n" +
                        "\t\t\t\t\t, e.FNAME AS FPLANERNAME, f.FNAME AS FPURCHASERNAME, g.FNUMBER AS ONum\n" +
                        "\t\t\t\tFROM (\n" +
                        "\t\t\t\t\tSELECT a.FMATERIALID, b.FPLANERID, c.FPURCHASERID, a.FUSEORGID, a.FNUMBER\n" +
                        "\t\t\t\t\tFROM T_BD_MATERIAL a\n" +
                        "\t\t\t\t\tLEFT JOIN t_BD_MaterialPlan b ON a.FMATERIALID = b.FMATERIALID \n" +
                        "\t\t\t\t\t\tLEFT JOIN t_bd_MaterialPurchase c ON a.FMATERIALID = c.FMATERIALID \n" +
                        "\t\t\t\t) d\n" +
                        "\t\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                        "\t\t\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                        "\t\t\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID \n" +
                        "\t\t\t\t) e ON d.FPLANERID = e.FENTRYID \n" +
                        "\t\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                        "\t\t\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                        "\t\t\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID \n" +
                        "\t\t\t\t) f ON d.FPURCHASERID = f.FENTRYID \n" +
                        "\t\t\t\t\tLEFT JOIN (\n" +
                        "\t\t\t\t\t\tSELECT FORGID, FNUMBER\n" +
                        "\t\t\t\t\t\tFROM T_ORG_Organizations\n" +
                        "\t\t\t\t\t) g ON d.FUSEORGID = g.FORGID \n" +
                        "\t\t\t) b ON a.ItemNum = b.FNUMBER\n" +
                        "\t\tAND a.ONum = b.ONum \n" +
                        "\t) h\n" +
                        ") i\n" +
                        "WHERE i.init BETWEEN "+begin+" AND "+end;
            }
            List<List<Object>> sResult = InvokeHelper.QueryNewIqty(sql);
            return sResult;
        }
        return  null;
    }

    //查询同一编码，同一版本在途和 总数
    public static List<List<Object>> queryNewPoqtyCount() throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
            String sql="SELECT COUNT(*) AS count\n" +
                    "FROM (\n" +
                    "\tSELECT a.poqty, a.ItemNum, a.ITEMVER, a.ONum, a.OName\n" +
                    "\t\t, b.FPLANERNAME, b.FPURCHASERNAME\n" +
                    "\tFROM (\n" +
                    "\t\tSELECT SUM(POQTY) AS poqty, ItemNum, ITEMVER, ONum\n" +
                    "\t\t\t, OName\n" +
                    "\t\tFROM V_APPO_INVALLQRY_POQTY\n" +
                    "\t\tGROUP BY ItemNum, ITEMVER, ONum, OName\n" +
                    "\t) a\n" +
                    "\t\tLEFT JOIN (\n" +
                    "\t\t\tSELECT d.FMATERIALID, d.FPLANERID, d.FPURCHASERID, d.FUSEORGID, d.FNUMBER\n" +
                    "\t\t\t\t, e.FNAME AS FPLANERNAME, f.FNAME AS FPURCHASERNAME, g.FNUMBER AS ONum\n" +
                    "\t\t\tFROM (\n" +
                    "\t\t\t\tSELECT a.FMATERIALID, b.FPLANERID, c.FPURCHASERID, a.FUSEORGID, a.FNUMBER\n" +
                    "\t\t\t\tFROM T_BD_MATERIAL a\n" +
                    "\t\t\t\tLEFT JOIN t_BD_MaterialPlan b ON a.FMATERIALID = b.FMATERIALID \n" +
                    "\t\t\t\t\tLEFT JOIN t_bd_MaterialPurchase c ON a.FMATERIALID = c.FMATERIALID \n" +
                    "\t\t\t) d\n" +
                    "\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                    "\t\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                    "\t\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID \n" +
                    "\t\t\t) e ON d.FPLANERID = e.FENTRYID \n" +
                    "\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                    "\t\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                    "\t\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID \n" +
                    "\t\t\t) f ON d.FPURCHASERID = f.FENTRYID \n" +
                    "\t\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\t\tSELECT FORGID, FNUMBER\n" +
                    "\t\t\t\t\tFROM T_ORG_Organizations\n" +
                    "\t\t\t\t) g ON d.FUSEORGID = g.FORGID \n" +
                    "\t\t) b ON a.ItemNum = b.FNUMBER\n" +
                    "\tAND a.ONum = b.ONum \n" +
                    ") h";
            List<List<Object>> sResult = InvokeHelper.QueryNewPoqtyCount(sql);
            return sResult;
        }
        return  null;
    }

    //查询同一编码，同一版本在制和 总数（峰米没有在制，光峰才有在制）
    public static List<List<Object>> queryNewFqtyCount() throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
            String sql="SELECT COUNT(*) AS count\n" +
                    "FROM (\n" +
                    "\tSELECT a.fqty, a.FMATNUMBER, a.FVERNUMBER, b.FPLANERNAME, b.FPURCHASERNAME\n" +
                    "\tFROM (\n" +
                    "\t\tSELECT SUM(FQTY) AS fqty, FMATNUMBER, FVERNUMBER\n" +
                    "\t\tFROM V_APPO_WIPQTY\n" +
                    "\t\tGROUP BY FMATNUMBER, FVERNUMBER\n" +
                    "\t) a\n" +
                    "\t\tLEFT JOIN (\n" +
                    "\t\t\tSELECT d.FMATERIALID, d.FPLANERID, d.FPURCHASERID, d.FUSEORGID, d.FNUMBER\n" +
                    "\t\t\t\t, e.FNAME AS FPLANERNAME, f.FNAME AS FPURCHASERNAME\n" +
                    "\t\t\tFROM (\n" +
                    "\t\t\t\tSELECT a.FMATERIALID, b.FPLANERID, c.FPURCHASERID, a.FUSEORGID, a.FNUMBER\n" +
                    "\t\t\t\tFROM T_BD_MATERIAL a\n" +
                    "\t\t\t\tLEFT JOIN t_BD_MaterialPlan b ON a.FMATERIALID = b.FMATERIALID \n" +
                    "\t\t\t\t\tLEFT JOIN t_bd_MaterialPurchase c ON a.FMATERIALID = c.FMATERIALID \n" +
                    "\t\t\t) d\n" +
                    "\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                    "\t\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                    "\t\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID \n" +
                    "\t\t\t) e ON d.FPLANERID = e.FENTRYID \n" +
                    "\t\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                    "\t\t\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                    "\t\t\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID \n" +
                    "\t\t\t\t) f ON d.FPURCHASERID = f.FENTRYID \n" +
                    "\t\t) b ON a.FMATNUMBER = b.FNUMBER\n" +
                    "\tAND b.FUSEORGID = '100146' \n" +
                    ") h";
            List<List<Object>> sResult = InvokeHelper.QueryNewFqtyCount(sql);
            return sResult;
        }
        return  null;
    }

    //查询同一编码，同一版本库存和 总数
    public static List<List<Object>> queryNewIqtyCount() throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
            String sql="SELECT COUNT(*) as count\n" +
                    "FROM (\n" +
                    "\tSELECT a.iqty, a.ItemNum, a.ITEMVER, a.ONum, a.OName\n" +
                    "\t\t, b.FPLANERNAME, b.FPURCHASERNAME\n" +
                    "\tFROM (\n" +
                    "\t\tSELECT SUM(IQTY) AS iqty, ItemNum, ITEMVER, ONum\n" +
                    "\t\t\t, OName\n" +
                    "\t\tFROM V_APPO_INVALLQRY_INV\n" +
                    "\t\tGROUP BY ItemNum, ITEMVER, ONum, OName\n" +
                    "\t) a\n" +
                    "\t\tLEFT JOIN (\n" +
                    "\t\t\tSELECT d.FMATERIALID, d.FPLANERID, d.FPURCHASERID, d.FUSEORGID, d.FNUMBER\n" +
                    "\t\t\t\t, e.FNAME AS FPLANERNAME, f.FNAME AS FPURCHASERNAME, g.FNUMBER AS ONum\n" +
                    "\t\t\tFROM (\n" +
                    "\t\t\t\tSELECT a.FMATERIALID, b.FPLANERID, c.FPURCHASERID, a.FUSEORGID, a.FNUMBER\n" +
                    "\t\t\t\tFROM T_BD_MATERIAL a\n" +
                    "\t\t\t\tLEFT JOIN t_BD_MaterialPlan b ON a.FMATERIALID = b.FMATERIALID \n" +
                    "\t\t\t\t\tLEFT JOIN t_bd_MaterialPurchase c ON a.FMATERIALID = c.FMATERIALID \n" +
                    "\t\t\t) d\n" +
                    "\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                    "\t\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                    "\t\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID \n" +
                    "\t\t\t) e ON d.FPLANERID = e.FENTRYID \n" +
                    "\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\tSELECT b.FNAME, a.FENTRYID\n" +
                    "\t\t\t\tFROM T_BD_OPERATORENTRY a\n" +
                    "\t\t\t\t\tLEFT JOIN T_BD_STAFF_L b ON a.FSTAFFID = b.FSTAFFID \n" +
                    "\t\t\t) f ON d.FPURCHASERID = f.FENTRYID \n" +
                    "\t\t\t\tLEFT JOIN (\n" +
                    "\t\t\t\t\tSELECT FORGID, FNUMBER\n" +
                    "\t\t\t\t\tFROM T_ORG_Organizations\n" +
                    "\t\t\t\t) g ON d.FUSEORGID = g.FORGID \n" +
                    "\t\t) b ON a.ItemNum = b.FNUMBER\n" +
                    "\tAND a.ONum = b.ONum \n" +
                    ") h";
            List<List<Object>> sResult = InvokeHelper.QueryNewIqtyCount(sql);
            return sResult;
        }
        return  null;
    }

    //查询K3全部在制，在途，库存 总数
    public static List<List<Object>> query2AllCount() throws Exception {

        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
            String sql="select count(*) as count from \n" +
                    "(select iqty,fqty,poqty,isnull(c.ItemNum,d.ItemNum) as ItemNum,isnull(c.ITEMVER,d.ITEMVER) as ITEMVER from\n" +
                    " (select iqty,fqty,isnull(a.ItemNum,b.FMATNUMBER) as ItemNum,isnull(a.ITEMVER,b.FVERNUMBER) as ITEMVER\n" +
                    " from (select sum(IQTY) iqty,ItemNum,ITEMVER from V_APPO_INVALLQRY_INV where StockName<>'废品仓' Group By ItemNum,ITEMVER) a\n" +
                    " full join (select sum(FQTY) fqty,FMATNUMBER,FVERNUMBER from V_APPO_WIPQTY Group By FMATNUMBER,FVERNUMBER) b on a.ItemNum=b.FMATNUMBER and a.ITEMVER=b.FVERNUMBER) c \n" +
                    " full join (select sum(POQTY) poqty,ItemNum,ITEMVER from V_APPO_INVALLQRY_POQTY Group By ItemNum,ITEMVER) d on c.ItemNum=d.ItemNum and c.ITEMVER=d.ITEMVER) e";
            System.out.println("sql=="+sql);
            List<List<Object>> sResult = InvokeHelper.Query2AllCount(sql);
            return sResult;
        }
        return null;
    }

    //查询全部在制，在途，库存
    public static List<List<Object>> query2All(String begin, String end) throws Exception {

        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
            String sql="select * from \n" +
                    "(select iqty,fqty,poqty,isnull(c.ItemNum,d.ItemNum) as ItemNum,isnull(c.ITEMVER,d.ITEMVER) as ITEMVER from\n" +
                    " (select iqty,fqty,isnull(a.ItemNum,b.FMATNUMBER) as ItemNum,isnull(a.ITEMVER,b.FVERNUMBER) as ITEMVER\n" +
                    " from (select sum(IQTY) iqty,ItemNum,ITEMVER from V_APPO_INVALLQRY_INV where StockName<>'废品仓' Group By ItemNum,ITEMVER) a\n" +
                    " full join (select sum(FQTY) fqty,FMATNUMBER,FVERNUMBER from V_APPO_WIPQTY Group By FMATNUMBER,FVERNUMBER) b on a.ItemNum=b.FMATNUMBER and a.ITEMVER=b.FVERNUMBER) c \n" +
                    " full join (select sum(POQTY) poqty,ItemNum,ITEMVER from V_APPO_INVALLQRY_POQTY Group By ItemNum,ITEMVER) d on c.ItemNum=d.ItemNum and c.ITEMVER=d.ITEMVER) e";
            if (!begin.isEmpty()&&!end.isEmpty()){
                //init 自增长列
                sql="select * from \n" +
                        " (select Row_Number() over ( order by getdate() ) as init,* from\n" +
                        "(select iqty,fqty,poqty,isnull(c.ItemNum,d.ItemNum) as ItemNum,isnull(c.ITEMVER,d.ITEMVER) as ITEMVER from\n" +
                        " (select iqty,fqty,isnull(a.ItemNum,b.FMATNUMBER) as ItemNum,isnull(a.ITEMVER,b.FVERNUMBER) as ITEMVER\n" +
                        " from (select sum(IQTY) iqty,ItemNum,ITEMVER from V_APPO_INVALLQRY_INV where StockName<>'废品仓' Group By ItemNum,ITEMVER) a\n" +
                        " full join (select sum(FQTY) fqty,FMATNUMBER,FVERNUMBER from V_APPO_WIPQTY Group By FMATNUMBER,FVERNUMBER) b on a.ItemNum=b.FMATNUMBER and a.ITEMVER=b.FVERNUMBER) c \n" +
                        " full join (select sum(POQTY) poqty,ItemNum,ITEMVER from V_APPO_INVALLQRY_POQTY Group By ItemNum,ITEMVER) d on c.ItemNum=d.ItemNum and c.ITEMVER=d.ITEMVER) e) f \n" +
                        " where f.init between "+begin+" and "+end;
            }
            System.out.println("sql====="+sql);
            List<List<Object>> sResult = InvokeHelper.Query2All(sql);
            return sResult;
        }
        return null;
    }

    //通过http查询采购订单 在途数量(只查询未作废状态和业务终止为正常，同一组织，同一版本和)
    //t_PUR_POOrder e 采购订单表
    //t_PUR_POOrderEntry a 采购订单明细表
    //T_PUR_POORDERENTRY_R b 采购订单明细_关联信息
    //T_BD_AUXPTYVALUEENTITY d 辅助属性表
    //T_BD_FLEXSITEMDETAILV c 辅助属性表
    //e.FPURCHASEORGID 采购组织
    //b.FRemainStockINQty 剩余入库数量（在途）
    //d.FAUXPTYNUMBER 版本
    //e.FCANCELSTATUS 作废状态(A 未作废，B 已作废)
    //a.FMRPTerminateStatus 业务终止(A 正常，B 业务终止)
    //a.FMRPCLOSESTATUS 业务关闭（A 正常，B 业务关闭）
    //a.FMaterialId 物料内码
    public static List<List<Object>> queryNewPoorder(String fMaterialId_GF,String fMaterialId_FM,String companyId,String version) throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
            //在途（剩余入库数量）
            String sql="select sum(b.FRemainStockINQty) FRemainStockINQty from t_PUR_POOrder e\n" +
                    "left join t_PUR_POOrderEntry a on e.fid=a.fid\n" +
                    "left join  T_PUR_POORDERENTRY_R b on a.fid = b.fid and a.FENTRYID=b.FENTRYID\n" +
                    "left join T_BD_FLEXSITEMDETAILV c on a.FAUXPROPID = c.fid\n" +
                    "left join (select * from T_BD_AUXPTYVALUEENTITY where FENTRYID in (select min(FENTRYID) from T_BD_AUXPTYVALUEENTITY group by FAUXPTYID)) d \n" +
                    "on c.ff100001 = d.FAUXPTYID where e.FCANCELSTATUS='A' and a.FMRPTerminateStatus='A' and a.FMATERIALID in ('"+fMaterialId_GF+"','"+fMaterialId_FM+"')  \n" +
                    "and e.FPURCHASEORGID='"+companyId+"' and d.FAUXPTYNUMBER='"+version+"' and e.FCLOSESTATUS='A' and a.FMRPCLOSESTATUS\n" ;
            List<List<Object>> sResult = InvokeHelper.QueryNewPoorder(sql);
            return sResult;
        }
        return null;
    }

    //通过http方式查询光峰，峰米的物料信息
    //查询物料表
    //a.FMATERIALID 物料内码
    //a.F_APPO_ZT 物料状态
    //b.FPLANERID 计划员
    //c.FPURCHASERID 采购员
    //a.FUSEORGID 使用组织
    public static List<List<Object>> query2Materiel(String partNumber) throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {

            String sql="select a.FMATERIALID,a.F_APPO_ZT,b.FPLANERID,c.FPURCHASERID,a.FUSEORGID from T_BD_MATERIAL a \n" +
                    "left join t_BD_MaterialPlan b on a.FMATERIALID=b.FMATERIALID\n" +
                    "left join t_bd_MaterialPurchase c on a.FMATERIALID=c.FMATERIALID\n" +
                    "where a.FNUMBER='"+partNumber+"'";
            List<List<Object>> sResult = InvokeHelper.Query2Materiel(sql);
            return sResult;
        }
        return null;
    }

    //通过物料编码和版本查询版本全部库存数量
    public static List<List<Object>> queryIqty(String partNumber,String version) throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {

            String sql="select sum(IQTY) iqty  from V_APPO_INVALLQRY_INV where Itemnum = '"+partNumber+"'  and ITEMVER = '"+version+"'";
            List<List<Object>> sResult = InvokeHelper.QueryIqty(sql);
            return sResult;
        }
        return null;
    }

    //通过物料编码和版本查询版本全部在途数量
    public static List<List<Object>> queryPoqty(String partNumber,String version) throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {

            String sql="select sum(POQTY) poqty from V_APPO_INVALLQRY_POQTY where Itemnum = '"+partNumber+"'  and ITEMVER = '"+version+"'";
            List<List<Object>> sResult = InvokeHelper.QueryPoqty(sql);
            return sResult;
        }
        return null;
    }

    //通过物料编码和版本查询版本全部在制
    public static List<List<Object>> queryFqty(String partNumber,String version) throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {

            String sql="select sum(FQTY) fqty from V_APPO_WIPQTY where FMATNUMBER= '"+partNumber+"'  and FVERNUMBER = '"+version+"'";
            List<List<Object>> sResult = InvokeHelper.QueryFqty(sql);
            return sResult;
        }
        return null;
    }



    //通过物料内码查询
    //采购价目表formid(新)
    //FCreateOrgId 采购组织(1为光峰，100146为峰米)
    //FApproveDate 审核日期
    //FAuxPropId 辅助属性ID
    //F_APPO_MINPOQTY 最小订货量
    //F_APPO_INCREASEQTY 最小包装量
    //F_APPO_FIXLEADTIME 固定提前期
    //FTaxPrice 含税单价
    //FCurrencyID 币别
    //FEntryExpiryDate 失效日期
    //FMATERIALID 物料内码
    public static List<List<Object>> query2PriceCategory(String fMaterialId_GF,String fMaterialId_FM) throws Exception {
        K3CloudApiClient client = new K3CloudApiClient(K3CloudURL);
        Boolean result = client.login(dbId, uid, pwd, lang);
        if (result) {
            String sContent =
                    "{\"FormId\":\"PUR_PriceCategory\"," +// 采购价目表formid
                            "\"FilterString\":\"FMaterialId in ("+fMaterialId_GF+","+fMaterialId_FM+")\","+// 过滤条件
                            "\"FieldKeys\":\"FCreateOrgId,FApproveDate,FAuxPropId,F_APPO_MINPOQTY,F_APPO_INCREASEQTY,F_APPO_FIXLEADTIME,FTaxPrice,FCurrencyID,FEntryExpiryDate,FMATERIALID\"}";

//            System.out.println(sContent);
            List<List<Object>> sResult = client.executeBillQuery(sContent);
//            System.out.println("采购价目表："+sResult);
            return  sResult;
        }
        return null;
    }



    //通过物料编码查询物料表辅助属性是否开启
    // FAuxPropertyId 100001为版本id,100002为等级
    // FIsEnable1 true为开启，false为未开启
    //FUseOrgId 使用组织 1为光峰
    //FUseOrgId 使用组织 100146 峰米，1 光峰
    public static List<List<Object>> queryFAuxProperty(String partNumber) throws Exception {
        K3CloudApiClient client = new K3CloudApiClient(K3CloudURL);
        Boolean result = client.login(dbId, uid, pwd, lang);
        if (result) {
            String sContent = "{\"FormId\":\"BD_MATERIAL\"," +// 物料formid
                    "\"FilterString\":\"FNumber=\'"+partNumber+"\'\","+// 过滤条件
                    "\"FieldKeys\":\"FAuxPropertyId,FIsEnable1,FUseOrgId,FNumber,FMATERIALID\"}";//FAuxPropertyId 辅助属性，FIsEnable1 启用，FNumber 物料编码,FMATERIALID 物料内码

            List<List<Object>> sResult = client.executeBillQuery(sContent);
//            System.out.println(sContent);
            return  sResult;
        }else {
            throw  new RuntimeException("ERP连接失败，请联系管理员！");
        }
    }




    //通过物料编码查询库存(同一版本，同一组织)
    //a.FBASEQTY 库存量(基本单位)
    //d.FAUXPTYNUMBER 版本
    //a.FSTOCKORGID 库存组织
    public static List<List<Object>> queryNewiQuantity(String fMaterialId_GF,String fMaterialId_FM,String companyId,String version) throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
            //去除T_BD_AUXPTYVALUEENTITY辅助表中重复的数据select * from T_BD_AUXPTYVALUEENTITY where FENTRYID in (select min(FENTRYID) from T_BD_AUXPTYVALUEENTITY group by FAUXPTYID)
            String sql="select sum(a.FBASEQTY) fbaseqty from T_STK_INVENTORY a\n" +
                    "left join T_BD_FLEXSITEMDETAILV c on a.FAUXPROPID = c.fid\n" +
                    "left join (select * from T_BD_AUXPTYVALUEENTITY where FENTRYID in (select min(FENTRYID) from T_BD_AUXPTYVALUEENTITY group by FAUXPTYID)) d \n" +
                    "on c.ff100001 = d.FAUXPTYID where d.FAUXPTYNUMBER='"+version+"' and a.FMATERIALID in ('"+fMaterialId_GF+"','"+fMaterialId_FM+"') and a.FSTOCKORGID='"+companyId+"'";
            List<List<Object>> sResult = InvokeHelper.QueryNewiQuantity(sql);
            return sResult;
        }
        return null;
    }

    //通过币别内码查询币别名称
    public static List<List<Object>> queryFpkName(String fcurrencyid) throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
            //FPKID 币别内码，FNAME 币别名称,FCURRENCYID 币别ID
            String sql="select FNAME from t_bd_currency_l where FCURRENCYID="+fcurrencyid;
            List<List<Object>> sResult = InvokeHelper.QueryFpkName(sql);
            return sResult;
        }
        return null;
    }

    //BD_Rate 汇率表formid
    //通过原币ID查询与人民币的汇率
    //1为人民币，为目标币
    //FRATETYPEID 汇率类型 1固定汇率
    public static List<List<Object>> queryRate(String fcyforid) throws Exception {
        K3CloudApiClient client = new K3CloudApiClient(K3CloudURL);
        Boolean result = client.login(dbId, uid, pwd, lang);
        if (result) {
            String sContent =
                    "{\"FormId\":\"BD_Rate\"," +// 汇率表formid
                            "\"FilterString\":\"FCyForID =\'"+fcyforid+"\' and FCyToID=\'1\' and FRATETYPEID=\'1\' \","+// 过滤条件
                            "\"FieldKeys\":\"FCyForID,FExchangeRate,FReverseExRate,FEndDate,FCyToID,FRATETYPEID,FUseOrgId\"}";//原币，直接汇率，间接汇率,失效日期,目标币，汇率类型,使用组织

//            System.out.println(sContent);
            List<List<Object>> sResult = client.executeBillQuery(sContent);
//            System.out.println("汇率表formid："+sResult);
            return  sResult;
        }
        return null;
    }

    //通过组织ID查询组织名称（1光峰，100146峰米）
    public static List<List<Object>> queryOrgName(String forgId) throws Exception {
        K3CloudApiClient client = new K3CloudApiClient(K3CloudURL);
        Boolean result = client.login(dbId, uid, pwd, lang);
        if (result) {
            String sContent =
                    "{\"FormId\":\"ORG_Organizations\"," +// 组织机构表formid
                            "\"FilterString\":\"FOrgID =\'"+forgId+"\'\","+// 过滤条件
                            "\"FieldKeys\":\"FName\"}";//组织名称

//            System.out.println(sContent);
            List<List<Object>> sResult = client.executeBillQuery(sContent);
//            System.out.println("组织机构表："+sResult);
            return  sResult;
        }
        return null;
    }

    //通过辅助属性ID查询辅助属性版本
    public static List<List<Object>> queryFversion(String fauxpropId) throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {

            String sql="select FAUXPTYNUMBER from\n" +
                    "T_BD_FLEXSITEMDETAILV c \n" +
                    "left join (select * from T_BD_AUXPTYVALUEENTITY where FENTRYID in (select min(FENTRYID) from T_BD_AUXPTYVALUEENTITY group by FAUXPTYID)) d \n" +
                    "on c.ff100001 = d.FAUXPTYID where c.fid="+fauxpropId;
            List<List<Object>> sResult = InvokeHelper.QueryFversion(sql);
            return sResult;
        }
        return null;
    }

//    //通过物料内码查询订单表
//    //t_PUR_POOrder e 采购订单表
//    //t_PUR_POOrderEntry a 采购订单明细表
//    //T_PUR_POORDERENTRY_R b 采购订单明细_关联信息
//    //T_BD_AUXPTYVALUEENTITY 辅助属性表
//    //T_BD_FLEXSITEMDETAILV c 辅助属性表
//    //e.FPURCHASEORGID 采购组织(1为光峰，100146为峰米)
//    //a.FBASEUNITQTY 采购数量(基本)
//    //b.FSTOCKBASESTOCKINQTY 累计入库数量(库存基本)
//    //d.FAUXPTYNUMBER 版本
//    //f.FSETTLECURRID 币别
//    //e.FCANCELSTATUS (作废状态 A 未作废)
//    //在途数量=采购数量-累计入库数量(库存基本)（需累加）
//    public static List<List<Object>> queryNewPoorder(String fMaterialId) throws Exception {
//        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
//            //去除T_BD_AUXPTYVALUEENTITY辅助表中重复的数据select * from T_BD_AUXPTYVALUEENTITY where FENTRYID in (select min(FENTRYID) from T_BD_AUXPTYVALUEENTITY group by FAUXPTYID)
//            String sql="select e.FPURCHASEORGID,a.FBASEUNITQTY,b.FSTOCKBASESTOCKINQTY,d.FAUXPTYNUMBER,f.FSETTLECURRID from t_PUR_POOrder e\n" +
//                    "left join t_PUR_POOrderEntry a on e.fid=a.fid\n" +
//                    "left join (select * from T_PUR_POORDERENTRY_R where FENTRYID in (select min(FENTRYID) from T_PUR_POORDERENTRY_R group by FID)) b on a.fid = b.fid\n" +
//                    "left join T_PUR_POORDERFIN f on b.fid=f.fid\n" +
//                    "left join T_BD_FLEXSITEMDETAILV c on a.FAUXPROPID = c.fid\n" +
//                    "left join (select * from T_BD_AUXPTYVALUEENTITY where FENTRYID in (select min(FENTRYID) from T_BD_AUXPTYVALUEENTITY group by FAUXPTYID)) d \n" +
//                    "on c.ff100001 = d.FAUXPTYID where e.FCANCELSTATUS='A' and a.FMATERIALID="+fMaterialId;
//            List<List<Object>> sResult = InvokeHelper.QueryNewPoorder(sql);
//            return sResult;
//        }
//        return null;
//    }

    //通过物料内码查询价目表(失效日期未失效,与PLM物料版本相同)
    //t_PUR_PriceListEntry a 采购价目明细表
    //t_PUR_PriceList b 采购价目表
    //T_BD_AUXPTYVALUEENTITY 辅助属性表
    //T_BD_FLEXSITEMDETAILV c 辅助属性表
    //b.FCREATEORGID 采购组织(1为光峰，100146为峰米)
    //b.FApproveDate 审核日期
    //d.FAUXPTYNUMBER 版本
    //a.F_APPO_MINPOQTY 最小订货量
    //a.F_APPO_INCREASEQTY 最小包装量
    //a.F_APPO_FIXLEADTIME 固定提前期
    //a.FTaxPrice 含税单价
    //b.FCURRENCYID 币别
    //a.FEXPIRYDATE 失效日期
    //a.FMATERIALID 物料内码
    //b.FDOCUMENTSTATUS 单据状态(A 创建，B 审核中，C 已审核)
    public static List<List<Object>> queryNewPriceCategory(String fMaterialId_GF,String fMaterialId_FM,String version) throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
            //去除T_BD_AUXPTYVALUEENTITY辅助表中重复的数据select * from T_BD_AUXPTYVALUEENTITY where FENTRYID in (select min(FENTRYID) from T_BD_AUXPTYVALUEENTITY group by FAUXPTYID)
            String sql="select b.FCREATEORGID,b.FApproveDate,d.FAUXPTYNUMBER,a.F_APPO_MINPOQTY,a.F_APPO_INCREASEQTY,\n" +
                    "a.F_APPO_FIXLEADTIME,a.FTaxPrice,b.FCURRENCYID,a.FEXPIRYDATE,a.FMATERIALID from t_PUR_PriceListEntry a\n" +
                    "left join t_PUR_PriceList b on a.fid = b.fid\n" +
                    "left join T_BD_FLEXSITEMDETAILV c on a.FAUXPROPID = c.fid\n" +
                    "left join (select * from T_BD_AUXPTYVALUEENTITY where FENTRYID in (select min(FENTRYID) from T_BD_AUXPTYVALUEENTITY group by FAUXPTYID)) d \n" +
                    "on c.ff100001 = d.FAUXPTYID where a.FMATERIALID in ('"+fMaterialId_GF+"','"+fMaterialId_FM+"') and a.FEXPIRYDATE>getdate() and d.FAUXPTYNUMBER='"+version+"' and b.FDOCUMENTSTATUS='C'";
            List<List<Object>> sResult = InvokeHelper.QueryNewPriceCategory(sql);
            return sResult;
        }
        return null;
    }

    //通过接口查询采购订单(只查询未作废状态和业务终止为正常)
    // FTaxPrice 含税单价
    //FRemainStockINQty 剩余入库数量（在途）
    //FMRPTerminateStatus 业务终止(A 正常，B 业务终止)
    // FAuxPropId 辅助属性ID
    // FCANCELSTATUS 作废状态(A 未作废，B 已作废)
    // FPURCHASEORGID 采购组织
    //FSETTLECURRID 币别
    //FApproveDate 审核日期
    //FMaterialId 物料内码
    public static List<List<Object>> query2Poorder(String FMaterialId) throws Exception {
        K3CloudApiClient client = new K3CloudApiClient(K3CloudURL);
        Boolean result = client.login(dbId, uid, pwd, lang);
        if (result) {
            String sContent =
                    "{\"FormId\":\"PUR_PurchaseOrder\"," +//采购订单表formid
//                            "\"FilterString\":\"FMaterialId =\'"+FMaterialId+"\'\","+// 过滤条件
                            "\"FilterString\":\"FMRPTerminateStatus ='A' and FCANCELSTATUS='A' and FMaterialId =\'"+FMaterialId+"\'\","+// 过滤条件
                            "\"FieldKeys\":\"FTaxPrice,FRemainStockINQty,FMRPTerminateStatus,FAuxPropId,FCANCELSTATUS,FPURCHASEORGID,FSETTLECURRID,FApproveDate,FMaterialId\"}";

            List<List<Object>> sResult = client.executeBillQuery(sContent);//("Kingdee.BOS.WebApi.ServicesStub.DynamicFormService.Save", new Object[]{sFormId,sContent},String.class);
//            System.out.println("采购订单===" + sResult);
            return sResult;
        }
        return  null;
    }

    //查询组织是否存在BOM版本， FUSEORGID 使用组织，FNUMBER BOM版本
    public static List<List<Object>> queryisBOM(String FUSEORGID,String FNUMBER) throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
            //FUSEORGID 组织内码，1光峰，100146峰米
            String sql="select * from T_ENG_BOM where FUSEORGID="+FUSEORGID +" and FNUMBER='"+FNUMBER+"'";
            List<List<Object>> sResult = InvokeHelper.QueryisBOM(sql);
            return sResult;
        }
        return null;
    }

    //查询组织是否存在BOM版本， FUSEORGID 使用组织，FNUMBER BOM版本
    public static List<List<Object>> queryBOMFid(String FUSEORGID,String FNUMBER) throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
            //FUSEORGID 组织内码，1光峰，100146峰米
            String sql="select FID from T_ENG_BOM where FUSEORGID="+FUSEORGID +" and FNUMBER='"+FNUMBER+"'";
            List<List<Object>> sResult = InvokeHelper.QueryBOMFid(sql);
            return sResult;
        }
        return null;
    }
//    String sql27="select FID from T_ENG_BOM where FUSEORGID=100146 and FNUMBER='"+FNUMBER+"'";

    //查询 BOM下FENTRYID子项内码
    public static List<List<Object>> queryFENTRYID(String parentBOM,String childrenNumber,String FUSEORGID) throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
            //FUSEORGID 组织内码，1光峰，100146峰米
            String sql="SELECT  T2.FENTRYID  FROM T_ENG_BOM T1 \n" +
                    "INNER JOIN T_ENG_BOMCHILD T2 ON T1.FID = T2.FID \n" +
                    "INNER JOIN T_BD_MATERIAL T3 ON T2.FMATERIALID = T3.FMATERIALID\n" +
                    "WHERE T1.FNUMBER = '"+parentBOM+"' AND T3.FNUMBER IN ('"+childrenNumber+"') and T1.FUSEORGID='"+FUSEORGID+"'";
            List<List<Object>> sResult = InvokeHelper.QueryAll(sql);
            return sResult;
        }
        return null;
    }

    //通过物料编码查询
    //采购价目表formid(按审核日期降序)
    public static List<List<Object>> queryPriceCategory(String fMaterialId) throws Exception {
        K3CloudApiClient client = new K3CloudApiClient(K3CloudURL);
        Boolean result = client.login(dbId, uid, pwd, lang);
        if (result) {
            String sContent =
                    "{\"FormId\":\"PUR_PriceCategory\"," +// 采购价目表formid
                            "\"FilterString\":\"FMaterialId =\'"+fMaterialId+"\'\","+// 过滤条件
                            "\"OrderString\":\"FApproveDate  DESC\","+  // 排序条件（ASC 升序,DESC 降序）
                            "\"FieldKeys\":\"F_APPO_MINPOQTY,F_APPO_INCREASEQTY,F_APPO_FIXLEADTIME,FApproveDate,FTaxPrice,FSupplierID\"}";//最小订货量,最小包装量,固定提前期,审核日期,含税单价,供应商,FMaterialId物料内码

//            System.out.println(sContent);
            List<List<Object>> sResult = client.executeBillQuery(sContent);
//            System.out.println("采购价目表："+sResult);
            return  sResult;
        }
        return null;
    }


    //通过供应商id查询供应商表id 的 供应商名
    //供应商表单id
    public static List<List<Object>> querySupplier(String fsupplierId) throws Exception {
        K3CloudApiClient client = new K3CloudApiClient(K3CloudURL);
        Boolean result = client.login(dbId, uid, pwd, lang);
        if (result) {
            String sContent = "{\"FormId\":\"BD_Supplier\"," +// 供应商formid
                    "\"FilterString\":\"FSupplierId=\'"+fsupplierId+"\'\","+// 过滤条件
                    "\"FieldKeys\":\"FName\"}";// FName,FShortName 供应商名，供应商简称

            List<List<Object>> sResult = client.executeBillQuery(sContent);
//            System.out.println(sContent);
//            System.out.println("供应商id："+fsupplierId+" 供应商名：" + sResult);
            return  sResult;
        }
        return null;
    }

    //通过物料编码查询物料表
    public static List<List<Object>> queryMateriel(String partNumber) throws Exception {
        K3CloudApiClient client = new K3CloudApiClient(K3CloudURL);
        Boolean result = client.login(dbId, uid, pwd, lang);
        if (result) {
            String sContent = "{\"FormId\":\"BD_MATERIAL\"," +// 物料formid
                    "\"FilterString\":\"FNumber=\'"+partNumber+"\'\","+// 过滤条件
//                    "\"FilterString\":\"FUseOrgId=\'"+1+"\'\","+// 过滤条件 使用组织FUseOrgId, 1为光峰
            "\"FieldKeys\":\"FMATERIALID ,F_APPO_ZT,FPlanerID,FPurchaserId,FUseOrgId\"}";//FMATERIALID 物料内码 ,F_APPO_ZT 物料状态,计划员,采购员,使用组织

            List<List<Object>> sResult = client.executeBillQuery(sContent);
//            System.out.println(sContent);
//            System.out.println("物料返回结果："+ sResult);
            return  sResult;
        }
        return null;
    }


    //通过物料内码查询(降序)
    //T_PUR_POORDER(采购订单)，T_PUR_POORDERENTRY(采购订单明细)
    public static List<List<Object>> queryPoorder(String fMaterialId) throws Exception {
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
            //在途（基本单位数量-库存基本数量）
            //FBaseUnitQty采购数量(基本) - FBASESTOCKINQTY累计入库数量（基本）
            String sql="select t1.FBaseUnitQty,t2.FBASESTOCKINQTY,t1.FAUXPROPID from T_PUR_POORDERENTRY_R t2  with(nolock) join T_PUR_POOrderEntry t1 with(nolock) on t2.FID=t1.FID and t1.FMATERIALID="+fMaterialId;

            //FBASEUNITQTY 基本单位数量  //FSTOCKBASEQTY 库存基本数量 //FSTOCKQTY 库存数量 //FPURCHASERID 采购员 //FDATE 采购日期
//            String sql="select t1.FBASEUNITQTY,t1.FSTOCKBASEQTY,t1.FSTOCKQTY,t2.FPURCHASERID,t2.FDATE from T_PUR_POOrder t2  with(nolock) join T_PUR_POOrderEntry t1 with(nolock) on t2.FID=t1.FID and t2.FCANCELSTATUS='A' and t1.FMATERIALID="+fMaterialId+"order by t2.FDATE desc";
//            System.out.println("在途sql:"+sql);
            List<List<Object>> sResult = InvokeHelper.Query(sql);
//            System.out.println("在途："+sResult);
            return sResult;
        }
        return null;
    }

    //通过采购员ID,计划员ID 查询对应名称
    //通过FPURCHASERID 采购员ID(FENTRYID 分录内码) 查询 T_BD_OPERATORENTRY(业务员分录表) 的 职员ID，再通过 职员ID 查询 T_BD_STAFF_L(员工多语言表) 的 名称FNAME
    public static List<List<Object>> queryName(String fentryId) throws Exception {
//        HashMap map= (HashMap) readProperty.readProperty();
//        K3CloudApiClient client = new K3CloudApiClient((String) map.get("K3CloudURL"));
//        String dbId=(String) map.get("dbId");
//        String uid=(String) map.get("uid");
//        String pwd=(String) map.get("pwd");
//        int lang=(int) map.get("lang");
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
            String sql="select FNAME from T_BD_STAFF_L where FSTAFFID=(select FStaffId from T_BD_OPERATORENTRY where FENTRYID="+fentryId+")";
//            System.out.println("员工名sql:"+sql);
            List<List<Object>> sResult = InvokeHelper.Query2(sql);
//            System.out.println("员工名："+sResult);
            return sResult;
        }
        return null;
    }

    //通过物料编码查询 ，FQTY 基本单位数量(在制) ，FVERNUMBER 版本编码，FMATNUMBER 物料编码
    public static List<List<Object>> queryWipqty(String partNumber) throws Exception {
//        HashMap map= (HashMap) readProperty.readProperty();
//        K3CloudApiClient client = new K3CloudApiClient((String) map.get("K3CloudURL"));
//        String dbId=(String) map.get("dbId");
//        String uid=(String) map.get("uid");
//        String pwd=(String) map.get("pwd");
//        int lang=(int) map.get("lang");
        if (InvokeHelper.Login(dbId, uid, pwd, lang)) {
            String sql="select * from V_APPO_WIPQTY where FMATNUMBER='"+partNumber+"'";
//            String sql="select * from V_APPO_WIPQTY where FMATNUMBER='S05000000056'";
//            System.out.println("在制sql:"+sql);
            List<List<Object>> sResult = InvokeHelper.Query3(sql);
//            System.out.println("在制："+sResult);
            return sResult;
        }
        return null;
    }

    //查询库存
    public static List<List<Object>> queryiQuantity(String FMaterialId) throws Exception {
        K3CloudApiClient client = new K3CloudApiClient(K3CloudURL);
        Boolean result = client.login(dbId, uid, pwd, lang);
        if (result) {
            String sContent =
                    "{\"FormId\":\"STK_Inventory\"," +// 即时库存表formid
//                                "\"FilterString\":\"FMaterialId =\'"+fMaterialId+"\'\","+// 过滤条件
//                                "\"OrderString\":\"FApproveDate DESC\"," +// 排序条件
                            "\"FilterString\":\"FMaterialId =\'"+FMaterialId+"\'\","+// 过滤条件
                            "\"FieldKeys\":\"FBaseQty,FAuxPropId,FStockOrgId,FMaterialId\"}";//库存量(基本单位)：FBaseQty,FAuxPropId 辅助属性 ID,FStockOrgId 库存组织,FMaterialId 物料内码

            List<List<Object>> sResult = client.executeBillQuery(sContent);//("Kingdee.BOS.WebApi.ServicesStub.DynamicFormService.Save", new Object[]{sFormId,sContent},String.class);
//            System.out.println("查询库存" + sResult);
            return sResult;
        }
        return  null;
    }


//    //在制 T_PRD_MOENTRY 生产订单表体 //T_PRD_MOENTRY_Q 生产订单明细数量表
//    public static List<List<Object>> queryMoentry(String FMATERIALID) throws Exception {
//        K3CloudApiClient client = new K3CloudApiClient(K3CloudURL);
//        Boolean result = client.login(dbId, uid, pwd, lang);
//        if (result) {
//            //FMATERIALID 物料内码 //FNOSTOCKINQTY 未入库数量
//            String sql="select t1.FNOSTOCKINQTY,t2.FMATERIALID from T_PRD_MOENTRY t2 with(nolock) join T_PRD_MOENTRY_Q t1 with(nolock) on t2.FID=t1.FID";
//            List<List<Object>> sResult = InvokeHelper.Query2(sql);
//            return sResult;
//        }
//        return null;
//    }

}


