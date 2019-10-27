package ext.appo.change.util;

import com.ptc.netmarkets.util.beans.NmCommandBean;
import ext.appo.change.constants.ModifyConstants;
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

import java.util.Iterator;
import java.util.Map;

/**
 * 处理事务性任务(ECA)相关的逻辑
 */
public class TransactionECAUtil implements ChangeConstants, ModifyConstants {

    private static final Logger LOGGER = LogR.getLogger(TransactionECAUtil.class.getName());

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
            if (eca == null) {
                // TODO 创建时需把名称带上
                eca = ModifyUtils.createChangeTask(changeOrder, dataJSONObject.getString(CHANGETHEME_COMPID), null, dataJSONObject.getString(CHANGEDESCRIBE_COMPID), TYPE_3, dataJSONObject.getString(RESPONSIBLE_COMPID));
            } else {
                // 名称修改
                String newName = dataJSONObject.getString(CHANGETHEME_COMPID);
                if (!eca.getName().equals(newName)) {
                    ModifyUtils.setChangeActivity2Name(eca, newName);
                }
                // 工作负责人修改
                ModifyUtils.setChangeActivity2Assignee(eca, dataJSONObject.getString(RESPONSIBLE_COMPID));
                // 说明修改
                String changeDescribe = dataJSONObject.getString(CHANGEDESCRIBE_COMPID);
                if (!changeDescribe.equals(eca.getDescription())) {
                    eca.setDescription(changeDescribe);
                    eca = (WTChangeActivity2) PersistenceHelper.manager.save(eca);
                }
            }

            // 期望完成日期
            if (dataJSONObject.has(ChangeConstants.NEEDDATE_COMPID)) {
                String needDate = dataJSONObject.getString(ChangeConstants.NEEDDATE_COMPID);
                ModifyUtils.updateNeedDate(eca, needDate);
            }


            //创建模型对象，保存事务性任务属性。并与ECN建立关联关系


        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
    }

}
