package ext.appo.erp.util;

import ext.appo.common.util.Excel2007Handler;
import wt.util.WTProperties;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ReadProperty {
    private static String WT_CODEBASE = "";

    static {
        WTProperties wtproperties;
        try {
            wtproperties = WTProperties.getLocalProperties();
            WT_CODEBASE = wtproperties.getProperty("wt.codebase.location");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  static Map<String, String> ReadProperty() throws IOException {
        Map<String,String> map = new HashMap<>();

        String filePath = WT_CODEBASE + File.separator + "config" + File.separator + "custom" + File.separator + "K3.xlsx";
//        System.out.println("读取K表地址："+filePath);
        Excel2007Handler handler = new Excel2007Handler(filePath);
        handler.switchCurrentSheet(0);
        int rowCount = handler.getSheetRowCount();
        for (int i = 1; i < rowCount; i++) {

            map.put(null2blank(handler.getStringValue(i, 0)), null2blank(handler.getStringValue(i, 1)));
        }

//        System.out.println("读取K表:"+map.toString());
        return map;
    }


    public static String null2blank(Object obj){
        if(obj == null){
            return "";
        }else{
            String tmp = obj.toString();
            return tmp.trim();
        }
    }
}
