package ext.appo.doc.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.ptc.core.lwc.common.view.TypeDefinitionReadView;
import com.ptc.core.lwc.server.TypeDefinitionServiceHelper;
import com.ptc.core.meta.common.TypeIdentifier;

import ext.appo.test.DataReportUtil;
import ext.pi.core.PIAttributeHelper;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.FormatContentHolder;
import wt.doc.WTDocument;
import wt.fc.ObjectVector;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.httpgw.GatewayAuthenticator;
import wt.iba.definition.StringDefinition;
import wt.iba.value.StringValue;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.PartDocHelper;
import wt.part.WTPart;
import wt.query.ClassAttribute;
import wt.query.ConstantExpression;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SQLFunction;
import wt.query.SearchCondition;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.vc.config.LatestConfigSpec;

public class ProjectDocExportUtil implements RemoteAccess {

	public static String filename;
	private HSSFWorkbook hssfWorkbook;

	public static void main(String[] args) throws WTException, RemoteException, InvocationTargetException {
		// TODO Auto-generated method stub
		// windchill ext.appo.test.DataReportUtil 2EP015A
		RemoteMethodServer rms = RemoteMethodServer.getDefault();
		GatewayAuthenticator auth = new GatewayAuthenticator();
		auth.setRemoteUser("wcadmin");
		rms.setAuthenticator(auth);

		String Number = "";

		for (int i = 0; i < args.length; i++) {
			if (Number.length() > 0) {
				Number = Number + " " + args[i];
			} else {
				Number = Number + args[i];
			}
		}
		System.out.println(Number);
		try {
			ProjectDocExportUtil projectDocExportUtil = new ProjectDocExportUtil();
			projectDocExportUtil.docReportByproject(Number);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 * QueryResult documentqr = getDocumentByIBA("ssxm", "2EP014A E6", "",
		 * ""); System.out.println("document size ==" + documentqr.size());
		 * while (documentqr.hasMoreElements()) { WTDocument doc = (WTDocument)
		 * documentqr.nextElement(); System.out.println("doc number==" +
		 * doc.getNumber()); QueryResult qResult =
		 * PartDocHelper.service.getAssociatedParts(doc); System.out.println(
		 * "doc type  name===" + getTypeIdentifierRemote(doc));
		 * System.out.println("doc type  name===" + qResult.size()); }
		 */
	}

	public static String getassociatePartnumber(WTDocument doc) {
		QueryResult qResult = null;
		String partnumber = "";
		try {
			qResult = PartDocHelper.service.getAssociatedParts(doc);
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (qResult.hasMoreElements()) {
			WTPart object = (WTPart) qResult.nextElement();
			if (partnumber.length() > 0) {
				partnumber = partnumber + ";" + object.getNumber();
			} else {
				partnumber = partnumber + object.getNumber();
			}

		}
		return partnumber;
	}

	public static String getTypeIdentifier(WTDocument doc) throws WTException {
		TypeIdentifier ti = com.ptc.core.meta.server.TypeIdentifierUtility.getTypeIdentifier(doc);
		TypeDefinitionReadView tiv = TypeDefinitionServiceHelper.service.getTypeDefView(ti);

		String docType = tiv.getDisplayName();
		return docType;
	}

	public static String getTypeIdentifiertype(WTDocument doc) throws WTException {
		TypeIdentifier ti = com.ptc.core.meta.server.TypeIdentifierUtility.getTypeIdentifier(doc);
		// TypeDefinitionReadView tiv =
		// TypeDefinitionServiceHelper.service.getTypeDefView(ti);

		String docType = ti.getTypename();
		return docType;
	}

	/**
	 * 远程调用根据文档获取文档类型显示值
	 * 
	 * @param doc
	 * @return
	 * @throws RemoteException
	 * @throws InvocationTargetException
	 */
	public static String getTypeIdentifierRemote(WTDocument doc) throws RemoteException, InvocationTargetException {
		RemoteMethodServer server = RemoteMethodServer.getDefault();
		server.setUserName("wcadmin");
		server.setPassword("wcadmin");

		Class[] argTypes = { WTDocument.class };
		Object[] args = { doc };
		String str = (String) server.invoke("getTypeIdentifier", ProjectDocExportUtil.class.getName(), null, argTypes,
				args);

		return str;

	}

	/**
	 * 远程调用根据文档获取文档类型
	 * 
	 * @param doc
	 * @return
	 * @throws RemoteException
	 * @throws InvocationTargetException
	 */
	public static String getTypeIdentifiertypeRemote(WTDocument doc) throws RemoteException, InvocationTargetException {
		RemoteMethodServer server = RemoteMethodServer.getDefault();
		server.setUserName("wcadmin");
		server.setPassword("wcadmin");

		Class[] argTypes = { WTDocument.class };
		Object[] args = { doc };
		String str = (String) server.invoke("getTypeIdentifiertype", ProjectDocExportUtil.class.getName(), null,
				argTypes, args);

		return str;

	}

	public void docReportByproject(String projectname) throws Exception {
		try {
			Vector dataList = new Vector<>();
			QueryResult documentqr = getDocumentByIBA("ssxm", projectname, "", "");
			System.out.println("document size ==" + documentqr.size());
			while (documentqr.hasMoreElements()) {
				WTDocument doc = (WTDocument) documentqr.nextElement();
				System.out.println("doc number==" + doc.getNumber());
				String doctype = getTypeIdentifiertypeRemote(doc);
				if (doctype.contains("ReferenceDocument")) {

					DocBean bean = new DocBean();
					bean.setSsxm(projectname);
					bean.setProductline(PIAttributeHelper.service.getDisplayValue(doc, "sscpx", Locale.CHINA));
					bean.setDoctype(getTypeIdentifierRemote(doc));
					bean.setSmalltype(DataReportUtil.getIBAvalue(doc, "SmallDocType"));// 文档小类
					bean.setDocnumber(doc.getNumber());
					bean.setDocname(doc.getName());
					bean.setVersion(
							doc.getVersionIdentifier().getValue() + "." + doc.getIterationIdentifier().getValue());
					bean.setMainname(getPrimaryContent(doc).getFileName());// 主内容
					bean.setDocstate(doc.getState().toString());
					bean.setReleatepart(getassociatePartnumber(doc));
					bean.setDoccreatime(doc.getCreateTimestamp().toString());
					bean.setDoccreator(doc.getCreatorFullName());
					bean.setModifidior(doc.getModifierFullName());
					bean.setLastmodifytime(doc.getModifyTimestamp().toString());
					bean.setRemark(DataReportUtil.getIBAvalue(doc, "bz"));
					dataList.add(bean);

				}
			}
			doExport(dataList, projectname);
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 获取EDocument对象的内容
	 * 
	 * @param wtdocument
	 *            动态文档
	 * @retun ApplicationData,业务对象的主文件对象
	 * @throws Exception
	 *             WTException
	 */

	public static ApplicationData getPrimaryContent(ContentHolder contentHolder1) throws Exception, WTException {
		ContentHolder contentHolder = null;
		ApplicationData applicationdata = null;
		try {
			contentHolder = ContentHelper.service.getContents((ContentHolder) contentHolder1);// 获取ContentHolder
			ContentItem contentitem = ContentHelper.getPrimary((FormatContentHolder) contentHolder);// 获取主内容对象
			applicationdata = (ApplicationData) contentitem;// ContentItem转换成ApplicationData
		} catch (WTException e1) {
			e1.printStackTrace();
			throw new WTException(e1.getLocalizedMessage());
		} catch (Exception e) {
			e.printStackTrace();
			// errorMessage = e.getLocalizedMessage();
			throw new IOException(e.getLocalizedMessage());
		}
		return applicationdata;
	}

	protected static QueryResult getDocumentByIBA(String definition1, String value1, String definition2, String value2)
			throws WTException {
		QuerySpec qs = new QuerySpec();
		int crIndex = qs.appendClassList(WTDocument.class, true);

		Hashtable hashtable = new Hashtable();
		hashtable.put(definition1, value1);
		if (hashtable != null && hashtable.size() > 0) {
			addIBASearchCondition(WTDocument.class, qs, hashtable);
		}

		if (StringUtils.isNotBlank(definition2) && StringUtils.isNotBlank(value2)) {
			Hashtable htIBAS = new Hashtable();
			htIBAS.put(definition2, value2);
			if (htIBAS != null && htIBAS.size() > 0) {
				addIBASearchCondition(WTDocument.class, qs, htIBAS);
			}
		}

		System.out.println("qs   ===  " + qs);
		ObjectVector vector = new ObjectVector();
		QueryResult qr = PersistenceHelper.manager.find(qs);
		WTDocument doc = null;
		while (qr.hasMoreElements()) {
			Object[] obj = (Object[]) qr.nextElement();
			doc = (WTDocument) obj[0];
			vector.addElement(doc);
		}
		qr = new LatestConfigSpec().process(new QueryResult(vector));
		/*
		 * if (qr.hasMoreElements()) { doc = (WTDocument) qr.nextElement(); }
		 */
		return qr;
	}

	/**
	 * 添加软属性搜索条件
	 *
	 * @param ibaHolderClass
	 *            Class搜索对象类型
	 * @param qs
	 *            QuerySpec搜索规格
	 * @param htIBAS
	 *            Hashtable IBA属性集合
	 * @throws QueryException
	 */
	public static void addIBASearchCondition(Class ibaHolderClass, QuerySpec qs, Hashtable htIBAS)
			throws QueryException {
		Enumeration eIBA = htIBAS.keys();
		while (eIBA.hasMoreElements()) {
			int ibaStringDefinitionIndex = qs.appendClassList(StringDefinition.class, false);
			int iIndex_StringValue = qs.appendClassList(StringValue.class, false);

			String strIBA_Name = (String) eIBA.nextElement();
			String strIBA_Value = (String) htIBAS.get(strIBA_Name);

			// logger.debug("IBA_Name:" + strIBA_Name + "====>IBA_Value:" +
			// strIBA_Value);

			ClassAttribute caValue = new ClassAttribute(StringValue.class, StringValue.VALUE);
			SQLFunction upperFunction = new SQLFunction("UPPER", caValue);
			String strOP = SearchCondition.EQUAL;
			if (strIBA_Value != null && (strIBA_Value.indexOf('*') != -1 || strIBA_Value.indexOf('%') != -1)) {
				strOP = SearchCondition.LIKE;
			}
			SearchCondition scStringValueValue = new SearchCondition(upperFunction, strOP,
					new ConstantExpression(new String(strIBA_Value).toUpperCase()));
			SearchCondition scStringDefinitionName = new SearchCondition(StringDefinition.class, StringDefinition.NAME,
					SearchCondition.EQUAL, strIBA_Name);
			SearchCondition scJoinStringValueStringDefinition = new SearchCondition(StringValue.class,
					"definitionReference.key.id", StringDefinition.class, WTAttributeNameIfc.ID_NAME);
			SearchCondition scStringValueDoc = new SearchCondition(StringValue.class, "theIBAHolderReference.key.id",
					ibaHolderClass, WTAttributeNameIfc.ID_NAME);

			if (qs.getConditionCount() > 0) {
				qs.appendAnd();
			}
			qs.appendWhere(scStringValueValue, new int[] { iIndex_StringValue });
			qs.appendAnd();
			int iIBAHolderIndex = qs.getFromClause().getPosition(ibaHolderClass);
			qs.appendWhere(scStringValueDoc, new int[] { iIndex_StringValue, iIBAHolderIndex });
			qs.appendAnd();
			qs.appendWhere(scJoinStringValueStringDefinition,
					new int[] { iIndex_StringValue, ibaStringDefinitionIndex });
			qs.appendAnd();
			qs.appendWhere(scStringDefinitionName, new int[] { ibaStringDefinitionIndex });
		}
	}

	public void doExport(Vector datalist, String projectname) {
		try {
			try {
				this.hssfWorkbook = WriteToDoc(datalist);
				// SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd
				// HH:mm:ss");
				String fil_ename = projectname + "_project_Report" + new Date() + ".xls";

				FileOutputStream iOutputStream = new FileOutputStream(getDefaultPath() + fil_ename);

				this.hssfWorkbook.write(iOutputStream);

				iOutputStream.close();
			} catch (WTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private HSSFWorkbook WriteToDoc(Vector datalist) throws IOException, WTException {
		HSSFWorkbook hssfworkbook = new HSSFWorkbook();

		HSSFSheet dochssfsheet = hssfworkbook.createSheet("result");

		writeDocToSheet(datalist, hssfworkbook, dochssfsheet);
		System.out.println("end>>>>>>>>>>>>>>>>>>>>>>>");
		return hssfworkbook;
	}

	// 保存路径
	public static String getDefaultPath() {
		WTProperties props;
		try {
			props = WTProperties.getLocalProperties();
			String base = props.getProperty("wt.codebase.location");
			filename = base + File.separator + "temp" + File.separator;
			System.out.println(">>>>>" + filename);
			return filename;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static HSSFCellStyle getAlignCellStyle(HSSFWorkbook wb) throws IOException {
		HSSFCellStyle cellStyle = wb.createCellStyle();
		cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		cellStyle.setWrapText(true);
		return cellStyle;
	}

	@SuppressWarnings("deprecation")
	public static void writeDocToSheet(Vector ecalist, HSSFWorkbook hssfWorkbook, HSSFSheet hssfSheet)
			throws IOException {
		/*
		 * hssfSheet.setColumnWidth((short)0, (short)1500);
		 * hssfSheet.setColumnWidth((short)1, (short)5000);
		 * hssfSheet.setColumnWidth((short)2, (short)5000);
		 * hssfSheet.setColumnWidth((short)3, (short)3000);
		 * hssfSheet.setColumnWidth((short)4, (short)3000);
		 * hssfSheet.setColumnWidth((short)5, (short)3000);
		 * hssfSheet.setColumnWidth((short)6, (short)3000);
		 * hssfSheet.setColumnWidth((short)7, (short)1500);
		 * hssfSheet.setColumnWidth((short)8, (short)5000);
		 * hssfSheet.setColumnWidth((short)9, (short)5000);
		 * hssfSheet.setColumnWidth((short)10, (short)3000);
		 * hssfSheet.setColumnWidth((short)11, (short)3000);
		 * hssfSheet.setColumnWidth((short)12, (short)3000);
		 * hssfSheet.setColumnWidth((short)13, (short)3000);
		 * hssfSheet.setColumnWidth((short)14, (short)3000);
		 * hssfSheet.setColumnWidth((short)15, (short)3000);
		 */

		int i = 0;
		int j = 0;
		HSSFRow firstrow = hssfSheet.createRow((short) (i));
		HSSFCell cell = firstrow.createCell((short) (j++));
		cell.setCellValue("项目报表");
		cell.setCellStyle(getAlignCellStyle(hssfWorkbook));

		i++;
		j = 0;

		HSSFRow secondrow = hssfSheet.createRow((short) (i));
		secondrow.createCell((short) (j++)).setCellValue("序号");
		secondrow.createCell((short) (j++)).setCellValue("所属项目");
		secondrow.createCell((short) (j++)).setCellValue("所属产品类别");
		secondrow.createCell((short) (j++)).setCellValue("文档大类");
		secondrow.createCell((short) (j++)).setCellValue("文档小类");
		secondrow.createCell((short) (j++)).setCellValue("编号");
		secondrow.createCell((short) (j++)).setCellValue("文档名称");
		secondrow.createCell((short) (j++)).setCellValue("版本");
		secondrow.createCell((short) (j++)).setCellValue("主要内容名称");
		secondrow.createCell((short) (j++)).setCellValue("状态");
		secondrow.createCell((short) (j++)).setCellValue("参考方部件");
		secondrow.createCell((short) (j++)).setCellValue("创建者");
		secondrow.createCell((short) (j++)).setCellValue("创建时间");
		secondrow.createCell((short) (j++)).setCellValue("修改者");
		secondrow.createCell((short) (j++)).setCellValue("上次修改时间");
		secondrow.createCell((short) (j++)).setCellValue("备注");

		/*
		 * Vector ecnlist=(Vector)datalist.get(0); System.out.println(
		 * "ecnlist size===sheet==="+ecnlist.size());
		 */

		System.out.println("ecalist size===sheet===" + ecalist.size());
		/*
		 * for(int n=0 ;n<ecnlist.size();n++ ){ ECNObject ecnObject =
		 * (ECNObject) ecnlist.get(n);
		 */

		// hssfsheet.addMergedRegion(new Region(8, (short) 0, 9, (short) 1));
		for (int k = 0; k < ecalist.size(); k++) {
			DocBean docBean = (DocBean) ecalist.get(k);
			HSSFRow row = hssfSheet.createRow((short) (i + k + 1));
			j = 0;

			row.createCell((short) (j++)).setCellValue(k);
			row.createCell((short) (j++)).setCellValue(docBean.getSsxm());
			row.createCell((short) (j++)).setCellValue(docBean.getProductline());
			row.createCell((short) (j++)).setCellValue(docBean.getDoctype());
			row.createCell((short) (j++)).setCellValue(docBean.getSmalltype());
			row.createCell((short) (j++)).setCellValue(docBean.getDocnumber());
			row.createCell((short) (j++)).setCellValue(docBean.getDocname());
			row.createCell((short) (j++)).setCellValue(docBean.getVersion());
			row.createCell((short) (j++)).setCellValue(docBean.getMainname());
			row.createCell((short) (j++)).setCellValue(docBean.getDocstate());
			row.createCell((short) (j++)).setCellValue(docBean.getReleatepart());
			row.createCell((short) (j++)).setCellValue(docBean.getDoccreator());
			row.createCell((short) (j++)).setCellValue(docBean.getDoccreatime());
			row.createCell((short) (j++)).setCellValue(docBean.getModifidior());
			row.createCell((short) (j++)).setCellValue(docBean.getLastmodifytime());
			row.createCell((short) (j++)).setCellValue(docBean.getRemark());

		}

		// hssfSheet.addMergedRegion(new Region(0, (short) 0, 0, (short) 8));
		// //参数1，起始行-1，参数2,起始列-1，参数3,终止行-1,参数4,终止列-1
		// }
	}

	public HSSFWorkbook getHssfWorkbook() {
		return hssfWorkbook;
	}

	public void setHssfWorkbook(HSSFWorkbook hssfWorkbook) {
		this.hssfWorkbook = hssfWorkbook;
	}
}
