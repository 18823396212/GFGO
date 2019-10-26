<%@page import="wt.fc.PersistenceHelper"%>
<%@page import="java.util.ArrayList"%>
<%@page import="wt.workflow.engine.WfActivity"%>
<%@page import="wt.workflow.work.WorkItem"%>
<%@page import="wt.fc.ReferenceFactory"%>
<%@page import="wt.workflow.engine.WfProcess"%>
<%@page import="wt.workflow.engine.ProcessData"%>

<%@ page  pageEncoding="UTF-8"%>
<%
  boolean flag = false;
  try{
    flag =  wt.session.SessionServerHelper.manager.setAccessEnforced(false); 
		String activityName = request.getParameter("activityName");
		String workItemOid = request.getParameter("workItemOid");
		ReferenceFactory rf = new ReferenceFactory();
		WorkItem workitem = (WorkItem) rf.getReference(workItemOid).getObject();
		WfActivity wfactivity = (WfActivity) workitem.getSource().getObject();
		ProcessData context = wfactivity.getContext();
		String augmentActivities = (String)context.getValue("augmentActivities");
		String[] augmentActs = augmentActivities.split(">>AND<<");
		ArrayList<String> arr = new ArrayList<String>();
		for(int i=0;i<augmentActs.length;i++){
			arr.add(augmentActs[i]);
		}
		if(!arr.contains(activityName)){
			arr.add(activityName);
		}
		StringBuffer sbuff = new StringBuffer();
		for(int i=0;i<arr.size();i++){
			if(sbuff.length()>0){
				sbuff.append(">>AND<<");
			}
			sbuff.append(arr.get(i));
		}
		String value="";
		if(sbuff.length()>0){
			value = sbuff.toString();
		}
		
		if(workitem.getContext() != null){
			workitem.getContext().setValue("augmentActivities", value);
			workitem = (WorkItem)PersistenceHelper.manager.save(workitem);
			PersistenceHelper.manager.refresh(workitem);
		}
		
		wfactivity.getContext().setValue("augmentActivities",value);
		wfactivity=(WfActivity)PersistenceHelper.manager.save(wfactivity);
		PersistenceHelper.manager.refresh(wfactivity); 

		out.print("ok");
		out.flush();
		
	}finally{
     wt.session.SessionServerHelper.manager.setAccessEnforced(flag);           
  }
%>