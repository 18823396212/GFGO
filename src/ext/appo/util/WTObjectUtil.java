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
 * 文件名称：WTObjectUtil.java 
 * 作         者: 马学游
 * 生成日期：2011-07-12
 * </pre>
 */

/**
 * <pre>
 * 修改记录：01 
 * 修改日期：2011-07-12
 * 修  改   人：马学游 
 * 关联活动：IT-DB00037590 C_流程管理_部件引进流程创建部件_代码开发_maxueyou
 * 修改内容：初始化
 * </pre>
 */

package ext.appo.util;

import org.apache.log4j.Logger;

import wt.enterprise.Master;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.fc.WTReference;
import wt.pom.PersistenceException;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.vc.Iterated;
import wt.vc.VersionControlHelper;

/**
 * 此类用于提供与单纯对象相关的公共方法
 */
public class WTObjectUtil
{
    
    private static final Logger LOGGER = Logger.getLogger(WTObjectUtil.class);
    
    /**
     * 根据oid得到对象
     * 
     * @param oid
     * @return
     */
    public static WTObject getObjectByOid(String oid)
    {

        WTObject obj = null;
        try
        {
            ReferenceFactory referencefactory = new ReferenceFactory();
            WTReference wtreference = referencefactory.getReference(oid);
            if (wtreference != null && wtreference.getObject() != null)
            {
                obj = (WTObject) wtreference.getObject();
            }
        }
        catch (WTException e)
        {
            LOGGER.error(e);
        }
        return obj;
    }
    
    /**
     * 根据对象的name获取对象(不是最新小版本),
     * 
     * @param name 对象的名字
     * @param thisClass class对象
     * @return
     */
    @SuppressWarnings({ "deprecation", "rawtypes" })
    public static QueryResult getObjectByName(String name, Class thisClass)
    {

        QueryResult qr = null;
        try
        {
            // 根据thisClass的类型，找到对应的WTObject具体对象
            QuerySpec query = new QuerySpec(thisClass);
            String attribute = (String) thisClass.getField("NAME").get(thisClass);
            SearchCondition search = new SearchCondition(thisClass, attribute, SearchCondition.EQUAL, name);
            query.appendSearchCondition(search);
            qr = PersistenceHelper.manager.find(query);
        }
        catch (Exception e)
        {
            LOGGER.error(e);
        }
        
        return qr;
    }
    
    /**
     * 根据number获取对象(不是最新小版本)
     * 
     * @param number 对象的编号
     * @param thisClass class对象
     * @return 由number标识的对象
     */
    @SuppressWarnings("deprecation")
    public static QueryResult getObjectByNumber(String number, Class thisClass)
    {

        QueryResult qr = null;
        try
        {
            QuerySpec query = new QuerySpec(thisClass);
            String attribute = (String) thisClass.getField("NUMBER").get(thisClass);
            SearchCondition search = new SearchCondition(thisClass, attribute, SearchCondition.EQUAL, number);
            query.appendSearchCondition(search);
            qr = PersistenceHelper.manager.find(query);
        }
        catch (Exception e)
        {
            LOGGER.error(e);
        }
        
        return qr;
    }
    
    /**
     * 根据master获得最新版本的对象
     * 
     * @param master Master对象
     * @return 将Iterated对象强制转换为目标对象就可以了
     */
    public static Iterated getLatestPersistableByMaster(Master master)
    {

        Iterated iterated = null;
        
        try
        {
            // 获取master的所有版本
            QueryResult queryResult = VersionControlHelper.service.allVersionsOf(master);
            if (queryResult != null)
            {
                while (queryResult.hasMoreElements())
                {
                    Iterated temp = (Iterated) queryResult.nextElement();
                    
                    // 判断版本是否最新版本
                    if (temp.isLatestIteration())
                    {
                        iterated = temp;
                        break;
                    }
                }
            }
        }
        catch (WTException e)
        {
            LOGGER.error(e);
        }
        
        return iterated;
    }
}
