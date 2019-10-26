package ext.appo.update;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.ptc.netmarkets.workflow.NmWorkflowHelper;

import wt.change2.ChangeHelper2;
import wt.change2.ChangeOrder2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.State;
import wt.method.RemoteMethodServer;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.vc.config.LatestConfigSpec;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfProcess;

public class UpdateECAState {

	public static void main(String[] args) throws Exception {
		String ecnnumber = null;

		if (args == null)
			return;
		for (int i = 0; i < args.length; i += 2) {
			if (i + 1 < args.length) {
				 if (("-e".equals(args[i]))) {
					 ecnnumber = args[i + 1];
				 }
			}
		}
		RemoteMethodServer server = RemoteMethodServer.getDefault();
		server.setUserName("wcadmin");
		server.setPassword("wcadmin");
		List<String> list=new ArrayList<String>();
		WTChangeOrder2 ecn=null;
		try {
			//WTChangeOrder2 ecn=null;
			Vector ecnvec =getAllEC("");
			for(int i=0;i<ecnvec.size();i++){
				String number=((ChangeOrder2)ecnvec.get(i)).getNumber();
				if (ecnnumber.startsWith(number)) {
					ecn=(WTChangeOrder2)ecnvec.get(i);
					break;
				}
			}

			System.out.println("ecn number==="+ecn.getNumber());
			QueryResult processResult=new QueryResult();
			QueryResult ecnqr = ChangeHelper2.service.getChangeActivities(ecn);
			while (ecnqr.hasMoreElements()) {
				WTChangeActivity2 eca = (WTChangeActivity2) ecnqr.nextElement();
                  System.out.println("ECA number===="+eca.getNumber());
                  WfEngineHelper.service.terminateObjectsRunningWorkflows(eca);
				 if(eca.getState().toString().equalsIgnoreCase("OPEN")||eca.getState().toString().equalsIgnoreCase("IMPLEMENTATION"))
					 System.out.println("ECA number===="+eca.getNumber());
					 {
					 if(list.contains(eca.getName())){
						 LifeCycleHelper.service.setLifeCycleState(eca, State.toState("CANCELLED"), false);
						 System.out.println("ECA number===="+eca.getNumber()+"change ok!");
					 }else{
					  list.add(eca.getName());
					 }
					 }	

		}
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (WTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	//如果为“”，则得到所有的ecn
		public static Vector getAllEC(String number) throws Exception
		{
			QuerySpec qs = new QuerySpec(ChangeOrder2.class);
	        if(number==null)
	        	return null;
			if (number.trim().length() > 0)
			{
				
				SearchCondition scNumber = new SearchCondition(WTChangeOrder2.class, WTChangeOrder2.NUMBER, SearchCondition.EQUAL, number.toUpperCase());
				qs.appendWhere(scNumber);
			}

			SearchCondition scLatestIteration = new SearchCondition(ChangeOrder2.class, WTAttributeNameIfc.LATEST_ITERATION, SearchCondition.IS_TRUE);
			
			qs.appendWhere(scLatestIteration);

			QueryResult qr = PersistenceHelper.manager.find(qs);
			if (qr != null && qr.hasMoreElements())
				qr = (new LatestConfigSpec()).process(qr);

			if (qr != null && qr.hasMoreElements())
				return qr.getObjectVectorIfc().getVector();

			return new Vector();
		}
}
