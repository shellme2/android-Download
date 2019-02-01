package com.eebbk.bfc.sdk.download.check;

import com.eebbk.bfc.sdk.download.exception.DownloadCheckException;
import com.eebbk.bfc.sdk.download.exception.ErrorCode;
import com.eebbk.bfc.sdk.download.util.LogUtil;

/**
 * Desc: 校验文件基础类
 * Author: llp
 * Create Time: 2016-11-06 21:00
 * Email: jacklulu29@gmail.com
 */

public abstract class BaseValidator implements IValidator {

    public interface Creator {
        /**
         * 构建校验器
         *
         * @return 校验器
         */
        BaseValidator create();

        /**
         * 获取校验器类型
         *
         * @return 校验类型
         */
        String getType();
    }

    private ICheckCallback mCallback;

    @Override
    public boolean isKeyValid(String checkCode) {
        return false;
    }

    @Override
    public void check(String fileSourcePath, String checkType, String checkCode, ICheckCallback callback) {
        try{
            mCallback = callback;
            callback.onStart();
            boolean result = checkFile(fileSourcePath, checkType, checkCode);
            if(result){
                callback.onSuccess();
            } else {
                LogUtil.e(" check sourceFilePath["+fileSourcePath+"] checkType["+checkType+"] checkCode["+checkCode+"] failed ");
                callback.onError( new DownloadCheckException(ErrorCode.Values.CHECK_FAILED, " unpack failed "));
            }
        } catch (DownloadCheckException e){
            LogUtil.e(" check sourceFilePath["+fileSourcePath+"] checkType["+checkType+"] checkCode["+checkCode+"] error ");
            callback.onError(e);
        } finally {
            mCallback = null;
        }
    }

    public abstract boolean checkFile(String fileSourcePath, String checkType, String checkCode) throws DownloadCheckException;

    public void checkProgress(long totalSize, long finishedSize){
        if(mCallback != null){
            mCallback.onProgress(totalSize, finishedSize);
        }
    }

}
