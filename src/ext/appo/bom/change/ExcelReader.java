package ext.appo.bom.change;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.ptc.windchill.uwgm.common.prefs.res.newCadDocPrefsResource;


import wt.util.WTProperties;
import wt.vc.wip.wipResource;

/**
 * @author 聂善福
 * 增强的读取Excel模板功能类
 * 建议您Extend此class并override ReadData方法
 */
public class ExcelReader implements Serializable
{
	protected  HSSFWorkbook sourceHssfWorkbook = null;
	protected  HSSFSheet sourcehssfsheet = null;
	protected  Vector dataVector = new Vector();
	protected  static boolean VERBOSE;
	protected  static String codebase;
	
	protected  String fileType="EXCEL";
	protected  String fileName;
	protected  String fileLocation;
	protected  String sheetName;

	static
	{
		try
		{
			WTProperties wtproperties = WTProperties.getLocalProperties();
			codebase = wtproperties.getProperty("wt.codebase.location");
			VERBOSE = wtproperties.getProperty("ext.generic.excel.verbose", false);
		} catch (Throwable t)
		{
			throw new ExceptionInInitializerError(t);
		}
	}

	public HSSFWorkbook getDefineWorkbook()
	{
		
		try
		{
			String defaultLocation = File.separator + "temp" + File.separator;
			String location;
			if (this.fileLocation == null)
				location=defaultLocation;
			else 
			    location=this.fileLocation.trim().length() == 0?defaultLocation:this.fileLocation;

			HSSFWorkbook hssfWorkbook = new HSSFWorkbook(new FileInputStream(codebase+location+fileName));
			return hssfWorkbook;
		} catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public ExcelReader(String fileName,String sheetName)
	{
		setFileName(fileName);
		setSheetName(sheetName);
	}

	public ExcelReader(String fileName)
	{
		setFileName(fileName);
	}

	public void initHssfSheet()
	{
		sourceHssfWorkbook = getDefineWorkbook();
		
		if (this.sheetName == null)
			sourcehssfsheet=sourceHssfWorkbook.getSheetAt(0);
		else {
			sourcehssfsheet=this.sheetName.trim().length()==0?sourceHssfWorkbook.getSheetAt(0):sourceHssfWorkbook.getSheet(this.sheetName);
		}
		
		if (sourcehssfsheet != null)
			readData();
	}
	
	public ExcelReader(String fileName,String fileLocation,String sheetName)
	{
		setFileName(fileName);
		setFileLocation(fileLocation);
		setSheetName(sheetName);
		
		sourceHssfWorkbook = getDefineWorkbook();
		sourcehssfsheet = sourceHssfWorkbook.getSheet(this.sheetName);
		
		if (sourcehssfsheet != null)
			readData();
	}

	public static String getCell(int rowid, int columnid, HSSFSheet sheet)
	{
		if (columnid < 0)
			return "";
		String value = "";

		try
		{
			HSSFRow row = sheet.getRow(rowid - 1);
			if (row == null)
				return "";
			HSSFCell cell = row.getCell((short) (columnid - 1));

			if (cell != null)
			{
				switch (cell.getCellType())
				{
				case HSSFCell.CELL_TYPE_BLANK:
					break;
				case HSSFCell.CELL_TYPE_BOOLEAN:
					value = "" + cell.getBooleanCellValue();
					break;
				case HSSFCell.CELL_TYPE_ERROR:
					value = "" + cell.getErrorCellValue();
					break;
				case HSSFCell.CELL_TYPE_FORMULA:
					value = "" + cell.getCellFormula();
					break;
				case HSSFCell.CELL_TYPE_NUMERIC:
					value = "" + cell.getNumericCellValue();
					break;
				case HSSFCell.CELL_TYPE_STRING:
					value = "" + cell.getRichStringCellValue().getString();
					break;
				default:
					value += cell.getRichStringCellValue().getString();
				}
			} else
			{
				value = "";
			}
			return value;
		} catch (Exception e)
		{
			System.out.println("data error with CellType");
			e.printStackTrace();
			return "";
		}
	}

	public boolean isEndOfExcelFile(int rowID)
	{
		try
		{
			String cell1 = getCell(rowID, 0, sourcehssfsheet);
			// System.out.println("getCell("+String.valueOf(rowID)+","+String.valueOf(colLevel)+")=OK====cell-1---"+cell1);
			String cell2 = getCell(rowID + 1, 0, sourcehssfsheet);
			// System.out.println("getCell("+String.valueOf(rowID+1)+","+String.valueOf(colLevel)+")=OK====cell-2---"+cell2);
			String cell3 = getCell(rowID + 2, 0, sourcehssfsheet);
			// System.out.println("getCell("+String.valueOf(rowID+2)+","+String.valueOf(colLevel)+")=OK====cell-3---"+cell3);
			String cell4 = getCell(rowID + 3, 0, sourcehssfsheet);
			// System.out.println("getCell("+String.valueOf(rowID+2)+","+String.valueOf(colLevel)+")=OK====cell-3---"+cell3);

			if (cell1 == "" && cell2 == "" && cell3 == "" && cell4=="")
				return true;
			else
				return false;
		} catch (Exception e)
		{
			return true;
		}
	}
	
	/**
	 * 这里只是一个示例方法，子类请继承之
	 */
	public void readData()  
	{
		for (int i = 2; i < 1000; i++)
		{
			String str=getCell(i, 2, sourcehssfsheet);
			
			if (str.equals(""))
				break;
			
//			ItemObject itemObject=new ItemObject();
		//	DemoObject demoObject=new DemoObject();
		//	dataVector.add(demoObject);
		}
	}

	public HSSFWorkbook getSourceHssfWorkbook()
	{
		return sourceHssfWorkbook;
	}

	public void setSourceHssfWorkbook(HSSFWorkbook sourceHssfWorkbook)
	{
		this.sourceHssfWorkbook = sourceHssfWorkbook;
	}

	public HSSFSheet getSourcehssfsheet()
	{
		return sourcehssfsheet;
	}

	public void setSourcehssfsheet(HSSFSheet sourcehssfsheet)
	{
		this.sourcehssfsheet = sourcehssfsheet;
	}

	public Vector getDataVector()
	{
		return dataVector;
	}

	public void setDataVector(Vector dataVector)
	{
		this.dataVector = dataVector;
	}

	public String getFileName()
	{
		return fileName;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public String getFileLocation()
	{
		return fileLocation;
	}

	public void setFileLocation(String fileLocation)
	{
		this.fileLocation = fileLocation;
	}

	public String getSheetName()
	{
		return sheetName;
	}

	public void setSheetName(String sheetName)
	{
		this.sheetName = sheetName;
	}

}
