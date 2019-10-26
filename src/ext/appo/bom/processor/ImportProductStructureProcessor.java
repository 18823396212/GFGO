package ext.appo.bom.processor;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTKeyedHashMap;
import wt.fc.collections.WTKeyedMap;
import wt.log4j.LogR;
import wt.occurrence.OccurrenceHelper;
import wt.part.PartUsesOccurrence;
import wt.part.Quantity;
import wt.part.QuantityUnit;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.part.WTPartSubstituteLink;
import wt.part.WTPartUsageLink;
import wt.util.WTException;
import wt.vc.views.View;

import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.appo.bom.beans.ImportProductStructureExcelBean;
import ext.appo.bom.util.ImportProductStructureExcelReader;
import ext.appo.ecn.constants.ChangeConstants;
import ext.lang.PIStringUtils;
import ext.pi.PIException;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PICoreHelper;
import ext.pi.core.PIPartHelper;

/***
 * BOM结构搭建类
 * 
 * @author Administrator
 *
 */
public class ImportProductStructureProcessor extends DefaultObjectFormProcessor{
	
	private static final String CLASSNAME = ImportProductStructureProcessor.class.getName() ;
	private static final Logger LOG = LogR.getLogger(CLASSNAME) ;

	@Override
	public FormResult postProcess(NmCommandBean nmCommandBean, List<ObjectBean> paramList) throws WTException {
		FormResult formResult = new FormResult();
		formResult.setStatus(FormProcessingStatus.SUCCESS);
		
		try {
			WTPart parentPart = (WTPart)nmCommandBean.getActionOid().getRefObject() ;
			if(LOG.isDebugEnabled()){
				LOG.debug("parentPart : " + parentPart.getDisplayIdentity()) ;
			}
			
			// 获取用户上传的文件对象
			HttpServletRequest request = nmCommandBean.getRequest();
			File file = (File) request.getAttribute("txtFile");
			if(LOG.isDebugEnabled()){
				LOG.debug("File : " + file.getName());
			}
			
			// 读取导入Excel文件中的内容
			ImportProductStructureExcelReader reader = new ImportProductStructureExcelReader(file) ;
			reader.read(); 
			List<ImportProductStructureExcelBean> beanList = reader.getBeanList() ;
			
			// 检查导入数据是否符合要求
			checkDatas(parentPart, beanList) ;
			
			// BOM结构搭建
			Map<ImportProductStructureExcelBean, WTPartUsageLink> partLinkMap = createPartUsageLinks(parentPart, beanList) ;
			if(partLinkMap == null || partLinkMap.size() == 0){
				throw new WTException("结构搭建失败!") ;
			}
			
			// 替代关系搭建
			createSubstituteLinks(beanList, partLinkMap) ;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage()) ;
		}
		
		return formResult ;
	}

	/***
	 * 导入前数据检查
	 * 
	 * @param parentPart
	 *            顶层部件(操作部件)
	 * @param beanList
	 *            解析导入Excel数据集合
	 * @throws WTException
	 */
	public void checkDatas(WTPart parentPart, List<ImportProductStructureExcelBean> beanList) throws WTException {
		if(beanList == null || beanList.size() < 6){
			throw new WTException("导入表格中无数据!") ;
		}
		
		// 父件视图
		View view = (View)parentPart.getView().getObject() ;
		if(LOG.isDebugEnabled()){
			LOG.debug("View : " + view.toString());
		}
		// 其他视图
		String viewName = "" ;
		if(view.getName().equals("Design")){
			viewName = "Manufacturing" ;
		}else{
			viewName = "Design" ;
		}
		
		// 获取系统所有单位
		QuantityUnit[] units = QuantityUnit.getQuantityUnitSet() ;
		
		StringBuilder errorMsg = new StringBuilder() ;
		try {
			for (int i = 5; i < beanList.size(); i++) {
				ImportProductStructureExcelBean excelBean = beanList.get(i) ;
				// ‘特定替代料’检查
				String replaceNumber = excelBean.getReplaceNumber() ;
				if(PIStringUtils.isNotNull(replaceNumber)){
					// 部件Master
					WTPart replacePart = PIPartHelper.service.findWTPart(replaceNumber, view) ;
					if(replacePart == null){
						// 获取其他视图对象
						replacePart = PIPartHelper.service.findWTPart(replaceNumber, viewName) ;
						if(replacePart == null){
							errorMsg.append("第" + (i + 1) + "行：替代料 "+ replaceNumber +" 不存在!") ;
							errorMsg.append("\n") ;
						}else{
							excelBean.setReplacePart(replacePart); 
						}
					}else{
						excelBean.setReplacePart(replacePart); 
					}
					// 替代料层级必须为空
					if(PIStringUtils.isNotNull(excelBean.getLevel())){
						errorMsg.append("第" + (i + 1) + "行为替代料信息 ‘阶层’属性必须为空! ") ;
						errorMsg.append("\n") ;
					}
					continue ;
				}
				
				// ‘阶层’检查
				if(PIStringUtils.isNull(excelBean.getLevel())){
					errorMsg.append("第" + (i + 1) + "行：阶层 为空!") ;
					errorMsg.append("\n") ;
					continue ;
				}else{
					if(excelBean.getLevel().equals("0")){
						continue ;
					}
					// ‘子件编码’检查
					String partNumber = excelBean.getNumber() ;
					if(PIStringUtils.isNull(partNumber)){
						errorMsg.append("第" + (i + 1) + "行：编码 为空!") ;
						errorMsg.append("\n") ;
						continue ;
					}
					WTPart part = PIPartHelper.service.findWTPart(partNumber, view) ;
					if(part == null){
						// 获取其他视图对象
						part = PIPartHelper.service.findWTPart(partNumber, viewName) ;
						if(part == null){
							errorMsg.append("第" + (i + 1) + "行：编码 "+ partNumber +" 零部件不存在或没有权限!") ;
							errorMsg.append("\n") ;
							continue ;
						}else{
							excelBean.setPart(part);
						}
					}else{
						excelBean.setPart(part);
					}
				}
				
				// 单位检查
				String partUnit = excelBean.getUnit() ;
				if(PIStringUtils.isNotNull(partUnit)){
					Boolean isExist = false ;
					for(QuantityUnit quantityUnit : units){
						if(quantityUnit.toString().equals(partUnit) ||
								quantityUnit.getLocalizedMessage(Locale.CHINA).equals(partUnit)){
							isExist = true ;
							excelBean.setQuantityUnit(quantityUnit);
							break ;
						}
					}
					if(!isExist){
						errorMsg.append("第" + (i + 1) + "行：单位 "+ partUnit +" 未在系统中定义!") ;
						errorMsg.append("\n") ;
						continue ;
					}
				}else{
					errorMsg.append("第" + (i + 1) + "行：单位 不能为空!") ;
					errorMsg.append("\n") ;
					continue ;
				}
				
				// 数量检查
				if(PIStringUtils.isNotNull(partUnit)){
					if(partUnit.contains("个")){
						String quantity = excelBean.getQuantity() ;
						if(PIStringUtils.isNotNull(quantity) && quantity.contains(".")){
							errorMsg.append("第" + (i + 1) + "行：单位 "+ partUnit +" 数量不能存在小数!") ;
							errorMsg.append("\n") ;
						}
					}
				}
				
				// 位号检查
				String siteLine = excelBean.getSiteLine() ;
				if(PIStringUtils.isNotNull(siteLine)){
					if(siteLine.contains("-") || siteLine.contains("-")){
						errorMsg.append("第" + (i + 1) + "行：行号包含了‘-’字符，无法导入系统!") ;
						errorMsg.append("\n") ;
					}
				}
				
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage()) ;
		} finally{
			if(errorMsg.length() > 0){
				throw new WTException(errorMsg.toString()) ;
			}
		}
	}
	
	/***
	 * BOM结构搭建
	 * 
	 * @param parentPart
	 *            顶层部件(操作部件) 
	 * @param beanList
	 *            导入结构数据集合
	 * @return
	 * @throws WTException
	 */
	public Map<ImportProductStructureExcelBean, WTPartUsageLink> createPartUsageLinks(WTPart parentPart,
			List<ImportProductStructureExcelBean> beanList) throws WTException{
		Map<ImportProductStructureExcelBean, WTPartUsageLink> returnMap = new HashMap<ImportProductStructureExcelBean, WTPartUsageLink>();
		if(parentPart == null || beanList == null || beanList.size() == 0){
			return returnMap ;
		}
		
		// 提取部件与部件关系数据
		Map<ImportProductStructureExcelBean, Collection<ImportProductStructureExcelBean>> extractPartLinks = extractPartLinks(beanList) ;
		if(extractPartLinks == null || extractPartLinks.size() == 0){
			throw new WTException("结构关系提取异常，请检查导入数据是否正确!") ;
		}
		
		try {
			WTPart parent = null ;
			for(Map.Entry<ImportProductStructureExcelBean, Collection<ImportProductStructureExcelBean>> entryMap : extractPartLinks.entrySet()){
				// 父件
				parent = entryMap.getKey().getPart() == null ? parentPart : entryMap.getKey().getPart() ;
				// 查询父件下是否存在BOM
				WTCollection findChildren = PIPartHelper.service.findChildren(parent) ;
				if(findChildren != null && findChildren.size() > 0){
					throw new WTException(parent.getDisplayIdentity() + " 已经存在BOM结构，不允许导入！请移除BOM结构后再进行导入。") ;
				}
				for(ImportProductStructureExcelBean excelBean : entryMap.getValue()){
					// 子件
					WTPart childPart = excelBean.getPart() ;
					if(childPart == null){
						continue ;
					}
					// 检查关系是否存在
//					QueryResult qr = findUsageLinks(parent, childPart.getMaster()) ;
//					if(qr != null && qr.size() > 0){
//						LOG.error("parent :" + parent.getDisplayIdentity() + " childPart :" + childPart.getDisplayIdentity() + " 存在关联关系!"); 
//						if(qr.hasMoreElements()){
//							Object object = qr.nextElement() ;
//							if(object instanceof ObjectReference){
//								object = ((ObjectReference)object).getObject() ;
//							}
//							returnMap.put(excelBean, (WTPartUsageLink)object) ;
//						}
//						continue ;
//					}
					if(!PICoreHelper.service.isCheckout(parent)){
						parent = (WTPart)PICoreHelper.service.checkoutObject(parent) ;
					}
					// 创建UsageLink对象
					WTPartUsageLink newUsageLink = WTPartUsageLink.newWTPartUsageLink(parent, (WTPartMaster) childPart.getMaster()) ;
					// 设置单位数量
					if(excelBean.getQuantityUnit() != null){
						Quantity quantity = new Quantity();
						quantity.setUnit(excelBean.getQuantityUnit());
						if(PIStringUtils.isNotNull(excelBean.getQuantity())){
							quantity.setAmount(Double.parseDouble(excelBean.getQuantity()));
						}
						newUsageLink.setQuantity(quantity);
					}
					newUsageLink = (WTPartUsageLink) PersistenceHelper.manager.save(newUsageLink);
					// 设置位号
					String[] locationArray = null ;
					if(PIStringUtils.isNotNull(excelBean.getSiteLine())){
						if(excelBean.getSiteLine().contains(ChangeConstants.USER_KEYWORD2)){
							locationArray = excelBean.getSiteLine().split(ChangeConstants.USER_KEYWORD2) ;
						}else{
							locationArray = new String[]{excelBean.getSiteLine()} ;
						}
					}
					if(locationArray != null){
						WTKeyedMap map = new WTKeyedHashMap();
			            for (String location : locationArray) {
			                PartUsesOccurrence occurrence = PartUsesOccurrence.newPartUsesOccurrence(newUsageLink);
			                occurrence.setName(location);
			                map.put(occurrence, null);
			            }
			            OccurrenceHelper.service.saveUsesOccurrenceAndData(map);
					}
					// 设置IBA属性
					Map<String, Object> ibaMap = new HashMap<String, Object>() ;
					ibaMap.put("bom_note", excelBean.getRemark()) ;
					ibaMap.put("stockGrade", excelBean.getZdchdj()) ;
					newUsageLink = (WTPartUsageLink)PIAttributeHelper.service.forceUpdateSoftAttributes(newUsageLink, ibaMap) ;
					
					returnMap.put(excelBean, newUsageLink) ;
				}
				if(PICoreHelper.service.isCheckout(parent)){
					parent = (WTPart)PICoreHelper.service.checkinObject(parent) ;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage()) ;
		}
		
		return returnMap ;
	}
	
	/***
	 * 提取部件与部件关系数据
	 * 
	 * @param beanList
	 * @return
	 * @throws WTException
	 */
	public Map<ImportProductStructureExcelBean, Collection<ImportProductStructureExcelBean>> extractPartLinks(List<ImportProductStructureExcelBean> beanList) throws WTException{
		Map<ImportProductStructureExcelBean, Collection<ImportProductStructureExcelBean>> partLinkMap = new HashMap<ImportProductStructureExcelBean, Collection<ImportProductStructureExcelBean>>() ;
		if(beanList == null || beanList.size() == 0){
			return partLinkMap ;
		}
		// 临时数据
		List<ImportProductStructureExcelBean> tempArray = beanList ;
		try {
			Collection<ImportProductStructureExcelBean> childArray = null ;
			for(int i = beanList.size() - 1 ; i > 3 ; i--){
				ImportProductStructureExcelBean excelBean = beanList.get(i) ;
				if(PIStringUtils.isNull(excelBean.getLevel())){
					continue ;
				}
				
				Integer integer = (Integer.parseInt(excelBean.getLevel()) - 1) ;
				if(integer == -1){
					continue ;
				}
				
				String parentLevel = integer.toString() ;
				Boolean bol = false ;
				// 父件
				ImportProductStructureExcelBean parentExcelBean = null ;
				for(int j = tempArray.size() - 1 ; j > 3; j --){
					parentExcelBean = tempArray.get(j) ;
					if(excelBean == parentExcelBean){
						bol = true ;
					}
					if(bol){
						if(parentLevel.equals(parentExcelBean.getLevel())){
							break ;
						}
					}
					parentExcelBean = null ;
				}
				if(parentExcelBean != null){
					if(partLinkMap.containsKey(parentExcelBean)){
						childArray = partLinkMap.get(parentExcelBean) ;
					}else{
						childArray = new HashSet<ImportProductStructureExcelBean>() ;
					}
					childArray.add(excelBean) ;
					partLinkMap.put(parentExcelBean, childArray) ;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage()) ;
		}
		
		return partLinkMap ;
	}
	
	/***
	 * 查询部件与部件间的UsageLink
	 * 
	 * @param parent
	 *            父件
	 * @param childMaster
	 *            子件Master
	 * @return
	 * @throws WTException
	 */
	public QueryResult findUsageLinks(WTPart parent, WTPartMaster childMaster) throws WTException {
		try {
			if (parent == null || childMaster == null) {
				return (new QueryResult());
			}
			QueryResult qr = PersistenceHelper.manager.find(WTPartUsageLink.class, parent, WTPartUsageLink.USED_BY_ROLE, childMaster);
			return (qr == null ? (new QueryResult()) : qr);
		} catch (WTException e) {
			throw new PIException(e);
		}
	}
	
	/***
	 * 替代关系搭建
	 * 
	 * @param beanList
	 *            导入数据集合
	 * @param partLinksMap
	 *            结构部件集合
	 * @throws WTException
	 */
	public void createSubstituteLinks(List<ImportProductStructureExcelBean> beanList, Map<ImportProductStructureExcelBean, WTPartUsageLink> partLinksMap) throws WTException{
		if(beanList == null || beanList.size() == 0 || partLinksMap == null || partLinksMap.size() == 0){
			return ;
		}
		
		// 提取替代关系
		Map<ImportProductStructureExcelBean, Collection<ImportProductStructureExcelBean>> subLinksMap = extractSubstituteLinks(beanList) ;
		for(Map.Entry<ImportProductStructureExcelBean, Collection<ImportProductStructureExcelBean>> entryMap : subLinksMap.entrySet()){
			WTPartUsageLink usageLink = null ;
			if(partLinksMap.containsKey(entryMap.getKey())){
				usageLink = partLinksMap.get(entryMap.getKey()) ;
			}
			if(usageLink == null){
				continue ;
			}
			for(ImportProductStructureExcelBean excelBean : entryMap.getValue()){
				// 替代物料
				WTPart replacePart = excelBean.getReplacePart() ;
				// 查询替代料是否存在
				QueryResult qr = findSubLinks(usageLink, (WTPartMaster) replacePart.getMaster()) ;
				if(qr != null && qr.size() > 0){
					LOG.error("UsageLink :" + usageLink.getDisplayIdentity() + " replacePart :" + replacePart.getDisplayIdentity() + " 存在替代关系!"); 
					continue ;
				}
				WTPartSubstituteLink newSubstituteLink = WTPartSubstituteLink.newWTPartSubstituteLink(usageLink, (WTPartMaster) replacePart.getMaster()) ; 
				PersistenceHelper.manager.save(newSubstituteLink);
			}
		}
	}

	/***
	 * 提取替代关系
	 * 
	 * @param beanList
	 *            导入数据集合
	 * @return
	 * @throws WTException
	 */
	public Map<ImportProductStructureExcelBean, Collection<ImportProductStructureExcelBean>> extractSubstituteLinks(List<ImportProductStructureExcelBean> beanList) throws WTException {
		Map<ImportProductStructureExcelBean, Collection<ImportProductStructureExcelBean>> subLinksMap = new HashMap<ImportProductStructureExcelBean, Collection<ImportProductStructureExcelBean>>() ;
		if(beanList == null || beanList.size() == 0){
			return subLinksMap ;
		}
		
		Collection<ImportProductStructureExcelBean> subArray = new HashSet<ImportProductStructureExcelBean>() ;
		for (int i = 4; i < beanList.size(); i++) {
			ImportProductStructureExcelBean excelBean = beanList.get(i) ;
			if(PIStringUtils.isNotNull(excelBean.getLevel())){
				subArray = new HashSet<ImportProductStructureExcelBean>() ;
				subLinksMap.put(excelBean, subArray) ;
				continue ;
			}
			subArray.add(excelBean) ;
		}
		
		// 子件编码为A或B开头的都没有替代料，导入时要做校验。
		for(Map.Entry<ImportProductStructureExcelBean, Collection<ImportProductStructureExcelBean>> entryMap : subLinksMap.entrySet()){
			WTPart part = entryMap.getKey().getPart() ;
			if(part != null){
				if(part.getNumber().startsWith("A") || part.getNumber().startsWith("B")){
					if(entryMap.getValue().size() > 0){
						throw new WTException(part.getDisplayIdentity() + " 不能搭建替代料") ;
					}
				}
			}
		}
		
		return subLinksMap;
	}
	
	/***
	 * 查询UsageLink与部件的替代关系
	 * 
	 * @param usageLink
	 * @param replacePartMaster
	 *            替代部件Master
	 * @return
	 * @throws WTException
	 */
	public QueryResult findSubLinks(WTPartUsageLink usageLink, WTPartMaster replacePartMaster) throws WTException {
		try {
			if (usageLink == null || replacePartMaster == null) {
				return (new QueryResult());
			}
			QueryResult qr = PersistenceHelper.manager.find(WTPartSubstituteLink.class, usageLink, WTPartSubstituteLink.SUBSTITUTE_FOR_ROLE, replacePartMaster);
			return (qr == null ? (new QueryResult()) : qr);
		} catch (WTException e) {
			throw new PIException(e);
		}
	}
}
