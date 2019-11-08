package ext.appo.part.util;

import com.lowagie.text.DocumentException;
import com.ptc.windchill.enterprise.part.commands.AlternatesSubstitutesCommand;
import ext.appo.common.util.Excel2007Handler;
import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.part.beans.ChangPartStateECNBean;
import ext.appo.part.beans.EffectiveBaselineBean;
import ext.appo.util.PartUtil;
import ext.com.workflow.WorkflowUtil;
import ext.generic.reviewObject.model.ProcessReviewObjectLink;
import ext.generic.reviewObject.model.ProcessReviewObjectLinkHelper;
import org.apache.commons.lang.StringUtils;
import wt.change2.ChangeHelper2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.enterprise.Master;
import wt.enterprise.RevisionControlled;
import wt.fc.*;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.part.*;
import wt.pds.StatementSpec;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.vc.OneOffVersioned;
import wt.vc.VersionControlHelper;
import wt.workflow.engine.ProcessData;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfVariable;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.ParseException;
import java.util.*;

public class EffecitveBaselineUtil {

    private static String WT_CODEBASE = "";

    static {
        WTProperties wtproperties;
        try {
            wtproperties = WTProperties.getLocalProperties();
            WT_CODEBASE = wtproperties.getProperty("wt.codebase.location");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加有效基线数据----发布流程--M视图归档流程
     *
     * @param pbo
     * @param self
     * @throws WTException
     * @throws ParseException
     * @throws DocumentException
     * @throws IOException
     */
    public static void insertEffecitveBaseline(WTObject pbo, ObjectReference self) throws WTException, IOException, DocumentException, ParseException {
        WfProcess process = (WfProcess) self.getObject();
        Set<WTPart> set = new HashSet<WTPart>();
        WTArrayList collect = collect(self);
        for (int i = 0; i < collect.size(); i++) {
            Persistable persistable = collect.getPersistable(i);
            if (persistable instanceof WTPart) {
                WTPart part = (WTPart) persistable;
                String partNumber = part.getNumber();
                Object obj = partNumber.substring(0, 1);
                if (obj.toString().equals("A") || obj.toString().equals("B")) {
                    set.add(part);
                }
            }
        }
        List<EffectiveBaselineBean> listBean = MversionControlHelper.buildAllEffectiveParts(process, set);
        if (!listBean.isEmpty()) {
            insertEffectiveBaselineBean(listBean);
        }
    }

    /**
     * 添加有效基线数据----归档流程-(首版)
     *
     * @param pbo
     * @param self
     * @throws WTException
     * @throws ParseException
     * @throws DocumentException
     * @throws IOException
     */
    public static void insertGDEffecitveBaseline(WTObject pbo, ObjectReference self) throws WTException, IOException, DocumentException, ParseException {
        WfProcess process = (WfProcess) self.getObject();
        Set<WTPart> set = new HashSet<WTPart>();
        WTArrayList collect = collect(self);
        for (int i = 0; i < collect.size(); i++) {
            Persistable persistable = collect.getPersistable(i);
            if (persistable instanceof WTPart) {
                WTPart part = (WTPart) persistable;
//							String partVersion = part.getVersionInfo().getIdentifier().getValue();
//								if(partVersion.equals("A")){

                String partNumber = part.getNumber();
                Object obj = partNumber.substring(0, 1);
                if (obj.toString().equals("A") || obj.toString().equals("B")) {
                    set.add(part);
                }
//								}
            }
        }
        List<EffectiveBaselineBean> listBean = MversionControlHelper.buildAllEffectiveParts(process, set);
        if (!listBean.isEmpty()) {
            insertEffectiveBaselineBean(listBean);
        }
    }

    /**
     * 添加有效基线数据----归档流程-(非首版)
     *
     * @param pbo
     * @param self
     * @throws WTException
     * @throws ParseException
     * @throws DocumentException
     * @throws IOException
     */
    public static void insertECNEffecitveBaseline(WTObject pbo, ObjectReference self) throws WTException, IOException, DocumentException, ParseException {
        WTChangeOrder2 eco = (WTChangeOrder2) pbo;
        Set<WTPart> set = new HashSet<WTPart>();
        WfProcess process = (WfProcess) self.getObject();
        QueryResult qs = ChangeHelper2.service.getChangeActivities(eco);
        while (qs.hasMoreElements()) {
            WTChangeActivity2 activity = (WTChangeActivity2) qs.nextElement();
            QueryResult qsafter = ChangeHelper2.service.getChangeablesAfter(activity);
            while (qsafter.hasMoreElements()) {
                Object obj = qsafter.nextElement();
                if (obj instanceof WTPart) {
                    WTPart part = (WTPart) obj;
                    String partVersion = part.getVersionInfo().getIdentifier().getValue();
                    if (!partVersion.equals("A") && part.getLifeCycleState().toString().equals(ChangeConstants.RELEASED)) {
                        String partNumber = part.getNumber();
                        Object obj2 = partNumber.substring(0, 1);
                        if (obj2.toString().equals("A") || obj2.toString().equals("B")) {
                            set.add(part);
                        }
                    }
                }
            }
        }
//					 WfProcess process = (WfProcess) self.getObject();
//			         Set<WTPart> set = new HashSet<WTPart>();
//			         WTArrayList collect = collect(self);
//						for (int i = 0; i < collect.size(); i++) {
//							Persistable persistable = collect.getPersistable(i);
//							if (persistable instanceof WTPart) {
//								WTPart part = (WTPart) persistable;
//								String partVersion = part.getVersionInfo().getIdentifier().getValue();
//									if(!partVersion.equals("A")){
//										set.add(part);
//									}
//								}
//						}
        List<EffectiveBaselineBean> listBean = MversionControlHelper.buildAllEffectiveParts(process, set);
        if (!listBean.isEmpty()) {
            insertEffectiveBaselineBean(listBean);
        }
    }

    /**
     * 移除有效基线数据
     *
     * @param pbo
     * @param self
     * @throws WTException
     * @throws ParseException
     * @throws DocumentException
     * @throws IOException
     */
    public static void deleteEffecitveBaseline(WTObject pbo, ObjectReference self) throws WTException, IOException, DocumentException, ParseException {
        WfProcess process = (WfProcess) self.getObject();
        Set<WTPart> set = new HashSet<WTPart>();
        WTArrayList collect = collect(self);
        for (int i = 0; i < collect.size(); i++) {
            Persistable persistable = collect.getPersistable(i);
            if (persistable instanceof WTPart) {
                WTPart part = (WTPart) persistable;
                set.add(part);
            }
        }


        List<EffectiveBaselineBean> listBean = MversionControlHelper.buildAllEffectiveParts(process, set);
        if (!listBean.isEmpty()) {
            deleteEffectiveBaselineBean(listBean);
        }

    }


    /**
     * 搜集随签列表中的对象
     *
     * @param self
     * @return
     * @throws WTException
     */
    public static WTArrayList collect(ObjectReference self) throws WTException {
        WfProcess wfprocess = null;
        WTArrayList list = new WTArrayList();
        wfprocess = WorkflowUtil.getProcess(self);
        ProcessReviewObjectLink link = null;
        if (wfprocess != null) {
            QueryResult queryresult = ProcessReviewObjectLinkHelper.service.getProcessReviewObjectLinkByRoleA(wfprocess);
            while (queryresult != null && queryresult.hasMoreElements()) {
                link = (ProcessReviewObjectLink) queryresult.nextElement();
                WTObject obj = (WTObject) link.getRoleBObject();
                list.add(obj);
            }
        }
        return list;

    }

    /**
     * 搜集随签列表中的对象
     *
     * @param self
     * @return
     * @throws WTException
     */
    public static WTArrayList collectProcess(WfProcess wfprocess) throws WTException {
        WTArrayList list = new WTArrayList();
        ProcessReviewObjectLink link = null;
        if (wfprocess != null) {
            QueryResult queryresult = ProcessReviewObjectLinkHelper.service.getProcessReviewObjectLinkByRoleA(wfprocess);
            while (queryresult != null && queryresult.hasMoreElements()) {
                link = (ProcessReviewObjectLink) queryresult.nextElement();
                WTObject obj = (WTObject) link.getRoleBObject();
                list.add(obj);
            }
        }
        return list;

    }

    /**
     * 添加有效基线
     * @param listBean
     * @return
     * @throws WTException
     * @throws IOException
     * @throws DocumentException
     * @throws ParseException
     */
    public static int insertEffectiveBaselineBean(List<EffectiveBaselineBean> listBean) throws WTException, IOException, DocumentException, ParseException {
        Connection conn = getConn();
        System.out.println("conn=================================" + conn);
        int i = 0;
        String sql = "insert into ED_EffectiveBaseline (processoid,beanUID,upUID,partoid,partvid,partNumber,partName,partState,parentoid,parentvid,parenNumber," + "parenName,parenState,usageLinkoid,createTime,substituteLinkoid,alternateLinkoid)" + " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement pstmt = null;
        try {
            System.out.println("listBean========================" + listBean);
            pstmt = conn.prepareStatement(sql);
            for (EffectiveBaselineBean bean : listBean) {
//	    			String partNumber = bean.getPartNumber();
//	    			Object obj = partNumber.substring(0,1);
//	    			if(obj.toString().equals("A") || obj.toString().equals("B")){    		
                System.out.println("getBeanUID=================" + bean.getBeanUID());
                // pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, bean.getProcessoid() == null ? "" : bean.getProcessoid());
                pstmt.setString(2, bean.getBeanUID() == null ? "" : bean.getBeanUID());
                pstmt.setString(3, bean.getUpUID() == null ? "" : bean.getUpUID());
                pstmt.setString(4, bean.getPartoid() == null ? "" : bean.getPartoid());
                pstmt.setString(5, bean.getPartvid() == null ? "" : bean.getPartvid());
                pstmt.setString(6, bean.getPartNumber() == null ? "" : bean.getPartNumber());
                pstmt.setString(7, bean.getPartName() == null ? "" : bean.getPartName());
                pstmt.setString(8, bean.getPartState() == null ? "" : bean.getPartState());
                pstmt.setString(9, bean.getParentoid() == null ? "" : bean.getParentoid());
                pstmt.setString(10, bean.getParentvid() == null ? "" : bean.getParentvid());
                pstmt.setString(11, bean.getParenNumber() == null ? "" : bean.getParenNumber());
                pstmt.setString(12, bean.getParenName() == null ? "" : bean.getParenName());
                pstmt.setString(13, bean.getParenState() == null ? "" : bean.getParenState());
                pstmt.setString(14, bean.getUsageLinkoid() == null ? "" : bean.getUsageLinkoid());
                pstmt.setString(15, bean.getCreateTime() == null ? "" : bean.getCreateTime());
                pstmt.setString(16, bean.getSubstituteLinkoid() == null ? "" : bean.getSubstituteLinkoid());
                pstmt.setString(17, bean.getAlternateLinkoid() == null ? "" : bean.getAlternateLinkoid());
                System.out.println("pstmt111111=================================" + pstmt);
                pstmt.addBatch();
                System.out.println("pstmt=================================" + pstmt);
            }
            pstmt.executeBatch();
            conn.commit();

            //pstmt.close();
            // conn.close();
        } catch (SQLException e) {
            e.printStackTrace();

        } finally {

            try {
                pstmt.close();
                conn.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return i;
    }


    /**
     * 移除
     *
     * @param doc
     * @param name
     * @param part
     * @return
     * @throws WTException
     * @throws IOException
     * @throws ParseException
     * @throws DocumentException
     */
    public static int deleteEffectiveBaselineBean(List<EffectiveBaselineBean> listBean) throws WTException, IOException, DocumentException, ParseException {
        Connection conn = getConn();
        int i = 0;
        String sql = "delete from ED_EffectiveBaseline where processoid=?";
        PreparedStatement pstmt = null;
        try {
            for (EffectiveBaselineBean bean : listBean) {
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, bean.getProcessoid());
                i = pstmt.executeUpdate();
            }
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return i;
    }

    public static Set<EffectiveBaselineBean> checkSendEffectiveBaselineData(WTPart part) throws IOException, WTException {
        PreparedStatement pstmt;
        Set<EffectiveBaselineBean> listBean = new HashSet<>();

        try {
            Connection conn = EffecitveBaselineUtil.getConn();

//	          WfRequester wfRequester = process.getRequester();
//	 		   if (wfRequester instanceof WfRequesterActivity) {
//	 			   WfRequesterActivity wfRequesterActivity = (WfRequesterActivity) wfRequester;
//	 				WfProcess parentWfProcess = wfRequesterActivity.getParentProcess();
//	 				String pid = MversionControlHelper.getOidByObject(parentWfProcess);
//	        	
//	        	Set<WTPart> setPart = new HashSet<WTPart>();
//	        	if(parentWfProcess != null) {
//	        	 WTArrayList collect = collectProcess(parentWfProcess);
//	        	 System.out.println("collect_____-XXXXXX"+collect);
//					for (int i = 0; i < collect.size(); i++) {
//						Persistable persistable = collect.getPersistable(i);
//						if (persistable instanceof WTPart) {
//							WTPart part = (WTPart) persistable;
//							String partNumber = part.getNumber();
//			    			Object obj = partNumber.substring(0,1);
//			    			if(obj.toString().equals("A") || obj.toString().equals("B")){
//							setPart.add(part);
//			    			}
//						}
//					}
//	        	}
//					System.out.println("setPart______-XXXXXX"+setPart);
//	
//	        	 System.out.println("checkFolderId====================="+conn);
//	        	 if(!setPart.isEmpty()) {
//			        	 for(WTPart part : setPart) {
            String oid = getOidByObject(part);
            String sql = "select n.*,level  from  ED_EffectiveBaseline n  CONNECT BY  PRIOR beanUID = upUID start WITH partoid = '" + oid + "'";

            System.out.println("sql=========================" + sql);
            pstmt = (PreparedStatement) conn.prepareStatement(sql);
            System.out.println("checkFolderId============pstmt=========" + pstmt);
            ResultSet rs = pstmt.executeQuery();
            System.out.println("checkFolderId============rs=========" + rs);
            //5.处理ResultSet
            while (rs.next()) {
                int level = rs.getInt(18);
//			        			 String first = partName.substring(0, 1);

                if (level == 2) {
                    //rs.get+数据库中对应的类型+(数据库中对应的列别名)
                    EffectiveBaselineBean bean = new EffectiveBaselineBean();
                    bean.setBeanUID(rs.getString(1));
                    bean.setProcessoid(rs.getString(2));
                    bean.setUpUID(rs.getString(3));
                    bean.setPartoid(rs.getString(4));
                    bean.setPartvid(rs.getString(5));
                    bean.setPartNumber(rs.getString(6));
                    bean.setPartName(rs.getString(7));
                    bean.setPartState(rs.getString(8));
                    bean.setParentoid(rs.getString(9));
                    bean.setParentvid(rs.getString(10));
                    bean.setParenNumber(rs.getString(11));
                    bean.setParenName(rs.getString(12));
                    bean.setParenState(rs.getString(13));
                    bean.setUsageLinkoid(rs.getString(14));
                    bean.setCreateTime(rs.getString(15));
                    bean.setSubstituteLinkoid(rs.getString(16));
                    bean.setAlternateLinkoid(rs.getString(17));
                    bean.setLevel(rs.getString(18));
                    listBean.add(bean);
                }
            }
//			        	 }
//	        	 }
//	 		   }

//	        	 System.out.println("ECN??=============================================="+ecn);
//	        	 Set<WTPart> set = new HashSet<WTPart>();
//	        	 if(ecn != null) {
//				  QueryResult qs = ChangeHelper2.service.getChangeActivities(ecn);
//			      while (qs.hasMoreElements()) {
//		                WTChangeActivity2 activity = (WTChangeActivity2) qs.nextElement();
//		                System.out.println("eca???==========================================="+activity);
//		                QueryResult qsafter = ChangeHelper2.service.getChangeablesAfter(activity);
//		                while (qsafter.hasMoreElements()) {
//		                    Object obj = qsafter.nextElement();
//		                    if (obj instanceof WTPart) {
//		                        WTPart part = (WTPart) obj;
//		                        System.out.println("WTPART???================================"+part);
//		                        String partVersion = part.getVersionInfo().getIdentifier().getValue();
//		                        System.out.println("partVersion???============================"+partVersion);
//		                        String partNumber = part.getNumber();
//				    			Object obj1 = partNumber.substring(0,1);
//				    			if(obj1.toString().equals("A") || obj1.toString().equals("B")){
////		                        if(!partVersion.equals("A")){
//		                        	set.add(part);
////		                        }
//				    			}
//		                    }
//		                }
//		            }
//	        	 }

//	        	 System.out.println("======================ECNSET====================="+set);
//			      if(!set.isEmpty()){
//			    	   for(WTPart part : set) {
//			    		   String vid = getVidByObject(part);
//			        		 String sql = "select n.*,level  from  ED_EffectiveBaseline n  CONNECT BY  PRIOR beanUID = upUID start WITH partvid = '"+vid+"'";
//		
//			        		 System.out.println("sql========================="+sql);
//			        		 pstmt = (PreparedStatement)conn.prepareStatement(sql);
//			        		 System.out.println("checkFolderId============pstmt========="+pstmt);
//			        		 ResultSet rs = pstmt.executeQuery();
//			        		 System.out.println("checkFolderId============rs========="+rs);
//			        		 //5.处理ResultSet
//			        		 while(rs.next()){
//			        			 
//			        			 int level = rs.getInt(18);
//			        			 if(level <=2) {
//				                //rs.get+数据库中对应的类型+(数据库中对应的列别名)
//			        			 EffectiveBaselineBean bean = new EffectiveBaselineBean();
//			        			 bean.setBeanUID(rs.getString(1));
//				                 bean.setProcessoid(rs.getString(2));
//				                 bean.setUpUID(rs.getString(3));
//				            	 bean.setPartoid(rs.getString(4));
//				                 bean.setPartvid(rs.getString(5));
//				            	 bean.setPartNumber(rs.getString(6));
//				            	 bean.setPartName(rs.getString(7));
//				            	 bean.setPartState(rs.getString(8));
//			            		 bean.setParentoid(rs.getString(9));
//			            		 bean.setParentvid(rs.getString(10));
//			            		 bean.setParenNumber(rs.getString(11));
//			            		 bean.setParenName(rs.getString(12));
//			            		 bean.setParenState(rs.getString(13));
//			            		 bean.setUsageLinkoid(rs.getString(14));
//			            		 bean.setCreateTime(rs.getString(15));
//			            		 bean.setSubstituteLinkoid(rs.getString(16));
//			            		 bean.setAlternateLinkoid(rs.getString(17));
//			            		 bean.setLevel(rs.getString(18));
//			            		 listBean.add(bean);
//			        			 }
//			            	}
//			    	   }
//			      }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listBean;
    }

    public static Object getGlobalVariable(WfProcess process, String vname) {
        if (process == null) {
            return null;
        }
        ProcessData processdata = process.getContext();
        WfVariable wfvariable = processdata.getVariable(vname);
        if (wfvariable != null) {
            return wfvariable.getValue();
        } else {
            return null;
        }
    }

    /**
     * 建立jdbc链接
     *
     * @return
     * @throws IOException
     */
    public static Connection getConn() throws IOException {

        Map<String, String> map = getSendDocDataBeans();
        String driver = "oracle.jdbc.driver.OracleDriver";
        String[] arrtPort = map.get("dataPort").split("\\.");
        String url = "jdbc:oracle:thin:@" + map.get("dataAddress") + ":" + arrtPort[0] + ":" + map.get("dataStorageRoom");
        System.out.println("url================================" + url);
        String username = map.get("userName");
        String password = map.get("password");
        Connection conn = null;
        try {
            Class.forName(driver); //classLoader,加载对应驱动
            conn = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }


    public static Map<String, String> getSendDocDataBeans() throws IOException {
        Map<String, String> map = new HashMap<>();

        String filePath = WT_CODEBASE + File.separator + "config" + File.separator + "custom" + File.separator + "windJDBCDataConfigure.xlsx";
        Excel2007Handler handler = new Excel2007Handler(filePath);
//	        Excel2007Handler handler = new Excel2007Handler("C:\\jdbcDataConfigure.xlsx");
        handler.switchCurrentSheet(0);
        int rowCount = handler.getSheetRowCount();
        for (int i = 1; i < rowCount; i++) {
            if (i > 5) {
                break;
            }
            map.put("userName", null2blank(handler.getStringValue(1, 1)));
            map.put("password", null2blank(handler.getStringValue(2, 1)));
            map.put("dataAddress", null2blank(handler.getStringValue(3, 1)));
            map.put("dataPort", null2blank(handler.getStringValue(4, 1)));
            map.put("dataStorageRoom", null2blank(handler.getStringValue(5, 1)));
        }
        return map;
    }

    public static String null2blank(Object obj) {
        if (obj == null) {
            return "";
        } else {
            String tmp = obj.toString();
            return tmp.trim();
        }
    }

    /**
     * GET OID BY PERSISTABLE
     *
     * @param p
     * @return
     * @throws WTException
     */
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

    public static Persistable getObjectByOid(String oid) throws WTException {
        Persistable p = null;

        boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
        try {
            ReferenceFactory referencefactory = new ReferenceFactory();
            WTReference wtreference = referencefactory.getReference(oid);
            p = wtreference.getObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SessionServerHelper.manager.setAccessEnforced(flag);
        }

        return p;
    }

    /**
     * getVID
     *
     * @param p
     * @return
     */
    public static String getVidByObject(Persistable p) {
        String oid = "";
        if (p != null) {
            RevisionControlled rc = (RevisionControlled) p;

            oid = "VR:" + rc.getPersistInfo().getObjectIdentifier().getClassname() + ":" + rc.getBranchIdentifier();
        }
        return oid;
    }

    public static String getReferenceDesignatorSet(WTPartUsageLink link) {
        // 取位号
        String consolidatedReferenceDesignators = null;
        try {
            QueryResult occurrences = wt.occurrence.OccurrenceHelper.service.getUsesOccurrences(link);
            @SuppressWarnings("rawtypes") Vector occurrencesVec = occurrences.getObjectVectorIfc().getVector();
            List<String> list = new ArrayList<String>();
            if (occurrencesVec != null) {
                for (int i = 0; i < occurrencesVec.size(); i++) {
                    PartUsesOccurrence occurrence = (PartUsesOccurrence) occurrencesVec.get(i);
                    if (StringUtils.isNotEmpty(occurrence.getName())) {
                        list.add(occurrence.getName());
                    }
                }
            }
            ReferenceDesignatorSetDelegateFactory rdsf = new ReferenceDesignatorSetDelegateFactory();
            ReferenceDesignatorSet rds = rdsf.get(list);
            if (rds != null) {
                consolidatedReferenceDesignators = rds.getConsolidatedReferenceDesignators();
            }
        } catch (WTException e) {
            e.printStackTrace();
        }
        return consolidatedReferenceDesignators;
    }

    /**
     * 打印影响物料状态ecn
     *
     * @param pbo
     * @param self
     * @throws WTException
     * @throws ParseException
     * @throws DocumentException
     * @throws IOException
     */
    public static void insertChangPartStateECN(WTObject pbo, ObjectReference self) throws WTException, IOException, DocumentException, ParseException {
        WTChangeOrder2 eco = (WTChangeOrder2) pbo;
        Set<WTPart> setCountPart = new HashSet<WTPart>();
        QueryResult qs = ChangeHelper2.service.getChangeActivities(eco);
        while (qs.hasMoreElements()) {
            WTChangeActivity2 activity = (WTChangeActivity2) qs.nextElement();
            QueryResult qsafter = ChangeHelper2.service.getChangeablesAfter(activity);
            while (qsafter.hasMoreElements()) {
                Object obj = qsafter.nextElement();
                if (obj instanceof WTPart) {
                    WTPart part = (WTPart) obj;

                    Map<WTPart, Set<WTPart>> testmap = new HashMap<WTPart, Set<WTPart>>();

                    getZPartAndLink(part, testmap);

                    Set<WTPart> childPartSet = new HashSet<WTPart>();
                    for (Set<WTPart> set : testmap.values()) {
                        childPartSet.addAll(set);
                    }

                    for (WTPart setPart : childPartSet) {
                        if (setPart.getLifeCycleState().toString().equals(ChangeConstants.ARCHIVED)) {
                            setCountPart.add(setPart);
                        }
                    }

                }
            }
        }

        List<ChangPartStateECNBean> listBean = new ArrayList<>();
        for (WTPart part : setCountPart) {
            ChangPartStateECNBean bean = new ChangPartStateECNBean();
            String partOid = getOidByObject(part);
            String partVid = getVidByObject(part);
            String ecnOid = getOidByObject(eco);

            bean.setUuid(UUID.randomUUID().toString());
            bean.setPartoid(partOid);
            bean.setPartvid(partVid);
            bean.setPartNumber(part.getNumber());
            bean.setPartName(part.getName());
            bean.setEcnoid(ecnOid);
            bean.setEcnNumber(eco.getNumber());
            bean.setEcnName(eco.getName());
            listBean.add(bean);
        }
        if (!listBean.isEmpty()) {
            insertChangPartStateECNBean(listBean);
        }
    }

    /**
     * 打印影响物料状态ecn
     *
     * @param doc
     * @param name
     * @param part
     * @return
     * @throws WTException
     * @throws IOException
     * @throws ParseException
     * @throws DocumentException
     */
    public static int insertChangPartStateECNBean(List<ChangPartStateECNBean> listBean) throws WTException, IOException, DocumentException, ParseException {
        Connection conn = getConn();
        System.out.println("conn=========insertChangPartStateECNBean========================" + conn);
        int i = 0;
        String sql = "insert into ED_ChangPartStateECN (uuid,partoid,partvid,partName,partNumber,ecnoid,ecnName,ecnNumber)" + "values(?,?,?,?,?,?,?,?)";
        PreparedStatement pstmt = null;
        try {
            System.out.println("listBean=========insertChangPartStateECNBean===============" + listBean);
            for (ChangPartStateECNBean bean : listBean) {
                System.out.println("getBeanUID===insertChangPartStateECNBean==============" + bean.getUuid());
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, bean.getUuid() == null ? "" : bean.getUuid());
                pstmt.setString(2, bean.getPartoid() == null ? "" : bean.getPartoid());
                pstmt.setString(3, bean.getPartvid() == null ? "" : bean.getPartvid());
                pstmt.setString(4, bean.getPartName() == null ? "" : bean.getPartName());
                pstmt.setString(5, bean.getPartNumber() == null ? "" : bean.getPartNumber());
                pstmt.setString(6, bean.getEcnoid() == null ? "" : bean.getEcnoid());
                pstmt.setString(7, bean.getEcnName() == null ? "" : bean.getEcnName());
                pstmt.setString(8, bean.getEcnNumber() == null ? "" : bean.getEcnNumber());
                System.out.println("pstmt====insertChangPartStateECNBean==========================" + pstmt);
                i = pstmt.executeUpdate();
                System.out.println("pstmt=======insertChangPartStateECNBean==========================" + pstmt);
            }
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return i;
    }

    /**
     * 通过父项获取子件：单层
     *
     * @param fpart
     * @return
     * @throws WTException
     */
    public static Set<WTPart> getMonolayerPart(WTPart part) throws WTException {
        Set<WTPart> setPart = new HashSet<WTPart>();
        part = MversionControlHelper.getLastObjectAndRelease(part.getMaster());
        if (part != null) {
            QueryResult qr = WTPartHelper.service.getUsesWTPartMasters(part);
            while (qr.hasMoreElements()) {
                WTPartUsageLink links = (WTPartUsageLink) qr.nextElement();
                WTPartMaster masterChild = links.getUses();
                WTPart sunpart = (WTPart) PartUtil.getLastestWTPartByNumber(masterChild.getNumber());
                setPart.add(sunpart);
            }
        }
        return setPart;
    }

    public static List<Persistable> checkChangPartStateECNBeanData(String vid) throws IOException, WTException {
        PreparedStatement pstmt;
        List<Persistable> listBean = new ArrayList<>();
        try {
            Connection conn = EffecitveBaselineUtil.getConn();
            System.out.println("checkFolderId=====================" + conn);
            String sql = "select * from ED_ChangPartStateECN where partvid='" + vid + "'";

            System.out.println("sql=========================" + sql);
            pstmt = (PreparedStatement) conn.prepareStatement(sql);
            System.out.println("checkFolderId============pstmt=========" + pstmt);
            ResultSet rs = pstmt.executeQuery();
            System.out.println("checkFolderId============rs=========" + rs);
            //5.处理ResultSet
            while (rs.next()) {
                listBean.add(getObjectByOid(rs.getString(6)));
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listBean;
    }

    /**
     * 通过父项获取子件：所有
     *
     * @param fpart
     * @return
     * @throws WTException
     */
    public static void getZPartAndLink(WTPart fpart, Map<WTPart, Set<WTPart>> testmap) throws WTException {
        Set<WTPart> partlist = new HashSet<>();
        fpart = MversionControlHelper.getLastObjectAndRelease(fpart.getMaster());
        if (fpart != null) {
            QueryResult qr = WTPartHelper.service.getUsesWTPartMasters(fpart);
            while (qr.hasMoreElements()) {
                WTPartUsageLink links = (WTPartUsageLink) qr.nextElement();
//					Object domain = MBAUtil.getValue(links, "Domain");
//					    String result = "";
//					   String field = domain == null ? "" : domain.toString();
//		            
//					   if(BOMConst.total_type.contains(option) || BOMConst.replace_type.contains(option)){
//					   int leng=option.indexOf(BOMConst.whole);
//					   result=option.substring(0,leng);
//					   }
//					   if(result.equals(field)){
                WTPartMaster masterChild = links.getUses();
                WTPart p = (WTPart) getLatestVersionByMaster(masterChild);
                partlist.add(p);
//					   }
            }
        }
        testmap.put(fpart, partlist);//1:2,3

        for (WTPart part : partlist) {
            getZPartAndLink(part, testmap);
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
     * 获取选择的oid
     *
     * @param soid
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<String> getSelectedOid(List soid) {
        List<String> oids = new ArrayList<String>();
        String ids[];
        for (Iterator i$ = soid.iterator(); i$.hasNext(); oids.add(ids[ids.length - 1])) {
            Object oid = i$.next();
            String str = oid.toString();
            str = str.replace("!*", "");
            ids = str.split("\\$", -1);
        }

        return oids;
    }

    // 获取特定替换件
    public static List<WTPartSubstituteLink> getSubstituteLinks(WTPartUsageLink usageLink) throws WTException {
        List<WTPartSubstituteLink> substituteLinks = new ArrayList<WTPartSubstituteLink>();
        WTCollection collection = WTPartHelper.service.getSubstituteLinks(usageLink);
        for (Object object : collection) {
            if (object instanceof ObjectReference) {
                ObjectReference objref = (ObjectReference) object;
                Object obj = objref.getObject();

                if (obj instanceof WTPartSubstituteLink) {
                    WTPartSubstituteLink link = (WTPartSubstituteLink) obj;
                    substituteLinks.add(link);
                }
            }
        }

        return substituteLinks;
    }

    // 获取全局替换件
    public static List<WTPartAlternateLink> getAlternateLinks(WTPart part) throws WTException {
        List<WTPartAlternateLink> substituteLinks = new ArrayList<WTPartAlternateLink>();
        WTCollection wtCollection = AlternatesSubstitutesCommand.getAlternateParts(part);
        for (Object object : wtCollection) {
            if (object instanceof ObjectReference) {
                ObjectReference objref = (ObjectReference) object;
                Object obj = objref.getObject();

                if (obj instanceof WTPartAlternateLink) {
                    WTPartAlternateLink link = (WTPartAlternateLink) obj;
                    substituteLinks.add(link);
                }
            }
        }

        return substituteLinks;
    }

    /**
     * 获取部件
     *
     * @param number
     * @throws WTException
     * @throws RemoteException
     */
    public static WTPart getPartByNumberAndVersion(String number) throws WTException {
        WTPart wtpart = null;
        QuerySpec qs = new QuerySpec(WTPart.class);
        qs.appendWhere(new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.EQUAL, number), new int[]{0});
        QueryResult queryresult = PersistenceHelper.manager.find((StatementSpec) qs);
        if (queryresult.hasMoreElements()) {
            wtpart = (WTPart) queryresult.nextElement();
        }

        return wtpart;
    }

}
