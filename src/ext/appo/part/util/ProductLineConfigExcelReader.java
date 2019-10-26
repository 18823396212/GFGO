package ext.appo.part.util;

import java.io.File;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Workbook;

import wt.util.WTException;
import ext.appo.part.beans.ProductLineConfigBean;
import ext.lang.office.PIExcelReader;

public class ProductLineConfigExcelReader extends PIExcelReader<ProductLineConfigBean>{

	private static final long serialVersionUID = 1L;

	public ProductLineConfigExcelReader(){
		super() ;
	}
	
	public ProductLineConfigExcelReader(File file) throws WTException {
		super(file);
	}

	public ProductLineConfigExcelReader(InputStream inStream) throws WTException {
		super(inStream);
	}

	public ProductLineConfigExcelReader(String filePath) throws WTException {
		super(filePath);
	}

	public ProductLineConfigExcelReader(Workbook workbook) throws WTException {
		super(workbook);
	}
}
