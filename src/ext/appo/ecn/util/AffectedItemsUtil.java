package ext.appo.ecn.util;

import ext.appo.common.util.Excel2007Handler;
import ext.appo.ecn.beans.QuantitativePriceBean;
import ext.generic.integration.erp.bean.InventoryPrice;
import ext.generic.integration.erp.util.SqlHelperUtil;
import wt.util.WTException;
import wt.util.WTProperties;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class AffectedItemsUtil {

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

    //将K3物料编码，大版本，在制，在途，库存,所属公司id,所属公司名称，计划员，采购员， 插入数据库
    public static  Integer insertEXT_STOCK_TAB() throws Exception {
        System.out.println("开始执行插入EXT_STOCK_TAB表方法=====" + new Date());
        Connection conn = null;
        Connection conn2 = null;
        Connection conn3 = null;
        Connection conn4 = null;
        Connection conn5 = null;

        int iqtySplitNumber = 20000;//库存拆分数
        List<List<Object>> iqtyCountList = new ArrayList<>();
        List<List<Object>> iqtyList = new ArrayList<>();

        int poqtySplitNumber = 20000;//在途拆分数
        List<List<Object>> poqtyCountList = new ArrayList<>();
        List<List<Object>> poqtyList = new ArrayList<>();

        int fqtySplitNumber = 20000;//在制拆分数
        List<List<Object>> fqtyCountList = new ArrayList<>();
        List<List<Object>> fqtyList = new ArrayList<>();

        int i = 0;//插入数据库是否成功，1成功

        System.out.println("开始查询处理后K3库存视图====" + new Date());
        //查询区分组织的库存 总数
        try {
            iqtyCountList = SqlHelperUtil.queryNewIqtyCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (iqtyCountList != null && iqtyCountList.size() > 0) {
            int iqtyCount = Integer.parseInt(String.valueOf(iqtyCountList.get(0).get(0)));
            System.out.println("查询到的库存总数iqtyCount===" + iqtyCount);
            if (iqtyCount > 0) {
                if (iqtyCount <= iqtySplitNumber) {
                    //小于或等于拆分数,查询K3库存表全部数据
                    System.out.println("====小于或等于拆分数，直接查询处理后K3库存视图，无需拆分查询====");
                    try {
                        iqtyList = SqlHelperUtil.queryNewIqty("", "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //大于拆分数,拆分查询K3库存表数据，避免数据过大返回数据时报错
                    int size = iqtyCount / iqtySplitNumber + 1;//拆分查询
                    System.out.println("拆分组数===" + size);
                    for (int j = 0; j < size; j++) {
                        if (j == size - 1) {
                            //最后一组，存在查询的数量不足拆分数情况
                            String begin = String.valueOf(j * iqtySplitNumber + 1);
                            String end = String.valueOf(iqtyCount);
                            System.out.println("查询处理后K3库存视图第" + (j + 1) + "组begin==" + begin + "==end==" + end);
                            try {
                                iqtyList = SqlHelperUtil.queryNewIqty(begin, end);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (iqtyList != null && iqtyList.size() > 0) {
                                System.out.println("第" + (j + 1) + "组查询到的iqtyList==" + iqtyList.size());
                            }
                        } else {
                            //不是最后一组，不存在查询的数量不足splitNumber拆分数情况
                            String begin = String.valueOf(j * iqtySplitNumber + 1);
                            String end = String.valueOf((j + 1) * iqtySplitNumber);
                            System.out.println("查询处理后K3库存视图第" + (j + 1) + "组begin==" + begin + "==end==" + end);
                            try {
                                iqtyList = SqlHelperUtil.queryNewIqty(begin, end);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (iqtyList != null && iqtyList.size() > 0) {
                                System.out.println("第" + (j + 1) + "组查询到的iqtyList==" + iqtyList.size());
                            }
                        }

                    }
                }
            }

        }
        System.out.println("结束查询处理后K3库存视图====" + new Date());

        System.out.println("开始查询处理后K3在途视图====" + new Date());
        //查询区分组织在途 总数
        try {
            poqtyCountList = SqlHelperUtil.queryNewPoqtyCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (poqtyCountList != null && poqtyCountList.size() > 0) {
            int poqtyCount = Integer.parseInt(String.valueOf(poqtyCountList.get(0).get(0)));
            System.out.println("查询到的在途总数poqtyCount===" + poqtyCount);
            if (poqtyCount > 0) {
                if (poqtyCount <= poqtySplitNumber) {
                    //小于或等于拆分数,查询K3在途表全部数据
                    System.out.println("====小于或等于拆分数，直接查询处理后K3在途视图，无需拆分查询====");
                    try {
                        poqtyList = SqlHelperUtil.queryNewPoqty("", "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //大于拆分数,拆分查询K3在途表数据，避免数据过大返回数据时报错
                    int size = poqtyCount / poqtySplitNumber + 1;//拆分查询
                    System.out.println("拆分组数===" + size);
                    for (int j = 0; j < size; j++) {
                        if (j == size - 1) {
                            //最后一组，存在查询的数量不足拆分数情况
                            String begin = String.valueOf(j * poqtySplitNumber + 1);
                            String end = String.valueOf(poqtyCount);
                            System.out.println("查询处理后K3在途视图第" + (j + 1) + "组begin==" + begin + "==end==" + end);
                            try {
                                poqtyList = SqlHelperUtil.queryNewPoqty(begin, end);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (poqtyList != null && poqtyList.size() > 0) {
                                System.out.println("第" + (j + 1) + "组查询到的poqtyList==" + poqtyList.size());
                            }
                        } else {
                            //不是最后一组，不存在查询的数量不足splitNumber拆分数情况
                            String begin = String.valueOf(j * poqtySplitNumber + 1);
                            String end = String.valueOf((j + 1) * poqtySplitNumber);
                            System.out.println("查询处理后K3在途视图第" + (j + 1) + "组begin==" + begin + "==end==" + end);
                            try {
                                poqtyList = SqlHelperUtil.queryNewPoqty(begin, end);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (poqtyList != null && poqtyList.size() > 0) {
                                System.out.println("第" + (j + 1) + "组查询到的poqtyList==" + poqtyList.size());
                            }
                        }
                    }
                }
            }
        }
        System.out.println("结束查询处理后K3在途视图====" + new Date());

        System.out.println("开始查询处理后K3在制视图====" + new Date());
        //查询在制 总数
        try {
            fqtyCountList = SqlHelperUtil.queryNewFqtyCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (fqtyCountList != null && fqtyCountList.size() > 0) {
            int fqtyCount = Integer.parseInt(String.valueOf(fqtyCountList.get(0).get(0)));
            System.out.println("查询到的在制总数fqtyCount===" + fqtyCount);
            if (fqtyCount > 0) {
                if (fqtyCount <= fqtySplitNumber) {
                    //小于或等于拆分数,查询K3在制表全部数据
                    System.out.println("====小于或等于拆分数，直接查询处理后K3在途视图，无需拆分查询====");
                    try {
                        fqtyList = SqlHelperUtil.queryNewFqty("", "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //大于拆分数,拆分查询K3在制数据，避免数据过大返回数据时报错
                    int size = fqtyCount / fqtySplitNumber + 1;//拆分查询
                    System.out.println("拆分组数===" + size);
                    for (int j = 0; j < size; j++) {
                        if (j == size - 1) {
                            //最后一组，存在查询的数量不足拆分数情况
                            String begin = String.valueOf(j * fqtySplitNumber + 1);
                            String end = String.valueOf(fqtyCount);
                            System.out.println("查询处理后K3在在制视图第" + (j + 1) + "组begin==" + begin + "==end==" + end);
                            try {
                                fqtyList = SqlHelperUtil.queryNewFqty(begin, end);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (fqtyList != null && fqtyList.size() > 0) {
                                System.out.println("第" + (j + 1) + "组查询到的fqtyList==" + fqtyList.size());
                            }
                        } else {
                            //不是最后一组，不存在查询的数量不足splitNumber拆分数情况
                            String begin = String.valueOf(j * fqtySplitNumber + 1);
                            String end = String.valueOf((j + 1) * fqtySplitNumber);
                            System.out.println("查询处理后K3在制视图第" + (j + 1) + "组begin==" + begin + "==end==" + end);
                            try {
                                fqtyList = SqlHelperUtil.queryNewFqty(begin, end);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (fqtyList != null && fqtyList.size() > 0) {
                                System.out.println("第" + (j + 1) + "组查询到的fqtyList==" + fqtyList.size());
                            }
                        }
                    }
                }
            }
        }
        System.out.println("结束查询处理后K3在途视图====" + new Date());

        PreparedStatement pstmt = null;
        PreparedStatement pstmt2 = null;
        PreparedStatement pstmt3 = null;
        PreparedStatement pstmt4 = null;
//        PreparedStatement pstmt5 = null;

        //库存，在途，在制,物料价目表 其中有查询结果不为空则删除数据库表EXT_STOCK_TAB
        if ((iqtyList != null && iqtyList.size() > 0) || (poqtyList != null && poqtyList.size() > 0) || (fqtyList != null && fqtyList.size() > 0)) {
            //删除表内容
            System.out.println("开始删除插入前的EXT_STOCK_TAB表====" + new Date());
            String tsql = "truncate table EXT_STOCK_TAB";
            int rs = 1;
            try {
                conn = getConn();
                conn.setAutoCommit(false);//设置不自动提交

                pstmt = conn.prepareStatement(tsql);
                rs = pstmt.executeUpdate();
                conn.commit();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    conn.rollback();//回滚此次链接的所有操作
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            } finally {
                try {
                    if (pstmt!=null)
                    pstmt.close();
                    if (conn!=null)
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("删除插入前的EXT_STOCK_TAB表结果rs===" + rs);
            System.out.println("结束删除插入前的EXT_STOCK_TAB表====" + new Date());
            if (rs == 0) {
                //删除表内容成功，插入库存，在途，在制数据

                //插入库存
                String sql = "insert into EXT_STOCK_TAB (ID,WTPARTNUMBER,VERSIONINFO,KCNUM,CREATEDDATE,COMPANYID,COMPANY,CINVPERSON,CPURPERSON) values(EXT_STOCK_TAB_S.nextval,?,?,?,to_timestamp(?,'yyyy-MM-dd hh24:mi:ss.ff'),?,?,?,?)";


                System.out.println("开始循环处理后K3库存数据====" + new Date());
                if (iqtyList != null && iqtyList.size() > 0) {
                    try {
                        conn2 = getConn();
                        conn2.setAutoCommit(false);//设置不自动提交
                        pstmt2 = conn2.prepareStatement(sql);

                        for (int j = 0; j < iqtyList.size(); j++) {
                            String iqty = String.valueOf(iqtyList.get(j).get(0) == null ? "" : iqtyList.get(j).get(0));//库存
                            String itemnum = String.valueOf(iqtyList.get(j).get(1) == null ? "" : iqtyList.get(j).get(1));//编码
                            String itemver = String.valueOf(iqtyList.get(j).get(2) == null ? "" : iqtyList.get(j).get(2));//版本
                            String onum = String.valueOf(iqtyList.get(j).get(3) == null ? "" : iqtyList.get(j).get(3));//所属公司ID
                            String oname = String.valueOf(iqtyList.get(j).get(4) == null ? "" : iqtyList.get(j).get(4));//所属公司名称
                            String planername = String.valueOf(iqtyList.get(j).get(5) == null ? "" : iqtyList.get(j).get(5));//计划员名称
                            String purchasername = String.valueOf(iqtyList.get(j).get(6) == null ? "" : iqtyList.get(j).get(6));//采购员名称

                            String date = getCurrentDateStr();

                            pstmt2.setString(1, itemnum);//物料编码
                            pstmt2.setString(2, itemver);//大版本
                            pstmt2.setString(3, iqty);//库存
                            pstmt2.setString(4, date);//创建日期
                            pstmt2.setString(5, onum);//所属公司ID
                            pstmt2.setString(6, oname);//所属公司
                            pstmt2.setString(7, planername);//计划员名称
                            pstmt2.setString(8, purchasername);//采购员名称
                            pstmt2.addBatch();
                        }
                        System.out.println("结束循环K3数据====" + new Date());
                        System.out.println("开始库存数据插入EXT_STOCK_TAB表====" + new Date());
                        pstmt2.executeBatch();
                        System.out.println("结束库存数据插入EXT_STOCK_TAB表====" + new Date());
                        conn2.commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            conn2.rollback();//回滚此次链接的所有操作
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    } finally {
                        try {
                            if (pstmt2!=null)
                            pstmt2.close();
                            if (conn2!=null)
                            conn2.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
                System.out.println("开始处理后K3在途数据====" + new Date());
                //拼接在途
                if (poqtyList != null && poqtyList.size() > 0) {
                    //查询当前EXT_STOCK_TAB 在途在制库存表全部数据
                    List<QuantitativePriceBean> qpbList=queryAllStock();

                    for (int j = 0; j < poqtyList.size(); j++) {
                        //比较数据库 EXT_STOCK_TAB 是否存在同一物料编码，同一版本,同一所属公司 的数据，有则更新数据，没有则插入数据
                        if (qpbList != null && qpbList.size() > 0) {
                            try {
                                conn3 = getConn();
                                conn3.setAutoCommit(false);//设置不自动提交

                                String usql = "";
                                String poqty = String.valueOf(poqtyList.get(j).get(0) == null ? "" : poqtyList.get(j).get(0));//在途
                                String itemnum = String.valueOf(poqtyList.get(j).get(1) == null ? "" : poqtyList.get(j).get(1));//编码
                                String itemver = String.valueOf(poqtyList.get(j).get(2) == null ? "" : poqtyList.get(j).get(2));//版本
                                String onum = String.valueOf(poqtyList.get(j).get(3) == null ? "" : poqtyList.get(j).get(3));//所属公司ID
                                String oname = String.valueOf(poqtyList.get(j).get(4) == null ? "" : poqtyList.get(j).get(4));//所属公司名称
                                String planername = String.valueOf(poqtyList.get(j).get(5) == null ? "" : poqtyList.get(j).get(5));//计划员名称
                                String purchasername = String.valueOf(poqtyList.get(j).get(6) == null ? "" : poqtyList.get(j).get(6));//采购员名称

                                String date = getCurrentDateStr();
                                //默认数据库不存在同一组织该版本物料,执行插入在途操作
                                usql = "insert into EXT_STOCK_TAB (ID,WTPARTNUMBER,VERSIONINFO,ZTNUM,CREATEDDATE,COMPANYID,COMPANY,CINVPERSON,CPURPERSON) values(EXT_STOCK_TAB_S.nextval,'" + itemnum + "','" + itemver + "','" + poqty + "',to_timestamp('" + date + "','yyyy-MM-dd hh24:mi:ss.ff'),'" + onum + "','" + oname + "','" + planername + "','" + purchasername + "')";

                                for (int k = 0; k < qpbList.size(); k++) {
                                    QuantitativePriceBean qpBean = qpbList.get(k);
                                    String wtpartNumber = qpBean.getWtpartNumber();
                                    String versionInfo = qpBean.getVersionInfo();
                                    String companyId = qpBean.getCompanyId();
                                    if (itemnum.equals(wtpartNumber) && itemver.equals(versionInfo) && onum.equals(companyId)) {
                                        //数据库存在该版本物料,执行更新在途操作
                                        usql = "update EXT_STOCK_TAB set ZTNUM = '" + poqty + "',UPDATEDDATE= to_timestamp('" + date + "','yyyy-MM-dd hh24:mi:ss.ff') where WTPARTNUMBER='" + itemnum + "' and VERSIONINFO='" + itemver + "' and COMPANYID='" + onum + "'";
                                    }
                                }

                                pstmt3 = conn3.prepareStatement(usql);
                                pstmt3.executeUpdate();
                                conn3.commit();

                            } catch (Exception e) {
                                e.printStackTrace();
                                try {
                                    conn3.rollback();//回滚此次链接的所有操作
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            } finally {
                                try {
                                    if (pstmt3!=null)
                                    pstmt3.close();
                                    if (conn3!=null)
                                    conn3.close();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                System.out.println("结束处理后K3在途数据====" + new Date());
                System.out.println("开始处理后K3在制数据====" + new Date());
                //拼接在制(只有光峰有在制)
                if (fqtyList != null && fqtyList.size() > 0) {

                    //查询当前EXT_STOCK_TAB 在途在制库存表全部数据
                    List<QuantitativePriceBean> qpbList=queryAllStock();
                    String onum = "02";
                    String oname = "光峰";
                    //通过组织ID查询组织名称（1光峰，100146峰米）
                    List<List<Object>> OrgNameList = SqlHelperUtil.queryOrg("1");
                    if (OrgNameList != null && OrgNameList.size() > 0) {
                        oname = String.valueOf(OrgNameList.get(0).get(1) == null ? "" :OrgNameList.get(0).get(1));
                    }
                    for (int j = 0; j < fqtyList.size(); j++) {
                        //比较数据库 EXT_STOCK_TAB 是否存在同一物料编码，同一版本,同一所属公司 的数据，有则更新数据，没有则插入数据
                        if (qpbList != null && qpbList.size() > 0) {

                            try {
                                conn4 = getConn();
                                conn4.setAutoCommit(false);//设置不自动提交

                                String usql = "";
                                String fqty = String.valueOf(fqtyList.get(j).get(0) == null ? "" : fqtyList.get(j).get(0));//在制
                                String itemnum = String.valueOf(fqtyList.get(j).get(1) == null ? "" : fqtyList.get(j).get(1));//编码
                                String itemver = String.valueOf(fqtyList.get(j).get(2) == null ? "" : fqtyList.get(j).get(2));//版本
                                String planername = String.valueOf(fqtyList.get(j).get(3) == null ? "" : fqtyList.get(j).get(3));//计划员名称
                                String purchasername = String.valueOf(fqtyList.get(j).get(4) == null ? "" : fqtyList.get(j).get(4));//采购员名称

                                String date = getCurrentDateStr();
                                //默认数据库不存在同一组织（光峰）该版本物料,执行插入在制操作
                                usql = "insert into EXT_STOCK_TAB (ID,WTPARTNUMBER,VERSIONINFO,ZZNUM,CREATEDDATE,COMPANYID,COMPANY,CINVPERSON,CPURPERSON) values(EXT_STOCK_TAB_S.nextval,'" + itemnum + "','" + itemver + "','" + fqty + "',to_timestamp('" + date + "','yyyy-MM-dd hh24:mi:ss.ff'),'" + onum + "','" + oname + "','" + planername + "','" + purchasername + "')";

                                for (int k = 0; k < qpbList.size(); k++) {
                                    QuantitativePriceBean qpBean = qpbList.get(k);
                                    String wtpartNumber = qpBean.getWtpartNumber();
                                    String versionInfo = qpBean.getVersionInfo();
                                    String companyId = qpBean.getCompanyId();
                                    if (itemnum.equals(wtpartNumber) && itemver.equals(versionInfo) && onum.equals(companyId)) {
                                        //数据库（光峰）存在该版本物料,执行更新在制操作
                                        usql = "update EXT_STOCK_TAB set ZZNUM = '" + fqty + "',UPDATEDDATE= to_timestamp('" + date + "','yyyy-MM-dd hh24:mi:ss.ff') where WTPARTNUMBER='" + itemnum + "' and VERSIONINFO='" + itemver + "' and COMPANYID='"+onum+"'";
                                    }
                                }

                                pstmt4 = conn4.prepareStatement(usql);
                                pstmt4.executeUpdate();
                                conn4.commit();

                            } catch (Exception e) {
                                e.printStackTrace();
                                try {
                                    conn4.rollback();//回滚此次链接的所有操作
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            } finally {
                                try {
                                    if (pstmt4!=null)
                                    pstmt4.close();
                                    if (conn4!=null)
                                    conn4.close();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                }
                System.out.println("结束处理后K3在制数据====" + new Date());

                i = 1;
            }
        }
        System.out.println("返回的i==" + i);
        System.out.println("结束执行插入EXT_STOCK_TAB表方法=====" + new Date());
        return i;
    }


    //将K3价目表信息插入数据库
    public static  Integer insertEXT_INVENTORYPRICE_TAB() {
        System.out.println("开始执行插入insertEXT_INVENTORYPRICE_TAB表方法=====" + new Date());
        int i=0;//插入数据库是否成功，1成功
        Connection conn = null;
        Connection conn2 = null;

        int splitNumber = 30000;//价目表拆分数
        List<List<Object>> countList = new ArrayList<>();
        List<List<Object>> list = new ArrayList<>();

        System.out.println("开始查询K3价目表====" + new Date());
        //查询价目表 总数
        try {
            countList = SqlHelperUtil.queryNewPriceCategoryCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (countList != null && countList.size() > 0) {
            int count = Integer.parseInt(String.valueOf(countList.get(0).get(0)));
            System.out.println("查询到的价目表总数count===" + count);
            if (count > 0) {
                if (count <= splitNumber) {
                    //小于或等于拆分数,查询K3在途表全部数据
                    System.out.println("====小于或等于拆分数，直接查询K3价目表，无需拆分查询====");
                    try {
                        list = SqlHelperUtil.queryNewPriceCategory("", "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //大于拆分数,拆分查询K3在途表数据，避免数据过大返回数据时报错
                    int size = count / splitNumber + 1;//拆分查询
                    System.out.println("拆分组数===" + size);
                    for (int j = 0; j < size; j++) {
                        if (j == size - 1) {
                            //最后一组，存在查询的数量不足拆分数情况
                            String begin = String.valueOf(j * splitNumber + 1);
                            String end = String.valueOf(count);
                            System.out.println("查询查询K3价目表第" + (j + 1) + "组begin==" + begin + "==end==" + end);
                            try {
                                list = SqlHelperUtil.queryNewPriceCategory(begin, end);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (list != null && list.size() > 0) {
                                System.out.println("第" + (j + 1) + "组查询到的list==" + list.size());
                            }
                        } else {
                            //不是最后一组，不存在查询的数量不足splitNumber拆分数情况
                            String begin = String.valueOf(j * splitNumber + 1);
                            String end = String.valueOf((j + 1) * splitNumber);
                            System.out.println("查询查询K3价目表视图第" + (j + 1) + "组begin==" + begin + "==end==" + end);
                            try {
                                list = SqlHelperUtil.queryNewPriceCategory(begin, end);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (list != null && list.size() > 0) {
                                System.out.println("第" + (j + 1) + "组查询到的list==" + list.size());
                            }
                        }
                    }
                }
            }
        }
        System.out.println("结束查询K3价目表====" + new Date());

        PreparedStatement pstmt = null;
        PreparedStatement pstmt2 = null;
        //库存，在途，在制,物料价目表 其中有查询结果不为空则删除数据库表EXT_STOCK_TAB
        if (list != null && list.size() > 0) {
            //删除表内容
            System.out.println("开始删除插入前的EXT_INVENTORYPRICE_TAB表====" + new Date());
            String tsql = "truncate table EXT_INVENTORYPRICE_TAB";
            int rs = 1;
            try {
                conn2 = getConn();
                conn2.setAutoCommit(false);//设置不自动提交

                pstmt2 = conn2.prepareStatement(tsql);
                rs = pstmt2.executeUpdate();
                conn2.commit();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    conn2.rollback();//回滚此次链接的所有操作
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            } finally {
                try {
                    if (pstmt2 != null)
                        pstmt2.close();
                    if (conn2 != null)
                        conn2.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("删除插入前的EXT_INVENTORYPRICE_TAB表结果rs===" + rs);
            System.out.println("结束删除插入前的EXT_INVENTORYPRICE_TAB表====" + new Date());
            if (rs == 0) {
                try {
                    //删除表内容成功，插入价目表信息
                    String sql = "insert into EXT_INVENTORYPRICE_TAB (ID,WTPARTNUMBER,VERSIONINFO,AVERAGECOST,SUPPLYCYCLE,MOQ,MPQ,CINVPERSON,CPURPERSON,DRELEASEDATE,CREATEDDATE,COMPANYID,COMPANY,FCURRENCYNANE) values(EXT_INVENTORYPRICE_TAB_S.nextval,?,?,?,?,?,?,?,?,to_date(?,'yyyy-mm-dd hh24:mi:ss'),to_timestamp(?,'yyyy-MM-dd hh24:mi:ss.ff'),?,?,?)";

                    conn = getConn();
                    conn.setAutoCommit(false);//设置不自动提交
                    pstmt = conn.prepareStatement(sql);

                    for (int j = 0; j < list.size(); j++) {

//                    String  orgId = String.valueOf(list.get(j).get(0) == null ? "" : list.get(j).get(0));//采购组织ID
                        String dreleaseDate = String.valueOf(list.get(j).get(1) == null ? "" : list.get(j).get(1));//审核日期
                        String itemver = String.valueOf(list.get(j).get(2) == null ? "" : list.get(j).get(2));//版本
                        if (itemver.equals("empty")){
                            //拼接sql需要先给没版本赋值 empty，插入数据库时去除
                            itemver="";
                        }
                        String moq = String.valueOf(list.get(j).get(3) == null ? "" : list.get(j).get(3));//最小订货量
                        String mpq = String.valueOf(list.get(j).get(4) == null ? "" : list.get(j).get(4));//最小包装量
                        String supplycycle = String.valueOf(list.get(j).get(5) == null ? "" : list.get(j).get(5));//固定提前期
                        String averagecost = String.valueOf(list.get(j).get(6) == null ? "" : list.get(j).get(6));//含税单价
                        String itemnum = String.valueOf(list.get(j).get(7) == null ? "" : list.get(j).get(7));//物料编码
                        String  onum= String.valueOf(list.get(j).get(8) == null ? "" : list.get(j).get(8));//组织编码(02光峰，20 峰米...)
                        String  oname= String.valueOf(list.get(j).get(9) == null ? "" : list.get(j).get(9));//组织名称
                        String planername = String.valueOf(list.get(j).get(10) == null ? "" : list.get(j).get(10));//计划员名称
                        String purchasername = String.valueOf(list.get(j).get(11) == null ? "" : list.get(j).get(11));//采购员名称
                        String fcurrencynane = String.valueOf(list.get(j).get(12) == null ? "" : list.get(j).get(12));//币别名称

                        String date = getCurrentDateStr();

                        pstmt.setString(1, itemnum);//编码
                        pstmt.setString(2, itemver);//版本
                        pstmt.setString(3, averagecost);//价格
                        pstmt.setString(4, supplycycle);//供货周期
                        pstmt.setString(5, moq);//最小订货量
                        pstmt.setString(6, mpq);//最小包装数
                        pstmt.setString(7, planername);//计划员名称
                        pstmt.setString(8, purchasername);//采购员名称
                        pstmt.setString(9, dreleaseDate);//审核日期
                        pstmt.setString(10, date);//创建日期
                        pstmt.setString(11, onum);//组织编码
                        pstmt.setString(12, oname);//组织名称
                        pstmt.setString(13, fcurrencynane);//币别名称
                        pstmt.addBatch();
                    }
                    System.out.println("开始价目表数据插入EXT_INVENTORYPRICE_TAB表====" + new Date());
                    pstmt.executeBatch();
                    System.out.println("结束价目表数据插入EXT_INVENTORYPRICE_TAB表====" + new Date());
                    conn.commit();

                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        conn.rollback();//回滚此次链接的所有操作
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                } finally {
                    try {
                        pstmt.close();
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        i=1;
        System.out.println("返回的i==" + i);

        System.out.println("结束执行插入insertEXT_INVENTORYPRICE_TAB表方法=====" + new Date());
        return i;
    }

    //查询数据库表 EXT_STOCK_TAB 所有数据（区分组织）
    //WTPARTNUMBER 物料编码,VERSIONINFO 大版本,ZTNUM 在途数量,ZZNUM 在制数量,KCNUM 库存数量,COMPANYID 所属公司ID,CINVPERSON 计划员名称,CPURPERSON 采购员名称
    public static List<QuantitativePriceBean> queryAllStock() throws Exception {
        PreparedStatement pstmt = null;
        List resultList = new ArrayList();
        Connection conn = null;
        try {
            conn = getConn();
            String sql = "select WTPARTNUMBER,VERSIONINFO,ZZNUM,ZTNUM,KCNUM,COMPANYID,COMPANY,CINVPERSON,CPURPERSON from EXT_STOCK_TAB";

//            System.out.println("sql========================="+sql);
            pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
//            System.out.println("rs========="+rs);
            while (rs.next()) {
                QuantitativePriceBean qBean = new QuantitativePriceBean();
                qBean.setWtpartNumber(rs.getString("WTPARTNUMBER"));//编号
                qBean.setVersionInfo(rs.getString("VERSIONINFO"));//版本
                qBean.setZtnum(rs.getString("ZTNUM"));//在途
                qBean.setZznum(rs.getString("ZZNUM"));//在制
                qBean.setKcnum(rs.getString("KCNUM"));//库存
                qBean.setCompanyId(rs.getString("COMPANYID"));//所属公司ID
                qBean.setCompany(rs.getString("COMPANY"));//所属公司名称
                qBean.setCinvPerson(rs.getString("CINVPERSON"));//计划员名称
                qBean.setCpurPerson(rs.getString("CPURPERSON"));//采购员名称
//                List itemList=new ArrayList();
//                itemList.add(rs.getString("WTPARTNUMBER"));//编号
//                itemList.add(rs.getString("VERSIONINFO"));//版本
//                itemList.add(rs.getString("ZTNUM"));//在途
//                itemList.add(rs.getString("ZZNUM"));//在制
//                itemList.add(rs.getString("KCNUM"));//库存

                resultList.add(qBean);
            }
            rs.close();
//                conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                conn.rollback();//回滚此次链接的所有操作
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                pstmt.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return resultList;
    }


    //查询数据库表 EXT_STOCK_TAB 数据（不区分组织，合并同一版本物料下所有组织在途在制库存，取光峰物料上计划员、采购员）
    //WTPARTNUMBER 物料编码,VERSIONINFO 大版本,ZTNUM 在途数量,ZZNUM 在制数量,KCNUM 库存数量
    public static List<QuantitativePriceBean> queryAll() throws Exception {
        PreparedStatement pstmt = null;
        List resultList = new ArrayList();
        Connection conn = null;
        try {
            conn = getConn();
            String sql = "SELECT a.WTPARTNUMBER, a.VERSIONINFO, a.ZZNUM, a.ZTNUM, a.KCNUM, b.CINVPERSON, b.CPURPERSON\n" +
                    "FROM (\n" +
                    "\tSELECT WTPARTNUMBER, VERSIONINFO, SUM(ZZNUM) AS ZZNUM, SUM(ZTNUM) AS ZTNUM, SUM(KCNUM) AS KCNUM\n" +
                    "\tFROM EXT_STOCK_TAB GROUP BY WTPARTNUMBER, VERSIONINFO\n" +
                    ") a\n" +
                    "LEFT JOIN (\n" +
                    "\tSELECT WTPARTNUMBER, MIN(CINVPERSON) AS CINVPERSON, MIN(CPURPERSON) AS CPURPERSON\n" +
                    "\tFROM (\n" +
                    "\t\tSELECT * FROM EXT_STOCK_TAB WHERE COMPANYID = '02'\n" +
                    "\t) a\n" +
                    "\tGROUP BY WTPARTNUMBER\n" +
                    ") b\n" +
                    "ON a.WTPARTNUMBER = b.WTPARTNUMBER";

//            System.out.println("sql========================="+sql);
            pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
//            System.out.println("rs========="+rs);
            while (rs.next()) {
                QuantitativePriceBean qBean = new QuantitativePriceBean();
                qBean.setWtpartNumber(rs.getString("WTPARTNUMBER"));//编号
                qBean.setVersionInfo(rs.getString("VERSIONINFO"));//版本
                qBean.setZtnum(rs.getString("ZTNUM"));//在途
                qBean.setZznum(rs.getString("ZZNUM"));//在制
                qBean.setKcnum(rs.getString("KCNUM"));//库存
                qBean.setCinvPerson(rs.getString("CINVPERSON"));//计划员名称
                qBean.setCpurPerson(rs.getString("CPURPERSON"));//采购员名称
//                List itemList=new ArrayList();
//                itemList.add(rs.getString("WTPARTNUMBER"));//编号
//                itemList.add(rs.getString("VERSIONINFO"));//版本
//                itemList.add(rs.getString("ZTNUM"));//在途
//                itemList.add(rs.getString("ZZNUM"));//在制
//                itemList.add(rs.getString("KCNUM"));//库存

                resultList.add(qBean);
            }
            rs.close();
//                conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                conn.rollback();//回滚此次链接的所有操作
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                pstmt.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return resultList;
    }

    //通过物料和版本查询数据库表 EXT_STOCK_TAB获取该版本物料在制和，在途和，库存和
    //WTPARTNUMBER 物料编码,VERSIONINFO 大版本,ZZNUM 在制数量,KCNUM 库存数量,ZTNUM 在途数量
    public static List queryAmount (String number, String mversion) throws IOException, WTException {
        PreparedStatement pstmt = null;
        List resultList = new ArrayList();
        Connection conn = null;
        try {
            conn = getConn();
            conn.setAutoCommit(false);//设置不自动提交
            String sql = "select sum(ZZNUM) AS ZZNUM,sum(ZTNUM) AS ZTNUM,sum(KCNUM) AS KCNUM from EXT_STOCK_TAB where WTPARTNUMBER='" + number + "' and VERSIONINFO='" + mversion + "'";

//            System.out.println("sql========================="+sql);
            pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
//            System.out.println("rs========="+rs);
            while (rs.next()) {
                resultList.add(rs.getString("ZZNUM"));
                resultList.add(rs.getString("KCNUM"));
                resultList.add(rs.getString("ZTNUM"));
            }
            rs.close();
//                pstmt.close();
//                conn.close();

            //add by lzy at 20200303 start
            //查询不到数据，查询版本为null的数据
            if(resultList.isEmpty()){
                PreparedStatement pstmt2 = null;
                Connection conn2 = null;
                try{
                    conn2 = getConn();
                    conn2.setAutoCommit(false);//设置不自动提交
                    String sql2 = "select sum(ZZNUM) AS ZZNUM,sum(ZTNUM) AS ZTNUM,sum(KCNUM) AS KCNUM from EXT_STOCK_TAB where WTPARTNUMBER='" + number + "' and VERSIONINFO is null";
                    pstmt2 = conn2.prepareStatement(sql2);
                    ResultSet rs2 = pstmt2.executeQuery();
                    while (rs2.next()) {
                        resultList.add(rs2.getString("ZZNUM"));
                        resultList.add(rs2.getString("KCNUM"));
                        resultList.add(rs2.getString("ZTNUM"));
                    }
                    rs2.close();
                }catch (Exception e){
                    e.printStackTrace();
                    try {
                        conn.rollback();//回滚此次链接的所有操作
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                }finally {
                    pstmt2.close();
                    conn2.close();
                }
            }
            //add by lzy at 20200303 end

        } catch (Exception e) {
            e.printStackTrace();
            try {
                conn.rollback();//回滚此次链接的所有操作
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                pstmt.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return resultList;
    }


    //通过物料查询数据库表 EXT_STOCK_TAB(在制在途库存表)，EXT_INVENTORYPRICE_TAB（价目表）
    //WTPARTNUMBER 物料编码,VERSIONINFO 大版本,ZZNUM 在制数量,KCNUM 库存数量,ZTNUM 在途数量
    //编码,版本,价格,供货周期,最小订货量,最小包装数,最小包装数,计划员名称，采购员名称，审核日期，公司名称
    public static List<InventoryPrice> queryInventoryPrice(String number) throws IOException, WTException {
        PreparedStatement pstmt = null;
        List resultList = new ArrayList();
        Connection conn = null;
        try {
            conn = getConn();
            conn.setAutoCommit(false);//设置不自动提交
            String sql = "SELECT nvl(a.WTPARTNUMBER, b.WTPARTNUMBER) AS WTPARTNUMBER, nvl(a.VERSIONINFO, b.VERSIONINFO) AS VERSIONINFO, a.AVERAGECOST\n" +
                    "\t, a.SUPPLYCYCLE, a.MOQ, a.MPQ, nvl(a.CINVPERSON, b.CINVPERSON) AS CINVPERSON, nvl(a.CPURPERSON, b.CPURPERSON) AS CPURPERSON, \n" +
                    "\ta.DRELEASEDATE, nvl(a.COMPANY, b.COMPANY) AS COMPANY,a.FCURRENCYNANE,b.ZZNUM, b.ZTNUM, b.KCNUM\n" +
                    "FROM (\n" +
                    "\tSELECT *\n" +
                    "\tFROM (\n" +
                    "\t\tSELECT *\n" +
                    "\t\tFROM (\n" +
                    "\t\t\tSELECT T.*, ROW_NUMBER() OVER (PARTITION BY T.WTPARTNUMBER, T.VERSIONINFO,T.COMPANYID ORDER BY T.DRELEASEDATE DESC) AS FLAG\n" +
                    "\t\t\tFROM EXT_INVENTORYPRICE_TAB T\n" +
                    "\t\t) TMP\n" +
                    "\t\tWHERE TMP.FLAG = 1\n" +
                    "\t)\n" +
                    "\tWHERE WTPARTNUMBER = '"+number+"'\n" +
                    "\t\tAND VERSIONINFO IS NOT NULL\n" +
                    ") a\n" +
                    "\tFULL JOIN (\n" +
                    "\t\tSELECT *\n" +
                    "\t\tFROM EXT_STOCK_TAB\n" +
                    "\t\tWHERE WTPARTNUMBER = '"+number+"'\n" +
                    "\t\t\tAND VERSIONINFO IS NOT NULL\n" +
                    "\t) b\n" +
                    "\tON a.WTPARTNUMBER = b.WTPARTNUMBER\n" +
                    "\t\tAND a.VERSIONINFO = b.VERSIONINFO\n" +
                    "\t\tAND a.COMPANYID = b.COMPANYID";

//            System.out.println("sql========================="+sql);
            pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
//            System.out.println("rs========="+rs);
            while (rs.next()) {
                //去除多余的0和.
                String price=rs.getString("AVERAGECOST")==null?"0":rs.getString("AVERAGECOST");
                price=subZeroAndDot(price.isEmpty()?"0":price.trim());

                InventoryPrice inventoryPrice=new InventoryPrice();
                inventoryPrice.setItem_id(rs.getString("WTPARTNUMBER"));//物料编码
                if (price==null||price=="0"){
                    inventoryPrice.setiAveragecost("0");
                }else{
                    if (rs.getString("FCURRENCYNANE")!=null){
                        inventoryPrice.setiAveragecost(price+rs.getString("FCURRENCYNANE"));//物料成本
                    }else{
                        inventoryPrice.setiAveragecost(price);//物料成本
                    }

                }
//                        inventoryPrice.setiAveragecost(price+rs.getString("FCURRENCYNANE"));//物料成本
                inventoryPrice.setiSupplycycle(rs.getString("SUPPLYCYCLE"));//供货周期
                inventoryPrice.setiMoq(rs.getString("MOQ"));//最小订单量
                inventoryPrice.setiMpq(rs.getString("MPQ"));//最小包装数量
                inventoryPrice.setCpurPerson(rs.getString("CPURPERSON"));//采购员
                inventoryPrice.setCinvPerson(rs.getString("CINVPERSON"));//计划员
                inventoryPrice.setiQuantity(rs.getString("KCNUM"));//库存数量
                inventoryPrice.setfTransinquantity(rs.getString("ZTNUM"));//在途
                inventoryPrice.setfInquantity(rs.getString("ZZNUM"));//在制
                inventoryPrice.setDreleaseDate(rs.getString("DRELEASEDATE"));//ERP更新时间
                inventoryPrice.setmVersion(rs.getString("VERSIONINFO"));//版本
                inventoryPrice.setCompany(rs.getString("COMPANY"));//所属公司

                resultList.add(inventoryPrice);
            }
            rs.close();
//                pstmt.close();
//                conn.close();

            //add by lzy at 20200304 start
            //查询不到数据，查询版本为null的数据
            if(resultList.isEmpty()){
                PreparedStatement pstmt2 = null;
                Connection conn2 = null;
                try{
                    conn2 = getConn();
                    conn2.setAutoCommit(false);//设置不自动提交
                    String sql2 = "SELECT nvl(a.WTPARTNUMBER, b.WTPARTNUMBER) AS WTPARTNUMBER, nvl(a.VERSIONINFO, b.VERSIONINFO) AS VERSIONINFO, a.AVERAGECOST \n" +
                            ", a.SUPPLYCYCLE, a.MOQ, a.MPQ, nvl(a.CINVPERSON, b.CINVPERSON) AS CINVPERSON, nvl(a.CPURPERSON, b.CPURPERSON) AS CPURPERSON,  \n" +
                            "a.DRELEASEDATE, nvl(a.COMPANY, b.COMPANY) AS COMPANY,a.FCURRENCYNANE,b.ZZNUM, b.ZTNUM, b.KCNUM \n" +
                            "FROM ( \n" +
                            "SELECT * \n" +
                            "FROM ( \n" +
                            "SELECT * \n" +
                            "FROM ( \n" +
                            "SELECT T.*, ROW_NUMBER() OVER (PARTITION BY T.WTPARTNUMBER, T.VERSIONINFO,T.COMPANYID ORDER BY T.DRELEASEDATE DESC) AS FLAG \n" +
                            "FROM EXT_INVENTORYPRICE_TAB T \n" +
                            ") TMP \n" +
                            "WHERE TMP.FLAG = 1 \n" +
                            ") \n" +
                            "WHERE WTPARTNUMBER = '"+number+"' \n" +
                            "AND VERSIONINFO IS NULL \n" +
                            ") a \n" +
                            "FULL JOIN ( \n" +
                            "SELECT * \n" +
                            "FROM EXT_STOCK_TAB \n" +
                            "WHERE WTPARTNUMBER = '"+number+"' \n" +
                            "AND VERSIONINFO IS NULL \n" +
                            ") b \n" +
                            "ON a.WTPARTNUMBER = b.WTPARTNUMBER  \n" +
                            "AND a.COMPANYID = b.COMPANYID";
                    pstmt2 = conn2.prepareStatement(sql2);
                    ResultSet rs2 = pstmt2.executeQuery();
                    while (rs2.next()) {
                        //去除多余的0和.
                        String price=rs2.getString("AVERAGECOST")==null?"0":rs2.getString("AVERAGECOST");
                        price=subZeroAndDot(price.isEmpty()?"0":price.trim());

                        InventoryPrice inventoryPrice=new InventoryPrice();
                        inventoryPrice.setItem_id(rs2.getString("WTPARTNUMBER"));//物料编码
                        if (price==null||price=="0"){
                            inventoryPrice.setiAveragecost("0");
                        }else{
                            if (rs2.getString("FCURRENCYNANE")!=null){
                                inventoryPrice.setiAveragecost(price+rs2.getString("FCURRENCYNANE"));//物料成本
                            }else{
                                inventoryPrice.setiAveragecost(price);//物料成本
                            }

                        }
//                        inventoryPrice.setiAveragecost(price+rs2.getString("FCURRENCYNANE"));//物料成本
                        inventoryPrice.setiSupplycycle(rs2.getString("SUPPLYCYCLE"));//供货周期
                        inventoryPrice.setiMoq(rs2.getString("MOQ"));//最小订单量
                        inventoryPrice.setiMpq(rs2.getString("MPQ"));//最小包装数量
                        inventoryPrice.setCpurPerson(rs2.getString("CPURPERSON"));//采购员
                        inventoryPrice.setCinvPerson(rs2.getString("CINVPERSON"));//计划员
                        inventoryPrice.setiQuantity(rs2.getString("KCNUM"));//库存数量
                        inventoryPrice.setfTransinquantity(rs2.getString("ZTNUM"));//在途
                        inventoryPrice.setfInquantity(rs2.getString("ZZNUM"));//在制
                        inventoryPrice.setDreleaseDate(rs2.getString("DRELEASEDATE"));//ERP更新时间
                        inventoryPrice.setmVersion(rs2.getString("VERSIONINFO"));//版本
                        inventoryPrice.setCompany(rs2.getString("COMPANY"));//所属公司

                        resultList.add(inventoryPrice);
                    }
                    rs2.close();
                }catch (Exception e){
                    e.printStackTrace();
                    try {
                        conn.rollback();//回滚此次链接的所有操作
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                }finally {
                    pstmt2.close();
                    conn2.close();
                }
            }
            //add by lzy at 20200304 end

        } catch (Exception e) {
            e.printStackTrace();
            try {
                conn.rollback();//回滚此次链接的所有操作
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                pstmt.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return resultList;
    }



    /**
     * 建立jdbc链接
     * @return
     * @throws IOException
     */
    public static Connection getConn() throws Exception {

        Map<String,String> map = getSendDocDataBeans();
        String driver = "oracle.jdbc.driver.OracleDriver";
        String[] arrtPort = map.get("dataPort").split("\\.");
        String url = "jdbc:oracle:thin:@"+map.get("dataAddress")+":"+arrtPort[0]+":"+map.get("dataStorageRoom");
//        System.out.println("url================================"+url);
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

    // 获取当前日期和时间
    public static String getCurrentDateStr() {
         Date date = new Date();
         String str = null;
         SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSS");
         str = df.format(date);
         return str;
    }

    /**
     * 使用java正则表达式去掉多余的.与0
     * @param str
     * @return
     */
    public static String subZeroAndDot(String str){
        if(str.indexOf(".") > 0){
            str = str.replaceAll("0+?$", "");//去掉多余的0
            str = str.replaceAll("[.]$", "");//如最后一位是.则去掉
        }
        return str;
    }

}
