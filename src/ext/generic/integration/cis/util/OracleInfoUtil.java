package ext.generic.integration.cis.util;

import java.io.FileInputStream;
import java.util.Properties;

public class OracleInfoUtil {

	public static String tomcatClassPath = OracleInfoUtil.class.getClassLoader().getResource("").getPath().replaceAll("%20", " ");
	public static String filePath = tomcatClassPath + "ext/generic/integration/cis/config";

	static Properties p;
	public static String user = null;
	public static String password = null;
	public static String driverClass = null;
	public static String jdbcUrl = null;
	static{
		p= new Properties();
		try {
			p.load(new FileInputStream(filePath+"/Oracle.properties"));
			user = p.getProperty("user");
			password = p.getProperty("password");
			driverClass =p.getProperty("driverClass");
			jdbcUrl = p.getProperty("jdbcUrl");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
