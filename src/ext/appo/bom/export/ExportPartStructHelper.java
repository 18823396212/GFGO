package ext.appo.bom.export;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import wt.fc.ReferenceFactory;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.part.WTPart;
import wt.util.WTException;
import ext.appo.bom.beans.ImportProductStructureExcelBean;
import ext.appo.bom.util.ImportProductStructureExcelReader;
import ext.lang.PIStringUtils;
import ext.pi.core.PICoreHelper;

public class ExportPartStructHelper implements RemoteAccess, Serializable{

	private static final long serialVersionUID = 1L;
	
	private static final String CLASSNAME = ExportPartStructHelper.class.getName() ;
	private static final Logger LOG = LogR.getLogger(CLASSNAME) ;
	
	private String filePath = null;
	
	public ExportPartStructHelper() {
		try {
			String codebase = PICoreHelper.service.getCodebase();
			filePath = codebase + File.separator + "ext" + File.separator + "appo" + File.separator + "bom"
					+ File.separator + "export" + File.separator + "exportBomTemplate.xlsx";
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/***
	 * 导出BOM结构主方法
	 * 
	 * @param request
	 * @param response
	 * @throws WTException
	 */
	public void exportData(HttpServletRequest request, HttpServletResponse response) throws WTException{
		if(request == null || response == null || PIStringUtils.isNull(this.filePath)){
			LOG.error("Parameter is null.");
			return ;
		}
		
		OutputStream os = null ;
		try {
			// 操作主对象
			WTPart part = (WTPart)((new ReferenceFactory()).getReference(request.getParameter("oid")).getObject()) ;
			
			// 导出参数配置
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/x-msdownload;charset=UTF-8");
			response.setHeader("Content-disposition", "attachment; filename="+ part.getNumber() + ".xlsx");
			// 获取导出模板
			ImportProductStructureExcelReader reader = new ImportProductStructureExcelReader(this.filePath) ;
			Workbook workbook = reader.getWorkbook() ;
			// 提取BOM信息并将该信息转化为可导出的数据对象
			ExportPartStructService service = new ExportPartStructService() ;
			List<ImportProductStructureExcelBean> extractDatas = service.extractDatas(part) ;
			if(LOG.isDebugEnabled()){
				LOG.debug("extractDatas : " + extractDatas);
			}
			
			// 写入数据
			if(extractDatas != null && extractDatas.size() > 0){
				writeData(workbook, workbook.getSheetAt(0), extractDatas) ;
			}
			
			// 获取输出流
			os = response.getOutputStream();
			// 写入文件流
			workbook.write(os); 
		} catch (Exception e) {
			throw new WTException(e.getLocalizedMessage()) ;
		} finally {
			try {
				if(os != null){
					os.flush();
					os.close() ;
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
	public void writeData(Workbook workbook, Sheet sheet, List<ImportProductStructureExcelBean> extractDatas) {
		if(sheet == null || extractDatas == null || extractDatas.size() == 0){
			return ;
		}
		
		// 创建边框样式
		CellStyle cellStyle = createCellStyle(workbook) ;
		
		Integer rowIndex = 4 ;
		for(ImportProductStructureExcelBean excelBean : extractDatas){
			Integer cellIndex = 0 ;
			
			setCellStringValue(sheet, rowIndex, cellIndex, excelBean.getSerial(), cellStyle);
			cellIndex ++ ;
			
			setCellStringValue(sheet, rowIndex, cellIndex, excelBean.getLevel(), cellStyle);
			cellIndex ++ ;
			
			setCellStringValue(sheet, rowIndex, cellIndex, excelBean.getNumber(), cellStyle);
			cellIndex ++ ;
			
			setCellStringValue(sheet, rowIndex, cellIndex, excelBean.getReplaceNumber(), cellStyle);
			cellIndex ++ ;
			
			setCellStringValue(sheet, rowIndex, cellIndex, excelBean.getReplaceType(), cellStyle);
			cellIndex ++ ;
			
			setCellStringValue(sheet, rowIndex, cellIndex, excelBean.getPartVersion(), cellStyle);
			cellIndex ++ ;
			
			setCellStringValue(sheet, rowIndex, cellIndex, excelBean.getPartName(), cellStyle);
			cellIndex ++ ;
			
			setCellStringValue(sheet, rowIndex, cellIndex, excelBean.getGgms(), cellStyle);
			cellIndex ++ ;
			
			setCellStringValue(sheet, rowIndex, cellIndex, excelBean.getZdchdj(), cellStyle);
			cellIndex ++ ;
			
			setCellStringValue(sheet, rowIndex, cellIndex, excelBean.getUnit(), cellStyle);
			cellIndex ++ ;
			
			setCellStringValue(sheet, rowIndex, cellIndex, excelBean.getQuantity(), cellStyle);
			cellIndex ++ ;
			
			setCellStringValue(sheet, rowIndex, cellIndex, excelBean.getSiteLine(), cellStyle);
			cellIndex ++ ;
			
			setCellStringValue(sheet, rowIndex, cellIndex, excelBean.getRemark(), cellStyle);
			rowIndex ++ ;
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
	
	public CellStyle createCellStyle(Workbook workbook){
		if(workbook == null){
			return null ;
		}
		
		CellStyle cellStyle = workbook.createCellStyle() ;
		cellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
		cellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
		cellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
		cellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
		
		return cellStyle ;
	}
}
