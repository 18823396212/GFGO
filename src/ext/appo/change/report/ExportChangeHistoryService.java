package ext.appo.change.report;

import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.workflow.NmWorkflowHelper;
import ext.appo.change.beans.BOMChangeInfoBean;
import ext.appo.change.beans.ChangeHistoryReportBean;
import ext.appo.change.beans.PartUpdateInfoBean;
import ext.appo.change.beans.PartUpdateTypeBean;
import ext.appo.change.constants.BomChangeConstants;
import wt.change2.WTChangeOrder2;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTList;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.vc.config.LatestConfigSpec;
import wt.workflow.engine.WfProcess;

import java.io.Serializable;
import java.util.*;

import static ext.appo.change.report.BomChangeReport.addDate;

/**
 * 变更履历导出：将Windchill数据转换为可导出的Java对象
 */
public class ExportChangeHistoryService implements Serializable {

    /**
     * 提取变更履历信息并将该信息转化为可导出的数据对象
     *
     * @param parentPart             顶层部件
     * @param level(0,1,2...;0为顶层部件) 查询BOM层数
     * @return
     * @throws WTException
     */
    public List<ChangeHistoryReportBean> extractDatas(WTPart parentPart, int level) throws Exception {
        List<ChangeHistoryReportBean> extractDatas = new ArrayList<>();
        if (parentPart == null && level < 0) {
            return extractDatas;
        }
        ChangeHistoryReportBean excelBean = getChangeHistoryBean(parentPart, "0", "", 0);
        extractDatas.add(excelBean);
        getChangeHistoryDatas(parentPart, "0", 0, extractDatas, level);
        return extractDatas;
    }

    /**
     * 获取多层BOM下部件变更履历信息并转化为指定JavaBean对象
     *
     * @param parentPart   父件
     * @param levelFlag    父件层数
     * @param extractDatas 提取后数据集合
     * @param level        查询层数
     * @throws WTException
     */
    public void getChangeHistoryDatas(WTPart parentPart, String parentId, Integer levelFlag, List<ChangeHistoryReportBean> extractDatas, Integer level) throws Exception {
        if (parentPart == null || extractDatas == null || levelFlag + 1 > level) {
            return;
        }
        levelFlag++;
//        WTList parentList = new WTArrayList();
//        parentList.add(parentPart);
        //获得子件集合
        Set<WTPart> childParts = getMonolayerPart(parentPart);
        int num = 1;
        for (WTPart childPart : childParts) {
            // 自定义JavaBean对象
            String id = "";
            if (("0").equals(parentId)) {
                id = num + "";
            } else {
                id = parentId + "." + num;
            }
            ChangeHistoryReportBean excelBean = getChangeHistoryBean(childPart, id, parentId, levelFlag);
            //add by lzy at 20200529 start
            Boolean isAllUnchanged = false;
            //最后一层如果变更类型全是 未发生变更 则不显示
            Set<WTPart> MonolayerParts = getMonolayerPart(childPart);
            if (MonolayerParts == null || MonolayerParts.size() == 0) {
                Map<String, String> updateType = excelBean.getUpdateType();
                if (updateType != null && updateType.size() > 0) {
                    Boolean isAllUnchange = true;
                    for (String flag : updateType.keySet()) {
                        String type = updateType.get(flag);
                        if (!type.trim().equals(BomChangeConstants.TYPE_12.trim())) {
                            isAllUnchange = false;
                            break;
                        }
                    }
                    if (isAllUnchange) {
                        isAllUnchanged = true;
                    }
                }
            }
            if (!isAllUnchanged) extractDatas.add(excelBean);
            //add by lzy at 20200529 end
//            extractDatas.add(excelBean);
            getChangeHistoryDatas(childPart, excelBean.getId(), levelFlag, extractDatas, level);
            num++;
        }

    }


    /**
     * 通过物料获取变更履历信息
     *
     * @param part     物料
     * @param id       序号
     * @param parentId 父序号
     * @param level    当前层数
     * @return
     * @throws WTException
     */
    public ChangeHistoryReportBean getChangeHistoryBean(WTPart part, String id, String parentId, int level) throws Exception {
        ChangeHistoryReportBean excelBean = new ChangeHistoryReportBean();

        excelBean.setPart(part);
        excelBean.setId(id);
        excelBean.setParentId(parentId);
        excelBean.setLevel(level);
        excelBean.setPartNumber(part.getNumber());
        excelBean.setPartName(part.getName());
        String number = part.getNumber();
        //物料升版各阶段信息
        List<PartUpdateTypeBean> partUpdateTypeBeans = getPartUpdateType(part);
        Set<WTChangeOrder2> wtChangeOrder2s = ChangeHistoryReport.getAllECN(part);
        //获取ECN,受影响物料的合并信息
        Map<String, PartUpdateInfoBean> partUpdateInfoBeanMap = ChangeHistoryReport.getUpdateInfo(wtChangeOrder2s, number);
        Map<String, Map<String, String>> changeVersionRecord = new HashMap<>();
        Map<String, WTChangeOrder2> ecn = new HashMap<>();
        Map<String, String> updateType = new HashMap<>();
        Map<String, String> ecnCreator = new HashMap<>();
        Map<String, String> ecnStartTime = new HashMap<>();
        Map<String, String> productType = new HashMap<>();
        Map<String, String> projectName = new HashMap<>();
        Map<String, String> changeType = new HashMap<>();
        Map<String, String> changeReason = new HashMap<>();
        Map<String, String> changePhase = new HashMap<>();
        Map<String, String> isChangeDrawing = new HashMap<>();
        Map<String, String> changeDescription = new HashMap<>();
        Map<String, String> changeDetailedDescription = new HashMap<>();
        //BOM变更内容
        Map<String, List<BOMChangeInfoBean>> bomChangeInfo = new HashMap<>();
        //物料升版各阶段信息
        for (int j = 0; j < partUpdateTypeBeans.size(); j++) {
            String flag = j + 1 + "";
            PartUpdateTypeBean partUpdateTypeBean = partUpdateTypeBeans.get(j);
            changeVersionRecord.put(flag, partUpdateTypeBean.getChangeVersion());
            ecn.put(flag, partUpdateTypeBean.getEcn());
            updateType.put(flag, partUpdateTypeBean.getUpdateType());

            PartUpdateInfoBean partUpdateInfoBean = new PartUpdateInfoBean();
            List<BOMChangeInfoBean> bomChangeInfoBeans = new ArrayList<>();
            if (partUpdateTypeBean.getEcn() != null) {
                String ecnNumber = partUpdateTypeBean.getEcn().getNumber() == null ? "" : partUpdateTypeBean.getEcn().getNumber();
                for (String ecnStr : partUpdateInfoBeanMap.keySet()) {
                    if (ecnNumber.equals(ecnStr)) {
                        partUpdateInfoBean = partUpdateInfoBeanMap.get(ecnStr);
                        break;
                    }
                }
                ecnCreator.put(flag, partUpdateInfoBean.getEcnCreator());
                ecnStartTime.put(flag, partUpdateInfoBean.getEcnStartTime());
                productType.put(flag, partUpdateInfoBean.getProductType());
                projectName.put(flag, partUpdateInfoBean.getProjectName());
                changeType.put(flag, partUpdateInfoBean.getChangeType());
                changeReason.put(flag, partUpdateInfoBean.getChangeReason());
                changePhase.put(flag, partUpdateInfoBean.getChangePhase());
                isChangeDrawing.put(flag, partUpdateInfoBean.getIsChangeDrawing());
                changeDescription.put(flag, partUpdateInfoBean.getChangeDescription());
                changeDetailedDescription.put(flag, partUpdateInfoBean.getChangeDetailedDescription());
                //BOM变更内容
                Map<String, String> changeVersionMap = partUpdateTypeBean.getChangeVersion();
                String beforeVersion = changeVersionMap.get("before") == null ? "" : changeVersionMap.get("before");
                String afterVersion = changeVersionMap.get("after") == null ? "" : changeVersionMap.get("after");
                WTPart beforePart = ChangeHistoryReport.getLatestPart(part.getNumber(), part.getViewName(), beforeVersion);
                WTPart afterPart = ChangeHistoryReport.getLatestPart(part.getNumber(), part.getViewName(), afterVersion);
                bomChangeInfoBeans = ChangeHistoryReport.getBomChangeInfoByPart(beforePart, afterPart);
                bomChangeInfo.put(flag, bomChangeInfoBeans);
            } else {
                //BOM变更内容
                Map<String, String> changeVersionMap = partUpdateTypeBean.getChangeVersion();
                String beforeVersion = changeVersionMap.get("before") == null ? "" : changeVersionMap.get("before");
                String afterVersion = changeVersionMap.get("after") == null ? "" : changeVersionMap.get("after");
                if (!beforeVersion.trim().isEmpty() && !afterVersion.trim().isEmpty()) {
                    WTPart beforePart = ChangeHistoryReport.getLatestPart(part.getNumber(), part.getViewName(), beforeVersion);
                    WTPart afterPart = ChangeHistoryReport.getLatestPart(part.getNumber(), part.getViewName(), afterVersion);
                    System.out.println("");
                    bomChangeInfoBeans = ChangeHistoryReport.getBomChangeInfoByPart(beforePart, afterPart);
                    //获取变更前版本归档流程
                    NmOid nmOid = NmOid.newNmOid(PersistenceHelper.getObjectIdentifier(beforePart));
                    String mVersion = beforePart.getVersionIdentifier().getValue();//物料大版本
                    QueryResult qr = NmWorkflowHelper.service.getRoutingHistoryData(nmOid);
                    while (qr.hasMoreElements()) {
                        WfProcess process = (WfProcess) qr.nextElement();
                        String templateName = process.getTemplate().getName();
                        if (templateName.equals("GenericPartWF")) {
                            //流程主对象
                            WTObject pbo = (WTObject) (process.getContext().getValue("primaryBusinessObject"));
                            System.out.println("pbo==" + pbo);
                            if (pbo instanceof WTPart) {
                                WTPart wtPart = (WTPart) pbo;
                                String majorVersion = wtPart.getVersionIdentifier().getValue();//物料大版本
                                if (mVersion.trim().compareTo(majorVersion.trim()) >= 0) {
                                    String processCreator = process.getCreator().getFullName();//流程启动者
                                    String processStartTime = addDate(process.getStartTime().toLocaleString(), 8);//流程启动时间，服务器少8小时;
                                    ecnCreator.put(flag, processCreator);
                                    ecnStartTime.put(flag, processStartTime);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            bomChangeInfo.put(flag, bomChangeInfoBeans);
        }
        excelBean.setChangeVersionRecord(changeVersionRecord);
        excelBean.setEcn(ecn);
        excelBean.setUpdateType(updateType);
        excelBean.setEcnCreator(ecnCreator);
        excelBean.setEcnStartTime(ecnStartTime);
        excelBean.setProductType(productType);
        excelBean.setProjectName(projectName);
        excelBean.setChangeType(changeType);
        excelBean.setChangeReason(changeReason);
        excelBean.setChangePhase(changePhase);
        excelBean.setIsChangeDrawing(isChangeDrawing);
        excelBean.setChangeDescription(changeDescription);
        excelBean.setChangeDetailedDescription(changeDetailedDescription);
        excelBean.setBomChangeInfo(bomChangeInfo);

        return excelBean;
    }


    //获得子件集合
    public static Set<WTPart> getMonolayerPart(WTPart part) {
        Set<WTPart> setPart = new HashSet<>();
        if (part != null) {
            try {
                QueryResult qr = WTPartHelper.service.getUsesWTPartMasters(part);
                while (qr.hasMoreElements()) {
                    WTPartUsageLink links = (WTPartUsageLink) qr.nextElement();
                    WTPartMaster masterChild = links.getUses();
                    WTPart sunpart = getLastestWTPartByNumber(masterChild.getNumber());
                    setPart.add(sunpart);
                }
            } catch (WTException e) {
                e.printStackTrace();
            }
        }
        return setPart;
    }

    /*
     * 通过number获取最新的物料
     */
    public static WTPart getLastestWTPartByNumber(String numStr) {
        QuerySpec queryspec = null;
        try {
            queryspec = new QuerySpec(WTPart.class);

            queryspec.appendSearchCondition(
                    new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL, numStr));
            QueryResult queryresult = PersistenceHelper.manager.find(queryspec);
            LatestConfigSpec cfg = new LatestConfigSpec();
            QueryResult qr = cfg.process(queryresult);
            while (qr.hasMoreElements()) {
                WTPart part = (WTPart) qr.nextElement();
                return part;
            }
        } catch (WTException e) {
            e.printStackTrace();
        }
        return null;
    }

    //返回物料升版各阶段信息（有制造视图和设计视图判断如果制造视图版本升版过则显示制造视图升版,否则显示设计视图升版）,没变更则为修订
    //降序(不过滤没变更的物料)
    public static List<PartUpdateTypeBean> getPartUpdateType(WTPart part) throws WTException {
        List<PartUpdateTypeBean> partUpdateTypeBeans = new ArrayList<>();
        String view = part.getViewName();
        Boolean flag = ChangeHistoryReport.isOneView(part);
        if (!flag) {
            //存在多视图
            List<WTPart> mParts = ChangeHistoryReport.getAllPartsByViewName(part, "Manufacturing");//降序排序
            //制造视图存在多版本,取制造视图升版信息,不显示设计视图信息
            if (mParts != null && mParts.size() > 1) {
                List<PartUpdateTypeBean> partUpdateTypeBeanList = getPartUpdateTypeBeans(mParts);
                partUpdateTypeBeans.addAll(partUpdateTypeBeanList);
            } else {
                //制造视图没变更升版，取设计视图
                List<WTPart> dParts = ChangeHistoryReport.getAllPartsByViewName(part, "Design");//降序排序
                List<PartUpdateTypeBean> partUpdateTypeBeanList = getPartUpdateTypeBeans(dParts);
                partUpdateTypeBeans.addAll(partUpdateTypeBeanList);
            }

        } else {
            //只有一个视图
            List<WTPart> parts = ChangeHistoryReport.getAllPartsByViewName(part, view);//降序排序
            if (parts != null && parts.size() > 0) {
                List<PartUpdateTypeBean> partUpdateTypeBeanList = getPartUpdateTypeBeans(parts);
                partUpdateTypeBeans.addAll(partUpdateTypeBeanList);
            }

        }

        return partUpdateTypeBeans;
    }

    //比较同一物料每个大版本之间的变更类型(不过滤没变更的物料)
    public static List<PartUpdateTypeBean> getPartUpdateTypeBeans(List<WTPart> parts) throws WTException {
        List<PartUpdateTypeBean> partUpdateTypeBeans = new ArrayList<>();

        if (parts.size() == 1) {
            //最新大版本没有升版变更
            PartUpdateTypeBean partUpdateTypeBean = new PartUpdateTypeBean();
            partUpdateTypeBean.setUpdateType(BomChangeConstants.TYPE_12);
            partUpdateTypeBean.setPartNumber(parts.get(0).getNumber());
            partUpdateTypeBean.setView(parts.get(0).getViewName());
            partUpdateTypeBeans.add(partUpdateTypeBean);
        } else {
            for (int i = 1; i < parts.size(); i++) {//降序
                WTPart part = parts.get(i);
                String partNumber = part.getNumber();
                String viewName = part.getViewName();
                String beforeVersion = part.getVersionIdentifier().getValue();// 变更前物料大版本
                String afterVersion = parts.get(i - 1).getVersionIdentifier().getValue();// 变更后物料大版本
                Map<String, String> changeVersion = new HashMap<>();
                changeVersion.put("before", beforeVersion);
                changeVersion.put("after", afterVersion);
                //检出导致查询出两个同一版本（A版本有问题）
                if (beforeVersion.equals(afterVersion)) {
                    //最新大版本没有升版变更
                    PartUpdateTypeBean partUpdateTypeBean = new PartUpdateTypeBean();
                    partUpdateTypeBean.setUpdateType(BomChangeConstants.TYPE_12);
                    partUpdateTypeBean.setPartNumber(part.getNumber());
                    partUpdateTypeBean.setView(part.getViewName());
                    partUpdateTypeBeans.add(partUpdateTypeBean);
                    break;
                }
                PartUpdateTypeBean partUpdateTypeBean = new PartUpdateTypeBean();
                //查询版本在ECN中是否存在升版，存在ECN升版，不存在则为修订升版
                Map<WTPart, WTChangeOrder2> wtPartWTChangeOrder2Map = ChangeHistoryReport.getAllECNAffectedParts(part);
                Boolean isEcn = false;//默认没有ECN,修订升版
                for (WTPart wtPart : wtPartWTChangeOrder2Map.keySet()) {
                    String version = wtPart.getVersionIdentifier().getValue();//物料大版本
                    String view = wtPart.getViewName();
                    if (beforeVersion.equals(version) && viewName.equals(view)) {
                        //该大版本存在升版ECN，ECN变更
                        WTChangeOrder2 ecn = wtPartWTChangeOrder2Map.get(wtPart);

                        partUpdateTypeBean.setUpdateType(BomChangeConstants.TYPE_10);
                        partUpdateTypeBean.setChangeVersion(changeVersion);
                        partUpdateTypeBean.setPartNumber(wtPart.getNumber());
                        partUpdateTypeBean.setView(wtPart.getViewName());
                        partUpdateTypeBean.setEcn(ecn);

                        partUpdateTypeBeans.add(partUpdateTypeBean);
                        isEcn = true;
                        break;
                    }
                }
                if (!isEcn) {
                    //修订升版
                    partUpdateTypeBean.setUpdateType(BomChangeConstants.TYPE_11);
                    partUpdateTypeBean.setChangeVersion(changeVersion);
                    partUpdateTypeBean.setPartNumber(partNumber);
                    partUpdateTypeBean.setView(part.getViewName());

                    partUpdateTypeBeans.add(partUpdateTypeBean);
                }

            }
        }
        return partUpdateTypeBeans;
    }

}
