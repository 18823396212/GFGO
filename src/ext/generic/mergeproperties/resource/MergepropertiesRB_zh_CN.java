package ext.generic.mergeproperties.resource;

import wt.util.resource.RBEntry;
import wt.util.resource.RBUUID;
import wt.util.resource.WTListResourceBundle;

/**
 * 提示信息资源文件 MergepropertiesRB_zh_CN
 * 
 * @author Administrator 2014-12-5
 *
 */
@RBUUID("ext.generic.mergeproperties.resource.MergepropertiesRB")
public class MergepropertiesRB_zh_CN extends WTListResourceBundle {

	@RBEntry(" 部件属性名称在系统中未定义 ！")
	public static final String NOT_EXIST_SYS = "NOT_EXIST_SYS";

	@RBEntry(" 属性拼接条件不完整！")
	public static final String RULE_NOT_COMPLETE = "RULE_NOT_COMPLETE";

	@RBEntry(" 配置文件信息不完整！")
	public static final String NOT_COMPLETE = "NOT_COMPLETE";
	
	@RBEntry("合并后的结果长度为：{0} ，大于允许的最大长度：{1}")
	public static final String MERGE_ERR_OVEW_MAXLENGTH = "MERGE_ERR_OVEW_MAXLENGTH";
	
	@RBEntry("属性合并配置文件中targetProperties值为空。")
	public static final String MERGE_ERR_TARGETPROPERTIES = "MERGE_ERR_TARGETPROPERTIES";
	
	@RBEntry("属性合并配置文件中mergeRule值为空。")
	public static final String MERGE_ERR_MERGERULE = "MERGE_ERR_MERGERULE";

}
