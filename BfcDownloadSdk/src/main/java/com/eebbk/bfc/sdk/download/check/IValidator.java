package com.eebbk.bfc.sdk.download.check;

import android.support.annotation.WorkerThread;

/**
 * Desc: 校验器接口
 * Author: llp
 * Create Time: 2016-10-11 15:29
 * Email: jacklulu29@gmail.com
 */

public interface IValidator {

    /**
     * 验证校验码是否有效
     *
     * @param checkCode 校验码
     * @return true有效，false无效
     */
    boolean isKeyValid(String checkCode);

    /**
     * 校验指定的文件
     *
     * @param fileSourcePath 要校验的文件路径
     * @param checkType  校验类型
     * @param checkCode 校验码
     * @param callback 回调方法
     */
    @WorkerThread
    void check(String fileSourcePath, String checkType, String checkCode, ICheckCallback callback);

}
