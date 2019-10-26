package ext.appo.ecn.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import ext.appo.ecn.common.util.ChangeUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.ecn.report.Object.ECAObject;
import ext.appo.util.PartUtil;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PICoreHelper;
import wt.change2.ChangeActivityIfc;
import wt.change2.ChangeOrder2;
import wt.change2.Changeable2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.vc.config.LatestConfigSpec;

public class ExportEcadeferentItems implements RemoteAccess, Serializable {
	private Vector datalist;
	public static String filename;
	private HSSFWorkbook hssfWorkbook;

	public ExportEcadeferentItems(Vector datalist) {

		this.datalist = datalist;
		this.hssfWorkbook = null;
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String ecnnumber = null;

		if (args == null)
			return;
		for (int i = 0; i < args.length; i += 2) {
			if (i + 1 < args.length) {
				if (("-e".equals(args[i]))) {
					ecnnumber = args[i + 1];
				}
			}
		}
		RemoteMethodServer server = RemoteMethodServer.getDefault();
		server.setUserName("wcadmin");
		server.setPassword("wcadmin");
		Vector ecnvector = getAllEC("");
		System.out.println("ecn vector ===" + ecnvector.size());
		Vector dataList = getallECA(ecnvector);
		System.out.println("ecArrayList  ===" + dataList.size());
		ExportEcadeferentItems exportEcadeferentItems = new ExportEcadeferentItems(dataList);
		exportEcadeferentItems.doExport(dataList);
	}

	public static Vector getallECA(Vector ecnvector) {
		if (ecnvector.size() == 0) {
			System.out.println("ecnvector size is null------");
			return null;
		}
		Vector dataArrayList = new Vector();
		Vector ecaArrayList = new Vector();
		Vector ecnArrayList = new Vector();
		for (int i = 0; i < ecnvector.size(); i++) {

			try {
				WTChangeOrder2 ecn = (WTChangeOrder2) ecnvector.get(i);
				if (ecn.getState().toString().startsWith("RESOLVED")) {
					com.ptc.windchill.pdmlink.change.server.impl.WorkflowProcessHelper.setECNResolutionDate(ecn);
				}

				// 获取ECN中所有ECA与受影响对象集合
				Map<ChangeActivityIfc, Collection<Changeable2>> beforeInfoMap = ChangeUtils
						.getChangeablesBeforeInfo(ecn);

				// 获取ECN中所有ECA与产生对象集合
				Map<ChangeActivityIfc, Collection<Changeable2>> afterInfoMap = ChangeUtils.getChangeablesAfterInfo(ecn);

				for (Map.Entry<ChangeActivityIfc, Collection<Changeable2>> beforeEntryMap : beforeInfoMap.entrySet()) {
					WTChangeActivity2 eca = (WTChangeActivity2) beforeEntryMap.getKey();

					ECAObject ecaObject = new ECAObject();
					ecaObject.setECN_name(ecn.getName());
					ecaObject.setECAcreatetime(ecn.getCreatorName());
					ecaObject.setECN_number(ecn.getNumber());
					ecaObject.setECN_state(ecn.getState().toString());
					ecaObject.setECAnumber(ChangeUtils.getNumber(eca));
					ecaObject.setECAname(eca.getName());
					ecaObject.setECA_state(eca.getState().toString());
					// 判断ChangeActivityIfc是否为‘事务性任务’更改任务
					if (PICoreHelper.service.isType(beforeEntryMap.getKey(),
							ChangeConstants.TRANSACTIONAL_CHANGEACTIVITY2)) {
						ecaObject.setECA_type("事务性任务");
					} else {
						ecaObject.setECA_type("更改任务");
					}
					// 受影响对象集合
					Collection<Changeable2> beforeArray = beforeEntryMap.getValue();
					for (Changeable2 changeable2 : beforeArray) {
						// if(ChangeUtils.checkState((LifeCycleManaged)
						// changeable2, ChangeConstants.RELEASED)){
						// 产生对象集合
						Collection<Changeable2> afterInfoArray = afterInfoMap.get(eca);
						if (afterInfoArray.size() == 0 || afterInfoArray == null) {
							if (changeable2 instanceof WTPart) {
								WTPart before_part = (WTPart) changeable2;

								ecaObject.setECA_after_affectitems(String.valueOf(afterInfoArray.size()));
								ecaObject.setECA_before_affectitems(String.valueOf(beforeArray.size()));

								ecaObject.setECA_after_affectitemsnumber("");
								ecaObject.setECA_before_affectitemsnumber(before_part.getNumber());

								ecaObject.setECA_after_affectitemsname("");
								ecaObject.setECA_before_affectitemsname(before_part.getName());

								ecaObject.setECA_after_affectitemsstate("");
								ecaObject.setECA_before_affectitemsstate(before_part.getState().toString());

								ecaObject.setECA_before_version(before_part.getVersionIdentifier().getValue());
								ecaObject.setECA_after_version("");

								ecaObject.setECA_change_type(
										(String) PIAttributeHelper.service.getValue(before_part, "ChangeType"));
								ecaObject.setECN_creator(ecn.getCreatorFullName());
								ecaObject.setECN_create_time(ecn.getCreateTimestamp().toLocaleString());

								WTPart newpart = PartUtil.getLastestWTPartByNumber(before_part.getNumber());
								ecaObject.setECA_new_version(newpart.getVersionIdentifier().getValue());
								ecaObject.setECA_new_state(newpart.getState().toString());

							}
						}
						for (Changeable2 afterObject : afterInfoArray) {
							if (afterObject instanceof WTPart && changeable2 instanceof WTPart) {
								WTPart after_part = (WTPart) afterObject;
								WTPart before_part = (WTPart) changeable2;
								ecaObject.setECA_after_affectitems(String.valueOf(afterInfoArray.size()));
								ecaObject.setECA_before_affectitems(String.valueOf(beforeArray.size()));

								ecaObject.setECA_after_affectitemsnumber(after_part.getNumber());
								ecaObject.setECA_before_affectitemsnumber(before_part.getNumber());

								ecaObject.setECA_after_affectitemsname(after_part.getName());
								ecaObject.setECA_before_affectitemsname(before_part.getName());

								ecaObject.setECA_after_affectitemsstate(after_part.getState().toString());
								ecaObject.setECA_before_affectitemsstate(before_part.getState().toString());

								ecaObject.setECA_before_version(before_part.getVersionIdentifier().getValue());
								ecaObject.setECA_after_version(after_part.getVersionIdentifier().getValue());
								WTPart newpart = PartUtil.getLastestWTPartByNumber(before_part.getNumber());
								ecaObject.setECA_new_version(newpart.getVersionIdentifier().getValue());
								ecaObject.setECA_new_state(newpart.getState().toString());
								ecaObject.setECA_change_type(
										(String) PIAttributeHelper.service.getValue(before_part, "ChangeType"));
								ecaObject.setECN_creator(ecn.getCreatorFullName());
								ecaObject.setECN_create_time(ecn.getCreateTimestamp().toLocaleString());
							}
						}

					}
					ecaArrayList.add(ecaObject);
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		System.out.println("ecnArrayList===" + ecnArrayList.size());
		System.out.println("ecaArrayList===" + ecaArrayList.size());

		return ecaArrayList;

	}

	// 如果为“”，则得到所有的ecn
	public static Vector getAllEC(String number) throws Exception {
		QuerySpec qs = new QuerySpec(ChangeOrder2.class);
		if (number == null)
			return null;
		if (number.trim().length() > 0) {

			SearchCondition scNumber = new SearchCondition(ChangeOrder2.class, ChangeOrder2.NUMBER,
					SearchCondition.EQUAL, number.toUpperCase());
			qs.appendWhere(scNumber);
		}

		SearchCondition scLatestIteration = new SearchCondition(ChangeOrder2.class, WTAttributeNameIfc.LATEST_ITERATION,
				SearchCondition.IS_TRUE);

		qs.appendWhere(scLatestIteration);

		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr != null && qr.hasMoreElements())
			qr = (new LatestConfigSpec()).process(qr);

		if (qr != null && qr.hasMoreElements())
			return qr.getObjectVectorIfc().getVector();

		return new Vector();
	}

	private Vector datavecVector;
	int rowNum = 0; // work sheet row number

	public static String[] getHeaderByProp() {
		Properties props = new Properties();
		try {
			props.load(
					new InputStreamReader(ExportEcadeferentItems.class.getResourceAsStream("pz.properties"), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] Header = props.getProperty("Header").trim().split(",");
		return Header;
	}

	public void doExport(Vector datalist) {
		try {
			try {
				this.hssfWorkbook = WriteToDoc(datalist);
				// SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd
				// HH:mm:ss");
				String fil_ename = "ecn_Report" + new Date() + ".xls";

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
		cell.setCellValue("ECN报表");
		cell.setCellStyle(getAlignCellStyle(hssfWorkbook));

		i++;
		j = 0;

		HSSFRow secondrow = hssfSheet.createRow((short) (i));
		secondrow.createCell((short) (j++)).setCellValue("序号");
		secondrow.createCell((short) (j++)).setCellValue("ECN编码");
		secondrow.createCell((short) (j++)).setCellValue("ECN名称");
		secondrow.createCell((short) (j++)).setCellValue("ECN创建时间");
		secondrow.createCell((short) (j++)).setCellValue("ECN创建人");
		secondrow.createCell((short) (j++)).setCellValue("ECN状态");
		secondrow.createCell((short) (j++)).setCellValue("ECA编码");
		secondrow.createCell((short) (j++)).setCellValue("ECA名称");
		secondrow.createCell((short) (j++)).setCellValue("ECA状态");
		secondrow.createCell((short) (j++)).setCellValue("ECA类型");
		secondrow.createCell((short) (j++)).setCellValue("受影响对象数量");
		secondrow.createCell((short) (j++)).setCellValue("产生对象数量");
		secondrow.createCell((short) (j++)).setCellValue("受影响对象编码");
		secondrow.createCell((short) (j++)).setCellValue("受影响对象名称");
		secondrow.createCell((short) (j++)).setCellValue("受影响对象状态");
		secondrow.createCell((short) (j++)).setCellValue("受影响对象版本");
		secondrow.createCell((short) (j++)).setCellValue("变更类型");
		secondrow.createCell((short) (j++)).setCellValue("产生对象编码");
		secondrow.createCell((short) (j++)).setCellValue("产生对象名称");
		secondrow.createCell((short) (j++)).setCellValue("产生对象状态");
		secondrow.createCell((short) (j++)).setCellValue("产生对象版本");
		secondrow.createCell((short) (j++)).setCellValue("最新版本");
		secondrow.createCell((short) (j++)).setCellValue("最新状态");
		secondrow.createCell((short) (j++)).setCellValue("材质");
		secondrow.createCell((short) (j++)).setCellValue("容值");
		secondrow.createCell((short) (j++)).setCellValue("额定电压");
		secondrow.createCell((short) (j++)).setCellValue("额定工作温度");
		secondrow.createCell((short) (j++)).setCellValue("字段1");

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
			ECAObject ecaObject = (ECAObject) ecalist.get(k);
			HSSFRow row = hssfSheet.createRow((short) (i + k + 1));
			j = 0;

			row.createCell((short) (j++)).setCellValue(k);
			row.createCell((short) (j++)).setCellValue(ecaObject.getECN_number());
			row.createCell((short) (j++)).setCellValue(ecaObject.getECN_name());
			row.createCell((short) (j++)).setCellValue(ecaObject.getECN_create_time());
			row.createCell((short) (j++)).setCellValue(ecaObject.getECN_creator());
			row.createCell((short) (j++)).setCellValue(ecaObject.getECN_state());
			row.createCell((short) (j++)).setCellValue(ecaObject.getECAnumber());
			row.createCell((short) (j++)).setCellValue(ecaObject.getECAname());
			row.createCell((short) (j++)).setCellValue(ecaObject.getECA_state());
			row.createCell((short) (j++)).setCellValue(ecaObject.getECA_type());
			row.createCell((short) (j++)).setCellValue(ecaObject.getECA_before_affectitems());
			row.createCell((short) (j++)).setCellValue(ecaObject.getECA_after_affectitems());
			row.createCell((short) (j++)).setCellValue(ecaObject.getECA_before_affectitemsnumber());
			row.createCell((short) (j++)).setCellValue(ecaObject.getECA_before_affectitemsname());
			row.createCell((short) (j++)).setCellValue(ecaObject.getECA_before_affectitemsstate());
			row.createCell((short) (j++)).setCellValue(ecaObject.getECA_before_version());
			row.createCell((short) (j++)).setCellValue(ecaObject.getECA_change_type());
			row.createCell((short) (j++)).setCellValue(ecaObject.getECA_after_affectitemsnumber());
			row.createCell((short) (j++)).setCellValue(ecaObject.getECA_after_affectitemsname());
			row.createCell((short) (j++)).setCellValue(ecaObject.getECA_after_affectitemsstate());
			row.createCell((short) (j++)).setCellValue(ecaObject.getECA_after_version());
			row.createCell((short) (j++)).setCellValue(ecaObject.getECA_new_version());
			row.createCell((short) (j++)).setCellValue(ecaObject.getECA_new_state());
			row.createCell((short) (j++)).setCellValue(ecaObject.getCz());
			row.createCell((short) (j++)).setCellValue(ecaObject.getRz());
			row.createCell((short) (j++)).setCellValue(ecaObject.getEddy());
			row.createCell((short) (j++)).setCellValue(ecaObject.getEdgzwd());

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

	public Vector getDatavecVector() {
		return datavecVector;
	}

	public void setDatavecVector(Vector datavecVector) {
		this.datavecVector = datavecVector;
	}

	public static HSSFCellStyle getAlignCellStyle(HSSFWorkbook wb) throws IOException {
		HSSFCellStyle cellStyle = wb.createCellStyle();
		cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		cellStyle.setWrapText(true);
		return cellStyle;
	}

}
