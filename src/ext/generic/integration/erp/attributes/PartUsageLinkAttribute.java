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
import java.util.Set;

import wt.inf.container.WTContainer;
import wt.method.RemoteAccess;
import wt.part.WTPart;
import wt.part.WTPartUsageLink;
import wt.util.WTException;
import wt.vc.baseline.Baseline;
import wt.vc.baseline.ManagedBaseline;
import ext.com.iba.IBAUtil;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.generic.wfbaseline.util.BaselineUtil;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIPartHelper;

/**
 * 
 * 本类作用：获取零部件Link属性，即BOM结构属性
 * 
 * @author 魏文杰
 * @Version 1.0
 * @Date 2013-10-08
 */
public class PartUsageLinkAttribute implements RemoteAccess {
	// 父件编号
	public static final String PARENT_PART_NUMBER = "parentNumber";

	// 父件视图
	public static final String PARENT_PART_VIEW = "parentView";

	// 父件大版本
	public static final String PARENT_PART_MAJOR_VERSION = "parentMajorVersion";

	// 父件完整版本
	public static final String PARENT_PART_VERSION = "parentVersion";

	// 子件编号
	public static final String CHILD_PART_NUMBER = "childNumber";
	
	// 子件版本
	public static final String CHILD_PART_VERSION = "childVersion" ;

	public static final String LINE_NUMBER = "lineNumber";

	private static final String FIND_NUMBER = "findNumber";

	private static final String QUANTITY = "quantity";

	private static final String UNIT = "unit";

	private static final String TRACE_CODE = "traceCode";

	private static final String OCCURRENCE = "occurrence";

	private static final String STATE = "state.state";

	private static final String OID = "oid";

	/**
	 * 获取属性
	 * 
	 * @param usageLink
	 * @param attributeName
	 * @return
	 * @throws WTException
	 */
	protected static String getAttribute(WTPartUsageLink usageLink, String attributeName) throws WTException {
		String attributeValue = "";

		try {
			if (attributeName.equals(PARENT_PART_NUMBER)) {

				attributeValue = CommonPDMUtil.getParentNumber(usageLink);

			} else if (attributeName.equals(PARENT_PART_VIEW)) {

				attributeValue = CommonPDMUtil.getParentView(usageLink);

			} else if (attributeName.equals(PARENT_PART_MAJOR_VERSION)) {

				attributeValue = CommonPDMUtil.getParentMajorVersion(usageLink);

			} else if (attributeName.equals(PARENT_PART_VERSION)) {

				attributeValue = CommonPDMUtil.getParentVersion(usageLink);

			} else if (attributeName.equals(CHILD_PART_NUMBER)) {

				attributeValue = CommonPDMUtil.getChildNumber(usageLink);

			} else if (attributeName.equals(CHILD_PART_VERSION)){
				
				WTPart childPart = PIPartHelper.service.findWTPart(usageLink.getUses(), usageLink.getUsedBy().getViewName()) ;
				if(childPart != null){
					return childPart.getVersionIdentifier().getValue() ;
				}
				
			} else if (attributeName.equals(FIND_NUMBER)) {

				attributeValue = usageLink.getFindNumber();

			} else if (attributeName.equals(LINE_NUMBER)) {

				attributeValue = CommonPDMUtil.getLineNumber(usageLink);

			} else if (attributeName.equals(QUANTITY)) {

				attributeValue = CommonPDMUtil.getQuantityValue(usageLink);

			} else if (attributeName.equals(UNIT)) {

				attributeValue = CommonPDMUtil.getERPMappingUnit(usageLink);

			} else if (attributeName.equals(TRACE_CODE)) {

				attributeValue = CommonPDMUtil.getTraceCode(usageLink);

			} else if (attributeName.equals(OCCURRENCE)) {

				attributeValue = CommonPDMUtil.getOccurrence(usageLink);

			} else if (attributeName.equals(OID)) {

				attributeValue = CommonPDMUtil.getObjectID(usageLink) + "";

			} else if (attributeName.equals("ssgs")) {

				if (usageLink != null) {
					WTPart parent = usageLink.getUsedBy();
					if (parent != null) {
						WTContainer container = parent.getContainer();
						attributeValue = (String) PIAttributeHelper.service.getValue(container, "ssgs");
					}
				}
			} else if (attributeName.equals("baselineNumber")) {
				
				if (usageLink != null) {
					WTPart parent = usageLink.getUsedBy();
					if (parent != null) {
						BaselineUtil util = new BaselineUtil();
						Set<Baseline> set = util.getWTObjectBaselineLinks(parent);
						Iterator<Baseline> iterator = set.iterator();
						//编号比对
						int number = 0;
						while(iterator.hasNext()) {
							ManagedBaseline next = (ManagedBaseline) iterator.next();
							System.out.println("基线号是:"+next);
							String number2 = next.getNumber();
							Integer bNumber = Integer.valueOf(number2);
							if(bNumber>=number) {
								number = bNumber;
								attributeValue = number2;
							}
						}
					}
				}
				System.out.println("基线号是:"+attributeValue);
			} else if (attributeName.equals("sfxnj")) {
				if (usageLink != null) {
					WTPart parent = usageLink.getUsedBy();
					if (parent != null) {
						System.out.println("parent===" + parent.getName());
						Object obj = PIAttributeHelper.service.getValue(parent, "sfxnj");
						if (obj != null && obj instanceof Boolean) {
							boolean value = (boolean) obj;

							if (value) {
								attributeValue = "是";
							}
						}
					}
				}
			} else if (attributeName.equals(STATE)) {

				if (usageLink != null) {
					WTPart parent = usageLink.getUsedBy();
					if (parent != null) {
						attributeValue = parent.getState().toString();
					}
				}
			} else {
				attributeValue = (String) IBAUtil.getIBAValue(usageLink, attributeName);
			}

			// 当结构的属性在WTPartUsageLink上取不到时，从WTPart上取。
			// 此代码可能根据不同的业务需求，会导致BUG
			if (attributeValue == null || attributeValue.trim().equals("")) {
				WTPart parent = usageLink.getUsedBy();
				attributeValue = PartAttribute.getAttribute(parent, attributeName);
				// 修改原因：料号生效地属性如果获取不到并且当前零部件视图不为“Design”时，使用当前零部件视图来给料号生产地赋值。
				if (attributeName.equals("partNumberEffectiveSite")) {

					if (attributeValue == null || attributeValue.trim().equals("")) {

						String view = parent.getViewName();

						if (view != null && !view.trim().isEmpty() && !view.trim().equals("Design")) {
							attributeValue = view.trim();
						}
					}
				}
			}
		} catch (WTException wte) {
			throw new WTException(wte);
		} catch (RemoteException e) {
			e.printStackTrace();
		} finally {

		}

		return attributeValue;
	}
}
