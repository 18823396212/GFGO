package ext.generic.mergeproperties.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.PropertyResourceBundle;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ptc.core.lwc.client.commands.LWCCommands;
import com.ptc.core.lwc.common.view.AttributeDefinitionReadView;
import com.ptc.core.lwc.common.view.PropertyHolderHelper;
import com.ptc.core.lwc.server.LWCTypeDefinition;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.type.ClientTypedUtility;
import wt.util.WTException;
import wt.util.WTMessage;
import wt.util.WTPropertyVetoException;
import wt.vc.Iterated;
import wt.vc.config.ConfigHelper;
import wt.vc.config.ConfigSpec;
import wt.vc.config.LatestConfigSpec;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;

import ext.com.core.CoreUtil;
import ext.com.csm.CSMUtil;
import ext.com.iba.IBAUtil;
/**
 * 属性合并功能工具类
 * MergePropertiesUtil
 *  @author Administrator
 *	2014-12-4
 *
 */
public class MergePropertiesUtil implements RemoteAccess {
	private static final String CLASSNAME = MergePropertiesUtil.class.getName();
	private static String RESOURCE = "ext.generic.mergeproperties.resource.MergepropertiesRB";
	private static Logger LOGGER = null;
	private static Locale LOCALE = null;
	private static String PropertiesConfig = "ext.generic.mergeproperties.config.mergeproperties";

	private static final String CREATOR = "创建者";

	private static final String TRACE_CODE = "追踪代码";

	private static final String LIFECYCLE = "生命周期";

	private static final String MODIFIER = "修改者";

	private static final String MODIFY_TIME = "修改时间";

	private static final String NAME = "名称";

	private static final String NUMBER = "编号";

	private static final String SOURCE = "源";

	private static final String VIEW = "视图";

	// 零部件完整版本，包括大版本和小版本，通常形如：A.1
	private static final String VERSION = "版本";

	// 零部件大版本
	private static final String MAJOR_VERSION = "大版本";

	// 零部件小版本
	private static final String MINOR_VERSION = "小版本";

	// 零部件软类型
	private static final String PART_SOFT_TYPE = "软类型";
	
	//分类
	private static final String CLASSFIFCATION="分类";

	static {
		try {
			LOGGER = LogR.getLogger(CLASSNAME);
			LOCALE = SessionHelper.manager.getLocale();
		} catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
	}

	/**
	 * 获取properties中的值
	 * 
	 * @param propertityFile
	 *            properties的文件名
	 * @param keyInProperties
	 *            properties的key值
	 * @return String properties的值
	 **/
	public static String getPropertiesValue(String propertityFile, String keyInProperties) {
		String valueInProperties = null;
		PropertyResourceBundle prBundle = (PropertyResourceBundle) PropertyResourceBundle.getBundle(propertityFile);
		try {
			byte[] temp = null;
			temp = keyInProperties.getBytes("GB2312");
			keyInProperties = new String(temp, "ISO-8859-1");
			temp = prBundle.getString(keyInProperties).getBytes("ISO-8859-1");
			valueInProperties = new String(temp, "GB2312");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return valueInProperties;
	}

	/**
	 * 根据部件对象去获取具体属性并设置
	 * 
	 * @param wtpart
	 *            需要合并的属性对象
	 * @throws WTException
	 *             exp
	 */
	public static void setProperties(WTPart wtpart) throws WTException {
		String type = null;
		String target = "";
		String rule = "";
		wtpart = getLatestPartByPartMaster((WTPartMaster) wtpart.getMaster());
		target = getPropertiesValue(PropertiesConfig, "targetProperties");
		type = getPropertiesValue(PropertiesConfig, "MergeType");
		rule = getPropertiesValue(PropertiesConfig, "MergeRule");
		if (target == null || target.trim().equals("") || type == null || type.trim().equals("") || rule == null || rule.trim().equals("")) {
			throw new WTException( WTMessage.getLocalizedMessage(RESOURCE, "NOT_COMPLETE", null, LOCALE));
		} else {
			LOGGER.debug("   target:" + target + "   MergeType:" + type + "   rule:" + rule + "    wtpart info:" + wtpart.getIdentity());
		}
		if (type.equals("0")) {
			doMergeProperties(wtpart, target, rule, false);
			// 直接通过内部获取和设置合并属性
		} else if (type.equals("1")) {
			rule = PartAttribute.getAttribute(wtpart, rule);
			doMergeProperties(wtpart, target, rule, true);
		}
	}

	/**
	 * 实现属性的拼接
	 * @param wtpart
	 *            需要拼接属性的部件
	 * @param target
	 *            目标属性
	 * @param rule
	 *            属性拼接规则
	 * @param flag
	 *            是否需要通过显示名称获得逻辑标识符
	 * @throws WTException exp
	 */
	private static void doMergeProperties(WTPart wtpart, String target, String rule, boolean flag) throws WTException {
		LOGGER.debug(" doMergeProperties start with rule:" + rule);
		if(rule == null)
			return; 
		
		rule = rule.trim();
		//modified by zhouhaiwei on 20150911,如果合并规则为空，直接返回，不报错；
		//支持合并规则中只有一个属性（不包含+）
		if("".equals(rule))
			return; 
			
//		if (!rule.contains("+")) {
//			throw new WTException( WTMessage.getLocalizedMessage(RESOURCE, "RULE_NOT_COMPLETE", null, LOCALE));
//		}
		//
		String[] str = rule.split("[+]");
		String results = "";
		String partType = getPartExternalType(wtpart);
		for (int i = 0; i < str.length; i++) {
			String logicId = str[i];
			if (flag)
				logicId = getIBANamesAndDisplays(partType, str[i]);
			LOGGER.debug(" logical identifier:" + logicId);
//			if (logicId==null||logicId.equals("")) {
//				throw new WTException(str[i] + WTMessage.getLocalizedMessage(RESOURCE, "NOT_EXIST_SYS", null, LOCALE));
//			}
			if(logicId==null||logicId.equals("")){
				//获取不到logicId,可能是分类属性
				logicId = str[i];
			}
			
			if (i == 0)
				results = PartAttribute.getAttribute(wtpart, logicId);
			else
				results = results + "_" + PartAttribute.getAttribute(wtpart, logicId);
		}
		LOGGER.debug("  target logicalID:" + target + "  result:" + results);
		IBAUtil.forceSetIBAValue(wtpart, target, results);
		LOGGER.debug(" doMergeProperties end !");
	}

	/**
	 * 
	 * 获得对象的软类型内部名称
	 * 
	 * @param obj 对象
	 * 
	 * @return wt.part.WTPart
	 * 
	 * @throws WTException exp
	 */
	public static String getPartExternalType(WTObject obj) throws WTException {
		String moduleName = "";
		WTPart part = null;
		if (obj == null) {
			LOGGER.debug("obj is null");
		}
		if (!(obj instanceof WTPart)) {
			LOGGER.debug("obj is not instance of WTDocument");
		}
		if (obj != null && obj instanceof WTPart)
			part = (WTPart) obj;
		try {
			String getExternalTypeIdentifier = ClientTypedUtility.getExternalTypeIdentifier(part);
			LOGGER.debug(" getExternalTypeIdentifier: " + getExternalTypeIdentifier);
			int beginIndex = getExternalTypeIdentifier.lastIndexOf("|");
			beginIndex = beginIndex + 1;
			moduleName = getExternalTypeIdentifier.substring(beginIndex);
		} catch (Exception e) {
			throw new WTException(e);
		}
		return moduleName;
	}

	/**
	 * 通过一个类型的显示名称获取逻辑标识符
	 * 
	 * @param type  wt.part.WTPart
	 * @param displayName 显示名称
	 * @return 逻辑标识符
	 * @throws WTException exp
	 */
	public static String getIBANamesAndDisplays(String type, String displayName) throws WTException {
		if (displayName.equals(CREATOR)) {

			return "creator";

		} else if (displayName.equals(TRACE_CODE)) {

			return "traceCode";

		} else if (displayName.equals(LIFECYCLE)) {

			return "lifecycle";

		} else if (displayName.equals(MODIFIER)) {

			return "modifier";

		} else if (displayName.equals(MODIFY_TIME)) {

			return "modifyTime";

		} else if (displayName.equals(NAME)) {

			return "name";

		} else if (displayName.equals(NUMBER)) {

			return "number";

		} else if (displayName.equals(SOURCE)) {

			return "source";

		} else if (displayName.equals(VIEW)) {

			return "view";

		} else if (displayName.equals(MAJOR_VERSION)) {

			return "majorVersion";

		} else if (displayName.equals(MINOR_VERSION)) {

			return "minorVersion";

		} else if (displayName.equals(VERSION)) {

			return "version";

		} else if (displayName.equals(PART_SOFT_TYPE)) {

			return "part_type";
		}else if (displayName.equals(CLASSFIFCATION)) {

			return "Classfifcation";
		}
		ReferenceFactory rf = new ReferenceFactory();
		try {
			WTUser curUser = (WTUser) SessionHelper.manager.getPrincipal();
			// 临时授予当前用户权限
			SessionHelper.manager.setAdministrator(); // 设为管理员
			Locale locale = SessionHelper.getLocale();
			QuerySpec qs = new QuerySpec(LWCTypeDefinition.class);
			qs.appendWhere(new SearchCondition(LWCTypeDefinition.class, LWCTypeDefinition.NAME, SearchCondition.EQUAL, type));
			QueryResult qr = PersistenceHelper.manager.find(qs);
			if (qr.hasMoreElements()) {
				LWCTypeDefinition obj = (LWCTypeDefinition) qr.nextElement();
				String oid = rf.getReferenceString(obj);
				ArrayList<AttributeDefinitionReadView> localArrayList = LWCCommands.getTypeAttributes(oid);
				AttributeDefinitionReadView t = null;
				for (int i = 0; i < localArrayList.size(); i++) {
					t = localArrayList.get(i);
					if (t.getIBARefView() != null) {
						String logicId = PropertyHolderHelper.getName(t);
						String name = PropertyHolderHelper.getDisplayName(t, locale);
						LOGGER.debug(" DisplayName:" + name + "  logicId:" + logicId);
						if (name.equals(displayName)) {
							return logicId;
						}
					}
				}
			}
			SessionHelper.manager.setPrincipal(curUser.getName());// 设回当前用户
		} catch (WTException e) {
			e.printStackTrace();
			throw e;
		}
		return null;
	}

	/**
	 * 读取excel
	 * 
	 * @param path
	 *            excel路径
	 * @param part
	 *            需要合并属性的部件
	 * @return 合并属性
	 * @throws IOException
	 *             exp
	 * @throws WTException
	 *             exp
	 * @throws WTPropertyVetoException
	 *             exp
	 */
	public static String readSheet(String path, WTPart part) throws IOException, WTException, WTPropertyVetoException {

		File file = new File(path);
		InputStream in = new FileInputStream(file);
		XSSFWorkbook wb = new XSSFWorkbook(in);
		XSSFSheet sheet = wb.getSheetAt(0);
		int rowcount = sheet.getLastRowNum();
		String result = "";
		for (int i = 0; i < rowcount; i++) {
			XSSFRow row = sheet.getRow(i);
			XSSFCell cell0 = row.getCell(0);
			XSSFCell cell1 = row.getCell(2);
			String name = CSMUtil.getOneLastClfNodeDisplayNameByWTPart(part);
			// String name="140004瞬变电压抑制二极管";
			String value = getCellValue(cell0);
			if (name != null) {
				if (name.equals(value)) {
					result = getCellValue(cell1);
				}
			} else {
				return "";
			}
		}
		return result;
	}

	/**
	 * 获取单元格中的信息
	 * 
	 * @param cell
	 *            单元格
	 * @return 主要参数信息
	 * @throws WTException
	 *             exp
	 */
	public static String getCellValue(Cell cell) throws WTException {
		String value = "";
		try {
			if (null == cell) {
				return "";
			}
			switch (cell.getCellType()) {
			case XSSFCell.CELL_TYPE_NUMERIC:
				value = Double.toString(cell.getNumericCellValue());
				break;
			case XSSFCell.CELL_TYPE_STRING:
				value = cell.getStringCellValue();
				break;
			case XSSFCell.CELL_TYPE_BLANK:
				value = "";
				break;
			case XSSFCell.CELL_TYPE_BOOLEAN:
				value = Boolean.toString(cell.getBooleanCellValue());
				break;
			case XSSFCell.CELL_TYPE_FORMULA:
				try {
					value = String.valueOf(cell.getNumericCellValue());
				} catch (Exception e) {
					value = cell.getStringCellValue();
				}
				break;
			default:
				break;
			}
		} catch (Exception e) {
		}
		if (value.indexOf(".") != -1) {
			value = value.substring(0, value.indexOf("."));
		}
		return value;
	}

	/**
	 * 根据零部件的master，获取零部件的最新小版本
	 * 
	 * @param partMaster
	 *            想要获取最新小版本的零部件的master
	 * @return 返回该master对应的最新小版本的零部件，如果master为空，返回空
	 * @throws WTException
	 *             异常
	 */
	private static WTPart getLatestPartByPartMaster(WTPartMaster partMaster) throws WTException {
		Iterated iter = null;
		boolean flag = false;
		LatestConfigSpec latestconfigspec = new LatestConfigSpec();
		// 根据零部件的master和latestconfigspec得到该master的所有小版本
		QueryResult queryresult = ConfigHelper.service.filteredIterationsOf(partMaster, latestconfigspec);
		if (queryresult != null && queryresult.size() <= 0) {
			ConfigSpec configspec = ConfigHelper.service.getDefaultConfigSpecFor(WTPartMaster.class);
			queryresult = ConfigHelper.service.filteredIterationsOf(partMaster, configspec);
		}
		while (queryresult.hasMoreElements() && (!flag)) {
			iter = (Iterated) (queryresult.nextElement());
			flag = iter.isLatestIteration();
			if (WorkInProgressHelper.isCheckedOut((Workable) iter)) {
				if (!WorkInProgressHelper.isWorkingCopy((Workable) iter)) {
					iter = (Iterated) WorkInProgressHelper.service.workingCopyOf((Workable) iter);
				}
			}
		}
		return (WTPart) iter;
	}

	/**
	 * 测试
	 * 
	 * @param args
	 *            参数
	 * @throws Exception
	 *             exp
	 */
	public static void main(String[] args) throws Exception {

		if (!RemoteMethodServer.ServerFlag) {
			try {
				RemoteMethodServer.getDefault().invoke("main", CLASSNAME, null, new Class[] { String[].class }, new Object[] { args });
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return;
		}
		WTPart part = CoreUtil.getWTPartByNumberAndView(args[0], "Design");
		setProperties(part);
	}
	
	
}
