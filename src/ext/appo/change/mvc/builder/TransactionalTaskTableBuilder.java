package ext.appo.change.mvc.builder;

import com.ptc.core.components.beans.CreateAndEditWizBean;
import com.ptc.jca.mvc.components.JcaColumnConfig;
import com.ptc.jca.mvc.components.JcaComponentParams;
import com.ptc.jca.mvc.components.JcaTableConfig;
import com.ptc.mvc.components.*;
import com.ptc.mvc.util.ClientMessageSource;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.beans.NmHelperBean;
import com.ptc.windchill.enterprise.change2.ChangeTaskRoleParticipantHelper;
import ext.appo.change.ModifyHelper;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.models.TransactionTask;
import ext.appo.change.util.ModifyUtils;
import ext.appo.ecn.beans.ChangeTaskBean;
import ext.appo.ecn.common.util.ChangeUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.lang.PIStringUtils;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PICoreHelper;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.util.WTException;
import wt.workflow.work.WorkItem;

import java.util.*;

@ComponentBuilder("ext.appo.change.mvc.builder.TransactionalTaskTableBuilder")
public class TransactionalTaskTableBuilder extends AbstractComponentBuilder implements ChangeConstants, ModifyConstants {

    private static final String TABLE_ID = "ext.appo.change.mvc.builder.TransactionalTaskTableBuilder";
    private static final Logger LOGGER = LogR.getLogger(TransactionalTaskTableBuilder.class.getName());
    ClientMessageSource source = getMessageSource("ext.appo.ecn.resource.changeNoticeActionsRB");

    @Override
    public Object buildComponentData(ComponentConfig config, ComponentParams params) throws Exception {
        Collection<ChangeTaskBean> collection = new HashSet<>();

        NmHelperBean nmHelperBean = ((JcaComponentParams) params).getHelperBean();
        NmCommandBean nmCommandBean = nmHelperBean.getNmCommandBean();
        Object object = nmCommandBean.getActionOid().getRefObject();
        LOGGER.info("=====buildComponentData.object: " + object);

        Map<String, ChangeTaskBean> map = new HashMap<>();
        //将ECN中所有ECA对象转换为ChangeTaskBean对象
        if (object instanceof WorkItem) {
            WorkItem workItem = (WorkItem) object;
            Object pbo = workItem.getPrimaryBusinessObject().getObject();
            if (pbo instanceof WTChangeOrder2) {
                WTChangeOrder2 changeOrder2 = (WTChangeOrder2) pbo;
                map.putAll(generateChangeTaskBeans(changeOrder2));

                //获取暂存的事务性任务
                collection.addAll(queryTransactionTask(changeOrder2, map));
            }
        }
        LOGGER.info("=====buildComponentData.map: " + map);
        collection.addAll(map.values());

        //添加新增数据
        addDatasArray(nmCommandBean, collection);
        LOGGER.info("=====buildComponentData.collection: " + collection);

        return collection;
    }

    @Override
    public ComponentConfig buildComponentConfig(ComponentParams arg0) throws WTException {
        ComponentConfigFactory componentconfigfactory = getComponentConfigFactory();
        JcaTableConfig tableConfig = (JcaTableConfig) componentconfigfactory.newTableConfig();

        tableConfig.setId(TABLE_ID);
        tableConfig.setLabel(this.source.getMessage("CHANGETASK_TABLE_NAME"));
        tableConfig.setSelectable(true);
        tableConfig.setShowCount(true);

        NmHelperBean localNmHelperBean = ((JcaComponentParams) arg0).getHelperBean();
        NmCommandBean localNmCommandBean = localNmHelperBean.getNmCommandBean();
        boolean bool = CreateAndEditWizBean.isCreateEditWizard(localNmCommandBean);
        tableConfig.setActionModel("changeTask.table.create_remove_edit");

        //变更主题
        JcaColumnConfig columnconfig = (JcaColumnConfig) componentconfigfactory.newColumnConfig(CHANGETHEME_COMPID, true);
        columnconfig.setLabel(this.source.getMessage("COLUMNNAME_CHANGETHEME"));
        columnconfig.setAutoSize(true);
        columnconfig.setWidth(40);
        columnconfig.setDataUtilityId("TransactionalTaskDataUtility");
        tableConfig.addComponent(columnconfig);

        //变更任务描述
        columnconfig = (JcaColumnConfig) componentconfigfactory.newColumnConfig(CHANGEDESCRIBE_COMPID, true);
        columnconfig.setLabel(this.source.getMessage("COLUMNNAME_CHANGEDESCRIBE"));
        columnconfig.setAutoSize(true);
        columnconfig.setWidth(100);
        columnconfig.setDataUtilityId("TransactionalTaskDataUtility");
        tableConfig.addComponent(columnconfig);

        //期望完成日期
        columnconfig = (JcaColumnConfig) componentconfigfactory.newColumnConfig(NEEDDATE_COMPID, true);
        columnconfig.setLabel(this.source.getMessage("COLUMNNAME_NEEDDATE"));
        columnconfig.setAutoSize(true);
        columnconfig.setWidth(20);
        columnconfig.setDataUtilityId("TransactionalTaskDataUtility");
        tableConfig.addComponent(columnconfig);

        //责任人
        columnconfig = (JcaColumnConfig) componentconfigfactory.newColumnConfig(RESPONSIBLE_COMPID, true);
        columnconfig.setLabel(this.source.getMessage("COLUMNNAME_RESPONSIBLE"));
        columnconfig.setAutoSize(true);
        columnconfig.setWidth(20);
        columnconfig.setDataUtilityId("TransactionalTaskDataUtility");
        tableConfig.addComponent(columnconfig);

        //ECA对象
        columnconfig = (JcaColumnConfig) componentconfigfactory.newColumnConfig(CHANGEACTIVITY2_COMPID, true);
        columnconfig.setLabel("ECA");
        columnconfig.setAutoSize(true);
        columnconfig.setHidden(true);
        tableConfig.addComponent(columnconfig);

        return tableConfig;
    }

    /***
     * 将ECN中所有ECA对象转换为ChangeTaskBean对象
     * @param changeOrder2
     * @return
     * @throws WTException
     */
    public Map<String, ChangeTaskBean> generateChangeTaskBeans(WTChangeOrder2 changeOrder2) throws WTException {
        Map<String, ChangeTaskBean> map = new HashMap<>();
        if (changeOrder2 == null) return map;

        // 将ECN中所有ECA对象(事务性任务)转换为ChangeTaskBean对象
        for (WTChangeActivity2 eca : ModifyUtils.getChangeActivities(changeOrder2)) {
            // TODO 类型判断是否为‘事务性任务’类型
            if (!PICoreHelper.service.isType(eca, TRANSACTIONAL_CHANGEACTIVITY2)) continue;

            // ‘已取消’状态不录入
            if (ChangeUtils.checkState(eca, CANCELLED)) continue;

            ChangeTaskBean changeTaskBean = new ChangeTaskBean();
            changeTaskBean.setChangeTheme(eca.getName());
            changeTaskBean.setChangeDescribe(eca.getDescription());
            // ECA工作负责人
            String assigneeName = "";
            Enumeration<?> roleem = ChangeTaskRoleParticipantHelper.getRoleParticipants(eca, ROLE_ASSIGNEE);
            while (roleem.hasMoreElements()) {
                Object object = roleem.nextElement();
                if (object instanceof ObjectReference) {
                    object = ((ObjectReference) object).getObject();
                }
                WTPrincipal principal = (WTPrincipal) object;
                LOGGER.info("=====generateChangeTaskBeans.principal: " + principal.getDisplayIdentity());

                if (principal instanceof WTUser) {
                    if (PIStringUtils.isNull(assigneeName)) {
                        assigneeName = ((WTUser) principal).getFullName();
                    } else {
                        assigneeName = assigneeName + ";" + ((WTUser) principal).getFullName();
                    }
                }
            }
            changeTaskBean.setResponsible(assigneeName);
            String changeActivity2 = PersistenceHelper.getObjectIdentifier(eca).toString();
            changeTaskBean.setChangeActivity2(changeActivity2);
            Object needDate = PIAttributeHelper.service.getValue(eca, COMPLETION_TIME);
            changeTaskBean.setNeedDate(needDate == null ? "" : (String) needDate);

            map.put(changeActivity2, changeTaskBean);
        }

        return map;
    }

    /**
     * 获取暂存的事务性任务
     *
     * @param changeOrder2
     * @param map
     * @return
     * @throws WTException
     */
    public Collection<ChangeTaskBean> queryTransactionTask(WTChangeOrder2 changeOrder2, Map<String, ChangeTaskBean> map) throws WTException {
        Collection<ChangeTaskBean> collection = new HashSet<>();

        Set<Persistable> result = ModifyHelper.service.queryPersistable(changeOrder2, LINKTYPE_3);
        for (Persistable persistable : result) {
            if (persistable instanceof TransactionTask) {
                TransactionTask task = (TransactionTask) persistable;
                String changeActivity2 = task.getChangeActivity2();

                ChangeTaskBean changeTaskBean = new ChangeTaskBean();
                changeTaskBean.setChangeTheme(task.getChangeTheme());
                changeTaskBean.setChangeDescribe(task.getChangeDescribe());
                changeTaskBean.setResponsible(task.getResponsible());
                changeTaskBean.setChangeActivity2(changeActivity2);
                changeTaskBean.setNeedDate(task.getNeedDate());

                NmOid nmOid = NmOid.newNmOid(PICoreHelper.service.getOid(task));
                changeTaskBean.setOid(nmOid);
                if (map.containsKey(changeActivity2)) {
                    map.get(changeActivity2).setOid(nmOid);
                } else {
                    collection.add(changeTaskBean);
                }
            }
        }

        return collection;
    }

    /**
     * 新增数据，处理重复数据
     *
     * @param nmCommandBean
     * @param collection
     * @throws WTException
     */
    public void addDatasArray(NmCommandBean nmCommandBean, Collection<ChangeTaskBean> collection) throws Exception {
        String jsonString = nmCommandBean.getRequest().getParameter(CHANGETASKBEAN_ID);
        if (PIStringUtils.isNotNull(jsonString)) {
            collection.removeAll(collection);
            JSONObject object = new JSONObject(jsonString);
            Iterator<?> keyIterator = object.keys();
            while (keyIterator.hasNext()) {
                // 对象ID
                String key = (String) keyIterator.next();
                // 对象数据
                JSONObject jsonObject = new JSONObject(object.getString(key));
                ChangeTaskBean changeTaskBean = new ChangeTaskBean(key);
                if (jsonObject.has(CHANGETHEME_COMPID)) {
                    changeTaskBean.setChangeTheme(jsonObject.getString(CHANGETHEME_COMPID));
                }
                if (jsonObject.has(CHANGEDESCRIBE_COMPID)) {
                    changeTaskBean.setChangeDescribe(jsonObject.getString(CHANGEDESCRIBE_COMPID));
                }
                if (jsonObject.has(RESPONSIBLE_COMPID)) {
                    changeTaskBean.setResponsible(jsonObject.getString(RESPONSIBLE_COMPID));
                }
                if (jsonObject.has(CHANGEACTIVITY2_COMPID)) {
                    changeTaskBean.setChangeActivity2(jsonObject.getString(CHANGEACTIVITY2_COMPID));
                }
                if (jsonObject.has(NEEDDATE_COMPID)) {
                    changeTaskBean.setNeedDate(jsonObject.getString(NEEDDATE_COMPID));
                }
                collection.add(changeTaskBean);
            }
        }
    }

}
