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
import ext.appo.change.beans.UsabilityChangeTaskBean;
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

@ComponentBuilder("ext.appo.change.mvc.builder.UsabilityChangeTaskTableBuilder")
public class UsabilityChangeTaskTableBuilder extends AbstractComponentBuilder implements ChangeConstants, ModifyConstants {

    private static final String TABLE_ID = "ext.appo.change.mvc.builder.UsabilityChangeTaskTableBuilder";
    private static final Logger LOGGER = LogR.getLogger(UsabilityChangeTaskTableBuilder.class.getName());
    ClientMessageSource messageChange2ClientResource = getMessageSource("ext.appo.change.resource.ModifyResource");

    @Override
    public Object buildComponentData(ComponentConfig config, ComponentParams params) throws Exception {
        Collection<UsabilityChangeTaskBean> collection = new HashSet<>();

        NmHelperBean nmHelperBean = ((JcaComponentParams) params).getHelperBean();
        NmCommandBean nmCommandBean = nmHelperBean.getNmCommandBean();
        Object object = nmCommandBean.getActionOid().getRefObject();
        LOGGER.info("=====buildComponentData.object: " + object);
        boolean bool = CreateAndEditWizBean.isCreateEditWizard(nmCommandBean);//是否创建及编辑状态
        Map<String, UsabilityChangeTaskBean> map = new HashMap<>();
        //将ECN中所有ECA对象转换为UsabilityChangeTaskBean对象
        if (object instanceof WTChangeOrder2) {
            WTChangeOrder2 changeOrder2 = (WTChangeOrder2) object;
            map.putAll(generateUsabilityChangeTaskBeans(changeOrder2));
            if (bool){
                //获取暂存的事务性任务
                collection.addAll(queryTransactionTask(changeOrder2, map));
            }else{
                collection.addAll(queryTransactionTaskNoBool(changeOrder2, map));
            }
        } else if (object instanceof WorkItem) {
            WorkItem workItem = (WorkItem) object;
            Object pbo = workItem.getPrimaryBusinessObject().getObject();
            if (pbo instanceof WTChangeOrder2) {
                WTChangeOrder2 changeOrder2 = (WTChangeOrder2) pbo;
                map.putAll(generateUsabilityChangeTaskBeans(changeOrder2));
                if (bool){
                    //获取暂存的事务性任务
                    collection.addAll(queryTransactionTask(changeOrder2, map));
                }else{
                    collection.addAll(queryTransactionTaskNoBool(changeOrder2, map));
                }
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
        tableConfig.setLabel(this.messageChange2ClientResource.getMessage("CHANGETASK_TABLE_NAME"));
        tableConfig.setSelectable(true);
        tableConfig.setShowCount(true);
        NmHelperBean localNmHelperBean = ((JcaComponentParams) arg0).getHelperBean();
        NmCommandBean localNmCommandBean = localNmHelperBean.getNmCommandBean();
        boolean bool = CreateAndEditWizBean.isCreateEditWizard(localNmCommandBean);//是否创建及编辑状态
        if (bool) {
            tableConfig.setActionModel("changeTask.table.create_remove_edit");
        }
//        else if (object instanceof WorkItem){
//            tableConfig.setActionModel("changeTask.table.create_remove_startup");
//        }

        //任务类型
        JcaColumnConfig columnconfig = (JcaColumnConfig) componentconfigfactory.newColumnConfig(ModifyConstants.TASKTYPE_COMPID, true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_TASKTYPE"));
        columnconfig.setAutoSize(true);
        columnconfig.setWidth(60);
        columnconfig.setDataUtilityId("ModifyChangeTaskDataUtility");
        tableConfig.addComponent(columnconfig);

        //任务主题
        columnconfig = (JcaColumnConfig) componentconfigfactory.newColumnConfig(ChangeConstants.CHANGETHEME_COMPID, true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_CHANGETHEME"));
        columnconfig.setAutoSize(true);
        columnconfig.setWidth(40);
        columnconfig.setDataUtilityId("ModifyChangeTaskDataUtility");
        tableConfig.addComponent(columnconfig);

        //管理方式
        columnconfig = (JcaColumnConfig) componentconfigfactory.newColumnConfig(ModifyConstants.GLFS_COMPID, true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_GLFS"));
        columnconfig.setAutoSize(true);
        columnconfig.setWidth(80);
        columnconfig.setDataUtilityId("ModifyChangeTaskDataUtility");
        tableConfig.addComponent(columnconfig);

        //任务描述
        columnconfig = (JcaColumnConfig) componentconfigfactory.newColumnConfig(ChangeConstants.CHANGEDESCRIBE_COMPID, true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_CHANGEDESCRIBE"));
        columnconfig.setAutoSize(true);
        columnconfig.setWidth(100);
        columnconfig.setDataUtilityId("ModifyChangeTaskDataUtility");
        tableConfig.addComponent(columnconfig);

        //期望完成时间
        columnconfig = (JcaColumnConfig) componentconfigfactory.newColumnConfig(ChangeConstants.NEEDDATE_COMPID, true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_NEEDDATE"));
        columnconfig.setAutoSize(true);
        columnconfig.setWidth(20);
        columnconfig.setDataUtilityId("ModifyChangeTaskDataUtility");
        tableConfig.addComponent(columnconfig);

        //责任人
        columnconfig = (JcaColumnConfig) componentconfigfactory.newColumnConfig(ChangeConstants.RESPONSIBLE_COMPID, true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_RESPONSIBLE"));
        columnconfig.setAutoSize(true);
        columnconfig.setWidth(20);
        columnconfig.setDataUtilityId("ModifyChangeTaskDataUtility");
        tableConfig.addComponent(columnconfig);

        //状态
        columnconfig = (JcaColumnConfig) componentconfigfactory.newColumnConfig(ModifyConstants.TASKSTATE_COMPID, true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_TASKSTATE"));
        columnconfig.setAutoSize(true);
        columnconfig.setWidth(20);
        columnconfig.setDataUtilityId("ModifyChangeTaskDataUtility");
        tableConfig.addComponent(columnconfig);

        //任务单号
        columnconfig = (JcaColumnConfig) componentconfigfactory.newColumnConfig(ModifyConstants.TASKNUMBER_COMPID, true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_TASKNUMBER"));
        columnconfig.setAutoSize(true);
        columnconfig.setWidth(20);
        columnconfig.setDataUtilityId("ModifyChangeTaskDataUtility");
        tableConfig.addComponent(columnconfig);

        if (!bool) {
            //查看信息
            columnconfig = (JcaColumnConfig) componentconfigfactory.newColumnConfig(ModifyConstants.VIEWINFO_COMPID, true);
            columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_VIEWINFO"));
            columnconfig.setAutoSize(true);
            columnconfig.setDataUtilityId("ModifyChangeTaskDataUtility");
            tableConfig.addComponent(columnconfig);
        }

        //实际完成时间
        columnconfig = (JcaColumnConfig) componentconfigfactory.newColumnConfig(ModifyConstants.ACTUALDATE_COMPID, true);
        columnconfig.setLabel(this.messageChange2ClientResource.getMessage("COLUMNNAME_ACTUALDATE"));
        columnconfig.setAutoSize(true);
        columnconfig.setWidth(20);
        columnconfig.setDataUtilityId("ModifyChangeTaskDataUtility");
        tableConfig.addComponent(columnconfig);

        //ECA对象
        columnconfig = (JcaColumnConfig) componentconfigfactory.newColumnConfig(ChangeConstants.CHANGEACTIVITY2_COMPID, true);
        columnconfig.setLabel("ECA");
        columnconfig.setAutoSize(true);
        columnconfig.setHidden(true);
        tableConfig.addComponent(columnconfig);

//        //TaskOid
//        columnconfig = (JcaColumnConfig) componentconfigfactory.newColumnConfig(ModifyConstants.TASKOID_COMPID, true);
//        columnconfig.setLabel("TaskOid");
//        columnconfig.setAutoSize(true);
//        columnconfig.setHidden(true);
//        columnconfig.setDataUtilityId("ModifyChangeTaskDataUtility");
//        tableConfig.addComponent(columnconfig);

        return tableConfig;
    }

    /***
     * 将ECN中所有ECA对象转换为UsabilityChangeTaskBean对象
     * @param changeOrder2
     * @return
     * @throws WTException
     */
    public Map<String, UsabilityChangeTaskBean> generateUsabilityChangeTaskBeans(WTChangeOrder2 changeOrder2) throws WTException {
        Map<String, UsabilityChangeTaskBean> map = new HashMap<>();
        if (changeOrder2 == null) return map;

        // 将ECN中所有ECA对象(事务性任务)转换为UsabilityChangeTaskBean对象
        for (WTChangeActivity2 eca : ModifyUtils.getChangeActivities(changeOrder2)) {
            // TODO 类型判断是否为‘事务性任务’类型
            if (!(PICoreHelper.service.isType(eca, ChangeConstants.TRANSACTIONAL_CHANGEACTIVITY2) || PICoreHelper.service.isType(eca, TYPE_3)))
                continue;

            // ‘已取消’状态不录入
            if (ChangeUtils.checkState(eca, ChangeConstants.CANCELLED)) continue;

            UsabilityChangeTaskBean usabilityChangeTaskBean = new UsabilityChangeTaskBean();
            usabilityChangeTaskBean.setChangeTheme(eca.getName());
            usabilityChangeTaskBean.setChangeDescribe(eca.getDescription());
            // ECA工作负责人
            String assigneeName = "";
            Enumeration<?> roleem = ChangeTaskRoleParticipantHelper.getRoleParticipants(eca, ChangeConstants.ROLE_ASSIGNEE);
            while (roleem.hasMoreElements()) {
                Object object = roleem.nextElement();
                if (object instanceof ObjectReference) {
                    object = ((ObjectReference) object).getObject();
                }
                WTPrincipal principal = (WTPrincipal) object;
                LOGGER.info("=====generateUsabilityChangeTaskBeans.principal: " + principal.getDisplayIdentity());

                if (principal instanceof WTUser) {
                    if (PIStringUtils.isNull(assigneeName)) {
                        assigneeName = ((WTUser) principal).getFullName();
                    } else {
                        assigneeName = assigneeName + ";" + ((WTUser) principal).getFullName();
                    }
                }
            }
            usabilityChangeTaskBean.setResponsible(assigneeName);
            String changeActivity2 = PersistenceHelper.getObjectIdentifier(eca).toString();
            usabilityChangeTaskBean.setChangeActivity2(changeActivity2);
            Object needDate = PIAttributeHelper.service.getValue(eca, ChangeConstants.COMPLETION_TIME);
            usabilityChangeTaskBean.setNeedDate(needDate == null ? "" : (String) needDate);
            Object taskType = PIAttributeHelper.service.getValue(eca, ModifyConstants.TASKTYPE_COMPID);
            usabilityChangeTaskBean.setTaskType(taskType == null ? "" : (String) taskType);
            Object glfs = PIAttributeHelper.service.getValue(eca, ModifyConstants.GLFS_COMPID);
            usabilityChangeTaskBean.setGlfs(glfs == null ? "" : (String) glfs);
            Object taskNumber = eca.getNumber();
            usabilityChangeTaskBean.setTaskNumber(taskNumber == null ? "" : (String) taskNumber);
            map.put(changeActivity2, usabilityChangeTaskBean);
        }
        return map;
    }

        /**
         * 获取暂存的事务性任务（不显示任务主题为空的）
         *
         * @param changeOrder2
         * @param map
         * @return
         * @throws WTException
         */
        public Collection<UsabilityChangeTaskBean> queryTransactionTaskNoBool(WTChangeOrder2 changeOrder2, Map<String, UsabilityChangeTaskBean> map) throws WTException {
            Collection<UsabilityChangeTaskBean> collection = new HashSet<>();

            Set<Persistable> result = ModifyHelper.service.queryPersistable(changeOrder2, LINKTYPE_3);
            for (Persistable persistable : result) {
                if (persistable instanceof TransactionTask) {
                    TransactionTask task = (TransactionTask) persistable;
                    String changeActivity2 = task.getChangeActivity2();
                    if (task.getChangeTheme()==null||task.getChangeTheme().isEmpty()) continue;//（不显示任务主题为空的）
                    UsabilityChangeTaskBean usabilityChangeTaskBean = new UsabilityChangeTaskBean();
                    usabilityChangeTaskBean.setChangeTheme(task.getChangeTheme());
                    usabilityChangeTaskBean.setChangeDescribe(task.getChangeDescribe());
                    usabilityChangeTaskBean.setResponsible(task.getResponsible());
                    usabilityChangeTaskBean.setChangeActivity2(changeActivity2);
                    usabilityChangeTaskBean.setNeedDate(task.getNeedDate());
                    usabilityChangeTaskBean.setTaskType(task.getTaskType());
                    usabilityChangeTaskBean.setGlfs(task.getManagementStyle());
                    NmOid nmOid = NmOid.newNmOid(PICoreHelper.service.getOid(task));
                    usabilityChangeTaskBean.setOid(nmOid);
                    if (map.containsKey(changeActivity2)) {
                        map.get(changeActivity2).setOid(nmOid);
                    } else {
                        collection.add(usabilityChangeTaskBean);
                    }
                }
            }

            return collection;
        }

    /**
     * 获取暂存的事务性任务
     *
     * @param changeOrder2
     * @param map
     * @return
     * @throws WTException
     */
    public Collection<UsabilityChangeTaskBean> queryTransactionTask(WTChangeOrder2 changeOrder2, Map<String, UsabilityChangeTaskBean> map) throws WTException {
        Collection<UsabilityChangeTaskBean> collection = new HashSet<>();

        Set<Persistable> result = ModifyHelper.service.queryPersistable(changeOrder2, LINKTYPE_3);
        for (Persistable persistable : result) {
            if (persistable instanceof TransactionTask) {
                TransactionTask task = (TransactionTask) persistable;
                String changeActivity2 = task.getChangeActivity2();

                UsabilityChangeTaskBean usabilityChangeTaskBean = new UsabilityChangeTaskBean();
                usabilityChangeTaskBean.setChangeTheme(task.getChangeTheme());
                usabilityChangeTaskBean.setChangeDescribe(task.getChangeDescribe());
                usabilityChangeTaskBean.setResponsible(task.getResponsible());
                usabilityChangeTaskBean.setChangeActivity2(changeActivity2);
                usabilityChangeTaskBean.setNeedDate(task.getNeedDate());
                usabilityChangeTaskBean.setTaskType(task.getTaskType());
                usabilityChangeTaskBean.setGlfs(task.getManagementStyle());
                NmOid nmOid = NmOid.newNmOid(PICoreHelper.service.getOid(task));
                usabilityChangeTaskBean.setOid(nmOid);
                if (map.containsKey(changeActivity2)) {
                    map.get(changeActivity2).setOid(nmOid);
                } else {
                    collection.add(usabilityChangeTaskBean);
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
    public void addDatasArray(NmCommandBean nmCommandBean, Collection<UsabilityChangeTaskBean> collection) throws Exception {
        String jsonString = nmCommandBean.getRequest().getParameter(ChangeConstants.CHANGETASKBEAN_ID);
        if (PIStringUtils.isNotNull(jsonString)) {
            collection.removeAll(collection);
            JSONObject object = new JSONObject(jsonString);
            Iterator<?> keyIterator = object.keys();
            while (keyIterator.hasNext()) {
                // 对象ID
                String key = (String) keyIterator.next();
                // 对象数据
                JSONObject jsonObject = new JSONObject(object.getString(key));
                UsabilityChangeTaskBean usabilityChangeTaskBean = new UsabilityChangeTaskBean(key);
                if (jsonObject.has(ChangeConstants.CHANGETHEME_COMPID)) {
                    usabilityChangeTaskBean.setChangeTheme(jsonObject.getString(ChangeConstants.CHANGETHEME_COMPID));
                }
                if (jsonObject.has(ChangeConstants.CHANGEDESCRIBE_COMPID)) {
                    usabilityChangeTaskBean.setChangeDescribe(jsonObject.getString(ChangeConstants.CHANGEDESCRIBE_COMPID));
                }
                if (jsonObject.has(ChangeConstants.RESPONSIBLE_COMPID)) {
                    usabilityChangeTaskBean.setResponsible(jsonObject.getString(ChangeConstants.RESPONSIBLE_COMPID));
                }
                if (jsonObject.has(ChangeConstants.CHANGEACTIVITY2_COMPID)) {
                    usabilityChangeTaskBean.setChangeActivity2(jsonObject.getString(ChangeConstants.CHANGEACTIVITY2_COMPID));
                }
                if (jsonObject.has(ChangeConstants.NEEDDATE_COMPID)) {
                    usabilityChangeTaskBean.setNeedDate(jsonObject.getString(ChangeConstants.NEEDDATE_COMPID));
                }
                if (jsonObject.has(ModifyConstants.TASKTYPE_COMPID)) {
                    usabilityChangeTaskBean.setTaskType(jsonObject.getString(ModifyConstants.TASKTYPE_COMPID));
                }
                if (jsonObject.has(ModifyConstants.GLFS_COMPID)) {
                    usabilityChangeTaskBean.setGlfs(jsonObject.getString(ModifyConstants.GLFS_COMPID));
                }
                collection.add(usabilityChangeTaskBean);
            }
        }
    }

}
