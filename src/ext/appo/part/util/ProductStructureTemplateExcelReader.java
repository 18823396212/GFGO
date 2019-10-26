package ext.appo.part.util;

import java.io.File;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Workbook;

import wt.util.WTException;
import ext.appo.part.beans.ProductStructureTemplateExcelBean;
import ext.lang.office.PIExcelReader;

public class ProductStructureTemplateExcelReader extends PIExcelReader<ProductStructureTemplateExcelBean>{

	private static final long serialVersionUID = 1L;

	public ProductStructureTemplateExcelReader(){
		super() ;
	}
	
	public ProductStructureTemplateExcelReader(File file) throws WTException {
		super(file);
	}

	public ProductStructureTemplateExcelReader(InputStream inStream) throws WTException {
		super(inStream);
	}

	public ProductStructureTemplateExcelReader(String filePath) throws WTException {
		super(filePath);
	}

	public ProductStructureTemplateExcelReader(Workbook workbook) throws WTException {
		super(workbook);
	}
}
