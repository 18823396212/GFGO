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
package ext.generic.integration.erp.ibatis;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import wt.fc.collections.WTKeyedHashMap;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.util.WTException;

import ext.generic.integration.erp.attributes.AttributeUtil;
import ext.generic.integration.erp.attributes.PartAttribute;
import ext.generic.integration.erp.bean.EBOMInfo;
import ext.generic.integration.erp.util.IbatisUtil;

import com.ibatis.sqlmap.client.SqlMapClient;
/**
 * 按父节点删除BOM结构表中的结构关系
 * 
 * 主要用于保证同一大版本的BOM结构，中间表只保留一条记录
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-10-08
 */
public class BomStructureRemoveByParentIbatis {
	private static final String clazz = BomStructureRemoveByParentIbatis.class.getName() ;
	private static final Logger logger = LogR.getLogger(clazz);
	
	/**
	 * 从中间表中删除BOM结构关系的信息
	 * 
	 * @param usageLink
	 * @throws SQLException 
	 * @throws WTException 
	 */
	public static WTKeyedHashMap delete( WTPart part ) throws SQLException, WTException{
		logger.debug("   "+"Entering In delete EBOM ( WTPart part )......") ;
		WTKeyedHashMap hashMap = new WTKeyedHashMap();
		if( part == null ){
			logger.debug("   "+"delete Method,WTPart is NULL");
			return hashMap;
		}
		EBOMInfo ebominfo = new EBOMInfo();
		//根据父项的编码、大版本、视图删除子件
		ebominfo.setParentNumber(AttributeUtil.getAttribute(part, PartAttribute.NUMBER));
		ebominfo.setParentMajorVersion(AttributeUtil.getAttribute(part, PartAttribute.MAJOR_VERSION));
		ebominfo.setParentView(AttributeUtil.getAttribute(part, PartAttribute.VIEW));
		SqlMapClient sqlMap = null;
		int result=0;
	    sqlMap = IbatisUtil.getSqlMapInstance();
	    if (sqlMap != null){
	    	try
	    	{
	    		logger.debug("   "+"执行BOM结构关系信息删除操作") ;
	    		sqlMap.startTransaction();
	    		result =sqlMap.delete(IbatisSqlConstant.REMOVE_EBOMRECOMD_BY_PARENT, ebominfo);
	    		sqlMap.commitTransaction();
	    		logger.debug("   "+"删除BOM结构关系信息任务结束") ;
	    	}
	    	catch (SQLException e)
	    	{
	    		e.printStackTrace();
	    		hashMap.put(part, "删除BOM失败");
	     	}finally{
	     		sqlMap.endTransaction();
	     		IbatisUtil.closeSqlMapInstance();
	     	}
	    	
	    }
	    return hashMap;
	}
}
