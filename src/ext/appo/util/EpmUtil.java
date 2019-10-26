package ext.appo.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import wt.build.BuildHistory;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentMaster;
import wt.epm.build.EPMBuildRule;
import wt.epm.structure.EPMStructureHelper;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.part.WTPart;
import wt.util.WTException;
import wt.vc.VersionControlHelper;

public class EpmUtil {

    public static Collection getRelatedParts(EPMDocument source) throws WTException {

        QueryResult result = null;
        if (!VersionControlHelper.isLatestIteration(source)) {
            result = PersistenceHelper.manager.navigate(source, BuildHistory.BUILT_ROLE, BuildHistory.class);
        } else {
            result = PersistenceHelper.manager.navigate(source, EPMBuildRule.BUILD_TARGET_ROLE, EPMBuildRule.class);
        }
        if (result != null) {
            return result.getObjectVectorIfc().getVector();
        } else {
            return new ArrayList();
        }
    }

    public static Collection getRelatedEpmdoc(WTPart part) throws WTException {

        QueryResult result = null;
        if (!VersionControlHelper.isLatestIteration(part)) {
            result = PersistenceHelper.manager.navigate(part, BuildHistory.BUILT_BY_ROLE, BuildHistory.class);
        } else {
            result = PersistenceHelper.manager.navigate(part, EPMBuildRule.BUILD_SOURCE_ROLE, EPMBuildRule.class);
        }
        if (result != null) {
            return result.getObjectVectorIfc().getVector();
        } else {
            return new ArrayList();
        }
    }
    public static Collection<EPMDocument> getDrawings(EPMDocument epmDocument) throws WTException {

        List<EPMDocument> uniqueReferencedDocs = new ArrayList<EPMDocument>();
        QueryResult referencedObjects = EPMStructureHelper.service.navigateReferencedBy((EPMDocumentMaster) epmDocument.getMaster(), null, true);
        while (referencedObjects.hasMoreElements()) {
            Object refObject = referencedObjects.nextElement();
            if (refObject instanceof EPMDocument) {
                EPMDocument referencedDoc = (EPMDocument) refObject;
                if (referencedDoc.getDocType().toString().equals("CADDRAWING")) {
                    EPMDocument drawing = (EPMDocument) VersionControlHelper.getLatestIteration(referencedDoc, false);
                    drawing = (EPMDocument) PersistenceHelper.manager.refresh(drawing);
                    if (!uniqueReferencedDocs.contains(drawing)) {
                        uniqueReferencedDocs.add(drawing);
                    }
                }
            }
        }
        return uniqueReferencedDocs;
    }
}
