package ext.appo.test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.netmarkets.model.NmChangeModel;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.services.StandardManager;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTProperties;

public class StandardCustService extends StandardManager implements CustService, Serializable
{

	private static final String CLASSNAME = StandardCustService.class.getName();
	private static final boolean VERBOSE;
	private static boolean WILDCARD_SEARCH = true;
	private static String templateName;
	private static final Logger LOG;

	static
	{
		try
		{
			LOG = LogR.getLogger(StandardCustService.class.getName());
			WTProperties wtproperties = WTProperties.getLocalProperties();
			VERBOSE = wtproperties.getProperty("ext.generic.service.verbose", false);
			WILDCARD_SEARCH = wtproperties.getProperty("com.ptc.netmarkets.user.principalWildcardSearch", true);
			templateName = wtproperties.getProperty("ext.generic.workflow.templateName", "InvtWorkflowRoleMember.xls");
		} catch (Throwable throwable)
		{
			System.err.println((new StringBuilder()).append("Error initializing ").append(CLASSNAME).toString());
			throwable.printStackTrace(System.err);
			throw new ExceptionInInitializerError(throwable);
		}
	}

	public String getConceptualClassname()
	{
		return CLASSNAME;
	}

	public static StandardCustService newStandardCustService() throws WTException
	{
		StandardCustService standardCustService = new StandardCustService();
		standardCustService.initialize();
		return standardCustService;
	}

	public NmChangeModel[] setState(NmCommandBean nmcommandbean, String state) throws WTException
	{
		NmOid nmoid = nmcommandbean.getElementOid();
		if (nmoid == null)
			nmoid = nmcommandbean.getPrimaryOid();
		HashMap hashmap = nmcommandbean.getChecked();
		boolean flag = false;
		if (hashmap != null)
		{
			ArrayList arraylist = (ArrayList) hashmap.get("terminateProc");
			if (arraylist != null && arraylist.get(0).equals("terminateProc"))
				flag = true;
		}
		LifeCycleManaged lifecyclemanaged = (LifeCycleManaged) nmoid.getRef();
		LifeCycleHelper.service.setLifeCycleState(lifecyclemanaged, State.toState(state), flag);
		return null;
	}

	

	public String getObjectType(Object object) throws WTException
	{
		String type = "";
		boolean flag = true;
		try
		{
			flag = SessionServerHelper.manager.isAccessEnforced();
			SessionServerHelper.manager.setAccessEnforced(false);

			if (object != null)
			{
				TypeIdentifier ti = TypeIdentifierUtility.getTypeIdentifier(object);
				type = ti.getTypename();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
		return type;
	}


}
