package com.eebbk.bfc.sdk.download.listener;

/**
 * Desc: 事件监听接口
 * Author: llp
 * Create Time: 2016-10-25 15:51
 * Email: jacklulu29@gmail.com
 */

public interface IDownloadListener {

    boolean isEmpty();

    int getId();

    IDownloadListener setId(int id);

    /**
     * 设置文件下载监听，可监听下载状态变化、下载进度等
     * @param listener 下载监听
     * @return 下载监听
     */
    IDownloadListener setOnDownloadListener(OnDownloadListener listener);

    /**
     * 获取文件下载监听
     * @return 下载监听
     */
    OnDownloadListener getOnDownloadListener();

    /**
     * 设置文件校验监听，可监听校验文件状态变化、进度等
     * @param listener 校验监听
     * @return 下载监听
     */
    IDownloadListener setOnCheckListener(OnCheckListener listener);

    /**
     * 获取文件校验监听
     *
     * @return 校验监听
     */
    OnCheckListener getOnCheckListener();

    /**
     * 设置文件解压监听，可监听解压文件状态变化、进度等
     * @param listener 解压监听
     * @return 下载监听
     */
    IDownloadListener setOnUnpackListener(OnUnpackListener listener);

    /**
     * 获取文件解压监听
     *
     * @return 解压监听
     */
    OnUnpackListener getOnUnpackListener();

    /**
     * 任务是否被标记为取消（删除任务），标记为取消代表任务已被销毁，事件监听都将无效
     *
     * @return true已取消，false没有取消
     */
    boolean isCanceled();

    /**
     * 设置任务是否被标记为取消
     * @param canceled true取消
     * @return 下载监听
     */
    IDownloadListener setCanceled(boolean canceled);

}
