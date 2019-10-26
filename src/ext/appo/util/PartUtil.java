package ext.appo.util;

import org.apache.log4j.Logger;

import wt.enterprise.RevisionControlled;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTReference;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.vc.Mastered;
import wt.vc.VersionControlHelper;
import wt.vc.Versioned;
import wt.vc.config.LatestConfigSpec;

public class PartUtil {
	private static Logger log=Logger.getLogger(PartUtil.class.getName());
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static boolean existGreaterVersion(RevisionControlled obj) throws WTException{
		Mastered master = obj.getMaster();
		RevisionControlled lastestVersion = getLatestVersion(master);
		if(lastestVersion.getVersionIdentifier().getSeries().greaterThan(obj.getVersionIdentifier().getSeries())){
			return true;
		}
		
		return false;
	}
	public static RevisionControlled getLatestVersion(Mastered master ) throws WTException{
		
		QueryResult queryResult = VersionControlHelper.service.allVersionsOf(master);
		
		RevisionControlled result = null;
		while (queryResult.hasMoreElements())
		{
			RevisionControlled obj = ((RevisionControlled) queryResult.nextElement());
			//比较获取最大版本的
			if (result == null || obj.getVersionIdentifier().getSeries().greaterThan(result.getVersionIdentifier().getSeries()))
			{
				result = obj;
			}
		}
		result =  (RevisionControlled) VersionControlHelper.service.getLatestIteration(result,false);//false代表不查标志为删除的对象
		
		return result;
	}
    /**
     * 获取对象上一版本
     * 
     * @param currentVersion
     * @return
     * @throws WTException
     */
    public static Versioned getPreviousVersion(Versioned currentVersion) throws WTException {
        log.debug("进入获取对象上一版本，返回Versioned getPreviousVersion");
        Versioned previousVersion = null;
        try {
            QueryResult allVersions = wt.vc.VersionControlHelper.service.allVersionsOf((Versioned) currentVersion);
            if (allVersions.size() <= 1)
                return null;
            Versioned latestVersion = (Versioned) allVersions.nextElement(); // latest
                                                                             // version
            previousVersion = (Versioned) allVersions.nextElement(); // previous
                                                                     // version
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return previousVersion;
    }
	public static WTPart getLastestWTPartByNumber(String numStr) {
		try {
			QuerySpec queryspec = new QuerySpec(WTPart.class);

			queryspec.appendSearchCondition(new SearchCondition(WTPart.class,
					WTPart.NUMBER, SearchCondition.EQUAL, numStr));
			QueryResult queryresult = PersistenceHelper.manager.find(queryspec);
			LatestConfigSpec cfg = new LatestConfigSpec();
			QueryResult qr = cfg.process(queryresult);
			if (qr.hasMoreElements()) {
				return (WTPart) qr.nextElement();
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static Persistable getPersistableByOid(String oid) {
		Persistable obj = null;
		try {
			ReferenceFactory referencefactory = new ReferenceFactory();
			WTReference wtreference = referencefactory.getReference(oid);
			if (wtreference.getObject() != null) {
				obj = wtreference.getObject();
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return obj;
	}
	
}
