package ext.appo.doc.uploadDoc;

import java.beans.PropertyVetoException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;

import com.lowagie.text.DocumentException;
import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.appo.common.util.Excel2007Handler;
import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.ecn.pdf.PdfUtil;
import ext.com.iba.IBAUtil;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.content.Streamed;
import wt.doc.WTDocument;
import wt.enterprise.Master;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.inf.container.WTContainer;
import wt.inf.library.WTLibrary;
import wt.org.WTUser;
import wt.pds.StatementSpec;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.vc.config.LatestConfigSpec;
import wt.workflow.work.WorkItem;
import wt.workflow.work.WorkflowHelper;

/**
 * 历史数据----文档
 * @author rdpplm
 *
 */
public class UploadHistoricalDataDoc{
	private final static Logger logger = Logger.getLogger(UploadHistoricalDataDoc.class.getName());
	private final static List<String> listExcept = new ArrayList<>();
	
	private static String WT_CODEBASE = "";
    static {
        WTProperties wtproperties;
        try {
            wtproperties = WTProperties.getLocalProperties();
            WT_CODEBASE = wtproperties.getProperty("wt.codebase.location");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage(),e);
        }
    }
//    
//    public static void main(String[] args) throws WTException {
//    	 try {
//			uploadContentData();
//		} catch (DocumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			logger.error(e.getMessage(),e);
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			logger.error(e.getMessage(),e);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
    
//    @Override
//	public FormResult doOperation(NmCommandBean nmCommandBean, List<ObjectBean> objectBeans) throws WTException {
//    	FormResult doOperation = super.doOperation(nmCommandBean, objectBeans);
//    	boolean flag = SessionServerHelper.manager.setAccessEnforced(false); // 忽略权限
//    	try {
//    		try {
//				uploadContentData();
//				setTxt(listExcept.toString());
//			} catch (Exception e) {
//				e.printStackTrace();
//				logger.error(e.getMessage(),e);
//			}
//    	}finally {
//    		SessionServerHelper.manager.setAccessEnforced(flag); // 添加权限
//    	}
//    	return doOperation;
//    }
	
	  public static Set<String>  uploadContentData() throws Exception{
		  
		  boolean flag = SessionServerHelper.manager.setAccessEnforced(false); // 忽略权限
		  Map<WTDocument,String> mapDoc = new HashMap<>();
		  Set<String> setCount = new HashSet<>();
		  Map<WTDocument,String> mapYLDoc = new HashMap<>();
		  Set<FileAttachListYLBean> listYLBean = new HashSet<>();
		  System.out.println("=======================action=======================");
		  try{
		  Map<String,SendDocDataBean> map = ReadSendDocData.getSendDocDataBeans();
		  int j= 0;
			
		    Set<FileAttachListBean> listBean = new HashSet<>();
			Set<WTDocument> setYesProcess = new HashSet<>();
			Set<WTDocument> setNoProcess = new HashSet<>();
			Vector vector = getAllLatestWTDocs("");
	      	  System.out.println("vector=="+vector.size());
	      	  for (int i = 0; i < vector.size(); i++) {
	      		WTDocument  doc =(WTDocument)vector.get(i);
	      	  if(doc.getLifeCycleState().toString().equals(ChangeConstants.ARCHIVED) || doc.getLifeCycleState().toString().equals(ChangeConstants.RELEASED)){
//			Set<String> docNumSet = getDocData();
//			for(String partNum : docNumSet){
//				WTDocument doc = getDocByNumber(partNum);
				if(doc != null){
				doc = (WTDocument)DocUploadAttachmentContent.getLatestVersionByMaster((Master)doc.getMaster());
                     
				Set<WorkItem> set = new HashSet<>(); 
				QueryResult qr = WorkflowHelper.service.getWorkItems(doc);
				while(qr.hasMoreElements()){
		        WorkItem wi = (WorkItem)qr.nextElement();
		        set.add(wi);
				}
			if(!set.isEmpty()){			    	
				setYesProcess.add(doc); 
				
//				for(String key: map.keySet()){
//					SendDocDataBean bean = map.get(key);
//					if(bean.getDescOrRef().equals("参考文档")){
						TypeIdentifier typeIdentifier = TypeIdentifierUtility.getTypeIdentifier(doc);
						String typename = typeIdentifier.getTypename();
						if (typename.contains("com.ptc.ReferenceDocument")) {
							System.out.println("doc============="+doc);
//							String[] arry = key.split("__");
							String type = UploadAttachmentUtil.getTypeInternalName(doc);
							String passageInventory = (String)PdfUtil.getIBAObjectValue(doc, "SmallDocType");
							System.out.println("type======="+type);
							System.out.println("passageInventory======="+passageInventory);
//							System.out.println("arry================"+arry.toString());
							if(map.keySet().toString().contains(type)){
						  
								//==========================================主内容action====================
								if(!doc.getContainerName().equals("SL-舞台灯产品库")){
									//==========================================主内容action====================
									ContentItem ci = ContentHelper.service.getPrimary(doc);
									if (ci != null) {
										ApplicationData data = (ApplicationData) ci;                            //主内容
										System.out.println("ApplicationData======="+data);
										Map<String,String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
										  String downPath = mapPath.get("mappingPath");
										 String name = data.getFileName();                                       //文件名--取原始名称
										 String str = name.substring(name.lastIndexOf("."), name.length());
										  if(!str.equalsIgnoreCase(".PDF") && !str.equalsIgnoreCase(".pptx") && !str.equalsIgnoreCase(".ppt") 
													 && !str.equalsIgnoreCase(".xls") && !str.equalsIgnoreCase(".xlsx") 
													 && !str.equalsIgnoreCase(".doc") && !str.equalsIgnoreCase(".docx")){
												 UUID u = UUID.randomUUID();
												 String[] arryName = name.split("\\.");
												 String conversionName = u.toString()+str;
												 System.out.println("conversionName=========="+conversionName);
													System.out.println("name=================="+name);
													DocUploadAttachmentContent.downContexts(downPath, data, conversionName);                       		  	//下载文件											
													//得到符合要求的数据
													j++;
													mapDoc.put(doc, String.valueOf(j));
													FileAttachListBean fileBean = new  FileAttachListBean();
													fileBean.setFileID("");
													fileBean.setAttachName(name);
													fileBean.setGuidName(u.toString()+str);
													fileBean.setExtensionName(str);
													fileBean.setUerNo(doc.getNumber());
													fileBean.setFileSize(String.valueOf(data.getFileSize()));
													listBean.add(fileBean);
													setCount.add(UploadHistoricalData.getOidByObject(doc)+"___GF");
//													mapCount.put(doc, "GF");
										  }
									}
									//=======================================主内容end==========================
							 
									//======================================附件action============================================
									QueryResult pdf = ContentHelper.service.getContentsByRole(doc, ContentRoleType.SECONDARY);
								
									while (pdf.hasMoreElements()) {
										ApplicationData data = (ApplicationData) pdf.nextElement();
										System.out.println("ApplicationData======="+data);
										Map<String,String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
										  String downPath = mapPath.get("mappingPath");
										String name = data.getFileName();
										System.out.println("name==============="+name);
										String str = name.substring(name.lastIndexOf("."), name.length());
										 if(str.equalsIgnoreCase(".PDF") || str.equalsIgnoreCase(".pptx") || str.equalsIgnoreCase(".ppt") 
												 || str.equalsIgnoreCase(".xls") || str.equalsIgnoreCase(".xlsx")
												 || str.equalsIgnoreCase(".doc") || str.equalsIgnoreCase(".docx")){
											 boolean isprint =name.startsWith("PRINT");
											 if(isprint){
													UUID u = UUID.randomUUID();
													String[] arryName = name.split("\\.");
													String conversionName = u.toString()+str;
													System.out.println("conversionName=========="+conversionName);
													DocUploadAttachmentContent.downContexts(downPath, data, conversionName);
													j++;
													//得到符合要求的数据
													mapDoc.put(doc, String.valueOf(j));
													FileAttachListBean fileBean = new  FileAttachListBean();
													fileBean.setFileID("");
													fileBean.setAttachName(name);
													fileBean.setGuidName(u.toString()+str);
													fileBean.setExtensionName(str);
													fileBean.setUerNo(doc.getNumber());
													fileBean.setFileSize(String.valueOf(data.getFileSize()));
													listBean.add(fileBean);
													setCount.add(UploadHistoricalData.getOidByObject(doc)+"___GF");
//													mapCount.put(doc, "GF");
											 }
										 }else{
											 	UUID u = UUID.randomUUID();
												String[] arryName = name.split("\\.");
												String conversionName = u.toString()+str;
												System.out.println("conversionName=========="+conversionName);
												DocUploadAttachmentContent.downContexts(downPath, data, conversionName);
												j++;
												//得到符合要求的数据
												mapDoc.put(doc, String.valueOf(j));
												FileAttachListBean fileBean = new  FileAttachListBean();
												fileBean.setFileID("");
												fileBean.setAttachName(name);
												fileBean.setGuidName(u.toString()+str);
												fileBean.setExtensionName(str);
												fileBean.setUerNo(doc.getNumber());
												fileBean.setFileSize(String.valueOf(data.getFileSize()));
												listBean.add(fileBean);
												setCount.add(UploadHistoricalData.getOidByObject(doc)+"___GF");
//												mapCount.put(doc, "GF");
										 }
									}
							
									//=======================================附件end===============================================
									}else {
										//==========================================主内容action====================
										ContentItem ci = ContentHelper.service.getPrimary(doc);
										if (ci != null) {
											ApplicationData data = (ApplicationData) ci;                            //主内容
											System.out.println("ApplicationData======="+data);
											Map<String,String> mapPath = DataConfigurationPacketTable.getYLSendDocDataBeans();
											  String downPath = mapPath.get("mappingPath");
											 String name = data.getFileName();                                       //文件名--取原始名称
											 String str = name.substring(name.lastIndexOf("."), name.length());
											  if(!str.equalsIgnoreCase(".PDF") && !str.equalsIgnoreCase(".pptx") && !str.equalsIgnoreCase(".ppt") 
														 && !str.equalsIgnoreCase(".xls") && !str.equalsIgnoreCase(".xlsx") 
														 && !str.equalsIgnoreCase(".doc") && !str.equalsIgnoreCase(".docx")){
													 UUID u = UUID.randomUUID();
													 String[] arryName = name.split("\\.");
													 String conversionName = u.toString()+str;
													 System.out.println("conversionName=========="+conversionName);
														System.out.println("name=================="+name);
														DocUploadAttachmentContent.downContexts(downPath, data, conversionName);                       		  	//下载文件											
														mapYLDoc.put(doc, "2");
														FileAttachListYLBean fileBean = new  FileAttachListYLBean();
														fileBean.setFileID("");
														fileBean.setAttachName(name);
														fileBean.setGuidName(u.toString()+str);
														fileBean.setExtensionName(str);
														fileBean.setUerNo(doc.getNumber());
														fileBean.setFileSize(String.valueOf(data.getFileSize()));
														listYLBean.add(fileBean);
														setCount.add(UploadHistoricalData.getOidByObject(doc)+"___YL");
//														mapCount.put(doc, "YL");
											  }
										}
										//=======================================主内容end==========================
								 
										//======================================附件action============================================
										QueryResult pdf = ContentHelper.service.getContentsByRole(doc, ContentRoleType.SECONDARY);
									
										while (pdf.hasMoreElements()) {
											ApplicationData data = (ApplicationData) pdf.nextElement();
											System.out.println("ApplicationData======="+data);
											Map<String,String> mapPath = DataConfigurationPacketTable.getYLSendDocDataBeans();
											  String downPath = mapPath.get("mappingPath");
											String name = data.getFileName();
											System.out.println("name==============="+name);
											String str = name.substring(name.lastIndexOf("."), name.length());
											 if(str.equalsIgnoreCase(".PDF") || str.equalsIgnoreCase(".pptx") || str.equalsIgnoreCase(".ppt") 
													 || str.equalsIgnoreCase(".xls") || str.equalsIgnoreCase(".xlsx")
													 || str.equalsIgnoreCase(".doc") || str.equalsIgnoreCase(".docx")){
												 boolean isprint =name.startsWith("PRINT");
												 if(isprint){
														UUID u = UUID.randomUUID();
														String[] arryName = name.split("\\.");
														String conversionName = u.toString()+str;
														System.out.println("conversionName=========="+conversionName);
														DocUploadAttachmentContent.downContexts(downPath, data, conversionName);
														mapYLDoc.put(doc, "2");
														FileAttachListYLBean fileBean = new  FileAttachListYLBean();
														fileBean.setFileID("");
														fileBean.setAttachName(name);
														fileBean.setGuidName(u.toString()+str);
														fileBean.setExtensionName(str);
														fileBean.setUerNo(doc.getNumber());
														fileBean.setFileSize(String.valueOf(data.getFileSize()));
														listYLBean.add(fileBean);
														setCount.add(UploadHistoricalData.getOidByObject(doc)+"___YL");
//														mapCount.put(doc, "YL");
												 }
											 }else{
												 	UUID u = UUID.randomUUID();
													String[] arryName = name.split("\\.");
													String conversionName = u.toString()+str;
													System.out.println("conversionName=========="+conversionName);
													DocUploadAttachmentContent.downContexts(downPath, data, conversionName);
													j++;
													//得到符合要求的数据
													mapYLDoc.put(doc, "2");
													FileAttachListYLBean fileBean = new  FileAttachListYLBean();
													fileBean.setFileID("");
													fileBean.setAttachName(name);
													fileBean.setGuidName(u.toString()+str);
													fileBean.setExtensionName(str);
													fileBean.setUerNo(doc.getNumber());
													fileBean.setFileSize(String.valueOf(data.getFileSize()));
													listYLBean.add(fileBean);
													setCount.add(UploadHistoricalData.getOidByObject(doc)+"___YL");
//													mapCount.put(doc, "YL");
											 }
										}
								
										//=======================================附件end===============================================
									}
					     
							}
					  }
//					}
//					
//				}
		  
//				for(String key: map.keySet()){
//					SendDocDataBean bean = map.get(key);
//					System.out.println("getDescOrRef==========="+bean.getDescOrRef());
//					if(bean.getDescOrRef().equals("说明文档")){
////						List<WTPart> listDesPart = getAllPartByDoc(doc);                          //取说明部件，暂时预留			
//						TypeIdentifier typeIdentifier = TypeIdentifierUtility.getTypeIdentifier(doc);
//						String typename = typeIdentifier.getTypename();
						if (typename.contains("com.plm.description_square_document")) {
//							String[] arry = key.split("__");
							String type = UploadAttachmentUtil.getTypeInternalName(doc);
							String passageInventory = (String)PdfUtil.getIBAObjectValue(doc, "SmallDocType");
							System.out.println("type======="+type);
							System.out.println("passageInventory============="+passageInventory);
//							System.out.println("arry[0]========"+arry[0]);
//							System.out.println("arry[1]========"+arry[1]);
							if(map.keySet().toString().contains(type)){
								
								if(type.equals("com.plm.drawingdoc")){             //------------------1.图纸不主内容不发PDF格式--2.附件有PDF必须PRINT开头
									
									WTContainer container = doc.getContainer();
					                if(container instanceof WTLibrary){
					                    WTLibrary library = (WTLibrary) container;
					                    String libName = library.getName();	
					                    if(libName.contains("原材料库")){					  
									  //==========================================主内容action====================
										 ContentItem ci = ContentHelper.service.getPrimary(doc);
										 if (ci != null) {
											  ApplicationData data = (ApplicationData) ci;                            //主内容
											  System.out.println("ApplicationData========+"+data);
											  //String downPath = "smb://rdpplm:rdpplm123@LAPTOP-UL273M6G/excel/";  
											  
											  //============光峰
											  Map<String,String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
											  String downPath = mapPath.get("mappingPath");
											  String name = data.getFileName();
											  String str = name.substring(name.lastIndexOf("."), name.length());
											  if(!str.equalsIgnoreCase(".PDF") && !str.equalsIgnoreCase(".pptx") && !str.equalsIgnoreCase(".ppt") 
														 && !str.equalsIgnoreCase(".xls") && !str.equalsIgnoreCase(".xlsx") 
														 && !str.equalsIgnoreCase(".doc") && !str.equalsIgnoreCase(".docx")){
													  UUID u = UUID.randomUUID();
													  String[] arryName = name.split("\\.");
													  System.out.println("name==============="+name);
													  String conversionName = u.toString()+str;
													  System.out.println("conversionName=========="+conversionName);
													  DocUploadAttachmentContent.downContexts(downPath, data, conversionName);
													  j++;
													  mapDoc.put(doc, String.valueOf(j));
													  FileAttachListBean fileBean = new  FileAttachListBean();
													  fileBean.setFileID("");
													  fileBean.setAttachName(name);
													  fileBean.setGuidName(u.toString()+str);
													  fileBean.setExtensionName(str);
													  fileBean.setUerNo(doc.getNumber());
													  fileBean.setFileSize(String.valueOf(data.getFileSize()));
													  listBean.add(fileBean);
													  setCount.add(UploadHistoricalData.getOidByObject(doc)+"___GF");
//													  mapCount.put(doc, "GF");
										  	}
//											  
											  //===========绎立
											  Map<String,String> mapPathYL = DataConfigurationPacketTable.getYLSendDocDataBeans();
											  String downPathYL = mapPathYL.get("mappingPath");
											  String nameYL = data.getFileName();
											  String strYL = nameYL.substring(nameYL.lastIndexOf("."), nameYL.length());
											  if(!strYL.equalsIgnoreCase(".PDF") && !strYL.equalsIgnoreCase(".pptx") && !strYL.equalsIgnoreCase(".ppt") 
														 && !strYL.equalsIgnoreCase(".xls") && !strYL.equalsIgnoreCase(".xlsx") 
														 && !strYL.equalsIgnoreCase(".doc") && !strYL.equalsIgnoreCase(".docx")){
													  UUID u = UUID.randomUUID();
													  String[] arryName = nameYL.split("\\.");
													  System.out.println("name==============="+nameYL);
													  String conversionName = u.toString()+str;
													  System.out.println("conversionName=========="+conversionName);
													  DocUploadAttachmentContent.downContexts(downPathYL, data, conversionName);
													  mapYLDoc.put(doc, "1");
													  FileAttachListYLBean fileBean = new  FileAttachListYLBean();
													  fileBean.setFileID("");
													  fileBean.setAttachName(nameYL);
													  fileBean.setGuidName(u.toString()+str);
													  fileBean.setExtensionName(str);
													  fileBean.setUerNo(doc.getNumber());
													  fileBean.setFileSize(String.valueOf(data.getFileSize()));
													  listYLBean.add(fileBean);
													  setCount.add(UploadHistoricalData.getOidByObject(doc)+"___YL");
//													  mapCount.put(doc, "YL");
										  	}
										 }
										 
										//======================================附件action============================================
										 QueryResult pdf = ContentHelper.service.getContentsByRole(doc, ContentRoleType.SECONDARY);
											while (pdf.hasMoreElements()) {
												ApplicationData data = (ApplicationData) pdf.nextElement();
												System.out.println("AttApplicationData========+"+data);
//												String downPath = "smb://rdpplm:rdpplm123@LAPTOP-UL273M6G/excel/";
												Map<String,String> mapPathYL = DataConfigurationPacketTable.getYLSendDocDataBeans();
												 String downPathYL = mapPathYL.get("mappingPath");
												 //光峰
												 Map<String,String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
												 String downPath = mapPath.get("mappingPath");
												 String name = data.getFileName();
												 String str = name.substring(name.lastIndexOf("."), name.length());
												 if(str.equalsIgnoreCase(".PDF") || str.equalsIgnoreCase(".pptx") || str.equalsIgnoreCase(".ppt") 
														 || str.equalsIgnoreCase(".xls") || str.equalsIgnoreCase(".xlsx")
														 || str.equalsIgnoreCase(".doc") || str.equalsIgnoreCase(".docx")){
													 boolean isprint =name.startsWith("PRINT");
													 if(isprint){
														 
														    //光峰
														 	UUID u = UUID.randomUUID();
															String[] arryName = name.split("\\.");
															System.out.println("AttName==============="+name);
															String conversionName = u.toString()+str;
															System.out.println("conversionName=========="+conversionName);
															DocUploadAttachmentContent.downContexts(downPath, data, conversionName);
															mapDoc.put(doc, String.valueOf(j));
															FileAttachListBean fileBean = new  FileAttachListBean();
															fileBean.setFileID("");
															fileBean.setAttachName(name);
															fileBean.setGuidName(u.toString()+str);
															fileBean.setExtensionName(str);
															fileBean.setUerNo(doc.getNumber());
															fileBean.setFileSize(String.valueOf(data.getFileSize()));
															listBean.add(fileBean);
															setCount.add(UploadHistoricalData.getOidByObject(doc)+"___GF");
//															mapCount.put(doc, "GF");
															
															//YL
															UUID uYL = UUID.randomUUID();
															String[] arryNameYL = name.split("\\.");
															System.out.println("AttName==============="+name);
															String conversionNameYL = uYL.toString()+"."+arryNameYL[1];
															System.out.println("conversionName=========="+conversionNameYL);
															DocUploadAttachmentContent.downContexts(downPathYL, data, conversionNameYL);
															j++;
															mapYLDoc.put(doc, "1");
															FileAttachListYLBean fileBeanYL = new  FileAttachListYLBean();
															fileBeanYL.setFileID("");
															fileBeanYL.setAttachName(name);
															fileBeanYL.setGuidName(uYL.toString()+"."+arryNameYL[1]);
															fileBeanYL.setExtensionName("."+arryNameYL[1]);
															fileBeanYL.setUerNo(doc.getNumber());
															fileBeanYL.setFileSize(String.valueOf(data.getFileSize()));
															listYLBean.add(fileBeanYL);
															setCount.add(UploadHistoricalData.getOidByObject(doc)+"___YL");
//															mapCount.put(doc, "YL");
													 }
													 

												}else {
													
													//光峰
													UUID u = UUID.randomUUID();
													String[] arryName = name.split("\\.");
													System.out.println("AttName==============="+name);
													String conversionName = u.toString()+str;
													System.out.println("conversionName=========="+conversionName);
													DocUploadAttachmentContent.downContexts(downPath, data, conversionName);
													j++;
													mapDoc.put(doc, String.valueOf(j));
													FileAttachListBean fileBean = new  FileAttachListBean();
													fileBean.setFileID("");
													fileBean.setAttachName(name);
													fileBean.setGuidName(u.toString()+str);
													fileBean.setExtensionName(str);
													fileBean.setUerNo(doc.getNumber());
													fileBean.setFileSize(String.valueOf(data.getFileSize()));
													listBean.add(fileBean);
													setCount.add(UploadHistoricalData.getOidByObject(doc)+"___GF");
//													mapCount.put(doc, "GF");
													
													//绎立
													UUID uYL = UUID.randomUUID();
													String[] arryNameYL = name.split("\\.");
													System.out.println("AttName==============="+name);
													String conversionNameYL = uYL.toString()+"."+arryNameYL[1];
													System.out.println("conversionName=========="+conversionNameYL);
													DocUploadAttachmentContent.downContexts(downPathYL, data, conversionNameYL);
													j++;
													mapYLDoc.put(doc, "1");
													FileAttachListYLBean fileBeanYL = new  FileAttachListYLBean();
													fileBeanYL.setFileID("");
													fileBeanYL.setAttachName(name);
													fileBeanYL.setGuidName(uYL.toString()+"."+arryNameYL[1]);
													fileBeanYL.setExtensionName("."+arryNameYL[1]);
													fileBeanYL.setUerNo(doc.getNumber());
													fileBeanYL.setFileSize(String.valueOf(data.getFileSize()));
													listYLBean.add(fileBeanYL);
													setCount.add(UploadHistoricalData.getOidByObject(doc)+"___YL");
//													mapCount.put(doc, "YL");
												 }
										 }	
					                    }
									  }
										//=======================================附件end===============================================
										 
										 
										 //=======================================主内容end==========================
								  }else if(type.equals("com.plm.softwaredoc")){                                    //软件只发送zip,rar格式
									//==========================================主内容action====================
									  
									     if(!doc.getContainerName().equals("SL-舞台灯产品库")){
										 ContentItem ci = ContentHelper.service.getPrimary(doc);
										 if (ci != null) {
											  ApplicationData data = (ApplicationData) ci;                            //主内容
											  System.out.println("ApplicationData========+"+data);
											  //String downPath = "smb://rdpplm:rdpplm123@LAPTOP-UL273M6G/excel/";     
											  Map<String,String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
											  String downPath = mapPath.get("mappingPath");
											  String name = data.getFileName();
											  String str = name.substring(name.lastIndexOf("."), name.length());
											  if(str.equalsIgnoreCase(".zip") || str.equalsIgnoreCase(".rar")){
												  UUID u = UUID.randomUUID();
												  String[] arryName = name.split("\\.");
												  System.out.println("name==============="+name);
												  String conversionName = u.toString()+str;
												  System.out.println("conversionName=========="+conversionName);
												  DocUploadAttachmentContent.downContexts(downPath, data, conversionName);
												//得到符合要求的数据
												  j++;
												  mapDoc.put(doc, String.valueOf(j));
												  FileAttachListBean fileBean = new  FileAttachListBean();
												  fileBean.setFileID("");
												  fileBean.setAttachName(name);
												  fileBean.setGuidName(u.toString()+str);
												  fileBean.setExtensionName(str);
												  fileBean.setUerNo(doc.getNumber());
												  fileBean.setFileSize(String.valueOf(data.getFileSize()));
												  listBean.add(fileBean);
												  setCount.add(UploadHistoricalData.getOidByObject(doc)+"___GF");
//												  mapCount.put(doc, "GF");
											}
										 }
										 //=======================================主内容end==========================
										 
										 //======================================附件action============================================
										 QueryResult pdf = ContentHelper.service.getContentsByRole(doc, ContentRoleType.SECONDARY);
											while (pdf.hasMoreElements()) {
												ApplicationData data = (ApplicationData) pdf.nextElement();
												System.out.println("AttApplicationData========+"+data);
//												String downPath = "smb://rdpplm:rdpplm123@LAPTOP-UL273M6G/excel/";     
												Map<String,String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
												  String downPath = mapPath.get("mappingPath");
												String name = data.getFileName();
												String str = name.substring(name.lastIndexOf("."), name.length());
												if(str.equalsIgnoreCase(".zip") || str.equalsIgnoreCase(".rar")){
													UUID u = UUID.randomUUID();
													String[] arryName = name.split("\\.");
													System.out.println("AttName==============="+name);
													String conversionName = u.toString()+str;
													System.out.println("conversionName=========="+conversionName);
													DocUploadAttachmentContent.downContexts(downPath, data, conversionName);
													//UUID u = UUID.randomUUID();
													//得到符合要求的数据
		//		                                    setPart.add(part);
													mapDoc.put(doc, String.valueOf(j));
													FileAttachListBean fileBean = new  FileAttachListBean();
													fileBean.setFileID("");
													fileBean.setAttachName(name);
													fileBean.setGuidName(u.toString()+str);
													fileBean.setExtensionName(str);
													fileBean.setUerNo(doc.getNumber());
													fileBean.setFileSize(String.valueOf(data.getFileSize()));
													listBean.add(fileBean);
													setCount.add(UploadHistoricalData.getOidByObject(doc)+"___GF");
//													mapCount.put(doc, "GF");
												}
										 }
											
									   }else {
										   
										   ContentItem ci = ContentHelper.service.getPrimary(doc);
											 if (ci != null) {
												  ApplicationData data = (ApplicationData) ci;                            //主内容
												  System.out.println("ApplicationData========+"+data);
												  //String downPath = "smb://rdpplm:rdpplm123@LAPTOP-UL273M6G/excel/";     
												  Map<String,String> mapPath = DataConfigurationPacketTable.getYLSendDocDataBeans();
												  String downPath = mapPath.get("mappingPath");
												  String name = data.getFileName();
												  String str = name.substring(name.lastIndexOf("."), name.length());
												  if(str.equalsIgnoreCase(".zip") || str.equalsIgnoreCase(".rar")){
													  UUID u = UUID.randomUUID();
													  String[] arryName = name.split("\\.");
													  System.out.println("name==============="+name);
													  String conversionName = u.toString()+str;
													  System.out.println("conversionName=========="+conversionName);
													  DocUploadAttachmentContent.downContexts(downPath, data, conversionName);
													//得到符合要求的数据
													  j++;
													  mapYLDoc.put(doc, "2");
													  FileAttachListYLBean fileBean = new  FileAttachListYLBean();
													  fileBean.setFileID("");
													  fileBean.setAttachName(name);
													  fileBean.setGuidName(u.toString()+str);
													  fileBean.setExtensionName(str);
													  fileBean.setUerNo(doc.getNumber());
													  fileBean.setFileSize(String.valueOf(data.getFileSize()));
													  listYLBean.add(fileBean);
													  setCount.add(UploadHistoricalData.getOidByObject(doc)+"___YL");
//													  mapCount.put(doc, "YL");
												}
											 }
											 //=======================================主内容end==========================
											 
											 //======================================附件action============================================
											 QueryResult pdf = ContentHelper.service.getContentsByRole(doc, ContentRoleType.SECONDARY);
												while (pdf.hasMoreElements()) {
													ApplicationData data = (ApplicationData) pdf.nextElement();
													System.out.println("AttApplicationData========+"+data);
//													String downPath = "smb://rdpplm:rdpplm123@LAPTOP-UL273M6G/excel/";     
													Map<String,String> mapPath = DataConfigurationPacketTable.getYLSendDocDataBeans();
													  String downPath = mapPath.get("mappingPath");
													String name = data.getFileName();
													String str = name.substring(name.lastIndexOf("."), name.length());
													if(str.equalsIgnoreCase(".zip") || str.equalsIgnoreCase(".rar")){
														UUID u = UUID.randomUUID();
														String[] arryName = name.split("\\.");
														System.out.println("AttName==============="+name);
														String conversionName = u.toString()+str;
														System.out.println("conversionName=========="+conversionName);
														DocUploadAttachmentContent.downContexts(downPath, data, conversionName);
														//UUID u = UUID.randomUUID();
														//得到符合要求的数据
														j++;
			//		                                    setPart.add(part);
														mapYLDoc.put(doc, "2");
														FileAttachListYLBean fileBean = new  FileAttachListYLBean();
														fileBean.setFileID("");
														fileBean.setAttachName(name);
														fileBean.setGuidName(u.toString()+str);
														fileBean.setExtensionName(str);
														fileBean.setUerNo(doc.getNumber());
														fileBean.setFileSize(String.valueOf(data.getFileSize()));
														listYLBean.add(fileBean);
														setCount.add(UploadHistoricalData.getOidByObject(doc)+"___YL");
//														mapCount.put(doc, "YL");
													}
											 }
									   }
										//=======================================附件end===============================================
								  }else if(type.equals("com.plm.datasheet")) {
									//==========================================主内容action====================
										 ContentItem ci = ContentHelper.service.getPrimary(doc);
										 if (ci != null) {
											  ApplicationData data = (ApplicationData) ci;                            //主内容
											  System.out.println("ApplicationData========+"+data);
											  //String downPath = "smb://rdpplm:rdpplm123@LAPTOP-UL273M6G/excel/";     
											  
											  //光峰
											  Map<String,String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
											  String downPath = mapPath.get("mappingPath");
											  String name = data.getFileName();
											  String str = name.substring(name.lastIndexOf("."), name.length());
											  if(!str.equalsIgnoreCase(".PDF") && !str.equalsIgnoreCase(".pptx") && !str.equalsIgnoreCase(".ppt") 
														 && !str.equalsIgnoreCase(".xls") && !str.equalsIgnoreCase(".xlsx") 
														 && !str.equalsIgnoreCase(".doc") && !str.equalsIgnoreCase(".docx")){
												  UUID u = UUID.randomUUID();
												  String[] arryName = name.split("\\.");
												  System.out.println("name==============="+name);
												  String conversionName = u.toString()+str;
												  System.out.println("conversionName=========="+conversionName);
												  DocUploadAttachmentContent.downContexts(downPath, data, conversionName);
												//得到符合要求的数据
												  j++;
												  	mapDoc.put(doc, String.valueOf(j));
												  	FileAttachListBean fileBean = new  FileAttachListBean();
													fileBean.setFileID("");
													fileBean.setAttachName(name);
													fileBean.setGuidName(u.toString()+str);
													fileBean.setExtensionName(str);
													fileBean.setUerNo(doc.getNumber());
													fileBean.setFileSize(String.valueOf(data.getFileSize()));
													listBean.add(fileBean);
													setCount.add(UploadHistoricalData.getOidByObject(doc)+"___GF");
//													mapCount.put(doc, "GF");
											  }
											  
											  //YL
											  Map<String,String> mapPathYL = DataConfigurationPacketTable.getYLSendDocDataBeans();
											  String downPathYL = mapPathYL.get("mappingPath");
											  String nameYL = data.getFileName();
											  String strYL = nameYL.substring(nameYL.lastIndexOf("."), nameYL.length());
											  if(!strYL.equalsIgnoreCase(".PDF") && !strYL.equalsIgnoreCase(".pptx") && !strYL.equalsIgnoreCase(".ppt") 
														 && !strYL.equalsIgnoreCase(".xls") && !strYL.equalsIgnoreCase(".xlsx") 
														 && !strYL.equalsIgnoreCase(".doc") && !strYL.equalsIgnoreCase(".docx")){
												  UUID u = UUID.randomUUID();
												  String[] arryName = nameYL.split("\\.");
												  System.out.println("name==============="+nameYL);
												  String conversionName = u.toString()+str;
												  System.out.println("conversionName=========="+conversionName);
												  DocUploadAttachmentContent.downContexts(downPathYL, data, conversionName);
												//得到符合要求的数据
												  	mapYLDoc.put(doc, "3");
												  	FileAttachListYLBean fileBean = new  FileAttachListYLBean();
													fileBean.setFileID("");
													fileBean.setAttachName(nameYL);
													fileBean.setGuidName(u.toString()+str);
													fileBean.setExtensionName(str);
													fileBean.setUerNo(doc.getNumber());
													fileBean.setFileSize(String.valueOf(data.getFileSize()));
													listYLBean.add(fileBean);
													setCount.add(UploadHistoricalData.getOidByObject(doc)+"___YL");
//													mapCount.put(doc, "YL");
											  }
											  
										 }
										 //=======================================主内容end==========================
										 
										 //======================================附件action============================================
										 QueryResult pdf = ContentHelper.service.getContentsByRole(doc, ContentRoleType.SECONDARY);
											while (pdf.hasMoreElements()) {
												ApplicationData data = (ApplicationData) pdf.nextElement();
												System.out.println("AttApplicationData========+"+data);
												Map<String,String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
												  String downPath = mapPath.get("mappingPath");
												  String name = data.getFileName();
												  Map<String,String> mapPathYL = DataConfigurationPacketTable.getYLSendDocDataBeans();
												  String downPathYL = mapPathYL.get("mappingPath");
													
												String str = name.substring(name.lastIndexOf("."), name.length());
												 if(str.equalsIgnoreCase(".PDF") || str.equalsIgnoreCase(".pptx") || str.equalsIgnoreCase(".ppt") 
														 || str.equalsIgnoreCase(".xls") || str.equalsIgnoreCase(".xlsx")
														 || str.equalsIgnoreCase(".doc") || str.equalsIgnoreCase(".docx")){
													 boolean isprint =name.startsWith("PRINT");
													 if(isprint) {
															UUID u = UUID.randomUUID();
															String[] arryName = name.split("\\.");
															System.out.println("AttName==============="+name);
															String conversionName = u.toString()+str;
															System.out.println("conversionName=========="+conversionName);
															DocUploadAttachmentContent.downContexts(downPath, data, conversionName);
															//UUID u = UUID.randomUUID();
															//得到符合要求的数据
															j++;
															mapDoc.put(doc, String.valueOf(j));
															FileAttachListBean fileBean = new  FileAttachListBean();
															fileBean.setFileID("");
															fileBean.setAttachName(name);
															fileBean.setGuidName(u.toString()+str);
															fileBean.setExtensionName(str);
															fileBean.setUerNo(doc.getNumber());
															fileBean.setFileSize(String.valueOf(data.getFileSize()));
															listBean.add(fileBean);
															setCount.add(UploadHistoricalData.getOidByObject(doc)+"___GF");
//															mapCount.put(doc, "GF");
															
															    //绎立
																UUID uYL = UUID.randomUUID();
																String[] arryNameYL = name.split("\\.");
																System.out.println("AttName==============="+name);
																String conversionNameYL = uYL.toString()+"."+arryNameYL[1];
																System.out.println("conversionName=========="+conversionNameYL);
																DocUploadAttachmentContent.downContexts(downPathYL, data, conversionNameYL);
																j++;
																mapYLDoc.put(doc, "3");
																FileAttachListYLBean fileBeanYL = new  FileAttachListYLBean();
																fileBeanYL.setFileID("");
																fileBeanYL.setAttachName(name);
																fileBeanYL.setGuidName(uYL.toString()+"."+arryNameYL[1]);
																fileBeanYL.setExtensionName("."+arryNameYL[1]);
																fileBeanYL.setUerNo(doc.getNumber());
																fileBeanYL.setFileSize(String.valueOf(data.getFileSize()));
																listYLBean.add(fileBeanYL);
																setCount.add(UploadHistoricalData.getOidByObject(doc)+"___YL");
//																mapCount.put(doc, "YL");
													 }
													 
												 }else{
													 	UUID u = UUID.randomUUID();
														String[] arryName = name.split("\\.");
														System.out.println("AttName==============="+name);
														String conversionName = u.toString()+str;
														System.out.println("conversionName=========="+conversionName);
														DocUploadAttachmentContent.downContexts(downPath, data, conversionName);
														//UUID u = UUID.randomUUID();
														//得到符合要求的数据
														j++;
														mapDoc.put(doc, String.valueOf(j));
														FileAttachListBean fileBean = new  FileAttachListBean();
														fileBean.setFileID("");
														fileBean.setAttachName(name);
														fileBean.setGuidName(u.toString()+str);
														fileBean.setExtensionName(str);
														fileBean.setUerNo(doc.getNumber());
														fileBean.setFileSize(String.valueOf(data.getFileSize()));
														listBean.add(fileBean);
//														mapCount.put(doc, "GF");
														setCount.add(UploadHistoricalData.getOidByObject(doc)+"___GF");
														
														//绎立
														UUID uYL = UUID.randomUUID();
														String[] arryNameYL = name.split("\\.");
														System.out.println("AttName==============="+name);
														String conversionNameYL = uYL.toString()+"."+arryNameYL[1];
														System.out.println("conversionName=========="+conversionNameYL);
														DocUploadAttachmentContent.downContexts(downPathYL, data, conversionNameYL);
														j++;
														mapYLDoc.put(doc, "3");
														FileAttachListYLBean fileBeanYL = new  FileAttachListYLBean();
														fileBeanYL.setFileID("");
														fileBeanYL.setAttachName(name);
														fileBeanYL.setGuidName(uYL.toString()+"."+arryNameYL[1]);
														fileBeanYL.setExtensionName("."+arryNameYL[1]);
														fileBeanYL.setUerNo(doc.getNumber());
														fileBeanYL.setFileSize(String.valueOf(data.getFileSize()));
														listYLBean.add(fileBeanYL);
														setCount.add(UploadHistoricalData.getOidByObject(doc)+"___YL");
//														mapCount.put(doc, "YL");
												 }
										 }		
										//=======================================附件end===============================================
									  
								  }else{
									  if(!doc.getContainerName().equals("SL-舞台灯产品库")){
											 //==========================================主内容action====================
											 ContentItem ci = ContentHelper.service.getPrimary(doc);
											 if (ci != null) {
												  ApplicationData data = (ApplicationData) ci;                            //主内容
												  System.out.println("ApplicationData========+"+data);
												  //String downPath = "smb://rdpplm:rdpplm123@LAPTOP-UL273M6G/excel/";     
												  Map<String,String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
												  String downPath = mapPath.get("mappingPath");
												  String name = data.getFileName();
												  String str = name.substring(name.lastIndexOf("."), name.length());
												  if(!str.equalsIgnoreCase(".PDF") && !str.equalsIgnoreCase(".pptx") && !str.equalsIgnoreCase(".ppt") 
															 && !str.equalsIgnoreCase(".xls") && !str.equalsIgnoreCase(".xlsx") 
															 && !str.equalsIgnoreCase(".doc") && !str.equalsIgnoreCase(".docx")){
													  UUID u = UUID.randomUUID();
													  String[] arryName = name.split("\\.");
													  System.out.println("name==============="+name);
													  String conversionName = u.toString()+str;
													  System.out.println("conversionName=========="+conversionName);
													  DocUploadAttachmentContent.downContexts(downPath, data, conversionName);
													//得到符合要求的数据
													  j++;
													  	mapDoc.put(doc, String.valueOf(j));
													  	FileAttachListBean fileBean = new  FileAttachListBean();
														fileBean.setFileID("");
														fileBean.setAttachName(name);
														fileBean.setGuidName(u.toString()+str);
														fileBean.setExtensionName(str);
														fileBean.setUerNo(doc.getNumber());
														fileBean.setFileSize(String.valueOf(data.getFileSize()));
														listBean.add(fileBean);
//														mapCount.put(doc, "GF");
														setCount.add(UploadHistoricalData.getOidByObject(doc)+"___GF");
												  }
											 }
											 //=======================================主内容end==========================
											 
											 //======================================附件action============================================
											 QueryResult pdf = ContentHelper.service.getContentsByRole(doc, ContentRoleType.SECONDARY);
												while (pdf.hasMoreElements()) {
													ApplicationData data = (ApplicationData) pdf.nextElement();
													System.out.println("AttApplicationData========+"+data);
													Map<String,String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
													  String downPath = mapPath.get("mappingPath");
													String name = data.getFileName();
													
													String str = name.substring(name.lastIndexOf("."), name.length());
													 if(str.equalsIgnoreCase(".PDF") || str.equalsIgnoreCase(".pptx") || str.equalsIgnoreCase(".ppt") 
															 || str.equalsIgnoreCase(".xls") || str.equalsIgnoreCase(".xlsx")
															 || str.equalsIgnoreCase(".doc") || str.equalsIgnoreCase(".docx")){
														 boolean isprint =name.startsWith("PRINT");
														 if(isprint) {
																UUID u = UUID.randomUUID();
																String[] arryName = name.split("\\.");
																System.out.println("AttName==============="+name);
																String conversionName = u.toString()+str;
																System.out.println("conversionName=========="+conversionName);
																DocUploadAttachmentContent.downContexts(downPath, data, conversionName);
																//UUID u = UUID.randomUUID();
																//得到符合要求的数据
																j++;
																mapDoc.put(doc, String.valueOf(j));
																FileAttachListBean fileBean = new  FileAttachListBean();
																fileBean.setFileID("");
																fileBean.setAttachName(name);
																fileBean.setGuidName(u.toString()+str);
																fileBean.setExtensionName(str);
																fileBean.setUerNo(doc.getNumber());
																fileBean.setFileSize(String.valueOf(data.getFileSize()));
																listBean.add(fileBean);
//																mapCount.put(doc, "GF");
																setCount.add(UploadHistoricalData.getOidByObject(doc)+"___GF");
														 }
													 }else{
														 	UUID u = UUID.randomUUID();
															String[] arryName = name.split("\\.");
															System.out.println("AttName==============="+name);
															String conversionName = u.toString()+str;
															System.out.println("conversionName=========="+conversionName);
															DocUploadAttachmentContent.downContexts(downPath, data, conversionName);
															//UUID u = UUID.randomUUID();
															//得到符合要求的数据
															j++;
															mapDoc.put(doc, String.valueOf(j));
															FileAttachListBean fileBean = new  FileAttachListBean();
															fileBean.setFileID("");
															fileBean.setAttachName(name);
															fileBean.setGuidName(u.toString()+str);
															fileBean.setExtensionName(str);
															fileBean.setUerNo(doc.getNumber());
															fileBean.setFileSize(String.valueOf(data.getFileSize()));
															listBean.add(fileBean);
//															mapCount.put(doc, "GF");
															setCount.add(UploadHistoricalData.getOidByObject(doc)+"___GF");
													 }
											 }		
											//=======================================附件end===============================================
								  }else {
									//==========================================主内容action====================
										 ContentItem ci = ContentHelper.service.getPrimary(doc);
										 if (ci != null) {
											  ApplicationData data = (ApplicationData) ci;                            //主内容
											  System.out.println("ApplicationData========+"+data);
											  //String downPath = "smb://rdpplm:rdpplm123@LAPTOP-UL273M6G/excel/";     
											  Map<String,String> mapPath = DataConfigurationPacketTable.getYLSendDocDataBeans();
											  String downPath = mapPath.get("mappingPath");
											  String name = data.getFileName();
											  String str = name.substring(name.lastIndexOf("."), name.length());
											  if(!str.equalsIgnoreCase(".PDF") && !str.equalsIgnoreCase(".pptx") && !str.equalsIgnoreCase(".ppt") 
														 && !str.equalsIgnoreCase(".xls") && !str.equalsIgnoreCase(".xlsx") 
														 && !str.equalsIgnoreCase(".doc") && !str.equalsIgnoreCase(".docx")){
												  UUID u = UUID.randomUUID();
												  String[] arryName = name.split("\\.");
												  System.out.println("name==============="+name);
												  String conversionName = u.toString()+str;
												  System.out.println("conversionName=========="+conversionName);
												  DocUploadAttachmentContent.downContexts(downPath, data, conversionName);
												  //得到符合要求的数据
												    mapYLDoc.put(doc, "2");
												  	FileAttachListYLBean fileBean = new  FileAttachListYLBean();
													fileBean.setFileID("");
													fileBean.setAttachName(name);
													fileBean.setGuidName(u.toString()+str);
													fileBean.setExtensionName(str);
													fileBean.setUerNo(doc.getNumber());
													fileBean.setFileSize(String.valueOf(data.getFileSize()));
													listYLBean.add(fileBean);
//													mapCount.put(doc, "YL");
													setCount.add(UploadHistoricalData.getOidByObject(doc)+"___YL");
											  }
										 }
										 //=======================================主内容end==========================
										 
										 //======================================附件action============================================
										 QueryResult pdf = ContentHelper.service.getContentsByRole(doc, ContentRoleType.SECONDARY);
											while (pdf.hasMoreElements()) {
												ApplicationData data = (ApplicationData) pdf.nextElement();
												System.out.println("AttApplicationData========+"+data);
												Map<String,String> mapPath = DataConfigurationPacketTable.getYLSendDocDataBeans();
												  String downPath = mapPath.get("mappingPath");
												String name = data.getFileName();
												
												String str = name.substring(name.lastIndexOf("."), name.length());
												 if(str.equalsIgnoreCase(".PDF") || str.equalsIgnoreCase(".pptx") || str.equalsIgnoreCase(".ppt") 
														 || str.equalsIgnoreCase(".xls") || str.equalsIgnoreCase(".xlsx")
														 || str.equalsIgnoreCase(".doc") || str.equalsIgnoreCase(".docx")){
													 boolean isprint =name.startsWith("PRINT");
													 if(isprint) {
															UUID u = UUID.randomUUID();
															String[] arryName = name.split("\\.");
															System.out.println("AttName==============="+name);
															String conversionName = u.toString()+str;
															System.out.println("conversionName=========="+conversionName);
															DocUploadAttachmentContent.downContexts(downPath, data, conversionName);
															//UUID u = UUID.randomUUID();
															//得到符合要求的数据
															j++;
															mapYLDoc.put(doc, "2");
														  	FileAttachListYLBean fileBean = new  FileAttachListYLBean();
															fileBean.setFileID("");
															fileBean.setAttachName(name);
															fileBean.setGuidName(u.toString()+str);
															fileBean.setExtensionName(str);
															fileBean.setUerNo(doc.getNumber());
															fileBean.setFileSize(String.valueOf(data.getFileSize()));
															listYLBean.add(fileBean);
//															mapCount.put(doc, "YL");
															setCount.add(UploadHistoricalData.getOidByObject(doc)+"___YL");
													 }
												 }else{
													 	UUID u = UUID.randomUUID();
														String[] arryName = name.split("\\.");
														System.out.println("AttName==============="+name);
														String conversionName = u.toString()+str;
														System.out.println("conversionName=========="+conversionName);
														DocUploadAttachmentContent.downContexts(downPath, data, conversionName);
														//UUID u = UUID.randomUUID();
														//得到符合要求的数据
														j++;
														mapYLDoc.put(doc, "2");
													  	FileAttachListYLBean fileBean = new  FileAttachListYLBean();
														fileBean.setFileID("");
														fileBean.setAttachName(name);
														fileBean.setGuidName(u.toString()+str);
														fileBean.setExtensionName(str);
														fileBean.setUerNo(doc.getNumber());
														fileBean.setFileSize(String.valueOf(data.getFileSize()));
														listYLBean.add(fileBean);
//														mapCount.put(doc, "YL");
														setCount.add(UploadHistoricalData.getOidByObject(doc)+"___YL");
												 }
										 }	
								  }  
								  }
                           }
							}
					}
				  }
				}else {
					setNoProcess.add(doc);
				}
					}  
//	      	  }
//			}
//			
//			 System.out.println("发送成功的数量:==============="+setYesProcess.size());
//		     System.out.println("发送失败的数量:==============="+setNoProcess.size());
//			    if(!mapDoc.isEmpty()){
//			    	for(WTDocument doc: mapDoc.keySet()) {	
//			    	insertEDFiles(doc,mapDoc.get(doc),listBean);
//			    	}
//			    }
//				for(FileAttachListBean bean : listBean){
//					insertFileAttachList(bean);
//				}
	      	if(!mapDoc.isEmpty()){
	      		System.out.println("发送成功文档数量+"+mapDoc.size());
	         	DocUploadAttachmentContent.insertEDFiles(mapDoc, listBean);
	      	}
	      	
	      	if(!mapYLDoc.isEmpty()) {
	      		System.out.println("发送成功YL文档数量+"+mapYLDoc.size());
	      		System.out.println("listYLBean=========================="+listYLBean);
	      		DocUploadAttachmentYLContent.insertEDFiles(mapYLDoc, listYLBean);
	      	}
	      	
			Set<String> filesCount = UploadAttachmentContent.checkFilesCount();
			UploadAttachmentContent.updateFilesCount(filesCount);
			
			Set<String> filesCount2 = UploadAttachmentYLContent.checkFilesCount();
			UploadAttachmentYLContent.updateFilesCount(filesCount2);
		  } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PropertyVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			  SessionServerHelper.manager.setAccessEnforced(flag); // 添加权限
		  }
		return setCount;
		     
	  }
	
	
	  
	  public Workbook exportData() throws WTException {
			 try{
				 
				    String strTemplate = WT_CODEBASE + File.separator + "config" + File.separator + "custom" + File.separator + "sengNumber.xlsx";
		        	Excel2007Handler excelHander = new Excel2007Handler(strTemplate);          
		        	Set<String> set = uploadContentData();
//		        	System.out.println("map=========================="+map);
		            excelHander.switchCurrentSheet("Sheet1");
		            int i = 0;
		            int j = 0;
		            for(String str : set) {
		            	String arry[] = str.split("___");
		            	WTDocument doc = (WTDocument)UploadHistoricalData.getObjectByOid(arry[0]);
		            	if(arry[1].equals("GF")) {
		            	i++;
		            	int rowNum = i+1;
		            	int iCol = 0;
		            	
		            	excelHander.setStringValue(rowNum, iCol++, String.valueOf(i));
		            	excelHander.setStringValue(rowNum, iCol++, doc.getNumber());
		            	excelHander.setStringValue(rowNum, iCol++, doc.getName());
		            	
		            	String version = doc.getVersionInfo().getIdentifier().getValue();
 			            String iteration = doc.getIterationInfo().getIdentifier().getValue();
		            	excelHander.setStringValue(rowNum, iCol++, version+"."+iteration);
		            	}else if(arry[1].equals("YL")){
		            		j++;
			            	int rowNum = j+1;
			            	int iCol = 4;
			            	
			            	excelHander.setStringValue(rowNum, iCol++, String.valueOf(i));
			            	excelHander.setStringValue(rowNum, iCol++, doc.getNumber());
			            	excelHander.setStringValue(rowNum, iCol++, doc.getName());
			            	
			            	String version = doc.getVersionInfo().getIdentifier().getValue();
	 			            String iteration = doc.getIterationInfo().getIdentifier().getValue();
			            	excelHander.setStringValue(rowNum, iCol++, version+"."+iteration);
		            	}

		            }
		             
		            return excelHander.getWorkbook();
		        } catch(Exception e){
		        	e.printStackTrace();
		        	throw new WTException(e.getLocalizedMessage());
		        }
		}
		
		/**
		 * 历史数据路径
		 * @return
		 * @throws IOException
		 */
		public static Set<String> getDocData() throws IOException{
	        Set<String> set = new HashSet<>();
	        
	        String filePath = WT_CODEBASE + File.separator + "config" + File.separator + "custom" + File.separator + "historicalDataDoc.xlsx";
	        Excel2007Handler handler = new Excel2007Handler(filePath);
	        //Excel2007Handler handler = new Excel2007Handler("D:\\ideaspace\\gfgd\\windchill\\codebase\\ext\\appo\\doc\\sendDocData.xlsx");
	        handler.switchCurrentSheet(0);
	        int rowCount = handler.getSheetRowCount();
	        for (int i = 1; i < rowCount; i++) {
	        	 set.add(null2blank(handler.getStringValue(i, 0)));     //文档编号
	        }
	        return set;
	    }
		
		public static String null2blank(Object obj){
	        if(obj == null){
	            return "";
	        }else{
	            String tmp = obj.toString();
	            return tmp.trim();
	        }
	    }
		
		  public static WTDocument getDocByNumber(String docNumber) throws WTException {
		    	WTDocument doc = null;
		    	
		    	QuerySpec qs = new QuerySpec(WTDocument.class);
		    	SearchCondition sc = new SearchCondition(WTDocument.class, WTDocument.NUMBER, SearchCondition.EQUAL, docNumber);
		    	qs.appendWhere(sc, new int[] { 0 });
		    	QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
		    	if(qr.hasMoreElements()){
		    		doc = (WTDocument)qr.nextElement();
		    	}
		    	return doc;
		    }
		 
		
		  public static Vector getAllLatestWTDocs(String number) throws Exception
			{
				QuerySpec qs = new QuerySpec(WTDocument.class);
		        if(number==null)
		        	return null;
				if (number.trim().length() > 0)
				{
					
					SearchCondition scNumber = new SearchCondition(WTDocument.class, WTDocument.NUMBER, SearchCondition.EQUAL, number.toUpperCase());
					qs.appendWhere(scNumber);
				}

				SearchCondition scLatestIteration = new SearchCondition(WTDocument.class, WTAttributeNameIfc.LATEST_ITERATION, SearchCondition.IS_TRUE);
				
				qs.appendWhere(scLatestIteration);

				QueryResult qr = PersistenceHelper.manager.find(qs);
				if (qr != null && qr.hasMoreElements())
					qr = (new LatestConfigSpec()).process(qr);

				if (qr != null && qr.hasMoreElements())
					return qr.getObjectVectorIfc().getVector();

				return new Vector();
			}
		  
		  /**
			 * 下载方法
			 * 
			 * @param downPath
			 * @param data
			 * @param name
			 * @throws WTException
			 * @throws FileNotFoundException
			 * @throws IOException
			 */
			public static  void downContexts(String downPath, ApplicationData data, String name,WTDocument doc){
				
				WTUser user = null;
				BufferedInputStream bis = null;
				try {
					user = (WTUser)SessionHelper.manager.getPrincipal();
					System.out.println("FullName========="+user.getFullName());
					File f = new File(downPath);
					if (!f.exists())
						f.mkdir();

					Streamed sd = data != null ? (Streamed) data.getStreamData().getObject() : null;
					if (sd != null);
					if (sd.retrieveStream() != null);
					
					bis = new BufferedInputStream(sd.retrieveStream());
				} catch (WTException e2) {
					listExcept.add("WTException:======="+e2.toString()+","+doc.getNumber());
					e2.printStackTrace();
				}
		  
	
			
				//String finalFileName = URLEncoder.encode(name, "GB18030");
				System.out.println("c========"+name);
				File outFile = new File(downPath,name);
	            System.out.println("outFile===================="+outFile.getName());
				BufferedOutputStream bos = null;
				try {
					bos = new BufferedOutputStream(new FileOutputStream(outFile));
				} catch (FileNotFoundException e1) {
					
					listExcept.add("FileNotFoundException:======="+e1.toString()+","+doc.getNumber());
					e1.printStackTrace();
				}
				System.out.println("bos====================="+bos);
				byte buff1[] = new byte[1024];
				int read;
				try {
					while ((read = bis.read(buff1)) > 0) {
						bos.write(buff1, 0, read);
					}
					bos.flush();
					bis.close();
					bos.close();
				} catch (IOException e) {
					 listExcept.add("FileNotFoundException:======="+e.toString()+","+doc.getNumber());
					e.printStackTrace();
				}

			}

			
			/**
			 * 插入物料表
			 * @param doc
			 * @param name
			 * @param part
			 * @return
			 * @throws WTException
			 * @throws IOException 
			 * @throws ParseException 
			 * @throws DocumentException 
			 */
			public static  int insertEDFiles(WTDocument doc,String count,List<FileAttachListBean> listBean) throws WTException, IOException, DocumentException, ParseException {
				int i = 0;
//			    for(WTDocument doc: mapDoc.keySet()) {	
			    	PreparedStatement pstmt;
			    	Connection conn = getConn();
//				    System.out.println("mapDoc================"+mapDoc);
				    
				    String sql = "insert into ED_Files (FileNo,FileName,FileTitle,Rev,FileStatus,Proposer,ProposeTime,Approver,ApproveTime,ExtensionName,"
				    		+ "FolderID,Remark,CompanyType,Product,FilePath,FileSize,AttachCount,ApproveResult,FileGuidName)"
				    		+ " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			    try {
			    		
			    	UUID u =  UUID.randomUUID();		 
			        
			        
			        List<String> listVer = checkVer(doc.getNumber());
			        if(!listVer.isEmpty()){
			        WTDocument docLa = (WTDocument)DocUploadAttachmentContent.getLatestVersionByMaster((Master)doc.getMaster());
			        String partVersion = docLa.getVersionInfo().getIdentifier().getValue();
			        System.out.println("listVer======"+listVer);
			        for(String rev : listVer){
			        	String[] arryRev = rev.split("\\.");
			        	if(!partVersion.equals(arryRev[0]) && !arryRev[0].equals("Rev")){
			        		updateIsinvalid(doc.getNumber(), rev);
			        	}
			        }
			        }
			        String partVersion = doc.getVersionInfo().getIdentifier().getValue();
			        String partIteration = doc.getIterationInfo().getIdentifier().getValue();
			        
//			        String filePath = WT_CODEBASE + File.separator + "config" + File.separator + "custom" + File.separator + "ControlledCover.pdf";
//			        File file = new File(filePath);
			        String name = u.toString()+".PDF";
//			        UploadAttachmentContent.downContextsPDF(file, name);
			        File fileCover = new DocCreateCoverPDF().generateYLPDFs(name,doc);
			        
			        System.out.println("conn====================================="+conn);
			        pstmt = (PreparedStatement) conn.prepareStatement(sql);
			        pstmt.setString(1, doc.getNumber());       //部件编号                                     
			        pstmt.setString(2, doc.getNumber()+"_"+doc.getName()+"_"+"封面");                //编号+'_'+版本+'_'+名称
			        pstmt.setString(3, doc.getName());        //部件名称
			        pstmt.setString(4,  partVersion+"."+partIteration);                    //版本
//			        State s = State.toState(part.getState().toString());
//			        String partState = s.getDisplay(SessionHelper.getLocale());
			        pstmt.setString(5,  "2");                           //状态
			        pstmt.setString(6, doc.getCreatorFullName());                     //创建者		        
			        SimpleDateFormat sdf = new SimpleDateFormat(PdfUtil.SIMPLE_DATE_FORMAT);
			        Timestamp createData =  doc.getPersistInfo().getCreateStamp();        
			        pstmt.setString(7,sdf.format(createData));                         //创建时间
			        pstmt.setString(8,doc.getModifierFullName());    //修改者
			        Timestamp updateData = doc.getPersistInfo().getModifyStamp();
			        pstmt.setString(9,sdf.format(updateData));                              //修改时间
			        pstmt.setString(10, ".PDF");                         
			        String folderId = checkFolderId(doc.getNumber());
			        pstmt.setString(11, folderId);    
			        pstmt.setString(12, "");           
			        pstmt.setString(13,"");               
			        pstmt.setString(14, "");
			        pstmt.setString(15, "~/@Upload/Attach/");
			        pstmt.setString(16,  String.valueOf(fileCover.length())); 
			        pstmt.setString(17,  count); 
			        pstmt.setString(18,  "1"); 
			        pstmt.setString(19,  u.toString()); 
			        i = pstmt.executeUpdate();
			        
			        for(FileAttachListBean bean : listBean){
						if(bean.getUerNo().equals(doc.getNumber())){
							String seqNo = checkNo(bean.getUerNo());
							insertFileAttachList(bean,seqNo,doc);
						}
					}
			        
			        System.out.println("pstmt======================================="+pstmt);
			        System.out.println("SqlSize================================================"+i);
			        pstmt.close();
			        conn.close();
			     
			    } catch (SQLException e) {
			    	listExcept.add("SQLException:======="+e.toString()+","+doc.getNumber());
			        e.printStackTrace();
			    }
//			    }
			    return i;
			}
			
			/**
			 * 建立jdbc链接
			 * @return
			 * @throws IOException 
			 */
			private static Connection getConn() throws IOException {
				
				Map<String,String> map = DataConfigurationPacketTable.getSendDocDataBeans();
			    String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
			    String[] arrtPort = map.get("dataPort").split("\\.");
			    String url = "jdbc:sqlserver://"+map.get("dataAddress")+"\\dbo:"+arrtPort[0]+";database="+map.get("dataStorageRoom");
			    System.out.println("url================================"+url);
			    String username = map.get("userName");
			    String password = map.get("password");
			    Connection conn = null;
			    try {
			        Class.forName(driver); //classLoader,加载对应驱动
			        conn = (Connection) DriverManager.getConnection(url, username, password);
			    } catch (ClassNotFoundException e) {
			        e.printStackTrace();
			    } catch (SQLException e) {
			        e.printStackTrace();
			    }
			    return conn;
			}
			
//			 public static String checkFolderId(WTDocument doc) throws IOException{
//	             String folderId = "";
//				  PreparedStatement pstmt;
//			        try {
//			        	 Connection conn = getConn();
//			        	 System.out.println("checkFolderId====================="+conn);
//			            String sql = "select folderId from ED_Folder where  dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\') ='企业空间\\PLM\\影院Barco\\图纸\\结构图纸'";
//			            pstmt = (PreparedStatement)conn.prepareStatement(sql);
//			            System.out.println("checkFolderId============pstmt========="+pstmt);
//			            ResultSet rs = pstmt.executeQuery();
//			            System.out.println("checkFolderId============rs========="+rs);
//			            //5.处理ResultSet
//			            while(rs.next()){
//			                //rs.get+数据库中对应的类型+(数据库中对应的列别名)
//			            	folderId = rs.getString(1);
//			              
//			            }
//			            conn.close();
//			        } catch (SQLException e) {
//			        	listExcept.add("SQLException:======="+e.toString()+","+doc.getNumber());
//				        e.printStackTrace();
//				    }
//			        return folderId;
//			    }
			 
			 public static String checkNo(String docNumber) throws IOException{
	              String id = "";
				  PreparedStatement pstmt;
			        try {
			        	 Connection conn = getConn();
			     
			            String sql = "SELECT Max(ID) FROM ED_Files WHERE FileNo=?";
			            pstmt = (PreparedStatement)conn.prepareStatement(sql);
			            pstmt.setString(1,docNumber);
			            ResultSet rs = pstmt.executeQuery();
			            //5.处理ResultSet
			            while(rs.next()){
			                //rs.get+数据库中对应的类型+(数据库中对应的列别名)
			                id = rs.getString(1);
			              
			            }
			            conn.close();
			        } catch (SQLException e) {
			        	listExcept.add("SQLException:======="+e.toString()+","+docNumber);
				        e.printStackTrace();
				    }
			        return id;
			    }
			
			 public static int updateIsinvalid(String docNumber,String ver) throws IOException{
				 Connection conn = getConn();
				    System.out.println("updateIsinvalidConn================"+conn);
				    int i = 0;
				    String sql = "update ED_Files set isinvalid=? where FileNo=? and Rev=?";
				    PreparedStatement pstmt;
				    try {       
				        pstmt = (PreparedStatement) conn.prepareStatement(sql);
				        pstmt.setString(1, "1");                                     
				        pstmt.setString(2, docNumber);        
				        pstmt.setString(3, ver);    
				        i = pstmt.executeUpdate();
				        System.out.println("updateIsinvalidPstmt======================================="+pstmt);
				        System.out.println("updateIsinvalidSqlSize================================================"+i);
				        pstmt.close();
				        conn.close();
				    } catch (SQLException e) {
				    	listExcept.add("SQLException:======="+e.toString()+","+docNumber);
				        e.printStackTrace();
				    }
				    return i;
			   }
			 
			
			   /**
			 * 插入附件表
			 * @param doc
			 * @param name
			 * @param part
			 * @return
			 * @throws WTException
		     * @throws IOException 
			 */
			public static  int insertFileAttachList(FileAttachListBean bean,String seqNo,WTDocument doc) throws WTException, IOException {
			    Connection conn = getConn();
			    System.out.println("conn================"+conn);
			    int i = 0;
			    String sql = "insert into ED_FileAttachList (FileID,AttachName,GuidName,ExtensionName,UserNo,FileSize)values(?,?,?,?,?,?)";
			    PreparedStatement pstmt;
			    try {       
			        pstmt = (PreparedStatement) conn.prepareStatement(sql);
			        pstmt.setString(1, seqNo);                                     
			        pstmt.setString(2, bean.getAttachName());        //文件名
			        pstmt.setString(3, bean.getGuidName());     //UUID
			        pstmt.setString(4, bean.getExtensionName());  //后缀
			        pstmt.setString(5, bean.getUerNo());          //料号            
			        pstmt.setString(6, bean.getFileSize());                     //文件大小		        
			        
			        System.out.println("FJdata============="+"1"+seqNo+","+bean.getAttachName()+","+bean.getExtensionName()+","+bean.getUerNo()+","+bean.getFileSize());
			        System.out.println("pstmt======================================="+pstmt);
			        i = pstmt.executeUpdate();
			        System.out.println("SqlSize================================================"+i);
			        pstmt.close();
			        conn.close();
			        
			        IBAUtil.forceSetIBAValue(doc, "SendDccState", "成功");
			    } catch (SQLException e) {
			    	listExcept.add("SQLException:======="+e.toString()+","+doc.getNumber());
			        e.printStackTrace();
			    }
			    return i;
			}
			
			  public static List<String> checkVer(String docNumber) throws IOException{
					List<String> list = new ArrayList<>();
		            String ver = "";
					  PreparedStatement pstmt;
				        try {
				        	 Connection conn = getConn();
				     
				            String sql = "SELECT Rev FROM ED_Files WHERE FileNo=?";
				            System.out.println("RevConn=="+conn);
				            pstmt = (PreparedStatement)conn.prepareStatement(sql);
				            pstmt.setString(1,docNumber);
				            System.out.println("RevPstmt==="+pstmt);
				            ResultSet rs = pstmt.executeQuery();
				            //5.处理ResultSet
				            while(rs.next()){
				                //rs.get+数据库中对应的类型+(数据库中对应的列别名)
				            	ver = rs.getString(1);
				            	list.add(ver);
				            }
				            conn.close();
				        } catch (SQLException e) {
				        	listExcept.add("SQLException:======="+e.toString()+","+docNumber);
					        e.printStackTrace();
					    }
				        return list;
				    }
			  
			  public static String checkFolderId(String docNumber) throws IOException{
	              String folderId = "";
				  PreparedStatement pstmt;
			        try {
			        	 Connection conn = getConn();
			        	 System.out.println("checkFolderId====================="+conn);
			            String sql = "select folderId from ED_Folder where  dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\') ='企业空间\\PLM\\影院Barco\\图纸\\结构图纸'";
			            pstmt = (PreparedStatement)conn.prepareStatement(sql);
			            System.out.println("checkFolderId============pstmt========="+pstmt);
			            ResultSet rs = pstmt.executeQuery();
			            System.out.println("checkFolderId============rs========="+rs);
			            //5.处理ResultSet
			            while(rs.next()){
			                //rs.get+数据库中对应的类型+(数据库中对应的列别名)
			            	folderId = rs.getString(1);
			              
			            }
			            conn.close();
			        } catch (SQLException e) {
			        	listExcept.add("SQLException:======="+e.toString()+","+docNumber);
				        e.printStackTrace();
				    }
			        return folderId;
			    }
			  
			  public static void setTxt(String val){
			    	try {
			    		
			    		File fp=new File("/ptc/Windchill_11.0/Windchill/logs/DocAbnormalInformation.txt");
			    		PrintWriter pw=new PrintWriter(fp);		

						
							 pw.println(val);
							 pw.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
			  
}
