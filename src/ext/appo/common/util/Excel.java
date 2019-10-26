package ext.appo.common.util;

import java.io.File;
import java.io.IOException;

public class Excel {
	
	 /**
     * 
     * Get excel handler by excel version.
     * <br>
     * 
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param filePathName
     * @return ExcelHandler
     * @throws Exception
     * 
     * 
     */
    public static ExcelHandler getExcelHandler(String filePathName) throws Exception {
        if (filePathName == null) {
            return null;
        }
        if (filePathName.toLowerCase().endsWith("xls")) {
            return get2003ExcelHandler(filePathName);
        } else if (filePathName.toLowerCase().endsWith("xlsx")) {
            return get2007ExcelHandler(filePathName);
        } else {
            // can't identify handler by file extension name
            ExcelHandler handler = null;
            try {
                handler = get2007ExcelHandler(filePathName);
            } catch (Exception e) {
                handler = get2003ExcelHandler(filePathName);
            }
            return handler;
        }
    }

    /**
     * 
     * Get excel handler by file.
     * <br>
     * 
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param file
     * @return ExcelHandler
     * @throws Exception
     * 
     * 
     */
    public static ExcelHandler getExcelHandler(File file) throws Exception {
        if (file == null) {
            return null;
        }
        return getExcelHandler(file.getAbsolutePath());
    }

    /**
     * 
     * Get excel 2003 handler by file name.
     * 
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param filePathName
     * @return ExcelHandler
     * @throws IOException
     * 
     * 
     */
    public static ExcelHandler get2003ExcelHandler(String filePathName) throws IOException {
        return new Excel2003Handler(filePathName);
    }

    /**
     * 
     * Get excel 2003 handler by file.
     * <br>
     * 
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param file
     * @return
     * @throws IOException
     * 
     * 
     */
    public static ExcelHandler get2003ExcelHandler(File file) throws IOException {
        return new Excel2003Handler(file);
    }

    /**
     * 
     * Get excel 2007 handler by file name.
     * 
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param filePathName
     * @return ExcelHandler
     * @throws IOException
     * 
     * 
     */
    public static ExcelHandler get2007ExcelHandler(String filePathName) throws IOException {
        return new Excel2007Handler(filePathName);
    }

    /**
     * 
     * Get excel 2007 handler by file.
     * 
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @param file
     * @return ExcelHandler
     * @throws IOException
     * 
     * 
     */
    public static ExcelHandler get2007ExcelHandler(File file) throws IOException {
        return new Excel2007Handler(file);
    }

    /**
     * 
     * Get excel 2003 handler.
     * 
     * <br>
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @return
     * @throws IOException
     * 
     * 
     */
    public static ExcelHandler get2003ExcelHandler() throws IOException {
        return new Excel2003Handler();
    }

    /**
     * 
     * Get excel 2003 handler.
     * <br>
     * 
     * <b>Revision History</b><br>
     * <b>Rev:</b> 1.0 - 2012-7-12, jhong<br>
     * <b>Comment:</b>
     * 
     * @return
     * @throws IOException
     * 
     * 
     */
    public static ExcelHandler get2007ExcelHandler() throws IOException {
        return new Excel2007Handler();
    }

}
