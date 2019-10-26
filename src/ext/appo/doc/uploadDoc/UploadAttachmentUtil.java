package ext.appo.doc.uploadDoc;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.ptc.core.lwc.common.view.TypeDefinitionReadView;
import com.ptc.core.lwc.server.TypeDefinitionServiceHelper;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.common.impl.TypeIdentifierUtilityHelper;
import com.ptc.core.meta.server.TypeIdentifierUtility;

import wt.doc.DocumentVersion;
import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.fc.ObjectVector;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTKeyedMap;
import wt.part.PartDocHelper;
import wt.part.WTPart;
import wt.part.WTPartDescribeLink;
import wt.part.WTPartReferenceLink;
import wt.type.Typed;
import wt.util.WTException;
import wt.vc.config.ConfigHelper;
import wt.vc.config.LatestConfigSpec;

public class UploadAttachmentUtil {

	/**
     * 根据部件得到其关联的参考文档
     * 
     * @param wtpart
     * @return
     * @throws WTException
     */
    public static Set<WTDocument> getReferenceLink(WTPart part) throws WTException {
    	Set<WTDocument> set = new HashSet<WTDocument>();
        
        QueryResult qr = PersistenceHelper.manager.navigate(part, WTPartReferenceLink.REFERENCES_ROLE, WTPartReferenceLink.class, false);
        while (qr.hasMoreElements()) {
            Object wtobject = (Object) qr.nextElement();
            if (wtobject != null && wtobject instanceof WTPartReferenceLink) {
                WTPartReferenceLink reflink = (WTPartReferenceLink) wtobject;
                WTDocumentMaster theMaster = (WTDocumentMaster) reflink.getReferences();

                QueryResult queryresult = ConfigHelper.service.filteredIterationsOf(theMaster, new LatestConfigSpec());
                while(queryresult.hasMoreElements()) {
                	WTDocument theDocument = (WTDocument) queryresult.nextElement();
                    set.add(theDocument);
                	}
                }
            }

        return set;
    }
    
    public static QueryResult getAssociatedDescribeDocuments(WTPart wtpart)
			throws WTException {
		WTArrayList wtarraylist = new WTArrayList();
		wtarraylist.add(wtpart);
		WTKeyedMap wtkeyedmap = PartDocHelper.service
				.getAssociatedDescribeDocuments(wtarraylist);
		WTCollection wtcollection = (WTCollection) wtkeyedmap.get(wtpart);
		return getDocs(wtcollection);
	}
	private static QueryResult getDocs(WTCollection wtcollection) {
		QueryResult queryresult = new QueryResult();
		try {
			if (wtcollection != null) {
				ObjectVector objectvector = new ObjectVector();
				DocumentVersion documentversion;
				for (Iterator iterator = wtcollection.persistableIterator(); iterator
						.hasNext(); objectvector.addElement(documentversion))
					documentversion = (DocumentVersion) iterator.next();

				queryresult.appendObjectVector(objectvector);
			}
		} catch (WTException wtexception) {
			wtexception.printStackTrace();
		}
		return queryresult;
	}
    
    
    /**
     * 根据部件得到其关联的说明文档
     * 
     * @param wtpart
     * @return
     * @throws WTException
     */
    public static Set<WTDocument> getDescribeLink(WTPart part) throws WTException {
    	Set<WTDocument> set = new HashSet<WTDocument>();
    	
    	QueryResult qr = PersistenceHelper.manager.navigate(part, WTPartDescribeLink.DESCRIBED_BY_ROLE, WTPartDescribeLink.class, false);
        while (qr.hasMoreElements()) {
            Object wtobject = (Object) qr.nextElement();
            if (wtobject != null && wtobject instanceof WTPartDescribeLink) {
            	WTPartDescribeLink reflink = (WTPartDescribeLink) wtobject;
            	WTDocument theDocument = (WTDocument) reflink.getRoleBObject();
                 set.add(theDocument);
  
                }
            }

        return set;
    }
    
    /**
	 * param str 要分割的字符串
	 * return ary[ary.length-1] 返回最后一个元数
	 * @author wuzhitao
	 */
	public static String getStrSplit(Persistable p) {
		
		String str = TypeIdentifierUtility.getTypeIdentifier(p).getTypename();
		
		if (str != null) {
			return str.substring(str.lastIndexOf("|") + 1, str.length());
		}
		return "";
	}
	
	
	/*
	 * Typed 可类型管理的对象，如文档，图纸，部件
	 *
	 * @return 获得类型的内部名称，如：wt.doc.WTDocument
	 */
    public static String getTypeInternalName(Typed typed_object) throws WTException {
        String name = null;
        try {
            TypeIdentifier type = TypeIdentifierUtilityHelper.service.getTypeIdentifier(typed_object);
            TypeDefinitionReadView trv = TypeDefinitionServiceHelper.service.getTypeDefView(type);
            name = trv.getName();
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e);
        }
        return name;
    }
	
	
}
