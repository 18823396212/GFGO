package ext.appo.bom.change;

import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFSheet;

/**
 * 增强的读取Excel模板功能类 用来处理Bom删除修改模板清单
 */
public class BomChangeReader extends ExcelReader {
	private static final long serialVersionUID = 1L;
	protected HSSFSheet secondhssfsheet = null;
	protected HSSFSheet thirdhssfsheet = null;

	public BomChangeReader(String fileName) {
		super(fileName);
	}

	public BomChangeReader(String fileName, String sheetCount) {
		super(fileName);
	}

	public void readData() {
		Vector keyVector = new Vector();

		for (int j = 2; j < 65536; j++) {
			String key = getCell(1, j, this.sourcehssfsheet);

			if (key.equals(""))
				break;

			keyVector.add(key);
			System.out.println("key=" + key);

		}

		for (int i = 2; i < 65536; i++) {
			String number = getCell(i, 1, this.sourcehssfsheet);

			if (number.equals(""))
				break;
			ChangeBOMObject changeBOMObject = new ChangeBOMObject();
			changeBOMObject.setId(getCell(i, 1, this.sourcehssfsheet).trim());
			changeBOMObject.setParentnumber(getCell(i, 2, this.sourcehssfsheet).trim());
			changeBOMObject.setChangetype(getCell(i, 3, this.sourcehssfsheet).trim());
			changeBOMObject.setChildnumber(getCell(i, 4, this.sourcehssfsheet).trim());
			changeBOMObject.setQuantity(getCell(i, 5, this.sourcehssfsheet).trim());
			changeBOMObject.setOldquantity(getCell(i, 6, this.sourcehssfsheet).trim());
			changeBOMObject.setOccurrencmodify(getCell(i, 7, this.sourcehssfsheet).trim());
			changeBOMObject.setOccurrence(getCell(i, 8, this.sourcehssfsheet).trim());
			changeBOMObject.setUnit(getCell(i, 9, this.sourcehssfsheet).trim());
			dataVector.add(changeBOMObject);

		}
	}

}
