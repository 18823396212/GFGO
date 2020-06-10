package ext.appo.change.report;

import ext.appo.change.beans.ChangeTaskInfoBean;
import ext.appo.change.beans.EcnChangeTaskBean;
import ext.appo.statReport.statChangeInfo.bean.ChangeInfoBean;
import ext.appo.util.DateUtil;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

/**
 * 此类用于提供事务性任务跟踪报表的生成
 */
public class ChangeInfoExport {

    private static final Logger LOGGER = Logger.getLogger(ChangeInfoExport.class);

    public static String SheetName = "事务性任务跟踪报表";
    /*
     * 表头
     */
    private static final String[] tableItem = {"序号", "ECN编号", "ECN变更原因说明", "任务类型", "任务主题",
            "管理方式", "任务描述", "计划完成时间", "责任人", "状态", "任务单号", "实际完成时间"};
    // 创建工作本
    public HSSFWorkbook workBook = new HSSFWorkbook();
    // 创建表
    public HSSFSheet sheet = workBook.createSheet(SheetName);
    // 表头的单元格个数目
    public static final short colSum = (short) tableItem.length;

    /**
     * 创建表头信息
     */
    public void createSheedHead() {

        HSSFRow row0 = sheet.createRow(0);
        HSSFRow row1 = sheet.createRow(1);
        sheet.setColumnWidth(1,6000);
        sheet.setColumnWidth(2,9000);
        sheet.setColumnWidth(3,6000);
        sheet.setColumnWidth(4,6000);
        sheet.setColumnWidth(5,6000);
        sheet.setColumnWidth(6,9000);
        sheet.setColumnWidth(7,6000);
        sheet.setColumnWidth(8,6000);
        sheet.setColumnWidth(9,5000);
        sheet.setColumnWidth(10,6000);
        sheet.setColumnWidth(11,6000);
        HSSFCellStyle cellStyle = this.setCellStyle(workBook);

        row1.setHeight((short) 500);

        // 设置单元格字体
        HSSFFont font = workBook.createFont();
        setFontStyle(font, "宋体", HSSFFont.BOLDWEIGHT_BOLD, (short) 220);
        cellStyle.setFont(font);

        HSSFCell cell = null;

        // 创建文档时间表头栏位
        Date date = new Date();
        String value = DateUtil.formatDate(date, "yyyy-MM-dd HH:mm");
        this.createCell(cell, row0, (short) 0, cellStyle, value);

        // 创建变更详细信息表头
        for (int j = 0; j < tableItem.length; j++) {
            short col_index2 = (short) (j);
            this.createCell(cell, row1, col_index2, cellStyle, tableItem[j]);
        }
    }

    /**
     * 写入事务性任务跟踪报表信息
     *
     * @param changeInfos
     */
    public void writeSheetContent(List<EcnChangeTaskBean> changeInfos) {

        HSSFCellStyle cellStyle = this.setCellStyle(workBook);
        if (changeInfos != null && changeInfos.size() > 0) {
            // 循环结果插入数据到表格
            // 从第三行开始插入数据
            Integer id = 1;//序号
            Integer rowIndex = 2;
            for (int i = 0; i < changeInfos.size(); i++) {
                EcnChangeTaskBean bean = changeInfos.get(i);
                List<ChangeTaskInfoBean> changeTaskBeans = bean.getChangeTaskBeans();
                //第2~3列跨行相同
                int rowSize = 1;
                if (changeTaskBeans != null && changeTaskBeans.size() > 1) {
                    rowSize = rowIndex + changeTaskBeans.size() - 1;
                    for (int j = 1; j < 3; j++) {
                        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowSize, j, j));
                    }
                    for (int j = 0; j < changeTaskBeans.size(); j++) {
                        ChangeTaskInfoBean changeTaskBean=changeTaskBeans.get(j);
                        HSSFRow row = sheet.createRow(rowIndex);
                        HSSFCell cell = null;
                        // 创建单元格并写入信息
                        this.createCell(cell, row, (short) 0, cellStyle, id + "");
                        if (j == 0) {
                            this.createCell(cell, row, (short) 1, cellStyle, bean.getEcnNumber());
                            this.createCell(cell, row, (short) 2, cellStyle, bean.getChangeDescription());
                        }
                        this.createCell(cell, row, (short) 3, cellStyle, changeTaskBean.getTaskType());
                        this.createCell(cell, row, (short) 4, cellStyle, changeTaskBean.getTaskTheme());
                        this.createCell(cell, row, (short) 5, cellStyle, changeTaskBean.getGlfs());
                        this.createCell(cell, row, (short) 6, cellStyle, changeTaskBean.getChangeDescribe());
                        this.createCell(cell, row, (short) 7, cellStyle, changeTaskBean.getNeedDate());
                        this.createCell(cell, row, (short) 8, cellStyle, changeTaskBean.getResponsible());
                        this.createCell(cell, row, (short) 9, cellStyle, changeTaskBean.getTaskState());
                        this.createCell(cell, row, (short) 10, cellStyle, changeTaskBean.getTaskNumber());
                        this.createCell(cell, row, (short) 11, cellStyle, changeTaskBean.getActualDate());
                        id++;
						rowIndex++;
                    }
                } else {
					HSSFRow row = sheet.createRow(rowIndex);
					HSSFCell cell = null;
					// 创建单元格并写入信息
					this.createCell(cell, row, (short) 0, cellStyle, id + "");
					this.createCell(cell, row, (short) 1, cellStyle, bean.getEcnNumber());
					this.createCell(cell, row, (short) 2, cellStyle, bean.getChangeDescription());
					if (changeTaskBeans!=null&&changeTaskBeans.size()>0){
                        ChangeTaskInfoBean changeTaskBean=changeTaskBeans.get(0);
						this.createCell(cell, row, (short) 3, cellStyle, changeTaskBean.getTaskType());
						this.createCell(cell, row, (short) 4, cellStyle, changeTaskBean.getTaskTheme());
						this.createCell(cell, row, (short) 5, cellStyle, changeTaskBean.getGlfs());
						this.createCell(cell, row, (short) 6, cellStyle, changeTaskBean.getChangeDescribe());
						this.createCell(cell, row, (short) 7, cellStyle, changeTaskBean.getNeedDate());
						this.createCell(cell, row, (short) 8, cellStyle, changeTaskBean.getResponsible());
						this.createCell(cell, row, (short) 9, cellStyle, changeTaskBean.getTaskState());
						this.createCell(cell, row, (short) 10, cellStyle, changeTaskBean.getTaskNumber());
						this.createCell(cell, row, (short) 11, cellStyle, changeTaskBean.getActualDate());
					}
					id++;
					rowIndex++;
                }
            }
        }
    }

    /**
     * 创建单元表格
     *
     * @param cell
     * @param row
     * @param index
     * @param cellStyle
     * @param value
     */
    public void createCell(HSSFCell cell, HSSFRow row, Short index, HSSFCellStyle cellStyle, String value) {

        cell = row.createCell(index);
        cell.setCellType(HSSFCell.ENCODING_UTF_16);
        cell.setCellStyle(cellStyle);
        cell.setCellValue(new HSSFRichTextString(value));
    }

    /**
     * 设定工作表单元格样式
     *
     * @param workBook
     * @return
     */
    public HSSFCellStyle setCellStyle(HSSFWorkbook workBook) {

        HSSFCellStyle cellStyle = workBook.createCellStyle();
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT); // 指定单元格居中对齐
        cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 指定单元格垂直居中对齐
        return cellStyle;
    }

    /**
     * 将查询结果导出成Excel文档
     *
     * @param HttpServletRequest request, HttpServletResponse response
     */
    public void exportSearchResult(HttpServletRequest request, HttpServletResponse response,
                                   List<EcnChangeTaskBean> changeInfos) {

        OutputStream os = null;
        try {
            this.createSheedHead();
            this.writeSheetContent(changeInfos);

            response.setContentType("charset=utf-8");
            response.setHeader("Content-Type", "application/x-msdownload");
            response.addHeader("Content-Disposition", "attachment;filename=changeInfoExport.xls");
            os = response.getOutputStream();
            this.workBook.write(os);
            os.flush();

        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 设置单元格字体
     *
     * @param font
     * @param name
     * @param boldweight
     * @param height
     */
    public static void setFontStyle(HSSFFont font, String name, short boldweight, short height) {

        // 设置单元格字体
        font.setFontName(name);
        font.setBoldweight(boldweight);
        font.setFontHeight(height);
    }
}
