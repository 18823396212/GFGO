package ext.appo.ecn.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.Workbook;

import wt.method.RemoteMethodServer;
import wt.util.WTException;

public class GenerateMain {

	 public static void main(String[] args) throws WTException, IOException {
		 
		 RemoteMethodServer server = RemoteMethodServer.getDefault();
		 server.setUserName("wcadmin");
		 server.setPassword("wcadmin");
		 
		 GenerateAffObjExcelUtil genAffObj = new GenerateAffObjExcelUtil();
		 Workbook wb = genAffObj.exportReport();
		 String outPath = "/ptc/Windchill_11.0/Windchill/temp";
	
		 File file = new File(outPath);
         FileOutputStream fileOutputStream = new FileOutputStream(file);
         wb.write(fileOutputStream);
	}
	 
	 
	
}
