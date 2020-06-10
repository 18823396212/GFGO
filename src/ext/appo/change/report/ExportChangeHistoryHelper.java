package ext.appo.change.report;

import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.workflow.NmWorkflowHelper;
import com.ptc.windchill.enterprise.history.HistoryTablesCommands;
import com.ptc.windchill.enterprise.history.MaturityHistoryInfo;
import ext.appo.change.beans.BOMChangeInfoBean;
import ext.appo.change.beans.ChangeHistoryReportBean;
import ext.appo.change.constants.BomChangeConstants;
import ext.lang.PIExcelUtils;
import ext.lang.PIStringUtils;
import ext.pi.core.PICoreHelper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.part.WTPart;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.workflow.engine.WfProcess;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ext.appo.change.report.BomChangeReport.addDate;
import static ext.appo.change.report.ChangeHistoryReport.getAllPartsByViewName;
import static ext.appo.change.report.ChangeHistoryReport.isOneView;

public class ExportChangeHistoryHelper implements Serializable {

    private String filePath = null;

    public ExportChangeHistoryHelper() {
        try {
            String codebase = PICoreHelper.service.getCodebase();
            filePath = codebase + File.separator + "ext" + File.separator + "appo" + File.separator + "change"
                    + File.separator + "report" + File.separator + "exportChangeHistoryTemplate.xlsx";
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /***
     * 导出变更履历主方法
     *
     * @param request
     * @param response
     * @throws WTException
     */
    public void exportData(HttpServletRequest request, HttpServletResponse response) throws WTException {
        if (request == null || response == null || PIStringUtils.isNull(this.filePath)) {
            return;
        }
        boolean flag = SessionServerHelper.manager.setAccessEnforced(false); // 忽略权限
        OutputStream os = null;
        try {
            // 操作主对象
            Persistable persistable = (new ReferenceFactory()).getReference(request.getParameter("oid")).getObject();
            if (persistable instanceof WTPart) {
                WTPart part = (WTPart) persistable;
                // 导出参数配置
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/x-msdownload;charset=UTF-8");
                response.setHeader("Content-disposition", "attachment; filename=" + part.getNumber() + "_ChangeHistory.xlsx");
                // 获取导出模板
                Workbook workbook = PIExcelUtils.getWorkbook(new File(this.filePath));
                // 提取变更履历信息并将该信息转化为可导出的数据对象
                ExportChangeHistoryService service = new ExportChangeHistoryService();

                Boolean isOne = isOneView(part);
                if (!isOne) {
                    //存在多视图
                    List<WTPart> mParts = getAllPartsByViewName(part, "Manufacturing");//降序排序
                    //制造视图存在多版本,取制造视图升版信息,不显示设计视图信息
                    if (mParts != null && mParts.size() > 1) {
                        part = mParts.get(0);
                    } else {
                        //制造视图没变更升版，取设计视图
                        List<WTPart> dParts = ChangeHistoryReport.getAllPartsByViewName(part, "Design");//降序排序
                        part = dParts.get(0);
                    }
                }
                System.out.println("last part==" + part);
                List<ChangeHistoryReportBean> extractDatas = service.extractDatas(part, 100);//(0,1,2,...)，展现层数
                //获取第一次归档或已发布时间
                String startTime = "";
                QueryResult queryResult = HistoryTablesCommands.maturityHistory(part);
                while (queryResult.hasMoreElements()) {
                    Object object = queryResult.nextElement();
                    if (object instanceof MaturityHistoryInfo) {
                        MaturityHistoryInfo maturityData = (MaturityHistoryInfo) object;
                        Object rowObject = maturityData.getRowObject();
                        if (rowObject instanceof WTPart){
                            WTPart wtPart=(WTPart)rowObject;
                            if (wtPart.getViewName().equals(part.getViewName())){
                                String stateStr = maturityData.getLifecycleStateStr();
                                if (stateStr.contains("已归档") || stateStr.contains("已发布")) {
                                    startTime = addDate(maturityData.getPromotedDate().get(0).toLocaleString(), 8);
                                }
                            }
                        }
                    }
                }

                //获取第一次归档或已发布时间
//                NmOid nmOid = NmOid.newNmOid(PersistenceHelper.getObjectIdentifier(part));
//                QueryResult qr = NmWorkflowHelper.service.getRoutingHistoryData(nmOid);
//                while (qr.hasMoreElements()) {
//                    WfProcess process = (WfProcess) qr.nextElement();
//                    String templateName = process.getTemplate().getName();
//                    if (templateName.equals("GenericPartWF")) {
//                        startTime = addDate(process.getStartTime().toLocaleString(),8);//流程启动时间，服务器少8小时
//                    }
//                }

                // 写入数据
                if (extractDatas != null && extractDatas.size() > 0) {
                    writeData(workbook, workbook.getSheetAt(0), extractDatas, startTime);
                }

                // 获取输出流
                os = response.getOutputStream();
                // 写入文件流
                workbook.write(os);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        } finally {
            SessionServerHelper.manager.setAccessEnforced(flag); // 添加权限
            try {
                if (os != null) {
                    os.flush();
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /***
     * 数据写入
     *
     * @param sheet
     *            第一页
     * @param extractDatas
     *            写入的数据集合
     */
    public void writeData(Workbook workbook, Sheet sheet, List<ChangeHistoryReportBean> extractDatas, String startTime) {
        if (sheet == null || extractDatas == null || extractDatas.size() == 0) {
            return;
        }

        // 创建边框样式
//        CellStyle cellStyle = createCellStyle(workbook);
        // 创建边框样式,背景色,居中
        CellStyle cellStyle2 = createCellStyle2(workbook);
        // 创建边框样式,居中
        CellStyle cellStyle3 = createCellStyle3(workbook);
        // 创建边框样式,居上
        CellStyle cellStyle4 = createCellStyle4(workbook);
        // 创建边框样式,字体颜色
        CellStyle cellStyle5 = createCellStyle5(workbook);

        for (int i = 0; i < extractDatas.size(); i++) {
            ChangeHistoryReportBean changeHistoryReportBean = extractDatas.get(i);
            String id = changeHistoryReportBean.getId();
            if (id.equals("0")) {
                setCellStringValue(sheet, 0, 1, changeHistoryReportBean.getPartNumber(), cellStyle3);
                break;
            }
        }
        setCellStringValue(sheet, 0, 3, startTime, cellStyle3);
        Integer rowIndex = 2;
        for (ChangeHistoryReportBean excelBean : extractDatas) {
            Integer cellIndex = 0;
            Integer rowSize = rowIndex;
            Map<String, List<BOMChangeInfoBean>> bomChangeInfo = excelBean.getBomChangeInfo();
            for (String flagId : bomChangeInfo.keySet()) {
                List<BOMChangeInfoBean> bomChangeInfoBeanList = bomChangeInfo.get(flagId);
                if (bomChangeInfoBeanList == null || bomChangeInfoBeanList.size() < 1) {
                    //修订没有BOM变更
                    rowSize += 1;
                } else {
                    rowSize += bomChangeInfoBeanList.size();
                }
            }
            //bomChangeInfo多跨一行,减去
            rowSize -= 1;
            //bomChangeInfo为空没变更内容，不跨行
            if (rowIndex > rowSize) {
                rowSize = rowIndex;
            }
            if (rowSize > rowIndex) {
                //前三列跨行相同
                for (int i = 0; i < 3; i++) {
                    sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowSize, i, i));
                }
            }
            //序号
            String id = excelBean.getId();
            String[] ids = id.split("\\.");
            if (ids != null && ids.length == 1 && ids[0] != "0") {
                setCellStringValue(sheet, rowIndex, cellIndex, id, cellStyle2);
            } else {
                setCellStringValue(sheet, rowIndex, cellIndex, id, cellStyle3);
            }
            cellIndex++;
            //物料编码
            setCellStringValue(sheet, rowIndex, cellIndex, excelBean.getPartNumber(), cellStyle3);
            cellIndex++;
            //物料名称
            setCellStringValue(sheet, rowIndex, cellIndex, excelBean.getPartName(), cellStyle3);
            cellIndex++;

            Map<String, String> updateType = excelBean.getUpdateType();
            String typeValue = "";
            if (updateType != null && updateType.size() > 0) {
                //未发生变更只存在一个
                for (String type : updateType.keySet()) {
                    typeValue = updateType.get(type);
                    break;
                }
            }

            if (typeValue.equals(BomChangeConstants.TYPE_12)) {
                //没有发生变更，只有A版本
                //变更版本记录
                setCellStringValue(sheet, rowIndex, cellIndex, typeValue, cellStyle3);
                rowIndex++;
            } else {
                Map<String, Map<String, String>> changeVersionRecord = excelBean.getChangeVersionRecord();
                Integer startIndex = rowIndex;
                Integer endIndex;
                Integer changeVersionCellIndex = cellIndex;
                for (String flagId : changeVersionRecord.keySet()) {
                    List<BOMChangeInfoBean> bomChangeInfoBeanList = bomChangeInfo.get(flagId);
                    if (bomChangeInfoBeanList != null && bomChangeInfoBeanList.size() > 0) {
                        endIndex = startIndex + bomChangeInfoBeanList.size();
                    } else {
                        endIndex = startIndex + 1;
                    }
                    //多跨一行,减去
                    endIndex -= 1;
                    //bomChangeInfo为空没变更内容，不跨行
                    if (startIndex > endIndex) {
                        endIndex = startIndex;
                    }
                    if (endIndex > startIndex) {
                        //第四列到“变更类型”列前跨行相同
                        for (int i = 3; i < 15; i++) {
                            sheet.addMergedRegion(new CellRangeAddress(startIndex, endIndex, i, i));
                        }
                    }

                    //变更版本记录
                    Map<String, String> versionRecordMap = changeVersionRecord.get(flagId);
                    String before = versionRecordMap.get("before");
                    String after = versionRecordMap.get("after");
                    setCellStringValue(sheet, rowIndex, cellIndex, before + "→" + after, cellStyle3);
                    cellIndex++;
                    //ECN编号或修订
                    Map<String, String> typeMap = excelBean.getUpdateType();
                    String type = typeMap.get(flagId);
                    if (type.contains(BomChangeConstants.TYPE_10)) {
                        Map<String, WTChangeOrder2> ecn = excelBean.getEcn();
                        if (ecn != null) {
                            if (ecn.keySet().contains(flagId)) {
                                type = ecn.get(flagId).getNumber();
                            }
                        }
                    }
                    setCellStringValue(sheet, rowIndex, cellIndex, type, cellStyle3);
                    cellIndex++;
                    //变更申请人
                    Map<String, String> ecnCreatorMap = excelBean.getEcnCreator();
                    String ecnCreator = ecnCreatorMap.get(flagId);
                    setCellStringValue(sheet, rowIndex, cellIndex, ecnCreator, cellStyle3);
                    cellIndex++;
                    //申请时间
                    Map<String, String> ecnStartTimeMap = excelBean.getEcnStartTime();
                    String ecnStartTime = ecnStartTimeMap.get(flagId);
                    setCellStringValue(sheet, rowIndex, cellIndex, ecnStartTime, cellStyle3);
                    cellIndex++;
                    //所属产品类别
                    Map<String, String> productTypeMap = excelBean.getProductType();
                    String productType = productTypeMap.get(flagId);
                    setCellStringValue(sheet, rowIndex, cellIndex, productType, cellStyle3);
                    cellIndex++;
                    //所属项目
                    Map<String, String> projectNameMap = excelBean.getProjectName();
                    String projectName = projectNameMap.get(flagId);
                    setCellStringValue(sheet, rowIndex, cellIndex, projectName, cellStyle3);
                    cellIndex++;
                    //ECN变更类型
                    Map<String, String> changeTypeMap = excelBean.getChangeType();
                    String changeType = changeTypeMap.get(flagId);
                    setCellStringValue(sheet, rowIndex, cellIndex, changeType, cellStyle3);
                    cellIndex++;
                    //变更原因
                    Map<String, String> changeReasonMap = excelBean.getChangeReason();
                    String changeReaso = changeReasonMap.get(flagId);
                    setCellStringValue(sheet, rowIndex, cellIndex, changeReaso, cellStyle3);
                    cellIndex++;
                    //变更阶段
                    Map<String, String> changePhaseMap = excelBean.getChangePhase();
                    String changePhase = changePhaseMap.get(flagId);
                    setCellStringValue(sheet, rowIndex, cellIndex, changePhase, cellStyle3);
                    cellIndex++;
                    //是否变更图纸
                    Map<String, String> isChangeDrawingMap = excelBean.getIsChangeDrawing();
                    String isChangeDrawing = isChangeDrawingMap.get(flagId);
                    setCellStringValue(sheet, rowIndex, cellIndex, isChangeDrawing, cellStyle3);
                    cellIndex++;
                    //变更说明
                    Map<String, String> changeDescriptionMap = excelBean.getChangeDescription();
                    String changeDescription = changeDescriptionMap.get(flagId);
                    setCellStringValue(sheet, rowIndex, cellIndex, changeDescription, cellStyle5);
                    cellIndex++;
                    //当前物料更改说明
                    Map<String, String> changeDetailedDescriptionMap = excelBean.getChangeDetailedDescription();
                    String changeDetailedDescription = changeDetailedDescriptionMap.get(flagId);
                    setCellStringValue(sheet, rowIndex, cellIndex, changeDetailedDescription, cellStyle5);
                    cellIndex++;

                    //BOM变更内容
                    Integer bomRowIndex = rowIndex;
                    Integer bomCellIndex = cellIndex;
                    if (bomChangeInfoBeanList != null) {
                        for (int i = 0; i < bomChangeInfoBeanList.size(); i++) {
                            BOMChangeInfoBean bomChangeInfoBean = bomChangeInfoBeanList.get(i);
                            //变更类型
                            Set changeType1 = bomChangeInfoBean.getChangeType();
                            if (changeType1.contains(BomChangeConstants.TYPE_1) && changeType1.contains(BomChangeConstants.TYPE_2)) {
                                changeType1.remove(BomChangeConstants.TYPE_1);
                                changeType1.remove(BomChangeConstants.TYPE_2);
                                changeType1.add(BomChangeConstants.TYPE_7);
                            }
                            if (changeType1.contains(BomChangeConstants.TYPE_3) && changeType1.contains(BomChangeConstants.TYPE_4)) {
                                changeType1.remove(BomChangeConstants.TYPE_3);
                                changeType1.remove(BomChangeConstants.TYPE_4);
                                changeType1.add(BomChangeConstants.TYPE_8);
                            }
                            String typeName = "";
                            for (Object s : changeType1) {
                                typeName = typeName + "," + s;
                            }
                            if (typeName.length() > 0) {
                                typeName = typeName.substring(1);
                            }
                            setCellStringValue(sheet, bomRowIndex, cellIndex, typeName, cellStyle3);
                            cellIndex++;
                            //物料编码
                            String number = bomChangeInfoBean.getNumber();
                            setCellStringValue(sheet, bomRowIndex, cellIndex, number, cellStyle3);
                            cellIndex++;
                            //物料名称
                            String name = bomChangeInfoBean.getName();
                            setCellStringValue(sheet, bomRowIndex, cellIndex, name, cellStyle3);
                            cellIndex++;
                            //物料规格(父件规格属性变更则显示变更内容，不是则显示子件规格)
                            Map<String, String> parentSpecification = bomChangeInfoBean.getParentSpecification();
                            if (parentSpecification != null && parentSpecification.size() > 0) {
                                String before1 = parentSpecification.get("before");
                                String after1 = parentSpecification.get("after");
                                setCellStringValue(sheet, bomRowIndex, cellIndex, "变更前：" + before1 + "\n变更后：" + after1, cellStyle5);
                                cellIndex++;
                            } else {
                                //规格描述(子料)
                                String specification = bomChangeInfoBean.getSpecification();
                                setCellStringValue(sheet, bomRowIndex, cellIndex, specification, cellStyle5);
                                cellIndex++;
                            }
                            //位号(key：before 变更前;after 变更后)
                            Map<String, String> placeNumberMap = bomChangeInfoBean.getPlaceNumber();
                            if (placeNumberMap != null && placeNumberMap.size() > 0) {
                                if (typeName.contains(BomChangeConstants.TYPE_1) || typeName.contains(BomChangeConstants.TYPE_7)) {
                                    String after1 = placeNumberMap.get("after");
                                    setCellStringValue(sheet, bomRowIndex, cellIndex, after1, cellStyle5);
                                    cellIndex++;
                                } else if (typeName.contains(BomChangeConstants.TYPE_3) || typeName.contains(BomChangeConstants.TYPE_8)) {
                                    setCellStringValue(sheet, bomRowIndex, cellIndex, "", cellStyle5);
                                    cellIndex++;
                                } else if (typeName.contains(BomChangeConstants.TYPE_5)) {
                                    String before1 = placeNumberMap.get("before");
                                    String after1 = placeNumberMap.get("after");
                                    setCellStringValue(sheet, bomRowIndex, cellIndex, "变更前：" + before1 + "\n变更后：" + after1, cellStyle5);
                                    cellIndex++;
                                } else {
                                    cellIndex++;
                                }
                            } else {
                                cellIndex++;
                            }
                            //数量(key：before 变更前;after 变更后)
                            Map<String, String> quantitMap = bomChangeInfoBean.getQuantit();
                            if (quantitMap != null && quantitMap.size() > 0) {
                                if (typeName.contains(BomChangeConstants.TYPE_1) || typeName.contains(BomChangeConstants.TYPE_7)) {
                                    String after1 = quantitMap.get("after");
                                    setCellStringValue(sheet, bomRowIndex, cellIndex, after1, cellStyle3);
                                    cellIndex++;
                                } else if (typeName.contains(BomChangeConstants.TYPE_3) || typeName.contains(BomChangeConstants.TYPE_8)) {
                                    setCellStringValue(sheet, bomRowIndex, cellIndex, "", cellStyle3);
                                    cellIndex++;
                                } else if (typeName.contains(BomChangeConstants.TYPE_6)) {
                                    String before1 = quantitMap.get("before");
                                    String after1 = quantitMap.get("after");
                                    setCellStringValue(sheet, bomRowIndex, cellIndex, "变更前：" + before1 + "\n变更后：" + after1, cellStyle3);
                                    cellIndex++;
                                } else {
                                    cellIndex++;
                                }
                            } else {
                                cellIndex++;
                            }
                            //替代料(key：delete 删除的替代料;add 新增的替代料)
                            Map<String, List<String>> replacePartNumbersMap = bomChangeInfoBean.getReplacePartNumbers();
                            if (replacePartNumbersMap != null && replacePartNumbersMap.size() > 0) {
                                if (typeName.contains(BomChangeConstants.TYPE_2) && typeName.contains(BomChangeConstants.TYPE_4)) {
                                    List<String> addReplacePartNumberList = replacePartNumbersMap.get("add");
                                    List<String> delReplacePartNumberList = replacePartNumbersMap.get("delete");
                                    String add = "";
                                    String delete = "";
                                    if (addReplacePartNumberList != null && addReplacePartNumberList.size() > 0) {
                                        for (int k = 0; k < addReplacePartNumberList.size(); k++) {
                                            if (k != 0) {
                                                add = add + "," + addReplacePartNumberList.get(k);
                                            } else {
                                                add = addReplacePartNumberList.get(k);
                                            }
                                        }
                                    }
                                    if (delReplacePartNumberList != null && delReplacePartNumberList.size() > 0) {
                                        for (int k = 0; k < delReplacePartNumberList.size(); k++) {
                                            if (k != 0) {
                                                delete = delete + "," + delReplacePartNumberList.get(k);
                                            } else {
                                                delete = delReplacePartNumberList.get(k);
                                            }
                                        }
                                    }
                                    setCellStringValue(sheet, bomRowIndex, cellIndex, "新增替代料：" + add + "\n删除替代料：" + delete, cellStyle4);
                                    cellIndex++;
                                } else {
                                    List<String> addReplacePartNumberList = replacePartNumbersMap.get("add");
                                    List<String> delReplacePartNumberList = replacePartNumbersMap.get("delete");
                                    String replacePartNumber = "";
                                    if (addReplacePartNumberList != null && addReplacePartNumberList.size() > 0) {
                                        for (int k = 0; k < addReplacePartNumberList.size(); k++) {
                                            if (k != 0) {
                                                replacePartNumber = replacePartNumber + "," + addReplacePartNumberList.get(k);
                                            } else {
                                                replacePartNumber = addReplacePartNumberList.get(k);
                                            }
                                        }
                                    }
                                    if (delReplacePartNumberList != null && delReplacePartNumberList.size() > 0) {
                                        for (int k = 0; k < delReplacePartNumberList.size(); k++) {
                                            if (k != 0) {
                                                replacePartNumber = replacePartNumber + "," + delReplacePartNumberList.get(k);
                                            } else {
                                                replacePartNumber = delReplacePartNumberList.get(k);
                                            }
                                        }
                                    }
                                    setCellStringValue(sheet, bomRowIndex, cellIndex, replacePartNumber, cellStyle4);
                                    cellIndex++;
                                }
                            }
                            bomRowIndex++;
                            cellIndex = bomCellIndex;
                        }
                    }
                    rowIndex = endIndex + 1;
                    startIndex = rowIndex;
                    cellIndex = changeVersionCellIndex;
                }
            }
//            rowIndex = rowSize + 1;
        }
    }

    public void setCellStringValue(Sheet sheet, int rowIndex, int colIndex, String string, CellStyle cellStyle) {
        Cell cell = getCell(sheet, rowIndex, colIndex);
        cell.setCellStyle(cellStyle);
        RichTextString text = new XSSFRichTextString(string);
        cell.setCellValue(text);
    }

    public Cell getCell(Sheet sheet, int rowIndex, int colIndex) {
        Row row = getRow(sheet, rowIndex);
        Cell cell = getCell(row, colIndex);
        return cell;
    }

    public Cell getCell(Row row, int colIndex) {
        Cell cell = row.getCell((short) colIndex);
        if (cell == null) {
            cell = row.createCell((short) colIndex);
        }
        return cell;
    }

    public Row getRow(Sheet sheet, int index) {
        Row row = sheet.getRow(index);
        if (row == null) {
            row = sheet.createRow(index);
        }
        return row;
    }

    public CellStyle createCellStyle(Workbook workbook) {
        if (workbook == null) {
            return null;
        }
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(true);
        cellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);

        return cellStyle;
    }

    //设置背景色,居中
    public CellStyle createCellStyle2(Workbook workbook) {
        if (workbook == null) {
            return null;
        }
        CellStyle cellStyle = createCellStyle3(workbook);
        cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);//设置背景色
        cellStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());//设置背景色
        return cellStyle;
    }

    //居中
    public CellStyle createCellStyle3(Workbook workbook) {
        if (workbook == null) {
            return null;
        }
        CellStyle cellStyle = createCellStyle(workbook);
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);//垂直居中
        cellStyle.setAlignment(CellStyle.ALIGN_CENTER); //水平居中
        return cellStyle;
    }

    //垂直居上
    public CellStyle createCellStyle4(Workbook workbook) {
        if (workbook == null) {
            return null;
        }
        CellStyle cellStyle = createCellStyle(workbook);
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP);//垂直居上
        return cellStyle;
    }

    //垂直居上,字体颜色
    public CellStyle createCellStyle5(Workbook workbook) {
        if (workbook == null) {
            return null;
        }
        CellStyle cellStyle = createCellStyle(workbook);
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP);//垂直居上
        Font font = workbook.createFont();
        font.setColor(IndexedColors.TEAL.getIndex());
        cellStyle.setFont(font);
        return cellStyle;
    }

}
