package ext.appo.change.util;

import com.ptc.netmarkets.util.beans.NmCommandBean;
import ext.appo.change.ModifyHelper;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.models.TransactionTask;
import ext.appo.ecn.constants.ChangeConstants;
import ext.lang.PIStringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.PersistenceHelper;
import wt.fc.ReferenceFactory;
import wt.log4j.LogR;
import wt.util.WTException;

import java.util.*;

/**
 * 处理事务性任务(ECA)相关的逻辑
 */
public class TransactionECAUtil implements ChangeConstants, ModifyConstants {

    private static final Logger LOGGER = LogR.getLogger(TransactionECAUtil.class.getName());
    private WTChangeOrder2 ORDER2;
    private NmCommandBean NMCOMMANDBEAN;
    public Set<TransactionTask> TASKS = new HashSet<>();//事务性任务模型对象
    private Set<Map<String, String>> TRANSACTIONS = new HashSet<>();//事务性任务表单
    private Set<String> REPETITION = new HashSet<>();//重复的事务性任务名称
    public Set<String> MESSAGES = new HashSet<>();

    public TransactionECAUtil(WTChangeOrder2 changeOrder2, NmCommandBean nmcommandBean) throws WTException {
        NMCOMMANDBEAN = nmcommandBean;
        ORDER2 = changeOrder2;
        //收集事务性任务表单属性
        collectionOne();
    }

    /**
     * 收集事务性任务表单属性
     * @throws WTException
     */
    private void collectionOne() throws WTException {
        if (NMCOMMANDBEAN != null) {
            try {
                Set<String> theme = new HashSet<>();
                Map<String, Object> parameterMap = NMCOMMANDBEAN.getParameterMap();
                LOGGER.info(">>>>>>>>>>collectionOne.parameterMap: " + parameterMap);
                if (parameterMap.containsKey(DATA_ARRAY)) {
                    String[] datasArrayStr = (String[]) parameterMap.get(DATA_ARRAY);
                    if (datasArrayStr != null && datasArrayStr.length > 0) {
                        String datasJSON = datasArrayStr[0];
                        LOGGER.info(">>>>>>>>>>collectionOne.datasJSON: " + datasJSON);
                        if (PIStringUtils.isNotNull(datasJSON)) {
                            JSONObject jsonObject = new JSONObject(datasJSON);
                            Iterator<?> keyIterator = jsonObject.keys();
                            while (keyIterator.hasNext()) {
                                Object key = keyIterator.next();
                                // 数据信息
                                JSONObject dataJSONObject = new JSONObject(jsonObject.getString((String) key));
                                LOGGER.info(">>>>>>>>>>collectionOne.dataJSONObject: " + dataJSONObject);

                                String changeTheme = "";//变更主题
                                if (dataJSONObject.has(CHANGETHEME_COMPID))
                                    changeTheme = dataJSONObject.getString(CHANGETHEME_COMPID);//变更主题
                                String changeDescribe = "";//变更任务描述
                                if (dataJSONObject.has(CHANGEDESCRIBE_COMPID))
                                    changeDescribe = dataJSONObject.getString(CHANGEDESCRIBE_COMPID);//变更任务描述
                                String responsible = "";//责任人
                                if (dataJSONObject.has(RESPONSIBLE_COMPID))
                                    responsible = dataJSONObject.getString(RESPONSIBLE_COMPID);//责任人
                                String needDate = "";//期望完成日期
                                if (dataJSONObject.has(NEEDDATE_COMPID))
                                    needDate = dataJSONObject.getString(NEEDDATE_COMPID);//期望完成日期
                                String changeActivity2 = "";//ECA
                                if (dataJSONObject.has(CHANGEACTIVITY2_COMPID))
                                    changeActivity2 = dataJSONObject.getString(CHANGEACTIVITY2_COMPID);//ECA
                                LOGGER.info(">>>>>>>>>>changeTheme: " + changeTheme + " changeDescribe: " + changeDescribe + " responsible: " + responsible + " needDate: " + needDate + " changeActivity2: " + changeActivity2);

                                Map<String, String> attributeMap = new HashMap<>();
                                attributeMap.put(CHANGETHEME_COMPID, changeTheme);//变更主题
                                attributeMap.put(CHANGEDESCRIBE_COMPID, changeDescribe);//变更任务描述
                                attributeMap.put(RESPONSIBLE_COMPID, responsible);//责任人
                                attributeMap.put(NEEDDATE_COMPID, needDate);//期望完成日期
                                attributeMap.put(CHANGEACTIVITY2_COMPID, changeActivity2);//ECA
                                TRANSACTIONS.add(attributeMap);

                                if (theme.contains(changeTheme)) REPETITION.add(changeTheme);
                                else theme.add(changeTheme);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new WTException(e.getStackTrace());
            }
        }
        LOGGER.info(">>>>>>>>>>collectionOne.TRANSACTIONS: " + TRANSACTIONS);
    }

    /**
     * 校验 任务主题 是否重复
     * @throws WTException
     */
    public void check() throws WTException {
        if (REPETITION.size() > 0) {
            MESSAGES.add("事务性任务列表中存在「任务主题」重复的的数据！");
        }
    }

    /***
     * 创建或更新ECA对象信息
     */
    public void createEditChangeActivity2() throws WTException {
        if (ORDER2 != null) {
            try {
                for (Map<String, String> attributeMap : TRANSACTIONS) {
                    String changeTheme = attributeMap.get(CHANGETHEME_COMPID);//变更主题
                    String changeDescribe = attributeMap.get(CHANGEDESCRIBE_COMPID);//变更任务描述
                    String responsible = attributeMap.get(RESPONSIBLE_COMPID);//责任人
                    String needDate = attributeMap.get(NEEDDATE_COMPID);//期望完成日期
                    String changeActivity2 = attributeMap.get(CHANGEACTIVITY2_COMPID);//ECA
                    LOGGER.info(">>>>>>>>>>changeTheme: " + changeTheme + " changeDescribe: " + changeDescribe + " responsible: " + responsible + " needDate: " + needDate + " changeActivity2: " + changeActivity2);

                    WTChangeActivity2 activity2 = null;
                    if (PIStringUtils.isNotNull(changeActivity2)) {
                        activity2 = (WTChangeActivity2) (new ReferenceFactory()).getReference(changeActivity2).getObject();
                    }

                    if (activity2 == null) {
                        activity2 = ModifyUtils.createChangeTask(ORDER2, changeTheme, null, changeDescribe, TYPE_3, responsible);
                    } else {
                        // 名称修改
                        if (!activity2.getName().equals(changeTheme)) {
                            ModifyUtils.setChangeActivity2Name(activity2, changeTheme);
                        }
                        // 工作负责人修改
                        ModifyUtils.setChangeActivity2Assignee(activity2, responsible);
                        // 说明修改
                        if (!changeDescribe.equals(activity2.getDescription())) {
                            activity2.setDescription(changeDescribe);
                            activity2 = (WTChangeActivity2) PersistenceHelper.manager.save(activity2);
                        }
                    }

                    // 期望完成日期
                    if (PIStringUtils.isNotNull(needDate)) {
                        ModifyUtils.updateNeedDate(activity2, needDate);
                    }
                }
            } catch (Exception e) {
                throw new WTException(e.getStackTrace());
            }
        }
    }

    /**
     * 创建事务性任务-模型对象
     * @return
     * @throws WTException
     */
    public void createTransactionECA() throws WTException {
        if (ORDER2 != null) {
            for (Map<String, String> attributeMap : TRANSACTIONS) {
                String changeTheme = attributeMap.get(CHANGETHEME_COMPID);//变更主题
                String changeDescribe = attributeMap.get(CHANGEDESCRIBE_COMPID);//变更任务描述
                String responsible = attributeMap.get(RESPONSIBLE_COMPID);//责任人
                String needDate = attributeMap.get(NEEDDATE_COMPID);//期望完成日期
                String changeActivity2 = attributeMap.get(CHANGEACTIVITY2_COMPID);//ECA
                LOGGER.info(">>>>>>>>>>changeTheme: " + changeTheme + " changeDescribe: " + changeDescribe + " responsible: " + responsible + " needDate: " + needDate + " changeActivity2: " + changeActivity2);

                WTChangeActivity2 activity2 = null;
                if (PIStringUtils.isNotNull(changeActivity2)) {
                    activity2 = (WTChangeActivity2) (new ReferenceFactory()).getReference(changeActivity2).getObject();
                }

                //创建模型对象，保存事务性任务属性
                TransactionTask task = ModifyHelper.service.queryTransactionTask(ORDER2, activity2, changeTheme);
                if (task == null) {
                    task = ModifyHelper.service.newTransactionTask(changeTheme, changeDescribe, responsible, needDate, activity2);
                } else {
                    task = ModifyHelper.service.updateTransactionTask(task, changeTheme, changeDescribe, responsible, needDate);
                }
                TASKS.add(task);
            }
            LOGGER.info(">>>>>>>>>>createTransactionECA.TASKS: " + TASKS);
        }
    }

}
