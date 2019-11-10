package ext.appo.change.workflow;

import com.lowagie.text.DocumentException;
import com.ptc.windchill.enterprise.team.server.TeamCCHelper;
import ext.appo.change.ModifyHelper;
import ext.appo.change.constants.ModifyConstants;
import ext.appo.change.models.CorrelationObjectLink;
import ext.appo.change.util.ModifyUtils;
import ext.appo.ecn.common.util.ChangePartQueryUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.part.beans.EffectiveBaselineBean;
import ext.appo.part.util.EffecitveBaselineUtil;
import ext.appo.part.util.MversionControlHelper;
import ext.appo.part.workflow.PartWorkflowUtil;
import ext.appo.util.PartUtil;
import ext.com.workflow.WorkflowUtil;
import ext.customer.common.MBAUtil;
import ext.generic.integration.cis.constant.CISConstant;
import ext.generic.integration.cis.rule.CISBusinessRuleXML;
import ext.generic.integration.cis.util.OracleUtil;
import ext.generic.integration.cis.util.SQLServerUtil;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.pi.PIException;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIClassificationHelper;
import ext.pi.core.PICoreHelper;
import ext.pi.core.PIPartHelper;
import org.apache.log4j.Logger;
import wt.change2.Changeable2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.doc.WTDocument;
import wt.fc.*;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTList;
import wt.inf.container.WTContainer;
import wt.inf.team.ContainerTeam;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.team.ContainerTeamManaged;
import wt.lifecycle.LifeCycleManaged;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.*;
import wt.project.Role;
import wt.session.SessionServerHelper;
import wt.team.Team;
import wt.team.WTRoleHolder2;
import wt.util.WTException;
import wt.vc.config.LatestConfigSpec;
import wt.vc.wip.Workable;
import wt.workflow.engine.WfProcess;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
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
     * 判断ECN流程中是否强制选择会签人员
     * @param pbo
     * @param self
     * @return
     * @throws WTException
     */
    public String checkPartType(WTObject pbo, ObjectReference self) throws WTException {
        List<String> isRDPart = new ArrayList<>();
        if (pbo instanceof WTChangeActivity2) {
            Collection<Changeable2> collection = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
            LOGGER.info("=====checkPartType.collection: " + collection);
            for (Changeable2 changeable2 : collection) {
                if (changeable2 instanceof WTPart) {
                    WTPart part = (WTPart) changeable2;
                    LOGGER.info("=====checkPartType.part: " + part.getNumber());
                    if (part.getNumber().startsWith("A") || part.getNumber().startsWith("C") || part.getNumber().startsWith("D") || part.getNumber().startsWith("E") || part.getNumber().startsWith("P") || part.getNumber().startsWith("T") || part.getNumber().startsWith("S") || part.getNumber().startsWith("K")) {
                        return "RD";
                    }
                    if (part.getNumber().startsWith("B")) {
                        String value = (String) PIAttributeHelper.service.getValue(part, "Classification");
                        String nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(value);
                        if (nodeHierarchy.contains("appo_bcp22")) {// 软件总成
                            isRDPart.add("NOTRD");
                        } else {
                            return "RD";
                        }
                    }
                    if (part.getNumber().startsWith("X")) {
                        isRDPart.add("NOTRD");
                    }
                    if (part.getNumber().startsWith("N") || part.getNumber().startsWith("M")) {
                        return "NM";
                    }
                }
            }
        }
        LOGGER.info("=====checkPartType.isRDPart: " + isRDPart);
        return "NOTRD";
    }

    /**
     * 检查团队角色
     * @param pbo
     * @param rolename
     * @param reviewname
     * @throws WTException
     */
    public void checkTeam(WTObject pbo, String rolename, String reviewname) throws WTException {
        boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
        Role role = Role.toRole(reviewname);
        Role selectrole = Role.toRole(rolename);
        Boolean isrole = false;

        if (pbo instanceof WTChangeOrder2) {
            WTChangeOrder2 ecn = (WTChangeOrder2) pbo;
            Team processTeam = WorkflowUtil.getTeam(ecn);
            if (processTeam != null) {
                Enumeration enumPrin = processTeam.getPrincipalTarget(role);// 会签者

                while (enumPrin.hasMoreElements()) {
                    WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
                    WTPrincipal principal = tempPrinRef.getPrincipal();
                    LOGGER.info("=====checkTeam.principal: " + principal.getName());
                    isrole = checkRole(pbo, rolename, principal);
                    LOGGER.info("=====checkTeam.isrole: " + isrole);
                    if (isrole) {
                        break;
                    }
                }
                if (!isrole && reviewname.equalsIgnoreCase("Signer")) {
                    throw new WTException("会签者/会签节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Receiver")) {
                    throw new WTException("接收者/通知节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Approver")) {
                    throw new WTException("批准者/批准节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Receiver")) {
                    throw new WTException("审核者/审核节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
            }
        } else if (pbo instanceof WTChangeActivity2) {
            WTChangeActivity2 activity2 = (WTChangeActivity2) pbo;
            Team processTeam = WorkflowUtil.getTeam(activity2);
            if (processTeam != null) {
                Enumeration enumPrin = processTeam.getPrincipalTarget(role);// 会签者

                while (enumPrin.hasMoreElements()) {
                    WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
                    WTPrincipal principal = tempPrinRef.getPrincipal();
                    LOGGER.info("=====checkTeam.principal: " + principal.getName());
                    isrole = checkRole(pbo, rolename, principal);
                    LOGGER.info("=====checkTeam.isrole: " + isrole);
                    if (isrole) {
                        break;
                    }
                }
                if (!isrole && reviewname.equalsIgnoreCase("Signer")) {
                    throw new WTException("会签者/会签节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Receiver")) {
                    throw new WTException("接收者/通知节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Approver")) {
                    throw new WTException("批准者/批准节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Receiver")) {
                    throw new WTException("审核者/审核节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
            }
        } else if (pbo instanceof WTPart) {
            WTPart part = (WTPart) pbo;
            Team processTeam = WorkflowUtil.getTeam(part);
            if (processTeam != null) {
                Enumeration enumPrin = processTeam.getPrincipalTarget(role);// 会签者
                while (enumPrin.hasMoreElements()) {
                    WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
                    WTPrincipal principal = tempPrinRef.getPrincipal();
                    LOGGER.info("=====checkTeam.principal: " + principal.getName());
                    isrole = checkRole(pbo, rolename, principal);
                    LOGGER.info("=====checkTeam.isrole: " + isrole);
                    if (isrole) {
                        break;
                    }
                }
                if (!isrole && reviewname.equalsIgnoreCase("Signer")) {
                    throw new WTException("会签者/会签节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Receiver")) {
                    throw new WTException("接收者/通知节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Approver")) {
                    throw new WTException("批准者/批准节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Receiver")) {
                    throw new WTException("审核者/审核节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
            }
        } else if (pbo instanceof WTDocument) {
            WTDocument doc = (WTDocument) pbo;
            Team processTeam = WorkflowUtil.getTeam(doc);
            if (processTeam != null) {
                Enumeration enumPrin = processTeam.getPrincipalTarget(role);// 会签者
                while (enumPrin.hasMoreElements()) {
                    WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
                    WTPrincipal principal = tempPrinRef.getPrincipal();
                    LOGGER.info("=====checkTeam.principal: " + principal.getName());
                    isrole = checkRole(pbo, rolename, principal);
                    LOGGER.info("=====checkTeam.isrole: " + isrole);
                    if (isrole) {
                        break;
                    }
                }
                if (!isrole && reviewname.equalsIgnoreCase("Signer")) {
                    throw new WTException("会签者/会签节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Receiver")) {
                    throw new WTException("接收者/通知节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Approver")) {
                    throw new WTException("批准者/批准节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
                if (!isrole && reviewname.equalsIgnoreCase("Receiver")) {
                    throw new WTException("审核者/审核节点必须选择:" + selectrole.getShortDescription() + "角色");
                }
            }
        }
    }

    /**
     * 检查指定角色是否在团队中
     * @param pbo
     * @param rolename
     * @param principal
     * @return
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    public static Boolean checkRole(WTObject pbo, String rolename, WTPrincipal principal) {
        Boolean isrole = false;

        Role role = Role.toRole(rolename);
        System.out.println("role===" + role.getShortDescription());
        WTContainer container = null;
        if (pbo instanceof WTChangeOrder2) {
            WTChangeOrder2 object = (WTChangeOrder2) pbo;
            container = object.getContainer();
        }
        if (pbo instanceof WTPart) {
            WTPart object = (WTPart) pbo;
            container = object.getContainer();
        }
        if (pbo instanceof WTDocument) {
            WTDocument object = (WTDocument) pbo;
            container = object.getContainer();
        }
        if (container != null) {
            try {
                WTRoleHolder2 wtroleholder2 = TeamCCHelper.getTeamFromObject(container);
                ContainerTeam containerTeam = ContainerTeamHelper.service.getContainerTeam((ContainerTeamManaged) container);
                Enumeration enumPrin = containerTeam.getPrincipalTarget(role);
                Set<WTUser> userSet = new HashSet<>();
                while (enumPrin.hasMoreElements()) {
                    WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
                    WTPrincipal wtPrincipal = tempPrinRef.getPrincipal();
                    System.out.println("wtprincipal====" + wtPrincipal.getName());

                    if (wtPrincipal instanceof WTUser) {
                        userSet.add((WTUser) wtPrincipal);
                    } else if (wtPrincipal instanceof wt.org.WTGroup) {
                        userSet.addAll(PartWorkflowUtil.getGroupMembers((wt.org.WTGroup) wtPrincipal));
                    }
                }

                if (userSet != null && userSet.size() > 0 && userSet.contains(principal)) {
                    isrole = true;
                }
            } catch (WTException e) {
                e.printStackTrace();
            } // 获取容器的人

        }

        return isrole;
    }

    /**
     * 根据产品类别，判断是否需要加人
     * @param pbo
     * @return
     */
    public Boolean checkContainer(WTObject pbo) {
        Boolean isok = false;
        if (pbo instanceof WTChangeOrder2) {
            WTChangeOrder2 ecn = (WTChangeOrder2) pbo;
            String productline = "";
            try {
                productline = (String) PIAttributeHelper.service.getValue(ecn, ATTRIBUTE_2);
            } catch (PIException e) {
                e.printStackTrace();
            }
            LOGGER.info("=====checkContainer.productline: " + productline);
            // 微投--40 激光电视--60
            if (productline.equalsIgnoreCase("40") || productline.equalsIgnoreCase("60")) {
                isok = true;
            }
        }
        LOGGER.info("=====checkContainer.isok: " + isok);
        return isok;
    }

    /**
     * 给流程节点角色添加固定人员
     * 删除用户为空抛出异常
     * @param pbo
     * @param rolename
     * @param principalname
     * @throws WTException
     */
    public void addTeamRole(WTObject pbo, String rolename, String principalname) throws WTException {
        Role role = Role.toRole(rolename);
        WTPrincipal wtprincipal = PartWorkflowUtil.getUserFromName(principalname);
        if (wtprincipal != null) {
            LOGGER.info("=====addTeamRole.wtprincipal: " + wtprincipal.getName());
            if (pbo instanceof WTChangeOrder2) {
                WTChangeOrder2 ecn = (WTChangeOrder2) pbo;
                Team processTeam = WorkflowUtil.getTeam(ecn);
                if (processTeam != null) {
                    processTeam.addPrincipal(role, wtprincipal);
                    LOGGER.info("=====addTeamRole.end add people success: ");
                }
            }
        }
    }

    /**
     * 检查流程中的角色是否在上下文团队中
     * @param pbo
     * @throws WTException
     */
    public void checkUserPermission(WTObject pbo) throws WTException {
        StringBuffer result = new StringBuffer();
        if (pbo instanceof WTChangeActivity2) {
            WTChangeActivity2 activity2 = (WTChangeActivity2) pbo;
            WTContainer container = activity2.getContainer();
            ContainerTeam containerTeam = ContainerTeamHelper.service.getContainerTeam((ContainerTeamManaged) container);
            Set<WTUser> userSet = PartWorkflowUtil.getAllMembers(containerTeam);
            Team processTeam = WorkflowUtil.getTeam(activity2);
            // 流程实例所有角色
            Vector processRoleVector = processTeam.getRoles();
            a:for (Object o : processRoleVector) {
                Role processRole = (Role) o;
                Enumeration enumPrin = processTeam.getPrincipalTarget(processRole);// 会签者
                while (enumPrin.hasMoreElements()) {
                    WTPrincipalReference tempPrinRef = (WTPrincipalReference) enumPrin.nextElement();
                    WTPrincipal principal = tempPrinRef.getPrincipal();
                    if (principal instanceof WTUser) {
                        WTUser user = (WTUser) principal;
                        if (userSet != null && userSet.size() > 0 && !userSet.contains(user)) {
                            result.append("用户").append(user.getFullName()).append("不在").append(container.getContainerName()).append("团队中，请另外选择人员或通知业务管理员设置用户权限！");
                            break a;
                        }
                    }
                }
            }
        }
        if (result.length() > 0) {
            throw new WTException(result.toString());
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

            PICoreHelper.service.setLifeCycleState((LifeCycleManaged) after, state);
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
        //pbo为ECA，获取产生对象的随机一个部件作为pbo
        LOGGER.info("=====checkSoftTypeSetRole.pbo: " + pbo);
        if (pbo instanceof WTChangeActivity2) {
            Collection<Changeable2> collection = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
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
     * 发布多条数据-CIS
     *
     * @param pbo
     * @param self
     * @return
     * @throws SQLException
     * @throws WTException
     */
    public String publishAllData(WTObject pbo, ObjectReference self) throws SQLException, WTException {
        //原GenericPartWF流程返回值永远为空，也就是流程不管CIS有没有写到数据库
        StringBuffer buffer = new StringBuffer();
        Connection connection = null;
        try {
            Collection<Changeable2> collection = ModifyUtils.getChangeablesAfter((WTChangeActivity2) pbo);
            LOGGER.info("=====publishAllData.collection: " + collection);
            if (!collection.isEmpty()) {
                String name = CISBusinessRuleXML.getInstance().getDataBaseName();
                LOGGER.debug("=====publishAllData.name" + name);
                if (name.equals(CISConstant.ORACLE)) connection = OracleUtil.getConnection();
                else if (name.equals(CISConstant.SQLSERVER)) connection = SQLServerUtil.getConnection();

                for (Changeable2 changeable2 : collection) {
                    LOGGER.info("=====publishAllData.changeable2: " + changeable2);
                    if (changeable2 instanceof WTPart) {
                        WTPart part = (WTPart) changeable2;
                        //if (WorkflowUtil.checkLibrary(part)) buffer.append(WorkflowUtil.publishData(part, connection));
                        if (ext.generic.integration.cis.workflow.WorkflowUtil.checkLibrary(part))
                            ext.generic.integration.cis.workflow.WorkflowUtil.publishData(part, connection);
                    } else if (changeable2 instanceof WTDocument) {
                        WTDocument document = (WTDocument) changeable2;
                        //buffer.append(WorkflowUtil.publishData(document, connection));
                        ext.generic.integration.cis.workflow.WorkflowUtil.publishData(document, connection);
                    }
                }
            }
        } finally {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        }
        return buffer.toString();
    }

    /**
     * 2.2.1 若已经产生“修订对象”，且“修订对象”的状态为“正在工作”或“重新工作”才可以删除“受影响对象”，且受影响对象需要恢复到变更前的版本，
     * 2.2.2 删除的“受影响对象”关联的ECA及ECN中的“受影响的对象”“产生的对象”列表需要同步处理。
     * 2.2.3 删除的“受影响对象”的收集图纸对象需要同步处理。
     * 2.2.4 若ECA中无受影响的对象，需要删除当前的ECA对象及流程
     * @param pbo
     * @param self
     * @throws WTException
     */
    public void rejectChangeRequest(WTObject pbo, ObjectReference self) throws WTException {
        if (pbo instanceof WTChangeActivity2) {
            WTChangeActivity2 activity2 = (WTChangeActivity2) pbo;
            //更新Link的路由为「已驳回」
            Set<CorrelationObjectLink> links = ModifyHelper.service.queryCorrelationObjectLinks(activity2, LINKTYPE_1);
            for (CorrelationObjectLink link : links) {
                ModifyHelper.service.updateCorrelationObjectLink(link, link.getAadDescription(), ROUTING_2);
            }
            //获取所有需要回退版本的对象
            Collection<Changeable2> collection = ModifyUtils.getChangeablesAfter(activity2);//获取ECA关联的产生对象
            LOGGER.info("=====rejectChangeRequest.activity2: " + activity2.getNumber() + " >>>>>collection: " + collection);
            //移除受影响对象
            ModifyUtils.removeAffectedActivityData(activity2);
            //移除产生对象
            ModifyUtils.removeChangeRecord(activity2);
            //删除ECA-如果在流程中无法删除，则在编辑点完成或暂存时删除（考虑流程进程是否要删除）
            PersistenceServerHelper.manager.remove(activity2);
            //删除修订版本
            PersistenceServerHelper.manager.remove(new WTHashSet(collection));
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
