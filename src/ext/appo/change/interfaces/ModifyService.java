package ext.appo.change.interfaces;

import ext.appo.change.models.CorrelationObjectLink;
import ext.appo.change.models.TransactionTask;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.util.WTException;
import wt.vc.Iterated;

import java.util.Set;

public interface ModifyService {

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
    TransactionTask newTransactionTask(String changeTheme, String changeDescribe, String responsible, String needDate, WTChangeActivity2 changeActivity2) throws WTException;

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
    CorrelationObjectLink newCorrelationObjectLink(WTChangeOrder2 changeOrder2, Persistable persistable, String linkType, String ecnBranchIdentifier, String perBranchIdentifier) throws WTException;

    /**
     * 删除事务性任务
     * @param task
     * @throws Exception
     */
    void deleteTransactionTask(TransactionTask task) throws WTException;

    /**
     * 移除ECN与相关对象的Link
     * @param link
     * @throws WTException
     */
    void removeCorrelationObjectLink(CorrelationObjectLink link) throws WTException;

    /**
     * 移除ECN与相关对象的Link
     * @param changeOrder2
     * @param persistable
     * @throws Exception
     */
    void removeCorrelationObjectLink(WTChangeOrder2 changeOrder2, Persistable persistable) throws WTException;

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
    TransactionTask updateTransactionTask(TransactionTask task, String changeTheme, String changeDescribe, String responsible, String needDate) throws WTException;

    /**
     * ECN与相关对象的Link
     * @param ecnBranchIdentifier
     * @param perBranchIdentifier
     * @throws WTException
     */
    void updateCorrelationObjectLink(String ecnBranchIdentifier, String perBranchIdentifier, String linkType) throws WTException;

    /**
     * 查询ECN与相关对象的Link
     * @param changeOrder2
     * @param persistable
     * @return
     * @throws Exception
     */
    CorrelationObjectLink queryCorrelationObjectLink(WTChangeOrder2 changeOrder2, Persistable persistable) throws WTException;

    /**
     * 查询ECN与相关对象的Link
     * @param changeOrder2
     * @param perBranchIdentifier
     * @return
     * @throws WTException
     */
    CorrelationObjectLink queryCorrelationObjectLink(WTChangeOrder2 changeOrder2, String perBranchIdentifier) throws WTException;

    /**
     * 查询ECN与相关对象的Link
     * @param ecnBranchIdentifier
     * @param perBranchIdentifier
     * @param linkType
     * @return
     * @throws WTException
     */
    CorrelationObjectLink queryCorrelationObjectLink(String ecnBranchIdentifier, String perBranchIdentifier, String linkType) throws WTException;

    /**
     * 查询ECN与相关对象的Link
     * @param changeOrder2
     * @return
     * @throws WTException
     */
    Set<CorrelationObjectLink> queryCorrelationObjectLinks(WTChangeOrder2 changeOrder2) throws WTException;

    /**
     * 查询ECN与相关对象的Link
     * @param changeOrder2
     * @param linkType
     * @return
     * @throws WTException
     */
    Set<CorrelationObjectLink> queryCorrelationObjectLinks(WTChangeOrder2 changeOrder2, String linkType) throws WTException;

    /**
     * 查询ECN与相关的对象
     * @param changeOrder2
     * @return
     * @throws WTException
     */
    Set<Persistable> queryPersistable(WTChangeOrder2 changeOrder2, String linkType) throws WTException;

    /**
     * 查询ECN与相关的对象
     * @param changeOrder2
     * @return
     * @throws WTException
     */
    Set<Persistable> queryPersistable(WTChangeOrder2 changeOrder2) throws WTException;

    /**
     * 查询事务性任务
     * @param changeActivity2
     * @return
     * @throws WTException
     */
    TransactionTask queryTransactionTask(WTChangeActivity2 changeActivity2) throws WTException;

    /**
     * 查询对象关联的ECN
     * @param perBranchIdentifier
     * @param linkType
     * @return
     * @throws WTException
     */
    Set<WTChangeOrder2> queryWTChangeOrder2(String perBranchIdentifier, String linkType) throws WTException;

    /**
     * 获取对象的最新版本(最新大版本最新小版本)
     * @param iterated
     * @return
     * @throws WTException
     */
    Iterated getLatestIterated(Iterated iterated) throws WTException;

    /**
     * 获取对象传入大版本的最新版本(传入大版本最新小版本)
     * @param iterated
     * @return
     * @throws WTException
     */
    Iterated getLatestVersion(Iterated iterated) throws WTException;

}
