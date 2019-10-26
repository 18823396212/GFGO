package ext.generic.mergeproperties.resource;

import wt.util.resource.RBEntry;
import wt.util.resource.RBUUID;
import wt.util.resource.WTListResourceBundle;

/**
 * 提示信息资源文件 MergepropertiesRB
 * 
 * @author Administrator 2014-12-5
 *
 */
@RBUUID("ext.generic.mergeproperties.resource.MergepropertiesRB")
public class MergepropertiesRB extends WTListResourceBundle {

	@RBEntry(" Member attribute name is not defined in the system ！")
	public static final String NOT_EXIST_SYS = "NOT_EXIST_SYS";

	@RBEntry(" Attribute merge condition is not complete ！")
	public static final String RULE_NOT_COMPLETE = "RULE_NOT_COMPLETE";

	@RBEntry(" The information in the configuration file is not complete ！")
	public static final String NOT_COMPLETE = "NOT_COMPLETE";
	
	@RBEntry("After Merging Length Is ：{0} ，Over Allowed MaxLength：{1}")
	public static final String MERGE_ERR_OVEW_MAXLENGTH = "MERGE_ERR_OVEW_MAXLENGTH";
	
	@RBEntry("targetProperties Is Blank In The Config File")
	public static final String MERGE_ERR_TARGETPROPERTIES = "MERGE_ERR_TARGETPROPERTIES";
	
	@RBEntry("mergeRule Is Blank In The Config File")
	public static final String MERGE_ERR_MERGERULE = "MERGE_ERR_MERGERULE";

}
