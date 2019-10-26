package ext.appo.util;

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
 * 组件编号：C_系统管理
 * 组件名称：系统管理
 * 文件名称：NumberUtil.java 
 * 作         者: 裴均宇
 * 生成日期：2011-09-23
 * </pre>
 */

/**
 * <pre>
 * 修改记录：01 
 * 修改日期：2011-09-23
 * 修  改   人：裴均宇
 * 关联活动：IT-DB00040918 C_系统管理_邮件跟催_代码开发_peijunyu
 * 修改内容：初始化
 * </pre>
 */

/**
 * <pre>
 * 修改记录：02 
 * 修改日期：2013-02-19
 * 修  改   人：裴均宇
 * 关联活动：C_器件管理_Windchill中BOM自动导入功能_代码开发_peijunyu
 * 修改内容：新增字符是否为数字的方法
 * </pre>
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberUtil
{
    
    public static final String PATTERN = "[0-9]*";
    // 数字正则表达式
    public static final String REG = "[\\d.]+";
    // 支持1.0这种格式的判断;缺点是不支持负数
    public static final String PATTERN_INT = "^(\\d+)\\.?[0]*$";
    
    /**
     * 判断字符串是否为整数格式
     * 
     * @param str
     * @return
     */
    public static boolean isNumberic(String str)
    {

        if (str == null || str.equals(""))
        {
            return false;
        }
        else
        {
            Pattern pattern = Pattern.compile(PATTERN);
            Matcher isNum = pattern.matcher(str);
            if (!isNum.matches())
            {
                return false;
            }
            return true;
        }
    }
    
    /**
     * 判断一个字符串是否是数字格式
     * 
     * @param value
     * @return
     */
    public static boolean isNumber(String value)
    {

        if (StringUtil.isEmptyOrNull(value))
        {
            return false;
        }
        return value.matches(REG);
    }
    
    /**
     * 判断字符串是否为整数格式
     * @param str
     * @return
     */
    public static boolean isNumbericInt(String str)
    {

        if (str == null || str.equals(""))
        {
            return false;
        }
        else
        {
            Pattern pattern = Pattern.compile(PATTERN_INT);
            Matcher isNum = pattern.matcher(str);
            if (!isNum.matches())
            {
                return false;
            }
            return true;
        }
    }
    
    /**
     * 判断double是否可以转化为整数格式,效率更高
     * @param double
     * @return
     */
    public static boolean isInt(double value)
    {
        return (value % 1d) == 0d;
    }
}
