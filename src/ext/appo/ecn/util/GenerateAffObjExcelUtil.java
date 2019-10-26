package ext.appo.ecn.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Workbook;

import ext.appo.common.util.Excel2007Handler;
import ext.appo.ecn.common.util.ChangePartQueryUtils;
import ext.appo.ecn.common.util.ChangeUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.ecn.pdf.PdfUtil;
import wt.change2.AffectedActivityData;
import wt.change2.ChangeActivityIfc;
import wt.change2.ChangeOrder2;
import wt.change2.Changeable2;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.lifecycle.State;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.vc.config.LatestConfigSpec;

public class GenerateAffObjExcelUtil {

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
		private static final String Vector = null;
		
//		  private static String ecoNumber = "";
//		  private static String ecoInitiator = "";
//		  private static String chanType = "";
//		  private static String chanTypeDes = "";
//		  private static String partName = "";
//		  private static String partNumber = "";
//		  private static String version = "";
//		  private static String partState = "";
//		  private static String chanType = "";
//		  private static String WT_CODEBASE = "";
//		  private static String WT_CODEBASE = "";
//		  private static String WT_CODEBASE = "";
//		  private static String WT_CODEBASE = "";
//		  private static String WT_CODEBASE = "";
//		  private static String WT_CODEBASE = "";


		public static Vector getAllEC(String number) throws Exception
		{
			QuerySpec qs = new QuerySpec(ChangeOrder2.class);
			if(number==null)
				return null;
			if (number.trim().length() > 0)
			{
			
				SearchCondition scNumber = new SearchCondition(ChangeOrder2.class, ChangeOrder2.NUMBER, SearchCondition.EQUAL, number.toUpperCase());
				qs.appendWhere(scNumber);
			}

			SearchCondition scLatestIteration = new SearchCondition(ChangeOrder2.class, WTAttributeNameIfc.LATEST_ITERATION, SearchCondition.IS_TRUE);
		
			qs.appendWhere(scLatestIteration);

			QueryResult qr = PersistenceHelper.manager.find(qs);
			if (qr != null && qr.hasMoreElements())
				qr = (new LatestConfigSpec()).process(qr);

			if (qr != null && qr.hasMoreElements())
				return qr.getObjectVectorIfc().getVector();

			return new Vector();
		}
	
		
		public Workbook exportReport() throws WTException {
			 try{
				 
				    String strTemplate = WT_CODEBASE + File.separator + "config" + File.separator + "custom" + File.separator + "GenerateAffObjTemplate.xlsx";
		        	Excel2007Handler excelHander = new Excel2007Handler(strTemplate);            
		        	List<Map<String, String>> dataPartList = generateData();
		            excelHander.switchCurrentSheet("Sheet1");
		            for (int i = 0; i < dataPartList.size(); i++) {
		            	int rowNum = i+1;
		            	int iCol = 0;
		            	Map<String, String> dataPartMap = dataPartList.get(i);
		            	
		            	excelHander.setStringValue(rowNum, iCol++, dataPartMap.get("ecoNumber").toString());
		            	excelHander.setStringValue(rowNum, iCol++, dataPartMap.get("ecoInitiator").toString());
		            	excelHander.setStringValue(rowNum, iCol++, dataPartMap.get("ChangeCause").toString());
		            	excelHander.setStringValue(rowNum, iCol++, dataPartMap.get("chanTypeDes").toString());
		            	excelHander.setStringValue(rowNum, iCol++, dataPartMap.get("partName").toString());
		            	excelHander.setStringValue(rowNum, iCol++, dataPartMap.get("partNumber").toString());
		            	excelHander.setStringValue(rowNum, iCol++, dataPartMap.get("version").toString());
		            	excelHander.setStringValue(rowNum, iCol++, dataPartMap.get("partState").toString());
		            	excelHander.setStringValue(rowNum, iCol++, dataPartMap.get("chanType").toString());
		            	excelHander.setStringValue(rowNum, iCol++, dataPartMap.get("des").toString());
		            	excelHander.setStringValue(rowNum, iCol++, dataPartMap.get("zzsl").toString());
		            	excelHander.setStringValue(rowNum, iCol++, dataPartMap.get("zzslcs").toString());
		            	excelHander.setStringValue(rowNum, iCol++, dataPartMap.get("ztsl").toString());
		            	excelHander.setStringValue(rowNum, iCol++, dataPartMap.get("ztclcs").toString());	
		            	excelHander.setStringValue(rowNum, iCol++, dataPartMap.get("kcsl").toString());
		            	excelHander.setStringValue(rowNum, iCol++, dataPartMap.get("kcslcs").toString());
		            	excelHander.setStringValue(rowNum, iCol++, dataPartMap.get("ychclcs").toString());
		            	excelHander.setStringValue(rowNum, iCol++, dataPartMap.get("qwwcsj").toString());
		            	excelHander.setStringValue(rowNum, iCol++, dataPartMap.get("qesponsiblePerson").toString());
		            	excelHander.setStringValue(rowNum, iCol++, dataPartMap.get("collecObj").toString());
		            }
		             
		            return excelHander.getWorkbook();
		        } catch(Exception e){
		        	e.printStackTrace();
		        	throw new WTException(e.getLocalizedMessage());
		        }
		}
		
	        public static List<Map<String,String>> generateData() throws Exception {
	        	Vector vector = getAllEC("");
	        	System.out.println("vector=="+vector.size());
	        	List<Map<String,String>> list = new ArrayList<>();
	        	for (int i = 0; i < vector.size(); i++) {
	        		WTChangeOrder2  eco =(WTChangeOrder2)vector.get(i);
	        	
	        		Collection<Persistable> returnArray = new HashSet<Persistable>() ;
	        		Map<ChangeActivityIfc, Collection<Changeable2>> ecaDatasMap = ChangeUtils.getChangeablesBeforeInfo(eco) ;
	        		for(ChangeActivityIfc ca: ecaDatasMap.keySet()){
	        			Collection<Changeable2> cl = ecaDatasMap.get(ca);
	        			for(Persistable per: cl){
	        				returnArray.add(per);
	        			}
	        		}
	            
	            		if(!returnArray.isEmpty()){
	            			for(Persistable per : returnArray){
	            				if(per instanceof WTPart){
	            					WTPart part = (WTPart)per;
	            					Map<String,String> map = new HashMap<>();
	            					
	            					
	            				    map.put("ecoNumber", eco.getNumber());               //ECO编号
	            				    
	            				    String  sendPersion   = eco.getCreatorFullName();
	            			    	//String sendPersionCount = sendPersion.replaceAll("\\d+","");
	            			       // String[] sendPersionArry = sendPersionCount.split("\\|");
	            				    map.put("ecoInitiator",  sendPersion == null ? "" :   sendPersion);            //发起人
	            				    
	            				    
	            				    System.out.println("ecn numer===="+eco.getNumber());
	            				    System.out.println("ChangeCause========="+PdfUtil.getIBAObjectValue(eco, "ChangeCause"));
	            				     Object object=PdfUtil.getIBAObjectValue(eco, "ChangeCause");
	            				     if(object instanceof String ){
	            				    String changeComment = (String)PdfUtil.getIBAObjectValue(eco, "ChangeCause");
	            				    map.put("ChangeCause", changeComment == null ? "" :   changeComment);  }
	            				     else{
	            				    	 map.put("ChangeCause", ""); 
	            				     }
	            				     //变更原因
	            				    
	            				    String comment = eco.getDescription();	            				    
	            				    map.put("chanTypeDes", comment == null ? "" :   comment);                       //变更原因说明
	            				    
	            				    map.put("partName", part.getName());                       //物料名称
	            				    map.put("partNumber", part.getNumber());                   //物料编号
	            				    
	            				    String version = part.getVersionInfo().getIdentifier().getValue();
	            			        String iteration = part.getIterationInfo().getIdentifier().getValue();
	            				    map.put("version", version+"."+iteration);                   //物料版本
	            				    
	            				    State s = State.toState(part.getState().toString());
	            				    String partState = s.getDisplay(SessionHelper.getLocale());
	            				    map.put("partState",   partState == null ? "" :   partState);  //物料状态     
	            				    
	            				    String partTypeName = (String)PdfUtil.getIBAObjectValue(part, "ChangeType");
	            			    	//String[] typeArr = partTypeName.split(";");
	            				    map.put("chanType", partTypeName == null ? "" :   partTypeName);  //类型
	            				    
	            				    String des = "";
	            			        Collection<ChangeActivityIfc> changeActivities = ChangeUtils.getChangeActivities(eco) ;
	            					for(ChangeActivityIfc changeActivityIfc : changeActivities){
	            						AffectedActivityData affectedActivityData = ChangeUtils.getAffectedActivity(changeActivityIfc, part) ;
	            						if(affectedActivityData != null){
	            							des = affectedActivityData.getDescription();
	            						}
	            					}
	            				    map.put("des", des == null ? "" :  des);                      //说明
	            		
	            				    String articleInventory = (String)PdfUtil.getIBAObjectValue(part, "ArticleInventory");
	            				    
	            				    map.put("zzsl", articleInventory == null ? "" :  articleInventory);         //在制数量
	            				    
	            				    String articleDispose = (String)PdfUtil.getIBAObjectValue(part, "ArticleDispose");
	            			    	//String[] articleArr = articleDispose.split(";");	
	            				    map.put("zzslcs", articleDispose == null ? "" : articleDispose);          //在制处理措施
	            				    
	            				    String passageInventory = (String)PdfUtil.getIBAObjectValue(part, "PassageInventory");
	            				    map.put("ztsl", passageInventory == null ? "" :  passageInventory);          //在途数量
	            				    

	            				    String passageDispose = (String)PdfUtil.getIBAObjectValue(part, "PassageDispose");
	            				    //String[] passageArr = passageDispose.split(";");	
	            				   
	            				    map.put("ztclcs", passageDispose == null ? "" : passageDispose); //在途处理措施

	    
	            				    String centralWarehouseInventory = (String)PdfUtil.getIBAObjectValue(part, "CentralWarehouseInventory"); 
	            				    map.put("kcsl", centralWarehouseInventory == null ? "" :  centralWarehouseInventory); //库存数量
	      

	            				    String inventoryDispose = (String)PdfUtil.getIBAObjectValue(part, "InventoryDispose");
	            				    //String[] inventoryArr = inventoryDispose.split(";");	
	            				    map.put("kcslcs", inventoryDispose== null ? "" :  inventoryDispose); //库存处理措施
	            				    
	            				    String productDispose = (String)PdfUtil.getIBAObjectValue(part, "ProductDispose");
	            				   // String[] productArr = productDispose.split(";");	
	            				    map.put("ychclcs", productDispose == null ? "" :   productDispose);//已出货处理措施
	            				    

	            				    String completionTime = (String)PdfUtil.getIBAObjectValue(part, "CompletionTime");
	            				    
	            				    map.put("qwwcsj", completionTime == null ? "" :   completionTime);//期望完成时间
	            				    
	            				    String responsiblePerson = (String)PdfUtil.getIBAObjectValue(part, "ResponsiblePerson");
	            				   // count = responsiblePerson.replaceAll("\\d+","");
	            				  //  String[] arry = count.split("\\|");
	            				    
	            				    map.put("qesponsiblePerson",  responsiblePerson == null ? "" :   responsiblePerson);//责任人

	            				    String collecObj = getChildsNumber(part, eco);
	            				    map.put("collecObj",  collecObj == null ? "" :   collecObj);//收集对象
	            				    list.add(map);
	            				}
	            			}
	            		}
	        	}
	        	return list;
	        }
	        
	        /***
	    	 * 获取存在受影响对象列表的部件编码
	    	 * 
	    	 * @param paramModelContext
	    	 * @param parentPart
	    	 * @return
	    	 * @throws WTException
	    	 */
	    	public static String getChildsNumber(WTPart parentPart,WTChangeOrder2 eco) throws WTException{
	    		StringBuilder returnStr = new StringBuilder() ;
	    		Collection<WTPart> childArray = ChangePartQueryUtils.getPartMultiwallStructure(parentPart) ;
	    		if(childArray == null || childArray.size() == 0){
	    			return returnStr.toString() ;
	    		}
	    		
	    		Collection<Changeable2> changeablesBefore = ChangeUtils.getChangeablesBefore(eco) ;
	    		for(Changeable2 changeable2 : changeablesBefore){
	    			if(changeable2 instanceof WTPart){
	    				for(WTPart childPart : childArray){
	    					if(ChangeUtils.getNumber(changeable2).equals(childPart.getNumber())){
	    						if(returnStr.length() > 0){
	    							returnStr.append(ChangeConstants.USER_KEYWORD) ; 
	    						}
	    						returnStr.append(childPart.getNumber()) ;
	    						break ;
	    					}
	    				}
	    			}
	    		}
	        	return returnStr.toString() ;
	    	}
	
}
