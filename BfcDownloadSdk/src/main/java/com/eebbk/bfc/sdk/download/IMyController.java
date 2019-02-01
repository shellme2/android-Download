package com.eebbk.bfc.sdk.download;

import com.eebbk.bfc.sdk.download.listener.OnDownloadConnectListener;

public interface IMyController {

	/**
	 * 启动下载服务，让下载服务先起来可以缩短任务操作的时间，避免任务操作延时
	 */
//	void startService();

	/**
	 * 立即停止下载服务，可能会造成任务进度的丢失，建议不使用
	 */
//	void stopService();

	/**
	 * 如果空闲自动停止下载服务<br/>
	 * 调用此方法表示，想要结束下载服务，将会首先清理掉所有监听，下载任务一旦完成即会自动停止<br/>
	 * 建议应用退出时调用此方法，清理所有监听
	 */
//	void stopServiceIfIdle();

	/**
	 * 注册服务连接监听，不再使用时请及时调用{@link #unregisterConnectionListener(OnDownloadConnectListener)}方法注销监听，避免内存泄露
	 * @param listener 服务连接监听
	 */
	void registerConnectionListener(OnDownloadConnectListener listener);

	/**
	 * 注销服务连接监听
	 * @param listener 服务连接监听
	 */
	void unregisterConnectionListener(OnDownloadConnectListener listener);

	/**
	 * 销毁对象，注销相关的监听
	 * 在适当的时候请务必调用
	 *
	 */
//	void onDestroy();

}
