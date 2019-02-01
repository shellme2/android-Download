package com.eebbk.bfc.sdk.download.unpack;

import android.text.TextUtils;

import com.eebbk.bfc.sdk.download.exception.DownloadUnpackException;
import com.eebbk.bfc.sdk.download.exception.ErrorCode;
import com.eebbk.bfc.sdk.download.util.LogUtil;

import java.io.File;

/**
 * Desc: 解压文件基类
 * Author: llp
 * Create Time: 2016-10-11 9:31
 * Email: jacklulu29@gmail.com
 */

public abstract class DownloadBaseUnpacker implements IDownloadUnpacker {

    public interface Creator {
        DownloadBaseUnpacker create();
    }

    private IDownloadUnpackCallback mCallback;

    @Override
    public void unpack(String sourceFilePath, String targetPath, boolean deleteSourceAfterUnpack, IDownloadUnpackCallback callback) {
        try{
            mCallback = callback;
            String path = parseTargetPath(sourceFilePath, targetPath);
            callback.onStart(path);
            boolean result = unpackByParseTargetPath(sourceFilePath, path, deleteSourceAfterUnpack);
            if(result){
                callback.onSuccess();
                LogUtil.i(" unpack successed; targetPath["+targetPath+"] ");
            } else {
                LogUtil.e(" unpack sourceFilePath["+sourceFilePath+"] targetPath["+targetPath+"] failed ");
                callback.onError( new DownloadUnpackException(ErrorCode.Values.CHECK_FAILED, " unpack failed "));
            }
        } catch (DownloadUnpackException e){
            LogUtil.e(e, " unpack sourceFilePath["+sourceFilePath+"] targetPath["+targetPath+"] error ");
            callback.onError(e);
        } finally {
            mCallback = null;
        }
    }

    /**
     * 解压文件，不过解压文件保存路径经过了重新解析
     *
     * @param sourceFilePath 原始文件路径
     * @param targetPath 已被解析过的目标保存路径
     */
    public abstract boolean unpackByParseTargetPath(String sourceFilePath, String targetPath,
        boolean deleteSourceAfterUnpack) throws DownloadUnpackException;

    public void unpackProgress(long totalSize, long finishedSize){
        if(mCallback != null){
            mCallback.onProgress(totalSize, finishedSize);
        }
    }

    /**
     * 解析解压文件保存路径
     *
     * @param sourceFilePath 原始文件路径
     * @param defaultTargetPath 默认解压文件保存路径，可能为null
     * @return 最终解压文件保存路径
     */
    public String parseTargetPath(String sourceFilePath, String defaultTargetPath) {
        String resultTargetPath = null;
        // 优先使用设置的保存路径，默认保存路径为不为空
        if(!TextUtils.isEmpty(defaultTargetPath)){
            if(!defaultTargetPath.endsWith("/")){
                defaultTargetPath = defaultTargetPath + File.separator;
            }
            resultTargetPath = defaultTargetPath;
        } else {
            if(!TextUtils.isEmpty(sourceFilePath)){
                // 截取存储路径,设置目标路径
                resultTargetPath = getTargetPath(sourceFilePath) + getFileNameNoEx(sourceFilePath) + File.separator;
            }
        }
        return resultTargetPath;
    }

    /**
     * 截取存储地址,返回目标存储路径 例如: "/mnt/sdcard/学科同步/";
     *
     * @param savePath
     *            路径
     * @return 处理过的路径
     */
    public String getTargetPath(String savePath) {
        if ((savePath != null) && (savePath.length() > 0)) {
            int start = savePath.indexOf('/');
            int end = savePath.lastIndexOf('/');
            if ((end > -1) && (end < (savePath.length() - 1))) {
                return savePath.substring(start, end + 1);
            }
        }
        return savePath;
    }

    /**
     * Java文件操作 从全路径中获取不带后缀的文件名
     */
    public String getFileNameNoEx(String allSavePath) {
        File targetFile = new File(allSavePath);
        String filename = targetFile.getName();
        LogUtil.d("getFileNameNoEx : " + filename);
        if (filename.length() > 0) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }

        return filename;
    }

}
