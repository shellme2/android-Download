package com.eebbk.bfc.sdk.download.db.asyn;

public interface DbOperation {
	
	/**
	 * 执行操作
	 */
	void operation();
	
	/**
	 * 是否同一组操作，假如返回true则新的DbOperation对象会替换旧的DbOperation对象
	 * 
	 * @param OldOperation  旧操作对象
	 * @return 是可替换，否不可替换
	 */
	boolean canReplace(DbOperation OldOperation);

	String getMessage();
	
	Object getData();

	void clear();
}