/** *********************************************************************** */
/*                                                                          */
/* Copyright (c) 2008-2012 YULONG Company */
/* 宇龙计算机通信科技（深圳）有限公司版权所有 2008-2012 */
/*                                                                          */
/* PROPRIETARY RIGHTS of YULONG Company are involved in the */
/* subject matter of this material. All manufacturing, reproduction, use, */
/* and sales rights pertaining to this subject matter are governed by the */
/* license agreement. The recipient of this software implicitly accepts */
/* the terms of the license. */
/* 本软件文档资料是宇龙公司的资产，任何人士阅读和使用本资料必须获得 */
/* 相应的书面授权，承担保密责任和接受相应的法律约束。 */
/*                                                                          */
/** *********************************************************************** */

/**
 * <pre>
 * 系统缩写：PLM 
 * 系统名称：产品生命周期管理系统 
 * 组件编号：C_知识管理
 * 组件名称：知识管理
 * 文件名称：TypeDefinitionUtil.java 
 * 作         者: 马学游
 * 生成日期：2011-11-16
 * </pre>
 */

/**
 * <pre>
 * 修改记录：01 
 * 修改日期：2011-11-16
 * 修   改  人：马学游 
 * 关联活动：IT-DB00042042 C_知识管理_新checkList存储管理_代码开发_maxueyou
 * 修改内容：初始化
 * </pre>
 */

/**
 * <pre>
 * 修改记录：02 
 * 修改日期：2012-3-8
 * 修  改   人：毛兵义 
 * 关联活动：IT-DB00047158 C_集成_PLM物料主数据接口字段变更_机型信息获取功能开发_maobingyi
 * 修改内容：添加方法null2String(String str)
 * </pre>
 */

/**
 * <pre>
 * 修改记录：03 
 * 修改日期：2012-9-13
 * 修  改   人：裴均宇 
 * 关联活动：IT-DB00054715　C_知识管理_电子物料选型硬件评估要素表的E化_功能开发_peijunyu
 * 修改内容：添加方法parseToHashCode(String str),parseHashCodeToString(String str)
 * </pre>
 */
package ext.appo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 此类用于提供与字符串相关的工具方法
 */
public class StringUtil {

	/**
	 * 判断字符串是否为空字符串或者NULL
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmptyOrNull(String str) {

		boolean flag = false;

		if (null == str) {
			flag = true;
		} else {
			if ("".equals(str.trim())) {
				flag = true;
			}
		}

		return flag;
	}

	/**
	 * 判断字符串是否为空字符串
	 * 
	 * @param value
	 * @return
	 */
	public static boolean isEmptyString(String str) {

		boolean flag = false;

		if (null != str) {
			if ("".equals(str.trim())) {
				flag = true;
			}
		}

		return flag;
	}

	/**
	 * 根据length,获取字符串前面长度为length的子串，如果字符串的长度少于length，则返回本身
	 * 
	 * @param str
	 * @param length
	 * @return
	 */
	public static String getSubstring(String str, int length) {

		String substring = null;

		if (null != str && str.length() > length) {
			substring = str.substring(0, length);
		} else {
			substring = str;
		}

		return substring;
	}

	/**
	 * 字符串对象获取（若为NULL转“”）
	 * 
	 * @param str
	 * @return
	 */
	public static String null2String(String str) {

		if (str != null) {
			if (str.length() >= 1) {
				return str.trim();
			}
			return str;
		} else {
			return "";
		}
	}

	/**
	 * 把字符串每个字符解释成HashCode，并以逗号隔开
	 * 
	 * @param str
	 * @return
	 */
	public static String parseToHashCode(String str) {

		// 返回结果
		String result = "";

		if (!StringUtil.isEmptyOrNull(str)) {
			int len = str.length();
			char c;
			// 偱环每个字符，并取得其HashCode
			for (int i = 0; i < len; i++) {
				// 取得字符
				c = str.charAt(i);
				Integer k = Integer.valueOf(c);
				// 以逗号分开
				result = result + k.intValue() + ",";
			}
		}
		return result;
	}

	/**
	 * 把HashCode转成字符并组成字符串（参数只符串只能包含数字或逗号字符）
	 * 
	 * @param str
	 * @return
	 */
	public static String parseHashCodeToString(String str) {

		// 返回结果
		String result = "";

		if (!StringUtil.isEmptyOrNull(str)) {
			// 以逗号分隔
			String array[] = str.split(",");
			Integer k;
			char c;
			// 偱环每个字符，并转成字符
			for (int i = 0; i < array.length; i++) {
				k = Integer.valueOf(array[i]);
				c = (char) k.intValue();
				result = result + c;
			}
		}
		return result;
	}

	/**
	 * 将字符串安装分隔符进行分隔返回数组
	 * 
	 * @param str
	 * @param delimiter
	 * @return
	 */
	public static String[] split(String str, String delimiter) {

		if (str == null) {
			return new String[0];
		}
		StringTokenizer st = new StringTokenizer(str, delimiter);
		List<String> tokens = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			token = token.trim();
			if (token.length() > 0) {
				tokens.add(token);
			}
		}
		return tokens.toArray(new String[tokens.size()]);
	}
}
