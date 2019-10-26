/**
 *
 * Copyright (c) 2017-2030 上海湃睿信息科技有限公司 (PISX). All Rights Reserved.
 *
 * This software is the confidential and proprietary information of PISX
 * and is subject to the terms of a software license agreement. You shall
 * not disclose such confidential information and shall use it only in accordance
 * with the terms of the license agreement.
 *
 */
package ext.generic.integration.erp.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

public class IbatisUtil {
	private static final Logger LOGGER = Logger.getLogger(IbatisUtil.class);
	private static final String CLASS_NAME = IbatisUtil.class.getName();
	private static final String RESOURCE = "/ext/generic/integration/erp/config/sqlMapConfig.xml";
	private static SqlMapClient sqlMap;
	private static InputStream inputStream;

	static {
		try {
			inputStream = IbatisUtil.class.getResourceAsStream(RESOURCE);
			sqlMap = SqlMapClientBuilder.buildSqlMapClient(inputStream);
		} catch (Exception e) {
			LOGGER.error(CLASS_NAME + "ERROR:", e);
			throw new RuntimeException("Error initialing IbatisUtil", e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					LOGGER.error(CLASS_NAME + "ERROR:", e);
				}
			}
		}
	}

	public static SqlMapClient getSqlMapInstance() {
		return sqlMap;
	}

	public static void closeSqlMapInstance() {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error(CLASS_NAME + "ERROR:", e);
			}
		}
	}

	public static void main(String args[]) {
		System.out.println(getSqlMapInstance());
	}
}
