package ext.appo.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author HYJ&NJH
 * 读取Properties文件。
 */
public class AppoPropertiesUtil {

	// 配置文件类
	private Properties pro = null; // 创建文件
	private FileInputStream fis = null; // 写入

	/**
	 * 加载Properties文件
	 * @param file
	 * @return
	 */
	public Properties load(File file) {
		try {
			fis = new FileInputStream(file);
			pro = new Properties();
			pro.load(fis);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return pro;
	}
	
	/**
	 * 输入Properties文件路径和key值，返回对应的值
	 * @param path
	 * @param key
	 * @return
	 */
	public String getStringValueOfKey(String path,String key) {
		File file = new File(path);
		Properties pro = load(file);
		String value = (String) pro.get(key);
		return value;
	}
	
}
