package ext.appo.integration.erp.servers;

import java.io.Serializable;

import wt.method.RemoteAccess;

import com.infoengine.object.factory.Group;

public class ErpQueryBomInfoHelp implements Serializable, RemoteAccess{
	
	private static final long serialVersionUID = 1L;

	public static Group queryBomInfo(String baselineNumber){
		ErpQueryBomInfoWSService service = new ErpQueryBomInfoWSService() ;
		return service.queryBomInfo(baselineNumber) ;
	}
}
