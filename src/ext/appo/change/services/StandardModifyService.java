package ext.appo.change.services;

import ext.appo.change.interfaces.ModifyService;
import ext.appo.change.models.CorrelationObjectLink;
import ext.appo.change.models.ManageTeamTemplate;
import ext.appo.change.models.ManageTeamTemplateShow;
import ext.appo.change.models.TransactionTask;
import ext.com.workflow.WorkflowUtil;
import ext.generic.reviewprincipal.model.PersonalTeamTemplate;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.*;
import wt.log4j.LogR;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.services.StandardManager;
import wt.util.WTException;
import wt.vc.Iterated;
import wt.vc.VersionControlHelper;
import wt.workflow.engine.WfProcess;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StandardModifyService extends StandardManager implements ModifyService {

    private static final Logger LOGGER = LogR.getLogger(StandardModifyService.class.getName());

    public static StandardModifyService newStandardModifyService() throws Exception {
        StandardModifyService instance = new StandardModifyService();
        instance.initialize();
        return instance;
    }

    @Override
    public TransactionTask newTransactionTask(String changeTheme, String changeDescribe, String responsible, String needDate, WTChangeActivity2 changeActivity2) throws WTException {
        TransactionTask task = TransactionTask.newTransactionTask();
        try {
            task.setChangeTheme(changeTheme);
            task.setChangeDescribe(changeDescribe);
            task.setResponsible(responsible);
            task.setNeedDate(needDate);
            if (changeActivity2 != null)
                task.setChangeActivity2(PersistenceHelper.getObjectIdentifier(changeActivity2).toString());
            PersistenceServerHelper.manager.insert(task);
            task = (TransactionTask) PersistenceHelper.manager.refresh(task);
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
        return task;
    }

    @Override
    public TransactionTask newTransactionTask(String changeTheme, String changeDescribe, String responsible, String needDate, WTChangeActivity2 changeActivity2, String taskType, String clfs) throws WTException {
        TransactionTask task = TransactionTask.newTransactionTask();
        try {
            task.setChangeTheme(changeTheme);
            task.setChangeDescribe(changeDescribe);
            task.setResponsible(responsible);
            task.setNeedDate(needDate);
            task.setTaskType(taskType);
            task.setManagementStyle(clfs);
            if (changeActivity2 != null)
                task.setChangeActivity2(PersistenceHelper.getObjectIdentifier(changeActivity2).toString());
            PersistenceServerHelper.manager.insert(task);
            task = (TransactionTask) PersistenceHelper.manager.refresh(task);
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
        return task;
    }

    @Override
    public CorrelationObjectLink newCorrelationObjectLink(WTChangeOrder2 changeOrder2, Persistable persistable, String linkType, String ecnBranchIdentifier, String perBranchIdentifier) throws WTException {
        CorrelationObjectLink link = CorrelationObjectLink.newCorrelationObjectLink(changeOrder2, persistable);
        try {
            link.setLinkType(linkType);
            link.setEcnBranchIdentifier(ecnBranchIdentifier);
            link.setPerBranchIdentifier(perBranchIdentifier);
            PersistenceServerHelper.manager.insert(link);
            link = (CorrelationObjectLink) PersistenceHelper.manager.refresh(link);
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
        return link;
    }

    @Override
    public CorrelationObjectLink newCorrelationObjectLink(WTChangeOrder2 changeOrder2, Persistable persistable, String linkType, String ecnBranchIdentifier, String perBranchIdentifier, String treatment) throws WTException {
        CorrelationObjectLink link = CorrelationObjectLink.newCorrelationObjectLink(changeOrder2, persistable);
        try {
            link.setLinkType(linkType);
            link.setEcnBranchIdentifier(ecnBranchIdentifier);
            link.setPerBranchIdentifier(perBranchIdentifier);
            link.setTreatment(treatment);
            PersistenceServerHelper.manager.insert(link);
            link = (CorrelationObjectLink) PersistenceHelper.manager.refresh(link);
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
        return link;
    }

    @Override
    public CorrelationObjectLink newCorrelationObjectLink(WTChangeOrder2 changeOrder2, Persistable persistable, String linkType, String ecnBranchIdentifier, String perBranchIdentifier, String ecaIdentifier, String aadDescription, String routing) throws WTException {
        CorrelationObjectLink link = CorrelationObjectLink.newCorrelationObjectLink(changeOrder2, persistable);
        try {
            link.setLinkType(linkType);
            link.setEcnBranchIdentifier(ecnBranchIdentifier);
            link.setPerBranchIdentifier(perBranchIdentifier);
            link.setEcaIdentifier(ecaIdentifier);
            link.setAadDescription(aadDescription);
            link.setRouting(routing);
            PersistenceServerHelper.manager.insert(link);
            link = (CorrelationObjectLink) PersistenceHelper.manager.refresh(link);
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
        return link;
    }

    @Override
    public void deleteTransactionTask(TransactionTask task) throws WTException {
        if (task != null) PersistenceServerHelper.manager.remove(task);
    }

    @Override
    public void removeCorrelationObjectLink(CorrelationObjectLink link) throws WTException {
        if (link != null) PersistenceServerHelper.manager.remove(link);
    }

    @Override
    public void removeCorrelationObjectLink(WTChangeOrder2 changeOrder2, Persistable persistable) throws WTException {
        CorrelationObjectLink link = queryCorrelationObjectLink(changeOrder2, persistable);
        if (null != link) PersistenceServerHelper.manager.remove(link);
    }

    @Override
    public TransactionTask updateTransactionTask(TransactionTask task, String changeTheme, String changeDescribe, String responsible, String needDate) throws WTException {
        if (task != null) {
            try {
                task.setChangeTheme(changeTheme);
                task.setChangeDescribe(changeDescribe);
                task.setResponsible(responsible);
                task.setNeedDate(needDate);
                PersistenceServerHelper.manager.update(task);
                task = (TransactionTask) PersistenceHelper.manager.refresh(task);
            } catch (Exception e) {
                throw new WTException(e.getStackTrace());
            }
        }
        return task;
    }

    @Override
    public TransactionTask updateTransactionTask(TransactionTask task, String changeTheme, String changeDescribe, String responsible, String needDate, String taskType, String clfs) throws WTException {
        if (task != null) {
            try {
                task.setChangeTheme(changeTheme);
                task.setChangeDescribe(changeDescribe);
                task.setResponsible(responsible);
                task.setNeedDate(needDate);
                task.setTaskType(taskType);
                task.setManagementStyle(clfs);
                PersistenceServerHelper.manager.update(task);
                task = (TransactionTask) PersistenceHelper.manager.refresh(task);
            } catch (Exception e) {
                throw new WTException(e.getStackTrace());
            }
        }
        return task;
    }

    @Override
    public TransactionTask updateTransactionTask(TransactionTask task, String changeActivity2) throws WTException {
        if (task != null) {
            try {
                task.setChangeActivity2(changeActivity2);
                PersistenceServerHelper.manager.update(task);
                task = (TransactionTask) PersistenceHelper.manager.refresh(task);
            } catch (Exception e) {
                throw new WTException(e.getStackTrace());
            }
        }
        return task;
    }

    @Override
    public void updateCorrelationObjectLink(String ecnBranchIdentifier, String perBranchIdentifier, String linkType) throws WTException {
        CorrelationObjectLink link = queryCorrelationObjectLink(ecnBranchIdentifier, perBranchIdentifier, linkType);
        try {
            if (link != null) {
                WTChangeOrder2 changeOrder2 = link.getChangeOrder2();
                LOGGER.info("=====updateCorrelationObjectLink.changeOrder2: " + changeOrder2);
                changeOrder2 = (WTChangeOrder2) getLatestVersion(changeOrder2);
                LOGGER.info("=====updateCorrelationObjectLink.changeOrder2: " + changeOrder2);
                link.setChangeOrder2(changeOrder2);

                Persistable persistable = link.getPersistable();
                LOGGER.info("=====updateCorrelationObjectLink.persistable: " + persistable);

                if (!(persistable instanceof TransactionTask)) persistable = getLatestVersion((Iterated) persistable);
                LOGGER.info("=====updateCorrelationObjectLink.persistable: " + persistable);
                link.setPersistable(persistable);

                PersistenceServerHelper.manager.update(link);
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
    }

    @Override
    public void updateCorrelationObjectLink(String ecnBranchIdentifier, String perBranchIdentifier, String linkType, String treatment) throws WTException {
        CorrelationObjectLink link = queryCorrelationObjectLink(ecnBranchIdentifier, perBranchIdentifier, linkType);
        try {
            if (link != null) {
                WTChangeOrder2 changeOrder2 = link.getChangeOrder2();
                LOGGER.info("=====updateCorrelationObjectLink.changeOrder2: " + changeOrder2);
                changeOrder2 = (WTChangeOrder2) getLatestVersion(changeOrder2);
                LOGGER.info("=====updateCorrelationObjectLink.changeOrder2: " + changeOrder2);
                link.setChangeOrder2(changeOrder2);
                link.setTreatment(treatment);
                Persistable persistable = link.getPersistable();
                LOGGER.info("=====updateCorrelationObjectLink.persistable: " + persistable);

                if (!(persistable instanceof TransactionTask)) persistable = getLatestVersion((Iterated) persistable);
                LOGGER.info("=====updateCorrelationObjectLink.persistable: " + persistable);
                link.setPersistable(persistable);

                PersistenceServerHelper.manager.update(link);
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
    }

    @Override
    public CorrelationObjectLink updateCorrelationObjectLink(CorrelationObjectLink link, String aadDescription, String routing) throws WTException {
        try {
            if (link != null) {
                WTChangeOrder2 changeOrder2 = link.getChangeOrder2();
                LOGGER.info("=====updateCorrelationObjectLink.changeOrder2: " + changeOrder2);
                changeOrder2 = (WTChangeOrder2) getLatestVersion(changeOrder2);
                LOGGER.info("=====updateCorrelationObjectLink.changeOrder2: " + changeOrder2);
                link.setChangeOrder2(changeOrder2);

                Persistable persistable = link.getPersistable();
                LOGGER.info("=====updateCorrelationObjectLink.persistable: " + persistable);
                if (!(persistable instanceof TransactionTask)) persistable = getLatestVersion((Iterated) persistable);
                LOGGER.info("=====updateCorrelationObjectLink.persistable: " + persistable);
                link.setPersistable(persistable);

                link.setAadDescription(aadDescription);
                link.setRouting(routing);
                PersistenceServerHelper.manager.update(link);
                link = (CorrelationObjectLink) PersistenceHelper.manager.refresh(link);
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
        return link;
    }

    @Override
    public CorrelationObjectLink updateCorrelationObjectLink(CorrelationObjectLink link, String ecaIdentifier, String aadDescription, String routing) throws WTException {
        try {
            if (link != null) {
                WTChangeOrder2 changeOrder2 = link.getChangeOrder2();
                LOGGER.info("=====updateCorrelationObjectLink.changeOrder2: " + changeOrder2);
                changeOrder2 = (WTChangeOrder2) getLatestVersion(changeOrder2);
                LOGGER.info("=====updateCorrelationObjectLink.changeOrder2: " + changeOrder2);
                link.setChangeOrder2(changeOrder2);

                Persistable persistable = link.getPersistable();
                LOGGER.info("=====updateCorrelationObjectLink.persistable: " + persistable);
                if (!(persistable instanceof TransactionTask)) persistable = getLatestVersion((Iterated) persistable);
                LOGGER.info("=====updateCorrelationObjectLink.persistable: " + persistable);
                link.setPersistable(persistable);

                link.setEcaIdentifier(ecaIdentifier);
                link.setAadDescription(aadDescription);
                link.setRouting(routing);
                PersistenceServerHelper.manager.update(link);
                link = (CorrelationObjectLink) PersistenceHelper.manager.refresh(link);
            }
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
        return link;
    }

    @Override
    public CorrelationObjectLink queryCorrelationObjectLink(WTChangeOrder2 changeOrder2, Persistable persistable) throws WTException {
        if (changeOrder2 != null && persistable != null) {
            QuerySpec qs = new QuerySpec(CorrelationObjectLink.class);

            SearchCondition sc = new SearchCondition(CorrelationObjectLink.class, "roleAObjectRef.key.id", SearchCondition.EQUAL, changeOrder2.getPersistInfo().getObjectIdentifier().getId());
            qs.appendWhere(sc, new int[]{0});

            qs.appendAnd();
            sc = new SearchCondition(CorrelationObjectLink.class, "roleBObjectRef.key.id", SearchCondition.EQUAL, persistable.getPersistInfo().getObjectIdentifier().getId());
            qs.appendWhere(sc, new int[]{0});

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("=====queryCorrelationObjectLink.sql=" + qs);
            }

            QueryResult result = PersistenceHelper.manager.find(qs);
            if (result.hasMoreElements()) {
                return (CorrelationObjectLink) result.nextElement();
            }
        }
        return null;
    }

    @Override
    public CorrelationObjectLink queryCorrelationObjectLink(WTChangeOrder2 changeOrder2, String perBranchIdentifier) throws WTException {
        if (changeOrder2 != null && StringUtils.isNotEmpty(perBranchIdentifier)) {
            QuerySpec qs = new QuerySpec(CorrelationObjectLink.class);

            SearchCondition sc = new SearchCondition(CorrelationObjectLink.class, "roleAObjectRef.key.id", SearchCondition.EQUAL, changeOrder2.getPersistInfo().getObjectIdentifier().getId());
            qs.appendWhere(sc, new int[]{0});

            qs.appendAnd();
            sc = new SearchCondition(CorrelationObjectLink.class, CorrelationObjectLink.PER_BRANCH_IDENTIFIER, SearchCondition.EQUAL, perBranchIdentifier);
            qs.appendWhere(sc, new int[]{0});

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("=====queryCorrelationObjectLink.sql=" + qs);
            }

            QueryResult result = PersistenceHelper.manager.find(qs);
            if (result.hasMoreElements()) {
                return (CorrelationObjectLink) result.nextElement();
            }
        }
        return null;
    }

    @Override
    public CorrelationObjectLink queryCorrelationObjectLink(String ecnBranchIdentifier, String perBranchIdentifier, String linkType) throws WTException {
        if (StringUtils.isNotEmpty(ecnBranchIdentifier) && StringUtils.isNotEmpty(perBranchIdentifier)) {
            QuerySpec qs = new QuerySpec(CorrelationObjectLink.class);

            SearchCondition sc = new SearchCondition(CorrelationObjectLink.class, CorrelationObjectLink.ECN_BRANCH_IDENTIFIER, SearchCondition.EQUAL, ecnBranchIdentifier);
            qs.appendWhere(sc, new int[]{0});

            qs.appendAnd();
            sc = new SearchCondition(CorrelationObjectLink.class, CorrelationObjectLink.PER_BRANCH_IDENTIFIER, SearchCondition.EQUAL, perBranchIdentifier);
            qs.appendWhere(sc, new int[]{0});

            qs.appendAnd();
            sc = new SearchCondition(CorrelationObjectLink.class, CorrelationObjectLink.LINK_TYPE, SearchCondition.EQUAL, linkType);
            qs.appendWhere(sc, new int[]{0});

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("=====queryCorrelationObjectLink.sql=" + qs);
            }

            QueryResult result = PersistenceHelper.manager.find(qs);
            if (result.hasMoreElements()) {
                return (CorrelationObjectLink) result.nextElement();
            }
        }
        return null;
    }

    @Override
    public Set<CorrelationObjectLink> queryCorrelationObjectLinks(WTChangeActivity2 activity2, String linkType) throws WTException {
        Set<CorrelationObjectLink> links = new HashSet<>();
        if (activity2 != null) {
            String ecaVid = PersistenceHelper.getObjectIdentifier(activity2).toString();
            LOGGER.info(">>>>>>>>>>queryCorrelationObjectLink.ecaVid:" + ecaVid);

            QuerySpec qs = new QuerySpec(CorrelationObjectLink.class);
            SearchCondition sc = new SearchCondition(CorrelationObjectLink.class, CorrelationObjectLink.ECA_IDENTIFIER, SearchCondition.EQUAL, ecaVid);
            qs.appendWhere(sc, new int[]{0});

            if (StringUtils.isNotEmpty(linkType)) {
                qs.appendAnd();
                sc = new SearchCondition(CorrelationObjectLink.class, CorrelationObjectLink.LINK_TYPE, SearchCondition.EQUAL, linkType);
                qs.appendWhere(sc, new int[]{0});
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("=====queryCorrelationObjectLink.sql=" + qs);
            }

            QueryResult result = PersistenceHelper.manager.find(qs);
            while (result.hasMoreElements()) {
                links.add((CorrelationObjectLink) result.nextElement());
            }
        }
        return links;
    }

    @Override
    public Set<CorrelationObjectLink> queryCorrelationObjectLinks(WTChangeOrder2 changeOrder2) throws WTException {
        Set<CorrelationObjectLink> links = new HashSet<>();
        if (changeOrder2 != null) {
            QuerySpec qs = new QuerySpec(CorrelationObjectLink.class);

            SearchCondition sc = new SearchCondition(CorrelationObjectLink.class, "roleAObjectRef.key.id", SearchCondition.EQUAL, changeOrder2.getPersistInfo().getObjectIdentifier().getId());
            qs.appendWhere(sc, new int[]{0});

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("=====queryCorrelationObjectLink.sql=" + qs);
            }

            QueryResult result = PersistenceHelper.manager.find(qs);
            while (result.hasMoreElements()) {
                links.add((CorrelationObjectLink) result.nextElement());
            }
        }
        return links;
    }

    @Override
    public Set<CorrelationObjectLink> queryCorrelationObjectLinks(WTChangeOrder2 changeOrder2, String linkType) throws WTException {
        Set<CorrelationObjectLink> links = new HashSet<>();
        if (changeOrder2 != null && StringUtils.isNotEmpty(linkType)) {
            QuerySpec qs = new QuerySpec(CorrelationObjectLink.class);

            SearchCondition sc = new SearchCondition(CorrelationObjectLink.class, "roleAObjectRef.key.id", SearchCondition.EQUAL, changeOrder2.getPersistInfo().getObjectIdentifier().getId());
            qs.appendWhere(sc, new int[]{0});

            qs.appendAnd();
            sc = new SearchCondition(CorrelationObjectLink.class, CorrelationObjectLink.LINK_TYPE, SearchCondition.EQUAL, linkType);
            qs.appendWhere(sc, new int[]{0});

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("=====queryCorrelationObjectLinks.sql=" + qs);
            }

            QueryResult result = PersistenceHelper.manager.find(qs);
            while (result.hasMoreElements()) {
                links.add((CorrelationObjectLink) result.nextElement());
            }
        }
        return links;
    }

    @Override
    public Set<CorrelationObjectLink> queryCorrelationObjectLinks(WTChangeOrder2 changeOrder2, String linkType, String routing) throws WTException {
        Set<CorrelationObjectLink> links = new HashSet<>();
        if (changeOrder2 != null && StringUtils.isNotEmpty(linkType)) {
            QuerySpec qs = new QuerySpec(CorrelationObjectLink.class);

            SearchCondition sc = new SearchCondition(CorrelationObjectLink.class, "roleAObjectRef.key.id", SearchCondition.EQUAL, changeOrder2.getPersistInfo().getObjectIdentifier().getId());
            qs.appendWhere(sc, new int[]{0});

            qs.appendAnd();
            sc = new SearchCondition(CorrelationObjectLink.class, CorrelationObjectLink.LINK_TYPE, SearchCondition.EQUAL, linkType);
            qs.appendWhere(sc, new int[]{0});

            qs.appendAnd();
            sc = new SearchCondition(CorrelationObjectLink.class, CorrelationObjectLink.ROUTING, SearchCondition.EQUAL, routing);
            qs.appendWhere(sc, new int[]{0});

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("=====queryCorrelationObjectLinks.sql=" + qs);
            }

            QueryResult result = PersistenceHelper.manager.find(qs);
            while (result.hasMoreElements()) {
                links.add((CorrelationObjectLink) result.nextElement());
            }
        }
        return links;
    }

    @Override
    public Set<Persistable> queryPersistable(WTChangeOrder2 changeOrder2, String linkType) throws WTException {
        Set<Persistable> persistables = new HashSet<>();
        if (changeOrder2 != null && StringUtils.isNotEmpty(linkType)) {
            QuerySpec qs = new QuerySpec(CorrelationObjectLink.class);

            SearchCondition sc = new SearchCondition(CorrelationObjectLink.class, "roleAObjectRef.key.id", SearchCondition.EQUAL, changeOrder2.getPersistInfo().getObjectIdentifier().getId());
            qs.appendWhere(sc, new int[]{0});

            qs.appendAnd();
            sc = new SearchCondition(CorrelationObjectLink.class, CorrelationObjectLink.LINK_TYPE, SearchCondition.EQUAL, linkType);
            qs.appendWhere(sc, new int[]{0});

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("=====queryPersistable.sql=" + qs);
            }

            QueryResult result = PersistenceHelper.manager.find(qs);
            while (result.hasMoreElements()) {
                CorrelationObjectLink link = (CorrelationObjectLink) result.nextElement();
                Persistable persistable = link.getPersistable();
                if (persistable instanceof Iterated) {
                    persistables.add(getLatestVersion((Iterated) persistable));
                } else {
                    persistables.add(persistable);
                }
            }
        }
        return persistables;
    }

    @Override
    public Set<Persistable> queryPersistable(WTChangeOrder2 changeOrder2) throws WTException {
        Set<Persistable> persistables = new HashSet<>();
        if (changeOrder2 != null) {
            QuerySpec qs = new QuerySpec(CorrelationObjectLink.class);

            SearchCondition sc = new SearchCondition(CorrelationObjectLink.class, "roleAObjectRef.key.id", SearchCondition.EQUAL, changeOrder2.getPersistInfo().getObjectIdentifier().getId());
            qs.appendWhere(sc, new int[]{0});

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("=====queryPersistable.sql=" + qs);
            }

            QueryResult result = PersistenceHelper.manager.find(qs);
            while (result.hasMoreElements()) {
                CorrelationObjectLink link = (CorrelationObjectLink) result.nextElement();
                Persistable persistable = link.getPersistable();
                if (persistable instanceof Iterated) {
                    persistables.add(getLatestVersion((Iterated) persistable));
                } else {
                    persistables.add(persistable);
                }
            }
        }
        return persistables;
    }

    @Override
    public TransactionTask queryTransactionTask(WTChangeOrder2 changeOrder2, WTChangeActivity2 changeActivity2, String changeTheme) throws WTException {
        if (changeOrder2 != null) {
            QuerySpec qs = new QuerySpec();
            qs.setAdvancedQueryEnabled(true);

            int taskIndex = qs.appendClassList(TransactionTask.class, true);
            int linkIndex = qs.appendClassList(CorrelationObjectLink.class, false);

            SearchCondition sc = new SearchCondition(CorrelationObjectLink.class, CorrelationObjectLink.ECN_BRANCH_IDENTIFIER, SearchCondition.EQUAL, String.valueOf(changeOrder2.getBranchIdentifier()));
            qs.appendWhere(sc, new int[]{linkIndex});

            qs.appendAnd();
            sc = new SearchCondition(CorrelationObjectLink.class, "roleBObjectRef.key.id", TransactionTask.class, "thePersistInfo.theObjectIdentifier.id");
            qs.appendWhere(sc, new int[]{linkIndex, taskIndex});

            if (changeActivity2 == null) {
                if (StringUtils.isNotEmpty(changeTheme)) {
                    qs.appendAnd();
                    sc = new SearchCondition(TransactionTask.class, TransactionTask.CHANGE_THEME, SearchCondition.EQUAL, changeTheme);
                    qs.appendWhere(sc, new int[]{taskIndex});
                }
            } else {
                qs.appendAnd();
                String activity2 = PersistenceHelper.getObjectIdentifier(changeActivity2).toString();
                sc = new SearchCondition(TransactionTask.class, TransactionTask.CHANGE_ACTIVITY2, SearchCondition.EQUAL, activity2);
                qs.appendWhere(sc, new int[]{taskIndex});
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("=====queryCorrelationObjectLink.sql=" + qs);
            }

            QueryResult result = PersistenceHelper.manager.find(qs);
            if (result.hasMoreElements()) {
                Object[] objects = (Object[]) result.nextElement();
                return (TransactionTask) objects[0];
            }
        }
        return null;
    }

    @Override
    public Set<WTChangeOrder2> queryWTChangeOrder2(String perBranchIdentifier, String linkType) throws WTException {
        Set<WTChangeOrder2> order2s = new HashSet<>();
        if (StringUtils.isNotEmpty(perBranchIdentifier) && StringUtils.isNotEmpty(linkType)) {
            QuerySpec qs = new QuerySpec(CorrelationObjectLink.class);

            SearchCondition sc = new SearchCondition(CorrelationObjectLink.class, CorrelationObjectLink.PER_BRANCH_IDENTIFIER, SearchCondition.EQUAL, perBranchIdentifier);
            qs.appendWhere(sc, new int[]{0});

            qs.appendAnd();
            sc = new SearchCondition(CorrelationObjectLink.class, CorrelationObjectLink.LINK_TYPE, SearchCondition.EQUAL, linkType);
            qs.appendWhere(sc, new int[]{0});

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("=====queryWTChangeOrder2.sql=" + qs);
            }

            QueryResult result = PersistenceHelper.manager.find(qs);
            while (result.hasMoreElements()) {
                CorrelationObjectLink link = (CorrelationObjectLink) result.nextElement();
                order2s.add(link.getChangeOrder2());
            }
        }
        return order2s;
    }

    @Override
    public Iterated getLatestIterated(Iterated iterated) throws WTException {
        QueryResult result = VersionControlHelper.service.allIterationsFrom(iterated);
        if (result.hasMoreElements()) {
            return (Iterated) result.nextElement();
        }
        return iterated;
    }

    @Override
    public Iterated getLatestVersion(Iterated iterated) throws WTException {
        QueryResult result = VersionControlHelper.service.iterationsOf(iterated);
        if (result.hasMoreElements()) {
            return (Iterated) result.nextElement();
        }
        return iterated;
    }


    @Override
    public ManageTeamTemplate newManageTeamTemplate(String templateName, String showTemplate, String shareTemplate, String templateOid, ObjectReference objectReference, String userName, String userFullNmae) throws WTException {
        ManageTeamTemplate teamTemplate = ManageTeamTemplate.newManageTeamTemplate();
        try {
            teamTemplate.setTemplateName(templateName);
            teamTemplate.setShowTemplate(showTemplate);
            teamTemplate.setShareTemplate(shareTemplate);
            teamTemplate.setTemplateOid(templateOid);
            teamTemplate.setSaveUser(objectReference);
            teamTemplate.setUserName(userName);
            teamTemplate.setUserFullName(userFullNmae);
            PersistenceServerHelper.manager.insert(teamTemplate);
            teamTemplate = (ManageTeamTemplate) PersistenceHelper.manager.refresh(teamTemplate);
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
        return teamTemplate;
    }

    @Override
    public ManageTeamTemplate queryManageTeamTemplate(String templateOid) throws WTException {
        QuerySpec qs = new QuerySpec(ManageTeamTemplate.class);
        SearchCondition sc = new SearchCondition(ManageTeamTemplate.class, ManageTeamTemplate.TEMPLATE_OID, SearchCondition.EQUAL, templateOid);
        qs.appendWhere(sc, new int[]{0});
        QueryResult result = PersistenceHelper.manager.find(qs);
        if (result.hasMoreElements()) {
            Object object = result.nextElement();
            if (object instanceof ManageTeamTemplate)
                return (ManageTeamTemplate) object;
        }
        return null;
    }

    @Override
    public List<PersonalTeamTemplate> queryAllTemplates(Object processObj) throws WTException {
        List<PersonalTeamTemplate> personalTeamTemplates = new ArrayList<>();
        WfProcess process = WorkflowUtil.getProcess(processObj);
        if (process == null) {
            throw new WTException("工作流进程不存在");
        } else {
            String workflowTemplateName = process.getTemplate().getName();
            QuerySpec qs = new QuerySpec(PersonalTeamTemplate.class);
            SearchCondition sc = new SearchCondition(PersonalTeamTemplate.class, "workflowTemplateName", "=", workflowTemplateName);
            qs.appendWhere(sc, new int[1]);
            QueryResult result = PersistenceHelper.manager.find(qs);
            while (result != null && result.hasMoreElements()) {
                personalTeamTemplates.add((PersonalTeamTemplate) result.nextElement());
            }
            return personalTeamTemplates;
        }
    }

    @Override
    public void deleteManageTeamTemplate(ManageTeamTemplate manageTeamTemplate) throws WTException {
        if (manageTeamTemplate != null) PersistenceServerHelper.manager.remove(manageTeamTemplate);
    }


    @Override
    public ManageTeamTemplate updateManageTeamTemplate(ManageTeamTemplate manageTeamTemplate, String showTemplate, String shareTemplate) throws WTException {
        if (manageTeamTemplate != null) {
            try {
                manageTeamTemplate.setShowTemplate(showTemplate);
                manageTeamTemplate.setShareTemplate(shareTemplate);
                PersistenceServerHelper.manager.update(manageTeamTemplate);
                manageTeamTemplate = (ManageTeamTemplate) PersistenceHelper.manager.refresh(manageTeamTemplate);
            } catch (Exception e) {
                throw new WTException(e.getStackTrace());
            }
        }
        return manageTeamTemplate;
    }

    @Override
    public ManageTeamTemplateShow newManageTeamTemplateShow(String templateName, String showTemplate, String templateOid, ObjectReference objectReference, String userName, String userFullNmae) throws WTException {
        ManageTeamTemplateShow teamTemplateShow = ManageTeamTemplateShow.newManageTeamTemplateShow();
        try {
            teamTemplateShow.setTemplateName(templateName);
            teamTemplateShow.setShowTemplate(showTemplate);
            teamTemplateShow.setTemplateOid(templateOid);
            teamTemplateShow.setSaveUser(objectReference);
            teamTemplateShow.setUserName(userName);
            teamTemplateShow.setUserFullName(userFullNmae);
            PersistenceServerHelper.manager.insert(teamTemplateShow);
            teamTemplateShow = (ManageTeamTemplateShow) PersistenceHelper.manager.refresh(teamTemplateShow);
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
        return teamTemplateShow;
    }

    @Override
    public ManageTeamTemplateShow updateManageTeamTemplateShow(ManageTeamTemplateShow manageTeamTemplateShow, String showTemplate) throws WTException {
        if (manageTeamTemplateShow != null) {
            try {
                manageTeamTemplateShow.setShowTemplate(showTemplate);
                PersistenceServerHelper.manager.update(manageTeamTemplateShow);
                manageTeamTemplateShow = (ManageTeamTemplateShow) PersistenceHelper.manager.refresh(manageTeamTemplateShow);
            } catch (Exception e) {
                throw new WTException(e.getStackTrace());
            }
        }
        return manageTeamTemplateShow;
    }

    @Override
    public List<ManageTeamTemplateShow> queryManageTeamTemplateShow(String templateOid) throws WTException {
        List<ManageTeamTemplateShow> manageTeamTemplateShows = new ArrayList<>();
        QuerySpec qs = new QuerySpec(ManageTeamTemplateShow.class);
        SearchCondition sc = new SearchCondition(ManageTeamTemplateShow.class, ManageTeamTemplateShow.TEMPLATE_OID, SearchCondition.EQUAL, templateOid);
        qs.appendWhere(sc, new int[]{0});
        QueryResult result = PersistenceHelper.manager.find(qs);
        while (result.hasMoreElements()) {
            Object object = result.nextElement();
            if (object instanceof ManageTeamTemplateShow)
                manageTeamTemplateShows.add((ManageTeamTemplateShow) object);
        }
        return manageTeamTemplateShows;
    }

    @Override
    public void deleteManageTeamTemplateShow(ManageTeamTemplateShow manageTeamTemplateShow) throws WTException {
        if (manageTeamTemplateShow != null) PersistenceServerHelper.manager.remove(manageTeamTemplateShow);
    }
}
