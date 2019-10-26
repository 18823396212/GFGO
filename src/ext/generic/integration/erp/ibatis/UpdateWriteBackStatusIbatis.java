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
import wt.log4j.LogR;
import ext.generic.integration.erp.util.IbatisUtil;

import com.ibatis.sqlmap.client.SqlMapClient;
/**
 * 主要用于：PDM反写ERP接收数据状态后，更新中间表的标识符
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-10-08
 */
public class UpdateWriteBackStatusIbatis {
	private static final String clazz = UpdateWriteBackStatusIbatis.class.getName() ;

	private static final Logger logger = LogR.getLogger(clazz);	
	
	/**
	 * 在中间表中更新记录
	 * 
	 * @param part
	 * @param releaseTime
	 * @throws SQLException 
	 */
	public static void update(String sqlid , Object obj) throws SQLException{
		
		logger.debug("   "+"Entering In  update(String where )......") ;
		
		if(sqlid ==null||obj ==null)
			return ;

		
		SqlMapClient sqlMap = null;
	    sqlMap = IbatisUtil.getSqlMapInstance();
		try {
			logger.debug("   "+"执行ECN信息更新任务") ;
			
			sqlMap.startTransaction();
			sqlMap.update(sqlid ,obj);
			sqlMap.commitTransaction();
			
			logger.debug("   "+"更新ECN信息任务结束") ;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e.getMessage());
		} finally{
			sqlMap.endTransaction();
	    	IbatisUtil.closeSqlMapInstance();
			logger.debug("   "+"Existed Out update( ChangeOrder2 ecn , String releaseTime )......") ;
		}
	}
}
