package ext.appo.service;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import wt.content.ApplicationData;
import wt.content.ContentHolder;
import wt.doc.WTDocument;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.services.Manager;
import wt.services.ManagerServiceFactory;
import wt.util.WTException;

public class LoadServiceFwd implements RemoteAccess, LoadService, Serializable
{

	public LoadServiceFwd()
	{
	}

	static final boolean SERVER;
	private static final String CLASSNAME = LoadServiceFwd.class.getName();
	static
	{
		SERVER = RemoteMethodServer.ServerFlag;
	}
	private static Manager getManager() throws WTException
	{
		Manager manager = ManagerServiceFactory.getDefault().getManager(LoadService.class);
		if (manager == null)
		{
			Object aobj[] = { LoadService.class.getName() };
			throw new WTException("wt.fc.fcResource", "40", aobj);
		} else
		{
			return manager;
		}
	}
	public  ApplicationData getPrimaryContent(WTDocument wtdocument) throws WTException, IOException
	{
		if (SERVER)
			return ((LoadService) getManager()).getPrimaryContent(wtdocument);
		try
		{
			Class aclass[] = { wt.doc.WTDocument.class};
			Object aobj[] = {wtdocument };
			return (ApplicationData) (ApplicationData) RemoteMethodServer.getDefault().invoke("getPrimaryContent", null, this, aclass, aobj);
		} catch (InvocationTargetException invocationtargetexception)
		{
			Throwable throwable = invocationtargetexception.getTargetException();
			if (throwable instanceof WTException)
			{
				throw (WTException) throwable;
			} else
			{
				Object aobj2[] = { "getPrimaryContent" };
				throw new WTException(throwable, "wt.fc.fcResource", "0", aobj2);
			}
		} catch (RemoteException remoteexception)
		{
			Object aobj1[] = { "getPrimaryContent" };
			throw new WTException(remoteexception, "wt.fc.fcResource", "0", aobj1);
		}
	}
	
	
	public  Boolean ChangePrimaryContentName(ContentHolder object,String fileName) throws WTException, Exception
	{
		if (SERVER)
			return ((LoadService) getManager()).ChangePrimaryContentName(object,fileName);
		try
		{
			Class aclass[] = {wt.content.ContentHolder.class, java.lang.String.class};
			Object aobj[] = { object, fileName };
			return (Boolean) (Boolean) RemoteMethodServer.getDefault().invoke("ChangePrimaryContentName", null, this, aclass, aobj);
		} catch (InvocationTargetException invocationtargetexception)
		{
			Throwable throwable = invocationtargetexception.getTargetException();
			if (throwable instanceof WTException)
			{
				throw (WTException) throwable;
			} else
			{
				Object aobj2[] = { "ChangePrimaryContentName" };
				throw new WTException(throwable, "wt.fc.fcResource", "0", aobj2);
			}
		} catch (RemoteException remoteexception)
		{
			Object aobj1[] = { "ChangePrimaryContentName" };
			throw new WTException(remoteexception, "wt.fc.fcResource", "0", aobj1);
		}
	}

	
}
