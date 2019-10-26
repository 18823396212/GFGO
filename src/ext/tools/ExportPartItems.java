package ext.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import wt.method.RemoteAccess;
import wt.util.WTException;
import wt.util.WTProperties;

public class ExportPartItems implements RemoteAccess, Serializable {
	private List<List<String>> datalist;
	public static String filename;
	private HSSFWorkbook hssfWorkbook;

	public ExportPartItems(List<List<String>> datalist) {

		this.datalist = datalist;
		this.hssfWorkbook = null;
	}

	public void doExport(List<List<String>> datalist) {
		try {
			try {
				this.hssfWorkbook = WriteToDoc(datalist);
				// SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd
				// HH:mm:ss");
				String fil_ename = "PLM_K3_Comparison_bom_Report" + new Date() + ".xls";

				FileOutputStream iOutputStream = new FileOutputStream(getDefaultPath() + fil_ename);

				this.hssfWorkbook.write(iOutputStream);

				iOutputStream.close();
			} catch (WTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private HSSFWorkbook WriteToDoc(List<List<String>> datalist) throws IOException, WTException {
		HSSFWorkbook hssfworkbook = new HSSFWorkbook();

		HSSFSheet dochssfsheet = hssfworkbook.createSheet("result");

		writeDocToSheet(datalist, hssfworkbook, dochssfsheet);
		System.out.println("end>>>>>>>>>>>>>>>>>>>>>>>");
		return hssfworkbook;
	}

	// 保存路径
	public static String getDefaultPath() {
		WTProperties props;
		try {
			props = WTProperties.getLocalProperties();
			String base = props.getProperty("wt.codebase.location");
			filename = base + File.separator + "temp" + File.separator;
			System.out.println(">>>>>" + filename);
			return filename;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void writeDocToSheet(List<List<String>> datalist, HSSFWorkbook hssfWorkbook, HSSFSheet hssfSheet)
			throws IOException {
		/*
		 * hssfSheet.setColumnWidth((short)0, (short)1500);
		 * hssfSheet.setColumnWidth((short)1, (short)5000);
		 * hssfSheet.setColumnWidth((short)2, (short)5000);
		 * hssfSheet.setColumnWidth((short)3, (short)3000);
		 * hssfSheet.setColumnWidth((short)4, (short)3000);
		 * hssfSheet.setColumnWidth((short)5, (short)3000);
		 * hssfSheet.setColumnWidth((short)6, (short)3000);
		 * hssfSheet.setColumnWidth((short)7, (short)1500);
		 * hssfSheet.setColumnWidth((short)8, (short)5000);
		 * hssfSheet.setColumnWidth((short)9, (short)5000);
		 * hssfSheet.setColumnWidth((short)10, (short)3000);
		 * hssfSheet.setColumnWidth((short)11, (short)3000);
		 * hssfSheet.setColumnWidth((short)12, (short)3000);
		 * hssfSheet.setColumnWidth((short)13, (short)3000);
		 * hssfSheet.setColumnWidth((short)14, (short)3000);
		 * hssfSheet.setColumnWidth((short)15, (short)3000);
		 */

		int i = 0;
		int j = 0;
		HSSFRow firstrow = hssfSheet.createRow((short) (i));
		HSSFCell cell = firstrow.createCell((short) (j++));
		cell.setCellValue("物料信息");

		i++;
		j = 0;

		HSSFRow secondrow = hssfSheet.createRow((short) (i));
		secondrow.createCell((short) (j++)).setCellValue("序号");
		secondrow.createCell((short) (j++)).setCellValue("BOM");
		secondrow.createCell((short) (j++)).setCellValue("子件编号");
		secondrow.createCell((short) (j++)).setCellValue("不一致描述");
		secondrow.createCell((short) (j++)).setCellValue("PLM版本");
		secondrow.createCell((short) (j++)).setCellValue("K3版本");
		secondrow.createCell((short) (j++)).setCellValue("PLM数量");
		secondrow.createCell((short) (j++)).setCellValue("K3数量");
		secondrow.createCell((short) (j++)).setCellValue("PLM位号");
		secondrow.createCell((short) (j++)).setCellValue("K3位号");
		secondrow.createCell((short) (j++)).setCellValue("PLM BOM状态");
		secondrow.createCell((short) (j++)).setCellValue("PLM 子件状态");

		for (int k = 0; k < datalist.size(); k++) {
			List<String> vector = datalist.get(k);
			HSSFRow row = hssfSheet.createRow((short) (i + k + 1));
			j = 0;

			row.createCell((short) (j++)).setCellValue(k);
			row.createCell((short) (j++)).setCellValue(String.valueOf(vector.get(0)));
			row.createCell((short) (j++)).setCellValue(String.valueOf(vector.get(1)));
			row.createCell((short) (j++)).setCellValue(String.valueOf(vector.get(2)));
			row.createCell((short) (j++)).setCellValue(String.valueOf(vector.get(3)));
			row.createCell((short) (j++)).setCellValue(String.valueOf(vector.get(4)));
			row.createCell((short) (j++)).setCellValue(String.valueOf(vector.get(5)));
			row.createCell((short) (j++)).setCellValue(String.valueOf(vector.get(6)));
			row.createCell((short) (j++)).setCellValue(String.valueOf(vector.get(7)));
			row.createCell((short) (j++)).setCellValue(String.valueOf(vector.get(8)));
			row.createCell((short) (j++)).setCellValue(String.valueOf(vector.get(9)));
			row.createCell((short) (j++)).setCellValue(String.valueOf(vector.get(10)));

		}

		// hssfSheet.addMergedRegion(new Region(0, (short) 0, 0, (short) 8));
		// //参数1，起始行-1，参数2,起始列-1，参数3,终止行-1,参数4,终止列-1
		// }
	}

}
