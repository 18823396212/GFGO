package ext.appo.doc.uploadDoc;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.lowagie.text.DocumentException;
import com.ptc.core.lwc.common.view.AttributeDefinitionReadView;
import com.ptc.core.lwc.common.view.ConstraintDefinitionReadView;
import com.ptc.core.lwc.common.view.ConstraintRuleDefinitionReadView;
import com.ptc.core.lwc.common.view.DatatypeReadView;
import com.ptc.core.lwc.common.view.EnumerationEntryReadView;
import com.ptc.core.lwc.common.view.TypeDefinitionReadView;
import com.ptc.core.lwc.common.view.ConstraintDefinitionReadView.RuleDataObject;
import com.ptc.core.lwc.server.TypeDefinitionServiceHelper;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.type.mgmt.server.impl.WTTypeDefinitionMaster;
import com.ptc.windchill.enterprise.part.commands.PartDocServiceCommand;

import ext.appo.ecn.pdf.PdfUtil;
import ext.com.iba.IBAUtil;
import ext.generic.workflow.WorkFlowBase;
import wt.doc.WTDocument;
import wt.enterprise.Master;
import wt.epm.EPMDocument;
import wt.fc.ObjectReference;
import wt.fc.ObjectVector;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.org.WTPrincipal;
import wt.part.WTPart;
import wt.query.ClassAttribute;
import wt.query.OrderBy;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.type.TypedUtility;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.vc.VersionControlHelper;
import wt.vc.wip.WorkInProgressHelper;

public class DocUploadAttachmentYLContent extends WorkFlowBase{
	
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
	
	public DocUploadAttachmentYLContent() {
	}
	

	public DocUploadAttachmentYLContent(WTObject pbo, ObjectReference self) {
		if (pbo != null && self != null) {
			this.pbo = pbo;
			this.self = self;
		}
	}
  

	/**
	 * 建立jdbc链接
	 * @return
	 * @throws IOException 
	 */
	private static Connection getConn() throws IOException {
		
		Map<String,String> map = DataConfigurationPacketTable.getYLSendDocDataBeans();
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
	
	/**
	 * 插入文档表
	 * @param doc
	 * @param name
	 * @param part
	 * @return
	 * @throws WTException
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws DocumentException 
	 */
	public static  int insertEDFiles(Map<WTDocument,String> mapDoc,Set<FileAttachListYLBean> listBean) throws WTException, IOException, DocumentException, ParseException {
	    Connection conn = getConn();
	    System.out.println("mapDoc================"+mapDoc);
	    int i = 0;
	    String sql = "insert into ED_Files (FileNo,FileName,FileTitle,Rev,FileStatus,Proposer,ProposeTime,Approver,ApproveTime,ExtensionName,"
	    		+ "FolderID,Remark,CompanyType,Product,FilePath,FileSize,AttachCount,ApproveResult,FileGuidName,updateDate)"
	    		+ " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	    PreparedStatement pstmt = null;
	    try {
	    	for(WTDocument doc: mapDoc.keySet()) {		
	    	UUID u =  UUID.randomUUID();		 
	        
	        
	        List<String> listVer = UploadAttachmentYLContent.checkVer(doc.getNumber());
	        if(!listVer.isEmpty()){
	        WTDocument docLa = (WTDocument)getLatestVersionByMaster((Master)doc.getMaster());
	        String partVersion = docLa.getVersionInfo().getIdentifier().getValue();
	        System.out.println("listVer======"+listVer);
	        for(String rev : listVer){
	        	String[] arryRev = rev.split("\\.");
	        	if(!partVersion.equals(arryRev[0]) && !arryRev[0].equals("Rev")){
	        		UploadAttachmentYLContent.updateIsinvalid(doc.getNumber(), rev);
	        	}
	        }
	        }
	        String partVersion = doc.getVersionInfo().getIdentifier().getValue();
	        String partIteration = doc.getIterationInfo().getIdentifier().getValue();
	        
//	        String filePath = WT_CODEBASE + File.separator + "config" + File.separator + "custom" + File.separator + "ControlledCover.pdf";
//	        File file = new File(filePath);
	        String name = u.toString()+".PDF";
//	        UploadAttachmentContent.downContextsPDF(file, name);
	        File fileCover = new DocCreateCoverPDF().generatePDFs(name,doc);
	        
	        pstmt = (PreparedStatement) conn.prepareStatement(sql);
	        pstmt.setString(1, doc.getNumber());       //部件编号                                     
	        pstmt.setString(2, doc.getNumber()+"_"+doc.getName()+"_"+partVersion+"."+partIteration+"_"+"封面");                //编号+'_'+版本+'_'+名称
	        pstmt.setString(3, doc.getName());        //部件名称
	        pstmt.setString(4,  partVersion+"."+partIteration);                    //版本
//	        State s = State.toState(part.getState().toString());
//	        String partState = s.getDisplay(SessionHelper.getLocale());
	        pstmt.setString(5,  "2");                           //状态
	        pstmt.setString(6, doc.getCreatorFullName());                     //创建者		        
	        SimpleDateFormat sdf = new SimpleDateFormat(PdfUtil.SIMPLE_DATE_FORMAT);
	        Timestamp createData =  doc.getPersistInfo().getCreateStamp();        
	        pstmt.setString(7,sdf.format(createData));                         //创建时间
	        pstmt.setString(8,doc.getModifierFullName());    //修改者
	        Timestamp updateData = doc.getPersistInfo().getModifyStamp();
	        pstmt.setString(9,sdf.format(updateData));                              //修改时间
	        pstmt.setString(10, ".PDF");                         
	        String folderId = checkFolderId(doc,mapDoc.get(doc));
	        
//	        System.out.println("folderId================================="+folderId);
//	        if(!folderId.equals("")){
//	          pstmt.setString(11, folderId);   
//	        }else{
	          pstmt.setString(11, folderId); 
//	        }
	        pstmt.setString(12, "");           
	        pstmt.setString(13,"");               
	        pstmt.setString(14, "");
	        pstmt.setString(15, "~/@Upload/Attach/");
	        List<String> count = new ArrayList<String>();
	        for(FileAttachListYLBean bean : listBean){
				if(bean.getUerNo().equals(doc.getNumber())){
					count.add(doc.getNumber());
				}
			}
	        pstmt.setString(16,  String.valueOf(fileCover.length())); 
	        pstmt.setString(17,  String.valueOf(count.size())); 
	        pstmt.setString(18,  "1"); 
	        pstmt.setString(19,  u.toString()); 
	        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        pstmt.setString(20, sdf2.format(new Date()));
	        i = pstmt.executeUpdate();
	        
	        for(FileAttachListYLBean bean : listBean){
				if(bean.getUerNo().equals(doc.getNumber())){
					String seqNo = UploadAttachmentYLContent.checkNo(bean.getUerNo());
					insertFileAttachList(bean,seqNo,doc);
				}
			}
	        
	        System.out.println("pstmt======================================="+pstmt);
	        System.out.println("SqlSize================================================"+i);
	      }
	    	pstmt.close();
	        conn.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return i;
	}
	
	/**
     * 根据类型的内部名称，获取显现名称
     * @param internalName
     * @return
     * @throws WTException
     */
    public static String getTypeDisplayNameByInternalName(String internalName) throws WTException{
    	WTPrincipal curUser = SessionHelper.manager.getPrincipal();
        SessionHelper.manager.setAdministrator();
        try{
        	TypeIdentifier type = TypedUtility.getTypeIdentifier(internalName);
        	TypeDefinitionReadView trv = TypeDefinitionServiceHelper.service.getTypeDefView(type);
        	return trv.getDisplayName();
        } finally {
            SessionHelper.manager.setPrincipal(curUser.getName());
        }
    }
	
	
	 public static String checkFolderId(WTDocument doc,String types) throws IOException, WTException{
         String folderId = "";
		  PreparedStatement pstmt;
	        try {
	        	 Connection conn = getConn();
	        	 System.out.println("checkFolderId====================="+conn);    
                 String sql = "";
                 String type = UploadAttachmentUtil.getTypeInternalName(doc);
                 System.out.println("typesss========================"+type);
				  if(type.equals("com.plm.drawingdoc")) {         //图纸发
					  sql = "select folderId from ED_Folder where  dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\') = '企业空间\\PLM\\原材料库'";
				  }else if(type.equals("com.plm.datasheet")) {   //物料规格书
					  sql = "select folderId from ED_Folder where  dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\') = '企业空间\\PLM\\物料规格书'";
				  }else if(type.equals("com.plm.softwaredoc")){    //软件                           
//					  String sscpx = (String)PdfUtil.getIBAObjectValue(doc, "sscpx");
//					  String productCategory = getAllSofttypeByDocByEnum(type, "sscpx", sscpx);
                      sql = "select folderId from ED_Folder where  dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\') = '企业空间\\PLM\\舞台灯\\产品软件'";
				  }else {
					  String sscpx = (String)PdfUtil.getIBAObjectValue(doc, "sscpx");
					  System.out.println("sscpx=================="+sscpx);
					  String smallDocType = (String)PdfUtil.getIBAObjectValue(doc, "SmallDocType");
					  String productCategory = getAllSofttypeByDocByEnum(type, "sscpx", sscpx);
					  System.out.println("productCategory=========================="+productCategory);
					  System.out.println("getTypeDisplayNameByInternalName====================="+getTypeDisplayNameByInternalName(type));
					  System.out.println("smallDocType==================================="+smallDocType);
                      sql = "select folderId from ED_Folder where  dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\') = '企业空间\\PLM\\舞台灯\\"+smallDocType+"'";
				  }
	            
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
		        e.printStackTrace();
		    }
	        return folderId;
	    }
	 
	 public static String checkStorehouse() throws IOException, WTException{
         String folderId = "";
		  PreparedStatement pstmt;
	        try {
	        	 Connection conn = getConn();
	        	 System.out.println("checkFolderId====================="+conn);    
                 String sql = "";
			     sql = "select folderId from ED_Folder where  dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\') = '企业空间\\PLM\\原材料库'";
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
		        e.printStackTrace();
		    }
	        return folderId;
	    }
	 
	 /**
	     * 取枚举的显示名称
	     * @param topType
	     * @param ibaConst
	     * @return
	     * @throws WTException
	     */
		public static String getAllSofttypeByDocByEnum(String topType,String ibaConst,String enumConst) throws WTException {
			String intHid = "";
			QuerySpec qs = new QuerySpec(WTTypeDefinitionMaster.class);
			int iIndex = qs.getFromClause().getPosition(WTTypeDefinitionMaster.class);
			SearchCondition sc = new SearchCondition(WTTypeDefinitionMaster.class, WTTypeDefinitionMaster.INT_HID, SearchCondition.LIKE, "%" + topType + "%");
			qs.appendWhere(sc, new int[iIndex]);
			qs.appendAnd();
			sc = new SearchCondition(WTTypeDefinitionMaster.class, WTTypeDefinitionMaster.DELETED_ID, true);// WTTypeDefinitionMaster.DELETED_ID"deleted_id"
			qs.appendWhere(sc, new int[iIndex]);
		
			ClassAttribute idAttr = new ClassAttribute(WTTypeDefinitionMaster.class, WTTypeDefinitionMaster.INT_HID);
			OrderBy orderbyId = new OrderBy(idAttr, false);
			int ordIndex[] = { iIndex };
			qs.appendOrderBy(orderbyId, ordIndex);
			
			QueryResult qr = PersistenceHelper.manager.find(qs);
			while (qr.hasMoreElements()) {
				WTTypeDefinitionMaster wtm = (WTTypeDefinitionMaster) qr.nextElement();
				intHid = wtm.getIntHid();
				if (intHid.indexOf("|")>-1) {
					String[] softTypes = intHid.split("\\|");
					String type = softTypes[softTypes.length -1];
					TypeIdentifier ti = TypedUtility.getTypeIdentifier(type);
		          	TypeDefinitionReadView tv = TypeDefinitionServiceHelper.service.getTypeDefView(ti);
		          	Collection<AttributeDefinitionReadView> attrs = tv.getAllAttributes();
		          	for (AttributeDefinitionReadView av : attrs) {
		          		if (av.getName().equals(ibaConst)) {
		          			return getAttrEnumMap(av).get(enumConst);
		          		}
		          	}
				}
			}
			return "";
		}
		
		 /**
	     * 获取属性定义中的枚举Map
	     * @param av
	     * @return
	     */
	    public static HashMap<String,String> getAttrEnumMap(AttributeDefinitionReadView av) {
	    	HashMap<String,String> enumMap = new HashMap<String,String>();
	        Collection<ConstraintDefinitionReadView> constraints = av.getAllConstraints();
	        for (ConstraintDefinitionReadView constraint : constraints) {
	            ConstraintRuleDefinitionReadView crdrv = constraint.getRule();
	            DatatypeReadView datatypeReadView = crdrv.getDatatype();
	            String datatype = datatypeReadView.getName();
	            String rule = constraint.getRule().getKey().toString();
	            // 枚举约束
	            if (rule.indexOf("com.ptc.core.lwc.server.LWCEnumerationBasedConstraint") > -1) {
	               
	                RuleDataObject rdo = constraint.getRuleDataObj();
	                if (rdo != null) {
	                    Collection coll = rdo.getEnumDef().getAllEnumerationEntries().values();
	                    Iterator<EnumerationEntryReadView> it = coll.iterator();
	                    while (it.hasNext()) {
	                        EnumerationEntryReadView view = it.next();
	                        String enumKey = view.getName();
	                        String enumDisplayName = view.getPropertyValueByName("displayName").getValue().toString();
	                        enumMap.put(enumKey, enumDisplayName);
	                    }
	                }
	            }
	        }
	        return enumMap;
	    }
	
	public static Persistable getLatestVersionByMaster(Master master) {
        try {
            if (master != null) {
                QueryResult qrVersions = allVersionsOf(master);
                if (qrVersions.hasMoreElements()) {
                    Persistable p = (Persistable) qrVersions.nextElement();
                    return p;
                }
            }
        } catch (QueryException e) {
            e.printStackTrace();
        } catch (WTException e) {
            e.printStackTrace();
        }
        return null;
    }
	
	public static QueryResult allVersionsOf(Master master) throws WTException {
        QueryResult qr = new QueryResult();
        ;
        try {
            if (master != null) {
                QueryResult qrVersions = VersionControlHelper.service.allVersionsOf(master);
                List<Persistable> list = new ArrayList<Persistable>();
                while (qrVersions.hasMoreElements()) {
                    Persistable p = (Persistable) qrVersions.nextElement();
                    if (p instanceof EPMDocument) {
                        EPMDocument epm = (EPMDocument) p;
                        if (WorkInProgressHelper.isPrivateWorkingCopy(epm)) {
                        } else {
                            list.add(p);
                        }
                    } else {
                        list.add(p);
                    }
                }

                if (list.size() > 0) {
                    ObjectVector ovi = new ObjectVector();
                    for (Persistable p : list) {
                        ovi.addElement(p);
                    }
                    qr.appendObjectVector(ovi);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e);
        }
        return qr;
    }
	
	
	 /**
     * 通过文档找相关联的部件
     */
    public static List<WTPart> getAllPartByDoc(WTDocument doc){
    	List<WTPart> partList = new ArrayList<WTPart>();
		try {
	        QueryResult qr = PartDocServiceCommand.getAssociatedDescParts(doc);
	    	while(qr.hasMoreElements()){
	    		Object obj = qr.nextElement();
	    		if(obj instanceof WTPart){
	    			partList.add((WTPart)obj);
	    		}
	    	}
	        
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return partList;
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
	public static  int insertFileAttachList(FileAttachListYLBean bean,String seqNo,WTDocument doc) throws WTException, IOException {
	    Connection conn = getConn();
	    System.out.println("conn================"+conn);
	    int i = 0;
	    String sql = "insert into ED_FileAttachList (FileID,AttachName,GuidName,ExtensionName,UserNo,FileSize)values(?,?,?,?,?,?)";
	    PreparedStatement pstmt;
	    try {       
	        pstmt = (PreparedStatement) conn.prepareStatement(sql);
	        pstmt.setString(1, seqNo);
	        System.out.println("seqNo=============================="+seqNo);
	        pstmt.setString(2, bean.getAttachName());        //文件名
	        System.out.println("bean.getAttachName()=============================="+bean.getAttachName());
	        pstmt.setString(3, bean.getGuidName());     //UUID
	        System.out.println("bean.getGuidName()=============================="+bean.getGuidName());
	        pstmt.setString(4, bean.getExtensionName());  //后缀
	        System.out.println("bean.getExtensionName()=============================="+bean.getExtensionName());
	        pstmt.setString(5, bean.getUerNo());          //料号            
	        pstmt.setString(6, bean.getFileSize());                     //文件大小		        
	        i = pstmt.executeUpdate();
//	        System.out.println("pstmt======================================="+pstmt);
//	        System.out.println("SqlSize================================================"+i);
	        pstmt.close();
	        conn.close();
	        
	        IBAUtil.forceSetIBAValue(doc, "SendDccState", "成功");
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return i;
	}
	
}
