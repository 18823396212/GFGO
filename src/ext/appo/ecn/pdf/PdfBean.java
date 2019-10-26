package ext.appo.ecn.pdf;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ext.pi.PIException;
import ext.pi.core.PIAttributeHelper;
import wt.change2.WTChangeOrder2;
import wt.fc.QueryResult;
import wt.org.WTUser;
import wt.project.Role;
import wt.util.WTException;
import wt.workflow.WfException;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WorkItem;
import wt.workflow.work.WorkflowHelper;

public class PdfBean {

	private WTChangeOrder2 ecn;

	// 受影响列表
	private List<AffectedItemBean> aibs = new ArrayList<>();
	// 受影响产品
	private List<AffectedProductBean> apbs = new ArrayList<>();
	// 事务性任务
	private List<Map<String, String>> swxrw = new ArrayList<>();
	// 流程节点
	private List<ReviewBean> rbs = new ArrayList<>();

	public PdfBean(WTChangeOrder2 ecn) {
		this.ecn = ecn;
	}

	public String getPdfTitle() {
		return "工程更改通知单（ECN）";
	}

	public String getECName() {
		return ecn.getName();
	}

	public String getECNumber() {
		return ecn.getNumber();
	}

	// 发出日期
	public String getSendDate() {
		SimpleDateFormat sdf = new SimpleDateFormat(PdfUtil.SIMPLE_DATE_FORMAT);
		return sdf.format(ecn.getCreateTimestamp());
	}

	// ECN发起人
	public String getSendPersion() {
		String sendPersion = ecn.getCreatorFullName();
		String count = sendPersion.replaceAll("\\d+", "");
		String[] arry = count.split("\\|");
		return arry[0];
	}

	// 变更原因
	public String getChangeComment() {
		Object object = PdfUtil.getIBAObjectValue(ecn, "ChangeCause");
		System.out.println("object====" + object);
		String comment = "";
		if (object instanceof String) {
			String changeComment = (String) PdfUtil.getIBAObjectValue(ecn, "ChangeCause");
			comment = changeComment;
		}
		if (object instanceof Object[]) {
			Object[] objArr = (Object[]) object;
			for (int i = 0; i < objArr.length; i++) {
				comment = comment + objArr[i].toString() + "  ";
			}
		}
		return comment;
	}

	// 获取变更属性
	public String getChangeAtt(String para) {

		String comment = "";
		if ("sscpx".equals(para)) {
			try {
				comment = PIAttributeHelper.service.getDisplayValue(ecn, "sscpx", Locale.CHINA);
				return comment;
			} catch (PIException e) {
				e.printStackTrace();
			}
		}

		Object object = PdfUtil.getIBAObjectValue(ecn, para);
		System.out.println("object====" + object);

		if (object instanceof String) {
			String changeComment = (String) object;
			comment = changeComment;
		}
		if (object instanceof Object[]) {
			Object[] objArr = (Object[]) object;
			for (int i = 0; i < objArr.length; i++) {
				comment = comment + objArr[i].toString() + "  ";
			}
		}

		return comment;
	}
	//

	// PIAttributeHelper.service.getDisplayValue(doc, "sscpx", Locale.CHINA)

	// 变更原因说明
	public String getComment() {
		String comment = ecn.getDescription();

		return comment == null ? "" : comment;
	}

	// 会签人
	public String getHQs() throws WfException, WTException {

		Set<String> set = new HashSet<>();
		QueryResult qr = WorkflowHelper.service.getWorkItems(ecn);
		while (qr.hasMoreElements()) {
			WorkItem wi = (WorkItem) qr.nextElement();
			WfProcess process = PdfUtil.getProcess(wi);
			// wi.getc
			Role role = Role.toRole("Signer");
			List<WTUser> users = PdfUtil.getUsers(process, role);
			if (!users.isEmpty()) {
				for (WTUser user : users) {
					String hquser = user.getFullName();
					String count = hquser.replaceAll("\\d+", "");
					String[] arry = count.split("\\|");
					set.add(arry[0]);
				}
			}

		}
		return StringUtils.strip(set.toString(), "[]");
	}

	public List<AffectedItemBean> getAibs() {
		return aibs;
	}

	public void setAibs(List<AffectedItemBean> aibs) {
		this.aibs = aibs;
	}

	public List<AffectedProductBean> getApbs() {
		return apbs;
	}

	public void setApbs(List<AffectedProductBean> apbs) {
		this.apbs = apbs;
	}

	public List<Map<String, String>> getSwxrw() {
		return swxrw;
	}

	public void setSwxrw(List<Map<String, String>> swxrw) {
		this.swxrw = swxrw;
	}

	public List<ReviewBean> getRbs() {
		return rbs;
	}

	public void setRbs(List<ReviewBean> rbs) {
		this.rbs = rbs;
	}
}
