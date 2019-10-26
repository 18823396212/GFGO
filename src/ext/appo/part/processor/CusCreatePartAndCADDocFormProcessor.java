package ext.appo.part.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.lwc.server.LWCStructEnumAttTemplate;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.windchill.enterprise.part.forms.CreatePartAndCADDocFormProcessor;

import ext.appo.util.excel.AppoExcelUtil;
import ext.generic.generatenumber.GenerateNumber;
import ext.generic.generatenumber.rule.util.PartAttributeRule;
import ext.generic.integration.cis.util.CISCSMUtil;
import ext.lang.file.FileConstantIfc;
import ext.pi.PIException;
import ext.pi.core.PIAccessHelper;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIClassificationHelper;
import wt.access.AccessPermission;
import wt.fc.IdentityHelper;
import wt.fc.PersistenceHelper;
import wt.folder.Folder;
import wt.folder.SubFolder;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.part.WTPartMasterIdentity;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.util.WTException;

public class CusCreatePartAndCADDocFormProcessor extends CreatePartAndCADDocFormProcessor {

	private static String CLASSNAME = CusCreatePartAndCADDocFormProcessor.class.getName();
	private static final Logger LOGGER = LogR.getLogger(CLASSNAME);
	private final String SEPARATOR = FileConstantIfc.UNIX_PATH_SEPARATOR;
	// 8.30change by chenjintian>>>>>>>>
	private static final Logger log = null;
	private String cls = "";
	private String defultUnit = "";
	private String location = "";
	private String name = "";
	private String enName = "";
	HashMap textMap = new HashMap();
	HashMap textAreaMap = new HashMap();
	HashMap comboxMap = new HashMap();
	LWCStructEnumAttTemplate clsNodeAttTemplate;
	// @Override
	// public FormResult doOperation(NmCommandBean arg0, List<ObjectBean> arg1)
	// throws WTException {
	// FormResult doOperation = super.doOperation(arg0, arg1);
	//
	// ObjectBean objectBean = arg1.get(0);
	// Object object = objectBean.getObject();
	// if(object!=null && object instanceof WTPart) {
	// WTPart part = (WTPart) object;
	// checkAttribute(part);
	// setNumber(part);
	// }
	//
	// return doOperation;
	// }

	@Override
	public FormResult preProcess(NmCommandBean nmcommandBean, List<ObjectBean> list) throws WTException {
		initial(nmcommandBean, list);
		preCheckLocation();
		// preCheckNameLength();

		return super.preProcess(nmcommandBean, list);
	}

	private void preCheckLocation() throws WTException {
		if (location != null) {
			if (location.split("/").length == 2) {
				throw new WTException("不能在根目录创建零部件！");
			}
		}
	}

	private void initial(NmCommandBean nmcommandBean, List<ObjectBean> list) throws WTException {
		textMap = nmcommandBean.getText();
		textAreaMap = nmcommandBean.getTextArea();
		comboxMap = nmcommandBean.getComboBox();
		Iterator it = textMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			Object key = entry.getKey();
			Object val = entry.getValue();
			if (key instanceof String) {
				String k = (String) key;

				if (key.toString().contains("Location")) {
					location = val.toString();

				}
			}
		}
		// clsNodeAttTemplate =
		// ClassificationUtil.getLWCStructEnumAttTemplateByName(cls);
	}

	// >>>>>>>>>>>>>>>change by chenjintian 8.30
	/**
	 * 设置编码
	 * 
	 * @param part
	 *            部件
	 * @throws WTException
	 */
	public void setNumber(WTPart part) throws WTException {
		part = (WTPart) PersistenceHelper.manager.save(part);

		LOGGER.debug("part=======" + part.getName());
		// 获取当前用户
		WTPrincipal principal = SessionHelper.manager.getPrincipal();
		try {
			// 赋予修改编码权限
			PIAccessHelper.service.grantPersistablePermissions(part,
					WTPrincipalReference.newWTPrincipalReference(principal), AccessPermission.MODIFY_IDENTITY, true);
			PartAttributeRule rule = new PartAttributeRule(part);
			if (rule.canEncodeNumberByRule()) {
				LOGGER.debug("part设置编码！");
				rule.updatePartNumber();
			} else {
				// 设置编码
				GenerateNumber gn = new GenerateNumber(part);
				gn.updatePartNumber();
			}
		} finally {
			PIAccessHelper.service.removePersistablePermissions(part,
					WTPrincipalReference.newWTPrincipalReference(principal), AccessPermission.MODIFY_IDENTITY, true);
		}
	}

	/**
	 * 校验分类
	 * 
	 * @param part
	 * @param classificationName
	 * @param productName
	 * @throws PIException
	 * @throws WTException
	 */
	private void checkAttribute(WTPart part) throws WTException {
		// 获取部件位置
		Folder folder = (Folder) PIAttributeHelper.service.getValue(part, "folder.id");
		// 文件夹名称
		String folderName = "";
		if (folder != null && folder instanceof SubFolder) {
			String path = folder.getFolderPath();
			if (path.indexOf("/") > -1) {
				String[] split = path.split("/");
				folderName = split[2];
			}
		} else {
			// 不允许在根目录创建部件!
			throw new WTException("不允许在根目录创建部件!");
		}
		// 获取分类内部值
		String value = (String) PIAttributeHelper.service.getValue(part, "Classification");
		// 获取分类全路径
		String nodeHierarchy = PIClassificationHelper.service.getNodeHierarchy(value);

		// 分类中大类的内部值
		String nodeSecondInsideValue = "";
		if (nodeHierarchy != null) {
			if (nodeHierarchy.indexOf(SEPARATOR) > -1) {
				String[] split = nodeHierarchy.split(SEPARATOR);
				nodeSecondInsideValue = split[1];
			} else {
				nodeSecondInsideValue = nodeHierarchy;
			}
		}
		// 配置表中的文件夹名称
		Map<String, String> readSheet5 = new AppoExcelUtil().readSheet5();
		String folderNameForExcel = readSheet5.get(nodeSecondInsideValue);
		// 名称包含
		if (folderName.contains(folderNameForExcel)) {
			return;
		} else {
			// 不允许在根目录创建部件!
			throw new WTException("业务规则规定:/n当前文件夹:" + folderName + "/n不允许创建分类大类为:" + folderNameForExcel + "的部件!");
		}
	}

	@Override
	public FormResult postProcess(NmCommandBean nmcommandBean, List<ObjectBean> arg1) throws WTException {
		FormResult postProcess = super.postProcess(nmcommandBean, arg1);

		ObjectBean objectBean = arg1.get(0);
		WTPart part = (WTPart) objectBean.getObject();

		SessionContext previous = SessionContext.newContext();
		try {
			SessionHelper.manager.setAdministrator();
			if (part != null) {
				// 回填所属产品线
				HashMap comboBox = nmcommandBean.getComboBox();
				Set keySet = comboBox.keySet();
				Iterator iterator = keySet.iterator();
				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					if (key.contains("sscpx")) {
						Object value = comboBox.get(key);
						if (value != null) {
							if (value instanceof ArrayList<?>) {
								part = (WTPart) PIAttributeHelper.service.forceUpdateSoftAttribute(part, "sscpx",
										((ArrayList<?>) value).get(0));
							} else {
								part = (WTPart) PIAttributeHelper.service.forceUpdateSoftAttribute(part, "sscpx",
										value);
							}
						}
					} else if (key.contains("ssxm")) {
						Object value = comboBox.get(key);
						if (value != null) {
							if (value instanceof ArrayList<?>) {
								part = (WTPart) PIAttributeHelper.service.forceUpdateSoftAttribute(part, "ssxm",
										((ArrayList<?>) value).get(0));
							} else {
								part = (WTPart) PIAttributeHelper.service.forceUpdateSoftAttribute(part, "ssxm", value);
							}
						}
					}
				}

				Map<String, String> readSheet5 = new AppoExcelUtil().readSheet5();
				String cls = (String) PIAttributeHelper.service.getValue(part, "Classification");
				if (readSheet5.containsKey(cls)) {
					// 获取容器
					String container = part.getContainer().getName();
					String productline = readSheet5.get(cls);
					LWCStructEnumAttTemplate node = CISCSMUtil.getClassificationByInternalName(cls);
					if (!container.startsWith("原材料库")) {
						if (!productline.startsWith(container)) {
							throw new WTException("所选物料分类不能在" + container + "中创建，请到" + productline + "中创建！");
						}
					}
				}
				// 获取配置表中需要修改名称的分类Map
				Map<String, String> readSheet3 = new AppoExcelUtil().readSheet3();
				String key = (String) PIAttributeHelper.service.getValue(part, "Classification");
				if (readSheet3.containsKey(key)) {
					WTPartMaster master = (WTPartMaster) part.getMaster();
					WTPartMasterIdentity identity = (WTPartMasterIdentity) master.getIdentificationObject();
					identity.setName(readSheet3.get(key));
					IdentityHelper.service.changeIdentity(master, identity);
				}

				// 检查创建位置
				checkAttribute(part);
				// mao
				String container = part.getContainer().getName();
				String viewName = part.getViewName();
				if ("原材料库".equals(container) && "Manufacturing".equals(viewName)) {
					throw new WTException("原材料库中创建物料时，视图选项只能选择Design！");
				}
				// 设置编码
				setNumber(part);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		} finally {
			SessionContext.setContext(previous);
		}

		return postProcess;
	}
}
