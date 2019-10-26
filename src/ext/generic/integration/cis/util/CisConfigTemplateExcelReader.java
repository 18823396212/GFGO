package ext.generic.integration.cis.util;

import java.io.File;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Workbook;

import wt.util.WTException;
import ext.generic.integration.cis.beans.CisConfigExcelBean;
import ext.lang.office.PIExcelReader;

public class CisConfigTemplateExcelReader extends PIExcelReader<CisConfigExcelBean>{

	private static final long serialVersionUID = 1L;

	public CisConfigTemplateExcelReader(){
		super() ;
	}
	
	public CisConfigTemplateExcelReader(File file) throws WTException {
		super(file);
	}

	public CisConfigTemplateExcelReader(InputStream inStream) throws WTException {
		super(inStream);
	}

	public CisConfigTemplateExcelReader(String filePath) throws WTException {
		super(filePath);
	}

	public CisConfigTemplateExcelReader(Workbook workbook) throws WTException {
		super(workbook);
	}
}
