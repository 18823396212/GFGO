package ext.appo.bom.util;

import java.io.File;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Workbook;

import wt.util.WTException;
import ext.appo.bom.beans.ImportProductStructureExcelBean;
import ext.lang.office.PIExcelReader;

public class ImportProductStructureExcelReader extends PIExcelReader<ImportProductStructureExcelBean>{

	private static final long serialVersionUID = 1L;

	public ImportProductStructureExcelReader(){
		super() ;
	}
	
	public ImportProductStructureExcelReader(File file) throws WTException {
		super(file);
	}

	public ImportProductStructureExcelReader(InputStream inStream) throws WTException {
		super(inStream);
	}

	public ImportProductStructureExcelReader(String filePath) throws WTException {
		super(filePath);
	}

	public ImportProductStructureExcelReader(Workbook workbook) throws WTException {
		super(workbook);
	}
}
