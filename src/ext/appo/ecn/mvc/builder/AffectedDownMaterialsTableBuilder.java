package ext.appo.ecn.mvc.builder;

import com.ptc.jca.mvc.components.JcaColumnConfig;
import com.ptc.jca.mvc.components.JcaComponentParams;
import com.ptc.jca.mvc.components.JcaTableConfig;
import com.ptc.mvc.components.*;
import com.ptc.mvc.util.ClientMessageSource;
import com.ptc.netmarkets.util.beans.NmHelperBean;
import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.ecn.util.AffectedMaterialsUtil;
import org.apache.log4j.Logger;
import wt.change2.WTChangeOrder2;
import wt.log4j.LogR;
import wt.util.WTException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ComponentBuilder("ext.appo.ecn.mvc.builder.AffectedDownMaterialsTableBuilder")
public class AffectedDownMaterialsTableBuilder extends AbstractComponentBuilder{
	
	private static final String TABLE_ID = "ext.appo.ecn.mvc.builder.AffectedDownMaterialsTableBuilder";
	
	private static final String CLASSNAME = AffectedDownMaterialsTableBuilder.class.getName() ;
	private static final Logger LOG = LogR.getLogger(CLASSNAME) ;
	

	@Override
	public Object buildComponentData(ComponentConfig config, ComponentParams params) throws Exception {

		List<Map<String,String>> datasArray = new ArrayList<>();

		if(params == null){
			return datasArray;
		}

		try {
			NmHelperBean helper = ((JcaComponentParams) params).getHelperBean();
			Object pbo = helper.getNmCommandBean().getActionOid().getRefObject();
			if (pbo instanceof WTChangeOrder2) {
				WTChangeOrder2 ecn = (WTChangeOrder2)pbo;
				datasArray = AffectedMaterialsUtil.getBuilderDateValue(ecn,"2");
			}
		}catch (Exception e) {
		// TODO: handle exception
		e.printStackTrace();
		throw new WTException(e.getLocalizedMessage()) ;
	}


		return  datasArray;
	}

	@Override
	public ComponentConfig buildComponentConfig(ComponentParams arg0) throws WTException {
        ComponentConfigFactory componentconfigfactory = getComponentConfigFactory();
		JcaTableConfig tableConfig = (JcaTableConfig)componentconfigfactory.newTableConfig();
		
		tableConfig.setId (TABLE_ID);
		tableConfig.setLabel("受影响下层物料");
		tableConfig.setSelectable(true);
        tableConfig.setShowCount(true);

        //下层物料编码
		JcaColumnConfig columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("parentNumber", true);
        columnconfig.setLabel("下层物料编码");
        columnconfig.setAutoSize(true);
        columnconfig.setWidth(100);
        tableConfig.addComponent(columnconfig);

        //下层物料名称
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("parentName", true);
        columnconfig.setLabel("下层物料名称");
        columnconfig.setAutoSize(true);
        columnconfig.setWidth(100);
        tableConfig.addComponent(columnconfig);

		//下层物料版本
		columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("parentVersion", true);
		columnconfig.setLabel("下层物料版本");
		columnconfig.setAutoSize(true);
		columnconfig.setWidth(100);
		tableConfig.addComponent(columnconfig);

		//下层物料状态
		columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("parentState", true);
		columnconfig.setLabel("下层物料状态");
		columnconfig.setAutoSize(true);
		columnconfig.setWidth(100);
		tableConfig.addComponent(columnconfig);


		//受影响物料编码
		columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("childNumber", true);
		columnconfig.setLabel("受影响物料编码");
		columnconfig.setAutoSize(true);
		columnconfig.setWidth(100);
		tableConfig.addComponent(columnconfig);


		//受影层物料名称
		columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("childName", true);
		columnconfig.setLabel("受影响物料名称");
		columnconfig.setAutoSize(true);
		columnconfig.setWidth(100);
		tableConfig.addComponent(columnconfig);

		//受影响物料版本
		columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("childVersion", true);
		columnconfig.setLabel("受影响物料版本");
		columnconfig.setAutoSize(true);
		columnconfig.setWidth(100);
		tableConfig.addComponent(columnconfig);

		//受影响物料状态
		columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("childState", true);
		columnconfig.setLabel("受影响物料状态");
		columnconfig.setAutoSize(true);
		columnconfig.setWidth(100);
		tableConfig.addComponent(columnconfig);

		//数量
		columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("shuliang", true);
		columnconfig.setLabel("数量");
		columnconfig.setAutoSize(true);
		columnconfig.setWidth(100);
		tableConfig.addComponent(columnconfig);

        //单位
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("danwei", true);
        columnconfig.setLabel("单位");
        columnconfig.setAutoSize(true);
        columnconfig.setWidth(100);
        tableConfig.addComponent(columnconfig);
        
        //位号
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("weihao", true);
        columnconfig.setLabel("位号");
        columnconfig.setAutoSize(true);
        columnconfig.setWidth(100);
        tableConfig.addComponent(columnconfig);

		//最低存货等级
		columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("zdchdj", true);
		columnconfig.setLabel("最低存货等级");
		columnconfig.setAutoSize(true);
		columnconfig.setWidth(100);
		tableConfig.addComponent(columnconfig);

		//BOM备注信息
		columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("bombzxx", true);
		columnconfig.setLabel("BOM备注信息");
		columnconfig.setAutoSize(true);
		columnconfig.setWidth(100);
		tableConfig.addComponent(columnconfig);

		return tableConfig;
	}

}
