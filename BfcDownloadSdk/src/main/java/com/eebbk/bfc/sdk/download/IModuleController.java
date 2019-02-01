package com.eebbk.bfc.sdk.download;

import com.eebbk.bfc.sdk.download.listener.IDownloadListener;
import com.eebbk.bfc.sdk.download.listener.OnDownloadOperationListener;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

import java.util.ArrayList;

/**
 * 下载模块操作基础接口
 */
public interface IModuleController {

	/**
	 * 查询指定模块的任务
	 * @param status 根据下载状态查询
	 * @param moduleName    模块名
	 * @return 相应状态的下载列表
	 */
	ArrayList<ITask> getTaskByStatus(int status, String moduleName);

	/**
	 * 获取指定模块的所有的下载任务
	 * @param moduleName    模块名
	 * @return 所有任务列表
	 */
	ArrayList<ITask> getTask(String moduleName);

	/**
	 * 查询指定模块的任务 根据扩展字段
	 * @param keys 键数组
	 * @param values 值数组
	 * @param moduleName    模块名
	 * @return 相应的任务列表
	 */
	ArrayList<ITask> getTaskByExtras(String[] keys, String[] values, String moduleName);

	/**
	 * 注册指定模块的全局监听，可以监听所有下载任务
	 * @param listener      监听
	 * @param moduleName 模块名
	 * @return 注册成功的数量
	 */
	boolean registerTaskListener(IDownloadListener listener, String moduleName);

	/**
	 * 注销指定模块的全局监听
	 * @param listener      监听
	 * @param moduleName 模块名
	 * @return 注销成功的数量
	 */
	boolean unregisterTaskListener(IDownloadListener listener, String moduleName);

	/**
	 * 注册下载操作监听，可以监听任务被添加、删除等操作，给指定模块注册
	 * @param listener 监听
	 * @param moduleName 模块名
	 * @return true为成功，false为失败
	 */
	boolean registerOperationListener(OnDownloadOperationListener listener, String moduleName);

	/**
	 * 注销下载操作监听，不使用时请及时注销，不注销监听将可能导致内存泄露，给指定模块注销
	 * @param listener 监听
	 * @param moduleName 模块名
	 * @return true为成功，false为失败
	 */
	boolean unregisterOperationListener(OnDownloadOperationListener listener, String moduleName);
}
