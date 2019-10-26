package ext.generic.esignature.delegate;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.log4j.Logger;

import wt.content.ApplicationData;
import wt.content.ContentHolder;
import wt.content.ContentServerHelper;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTKeyedHashMap;
import wt.iba.value.IBAHolder;
import wt.inf.container.WTContained;
import wt.inf.container.WTContainer;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.pom.Transaction;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTMessage;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.vc.IterationIdentifier;
import wt.vc.VersionIdentifier;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WorkItem;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import ext.com.core.CoreUtil;
import ext.com.org.OrgUtil;
import ext.generic.esignature.bean.SignatureBean;
import ext.generic.esignature.constant.EsignatureConstant;
import ext.generic.esignature.handler.AttributeHandler;
import ext.generic.esignature.handler.EsignatureHandler;
import ext.generic.esignature.handler.ExcelCfgHandler;
import ext.generic.esignature.handler.PDFSizeHandler;
import ext.generic.esignature.handler.PdfSourceHandler;
import ext.generic.esignature.handler.ProcessInfoHandler;
import ext.generic.esignature.util.EsignatureUtil;
import ext.generic.esignature.util.ReadConfigUtil;
import ext.pi.core.PIAttributeHelper;

public class EsignatureDelegate {
	
	private static final Logger logger = LogR.getLogger(EsignatureDelegate.class.getName());
	
	public static WTProperties wtProperties;
	public static String TEMP_PATH = "";
	public static Locale locale = null;
	
	private static final String RESOURCE = "ext.generic.esignature.resource.EsignatureResource";
	private static ArrayList excludeWF = new ArrayList();;
	
	static { 
		try {
			wtProperties = WTProperties.getLocalProperties();
			TEMP_PATH = wtProperties.getProperty("wt.temp");
			locale = SessionHelper.manager.getLocale();
			
			ReadConfigUtil config = ReadConfigUtil.getInstance();
			Map map = config.readProperties();
			if(map.containsKey("ExcludeWorkflow")){
				String excludeWFStr = (String) map.get("ExcludeWorkflow");
				String[] ewfs = excludeWFStr.split(",");
				for (int i = 0; i < ewfs.length; i++) {
					excludeWF.add(ewfs[i]);
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		catch(WTException e) {
			e.printStackTrace();
		}
		
	}
	
	private WfProcess process = null;
	private WTArrayList arrayList = null;
	//单个文件签审时,初始化的变量
	
	private Persistable persistable = null;
	
	private PdfReader reader = null;
	private PdfStamper stamp = null;
	private Map<String, WorkItem>  keyWorkItem = null;
	private List<SignatureBean> valueSignatureBean = null;
	private Map<String, SignatureBean> keySignatureBeans=null;//add by kyang
	
	
	public EsignatureDelegate() {
		
	}
	
	public EsignatureDelegate(WTArrayList arrayList, WfProcess process) {
		this.arrayList = arrayList;
		this.process = process;
		logger.debug(">>rchen EsignatureDelegate:"+excludeWF);
		if(process!=null){
			if(excludeWF.contains(this.process.getTemplate().getName())){
				this.process = null;
			}
		}
	}
	
	/**
	 * 签名主方法
	 * @return
	 * @throws WTException
	 * @throws RemoteException 
	 */
	public WTKeyedHashMap eSignature() throws WTException, RemoteException {
		
		WTKeyedHashMap wtMap = new WTKeyedHashMap();
		//判断是否需要签名
		needEsignature();

		if(arrayList!=null) {
			for(int i=0; i< arrayList.size(); i++) {
				Persistable persistable = arrayList.getPersistable(i);
				this.persistable = persistable;
				String errorMsg = eSignaturePersistable();
				if(errorMsg!=null && (!errorMsg.isEmpty())) {
					wtMap.put(persistable, errorMsg);
				}
			}
			
		}
		return wtMap;
		
	}
	
	
	
	/**
	 * 电子签名主方法
	 * @param doc
	 * @return
	 * @throws PropertyVetoException 
	 * @throws WTException 
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	protected String eSignaturePersistable() {
		logger.debug(">eSignature start!");
		logger.debug("persistable = " + persistable.getIdentity());
		
		if(persistable == null) {
			String errorStr = getLocalizedMessage("Object_Is_Null");
			logger.error(errorStr);
			return errorStr;	
		}
		
		if(!(persistable instanceof ContentHolder)) {
			String errorStr = getLocalizedMessage("Object_Not_ContentHolder");
			logger.error(errorStr);
			return errorStr;
		}

		Vector<ApplicationData> sourcePDF = null;
		try {
			PdfSourceHandler pdfSource = new PdfSourceHandler((ContentHolder)persistable);
			sourcePDF = pdfSource.getSourcePDF();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			String errorStr = getLocalizedMessage("Get_Pdf_Error");		
			return errorStr;
		}
		if(sourcePDF!=null) {
			int size = sourcePDF.size();
			logger.debug("szie=" + size);
			if(size ==0) {
				String errorStr = getLocalizedMessage("Get_Pdf_Size_Zero");
				logger.error(errorStr);
				return errorStr;
			}
			for(int i=0; i<size; i++) {
				ApplicationData applicationData = (ApplicationData)sourcePDF.get(i);
				String fileName = applicationData.getFileName();
				
				//change by rchen ,解决PDF乱码问题
				if(size == 1){
					if(persistable instanceof WTDocument){
						WTDocument doc = (WTDocument)persistable;
						fileName = doc.getName()+".pdf";
					}
					if(persistable instanceof EPMDocument){
						EPMDocument epm = (EPMDocument)persistable;
						fileName = epm.getCADName();
						if(fileName.contains(".")){
							fileName = fileName.substring(0,fileName.lastIndexOf("."));
						}
						fileName = fileName+".pdf";
					}
				}
				
				//解决芝麻问题5979073，替换文件名中的"/","\\"
				fileName = fileName.replace("/", "_");
				fileName = fileName.replace("\\", "_");
				
				
				String newFilePath = TEMP_PATH + File.separator + EsignatureConstant.PRINT_FILE_PERFFIX + fileName;
				logger.debug("newFilePath = " + newFilePath);
				
				try {
					InputStream is = ContentServerHelper.service.findContentStream(applicationData);
					this.reader = new PdfReader(is);
				}
				catch(Exception e) {
					e.printStackTrace();
					logger.error(e);
					String errorStr = getLocalizedMessage("Read_Pdf_Error");
					return errorStr;
				}
				
				int pageNumber = reader.getNumberOfPages();
				
				if(pageNumber < 1) {
					logger.warn(fileName + "pageNumber is 0!");
					continue;
				}
				
				//add by kyang 记录pdf每页的图幅大小,以及有多少种图幅
				Map<Integer, String> pageTufuMap = new HashMap<Integer, String>();
				List<String> tufus = new ArrayList<String>();
				for(int t = 1; t <= pageNumber; t++){
					// Modify by Kwang : 解决读取PDF尺寸时，无法识别横纵图
//					Rectangle psize = reader.getPageSize(t);
					Rectangle psize = reader.getPageSizeWithRotation(t) ;
					if(psize != null) {
						float width = psize.getWidth();
						float height = psize.getHeight();
						PDFSizeHandler pdfSizeCalculate = new PDFSizeHandler(width, height);
						String tufu = pdfSizeCalculate.getPdfSizeValue();
						logger.debug("页数="+t+"的图幅="+tufu);
						
						pageTufuMap.put(t, tufu);
						if(!tufus.contains(tufu)){
							tufus.add(tufu);
						}
					}
				}
				
				logger.debug("各页图幅Map=" + pageTufuMap);
				logger.debug("图幅样式="+tufus);
				
				try {
					//获取Excel的配置信息
					ExcelCfgHandler excelInfo = new ExcelCfgHandler(persistable, reader);
					
					if(tufus!=null && tufus.size() > 1 && persistable instanceof EPMDocument){ //说明至少有2页不同，即可认为是不规则的图纸 // add by kyang
						keySignatureBeans = excelInfo.getInfo(tufus);
					}else{
						//keySignatureBean = excelInfo.getInfo();
						valueSignatureBean = excelInfo.getInfo();
					}
				}
				catch(Exception e) {
					e.printStackTrace();
					logger.error(e);
					String errorStr = getLocalizedMessage("Get_Excel_Cell_Value_Error");
					return errorStr;
				}
				//流程中title和WorkItem的MAP集合
				try {
					//获取流程信息
					logger.error("process <<<<   "+ process);
					ProcessInfoHandler processInfo = new ProcessInfoHandler(persistable, process);
					keyWorkItem = processInfo.processData();
				} catch(Exception e) {
					e.printStackTrace();
					logger.error(e);
					String errorStr = getLocalizedMessage("Get_WorkItem_Error");
					return errorStr;
				}
				if(valueSignatureBean == null && keySignatureBeans==null) {
					String errorStr = getLocalizedMessage("Value_CellValue_Is_Null");
					logger.error(errorStr);
					return errorStr;
				}
				if(keyWorkItem == null) {
					String errorStr = getLocalizedMessage("Get_WorkItem_Error");
					logger.error(errorStr);
					return errorStr;
				}
				try {
					File tempFile = new File(newFilePath);
					stamp = new PdfStamper(reader, new FileOutputStream(tempFile));
					esignaturePdf(tufus);
					stamp.close();
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e);
					String errorStr = getLocalizedMessage("Esignature_Failed");
					return errorStr;
				}
				Transaction trx = new Transaction();
				try {
					trx.start();
					updatePrintFile(fileName, newFilePath, (ContentHolder)persistable);
					trx.commit();
					trx = null;
				} catch(Exception e) {
					e.printStackTrace();
					logger.error(e);
					String errorStr = getLocalizedMessage("Update_Content_Failed");
					return errorStr;
				} finally {
					if(trx != null) {
						trx.rollback();
					}
				}
			}
		}
		logger.debug(">eSignature end!");	
		return "";
	}
	
	/**
	 * 签名PDF
	 * @throws WTException
	 * @throws IOException
	 * @throws DocumentException
	 * @throws RemoteException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	protected void esignaturePdf(List<String> pages) throws WTException,IOException,
	DocumentException,RemoteException,NoSuchMethodException,
	SecurityException,IllegalAccessException,IllegalArgumentException,
	InvocationTargetException 
	{
		int pageNumber = reader.getNumberOfPages();
		if(keySignatureBeans!=null && keySignatureBeans.size()>0){
			logger.debug("keySignatureBeans.size=" + keySignatureBeans.size());
			logger.debug("keyWorkItem.size=" + keyWorkItem.size());
			logger.debug(" >>>>>>>>>>>>>>>keySignatureBeans :" + keySignatureBeans);
			
			if(pages != null && pages.size() > 1){ //说明不规则的图纸
				for(int i = 1;i <= pageNumber;i++){
					// Modify by Kwang : 解决读取PDF尺寸时，无法识别横纵图。
//					Rectangle psize = reader.getPageSize(i);
					Rectangle psize = reader.getPageSizeWithRotation(i) ;
		        	if(psize!=null) {
						float width = psize.getWidth();
						float height = psize.getHeight();
						PDFSizeHandler pdfSizeCalculate = new PDFSizeHandler(width, height);
						String pdftufu = pdfSizeCalculate.getPdfSizeValue();
						logger.debug(">>>>>>>pdftufu :"+pdftufu);
						Iterator<Entry<String, SignatureBean>> iterator = keySignatureBeans.entrySet().iterator();
						while(iterator.hasNext()) {
							Entry<String, SignatureBean> entry = iterator.next();
							String key = entry.getKey();
							if(key.indexOf(",")>-1){
								String[] str = key.split(",");
								if(pdftufu.equals(str[0])){
									SignatureBean signatureBean = entry.getValue();
									logger.debug("key = " + key  +"  value :"+signatureBean.toString());
									if(signatureBean!=null){
										logger.debug( str[0]+" >>>>>>>>>start >>>>>>>>>>>str[1] ："+str[1]);
										logger.debug(">>>>LOCATIONX:"+signatureBean.getLOCATIONX());
										logger.debug(">>>>LOCATIONY:"+signatureBean.getLOCATIONY());
										logger.debug(">>>>PRINTTYPE:"+signatureBean.getPRINTTYPE());
										logger.debug(">>>>PAGE:"+signatureBean.getPAGE());
										logger.debug(">>>>LOCATIONX2:"+signatureBean.getLOCATIONX2());
										logger.debug(">>>>LOCATIONY2:"+signatureBean.getLOCATIONY2());
										logger.debug(">>>>TITLE:"+signatureBean.getTITLE());
										logger.debug( str[0]+" >>>>>>>>>end >>>>>>>>>>>str[1] ："+str[1]);
									}
									//处理配置打印类型为空时，默认为文本签名
									String printType = signatureBean.getPRINTTYPE();
									Sign(pageNumber, signatureBean, printType, str[1]);
								}
							}
						}
					}
				}
			}
		}else
		{
			Iterator<SignatureBean> iterator = valueSignatureBean.iterator();
			while(iterator.hasNext())
			{
				SignatureBean signatureBean = iterator.next();
				//处理配置打印类型为空时，默认为文本签名
				String printType = signatureBean.getPRINTTYPE();
				String key = signatureBean.getTITLE();
				Sign(pageNumber, signatureBean, printType, key);
			}
		}
	}

	/**
	 * 签名
	 * @param pageNumber
	 * @param signatureBean
	 * @param printType
	 * @param key
	 * @throws WTException
	 * @throws IOException
	 * @throws DocumentException
	 * @throws RemoteException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void Sign(int pageNumber, SignatureBean signatureBean,
			String printType, String key) throws WTException, IOException,
			DocumentException, RemoteException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		if(keyWorkItem.containsKey(key)) {
			WorkItem workItem = keyWorkItem.get(key);	
			//签名审核人
			String userId = workItem.getCompletedBy();
			logger.debug("userId = " + userId);
			if(printType.equals(EsignatureConstant.TEXT)) {
				WTUser user = OrgUtil.getUserById(userId);
				if(user!=null) {
					userId = user.getFullName();
				}
			}   
			logger.debug("key = " + key);
			logger.debug("userId = " + userId);
			
			EsignatureHandler esignature = new EsignatureHandler(reader,stamp, signatureBean, userId, pageNumber,null,null);
			esignature.process();

		} else if (key.startsWith(EsignatureConstant.IBA_PERFFIX) || key.startsWith(EsignatureConstant.MBA_PERFFIX)){
			String str = "" ;
			if(key.equalsIgnoreCase(EsignatureConstant.MBA_PERFFIX) ||
					key.equalsIgnoreCase(EsignatureConstant.IBA_PERFFIX)){
				// 获取所属库上的‘所属公司’属性值
				Object valueObject = PIAttributeHelper.service.getValue(((WTContained)persistable).getContainer(), "ssgs") ;
				
				str = valueObject == null ? "" : (String)valueObject ;
			}else{
				AttributeHandler attHandler = new AttributeHandler((IBAHolder) persistable, key);
				Object value = attHandler.getValue();
				logger.debug("value=" + value);
				//logger.debug("value=" + value.getClass());
				str = parseAttrToStr(value, printType);
				logger.debug("str = " + str);
			}
			if(str!=null && !str.isEmpty()) {
				EsignatureHandler dateEsignature = new EsignatureHandler(reader,stamp, signatureBean, str, pageNumber,null,null);
				dateEsignature.process();
			}	
		} else if(keyWorkItem.containsKey(key.split(EsignatureConstant.DATE_TITILE_SUFFIX)[0]) && key.endsWith(EsignatureConstant.DATE_TITILE_SUFFIX)){
			//签名日期
			WorkItem workItem = keyWorkItem.get(key.split(EsignatureConstant.DATE_TITILE_SUFFIX)[0]);
			Timestamp ts = workItem.getModifyTimestamp();
			String dateStr = parseTimestampToStr(ts);
			logger.debug("dateStr = " + dateStr);
			EsignatureHandler dateEsignature = new EsignatureHandler(reader,stamp, signatureBean, dateStr, pageNumber,null,null);
			dateEsignature.process();
		}
		else {
			//TODO 根据具体的项目进行添加
		}
	}

	/**
	 * 过滤需要签名的对象的集合，直接操作传入的对象
	 * @Author: gzhou
	 * @Date: 2015-3-17 上午09:53:34
	 * @Description:
	 * @return
	 * @throws WTException
	 * @throws RemoteException 
	 */
	protected void needEsignature() throws WTException, RemoteException {
		if(arrayList!=null) {
			WTArrayList needSignList = new WTArrayList();
			int size = arrayList.size();
			for(int i=0; i<size; i++) {
				Persistable persistable = arrayList.getPersistable(i);
				if(EsignatureUtil.needSign(persistable))
					needSignList.add(persistable);
				/*if(!EsignatureUtil.needSign(persistable)) {
					arrayList.remove(persistable);
				}*/
			}
			arrayList = needSignList;
		}
	}
	
	
	
	/**
	 * 转换属性中的属性
	 * @Author: gzhou
	 * @Date: 2015-3-17 上午11:02:02
	 * @Description:
	 * @param obj
	 * @return
	 * @throws WTException 
	 */
	private String parseAttrToStr(Object obj, String printType) throws WTException {
		String str  = "";
		if(obj!=null && printType!=null) {
			if(obj instanceof VersionIdentifier){
				str = ((VersionIdentifier) obj).getValue();
			} else if(obj instanceof IterationIdentifier){
				str = ((IterationIdentifier) obj).getValue();
			} else if(obj instanceof String) {
				str = (String)obj;
			} else if(obj instanceof Timestamp) {
				str = parseTimestampToStr((Timestamp)obj);
			} else if(obj instanceof WTPrincipalReference) {
				WTPrincipalReference principalRef = (WTPrincipalReference)obj;
				WTPrincipal principal = principalRef.getPrincipal();
				if(principal instanceof WTUser) {
					WTUser user = (WTUser)principal; 
					if(printType.equals(EsignatureConstant.TEXT)) {
						str = user.getFullName();
					} else if(printType.equals(EsignatureConstant.IMAGE)) {
						str = user.getName();
					}
				}
				
			} else if(obj instanceof WTContainer) {
				WTContainer container = (WTContainer)obj;
				str = container.getName();
			} else if(obj instanceof State) {
				State state = (State)obj;
				str = state.getDisplay(locale);
			}
		}
		return str;
	}
	
	/**
	 * @Author: gzhou
	 * @Date: 2015-3-17 上午11:00:19
	 * @Description:
	 * @param ts
	 * @return
	 */
	private String parseTimestampToStr(Timestamp ts) {
		DateFormat sdf = new SimpleDateFormat(EsignatureConstant.DATA_FORMAT);  
		String dateStr = sdf.format(ts);
		return dateStr;
	}

	/**
	 * 获取本地化的提示信息
	 * @Author: gzhou
	 * @Date: 2015-3-17 下午04:01:27
	 * @Description:
	 * @param s
	 * @return
	 */
	private static String getLocalizedMessage(String s) {
		String str = "";
		if(s!=null && !s.isEmpty()) {
			str = WTMessage.getLocalizedMessage(RESOURCE, s, null, locale);
		}
		return str;
	}
	
	/**
	 * @Author: gzhou
	 * @Date: 2015-3-17 下午01:38:51
	 * @Description:
	 * @param newFilePath
	 * @param persistable
	 * @throws WTException 
	 * @throws WTPropertyVetoException 
	 */
	private void updatePrintFile(String fileName, String newFilePath,  ContentHolder contentHolder) throws WTException, WTPropertyVetoException {
		
		logger.debug("fileName=" + fileName);
		logger.debug("newFilePath=" + newFilePath);
		String printFileName = EsignatureConstant.PRINT_FILE_PERFFIX + fileName;
		logger.debug("printFileName = "+ printFileName);
		//新增文件
		File file = new File(newFilePath);
		if(!file.exists()) {
			return;
		}
		if(file.length() == 0) {
			file.delete();
			return;
		}
			
		//删除附件中的PRINT_开头的文件
		QueryResult contents = CoreUtil.getSecondaryContents(contentHolder);
		if(contents!=null) {
			while(contents.hasMoreElements()) {
				Object content = contents.nextElement();
				if(content!=null && content instanceof ApplicationData) {
					ApplicationData applicaData = (ApplicationData)content;
					String tempFileName = applicaData.getFileName();
					if(tempFileName.equals(printFileName)) {
						ContentServerHelper.service.deleteContent(contentHolder, applicaData);
					}
				}
			}
		}
		
		//新增文件
		CoreUtil.addSecondaryContent(contentHolder, file);
		
	}
	
}