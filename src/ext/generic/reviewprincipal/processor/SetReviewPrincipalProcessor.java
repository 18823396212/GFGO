package ext.generic.reviewprincipal.processor;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.util.FeedbackMessage;
import com.ptc.core.ui.resources.FeedbackType;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.misc.NmContext;

import ext.generic.organization.helper.MgtArchitectureHelper;
import ext.generic.organization.models.MgtArchitecture;
import ext.generic.reviewprincipal.util.ReviewPrincipalUtil;
import ext.generic.workflow.util.WTPrincipalUtil;
import wt.fc.Persistable;
import wt.fc.WTObject;
import wt.fc.WTReference;
import wt.lifecycle.LifeCycleManaged;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.project.Role;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.team.Team;
import wt.util.WTException;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WorkItem;

public class SetReviewPrincipalProcessor extends DefaultObjectFormProcessor {
	private static final Logger LOGGER = LogR.getLogger(SetReviewPrincipalProcessor.class.getName());
	private static Locale locale;

	static {
		try {
			locale = SessionHelper.manager.getLocale();
		} catch (Throwable arg0) {
			throw new ExceptionInInitializerError(arg0);
		}
	}

	public FormResult doOperation(NmCommandBean nmcommandbean, List list) throws WTException {
		boolean bool = SessionServerHelper.manager.isAccessEnforced();
		FormResult formresult = new FormResult(FormProcessingStatus.SUCCESS);

		try {
			bool = SessionServerHelper.manager.setAccessEnforced(false);
			Object obj = nmcommandbean.getActionOid().getRefObject();
			if (obj instanceof WorkItem) {
				HashMap comboBox = nmcommandbean.getComboBox();
				LOGGER.debug("  >>>>>>>>>combox=" + comboBox);
				ArrayList arraylist = (ArrayList) comboBox.get("rolecomb");
				if (arraylist != null && arraylist.size() > 0) {
					String rolename = (String) arraylist.get(0);
					Role role = Role.toRole(rolename);
					WorkItem workitem = (WorkItem) obj;
					WfActivity wfactivity = (WfActivity) workitem.getSource().getObject();
					WfProcess wfprocess = wfactivity.getParentProcess();
					System.out.println("ECN=========" + wfprocess.getName());
					if (wfprocess.getName().contains("ECN")) {
						formresult = setFeedbackMessage(formresult, nmcommandbean, false,
								"会签节点NPI代表、采购代表、PMC代表、质量代表、测试代表等代表必须参加会签");
					}
					Team team = (Team) wfprocess.getTeamId().getObject();
					ArrayList oldarray = new ArrayList();
					Enumeration participants = team.getPrincipalTarget(role);

					while (participants != null && participants.hasMoreElements()) {
						WTPrincipal haschange = ((WTPrincipalReference) participants.nextElement()).getPrincipal();
						oldarray.add(haschange);
					}

					boolean arg30 = false;
					LOGGER.debug("  oldarray=" + oldarray);
					ArrayList arraylistselected = nmcommandbean.getSelected();

					for (int pbo = 0; pbo < arraylistselected.size(); ++pbo) {
						Persistable persistable = null;
						NmContext nmcontext = (NmContext) arraylistselected.get(pbo);
						NmOid oid = nmcontext.getTargetOid();
						WTReference oidwtref = oid.getWtRef();
						if (oidwtref != null) {
							persistable = oidwtref.getObject();
							if (persistable != null && persistable instanceof WTUser) {
								WTUser arg29 = (WTUser) persistable;
								if (!oldarray.contains(arg29)) {
									LOGGER.debug("  add user=" + arg29 + "   to role =" + role);
									team.addPrincipal(role, arg29);
									arg30 = true;
								}
							}
						} else {
							LOGGER.debug(">>>>>>>>>>>>...oid:" + oid);
							MgtArchitecture mgt = MgtArchitectureHelper.service
									.getMgtArchitectureByIda2a2(oid.toString());
							Object users = new ArrayList();
							if (mgt != null) {
								users = WTPrincipalUtil.getAllUsersInSublayerMgtArchitecture(mgt, (List) users);
							}

							for (int j = 0; j < ((List) users).size(); ++j) {
								WTUser evUser = (WTUser) ((List) users).get(j);
								if (!oldarray.contains(evUser)) {
									LOGGER.debug("  add user=" + evUser + "   to role =" + role);
									team.addPrincipal(role, evUser);
									arg30 = true;
								}
							}
						}
					}

					if (arg30) {
						WTObject arg31 = ReviewPrincipalUtil.getPBOByWfProcess(wfprocess);
						ReviewPrincipalUtil.updateTeam(team, (LifeCycleManaged) arg31);
					}
				}
			}
		} finally {
			SessionServerHelper.manager.setAccessEnforced(bool);
		}

		return formresult;
	}

	/**
	 * 设置回显信息
	 * 
	 * @author HYJ&NJH
	 * @param doOperation
	 * @param nmCommandBean
	 * @param flag
	 *            是否成功
	 * @param message
	 *            信息内容
	 * @return
	 * @throws WTException
	 */
	public FormResult setFeedbackMessage(FormResult doOperation, NmCommandBean nmCommandBean, boolean flag,
			String message) throws WTException {

		FeedbackType type = FeedbackType.FAILURE;
		if (flag) {
			type = FeedbackType.SUCCESS;
		}

		FeedbackMessage feedbackMessage = new FeedbackMessage(type, nmCommandBean.getLocale(), message, null,
				new String[] {});
		doOperation.addFeedbackMessage(feedbackMessage);

		return doOperation;

	}
}