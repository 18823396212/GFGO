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
 * 组件编号：C_变更管理
 * 组件名称：变更管理
 * 文件名称：DateUtil.java 
 * 作         者: 马学游
 * 生成日期：2011-09-15
 * </pre>
 */

/**
 * <pre>
 * 修改记录：01 
 * 修改日期：2011-09-15
 * 修  改   人：马学游 
 * 关联活动：IT-DB00040091 C_变更管理_变更过程看板_代码开发_maxueyou
 * 修改内容：初始化
 * </pre>
 */

/**
 * <pre>
 * 修改记录：02 
 * 修改日期：2011-09-20
 * 修  改   人：马学游 
 * 关联活动：IT-DB00040336 C_xplm_系统管理_产品权限管控_代码开发_peijunyu
 * 修改内容：新增getDateAfter()方法
 * </pre>
 */
package ext.appo.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import wt.util.WTException;

/**
 * 此类用于提供时间转换相关的工具类
 */
public class DateUtil
{
    
    /**
     * 将格林时间转为当地时间，并转换格式（yyyy/MM/dd HH:mm）
     * 
     * @param timestamp
     * @return
     * @throws WTException
     */
    public static String changeGMTtoLocal(Timestamp timestamp)
    {

        String localTime = null;
        
        if (timestamp != null)
        {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timestamp.getTime());
            
            // 将格林时间转为北京时间
            cal.add(Calendar.HOUR, +8);
            
            // 格式化时间模式
            Date date = cal.getTime();
            localTime = formatDate(date, "yyyy/MM/dd HH:mm");
        }
        
        return localTime;
    }
    
    /**
     * 将格林时间转为当地时间，并转换给定格式(formatType)
     * 
     * @param timestamp
     * @return
     * @throws WTException
     */
    public static String changeGMTtoLocal(Date date, String formatType)
    {

        String localTime = null;
        
        if (date != null)
        {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(date.getTime());
            
            // 将格林时间转为北京时间
            cal.add(Calendar.HOUR, +8);
            
            // 格式化时间模式
            date = cal.getTime();
            localTime = formatDate(date, formatType);
        }
        
        return localTime;
    }
    
    /**
     * 格式化给定格式(formatType)的日期(Date)
     * 
     * @param date
     * @param formatType
     * @return
     */
    public static String formatDate(Date date, String formatType)
    {

        String formatString = null;
        
        if (null != date && !StringUtil.isEmptyOrNull(formatType))
        {
            DateFormat dateFormat = new SimpleDateFormat(formatType);
            formatString = dateFormat.format(date);
        }
        
        return formatString;
    }
    
    /**
     * 格式化给定格式(formatType)的日期(Timestamp)
     * 
     * @param timestamp
     * @param formatType
     * @return
     */
    public static String formatTimestamp(Timestamp timestamp, String formatType)
    {

        String formatString = null;
        
        if (null != timestamp && !StringUtil.isEmptyOrNull(formatType))
        {
            Date date = new Date(timestamp.getTime());
            formatString = formatDate(date, formatType);
        }
        
        return formatString;
    }
    
    /**
     * 获取某个日期的间隔天数的日期
     * 
     * @param date
     * @param timeSpace
     * @return
     */
    public static Date getDateAfter(Date date, int timeSpace)
    {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, timeSpace);
        return cal.getTime();
    }
    
}
