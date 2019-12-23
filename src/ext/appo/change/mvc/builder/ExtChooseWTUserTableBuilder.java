package ext.appo.change.mvc.builder;

import com.ptc.jca.mvc.components.JcaComponentParams;
import com.ptc.mvc.components.*;
import com.ptc.mvc.util.ClientMessageSource;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.beans.NmHelperBean;
import ext.appo.change.util.ECAReviewActivityUtil;
import ext.generic.excel.bean.CellBean;
import ext.generic.excel.bean.RowBean;
import ext.generic.reviewprincipal.resource.ReviewPrincipalRB;
import ext.generic.reviewprincipal.util.ReviewPrincipalUtil;
import ext.generic.workflow.util.WTPrincipalUtil;
import org.apache.log4j.Logger;
import wt.fc.Persistable;
import wt.fc.WTObject;
import wt.inf.container.WTContained;
import wt.inf.container.WTContainer;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.project.Role;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WorkItem;

import java.util.*;

@ComponentBuilder({"ext.appo.change.mvc.builder.ExtChooseWTUserTableBuilder"})
public class ExtChooseWTUserTableBuilder extends AbstractComponentBuilder {

    private final ClientMessageSource messageSource = this.getMessageSource(ReviewPrincipalRB.class.getName());
    private static final Logger LOGGER = LogR.getLogger(ExtChooseWTUserTableBuilder.class.getName());

    public ComponentConfig buildComponentConfig(ComponentParams arg0) throws WTException {
        ComponentConfigFactory componentconfigfactory = this.getComponentConfigFactory();
        TableConfig tableconfig = componentconfigfactory.newTableConfig();
        tableconfig.setLabel(this.messageSource.getMessage("generic.user.title"));
        tableconfig.setSelectable(true);
        tableconfig.setShowCount(true);
        tableconfig.setId("ChooseWTUserTable");
        tableconfig.addComponent(this.newColumnConfig("type_icon", true));
        ColumnConfig columnconfig = componentconfigfactory.newColumnConfig("fullName", "名称", true);
        columnconfig.setWidth(200);
        tableconfig.addComponent(columnconfig);
        columnconfig = componentconfigfactory.newColumnConfig("name", true);
        columnconfig.setWidth(200);
        tableconfig.addComponent(columnconfig);
        columnconfig = componentconfigfactory.newColumnConfig("eMail", "邮箱", true);
        columnconfig.setWidth(200);
        tableconfig.addComponent(columnconfig);
        columnconfig = componentconfigfactory.newColumnConfig("userBelongRoles", true);
        columnconfig.setLabel(this.messageSource.getMessage("USERBELONGROLES"));
        columnconfig.setDataUtilityId("genericRoleParticipantsName");
        tableconfig.addComponent(columnconfig);
        return tableconfig;
    }

    public Object buildComponentData(ComponentConfig arg0, ComponentParams arg1) throws Exception {
        boolean bool = SessionServerHelper.manager.isAccessEnforced();

        List<WTPrincipal> userList = new ArrayList();
        try {
            SessionServerHelper.manager.setAccessEnforced(false);

            NmHelperBean localNmHelperBean = ((JcaComponentParams) arg1).getHelperBean();
            NmCommandBean commandBean = localNmHelperBean.getNmCommandBean();
            HashMap map = null;
            if (commandBean != null) {
                map = commandBean.getComboBox();
            }

            ArrayList roleInters = null;
            if (map != null) {
                roleInters = (ArrayList) map.get("rolecomb");
            }

            String roleInter = "";
            if (roleInters != null && roleInters.size() > 0) {
                roleInter = (String) roleInters.get(0);
            }

            NmOid contextNmOid = null;
            if (commandBean != null) {
                contextNmOid = commandBean.getElementContext().getPrimaryOid();
            }

            if (contextNmOid != null) {
                WTContainer container = contextNmOid.getContainerObject();
                WorkItem workitem = (WorkItem) contextNmOid.getRefObject();
                WfActivity wfactivity = (WfActivity) workitem.getSource().getObject();
                Object isAllActivties = wfactivity.getContext().getValue("isAllActivties");
                Persistable pbo = workitem.getPrimaryBusinessObject().getObject();
                WfProcess wfprocess = wfactivity.getParentProcess();
                Hashtable<String, Role> acttable;
                if (isAllActivties != null) {
                    acttable = ReviewPrincipalUtil.getAllActivities(wfactivity, wfprocess);
                } else {
                    acttable = ReviewPrincipalUtil.getNextActivity(workitem, wfactivity, pbo, wfprocess);
                }

                ReviewPrincipalUtil.getUserByRole(workitem, wfactivity, wfprocess, acttable);
                Iterator iter = acttable.keySet().iterator();

                while (iter != null && iter.hasNext()) {
                    String activityName = (String) iter.next();
                    Role role = acttable.get(activityName);
                    String interRole = role.toString();
                    if (interRole.equals(roleInter)) {
                        getUserByCellBean(activityName, wfactivity, wfprocess, pbo, userList);
                        break;
                    }
                }
            } else {
                LOGGER.debug(">>>>>>>>>>>>>>>>primaryOid is null!");
            }

            LOGGER.debug(">>>>>>>>>>>>>>>>>userList size: " + userList.size());
        } finally {
            SessionServerHelper.manager.setAccessEnforced(bool);
        }

        return userList;
    }

    protected ColumnConfig newColumnConfig(String paramString, boolean paramBoolean) {
        return this.getComponentConfigFactory().newColumnConfig(paramString, paramBoolean);
    }

    public static void getUserByCellBean(String activityName, WfActivity wfactivity, WfProcess wfprocess, Persistable pbo, List userList) throws WTException {
        String sheetName = ECAReviewActivityUtil.getSheetName((WTObject) pbo);
        RowBean rowBean = ECAReviewActivityUtil.getRowBean(null, wfprocess, (WTObject) pbo, sheetName);
        CellBean cellBean = ECAReviewActivityUtil.getCellBean((WTObject) pbo, null, wfprocess, activityName);
        String augmentActivities = (String) wfactivity.getContext().getValue("augmentActivities");
        WTPrincipalUtil.getUsersInNoMgts(userList, cellBean, ((WTContained) pbo).getContainer(), wfprocess, rowBean, augmentActivities);
    }

}