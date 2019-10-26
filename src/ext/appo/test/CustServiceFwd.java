package ext.appo.test;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.ptc.netmarkets.model.NmChangeModel;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.table.NmHTMLTable;

import wt.change2.WTChangeRequest2;
import wt.content.ApplicationData;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.inf.container.WTContainer;
import wt.inf.team.ContainerTeamManaged;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTPrincipal;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.projmgmt.admin.Project2;
import wt.services.Manager;
import wt.services.ManagerServiceFactory;
import wt.team.WTRoleHolder2;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.wip.Workable;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfProcess;

public class CustServiceFwd implements RemoteAccess, CustService, Serializable
{

	public CustServiceFwd()
	{
	}
	
	static final boolean SERVER;
	private static final String CLASSNAME = CustServiceFwd.class.getName();
	static
	{
		SERVER = RemoteMethodServer.ServerFlag;
	}

	
	private static Manager getManager() throws WTException
	{
		Manager manager = ManagerServiceFactory.getDefault().getManager(ext.appo.test.CustService.class);
		if (manager == null)
		{
			Object aobj[] = { CustService.class.getName() };
			throw new WTException("wt.fc.fcResource", "40", aobj);
		} else
		{
			return manager;
		}
	}


	public String getObjectType(Object object) throws WTException
	{
		if (SERVER)
            return ((CustService)getManager()).getObjectType(object);
		else
			try
			{
				Class aclass[] = { java.lang.Object.class };
				Object aobj[] = { object };
	            return (String)(String)RemoteMethodServer.getDefault().invoke("getObjectType", null, this, aclass, aobj);
			} catch (InvocationTargetException invocationtargetexception)
			{
				Throwable throwable = invocationtargetexception.getTargetException();
				if (throwable instanceof WTException)
				{
					throw (WTException) throwable;
				} else
				{
					Object aobj2[] = { "getObjectType" };
					throw new WTException(throwable, "wt.fc.fcResource", "0", aobj2);
				}
			} catch (RemoteException remoteexception)
			{
				Object aobj1[] = { "getObjectType" };
				throw new WTException(remoteexception, "wt.fc.fcResource", "0", aobj1);
			}
	}

	

}
