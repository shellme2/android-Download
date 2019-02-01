package com.eebbk.bfc.sdk.download.unpack;

import com.eebbk.bfc.sdk.download.exception.DownloadUnpackException;

/**
 * Desc: 下载解压回调
 * Author: llp
 * Create Time: 2016-10-11 8:53
 * Email: jacklulu29@gmail.com
 */

public interface IDownloadUnpackCallback {

    /**
     * 解压开始
     */
    void onStart(String targetPath);

    /**
     * 解压进度
     * @param totalSize 总大小
     * @param finishedSize 已完成大小
     */
    void onProgress(long totalSize, long finishedSize);

    /**
     * 解压文件成功
     */
    void onSuccess();

    /**
     * 解压文件出错了
     * @param e 异常
     */
    void onError(DownloadUnpackException e);
}
