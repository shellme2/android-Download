package com.eebbk.bfc.sdk.download.listener;

/**
 * Desc: 下载任务操作监听，任务添加、删除时调用
 * Author: llp
 * Create Time: 2016-12-11 19:01
 * Email: jacklulu29@gmail.com
 */

public interface OnDownloadOperationListener {

    /**
     * 任务成功添加后被调用
     * @param taskId 任务id
     */
    void onTaskAdd(final int taskId);

    /**
     * 任务被删除后调用
     * @param taskId 任务id
     */
    void onTaskDelete(final int taskId);

}
