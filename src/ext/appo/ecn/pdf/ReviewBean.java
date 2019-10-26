package ext.appo.ecn.pdf;

import java.text.SimpleDateFormat;
import java.util.Vector;

import com.ptc.windchill.enterprise.workflow.WfDataUtilitiesHelper;

import ext.customer.common.MBAUtil;
import wt.org.WTUser;
import wt.util.WTException;
import wt.util.WTRuntimeException;
import wt.workflow.engine.ProcessData;
import wt.workflow.engine.WfVariable;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;

public class ReviewBean {

	private WorkItem wi;

	public ReviewBean(WorkItem wi) {
		this.wi = wi;
	}

	public String getName() {
		WfAssignedActivity activity = (WfAssignedActivity) wi.getSource().getObject();
		return activity.getName();
	}

	public String getCompleteName() throws WTRuntimeException, WTException {
		String userCh = "";
		String completedBy = wi.getCompletedBy();
		if (completedBy != null && completedBy != "") {
			WTUser user = null;
			if (WfDataUtilitiesHelper.getUserPrinRef(completedBy) != null) {
				user = (WTUser) WfDataUtilitiesHelper.getUserPrinRef(completedBy).getObject();
				userCh = user.getFullName();
			} else {
				System.out.println(completedBy + " not find user");
			}
		}
		if (userCh != null) {
			String count = userCh.replaceAll("\\d+", "");
			String[] arry = count.split("\\|");
			return arry[0];
		} else {
			return completedBy;
		}
	}

	public String getCompleteTime() {
		SimpleDateFormat sdf = new SimpleDateFormat(PdfUtil.SIMPLE_DATE_FORMAT);
		return sdf.format(wi.getModifyTimestamp());
	}

	public String getComment() {
		String comment = (String) MBAUtil.getValue(wi, "workitem_comment");
		return comment;
	}

	public String getReviewOpinion() {
		String reviewReResult = "";
		ProcessData processData = wi.getContext();
		if (processData != null) {
			WfVariable wfVariable = processData.getVariable("WfUserEventList");
			if (wfVariable != null) {
				Vector<Object> variable = (Vector<Object>) wfVariable.getValue();
				if (!variable.isEmpty()) {
					variable.get(0);
					reviewReResult = variable.get(0).toString();
				}
			}
		}
		return reviewReResult;
	}

	public String getExamineUser() throws WTException {
		String user = "";
		WTUser owner = (WTUser) wi.getOwnership().getOwner().getPrincipal();
		if (owner != null) {
			user = owner.getFullName();
		}

		String count = user.replaceAll("\\d+", "");
		String[] arry = count.split("\\|");

		return arry[0];
	}

}
