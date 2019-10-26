package ext.appo.ecn.util;

import ext.appo.common.util.Excel2007Handler;
import ext.appo.ecn.beans.EDUpperMaterialsBean;
import ext.appo.ecn.constants.ChangeConstants;
import ext.appo.ecn.pdf.PdfUtil;
import ext.appo.part.util.EffecitveBaselineUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import wt.change2.ChangeException2;
import wt.change2.ChangeHelper2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.enterprise.Master;
import wt.epm.EPMDocument;
import wt.fc.*;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.maturity.PromotionNotice;
import wt.occurrence.Occurrence;
import wt.occurrence.OccurrenceHelper;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.vc.OneOffVersioned;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;
import wt.vc.wip.WorkInProgressHelper;
import wt.workflow.engine.WfProcess;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class AffectedMaterialsUtil {
    private static final Logger logger = LogR.getLogger(AffectedMaterialsUtil.class.getName());
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

    public static Map<String,String> getSendDocDataBeans() throws IOException{
        Map<String,String> map = new HashMap<>();

        String filePath = WT_CODEBASE + File.separator + "config" + File.separator + "custom" + File.separator + "windJDBCDataConfigure.xlsx";
        Excel2007Handler handler = new Excel2007Handler(filePath);
//	        Excel2007Handler handler = new Excel2007Handler("C:\\jdbcDataConfigure.xlsx");
        handler.switchCurrentSheet(0);
        int rowCount = handler.getSheetRowCount();
        for (int i = 1; i < rowCount; i++) {
            if(i>5){
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

    public static String null2blank(Object obj){
        if(obj == null){
            return "";
        }else{
            String tmp = obj.toString();
            return tmp.trim();
        }
    }


    /**
     * 建立jdbc链接
     * @return
     * @throws IOException
     */
    public static Connection getConn() throws IOException {

        Map<String,String> map = getSendDocDataBeans();
        String driver = "oracle.jdbc.driver.OracleDriver";
        String[] arrtPort = map.get("dataPort").split("\\.");
        String url = "jdbc:oracle:thin:@"+map.get("dataAddress")+":"+arrtPort[0]+":"+map.get("dataStorageRoom");
        System.out.println("url================================"+url);
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
//通过Oid获取对象
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


    // 获取变更后对象
    public static Map<String, WTPart> getAfterDataWithPart(WTChangeOrder2 eco) throws WTException {
        Map<String, WTPart> allMp = new HashMap<String, WTPart>();
        try {
            if (eco == null) return allMp;
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

    //获取上层部件
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

    /**
     * 根据部件查询其父件
     */
    public static List<WTPart> getParentPartByChildPart(WTPart part) {
        List<WTPart> parentParts = new ArrayList<WTPart>();
        QueryResult qr;
        try {
            qr = WTPartHelper.service.getUsedByWTParts((WTPartMaster) part.getMaster());
            while (qr != null && qr.hasMoreElements()) {
                Object obj = qr.nextElement();
                if (obj instanceof WTPart) {
                    WTPart parentPart = (WTPart) obj;
                    System.out.println("++++++++is new++++++++"+parentPart.isLatestIteration());
                    WTPart newParentPart = (WTPart) getLatestVersionByMaster(parentPart.getMaster());
                    //过滤不是最新版本的物料
                    if (!getOidByObject(parentPart).equals(getOidByObject(newParentPart))){
                        continue;
                    }
                    //LOGGER.debug(part.getName() + "的父件是--" + parentPart.getName());
                    parentParts.add(parentPart);
                }
            }
        } catch (WTException e) {
            e.printStackTrace();
        }
        return parentParts;
    }




//通过父件个子件获得link
    public static WTPartUsageLink getUsageLink(WTPart parentWTPart, WTPartMaster childWTPartMaster) throws WTException{
        QuerySpec qs = new QuerySpec(WTPartUsageLink.class);
        int[] fromIndicies = {0, wt.query.FromClause.NULL_INDEX};
        qs.appendWhere(new SearchCondition(WTPartUsageLink.class,
                ObjectToObjectLink.ROLE_AOBJECT_REF + "." + ObjectReference.KEY + "." + ObjectIdentifier.ID,
                SearchCondition.EQUAL, PersistenceHelper.getObjectIdentifier(parentWTPart).getId()), fromIndicies);
        qs.appendAnd();
        qs.appendWhere(new SearchCondition(WTPartUsageLink.class,
                ObjectToObjectLink.ROLE_BOBJECT_REF + "." + ObjectReference.KEY + "." + ObjectIdentifier.ID,
                SearchCondition.EQUAL, PersistenceHelper.getObjectIdentifier(childWTPartMaster).getId()), fromIndicies);

        QueryResult qr = PersistenceHelper.manager.find((wt.pds.StatementSpec) qs);
        while (qr.hasMoreElements()) {
            WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
            return link;
        }
        return null;
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
                            logger.debug((new StringBuilder()).append("getLatestVersionByMaster123:").append(epm).toString());
                        else list.add(p);
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


    /**
     * 通过父项获取子件Link：所有
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
                WTPartMaster masterChild = links.getUses();
                linklist.add(links);
            }

        }
        testmap.put(fpart, linklist);//1:2,3

        for (WTPartUsageLink link : linklist) {
            WTPartMaster masterChild = link.getUses();
            WTPart p = (WTPart) getLatestVersionByMaster(masterChild);
            getZPartAndLink(p, testmap);
        }
    }

    /**
     * 获取Part所有上层父Link
     *
     * @param part
     * @return
     * @throws WTException
     */
    public static void getSuperstratumParent(WTPart part, Map<WTPart, Map<WTPart, WTPartUsageLink>> maps) throws WTException {
        Map<WTPart, WTPartUsageLink> map = new HashMap<>();
        //Set<WTPartUsageLink> set = new HashSet<>();
        if(part!=null) {
            QueryResult queryresult = PersistenceHelper.manager.navigate(part.getMaster(), WTPartUsageLink.USED_BY_ROLE, WTPartUsageLink.class, false);
            while (queryresult.hasMoreElements()) {
                WTPartUsageLink usageLink = (WTPartUsageLink) queryresult.nextElement();
                WTPartMaster usedByMaster = usageLink.getUsedBy().getMaster();
                WTPart part1 = (WTPart) getLatestVersionByMaster(usedByMaster);
                map.put(part1, usageLink);
                //set.add(usageLink);
            }
        }
        //return map;
        maps.put(part, map);

//        for (WTPartMaster master : map.keySet()) {
//            WTPart p = (WTPart) getLatestVersionByMaster(master);
//            getSuperstratumParent(p, maps);
//        }

    }

    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    // 获取变更前对象
    public static Map<String, WTPart> getBeforeDataWithPart(WTChangeOrder2 eco) throws WTException {
        Map<String, WTPart> allMp = new HashMap<String, WTPart>();
        try {
            if (eco == null) return allMp;
            QueryResult qs = ChangeHelper2.service.getChangeActivities(eco);
            while (qs.hasMoreElements()) {
                WTChangeActivity2 activity = (WTChangeActivity2) qs.nextElement();
                QueryResult qsafter = ChangeHelper2.service.getChangeablesBefore(activity);//变更前对象
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

    public static Map<String, String> buildAffectedValue(WTPart parent, WTPart child, WTPartUsageLink link,String state) throws WTException {
        Map<String, String> map = new HashMap<>();
        if (link == null) {
            return map;
        }
        map.put("parentNumber", child.getNumber());
        map.put("parentName", child.getName());
        map.put("parentVersion", child.getVersionIdentifier().getValue() + "." + child.getIterationIdentifier().getValue() + "(" + child.getViewName() + ")");
        //map.put("parentState", child.getLifeCycleState().getDisplay(SessionHelper.getLocale()).toString());
        map.put("parentState", state);
        map.put("childNumber", parent.getNumber());
        map.put("childName", parent.getName());
        map.put("childVersion", parent.getVersionIdentifier().getValue() + "." + parent.getIterationIdentifier().getValue() + "(" + parent.getViewName() + ")");
        map.put("childState", parent.getLifeCycleState().getDisplay(SessionHelper.getLocale()).toString());
        map.put("weihao", getPartReferenceDesignators(link));//位号
        map.put("danwei", link.getQuantity().getUnit().getDisplay(SessionHelper.getLocale()));//单位
        map.put("shuliang", getLinkSum(link));//数量
        String stockGrade = (String)PdfUtil.getIBAObjectValue(link, "stockGrade");
        String bomNote = (String)PdfUtil.getIBAObjectValue(link, "bom_note");
        map.put("zdchdj", stockGrade == null ? "" :   stockGrade);//最低存货等级
        map.put("bombzxx", bomNote == null ? "" :   bomNote);//BOM备注信息
        map.put("oid", getOidByObject(parent) + "___" + getOidByObject(child) + "___" + getOidByObject(link));
        return map;
    }



    //存储数据
    public static void saveChangeData(EDUpperMaterialsBean bean,String flag) {

        PreparedStatement ps;
        try {
            Connection conn = getConn();
            String sql = "insert into ED_UpperMaterials (UUID,ecoOid,processoid,upDownOid,partOid,usageLinkoid,updownState,flag,createTime) values(?,?,?,?,?,?,?,?,?)";
            ps = (PreparedStatement) conn.prepareStatement(sql);
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, bean.getEcoOid());
            ps.setString(3, bean.getProcessoid());
            ps.setString(4, bean.getUpDownOid());
            ps.setString(5, bean.getPartOid());
            ps.setString(6, bean.getUsageLinkoid());
            ps.setString(7, bean.getState());
            ps.setString(8, flag);
            ps.setString(9, getCurrentDate());
            ps.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//通过ecoOid获取受影响物料信息
    public static List<EDUpperMaterialsBean> getChangeData(String ecoOid,String flag) {
        PreparedStatement ps = null;
        List<EDUpperMaterialsBean> list = new ArrayList<>();
        try {
            Connection conn = getConn();
                String sql = "select *  from  ED_UpperMaterials where ecoOid=? and flag=?";
                ps = (PreparedStatement) conn.prepareStatement(sql);
                ps.setString(1, ecoOid);
                ps.setString(2, flag);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String UUID = rs.getString("UUID");
                String ecoOid1 = rs.getString("ecoOid");
                String processoid = rs.getString("processoid");
                String partOid = rs.getString("partOid");
                String upDownOid = rs.getString("upDownOid");
                String usageLinkoid = rs.getString("usageLinkoid");
                String state = rs.getString("updownState");
                String createTime = rs.getString("createTime");
                EDUpperMaterialsBean bean = new EDUpperMaterialsBean(UUID, ecoOid1,processoid, partOid,upDownOid,state, usageLinkoid, createTime);
                list.add(bean);
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }



    //保存表格数据
    public static void  saveAffectedPartValue(WTObject pbo, ObjectReference self) throws WTException {

        WfProcess process = (WfProcess) self.getObject();
        String processoId = getOidByObject(process);
        WTChangeOrder2 ecn = null;
        if (pbo instanceof WTChangeOrder2){
            ecn = (WTChangeOrder2)pbo;
        }else {
            return;
        }

        String ecooid=getOidByObject(ecn);
        //变更后对象
        Map<String, WTPart> map = getAfterDataWithPart(ecn);

        //变更前对象
        Map<String, WTPart>beforeMap = getBeforeDataWithPart(ecn);
        for (String num : map.keySet()) {
            WTPart part = map.get(num);
            String state = part.getLifeCycleState().toString();

            //获取变更前对象的状态
            for (String beforeNum:beforeMap.keySet()){
                if (num.equals(beforeNum)){
                    WTPart beforePart = beforeMap.get(beforeNum);
                    state = beforePart.getLifeCycleState().toString();
                }
            }

            System.out.println("++++++++state+++++"+state);

            //归档状态==============
            if (ChangeConstants.ARCHIVED.equals(state)) {

//                    Map<WTPart, Map<WTPart, WTPartUsageLink>> maps = new HashMap<>();
//                    getSuperstratumParent(part, maps);
//                    Map<WTPart, WTPartUsageLink> parentMap = new HashMap<>();
//                    for (Map<WTPart, WTPartUsageLink> ignored : maps.values()) {
//                        parentMap.putAll(ignored);
//                    }
//
//                    for (WTPart parentPart : parentMap.keySet()) {
//                            //WTPart parentPart = (WTPart) getLatestVersionByMaster(master);

                List<WTPart> parentParts  = getParentPartByChildPart(part);
                    for (WTPart parentPart:parentParts) {

                        if (map.keySet().contains(parentPart.getNumber())) {
                            continue;
                        }
                        //过滤不是归档状态的
                        if (!parentPart.getLifeCycleState().toString().equals(ChangeConstants.ARCHIVED)) {
                            continue;
                        }
                        WTPartUsageLink usageLink = getUsageLink(parentPart,part.getMaster());

                        String partOid = getOidByObject(part);
                        String upDownOid = getOidByObject(parentPart);
                        String usageLinkoid = getOidByObject(usageLink);
                        String parentState=parentPart.getState().toString();
                        EDUpperMaterialsBean bean = new EDUpperMaterialsBean(null,ecooid,processoId,partOid,upDownOid,parentState,usageLinkoid,null);
                        String flag = "1";//1代表上街，2代表子阶
                        //存数据库
                        saveChangeData(bean,flag);

                    }

            }


            //发布状态=============
            if (ChangeConstants.RELEASED.equals(state)) {

                //加入所有父件件==========================

//                    Map<WTPart, Map<WTPart, WTPartUsageLink>> maps = new HashMap<>();
//                    getSuperstratumParent(part, maps);
//
//                    Map<WTPart, WTPartUsageLink> parentMap = new HashMap<>();
//                    for (Map<WTPart, WTPartUsageLink> ignored : maps.values()) {
//                        parentMap.putAll(ignored);
//                    }
//
//                    for (WTPart parentPart : parentMap.keySet()) {
//
//                        //WTPart parentPart = (WTPart) getLatestVersionByMaster(master);
//                        System.out.println("++++++parentPart+++++++++++" + parentPart.getName() + parentPart);
                List<WTPart> parentParts  = getParentPartByChildPart(part);

                for (WTPart parentPart : parentParts) {

                        if (map.keySet().contains(parentPart.getNumber())) {
                            continue;
                        }

                        //过滤不是归档和发布状态的
                        if (!parentPart.getLifeCycleState().toString().equals(ChangeConstants.ARCHIVED) && !parentPart.getLifeCycleState().toString().equals(ChangeConstants.RELEASED)) {
                            continue;
                        }
                    WTPartUsageLink usageLink = getUsageLink(parentPart,part.getMaster());
                        String partOid = getOidByObject(part);
                        String upDownOid = getOidByObject(parentPart);
                        String usageLinkoid = getOidByObject(usageLink);
                        String parentState=parentPart.getState().toString();
                        String flag = "1";//1代表上街，2代表子阶
                        EDUpperMaterialsBean bean = new EDUpperMaterialsBean(null,ecooid,processoId,partOid,upDownOid,parentState,usageLinkoid,null);
                        //存数据库
                        saveChangeData(bean,flag);

                    }



                //加入所有子件===================================
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
                    //过滤不是归档状态的
                    if (!childPart.getLifeCycleState().toString().equals(ChangeConstants.ARCHIVED)) {
                        continue;
                    }

                    String partOid = getOidByObject(part);
                    String upDownOid = getOidByObject(childPart);
                    String usageLinkoid = getOidByObject(childLink);
                    String childState=childPart.getState().toString();
                    System.out.println("+++++++++++childState++++++++++"+childState);
                    String flag = "2";//1代表上街，2代表子阶
                    EDUpperMaterialsBean bean = new EDUpperMaterialsBean(null,ecooid,processoId,partOid,upDownOid,childState,usageLinkoid,null);
                    //存数据库
                    saveChangeData(bean,flag);
                }
            }
        }
    }


public static List getBuilderDateValue(WTChangeOrder2 ecn,String flag) throws WTException {
    List<Map<String, String>> list = new ArrayList<>();
    String ecnoid=getOidByObject(ecn);
    List<EDUpperMaterialsBean>beans = getChangeData(ecnoid,flag);
        for (EDUpperMaterialsBean bean:beans){
            Map<String,String>kv = new HashMap<>();
            WTPart part =(WTPart)getObjectByOid(bean.getPartOid());
            WTPart relPart =(WTPart)getObjectByOid(bean.getUpDownOid());
            State s = State.toState(bean.getState());
            String upDownState = s.getDisplay(SessionHelper.getLocale());
            WTPartUsageLink link = (WTPartUsageLink)getObjectByOid(bean.getUsageLinkoid());
            kv = buildAffectedValue(part,relPart,link,upDownState);
            list.add(kv);
        }
        return list;
}



}

