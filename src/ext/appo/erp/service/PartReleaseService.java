package ext.appo.erp.service;

import ext.appo.erp.bean.MaterialInfo;
import ext.appo.erp.util.KingDeeK3Helper;
import ext.appo.erp.util.PartUtil;
import ext.com.iba.IBAUtil;
import ext.pi.core.PIAttributeHelper;
import kingdee.bos.json.JSONObject;
import org.apache.log4j.Logger;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTKeyedHashMap;
import wt.iba.value.IBAHolder;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.util.WTException;
import wt.vc.VersionControlHelper;

import java.util.Date;
import java.util.Vector;

import static ext.appo.erp.util.BomUtil.getBomHistoricalData;
import static ext.appo.erp.util.PartUtil.getIBAvalue;


public class PartReleaseService {

    private static final Logger logger = LogR.getLogger(PartReleaseService.class.getName());


    /**
     * 发布零部件物料信息(发送物料流程调用入口)
     *
     * @param parts 物料集合
     * @param self  流程
     * @return
     * @throws Exception
     *parts=====================
     */
    public static WTKeyedHashMap releasePersistable(WTArrayList parts, WTObject pbo, ObjectReference self) throws Exception {
        WTKeyedHashMap hashMap = new WTKeyedHashMap();
        System.out.println("物料上的pbo=="+pbo);
        System.out.println("物料size=="+parts.size());
        System.out.println("parts===========ERP===============" + parts);
        if (parts == null||parts.size()<=0) {
            return hashMap;
        }
        try {
            int size = parts.size();
            for (int i = 0; i < size; i++) {
                Persistable persistable = parts.getPersistable(i);

                if (persistable != null && persistable instanceof WTPart) {
                    WTPart part = (WTPart) persistable;

                    //如果物料存在有ECN，就不发送（测试时跳过）
//                    if (ErpUtil.isHaveECN(part)){
//                        System.out.println("============存在ECN==============");
//                        continue;
//                    }
                    //A开头物料的设计视图只发A版（手工发送不做限制）
                    String partNumber=part.getNumber();
                    String str=partNumber.substring(0, 1);
                    String view= part.getView().getName();
                    Vector hVector=new Vector();
                    System.out.println("物料"+part.getNumber()+"是否A开头物料且是设计视图==="+(str.equals("A")&&view.equals("Design")));
                    if (str.equals("A")&&view.equals("Design")){
                        String mVersion=part.getVersionIdentifier().getValue();//物料大版本
                        if (!mVersion.equals("A")){
                            //不是A版不发
                            continue;
                        }else{
                            //A版判断是否M视图是否发过K3，发过不发
                            hVector= getBomHistoricalData(part, "Manufacturing");
                            if (hVector!=null&&hVector.size()>0){
                                for (int j = 0; j <hVector.size() ; j++) {
                                    String isSend=getIBAvalue((WTPart)hVector.get(j),"bomReleaseStatus");
                                    if (isSend.equals("PDM发布成功")){
                                        continue;
                                    }

                                }
                            }
                        }
                    }

//                    //清空属性
//                    PIAttributeHelper.service.forceUpdateSoftAttribute(part, "erpErrorMsg", "");
//                    PIAttributeHelper.service.forceUpdateSoftAttribute(part, "partReleaseStatus", "");
//                    PIAttributeHelper.service.forceUpdateSoftAttribute(part, "bomReleaseStatus", "");

//                    System.out.println("===执行发送方法==");
                    //发送物料
                    hashMap.putAll(sendMaterial(part));
                }
            }
        } catch (WTException e) {
            e.printStackTrace();

            System.out.println("===发送的物料ERRO==");
        }

        return hashMap;
    }



    //发送物料到ERP
    public static WTKeyedHashMap sendMaterial(WTPart part) {

//        System.out.println("===============发送物料到ERP====================");
//        String version = part.getVersionInfo().getIdentifier().getValue() + "." + part.getIterationInfo().getIdentifier().getValue();//物料版本

//        System.out.println("传过来的物料版本"+version);
        //如果不是最新版本
        if (!part.isLatestIteration()) {
            try {
                part = (WTPart) VersionControlHelper.service.getLatestIteration(part, false);
            } catch (Exception e2) {
                System.out.println("============获取出错=============");
                e2.printStackTrace();
            }
        }
//        String version1 = part.getVersionInfo().getIdentifier().getValue() + "." + part.getIterationInfo().getIdentifier().getValue();//物料版本

//        System.out.println("结束后的物料版本"+version1);
//        System.out.println("========最新=========="+part);
        WTKeyedHashMap hashMap = new WTKeyedHashMap();



//        boolean canRelease = BussinessRule.canReleasePart(part);
////        System.out.println("=============部件是否可发布=============="+canRelease);
////        logger.debug("部件是否可发布：" + canRelease);
////        if (canRelease) {



//            if (part.isPhantom()) {
//                //如果部件为虚拟件，判断用户是否需要发布虚拟件
//                if (BusinessRuleXMLConfigUtil.getInstance().getIsReleasePhantom().equals("false")) {
//                    return hashMap;
//                }
//            }
//            if (part.getHidePartInStructure()) {
//                //如果部件为收集件，判断用户是否需要发布收集件
//
//                if (BusinessRuleXMLConfigUtil.getInstance().getIsReleaseHidePartInStructure().equals("false")) {
//                    System.out.println("=============收集件==============");
//                    return hashMap;
//                }
//            }
//


            String data = "";
            try {

//              System.out.println("=============创建materialInfo==============");
//                System.out.println("开始创建materialInfo并转为json方法:"+new Date());
                MaterialInfo  materialInfo = PartUtil.createMaterilInfoBeanByPart(part);

//                System.out.println("===materialInfo===="+materialInfo);

                //将物料信息对象转JSON
               // data = KingDeeK3Helper.materialInfoToJson(materialInfo);//标准接口

                data = KingDeeK3Helper.getMaterialJsonData(materialInfo);
//                System.out.println("结束创建materialInfo并转为json方法:"+new Date());

            } catch (Exception e1) {
                e1.printStackTrace();
                hashMap.put(part, "创建物料失败" );
//                System.out.println("=============创建materialInfo失败==============");
                return hashMap;
            }

            try {

//                System.out.println("++++++++++++++++++发送物料+++++++++++++++++++++++++++");
//                System.out.println("data数据："+data);
                //发送物料
               // String msg = KingDeeK3Helper.saveMaterial(data);//标准接口
//                System.out.println("开始调用金蝶发送物料接口："+new Date());
                String msg = KingDeeK3Helper.AddMaterials(data);
//                System.out.println("结束调用金蝶发送物料接口："+new Date());

//                System.out.println("msg=="+msg);
                //是否发送成功
                Boolean isSuccess = isSendSuccess(msg);

                if (!isSuccess) {
                    String error = errorInfo(msg);
//                    System.out.println("报错信息error=="+error);
                    throw new RuntimeException(error);
                }

            } catch (Exception e) {
                e.printStackTrace();
                hashMap.put(part, "物料保存失败" + e.toString());

            }

            if (hashMap.size() > 0) {
                System.out.println("++++++++++++++++++物料发布出现异常，判断物料发布失败+++++++++++++++++++++++++++");
                //发布出现异常，发布状态为：物料发布失败
//                UpdatePDMDataService.updatePDMFailed(part, "partReleaseStatus");
                updatePDMFailed(part, "partReleaseStatus");
                return hashMap;
            } else {
                //更新时，如果没有出现异常则判断为更新成功
                System.out.println("++++++++++++++++++更新成功+++++++++++++++++++++++++++");
//                UpdatePDMDataService.updatePDMSucessful(part, "partReleaseStatus");
                updatePDMSucessful(part, "partReleaseStatus");
            }

        System.out.println("最后返回的报错信息："+hashMap);
        return hashMap;
    }





//解析发送后返回的json，返回是否发送成功
public static Boolean isSendSuccess(String data) {

    //去空格
    data = data.replace("\\s","");
    //去回车
    data = data.replace("\n","");
    System.out.println("====MSG===="+data);

    JSONObject jsonObject = new JSONObject(data);


    String is = jsonObject.getString("Result");

    if (is.equals("1")) {//1成功，0失败
        return true;
    }else {
        return false;
    }

}

public static String errorInfo(String data) {

    System.out.println("Msg=="+data);
    String msg ="无法解析返回的报错信息";
    try {
        //去空格
        data = data.replace("\\s","");
        //去回车
        data = data.replace("\n","");
        System.out.println("Msg2=="+data);
        JSONObject jsonObject = new JSONObject(data);
        msg = jsonObject.getString("Msg");
        System.out.println("Msg3=="+msg);
    }catch (Exception e){
        return msg;
    }

    return msg;

}


    public static void updatePDMFailed(IBAHolder ibaHolder , String ibaName ){

        updatePDMData( ibaHolder , ibaName , "PDM发布失败"  ) ;

    }



    public static void updatePDMSucessful(IBAHolder ibaHolder , String ibaName ){
        updatePDMData( ibaHolder , ibaName , "PDM发布成功" );
    }


    protected static void updatePDMData(IBAHolder ibaHolder , String ibaName , String ibaValue ){
        if( ibaHolder != null ){
            try {
                Object obj = IBAUtil.getIBAValue(ibaHolder, ibaName);

                if( obj == null || ( (String)obj ).trim().isEmpty() || !( (String)obj ).trim().equals( ibaValue ) ){
                    IBAUtil.forceSetIBAValue(ibaHolder, ibaName, ibaValue ) ;
                }

            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }




}
