package ext.appo.change.util;

import ext.appo.change.ModifyHelper;
import ext.appo.change.beans.TeamTemplateBean;
import ext.appo.change.models.ManageTeamTemplate;
import ext.appo.change.models.ManageTeamTemplateShow;
import ext.generic.reviewprincipal.model.PersonalTeamTemplate;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.ReferenceFactory;
import wt.fc.WTReference;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;

import java.util.ArrayList;
import java.util.List;

//管理模板工具类
public class TeamTemplateUtil {

    //通过流程oid获取模板信息，限制用户：只能查看自己创建的模板或共享的模板(管理模板)
    public static List<TeamTemplateBean> getTeamTemplateInfosLimitRole(String workItemOid) throws WTException {
        // 获取当前用户
        WTPrincipal principal = SessionHelper.manager.getPrincipal();
        WTUser user = (WTUser) principal;
        List<TeamTemplateBean> teamTemplateBeans = getTeamTemplateInfos(workItemOid, user);
        List<TeamTemplateBean> teamTemplateBeanList = new ArrayList<>();
        if (teamTemplateBeans != null && teamTemplateBeans.size() > 0) {
            for (int i = 0; i < teamTemplateBeans.size(); i++) {
                TeamTemplateBean teamTemplateBean = teamTemplateBeans.get(i);
                System.out.println("teamTemplateBean=="+teamTemplateBean.toString());
                WTUser wtUser = teamTemplateBean.getUser();
                if (wtUser.getIdentity().equals(user.getIdentity()) || teamTemplateBean.getShareTemplate().contains("true")) {
                    teamTemplateBeanList.add(teamTemplateBean);
                }
            }
        }
        return teamTemplateBeanList;
//        }
    }

    //通过流程oid获取模板信息，限制用户：只能查看自己创建的、共享的且是显示的模板（应用模板）
    public static List<TeamTemplateBean> getTeamTemplateInfosLimitShow(String workItemOid) throws WTException {
        // 获取当前用户
        WTPrincipal principal = SessionHelper.manager.getPrincipal();
        WTUser user = (WTUser) principal;
        List<TeamTemplateBean> teamTemplateBeans = getTeamTemplateInfos(workItemOid, user);
        List<TeamTemplateBean> teamTemplateBeanList = new ArrayList<>();
        if (teamTemplateBeans != null && teamTemplateBeans.size() > 0) {
            for (int i = 0; i < teamTemplateBeans.size(); i++) {
                TeamTemplateBean teamTemplateBean = teamTemplateBeans.get(i);
                WTUser wtUser = teamTemplateBean.getUser();
                if (wtUser.getIdentity().equals(user.getIdentity()) || teamTemplateBean.getShareTemplate().contains("true")) {
                    if (teamTemplateBean.getShowTemplate().contains("true")) {
                        teamTemplateBeanList.add(teamTemplateBean);
                    }
                }
            }
        }
        return teamTemplateBeanList;
    }

    //通过流程oid获取模板信息，限制：只能删除自己创建的模板（删除模板）
    public static List<TeamTemplateBean> getTeamTemplateInfosLimitDelete(String workItemOid) throws WTException {
        // 获取当前用户
        WTPrincipal principal = SessionHelper.manager.getPrincipal();
        WTUser user = (WTUser) principal;
        List<TeamTemplateBean> teamTemplateBeans = getTeamTemplateInfos(workItemOid, user);
        List<TeamTemplateBean> teamTemplateBeanList = new ArrayList<>();
        if (teamTemplateBeans != null && teamTemplateBeans.size() > 0) {
            for (int i = 0; i < teamTemplateBeans.size(); i++) {
                TeamTemplateBean teamTemplateBean = teamTemplateBeans.get(i);
                WTUser wtUser = teamTemplateBean.getUser();
                if (wtUser.getIdentity().equals(user.getIdentity())) {
                    teamTemplateBeanList.add(teamTemplateBean);
                }
            }
        }
        return teamTemplateBeanList;
    }


    //通过流程oid,当前用户 获取全部模板信息（同一流程模板）,历史模板默认不显示，不共享
    public static List<TeamTemplateBean> getTeamTemplateInfos(String workItemOid, WTUser preUser) throws WTException {
        List<TeamTemplateBean> teamTemplateBeans = new ArrayList<>();
        if (workItemOid == null) return teamTemplateBeans;
        SessionContext previous = SessionContext.newContext();
        try {
            // 当前用户设置为管理员，用于忽略权限
            SessionHelper.manager.setAdministrator();
            Persistable persistable = getObjectByOid(workItemOid);
            List<PersonalTeamTemplate> personalTeamTemplates = ModifyHelper.service.queryAllTemplates(persistable);
            if (personalTeamTemplates != null && personalTeamTemplates.size() > 0) {
                for (int i = 0; i < personalTeamTemplates.size(); i++) {
                    PersonalTeamTemplate ptemplate = personalTeamTemplates.get(i);
                    TeamTemplateBean teamTemplateBean = new TeamTemplateBean();
                    String templateName = "";
                    String showTemplate = "false";//历史数据，默认false
                    String shareTemplate = "false";//历史数据，默认false
                    String templateOid = "";
                    String creator = "";
                    String createData = "";
                    templateName = ptemplate.getPTemplateName();
                    templateOid = PersistenceHelper.getObjectIdentifier(ptemplate).toString();
                    ManageTeamTemplate manageTeamTemplate = ModifyHelper.service.queryManageTeamTemplate(templateOid);
                    if (manageTeamTemplate != null) {
                        shareTemplate = manageTeamTemplate.getShareTemplate();
                    }
                    List<ManageTeamTemplateShow> manageTeamTemplateShows = ModifyHelper.service.queryManageTeamTemplateShow(templateOid);
                    if (manageTeamTemplateShows != null && manageTeamTemplateShows.size() > 0) {
                        System.out.println("manageTeamTemplateShows.size()=="+manageTeamTemplateShows.size());
                        //ManageTeamTemplateShow显示模板表有该模板，模板没有对应用户则该用户还未对该模板进行显示操作，默认false
                        showTemplate = "false";
                        for (int j = 0; j < manageTeamTemplateShows.size(); j++) {
                            ManageTeamTemplateShow manageTeamTemplateShow = manageTeamTemplateShows.get(j);
                            WTPrincipal principal = (WTPrincipal) manageTeamTemplateShow.getSaveUser().getObject();
                            if (principal != null && principal instanceof WTUser) {
                                WTUser wtUser = (WTUser) principal;
                                System.out.println("wtUser.getIdentity()=="+wtUser.getIdentity());
                                System.out.println("preUser.getIdentity()=="+preUser.getIdentity());
                                System.out.println("比较=="+wtUser.getIdentity().equals(preUser.getIdentity()));
                                if (wtUser.getIdentity().equals(preUser.getIdentity())) {
                                    System.out.println("manageTeamTemplateShow.getShowTemplate()=="+manageTeamTemplateShow.getShowTemplate());
                                    //ManageTeamTemplateShow显示模板表对应模板存在该用户
                                    showTemplate = manageTeamTemplateShow.getShowTemplate();
                                    System.out.println("showTemplate=="+showTemplate);
                                    break;
                                }
                            }
                        }
                    }
                    creator = ptemplate.getSaveUserFullName();
                    if (ptemplate.getCreateTimestamp() != null)
                        createData = ptemplate.getCreateTimestamp().toLocaleString();
                    WTUser user = null;
                    if (ptemplate.getSaveUser() != null) {
                        WTPrincipal principal = (WTPrincipal) ptemplate.getSaveUser().getObject();
                        if (principal != null && principal instanceof WTUser) {
                            user = (WTUser) principal;
                        }
                    }
                    teamTemplateBean.setTemplateName(templateName == null ? "" : templateName);
                    teamTemplateBean.setShowTemplate(showTemplate == null ? "" : showTemplate);
                    teamTemplateBean.setShareTemplate(shareTemplate == null ? "" : shareTemplate);
                    teamTemplateBean.setTemplateOid(templateOid == null ? "" : templateOid);
                    teamTemplateBean.setCreator(creator == null ? "" : creator);
                    teamTemplateBean.setCreateData(createData == null ? "" : createData);
                    teamTemplateBean.setUser(user);
                    System.out.println("teamTemplateBean=="+teamTemplateBean.toString());
                    teamTemplateBeans.add(teamTemplateBean);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        } finally {
            SessionContext.setContext(previous);
        }
        return teamTemplateBeans;
    }


    //通过Oid获取流程
    public static Persistable getObjectByOid(String oid) throws WTException {
        Persistable p = null;

        boolean flag = SessionServerHelper.manager.setAccessEnforced(false);// 忽略权限
        try {
            ReferenceFactory referencefactory = new ReferenceFactory();
            WTReference wtreference = referencefactory.getReference(oid);
            p = wtreference.getObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SessionServerHelper.manager.setAccessEnforced(flag);// 添加权限
        }
        return p;
    }
}
