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
import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.appo.common.util.Excel2007Handler;
import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.ecn.pdf.PdfUtil;
import ext.com.iba.IBAUtil;
import ext.generic.change.constant.ChangeConstant;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.content.Streamed;
import wt.doc.WTDocument;
import wt.enterprise.Master;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTReference;
import wt.inf.container.WTContainer;
import wt.inf.library.WTLibrary;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.pds.StatementSpec;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.vc.OneOffVersioned;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;
import wt.vc.views.View;
import wt.vc.views.ViewHelper;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WorkItem;
import wt.workflow.work.WorkflowHelper;

/**
 * 历史数据
 * @author rdpplm
 *
 */
public class UploadHistoricalData{
	 private final static Logger logger = Logger.getLogger(UploadHistoricalData.class.getName());
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
    
//    public static void main(String[] args) throws Exception {
//    	 try {
//			try {
//				uploadContentData();
//			} catch (DocumentException e) {
//				e.printStackTrace();
//				logger.error(e.getMessage(),e);
//			} catch (ParseException e) {
//				e.printStackTrace();
//				logger.error(e.getMessage(),e);
//			}
//		} catch (WTException e) {
//			e.printStackTrace();
//			logger.error(e.getMessage(),e);
//		}
//	}
    
//    @Override
//   	public FormResult doOperation(NmCommandBean nmCommandBean, List<ObjectBean> objectBeans) throws WTException {
//       	FormResult doOperation = super.doOperation(nmCommandBean, objectBeans);
//       	boolean flag = SessionServerHelper.manager.setAccessEnforced(false); // 忽略权限
//       	try {
//       		try {
//       			uploadContentData();
//       			setTxt(listExcept.toString());
//   			} catch (Exception e) {
//   				e.printStackTrace();
//   				logger.error(e.getMessage(),e);
//   			}
//       	}finally {
//       		SessionServerHelper.manager.setAccessEnforced(flag); // 添加权限
//       	}
//       	return doOperation;
//       }
	
	  public static Set<String>  uploadContentData() throws Exception{
		  
		  //boolean flag = SessionServerHelper.manager.setAccessEnforced(false); // 忽略权限
		  
		  Map<WTPart,String> mapPart = new HashMap<>();
		  Set<String> setCount = new HashSet<>();
		  System.out.println("=======================action=======1================");
		  try{
		  Map<String,SendDocDataBean> map = ReadSendDocData.getSendDocDataBeans();
		  int j= 0;
		  Map<WTPart,String> mapYLPart = new HashMap<>();
		  Set<FileAttachListYLBean> listYLBean = new HashSet<>();
		  Set<FileAttachListBean> listBean = new HashSet<>();
		  Set<WTPart> setYesProcess = new HashSet<>();
		  Set<WTPart> setNoProcess = new HashSet<>();
		  Set<WTPart> countPart = new HashSet<>();
		  Vector ManufacturingVector = getAllLatestWTParts("Manufacturing", "");
		  System.out.println("ManufacturingVector====="+ManufacturingVector.size());
		  for (int i = 0; i < ManufacturingVector.size(); i++) {
	      		WTPart  part =(WTPart)ManufacturingVector.get(i);
	      		if(part.getLifeCycleState().toString().equals(ChangeConstants.ARCHIVED) || part.getLifeCycleState().toString().equals(ChangeConstants.RELEASED)){
	      		countPart.add(part);
	      		}
	      }
		  Vector vector = getAllLatestWTParts("Design", "");
      	  System.out.println("vector=="+vector.size());
      	  for (int i = 0; i < vector.size(); i++) {
      		WTPart  part =(WTPart)vector.get(i);
      		if(part.getLifeCycleState().toString().equals(ChangeConstants.ARCHIVED) || part.getLifeCycleState().toString().equals(ChangeConstants.RELEASED)){
      		countPart.add(part);
      		}
      	  }
//      		}
//			Set<String> partNumSet = getPartData();
//			for(String partNum : partNumSet){
//				WTPart part = getPartByNumberAndVersion(pentPart);
//				if(part != null){
      	  for(WTPart part : countPart){
			    part = (WTPart)getLatestObjectByMaster(part.getMaster());
			    
			    Set<WorkItem> set = new HashSet<>(); 
					QueryResult qr = WorkflowHelper.service.getWorkItems(part);
					while(qr.hasMoreElements()){
			        WorkItem wi = (WorkItem)qr.nextElement();
			        set.add(wi);
					}
				if(!set.isEmpty()){			    	
					setYesProcess.add(part);       
			  //说明文档
			   QueryResult qrs =  UploadAttachmentUtil.getAssociatedDescribeDocuments(part);
//				for(String key: map.keySet()){
//					SendDocDataBean bean = map.get(key);
//					if(bean.getDescOrRef().equals("说明文档")){
						while (qrs.hasMoreElements()) {
							WTDocument doc = (WTDocument) qrs.nextElement();

							String type = UploadAttachmentUtil.getTypeInternalName(doc);
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
													  UploadAttachmentContent.downContexts(downPath, data, conversionName);
													  j++;
													  mapPart.put(part, String.valueOf(j));
													  FileAttachListBean fileBean = new  FileAttachListBean();
													  fileBean.setFileID("");
													  fileBean.setAttachName(name);
													  fileBean.setGuidName(u.toString()+str);
													  fileBean.setExtensionName(str);
													  fileBean.setUerNo(part.getNumber());
													  fileBean.setFileSize(String.valueOf(data.getFileSize()));
													  listBean.add(fileBean);
													  
													  setCount.add(getOidByObject(part)+"___GF");
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
													  UploadAttachmentContent.downContexts(downPathYL, data, conversionName);
													  mapYLPart.put(part, "1");
													  FileAttachListYLBean fileBean = new  FileAttachListYLBean();
													  fileBean.setFileID("");
													  fileBean.setAttachName(nameYL);
													  fileBean.setGuidName(u.toString()+str);
													  fileBean.setExtensionName(str);
													  fileBean.setUerNo(part.getNumber());
													  fileBean.setFileSize(String.valueOf(data.getFileSize()));
													  listYLBean.add(fileBean);
													  setCount.add(getOidByObject(part)+"___YL");
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
															UploadAttachmentContent.downContexts(downPath, data, conversionName);
															mapPart.put(part, String.valueOf(j));
															FileAttachListBean fileBean = new  FileAttachListBean();
															fileBean.setFileID("");
															fileBean.setAttachName(name);
															fileBean.setGuidName(u.toString()+str);
															fileBean.setExtensionName(str);
															fileBean.setUerNo(part.getNumber());
															fileBean.setFileSize(String.valueOf(data.getFileSize()));
															listBean.add(fileBean);
															setCount.add(getOidByObject(part)+"___GF");
															//YL
															UUID uYL = UUID.randomUUID();
															String[] arryNameYL = name.split("\\.");
															System.out.println("AttName==============="+name);
															String conversionNameYL = uYL.toString()+"."+arryNameYL[1];
															System.out.println("conversionName=========="+conversionNameYL);
															UploadAttachmentContent.downContexts(downPathYL, data, conversionNameYL);
															j++;
															mapYLPart.put(part, "1");
															FileAttachListYLBean fileBeanYL = new  FileAttachListYLBean();
															fileBeanYL.setFileID("");
															fileBeanYL.setAttachName(name);
															fileBeanYL.setGuidName(uYL.toString()+"."+arryNameYL[1]);
															fileBeanYL.setExtensionName("."+arryNameYL[1]);
															fileBeanYL.setUerNo(part.getNumber());
															fileBeanYL.setFileSize(String.valueOf(data.getFileSize()));
															listYLBean.add(fileBeanYL);
															setCount.add(getOidByObject(part)+"___YL");
													 }
													 

												}else {
													
													//光峰
													UUID u = UUID.randomUUID();
													String[] arryName = name.split("\\.");
													System.out.println("AttName==============="+name);
													String conversionName = u.toString()+str;
													System.out.println("conversionName=========="+conversionName);
													UploadAttachmentContent.downContexts(downPath, data, conversionName);
													j++;
													mapPart.put(part, String.valueOf(j));
													FileAttachListBean fileBean = new  FileAttachListBean();
													fileBean.setFileID("");
													fileBean.setAttachName(name);
													fileBean.setGuidName(u.toString()+str);
													fileBean.setExtensionName(str);
													fileBean.setUerNo(part.getNumber());
													fileBean.setFileSize(String.valueOf(data.getFileSize()));
													listBean.add(fileBean);
													setCount.add(getOidByObject(part)+"___GF");
//													mapCount.put(part, "GF");
													//绎立
													UUID uYL = UUID.randomUUID();
													String[] arryNameYL = name.split("\\.");
													System.out.println("AttName==============="+name);
													String conversionNameYL = uYL.toString()+"."+arryNameYL[1];
													System.out.println("conversionName=========="+conversionNameYL);
													UploadAttachmentContent.downContexts(downPathYL, data, conversionNameYL);
													j++;
													mapYLPart.put(part, "1");
													FileAttachListYLBean fileBeanYL = new  FileAttachListYLBean();
													fileBeanYL.setFileID("");
													fileBeanYL.setAttachName(name);
													fileBeanYL.setGuidName(uYL.toString()+"."+arryNameYL[1]);
													fileBeanYL.setExtensionName("."+arryNameYL[1]);
													fileBeanYL.setUerNo(part.getNumber());
													fileBeanYL.setFileSize(String.valueOf(data.getFileSize()));
													listYLBean.add(fileBeanYL);
													setCount.add(getOidByObject(part)+"___YL");
//													mapCount.put(part, "YL");
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
												  UploadAttachmentContent.downContexts(downPath, data, conversionName);
												//得到符合要求的数据
												  j++;
												  mapPart.put(part, String.valueOf(j));
												  FileAttachListBean fileBean = new  FileAttachListBean();
												  fileBean.setFileID("");
												  fileBean.setAttachName(name);
												  fileBean.setGuidName(u.toString()+str);
												  fileBean.setExtensionName(str);
												  fileBean.setUerNo(part.getNumber());
												  fileBean.setFileSize(String.valueOf(data.getFileSize()));
												  listBean.add(fileBean);
												  setCount.add(getOidByObject(part)+"___GF");
//												  mapCount.put(part, "GF");
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
													UploadAttachmentContent.downContexts(downPath, data, conversionName);
													//UUID u = UUID.randomUUID();
													//得到符合要求的数据
		//		                                    setPart.add(part);
													mapPart.put(part, String.valueOf(j));
													FileAttachListBean fileBean = new  FileAttachListBean();
													fileBean.setFileID("");
													fileBean.setAttachName(name);
													fileBean.setGuidName(u.toString()+str);
													fileBean.setExtensionName(str);
													fileBean.setUerNo(part.getNumber());
													fileBean.setFileSize(String.valueOf(data.getFileSize()));
													listBean.add(fileBean);
													setCount.add(getOidByObject(part)+"___GF");
//													mapCount.put(part, "GF");
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
													  UploadAttachmentContent.downContexts(downPath, data, conversionName);
													//得到符合要求的数据
													  j++;
													  mapYLPart.put(part, "2");
													  FileAttachListYLBean fileBean = new  FileAttachListYLBean();
													  fileBean.setFileID("");
													  fileBean.setAttachName(name);
													  fileBean.setGuidName(u.toString()+str);
													  fileBean.setExtensionName(str);
													  fileBean.setUerNo(part.getNumber());
													  fileBean.setFileSize(String.valueOf(data.getFileSize()));
													  listYLBean.add(fileBean);
													  setCount.add(getOidByObject(part)+"___YL");
//													  mapCount.put(part, "YL");
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
														UploadAttachmentContent.downContexts(downPath, data, conversionName);
														//UUID u = UUID.randomUUID();
														//得到符合要求的数据
														j++;
			//		                                    setPart.add(part);
														mapYLPart.put(part, "2");
														FileAttachListYLBean fileBean = new  FileAttachListYLBean();
														fileBean.setFileID("");
														fileBean.setAttachName(name);
														fileBean.setGuidName(u.toString()+str);
														fileBean.setExtensionName(str);
														fileBean.setUerNo(part.getNumber());
														fileBean.setFileSize(String.valueOf(data.getFileSize()));
														listYLBean.add(fileBean);
														setCount.add(getOidByObject(part)+"___YL");
//														mapCount.put(part, "YL");
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
												  UploadAttachmentContent.downContexts(downPath, data, conversionName);
												//得到符合要求的数据
												  j++;
												  	mapPart.put(part, String.valueOf(j));
												  	FileAttachListBean fileBean = new  FileAttachListBean();
													fileBean.setFileID("");
													fileBean.setAttachName(name);
													fileBean.setGuidName(u.toString()+str);
													fileBean.setExtensionName(str);
													fileBean.setUerNo(part.getNumber());
													fileBean.setFileSize(String.valueOf(data.getFileSize()));
													listBean.add(fileBean);
													setCount.add(getOidByObject(part)+"___GF");
//													mapCount.put(part, "GF");
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
												  UploadAttachmentContent.downContexts(downPathYL, data, conversionName);
												//得到符合要求的数据
												  	mapYLPart.put(part, "3");
												  	FileAttachListYLBean fileBean = new  FileAttachListYLBean();
													fileBean.setFileID("");
													fileBean.setAttachName(nameYL);
													fileBean.setGuidName(u.toString()+str);
													fileBean.setExtensionName(str);
													fileBean.setUerNo(part.getNumber());
													fileBean.setFileSize(String.valueOf(data.getFileSize()));
													listYLBean.add(fileBean);
													setCount.add(getOidByObject(part)+"___YL");
//													mapCount.put(part, "YL");
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
															UploadAttachmentContent.downContexts(downPath, data, conversionName);
															//UUID u = UUID.randomUUID();
															//得到符合要求的数据
															j++;
															mapPart.put(part, String.valueOf(j));
															FileAttachListBean fileBean = new  FileAttachListBean();
															fileBean.setFileID("");
															fileBean.setAttachName(name);
															fileBean.setGuidName(u.toString()+str);
															fileBean.setExtensionName(str);
															fileBean.setUerNo(part.getNumber());
															fileBean.setFileSize(String.valueOf(data.getFileSize()));
															listBean.add(fileBean);
															setCount.add(getOidByObject(part)+"___GF");
//															mapCount.put(part, "GF");
															
															    //绎立
																UUID uYL = UUID.randomUUID();
																String[] arryNameYL = name.split("\\.");
																System.out.println("AttName==============="+name);
																String conversionNameYL = uYL.toString()+"."+arryNameYL[1];
																System.out.println("conversionName=========="+conversionNameYL);
																UploadAttachmentContent.downContexts(downPathYL, data, conversionNameYL);
																j++;
																mapYLPart.put(part, "3");
																FileAttachListYLBean fileBeanYL = new  FileAttachListYLBean();
																fileBeanYL.setFileID("");
																fileBeanYL.setAttachName(name);
																fileBeanYL.setGuidName(uYL.toString()+"."+arryNameYL[1]);
																fileBeanYL.setExtensionName("."+arryNameYL[1]);
																fileBeanYL.setUerNo(part.getNumber());
																fileBeanYL.setFileSize(String.valueOf(data.getFileSize()));
																listYLBean.add(fileBeanYL);
																setCount.add(getOidByObject(part)+"___YL");
//																mapCount.put(part, "YL");
													 }
													 
												 }else{
													 	UUID u = UUID.randomUUID();
														String[] arryName = name.split("\\.");
														System.out.println("AttName==============="+name);
														String conversionName = u.toString()+str;
														System.out.println("conversionName=========="+conversionName);
														UploadAttachmentContent.downContexts(downPath, data, conversionName);
														//UUID u = UUID.randomUUID();
														//得到符合要求的数据
														j++;
														mapPart.put(part, String.valueOf(j));
														FileAttachListBean fileBean = new  FileAttachListBean();
														fileBean.setFileID("");
														fileBean.setAttachName(name);
														fileBean.setGuidName(u.toString()+str);
														fileBean.setExtensionName(str);
														fileBean.setUerNo(part.getNumber());
														fileBean.setFileSize(String.valueOf(data.getFileSize()));
														listBean.add(fileBean);
														setCount.add(getOidByObject(part)+"___GF");
//														mapCount.put(part, "GF");
														
														//绎立
														UUID uYL = UUID.randomUUID();
														String[] arryNameYL = name.split("\\.");
														System.out.println("AttName==============="+name);
														String conversionNameYL = uYL.toString()+"."+arryNameYL[1];
														System.out.println("conversionName=========="+conversionNameYL);
														UploadAttachmentContent.downContexts(downPathYL, data, conversionNameYL);
														j++;
														mapYLPart.put(part, "3");
														FileAttachListYLBean fileBeanYL = new  FileAttachListYLBean();
														fileBeanYL.setFileID("");
														fileBeanYL.setAttachName(name);
														fileBeanYL.setGuidName(uYL.toString()+"."+arryNameYL[1]);
														fileBeanYL.setExtensionName("."+arryNameYL[1]);
														fileBeanYL.setUerNo(part.getNumber());
														fileBeanYL.setFileSize(String.valueOf(data.getFileSize()));
														listYLBean.add(fileBeanYL);
														setCount.add(getOidByObject(part)+"___YL");
//														mapCount.put(part, "YL");
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
													  UploadAttachmentContent.downContexts(downPath, data, conversionName);
													//得到符合要求的数据
													  j++;
													  	mapPart.put(part, String.valueOf(j));
													  	FileAttachListBean fileBean = new  FileAttachListBean();
														fileBean.setFileID("");
														fileBean.setAttachName(name);
														fileBean.setGuidName(u.toString()+str);
														fileBean.setExtensionName(str);
														fileBean.setUerNo(part.getNumber());
														fileBean.setFileSize(String.valueOf(data.getFileSize()));
														listBean.add(fileBean);
														setCount.add(getOidByObject(part)+"___GF");
//														mapCount.put(part, "GF");
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
																UploadAttachmentContent.downContexts(downPath, data, conversionName);
																//UUID u = UUID.randomUUID();
																//得到符合要求的数据
																j++;
																mapPart.put(part, String.valueOf(j));
																FileAttachListBean fileBean = new  FileAttachListBean();
																fileBean.setFileID("");
																fileBean.setAttachName(name);
																fileBean.setGuidName(u.toString()+str);
																fileBean.setExtensionName(str);
																fileBean.setUerNo(part.getNumber());
																fileBean.setFileSize(String.valueOf(data.getFileSize()));
																listBean.add(fileBean);
																setCount.add(getOidByObject(part)+"___GF");
//																mapCount.put(part, "GF");
														 }
													 }else{
														 	UUID u = UUID.randomUUID();
															String[] arryName = name.split("\\.");
															System.out.println("AttName==============="+name);
															String conversionName = u.toString()+str;
															System.out.println("conversionName=========="+conversionName);
															UploadAttachmentContent.downContexts(downPath, data, conversionName);
															//UUID u = UUID.randomUUID();
															//得到符合要求的数据
															j++;
															mapPart.put(part, String.valueOf(j));
															FileAttachListBean fileBean = new  FileAttachListBean();
															fileBean.setFileID("");
															fileBean.setAttachName(name);
															fileBean.setGuidName(u.toString()+str);
															fileBean.setExtensionName(str);
															fileBean.setUerNo(part.getNumber());
															fileBean.setFileSize(String.valueOf(data.getFileSize()));
															listBean.add(fileBean);
															setCount.add(getOidByObject(part)+"___GF");
//															mapCount.put(part, "GF");
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
												  UploadAttachmentContent.downContexts(downPath, data, conversionName);
												  //得到符合要求的数据
												    mapYLPart.put(part, "2");
												  	FileAttachListYLBean fileBean = new  FileAttachListYLBean();
													fileBean.setFileID("");
													fileBean.setAttachName(name);
													fileBean.setGuidName(u.toString()+str);
													fileBean.setExtensionName(str);
													fileBean.setUerNo(part.getNumber());
													fileBean.setFileSize(String.valueOf(data.getFileSize()));
													listYLBean.add(fileBean);
													setCount.add(getOidByObject(part)+"___YL");
//													mapCount.put(part, "YL");
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
															UploadAttachmentContent.downContexts(downPath, data, conversionName);
															//UUID u = UUID.randomUUID();
															//得到符合要求的数据
															j++;
															mapYLPart.put(part, "2");
														  	FileAttachListYLBean fileBean = new  FileAttachListYLBean();
															fileBean.setFileID("");
															fileBean.setAttachName(name);
															fileBean.setGuidName(u.toString()+str);
															fileBean.setExtensionName(str);
															fileBean.setUerNo(part.getNumber());
															fileBean.setFileSize(String.valueOf(data.getFileSize()));
															listYLBean.add(fileBean);
															setCount.add(getOidByObject(part)+"___YL");
//															mapCount.put(part, "YL");
													 }
												 }else{
													 	UUID u = UUID.randomUUID();
														String[] arryName = name.split("\\.");
														System.out.println("AttName==============="+name);
														String conversionName = u.toString()+str;
														System.out.println("conversionName=========="+conversionName);
														UploadAttachmentContent.downContexts(downPath, data, conversionName);
														//UUID u = UUID.randomUUID();
														//得到符合要求的数据
														j++;
														mapYLPart.put(part, "2");
													  	FileAttachListYLBean fileBean = new  FileAttachListYLBean();
														fileBean.setFileID("");
														fileBean.setAttachName(name);
														fileBean.setGuidName(u.toString()+str);
														fileBean.setExtensionName(str);
														fileBean.setUerNo(part.getNumber());
														fileBean.setFileSize(String.valueOf(data.getFileSize()));
														listYLBean.add(fileBean);
														setCount.add(getOidByObject(part)+"___YL");
//														mapCount.put(part, "YL");
												 }
										 }	
									  
								  }
								  }
							}
//						} 
//						}
					}  
				}else{
					setNoProcess.add(part);
				}
					  
      	  }
			
//			     System.out.println("有流程并且归档、开发的部件数量============"+setYesProcess.size());
//			     System.out.println("无流程并且归档、开发的部件数量============="+setNoProcess.size());
//			    if(!mapPart.isEmpty()){
//			    System.out.println("发送成功部件数量:========="+mapPart.size());
//			    	for(WTPart part: mapPart.keySet()) {
//			    	insertEDFiles(part,mapPart.get(part),listBean);
//			    	}
//			    }
      	 if(!mapPart.isEmpty()){
      		System.out.println("发送成功GF部件数量："+mapPart.size());
      		UploadAttachmentContent.insertEDFiles(mapPart,listBean);
      	 }
      	 
      	if(!mapYLPart.isEmpty()){
      		System.out.println("发送成功YL部件数量："+mapYLPart.size());
      		System.out.println("listYLBean============================"+listYLBean);
      		UploadAttachmentYLContent.insertEDFiles(mapYLPart,listYLBean);
      	 }
      	 
      	Set<String> filesCount = UploadAttachmentContent.checkFilesCount();
		UploadAttachmentContent.updateFilesCount(filesCount);
		
		Set<String> filesCount2 = UploadAttachmentYLContent.checkFilesCount();
		UploadAttachmentYLContent.updateFilesCount(filesCount2);
      	 return setCount;
		  } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e);
			System.out.println("IOException======================"+e);
		} catch (PropertyVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e);
			System.out.println("PropertyVetoException======================"+e);
		}finally {
			  //SessionServerHelper.manager.setAccessEnforced(flag); // 添加权限
		}
		   System.out.println("=========================END========================================");
		   return setCount;
		  
	  }
	  
	  
	  public  Workbook exportData() throws WTException {
			 try{
				    String strTemplate = WT_CODEBASE + File.separator + "config" + File.separator + "custom" + File.separator + "sengNumber.xlsx";
		        	Excel2007Handler excelHander = new Excel2007Handler(strTemplate);          
		        	Set<String> set = uploadContentData();
//		        	System.out.println("mapCount=================================="+map);
		            excelHander.switchCurrentSheet("Sheet1");
		            int i = 0;
		            int j = 0;
		            for(String str: set) {
						String[] arry = str.split("___");
		            	WTPart part = (WTPart)getObjectByOid(arry[0]);
		            	if(arry[1].equals("GF")){
		            	i++;
		            	int rowNum = i+1;
		            	int iCol = 0;
		            	
		            	excelHander.setStringValue(rowNum, iCol++, String.valueOf(i));
		            	excelHander.setStringValue(rowNum, iCol++, part.getNumber());
		            	excelHander.setStringValue(rowNum, iCol++, part.getName());
		            	
		            	String version = part.getVersionInfo().getIdentifier().getValue();
    			        String iteration = part.getIterationInfo().getIdentifier().getValue();
		            	excelHander.setStringValue(rowNum, iCol++, version+"."+iteration);
		            	}else if(arry[1].equals("YL")) {
		            		j++;
			            	int rowNum = j+1;
			            	int iCol = 4;
			            	
			            	excelHander.setStringValue(rowNum, iCol++, String.valueOf(j));
			            	excelHander.setStringValue(rowNum, iCol++, part.getNumber());
			            	excelHander.setStringValue(rowNum, iCol++, part.getName());
			            	
			            	String version = part.getVersionInfo().getIdentifier().getValue();
	    			        String iteration = part.getIterationInfo().getIdentifier().getValue();
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
			 * 下载方法
			 * 
			 * @param downPath
			 * @param data
			 * @param name
			 * @throws WTException
			 * @throws FileNotFoundException
			 * @throws IOException
			 */
			public static  void downContexts(String downPath, ApplicationData data, String name,WTPart part){
				
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
					listExcept.add("WTException:======="+e2.toString()+","+part.getNumber());
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
					
					listExcept.add("FileNotFoundException:======="+e1.toString()+","+part.getNumber());
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
					 listExcept.add("FileNotFoundException:======="+e.toString()+","+part.getNumber());
					e.printStackTrace();
				}

			}
	  
	
//	  /**
//		 * 插入物料表
//		 * @param doc
//		 * @param name
//		 * @param part
//		 * @return
//		 * @throws WTException
//		 * @throws IOException 
//		 * @throws ParseException 
//		 * @throws DocumentException 
//		 */
//		public static  int insertEDFiles(WTPart part,String count,List<FileAttachListBean> listBean) throws WTException, IOException, DocumentException, ParseException {
//			int i = 0;
//			
//				Connection conn = getConn(part.getNumber());
//			    String sql = "insert into ED_Files (FileNo,FileName,FileTitle,Rev,FileStatus,Proposer,ProposeTime,Approver,ApproveTime,ExtensionName,"
//			    		+ "FolderID,Remark,CompanyType,Product,FilePath,FileSize,AttachCount,ApproveResult,FileGuidName)"
//			    		+ " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
//			    PreparedStatement pstmt;
//		    try {		
//		    	UUID u =  UUID.randomUUID();		 
//		        
//		        
//		        List<String> listVer = checkVer(part.getNumber());
//		        if(!listVer.isEmpty()){
//		        WTPart partLa = (WTPart)getLatestObjectByMaster(part.getMaster());
//		        String partVersion = partLa.getVersionInfo().getIdentifier().getValue();
//		        System.out.println("listVer======"+listVer);
//		        for(String rev : listVer){
//		        	String[] arryRev = rev.split("\\.");
//		        	if(!partVersion.equals(arryRev[0])){
//		        		updateIsinvalid(part.getNumber(), rev);
//		        	}
//		        }
//		        }
//		        String partVersion = part.getVersionInfo().getIdentifier().getValue();
//		        String partIteration = part.getIterationInfo().getIdentifier().getValue();
//		        
////		        String filePath = WT_CODEBASE + File.separator + "config" + File.separator + "custom" + File.separator + "ControlledCover.pdf";
////		        File file = new File(filePath);
//		        String name = u.toString()+".PDF";
////		        downContextsPDF(file, name);
//		        System.out.println("partNumber+"+part.getNumber());
//		        File fileCover = new CreateCoverPDF().generatePDFs(name,part);
//		        System.out.println("SQLServerException=============="+part.getNumber());
//		        pstmt = (PreparedStatement) conn.prepareStatement(sql);
//		        pstmt.setString(1, part.getNumber());       //部件编号                                     
//		        pstmt.setString(2, part.getNumber()+"_"+part.getName()+"_"+"封面");                //编号+'_'+版本+'_'+名称
//		        pstmt.setString(3, part.getName());        //部件名称
//		        pstmt.setString(4,  partVersion+"."+partIteration);                    //版本
////		        State s = State.toState(part.getState().toString());
////		        String partState = s.getDisplay(SessionHelper.getLocale());
//		        pstmt.setString(5,  "2");                           //状态
//		        pstmt.setString(6, part.getCreatorFullName());                     //创建者		        
//		        SimpleDateFormat sdf = new SimpleDateFormat(PdfUtil.SIMPLE_DATE_FORMAT);
//		        Timestamp createData =  part.getPersistInfo().getCreateStamp();        
//		        pstmt.setString(7,sdf.format(createData));                         //创建时间
//		        pstmt.setString(8,part.getModifierFullName());    //修改者
//		        Timestamp updateData = part.getPersistInfo().getModifyStamp();
//		        pstmt.setString(9,sdf.format(updateData));                              //修改时间
//		        pstmt.setString(10, ".PDF");                         
//		        String folderId = checkFolderId(part);
//		        pstmt.setString(11, folderId);    
//		        pstmt.setString(12, "");           
//		        pstmt.setString(13,"");               
//		        pstmt.setString(14, "");
//		        pstmt.setString(15, "~/@Upload/Attach/");
//		        pstmt.setString(16,  String.valueOf(fileCover.length())); 
//		        pstmt.setString(17,  count); 
//		        pstmt.setString(18,  "1"); 
//		        pstmt.setString(19,  u.toString()); 
//		        i = pstmt.executeUpdate();
//		        
//		        for(FileAttachListBean bean : listBean){
//					if(bean.getUerNo().equals(part.getNumber())){
//						String seqNo = checkNo(bean.getUerNo());
//						insertFileAttachList(bean,seqNo,part);
//					}
//				}
//		        
//		        System.out.println("pstmt======================================="+pstmt);
//		        System.out.println("SqlSize================================================"+i);
//		        pstmt.close();
//		        conn.close();
//		      
//		    } catch (SQLException e) {
//		    	listExcept.add("SQLException:======="+e.toString()+","+part.getNumber());
//		        e.printStackTrace();
//		    }
//			 return i;
//		}
		
		 public static String checkNo(String partNumber) throws IOException{
             String id = "";
			  PreparedStatement pstmt;
		        try {
		        	 Connection conn = getConn(partNumber);
		     
		            String sql = "SELECT Max(ID) FROM ED_Files WHERE FileNo=?";
		            pstmt = (PreparedStatement)conn.prepareStatement(sql);
		            pstmt.setString(1,partNumber);
		            ResultSet rs = pstmt.executeQuery();
		            //5.处理ResultSet
		            while(rs.next()){
		                //rs.get+数据库中对应的类型+(数据库中对应的列别名)
		                id = rs.getString(1);
		              
		            }
		            conn.close();
		        } catch (SQLException e) {
		        	listExcept.add("SQLException:======="+e.toString()+","+partNumber);
			        e.printStackTrace();
			    }
		        return id;
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
		public static  int insertFileAttachList(FileAttachListBean bean,String seqNo,WTPart part) throws WTException, IOException {
		    Connection conn = getConn(part.getNumber());
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
		        i = pstmt.executeUpdate();
		        System.out.println("FJdata============="+"1"+seqNo+","+bean.getAttachName()+","+bean.getExtensionName()+","+bean.getUerNo()+","+bean.getFileSize());
		        System.out.println("pstmt======================================="+pstmt);
		        System.out.println("SqlSize================================================"+i);
		        pstmt.close();
		        conn.close();
		        
		        IBAUtil.forceSetIBAValue(part, "SendDccState", "成功");
		    } catch (SQLException e) {
		    	listExcept.add("SQLException:======="+e.toString()+","+part.getNumber());
		        e.printStackTrace();
		    }
		    return i;
		}
		
		
//		 public static String checkFolderId(WTPart part) throws IOException{
//             String folderId = "";
//			  PreparedStatement pstmt;
//		        try {
//		        	 Connection conn = getConn(part.getNumber());
//		        	 System.out.println("checkFolderId====================="+conn);
//		            String sql = "select folderId from ED_Folder where  dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\') ='企业空间\\PLM\\影院Barco\\图纸\\结构图纸'";
//		            pstmt = (PreparedStatement)conn.prepareStatement(sql);
//		            System.out.println("checkFolderId============pstmt========="+pstmt);
//		            ResultSet rs = pstmt.executeQuery();
//		            System.out.println("checkFolderId============rs========="+rs);
//		            //5.处理ResultSet
//		            while(rs.next()){
//		                //rs.get+数据库中对应的类型+(数据库中对应的列别名)
//		            	folderId = rs.getString(1);
//		              
//		            }
//		            conn.close();
//		        } catch (SQLException e) {
//		        	listExcept.add("SQLException:======="+e.toString()+","+part.getNumber());
//			        e.printStackTrace();
//			    }
//		        return folderId;
//		    }
		 
		 /**
			 * 建立jdbc链接
			 * @return
			 * @throws IOException 
			 */
			private static  Connection getConn(String partNumber) throws IOException {
				
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
			    	listExcept.add("ClassNotFoundException:======="+e.toString()+","+partNumber);
			        e.printStackTrace();
			    } catch (SQLException e) {
			    	listExcept.add("SQLException:======="+e.toString()+","+partNumber);
			        e.printStackTrace();
			    }
			    return conn;
			}
		 
		/**
		 * 历史数据路径
		 * @return
		 * @throws IOException
		 */
		public static Set<String> getPartData() throws IOException{
	        Set<String> set = new HashSet<>();
	        
	        String filePath = WT_CODEBASE + File.separator + "config" + File.separator + "custom" + File.separator + "historicalDataPart.xlsx";
	        Excel2007Handler handler = new Excel2007Handler(filePath);
	        //Excel2007Handler handler = new Excel2007Handler("D:\\ideaspace\\gfgd\\windchill\\codebase\\ext\\appo\\doc\\sendDocData.xlsx");
	        handler.switchCurrentSheet(0);
	        int rowCount = handler.getSheetRowCount();
	        for (int i = 1; i < rowCount; i++) {
	            set.add(null2blank(handler.getStringValue(i, 0)));     //物料编号)
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
		
		public static Vector getAllLatestWTParts(String viewName, String number) throws Exception
		{
			QuerySpec qs = new QuerySpec(WTPart.class);

			View view = ViewHelper.service.getView(viewName);
			SearchCondition sc = new SearchCondition(WTPart.class, "view.key.id", SearchCondition.EQUAL, view.getPersistInfo().getObjectIdentifier().getId());
			qs.appendWhere(sc);
			if (number.trim().length() > 0)
			{
				qs.appendAnd();
				SearchCondition scNumber = new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL, number.toUpperCase());
				qs.appendWhere(scNumber);
			}

			SearchCondition scLatestIteration = new SearchCondition(WTPart.class, WTAttributeNameIfc.LATEST_ITERATION, SearchCondition.IS_TRUE);
			qs.appendAnd();
			qs.appendWhere(scLatestIteration);

			QueryResult qr = PersistenceHelper.manager.find(qs);
			if (qr != null && qr.hasMoreElements())
				qr = (new LatestConfigSpec()).process(qr);

			if (qr != null && qr.hasMoreElements())
				return qr.getObjectVectorIfc().getVector();

			return new Vector();
		}
		
		 public static WTPart getPartByNumberAndVersion(String number) throws WTException {
		        WTPart wtpart = null;
		        QuerySpec qs = new QuerySpec(WTPart.class);
		        qs.appendWhere(new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL, number), new int[]{0});
		        QueryResult queryresult = PersistenceHelper.manager.find((StatementSpec) qs);
		        if (queryresult.hasMoreElements()) {
		            wtpart = (WTPart) queryresult.nextElement();
		        }

		        return wtpart;
		    }
		 
		 public static int updateIsinvalid(String partNumber,String ver) throws IOException{
			 Connection conn = getConn(partNumber);
			    System.out.println("updateIsinvalidConn================"+conn);
			    int i = 0;
			    String sql = "update ED_Files set isinvalid=? where FileNo=? and Rev=?";
			    PreparedStatement pstmt;
			    try {       
			        pstmt = (PreparedStatement) conn.prepareStatement(sql);
			        pstmt.setString(1, "1");                                     
			        pstmt.setString(2, partNumber);        
			        pstmt.setString(3, ver);    
			        i = pstmt.executeUpdate();
			        System.out.println("updateIsinvalidPstmt======================================="+pstmt);
			        System.out.println("updateIsinvalidSqlSize================================================"+i);
			        pstmt.close();
			        conn.close();
			    } catch (SQLException e) {
			    	listExcept.add("SQLException:======="+e.toString()+","+partNumber);
			        e.printStackTrace();
			    }
			    return i;
		   }
		 
		 
		 /**
		     * 获取最新大版本的最新小版本
		     *
		     * @param master
		     * @return
		     */
		    public static Persistable getLatestObjectByMaster(Master master) {
		        try {
		            if (master != null) {
		                QueryResult qrVersions = VersionControlHelper.service.allVersionsOf(master);
		                while (qrVersions.hasMoreElements()) {
		                    Persistable p = (Persistable) qrVersions.nextElement();
		                    if (!VersionControlHelper.isAOneOff((OneOffVersioned) p)) {
		                        return p;
		                    }
		                }
		            }
		        } catch (QueryException e) {
		            e.printStackTrace();
		        } catch (WTException e) {
		            e.printStackTrace();
		        }
		        return null;
		    }
		 
		    
		    public static List<String> checkVer(String partNumber) throws IOException{
				List<String> list = new ArrayList<>();
	            String ver = "";
				  PreparedStatement pstmt;
			        try {
			        	 Connection conn = getConn(partNumber);
			     
			            String sql = "SELECT Rev FROM ED_Files WHERE FileNo=?";
			            System.out.println("RevConn=="+conn);
			            pstmt = (PreparedStatement)conn.prepareStatement(sql);
			            pstmt.setString(1,partNumber);
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
			        	listExcept.add("SQLException:======="+e.toString()+","+partNumber);
				        e.printStackTrace();
				    }
			        return list;
			    }
		   
		 
		 public static void setTxt(String val){
		    	try {
		    		
		    		File fp=new File("/ptc/Windchill_11.0/Windchill/logs/AbnormalInformation.txt");
		    		PrintWriter pw=new PrintWriter(fp);		

					
						 pw.println(val);
						 pw.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		 
		 /**
		     * GET OID BY PERSISTABLE
		     *
		     * @param p
		     * @return
		     * @throws WTException
		     */
		    public static String getOidByObject(Persistable p) {
		        String oid = "";
		        if (p instanceof WfProcess) {
		            oid = "OR:wt.workflow.engine.WfProcess:" + p.getPersistInfo().getObjectIdentifier().getId();
		            return oid;
		        }
		        if (p != null) {
		            oid = "OR:" + p.toString();
		        }
		        return oid;
		    }
		    
		    public static Persistable getObjectByOid(String oid) throws WTException {
		        Persistable p = null;

		        boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		        try {
		            ReferenceFactory referencefactory = new ReferenceFactory();
		            WTReference wtreference = referencefactory.getReference(oid);
		            p = wtreference.getObject();
		        } catch (Exception e) {
		            e.printStackTrace();
		        } finally {
		            SessionServerHelper.manager.setAccessEnforced(flag);
		        }

		        return p;
		    }
		 
	
}
