package ext.generic.integration.cis.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.ptc.windchill.enterprise.part.commands.PartDocServiceCommand;

import ext.com.core.CoreUtil;
import ext.generic.integration.cis.constant.CISConstant;
import ext.generic.integration.cis.rule.CISBusinessRuleXML;
import wt.content.ApplicationData;
import wt.content.ContentItem;
import wt.content.Streamed;
import wt.doc.WTDocument;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.util.WTException;

/**
 * 该方法用来处理下载文件至共享目录
 * 
 * @author Administrator
 * 
 */
public class CISFileUtil {
	private static Logger logger = LogR.getLogger(CISFileUtil.class.getName());

	/**
	 * 根据对象去获取关联的参考文档，并下载至指定目录
	 * 
	 * @param obj
	 * @throws WTException
	 * @throws IOException
	 */
	public static void downFiles(WTObject obj) throws WTException, IOException {
		String type = CISBusinessRuleXML.getInstance().getFileType();
		logger.debug(" >>>>> filetype :" + type);
		if (obj instanceof WTPart) {
			WTPart part = (WTPart) obj;
			// 通过部件对象获取关联的参考文档
			QueryResult qrdoc = PartDocServiceCommand.getAssociatedReferenceDocuments(part);
			String downPath = "";

			while (qrdoc.hasMoreElements()) {
				WTDocument doc = (WTDocument) qrdoc.nextElement();

				downFiles(type, doc);
			}
		} else if (obj instanceof WTDocument) {
			WTDocument doc = (WTDocument) obj;
			downFiles(type, doc);
		}
	}

	public static void downFiles(String type, WTDocument doc) throws WTException, FileNotFoundException, IOException {
		if (type.equals(CISConstant.ALL)) {
			ContentItem ci = CoreUtil.getPrimaryContent(doc);
			if (ci instanceof ApplicationData) {
				ApplicationData data = (ApplicationData) ci;
				String name = data.getFileName();
				logger.debug(" >>>>>>>主内容文件名称：" + name);
				// 下载主内容
				getDocTypeAndDownType(doc, data, name);
			}
			// 获取附件
			QueryResult qr = CoreUtil.getSecondaryContents(doc);
			while (qr != null && qr.hasMoreElements()) {
				ContentItem contentItem = (ContentItem) qr.nextElement();
				if (contentItem instanceof ApplicationData) {
					ApplicationData data = (ApplicationData) contentItem;
					// 输出信息
					String dataFileName = data.getFileName();
					// 下载附件
					getDocTypeAndDownType(doc, data, dataFileName);

				}
			}
		} else if (type.equals(CISConstant.PRIMARY)) {
			ContentItem ci = CoreUtil.getPrimaryContent(doc);
			if (ci instanceof ApplicationData) {
				ApplicationData data = (ApplicationData) ci;
				String name = data.getFileName();
				logger.debug(" >>>>>>>主内容文件名称：" + name);
				// 下载主内容
				getDocTypeAndDownType(doc, data, name);
			}
		} else if (type.equals(CISConstant.SECONDARY)) {
			// 获取附件
			QueryResult qr = CoreUtil.getSecondaryContents(doc);
			while (qr != null && qr.hasMoreElements()) {
				ContentItem contentItem = (ContentItem) qr.nextElement();
				if (contentItem instanceof ApplicationData) {
					ApplicationData data = (ApplicationData) contentItem;
					// 输出信息
					String dataFileName = data.getFileName();
					// 下载附件
					getDocTypeAndDownType(doc, data, dataFileName);

				}
			}
		}
	}

	/**
	 * 根据类型下载到指定路径
	 * 
	 * @param doc
	 * @param data
	 * @param name
	 * @Param result
	 * @throws WTException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void getDocTypeAndDownType(WTDocument doc, ApplicationData data, String name) throws WTException,
			FileNotFoundException, IOException {
		String downPath = "";
		String typeName = CISCommonUtil.getSoftTypeName(doc);
		logger.debug(" >>>>>>>>>>softtypename :" + typeName);

		downPath = CISBusinessRuleXML.getInstance().getFilePath();

		if (typeName.equals(CISBusinessRuleXML.getInstance().getSymbol())) {

			downPath = downPath + CISBusinessRuleXML.getInstance().getSymbolpath();
			downContexts(downPath, data, name);
			/*if (result) {
				// 判断主内容的的后缀名
				String str = name.substring(name.lastIndexOf("."), name.length());
				if (str.equalsIgnoreCase(CISConstant.SCHLIB))
					downContexts(downPath, data, name);
			} else if (!result) {
				downContexts(downPath, data, name);
			}*/
		}
		if (typeName.equals(CISBusinessRuleXML.getInstance().getFootprint())) {
			downPath = downPath + CISBusinessRuleXML.getInstance().getFootprintpath();
			downContexts(downPath, data, name);
			/*if (result) {
				// 判断主内容的的后缀名
				String str = name.substring(name.lastIndexOf("."), name.length());
				if (str.equalsIgnoreCase(CISConstant.PCBLIB))
					downContexts(downPath, data, name);
			} else if (!result) {
				downContexts(downPath, data, name);
			}*/
		}
		if (typeName.equals(CISBusinessRuleXML.getInstance().getDatasheet())) {
			downPath = downPath + CISBusinessRuleXML.getInstance().getDatasheetpath();
			downContexts(downPath, data, name);
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
	public static void downContexts(String downPath, ApplicationData data, String name) throws WTException, FileNotFoundException,
			IOException {
		File f = new File(downPath);
		if (!f.exists())
			f.mkdir();

		Streamed sd = data == null ? null : (Streamed) data.getStreamData().getObject();
		BufferedInputStream bis = new BufferedInputStream(sd.retrieveStream());

		File outFile = new File(downPath, name);

		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile));
		byte buff1[] = new byte[16];
		int read;
		while ((read = bis.read(buff1)) > 0) {
			bos.write(buff1, 0, read);
		}
		bis.close();
		bos.close();
	}

}
