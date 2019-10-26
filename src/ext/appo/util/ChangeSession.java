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
 * 文件名称：ChangeSession.java 
 * 作         者: 马学游
 * 生成日期：2011-07-18
 * </pre>
 */

/**
 * <pre>
 * 修改记录：01 
 * 修改日期：2011-07-18
 * 修  改   人：马学游 
 * 关联活动：IT-DB00037590 C_流程管理_部件引进流程创建部件_代码开发_maxueyou
 * 修改内容：初始化
 * </pre>
 */

package ext.appo.util;

import org.apache.log4j.Logger;

import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.util.WTException;

/**
 * 用来切换session
 * 
 * @Author:
 * @Version 1.0 2009-2-11
 */

public class ChangeSession implements RemoteAccess
{
    
    private static final Logger log = Logger.getLogger(ChangeSession.class);
    
    /**
     * 切换到Administrator session
     * 
     * @throws WTException
     */
    public static WTUser changeToAdministratorSession()
    {

        WTUser user = null;
        
        try
        {
            user = (WTUser) SessionHelper.manager.getPrincipal();
            WTPrincipal wtadministrator = SessionHelper.manager.getAdministrator();
            SessionContext.setEffectivePrincipal(wtadministrator);
            SessionHelper.manager.setAdministrator();
        }
        catch (Exception e)
        {
            log.error("切换到管理员session出错!");
        }
        
        return user;
    }
    /**
     * 切换到某一指定的用户
     * @param userParam
     * @return
     */
    public static WTUser changeToWTUserSession(WTUser userParam)
    {

        WTUser user = null;
        
        try
        {
            if (userParam != null)
            {
                user = (WTUser) SessionHelper.manager.getPrincipal();
                SessionContext.setEffectivePrincipal(userParam);
                SessionHelper.manager.setPrincipal(userParam.getAuthenticationName());
            }
        }
        catch (Exception e)
        {
            log.error("切换到管理员session出错!");
        }
        
        return user;
    }
    
    /**
     * 切换到原来的session
     * 
     * @throws WTException
     */
    public static void changeToPreviousSession(WTUser user)
    {

        try
        {
            SessionContext.setEffectivePrincipal(user);
            SessionHelper.manager.setPrincipal(user.getAuthenticationName());
        }
        catch (Exception e)
        {
            log.error("切换回原来的session出错!");
        }
    }
}
