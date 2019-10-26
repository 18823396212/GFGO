/** *********************************************************************** */
/*                                                                          */
/* Copyright (c) 2008-2012 YULONG Company */
/* 宇龙计算机通信科技（深圳）有限公司版权所有 2008-2012 */
/*                                                                          */
/* PROPRIETARY RIGHTS of YULONG Company are involved in the */
/* subject matter of this material. All manufacturing, reproduction, use, */
/* and sales rights pertaining to this subject matter are governed by the */
/* license agreement. The recipient of this software implicitly accepts */
/* the terms of the license. */
/* 本软件文档资料是宇龙公司的资产，任何人士阅读和使用本资料必须获得 */
/* 相应的书面授权，承担保密责任和接受相应的法律约束。 */
/*                                                                          */
/** *********************************************************************** */

/**
 * <pre>
 * 系统缩写：PLM 
 * 系统名称：产品生命周期管理系统 
 * 组件编号：C_变更管理
 * 组件名称：变更管理
 * 文件名称：StatChangeInfoExport.java 
 * 作         者: 裴均宇
 * 生成日期：2011-09-09
 * </pre>
 */

/**
 * <pre>
 * 修改记录：01 
 * 修改日期：2011-09-09
 * 修  改   人：裴均宇
 * 关联活动：IT-DB00040092 C_变更管理_变更过程看板_代码开发_peijunyu
 * 修改内容：初始化
 * </pre>
 */

/**
 * <pre>
 * 修改记录：02 
 * 修改日期：2014-10-20
 * 修  改   人：何成峰
 * 关联活动：IT-DB00040092 C_变更管理_变更信息统计导出格式
 * 修改内容：导出 的报表中添加一行“机型”  修改方法：createSheedHead();  writeSheetContent(List<List> changeInfos);
 * </pre>
 */
package ext.appo.statReport.statChangeInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import ext.appo.statReport.statChangeInfo.bean.ChangeInfoBean;
import ext.appo.util.DateUtil;

/**
 * 此类用于提供变更信息报表的生成
 */
public class StatChangeInfoExport {

	private static final Logger LOGGER = Logger.getLogger(StatChangeInfoExport.class);

	public static String SheetName = "变更信息统计";
	/*
	 * 表头
	 */
	private static final String[] tableItem = { "ECN创建者", "ECN编号", "ECN名称", "变更原因", "变更原因说明", "所属产品类别", "所属项目",
			"受影响对象状态", "受影响对象编号", "受影响对象名称", "受影响对象版本", "在制数量", "在制处理措施", "在途数量", "在途处理措施", "库存数量", "库存处理措施" };
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
	 * 写入变更异常统计信息
	 * 
	 * @param changeInfos
	 */
	@SuppressWarnings("rawtypes")
	public void writeSheetContent(List<ChangeInfoBean> changeInfos) {

		HSSFCellStyle cellStyle = this.setCellStyle(workBook);
		if (changeInfos != null && changeInfos.size() > 0) {
			// 循环结果插入数据到表格
			for (int i = 0; i < changeInfos.size(); i++) {
				// 从第二行开始插入数据
				HSSFRow row = sheet.createRow(i + 2);
				HSSFCell cell = null;
				ChangeInfoBean bean = changeInfos.get(i);
				// 标志表格单元格位置（统计插入数据的位置，j为变更对象位置）

				// 创建变更信息的单元格并写入信息
				this.createCell(cell, row, (short) 0, cellStyle, bean.getEcnCreator());
				this.createCell(cell, row, (short) 1, cellStyle, bean.getEcnNumber());
				this.createCell(cell, row, (short) 2, cellStyle, bean.getEcnName());
				this.createCell(cell, row, (short) 3, cellStyle, bean.getChangeReason());
				this.createCell(cell, row, (short) 4, cellStyle, bean.getChangeReasonDes());
				this.createCell(cell, row, (short) 5, cellStyle, bean.getProductType());
				this.createCell(cell, row, (short) 6, cellStyle, bean.getProjectName());
				this.createCell(cell, row, (short) 7, cellStyle, bean.getEffectObjectState());
				this.createCell(cell, row, (short) 8, cellStyle, bean.getEffectObjectNo());
				this.createCell(cell, row, (short) 9, cellStyle, bean.getEffectObjectName());
				this.createCell(cell, row, (short) 10, cellStyle, bean.getEffectObjectVesion());
				this.createCell(cell, row, (short) 11, cellStyle, bean.getInProcessQuantities());
				this.createCell(cell, row, (short) 12, cellStyle, bean.getProcessingMeasures());
				this.createCell(cell, row, (short) 13, cellStyle, bean.getOnthewayQuantity());
				this.createCell(cell, row, (short) 14, cellStyle, bean.getOnthewayTreatmentMeasure());
				this.createCell(cell, row, (short) 15, cellStyle, bean.getStockQuantity());
				this.createCell(cell, row, (short) 16, cellStyle, bean.getStockTreatmentMeasure());
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
	 * @param HttpServletRequest
	 *            request, HttpServletResponse response
	 * 
	 */
	public void exportSearchResult(HttpServletRequest request, HttpServletResponse response,
			List<ChangeInfoBean> changeInfos) {

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
