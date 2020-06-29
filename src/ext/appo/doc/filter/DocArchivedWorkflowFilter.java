package ext.appo.doc.filter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.workflow.NmWorkflowHelper;

import ext.appo.change.report.ChangeHistoryReport;
import ext.appo.change.util.ModifyUtils;
import ext.appo.ecn.common.util.ChangeUtils;
import ext.com.workflow.WorkflowUtil;
import ext.generic.doc.config.DocValidatorRuleUtil;
import wt.change2.ChangeHelper2;
import wt.change2.Changeable2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.WTReference;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfState;

public class DocArchivedWorkflowFilter extends DefaultSimpleValidationFilter {
	public UIValidationStatus preValidateAction(UIValidationKey uivalidationkey,
			UIValidationCriteria uivalidationcriteria) {
		UIValidationStatus uivalidationstatus = UIValidationStatus.DISABLED;

		if ((uivalidationcriteria != null) && (uivalidationcriteria.getContextObject() != null)) {
			WTReference wtreference = uivalidationcriteria.getContextObject();
			Persistable persistable = wtreference.getObject();
			try {
				if (persistable instanceof WTDocument) {
					WTDocument doc = (WTDocument) persistable;

					WTUser currentUser = (WTUser) SessionHelper.manager.getPrincipal();

					String currentState = doc.getLifeCycleState().toString();
					boolean stateOk = false;
					if (currentState.equals("INWORK")) {
						stateOk = true;
					}

					// boolean typeOk = false;
					// TypeIdentifier typeIdentifier = TypeIdentifierUtility.getTypeIdentifier(doc);
					// String typename = typeIdentifier.getTypename();
					// if (typename.contains("com.ptc.ReferenceDocument")) {
					// typeOk = true;
					// }

					DocValidatorRuleUtil docUtil = DocValidatorRuleUtil.getInstance();
					boolean isNeedCreator = docUtil.isNeedCreator();

					boolean userOk = false;
					if (isNeedCreator) {
						WTUser creator = (WTUser) doc.getModifier().getPrincipal();
						if (PersistenceHelper.isEquivalent(currentUser, creator))
							userOk = true;
					} else {
						userOk = true;
					}

					boolean processok = true;

					NmOid nmOid = NmOid.newNmOid(PersistenceHelper.getObjectIdentifier(doc));
					QueryResult qr = NmWorkflowHelper.service.getRoutingHistoryData(nmOid);
					// 获取所有开启的流程模板名称
					Set<String> set = new HashSet<>();
					while (qr.hasMoreElements()) {
						WfProcess process = (WfProcess) qr.nextElement();
						String templateName = process.getTemplate().getName();
						if (process.getState()
								.equals(WfState.OPEN_RUNNING) /* && templateName.contains("GenericPartWF") */ ) {
							set.add(templateName);
						}
					}
					// 存在开启的流程
					if (set.size() > 0) {
						// 需过滤eca
						if (true) {
							// 移除对应流程名称,PS:GenericECAWF
							set.remove("GenericECNWF");
							set.remove("GenericECAWF");
							if (set.size() > 0) {
								processok = true;
							}
						}
					}

					// if ((stateOk) && (userOk) && (typeOk) && (processok) && (isLatestObject(doc))
					// && (!WorkflowUtil.isObjectCheckedOut(doc)))
					//add by lzy at 20200611 start
//					if ((stateOk) && (userOk) && (processok) && (isLatestObject(doc))
//							&& (!WorkflowUtil.isObjectCheckedOut(doc)))
					if ((stateOk) && (userOk) && (processok) && (isLatestObject(doc))
							&& (!WorkflowUtil.isObjectCheckedOut(doc)) && !isRunningNewEcnWorkflowAndAfter(doc))
					//add by lzy at 20200611 end
						uivalidationstatus = UIValidationStatus.ENABLED;
				}
			} catch (WTException e) {
				e.printStackTrace();
			}

		}

		return uivalidationstatus;
	}

	private static boolean isLatestObject(WTObject oldObj) {
		boolean isLstest = true;
		RevisionControlled iterated = null;
		if (oldObj instanceof RevisionControlled) {
			iterated = (RevisionControlled) oldObj;
			if (!iterated.isLatestIteration()) {
				isLstest = false;
			}
		}
		return isLstest;
	}

	/**
	 * add by lzy
	 * 判断pbo是否在新ECN流程APPO_ECNWF产生对象中且流程未结束
	 *
	 * @param document
	 * @return
	 * @throws WTException
	 */
	private static Boolean isRunningNewEcnWorkflowAndAfter(WTDocument document) throws WTException {
		if (document == null) return false;
		String number = document.getNumber();
		String mVersion = document.getVersionIdentifier().getValue();//文档大版本
		WTDocument wtDocument = ext.generic.integration.cis.workflow.WorkflowUtil.getDocumentPreVersionLatestIter(document);//获取取前一个版本
		WTChangeOrder2 changeOrder2 = null;
		//获取对象所有关联的ECA对象
		QueryResult result = ChangeHelper2.service.getAffectingChangeActivities(wtDocument);
		while (result.hasMoreElements()) {
			WTChangeActivity2 changeActivity2 = (WTChangeActivity2) result.nextElement();
			//获取产生对象
			Collection<Changeable2> collection = ModifyUtils.getChangeablesAfter(changeActivity2);
			for (Changeable2 changeable2 : collection) {
				if (changeable2 instanceof WTDocument) {
					WTDocument doc = (WTDocument) changeable2;
					String docNumber = doc.getNumber();
					String docVersion = doc.getVersionIdentifier().getValue();//文档大版本
					if (number.equals(docNumber) && mVersion.equals(docVersion)) {
						changeOrder2 = ChangeUtils.getEcnByEca(changeActivity2);
					}
				}
			}
		}
		if (changeOrder2!=null){
			//ECN进程
			QueryResult qr = WfEngineHelper.service.getAssociatedProcesses(changeOrder2, null, null);
			while (qr.hasMoreElements()) {
				WfProcess process = (WfProcess) qr.nextElement();
				String templateName = process.getTemplate().getName();
				String state = String.valueOf(changeOrder2.getLifeCycleState());
				//不是已取消、已解决的新ECN流程APPO_ECNWF
				if (templateName.equals("APPO_ECNWF")) {
					if (!state.equals("CANCELLED") && !state.equals("RESOLVED")) {
						return true;
					}
				}
			}
		}
		return false;
	}
}