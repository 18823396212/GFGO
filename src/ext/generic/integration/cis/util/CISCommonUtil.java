package ext.generic.integration.cis.util;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import wt.auth.SimpleAuthenticator;
import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.method.MethodContext;
import wt.method.MethodServerException;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.pom.UnsupportedPDSException;
import wt.pom.WTConnection;
import wt.session.SessionServerHelper;
import wt.type.ClientTypedUtility;
import wt.util.WTException;

import com.ptc.core.lwc.common.view.TypeDefinitionReadView;
import com.ptc.core.lwc.server.LWCStructEnumAttTemplate;
import com.ptc.windchill.csm.client.helpers.CSMTypeDefHelper;

import ext.com.iba.IBAUtil;
import ext.generic.integration.cis.constant.CISConstant;
import ext.generic.integration.cis.rule.CISBusinessRuleXML;

/**
 * 
 * Copyright (c) 2017-2030 上海湃睿信息科技有限公司 (PISX). All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of PISX and is
 * subject to the terms of a software license agreement. You shall not disclose
 * such confidential information and shall use it only in accordance with the
 * terms of the license agreement.
 * 
 */
public class CISCommonUtil implements RemoteAccess, Serializable {

	private static final long serialVersionUID = 1L;
	private static final String CLASSNAME = CISCommonUtil.class.getName();
	private static final Logger logger = LogR.getLogger(CLASSNAME);

	/**
	 * 获得对象的软类型显示名称
	 * 
	 * @param obj
	 * @param locale
	 * @return
	 * @throws WTException
	 */
	public static String getSoftTypeDisplayName(WTObject obj, Locale locale) throws WTException {
		String moduleName = "";
		if (locale == null) {
			locale = Locale.CHINA;
		}
		if (obj != null) {
			try {
				// 获取对象的软属性显示名
				moduleName = ClientTypedUtility.getLocalizedTypeName(obj, locale);

			} catch (Exception e) {
				throw new WTException(e);
			}
		}

		return moduleName;
	}

	/**
	 * 获得对象的内部名称
	 * 
	 * @param obj
	 *            对象
	 * @return
	 */
	public static String getSoftTypeName(WTObject obj) {
		String internalName = "";

		if (obj != null) {
			logger.debug("obj.display=" + obj.getDisplayIdentifier());
			try {
				// 获取完整的内部值
				internalName = ClientTypedUtility.getTypeIdentifier(obj).getTypename();
				logger.debug("internalName=" + internalName);
				if (internalName != null) {
					int startIndex = internalName.lastIndexOf(".") + 1;

					// 截取最后一个"."的后面部分
					internalName = internalName.substring(startIndex);
				}
			} catch (WTException e) {
				e.printStackTrace();
			}
		}
		logger.debug("Exit .getSoftTypeName() internalName=" + internalName);
		return internalName;
	}

	/**
	 * 获取方法上下文
	 * 
	 * @return
	 * @throws UnsupportedPDSException
	 * @throws UnknownHostException
	 */
	public static MethodContext getMethodContext() throws UnsupportedPDSException, UnknownHostException {
		MethodContext methodcontext = null;
		try {
			methodcontext = MethodContext.getContext();
		} catch (MethodServerException methodserverexception) {
			RemoteMethodServer.ServerFlag = true;
			InetAddress inetaddress = InetAddress.getLocalHost();
			String s = inetaddress.getHostName();
			if (s == null) {
				s = inetaddress.getHostAddress();
			}
			SimpleAuthenticator simpleauthenticator = new SimpleAuthenticator();
			methodcontext = new MethodContext(s, simpleauthenticator);
			methodcontext.setThread(Thread.currentThread());
		}
		return methodcontext;
	}

	/**
	 * 获取连接对象
	 * 
	 * @return
	 * @throws UnsupportedPDSException
	 * @throws UnknownHostException
	 * @throws Exception
	 */
	public static WTConnection getConnection() throws UnsupportedPDSException, UnknownHostException, Exception {
		WTConnection connection = (WTConnection) getMethodContext().getConnection();
		return connection;
	}

	/**
	 * 查询判断部件是否发布过CIS
	 * 
	 * @param part
	 * @param conn
	 * @return
	 * @throws RemoteException
	 * @throws WTException
	 * @throws SQLException
	 * @Description:
	 */
	public static boolean selectData(String number, Connection conn, String dataName, HashMap<String, String> map)
			throws RemoteException, WTException, SQLException {
		boolean result = false;
		String sql = "select * from " + dataName + " where " + map.get("number") + "=?";
		logger.debug(" >>>>>>>.selectData.>>>>sql:" + sql);
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, number);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next()) {
			result = true;
		}
		OracleUtil.release(null, pstmt, rs);
		return result;
	}

	/**
	 * 更新数据
	 * 
	 * @param part
	 * @return 错误消息
	 * @throws UnsupportedPDSException
	 * @throws UnknownHostException
	 * @throws Exception
	 */
	public static String updateData(WTPart part, Connection connection) throws UnsupportedPDSException, UnknownHostException,
			Exception {

		String dataName = CISSignExcelUtil.getSheetName();// 数据库名称
		logger.debug(" >>>>>>>>>>>tableName :" + dataName);
		HashMap<String, String> map = CISSignExcelUtil.readCISConfigInfo();

		// 检查中间表是否存在
		boolean isExist = selectData(part.getNumber(), connection, dataName, map);
		logger.debug(" >>>>>>>>>>>isExist :" + isExist);
		String error = "";
		PreparedStatement pstmt = null;
		// 获取是否是发送多表
		String isMultiTable = CISBusinessRuleXML.getInstance().getIsMultiTable();
		StringBuffer sqlbuff = new StringBuffer();
		try {
			
			//modify by 20180605
//			if (isExist) {// 说明存在，则更新
//
//				sqlbuff.append("UPDATE " + dataName + " set ");
//				ArrayList listv = new ArrayList();
//				int i = 0;
//				for (String key : map.keySet()) {
//
//					String str = "";
//					if (key.equals("versionInfo.identifier.versionId")) {
//						str = VersionControlHelper.getIterationDisplayIdentifier(part).toString();
//					} else if (key.equals("Classification")) {
//						String v = PIAttributeHelper.service.getDisplayValue(part, key, null);// 获取值
//						str = PIClassificationHelper.service.getNodeLocalizedHierarchy(v, "\\", Locale.CHINA);
//					} else {
//						str = PIAttributeHelper.service.getDisplayValue(part, key, Locale.CHINA);// 获取值
//					}
//					String value = map.get(key); // 获取字段
//					// if(!key.equals("number")){
//					if (i != 0) {
//						sqlbuff.append(",");
//					}
//					str = str == null ? "" : str;
//					listv.add(str);
//					sqlbuff.append(value + "=? ");
//
//					i++;
//					// }
//				}
//
//				sqlbuff.append("where " + map.get("number") + " = '" + part.getNumber() + "'");
//
//				logger.debug(" >>>>>>>>.update sql :" + sqlbuff.toString());
//
//				pstmt = connection.prepareStatement(sqlbuff.toString());
//				for (int j = 0; j < listv.size(); j++) {
//					int c = j + 1;
//					logger.debug(" >>>> c:" + c + " >>>>>>>> va :" + listv.get(j).toString());
//					pstmt.setString(j + 1, listv.get(j).toString());
//				}
//				pstmt.execute();
//
//			} else { // 插入
//			// insertData(part,connection);
//				sqlbuff.append("INSERT INTO " + dataName + " (");
//				ArrayList listv = new ArrayList();
//
//				int i = 0;
//				for (String key : map.keySet()) {
//
//					String str = "";
//					if (key.equals("versionInfo.identifier.versionId")) {
//						str = VersionControlHelper.getIterationDisplayIdentifier(part).toString();
//					}else if (key.equals("Classification")) {
//						String v = PIAttributeHelper.service.getDisplayValue(part, key, null);// 获取值
//						str = PIClassificationHelper.service.getNodeLocalizedHierarchy(v, "\\", Locale.CHINA);
//					} else {
//						str = PIAttributeHelper.service.getDisplayValue(part, key, Locale.CHINA);// 获取值
//					}
//					String value = map.get(key); // 获取字段
//					if (i != 0) {
//						sqlbuff.append(",");
//					}
//					str = str == null ? "" : str;
//					listv.add(str);
//					sqlbuff.append(value);
//
//					i++;
//				}
//
//				sqlbuff.append(") values(");
//
//				for (int k = 0; k < listv.size(); k++) {
//					if (k != 0) {
//						sqlbuff.append(",");
//					}
//					sqlbuff.append("?");
//				}
//				sqlbuff.append(")");
//
//				logger.debug(" >>>>>>>> insert  sql:" + sqlbuff.toString());
//				pstmt = connection.prepareStatement(sqlbuff.toString());
//				for (int j = 0; j < listv.size(); j++) {
//					pstmt.setString(j + 1, listv.get(j).toString());
//				}
//				pstmt.execute();
//
//			}
			System.out.println("connection : " + connection);
			if(connection != null){
				System.out.println("isClosed : " + connection.isClosed());
			}
			if(connection == null || connection.isClosed()){
				String name = CISBusinessRuleXML.getInstance().getDataBaseName();
				if (name.equals(CISConstant.ORACLE))
					connection = OracleUtil.getConnection();
				else if (name.equals(CISConstant.SQLSERVER))
					connection = SQLServerUtil.getConnection();
			}
			if (isMultiTable.equals("true")) {
				publishCls(part, connection);
			}
		} catch (Exception e) {
			error = e.getMessage();
			e.printStackTrace();
		} finally {
			OracleUtil.release(connection, pstmt, null);
		}
		return error;
	}

	/**
	 * 获取发布分类的表信息
	 * 
	 * @param part
	 * @param conn
	 * @return
	 * @throws SQLException
	 * @throws WTException
	 * @throws RemoteException
	 */
	public static String publishCls(WTPart part, Connection conn) throws RemoteException, WTException, SQLException {
		StringBuffer sqlbuff = new StringBuffer();
		HashMap<String, Object> info = getPublishInfo(part);
		logger.debug(" >>>> info:" + info);
		String tableName = (String) info.get("tableName");
		List datas = (List) info.get("data");
		logger.debug(" >>datas:" + datas);

		if (isPublished(part, conn)) {
			sqlbuff.append("UPDATE " + tableName + " set ");

			ArrayList listv = new ArrayList();

			for (int i = 0; i < datas.size(); i++) {
				String val = (String) datas.get(i);
				if (val.indexOf(",,,") > -1) {
					String[] vals = val.split(",,,");
					if (i != 0) {
						sqlbuff.append(",");
					}
					listv.add(vals[1]);
					sqlbuff.append(vals[0] + "=? ");
				}

			}
			sqlbuff.append("where Part_Number = '" + part.getNumber() + "'");

			executeSQL(sqlbuff.toString(), listv, conn);
		} else {
			sqlbuff.append("INSERT INTO " + tableName + " (");
			ArrayList listv = new ArrayList();
			for (int i = 0; i < datas.size(); i++) {
				String val = (String) datas.get(i);
				if (val.indexOf(",,,") > -1) {
					String[] vals = val.split(",,,");
					if (i != 0) {
						sqlbuff.append(",");
					}
					listv.add(vals[1]);
					sqlbuff.append(vals[0]);
				}

			}
			sqlbuff.append(") values(");

			for (int i = 0; i < listv.size(); i++) {
				if (i != 0) {
					sqlbuff.append(",");
				}
				sqlbuff.append("?");
			}
			sqlbuff.append(")");

			executeSQL(sqlbuff.toString(), listv, conn);
		}

		return "OK";
	}

	/**
	 * 执行数据库操作语句
	 * 
	 * @param sql
	 * @param paras
	 * @param conn
	 * @throws SQLException
	 * @Description:
	 */
	public static void executeSQL(String sql, ArrayList paras, Connection conn) throws SQLException {
		logger.debug(">>>executeSQL:" + sql);
		if (logger.isDebugEnabled()) {
			StringBuffer b = new StringBuffer();
			for (int i = 0; i < paras.size(); i++) {
				b.append(paras.get(i).toString()).append(",");
				logger.debug(">>paras:" + b.toString());
			}
		}
		
		try {
			if(conn.isClosed()){
				conn = OracleUtil.getConnection();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		PreparedStatement pstmt = conn.prepareStatement(sql);
		for (int i = 0; i < paras.size(); i++) {
			int c = i + 1;
			logger.debug(" >>>> c:" + c);
			pstmt.setString(i + 1, paras.get(i).toString());
		}
		pstmt.execute();

		if (pstmt != null) {
			pstmt.close();
			pstmt = null;
		}
	}

	/**
	 * 查询判断部件是否发布过CIS
	 * 
	 * @param part
	 * @param conn
	 * @return
	 * @throws RemoteException
	 * @throws WTException
	 * @throws SQLException
	 * @Description:
	 */
	public static boolean isPublished(WTPart part, Connection conn) throws RemoteException, WTException, SQLException {
		boolean result = false;
		String tableName = getPublishTable(part);
		if (tableName == null || tableName.trim().length() == 0) {
			return result;
		}
		String number = part.getNumber();
		String sql = "select * from " + tableName + " where Part_Number=?";

		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, number);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next()) {
			result = true;
		}

		OracleUtil.release(conn, pstmt, rs);

		return result;
	}

	/**
	 * 获取发布的表名
	 * 
	 * @param part
	 * @return
	 * @throws RemoteException
	 * @throws WTException
	 * @Description:
	 */
	public static String getPublishTable(WTPart part) throws RemoteException, WTException {
		String classInternalName = (String) IBAUtil.getIBAValue(part, "Classification");
		LWCStructEnumAttTemplate template = CISCSMUtil.getClassificationByInternalName(classInternalName);
		String path = getClassificationPath(template);
		String[] ps = path.split("/");
		ArrayList<HashMap<String, String>> conf = null;
		String tableName = null;
		for (int i = ps.length - 1; i > -1 && conf == null; i--) {
			String p = ps[i];
			conf = CISConfigExcelUtil.getAttConfig(p);
			if (conf != null) {
				tableName = CISConfigExcelUtil.getTableName(p);
				break;
			}
		}
		return tableName;
	}

	/**
	 * 获取发布的信息
	 * 
	 * @param part
	 * @return
	 */
	private static HashMap<String, Object> getPublishInfo(WTPart part) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		try {

			String classInternalName = (String) IBAUtil.getIBAValue(part, CISBusinessRuleXML.getInstance().getLogicId());
			LWCStructEnumAttTemplate template = CISCSMUtil.getClassificationByInternalName(classInternalName);
			String path = getClassificationPath(template);
			logger.debug(" >>>>path :" + path);
			String[] ps = path.split("/");
			ArrayList<HashMap<String, String>> conf = null;
			String tableName = null;
			for (int i = ps.length - 1; i > -1 && conf == null; i--) {
				String p = ps[i];
				logger.debug(" >>>>p :" + p +"|");
				conf = CISConfigExcelUtil.getAttConfig(p);
				logger.debug(" >>>>conf :" + conf);
				if (conf != null) {
					tableName = CISConfigExcelUtil.getTableName(p);
					break;
				}
			}
			if (conf == null) {
				throw new WTException("ERR:分类没有对应的CIS配置");
			}
			logger.debug(" >>>>tableName :" + tableName);

			HashMap<String, String> classificationValues = CISCSMUtil.getClassificationAttValue(part);
			String[][] datas = new String[conf.size() + 1][2]; // 属性加标识位
			List list = new ArrayList();

			for (int i = 0; i < conf.size(); i++) {
				HashMap<String, String> c = conf.get(i);
				String pdmName = c.get("pdmName");
				String cisName = c.get("cisName");
				String pdmType = c.get("pdmType");
				String value = null;

				logger.debug(" >>>> pdmName:" + pdmName);
				logger.debug(" >>>> cisName:" + cisName);
				logger.debug(" >>>> pdmType:" + pdmType);
				if ("MBA".equals(pdmType)) {
					if ("name".equals(pdmName)) {
						value = part.getName();
					} else if ("number".equals(pdmName)) {
						value = part.getNumber();
					}
				} else if ("IBA".equals(pdmType)) {
					value = (String) IBAUtil.getIBAValue(part, pdmName);
					if (CISBusinessRuleXML.getInstance().getLogicId().equals(pdmName) && value != null && value.trim().length() > 0) { // 如果是分类，取分类显示名
						LWCStructEnumAttTemplate node = CISCSMUtil.getClassificationByInternalName(value);
						TypeDefinitionReadView rv = CSMTypeDefHelper.getRV(node);
						value = rv.getDisplayName();
					}
				} else if ("CLASSBA".equals(pdmType)) {
					value = classificationValues.get(pdmName);
				}
				if (value == null || value.trim().length() == 0) {
					value = "/";
				}
				logger.debug(" cisName :" + cisName + " >>>Value :" + value);
				list.add(cisName + ",,," + value);
			}

			result.put("tableName", tableName);
			result.put("data", list);
		} catch (RemoteException | WTException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 获取分类路径
	 * 
	 * @param node
	 * @return
	 * @throws WTException
	 * @Description:
	 */
	public static String getClassificationPath(LWCStructEnumAttTemplate node) throws WTException {

		if (node == null) {
			return "";
		}
		String temp = "";
		if (!RemoteMethodServer.ServerFlag) {
			String method = "getClassificationPath";
			Class[] argTypes = { LWCStructEnumAttTemplate.class };
			Object[] argValues = { node };
			try {
				return (String) RemoteMethodServer.getDefault().invoke(method, CISCommonUtil.class.getName(), null, argTypes,
						argValues);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
			try {
				TypeDefinitionReadView nodeView = CSMTypeDefHelper.getRV(node);
				temp = nodeView.getName();
				while (node.getParent() != null) {
					node = (LWCStructEnumAttTemplate) node.getParent();
					TypeDefinitionReadView rv = CSMTypeDefHelper.getRV(node);
					temp = rv.getName() + "/" + temp;
				}
				logger.debug("temp--------->" + temp);
				return temp;
			} catch (Exception e) {
				e.printStackTrace();

			} finally {
				SessionServerHelper.manager.setAccessEnforced(enforce);
			}
		}
		return temp;
	}

}