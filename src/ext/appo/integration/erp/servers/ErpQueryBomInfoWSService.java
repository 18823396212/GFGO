package ext.appo.integration.erp.servers;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.collections.WTCollection;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.part.WTPart;
import wt.part.WTPartAlternateLink;
import wt.part.WTPartBaselineConfigSpec;
import wt.part.WTPartHelper;
import wt.part.WTPartSubstituteLink;
import wt.part.WTPartUsageLink;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.vc.baseline.Baselineable;
import wt.vc.baseline.ManagedBaseline;

import com.infoengine.object.factory.Att;
import com.infoengine.object.factory.Element;
import com.infoengine.object.factory.Group;

import ext.generic.integration.erp.common.CommonPDMUtil;
import ext.lang.PIStringUtils;
import ext.pi.core.PIAttributeHelper;

public class ErpQueryBomInfoWSService implements Serializable, RemoteAccess{

	private static final long serialVersionUID = 1L;
	
	private static final String CLASSNAME = ErpQueryBomInfoWSService.class.getName() ;
	private static final Logger LOG = LogR.getLogger(CLASSNAME) ;
	
	/***
	 * ERP查询PLM基线BOM入口
	 * 
	 * @param baselineNumber
	 *            基线编号
	 * @return
	 */
	public Group queryBomInfo(String baselineNumber){
		if(PIStringUtils.isNull(baselineNumber)){
			return createGroup("基线号不能为空!", baselineNumber) ;
		}
		
		try {
			// 查询基线对象
			ManagedBaseline baseline = getBaseLineByNumber(baselineNumber) ;
			if(baseline == null){
				return createGroup("基线号在PLM系统不存在!", baselineNumber) ;
			}
			if(Log.isDebugEnabled()){
				Log.debug("ManagedBaseline : " + baseline.getDisplayIdentity());
			}
			
			// 获取基线主对象
			Baselineable baselineable = baseline.getTopObject() ;
			if(baselineable == null && !(baselineable instanceof WTPart)){
				return createGroup("基线主对象异常，无法查询BOM信息!", baselineNumber) ;
			}
			WTPart parentPart = (WTPart) baselineable ;
			if(Log.isDebugEnabled()){
				Log.debug("TopObject : " + parentPart.getDisplayIdentity());
			}
			
			// 根据基线查询基线BOM第一层
			Map<WTPartUsageLink, WTPart> baselineBomMap = findChildrenAndLinks(parentPart, baseline) ;
			if(baselineBomMap == null || baselineBomMap.size() == 0){
				return createGroup("基线对应的物料不存在BOM结构!" + parentPart.getDisplayIdentity(), baselineNumber) ;
			}
			
			// 生成数据格式
			JSONObject jsonObject = generateBomJSON(baselineNumber, parentPart, baselineBomMap) ;
			if(LOG.isDebugEnabled()){
				LOG.debug("JSONObject : " + jsonObject.toJSONString());
			}
			
			return createGroup(null, jsonObject.toJSONString()) ;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return createGroup(e.getLocalizedMessage(), baselineNumber) ;
		}
	}
	
	/***
	 * 根据输入的BOM信息生成指定格式的JSON对象
	 * 
	 * @param baselineNumber
	 *            基线编码
	 * @param parentPart
	 *            父件
	 * @param bomMap
	 *            子件信息集合
	 * @return
	 * @throws WTException
	 */
	public JSONObject generateBomJSON(String baselineNumber, WTPart parentPart, Map<WTPartUsageLink, WTPart> bomMap) throws WTException{
		JSONObject jsonObject = new JSONObject() ;
		if(PIStringUtils.isNull(baselineNumber) || parentPart == null){
			return jsonObject ;
		}
		
		try {
			/** 父件信息 **/
			JSONObject parentJson = new JSONObject() ;
			// 父件编码
			parentJson.put("PARENTNUMBER", parentPart.getNumber()) ;
			// 父件视图
			parentJson.put("PARENTVIEW", parentPart.getViewName()) ;
			// 父件大小版本
			parentJson.put("PARENTVERSION", CommonPDMUtil.getVersion(parentPart)) ;
			// 父件状态
			parentJson.put("PARENTSTATE", CommonPDMUtil.getLifecycle(parentPart)) ;
			// 结构信息
			parentJson.put("STRUCTDATA", generateChildJSON(bomMap)) ;
			
			jsonObject.put(baselineNumber, parentJson) ;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage()) ;
		}
		
		return jsonObject ;
	}
	
	/***
	 * 构建BOM子件JSONArray数组
	 * 
	 * @param bomMap
	 * @return
	 * @throws WTException
	 */
	public JSONArray generateChildJSON(Map<WTPartUsageLink, WTPart> bomMap) throws WTException{
		JSONArray jsonArray = new JSONArray() ;
		if(bomMap == null || bomMap.size() == 0){
			return jsonArray ;
		}
		
		try {
			for(Map.Entry<WTPartUsageLink, WTPart> entryMap : bomMap.entrySet()){
				WTPartUsageLink usageLink = entryMap.getKey() ;
				
				WTPart childPart = entryMap.getValue() ;
				/** 子件信息  **/
				JSONObject jsonObject = new JSONObject() ;
				// 子件编码
				jsonObject.put("CHILDNUMBER", childPart.getNumber()) ;
				// 子件大小版本
				jsonObject.put("CHILDVERSION", CommonPDMUtil.getVersion(childPart)) ;
				// 位号
				jsonObject.put("TAGNUMBER", CommonPDMUtil.getOccurrence(usageLink)) ;
				// BOM单位（英文）
				jsonObject.put("CHILDUNIT", CommonPDMUtil.getQuantityUnit(usageLink)) ;
				// BOM数量
				jsonObject.put("CHILDQT", CommonPDMUtil.getQuantityValue(usageLink)) ;
				// IBA:BOM备注
				Object ibaValue = PIAttributeHelper.service.getValue(usageLink, "bom_note") ;
				jsonObject.put("BOMCOMMENT", ibaValue == null ? "" : (String)ibaValue) ;
				// IBA:存货等级
				ibaValue = PIAttributeHelper.service.getValue(usageLink, "stockGrade") ;
				jsonObject.put("STOCKGRADE", ibaValue == null ? "" : (String)ibaValue) ;
				// 替代编码
				jsonObject.put("REPLACEDATA", generateReplaceJSON(childPart, usageLink)) ;
				
				jsonArray.put(jsonObject) ;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage()) ;
		}
		
		return jsonArray ;
	}
	
	/***
	 * 构建替代JSONArray数组
	 * 
	 * @param childPart
	 * @param usageLink
	 * @return
	 * @throws WTException
	 */
	public JSONArray generateReplaceJSON(WTPart childPart, WTPartUsageLink usageLink) throws WTException{
		JSONArray jsonArray = new JSONArray() ;
		if(childPart == null || usageLink == null){
			return jsonArray ;
		}
		
		try {
			// 查询全局替代
			Collection<WTPartAlternateLink> alArray = findAlternateLinks(childPart) ;
			for(WTPartAlternateLink alLink : alArray){
				jsonArray.put(alLink.getAlternates().getNumber()) ;
			}
			// 查询特定替代
			Collection<WTPartSubstituteLink> subArray = findSubLinks(usageLink) ;
			for(WTPartSubstituteLink subLink : subArray){
				jsonArray.put(subLink.getSubstitutes().getNumber()) ;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage()) ;
		}
		
		return jsonArray ;
	}
	
	/***
	 * 查询全局替代
	 * 
	 * @param part
	 * @return
	 * @throws WTException
	 */
	public Collection<WTPartAlternateLink> findAlternateLinks(WTPart part) throws WTException{
		Collection<WTPartAlternateLink> alArray = new HashSet<WTPartAlternateLink>() ;
		if(part == null){
			return alArray ;
		}
		
		try {
			WTCollection wtcol = WTPartHelper.service.getAlternateForLinks(part.getMaster()) ;
			if(wtcol != null){
				if(LOG.isDebugEnabled()){
					LOG.debug(part.getDisplayIdentity() + " > findAlternateLinks() : " + wtcol.size());
				}
				for(Object object : wtcol){
					if(object instanceof ObjectReference){
						object = ((ObjectReference)object).getObject() ; 
					}
					if(object instanceof WTPartAlternateLink){
						alArray.add((WTPartAlternateLink)object) ;
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage()) ;
		}
		
		return alArray ;
	}
	
	/***
	 * 查询特定替代料
	 * 
	 * @param usageLink
	 * @return
	 * @throws WTException
	 */
	public Collection<WTPartSubstituteLink> findSubLinks(WTPartUsageLink usageLink) throws WTException{
		Collection<WTPartSubstituteLink> subArray = new HashSet<WTPartSubstituteLink>() ;
		if(usageLink == null){
			return subArray ;
		}
		
		try {
			//获取usageLink上的所有特定替代关系
			WTCollection wtcol = WTPartHelper.service.getSubstituteLinks( usageLink ) ;
			if(wtcol != null){
				if(LOG.isDebugEnabled()){
					LOG.debug(usageLink.getDisplayIdentity() + " > findSubLinks() : " + wtcol.size());
				}
				for(Object object : wtcol){
					if(object instanceof ObjectReference){
						object = ((ObjectReference)object).getObject() ; 
					}
					if(object instanceof WTPartSubstituteLink){
						subArray.add((WTPartSubstituteLink)object) ;
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage()) ;
		}
		
		return subArray ;
	}
	
	/***
	 * 根据基线查询基线BOM第一层
	 * 
	 * @param parentPart
	 *            上层部件
	 * @param baseline
	 *            基线对象
	 * @return
	 * @throws WTException
	 */
	public Map<WTPartUsageLink, WTPart> findChildrenAndLinks(WTPart parentPart, ManagedBaseline baseline) throws WTException{
		Map<WTPartUsageLink, WTPart> returnMap = new HashMap<WTPartUsageLink, WTPart>() ;
		if(parentPart == null || baseline == null){
			return returnMap ;
		}
		
		try {
			// 创建基线搜索条件
			WTPartBaselineConfigSpec baselineConfig = WTPartBaselineConfigSpec.newWTPartBaselineConfigSpec(baseline);
			// 根据基线查询基线BOM第一层
			QueryResult qr = WTPartHelper.service.getUsesWTParts(parentPart, baselineConfig);
			while(qr.hasMoreElements()){
				Persistable[] persistables = (Persistable[]) qr.nextElement();
				// 子件对象
				Persistable childPersistable = persistables[1];
				if(childPersistable instanceof WTPart){
					returnMap.put((WTPartUsageLink) persistables[0], (WTPart)childPersistable);
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
	 * 根据基线编号获取基线对象
	 * 
	 * @param number 基线编号
	 * @return
	 * @throws WTException
	 */
	public ManagedBaseline getBaseLineByNumber( String number ) throws WTException{
		ManagedBaseline baselink = null;
		if( number == null || number.trim().isEmpty() ){
			return baselink;
		}
		
		try {
			QuerySpec qs = new QuerySpec(ManagedBaseline.class);
			qs.appendWhere(new SearchCondition(ManagedBaseline.class, ManagedBaseline.NUMBER, SearchCondition.EQUAL, number), new int[]{0});
			QueryResult qr = PersistenceHelper.manager.find(qs);
			if (qr.hasMoreElements()){
				baselink = (ManagedBaseline)qr.nextElement();
			}
		} catch (QueryException e) {
			e.printStackTrace();
			throw new WTException(e);
		} catch (WTException e) {
			e.printStackTrace();
			throw e;
		}
		
		return baselink;
	}

	/***
	 * 构建返回的数据组
	 * 
	 * @param errorMassage
	 *            错误信息
	 * @param returnData
	 *            返回数据
	 * @return
	 */
	public Group createGroup(String errorMassage, String returnData) {
		Group resultGroup = new Group("PLM_ERP_QUERYBOMINFO");
		Element element = new Element();
		if (PIStringUtils.isNull(errorMassage)) {
			element.addAtt(new Att("STATUS", "S"));
			element.addAtt(new Att("MESSAGE", errorMassage));
			element.addAtt(new Att("DATA", returnData));
		} else {
			element.addAtt(new Att("STATUS", "E"));
			element.addAtt(new Att("MESSAGE", errorMassage));
			element.addAtt(new Att("DATA", returnData));
			
		}
		resultGroup.addElement(element);
		return resultGroup;
	}
	
}
