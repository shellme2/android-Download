package com.eebbk.bfc.sdk.download.listener;

import com.eebbk.bfc.sdk.downloadmanager.ITask;

/**
 * Desc: 文件解压监听
 * Author: llp
 * Create Time: 2016-09-29 18:16
 * Email: jacklulu29@gmail.com
 */

public interface OnUnpackListener {

    /**
     * 解压开始
     *
     * @param task         当前任务
     * @param totalSize 文件总大小，单位Bytes
     */
    void onUnpackStarted(final ITask task, final long totalSize);

    /**
     * 解压中,暂不支持，后续开放
     *
     * @param task 当前任务
     * @param finishedSize 解压已完成大小，单位Bytes
     * @param totalSize 文件总大小，单位Bytes
     */
    void onUnpacking(final ITask task, final long finishedSize, final long totalSize);

    /**
     * 解压失败
     *
     * @param task        当前任务
     * @param errorCode 错误码
     * @param throwable 异常
     */
    void onUnpackFailure(final ITask task, final String errorCode, final Throwable throwable);

    /**
     * 解压成功
     *
     * @param task    当前任务
     */
    void onUnpackSuccess(final ITask task);

}
