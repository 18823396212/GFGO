<%@page import="java.util.Map"%>
<%@ taglib prefix="jca" uri="http://www.ptc.com/windchill/taglib/components" %>
<%@ include file="/netmarkets/jsp/util/begin.jspf"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/wrappers" prefix="w"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"   %>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/fmt" prefix="fmt"%>
<%@ page import="wt.workflow.work.WorkItem,java.util.Hashtable,wt.workflow.engine.WfActivity,wt.fc.Persistable"%>
<%@ page import="wt.workflow.engine.WfProcess,ext.generic.reviewprincipal.util.*,wt.project.Role,wt.fc.WTObject"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="ext.generic.workflow.util.ReviewActivityUtil"%>
<%@ page import="java.util.Locale"%>
<%@ page import="wt.session.SessionServerHelper"%>

<fmt:setLocale value="${localeBean.locale}" />
<fmt:setBundle basename="ext.generic.reviewprincipal.resource.ReviewPrincipalRB" />
<fmt:message var="roleStrvar"	key="SELECTROLE" />

<%
	NmOid contextNmOid = commandBean.getElementContext().getPrimaryOid();
	ArrayList internalvalue = new ArrayList();
	ArrayList desplayvalue = new ArrayList();

	boolean bool = SessionServerHelper.manager.isAccessEnforced();
	try {
		SessionServerHelper.manager.setAccessEnforced(false);

		if (contextNmOid != null) {

			WorkItem workitem = (WorkItem) contextNmOid.getRefObject();

			WfActivity wfactivity = (WfActivity) workitem.getSource().getObject();

			Persistable pbo = workitem.getPrimaryBusinessObject().getObject();

			WfProcess wfprocess = wfactivity.getParentProcess();
			
			// 获取所有活动
			String setactive = "" ;
			Hashtable<String, String> table = ReviewActivityUtil.getAugumentTable((WTObject) pbo, null, wfprocess);
			if(table != null){
				setactive = table.get("augmentRoles");
			}
			System.out.println("setactive : " + setactive);

			//add by ccao 20141222
			Object isAllActivties = wfactivity.getContext().getValue("isAllActivties");
			Map<String, Role> acttable = null;
			System.out.println("isAllActivties : " + isAllActivties);
			if (isAllActivties != null) {
				
				if("YES".equals(isAllActivties)){
					// 获取所有活动
					acttable = ReviewPrincipalUtil.getAllActivitiesWithOrdered(wfactivity, wfprocess);
					System.out.println("YES : " + acttable);
				}else if(!"".equals(isAllActivties)){
					//获得特定活动节点
					acttable = ReviewPrincipalUtil.getSpecialActivities(wfactivity,isAllActivties, wfprocess);
					System.out.println("NO : " + acttable);
				}

			} else {
				// 获取下一步活动
				acttable = ReviewPrincipalUtil.getNextActivity(workitem, wfactivity, pbo, wfprocess);
				System.out.println("else : " + acttable);

			}
			//add by ccao 20141222 end
			//if (acttable != null) {
			//	java.util.Collection collection = acttable.values();
			//	java.util.Iterator iter = collection.iterator();
			//	while (iter != null && iter.hasNext()) {
			//		Role role = (Role) iter.next();
			//		if("SUBMITTER".equals(role.toString())){
			//			continue;
			//		}
			//		internalvalue.add(role.toString());
			//		desplayvalue.add(role.getDisplay());
			//	}
			//}
			
			// Modify bu kwang : 排序
			if(setactive != null && !setactive.trim().isEmpty()){
				if(acttable != null){
					String [] activity = null ;
					if(setactive.contains(";;;qqq")){
						activity = setactive.split(";;;qqq");  
					}else{
						activity = new String[]{setactive} ;
					}
					for(String activityName : activity){
						if(acttable.containsKey(activityName)){
							Role role = acttable.get(activityName);
							if("SUBMITTER".equals(role.toString())){
								continue;
							}
							internalvalue.add(role.toString());
							desplayvalue.add(role.getDisplay(Locale.CHINA));
						}
					}
				}
			}else{
				if (acttable != null) {
					java.util.Collection collection = acttable.values();
					java.util.Iterator iter = collection.iterator();
					while (iter != null && iter.hasNext()) {
						Role role = (Role) iter.next();
						if("SUBMITTER".equals(role.toString())){
							continue;
						}
						internalvalue.add(role.toString());
						desplayvalue.add(role.getDisplay(Locale.CHINA));
					}
				}
			}

		}

	} finally {
		SessionServerHelper.manager.setAccessEnforced(bool);
	}
%>

<c:set var="arrayvalue" value="<%=internalvalue %>"/>
  <c:set var="arraydisp" value="<%=desplayvalue %>"/>
  	
  <jca:renderPropertyPanel>
      <w:comboBox id="rolecomb" propertyLabel="${roleStrvar}"  name="rolecomb"  size="1" required="true"  multiSelect="false"  internalValues="${arrayvalue}"  displayValues="${arraydisp}" onchange="changeDepart(this);"/>
  </jca:renderPropertyPanel>
	
<%@ include file="/netmarkets/jsp/util/end.jspf"%>
  

  	
  	
		    		   