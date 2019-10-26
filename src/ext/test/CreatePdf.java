package ext.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import ext.appo.ecn.pdf.AffectedItemBean;
import ext.appo.ecn.pdf.AffectedProductBean;
import ext.appo.ecn.pdf.PdfBean;
import ext.appo.ecn.pdf.PdfUtil;
import ext.appo.ecn.pdf.ReviewBean;
import wt.change2.WTChangeOrder2;
import wt.log4j.LogR;
import wt.session.SessionServerHelper;
import wt.util.WTException;

public class CreatePdf {

	private static final String CLASSNAME = CreatePdf.class.getName();
	private static final Logger LOG = LogR.getLogger(CLASSNAME);

	Document document = new Document();// 建立一个Document对象

	private static Font headfont;// 设置字体大小
	private static Font keyfont;// 设置字体大小
	private static Font textfont;// 设置字体大小

	static {
		// 中文格式
		BaseFont bfChinese;
		try {
			// 设置中文显示
			String font_cn = getChineseFont();
			bfChinese = BaseFont.createFont(font_cn + ",1", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

			headfont = new Font(bfChinese, 18, Font.BOLD);// 设置字体大小
			keyfont = new Font(bfChinese, 13, Font.BOLD);// 设置字体大小m
			textfont = new Font(bfChinese, 8, Font.NORMAL);// 设置字体大小
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取中文字体位置
	 * 
	 * @return
	 * @author
	 */
	private static String getChineseFont() {

		// 宋体（对应css中的 属性 font-family: SimSun; /*宋体*/）
		String font1 = "C:/Windows/Fonts/simsun.ttc";

		// 判断系统类型，加载字体文件
		java.util.Properties prop = System.getProperties();
		String osName = prop.getProperty("os.name").toLowerCase();
		System.out.println(osName);
		if (osName.indexOf("linux") > -1) {
			font1 = "/usr/share/fonts/simsun.ttc";
		}
		if (!new File(font1).exists()) {
			throw new RuntimeException("字体文件不存在,影响导出pdf中文显示！" + font1);
		}
		return font1;
	}

	/**
	 * 文成文件
	 * 
	 * @param file
	 *            待生成的文件名
	 */
	public CreatePdf(File file) {
		document.setPageSize(PageSize.A4);// 设置页面大小
		try {
			PdfWriter.getInstance(document, new FileOutputStream(file));
			document.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public CreatePdf() {

	}

	public void initFile(File file) {
		document.setPageSize(PageSize.A4);// 设置页面大小
		try {
			PdfWriter.getInstance(document, new FileOutputStream(file));
			document.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	int maxWidth = 520;

	/**
	 * 为表格添加一个内容
	 * 
	 * @param value
	 *            值
	 * @param font
	 *            字体
	 * @param align
	 *            对齐方式
	 * @return 添加的文本框
	 */
	public PdfPCell createCell(String value, Font font, int align) {
		PdfPCell cell = new PdfPCell();
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setHorizontalAlignment(align);
		cell.setPhrase(new Phrase(value, font));
		return cell;
	}

	/**
	 * 为表格添加一个内容
	 * 
	 * @param value
	 *            值
	 * @param font
	 *            字体
	 * @return 添加的文本框
	 */
	public PdfPCell createCell(String value, Font font) {
		PdfPCell cell = new PdfPCell();
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setPhrase(new Phrase(value, font));
		return cell;
	}

	/**
	 * 为表格添加一个内容
	 * 
	 * @param value
	 *            值
	 * @param font
	 *            字体
	 * @param align
	 *            对齐方式
	 * @param colspan
	 *            占多少列
	 * @return 添加的文本框
	 */
	public PdfPCell createCell(String value, Font font, int align, int colspan) {
		PdfPCell cell = new PdfPCell();
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setHorizontalAlignment(align);
		cell.setColspan(colspan);
		cell.setPhrase(new Phrase(value, font));
		return cell;
	}

	/**
	 * 为表格添加一个内容
	 * 
	 * @param value
	 *            值
	 * @param font
	 *            字体
	 * @param align
	 *            对齐方式
	 * @param colspan
	 *            占多少列
	 * @param boderFlag
	 *            是否有有边框
	 * @return 添加的文本框
	 */
	public PdfPCell createCell(String value, Font font, int align, int colspan, boolean boderFlag) {
		PdfPCell cell = new PdfPCell();
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setHorizontalAlignment(align);
		cell.setColspan(colspan);
		cell.setPhrase(new Phrase(value, font));
		cell.setPadding(3.0f);
		if (!boderFlag) {
			cell.setBorder(0);
			cell.setPaddingTop(15.0f);
			cell.setPaddingBottom(8.0f);
		}
		return cell;
	}

	/**
	 * 创建一个表格对象
	 * 
	 * @param colNumber
	 *            表格的列数
	 * @return 生成的表格对象
	 */
	public PdfPTable createTable(int colNumber) {
		PdfPTable table = new PdfPTable(colNumber);
		try {
			table.setTotalWidth(maxWidth);
			table.setLockedWidth(true);
			table.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.getDefaultCell().setBorder(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}

	public PdfPTable createTable(float[] widths) {
		PdfPTable table = new PdfPTable(widths);
		try {
			table.setTotalWidth(maxWidth);
			table.setLockedWidth(true);
			table.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.getDefaultCell().setBorder(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}

	public PdfPTable createBlankTable() {
		PdfPTable table = new PdfPTable(1);
		table.getDefaultCell().setBorder(0);
		table.addCell(createCell("", keyfont));
		table.setSpacingAfter(20.0f);
		table.setSpacingBefore(20.0f);
		return table;
	}

	public <T> void generatePDF(WTChangeOrder2 eco) throws WTException, DocumentException, ParseException {
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false); // 忽略权限
		PdfBean bean = PdfUtil.testPdfBean(eco);
		String[] ecnName = { "ECN名称：" + bean.getECName() };
		String[] val1 = { "发出日期：" + bean.getSendDate(), "ECN编号No:" + bean.getECNumber(),
				"ECN发起人：" + bean.getSendPersion() };
		String[] val2 = { "所属产品类别：" + bean.getChangeAtt("sscpx"), "所属项目:" + bean.getChangeAtt("ssxm"),
				"变更类型：" + bean.getChangeAtt("ChangeItemType") };
		String[] val3 = { "变更阶段：" + bean.getChangeAtt("bgjd"), "是否变更图纸:" + bean.getChangeAtt("change_dwg_ornot") };
		String changeReason = "变更原因：" + bean.getChangeComment();
		String changeDes = "变更原因说明:" + bean.getComment();
		String signer = "会签人：" + bean.getHQs();
		String affObj = "受影响对象";

		String[] headObj = { "名称", "料号", "版本", "状态", "类型", "说明", "在制数量", "在制处理措施", "在途数量", "在途处理措施", "库存数量", "库存处理措施",
				"已出货处理措施", "期望完成时间", "责任人", "收集对象" };
		String affProduct = "受影响产品";
		String[] headProduct = { "名称", "编号", "版本", "状态", "规格描述" };
		String tasks = "事务性任务";
		String[] headTasks = { "任务主题", "任务描述", "期望完成时间", "责任人" };
		String review = "签审流程";
		String[] headReview = { "审核节点", "审核人", "签批时间", "签审人", "签审意见", "备注" };

		new PdfUtil();

		PdfPTable table15 = createTable(ecnName.length);
		table15.addCell(createCell(bean.getPdfTitle(), headfont, Element.ALIGN_CENTER, ecnName.length, true));

		PdfPTable table6 = createTable(ecnName.length);
		for (int i = 0; i < ecnName.length; i++) {
			table6.addCell(createCell(ecnName[i], textfont, Element.ALIGN_LEFT));
		}
		// float[] wid6 ={0.10f,0.90f}; //两列宽度的比例
		// table6.setWidths(wid6);

		PdfPTable table1 = createTable(val1.length);
		for (int i = 0; i < val1.length; i++) {
			table1.addCell(createCell(val1[i], textfont, Element.ALIGN_LEFT));
		}

		PdfPTable table2 = createTable(1);
		table2.addCell(createCell(changeReason, textfont, Element.ALIGN_LEFT));

		PdfPTable table7 = createTable(1);
		table7.addCell(createCell(changeDes, textfont, Element.ALIGN_LEFT));

		PdfPTable table16 = createTable(val2.length);
		for (int i = 0; i < val2.length; i++) {
			table16.addCell(createCell(val2[i], textfont, Element.ALIGN_LEFT));
		}
		PdfPTable table17 = createTable(val3.length);
		for (int i = 0; i < val3.length; i++) {
			table17.addCell(createCell(val3[i], textfont, Element.ALIGN_LEFT));
		}
		PdfPTable table3 = createTable(1);
		table3.addCell(createCell(signer, textfont, Element.ALIGN_LEFT));

		PdfPTable table4 = createTable(1);
		table4.addCell(createCell(affObj, keyfont, Element.ALIGN_CENTER));

		PdfPTable table5 = createTable(headObj.length);
		// 设置表头
		for (int i = 0; i < headObj.length; i++) {
			table5.addCell(createCell(headObj[i], textfont, Element.ALIGN_CENTER));
		}

		List<AffectedItemBean> listAffBean = bean.getAibs();
		for (AffectedItemBean affectedItemBean : listAffBean) {
			String[] affectedItem = { affectedItemBean.getName(), affectedItemBean.getNumber(),
					affectedItemBean.getVersion(), affectedItemBean.getState(), affectedItemBean.getType(),
					affectedItemBean.getDes(), affectedItemBean.getInQuantity(), affectedItemBean.getInTreatment(),
					affectedItemBean.getWayQuantity(), affectedItemBean.getWayTreatment(),
					affectedItemBean.getStockQuantity(), affectedItemBean.getStockTreatment(),
					affectedItemBean.getShipmentsTreatment(), affectedItemBean.getExpectDate(),
					affectedItemBean.getPersonLiable(), affectedItemBean.getCollectObj() };
			for (int i = 0; i < affectedItem.length; i++) {
				table5.addCell(createCell(affectedItem[i], textfont, Element.ALIGN_CENTER));
			}
		}

		float[] wid5 = { 0.10f, 0.11f, 0.04f, 0.06f, 0.04f, 0.09f, 0.04f, 0.04f, 0.04f, 0.04f, 0.04f, 0.04f, 0.04f,
				0.10f, 0.06f, 0.12f }; // 两列宽度的比例
		table5.setWidths(wid5);

		PdfPTable table8 = createTable(1);
		table8.addCell(createCell(affProduct, keyfont, Element.ALIGN_CENTER));

		PdfPTable table9 = createTable(headProduct.length);
		for (int i = 0; i < headProduct.length; i++) {
			table9.addCell(createCell(headProduct[i], textfont, Element.ALIGN_CENTER));
		}

		List<AffectedProductBean> listAffProductBean = bean.getApbs();
		for (AffectedProductBean affectedProductBean : listAffProductBean) {
			String[] affectedProduct = { affectedProductBean.getName(), affectedProductBean.getNumber(),
					affectedProductBean.getVersion(), affectedProductBean.getState(), affectedProductBean.getGGSM() };
			for (int j = 0; j < affectedProduct.length; j++) {
				table9.addCell(createCell(affectedProduct[j], textfont, Element.ALIGN_CENTER));
			}
		}
		float[] wid9 = { 0.25f, 0.15f, 0.10f, 0.10f, 0.40f }; // 两列宽度的比例
		table9.setWidths(wid9);

		PdfPTable table10 = createTable(1);
		table10.addCell(createCell(tasks, keyfont, Element.ALIGN_CENTER));

		PdfPTable table11 = createTable(headTasks.length);
		for (int i = 0; i < headTasks.length; i++) {
			table11.addCell(createCell(headTasks[i], textfont, Element.ALIGN_CENTER));
		}

		List<Map<String, String>> listSwxrw = bean.getSwxrw();
		for (int i = 0; i < listSwxrw.size(); i++) {
			Map<String, String> map = listSwxrw.get(i);
			String[] swxrw = { map.get("taskTitle"), map.get("taskDesc"), map.get("taskEndTime"),
					map.get("taskPersion") };
			for (int j = 0; j < swxrw.length; j++) {
				table11.addCell(createCell(swxrw[j], textfont, Element.ALIGN_CENTER));
			}
		}

		float[] wid11 = { 0.25f, 0.40f, 0.20f, 0.15f }; // 两列宽度的比例
		table11.setWidths(wid11);

		PdfPTable table12 = createTable(1);
		table12.addCell(createCell(review, keyfont, Element.ALIGN_CENTER));

		PdfPTable table13 = createTable(headReview.length);
		for (int i = 0; i < headReview.length; i++) {
			table13.addCell(createCell(headReview[i], textfont, Element.ALIGN_CENTER));
		}

		List<ReviewBean> listReviewBeans = bean.getRbs();
		for (ReviewBean reviewBean : listReviewBeans) {
			String[] reviewVal = { reviewBean.getName(), reviewBean.getExamineUser(), reviewBean.getCompleteTime(),
					reviewBean.getCompleteName(), reviewBean.getReviewOpinion(), reviewBean.getComment() };
			for (int j = 0; j < reviewVal.length; j++) {
				table13.addCell(createCell(reviewVal[j], textfont, Element.ALIGN_CENTER));
			}
		}
		float[] wid13 = { 0.10f, 0.13f, 0.14f, 0.13f, 0.10f, 0.40f }; // 两列宽度的比例
		table13.setWidths(wid13);

		// Date date = new Date();
		// String year = String.format("%tY", date);
		// String month = String.format("%tB", date);
		// String day = String.format("%te", date);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		PdfPTable table20 = createTable(ecnName.length);
		table20.addCell(
				createCell("打印时间：" + sdf.format(new Date()), textfont, Element.ALIGN_RIGHT, ecnName.length, false));

		try {
			// 将表格添加到文档中
			document.add(table15);
			document.add(table6);
			document.add(table1);
			document.add(table2);
			document.add(table7);
			document.add(table16);
			document.add(table17);
			document.add(table3);
			document.add(table4);
			document.add(table5);
			document.add(table8);
			document.add(table9);
			document.add(table10);
			document.add(table11);
			document.add(table12);
			document.add(table13);
			document.add(table20);
		} catch (DocumentException e) {
			e.printStackTrace();
		} finally {
			SessionServerHelper.manager.setAccessEnforced(flag); // 添加权限
		}

		// 关闭流
		document.close();
	}

	/**
	 * 提供外界调用的接口，生成以head为表头，list为数据的pdf
	 * 
	 * @param head
	 *            //数据表头
	 * @param list
	 *            //数据
	 * @return //excel所在的路径
	 * @throws WTException
	 * @throws DocumentException
	 * @throws ParseException
	 */
	public <T> String generatePDFs(WTChangeOrder2 eco) throws WTException, DocumentException, ParseException {

		final String FilePath = "pdfPath";
		String saveFilePathAndName = "";

		// 获得存储的根目录
		// String savePath = new
		// GetFilePlace().getFileDirFromProperties(FilePath);
		String savePath = "/ptc/Windchill_11.0/Windchill/temp";

		// 获得当天存储的路径,不存在则生成当天的文件夹
		String realSavePath = new GenerateFold().getFold(savePath);

		saveFilePathAndName = new GenerateFileName().generateFileName(realSavePath, "pdf");

		File file = new File(saveFilePathAndName);
		try {
			file.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		initFile(file);
		try {
			file.createNewFile(); // 生成一个pdf文件
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// new CreatePdf().generatePDFa(val1,val1.length);
		new CreatePdf(file).generatePDF(eco);

		return saveFilePathAndName;
	}

}
