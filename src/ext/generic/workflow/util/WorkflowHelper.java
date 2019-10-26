package ext.generic.workflow.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import wt.enterprise.RevisionControlled;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTSet;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.part.WTPart;
import wt.query.ClassAttribute;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.SubSelectExpression;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.vc.Mastered;
import wt.vc.views.View;
import wt.workflow.engine.WfProcess;
import ext.generic.reviewObject.model.ProcessReviewObjectLink;
import ext.lang.PIStringUtils;
import ext.pi.core.PIPartHelper;
import ext.pi.core.PIWorkflowHelper;

/**
 * 提供工作流中可以调用的一些方法
 * @author Administrator
 *
 */
public class WorkflowHelper implements Serializable, RemoteAccess {

	private static final long serialVersionUID = 1L;
	private static final String CLASSNAME = WorkflowHelper.class.getName();
	private static final Logger logger = LogR.getLogger(CLASSNAME);
	
	/**
	 * 判断pbo是否在其他流程中运行
	 * @param pbo 
	 * @return
	 * @throws WTException
	 */
	public static boolean isRunningWorkflow(WTObject pbo) throws WTException{
		boolean processok = false;
		if(PIWorkflowHelper.service.hasRunningProcesses(pbo) 
				|| WorkflowHelper.hasRunningProcesses((WTObject) pbo)){
			logger.warn("Has Running Workflow");
			processok = true;
		}
		return processok;
	}
	
	/**
	 * 判断对象是否有正在运行的流程
	 * @param wtobj pbo和随签对象都会和流程关联，建立ProcessReviewObjectLink
	 * @return
	 */
	public static boolean hasRunningProcesses(WTObject wtobj ) {
		boolean has = false;
		if(wtobj != null && (wtobj instanceof RevisionControlled)){
			RevisionControlled rc = (RevisionControlled) wtobj;
			Mastered master = rc.getMaster();
			
			try {
				QuerySpec qs = new QuerySpec(WfProcess.class);
				
				SearchCondition sc = new SearchCondition(WfProcess.class, WfProcess.STATE,
						SearchCondition.EQUAL, "OPEN_RUNNING");
				qs.appendWhere(sc, new int[]{0});
				
				QuerySpec subQs = new QuerySpec();
				ClassAttribute ca = new ClassAttribute(ProcessReviewObjectLink.class, "roleAObjectRef.key.id");
				int index = subQs.appendClassList(ProcessReviewObjectLink.class, false);
				subQs.appendSelect(ca, new int[]{index}, false);
				
				SearchCondition subSc = new SearchCondition(ProcessReviewObjectLink.class,"currentIteratedReference.key.id",
						SearchCondition.EQUAL, PersistenceHelper.getObjectIdentifier(master).getId());
				subQs.appendWhere(subSc, new int[]{index});
				
				SubSelectExpression sse = new SubSelectExpression(subQs);
				logger.debug("--subQs="+subQs);
				
				qs.appendAnd();
				ClassAttribute caId = new ClassAttribute(WfProcess.class, "thePersistInfo.theObjectIdentifier.id");
				qs.appendWhere(new SearchCondition(caId, SearchCondition.IN, sse), new int[]{0});
				qs.setAdvancedQueryEnabled(true);
				logger.debug("--hasRunningProcesses.sql="+qs);
				QueryResult rs = PersistenceHelper.manager.find(qs);
				if(rs != null && rs.size()>0){
					has = true;
				}
			} catch (QueryException e) {
				e.printStackTrace();
			} catch (WTException e) {
				e.printStackTrace();
			}
			
		}
		return has;
	}
	
	/**
	 * 检查不在随签中的子件状态是否符合给定状态
	 * @param pbo pbo
	 * @param self 流程
	 * @param states 多个状态以英文逗号分隔
	 * @throws WTException 
	 */
	public static void checkSubPartStates(WTObject pbo, ObjectReference self, String states) throws WTException{
		if(!PIStringUtils.hasText(states) || !(pbo instanceof WTPart)){
			return;
		}
		StringBuilder error = new StringBuilder("");
		String[] stateAry = states.split(",");
		List<String> stateList = new ArrayList<String>();
		stateList.addAll(Arrays.asList(stateAry));
		logger.debug(">>>stateList="+stateList);
		WTArrayList reviewObjs = ext.generic.reviewObject.util.ReviewObjectAndWorkFlowLink.getReviewObjectByProcess (self); 
		int size = reviewObjs.size();
		logger.debug(">>>ReviewObjects.size="+size);
		WTArrayList partList = new WTArrayList();
		for(int w=0; w<size; w++){
			Persistable pobj = reviewObjs.getPersistable(w);
		}
		logger.debug(">>>PartSize In ReviewObjs="+partList.size());
		WTPart pboPart = (WTPart) pbo;
		View pboView = null;
		if(pboPart.getView() != null){
			pboView = (View) pboPart.getView().getObject();
		}
		WTSet bomSet = new WTHashSet();
		WTCollection partCollection = PIPartHelper.service.findChildren(partList, pboView);
		bomSet.addAll(partCollection);
		//bom最多十层
		for(int i=1; i<10; i++){
			WTArrayList wtlist = new WTArrayList();
			wtlist.addAll(bomSet);
			bomSet.addAll(PIPartHelper.service.findChildren(wtlist, pboView));
			logger.debug("i="+i + ",,,size=" +bomSet.size());
		}
		WTArrayList bomList = new WTArrayList();
		bomList.addAll(bomSet);
		
		Locale locale = SessionHelper.manager.getLocale();
		int bomSize = bomList.size();
		logger.debug(">>>bomSize="+bomSize);
		for(int w=0; w<bomSize; w++){
			WTPart part = (WTPart) bomList.getPersistable(w);
			if(part == null || reviewObjs.contains(part)){
				continue;
			}
			String state = part.getLifeCycleState().toString();
			String stateDisplay = part.getLifeCycleState().getDisplay(locale);
			logger.debug(">>>Part:"+part.getNumber()+",,,state="+state);
			if(!states.contains(state)){
				logger.debug("Part:"+part.getNumber()+" State Not Ok");
				error.append("部件：").append(part.getDisplayIdentifier()).append(" 状态："
						+ "").append(stateDisplay).append("不符合条件\n");
			}
		}
		
		if(error.length()>0){
			throw new WTException(error.toString());
		}
		
	}
}
