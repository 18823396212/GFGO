package ext.appo.part.builder;

import com.ptc.jca.mvc.components.JcaColumnConfig;
import com.ptc.jca.mvc.components.JcaTableConfig;
import com.ptc.mvc.components.*;

import ext.appo.part.beans.ChangPartStateECNBean;
import ext.appo.part.util.EffecitveBaselineUtil;

import java.util.List;

import org.apache.log4j.Logger;

import wt.fc.Persistable;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.util.WTException;



@ComponentBuilder("ext.appo.part.builder.ChangeStateECNTableBuilder")
public class ChangeStateECNTableBuilder extends AbstractComponentBuilder{
	
	private static final String TABLE_ID = "ext.appo.part.builder.ChangeStateECNTableBuilder";
	
	private static final String CLASSNAME = ChangeStateECNTableBuilder.class.getName() ;
	private static final Logger LOG = LogR.getLogger(CLASSNAME) ;
	

	@Override
	public Object buildComponentData(ComponentConfig config, ComponentParams params) throws Exception {

		List<Persistable> list = null;
		Object contextObject = params.getContextObject();
    	System.out.println("contextObject====================================="+contextObject);
    	if(contextObject instanceof WTPart) {
    		WTPart part = (WTPart)contextObject;
    		System.out.println("part====================================="+part);
    	    String vid = EffecitveBaselineUtil.getVidByObject(part);
    	    list = EffecitveBaselineUtil.checkChangPartStateECNBeanData(vid);

    	}
		return  list;
	}

	@Override
	public ComponentConfig buildComponentConfig(ComponentParams arg0) throws WTException {
        ComponentConfigFactory componentconfigfactory = getComponentConfigFactory();
		JcaTableConfig tableConfig = (JcaTableConfig)componentconfigfactory.newTableConfig();
		tableConfig.setId (TABLE_ID);
		tableConfig.setLabel("更改物料状态的ECN");
		tableConfig.setSelectable(true);
        tableConfig.setShowCount(true);
        
        tableConfig.addComponent(componentconfigfactory.newColumnConfig("type_icon", true));
        
        //编号
		JcaColumnConfig columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("number", true);
        columnconfig.setLabel("编号");
        columnconfig.setAutoSize(true);
        columnconfig.setWidth(100);
		//columnconfig.setDataUtilityId("");
        tableConfig.addComponent(columnconfig);

        //名称
        columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("name", true);
        columnconfig.setLabel("名称");
        columnconfig.setAutoSize(true);
        columnconfig.setWidth(100);
        tableConfig.addComponent(columnconfig);

//		//更改请求复杂性
//		columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("changeComplexity", true);
//		columnconfig.setLabel("更改请求复杂性");
//		columnconfig.setAutoSize(true);
////		columnconfig.setDataUtilityId("ChangeStateECNDataUtility");
//		columnconfig.setWidth(100);
//		tableConfig.addComponent(columnconfig);


		//状态
		columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("state", true);
		columnconfig.setLabel("状态");
		columnconfig.setAutoSize(true);
		columnconfig.setWidth(100);
		tableConfig.addComponent(columnconfig);




		//创建时间
		columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("thePersistInfo.createStamp", true);
		columnconfig.setLabel("创建时间");
		columnconfig.setAutoSize(true);
//		columnconfig.setDataUtilityId("ChangeStateECNDataUtility");
		columnconfig.setWidth(100);
		tableConfig.addComponent(columnconfig);


		//上下文
		columnconfig = (JcaColumnConfig)componentconfigfactory.newColumnConfig("containerName", true);
		columnconfig.setLabel("上下文");
		columnconfig.setAutoSize(true);
//		columnconfig.setDataUtilityId("ChangeStateECNDataUtility");
		columnconfig.setWidth(100);
		tableConfig.addComponent(columnconfig);



		return tableConfig;
	}

}
