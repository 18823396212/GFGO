//package ext.appo.erp.service;
//
//
//import ext.appo.common.util.Excel2003Handler;
//import ext.appo.common.util.Excel2007Handler;
//import ext.appo.erp.bean.SendMessage;
//import ext.appo.erp.util.ExportAllItems;
//import ext.appo.util.PartUtil;
//import ext.generic.integration.erp.common.CommonPDMUtil;
//import ext.generic.integration.erp.common.CommonUtil;
//import ext.generic.integration.erp.service.BOMReleaseService;
//import wt.fc.collections.WTKeyedHashMap;
//import wt.part.WTPart;
//import wt.util.WTProperties;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Vector;
//
//public class SendUpdateBOMToU8 {
//
//    private static String WT_CODEBASE = "";
//
//    static {
//        WTProperties wtproperties;
//        try {
//            wtproperties = WTProperties.getLocalProperties();
//            WT_CODEBASE = wtproperties.getProperty("wt.codebase.location");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void main(String[] args) {
//        send();
//
//    }
//
//    public static void send() {
//
//        WTKeyedHashMap hashMap=new WTKeyedHashMap();
//        String partNumber1 = "";//物料编码
//        String isSuccess1 = "";//标志
//        String message1 = "";//详情
//
//        List list2=new ArrayList();
//        Vector dataList1=new Vector();
//        String date1=new Date().toString();
//        String count1="0";
//        String type1="BOMupdate_" + date1;
//
//        try {
//            list2=getChangebomTable();
//            System.out.println("获取到的表父件编码数量："+list2.size());
//            for (int i = 0; i <list2.size() ; i++) {
//                System.out.println("list1"+i+"==="+list2.get(i));
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        if (list2!=null&&list2.size()>1){
//            //去除重复元素
//            list2=removeDuplicate(list2);
//        }
//        System.out.println("去重复后list数量："+list2.size());
//        for (int i = 0; i <list2.size() ; i++) {
//            System.out.println("去重复后list1"+i+"==="+list2.get(i));
//        }
//
//        for (int i = 0; i <list2.size() ; i++) {
//            System.out.println("获取前:"+list2.get(i));
//            //通过物料编码获取物料
//            WTPart part= ext.appo.util.PartUtil.getLastestWTPartByNumber( String.valueOf(list2.get(i)));
//            System.out.println("获取到的part==="+part+";获取到的partNumber==="+part.getNumber());
//            partNumber1=part.getNumber();
//
//            //将更新的BOM发到中间表
//            // 发布BOM结构
//            String releaseTime = CommonUtil.getCurrentTime() ;
//            System.out.println("获取到的releaseTime==="+releaseTime);
//            hashMap = BOMReleaseService.releaseSingleLevel(part, releaseTime, null, CommonPDMUtil.getBatchNumber(null));
//            System.out.println("获取到的hashMap==="+hashMap);
//            SendMessage sendMessage = new SendMessage();
//            message1 = hashMap.toString();
//            if( hashMap!= null&&hashMap.size() > 0 ){
//                isSuccess1 = "失败";
//            } else {
//                isSuccess1 = "成功";
//                message1 = "BOM发送成功";
//            }
//            sendMessage.setPartNumber(partNumber1);//物料编码
//            sendMessage.setIsSuccess(isSuccess1);//标志
//            sendMessage.setMessage(message1);//详情
//            dataList1.add(sendMessage);
//        }
//
//        if (dataList1.size() > 0){
//            count1=String.valueOf(dataList1.size());
//        }
//
//        ExportAllItems exportAllItems2 = new ExportAllItems(dataList1);
//        exportAllItems2.doExport(dataList1,count1,type1);
//
//
//    }
//
//    //去除重复元素
//    public static List removeDuplicate(List list) {
//        for (int i = 0; i < list.size() - 1; i++) {
//
//            for (int j = list.size() - 1; j > i; j--) {
//
//                if (list.get(j).equals(list.get(i))) {
//
//                    list.remove(j);
//                }
//            }
//        }
//        return  list;
//    }
//
//
//
//
//
//    /*****
//     * 读取changebom表
//     * @return
//     * @throws IOException
//     */
//    public static List getChangebomTable() throws IOException {
//        List list=new ArrayList();
//        System.out.println("读取changebom.xls地址");
//        String filePath = WT_CODEBASE + File.separator + "temp" +  File.separator + "changebom.xls";
//        System.out.println("更新BOM地址："+filePath);
//        Excel2003Handler handler = new Excel2003Handler(filePath);
//        handler.switchCurrentSheet(0);
//        int rowCount = handler.getSheetRowCount();
////    System.out.println("产品线读表rowCount:"+rowCount);
//        for (int i = 1; i < rowCount; i++) {
//            list.add(null2blank(handler.getStringValue(i, 1)));
//        }
//        return list;
//    }
//
//    public static String null2blank(Object obj){
//        if(obj == null){
//            return "";
//        }else{
//            String tmp = obj.toString();
//            return tmp.trim();
//        }
//    }
//
//}