package ext.appo.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;

/**
 * 
 * Read 2003 Excel.
 * 
 * 
 * @version 1.0
 * @author jhong
 * 
 */
public class Excel2003Handler implements ExcelHandler {

    /** POI Workbook */
    private HSSFWorkbook workbook = null;
    /** Excel file */
    private File excelFile = null;
    /** current Sheet */
    private HSSFSheet currentSheet = null;

    public Excel2003Handler() {
    }

    /**
     * init constructor
     * 
     * @param filePathName
     * @throws IOException
     */
    public Excel2003Handler(String filePathName) throws IOException {
        POIFSFileSystem poiFileSystem = null;
        excelFile = new File(filePathName);
        excelFile.getName();
        // check file is exist or not
        if (excelFile.exists()) {
            FileInputStream inputStream =  new FileInputStream(excelFile);
            poiFileSystem = new POIFSFileSystem(inputStream);
        }
        if (poiFileSystem != null) {
            workbook = new HSSFWorkbook(poiFileSystem);
            currentSheet = workbook.getSheetAt(0);
        }
    }

    /**
     * init constructor
     * 
     * @param file
     * @throws IOException
     */
    public Excel2003Handler(File file) throws IOException {
        POIFSFileSystem poiFSFileSystem = null;
        excelFile = file;
        // check file is exist or not
        if (excelFile.exists()) {
            poiFSFileSystem = new POIFSFileSystem(new FileInputStream(excelFile));
        }
        if (poiFSFileSystem != null) {
            workbook = new HSSFWorkbook(poiFSFileSystem);
            currentSheet = workbook.getSheetAt(0);
        }
    }

    /**
     * init constructor
     * 
     * @param inputStream
     * @throws IOException
     */
    public Excel2003Handler(FileInputStream inputStream) throws IOException {
        // if fileinputstrea is exist，init
        if (inputStream != null) {
            POIFSFileSystem poiFISFileSystem = new POIFSFileSystem(inputStream);
            workbook = new HSSFWorkbook(poiFISFileSystem);
            currentSheet = workbook.getSheetAt(0);
        }
    }
    
    public Excel2003Handler(FileInputStream inputStream,int sheetCount) throws IOException {
        // if fileinputstrea is exist，init
        if (inputStream != null) {
            POIFSFileSystem poiFISFileSystem = new POIFSFileSystem(inputStream);
            workbook = new HSSFWorkbook(poiFISFileSystem);
            currentSheet = workbook.getSheetAt(sheetCount);
        }
    }

    /**
     * 
     * 
     * Check workbook is exist or not .
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @return boolean
     * 
     * 
     */
    @Override
    public boolean exists() {
        return workbook != null;
    }

    /**
     * 
     * 
     * Get Path .
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @return String
     * 
     * 
     */
    @Override
    public String getParent() {
        return excelFile.getParent();
    }

    /**
     * 
     * 
     * Get file name.
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @return String
     * 
     * 
     */
    @Override
    public String getFileName() {
        return excelFile.getName();
    }

    /**
     * 
     * 
     * create file.
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @return boolean
     * 
     * 
     */
    @Override
    public boolean createNewFile() throws IOException {
        workbook = new HSSFWorkbook();

        if (excelFile != null) {
            // create file path
            boolean dirResult = excelFile.getParentFile().mkdirs();

            if (dirResult || excelFile.getParentFile().exists()) {
                FileOutputStream fileOut = new FileOutputStream(getParent() + File.separator + getFileName());
                workbook.write(fileOut);
                fileOut.close();
            } else {
                // if create fail 回False;
                return false;
            }
        }
        // create success
        return true;
    }

    /**
     * 
     * 
     * create file by file.
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param newfile
     * @return boolean
     * 
     * 
     */
    @Override
    public boolean createNewFile(File newfile) throws IOException {
        excelFile = newfile;
        return createNewFile();
    }

    /**
     * 
     * 
     * create file by file name.
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param fileName
     * @return boolean
     * 
     * 
     */
    @Override
    public boolean createNewFile(String fileName) throws IOException {
        excelFile = new File(fileName);
        return createNewFile();
    }

    /**
     * 
     * 
     * create sheet by sheet name.
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param sheetName
     * @return boolean
     * 
     * 
     */
    @Override
    public boolean createNewSheet(String sheetName) throws IOException {
        if (workbook == null) {
            return false;
        }
        HSSFSheet sheet = workbook.createSheet(sheetName);
        // set current Sheet
        this.currentSheet = sheet;

        if (sheet != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 
     * 
     * switch sheet by sheet name.
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param sheetName
     * @return boolean
     * 
     * 
     */
    @Override
    public void switchCurrentSheet(String sheetName) {
        if (workbook == null) {
            return;
        }
        HSSFSheet sheet = workbook.getSheet(sheetName);

        if (sheet != null) {
            this.currentSheet = sheet;
        }
    }

    /**
     * 
     * 
     * switch sheet by sheet id.
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param sheetId
     * @return boolean
     * 
     * 
     */
    @Override
    public void switchCurrentSheet(int sheetId) {
        if (workbook == null) {
            return;
        }
        HSSFSheet sheet = workbook.getSheetAt(sheetId);

        if (sheet != null) {
            this.currentSheet = sheet;
        }
    }

    /**
     * 
     * Get sheet row count.
     * <br>
     * 
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @return int
     * 
     * 
     */
    @Override
    public int getSheetRowCount() {
        return this.currentSheet.getPhysicalNumberOfRows();
    }

    /**
     * 
     * Check sheet is exist or not.
     * 
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param sheetName
     * @return boolean
     * 
     * 
     */
    @Override
    public boolean isExistSheet(String sheetName) {
        if (workbook == null) {
            return false;
        }
        HSSFSheet sheet = workbook.getSheet(sheetName);

        if (sheet == null) {
            return false;
        }

        return true;
    }

    /**
     * 
     * Set String value to position.
     * 
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param row
     * @param col
     * @param value
     * @return boolean
     * 
     * 
     */
    @Override
    @SuppressWarnings("deprecation")
    public boolean setStringValue(int row, int col, String value) {
        if (workbook == null) {
            return false;
        }
        HSSFRow hssfRow = currentSheet.getRow(row);
        if (hssfRow == null){
            hssfRow = currentSheet.createRow(row);
        }
        HSSFCell cell = hssfRow.getCell(col);
        if(cell == null){
        	cell = hssfRow.createCell(col);
        }

        cell.setCellType(2);
        cell.setCellValue(value);

        return true;
    }

    /**
     * 
     * Set String value to position.
     * 
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2017-3-27, piliping<br>
     * <b>Comment:</b>
     * 
     * @param row
     * @param col
     * @param value
     * @return boolean
     * 
     * 
     */
    @SuppressWarnings("deprecation")
	@Override
    public boolean setFormulaValue(int row, int col, String value) {
        if (workbook == null) {
            return false;
        }

        // XSSFRow hssfRow = currentSheet.createRow(row);

        HSSFRow hssfRow = currentSheet.getRow(row);
        if (hssfRow == null){
            hssfRow = currentSheet.createRow(row);
        }
        HSSFCell cell = hssfRow.getCell(col);
        if(cell == null){
        	cell = hssfRow.createCell(col);
        }
        // cell.setEncoding(XSSFCell.ENCODING_UTF_16);
        cell.setCellType(2);
        cell.setCellFormula(value);

        return true;
    }
    
  //add by piliping 增加设置单元格格式为百分比20170518
    @SuppressWarnings("deprecation")
	@Override
    public boolean setFormulaValueForPercent(int row, int col, String value) {
        if (workbook == null) {
            return false;
        }

        HSSFCellStyle cellStyle = workbook.createCellStyle();  
        cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00%"));  


        HSSFRow hssfRow = currentSheet.getRow(row);
        if (hssfRow == null){
            hssfRow = currentSheet.createRow(row);
        }
        HSSFCell cell = hssfRow.getCell(col);
        if(cell == null){
        	cell = hssfRow.createCell(col);
        }
        // cell.setEncoding(XSSFCell.ENCODING_UTF_16);
        //cell.setCellType(XSSFCell.CELL_TYPE_FORMULA);
        cell.setCellType(2);
        cell.setCellFormula(value);
        cell.setCellStyle(cellStyle);  

        return true;
    }
    @Override
    @SuppressWarnings("deprecation")
    public boolean setTitleStringValue(int row, int col, String value) {
        if (workbook == null) {
            return false;
        }
        HSSFCellStyle cellStyle = workbook.createCellStyle(); 
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中 
        HSSFFont font = workbook.createFont();    
        font.setFontName("宋体");    
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD); // 字体加粗  
        font.setFontHeightInPoints((short) 16);//设置字体大小

        
        HSSFRow hssfRow = currentSheet.getRow(row);
        if (hssfRow == null){
            hssfRow = currentSheet.createRow(row);
        }          
        HSSFCell cell = hssfRow.createCell((short) col);
        // cell.setEncoding(HSSFCell.ENCODING_UTF_16);
        cell.setCellType(2);
        cell.setCellValue(value);
        cell.setCellStyle(cellStyle);
        return true;
    }
    
    /**
     * 
     * Set number value to position.
     * 
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param row
     * @param col
     * @param value
     * @return
     * 
     * 
     */
    @Override
    @SuppressWarnings("deprecation")
    public boolean setNumericValue(int row, int col, double value) {
        if (workbook == null) {
            return false;
        }

        // HSSFRow hssfRow = currentSheet.createRow(row);
        HSSFRow hssfRow = currentSheet.getRow(row);
        if (hssfRow == null) {
            hssfRow = currentSheet.createRow(row);
        }

        HSSFCell cell = hssfRow.createCell((short) col);
        // cell.setEncoding(HSSFCell.ENCODING_UTF_16);
        cell.setCellType(2);
        cell.setCellValue(value);

        return true;
    }

    /**
     * 
     * Set date value to position.
     * 
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param row
     * @param col
     * @param value
     * @param fomat
     * @return boolean
     * 
     * 
     */
    @Override
    @SuppressWarnings("deprecation")
    public boolean setDateValue(int row, int col, Date value, String fomat) {
        if (workbook == null) {
            return false;
        }

        // HSSFRow hssfRow = currentSheet.createRow(row);
        HSSFRow hssfRow = currentSheet.getRow(row);
        if (hssfRow == null) {
            hssfRow = currentSheet.createRow(row);
        }

        HSSFCell cell = hssfRow.createCell((short) col);
        // cell.setEncoding(HSSFCell.ENCODING_UTF_16);
        cell.setCellType(2);

        SimpleDateFormat dateFormat = new SimpleDateFormat(fomat);
        cell.setCellValue(dateFormat.format(value));

        return true;
    }

    /**
     * 
     * Set boolean value to position.
     * 
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param row
     * @param col
     * @param value
     * @return boolean
     * 
     * 
     */
    @Override
    @SuppressWarnings("deprecation")
    public boolean setBooleanValue(int row, int col, boolean value) {
        if (workbook == null) {
            return false;
        }

        // HSSFRow hssfRow = currentSheet.createRow(row);
        HSSFRow hssfRow = currentSheet.getRow(row);
        if (hssfRow == null) {
            hssfRow = currentSheet.createRow(row);
        }

        HSSFCell cell = hssfRow.createCell((short) col);
        // cell.setEncoding(HSSFCell.ENCODING_UTF_16);
        cell.setCellType(2);
        cell.setCellValue(value);

        return true;
    }

    /**
     * 
     * Merge cells.
     * 
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param rowFrom
     * @param colFrom
     * @param rowTo
     * @param colTo
     * @return
     * 
     * 
     */
    @Override
    public boolean mergeCells(int rowFrom, int colFrom, int rowTo, int colTo) {
        if (workbook == null) {
            return false;
        }

        currentSheet.addMergedRegion(new CellRangeAddress(rowFrom, (short) colFrom, rowTo, (short) colTo));

        return true;
    }

    /**
     * 
     * Get value from row and col.
     * 
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param row
     * @param col
     * @return String
     * 
     * 
     */
    @Override
    @SuppressWarnings("deprecation")
    public String getValue(int row, int col) {
        if (workbook == null) {
            return "";
        }
        String value = "";

        // read data from current Sheet row
        HSSFRow hssfrow = currentSheet.getRow(row);
        if (hssfrow == null) {
            return "";
        }
        // get col by row
        HSSFCell cell = hssfrow.getCell((short) col);
        if (cell == null) {
            return "";
        }
        // get cell type
        int type = cell.getCellType();

        if (type == HSSFCell.CELL_TYPE_STRING) {
            // string type
            value = cell.getStringCellValue();
        } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
            // date type
            double data = cell.getNumericCellValue();
            if (data == 0.0) {
                value = "";
            } else {
                Date date = HSSFDateUtil.getJavaDate(data);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                value = dateFormat.format(date);
            }
        } else if (type == HSSFCell.CELL_TYPE_NUMERIC) {
            // number type
            double dvalue = cell.getNumericCellValue();
            value = String.valueOf(dvalue);
        } else if (type == HSSFCell.CELL_TYPE_BOOLEAN) {
            // boolean type
            value = cell.getBooleanCellValue() + "";
        } else if (type == HSSFCell.CELL_TYPE_BLANK) {
            value = "";
        } else {
            value = cell.getStringCellValue();
        }

        if (value == null) {
            return "";
        }

        return value.trim();
    }

    /**
     * 
     * Get string value from row and col.
     * 
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param row
     * @param col
     * @return String
     * 
     * 
     */
    @Override
    public String getStringValue(int row, int col) {
        if (workbook == null) {
            return "";
        }
        String value = "";

        // read data from current Sheet row
        HSSFRow hssfrow = currentSheet.getRow(row);
        if (hssfrow == null) {
            return "";
        }
        // get col by row
        HSSFCell cell = hssfrow.getCell((short) col);
        if (cell == null) {
            return "";
        }
        // get cell type
        int type = cell.getCellType();
        //CellType type = cell.getCellTypeEnum();
        if (type == 1) {
            // string type
            value = cell.getStringCellValue();
        } else if (type == 0) {
            // number type
            double dvalue = cell.getNumericCellValue();
            value = String.valueOf(dvalue);
            if (value.equals("0.0")) {
                value = "0";
            }
        } else {
            value = cell.getStringCellValue();
        }
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    /**
     * 
     * Save change.
     * 
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @return boolean
     * @throws IOException
     * 
     * 
     */
    @Override
    public boolean saveChanges() throws IOException {
        if (workbook == null || excelFile == null) {
            return false;
        }

        FileOutputStream fileOut = new FileOutputStream(getParent() + File.separator + getFileName());
        workbook.write(fileOut);
        fileOut.close();

        return true;
    }
    
    public HSSFWorkbook getWorkbook(){
        return workbook;
    }

    /**
     * 
     * Down load excel.
     * 
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param response
     * @throws IOException
     * 
     * 
     */
    @Override
    public void downloadExcel(HttpServletResponse response) throws IOException {
        OutputStream outstream = response.getOutputStream();
        workbook.write(outstream);
        outstream.flush();
        outstream.close();

        response.setStatus(HttpServletResponse.SC_OK);
        response.flushBuffer();
    }

    /*
     * public static void main(String[] args) throws Exception { System.out.println("------ test start ------");
     * 
     * // 建立CSCExcel對象 IExcelHandler excel = new Excel2003Handler("C:\\test\\testtwoa\\test.xls");
     * 
     * System.out.println(" 路徑名稱 == " + excel.getParent()); System.out.println(" 文件名稱 == " + excel.getFileName());
     * 
     * boolean fileResult = false; // 判斷是否存在該 Excel文件，如果不存在並用戶繼續執行操作，則後續處理返回都是False，即操作不成功。 if (!excel.exists()) { //
     * 創建新Excel文件 fileResult = excel.createNewFile(); } // 創建結果 System.out.println(" fileResult == " + fileResult);
     * 
     * 
     * // 判斷是否存在指定名稱的Sheet if (!excel.isExistSheet("jinxin")) { // 如果不存在，創建新Sheet boolean sheetResult =
     * excel.createNewSheet("jinxin"); // 創建結果 System.out.println(" sheetResult == " + sheetResult); } else { //
     * 如果存在，則把指定Sheet設為當前Sheet，如果不設定，取Workbook的第一個Sheet作為當前Sheet excel.switchCurrentSheet("jinxin"); }
     * 
     * // 取指定位置的值 String value = excel.getValue(0, 0); System.out.println(" value == " + value);
     * 
     * // 設置指定位置的字符串值 excel.setStringValue(0, 0, "sss"); // 設置指定位置的數字值 excel.setNumericValue(0, 1, 1234.012); //
     * 設置指定位置的布爾值 excel.setBooleanValue(0, 2, false);
     * 
     * if (!excel.isExistSheet("seconds")) { // 創建第二個Sheet excel.createNewSheet("seconds"); } else {
     * excel.switchCurrentSheet("seconds"); }
     * 
     * // 設置指定位值的布爾值 excel.setBooleanValue(1, 2, true); // 設置指定位值的日期值 excel.setDateValue(1, 1, new Date(),
     * "yyyy-MM-dd");
     * 
     * // 合併單元格操作 excel.setStringValue(2, 1, "Test for Merging Cells"); excel.mergeCells(2, 1, 2, 2);
     * 
     * // 對所有操作進行保存 excel.saveChanges();
     * 
     * System.out.println("------ test end ------"); }
     */
}
