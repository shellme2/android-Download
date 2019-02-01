package com.eebbk.bfc.sdk.download.check;

import com.eebbk.bfc.sdk.download.exception.DownloadCheckException;

/**
 * Desc: 校验器接口
 * Author: llp
 * Create Time: 2016-10-11 15:29
 * Email: jacklulu29@gmail.com
 */

public interface ICheckCallback {

    /**
     * 校验文件开始
     */
    void onStart();

    /**
     * 校验中
     * @param totalSize 总大小
     * @param finishedSize 已完成大小
     */
    void onProgress(long totalSize, long finishedSize);

    /**
     * 校验文件成功
     */
    void onSuccess();

    /**
     * 校验文件出错了
     * @param e 异常
     */
    void onError(DownloadCheckException e);

}
