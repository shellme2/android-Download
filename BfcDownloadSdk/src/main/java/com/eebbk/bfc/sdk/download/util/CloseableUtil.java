package com.eebbk.bfc.sdk.download.util;

import android.database.Cursor;

import java.io.Closeable;

/**
 * Desc: Closeable关闭工具类，用于所有实现Closeable的类，比如数据库连接、游标等等
 * Author: llp
 * Create Time: 2016年5月6日 下午3:50:23
 * Email: jacklulu29@gmail.com
 */
public class CloseableUtil {

	private CloseableUtil(){
		// private construct
	}
	
	/**
	 * 关闭Closeable
	 * @param closeable Closeable接口
	 */
	public static void close(Closeable closeable){
		if(closeable != null){
			try {
				closeable.close();
                closeable = null;
			} catch (Exception e) {
				LogUtil.w(" close error: " + e);
				closeable = null;
			}
		}
	}

	/**
	 * 关闭Cursor
	 * @param closeable Closeable接口
	 */
	public static void close(Cursor closeable){
		if(closeable != null){
			try {
				closeable.close();
			} catch (Exception e) {
				LogUtil.w(" close error: " + e);
			}
		}
	}

}
