package ext.appo.access;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

import wt.access.AccessControlHelper;
import wt.access.AccessControlRule;
import wt.access.AccessPermission;
import wt.access.AccessPermissionSet;
import wt.admin.AdminDomainRef;
import wt.admin.AdministrativeDomain;
import wt.admin.AdministrativeDomainHelper;
import wt.lifecycle.State;
import wt.org.WTGroup;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTRolePrincipal;
import wt.org.WTUser;
import wt.util.WTException;

public class ACLExporter {
	
	public static void exportDomainACLtoCSVFile(String domainPath, String csvFilePath) throws IOException, WTException {
		 Collection<AccessControlRule> rules = getACLFromDomain(domainPath);
		 exportToCSV(domainPath, rules, csvFilePath);
	}
	
	public static void deleteAllACL(String domainPath) throws WTException {
		AdministrativeDomain domain = AdministrativeDomainHelper.manager.getDomain(domainPath);
		AccessControlHelper.manager.deleteAccessControlRules(AdminDomainRef.newAdminDomainRef(domain));
	}

	public static Collection<AccessControlRule> getACLFromDomain(String domainPath) throws WTException {
		AdministrativeDomain domain = AdministrativeDomainHelper.manager.getDomain(domainPath);
		Collection<AccessControlRule> rules = AccessControlHelper.manager.getAccessControlRules(AdminDomainRef.newAdminDomainRef(domain));
		return rules;
	}

	public static void exportToCSV(String domainPath, Collection<AccessControlRule> rules, String csvFilePath) throws IOException, WTException {
		if (rules == null || rules.isEmpty()) {
			System.out.println("There no rules for export!");
			return;
		}
		
		TreeMap<String, AccessControlRule> ruleMap = new TreeMap<String, AccessControlRule>();
		
		
		for (AccessControlRule rule : rules) {
			//convert WTPrincipalRef to String
			String principal = null;
			String stateStr = "";
			WTPrincipalReference pRef = rule.getPrincipalRef();
			if (WTPrincipalReference.ALL.equals(pRef)) {
				principal = "ALL";
			} else if (WTPrincipalReference.OWNER.equals(pRef)) {
				principal = "OWNER";
			} else {
				WTPrincipal wtPrincipal = pRef.getPrincipal();
				if (wtPrincipal instanceof WTUser) {
					principal = ((WTUser) wtPrincipal).getName();
				}
				
				if (wtPrincipal instanceof WTGroup) {
					WTGroup group = (WTGroup) wtPrincipal;
					principal = group.getName();
				}
				
				if (wtPrincipal instanceof WTRolePrincipal) {
					WTRolePrincipal rolePrincipal = (WTRolePrincipal) wtPrincipal;
					principal = rolePrincipal.getRole().toString();
				}
			}
			
			// convet state to string
			State state = rule.getState();
			if (state == null) {
				stateStr = "ALL";
			} else {
				stateStr = state.toString();
			}
			String key = rule.getTypeId() + "$" + principal + "$" + stateStr;
			ruleMap.put(key, rule);
		}
		FileWriter fw = new FileWriter(csvFilePath);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("#AccessRule,user,domain,typeId,permission,principal,permissionList,state\n");
		rules = ruleMap.values();
		for (AccessControlRule rule : rules) {
			bw.write(getAccessControlRuleString(domainPath, rule));
		}
		bw.flush();
		bw.close();
		fw.close();
	}

	public static String getAccessControlRuleString(String domainPath, AccessControlRule rule) throws WTException {
		//#AccessRule,user,domain,typeId,permission,principal,permissionList,state
		//AccessRule,,[/wt.inf.container.OrgContainer=ipg/wt.pdmlink.PDMLinkProduct=CTO_DT_COMMON]/Default,WCTYPE|wt.part.WTPart|com.lenovo.lenovoPart|com.lenovo.86Part,+,ALL,0,ALL
		

		//convert WTPrincipalRef to String
		String principal = null;
		String stateStr = "";
		WTPrincipalReference pRef = rule.getPrincipalRef();
		if (WTPrincipalReference.ALL.equals(pRef)) {
			principal = "ALL";
		} else if (WTPrincipalReference.OWNER.equals(pRef)) {
			principal = "OWNER";
		} else {
			WTPrincipal wtPrincipal = pRef.getPrincipal();
			if (wtPrincipal instanceof WTUser) {
				principal = ((WTUser) wtPrincipal).getName();
			}
			
			if (wtPrincipal instanceof WTGroup) {
				WTGroup group = (WTGroup) wtPrincipal;
				principal = group.getName();
			}
			
			if (wtPrincipal instanceof WTRolePrincipal) {
				WTRolePrincipal rolePrincipal = (WTRolePrincipal) wtPrincipal;
				principal = rolePrincipal.getRole().toString();
			}
		}
		
		// convet state to string
		State state = rule.getState();
		if (state == null) {
			stateStr = "ALL";
		} else {
			stateStr = state.toString();
		}
		
		StringBuffer sb = new StringBuffer();
		// if ACL granted permission
		AccessPermissionSet grantedPermissionSet = rule.getGrantPermissions();
		if (grantedPermissionSet != null && !grantedPermissionSet.isEmpty()) {
			sb.append("AccessRule,,");
			sb.append(domainPath);
			sb.append(",");
			sb.append(rule.getTypeId());
			sb.append(",");
			sb.append("+");
			sb.append(",");
			sb.append(principal);
			sb.append(",");
			
			// convert Permission to string
			String grantedPermission = "";
			Iterator it = grantedPermissionSet.iterator();
			int i = 0;
			while (it.hasNext()) {
				i++;
				AccessPermission p = (AccessPermission) it.next();
				if (i == 1) {
					grantedPermission = grantedPermission + p.toString();
				} else {
					grantedPermission = grantedPermission + "/" + p.toString();
				}
			}
			sb.append(grantedPermission);
			sb.append(",");
			sb.append(stateStr);
			sb.append("\n");
		}
		
		AccessPermissionSet denyPermissionSet = rule.getDenyPermissions();
		if (denyPermissionSet != null && !denyPermissionSet.isEmpty()) {
			sb.append("AccessRule,,");
			sb.append(domainPath);
			sb.append(",");
			sb.append(rule.getTypeId());
			sb.append(",");
			sb.append("-");
			sb.append(",");
			sb.append(principal);
			sb.append(",");
			
			// convert Permission to string
			String denyPermission = "";
			Iterator it = denyPermissionSet.iterator();
			int i = 0;
			while (it.hasNext()) {
				i++;
				AccessPermission p = (AccessPermission) it.next();
				if (i == 1) {
					denyPermission = denyPermission + p.toString();
				} else {
					denyPermission = denyPermission + "/" + p.toString();
				}
			}
			sb.append(denyPermission);
			sb.append(",");
			sb.append(stateStr);
			sb.append("\n");
		}

		return sb.toString();
	}

}
