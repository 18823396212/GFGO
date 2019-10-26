package ext.appo.doc.uploadDoc;

import ext.appo.common.util.Excel2007Handler;
import wt.util.WTProperties;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReadSendDocData {

//    private static String codebalocation;
//    private static String filePath;

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
    
//    static {
//        try {
//            codebalocation = PICoreHelper.service.getCodebase();
//            filePath = codebalocation + File.separator + "ext" + File.separator + "appo" + File.separator + "doc"  + File.separator + "sendDocData.xlsx";
//        } catch (PIException e) {
//            e.printStackTrace();
//        }
//    }

    public static void main(String[] args) throws IOException{
        Map<String,SendDocDataBean> t = getSendDocDataBeans();
        System.out.println(t);
    }
    public static Map<String,SendDocDataBean> getSendDocDataBeans() throws IOException{
        Map<String,SendDocDataBean> map = new HashMap<>();
        
        String filePath = WT_CODEBASE + File.separator + "config" + File.separator + "custom" + File.separator + "sendDocData.xlsx";
        Excel2007Handler handler = new Excel2007Handler(filePath);
        //Excel2007Handler handler = new Excel2007Handler("D:\\ideaspace\\gfgd\\windchill\\codebase\\ext\\appo\\doc\\sendDocData.xlsx");
        handler.switchCurrentSheet(0);
        int rowCount = handler.getSheetRowCount();
        for (int i = 1; i < rowCount; i++) {
            SendDocDataBean sddb = new SendDocDataBean();
            sddb.setSno(null2blank(handler.getStringValue(i, 0)));                         //序号
            sddb.setDocType(null2blank(handler.getStringValue(i, 1)));                     //文档类型
            sddb.setDocTypeName(null2blank(handler.getStringValue(i, 2)));                 //文档类型名称
//            sddb.setDocSmallType(null2blank(handler.getStringValue(i, 3)));                //文档小类
            sddb.setDocSmallTypeName(null2blank(handler.getStringValue(i, 3)));            //文档小类名称
            sddb.setDescOrRef(null2blank(handler.getStringValue(i, 4)));                   //文档关系类型
            sddb.setIsSendDcc(null2blank(handler.getStringValue(i, 5)));                   //是否发送dcc
            sddb.setSendType(null2blank(handler.getStringValue(i, 6)));                    //发送类型
            sddb.setComment(null2blank(handler.getStringValue(i, 7)));                     //备注
            if(StringUtils.isNoneEmpty(sddb.getDocType())){
                map.put(sddb.getDocType(), sddb);
            }
        }
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
