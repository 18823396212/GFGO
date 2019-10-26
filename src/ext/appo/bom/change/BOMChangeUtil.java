package ext.appo.bom.change;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.ptc.windchill.enterprise.object.WhereUsedConfigSpec;

import ext.appo.util.PartUtil;
import wt.change2.Changeable2;
import wt.enterprise.Master;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.method.RemoteMethodServer;
import wt.occurrence.OccurrenceHelper;
import wt.part.LineNumber;
import wt.part.PartUsesOccurrence;
import wt.part.Quantity;
import wt.part.QuantityUnit;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.pom.PersistenceException;
import wt.query.CompositeWhereExpression;
import wt.query.ConstantExpression;
import wt.query.LogicalOperator;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.TableColumn;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.vc.Iterated;
import wt.vc.VersionControlHelper;
import wt.vc.struct.StructHelper;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.WorkInProgressException;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;

public class BOMChangeUtil {

	private Vector dataVector = new Vector();
	private Vector messageVector = new Vector();
	private String filename = "";
	private static Boolean fag = true;
	public static String filename1;

	/**
	 * windchill ext.common.change.BOM.InvtBOMChangeUtil -u wcadmin -p pdm -f
	 * changeBOM.xls
	 * 
	 * @param args
	 * @throws IOException
	 * @throws WTPropertyVetoException
	 */
	public static void main(String[] args) throws IOException, WTPropertyVetoException {
		// TODO Auto-generated method stub

		// TODO Auto-generated method stub
		String userName = null;
		String passWord = null;
		String filename = null;

		for (int i = 0; i < args.length; i += 2) {
			if (i + 1 < args.length) {
				if (("-u".equals(args[i]))) {
					userName = args[i + 1];
				} else if ("-p".equals(args[i])) {
					passWord = args[i + 1];
				} else if ("-f".equals(args[i])) {
					filename = args[i + 1];
				}
			}
		}

		if (userName == null)
			userName = "wcadmin";
		if (passWord == null)
			passWord = "pdm";
		RemoteMethodServer server = RemoteMethodServer.getDefault();
		server.setUserName(userName);
		server.setPassword(passWord);
		BOMChangeUtil invtBOMChangeUtil = new BOMChangeUtil(filename);
		invtBOMChangeUtil.doOperate();

	}

	public BOMChangeUtil() {
		this.dataVector = null;
		this.filename = "";
	}

	public BOMChangeUtil(String filename) {
		this.filename = filename;
		this.dataVector = getExcelData(this.filename);
	}

	// 获取Excel表中的数据
	public static Vector getExcelData(String filename) {
		BomChangeReader bomChangeReader = new BomChangeReader(filename);
		bomChangeReader.initHssfSheet();
		return bomChangeReader.getDataVector();
	}

	public static Vector changeBom(Vector dataVector) throws WTPropertyVetoException {
		Vector messageVector = new Vector();
		Vector delVector = new Vector();
		Vector addVector = new Vector();
		Vector updateVector = new Vector();
		Vector changeVector = new Vector();
		BomDataExecutor invtBomDataExecutor = new BomDataExecutor();
		// 循环两次，先做删除，再做添加的操作
		for (int i = 0; i < dataVector.size(); i++) {
			ChangeBOMObject changeBOMObject = (ChangeBOMObject) dataVector.get(i);
			String changetype = changeBOMObject.getChangetype();
			WTPart parentPart = new WTPart();
			try {
				parentPart = PartUtil.getLastestWTPartByNumber(changeBOMObject.getParentnumber());
				fag = checkChangeStatus(parentPart);// 查看是否有变更的情况，有，返回false
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (fag) {
				if (changetype.equals("删除")) {
					// delBOMLine(parentPartnumber, childPartnumber);
					delVector = invtBomDataExecutor.deleteBomLine(changeBOMObject);
					messageVector.add(delVector);
				}
				if (changetype.equals("修改")) {
					Vector errorVector = invtBomDataExecutor.checkOccurrence(parentPart, changeBOMObject);
					if (errorVector.size() > 0) {
						messageVector.add(errorVector);
					} else {
						updateVector = invtBomDataExecutor.updateBomLine(changeBOMObject);
						messageVector.add(updateVector);
					}
				}
				if (changetype.equals("增加")) {
					Vector errorVector = invtBomDataExecutor.checkOccurrence(parentPart, changeBOMObject);
					if (errorVector.size() > 0) {
						messageVector.add(errorVector);
					} else {
						addVector = invtBomDataExecutor.addBomLine(changeBOMObject);
						messageVector.add(addVector);
					}
				}
			} else {
				changeVector.add("第" + changeBOMObject.getId() + "行" + changeBOMObject.getParentnumber() + "存在变更或检出状态");
				messageVector.add(changeVector);
			}
		}
		return messageVector;
	}

	/**
	 * 创建part间的usagelink关系
	 * 
	 * @param parentPart
	 * @param part
	 * @throws WTException
	 */

	public static WTPartUsageLink createUsageLink(WTPart parent, WTPartMaster master, String unit, double amount,
			String index, String occurence) throws WTException {
		// parent part need checking out before creating the link.
		parent = (WTPart) PersistenceHelper.manager.prepareForModification(parent);
		PersistenceServerHelper.manager.lock(parent, true);

		QuantityUnit quantityUnit = QuantityUnit.toQuantityUnit(unit);
		Quantity quantity = Quantity.newQuantity(amount, quantityUnit);
		WTPartUsageLink usagelink = WTPartUsageLink.newWTPartUsageLink(parent, master);
		try {
			usagelink.setQuantity(quantity);
			if ((index != null) && (index.length() > 0)) {
				LineNumber lineNumber = LineNumber.newLineNumber(Long.parseLong(index));
				usagelink.setLineNumber(lineNumber);
			}
			usagelink = (WTPartUsageLink) PersistenceHelper.manager.store(usagelink);
			// add new occurrence
			if ((occurence != null) && (occurence.length() > 0)) {
				PartUsesOccurrence newOccurrence = PartUsesOccurrence.newPartUsesOccurrence(usagelink);

				newOccurrence.setName(occurence);
				OccurrenceHelper.service.saveUsesOccurrenceAndData(newOccurrence, null);
			}
		} catch (WTPropertyVetoException wpve) {
			throw new WTException(wpve);
		}
		System.out.println("    created one usage link between " + parent.getNumber() + " and " + master.getNumber());
		return usagelink;
	}

	// 删除bom下面的某个子件
	public static List delBOMLine(String parentnumber, String childpartnumer) throws Exception {
		int preOffset = 1;
		WTPart parentPart = PartUtil.getLastestWTPartByNumber(parentnumber);

		if (parentPart == null) {
			System.out.println("can not find the parentpart");
		}
		removeBOMFromPart(parentPart, childpartnumer, "");
		return null;

	}

	public static void removeBOMFromPart(WTPart part, String childNum, String Remark) {

		try {
			try {
				part = (WTPart) checkoutWTPart(part, "");
			} catch (WTPropertyVetoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			WTPartUsageLink link = queryPartusageLink(part, childNum);

			PersistenceHelper.manager.delete(link);
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			WTPart checkinWTPart = checkinWTPart(part, "Delete childpart");
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static WTPartUsageLink queryPartusageLink(WTPart part, String childnumer) {
		WTPartUsageLink usagelink = null;
		if (part == null || childnumer.length() == 0) {
			return null;
		}
		long poid = part.getPersistInfo().getObjectIdentifier().getId();

		try {
			QuerySpec queryspec = new QuerySpec();
			int a = queryspec.appendClassList(WTPartUsageLink.class, true);
			queryspec.setAdvancedQueryEnabled(true);
			String[] aliases = new String[1];
			aliases[0] = queryspec.getFromClause().getAliasAt(a);
			TableColumn tc1 = new TableColumn(aliases[0], "IDA3A5");
			CompositeWhereExpression andExpression = new CompositeWhereExpression(LogicalOperator.AND);
			andExpression.append(new SearchCondition(tc1, "=", new ConstantExpression(new Long(poid))));
			queryspec.appendWhere(andExpression, null);
			QueryResult qr1 = PersistenceHelper.manager.find(queryspec);
			while (qr1.hasMoreElements()) {
				Object obj[] = (Object[]) qr1.nextElement();
				WTPartUsageLink usage = (WTPartUsageLink) obj[0];
				WTPartMaster partmasterObj = (WTPartMaster) usage.getRoleBObject();
				if (childnumer.equals(partmasterObj.getNumber())) {
					System.out.println("-------------" + childnumer);
					usagelink = usage;

				}
			}
		} catch (QueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return usagelink;
	}

	// 检入部件
	public static WTPart checkinWTPart(WTPart part, String checkInNote) throws WTException {
		// 判断是否需要检入部件
		boolean needToCheckin = WorkInProgressHelper.isCheckedOut((Workable) part);
		try {
			if (needToCheckin) {
				part = (WTPart) WorkInProgressHelper.service.checkin(part, checkInNote);
				System.out.print("check in>>>>>>");
			}
		} catch (WorkInProgressException e) {
			e.printStackTrace();
		} catch (WTPropertyVetoException e) {
			e.printStackTrace();
		} catch (PersistenceException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
		return part;
	}

	/*
	 * 检出部件
	 */
	public static WTPart checkoutWTPart(WTPart part, String checkOutNote) throws WTException, WTPropertyVetoException {
		WTPart workingPart;
		// 判断是否有检出权限)
		if (WorkInProgressHelper.service.isCheckoutAllowed(part)) {
			wt.folder.Folder checkOutFolder = null;
			try {
				checkOutFolder = WorkInProgressHelper.service.getCheckoutFolder();
			} catch (WTException e) {
				e.printStackTrace();
			}
			CheckoutLink checkOutLink = null;
			try {
				checkOutLink = WorkInProgressHelper.service.checkout(part, checkOutFolder, checkOutNote);
				System.out.println(">>>>>>>>>>>>>>The part has  check out1");
			} catch (WTException e) {
				throw e;
			}
			workingPart = (WTPart) checkOutLink.getWorkingCopy();
		} else {
			workingPart = part;
		}
		return workingPart;
	}

	public static long getMaxLineNumber(long[] array) {
		Long big = Long.valueOf(0);
		for (int i = 0; i < array.length; i++) {
			if (big < array[i]) {
				big = array[i];
			}
		}
		return big + 10;
	}

	public void doOperate() throws IOException, WTPropertyVetoException {
		Vector allMessage = new Vector();
		if (this.filename.length() == 0) {
			System.out.println("can not find the file to read!");
		} else {
			this.dataVector = getExcelData(this.filename);
		}
		if (this.dataVector.size() == 0) {
			System.out.println("this excel file is null\\\\");
		} else {

			this.messageVector = verifyBusinessData(this.dataVector);
			if (this.messageVector.size() > 0) {
				for (int k = 0; k < messageVector.size(); k++) {
					System.out.println("messageVector==" + messageVector.get(k).toString());
				}
				allMessage = this.messageVector;
			} else {
				this.messageVector = changeBom(this.dataVector);
				System.out.println("this.messageVector.size()===" + this.messageVector.size());
				for (int i = 0; i < this.messageVector.size(); i++) {

					Vector vector = (Vector) messageVector.get(i);
					for (int k = 0; k < vector.size(); k++) {
						allMessage.add(vector.get(k).toString());
					}
				}
			}

			HSSFWorkbook hssWorkbook = WriteToDoc(allMessage);
			System.out.println("allmessage===" + allMessage.size());
			Date now = new Date();
			String fil_ename = "BOMme" + now.getHours() + now.getMinutes() + now.getSeconds() + ".xls";
			FileOutputStream iOutputStream = new FileOutputStream(getDefaultPath() + fil_ename);
			hssWorkbook.write(iOutputStream);

			iOutputStream.close();

		}
	}

	// 保存路径
	public static String getDefaultPath() {
		WTProperties props;
		try {
			props = WTProperties.getLocalProperties();
			String base = props.getProperty("wt.codebase.location");
			filename1 = base + File.separator + "temp" + File.separator;
			System.out.println(">>>>>" + filename1);
			return filename1;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static HSSFWorkbook WriteToDoc(Vector objectVector) throws IOException {
		long beginTime = System.currentTimeMillis();
		HSSFWorkbook hssfworkbook = new HSSFWorkbook();

		if (objectVector.size() > 0) {
			HSSFSheet dochssfsheet = hssfworkbook.createSheet("result");
			writeDocToSheet(objectVector, hssfworkbook, dochssfsheet);
		} else {
			objectVector.add("所有操作成功！！！");
			HSSFSheet dochssfsheet = hssfworkbook.createSheet("result");
			writeDocToSheet(objectVector, hssfworkbook, dochssfsheet);
		}
		return hssfworkbook;
	}

	public static void writeDocToSheet(Vector vector, HSSFWorkbook hssfWorkbook, HSSFSheet hssfSheet)
			throws IOException {
		hssfSheet.setColumnWidth((short) 0, (short) 1500);
		hssfSheet.setColumnWidth((short) 1, (short) 5000);

		int i = 0;
		int j = 0;
		HSSFRow firstrow = hssfSheet.createRow((short) (i));

		HSSFCell cell = firstrow.createCell((short) (j++));
		cell.setCellValue("BOM操作异常信息");
		cell.setCellStyle(getAlignCellStyle(hssfWorkbook));

		i++;
		j = 0;
		HSSFRow secondrow = hssfSheet.createRow((short) (i));
		secondrow.createCell((short) (j++)).setCellValue("序号");
		secondrow.createCell((short) (j++)).setCellValue("异常信息");

		// hssfsheet.addMergedRegion(new Region(8, (short) 0, 9, (short) 1));
		for (int k = 0; k < vector.size(); k++) {
			String message = vector.get(k).toString();

			HSSFRow row = hssfSheet.createRow((short) (i + k + 1));
			j = 0;
			row.createCell((short) (j++)).setCellValue(k + 1);
			row.createCell((short) (j++)).setCellValue(message);
		}
		// hssfSheet.addMergedRegion(new Region(0, (short) 0, 0, (short) 8)); //
		// 参数1，起始行-1，参数2,起始列-1，参数3,终止行-1,参数4,终止列-1
	}

	public static HSSFCellStyle getAlignCellStyle(HSSFWorkbook wb) throws IOException {
		HSSFCellStyle cellStyle = wb.createCellStyle();
		cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		cellStyle.setWrapText(true);
		return cellStyle;
	}

	/**
	 * 得到当前子件的全部父件集合(仅为最新的大版本)
	 */
	public static QueryResult getPartUsedBy(Master master) throws WTException {
		// WTPrincipal currentUser = SessionHelper.manager.getPrincipal();
		// WTPrincipal previous = SessionHelper.manager.setAdministrator();
		QueryResult queryresult = null;
		WhereUsedConfigSpec obj = new WhereUsedConfigSpec();
		QueryResult queryresult2 = VersionControlHelper.service.allVersionsOf(master);
		Iterated iterated1 = null;
		if (queryresult2.hasMoreElements())
			iterated1 = (Iterated) queryresult2.nextElement();
		queryresult = StructHelper.service.navigateUsedByToIteration(iterated1, true, obj);
		return queryresult;
	}

	/**
	 * 1.料号是否重复 2.料号是否存在 3.子件是否为检出状态 4.子件是否与父件相同 5.数量是否为double 6.位号是否与数量匹配
	 * 7.位号是否有重复值
	 */
	protected Vector verifyBusinessData(Vector dataVector) {
		Vector feedBackMessage = new Vector();

		for (int i = 0; i < dataVector.size(); i++) {
			Boolean isParentPartBoolean = false;// 用于判断父子件关系
			ChangeBOMObject changeBOMObject = (ChangeBOMObject) dataVector.get(i);
			String childpartnumber = changeBOMObject.getChildnumber();
			String parentpartnumber = changeBOMObject.getParentnumber();
			WTPart childPart = null;
			WTPart parentPart1 = null;
			try {
				childPart = PartUtil.getLastestWTPartByNumber(childpartnumber);
				parentPart1 = PartUtil.getLastestWTPartByNumber(parentpartnumber);
				if (parentPart1 == null) {
					feedBackMessage
							.add("第" + changeBOMObject.getId() + "行" + changeBOMObject.getParentnumber() + "未找到该部件！");

				} /*
					 * else if
					 * (!parentPart1.getState().toString().equals("RELEASED")) {
					 * feedBackMessage .add("第" + changeBOMObject.getId() + "行"
					 * + changeBOMObject.getParentnumber() + "未发布不能修改"); }
					 */
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			WTPartMaster master = (WTPartMaster) childPart.getMaster();
			QueryResult parentQueryResult = new QueryResult();
			;
			try {
				parentQueryResult = getPartUsedBy(master);
			} catch (WTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			while (parentQueryResult.hasMoreElements()) {
				WTPart parentPart = (WTPart) parentQueryResult.nextElement();
				if (parentPart.getNumber().equals(changeBOMObject.getParentnumber())) {
					isParentPartBoolean = true;
				}

			}
			if (!isParentPartBoolean && !changeBOMObject.getChangetype().equals("增加"))// 如果是增加操作侧不验证这个关系
			{
				feedBackMessage.add("第" + changeBOMObject.getId() + "行" + changeBOMObject.getChildnumber() + "不是父子件关系");
			}
		}

		HashMap<String, String> nHashMap = new HashMap<String, String>(); // 用于判断料号是否重复
		HashMap<String, String> oHashMap = new HashMap<String, String>(); // 用于判断位号是否重复

		for (int i = 0; i < dataVector.size(); i++) {
			ChangeBOMObject changeBOMObject = (ChangeBOMObject) dataVector.get(i);
			/*
			 * if
			 * (nHashMap.containsKey(changeBOMObject.getChildnumber())&&nHashMap
			 * .containsValue(changeBOMObject.getParentnumber())) { String oldID
			 * = nHashMap.get(changeBOMObject.getChildnumber());
			 * nHashMap.put(changeBOMObject.getChildnumber(),oldID.trim().length
			 * ()==0?changeBOMObject.getId():oldID+","+changeBOMObject.getId());
			 * feedBackMessage.add("第"+changeBOMObject.getId()+"行"+
			 * changeBOMObject.getChildnumber()+"存在重复值"); } else {
			 * nHashMap.put(changeBOMObject.getChildnumber(),
			 * changeBOMObject.getParentnumber()); }
			 */

			WTPart aPart = null;

			try {
				aPart = PartUtil.getLastestWTPartByNumber(changeBOMObject.getChildnumber());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (aPart == null) {
				feedBackMessage
						.add("第" + changeBOMObject.getId() + "行子件" + changeBOMObject.getChildnumber() + "在系统中不存在");
			}

			try {
				if (WorkInProgressHelper.isWorkingCopy(aPart) || WorkInProgressHelper.isCheckedOut(aPart)) {
					feedBackMessage.add(
							"第" + changeBOMObject.getId() + "行子件" + changeBOMObject.getChildnumber() + "是检出状态不能创建BOM");
				}
			} catch (WTException e) {
				feedBackMessage
						.add("第" + changeBOMObject.getId() + "行子件" + changeBOMObject.getChildnumber() + "在系统查找过程中出错了");
				e.printStackTrace();
			}

			if (changeBOMObject.getChildnumber().equalsIgnoreCase(changeBOMObject.getParentnumber())) {
				feedBackMessage
						.add("第" + changeBOMObject.getId() + "行子件" + changeBOMObject.getChildnumber() + "与父件相同不符合业务规定");
			}

			if (changeBOMObject.getOccurrence().length() > 0) {
				StringTokenizer st = new StringTokenizer(changeBOMObject.getOccurrence(), ",");
				while (st.hasMoreTokens()) {
					String subStr = st.nextToken().trim();

					if (oHashMap.containsKey(subStr)) {
						String oldID = nHashMap.get(changeBOMObject.getChildnumber());
						System.out.println("oldID==" + oldID);
						System.out.println("subStr==" + subStr);
						System.out.println("changeBOMObject.getId()==" + changeBOMObject.getId());
						oHashMap.put(subStr,
								oldID == null ? changeBOMObject.getId() : oldID + "," + changeBOMObject.getId());
						feedBackMessage.add("第" + changeBOMObject.getId() + "行" + changeBOMObject.getQuantity() + ",位号"
								+ subStr + "存在重复值");
					} else {
						oHashMap.put(subStr, changeBOMObject.getId());
					}
				}
			}
		}

		// feedBackMessage.add(childObject.getNumber() + "在第" +
		// childObject.getID() + "," + (String)
		// tempHM.get(childObject.getNumber()) + "行重复");

		return feedBackMessage;
	}

	protected boolean isDouble(String str) {
		try {
			double quantity = Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 仅用于单个Part的创建ChangeRequest Action菜单校验
	 */
	public static boolean checkChangeStatus(Changeable2 changeable2) {
		return true;
	}

	/**
	 * 如果有变更请求!=已取消或已解决，许可 如果有变更通知!=已解决，许可
	 * 默认值为true,表示不存在正在运行中的变更对象，包括ECR，ECN，ECA，如果存在以上变更，值为false
	 * 加上primaryBusinessObject为自已的判断，避免系统状态为返工时重复提交时，干涉检查不通过的情况,
	 * primaryBusinessObject为空时，仅用于在单个Part的创建ChangeRequest Action菜单校验
	 */

}
