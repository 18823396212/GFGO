package ext.appo.bom.processor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.appo.update.UpdateContainerRole;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.util.WTException;

/***
 * 角色用户导入类
 * 
 * @author Administrator
 *
 */
public class AddRoleProcessor extends DefaultObjectFormProcessor {

	private static final String CLASSNAME = AddRoleProcessor.class.getName();
	private static final Logger LOG = LogR.getLogger(CLASSNAME);

	@Override
	public FormResult postProcess(NmCommandBean nmCommandBean, List<ObjectBean> paramList) throws WTException {
		FormResult formResult = new FormResult();
		formResult.setStatus(FormProcessingStatus.SUCCESS);

		try {
			WTPart parentPart = (WTPart) nmCommandBean.getActionOid().getRefObject();
			if (LOG.isDebugEnabled()) {
				LOG.debug("parentPart : " + parentPart.getDisplayIdentity());
			}

			// 获取用户上传的文件对象
			HttpServletRequest request = nmCommandBean.getRequest();
			File file = (File) request.getAttribute("txtFile");
			if (LOG.isDebugEnabled()) {
				LOG.debug("File : " + file.getName());
			}

			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			// 打开HSSWorkbook
			POIFSFileSystem fs = new POIFSFileSystem(in);
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheetAt = wb.getSheetAt(0);// 获取第一个表格
			int lastRowNum = sheetAt.getLastRowNum();
			List<String> attslist = getAttsList(sheetAt);// 获取表头
			System.out.println(attslist.size());
			List<List> allValuesList = getAllValuesList(sheetAt, lastRowNum, attslist);
			System.out.println(allValuesList.size());
			List<Map> datalist = getAllEditObj(attslist, allValuesList);

			UpdateContainerRole.setcontainerRole(datalist);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		}

		return formResult;
	}

	private static List<Map> getAllEditObj(List<String> attslist, List<List> allValuesList) {
		List<Map> allEditObj = new ArrayList<>();
		if (allValuesList.size() > 0) {
			for (List list : allValuesList) {
				Map<String, Object> map = new HashMap<>();
				for (int i = 0; i < list.size(); i++) {
					map.put(attslist.get(i), list.get(i));
				}
				allEditObj.add(map);
			}
		}
		return allEditObj;
	}

	private static List<List> getAllValuesList(HSSFSheet sheetAt, int lastRowNum, List<String> attslist) {
		List<List> tableValueList = new ArrayList<>();
		int size = attslist.size();
		for (int i = 1; i < lastRowNum + 1; i++) {
			Row row2 = sheetAt.getRow(i);
			List<String> rowValueList = new ArrayList<>();
			for (int j = 0; j < size; j++) {
				String velue = getStringVelue(row2.getCell(j));
				rowValueList.add(velue);
			}
			tableValueList.add(rowValueList);
		}
		return tableValueList;
	}

	private static List<String> getAttsList(HSSFSheet sheetAt) {
		List<String> attlist = new ArrayList<>();
		Row row = sheetAt.getRow(0);
		short s = row.getLastCellNum();
		for (int i = 0; i < s; i++) {
			String velue = getStringVelue(row.getCell(i));
			attlist.add(velue);
		}
		return attlist;
	}

	/**
	 * 获取string类型的值
	 * 
	 * @param cell
	 * @return
	 */
	private static String getStringVelue(Cell cell) {
		if (cell != null) {
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			String value = cell.getStringCellValue();
			return value;
		} else {
			return null;
		}
	}

	// 创建workbook对象
	public static Workbook getWorkBook(final File f) {
		Workbook workbook = null;
		InputStream is = null;
		try {
			is = new FileInputStream(f);
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