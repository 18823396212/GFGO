package ext.appo.test.tools;

import ext.appo.common.util.Excel2007Handler;
import wt.util.WTProperties;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadPartExcel {
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

    public  static List ReadPartNumber() throws IOException {
        List list=new ArrayList();
        String filePath = WT_CODEBASE + File.separator + "ext" + File.separator + "appo" + File.separator+ "test" + File.separator + "tools" + File.separator  + "excelB.xlsx";
        System.out.println("读取excelB表地址："+filePath);
        Excel2007Handler handler = new Excel2007Handler(filePath);
        handler.switchCurrentSheet(0);
        int rowCount = handler.getSheetRowCount();
        for (int i = 1; i < rowCount; i++) {
            list.add(null2blank(handler.getStringValue(i, 0)));
        }

//        System.out.println("读取K表:"+map.toString());
        return list;
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
