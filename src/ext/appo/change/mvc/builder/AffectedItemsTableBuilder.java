package ext.appo.change.mvc.builder;

import com.ptc.core.components.beans.CreateAndEditWizBean;
import com.ptc.jca.mvc.components.JcaComponentParams;
import com.ptc.jca.mvc.components.JcaTableConfig;
import com.ptc.mvc.components.*;
import com.ptc.mvc.util.ClientMessageSource;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.beans.NmHelperBean;
import ext.appo.change.ModifyHelper;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.util.AffectedObjectUtil;
import ext.appo.change.util.ModifyUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.lang.PIStringUtils;
import ext.pi.core.PIWorkflowHelper;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import wt.change2.ChangeHelper2;
import wt.change2.Changeable2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.log4j.LogR;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WorkItem;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@ComponentBuilder("ext.appo.change.mvc.builder.AffectedItemsTableBuilder")
public class AffectedItemsTableBuilder extends AbstractComponentBuilder implements ChangeConstants, ModifyConstants {

    private static final Logger LOGGER = LogR.getLogger(AffectedItemsTableBuilder.class.getName());
    private static final String TABLE_ID = "ext.appo.change.mvc.builder.AffectedItemsTableBuilder";
    ClientMessageSource messageChange2ClientResource = getMessageSource("ext.appo.ecn.resource.changeNoticeActionsRB");

    @Override
    public Object buildComponentData(ComponentConfig paramComponentConfig, ComponentParams paramComponentParams) throws Exception {
        Collection<Persistable> result = new HashSet<>();

        SessionContext previous = SessionContext.newContext();
        try {
            SessionHelper.manager.setAdministrator();
            NmHelperBean nmhelperbean = ((JcaComponentParams) paramComponentParams).getHelperBean();
            NmCommandBean commandbean = nmhelperbean.getNmCommandBean();
            HttpServletRequest request = commandbean.getRequest();
            Map<String, Object> parameterMap = commandbean.getParameterMap();
            Object object = commandbean.getActionOid().getRefObject();// 获取操作对象
            LOGGER.info("=====buildComponentData.object: " + object);

            AffectedObjectUtil affectedObjectUtil = new AffectedObjectUtil(commandbean, null);
            Map<Persistable, Map<String, String>> pageDataMap = affectedObjectUtil.PAGEDATAMAP;
            if (pageDataMap.isEmpty()) {
                if (!parameterMap.containsKey(CHANGETASK_ARRAY)) {
                    //获取已暂存或已创建ECA的数据
                    if (object instanceof WTChangeOrder2) {
                        WTChangeOrder2 changeOrder2 = (WTChangeOrder2) object;

                        // 获取ECN中所有受影响对象
                        Map<WTChangeActivity2, Collection<Changeable2>> dataMap = ModifyUtils.getChangeablesBefore(changeOrder2);
                        for (Map.Entry<WTChangeActivity2, Collection<Changeable2>> entry : dataMap.entrySet()) {
                            result.addAll(entry.getValue());
                        }

                        //获取ECN暂存的受影响对象
                        result.addAll(ModifyHelper.service.queryPersistable(changeOrder2, LINKTYPE_1));
                    }else if (object instanceof WorkItem) {
                        //在流程中
                        WTChangeOrder2 changeOrder2 = new WTChangeOrder2();
                        WorkItem workItem = (WorkItem) object;
                        WfProcess wfprocess = PIWorkflowHelper.service.getParentProcess(workItem);
                        Object[] objects = wfprocess.getContext().getObjects();
                        if (objects.length > 0) {
                            objects:for (int i = 0; i < objects.length; i++) {
                                if (objects[i] instanceof WTChangeActivity2) { //eca
                                    WTChangeActivity2 eca = (WTChangeActivity2) objects[i];
                                    QueryResult ecaqr = ChangeHelper2.service.getChangeOrder(eca);
                                    while (ecaqr.hasMoreElements()) {
                                        Object ecn = ecaqr.nextElement();
                                        if (ecn instanceof WTChangeOrder2) {
                                            changeOrder2 = (WTChangeOrder2) ecn;
                                            break objects;
                                        }
                                    }

                                }
                            }
                        }
                        // 获取ECN中所有受影响对象
                        Map<WTChangeActivity2, Collection<Changeable2>> dataMap = ModifyUtils.getChangeablesBefore(changeOrder2);
                        for (Map.Entry<WTChangeActivity2, Collection<Changeable2>> entry : dataMap.entrySet()) {
                            result.addAll(entry.getValue());
                        }

                        //获取ECN暂存的受影响对象
                        result.addAll(ModifyHelper.service.queryPersistable(changeOrder2, LINKTYPE_1));

                    }
                }
            } else {
                result.addAll(pageDataMap.keySet());
            }

            // 获取新增数据
            Map<String, String> collectionMap = new HashMap<>();
            String selectOids = request.getParameter("selectOids");
            LOGGER.info("=====buildComponentData.selectOids: " + selectOids);
            if (PIStringUtils.isNotNull(selectOids)) {
                JSONArray jsonArray = new JSONArray(selectOids);
                for (int i = 0; i < jsonArray.length(); i++) {
                    String oid = jsonArray.getString(i);
                    String collection = "";
                    if (oid.startsWith("{") && oid.endsWith("}")) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        oid = jsonObject.getString("oid");
                        collection = jsonObject.getString("collection");
                    }
                    collectionMap.put(oid, collection);
                    result.add((new ReferenceFactory()).getReference(oid).getObject());
                }
            }
            commandbean.getParameterMap().put("collectionObjectMap", collectionMap);
        } finally {
            SessionContext.setContext(previous);
        }

        return result;
    }

    @Override
    public ComponentConfig buildComponentConfig(ComponentParams paramComponentParams) throws WTException {
        ComponentConfigFactory componentconfigfactory = getComponentConfigFactory();
        JcaTableConfig tableConfig = (JcaTableConfig) componentconfigfactory.newTableConfig();

        tableConfig.setLabel(this.messageChange2ClientResource.getMessage("changeNotice.affectedItemsTableBuilder.description"));
        tableConfig.setId(TABLE_ID);
        tableConfig.setSelectable(true);
        tableConfig.setShowCount(true);

        NmHelperBean localNmHelperBean = ((JcaComponentParams) paramComponentParams).getHelperBean();
        NmCommandBean localNmCommandBean = localNmHelperBean.getNmCommandBean();
        boolean bool = CreateAndEditWizBean.isCreateEditWizard(localNmCommandBean);
        if (bool) {
            tableConfig.setActionModel("dcn.affectedItems.table.create_edit");
        }

        ColumnConfig columnconfig = componentconfigfactory.newColumnConfig("name", true);
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("number", true);
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("version", true);
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("state", true);
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("ArticleInventory", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("ARTICLEINVENTORY"));
        columnconfig.setDataUtilityId("ModifyAffectedItemsDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("ArticleDispose", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("ARTICLEDISPOSE"));
        columnconfig.setDataUtilityId("ModifyAffectedItemsDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("PassageInventory", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("PASSAGEINVENTORY"));
        columnconfig.setDataUtilityId("ModifyAffectedItemsDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("PassageDispose", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("PASSAGEDISPOSE"));
        columnconfig.setDataUtilityId("ModifyAffectedItemsDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("CentralWarehouseInventory", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("CENTRALWAREHOUSEINVENTORY"));
        columnconfig.setDataUtilityId("ModifyAffectedItemsDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("InventoryDispose", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("INVENTORYDISPOSE"));
        columnconfig.setDataUtilityId("ModifyAffectedItemsDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("ProductDispose", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("PRODUCTDISPOSE"));
        columnconfig.setDataUtilityId("ModifyAffectedItemsDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);
        //add by lzy at20191231 start
//        columnconfig = componentconfigfactory.newColumnConfig("ChangeObjectType", true);
//        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("CHANGEOBJECTTYPE"));
//        columnconfig.setDataUtilityId("ModifyAffectedItemsDataUtility");
//        columnconfig.setAutoSize(true);
//        tableConfig.addComponent(columnconfig);//变更对象类型
        //add by lzy at20191231 end

        columnconfig = componentconfigfactory.newColumnConfig("ChangeType", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("CHANGETYPE"));
        columnconfig.setDataUtilityId("ModifyAffectedItemsDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);


        columnconfig = componentconfigfactory.newColumnConfig("CompletionTime", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COMPLETIONTIME"));
        columnconfig.setDataUtilityId("ModifyAffectedItemsDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("ResponsiblePerson", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("RESPONSIBLEPERSON"));
        columnconfig.setDataUtilityId("ModifyUserPickerDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("aadDescription", true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("AADDESCRIPTION"));
        columnconfig.setDataUtilityId("ModifyAffectedItemsDataUtility");
        columnconfig.setAutoSize(true);
        tableConfig.addComponent(columnconfig);

        columnconfig = componentconfigfactory.newColumnConfig("CollectionNumber", true);
        columnconfig.setLabel("收集对象");

        //add by lzy at 20200312 start
        String dataUtilityId="ModifyAffectedItemsDataUtility";
        Object object = localNmCommandBean.getActionOid().getRefObject();// 获取操作对象
        if (object instanceof WTChangeOrder2) {
            WTChangeOrder2 ecn=(WTChangeOrder2)object;
            String templateName="";
            QueryResult result = WfEngineHelper.service.getAssociatedProcesses(ecn, null, null);
            while (result.hasMoreElements()) {
                WfProcess process = (WfProcess) result.nextElement();
                if (process != null) {
                    templateName = process.getTemplate().getName();
                }
            }
            //不是新流程ECN则使用原来的DataUtilityId
            if (!"APPO_ECNWF".equals(templateName)){
                dataUtilityId="customizationDataUtility";
            }
        }
        columnconfig.setDataUtilityId(dataUtilityId);
        //add by lzy at 20200312 end
//        columnconfig.setDataUtilityId("ModifyAffectedItemsDataUtility");
        columnconfig.setWidth(60);
        tableConfig.addComponent(columnconfig);

        return tableConfig;
    }

}
