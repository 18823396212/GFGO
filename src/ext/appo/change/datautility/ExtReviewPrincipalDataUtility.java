package ext.appo.change.datautility;

import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.AbstractDataUtility;
import com.ptc.core.components.rendering.guicomponents.CheckBox;
import com.ptc.core.components.rendering.guicomponents.TextDisplayComponent;
import com.ptc.netmarkets.model.NmSimpleOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import ext.appo.change.util.ECAReviewActivityUtil;
import ext.generic.excel.bean.CellBean;
import ext.generic.excel.bean.RowBean;
import ext.generic.reviewprincipal.constant.ReviewConstant;
import ext.generic.workflow.util.WTPrincipalUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import wt.fc.Persistable;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.inf.container.WTContained;
import wt.inf.container.WTContainer;
import wt.inf.team.ContainerTeam;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.team.ContainerTeamManaged;
import wt.log4j.LogR;
import wt.org.*;
import wt.project.Role;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTMessage;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WfAssignmentState;
import wt.workflow.work.WorkItem;

import java.util.*;

/**
 * @author Administrator
 */
public class ExtReviewPrincipalDataUtility extends AbstractDataUtility {

    private static final Logger LOGGER = LogR.getLogger(ExtReviewPrincipalDataUtility.class.getName());

    @Override
    public Object getDataValue(String s, Object obj, ModelContext modelcontext) throws WTException {
        Locale locale = modelcontext.getLocale();
        LOGGER.debug("locale=" + locale);
        TextDisplayComponent textdisplaycomponent = new TextDisplayComponent("");
        if (obj instanceof WTUser) {
            WTUser user = (WTUser) obj;

            if (s.equals("extlistGroupFullName") || "genericRoleParticipantsName".equals(s) || "name".equals(s)) {
                textdisplaycomponent.setValue(user.getFullName());
                return textdisplaycomponent;
            } else if (s.equals("userBelongRoles")) {//用户所属角色
                NmCommandBean commandBean = modelcontext.getNmCommandBean();
                LOGGER.debug("pageObject=" + commandBean.getPageOid().getRefObject());
                Object object = commandBean.getPageOid().getRefObject();
                WTContainer container = null;
                if (object instanceof WorkItem) {//获取WorkItem的上下文
                    WorkItem workItem = (WorkItem) object;
                    Persistable persistable = workItem.getPrimaryBusinessObject().getObject();
                    WTContained contained = null;
                    if (persistable instanceof WTContained) {
                        contained = (WTContained) persistable;
                    }
                    container = contained.getContainer();
                }

                LOGGER.debug("pageObj.container=" + container);
                if (container != null) {
                    //根据上下文和用户，获得其角色
                    List<String> roles = null;
                    try {
                        roles = getUserRolesByContainer(user, container, locale);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    textdisplaycomponent.setValue(StringUtils.join(roles, "，"));
                    return textdisplaycomponent;
                }
            }
        } else if (obj instanceof WTGroup) {

            WTGroup group = (WTGroup) obj;
            if (s.equals("extlistGroupFullName")) {
                textdisplaycomponent.setValue(group.getName());
                return textdisplaycomponent;
            }
        } else if (obj instanceof NmSimpleOid) {
            if ("genericRoleParticipantsName".equals(s)) {
                String interName = ((NmSimpleOid) obj).getInternalName();
                String[] roleStr = interName.split(";;;");
                Role role = Role.toRole(roleStr[0]);
                //	Role role = Role.toRole(((NmSimpleOid)obj).getInternalName());
                textdisplaycomponent.setValue(role.getDisplay(locale));
                return textdisplaycomponent;

            } else if ("name".equals(s)) {
                textdisplaycomponent.setValue(((NmSimpleOid) obj).getType());
                return textdisplaycomponent;

            } else if ("isNeed".equals(s)) {
                NmSimpleOid sid = (NmSimpleOid) obj;

                CheckBox checkBox = new CheckBox();
                String id = "isNeed" + ((NmSimpleOid) obj).getInternalName();
                checkBox.setId(id);
                checkBox.setName(id);
                checkBox.setChecked(false);
                checkBox.setEditable(true);
                checkBox.setEnabled(true);

                String oid = modelcontext.getNmCommandBean().getPrimaryOid().toString();

                WorkItem workItem = (WorkItem) new ReferenceFactory().getReference(oid).getObject();

                WfActivity wfactivity = (WfActivity) workItem.getSource().getObject();

                String augmentActivities = (String) wfactivity.getContext().getValue("augmentActivities");

                String activName = sid.getType();

                LOGGER.debug("  augmentActivities: " + augmentActivities + "  activName: " + activName);

                String[] augmentActs = augmentActivities.split(ReviewConstant.OPTIONAL_ACT_SEP);

                WfAssignmentState status = workItem.getStatus();

                LOGGER.debug(">>>>>>>>>>>>status:" + status);

                for (String augmentActivity : augmentActs) {

                    LOGGER.debug("  augmentActivity: " + augmentActivity);

                    //必选活动
                    if (augmentActivity.contains(ReviewConstant.REQUIRED_ACT_SEP)) {

                        String[] activity = augmentActivity.split(ReviewConstant.REQUIRED_ACT_SEP);

                        for (String value : activity) {

                            if (value.equals(activName.trim())) {
                                checkBox.setChecked(true);
                                checkBox.setEditable(false);
                            }
                        }

                    } else {//可选择活动

                        if (augmentActivity.equals(activName)) {
                            checkBox.setChecked(true);
                        }


                        if (status.equals(WfAssignmentState.COMPLETED)) {
                            checkBox.setEditable(false);
                        }
                    }
                }

                String jsStr = "reviewCheckOnchange('" + activName + "','" + modelcontext.getNmCommandBean().getPrimaryOid() + "','" + id + "')";
                checkBox.addJsAction("onChange", jsStr);
                checkBox.addJsAction("onpropertychange", jsStr);
                return checkBox;

            } else if ("workItemInfo".equals(s)) {
                NmSimpleOid sid = (NmSimpleOid) obj;
                return sid.getType();

            } else if ("processSignInfo".equals(s)) {

                String resource = "ext.generic.reviewprincipal.resource.ReviewPrincipalRB";
                NmSimpleOid sid = (NmSimpleOid) obj;
                String activName = sid.getType();

                String oid = modelcontext.getNmCommandBean().getPrimaryOid().toString();
                WorkItem workItem = (WorkItem) new ReferenceFactory().getReference(oid).getObject();
                WfActivity wfactivity = (WfActivity) workItem.getSource().getObject();

                Persistable pbo = workItem.getPrimaryBusinessObject().getObject();

                WfProcess wfprocess = wfactivity.getParentProcess();

                String sheetName = ECAReviewActivityUtil.getSheetName((WTObject) pbo);

                RowBean rowBean = ECAReviewActivityUtil.getRowBean(null, wfprocess, (WTObject) pbo, sheetName);

                CellBean cellBean = ECAReviewActivityUtil.getCellBean((WTObject) pbo, null, wfprocess, activName);
                StringBuffer sbf = new StringBuffer("");
                if (cellBean != null) {
                    List<String> arts = cellBean.getArtList();
                    List<String> groups = cellBean.getGroupList();
                    List<String> relates = cellBean.getRelatedList();
                    List<String> roles = cellBean.getRoleList();
                    List<String> users = cellBean.getUserList();

                    if (users.size() > 0) {
                        List<WTUser> tempUsers = new ArrayList<WTUser>();
                        tempUsers = WTPrincipalUtil.getActorsByUserName(users, tempUsers);
                        sbf.append(WTMessage.getLocalizedMessage(resource, "PERSON", null, locale)).append("：[");
                        for (WTUser user : tempUsers) {
                            sbf.append(user.getFullName()).append("(").append(user.getName()).append(")  ");
                        }
                        sbf.append("]");

                    }
                    if (roles.size() > 0) {
                        sbf.append(WTMessage.getLocalizedMessage(resource, "ROLENAME", null, locale)).append("：[");
                        for (String roleInter : roles) {
                            Role role = Role.toRole(roleInter);
                            sbf.append(role.getDisplay(locale)).append("  ");
                        }
                        sbf.append("]");

                    }
                    if (groups.size() > 0) {
                        sbf.append(WTMessage.getLocalizedMessage(resource, "GROUPNAME", null, locale)).append("：").append(groups).append("  ");
                    }
                    if (arts.size() > 0) {
                        sbf.append(WTMessage.getLocalizedMessage(resource, "POSTNAME", null, locale)).append("：").append(arts).append("  ");
                    }
                    if (relates.size() > 0) {
                        sbf.append(WTMessage.getLocalizedMessage(resource, "RELATE_POSTNAME", null, locale)).append("：").append(relates).append("  ");
                    }

                    return sbf.toString();
                } else {
                    return "";
                }

            } else if ("blankInfo".equals(s)) {
                return "";
            }

        }

        return TextDisplayComponent.NBSP;
    }

    /**
     * 获得用户在上下文中所属的角色
     *
     * @param user      用户
     * @param container 上下文
     * @return
     */
    private List<String> getUserRolesByContainer(WTUser user, WTContainer container) {
        List<String> roles = new ArrayList<String>();
        try {
            Locale locale = SessionHelper.manager.getLocale();
            roles = getUserRolesByContainer(user, container, locale);
        } catch (WTException e) {
            e.printStackTrace();
        }
        LOGGER.debug("Exit .getUserRolesByContainer() roles=" + roles);
        return roles;
    }

    /**
     * 获得用户所属的角色
     *
     * @param user      用户
     * @param container 上下文
     * @param locale    时区
     * @return
     */
    private List<String> getUserRolesByContainer(WTUser user, WTContainer container, Locale locale) {
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        List<String> roles = new ArrayList<String>();
        try {
            ContainerTeam team = ContainerTeamHelper.service.getContainerTeam((ContainerTeamManaged) container);
            Vector roleVector = team.getRoles();
            //若角色下有用户(组除外)，则添加到下拉列表
            for (Object object : roleVector) {
                Role role = (Role) object;
                Enumeration principalEnum = team.getPrincipalTarget(role);
                while (principalEnum.hasMoreElements()) {
                    WTPrincipalReference reference = (WTPrincipalReference) principalEnum.nextElement();
                    WTPrincipal principal = reference.getPrincipal();
                    if (principal instanceof WTUser) {
                        if (user.equals((WTUser) principal)) {
                            roles.add(role.getDisplay(locale));
                            break;
                        }
                    } else if (principal instanceof WTGroup) {
                        boolean isTeam = OrganizationServicesHelper.manager.isMember((WTGroup) principal, user);
                        if (isTeam) {
                            roles.add(role.getDisplay(locale));
                            break;
                        }
                    }
                }
            }
        } catch (WTException e) {
            e.printStackTrace();
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
        LOGGER.debug("Exit .getUserRolesByContainer() roles=" + roles);
        return roles;
    }

}
