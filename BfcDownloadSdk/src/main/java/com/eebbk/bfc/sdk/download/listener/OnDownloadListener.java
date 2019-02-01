package com.eebbk.bfc.sdk.download.listener;

import com.eebbk.bfc.sdk.downloadmanager.ITask;

/**
 * Desc: 单个任务下载监听
 * 
 * Author: llp
 * Create Time: 2016年5月4日 下午9:13:10
 * Email: jacklulu29@gmail.com
 */
public interface OnDownloadListener {

	/**
	 * 等待中
	 *
	 * @param task 当前任务
	 */
	void onDownloadWaiting(final ITask task);

	/**
	 * 已开始执行下载任务
	 * 
	 * @param task 当前任务
	 */
	void onDownloadStarted(final ITask task);

	/**
	 * 已连接下载服务器
	 *
	 * @param task  当前任务
	 * @param resuming 是否恢复是否恢复
	 * @param finishedSize 已下载文件大小，单位Bytes
	 * @param totalSize 文件总大小，单位Bytes
	 */
	void onDownloadConnected(final ITask task, final boolean resuming, final long finishedSize, final long totalSize);
	
	/**
	 * 下载中
	 * 
	 * @param task   当前任务
	 * @param finishedSize 已下载文件大小，单位Bytes
	 * @param totalSize 文件总大小，单位Bytes
	 */
	void onDownloading(final ITask task, final long finishedSize, final long totalSize);

	/**
	 * <pre>已暂停，任务被暂停时回调
	 * 暂停原因由errorCode错误码标识，目前造成暂停的原因只有三种：
	 * 1.用户手动暂停，必须手动恢复下载
	 * 2.网络原因，网络正常后会自动恢复下载
	 * 3.存储空间不足造成暂停，清理后必须手动恢复下载
	 * 具体判断方法可以调用{@link ITask#isPauseByUser()}、{@link ITask#isPauseByNetwork()}、{@link ITask#isPauseByOutOfSpace()}
	 * 具体错误码：
	 * 1.用户手动暂停：{@link com.eebbk.bfc.sdk.download.exception.ErrorCode.Values#DOWNLOAD_PAUSE_BY_USER}
	 * 2.网络原因暂停：{@link com.eebbk.bfc.sdk.download.exception.ErrorCode.Values#DOWNLOAD_NETWORK_UNKNOWN}、
	 * {@link com.eebbk.bfc.sdk.download.exception.ErrorCode.Values#DOWNLOAD_NETWORK_NO_CONNECTION}、
	 * {@link com.eebbk.bfc.sdk.download.exception.ErrorCode.Values#DOWNLOAD_NETWORK_UNUSABLE_DUE_TO_SIZE}、
	 * {@link com.eebbk.bfc.sdk.download.exception.ErrorCode.Values#DOWNLOAD_NETWORK_RECOMMENDED_UNUSABLE_DUE_TO_SIZE}、
	 * {@link com.eebbk.bfc.sdk.download.exception.ErrorCode.Values#DOWNLOAD_NETWORK_CANNOT_USE_ROAMING}、
	 * {@link com.eebbk.bfc.sdk.download.exception.ErrorCode.Values#DOWNLOAD_NETWORK_TYPE_DISALLOWED_BY_REQUESTOR}、
	 * {@link com.eebbk.bfc.sdk.download.exception.ErrorCode.Values#DOWNLOAD_NETWORK_BLOCKED}、
	 * {@link com.eebbk.bfc.sdk.download.exception.ErrorCode.Values#DOWNLOAD_NETWORK_NO_INIT_CONTEXT}
	 * 3.存储空间不足：{@link com.eebbk.bfc.sdk.download.exception.ErrorCode.Values#DOWNLOAD_OUT_OF_SPACE}
	 * </pre>
	 * @param task    当前任务
	 * @param errorCode 错误码，标识当前暂停原因
	 */
	void onDownloadPause(final ITask task, final String errorCode);

	/**
	 * 正在重试
	 *
	 * @param task       当前任务
	 * @param retries 重试次数
	 * @param errorCode  错误码，标识错误代号
	 * @param throwable 异常
	 */
	void onDownloadRetry(final ITask task, final int retries, final String errorCode, final Throwable throwable);
	
	/**
	 * 下载失败
	 * 
	 * @param task         当前任务
	 * @param errorCode 错误码，标识错误代号
	 * @param throwable 异常
	 */
	void onDownloadFailure(final ITask task, final  String errorCode, final Throwable throwable);
	
	/**
	 * 下载成功
	 * 
	 * @param task 当前任务
	 */
	void onDownloadSuccess(final ITask task);
	
}