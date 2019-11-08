package ext.appo.change.workflow;

import com.lowagie.text.DocumentException;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.util.ModifyUtils;
import ext.appo.ecn.common.util.ChangePartQueryUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.part.beans.EffectiveBaselineBean;
import ext.appo.part.util.EffecitveBaselineUtil;
import ext.appo.part.util.MversionControlHelper;
import ext.appo.part.workflow.PartWorkflowUtil;
import ext.appo.util.PartUtil;
import ext.customer.common.MBAUtil;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIClassificationHelper;
import ext.pi.core.PICoreHelper;
import ext.pi.core.PIPartHelper;
import org.apache.log4j.Logger;
import wt.change2.Changeable2;
import wt.change2.WTChangeActivity2;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTList;
import wt.lifecycle.LifeCycleManaged;
import wt.log4j.LogR;
import wt.part.*;
import wt.util.WTException;
import wt.vc.config.LatestConfigSpec;
import wt.vc.wip.Workable;
import wt.workflow.engine.WfProcess;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class ECAWorkflowUtil implements ChangeConstants, ModifyConstants {

    private static Logger LOGGER = LogR.getLogger(ECAWorkflowUtil.class.getName());
    private Set<String> MESSAGES = new HashSet<>();

    /***
     * 检查子件状态是否为‘已归档’或‘已发布’
     *
     * @param pbo
     * @throws WTException
     */
    public void checkChildState(WTObject pbo) throws WTException {
        if (!(pbo instanceof WTChangeActivity2)) return;

        Collection<Changeable2> changeable2s = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
        LOGGER.info("=====checkChildState.changeable2s: " + changeable2s.size());
        for (Changeable2 changeable2 : changeable2s) {
            LOGGER.info("=====checkChildState.changeable2: " + changeable2);
            if (changeable2 instanceof WTPart) {
                WTPart parent = (WTPart) changeable2;
                String parentNumber = parent.getNumber();
                LOGGER.info("=====checkChildState.parentNumber: " + parentNumber);

                //获取部件下所有子件信息
                Collection<WTPart> collection = ChangePartQueryUtils.getPartMultiwallStructure(parent);
                LOGGER.info("=====checkChildState.collection: " + collection);
                if (collection.isEmpty()) return;

                if (!parentNumber.startsWith("A") && !parentNumber.startsWith("B"))
                    throw new WTException("除成品、半成品外不允许搭BOM！");

                //检查首层子件特定替代料状态
                MESSAGES.addAll(checkAllSubstitute(parent));

                String parentView = parent.getViewName();
                LOGGER.info("=====checkChildState.parentView: " + parentView);
                for (WTPart part : collection) {
                    String childNumber = part.getNumber();
                    LOGGER.info("=====checkChildState.childNumber: " + childNumber);
                    String state = part.getLifeCycleState().toString();
                    LOGGER.info("=====checkChildState.state: " + state);
                    String view = part.getViewName();
                    LOGGER.info("=====checkChildState.view: " + view);

                    if (parentView.equalsIgnoreCase("Design") && view.equalsIgnoreCase("Manufacturing")) {
                        MESSAGES.add("子件「" + childNumber + "」为制造视图(Manufacturing)，不能加到设计视图(Design)「" + parentNumber + "」的BOM中！");
                    }

                    if (!state.equals(ARCHIVED) && !state.equals(RELEASED)) {
                        MESSAGES.add("子件「" + childNumber + "」的状态不为“已归档”或“已发布”！");
                    }

                    if (childNumber.startsWith("Q")) {
                        MESSAGES.add("子件「" + childNumber + "」为临时编码，不能添加到「" + parentNumber + "」的BOM中！");
                    }

                    //检查子件特定替代料
                    MESSAGES.addAll(checkAllSubstitute(part));


                    //检查全局替代料状态
                    MESSAGES.addAll(checkAlternateLinks(part));
                }
            }
        }
    }

    /***
     * 检查BOM结构中位号是否包含中文字符
     * @ PCBA位号增加校验，需添加校验：第一位为字母第二位不允许为0 -mao
     * @param pbo
     * @throws WTException
     */
    public void checkReferenceDesignatorRange(WTObject pbo) throws WTException {
        if (!(pbo instanceof WTChangeActivity2)) return;

        Collection<Changeable2> changeable2s = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
        LOGGER.info("=====checkReferenceDesignatorRange.changeable2s: " + changeable2s.size());
        for (Changeable2 changeable2 : changeable2s) {
            LOGGER.info("=====checkReferenceDesignatorRange.changeable2: " + changeable2);
            if (changeable2 instanceof WTPart) {
                WTPart parent = (WTPart) changeable2;
                WTCollection collection = PIPartHelper.service.findChildrenLinks(parent);//获取BOM结构
                LOGGER.info("=====checkReferenceDesignatorRange.collection: " + collection.size());
                for (Object object : collection) {
                    if (object instanceof ObjectReference) {
                        object = ((ObjectReference) object).getObject();
                    }
                    LOGGER.info("=====checkReferenceDesignatorRange.object: " + object);
                    if (object instanceof WTPartUsageLink) {
                        WTPartUsageLink usageLink = (WTPartUsageLink) object;
                        String value = MBAUtil.getValue(usageLink, ATTRIBUTE_5) == null ? "" : (String) MBAUtil.getValue(usageLink, ATTRIBUTE_5);
                        LOGGER.info("=====checkReferenceDesignatorRange.value: " + value);
                        // 检查位号是否存在中文字符
                        char[] valueArray = value.toCharArray();
                        for (char chinese : valueArray) {
                            if (isChinese(chinese)) {
                                MESSAGES.add(usageLink.getDisplayIdentity() + "「位号」中含有中文字符！");
                            }
                        }
                        if (valueArray.length >= 2) {
                            char c1 = valueArray[0];
                            char c2 = valueArray[1];
                            if (!Character.isLetter(c1)) {
                                MESSAGES.add(usageLink.getDisplayIdentity() + "「位号」第一位需为字母！");
                            }
                            if (c2 == '0') {
                                MESSAGES.add(usageLink.getDisplayIdentity() + "「位号」第二位不能为0！");
                            }
                        }
                    }
                }
            }
        }
    }

    /***
     * 检查BOM中一个子件不能存在多行
     * @param pbo
     * @throws WTException
     */
    public void checkSameChildParts(WTObject pbo) throws WTException {
        if (!(pbo instanceof WTChangeActivity2)) return;

        Collection<Changeable2> changeable2s = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
        LOGGER.info("=====checkSameChildParts.changeable2s: " + changeable2s.size());
        for (Changeable2 changeable2 : changeable2s) {
            LOGGER.info("=====checkSameChildParts.changeable2: " + changeable2);
            if (changeable2 instanceof WTPart) {
                WTPart parent = (WTPart) changeable2;

                Collection<WTPart> childArray = new HashSet<>();//所有子件集合
                Set<WTPart> repetitions = new HashSet<>();//重复子件集合
                // 查询第一层结构信息
                WTList list = new WTArrayList();
                list.add(parent);
                QueryResult queryResult = ChangePartQueryUtils.getPartsFirstStructure(list, new LatestConfigSpec());
                LOGGER.info("=====checkSameChildParts.queryResult: " + queryResult.size());
                while (queryResult.hasMoreElements()) {
                    Persistable[] persistables = (Persistable[]) queryResult.nextElement();
                    LOGGER.info("=====checkSameChildParts.persistables: " + persistables.length);
                    Object object = persistables[1];//子件对象
                    LOGGER.info("=====checkSameChildParts.object: " + object);
                    if (object instanceof WTPart) {
                        WTPart childPart = (WTPart) object;
                        LOGGER.info("=====checkSameChildParts.childArray: " + childArray);
                        if (childArray.contains(childPart)) {
                            repetitions.add(childPart);
                        } else {
                            childArray.add(childPart);
                        }
                    }
                }

                for (WTPart part : repetitions) {
                    MESSAGES.add("BOM「" + parent.getNumber() + "」的子件「" + part.getNumber() + "」存在多行！");
                }
            }
        }
    }

    /**
     * 检查BOM中子件的单位和物料的单位是否一致
     * @param pbo
     * @throws WTException
     */
    public void checkBOMUnit(WTObject pbo) throws WTException {
        if (!(pbo instanceof WTChangeActivity2)) return;

        Collection<Changeable2> changeable2s = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
        LOGGER.info("=====checkBOMUnit.changeable2s: " + changeable2s.size());
        for (Changeable2 changeable2 : changeable2s) {
            LOGGER.info("=====checkBOMUnit.changeable2: " + changeable2);
            if (changeable2 instanceof WTPart) {
                WTPart parent = (WTPart) changeable2;
                QueryResult result = WTPartHelper.service.getUsesWTPartMasters(parent);
                while (result.hasMoreElements()) {
                    WTPartUsageLink link = (WTPartUsageLink) result.nextElement();
                    String childNumber = link.getUses().getNumber();
                    LOGGER.info("=====checkBOMUnit.childNumber: " + childNumber);

                    WTPart child = PartUtil.getLastestWTPartByNumber(childNumber);
                    LOGGER.info("=====checkBOMUnit.child: " + child);

                    String defaultUnit = CommonPDMUtil.getUnitCN(child.getDefaultUnit());
                    LOGGER.info("=====checkBOMUnit.defaultUnit: " + defaultUnit);

                    String unit = CommonPDMUtil.getUnitCN(link.getQuantity().getUnit());
                    LOGGER.info("=====checkBOMUnit.unit: " + unit);

                    if (unit.contains("item") && defaultUnit.contains("PCS")) {

                    } else {
                        if (!defaultUnit.equalsIgnoreCase(unit)) {
                            MESSAGES.add(parent.getNumber() + "的子件：" + childNumber + "单位: " + unit + " ，与物料单位：" + defaultUnit + "不符！");
                        }
                    }
                }
            }
        }
    }

    /**
     * 检查受影响对象、产生对象是否检出
     * @param pbo
     * @throws WTException
     */
    public void isCheckout(WTObject pbo) throws WTException {
        if (!(pbo instanceof WTChangeActivity2)) return;

        Collection<Changeable2> befores = ModifyUtils.getChangeablesBefore((WTChangeActivity2) pbo);
        LOGGER.info("=====isCheckout.befores: " + befores.size());
        for (Changeable2 before : befores) {
            LOGGER.info("=====isCheckout.before: " + before);
            if (!(before instanceof Workable)) continue;
            if (PICoreHelper.service.isCheckout((Workable) before)) {
                MESSAGES.add("受影响对象「" + ModifyUtils.getNumber(before) + "」被检出！");
            }
        }

        Collection<Changeable2> afters = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
        LOGGER.info("=====isCheckout.afters: " + afters.size());
        for (Changeable2 after : afters) {
            LOGGER.info("=====isCheckout.after: " + after);
            if (!(after instanceof Workable)) continue;
            if (PICoreHelper.service.isCheckout((Workable) after)) {
                MESSAGES.add("产生对象「" + ModifyUtils.getNumber(after) + "」被检出！");
            }
        }
    }

    /**
     * 检查全局替代料状态的状态是否为已归档或已发布
     * @param part
     * @return
     * @throws WTException
     */
    private Set<String> checkAlternateLinks(WTPart part) throws WTException {
        Set<String> messages = new HashSet<>();
        WTCollection collection = WTPartHelper.service.getAlternateLinks(part.getMaster());
        LOGGER.info("=====checkAlternateLinks.collection: " + collection.size());
        for (Object object : collection) {
            if (object instanceof ObjectReference) {
                ObjectReference reference = (ObjectReference) object;
                object = reference.getObject();
            }
            LOGGER.info("=====checkAlternateLinks.object: " + object);
            if (object instanceof WTPartAlternateLink) {
                WTPartAlternateLink link = (WTPartAlternateLink) object;
                WTPartMaster alternateFor = link.getAlternateFor();
                WTPartMaster alternate = link.getAlternates();
                LOGGER.info("=====checkAlternateLinks.alternateFor: " + alternateFor);
                LOGGER.info("=====checkAlternateLinks.alternate: " + alternate);

                WTPart alternatePart = PartUtil.getLastestWTPartByNumber(alternate.getNumber());
                LOGGER.info("=====checkAlternateLinks.alternatePart: " + alternatePart);
                String state = alternatePart.getState().toString();
                LOGGER.info("=====checkAlternateLinks.state: " + state);
                if (!state.equalsIgnoreCase(RELEASED) && !state.equalsIgnoreCase(ARCHIVED)) {
                    messages.add("部件「" + alternateFor.getNumber() + "」的全局替代料「" + alternatePart.getNumber() + "」状态不为“已归档”或“已发布”！");
                }
            }
        }
        return messages;
    }

    /**
     * 检查特定替代料的状态是否为已归档或已发布
     * @param part
     * @return
     * @throws WTException
     */
    private Set<String> checkAllSubstitute(WTPart part) throws WTException {
        Set<String> messages = new HashSet<>();
        Map<WTPartMaster, Set<WTPart>> substituteMap = checkAllSubstitutePart(part);
        LOGGER.info("=====checkAllSubstitute.substituteMap: " + substituteMap);
        for (Map.Entry<WTPartMaster, Set<WTPart>> entry : substituteMap.entrySet()) {
            WTPartMaster master = entry.getKey();
            Set<WTPart> result = entry.getValue();
            LOGGER.info("=====checkAllSubstitute.master: " + master);
            LOGGER.info("=====checkAllSubstitute.result: " + result);
            for (WTPart substitute : result) {
                messages.add("部件「" + master.getNumber() + "」的特定替代料「" + substitute.getNumber() + "」状态不为“已归档”或“已发布”！");
            }
        }
        return messages;
    }

    /**
     * 获取状态不为已归档、已发布的特定替代料
     * @param part
     * @return
     * @throws WTException
     */
    private Map<WTPartMaster, Set<WTPart>> checkAllSubstitutePart(WTPart part) throws WTException {
        Map<WTPartMaster, Set<WTPart>> map = new HashMap<>();

        QueryResult qr = WTPartHelper.service.getUsesWTPartMasters(part);
        LOGGER.info("=====checkAllSubstitutePart.qr: " + qr.size());
        while (qr.hasMoreElements()) {
            Object object = qr.nextElement();
            LOGGER.info("=====checkAllSubstitutePart.object: " + object);
            if (object instanceof WTPartUsageLink) {
                WTPartUsageLink link = (WTPartUsageLink) object;
                WTPartMaster master = link.getUses();
                map.put(master, checkSubstitutePart((WTPartUsageLink) object));
            }
        }
        LOGGER.info("=====checkAllSubstitutePart.map: " + map);

        return map;
    }

    /**
     * 获取状态不为已归档、已发布的特定替代料
     * @param usageLink
     * @return
     * @throws WTException
     */
    private Set<WTPart> checkSubstitutePart(WTPartUsageLink usageLink) throws WTException {
        Set<WTPart> result = new HashSet<>();

        ArrayList<String> list = getSubstitutePart(usageLink);
        for (String number : list) {
            WTPart part = PartUtil.getLastestWTPartByNumber(number);
            LOGGER.info("=====checkSubstitutePart.part: " + part);
            String state = part.getState().toString();
            LOGGER.info("=====checkSubstitutePart.state: " + state);
            if (!state.equalsIgnoreCase(RELEASED) && !state.equalsIgnoreCase(ARCHIVED)) {
                result.add(part);
            }
        }

        return result;
    }

    /**
     * 获取特定替代件
     * @param link
     * @return
     * @throws WTException
     */
    private ArrayList<String> getSubstitutePart(WTPartUsageLink link) throws WTException {
        ArrayList<String> list = new ArrayList<>();

        WTCollection collection = WTPartHelper.service.getSubstituteLinks(link);
        LOGGER.info("=====getSubstitutePart.collection: " + collection.size());
        for (Object object : collection) {
            LOGGER.info("=====getSubstitutePart.object1: " + object);
            if (object instanceof ObjectReference) object = ((ObjectReference) object).getObject();

            LOGGER.info("=====getSubstitutePart.object2: " + object);
            if (object instanceof WTPartSubstituteLink) {
                WTPartSubstituteLink subLink = (WTPartSubstituteLink) object;
                WTPartMaster master = subLink.getSubstitutes();
                list.add(master.getNumber());
            }
        }
        LOGGER.info("=====getSubstitutePart.list: " + list);
        return list;
    }

    /**
     * 判定输入的是否是汉字
     * @param c 被校验的字符
     * @return true 代表是汉字
     */
    public boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    /**
     * 设置产生对象的生命周期状态
     * @param pbo
     * @param state
     * @throws WTException
     */
    public void setChangeableAfterStates(WTObject pbo, String state) throws WTException {
        if (!(pbo instanceof WTChangeActivity2)) return;

        Collection<Changeable2> afters = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
        LOGGER.info("=====setChangeableAfterStates.afters: " + afters.size());
        for (Changeable2 after : afters) {
            LOGGER.info("=====setChangeableAfterStates.after: " + after);
            if (!(after instanceof LifeCycleManaged)) continue;

            ModifyUtils.setLifeCycleState((LifeCycleManaged) after, state);
        }
    }

    /**
     * 添加有效基线数据----归档流程-(首版)
     * @param pbo
     * @param self
     * @throws WTException
     * @throws IOException
     */
    public void insertGDEffecitveBaseline(WTObject pbo, ObjectReference self) throws WTException, IOException, ParseException, DocumentException {
        Set<WTPart> result = new HashSet<>();//成品集合

        Collection<Changeable2> collection = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
        LOGGER.info("=====insertGDEffecitveBaseline.collection: " + collection);
        for (Changeable2 changeable2 : collection) {
            LOGGER.info("=====insertGDEffecitveBaseline.changeable2: " + changeable2);
            if (changeable2 instanceof WTPart) {
                WTPart part = (WTPart) changeable2;
                String number = part.getNumber();
                LOGGER.info("=====insertGDEffecitveBaseline.number: " + number);
                //判断是否成品
                if (number.startsWith("A") || number.startsWith("B")) {
                    result.add(part);
                }
            }
        }

        WfProcess process = (WfProcess) self.getObject();
        List<EffectiveBaselineBean> listBean = MversionControlHelper.buildAllEffectiveParts(process, result);
        LOGGER.info("=====insertGDEffecitveBaseline.listBean: " + listBean);
        if (!listBean.isEmpty()) {
            EffecitveBaselineUtil.insertEffectiveBaselineBean(listBean);
        }
    }

    /**
     * 角色校验、添加角色成员
     * 原GenericPartWF提交逻辑
     * @param pbo
     * @param self
     * @param processName
     * @param username
     * @param containerNames
     * @throws WTException
     */
    public void checkSoftTypeSetRole(WTObject pbo, ObjectReference self, String processName, String username, String containerNames) throws WTException {
        PartWorkflowUtil partWorkflowUtil = new PartWorkflowUtil();
        //pbo为ECA，获取受影响对象的随机一个部件作为pbo
        LOGGER.info("=====checkSoftTypeSetRole.pbo: " + pbo);
        if (pbo instanceof WTChangeActivity2) {
            Collection<Changeable2> collection = ModifyUtils.getChangeablesBefore((WTChangeActivity2) pbo);
            LOGGER.info("=====checkSoftTypeSetRole.collection: " + collection);
            for (Changeable2 changeable2 : collection) {
                LOGGER.info("=====checkSoftTypeSetRole.changeable2: " + changeable2);
                if (changeable2 instanceof WTPart) {
                    pbo = (WTPart) changeable2;
                    break;
                }
            }
        }

        if (pbo != null && pbo instanceof WTPart) {
            WTPart part = (WTPart) pbo;
            LOGGER.info("=====checkSoftTypeSetRole.part: " + part.getNumber());
            if (part.getNumber().startsWith("X")) {
                Object valueObject = PIAttributeHelper.service.getValue(part.getContainer(), "ssgs");
                String company = valueObject == null ? "" : (String) valueObject;
                LOGGER.info("=====checkSoftTypeSetRole.company: " + company);
                if ("APPO".equals(company)) {
                    String nodeHierarchy = "";// 获取分类全路径
                    // 获取分类内部值
                    String value = "";
                    value = (String) PIAttributeHelper.service.getValue(part, "Classification");
                    nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(value);
                    LOGGER.info("=====checkSoftTypeSetRole.nodeHierarchy: " + nodeHierarchy);
                    if (nodeHierarchy.contains("appo_rj01") || nodeHierarchy.contains("appo_rj02") || nodeHierarchy.contains("appo_rj03") || nodeHierarchy.contains("appo_rj04") || nodeHierarchy.contains("appo_rj06")) {
                        // 归档流程
                        if (processName != null && processName.contains("GenericPartWF")) {
                            partWorkflowUtil.CheckTeam(pbo, "manufacturing_manager", "Receiver");
                            partWorkflowUtil.CheckTeam(pbo, "hardware_development", "Receiver");
                            // 判断是否是光机产品库，且是否添加程国平，若无添加程国平00409
                            if (containerNames.contains(part.getContainerName())) {
                                if (partWorkflowUtil.CheckProcessTeamUser(pbo, username, "Receiver")) {
                                } else {
                                    // 添加
                                    partWorkflowUtil.AddTeamRole(pbo, "Receiver", username);
                                }
                            }
                        }
                        // 发布流程
                        else if (processName != null && processName.contains("APPO_ReleasedPartWF")) {
                            partWorkflowUtil.CheckTeam(pbo, "project_manager", "Receiver");
                            // 判断是否是光机产品库，且是否添加程国平，若无添加程国平000409
                            if (containerNames.contains(part.getContainerName())) {
                                if (partWorkflowUtil.CheckProcessTeamUser(pbo, username, "Receiver")) {
                                } else {
                                    // 添加
                                    partWorkflowUtil.AddTeamRole(pbo, "Receiver", username);
                                }
                            }
                        }
                    } else if (nodeHierarchy.contains("appo_rj05") || nodeHierarchy.contains("appo_rj07")) {
                        if (processName != null && processName.contains("GenericPartWF")) {
                            partWorkflowUtil.CheckTeam(pbo, "manufacturing_representative", "Receiver");
                        } else if (processName != null && processName.contains("APPO_ReleasedPartWF")) {
                            partWorkflowUtil.CheckTeam(pbo, "project_manager", "Receiver");
                        }
                    }
                }
            }
        }
    }

    /**
     * 合成错误信息
     * @return
     */
    public void compoundMessage() throws WTException {
        StringBuilder builder = new StringBuilder();
        if (MESSAGES.size() > 0) {
            builder.append("无法提交流程，存在以下问题：").append("\n");
            int i = 1;
            for (String message : MESSAGES) {
                builder.append(i++).append(". ").append(message).append("\n");
            }
        }

        if (builder.length() > 0) {
            throw new WTException(builder.toString());
        }
    }

}
