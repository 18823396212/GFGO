package ext.appo.doc.uploadDoc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import wt.part.WTPart;
public class ShareDirectory {

	
	   public static void main(String[] args) throws IOException {
		  
		   
		    Map<String,String> map = new HashMap<String,String>();
       	    map.put("folderID", "123");
            map.put("count", "1");
            map.put("folderID", "1234");
            map.put("count", "1");
            
            System.out.println(map.get("count"));
			
	   }
	   

	    public static void setTxt(String val){
	    	try {
	    		
	    		File fp=new File("C:/360/1.txt");
	    		PrintWriter pw=new PrintWriter(fp);		

				
					 pw.println(val);
					 pw.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	
}
