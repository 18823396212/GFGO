package ext.generic.mergeproperties.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ext.com.core.CoreUtil;
import ext.com.iba.IBAUtil;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTMessage;
import wt.util.WTProperties;

/**
 * 属性合并功能 MergeProperties
 * 
 * @author Administrator
 *
 */
public class MergeProperties implements RemoteAccess {
	private static final String CLASSNAME = MergeProperties.class.getName();
	private static Logger logger = LogR.getLogger(CLASSNAME);

	public static final String RESOURCE = "ext.generic.mergeproperties.resource.MergepropertiesRB";

	// private static String PropertiesConfig =
	// "ext.generic.mergeproperties.config.mergeproperties";

	private static final String TARGET_PROPERTIES = "targetProperties";
	private static final String RESULT_MAX_LENGTH = "ResultMaxLength";
	private static final String MERGE_TYPE = "MergeType";
	private static final String MERGE_RULE = "MergeRule";
	private static final String MERGE_SEPARATOR = "MergeSeparator";
	private static final String SHOW_ATTRIBUTEINAME = "ShowAttributeName";
	private static final String DEFAULT_SEPARATOR = "_";

	private static String configFile = "";// XML配置文件的路径
	public static WTProperties wtproperties;

	static {
		try {
			wtproperties = WTProperties.getLocalProperties();
			configFile = wtproperties.getProperty("wt.home");
			configFile += File.separator + "codebase" + File.separator + "ext" + File.separator + "generic"
					+ File.separator + "mergeproperties" + File.separator + "config" + File.separator
					+ "mergeproperties.properties";
		} catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
	}

	// 需要合并的目标属性
	private String targetProperties = "";

	// 合并结果允许的最大长度
	private String resultMaxLength = "";

	// 属性合并类型
	private String mergeType = "";

	// 属性合并规则
	private String mergeRule = "";

	// 属性合并分隔符
	private String mergeSeparator = "";

	// 是否显示属性逻辑标识符
	private String showAttributeName = "";

	/**
	 * 根据部件对象去获取具体属性并设置
	 * 
	 * @param wtpart
	 *            需要合并的属性对象
	 * @throws WTException
	 *             exp
	 */
	public void setProperties(WTPart wtpart) throws WTException {

		if (wtpart == null)
			return;

		// 错误信息
		String errorMessage = "";

		// 重新读取属性
		readProerties();
		Locale locale = SessionHelper.manager.getLocale();
		// 如果WTPart不是最新版本，则获取最新版本的数据
		if (!wtpart.isLatestIteration()) {
			wtpart = CoreUtil.getWTPartByNumberAndView(wtpart.getNumber(), wtpart.getViewName());
		}

		if (targetProperties == null || targetProperties.isEmpty()) {
			// errorMessage += "属性合并配置文件中targetProperties值为空。";
			errorMessage += WTMessage.getLocalizedMessage(RESOURCE, "MERGE_ERR_TARGETPROPERTIES", null, locale);
		}

		if (resultMaxLength == null || resultMaxLength.isEmpty()) {
			resultMaxLength = "";
		}

		if (mergeRule == null || mergeRule.isEmpty()) {
			// errorMessage += "属性合并配置文件中mergeRule值为空。";
			errorMessage += WTMessage.getLocalizedMessage(RESOURCE, "MERGE_ERR_MERGERULE", null, locale);
		}

		if (mergeSeparator == null || mergeSeparator.isEmpty()) {
			mergeSeparator = DEFAULT_SEPARATOR;
		}

		if (showAttributeName == null || showAttributeName.isEmpty()) {
			showAttributeName = "0";
		}

		if (!errorMessage.isEmpty()) {
			throw new WTException(errorMessage);
		}

		// 属性合并
		doMergeProperties(wtpart);
	}

	/*
	 * 从配置文件中读取所有的配置属性
	 */
	private void readProerties() {

		Map propertiesMap = readProperties(configFile);
		if (propertiesMap != null) {
			if (propertiesMap.containsKey(TARGET_PROPERTIES)) {
				targetProperties = (String) propertiesMap.get(TARGET_PROPERTIES);
				targetProperties = targetProperties.trim();
			}

			if (propertiesMap.containsKey(RESULT_MAX_LENGTH)) {
				resultMaxLength = (String) propertiesMap.get(RESULT_MAX_LENGTH);
				resultMaxLength = resultMaxLength.trim();
			}

			if (propertiesMap.containsKey(MERGE_TYPE)) {
				mergeType = (String) propertiesMap.get(MERGE_TYPE);
				mergeType = mergeType.trim();
			}

			if (propertiesMap.containsKey(MERGE_RULE)) {
				mergeRule = (String) propertiesMap.get(MERGE_RULE);
				mergeRule = mergeRule.trim();
			}

			if (propertiesMap.containsKey(MERGE_SEPARATOR)) {
				mergeSeparator = (String) propertiesMap.get(MERGE_SEPARATOR);
				mergeSeparator = mergeSeparator.trim();
			}

			if (propertiesMap.containsKey(SHOW_ATTRIBUTEINAME)) {
				showAttributeName = (String) propertiesMap.get(SHOW_ATTRIBUTEINAME);
				showAttributeName = showAttributeName.trim();
			}

		}
	}

	/**
	 * 读取配置文件
	 * 
	 * @param propertityFile
	 *            文件名
	 * @return Map
	 */
	private Map readProperties(String propertityFile) {
		Properties prop = new Properties();

		InputStream inStream = null;

		try {
			inStream = new FileInputStream(propertityFile);
			BufferedReader bf = new BufferedReader(new InputStreamReader(inStream)); // modify
																						// by
																						// wwu
																						// 2015/01/19
			prop.load(bf);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeStream(inStream);
		}

		return prop;
	}

	/**
	 * 关闭InputStream文件流
	 * 
	 * @param inStream
	 *            文件流
	 */
	public static void closeStream(InputStream inStream) {
		try {
			if (inStream != null) {
				inStream.close();
			}
		} catch (IOException e) {
			inStream = null;
			e.printStackTrace();
		}
	}

	/**
	 * 实现属性的拼接
	 * 
	 * @param wtpart
	 *            需要拼接属性的部件
	 * @throws WTException
	 *             exp
	 */
	private void doMergeProperties(WTPart wtpart) throws WTException {
		logger.debug(" doMergeProperties start with rule:" + mergeRule);
		if (mergeRule == null || mergeRule.isEmpty())
			return;
		System.out.println("mergeRule=====" + mergeRule);
		// 如果是通过分类属性进行合并，则获取分类属性。
		if ("1".equalsIgnoreCase(mergeType)) {
			mergeRule = PartAttribute.getAttribute(wtpart, mergeRule);
		}
		System.out.println("mergeRule=====" + mergeRule);
		String[] sourceAttributes = mergeRule.split("[+]");
		String targetValue = "";
		List<String> mergeValues = new ArrayList<String>();
		for (int i = 0; i < sourceAttributes.length; i++) {
			String logicId = sourceAttributes[i];
			logicId = logicId.trim();
			if (!logicId.isEmpty()) {
				// 获取属性
				String value = PartAttribute.getAttribute(wtpart, logicId);

				if (/* value != null && !value.isEmpty() */ StringUtils.isNotBlank(value)) {
					if ("1".equals(showAttributeName)) {
						targetValue += logicId;
						targetValue += ":";

					} else if ("2".equals(showAttributeName)) {
						targetValue += PartAttribute.getAttributeDisplayName(wtpart, logicId);
						targetValue += ":";

					}
					logger.debug("i=" + i + ",,logicId=" + logicId + ",,value=" + value);
					mergeValues.add(value);
					// targetValue += value;
					// targetValue += mergeSeparator;
				}
			}
		}

		// 去掉最后的分隔符
		// if(targetValue.endsWith(mergeSeparator)){
		// targetValue = targetValue.substring(0, targetValue.length() - 1);
		// }
		System.out.println("mergeSeparator===" + mergeSeparator);
		targetValue = StringUtils.join(mergeValues, mergeSeparator);
		logger.debug("合并后结果targetValue=" + targetValue);
		// 判断合并后的结果是否超出了最大长度限制
		if (resultMaxLength != null && !resultMaxLength.isEmpty()) {
			long iMaxLength = 4000;// 数据库存储最大字符数是4000
			try {
				iMaxLength = Long.valueOf(resultMaxLength).longValue();

			} catch (NumberFormatException e) {
				e.printStackTrace();
			}

			int iResultLength = targetValue.getBytes().length;
			// 英文1：1，中文1：3
			if (iResultLength > iMaxLength) {
				// throw new WTException("合并后的结果长度为：" + iResultLength +
				// "，大于允许的最大长度：" + iMaxLength);
				throw new WTException(WTMessage.getLocalizedMessage(RESOURCE, "MERGE_ERR_OVEW_MAXLENGTH",
						new Object[] { iResultLength, iMaxLength }, SessionHelper.manager.getLocale()));
			}
		}
		System.out.println("targetProperties===" + targetProperties);
		System.out.println("targetValue===" + targetValue);
		logger.debug("  target logicalID:" + targetProperties + "  result:" + targetValue);
		IBAUtil.forceSetIBAValue(wtpart, targetProperties, targetValue);
		logger.debug(" doMergeProperties end !");
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
				RemoteMethodServer.getDefault().invoke("main", CLASSNAME, null, new Class[] { String[].class },
						new Object[] { args });
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return;
		}
		WTPart part = CoreUtil.getWTPartByNumberAndView(args[0], "Design");
		// setProperties(part);
	}

}
