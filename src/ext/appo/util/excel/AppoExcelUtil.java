package ext.appo.util.excel;

import ext.lang.file.FileConstantIfc;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import wt.util.WTException;
import wt.util.WTProperties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HYJ&NJH 光峰易用性开发表读取类
 */
public class AppoExcelUtil {

    private static String codebasePath;
    private static String separator = FileConstantIfc.UNIX_PATH_SEPARATOR;
    private static String path;

    static {
        try {
            codebasePath = WTProperties.getLocalProperties().getProperty("wt.codebase.location");
            path = codebasePath + separator + "ext" + separator + "appo" + separator + "cfg" + separator + "appoConfig.xlsx";
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 分类码截取规则
     *
     * @return
     */
    public Map<String, String> readSheet1() {
        Map<String, String> map = new HashMap<>();
        Workbook workBook = getWorkBook(path);
        Sheet sheet = workBook.getSheetAt(0);
        int lastRowNum = sheet.getLastRowNum();
        for (int i = 1; i < lastRowNum + 1; i++) {
            Row row = sheet.getRow(i);
            String key = getStringVelue(row.getCell(0));
            String velue = getStringVelue(row.getCell(1));
            map.put(key, velue);
        }
        return map;
    }

    /**
     * CIS库维护配置
     *
     * @return
     */
    public List<String> readSheet2() {
        List<String> list = new ArrayList<>();
        Workbook workBook = getWorkBook(path);
        Sheet sheet = workBook.getSheetAt(1);
        int lastRowNum = sheet.getLastRowNum();
        for (int i = 1; i < lastRowNum + 1; i++) {
            Row row = sheet.getRow(i);
            String key = getStringVelue(row.getCell(0));
            list.add(key);
        }
        return list;
    }

    /**
     * 电子料回填名称
     *
     * @return
     */
    public Map<String, String> readSheet3() {
        Map<String, String> map = new HashMap<>();
        Workbook workBook = getWorkBook(path);
        Sheet sheet = workBook.getSheetAt(2);
        int lastRowNum = sheet.getLastRowNum();
        for (int i = 1; i < lastRowNum + 1; i++) {
            Row row = sheet.getRow(i);
            String key = getStringVelue(row.getCell(0));
            String velue = getStringVelue(row.getCell(1));
            map.put(key, velue);
        }
        return map;
    }

    /**
     * 产品线与项目联动
     * key:上层，value:下层list,key为all时，value是所有产品线
     *
     * @return
     */
    public Map<String, List<String>> readSheet4() {
        Workbook workBook = getWorkBook(path);
        Sheet sheet = workBook.getSheetAt(3);

        //构建存储对象
        Map<String, List<String>> map = new HashMap<>();
        List<String> list = null;

        //第一列：库名
        String containerName = "";
        //第二列：产品线名称
        String productName = "";

        int lastRowNum = sheet.getLastRowNum();
        for (int i = 1; i < lastRowNum + 1; i++) {
            Row row = sheet.getRow(i);
            String container = getStringVelue(row.getCell(0));
            String productLine = getStringVelue(row.getCell(1));
            String project = getStringVelue(row.getCell(2));

            //第一列有数据
            if (container != null && !"".equals(container.trim())) {
                containerName = container;
            }

            //第二列有数据
            if (productLine != null && !"".equals(productLine.trim())) {
                productName = productLine;
                //往库对应的map中添加产品线
                if (map.containsKey(containerName)) {
                    list = map.get(containerName);
                    list.add(productName);
                } else {
                    list = new ArrayList<>();
                    list.add(productName);
                }
                map.put(containerName, list);
                //往所有“all”中添加产品线
                if (map.containsKey("all")) {
                    list = map.get("all");
                    list.add(productName);
                } else {
                    list = new ArrayList<>();
                    list.add(productName);
                }
                map.put("all", list);
            }

            //第三列有数据
            if (project != null && !"".equals(project.trim())) {
                //往产品线对应的map中添加项目
                if (map.containsKey(productName)) {
                    list = map.get(productName);
                    list.add(project);
                } else {
                    list = new ArrayList<>();
                    list.add(project);
                }
                map.put(productName, list);
            }
        }

        return map;
    }

    /**
     * 文件夹卡关
     *
     * @return
     */
    public Map<String, String> readSheet5() {
        Map<String, String> map = new HashMap<>();
        Workbook workBook = getWorkBook(path);
        Sheet sheet = workBook.getSheetAt(4);
        int lastRowNum = sheet.getLastRowNum();
        for (int i = 1; i < lastRowNum + 1; i++) {
            Row row = sheet.getRow(i);
            String key = getStringVelue(row.getCell(0));
            String velue = getStringVelue(row.getCell(1));
            map.put(key, velue);
        }
        return map;
    }

    private String getStringVelue(Cell cell) {
        if (cell != null) {
            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
            String value = cell.getStringCellValue();
            return value;
        } else {
            return null;
        }
    }

    public static void throwsException(Exception e) throws WTException {
        throw new WTException(e);
    }

    // 创建workbook对象
    public Workbook getWorkBook(final String fileName) {
        Workbook workbook = null;
        InputStream is = null;
        try {
            is = new FileInputStream(fileName);
            workbook = WorkbookFactory.create(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (EncryptedDocumentException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return workbook;
    }
}
