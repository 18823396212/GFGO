package ext.appo.doc.uploadDoc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.lowagie.text.DocumentException;
import com.ptc.core.lwc.common.view.EnumerationDefinitionReadView;
import com.ptc.core.lwc.common.view.EnumerationEntryReadView;
import com.ptc.core.lwc.common.view.PropertyHolderHelper;
import com.ptc.core.lwc.server.TypeDefinitionServiceHelper;

import ext.appo.ecn.pdf.PdfUtil;
import ext.com.iba.IBAUtil;
import wt.access.NotAuthorizedException;
import wt.content.ApplicationData;
import wt.content.Streamed;
import wt.enterprise.Master;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.inf.container.WTContainerException;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.query.QueryException;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.vc.OneOffVersioned;
import wt.vc.VersionControlHelper;

public class UploadAttachmentYLContent {

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
	public static int insertEDFiles(Map<WTPart, String> mapPart, Set<FileAttachListYLBean> listBean)
			throws WTException, IOException, DocumentException, ParseException {
		Connection conn = getConn();
		System.out.println("mapPart================" + mapPart);
		int i = 0;
		String sql = "insert into ED_Files (FileNo,FileName,FileTitle,Rev,FileStatus,Proposer,ProposeTime,Approver,ApproveTime,ExtensionName,"
				+ "FolderID,Remark,CompanyType,Product,FilePath,FileSize,AttachCount,ApproveResult,FileGuidName,updateDate)"
				+ " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
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

				// String filePath = WT_CODEBASE + File.separator + "config" +
				// File.separator + "custom" + File.separator +
				// "ControlledCover.pdf";
				// File file = new File(filePath);
				String name = u.toString() + ".PDF";
				// downContextsPDF(file, name);

				File fileCover = new CreateCoverPDF().generateYLPDFs(name, part);
				System.out.println("SQLServerException==============" + part.getNumber());
				pstmt = (PreparedStatement) conn.prepareStatement(sql);
				pstmt.setString(1, part.getNumber()); // 变更过
				pstmt.setString(2,
						part.getNumber() + "_" + part.getName() + "_" + partVersion + "." + partIteration + "_" + "封面"); // 编号+'_'+版本+'_'+名称
				pstmt.setString(3, part.getName()); // 部件名称
				pstmt.setString(4, partVersion + "." + partIteration); // 版本
				// State s = State.toState(part.getState().toString());
				// String partState = s.getDisplay(SessionHelper.getLocale());
				pstmt.setString(5, "2"); // 状态
				pstmt.setString(6, part.getCreatorFullName()); // 创建者
				SimpleDateFormat sdf = new SimpleDateFormat(PdfUtil.SIMPLE_DATE_FORMAT);
				Timestamp createData = part.getPersistInfo().getCreateStamp();
				pstmt.setString(7, sdf.format(createData)); // 创建时间
				pstmt.setString(8, part.getModifierFullName()); // 修改者
				Timestamp updateData = part.getPersistInfo().getModifyStamp();
				pstmt.setString(9, sdf.format(updateData)); // 修改时间
				pstmt.setString(10, ".PDF");
				String folderId = checkFolderId(part, mapPart.get(part));
				// System.out.println("folderId================"+folderId);
				// if(!folderId.equals("")){
				// pstmt.setString(11, folderId);
				// }else {
				pstmt.setString(11, folderId);
				// }
				pstmt.setString(12, "");
				pstmt.setString(13, "");
				pstmt.setString(14, "");
				pstmt.setString(15, "~/@Upload/Attach/");
				List<String> count = new ArrayList<String>();
				for (FileAttachListYLBean bean : listBean) {
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

				for (FileAttachListYLBean bean : listBean) {
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
	 * 建立jdbc链接
	 * 
	 * @return
	 * @throws IOException
	 */
	private static Connection getConn() throws IOException {

		Map<String, String> map = DataConfigurationPacketTable.getYLSendDocDataBeans();
		String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		String[] arrtPort = map.get("dataPort").split("\\.");
		String url = "jdbc:sqlserver://" + map.get("dataAddress") + "\\dbo:" + arrtPort[0] + ";database="
				+ map.get("dataStorageRoom");
		System.out.println("url================================" + url);
		String username = map.get("userName");
		String password = map.get("password");
		Connection conn = null;
		try {
			Class.forName(driver); // classLoader,加载对应驱动
			conn = (Connection) DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
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
				// System.out.println("arry[]======================================="+arry);
				// System.out.println("arry[0]======================================="+arry[0]);
				// System.out.println("arry[1]======================================="+arry[1]);
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
			// System.out.println("checkFolderId_________====================="+conn);
			String sql = "select folderid,count(*) from ED_Files  GROUP BY folderid";

			// System.out.println("sql___________========================="+sql);
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			// System.out.println("checkFolderId___________============pstmt========="+pstmt);
			ResultSet rs = pstmt.executeQuery();
			// System.out.println("checkFolderId___________============rs========="+rs);
			// 5.处理ResultSet
			while (rs.next()) {
				// rs.get+数据库中对应的类型+(数据库中对应的列别名)
				set.add(rs.getString(1) + "___" + rs.getString(2));
				// System.out.println("set____=================================================");
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
	 * 下载方法
	 * 
	 * @param downPath
	 * @param data
	 * @param name
	 * @throws WTException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void downContexts(String downPath, ApplicationData data, String name)
			throws WTException, FileNotFoundException, IOException {

		WTUser user = (WTUser) SessionHelper.manager.getPrincipal();
		System.out.println("FullName=========" + user.getFullName());
		File f = new File(downPath);
		if (!f.exists())
			f.mkdir();

		Streamed sd = data != null ? (Streamed) data.getStreamData().getObject() : null;
		if (sd != null)
			;
		if (sd.retrieveStream() != null)
			;
		BufferedInputStream bis = new BufferedInputStream(sd.retrieveStream());

		// String finalFileName = URLEncoder.encode(name, "GB18030");
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
		Map<String, String> mapPath = DataConfigurationPacketTable.getYLSendDocDataBeans();
		String downPath = mapPath.get("mappingPath");
		// String finalFileName = URLEncoder.encode(name, "GB18030");
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
			// 5.处理ResultSet
			while (rs.next()) {
				// rs.get+数据库中对应的类型+(数据库中对应的列别名)
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
			// String sql = "select folderId from ED_Folder where
			// dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\')
			// ='企业空间\\PLM\\影院Barco\\图纸\\结构图纸'";
			String sql = "select folderId from ED_Folder where  dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\') = '企业空间\\I 作废保留文件--旧版\\PLM\\原材料库'";

			System.out.println("sql=========================" + sql);
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			System.out.println("checkFolderId============pstmt=========" + pstmt);
			ResultSet rs = pstmt.executeQuery();
			System.out.println("checkFolderId============rs=========" + rs);
			// 5.处理ResultSet
			while (rs.next()) {
				// rs.get+数据库中对应的类型+(数据库中对应的列别名)
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
	public static int insertFileAttachList(FileAttachListYLBean bean, String seqNo, WTPart part)
			throws WTException, IOException {
		Connection conn = getConn();
		System.out.println("conn================" + conn);
		int i = 0;
		String sql = "insert into ED_FileAttachList (FileID,AttachName,GuidName,ExtensionName,UserNo,FileSize)values(?,?,?,?,?,?)";
		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			pstmt.setString(1, seqNo);
			pstmt.setString(2, bean.getAttachName()); // 文件名
			pstmt.setString(3, bean.getGuidName()); // UUID
			pstmt.setString(4, bean.getExtensionName()); // 后缀
			pstmt.setString(5, bean.getUerNo()); // 料号
			pstmt.setString(6, bean.getFileSize()); // 文件大小
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
			// 5.处理ResultSet
			while (rs.next()) {
				// rs.get+数据库中对应的类型+(数据库中对应的列别名)
				id = rs.getString(1);

			}
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}

	public static String checkFolderId(WTPart part, String type) throws IOException, WTException {
		String folderId = "";
		PreparedStatement pstmt;
		try {
			Connection conn = getConn();
			System.out.println("checkFolderId=====================" + conn);
			// String sql = "select folderId from ED_Folder where
			// dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\')
			// ='企业空间\\PLM\\影院Barco\\图纸\\结构图纸'";
			String sql = "select folderId from ED_Folder where  dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\') = '企业空间\\PLM\\原材料库'";

			// if(type.equals("2")){
			// String classification = (String)PdfUtil.getIBAObjectValue(part,
			// "Classification");
			// System.out.println("classification======================"+classification);
			// if(classification.contains("appo_rj")) {
			// // String type = UploadAttachmentUtil.getTypeInternalName(part);
			// String sscpx = (String)PdfUtil.getIBAObjectValue(part, "sscpx");
			// System.out.println("sscpx============================="+sscpx);
			// String productCategory = getEnumerationDisplayName("sscpx",
			// sscpx);
			// System.out.println("productCategory================================"+productCategory);
			// sql = "select folderId from ED_Folder where
			// dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\') =
			// '企业空间\\PLM\\舞台灯\\"+productCategory+"'";
			// }
			// }else if(type.equals("1")){
			// sql = "select folderId from ED_Folder where
			// dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\') =
			// '企业空间\\PLM\\原材料库'";
			// }else if(type.equals("3")) {
			// sql = "select folderId from ED_Folder where
			// dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\') =
			// '企业空间\\PLM\\物料规格书'";
			// }
			System.out.println("sql=========================" + sql);
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			System.out.println("checkFolderId============pstmt=========" + pstmt);
			ResultSet rs = pstmt.executeQuery();
			System.out.println("checkFolderId============rs=========" + rs);
			// 5.处理ResultSet
			while (rs.next()) {
				// rs.get+数据库中对应的类型+(数据库中对应的列别名)
				folderId = rs.getString(1);

			}
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return folderId;
	}

	// public static String checkFolderId() throws IOException, WTException{
	// String folderId = "";
	// PreparedStatement pstmt;
	// try {
	// Connection conn = getConn();
	// System.out.println("checkFolderId====================="+conn);
	//// String sql = "select folderId from ED_Folder where
	// dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\')
	// ='企业空间\\PLM\\影院Barco\\图纸\\结构图纸'";
	// String sql = "select folderId from ED_Folder where
	// dbo.FolderPathIDConvertFolderPathName(FolderPath ,'\\') =
	// '企业空间\\PLM\\原材料库'";
	//
	// System.out.println("sql========================="+sql);
	// pstmt = (PreparedStatement)conn.prepareStatement(sql);
	// System.out.println("checkFolderId============pstmt========="+pstmt);
	// ResultSet rs = pstmt.executeQuery();
	// System.out.println("checkFolderId============rs========="+rs);
	// //5.处理ResultSet
	// while(rs.next()){
	// //rs.get+数据库中对应的类型+(数据库中对应的列别名)
	// folderId = rs.getString(1);
	//
	// }
	// conn.close();
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	// return folderId;
	// }

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

	public static String isSuccessFiles(String partNumber) throws IOException {
		String files = "";

		PreparedStatement pstmt;
		try {
			Connection conn = getConn();

			String sql = "SELECT Max(ID),AttachCount FROM ED_Files WHERE FileNo=? and isinvalid=0 group by AttachCount";
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			pstmt.setString(1, partNumber);
			ResultSet rs = pstmt.executeQuery();
			// 5.处理ResultSet
			while (rs.next()) {
				// rs.get+数据库中对应的类型+(数据库中对应的列别名)
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
			// 5.处理ResultSet
			while (rs.next()) {
				// rs.get+数据库中对应的类型+(数据库中对应的列别名)
				id = rs.getString(1);
				list.add(id);
			}
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

}
