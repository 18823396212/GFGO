package ext.appo.part.builder;

import com.ptc.core.components.descriptor.DescriptorConstants;
import com.ptc.jca.mvc.components.JcaTreeConfig;
import com.ptc.mvc.components.*;
import com.ptc.mvc.components.ds.DataSourceMode;
import ext.appo.part.beans.EffectiveBaselineBean;
import ext.appo.part.util.EffecitveBaselineUtil;
import wt.part.WTPart;
import wt.util.WTException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ComponentBuilder(value = "ext.appo.part.builder.BaseLineTreeBuilder")
public class BaseLineTreeBuilder extends AbstractComponentBuilder {


    @Override
    public ComponentConfig buildComponentConfig(ComponentParams params) throws WTException {
        ComponentConfigFactory factory = getComponentConfigFactory();
        //创建 TreeConfig
        TreeConfig tree = factory.newTreeConfig();
        tree.setSelectable(true);
        tree.setSingleSelect(true);
        tree.setConfigurable(true);
        tree.setShowCount(true);
        tree.setActionModel("showBaselineMaterialsTools");//添加导出Model
        tree.setExpansionLevel(DescriptorConstants.TableTreeProperties.NO_EXPAND);
        tree.setLabel("展示基线物料");
        tree.setNodeColumn("partNo");
        tree.setId("ext.appo.part.builder.BaseLineTreeBuilder");
        ((JcaTreeConfig) tree).setDataSourceMode(DataSourceMode.SYNCHRONOUS);
//        tree.setSingleSelect(true);
//        tree.setConfigurable(true);
        
//        tree.addComponent(factory.newColumnConfig("type_icon", true));
        
        //部件编码
        ColumnConfig partNo = factory.newColumnConfig("partNo", true);
        partNo.setLabel("部件编码");
        partNo.setAutoSize(true);
        partNo.setDataUtilityId("baseLineDataUtility");
        tree.addComponent(partNo);

        //部件名称
        ColumnConfig partName = factory.newColumnConfig("partName", true);
        partName.setLabel("部件名称");
        partName.setAutoSize(true);
        partName.setDataUtilityId("baseLineDataUtility");
        tree.addComponent(partName);
        
        ColumnConfig substitutePartNumber = factory.newColumnConfig("substitutePartNumber", true);
        substitutePartNumber.setLabel("特定替代料");
        substitutePartNumber.setAutoSize(true);
        substitutePartNumber.setDataUtilityId("baseLineDataUtility");
        tree.addComponent(substitutePartNumber);
        
        ColumnConfig substituteType = factory.newColumnConfig("overallPartNumber", true);
        substituteType.setLabel("全局替代料");
        substituteType.setAutoSize(true);
        substituteType.setDataUtilityId("baseLineDataUtility");
        tree.addComponent(substituteType);

        //系统版本
        ColumnConfig partVersion = factory.newColumnConfig("partVersion", true);
        partVersion.setLabel("系统版本");
        partVersion.setAutoSize(true);
        partVersion.setDataUtilityId("baseLineDataUtility");
        tree.addComponent(partVersion);

        //状态
        ColumnConfig state = factory.newColumnConfig("state", true);
        state.setLabel("状态");
        state.setAutoSize(true);
        state.setDataUtilityId("baseLineDataUtility");
        tree.addComponent(state);

        //数量
        ColumnConfig number = factory.newColumnConfig("number", true);
        number.setLabel("数量");
        number.setAutoSize(true);
        number.setDataUtilityId("baseLineDataUtility");
        tree.addComponent(number);


        //单位
        ColumnConfig unit = factory.newColumnConfig("unit", true);
        unit.setLabel("单位");
        unit.setAutoSize(true);
        unit.setDataUtilityId("baseLineDataUtility");
        tree.addComponent(unit);

        //位号
        ColumnConfig placeNumber = factory.newColumnConfig("placeNumber", true);
        placeNumber.setLabel("位号");
        placeNumber.setAutoSize(true);
        placeNumber.setDataUtilityId("baseLineDataUtility");
        tree.addComponent(placeNumber);

        //最低存货等级
        ColumnConfig zdckdj = factory.newColumnConfig("zdckdj", true);
        zdckdj.setLabel("最低存货等级");
        zdckdj.setAutoSize(true);
        zdckdj.setDataUtilityId("baseLineDataUtility");
        tree.addComponent(zdckdj);

        //BOM备注信息
        ColumnConfig bomRemarkInfo = factory.newColumnConfig("BOMRemarkInfo", true);
        bomRemarkInfo.setLabel("BOM备注信息");
        bomRemarkInfo.setAutoSize(true);
        bomRemarkInfo.setDataUtilityId("baseLineDataUtility");
        tree.addComponent(bomRemarkInfo);

        //BOM备注信息
        ColumnConfig  createTime = factory.newColumnConfig("createTime", true);
        createTime.setLabel("打印时间");
        createTime.setAutoSize(true);
        createTime.setDataUtilityId("baseLineDataUtility");
        tree.addComponent(createTime);

        //上层物料编码
        ColumnConfig  upperPartNumber = factory.newColumnConfig("upperPartNumber", true);
        upperPartNumber.setLabel("上层物料编号");
        upperPartNumber.setAutoSize(true);
        upperPartNumber.setDataUtilityId("baseLineDataUtility");
        tree.addComponent(upperPartNumber);
        
        ColumnConfig  upperPartVersion = factory.newColumnConfig("upperPartVersion", true);
        upperPartVersion.setLabel("上层物料版本");
        upperPartVersion.setAutoSize(true);
        upperPartVersion.setDataUtilityId("baseLineDataUtility");
        tree.addComponent(upperPartVersion);
        
        return tree;

    }

    @Override
    public Object buildComponentData(ComponentConfig componentConfig, ComponentParams params) throws Exception {

    	List<EffectiveBaselineBean> beans = null;
    	Object contextObject = params.getContextObject();
    	System.out.println("contextObject====================================="+contextObject);
    	if(contextObject instanceof WTPart) {
    		WTPart part = (WTPart)contextObject;
    		System.out.println("part====================================="+part);
    	    String vid = EffecitveBaselineUtil.getVidByObject(part);
    	    System.out.println("vid==============================="+vid);
    		beans = checkData(vid);
    	}
        System.out.println("beans========================"+beans);
        return new EffecitveBaselineHandler(beans);
    }
    
    public static List<EffectiveBaselineBean> checkData(String vid) throws IOException, WTException{
		  PreparedStatement pstmt;
		   List<EffectiveBaselineBean> listBean = new ArrayList<>();
	        try {
	        	 Connection conn = EffecitveBaselineUtil.getConn();
	        	 System.out.println("checkFolderId====================="+conn);    
	        	 String sql = "select n.*,level  from  ED_EffectiveBaseline n  CONNECT BY  PRIOR beanUID = upUID start WITH partvid = '"+vid+"'";

	            System.out.println("sql========================="+sql);
	            pstmt = (PreparedStatement)conn.prepareStatement(sql);
	            System.out.println("checkFolderId============pstmt========="+pstmt);
	            ResultSet rs = pstmt.executeQuery();
	            System.out.println("checkFolderId============rs========="+rs);
	            //5.处理ResultSet
	            while(rs.next()){
	                //rs.get+数据库中对应的类型+(数据库中对应的列别名)
	            	EffectiveBaselineBean bean = new EffectiveBaselineBean();
	            	bean.setBeanUID(rs.getString(1));
	                bean.setProcessoid(rs.getString(2));
	                bean.setUpUID(rs.getString(3));
	            	bean.setPartoid(rs.getString(4));
	                bean.setPartvid(rs.getString(5));
	            	bean.setPartNumber(rs.getString(6));
	            	bean.setPartName(rs.getString(7));
	            	bean.setPartState(rs.getString(8));
	                bean.setParentoid(rs.getString(9));
	            	bean.setParentvid(rs.getString(10));
                    bean.setParenNumber(rs.getString(11));
	            	bean.setParenName(rs.getString(12));
	            	bean.setParenState(rs.getString(13));
	            	bean.setUsageLinkoid(rs.getString(14));
	            	bean.setCreateTime(rs.getString(15));
	            	bean.setSubstituteLinkoid(rs.getString(16));
	            	bean.setAlternateLinkoid(rs.getString(17));
	            	bean.setLevel(rs.getString(18));
	            	listBean.add(bean);
	            }
	            conn.close();
	        } catch (SQLException e) {
		        e.printStackTrace();
		    }
	        return listBean;
	    }
    
}
