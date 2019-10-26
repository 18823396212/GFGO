/**
 *
 * Copyright (c) 2017-2030 上海湃睿信息科技有限公司 (PISX). All Rights Reserved.
 *
 * This software is the confidential and proprietary information of PISX
 * and is subject to the terms of a software license agreement. You shall
 * not disclose such confidential information and shall use it only in accordance
 * with the terms of the license agreement.
 *
 */
package ext.generic.integration.erp.attributes;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ext.appo.util.excel.AppoExcelUtil;
import ext.com.csm.CSMUtil;
import ext.com.iba.IBAUtil;
import ext.customer.common.IBAUtility;
import ext.customer.common.MBAUtil;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.generic.integration.erp.util.BusinessRuleXMLConfigUtil;
import ext.generic.integration.erp.util.IntegrationConstant;
import ext.generic.wfbaseline.util.BaselineUtil;
import ext.pi.core.PIAttributeHelper;
import wt.fc.QueryResult;
import wt.inf.container.WTContainer;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.part.WTPart;
import wt.pom.PersistenceException;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.baseline.Baseline;
import wt.vc.baseline.ManagedBaseline;

/**
 * 
 * 本类作用：获取零部件属性
 * 
 * @author 魏文杰
 * @Version 1.0
 * @Date 2013-10-08
 */
public class PartAttribute implements RemoteAccess {
	private static final String CLASSNAME = PartAttribute.class.getName();
	private static final Logger logger = LogR.getLogger(CLASSNAME);

	public static final String CREATOR = "creator";

	public static final String TRACE_CODE = "traceCode";

	public static final String DEFAULT_UNIT = "defaultUnit";

	public static final String LIFECYCLE = "lifecycle";

	public static final String MODIFIER = "modifier";

	public static final String MODIFY_TIME = "modifyTime";

	public static final String NAME = "name";

	public static final String NUMBER = "number";

	public static final String SOURCE = "source";

	public static final String VIEW = "view";

	// 零部件完整版本，包括大版本和小版本，通常形如：A.1
	public static final String VERSION = "version";

	// 零部件大版本
	public static final String MAJOR_VERSION = "majorVersion";

	// 零部件小版本
	public static final String MINOR_VERSION = "minorVersion";

	// 零部件软类型
	public static final String PART_SOFT_TYPE = "part_type";

	public static final String RESOLUTION = "resolution";

	private static final String CLASSIFICATION = "Classification";

	private static final String CLASSIFICATIONEND = "ClassificationEnd";

	// 前一大版本状态
	public static final String BEFORE_VERSION_STATE = "beforeVersionState";

	// 生命周期中文
	public static final String LIFECYCLE_CN = "lifecycleCN";

	private static final String OID = "oid";

	public static final String CHANGETYPE = "ChangeType";// 变更类型

	/**
	 * 获取零部件属性信息
	 * 
	 * @param part
	 * @param attributeName
	 * @return
	 */
	protected static String getAttribute(WTPart part, String attributeName) {
		String attributeValue = "";

		try {
			if (attributeName.equals(CREATOR)) {

				attributeValue = CommonPDMUtil.getCreatorName(part);

			} else if (attributeName.equals(TRACE_CODE)) {

				attributeValue = CommonPDMUtil.getTraceCode(part);

			} else if (attributeName.equals(DEFAULT_UNIT)) {

				attributeValue = CommonPDMUtil.getERPMappingUnit(part);

			} else if (attributeName.equals(LIFECYCLE)) {

				attributeValue = CommonPDMUtil.getLifecycle(part);

			} else if (attributeName.equals(LIFECYCLE_CN)) {

				attributeValue = CommonPDMUtil.getLifecycleCN(part);

			} else if (attributeName.equals(MODIFIER)) {

				attributeValue = CommonPDMUtil.getAlternateName(part);

			} else if (attributeName.equals(MODIFY_TIME)) {

				attributeValue = CommonPDMUtil.getModifyTime(part);

			} else if (attributeName.equals(NAME)) {

				attributeValue = part.getName();

			} else if (attributeName.equals(NUMBER)) {

				attributeValue = part.getNumber();

			} else if (attributeName.equals(SOURCE)) {

				attributeValue = CommonPDMUtil.getSourceCN(part);

			} else if (attributeName.equals(VIEW)) {

				attributeValue = part.getViewName();

			} else if (attributeName.equals(MAJOR_VERSION)) {

				attributeValue = CommonPDMUtil.getMajorVersion(part);

			} else if (attributeName.equals(MINOR_VERSION)) {

				attributeValue = CommonPDMUtil.getMinorVersion(part);

			} else if (attributeName.equals(BEFORE_VERSION_STATE)) {
				// 获取前一个BOM发布成功的版本对象
				WTPart berfPart = findHistoryParts(part);

				if (berfPart != null) {
					attributeValue = CommonPDMUtil.getLifecycleCN(berfPart);
				}
			} else if (attributeName.equals(VERSION)) {

				attributeValue = CommonPDMUtil.getVersion(part);

			} else if (attributeName.equals(PART_SOFT_TYPE)) {

				attributeValue = CommonPDMUtil.getSoftType(part);

			}
			// 添加分类属性的获取，取分类名称
			else if (attributeName.equals(CLASSIFICATION)) {

				// attributeValue = CommonPDMUtil.getSoftType( part ) ;
				attributeValue = CSMUtil.getOneLastClfNodeDisplayNameByWTPart(part);

			} else if (attributeName.equals("ArticleDispose")) {
				Object obj = PIAttributeHelper.service.getValue(part, "ArticleDispose");
				if (obj != null && obj instanceof String) {
					String value = (String) obj;
					if (value.contains(";")) {
						String[] split = value.split(";");
						attributeValue = split[1];
					}

				}
			} else if (attributeName.equals("PassageDispose")) {
				Object obj = PIAttributeHelper.service.getValue(part, "PassageDispose");
				if (obj != null && obj instanceof String) {
					String value = (String) obj;
					if (value.contains(";")) {
						String[] split = value.split(";");
						attributeValue = split[1];
					}

				}
			} else if (attributeName.equals("InventoryDispose")) {
				Object obj = PIAttributeHelper.service.getValue(part, "InventoryDispose");
				if (obj != null && obj instanceof String) {
					String value = (String) obj;
					if (value.contains(";")) {
						String[] split = value.split(";");
						attributeValue = split[1];
					}

				}
			} else if (attributeName.equals("ProductDispose")) {
				Object obj = PIAttributeHelper.service.getValue(part, "ProductDispose");
				if (obj != null && obj instanceof String) {
					String value = (String) obj;
					if (value.contains(";")) {
						String[] split = value.split(";");
						attributeValue = split[1];
					}
				}
			} else if (attributeName.equals(CHANGETYPE)) {
				Object obj = PIAttributeHelper.service.getValue(part, "ChangeType");
				if (obj != null && obj instanceof String) {
					String value = (String) obj;
					if (value.contains(";")) {
						String[] split = value.split(";");
						attributeValue = split[1];
					}

				}
			} else if (attributeName.equals("ssgs")) {
				WTContainer container = part.getContainer();
				attributeValue = (String) PIAttributeHelper.service.getValue(container, "ssgs");

			} else if (attributeName.equals("baselineNumber")) {

				if (part != null) {
					BaselineUtil util = new BaselineUtil();
					Set<Baseline> set = util.getWTObjectBaselineLinks(part);
					Iterator<Baseline> iterator = set.iterator();
					// 编号比对
					int number = 0;
					while (iterator.hasNext()) {
						ManagedBaseline next = (ManagedBaseline) iterator.next();
						System.out.println("基线号是:" + next);
						String number2 = next.getNumber();
						Integer bNumber = Integer.valueOf(number2);
						if (bNumber >= number) {
							number = bNumber;
							attributeValue = number2;
						}
					}
				}
				System.out.println("基线号是:" + attributeValue);
			} else if (attributeName.equals("sfxnj")) {
				Object obj = PIAttributeHelper.service.getValue(part, "sfxnj");
				if (obj != null && obj instanceof Boolean) {
					boolean value = (boolean) obj;

					if (value) {
						attributeValue = "是";
					}
				}
			}

			// 添加分类属性的获取:读取配置表，按照配置的内容截取编号的对应长度
			else if (attributeName.equals(CLASSIFICATIONEND)) {
				String number = part.getNumber();
				String key = number.substring(0, 1);

				Map<String, String> readSheet1 = new AppoExcelUtil().readSheet1();
				String value = readSheet1.get(key);
				String classificationNo = number.substring(0, Integer.valueOf(value));
				attributeValue = classificationNo;

			}

			else if (attributeName.equals(OID)) {

				attributeValue = CommonPDMUtil.getObjectID(part) + "";
			}

			else if (attributeName.equals(RESOLUTION)) {
				String resolutionH = (String) IBAUtil.getIBAValue(part, "finishedGoodResolutionH");
				String resolutionV = (String) IBAUtil.getIBAValue(part, "finishedGoodResolutionV");

				if (resolutionH == null) {
					resolutionH = "";
				} else {
					resolutionH = resolutionH.trim();
				}

				if (resolutionV == null) {
					resolutionV = "";
				} else {
					resolutionV = resolutionV.trim();
				}

				if ((!resolutionH.equals("")) && (!resolutionV.equals(""))) {
					attributeValue = resolutionH + "X" + resolutionV;
				} else {
					attributeValue = resolutionH + resolutionV;
				}

			} else if (attributeName.equals("partInspectionStandard")) {

				String ibaValue = (String) IBAUtil.getIBAValue(part, attributeName);

				if (ibaValue != null && !ibaValue.isEmpty()) {
					attributeValue = EnumerationUtil.getEnumerationDisplayValue("检验标准", ibaValue);
				}

			} else if (attributeName.equals("finishedGoodDisplayMode")) {

				String ibaValue = (String) IBAUtil.getIBAValue(part, attributeName);

				if (ibaValue != null && !ibaValue.isEmpty()) {
					attributeValue = EnumerationUtil.getEnumerationDisplayValue("显示模式", ibaValue);
				}

			} // 修改原因：料号生效地属性如果获取不到并且当前零部件视图不为“Design”时，使用当前零部件视图来给料号生产地赋值。
			else if (attributeName.equals("partNumberEffectiveSite")) {

				String ibaValue = (String) IBAUtil.getIBAValue(part, attributeName);

				if (ibaValue == null || ibaValue.trim().isEmpty()) {

					String view = part.getViewName();

					if (view != null && !view.trim().isEmpty() && !view.trim().equals("Design")) {
						attributeValue = view.trim();
					}

				} else {
					attributeValue = ibaValue.trim();
				}

			} else {
				// Object valueObj = IBAUtil.getIBAValue(part, attributeName);
				// if( valueObj != null ){
				// attributeValue = (String) valueObj ;
				// }else{
				// attributeValue = "" ;
				// }
				// QS102-GenericManagement封装包中的类ext.customer.common.IBAUtility
				IBAUtility ibaUtil = new IBAUtility(part);
				if (ibaUtil.getAllIBAValues().containsKey(attributeName)) {

					attributeValue = ibaUtil.getIBAValue(attributeName);// 能够很好的处理多值和带单位的实数

				} else {// 非IBA属性或者是属性值为空的IBA属性
					Object valueObj = MBAUtil.getValue(part, attributeName);
					if (valueObj != null) {
						if (valueObj instanceof Object[]) {
							Object[] valueObjAry = (Object[]) valueObj;
							attributeValue = StringUtils.join(valueObjAry, ",");
						} else {
							attributeValue = valueObj.toString();
						}
					}

				}
			}
		} catch (WTException wte) {
			wte.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} finally {
			if (attributeValue == null) {
				attributeValue = "";
			}
		}

		return attributeValue;
	}

	/**
	 * 获取发布成功的历史版本，如果没有，则返回空
	 * 
	 * @param part
	 * @return
	 * @throws WTException
	 * @throws PersistenceException
	 * @throws RemoteException
	 */
	public static WTPart findHistoryParts(WTPart part) throws WTException, RemoteException {
		WTPart returnpart = null;

		if (part == null) {
			return returnpart;
		}

		// 根据part的主数据获取其所有的版本历史记录部件，且这些历史版本部件都已排序，排序方式为：B2、B1、A2、A1
		QueryResult result = VersionControlHelper.service.allIterationsOf(part.getMaster());
		String viewName = part.getViewName();

		while (result.hasMoreElements()) {
			WTPart hisPart = (WTPart) result.nextElement();
			String hisViewName = hisPart.getViewName();

			if (hisViewName.equals(viewName)) {
				String bomPublishState = "";
				Object iba = IBAUtil.getIBAValue(hisPart,
						BusinessRuleXMLConfigUtil.getInstance().getBomReleaseStatus());
				if (iba != null) {
					bomPublishState = iba.toString();
				}
				logger.debug(" >>> findHistoryParts() ... bomPublishState = " + bomPublishState);

				if (IntegrationConstant.PDM_RELEASE_STATUS_CN.equals(bomPublishState)
						|| IntegrationConstant.ERP_PROCESS_SUCCESS_CN.equals(bomPublishState)) {
					returnpart = hisPart;
					break;
				}
			}
		}
		return returnpart;
	}
}
