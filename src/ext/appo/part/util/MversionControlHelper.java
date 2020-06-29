package ext.appo.part.util;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import ext.appo.change.report.ChangeHistoryReport;
import ext.appo.change.util.ModifyUtils;
import ext.appo.ecn.common.util.ChangeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ptc.netmarkets.model.NmSimpleOid;

import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.part.beans.EffectiveBaselineBean;
import ext.generic.integration.erp.util.BOMStructureUtil;
import wt.change2.*;
import wt.enterprise.Master;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMDocument;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectReference;
import wt.fc.ObjectSetVector;
import wt.fc.ObjectToObjectLink;
import wt.fc.ObjectVector;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.log4j.LogR;
import wt.occurrence.Occurrence;
import wt.occurrence.OccurrenceHelper;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.pom.PersistenceException;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.vc.OneOffVersioned;
import wt.vc.VersionControlHelper;
import wt.vc.config.ConfigException;
import wt.vc.config.LatestConfigSpec;
import wt.vc.wip.WorkInProgressHelper;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfProcess;

public class MversionControlHelper {

	private static final Logger logger = LogR.getLogger(MversionControlHelper.class.getName());

	public final static String effState = "RELEASED";// 已发布状态
	public static final String ARCHIVED = "ARCHIVED"; // 已归档

	public static String getOidByObject(Persistable p) {
		String oid = "";
		if (p instanceof WfProcess) {
			oid = "OR:wt.workflow.engine.WfProcess:" + p.getPersistInfo().getObjectIdentifier().getId();
			return oid;
		}
		if (p != null) {
			oid = "OR:" + p.toString();
		}
		return oid;
	}

	public static String getVidByObject(Persistable p) {
		String oid = "";
		if (p != null) {
			RevisionControlled rc = (RevisionControlled) p;
			oid = "VR:" + rc.getPersistInfo().getObjectIdentifier().getClassname() + ":" + rc.getBranchIdentifier();
		}
		return oid;
	}

	public static String getCurrentDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		return sdf.format(new Date());
	}

	public static WTPartUsageLink getUsageLink(WTPart theWTPart, WTPartMaster theWTPartMaster) throws WTException {
		QuerySpec qs = new QuerySpec(WTPartUsageLink.class);
		int[] fromIndicies = { 0, wt.query.FromClause.NULL_INDEX };
		qs.appendWhere(new SearchCondition(WTPartUsageLink.class,
				ObjectToObjectLink.ROLE_AOBJECT_REF + "." + ObjectReference.KEY + "." + ObjectIdentifier.ID,
				SearchCondition.EQUAL, PersistenceHelper.getObjectIdentifier(theWTPart).getId()), fromIndicies);
		qs.appendAnd();
		qs.appendWhere(
				new SearchCondition(WTPartUsageLink.class,
						ObjectToObjectLink.ROLE_BOBJECT_REF + "." + ObjectReference.KEY + "." + ObjectIdentifier.ID,
						SearchCondition.EQUAL, PersistenceHelper.getObjectIdentifier(theWTPartMaster).getId()),
				fromIndicies);

		QueryResult qr = PersistenceHelper.manager.find((wt.pds.StatementSpec) qs);
		while (qr.hasMoreElements()) {
			WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
			return link;
		}
		return null;
	}

	public static WTPart getLatestEffective(WTPart part) throws WTException {
		return getLatestEffective(part, part.getViewName());
	}

	public static WTPart getLatestEffective(WTPart part, String viewName) throws WTException {
		return getLatestEffective(part.getMaster(), viewName);
	}

	public static WTPart getLatestEffective(WTPartMaster wm, String viewName) throws WTException {
		try {
			QueryResult qr = VersionControlHelper.service.allIterationsOf(wm);
			WTPart release = null;
			while (qr.hasMoreElements()) {
				WTPart pp = (WTPart) qr.nextElement();
				String state = pp.getState().toString();
				// effState.equals(state) &&
				// pp.getViewName().equals(viewName)
				if (effState.equals(state) || ARCHIVED.equals(state)) {
					release = pp;
					break;
				}
			}
			return release;
		} catch (PersistenceException e) {
			throw new WTException(e);
		}
	}

	public static void buildEffectiveChild(WfProcess process, WTPart top, String upUID,
			List<EffectiveBaselineBean> beans) throws WTException {
		try {

			String pid = getOidByObject(process);
			List<WTPartUsageLink> links = BOMStructureUtil.getFirstLevelUsageLink(top);
			if (!links.isEmpty()) {
				System.out.println("links===========================================" + links);
				for (WTPartUsageLink link : links) {
					WTPartMaster wm = link.getUses();
					System.out.println("wm==================================" + wm);
					WTPart eff = getLatestEffective(wm, top.getViewName());
					System.out.println("eff==================================" + eff);
					if (eff != null) {
						//add by lzy at 20200603 start
						//如果部件在新ECN流程中，取前一个版本物料
						Boolean flag = isRunningNewEcnWorkflowAndAfter(eff);
						String mVersion = eff.getVersionIdentifier().getValue();//大版本
						if (flag) {
							// 存在，取前一个版本
							String englishLetter = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
							if (englishLetter.contains(mVersion)) {
								int index = englishLetter.indexOf(mVersion);
								if (index > 0 && index < englishLetter.length()) {
									mVersion = englishLetter.substring(index - 1, index);
								}
							}
							eff = ChangeHistoryReport.getLatestPart(eff.getNumber(), eff.getViewName(), mVersion);
						}
						//add by lzy at 20200603 end
						String lid = getOidByObject(link);
						EffectiveBaselineBean eblb = new EffectiveBaselineBean(pid, upUID, top, eff, lid);
						beans.add(eblb);
						// List<WTPartSubstituteLink> subLinkList =
						// EffecitveBaselineUtil.getSubstituteLinks(link);
						// if(!subLinkList.isEmpty()) {
						// for(WTPartSubstituteLink subLink : subLinkList) {
						// EffectiveBaselineBean eblb2 = new
						// EffectiveBaselineBean(pid,eblb.getUpUID(), top, eff,
						// lid,subLink);
						// beans.add(eblb2);
						// }
						// }
						// List<WTPartAlternateLink> altLinkList =
						// EffecitveBaselineUtil.getAlternateLinks(eff);
						// if(!altLinkList.isEmpty()) {
						// for(WTPartAlternateLink altLink : altLinkList) {
						// EffectiveBaselineBean eblb2 = new
						// EffectiveBaselineBean(pid,eblb.getUpUID(), top, eff,
						// lid,altLink);
						// beans.add(eblb2);
						// }
						// }
						System.out.println("eblb==============================" + eblb);
						buildEffectiveChild(process, eff, eblb.getBeanUID(), beans);
					} else {
						logger.error("编码为：" + top.getNumber() + "的物料，其下层物料：" + wm.getNumber() + "没有已发布版本!");
					}
				}
			}
		} catch (ConfigException e) {
			throw new WTException(e);
		}
	}

	// 获取所有的bean
	public static List<EffectiveBaselineBean> buildAllEffectiveParts(WfProcess process, Set<WTPart> parts)
			throws WTException {
		List<EffectiveBaselineBean> lists = new ArrayList<>();
		System.out.println("parts=====================" + parts);
		for (WTPart top : parts) {
			String pid = getOidByObject(process);
			System.out.println("pid===========================" + pid);
			EffectiveBaselineBean eblb = new EffectiveBaselineBean(pid, top);

			List<EffectiveBaselineBean> beans = new ArrayList<>();
			buildEffectiveChild(process, top, eblb.getBeanUID(), beans);
			System.out.println("beans=======================" + beans);
			if (beans.size() > 0) {
				// 如果能找到bom结构
				beans.add(eblb);
			} else {
				logger.error("没有找到bom结构:" + top.getNumber());
			}

			// List<WTPartAlternateLink> altLinkList =
			// EffecitveBaselineUtil.getAlternateLinks(top);
			// if(!altLinkList.isEmpty()) {
			// for(WTPartAlternateLink altLink : altLinkList) {
			// EffectiveBaselineBean eblb2 = new EffectiveBaselineBean(pid,
			// top,altLink,eblb.getBeanUID());
			// beans.add(eblb2);
			// }
			// }

			lists.addAll(beans);
		}
		return lists;
	}

	public static List getEffBaselineNode(List<EffectiveBaselineBean> beans, NmSimpleOid nso) throws WTException {

		List list = new ArrayList();
		String key = nso.getInternalName();
		System.out.println("key========================================" + key);
		String[] arrs = key.split("___");
		System.out.println("arrs=================================" + arrs);
		String buid = arrs[1];
		for (EffectiveBaselineBean bean : beans) {
			String upid = bean.getUpUID();
			if (upid != null) {
				if (upid.equals(buid)) {
					NmSimpleOid simpleOid = new NmSimpleOid();
					simpleOid.setInternalName(bean.getoid());
					simpleOid.setRef(simpleOid.getInternalName());
					simpleOid.setDisplayIdentifier(simpleOid.getInternalName());
					list.add(simpleOid);
				}
			}
		}
		System.out.println("list====================================" + list);
		if (list.size() > 0)
			return list;
		else
			return null;
	}

	// 获取变更后对象
	public static Map<String, WTPart> getAfterDataWithPart(WTChangeOrder2 eco) throws WTException {
		Map<String, WTPart> allMp = new HashMap<String, WTPart>();
		try {
			if (eco == null)
				return allMp;
			QueryResult qs = ChangeHelper2.service.getChangeActivities(eco);
			while (qs.hasMoreElements()) {
				WTChangeActivity2 activity = (WTChangeActivity2) qs.nextElement();
				QueryResult qsafter = ChangeHelper2.service.getChangeablesAfter(activity);
				while (qsafter.hasMoreElements()) {
					Object obj = qsafter.nextElement();
					if (obj instanceof WTPart) {
						WTPart part = (WTPart) obj;
						allMp.put(part.getNumber(), part);
					}
				}
			}

		} catch (ChangeException2 e) {
			e.printStackTrace();
			throw new WTException(e);
		}
		return allMp;
	}

	// 获取上层部件
	public static Set<WTPart> getParentParts(WTPart part) throws WTException {
		ObjectSetVector vector = new ObjectSetVector();
		QueryResult usedByWTParts = WTPartHelper.service.getUsedByWTParts(part.getMaster());
		while (usedByWTParts.hasMoreElements()) {
			WTPart parentPart = (WTPart) usedByWTParts.nextElement();
			vector.addElement(parentPart);
		}
		Set<WTPart> parents = new HashSet<>();
		if (!vector.isEmpty()) {
			QueryResult qr = new QueryResult();
			qr.append(vector);
			qr = (new LatestConfigSpec()).process(qr);
			while (qr.hasMoreElements()) {
				parents.add((WTPart) qr.nextElement());
			}
		}
		return parents;
	}

	public static String getLinkSum(WTPartUsageLink link) {
		if (link != null) {
			String str = new BigDecimal(link.getQuantity().getAmount() + "").toString();
			if (str.indexOf(".") > 0) {
				str = str.replaceAll("0+$", "").replaceAll("[.]$", "");
			}
			return str;
		} else {
			return "";
		}
	}

	public static String getPartReferenceDesignators(WTPartUsageLink partUsageLink) throws WTException {
		String result = "";
		QueryResult qr = OccurrenceHelper.service.getUsesOccurrences(partUsageLink);
		int nOccurences = qr.size();
		ArrayList refDesignatorList = new ArrayList(nOccurences);

		while (qr.hasMoreElements()) {
			Occurrence occurrence = (Occurrence) qr.nextElement();
			String occurrenceName = occurrence.getName();
			if (occurrenceName != null) {
				refDesignatorList.add(occurrenceName);
			}
		}

		Collections.sort(refDesignatorList);
		if (!refDesignatorList.isEmpty()) {
			result = StringUtils.join(refDesignatorList, ",");
		}

		return result;
	}

	public static Map<String, String> buildAffectedValue(WTPart parent, WTPart child) throws WTException {
		WTPartUsageLink link = getUsageLink(parent, child.getMaster());
		Map<String, String> map = new HashMap<>();
		if (link == null) {
			return map;
		}
		map.put("parentNumber", parent.getNumber());
		map.put("parentName", parent.getName());
		map.put("parentVersion",
				parent.getVersionIdentifier().getValue() + "." + parent.getIterationIdentifier().getValue());
		map.put("parentState", parent.getLifeCycleState().getDisplay(SessionHelper.getLocale()).toString());
		map.put("childNumber", child.getNumber());
		map.put("childName", child.getName());
		map.put("childVersion",
				child.getVersionIdentifier().getValue() + "." + child.getIterationIdentifier().getValue());
		map.put("childState", child.getLifeCycleState().getDisplay(SessionHelper.getLocale()).toString());
		map.put("weihao", getPartReferenceDesignators(link));// 位号
		map.put("danwei", link.getQuantity().getUnit().getDisplay(SessionHelper.getLocale()));// 单位
		map.put("shuliang", getLinkSum(link));// 数量
		map.put("zdchdj", "");// 最低存货等级
		map.put("bombzxx", "");// BOM备注信息
		map.put("oid", getOidByObject(parent) + "___" + getOidByObject(child) + "___" + getOidByObject(link));
		return map;
	}

	public static List<Map<String, String>> getAffectedPartValue(WTChangeOrder2 ecn) throws WTException {
		Map<String, WTPart> map = getBeforeDataWithPart(ecn);
		List<Map<String, String>> list = new ArrayList<>();
		for (String num : map.keySet()) {
			WTPart part = map.get(num);
			String state = part.getLifeCycleState().toString();
			// Set<WTPart> parents = getParentParts(part);

			// 归档状态==============
			if (ChangeConstants.ARCHIVED.equals(state)) {

				Set<WTPartUsageLink> parentLinks = getSuperstratumParent(part);
				System.out.println("++++++++parentLinks++++++" + parentLinks);
				for (WTPartUsageLink parentLink : parentLinks) {
					WTPartMaster partMaster = parentLink.getUses();
					WTPart parentPart = (WTPart) getLatestVersionByMaster(partMaster);

					if (map.keySet().contains(parentPart.getNumber())) {
						continue;
					}

					System.out.println("+++++++parentState+++++" + parentPart.getLifeCycleState().toString());
					// 过滤不是归档和发布状态的
					if (!parentPart.getLifeCycleState().toString().equals(ChangeConstants.ARCHIVED)) {
						continue;
					}

					Map<String, String> kvs = buildAffectedValueByChild(part, parentPart, parentLink);
					list.add(kvs);
				}
			}

			// 发布状态=============
			if (ChangeConstants.RELEASED.equals(state)) {

				// 加入所有父件件==========================
				Set<WTPartUsageLink> parentLinks = getSuperstratumParent(part);

				System.out.println("++++++++parentLinks++++++" + parentLinks);
				for (WTPartUsageLink parentLink : parentLinks) {
					WTPartMaster partMaster = parentLink.getUses();

					WTPart parentPart = (WTPart) getLatestVersionByMaster(partMaster);

					if (map.keySet().contains(parentPart.getNumber())) {
						continue;
					}

					// 过滤不是归档和发布状态的
					if (!parentPart.getLifeCycleState().toString().equals(ChangeConstants.ARCHIVED)
							|| !parentPart.getLifeCycleState().toString().equals(ChangeConstants.RELEASED)) {
						continue;
					}

					Map<String, String> kvs = buildAffectedValueByChild(part, parentPart, parentLink);
					list.add(kvs);
				}

				// 加入所有子件===================================
				Map<WTPart, Set<WTPartUsageLink>> testmap = new HashMap<WTPart, Set<WTPartUsageLink>>();

				getZPartAndLink(part, testmap);

				Set<WTPartUsageLink> setLink = new HashSet<WTPartUsageLink>();
				for (Set<WTPartUsageLink> set : testmap.values()) {
					for (WTPartUsageLink link : set) {
						setLink.add(link);
					}
				}
				for (WTPartUsageLink childLink : setLink) {
					WTPartMaster partMaster = childLink.getUses();
					WTPart childPart = (WTPart) getLatestVersionByMaster(partMaster);

					if (map.keySet().contains(childPart.getNumber())) {
						continue;
					}
					// 过滤不是归档状态的
					if (!childPart.getLifeCycleState().toString().equals(ChangeConstants.ARCHIVED)) {
						continue;
					}

					Map<String, String> kvs = buildAffectedValueByChild(part, childPart, childLink);
					list.add(kvs);
				}
			}

		}

		return list;
	}

	public static Map<String, String> buildAffectedValueByChild(WTPart parent, WTPart child, WTPartUsageLink link)
			throws WTException {
		Map<String, String> map = new HashMap<>();
		if (link == null) {
			return map;
		}
		map.put("parentNumber", child.getNumber());
		map.put("parentName", child.getName());
		map.put("parentVersion", child.getVersionIdentifier().getValue() + "."
				+ child.getIterationIdentifier().getValue() + "(" + child.getViewName() + ")");
		map.put("parentState", child.getLifeCycleState().getDisplay(SessionHelper.getLocale()).toString());
		map.put("childNumber", parent.getNumber());
		map.put("childName", parent.getName());
		map.put("childVersion", parent.getVersionIdentifier().getValue() + "."
				+ parent.getIterationIdentifier().getValue() + "(" + parent.getViewName() + ")");
		map.put("childState", parent.getLifeCycleState().getDisplay(SessionHelper.getLocale()).toString());
		map.put("weihao", getPartReferenceDesignators(link));// 位号
		map.put("danwei", link.getQuantity().getUnit().getDisplay(SessionHelper.getLocale()));// 单位
		map.put("shuliang", getLinkSum(link));// 数量
		map.put("zdchdj", "");// 最低存货等级
		map.put("bombzxx", "");// BOM备注信息
		map.put("oid", getOidByObject(parent) + "___" + getOidByObject(child) + "___" + getOidByObject(link));
		return map;
	}

	/**
	 * 通过父项获取子件：所有
	 * 
	 * @param fpart
	 * @return
	 * @throws WTException
	 */
	public static void getZPartAndLink(WTPart fpart, Map<WTPart, Set<WTPartUsageLink>> testmap) throws WTException {
		Set<WTPartUsageLink> linklist = new HashSet<>();
		fpart = getLastObjectAndRelease(fpart.getMaster());

		if (fpart != null) {

			QueryResult qr = WTPartHelper.service.getUsesWTPartMasters(fpart);
			while (qr.hasMoreElements()) {
				WTPartUsageLink links = (WTPartUsageLink) qr.nextElement();
				// Object domain = MBAUtil.getValue(links, "Domain");
				// String result = "";
				// String field = domain == null ? "" : domain.toString();
				//
				// if(BOMConst.total_type.contains(option) ||
				// BOMConst.replace_type.contains(option)){
				// int leng=option.indexOf(BOMConst.whole);
				// result=option.substring(0,leng);
				// }
				// if(result.equals(field)){
				WTPartMaster masterChild = links.getUses();
				// WTPart p =(WTPart)getLatestVersionByMaster(masterChild);
				linklist.add(links);
				// }
			}

		}
		testmap.put(fpart, linklist);// 1:2,3

		for (WTPartUsageLink link : linklist) {
			WTPartMaster masterChild = link.getUses();
			WTPart p = (WTPart) getLatestVersionByMaster(masterChild);
			getZPartAndLink(p, testmap);
		}
	}

	/**
	 * 获取最新大版本的最新小版本
	 *
	 * @param master
	 * @return
	 */
	public static Persistable getLatestVersionByMaster(Master master) {
		try {
			if (master != null) {
				QueryResult qrVersions = VersionControlHelper.service.allVersionsOf(master);
				while (qrVersions.hasMoreElements()) {
					Persistable p = (Persistable) qrVersions.nextElement();
					if (!VersionControlHelper.isAOneOff((OneOffVersioned) p)) {
						return p;
					}
				}
			}
		} catch (QueryException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取最新的版本
	 * 
	 * @throws WTException
	 */
	public static WTPart getLastObjectAndRelease(WTPartMaster partMaster) throws WTException {
		QueryResult qr = allVersionsOf((Master) partMaster);
		while (qr.hasMoreElements()) {
			Persistable p = (Persistable) qr.nextElement();
			if (p instanceof WTPart) {
				WTPart relasepart = (WTPart) p;
				if (!VersionControlHelper.isAOneOff((OneOffVersioned) p)) {
					return relasepart;
				}
			}
		}
		return null;
	}

	public static QueryResult allVersionsOf(Master master) throws WTException {
		QueryResult qr = new QueryResult();
		try {
			if (master != null) {
				QueryResult qrVersions = VersionControlHelper.service.allVersionsOf(master);
				List list = new ArrayList();
				while (qrVersions.hasMoreElements()) {
					Persistable p = (Persistable) qrVersions.nextElement();
					if (p instanceof EPMDocument) {
						EPMDocument epm = (EPMDocument) p;
						if (WorkInProgressHelper.getState(epm).toString().equals("wrk-p"))
							logger.debug((new StringBuilder()).append("getLatestVersionByMaster123:").append(epm)
									.toString());
						else
							list.add(p);
					} else {
						if (!VersionControlHelper.isAOneOff((OneOffVersioned) p)) {
							list.add(p);
						}
					}
				}
				if (list.size() > 0) {
					ObjectVector ovi = new ObjectVector();
					Persistable p;
					for (Iterator i$ = list.iterator(); i$.hasNext(); ovi.addElement(p))
						p = (Persistable) i$.next();

					qr.appendObjectVector(ovi);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new WTException(e);
		}
		return qr;
	}

	/**
	 * 获取Part所有上层父Link
	 *
	 * @param part
	 * @return
	 * @throws WTException
	 */
	public static Set<WTPartUsageLink> getSuperstratumParent(WTPart part) throws WTException {
		Set<WTPartUsageLink> set = new HashSet<>();
		QueryResult queryresult = PersistenceHelper.manager.navigate(part.getMaster(), WTPartUsageLink.USED_BY_ROLE,
				WTPartUsageLink.class, false);
		System.out.println("++++++++queryresult+++++++" + queryresult);
		while (queryresult.hasMoreElements()) {
			WTPartUsageLink usageLink = (WTPartUsageLink) queryresult.nextElement();
			System.out.println("+++++++++usageLink+++++++++" + usageLink);
			// WTPartMaster usedByMaster = usageLink.getUsedBy().getMaster();
			// map.put(usageLink);
			set.add(usageLink);
		}
		return set;
	}

	// 获取变更前对象
	public static Map<String, WTPart> getBeforeDataWithPart(WTChangeOrder2 eco) throws WTException {
		Map<String, WTPart> allMp = new HashMap<String, WTPart>();
		try {
			if (eco == null)
				return allMp;
			QueryResult qs = ChangeHelper2.service.getChangeActivities(eco);
			while (qs.hasMoreElements()) {
				WTChangeActivity2 activity = (WTChangeActivity2) qs.nextElement();
				QueryResult qsafter = ChangeHelper2.service.getChangeablesBefore(activity);// 变更前对象
				while (qsafter.hasMoreElements()) {
					Object obj = qsafter.nextElement();
					if (obj instanceof WTPart) {
						WTPart part = (WTPart) obj;
						allMp.put(part.getNumber(), part);
					}
				}
			}
		} catch (ChangeException2 e) {
			e.printStackTrace();
			throw new WTException(e);
		}
		return allMp;
	}

	/**
	 * add by lzy
	 * 判断pbo是否在新ECN流程APPO_ECNWF产生对象中且流程未结束
	 *
	 * @param pbo
	 * @return
	 * @throws WTException
	 */
	private static Boolean isRunningNewEcnWorkflowAndAfter(WTPart pbo) throws WTException {
		if (pbo == null) return false;
		String number = pbo.getNumber();
		String view = pbo.getViewName();
		String mVersion = pbo.getVersionIdentifier().getValue();//物料大版本
		// 传前一个版本，如果为A版不发
		String EnglishLetter = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String strsub = mVersion;
		if (EnglishLetter.contains(mVersion)) {
			int index = EnglishLetter.indexOf(mVersion);
			if (index > 0) {
				strsub = EnglishLetter.substring(index - 1, index);
			}
		}
		WTPart wtPart = ChangeHistoryReport.getLatestPart( number , view , strsub);//获取取前一个版本
		WTChangeOrder2 changeOrder2 = null;
		//获取对象所有关联的ECA对象
		QueryResult result = ChangeHelper2.service.getAffectingChangeActivities(wtPart);
		while (result.hasMoreElements()) {
			WTChangeActivity2 changeActivity2 = (WTChangeActivity2) result.nextElement();
			//获取产生对象
			Collection<Changeable2> collection = ModifyUtils.getChangeablesAfter(changeActivity2);
			for (Changeable2 changeable2 : collection) {
				if (changeable2 instanceof WTPart) {
					WTPart part = (WTPart) changeable2;
					String partNumber = part.getNumber();
					String partVersion = part.getVersionIdentifier().getValue();//物料大版本
					String partView = part.getViewName();
					if (number.equals(partNumber) && view.equals(partView) && mVersion.equals(partVersion)) {
						changeOrder2 = ChangeUtils.getEcnByEca(changeActivity2);
					}
				}
			}
		}
		if (changeOrder2!=null){
			//ECN进程
			QueryResult qr = WfEngineHelper.service.getAssociatedProcesses(changeOrder2, null, null);
			while (qr.hasMoreElements()) {
				WfProcess process = (WfProcess) qr.nextElement();
				String templateName = process.getTemplate().getName();
				String state = String.valueOf(changeOrder2.getLifeCycleState());
				//不是已取消、已解决的新ECN流程APPO_ECNWF
				if (templateName.equals("APPO_ECNWF")) {
					if (!state.equals("CANCELLED") && !state.equals("RESOLVED")) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
