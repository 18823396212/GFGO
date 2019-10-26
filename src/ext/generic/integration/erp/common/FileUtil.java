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
package ext.generic.integration.erp.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
/**
 * 文件操作工具类
 * 
 * @author 魏文杰 
 * @Version 1.0
 * @Date 2013-07-09
 */ 
public class FileUtil {
	/**
	 * 关闭Reader文件流
	 * 
	 * @param reader
	 */
	public static void closeStream(Reader reader) {
		try {
			if(reader != null){
				reader.close();
			}
		} catch (IOException e) {
			reader = null ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 关闭InputStream文件流
	 * 
	 * @param inStream
	 */
	public static void closeStream(InputStream inStream) {
		try {
			if(inStream != null){
				inStream.close();
			}
		} catch (IOException e) {
			inStream = null ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 关闭Writer文件流
	 * 
	 * @param writer
	 */
	public static void closeStream(Writer writer) {
		try {
			if(writer != null){
				writer.close();
			}
		} catch (IOException e) {
			writer = null ;
			e.printStackTrace();
		}
	}
	
	/**
	 * 关闭OutputStream文件流
	 * 
	 * @param outStream
	 */
	public static void closeStream(OutputStream outStream) {
		try {
			if(outStream != null){
				outStream.close();
			}
		} catch (IOException e) {
			outStream = null ;
			e.printStackTrace();
		}
	}
}
