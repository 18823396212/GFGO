package ext.generic.integration.cis.workflow;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import wt.content.ApplicationData;
import wt.content.ContentItem;
import wt.doc.WTDocument;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.inf.container.WTContainer;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.pom.UnsupportedPDSException;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.Versioned;

import com.ptc.windchill.enterprise.part.commands.PartDocServiceCommand;

import ext.com.core.CoreUtil;
import ext.generic.integration.cis.constant.CISConstant;
import ext.generic.integration.cis.rule.CISBusinessRuleXML;
import ext.generic.integration.cis.util.CISCommonUtil;
import ext.generic.integration.cis.util.CISFileUtil;
import ext.generic.integration.cis.util.OracleUtil;
import ext.generic.integration.cis.util.SQLServerUtil;
import ext.generic.reviewObject.util.ReviewObjectAndWorkFlowLink;
import ext.generic.reviewObject.util.WorkFlowReviewObjectUtil;

public class WorkflowUtil {
	private static final Logger logger = LogR.getLogger(WorkflowUtil.class.getName());

	/**
	 * 判断当前PBO对象是否属于“电子元器件库” 并且分类值包含“电子元件” 才能进行发布
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean checkLibrary(WTObject obj) {
		boolean result = false;
		try {
			if (obj instanceof WTPart) {
				WTPart part = (WTPart) obj;

				String clsname = ext.com.csm.CSMUtil.getFullClfNodeDisplayPathByWTPart(part);
				
				//modify by 20180605
				if (clsname.contains("/")) {
					String[] split = clsname.split("/");
					clsname = split[1];
				}
				logger.debug(" >>>>>>>>>> clsname :" + clsname);
				WTContainer container = part.getContainer();
				String name = container.getName();
				logger.debug(" >>>>>>>>> name :" + name);
				
				//modify by 20180605
				if (CISBusinessRuleXML.getInstance().getLibrary().contains(name)
						&& CISBusinessRuleXML.getInstance().getCls().contains(clsname)) {
					result = true;
				}
			}
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 发布单条数据至中间表。
	 * 
	 * @param obj
	 * @return
	 */
	public static String publishData(WTObject obj, Connection connection) {
		StringBuffer sb = new StringBuffer("");
		try {
			if (obj instanceof WTPart) {
				try {
					WTPart part = (WTPart) obj;
					logger.debug(" >>>>>>>>>publishData  part :" + part.getDisplayIdentifier());
					part = CoreUtil.getWTPartByNumberAndView(part.getNumber(), part.getViewName());
					String error = "";
					if (checkLibrary(part)) { // 先校验是否符合要求，才进行下载文件
						CISFileUtil.downFiles(part); // 下载文件
						// 发送数据
						error = CISCommonUtil.updateData(part, connection);// oracle
					}
					sb.append(error);
				} catch (UnsupportedPDSException e) {
					sb.append(e.getMessage());
					e.printStackTrace();
				} catch (UnknownHostException e) {
					sb.append(e.getMessage());
					e.printStackTrace();
				} catch (Exception e) {
					sb.append(e.getMessage());
					e.printStackTrace();
				}
			} else if (obj instanceof WTDocument) {
				WTDocument doc = (WTDocument) obj;
				CISFileUtil.downFiles(doc); // 下载文件
			}
		} catch (Exception e) {
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * 发布多条数据
	 * 
	 * @param obj
	 * @param self
	 * @return
	 * @throws SQLException
	 * @throws WTException
	 */
	public static String publishAllData(WTObject pbo, ObjectReference self) throws SQLException, WTException {
		StringBuffer sb = new StringBuffer("");
		WorkFlowReviewObjectUtil wfrou = new WorkFlowReviewObjectUtil();
		String name = CISBusinessRuleXML.getInstance().getDataBaseName();
		Connection connection = null;

		try {
			WTArrayList al = wfrou.getReivewObjects(self, pbo);
			logger.debug(" >>>>>>>>>>..al :" + al.size());
			if (al != null && al.size() > 0) { // 判断是否有随签对象

				if (name.equals(CISConstant.ORACLE))
					connection = OracleUtil.getConnection();
				else if (name.equals(CISConstant.SQLSERVER))
					connection = SQLServerUtil.getConnection();

				for (int i = 0; i < al.size(); i++) {
					WTObject obj = (WTObject) al.getPersistable(i);
					logger.debug(" >>>>>>>>>. obj :" + obj.getClass());
					if (obj instanceof WTPart) {
						WTPart part = (WTPart) obj;
						if (checkLibrary(part))
							publishData(part, connection);
					} else if (obj instanceof WTDocument) {
						WTDocument doc = (WTDocument) obj;
						publishData(doc, connection);
					}
				}
			} else {
				if (name.equals(CISConstant.ORACLE))
					connection = OracleUtil.getConnection();
				else if (name.equals(CISConstant.SQLSERVER))
					connection = SQLServerUtil.getConnection();

				publishData(pbo, connection); // 否则就发布当前PBO
			}
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.close();
				connection = null;
			}
		}

		return sb.toString();
	}

	/**
	 * 检查提交时是否有主内容
	 * 
	 * @param obj
	 * @param self
	 * @return
	 * @throws WTException
	 */
	public static void checkSubmit(WTObject obj, ObjectReference self) throws WTException {
		StringBuffer sb = new StringBuffer("");
		WTArrayList list = ReviewObjectAndWorkFlowLink.getReviewObjectByProcess(self);
		logger.debug(" >>>>>>> list :" + list.size());
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				Persistable per = list.getPersistable(i);
				if (per instanceof WTPart) {
					WTPart part = (WTPart) per;
					logger.debug(" >>>>part :" + part.getDisplayIdentifier());
					boolean result = checkLibrary(part);
					if (result) {
						// 通过部件获取关联的参考文档
						QueryResult qrdoc = PartDocServiceCommand.getAssociatedReferenceDocuments(part);
						if (qrdoc != null) {
							while (qrdoc.hasMoreElements()) {
								WTDocument doc = (WTDocument) qrdoc.nextElement();
								String refVersion = getDocumentVersionNumber(doc);
								String typeName = CISCommonUtil.getSoftTypeName(doc);
								if (typeName.equals("symbol") || typeName.equals("footprint")) {
									ContentItem ci = CoreUtil.getPrimaryContent(doc);
									if (ci == null) {
										sb.append(part.getDisplayIdentifier() + " 关联的文档 " + doc.getDisplayIdentifier()
												+ " 无主内容,不能完成任务!");
									} else {
										if (!refVersion.equals("A")) {// 说明是变更
											WTDocument befortDoc = getDocumentPreVersionLatestIter(doc);
											ApplicationData ad = (ApplicationData) ci;
											ContentItem befci = CoreUtil.getPrimaryContent(befortDoc);
											ApplicationData befad = (ApplicationData) befci;
											if (!befad.getFileName().equals(ad.getFileName())) {
												sb.append(" 文档对象 " + doc.getDisplayIdentifier() + " 和上一最新版本对象"
														+ befortDoc.getDisplayIdentifier() + "的主内容名称不一致!");
											}
										}
									}

								} else {
									sb.append("部件对象" + part.getDisplayIdentifier() + "关联的文档对象" + doc.getDisplayIdentifier()
											+ " 不是symbol 或 footprint 文档类型");
								}
							}
						} else {
							sb.append("部件没有关联文档类型!");
						}

					}
				} else if (per instanceof WTDocument) {
					WTDocument doc = (WTDocument) per;
					String refVersion = getDocumentVersionNumber(doc);
					String typeName = CISCommonUtil.getSoftTypeName(doc);
					if (typeName.equals("symbol") || typeName.equals("footprint")) {
						ContentItem ci = CoreUtil.getPrimaryContent(doc);
						if (ci == null) {
							sb.append(doc.getDisplayIdentifier() + " 无主内容,不能完成任务!");
						} else {
							if (!refVersion.equals("A")) {// 说明是变更
								WTDocument befortDoc = getDocumentPreVersionLatestIter(doc);
								ApplicationData ad = (ApplicationData) ci;
								ContentItem befci = CoreUtil.getPrimaryContent(befortDoc);
								ApplicationData befad = (ApplicationData) befci;
								if (!befad.getFileName().equals(ad.getFileName())) {
									sb.append(" 文档对象 " + doc.getDisplayIdentifier() + " 和上一最新版本对象" + befortDoc.getDisplayIdentifier()
											+ "的主内容名称不一致!");
								}
							}
						}

					}
				}
			}
		}

		if (sb.toString() != null && sb.toString().length() > 0) {
			throw new WTException(sb.toString());
		}
	}

	/**
	 * 根据文档, 获取其上一大版本的最新小版本
	 * 
	 * @param wtdocument
	 *            想要获取小版本号的文档
	 * @return WTDocument 返回该文档的上一版本的最新小版本, 如果对象为空, 返回空
	 * 
	 */
	public static WTDocument getDocumentPreVersionLatestIter(WTDocument wtdocument) {
		WTDocument preVersionDoc = null;
		String curVersion = null;
		try {
			curVersion = getDocumentVersionNumber(wtdocument);
			QueryResult allIterations = VersionControlHelper.service.allVersionsFrom((Versioned) wtdocument);

			// 如果当前大版本是 A，那么就获取当前A,版本的最新小版本
			if (curVersion.equals("A") == true) {
				if (allIterations != null && allIterations.hasMoreElements()) {
					preVersionDoc = (WTDocument) allIterations.nextElement();
				}
				return preVersionDoc;
			} else {
				while (allIterations != null && allIterations.hasMoreElements()) {
					preVersionDoc = (WTDocument) allIterations.nextElement();

					String prevVersion = getDocumentVersionNumber(preVersionDoc);
					if (!prevVersion.equalsIgnoreCase(curVersion)) {
						// logger.debug("\tcurVersion="+curVersion+"   preVersion="+prevVersion);
						return preVersionDoc;
					}
				}
			}
		} catch (WTException wte) {
			wte.printStackTrace();
		}
		return null;

	}

	/**
	 * 根据文档,获取其大版本号
	 * 
	 * @param wtdocument
	 *            想要获取大版本号的文档
	 * @return String 返回该文档的大版本号,如果文档为空,返回空
	 * @throws WTException
	 */
	public static String getDocumentVersionNumber(WTDocument wtdocument) throws WTException {
		String version = null;
		try {
			version = VersionControlHelper.getVersionIdentifier((Versioned) wtdocument).getValue();
			// logger.debug("       Doc Version:"+version);
		} catch (WTException wte) {
			throw new WTException(wte);
		}
		return version;
	}
}
