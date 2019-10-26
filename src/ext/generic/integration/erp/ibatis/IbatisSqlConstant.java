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

public class IbatisSqlConstant {
	
	//根据父件的编号和大版本删除BOM记录
	public static final String REMOVE_EBOMRECOMD_BY_PARENT = "removeEBOMRecordByParent" ;
	
	//根据父件编号，父件版本，行号及子健编号 查询BOM记录
	public static final String QUERY_EBOMRECOMD_BY_CONDITION = "queryEBOMRecordByParent" ;
	
	//根据父件编号，父件版本，视图 查询零部件信息
	public static final String QUERY_ALLEBOMRECOMD_BY_CONDITION = "queryAllEBOMRecordByParent" ;
		
	
	//根据父件编号，父件版本，行号及子健编号 查询BOM记录
	public static final String INSERT_EBOM_INFO = "insertEBOMInfo" ;
	
	
	// 根据父件编号 父件版本 行号 子健编号 PDM反写状态 删除EBOM
	public static final String DELETE_EBOM_INFO = "deleteEBOMInfo" ;
	
	
	// 更新BOM记录   条件：父件编号、父件版本、子健编号、行号 
	public static final String UPDATE_EBOM_INFO = "updateEBOMInfo" ;
	
	// 根据父件编号、子健编号、替代类型删除特定替代
	public static final String DELETE_SUBSTITUTE_MATERIAL = "deleteSubstituteMaterial" ;
	
	// 根据父件编号和替代类型删除全局替代关系信息
	public static final String DELETE_ALTERNATIVE_MATERIAL = "deleteAlternativeMaterial" ;
	
	// 根据父件编号和子健编号删除替代关系信息
	public static final String QUERY_ALTERNATIVE_MATERIAL = "queryAlternativeMaterial" ;
	
	// 根据父件编号、子健编号、替代料号、PDM反写状态 删除替代信息
	public static final String DELETE_ALTERNATIVE_MATERIAL2 = "deleteAlternativeMaterialByCondtion" ;
	
	// 插入替代料信息记录
	public static final String INSERT_ALTERNATIVE_MATERIAL = "insertAlternativeMaterial" ;
	
	// 更新替代料信息记 条件   附件编号、子健编号、替代料号、替代类型
	public static final String UPDATE_ALTERNATIVE_MATERIAL = "updateAlternativeMaterial" ;
	
	
	// 根据物料号和大版本查询 BOM父件发布信息
	public static final String QUERY_BOM_ROOT_INFO = "queryBomRootInfo" ;
	
	// 根据物料号、大版本、PDM反写状态 删除 BOM父件发布信息
	public static final String DELETE_BOM_ROOT_INFO = "deleteBomRootInfo" ;
	
	// 插入BOM父节点信息
	public static final String INSERT_BOM_ROOT_INFO = "insertBomRootInfo" ;
	
	// 根据编号、大版本 更新BOM父节点信息
	public static final String UPDATE_BOM_ROOT_INFO = "updateBomRootInfo" ;
	
	
	// 根据编号查询ECN信息
	public static final String QUERY_ECN__INFO = "queryECNInfo" ;
	
	// 根据编号和PDM反写状态删除ECN信息
	public static final String DELETE_ECN_INFO = "deleteECNInfo" ;
	
	// 根据编号插入ECN信息
	public static final String INSERT_ECN_INFO = "insertECNInfo" ;
	
	// 根据编号更新ECN信息
	public static final String UPDATE_ECN_INFO = "updateECNInfo" ;
	
	
	// 
	public static final String INSERT_MATERIAL_INFO = "insertMaterialInfo" ;
	
	// 
	public static final String GETALL_MATERIAL_INFO = "getAllMaterialInfo" ;
	
	// 
	public static final String UPDATE_MATERIAL_INFO = "updateMaterialInfo" ;
	
	// 
	public static final String GET_MATERIAL_INFO_BY_CONDITION = "getMaterialInfoByPartNumberAndVersion" ;
	
	//查询ERP已经处理过的物料信息，flag表示不为N
	public static final String GET_ERP_RECEIVED_MATERIAL = "getERPReceivedMaterial" ;
	
	//更新物料表中WriteBack标识
	public static final String UPDATE_MATERIAL_WRITEBACK = "updateMaterialInfoWriteBack" ;
	
	//查询ERP已经处理过的BOM表头信息，flag表示不为N
	public static final String GET_ERP_RECEIVED_BOMINFO = "getERPReceivedBOMInfo";
	
	//更新BOM表头表中WriteBack标识
	public static final String UPDATE_BOMINFO_WRITEBACK = "updateBOMInfoWriteBack" ;
	
	//查询库存价格信息
	public static final String QUERY_INVENTORY_PRICE = "queryInventoryPrice" ;
	
	//查询库存价格信息
	public static final String QUERY_INVENTORY_PRICE_WITH_COMPANY = "queryInventoryPriceWithCompany" ;
	
	//ecn查询库存价格信息
	public static final String QUERY_INVENTORY_PRICE_VERION = "queryInventoryPriceVersion" ;
	
}
