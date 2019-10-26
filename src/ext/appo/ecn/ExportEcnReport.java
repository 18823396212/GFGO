package ext.appo.ecn;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.ptc.windchill.enterprise.change2.commands.RelatedChangesQueryCommands;
import com.ptc.windchill.enterprise.history.HistoryTablesCommands;

import ext.appo.util.PartUtil;
import wt.change2.ChangeOrder2;
import wt.change2.WTChangeOrder2;
import wt.fc.ObjectReference;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTCollection;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;

public class ExportEcnReport {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String userName = null;
		String passWord = null;
		String num = null;

		if (args == null)
			{System.out.println("plase input factor");
			return;
			}
		for (int i = 0; i < args.length; i += 2)
		{
			if (i + 1 < args.length)
			{
				if (("-num".equals(args[i])))
				{
					num = args[i + 1];
				} 
			}
		}

		if (userName == null)
			userName = "wcadmin";
		if (passWord == null)
			passWord = "wcadmin";
		RemoteMethodServer server = RemoteMethodServer.getDefault();
		server.setUserName(userName);
		server.setPassword(passWord);
		Vector ecnvector=null;
		try {
			//ecnvector = getAllEC("");
			WTPart part =PartUtil.getLastestWTPartByNumber(num);
			checkExistEC(part);
			List list =HistoryTablesCommands.versionHistory(part);
			System.out.println("list size====="+list.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//检查pbo是否存在未完成的变更
    public static Boolean checkExistEC(WTObject pbo)
    {
    	Boolean isworkECN=false;
    	try {
    		WTCollection collection= RelatedChangesQueryCommands.getRelatedResultingChangeNotices((WTPart)pbo);
    		if(!collection.isEmpty()){
    		Iterator iterator= collection.iterator();
    		while(iterator.hasNext()){
    			ObjectReference objReference=(ObjectReference)iterator.next();
				WTChangeOrder2 ecn = (WTChangeOrder2)objReference.getObject() ;
				System.out.println("ecn number=="+ecn.getNumber());

					System.out.println("ecn number==="+ecn.getNumber()+"state="+ecn.getState().getState().toString());
					 if(ecn.getState().toString().equalsIgnoreCase("RESOLVED")||
						ecn.getState().toString().equalsIgnoreCase("CANCELLED"))
						 {
						 isworkECN=true;
						 }		
			}

    		}
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return isworkECN;
    }
	//如果为“”，则得到所有的ecn
	public static Vector getAllEC(String number) throws Exception
	{
		QuerySpec qs = new QuerySpec(ChangeOrder2.class);
        if(number==null)
        	return null;
		if (number.trim().length() > 0)
		{
			
			SearchCondition scNumber = new SearchCondition(ChangeOrder2.class, ChangeOrder2.NUMBER, SearchCondition.EQUAL, number.toUpperCase());
			qs.appendWhere(scNumber);
		}

		SearchCondition scLatestIteration = new SearchCondition(ChangeOrder2.class, WTAttributeNameIfc.LATEST_ITERATION, SearchCondition.IS_TRUE);
		
		qs.appendWhere(scLatestIteration);

		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr != null && qr.hasMoreElements())
			qr = (new LatestConfigSpec()).process(qr);

		if (qr != null && qr.hasMoreElements())
			return qr.getObjectVectorIfc().getVector();

		return new Vector();
	}
}
