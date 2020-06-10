//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package ext.generic.wfbaseline.util;

import com.ptc.windchill.enterprise.part.commands.PartDocServiceCommand;
import com.ptc.windchill.uwgm.common.navigate.AssociationTracer;
import com.ptc.windchill.uwgm.common.navigate.AssociationTracer.NavigateModelItems;
import com.ptc.windchill.uwgm.common.navigate.AssociationTracer.Type;
import ext.appo.change.report.ChangeHistoryReport;
import ext.appo.change.util.ModifyUtils;
import ext.appo.ecn.common.util.ChangeUtils;
import ext.com.core.CoreUtil;
import ext.com.iba.IBAUtil;
import ext.generic.wfbaseline.model.WTObjectBaselineLink;
import org.apache.log4j.Logger;
import wt.change2.*;
import wt.doc.WTDocument;
import wt.doc.WTDocumentDependencyLink;
import wt.doc.WTDocumentStandardConfigSpec;
import wt.doc.WTDocumentUsageLink;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentMaster;
import wt.epm.structure.EPMMemberLink;
import wt.epm.structure.EPMReferenceLink;
import wt.epm.structure.EPMStructureHelper;
import wt.fc.*;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTKeyedMap;
import wt.fc.collections.WTList;
import wt.iba.value.IBAHolder;
import wt.inf.container.WTContainer;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.org.DirectoryContextProvider;
import wt.org.OrganizationServicesHelper;
import wt.org.WTUser;
import wt.part.PartDocHelper;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartStandardConfigSpec;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.type.ClientTypedUtility;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.Versioned;
import wt.vc.baseline.Baseline;
import wt.vc.config.ConfigException;
import wt.vc.config.ConfigSpec;
import wt.vc.config.LatestConfigSpec;
import wt.vc.struct.StructHelper;
import wt.vc.views.View;
import wt.vc.views.ViewException;
import wt.vc.views.ViewHelper;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfProcess;

import java.rmi.RemoteException;
import java.util.*;

public class BaselineUtil {
    private static final String CLASSNAME = BaselineUtil.class.getName();
    private static final Logger logger;

    static {
        logger = LogR.getLogger(CLASSNAME);
    }

    public BaselineUtil() {
    }

    public Set<Persistable> getDrwDocAllChildren(Persistable persistable, List<String> ptStateList, Boolean collectStruce) throws WTException {
        Set<Persistable> ptSet = new HashSet();
        if (persistable == null) {
            return ptSet;
        } else {
            if (collectStruce) {
                WTList wtList = new WTArrayList();
                wtList.add(persistable);
                if (persistable instanceof WTDocument) {
                    this.getDrwDocAllChildren(wtList, ptStateList, ptSet, WTDocument.class.getName());
                } else if (persistable instanceof EPMDocument) {
                    this.getDrwDocAllChildren(wtList, ptStateList, ptSet, EPMDocument.class.getName());
                }
            }

            ptSet.add(persistable);
            return ptSet;
        }
    }

    public void getDrwDocAllChildren(WTList parentList, List<String> ptStateList, Set<Persistable> childrenSet, String type) throws WTException {
        if (parentList != null) {
            Set<Persistable> ptSet = this.getDrwDocFirstChildren(parentList, type);
            if (ptSet != null) {
                WTList childList = new WTArrayList();
                Iterator var8 = ptSet.iterator();

                while (true) {
                    while (true) {
                        Persistable persistable;
                        do {
                            if (!var8.hasNext()) {
                                if (childList.size() > 0) {
                                    this.getDrwDocAllChildren(childList, ptStateList, childrenSet, type);
                                }

                                return;
                            }

                            persistable = (Persistable) var8.next();
                            childList.add(persistable);
                        } while (!(persistable instanceof LifeCycleManaged));

                        String ptState = ((LifeCycleManaged) persistable).getLifeCycleState().toString();
                        if (ptStateList != null && ptStateList.size() > 0) {
                            if (ptStateList.contains(ptState)) {
                                childrenSet.add(persistable);
                            }
                        } else {
                            childrenSet.add(persistable);
                        }
                    }
                }
            }
        }
    }

    public Set<Persistable> getDrwDocFirstChildren(WTList parentList, String type) throws WTException {
        Set<Persistable> ptSet = new HashSet();
        if (parentList != null && parentList.size() != 0) {
            try {
                WTKeyedMap keyedMap = null;
                if (WTDocument.class.getName().equals(type)) {
                    keyedMap = StructHelper.service.navigateUsesToIteration(parentList, WTDocumentUsageLink.class, false, new ConfigSpec[]{WTDocumentStandardConfigSpec.newWTDocumentStandardConfigSpec()});
                } else {
                    if (!EPMDocument.class.getName().equals(type)) {
                        return ptSet;
                    }

                    keyedMap = StructHelper.service.navigateUsesToIteration(parentList, EPMMemberLink.class, false, new ConfigSpec[]{new LatestConfigSpec()});
                }

                Iterator iterator = keyedMap.wtKeySet().iterator();

                while (true) {
                    QueryResult qr;
                    do {
                        Object value;
                        do {
                            do {
                                if (!iterator.hasNext()) {
                                    return ptSet;
                                }

                                value = keyedMap.get(iterator.next());
                            } while (value == null);
                        } while (!(value instanceof QueryResult));

                        qr = (QueryResult) value;
                    } while (qr == null);

                    while (qr.hasMoreElements()) {
                        Object nextElement = qr.nextElement();
                        if (nextElement != null && nextElement instanceof Persistable[]) {
                            Persistable[] persistables = (Persistable[]) nextElement;
                            if (persistables[1] != null) {
                                ptSet.add(persistables[1]);
                            }
                        }
                    }
                }
            } catch (ConfigException var10) {
                var10.printStackTrace();
                throw new WTException(var10);
            } catch (WTException var11) {
                var11.printStackTrace();
                throw var11;
            }
        } else {
            return ptSet;
        }
    }

    public Set<Persistable> getAllBomStructures(WTPart part, String partView, List<String> partStateList, Boolean collectStruct) throws WTException {
        Set<Persistable> partSet = new HashSet();
        if (part == null) {
            return partSet;
        } else {
            try {
                if (this.isNull(partView)) {
                    partView = part.getViewName();
                }

                View view = ViewHelper.service.getView(partView);
                if (view == null) {
                    throw new WTException(partView + " View gets empty.");
                } else {
                    WTPartStandardConfigSpec spec = WTPartStandardConfigSpec.newWTPartStandardConfigSpec(view, (State) null);
                    if (collectStruct) {
                        WTList wtList = new WTArrayList();
                        wtList.add(part);
                        this.getAllBomStructures((WTList) wtList, (List) partStateList, (Set) partSet, (WTPartStandardConfigSpec) spec);
                    }

                    partSet.add(part);
                    return partSet;
                }
            } catch (ViewException var9) {
                var9.printStackTrace();
                throw new WTException(var9);
            } catch (WTException var10) {
                var10.printStackTrace();
                throw var10;
            }
        }
    }

    public void getAllBomStructures(WTList wtList, List<String> stateList, Set<Persistable> partSet, WTPartStandardConfigSpec spec) throws WTException {
        if (wtList != null && partSet != null) {
            WTList childPartWTList = new WTArrayList();
            QueryResult qr = this.getPartFirstLink(wtList, spec);

            while (true) {
                while (true) {
                    Persistable childPersistable;
                    do {
                        if (!qr.hasMoreElements()) {
                            if (childPartWTList.size() > 0) {
                                this.getAllBomStructures((WTList) childPartWTList, (List) stateList, (Set) partSet, (WTPartStandardConfigSpec) spec);
                            }

                            return;
                        }

                        childPersistable = ((Persistable[]) qr.nextElement())[1];
                    } while (!(childPersistable instanceof WTPart));

                    //add by lzy at 20200528 start
                    //如果部件在新ECN流程中，取前一个版本物料
                    WTPart part = null;
                    if (childPersistable instanceof WTPart) {
                        part = (WTPart) childPersistable;
                        String mVersion = part.getVersionIdentifier().getValue();//大版本
                        Boolean flag = isRunningNewEcnWorkflowAndAfter(part);
                        if (flag) {
                            // 存在，取前一个版本
                            String englishLetter = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                            if (englishLetter.contains(mVersion)) {
                                int index = englishLetter.indexOf(mVersion);
                                if (index > 0 && index < englishLetter.length()) {
                                    mVersion = englishLetter.substring(index - 1, index);
                                }
                            }
                            part = ChangeHistoryReport.getLatestPart(part.getNumber(), part.getViewName(), mVersion);
                        }
                        childPartWTList.add(part);
                    }else{
                        childPartWTList.add(childPersistable);
                    }
                    if (stateList != null && stateList.size() > 0) {
                        if (part!=null){
                            String state = part.getLifeCycleState().toString();
                            if (stateList.contains(state)) {
                                partSet.add(part);
                            }
                        }else{
                            String state = ((WTPart) childPersistable).getLifeCycleState().toString();
                            if (stateList.contains(state)) {
                                partSet.add(childPersistable);
                            }
                        }
                    } else {
                        if (part!=null){
                            partSet.add(part);
                        }else{
                            partSet.add(childPersistable);
                        }
                    }
                    //add by lzy at 20200528 end
//                    childPartWTList.add(childPersistable);
//                    if (stateList != null && stateList.size() > 0) {
//                        String state = ((WTPart) childPersistable).getLifeCycleState().toString();
//                        if (stateList.contains(state)) {
//                            partSet.add(childPersistable);
//                        }
//                    } else {
//                        partSet.add(childPersistable);
//                    }
                }
            }
        }
    }

    public QueryResult getPartFirstLink(WTList partList, WTPartStandardConfigSpec spec) throws WTException {
        if (partList != null && spec != null) {
            Persistable[][][] persistableArray = WTPartHelper.service.getUsesWTParts(partList, spec);
            Vector<Object> vector = new Vector();

            for (int i = 0; i < persistableArray.length; ++i) {
                Persistable[][] persistables = persistableArray[i];
                if (persistables != null) {
                    for (int j = 0; j < persistables.length; ++j) {
                        vector.add(persistables[j]);
                    }
                }
            }

            return new QueryResult(new ObjectVector(vector));
        } else {
            return new QueryResult();
        }
    }

    public Set<Persistable> getAssociatedDocuments(Collection<Persistable> partCollection) throws WTException {
        if (partCollection != null && partCollection.size() != 0) {
            Set<Persistable> persistableSet = new HashSet();
            WTCollection partWTCollection = new WTArrayList();
            partWTCollection.addAll(partCollection);
            WTKeyedMap wtKeyMap = PartDocHelper.service.getAssociatedDocuments(partWTCollection);
            if (wtKeyMap != null) {
                Iterator iterator = wtKeyMap.keySet().iterator();

                while (iterator.hasNext()) {
                    WTCollection wtCollection = (WTCollection) wtKeyMap.get(iterator.next());
                    Iterator var8 = wtCollection.iterator();

                    while (var8.hasNext()) {
                        Object valueObj = var8.next();
                        if (valueObj instanceof ObjectReference) {
                            ObjectReference or = (ObjectReference) valueObj;
                            valueObj = or.getObject();
                        }

                        if (valueObj instanceof Persistable) {
                            persistableSet.add((Persistable) valueObj);
                        }
                    }
                }
            }

            return persistableSet;
        } else {
            return new HashSet();
        }
    }

    public Set<Persistable> getReferencesEpmRoleAByEpm(Collection<Persistable> epmCollection) throws WTException {
        if (epmCollection != null && epmCollection.size() != 0) {
            Set<Persistable> persistableSet = new HashSet();
            WTArrayList wtArrayList = new WTArrayList();
            wtArrayList.addAll(epmCollection);
            WTKeyedMap wtKeyMap = EPMStructureHelper.service.navigateReferencesToIteration(wtArrayList, (QuerySpec) null, true, new LatestConfigSpec());
            if (wtKeyMap != null) {
                Iterator iterator = wtKeyMap.keySet().iterator();

                while (iterator.hasNext()) {
                    WTCollection wtCollection = (WTCollection) wtKeyMap.get(iterator.next());
                    Iterator var8 = wtCollection.iterator();

                    while (var8.hasNext()) {
                        Object valueObj = var8.next();
                        if (valueObj instanceof ObjectReference) {
                            ObjectReference or = (ObjectReference) valueObj;
                            valueObj = or.getObject();
                        }

                        if (valueObj instanceof Persistable) {
                            persistableSet.add((Persistable) valueObj);
                        }
                    }
                }
            }

            return persistableSet;
        } else {
            return new HashSet();
        }
    }

    public Set<Persistable> getReferencesEpmRoleBByEpm(Collection<Persistable> epmCollection) throws WTException {
        if (epmCollection != null && epmCollection.size() != 0) {
            Set<Persistable> persistableSet = new HashSet();
            WTArrayList wtArrayList = new WTArrayList();
            Iterator var5 = epmCollection.iterator();

            while (var5.hasNext()) {
                Persistable persistable = (Persistable) var5.next();
                if (persistable instanceof EPMDocument) {
                    wtArrayList.add((EPMDocumentMaster) ((EPMDocument) persistable).getMaster());
                }
            }

            QuerySpec qs = new QuerySpec(EPMReferenceLink.class);
            WTKeyedMap wtKeyMap = EPMStructureHelper.service.navigateReferencedBy(wtArrayList, qs, true);
            if (wtKeyMap != null) {
                Iterator iterator = wtKeyMap.keySet().iterator();

                while (iterator.hasNext()) {
                    WTCollection wtCollection = (WTCollection) wtKeyMap.get(iterator.next());
                    Iterator var9 = wtCollection.iterator();

                    while (var9.hasNext()) {
                        Object obj = var9.next();
                        if (obj instanceof ObjectReference) {
                            obj = ((ObjectReference) obj).getObject();
                        }

                        if (obj instanceof EPMDocument) {
                            EPMDocument refDoc = (EPMDocument) obj;
                            EPMDocumentMaster master = (EPMDocumentMaster) refDoc.getMaster();
                            refDoc = CoreUtil.getLatestCADDocByMaster(master);
                            persistableSet.add(refDoc);
                        }
                    }
                }
            }

            return persistableSet;
        } else {
            return new HashSet();
        }
    }

    public Set<Persistable> getAssociatedPartsByEpm(Collection<Persistable> epmCollection) throws WTException {
        if (epmCollection != null && epmCollection.size() != 0) {
            Set<Persistable> persistableSet = new HashSet();
            WTArrayList wtArrayList = new WTArrayList();
            wtArrayList.addAll(epmCollection);
            WTCollection partColl = AssociationTracer.getAssociatedWTParts(wtArrayList, NavigateModelItems.NONE, Type.getAll());
            if (partColl != null) {
                Iterator iterator = partColl.iterator();

                while (iterator.hasNext()) {
                    Object obj = iterator.next();
                    if (obj instanceof ObjectReference) {
                        ObjectReference orf = (ObjectReference) obj;
                        obj = orf.getObject();
                    }

                    if (obj instanceof WTPart) {
                        persistableSet.add((WTPart) obj);
                    }
                }
            }

            return persistableSet;
        } else {
            return new HashSet();
        }
    }

    public Set<Persistable> getAssociatedRfDocuments(Collection<Persistable> docCollection) throws WTException {
        if (docCollection != null && docCollection.size() != 0) {
            Set<Persistable> persistableSet = new HashSet();
            WTCollection wtCollection = new WTArrayList();
            wtCollection.addAll(docCollection);
            WTKeyedMap wtKeyMap = StructHelper.service.navigateDescribedBys(wtCollection, WTDocumentDependencyLink.class, true);
            if (wtKeyMap != null) {
                Iterator iterator = wtKeyMap.keySet().iterator();

                while (iterator.hasNext()) {
                    WTCollection wtColl = (WTCollection) wtKeyMap.get(iterator.next());
                    Iterator var8 = wtColl.iterator();

                    while (var8.hasNext()) {
                        Object valueObj = var8.next();
                        if (valueObj instanceof ObjectReference) {
                            ObjectReference or = (ObjectReference) valueObj;
                            valueObj = or.getObject();
                        }

                        if (valueObj instanceof Persistable) {
                            persistableSet.add((Persistable) valueObj);
                        }
                    }
                }
            }

            return persistableSet;
        } else {
            return new HashSet();
        }
    }

    public Set<Persistable> getAssociatedPartsByDoc(Collection<Persistable> docCollection) throws WTException {
        if (docCollection != null && docCollection.size() != 0) {
            Set<Persistable> persistableSet = new HashSet();
            Iterator var4 = docCollection.iterator();

            while (true) {
                QueryResult qr;
                do {
                    Persistable persistable;
                    do {
                        do {
                            if (!var4.hasNext()) {
                                return persistableSet;
                            }

                            persistable = (Persistable) var4.next();
                        } while (persistable == null);
                    } while (!(persistable instanceof WTDocument));

                    qr = PartDocServiceCommand.getAssociatedParts((WTDocument) persistable);
                } while (qr == null);

                while (qr.hasMoreElements()) {
                    Object obj = qr.nextElement();
                    if (obj instanceof ObjectReference) {
                        ObjectReference orf = (ObjectReference) obj;
                        obj = orf.getObject();
                    }

                    if (obj instanceof WTPart) {
                        persistableSet.add((WTPart) obj);
                    }
                }
            }
        } else {
            return new HashSet();
        }
    }

    public Persistable getPrimaryBusinessObject(WfProcess wfprocess) {
        if (wfprocess == null) {
            return null;
        } else {
            ReferenceFactory rf = new ReferenceFactory();
            WTReference wtreference = wfprocess.getBusinessObjectReference(rf);
            return wtreference != null ? wtreference.getObject() : null;
        }
    }

    public Set<Persistable> getChangeablesAfter(WTChangeOrder2 ecn) throws WTException {
        if (ecn == null) {
            new HashSet();
        }

        HashSet persistableSet = new HashSet();

        try {
            QueryResult afterQr = ChangeHelper2.service.getChangeablesAfter(ecn);

            while (afterQr.hasMoreElements()) {
                Object afterObj = afterQr.nextElement();
                if (afterObj instanceof ObjectReference) {
                    ObjectReference orf = (ObjectReference) afterObj;
                    afterObj = orf.getObject();
                }

                if (afterObj instanceof Persistable) {
                    persistableSet.add((Persistable) afterObj);
                }
            }

            return persistableSet;
        } catch (ChangeException2 var6) {
            var6.printStackTrace();
            throw new WTException(var6);
        } catch (WTException var7) {
            var7.printStackTrace();
            throw var7;
        }
    }

    public String getSoftType(Object obj) throws WTException {
        String internalName = "";
        if (obj != null) {
            internalName = ClientTypedUtility.getTypeIdentifier(obj).getTypename();
            if (internalName != null && internalName.contains("|")) {
                int startIndex = internalName.lastIndexOf("|") + 1;
                internalName = internalName.substring(startIndex);
            }
        }

        return internalName;
    }

    public WTContainer getContainer(String containerName) throws WTException {
        WTContainer wtcontainer = null;

        try {
            QuerySpec queryspec = new QuerySpec(WTContainer.class);
            queryspec.appendSearchCondition(new SearchCondition(WTContainer.class, "containerInfo.name", "=", containerName));
            QueryResult queryresult = PersistenceHelper.manager.find(queryspec);

            while (queryresult.hasMoreElements()) {
                Object obj = queryresult.nextElement();
                if (obj instanceof WTContainer) {
                    wtcontainer = (WTContainer) obj;
                }
            }

            return wtcontainer;
        } catch (QueryException var6) {
            var6.printStackTrace();
            throw new WTException(var6);
        } catch (WTException var7) {
            var7.printStackTrace();
            throw var7;
        }
    }

    public Set<Baseline> getWTObjectBaselineLinks(Persistable persistable) throws WTException {
        Set<Baseline> linkSet = new HashSet();
        if (persistable == null) {
            return linkSet;
        } else {
            QueryResult qr = PersistenceHelper.manager.navigate(persistable, "roleBObject", WTObjectBaselineLink.class, true);

            while (qr.hasMoreElements()) {
                linkSet.add((Baseline) qr.nextElement());
            }

            return linkSet;
        }
    }

    public String getObjectAttributeValue(Persistable persistable, String attributeName) throws WTException {
        logger.debug(">>>Enter .attributeName=" + attributeName);
        String attributeValue = "";
        if (persistable != null && !this.isNull(attributeName)) {
            try {
                if (persistable instanceof WTPart) {
                    WTPart part = (WTPart) persistable;
                    if (!"TopName".equalsIgnoreCase(attributeName) && !"PboName".equalsIgnoreCase(attributeName)) {
                        if (!"TopNumber".equalsIgnoreCase(attributeName) && !"PboNumber".equalsIgnoreCase(attributeName)) {
                            if ("TopView".equalsIgnoreCase(attributeName) || "PboView".equalsIgnoreCase(attributeName)) {
                                attributeValue = part.getViewName();
                            }
                        } else {
                            attributeValue = part.getNumber();
                        }
                    } else {
                        attributeValue = part.getName();
                    }
                } else if (persistable instanceof WTDocument) {
                    WTDocument doc = (WTDocument) persistable;
                    if (!"TopName".equalsIgnoreCase(attributeName) && !"PboName".equalsIgnoreCase(attributeName)) {
                        if ("TopNumber".equalsIgnoreCase(attributeName) || "PboNumber".equalsIgnoreCase(attributeName)) {
                            attributeValue = doc.getNumber();
                        }
                    } else {
                        attributeValue = doc.getName();
                    }
                } else if (persistable instanceof EPMDocument) {
                    EPMDocument epm = (EPMDocument) persistable;
                    if (!"TopName".equalsIgnoreCase(attributeName) && !"PboName".equalsIgnoreCase(attributeName)) {
                        if ("TopNumber".equalsIgnoreCase(attributeName) || "PboNumber".equalsIgnoreCase(attributeName)) {
                            attributeValue = epm.getNumber();
                        }
                    } else {
                        attributeValue = epm.getName();
                    }
                } else if (persistable instanceof WTChangeOrder2) {
                    WTChangeOrder2 ecn = (WTChangeOrder2) persistable;
                    if (!"TopName".equalsIgnoreCase(attributeName) && !"PboName".equalsIgnoreCase(attributeName)) {
                        if ("TopNumber".equalsIgnoreCase(attributeName) || "PboNumber".equalsIgnoreCase(attributeName)) {
                            attributeValue = ecn.getNumber();
                        }
                    } else {
                        attributeValue = ecn.getName();
                    }
                }

                if (("TopVersion".equalsIgnoreCase(attributeName) || "PboVersion".equalsIgnoreCase(attributeName)) && persistable instanceof Versioned) {
                    Versioned versionObj = (Versioned) persistable;
                    attributeValue = VersionControlHelper.getIterationDisplayIdentifier(versionObj).toString();
                }

                if (this.isNull(attributeValue) && persistable instanceof IBAHolder) {
                    Object objValue = IBAUtil.getIBAValue((IBAHolder) persistable, attributeName);
                    if (objValue != null && !"".equals(objValue)) {
                        attributeValue = String.valueOf(objValue);
                    } else {
                        attributeValue = attributeName;
                    }
                }
            } catch (RemoteException var5) {
                var5.printStackTrace();
                throw new WTException(var5);
            } catch (WTException var6) {
                var6.printStackTrace();
                throw var6;
            }

            logger.debug("<<<Exit .attributeValue=" + attributeValue);
            return attributeValue;
        } else {
            return attributeValue;
        }
    }

    public WTUser getUserByName(String userName) throws WTException {
        if (this.isNull(userName)) {
            return null;
        } else {
            DirectoryContextProvider dcp = OrganizationServicesHelper.manager.newDirectoryContextProvider((String[]) null, (String[]) null);
            Enumeration userEnm = OrganizationServicesHelper.manager.findLikeUsers("authenticationName", userName, dcp);
            if (userEnm == null || !userEnm.hasMoreElements()) {
                userEnm = OrganizationServicesHelper.manager.findLikeUsers("fullName", userName, dcp);
                if (userEnm == null || !userEnm.hasMoreElements()) {
                    userEnm = OrganizationServicesHelper.manager.findLikeUsers("eMail", userName, dcp);
                }
            }

            return userEnm != null && userEnm.hasMoreElements() && userEnm.hasMoreElements() ? (WTUser) userEnm.nextElement() : null;
        }
    }

    public Boolean isNull(String parameter) {
        return parameter != null && !parameter.trim().isEmpty() ? false : true;
    }

    /**
     * add by lzy
     * 判断pbo是否在新ECN流程APPO_ECNWF产生对象中且流程未结束
     *
     * @param pbo
     * @return
     * @throws WTException
     */
    private static Boolean isRunningNewEcnWorkflowAndAfter(WTPart pbo) throws WTException {
        if (pbo == null) return false;
        String number = pbo.getNumber();
        String view = pbo.getViewName();
        String mVersion = pbo.getVersionIdentifier().getValue();//物料大版本
        // 传前一个版本，如果为A版不发
        String EnglishLetter = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String strsub = mVersion;
        if (EnglishLetter.contains(mVersion)) {
            int index = EnglishLetter.indexOf(mVersion);
            if (index > 0) {
                strsub = EnglishLetter.substring(index - 1, index);
            }
        }
        WTPart wtPart = ChangeHistoryReport.getLatestPart( number , view , strsub);//获取取前一个版本
        WTChangeOrder2 changeOrder2 = null;
        //获取对象所有关联的ECA对象
        QueryResult result = ChangeHelper2.service.getAffectingChangeActivities(wtPart);
        while (result.hasMoreElements()) {
            WTChangeActivity2 changeActivity2 = (WTChangeActivity2) result.nextElement();
            //获取产生对象
            Collection<Changeable2> collection = ModifyUtils.getChangeablesAfter(changeActivity2);
            for (Changeable2 changeable2 : collection) {
                if (changeable2 instanceof WTPart) {
                    WTPart part = (WTPart) changeable2;
                    String partNumber = part.getNumber();
                    String partVersion = part.getVersionIdentifier().getValue();//物料大版本
                    String partView = part.getViewName();
                    if (number.equals(partNumber) && view.equals(partView) && mVersion.equals(partVersion)) {
                        changeOrder2 = ChangeUtils.getEcnByEca(changeActivity2);
                    }
                }
            }
        }
        if (changeOrder2!=null){
            //ECN进程
            QueryResult qr = WfEngineHelper.service.getAssociatedProcesses(changeOrder2, null, null);
            while (qr.hasMoreElements()) {
                WfProcess process = (WfProcess) qr.nextElement();
                String templateName = process.getTemplate().getName();
                String state = String.valueOf(changeOrder2.getLifeCycleState());
                //不是已取消、已解决的新ECN流程APPO_ECNWF
                if (templateName.equals("APPO_ECNWF")) {
                    if (!state.equals("CANCELLED") && !state.equals("RESOLVED")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
