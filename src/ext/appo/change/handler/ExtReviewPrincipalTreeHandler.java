package ext.appo.change.handler;

import com.ptc.core.components.beans.TreeHandlerAdapter;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.model.NmSimpleOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import ext.appo.change.util.ECAReviewActivityUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import wt.fc.Persistable;
import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.project.Role;
import wt.session.SessionServerHelper;
import wt.team.Team;
import wt.util.WTException;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WorkItem;

import java.util.*;

public class ExtReviewPrincipalTreeHandler extends TreeHandlerAdapter {

    private static final Logger LOGGER = LogR.getLogger(ExtReviewPrincipalTreeHandler.class.getName());

    public List getRootNodes() throws WTException {
        boolean bool = SessionServerHelper.manager.isAccessEnforced();

        ArrayList arraylist = new ArrayList();
        try {
            SessionServerHelper.manager.setAccessEnforced(false);
            NmCommandBean nmcommandbean = this.getModelContext().getNmCommandBean();
            if (nmcommandbean == null) {
                LOGGER.debug("  nmcommandbean is Null !");
                return arraylist;
            }

            NmOid nmoid = nmcommandbean.getPrimaryOid();
            WorkItem workitem = (WorkItem) nmoid.getRefObject();
            Persistable pbo = workitem.getPrimaryBusinessObject().getObject();
            WfActivity wfactivity = (WfActivity) workitem.getSource().getObject();
            WfProcess process = wfactivity.getParentProcess();
            String augmentActivities = (String) wfactivity.getContext().getValue("augmentActivities");
            String setactive = "";
            Object isAllActivties = wfactivity.getContext().getValue("isAllActivties");
            if (isAllActivties != null) {
                Hashtable<String, String> table = ECAReviewActivityUtil.getAugumentTable((WTObject) pbo, null, process);
                String augmentRoles = table.get("augmentRoles");
                if ("YES".equals(isAllActivties)) {
                    setactive = augmentRoles;
                } else {
                    List<String> excelActs = new ArrayList();
                    if (augmentRoles != null && augmentRoles.trim().length() > 0) {
                        excelActs.addAll(Arrays.asList(augmentRoles.split(";;;qqq")));
                    }

                    LOGGER.trace("签陪表：" + excelActs);
                    List<String> varActs = new ArrayList();
                    varActs.addAll(Arrays.asList(((String) isAllActivties).split(";;;qqq")));
                    LOGGER.trace("isAllActivties变量：" + varActs);
                    excelActs.retainAll(varActs);
                    LOGGER.trace("签陪表与isAllActivties交集：" + excelActs);
                    setactive = StringUtils.join(excelActs, ";;;qqq");
                }
            } else {
                setactive = ECAReviewActivityUtil.getNextActivityToStr(null, process, (WTObject) pbo, augmentActivities, wfactivity.getName());
            }

            LOGGER.debug(">>>>>>>>>>>.  augmentRoles: " + setactive);
            if (setactive.trim().length() > 0) {
                String[] segs = setactive.split(";;;qqq");

                for (String eachRecord : segs) {
                    if (eachRecord != null && eachRecord.trim().length() != 0) {
                        Role role = ECAReviewActivityUtil.getRoleByActivity(eachRecord, process);
                        LOGGER.debug(">>>>>>>>>>>.  activtiyName: " + eachRecord + " role: " + role);
                        if (role != null) {
                            NmSimpleOid simpleoid = new NmSimpleOid();
                            simpleoid.setType(eachRecord);
                            simpleoid.setInternalName(role.toString() + ";;;" + eachRecord);
                            arraylist.add(simpleoid);
                        } else {
                            LOGGER.debug(">>>>>>>>>>>错误配置：  活动模板: " + eachRecord + " 未配置角色!");
                        }
                    }
                }
            }

        } finally {
            SessionServerHelper.manager.setAccessEnforced(bool);
        }

        return arraylist;
    }

    public Map getNodes(List list) throws WTException {
        boolean bool = SessionServerHelper.manager.isAccessEnforced();

        HashMap hashmap = new HashMap();
        try {
            SessionServerHelper.manager.setAccessEnforced(false);
            NmOid nmoid = this.getModelContext().getNmCommandBean().getPrimaryOid();
            WorkItem workitem = (WorkItem) nmoid.getRefObject();
            if (workitem == null) {
                return hashmap;
            } else {
                WfActivity wfactivity = (WfActivity) workitem.getSource().getObject();
                WfProcess process = wfactivity.getParentProcess();
                Team team = (Team) process.getTeamId().getObject();

                for (Object object : list) {
                    ArrayList arraylist1 = new ArrayList();
                    if (object instanceof NmSimpleOid) {
                        String interName = ((NmSimpleOid) object).getInternalName();
                        String[] roleStr = interName.split(";;;");
                        Role role = Role.toRole(roleStr[0]);
                        LOGGER.debug(">>>>>>>>>>>.  role: " + role.getDisplay());
                        Enumeration participants = team.getPrincipalTarget(role);

                        while (participants != null && participants.hasMoreElements()) {
                            WTPrincipal principal = ((WTPrincipalReference) participants.nextElement()).getPrincipal();
                            arraylist1.add(principal);
                        }

                        hashmap.put(object, arraylist1);
                    }
                }

                return hashmap;
            }
        } finally {
            SessionServerHelper.manager.setAccessEnforced(bool);
        }
    }
}
