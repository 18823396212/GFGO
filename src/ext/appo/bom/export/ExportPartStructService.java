package ext.appo.bom.export;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTList;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.occurrence.OccurrenceHelper;
import wt.part.PartUsesOccurrence;
import wt.part.Quantity;
import wt.part.WTPart;
import wt.part.WTPartAlternateLink;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartSubstituteLink;
import wt.part.WTPartUsageLink;
import wt.util.WTException;
import wt.vc.config.LatestConfigSpec;
import wt.vc.views.View;
import ext.appo.bom.beans.ImportProductStructureExcelBean;
import ext.appo.ecn.common.util.ChangePartQueryUtils;
import ext.appo.ecn.constants.ChangeConstants;
import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.lang.PIStringUtils;
import ext.pi.core.PIAttributeHelper;
import ext.pi.core.PIPartHelper;

/***
 * BOM导出：将Windchill数据转换为可导出的Java对象
 * 
 * @author Administrator
 *
 */
public class ExportPartStructService implements RemoteAccess, Serializable{

	private static final long serialVersionUID = 1L;
	
	private static final String CLASSNAME = ExportPartStructService.class.getName() ;
	private static final Logger LOG = LogR.getLogger(CLASSNAME) ;

	/***
	 * 提取BOM信息并将该信息转化为可导出的数据对象
	 * 
	 * @param parentPart
	 *            顶层部件
	 * @return
	 * @throws WTException
	 */
	public List<ImportProductStructureExcelBean> extractDatas(WTPart parentPart) throws WTException{
		List<ImportProductStructureExcelBean> extractDatas = new ArrayList<ImportProductStructureExcelBean>() ;
		if(parentPart == null){
			return extractDatas ;
		}
		if(LOG.isDebugEnabled()){
			LOG.debug("parentPart : " + parentPart.getDisplayIdentity());
		}
		
		Integer level = 0 ;
		ImportProductStructureExcelBean excelBean = new ImportProductStructureExcelBean() ;
		excelBean.setSerial(extractDatas.size() + 1 + "");
		excelBean.setLevel(level + "");
		excelBean.setNumber(parentPart.getNumber());
		excelBean.setPartName(parentPart.getName());
		excelBean.setPartVersion(CommonPDMUtil.getVersion(parentPart));
		// 规格描述
		Object ggms = PIAttributeHelper.service.getValue(parentPart, "ggms") ;
		excelBean.setGgms(ggms == null ? "" : (String)ggms);
		extractDatas.add(excelBean) ;
		
		//处理子件全局替代
		WTCollection wtcol = WTPartHelper.service.getAlternateLinks(parentPart.getMaster());
		if(wtcol != null){
			Iterator<?> iterator = wtcol.iterator() ;
			while(iterator.hasNext()){
				Object obj = iterator.next() ;
				if(obj instanceof ObjectReference){
					obj = ((ObjectReference)obj).getObject() ;
				}
				WTPartAlternateLink alternateLink = (WTPartAlternateLink)obj ;
				// 全局替代
				WTPartMaster altMaster = alternateLink.getAlternates() ;
				// 全局替代部件
				WTPart altPart = PIPartHelper.service.findWTPart(altMaster, (View)parentPart.getView().getObject()) ;
				excelBean = new ImportProductStructureExcelBean() ;
				excelBean.setSerial(extractDatas.size() + 1 + "");
				excelBean.setReplaceNumber(altMaster.getNumber());
				excelBean.setPartName(altMaster.getName());
				excelBean.setReplaceType("全局替代");
				if(altPart != null){
					excelBean.setPartVersion(CommonPDMUtil.getVersion(altPart));
					// 规格描述
					ggms = PIAttributeHelper.service.getValue(altPart, "ggms") ;
					excelBean.setGgms(ggms == null ? "" : (String)ggms);
				}
				extractDatas.add(excelBean) ;
			}
		}
		
		level++ ;
		// 获取BOM信息并转化为指定JavaBean对象
		getBomDatas(parentPart, (View)parentPart.getView().getObject(), level, extractDatas);
		
		if(LOG.isDebugEnabled()){
			LOG.debug("extractDatas : " + extractDatas);
		}
		return extractDatas ;
	}
	
	/***
	 * 获取BOM信息并转化为指定JavaBean对象
	 * 
	 * @param parentPart
	 *            父件
	 * @param view
	 *            视图
	 * @param level
	 *            层数
	 * @param extractDatas
	 *            提取后数据集合
	 * @throws WTException
	 */
	public void getBomDatas(WTPart parentPart, View view, Integer level, List<ImportProductStructureExcelBean> extractDatas) throws WTException{
		if(parentPart == null || view == null || extractDatas == null){
			return ;
		}
		
		Integer levelIndex = level ;
		level++ ;
		WTList parentList = new WTArrayList() ;
		parentList.add(parentPart) ;
		QueryResult qr = ChangePartQueryUtils.getPartsFirstStructure(parentList, new LatestConfigSpec());
		// 获取
		while(qr.hasMoreElements()){
			Persistable[] persistables = (Persistable[]) qr.nextElement();
			if(persistables != null){
				WTPartUsageLink usageLink = (WTPartUsageLink)persistables[0] ;
				// 获取子件Master
				WTPartMaster childMaster = null ;
				// 获取子件对象
				WTPart childPart = null ;
				if(persistables[1] instanceof WTPart){
					childPart = (WTPart)persistables[1] ;
					childMaster = childPart.getMaster() ;
				}else{
					childMaster = (WTPartMaster)persistables[1] ;
				}
				// 自定义JavaBean对象
				ImportProductStructureExcelBean excelBean = new ImportProductStructureExcelBean() ;
				// 序号
				excelBean.setSerial(extractDatas.size() + 1 + "");
				// 层级
				excelBean.setLevel(levelIndex + "");
				Quantity quantity = usageLink.getQuantity() ;
				if(quantity != null){
					// 单位
					excelBean.setUnit(CommonPDMUtil.getUnitCN(quantity.getUnit()));
				}
				// 数量
				excelBean.setQuantity(CommonPDMUtil.getQuantityValue(usageLink));
				// 位号
				excelBean.setSiteLine(getOccurrence(usageLink));
				// 最低存货等级
				Object stockGrade = PIAttributeHelper.service.getValue(usageLink, "stockGrade") ;
				excelBean.setZdchdj(stockGrade == null ? "" : (String)stockGrade);
				// BOM备注
				Object bomNote = PIAttributeHelper.service.getValue(usageLink, "bom_note") ;
				excelBean.setRemark(bomNote == null ? "" : (String)bomNote);
				excelBean.setNumber(childMaster.getNumber());
				excelBean.setPartName(childMaster.getName());
				if(childPart != null){
					excelBean.setPartVersion(CommonPDMUtil.getVersion(childPart));
					// 规格描述
					Object ggms = PIAttributeHelper.service.getValue(childPart, "ggms") ;
					excelBean.setGgms(ggms == null ? "" : (String)ggms);
				}
				extractDatas.add(excelBean) ;
				// 处理特定替代件
				List<WTPartSubstituteLink> subArray = getSubstituteLinks(usageLink) ;
				for(WTPartSubstituteLink subLink : subArray){
					// 替代主对象
					WTPartMaster replaceMaster = subLink.getSubstitutes() ;
					// 替代部件
					WTPart replacePart = PIPartHelper.service.findWTPart(replaceMaster, view) ;
					excelBean = new ImportProductStructureExcelBean() ;
					excelBean.setSerial(extractDatas.size() + 1 + "");
					excelBean.setReplaceNumber(replaceMaster.getNumber());
					excelBean.setPartName(replaceMaster.getName());
					excelBean.setReplaceType("局部替代");
					if(replacePart != null){
						excelBean.setPartVersion(CommonPDMUtil.getVersion(replacePart));
						// 规格描述
						Object ggms = PIAttributeHelper.service.getValue(replacePart, "ggms") ;
						excelBean.setGgms(ggms == null ? "" : (String)ggms);
					}
					extractDatas.add(excelBean) ;
				}
				//处理子件全局替代
				WTCollection wtcol = WTPartHelper.service.getAlternateLinks(childMaster);
				if(wtcol != null){
					Iterator<?> iterator = wtcol.iterator() ;
					while(iterator.hasNext()){
						Object obj = iterator.next() ;
						if(obj instanceof ObjectReference){
							obj = ((ObjectReference)obj).getObject() ;
						}
						WTPartAlternateLink alternateLink = (WTPartAlternateLink)obj ;
						// 全局替代
						WTPartMaster altMaster = alternateLink.getAlternates() ;
						// 全局替代部件
						WTPart altPart = PIPartHelper.service.findWTPart(altMaster, view) ;
						excelBean = new ImportProductStructureExcelBean() ;
						excelBean.setSerial(extractDatas.size() + 1 + "");
						excelBean.setReplaceNumber(altMaster.getNumber());
						excelBean.setPartName(altMaster.getName());
						excelBean.setReplaceType("全局替代");
						if(altPart != null){
							excelBean.setPartVersion(CommonPDMUtil.getVersion(altPart));
							// 规格描述
							Object ggms = PIAttributeHelper.service.getValue(altPart, "ggms") ;
							excelBean.setGgms(ggms == null ? "" : (String)ggms);
						}
						extractDatas.add(excelBean) ;
					}
				}
				if(childPart != null){
					getBomDatas(childPart, view, level, extractDatas);
				}
			}
		}
	}
	
	/***
	 * 查询特定替代关系
	 * 
	 * @param usageLink
	 * @return
	 * @throws WTException
	 */
	public List<WTPartSubstituteLink> getSubstituteLinks(WTPartUsageLink usageLink) throws WTException {
        List<WTPartSubstituteLink> substituteLinks = new ArrayList<WTPartSubstituteLink>();
        if(usageLink == null){
        	return substituteLinks ;
        }
        
        // 查询管理的替代部件
        WTCollection collection = WTPartHelper.service.getSubstituteLinks(usageLink);
        for (Object object : collection) {
            if (object instanceof ObjectReference) {
               object = ((ObjectReference)object).getObject() ;
            }
            if(object instanceof WTPartSubstituteLink){
            	substituteLinks.add((WTPartSubstituteLink)object) ;
            }
        }

        return substituteLinks;
    }
	
	/***
	 * 获取位号信息
	 * 
	 * @param usageLink
	 * @return
	 * @throws WTException
	 */
	public String getOccurrence(WTPartUsageLink usageLink) throws WTException {
		StringBuilder returnStr = new StringBuilder() ;
		if (usageLink == null) {
			return returnStr.toString();
		}

		try {
			// 存储位号并排序
			Map<String, String> map = new TreeMap<String, String>() ;
			QueryResult usesoccurrences = OccurrenceHelper.service.getUsesOccurrences(usageLink);
			if (usesoccurrences != null) {
				while (usesoccurrences.hasMoreElements()) {
					Object obj = usesoccurrences.nextElement();
					// logger.debug("获取的位号对象类型为：" + obj.getClass().getName());
					if (obj instanceof PartUsesOccurrence) {
						String name = ((PartUsesOccurrence) obj).getName();
						// 判断名称是否为空
						if (PIStringUtils.isNotNull(name)) {
							map.put(name, name) ;
						}
					}
				}
			}
			for(Map.Entry<String, String> entryMap : map.entrySet()){
				if(returnStr.length() > 0){
					returnStr.append(ChangeConstants.USER_KEYWORD2) ;
				}
				returnStr.append(entryMap.getKey()) ;
			}
		} catch (WTException e) {
			e.printStackTrace();
			throw e ;
		}
		
		return returnStr.toString() ;
	}
}
