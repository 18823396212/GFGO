package ext.appo.change.mvc.builder;

import com.ptc.jca.mvc.components.JcaComponentParams;
import com.ptc.jca.mvc.components.JcaTableConfig;
import com.ptc.mvc.components.*;
import com.ptc.mvc.util.ClientMessageSource;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.beans.NmHelperBean;
import ext.appo.change.ModifyHelper;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.util.ModifyUtils;
import ext.appo.ecn.constants.ChangeConstants;
import org.apache.log4j.Logger;
import org.springframework.security.access.method.P;
import wt.change2.Changeable2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.workflow.work.WorkItem;

import java.util.*;
import java.util.stream.Collectors;

@ComponentBuilder("ext.appo.change.mvc.builder.AffectedTableBuilder")
public class AffectedTableBuilder extends AbstractComponentBuilder implements ChangeConstants, ModifyConstants {

    private static final Logger LOGGER = LogR.getLogger(AffectedTableBuilder.class.getName());
    private static final String TABLE_ID = "ext.appo.change.mvc.builder.AffectedTableBuilder";
    ClientMessageSource source = getMessageSource("ext.appo.ecn.resource.changeNoticeActionsRB");

    @Override
    public Object buildComponentData(ComponentConfig paramComponentConfig, ComponentParams paramComponentParams) throws Exception {
        List result = new ArrayList<>();
        SessionContext previous = SessionContext.newContext();
        try {
            SessionHelper.manager.setAdministrator();
            NmHelperBean nmhelperbean = ((JcaComponentParams) paramComponentParams).getHelperBean();
            NmCommandBean commandBean = nmhelperbean.getNmCommandBean();
            Object object = commandBean.getActionOid().getRefObject();// 获取操作对象
            LOGGER.info("=====buildComponentData.object: " + object);

            //获取已暂存或已创建ECA的数据
            Set<Persistable> collection = new HashSet<>();
            if (object instanceof WorkItem) {
                WorkItem workItem = (WorkItem) object;
                Object pbo = workItem.getPrimaryBusinessObject().getObject();
                if (pbo instanceof WTChangeOrder2) {
                    WTChangeOrder2 changeOrder2 = (WTChangeOrder2) pbo;

                    // 获取ECN中所有受影响对象
                    Map<WTChangeActivity2, Collection<Changeable2>> dataMap = ModifyUtils.getChangeablesBefore(changeOrder2);
                    for (Map.Entry<WTChangeActivity2, Collection<Changeable2>> entry : dataMap.entrySet()) {
                        collection.addAll(entry.getValue());
                    }

                    //获取ECN暂存的受影响对象
                    collection.addAll(ModifyHelper.service.queryPersistable(changeOrder2, LINKTYPE_1));
                }
            }
            result.addAll(collection.stream().collect(Collectors.toList()));


            Map map = new HashMap();
            map.put("number", CONSTANTS_9);
            map.put("name", "");
            map.put("version", "");
            map.put("state", "");
            map.put("ArticleInventory", "");
            map.put("ArticleDispose", "");
            map.put("PassageInventory", "");
            map.put("PassageDispose", "");
            map.put("CentralWarehouseInventory", "");
            map.put("InventoryDispose", "");
            map.put("ProductDispose", "");
            map.put("ChangeObjectType", "");
            map.put("ChangeType", "");
            map.put("CompletionTime", "");
            map.put("ResponsiblePerson", "");
            map.put("aadDescription", "");
            map.put("CollectionNumber", "");
            map.put("ApprovalOpinion", "");
            map.put("Remark", "");
            result.add(map);


        } finally {
            SessionContext.setContext(previous);
        }
        return result;
    }

    @Override
    public ComponentConfig buildComponentConfig(ComponentParams paramComponentParams) throws WTException {
        ComponentConfigFactory componentconfigfactory = getComponentConfigFactory();
        JcaTableConfig tableConfig = (JcaTableConfig) componentconfigfactory.newTableConfig();

        tableConfig.setLabel(this.source.getMessage("changeNotice.affectedItemsTableBuilder.description"));
        tableConfig.setId(TABLE_ID);
        //tableConfig.setSelectable(true);
        tableConfig.setShowCount(true);

        /*NmHelperBean localNmHelperBean = ((JcaComponentParams) paramComponentParams).getHelperBean();
        NmCommandBean localNmCommandBean = localNmHelperBean.getNmCommandBean();
        boolean bool = CreateAndEditWizBean.isCreateEditWizard(localNmCommandBean);
        if (bool) {
            tableConfig.setActionModel("dcn.affectedItems.table.create_edit");
        }*/

        ColumnConfig columnconfig = componentconfigfactory.newColumnConfig("number", true);
//        columnconfig.setAutoSize(true);
        columnconfig.setWidth(80);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("name", true);
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("version", true);
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("state", true);
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("ArticleInventory", true);
        columnconfig.setLabel(this.source.getMessage("ARTICLEINVENTORY"));
        columnconfig.setDataUtilityId("AffectedDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("ArticleDispose", true);
        columnconfig.setLabel(this.source.getMessage("ARTICLEDISPOSE"));
        columnconfig.setDataUtilityId("AffectedDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("PassageInventory", true);
        columnconfig.setLabel(this.source.getMessage("PASSAGEINVENTORY"));
        columnconfig.setDataUtilityId("AffectedDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("PassageDispose", true);
        columnconfig.setLabel(this.source.getMessage("PASSAGEDISPOSE"));
        columnconfig.setDataUtilityId("AffectedDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("CentralWarehouseInventory", true);
        columnconfig.setLabel(this.source.getMessage("CENTRALWAREHOUSEINVENTORY"));
        columnconfig.setDataUtilityId("AffectedDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("InventoryDispose", true);
        columnconfig.setLabel(this.source.getMessage("INVENTORYDISPOSE"));
        columnconfig.setDataUtilityId("AffectedDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("ProductDispose", true);
        columnconfig.setLabel(this.source.getMessage("PRODUCTDISPOSE"));
        columnconfig.setDataUtilityId("AffectedDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        //add by lzy at 20191231 start
//        columnconfig = componentconfigfactory.newColumnConfig("ChangeObjectType", true);
//        columnconfig.setLabel(this.source.getMessage("CHANGEOBJECTTYPE"));
//        //columnconfig.setDataUtilityId("AffectedDataUtility");
//        columnconfig.setAutoSize(true);
//        tableConfig.addComponent(columnconfig);//变更对象类型
        //add by lzy at 20191231 end

        columnconfig = componentconfigfactory.newColumnConfig("ChangeType", true);
        columnconfig.setLabel(this.source.getMessage("CHANGETYPE"));
        //columnconfig.setDataUtilityId("AffectedDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("CompletionTime", true);
        columnconfig.setLabel(this.source.getMessage("COMPLETIONTIME"));
        //columnconfig.setDataUtilityId("AffectedDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("ResponsiblePerson", true);
        columnconfig.setLabel(this.source.getMessage("RESPONSIBLEPERSON"));
        //columnconfig.setDataUtilityId("ModifyUserPickerDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("aadDescription", true);
        columnconfig.setLabel(this.source.getMessage("AADDESCRIPTION"));
        columnconfig.setDataUtilityId("AffectedDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("ApprovalOpinion", true);
        columnconfig.setLabel("审批意见");
        columnconfig.setDataUtilityId("AffectedDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("Remark", true);
        columnconfig.setLabel("备注（驳回必填）");
        columnconfig.setDataUtilityId("AffectedDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("CollectionNumber", true);
        columnconfig.setLabel("收集对象");
        columnconfig.setDataUtilityId("AffectedDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        return tableConfig;
    }

}
