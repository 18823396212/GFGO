package ext.appo.doc.uploadDoc.processor;

import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.lowagie.text.DocumentException;
import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.forms.FormResultAction;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.appo.doc.uploadDoc.DataConfigurationPacketTable;
import ext.appo.doc.uploadDoc.DocUploadAttachmentContent;
import ext.appo.doc.uploadDoc.DocUploadAttachmentYLContent;
import ext.appo.doc.uploadDoc.FileAttachListBean;
import ext.appo.doc.uploadDoc.FileAttachListYLBean;
import ext.appo.doc.uploadDoc.ReadSendDocData;
import ext.appo.doc.uploadDoc.SendDocDataBean;
import ext.appo.doc.uploadDoc.UploadAttachmentContent;
import ext.appo.doc.uploadDoc.UploadAttachmentUtil;
import ext.appo.doc.uploadDoc.UploadAttachmentYLContent;
import ext.appo.ecn.pdf.PdfUtil;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.doc.WTDocument;
import wt.fc.QueryResult;
import wt.inf.container.WTContainer;
import wt.inf.library.WTLibrary;
import wt.session.SessionServerHelper;
import wt.util.WTException;

/**
 * @author HYJ&NJH 四合一流程启动入口处理类
 */
public class DocUploadAttachmentProcessor extends DefaultObjectFormProcessor {

	@Override
	public FormResult doOperation(NmCommandBean nmCommandBean, List<ObjectBean> objectBeans) throws WTException {
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false); // 忽略权限
		FormResult doOperation = super.doOperation(nmCommandBean, objectBeans);
		  System.out.println("=======================action=======================");
		  try{
		Object refObject = nmCommandBean.getActionOid().getRefObject();
		Map<String,SendDocDataBean> map = ReadSendDocData.getSendDocDataBeans();
		int j= 0;
		Map<WTDocument,String> mapYLDoc = new HashMap<>();
		Set<FileAttachListYLBean> listYLBean = new HashSet<>();
		Map<WTDocument,String> mapDoc = new HashMap<>();
		Set<FileAttachListBean> listBean = new HashSet<>();
		System.out.println("refObject=================================="+refObject);
		if (refObject != null && refObject instanceof WTDocument) {
			WTDocument doc = (WTDocument) refObject;
//			for(String key: map.keySet()){
//				SendDocDataBean bean = map.get(key);
//				System.out.println("bean.getDescOrRef()=============================="+bean.getDescOrRef());
//				if(bean.getDescOrRef().equals("参考文档")){
					TypeIdentifier typeIdentifier = TypeIdentifierUtility.getTypeIdentifier(doc);
					String typename = typeIdentifier.getTypename();
					System.out.println("typename=============================="+typename);
					if (typename.contains("com.ptc.ReferenceDocument")) {
						System.out.println("doc============="+doc);
//						String[] arry = key.split("__");
						String type = UploadAttachmentUtil.getTypeInternalName(doc);
						String passageInventory = (String)PdfUtil.getIBAObjectValue(doc, "SmallDocType");
						System.out.println("type======="+type);
						System.out.println("passageInventory======="+passageInventory);
//						System.out.println("arry================"+arry.toString());
						if(map.keySet().toString().contains(type)){
					  
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
										 }
									}
							
									//=======================================附件end===============================================
								}
				     
						}
//				  }
//				}
				
			}
	  
//			for(String key: map.keySet()){
//				SendDocDataBean bean = map.get(key);
//				System.out.println("getDescOrRef==========="+bean.getDescOrRef());
//				if(bean.getDescOrRef().equals("说明文档")){
////					List<WTPart> listDesPart = getAllPartByDoc(doc);                          //取说明部件，暂时预留			
//					TypeIdentifier typeIdentifier = TypeIdentifierUtility.getTypeIdentifier(doc);
//					String typename = typeIdentifier.getTypename();
					if (typename.contains("com.plm.description_square_document")) {
//						String[] arry = key.split("__");
						String type = UploadAttachmentUtil.getTypeInternalName(doc);
						String passageInventory = (String)PdfUtil.getIBAObjectValue(doc, "SmallDocType");
						System.out.println("type======="+type);
						System.out.println("passageInventory============="+passageInventory);
//						System.out.println("arry[0]========"+arry[0]);
//						System.out.println("arry[1]========"+arry[1]);
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
									  	}
									 }
									 
									//======================================附件action============================================
									 QueryResult pdf = ContentHelper.service.getContentsByRole(doc, ContentRoleType.SECONDARY);
										while (pdf.hasMoreElements()) {
											ApplicationData data = (ApplicationData) pdf.nextElement();
											System.out.println("AttApplicationData========+"+data);
//											String downPath = "smb://rdpplm:rdpplm123@LAPTOP-UL273M6G/excel/";
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
										}
									 }
									 //=======================================主内容end==========================
									 
									 //======================================附件action============================================
									 QueryResult pdf = ContentHelper.service.getContentsByRole(doc, ContentRoleType.SECONDARY);
										while (pdf.hasMoreElements()) {
											ApplicationData data = (ApplicationData) pdf.nextElement();
											System.out.println("AttApplicationData========+"+data);
//											String downPath = "smb://rdpplm:rdpplm123@LAPTOP-UL273M6G/excel/";     
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
											}
										 }
										 //=======================================主内容end==========================
										 
										 //======================================附件action============================================
										 QueryResult pdf = ContentHelper.service.getContentsByRole(doc, ContentRoleType.SECONDARY);
											while (pdf.hasMoreElements()) {
												ApplicationData data = (ApplicationData) pdf.nextElement();
												System.out.println("AttApplicationData========+"+data);
//												String downPath = "smb://rdpplm:rdpplm123@LAPTOP-UL273M6G/excel/";     
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
											 }
									 }	
							  }  
							  }
						}
				}
//			  }
//			}
			
		}
		if(!mapDoc.isEmpty()) {
		DocUploadAttachmentContent.insertEDFiles(mapDoc,listBean);
		}
		
		if(!mapYLDoc.isEmpty()) {
			DocUploadAttachmentYLContent.insertEDFiles(mapYLDoc,listYLBean);
		}
		
		Set<String> filesCount = UploadAttachmentContent.checkFilesCount();
		UploadAttachmentContent.updateFilesCount(filesCount);
		
		Set<String> filesCount2 = UploadAttachmentYLContent.checkFilesCount();
		UploadAttachmentYLContent.updateFilesCount(filesCount2);
		  } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			  SessionServerHelper.manager.setAccessEnforced(flag); // 添加权限
		  }

		doOperation.setNextAction(FormResultAction.JAVASCRIPT);
		doOperation.setJavascript("window.close();window.opener.PTC.navigation.reload();");   //刷新父页面
		return doOperation;
	}
}
