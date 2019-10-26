package ext.generic.integration.cis.validators;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;

import wt.fc.Persistable;
import wt.fc.WTObject;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerRef;
import wt.inf.team.ContainerTeam;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.team.ContainerTeamManaged;
import wt.lifecycle.LifeCycleManaged;
import wt.log4j.LogR;
import wt.org.OrganizationServicesHelper;
import wt.org.WTGroup;
import wt.org.WTOrganization;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.project.Role;
import wt.session.SessionHelper;
import wt.util.WTException;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;

import ext.com.org.OrgUtil;
import ext.com.workflow.WorkflowUtil;
import ext.generic.integration.cis.rule.CISBusinessRuleXML;

public class PublishDocValidators extends DefaultSimpleValidationFilter{
	private static final String CLASSNAME = PublishDocValidators.class.getName();
	private static final Logger logger = LogR.getLogger(CLASSNAME);
	@Override
	public UIValidationStatus preValidateAction(UIValidationKey uivalidationKey,
			UIValidationCriteria criteria) {
		logger.debug("Enter .preValidateAction()");
		UIValidationStatus status = UIValidationStatus.HIDDEN;
		Persistable pobj = criteria.getContextObject().getObject();
		logger.debug("ContextObj="+pobj);
		WTContainer container = criteria.getParentContainer().getContainer();
		logger.debug("container="+container);
		if(pobj instanceof WTPart){
			try {
				if(WorkflowUtil.isObjectCheckedOut((WTObject)pobj)){
					return status;
				}
					CISBusinessRuleXML rule = CISBusinessRuleXML.getInstance();
					List<String> states = rule.getManuallyPublishState();
					String state = ((LifeCycleManaged)pobj).getLifeCycleState().toString();
					boolean stateOk = true;
					if(states != null && states.size()>0){
						stateOk = states.contains(state);
					}
					if(stateOk){
						// 获取当前用户
						WTUser curUser = (WTUser) SessionHelper.manager.getPrincipal();
						logger.debug("current User = "+curUser.getName());
						List<String> roles = rule.getManuallyPublishUserRole();
						logger.debug("roles="+roles);
						boolean isMember = false;
						if(container instanceof ContainerTeamManaged){
							for(String roleName : roles){
								//修正角色中是组，无法判断用户属于角色问题
								List<WTUser> users = getUsersByRole(roleName,container);
								if (users != null && users.contains(curUser)) {
									isMember = true;
									break;
								}
							}
						}
						if(!isMember){
							List<String> groups = rule.getManuallyPublishUserGroup();
							logger.debug("groups="+groups);
							if(groups.size()==0 && roles.size()==0){
								isMember = true;
							}
							for(String group : groups){
								isMember = OrgUtil.isMemberOfGroup(group , curUser);
								if (isMember)
									break;
							}
						}
						logger.debug("isMember="+isMember);
						if(criteria.isSiteAdmin() || isMember){
							status = UIValidationStatus.ENABLED;
						}
					}
					
			} catch (WTException e) {
				if(logger.isDebugEnabled()){
					e.printStackTrace();
				}
				logger.error("判断手工发布显示条件出现异常Error："+e);
			}
		}
		return status;
	}
	/**
	 * 获得wtprincipal对象下的所有用户
	 * @param users 存储用户集合
	 * @param wtp 组或用户
	 * @return
	 * @throws WTException
	 */
	public static List<WTUser> getWTUserFromWTPrincipal(List<WTUser> users,WTPrincipal wtp) throws WTException{
		if(users != null){
			if (wtp instanceof WTUser){
				WTUser user =(WTUser)wtp;
				if(!users.contains(user)){
					users.add(user);
				}
				
			}else if (wtp instanceof WTGroup){
				WTGroup group = (WTGroup) wtp;
				Enumeration emm = OrganizationServicesHelper.manager.members(group, true);
				while (emm.hasMoreElements()){
					WTUser user = (WTUser) emm.nextElement();
					if(!users.contains(user)){
						users.add(user);
					}
				}
			}
		}
		return users;
	}
	
	/**
	 * 获得角色的用户
	 * @param interRoleName
	 * @param wtcontainer
	 * @return
	 * @throws WTException
	 */
	public static List<WTUser> getUsersByRole(String interRoleName, WTContainer wtcontainer) throws WTException {
		List<WTUser> users = new ArrayList<WTUser>();
		if (users != null) {
			// 角色的内部值获得角色
			Role r = Role.toRole(interRoleName);

			ContainerTeam containerTeam = ContainerTeamHelper.service.getContainerTeam((ContainerTeamManaged) wtcontainer);
			ArrayList princArray = containerTeam.getAllPrincipalsForTarget(r);

			for (int i = 0; i < princArray.size(); i++) {
				WTPrincipalReference wtpRef = (WTPrincipalReference) princArray.get(i);
				WTPrincipal wtp = wtpRef.getPrincipal();
				users = getWTUserFromWTPrincipal(users, wtp);
			}
		}

		return users;
	}
 
	 /**
	  * 是否为站点管理员
	  * @param wtPrincipal
	  * @return
	  * @throws WTException
	  */
    public static boolean isSiteAdmin(WTPrincipal wtPrincipal) throws WTException {
        return WTContainerHelper.service.isAdministrator(WTContainerHelper.service.getExchangeRef(), wtPrincipal);
    }

    /**
     * 是否为组织管理员
     * @param paramWTPrincipal
     * @return
     * @throws WTException
     */
    public static boolean isOrgAdmin(WTPrincipal paramWTPrincipal) throws WTException {
        boolean isGrgAdmin = false;
        WTOrganization wtOrganization = OrganizationServicesHelper.manager.getOrganization(paramWTPrincipal);
        WTContainerRef wtContainerRef = WTContainerHelper.service.getOrgContainerRef(wtOrganization);
        if (wtContainerRef != null) {
            isGrgAdmin = WTContainerHelper.service.isAdministrator(wtContainerRef, paramWTPrincipal);
        }
        return isGrgAdmin;
    }

    /**
     * 是否为管理员
     * @param paramWTPrincipa
     * @return
     */
    public static boolean isAdmin(WTPrincipal paramWTPrincipa) {
        boolean isAdmin = false;
        try {
            if (isOrgAdmin(paramWTPrincipa) || isSiteAdmin(paramWTPrincipa)) {
                isAdmin = true;
            }
        } catch (WTException e) {
            e.printStackTrace();
        }
        return isAdmin;
    }
}
