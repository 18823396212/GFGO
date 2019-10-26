package ext.appo.util;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.apache.log4j.Logger;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.content.FormatContentHolder;
import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.epm.EPMDocument;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.part.WTPart;
import wt.pom.Transaction;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionServerHelper;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.config.LatestConfigSpec;

import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.netmarkets.model.NmOid;

public class DocUtil {

	private static Logger log=Logger.getLogger(DocUtil.class.getName());
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	   public static WTDocument getLatestWTDocument(String docNumber) throws WTException
	   {
			if (docNumber == null || docNumber.length() == 0)
				return null;

			QuerySpec qs = new QuerySpec(WTDocument.class);
			SearchCondition scNumber = new SearchCondition(WTDocument.class,
					WTDocument.NUMBER, SearchCondition.EQUAL, docNumber
							.toUpperCase());
			SearchCondition scLatestIteration = new SearchCondition(
					WTDocument.class, WTAttributeNameIfc.LATEST_ITERATION,
					SearchCondition.IS_TRUE);
			qs.appendWhere(scNumber);
			qs.appendAnd();
			qs.appendWhere(scLatestIteration);

			QueryResult qr = PersistenceHelper.manager.find(qs);
			if (qr != null && qr.hasMoreElements())
				qr = (new LatestConfigSpec()).process(qr);

			if (qr != null && qr.hasMoreElements())
				return (WTDocument) qr.nextElement();

			return null;
		}
	   public static WTDocumentMaster getDocumentMaster(String documentNumber) throws WTException
	   {
	       WTDocumentMaster wtdocumentmaster = null;
	       QuerySpec criteria = new QuerySpec(WTDocumentMaster.class);
	       criteria.appendSearchCondition(new SearchCondition(WTDocumentMaster.class,
	                                                       WTDocumentMaster.NUMBER,
	                                                       SearchCondition.EQUAL,
	                                                       documentNumber,
	                                                       false));
	       QueryResult results = PersistenceHelper.manager.find(criteria);
	       if (results.hasMoreElements())
	           wtdocumentmaster = (WTDocumentMaster)results.nextElement();
	       return wtdocumentmaster;
	   }

		public static String getObjectType(Object object) throws WTException
		{
			String type = "";
			boolean flag = true;
			try
			{
				flag = SessionServerHelper.manager.isAccessEnforced();
				SessionServerHelper.manager.setAccessEnforced(false);

				if (object != null)
				{
					TypeIdentifier ti = TypeIdentifierUtility.getTypeIdentifier(object);
					type = ti.getTypename();
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			} finally
			{
				SessionServerHelper.manager.setAccessEnforced(flag);
			}
			return type;
		}

		public static EPMDocument getLastestEPMDocumentByNumber(String numStr) {
			try {
				QuerySpec queryspec = new QuerySpec(EPMDocument.class);

				queryspec.appendSearchCondition(new SearchCondition(EPMDocument.class,
						EPMDocument.NUMBER, SearchCondition.EQUAL, numStr));
				QueryResult queryresult = PersistenceHelper.manager.find(queryspec);
				LatestConfigSpec cfg = new LatestConfigSpec();
				QueryResult qr = cfg.process(queryresult);
				if (qr.hasMoreElements()) {
					return (EPMDocument) qr.nextElement();
				}
			} catch (WTException e) {
				e.printStackTrace();
			}
			return null;
		}
		public static WTObject addAttachment(WTObject object,String strAttachmentFilePath,String attachementName)
		{
				File asrc = new File(strAttachmentFilePath);
				if (asrc != null && asrc.exists()) {
					InputStream attSymbleInputStream=null;
					try {
						attSymbleInputStream = new FileInputStream(asrc);
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						log.debug("geti fileInputStream failed!");
						e1.printStackTrace();
					}
					if (object != null && attSymbleInputStream != null) {

						object = (WTObject) createSECONDARY((ContentHolder) object, attachementName, attSymbleInputStream);
					}
				}
			
				try {
					object = (WTObject) PersistenceHelper.manager.save(object);
					object = (WTObject) PersistenceServerHelper.manager.restore(object);
				} catch (WTException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		  return object;
		}		/**
		 * 获取wtDocument对象的内容
		 * 
		 * @param wtdocument
		 *            动态文档
		 * @throws IOException
		 */
		public static ApplicationData getPrimaryContent(WTDocument wtdocument)
				throws Exception, WTException {
			ContentHolder contentHolder = null;
			ApplicationData applicationdata = null;
			try {
				contentHolder = ContentHelper.service
						.getContents((ContentHolder) wtdocument);
				ContentItem contentitem = ContentHelper
						.getPrimary((FormatContentHolder) contentHolder);
				applicationdata = (ApplicationData) contentitem;
			} catch (WTException e1) {
				e1.printStackTrace();
				throw new WTException(e1.getLocalizedMessage());
			} catch (Exception e) {
				e.printStackTrace();
				throw new IOException(e.getLocalizedMessage());
			}
			return applicationdata;
		}
		public static Boolean ChangePrimaryContentName(ContentHolder object,String newname) throws WTException, Exception{
			ApplicationData appData=getPrimaryContent((WTDocument)object);
			System.out.println("appData name=="+appData.getFileName());
			InputStream is=null;
			if (appData != null) {
				is = ContentServerHelper.service
						.findContentStream(appData);
			}
			appData.setFileName(newname);
			appData.setUploadedFromPath(newname);
			appData.setDescription("系统自动修改的名字");
			 ContentServerHelper.service.updateContent(object,
					 appData, is);
			return true;
		}
		public static Boolean ChangeAttachmentName(ContentHolder object,String newname,String oldname) 
				throws FileNotFoundException, IOException{
			
			try {
				ContentHolder content = ContentHelper.service
						.getContents(object);
				
				Vector vApplicationData = ContentHelper.getApplicationData(content);
                System.out.println("Data size ==="+vApplicationData.size());
				for (int i = 0; i < vApplicationData.size(); i++) {
					ApplicationData applicationdata = (ApplicationData) vApplicationData.elementAt(i);
					InputStream is=null;
					if (applicationdata != null) {
						is = ContentServerHelper.service
								.findContentStream(applicationdata);
					}
					System.out.println("applicationdata.getFileName()===="+applicationdata.getFileName());
					if (applicationdata.getFileName().equals(oldname)) {
						applicationdata.setFileName(newname);
						applicationdata.setUploadedFromPath(newname);
						applicationdata.setDescription("系统自动修改的名字");
						applicationdata = ContentServerHelper.service.updateContent(object,
								applicationdata, is);
						
					}
				}
			} catch (WTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PropertyVetoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		public static ContentHolder createSECONDARY(ContentHolder object,
				String attachmentName, InputStream isAttachment) {
			if (object != null && attachmentName != null && isAttachment != null
					&& attachmentName.length() > 0) {

				try {
					Transaction transaction = new Transaction();
					try {

						ApplicationData appdata = ApplicationData
								.newApplicationData( object);
						appdata.setRole(ContentRoleType.SECONDARY);
						transaction.start();
						//delete first
						ContentHolder content = ContentHelper.service
								.getContents(object);
						Vector vApplicationData = ContentHelper.getApplicationData(content);
						for (int i = 0; i < vApplicationData.size(); i++) {
							ApplicationData applicationdata = (ApplicationData) vApplicationData
									.elementAt(i);
							if (applicationdata.getFileName().equals(attachmentName)) {
								log.debug("...清除附件："
										+ applicationdata.getFileName());
								ContentServerHelper.service.deleteContent(content,
										applicationdata);
								break;
							}
						}
						
						appdata.setFileName(attachmentName);
						appdata.setUploadedFromPath(attachmentName);
						appdata.setDescription("系统自动生成");
						appdata = ContentServerHelper.service.updateContent(object,
								appdata, isAttachment);
						transaction.commit();
						transaction = null;
					} catch (WTException wte) {
						log.debug("error1: " + wte.getMessage());
					} catch (WTPropertyVetoException wtpve) {
						log.debug("error2: " + wtpve.getMessage());
					} catch (PropertyVetoException pve) {
						log.debug("error3: " + pve.getMessage());
					} catch (IOException ioe) {
						log.debug("error4: " + ioe.getMessage());
					} finally {
						if (transaction != null) {
							transaction.rollback();
						}
					}
					isAttachment.close();
				} catch (IOException ioe) {
					// System.out.println(ioe.getMessage());
				}
				return object;
			} else {
				return null;
			}
		}
		public static void deleteAttachmentHtml(WTObject object) throws Exception
		{
			try
			{
				object = (WTObject) PersistenceHelper.manager.refresh(object);
			     ContentHolder contentHolder = ContentHelper.service.getContents((ContentHolder) object);
			      Vector vData = ContentHelper.getApplicationData(contentHolder);
			      log.debug("data size=="+vData.size());
			      if (vData != null && vData.size() > 0) {
			      	for (int i = 0; i < vData.size(); i++)
			      	{
				    	ApplicationData appData = (ApplicationData) vData.get(i);
				    	log.debug("file name==="+appData.getFileName());
					    if(appData.getFileName().endsWith(".html"))
					    {
							ContentServerHelper.service.deleteContent((ContentHolder) object, appData);
							
					    }
					  
				}
			  }
			} catch (WTException e)
			{
				e.printStackTrace();
				throw e;
			} catch (Exception e)
			{
				e.printStackTrace();
				throw new WTException(e);
			} finally
			{
			}

		}
		
}
