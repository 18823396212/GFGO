package ext.appo.doc.uploadDoc;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import ext.appo.common.util.Excel2007Handler;
import wt.util.WTProperties;

public class DataConfigurationPacketTable {
	
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

	    public static void main(String[] args) throws IOException{
	        Map<String,String> t = getSendDocDataBeans();
	        
	        String[] arrtPort = t.get("dataPort").split("\\.");
	        
	        System.out.println(t.get("userName"));
	        System.out.println(t.get("password"));
	        System.out.println(t.get("dataAddress"));
	        System.out.println(arrtPort[0]);
	        System.out.println(t.get("dataStorageRoom"));
	    }
	    
	    /**
	     * 读取光峰数据库
	     * @return
	     * @throws IOException
	     */
	    public static Map<String,String> getSendDocDataBeans() throws IOException{
	        Map<String,String> map = new HashMap<>();
	        
	        String filePath = WT_CODEBASE + File.separator + "config" + File.separator + "custom" + File.separator + "jdbcDataConfigure.xlsx";
	        Excel2007Handler handler = new Excel2007Handler(filePath);
//	        Excel2007Handler handler = new Excel2007Handler("C:\\jdbcDataConfigure.xlsx");
	        handler.switchCurrentSheet(0);
	        int rowCount = handler.getSheetRowCount();
	        for (int i = 1; i < rowCount; i++) {
	            if(i>6){
	            	 break;
	            }
	            map.put("userName", null2blank(handler.getStringValue(1, 1)));
	            map.put("password", null2blank(handler.getStringValue(2, 1)));
	            map.put("dataAddress", null2blank(handler.getStringValue(3, 1)));
	            map.put("dataPort", null2blank(handler.getStringValue(4, 1)));
	            map.put("dataStorageRoom", null2blank(handler.getStringValue(5, 1)));
	            map.put("mappingPath", null2blank(handler.getStringValue(6, 1)));
	        }
	        return map;
	    }
	    
	    /**
	     * 读取绎立数据库
	     * @return
	     * @throws IOException
	     */
	    public static Map<String,String> getYLSendDocDataBeans() throws IOException{
	        Map<String,String> map = new HashMap<>();
	        
	        String filePath = WT_CODEBASE + File.separator + "config" + File.separator + "custom" + File.separator + "jdbcDataConfigure.xlsx";
	        Excel2007Handler handler = new Excel2007Handler(filePath);
//	        Excel2007Handler handler = new Excel2007Handler("C:\\jdbcDataConfigure.xlsx");
	        handler.switchCurrentSheet(0);
	        int rowCount = handler.getSheetRowCount();
	        for (int i = 1; i < rowCount; i++) {
	            if(i>6){
	            	 break;
	            }
	            map.put("userName", null2blank(handler.getStringValue(1, 3)));
	            map.put("password", null2blank(handler.getStringValue(2, 3)));
	            map.put("dataAddress", null2blank(handler.getStringValue(3, 3)));
	            map.put("dataPort", null2blank(handler.getStringValue(4, 3)));
	            map.put("dataStorageRoom", null2blank(handler.getStringValue(5, 3)));
	            map.put("mappingPath", null2blank(handler.getStringValue(6, 3)));
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
