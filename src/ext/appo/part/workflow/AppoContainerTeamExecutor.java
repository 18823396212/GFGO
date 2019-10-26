package ext.appo.part.workflow;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

import wt.fc.QueryResult;
import wt.inf.team.ContainerTeam;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.team.ContainerTeamManaged;
import wt.method.RemoteAccess;
import wt.org.OrganizationServicesHelper;
import wt.org.WTGroup;
import wt.org.WTPrincipal;
import wt.project.Role;
import wt.team.WTRoleHolder2;
import wt.util.WTException;
import wt.util.WTInvalidParameterException;
import wt.util.WTProperties;

import com.ptc.netmarkets.roleAccess.NmRoleAccessHelper;
import com.ptc.windchill.enterprise.team.server.TeamCCHelper;

/**
 * @author nieshanfu </br> 
 * 本类用于执行工作流角色临时成员的增加/移除工作
 */
public class AppoContainerTeamExecutor implements RemoteAccess, Serializable
{
	public static final String GUEST = "GUEST";
	public static final String ADD = "ADD";
	public static final String REMOVE = "REMOVE";

	private static final long serialVersionUID = 111L;
	protected ContainerTeamManaged containerTeamManaged;
	protected String containerRole;
	protected Vector principalVector;
	protected String operation;

	private static final boolean VERBOSE;

	static
	{
		try
		{
			WTProperties wtproperties = WTProperties.getLocalProperties();
			VERBOSE = wtproperties.getProperty("ext.generic.service.verbose", false);
		} catch (Throwable throwable)
		{
			System.err.println((new StringBuilder()).append("Error initializing ").append(AppoContainerTeamExecutor.class.getName()).toString());
			throwable.printStackTrace(System.err);
			throw new ExceptionInInitializerError(throwable);
		}
	}

	public AppoContainerTeamExecutor()
	{
		this.containerTeamManaged = null;
		this.containerRole = "";
		this.principalVector = new Vector();
		this.operation = "";
	}

	public AppoContainerTeamExecutor(ContainerTeamManaged containerTeamManaged)
	{
		this.containerTeamManaged = containerTeamManaged;
		this.containerRole = "";
		this.principalVector = new Vector();
		this.operation = "";
	}

	public AppoContainerTeamExecutor(ContainerTeamManaged containerTeamManaged, String containerRole, Vector principalVector, String operation)
	{
		this.containerTeamManaged = containerTeamManaged;
		this.containerRole = containerRole;
		this.principalVector = principalVector;
		this.operation = operation;
	}

	public void doExecute()
	{
		this.containerRole = this.containerRole == null ? "" : this.containerRole.trim();
		this.operation = this.operation == null ? "" : this.operation.trim();

		if (this.containerTeamManaged == null)
		{
			System.out.println(AppoContainerTeamExecutor.class.getSimpleName() + " the containerTeamManaged is null,so can not continue...");
			return;
		}

		if (this.containerRole.length() == 0)
		{
			System.out.println(AppoContainerTeamExecutor.class.getSimpleName() + " the containerRole is blank");
			return;
		}

		if (this.operation.length() == 0)
		{
			System.out.println(AppoContainerTeamExecutor.class.getSimpleName() + " the operation is blank");
			return;
		}

		Role systemRole = null; //用于验证该角色KEY是否已在系统中配置

		try
		{
			systemRole = Role.toRole(this.containerRole);
		} catch (WTInvalidParameterException e)
		{
			e.printStackTrace();
		}

		if (systemRole == null)
		{
			System.out.println(AppoContainerTeamExecutor.class.getSimpleName() + " the role assigned can not find assigned role " + this.containerRole);
			return;
		}

		if (this.operation.equalsIgnoreCase(ADD))
		{
			System.out.println("execute add==>start");
			try
			{
				boolean flag = addUsersToRole(this.containerTeamManaged, this.containerRole, this.principalVector);
				System.out.println(flag);
			} catch (WTException e)
			{
				e.printStackTrace();
			}
			System.out.println("execute add");
		} else if (this.operation.equalsIgnoreCase(REMOVE))
		{
			System.out.println("execute remove-->start");
			try
			{
				boolean flag = removeUsersFromRole(this.containerTeamManaged, this.containerRole, this.principalVector);
				System.out.println(flag);
			} catch (WTException e)
			{
				e.printStackTrace();
			}
			System.out.println("execute remove==>end");
		} else
		{
			System.out.println(AppoContainerTeamExecutor.class.getSimpleName() + " the operation " + this.operation + " is inaccurate");
		}
	}

	protected boolean removeUsersFromRole(ContainerTeamManaged containerTeamManaged, String roleStr, Vector principalVector) throws WTException
	{
		boolean resultFlag = false;
		boolean flag = false;

		WTRoleHolder2 wtroleholder2 = TeamCCHelper.getTeamFromObject(containerTeamManaged);

		for (int i = 0; i < principalVector.size(); i++)
		{
			WTPrincipal wtprincipal = (WTPrincipal) principalVector.get(i);
			if (roleStr.equalsIgnoreCase(GUEST))
			{
				
				WTGroup wtgroup = ContainerTeamHelper.service.findContainerTeamGroup((ContainerTeam) wtroleholder2, GUEST, GUEST);

				Enumeration enumeration = OrganizationServicesHelper.manager.members(wtgroup, false);
				boolean flag2 = false;

				while (enumeration.hasMoreElements())
				{
					WTPrincipal awtPrincipal = (WTPrincipal) enumeration.nextElement();
					System.out.println("DEBUG==>" + awtPrincipal);
					if (wtprincipal.equals(awtPrincipal))
					{
						flag2 = true;
						break;
					}
				}

				if (flag2)
				{
					ContainerTeamHelper.service.removeGuestMember((ContainerTeam) wtroleholder2, wtprincipal);
					flag = true;
				}
			} else
			{
				Role role = null;
				try
				{
					role = Role.toRole(roleStr);
				} catch (WTInvalidParameterException e)
				{
					e.printStackTrace();
				}

				if (role != null)
				{
					wtroleholder2.deletePrincipalTarget(role, wtprincipal);
					flag =true;
				}
			}
		}

		return resultFlag;
	}

	protected boolean addUsersToRole(ContainerTeamManaged containerTeamManaged, String roleStr, Vector principalVector) throws WTException
	{
		System.out.println("addUsersToRole==>"+roleStr+ " "+ principalVector.size() );
		
		boolean resultFlag = false;
		boolean flag = false;
		WTRoleHolder2 wtroleholder2 = TeamCCHelper.getTeamFromObject(containerTeamManaged); //获取

		for (int i = 0; i < principalVector.size(); i++)
		{
			WTPrincipal wtprincipal = (WTPrincipal) principalVector.get(i);
			System.out.println("addUsersToRole==>222 "+roleStr);

			if (roleStr.equalsIgnoreCase(GUEST))
			{
				WTGroup wtgroup = ContainerTeamHelper.service.findContainerTeamGroup((ContainerTeam) wtroleholder2, GUEST, GUEST);

				Enumeration enumeration = OrganizationServicesHelper.manager.members(wtgroup, false);
				boolean flag2 = false;

				while (enumeration.hasMoreElements())
				{
					WTPrincipal awtPrincipal = (WTPrincipal) enumeration.nextElement();
					System.out.println("DEBUG==>" + awtPrincipal.getName());

					if (wtprincipal.equals(awtPrincipal))
					{
						flag2 = true;
						break;
					}
				}

				if (!flag2)
				{
					ContainerTeamHelper.service.addGuestMember((ContainerTeam) wtroleholder2, wtprincipal);
					flag = true;
				}
			} else
			{
				Role role = null;
				try
				{
					role = Role.toRole(roleStr);
				} catch (WTInvalidParameterException e)
				{
					e.printStackTrace();
				}
                 System.out.println("role=="+role);
				if (role != null)
				{
					QueryResult queryresult = ContainerTeamHelper.service.findRolePrincipalMap(role, wtprincipal, (ContainerTeam) wtroleholder2);
					System.out.println("queryresult=="+queryresult.size());
					if (!queryresult.hasMoreElements())
					{
						((ContainerTeam) wtroleholder2).addPrincipal(role, wtprincipal);
						flag = true;
					}
				}
			}
		}

		if (flag)
		{
			NmRoleAccessHelper.service.resetCache();
		}

		return resultFlag;
	}

	public static void main(String[] args)
	{

	}

	public ContainerTeamManaged getContainerTeamManaged()
	{
		return containerTeamManaged;
	}

	public void setContainerTeamManaged(ContainerTeamManaged containerTeamManaged)
	{
		this.containerTeamManaged = containerTeamManaged;
	}

	public String getRole()
	{
		return containerRole;
	}

	public void setRole(String role)
	{
		this.containerRole = role;
	}

	public Vector getPrincipalVector()
	{
		return principalVector;
	}

	public void setPrincipalVector(Vector principalVector)
	{
		this.principalVector = principalVector;
	}

	public String getOperation()
	{
		return operation;
	}

	public void setOperation(String operation)
	{
		this.operation = operation;
	}

}
