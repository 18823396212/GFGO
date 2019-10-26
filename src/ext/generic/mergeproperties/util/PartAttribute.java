package ext.generic.mergeproperties.util;

import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import wt.iba.definition.litedefinition.AttributeDefDefaultView;
import wt.method.RemoteAccess;
import wt.part.WTPart;
import wt.session.SessionHelper;
import wt.util.WTException;

import com.ptc.core.lwc.server.*;

import ext.com.csm.CSMUtil;
import ext.com.iba.IBADelegate;
import ext.com.iba.IBAUtil;
import ext.customer.common.IBAUtility;
import ext.customer.common.MBAUtil;
import ext.generic.mergeproperties.util.CommonPDMUtil;

/**
 * 本类作用：获取零部件属性
 * PartAttribute
 *  @author Administrator
 *	2014-12-5
 *
 */
public class PartAttribute implements RemoteAccess {

	private static final String CREATOR = "creator";

	private static final String TRACE_CODE = "traceCode";

	private static final String LIFECYCLE = "lifecycle";

	private static final String MODIFIER = "modifier";

	private static final String MODIFY_TIME = "modifyTime";

	private static final String NAME = "name";

	private static final String NUMBER = "number";

	private static final String SOURCE = "source";

	private static final String VIEW = "view";

	// 零部件完整版本，包括大版本和小版本，通常形如：A.1
	private static final String VERSION = "version";

	// 零部件大版本
	private static final String MAJOR_VERSION = "majorVersion";

	// 零部件小版本
	private static final String MINOR_VERSION = "minorVersion";

	// 零部件软类型
	private static final String PART_SOFT_TYPE = "part_type";
	
	//分类
	private static final String CLASSFIFCATION="Classfifcation";

	//显示名称
	private static final String CREATOR_NAME = "创建者";

	private static final String TRACE_CODE_NAME = "默认追踪代码";

	private static final String LIFECYCLE_NAME = "生命周期";

	private static final String MODIFIER_NAME = "修改者";

	private static final String MODIFY_TIME_NAME = "修改时间";

	private static final String NAME_NAME = "名称";

	private static final String NUMBER_NAME = "编号";

	private static final String SOURCE_NAME = "源";

	private static final String VIEW_NAME = "视图";

	// 零部件完整版本，包括大版本和小版本，通常形如：A.1
	private static final String VERSION_NAME = "版本";

	// 零部件大版本
	private static final String MAJOR_VERSION_NAME = "大版本";

	// 零部件小版本
	private static final String MINOR_VERSION_NAME = "小版本";

	// 零部件软类型
	private static final String PART_SOFT_TYPE_NAME = "软类型";
	
	//分类
	private static final String CLASSFIFCATION_NAME = "分类";
	
	

	/**
	 * 获取零部件属性信息
	 * 
	 * @param part 部件
	 * @param attributeName 属性
	 * @return 属性
	 */
	@SuppressWarnings("deprecation")
	protected static String getAttribute(WTPart part, String attributeName) {
		String attributeValue = "";

		try {
			if (attributeName.equals(CREATOR)) {

				attributeValue = CommonPDMUtil.getCreatorName(part);

			} else if (attributeName.equals(TRACE_CODE)) {

				attributeValue = CommonPDMUtil.getTraceCode(part);

			} else if (attributeName.equals(LIFECYCLE)) {

				attributeValue = CommonPDMUtil.getLifecycle(part);

			} else if (attributeName.equals(MODIFIER)) {

				attributeValue = CommonPDMUtil.getAlternateName(part);

			} else if (attributeName.equals(MODIFY_TIME)) {

				attributeValue = part.getModifyTimestamp().toGMTString().toString();

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

			} else if (attributeName.equals(VERSION)) {

				attributeValue = CommonPDMUtil.getVersion(part);

			} else if (attributeName.equals(PART_SOFT_TYPE)) {

				attributeValue = CommonPDMUtil.getSoftType(part);

			}else if (attributeName.equals(CLASSFIFCATION)){
				attributeValue = CSMUtil.getFullClfNodeDisplayPathByWTPart(part);
			}
			 else {
				//Object valueObj = IBAUtil.getIBAValue(part, attributeName);
				//带单位的实数存在问题，将getIBAValue方法换成getLocalizedIBANamesAndValues来获取属性值
//				 IBADelegate ibaDelegate = new IBADelegate(part);
//				 attributeValue = ibaDelegate.getIBAValueAsString(attributeName, SessionHelper.getLocale());
				 
				 //QS102-GenericManagement封装包中的类ext.customer.common.IBAUtility
				 IBAUtility ibaUtil = new IBAUtility(part);
				 if(ibaUtil.getAllIBAValues().containsKey(attributeName)){
					 
					 attributeValue = ibaUtil.getIBAValue(attributeName);//能够很好的处理多值和带单位的实数
					 
				 }else{//非IBA属性或者是属性值为空的IBA属性
					 Object valueObj = MBAUtil.getValue(part, attributeName);
					 if(valueObj != null){
						 if(valueObj instanceof Object[]){
							 Object[] valueObjAry = (Object[]) valueObj;
							 attributeValue = StringUtils.join(valueObjAry, ",");
						 }else{
							 attributeValue = valueObj.toString(); 
						 } 
					 }
					 
				 }
			}
		} catch (WTException wte) {
			wte.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (attributeValue == null) {
				attributeValue = "";
			}
		}
		return attributeValue;
	}
	
	/*
	 * 根据逻辑标识符获取属性显示名称 
	 * @param part 需要获取的对象
	 * @param attributeName 需要获取的属性名称
	 */
	protected static String getAttributeDisplayName(WTPart part, String attributeName) {
		
		String attributeDisplayName = "";

		try {
			if (attributeName.equals(CREATOR)) {

				attributeDisplayName = CREATOR_NAME;

			} else if (attributeName.equals(TRACE_CODE)) {

				attributeDisplayName = TRACE_CODE_NAME;

			} else if (attributeName.equals(LIFECYCLE)) {

				attributeDisplayName = LIFECYCLE_NAME;

			} else if (attributeName.equals(MODIFIER)) {

				attributeDisplayName = MODIFIER_NAME;

			} else if (attributeName.equals(MODIFY_TIME)) {

				attributeDisplayName = MODIFY_TIME_NAME;

			} else if (attributeName.equals(NAME)) {

				attributeDisplayName = NAME_NAME;

			} else if (attributeName.equals(NUMBER)) {

				attributeDisplayName = NUMBER_NAME;

			} else if (attributeName.equals(SOURCE)) {

				attributeDisplayName = SOURCE_NAME;

			} else if (attributeName.equals(VIEW)) {

				attributeDisplayName = VIEW_NAME;

			} else if (attributeName.equals(MAJOR_VERSION)) {

				attributeDisplayName = MAJOR_VERSION_NAME;

			} else if (attributeName.equals(MINOR_VERSION)) {

				attributeDisplayName = MINOR_VERSION_NAME;

			} else if (attributeName.equals(VERSION)) {

				attributeDisplayName = VERSION_NAME;

			} else if (attributeName.equals(PART_SOFT_TYPE)) {

				attributeDisplayName = PART_SOFT_TYPE_NAME;

			}else if (attributeName.equals(CLASSFIFCATION)){
				
				attributeDisplayName = CLASSFIFCATION_NAME;
				
			}
			 else {
				 
				 attributeDisplayName = IBAUtil.getAttributeDisplayName(attributeName);
				 
			}
		} catch (WTException wte) {
			wte.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (attributeDisplayName == null) {
				attributeDisplayName = "";
			}
		}
		return attributeDisplayName;
	}
	
	
	
//	 /**
//     * 数组值处理
//     * 
//     * @param obj
//     * @return
//     */
//    private static String getValue(Object obj) {
//        StringBuffer sb = new StringBuffer();
//        System.out.println("=========obj = "+obj);
//        if (obj instanceof Object[]) {
//            Object[] value = (Object[]) obj;
//            System.out.println("========value = "+value.length);
//            int length = value.length;
//            if (length == 1) {// 如果只有一个值，直接显示，无需下拉选择
//                sb.append(value[0]);
//            } else {// 如果为多值，默认为空，需要下拉选择
//                for (int j = 0; j < length; j++) {
//                    Object str = value[j];
//                    System.out.println("==========str = "+str);
////                    // if (sb.length() <= 0) {
////                    // sb.append(str);
////                    // } else
////                    {
//                        sb.append("," + str);
////                    }
//                }
//            }
//        } else {
//        	System.out.println("==========obj.toString() = "+obj.toString());
//            sb.append(obj.toString());
//        }
//        return sb.toString();
//    }
}
