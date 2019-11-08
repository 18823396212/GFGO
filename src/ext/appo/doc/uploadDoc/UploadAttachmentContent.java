package ext.appo.doc.uploadDoc;

import com.lowagie.text.DocumentException;
import com.ptc.core.lwc.common.view.EnumerationDefinitionReadView;
import com.ptc.core.lwc.common.view.EnumerationEntryReadView;
import com.ptc.core.lwc.common.view.PropertyHolderHelper;
import com.ptc.core.lwc.server.TypeDefinitionServiceHelper;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import ext.appo.ecn.pdf.PdfUtil;
import ext.com.iba.IBAUtil;
import ext.com.workflow.WorkflowUtil;
import ext.generic.reviewObject.model.ProcessReviewObjectLink;
import ext.generic.reviewObject.model.ProcessReviewObjectLinkHelper;
import ext.generic.workflow.WorkFlowBase;
import wt.access.NotAuthorizedException;
import wt.content.*;
import wt.doc.WTDocument;
import wt.enterprise.Master;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerException;
import wt.inf.library.WTLibrary;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.query.QueryException;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.vc.OneOffVersioned;
import wt.vc.VersionControlHelper;
import wt.workflow.engine.WfProcess;

import java.beans.PropertyVetoException;
import java.io.*;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;

public class UploadAttachmentContent extends WorkFlowBase {

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

    public UploadAttachmentContent() {
    }


    public UploadAttachmentContent(WTObject pbo, ObjectReference self) {
        if (pbo != null && self != null) {
            this.pbo = pbo;
            this.self = self;
        }
    }


    /**
     * 发送数据入口
     *
     * @throws WTException
     * @throws ParseException
     * @throws DocumentException
     */
    public void uploadContentData() throws WTException, DocumentException, ParseException {

        boolean flag = SessionServerHelper.manager.setAccessEnforced(false); // 忽略权限

        System.out.println("=======================action=======================");
        try {
            Map<String, SendDocDataBean> map = ReadSendDocData.getSendDocDataBeans();
            int j = 0;
            System.out.println("map==============" + map);
            Set<WTDocument> signUpDocSet = new HashSet<>();

            //Set<WTPart> setPart = new HashSet<WTPart>();
            Map<WTPart, String> mapPart = new HashMap<>();
//            Map<WTPart, ApplicationData> mapInsertData = new HashMap<WTPart, ApplicationData>();
            Set<FileAttachListBean> listBean = new HashSet<>();

            Map<WTPart, String> mapYLPart = new HashMap<>();
            Set<FileAttachListYLBean> listYLBean = new HashSet<>();

            WTArrayList collect = collect();
            System.out.println("collect==================" + collect);
            for (int i = 0; i < collect.size(); i++) {
                Persistable persistable = collect.getPersistable(i);
                if (persistable instanceof WTPart) {
                    WTPart part = (WTPart) persistable;
                    //说明文档
                    QueryResult qrs = UploadAttachmentUtil.getAssociatedDescribeDocuments(part);
                    while (qrs.hasMoreElements()) {
                        WTDocument doc = (WTDocument) qrs.nextElement();
                        signUpDocSet.add(doc);
                        System.out.println("doc=============" + doc);
                        String type = UploadAttachmentUtil.getTypeInternalName(doc);
                        String passageInventory = (String) PdfUtil.getIBAObjectValue(doc, "SmallDocType");
                        System.out.println("type=======" + type);
                        System.out.println("passageInventory=============" + passageInventory);
                        if (map.keySet().toString().contains(type)) {
                            if (type.equals("com.plm.drawingdoc")) {             //------------------1.图纸不主内容不发PDF格式--2.附件有PDF必须PRINT开头

                                WTContainer container = doc.getContainer();
                                if (container instanceof WTLibrary) {
                                    WTLibrary library = (WTLibrary) container;
                                    String libName = library.getName();
                                    if (libName.contains("原材料库")) {
                                        //==========================================主内容action====================
                                        ContentItem ci = ContentHelper.service.getPrimary(doc);
                                        if (ci != null) {
                                            ApplicationData data = (ApplicationData) ci;                            //主内容
                                            System.out.println("ApplicationData========+" + data);
                                            //String downPath = "smb://rdpplm:rdpplm123@LAPTOP-UL273M6G/excel/";

                                            //============光峰
                                            Map<String, String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
                                            String downPath = mapPath.get("mappingPath");
                                            String name = data.getFileName();
                                            String str = name.substring(name.lastIndexOf("."), name.length());
                                            if (!str.equalsIgnoreCase(".PDF") && !str.equalsIgnoreCase(".pptx") && !str.equalsIgnoreCase(".ppt") && !str.equalsIgnoreCase(".xls") && !str.equalsIgnoreCase(".xlsx") && !str.equalsIgnoreCase(".doc") && !str.equalsIgnoreCase(".docx")) {
                                                UUID u = UUID.randomUUID();
                                                String[] arryName = name.split("\\.");
                                                System.out.println("name===============" + name);
                                                String conversionName = u.toString() + str;
                                                System.out.println("conversionName==========" + conversionName);
                                                downContexts(downPath, data, conversionName);
                                                j++;
                                                mapPart.put(part, String.valueOf(j));
                                                FileAttachListBean fileBean = new FileAttachListBean();
                                                fileBean.setFileID("");
                                                fileBean.setAttachName(name);
                                                fileBean.setGuidName(u.toString() + str);
                                                fileBean.setExtensionName(str);
                                                fileBean.setUerNo(part.getNumber());
                                                fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                                listBean.add(fileBean);
                                            }
//
                                            //===========绎立
                                            Map<String, String> mapPathYL = DataConfigurationPacketTable.getYLSendDocDataBeans();
                                            String downPathYL = mapPathYL.get("mappingPath");
                                            String nameYL = data.getFileName();
                                            String strYL = nameYL.substring(nameYL.lastIndexOf("."), nameYL.length());
                                            if (!strYL.equalsIgnoreCase(".PDF") && !strYL.equalsIgnoreCase(".pptx") && !strYL.equalsIgnoreCase(".ppt") && !strYL.equalsIgnoreCase(".xls") && !strYL.equalsIgnoreCase(".xlsx") && !strYL.equalsIgnoreCase(".doc") && !strYL.equalsIgnoreCase(".docx")) {
                                                UUID u = UUID.randomUUID();
                                                String[] arryName = nameYL.split("\\.");
                                                System.out.println("name===============" + nameYL);
                                                String conversionName = u.toString() + str;
                                                System.out.println("conversionName==========" + conversionName);
                                                downContexts(downPathYL, data, conversionName);
                                                mapYLPart.put(part, "1");
                                                FileAttachListYLBean fileBean = new FileAttachListYLBean();
                                                fileBean.setFileID("");
                                                fileBean.setAttachName(nameYL);
                                                fileBean.setGuidName(u.toString() + str);
                                                fileBean.setExtensionName(str);
                                                fileBean.setUerNo(part.getNumber());
                                                fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                                listYLBean.add(fileBean);
                                            }
                                        }

                                        //======================================附件action============================================
                                        QueryResult pdf = ContentHelper.service.getContentsByRole(doc, ContentRoleType.SECONDARY);
                                        while (pdf.hasMoreElements()) {
                                            ApplicationData data = (ApplicationData) pdf.nextElement();
                                            System.out.println("AttApplicationData========+" + data);
//												String downPath = "smb://rdpplm:rdpplm123@LAPTOP-UL273M6G/excel/";
                                            Map<String, String> mapPathYL = DataConfigurationPacketTable.getYLSendDocDataBeans();
                                            String downPathYL = mapPathYL.get("mappingPath");
                                            //光峰
                                            Map<String, String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
                                            String downPath = mapPath.get("mappingPath");
                                            String name = data.getFileName();
                                            String str = name.substring(name.lastIndexOf("."), name.length());
                                            if (str.equalsIgnoreCase(".PDF") || str.equalsIgnoreCase(".pptx") || str.equalsIgnoreCase(".ppt") || str.equalsIgnoreCase(".xls") || str.equalsIgnoreCase(".xlsx") || str.equalsIgnoreCase(".doc") || str.equalsIgnoreCase(".docx")) {
                                                boolean isprint = name.startsWith("PRINT");
                                                if (isprint) {

                                                    //光峰
                                                    UUID u = UUID.randomUUID();
                                                    String[] arryName = name.split("\\.");
                                                    System.out.println("AttName===============" + name);
                                                    String conversionName = u.toString() + str;
                                                    System.out.println("conversionName==========" + conversionName);
                                                    downContexts(downPath, data, conversionName);
                                                    mapPart.put(part, String.valueOf(j));
                                                    FileAttachListBean fileBean = new FileAttachListBean();
                                                    fileBean.setFileID("");
                                                    fileBean.setAttachName(name);
                                                    fileBean.setGuidName(u.toString() + str);
                                                    fileBean.setExtensionName(str);
                                                    fileBean.setUerNo(part.getNumber());
                                                    fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                                    listBean.add(fileBean);

                                                    //YL
                                                    UUID uYL = UUID.randomUUID();
                                                    String[] arryNameYL = name.split("\\.");
                                                    System.out.println("AttName===============" + name);
                                                    String conversionNameYL = uYL.toString() + "." + arryNameYL[1];
                                                    System.out.println("conversionName==========" + conversionNameYL);
                                                    downContexts(downPathYL, data, conversionNameYL);
                                                    j++;
                                                    mapYLPart.put(part, "1");
                                                    FileAttachListYLBean fileBeanYL = new FileAttachListYLBean();
                                                    fileBeanYL.setFileID("");
                                                    fileBeanYL.setAttachName(name);
                                                    fileBeanYL.setGuidName(uYL.toString() + "." + arryNameYL[1]);
                                                    fileBeanYL.setExtensionName("." + arryNameYL[1]);
                                                    fileBeanYL.setUerNo(part.getNumber());
                                                    fileBeanYL.setFileSize(String.valueOf(data.getFileSize()));
                                                    listYLBean.add(fileBeanYL);
                                                }


                                            } else {

                                                //光峰
                                                UUID u = UUID.randomUUID();
                                                String[] arryName = name.split("\\.");
                                                System.out.println("AttName===============" + name);
                                                String conversionName = u.toString() + str;
                                                System.out.println("conversionName==========" + conversionName);
                                                downContexts(downPath, data, conversionName);
                                                j++;
                                                mapPart.put(part, String.valueOf(j));
                                                FileAttachListBean fileBean = new FileAttachListBean();
                                                fileBean.setFileID("");
                                                fileBean.setAttachName(name);
                                                fileBean.setGuidName(u.toString() + str);
                                                fileBean.setExtensionName(str);
                                                fileBean.setUerNo(part.getNumber());
                                                fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                                listBean.add(fileBean);

                                                //绎立
                                                UUID uYL = UUID.randomUUID();
                                                String[] arryNameYL = name.split("\\.");
                                                System.out.println("AttName===============" + name);
                                                String conversionNameYL = uYL.toString() + "." + arryNameYL[1];
                                                System.out.println("conversionName==========" + conversionNameYL);
                                                downContexts(downPathYL, data, conversionNameYL);
                                                j++;
                                                mapYLPart.put(part, "1");
                                                FileAttachListYLBean fileBeanYL = new FileAttachListYLBean();
                                                fileBeanYL.setFileID("");
                                                fileBeanYL.setAttachName(name);
                                                fileBeanYL.setGuidName(uYL.toString() + "." + arryNameYL[1]);
                                                fileBeanYL.setExtensionName("." + arryNameYL[1]);
                                                fileBeanYL.setUerNo(part.getNumber());
                                                fileBeanYL.setFileSize(String.valueOf(data.getFileSize()));
                                                listYLBean.add(fileBeanYL);
                                            }
                                        }
                                    }
                                }
                                //=======================================附件end===============================================


                                //=======================================主内容end==========================
                            } else if (type.equals("com.plm.softwaredoc")) {                                    //软件只发送zip,rar格式
                                //==========================================主内容action====================

                                if (!doc.getContainerName().equals("SL-舞台灯产品库")) {
                                    ContentItem ci = ContentHelper.service.getPrimary(doc);
                                    if (ci != null) {
                                        ApplicationData data = (ApplicationData) ci;                            //主内容
                                        System.out.println("ApplicationData========+" + data);
                                        //String downPath = "smb://rdpplm:rdpplm123@LAPTOP-UL273M6G/excel/";
                                        Map<String, String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
                                        String downPath = mapPath.get("mappingPath");
                                        String name = data.getFileName();
                                        String str = name.substring(name.lastIndexOf("."), name.length());
                                        if (str.equalsIgnoreCase(".zip") || str.equalsIgnoreCase(".rar")) {
                                            UUID u = UUID.randomUUID();
                                            String[] arryName = name.split("\\.");
                                            System.out.println("name===============" + name);
                                            String conversionName = u.toString() + str;
                                            System.out.println("conversionName==========" + conversionName);
                                            downContexts(downPath, data, conversionName);
                                            //得到符合要求的数据
                                            j++;
                                            mapPart.put(part, String.valueOf(j));
                                            FileAttachListBean fileBean = new FileAttachListBean();
                                            fileBean.setFileID("");
                                            fileBean.setAttachName(name);
                                            fileBean.setGuidName(u.toString() + str);
                                            fileBean.setExtensionName(str);
                                            fileBean.setUerNo(part.getNumber());
                                            fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                            listBean.add(fileBean);
                                        }
                                    }
                                    //=======================================主内容end==========================

                                    //======================================附件action============================================
                                    QueryResult pdf = ContentHelper.service.getContentsByRole(doc, ContentRoleType.SECONDARY);
                                    while (pdf.hasMoreElements()) {
                                        ApplicationData data = (ApplicationData) pdf.nextElement();
                                        System.out.println("AttApplicationData========+" + data);
//												String downPath = "smb://rdpplm:rdpplm123@LAPTOP-UL273M6G/excel/";
                                        Map<String, String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
                                        String downPath = mapPath.get("mappingPath");
                                        String name = data.getFileName();
                                        String str = name.substring(name.lastIndexOf("."), name.length());
                                        if (str.equalsIgnoreCase(".zip") || str.equalsIgnoreCase(".rar")) {
                                            UUID u = UUID.randomUUID();
                                            String[] arryName = name.split("\\.");
                                            System.out.println("AttName===============" + name);
                                            String conversionName = u.toString() + str;
                                            System.out.println("conversionName==========" + conversionName);
                                            downContexts(downPath, data, conversionName);
                                            //UUID u = UUID.randomUUID();
                                            //得到符合要求的数据
                                            //		                                    setPart.add(part);
                                            mapPart.put(part, String.valueOf(j));
                                            FileAttachListBean fileBean = new FileAttachListBean();
                                            fileBean.setFileID("");
                                            fileBean.setAttachName(name);
                                            fileBean.setGuidName(u.toString() + str);
                                            fileBean.setExtensionName(str);
                                            fileBean.setUerNo(part.getNumber());
                                            fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                            listBean.add(fileBean);
                                        }
                                    }

                                } else {

                                    ContentItem ci = ContentHelper.service.getPrimary(doc);
                                    if (ci != null) {
                                        ApplicationData data = (ApplicationData) ci;                            //主内容
                                        System.out.println("ApplicationData========+" + data);
                                        //String downPath = "smb://rdpplm:rdpplm123@LAPTOP-UL273M6G/excel/";
                                        Map<String, String> mapPath = DataConfigurationPacketTable.getYLSendDocDataBeans();
                                        String downPath = mapPath.get("mappingPath");
                                        String name = data.getFileName();
                                        String str = name.substring(name.lastIndexOf("."), name.length());
                                        if (str.equalsIgnoreCase(".zip") || str.equalsIgnoreCase(".rar")) {
                                            UUID u = UUID.randomUUID();
                                            String[] arryName = name.split("\\.");
                                            System.out.println("name===============" + name);
                                            String conversionName = u.toString() + str;
                                            System.out.println("conversionName==========" + conversionName);
                                            downContexts(downPath, data, conversionName);
                                            //得到符合要求的数据
                                            j++;
                                            mapYLPart.put(part, "2");
                                            FileAttachListYLBean fileBean = new FileAttachListYLBean();
                                            fileBean.setFileID("");
                                            fileBean.setAttachName(name);
                                            fileBean.setGuidName(u.toString() + str);
                                            fileBean.setExtensionName(str);
                                            fileBean.setUerNo(part.getNumber());
                                            fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                            listYLBean.add(fileBean);
                                        }
                                    }
                                    //=======================================主内容end==========================

                                    //======================================附件action============================================
                                    QueryResult pdf = ContentHelper.service.getContentsByRole(doc, ContentRoleType.SECONDARY);
                                    while (pdf.hasMoreElements()) {
                                        ApplicationData data = (ApplicationData) pdf.nextElement();
                                        System.out.println("AttApplicationData========+" + data);
//													String downPath = "smb://rdpplm:rdpplm123@LAPTOP-UL273M6G/excel/";
                                        Map<String, String> mapPath = DataConfigurationPacketTable.getYLSendDocDataBeans();
                                        String downPath = mapPath.get("mappingPath");
                                        String name = data.getFileName();
                                        String str = name.substring(name.lastIndexOf("."), name.length());
                                        if (str.equalsIgnoreCase(".zip") || str.equalsIgnoreCase(".rar")) {
                                            UUID u = UUID.randomUUID();
                                            String[] arryName = name.split("\\.");
                                            System.out.println("AttName===============" + name);
                                            String conversionName = u.toString() + str;
                                            System.out.println("conversionName==========" + conversionName);
                                            downContexts(downPath, data, conversionName);
                                            //UUID u = UUID.randomUUID();
                                            //得到符合要求的数据
                                            j++;
                                            //		                                    setPart.add(part);
                                            mapYLPart.put(part, "2");
                                            FileAttachListYLBean fileBean = new FileAttachListYLBean();
                                            fileBean.setFileID("");
                                            fileBean.setAttachName(name);
                                            fileBean.setGuidName(u.toString() + str);
                                            fileBean.setExtensionName(str);
                                            fileBean.setUerNo(part.getNumber());
                                            fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                            listYLBean.add(fileBean);
                                        }
                                    }
                                }
                                //=======================================附件end===============================================
                            } else if (type.equals("com.plm.datasheet")) {
                                //==========================================主内容action====================
                                ContentItem ci = ContentHelper.service.getPrimary(doc);
                                if (ci != null) {
                                    ApplicationData data = (ApplicationData) ci;                            //主内容
                                    System.out.println("ApplicationData========+" + data);
                                    //String downPath = "smb://rdpplm:rdpplm123@LAPTOP-UL273M6G/excel/";

                                    //光峰
                                    Map<String, String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
                                    String downPath = mapPath.get("mappingPath");
                                    String name = data.getFileName();
                                    String str = name.substring(name.lastIndexOf("."), name.length());
                                    if (!str.equalsIgnoreCase(".PDF") && !str.equalsIgnoreCase(".pptx") && !str.equalsIgnoreCase(".ppt") && !str.equalsIgnoreCase(".xls") && !str.equalsIgnoreCase(".xlsx") && !str.equalsIgnoreCase(".doc") && !str.equalsIgnoreCase(".docx")) {
                                        UUID u = UUID.randomUUID();
                                        String[] arryName = name.split("\\.");
                                        System.out.println("name===============" + name);
                                        String conversionName = u.toString() + str;
                                        System.out.println("conversionName==========" + conversionName);
                                        downContexts(downPath, data, conversionName);
                                        //得到符合要求的数据
                                        j++;
                                        mapPart.put(part, String.valueOf(j));
                                        FileAttachListBean fileBean = new FileAttachListBean();
                                        fileBean.setFileID("");
                                        fileBean.setAttachName(name);
                                        fileBean.setGuidName(u.toString() + str);
                                        fileBean.setExtensionName(str);
                                        fileBean.setUerNo(part.getNumber());
                                        fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                        listBean.add(fileBean);
                                    }

                                    //YL
                                    Map<String, String> mapPathYL = DataConfigurationPacketTable.getYLSendDocDataBeans();
                                    String downPathYL = mapPathYL.get("mappingPath");
                                    String nameYL = data.getFileName();
                                    String strYL = nameYL.substring(nameYL.lastIndexOf("."), nameYL.length());
                                    if (!strYL.equalsIgnoreCase(".PDF") && !strYL.equalsIgnoreCase(".pptx") && !strYL.equalsIgnoreCase(".ppt") && !strYL.equalsIgnoreCase(".xls") && !strYL.equalsIgnoreCase(".xlsx") && !strYL.equalsIgnoreCase(".doc") && !strYL.equalsIgnoreCase(".docx")) {
                                        UUID u = UUID.randomUUID();
                                        String[] arryName = nameYL.split("\\.");
                                        System.out.println("name===============" + nameYL);
                                        String conversionName = u.toString() + str;
                                        System.out.println("conversionName==========" + conversionName);
                                        downContexts(downPathYL, data, conversionName);
                                        //得到符合要求的数据
                                        mapYLPart.put(part, "3");
                                        FileAttachListYLBean fileBean = new FileAttachListYLBean();
                                        fileBean.setFileID("");
                                        fileBean.setAttachName(nameYL);
                                        fileBean.setGuidName(u.toString() + str);
                                        fileBean.setExtensionName(str);
                                        fileBean.setUerNo(part.getNumber());
                                        fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                        listYLBean.add(fileBean);
                                    }

                                }
                                //=======================================主内容end==========================

                                //======================================附件action============================================
                                QueryResult pdf = ContentHelper.service.getContentsByRole(doc, ContentRoleType.SECONDARY);
                                while (pdf.hasMoreElements()) {
                                    ApplicationData data = (ApplicationData) pdf.nextElement();
                                    System.out.println("AttApplicationData========+" + data);
                                    Map<String, String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
                                    String downPath = mapPath.get("mappingPath");
                                    String name = data.getFileName();
                                    Map<String, String> mapPathYL = DataConfigurationPacketTable.getYLSendDocDataBeans();
                                    String downPathYL = mapPathYL.get("mappingPath");

                                    String str = name.substring(name.lastIndexOf("."), name.length());
                                    if (str.equalsIgnoreCase(".PDF") || str.equalsIgnoreCase(".pptx") || str.equalsIgnoreCase(".ppt") || str.equalsIgnoreCase(".xls") || str.equalsIgnoreCase(".xlsx") || str.equalsIgnoreCase(".doc") || str.equalsIgnoreCase(".docx")) {
                                        boolean isprint = name.startsWith("PRINT");
                                        if (isprint) {
                                            UUID u = UUID.randomUUID();
                                            String[] arryName = name.split("\\.");
                                            System.out.println("AttName===============" + name);
                                            String conversionName = u.toString() + str;
                                            System.out.println("conversionName==========" + conversionName);
                                            downContexts(downPath, data, conversionName);
                                            //UUID u = UUID.randomUUID();
                                            //得到符合要求的数据
                                            j++;
                                            mapPart.put(part, String.valueOf(j));
                                            FileAttachListBean fileBean = new FileAttachListBean();
                                            fileBean.setFileID("");
                                            fileBean.setAttachName(name);
                                            fileBean.setGuidName(u.toString() + str);
                                            fileBean.setExtensionName(str);
                                            fileBean.setUerNo(part.getNumber());
                                            fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                            listBean.add(fileBean);

                                            //绎立
                                            UUID uYL = UUID.randomUUID();
                                            String[] arryNameYL = name.split("\\.");
                                            System.out.println("AttName===============" + name);
                                            String conversionNameYL = uYL.toString() + "." + arryNameYL[1];
                                            System.out.println("conversionName==========" + conversionNameYL);
                                            downContexts(downPathYL, data, conversionNameYL);
                                            j++;
                                            mapYLPart.put(part, "3");
                                            FileAttachListYLBean fileBeanYL = new FileAttachListYLBean();
                                            fileBeanYL.setFileID("");
                                            fileBeanYL.setAttachName(name);
                                            fileBeanYL.setGuidName(uYL.toString() + "." + arryNameYL[1]);
                                            fileBeanYL.setExtensionName("." + arryNameYL[1]);
                                            fileBeanYL.setUerNo(part.getNumber());
                                            fileBeanYL.setFileSize(String.valueOf(data.getFileSize()));
                                            listYLBean.add(fileBeanYL);
                                        }

                                    } else {
                                        UUID u = UUID.randomUUID();
                                        String[] arryName = name.split("\\.");
                                        System.out.println("AttName===============" + name);
                                        String conversionName = u.toString() + str;
                                        System.out.println("conversionName==========" + conversionName);
                                        downContexts(downPath, data, conversionName);
                                        //UUID u = UUID.randomUUID();
                                        //得到符合要求的数据
                                        j++;
                                        mapPart.put(part, String.valueOf(j));
                                        FileAttachListBean fileBean = new FileAttachListBean();
                                        fileBean.setFileID("");
                                        fileBean.setAttachName(name);
                                        fileBean.setGuidName(u.toString() + str);
                                        fileBean.setExtensionName(str);
                                        fileBean.setUerNo(part.getNumber());
                                        fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                        listBean.add(fileBean);

                                        //绎立
                                        UUID uYL = UUID.randomUUID();
                                        String[] arryNameYL = name.split("\\.");
                                        System.out.println("AttName===============" + name);
                                        String conversionNameYL = uYL.toString() + "." + arryNameYL[1];
                                        System.out.println("conversionName==========" + conversionNameYL);
                                        downContexts(downPathYL, data, conversionNameYL);
                                        j++;
                                        mapYLPart.put(part, "3");
                                        FileAttachListYLBean fileBeanYL = new FileAttachListYLBean();
                                        fileBeanYL.setFileID("");
                                        fileBeanYL.setAttachName(name);
                                        fileBeanYL.setGuidName(uYL.toString() + "." + arryNameYL[1]);
                                        fileBeanYL.setExtensionName("." + arryNameYL[1]);
                                        fileBeanYL.setUerNo(part.getNumber());
                                        fileBeanYL.setFileSize(String.valueOf(data.getFileSize()));
                                        listYLBean.add(fileBeanYL);
                                    }
                                }
                                //=======================================附件end===============================================

                            } else {
                                if (!doc.getContainerName().equals("SL-舞台灯产品库")) {
                                    //==========================================主内容action====================
                                    ContentItem ci = ContentHelper.service.getPrimary(doc);
                                    if (ci != null) {
                                        ApplicationData data = (ApplicationData) ci;                            //主内容
                                        System.out.println("ApplicationData========+" + data);
                                        //String downPath = "smb://rdpplm:rdpplm123@LAPTOP-UL273M6G/excel/";
                                        Map<String, String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
                                        String downPath = mapPath.get("mappingPath");
                                        String name = data.getFileName();
                                        String str = name.substring(name.lastIndexOf("."), name.length());
                                        if (!str.equalsIgnoreCase(".PDF") && !str.equalsIgnoreCase(".pptx") && !str.equalsIgnoreCase(".ppt") && !str.equalsIgnoreCase(".xls") && !str.equalsIgnoreCase(".xlsx") && !str.equalsIgnoreCase(".doc") && !str.equalsIgnoreCase(".docx")) {
                                            UUID u = UUID.randomUUID();
                                            String[] arryName = name.split("\\.");
                                            System.out.println("name===============" + name);
                                            String conversionName = u.toString() + str;
                                            System.out.println("conversionName==========" + conversionName);
                                            downContexts(downPath, data, conversionName);
                                            //得到符合要求的数据
                                            j++;
                                            mapPart.put(part, String.valueOf(j));
                                            FileAttachListBean fileBean = new FileAttachListBean();
                                            fileBean.setFileID("");
                                            fileBean.setAttachName(name);
                                            fileBean.setGuidName(u.toString() + str);
                                            fileBean.setExtensionName(str);
                                            fileBean.setUerNo(part.getNumber());
                                            fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                            listBean.add(fileBean);
                                        }
                                    }
                                    //=======================================主内容end==========================

                                    //======================================附件action============================================
                                    QueryResult pdf = ContentHelper.service.getContentsByRole(doc, ContentRoleType.SECONDARY);
                                    while (pdf.hasMoreElements()) {
                                        ApplicationData data = (ApplicationData) pdf.nextElement();
                                        System.out.println("AttApplicationData========+" + data);
                                        Map<String, String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
                                        String downPath = mapPath.get("mappingPath");
                                        String name = data.getFileName();

                                        String str = name.substring(name.lastIndexOf("."), name.length());
                                        if (str.equalsIgnoreCase(".PDF") || str.equalsIgnoreCase(".pptx") || str.equalsIgnoreCase(".ppt") || str.equalsIgnoreCase(".xls") || str.equalsIgnoreCase(".xlsx") || str.equalsIgnoreCase(".doc") || str.equalsIgnoreCase(".docx")) {
                                            boolean isprint = name.startsWith("PRINT");
                                            if (isprint) {
                                                UUID u = UUID.randomUUID();
                                                String[] arryName = name.split("\\.");
                                                System.out.println("AttName===============" + name);
                                                String conversionName = u.toString() + str;
                                                System.out.println("conversionName==========" + conversionName);
                                                downContexts(downPath, data, conversionName);
                                                //得到符合要求的数据
                                                j++;
                                                mapPart.put(part, String.valueOf(j));
                                                FileAttachListBean fileBean = new FileAttachListBean();
                                                fileBean.setFileID("");
                                                fileBean.setAttachName(name);
                                                fileBean.setGuidName(u.toString() + str);
                                                fileBean.setExtensionName(str);
                                                fileBean.setUerNo(part.getNumber());
                                                fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                                listBean.add(fileBean);
                                            }
                                        } else {
                                            UUID u = UUID.randomUUID();
                                            String[] arryName = name.split("\\.");
                                            System.out.println("AttName===============" + name);
                                            String conversionName = u.toString() + str;
                                            System.out.println("conversionName==========" + conversionName);
                                            downContexts(downPath, data, conversionName);
                                            //得到符合要求的数据
                                            j++;
                                            mapPart.put(part, String.valueOf(j));
                                            FileAttachListBean fileBean = new FileAttachListBean();
                                            fileBean.setFileID("");
                                            fileBean.setAttachName(name);
                                            fileBean.setGuidName(u.toString() + str);
                                            fileBean.setExtensionName(str);
                                            fileBean.setUerNo(part.getNumber());
                                            fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                            listBean.add(fileBean);
                                        }
                                    }
                                    //=======================================附件end===============================================
                                } else {
                                    //==========================================主内容action====================
                                    ContentItem ci = ContentHelper.service.getPrimary(doc);
                                    if (ci != null) {
                                        ApplicationData data = (ApplicationData) ci;                            //主内容
                                        System.out.println("ApplicationData========+" + data);
                                        Map<String, String> mapPath = DataConfigurationPacketTable.getYLSendDocDataBeans();
                                        String downPath = mapPath.get("mappingPath");
                                        String name = data.getFileName();
                                        String str = name.substring(name.lastIndexOf("."), name.length());
                                        if (!str.equalsIgnoreCase(".PDF") && !str.equalsIgnoreCase(".pptx") && !str.equalsIgnoreCase(".ppt") && !str.equalsIgnoreCase(".xls") && !str.equalsIgnoreCase(".xlsx") && !str.equalsIgnoreCase(".doc") && !str.equalsIgnoreCase(".docx")) {
                                            UUID u = UUID.randomUUID();
                                            String[] arryName = name.split("\\.");
                                            System.out.println("name===============" + name);
                                            String conversionName = u.toString() + str;
                                            System.out.println("conversionName==========" + conversionName);
                                            downContexts(downPath, data, conversionName);
                                            //得到符合要求的数据
                                            mapYLPart.put(part, "2");
                                            FileAttachListYLBean fileBean = new FileAttachListYLBean();
                                            fileBean.setFileID("");
                                            fileBean.setAttachName(name);
                                            fileBean.setGuidName(u.toString() + str);
                                            fileBean.setExtensionName(str);
                                            fileBean.setUerNo(part.getNumber());
                                            fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                            listYLBean.add(fileBean);
                                        }
                                    }
                                    //=======================================主内容end==========================

                                    //======================================附件action============================================
                                    QueryResult pdf = ContentHelper.service.getContentsByRole(doc, ContentRoleType.SECONDARY);
                                    while (pdf.hasMoreElements()) {
                                        ApplicationData data = (ApplicationData) pdf.nextElement();
                                        System.out.println("AttApplicationData========+" + data);
                                        Map<String, String> mapPath = DataConfigurationPacketTable.getYLSendDocDataBeans();
                                        String downPath = mapPath.get("mappingPath");
                                        String name = data.getFileName();

                                        String str = name.substring(name.lastIndexOf("."), name.length());
                                        if (str.equalsIgnoreCase(".PDF") || str.equalsIgnoreCase(".pptx") || str.equalsIgnoreCase(".ppt") || str.equalsIgnoreCase(".xls") || str.equalsIgnoreCase(".xlsx") || str.equalsIgnoreCase(".doc") || str.equalsIgnoreCase(".docx")) {
                                            boolean isprint = name.startsWith("PRINT");
                                            if (isprint) {
                                                UUID u = UUID.randomUUID();
                                                String[] arryName = name.split("\\.");
                                                System.out.println("AttName===============" + name);
                                                String conversionName = u.toString() + str;
                                                System.out.println("conversionName==========" + conversionName);
                                                downContexts(downPath, data, conversionName);
                                                //得到符合要求的数据
                                                j++;
                                                mapYLPart.put(part, "2");
                                                FileAttachListYLBean fileBean = new FileAttachListYLBean();
                                                fileBean.setFileID("");
                                                fileBean.setAttachName(name);
                                                fileBean.setGuidName(u.toString() + str);
                                                fileBean.setExtensionName(str);
                                                fileBean.setUerNo(part.getNumber());
                                                fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                                listYLBean.add(fileBean);
                                            }
                                        } else {
                                            UUID u = UUID.randomUUID();
                                            String[] arryName = name.split("\\.");
                                            System.out.println("AttName===============" + name);
                                            String conversionName = u.toString() + str;
                                            System.out.println("conversionName==========" + conversionName);
                                            downContexts(downPath, data, conversionName);
                                            //得到符合要求的数据
                                            j++;
                                            mapYLPart.put(part, "2");
                                            FileAttachListYLBean fileBean = new FileAttachListYLBean();
                                            fileBean.setFileID("");
                                            fileBean.setAttachName(name);
                                            fileBean.setGuidName(u.toString() + str);
                                            fileBean.setExtensionName(str);
                                            fileBean.setUerNo(part.getNumber());
                                            fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                            listYLBean.add(fileBean);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!mapPart.isEmpty()) {
                insertEDFiles(mapPart, listBean);
            }
            if (!mapYLPart.isEmpty()) {
                UploadAttachmentYLContent.insertEDFiles(mapYLPart, listYLBean);
            }

            //------------------随签对象存在--物料关联不存在得文档
            int count = 0;
            System.out.println("map==============" + map);
            Map<WTDocument, String> mapDoc = new HashMap<>();
            Map<WTDocument, String> mapYLDoc = new HashMap<>();
            WTArrayList collectDoc = collect();
            for (int i = 0; i < collectDoc.size(); i++) {
                Persistable persistable = collectDoc.getPersistable(i);
                if (persistable instanceof WTDocument) {
                    WTDocument doc = (WTDocument) persistable;
                    TypeIdentifier typeIdentifier = TypeIdentifierUtility.getTypeIdentifier(doc);
                    String typename = typeIdentifier.getTypename();
                    if (typename.contains("com.ptc.ReferenceDocument")) {
                        System.out.println("doc=============" + doc);
                        String type = UploadAttachmentUtil.getTypeInternalName(doc);
                        String passageInventory = (String) PdfUtil.getIBAObjectValue(doc, "SmallDocType");
                        System.out.println("type=======" + type);
                        System.out.println("passageInventory=======" + passageInventory);
                        if (map.keySet().toString().contains(type)) {

                            if (!doc.getContainerName().equals("SL-舞台灯产品库")) {
                                //==========================================主内容action====================
                                ContentItem ci = ContentHelper.service.getPrimary(doc);
                                if (ci != null) {
                                    ApplicationData data = (ApplicationData) ci;                            //主内容
                                    System.out.println("ApplicationData=======" + data);
                                    Map<String, String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
                                    String downPath = mapPath.get("mappingPath");
                                    String name = data.getFileName();                                       //文件名--取原始名称
                                    String str = name.substring(name.lastIndexOf("."), name.length());
                                    if (!str.equalsIgnoreCase(".PDF") && !str.equalsIgnoreCase(".pptx") && !str.equalsIgnoreCase(".ppt") && !str.equalsIgnoreCase(".xls") && !str.equalsIgnoreCase(".xlsx") && !str.equalsIgnoreCase(".doc") && !str.equalsIgnoreCase(".docx")) {
                                        UUID u = UUID.randomUUID();
                                        String[] arryName = name.split("\\.");
                                        String conversionName = u.toString() + str;
                                        System.out.println("conversionName==========" + conversionName);
                                        System.out.println("name==================" + name);
                                        downContexts(downPath, data, conversionName);                                //下载文件
                                        //得到符合要求的数据
                                        count++;
                                        mapDoc.put(doc, String.valueOf(count));
                                        FileAttachListBean fileBean = new FileAttachListBean();
                                        fileBean.setFileID("");
                                        fileBean.setAttachName(name);
                                        fileBean.setGuidName(u.toString() + str);
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
                                    System.out.println("ApplicationData=======" + data);
                                    Map<String, String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
                                    String downPath = mapPath.get("mappingPath");
                                    String name = data.getFileName();
                                    System.out.println("name===============" + name);
                                    String str = name.substring(name.lastIndexOf("."), name.length());
                                    if (str.equalsIgnoreCase(".PDF") || str.equalsIgnoreCase(".pptx") || str.equalsIgnoreCase(".ppt") || str.equalsIgnoreCase(".xls") || str.equalsIgnoreCase(".xlsx") || str.equalsIgnoreCase(".doc") || str.equalsIgnoreCase(".docx")) {
                                        boolean isprint = name.startsWith("PRINT");
                                        if (isprint) {
                                            UUID u = UUID.randomUUID();
                                            String[] arryName = name.split("\\.");
                                            String conversionName = u.toString() + str;
                                            System.out.println("conversionName==========" + conversionName);
                                            downContexts(downPath, data, conversionName);
                                            count++;
                                            //得到符合要求的数据
                                            mapDoc.put(doc, String.valueOf(count));
                                            FileAttachListBean fileBean = new FileAttachListBean();
                                            fileBean.setFileID("");
                                            fileBean.setAttachName(name);
                                            fileBean.setGuidName(u.toString() + str);
                                            fileBean.setExtensionName(str);
                                            fileBean.setUerNo(doc.getNumber());
                                            fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                            listBean.add(fileBean);
                                        }
                                    } else {
                                        UUID u = UUID.randomUUID();
                                        String[] arryName = name.split("\\.");
                                        String conversionName = u.toString() + str;
                                        System.out.println("conversionName==========" + conversionName);
                                        downContexts(downPath, data, conversionName);
                                        count++;
                                        //得到符合要求的数据
                                        mapDoc.put(doc, String.valueOf(count));
                                        FileAttachListBean fileBean = new FileAttachListBean();
                                        fileBean.setFileID("");
                                        fileBean.setAttachName(name);
                                        fileBean.setGuidName(u.toString() + str);
                                        fileBean.setExtensionName(str);
                                        fileBean.setUerNo(doc.getNumber());
                                        fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                        listBean.add(fileBean);
                                    }
                                }

                                //=======================================附件end===============================================
                            } else {
                                //==========================================主内容action====================
                                ContentItem ci = ContentHelper.service.getPrimary(doc);
                                if (ci != null) {
                                    ApplicationData data = (ApplicationData) ci;                            //主内容
                                    System.out.println("ApplicationData=======" + data);
                                    Map<String, String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
                                    String downPath = mapPath.get("mappingPath");
                                    String name = data.getFileName();                                       //文件名--取原始名称
                                    String str = name.substring(name.lastIndexOf("."), name.length());
                                    if (!str.equalsIgnoreCase(".PDF") && !str.equalsIgnoreCase(".pptx") && !str.equalsIgnoreCase(".ppt") && !str.equalsIgnoreCase(".xls") && !str.equalsIgnoreCase(".xlsx") && !str.equalsIgnoreCase(".doc") && !str.equalsIgnoreCase(".docx")) {
                                        UUID u = UUID.randomUUID();
                                        String[] arryName = name.split("\\.");
                                        String conversionName = u.toString() + str;
                                        System.out.println("conversionName==========" + conversionName);
                                        System.out.println("name==================" + name);
                                        downContexts(downPath, data, conversionName);                                //下载文件
                                        mapYLDoc.put(doc, "2");
                                        FileAttachListYLBean fileBean = new FileAttachListYLBean();
                                        fileBean.setFileID("");
                                        fileBean.setAttachName(name);
                                        fileBean.setGuidName(u.toString() + str);
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
                                    System.out.println("ApplicationData=======" + data);
                                    Map<String, String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
                                    String downPath = mapPath.get("mappingPath");
                                    String name = data.getFileName();
                                    System.out.println("name===============" + name);
                                    String str = name.substring(name.lastIndexOf("."), name.length());
                                    if (str.equalsIgnoreCase(".PDF") || str.equalsIgnoreCase(".pptx") || str.equalsIgnoreCase(".ppt") || str.equalsIgnoreCase(".xls") || str.equalsIgnoreCase(".xlsx") || str.equalsIgnoreCase(".doc") || str.equalsIgnoreCase(".docx")) {
                                        boolean isprint = name.startsWith("PRINT");
                                        if (isprint) {
                                            UUID u = UUID.randomUUID();
                                            String[] arryName = name.split("\\.");
                                            String conversionName = u.toString() + str;
                                            System.out.println("conversionName==========" + conversionName);
                                            downContexts(downPath, data, conversionName);
                                            mapYLDoc.put(doc, "2");
                                            FileAttachListYLBean fileBean = new FileAttachListYLBean();
                                            fileBean.setFileID("");
                                            fileBean.setAttachName(name);
                                            fileBean.setGuidName(u.toString() + str);
                                            fileBean.setExtensionName(str);
                                            fileBean.setUerNo(doc.getNumber());
                                            fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                            listYLBean.add(fileBean);
                                        }
                                    } else {
                                        UUID u = UUID.randomUUID();
                                        String[] arryName = name.split("\\.");
                                        String conversionName = u.toString() + str;
                                        System.out.println("conversionName==========" + conversionName);
                                        downContexts(downPath, data, conversionName);
                                        count++;
                                        //得到符合要求的数据
                                        mapYLDoc.put(doc, "2");
                                        FileAttachListYLBean fileBean = new FileAttachListYLBean();
                                        fileBean.setFileID("");
                                        fileBean.setAttachName(name);
                                        fileBean.setGuidName(u.toString() + str);
                                        fileBean.setExtensionName(str);
                                        fileBean.setUerNo(doc.getNumber());
                                        fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                        listYLBean.add(fileBean);
                                    }
                                }
                                //=======================================附件end===============================================
                            }
                        }
                    }
                    if (typename.contains("com.plm.description_square_document")) {
                        if (!signUpDocSet.contains(doc)) {
                            String type = UploadAttachmentUtil.getTypeInternalName(doc);
                            String passageInventory = (String) PdfUtil.getIBAObjectValue(doc, "SmallDocType");
                            System.out.println("type=======" + type);
                            System.out.println("passageInventory=============" + passageInventory);
                            if (map.keySet().toString().contains(type)) {

                                if (type.equals("com.plm.drawingdoc")) {             //------------------1.图纸不主内容不发PDF格式--2.附件有PDF必须PRINT开头

                                    WTContainer container = doc.getContainer();
                                    if (container instanceof WTLibrary) {
                                        WTLibrary library = (WTLibrary) container;
                                        String libName = library.getName();
                                        if (libName.contains("原材料库")) {
                                            //==========================================主内容action====================
                                            ContentItem ci = ContentHelper.service.getPrimary(doc);
                                            if (ci != null) {
                                                ApplicationData data = (ApplicationData) ci;                            //主内容
                                                System.out.println("ApplicationData========+" + data);
                                                //============光峰
                                                Map<String, String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
                                                String downPath = mapPath.get("mappingPath");
                                                String name = data.getFileName();
                                                String str = name.substring(name.lastIndexOf("."), name.length());
                                                if (!str.equalsIgnoreCase(".PDF") && !str.equalsIgnoreCase(".pptx") && !str.equalsIgnoreCase(".ppt") && !str.equalsIgnoreCase(".xls") && !str.equalsIgnoreCase(".xlsx") && !str.equalsIgnoreCase(".doc") && !str.equalsIgnoreCase(".docx")) {
                                                    UUID u = UUID.randomUUID();
                                                    String[] arryName = name.split("\\.");
                                                    System.out.println("name===============" + name);
                                                    String conversionName = u.toString() + str;
                                                    System.out.println("conversionName==========" + conversionName);
                                                    downContexts(downPath, data, conversionName);
                                                    j++;
                                                    mapDoc.put(doc, String.valueOf(j));
                                                    FileAttachListBean fileBean = new FileAttachListBean();
                                                    fileBean.setFileID("");
                                                    fileBean.setAttachName(name);
                                                    fileBean.setGuidName(u.toString() + str);
                                                    fileBean.setExtensionName(str);
                                                    fileBean.setUerNo(doc.getNumber());
                                                    fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                                    listBean.add(fileBean);
                                                }
//
                                                //===========绎立
                                                Map<String, String> mapPathYL = DataConfigurationPacketTable.getYLSendDocDataBeans();
                                                String downPathYL = mapPathYL.get("mappingPath");
                                                String nameYL = data.getFileName();
                                                String strYL = nameYL.substring(nameYL.lastIndexOf("."), nameYL.length());
                                                if (!strYL.equalsIgnoreCase(".PDF") && !strYL.equalsIgnoreCase(".pptx") && !strYL.equalsIgnoreCase(".ppt") && !strYL.equalsIgnoreCase(".xls") && !strYL.equalsIgnoreCase(".xlsx") && !strYL.equalsIgnoreCase(".doc") && !strYL.equalsIgnoreCase(".docx")) {
                                                    UUID u = UUID.randomUUID();
                                                    String[] arryName = nameYL.split("\\.");
                                                    System.out.println("name===============" + nameYL);
                                                    String conversionName = u.toString() + str;
                                                    System.out.println("conversionName==========" + conversionName);
                                                    downContexts(downPathYL, data, conversionName);
                                                    mapYLDoc.put(doc, "1");
                                                    FileAttachListYLBean fileBean = new FileAttachListYLBean();
                                                    fileBean.setFileID("");
                                                    fileBean.setAttachName(nameYL);
                                                    fileBean.setGuidName(u.toString() + str);
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
                                                System.out.println("AttApplicationData========+" + data);
                                                Map<String, String> mapPathYL = DataConfigurationPacketTable.getYLSendDocDataBeans();
                                                String downPathYL = mapPathYL.get("mappingPath");
                                                //光峰
                                                Map<String, String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
                                                String downPath = mapPath.get("mappingPath");
                                                String name = data.getFileName();
                                                String str = name.substring(name.lastIndexOf("."), name.length());
                                                if (str.equalsIgnoreCase(".PDF") || str.equalsIgnoreCase(".pptx") || str.equalsIgnoreCase(".ppt") || str.equalsIgnoreCase(".xls") || str.equalsIgnoreCase(".xlsx") || str.equalsIgnoreCase(".doc") || str.equalsIgnoreCase(".docx")) {
                                                    boolean isprint = name.startsWith("PRINT");
                                                    if (isprint) {

                                                        //光峰
                                                        UUID u = UUID.randomUUID();
                                                        String[] arryName = name.split("\\.");
                                                        System.out.println("AttName===============" + name);
                                                        String conversionName = u.toString() + str;
                                                        System.out.println("conversionName==========" + conversionName);
                                                        downContexts(downPath, data, conversionName);
                                                        mapDoc.put(doc, String.valueOf(j));
                                                        FileAttachListBean fileBean = new FileAttachListBean();
                                                        fileBean.setFileID("");
                                                        fileBean.setAttachName(name);
                                                        fileBean.setGuidName(u.toString() + str);
                                                        fileBean.setExtensionName(str);
                                                        fileBean.setUerNo(doc.getNumber());
                                                        fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                                        listBean.add(fileBean);

                                                        //YL
                                                        UUID uYL = UUID.randomUUID();
                                                        String[] arryNameYL = name.split("\\.");
                                                        System.out.println("AttName===============" + name);
                                                        String conversionNameYL = uYL.toString() + "." + arryNameYL[1];
                                                        System.out.println("conversionName==========" + conversionNameYL);
                                                        downContexts(downPathYL, data, conversionNameYL);
                                                        j++;
                                                        mapYLDoc.put(doc, "1");
                                                        FileAttachListYLBean fileBeanYL = new FileAttachListYLBean();
                                                        fileBeanYL.setFileID("");
                                                        fileBeanYL.setAttachName(name);
                                                        fileBeanYL.setGuidName(uYL.toString() + "." + arryNameYL[1]);
                                                        fileBeanYL.setExtensionName("." + arryNameYL[1]);
                                                        fileBeanYL.setUerNo(doc.getNumber());
                                                        fileBeanYL.setFileSize(String.valueOf(data.getFileSize()));
                                                        listYLBean.add(fileBeanYL);
                                                    }
                                                } else {
                                                    //光峰
                                                    UUID u = UUID.randomUUID();
                                                    String[] arryName = name.split("\\.");
                                                    System.out.println("AttName===============" + name);
                                                    String conversionName = u.toString() + str;
                                                    System.out.println("conversionName==========" + conversionName);
                                                    downContexts(downPath, data, conversionName);
                                                    j++;
                                                    mapDoc.put(doc, String.valueOf(j));
                                                    FileAttachListBean fileBean = new FileAttachListBean();
                                                    fileBean.setFileID("");
                                                    fileBean.setAttachName(name);
                                                    fileBean.setGuidName(u.toString() + str);
                                                    fileBean.setExtensionName(str);
                                                    fileBean.setUerNo(doc.getNumber());
                                                    fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                                    listBean.add(fileBean);

                                                    //绎立
                                                    UUID uYL = UUID.randomUUID();
                                                    String[] arryNameYL = name.split("\\.");
                                                    System.out.println("AttName===============" + name);
                                                    String conversionNameYL = uYL.toString() + "." + arryNameYL[1];
                                                    System.out.println("conversionName==========" + conversionNameYL);
                                                    downContexts(downPathYL, data, conversionNameYL);
                                                    j++;
                                                    mapYLDoc.put(doc, "1");
                                                    FileAttachListYLBean fileBeanYL = new FileAttachListYLBean();
                                                    fileBeanYL.setFileID("");
                                                    fileBeanYL.setAttachName(name);
                                                    fileBeanYL.setGuidName(uYL.toString() + "." + arryNameYL[1]);
                                                    fileBeanYL.setExtensionName("." + arryNameYL[1]);
                                                    fileBeanYL.setUerNo(doc.getNumber());
                                                    fileBeanYL.setFileSize(String.valueOf(data.getFileSize()));
                                                    listYLBean.add(fileBeanYL);
                                                }
                                            }
                                        }
                                    }
                                    //=======================================附件end===============================================


                                    //=======================================主内容end==========================
                                } else if (type.equals("com.plm.softwaredoc")) {                                    //软件只发送zip,rar格式
                                    //==========================================主内容action====================

                                    if (!doc.getContainerName().equals("SL-舞台灯产品库")) {
                                        ContentItem ci = ContentHelper.service.getPrimary(doc);
                                        if (ci != null) {
                                            ApplicationData data = (ApplicationData) ci;                            //主内容
                                            System.out.println("ApplicationData========+" + data);
                                            Map<String, String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
                                            String downPath = mapPath.get("mappingPath");
                                            String name = data.getFileName();
                                            String str = name.substring(name.lastIndexOf("."), name.length());
                                            if (str.equalsIgnoreCase(".zip") || str.equalsIgnoreCase(".rar")) {
                                                UUID u = UUID.randomUUID();
                                                String[] arryName = name.split("\\.");
                                                System.out.println("name===============" + name);
                                                String conversionName = u.toString() + str;
                                                System.out.println("conversionName==========" + conversionName);
                                                downContexts(downPath, data, conversionName);
                                                //得到符合要求的数据
                                                j++;
                                                mapDoc.put(doc, String.valueOf(j));
                                                FileAttachListBean fileBean = new FileAttachListBean();
                                                fileBean.setFileID("");
                                                fileBean.setAttachName(name);
                                                fileBean.setGuidName(u.toString() + str);
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
                                            System.out.println("AttApplicationData========+" + data);
                                            Map<String, String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
                                            String downPath = mapPath.get("mappingPath");
                                            String name = data.getFileName();
                                            String str = name.substring(name.lastIndexOf("."), name.length());
                                            if (str.equalsIgnoreCase(".zip") || str.equalsIgnoreCase(".rar")) {
                                                UUID u = UUID.randomUUID();
                                                String[] arryName = name.split("\\.");
                                                System.out.println("AttName===============" + name);
                                                String conversionName = u.toString() + str;
                                                System.out.println("conversionName==========" + conversionName);
                                                downContexts(downPath, data, conversionName);
                                                mapDoc.put(doc, String.valueOf(j));
                                                FileAttachListBean fileBean = new FileAttachListBean();
                                                fileBean.setFileID("");
                                                fileBean.setAttachName(name);
                                                fileBean.setGuidName(u.toString() + str);
                                                fileBean.setExtensionName(str);
                                                fileBean.setUerNo(doc.getNumber());
                                                fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                                listBean.add(fileBean);
                                            }
                                        }

                                    } else {

                                        ContentItem ci = ContentHelper.service.getPrimary(doc);
                                        if (ci != null) {
                                            ApplicationData data = (ApplicationData) ci;                            //主内容
                                            System.out.println("ApplicationData========+" + data);
                                            Map<String, String> mapPath = DataConfigurationPacketTable.getYLSendDocDataBeans();
                                            String downPath = mapPath.get("mappingPath");
                                            String name = data.getFileName();
                                            String str = name.substring(name.lastIndexOf("."), name.length());
                                            if (str.equalsIgnoreCase(".zip") || str.equalsIgnoreCase(".rar")) {
                                                UUID u = UUID.randomUUID();
                                                String[] arryName = name.split("\\.");
                                                System.out.println("name===============" + name);
                                                String conversionName = u.toString() + str;
                                                System.out.println("conversionName==========" + conversionName);
                                                downContexts(downPath, data, conversionName);
                                                //得到符合要求的数据
                                                j++;
                                                mapYLDoc.put(doc, "2");
                                                FileAttachListYLBean fileBean = new FileAttachListYLBean();
                                                fileBean.setFileID("");
                                                fileBean.setAttachName(name);
                                                fileBean.setGuidName(u.toString() + str);
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
                                            System.out.println("AttApplicationData========+" + data);
                                            Map<String, String> mapPath = DataConfigurationPacketTable.getYLSendDocDataBeans();
                                            String downPath = mapPath.get("mappingPath");
                                            String name = data.getFileName();
                                            String str = name.substring(name.lastIndexOf("."), name.length());
                                            if (str.equalsIgnoreCase(".zip") || str.equalsIgnoreCase(".rar")) {
                                                UUID u = UUID.randomUUID();
                                                String[] arryName = name.split("\\.");
                                                System.out.println("AttName===============" + name);
                                                String conversionName = u.toString() + str;
                                                System.out.println("conversionName==========" + conversionName);
                                                downContexts(downPath, data, conversionName);
                                                j++;
                                                mapYLDoc.put(doc, "2");
                                                FileAttachListYLBean fileBean = new FileAttachListYLBean();
                                                fileBean.setFileID("");
                                                fileBean.setAttachName(name);
                                                fileBean.setGuidName(u.toString() + str);
                                                fileBean.setExtensionName(str);
                                                fileBean.setUerNo(doc.getNumber());
                                                fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                                listYLBean.add(fileBean);
                                            }
                                        }
                                    }
                                    //=======================================附件end===============================================
                                } else if (type.equals("com.plm.datasheet")) {
                                    //==========================================主内容action====================
                                    ContentItem ci = ContentHelper.service.getPrimary(doc);
                                    if (ci != null) {
                                        ApplicationData data = (ApplicationData) ci;                            //主内容
                                        System.out.println("ApplicationData========+" + data);
                                        //光峰
                                        Map<String, String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
                                        String downPath = mapPath.get("mappingPath");
                                        String name = data.getFileName();
                                        String str = name.substring(name.lastIndexOf("."), name.length());
                                        if (!str.equalsIgnoreCase(".PDF") && !str.equalsIgnoreCase(".pptx") && !str.equalsIgnoreCase(".ppt") && !str.equalsIgnoreCase(".xls") && !str.equalsIgnoreCase(".xlsx") && !str.equalsIgnoreCase(".doc") && !str.equalsIgnoreCase(".docx")) {
                                            UUID u = UUID.randomUUID();
                                            String[] arryName = name.split("\\.");
                                            System.out.println("name===============" + name);
                                            String conversionName = u.toString() + str;
                                            System.out.println("conversionName==========" + conversionName);
                                            downContexts(downPath, data, conversionName);
                                            //得到符合要求的数据
                                            j++;
                                            mapDoc.put(doc, String.valueOf(j));
                                            FileAttachListBean fileBean = new FileAttachListBean();
                                            fileBean.setFileID("");
                                            fileBean.setAttachName(name);
                                            fileBean.setGuidName(u.toString() + str);
                                            fileBean.setExtensionName(str);
                                            fileBean.setUerNo(doc.getNumber());
                                            fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                            listBean.add(fileBean);
                                        }

                                        //YL
                                        Map<String, String> mapPathYL = DataConfigurationPacketTable.getYLSendDocDataBeans();
                                        String downPathYL = mapPathYL.get("mappingPath");
                                        String nameYL = data.getFileName();
                                        String strYL = nameYL.substring(nameYL.lastIndexOf("."), nameYL.length());
                                        if (!strYL.equalsIgnoreCase(".PDF") && !strYL.equalsIgnoreCase(".pptx") && !strYL.equalsIgnoreCase(".ppt") && !strYL.equalsIgnoreCase(".xls") && !strYL.equalsIgnoreCase(".xlsx") && !strYL.equalsIgnoreCase(".doc") && !strYL.equalsIgnoreCase(".docx")) {
                                            UUID u = UUID.randomUUID();
                                            String[] arryName = nameYL.split("\\.");
                                            System.out.println("name===============" + nameYL);
                                            String conversionName = u.toString() + str;
                                            System.out.println("conversionName==========" + conversionName);
                                            downContexts(downPathYL, data, conversionName);
                                            //得到符合要求的数据
                                            mapYLDoc.put(doc, "3");
                                            FileAttachListYLBean fileBean = new FileAttachListYLBean();
                                            fileBean.setFileID("");
                                            fileBean.setAttachName(nameYL);
                                            fileBean.setGuidName(u.toString() + str);
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
                                        System.out.println("AttApplicationData========+" + data);
                                        Map<String, String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
                                        String downPath = mapPath.get("mappingPath");
                                        String name = data.getFileName();
                                        Map<String, String> mapPathYL = DataConfigurationPacketTable.getYLSendDocDataBeans();
                                        String downPathYL = mapPathYL.get("mappingPath");

                                        String str = name.substring(name.lastIndexOf("."), name.length());
                                        if (str.equalsIgnoreCase(".PDF") || str.equalsIgnoreCase(".pptx") || str.equalsIgnoreCase(".ppt") || str.equalsIgnoreCase(".xls") || str.equalsIgnoreCase(".xlsx") || str.equalsIgnoreCase(".doc") || str.equalsIgnoreCase(".docx")) {
                                            boolean isprint = name.startsWith("PRINT");
                                            if (isprint) {
                                                UUID u = UUID.randomUUID();
                                                String[] arryName = name.split("\\.");
                                                System.out.println("AttName===============" + name);
                                                String conversionName = u.toString() + str;
                                                System.out.println("conversionName==========" + conversionName);
                                                downContexts(downPath, data, conversionName);
                                                j++;
                                                mapDoc.put(doc, String.valueOf(j));
                                                FileAttachListBean fileBean = new FileAttachListBean();
                                                fileBean.setFileID("");
                                                fileBean.setAttachName(name);
                                                fileBean.setGuidName(u.toString() + str);
                                                fileBean.setExtensionName(str);
                                                fileBean.setUerNo(doc.getNumber());
                                                fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                                listBean.add(fileBean);

                                                //绎立
                                                UUID uYL = UUID.randomUUID();
                                                String[] arryNameYL = name.split("\\.");
                                                System.out.println("AttName===============" + name);
                                                String conversionNameYL = uYL.toString() + "." + arryNameYL[1];
                                                System.out.println("conversionName==========" + conversionNameYL);
                                                downContexts(downPathYL, data, conversionNameYL);
                                                j++;
                                                mapYLDoc.put(doc, "3");
                                                FileAttachListYLBean fileBeanYL = new FileAttachListYLBean();
                                                fileBeanYL.setFileID("");
                                                fileBeanYL.setAttachName(name);
                                                fileBeanYL.setGuidName(uYL.toString() + "." + arryNameYL[1]);
                                                fileBeanYL.setExtensionName("." + arryNameYL[1]);
                                                fileBeanYL.setUerNo(doc.getNumber());
                                                fileBeanYL.setFileSize(String.valueOf(data.getFileSize()));
                                                listYLBean.add(fileBeanYL);
                                            }

                                        } else {
                                            UUID u = UUID.randomUUID();
                                            String[] arryName = name.split("\\.");
                                            System.out.println("AttName===============" + name);
                                            String conversionName = u.toString() + str;
                                            System.out.println("conversionName==========" + conversionName);
                                            downContexts(downPath, data, conversionName);
                                            j++;
                                            mapDoc.put(doc, String.valueOf(j));
                                            FileAttachListBean fileBean = new FileAttachListBean();
                                            fileBean.setFileID("");
                                            fileBean.setAttachName(name);
                                            fileBean.setGuidName(u.toString() + str);
                                            fileBean.setExtensionName(str);
                                            fileBean.setUerNo(doc.getNumber());
                                            fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                            listBean.add(fileBean);

                                            //绎立
                                            UUID uYL = UUID.randomUUID();
                                            String[] arryNameYL = name.split("\\.");
                                            System.out.println("AttName===============" + name);
                                            String conversionNameYL = uYL.toString() + "." + arryNameYL[1];
                                            System.out.println("conversionName==========" + conversionNameYL);
                                            downContexts(downPathYL, data, conversionNameYL);
                                            j++;
                                            mapYLDoc.put(doc, "3");
                                            FileAttachListYLBean fileBeanYL = new FileAttachListYLBean();
                                            fileBeanYL.setFileID("");
                                            fileBeanYL.setAttachName(name);
                                            fileBeanYL.setGuidName(uYL.toString() + "." + arryNameYL[1]);
                                            fileBeanYL.setExtensionName("." + arryNameYL[1]);
                                            fileBeanYL.setUerNo(doc.getNumber());
                                            fileBeanYL.setFileSize(String.valueOf(data.getFileSize()));
                                            listYLBean.add(fileBeanYL);
                                        }
                                    }
                                    //=======================================附件end===============================================

                                } else {
                                    if (!doc.getContainerName().equals("SL-舞台灯产品库")) {
                                        //==========================================主内容action====================
                                        ContentItem ci = ContentHelper.service.getPrimary(doc);
                                        if (ci != null) {
                                            ApplicationData data = (ApplicationData) ci;                            //主内容
                                            System.out.println("ApplicationData========+" + data);
                                            Map<String, String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
                                            String downPath = mapPath.get("mappingPath");
                                            String name = data.getFileName();
                                            String str = name.substring(name.lastIndexOf("."), name.length());
                                            if (!str.equalsIgnoreCase(".PDF") && !str.equalsIgnoreCase(".pptx") && !str.equalsIgnoreCase(".ppt") && !str.equalsIgnoreCase(".xls") && !str.equalsIgnoreCase(".xlsx") && !str.equalsIgnoreCase(".doc") && !str.equalsIgnoreCase(".docx")) {
                                                UUID u = UUID.randomUUID();
                                                String[] arryName = name.split("\\.");
                                                System.out.println("name===============" + name);
                                                String conversionName = u.toString() + str;
                                                System.out.println("conversionName==========" + conversionName);
                                                downContexts(downPath, data, conversionName);
                                                //得到符合要求的数据
                                                j++;
                                                mapDoc.put(doc, String.valueOf(j));
                                                FileAttachListBean fileBean = new FileAttachListBean();
                                                fileBean.setFileID("");
                                                fileBean.setAttachName(name);
                                                fileBean.setGuidName(u.toString() + str);
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
                                            System.out.println("AttApplicationData========+" + data);
                                            Map<String, String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
                                            String downPath = mapPath.get("mappingPath");
                                            String name = data.getFileName();

                                            String str = name.substring(name.lastIndexOf("."), name.length());
                                            if (str.equalsIgnoreCase(".PDF") || str.equalsIgnoreCase(".pptx") || str.equalsIgnoreCase(".ppt") || str.equalsIgnoreCase(".xls") || str.equalsIgnoreCase(".xlsx") || str.equalsIgnoreCase(".doc") || str.equalsIgnoreCase(".docx")) {
                                                boolean isprint = name.startsWith("PRINT");
                                                if (isprint) {
                                                    UUID u = UUID.randomUUID();
                                                    String[] arryName = name.split("\\.");
                                                    System.out.println("AttName===============" + name);
                                                    String conversionName = u.toString() + str;
                                                    System.out.println("conversionName==========" + conversionName);
                                                    downContexts(downPath, data, conversionName);
                                                    j++;
                                                    mapDoc.put(doc, String.valueOf(j));
                                                    FileAttachListBean fileBean = new FileAttachListBean();
                                                    fileBean.setFileID("");
                                                    fileBean.setAttachName(name);
                                                    fileBean.setGuidName(u.toString() + str);
                                                    fileBean.setExtensionName(str);
                                                    fileBean.setUerNo(doc.getNumber());
                                                    fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                                    listBean.add(fileBean);
                                                }
                                            } else {
                                                UUID u = UUID.randomUUID();
                                                String[] arryName = name.split("\\.");
                                                System.out.println("AttName===============" + name);
                                                String conversionName = u.toString() + str;
                                                System.out.println("conversionName==========" + conversionName);
                                                downContexts(downPath, data, conversionName);
                                                //UUID u = UUID.randomUUID();
                                                //得到符合要求的数据
                                                j++;
                                                mapDoc.put(doc, String.valueOf(j));
                                                FileAttachListBean fileBean = new FileAttachListBean();
                                                fileBean.setFileID("");
                                                fileBean.setAttachName(name);
                                                fileBean.setGuidName(u.toString() + str);
                                                fileBean.setExtensionName(str);
                                                fileBean.setUerNo(doc.getNumber());
                                                fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                                listBean.add(fileBean);
                                            }
                                        }
                                        //=======================================附件end===============================================
                                    } else {
                                        //==========================================主内容action====================
                                        ContentItem ci = ContentHelper.service.getPrimary(doc);
                                        if (ci != null) {
                                            ApplicationData data = (ApplicationData) ci;                            //主内容
                                            System.out.println("ApplicationData========+" + data);
                                            Map<String, String> mapPath = DataConfigurationPacketTable.getYLSendDocDataBeans();
                                            String downPath = mapPath.get("mappingPath");
                                            String name = data.getFileName();
                                            String str = name.substring(name.lastIndexOf("."), name.length());
                                            if (!str.equalsIgnoreCase(".PDF") && !str.equalsIgnoreCase(".pptx") && !str.equalsIgnoreCase(".ppt") && !str.equalsIgnoreCase(".xls") && !str.equalsIgnoreCase(".xlsx") && !str.equalsIgnoreCase(".doc") && !str.equalsIgnoreCase(".docx")) {
                                                UUID u = UUID.randomUUID();
                                                String[] arryName = name.split("\\.");
                                                System.out.println("name===============" + name);
                                                String conversionName = u.toString() + str;
                                                System.out.println("conversionName==========" + conversionName);
                                                downContexts(downPath, data, conversionName);
                                                //得到符合要求的数据
                                                mapYLDoc.put(doc, "2");
                                                FileAttachListYLBean fileBean = new FileAttachListYLBean();
                                                fileBean.setFileID("");
                                                fileBean.setAttachName(name);
                                                fileBean.setGuidName(u.toString() + str);
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
                                            System.out.println("AttApplicationData========+" + data);
                                            Map<String, String> mapPath = DataConfigurationPacketTable.getYLSendDocDataBeans();
                                            String downPath = mapPath.get("mappingPath");
                                            String name = data.getFileName();

                                            String str = name.substring(name.lastIndexOf("."), name.length());
                                            if (str.equalsIgnoreCase(".PDF") || str.equalsIgnoreCase(".pptx") || str.equalsIgnoreCase(".ppt") || str.equalsIgnoreCase(".xls") || str.equalsIgnoreCase(".xlsx") || str.equalsIgnoreCase(".doc") || str.equalsIgnoreCase(".docx")) {
                                                boolean isprint = name.startsWith("PRINT");
                                                if (isprint) {
                                                    UUID u = UUID.randomUUID();
                                                    String[] arryName = name.split("\\.");
                                                    System.out.println("AttName===============" + name);
                                                    String conversionName = u.toString() + str;
                                                    System.out.println("conversionName==========" + conversionName);
                                                    downContexts(downPath, data, conversionName);
                                                    j++;
                                                    mapYLDoc.put(doc, "2");
                                                    FileAttachListYLBean fileBean = new FileAttachListYLBean();
                                                    fileBean.setFileID("");
                                                    fileBean.setAttachName(name);
                                                    fileBean.setGuidName(u.toString() + str);
                                                    fileBean.setExtensionName(str);
                                                    fileBean.setUerNo(doc.getNumber());
                                                    fileBean.setFileSize(String.valueOf(data.getFileSize()));
                                                    listYLBean.add(fileBean);
                                                }
                                            } else {
                                                UUID u = UUID.randomUUID();
                                                String[] arryName = name.split("\\.");
                                                System.out.println("AttName===============" + name);
                                                String conversionName = u.toString() + str;
                                                System.out.println("conversionName==========" + conversionName);
                                                downContexts(downPath, data, conversionName);
                                                j++;
                                                mapYLDoc.put(doc, "2");
                                                FileAttachListYLBean fileBean = new FileAttachListYLBean();
                                                fileBean.setFileID("");
                                                fileBean.setAttachName(name);
                                                fileBean.setGuidName(u.toString() + str);
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
                    }
                }
            }
            if (!mapDoc.isEmpty()) {
                DocUploadAttachmentContent.insertEDFiles(mapDoc, listBean);
            }

            if (!mapYLDoc.isEmpty()) {
                DocUploadAttachmentYLContent.insertEDFiles(mapYLDoc, listYLBean);
            }


            Set<String> filesCount = checkFilesCount();
            System.out.println("filesCount2=2=================================" + filesCount);
            updateFilesCount(filesCount);

            Set<String> filesCount2 = UploadAttachmentYLContent.checkFilesCount();
            System.out.println("filesCount2=2=================================" + filesCount);
            UploadAttachmentYLContent.updateFilesCount(filesCount2);
        } catch (IOException | PropertyVetoException e) {
            e.printStackTrace();
        } finally {
            SessionServerHelper.manager.setAccessEnforced(flag); // 添加权限
        }

    }

    /**
     * 修改文件夹总数量
     *
     * @param partNumber
     * @param ver
     * @return
     * @throws IOException
     * @throws WTException
     */
    public static int updateFilesCount(Set<String> filesCount) throws IOException, WTException {
        Connection conn = getConn();
        System.out.println("updateIsinvalidConn================" + conn);
        int i = 0;
        String sql = "update ED_Folder set ChildFileCount=? where folderid=?";
        PreparedStatement pstmt = null;
        try {
            System.out.println("filesCount==========================================" + filesCount);
            for (String str : filesCount) {
                String[] arry = str.split("___");
//			    		System.out.println("arry[]======================================="+arry);
//			    		System.out.println("arry[0]======================================="+arry[0]);
//			    		System.out.println("arry[1]======================================="+arry[1]);
                pstmt = (PreparedStatement) conn.prepareStatement(sql);
                pstmt.setString(1, arry[1]);
                pstmt.setString(2, arry[0]);
                i = pstmt.executeUpdate();
            }
            System.out.println("updateIsinvalidPstmt=======================================" + pstmt);
            System.out.println("updateIsinvalidSqlSize================================================" + i);
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return i;
    }

    /**
     * 查询文件夹总数量
     *
     * @return
     * @throws IOException
     * @throws WTException
     */
    public static Set<String> checkFilesCount() throws IOException, WTException {
        PreparedStatement pstmt;
        Set<String> set = new HashSet<String>();
        try {
            Connection conn = getConn();
            System.out.println("checkFolderId_________=====================" + conn);
            String sql = "select folderid,count(*) from ED_Files  GROUP BY folderid";

            System.out.println("sql___________=========================" + sql);
            pstmt = (PreparedStatement) conn.prepareStatement(sql);
            System.out.println("checkFolderId___________============pstmt=========" + pstmt);
            ResultSet rs = pstmt.executeQuery();
            System.out.println("checkFolderId___________============rs=========" + rs);
            //5.处理ResultSet
            while (rs.next()) {
                //rs.get+数据库中对应的类型+(数据库中对应的列别名)
                set.add(rs.getString(1) + "___" + rs.getString(2));
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return set;
    }


    public static Timestamp toDate(String basicDate) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Timestamp ts = null;
        try {
            ts = new Timestamp(dateFormat.parse(basicDate).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ts;
    }

    /**
     * 搜集随签列表中的对象
     *
     * @param self
     * @return
     * @throws WTException
     */
    public WTArrayList collect() throws WTException {
        WfProcess wfprocess = null;
        WTArrayList list = new WTArrayList();
        wfprocess = WorkflowUtil.getProcess(this.self);
        ProcessReviewObjectLink link = null;
        if (wfprocess != null) {
            QueryResult queryresult = ProcessReviewObjectLinkHelper.service.getProcessReviewObjectLinkByRoleA(wfprocess);
            while (queryresult != null && queryresult.hasMoreElements()) {
                link = (ProcessReviewObjectLink) queryresult.nextElement();
                WTObject obj = (WTObject) link.getRoleBObject();
                list.add(obj);
            }
        }
        return list;

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
    public static void downContexts(String downPath, ApplicationData data, String name) throws WTException, FileNotFoundException, IOException {

        WTUser user = (WTUser) SessionHelper.manager.getPrincipal();
        System.out.println("FullName=========" + user.getFullName());
        File f = new File(downPath);
        if (!f.exists()) f.mkdir();

        Streamed sd = data != null ? (Streamed) data.getStreamData().getObject() : null;
        if (sd != null) ;
        if (sd.retrieveStream() != null) ;
        BufferedInputStream bis = new BufferedInputStream(sd.retrieveStream());

        //String finalFileName = URLEncoder.encode(name, "GB18030");
        System.out.println("c========" + name);
        File outFile = new File(downPath, name);
        System.out.println("outFile====================" + outFile.getName());
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile));
        System.out.println("bos=====================" + bos);
        byte buff1[] = new byte[1024];
        int read;
        while ((read = bis.read(buff1)) > 0) {
            bos.write(buff1, 0, read);
        }
        bos.flush();
        bis.close();
        bos.close();
    }

    /**
     * 建立jdbc链接
     *
     * @return
     * @throws IOException
     */
    private static Connection getConn() throws IOException {

        Map<String, String> map = DataConfigurationPacketTable.getSendDocDataBeans();
        String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        String[] arrtPort = map.get("dataPort").split("\\.");
        String url = "jdbc:sqlserver://" + map.get("dataAddress") + "\\dbo:" + arrtPort[0] + ";database=" + map.get("dataStorageRoom");
        System.out.println("url================================" + url);
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

    /**
     * 插入物料表
     *
     * @param doc
     * @param name
     * @param part
     * @return
     * @throws WTException
     * @throws IOException
     * @throws ParseException
     * @throws DocumentException
     */
    public static int insertEDFiles(Map<WTPart, String> mapPart, Set<FileAttachListBean> listBean) throws WTException, IOException, DocumentException, ParseException {
        Connection conn = getConn();
        System.out.println("mapPart================" + mapPart);
        int i = 0;
        String sql = "insert into ED_Files (FileNo,FileName,FileTitle,Rev,FileStatus,Proposer,ProposeTime,Approver,ApproveTime,ExtensionName," + "FolderID,Remark,CompanyType,Product,FilePath,FileSize,AttachCount,ApproveResult,FileGuidName,updateDate)" + " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement pstmt = null;
        try {
            for (WTPart part : mapPart.keySet()) {
                UUID u = UUID.randomUUID();


                List<String> listVer = checkVer(part.getNumber());
                if (!listVer.isEmpty()) {
                    WTPart partLa = (WTPart) getLatestObjectByMaster(part.getMaster());
                    String partVersion = partLa.getVersionInfo().getIdentifier().getValue();
                    System.out.println("listVer======" + listVer);
                    for (String rev : listVer) {
                        String[] arryRev = rev.split("\\.");
                        if (!partVersion.equals(arryRev[0]) && !arryRev[0].equals("Rev")) {
                            updateIsinvalid(part.getNumber(), rev);
                        }
                    }
                }
                String partVersion = part.getVersionInfo().getIdentifier().getValue();
                String partIteration = part.getIterationInfo().getIdentifier().getValue();

//		        String filePath = WT_CODEBASE + File.separator + "config" + File.separator + "custom" + File.separator + "ControlledCover.pdf";
//		        File file = new File(filePath);
                String name = u.toString() + ".PDF";
//		        downContextsPDF(file, name);

                File fileCover = new CreateCoverPDF().generatePDFs(name, part);
                System.out.println("SQLServerException==============" + part.getNumber());
                pstmt = (PreparedStatement) conn.prepareStatement(sql);
                pstmt.setString(1, part.getNumber());       //部件编号
                pstmt.setString(2, part.getNumber() + "_" + part.getName() + "_" + partVersion + "." + partIteration + "_" + "封面");                //编号+'_'+版本+'_'+名称
                pstmt.setString(3, part.getName());        //部件名称
                pstmt.setString(4, partVersion + "." + partIteration);                    //版本
//		        State s = State.toState(part.getState().toString());
//		        String partState = s.getDisplay(SessionHelper.getLocale());
                pstmt.setString(5, "2");                           //状态
//		        WTPrincipalReference creator =part.getCreator();
//		        WTPrincipalReference modifier =part.

                System.out.println("part.getCreator().getIdentity().toString()================================" + part.getCreator().getIdentity().toString());
                System.out.println("part.getModifier().getIdentity()============================" + part.getModifier().getIdentity());
                System.out.println("part.getCreator()======================================" + part.getCreator().toString());
                System.out.println("part.getCreatorEMail()=================================" + part.getCreatorEMail());
                System.out.println("part.getModifierEMail()====================================" + part.getModifierEMail().toString());
                System.out.println("part.getModifier()======================================" + part.getModifier().toString());
                pstmt.setString(6, part.getCreatorFullName());                     //创建者
                SimpleDateFormat sdf = new SimpleDateFormat(PdfUtil.SIMPLE_DATE_FORMAT);
                Timestamp createData = part.getPersistInfo().getCreateStamp();
                pstmt.setString(7, sdf.format(createData));                         //创建时间
                pstmt.setString(8, part.getModifierFullName());    //修改者
                Timestamp updateData = part.getPersistInfo().getModifyStamp();
                pstmt.setString(9, sdf.format(updateData));                              //修改时间
                pstmt.setString(10, ".PDF");
                String folderId = checkFolderId(part);
                System.out.println("folderId================" + folderId);
                if (!folderId.equals("")) {
                    pstmt.setString(11, folderId);
                } else {
                    pstmt.setString(11, checkFolderId());
                }
                pstmt.setString(12, "");
                pstmt.setString(13, "");
                pstmt.setString(14, "");
                pstmt.setString(15, "~/@Upload/Attach/");
                List<String> count = new ArrayList<String>();
                for (FileAttachListBean bean : listBean) {
                    if (bean.getUerNo().equals(part.getNumber())) {
                        count.add(part.getNumber());
                    }
                }
                pstmt.setString(16, String.valueOf(fileCover.length()));
                pstmt.setString(17, String.valueOf(count.size()));
                pstmt.setString(18, "1");
                pstmt.setString(19, u.toString());

                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                pstmt.setString(20, sdf2.format(new Date()));
                i = pstmt.executeUpdate();

                for (FileAttachListBean bean : listBean) {
                    if (bean.getUerNo().equals(part.getNumber())) {
                        String seqNo = checkNo(bean.getUerNo());
                        insertFileAttachList(bean, seqNo, part);
                    }
                }

                System.out.println("pstmt=======================================" + pstmt);
                System.out.println("SqlSize================================================" + i);
            }
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return i;
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
    public static void downContextsPDF(File file, String name) throws WTException, FileNotFoundException, IOException {

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        Map<String, String> mapPath = DataConfigurationPacketTable.getSendDocDataBeans();
        String downPath = mapPath.get("mappingPath");
        //String finalFileName = URLEncoder.encode(name, "GB18030");
        System.out.println("c========" + name);
        File outFile = new File(downPath, name);
        System.out.println("outFile====================" + outFile.getName());
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile));
        System.out.println("bos=====================" + bos);
        byte buff1[] = new byte[1024];
        int read;
        while ((read = bis.read(buff1)) > 0) {
            bos.write(buff1, 0, read);
        }
        bos.flush();
        bis.close();
        bos.close();
    }

    public static List<String> checkVer(String partNumber) throws IOException {
        List<String> list = new ArrayList<>();
        String ver = "";
        PreparedStatement pstmt;
        try {
            Connection conn = getConn();

            String sql = "SELECT Rev FROM ED_Files WHERE FileNo=?";
            System.out.println("RevConn==" + conn);
            pstmt = (PreparedStatement) conn.prepareStatement(sql);
            pstmt.setString(1, partNumber);
            System.out.println("RevPstmt===" + pstmt);
            ResultSet rs = pstmt.executeQuery();
            //5.处理ResultSet
            while (rs.next()) {
                //rs.get+数据库中对应的类型+(数据库中对应的列别名)
                ver = rs.getString(1);
                list.add(ver);
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    public static int updateIsinvalid(String partNumber, String ver) throws IOException, WTException {
        Connection conn = getConn();
        System.out.println("updateIsinvalidConn================" + conn);
        int i = 0;
        String sql = "update ED_Files set isinvalid=?,folderId=? where FileNo=? and Rev=?";
        PreparedStatement pstmt;
        try {
            pstmt = (PreparedStatement) conn.prepareStatement(sql);
            pstmt.setString(1, "1");
            pstmt.setString(2, checkVoidFolderId());
            pstmt.setString(3, partNumber);
            pstmt.setString(4, ver);
            i = pstmt.executeUpdate();
            System.out.println("updateIsinvalidPstmt=======================================" + pstmt);
            System.out.println("updateIsinvalidSqlSize================================================" + i);
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return i;
    }

    public static String checkVoidFolderId() throws IOException, WTException {
        String folderId = "";
        PreparedStatement pstmt;
        try {
            Connection conn = getConn();
            System.out.println("checkFolderId=====================" + conn);
//		            String sql = "select folderId from ED_Folder where  dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\') ='企业空间\\PLM\\影院Barco\\图纸\\结构图纸'";
            String sql = "select folderId from ED_Folder where  dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\') = '企业空间\\I 作废保留文件--旧版\\PLM\\原材料库'";

            System.out.println("sql=========================" + sql);
            pstmt = (PreparedStatement) conn.prepareStatement(sql);
            System.out.println("checkFolderId============pstmt=========" + pstmt);
            ResultSet rs = pstmt.executeQuery();
            System.out.println("checkFolderId============rs=========" + rs);
            //5.处理ResultSet
            while (rs.next()) {
                //rs.get+数据库中对应的类型+(数据库中对应的列别名)
                folderId = rs.getString(1);

            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return folderId;
    }


    /**
     * 插入附件表
     *
     * @param doc
     * @param name
     * @param part
     * @return
     * @throws WTException
     * @throws IOException
     */
    public static int insertFileAttachList(FileAttachListBean bean, String seqNo, WTPart part) throws WTException, IOException {
        Connection conn = getConn();
        System.out.println("conn================" + conn);
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
            System.out.println("pstmt=======================================" + pstmt);
            System.out.println("SqlSize================================================" + i);
            pstmt.close();
            conn.close();

            IBAUtil.forceSetIBAValue(part, "SendDccState", "成功");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return i;
    }

    public static String checkNo(String partNumber) throws IOException {
        String id = "";
        PreparedStatement pstmt;
        try {
            Connection conn = getConn();

            String sql = "SELECT Max(ID) FROM ED_Files WHERE FileNo=?";
            pstmt = (PreparedStatement) conn.prepareStatement(sql);
            pstmt.setString(1, partNumber);
            ResultSet rs = pstmt.executeQuery();
            //5.处理ResultSet
            while (rs.next()) {
                //rs.get+数据库中对应的类型+(数据库中对应的列别名)
                id = rs.getString(1);

            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public static String checkFolderId(WTPart part) throws IOException, WTException {
        String folderId = "";
        PreparedStatement pstmt;
        try {
            Connection conn = getConn();
            System.out.println("checkFolderId=====================" + conn);
//		            String sql = "select folderId from ED_Folder where  dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\') ='企业空间\\PLM\\影院Barco\\图纸\\结构图纸'";
            String sql = "";
            String classification = (String) PdfUtil.getIBAObjectValue(part, "Classification");
            System.out.println("classification======================" + classification);
            if (classification.contains("appo_rj")) {
//		        	    String type = UploadAttachmentUtil.getTypeInternalName(part);
                String sscpx = (String) PdfUtil.getIBAObjectValue(part, "sscpx");
                System.out.println("sscpx=============================" + sscpx);
                String productCategory = getEnumerationDisplayName("sscpx", sscpx);
                System.out.println("productCategory================================" + productCategory);
                sql = "select folderId from ED_Folder where  dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\') = '企业空间\\PLM\\" + productCategory + "\\产品软件'";
            } else {
                sql = "select folderId from ED_Folder where  dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\') = '企业空间\\PLM\\原材料库'";
            }
            System.out.println("sql=========================" + sql);
            pstmt = (PreparedStatement) conn.prepareStatement(sql);
            System.out.println("checkFolderId============pstmt=========" + pstmt);
            ResultSet rs = pstmt.executeQuery();
            System.out.println("checkFolderId============rs=========" + rs);
            //5.处理ResultSet
            while (rs.next()) {
                //rs.get+数据库中对应的类型+(数据库中对应的列别名)
                folderId = rs.getString(1);

            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return folderId;
    }

    public static String checkFolderId() throws IOException, WTException {
        String folderId = "";
        PreparedStatement pstmt;
        try {
            Connection conn = getConn();
            System.out.println("checkFolderId=====================" + conn);
//		            String sql = "select folderId from ED_Folder where  dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\') ='企业空间\\PLM\\影院Barco\\图纸\\结构图纸'";
            String sql = "select folderId from ED_Folder where  dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\') = '企业空间\\PLM\\原材料库'";

            System.out.println("sql=========================" + sql);
            pstmt = (PreparedStatement) conn.prepareStatement(sql);
            System.out.println("checkFolderId============pstmt=========" + pstmt);
            ResultSet rs = pstmt.executeQuery();
            System.out.println("checkFolderId============rs=========" + rs);
            //5.处理ResultSet
            while (rs.next()) {
                //rs.get+数据库中对应的类型+(数据库中对应的列别名)
                folderId = rs.getString(1);

            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return folderId;
    }

    /**
     * 取属性枚举值显示名称
     *
     * @param key
     * @param value
     * @return
     */
    public static String getEnumerationDisplayName(String Attrkey, String value) {
        String displayName = "";
        try {
            EnumerationDefinitionReadView edr = TypeDefinitionServiceHelper.service.getEnumDefView(Attrkey);
            if (edr != null) {
                Map<String, EnumerationEntryReadView> views = edr.getAllEnumerationEntries();
                if (views != null) {
                    Set<String> keysOfView = views.keySet();
                    List<String> keysList = new ArrayList<String>();
                    keysList.addAll(keysOfView);
                    // Collections.sort(keysList);
                    for (String key : keysList) {
                        EnumerationEntryReadView view = views.get(key);
                        String name = view.getName();
                        displayName = PropertyHolderHelper.getDisplayName(view, SessionHelper.getLocale());
                        if (value != null && value.equals(name)) {
                            return displayName;
                        }
                    }
                }
            }
        } catch (NotAuthorizedException e) {
            e.printStackTrace();
        } catch (WTContainerException e) {
            e.printStackTrace();
        } catch (WTException e) {
            e.printStackTrace();
        }

        return value;
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

    /**
     * 是否发送成功
     *
     * @throws WTException
     * @throws IOException
     * @throws PropertyVetoException
     */
    public boolean isSendDataSuccess() throws WTException, IOException {

        boolean isSuccess = true;

        WTArrayList collect = collect();
        for (int i = 0; i < collect.size(); i++) {
            Persistable persistable = collect.getPersistable(i);
            if (persistable instanceof WTPart) {

                WTPart part = (WTPart) persistable;
                List<String> listFolderId = isFolderId(part.getNumber());
                System.out.println("isFolderId===================================" + listFolderId);
                IBAUtil.forceSetIBAValue(part, "SendDccState", "成功");
                String fileid = isSuccessFiles(part.getNumber());
                if (fileid != null && fileid != "") {
                    String arryFileid[] = fileid.split("\\_");
                    List<String> listFileid = isSuccessFileAttachList(arryFileid[0]);
                    if (listFileid.isEmpty()) {
                        int fileidSize = Integer.parseInt(arryFileid[1]);
                        if (fileidSize != listFileid.size()) {
                            IBAUtil.forceSetIBAValue(part, "SendDccState", "失败(发送数量不对。)");
                            return false;
                        }
                    }
                }

                for (String folderId : listFolderId) {
                    if (folderId.equals("0")) {
                        IBAUtil.forceSetIBAValue(part, "SendDccState", "失败(当前或作废版本未能找到作废路径，请检查)");
                        return false;
                    }
                }

            }
        }

        WTArrayList collectDoc = collect();
        for (int i = 0; i < collectDoc.size(); i++) {
            Persistable persistable = collectDoc.getPersistable(i);
            if (persistable instanceof WTDocument) {
                WTDocument doc = (WTDocument) persistable;
                List<String> listFolderId = isFolderId(doc.getNumber());
                System.out.println("isFolderId===================================" + listFolderId);
                IBAUtil.forceSetIBAValue(doc, "SendDccState", "成功");
                String fileid = isSuccessFiles(doc.getNumber());
                if (fileid != null && fileid != "") {
                    String arryFileid[] = fileid.split("\\_");
                    List<String> listFileid = isSuccessFileAttachList(arryFileid[0]);
                    if (listFileid.isEmpty()) {
                        int fileidSize = Integer.parseInt(arryFileid[1]);
                        if (fileidSize != listFileid.size()) {
                            IBAUtil.forceSetIBAValue(doc, "SendDccState", "失败(发送数量不对。)");
                            return false;
                        }
                    }
                }

                for (String folderId : listFolderId) {
                    if (folderId.equals("0")) {
                        IBAUtil.forceSetIBAValue(doc, "SendDccState", "失败(当前或作废版本未能找到作废路径，请检查)");
                        return false;
                    }
                }

            }
        }

        return isSuccess;
    }

    public static String isSuccessFiles(String partNumber) throws IOException {
        String files = "";

        PreparedStatement pstmt;
        try {
            Connection conn = getConn();

            String sql = "SELECT Max(ID),AttachCount FROM ED_Files WHERE FileNo=? and isinvalid=0 group by AttachCount";
            pstmt = (PreparedStatement) conn.prepareStatement(sql);
            pstmt.setString(1, partNumber);
            ResultSet rs = pstmt.executeQuery();
            //5.处理ResultSet
            while (rs.next()) {
                //rs.get+数据库中对应的类型+(数据库中对应的列别名)
                String id = rs.getString(1);
                String count = rs.getString(2);
                files = id + "_" + count;
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return files;
    }

    public static List<String> isSuccessFileAttachList(String fileid) throws IOException {

        List<String> list = new ArrayList<>();
        String id = "";
        PreparedStatement pstmt;
        try {
            Connection conn = getConn();

            String sql = "SELECT id FROM ED_FileAttachList WHERE FileID=?";
            pstmt = (PreparedStatement) conn.prepareStatement(sql);
            pstmt.setString(1, fileid);
            ResultSet rs = pstmt.executeQuery();
            //5.处理ResultSet
            while (rs.next()) {
                //rs.get+数据库中对应的类型+(数据库中对应的列别名)
                id = rs.getString(1);
                list.add(id);
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<String> isFolderId(String partNumber) throws IOException {
        String files = "";

        List<String> list = new ArrayList<>();
        PreparedStatement pstmt;
        try {
            Connection conn = getConn();

            String sql = "SELECT folderId FROM ED_Files WHERE FileNo=?";
            pstmt = (PreparedStatement) conn.prepareStatement(sql);
            pstmt.setString(1, partNumber);
            ResultSet rs = pstmt.executeQuery();
            //5.处理ResultSet
            while (rs.next()) {
                String folderId = rs.getString(1);
                files = folderId;
                list.add(folderId);
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

}
