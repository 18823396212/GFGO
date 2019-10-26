package ext.appo.access;

import java.util.Collection;
import java.util.List;

import wt.access.AccessControlHelper;
import wt.access.AccessControlRule;
import wt.admin.AdminDomainRef;
import wt.admin.AdministrativeDomain;
import wt.admin.AdministrativeDomainHelper;
import wt.inf.container.WTContainer;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.team.ContainerTeamManaged;
import wt.org.WTGroup;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.util.WTException;

public class ACLUtil {


	/**
	 * @param sourceDomainPath
	 * @param targetDomainPathList
	 * @throws WTException
	 */
	public static void synchronizeDomainACL(String sourceDomainPath, List<String> targetDomainPathList) throws WTException {
		AdministrativeDomain sourceDomain = AdministrativeDomainHelper.manager.getDomain(sourceDomainPath);
		Collection<AccessControlRule> aclList = AccessControlHelper.manager.getAccessControlRules(AdminDomainRef.newAdminDomainRef(sourceDomain));
		for (String targetDomainPath: targetDomainPathList) {
			AdministrativeDomain targetDomain = AdministrativeDomainHelper.manager.getDomain(targetDomainPath);
			fillDomainACL(aclList, targetDomain);
		}
	}
	
	/**
	 * 将一个Domian的权限，同步到另一个Domain中去
	 * @param sourceDomain ACL source domain
	 * @param targetDomain target domain, all ACL in this domian will be deleted befre
	 * @throws WTException 
	 */
	public static void synchronizeDomainACL(AdministrativeDomain sourceDomain, AdministrativeDomain targetDomain) throws WTException {
		Collection<AccessControlRule> aclList = AccessControlHelper.manager.getAccessControlRules(AdminDomainRef.newAdminDomainRef(sourceDomain));
		fillDomainACL(aclList, targetDomain);
	}
	
	public static void fillDomainACL(Collection<AccessControlRule> aclList, AdministrativeDomain targetDomain) throws WTException {
		AdminDomainRef domainRef = AdminDomainRef.newAdminDomainRef(targetDomain);
		WTContainer targetContainer = targetDomain.getContainer(); // target container
		// delete all ACL first!
		AccessControlHelper.manager.deleteAccessControlRules(domainRef);
		
		// 开始复制ACL
		for (AccessControlRule rule : aclList) {
			WTPrincipalReference pRef = rule.getPrincipalRef();
			// if Principal is ALL or OWNER, create ACL 
			if (WTPrincipalReference.ALL.equals(rule.getPrincipalRef()) && WTPrincipalReference.OWNER.equals(rule.getPrincipalRef())) {
				AccessControlHelper.manager.createAccessControlRule(domainRef, rule.getTypeId(), rule.getState(), rule.getPrincipalRef(), rule.getGrantPermissions(), rule.getDenyPermissions());
				continue;
			}
			WTPrincipal p = pRef.getPrincipal();
			if (p instanceof WTGroup) { // 如果P是一个Group
				WTGroup group = (WTGroup) p;
				if (group.isInternal()) { // 如果Group是一个ContainerTeam中的Context role group
					String roleName = group.getName();
					if (targetContainer instanceof ContainerTeamManaged) {
						WTGroup g2 = null;
						if ("teamMembers".equals(roleName)) {
							g2 = ContainerTeamHelper.service.findContainerTeamGroup((ContainerTeamManaged) targetContainer, ContainerTeamHelper.TEAM_MEMBERS, roleName);
						} else if ("GUEST".equals(roleName)) {
							g2 = ContainerTeamHelper.service.findContainerTeamGroup((ContainerTeamManaged) targetContainer, ContainerTeamHelper.GUEST, roleName);
						} else {
							g2 = ContainerTeamHelper.service.findContainerTeamGroup((ContainerTeamManaged) targetContainer, ContainerTeamHelper.ROLE_GROUPS, roleName);
						}
						AccessControlHelper.manager.createAccessControlRule(domainRef, rule.getTypeId(), rule.getState(), WTPrincipalReference.newWTPrincipalReference(g2), rule.getGrantPermissions(), rule.getDenyPermissions());
					} else {
						System.out.println("***WARNING***:" + rule + " not create in target domain.");
					}
				} else { // 是一个外部Group，比如组织中的Group，Site中的Group
					AccessControlHelper.manager.createAccessControlRule(domainRef, rule.getTypeId(), rule.getState(), rule.getPrincipalRef(), rule.getGrantPermissions(), rule.getDenyPermissions());
				}
			} else {
				AccessControlHelper.manager.createAccessControlRule(domainRef, rule.getTypeId(), rule.getState(), rule.getPrincipalRef(), rule.getGrantPermissions(), rule.getDenyPermissions());
			}
		}
	}

}
