package ext.appo.common.util;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

public interface ExcelHandler {

    /**
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
    public abstract boolean exists();

    /**
     * 
     * Get path.
     * 
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @return path
     * 
     * 
     */
    public abstract String getParent();

    /**
     * 
     * Get file name.
     * <br>
     * 
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @return file name
     * 
     * 
     */
    public abstract String getFileName();

    /**
     * 
     * Create Excel.
     * 
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @return boolean
     * @throws IOException
     * 
     */
    public abstract boolean createNewFile() throws IOException;

    /**
     * 
     * Create Excel by file.
     * <br>
     * 
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param newfile
     * @return boolean
     * @throws IOException
     * 
     * 
     */
    public abstract boolean createNewFile(File newfile) throws IOException;

    /**
     * 
     * Create Excel by file name.
     * 
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param fileName
     * @return boolean
     * @throws IOException
     * 
     * 
     */
    public abstract boolean createNewFile(String fileName) throws IOException;

    /**
     * 
     * Create sheet by sheet name.
     * 
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param sheetName
     * @return boolean
     * @throws IOException
     * 
     * 
     */
    public abstract boolean createNewSheet(String sheetName) throws IOException;

    /**
     * 
     * Change current sheet by sheet name.
     * 
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param sheetName
     * 
     * 
     */
    public abstract void switchCurrentSheet(String sheetName);

    /**
     * 
     * Change current sheet by sheet id.
     * 
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param sheetId
     * 
     * 
     */
    public abstract void switchCurrentSheet(int sheetId);

    /**
     * 
     * Get sheet row count.
     * 
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @return int
     * 
     * 
     */
    public abstract int getSheetRowCount();

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
    public abstract boolean isExistSheet(String sheetName);

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
    public abstract boolean setStringValue(int row, int col, String value);
    public abstract boolean setFormulaValue(int row, int col, String value);
    public abstract boolean setFormulaValueForPercent(int row, int col, String value);//add by piliping 增加设置单元格格式为百分比20170518

    public abstract boolean setTitleStringValue(int row, int col, String value);
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
    public abstract boolean setNumericValue(int row, int col, double value);

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
    public abstract boolean setDateValue(int row, int col, Date value, String fomat);

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
    public abstract boolean setBooleanValue(int row, int col, boolean value);

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
    public abstract boolean mergeCells(int rowFrom, int colFrom, int rowTo, int colTo);

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
    public abstract String getValue(int row, int col);

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
    public abstract String getStringValue(int row, int col);

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
    public abstract boolean saveChanges() throws IOException;

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
    public abstract void downloadExcel(HttpServletResponse response) throws IOException;

}
