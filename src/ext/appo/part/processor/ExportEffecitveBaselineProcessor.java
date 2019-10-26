package ext.appo.part.processor;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ext.appo.common.util.Excel2007Handler;
import ext.appo.ecn.pdf.PdfUtil;
import ext.appo.part.util.EffecitveBaselineUtil;
import wt.part.QuantityUnit;
import wt.part.WTPart;
import wt.part.WTPartUsageLink;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTProperties;

public class ExportEffecitveBaselineProcessor {
	
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

	    public Workbook exportReport(String oid,String datatime) throws WTException {
	    	boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
	    	try{
	    		System.out.println("Oid======================================================="+oid);
	    		String strTemplate = WT_CODEBASE + File.separator + "config" + File.separator + "custom" + File.separator + "EffecitveBaselineTemplate.xlsx";
	        	Excel2007Handler excelHander = new Excel2007Handler(strTemplate);            
	        	List<List<String>> listBean = checkData(oid,datatime);
	        	System.out.println("listBean======================"+listBean);
	            excelHander.switchCurrentSheet("Sheet1");
	            XSSFWorkbook book  = excelHander.getWorkbook();
	            XSSFSheet sheet = book.getSheetAt(0);
	            XSSFCellStyle style = book.createCellStyle();  
	            style.setBorderBottom(CellStyle.BORDER_THIN);  
	            style.setBorderLeft(CellStyle.BORDER_THIN);  
	            style.setBorderRight(CellStyle.BORDER_THIN);  
	            style.setBorderTop(CellStyle.BORDER_THIN);  
	           
	            int rowNum = 0;
	            
			            if(!listBean.isEmpty()) {
			            for (int i = 0; i < listBean.size(); i++) {
			            	rowNum = i+4;
			            	int iCol = 1;
			            	List<String> dataPartMap = listBean.get(i);
			            	
			            	
			            	XSSFRow row = sheet.getRow(rowNum);
			            	if (row == null){
			            		row = sheet.createRow(rowNum);
			                }
			          
			            	for(int j =0; j < dataPartMap.size(); j++){
			            		int l = j;
			            	    Cell cecll = row.getCell(l);
			            	    //cecll.set= 
			            	    if(cecll == null){
			            	    	cecll = row.createCell(l);
			                    }
			            		cecll.setCellValue(dataPartMap.get(j));
			            		cecll.setCellStyle(style);
			            	}
			            }
	            }
	            return excelHander.getWorkbook();
	        } catch(Exception e){
	        	e.printStackTrace();
	        	throw new WTException(e.getLocalizedMessage());
	        }finally {
	        	SessionServerHelper.manager.setAccessEnforced(flag); 
	        }
	}
	    
	    public static List<List<String>> checkData(String oid,String datatime) throws IOException, WTException{
			  PreparedStatement pstmt;
			   List<List<String>> listBean = new ArrayList<>();
		        try {
		        	 Connection conn = EffecitveBaselineUtil.getConn();
		        	 System.out.println("checkFolderId====================="+conn);    
		        	 String sql = "select n.*,level  from  ED_EffectiveBaseline n  CONNECT BY  PRIOR beanUID = upUID start WITH partvid = '"+oid+"' and CREATETIME='"+datatime+"'";

		            System.out.println("sql========================="+sql);
		            pstmt = (PreparedStatement)conn.prepareStatement(sql);
		            System.out.println("checkFolderId============pstmt========="+pstmt);
		            ResultSet rs = pstmt.executeQuery();
		            System.out.println("checkFolderId============rs========="+rs);
		            //5.处理ResultSet
		            int i =1;
		            while(rs.next()){
//		            	i++;
		            	int level = rs.getInt(18);
		            	level--;
		                //rs.get+数据库中对应的类型+(数据库中对应的列别名)
		            	List<String> list = new ArrayList<>();
		            	list.add(String.valueOf(i++));
		            	list.add(String.valueOf(level));
		            	System.out.println("rs.getString(16)======================"+rs.getString(16));
		            	System.out.println("rs.getString(17)=========================="+rs.getString(17));
		            	if(level == 0){
		            		String partOid = rs.getString(4);
		            		WTPart part = (WTPart)EffecitveBaselineUtil.getObjectByOid(partOid);
		            		String partVersion = part.getVersionInfo().getIdentifier().getValue();
			 		        String partIteration = part.getIterationInfo().getIdentifier().getValue();        
		            		list.add(part.getNumber());
		            		list.add("");
			            	list.add("");
			            	list.add(partVersion+"."+partIteration);
			            	list.add(part.getName());
			            	String ggms = (String)PdfUtil.getIBAObjectValue(part, "ggms");
			            	list.add(ggms == null ? "" :   ggms);
//			            	WTPartUsageLink  link=(WTPartUsageLink)EffecitveBaselineUtil.getObjectByOid(rs.getString(14));
//			            	String stockGrade = (String)PdfUtil.getIBAObjectValue(link, "stockGrade");
//			            	list.add(stockGrade == null ? "" :   stockGrade);
//			            	QuantityUnit unit =   link.getQuantity().getUnit();
//			            	Double amount = link.getQuantity().getAmount();
//			            	list.add(String.valueOf(unit.getDisplay(Locale.CHINA)) == null ? "" :   String.valueOf(unit.getDisplay(Locale.CHINA)));
//			            	list.add(String.valueOf(amount) == null ? "" :   String.valueOf(amount));
//			            	String placeNumber = EffecitveBaselineUtil.getReferenceDesignatorSet(link);
//			            	list.add(placeNumber == null ? "" :   placeNumber);
//			            	String bomNote = (String)PdfUtil.getIBAObjectValue(link, "bom_note");
//			            	list.add(bomNote == null ? "" :   bomNote);
			            	list.add("");
			            	list.add("");
			            	list.add("");
			            	list.add("");
			            	list.add("");
			            	listBean.add(list);
			            	if(rs.getString(16) != null && !rs.getString(16).contains("null")&& rs.getString(16) != "") {
			            		String[] subArry = rs.getString(16).split("\\,");
			            		for(int j=0;j<subArry.length;j++) {
			            			String number = subArry[j].toString();
			            			WTPart subpart = EffecitveBaselineUtil.getPartByNumberAndVersion(number);
			            			if(subpart != null) {
			            			List<String> list2 = new ArrayList<>();
			            			list2.add(String.valueOf(i++));
			            			list2.add("");
			            			list2.add("");
			            			list2.add(subpart.getNumber());
			            			list2.add("局部替代");
			            			list2.add("");
			            			list2.add(subpart.getName());
			            			list2.add("");
			            			list2.add("");
			            			list2.add("");
			            			list2.add("");
			            			list2.add("");
			            			list2.add("");
			            			listBean.add(list2);
			            			}
			            		}
			            	}
			            	
			            	if(rs.getString(17) != null && !rs.getString(17).contains("null") && rs.getString(17) != "") {
			            		String[] aftArry = rs.getString(17).split("\\,");
			            		for(int j=0;j<aftArry.length;j++) {
			            			String number = aftArry[j].toString();
			            			WTPart aftpart = EffecitveBaselineUtil.getPartByNumberAndVersion(number);
			            			if(aftpart != null) {
			            			List<String> list2 = new ArrayList<>();
			            			list2.add(String.valueOf(i++));
			            			list2.add("");
			            			list2.add("");
			            			list2.add(aftpart.getNumber());
			            			list2.add("全局替代");
			            			list2.add("");
			            			list2.add(aftpart.getName());
			            			list2.add("");
			            			list2.add("");
			            			list2.add("");
			            			list2.add("");
			            			list2.add("");
			            			list2.add("");
			            			listBean.add(list2);
			            			}
			            		}
			            	}
		            	}else {
		            		String partOid = rs.getString(4);
		            		WTPart part = (WTPart)EffecitveBaselineUtil.getObjectByOid(partOid);
		            		String partVersion = part.getVersionInfo().getIdentifier().getValue();
			 		        String partIteration = part.getIterationInfo().getIdentifier().getValue();        
		            		list.add(part.getNumber());
		            		list.add("");
			            	list.add("");
			            	list.add(partVersion+"."+partIteration);
			            	list.add(part.getName());
			            	String ggms = (String)PdfUtil.getIBAObjectValue(part, "ggms");
			            	list.add(ggms == null ? "" :   ggms);
			            	WTPartUsageLink  link=(WTPartUsageLink)EffecitveBaselineUtil.getObjectByOid(rs.getString(14));
			            	String stockGrade = (String)PdfUtil.getIBAObjectValue(link, "stockGrade");
			            	list.add(stockGrade == null ? "" :   stockGrade);
			            	QuantityUnit unit =   link.getQuantity().getUnit();
			            	String am = getCountByE(String.valueOf(link.getQuantity().getAmount()));
			            	list.add(String.valueOf(unit.getDisplay(Locale.CHINA)) == null ? "" :   String.valueOf(unit.getDisplay(Locale.CHINA)));
			            	list.add(am == null ? "" :   am);
			            	String placeNumber = EffecitveBaselineUtil.getReferenceDesignatorSet(link);
			            	list.add(placeNumber == null ? "" :   placeNumber);
			            	String bomNote = (String)PdfUtil.getIBAObjectValue(link, "bom_note");
			            	list.add(bomNote == null ? "" :   bomNote);
			            	listBean.add(list);
			            	
			            	if(rs.getString(16) != null && !rs.getString(16).contains("null")&& rs.getString(16) != "") {
			            		String[] subArry = rs.getString(16).split("\\,");
			            		for(int j=0;j<subArry.length;j++) {
			            			String number = subArry[j].toString();
			            			WTPart subpart = EffecitveBaselineUtil.getPartByNumberAndVersion(number);
			            			if(subpart != null) {
			            			List<String> list2 = new ArrayList<>();
			            			list2.add(String.valueOf(i++));
			            			list2.add("");
			            			list2.add("");
			            			list2.add(subpart.getNumber());
			            			list2.add("局部替代");
			            			list2.add("");
			            			list2.add(subpart.getName());
			            			list2.add("");
			            			list2.add("");
			            			list2.add("");
			            			list2.add("");
			            			list2.add("");
			            			list2.add("");
			            			listBean.add(list2);
			            			}
			            		}
			            	}
			            	
			            	if(rs.getString(17) != null && !rs.getString(17).contains("null") && rs.getString(17) != "") {
			            		String[] aftArry = rs.getString(17).split("\\,");
			            		for(int j=0;j<aftArry.length;j++) {
			            			String number = aftArry[j].toString();
			            			WTPart aftpart = EffecitveBaselineUtil.getPartByNumberAndVersion(number);
			            			if(aftpart != null) {
			            			List<String> list2 = new ArrayList<>();
			            			list2.add(String.valueOf(i++));
			            			list2.add("");
			            			list2.add("");
			            			list2.add(aftpart.getNumber());
			            			list2.add("全局替代");
			            			list2.add("");
			            			list2.add(aftpart.getName());
			            			list2.add("");
			            			list2.add("");
			            			list2.add("");
			            			list2.add("");
			            			list2.add("");
			            			list2.add("");
			            			listBean.add(list2);
			            			}
			            		}
			            	}
		            	}
		            	
//		            	bean.setBeanUID(rs.getString(1));
//		                bean.setProcessoid(rs.getString(2));
//		                bean.setUpUID(rs.getString(3));
//		            	bean.setPartoid(rs.getString(4));
//		                bean.setPartvid(rs.getString(5));
//		            	bean.setPartNumber(rs.getString(6));
//		            	bean.setPartName(rs.getString(7));
//		            	bean.setPartState(rs.getString(8));
//		                bean.setParentoid(rs.getString(9));
//		            	bean.setParentvid(rs.getString(10));
//	                    bean.setParenNumber(rs.getString(11));
//		            	bean.setParenName(rs.getString(12));
//		            	bean.setParenState(rs.getString(13));
//		            	bean.setUsageLinkoid(rs.getString(14));
//		            	bean.setCreateTime(rs.getString(15));
//		            	bean.setLevel(rs.getString(16));
//		            	listBean.add(bean);
		            }
		            conn.close();
		        } catch (SQLException e) {
			        e.printStackTrace();
			    }
		        return listBean;
		    }
	    
	    /**
		 * 处理科学计数法显示问题.
		 */
		public static String getCountByE(String amount){
		    String regx = "^((-?\\d+.?\\d*)[Ee]{1}(-?\\d+))$";//科学计数法正则表达式
	        Pattern pattern = Pattern.compile(regx);
	        
	        boolean flag = pattern.matcher(amount).matches();
	        if(flag){
				BigDecimal db = new BigDecimal(amount.toString());
				return db.toPlainString();
	        }else{
	        	return amount;
	        }
		}
		
		
		
		 public static List<String> checkDataCount(String oid,String datatime) throws IOException, WTException{
			  PreparedStatement pstmt;
			   List<String> listBean = new ArrayList<>();
		        try {
		        	 Connection conn = EffecitveBaselineUtil.getConn();
		        	 System.out.println("checkFolderId====================="+conn);    
		        	 String sql = "select n.*,level  from  ED_EffectiveBaseline n  CONNECT BY  PRIOR beanUID = upUID start WITH partvid = '"+oid+"' and CREATETIME='"+datatime+"'";

		            System.out.println("sql========================="+sql);
		            pstmt = (PreparedStatement)conn.prepareStatement(sql);
		            System.out.println("checkFolderId============pstmt========="+pstmt);
		            ResultSet rs = pstmt.executeQuery();
		            System.out.println("checkFolderId============rs========="+rs);
		            //5.处理ResultSet
		            int i =0;
		            while(rs.next()){
                   
		            	listBean.add(rs.getString(1));
		            }
		            conn.close();
		        } catch (SQLException e) {
			        e.printStackTrace();
			    }
		        return listBean;
		    }
	               
	
}
