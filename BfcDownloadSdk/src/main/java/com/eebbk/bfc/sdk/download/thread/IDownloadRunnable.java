package com.eebbk.bfc.sdk.download.thread;

/**
 * Desc:
 * Author: llp
 * Create Time: 2016-10-09 22:59
 * Email: jacklulu29@gmail.com
 */

public interface IDownloadRunnable extends Runnable {

    /**
     * 获取task id
     */
    int getTaskId();

    /**
     * 获取runnable id
     * @return runnable ID
     */
    String getRunnableId();

    /**
     * runnable是否有效（执行中？）
     * @return true有效，false无效
     */
    boolean isValid();

    /**
     * 取消runnable
     */
    void cancelRunnable();

    /**
     * 是否被取消
     * @return true被取消，false没有
     */
    boolean isCanceled();

}
