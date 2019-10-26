package ext.appo.change.constants;

public interface ModifyConstants {

    String RESOURCE = "com.oppo.plm.bom.resource.BomResource";
    String MSSAGERESOURCE = "com.oppo.plm.bom.resource.BomMessageResource";

    //属性定义
    String ATTRIBUTE_1 = "Classification";//物料分类

    //类型定义
    String TYPE_1 = "com.pisx.BOMChangeActivity2";//BOM变更ECA
    String TYPE_2 = "com.pisx.DrawingsChangeActivity2";//图纸变更ECA
    String TYPE_3 = "com.pisx.TransactionalChangeActivity2";//事务性任务
    String TYPE_4 = "com.pisx.ChangeOrder2AffectedLink";//ECN与受影响对象链接
    String TYPE_5 = "com.pisx.ChangeOrder2EndItemsLink";//ECN与受影响产品链接
    String TYPE_6 = "com.pisx.ChangeOrder2TransactionLink";//ECN与事务性任务链接

    //属性值
    String VALUE_1 = "替换";
    String VALUE_2 = "PCB";//物料分类
    String VALUE_3 = "E1500000";//物料分类
    String VALUE_4 = "升版";
    String VALUE_5 = "BOM变更";
    String VALUE_6 = "图纸变更";

    //分隔符
    String SEPARATOR_1 = "_";

}
