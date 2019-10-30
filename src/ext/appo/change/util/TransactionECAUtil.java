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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 处理事务性任务(ECA)相关的逻辑
 */
public class TransactionECAUtil implements ChangeConstants, ModifyConstants {

    private static final Logger LOGGER = LogR.getLogger(TransactionECAUtil.class.getName());
    public Set<TransactionTask> TASKS = new HashSet<>();//事务性任务模型对象

    /**
     * 创建事务性任务(ECA)
     * @param changeOrder2
     * @param nmcommandBean
     * @throws WTException
     */
    public TransactionECAUtil(WTChangeOrder2 changeOrder2, NmCommandBean nmcommandBean) throws WTException {
        createEditChangeActivity2(changeOrder2, nmcommandBean);
    }

    /***
     * 创建或更新ECA对象信息
     * @param changeOrder
     * @param nmcommandBean
     * @throws WTException
     */
    public void createEditChangeActivity2(WTChangeOrder2 changeOrder, NmCommandBean nmcommandBean) throws WTException {
        try {
            Map<String, Object> parameterMap = nmcommandBean.getParameterMap();
            LOGGER.info(">>>>>>>>>>parameterMap: " + parameterMap);
            if (parameterMap.containsKey(DATA_ARRAY)) {
                String[] datasArrayStr = (String[]) parameterMap.get(DATA_ARRAY);
                if (datasArrayStr != null && datasArrayStr.length > 0) {
                    String datasJSON = datasArrayStr[0];
                    LOGGER.info(">>>>>>>>>>datasJSON: " + datasJSON);
                    if (PIStringUtils.isNotNull(datasJSON)) {
                        JSONObject jsonObject = new JSONObject(datasJSON);
                        Iterator<?> keyIterator = jsonObject.keys();
                        while (keyIterator.hasNext()) {
                            Object key = keyIterator.next();
                            // 数据信息
                            JSONObject dataJSONObject = new JSONObject(jsonObject.getString((String) key));
                            WTChangeActivity2 eca = null;
                            // ECA对象OID
                            if (dataJSONObject.has(CHANGEACTIVITY2_COMPID)) {
                                String ecaOID = dataJSONObject.getString(CHANGEACTIVITY2_COMPID);
                                if (PIStringUtils.isNotNull(ecaOID)) {
                                    eca = (WTChangeActivity2) (new ReferenceFactory()).getReference(ecaOID).getObject();
                                }
                            }
                            createEditChangeActivity2(changeOrder, dataJSONObject, eca);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
    }

    /***
     * 创建或更新ECA对象信息
     * @param changeOrder
     *            ECN
     * @param dataJSONObject
     *            需要更新的数据集
     * @param eca
     * @throws WTException
     */
    public void createEditChangeActivity2(WTChangeOrder2 changeOrder, JSONObject dataJSONObject, WTChangeActivity2 eca) throws WTException {
        try {

            String changeTheme = "";//变更主题
            if (dataJSONObject.has(CHANGETHEME_COMPID))
                changeTheme = dataJSONObject.getString(CHANGETHEME_COMPID);//变更主题
            String changeDescribe = "";//变更任务描述
            if (dataJSONObject.has(CHANGEDESCRIBE_COMPID))
                changeDescribe = dataJSONObject.getString(CHANGEDESCRIBE_COMPID);//变更任务描述
            String responsible = "";//责任人
            if (dataJSONObject.has(RESPONSIBLE_COMPID)) responsible = dataJSONObject.getString(RESPONSIBLE_COMPID);//责任人
            String needDate = "";//期望完成日期
            if (dataJSONObject.has(NEEDDATE_COMPID)) needDate = dataJSONObject.getString(NEEDDATE_COMPID);//期望完成日期
            LOGGER.info(">>>>>>>>>>changeTheme: " + changeTheme + " changeDescribe: " + changeDescribe + " responsible: " + responsible + " needDate: " + needDate);

            if (eca == null) {
                eca = ModifyUtils.createChangeTask(changeOrder,changeTheme , null, changeDescribe, TYPE_3, responsible);
            } else {
                // 名称修改
                if (!eca.getName().equals(changeTheme)) {
                    ModifyUtils.setChangeActivity2Name(eca, changeTheme);
                }
                // 工作负责人修改
                ModifyUtils.setChangeActivity2Assignee(eca, responsible);
                // 说明修改
                if (!changeDescribe.equals(eca.getDescription())) {
                    eca.setDescription(changeDescribe);
                    eca = (WTChangeActivity2) PersistenceHelper.manager.save(eca);
                }
            }

            // 期望完成日期
            if (dataJSONObject.has(ChangeConstants.NEEDDATE_COMPID)) {
                ModifyUtils.updateNeedDate(eca, needDate);
            }

            //创建模型对象，保存事务性任务属性
            TransactionTask task = ModifyHelper.service.queryTransactionTask(eca);
            if (task == null) {
                TASKS.add(ModifyHelper.service.newTransactionTask(changeTheme, changeDescribe, responsible, needDate, eca));
            } else {
                TASKS.add(ModifyHelper.service.updateTransactionTask(task, changeTheme, changeDescribe, responsible, needDate));
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
    }

}
