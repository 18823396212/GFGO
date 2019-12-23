package ext.appo.change.util;

import com.ptc.netmarkets.util.beans.NmCommandBean;
import ext.appo.change.ModifyHelper;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.ecn.common.util.ChangeUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.ecn.pdf.PdfUtil;
import ext.appo.part.filter.StandardPartsRevise;
import ext.lang.PIStringUtils;
import ext.pi.core.PICoreHelper;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import wt.change2.ChangeHelper2;
import wt.change2.ChangeOrder2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.identity.IdentityFactory;
import wt.lifecycle.LifeCycleManaged;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.util.WTException;
import wt.vc.VersionControlHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 处理暂存受影响对象列表相关的逻辑
 */
public class CacheAffectedObjectUtil implements ChangeConstants, ModifyConstants {

    private static final Logger LOGGER = LogR.getLogger(CacheAffectedObjectUtil.class.getName());
    private NmCommandBean NMCOMMANDBEAN;
    private WTChangeOrder2 ORDER2;
    public Map<Persistable, Map<String, String>> PAGEDATAMAP = new HashMap<>();//ECN受影响对象集合
    private Set<WTPart> AFFECTEDPART = new HashSet<>();//ECN受影响对象-部件集合
    private Collection<WTPart> LVERSIONPART = new HashSet<>();//用户所选"类型"为「升版」的部件
    private Set<String> MESSAGES = new HashSet<>();

    public CacheAffectedObjectUtil(NmCommandBean nmcommandBean, WTChangeOrder2 changeOrder2) throws WTException {
        NMCOMMANDBEAN = nmcommandBean;
        ORDER2 = changeOrder2;
        if (NMCOMMANDBEAN != null && ORDER2 != null) {
            //获取页面中受影响对象列表数据，以及属性集合
            getPageChangeTaskArray();
            checkEnvProtection(changeOrder2);
            /*
             * 9.0、至少一条受影响对象，必填项验证。
             * 9.1、检查受影响对象是否存在未结束的ECN，有则不允许创建。
             * 9.2、检查受影响对象的状态必须是已归档及已发布。
             * 9.3、检查受影响对象不能为标准件。
             */
            checkOne();
        }
    }

    /***
     * 获取页面中受影响对象列表数据，以及属性集合
     * @throws WTException
     */
    private void getPageChangeTaskArray() throws WTException {
        try {
            // 获取新增数据列
            Map<String, Object> parameterMap = NMCOMMANDBEAN.getParameterMap();
            if (parameterMap.containsKey(CHANGETASK_ARRAY)) {
                String[] changeTaskArrayStr = (String[]) parameterMap.get(CHANGETASK_ARRAY);
                if (changeTaskArrayStr != null && changeTaskArrayStr.length > 0) {
                    String datasJSON = changeTaskArrayStr[0];
                    LOGGER.info(">>>>>>>>>>datasJSON: " + datasJSON);
                    if (PIStringUtils.isNotNull(datasJSON)) {
                        JSONArray jsonArray = new JSONArray(datasJSON);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            Persistable persistable = null;
                            // 存储页面属性信息
                            Map<String, String> attributesMap = new HashMap<>();
                            JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                            Iterator<String> keyIterator = jsonObject.keys();
                            while (keyIterator.hasNext()) {
                                // 属性ID
                                String key = keyIterator.next();
                                // 属性值
                                String value = jsonObject.getString(key);
                                if (PIStringUtils.isNotNull(value)) {
                                    if (key.equalsIgnoreCase(OID_COMPID)) {
                                        persistable = (new ReferenceFactory()).getReference(value).getObject();
                                        continue;
                                    }
                                    if (key.equals(COMPLETIONTIME_COMPID)) {
                                        if (value.contains(" ")) {
                                            value = value.substring(0, value.indexOf(" ")).trim();
                                        }
                                        if (value.contains("-")) {
                                            value = (new SimpleDateFormat(ChangeConstants.SIMPLE_DATE_FORMAT_02)).format((new SimpleDateFormat(ChangeConstants.SIMPLE_DATE_FORMAT_03)).parse(value));
                                        }
                                    }
                                    attributesMap.put(key, value);
                                }
                            }
                            if (persistable != null) {
                                PAGEDATAMAP.put(persistable, attributesMap);
                            }
                            if (persistable instanceof WTPart) {
                                WTPart part = (WTPart) persistable;
                                AFFECTEDPART.add(part);

                                if (attributesMap.containsKey(CHANGETYPE_COMPID)) {
                                    String changeType = attributesMap.get(CHANGETYPE_COMPID);
                                    if (PIStringUtils.isNotNull(changeType)) {
                                        //用户所选"类型"为「升版」的部件
                                        if (changeType.contains(VALUE_4)) LVERSIONPART.add(part);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
    }

    /**
     * 9.0、至少一条受影响对象，必填项验证。
     * 9.1、检查受影响对象是否存在未结束的ECN，有则不允许创建。
     * 9.2、检查受影响对象的状态必须是已归档及已发布。
     * 9.3、检查受影响对象不能为标准件。
     */
    private void checkOne() throws WTException {
        //9.0、至少一条受影响对象，必填项验证。
        if (PAGEDATAMAP.isEmpty()) {
            MESSAGES.add("受影响对象列表不能为空！");
        }

        //9.1、检查受影响对象是否存在未结束的ECN（包含的ECA非取消状态、以及暂存状态的ECN），有则不允许创建。（已取消）
        //9.1、检查受影响对象是否存在未结束的ECN（无需判断ECA状态、以及暂存状态的ECN），有则不允许创建。
        for (Persistable persistable : PAGEDATAMAP.keySet()) {
            if (persistable instanceof WTPart) {
                WTPart part = (WTPart) persistable;
                String number = part.getNumber();
                LOGGER.info(">>>>>>>>>>part:" + number);

                boolean flog = true;
                //先检查每个大版本的最新小版本是否有关联的ECA、ECN非「已取消」「已解决」状态
                QueryResult queryResult = VersionControlHelper.service.allVersionsOf(part.getMaster());//获取所有大版本的最新小版本
                while (queryResult.hasMoreElements()) {
                    WTPart oldPart = (WTPart) queryResult.nextElement();

                    boolean flag = false;
                    //获取对象所有关联的ECA对象
                    QueryResult result = ChangeHelper2.service.getAffectingChangeActivities(oldPart);
                    LOGGER.info(">>>>>>>>>>result size:" + result.size());
                    while (result.hasMoreElements()) {
                        WTChangeActivity2 changeActivity2 = (WTChangeActivity2) result.nextElement();
                        LOGGER.info(">>>>>>>>>>changeActivity2:" + changeActivity2.getNumber());
//                        //判断关联的ECA是否非「已取消」「已解决」状态
//                        if ((!ChangeUtils.checkState(changeActivity2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeActivity2, ChangeConstants.RESOLVED))) {
//                            MESSAGES.add("物料: " + number + " 存在未解决的ECA: " + changeActivity2.getNumber() + " 不能同时提交两个ECA！");
//                            flag = true;
//                            flog = false;
//                            break;
//                        }

                        WTChangeOrder2 changeOrder2 = ChangeUtils.getEcnByEca(changeActivity2);
                        LOGGER.info(">>>>>>>>>>changeOrder2:" + changeOrder2.getNumber());
                        if (!ORDER2.getNumber().startsWith(changeOrder2.getNumber())) {
//                            //判断关联的ECN是否非「已取消」「已解决」状态
//                            if ((!ChangeUtils.checkState(changeOrder2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeOrder2, ChangeConstants.RESOLVED))) {
//                                MESSAGES.add("物料: " + number + " 存在未解决的ECN: " + changeOrder2.getNumber() + " 不能同时提交两个ECN！");
//                                flag = true;
//                                flog = false;
//                                break;
//                            }
                            //add by lzy at 20191130 start
                            //判断关联的ECN是否非「已取消」「已解决」状态，用户所选"类型"为「替换」的部件则无需判断
                            if ((!ChangeUtils.checkState(changeOrder2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeOrder2, ChangeConstants.RESOLVED))) {
                                if (!LVERSIONPART.contains(part)){
                                    break;
                                }else{
                                    MESSAGES.add("物料: " + number + " 存在未解决的ECN: " + changeOrder2.getNumber() + " 不能同时提交两个ECN！");
                                    flag = true;
                                    flog = false;
                                    break;
                                }
                            }
                            //add by lzy at 20191130 end

                        }
                    }
                    if (flag) break;
                }

                //再检查「暂存」的情况，遍历所有大版本的最新小版本检查是否关联非「已取消」「已解决」状态的ECN
                if (flog) {
                    boolean flag = false;
                    QueryResult result = VersionControlHelper.service.allVersionsOf(part.getMaster());//获取所有大版本的最新小版本
                    while (result.hasMoreElements()) {
                        WTPart oldPart = (WTPart) result.nextElement();
                        String branchId = String.valueOf(PICoreHelper.service.getBranchId(oldPart));
                        LOGGER.info(">>>>>>>>>>checkTwo.branchId: " + branchId);

                        Set<WTChangeOrder2> order2s = ModifyHelper.service.queryWTChangeOrder2(branchId, ModifyConstants.LINKTYPE_1);
                        LOGGER.info(">>>>>>>>>>checkTwo.order2s: " + order2s);
                        for (WTChangeOrder2 changeOrder2 : order2s) {
                            LOGGER.info(">>>>>>>>>>checkTwo.changeOrder2:" + changeOrder2.getNumber());
                            if (!ORDER2.getNumber().startsWith(changeOrder2.getNumber())) {
//                                //判断关联的ECN是否非「已取消」「已解决」状态
//                                if ((!ChangeUtils.checkState(changeOrder2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeOrder2, ChangeConstants.RESOLVED))) {
//                                    MESSAGES.add("物料: " + number + " 存在未解决的ECN: " + changeOrder2.getNumber() + " 不能同时提交两个ECN！");
//                                    flag = true;
//                                    break;
//                                }
                                //add by lzy at 20191130 start
                                //判断关联的ECN是否非「已取消」「已解决」状态，用户所选"类型"为「替换」的部件则无需判断
                                if ((!ChangeUtils.checkState(changeOrder2, ChangeConstants.CANCELLED)) && (!ChangeUtils.checkState(changeOrder2, ChangeConstants.RESOLVED))) {
                                    if (!LVERSIONPART.contains(part)){
                                        break;
                                    }else{
                                        MESSAGES.add("物料: " + number + " 存在未解决的ECN: " + changeOrder2.getNumber() + " 不能同时提交两个ECN！");
                                        flag = true;
                                        break;
                                    }
                                }
                                //add by lzy at 20191130 end
                            }
                        }
                        if (flag) break;
                    }
                }
            }
        }

        //9.2、检查受影响对象的状态必须是已归档及已发布。
        StringBuilder messages = new StringBuilder();
        for (Persistable persistable : PAGEDATAMAP.keySet()) {
            if (!ModifyUtils.checkState((LifeCycleManaged) persistable, ChangeConstants.ARCHIVED) && !ModifyUtils.checkState((LifeCycleManaged) persistable, ChangeConstants.RELEASED)) {
                messages.append(IdentityFactory.getDisplayIdentifier(persistable)).append("\n");
            }
        }
        if (messages.length() > 0) MESSAGES.add(messages.toString() + " 状态不满足：已归档或已发布！");

        //9.3、检查受影响对象不能为标准件。
        messages = new StringBuilder();
        try {
            List<Map> list = StandardPartsRevise.getExcelData();
            for (WTPart part : AFFECTEDPART) {
                if (LVERSIONPART.contains(part) && ModifyUtils.isStandardPart(list, part)) {
                    messages.append(part.getNumber()).append("、");
                }
            }

        } catch (WTException | IOException e) {
            throw new WTException(e.getStackTrace());
        }
        if (messages.length() > 0) MESSAGES.add(messages.toString() + " 业务定义为标准件，不能变更升版！");
    }

    /**
     * 合成错误信息
     * @return
     */
    public String compoundMessage() {
        StringBuilder builder = new StringBuilder();
        if (MESSAGES.size() > 0) {
            builder.append("无法创建变更申请，存在以下问题：").append("\n");
            int i = 1;
            for (String message : MESSAGES) {
                builder.append(i++).append(". ").append(message).append("\n");
            }
        }
        return builder.toString();
    }


    /**
     * 若是否经过环保评审为否，环保说明必填
     *
     * @param order
     * @throws WTException
     */
    public void checkEnvProtection(ChangeOrder2 ecn) throws WTException {
        if (PdfUtil.getIBAObjectValue(ecn, "ISEnvProtectionReview") != null
                && PdfUtil.getIBAObjectValue(ecn, "ISEnvProtectionReview") instanceof String) {
            String ISEnvProtectionReview = (String) PdfUtil.getIBAObjectValue(ecn, "ISEnvProtectionReview");
            if ("否".equals(ISEnvProtectionReview)) {
                if (PdfUtil.getIBAObjectValue(ecn, "EnvProtectionDesc") == null
                        || (PdfUtil.getIBAObjectValue(ecn, "EnvProtectionDesc") instanceof String
                        && "".equals((String) PdfUtil.getIBAObjectValue(ecn, "EnvProtectionDesc")))) {
                    throw new WTException(" 未经过环保评审，请您必须填写'环保说明'的属性字段.");
                }
            }
        }
    }

}
