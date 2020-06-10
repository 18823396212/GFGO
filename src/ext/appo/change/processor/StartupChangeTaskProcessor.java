package ext.appo.change.processor;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.ecn.constants.ChangeConstants;
import ext.lang.PIStringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import wt.log4j.LogR;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.util.WTException;

import java.util.*;

public class StartupChangeTaskProcessor extends DefaultObjectFormProcessor implements ChangeConstants, ModifyConstants {

    private static final Logger LOGGER = LogR.getLogger(StartupChangeTaskProcessor.class.getName());

    @Override
    public FormResult doOperation(NmCommandBean nmcommandBean, List<ObjectBean> objectBeans) throws WTException {
        FormResult result = new FormResult();
        result.setStatus(FormProcessingStatus.SUCCESS);
        SessionContext previous = SessionContext.newContext();
        //收集事务性任务表单属性
        try {
            SessionHelper.manager.setAdministrator();// 当前用户设置为管理员，用于忽略权限
            if (nmcommandBean != null) {
                Set<Map<String, String>> transActions = new HashSet<>();//事务性任务表单
                Set<String> repetition = new HashSet<>();//重复的事务性任务名称
                Set<String> theme = new HashSet<>();
                Map<String, Object> parameterMap = nmcommandBean.getParameterMap();
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
                                //add by lzy at 20200417 start
                                String taskType = "";//任务类型
                                if (dataJSONObject.has(TASKTYPE_COMPID))
                                    taskType = dataJSONObject.getString(TASKTYPE_COMPID);//任务类型
                                String clfs = "";//管理方式
                                if (dataJSONObject.has(GLFS_COMPID))
                                    clfs = dataJSONObject.getString(GLFS_COMPID);//管理方式
//                                String taskState = "";//状态
//                                if (dataJSONObject.has(TASKSTATE_COMPID))
//                                    taskState = dataJSONObject.getString(TASKSTATE_COMPID);//状态
                                String taskNumber = "";//任务单号
                                if (dataJSONObject.has(TASKNUMBER_COMPID))
                                    taskNumber = dataJSONObject.getString(TASKNUMBER_COMPID);//任务单号
//                                String actualDate = "";//实际完成时间
//                                if (dataJSONObject.has(ACTUALDATE_COMPID))
//                                    actualDate = dataJSONObject.getString(ACTUALDATE_COMPID);//实际完成时间
                                //add by lzy at 20200417 end
                                Map<String, String> attributeMap = new HashMap<>();
                                attributeMap.put(CHANGETHEME_COMPID, changeTheme);//变更主题
                                attributeMap.put(CHANGEDESCRIBE_COMPID, changeDescribe);//变更任务描述
                                attributeMap.put(RESPONSIBLE_COMPID, responsible);//责任人
                                attributeMap.put(NEEDDATE_COMPID, needDate);//期望完成日期
                                attributeMap.put(CHANGEACTIVITY2_COMPID, changeActivity2);//ECA
                                //add by lzy at 20200417 start
                                attributeMap.put(TASKTYPE_COMPID, taskType);//任务类型
                                attributeMap.put(GLFS_COMPID, clfs);//管理方式
//                                attributeMap.put(TASKSTATE_COMPID, taskState);//状态
                                attributeMap.put(TASKNUMBER_COMPID, taskNumber);//任务单号
//                                attributeMap.put(ACTUALDATE_COMPID, actualDate);//实际完成时间
                                //add by lzy at 20200417 end
                                transActions.add(attributeMap);
                                if (theme.contains(changeTheme)) repetition.add(changeTheme);
                                else theme.add(changeTheme);
                            }
                        }
                    }
                }
                if (repetition.size() > 0) {
                    throw new WTException("事务性任务列表中存在「任务主题」重复的的数据！");
                }
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        } finally {
            SessionContext.setContext(previous);
        }

        return result;
    }
}
