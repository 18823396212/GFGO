package ext.generic.integration.cis.util;

import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ptc.core.lwc.server.LWCStructEnumAttTemplate;

import ext.com.iba.IBAUtil;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.iba.definition.BooleanDefinition;
import wt.iba.definition.DefinitionLoader;
import wt.iba.definition.FloatDefinition;
import wt.iba.definition.IntegerDefinition;
import wt.iba.definition.StringDefinition;
import wt.iba.definition.TimestampDefinition;
import wt.iba.definition.URLDefinition;
import wt.iba.definition.UnitDefinition;
import wt.iba.definition.litedefinition.AbstractAttributeDefinizerView;
import wt.iba.definition.litedefinition.AttributeDefDefaultView;
import wt.iba.definition.litedefinition.AttributeDefNodeView;
import wt.iba.definition.service.IBADefinitionHelper;
import wt.method.MethodContext;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.pom.WTConnection;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionServerHelper;
import wt.units.FloatingPointWithUnits;
import wt.util.WTException;

public class CISCSMUtil {

	public static String sqlOfGetClsAttribute = null;

	/**
	 * 获取属性逻辑标识符
	 */

	public static String getAttrLogicIdByInternalName(String attr) throws WTException {
		String returnAttr = "";
		try {
			AttributeDefDefaultView attributedefdefaultview = IBADefinitionHelper.service
					.getAttributeDefDefaultViewByPath(attr);

			if (attributedefdefaultview == null) {
				AbstractAttributeDefinizerView abstractattributedefinizerview = DefinitionLoader
						.getAttributeDefinition(attr);
				if (abstractattributedefinizerview != null) {
					attributedefdefaultview = IBADefinitionHelper.service
							.getAttributeDefDefaultView((AttributeDefNodeView) abstractattributedefinizerview);
				}
			}
			if (attributedefdefaultview != null) {
				returnAttr = attributedefdefaultview.getLogicalIdentifier();
			} else {
				throw new WTException("系统中不存在属性 : " + attr);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new WTException(e);
		}
		return returnAttr;
	}

	/**
	 * 获取分类属性,属性值用字符串表示，用于写中间表
	 * 
	 */
	public static HashMap<String, String> getClassificationAttValue(WTPart part) throws WTException, RemoteException {
		HashMap<String, String> result = new HashMap<String, String>();

		String classInternalName = (String) IBAUtil.getIBAValue(part, "Classification");
		ReferenceFactory rf = new ReferenceFactory();

		ArrayList<Map<String, String>> arrinfo = getClassificationAttInfo(classInternalName);
		if (arrinfo.size() > 0) {

			for (int i = 0; i < arrinfo.size(); i++) {
				Map<String, String> atinfo = arrinfo.get(i);
				String ibaoid = atinfo.get("ibaoid");
				Persistable iba = rf.getReference(ibaoid).getObject();
				String name = null;
				String valueStr = null;
				if (iba instanceof UnitDefinition) {// 【单位】wt.iba.definition.UnitDefinition
					name = ((UnitDefinition) iba).getName();
					String logicId = getAttrLogicIdByInternalName(name);
					Object value = IBAUtil.getIBAValue(part, logicId);
					if (value == null) {
						continue;
					}

					if (value instanceof FloatingPointWithUnits) { // 带单位的浮点型
						double va = ((FloatingPointWithUnits) value).getValue(); // 实数
						String un = ((FloatingPointWithUnits) iba).getUnits(); // 单位
						valueStr = Double.toString(va) + " " + un;
					} else {
						valueStr = Double.toString((Double) value);
					}
				} else if (iba instanceof TimestampDefinition) {// 【时间】wt.iba.definition.TimestampDefinition
					name = ((TimestampDefinition) iba).getName();
					String logicId = getAttrLogicIdByInternalName(name);
					Object value = IBAUtil.getIBAValue(part, logicId);
					if (value == null) {
						continue;
					}
					Timestamp t = (Timestamp) value;
					long l = t.getTime();
					valueStr = Long.toString(l);
				} else if (iba instanceof IntegerDefinition) {// 【整数】wt.iba.definition.IntegerDefinition
					name = ((IntegerDefinition) iba).getName();
					String logicId = getAttrLogicIdByInternalName(name);
					Object value = IBAUtil.getIBAValue(part, logicId);
					if (value == null) {
						continue;
					}
					if (value instanceof Integer) {
						valueStr = Integer.toString((Integer) value);
					} else if (value instanceof Long) {
						valueStr = Long.toString((Long) value);
					}
					// arr.put(obj);//change by rchen 2016-07-06修改属性同步存放的问题
				} else if (iba instanceof BooleanDefinition) {// 【布尔类型】wt.iba.definition.BooleanDefinition
					name = ((BooleanDefinition) iba).getName();
					String logicId = getAttrLogicIdByInternalName(name);
					Object value = IBAUtil.getIBAValue(part, logicId);
					if (value == null) {
						continue;
					}
					valueStr = value.toString();
				} else if (iba instanceof FloatDefinition) {// 【实数】wt.iba.definition.FloatDefinition
					name = ((FloatDefinition) iba).getName();
					String logicId = getAttrLogicIdByInternalName(name);
					Object value = IBAUtil.getIBAValue(part, logicId);

					if (value == null) {
						continue;
					}
					valueStr = value.toString();
				} else if (iba instanceof StringDefinition) {// 【字符串】wt.iba.definition.StringDefinition
					name = ((StringDefinition) iba).getName();
					String logicId = getAttrLogicIdByInternalName(name);
					Object value = IBAUtil.getIBAValue(part, logicId);
					if (value == null) {
						continue;
					}
					valueStr = value.toString();
				} else if (iba instanceof URLDefinition) {// 【URL】>>wt.iba.definition.URLDefinition
					name = ((URLDefinition) iba).getName();
					String logicId = getAttrLogicIdByInternalName(name);
					Object value = IBAUtil.getIBAValue(part, logicId);
					if (value == null) {
						continue;
					}
					valueStr = value.toString();
				} else {
				}

				result.put(name, valueStr);
			}
			// MAO
			if (result != null && result.containsKey("rzdw") && result.containsKey("rz")) {
				String rz = result.get("rz");
				String rzdw = result.get("rzdw");
				if (!rz.endsWith(rzdw))
					rz = rz + rzdw;
				System.out.println(rz);
				result.put("rz", rz);
			} else if (result != null && result.containsKey("zzdw") && result.containsKey("zz")) {
				String zz = result.get("zz");
				String zzdw = result.get("zzdw");
				if (!zz.endsWith(zzdw))
					zz = zz + zzdw;
				System.out.println(zz);
				result.put("rz", zz);
			}
		}
		return result;
	}

	/**
	 * 获取分类的属性
	 * 
	 * @param clsInternalName
	 *            分类内部名
	 * @param partOid
	 * @return
	 * @throws WTException
	 */
	public static ArrayList<Map<String, String>> getClassificationAttInfo(String clsInternalName) throws WTException {
		ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();
		LWCStructEnumAttTemplate node = getClassificationByInternalName(clsInternalName);
		String clsOid = "";
		if (node == null) {
			return null;
		} else {
			clsOid = node.toString();
		}
		try {
			SessionServerHelper.manager.setAccessEnforced(false);

			if (sqlOfGetClsAttribute == null) {
				sqlOfGetClsAttribute = "  " + " select ibaDef.ida2a2 ibaDef_ida2a2, " + "    ibaDef.name ibaDef_name, "
						+ "    ibaDef.classnamekeya7 iba_class, " + "    ibaDef.ida3a7 iba_id, "
						+ "	ibaDisplay.Value ibaDisplay_名称, " + "	ibaDisplay.Zh_Cn ibaDisplay_显示名称, "
						+ "    typeDef.Name typeDef_name_类型名称, " + "    ibaDef.Ida3a7 ibaDef_IBA定义类型ida2a2, "
						+ "    ibaDef.Classnamekeya7 ibaDef_IBA定义类型, " + "    qOfm.baseunitsymbol qOfm_基本单位, "
						+ "    qOfm.name qOfm_单位类型, " + "    typeDef.handlerclassname handlerclassname_待定观察, "
						+ "    typeDef.columntypes columntypes_待定观察 "
						+ " from LWCIBAATTDEFINITION ibaDef,  LWCLocalizablePropertyValue ibaDisplay, LWCDatatypeDefinition typeDef, QuantityOfMeasure qOfm  "
						+ " where ibaDef.ida3a5 in (select a.ida2a2 "
						+ "                     from LWCStructEnumAttTemplate a "
						+ "                     start with ida2a2 = ? "
						+ "                                          CONNECT BY PRIOR ida3a4 = ida2a2 )and "
						+ "   ibaDef.ida3a6 = qOfm.ida2a2(+) and " + "   ibaDef.ida3b5 = typeDef.ida2a2 and "
						+ "   ibaDef.Ida2a2 = ibaDisplay.Ida3b4(+) and "
						+ "   ibaDisplay.Ida3a4=(select pdef.ida2a2 from LWCPropertyDefinition pdef where pdef.name='displayName' and pdef.classname='com.ptc.core.lwc.server.LWCAttributeDefinition') ";
			}

			WTConnection connection = null;
			PreparedStatement prepareStatement = null;
			ResultSet rs = null;

			try {
				connection = (WTConnection) MethodContext.getContext().getConnection();
				prepareStatement = connection.prepareStatement(sqlOfGetClsAttribute);
				prepareStatement.setString(1, clsOid.split(":")[1]);
				rs = prepareStatement.executeQuery();

				while (rs.next()) {
					Map map = new HashMap();
					String ibaDef_ida2a2 = rs.getString("ibaDef_ida2a2");
					String ibaDef_name = rs.getString("ibaDef_name");
					String ibaDisplay_名称 = rs.getString("ibaDisplay_名称");
					String ibaDisplay_显示名称 = rs.getString("ibaDisplay_显示名称");
					String typeDef_name_类型名称 = rs.getString("typeDef_name_类型名称");
					String ibaDef_IBA定义类型ida2a2 = rs.getString("ibaDef_IBA定义类型ida2a2");
					String ibaDef_IBA定义类型 = rs.getString("ibaDef_IBA定义类型");
					String qOfm_基本单位 = rs.getString("qOfm_基本单位");
					String qOfm_单位类型 = rs.getString("qOfm_单位类型");
					String handlerclassname_待定观察 = rs.getString("handlerclassname_待定观察");
					// String columntypes_待定观察 =
					// rs.getString("columntypes_待定观察");
					String ibaclass = rs.getString("iba_class");
					String ibaid = rs.getString("iba_id");

					map.put("ibaDef_ida2a2", ibaDef_ida2a2);
					map.put("ibaDef_name", ibaDef_name);
					map.put("ibaDisplay_名称", ibaDisplay_名称);
					map.put("ibaDisplay_显示名称", ibaDisplay_显示名称);
					map.put("typeDef_name_类型名称", typeDef_name_类型名称);
					map.put("ibaDef_IBA定义类型ida2a2", ibaDef_IBA定义类型ida2a2);
					map.put("ibaDef_IBA定义类型", ibaDef_IBA定义类型);
					map.put("qOfm_基本单位", qOfm_基本单位);
					map.put("qOfm_单位类型", qOfm_单位类型);
					map.put("handlerclassname_待定观察", handlerclassname_待定观察);
					map.put("ibaoid", "OR:" + ibaclass + ":" + ibaid);
					// map.put("columntypes_待定观察", columntypes_待定观察);
					result.add(map);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (rs != null)
					try {
						rs.close();
					} catch (Exception e2) {
					}
				if (prepareStatement != null)
					try {
						prepareStatement.close();
					} catch (Exception e2) {
					}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			SessionServerHelper.manager.setAccessEnforced(true);
		}
		return result;
	}

	/**
	 * 
	 * 
	 * @param internalName
	 * @return
	 * @throws WTException
	 */
	public static LWCStructEnumAttTemplate getClassificationByInternalName(String internalName) throws WTException {

		LWCStructEnumAttTemplate claObj = null;
		if (!RemoteMethodServer.ServerFlag) {
			String method = "getClassificationByInternalName";
			Class[] argTypes = { String.class };
			Object[] argValues = { internalName };
			try {
				return (LWCStructEnumAttTemplate) RemoteMethodServer.getDefault().invoke(method,
						CISCSMUtil.class.getName(), null, argTypes, argValues);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
			try {
				QuerySpec spec = new QuerySpec(LWCStructEnumAttTemplate.class);
				SearchCondition con = new SearchCondition(LWCStructEnumAttTemplate.class, LWCStructEnumAttTemplate.NAME,
						SearchCondition.EQUAL, internalName);
				spec.appendWhere(con, new int[] { 0 });
				QueryResult qur = PersistenceHelper.manager.find(spec);
				while (qur.hasMoreElements()) {
					Object obj = qur.nextElement();
					if (obj instanceof LWCStructEnumAttTemplate) {
						claObj = (LWCStructEnumAttTemplate) obj;
					}
				}
				return claObj;
			} finally {
				SessionServerHelper.manager.setAccessEnforced(enforce);
			}

		}
		return claObj;
	}
}
