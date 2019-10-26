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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ibatis.sqlmap.client.SqlMapClient;

import wt.log4j.LogR;
import ext.generic.integration.erp.bean.MaterialInfo;
import ext.generic.integration.erp.service.IbatisPartReleaseService;
import ext.generic.integration.erp.util.IbatisUtil;

public class PartIbatis {
	
	private static final String clazz = IbatisPartReleaseService.class.getName() ;
	private static final Logger logger = LogR.getLogger(clazz);
//	public static Map<String,String> fieldMap  = null;
	
//	static {
//		getFieldMap();
//	}
	
//	public static void  getFieldMap(){
//		QueryXMLConfigUtil configUtil =  new QueryXMLConfigUtil(IETaskCommonUtil.getConfigFilePath()+QueryPartReleaseAttributes.CONFIG_XML_FILE);
//		fieldMap = configUtil.getAttMapping();
//	}
	
	/**
	 * 判断中间表是否有WTPart
	 * 
	 * @param part
	 * @return boolean
	 * @throws SQLException 
	 * @throws WTException 
	 * @throws IEException 
	 * @throws IOException 
	 */
	public static boolean hasObject(MaterialInfo materialInfo) throws SQLException{
		logger.debug("   "+"Entering In hasObject(WTPart part)......") ;
		boolean flag = false;
		List list = selectMaterialInfoByNumberAndVersion(materialInfo);
		if(list!=null&&list.size()>0){
			return true;
		}
		return false;
	}
	
	/**
	 * 判断中间表是否有WTPart
	 * 
	 * @param part 
	 * @return boolean
	 * @throws SQLException 
	 * @throws WTException 
	 * @throws IEException 
	 * @throws IOException 
	 */
	public static boolean hasObject(MaterialInfo materialInfo,SqlMapClient sqlMap) throws SQLException{
		logger.debug("   "+"Entering In hasObject(WTPart part)......") ;
		boolean flag = false;
		List list = selectMaterialInfoByNumberAndVersion(materialInfo,sqlMap);
		if(list!=null&&list.size()>0){
			return true;
		}
		return false;
	}
	
	/**
	 * 向数据库中插入物料信息
	 * @param materialInfo
	 * @return
	 * @throws WTException
	 * @throws SQLException
	 */
	public static int insertMaterialInfo(MaterialInfo materialInfo) throws SQLException{
		int result = 0;
		SqlMapClient sqlMap = null;
	    sqlMap = IbatisUtil.getSqlMapInstance();
	    if (sqlMap != null){
	      try{
	    	  sqlMap.startTransaction(); 
	    	  //Bug: 返回值可能为空，这里不获取返回值。
	    	  sqlMap.insert(IbatisSqlConstant.INSERT_MATERIAL_INFO, materialInfo);
	    	  sqlMap.commitTransaction(); 
	    	  
	    	  result = 1;
	      }catch (SQLException e){
	        e.printStackTrace();
	        throw new SQLException(e.getMessage());
	      }finally{
	    	  sqlMap.endTransaction();
	    	  IbatisUtil.closeSqlMapInstance();
	      }
	    }
	    return result;
	}
	
	/**
	 * 查询数据库中的所有物料信息
	 * @return 物料信息集合
	 * @throws SQLException 
	 * @throws WTException
	 */
	public static List selectAllMaterialInfo() throws SQLException{
		SqlMapClient sqlMap = null;
	    sqlMap = IbatisUtil.getSqlMapInstance();
	    List list = new ArrayList();
	    if (sqlMap != null){
	      try{
	        list = sqlMap.queryForList(IbatisSqlConstant.GETALL_MATERIAL_INFO);
	      }
	      catch (SQLException e){
	        e.printStackTrace();
	        throw new SQLException(e.getMessage());
	      }
	    }
	    IbatisUtil.closeSqlMapInstance();
	    return list;
	}
	
	/**
	 * 更新物料信息
	 * @param materialInfo
	 * @return
	 * @throws WTException
	 * @throws SQLException 
	 */
	public static int updateMaterialInfo(MaterialInfo materialInfo) throws SQLException {
		SqlMapClient sqlMap = null;
		int result = 0;
	    sqlMap = IbatisUtil.getSqlMapInstance();
	    if (sqlMap != null){
	      try{
	    	  sqlMap.startTransaction();
	    	  result = sqlMap.update(IbatisSqlConstant.UPDATE_MATERIAL_INFO, materialInfo);
	    	  sqlMap.commitTransaction();
	      }
	      catch (SQLException e){
	        e.printStackTrace();
	        throw new SQLException(e.getMessage());
	      }finally{
	    	  sqlMap.endTransaction();
	    	  IbatisUtil.closeSqlMapInstance();
	      }
	    }
		return result;
	}
	
	/**
	 * 根据编号和版本查询物料
	 * @param materialInfo
	 * @return
	 * @throws WTException
	 * @throws SQLException 
	 */
	public static List selectMaterialInfoByNumberAndVersion(
			MaterialInfo materialInfo) throws  SQLException {
		SqlMapClient sqlMap = null;
		sqlMap = IbatisUtil.getSqlMapInstance();
		List list=new ArrayList();
		if (sqlMap!=null){
			try{
				list = sqlMap.queryForList(IbatisSqlConstant.GET_MATERIAL_INFO_BY_CONDITION, materialInfo);
			}catch (SQLException e){
				e.printStackTrace();
				throw new SQLException(e.fillInStackTrace().getMessage());
			}
		}
		IbatisUtil.closeSqlMapInstance();
		return	list;
	}
	
	/**
	 * 根据编号和版本查询物料
	 * @param materialInfo
	 * @return
	 * @throws WTException
	 * @throws SQLException 
	 */
	public static List selectMaterialInfoByNumberAndVersion(
			MaterialInfo materialInfo,SqlMapClient sqlMap) throws  SQLException {
		List list = new ArrayList();
		if (sqlMap!=null){
			try{
				list = sqlMap.queryForList(IbatisSqlConstant.GET_MATERIAL_INFO_BY_CONDITION, materialInfo);
			}catch (SQLException e){
				e.printStackTrace();
				throw new SQLException(e.fillInStackTrace().getMessage());
			}
		}
		return	list;
	}
	
	/**
	 * 查询中间表中ERP已经处理过的物料信息
	 * @return 满足条件的物料信息
	 * @throws WTException
	 * @throws SQLException 
	 */
	public static List selectERPReceivedMaterial() throws  SQLException {
		SqlMapClient sqlMap = null;
		sqlMap = IbatisUtil.getSqlMapInstance();
		List list=new ArrayList<MaterialInfo>();
		if (sqlMap!=null){
			try{
				list = sqlMap.queryForList(IbatisSqlConstant.GET_ERP_RECEIVED_MATERIAL);
			}catch (SQLException e){
				e.printStackTrace();
				throw new SQLException(e.fillInStackTrace().getMessage());
			}
		}
		IbatisUtil.closeSqlMapInstance();
		return	list;
	}
	
	/**
	 * 更新WriteBack信息
	 * @param materialInfo
	 * @return
	 * @throws WTException
	 * @throws SQLException 
	 */
	public static int updateMaterialWriteBackInfo(MaterialInfo materialInfo) throws SQLException {
		SqlMapClient sqlMap = null;
		int result = 0;
	    sqlMap = IbatisUtil.getSqlMapInstance();
	    if (sqlMap != null){
	      try{
	    	  sqlMap.startTransaction();
	    	  result = sqlMap.update(IbatisSqlConstant.UPDATE_MATERIAL_WRITEBACK, materialInfo);
	    	  sqlMap.commitTransaction();
	      }
	      catch (SQLException e){
	        e.printStackTrace();
	        throw new SQLException(e.getMessage());
	      }finally{
	    	  sqlMap.endTransaction();
	    	  IbatisUtil.closeSqlMapInstance();
	      }
	    }
		return result;
	}
}
