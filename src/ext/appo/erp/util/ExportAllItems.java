package ext.appo.erp.util;

import ext.appo.erp.bean.SendMessage;
import org.apache.poi.hssf.usermodel.*;
import wt.method.RemoteAccess;
import wt.util.WTException;
import wt.util.WTProperties;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExportAllItems implements RemoteAccess, Serializable {
	private Vector datalist;
	public static String filename;
	private HSSFWorkbook hssfWorkbook;

	public ExportAllItems(Vector datalist){

		this.datalist = datalist;
		this.hssfWorkbook = null;
	}

	public void doExport(Vector datalist,String count,String filename) {
		try {
			try {
				this.hssfWorkbook = WriteToDoc(datalist,count,filename);
				String fil_ename = filename + ".xls";

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

	private HSSFWorkbook WriteToDoc(Vector datalist,String count,String filename) throws IOException, WTException {
		HSSFWorkbook hssfworkbook = new HSSFWorkbook();

		HSSFSheet dochssfsheet = hssfworkbook.createSheet("result");

		writeDocToSheet(datalist, hssfworkbook, dochssfsheet,count,filename);
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

	public static void writeDocToSheet(Vector vectorList, HSSFWorkbook hssfWorkbook, HSSFSheet hssfSheet,String count,String filename)
			throws IOException {
		if (filename.contains("BOMupdate")){
			hssfSheet.setColumnWidth((short) 0, (short) 1500);
			hssfSheet.setColumnWidth((short) 1, (short) 5000);
			hssfSheet.setColumnWidth((short) 3, (short) 25000);
			int i = 0;
			int j = 0;
			HSSFRow firstrow = hssfSheet.createRow((short) (i));

			HSSFCell cell = firstrow.createCell((short) (j++));
			cell.setCellValue("发送中间表信息");
			cell.setCellStyle(getAlignCellStyle(hssfWorkbook));

			HSSFCell cell2 = firstrow.createCell((short) (j++));
			cell2.setCellValue("总共发送：");
			HSSFCell cell3 = firstrow.createCell((short) (j++));
			cell3.setCellValue(count);
			HSSFCell cell4 = firstrow.createCell((short) (j++));
			cell4.setCellValue("创建日期：");
			HSSFCell cell5 = firstrow.createCell((short) (j++));
			Date date=new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			cell5.setCellValue(sdf.format(date));

			i++;
			j = 0;
			HSSFRow secondrow = hssfSheet.createRow((short) (i));
			secondrow.createCell((short) (j++)).setCellValue("序号");
			secondrow.createCell((short) (j++)).setCellValue("物料编码");
			secondrow.createCell((short) (j++)).setCellValue("标志");
			secondrow.createCell((short) (j++)).setCellValue("详情");

			for (int k = 0; k < vectorList.size(); k++) {
				String partNumber=((SendMessage)vectorList.get(k)).getPartNumber();
				String isSuccess=((SendMessage)vectorList.get(k)).getIsSuccess();
				String message = ((SendMessage)vectorList.get(k)).getMessage();

				HSSFRow row = hssfSheet.createRow((short) (i + k + 1));
				j = 0;
				row.createCell((short) (j++)).setCellValue(k + 1);
				row.createCell((short) (j++)).setCellValue(partNumber);
				row.createCell((short) (j++)).setCellValue(isSuccess);
				row.createCell((short) (j++)).setCellValue(message);
			}
		}else{
			hssfSheet.setColumnWidth((short) 0, (short) 1500);
			hssfSheet.setColumnWidth((short) 1, (short) 5000);
			hssfSheet.setColumnWidth((short) 4, (short) 25000);
			int i = 0;
			int j = 0;
			HSSFRow firstrow = hssfSheet.createRow((short) (i));

			HSSFCell cell = firstrow.createCell((short) (j++));
			if (filename.contains("SendAllBOM")){
				cell.setCellValue("BOM发送异常信息");
			}else{
				cell.setCellValue("物料发送异常信息");
			}

			cell.setCellStyle(getAlignCellStyle(hssfWorkbook));
			HSSFCell cell2 = firstrow.createCell((short) (j++));
			cell2.setCellValue("总共发送：");
			HSSFCell cell3 = firstrow.createCell((short) (j++));
			cell3.setCellValue(count);
			HSSFCell cell4 = firstrow.createCell((short) (j++));
			cell4.setCellValue("创建日期：");
			HSSFCell cell5 = firstrow.createCell((short) (j++));
			Date date=new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			cell5.setCellValue(sdf.format(date));

			i++;
			j = 0;
			HSSFRow secondrow = hssfSheet.createRow((short) (i));
			secondrow.createCell((short) (j++)).setCellValue("序号");
			secondrow.createCell((short) (j++)).setCellValue("物料编码");
			secondrow.createCell((short) (j++)).setCellValue("版本");
			secondrow.createCell((short) (j++)).setCellValue("标志");
			secondrow.createCell((short) (j++)).setCellValue("详情");

			for (int k = 0; k < vectorList.size(); k++) {
				String partNumber=((SendMessage)vectorList.get(k)).getPartNumber();
				String version=((SendMessage)vectorList.get(k)).getVersion();
				String isSuccess=((SendMessage)vectorList.get(k)).getIsSuccess();
				String message = ((SendMessage)vectorList.get(k)).getMessage();

				HSSFRow row = hssfSheet.createRow((short) (i + k + 1));
				j = 0;
				row.createCell((short) (j++)).setCellValue(k + 1);
//			row.createCell((short) (j++)).setCellValue(vectorList.get(k).toString());
				row.createCell((short) (j++)).setCellValue(partNumber);
				row.createCell((short) (j++)).setCellValue(version);
				row.createCell((short) (j++)).setCellValue(isSuccess);
				row.createCell((short) (j++)).setCellValue(message);
			}
		}
		// hssfSheet.addMergedRegion(new Region(0, (short) 0, 0, (short) 8));
		// //参数1，起始行-1，参数2,起始列-1，参数3,终止行-1,参数4,终止列-1
		// }
	}


	public static HSSFCellStyle getAlignCellStyle(HSSFWorkbook wb) throws IOException {
		HSSFCellStyle cellStyle = wb.createCellStyle();
		cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		cellStyle.setWrapText(true);
		return cellStyle;
	}

}
