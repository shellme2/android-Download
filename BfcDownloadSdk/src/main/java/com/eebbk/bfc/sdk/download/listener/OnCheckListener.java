package com.eebbk.bfc.sdk.download.listener;

import com.eebbk.bfc.sdk.downloadmanager.ITask;

/**
 * Desc: 文件校验监听
 * Author: llp
 * Create Time: 2016-09-29 18:16
 * Email: jacklulu29@gmail.com
 */

public interface OnCheckListener {

    /**
     * 开始校验
     *
     * @param task   当前任务
     * @param totalSize 文件总大小，单位Bytes
     */
    void onCheckStarted(final ITask task, final long totalSize);

    /**
     * 校验中,暂不支持，后续开放
     *
     * @param task       当前任务
     * @param finishedSize 已校验大小，单位Bytes
     * @param totalSize 文件总大小，单位Bytes
     */
    void onChecking(final ITask task, final long finishedSize, final long totalSize);

    /**
     * 校验失败
     *
     * @param task         当前任务
     * @param errorCode 错误码
     * @param throwable 异常
     */
    void onCheckFailure(final ITask task, final String errorCode, final Throwable throwable);

    /**
     * 校验成功
     *
     * @param task   当前任务
     */
    void onCheckSuccess(final ITask task);

}
