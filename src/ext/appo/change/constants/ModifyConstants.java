package ext.appo.change.constants;

public interface ModifyConstants {

    String RESOURCE = "com.oppo.plm.bom.resource.BomResource";
    String MSSAGERESOURCE = "com.oppo.plm.bom.resource.BomMessageResource";

    //属性定义
    String ATTRIBUTE_1 = "Classification";//物料分类
    String ATTRIBUTE_2 = "sscpx";//所属产品类别
    String ATTRIBUTE_3 = "ssxm";//所属项目
    String ATTRIBUTE_4 = "isReject";//是否驳回
    String ATTRIBUTE_5 = "referenceDesignatorRange";//位号
    String ATTRIBUTE_6 = "ActionName";//操作名称
    String ATTRIBUTE_7 = "ChangeObjectType";//变更对象类型
    String ATTRIBUTE_8 = "ChangeItemType";//变更类型

    //类型定义
    String TYPE_1 = "com.pisx.BOMChangeActivity2";//BOM变更ECA
    String TYPE_2 = "com.pisx.DrawingsChangeActivity2";//图纸变更ECA
    String TYPE_3 = "com.pisx.TransactionalChangeActivity2";//事务性任务
    String TYPE_4 = "com.pisx.ChangeOrder2AffectedLink";//ECN与受影响对象链接
    String TYPE_5 = "com.pisx.ChangeOrder2EndItemsLink";//ECN与受影响产品链接
    String TYPE_6 = "com.pisx.ChangeOrder2TransactionLink";//ECN与事务性任务链接

    //生命周期状态
    String STATE_1 = "INWORK";//正在工作
    String STATE_2 = "REWORK";//重新工作

    //角色
    String ROLE_1 = "Receiver";//接收者
    String ROLE_2 = "Signer";//会签者
    String ROLE_3 = "Assessor";//审核者
    String ROLE_4 = "APPROVER";//批准者

    //属性值
    String VALUE_1 = "替换";
    String VALUE_2 = "PCB";//物料分类
    String VALUE_3 = "E1500000";//物料分类
    String VALUE_4 = "升版";
    String VALUE_5 = "BOM变更";
    String VALUE_6 = "图纸变更";

    //流程模版名
    String FLOWNAME_1 = "APPO_BOMCHANGEWF";//BOM变更流程
    String FLOWNAME_2 = "APPO_DRAWINGCHANGEWF";//图纸变更流程
    String FLOWNAME_3 = "BOM变更流程";
    String FLOWNAME_4 = "图纸变更流程";

    //分隔符
    String SEPARATOR_1 = "_";

    //常量
    String CONSTANTS_1 = "cacheButton";//缓存按钮
    String CONSTANTS_2 = "修改变更申请";//工作流节点名称

    /**
     * ECN与受影响对象链接
     */
    String LINKTYPE_1 = "ChangeOrder2AffectedLink";
    /**
     * ECN与受影响产品链接
     */
    String LINKTYPE_2 = "ChangeOrder2EndItemsLink";
    /**
     * ECN与事务性任务链接
     */
    String LINKTYPE_3 = "ChangeOrder2TransactionLink";



    /**
     * 子流程进度-已创建
     */
    String ROUTING_1 = "已创建";
    /**
     * 子流程进度-已驳回
     */
    String ROUTING_2 = "已驳回";
    /**
     * 子流程进度-已完成
     */
    String ROUTING_3 = "已完成";

}
