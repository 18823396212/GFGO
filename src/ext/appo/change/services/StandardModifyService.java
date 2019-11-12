package ext.appo.change.services;

import ext.appo.change.interfaces.ModifyService;
import ext.appo.change.models.CorrelationObjectLink;
import ext.appo.change.models.TransactionTask;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.log4j.LogR;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.services.StandardManager;
import wt.util.WTException;
import wt.vc.Iterated;
import wt.vc.VersionControlHelper;

import java.util.HashSet;
import java.util.Set;

public class StandardModifyService extends StandardManager implements ModifyService {

    private static final Logger LOGGER = LogR.getLogger(StandardModifyService.class.getName());

    public static StandardModifyService newStandardModifyService() throws Exception {
        StandardModifyService instance = new StandardModifyService();
        instance.initialize();
        return instance;
    }

    /**
     * 新建事务性任务
     * @param changeTheme
     * @param changeDescribe
     * @param responsible
     * @param needDate
     * @param changeActivity2
     * @return
     * @throws WTException
     */
    @Override
    public TransactionTask newTransactionTask(String changeTheme, String changeDescribe, String responsible, String needDate, WTChangeActivity2 changeActivity2) throws WTException {
        TransactionTask task = TransactionTask.newTransactionTask();
        try {
            task.setChangeTheme(changeTheme);
            task.setChangeDescribe(changeDescribe);
            task.setResponsible(responsible);
            task.setNeedDate(needDate);
            if (changeActivity2 != null) task.setChangeActivity2(PersistenceHelper.getObjectIdentifier(changeActivity2).toString());
            PersistenceServerHelper.manager.insert(task);
            task = (TransactionTask) PersistenceHelper.manager.refresh(task);
        } catch (Exception e) {
            throw new WTException(e.getStackTrace());
        }
        return task;
    }

    /**
     * 新建ECN与相关对象的Link
     * @param changeOrder2
     * @param persistable
     * @param linkType
     * @param ecnBranchIdentifier
     * @param perBranchIdentifier
     * @return
     * @throws WTException
     */
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

    /**
     * 新建ECN与相关对象的Link
     * @param changeOrder2
     * @param persistable
     * @param linkType
     * @param ecnBranchIdentifier
     * @param perBranchIdentifier
     * @param ecaIdentifier
     * @param aadDescription
     * @param routing
     * @return
     * @throws WTException
     */
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

    /**
     * 删除事务性任务
     * @param task
     * @throws WTException
     */
    @Override
    public void deleteTransactionTask(TransactionTask task) throws WTException {
        if (task != null) PersistenceServerHelper.manager.remove(task);
    }

    /**
     * 移除ECN与相关对象的Link
     * @param link
     * @throws WTException
     */
    @Override
    public void removeCorrelationObjectLink(CorrelationObjectLink link) throws WTException {
        if (link != null) PersistenceServerHelper.manager.remove(link);
    }

    /**
     * 移除ECN与相关对象的Link
     * @param changeOrder2
     * @param persistable
     * @throws WTException
     */
    @Override
    public void removeCorrelationObjectLink(WTChangeOrder2 changeOrder2, Persistable persistable) throws WTException {
        CorrelationObjectLink link = queryCorrelationObjectLink(changeOrder2, persistable);
        if (null != link) PersistenceServerHelper.manager.remove(link);
    }

    /**
     * 更事务性任务
     * @param task
     * @param changeTheme
     * @param changeDescribe
     * @param responsible
     * @param needDate
     * @throws WTException
     * @return
     */
    @Override
    public TransactionTask updateTransactionTask(TransactionTask task, String changeTheme, String changeDescribe, String responsible, String needDate) throws WTException {
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
        return task;
    }

    /**
     * ECN与相关对象的Link
     * @param ecnBranchIdentifier
     * @param perBranchIdentifier
     * @param linkType
     * @throws WTException
     */
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

    /**
     * 更新ECN与相关对象的Link
     * @param link
     * @param aadDescription
     * @param routing
     * @return
     * @throws WTException
     */
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

    /**
     * 更新ECN与相关对象的Link
     * @param link
     * @param ecaIdentifier
     * @param aadDescription
     * @param routing
     * @return
     * @throws WTException
     */
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

    /**
     * 查询ECN与相关对象的Link
     * @param changeOrder2
     * @param persistable
     * @return
     * @throws Exception
     */
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

    /**
     * 查询ECN与相关对象的Link
     * @param changeOrder2
     * @param perBranchIdentifier
     * @return
     * @throws WTException
     */
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

    /**
     * 查询ECN与相关对象的Link
     * @param ecnBranchIdentifier
     * @param perBranchIdentifier
     * @return
     * @throws WTException
     */
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

    /**
     * 查询ECA与相关对象的Link
     * @param activity2
     * @param linkType
     * @return
     * @throws WTException
     */
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

    /**
     * 查询ECN与相关对象的Link
     * @param changeOrder2
     * @return
     * @throws WTException
     */
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

    /**
     * 查询ECN与相关对象的Link
     * @param changeOrder2
     * @param linkType
     * @return
     * @throws WTException
     */
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

    /**
     * 查询ECN与相关对象的Link
     * @param changeOrder2
     * @param linkType
     * @param routing
     * @return
     * @throws WTException
     */
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

    /**
     * 查询ECN与相关的对象
     * @param changeOrder2
     * @param linkType
     * @return
     * @throws WTException
     */
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

    /**
     * 查询ECN与相关的对象
     * @param changeOrder2
     * @return
     * @throws WTException
     */
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

    /**
     * 查询事务性任务
     * @param changeOrder2
     * @param changeActivity2
     * @param changeTheme
     * @return
     * @throws WTException
     */
    @Override
    public TransactionTask queryTransactionTask(WTChangeOrder2 changeOrder2, WTChangeActivity2 changeActivity2, String changeTheme) throws WTException {
        if (changeOrder2 != null && StringUtils.isNotEmpty(changeTheme)) {
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
                qs.appendAnd();
                sc = new SearchCondition(TransactionTask.class, TransactionTask.CHANGE_THEME, SearchCondition.EQUAL, changeTheme);
                qs.appendWhere(sc, new int[]{taskIndex});
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

    /**
     * 查询对象关联的ECN
     * @param perBranchIdentifier
     * @param linkType
     * @return
     * @throws WTException
     */
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

    /**
     * 获取对象的最新版本(最新大版本最新小版本)
     * @param iterated
     * @return
     * @throws WTException
     */
    @Override
    public Iterated getLatestIterated(Iterated iterated) throws WTException {
        QueryResult result = VersionControlHelper.service.allIterationsFrom(iterated);
        if (result.hasMoreElements()) {
            return (Iterated) result.nextElement();
        }
        return iterated;
    }

    /**
     * 获取对象传入大版本的最新版本(传入大版本最新小版本)
     * @param iterated
     * @return
     * @throws WTException
     */
    @Override
    public Iterated getLatestVersion(Iterated iterated) throws WTException {
        QueryResult result = VersionControlHelper.service.iterationsOf(iterated);
        if (result.hasMoreElements()) {
            return (Iterated) result.nextElement();
        }
        return iterated;
    }

}
