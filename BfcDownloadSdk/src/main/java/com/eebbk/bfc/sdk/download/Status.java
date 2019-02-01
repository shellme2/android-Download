package com.eebbk.bfc.sdk.download;

/**
 * Desc: 下载状态常量
 * Author: llp
 * Create Time: 2016-09-28 8:43
 * Email: jacklulu29@gmail.com
 */

public interface Status {

    /**
     * 无效状态
     */
    int DOWNLOAD_INVALID = -1;
    /**
     * 下载等待中
     */
    int DOWNLOAD_WAITING = 0;
    /**
     * 下载已开始执行runnable
     */
    int DOWNLOAD_STARTED = 1;
    /**
     * 下载已连接
     */
    int DOWNLOAD_CONNECTED = 2;
    /**
     * 下载中
     */
    int DOWNLOAD_PROGRESS = 3;
    /**
     * 下载暂停
     */
    int DOWNLOAD_PAUSE = 4;
    /**
     * 下载重试
     */
    int DOWNLOAD_RETRY = 5;
    /**
     * 下载失败
     */
    int DOWNLOAD_FAILURE = 6;
    /**
     * 下载成功
     */
    int DOWNLOAD_SUCCESS = 7;

    /**
     * 校验初始化
     */
    int CHECK_STARTED = 10;
    /**
     * 校验中
     */
    int CHECK_PROGRESS = 11;
    /**
     * 校验失败
     */
    int CHECK_FAILURE = 12;
    /**
     * 校验成功
     */
    int CHECK_SUCCESS = 13;

    /**
     * 解压开始
     */
    int UNPACK_STARTED = 20;
    /**
     * 解压中
     */
    int UNPACK_PROGRESS = 21;
    /**
     * 解压失败
     */
    int UNPACK_FAILURE = 22;
    /**
     * 解压成功
     */
    int UNPACK_SUCCESS = 23;
}
