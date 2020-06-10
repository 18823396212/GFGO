package ext.appo.change.interfaces;

import ext.appo.change.models.CorrelationObjectLink;
import ext.appo.change.models.ManageTeamTemplate;
import ext.appo.change.models.ManageTeamTemplateShow;
import ext.appo.change.models.TransactionTask;
import ext.generic.reviewprincipal.model.PersonalTeamTemplate;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.util.WTException;
import wt.vc.Iterated;

import java.util.List;
import java.util.Set;

public interface ModifyService {

    /**
     * 新建事务性任务
     *
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
     * 新建事务性任务
     *
     * @param changeTheme
     * @param changeDescribe
     * @param responsible
     * @param needDate
     * @param changeActivity2
     * @return
     * @throws WTException
     */
    TransactionTask newTransactionTask(String changeTheme, String changeDescribe, String responsible, String needDate, WTChangeActivity2 changeActivity2, String taskType, String clfs) throws WTException;

    /**
     * 新建ECN与相关对象的Link
     *
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
     * 新建ECN与相关对象的Link
     *
     * @param changeOrder2
     * @param persistable
     * @param linkType
     * @param ecnBranchIdentifier
     * @param perBranchIdentifier
     * @param treatment
     * @return
     * @throws WTException
     */
    CorrelationObjectLink newCorrelationObjectLink(WTChangeOrder2 changeOrder2, Persistable persistable, String linkType, String ecnBranchIdentifier, String perBranchIdentifier, String treatment) throws WTException;


    /**
     * 新建ECN与相关对象的Link
     *
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
    CorrelationObjectLink newCorrelationObjectLink(WTChangeOrder2 changeOrder2, Persistable persistable, String linkType, String ecnBranchIdentifier, String perBranchIdentifier, String ecaIdentifier, String aadDescription, String routing) throws WTException;

    /**
     * 删除事务性任务
     *
     * @param task
     * @throws Exception
     */
    void deleteTransactionTask(TransactionTask task) throws WTException;

    /**
     * 移除ECN与相关对象的Link
     *
     * @param link
     * @throws WTException
     */
    void removeCorrelationObjectLink(CorrelationObjectLink link) throws WTException;

    /**
     * 移除ECN与相关对象的Link
     *
     * @param changeOrder2
     * @param persistable
     * @throws Exception
     */
    void removeCorrelationObjectLink(WTChangeOrder2 changeOrder2, Persistable persistable) throws WTException;

    /**
     * 更事务性任务
     *
     * @param task
     * @param changeTheme
     * @param changeDescribe
     * @param responsible
     * @param needDate
     * @return
     * @throws WTException
     */
    TransactionTask updateTransactionTask(TransactionTask task, String changeTheme, String changeDescribe, String responsible, String needDate) throws WTException;

    /**
     * 更事务性任务
     *
     * @param task
     * @param changeTheme
     * @param changeDescribe
     * @param responsible
     * @param needDate
     * @return
     * @throws WTException
     */
    TransactionTask updateTransactionTask(TransactionTask task, String changeTheme, String changeDescribe, String responsible, String needDate, String taskType, String clfs) throws WTException;


    /**
     * 更事务性任务ECA Id
     *
     * @param task
     * @param changeActivity2
     * @return
     * @throws WTException
     */
    TransactionTask updateTransactionTask(TransactionTask task, String changeActivity2) throws WTException;

    /**
     * 更新ECN与相关对象的Link
     *
     * @param ecnBranchIdentifier
     * @param perBranchIdentifier
     * @throws WTException
     */
    void updateCorrelationObjectLink(String ecnBranchIdentifier, String perBranchIdentifier, String linkType) throws WTException;

    /**
     * 更新ECN与相关对象的Link
     *
     * @param ecnBranchIdentifier
     * @param perBranchIdentifier
     * @param linkType
     * @param treatment
     * @throws WTException
     */
    void updateCorrelationObjectLink(String ecnBranchIdentifier, String perBranchIdentifier, String linkType, String treatment) throws WTException;

    /**
     * 更新ECN与相关对象的Link
     *
     * @param link
     * @param aadDescription
     * @param routing
     * @throws WTException
     */
    CorrelationObjectLink updateCorrelationObjectLink(CorrelationObjectLink link, String aadDescription, String routing) throws WTException;

    /**
     * 更新ECN与相关对象的Link
     *
     * @param link
     * @param ecaIdentifier
     * @param aadDescription
     * @param routing
     * @return
     * @throws WTException
     */
    CorrelationObjectLink updateCorrelationObjectLink(CorrelationObjectLink link, String ecaIdentifier, String aadDescription, String routing) throws WTException;

    /**
     * 查询ECN与相关对象的Link
     *
     * @param changeOrder2
     * @param persistable
     * @return
     * @throws Exception
     */
    CorrelationObjectLink queryCorrelationObjectLink(WTChangeOrder2 changeOrder2, Persistable persistable) throws WTException;

    /**
     * 查询ECN与相关对象的Link
     *
     * @param changeOrder2
     * @param perBranchIdentifier
     * @return
     * @throws WTException
     */
    CorrelationObjectLink queryCorrelationObjectLink(WTChangeOrder2 changeOrder2, String perBranchIdentifier) throws WTException;

    /**
     * 查询ECN与相关对象的Link
     *
     * @param ecnBranchIdentifier
     * @param perBranchIdentifier
     * @param linkType
     * @return
     * @throws WTException
     */
    CorrelationObjectLink queryCorrelationObjectLink(String ecnBranchIdentifier, String perBranchIdentifier, String linkType) throws WTException;

    /**
     * 查询ECA与相关对象的Link
     *
     * @param activity2
     * @param linkType
     * @return
     * @throws WTException
     */
    Set<CorrelationObjectLink> queryCorrelationObjectLinks(WTChangeActivity2 activity2, String linkType) throws WTException;

    /**
     * 查询ECN与相关对象的Link
     *
     * @param changeOrder2
     * @return
     * @throws WTException
     */
    Set<CorrelationObjectLink> queryCorrelationObjectLinks(WTChangeOrder2 changeOrder2) throws WTException;

    /**
     * 查询ECN与相关对象的Link
     *
     * @param changeOrder2
     * @param linkType
     * @param routing
     * @return
     * @throws WTException
     */
    Set<CorrelationObjectLink> queryCorrelationObjectLinks(WTChangeOrder2 changeOrder2, String linkType, String routing) throws WTException;

    /**
     * 查询ECN与相关对象的Link
     *
     * @param changeOrder2
     * @param linkType
     * @return
     * @throws WTException
     */
    Set<CorrelationObjectLink> queryCorrelationObjectLinks(WTChangeOrder2 changeOrder2, String linkType) throws WTException;

    /**
     * 查询ECN与相关的对象
     *
     * @param changeOrder2
     * @return
     * @throws WTException
     */
    Set<Persistable> queryPersistable(WTChangeOrder2 changeOrder2, String linkType) throws WTException;

    /**
     * 查询ECN与相关的对象
     *
     * @param changeOrder2
     * @return
     * @throws WTException
     */
    Set<Persistable> queryPersistable(WTChangeOrder2 changeOrder2) throws WTException;

    /**
     * 查询事务性任务
     *
     * @param changeOrder2
     * @param changeActivity2
     * @param changeTheme
     * @return
     * @throws WTException
     */
    TransactionTask queryTransactionTask(WTChangeOrder2 changeOrder2, WTChangeActivity2 changeActivity2, String changeTheme) throws WTException;

    /**
     * 查询对象关联的ECN
     *
     * @param perBranchIdentifier
     * @param linkType
     * @return
     * @throws WTException
     */
    Set<WTChangeOrder2> queryWTChangeOrder2(String perBranchIdentifier, String linkType) throws WTException;

    /**
     * 获取对象的最新版本(最新大版本最新小版本)
     *
     * @param iterated
     * @return
     * @throws WTException
     */
    Iterated getLatestIterated(Iterated iterated) throws WTException;

    /**
     * 获取对象传入大版本的最新版本(传入大版本最新小版本)
     *
     * @param iterated
     * @return
     * @throws WTException
     */
    Iterated getLatestVersion(Iterated iterated) throws WTException;


    /**
     * 新建管理模板
     *
     * @param templateName
     * @param showTemplate
     * @param shareTemplate
     * @param templateOid
     * @return
     */
    ManageTeamTemplate newManageTeamTemplate(String templateName, String showTemplate, String shareTemplate, String templateOid, ObjectReference objectReference, String userName, String userFullNmae) throws WTException;

    /**
     * 查询模板Oid对应的管理模板信息
     *
     * @param templateOid
     * @return
     * @throws WTException
     */
    ManageTeamTemplate queryManageTeamTemplate(String templateOid) throws WTException;

    /**
     * 查询PersonalTeamTemplate全部模板信息(同一流程模板)
     *
     * @param processObj
     * @return
     * @throws WTException
     */
    List<PersonalTeamTemplate> queryAllTemplates(Object processObj) throws WTException;

    /**
     * 删除ManageTeamTemplate模板
     *
     * @param manageTeamTemplate
     * @throws WTException
     */
    void deleteManageTeamTemplate(ManageTeamTemplate manageTeamTemplate) throws WTException;

    /**
     * 更新ManageTeamTemplate模板共享信息
     *
     * @param manageTeamTemplate
     * @param showTemplate
     * @param shareTemplate
     * @return
     * @throws WTException
     */
    ManageTeamTemplate updateManageTeamTemplate(ManageTeamTemplate manageTeamTemplate, String showTemplate, String shareTemplate) throws WTException;

    /**
     * 新建管理显示模板
     *
     * @param templateName
     * @param showTemplate
     * @param templateOid
     * @return
     */
    ManageTeamTemplateShow newManageTeamTemplateShow(String templateName, String showTemplate, String templateOid, ObjectReference objectReference, String userName, String userFullNmae) throws WTException;

    /**
     * 更新ManageTeamTemplateShow模板显示
     *
     * @param manageTeamTemplateShow
     * @param showTemplate
     * @return
     * @throws WTException
     */
    ManageTeamTemplateShow updateManageTeamTemplateShow(ManageTeamTemplateShow manageTeamTemplateShow, String showTemplate) throws WTException;

    /**
     * 查询模板Oid对应的管理显示模板信息
     *
     * @param templateOid
     * @return
     * @throws WTException
     */
    List<ManageTeamTemplateShow> queryManageTeamTemplateShow(String templateOid) throws WTException;

    /**
     * 删除ManageTeamTemplateShow模板
     *
     * @param manageTeamTemplateShow
     * @throws WTException
     */
    void deleteManageTeamTemplateShow(ManageTeamTemplateShow manageTeamTemplateShow) throws WTException;
}
