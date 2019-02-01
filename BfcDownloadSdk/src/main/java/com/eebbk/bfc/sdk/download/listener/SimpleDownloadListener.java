package com.eebbk.bfc.sdk.download.listener;

import com.eebbk.bfc.sdk.downloadmanager.ITask;

/**
 * Desc: 单个下载任务监听简单实现类，使用时继承只需要覆写自己需要的方法即可
 * Author: llp
 * Create Time: 2016年5月7日 上午9:22:38
 * Email: jacklulu29@gmail.com
 */
public class SimpleDownloadListener implements OnDownloadListener, OnCheckListener, OnUnpackListener {

	@Override
	public void onDownloadWaiting(ITask task) {
		// TODO: 2016/10/19
	}

	@Override
	public void onDownloadStarted(ITask task) {
		// TODO: 2016/10/19
	}

	@Override
	public void onDownloadConnected(ITask task, boolean resuming, long finishedSize, long totalSize) {
		// TODO: 2016/10/19
	}

	@Override
	public void onDownloading(ITask task, long finishedSize, long totalSize) {
		// TODO: 2016/10/19
	}

	@Override
	public void onDownloadPause(ITask task, String errorCode) {
		// TODO: 2016/10/19
	}

	@Override
	public void onDownloadRetry(ITask task, int retries, String errorCode, Throwable throwable) {
		// TODO: 2016/10/19
	}

	@Override
	public void onDownloadFailure(ITask task, String errorCode, Throwable throwable) {
		// TODO: 2016/10/19
	}

	@Override
	public void onDownloadSuccess(ITask task) {
		// TODO: 2016/10/19
	}

	@Override
	public void onCheckStarted(ITask task, long totalSize) {
		// TODO: 2016/10/19
	}

	@Override
	public void onChecking(ITask task, long finishedSize, long totalSize) {
		// TODO: 2016/10/19
	}

	@Override
	public void onCheckFailure(ITask task, String errorCode, Throwable throwable) {
		// TODO: 2016/10/19
	}

	@Override
	public void onCheckSuccess(ITask task) {
		// TODO: 2016/10/19
	}

	@Override
	public void onUnpackStarted(ITask task, long totalSize) {
		// TODO: 2016/10/19
	}

	@Override
	public void onUnpacking(ITask task, long finishedSize, long totalSize) {
		// TODO: 2016/10/19
	}

	@Override
	public void onUnpackFailure(ITask task, String errorCode, Throwable throwable) {
		// TODO: 2016/10/19
	}

	@Override
	public void onUnpackSuccess(ITask task) {
		// TODO: 2016/10/19
	}
}
