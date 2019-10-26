package ext.generic.integration.cis.util;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class SQLServerUtil {

	// 定义全局变量
	private static ComboPooledDataSource cpds;
	// 静态代码块
	static {
		cpds = new ComboPooledDataSource();
		try {
			cpds.setDriverClass(SQLInfoUtil.driverClass);
			cpds.setJdbcUrl(SQLInfoUtil.jdbcUrl);
			cpds.setUser(SQLInfoUtil.user);
			cpds.setPassword(SQLInfoUtil.password);
			System.out.println(" >>>>>user :"+cpds.getUser()+"  >>>>password:"+cpds.getPassword());
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
	}

	// 获得数据源
	public static DataSource getDataSource() {
		return cpds;
	}

	// 获得连接
	public static Connection getConnection() throws SQLException {
		return cpds.getConnection();
	}

	/**
	 * 释放连接
	 * @param conn
	 * @param pstmt
	 * @param rs  
	 * @Description:
	 */
    public static void release(Connection conn,PreparedStatement pstmt,ResultSet rs) {
        if(rs!=null) {
            try {
                rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            rs = null;
        }
        if(pstmt!=null){
            try {
                pstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            pstmt = null;
        }

        if(conn!=null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            conn = null;
        }
    }
}
