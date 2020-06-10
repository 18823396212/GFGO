package ext.appo.change.processor;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.util.FeedbackMessage;
import com.ptc.core.ui.resources.FeedbackType;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import ext.appo.change.ModifyHelper;
import ext.appo.change.models.ManageTeamTemplate;
import ext.appo.change.models.ManageTeamTemplateShow;
import ext.com.core.CoreUtil;
import ext.generic.reviewprincipal.model.PersonalTeamTemplate;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import wt.fc.ObjectReference;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;

import java.util.HashMap;
import java.util.List;

public class ExtManageTeamTemplateProcessor extends DefaultObjectFormProcessor {
    private static final Logger logger = LogR.getLogger(ExtManageTeamTemplateProcessor.class.getName());
    private static final String RESOURCE = "ext.generic.reviewprincipal.resource.WFTeamTemplateRB";

    public FormResult doOperation(NmCommandBean commandBean, List<ObjectBean> objectBeans) throws WTException {
        FormResult formResult = new FormResult();
        boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        WTPrincipal wtPrincipal = SessionHelper.manager.getPrincipal();//当前用户
        WTUser wtUser = (WTUser) wtPrincipal;
        String wtUserName = wtUser.getName();
        String wtUserFullName = wtUser.getFullName();
        HashMap<String, Object> parameterMap = commandBean.getParameterMap();
        String[] saveData = (String[]) parameterMap.get("saveData");
        try {
            if (saveData != null && saveData.length > 0) {
                String datasJSON = saveData[0];
                JSONArray jsonArray = new JSONArray(datasJSON);
                jsonArray:
                for (int i = 0; i < jsonArray.length(); i++) {
                    String data = jsonArray.getString(i);
                    String[] dataStr = data.split(";");
                    if (dataStr != null && dataStr.length >= 5) {
                        String templateOid = dataStr[0];//模板oid
                        String before = dataStr[1];//修改前值
                        String type = dataStr[2];//类型：show/share
                        String after = dataStr[4];//修改后值
                        if (before.equals(after) || templateOid.isEmpty() || type.isEmpty()) continue;
                        ManageTeamTemplate manageTeamTemplate = ModifyHelper.service.queryManageTeamTemplate(templateOid);
                        if (manageTeamTemplate != null) {
                            String showTemplate = manageTeamTemplate.getShowTemplate();
                            String shareTemplate = manageTeamTemplate.getShareTemplate();
                            String templateName = manageTeamTemplate.getTemplateName();
                            if (type.contains("show")) {
                                showTemplate = after;
                            } else if (type.contains("share")) {
                                shareTemplate = after;
                            }
                            ModifyHelper.service.updateManageTeamTemplate(manageTeamTemplate, showTemplate, shareTemplate);
                            List<ManageTeamTemplateShow> manageTeamTemplateShows = ModifyHelper.service.queryManageTeamTemplateShow(templateOid);
                            if (manageTeamTemplateShows != null && manageTeamTemplateShows.size() > 0) {
                                for (int j = 0; j < manageTeamTemplateShows.size(); j++) {
                                    ManageTeamTemplateShow manageTeamTemplateShow = manageTeamTemplateShows.get(j);
                                    WTPrincipal principal = (WTPrincipal) manageTeamTemplateShow.getSaveUser().getObject();
                                    if (principal != null && principal instanceof WTUser) {
                                        WTUser user = (WTUser) principal;
                                        if (user.getIdentity().equals(wtUser.getIdentity())) {
                                            //该模板显示用户存在更新
                                            ModifyHelper.service.updateManageTeamTemplateShow(manageTeamTemplateShow, showTemplate);
                                            continue jsonArray;
                                        }
                                    }
                                }
                            }
                            //该模板显示用户不存在新建
                            ModifyHelper.service.newManageTeamTemplateShow(templateName, showTemplate, templateOid, ObjectReference.newObjectReference(wtUser), wtUserName, wtUserFullName);
                        } else {
                            //不存在该ManageTeamTemplate，创建
                            PersonalTeamTemplate ptemplate = (PersonalTeamTemplate) CoreUtil.getWTObjectByOid(templateOid);
                            WTUser user = null;
                            String templateName = ptemplate.getPTemplateName();
                            //不在新建模板管理表中，不显示、不共享
                            String showTemplate = "false";
                            String shareTemplate = "false";
                            if (type.contains("show")) {
                                showTemplate = after;
                            } else if (type.contains("share")) {
                                shareTemplate = after;
                            }
                            String userName = ptemplate.getSaveUserName();
                            String userFullName = ptemplate.getSaveUserFullName();
                            if (ptemplate.getSaveUser() != null) {
                                WTPrincipal principal = (WTPrincipal) ptemplate.getSaveUser().getObject();
                                if (principal != null && principal instanceof WTUser) {
                                    user = (WTUser) principal;
                                }
                            }
                            ModifyHelper.service.newManageTeamTemplate(templateName, showTemplate, shareTemplate, templateOid, ObjectReference.newObjectReference(user), userName, userFullName);
                            List<ManageTeamTemplateShow> manageTeamTemplateShows = ModifyHelper.service.queryManageTeamTemplateShow(templateOid);
                            if (manageTeamTemplateShows != null && manageTeamTemplateShows.size() > 0) {
                                for (int j = 0; j < manageTeamTemplateShows.size(); j++) {
                                    ManageTeamTemplateShow manageTeamTemplateShow = manageTeamTemplateShows.get(j);
                                    WTPrincipal principal = (WTPrincipal) manageTeamTemplateShow.getSaveUser().getObject();
                                    if (principal != null && principal instanceof WTUser) {
                                        WTUser user1 = (WTUser) principal;
                                        if (user1.getIdentity().equals(wtUser.getIdentity())) {
                                            //该模板显示用户存在更新
                                            ModifyHelper.service.updateManageTeamTemplateShow(manageTeamTemplateShow, showTemplate);
                                            continue jsonArray;
                                        }
                                    }
                                }
                            }
                            //该模板显示用户不存在新建
                            ModifyHelper.service.newManageTeamTemplateShow(templateName, showTemplate, templateOid, ObjectReference.newObjectReference(wtUser), wtUserName, wtUserFullName);
                        }

                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
        formResult.addFeedbackMessage(new FeedbackMessage(
                FeedbackType.SUCCESS, null, "修改成功", null, new String[]{}));
        return formResult;
    }
}