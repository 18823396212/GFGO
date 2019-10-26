/**
 * Copyright (c) 2017-2030 上海湃睿信息科技有限公司 (PISX). All Rights Reserved.
 * <p>
 * This software is the confidential and proprietary information of PISX
 * and is subject to the terms of a software license agreement. You shall
 * not disclose such confidential information and shall use it only in accordance
 * with the terms of the license agreement.
 */
package ext.generic.integration.erp.ibatis;

import com.ibatis.sqlmap.client.SqlMapClient;
import ext.generic.integration.erp.bean.InventoryPrice;
import ext.generic.integration.erp.util.IbatisUtil;
import org.apache.log4j.Logger;
import wt.log4j.LogR;
import wt.org.WTUser;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventoryPriceIbatis {
    private static final String clazz = InventoryPriceIbatis.class.getName();
    private static final Logger logger = LogR.getLogger(clazz);

    /**
     * 根据物料编码从库存价格表中查询相关的信息
     * 需要查询的物料编码信息
     * @param
     * @return 满足条件的库存价格信息
     * @throws SQLException
     */

    public static List queryInventoryPrice(String partNumber) throws SQLException {
        logger.debug("   " + "Entering In queryInventoryPrice partNumber is: " + partNumber);
        List list = new ArrayList();
        SqlMapClient sqlMap = null;
        sqlMap = IbatisUtil.getSqlMapInstance();
        InventoryPrice inventoryPrice = new InventoryPrice();
        inventoryPrice.setItem_id(partNumber);
        //执行任务
        try {
            list = sqlMap.queryForList(IbatisSqlConstant.QUERY_INVENTORY_PRICE, inventoryPrice);
        } finally {
            IbatisUtil.closeSqlMapInstance();
        }
        //获取返回值
        logger.debug("   " + "Existed Out queryInventoryPrice......");

        return list;
    }

    /**
     * 根据物料编码从库存价格表中查询相关的信息
     * 需要查询的物料编码信息
     * @param
     * @return 满足条件的库存价格信息
     * @throws SQLException
     */
    public static List queryInventoryPriceWithCompany(String partNumber, String company) throws SQLException {
        logger.debug("   " + "Entering In queryInventoryPrice partNumber is: " + partNumber);
        List list = new ArrayList();
        SqlMapClient sqlMap = null;
        sqlMap = IbatisUtil.getSqlMapInstance();
        InventoryPrice inventoryPrice = new InventoryPrice();
        inventoryPrice.setItem_id(partNumber);
        inventoryPrice.setCompany(company);
        //执行任务
        try {
            list = sqlMap.queryForList(IbatisSqlConstant.QUERY_INVENTORY_PRICE_WITH_COMPANY, inventoryPrice);
        } finally {
            IbatisUtil.closeSqlMapInstance();
        }
        //获取返回值
        logger.debug("   " + "Existed Out queryInventoryPrice......");

        return list;
    }

    /**
     * 根据物料编码和大版本从库存价格表中查询相关的信息
     * 需要查询的物料编码信息
     * @param
     * @return 满足条件的库存价格信息
     * @throws SQLException
     */
    public static List queryInventoryPrice(String partNumber, String mVersion, WTUser user) throws SQLException {
        logger.debug("   " + "Entering In queryInventoryPrice partNumber is: " + partNumber);

        String fullName = user.getFullName();
        String company = "";
        if (fullName.contains("_")) {
            String[] split = fullName.split("_");
            company = split[1];
        } else if (fullName.contains("|")) {
            String[] split = fullName.split("|");
            company = split[1];
        }

        List list;
        SqlMapClient sqlMap;
        sqlMap = IbatisUtil.getSqlMapInstance();
        InventoryPrice inventoryPrice = new InventoryPrice();
        inventoryPrice.setItem_id(partNumber);
        inventoryPrice.setmVersion(mVersion);
        inventoryPrice.setCompany(company);
        //执行任务
        try {
            list = sqlMap.queryForList(IbatisSqlConstant.QUERY_INVENTORY_PRICE_VERION, inventoryPrice);
        } finally {
            IbatisUtil.closeSqlMapInstance();
        }
        //获取返回值
        logger.debug("   " + "Existed Out queryInventoryPrice......");

        return list;
    }
}
