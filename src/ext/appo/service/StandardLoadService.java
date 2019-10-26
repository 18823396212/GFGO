package ext.appo.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.apache.log4j.Logger;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.ContentServerHelper;
import wt.content.FormatContentHolder;
import wt.doc.WTDocument;
import wt.log4j.LogR;
import wt.services.StandardManager;
import wt.util.WTException;
import wt.util.WTProperties;

public class StandardLoadService extends StandardManager implements LoadService, Serializable
{

	private static final String CLASSNAME = StandardLoadService.class.getName();
	private static final boolean VERBOSE;
	private static final Logger LOG;

	static
	{
		try
		{
			LOG = LogR.getLogger(StandardLoadService.class.getName());
			WTProperties wtproperties = WTProperties.getLocalProperties();
			VERBOSE = wtproperties.getProperty("ext.generic.service.verbose", false);
		} catch (Throwable throwable)
		{
			System.err.println((new StringBuilder()).append("Error initializing ").append(CLASSNAME).toString());
			throwable.printStackTrace(System.err);
			throw new ExceptionInInitializerError(throwable);
		}
	}


	public  ApplicationData getPrimaryContent(WTDocument wtdocument)
			throws  WTException, IOException {
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
	public  Boolean ChangePrimaryContentName(ContentHolder object,String newname) throws WTException, Exception{
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


	
}
