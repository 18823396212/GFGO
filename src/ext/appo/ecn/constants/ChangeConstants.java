package ext.appo.ecn.constants;

public interface ChangeConstants {

	// 分割符：多个用户名称间的分割符
	String USER_KEYWORD = ";" ;
	
	// 分割符：用于‘事务性任务’对象传播分割 
	String USER_KEYWORD2 = "," ;
	
	// 分割符：用于分类路径分割 
	String USER_KEYWORD4 = "/" ;
	
	// 受影响对象表中IBA属性关键字
	String USER_KEYWORD3 = "IBA|" ;
	
	// 受影响对象表单ID
	String CHANGETASK_AFFECTEDITEMS_TABLE_ID = "changeTask_affectedItems_table" ;
	
	// “事务性任务”更改任务表单ID
	String CHANGETASK_CUSTOMIZE_TABLE_ID = "ext.appo.ecn.mvc.builder.ChangeTaskTableBuilder" ;
	
	// 页面创建编辑关键字
	String AFFECTED_PRODUCT_ID = "affectedProductID" ;
	String CHANGETASKBEAN_ID = "changeTaskBeanID" ;
	String RESULT_DATAS = "resultDatas" ;
	String ERROR_MESSAGE = "message" ;
	String DATA_ARRAY = "datasArray" ;
	String CHANGETASK_ARRAY = "changeTaskArray" ;
	// 受影响上层物料列名
	String UPPERMATERIALCODE_COMPID = "parentNumber" ;
	String UPPERMATERIALNAME_COMPID = "parentName" ;
	String UPPERMATERIALVERSION_COMPID = "parentVersion" ;
	String UPPERMATERIALSTATE_COMPID = "parentState" ;
	String AFFECTEDUPPERMATERIALCODE_COMPID = "childNumber" ;
	String AFFECTEDUPPERMATERIALNAME_COMPID = "childName" ;
	String AFFECTEDUPPERMATERIALVERSION_COMPID = "childVersion" ;
	String AFFECTEDUPPERMATERIALSTATE_COMPID = "childState" ;
	String NUMBER_COMPID = "shuliang" ;
	String UNIT_COMPID = "danwei" ;
	String BITNUMBER_COMPID = "weihao" ;
	String MININVENTORYLEVE_COMPID = "zdchdj" ;
	String BOMMEMOINFO_COMPID = "bombzxx" ;

	
	// 受影响列表新增列
	String ARTICLEINVENTORY_COMPID = "ArticleInventory";//在制数量
	String CENTRALWAREHOUSEINVENTORY_COMPID = "CentralWarehouseInventory";//库存数量
	String PASSAGEINVENTORY_COMPID = "PassageInventory";//在途数量
	String RESPONSIBLEPERSON_COMPID = "ResponsiblePerson"; //责任人
	String ARTICLEDISPOSE_COMPID = "ArticleDispose";//*在制处理措施
	String PASSAGEDISPOSE_COMPID = "PassageDispose";//*在途处理措施
	String INVENTORYDISPOSE_COMPID = "InventoryDispose";//*库存处理措施
	String PRODUCTDISPOSE_COMPID = "ProductDispose";//*已出货成品处理措施
	String CHANGETYPE_COMPID = "ChangeType";
	//add by tongwang 20191023 start
	String CHANGOBJECTETYPE_COMPID = "ChangeObjectType";//变更对象类型
	//add by tongwang 20191023 end
	String COMPLETIONTIME_COMPID = "CompletionTime"; 
	
	// build列对象id标识
	String OID_COMPID = "oid" ;
	// 受影响对象列表备注
	String AADDESCRIPTION_COMPID = "aadDescription" ;
	// 产生对象列表备注
	String CRDESCRIPTION_COMPID = "crDescription" ;
	
	// 事务性任务列名
	String CHANGETHEME_COMPID = "changeTheme" ;
	String CHANGEDESCRIBE_COMPID = "changeDescribe" ;
	String RESPONSIBLE_COMPID = "responsible" ;
	String CHANGEACTIVITY2_COMPID = "changeActivity2" ;
	String NEEDDATE_COMPID = "needDate" ;
	
	// 事务性任务内部名称
	String TRANSACTIONAL_CHANGEACTIVITY2 = "com.plm.TransactionalChangeActivity2";
	// DCN-ECN内部名称
	String DCN_ECN_CHANGEORDER2 = "com.plm.DCN-ECN" ;
	// 设计ECN内部名称
	String DESIGN_ECN_CHANGEORDER2 = "com.plm.DesignECN" ;
	// 工程ECN内部名称
	String ENGINEERING_ECN_CHANGEORDER2 = "com.plm.EngineeringECN" ;
	// 可配置Link子类型：用于存储ECN与产品关系
	String CHANGEORDER2_ENDITEMS_LINK_TYPE = "com.plm.ChangeOrder2EndItemsLink" ;
	
	// 生命周期状态
	String CANCELLED = "CANCELLED" ; // 已取消
	String ARCHIVED = "ARCHIVED" ; // 已归档
	String RELEASED = "RELEASED" ; // 已发布
	String RESOLVED = "RESOLVED" ; // 已解决
	String OPEN = "OPEN" ; // 开启
	
	// 日期格式
	String SIMPLE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss" ;
	String SIMPLE_DATE_FORMAT_02 = "yyyy/MM/dd" ;
	String SIMPLE_DATE_FORMAT_03 = "yyyy-MM-dd" ;
	
	// IBA属性内部名称
	String JDZT = "cpzt" ; // 产品状态
	
	// 角色：工作负责人
	String ROLE_ASSIGNEE = "ASSIGNEE" ;
	
	// IBA属性内部名称
	String COMPLETION_TIME = "qwwcsj" ;
}
