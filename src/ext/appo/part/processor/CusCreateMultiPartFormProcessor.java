package ext.appo.part.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import wt.log4j.LogR;
import wt.part.WTPart;
import wt.pdmlink.PDMLinkProduct;
import wt.session.SessionServerHelper;
import wt.util.WTException;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.windchill.enterprise.part.forms.CreateMultiPartFormProcessor;

import ext.generic.generatenumber.GenerateNumber;
import ext.generic.generatenumber.rule.util.PartAttributeRule;
import ext.lang.PIExcelUtils;
import ext.pi.PIException;
import ext.pi.core.PICoreHelper;

public class CusCreateMultiPartFormProcessor extends CreateMultiPartFormProcessor {
	private static final String CLASSNAME = CusCreateMultiPartFormProcessor.class.getName();
	private static final Logger logger = LogR.getLogger(CLASSNAME);

	public static String filePath;

	static {
		try {
			String codebase = PICoreHelper.service.getCodebase();
			filePath = codebase + File.separator + "ext" + File.separator + "appo" + File.separator + "part"
					+ File.separator + "appoClassificationConfigure.xlsx";
		} catch (PIException e) {
			e.printStackTrace();
		}
	}

	@Override
	public FormResult postProcess(NmCommandBean nmcommandbean, List list) throws WTException {
		FormResult formresult = new FormResult(FormProcessingStatus.SUCCESS);
		formresult = super.postProcess(nmcommandbean, list);
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		StringBuffer sbf = new StringBuffer("");
		NmOid pageOid = nmcommandbean.getActionOid();
		Object refObject = pageOid.getRefObject();
		String productName = "";
		try {
			if (refObject != null && refObject instanceof PDMLinkProduct) {
				productName = "产品库";

				for (int i = 0; i < list.size(); i++) {
					ObjectBean objBean = (ObjectBean) list.get(i);
					if (objBean != null) {
						Object obj = objBean.getObject();
						if (obj != null) {
							if (obj instanceof WTPart) {
								WTPart part = (WTPart) obj;
								logger.debug("part===="+part.getName());
								Map<String, String> text = objBean.getText();
								String classificationName = text.get("null_col_Classification");
								logger.debug("classificationName===="+classificationName);
								Sheet sheetAt = null;

								Workbook workbook = PIExcelUtils.getWorkbook(new File(filePath));
								sheetAt = workbook.getSheet(productName);
								int lastRowNum = sheetAt.getLastRowNum();

								List<String> list2 = new ArrayList<String>();
								for (int j = 1; j < lastRowNum; j++) {
									Row row = sheetAt.getRow(j);
									Cell classNameCell = row.getCell(0);
									String className = PIExcelUtils.getCellValue(classNameCell).toString();
									list2.add(className);
								}
								if (!list2.contains(classificationName)) {
									throw new WTException("部件的分类不在配置表中！");
								} else {
									//设置编码
									CusCreatePartAndCADDocFormProcessor processor = new CusCreatePartAndCADDocFormProcessor();
									processor.setNumber(part);
								}
							}
						}

					}

				}
			} else {
				for (int i = 0; i < list.size(); i++) {
					ObjectBean objBean = (ObjectBean) list.get(i);
					if (objBean != null) {
						Object obj = objBean.getObject();
						if (obj != null) {
							if (obj instanceof WTPart) {
								WTPart part = (WTPart) obj;
								logger.debug("part===="+part.getName());
								CusCreatePartAndCADDocFormProcessor processor = new CusCreatePartAndCADDocFormProcessor();
								processor.setNumber(part);
							}
						}

					}

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		} finally {
			SessionServerHelper.manager.setAccessEnforced(enforce);
		}

		return formresult;
	}

}
